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
package dev.ikm.tinkar.entity.transform;

import dev.ikm.tinkar.common.id.IntIdList;
import dev.ikm.tinkar.common.id.IntIdSet;
import dev.ikm.tinkar.common.id.IntIds;
import dev.ikm.tinkar.common.id.PublicId;
import dev.ikm.tinkar.common.id.PublicIds;
import dev.ikm.tinkar.common.service.PrimitiveData;
import dev.ikm.tinkar.common.util.time.DateTimeUtil;
import dev.ikm.tinkar.common.util.uuid.UuidUtil;
import dev.ikm.tinkar.component.Component;
import dev.ikm.tinkar.component.location.PlanarPoint;
import dev.ikm.tinkar.component.location.SpatialPoint;
import dev.ikm.tinkar.entity.*;
import dev.ikm.tinkar.entity.graph.DiGraphEntity;
import dev.ikm.tinkar.entity.graph.DiTreeEntity;
import dev.ikm.tinkar.entity.graph.EntityVertex;
import dev.ikm.tinkar.schema.ConceptChronology;
import dev.ikm.tinkar.schema.ConceptVersion;
import dev.ikm.tinkar.schema.DiGraph;
import dev.ikm.tinkar.schema.DiTree;
import dev.ikm.tinkar.schema.Field;
import dev.ikm.tinkar.schema.FieldDefinition;
import dev.ikm.tinkar.schema.IntToIntMap;
import dev.ikm.tinkar.schema.IntToMultipleIntMap;
import dev.ikm.tinkar.schema.PatternChronology;
import dev.ikm.tinkar.schema.PatternVersion;
import dev.ikm.tinkar.schema.SemanticChronology;
import dev.ikm.tinkar.schema.SemanticVersion;
import dev.ikm.tinkar.schema.StampChronology;
import dev.ikm.tinkar.schema.StampVersion;
import dev.ikm.tinkar.schema.TinkarMsg;
import dev.ikm.tinkar.schema.Vertex;
import dev.ikm.tinkar.schema.VertexUUID;
import dev.ikm.tinkar.terms.EntityBinding;
import dev.ikm.tinkar.terms.EntityProxy;
import dev.ikm.tinkar.terms.PatternFacade;
import org.eclipse.collections.api.factory.Lists;
import org.eclipse.collections.api.list.ImmutableList;
import org.eclipse.collections.api.list.MutableList;
import org.eclipse.collections.api.list.primitive.ImmutableIntList;
import org.eclipse.collections.api.list.primitive.MutableIntList;
import org.eclipse.collections.api.map.primitive.ImmutableIntIntMap;
import org.eclipse.collections.api.map.primitive.ImmutableIntObjectMap;
import org.eclipse.collections.api.map.primitive.MutableIntIntMap;
import org.eclipse.collections.api.map.primitive.MutableIntObjectMap;
import org.eclipse.collections.impl.factory.primitive.IntObjectMaps;
import org.eclipse.collections.impl.list.mutable.primitive.IntArrayList;
import org.eclipse.collections.impl.map.mutable.primitive.IntIntHashMap;
import org.eclipse.collections.impl.map.mutable.primitive.IntObjectHashMap;

import java.math.BigDecimal;
import java.math.BigInteger;
import java.math.MathContext;
import java.util.*;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.function.Consumer;

import static dev.ikm.tinkar.common.service.PrimitiveData.SCOPED_PATTERN_PUBLICID_FOR_NID;

