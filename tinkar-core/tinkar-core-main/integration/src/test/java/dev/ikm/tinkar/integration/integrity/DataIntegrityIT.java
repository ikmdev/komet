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
package dev.ikm.tinkar.integration.integrity;

import dev.ikm.tinkar.common.id.IntIdList;
import dev.ikm.tinkar.common.id.IntIdSet;
import dev.ikm.tinkar.common.id.IntIds;
import dev.ikm.tinkar.common.id.PublicId;
import dev.ikm.tinkar.common.service.PrimitiveData;
import dev.ikm.tinkar.common.service.TinkExecutor;
import dev.ikm.tinkar.common.util.io.FileUtil;
import dev.ikm.tinkar.entity.*;
import dev.ikm.tinkar.entity.graph.DiTreeEntity;
import dev.ikm.tinkar.entity.graph.EntityVertex;
import dev.ikm.tinkar.integration.DataIntegrity;
import dev.ikm.tinkar.integration.TestConstants;
import dev.ikm.tinkar.integration.helper.DataStore;
import dev.ikm.tinkar.integration.helper.TestHelper;
import dev.ikm.tinkar.terms.EntityProxy;
import dev.ikm.tinkar.terms.State;
import dev.ikm.tinkar.terms.TinkarTerm;
import org.eclipse.collections.api.factory.Lists;
import org.eclipse.collections.api.list.ImmutableList;
import org.eclipse.collections.api.list.MutableList;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.time.Instant;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicReference;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

public class DataIntegrityIT {
    private static final Logger LOG = LoggerFactory.getLogger(DataIntegrity.class);
    private static final File DATASTORE_ROOT = TestConstants.createFilePathInTargetFromClassName.apply(DataIntegrityIT.class);
    public DataIntegrity dataIntegrity;

    @BeforeAll
    public static void beforeAll() {
        FileUtil.recursiveDelete(DATASTORE_ROOT);
        TestHelper.startDataBase(DataStore.SPINED_ARRAY_STORE, DATASTORE_ROOT);
        TestHelper.loadDataFile(TestConstants.PB_STARTER_DATA_REASONED);
    }

    @AfterAll
    public static void afterAll() {
        TestHelper.stopDatabase();
    }

    @Test
    @Disabled
    public void incorrectDataIntegrityTest() throws InterruptedException {
        TinkExecutor.threadPool().awaitTermination(5, TimeUnit.SECONDS);

        // Data Creation
//        createData();

        List<Integer> aggregatedNullNidList = new ArrayList<>();
        Map<String, List<? extends Entity>> typeNameEntityMap = new HashMap<>();
        typeNameEntityMap.put("Stamp", dataIntegrity.validateStampReferences(aggregatedNullNidList));
        typeNameEntityMap.put("Concept", dataIntegrity.validateConceptReferences(aggregatedNullNidList));
        typeNameEntityMap.put("Semantic", dataIntegrity.validateSemanticReferences(aggregatedNullNidList));
        typeNameEntityMap.put("Pattern", dataIntegrity.validatePatternReferences(aggregatedNullNidList));
        typeNameEntityMap.put("Semantic Field Data Type", dataIntegrity.validateSemanticFieldDataTypes());

//        breakdown();
        LOG.info("Report for {} database:", PrimitiveData.get().name());
        typeNameEntityMap.forEach((typeString, misconfiguredList) -> {
            LOG.info("Found {} {}s containing incorrect references.", misconfiguredList.size(), typeString);
            if (!misconfiguredList.isEmpty()) {
                LOG.info("Misconfigured {} PublicIds: ", typeString);
                misconfiguredList.stream().map(Entity::publicId).map(PublicId::idString).forEach(LOG::info);
            }
        });
        LOG.info("Found {} Nids containing incorrect references.", aggregatedNullNidList.size());
        LOG.info("Misconfigured Nids:");
        aggregatedNullNidList.stream().map(String::valueOf).forEach(LOG::info);

        // ASSERTIONS
        assertEquals(3, typeNameEntityMap.get("Semantic Field Data Type").size());
    }

    @Test
    public void correctDataIntegrityTest() {
        List<Integer> aggregatedNullNidList = new ArrayList<>();
        Map<String, List<? extends Entity>> typeNameEntityMap = new HashMap<>();
        typeNameEntityMap.put("Stamp", dataIntegrity.validateStampReferences(aggregatedNullNidList));
        typeNameEntityMap.put("Concept", dataIntegrity.validateConceptReferences(aggregatedNullNidList));
        typeNameEntityMap.put("Semantic", dataIntegrity.validateSemanticReferences(aggregatedNullNidList));
        typeNameEntityMap.put("Pattern", dataIntegrity.validatePatternReferences(aggregatedNullNidList));
        typeNameEntityMap.put("Semantic Field Data Type", dataIntegrity.validateSemanticFieldDataTypes());

//        breakdown();

        LOG.info("Report for {} database:", PrimitiveData.get().name());
        typeNameEntityMap.forEach((typeString, misconfiguredList) -> {
            LOG.info("Found {} {}s containing incorrect references.", misconfiguredList.size(), typeString);
            if (!misconfiguredList.isEmpty()) {
                LOG.info("Misconfigured {} PublicIds: ", typeString);
                misconfiguredList.stream().map(Entity::publicId).map(PublicId::idString).forEach(LOG::info);
            }
        });
        LOG.info("Found {} Nids containing incorrect references.", aggregatedNullNidList.size());
        LOG.info("Misconfigured Nids:");
        aggregatedNullNidList.stream().map(String::valueOf).forEach(LOG::info);
        LOG.info("Misconfigured Nids:");
        aggregatedNullNidList.stream().map(String::valueOf).forEach(LOG::info);

        // ASSERTIONS
        assertEquals(0, aggregatedNullNidList.size());
    }

    @Test
    public void semanticFieldSample() {
        ImmutableList<EntityProxy.Concept> patternFieldDefinitions = Lists.immutable.of(
                TinkarTerm.COMPONENT_FIELD,
                TinkarTerm.STRING
        );

        ImmutableList<Object> tooLongSemanticFieldValues = Lists.immutable.of(
                TinkarTerm.IDENTIFIER_SOURCE,
                "Test-UUID",
                "This extra field value should throw an error"
        );

        int referencedComponentNid = TinkarTerm.ANONYMOUS_CONCEPT.nid();
        StampEntity authoringStamp = StampRecord.make(UUID.randomUUID(), State.ACTIVE, System.currentTimeMillis(), TinkarTerm.USER, TinkarTerm.SOLOR_OVERLAY_MODULE, TinkarTerm.DEVELOPMENT_PATH);
        Entity patternEntity = PatternTestHelper.createPattern(EntityProxy.Pattern.make("Test Pattern", UUID.randomUUID()), authoringStamp, patternFieldDefinitions);
        EntityService.get().putEntity(authoringStamp);
        EntityService.get().putEntity(patternEntity);

        SemanticEntity<? extends SemanticEntityVersion> semanticEntityWithTooManyFieldValues =
                SemanticTestHelper.createSemanticWithSupplier(referencedComponentNid, patternEntity.nid(), tooLongSemanticFieldValues, authoringStamp);

        assertFalse(DataIntegrity.validateSemanticFieldDataType(semanticEntityWithTooManyFieldValues));
    }

