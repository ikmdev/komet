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

import dev.ikm.tinkar.common.id.PublicId;
import dev.ikm.tinkar.common.service.PrimitiveData;
import dev.ikm.tinkar.common.util.io.FileUtil;
import dev.ikm.tinkar.component.Concept;
import dev.ikm.tinkar.composer.Composer;
import dev.ikm.tinkar.composer.Session;
import dev.ikm.tinkar.composer.template.Synonym;
import dev.ikm.tinkar.coordinate.Calculators;
import dev.ikm.tinkar.coordinate.stamp.calculator.StampCalculator;
import dev.ikm.tinkar.entity.EntityCountSummary;
import dev.ikm.tinkar.entity.EntityService;
import dev.ikm.tinkar.entity.EntityVersion;
import dev.ikm.tinkar.entity.load.LoadEntitiesFromProtobufFile;
import dev.ikm.tinkar.integration.TestConstants;
import dev.ikm.tinkar.integration.helper.DataStore;
import dev.ikm.tinkar.integration.helper.TestHelper;
import dev.ikm.tinkar.terms.EntityProxy;
import dev.ikm.tinkar.terms.State;
import dev.ikm.tinkar.terms.TinkarTerm;
import org.eclipse.collections.api.factory.Lists;
import org.eclipse.collections.api.list.ImmutableList;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.MethodOrderer;
import org.junit.jupiter.api.Order;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestInstance;
import org.junit.jupiter.api.TestMethodOrder;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.io.IOException;
import java.nio.file.FileSystem;
import java.nio.file.FileSystems;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.concurrent.atomic.AtomicLong;
import java.util.concurrent.atomic.AtomicReference;
import java.util.stream.Stream;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;


@TestInstance(TestInstance.Lifecycle.PER_METHOD)
@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
@Disabled("Requires running test methods in order but in separate JVMs, which is not supported by Maven / JUnit configurations. \n" +
        "To run these tests use IntelliJ with 'Fork Mode' set to 'method'")
public class ChangeSetRoundTripIT {

    private static final Logger LOG = LoggerFactory.getLogger(ChangeSetRoundTripIT.class);
    private static final File PARENT_DATASTORE_FOLDER = TestConstants.createFilePathInTargetFromClassName.apply(ChangeSetRoundTripIT.class);
    private final File SOURCE_DATASTORE_ROOT = new File(PARENT_DATASTORE_FOLDER, "createChangeSetTest");
    private final File DESTINATION_DATASTORE_ROOT = new File(PARENT_DATASTORE_FOLDER, "ingestChangeSetTest");

    @AfterEach
    public void afterEach() {
        TestHelper.stopDatabase();
    }

    @Test
    @Order(1)
    @DisplayName("createChangeSetTest")
    public void createChangeSetTest() {
        final File datastoreRoot = SOURCE_DATASTORE_ROOT;
        FileUtil.recursiveDelete(datastoreRoot);
        // Initialize the datastore with starter data
        TestHelper.startDataBase(DataStore.SPINED_ARRAY_STORE, datastoreRoot);
        EntityCountSummary initialLoadSummary = new LoadEntitiesFromProtobufFile(TestConstants.PB_STARTER_DATA_REASONED).compute();
        LOG.info("{} entities loaded from initialLoad: {}\n\n", initialLoadSummary.getTotalCount(), initialLoadSummary);

        // Create and commit a new synonym version
        Composer composer = new Composer("createChangeSetTest Composer");
        Session session = composer.open(State.ACTIVE, TinkarTerm.USER, TinkarTerm.DEVELOPMENT_MODULE, TinkarTerm.DEVELOPMENT_PATH);
        session.compose(new Synonym()
                        .semantic(getAuthorSynonym())
                        .language(TinkarTerm.ENGLISH_LANGUAGE)
                        .caseSignificance(TinkarTerm.DESCRIPTION_NOT_CASE_SENSITIVE)
                        .text("Author: createChangeSetTest"),
                TinkarTerm.USER);
        composer.commitSession(session);

        TestHelper.stopDatabase(); // Also saves changeSet(s)

        // Verify ChangeSet was created as expected
        ImmutableList<Path> changeSetFilePaths = filePathsToAdd(SOURCE_DATASTORE_ROOT.toPath(), "ike-cs.zip");
        assertEquals(1, changeSetFilePaths.size(), "Setup Failed: Expected one and only one changeSet in initial datastore.");
    }

