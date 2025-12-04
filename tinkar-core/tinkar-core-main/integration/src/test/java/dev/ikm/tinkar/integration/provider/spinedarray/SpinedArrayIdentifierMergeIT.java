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
import dev.ikm.tinkar.common.id.PublicIds;
import dev.ikm.tinkar.common.util.io.FileUtil;
import dev.ikm.tinkar.common.util.uuid.UuidT5Generator;
import dev.ikm.tinkar.composer.Composer;
import dev.ikm.tinkar.composer.Session;
import dev.ikm.tinkar.composer.assembler.ConceptAssembler;
import dev.ikm.tinkar.composer.template.Definition;
import dev.ikm.tinkar.composer.template.FullyQualifiedName;
import dev.ikm.tinkar.composer.template.Identifier;
import dev.ikm.tinkar.composer.template.StatedAxiom;
import dev.ikm.tinkar.composer.template.Synonym;
import dev.ikm.tinkar.coordinate.Coordinates;
import dev.ikm.tinkar.coordinate.stamp.calculator.Latest;
import dev.ikm.tinkar.coordinate.view.ViewCoordinateRecord;
import dev.ikm.tinkar.coordinate.view.calculator.ViewCalculatorWithCache;
import dev.ikm.tinkar.entity.EntityCountSummary;
import dev.ikm.tinkar.entity.EntityService;
import dev.ikm.tinkar.entity.PatternEntityVersion;
import dev.ikm.tinkar.entity.load.LoadEntitiesFromProtobufFile;
import dev.ikm.tinkar.integration.TestConstants;
import dev.ikm.tinkar.integration.helper.DataStore;
import dev.ikm.tinkar.integration.helper.TestHelper;
import dev.ikm.tinkar.terms.EntityProxy;
import dev.ikm.tinkar.terms.State;
import dev.ikm.tinkar.terms.TinkarTerm;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.opentest4j.AssertionFailedError;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.util.HashSet;
import java.util.Set;
import java.util.UUID;

import static dev.ikm.tinkar.terms.TinkarTerm.DESCRIPTION_NOT_CASE_SENSITIVE;
import static dev.ikm.tinkar.terms.TinkarTerm.ENGLISH_LANGUAGE;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

class SpinedArrayIdentifierMergeIT {
    static final Logger LOG = LoggerFactory.getLogger(SpinedArrayIdentifierMergeIT.class);

