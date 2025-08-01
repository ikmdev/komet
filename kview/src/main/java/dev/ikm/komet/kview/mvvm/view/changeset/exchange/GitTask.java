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

import dev.ikm.tinkar.events.EvtBusFactory;
import dev.ikm.komet.framework.events.appevents.RefreshCalculatorCacheEvent;
import dev.ikm.komet.kview.mvvm.model.GitHubPreferences;
import dev.ikm.komet.kview.mvvm.model.GitHubPreferencesDao;
import dev.ikm.komet.kview.mvvm.view.changeset.exchange.credentials.GitHubCredentialsProvider;
import dev.ikm.komet.kview.mvvm.view.changeset.exchange.credentials.GitSslValidationCredentialsProvider;
import dev.ikm.tinkar.common.service.PluggableService;
import dev.ikm.tinkar.common.service.SaveState;
import dev.ikm.tinkar.common.service.TrackingCallable;
import dev.ikm.tinkar.coordinate.Calculators;
import dev.ikm.tinkar.entity.ChangeSetWriterService;
import dev.ikm.tinkar.entity.EntityCountSummary;
import dev.ikm.tinkar.entity.load.LoadEntitiesFromProtobufFile;
import dev.ikm.tinkar.reasoner.service.ClassifierResults;
import dev.ikm.tinkar.reasoner.service.ReasonerService;
import dev.ikm.tinkar.terms.TinkarTerm;
import org.eclipse.collections.api.factory.Lists;
import org.eclipse.collections.api.list.ImmutableList;
import org.eclipse.collections.api.list.MutableList;
import org.eclipse.jgit.api.AddCommand;
import org.eclipse.jgit.api.CommitCommand;
import org.eclipse.jgit.api.Git;
import org.eclipse.jgit.api.InitCommand;
import org.eclipse.jgit.api.LsRemoteCommand;
import org.eclipse.jgit.api.PullCommand;
import org.eclipse.jgit.api.PullResult;
import org.eclipse.jgit.api.PushCommand;
import org.eclipse.jgit.api.errors.GitAPIException;
import org.eclipse.jgit.diff.DiffEntry;
import org.eclipse.jgit.lib.ObjectId;
import org.eclipse.jgit.lib.ObjectReader;
import org.eclipse.jgit.lib.Ref;
import org.eclipse.jgit.lib.Repository;
import org.eclipse.jgit.lib.StoredConfig;
import org.eclipse.jgit.revwalk.RevCommit;
import org.eclipse.jgit.revwalk.RevWalk;
import org.eclipse.jgit.transport.ChainingCredentialsProvider;
import org.eclipse.jgit.transport.CredentialItem;
import org.eclipse.jgit.transport.URIish;
import org.eclipse.jgit.treewalk.CanonicalTreeParser;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.io.IOException;
import java.net.URISyntaxException;
import java.nio.file.FileSystem;
import java.nio.file.FileSystems;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardOpenOption;
import java.time.LocalDateTime;
import java.util.Collection;
import java.util.Comparator;
import java.util.Date;
import java.util.List;
import java.util.Optional;
import java.util.ServiceLoader;
import java.util.Set;
import java.util.concurrent.ExecutionException;
import java.util.stream.Stream;

import static dev.ikm.tinkar.events.FrameworkTopics.CALCULATOR_CACHE_TOPIC;
import static dev.ikm.komet.framework.events.appevents.RefreshCalculatorCacheEvent.GLOBAL_REFRESH;

/**
 * A unified task for Git operations involving medical terminology data.
 * <p>
 * This class handles three main operations:
 * <ul>
 *   <li>CONNECT: Initialize a Git repository and connect to a remote repository</li>
 *   <li>PULL: Pull changes from remote repository and load them into the system</li>
 *   <li>SYNC: Pull changes and then push local changes back to the remote repository</li>
 * </ul>
 * <p>
 * Each operation follows a series of phases, with detailed progress tracking throughout.
 */
public class GitTask extends TrackingCallable<Boolean> {

    private static final Logger LOG = LoggerFactory.getLogger(GitTask.class);

    /**
     * The operation mode for this Git task.
     */
    public enum OperationMode {
        /**
         * Initialize and connect to a remote Git repository.
         */
        CONNECT,

        /**
         * Pull changes from the remote repository.
         */
        PULL,

        /**
         * Pull changes from the remote repository and push local changes.
         */
        SYNC;

        @Override
        public String toString() {
            String name = name();
            return name.substring(0, 1).toUpperCase() + name.substring(1).toLowerCase();
        }
    }

    /**
     * The total amount of work units for progress tracking.
     */
    private static final double TOTAL_WORK = 100.0;

    /**
     * Git configuration constants
     */
    public static final String DEFAULT_BRANCH = "main";
    public static final String REMOTE_NAME = "origin";
    public static final String README_FILENAME = "README.md";

