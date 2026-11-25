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
package dev.ikm.komet.kview.mvvm.view.changeset.exchange.test;

import dev.ikm.komet.kview.mvvm.model.GitHubPreferencesDao;
import dev.ikm.komet.kview.mvvm.view.changeset.exchange.GitTask;
import dev.ikm.komet.preferences.KometPreferences;
import dev.ikm.komet.preferences.PreferencesServiceFactory;
import dev.ikm.tinkar.common.service.TrackingListener;
import org.eclipse.jgit.api.Git;
import org.eclipse.jgit.lib.ObjectId;
import org.eclipse.jgit.lib.Ref;
import org.eclipse.jgit.lib.RepositoryState;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * An exchange that is cancelled is neither a success nor a failure, and the repository it leaves is one the next
 * exchange works with.
 */
class GitTaskCancelTest {

    private static final String TEST_NODE = "dev/ikm/komet/kview/test/git-task-cancel";
    private static final String TESTING = "Testing connection with remote repository...";
    private static final String SENDING_FIRST = "Establishing first connection with remote repository...";
    private static final String PULLING = "Pulling changes from remote...";
    private static final String CHECKING_OUT = "Checking out files";

    @TempDir
    Path root;

    private final AtomicInteger connections = new AtomicInteger();
    private KometPreferences node;
    private GitHubPreferencesDao dao;
    private Path changeSets;
    private Path remote;

    @BeforeEach
    void makeRemote() throws Exception {
        node = PreferencesServiceFactory.provider().getUserPreferences().node(TEST_NODE);
        dao = GitHubPreferencesDao.backedBy(() -> node);
        changeSets = Files.createDirectories(root.resolve("kb/changeSets"));
        remote = root.resolve("remote.git");
        Git.init().setBare(true).setDirectory(remote.toFile()).setInitialBranch("main").call().close();
        dao.save("file://" + remote, "author@example.org", "author", "Not-a-real-token-42".toCharArray());
    }

    @AfterEach
    void removeThrowAwayNode() throws Exception {
        dao.forgetPassword();
        node.removeNode();
    }

    @Test
    void aConnectionCancelledAsTheRemoteIsReachedIsNotMade() throws Exception {
        Cancelling run = run(GitTask.OperationMode.CONNECT, TESTING);
        assertFalse(run.result);
        assertEquals("Connect was cancelled.", run.last());
        assertEquals(0, connections.get(), "it does not say it is connected");
        try (Git git = Git.open(changeSets.toFile())) {
            assertNull(git.getRepository().getConfig().getString("remote", GitTask.REMOTE_NAME, "url"));
        }
        assertNull(main(), "nothing is sent");

        assertTrue(call(GitTask.OperationMode.CONNECT), "the next one connects");
    }

    @Test
    void aConnectionCancelledAsItSendsSaysWhatTheRemoteHasTaken() throws Exception {
        Cancelling run = run(GitTask.OperationMode.CONNECT, SENDING_FIRST);
        assertFalse(run.result);
        assertEquals(0, connections.get());
        assertEquals(main() == null ? "Connect was cancelled."
                : "Connect was cancelled after the remote had taken the change sets: they are sent.", run.last());
        assertTrue(call(GitTask.OperationMode.CONNECT));
    }

    @Test
    void aSyncCancelledAsItConnectsNeverSaysItSucceeded() throws Exception {
        call(GitTask.OperationMode.CONNECT);
        connections.set(0);
        Cancelling run = run(GitTask.OperationMode.SYNC, TESTING);
        assertFalse(run.result);
        assertEquals("Sync was cancelled.", run.last());
        assertEquals(0, connections.get());
        assertTrue(run.most <= 5, "it stops where it is: " + run.most);
    }

    @Test
    void aPullCancelledBeforeItsPullBringsNothing() throws Exception {
        call(GitTask.OperationMode.CONNECT);
        ObjectId before = head();
        another("theirs.txt");
        Cancelling run = run(GitTask.OperationMode.PULL, PULLING);
        assertFalse(run.result);
        assertEquals("Pull was cancelled.", run.last());
        assertEquals(before, head());
        assertPullable("theirs.txt");
    }

