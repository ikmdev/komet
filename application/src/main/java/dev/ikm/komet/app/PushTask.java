package dev.ikm.komet.app;

import dev.ikm.komet.app.credential.PluginCredentialProvider;
import dev.ikm.tinkar.common.alert.AlertStreams;
import dev.ikm.tinkar.common.service.TrackingCallable;
import org.eclipse.jgit.api.Git;
import org.eclipse.jgit.api.PushCommand;
import org.eclipse.jgit.transport.PushResult;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.nio.file.Path;

public class PushTask extends TrackingCallable<Void> {
    private static final Logger LOG = LoggerFactory.getLogger(PushTask.class);

    final Path changeSetFolder;

        public PushTask(Path changeSetFolder) {
            this.changeSetFolder = changeSetFolder;
            updateTitle("Pushing files to server");
            updateProgress(-1,1);
        }

        @Override
        protected Void compute() throws Exception {
            try {
                this.updateMessage("Getting synchronization service");

                Git git = Git.open(changeSetFolder.toFile());
                PushCommand pushCommand = git.push();
                pushCommand.setProgressMonitor(new JGitProgressMonitor());
                pushCommand.setCredentialsProvider(
                        new PluginCredentialProvider());
                Iterable<PushResult> result = pushCommand.call();
            } catch (IllegalArgumentException ex) {
                AlertStreams.dispatchToRoot(ex);
            }
            updateProgress(1,1);
            return null;
        }
    }


