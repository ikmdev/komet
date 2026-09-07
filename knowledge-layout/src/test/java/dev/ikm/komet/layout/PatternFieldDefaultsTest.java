package dev.ikm.komet.layout;

import dev.ikm.tinkar.common.id.IntIds;
import dev.ikm.tinkar.common.id.PublicId;
import dev.ikm.tinkar.common.id.PublicIds;
import dev.ikm.tinkar.terms.EntityProxy;
import org.junit.jupiter.api.Test;

import java.util.Arrays;
import java.util.List;
import java.util.UUID;

import static dev.ikm.komet.terms.KometTerm.BLANK_CONCEPT;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * Unit-tests {@link PatternFieldDefaults}' pure parts (no data store): which values count as
 * blank, which fields a default applies to, and the defaults semantic identity derivation.
 */
class PatternFieldDefaultsTest {

    private static final EntityProxy.Concept ENGLISH = EntityProxy.Concept.make("English language",
            UUID.fromString("0f3a5cfd-0a0c-4b4d-9a62-3d6a9b2b6a01"));

    @Test
    void blankIsWhatTheComposerStartsAFieldWith() {
        // The rules themselves are the composer's (see ObservableComposerDefaultFieldValuesTest);
        // this only pins the delegation.
        assertTrue(PatternFieldDefaults.isBlank(BLANK_CONCEPT), "the blank concept");
        assertTrue(PatternFieldDefaults.isBlank(""), "empty string");
        assertFalse(PatternFieldDefaults.isBlank(ENGLISH), "a concept");
        assertFalse(PatternFieldDefaults.isBlank("Myocardial infarction"), "text");
    }

    @Test
    void aDefaultAppliesOnlyToBlankFieldsWithANonBlankDefault() {
        // A new description: language, text, case significance, description type.
        List<Object> newSemantic = Arrays.asList(BLANK_CONCEPT, "", BLANK_CONCEPT, ENGLISH);
        List<Object> defaults = Arrays.asList(ENGLISH, "", ENGLISH, BLANK_CONCEPT);

        assertEquals(List.of(0, 2), PatternFieldDefaults.defaultedFieldIndices(newSemantic, defaults),
                "blank fields with a default take it; a field already holding a value (seeded by a filter)"
                        + " and a field whose default is blank are left alone");
    }

    @Test
    void fieldsBeyondTheShorterListNeverTakeADefault() {
        List<Object> newSemantic = Arrays.asList(BLANK_CONCEPT, "", BLANK_CONCEPT);
        List<Object> staleDefaults = Arrays.asList(ENGLISH, "", ENGLISH, ENGLISH, ENGLISH);

        assertEquals(List.of(0, 2), PatternFieldDefaults.defaultedFieldIndices(newSemantic, staleDefaults));
        assertEquals(List.of(), PatternFieldDefaults.defaultedFieldIndices(List.of(), staleDefaults));
    }

    @Test
    void defaultsSemanticIdentityIsDerivedFromThePattern() {
        PublicId descriptionPattern = PublicIds.of(UUID.fromString("a4de0039-2bb2-5f4e-b1b3-8f9c0e1e0d33"));
        PublicId otherPattern = PublicIds.of(UUID.fromString("2f236377-2da7-49bf-8802-fd0fd2dfcdb5"));

        assertTrue(PublicId.equals(PatternFieldDefaults.defaultsSemanticId(descriptionPattern),
                PatternFieldDefaults.defaultsSemanticId(descriptionPattern)), "stable for the same pattern");
        assertFalse(PublicId.equals(PatternFieldDefaults.defaultsSemanticId(descriptionPattern),
                PatternFieldDefaults.defaultsSemanticId(otherPattern)), "distinct per pattern");
        assertNotEquals(descriptionPattern.asUuidArray()[0],
                PatternFieldDefaults.defaultsSemanticId(descriptionPattern).asUuidArray()[0],
                "never the pattern's own identity");
    }
}
