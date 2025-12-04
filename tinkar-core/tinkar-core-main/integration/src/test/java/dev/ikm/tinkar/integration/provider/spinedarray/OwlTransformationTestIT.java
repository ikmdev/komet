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

import dev.ikm.tinkar.entity.*;
import dev.ikm.tinkar.entity.graph.adaptor.axiom.LogicalExpression;
import dev.ikm.tinkar.entity.transaction.Transaction;
import dev.ikm.tinkar.ext.lang.owl.Rf2OwlToLogicAxiomTransformer;
import dev.ikm.tinkar.ext.lang.owl.SctOwlUtilities;
import dev.ikm.tinkar.integration.KeyValueProviderExtension;
import dev.ikm.tinkar.integration.OpenSpinedArrayKeyValueProvider;
import dev.ikm.tinkar.integration.TestConstants;
import dev.ikm.tinkar.integration.helper.DataStore;
import dev.ikm.tinkar.integration.helper.TestHelper;
import dev.ikm.tinkar.terms.EntityProxy;
import dev.ikm.tinkar.terms.TinkarTerm;
import org.eclipse.collections.api.factory.Lists;
import org.eclipse.collections.api.list.MutableList;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.atomic.AtomicInteger;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

@ExtendWith(OpenSpinedArrayKeyValueProvider.class)
public class OwlTransformationTestIT {
    private static final Logger LOG = LoggerFactory.getLogger(OwlTransformationTestIT.class);
    public static final EntityProxy.Pattern IDENTIFIER_PATTERN = EntityProxy.Pattern.make("Identifier Pattern", UUID.fromString("5d60e14b-c410-5172-9559-3c4253278ae2"));
    public static final EntityProxy.Pattern AXIOM_SYNTAX_PATTERN = EntityProxy.Pattern.make("Axiom Syntax Pattern", UUID.fromString("c0ca180b-aae2-5fa1-9ab7-4a24f2dfe16b"));
    private static final File DATASTORE_ROOT = TestConstants.createFilePathInTargetFromClassName.apply(OwlTransformationTestIT.class);
    private static final List<EntityProxy.Concept> testConceptList = Arrays.asList(
            EntityProxy.Concept.make("TESTCONCEPTONE", UUID.randomUUID()),
            EntityProxy.Concept.make("TESTCONCEPTTWO", UUID.randomUUID()),
            EntityProxy.Concept.make("TESTCONCEPTTHREE", UUID.randomUUID()));

    @BeforeEach
    public void beforeEach(){
        TestHelper.startDataBase(DataStore.SPINED_ARRAY_STORE, DATASTORE_ROOT);
        TestHelper.loadDataFile(TestConstants.PB_STARTER_DATA_REASONED);
        generateTestAxiomData(); // Write minimal axiom test data
    }

    @AfterEach
    public void afterEach(){
        TestHelper.stopDatabase();
    }

    @Test
    @Disabled("Requires tinkar-starter-data.pb.zip test resource")
    public void owlTransformationWithDefaults(){
        Transaction owlTransformationTransaction = Transaction.make();
        try {
            new Rf2OwlToLogicAxiomTransformer(
                    owlTransformationTransaction,
                    AXIOM_SYNTAX_PATTERN,
                    TinkarTerm.EL_PLUS_PLUS_STATED_AXIOMS_PATTERN).call();
        } catch (Exception e){
            throw new RuntimeException(e);
        }
        AtomicInteger testCount = new AtomicInteger();
        Arrays.stream(EntityService.get()
                .semanticNidsOfPattern(
                    TinkarTerm.EL_PLUS_PLUS_STATED_AXIOMS_PATTERN.nid())).forEach(definitionNid -> {
                        EntityVersion version = EntityService.get().getEntityFast(definitionNid).versions().get(0);

                        if (!version.path().equals(TinkarTerm.PRIMORDIAL_PATH)) {
                            testCount.incrementAndGet();
                            assertEquals(version.author(), TinkarTerm.USER);
                            assertEquals(version.module(), TinkarTerm.SOLOR_OVERLAY_MODULE);
                            assertEquals(version.path(), TinkarTerm.DEVELOPMENT_PATH);
                        }
                    });
        assertEquals(testConceptList.size(), testCount.get());
        LOG.info("Completed validation of " + testCount.get() + " axioms with default coordinates");
    }

