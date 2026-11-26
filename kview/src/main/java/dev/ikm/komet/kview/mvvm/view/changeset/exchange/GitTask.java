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

import dev.ikm.komet.framework.events.appevents.RefreshCalculatorCacheEvent;
import dev.ikm.komet.kview.mvvm.model.GitHubPreferences;
import dev.ikm.komet.kview.mvvm.model.GitHubPreferencesDao;
import dev.ikm.komet.kview.mvvm.view.changeset.exchange.credentials.GitHubCredentialsProvider;
import dev.ikm.komet.kview.mvvm.view.changeset.exchange.credentials.GitSslValidationCredentialsProvider;
import dev.ikm.tinkar.common.alert.AlertObject;
import dev.ikm.tinkar.common.alert.AlertStreams;
import dev.ikm.tinkar.common.service.PluggableService;
import dev.ikm.tinkar.common.service.SaveState;
import dev.ikm.tinkar.common.service.TrackingCallable;
import dev.ikm.tinkar.entity.ChangeSetWriterService;
import dev.ikm.tinkar.importer.registry.ChangeSetLoad;
import dev.ikm.tinkar.events.EvtBusFactory;
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
import org.eclipse.jgit.api.Status;
import org.eclipse.jgit.api.errors.GitAPIException;
import org.eclipse.jgit.diff.DiffEntry;
import org.eclipse.jgit.lib.BranchTrackingStatus;
import org.eclipse.jgit.lib.Constants;
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

import java.io.IOException;
import java.net.URISyntaxException;
import java.nio.file.FileSystem;
import java.nio.file.FileSystems;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Comparator;
import java.util.Date;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.concurrent.CancellationException;
import java.util.stream.Stream;

import static dev.ikm.komet.framework.events.appevents.RefreshCalculatorCacheEvent.GLOBAL_REFRESH;
import static dev.ikm.tinkar.events.FrameworkTopics.CALCULATOR_CACHE_TOPIC;

/**
 * A unified task for Git operations involving medical terminology data.
 * <p>This class handles three main operations:
 * <ul>
 *   <li>CONNECT: Initialize a Git repository and connect to a remote repository</li>
 *   <li>PULL: Pull changes from remote repository and load them into the system</li>
 *   <li>SYNC: Pull changes and then push local changes back to the remote repository</li>
 * </ul>
 * <p>Each operation follows a series of phases, with detailed progress tracking throughout.
 */
public class GitTask extends TrackingCallable<Boolean> {

    private static final Logger LOG = LoggerFactory.getLogger(GitTask.class);

    /**
     * Maximum number of retry attempts for transient network errors.
     */
    private static final int MAX_RETRY_ATTEMPTS = 3;

    /**
     * Initial delay between retries in milliseconds.
     */
    private static final long INITIAL_RETRY_DELAY_MS = 1000;

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
     * <p>     * Each phase has a start and end percentage that defines its portion of the total
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
        PULL_FETCH(5.0, 40.0),
        PULL_LOAD(40.0, 95.0),
        PULL_RECEIVE_SHARED(95.0, 100.0),

        // SYNC phases (includes PULL phases plus PUSH)
        SYNC_VALIDATION(0.0, 5.0),
        SYNC_FETCH(5.0, 25.0),
        SYNC_LOAD(25.0, 50.0),
        SYNC_RECEIVE_SHARED(50.0, 55.0),
        SYNC_PUSH(55.0, 95.0),
        SYNC_SEND_SHARED(95.0, 100.0);

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

    /** Whether the remote has taken something this operation pushed, which a cancel does not take back. */
    private boolean sent;

