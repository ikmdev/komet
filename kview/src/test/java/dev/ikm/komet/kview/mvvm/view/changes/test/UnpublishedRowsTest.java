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
package dev.ikm.komet.kview.mvvm.view.changes.test;

import dev.ikm.komet.kview.mvvm.view.changes.UnpublishedChange;
import dev.ikm.komet.kview.mvvm.view.changes.UnpublishedGroup;
import dev.ikm.tinkar.common.service.CachingService;
import dev.ikm.tinkar.common.service.PrimitiveData;
import dev.ikm.tinkar.common.service.ServiceKeys;
import dev.ikm.tinkar.common.service.ServiceProperties;
import dev.ikm.tinkar.coordinate.Calculators;
import dev.ikm.tinkar.coordinate.view.calculator.ViewCalculator;
import dev.ikm.tinkar.entity.Entity;
import dev.ikm.tinkar.entity.EntityHandle;
import dev.ikm.tinkar.entity.EntityService;
import dev.ikm.tinkar.entity.SemanticEntity;
import dev.ikm.tinkar.entity.SemanticEntityVersion;
import dev.ikm.tinkar.entity.load.LoadEntitiesFromProtobufFile;
import dev.ikm.tinkar.terms.ComponentWithNid;
import dev.ikm.tinkar.terms.KernelConcept;
import dev.ikm.tinkar.terms.KernelPattern;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.io.File;
import java.nio.file.Files;
import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * The tray's rows, read from the real starter set: what a description and a dialect say, and
 * how a concept's changes nest. The starter set matters here: what a row says depends on how
 * the patterns define their fields (a dialect's acceptability is a field's purpose, not its
 * meaning), which a fixture written for the test would only repeat.
 */
class UnpublishedRowsTest {

    private static final File STARTER_DATA = new File("target/data/ike-starter-set-reasoned-pb.zip");
    /** Field index of a description's type in the description pattern. */
    private static final int DESCRIPTION_TYPE_FIELD = 3;

    private static ViewCalculator calculator;

    @BeforeAll
    static void loadStarterSet() throws Exception {
        assertTrue(STARTER_DATA.exists(), "Starter data must be at " + STARTER_DATA.getAbsolutePath());
        CachingService.clearAll();
        ServiceProperties.set(ServiceKeys.DATA_STORE_ROOT,
                Files.createTempDirectory("unpublished-rows-test").toFile());
        PrimitiveData.selectControllerByName("Load Memory-Only 64-bit Store");
        PrimitiveData.start();
        assertTrue(new LoadEntitiesFromProtobufFile(STARTER_DATA).compute().getTotalCount() > 0);
        calculator = Calculators.View.Default();
    }

    @AfterAll
    static void stopStore() {
        PrimitiveData.stop();
    }

    @Test
    @DisplayName("A dialect row says its acceptability and dialect, over the description it extends")
    void dialectRowSaysAcceptabilityOverItsDescription() {
        SemanticEntity<?> description = descriptionOfType(KernelConcept.REGULAR_NAME_DESCRIPTION_TYPE);
        UnpublishedChange dialect = row(usDialectOf(description));

        assertTrue(dialect.title().matches("(Preferred|Acceptable) · US English dialect"), dialect.title());
        // The row says what the store calls the type: this concept's regular name is its long one.
        assertEquals("of “" + text(description) + "” · Regular name description type", dialect.subtitle());
    }

    @Test
    @DisplayName("A description row says its text over its type and language")
    void descriptionRowSaysItsTypeAndLanguage() {
        UnpublishedChange fullyQualified = row(descriptionOfType(KernelConcept.FULLY_QUALIFIED_NAME_DESCRIPTION_TYPE));

        assertEquals(text(descriptionOfType(KernelConcept.FULLY_QUALIFIED_NAME_DESCRIPTION_TYPE)), fullyQualified.title());
        assertEquals("Fully qualified name · English language", fullyQualified.subtitle());
    }

    @Test
    @DisplayName("A semantic with no text of its own is named by its pattern alone")
    void aSemanticWithoutTextIsNamedByItsPattern() {
        SemanticEntity<?> membership = EntityService.get()
                .semanticsForComponentOfPattern(KernelPattern.US_DIALECT_PATTERN.nid(),
                        KernelPattern.TINKAR_BASE_MODEL_COMPONENT_PATTERN.nid())
                .findFirst().orElseThrow();
        UnpublishedChange row = row(membership);

        assertTrue(row.untitled());
        assertNull(row.subtitle());
    }

