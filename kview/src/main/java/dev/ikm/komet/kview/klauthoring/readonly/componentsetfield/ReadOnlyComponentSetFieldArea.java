package dev.ikm.komet.kview.klauthoring.readonly.componentsetfield;

import dev.ikm.komet.framework.Identicon;
import dev.ikm.komet.framework.observable.Feature;
import dev.ikm.komet.framework.observable.FeatureKey;
import dev.ikm.komet.kview.controls.ComponentItem;
import dev.ikm.komet.kview.controls.KLReadOnlyComponentSetControl;
import dev.ikm.komet.layout.area.AreaGridSettings;
import dev.ikm.komet.layout.area.KlAreaForIntIdSet;
import dev.ikm.komet.layout.preferences.KlPreferencesFactory;
import dev.ikm.komet.layout_engine.blueprint.FeatureAreaBlueprint;
import dev.ikm.komet.preferences.KometPreferences;
import dev.ikm.tinkar.common.id.IntIdSet;
import dev.ikm.tinkar.entity.EntityHandle;
import javafx.scene.image.Image;

import java.util.*;

/**
 * A read-only area for displaying IntIdSet fields within the Komet framework.
 * This area utilizes a KLReadOnlyDataTypeControl to present the IntIdSet value
 * in a non-editable format.
 */
public final class ReadOnlyComponentSetFieldArea extends FeatureAreaBlueprint<IntIdSet, Feature<IntIdSet>, KLReadOnlyComponentSetControl>
        implements KlAreaForIntIdSet<KLReadOnlyComponentSetControl> {
    private ReadOnlyComponentSetFieldArea(KometPreferences preferences) {
        super(preferences, new KLReadOnlyComponentSetControl());
    }

    private ReadOnlyComponentSetFieldArea(KlPreferencesFactory preferencesFactory, ReadOnlyComponentSetFieldArea.Factory areaFactory) {
        super(preferencesFactory, areaFactory, new KLReadOnlyComponentSetControl());
    }

    @Override
    public Optional<FeatureKey> getAreaFeatureKey() {
        return KlAreaForIntIdSet.super.getAreaFeatureKey();
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
    protected void featureChanged(Feature<IntIdSet> oldFeature, Feature<IntIdSet> newFeature) {
        if (newFeature != null) {
            setDisplayValues(newFeature);
        }
    }

    /**
     * Sets the display values of the read-only IntIdSet field area and title (based on meaning).
     * @param newValue the new Feature<IntIdSet> to display
     */
    private void setDisplayValues(Feature<IntIdSet> newValue) {
        getFxPeer().setTitle(calculatorForContext().getDescriptionTextOrNid(newValue.definition(calculatorForContext()).purposeNid()));

        getFxPeer().getItems().clear();
        newValue.value().forEach(nid -> {
            EntityHandle.get(nid).ifPresent(entity -> {
                Image icon = Identicon.generateIdenticonImage(entity.publicId());

                String description = calculatorForContext().languageCalculator()
                        .getFullyQualifiedDescriptionTextWithFallbackOrNid(entity.nid());

                ComponentItem componentItem = new ComponentItem(description, icon, nid);
                getFxPeer().getItems().add(componentItem);
            });
        });
    }

    public static Factory factory() {
        return new Factory();
    }

    public static class Factory implements KlAreaForIntIdSet.Factory {

        public Factory() {}

        @Override
        public String productName() {
            return ReadOnlyComponentSetFieldArea.class.getSimpleName();
        }

        @Override
        public ReadOnlyComponentSetFieldArea restore(KometPreferences preferences) {
            return new ReadOnlyComponentSetFieldArea(preferences);
        }

        @Override
        public ReadOnlyComponentSetFieldArea create(KlPreferencesFactory preferencesFactory, AreaGridSettings areaGridSettings) {
            ReadOnlyComponentSetFieldArea area = new ReadOnlyComponentSetFieldArea(preferencesFactory, this);
            area.setAreaLayout(areaGridSettings);
            return area;
        }
    }
}