public class TinkarSchemaToEntityTransformer {
    private static TinkarSchemaToEntityTransformer INSTANCE;
    private TinkarSchemaToEntityTransformer() {
    }
    public static TinkarSchemaToEntityTransformer getInstance() {
        if (INSTANCE == null) {
            synchronized (TinkarSchemaToEntityTransformer.class) {
                if (INSTANCE == null) {
                    INSTANCE = new TinkarSchemaToEntityTransformer();
                }
            }
        }
        return INSTANCE;
    }
    public void transform(TinkarMsg pbTinkarMsg, Consumer<Entity<? extends EntityVersion>> entityConsumer, Consumer<StampEntity<StampEntityVersion>> stampEntityConsumer) {
        Entity entity = switch (pbTinkarMsg.getValueCase()) {
            case CONCEPT_CHRONOLOGY -> transformConceptChronology(pbTinkarMsg.getConceptChronology(), stampEntityConsumer);
            case SEMANTIC_CHRONOLOGY -> transformSemanticChronology(pbTinkarMsg.getSemanticChronology(), stampEntityConsumer);
            case PATTERN_CHRONOLOGY -> transformPatternChronology(pbTinkarMsg.getPatternChronology(), stampEntityConsumer);
            case STAMP_CHRONOLOGY -> transformStampChronology(pbTinkarMsg.getStampChronology(), stampEntityConsumer);
            case VALUE_NOT_SET -> throw new IllegalStateException("Tinkar message value not set");
            default -> throw new IllegalStateException("Unexpected value: " + pbTinkarMsg.getValueCase());
        };
        if(entityConsumer != null) {
            entityConsumer.accept(entity);
        }
    }
    protected ConceptEntity<? extends ConceptEntityVersion> transformConceptChronology(ConceptChronology pbConceptChronology) {
        return transformConceptChronology(pbConceptChronology, null);
    }
    protected ConceptEntity<? extends ConceptEntityVersion> transformConceptChronology(ConceptChronology pbConceptChronology, Consumer<StampEntity<StampEntityVersion>> stampEntityConsumer) {
        if(pbConceptChronology.getConceptVersionsCount() == 0){
            throw new RuntimeException("Exception thrown, Concept Chronology can't contain zero versions");
        }
        int conceptNid = nidForConcept(pbConceptChronology.getPublicId());
        PublicId conceptPublicId = transformPublicId(pbConceptChronology.getPublicId());
        RecordListBuilder<ConceptVersionRecord> conceptVersions = RecordListBuilder.make();
        ConceptRecord conceptRecord = switch (conceptPublicId.uuidCount()) {
            case 0 -> throw new IllegalStateException("No UUIDs in PublicId.");
            case 1 -> ConceptRecordBuilder.builder()
                    .leastSignificantBits(conceptPublicId.asUuidArray()[0].getLeastSignificantBits())
                    .mostSignificantBits(conceptPublicId.asUuidArray()[0].getMostSignificantBits())
                    .nid(conceptNid)
                    .versions(conceptVersions)
                    .build();
            default -> ConceptRecordBuilder.builder()
                    .leastSignificantBits(conceptPublicId.asUuidArray()[0].getLeastSignificantBits())
                    .mostSignificantBits(conceptPublicId.asUuidArray()[0].getMostSignificantBits())
                    .additionalUuidLongs(UuidUtil.asArray(Arrays.copyOfRange(conceptPublicId.asUuidArray(),
                            1, conceptPublicId.uuidCount())))
                    .nid(conceptNid)
                    .versions(conceptVersions)
                    .build();
        };

        for (ConceptVersion pbConceptVersion : pbConceptChronology.getConceptVersionsList()) {
            conceptVersions.add(transformConceptVersion(pbConceptVersion, conceptRecord, stampEntityConsumer));
        }
        return ConceptRecordBuilder.builder(conceptRecord).versions(conceptVersions).build();
    }
    protected ConceptVersionRecord transformConceptVersion(ConceptVersion pbConceptVersion, ConceptRecord concept, Consumer<StampEntity<StampEntityVersion>> stampEntityConsumer) {
        return ConceptVersionRecordBuilder.builder()
                .chronology(concept)
                .stampNid(nidForStamp(pbConceptVersion.getStampChronologyPublicId()))
                .build();
    }

    protected SemanticEntity<? extends SemanticEntityVersion> transformSemanticChronology(SemanticChronology pbSemanticChronology){
        return transformSemanticChronology(pbSemanticChronology, null);
    }

