package dev.ikm.komet.layout;

import dev.ikm.komet.layout.area.KlSupplementalArea;
import dev.ikm.komet.layout_engine.blueprint.AreaBlueprint;
import dev.ikm.komet.layout_engine.blueprint.CardBlueprint;
import dev.ikm.komet.layout_engine.host.DynamicCard;
import org.junit.jupiter.api.Test;

import java.util.Arrays;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * Locks the sealed-hierarchy contract for the {@code Card} tier (ike-issues: Card vocabulary).
 *
 * <p>These are pure type/metadata assertions — no datastore, JavaFX toolkit, or preferences
 * are required — so they run anywhere and stand as the compiler-visible safety proof that
 * inserting {@link KlCard} into the sealed {@link KlArea} / {@link KlArea.Factory} families and
 * {@link CardBlueprint} into the sealed {@link AreaBlueprint} family is and stays correct.
 */
class KlCardSealedWiringTest {

    private static List<Class<?>> permits(Class<?> sealedType) {
        Class<?>[] permitted = sealedType.getPermittedSubclasses();
        return permitted == null ? List.of() : Arrays.asList(permitted);
    }

    @Test
    void klCardIsAFirstClassMemberOfTheSealedKlAreaFamily() {
        assertTrue(KlArea.class.isSealed(), "KlArea must remain sealed");
        assertTrue(permits(KlArea.class).contains(KlCard.class),
                "KlCard must be a permitted subtype of the sealed KlArea");
    }

    @Test
    void klCardFactoryIsAFirstClassMemberOfTheSealedKlAreaFactoryFamily() {
        assertTrue(KlArea.Factory.class.isSealed(), "KlArea.Factory must remain sealed");
        assertTrue(permits(KlArea.Factory.class).contains(KlCard.Factory.class),
                "KlCard.Factory must be a permitted subtype of the sealed KlArea.Factory");
    }

    @Test
    void cardBlueprintIsAFirstClassMemberOfTheSealedAreaBlueprintFamily() {
        assertTrue(AreaBlueprint.class.isSealed(), "AreaBlueprint must remain sealed");
        assertTrue(permits(AreaBlueprint.class).contains(CardBlueprint.class),
                "CardBlueprint must be a permitted subtype of the sealed AreaBlueprint");
    }

    @Test
    void klCardIsAnOpenAreaAndParentLikeKlSupplementalArea() {
        // A card is both an area and a parent (hosts child areas), mirroring KlSupplementalArea.
        assertTrue(KlArea.class.isAssignableFrom(KlCard.class), "KlCard must be a KlArea");
        assertTrue(KlParent.class.isAssignableFrom(KlCard.class), "KlCard must be a KlParent");
        // Non-sealed so any module may contribute a card implementation.
        assertNull(KlCard.class.getPermittedSubclasses(), "KlCard must be non-sealed (open for implementation)");
        assertFalse(KlCard.class.isSealed(), "KlCard must not be sealed");
    }

    @Test
    void dynamicCardRealizesTheCardContracts() {
        assertTrue(KlCard.class.isAssignableFrom(DynamicCard.class), "DynamicCard must be a KlCard");
        assertTrue(CardBlueprint.class.isAssignableFrom(DynamicCard.class),
                "DynamicCard must extend the CardBlueprint base");
        assertTrue(KlParent.class.isAssignableFrom(DynamicCard.class),
                "DynamicCard must be a KlParent (it hosts child areas)");
        assertTrue(KlCard.Factory.class.isAssignableFrom(DynamicCard.Factory.class),
                "DynamicCard.Factory must be a KlCard.Factory");
        assertTrue(KlArea.Factory.class.isAssignableFrom(DynamicCard.Factory.class),
                "DynamicCard.Factory must transitively be a KlArea.Factory");
    }

    @Test
    void cardTierIsDistinctFromSupplementalAreaTier() {
        // Card and SupplementalArea are sibling, independent members of the KlArea family;
        // neither is a subtype of the other.
        assertFalse(KlSupplementalArea.class.isAssignableFrom(KlCard.class),
                "KlCard must not be a KlSupplementalArea");
        assertFalse(KlCard.class.isAssignableFrom(KlSupplementalArea.class),
                "KlSupplementalArea must not be a KlCard");
    }
}
