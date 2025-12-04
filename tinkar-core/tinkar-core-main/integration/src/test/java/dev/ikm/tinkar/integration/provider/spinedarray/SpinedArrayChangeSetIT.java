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
package dev.ikm.tinkar.integration.provider.spinedarray;

import dev.ikm.tinkar.common.service.PluggableService;
import dev.ikm.tinkar.common.service.PrimitiveData;
import dev.ikm.tinkar.common.service.SaveState;
import dev.ikm.tinkar.common.service.ServiceKeys;
import dev.ikm.tinkar.common.service.ServiceProperties;
import dev.ikm.tinkar.common.util.io.FileUtil;
import dev.ikm.tinkar.composer.Composer;
import dev.ikm.tinkar.composer.Session;
import dev.ikm.tinkar.composer.template.StatedAxiom;
import dev.ikm.tinkar.entity.ChangeSetWriterService;
import dev.ikm.tinkar.entity.EntityCountSummary;
import dev.ikm.tinkar.entity.EntityService;
import dev.ikm.tinkar.entity.SemanticEntity;
import dev.ikm.tinkar.entity.SemanticEntityVersion;
import dev.ikm.tinkar.entity.load.LoadEntitiesFromProtobufFile;
import dev.ikm.tinkar.integration.TestConstants;
import dev.ikm.tinkar.integration.helper.DataStore;
import dev.ikm.tinkar.integration.helper.TestHelper;
import dev.ikm.tinkar.terms.State;
import dev.ikm.tinkar.terms.TinkarTerm;
import org.eclipse.collections.api.factory.Lists;
import org.eclipse.collections.api.list.ImmutableList;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestInstance;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.io.IOException;
import java.nio.file.FileSystem;
import java.nio.file.FileSystems;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Optional;
import java.util.concurrent.ExecutionException;
import java.util.stream.Stream;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

@TestInstance(TestInstance.Lifecycle.PER_CLASS)
class SpinedArrayChangeSetIT {
    private static final Logger LOG = LoggerFactory.getLogger(SpinedArrayChangeSetIT.class);
    private static final File DATASTORE_ROOT = TestConstants.createFilePathInTargetFromClassName.apply(
            SpinedArrayChangeSetIT.class);

    @BeforeAll
    static void beforeAll() {
        FileUtil.recursiveDelete(DATASTORE_ROOT);
        TestHelper.startDataBase(DataStore.SPINED_ARRAY_STORE, DATASTORE_ROOT);
        File file = TestConstants.PB_EXAMPLE_DATA_REASONED;
        LoadEntitiesFromProtobufFile loadProto = new LoadEntitiesFromProtobufFile(file);
        EntityCountSummary count = loadProto.compute();
        LOG.info(count + " entitles loaded from file: " + loadProto.summarize() + "\n\n");
    }

    @AfterAll
    static void afterAll() {
        TestHelper.stopDatabase();
    }

    @Test
    public void changesetWriterRaceCondition() throws InterruptedException, ExecutionException {
        // Get ChangeSetFolder location
        Optional<File> optionalDataStoreRoot = ServiceProperties.get(ServiceKeys.DATA_STORE_ROOT);
        assertFalse(optionalDataStoreRoot.isEmpty());
        final File changeSetFolder = new File(optionalDataStoreRoot.get(), "changeSets");
        assertTrue(changeSetFolder.exists() || changeSetFolder.mkdirs());

        // Find current stated logical definition Semantic to reference it when creating a new Version
        int[] statedDefNid = PrimitiveData.get().semanticNidsForComponentOfPattern(TinkarTerm.STATUS_VALUE.nid(), TinkarTerm.EL_PLUS_PLUS_STATED_AXIOMS_PATTERN.nid());
        assertEquals(1, statedDefNid.length);
        SemanticEntity<SemanticEntityVersion> statedDefSemantic = EntityService.get().getEntityFast(statedDefNid[0]);

        // Create a new Semantic Version and commit to database
        Composer composer = new Composer("changesetWriterRaceCondition");
        Session session = composer.open(State.ACTIVE, TinkarTerm.USER, TinkarTerm.DEVELOPMENT_MODULE, TinkarTerm.DEVELOPMENT_PATH);
        session.compose(new StatedAxiom()
                .semantic(statedDefSemantic.toProxy())
                .isA(TinkarTerm.OBJECT),
                statedDefSemantic.referencedComponent().toProxy());
        composer.commitSession(session);

        // Save ChangeSetWriter state
        ChangeSetWriterService changeSetWriterService = PluggableService.first(ChangeSetWriterService.class);
        assertNotEquals(null, changeSetWriterService);
        if (changeSetWriterService instanceof SaveState savableChangeSetWriterService) {
            savableChangeSetWriterService.save().get();
        }

        // Waiting until the changeset writer closes will cause this test to pass
        //Thread.sleep(10000);

        // Get files to add
        ImmutableList<String> filesToAdd = filesToAdd(changeSetFolder.toPath(), "ike-cs.zip");
        assertEquals(1, filesToAdd.size());
    }

    /**
     * Identifies files in a directory matching the specified pattern that are valid changesets.
     *
     * @param directory The directory to search for files
     * @param pattern   The file extension pattern to match
     * @return An immutable list of relative paths to valid changeset files
     */
    ImmutableList<String> filesToAdd(Path directory, String pattern) {
        try (Stream<Path> filesStream = Files.walk(directory)) {
            return Lists.immutable.ofAll(filesStream
                    .filter(Files::isRegularFile)
                    .filter(path -> path.toString().endsWith(pattern))
                    .filter(this::isValidChangeset)
                    .map(path -> directory.relativize(path).toString())
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
}
