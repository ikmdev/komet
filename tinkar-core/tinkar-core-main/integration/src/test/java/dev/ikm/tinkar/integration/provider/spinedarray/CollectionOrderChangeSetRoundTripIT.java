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

import dev.ikm.tinkar.common.id.IntIdList;
import dev.ikm.tinkar.common.id.IntIdSet;
import dev.ikm.tinkar.common.id.IntIds;
import dev.ikm.tinkar.common.id.PublicId;
import dev.ikm.tinkar.common.id.PublicIds;
import dev.ikm.tinkar.common.service.PrimitiveData;
import dev.ikm.tinkar.common.util.io.FileUtil;
import dev.ikm.tinkar.composer.Composer;
import dev.ikm.tinkar.composer.Session;
import dev.ikm.tinkar.composer.assembler.PatternAssemblerConsumer;
import dev.ikm.tinkar.composer.assembler.SemanticAssemblerConsumer;
import dev.ikm.tinkar.coordinate.Calculators;
import dev.ikm.tinkar.coordinate.stamp.calculator.Latest;
import dev.ikm.tinkar.coordinate.stamp.calculator.StampCalculator;
import dev.ikm.tinkar.entity.EntityCountSummary;
import dev.ikm.tinkar.entity.EntityVersion;
import dev.ikm.tinkar.entity.Field;
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
import java.util.UUID;
import java.util.concurrent.atomic.AtomicLong;
import java.util.stream.Stream;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;


@TestInstance(TestInstance.Lifecycle.PER_METHOD)
@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
@Disabled("Requires running test methods in order but in separate JVMs, which is not supported by Maven / JUnit configurations. \n" +
        "To run these tests use IntelliJ with 'Fork Mode' set to 'method' then add a changeset provider to the integration pom and module-info")
public class CollectionOrderChangeSetRoundTripIT {

    private static final Logger LOG = LoggerFactory.getLogger(CollectionOrderChangeSetRoundTripIT.class);
    private static final File PARENT_DATASTORE_FOLDER = TestConstants.createFilePathInTargetFromClassName.apply(CollectionOrderChangeSetRoundTripIT.class);
    private final File SOURCE_DATASTORE_ROOT_LIST = new File(PARENT_DATASTORE_FOLDER, "intIdList_Source");
    private final File DESTINATION_DATASTORE_ROOT_LIST = new File(PARENT_DATASTORE_FOLDER, "intIdList_Destination");
    private final File SOURCE_DATASTORE_ROOT_SET = new File(PARENT_DATASTORE_FOLDER, "intIdSet_Source");
    private final File DESTINATION_DATASTORE_ROOT_SET = new File(PARENT_DATASTORE_FOLDER, "intIdSet_Destination");



    @AfterEach
    public void afterEach() {
        TestHelper.stopDatabase();
    }

    @Test
    @Order(1)
    @DisplayName("createListChangeSetTest")
    public void createListChangeSetTest() {
        final File datastoreRoot = SOURCE_DATASTORE_ROOT_LIST;
        FileUtil.recursiveDelete(datastoreRoot);
        // Initialize the datastore with starter data
        TestHelper.startDataBase(DataStore.SPINED_ARRAY_STORE, datastoreRoot);
        EntityCountSummary initialLoadSummary = new LoadEntitiesFromProtobufFile(TestConstants.PB_STARTER_DATA_REASONED).compute();
        LOG.info("{} entities loaded from initialLoad: {}\n\n", initialLoadSummary.getTotalCount(), initialLoadSummary);

        EntityProxy.Concept fieldDataType = TinkarTerm.COMPONENT_ID_LIST_FIELD;
        IntIdList fieldVals = getOriginalIntIdList();

        //Given a Semantic with a PublicIdList
        EntityProxy.Pattern patternProxy = EntityProxy.Pattern.make(PublicIds.newRandom());
        EntityProxy.Semantic semanticProxy = getSemanticWithIntIdList();
        Composer composer = new Composer("protobufRoundTrip_PublicIdList");
        Session session = composer.open(State.ACTIVE, TinkarTerm.USER, TinkarTerm.DEVELOPMENT_MODULE, TinkarTerm.DEVELOPMENT_PATH);
        session.compose((PatternAssemblerConsumer)  patternAssembler -> patternAssembler
                .pattern(patternProxy)
                .meaning(TinkarTerm.MEANING)
                .purpose(TinkarTerm.PURPOSE)
                .fieldDefinition(TinkarTerm.MEANING, TinkarTerm.PURPOSE, fieldDataType));
        session.compose((SemanticAssemblerConsumer)  semanticAssembler ->  semanticAssembler
                .semantic(semanticProxy)
                .pattern(patternProxy)
                .reference(TinkarTerm.ROOT_VERTEX)
                .fieldValues(vals -> vals.with(fieldVals)));
        composer.commitSession(session);

        TestHelper.stopDatabase(); // Also saves changeSet(s)

        // Verify ChangeSet was created as expected
        ImmutableList<Path> changeSetFilePaths = filePathsToAdd(datastoreRoot.toPath(), "ike-cs.zip");
        assertEquals(1, changeSetFilePaths.size(), "Setup Failed: Expected one and only one changeSet in initial datastore.");
    }

