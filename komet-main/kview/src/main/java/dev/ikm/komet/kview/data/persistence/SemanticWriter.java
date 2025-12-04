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
package dev.ikm.komet.kview.data.persistence;

import dev.ikm.komet.kview.data.schema.SemanticDetail;
import dev.ikm.tinkar.common.id.PublicId;
import dev.ikm.tinkar.common.id.PublicIds;
import dev.ikm.tinkar.entity.*;
import dev.ikm.tinkar.terms.ConceptFacade;
import dev.ikm.tinkar.terms.TinkarTerm;
import org.eclipse.collections.api.factory.Lists;
import org.eclipse.collections.api.list.MutableList;
import java.util.UUID;


public class SemanticWriter implements Writer {

    private final PublicId stamp;

    public SemanticWriter(PublicId stamp) {
        this.stamp = stamp;
    }

    public void semantic(PublicId semantic, SemanticDetail semanticDetail){
        write(semantic, semanticDetail);
    }

    public void description(PublicId semantic, PublicId referencedComponent, PublicId descriptionType, String text){
        //Assign nids to description components
        final int descriptionTypeNid = EntityService.get().nidForPublicId(descriptionType);
        final ConceptFacade descriptionTypeFacade = ConceptFacade.make(descriptionTypeNid);

        //Create Semantic Detail
        SemanticDetail semanticDetail = new SemanticDetail(TinkarTerm.DESCRIPTION_PATTERN, referencedComponent, () -> {
            //Semantic Field Object values
            MutableList<Object> descriptionFields = Lists.mutable.empty();
            descriptionFields.add(TinkarTerm.ENGLISH_LANGUAGE);
            descriptionFields.add(text);
            descriptionFields.add(TinkarTerm.DESCRIPTION_NOT_CASE_SENSITIVE);
            descriptionFields.add(descriptionTypeFacade);
            return descriptionFields;
        });

        //Write Semantic
        semantic(semantic, semanticDetail);
    }

    public void identifier(PublicId semantic, PublicId referencedComponent, PublicId source, String id){
        //Reference Identifier Pattern
        PublicId identifierPattern = PublicIds.of(UUID.fromString("5d60e14b-c410-5172-9559-3c4253278ae2"));

        //Assign nids to description components
        final int sourceNid = EntityService.get().nidForPublicId(source);
        final ConceptFacade sourceFacade = ConceptFacade.make(sourceNid);

        //Create Semantic Detail
        SemanticDetail semanticDetail = new SemanticDetail(identifierPattern, referencedComponent, () -> {
            //Semantic Field Object values
            MutableList<Object> identifierFields = Lists.mutable.empty();
            identifierFields.add(sourceFacade);
            identifierFields.add(id);
            return identifierFields;
        });

        //Write Semantic
        semantic(semantic, semanticDetail);
    }

    public void usDialect(PublicId semantic, PublicId referencedComponent, PublicId dialectAcceptability){
        //Assign nids to description components
        final int dialectNid = EntityService.get().nidForPublicId(dialectAcceptability);
        final ConceptFacade dialectFacade = ConceptFacade.make(dialectNid);

        //Create Semantic Detail
        SemanticDetail semanticDetail = new SemanticDetail(TinkarTerm.US_DIALECT_PATTERN, referencedComponent, () -> {
            //Semantic Field Object values
            MutableList<Object> dialectFields = Lists.mutable.empty();
            dialectFields.add(dialectFacade);
            return dialectFields;
        });

        //Write Semantic
        semantic(semantic, semanticDetail);
    }

    public void axiomSyntax(PublicId semantic, PublicId referencedComponent, String axiomSyntax){
        //Reference Axiom Syntax Pattern
        PublicId axiomSyntaxPattern = PublicIds.of(UUID.fromString("c0ca180b-aae2-5fa1-9ab7-4a24f2dfe16b"));

        //Create Semantic Detail
        SemanticDetail semanticDetail = new SemanticDetail(axiomSyntaxPattern, referencedComponent, () -> {
            //Semantic Field Object values
            MutableList<Object> axiomSyntaxFields = Lists.mutable.empty();
            axiomSyntaxFields.add(axiomSyntax);
            return axiomSyntaxFields;
        });

        //Write Semantic
        semantic(semantic, semanticDetail);
    }

