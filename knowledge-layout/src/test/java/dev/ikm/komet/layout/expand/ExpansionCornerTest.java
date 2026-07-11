package dev.ikm.komet.layout.expand;

import dev.ikm.tinkar.common.bind.EnumConceptBinding;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import org.junit.jupiter.api.Test;

import java.util.HashSet;
import java.util.Set;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * Freezes the {@link ExpansionCorner} concept identities: the pinned UUIDs are frozen ids — if this
 * fails after an edit, the edit forked an identity. Also guards distinctness, the default, and the
 * {@link Pos} mapping.
 */
class ExpansionCornerTest {

    @Test
    void identitiesAreFrozen() {
        assertId(ExpansionCorner.TOP_LEFT, "e0d4f79c-ba0d-5da9-9aba-b95d98433390");
        assertId(ExpansionCorner.TOP_RIGHT, "35c078a2-a499-5919-9cfc-c0c624a40e49");
        assertId(ExpansionCorner.BOTTOM_LEFT, "df862aa4-78d4-5c41-9394-d0ff98f471ec");
        assertId(ExpansionCorner.BOTTOM_RIGHT, "93f4b913-1437-5955-a0dc-b93f147e0473");
    }

    @Test
    void identitiesAreDistinct() {
        Set<UUID> seen = new HashSet<>();
        for (ExpansionCorner corner : ExpansionCorner.values()) {
            assertTrue(seen.add(corner.publicId().asUuidArray()[0]), "duplicate identity for " + corner);
        }
        assertEquals(4, seen.size(), "all corner identities present and distinct");
    }

    @Test
    void defaultIsBottomRight() {
        assertEquals(ExpansionCorner.BOTTOM_RIGHT, ExpansionCorner.DEFAULT);
    }

    @Test
    void mapsToPos() {
        assertEquals(Pos.TOP_LEFT, ExpansionCorner.TOP_LEFT.pos());
        assertEquals(Pos.TOP_RIGHT, ExpansionCorner.TOP_RIGHT.pos());
        assertEquals(Pos.BOTTOM_LEFT, ExpansionCorner.BOTTOM_LEFT.pos());
        assertEquals(Pos.BOTTOM_RIGHT, ExpansionCorner.BOTTOM_RIGHT.pos());
    }

    @Test
    void clearsOnlyTheScrollBarsSharingTheCorner() {
        Insets base = new Insets(6);
        // A vertical bar occupies the right edge; a horizontal bar the bottom edge.
        assertEquals(new Insets(6, 6 + 15, 6 + 12, 6),
                ExpansionCorner.BOTTOM_RIGHT.insetsClearing(base, 15, 12));
        // Bottom-left shares the horizontal bar's edge only.
        assertEquals(new Insets(6, 6, 6 + 12, 6),
                ExpansionCorner.BOTTOM_LEFT.insetsClearing(base, 15, 12));
        // Top-right shares the vertical bar's edge only.
        assertEquals(new Insets(6, 6 + 15, 6, 6),
                ExpansionCorner.TOP_RIGHT.insetsClearing(base, 15, 12));
        // Top-left shares neither.
        assertEquals(base, ExpansionCorner.TOP_LEFT.insetsClearing(base, 15, 12));
    }

    @Test
    void clearingIsTheBaseWhenNoBarsAreVisible() {
        Insets base = new Insets(6);
        for (ExpansionCorner corner : ExpansionCorner.values()) {
            assertEquals(base, corner.insetsClearing(base, 0, 0), "no bars → base margin for " + corner);
        }
    }

    @Test
    void clearingToleratesNullAndNegativeInputs() {
        assertEquals(Insets.EMPTY, ExpansionCorner.BOTTOM_RIGHT.insetsClearing(null, 0, 0));
        assertEquals(new Insets(6), ExpansionCorner.BOTTOM_RIGHT.insetsClearing(new Insets(6), -4, -4));
    }

    private static void assertId(EnumConceptBinding concept, String expectedUuid) {
        assertEquals(UUID.fromString(expectedUuid), concept.publicId().asUuidArray()[0],
                "FROZEN identity forked for " + concept);
    }
}