    @Test
    public void semanticFieldsTooShort() {
        ImmutableList<EntityProxy.Concept> patternFieldDefinitions = Lists.immutable.of(
                TinkarTerm.COMPONENT_FIELD,
                TinkarTerm.STRING
        );

        ImmutableList<Object> tooShortSemanticFieldValues = Lists.immutable.of(
                TinkarTerm.IDENTIFIER_SOURCE
        );

        int referencedComponentNid = TinkarTerm.ANONYMOUS_CONCEPT.nid();
        StampEntity authoringStamp = StampRecord.make(UUID.randomUUID(), State.ACTIVE, System.currentTimeMillis(), TinkarTerm.USER, TinkarTerm.SOLOR_OVERLAY_MODULE, TinkarTerm.DEVELOPMENT_PATH);
        Entity patternEntity = PatternTestHelper.createPattern(EntityProxy.Pattern.make("Test Pattern", UUID.randomUUID()), authoringStamp, patternFieldDefinitions);
        EntityService.get().putEntity(authoringStamp);
        EntityService.get().putEntity(patternEntity);

        SemanticEntity<? extends SemanticEntityVersion> semanticEntityWithTooFewFieldValues =
                SemanticTestHelper.createSemanticWithSupplier(referencedComponentNid, patternEntity.nid(), tooShortSemanticFieldValues, authoringStamp);

        assertFalse(DataIntegrity.validateSemanticFieldDataType(semanticEntityWithTooFewFieldValues));
    }

    @Test
    public void semanticFieldsMatchStamp() {
        UUID stampUUID = UUID.randomUUID();
        StampEntity stamp = StampRecord.make(stampUUID, State.ACTIVE, System.currentTimeMillis(), TinkarTerm.USER, TinkarTerm.SOLOR_OVERLAY_MODULE, TinkarTerm.DEVELOPMENT_PATH);
        EntityService.get().putEntity(stamp);
        ImmutableList<EntityProxy.Concept> patternFieldDefinitions = Lists.immutable.of(
                TinkarTerm.COMPONENT_FIELD
        );

        ImmutableList<Object> semanticFieldValues = Lists.immutable.of(
                stamp
        );

        int referencedComponentNid = TinkarTerm.ANONYMOUS_CONCEPT.nid();
        StampEntity authoringStamp = StampRecord.make(UUID.randomUUID(), State.ACTIVE, System.currentTimeMillis(), TinkarTerm.USER, TinkarTerm.SOLOR_OVERLAY_MODULE, TinkarTerm.DEVELOPMENT_PATH);
        Entity patternEntity = PatternTestHelper.createPattern(EntityProxy.Pattern.make("Test Pattern", UUID.randomUUID()), authoringStamp, patternFieldDefinitions);
        EntityService.get().putEntity(authoringStamp);
        EntityService.get().putEntity(patternEntity);

        SemanticEntity<? extends SemanticEntityVersion> semanticEntityWithFieldValues =
                SemanticTestHelper.createSemanticWithSupplier(referencedComponentNid, patternEntity.nid(), semanticFieldValues, authoringStamp);

        assertTrue(DataIntegrity.validateSemanticFieldDataType(semanticEntityWithFieldValues));
    }

    @Test
    public void semanticFieldsMatchString() {
        ImmutableList<EntityProxy.Concept> patternFieldDefinitions = Lists.immutable.of(
                TinkarTerm.STRING
        );

        ImmutableList<Object> semanticFieldValues = Lists.immutable.of(
                "String"
        );

        int referencedComponentNid = TinkarTerm.ANONYMOUS_CONCEPT.nid();
        StampEntity authoringStamp = StampRecord.make(UUID.randomUUID(), State.ACTIVE, System.currentTimeMillis(), TinkarTerm.USER, TinkarTerm.SOLOR_OVERLAY_MODULE, TinkarTerm.DEVELOPMENT_PATH);
        Entity patternEntity = PatternTestHelper.createPattern(EntityProxy.Pattern.make("Test Pattern", UUID.randomUUID()), authoringStamp, patternFieldDefinitions);
        EntityService.get().putEntity(authoringStamp);
        EntityService.get().putEntity(patternEntity);

        SemanticEntity<? extends SemanticEntityVersion> semanticEntityWithFieldValues =
                SemanticTestHelper.createSemanticWithSupplier(referencedComponentNid, patternEntity.nid(), semanticFieldValues, authoringStamp);

        assertTrue(DataIntegrity.validateSemanticFieldDataType(semanticEntityWithFieldValues));
    }

    @Test
    @Disabled
    public void semanticFieldsMatchInt() {
        ImmutableList<EntityProxy.Concept> patternFieldDefinitions = Lists.immutable.of(
                TinkarTerm.INTEGER_FIELD
        );

        ImmutableList<Object> semanticFieldValues = Lists.immutable.of(
                123
        );

        int referencedComponentNid = TinkarTerm.ANONYMOUS_CONCEPT.nid();
        StampEntity authoringStamp = StampRecord.make(UUID.randomUUID(), State.ACTIVE, System.currentTimeMillis(), TinkarTerm.USER, TinkarTerm.SOLOR_OVERLAY_MODULE, TinkarTerm.DEVELOPMENT_PATH);
        Entity patternEntity = PatternTestHelper.createPattern(EntityProxy.Pattern.make("Test Pattern", UUID.randomUUID()), authoringStamp, patternFieldDefinitions);
        EntityService.get().putEntity(authoringStamp);
        EntityService.get().putEntity(patternEntity);

        SemanticEntity<? extends SemanticEntityVersion> semanticEntityWithFieldValues =
                SemanticTestHelper.createSemanticWithSupplier(referencedComponentNid, patternEntity.nid(), semanticFieldValues, authoringStamp);

        assertTrue(DataIntegrity.validateSemanticFieldDataType(semanticEntityWithFieldValues));
    }

