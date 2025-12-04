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
package dev.ikm.tinkar.integration.provider.ephemeral;

import dev.ikm.tinkar.common.service.PrimitiveData;
import dev.ikm.tinkar.entity.ConceptRecord;
import dev.ikm.tinkar.entity.ConceptRecordBuilder;
import dev.ikm.tinkar.entity.ConceptVersionRecord;
import dev.ikm.tinkar.entity.ConceptVersionRecordBuilder;
import dev.ikm.tinkar.entity.Entity;
import dev.ikm.tinkar.entity.EntityService;
import dev.ikm.tinkar.entity.RecordListBuilder;
import dev.ikm.tinkar.entity.StampEntityVersion;
import dev.ikm.tinkar.entity.graph.DiTreeEntity;
import dev.ikm.tinkar.entity.graph.EntityVertex;
import dev.ikm.tinkar.entity.graph.adaptor.axiom.LogicalExpression;
import dev.ikm.tinkar.ext.lang.owl.SctOwlUtilities;
import dev.ikm.tinkar.integration.TestConstants;
import dev.ikm.tinkar.integration.helper.DataStore;
import dev.ikm.tinkar.integration.helper.TestHelper;
import dev.ikm.tinkar.terms.EntityBinding;
import dev.ikm.tinkar.terms.TinkarTerm;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.atomic.AtomicLong;

import static dev.ikm.tinkar.common.service.PrimitiveData.SCOPED_PATTERN_PUBLICID_FOR_NID;
import static org.junit.jupiter.api.Assertions.assertEquals;

public class OwlToLogicalAxiomIT {
    private static final Logger LOG = LoggerFactory.getLogger(OwlToLogicalAxiomIT.class);

    @BeforeAll
    public static void beforeAll() {
        TestHelper.startDataBase(DataStore.EPHEMERAL_STORE);
        TestHelper.loadDataFile(TestConstants.PB_STARTER_DATA_REASONED);
    }

    @AfterAll
    public static void afterAll() {
        TestHelper.stopDatabase();
    }

    private boolean datastoreEmpty() {
        AtomicLong datastoreSize = new AtomicLong();
        PrimitiveData.get().forEachConceptNid((ignored) -> datastoreSize.incrementAndGet());
        return datastoreSize.get() == 0;
    }

