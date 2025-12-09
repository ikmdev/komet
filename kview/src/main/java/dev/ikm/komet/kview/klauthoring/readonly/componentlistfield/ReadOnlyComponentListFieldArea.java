package dev.ikm.komet.kview.klauthoring.readonly.componentlistfield;

import dev.ikm.komet.framework.Identicon;
import dev.ikm.komet.framework.observable.Feature;
import dev.ikm.komet.framework.observable.FeatureKey;
import dev.ikm.komet.kview.controls.ComponentItem;
import dev.ikm.komet.kview.controls.KLReadOnlyComponentListControl;
import dev.ikm.komet.layout.area.AreaGridSettings;
import dev.ikm.komet.layout.area.KlAreaForIntIdList;
import dev.ikm.komet.layout.preferences.KlPreferencesFactory;
import dev.ikm.komet.layout_engine.blueprint.FeatureAreaBlueprint;
import dev.ikm.komet.preferences.KometPreferences;
import dev.ikm.tinkar.common.id.IntIdList;
import dev.ikm.tinkar.entity.EntityHandle;
import javafx.scene.image.Image;

import java.util.*;

/**
 * A read-only area for displaying IntIdList fields within the Komet framework.
 * This area utilizes a KLReadOnlyDataTypeControl to present the IntIdList value
 * in a non-editable format.
 */
public final class ReadOnlyComponentListFieldArea extends FeatureAreaBlueprint<IntIdList, Feature<IntIdList>, KLReadOnlyComponentListControl>
        implements KlAreaForIntIdList<KLReadOnlyComponentListControl> {
    private ReadOnlyComponentListFieldArea(KometPreferences preferences) {
        super(preferences, new KLReadOnlyComponentListControl());
    }

    private ReadOnlyComponentListFieldArea(KlPreferencesFactory preferencesFactory, ReadOnlyComponentListFieldArea.Factory areaFactory) {
        super(preferencesFactory, areaFactory, new KLReadOnlyComponentListControl());
    }

    @Override
    public Optional<FeatureKey> getAreaFeatureKey() {
        return KlAreaForIntIdList.super.getAreaFeatureKey();
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
    protected void featureChanged(Feature<IntIdList> oldFeature, Feature<IntIdList> newFeature) {
        if (newFeature != null) {
            setDisplayValues(newFeature);
        }
    }

    /**
     * Sets the display values of the read-only IntIdList field area and title (based on meaning).
     * @param newValue
     */
    private void setDisplayValues(Feature<IntIdList> newValue) {
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

    public static class Factory implements KlAreaForIntIdList.Factory {

        public Factory() {}

        @Override
        public String productName() {
            return ReadOnlyComponentListFieldArea.class.getSimpleName();
        }

        @Override
        public ReadOnlyComponentListFieldArea restore(KometPreferences preferences) {
            return new ReadOnlyComponentListFieldArea(preferences);
        }

        @Override
        public ReadOnlyComponentListFieldArea create(KlPreferencesFactory preferencesFactory, AreaGridSettings areaGridSettings) {
            ReadOnlyComponentListFieldArea area = new ReadOnlyComponentListFieldArea(preferencesFactory, this);
            area.setAreaLayout(areaGridSettings);
            return area;
        }
    }
}