    @Test
    public void semanticFieldsMatchFloat() {
        ImmutableList<EntityProxy.Concept> patternFieldDefinitions = Lists.immutable.of(
                TinkarTerm.FLOAT_FIELD
        );

        ImmutableList<Object> semanticFieldValues = Lists.immutable.of(
                1.0
        );

        int referencedComponentNid = TinkarTerm.ANONYMOUS_CONCEPT.nid();
        StampEntity authoringStamp = StampRecord.make(UUID.randomUUID(), State.ACTIVE, System.currentTimeMillis(), TinkarTerm.USER, TinkarTerm.SOLOR_OVERLAY_MODULE, TinkarTerm.DEVELOPMENT_PATH);
        Entity patternEntity = PatternTestHelper.createPattern(EntityProxy.Pattern.make("Test Pattern", UUID.randomUUID()), authoringStamp, patternFieldDefinitions);
        EntityService.get().putEntity(authoringStamp);
        EntityService.get().putEntity(patternEntity);

        SemanticEntity<? extends SemanticEntityVersion> semanticEntityWithFieldValues =
                SemanticTestHelper.createSemanticWithSupplier(referencedComponentNid, patternEntity.nid(), semanticFieldValues, authoringStamp);

        assertTrue(DataIntegrity.validateSemanticFieldDataType(semanticEntityWithFieldValues));
    }

    @Test
    public void semanticFieldsMatchBoolean() {
        ImmutableList<EntityProxy.Concept> patternFieldDefinitions = Lists.immutable.of(
                TinkarTerm.BOOLEAN_FIELD
        );

        ImmutableList<Object> semanticFieldValues = Lists.immutable.of(
                true
        );

        int referencedComponentNid = TinkarTerm.ANONYMOUS_CONCEPT.nid();
        StampEntity authoringStamp = StampRecord.make(UUID.randomUUID(), State.ACTIVE, System.currentTimeMillis(), TinkarTerm.USER, TinkarTerm.SOLOR_OVERLAY_MODULE, TinkarTerm.DEVELOPMENT_PATH);
        Entity patternEntity = PatternTestHelper.createPattern(EntityProxy.Pattern.make("Test Pattern", UUID.randomUUID()), authoringStamp, patternFieldDefinitions);
        EntityService.get().putEntity(authoringStamp);
        EntityService.get().putEntity(patternEntity);

        SemanticEntity<? extends SemanticEntityVersion> semanticEntityWithFieldValues =
                SemanticTestHelper.createSemanticWithSupplier(referencedComponentNid, patternEntity.nid(), semanticFieldValues, authoringStamp);

        assertTrue(DataIntegrity.validateSemanticFieldDataType(semanticEntityWithFieldValues));
    }

    @Test
    public void semanticFieldsMatchByteArray() {
        String str = "TEST";
        byte[] byteArr = str.getBytes();
        ImmutableList<EntityProxy.Concept> patternFieldDefinitions = Lists.immutable.of(
                TinkarTerm.BYTE_ARRAY_FIELD
        );

        ImmutableList<Object> semanticFieldValues = Lists.immutable.of(
                byteArr
        );

        int referencedComponentNid = TinkarTerm.ANONYMOUS_CONCEPT.nid();
        StampEntity authoringStamp = StampRecord.make(UUID.randomUUID(), State.ACTIVE, System.currentTimeMillis(), TinkarTerm.USER, TinkarTerm.SOLOR_OVERLAY_MODULE, TinkarTerm.DEVELOPMENT_PATH);
        Entity patternEntity = PatternTestHelper.createPattern(EntityProxy.Pattern.make("Test Pattern", UUID.randomUUID()), authoringStamp, patternFieldDefinitions);
        EntityService.get().putEntity(authoringStamp);
        EntityService.get().putEntity(patternEntity);

        SemanticEntity<? extends SemanticEntityVersion> semanticEntityWithFieldValues =
                SemanticTestHelper.createSemanticWithSupplier(referencedComponentNid, patternEntity.nid(), semanticFieldValues, authoringStamp);

        assertTrue(DataIntegrity.validateSemanticFieldDataType(semanticEntityWithFieldValues));
    }

//    @Test
//    public void semanticFieldsMatchObjectArray() {
//        Instant[] instantArr = new Instant[1];
//        Instant inst1 = Instant.now();
//        instantArr[0] = inst1;
//        ImmutableList<EntityProxy.Concept> patternFieldDefinitions = Lists.immutable.of(
//                TinkarTerm.ARRAY_FIELD
//        );
//
//        ImmutableList<Object> semanticFieldValues = Lists.immutable.of(
//                instantArr
//        );
//
//        int referencedComponentNid = TinkarTerm.ANONYMOUS_CONCEPT.nid();
//        StampEntity authoringStamp = StampRecord.make(UUID.randomUUID(), State.ACTIVE, System.currentTimeMillis(), TinkarTerm.USER, TinkarTerm.SOLOR_OVERLAY_MODULE, TinkarTerm.DEVELOPMENT_PATH);
//        Entity patternEntity = PatternTestHelper.createPattern(EntityProxy.Pattern.make("Test Pattern", UUID.randomUUID()), authoringStamp , patternFieldDefinitions);
//        EntityService.get().putEntity(authoringStamp);
//        EntityService.get().putEntity(patternEntity);
//
//        SemanticEntity<? extends SemanticEntityVersion> semanticEntityWithFieldValues =
//                SemanticTestHelper.createSemanticWithSupplier(referencedComponentNid, patternEntity.nid(), semanticFieldValues, authoringStamp);
//
//        assertTrue(DataIntegrity.validateSemanticFieldDataType(semanticEntityWithFieldValues));
//    }