    @Test
    @Disabled("Requires tinkar-starter-data.pb.zip test resource")
    public void owlTransformationWithoutDefaults(){
        Transaction owlTransformationTransaction = Transaction.make();
        try {
            new Rf2OwlToLogicAxiomTransformer(
                    owlTransformationTransaction,
                    AXIOM_SYNTAX_PATTERN,
                    TinkarTerm.EL_PLUS_PLUS_STATED_AXIOMS_PATTERN,
                    TinkarTerm.KOMET_USER.nid(),
                    TinkarTerm.MODULE_FOR_USER.nid(),
                    TinkarTerm.PATH_FOR_USER.nid()).call();
        } catch (Exception e){
            throw new RuntimeException(e);
        }

        AtomicInteger testCount = new AtomicInteger();
        Arrays.stream(
            EntityService.get().semanticNidsOfPattern(TinkarTerm.EL_PLUS_PLUS_STATED_AXIOMS_PATTERN.nid())
        ).forEach(definitionNid -> {
            EntityVersion version = EntityService.get().getEntityFast(definitionNid).versions().get(0);

            if (!version.path().equals(TinkarTerm.PRIMORDIAL_PATH)) {
                testCount.incrementAndGet();
                assertEquals(version.author(), TinkarTerm.KOMET_USER);
                assertEquals(version.module(), TinkarTerm.MODULE_FOR_USER);
                assertEquals(version.path(), TinkarTerm.PATH_FOR_USER);
            }
        });
        assertEquals(testCount.get(), testConceptList.size());
        LOG.info("Completed validation of " + testCount.get() + " axioms with user-defined coordinates");
    }

