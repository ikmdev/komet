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
        KometPreferences repositoryNode = credentialsNode.node(repositoryId);
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
        KometPreferences repositoryNode = credentialsNode.node(repositoryId);
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
        KometPreferences repositoryNode = credentialsNode.node(repositoryId);
        try {
            repositoryNode.removeNode();
            credentialsNode.sync();
        } catch (BackingStoreException e) {
            throw new CredentialStoreException("Failed to remove credentials for repository " + repositoryId, e);
        }
    }
}
