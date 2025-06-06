package dev.ikm.komet.kview.mvvm.view.changeset.exchange;

import javafx.concurrent.Task;
import org.eclipse.jgit.api.Git;
import org.eclipse.jgit.api.Status;
import org.eclipse.jgit.api.errors.GitAPIException;
import org.eclipse.jgit.lib.StoredConfig;

import java.io.IOException;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static dev.ikm.komet.kview.mvvm.view.changeset.exchange.GitPropertyName.GIT_EMAIL;
import static dev.ikm.komet.kview.mvvm.view.changeset.exchange.GitPropertyName.GIT_STATUS;
import static dev.ikm.komet.kview.mvvm.view.changeset.exchange.GitPropertyName.GIT_URL;
import static dev.ikm.komet.kview.mvvm.view.changeset.exchange.GitPropertyName.GIT_USERNAME;
import static dev.ikm.komet.kview.mvvm.view.changeset.exchange.GitTask.REMOTE_NAME;

/**
 * The InfoTask class is a task that retrieves information about a Git repository.
 * It extends the Task<Map<GitPropertyName, String>> class, indicating that it returns a map of
 * Git properties upon completion.
 * <p>
 * This task fetches various repository details including the remote URL, user information,
 * and the current status of the repository.
 */
public class InfoTask extends Task<Map<GitPropertyName, String>> {

    private final Path changeSetFolder;

    public InfoTask(Path changeSetFolder) {
        this.changeSetFolder = changeSetFolder;
    }

    @Override
    public Map<GitPropertyName, String> call() throws Exception {
        return fetchRepositoryInfo();
    }

    /**
     * Fetches information about the Git repository.
     * This method retrieves the repository's remote URL, configured username and email,
     * and current status (added, uncommitted, and untracked files).
     *
     * @return A map containing repository information with GitPropertyName keys
     * @throws IOException     If there's an error opening the repository
     * @throws GitAPIException If there's an error executing Git commands
     */
    private Map<GitPropertyName, String> fetchRepositoryInfo()
            throws IOException, GitAPIException {
        Map<GitPropertyName, String> repoInfo = new HashMap<>();
        // Initialize with default error messages
        repoInfo.put(GIT_URL, "Error: could not retrieve repo name.");
        repoInfo.put(GIT_USERNAME, "Error: could not retrieve user name.");
        repoInfo.put(GIT_EMAIL, "Error: could not retrieve user email.");
        repoInfo.put(GIT_STATUS, "Error: could not retrieve status.");

        try (Git git = Git.open(changeSetFolder.toFile())) {
            final StoredConfig storedConfig = git.getRepository().getConfig();
            // Get repository URI
            final String remoteUrl = storedConfig.getString("remote", REMOTE_NAME, "url");
            if (remoteUrl != null) {
                repoInfo.put(GIT_URL, remoteUrl);
            }
            // Get username
            final String userName = storedConfig.getString("user", null, "name");
            if (userName != null) {
                repoInfo.put(GIT_USERNAME, userName);
            }
            // Get user email
            final String userEmail = storedConfig.getString("user", null, "email");
            if (userEmail != null) {
                repoInfo.put(GIT_EMAIL, userEmail);
            }

            // Get status
            Status status = git.status().call();
            List<String> statusItems = new ArrayList<>();
            statusItems.addAll(status.getAdded());
            statusItems.addAll(status.getUncommittedChanges());
            statusItems.addAll(status.getUntracked());
            String statusText = statusItems.isEmpty() ? "No changes" : String.join("\n", statusItems);
            repoInfo.put(GIT_STATUS, statusText);
        }

        return repoInfo;
    }
}