    @Test
    @Disabled("Requires Snomed data to run this test")
    public void testOwlExpression(){
        StringBuilder propertyBuilder = new StringBuilder();
        StringBuilder classBuilder = new StringBuilder();
        List<String> owlExpressionsToProcess = getOwlExpressionStrings();

        for (String owlExpression : owlExpressionsToProcess) {
            if (owlExpression.toLowerCase().contains("property")) {
                propertyBuilder.append(" ").append(owlExpression);
                if (!owlExpression.toLowerCase().contains("objectpropertychain")) {
                    String tempExpression = owlExpression.toLowerCase().replace("subobjectpropertyof", " subclassof");
                    tempExpression = tempExpression.toLowerCase().replace("subdatapropertyof", " subclassof");
                    classBuilder.append(" ").append(tempExpression);
                }
            } else {
                classBuilder.append(" ").append(owlExpression);
            }
        }

        String owlClassExpressionsToProcess = classBuilder.toString();
        String owlPropertyExpressionsToProcess = propertyBuilder.toString();
        try {
            LogicalExpression expression = SctOwlUtilities.sctToLogicalExpression(
                    owlClassExpressionsToProcess,
                    owlPropertyExpressionsToProcess);
            assertNotNull(expression);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    private static void generateTestAxiomData() {
        Entity<? extends EntityVersion> testStamp = createSTAMPTestHelper(TinkarTerm.ACTIVE_STATE,
                System.currentTimeMillis(), TinkarTerm.USER, TinkarTerm.MODULE, TinkarTerm.SANDBOX_PATH);

        EntityProxy.Concept parentConcept = EntityProxy.Concept.make("TESTPARENTCONCEPT", UUID.randomUUID());
        createConceptTestHelper(parentConcept, parentConcept.description(), testStamp);

        testConceptList.forEach(concept -> {
            String owlAxiomString = "SubClassOf(:" + concept.description() + " :" + parentConcept.description() + ")";
            createConceptTestHelper(concept, concept.description(), testStamp);
            createAxiomSyntaxSemanticTestHelper(concept.nid(), owlAxiomString, testStamp);
        });
    }

    private static List<String> getOwlExpressionStrings() {
        String inputString1 =
            "EquivalentClasses(:126885006 ObjectIntersectionOf(:64572001 ObjectSomeValuesFrom(:609096000 ObjectIntersectionOf(ObjectSomeValuesFrom(:116676008 :108369006) ObjectSomeValuesFrom(:363698007 :89837001)))))";
        String inputString2 =
            "EquivalentClasses(:895602001 ObjectIntersectionOf(:763158003 ObjectSomeValuesFrom(:411116001 :385287007) " +
            "ObjectSomeValuesFrom(:609096000 ObjectSomeValuesFrom(:127489000 :704226002)) DataHasValue(:1142139005 \"1\"^^xsd:integer)))";
        String inputString3 =
            "EquivalentClasses(:428684004 ObjectIntersectionOf(:763158003 ObjectSomeValuesFrom(:411116001 :447079001) " +
            "ObjectSomeValuesFrom(:609096000 ObjectIntersectionOf(ObjectSomeValuesFrom(:732943007 :386895008) ObjectSomeValuesFrom(:732945000 :258684004) ObjectSomeValuesFrom(:732947008 :732936001)" +
            "ObjectSomeValuesFrom(:762949000 :386895008) DataHasValue(:1142135004 \"12\"^^xsd:decimal) DataHasValue(:1142136003 \"1\"^^xsd:decimal)))" +
            "ObjectSomeValuesFrom(:609096000 ObjectIntersectionOf(ObjectSomeValuesFrom(:732943007 :386897000) ObjectSomeValuesFrom(:732945000 :258684004) ObjectSomeValuesFrom(:732947008 :732936001)" +
            "ObjectSomeValuesFrom(:762949000 :386897000) DataHasValue(:1142135004 \"60\"^^xsd:decimal) DataHasValue(:1142136003 \"1\"^^xsd:decimal)))" +
            "ObjectSomeValuesFrom(:609096000 ObjectIntersectionOf(ObjectSomeValuesFrom(:732943007 :386898005) ObjectSomeValuesFrom(:732945000 :258684004) ObjectSomeValuesFrom(:732947008 :732936001)" +
            "ObjectSomeValuesFrom(:762949000 :386898005) DataHasValue(:1142135004 \"100\"^^xsd:decimal) DataHasValue(:1142136003 \"1\"^^xsd:decimal))))";
        String inputString4 = "SubClassOf(:1222765007 ObjectIntersectionOf(:1222592004 :1222593009 :1222594003))";

        List<String> owlExpressionsToProcess = new ArrayList<>();
        owlExpressionsToProcess.add(inputString1);
        owlExpressionsToProcess.add(inputString2);
        owlExpressionsToProcess.add(inputString3);
        owlExpressionsToProcess.add(inputString4);
        return owlExpressionsToProcess;
    }

    private static Entity<? extends EntityVersion> createSTAMPTestHelper(EntityProxy.Concept status, long time, EntityProxy.Concept author, EntityProxy.Concept module, EntityProxy.Concept path){
        LOG.info("Building STAMP Chronology");
        UUID stampUUID = UUID.randomUUID();
        RecordListBuilder<StampVersionRecord> versions = RecordListBuilder.make();
        //Create STAMP Chronology
        StampRecord stampRecord = StampRecordBuilder.builder()
                .nid(EntityService.get().nidForUuids(stampUUID))
                .leastSignificantBits(stampUUID.getLeastSignificantBits())
                .mostSignificantBits(stampUUID.getMostSignificantBits())
                .additionalUuidLongs(null)
                .versions(versions)
                .build();

        LOG.info("Building STAMP Version");
        //Create STAMP Version
        versions.add(StampVersionRecordBuilder.builder()
                .chronology(stampRecord)
                .stateNid(status.nid())
                .time(time)
                .authorNid(author.nid())
                .moduleNid(module.nid())
                .pathNid(path.nid())
                .build());

        Entity<? extends EntityVersion> entity = StampRecordBuilder.builder(stampRecord).versions(versions.toImmutable()).build();
        EntityService.get().putEntity(entity);
        return entity;
    }

    private static Entity<? extends EntityVersion> createConceptTestHelper(EntityProxy.Concept concept, String id,
                                                            Entity<? extends EntityVersion> stampEntity){
        RecordListBuilder<ConceptVersionRecord> versions = RecordListBuilder.make();
        ConceptRecord conceptRecord = ConceptRecordBuilder.builder()
                .nid(concept.nid())
                .leastSignificantBits(concept.asUuidArray()[0].getLeastSignificantBits())
                .mostSignificantBits(concept.asUuidArray()[0].getMostSignificantBits())
                .additionalUuidLongs(null)
                .versions(versions.toImmutable())
                .build();

        versions.add(ConceptVersionRecordBuilder.builder()
                .chronology(conceptRecord)
                .stampNid(stampEntity.nid())
                .build());

        createIdentifierSemanticTestHelper(concept.nid(), id, stampEntity);

        Entity<? extends EntityVersion> entity = ConceptRecordBuilder.builder(conceptRecord).versions(versions.toImmutable()).build();
        EntityService.get().putEntity(entity);
        return entity;
    }

    private static void createIdentifierSemanticTestHelper(int referencedComponentNid,
                                                                       String id,
                                                                       Entity<? extends EntityVersion> authoringSTAMP){
        LOG.info("Building Identifier Semantic");
        RecordListBuilder<SemanticVersionRecord> versions = RecordListBuilder.make();

        UUID navigationSemanticUUID = UUID.randomUUID();
        SemanticRecord semanticRecord = SemanticRecordBuilder.builder()
                .nid(EntityService.get().nidForUuids(navigationSemanticUUID))
                .leastSignificantBits(navigationSemanticUUID.getLeastSignificantBits())
                .mostSignificantBits(navigationSemanticUUID.getMostSignificantBits())
                .additionalUuidLongs(null)
                .patternNid(IDENTIFIER_PATTERN.nid())
                .referencedComponentNid(referencedComponentNid)
                .versions(versions.toImmutable())
                .build();

        LOG.info("Building Identifier Semantic Fields");
        MutableList<Object> identifierFields = Lists.mutable.empty();
        identifierFields.add(TinkarTerm.UNIVERSALLY_UNIQUE_IDENTIFIER);
        identifierFields.add(id);

        versions.add(SemanticVersionRecordBuilder.builder()
                .chronology(semanticRecord)
                .stampNid(authoringSTAMP.nid())
                .fieldValues(identifierFields.toImmutable())
                .build());

        EntityService.get().putEntity(SemanticRecordBuilder.builder(semanticRecord).versions(versions.toImmutable()).build());
    }

    private static Entity<? extends EntityVersion> createAxiomSyntaxSemanticTestHelper(int referencedComponentNid,
                                                                     String axiomSyntax,
                                                                     Entity<? extends EntityVersion> authoringSTAMP){
        RecordListBuilder<SemanticVersionRecord> versions = RecordListBuilder.make();
        UUID axiomSyntaxSemantic = UUID.randomUUID();
        SemanticRecord semanticRecord = SemanticRecordBuilder.builder()
                .nid(EntityService.get().nidForUuids(axiomSyntaxSemantic))
                .leastSignificantBits(axiomSyntaxSemantic.getLeastSignificantBits())
                .mostSignificantBits(axiomSyntaxSemantic.getMostSignificantBits())
                .additionalUuidLongs(null)
                .patternNid(AXIOM_SYNTAX_PATTERN.nid())
                .referencedComponentNid(referencedComponentNid)
                .versions(versions.toImmutable())
                .build();

        MutableList<Object> axiomSyntaxFields = Lists.mutable.empty();
        axiomSyntaxFields.add(axiomSyntax);

        versions.add(SemanticVersionRecordBuilder.builder()
                .chronology(semanticRecord)
                .stampNid(authoringSTAMP.nid())
                .fieldValues(axiomSyntaxFields.toImmutable())
                .build());

        Entity<? extends EntityVersion> entity = SemanticRecordBuilder.builder(semanticRecord).versions(versions.toImmutable()).build();
        EntityService.get().putEntity(entity);
        return entity;
    }

}
