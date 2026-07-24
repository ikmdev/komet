/*
 * Copyright © 2015 Integrated Knowledge Management (support@ikm.dev)
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package dev.ikm.komet.pluginresolver;

import dev.ikm.komet.preferences.KometPreferences;
import dev.ikm.komet.preferences.PreferencesService;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.HexFormat;
import java.util.Objects;
import java.util.Optional;
import java.util.prefs.BackingStoreException;

/**
 * {@link RepositoryCredentialStore} backed by {@link KometPreferences}, using the same
 * {@code putPassword}/{@code getPassword} mechanism as the existing Git credential store
 * ({@code GitHubPreferencesDao} in {@code komet-kview}). Credentials are stored per-repository
 * under a dedicated child node of the user preferences root, one node per repository id.
 *
 * <p><strong>Known limitation:</strong> {@code KometPreferences.putPassword} encrypts with a
 * hardcoded literal key ({@code "obfuscate-komet"}) — this obfuscates a credential against
 * casual disk inspection but is not real secret protection against anyone who reads the jar.
 * This is an existing, inherited limitation of the underlying preferences API, not something
 * specific to this store; it can be swapped out later without changing the
 * {@link RepositoryCredentialStore} interface.
 */
public final class KometPreferencesCredentialStore implements RepositoryCredentialStore {

    private static final String CREDENTIALS_NODE_NAME = "dev.ikm.komet.pluginresolver.repositoryCredentials";
    private static final String USERNAME_KEY = "username";
    private static final String PASSWORD_KEY = "password";

    /**
     * Longest readable prefix kept in a derived node name. {@link java.util.prefs.Preferences}
     * caps a node name at 80 characters; this leaves room for the separator and digest below.
     */
    private static final int READABLE_PREFIX_LIMIT = 40;

    /** Hex characters of the repository id's digest appended to every derived node name. */
    private static final int DIGEST_LENGTH = 16;

    private final KometPreferences credentialsNode;

    /**
     * Creates a store backed by the current process's user preferences
     * ({@code PreferencesService.get().getUserPreferences()}).
     */
    public KometPreferencesCredentialStore() {
        this(PreferencesService.get().getUserPreferences());
    }

    /**
     * Creates a store backed by an explicit {@link KometPreferences} root, for testing
     * against a real (non-default) preferences node.
     *
     * @param userPreferences the preferences node to store repository credentials under
     * @throws NullPointerException if {@code userPreferences} is {@code null}
     */
    public KometPreferencesCredentialStore(KometPreferences userPreferences) {
        Objects.requireNonNull(userPreferences, "userPreferences");
        this.credentialsNode = userPreferences.node(CREDENTIALS_NODE_NAME);
    }

    /**
     * {@inheritDoc}
     *
     * @param repositoryId {@inheritDoc}
     * @return {@inheritDoc}
     * @throws NullPointerException if {@code repositoryId} is {@code null}
     */
    @Override
    public Optional<Credentials> get(String repositoryId) {
        Objects.requireNonNull(repositoryId, "repositoryId");
        KometPreferences repositoryNode = credentialsNode.node(nodeName(repositoryId));
        Optional<String> username = repositoryNode.get(USERNAME_KEY);
        if (username.isEmpty()) {
            return Optional.empty();
        }
        Optional<char[]> password = repositoryNode.getPassword(PASSWORD_KEY);
        return password.map(pw -> new Credentials(username.get(), pw));
    }

    /**
     * {@inheritDoc}
     *
     * @param repositoryId {@inheritDoc}
     * @param credentials {@inheritDoc}
     * @throws NullPointerException if either argument is {@code null}
     * @throws CredentialStoreException if the underlying preferences store cannot be synced
     */
    @Override
    public void put(String repositoryId, Credentials credentials) {
        Objects.requireNonNull(repositoryId, "repositoryId");
        Objects.requireNonNull(credentials, "credentials");
        KometPreferences repositoryNode = credentialsNode.node(nodeName(repositoryId));
        repositoryNode.put(USERNAME_KEY, credentials.username());
        repositoryNode.putPassword(PASSWORD_KEY, credentials.password());
        try {
            repositoryNode.sync();
        } catch (BackingStoreException e) {
            throw new CredentialStoreException("Failed to persist credentials for repository " + repositoryId, e);
        }
    }

    /**
     * {@inheritDoc}
     *
     * @param repositoryId {@inheritDoc}
     * @throws NullPointerException if {@code repositoryId} is {@code null}
     * @throws CredentialStoreException if the underlying preferences store cannot be updated
     */
    @Override
    public void remove(String repositoryId) {
        Objects.requireNonNull(repositoryId, "repositoryId");
        KometPreferences repositoryNode = credentialsNode.node(nodeName(repositoryId));
        try {
            repositoryNode.removeNode();
            credentialsNode.sync();
        } catch (BackingStoreException e) {
            throw new CredentialStoreException("Failed to remove credentials for repository " + repositoryId, e);
        }
    }

    /**
     * Derives the preferences node name holding {@code repositoryId}'s credentials.
     *
     * <p>A repository id is routinely a URL — {@code https://nexus.example.org/repository/public/}
     * — but {@link java.util.prefs.Preferences#node(String)} reads its argument as a <em>path</em>:
     * a name containing {@code /} is interpreted as node nesting, and {@code //} is rejected
     * outright with {@code IllegalArgumentException: Consecutive slashes in path}
     * (ikmdev/komet#881). A node name is also capped at 80 characters. This store owns that
     * invariant so every caller can pass whatever identifies a repository.
     *
     * <p>The derived name keeps a bounded, readable prefix — so a preferences browser still shows
     * which repository a node belongs to — and appends a digest of the <em>whole</em> id. The
     * digest is what makes the mapping injective: two ids sharing a prefix, or differing only past
     * the truncation point (a trailing slash, a long path tail), still land on distinct nodes.
     *
     * @param repositoryId the repository id, in any form
     * @return a valid, stable, single-segment preferences node name
     */
    static String nodeName(String repositoryId) {
        StringBuilder readable = new StringBuilder(READABLE_PREFIX_LIMIT);
        for (int i = 0; i < repositoryId.length() && readable.length() < READABLE_PREFIX_LIMIT; i++) {
            char character = repositoryId.charAt(i);
            boolean retained = (character >= 'a' && character <= 'z')
                    || (character >= 'A' && character <= 'Z')
                    || (character >= '0' && character <= '9')
                    || character == '.' || character == '-' || character == '_';
            readable.append(retained ? character : '_');
        }
        return readable + "_" + digest(repositoryId);
    }

    /**
     * Hex-encodes the leading {@link #DIGEST_LENGTH} characters of {@code value}'s SHA-256 digest.
     *
     * @param value the value to digest
     * @return the truncated hex digest
     * @throws IllegalStateException if SHA-256 is unavailable, which every conformant JRE provides
     */
    private static String digest(String value) {
        try {
            MessageDigest sha256 = MessageDigest.getInstance("SHA-256");
            byte[] hash = sha256.digest(value.getBytes(StandardCharsets.UTF_8));
            return HexFormat.of().formatHex(hash).substring(0, DIGEST_LENGTH);
        } catch (NoSuchAlgorithmException e) {
            throw new IllegalStateException("SHA-256 is required to derive a credential node name", e);
        }
    }
}
