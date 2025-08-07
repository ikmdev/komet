package dev.ikm.komet.app;

import dev.ikm.komet.framework.progress.ProgressHelper;
import dev.ikm.komet.kview.controls.GlassPane;
import dev.ikm.komet.kview.mvvm.view.changeset.exchange.*;
import dev.ikm.komet.kview.mvvm.viewmodel.GitHubPreferencesViewModel;
import dev.ikm.tinkar.common.service.ServiceKeys;
import dev.ikm.tinkar.common.service.ServiceProperties;
import dev.ikm.tinkar.common.service.TinkExecutor;
import javafx.scene.control.Hyperlink;
import javafx.scene.layout.Pane;
import org.carlfx.cognitive.loader.FXMLMvvmLoader;
import org.carlfx.cognitive.loader.JFXNode;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.CompletableFuture;
import java.util.prefs.BackingStoreException;

import static dev.ikm.komet.framework.events.FrameworkTopics.LANDING_PAGE_TOPIC;
import static dev.ikm.komet.kview.fxutils.FXUtils.runOnFxThread;
import static dev.ikm.komet.kview.mvvm.view.changeset.exchange.GitPropertyName.*;
import static dev.ikm.komet.kview.mvvm.view.changeset.exchange.GitPropertyName.GIT_STATUS;
import static dev.ikm.komet.kview.mvvm.view.changeset.exchange.GitTask.OperationMode.CONNECT;

public class AppGithub {

    private static final Logger LOG = LoggerFactory.getLogger(AppGithub.class);
    static final String CHANGESETS_DIR = "changeSets";

    private final AppInterface app;

    public AppGithub(AppInterface app) {
        this.app = app;
    }

    /**
     * Prompts the user for GitHub preferences and repository information.
     * <p>
     * This method displays a dialog where the user can enter their GitHub credentials
     * and repository URL. It creates a CompletableFuture that will be resolved with
     * {@code true} if the user successfully enters valid credentials and connects,
     * or {@code false} if they cancel the operation.
     *
     * @return A CompletableFuture that completes with true if valid GitHub preferences
     *         were successfully provided by the user, or false if the user canceled the operation
     */
    CompletableFuture<Boolean> promptForGitHubPrefs() {
        // Create a CompletableFuture that will be completed when the user makes a choice
        CompletableFuture<Boolean> future = new CompletableFuture<>();

        // Show dialog on JavaFX thread
        runOnFxThread(() -> {
            GlassPane glassPane = new GlassPane(app.getLandingPageController().getRoot());

            final JFXNode<Pane, GitHubPreferencesController> githubPreferencesNode = FXMLMvvmLoader
                    .make(GitHubPreferencesController.class.getResource("github-preferences.fxml"));
            final Pane dialogPane = githubPreferencesNode.node();
            final GitHubPreferencesController controller = githubPreferencesNode.controller();
            Optional<GitHubPreferencesViewModel> githubPrefsViewModelOpt = githubPreferencesNode
                    .getViewModel("gitHubPreferencesViewModel");

            controller.getConnectButton().setOnAction(actionEvent -> {
                controller.handleConnectButtonEvent(actionEvent);
                githubPrefsViewModelOpt.ifPresent(githubPrefsViewModel -> {
                    if (githubPrefsViewModel.validProperty().get()) {
                        glassPane.removeContent(dialogPane);
                        glassPane.hide();
                        future.complete(true); // Complete with true on successful connection
                    }
                });
            });

            controller.getCancelButton().setOnAction(_ -> {
                glassPane.removeContent(dialogPane);
                glassPane.hide();
                future.complete(false); // Complete with false on cancel
            });

            glassPane.addContent(dialogPane);
            glassPane.show();
        });

        return future;
    }

    /**
     * Sets up the UI to reflect a disconnected GitHub state.
     * <p>
     * This method updates the GitHub status hyperlink in the landing page to show that
     * the application is disconnected from GitHub. When clicked, the hyperlink will
     * attempt to connect to GitHub by calling the {@link #connectToGithub()} method.
     * <p>
     * The method runs on the JavaFX application thread to ensure thread safety when
     * updating UI components.
     */
    private void gotoGitHubDisconnectedState() {
        runOnFxThread(() -> {
            if (app.getLandingPageController() != null) {
                Hyperlink githubStatusHyperlink = app.getLandingPageController().getGithubStatusHyperlink();
                githubStatusHyperlink.setText("Disconnected, Select to connect");
                githubStatusHyperlink.setOnAction(event -> connectToGithub());
            }
        });
    }

