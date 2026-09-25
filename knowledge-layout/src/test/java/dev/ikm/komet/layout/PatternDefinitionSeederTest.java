package dev.ikm.komet.layout;

import dev.ikm.tinkar.common.id.PublicId;
import dev.ikm.tinkar.common.id.PublicIds;
import dev.ikm.tinkar.common.util.uuid.UuidT5Generator;
import org.junit.jupiter.api.Test;

import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * Unit-tests {@link PatternDefinitionSeeder}'s pure part (no data store): which
 * pattern-definition semantic identities are the projection's own, which decides whether a
 * pattern is defined by its semantics (created in the standard Pattern window) or the semantics
 * are projected from its inline definition.
 */
class PatternDefinitionSeederTest {

    private static final PublicId DESCRIBED_PATTERN = PublicIds.of(
            UUID.fromString("a4de0039-2bb2-5f4e-b1b3-8f9c0e1e0d33"));
    private static final PublicId OTHER_PATTERN = PublicIds.of(
            UUID.fromString("2f236377-2da7-49bf-8802-fd0fd2dfcdb5"));

    private static PublicId projectedId(PublicId pattern, String discriminator) {
        return PublicIds.of(UuidT5Generator.get(pattern.asUuidArray()[0], discriminator));
    }

    @Test
    void theProjectionsOwnIdentitiesAreProjected() {
        assertTrue(PatternDefinitionSeeder.isProjectedSemanticId(DESCRIBED_PATTERN,
                projectedId(DESCRIBED_PATTERN, "meaning-and-purpose"), 0), "meaning and purpose");
        assertTrue(PatternDefinitionSeeder.isProjectedSemanticId(DESCRIBED_PATTERN,
                projectedId(DESCRIBED_PATTERN, "field-0"), 2), "first field");
        assertTrue(PatternDefinitionSeeder.isProjectedSemanticId(DESCRIBED_PATTERN,
                projectedId(DESCRIBED_PATTERN, "field-1"), 2), "last field");
    }

    @Test
    void aFieldIdentityBeyondThePatternsFieldSemanticsIsNotProjected() {
        // The pattern has two field semantics: only field-0 and field-1 can be its projections.
        assertFalse(PatternDefinitionSeeder.isProjectedSemanticId(DESCRIBED_PATTERN,
                projectedId(DESCRIBED_PATTERN, "field-2"), 2));
    }

    @Test
    void aComposerMintedIdentityIsNotProjected() {
        // A semantic created in the standard Pattern window gets a random identity from the composer.
        assertFalse(PatternDefinitionSeeder.isProjectedSemanticId(DESCRIBED_PATTERN,
                PublicIds.newRandom(), 3));
    }

    @Test
    void anotherPatternsProjectionIsNotThisPatterns() {
        assertFalse(PatternDefinitionSeeder.isProjectedSemanticId(DESCRIBED_PATTERN,
                projectedId(OTHER_PATTERN, "meaning-and-purpose"), 1));
        assertFalse(PatternDefinitionSeeder.isProjectedSemanticId(DESCRIBED_PATTERN,
                projectedId(OTHER_PATTERN, "field-0"), 1));
    }
}