    /**
     * Defines the progress phases for each operation mode with their progress boundaries.
     * <p>
     * Each phase has a start and end percentage that defines its portion of the total
     * work. These percentages are used for progress tracking and reporting.
     */
    private enum TaskPhase {
        // CONNECT phases
        CONNECT_INITIALIZATION(0.0, 10.0),
        CONNECT_CONFIGURATION(10.0, 40.0),
        CONNECT_BRANCH_CHECK(40.0, 70.0),
        CONNECT_COMPLETION(70.0, 100.0),

        // PULL phases
        PULL_VALIDATION(0.0, 5.0),
        PULL_FETCH(5.0, 25.0),
        PULL_LOAD(25.0, 50.0),
        PULL_REASONER(50.0, 100.0),

        // SYNC phases (includes PULL phases plus PUSH)
        SYNC_VALIDATION(0.0, 5.0),
        SYNC_FETCH(5.0, 20.0),
        SYNC_LOAD(20.0, 40.0),
        SYNC_REASONER(40.0, 70.0),
        SYNC_PUSH(70.0, 100.0);

        private final double start;
        private final double end;

        /**
         * Creates a new task phase with the specified progress boundaries.
         *
         * @param start The start percentage of this phase (0.0 to 100.0)
         * @param end   The end percentage of this phase (0.0 to 100.0)
         */
        TaskPhase(double start, double end) {
            this.start = start;
            this.end = end;
        }

        /**
         * Gets the start percentage of this phase.
         *
         * @return The start percentage (0.0 to 100.0)
         */
        public double getStart() {
            return start;
        }

        /**
         * Gets the end percentage of this phase.
         *
         * @return The end percentage (0.0 to 100.0)
         */
        public double getEnd() {
            return end;
        }
    }

    // Common fields
    private final OperationMode operationMode;
    private final Path changeSetFolder;
    private final Runnable connectionSuccessCallback;
    private final GitHubPreferencesDao gitHubPreferencesDao;

    // CONNECT mode fields
    private final CredentialItem.StringType gitUrl;
    private final CredentialItem.StringType gitEmail;
    private final CredentialItem.Username gitUsername;
    private final CredentialItem.Password gitPassword;

    private final ChainingCredentialsProvider chainingCredentialsProvider;

    /**
     * Creates a new GitTask with the specified operation mode and changeset folder.
     *
     * @param operationMode             The mode of operation (CONNECT, PULL, or SYNC)
     * @param changeSetFolder           The folder containing changesets to work with
     * @param connectionSuccessCallback Callback to be executed when a successful connection is established
     */
    public GitTask(OperationMode operationMode,
                   Path changeSetFolder,
                   Runnable connectionSuccessCallback) {
        this.operationMode = operationMode;
        this.changeSetFolder = changeSetFolder;
        this.connectionSuccessCallback = connectionSuccessCallback;

        this.gitHubPreferencesDao = new GitHubPreferencesDao();

        // Initialize credential items for all modes to avoid null pointers
        this.gitUrl = new CredentialItem.StringType("Git URL", false);
        this.gitEmail = new CredentialItem.StringType("Git Email", false);
        this.gitUsername = new CredentialItem.Username();
        this.gitPassword = new CredentialItem.Password();

        // Set task title based on operation mode
        switch (operationMode) {
            case CONNECT -> updateTitle("Exchange Connection in progress...");
            case PULL -> updateTitle("Exchange Pull in progress...");
            case SYNC -> updateTitle("Exchange Sync in progress...");
        }

        this.chainingCredentialsProvider = new ChainingCredentialsProvider(
                new GitHubCredentialsProvider(),
                new GitSslValidationCredentialsProvider()
        );

        updateProgress(0, TOTAL_WORK);
    }

    /**
     * Executes the appropriate operation based on the selected mode.
     * For PULL and SYNC modes, first checks if the repository is fully
     * initialized and connected before proceeding.
     *
     * @return true if the operation was successful, false otherwise
     */
    @Override
    public Boolean compute() {
        try {
            boolean result = switch (operationMode) {
                case CONNECT -> executeConnect();
                case PULL, SYNC -> {
                    if (executeConnect()) {
                        // Now proceed with pull/sync
                        yield executeSync(operationMode == OperationMode.SYNC);
                    } else {
                        // Connection failed
                        yield false;
                    }
                }
            };

            // Ensure we reach 100% at the end if successful
            if (result) {
                updateProgress(TOTAL_WORK, TOTAL_WORK);
            }
            return result;
        } catch (Exception ex) {
            handleException("Unexpected error during " + operationMode, ex);
            return false;
        }
    }

    /**
     * Checks if the Git repository is fully initialized and connected.
     * This includes checking if the repository exists, has a remote configured,
     * and has the main branch set up.
     *
     * @return true if the repository is fully initialized, false otherwise
     */
    private boolean isRepositoryFullyInitialized() {
        try (Git git = Git.open(changeSetFolder.toFile())) {
            // Check if remote is configured
            if (git.getRepository().getRemoteNames().isEmpty()) {
                LOG.info("Git repository has no remote repositories configured");
                return false;
            }

            // Check if main branch exists and is tracking remote
            try {
                LsRemoteCommand lsRemoteCommand = git.lsRemote();
                lsRemoteCommand.setHeads(true);
                lsRemoteCommand.setRemote(REMOTE_NAME);
                lsRemoteCommand.setCredentialsProvider(chainingCredentialsProvider);

                Collection<Ref> refs = lsRemoteCommand.call();
                boolean mainBranchExists = refs.stream()
                        .anyMatch(ref -> ref.getName().equals("refs/heads/" + DEFAULT_BRANCH));

                if (!mainBranchExists) {
                    LOG.info("Main branch does not exist on remote repository");
                    return false;
                }
            } catch (GitAPIException ex) {
                LOG.error("Error checking remote branches", ex);
                return false;
            }

            return true;
        } catch (IOException ex) {
            return false;
        }
    }

