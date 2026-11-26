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
import dev.ikm.tinkar.common.alert.AlertLog;
import dev.ikm.tinkar.common.alert.AlertType;
import dev.ikm.tinkar.common.service.CachingService;
import dev.ikm.tinkar.common.service.PrimitiveData;
import dev.ikm.tinkar.common.service.ServiceKeys;
import dev.ikm.tinkar.common.service.ServiceProperties;
import dev.ikm.tinkar.common.service.TrackingListener;
import dev.ikm.tinkar.entity.load.LoadEntitiesFromProtobufFile;
import dev.ikm.tinkar.entity.scope.SharedFolder;
import dev.ikm.tinkar.schema.Field;
import dev.ikm.tinkar.schema.PublicId;
import dev.ikm.tinkar.schema.SemanticChronology;
import dev.ikm.tinkar.schema.SemanticVersion;
import dev.ikm.tinkar.schema.StampChronology;
import dev.ikm.tinkar.schema.StampVersion;
import dev.ikm.tinkar.schema.TinkarMsg;
import dev.ikm.tinkar.terms.EntityFacade;
import dev.ikm.tinkar.terms.KernelConcept;
import dev.ikm.tinkar.terms.KernelPattern;
import org.eclipse.jgit.api.Git;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import java.util.jar.Attributes;
import java.util.jar.Manifest;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * A Pull or a Sync that brings a change set the knowledge base refuses completes, and says so: its last message
 * names the refused file, and an advisory in Warnings and Errors says why it was refused. It used to end
 * "completed successfully." with nothing in Warnings and Errors, and only a passing message said a file was refused.
 *
 * <p>The knowledge base is the starter set in a memory store. Another person's clone pushes the change sets; each
 * describes a concept no file carries, so the store refuses it. A Sync is followed to its push, where it is
 * cancelled: this module's tests have no change set writer for the push to save.
 */
class GitTaskRefusedLoadTest {

    private static final File STARTER_DATA = new File("target/data/ike-starter-set-reasoned-pb.zip");
    private static final String TEST_NODE = "dev/ikm/komet/kview/test/git-task-refused-load";
    private static final Path KNOWLEDGE = Path.of("src", "main", "resources");
    private static final String SAVING = "Saving current change set...";

    @TempDir
    static Path root;

    private static KometPreferences node;
    private static GitHubPreferencesDao dao;
    private static Path knowledgeBase;
    private static Path changeSets;
    private static Path remote;

