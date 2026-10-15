package dev.ikm.komet.framework.observable;

import dev.ikm.tinkar.common.id.LongIds;
import dev.ikm.tinkar.terms.ConceptFacade;
import dev.ikm.tinkar.terms.EntityProxy;
import org.junit.jupiter.api.Test;

import java.util.UUID;

import static dev.ikm.komet.terms.KometConcept.BLANK_CONCEPT;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertInstanceOf;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * Unit-tests {@link ObservableComposer#isDefaultFieldValue} (no data store, off the FX thread):
 * the values {@link ObservableComposer#generateDefaultFieldValues} starts a field with count as
 * default, anything an author enters does not.
 */
class ObservableComposerDefaultFieldValuesTest {

    private static final EntityProxy.Concept ENGLISH = EntityProxy.Concept.make("English language",
            UUID.fromString("0f3a5cfd-0a0c-4b4d-9a62-3d6a9b2b6a01"));

    @Test
    void theValuesANewFieldStartsWithAreDefault() {
        assertTrue(ObservableComposer.isDefaultFieldValue(null), "null, an unsupported data type's start");
        assertTrue(ObservableComposer.isDefaultFieldValue(BLANK_CONCEPT), "the blank concept");
        assertTrue(ObservableComposer.isDefaultFieldValue(
                EntityProxy.Concept.make("Blank", BLANK_CONCEPT.publicId().leastUuid())), "any facade of the blank concept");
        assertTrue(ObservableComposer.isDefaultFieldValue(""), "empty string");
        assertTrue(ObservableComposer.isDefaultFieldValue(0), "zero integer");
        assertTrue(ObservableComposer.isDefaultFieldValue(0.0F), "zero float");
        assertTrue(ObservableComposer.isDefaultFieldValue(false), "false");
        assertTrue(ObservableComposer.isDefaultFieldValue(LongIds.list.empty()), "empty component list");
        assertTrue(ObservableComposer.isDefaultFieldValue(LongIds.set.empty()), "empty component set");
        assertTrue(ObservableComposer.isDefaultFieldValue(new byte[0]), "empty byte array");
    }

    @Test
    void aNewComponentFieldStartsWithTheBlankConceptAsAProxy() {
        Object seed = ObservableComposer.defaultComponentFieldValue();
        // The component controls hold EntityProxy values; the seed reaches them through an untyped
        // List<Object>, so the raw KometConcept enum there fails at the first cast in a skin.
        assertInstanceOf(EntityProxy.Concept.class, seed);
        assertTrue(ObservableComposer.isDefaultFieldValue(seed), "the seed counts as a default");
    }

    @Test
    void valuesAnAuthorEnteredAreNotDefault() {
        assertFalse(ObservableComposer.isDefaultFieldValue(ENGLISH), "a concept");
        assertFalse(ObservableComposer.isDefaultFieldValue("Myocardial infarction"), "text");
        assertFalse(ObservableComposer.isDefaultFieldValue(" "), "a space is entered text, not a blank field");
        assertFalse(ObservableComposer.isDefaultFieldValue(3), "a non-zero integer");
        assertFalse(ObservableComposer.isDefaultFieldValue(1.5F), "a non-zero float");
        assertFalse(ObservableComposer.isDefaultFieldValue(true), "true");
        assertFalse(ObservableComposer.isDefaultFieldValue(LongIds.list.of(1, 2)), "a component list with entries");
        assertFalse(ObservableComposer.isDefaultFieldValue(new byte[]{1}), "bytes");
    }
}