    protected SemanticEntity<? extends SemanticEntityVersion> transformSemanticChronology(SemanticChronology pbSemanticChronology, Consumer<StampEntity<StampEntityVersion>> stampEntityConsumer){
        if(pbSemanticChronology.getSemanticVersionsCount() == 0){
            throw new RuntimeException("Exception thrown, Semantic Chronology can't contain zero versions");
        }
        RecordListBuilder<SemanticVersionRecord> semanticVersions = RecordListBuilder.make();

        PublicId semanticPublicId = transformPublicId(pbSemanticChronology.getPublicId());
        PublicId patternPublicId = transformPublicId(pbSemanticChronology.getPatternForSemanticPublicId());
        PublicId referencedComponentPublicId = transformPublicId(pbSemanticChronology.getReferencedComponentPublicId());

        SemanticRecord semanticRecord;
        int patternNid = nidForPattern(patternPublicId);


        // Hope that the referenced component was created before the semantic and is already in the database.
        // If not, we'll have to do late binding somehow, or do a two pass...
        final int referencedComponentNid  = EntityHandle.getEntityOrThrow(referencedComponentPublicId).nid();

        if (semanticPublicId.uuidCount() > 0) {
            int semanticNid = nidForSemantic(patternPublicId, semanticPublicId);
            if (semanticPublicId.uuidCount() > 1) {
                semanticRecord = SemanticRecordBuilder.builder()
                        .leastSignificantBits(semanticPublicId.asUuidArray()[0].getLeastSignificantBits())
                        .mostSignificantBits(semanticPublicId.asUuidArray()[0].getMostSignificantBits())
                        .additionalUuidLongs(UuidUtil.asArray(Arrays.copyOfRange(semanticPublicId.asUuidArray(),
                                1, semanticPublicId.uuidCount())))
                        .nid(semanticNid)
                        .patternNid(patternNid)
                        .referencedComponentNid(referencedComponentNid)
                        .versions(semanticVersions)
                        .build();
            } else {
                semanticRecord = SemanticRecordBuilder.builder()
                        .leastSignificantBits(semanticPublicId.asUuidArray()[0].getLeastSignificantBits())
                        .mostSignificantBits(semanticPublicId.asUuidArray()[0].getMostSignificantBits())
                        .nid(semanticNid)
                        .patternNid(patternNid)
                        .referencedComponentNid(referencedComponentNid)
                        .versions(semanticVersions)
                        .build();
            }
        } else {
            throw new IllegalStateException("missing primordial UUID");
        }
        for(SemanticVersion pbSemanticVersion : pbSemanticChronology.getSemanticVersionsList()){
            semanticVersions.add(transformSemanticVersion(pbSemanticVersion, semanticRecord, stampEntityConsumer));
        }
        return SemanticRecordBuilder.builder(semanticRecord).versions(semanticVersions).build();
    }
    //This is creating PBSemanticVersion (line 314 Tinkar.proto)
    protected SemanticVersionRecord transformSemanticVersion(SemanticVersion pbSemanticVersion, SemanticRecord semantic, Consumer<StampEntity<StampEntityVersion>> stampEntityConsumer){
        MutableList<Object> fieldValues = Lists.mutable.ofInitialCapacity(pbSemanticVersion.getFieldsCount());
        for(Field pbField : pbSemanticVersion.getFieldsList()){
            Object transformedObject = EntityRecordFactory.externalToInternalObject(transformField(pbField, stampEntityConsumer));
            fieldValues.add(transformedObject);
        }
        return SemanticVersionRecordBuilder.builder()
                .chronology(semantic)
                //TODO: Need to add the stamp consumer here
                .stampNid(nidForStamp(pbSemanticVersion.getStampChronologyPublicId()))
                .fieldValues(fieldValues.toImmutable())
                .build();
    }
    protected PatternEntity<? extends PatternEntityVersion> transformPatternChronology(PatternChronology pbPatternChronology) {
        return transformPatternChronology(pbPatternChronology,null);
    }
    //Pattern Transformation
    //This is creating a PatternChronology (line 270 Tinkar.proto)
    protected PatternEntity<? extends PatternEntityVersion> transformPatternChronology(PatternChronology pbPatternChronology, Consumer<StampEntity<StampEntityVersion>> stampEntityConsumer){
        if(pbPatternChronology.getPatternVersionsCount() == 0){
            throw new RuntimeException("Exception thrown, Pattern Chronology can't contain zero versions");
        }
        RecordListBuilder<PatternVersionRecord> patternVersions = RecordListBuilder.make();
        PublicId patternPublicId = transformPublicId(pbPatternChronology.getPublicId());
        PatternRecord patternRecord;
        if (patternPublicId.uuidCount() > 0) {
            if (patternPublicId.uuidCount() > 1) {
                patternRecord = PatternRecordBuilder.builder()
                        .leastSignificantBits(patternPublicId.asUuidArray()[0].getLeastSignificantBits())
                        .mostSignificantBits(patternPublicId.asUuidArray()[0].getMostSignificantBits())
                        .nid(nidForPattern(patternPublicId))
                        .additionalUuidLongs(UuidUtil.asArray(Arrays.copyOfRange(patternPublicId.asUuidArray(),
                                1, patternPublicId.uuidCount())))
                        .versions(patternVersions)
                        .build();
            } else {
                patternRecord = PatternRecordBuilder.builder()
                        .leastSignificantBits(patternPublicId.asUuidArray()[0].getLeastSignificantBits())
                        .mostSignificantBits(patternPublicId.asUuidArray()[0].getMostSignificantBits())
                        .nid(nidForPattern(patternPublicId))
                        .versions(patternVersions)
                        .build();
            }
        } else {
            throw new IllegalStateException("missing primordial UUID");
        }
        for(PatternVersion pbPatternVersion : pbPatternChronology.getPatternVersionsList()){
            patternVersions.add(transformPatternVersion(pbPatternVersion, patternRecord, stampEntityConsumer));
        }
        return PatternRecordBuilder.builder(patternRecord).versions(patternVersions).build();
    }
    protected PatternVersionRecord transformPatternVersion(PatternVersion pbPatternVersion, PatternRecord pattern, Consumer<StampEntity<StampEntityVersion>> stampEntityConsumer){
        MutableList<FieldDefinitionRecord> fieldDefinition = Lists.mutable
                .withInitialCapacity(pbPatternVersion.getFieldDefinitionsCount());
        //TODO: Is this a proper way to grab NID?
        int patternNid = pattern.nid();
        int patternStampNid = nidForStamp(pbPatternVersion.getStampChronologyPublicId());
        int semanticPurposeNid = nidForConcept(pbPatternVersion.getReferencedComponentPurposePublicId());
        int semanticMeaningNid = nidForConcept(pbPatternVersion.getReferencedComponentMeaningPublicId());
        for(FieldDefinition pbFieldDefinition : pbPatternVersion.getFieldDefinitionsList()){
            fieldDefinition.add(transformFieldDefinitionRecord(pbFieldDefinition, patternStampNid, patternNid));
        }
        return PatternVersionRecordBuilder.builder()
                .chronology(pattern)
                .stampNid(patternStampNid)
                .semanticPurposeNid(semanticPurposeNid)
                .semanticMeaningNid(semanticMeaningNid)
                .fieldDefinitions(fieldDefinition.toImmutable())
                .build();
    }
    //STAMP Transformation
    //This is creating PBStampChronology (line 209 Tinkar.proto)
    public StampRecord transformStampChronology(StampChronology stampChronology, Consumer<StampEntity<StampEntityVersion>> stampEntityConsumer){
        if(stampChronology.getPublicId() == null){
            throw new RuntimeException("Exception thrown, STAMP Public is null.");
        }
        RecordListBuilder<StampVersionRecord> stampVersions = RecordListBuilder.make();
        PublicId stampPublicId = transformPublicId(stampChronology.getPublicId());
        StampRecord stampRecord;
        if (stampPublicId.uuidCount() > 0) {
            int stampNid = nidForStamp(stampChronology.getPublicId());
            if (stampPublicId.uuidCount() > 1) {
                stampRecord = StampRecordBuilder.builder()
                        .leastSignificantBits(stampPublicId.asUuidArray()[0].getLeastSignificantBits())
                        .mostSignificantBits(stampPublicId.asUuidArray()[0].getMostSignificantBits())
                        .additionalUuidLongs(UuidUtil.asArray(Arrays.copyOfRange(stampPublicId.asUuidArray(),
                                1, stampPublicId.uuidCount())))
                        .nid(stampNid)
                        .versions(stampVersions)
                        .build();
            } else {
                stampRecord = StampRecordBuilder.builder()
                        .leastSignificantBits(stampPublicId.asUuidArray()[0].getLeastSignificantBits())
                        .mostSignificantBits(stampPublicId.asUuidArray()[0].getMostSignificantBits())
                        .nid(stampNid)
                        .versions(stampVersions)
                        .build();
            }
        } else {
            throw new IllegalStateException("missing primordial UUID");
        }
        if(stampChronology.hasFirstStampVersion()){
            stampVersions.add(transformStampVersion(stampChronology.getFirstStampVersion(), stampRecord));
        }else{
            throw new IllegalStateException("Missing first Stamp version from the Stamp Chronology");
        }
        if(stampChronology.hasSecondStampVersion()){
            stampVersions.add(transformStampVersion(stampChronology.getSecondStampVersion(), stampRecord));
        }

        StampEntity<? extends StampEntityVersion> stampEntity = StampRecordBuilder.builder(stampRecord).versions(stampVersions).build();
        if(stampEntityConsumer != null){
            stampEntityConsumer.accept((StampEntity<StampEntityVersion>) stampEntity);
        }
        return (StampRecord) stampEntity;
    }
    protected StampVersionRecord transformStampVersion(StampVersion pbStampVersion, StampRecord stampRecord){
         return StampVersionRecordBuilder.builder()
                .chronology(stampRecord)
                .stateNid(nidForConcept(pbStampVersion.getStatusPublicId()))
                .time(pbStampVersion.getTime())
                .authorNid(nidForConcept(pbStampVersion.getAuthorPublicId()))
                .moduleNid(nidForConcept(pbStampVersion.getModulePublicId()))
                .pathNid(nidForConcept(pbStampVersion.getPathPublicId()))
                .build();
    }
    //Field Definition Transformation
    //This creates PBFieldDefinition (line 256 in Tinkar.proto)
    protected FieldDefinitionRecord transformFieldDefinitionRecord(FieldDefinition pbFieldDefinition,
                                                                   int patternVersionStampNid, int patternNid) {
        int meaningNid = nidForConcept(pbFieldDefinition.getMeaningPublicId());
        int purposeNid = nidForConcept(pbFieldDefinition.getPurposePublicId());
        int dataTypeNid = nidForConcept(pbFieldDefinition.getDataTypePublicId());
        return FieldDefinitionRecordBuilder.builder()
                .meaningNid(meaningNid)
                .purposeNid(purposeNid)
                .dataTypeNid(dataTypeNid)
                .patternVersionStampNid(patternVersionStampNid)
                .patternNid(patternNid)
                .build();
    }
    protected Object transformField(Field pbField){
        return transformField(pbField, null);
    }
    //Field Transformation
    //TODO: Use generics in transformField class rather than returning an object
    protected Object transformField(Field pbField, Consumer<StampEntity<StampEntityVersion>> stampEntityConsumer){
        return switch (pbField.getValueCase()) {
            case BOOLEAN_VALUE -> pbField.getBooleanValue();
            case BYTES_VALUE -> pbField.getBytesValue().toByteArray();
            case FLOAT_VALUE -> pbField.getFloatValue();
            case INT_VALUE -> pbField.getIntValue();
            case TIME_VALUE -> DateTimeUtil.epochMsToInstant(pbField.getTimeValue());
            case STRING_VALUE -> pbField.getStringValue();
            case PLANAR_POINT -> transformPlanarPoint(pbField.getPlanarPoint());
            case SPATIAL_POINT -> transformSpatialPoint(pbField.getSpatialPoint());
            case DI_GRAPH -> transformDigraph(pbField.getDiGraph(), stampEntityConsumer);
            case DI_TREE -> transformDiTreeEntity(pbField.getDiTree(), stampEntityConsumer);
            //TODO: A Graph Entity needs to be created here
            case GRAPH -> throw new UnsupportedOperationException("createGraphEntity not implemented");
            case PUBLIC_ID -> transformPublicId(pbField.getPublicId());
            case PUBLIC_IDS -> transformPublicIdList(pbField.getPublicIds());
            case PUBLIC_IDSET -> transformPublicIdSet(pbField.getPublicIdset());
            case INT_TO_INT_MAP -> parsePredecessors(Collections.singletonList(pbField.getIntToIntMap()));
            case INT_TO_MULTIPLE_INT_MAP -> parseSuccessors(Collections.singletonList(pbField.getIntToMultipleIntMap()));
            case VALUE_NOT_SET -> throw new IllegalStateException("PBField value not set");
            case VERTEX_UUID -> transformVertexUUID(pbField.getVertexUuid());
            case VERTEX  -> transformVertexEntity(pbField.getVertex(), stampEntityConsumer);
            case BIG_DECIMAL -> transformBigDecimal(pbField.getBigDecimal());
            case LONG -> transformLong(pbField.getLong());
        };
    }
    protected PublicId transformPublicId(dev.ikm.tinkar.schema.PublicId pbPublicId){
        if (pbPublicId == null || pbPublicId.getUuidsCount() == 0){
            throw new RuntimeException("Exception thrown, null Public ID is present.");
        }
        return PublicIds.of(pbPublicId.getUuidsList().stream()
                .map(UUID::fromString)
                .toList());
    }
    protected IntIdList transformPublicIdList(dev.ikm.tinkar.schema.PublicIdList pbPublicIdList) {
        if(pbPublicIdList.getPublicIdsCount() == 0){
            return IntIds.list.empty();
        }
        int[] nids = new int[pbPublicIdList.getPublicIdsCount()];
        for(int i = 0; i < pbPublicIdList.getPublicIdsCount(); i++) {
            nids[i] = PrimitiveData.nid(transformPublicId(pbPublicIdList.getPublicIds(i)));
        }
        return IntIds.list.of(nids);
    }
    protected IntIdSet transformPublicIdSet(dev.ikm.tinkar.schema.PublicIdSet pbPublicIdSet) {
        if(pbPublicIdSet.getPublicIdsCount() == 0){
            return IntIds.set.empty();
        }
        int[] nids = new int[pbPublicIdSet.getPublicIdsCount()];
        for(int i = 0; i < pbPublicIdSet.getPublicIdsCount(); i++) {
            nids[i] = PrimitiveData.nid(transformPublicId(pbPublicIdSet.getPublicIds(i)));

        }
        return IntIds.set.of(nids);
    }
    protected UUID transformVertexUUID(VertexUUID vertexUUID) {
        return UUID.fromString(vertexUUID.getUuid());
    }
    protected DiGraphEntity<EntityVertex> transformDigraph(DiGraph pbDiGraph, Consumer<StampEntity<StampEntityVersion>> stampEntityConsumer){
        //pbDiGraph.get
        List<IntToMultipleIntMap> PredecessorMapList = pbDiGraph.getPredecessorMapList();
        List<IntToMultipleIntMap> SuccessorMapList = pbDiGraph.getSuccessorMapList();
        return new DiGraphEntity<>(
                (ImmutableList<EntityVertex>) parseRootSequences(pbDiGraph),
                parseVertices(pbDiGraph.getVerticesList(),
                        pbDiGraph.getVerticesCount(), stampEntityConsumer),
                parsePredecessorAndSuccessorMaps(SuccessorMapList,
                        pbDiGraph.getSuccessorMapCount()),
                parsePredecessorAndSuccessorMaps(PredecessorMapList,
                        pbDiGraph.getPredecessorMapCount()));
    }
    //TODO: Created and need to get more context to finish. This the same as Successor parse atm.
    protected ImmutableIntObjectMap<ImmutableIntList> parsePredecessorAndSuccessorMaps(List<IntToMultipleIntMap> predecessorSuccessorMapList, int predecessorSuccessorMapCount) {
        MutableIntObjectMap<ImmutableIntList> mutableIntObjectMap = new IntObjectHashMap<>();
        predecessorSuccessorMapList.forEach(pbIntToMultipleIntMap -> {
            MutableIntList mutableIntList = new IntArrayList();
            pbIntToMultipleIntMap.getTargetsList().forEach(mutableIntList::add);
            mutableIntObjectMap.put(pbIntToMultipleIntMap.getSource(), mutableIntList.toImmutable());
        });
        return mutableIntObjectMap.toImmutable();
    }
    protected DiTreeEntity transformDiTreeEntity(DiTree pbDiTree, Consumer<StampEntity<StampEntityVersion>> stampEntityConsumer) {
        return new DiTreeEntity(
                EntityVertex.make(transformVertexEntity(pbDiTree.getVertices(pbDiTree.getRoot()), stampEntityConsumer)),
                parseVertices(pbDiTree.getVerticesList(), pbDiTree.getVerticesCount(), stampEntityConsumer),
                parseSuccessors(pbDiTree.getSuccessorMapList()),
                parsePredecessors(pbDiTree.getPredecessorMapList()));
    }
    protected ImmutableIntList parseRootSequences(DiGraph pbDiGraph) {
        MutableIntList rootSequences = new IntArrayList(pbDiGraph.getRootsCount());
        pbDiGraph.getRootsList().forEach(rootSequences::add);
        return rootSequences.toImmutable();
    }
    protected ImmutableIntObjectMap<ImmutableIntList> parseSuccessors(List<IntToMultipleIntMap> successorMapList) {
        MutableIntObjectMap<ImmutableIntList> mutableIntObjectMap = new IntObjectHashMap<>();
        successorMapList.forEach(pbIntToMultipleIntMap -> {
            MutableIntList mutableIntList = new IntArrayList();
            pbIntToMultipleIntMap.getTargetsList().forEach(mutableIntList::add);
            mutableIntObjectMap.put(pbIntToMultipleIntMap.getSource(), mutableIntList.toImmutable());
        });
        return mutableIntObjectMap.toImmutable();
    }
    protected static ImmutableIntIntMap parsePredecessors(List<IntToIntMap> predecesorMapList) {
        MutableIntIntMap mutableIntIntMap = new IntIntHashMap();
        predecesorMapList.forEach(pbIntToIntMap -> mutableIntIntMap.put(pbIntToIntMap.getSource(), pbIntToIntMap.getTarget()));
        return mutableIntIntMap.toImmutable();
    }
    protected ImmutableList<EntityVertex> parseVertices(List<Vertex> pbVertices, int pbVertexCount, Consumer<StampEntity<StampEntityVersion>> stampEntityConsumer) {
        MutableList<EntityVertex> vertexMap = Lists.mutable.ofInitialCapacity(pbVertexCount);
        pbVertices.forEach(pbVertex -> vertexMap.add(EntityVertex.make(transformVertexEntity(pbVertex, stampEntityConsumer))));
        return vertexMap.toImmutable();
    }
    protected EntityVertex transformVertexEntity(Vertex pbVertex, Consumer<StampEntity<StampEntityVersion>> stampEntityConsumer){
        UUID vertexID = transformVertexUUID(pbVertex.getVertexUuid());
        MutableIntObjectMap<Object> properties =  IntObjectMaps.mutable.empty();
        EntityVertex storedVertex = EntityVertex.make(vertexID, EntityService.get().nidForPublicId(transformPublicId(pbVertex.getMeaningPublicId())));
        storedVertex.setVertexIndex(pbVertex.getIndex());
        pbVertex.getPropertiesList().forEach(property -> {
            Object propertyObject = transformField(property.getField(), stampEntityConsumer);
            if(propertyObject instanceof PublicId){
                properties.put(createConceptRecord(property).nid(), EntityProxy.Concept.make((PublicId) propertyObject));
            } else {
                properties.put(createConceptRecord(property).nid(),propertyObject);
            }
        });
        storedVertex.setProperties(properties);
        return storedVertex;
    }