    @BeforeAll
    static void connectAKnowledgeBase() throws Exception {
        assertTrue(STARTER_DATA.exists(), "Starter data must be at " + STARTER_DATA.getAbsolutePath());
        knowledgeBase = Files.createDirectories(root.resolve("kb"));
        CachingService.clearAll();
        ServiceProperties.set(ServiceKeys.DATA_STORE_ROOT, knowledgeBase.toFile());
        PrimitiveData.selectControllerByName("Load Memory-Only 64-bit Store");
        PrimitiveData.start();
        assertTrue(new LoadEntitiesFromProtobufFile(STARTER_DATA).compute().getTotalCount() > 0);

        node = PreferencesServiceFactory.provider().getUserPreferences().node(TEST_NODE);
        dao = GitHubPreferencesDao.backedBy(() -> node);
        changeSets = Files.createDirectories(knowledgeBase.resolve("changeSets"));
        remote = root.resolve("remote.git");
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
    void aPullWhoseChangeSetIsRefusedCompletesAndNamesItLast() throws Exception {
        UUID nobodyCarries = uuid("pull nobody carries");
        String name = "Another 20261125T120000PST pull ike-cs.zip";
        pushFromAnotherClone(name, nobodyCarries);

        int before = AlertLog.root().entries().size();
        Recording pull = run(GitTask.OperationMode.PULL, null);

        assertTrue(pull.result, "a refusal does not fail the Pull: " + pull.messages);
        assertEquals(100, pull.most, "the Pull completed");
        assertEquals("Pulling completed; 1 change set refused: " + name + ".", pull.last(), pull.messages.toString());
        AlertLog.Entry advisory = advisory(before, "Exchange", name);
        assertTrue(advisory.description().contains(nobodyCarries.toString()),
                "the advisory says why: the component the file names that the store lacks: " + advisory.description());
        assertTrue(advisory.description().contains("tried again when the knowledge base next opens"), advisory.description());
    }

    @Test
    void aSyncRecordsItsRefusalBeforeItsPush() throws Exception {
        UUID nobodyCarries = uuid("sync nobody carries");
        String name = "Another 20261125T130000PST sync ike-cs.zip";
        pushFromAnotherClone(name, nobodyCarries);

        int before = AlertLog.root().entries().size();
        Recording sync = run(GitTask.OperationMode.SYNC, SAVING);

        assertFalse(sync.result);
        assertEquals("Sync was cancelled.", sync.last(), sync.messages.toString());
        AlertLog.Entry advisory = advisory(before, "Exchange", name);
        assertTrue(advisory.description().contains(nobodyCarries.toString()), advisory.description());
    }

    @Test
    void aRefusedSharedChangeSetIsNamedLastAndAdvisedToo() throws Exception {
        assertTrue(SharedFolder.declared(), "the starter set has the Shared module, so shared change sets load");
        UUID nobodyCarries = uuid("shared nobody carries");
        String name = "Another shared document ike-cs.zip";
        Path shared = Files.createDirectories(SharedFolder.of(knowledgeBase));
        Path file = write(shared.resolve(name), nobodyCarries);
        try {
            int before = AlertLog.root().entries().size();
            Recording pull = run(GitTask.OperationMode.PULL, null);

            assertTrue(pull.result, pull.messages.toString());
            assertEquals("Pulling completed; 1 change set refused: " + name + ".", pull.last(), pull.messages.toString());
            AlertLog.Entry advisory = advisory(before, "Shared documents", name);
            assertTrue(advisory.description().contains(nobodyCarries.toString()), advisory.description());
        } finally {
            Files.delete(file);
        }
    }

    @Test
    void aPullWithNothingRefusedStillSaysItSucceeded() throws Exception {
        Recording pull = run(GitTask.OperationMode.PULL, null);
        assertTrue(pull.result, pull.messages.toString());
        assertEquals("Pulling completed successfully.", pull.last());
    }

    /** The advisory recorded since {@code before} with this title that names the file. */
    private static AlertLog.Entry advisory(int before, String title, String file) {
        List<AlertLog.Entry> entries = AlertLog.root().entries();
        List<AlertLog.Entry> since = entries.subList(Math.min(before, entries.size()), entries.size());
        List<AlertLog.Entry> naming = since.stream()
                .filter(entry -> entry.title().equals(title) && entry.description().contains(file))
                .toList();
        assertEquals(1, naming.size(), "one advisory \"" + title + "\" names " + file + " in Warnings and Errors: " + since);
        AlertLog.Entry advisory = naming.getFirst();
        assertEquals(AlertType.WARNING, advisory.type(), "an advisory is a warning that opens no dialog");
        return advisory;
    }

    // ── Another person's change set, which this store refuses ─────────────────────────────────

    private static UUID uuid(String name) {
        return UUID.nameUUIDFromBytes(("git-task-refused-load " + name).getBytes());
    }

    /** Commits the file to the remote's main from a clone of its own, as another person's Sync does. */
    private static void pushFromAnotherClone(String name, UUID nobodyCarries) throws Exception {
        Path clone = root.resolve("another-" + UUID.randomUUID());
        try (Git git = Git.cloneRepository().setURI(remote.toUri().toString()).setDirectory(clone.toFile())
                .setBranch("main").call()) {
            Path folder = Files.createDirectories(clone.resolve(KNOWLEDGE));
            write(folder.resolve(name), nobodyCarries);
            git.add().addFilepattern(KNOWLEDGE.resolve(name).toString().replace(File.separatorChar, '/')).call();
            git.commit().setMessage("Another person's change set").setAuthor("another", "another@example.org")
                    .setCommitter("another", "another@example.org").setSign(false).call();
            git.push().setRemote("origin").call();
        }
    }

    private static PublicId id(UUID uuid) {
        return PublicId.newBuilder().addUuids(uuid.toString()).build();
    }

    private static PublicId id(EntityFacade facade) {
        return id(facade.publicId().asUuidArray()[0]);
    }

    /** A change set of one description of a concept no file carries, under a stamp of its own. */
    private static Path write(Path file, UUID nobodyCarries) throws IOException {
        UUID stamp = uuid(file.getFileName() + " stamp");
        TinkarMsg stampMsg = TinkarMsg.newBuilder().setStampChronology(StampChronology.newBuilder()
                .setPublicId(id(stamp))
                .setFirstStampVersion(StampVersion.newBuilder()
                        .setStatusPublicId(id(KernelConcept.ACTIVE_STATE))
                        .setAuthorPublicId(id(KernelConcept.USER))
                        .setModulePublicId(id(KernelConcept.PRIMORDIAL_MODULE))
                        .setPathPublicId(id(KernelConcept.DEVELOPMENT_PATH))
                        .setTime(1_764_000_000_000L))).build();
        TinkarMsg description = TinkarMsg.newBuilder().setSemanticChronology(SemanticChronology.newBuilder()
                .setPublicId(id(uuid(file.getFileName() + " description")))
                .setReferencedComponentPublicId(id(nobodyCarries))
                .setPatternForSemanticPublicId(id(KernelPattern.DESCRIPTION_PATTERN))
                .addSemanticVersions(SemanticVersion.newBuilder()
                        .setStampChronologyPublicId(id(stamp))
                        .addFields(Field.newBuilder().setPublicId(id(KernelConcept.ENGLISH_LANGUAGE)))
                        .addFields(Field.newBuilder().setStringValue("A description of a concept no file carries"))
                        .addFields(Field.newBuilder().setPublicId(id(KernelConcept.DESCRIPTION_NOT_CASE_SENSITIVE)))
                        .addFields(Field.newBuilder().setPublicId(id(KernelConcept.REGULAR_NAME_DESCRIPTION_TYPE))))).build();
        try (ZipOutputStream out = new ZipOutputStream(Files.newOutputStream(file))) {
            out.putNextEntry(new ZipEntry("Entities"));
            stampMsg.writeDelimitedTo(out);
            description.writeDelimitedTo(out);
            out.closeEntry();
            Manifest manifest = new Manifest();
            manifest.getMainAttributes().put(Attributes.Name.MANIFEST_VERSION, "1.0");
            manifest.getMainAttributes().putValue("Total-Count", "2");
            out.putNextEntry(new ZipEntry("META-INF/MANIFEST.MF"));
            manifest.write(out);
            out.closeEntry();
        }
        return file;
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

    /** Each message the task gave, and the furthest its bar went, of 100. */
    private static final class Recording implements TrackingListener<Boolean> {
        private final GitTask task;
        private final String cancelAt;
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

        @Override
        public void updateValue(Boolean result) {
        }

        @Override
        public void updateMessage(String message) {
            if (message == null || message.isEmpty()) {
                return;
            }
            messages.add(message);
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
