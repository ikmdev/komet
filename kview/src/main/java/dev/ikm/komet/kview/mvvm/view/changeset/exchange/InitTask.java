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
package dev.ikm.komet.kview.mvvm.view.changeset.exchange;

import dev.ikm.komet.preferences.KometPreferences;
import dev.ikm.komet.preferences.Preferences;
import dev.ikm.tinkar.common.service.TrackingCallable;
import org.eclipse.jgit.api.Git;
import org.eclipse.jgit.api.InitCommand;
import org.eclipse.jgit.api.LsRemoteCommand;
import org.eclipse.jgit.api.errors.GitAPIException;
import org.eclipse.jgit.lib.Ref;
import org.eclipse.jgit.lib.StoredConfig;
import org.eclipse.jgit.transport.CredentialItem;
import org.eclipse.jgit.transport.URIish;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.io.IOException;
import java.net.URISyntaxException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardOpenOption;
import java.time.LocalDateTime;
import java.util.Collection;

import static dev.ikm.komet.kview.mvvm.view.changeset.exchange.GitPropertyName.GIT_URL;

/**
 * A task that initializes a Git repository for changeset exchange.
 * <p>
 * This task performs the following operations:
 * <ul>
 *   <li>Loads Git configuration from user preferences</li>
 *   <li>Initializes a Git repository in the specified folder</li>
 *   <li>Configures the repository with appropriate settings</li>
 *   <li>Checks for the existence of the main branch on the remote repository</li>
 *   <li>Creates and pushes an initial commit if the main branch doesn't exist</li>
 * </ul>
 * <p>
 * The task provides progress updates during execution through the TrackingCallable interface.
 */
public class InitTask extends TrackingCallable<Boolean> {
    private static final Logger LOG = LoggerFactory.getLogger(InitTask.class);

    // Constants for progress tracking
    private static final double TOTAL_WORK = 100.0;

    // Git configuration constants
    private static final String DEFAULT_BRANCH = "main";
    private static final String REMOTE_NAME = "origin";
    private static final String README_FILENAME = "README.md";

    // Git configuration fields
    private final Path changeSetFolder;
    private final CredentialItem.StringType gitUrl;
    private final CredentialItem.StringType gitEmail;
    private final CredentialItem.Username gitUsername;
    private final CredentialItem.Password gitPassword;

    /**
     * Represents the distinct phases of the Git initialization process.
     * Each phase has a start and end percentage for progress tracking.
     */
    private enum InitPhase {
        INITIALIZATION(0.0, 10.0),
        CONFIGURATION(10.0, 40.0),
        BRANCH_CHECK(40.0, 70.0),
        COMPLETION(70.0, 100.0);

        private final double start;
        private final double end;

        InitPhase(double start, double end) {
            this.start = start;
            this.end = end;
        }

        public double getStart() {
            return start;
        }

        public double getEnd() {
            return end;
        }
    }

    /**
     * Creates a new InitTask for initializing a Git repository in the specified folder.
     *
     * @param changeSetFolder the folder where the Git repository will be initialized
     */
    public InitTask(Path changeSetFolder) {
        this.changeSetFolder = changeSetFolder;
        this.gitUrl = new CredentialItem.StringType("Git URL", false);
        this.gitEmail = new CredentialItem.StringType("Git Email", false);
        this.gitUsername = new CredentialItem.Username();
        this.gitPassword = new CredentialItem.Password();

        updateTitle("Initializing Exchange synchronization");
        updateMessage("Starting initialization process...");
        updateProgress(0, TOTAL_WORK);
    }

    /**
     * Executes the Git initialization task.
     *
     * @return true if the initialization was successful, false otherwise
     */
    @Override
    public Boolean compute() {
        try {
            if (!loadAndValidateConfiguration()) {
                return false;
            }

            Git git = initializeRepository();
            if (git == null) {
                return false;
            }

            try (git) {
                if (!configureRepository(git)) {
                    return false;
                }

                if (!checkAndSetupRemoteBranch(git)) {
                    return false;
                }

                updateMessage("Git repository successfully initialized and configured.");
                updateProgress(TOTAL_WORK, TOTAL_WORK);
                return true;
            }
        } catch (Exception ex) {
            return handleException("Unexpected error during Git initialization", ex);
        }
    }

    /**
     * Updates the current progress within a phase range.
     *
     * @param phase         the current initialization phase
     * @param phaseProgress fractional progress within phase (0.0 to 1.0)
     */
    private void updatePhaseProgress(InitPhase phase, double phaseProgress) {
        updatePhaseProgress(phase.getStart(), phase.getEnd(), phaseProgress);
    }