    // TODO revist below logic to create conceptRecord. Version info is missing below. Need to figure out to populate it.

    private ConceptRecord createConceptRecord(Vertex.Property property) {
        if(property.getPublicId() == null){
            throw new RuntimeException("Exception thrown, STAMP Public id is null.");
        }
        PublicId conceptPublicId = transformPublicId(property.getPublicId());
        RecordListBuilder<ConceptVersionRecord> conceptVersionRecords = RecordListBuilder.make();
        ConceptRecord conceptRecord;

        if (conceptPublicId.uuidCount() > 0) {
            int conceptNid = nidForConcept(conceptPublicId);
            if (conceptPublicId.uuidCount() > 1) {
                conceptRecord = ConceptRecordBuilder.builder()
                        .leastSignificantBits(conceptPublicId.asUuidArray()[0].getLeastSignificantBits())
                        .mostSignificantBits(conceptPublicId.asUuidArray()[0].getMostSignificantBits())
                        .additionalUuidLongs(UuidUtil.asArray(Arrays.copyOfRange(conceptPublicId.asUuidArray(),
                                1, conceptPublicId.uuidCount())))
                        .nid(conceptNid)
                        .versions(conceptVersionRecords)
                        .build();
            } else {
                conceptRecord = ConceptRecordBuilder.builder()
                        .leastSignificantBits(conceptPublicId.asUuidArray()[0].getLeastSignificantBits())
                        .mostSignificantBits(conceptPublicId.asUuidArray()[0].getMostSignificantBits())
                        .nid(conceptNid)
                        .versions(conceptVersionRecords)
                        .build();
            }
        } else {
            throw new IllegalStateException("missing primordial UUID");
        }


      //  ConceptEntity<? extends ConceptEntityVersion> conceptEntity = ConceptRecordBuilder.builder().versions().build();

        return conceptRecord;
    }