    @Test
    public void semanticFieldsMatchConceptPatternSemanticStamp() {
        ImmutableList<EntityProxy.Concept> patternFieldDefinitions = Lists.immutable.of(
                TinkarTerm.COMPONENT_FIELD,
                TinkarTerm.COMPONENT_FIELD,
                TinkarTerm.COMPONENT_FIELD,
                TinkarTerm.COMPONENT_FIELD
        );


        int referencedComponentNid = TinkarTerm.ANONYMOUS_CONCEPT.nid();
        StampEntity authoringStamp = StampRecord.make(UUID.randomUUID(), State.ACTIVE, System.currentTimeMillis(), TinkarTerm.USER, TinkarTerm.SOLOR_OVERLAY_MODULE, TinkarTerm.DEVELOPMENT_PATH);
        Entity patternEntity = PatternTestHelper.createPattern(EntityProxy.Pattern.make("Test Pattern", UUID.randomUUID()), authoringStamp, patternFieldDefinitions);
        EntityService.get().putEntity(authoringStamp);
        EntityService.get().putEntity(patternEntity);

        AtomicReference<SemanticEntity> semanticFieldValue = new AtomicReference<SemanticEntity>();
        EntityService.get().forEachSemanticForComponentOfPattern(TinkarTerm.ANONYMOUS_CONCEPT.nid(), TinkarTerm.DESCRIPTION_PATTERN.nid(), semanticFieldValue::set);

        ImmutableList<Object> semanticFieldValues = Lists.immutable.of(
                TinkarTerm.ANONYMOUS_CONCEPT,
                TinkarTerm.DESCRIPTION_PATTERN,
                semanticFieldValue.get(),
                authoringStamp
        );


        SemanticEntity<? extends SemanticEntityVersion> semanticEntityWithFieldValues =
                SemanticTestHelper.createSemanticWithSupplier(referencedComponentNid, patternEntity.nid(), semanticFieldValues, authoringStamp);

        assertTrue(DataIntegrity.validateSemanticFieldDataType(semanticEntityWithFieldValues));
    }

    @Test
    public void semanticFieldsMatchDitree() {
        // Initialize Vertices
        EntityVertex definitionRootVertex = EntityVertex.make(TinkarTerm.DEFINITION_ROOT.nid());
        EntityVertex andVertex = EntityVertex.make(TinkarTerm.AND.nid());
        EntityVertex necessarySetVertex = EntityVertex.make(TinkarTerm.NECESSARY_SET.nid());

        EntityVertex conceptReferenceVertex = EntityVertex.make(TinkarTerm.CONCEPT_REFERENCE.nid());
        conceptReferenceVertex.putUncommittedProperty(TinkarTerm.CONCEPT_REFERENCE.nid(), TinkarTerm.ANONYMOUS_CONCEPT);
        conceptReferenceVertex.commitProperties();

        // Build Sample DiTree
        DiTreeEntity.Builder expectedMergedDteBuilder = DiTreeEntity.builder();
        expectedMergedDteBuilder.setRoot(definitionRootVertex);
        expectedMergedDteBuilder.addVertex(andVertex);
        expectedMergedDteBuilder.addVertex(necessarySetVertex);
        expectedMergedDteBuilder.addEdge(necessarySetVertex.vertexIndex(), definitionRootVertex.vertexIndex());
        expectedMergedDteBuilder.addEdge(andVertex.vertexIndex(), necessarySetVertex.vertexIndex());
        expectedMergedDteBuilder.addVertex(conceptReferenceVertex);
        expectedMergedDteBuilder.addEdge(conceptReferenceVertex.vertexIndex(), andVertex.vertexIndex());
        DiTreeEntity expectedTree = expectedMergedDteBuilder.build();

        ImmutableList<EntityProxy.Concept> patternFieldDefinitions = Lists.immutable.of(
                TinkarTerm.DITREE_FIELD
        );

        ImmutableList<Object> semanticFieldValues = Lists.immutable.of(
                expectedTree
        );

        int referencedComponentNid = TinkarTerm.ANONYMOUS_CONCEPT.nid();
        StampEntity authoringStamp = StampRecord.make(UUID.randomUUID(), State.ACTIVE, System.currentTimeMillis(), TinkarTerm.USER, TinkarTerm.SOLOR_OVERLAY_MODULE, TinkarTerm.DEVELOPMENT_PATH);
        Entity patternEntity = PatternTestHelper.createPattern(EntityProxy.Pattern.make("Test Pattern", UUID.randomUUID()), authoringStamp, patternFieldDefinitions);
        EntityService.get().putEntity(authoringStamp);
        EntityService.get().putEntity(patternEntity);

        SemanticEntity<? extends SemanticEntityVersion> semanticEntityWithFieldValues =
                SemanticTestHelper.createSemanticWithSupplier(referencedComponentNid, patternEntity.nid(), semanticFieldValues, authoringStamp);

        assertTrue(DataIntegrity.validateSemanticFieldDataType(semanticEntityWithFieldValues));
    }

    @Test
    public void semanticFieldsMismatchDigraphDiTree() {
        // Initialize Vertices
        EntityVertex definitionRootVertex = EntityVertex.make(TinkarTerm.DEFINITION_ROOT.nid());
        EntityVertex andVertex = EntityVertex.make(TinkarTerm.AND.nid());
        EntityVertex necessarySetVertex = EntityVertex.make(TinkarTerm.NECESSARY_SET.nid());

        EntityVertex conceptReferenceVertex = EntityVertex.make(TinkarTerm.CONCEPT_REFERENCE.nid());
        conceptReferenceVertex.putUncommittedProperty(TinkarTerm.CONCEPT_REFERENCE.nid(), TinkarTerm.ANONYMOUS_CONCEPT);
        conceptReferenceVertex.commitProperties();

        // Build Sample DiTree
        DiTreeEntity.Builder expectedMergedDteBuilder = DiTreeEntity.builder();
        expectedMergedDteBuilder.setRoot(definitionRootVertex);
        expectedMergedDteBuilder.addVertex(andVertex);
        expectedMergedDteBuilder.addVertex(necessarySetVertex);
        expectedMergedDteBuilder.addEdge(necessarySetVertex.vertexIndex(), definitionRootVertex.vertexIndex());
        expectedMergedDteBuilder.addEdge(andVertex.vertexIndex(), necessarySetVertex.vertexIndex());
        expectedMergedDteBuilder.addVertex(conceptReferenceVertex);
        expectedMergedDteBuilder.addEdge(conceptReferenceVertex.vertexIndex(), andVertex.vertexIndex());
        DiTreeEntity expectedTree = expectedMergedDteBuilder.build();

        ImmutableList<EntityProxy.Concept> patternFieldDefinitions = Lists.immutable.of(
                TinkarTerm.DIGRAPH_FIELD
        );

        ImmutableList<Object> semanticFieldValues = Lists.immutable.of(
                expectedTree
        );

        int referencedComponentNid = TinkarTerm.ANONYMOUS_CONCEPT.nid();
        StampEntity authoringStamp = StampRecord.make(UUID.randomUUID(), State.ACTIVE, System.currentTimeMillis(), TinkarTerm.USER, TinkarTerm.SOLOR_OVERLAY_MODULE, TinkarTerm.DEVELOPMENT_PATH);
        Entity patternEntity = PatternTestHelper.createPattern(EntityProxy.Pattern.make("Test Pattern", UUID.randomUUID()), authoringStamp, patternFieldDefinitions);
        EntityService.get().putEntity(authoringStamp);
        EntityService.get().putEntity(patternEntity);

        SemanticEntity<? extends SemanticEntityVersion> semanticEntityWithFieldValues =
                SemanticTestHelper.createSemanticWithSupplier(referencedComponentNid, patternEntity.nid(), semanticFieldValues, authoringStamp);

        assertFalse(DataIntegrity.validateSemanticFieldDataType(semanticEntityWithFieldValues));
    }

