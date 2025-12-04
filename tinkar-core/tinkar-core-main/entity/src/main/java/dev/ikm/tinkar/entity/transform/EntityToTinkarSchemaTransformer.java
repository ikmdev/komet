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

import com.google.protobuf.ByteString;
import dev.ikm.tinkar.common.id.IntIdList;
import dev.ikm.tinkar.common.id.IntIdSet;
import dev.ikm.tinkar.common.id.PublicIdList;
import dev.ikm.tinkar.common.id.PublicIdSet;
import dev.ikm.tinkar.common.id.VertexId;
import dev.ikm.tinkar.common.util.time.DateTimeUtil;
import dev.ikm.tinkar.component.Component;
import dev.ikm.tinkar.component.graph.DiGraph;
import dev.ikm.tinkar.component.graph.DiTree;
import dev.ikm.tinkar.component.graph.Vertex;
import dev.ikm.tinkar.component.location.PlanarPoint;
import dev.ikm.tinkar.component.location.SpatialPoint;
import dev.ikm.tinkar.entity.ConceptEntity;
import dev.ikm.tinkar.entity.ConceptEntityVersion;
import dev.ikm.tinkar.entity.Entity;
import dev.ikm.tinkar.entity.EntityService;
import dev.ikm.tinkar.entity.FieldDefinitionRecord;
import dev.ikm.tinkar.entity.PatternEntity;
import dev.ikm.tinkar.entity.PatternEntityVersion;
import dev.ikm.tinkar.entity.SemanticEntity;
import dev.ikm.tinkar.entity.SemanticEntityVersion;
import dev.ikm.tinkar.entity.StampEntity;
import dev.ikm.tinkar.entity.StampVersionRecord;
import dev.ikm.tinkar.entity.graph.DiGraphEntity;
import dev.ikm.tinkar.entity.graph.DiTreeEntity;
import dev.ikm.tinkar.entity.graph.EntityVertex;
import dev.ikm.tinkar.schema.ConceptChronology;
import dev.ikm.tinkar.schema.ConceptVersion;
import dev.ikm.tinkar.schema.Field;
import dev.ikm.tinkar.schema.FieldDefinition;
import dev.ikm.tinkar.schema.IntToIntMap;
import dev.ikm.tinkar.schema.IntToMultipleIntMap;
import dev.ikm.tinkar.schema.PatternChronology;
import dev.ikm.tinkar.schema.PatternVersion;
import dev.ikm.tinkar.schema.PublicId;
import dev.ikm.tinkar.schema.SemanticChronology;
import dev.ikm.tinkar.schema.SemanticVersion;
import dev.ikm.tinkar.schema.StampChronology;
import dev.ikm.tinkar.schema.StampVersion;
import dev.ikm.tinkar.schema.TinkarMsg;
import dev.ikm.tinkar.schema.VertexUUID;
import dev.ikm.tinkar.terms.ConceptFacade;
import org.eclipse.collections.api.RichIterable;
import org.eclipse.collections.api.list.ImmutableList;
import org.eclipse.collections.api.list.primitive.ImmutableIntList;
import org.eclipse.collections.api.map.primitive.ImmutableIntIntMap;
import org.eclipse.collections.api.map.primitive.ImmutableIntObjectMap;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

/**
 * The entityTransformer class is responsible for transformer a entity of a certain data type to a
 * Protobuf message object.
 */
public class EntityToTinkarSchemaTransformer {

    private static EntityToTinkarSchemaTransformer INSTANCE;

    private EntityToTinkarSchemaTransformer(){
    }

    public static EntityToTinkarSchemaTransformer getInstance(){
        if(INSTANCE == null){
            synchronized (EntityToTinkarSchemaTransformer.class){
                if (INSTANCE == null){
                    INSTANCE = new EntityToTinkarSchemaTransformer();
                }
            }
        }
        return INSTANCE;
    }

    public static EntityToTinkarSchemaTransformer getIndependentInstance() {
        return new EntityToTinkarSchemaTransformer();
    }