    public void comment(PublicId semantic, PublicId referencedComponent, String comment, PublicId stamp){
        //Create Semantic Detail
        SemanticDetail semanticDetail = new SemanticDetail(TinkarTerm.COMMENT_PATTERN, referencedComponent, () -> {
            //Semantic Field Object values
            MutableList<Object> commentFields = Lists.mutable.empty();
            commentFields.add(comment);
            return commentFields;
        });

        //Write Semantic
        semantic(semantic, semanticDetail);
    }

    public void membership(PublicId semantic, PublicId referencedComponent, PublicId membershipPattern){
        //Create Semantic Detail
        SemanticDetail semanticDetail = new SemanticDetail(membershipPattern, referencedComponent, () -> {
            //Semantic Field Object values
            MutableList<Object> membershipFields = Lists.mutable.empty();
            return membershipFields;
        });

        //Write Semantic
        semantic(semantic, semanticDetail);
    }

    public void versionControl(PublicId semantic, PublicId referencedComponent, PublicId concept, String formattedTime){
        //Reference Version Control Pattern
        PublicId versionControlPattern = PublicIds.of(UUID.fromString("70f89dd5-2cdb-59bb-bbaa-98527513547c"));

        //Assign nids to description components
        final int conceptNid = EntityService.get().nidForPublicId(concept);
        final ConceptFacade conceptFacade = ConceptFacade.make(conceptNid);

        //Create Semantic Detail
        SemanticDetail semanticDetail = new SemanticDetail(versionControlPattern, referencedComponent, () -> {
            //Semantic Field Object values
            MutableList<Object> versionControlFields = Lists.mutable.empty();
            versionControlFields.add(conceptFacade);
            versionControlFields.add(formattedTime);
            return versionControlFields;
        });

        //Write Semantic
        semantic(semantic, semanticDetail);
    }

//    public void statedAxiom(PublicId semantic, PublicId referencedComponent, List<PublicId> origins){
//        //Create Semantic Detail
//        SemanticDetail semanticDetail = new SemanticDetail(TinkarTerm.EL_PLUS_PLUS_STATED_AXIOMS_PATTERN, referencedComponent, () -> {
//            //Semantic Field Object values
//            MutableList<Object> statedAxiomFields = Lists.mutable.empty();
//            MutableList<EntityVertex> vertexMap = Lists.mutable.empty();
//            MutableIntObjectMap<ImmutableIntList> succesorMap = IntObjectMaps.mutable.empty();
//            MutableIntIntMap predecessorMap = IntIntMaps.mutable.empty();
//            final AtomicInteger vertexIdx = new AtomicInteger(0);
//
//            //Definition Root
//            UUID definitionRootUUID = UUID.randomUUID();
//            MutableMap<ConceptDTO, Object> definitionRootProperty = Maps.mutable.empty();
//            VertexDTO definitionVertexDTO = new VertexDTO(
//                    definitionRootUUID.getMostSignificantBits(),
//                    definitionRootUUID.getLeastSignificantBits(),
//                    vertexIdx.getAndIncrement(),
//                    ConceptDTO.make(TinkarTerm.DEFINITION_ROOT.idString()),
//                    definitionRootProperty.toImmutable());
//            EntityVertex definitionRootVertex = EntityVertex.make(definitionVertexDTO);
//            vertexMap.add(definitionRootVertex);
//
//            //Reference(s)
//            MutableIntList referenceVeterxIdxList = IntLists.mutable.empty();
//            origins.stream()
//                    .map(publicId -> EntityService.get().nidForPublicId(publicId))
//                    .map(ConceptFacade::make)
//                    .forEach(conceptFacade -> {
//                        int referenceIdx = vertexIdx.getAndIncrement();
//                        referenceVeterxIdxList.add(referenceIdx);
//
//                        UUID referenceUUID = UUID.randomUUID();
//                        MutableMap<ConceptDTO, Object> referenceProperty = Maps.mutable.empty();
//                        referenceProperty.put(ConceptDTO.make(TinkarTerm.CONCEPT_REFERENCE.idString()),conceptFacade);
//                        EntityVertex referenceVertex = EntityVertex.make(new VertexDTO(
//                                referenceUUID.getMostSignificantBits(),
//                                referenceUUID.getLeastSignificantBits(),
//                                referenceIdx,
//                                ConceptDTO.make(TinkarTerm.CONCEPT_REFERENCE.idString()),
//                                referenceProperty.toImmutable()));
//                        vertexMap.add(referenceVertex);
//                    });
//
//            //AND
//            UUID andUUID = UUID.randomUUID();
//            MutableMap<ConceptDTO, Object> andProperty = Maps.mutable.empty();
//            EntityVertex andVertex = EntityVertex.make(new VertexDTO(
//                    andUUID.getMostSignificantBits(),
//                    andUUID.getLeastSignificantBits(),
//                    vertexIdx.getAndIncrement(),
//                    ConceptDTO.make(TinkarTerm.AND.idString()),
//                    andProperty.toImmutable()));
//            vertexMap.add(andVertex);
//
//            //Necessary Set
//            UUID necessarySetUUID = UUID.randomUUID();
//            MutableMap<ConceptDTO, Object> necessarySetProperty = Maps.mutable.empty();
//            EntityVertex necessarySetVertex = EntityVertex.make(new VertexDTO(
//                    necessarySetUUID.getMostSignificantBits(),
//                    necessarySetUUID.getLeastSignificantBits(),
//                    vertexIdx.get(),
//                    ConceptDTO.make(TinkarTerm.NECESSARY_SET.idString()),
//                    necessarySetProperty.toImmutable()));
//            vertexMap.add(necessarySetVertex);
//
//            int necessarySetIdx = vertexIdx.get();
//            int andIdx = vertexIdx.get() - 1;
//
//            //Successor Map
//            succesorMap.put(0, IntLists.immutable.of(necessarySetIdx).toImmutable());
//            succesorMap.put(andIdx, referenceVeterxIdxList.toImmutable());
//            succesorMap.put(necessarySetIdx, IntLists.immutable.of(andIdx).toImmutable());
//
//            //Predecessor Map
//            for (int referenceIdx : referenceVeterxIdxList.toArray()) {
//                predecessorMap.put(referenceIdx, andIdx);
//            }
//            predecessorMap.put(andIdx, necessarySetIdx);
//            predecessorMap.put(necessarySetIdx, 0);
//
//            statedAxiomFields.add(new DiTreeEntity(definitionRootVertex, vertexMap.toImmutable(), succesorMap.toImmutable(), predecessorMap.toImmutable()));
//            return statedAxiomFields;
//        });
//
//        //Write Semantic
//        semantic(semantic, semanticDetail);
//    }