    @Test
    public void semanticFieldsMatchInstant() {
        Instant currentTime = Instant.now();
        ImmutableList<EntityProxy.Concept> patternFieldDefinitions = Lists.immutable.of(
                TinkarTerm.INSTANT_LITERAL
        );

        ImmutableList<Object> semanticFieldValues = Lists.immutable.of(
                currentTime
        );

        int referencedComponentNid = TinkarTerm.ANONYMOUS_CONCEPT.nid();
        StampEntity authoringStamp = StampRecord.make(UUID.randomUUID(), State.ACTIVE, System.currentTimeMillis(), TinkarTerm.USER, TinkarTerm.SOLOR_OVERLAY_MODULE, TinkarTerm.DEVELOPMENT_PATH);
        Entity patternEntity = PatternTestHelper.createPattern(EntityProxy.Pattern.make("Test Pattern", UUID.randomUUID()), authoringStamp, patternFieldDefinitions);
        EntityService.get().putEntity(authoringStamp);
        EntityService.get().putEntity(patternEntity);

        SemanticEntity<? extends SemanticEntityVersion> semanticEntityWithFieldValues =
                SemanticTestHelper.createSemanticWithSupplier(referencedComponentNid, patternEntity.nid(), semanticFieldValues, authoringStamp);

        assertTrue(DataIntegrity.validateSemanticFieldDataType(semanticEntityWithFieldValues));
    }

    @Test
    public void semanticFieldsMatchComponentIdList() {
        IntIdList list = IntIds.list.of(123, 456);
        ImmutableList<EntityProxy.Concept> patternFieldDefinitions = Lists.immutable.of(
                TinkarTerm.COMPONENT_ID_LIST_FIELD
        );

        ImmutableList<Object> semanticFieldValues = Lists.immutable.of(
                list
        );

        int referencedComponentNid = TinkarTerm.ANONYMOUS_CONCEPT.nid();
        StampEntity authoringStamp = StampRecord.make(UUID.randomUUID(), State.ACTIVE, System.currentTimeMillis(), TinkarTerm.USER, TinkarTerm.SOLOR_OVERLAY_MODULE, TinkarTerm.DEVELOPMENT_PATH);
        Entity patternEntity = PatternTestHelper.createPattern(EntityProxy.Pattern.make("Test Pattern", UUID.randomUUID()), authoringStamp, patternFieldDefinitions);
        EntityService.get().putEntity(authoringStamp);
        EntityService.get().putEntity(patternEntity);

        SemanticEntity<? extends SemanticEntityVersion> semanticEntityWithFieldValues =
                SemanticTestHelper.createSemanticWithSupplier(referencedComponentNid, patternEntity.nid(), semanticFieldValues, authoringStamp);

        assertTrue(DataIntegrity.validateSemanticFieldDataType(semanticEntityWithFieldValues));
    }

    @Test
    public void semanticFieldsMatchComponentIdSet() {
        IntIdSet set = IntIds.set.of(123, 456);
        ImmutableList<EntityProxy.Concept> patternFieldDefinitions = Lists.immutable.of(
                TinkarTerm.COMPONENT_ID_SET_FIELD
        );

        ImmutableList<Object> semanticFieldValues = Lists.immutable.of(
                set
        );

        int referencedComponentNid = TinkarTerm.ANONYMOUS_CONCEPT.nid();
        StampEntity authoringStamp = StampRecord.make(UUID.randomUUID(), State.ACTIVE, System.currentTimeMillis(), TinkarTerm.USER, TinkarTerm.SOLOR_OVERLAY_MODULE, TinkarTerm.DEVELOPMENT_PATH);
        Entity patternEntity = PatternTestHelper.createPattern(EntityProxy.Pattern.make("Test Pattern", UUID.randomUUID()), authoringStamp, patternFieldDefinitions);
        EntityService.get().putEntity(authoringStamp);
        EntityService.get().putEntity(patternEntity);

        SemanticEntity<? extends SemanticEntityVersion> semanticEntityWithFieldValues =
                SemanticTestHelper.createSemanticWithSupplier(referencedComponentNid, patternEntity.nid(), semanticFieldValues, authoringStamp);

        assertTrue(DataIntegrity.validateSemanticFieldDataType(semanticEntityWithFieldValues));
    }

    @Test
    public void semanticFieldsMatchLong() {
        Long longTest = 123321L;
        ImmutableList<EntityProxy.Concept> patternFieldDefinitions = Lists.immutable.of(
                TinkarTerm.LONG
        );

        ImmutableList<Object> semanticFieldValues = Lists.immutable.of(
                longTest
        );

        int referencedComponentNid = TinkarTerm.ANONYMOUS_CONCEPT.nid();
        StampEntity authoringStamp = StampRecord.make(UUID.randomUUID(), State.ACTIVE, System.currentTimeMillis(), TinkarTerm.USER, TinkarTerm.SOLOR_OVERLAY_MODULE, TinkarTerm.DEVELOPMENT_PATH);
        Entity patternEntity = PatternTestHelper.createPattern(EntityProxy.Pattern.make("Test Pattern", UUID.randomUUID()), authoringStamp, patternFieldDefinitions);
        EntityService.get().putEntity(authoringStamp);
        EntityService.get().putEntity(patternEntity);

        SemanticEntity<? extends SemanticEntityVersion> semanticEntityWithFieldValues =
                SemanticTestHelper.createSemanticWithSupplier(referencedComponentNid, patternEntity.nid(), semanticFieldValues, authoringStamp);

        assertTrue(DataIntegrity.validateSemanticFieldDataType(semanticEntityWithFieldValues));
    }

