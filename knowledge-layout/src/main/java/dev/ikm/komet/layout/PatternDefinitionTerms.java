package dev.ikm.komet.layout;

import dev.ikm.tinkar.terms.EntityProxy;

import java.util.UUID;

/**
 * Bindings for the pattern-definition patterns: the patterns through which a pattern is itself
 * described — its meaning and purpose, and its fields. They are seeded into the data store by
 * {@link PatternDefinitionSeeder} so they appear in the KL editor pattern browser and behave
 * like any other pattern.
 * <p>
 * The UUIDs are fixed so that a future starter-data release can adopt the same identities and
 * merge with already-seeded stores by public id.
 */
public final class PatternDefinitionTerms {

    /**
     * Pattern whose semantics state the meaning and purpose of the referenced pattern.
     * Field 0 is a Component holding the pattern's meaning; field 1 is a Component holding
     * the pattern's purpose.
     */
    public static final EntityProxy.Pattern MEANING_AND_PURPOSE_PATTERN = EntityProxy.Pattern.make(
            "Meaning and Purpose Pattern", UUID.fromString("2f236377-2da7-49bf-8802-fd0fd2dfcdb5"));

    /**
     * Pattern whose semantics each describe one field of the referenced pattern.
     * Field 0 is a Component holding the field's data type; field 1 is a Component holding the
     * field's purpose; field 2 is a Component holding the field's meaning.
     */
    public static final EntityProxy.Pattern FIELDS_PATTERN = EntityProxy.Pattern.make(
            "Fields Pattern", UUID.fromString("8fafdacc-d1c0-4006-9965-53d3e05eaae7"));

    /** Purpose of both pattern-definition patterns: defining a pattern. */
    public static final EntityProxy.Concept PATTERN_DEFINITION = EntityProxy.Concept.make(
            "Pattern definition", UUID.fromString("03ac5b86-0965-4816-a5a2-e64aa8fe011e"));

    /** Meaning of the {@link #MEANING_AND_PURPOSE_PATTERN}. */
    public static final EntityProxy.Concept MEANING_AND_PURPOSE = EntityProxy.Concept.make(
            "Meaning and purpose", UUID.fromString("fdebcbf1-0b56-42da-8e6b-912d1e22f837"));

    /** Meaning of the {@link #FIELDS_PATTERN}. */
    public static final EntityProxy.Concept FIELD_DEFINITION = EntityProxy.Concept.make(
            "Field definition", UUID.fromString("8802ae3d-9af5-4d31-b69f-74766e70df16"));

    private PatternDefinitionTerms() {
    }
}