    /**
     * Sets up the UI to reflect a connected GitHub state.
     * <p>
     * This method updates the GitHub status hyperlink in the landing page to show that
     * the application is successfully connected to GitHub. When clicked, the hyperlink
     * will disconnect from GitHub by calling the {@link #disconnectFromGithub()} method.
     * <p>
     * The method runs on the JavaFX application thread to ensure thread safety when
     * updating UI components.
     */
    private void gotoGitHubConnectedState() {
        runOnFxThread(() -> {
            if (app.getLandingPageController() != null) {
                Hyperlink githubStatusHyperlink = app.getLandingPageController().getGithubStatusHyperlink();
                githubStatusHyperlink.setText("Connected");
                githubStatusHyperlink.setOnAction(event -> disconnectFromGithub());
            }
        });
    }

    /**
     * Executes a Git task, ensuring preferences are valid first.
     * <p>
     * This method performs the following operations:
     * <ol>
     *   <li>Verifies that the data store root is available</li>
     *   <li>Creates a changeset folder if it doesn't exist</li>
     *   <li>Validates GitHub preferences and prompts for them if missing</li>
     *   <li>Creates and runs the appropriate GitTask based on the operation mode</li>
     * </ol>
     * If GitHub preferences are missing or invalid, this method will prompt the user
     * to enter them before proceeding with the requested operation.
     *
     * @param mode The operation mode (CONNECT, PULL, or SYNC) that determines what
     *             Git operations will be performed
     */
    void executeGitTask(GitTask.OperationMode mode) {
        Optional<File> optionalDataStoreRoot = ServiceProperties.get(ServiceKeys.DATA_STORE_ROOT);
        if (optionalDataStoreRoot.isEmpty()) {
            LOG.error("ServiceKeys.DATA_STORE_ROOT not provided.");
            return;
        }

        final File changeSetFolder = new File(optionalDataStoreRoot.get(), CHANGESETS_DIR);
        if (!changeSetFolder.exists()) {
            if (!changeSetFolder.mkdirs()) {
                LOG.error("Unable to create {} directory", CHANGESETS_DIR);
                return;
            }
        }

        // Check if GitHub preferences are valid first
        if (!app.getGitHubPreferencesDao().validate()) {
            LOG.info("GitHub preferences missing or incomplete. Prompting user...");

            // Prompt for preferences before proceeding
            promptForGitHubPrefs().thenAccept(confirmed -> {
                if (confirmed) {
                    // Preferences entered successfully, now run the GitTask
                    createAndRunGitTask(mode, changeSetFolder);
                } else {
                    LOG.info("User cancelled the GitHub preferences dialog");
                }
            });
        } else {
            // Preferences already valid, run the GitTask directly
            createAndRunGitTask(mode, changeSetFolder);
        }
    }

    /**
     * Creates and runs a GitTask with the specified operation mode.
     * <p>
     * This helper method is called after GitHub preferences have been validated. It:
     * <ol>
     *   <li>Creates a new GitTask with the specified operation mode</li>
     *   <li>Registers a success callback to update the UI state</li>
     *   <li>Runs the task with progress tracking</li>
     *   <li>Handles errors and updates the UI accordingly</li>
     * </ol>
     * The task is executed asynchronously through the ProgressHelper service to
     * provide user feedback during long-running operations.
     *
     * @param operationMode The operation mode specifying which Git operations to perform
     * @param changeSetFolder The folder where the Git repository is located
     * @return A CompletableFuture that completes with true if the operation was successful,
     *         or false if it failed or was cancelled
     */
    CompletableFuture<Boolean> createAndRunGitTask(GitTask.OperationMode operationMode, File changeSetFolder) {
        // Create a GitTask with only the connection success callback
        GitTask gitTask = new GitTask(operationMode, changeSetFolder.toPath(), this::gotoGitHubConnectedState);

        // Run the task
        return ProgressHelper.progress(LANDING_PAGE_TOPIC, gitTask)
                .whenComplete((result, throwable) -> {
                    if (throwable != null) {
                        LOG.error("Error during {} operation", operationMode, throwable);
                        disconnectFromGithub();
                    } else if (!result) {
                        LOG.warn("{} operation did not complete successfully", operationMode);
                        disconnectFromGithub();
                    }
                });
    }