    // TESTS FOR MISMATCH - TYPE INCOMPATABILITY
    @Test
    public void semanticFieldsMismatchString() {
        ImmutableList<EntityProxy.Concept> patternFieldDefinitions = Lists.immutable.of(
                TinkarTerm.BOOLEAN_FIELD
        );

        ImmutableList<Object> semanticFieldValues = Lists.immutable.of(
                "String"
        );

        int referencedComponentNid = TinkarTerm.ANONYMOUS_CONCEPT.nid();
        StampEntity authoringStamp = StampRecord.make(UUID.randomUUID(), State.ACTIVE, System.currentTimeMillis(), TinkarTerm.USER, TinkarTerm.SOLOR_OVERLAY_MODULE, TinkarTerm.DEVELOPMENT_PATH);
        Entity patternEntity = PatternTestHelper.createPattern(EntityProxy.Pattern.make("Test Pattern", UUID.randomUUID()), authoringStamp, patternFieldDefinitions);
        EntityService.get().putEntity(authoringStamp);
        EntityService.get().putEntity(patternEntity);

        SemanticEntity<? extends SemanticEntityVersion> semanticEntityWithFieldValues =
                SemanticTestHelper.createSemanticWithSupplier(referencedComponentNid, patternEntity.nid(), semanticFieldValues, authoringStamp);

        assertFalse(DataIntegrity.validateSemanticFieldDataType(semanticEntityWithFieldValues));
    }

    @Test
    public void semanticFieldsMismatchInt() {
        ImmutableList<EntityProxy.Concept> patternFieldDefinitions = Lists.immutable.of(
                TinkarTerm.BOOLEAN_FIELD
        );

        ImmutableList<Object> semanticFieldValues = Lists.immutable.of(
                123
        );

        int referencedComponentNid = TinkarTerm.ANONYMOUS_CONCEPT.nid();
        StampEntity authoringStamp = StampRecord.make(UUID.randomUUID(), State.ACTIVE, System.currentTimeMillis(), TinkarTerm.USER, TinkarTerm.SOLOR_OVERLAY_MODULE, TinkarTerm.DEVELOPMENT_PATH);
        Entity patternEntity = PatternTestHelper.createPattern(EntityProxy.Pattern.make("Test Pattern", UUID.randomUUID()), authoringStamp, patternFieldDefinitions);
        EntityService.get().putEntity(authoringStamp);
        EntityService.get().putEntity(patternEntity);

        SemanticEntity<? extends SemanticEntityVersion> semanticEntityWithFieldValues =
                SemanticTestHelper.createSemanticWithSupplier(referencedComponentNid, patternEntity.nid(), semanticFieldValues, authoringStamp);

        assertFalse(DataIntegrity.validateSemanticFieldDataType(semanticEntityWithFieldValues));
    }

    @Test
    public void semanticFieldsMismatchFloat() {
        ImmutableList<EntityProxy.Concept> patternFieldDefinitions = Lists.immutable.of(
                TinkarTerm.BOOLEAN_FIELD
        );

        ImmutableList<Object> semanticFieldValues = Lists.immutable.of(
                1.0
        );

        int referencedComponentNid = TinkarTerm.ANONYMOUS_CONCEPT.nid();
        StampEntity authoringStamp = StampRecord.make(UUID.randomUUID(), State.ACTIVE, System.currentTimeMillis(), TinkarTerm.USER, TinkarTerm.SOLOR_OVERLAY_MODULE, TinkarTerm.DEVELOPMENT_PATH);
        Entity patternEntity = PatternTestHelper.createPattern(EntityProxy.Pattern.make("Test Pattern", UUID.randomUUID()), authoringStamp, patternFieldDefinitions);
        EntityService.get().putEntity(authoringStamp);
        EntityService.get().putEntity(patternEntity);

        SemanticEntity<? extends SemanticEntityVersion> semanticEntityWithFieldValues =
                SemanticTestHelper.createSemanticWithSupplier(referencedComponentNid, patternEntity.nid(), semanticFieldValues, authoringStamp);

        assertFalse(DataIntegrity.validateSemanticFieldDataType(semanticEntityWithFieldValues));
    }

    @Test
    public void semanticFieldsMismatchBoolean() {
        ImmutableList<EntityProxy.Concept> patternFieldDefinitions = Lists.immutable.of(
                TinkarTerm.STRING
        );

        ImmutableList<Object> semanticFieldValues = Lists.immutable.of(
                true
        );

        int referencedComponentNid = TinkarTerm.ANONYMOUS_CONCEPT.nid();
        StampEntity authoringStamp = StampRecord.make(UUID.randomUUID(), State.ACTIVE, System.currentTimeMillis(), TinkarTerm.USER, TinkarTerm.SOLOR_OVERLAY_MODULE, TinkarTerm.DEVELOPMENT_PATH);
        Entity patternEntity = PatternTestHelper.createPattern(EntityProxy.Pattern.make("Test Pattern", UUID.randomUUID()), authoringStamp, patternFieldDefinitions);
        EntityService.get().putEntity(authoringStamp);
        EntityService.get().putEntity(patternEntity);

        SemanticEntity<? extends SemanticEntityVersion> semanticEntityWithFieldValues =
                SemanticTestHelper.createSemanticWithSupplier(referencedComponentNid, patternEntity.nid(), semanticFieldValues, authoringStamp);

        assertFalse(DataIntegrity.validateSemanticFieldDataType(semanticEntityWithFieldValues));
    }

    @Test
    public void semanticFieldsMismatchByteArray() {
        String str = "TEST";
        byte[] byteArr = str.getBytes();
        ImmutableList<EntityProxy.Concept> patternFieldDefinitions = Lists.immutable.of(
                TinkarTerm.BOOLEAN_FIELD
        );

        ImmutableList<Object> semanticFieldValues = Lists.immutable.of(
                byteArr
        );

        int referencedComponentNid = TinkarTerm.ANONYMOUS_CONCEPT.nid();
        StampEntity authoringStamp = StampRecord.make(UUID.randomUUID(), State.ACTIVE, System.currentTimeMillis(), TinkarTerm.USER, TinkarTerm.SOLOR_OVERLAY_MODULE, TinkarTerm.DEVELOPMENT_PATH);
        Entity patternEntity = PatternTestHelper.createPattern(EntityProxy.Pattern.make("Test Pattern", UUID.randomUUID()), authoringStamp, patternFieldDefinitions);
        EntityService.get().putEntity(authoringStamp);
        EntityService.get().putEntity(patternEntity);

        SemanticEntity<? extends SemanticEntityVersion> semanticEntityWithFieldValues =
                SemanticTestHelper.createSemanticWithSupplier(referencedComponentNid, patternEntity.nid(), semanticFieldValues, authoringStamp);

        assertFalse(DataIntegrity.validateSemanticFieldDataType(semanticEntityWithFieldValues));
    }