    /**
     * This method takes in an entity and is matched on its entity type based on the type of message. It is then transformed into a PB message.
     * @param entity to be transformed to PB message
     * @return a Protobuf message of entity data type.
     */
    public TinkarMsg transform(Entity entity){
        return switch (entity.entityDataType()){
            case CONCEPT_CHRONOLOGY -> createPBConceptChronology((ConceptEntity<ConceptEntityVersion>) entity);
            case SEMANTIC_CHRONOLOGY -> createPBSemanticChronology((SemanticEntity<SemanticEntityVersion>) entity);
            case PATTERN_CHRONOLOGY -> createPBPatternChronology((PatternEntity<PatternEntityVersion>) entity);
            case STAMP -> TinkarMsg.newBuilder().setStampChronology(createPBStampChronology((StampEntity<StampVersionRecord>) entity)).build();
            default -> throw new IllegalStateException("not expecting" + entity.versionDataType());
        };
    }

    protected TinkarMsg createPBConceptChronology(ConceptEntity<ConceptEntityVersion> conceptEntity){
        return TinkarMsg.newBuilder()
                .setConceptChronology(ConceptChronology.newBuilder()
                        .setPublicId(createPBPublicId(conceptEntity.publicId()))
                        .addAllConceptVersions(createPBConceptVersions(conceptEntity.versions()))
                        .build())
                .build();
    }

    protected List<ConceptVersion> createPBConceptVersions(ImmutableList<ConceptEntityVersion> conceptEntityVersions){
        if(conceptEntityVersions.size() == 0){
            throw new RuntimeException("Exception thrown, ImmutableList contains zero Entity Concept Versions");
        }
        final ArrayList<ConceptVersion> pbConceptVersions = new ArrayList<>();
        conceptEntityVersions.forEach(conceptEntityVersion -> pbConceptVersions.add(ConceptVersion.newBuilder()
                .setStampChronologyPublicId(createPBPublicId(conceptEntityVersion.stamp().publicId()))
                .build()));
        return pbConceptVersions;
    }

    protected TinkarMsg createPBSemanticChronology(SemanticEntity<SemanticEntityVersion> semanticEntity){
        if(semanticEntity.versions().size() == 0){
            throw new RuntimeException("Exception thrown, Semantic Chronology can't contain zero versions");
        }
        if(semanticEntity.referencedComponent() == null){
            throw new RuntimeException("Exception thrown, Semantic Chronology " + semanticEntity + " has null referenced component");
        }
        return TinkarMsg.newBuilder()
                .setSemanticChronology(SemanticChronology.newBuilder()
                        .setPublicId(createPBPublicId(semanticEntity.publicId()))
                        .setReferencedComponentPublicId(createPBPublicId(semanticEntity.referencedComponent().publicId()))
                        .setPatternForSemanticPublicId(createPBPublicId(semanticEntity.pattern().publicId()))
                        .addAllSemanticVersions(createPBSemanticVersions(semanticEntity.versions()))
                        .build())
                .build();
    }

    protected List<SemanticVersion> createPBSemanticVersions(ImmutableList<SemanticEntityVersion> semanticEntityVersions) {
        if(semanticEntityVersions.size() == 0){
            throw new RuntimeException("Exception thrown, ImmutableList contains zero Entity Semantic Versions");
        }
        return semanticEntityVersions.stream()
                .map(semanticEntityVersion -> SemanticVersion.newBuilder()
                    .setStampChronologyPublicId(createPBPublicId(semanticEntityVersion.stamp().publicId()))
                    .addAllFields(createPBFields(semanticEntityVersion.fieldValues()))
                    .build())
                .toList();
    }
        protected TinkarMsg createPBPatternChronology(PatternEntity<PatternEntityVersion> patternEntity){
        return TinkarMsg.newBuilder()
                .setPatternChronology(PatternChronology.newBuilder()
                        .setPublicId(createPBPublicId(patternEntity.publicId()))
                        .addAllPatternVersions(createPBPatternVersions(patternEntity.versions()))
                        .build())
                .build();
    }