    /**
     * Initiates a connection to GitHub.
     * <p>
     * This method establishes a connection to GitHub by executing a GitTask in CONNECT mode.
     * If successful, the UI will be updated to reflect the connected state, and the local
     * Git repository will be initialized and configured with the remote origin.
     * <p>
     * If GitHub preferences are missing or invalid, the user will be prompted to
     * enter them before the connection is established.
     */
    void connectToGithub() {
        LOG.info("Attempting to connect to GitHub...");
        executeGitTask(CONNECT);
    }

    /**
     * Disconnects from GitHub and cleans up local resources.
     * <p>
     * This method performs the following cleanup operations:
     * <ol>
     *   <li>Logs the disconnection attempt</li>
     *   <li>Removes all GitHub-related preferences from user preferences</li>
     *   <li>Deletes the local .git repository folder if it exists</li>
     *   <li>Deletes the README.md file if it exists</li>
     *   <li>Updates the UI to reflect the disconnected state</li>
     * </ol>
     * If any errors occur during this process, they are logged but do not prevent
     * the disconnection from completing.
     */
    void disconnectFromGithub() {
        LOG.info("Disconnecting from GitHub...");

        // Delete stored user preferences related to GitHub
        try {
            app.getGitHubPreferencesDao().delete();
            LOG.info("Successfully deleted GitHub preferences");
        } catch (BackingStoreException e) {
            LOG.error("Failed to delete GitHub preferences", e);
        }
        // TODO: Refactor GitHub disconnect and credentials management without deleting .git folder
//        // Delete the .git folder and README.md if they exist
//        Optional<File> optionalDataStoreRoot = ServiceProperties.get(ServiceKeys.DATA_STORE_ROOT);
//        if (optionalDataStoreRoot.isPresent()) {
//            final File changeSetFolder = new File(optionalDataStoreRoot.get(), CHANGESETS_DIR);
//
//            // Delete .git folder
//            final File gitDir = new File(changeSetFolder, ".git");
//            if (gitDir.exists() && gitDir.isDirectory()) {
//                try {
//                    FileUtils.delete(gitDir, FileUtils.RECURSIVE);
//                    LOG.info("Successfully deleted .git folder at: {}", gitDir.getAbsolutePath());
//                } catch (IOException e) {
//                    LOG.error("Failed to delete .git folder at: {}", gitDir.getAbsolutePath(), e);
//                }
//            }
//
//            // Delete README.md file
//            final File readmeFile = new File(changeSetFolder, README_FILENAME);
//            if (readmeFile.exists() && readmeFile.isFile()) {
//                try {
//                    if (readmeFile.delete()) {
//                        LOG.info("Successfully deleted {} file at: {}", README_FILENAME, readmeFile.getAbsolutePath());
//                    } else {
//                        LOG.error("Failed to delete {} file at: {}", README_FILENAME, readmeFile.getAbsolutePath());
//                    }
//                } catch (SecurityException e) {
//                    LOG.error("Security exception while deleting {} file at: {}", readmeFile.getAbsolutePath(), e);
//                }
//            }
//        } else {
//            LOG.warn("Could not access data store root to delete .git folder and README.md");
//        }
//
        // Update the UI state
        gotoGitHubDisconnectedState();
    }