    @Test
    public void semanticFieldsMismatchObjectArray() {
        Instant[] instantArr = new Instant[1];
        Instant inst1 = Instant.now();
        instantArr[0] = inst1;
        ImmutableList<EntityProxy.Concept> patternFieldDefinitions = Lists.immutable.of(
                TinkarTerm.BOOLEAN_FIELD
        );

        ImmutableList<Object> semanticFieldValues = Lists.immutable.of(
                (Object) instantArr
        );

        int referencedComponentNid = TinkarTerm.ANONYMOUS_CONCEPT.nid();
        StampEntity authoringStamp = StampRecord.make(UUID.randomUUID(), State.ACTIVE, System.currentTimeMillis(), TinkarTerm.USER, TinkarTerm.SOLOR_OVERLAY_MODULE, TinkarTerm.DEVELOPMENT_PATH);
        Entity patternEntity = PatternTestHelper.createPattern(EntityProxy.Pattern.make("Test Pattern", UUID.randomUUID()), authoringStamp, patternFieldDefinitions);
        EntityService.get().putEntity(authoringStamp);
        EntityService.get().putEntity(patternEntity);

        SemanticEntity<? extends SemanticEntityVersion> semanticEntityWithFieldValues =
                SemanticTestHelper.createSemanticWithSupplier(referencedComponentNid, patternEntity.nid(), semanticFieldValues, authoringStamp);

        assertFalse(DataIntegrity.validateSemanticFieldDataType(semanticEntityWithFieldValues));
    }

    @Test
    public void semanticFieldsMismatchConceptPatternSemanticStamp() {
        ImmutableList<EntityProxy.Concept> patternFieldDefinitions = Lists.immutable.of(
                TinkarTerm.STRING,
                TinkarTerm.STRING,
                TinkarTerm.STRING,
                TinkarTerm.STRING
        );


        int referencedComponentNid = TinkarTerm.ANONYMOUS_CONCEPT.nid();
        StampEntity authoringStamp = StampRecord.make(UUID.randomUUID(), State.ACTIVE, System.currentTimeMillis(), TinkarTerm.USER, TinkarTerm.SOLOR_OVERLAY_MODULE, TinkarTerm.DEVELOPMENT_PATH);
        Entity patternEntity = PatternTestHelper.createPattern(EntityProxy.Pattern.make("Test Pattern", UUID.randomUUID()), authoringStamp, patternFieldDefinitions);
        EntityService.get().putEntity(authoringStamp);
        EntityService.get().putEntity(patternEntity);

        AtomicReference<SemanticEntity> semanticFieldValue = new AtomicReference<SemanticEntity>();
        EntityService.get().forEachSemanticForComponentOfPattern(TinkarTerm.ANONYMOUS_CONCEPT.nid(), TinkarTerm.DESCRIPTION_PATTERN.nid(), semanticFieldValue::set);

        ImmutableList<Object> semanticFieldValues = Lists.immutable.of(
                TinkarTerm.ANONYMOUS_CONCEPT,
                TinkarTerm.DESCRIPTION_PATTERN,
                semanticFieldValue.get(),
                authoringStamp
        );


        SemanticEntity<? extends SemanticEntityVersion> semanticEntityWithFieldValues =
                SemanticTestHelper.createSemanticWithSupplier(referencedComponentNid, patternEntity.nid(), semanticFieldValues, authoringStamp);

        assertFalse(DataIntegrity.validateSemanticFieldDataType(semanticEntityWithFieldValues));
    }

    @Test
    public void semanticFieldsMismatchInstant() {
        Instant currentTime = Instant.now();
        ImmutableList<EntityProxy.Concept> patternFieldDefinitions = Lists.immutable.of(
                TinkarTerm.STRING
        );

        ImmutableList<Object> semanticFieldValues = Lists.immutable.of(
                currentTime
        );

        int referencedComponentNid = TinkarTerm.ANONYMOUS_CONCEPT.nid();
        StampEntity authoringStamp = StampRecord.make(UUID.randomUUID(), State.ACTIVE, System.currentTimeMillis(), TinkarTerm.USER, TinkarTerm.SOLOR_OVERLAY_MODULE, TinkarTerm.DEVELOPMENT_PATH);
        Entity patternEntity = PatternTestHelper.createPattern(EntityProxy.Pattern.make("Test Pattern", UUID.randomUUID()), authoringStamp, patternFieldDefinitions);
        EntityService.get().putEntity(authoringStamp);
        EntityService.get().putEntity(patternEntity);

        SemanticEntity<? extends SemanticEntityVersion> semanticEntityWithFieldValues =
                SemanticTestHelper.createSemanticWithSupplier(referencedComponentNid, patternEntity.nid(), semanticFieldValues, authoringStamp);

        assertFalse(DataIntegrity.validateSemanticFieldDataType(semanticEntityWithFieldValues));
    }

    @Test
    public void semanticFieldsMismatchComponentIdList() {
        IntIdList list = IntIds.list.of(123, 456);
        ImmutableList<EntityProxy.Concept> patternFieldDefinitions = Lists.immutable.of(
                TinkarTerm.STRING
        );

        ImmutableList<Object> semanticFieldValues = Lists.immutable.of(
                list
        );

        int referencedComponentNid = TinkarTerm.ANONYMOUS_CONCEPT.nid();
        StampEntity authoringStamp = StampRecord.make(UUID.randomUUID(), State.ACTIVE, System.currentTimeMillis(), TinkarTerm.USER, TinkarTerm.SOLOR_OVERLAY_MODULE, TinkarTerm.DEVELOPMENT_PATH);
        Entity patternEntity = PatternTestHelper.createPattern(EntityProxy.Pattern.make("Test Pattern", UUID.randomUUID()), authoringStamp, patternFieldDefinitions);
        EntityService.get().putEntity(authoringStamp);
        EntityService.get().putEntity(patternEntity);

        SemanticEntity<? extends SemanticEntityVersion> semanticEntityWithFieldValues =
                SemanticTestHelper.createSemanticWithSupplier(referencedComponentNid, patternEntity.nid(), semanticFieldValues, authoringStamp);

        assertFalse(DataIntegrity.validateSemanticFieldDataType(semanticEntityWithFieldValues));
    }