    @Test
    @DisplayName("A concept heading holds its descriptions, each holding its dialects")
    void descriptionsNestUnderTheConceptAndDialectsUnderTheirDescription() {
        List<SemanticEntity<?>> descriptions = descriptions();
        List<UnpublishedChange> changes = new ArrayList<>();
        changes.add(row(concept()));
        for (SemanticEntity<?> description : descriptions) {
            changes.add(row(description));
            changes.add(row(usDialectOf(description)));
        }

        List<UnpublishedGroup> groups = UnpublishedGroup.groupsOf(changes, calculator);

        assertEquals(1, groups.size());
        UnpublishedGroup group = groups.getFirst();
        assertEquals(concept().nid(), group.nid());
        assertEquals(descriptions.size(), group.rows().size());
        for (UnpublishedChange description : group.rows()) {
            assertEquals(1, description.children().size(), description.title());
            assertEquals(KernelPattern.US_DIALECT_PATTERN.nid(),
                    ((SemanticEntity<?>) description.children().getFirst().entity()).patternNid());
        }
        assertEquals(changes.size(), group.changes().size());
        assertEquals(changes.size() - 1, group.semanticChanges().size());
        assertEquals(concept().nid(), group.changes().getFirst().nid(), "the concept's own change comes first");
    }

    @Test
    @DisplayName("A description changed on a published concept heads a row under the concept")
    void aPublishedConceptStillHeadsItsChanges() {
        SemanticEntity<?> description = descriptions().getFirst();

        List<UnpublishedGroup> groups = UnpublishedGroup.groupsOf(
                List.of(row(description), row(usDialectOf(description))), calculator);

        assertEquals(1, groups.size());
        UnpublishedGroup group = groups.getFirst();
        assertEquals(concept().nid(), group.nid());
        assertNull(group.kind(), "the concept itself has no change");
        assertEquals(1, group.rows().size());
        assertEquals(1, group.rows().getFirst().children().size());
        UnpublishedChange nested = group.rows().getFirst().children().getFirst();
        assertFalse(nested.showsSubtitle(), "the description is right above it");
        assertFalse(nested.untitled(), "it has a title of its own, not a muted one");
        assertTrue(group.rows().getFirst().showsSubtitle(), "a description keeps its type and language");
    }

    @Test
    @DisplayName("A dialect changed on a published description sits under the concept, saying which description")
    void aDialectOnAPublishedDescriptionClimbsToTheConcept() {
        SemanticEntity<?> description = descriptions().getFirst();
        UnpublishedChange dialect = row(usDialectOf(description));

        List<UnpublishedGroup> groups = UnpublishedGroup.groupsOf(List.of(dialect), calculator);

        assertEquals(1, groups.size());
        assertEquals(concept().nid(), groups.getFirst().nid());
        assertEquals(List.of(dialect.nid()), groups.getFirst().rows().stream().map(UnpublishedChange::nid).toList());
        assertTrue(dialect.subtitle().contains(text(description)), dialect.subtitle());
        assertTrue(groups.getFirst().rows().getFirst().showsSubtitle(), "with no description above it, it says which");
    }

    @Test
    @DisplayName("Two concepts stay two headings, whatever they are named")
    void twoConceptsStayTwoHeadings() {
        List<UnpublishedGroup> groups = UnpublishedGroup.groupsOf(List.of(row(concept()),
                row(EntityHandle.get(KernelConcept.DESCRIPTION_TYPE.nid()).orNull())), calculator);

        assertEquals(2, groups.size());
        assertFalse(groups.getFirst().nid() == groups.getLast().nid());
    }

    private static UnpublishedChange row(Entity<?> entity) {
        return UnpublishedChange.of(entity, calculator);
    }

    private static Entity<?> concept() {
        return EntityHandle.get(KernelConcept.ENGLISH_LANGUAGE.nid()).orNull();
    }

    private static List<SemanticEntity<?>> descriptions() {
        List<SemanticEntity<?>> descriptions = new ArrayList<>();
        EntityService.get().semanticsForComponentOfPattern(concept().nid(), KernelPattern.DESCRIPTION_PATTERN.nid())
                .forEach(descriptions::add);
        assertFalse(descriptions.isEmpty(), "English language must carry descriptions");
        return descriptions;
    }

    private static SemanticEntity<?> descriptionOfType(KernelConcept type) {
        return descriptions().stream()
                .filter(description -> version(description).fieldValues().get(DESCRIPTION_TYPE_FIELD)
                        instanceof ComponentWithNid value && value.nid() == type.nid())
                .findFirst().orElseThrow();
    }

    private static SemanticEntity<?> usDialectOf(SemanticEntity<?> description) {
        return EntityService.get()
                .semanticsForComponentOfPattern(description.nid(), KernelPattern.US_DIALECT_PATTERN.nid())
                .findFirst().orElseThrow();
    }

    private static String text(SemanticEntity<?> description) {
        return calculator.getTextFromSemanticVersion(version(description)).orElseThrow();
    }

    private static SemanticEntityVersion version(SemanticEntity<?> semantic) {
        return semantic.versions().getLast();
    }
}