    /**
     * Updates the current progress within a phase range.
     *
     * @param phaseStart    start percentage of the phase
     * @param phaseEnd      end percentage of the phase
     * @param phaseProgress fractional progress within phase (0.0 to 1.0)
     */
    private void updatePhaseProgress(double phaseStart, double phaseEnd, double phaseProgress) {
        // Ensure phaseProgress is between 0 and 1
        phaseProgress = Math.max(0.0, Math.min(1.0, phaseProgress));

        // Calculate progress within this phase
        double phaseWidth = phaseEnd - phaseStart;
        double currentProgress = phaseStart + (phaseWidth * phaseProgress);

        // Update the progress tracker
        updateProgress(currentProgress, TOTAL_WORK);
    }

    /**
     * Loads Git configuration from user preferences and validates required parameters.
     *
     * @return true if configuration loaded and validated successfully, false otherwise
     */
    private boolean loadAndValidateConfiguration() {
        updatePhaseProgress(InitPhase.INITIALIZATION, 0.2);
        updateMessage("Loading Git configuration from preferences...");

        KometPreferences userPreferences = Preferences.get().getUserPreferences();
        userPreferences.get(GIT_URL).ifPresent(gitUrl::setValue);
        userPreferences.get(GitPropertyName.GIT_EMAIL).ifPresent(gitEmail::setValue);
        userPreferences.get(GitPropertyName.GIT_USERNAME).ifPresent(gitUsername::setValue);
        userPreferences.get(GitPropertyName.GIT_PASSWORD)
                .ifPresent(value -> gitPassword.setValue(value.toCharArray()));

        updatePhaseProgress(InitPhase.INITIALIZATION, 0.6);
        updateMessage("Validating Git configuration...");

        // Validate required parameters
        if (gitUrl.getValue() == null || gitUrl.getValue().trim().isEmpty()) {
            updateMessage("Error: Git URL is not configured in preferences.");
            LOG.error("Git URL is not configured in preferences");
            updatePhaseProgress(InitPhase.INITIALIZATION, 1.0);
            return false;
        }

        updatePhaseProgress(InitPhase.INITIALIZATION, 1.0);
        return true;
    }

    /**
     * Initializes a Git repository in the changeset folder.
     *
     * @return the Git instance if initialization was successful, null otherwise
     */
    private Git initializeRepository() {
        updateMessage("Initializing Git repository...");
        updatePhaseProgress(InitPhase.CONFIGURATION, 0.2);

        try {
            InitCommand initCommand = Git.init();
            initCommand.setDirectory(changeSetFolder.toFile());
            initCommand.setInitialBranch(DEFAULT_BRANCH);
            return initCommand.call();
        } catch (GitAPIException ex) {
            handleException("Error initializing Git repository", ex);
            return null;
        }
    }

    /**
     * Configures the Git repository with remote URL, user settings, and repository settings.
     *
     * @param git the Git instance
     * @return true if configuration was successful, false otherwise
     */
    private boolean configureRepository(Git git) {
        if (!git.getRepository().getRemoteNames().isEmpty()) {
            updateMessage("Git repository already configured. No changes required.");
            updatePhaseProgress(InitPhase.CONFIGURATION, 1.0);
            return true;
        }

        updateMessage("Configuring Git repository...");
        updatePhaseProgress(InitPhase.CONFIGURATION, 0.6);

        try {
            // Configure remote repository
            configureRemote(git);
            updatePhaseProgress(InitPhase.CONFIGURATION, 0.7);

            // Configure user and repository settings
            configureUserSettings(git);
            configureRepositorySettings(git);

            updatePhaseProgress(InitPhase.CONFIGURATION, 1.0);
            return true;
        } catch (URISyntaxException | IOException | GitAPIException ex) {
            return handleException("Error configuring Git repository", ex);
        }
    }

    /**
     * Configures the remote repository in the Git configuration.
     *
     * @param git the Git instance
     * @throws URISyntaxException if the Git URL is invalid
     * @throws GitAPIException    if an error occurs when adding the remote
     */
    private void configureRemote(Git git) throws URISyntaxException, GitAPIException {
        URIish uri = new URIish(gitUrl.getValue().trim());
        git.remoteAdd().setName(REMOTE_NAME).setUri(uri).call();
    }

    /**
     * Configures user settings in the Git configuration.
     *
     * @param git the Git instance
     * @throws IOException if an error occurs when saving the configuration
     */
    private void configureUserSettings(Git git) throws IOException {
        updateMessage("Setting up Git user configuration...");
        final StoredConfig config = git.getRepository().getConfig();
        config.setString("user", null, "name", gitUsername.getValue().trim());
        config.setString("user", null, "email", gitEmail.getValue().trim());
    }