    protected List<PatternVersion> createPBPatternVersions(ImmutableList<PatternEntityVersion> patternEntityVersions){
        if(patternEntityVersions.size() == 0){
            throw new RuntimeException("Exception thrown, ImmutableList contains zero Entity Pattern Versions");
        }
        final ArrayList<PatternVersion> pbPatternVersions = new ArrayList<>();
        patternEntityVersions.forEach(patternEntityVersion -> pbPatternVersions
                .add(PatternVersion.newBuilder()
                .setStampChronologyPublicId(createPBPublicId(patternEntityVersion.stamp().publicId()))
                .setReferencedComponentPurposePublicId(createPBPublicId(patternEntityVersion.semanticPurpose().publicId()))
                .setReferencedComponentMeaningPublicId(createPBPublicId(patternEntityVersion.semanticMeaning().publicId()))
                .addAllFieldDefinitions(createPBFieldDefinitions((ImmutableList<FieldDefinitionRecord>) patternEntityVersion.fieldDefinitions()))
                .build()));
        return pbPatternVersions;
    }

    protected StampChronology createPBStampChronology(StampEntity<StampVersionRecord> stampEntity){
        StampChronology.Builder stampBuilder = StampChronology.newBuilder()
                .setPublicId(createPBPublicId(stampEntity.publicId()));
        switch (stampEntity.versions().size()){
            case 2: stampBuilder.setSecondStampVersion(createPBStampVersion(stampEntity.versions().get(1)));
            case 1: stampBuilder.setFirstStampVersion(createPBStampVersion(stampEntity.versions().get(0)));
                    break;
            default: throw new RuntimeException("Unexpected number of version size: " + stampEntity.versions().size() +
                    " for stamp entity: " + stampEntity.nid());
        }
        return stampBuilder.build();
    }

    // TODO: Check with Andrew, made this method return a singlar StampVersionRecord rather than a list
    protected StampVersion createPBStampVersion(StampVersionRecord stampVersionRecord){
        return StampVersion.newBuilder()
                    .setStatusPublicId(createPBPublicId(stampVersionRecord.state().publicId()))
                    .setTime(stampVersionRecord.time())
                    .setAuthorPublicId(createPBPublicId(stampVersionRecord.author().publicId()))
                    .setModulePublicId(createPBPublicId(stampVersionRecord.module().publicId()))
                    .setPathPublicId(createPBPublicId(stampVersionRecord.path().publicId()))
                    .build();
    }

    protected FieldDefinition createPBFieldDefinition(FieldDefinitionRecord fieldDefinitionRecord){
        return FieldDefinition.newBuilder()
                .setMeaningPublicId(createPBPublicId(fieldDefinitionRecord.meaning().publicId()))
                .setDataTypePublicId(createPBPublicId(fieldDefinitionRecord.dataType().publicId()))
                .setPurposePublicId(createPBPublicId(fieldDefinitionRecord.purpose().publicId()))
                .build();
    }

    protected List<FieldDefinition> createPBFieldDefinitions
            (ImmutableList<FieldDefinitionRecord> fieldDefinitionRecords){
        final ArrayList<FieldDefinition> pbFieldDefinitions = new ArrayList<>();
        fieldDefinitionRecords.forEach(fieldDefinitionRecord -> pbFieldDefinitions
                .add(createPBFieldDefinition(fieldDefinitionRecord)));
        return pbFieldDefinitions;
    }

    //TODO: Add in Planar/Spatial point into the switch statement.
    //TODO: Notify Andrew Semantic, Stamp, Concept, and Pattern were removed from field's below
    protected Field createPBField(Object obj){
        return switch (obj){
            case String s -> toPBString(s);
            case Boolean bool -> toPBBool(bool);
            case Integer i -> toPBInteger(i);
            case Float f -> toPBFloat(f);
            case byte[] bytes -> toPBByte(bytes);
            case Instant instant -> toPBInstant(instant);
            case Component component -> toPBComponent(component);
            case VertexId vertexUUID -> toVertexUUID(vertexUUID);
            case dev.ikm.tinkar.common.id.PublicId publicId -> toPBPublicId(publicId);
            case PublicIdList publicIdList -> toPBPublicIdList(publicIdList);
            case PublicIdSet publicIdSet -> toPBPublicIdSet(publicIdSet);
            case DiTree diTree -> toPBDiTree(diTree);
            case DiGraph diGraph -> toPBDiGraph(diGraph);
            case Vertex vertex -> toVertex(vertex);
            case PlanarPoint planarPoint -> toPlanarPoint(planarPoint);
            case SpatialPoint spatialPoint -> toSpatialPoint(spatialPoint);
            //TODO: we do not have a create undirected graph method [Ask Andrew]
//            case dev.ikm.tinkar.component.graph.Graph graph -> createGraph
            case IntIdList intIdList -> toPBPublicIdList(intIdList);
            case IntIdSet intIdSet -> toPBPublicIdSet(intIdSet);
            case BigDecimal bigDecimal -> toPBBigDecimal(bigDecimal);
            case Long l -> toPBLong(l);
            case null, default -> throw new IllegalStateException("Unknown or null field object for: " + obj + ", " +obj.getClass());
        };
    }

