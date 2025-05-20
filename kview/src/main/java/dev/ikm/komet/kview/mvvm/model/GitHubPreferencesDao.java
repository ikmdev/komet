package dev.ikm.komet.kview.mvvm.model;

import dev.ikm.komet.preferences.KometPreferences;
import dev.ikm.komet.preferences.Preferences;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Optional;
import java.util.prefs.BackingStoreException;

import static dev.ikm.komet.kview.mvvm.view.changeset.exchange.GitPropertyName.*;

/**
 * Data Access Object for GitHub preferences.
 */
public class GitHubPreferencesDao {

    private static final Logger LOG = LoggerFactory.getLogger(GitHubPreferencesDao.class);

    /**
     * Saves GitHub preferences to the user preferences store.
     *
     * @param gitHubPreferences the GitHubPreferences object containing the preferences to save
     * @throws BackingStoreException if there is an error syncing preferences
     */
    public void save(GitHubPreferences gitHubPreferences) throws BackingStoreException {
        final KometPreferences userPreferences = Preferences.get().getUserPreferences();
        setGitUrl(userPreferences, gitHubPreferences.gitUrl());
        setGitEmail(userPreferences, gitHubPreferences.gitEmail());
        setGitUsername(userPreferences, gitHubPreferences.gitUsername());
        setGitPassword(userPreferences, gitHubPreferences.gitPassword());
        userPreferences.sync();
    }

    /**
     * Saves GitHub preferences to the user preferences store.
     *
     * @param gitUrl The Git repository URL
     * @param gitEmail The user's Git email address
     * @param username The GitHub username
     * @param gitPassword The GitHub password or token
     * @throws BackingStoreException if there is an error syncing preferences
     */
    public void save(String gitUrl, String gitEmail, String username, char[] gitPassword)
            throws BackingStoreException {
        final KometPreferences userPreferences = Preferences.get().getUserPreferences();
        setGitUrl(userPreferences, gitUrl);
        setGitEmail(userPreferences, gitEmail);
        setGitUsername(userPreferences, username);
        setGitPassword(userPreferences, gitPassword);
        userPreferences.sync();
    }

    /**
     * Loads GitHub preferences from the user preferences store.
     *
     * @return An Optional containing the GitHubPreferences if all preferences are present,
     *         or an empty Optional if any preference is missing
     */
    public Optional<GitHubPreferences> load() {
        final KometPreferences userPreferences = Preferences.get().getUserPreferences();
        Optional<String> gitUrl = getGitUrl(userPreferences);
        Optional<String> gitEmail = getGitEmail(userPreferences);
        Optional<String> username = getGitUsername(userPreferences);
        Optional<char[]> gitPassword = getGitPassword(userPreferences);

        final boolean preferencesExist = gitUrl.isPresent()
                && gitEmail.isPresent()
                && username.isPresent()
                && gitPassword.isPresent();

        if (!preferencesExist) {
            LOG.warn("GitHub preferences not found. Please set them up.");
            return Optional.empty();
        }

        return Optional.of(new GitHubPreferences(gitUrl.get(), gitEmail.get(), username.get(), gitPassword.get()));
    }

    /**
     * Validates the GitHub preferences.
     *
     * @return true if all required fields are present and valid, false otherwise
     */
    public boolean validate() {
        final KometPreferences userPreferences = Preferences.get().getUserPreferences();
        Optional<String> gitUrl = getGitUrl(userPreferences);
        Optional<String> gitEmail = getGitEmail(userPreferences);
        Optional<String> username = getGitUsername(userPreferences);
        Optional<char[]> gitPassword = getGitPassword(userPreferences);

        return gitUrl.isPresent() && gitEmail.isPresent() && username.isPresent() && gitPassword.isPresent();
    }

    /**
     * Deletes the GitHub preferences from the user preferences store.
     *
     * @throws BackingStoreException if there is an error deleting the preferences
     */
    public void delete() throws BackingStoreException {
        final KometPreferences userPreferences = Preferences.get().getUserPreferences();
        userPreferences.remove(GIT_URL);
        userPreferences.remove(GIT_EMAIL);
        userPreferences.remove(GIT_USERNAME);
        userPreferences.remove(GIT_PASSWORD);
        userPreferences.sync();
        LOG.info("GitHub preferences deleted successfully.");
    }

    /**
     * Stores the Git repository URL in the specified preferences.
     *
     * @param preferences The preferences instance to update
     * @param gitUrl The Git repository URL to store
     */
    private void setGitUrl(KometPreferences preferences, String gitUrl) {
        preferences.put(GIT_URL, gitUrl);
    }

    /**
     * Retrieves the Git repository URL from the specified preferences.
     *
     * @param preferences The preferences instance to read from
     * @return The Git repository URL, or an empty Optional if not found
     */
    public Optional<String> getGitUrl(KometPreferences preferences) {
        return preferences.get(GIT_URL);
    }

    /**
     * Stores the Git user email in the specified preferences.
     *
     * @param preferences The preferences instance to update
     * @param gitEmail The Git user email to store
     */
    private void setGitEmail(KometPreferences preferences, String gitEmail) {
        preferences.put(GIT_EMAIL, gitEmail);
    }

    /**
     * Retrieves the Git user email from the specified preferences.
     *
     * @param preferences The preferences instance to read from
     * @return The Git user email, or an empty Optional if not found
     */
    public Optional<String> getGitEmail(KometPreferences preferences) {
        return preferences.get(GIT_EMAIL);
    }

    /**
     * Stores the Git username in the specified preferences.
     *
     * @param preferences The preferences instance to update
     * @param username The Git username to store
     */
    private void setGitUsername(KometPreferences preferences, String username) {
        preferences.put(GIT_USERNAME, username);
    }

    /**
     * Retrieves the Git username from the specified preferences.
     *
     * @param preferences The preferences instance to read from
     * @return The Git username, or an empty Optional if not found
     */
    public Optional<String> getGitUsername(KometPreferences preferences) {
        return preferences.get(GIT_USERNAME);
    }

    /**
     * Stores the Git gitPassword in the specified preferences.
     *
     * @param preferences The preferences instance to update
     * @param gitPassword The Git gitPassword to store as a character array
     */
    private void setGitPassword(KometPreferences preferences, char[] gitPassword) {
        preferences.putPassword(GIT_PASSWORD, gitPassword);
    }

    /**
     * Retrieves the Git gitPassword from the specified preferences.
     *
     * @param preferences The preferences instance to read from
     * @return The Git gitPassword, or an empty Optional if not found
     */
    public Optional<char[]> getGitPassword(KometPreferences preferences) {
        return preferences.getPassword(GIT_PASSWORD);
    }
}
