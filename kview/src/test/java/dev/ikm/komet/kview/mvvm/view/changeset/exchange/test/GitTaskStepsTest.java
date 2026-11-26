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
import dev.ikm.tinkar.common.service.CachingService;
import dev.ikm.tinkar.common.service.PrimitiveData;
import dev.ikm.tinkar.common.service.ServiceKeys;
import dev.ikm.tinkar.common.service.ServiceProperties;
import dev.ikm.tinkar.common.service.TrackingListener;
import dev.ikm.tinkar.entity.load.LoadEntitiesFromProtobufFile;
import org.eclipse.jgit.api.Git;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import java.io.File;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * A Pull or a Sync says only what it did, and its bar moves with that work. Neither runs a reasoner: a step that
 * said "Reasoning process completed successfully." ran none, and took half a Pull's bar and almost a third of a
 * Sync's with it.
 *
 * <p>The knowledge base is the starter set in a memory store, whose root holds the change sets' repository, so the
 * shared documents are received as in Komet. A Sync is followed to its push, where it is cancelled: this module's
 * tests have no change set writer for the push to save.
 */
class GitTaskStepsTest {

    private static final File STARTER_DATA = new File("target/data/ike-starter-set-reasoned-pb.zip");
    private static final String TEST_NODE = "dev/ikm/komet/kview/test/git-task-steps";
    private static final String SHARED = "Bringing the shared documents...";
    private static final String SAVING = "Saving current change set...";

    @TempDir
    static Path root;

    private static KometPreferences node;
    private static GitHubPreferencesDao dao;
    private static Path changeSets;

    @BeforeAll
    static void connectAKnowledgeBase() throws Exception {
        assertTrue(STARTER_DATA.exists(), "Starter data must be at " + STARTER_DATA.getAbsolutePath());
        Path knowledgeBase = Files.createDirectories(root.resolve("kb"));
        CachingService.clearAll();
        ServiceProperties.set(ServiceKeys.DATA_STORE_ROOT, knowledgeBase.toFile());
        PrimitiveData.selectControllerByName("Load Memory-Only 64-bit Store");
        PrimitiveData.start();
        assertTrue(new LoadEntitiesFromProtobufFile(STARTER_DATA).compute().getTotalCount() > 0);

        node = PreferencesServiceFactory.provider().getUserPreferences().node(TEST_NODE);
        dao = GitHubPreferencesDao.backedBy(() -> node);
        changeSets = Files.createDirectories(knowledgeBase.resolve("changeSets"));
        Path remote = root.resolve("remote.git");
        Git.init().setBare(true).setDirectory(remote.toFile()).setInitialBranch("main").call().close();
        dao.save("file://" + remote, "author@example.org", "author", "Not-a-real-token-42".toCharArray());
        assertTrue(run(GitTask.OperationMode.CONNECT, null).result);
    }

    @AfterAll
    static void stopStore() throws Exception {
        dao.forgetPassword();
        node.removeNode();
        PrimitiveData.stop();
    }

    @Test
    void aPullSaysNothingOfAReasoner() throws Exception {
        Recording pull = run(GitTask.OperationMode.PULL, null);
        assertTrue(pull.result, pull.messages.toString());
        assertEquals("Pulling completed successfully.", pull.last());
        assertNoReasoning(pull);
    }

    @Test
    void afterItsLoadAPullHasOnlyTheSharedDocumentsLeft() throws Exception {
        Recording pull = run(GitTask.OperationMode.PULL, null);
        assertTrue(pull.result, pull.messages.toString());
        assertEquals(100, pull.most);
        double loaded = pull.progressAt(SHARED);
        assertTrue(pull.most - loaded <= 5, "the bar stands at " + loaded + " when only the shared documents are left");
    }

    @Test
    void aSyncSaysNothingOfAReasonerBeforeItsPush() throws Exception {
        Recording sync = run(GitTask.OperationMode.SYNC, SAVING);
        assertStoppedAtItsPush(sync);
        assertNoReasoning(sync);
    }

    @Test
    void aSyncGoesFromItsLoadToItsPushWithOnlyTheSharedDocumentsBetween() throws Exception {
        Recording sync = run(GitTask.OperationMode.SYNC, SAVING);
        assertStoppedAtItsPush(sync);
        double loaded = sync.progressAt(SHARED);
        double pushing = sync.progressAt(SAVING);
        assertTrue(pushing - loaded <= 5, "the bar goes from " + loaded + " to " + pushing + " between the load and the push");
    }

    private static void assertStoppedAtItsPush(Recording sync) {
        assertFalse(sync.result);
        assertEquals("Sync was cancelled.", sync.last(), sync.messages.toString());
        assertTrue(sync.messages.contains(SHARED), sync.messages.toString());
    }

    private static void assertNoReasoning(Recording run) {
        List<String> reasoning = run.messages.stream()
                .filter(message -> message.toLowerCase(Locale.ROOT).contains("reason"))
                .toList();
        assertEquals(List.of(), reasoning, "no reasoner is run, so none is spoken of");
    }

    /**
     * Runs an operation to its end, or until it gives a message.
     *
     * @param cancelAt the message it is cancelled at, as a person cancels who reads it; null for none
     */
    private static Recording run(GitTask.OperationMode mode, String cancelAt) throws Exception {
        GitTask task = new GitTask(mode, changeSets, () -> { }, dao);
        Recording recording = new Recording(task, cancelAt);
        task.addListener(recording);
        recording.result = task.call();
        return recording;
    }

    /** Each message the task gave, with where its bar stood when it gave it, of 100. */
    private static final class Recording implements TrackingListener<Boolean> {
        private final GitTask task;
        private final String cancelAt;
        private final Map<String, Double> progressAt = new LinkedHashMap<>();
        private final List<String> messages = new ArrayList<>();
        private double most;
        private boolean result;

        private Recording(GitTask task, String cancelAt) {
            this.task = task;
            this.cancelAt = cancelAt;
        }

        private String last() {
            return messages.getLast();
        }

        private double progressAt(String message) {
            assertTrue(progressAt.containsKey(message), "no \"" + message + "\" in " + messages);
            return progressAt.get(message);
        }

        @Override
        public void updateValue(Boolean result) {
        }

        @Override
        public void updateMessage(String message) {
            if (message == null || message.isEmpty()) {
                return;
            }
            messages.add(message);
            progressAt.putIfAbsent(message, most);
            if (message.equals(cancelAt)) {
                task.cancel();
            }
        }

        @Override
        public void updateTitle(String title) {
        }

        @Override
        public void updateProgress(double workDone, double max) {
            if (max > 0) {
                most = Math.max(most, workDone * 100 / max);
            }
        }
    }
}