    @Test
    @Order(2)
    @DisplayName("ingestListChangeSetTest")
    public void ingestListChangeSetTest() {
        final File datastoreRoot = DESTINATION_DATASTORE_ROOT_LIST;
        FileUtil.recursiveDelete(datastoreRoot);
        // Initialize the datastore with starter data
        TestHelper.startDataBase(DataStore.SPINED_ARRAY_STORE, datastoreRoot);
        EntityCountSummary initialLoadSummary = new LoadEntitiesFromProtobufFile(TestConstants.PB_STARTER_DATA_REASONED).compute();
        LOG.info("{} entities loaded from initialLoad: {}\n\n", initialLoadSummary.getTotalCount(), initialLoadSummary);

        AtomicLong preRoundTripEntityCount = new AtomicLong();
        PrimitiveData.get().forEach((ignored, nid) -> preRoundTripEntityCount.incrementAndGet());

        // Load ChangeSet from the source datastore
        ImmutableList<Path> changeSetFilePaths = filePathsToAdd(SOURCE_DATASTORE_ROOT_LIST.toPath(), "ike-cs.zip");
        EntityCountSummary changeSetLoad = new LoadEntitiesFromProtobufFile(changeSetFilePaths.get(0).toFile()).compute();
        LOG.info("{} entities loaded from changeSetFilePath: {}", changeSetLoad.getTotalCount(), changeSetLoad);

        AtomicLong postRoundTripEntityCount = new AtomicLong();
        PrimitiveData.get().forEach((ignored, nid) -> postRoundTripEntityCount.incrementAndGet());

        // Verify all imported entity counts and resultant entity versions
        assertEquals(4, changeSetLoad.getTotalCount(), "Imported count should be 4 - reading a changeSet shows different entities for the Committed Stamp, Pattern Version, Semantic Version, then Uncommitted Stamp.");
        assertEquals(preRoundTripEntityCount.get() + 3, postRoundTripEntityCount.get(), "Post Round Trip Entity Count should be initialCount+3 (1 extra stamp, 1 extra Pattern, and 1 extra Semantic).");

        EntityProxy.Semantic semanticProxy = getSemanticWithIntIdList();
        StampCalculator stampCalc = Calculators.Stamp.DevelopmentLatest();
        assertNotNull(Calculators.Stamp.DevelopmentLatest().latest(semanticProxy.nid()).orElse(null), "Could not find the NewData in the round trip database.");
        EntityVersion synonym = stampCalc.latest(semanticProxy).get();
        assertNotEquals(Long.MAX_VALUE, synonym.stamp().time(), "Expected NewData to be committed after round trip but is still uncommitted.");
        Latest<Field<IntIdList>> transformedLatestField = stampCalc.getFieldForSemanticWithMeaning(semanticProxy, TinkarTerm.MEANING);
        IntIdList transformedIntIdList = transformedLatestField.get().value();

        // Then the PublicIdList will still be in the same order
        IntIdList originalIntIdList = getOriginalIntIdList();
        for (int i=0; i<originalIntIdList.size(); i++) {
            PublicId originalPublicIdAtIndex = PrimitiveData.publicId(originalIntIdList.toArray()[i]);
            PublicId transformedPublicIdAtIndex = PrimitiveData.publicId(transformedIntIdList.toArray()[i]);
            assertEquals(originalPublicIdAtIndex, transformedPublicIdAtIndex,
                    "Transformed ID List does not match original:"+originalPublicIdAtIndex+" ≠ "+transformedPublicIdAtIndex
                            + "\nOriginal: "+originalIntIdList+"\nTransformed: "+transformedIntIdList);
        }
    }

