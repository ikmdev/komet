package dev.ikm.komet.layout_engine.blueprint;

import dev.ikm.komet.framework.observable.Feature;
import dev.ikm.komet.layout.KlArea;
import dev.ikm.komet.layout.KlViewLayoutLifecycle;
import dev.ikm.komet.layout.area.KlAreaForFeature;
import dev.ikm.komet.layout.area.KlFeaturePropertyForArea;
import dev.ikm.komet.layout.preferences.KlPreferencesFactory;
import dev.ikm.komet.preferences.KometPreferences;
import javafx.beans.property.Property;
import javafx.beans.property.ReadOnlyProperty;
import javafx.scene.layout.Region;

import java.util.Optional;

public non-sealed abstract class FeatureAreaBlueprint<DT, F extends Feature<DT>, FX extends Region>
        extends AreaBlueprint<FX> implements KlFeaturePropertyForArea<F>, KlViewLayoutLifecycle {

    final FeaturePropertyHelper<F> featurePropertyHelper = new FeaturePropertyHelper<>(this.preferences(), this.lifecycleState, this::featureChanged);

    /**
     * Constructs a new {@code FieldPaneBlueprint} object by initializing it with the
     * specified preferences and UI gadget component.
     *
     * @param preferences the {@code KometPreferences} instance used to manage and
     *                    restore settings for this field pane blueprint.
     * @param fxGadget    the gadget instance of type {@code T} to be used as the primary
     *                    UI component for constructing and managing the field pane.
     */
    protected FeatureAreaBlueprint(KometPreferences preferences, FX fxGadget) {
        super(preferences, fxGadget);
    }

    /**
     * Constructs a new {@code FieldPaneBlueprint} instance with the specified preferences factory,
     * gadget factory, and UI gadget component. This constructor initializes the field pane blueprint
     * by delegating configuration to its superclass.
     *
     * @param preferencesFactory the factory managing and creating preference-related configurations
     *                           for the field pane blueprint.
     * @param areaFactory      the factory responsible for providing metadata and configurations
     *                           related to the UI gadget.
     * @param fxPeer           the UI gadget of type {@code FX} used as the primary component for
     *                           constructing and managing the field pane blueprint.
     */
    protected FeatureAreaBlueprint(KlPreferencesFactory preferencesFactory, KlArea.Factory areaFactory,
                                   FX fxPeer) {
        super(preferencesFactory, areaFactory, fxPeer);
    }

    protected final void subAreaRestoreFromPreferencesOrDefault() {
        featurePropertyHelper.restoreFromPreferencesOrDefaults();
        subFeatureAreaBlueprintRestoreFromPreferencesOrDefault();
    }

    protected abstract void subFeatureAreaBlueprintRestoreFromPreferencesOrDefault();


    protected abstract void featureChanged(F oldFeature, F newFeature);

    public final Property<ReadOnlyProperty<F>> featurePropertyWrapper() {
        return this.featurePropertyHelper.featurePropertyWrapper();
    }

    public Optional<F> getFeature() {
        return featurePropertyHelper.getFeature();
    }


    @Override
    public void knowledgeLayoutUnbind() {
        featurePropertyHelper.knowledgeLayoutUnbind();
        this.forget();
    }

    @Override
    public final void knowledgeLayoutBind() {
        featurePropertyHelper.knowledgeLayoutBind();
    }

    public interface Factory<DT, F extends Feature<DT>, FX extends Region, KL extends KlAreaForFeature<DT, F, FX>>
            extends KlAreaForFeature.Factory<DT, F, FX, KL> {
    }
}