    protected Field toPBBool(boolean value) {
        return Field.newBuilder().setBooleanValue(value).build();
    }
    protected Field toPBByte(byte[] value) {
        return Field.newBuilder().setBytesValue(ByteString.copyFrom(value)).build();
    }
    protected Field toPBFloat(float value) {
        return Field.newBuilder().setFloatValue(value).build();
    }
    protected Field toPBInteger(Integer value) {
        return Field.newBuilder().setIntValue(value).build();
    }

    protected Field toPBComponent(Component value) {
        return Field.newBuilder().setPublicId(createPBPublicId(value.publicId())).build();
    }
    protected Field toPBPublicId(dev.ikm.tinkar.common.id.PublicId value) {
        return Field.newBuilder().setPublicId(createPBPublicId(value)).build();
    }
    protected Field toPBPublicIdList(PublicIdList value) {
        return Field.newBuilder().setPublicIds(createPBPublicIdList(value)).build();
    }
    protected Field toPBPublicIdSet(PublicIdSet value) {
        return Field.newBuilder().setPublicIdset(createPBPublicIdSet(value)).build();
    }
    protected Field toPBString(String value) {
        return Field.newBuilder().setStringValue(value).build();
    }
    protected Field toPBInstant(Instant value) {
        return Field.newBuilder().setTimeValue(DateTimeUtil.instantToEpochMs(value)).build();
    }
    protected Field toPBPublicIdList(IntIdList value) {
        //TODO: Figure out what are the Int ID's getting written
        return Field.newBuilder().setPublicIds(createPBPublicIdList(value)).build();
    }
    protected Field toPBPublicIdSet(IntIdSet value) {
        return Field.newBuilder().setPublicIdset(createPBPublicIdSet(value)).build();
    }
    protected Field toPBDiTree(DiTree value) {
        return Field.newBuilder().setDiTree(createPBDiTree((DiTreeEntity) value)).build();
    }
    protected Field toPBDiGraph(DiGraph value) {
        return Field.newBuilder().setDiGraph(createPBDiGraph((DiGraphEntity<EntityVertex>) value)).build();
    }

    protected Field toVertexUUID(VertexId value) {
        return Field.newBuilder().setVertexUuid(createPBVertexUUID(value)).build();
    }

    protected Field toVertex(Vertex value) {
        return Field.newBuilder().setVertex(createPBVertex((EntityVertex) value)).build();
    }

    protected Field toPlanarPoint(PlanarPoint value) {
        return Field.newBuilder().setPlanarPoint(createPBPlanaPoint(value)).build();
    }

    protected Field toSpatialPoint(SpatialPoint value) {
        return Field.newBuilder().setSpatialPoint(createPBSpatialPoint(value)).build();
    }

    protected Field toPBBigDecimal(BigDecimal value) {
        return Field.newBuilder().setBigDecimal(createPBBigDecimal(value)).build();
    }

    protected Field toPBLong(Long value) {
        return Field.newBuilder().setLong(createPBLong(value)).build();
    }

    protected List<Field> createPBFields(ImmutableList<Object> objects){
        final ArrayList<Field> pbFields = new ArrayList<>();
        for(Object obj : objects){
            pbFields.add(createPBField(obj));
        }
        return pbFields;
    }

    //TODO: PBConcept on its own doesnt exist, because PBConcept is its own type of message (like semantic or pattern).
    protected ConceptChronology createPBConcept(dev.ikm.tinkar.common.id.PublicId publicId){
        return ConceptChronology.newBuilder()
                .setPublicId(createPBPublicId(publicId))
                .build();
    }