    protected PlanarPoint transformPlanarPoint(dev.ikm.tinkar.schema.PlanarPoint planarPoint){
        return new PlanarPoint(
                planarPoint.getX(),
                planarPoint.getY()
        );
    }
    protected SpatialPoint transformSpatialPoint(dev.ikm.tinkar.schema.SpatialPoint spatialPoint){
        return new SpatialPoint(
                spatialPoint.getX(),
                spatialPoint.getY(),
                spatialPoint.getZ()
        );
    }

    protected BigDecimal transformBigDecimal(dev.ikm.tinkar.schema.BigDecimal bigDecimal) {
        return new BigDecimal(new BigInteger(bigDecimal.getValue()),
                bigDecimal.getScale(),
                new MathContext(bigDecimal.getPrecision()));
    }

    protected Long transformLong(dev.ikm.tinkar.schema.Long value) {
        return value.getValue();
    }

    /**
     * Generates a nid for a component within a pattern context (protobuf version).
     *
     * @param patternNid the pattern nid providing context
     * @param pbPublicId protobuf PublicId of the component
     * @return the generated nid
     */
    protected int nidFor(int patternNid, dev.ikm.tinkar.schema.PublicId pbPublicId) {
        PublicId patternPublicId = EntityHandle.get(patternNid).expectEntity().publicId();
        PublicId componentPublicId = transformPublicId(pbPublicId);
        return ScopedValue
                .where(SCOPED_PATTERN_PUBLICID_FOR_NID, patternPublicId)
                .call(() -> PrimitiveData.nid(componentPublicId));
    }