    private List<Entity> addIfAbsent(List<UUID> entitiesToAdd) {
        List<Entity> concepts = new ArrayList<>();
        StampEntityVersion defaultStamp = (StampEntityVersion) EntityService.get().getEntity(PrimitiveData.NONEXISTENT_STAMP_UUID).get().versions().get(0);
        if (datastoreEmpty()) {
            EntityService.get().putEntity(ConceptRecord.build(TinkarTerm.MEANING.asUuidArray()[0], defaultStamp));
            EntityService.get().putEntity(ConceptRecord.build(TinkarTerm.DEFINITION_ROOT.asUuidArray()[0], defaultStamp));
            EntityService.get().putEntity(ConceptRecord.build(TinkarTerm.CONCEPT_REFERENCE.asUuidArray()[0], defaultStamp));
            EntityService.get().putEntity(ConceptRecord.build(TinkarTerm.AND.asUuidArray()[0], defaultStamp));
            EntityService.get().putEntity(ConceptRecord.build(TinkarTerm.NECESSARY_SET.asUuidArray()[0], defaultStamp));
            EntityService.get().putEntity(ConceptRecord.build(TinkarTerm.ROLE.asUuidArray()[0], defaultStamp));
            EntityService.get().putEntity(ConceptRecord.build(TinkarTerm.ROLE_TYPE.asUuidArray()[0], defaultStamp));
            EntityService.get().putEntity(ConceptRecord.build(TinkarTerm.ROLE_GROUP.asUuidArray()[0], defaultStamp));
        }
        for (UUID uuid : entitiesToAdd) {
            EntityService.get().getEntity(uuid).ifPresentOrElse(concepts::add,
                    () -> {
                        Entity conceptEntity = ConceptRecord.build(uuid, defaultStamp);
                        if (uuid.equals(UUID.fromString("051fbfed-3c40-3130-8c09-889cb7b7b5b6"))) {
                            RecordListBuilder<ConceptVersionRecord> versions = RecordListBuilder.make();
                            int conceptNid = ScopedValue
                                    .where(SCOPED_PATTERN_PUBLICID_FOR_NID, EntityBinding.Concept.pattern().publicId())
                                    .call(() -> PrimitiveData.nid(uuid));
                            ConceptRecord conceptRecord = ConceptRecordBuilder.builder()
                                    .nid(conceptNid)
                                    .mostSignificantBits(uuid.getMostSignificantBits())
                                    .leastSignificantBits(uuid.getLeastSignificantBits())
                                    .additionalUuidLongs(new long[] {
                                            TinkarTerm.ROLE_GROUP.asUuidArray()[0].getMostSignificantBits(),
                                            TinkarTerm.ROLE_GROUP.asUuidArray()[0].getLeastSignificantBits()
                                    })
                                    .versions(versions.toImmutable())
                                    .build();

                            versions.add(ConceptVersionRecordBuilder.builder()
                                    .chronology(conceptRecord)
                                    .stampNid(defaultStamp.nid())
                                    .build());

                            conceptEntity = ConceptRecordBuilder.builder(conceptRecord).versions(versions.toImmutable()).build();
                        }
                        concepts.add(conceptEntity);
                        EntityService.get().putEntity(conceptEntity);
            });
        }
        return concepts;
    }

    private EntityVertex makeRoleVertex(Entity roleType){
        EntityVertex roleVertex = EntityVertex.make(TinkarTerm.ROLE.nid());
        roleVertex.putUncommittedProperty(TinkarTerm.ROLE_TYPE.nid(), roleType);
        roleVertex.putUncommittedProperty(TinkarTerm.ROLE_OPERATOR.nid(), TinkarTerm.EXISTENTIAL_RESTRICTION);
        roleVertex.commitProperties();
        return roleVertex;
    }

    private EntityVertex makeConceptReferenceVertex(Entity conceptReference){
        EntityVertex conceptReferenceVertex = EntityVertex.make(TinkarTerm.CONCEPT_REFERENCE.nid());
        conceptReferenceVertex.putUncommittedProperty(TinkarTerm.CONCEPT_REFERENCE.nid(), conceptReference);
        conceptReferenceVertex.commitProperties();
        return conceptReferenceVertex;
    }

    @Test
    public void testSimpleOwlToLogicalExpression() throws IOException {
        String owlExpression = "SubClassOf(:[19be696b-c13c-3a20-8e60-11a92a00d640] :[517588a6-d4ae-3469-bbc1-b30e449c3b5b])";

        List<UUID> owlAxiomUuids = List.of(
                UUID.fromString("19be696b-c13c-3a20-8e60-11a92a00d640"),
                UUID.fromString("517588a6-d4ae-3469-bbc1-b30e449c3b5b")
        );
        List<Entity> owlAxiomConcepts = addIfAbsent(owlAxiomUuids);

        LogicalExpression logicalExpression = SctOwlUtilities.sctToLogicalExpression(owlExpression, "");

        // Initialize Vertices for Simple Axiom DiTree
        EntityVertex definitionRootVertex = EntityVertex.make(TinkarTerm.DEFINITION_ROOT.nid());
        EntityVertex andVertex = EntityVertex.make(TinkarTerm.AND.nid());
        EntityVertex necessarySetVertex = EntityVertex.make(TinkarTerm.NECESSARY_SET.nid());
        EntityVertex referenceVertex = makeConceptReferenceVertex(owlAxiomConcepts.get(1));

        // Build Expected Axiom DiTree
        DiTreeEntity.Builder dteBuilder = DiTreeEntity.builder();
        dteBuilder.setRoot(definitionRootVertex);
        dteBuilder.addVertex(referenceVertex);
        dteBuilder.addVertex(andVertex);
        dteBuilder.addVertex(necessarySetVertex);
        dteBuilder.addEdge(necessarySetVertex.vertexIndex(), definitionRootVertex.vertexIndex());
        dteBuilder.addEdge(andVertex.vertexIndex(), necessarySetVertex.vertexIndex());
        dteBuilder.addEdge(referenceVertex.vertexIndex(), andVertex.vertexIndex());
        DiTreeEntity expectedAxiomTree = dteBuilder.build();

        assertEquals(expectedAxiomTree, (DiTreeEntity) logicalExpression.sourceGraph(), "Owl Express Tree and Logical Axiom Tree do not evaluate to the same value.");
    }

