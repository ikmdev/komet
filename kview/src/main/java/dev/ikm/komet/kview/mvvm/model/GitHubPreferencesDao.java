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
package dev.ikm.komet.kview.mvvm.model;

import dev.ikm.komet.preferences.KometPreferences;
import dev.ikm.komet.preferences.Preferences;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Objects;
import java.util.Optional;
import java.util.function.Supplier;
import java.util.prefs.BackingStoreException;

import static dev.ikm.komet.kview.mvvm.view.changeset.exchange.GitPropertyName.GIT_EMAIL;
import static dev.ikm.komet.kview.mvvm.view.changeset.exchange.GitPropertyName.GIT_PASSWORD;
import static dev.ikm.komet.kview.mvvm.view.changeset.exchange.GitPropertyName.GIT_URL;
import static dev.ikm.komet.kview.mvvm.view.changeset.exchange.GitPropertyName.GIT_USERNAME;

/**
 * Data Access Object for the git configuration of a knowledge base's change set exchange.
 * <p>The remote's URL, the email and the user name are kept in the knowledge base's own preferences, so each
 * knowledge base has its own and they are there at the next launch.
 * <p>The password is never written to preferences: a person may commit and push the preferences of a knowledge
 * base to a remote, and what preferences do to a password hides it only from a casual look. It is kept in memory,
 * for every instance in this running Komet, until {@link #forgetPassword()} or Komet quits.
 */
public class GitHubPreferencesDao {

    private static final Logger LOG = LoggerFactory.getLogger(GitHubPreferencesDao.class);

    /** The node of a knowledge base's preferences the configuration is kept under. */
    private static final String EXCHANGE_NODE = "change-set-exchange";

    // Plain keys: a key derived from an enum carries its package, and a moved enum would lose what was stored.
    private static final String URL_KEY = "git-url";
    private static final String EMAIL_KEY = "git-email";
    private static final String USERNAME_KEY = "git-username";

    /** The password given in this running Komet, or {@code null}. A running Komet has one knowledge base open. */
    private static volatile char[] password;

    private final Supplier<KometPreferences> exchangePreferences;

    /**
     * The configuration of the open knowledge base. Its preferences are read when a method needs them, since an
     * instance may be made before a knowledge base is chosen.
     */
    public GitHubPreferencesDao() {
        this(() -> Preferences.get().getConfigurationPreferences().node(EXCHANGE_NODE));
    }

    private GitHubPreferencesDao(Supplier<KometPreferences> exchangePreferences) {
        this.exchangePreferences = exchangePreferences;
    }

    /**
     * Returns a DAO that keeps the configuration in the given preferences node.
     *
     * @param exchangePreferences supplies the node
     * @return a DAO over that node
     */
    public static GitHubPreferencesDao backedBy(Supplier<KometPreferences> exchangePreferences) {
        return new GitHubPreferencesDao(Objects.requireNonNull(exchangePreferences));
    }

    /**
     * Saves the configuration in the knowledge base's preferences, and keeps the password for this running Komet.
     *
     * @param gitHubPreferences the GitHubPreferences object containing the preferences to save
     * @throws BackingStoreException if there is an error syncing preferences
     */
    public void save(GitHubPreferences gitHubPreferences) throws BackingStoreException {
        Objects.requireNonNull(gitHubPreferences, "GitHubPreferences cannot be null");
        save(gitHubPreferences.gitUrl(), gitHubPreferences.gitEmail(), gitHubPreferences.gitUsername(),
                gitHubPreferences.gitPassword());
    }

    /**
     * Saves the configuration in the knowledge base's preferences, and keeps the password for this running Komet.
     *
     * @param gitUrl The Git repository URL
     * @param gitEmail The user's Git email address
     * @param username The GitHub username
     * @param gitPassword The GitHub password or token
     * @throws BackingStoreException if there is an error syncing preferences
     */
    public void save(String gitUrl, String gitEmail, String username, char[] gitPassword)
            throws BackingStoreException {
        password = gitPassword.clone();
        final KometPreferences preferences = exchangePreferences.get();
        preferences.put(URL_KEY, gitUrl);
        preferences.put(EMAIL_KEY, gitEmail);
        preferences.put(USERNAME_KEY, username);
        preferences.sync();
    }

    /**
     * Loads the configuration and the password.
     *
     * @return An Optional containing the GitHubPreferences if all preferences are present,
     *         or an empty Optional if any preference is missing
     */
    public Optional<GitHubPreferences> load() {
        final KometPreferences preferences = exchangePreferences.get();
        final Optional<String> gitUrl = preferences.get(URL_KEY);
        final Optional<String> gitEmail = preferences.get(EMAIL_KEY);
        final Optional<String> username = preferences.get(USERNAME_KEY);
        final char[] givenPassword = password;

        if (gitUrl.isEmpty() || gitEmail.isEmpty() || username.isEmpty() || givenPassword == null) {
            LOG.info("GitHub preferences or password not given yet.");
            return Optional.empty();
        }

        return Optional.of(new GitHubPreferences(gitUrl.get(), gitEmail.get(), username.get(), givenPassword.clone()));
    }

    /**
     * Whether the configuration and the password are all present.
     *
     * @return true if all required fields are present, false otherwise
     */
    public boolean validate() {
        return load().isPresent();
    }

    /**
     * The repository URL kept for the knowledge base.
     *
     * @return the URL, or the empty string if none is kept
     */
    public String storedUrl() {
        return exchangePreferences.get().get(URL_KEY, "");
    }

    /**
     * The email kept for the knowledge base.
     *
     * @return the email, or the empty string if none is kept
     */
    public String storedEmail() {
        return exchangePreferences.get().get(EMAIL_KEY, "");
    }

    /**
     * The user name kept for the knowledge base.
     *
     * @return the user name, or the empty string if none is kept
     */
    public String storedUsername() {
        return exchangePreferences.get().get(USERNAME_KEY, "");
    }

    /**
     * Forgets the password, so it is asked for at the next connection. The knowledge base's configuration stays.
     */
    public void forgetPassword() {
        password = null;
    }

    /**
     * Removes what an earlier Komet kept for all knowledge bases of this computer's user, its password among it.
     * It deleted that when it quit, so it is there only after a quit that did not finish.
     *
     * @throws BackingStoreException if there is an error syncing preferences
     */
    public static void removeUserWidePreferences() throws BackingStoreException {
        final KometPreferences userPreferences = Preferences.get().getUserPreferences();
        userPreferences.remove(GIT_URL);
        userPreferences.remove(GIT_EMAIL);
        userPreferences.remove(GIT_USERNAME);
        userPreferences.remove(GIT_PASSWORD);
        userPreferences.sync();
    }
}
