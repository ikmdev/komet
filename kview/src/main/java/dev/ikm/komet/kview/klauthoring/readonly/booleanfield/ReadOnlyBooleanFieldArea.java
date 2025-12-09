package dev.ikm.komet.kview.klauthoring.readonly.booleanfield;

import dev.ikm.komet.framework.observable.Feature;
import dev.ikm.komet.framework.observable.FeatureKey;
import dev.ikm.komet.kview.controls.KLReadOnlyDataTypeControl;
import dev.ikm.komet.layout.area.AreaGridSettings;
import dev.ikm.komet.layout.area.KlAreaForBoolean;
import dev.ikm.komet.layout.preferences.KlPreferencesFactory;
import dev.ikm.komet.layout_engine.blueprint.FeatureAreaBlueprint;
import dev.ikm.komet.preferences.KometPreferences;

import java.util.*;

/**
 * A read-only area for displaying boolean fields within the Komet framework.
 * This area utilizes a KLReadOnlyDataTypeControl to present the boolean value
 * in a non-editable format.
 */
public final class ReadOnlyBooleanFieldArea extends FeatureAreaBlueprint<Boolean, Feature<Boolean>, KLReadOnlyDataTypeControl<Boolean>>
        implements KlAreaForBoolean<KLReadOnlyDataTypeControl<Boolean>> {
    private ReadOnlyBooleanFieldArea(KometPreferences preferences) {
        super(preferences, new KLReadOnlyDataTypeControl<>(Boolean.class));
    }

    private ReadOnlyBooleanFieldArea(KlPreferencesFactory preferencesFactory, ReadOnlyBooleanFieldArea.Factory areaFactory) {
        super(preferencesFactory, areaFactory, new KLReadOnlyDataTypeControl<>(Boolean.class));
    }

    @Override
    public Optional<FeatureKey> getAreaFeatureKey() {
        return KlAreaForBoolean.super.getAreaFeatureKey();
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
    protected void featureChanged(Feature<Boolean> oldFeature, Feature<Boolean> newFeature) {
        if (newFeature != null) {
            setDisplayValues(newFeature);
        }
    }

    /**
     * Sets the display values of the read-only boolean field area and title (based on meaning).
     * @param newValue
     */
    private void setDisplayValues(Feature<Boolean> newValue) {
        getFxPeer().setTitle(calculatorForContext().getDescriptionTextOrNid(newValue.definition(calculatorForContext()).purposeNid()));
        getFxPeer().setValue(newValue.value());
    }

    public static Factory factory() {
        return new Factory();
    }

    public static class Factory implements KlAreaForBoolean.Factory {

        public Factory() {}

        @Override
        public String productName() {
            return ReadOnlyBooleanFieldArea.class.getSimpleName();
        }

        @Override
        public ReadOnlyBooleanFieldArea restore(KometPreferences preferences) {
            return new ReadOnlyBooleanFieldArea(preferences);
        }

        @Override
        public ReadOnlyBooleanFieldArea create(KlPreferencesFactory preferencesFactory, AreaGridSettings areaGridSettings) {
            ReadOnlyBooleanFieldArea area = new ReadOnlyBooleanFieldArea(preferencesFactory, this);
            area.setAreaLayout(areaGridSettings);
            return area;
        }
    }
}
