package dev.ikm.komet.layout_engine.blueprint;

import dev.ikm.komet.layout.KlArea;
import dev.ikm.komet.layout.KlCard;
import dev.ikm.komet.layout.preferences.KlPreferencesFactory;
import dev.ikm.komet.preferences.KometPreferences;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.GridPane;

/**
 * Blueprint base for {@link KlCard} implementations — the engine-side plumbing for a tier-four
 * <em>Card</em> (a movable panel on a workspace Surface). It supplies the full
 * {@link StateAndContextBlueprint} machinery (preferences-backed state, view context, lifecycle,
 * save/revert/restore) and a {@link BorderPane} root whose center is the card's
 * {@linkplain #gridPaneForChildren() children grid}, exactly mirroring
 * {@link SupplementalAreaBlueprint}.
 *
 * <p>It is a non-sealed member of the sealed {@link AreaBlueprint} family, so concrete cards (for
 * example {@code DynamicCard}) extend it the same way concrete supplemental areas extend
 * {@link SupplementalAreaBlueprint}.
 */
public non-sealed abstract class CardBlueprint extends AreaBlueprint<BorderPane>
        implements KlCard<BorderPane> {

    /** The grid that hosts this card's child {@link KlArea}s; installed as the root's center. */
    protected final GridPane gridPaneForChildren = new GridPane();

    {
        gridPaneForChildren.setAccessibleRoleDescription("Card Children GridPane");
        fxObject().setCenter(gridPaneForChildren);
    }

    /**
     * Restores a card blueprint from previously stored preferences.
     *
     * @param preferences the preferences node backing this card
     */
    public CardBlueprint(KometPreferences preferences) {
        super(preferences, new BorderPane());
    }

    /**
     * Creates a new card blueprint from a preferences factory and the producing area factory.
     *
     * @param preferencesFactory the factory that provisions this card's preferences node
     * @param areaFactory        the {@link KlCard.Factory} (as a {@link KlArea.Factory}) producing this card
     */
    public CardBlueprint(KlPreferencesFactory preferencesFactory, KlArea.Factory areaFactory) {
        super(preferencesFactory, areaFactory, new BorderPane());
    }

    @Override
    public final GridPane gridPaneForChildren() {
        return gridPaneForChildren;
    }

    /**
     * The factory contract for concrete cards, parameterized to a {@link BorderPane}-rooted
     * {@link KlCard}. Mirrors {@link SupplementalAreaBlueprint.Factory}.
     *
     * @param <KL> the concrete {@link KlCard} type produced
     */
    public interface Factory<KL extends KlCard<BorderPane>>
            extends KlCard.Factory<BorderPane, KL> {
    }
}