    @Test
    @Order(2)
    @DisplayName("ingestChangeSetTest")
    public void ingestChangeSetTest() {
        final File datastoreRoot = DESTINATION_DATASTORE_ROOT;
        FileUtil.recursiveDelete(datastoreRoot);
        // Initialize the datastore with starter data
        TestHelper.startDataBase(DataStore.SPINED_ARRAY_STORE, datastoreRoot);
        EntityCountSummary initialLoadSummary = new LoadEntitiesFromProtobufFile(TestConstants.PB_STARTER_DATA_REASONED).compute();
        LOG.info("{} entities loaded from initialLoad: {}\n\n", initialLoadSummary.getTotalCount(), initialLoadSummary);

        AtomicLong preRoundTripEntityCount = new AtomicLong();
        PrimitiveData.get().forEach((ignored, nid) -> preRoundTripEntityCount.incrementAndGet());

        // Load ChangeSet from the source datastore
        ImmutableList<Path> changeSetFilePaths = filePathsToAdd(SOURCE_DATASTORE_ROOT.toPath(), "ike-cs.zip");
        EntityCountSummary changeSetLoad = new LoadEntitiesFromProtobufFile(changeSetFilePaths.get(0).toFile()).compute();
        LOG.info("{} entities loaded from changeSetFilePath: {}", changeSetLoad.getTotalCount(), changeSetLoad);

        AtomicLong postRoundTripEntityCount = new AtomicLong();
        PrimitiveData.get().forEach((ignored, nid) -> postRoundTripEntityCount.incrementAndGet());

        // Verify all imported entity counts and resultant entity versions
        assertEquals(3, changeSetLoad.getTotalCount(), "Imported count should be 3 - reading a changeSet shows different entities for the Committed Stamp, Semantic Version, then Uncommitted Stamp.");
        assertEquals(preRoundTripEntityCount.get() + 1, postRoundTripEntityCount.get(), "Post Round Trip Entity Count should be initialCount+1 (1 extra stamp).");

        EntityProxy.Semantic synonymProxy = getAuthorSynonym();
        StampCalculator stampCalc = Calculators.Stamp.DevelopmentLatest();
        assertNotNull(Calculators.Stamp.DevelopmentLatest().latest(synonymProxy.nid()).orElse(null), "Could not find the NewData in the round trip database.");
        EntityVersion synonym = stampCalc.latest(synonymProxy).get();
        assertNotEquals(Long.MAX_VALUE, synonym.stamp().time(), "Expected NewData to be committed after round trip but is still uncommitted.");
    }

    private EntityProxy.Semantic getAuthorSynonym() {
        // Get Synonym PublicId for Author Concept (i.e., TinkarTerm.USER)
        AtomicReference<EntityProxy.Semantic> synonymEntityProxy = new AtomicReference<>();
        StampCalculator stampCalc = Calculators.Stamp.DevelopmentLatest();
        int idxDescriptionType = stampCalc.getIndexForMeaning(TinkarTerm.DESCRIPTION_PATTERN.nid(), TinkarTerm.DESCRIPTION_TYPE.nid())
                .orElseThrow(() -> new RuntimeException("Could not get description type index from description pattern."));
        EntityService.get().forEachSemanticForComponentOfPattern(TinkarTerm.USER.nid(), TinkarTerm.DESCRIPTION_PATTERN.nid(), descSemantic -> {
            Object descType = descSemantic.versions().getAny().fieldValues().get(idxDescriptionType);
            if (descType instanceof Concept descTypeConcept && PublicId.equals(descTypeConcept.publicId(), TinkarTerm.REGULAR_NAME_DESCRIPTION_TYPE)) {
                synonymEntityProxy.set(descSemantic.toProxy());
            }
        });
        return synonymEntityProxy.get();
    }

    /**
     * Identifies files in a directory matching the specified pattern that are valid changesets.
     *
     * @param directory The directory to search for files
     * @param pattern   The file extension pattern to match
     * @return An immutable list of relative paths to valid changeset files
     */
    ImmutableList<Path> filePathsToAdd(Path directory, String pattern) {
        try (Stream<Path> filesStream = Files.walk(directory)) {
            return Lists.immutable.ofAll(filesStream
                    .filter(Files::isRegularFile)
                    .filter(path -> path.toString().endsWith(pattern))
                    .filter(this::isValidChangeset)
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
