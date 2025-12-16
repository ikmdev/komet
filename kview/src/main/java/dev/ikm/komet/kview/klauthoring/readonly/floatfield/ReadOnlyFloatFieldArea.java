package dev.ikm.komet.kview.klauthoring.readonly.floatfield;

import dev.ikm.komet.framework.observable.Feature;
import dev.ikm.komet.framework.observable.FeatureKey;
import dev.ikm.komet.kview.controls.KLReadOnlyDataTypeControl;
import dev.ikm.komet.layout.area.AreaGridSettings;
import dev.ikm.komet.layout.area.KlAreaForFloat;
import dev.ikm.komet.layout.preferences.KlPreferencesFactory;
import dev.ikm.komet.layout_engine.blueprint.FeatureAreaBlueprint;
import dev.ikm.komet.preferences.KometPreferences;

import java.util.*;

/**
 * A read-only area for displaying Float fields within the Komet framework.
 * This area utilizes a KLReadOnlyDataTypeControl to present the Float value
 * in a non-editable format.
 */
public final class ReadOnlyFloatFieldArea extends FeatureAreaBlueprint<Float, Feature<Float>, KLReadOnlyDataTypeControl<Float>>
        implements KlAreaForFloat<KLReadOnlyDataTypeControl<Float>> {
    private ReadOnlyFloatFieldArea(KometPreferences preferences) {
        super(preferences, new KLReadOnlyDataTypeControl<>(Float.class));
    }

    private ReadOnlyFloatFieldArea(KlPreferencesFactory preferencesFactory, KlAreaForFloat.Factory areaFactory) {
        super(preferencesFactory, areaFactory, new KLReadOnlyDataTypeControl<>(Float.class));
    }

    @Override
    public Optional<FeatureKey> getAreaFeatureKey() {
        return KlAreaForFloat.super.getAreaFeatureKey();
    }

    @Override
    protected void subFeatureAreaBlueprintRestoreFromPreferencesOrDefault() {
        // Since this is a "leaf node", we don't need to worry about propagating the context change to children areas."
        getFeature().ifPresent(this::setDisplayValues);
    }


    @Override
    protected void subAreaRevert() {
    }

    @Override
    protected void subAreaSave() {
    }

    @Override
    public void contextChanged() {
        getFeature().ifPresent(this::setDisplayValues);
    }

    @Override
    protected void featureChanged(Feature<Float> oldFeature, Feature<Float> newFeature) {
        if (newFeature != null) {
            setDisplayValues(newFeature);
        }
    }

    /**
     * Sets the display values of the read-only Float field area and title (based on meaning).
     * @param newValue the new Feature<Float> to display
     */
    private void setDisplayValues(Feature<Float> newValue) {
        getFxPeer().setTitle(calculatorForContext().getDescriptionTextOrNid(newValue.definition(calculatorForContext()).purposeNid()));
        getFxPeer().setValue(newValue.value());
    }

    public static Factory factory() {
        return new Factory();
    }

    public static class Factory implements KlAreaForFloat.Factory {

        public Factory() {}

        @Override
        public String productName() {
            return ReadOnlyFloatFieldArea.class.getSimpleName();
        }

        @Override
        public ReadOnlyFloatFieldArea restore(KometPreferences preferences) {
            return new ReadOnlyFloatFieldArea(preferences);
        }

        @Override
        public ReadOnlyFloatFieldArea create(KlPreferencesFactory preferencesFactory, AreaGridSettings areaGridSettings) {
            ReadOnlyFloatFieldArea area = new ReadOnlyFloatFieldArea(preferencesFactory, this);
            area.setAreaLayout(areaGridSettings);
            return area;
        }
    }
}
