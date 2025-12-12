package dev.ikm.komet.kview.klauthoring.readonly.componentfield;

import dev.ikm.komet.framework.Identicon;
import dev.ikm.komet.framework.observable.Feature;
import dev.ikm.komet.framework.observable.FeatureKey;
import dev.ikm.komet.framework.observable.ObservableChronology;
import dev.ikm.komet.framework.observable.ObservableEntityHandle;
import dev.ikm.komet.kview.controls.ComponentItem;
import dev.ikm.komet.kview.controls.KLReadOnlyComponentControl;
import dev.ikm.komet.layout.area.AreaGridSettings;
import dev.ikm.komet.layout.area.KlAreaForComponentChronology;
import dev.ikm.komet.layout.preferences.KlPreferencesFactory;
import dev.ikm.komet.layout_engine.blueprint.FeatureAreaBlueprint;
import dev.ikm.komet.preferences.KometPreferences;
import dev.ikm.tinkar.common.id.PublicId;

import java.util.*;

/**
 * A read-only area for displaying Component fields within the Komet framework.
 * This area utilizes a KLReadOnlyComponentControl to present the boolean value
 * in a non-editable format.
 */
public final class ReadOnlyObservableChronologyFieldArea extends FeatureAreaBlueprint<ObservableChronology, Feature<ObservableChronology>, KLReadOnlyComponentControl>
        implements KlAreaForComponentChronology<KLReadOnlyComponentControl> {
    private ReadOnlyObservableChronologyFieldArea(KometPreferences preferences) {
        super(preferences, new KLReadOnlyComponentControl());
    }

    private ReadOnlyObservableChronologyFieldArea(KlPreferencesFactory preferencesFactory, KlAreaForComponentChronology.Factory areaFactory) {
        super(preferencesFactory, areaFactory, new KLReadOnlyComponentControl());
    }

    @Override
    public Optional<FeatureKey> getAreaFeatureKey() {
        return KlAreaForComponentChronology.super.getAreaFeatureKey();
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
    protected void featureChanged(Feature<ObservableChronology> oldFeature, Feature<ObservableChronology> newFeature) {
        if (newFeature != null) {
            setDisplayValues(newFeature);
        }
    }

    /**
     * Sets the display values of the read-only Component field area and title (based on meaning).
     * @param newValue the new Feature<ObservableChronology> to display
     */
    private void setDisplayValues(Feature<ObservableChronology> newValue) {
        if (newValue != null && newValue.value() != null) {
            String purpose = calculatorForContext().getDescriptionTextOrNid(newValue.definition(calculatorForContext()).purposeNid());
            getFxPeer().setTitle(purpose);
            int nid = newValue.value().nid();
            ObservableEntityHandle.get(nid).entity().ifPresent((observableEntity -> {
                PublicId pid = observableEntity.publicId();
                ComponentItem componentItem = new ComponentItem(purpose, Identicon.generateIdenticonImage(pid), nid);
                getFxPeer().setValue(componentItem);
            }));
        }
    }

    public static Factory factory() {
        return new Factory();
    }

    public static class Factory implements KlAreaForComponentChronology.Factory {

        public Factory() {}

        @Override
        public String productName() {
            return ReadOnlyObservableChronologyFieldArea.class.getSimpleName();
        }

        @Override
        public ReadOnlyObservableChronologyFieldArea restore(KometPreferences preferences) {
            return new ReadOnlyObservableChronologyFieldArea(preferences);
        }

        @Override
        public ReadOnlyObservableChronologyFieldArea create(KlPreferencesFactory preferencesFactory, AreaGridSettings areaGridSettings) {
            ReadOnlyObservableChronologyFieldArea area = new ReadOnlyObservableChronologyFieldArea(preferencesFactory, this);
            area.setAreaLayout(areaGridSettings);
            return area;
        }
    }
}