    /**
     * Configures repository settings in the Git configuration.
     *
     * @param git the Git instance
     * @throws IOException if an error occurs when saving the configuration
     */
    private void configureRepositorySettings(Git git) throws IOException {
        updateMessage("Setting up Git repository configuration...");
        final StoredConfig config = git.getRepository().getConfig();

        // Core settings
        config.setInt("core", null, "repositoryformatversion", 0);
        config.setBoolean("core", null, "filemode", false);
        config.setBoolean("core", null, "bare", false);
        config.setBoolean("core", null, "logallrefupdates", true);
        config.setBoolean("core", null, "symlinks", false);
        config.setBoolean("core", null, "ignorecase", true);

        // Other settings
        config.setString("submodule", null, "active", ".");
        config.setBoolean("commit", null, "gpgsign", false);
        // GPG Format Workaround: https://bugs.eclipse.org/bugs/show_bug.cgi?id=581483
        config.setString("gpg", null, "format", "x509");

        config.save();
    }

    /**
     * Checks for the existence of the main branch on the remote repository
     * and creates it if it doesn't exist.
     *
     * @param git the Git instance
     * @return true if the check was successful, false otherwise
     */
    private boolean checkAndSetupRemoteBranch(Git git) {
        updateMessage("Checking for main branch on remote repository...");
        updatePhaseProgress(InitPhase.BRANCH_CHECK, 0.5);

        try {
            updateMessage("Connecting to remote repository...");

            LsRemoteCommand lsRemoteCommand = git.lsRemote();
            lsRemoteCommand.setHeads(true);
            lsRemoteCommand.setRemote(REMOTE_NAME);
            lsRemoteCommand.setCredentialsProvider(new GitHubCredentialsProvider());

            updateMessage("Fetching branch information from remote...");
            Collection<Ref> refs = lsRemoteCommand.call();

            // Check if main branch exists
            final boolean mainBranchExists = refs.stream()
                    .anyMatch(ref -> ref.getName().equals("refs/heads/" + DEFAULT_BRANCH));

            if (!mainBranchExists) {
                updateMessage("Main branch does not exist on remote. Creating new branch...");
                updatePhaseProgress(InitPhase.BRANCH_CHECK, 0.7);

                try {
                    createAndPushMainBranch(git);
                } catch (GitAPIException | IOException ex) {
                    return handleException("Error creating main branch", ex);
                }
            } else {
                updateMessage("Main branch already exists on remote repository.");
                updatePhaseProgress(InitPhase.COMPLETION, 1.0);
            }

            return true;
        } catch (GitAPIException ex) {
            return handleException("Error checking remote branches", ex);
        }
    }

    /**
     * Creates a main branch with an initial commit and pushes it to remote.
     *
     * @param git the Git instance
     * @throws GitAPIException if a Git API error occurs
     * @throws IOException     if an I/O error occurs
     */
    private void createAndPushMainBranch(Git git) throws GitAPIException, IOException {
        // Create a README.md file for the initial commit
        final File readmeFile = new File(changeSetFolder.toFile(), README_FILENAME);
        if (!readmeFile.exists()) {
            updateMessage("Creating initial README.md file...");
            updatePhaseProgress(InitPhase.COMPLETION, 0.3);

            Files.write(
                    readmeFile.toPath(),
                    generateReadmeContent().getBytes(),
                    StandardOpenOption.CREATE
            );

            // Add and commit the file
            updateMessage("Adding and committing initial files...");
            updatePhaseProgress(InitPhase.COMPLETION, 0.5);

            git.add().addFilepattern(README_FILENAME).call();
            git.commit()
                    .setMessage("Initial commit for changeset exchange")
                    .setAuthor(gitUsername.getValue(), gitEmail.getValue())
                    .call();

            // Push the commit to remote
            updateMessage("Pushing initial commit to remote repository...");
            updatePhaseProgress(InitPhase.COMPLETION, 0.7);

            // Use a progress monitor for the push operation
            final GitProgressMonitor progressMonitor = new GitProgressMonitor(
                    this,
                    InitPhase.BRANCH_CHECK.getEnd(),
                    InitPhase.COMPLETION.getEnd(),
                    TOTAL_WORK);

            git.push()
                    .setRemote(REMOTE_NAME)
                    .setCredentialsProvider(new GitHubCredentialsProvider())
                    .setProgressMonitor(progressMonitor)
                    .setPushAll()
                    .call();

            updateMessage("Successfully created and pushed 'main' branch to remote repository.");
            updatePhaseProgress(InitPhase.COMPLETION, 1.0);
        }
    }

    /**
     * Generates the content for the README.md file.
     *
     * @return the content for the README.md file
     */
    private String generateReadmeContent() {
        return "# Changeset Exchange Repository\n\n" +
                "This repository is used for exchanging changesets with the Komet application.\n\n" +
                "Created: " + LocalDateTime.now() + "\n";
    }

    /**
     * Handles exceptions by updating the message, logging the error, and updating the progress.
     *
     * @param message the error message
     * @param ex      the exception
     * @return false to indicate failure
     */
    private boolean handleException(String message, Exception ex) {
        updateMessage(message + ": " + ex.getLocalizedMessage());
        LOG.error(message, ex);
        return false;
    }
}