    @Test
    public void semanticFieldsMismatchComponentIdSet() {
        IntIdSet set = IntIds.set.of(123, 456);
        ImmutableList<EntityProxy.Concept> patternFieldDefinitions = Lists.immutable.of(
                TinkarTerm.STRING
        );

        ImmutableList<Object> semanticFieldValues = Lists.immutable.of(
                set
        );

        int referencedComponentNid = TinkarTerm.ANONYMOUS_CONCEPT.nid();
        StampEntity authoringStamp = StampRecord.make(UUID.randomUUID(), State.ACTIVE, System.currentTimeMillis(), TinkarTerm.USER, TinkarTerm.SOLOR_OVERLAY_MODULE, TinkarTerm.DEVELOPMENT_PATH);
        Entity patternEntity = PatternTestHelper.createPattern(EntityProxy.Pattern.make("Test Pattern", UUID.randomUUID()), authoringStamp, patternFieldDefinitions);
        EntityService.get().putEntity(authoringStamp);
        EntityService.get().putEntity(patternEntity);

        SemanticEntity<? extends SemanticEntityVersion> semanticEntityWithFieldValues =
                SemanticTestHelper.createSemanticWithSupplier(referencedComponentNid, patternEntity.nid(), semanticFieldValues, authoringStamp);

        assertFalse(DataIntegrity.validateSemanticFieldDataType(semanticEntityWithFieldValues));
    }

    @Test
    public void semanticFieldsMismatchLong() {
        Long longTest = 123321L;
        ImmutableList<EntityProxy.Concept> patternFieldDefinitions = Lists.immutable.of(
                TinkarTerm.BOOLEAN_FIELD
        );

        ImmutableList<Object> semanticFieldValues = Lists.immutable.of(
                longTest
        );

        int referencedComponentNid = TinkarTerm.ANONYMOUS_CONCEPT.nid();
        StampEntity authoringStamp = StampRecord.make(UUID.randomUUID(), State.ACTIVE, System.currentTimeMillis(), TinkarTerm.USER, TinkarTerm.SOLOR_OVERLAY_MODULE, TinkarTerm.DEVELOPMENT_PATH);
        Entity patternEntity = PatternTestHelper.createPattern(EntityProxy.Pattern.make("Test Pattern", UUID.randomUUID()), authoringStamp, patternFieldDefinitions);
        EntityService.get().putEntity(authoringStamp);
        EntityService.get().putEntity(patternEntity);

        SemanticEntity<? extends SemanticEntityVersion> semanticEntityWithFieldValues =
                SemanticTestHelper.createSemanticWithSupplier(referencedComponentNid, patternEntity.nid(), semanticFieldValues, authoringStamp);

        assertFalse(DataIntegrity.validateSemanticFieldDataType(semanticEntityWithFieldValues));
    }


    /*############# Semantic Helper Functions ##############*/
    private class SemanticTestHelper {
        public static SemanticEntity<? extends SemanticEntityVersion> createSemanticWithSupplier(int referencedComponentNid,
                                                                                                 int patternNid,
                                                                                                 ImmutableList<Object> fieldValues,
                                                                                                 Entity<? extends EntityVersion> authoringSTAMP) {
            RecordListBuilder<SemanticVersionRecord> versions = RecordListBuilder.make();

            UUID navigationSemanticUUID = UUID.randomUUID();
            SemanticRecord semanticRecord = SemanticRecordBuilder.builder()
                    .nid(EntityService.get().nidForUuids(navigationSemanticUUID))
                    .leastSignificantBits(navigationSemanticUUID.getLeastSignificantBits())
                    .mostSignificantBits(navigationSemanticUUID.getMostSignificantBits())
                    .additionalUuidLongs(null)
                    .patternNid(patternNid)
                    .referencedComponentNid(referencedComponentNid)
                    .versions(versions.toImmutable())
                    .build();

            versions.add(SemanticVersionRecordBuilder.builder()
                    .chronology(semanticRecord)
                    .stampNid(authoringSTAMP.nid())
                    .fieldValues(fieldValues)
                    .build());

            return SemanticRecordBuilder.builder(semanticRecord).versions(versions.toImmutable()).build();
        }

    }

    /*############# Pattern Helper Functions ##############*/
    private class PatternTestHelper {
        public static Entity<? extends EntityVersion> createPattern(EntityProxy.Pattern pattern,
                                                                    Entity<? extends EntityVersion> authoringSTAMP,
                                                                    ImmutableList<EntityProxy.Concept> fieldDefinitionConcepts) {
            return createPattern(pattern, TinkarTerm.MEANING, TinkarTerm.PURPOSE, authoringSTAMP, fieldDefinitionConcepts);
        }

        public static Entity<? extends EntityVersion> createPattern(EntityProxy.Pattern pattern,
                                                                    EntityProxy.Concept meaningConcept,
                                                                    EntityProxy.Concept purposeConcept,
                                                                    Entity<? extends EntityVersion> authoringSTAMP,
                                                                    ImmutableList<EntityProxy.Concept> fieldDefinitionConcepts) {

            MutableList<FieldDefinitionRecord> fieldDefinitions = Lists.mutable.empty();
            int i = 0;
            for (EntityProxy.Concept fieldDefConcept : fieldDefinitionConcepts) {
                fieldDefinitions.add(fieldDefinition(pattern.nid(), fieldDefConcept, authoringSTAMP, i++));
            }

            RecordListBuilder<PatternVersionRecord> versions = RecordListBuilder.make();
            PatternRecord patternRecord = PatternRecordBuilder.builder()
                    .nid(pattern.nid())
                    .leastSignificantBits(pattern.asUuidArray()[0].getLeastSignificantBits())
                    .mostSignificantBits(pattern.asUuidArray()[0].getMostSignificantBits())
                    .additionalUuidLongs(null)
                    .versions(versions.toImmutable())
                    .build();

            versions.add(PatternVersionRecordBuilder.builder()
                    .chronology(patternRecord)
                    .stampNid(authoringSTAMP.nid())
                    .semanticMeaningNid(meaningConcept.nid())
                    .semanticPurposeNid(purposeConcept.nid())
                    .fieldDefinitions(fieldDefinitions.toImmutable())
                    .build());

            return PatternRecordBuilder.builder(patternRecord).versions(versions.toImmutable()).build();
        }

        public static FieldDefinitionRecord fieldDefinition(int patternNid, EntityProxy.Concept dataType,
                                                            Entity<? extends EntityVersion> authoringSTAMP, int idx) {
            return fieldDefinition(patternNid, TinkarTerm.MEANING, TinkarTerm.PURPOSE, dataType, authoringSTAMP, idx);
        }

        public static FieldDefinitionRecord fieldDefinition(int patternNid, EntityProxy.Concept meaning, EntityProxy.Concept purpose,
                                                            EntityProxy.Concept dataType, Entity<? extends EntityVersion> authoringSTAMP, int idx) {
            return FieldDefinitionRecordBuilder.builder()
                    .patternNid(patternNid)
                    .meaningNid(meaning.nid())
                    .purposeNid(purpose.nid())
                    .dataTypeNid(dataType.nid())
                    .indexInPattern(idx)
                    .patternVersionStampNid(authoringSTAMP.nid())
                    .build();
        }
    }

}