    /** The change sets this operation loaded that the knowledge base refused, the knowledge first, then the shared. */
    private final List<ChangeSetLoad.Result> refused = new ArrayList<>();

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
        this(operationMode, changeSetFolder, connectionSuccessCallback, new GitHubPreferencesDao());
    }

    /**
     * Creates a new GitTask that takes its configuration from the given DAO.
     *
     * @param operationMode             The mode of operation (CONNECT, PULL, or SYNC)
     * @param changeSetFolder           The folder containing changesets to work with
     * @param connectionSuccessCallback Callback to run when the connection is successful
     * @param gitHubPreferencesDao      Where the configuration is kept
     */
    public GitTask(OperationMode operationMode,
                   Path changeSetFolder,
                   Runnable connectionSuccessCallback,
                   GitHubPreferencesDao gitHubPreferencesDao) {
        this.operationMode = operationMode;
        this.changeSetFolder = changeSetFolder;
        this.connectionSuccessCallback = connectionSuccessCallback;

        this.gitHubPreferencesDao = gitHubPreferencesDao;

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
                new GitHubCredentialsProvider(gitHubPreferencesDao),
                new GitSslValidationCredentialsProvider()
        );

        updateProgress(0, TOTAL_WORK);
    }

    /**
     * Executes the appropriate operation based on the selected mode.
     * For PULL and SYNC modes, first checks if the repository is fully
     * initialized and connected before proceeding.
     *
     * @return true if the operation was successful, false if it failed or was cancelled
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
        } catch (CancellationException cancelled) {
            return endCancelled();
        } catch (Exception ex) {
            if (cancelAsked()) {
                // How git and the network end what they were doing when they are told to stop
                LOG.info("{} ended by its cancel: {}", operationMode, ex.toString());
                return endCancelled();
            }
            handleException(operationMode + " did not complete", ex);
            return false;
        }
    }

    /**
     * Whether a cancel was asked for. The popup's Cancel interrupts this thread, and says so a moment later.
     */
    private boolean cancelAsked() {
        return isCancelled() || Thread.currentThread().isInterrupted();
    }

    /**
     * Stops here when a cancel was asked for.
     *
     * @throws CancellationException if so
     */
    private void stopIfCancelled() {
        if (cancelAsked()) {
            throw new CancellationException();
        }
    }

    /**
     * Ends an operation that was cancelled, which is neither a success nor a failure. A cancel that came after the
     * remote had taken the change sets does not take them back, and that is said.
     *
     * @return false, since it did not complete
     */
    private boolean endCancelled() {
        final String message = sent
                ? operationMode + " was cancelled after the remote had taken the change sets: they are sent."
                : operationMode + " was cancelled.";
        LOG.info(message);
        updateMessage(message);
        if (sent) {
            AlertStreams.getRoot().dispatch(AlertObject.makeAdvisory("Exchange", message));
        }
        return false;
    }

    /**
     * Executes a Git operation with retry logic for transient errors.
     * Uses exponential backoff between retries.
     *
     * @param <T>           the return type of the operation
     * @param operation     the Git operation to execute
     * @param operationName a descriptive name for logging purposes
     * @return the result of the operation
     * @throws GitAPIException if the operation fails after all retry attempts
     */
    private <T> T executeWithRetry(GitOperation<T> operation, String operationName) throws GitAPIException {
        GitAPIException lastException = null;

        for (int attempt = 1; attempt <= MAX_RETRY_ATTEMPTS; attempt++) {
            try {
                return operation.execute();
            } catch (GitAPIException ex) {
                lastException = ex;
                if (cancelAsked()) {
                    throw ex;
                }

                // Check if this is a retryable error (5xx server errors)
                if (isRetryableError(ex) && attempt < MAX_RETRY_ATTEMPTS) {
                    long delayMs = INITIAL_RETRY_DELAY_MS * (1L << (attempt - 1)); // Exponential backoff
                    LOG.warn("Git {} operation failed with transient error (attempt {}/{}). Retrying in {} ms...",
                            operationName, attempt, MAX_RETRY_ATTEMPTS, delayMs, ex);
                    updateMessage("Connection error. Retrying (%d/%d)...".formatted(attempt, MAX_RETRY_ATTEMPTS));

                    try {
                        Thread.sleep(delayMs);
                    } catch (InterruptedException ie) {
                        Thread.currentThread().interrupt();
                        throw ex; // Re-throw the original exception if interrupted
                    }
                } else {
                    // Non-retryable error or last attempt
                    throw ex;
                }
            }
        }

        // Should not reach here, but just in case
        throw lastException;
    }

    /**
     * Determines if an exception represents a retryable error.
     * Server errors (5xx) are considered transient and retryable.
     *
     * @param ex the exception to check
     * @return true if the error is retryable, false otherwise
     */
    private boolean isRetryableError(GitAPIException ex) {
        Throwable cause = ex.getCause();
        if (cause != null) {
            String message = cause.getMessage();
            if (message != null) {
                // Check for 5xx server errors
                if (message.contains("500 ") ||
                        message.contains("502 ") ||
                        message.contains("503 ") ||
                        message.contains("504 ") ||
                        message.contains("Internal Server Error") ||
                        message.contains("Bad Gateway") ||
                        message.contains("Service Unavailable") ||
                        message.contains("Gateway Timeout")) {
                    return true;
                }
                if (message.contains("Error writing request body") ||
                        message.contains("Connection reset") ||
                        message.contains("broken pipe") ||
                        message.contains("timed out") ||
                        message.contains("read failed")) {
                    return true;
                }
            }
            if (cause instanceof IOException) {
                return true;
            }
        }
        return false;
    }

    /**
     * Functional interface for Git operations that can throw GitAPIException.
     *
     * @param <T> the return type of the operation
     */
    @FunctionalInterface
    private interface GitOperation<T> {
        T execute() throws GitAPIException;
    }

    // -------------------- CONNECT Operations --------------------

    /**
     * Executes the CONNECT operation: makes the repository if there is none, reaches the configured remote, and
     * only then sets it and, when it has no main branch, gives it one. A remote that cannot be reached leaves the
     * repository as it was.
     *
     * @return true if the connection was successful, false otherwise
     */
    private boolean executeConnect() {
        if (!loadAndValidateConfiguration()) {
            return false;
        }

        try (Git git = openRepository()) {
            final boolean mainBranchExists = remoteHasMainBranch(git);
            stopIfCancelled();
            configureRepository(git);
            if (!mainBranchExists) {
                createAndPushMainBranch(git);
            }
            updateMessage("Connection to remote repository successfully established.");
            updatePhaseProgress(TaskPhase.CONNECT_COMPLETION, 1.0);
        } catch (GitAPIException | IOException | URISyntaxException ex) {
            stopIfCancelled();
            return handleException("The connection to %s was not made".formatted(gitUrl.getValue().trim()), ex);
        }
        stopIfCancelled();

        // Call the connection success callback if provided
        if (connectionSuccessCallback != null) {
            connectionSuccessCallback.run();
        }
        return true;
    }

    /**
     * Loads Git configuration from the knowledge base's preferences and validates required parameters.
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
     * Opens the Git repository of the changeset folder, made when the folder is not one yet.
     *
     * @return the Git instance
     * @throws GitAPIException if the repository cannot be made
     * @throws IOException     if the repository cannot be opened
     */
    private Git openRepository() throws GitAPIException, IOException {
        updateMessage("Preparing local repository for connection...");
        updatePhaseProgress(TaskPhase.CONNECT_CONFIGURATION, 0.2);

        if (Files.isDirectory(changeSetFolder.resolve(".git"))) {
            return Git.open(changeSetFolder.toFile());
        }
        InitCommand initCommand = Git.init();
        initCommand.setDirectory(changeSetFolder.toFile());
        initCommand.setInitialBranch(DEFAULT_BRANCH);
        return initCommand.call();
    }

    /**
     * Makes the repository's remote and user those of the configuration. A repository without a remote also gets
     * the settings the exchange works with; the settings of one that has a remote are left as they are.
     *
     * @param git the Git instance
     * @throws GitAPIException    if the remote cannot be set
     * @throws IOException        if the configuration cannot be saved
     * @throws URISyntaxException if the configured URL is invalid
     */
    private void configureRepository(Git git) throws GitAPIException, IOException, URISyntaxException {
        updateMessage("Setting up connection to remote repository...");
        updatePhaseProgress(TaskPhase.CONNECT_CONFIGURATION, 0.6);

        final String url = gitUrl.getValue().trim();
        final String currentUrl = git.getRepository().getConfig().getString("remote", REMOTE_NAME, "url");
        if (currentUrl == null) {
            configureRemote(git);
            configureRepositorySettings(git);
        } else if (!currentUrl.equals(url)) {
            git.remoteSetUrl().setRemoteName(REMOTE_NAME).setRemoteUri(new URIish(url)).call();
            // A URIish prints no password, which an address set by hand may hold.
            LOG.info("The remote of {} was {} and is now {}", changeSetFolder, new URIish(currentUrl), url);
        }
        configureUserSettings(git);
        updatePhaseProgress(TaskPhase.CONNECT_CONFIGURATION, 1.0);
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
     * Reaches the configured remote and tells whether it has the main branch. An empty remote is reached and has
     * no branch; a remote that is not there, or refuses the login, is not reached.
     *
     * @param git the Git instance
     * @return whether the remote has the main branch
     * @throws GitAPIException if the remote cannot be reached
     */
    private boolean remoteHasMainBranch(Git git) throws GitAPIException {
        updateMessage("Testing connection with remote repository...");
        updatePhaseProgress(TaskPhase.CONNECT_BRANCH_CHECK, 0.5);

        Collection<Ref> refs = executeWithRetry(() -> {
            LsRemoteCommand lsRemoteCommand = git.lsRemote();
            lsRemoteCommand.setHeads(true);
            lsRemoteCommand.setRemote(gitUrl.getValue().trim());
            lsRemoteCommand.setCredentialsProvider(chainingCredentialsProvider);
            return lsRemoteCommand.call();
        }, "ls-remote");
        return refs.stream().anyMatch(ref -> ref.getName().equals("refs/heads/" + DEFAULT_BRANCH));
    }

    /**
     * Pushes the repository's branches to a remote that has no main branch. A repository with no commit yet first
     * gets an initial one, of a README that is written only when there is none.
     *
     * @param git the Git instance
     * @throws GitAPIException if a Git API error occurs
     * @throws IOException     if an I/O error occurs, or the remote refuses the branch
     */
    private void createAndPushMainBranch(Git git) throws GitAPIException, IOException {
        updateMessage("Initializing connection with remote repository...");
        updatePhaseProgress(TaskPhase.CONNECT_COMPLETION, 0.3);

        if (git.getRepository().resolve(Constants.HEAD) == null) {
            final Path readme = changeSetFolder.resolve(README_FILENAME);
            if (Files.notExists(readme)) {
                Files.writeString(readme, generateReadmeContent());
            }

            // Add and commit the file
            updateMessage("Preparing initial data for secure transmission...");
            updatePhaseProgress(TaskPhase.CONNECT_COMPLETION, 0.5);

            git.add().addFilepattern(README_FILENAME).call();
            git.commit()
                    .setMessage("Initial commit for changeset exchange")
                    .setAuthor(gitUsername.getValue(), gitEmail.getValue())
                    .call();
        }

        // Push the commit to remote
        updateMessage("Establishing first connection with remote repository...");
        updatePhaseProgress(TaskPhase.CONNECT_COMPLETION, 0.7);

        // The monitor ends when everything is sent, before the remote has answered: short of the phase's end
        final double phaseStart = TaskPhase.CONNECT_COMPLETION.getStart();
        final double phaseWidth = TaskPhase.CONNECT_COMPLETION.getEnd() - phaseStart;
        final GitProgressMonitor progressMonitor = new GitProgressMonitor(
                this,
                connectProgress(phaseStart + phaseWidth * 0.7),
                connectProgress(phaseStart + phaseWidth * 0.9),
                TOTAL_WORK);

        sent = PushAnswer.requireAccepted(executeWithRetry(() -> git.push()
                .setRemote(REMOTE_NAME)
                .setCredentialsProvider(chainingCredentialsProvider)
                .setProgressMonitor(progressMonitor)
                .setPushAll()
                .call(), "push"));

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
        TaskPhase receivePhase = pushChanges ? TaskPhase.SYNC_RECEIVE_SHARED : TaskPhase.PULL_RECEIVE_SHARED;

        updateMessage("Validating environment...");
        updateProgress(0, TOTAL_WORK);

        // Validation complete
        updatePhaseProgress(validationPhase.getStart(), validationPhase.getEnd(), 1.0);

        updateMessage("Starting %s process.".formatted(pushChanges ? "synchronization" : "pulling"));

        // Pull phase - now returns a list of added files
        ImmutableList<String> addedFiles = pull(validationPhase.getEnd(), pullPhase.getEnd(), new GitSyncFileSorter());

        // Load changesets phase - pass the list of added files
        loadChangesets(pullPhase.getEnd(), loadPhase.getEnd(), addedFiles);
        // Shared data after the knowledge it refers to (IKE-Network/ike-issues#1313)
        receiveShared(pushChanges);
        updatePhaseProgress(receivePhase.getStart(), receivePhase.getEnd(), 1.0);
        //Refresh UI after changesets are loaded
        EvtBusFactory.getDefaultEvtBus().publish(CALCULATOR_CACHE_TOPIC, new RefreshCalculatorCacheEvent(this, GLOBAL_REFRESH));

        stopIfCancelled();

        // No reasoner runs here: a Pull or Sync brings and sends what people wrote. A person runs the reasoner from
        // the Reasoner results window, and the inferred results go with that person's change sets.
        if (pushChanges) {
            push(TaskPhase.SYNC_PUSH.getStart(), TaskPhase.SYNC_PUSH.getEnd());
            sendShared();
            updatePhaseProgress(TaskPhase.SYNC_SEND_SHARED.getStart(), TaskPhase.SYNC_SEND_SHARED.getEnd(), 1.0);
        }
        stopIfCancelled();

        // A refused change set does not fail the operation; its last message names the file (IKE-Network/ike-issues#1406).
        String operation = pushChanges ? "Synchronization" : "Pulling";
        updateMessage(refused.isEmpty() ? operation + " completed successfully."
                : operation + " completed; " + RefusedChangeSets.sentence(refused));
        return true;
    }

    /**
     * Pulls the latest changes from the remote Git repository and returns a list of added files with default sorting.
     *
     * @param startPercentage the progress percentage at the start of this phase
     * @param endPercentage   the progress percentage at the end of this phase
     * @return a list of relative paths to files that were added during the pull operation
     * @throws GitAPIException if a Git API error occurs
     * @throws IOException     if an I/O error occurs
     */
    private ImmutableList<String> pull(double startPercentage, double endPercentage) throws GitAPIException, IOException {
        Comparator<String> defaultSorter = String::compareTo;
        return pull(startPercentage, endPercentage, defaultSorter);
    }

    /**
     * Pulls the latest changes from the remote Git repository and returns a list of added files with specified sorting.
     *
     * @param startPercentage the progress percentage at the start of this phase
     * @param endPercentage   the progress percentage at the end of this phase
     * @param fileSorter      the method to determine sorting of file paths
     * @return a list of relative paths to files that were added during the pull operation
     * @throws GitAPIException if a Git API error occurs
     * @throws IOException     if an I/O error occurs
     */
    private ImmutableList<String> pull(double startPercentage, double endPercentage,
                                       Comparator<String> fileSorter) throws GitAPIException, IOException {
        stopIfCancelled();
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

            PullResult pullResult = executeWithRetry(() -> pullCommand.call(), "pull");

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
                    newFiles.sort(fileSorter);
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
                cleanupAfterFailedPull(git, oldHead);
                updateMessage("Pull failed: " + pullResult.getMergeResult().getMergeStatus());
                throw new GitAPIException("Pull failed: " + pullResult.getMergeResult().getMergeStatus()) {
                };
            }
        }
    }

    /**
     * Attempts to return the repository to its pre-pull state after a failed pull so a retry starts clean.
     *
     * @param git     the Git instance
     * @param oldHead the commit to reset back to (may be null for new repos)
     */
    private void cleanupAfterFailedPull(Git git, ObjectId oldHead) {
        try {
            if (oldHead != null) {
                git.reset().setMode(org.eclipse.jgit.api.ResetCommand.ResetType.HARD).setRef(oldHead.getName()).call();
                LOG.info("Reset repository to {}", oldHead.getName());
            } else {
                git.reset().setMode(org.eclipse.jgit.api.ResetCommand.ResetType.HARD).call();
                LOG.info("Reset repository to clean state after failed pull");
            }
        } catch (Exception resetEx) {
            LOG.warn("Failed to reset repository after pull failure", resetEx);
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
     * Loads the pulled change sets the knowledge base has not registered, in the order given, and
     * registers each ({@code dev-ike-dashboard}, decision 20): a file the store already holds, by
     * an import or an earlier open, is not loaded again, and a file refused for a component no
     * file before it carried is tried again after the rest. A cancel is taken up after the load: the
     * next pull brings only what is new to the folder, and would leave these unloaded.
     *
     * @param startPercentage   the progress percentage at the start of this phase
     * @param endPercentage     the progress percentage at the end of this phase
     * @param relativeFilePaths list of relative paths to changeset files to load
     */
    private void loadChangesets(double startPercentage, double endPercentage,
                                ImmutableList<String> relativeFilePaths) {
        final boolean interrupted = Thread.interrupted();    // so a load that waits is not cut short by it
        try {
            loadPulled(startPercentage, endPercentage, relativeFilePaths);
        } finally {
            if (interrupted) {
                Thread.currentThread().interrupt();
            }
        }
    }

    private void loadPulled(double startPercentage, double endPercentage, ImmutableList<String> relativeFilePaths) {
        updatePhaseProgress(startPercentage, endPercentage, 0.0);

        if (relativeFilePaths.isEmpty()) {
            updateMessage("No changeset files found to load.");
            updatePhaseProgress(startPercentage, endPercentage, 1.0);
            return;
        }

        List<Path> files = relativeFilePaths.collect(changeSetFolder::resolve).castToList();
        ChangeSetLoad.Report report = ChangeSetLoad.load(changeSetFolder, files, (index, count, file, retry) -> {
            updateMessage((retry ? "Retrying changeset " : "Loading changeset ") + index + " of " + count + ": "
                    + file.getFileName());
            // Progress based on the file's position in its pass; 10% is reserved for completion.
            updatePhaseProgress(startPercentage, endPercentage, (double) index / count * 0.9);
        });
        LOG.info("Pulled change sets: {}\n{}", report.summary(), report.markdown());

        StringBuilder message = new StringBuilder("Loaded " + report.loaded().size() + " of " + files.size() + " changesets");
        long registeredBefore = report.count(ChangeSetLoad.Outcome.REGISTERED_BEFORE);
        if (registeredBefore > 0) {
            message.append(", ").append(registeredBefore).append(" already in the knowledge base");
        }
        if (!report.refused().isEmpty()) {
            message.append(", ").append(report.refused().size()).append(" refused");
            refused.addAll(report.refused());
            RefusedChangeSets.advise("Exchange", "brought by this " + operationMode, changeSetFolder.getParent(),
                    report.refused());
        }
        updateMessage(message.append('.').toString());
        updatePhaseProgress(startPercentage, endPercentage, 1.0);
    }

    // -------------------- Shared data --------------------

    /** The exchange of the knowledge base's shared data, with the same login (IKE-Network/ike-issues#1313). */
    private SharedDataExchange sharedData() {
        return new SharedDataExchange(changeSetFolder, chainingCredentialsProvider);
    }

    /**
     * Brings the shared data others synced. A failure is reported and the sync of the knowledge goes on, and so does
     * a shared change set the knowledge base refused, which the last message names.
     *
     * @param sending whether this sync also sends, so shared documents are first brought up to date with their files
     */
    private void receiveShared(boolean sending) {
        if (!cancelAsked()) {
            updateMessage("Bringing the shared documents...");
            refused.addAll(sharedData().receive(sending));
        }
    }

    /** Sends the shared data of this knowledge base. A failure is reported; the next sync sends it. */
    private void sendShared() {
        if (!cancelAsked()) {
            updateMessage("Sending the shared documents...");
            sharedData().send();
        }
    }

    /**
     * Pushes changes to the remote Git repository if enabled.
     *
     * @param startPercentage the progress percentage at the start of this phase
     * @param endPercentage   the progress percentage at the end of this phase
     * @throws GitAPIException if a Git API error occurs
     * @throws IOException     if the change set cannot be saved, or an I/O error occurs
     */
    private void push(double startPercentage, double endPercentage) throws GitAPIException, IOException {
        updatePhaseProgress(startPercentage, endPercentage, 0.0);

        updateMessage("Saving current change set...");
        updatePhaseProgress(startPercentage, endPercentage, 0.2);

        saveCurrentChangeSet();

        updatePhaseProgress(startPercentage, endPercentage, 0.4);

        try {
            pushToRemoteRepository(startPercentage, endPercentage);
        } catch (GitAPIException | IOException pushEx) {
            stopIfCancelled();
            StringBuilder msg = new StringBuilder("Push failed (network/remote error). Local changes are saved; please retry Sync to push them.");
            appendAheadHintIfAny(msg);
            updateMessage(msg.toString());
            LOG.error("Push failed, user can retry safely", pushEx);
            throw pushEx;
        }
    }

    /**
     * Saves the current change set using the ChangeSetWriterService.
     *
     * @throws IOException if it is not saved, so a push would send the folder without it
     */
    private void saveCurrentChangeSet() throws IOException {
        // TODO: Do we want to shutdown non-savable ChangeSetWriterServices?
        if (PluggableService.first(ChangeSetWriterService.class) instanceof SaveState savableChangeSetWriterService) {
            SaveAnswer.requireSaved(savableChangeSetWriterService.save());
        }
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

            Status status = git.status().call();
            if (!status.isClean()) {
                // Create a more descriptive commit message
                Set<String> addedFiles = status.getAdded();
                LOG.info("Commiting Files: {}", filesToAdd.makeString());
                String commitMessage = "Added %s changesets on %s".formatted(addedFiles.size(), new Date());

                // Commit changes
                updateMessage("Committing changes...");
                CommitCommand commitCommand = git.commit();
                commitCommand.setMessage(commitMessage);
                commitCommand.setAll(true);
                commitCommand.call();
                updatePhaseProgress(startPercentage, endPercentage, 0.7);
            } else {
                LOG.info("No new changes to commit; attempting to push existing commits.");
                updatePhaseProgress(startPercentage, endPercentage, 0.7);
            }

            // Push changes
            stopIfCancelled();
            updateMessage("Pushing changes to remote repository...");
            PushCommand pushCommand = git.push();

            // The monitor ends when everything is sent, before the remote has answered: short of the phase's end
            double phaseWidth = endPercentage - startPercentage;
            GitProgressMonitor progressMonitor = new GitProgressMonitor(
                    this,
                    startPercentage + phaseWidth * 0.7,
                    startPercentage + phaseWidth * 0.9,
                    TOTAL_WORK);

            pushCommand.setProgressMonitor(progressMonitor);
            pushCommand.setCredentialsProvider(chainingCredentialsProvider);
            sent |= PushAnswer.requireAccepted(executeWithRetry(pushCommand::call, "push"));
            updatePhaseProgress(startPercentage, endPercentage, 0.95);

            updateMessage("Changes successfully pushed to remote repository.");
            updatePhaseProgress(startPercentage, endPercentage, 1.0);
        }
    }

    /**
     * Append a hint when the local branch is ahead of remote so the user knows a retry will push existing commits.
     *
     * @param msg message builder to append to
     */
    private void appendAheadHintIfAny(StringBuilder msg) {
        try (Git git = Git.open(changeSetFolder.toFile())) {
            Repository repo = git.getRepository();
            BranchTrackingStatus trackingStatus = BranchTrackingStatus.of(repo, repo.getBranch());
            if (trackingStatus != null && trackingStatus.getAheadCount() > 0) {
                msg.append(" Local branch is ahead of remote; rerun Sync to push the existing commit.");
            }
        } catch (Exception ex) {
            LOG.debug("Unable to determine branch tracking status after push failure", ex);
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
     * Updates the current progress within a phase of the connection.
     *
     * @param phase         the current phase of the connection
     * @param phaseProgress fractional progress within phase (0.0 to 1.0)
     */
    private void updatePhaseProgress(TaskPhase phase, double phaseProgress) {
        updatePhaseProgress(connectProgress(phase.getStart()), connectProgress(phase.getEnd()), phaseProgress);
    }

    /**
     * Where a point of the connection lies in this operation. A pull and a sync connect first, within their
     * validation phase: progress only moves forward, so a connection that ran to its own end would leave nothing
     * to show of what follows it.
     *
     * @param percentage a percentage of the connection
     * @return the percentage of the operation
     */
    private double connectProgress(double percentage) {
        return switch (operationMode) {
            case CONNECT -> percentage;
            case PULL -> percentage * TaskPhase.PULL_VALIDATION.getEnd() / TOTAL_WORK;
            case SYNC -> percentage * TaskPhase.SYNC_VALIDATION.getEnd() / TOTAL_WORK;
        };
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
