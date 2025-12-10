package dev.ikm.komet.kview.klauthoring.readonly.imagefield;

import dev.ikm.komet.framework.observable.Feature;
import dev.ikm.komet.framework.observable.FeatureKey;
import dev.ikm.komet.kview.controls.KLReadOnlyImageControl;
import dev.ikm.komet.kview.klfields.KlFieldHelper;
import dev.ikm.komet.layout.area.AreaGridSettings;
import dev.ikm.komet.layout.area.KlAreaForImage;
import dev.ikm.komet.layout.preferences.KlPreferencesFactory;
import dev.ikm.komet.layout_engine.blueprint.FeatureAreaBlueprint;
import dev.ikm.komet.preferences.KometPreferences;

import java.util.*;

/**
 * A read-only area for displaying Image (byte[]) fields within the Komet framework.
 * This area utilizes a KLReadOnlyDataTypeControl to present the byte[] value
 * in a non-editable format.
 */
public final class ReadOnlyImageFieldArea extends FeatureAreaBlueprint<byte[], Feature<byte[]>, KLReadOnlyImageControl>
        implements KlAreaForImage<KLReadOnlyImageControl> {
    private ReadOnlyImageFieldArea(KometPreferences preferences) {
        super(preferences, new KLReadOnlyImageControl());
    }

    private ReadOnlyImageFieldArea(KlPreferencesFactory preferencesFactory, KlAreaForImage.Factory areaFactory) {
        super(preferencesFactory, areaFactory, new KLReadOnlyImageControl());
    }

    @Override
    public Optional<FeatureKey> getAreaFeatureKey() {
        return KlAreaForImage.super.getAreaFeatureKey();
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
    protected void featureChanged(Feature<byte[]> oldFeature, Feature<byte[]> newFeature) {
        if (newFeature != null) {
            setDisplayValues(newFeature);
        }
    }

    /**
     * Sets the display values of the read-only byte[] field area and title (based on meaning).
     * @param newValue the new Feature<byte[]> to display
     */
    private void setDisplayValues(Feature<byte[]> newValue) {
        getFxPeer().setTitle(calculatorForContext().getDescriptionTextOrNid(newValue.definition(calculatorForContext()).purposeNid()));
        byte[] imageBytes = newValue.value();
        getFxPeer().setValue(KlFieldHelper.newImageFromByteArray(imageBytes));
    }

    public static Factory factory() {
        return new Factory();
    }

    public static class Factory implements KlAreaForImage.Factory {

        public Factory() {}

        @Override
        public String productName() {
            return ReadOnlyImageFieldArea.class.getSimpleName();
        }

        @Override
        public ReadOnlyImageFieldArea restore(KometPreferences preferences) {
            return new ReadOnlyImageFieldArea(preferences);
        }

        @Override
        public ReadOnlyImageFieldArea create(KlPreferencesFactory preferencesFactory, AreaGridSettings areaGridSettings) {
            ReadOnlyImageFieldArea area = new ReadOnlyImageFieldArea(preferencesFactory, this);
            area.setAreaLayout(areaGridSettings);
            return area;
        }
    }
}
