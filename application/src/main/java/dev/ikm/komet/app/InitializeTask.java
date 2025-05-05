package dev.ikm.komet.app;

import dev.ikm.tinkar.common.service.TinkExecutor;
import dev.ikm.tinkar.common.service.TrackingCallable;
import javafx.application.Platform;
import org.eclipse.jgit.api.Git;
import org.eclipse.jgit.api.InitCommand;
import org.eclipse.jgit.lib.StoredConfig;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.nio.file.Path;

/**
 * The InitializeTask class represents a task that initializes a folder for synchronization, by
 * adding the necessary git configuration info. .
 * It extends the TrackingCallable class.
 */
class InitializeTask extends TrackingCallable<Void> {
    private static final Logger LOG = LoggerFactory.getLogger(InitializeTask.class);

    final Path changeSetFolder;

    /**
     * The InitializeTask class represents a task that initializes a folder for synchronization, by
     * adding the necessary git configuration info. It extends the TrackingCallable class.
     */
    public InitializeTask(Path changeSetFolder) {
        super(false, true);
        this.changeSetFolder = changeSetFolder;
        updateTitle("Initializing " + changeSetFolder +
                " for synchronization");

        addToTotalWork(1);
    }

    /**
     * Computes the necessary git configuration for initializing a folder for synchronization.
     *
     * @return Always returns null.
     * @throws Exception If there is an error during computation.
     */
    @Override
    protected Void compute() throws Exception {
        updateMessage("Initializing...");
        try {
            InitCommand initCommand = Git.init();
            initCommand.setDirectory(changeSetFolder.toFile());
            initCommand.setInitialBranch("main");
            initCommand.call();

            Git git = Git.open(changeSetFolder.toFile());
            if (git.getRepository().getRemoteNames().isEmpty()) {
                updateMessage("Updating Git Configuration");
                InitRemoteTask task = new InitRemoteTask(git);
                Platform.runLater(task);
                if (task.get()) {
                    StoredConfig config = git.getRepository().getConfig();
                    config.setBoolean("core", null, "ignorecase", true);
                    config.setBoolean("core", null, "bare", false);
                    config.setString("submodule", null, "active", ".");
                    config.setBoolean("commit", null, "gpgsign", false);
                    // GPG Format Workaround: https://bugs.eclipse.org/bugs/show_bug.cgi?id=581483
                    config.setString("gpg", null, "format", "x509");
                    config.save();
                    TinkExecutor.threadPool().submit(new PullTask(changeSetFolder));
                    updateMessage("Successfully updated Git Configuration");
                }
            } else {
                updateMessage("Git Configuration already exists, skipping reconfiguration.");
            }
            completedUnitOfWork();
            return null;
        } catch (IllegalArgumentException | IOException ex) {
            updateMessage("Error Initializing");
            LOG.error(ex.getLocalizedMessage(), ex);
            return null;
        }
    }
}