    @Test
    @Order(3)
    @DisplayName("createChangeSetTest")
    public void createSetChangeSetTest() {
        final File datastoreRoot = SOURCE_DATASTORE_ROOT_SET;
        FileUtil.recursiveDelete(datastoreRoot);
        // Initialize the datastore with starter data
        TestHelper.startDataBase(DataStore.SPINED_ARRAY_STORE, datastoreRoot);
        EntityCountSummary initialLoadSummary = new LoadEntitiesFromProtobufFile(TestConstants.PB_STARTER_DATA_REASONED).compute();
        LOG.info("{} entities loaded from initialLoad: {}\n\n", initialLoadSummary.getTotalCount(), initialLoadSummary);

        EntityProxy.Concept fieldDataType = TinkarTerm.COMPONENT_ID_SET_FIELD;
        IntIdSet fieldVals = getOriginalIntIdSet();

        //Given a Semantic with a PublicIdSet
        EntityProxy.Pattern patternProxy = EntityProxy.Pattern.make(PublicIds.newRandom());
        EntityProxy.Semantic semanticProxy = getSemanticWithIntIdSet();
        Composer composer = new Composer("protobufRoundTrip_PublicIdList");
        Session session = composer.open(State.ACTIVE, TinkarTerm.USER, TinkarTerm.DEVELOPMENT_MODULE, TinkarTerm.DEVELOPMENT_PATH);
        session.compose((PatternAssemblerConsumer)  patternAssembler -> patternAssembler
                .pattern(patternProxy)
                .meaning(TinkarTerm.MEANING)
                .purpose(TinkarTerm.PURPOSE)
                .fieldDefinition(TinkarTerm.MEANING, TinkarTerm.PURPOSE, fieldDataType));
        session.compose((SemanticAssemblerConsumer)  semanticAssembler ->  semanticAssembler
                .semantic(semanticProxy)
                .pattern(patternProxy)
                .reference(TinkarTerm.ROOT_VERTEX)
                .fieldValues(vals -> vals.with(fieldVals)));
        composer.commitSession(session);

        TestHelper.stopDatabase(); // Also saves changeSet(s)

        // Verify ChangeSet was created as expected
        ImmutableList<Path> changeSetFilePaths = filePathsToAdd(datastoreRoot.toPath(), "ike-cs.zip");
        assertEquals(1, changeSetFilePaths.size(), "Setup Failed: Expected one and only one changeSet in initial datastore.");
    }