    // -------------------- CONNECT Operations --------------------

    /**
     * Executes the CONNECT operation and initializes the Git repository if needed.
     *
     * @return true if the connection was successful, false otherwise
     * @throws IOException if an I/O error occurs
     */
    private boolean executeConnect() throws IOException {
        if (!loadAndValidateConfiguration()) {
            return false;
        }

        if (!isRepositoryFullyInitialized()) {
            updateMessage("Repository is not fully initialized. Preparing for initialization...");
            try (Git git = initializeRepository()) {
                if (git == null) {
                    updateMessage("Failed to initialize the Git repository");
                    return false;
                }

                // Configure the repository
                if (!configureRepository(git)) {
                    updateMessage("Failed to configure the Git repository.");
                    return false;
                }

                // Check and set up the remote branch
                if (!checkAndSetupRemoteBranch(git)) {
                    updateMessage("Failed to set up the remote branch in the Git repository.");
                    return false;
                }

                updateMessage("Repository initialized successfully.");
            }
        }

        try (Git git = Git.open(changeSetFolder.toFile())) {
            updateMessage("Connection to remote repository successfully established.");

            // Call the connection success callback if provided
            if (connectionSuccessCallback != null) {
                connectionSuccessCallback.run();
            }

            return true;
        }
    }

    /**
     * Loads Git configuration from user preferences and validates required parameters.
     *
     * @return true if configuration loaded and validated successfully, false otherwise
     */
    private boolean loadAndValidateConfiguration() {
        updatePhaseProgress(TaskPhase.CONNECT_INITIALIZATION, 0.2);
        updateMessage("Retrieving connection credentials from preferences...");

        Optional<GitHubPreferences> gitHubPreferencesOpt = gitHubPreferencesDao.load();
        if (gitHubPreferencesOpt.isPresent()) {
            GitHubPreferences gitHubPreferences = gitHubPreferencesOpt.get();
            gitUrl.setValue(gitHubPreferences.gitUrl());
            gitEmail.setValue(gitHubPreferences.gitEmail());
            gitUsername.setValue(gitHubPreferences.gitUsername());
            gitPassword.setValue(gitHubPreferences.gitPassword());
        } else {
            updateMessage("Git preferences not found.");
            LOG.error("Git preferences not found");
            return false;
        }

        updatePhaseProgress(TaskPhase.CONNECT_INITIALIZATION, 0.6);
        updateMessage("Validating remote connection parameters...");

        // Validate required parameters
        if (gitUrl.getValue() == null || gitUrl.getValue().trim().isEmpty() ||
                gitEmail.getValue() == null || gitEmail.getValue().trim().isEmpty() ||
                gitUsername.getValue() == null || gitUsername.getValue().trim().isEmpty() ||
                gitPassword.getValue() == null || gitPassword.getValue().length == 0) {

            updateMessage("Git preferences are not configured.");
            LOG.error("Git preferences are not configured");
            return false;
        }

        updatePhaseProgress(TaskPhase.CONNECT_INITIALIZATION, 1.0);
        return true;
    }

    /**
     * Initializes a Git repository in the changeset folder.
     *
     * @return the Git instance if initialization was successful, null otherwise
     */
    private Git initializeRepository() {
        updateMessage("Preparing local repository for connection...");
        updatePhaseProgress(TaskPhase.CONNECT_CONFIGURATION, 0.2);

        try {
            InitCommand initCommand = Git.init();
            initCommand.setDirectory(changeSetFolder.toFile());
            initCommand.setInitialBranch(DEFAULT_BRANCH);
            return initCommand.call();
        } catch (GitAPIException ex) {
            handleException("Error initializing repository for connection", ex);
            return null;
        }
    }

