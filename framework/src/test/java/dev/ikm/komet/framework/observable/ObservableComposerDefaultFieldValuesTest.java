package dev.ikm.komet.framework.observable;

import dev.ikm.tinkar.common.id.IntIds;
import dev.ikm.tinkar.terms.EntityProxy;
import org.junit.jupiter.api.Test;

import java.util.UUID;

import static dev.ikm.komet.terms.KometTerm.BLANK_CONCEPT;
import static org.junit.jupiter.api.Assertions.assertFalse;
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
                EntityProxy.Concept.make("Blank", BLANK_CONCEPT.asUuidArray()[0])), "any facade of the blank concept");
        assertTrue(ObservableComposer.isDefaultFieldValue(""), "empty string");
        assertTrue(ObservableComposer.isDefaultFieldValue(0), "zero integer");
        assertTrue(ObservableComposer.isDefaultFieldValue(0.0F), "zero float");
        assertTrue(ObservableComposer.isDefaultFieldValue(false), "false");
        assertTrue(ObservableComposer.isDefaultFieldValue(IntIds.list.empty()), "empty component list");
        assertTrue(ObservableComposer.isDefaultFieldValue(IntIds.set.empty()), "empty component set");
        assertTrue(ObservableComposer.isDefaultFieldValue(new byte[0]), "empty byte array");
    }

    @Test
    void valuesAnAuthorEnteredAreNotDefault() {
        assertFalse(ObservableComposer.isDefaultFieldValue(ENGLISH), "a concept");
        assertFalse(ObservableComposer.isDefaultFieldValue("Myocardial infarction"), "text");
        assertFalse(ObservableComposer.isDefaultFieldValue(" "), "a space is entered text, not a blank field");
        assertFalse(ObservableComposer.isDefaultFieldValue(3), "a non-zero integer");
        assertFalse(ObservableComposer.isDefaultFieldValue(1.5F), "a non-zero float");
        assertFalse(ObservableComposer.isDefaultFieldValue(true), "true");
        assertFalse(ObservableComposer.isDefaultFieldValue(IntIds.list.of(1, 2)), "a component list with entries");
        assertFalse(ObservableComposer.isDefaultFieldValue(new byte[]{1}), "bytes");
    }
}