    /**
     * Generates a nid for a component within a pattern context (protobuf version).
     *
     * @param patternPublicId the pattern PublicId providing context
     * @param pbPublicId protobuf PublicId of the component
     * @return the generated nid
     */
    protected int nidForSemantic(PublicId patternPublicId, dev.ikm.tinkar.schema.PublicId pbPublicId) {
        PublicId componentPublicId = transformPublicId(pbPublicId);
        return ScopedValue
                .where(SCOPED_PATTERN_PUBLICID_FOR_NID, patternPublicId)
                .call(() -> Entity.nid(componentPublicId));
    }

    protected int nidForSemantic(PublicId patternPublicId, PublicId semanticPublicId) {
        return ScopedValue
                .where(SCOPED_PATTERN_PUBLICID_FOR_NID, patternPublicId)
                .call(() -> Entity.nid(semanticPublicId));
    }

    protected int nidForPattern(dev.ikm.tinkar.schema.PublicId pbPublicId) {
        PublicId componentPublicId = transformPublicId(pbPublicId);
        return ScopedValue
                .where(SCOPED_PATTERN_PUBLICID_FOR_NID, EntityBinding.Pattern.pattern())
                .call(() -> Entity.nid(componentPublicId));
    }

    protected int nidForPattern(PublicId patternPublicId) {
        return ScopedValue
                .where(SCOPED_PATTERN_PUBLICID_FOR_NID, EntityBinding.Pattern.pattern())
                .call(() -> Entity.nid(patternPublicId));
    }

    protected int nidForStamp(dev.ikm.tinkar.schema.PublicId pbPublicId) {
        PublicId componentPublicId = transformPublicId(pbPublicId);
        return ScopedValue
                .where(SCOPED_PATTERN_PUBLICID_FOR_NID, EntityBinding.Stamp.pattern())
                .call(() -> Entity.nid(componentPublicId));
    }

    protected int nidForStamp(PublicId stampPublicId) {
        return ScopedValue
                .where(SCOPED_PATTERN_PUBLICID_FOR_NID, EntityBinding.Stamp.pattern())
                .call(() -> Entity.nid(stampPublicId));
    }

    protected int nidForConcept(dev.ikm.tinkar.schema.PublicId pbPublicId) {
        PublicId componentPublicId = transformPublicId(pbPublicId);
        return ScopedValue
                .where(SCOPED_PATTERN_PUBLICID_FOR_NID, EntityBinding.Concept.pattern())
                .call(() -> Entity.nid(componentPublicId));
    }

    protected int nidForConcept(PublicId conceptPublicId) {
        return ScopedValue
                .where(SCOPED_PATTERN_PUBLICID_FOR_NID, EntityBinding.Concept.pattern())
                .call(() -> Entity.nid(conceptPublicId));
    }
}
