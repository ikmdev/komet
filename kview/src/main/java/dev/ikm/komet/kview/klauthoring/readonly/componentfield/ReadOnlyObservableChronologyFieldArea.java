package dev.ikm.komet.kview.klauthoring.readonly.componentfield;

import dev.ikm.komet.framework.Identicon;
import dev.ikm.komet.framework.observable.Feature;
import dev.ikm.komet.framework.observable.FeatureKey;
import dev.ikm.komet.framework.observable.FeatureWrapper;
import dev.ikm.komet.framework.observable.ObservableEntityHandle;
import dev.ikm.komet.kview.controls.ComponentItem;
import dev.ikm.komet.kview.controls.KLReadOnlyComponentControl;
import dev.ikm.komet.layout.area.AreaGridSettings;
import dev.ikm.komet.layout.area.KlAreaForEntityFacade;
import dev.ikm.komet.layout.preferences.KlPreferencesFactory;
import dev.ikm.komet.layout_engine.blueprint.FeatureAreaBlueprint;
import dev.ikm.komet.preferences.KometPreferences;
import dev.ikm.tinkar.common.id.PublicId;
import dev.ikm.tinkar.terms.EntityFacade;
import javafx.beans.property.ReadOnlyObjectProperty;

import java.util.*;

/**
 * A read-only area for displaying Component fields within the Komet framework.
 * This area utilizes a KLReadOnlyComponentControl to present the boolean value
 * in a non-editable format.  KlAreaForEntityFacade
 */
public final class ReadOnlyObservableChronologyFieldArea extends FeatureAreaBlueprint<EntityFacade, Feature<EntityFacade>, KLReadOnlyComponentControl>
        implements KlAreaForEntityFacade<KLReadOnlyComponentControl> {
    private ReadOnlyObservableChronologyFieldArea(KometPreferences preferences) {
        super(preferences, new KLReadOnlyComponentControl());
    }

    private ReadOnlyObservableChronologyFieldArea(KlPreferencesFactory preferencesFactory, KlAreaForEntityFacade.Factory areaFactory) {
        super(preferencesFactory, areaFactory, new KLReadOnlyComponentControl());
    }

    @Override
    public Optional<FeatureKey> getAreaFeatureKey() {
        return KlAreaForEntityFacade.super.getAreaFeatureKey();
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
    protected void featureChanged(Feature<EntityFacade> oldFeature, Feature<EntityFacade> newFeature) {
        if (newFeature != null) {
            setDisplayValues(newFeature);
        }
    }

    /**
     * Sets the display values of the read-only Component field area and title (based on meaning).
     * @param newValue the new Feature<ObservableChronology> to display
     */
    private void setDisplayValues(Feature<EntityFacade> newValue) {
        if (newValue != null && newValue.value() != null) {
            String purpose = calculatorForContext().getDescriptionTextOrNid(newValue.definition(calculatorForContext()).purposeNid());
            getFxPeer().setTitle(purpose);

            int nid1 = ((EntityFacade)((ReadOnlyObjectProperty<?>)((FeatureWrapper<?>) newValue)
                    .valueProperty()
                    .getValue())
                    .getValue()).nid();
//            int nid2 = ((EntityFacade)((ReadOnlyProperty<Feature<EntityFacade>>)((FeatureWrapper<?>) newValue).valueProperty().getValue()).getValue()).nid();
//            int nid3 = ((ReadOnlyProperty<Feature<EntityFacade>>)newValue.featureProperty()).getValue().value().nid();
//            int nid4 = ((ReadOnlyObjectProperty<EntityFacade>)newValue.featureProperty().getValue().value()).get().nid();
//            int nid5 = newValue.value().nid();
            int nid = nid1;
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

    public static class Factory implements KlAreaForEntityFacade.Factory {

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