    @Test
    void aPullCancelledAsItChecksOutIsPulledByTheNext() throws Exception {
        call(GitTask.OperationMode.CONNECT);
        another("theirs.txt");
        Cancelling run = run(GitTask.OperationMode.PULL, CHECKING_OUT);
        assertTrue(run.asked, run.messages.toString());
        assertFalse(run.result);
        assertEquals("Pull was cancelled.", run.last());
        assertPullable("theirs.txt");
    }

    @Test
    void aMergeThatIsMadeWhenItIsCancelledIsKept() throws Exception {
        call(GitTask.OperationMode.CONNECT);
        another("theirs.txt");
        commit(changeSets, "mine.txt", false);
        Cancelling run = run(GitTask.OperationMode.PULL, CHECKING_OUT);
        assertTrue(run.asked, run.messages.toString());
        assertFalse(run.result);
        assertEquals("Pull was cancelled.", run.last());
        assertTrue(run.messages.contains("No changeset files found to load."), "what is pulled is loaded: " + run.messages);
        assertTrue(Files.exists(changeSets.resolve("theirs.txt")), "git writes the files of a merge before it can be told to stop");
        assertPullable("theirs.txt");
        assertTrue(Files.exists(changeSets.resolve("mine.txt")));
    }

    /** The repository is at rest, and a pull brings what the cancelled one was to bring. */
    private void assertPullable(String file) throws Exception {
        try (Git git = Git.open(changeSets.toFile())) {
            assertEquals(RepositoryState.SAFE, git.getRepository().getRepositoryState());
            assertTrue(git.pull().setRemoteBranchName("main").call().isSuccessful());
        }
        assertTrue(Files.exists(changeSets.resolve(file)));
    }

    /** Runs an operation that is cancelled when it first gives a message. */
    private Cancelling run(GitTask.OperationMode mode, String at) throws Exception {
        GitTask task = new GitTask(mode, changeSets, connections::incrementAndGet, dao);
        Cancelling run = new Cancelling(task, at);
        task.addListener(run);
        run.result = task.call();
        return run;
    }

    private boolean call(GitTask.OperationMode mode) throws Exception {
        return new GitTask(mode, changeSets, connections::incrementAndGet, dao).call();
    }

    /** Cancels the task when it gives a message, as a person does who reads it. */
    private static final class Cancelling implements TrackingListener<Boolean> {
        private final GitTask task;
        private final String at;
        private final List<String> messages = new ArrayList<>();
        private boolean asked;
        private boolean result;
        private double most;

        private Cancelling(GitTask task, String at) {
            this.task = task;
            this.at = at;
        }

        private String last() {
            return messages.getLast();
        }

        @Override
        public void updateValue(Boolean result) {
        }

        @Override
        public void updateMessage(String message) {
            messages.add(message);
            if (at.equals(message) && !asked) {
                asked = true;
                task.cancel();
            }
        }

        @Override
        public void updateTitle(String title) {
        }

        @Override
        public void updateProgress(double workDone, double max) {
            most = Math.max(most, workDone);
        }
    }

    /** Someone else's commit, at the remote. */
    private void another(String file) throws Exception {
        Path clone = root.resolve("another");
        Git.cloneRepository().setURI("file://" + remote).setDirectory(clone.toFile()).setBranch("main").call().close();
        commit(clone, file, true);
    }

    private static void commit(Path folder, String file, boolean push) throws Exception {
        Files.writeString(folder.resolve(file), file);
        try (Git git = Git.open(folder.toFile())) {
            git.add().addFilepattern(file).call();
            git.commit().setMessage(file).setAuthor("a", "a@example.org").setCommitter("a", "a@example.org").setSign(false).call();
            if (push) {
                git.push().call();
            }
        }
    }

    private ObjectId head() throws Exception {
        try (Git git = Git.open(changeSets.toFile())) {
            return git.getRepository().resolve("HEAD");
        }
    }

    private ObjectId main() throws Exception {
        try (Git git = Git.open(remote.toFile())) {
            Ref ref = git.getRepository().findRef("refs/heads/main");
            return ref == null ? null : ref.getObjectId();
        }
    }
}