    /**
     * Configures the Git repository with remote URL, user settings, and repository settings.
     * Always checks and updates configuration, even for existing repositories.
     *
     * @param git the Git instance
     * @return true if configuration was successful, false otherwise
     */
    private boolean configureRepository(Git git) {
        if (!git.getRepository().getRemoteNames().isEmpty()) {
            updateMessage("Remote connection already established. No changes required.");
            updatePhaseProgress(TaskPhase.CONNECT_CONFIGURATION, 1.0);
            return true;
        }

        updateMessage("Setting up connection to remote repository...");
        updatePhaseProgress(TaskPhase.CONNECT_CONFIGURATION, 0.6);

        try {
            // Configure remote repository
            configureRemote(git);
            updatePhaseProgress(TaskPhase.CONNECT_CONFIGURATION, 0.7);

            // Configure user and repository settings
            configureUserSettings(git);
            configureRepositorySettings(git);

            updatePhaseProgress(TaskPhase.CONNECT_CONFIGURATION, 1.0);
            return true;
        } catch (URISyntaxException | IOException | GitAPIException ex) {
            return handleException("Error configuring repository connection", ex);
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
        updateMessage("Configuring authentication credentials for access...");
        final StoredConfig config = git.getRepository().getConfig();
        config.setString("user", null, "name", gitUsername.getValue().trim());
        config.setString("user", null, "email", gitEmail.getValue().trim());
        config.save();
    }

    /**
     * Configures repository settings in the Git configuration.
     *
     * @param git the Git instance
     * @throws IOException if an error occurs when saving the configuration
     */
    private void configureRepositorySettings(Git git) throws IOException {
        updateMessage("Optimizing connection settings for reliability...");
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
        updateMessage("Verifying connection to remote repository...");
        updatePhaseProgress(TaskPhase.CONNECT_BRANCH_CHECK, 0.5);

        try {
            updateMessage("Testing connection with remote repository...");

            LsRemoteCommand lsRemoteCommand = git.lsRemote();
            lsRemoteCommand.setHeads(true);
            lsRemoteCommand.setRemote(REMOTE_NAME);
            lsRemoteCommand.setCredentialsProvider(chainingCredentialsProvider);

            final Collection<Ref> refs = lsRemoteCommand.call();

            // Check if main branch exists
            final boolean mainBranchExists = refs.stream()
                    .anyMatch(ref -> ref.getName().equals("refs/heads/" + DEFAULT_BRANCH));

            if (!mainBranchExists) {
                updateMessage("Remote branch not found. Establishing new connection point...");
                updatePhaseProgress(TaskPhase.CONNECT_BRANCH_CHECK, 0.7);

                try {
                    createAndPushMainBranch(git);
                } catch (GitAPIException | IOException ex) {
                    return handleException("Failed to establish initial connection to remote repository", ex);
                }
            } else {
                updateMessage("Connection to remote branch already established.");
                updatePhaseProgress(TaskPhase.CONNECT_COMPLETION, 1.0);
            }
        } catch (GitAPIException ex) {
            try {
                // Create and push the main branch to an empty remote repository
                createAndPushMainBranch(git);
            } catch (GitAPIException | IOException createEx) {
                return handleException("Failed to establish initial connection to remote repository", createEx);
            }
        }

        return true;
    }

    /**
     * Creates a main branch with an initial commit and pushes it to remote.
     *
     * @param git the Git instance
     * @throws GitAPIException if a Git API error occurs
     * @throws IOException     if an I/O error occurs
     */
    private void createAndPushMainBranch(Git git) throws GitAPIException, IOException {
        updateMessage("Initializing connection with remote repository...");
        updatePhaseProgress(TaskPhase.CONNECT_COMPLETION, 0.3);

        // Always create or update the README file for the initial commit
        final File readmeFile = new File(changeSetFolder.toFile(), README_FILENAME);
        Files.write(
                readmeFile.toPath(),
                generateReadmeContent().getBytes(),
                StandardOpenOption.CREATE,
                StandardOpenOption.TRUNCATE_EXISTING  // This will overwrite if it exists
        );

        // Add and commit the file
        updateMessage("Preparing initial data for secure transmission...");
        updatePhaseProgress(TaskPhase.CONNECT_COMPLETION, 0.5);

        git.add().addFilepattern(README_FILENAME).call();
        git.commit()
                .setMessage("Initial commit for changeset exchange")
                .setAuthor(gitUsername.getValue(), gitEmail.getValue())
                .call();

        // Push the commit to remote
        updateMessage("Establishing first connection with remote repository...");
        updatePhaseProgress(TaskPhase.CONNECT_COMPLETION, 0.7);

        // Use a progress monitor for the push operation
        final GitProgressMonitor progressMonitor = new GitProgressMonitor(
                this,
                TaskPhase.CONNECT_BRANCH_CHECK.getEnd(),
                TaskPhase.CONNECT_COMPLETION.getEnd(),
                TOTAL_WORK);

        git.push()
                .setRemote(REMOTE_NAME)
                .setCredentialsProvider(chainingCredentialsProvider)
                .setProgressMonitor(progressMonitor)
                .setPushAll()
                .call();

        updateMessage("Connection established successfully! Remote repository is now linked.");
        updatePhaseProgress(TaskPhase.CONNECT_COMPLETION, 1.0);
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

    // -------------------- PULL/SYNC Operations --------------------

    /**
     * Executes the PULL operation, optionally followed by a PUSH if in SYNC mode.
     *
     * @param pushChanges whether to push changes after pulling (true for SYNC mode)
     * @return true if the operation was successful, false otherwise
     * @throws GitAPIException if a Git API error occurs
     * @throws IOException     if an I/O error occurs
     */
    private boolean executeSync(boolean pushChanges) throws GitAPIException, IOException {
        TaskPhase validationPhase = pushChanges ? TaskPhase.SYNC_VALIDATION : TaskPhase.PULL_VALIDATION;
        TaskPhase pullPhase = pushChanges ? TaskPhase.SYNC_FETCH : TaskPhase.PULL_FETCH;
        TaskPhase loadPhase = pushChanges ? TaskPhase.SYNC_LOAD : TaskPhase.PULL_LOAD;
        TaskPhase reasonerPhase = pushChanges ? TaskPhase.SYNC_REASONER : TaskPhase.PULL_REASONER;
        TaskPhase pushPhase = TaskPhase.SYNC_PUSH; // Only used in SYNC mode

        updateMessage("Validating environment...");
        updateProgress(0, TOTAL_WORK);

        // Validation complete
        updatePhaseProgress(validationPhase.getStart(), validationPhase.getEnd(), 1.0);

        updateMessage("Starting %s process.".formatted(pushChanges ? "synchronization" : "pulling"));

        // Pull phase - now returns a list of added files
        ImmutableList<String> addedFiles = pull(validationPhase.getEnd(), pullPhase.getEnd());

        // Load changesets phase - pass the list of added files
        loadChangesets(pullPhase.getEnd(), loadPhase.getEnd(), addedFiles);
        //Refresh UI after changesets are loaded
        EvtBusFactory.getDefaultEvtBus().publish(CALCULATOR_CACHE_TOPIC, new RefreshCalculatorCacheEvent(this, GLOBAL_REFRESH));

        // Run reasoner phase
        runReasoner(loadPhase.getEnd(), reasonerPhase.getEnd());

        // Push phase (if enabled)
        if (pushChanges) {
            push(reasonerPhase.getEnd(), pushPhase.getEnd());
        }

        updateMessage("%s completed successfully.".formatted(pushChanges ? "Synchronization" : "Pulling"));
        return true;
    }

    /**
     * Pulls the latest changes from the remote Git repository and returns a list of added files.
     *
     * @param startPercentage the progress percentage at the start of this phase
     * @param endPercentage   the progress percentage at the end of this phase
     * @return a list of relative paths to files that were added during the pull operation
     * @throws GitAPIException if a Git API error occurs
     * @throws IOException     if an I/O error occurs
     */
    private ImmutableList<String> pull(double startPercentage, double endPercentage) throws GitAPIException, IOException {
        if (isCancelled()) {
            updateMessage("Operation cancelled by user.");
            return Lists.immutable.empty();
        }
        // Starting the pull phase
        updatePhaseProgress(startPercentage, endPercentage, 0.0);

        try (Git git = Git.open(changeSetFolder.toFile())) {
            updateMessage("Connecting to remote repository...");
            updatePhaseProgress(startPercentage, endPercentage, 0.2);

            // Store the current HEAD commit before pull
            ObjectId oldHead = git.getRepository().resolve("HEAD");

            // Create a progress monitor for Git operations
            GitProgressMonitor progressMonitor = new GitProgressMonitor(
                    this,
                    startPercentage,
                    endPercentage,
                    TOTAL_WORK);

            PullCommand pullCommand = git.pull();
            pullCommand.setProgressMonitor(progressMonitor);
            pullCommand.setRemoteBranchName(DEFAULT_BRANCH);
            pullCommand.setCredentialsProvider(chainingCredentialsProvider);

            updateMessage("Pulling changes from remote...");
            updatePhaseProgress(startPercentage, endPercentage, 0.3);

            PullResult pullResult = pullCommand.call();

            if (pullResult.isSuccessful()) {
                // Get the new HEAD after pull
                ObjectId newHead = git.getRepository().resolve("HEAD");

                // Default to empty list
                ImmutableList<String> addedFiles = Lists.immutable.empty();

                // Get list of changed files only if there were actually changes
                if (!newHead.equals(oldHead)) {
                    List<DiffEntry> changedFiles = getChangedFiles(git.getRepository(), oldHead, newHead);

                    // Filter for only ADD changes and files ending with ike-cs.zip
                    MutableList<String> newFiles = Lists.mutable.empty();
                    for (DiffEntry diff : changedFiles) {
                        if (diff.getChangeType() == DiffEntry.ChangeType.ADD &&
                                diff.getNewPath().endsWith("ike-cs.zip")) {

                            Path filePath = changeSetFolder.resolve(diff.getNewPath());
                            if (isValidChangeset(filePath)) {
                                // Add this to our list of files to load
                                newFiles.add(diff.getNewPath());
                            }
                        }
                    }

                    // Sort the files to ensure consistent loading order
                    newFiles.sort(String::compareTo);
                    addedFiles = newFiles.toImmutable();

                    updateMessage("Pull completed. " + addedFiles.size() + " new changeset files found.");

                    if (LOG.isInfoEnabled() && !addedFiles.isEmpty()) {
                        LOG.info("New changeset files to load:");
                        for (String file : addedFiles) {
                            LOG.info(file);
                        }
                    }
                } else {
                    updateMessage("Pull completed. No changes found.");
                }

                updatePhaseProgress(startPercentage, endPercentage, 1.0);
                return addedFiles;
            } else {
                updateMessage("Pull failed: " + pullResult.getMergeResult().getMergeStatus());
                return Lists.immutable.empty();
            }
        }
    }

    /**
     * Gets the list of files that changed between two commits.
     *
     * @param repository the Git repository
     * @param oldHead    the object ID of the old commit
     * @param newHead    the object ID of the new commit
     * @return a list of file differences between the two commits
     * @throws IOException     if an I/O error occurs
     * @throws GitAPIException if a Git API error occurs
     */
    private List<DiffEntry> getChangedFiles(Repository repository,
                                            ObjectId oldHead,
                                            ObjectId newHead) throws IOException, GitAPIException {
        // Handle case where oldHead might be null (new repository)
        if (oldHead == null) {
            LOG.info("No previous HEAD found, considering all files as new");
            try (RevWalk walk = new RevWalk(repository)) {
                RevCommit commit = walk.parseCommit(newHead);
                try (ObjectReader reader = repository.newObjectReader()) {
                    CanonicalTreeParser newTreeIter = new CanonicalTreeParser();
                    newTreeIter.reset(reader, commit.getTree());

                    // Empty tree for comparison
                    CanonicalTreeParser oldTreeIter = new CanonicalTreeParser();

                    return new Git(repository).diff()
                            .setOldTree(oldTreeIter)
                            .setNewTree(newTreeIter)
                            .call();
                }
            }
        }

        // Normal case: compare old and new HEAD
        try (RevWalk walk = new RevWalk(repository)) {
            RevCommit oldCommit = walk.parseCommit(oldHead);
            RevCommit newCommit = walk.parseCommit(newHead);

            // For merge commits, we need additional handling
            if (newCommit.getParentCount() > 1 && newCommit.getParent(0).equals(oldCommit)) {
                // This is a merge commit where our old HEAD is the first parent
                LOG.info("Detected merge commit, comparing with second parent");

                // Get the second parent (the one we merged from)
                RevCommit mergedCommit = walk.parseCommit(newCommit.getParent(1));

                try (ObjectReader reader = repository.newObjectReader()) {
                    CanonicalTreeParser oldTreeIter = new CanonicalTreeParser();
                    oldTreeIter.reset(reader, oldCommit.getTree());

                    CanonicalTreeParser newTreeIter = new CanonicalTreeParser();
                    newTreeIter.reset(reader, mergedCommit.getTree());

                    return new Git(repository).diff()
                            .setOldTree(oldTreeIter)
                            .setNewTree(newTreeIter)
                            .call();
                }
            }

            // Standard comparison between commits
            try (ObjectReader reader = repository.newObjectReader()) {
                CanonicalTreeParser oldTreeIter = new CanonicalTreeParser();
                oldTreeIter.reset(reader, oldCommit.getTree());

                CanonicalTreeParser newTreeIter = new CanonicalTreeParser();
                newTreeIter.reset(reader, newCommit.getTree());

                return new Git(repository).diff()
                        .setOldTree(oldTreeIter)
                        .setNewTree(newTreeIter)
                        .call();
            }
        }
    }

    /**
     * Loads changesets from the specified list of files.
     *
     * @param startPercentage   the progress percentage at the start of this phase
     * @param endPercentage     the progress percentage at the end of this phase
     * @param relativeFilePaths list of relative paths to changeset files to load
     */
    private void loadChangesets(double startPercentage, double endPercentage,
                                ImmutableList<String> relativeFilePaths) {
        if (isCancelled()) {
            updateMessage("Operation cancelled by user.");
            return;
        }

        updatePhaseProgress(startPercentage, endPercentage, 0.0);
        MutableList<EntityCountSummary> loadResults = Lists.mutable.empty();

        if (relativeFilePaths.isEmpty()) {
            updateMessage("No changeset files found to load.");
            updatePhaseProgress(startPercentage, endPercentage, 1.0);
            return;
        }

        int total = relativeFilePaths.size();
        int current = 0;

        for (String relativePath : relativeFilePaths) {
            current++;
            File file = changeSetFolder.resolve(relativePath).toFile();
            updateMessage("Loading changeset " + current + " of " + total + ": " + file.getName());
            // Update progress based on current file's position in the total
            double loadProgress = (double) current / total;
            updatePhaseProgress(startPercentage, endPercentage, loadProgress * 0.9); // Reserve 10% for completion

            try {
                // isValidChangeset check already done when filtering files
                EntityCountSummary ecs = new LoadEntitiesFromProtobufFile(file).compute();
                loadResults.add(ecs);
                LOG.info("Loaded changeset: {}", file.getName());
            } catch (Exception ex) {
                LOG.error("Failed to load changeset: {}", file.getName(), ex);
            }
        }

        updateMessage("Successfully loaded " + loadResults.size() + " of " + total + " changesets.");
        updatePhaseProgress(startPercentage, endPercentage, 1.0);
    }

    /**
     * Runs the reasoning process on the loaded data.
     *
     * @param startPercentage the progress percentage at the start of this phase
     * @param endPercentage   the progress percentage at the end of this phase
     */
    private void runReasoner(double startPercentage, double endPercentage) {
        if (isCancelled()) {
            updateMessage("Operation cancelled by user.");
            return;
        }

        updateMessage("Starting reasoning process...");
        updatePhaseProgress(startPercentage, endPercentage, 0.0);

        ImmutableList<ReasonerService> reasoners = loadReasonerServices();
        processWithReasoners(reasoners, startPercentage, endPercentage);

        updateMessage("Reasoning process completed successfully.");
        updatePhaseProgress(startPercentage, endPercentage, 1.0);
    }

    /**
     * Loads available reasoner services of the specified type.
     *
     * @return A list of available reasoner services
     */
    private ImmutableList<ReasonerService> loadReasonerServices() {
        final String reasonerType = "ElkSnomedReasoner";
        ImmutableList<ReasonerService> reasoners = Lists.immutable
                .ofAll(PluggableService.load(ReasonerService.class).stream()
                        .map(ServiceLoader.Provider::get)
                        .filter(reasoner -> reasoner.getName().contains(reasonerType))
                        .sorted(Comparator.comparing(ReasonerService::getName)).toList());

        LOG.info("Number of reasoners {}", reasoners.size());
        return reasoners;
    }

    /**
     * Processes data with the available reasoners.
     *
     * @param reasoners       The list of reasoners to use
     * @param startPercentage The start percentage for this phase
     * @param endPercentage   The end percentage for this phase
     */
    private void processWithReasoners(ImmutableList<ReasonerService> reasoners, double startPercentage, double endPercentage) {
        MutableList<ClassifierResults> resultList = Lists.mutable.empty();
        int reasonerCount = reasoners.size();

        // Give 10% of the reasoning phase to loading reasoners
        double reasoningWorkStart = startPercentage + ((endPercentage - startPercentage) * 0.1);
        updatePhaseProgress(startPercentage, endPercentage, 0.1);

        for (int i = 0; i < reasonerCount; i++) {
            ReasonerService rs = reasoners.get(i);
            LOG.info("Reasoner service: {}", rs);

            // Each reasoner gets an equal portion of the reasoning phase (after the 10% loading)
            double remainingReasoning = endPercentage - reasoningWorkStart;
            double reasonerStartPercent = reasoningWorkStart + ((remainingReasoning / reasonerCount) * i);
            double reasonerEndPercent = reasoningWorkStart + ((remainingReasoning / reasonerCount) * (i + 1));

            rs.init(Calculators.View.Default(), TinkarTerm.EL_PLUS_PLUS_STATED_AXIOMS_PATTERN, TinkarTerm.EL_PLUS_PLUS_INFERRED_AXIOMS_PATTERN);

            // Use this task as the progress updater
            rs.setProgressUpdater(this);

            try {
                // Extract
                updateMessage("Extracting data for reasoning...");
                rs.extractData();
                updatePhaseProgress(reasonerStartPercent, reasonerEndPercent, 0.25);

                // Load
                updateMessage("Loading data for reasoning...");
                rs.loadData();
                updatePhaseProgress(reasonerStartPercent, reasonerEndPercent, 0.5);

                // Compute
                updateMessage("Computing inferences...");
                rs.computeInferences();
                updatePhaseProgress(reasonerStartPercent, reasonerEndPercent, 0.75);

                // Build NNF
                updateMessage("Building necessary normal form...");
                rs.buildNecessaryNormalForm();
                updatePhaseProgress(reasonerStartPercent, reasonerEndPercent, 0.9);

                // Write inferred results
                updateMessage("Writing inferred results...");
                ClassifierResults results = rs.writeInferredResults();
                updatePhaseProgress(reasonerStartPercent, reasonerEndPercent, 1.0);

                LOG.info("After Size of ConceptSet: {}", rs.getReasonerConceptSet().size());
                LOG.info("ClassifierResults: inferred changes size {}", results.getConceptsWithInferredChanges().size());
                LOG.info("ClassifierResults: navigation changes size {}", results.getConceptsWithNavigationChanges().size());
                LOG.info("ClassifierResults: classificationconcept size {}", results.getClassificationConceptSet().size());
                resultList.add(results);
            } catch (Exception ex) {
                LOG.error("Failed to load data for reasoning", ex);
            }
        }
    }

    /**
     * Pushes changes to the remote Git repository if enabled.
     *
     * @param startPercentage the progress percentage at the start of this phase
     * @param endPercentage   the progress percentage at the end of this phase
     * @throws GitAPIException if a Git API error occurs
     * @throws IOException     if an I/O error occurs
     */
    private void push(double startPercentage, double endPercentage) throws GitAPIException, IOException {
        if (isCancelled()) {
            updateMessage("Operation cancelled by user.");
            return;
        }

        updatePhaseProgress(startPercentage, endPercentage, 0.0);

        updateMessage("Saving current change set...");
        updatePhaseProgress(startPercentage, endPercentage, 0.2);

        if (!saveCurrentChangeSet()) {
            updatePhaseProgress(startPercentage, endPercentage, 1.0);
            return;
        }

        updatePhaseProgress(startPercentage, endPercentage, 0.4);

        pushToRemoteRepository(startPercentage, endPercentage);
    }

    /**
     * Saves the current change set using the ChangeSetWriterService.
     *
     * @return true if the save operation was successful, false otherwise
     */
    private boolean saveCurrentChangeSet() {
        ChangeSetWriterService changeSetWriterService = PluggableService.first(ChangeSetWriterService.class);
        if (changeSetWriterService == null) {
            updateMessage("No ChangeSetWriterService available. Cannot save changes.");
            LOG.error("No ChangeSetWriterService found, cannot save changes");
            return false;
        }

        if (changeSetWriterService instanceof SaveState savableChangeSetWriterService) {
            // TODO: Refactor to return CompletableFuture<Boolean>
            try {
                savableChangeSetWriterService.save().get();
            } catch (InterruptedException | ExecutionException e) {
                updateMessage("Error while saving changes: " + e.getLocalizedMessage());
                LOG.error("Error while saving changes", e);
                return false;
            }
        }
        // TODO: Do we want to shutdown non-savable ChangeSetWriterServices?
        return true;
    }

    /**
     * Pushes changes to the remote Git repository.
     *
     * @param startPercentage the progress percentage at the start of this phase
     * @param endPercentage   the progress percentage at the end of this phase
     * @throws GitAPIException if a Git API error occurs
     * @throws IOException     if an I/O error occurs
     */
    private void pushToRemoteRepository(double startPercentage, double endPercentage)
            throws GitAPIException, IOException {
        try (Git git = Git.open(changeSetFolder.toFile())) {
            ImmutableList<String> filesToAdd = filesToAdd(changeSetFolder, "ike-cs.zip");

            if (filesToAdd.isEmpty()) {
                LOG.info("No changeset files found to add");
                updateMessage("No changeset files found to push.");
                updatePhaseProgress(startPercentage, endPercentage, 1.0);
                return;
            }

            // Add files to staging
            updateMessage("Adding files to Git staging area...");
            AddCommand addCommand = git.add();
            addCommand.setUpdate(false);
            filesToAdd.forEach(addCommand::addFilepattern);
            addCommand.call();
            updatePhaseProgress(startPercentage, endPercentage, 0.6);

            // Create a more descriptive commit message
            Set<String> addedFiles = git.status().call().getAdded();
            LOG.info("Commiting Files: {}", filesToAdd.makeString());
            String commitMessage = "Added %s changesets on %s".formatted(addedFiles.size(), new Date());

            // Commit changes
            updateMessage("Committing changes...");
            CommitCommand commitCommand = git.commit();
            commitCommand.setMessage(commitMessage);
            commitCommand.setAll(true);
            commitCommand.call();
            updatePhaseProgress(startPercentage, endPercentage, 0.7);

            // Push changes
            updateMessage("Pushing changes to remote repository...");
            PushCommand pushCommand = git.push();

            // Set a progress monitor for Git operations
            GitProgressMonitor progressMonitor = new GitProgressMonitor(
                    this,
                    startPercentage,
                    endPercentage,
                    TOTAL_WORK);

            pushCommand.setProgressMonitor(progressMonitor);
            pushCommand.setCredentialsProvider(chainingCredentialsProvider);
            pushCommand.call();
            updatePhaseProgress(startPercentage, endPercentage, 0.95);

            updateMessage("Changes successfully pushed to remote repository.");
            updatePhaseProgress(startPercentage, endPercentage, 1.0);
        }
    }

    /**
     * Identifies files in a directory matching the specified pattern that are valid changesets.
     *
     * @param directory The directory to search for files
     * @param pattern   The file extension pattern to match
     * @return An immutable list of relative paths to valid changeset files
     */
    ImmutableList<String> filesToAdd(Path directory, String pattern) {
        // TODO: Refactor to return List of Paths and force file separator in JGit commands
        try (Stream<Path> filesStream = Files.walk(directory)) {
            return Lists.immutable.ofAll(filesStream
                    .filter(Files::isRegularFile)
                    .filter(path -> path.toString().endsWith(pattern))
                    .filter(this::isValidChangeset)
                    .map(path -> directory.relativize(path).toString().replace("\\", "/")) // Must enforce '/' file path separator for JGit Add Command
                    .sorted() // Add natural sorting by file path
                    .toList());
        } catch (IOException e) {
            LOG.error("Error searching for files", e);
            return Lists.immutable.empty();
        }
    }

    /**
     * Validates whether a file is a valid changeset archive.
     *
     * @param file The path to the file to check
     * @return true if the file is a valid changeset, false otherwise
     */
    private boolean isValidChangeset(Path file) {
        try (FileSystem fs = FileSystems.newFileSystem(file)) {
            return Files.exists(fs.getPath("META-INF", "MANIFEST.MF"));
        } catch (IOException ex) {
            return false;
        }
    }

    // -------------------- Common Helper Methods --------------------

    /**
     * Updates the current progress within a phase range.
     *
     * @param phase         the current task phase
     * @param phaseProgress fractional progress within phase (0.0 to 1.0)
     */
    private void updatePhaseProgress(TaskPhase phase, double phaseProgress) {
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