    @BeforeAll
    static void beforeAll() {
        File datastoreRoot = TestConstants.createFilePathInTargetFromClassName.apply(SpinedArrayIdentifierMergeIT.class);
        FileUtil.recursiveDelete(datastoreRoot);
        TestHelper.startDataBase(DataStore.SPINED_ARRAY_STORE, datastoreRoot);
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
    public void testMergeIdentifiersCompoundPublicId_ExpectFailure() {
        UUID namespace = UUID.randomUUID();
        Composer composer = new Composer("Snomed Starter Data Composer");

        EntityProxy.Concept author = EntityProxy.Concept.make("IHTSDO SNOMED CT Starter Data Author",
                UuidT5Generator.get(namespace, "IHTSDO SNOMED CT Starter Data Author"));

        Session session = composer.open(State.ACTIVE, author, TinkarTerm.PRIMORDIAL_MODULE, TinkarTerm.PRIMORDIAL_PATH);

        initializeAuthor(session, namespace, author);

        EntityProxy.Concept sctId = createIdentifierSemantic(session, namespace, "SCTID");
        EntityProxy.Concept gmdnTerms = createIdentifierSemantic(session, namespace, "GMDN Terms");

        //

        // 1. Create first concept
        UUID snomedUuid = createConceptWithIdentifier(session, namespace, sctId, "725561007");
        // 2. Create second concept
        UUID gmdnUuid = createConceptWithIdentifier(session, namespace, gmdnTerms, "62567");

        // 3. Create third concept with compound Public ID
        EntityProxy.Concept compoundConcept = EntityProxy.Concept.make(PublicIds.of(snomedUuid, gmdnUuid));
        session.compose((ConceptAssembler conceptAssembler) -> conceptAssembler
                .concept(compoundConcept)
        );

        //

        composer.commitAllSessions();

        Set<String> identifiers = extractIdentifiers(compoundConcept);

        // TODO remove after fixing root cause
        assertThrows(AssertionFailedError.class, () -> {
            verifyIdentifiers(identifiers);
        });
    }

    @Test
    public void testMergeIdentifiersCompoundPublicId_Workaround() {
        UUID namespace = UUID.randomUUID();
        Composer composer = new Composer("Snomed Starter Data Composer");

        EntityProxy.Concept author = EntityProxy.Concept.make("IHTSDO SNOMED CT Starter Data Author",
                UuidT5Generator.get(namespace, "IHTSDO SNOMED CT Starter Data Author"));

        Session session = composer.open(State.ACTIVE, author, TinkarTerm.PRIMORDIAL_MODULE, TinkarTerm.PRIMORDIAL_PATH);

        initializeAuthor(session, namespace, author);

        EntityProxy.Concept sctId = createIdentifierSemantic(session, namespace, "SCTID");
        EntityProxy.Concept gmdnTerms = createIdentifierSemantic(session, namespace, "GMDN Terms");

        //

        // 1. Create first concept
        UUID gmdnUuid = createConceptWithIdentifier(session, namespace, gmdnTerms, "62567");
        // 2. Create third concept with compound Public ID, referencing first and second concepts
        UUID snomedUuid = UuidT5Generator.get(namespace, "725561007");
        EntityProxy.Concept compoundConcept = EntityProxy.Concept.make(PublicIds.of(snomedUuid, gmdnUuid));
        session.compose((ConceptAssembler conceptAssembler) -> conceptAssembler
                .concept(compoundConcept)
        );
        // 3. Create second concept, using previously generated UUID
        createConceptWithIdentifier(session, namespace, sctId, "725561007", snomedUuid);

        //

        composer.commitAllSessions();

        Set<String> identifiers = extractIdentifiers(compoundConcept);
        verifyIdentifiers(identifiers);
    }

    private void verifyIdentifiers(Set<String> identifiers) {
        LOG.info("IDENTIFIERS: {}", identifiers);
        assertEquals(2, identifiers.size());
        assertTrue(identifiers.contains("GMDN Terms: 62567"));
        assertTrue(identifiers.contains("SCTID: 725561007"));
    }

    private EntityProxy.Concept createIdentifierSemantic(Session session, UUID namespace, String name) {
        EntityProxy.Concept snomedIdentifier = EntityProxy.Concept.make(name, UuidT5Generator.get(namespace, name));
        session.compose((ConceptAssembler concept) -> concept
                .concept(snomedIdentifier)
                .attach((FullyQualifiedName fqn) -> fqn
                        .language(ENGLISH_LANGUAGE)
                        .text(name)
                        .caseSignificance(DESCRIPTION_NOT_CASE_SENSITIVE)
                )
                .attach((Synonym synonym) -> synonym
                        .language(ENGLISH_LANGUAGE)
                        .text(name)
                        .caseSignificance(DESCRIPTION_NOT_CASE_SENSITIVE)
                )
                .attach((Definition definition) -> definition
                        .language(ENGLISH_LANGUAGE)
                        .text(name)
                        .caseSignificance(DESCRIPTION_NOT_CASE_SENSITIVE)
                )
                .attach((Identifier identifier) -> identifier
                        .source(TinkarTerm.UNIVERSALLY_UNIQUE_IDENTIFIER)
                        .identifier(snomedIdentifier.asUuidArray()[0].toString())
                )
                .attach((StatedAxiom statedAxiom) -> statedAxiom
                        .isA(TinkarTerm.IDENTIFIER_SOURCE)
                )
        );
        return snomedIdentifier;
    }

    private UUID createConceptWithIdentifier(Session session, UUID namespace, EntityProxy.Concept identifierSource, String identiferValue) {
        UUID uuid = UuidT5Generator.get(namespace, identiferValue);
        return createConceptWithIdentifier(session, namespace, identifierSource, identiferValue, uuid);
    }

    private UUID createConceptWithIdentifier(Session session, UUID namespace, EntityProxy.Concept identifierSource, String identifierValue, UUID uuid) {
        EntityProxy.Concept concept = EntityProxy.Concept.make(PublicIds.of(uuid));
        session.compose((ConceptAssembler conceptAssembler) -> conceptAssembler
                .concept(concept)
                .attach((Identifier identifier) -> identifier
                        .source(TinkarTerm.UNIVERSALLY_UNIQUE_IDENTIFIER)
                        .identifier(concept.asUuidArray()[0].toString())
                )
                .attach((Identifier identifier) -> identifier
                        .source(identifierSource)
                        .identifier(identifierValue)
                )
        );
        return uuid;
    }

    private void initializeAuthor(Session session, UUID namespace, EntityProxy.Concept author) {
        session.compose((ConceptAssembler concept) -> concept
                .concept(author)
                .attach((FullyQualifiedName fqn) -> fqn
                        .language(ENGLISH_LANGUAGE)
                        .text("IHTSDO SNOMED CT Starter Data Author")
                        .caseSignificance(DESCRIPTION_NOT_CASE_SENSITIVE)
                )
                .attach((Synonym synonym) -> synonym
                        .language(ENGLISH_LANGUAGE)
                        .text("SNOMED CT Starter Data Author")
                        .caseSignificance(DESCRIPTION_NOT_CASE_SENSITIVE)
                )
                .attach((Definition definition) -> definition
                        .language(ENGLISH_LANGUAGE)
                        .text("International Health Terminology Standards Development Organisation (IHTSDO) SNOMED CT Starter Data Author")
                        .caseSignificance(DESCRIPTION_NOT_CASE_SENSITIVE)
                )
                .attach((Identifier identifier) -> identifier
                        .source(TinkarTerm.UNIVERSALLY_UNIQUE_IDENTIFIER)
                        .identifier(author.asUuidArray()[0].toString())
                )
                .attach((StatedAxiom statedAxiom) -> statedAxiom
                        .isA(TinkarTerm.USER)
                )
        );
    }

    private Set<String> extractIdentifiers(EntityProxy componentInDetailsViewer) {
        ViewCoordinateRecord viewCoord = Coordinates.View.DefaultView();
        ViewCalculatorWithCache viewCalc = ViewCalculatorWithCache.getCalculator(viewCoord);

        Latest<PatternEntityVersion> latestIdPattern = viewCalc.latestPatternEntityVersion(TinkarTerm.IDENTIFIER_PATTERN);
        Set<String> identifiers = new HashSet<>();

        EntityService.get().forEachSemanticForComponentOfPattern(componentInDetailsViewer.nid(), TinkarTerm.IDENTIFIER_PATTERN.nid(), (semanticEntity) -> {
            viewCalc.latest(semanticEntity).ifPresent((latestSemanticVersion -> {
                EntityProxy identifierSource = latestIdPattern.get().getFieldWithMeaning(TinkarTerm.IDENTIFIER_SOURCE, latestSemanticVersion);
                if (!PublicId.equals(identifierSource, TinkarTerm.UNIVERSALLY_UNIQUE_IDENTIFIER)) {
                    try {
                        String idSourceName = viewCalc.getPreferredDescriptionTextWithFallbackOrNid(identifierSource);
                        String idValue = latestIdPattern.get().getFieldWithMeaning(TinkarTerm.IDENTIFIER_VALUE, latestSemanticVersion);

                        identifiers.add("%s: %s".formatted(idSourceName, idValue));
                    } catch (IndexOutOfBoundsException exception) {
                        //
                    }
                }
            }));
        });

        return identifiers;
    }

}
