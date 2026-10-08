package dev.ikm.komet.layout.expand;

import dev.ikm.tinkar.common.bind.EnumConceptBinding;
import dev.ikm.tinkar.common.id.PublicId;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.TreeSet;
import java.util.UUID;
import java.util.function.Function;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
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
        List<ExpansionCorner> corners = List.of(ExpansionCorner.values());
        assertNoneShareAUuid(corners, ExpansionCorner::publicId);
        assertEquals(4, new TreeSet<>(corners.stream().map(ExpansionCorner::publicId).toList()).size(),
                "all corner identities present and distinct");
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
        assertTrue(concept.publicId().contains(UUID.fromString(expectedUuid)),
                "FROZEN identity forked for " + concept);
    }

    /** No two of the identities share a UUID: public ids that share any UUID are the same component. */
    private static <T> void assertNoneShareAUuid(List<T> components, Function<T, PublicId> publicId) {
        for (int i = 0; i < components.size(); i++) {
            for (int j = i + 1; j < components.size(); j++) {
                assertFalse(PublicId.equals(publicId.apply(components.get(i)), publicId.apply(components.get(j))),
                        "shared identity: " + components.get(i) + " and " + components.get(j));
            }
        }
    }
}