    private void write(PublicId semantic, SemanticDetail semanticDetail){
        //Assign primordial UUID from PublicId
        UUID primordialUUID = semantic.asUuidArray()[0];

        //Assign nids for PublicIds
        int semanticNid = EntityService.get().nidForPublicId(semantic);
        int patternNid = EntityService.get().nidForPublicId(semanticDetail.pattern());
        int referencedComponentNid = EntityService.get().nidForPublicId(semanticDetail.referencedComponent());
        int stampNid = EntityService.get().nidForPublicId(stamp);

        //Process additional UUID longs from PublicId
        long[] additionalLongs = createAdditionalLongs(semantic);

        //Create empty version list
        RecordListBuilder<SemanticVersionRecord> versions = RecordListBuilder.make();

        //Create Semantic Chronology
        SemanticRecord semanticRecord = SemanticRecordBuilder.builder()
                .nid(semanticNid)
                .leastSignificantBits(primordialUUID.getLeastSignificantBits())
                .mostSignificantBits(primordialUUID.getMostSignificantBits())
                .additionalUuidLongs(additionalLongs)
                .patternNid(patternNid)
                .referencedComponentNid(referencedComponentNid)
                .versions(versions.toImmutable())
                .build();

        //Create Semantic Version
        versions.add(SemanticVersionRecordBuilder.builder()
                .chronology(semanticRecord)
                .stampNid(stampNid)
                .fieldValues(semanticDetail.fieldsSupplier().get().toImmutable())
                .build());

        //Rebuild the Semantic with the now populated version data
        SemanticEntity<? extends SemanticEntityVersion> semanticEntity = SemanticRecordBuilder
                .builder(semanticRecord)
                .versions(versions.toImmutable()).build();
        EntityService.get().putEntity(semanticEntity);
    }
}
