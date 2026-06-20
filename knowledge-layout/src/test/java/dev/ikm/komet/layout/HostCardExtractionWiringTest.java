package dev.ikm.komet.layout;

import dev.ikm.komet.layout_engine.blueprint.CardBlueprint;
import dev.ikm.komet.layout_engine.host.AbstractHostCard;
import dev.ikm.komet.layout_engine.host.DynamicCard;
import dev.ikm.komet.layout_engine.host.DynamicComponentCard;
import dev.ikm.komet.layout_engine.host.ToolCard;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import org.junit.jupiter.api.Test;

import java.lang.reflect.Modifier;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * Locks the {@link AbstractHostCard} extraction and the {@link ToolCard} variant (ike-issues: Card
 * unification — host a tool area as a card).
 *
 * <p>Like {@link KlCardSealedWiringTest}, these are pure type/metadata assertions — no datastore, JavaFX
 * toolkit, or preferences are required — so they run anywhere and stand as the compiler-visible safety
 * proof that (a) the shared card-host infrastructure was extracted into {@code AbstractHostCard} without
 * changing the {@code DynamicCard}/{@code DynamicComponentCard} contracts, (b) {@code ToolCard} is a
 * first-class card sharing that base, and (c) the host-card content/chrome hooks stay the variation
 * points. The runtime behaviors the unit harness cannot exercise (per-instance preferences sandboxing of
 * the hosted tool; save/restore round-trip; drag) are verified in the running workspace.
 */
class HostCardExtractionWiringTest {

    @Test
    void abstractHostCardIsTheSharedCardBase() {
        assertTrue(Modifier.isAbstract(AbstractHostCard.class.getModifiers()),
                "AbstractHostCard must be abstract (it is never instantiated directly)");
        assertTrue(CardBlueprint.class.isAssignableFrom(AbstractHostCard.class),
                "AbstractHostCard must extend the CardBlueprint base");
        assertTrue(KlCard.class.isAssignableFrom(AbstractHostCard.class),
                "AbstractHostCard must be a KlCard");
    }

    @Test
    void dynamicCardAndToolCardShareTheHostBase() {
        // The extraction inserted AbstractHostCard directly above DynamicCard and ToolCard.
        assertEquals(AbstractHostCard.class, DynamicCard.class.getSuperclass(),
                "DynamicCard must extend AbstractHostCard");
        assertEquals(AbstractHostCard.class, ToolCard.class.getSuperclass(),
                "ToolCard must extend AbstractHostCard");
        // DynamicComponentCard remains a DynamicCard (identity header + history) — unchanged by the extraction.
        assertEquals(DynamicCard.class, DynamicComponentCard.class.getSuperclass(),
                "DynamicComponentCard must remain a DynamicCard");
    }

    @Test
    void toolCardRealizesTheCardContracts() {
        assertTrue(KlCard.class.isAssignableFrom(ToolCard.class), "ToolCard must be a KlCard");
        assertTrue(CardBlueprint.class.isAssignableFrom(ToolCard.class),
                "ToolCard must extend the CardBlueprint base");
        assertTrue(KlParent.class.isAssignableFrom(ToolCard.class),
                "ToolCard must be a KlParent (it hosts a child tool area)");
        assertTrue(KlCard.Factory.class.isAssignableFrom(ToolCard.Factory.class),
                "ToolCard.Factory must be a KlCard.Factory");
        assertTrue(KlArea.Factory.class.isAssignableFrom(ToolCard.Factory.class),
                "ToolCard.Factory must transitively be a KlArea.Factory");
    }

    @Test
    void hostCardContentAndTitleAreAbstractVariationPoints() throws NoSuchMethodException {
        // Every host card MUST supply its content and its tab title — these are abstract on the base.
        assertTrue(Modifier.isAbstract(AbstractHostCard.class.getDeclaredMethod("renderContent").getModifiers()),
                "renderContent() must be abstract (the content is the variation point)");
        assertTrue(Modifier.isAbstract(AbstractHostCard.class.getDeclaredMethod("cardTitle").getModifiers()),
                "cardTitle() must be abstract (every card names its own tab)");
    }

    @Test
    void hostCardChromeAndPersistenceHooksAreOverridableNotAbstract() throws NoSuchMethodException {
        // The chrome + content-persistence hooks are overridable (declared, non-abstract) so a minimal
        // card (e.g. ToolCard) inherits sensible defaults while a rich card (DynamicCard) overrides them.
        for (String noArgHook : List.of("clearContent", "refreshHeader", "subCardSave", "subCardRestore")) {
            assertFalse(Modifier.isAbstract(AbstractHostCard.class.getDeclaredMethod(noArgHook).getModifiers()),
                    noArgHook + "() must be an overridable (non-abstract) hook");
        }
        assertFalse(Modifier.isAbstract(
                        AbstractHostCard.class.getDeclaredMethod("buildToolbarControls", HBox.class).getModifiers()),
                "buildToolbarControls(HBox) must be an overridable hook");
        assertFalse(Modifier.isAbstract(
                        AbstractHostCard.class.getDeclaredMethod("contributeToHeader", VBox.class, HBox.class).getModifiers()),
                "contributeToHeader(VBox, HBox) must be an overridable hook");
    }

    @Test
    void toolCardOverridesTheContentPersistenceHooks() throws NoSuchMethodException {
        // ToolCard persists its hosted-tool identity through the framework save/restore via these hooks.
        assertEquals(ToolCard.class, ToolCard.class.getDeclaredMethod("subCardSave").getDeclaringClass(),
                "ToolCard must override subCardSave to persist its hosted-tool identity");
        assertEquals(ToolCard.class, ToolCard.class.getDeclaredMethod("subCardRestore").getDeclaringClass(),
                "ToolCard must override subCardRestore to restore its hosted-tool identity");
    }
}