    protected PublicId createPBPublicId(dev.ikm.tinkar.common.id.PublicId publicId){
        if (publicId.uuidCount() == 0){
            throw new RuntimeException("Exception thrown, empty Public ID is present [entity transformer].");
        }
        return PublicId.newBuilder()
                .addAllUuids(publicId.asUuidList().stream()
                        .map(UUID::toString)
                        .toList())
                .build();
    }

    protected dev.ikm.tinkar.schema.PublicIdList createPBPublicIdList(PublicIdList publicIdList){
        ArrayList<PublicId> pbPublicIds = new ArrayList<>();
        for(dev.ikm.tinkar.common.id.PublicId publicId : publicIdList.toIdArray()){
            pbPublicIds.add(createPBPublicId(publicId));
        }
        return dev.ikm.tinkar.schema.PublicIdList.newBuilder()
                .addAllPublicIds(pbPublicIds)
                .build();
    }

    protected dev.ikm.tinkar.schema.PublicIdSet createPBPublicIdSet(PublicIdSet publicIdSet){
        ArrayList<PublicId> pbPublicIds = new ArrayList<>();
        for(dev.ikm.tinkar.common.id.PublicId publicId : publicIdSet.toIdArray()){
            pbPublicIds.add(createPBPublicId(publicId));
        }
        return dev.ikm.tinkar.schema.PublicIdSet.newBuilder()
                .addAllPublicIds(pbPublicIds)
                .build();
    }

    protected dev.ikm.tinkar.schema.PublicIdList createPBPublicIdList(IntIdList intIdList){
        List<PublicId> pbPublicIds = new ArrayList<>();
        intIdList.forEach(nid -> pbPublicIds.add(createPBPublicId(EntityService.get().getEntityFast(nid).publicId())));
        return dev.ikm.tinkar.schema.PublicIdList.newBuilder()
                .addAllPublicIds(pbPublicIds)
                .build();
    }

    protected dev.ikm.tinkar.schema.PublicIdSet createPBPublicIdSet(IntIdSet intIdSet){
        List<PublicId> pbPublicIds = new ArrayList<>();
        intIdSet.forEach(nid -> pbPublicIds.add(createPBPublicId(EntityService.get().getEntityFast(nid).publicId())));
        return dev.ikm.tinkar.schema.PublicIdSet.newBuilder()
                .addAllPublicIds(pbPublicIds)
                .build();
    }

    protected dev.ikm.tinkar.schema.DiGraph createPBDiGraph(DiGraphEntity<EntityVertex> diGraph){
         //List all PBVertex TODO-aks8m: Why is this called a map when it's a list??
        ArrayList<dev.ikm.tinkar.schema.Vertex> pbVertices = new ArrayList<>();
        diGraph.vertexMap().forEach(vertex -> pbVertices.add(createPBVertex(vertex)));
        //Int Root Sequences TODO-aks8m: Is this a list of root Vertex's (then need to change protobuf)
        ArrayList<Integer> pbRoots = new ArrayList<>();
        diGraph.roots().forEach(root -> pbRoots.add(root.vertexIndex()));
        List<IntToMultipleIntMap> pbSuccessorsMap = createPBIntToMultipleIntMaps(diGraph.successorMap().toImmutable());
        List<IntToMultipleIntMap> pbPredecessorsMap = createPBIntToMultipleIntMaps(diGraph.predecessorMap().toImmutable());
        return dev.ikm.tinkar.schema.DiGraph.newBuilder()
                .addAllVertices(pbVertices)
                .addAllRoots(pbRoots)
                .addAllSuccessorMap(pbSuccessorsMap)
                .addAllPredecessorMap(pbPredecessorsMap)
                .build();
    }

