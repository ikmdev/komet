package dev.ikm.komet.kview.klauthoring.readonly.integerfield;

import dev.ikm.komet.framework.observable.Feature;
import dev.ikm.komet.framework.observable.FeatureKey;
import dev.ikm.komet.kview.controls.KLReadOnlyDataTypeControl;
import dev.ikm.komet.layout.area.AreaGridSettings;
import dev.ikm.komet.layout.area.KlAreaForInteger;
import dev.ikm.komet.layout.preferences.KlPreferencesFactory;
import dev.ikm.komet.layout_engine.blueprint.FeatureAreaBlueprint;
import dev.ikm.komet.preferences.KometPreferences;

import java.util.*;

/**
 * A read-only area for displaying Integer fields within the Komet framework.
 * This area utilizes a KLReadOnlyDataTypeControl to present the Integer value
 * in a non-editable format.
 */
public final class ReadOnlyIntegerFieldArea extends FeatureAreaBlueprint<Integer, Feature<Integer>, KLReadOnlyDataTypeControl<Integer>>
        implements KlAreaForInteger<KLReadOnlyDataTypeControl<Integer>> {
    private ReadOnlyIntegerFieldArea(KometPreferences preferences) {
        super(preferences, new KLReadOnlyDataTypeControl<>(Integer.class));
    }

    private ReadOnlyIntegerFieldArea(KlPreferencesFactory preferencesFactory, ReadOnlyIntegerFieldArea.Factory areaFactory) {
        super(preferencesFactory, areaFactory, new KLReadOnlyDataTypeControl<>(Integer.class));
    }

    @Override
    public Optional<FeatureKey> getAreaFeatureKey() {
        return KlAreaForInteger.super.getAreaFeatureKey();
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
    protected void featureChanged(Feature<Integer> oldFeature, Feature<Integer> newFeature) {
        if (newFeature != null) {
            setDisplayValues(newFeature);
        }
    }

    /**
     * Sets the display values of the read-only Integer field area and title (based on meaning).
     * @param newValue the new Feature<Integer> to display
     */
    private void setDisplayValues(Feature<Integer> newValue) {
        getFxPeer().setTitle(calculatorForContext().getDescriptionTextOrNid(newValue.definition(calculatorForContext()).purposeNid()));
        getFxPeer().setValue(newValue.value());
    }

    public static Factory factory() {
        return new Factory();
    }

    public static class Factory implements KlAreaForInteger.Factory {

        public Factory() {}

        @Override
        public String productName() {
            return ReadOnlyIntegerFieldArea.class.getSimpleName();
        }

        @Override
        public ReadOnlyIntegerFieldArea restore(KometPreferences preferences) {
            return new ReadOnlyIntegerFieldArea(preferences);
        }

        @Override
        public ReadOnlyIntegerFieldArea create(KlPreferencesFactory preferencesFactory, AreaGridSettings areaGridSettings) {
            ReadOnlyIntegerFieldArea area = new ReadOnlyIntegerFieldArea(preferencesFactory, this);
            area.setAreaLayout(areaGridSettings);
            return area;
        }
    }
}
