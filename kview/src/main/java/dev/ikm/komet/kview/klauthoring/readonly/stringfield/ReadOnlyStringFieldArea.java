package dev.ikm.komet.kview.klauthoring.readonly.stringfield;

import dev.ikm.komet.framework.observable.Feature;
import dev.ikm.komet.framework.observable.FeatureKey;
import dev.ikm.komet.kview.controls.KLReadOnlyDataTypeControl;
import dev.ikm.komet.layout.area.AreaGridSettings;
import dev.ikm.komet.layout.area.KlAreaForString;
import dev.ikm.komet.layout.preferences.KlPreferencesFactory;
import dev.ikm.komet.layout_engine.blueprint.FeatureAreaBlueprint;
import dev.ikm.komet.preferences.KometPreferences;

import java.util.*;

/**
 * A read-only area for displaying String fields within the Komet framework.
 * This area utilizes a KLReadOnlyDataTypeControl to present the boolean value
 * in a non-editable format.
 */
public final class ReadOnlyStringFieldArea extends FeatureAreaBlueprint<String, Feature<String>, KLReadOnlyDataTypeControl<String>>
        implements KlAreaForString<KLReadOnlyDataTypeControl<String>> {
    private ReadOnlyStringFieldArea(KometPreferences preferences) {
        super(preferences, new KLReadOnlyDataTypeControl<>(String.class));
    }

    private ReadOnlyStringFieldArea(KlPreferencesFactory preferencesFactory, KlAreaForString.Factory areaFactory) {
        super(preferencesFactory, areaFactory, new KLReadOnlyDataTypeControl<>(String.class));
    }

    @Override
    public Optional<FeatureKey> getAreaFeatureKey() {
        return KlAreaForString.super.getAreaFeatureKey();
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
    protected void featureChanged(Feature<String> oldFeature, Feature<String> newFeature) {
        if (newFeature != null) {
            setDisplayValues(newFeature);
        }
    }

    /**
     * Sets the display values of the read-only String field area and title (based on meaning).
     * @param newValue the new Feature<String> to display
     */
    private void setDisplayValues(Feature<String> newValue) {
        getFxPeer().setTitle(calculatorForContext().getDescriptionTextOrNid(newValue.definition(calculatorForContext()).purposeNid()));
        getFxPeer().setValue(newValue.value());
    }

    public static Factory factory() {
        return new Factory();
    }

    public static class Factory implements KlAreaForString.Factory {

        public Factory() {}

        @Override
        public String productName() {
            return ReadOnlyStringFieldArea.class.getSimpleName();
        }

        @Override
        public ReadOnlyStringFieldArea restore(KometPreferences preferences) {
            return new ReadOnlyStringFieldArea(preferences);
        }

        @Override
        public ReadOnlyStringFieldArea create(KlPreferencesFactory preferencesFactory, AreaGridSettings areaGridSettings) {
            ReadOnlyStringFieldArea area = new ReadOnlyStringFieldArea(preferencesFactory, this);
            area.setAreaLayout(areaGridSettings);
            return area;
        }
    }
}