    /**
     * Displays information about the current Git repository.
     * <p>
     * This method checks if a Git repository exists and displays basic information about it.
     * If no repository exists or is not properly configured, the user will be prompted to
     * enter GitHub preferences before proceeding. Upon successful connection to GitHub,
     * repository information will be fetched and displayed in a dialog.
     * <p>
     * The method performs the following operations:
     * <ol>
     *   <li>Verifies that the data store root is available</li>
     *   <li>Checks if a Git repository exists in the changeset folder</li>
     *   <li>If no repository exists, prompts for GitHub preferences and initiates connection</li>
     *   <li>Fetches and displays repository information</li>
     * </ol>
     */
    void infoAction() {
        Optional<File> optionalDataStoreRoot = ServiceProperties.get(ServiceKeys.DATA_STORE_ROOT);
        if (optionalDataStoreRoot.isEmpty()) {
            LOG.error("ServiceKeys.DATA_STORE_ROOT not provided.");
            return;
        }

        final File changeSetFolder = new File(optionalDataStoreRoot.get(), CHANGESETS_DIR);
        final File gitDir = new File(changeSetFolder, ".git");

        if (gitDir.exists()) {
            fetchAndShowRepositoryInfo(changeSetFolder);
        } else {
            // Prompt for preferences before proceeding
            promptForGitHubPrefs().thenCompose(confirmed -> {
                if (confirmed) {
                    // Preferences entered successfully, now run the GitTask
                    return createAndRunGitTask(CONNECT, changeSetFolder);
                } else {
                    return CompletableFuture.completedFuture(false);
                }
            }).thenAccept(confirmed -> {
                if (confirmed) {
                    fetchAndShowRepositoryInfo(changeSetFolder);
                }
            });
        }
    }

    /**
     * Fetches repository information and displays it in a dialog.
     * <p>
     * This method asynchronously retrieves information about the Git repository
     * located in the specified folder using an {@code InfoTask}, then displays
     * the results in a dialog. The operation is performed on a background thread
     * to avoid blocking the UI.
     *
     * @param changeSetFolder The repository folder to fetch information from
     */
    private void fetchAndShowRepositoryInfo(File changeSetFolder) {
        CompletableFuture.supplyAsync(() -> {
                    try {
                        InfoTask task = new InfoTask(changeSetFolder.toPath());
                        return task.call();
                    } catch (Exception ex) {
                        throw new RuntimeException("Failed to fetch repository information", ex);
                    }
                }, TinkExecutor.threadPool())
                .thenCompose(repoInfo -> showRepositoryInfoDialog(repoInfo)
                        .thenAccept(confirmed -> {
                            if (confirmed) {
                                LOG.info("User closed the repository info dialog");
                            }
                        }));
    }

    /**
     * Displays the repository information dialog.
     * <p>
     * This method creates and displays a dialog showing Git repository information
     * including URL, username, email, and status. The dialog is displayed using a
     * glass pane overlay on top of the landing page.
     * <p>
     * The method returns a CompletableFuture that will be completed when the user
     * closes the dialog.
     *
     * @param repoInfo Map containing repository information with keys defined in {@code GitPropertyName}
     * @return A CompletableFuture that completes with {@code true} when the user closes the dialog
     */
    private CompletableFuture<Boolean> showRepositoryInfoDialog(Map<GitPropertyName, String> repoInfo) {
        // Create a CompletableFuture that will be completed when the user makes a choice
        CompletableFuture<Boolean> future = new CompletableFuture<>();

        // Show dialog on JavaFX thread
        runOnFxThread(() -> {
            GlassPane glassPane = new GlassPane(app.getLandingPageController().getRoot());

            final JFXNode<Pane, GitHubInfoController> githubInfoNode = FXMLMvvmLoader
                    .make(GitHubInfoController.class.getResource("github-info.fxml"));
            final Pane dialogPane = githubInfoNode.node();
            final GitHubInfoController controller = githubInfoNode.controller();

            controller.getGitUrlTextField().setText(repoInfo.get(GIT_URL));
            controller.getGitUsernameTextField().setText(repoInfo.get(GIT_USERNAME));
            controller.getGitEmailTextField().setText(repoInfo.get(GIT_EMAIL));
            controller.getStatusTextArea().setText(repoInfo.get(GIT_STATUS));

            controller.getCloseButton().setOnAction(_ -> {
                glassPane.removeContent(dialogPane);
                glassPane.hide();
                future.complete(true); // Complete with true on close
            });

            glassPane.addContent(dialogPane);
            glassPane.show();
        });

        return future;
    }



}