    @Test
    @Order(4)
    @DisplayName("ingestSetChangeSetTest")
    public void ingestSetChangeSetTest() {
        final File datastoreRoot = DESTINATION_DATASTORE_ROOT_SET;
        FileUtil.recursiveDelete(datastoreRoot);
        // Initialize the datastore with starter data
        TestHelper.startDataBase(DataStore.SPINED_ARRAY_STORE, datastoreRoot);
        EntityCountSummary initialLoadSummary = new LoadEntitiesFromProtobufFile(TestConstants.PB_STARTER_DATA_REASONED).compute();
        LOG.info("{} entities loaded from initialLoad: {}\n\n", initialLoadSummary.getTotalCount(), initialLoadSummary);

        AtomicLong preRoundTripEntityCount = new AtomicLong();
        PrimitiveData.get().forEach((ignored, nid) -> preRoundTripEntityCount.incrementAndGet());

        // Load ChangeSet from the source datastore
        ImmutableList<Path> changeSetFilePaths = filePathsToAdd(SOURCE_DATASTORE_ROOT_SET.toPath(), "ike-cs.zip");
        EntityCountSummary changeSetLoad = new LoadEntitiesFromProtobufFile(changeSetFilePaths.get(0).toFile()).compute();
        LOG.info("{} entities loaded from changeSetFilePath: {}", changeSetLoad.getTotalCount(), changeSetLoad);

        AtomicLong postRoundTripEntityCount = new AtomicLong();
        PrimitiveData.get().forEach((ignored, nid) -> postRoundTripEntityCount.incrementAndGet());

        // Verify all imported entity counts and resultant entity versions
        assertEquals(4, changeSetLoad.getTotalCount(), "Imported count should be 4 - reading a changeSet shows different entities for the Committed Stamp, Pattern Version, Semantic Version, then Uncommitted Stamp.");
        assertEquals(preRoundTripEntityCount.get() + 3, postRoundTripEntityCount.get(), "Post Round Trip Entity Count should be initialCount+3 (1 extra stamp, 1 extra Pattern, and 1 extra Semantic).");

        EntityProxy.Semantic semanticProxy = getSemanticWithIntIdSet();
        StampCalculator stampCalc = Calculators.Stamp.DevelopmentLatest();
        assertNotNull(Calculators.Stamp.DevelopmentLatest().latest(semanticProxy.nid()).orElse(null), "Could not find the NewData in the round trip database.");
        EntityVersion synonym = stampCalc.latest(semanticProxy).get();
        assertNotEquals(Long.MAX_VALUE, synonym.stamp().time(), "Expected NewData to be committed after round trip but is still uncommitted.");
        Latest<Field<IntIdSet>> transformedLatestField = stampCalc.getFieldForSemanticWithMeaning(semanticProxy, TinkarTerm.MEANING);
        IntIdSet transformedIntIdSet = transformedLatestField.get().value();

        // Then the PublicIdSet will still be in the same order
        IntIdSet originalIntIdSet = getOriginalIntIdSet();
        for (int i=0; i<originalIntIdSet.size(); i++) {
            PublicId originalPublicIdAtIndex = PrimitiveData.publicId(originalIntIdSet.toArray()[i]);
            PublicId transformedPublicIdAtIndex = PrimitiveData.publicId(transformedIntIdSet.toArray()[i]);
            assertEquals(originalPublicIdAtIndex, transformedPublicIdAtIndex,
                    "Transformed ID List does not match original:"+originalPublicIdAtIndex+" ≠ "+transformedPublicIdAtIndex
                    +"\nOriginal: "+originalIntIdSet+"\nTransformed: "+transformedIntIdSet);
        }
    }

    private IntIdList getOriginalIntIdList() {
        return IntIds.list.of(
                TinkarTerm.MASTER_PATH.nid(),
                TinkarTerm.DEVELOPMENT_PATH.nid(),
                TinkarTerm.SANDBOX_PATH.nid(),
                TinkarTerm.PRIMORDIAL_PATH.nid()
        );
    }

    private EntityProxy.Semantic getSemanticWithIntIdList() {
        return EntityProxy.Semantic.make(PublicIds.of(UUID.nameUUIDFromBytes("SemanticWithIntIdList".getBytes())));
    }

    private IntIdSet getOriginalIntIdSet() {
        return IntIds.set.of(
                TinkarTerm.MASTER_PATH.nid(),
                TinkarTerm.DEVELOPMENT_PATH.nid(),
                TinkarTerm.SANDBOX_PATH.nid(),
                TinkarTerm.PRIMORDIAL_PATH.nid(),
                TinkarTerm.PATH.nid()
        );
    }

    private EntityProxy.Semantic getSemanticWithIntIdSet() {
        return EntityProxy.Semantic.make(PublicIds.of(UUID.nameUUIDFromBytes("SemanticWithIntIdSet".getBytes())));
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