    @Test
    public void testComplexOwlToLogicalExpression() throws IOException {
        String owlExpression = "SubClassOf(:[28152255-eeb8-3ec6-a3cf-8cbbd4684c4c] ObjectIntersectionOf(:[e54fe220-2aa6-385f-8c1a-22374f2328aa] :[2ffdca5d-ca83-304e-a781-597ea34d6f35] :[0cb15147-c04a-33a9-8b65-66d3501c5ae0] ObjectSomeValuesFrom(:[051fbfed-3c40-3130-8c09-889cb7b7b5b6] ObjectIntersectionOf(ObjectSomeValuesFrom(:[3161e31b-7d00-33d9-8cbd-9c33dc153aae] :[3d3c4a6a-98d6-3a7c-9e1b-7fabf61e5ca5]) ObjectSomeValuesFrom(:[d99e2a70-243d-3bf2-967a-faee3265102b] :[050cdd53-c5b0-3bca-ab11-605aa0f89519]) ObjectSomeValuesFrom(:[3a6d919d-6c25-3aae-9bc3-983ead83a928] :[d556592d-ee5f-3ff4-a893-bf23c007c5c0]) ObjectSomeValuesFrom(:[52542cae-017c-3fc4-bff0-97b7f620db28] :[637ab7d7-d8f3-3ce3-86b2-615f1dcc78a1]))) ObjectSomeValuesFrom(:[051fbfed-3c40-3130-8c09-889cb7b7b5b6] ObjectIntersectionOf(ObjectSomeValuesFrom(:[3161e31b-7d00-33d9-8cbd-9c33dc153aae] :[95d78b31-52e6-31e7-8eb4-c24490e1f0f4]) ObjectSomeValuesFrom(:[d99e2a70-243d-3bf2-967a-faee3265102b] :[050cdd53-c5b0-3bca-ab11-605aa0f89519]) ObjectSomeValuesFrom(:[3a6d919d-6c25-3aae-9bc3-983ead83a928] :[a64b9845-2739-3f23-803a-31878cbec9a2]) ObjectSomeValuesFrom(:[52542cae-017c-3fc4-bff0-97b7f620db28] :[637ab7d7-d8f3-3ce3-86b2-615f1dcc78a1]))) ObjectSomeValuesFrom(:[051fbfed-3c40-3130-8c09-889cb7b7b5b6] ObjectIntersectionOf(ObjectSomeValuesFrom(:[993a598d-a95a-3235-813e-59252c975070] :[0374e1c9-f732-3bdf-bfa3-fa6009ac5959]) ObjectSomeValuesFrom(:[75e0da0c-21ea-301f-a176-bf056788afe5] :[99b6a46c-5500-3a7a-852d-f7cd71e011bc])))))";

        List<UUID> owlAxiomUuids = List.of(
                UUID.fromString("28152255-eeb8-3ec6-a3cf-8cbbd4684c4c"),
                UUID.fromString("e54fe220-2aa6-385f-8c1a-22374f2328aa"),
                UUID.fromString("2ffdca5d-ca83-304e-a781-597ea34d6f35"),
                UUID.fromString("0cb15147-c04a-33a9-8b65-66d3501c5ae0"),
                UUID.fromString("051fbfed-3c40-3130-8c09-889cb7b7b5b6"),
                UUID.fromString("3161e31b-7d00-33d9-8cbd-9c33dc153aae"),
                UUID.fromString("3d3c4a6a-98d6-3a7c-9e1b-7fabf61e5ca5"),
                UUID.fromString("d99e2a70-243d-3bf2-967a-faee3265102b"),
                UUID.fromString("050cdd53-c5b0-3bca-ab11-605aa0f89519"),
                UUID.fromString("3a6d919d-6c25-3aae-9bc3-983ead83a928"),
                UUID.fromString("d556592d-ee5f-3ff4-a893-bf23c007c5c0"),
                UUID.fromString("52542cae-017c-3fc4-bff0-97b7f620db28"),
                UUID.fromString("637ab7d7-d8f3-3ce3-86b2-615f1dcc78a1"),
                UUID.fromString("051fbfed-3c40-3130-8c09-889cb7b7b5b6"),
                UUID.fromString("3161e31b-7d00-33d9-8cbd-9c33dc153aae"),
                UUID.fromString("95d78b31-52e6-31e7-8eb4-c24490e1f0f4"),
                UUID.fromString("d99e2a70-243d-3bf2-967a-faee3265102b"),
                UUID.fromString("050cdd53-c5b0-3bca-ab11-605aa0f89519"),
                UUID.fromString("3a6d919d-6c25-3aae-9bc3-983ead83a928"),
                UUID.fromString("a64b9845-2739-3f23-803a-31878cbec9a2"),
                UUID.fromString("52542cae-017c-3fc4-bff0-97b7f620db28"),
                UUID.fromString("637ab7d7-d8f3-3ce3-86b2-615f1dcc78a1"),
                UUID.fromString("051fbfed-3c40-3130-8c09-889cb7b7b5b6"),
                UUID.fromString("993a598d-a95a-3235-813e-59252c975070"),
                UUID.fromString("0374e1c9-f732-3bdf-bfa3-fa6009ac5959"),
                UUID.fromString("75e0da0c-21ea-301f-a176-bf056788afe5"),
                UUID.fromString("99b6a46c-5500-3a7a-852d-f7cd71e011bc")
        );
        List<Entity> owlAxiomConcepts = addIfAbsent(owlAxiomUuids);

        LogicalExpression logicalExpression = SctOwlUtilities.sctToLogicalExpression(owlExpression, "");

        // ####################################################
        // ##### Pretty printed owl axiom string to match #####
        // ####################################################
        /* SubClassOf(:[28152255-eeb8-3ec6-a3cf-8cbbd4684c4c]
              ObjectIntersectionOf(:[e54fe220-2aa6-385f-8c1a-22374f2328aa] :[2ffdca5d-ca83-304e-a781-597ea34d6f35] :[0cb15147-c04a-33a9-8b65-66d3501c5ae0]
                  ObjectSomeValuesFrom(:[051fbfed-3c40-3130-8c09-889cb7b7b5b6]
                      ObjectIntersectionOf(
                          ObjectSomeValuesFrom(:[3161e31b-7d00-33d9-8cbd-9c33dc153aae] :[3d3c4a6a-98d6-3a7c-9e1b-7fabf61e5ca5])
                          ObjectSomeValuesFrom(:[d99e2a70-243d-3bf2-967a-faee3265102b] :[050cdd53-c5b0-3bca-ab11-605aa0f89519])
                          ObjectSomeValuesFrom(:[3a6d919d-6c25-3aae-9bc3-983ead83a928] :[d556592d-ee5f-3ff4-a893-bf23c007c5c0])
                          ObjectSomeValuesFrom(:[52542cae-017c-3fc4-bff0-97b7f620db28] :[637ab7d7-d8f3-3ce3-86b2-615f1dcc78a1])
                      )
                  )
                  ObjectSomeValuesFrom(:[051fbfed-3c40-3130-8c09-889cb7b7b5b6]
                      ObjectIntersectionOf(
                          ObjectSomeValuesFrom(:[3161e31b-7d00-33d9-8cbd-9c33dc153aae] :[95d78b31-52e6-31e7-8eb4-c24490e1f0f4])
                          ObjectSomeValuesFrom(:[d99e2a70-243d-3bf2-967a-faee3265102b] :[050cdd53-c5b0-3bca-ab11-605aa0f89519])
                          ObjectSomeValuesFrom(:[3a6d919d-6c25-3aae-9bc3-983ead83a928] :[a64b9845-2739-3f23-803a-31878cbec9a2])
                          ObjectSomeValuesFrom(:[52542cae-017c-3fc4-bff0-97b7f620db28] :[637ab7d7-d8f3-3ce3-86b2-615f1dcc78a1])
                      )
                  )
                  ObjectSomeValuesFrom(:[051fbfed-3c40-3130-8c09-889cb7b7b5b6]
                      ObjectIntersectionOf(
                          ObjectSomeValuesFrom(:[993a598d-a95a-3235-813e-59252c975070] :[0374e1c9-f732-3bdf-bfa3-fa6009ac5959])
                          ObjectSomeValuesFrom(:[75e0da0c-21ea-301f-a176-bf056788afe5] :[99b6a46c-5500-3a7a-852d-f7cd71e011bc])
                      )
                  )
              )
          ) */

        // Initialize Vertices for Complex Axiom DiTree (Indented for readability / traceability)
        int entityIdx = 1; // Skip zeroth index because first item is its own UUID
        EntityVertex definitionRootVertex = EntityVertex.make(TinkarTerm.DEFINITION_ROOT.nid());
            EntityVertex necessarySetVertex = EntityVertex.make(TinkarTerm.NECESSARY_SET.nid());
                EntityVertex andVertex1 = EntityVertex.make(TinkarTerm.AND.nid());
                    EntityVertex referenceVertex1 = makeConceptReferenceVertex(owlAxiomConcepts.get(entityIdx++));
                    EntityVertex referenceVertex2 = makeConceptReferenceVertex(owlAxiomConcepts.get(entityIdx++));
                    EntityVertex referenceVertex3 = makeConceptReferenceVertex(owlAxiomConcepts.get(entityIdx++));
                    EntityVertex roleTypeVertex1 = makeRoleVertex(owlAxiomConcepts.get(entityIdx++));
                        EntityVertex andVertex2 = EntityVertex.make(TinkarTerm.AND.nid());
                            EntityVertex roleTypeVertex2 = makeRoleVertex(owlAxiomConcepts.get(entityIdx++));
                                EntityVertex referenceVertex4 = makeConceptReferenceVertex(owlAxiomConcepts.get(entityIdx++));
                            EntityVertex roleTypeVertex3 = makeRoleVertex(owlAxiomConcepts.get(entityIdx++));
                                EntityVertex referenceVertex5 = makeConceptReferenceVertex(owlAxiomConcepts.get(entityIdx++));
                            EntityVertex roleTypeVertex4 = makeRoleVertex(owlAxiomConcepts.get(entityIdx++));
                                EntityVertex referenceVertex6 = makeConceptReferenceVertex(owlAxiomConcepts.get(entityIdx++));
                            EntityVertex roleTypeVertex5 = makeRoleVertex(owlAxiomConcepts.get(entityIdx++));
                                EntityVertex referenceVertex7 = makeConceptReferenceVertex(owlAxiomConcepts.get(entityIdx++));
                    EntityVertex roleTypeVertex6 = makeRoleVertex(owlAxiomConcepts.get(entityIdx++));;
                        EntityVertex andVertex3 = EntityVertex.make(TinkarTerm.AND.nid());
                            EntityVertex roleTypeVertex7 = makeRoleVertex(owlAxiomConcepts.get(entityIdx++));
                                EntityVertex referenceVertex8 = makeConceptReferenceVertex(owlAxiomConcepts.get(entityIdx++));
                            EntityVertex roleTypeVertex8 = makeRoleVertex(owlAxiomConcepts.get(entityIdx++));
                                EntityVertex referenceVertex9 = makeConceptReferenceVertex(owlAxiomConcepts.get(entityIdx++));
                            EntityVertex roleTypeVertex9 = makeRoleVertex(owlAxiomConcepts.get(entityIdx++));
                                EntityVertex referenceVertex10 = makeConceptReferenceVertex(owlAxiomConcepts.get(entityIdx++));
                            EntityVertex roleTypeVertex10 = makeRoleVertex(owlAxiomConcepts.get(entityIdx++));
                                EntityVertex referenceVertex11 = makeConceptReferenceVertex(owlAxiomConcepts.get(entityIdx++));
                    EntityVertex roleTypeVertex11 = makeRoleVertex(owlAxiomConcepts.get(entityIdx++));
                        EntityVertex andVertex4 = EntityVertex.make(TinkarTerm.AND.nid());
                            EntityVertex roleTypeVertex12 = makeRoleVertex(owlAxiomConcepts.get(entityIdx++));
                                EntityVertex referenceVertex12 = makeConceptReferenceVertex(owlAxiomConcepts.get(entityIdx++));
                            EntityVertex roleTypeVertex13 = makeRoleVertex(owlAxiomConcepts.get(entityIdx++));
                                EntityVertex referenceVertex13 = makeConceptReferenceVertex(owlAxiomConcepts.get(entityIdx++));

        // Build Expected Axiom DiTree
        DiTreeEntity.Builder dteBuilder = DiTreeEntity.builder();
        // Add Vertices in order that follows the pseudo-recursive nature of the logical expression builder
        dteBuilder.setRoot(definitionRootVertex);
        dteBuilder.addVertex(referenceVertex1);
        dteBuilder.addVertex(referenceVertex2);
        dteBuilder.addVertex(referenceVertex3);
        dteBuilder.addVertex(referenceVertex4);
        dteBuilder.addVertex(roleTypeVertex2);
        dteBuilder.addVertex(referenceVertex5);
        dteBuilder.addVertex(roleTypeVertex3);
        dteBuilder.addVertex(referenceVertex6);
        dteBuilder.addVertex(roleTypeVertex4);
        dteBuilder.addVertex(referenceVertex7);
        dteBuilder.addVertex(roleTypeVertex5);
        dteBuilder.addVertex(andVertex2);
        dteBuilder.addVertex(roleTypeVertex1);
        dteBuilder.addVertex(referenceVertex8);
        dteBuilder.addVertex(roleTypeVertex7);
        dteBuilder.addVertex(referenceVertex9);
        dteBuilder.addVertex(roleTypeVertex8);
        dteBuilder.addVertex(referenceVertex10);
        dteBuilder.addVertex(roleTypeVertex9);
        dteBuilder.addVertex(referenceVertex11);
        dteBuilder.addVertex(roleTypeVertex10);
        dteBuilder.addVertex(andVertex3);
        dteBuilder.addVertex(roleTypeVertex6);
        dteBuilder.addVertex(referenceVertex12);
        dteBuilder.addVertex(roleTypeVertex12);
        dteBuilder.addVertex(referenceVertex13);
        dteBuilder.addVertex(roleTypeVertex13);
        dteBuilder.addVertex(andVertex4);
        dteBuilder.addVertex(roleTypeVertex11);
        dteBuilder.addVertex(andVertex1);
        dteBuilder.addVertex(necessarySetVertex);

        // Add Edges in the right order / hierarchy (Indented for readability / traceability)
        dteBuilder.addEdge(necessarySetVertex.vertexIndex(), definitionRootVertex.vertexIndex());
            dteBuilder.addEdge(andVertex1.vertexIndex(), necessarySetVertex.vertexIndex());
                dteBuilder.addEdge(referenceVertex1.vertexIndex(), andVertex1.vertexIndex());
                dteBuilder.addEdge(referenceVertex2.vertexIndex(), andVertex1.vertexIndex());
                dteBuilder.addEdge(referenceVertex3.vertexIndex(), andVertex1.vertexIndex());
                dteBuilder.addEdge(roleTypeVertex1.vertexIndex(), andVertex1.vertexIndex());
                    dteBuilder.addEdge(andVertex2.vertexIndex(), roleTypeVertex1.vertexIndex());
                        dteBuilder.addEdge(roleTypeVertex2.vertexIndex(), andVertex2.vertexIndex());
                            dteBuilder.addEdge(referenceVertex4.vertexIndex(), roleTypeVertex2.vertexIndex());
                        dteBuilder.addEdge(roleTypeVertex3.vertexIndex(), andVertex2.vertexIndex());
                            dteBuilder.addEdge(referenceVertex5.vertexIndex(), roleTypeVertex3.vertexIndex());
                        dteBuilder.addEdge(roleTypeVertex4.vertexIndex(), andVertex2.vertexIndex());
                            dteBuilder.addEdge(referenceVertex6.vertexIndex(), roleTypeVertex4.vertexIndex());
                        dteBuilder.addEdge(roleTypeVertex5.vertexIndex(), andVertex2.vertexIndex());
                            dteBuilder.addEdge(referenceVertex7.vertexIndex(), roleTypeVertex5.vertexIndex());
                dteBuilder.addEdge(roleTypeVertex6.vertexIndex(), andVertex1.vertexIndex());
                    dteBuilder.addEdge(andVertex3.vertexIndex(), roleTypeVertex6.vertexIndex());
                        dteBuilder.addEdge(roleTypeVertex7.vertexIndex(), andVertex3.vertexIndex());
                            dteBuilder.addEdge(referenceVertex8.vertexIndex(), roleTypeVertex7.vertexIndex());
                        dteBuilder.addEdge(roleTypeVertex8.vertexIndex(), andVertex3.vertexIndex());
                            dteBuilder.addEdge(referenceVertex9.vertexIndex(), roleTypeVertex8.vertexIndex());
                        dteBuilder.addEdge(roleTypeVertex9.vertexIndex(), andVertex3.vertexIndex());
                            dteBuilder.addEdge(referenceVertex10.vertexIndex(), roleTypeVertex9.vertexIndex());
                        dteBuilder.addEdge(roleTypeVertex10.vertexIndex(), andVertex3.vertexIndex());
                            dteBuilder.addEdge(referenceVertex11.vertexIndex(), roleTypeVertex10.vertexIndex());
                dteBuilder.addEdge(roleTypeVertex11.vertexIndex(), andVertex1.vertexIndex());
                    dteBuilder.addEdge(andVertex4.vertexIndex(), roleTypeVertex11.vertexIndex());
                        dteBuilder.addEdge(roleTypeVertex12.vertexIndex(), andVertex4.vertexIndex());
                            dteBuilder.addEdge(referenceVertex12.vertexIndex(), roleTypeVertex12.vertexIndex());
                        dteBuilder.addEdge(roleTypeVertex13.vertexIndex(), andVertex4.vertexIndex());
                            dteBuilder.addEdge(referenceVertex13.vertexIndex(), roleTypeVertex13.vertexIndex());

        DiTreeEntity expectedAxiomTree = dteBuilder.build();

        assertEquals(expectedAxiomTree, (DiTreeEntity) logicalExpression.sourceGraph(), "Owl Express Tree and Logical Axiom Tree do not evaluate to the same value.");
    }
}