    protected dev.ikm.tinkar.schema.DiTree createPBDiTree(DiTreeEntity diTree){
        ArrayList<dev.ikm.tinkar.schema.Vertex> pbVertices = new ArrayList<>();
        diTree.vertexMap().forEach(vertex -> pbVertices.add(createPBVertex(vertex)));
        Vertex pbVertexRoot = diTree.root();
        List<IntToIntMap> pbPredecesorMap = createPBIntToIntMaps(diTree.predecessorMap().toImmutable());
        List<IntToMultipleIntMap> pbSuccessorMap = createPBIntToMultipleIntMaps(diTree.successorMap().toImmutable());
        return dev.ikm.tinkar.schema.DiTree.newBuilder()
                .addAllVertices(pbVertices)
                .setRoot(pbVertexRoot.vertexIndex())
                .addAllPredecessorMap(pbPredecesorMap)
                .addAllSuccessorMap(pbSuccessorMap)
                .build();
    }

    protected dev.ikm.tinkar.schema.Vertex createPBVertex(EntityVertex vertex){
        int pbVertexIndex = vertex.vertexIndex();
        RichIterable<ConceptFacade> vertexKeys = vertex.propertyKeys();
        ArrayList<dev.ikm.tinkar.schema.Vertex.Property> pbPropertyList = new ArrayList<>();
        vertexKeys.forEach(concept -> pbPropertyList.add(dev.ikm.tinkar.schema.Vertex.Property.newBuilder()
                .setPublicId(createPBPublicId(concept.publicId()))
                .setField(createPBField(vertex.propertyFast(concept)))
                .build()));
        return dev.ikm.tinkar.schema.Vertex.newBuilder()
                .setVertexUuid(createPBVertexUUID(vertex.vertexId()))
                .setIndex(pbVertexIndex)
                .setMeaningPublicId(createPBPublicId(EntityService.get().getEntityFast(vertex.getMeaningNid()).publicId()))
                .addAllProperties(pbPropertyList)
                .build();
    }

    protected VertexUUID createPBVertexUUID(dev.ikm.tinkar.common.id.VertexId vertexId){
        return VertexUUID.newBuilder()
                .setUuid(vertexId.asUuid().toString())
                .build();
    }
    protected List<IntToIntMap> createPBIntToIntMaps(ImmutableIntIntMap intToIntMap) {
        ArrayList<IntToIntMap> pbIntToIntMaps = new ArrayList<>();
        intToIntMap.forEachKeyValue((source, target) -> pbIntToIntMaps.add(IntToIntMap.newBuilder()
                .setSource(source)
                .setTarget(target)
                .build()
        ));
        return pbIntToIntMaps;
    }
    protected List<IntToMultipleIntMap> createPBIntToMultipleIntMaps(ImmutableIntObjectMap intToMultipleIntMap){
        List<IntToMultipleIntMap> pbIntToMultipleIntMaps = new ArrayList<>();
        intToMultipleIntMap.keySet().forEach(source -> {
            final ArrayList<Integer> targets = new ArrayList<>();
            ((ImmutableIntList) intToMultipleIntMap.get(source)).forEach(targets::add);
            pbIntToMultipleIntMaps.add(IntToMultipleIntMap.newBuilder()
                    .setSource(source)
                    .addAllTargets(targets)
                    .build()
            );
        });
        return  pbIntToMultipleIntMaps;
    }

    protected dev.ikm.tinkar.schema.PlanarPoint createPBPlanaPoint(PlanarPoint planarPoint){
        return dev.ikm.tinkar.schema.PlanarPoint.newBuilder()
                .setX(planarPoint.x())
                .setY(planarPoint.y())
                .build();
    }

    protected dev.ikm.tinkar.schema.SpatialPoint createPBSpatialPoint(SpatialPoint spatialPoint){
        return dev.ikm.tinkar.schema.SpatialPoint.newBuilder()
                .setX(spatialPoint.x())
                .setY(spatialPoint.y())
                .setZ(spatialPoint.z())
                .build();
    }

    protected dev.ikm.tinkar.schema.BigDecimal createPBBigDecimal(BigDecimal bigDecimal) {
        return dev.ikm.tinkar.schema.BigDecimal.newBuilder()
                .setScale(bigDecimal.scale())
                .setPrecision(bigDecimal.precision())
                .setValue(bigDecimal.unscaledValue().toString())
                .build();
    }

    protected dev.ikm.tinkar.schema.Long createPBLong(Long value) {
        return dev.ikm.tinkar.schema.Long.newBuilder()
                .setValue(value)
                .build();
    }
}
