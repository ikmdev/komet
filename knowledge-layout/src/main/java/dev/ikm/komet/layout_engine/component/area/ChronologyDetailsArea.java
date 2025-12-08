
package dev.ikm.komet.layout_engine.component.area;

import dev.ikm.komet.framework.observable.Feature;
import dev.ikm.komet.framework.observable.FeatureKey;
import dev.ikm.komet.framework.observable.ObservableChronology;
import dev.ikm.komet.framework.observable.ObservableVersion;
import dev.ikm.komet.layout.KlArea;
import dev.ikm.komet.layout.KlParent;
import dev.ikm.komet.layout.KlPeerToRegion;
import dev.ikm.komet.layout.LayoutComputer;
import dev.ikm.komet.layout.area.AreaGridSettings;
import dev.ikm.komet.layout.area.KlAreaForListOfVersions;
import dev.ikm.komet.layout.component.KlGenericChronologyArea;
import dev.ikm.komet.layout.preferences.KlPreferencesFactory;
import dev.ikm.komet.layout_engine.blueprint.ChronologyAreaBlueprint;
import dev.ikm.komet.layout_engine.layout.RowIncrementLayoutComputer;
import dev.ikm.komet.preferences.KometPreferences;
import javafx.scene.control.Label;
import javafx.scene.layout.BorderPane;
import javafx.util.Subscription;
import org.eclipse.collections.api.list.ImmutableList;

public class ChronologyDetailsArea extends ChronologyAreaBlueprint<ObservableChronology>
        implements KlGenericChronologyArea<BorderPane, ObservableChronology>, KlParent<BorderPane> {

    private ChronologyDetailsArea(KometPreferences preferences) {
        super(preferences);
        this.fxObject().setId("ChronologyDetailsArea");
        this.gridPaneForChildren().setId("ChronologyDetailsArea GridPane for Children");
    }

    private ChronologyDetailsArea(KlPreferencesFactory preferencesFactory, KlArea.Factory areaFactory) {
        super(preferencesFactory, areaFactory);
        this.fxObject().setId("ChronologyDetailsArea");
        this.gridPaneForChildren().setId("ChronologyDetailsArea GridPane for Children");
    }

    @Override
    protected void subFeatureAreaBlueprintRestoreFromPreferencesOrDefault() {
        // Nothing to restore that is not handled by FeatureAreaBlueprint.
    }

    @Override
    protected void featureChanged(Feature<ObservableChronology> oldFeature, Feature<ObservableChronology> newFeature) {
        // Feature changes are handled via componentChanged
    }

    @Override
    protected void componentChanged(ObservableChronology oldValue,
                                    ObservableChronology newValue) {
        unbindKnowledgeLayoutDescendents();
        getSelectedItemsSubscriptionReference().get().unsubscribe();
        getSelectedItemsSubscriptionReference().set(Subscription.EMPTY);
        if (newValue != null) {
            ImmutableList<Feature<?>> features = newValue.getFeatures();

            // Can add or remove features here for layout...
            features = features.select(feature -> FeatureKey.Entity.PublicId().match(feature.featureKey())
                    || FeatureKey.Entity.VersionSet().match(feature.featureKey()));

            gridPaneForChildren().getChildren().clear();

            RowIncrementLayoutComputer rowIncrementLayoutComputer =
                    RowIncrementLayoutComputer.create(this.getMasterLayout());
            // Can add additional content here...
            // Add a description for a component as an example and then feed it into the layout computer.
            KlPeerToRegion.LOG.info("Laying out: " + this.getClass().getSimpleName());
            ImmutableList<LayoutComputer.LayoutElement> layout = rowIncrementLayoutComputer.layout(features,
                    this.getLayoutKeyForArea().makeAreaKeyProvider());

            layout.forEach(layoutElement -> {

                KlArea<?> klArea = layoutElement.areaGridSettings().makeAndAddToParent(this);
                klArea.setId(layoutElement);

                switch (klArea) {
                    case KlAreaForListOfVersions<?> listOfVersionArea ->
                            getSelectedItemsSubscriptionReference().get().and(listOfVersionArea.selectedItems().subscribe(() -> {
                                getSelectedItems().setAll(listOfVersionArea.selectedItems());
                                selectedItemsChanged();
                            }));
                    default -> {
                        // let others just pass through.
                    }
                }
            });
        } else {
            gridPaneForChildren().add(new Label("No component selected"), 0, 0);
        }
        bindKnowledgeLayoutDescendents();
    }

    private void selectedItemsChanged() {
        KlPeerToRegion.LOG.info("Selected items: " + getSelectedItems().size() + " " + getSelectedItems());
    }

    @Override
    protected void subChronologyAreaRevert() {
    }

    @Override
    protected void subChronologyAreaSave() {
    }

    public static Factory factory() {
        return new Factory();
    }

    public static ChronologyDetailsArea restore(KometPreferences preferences) {
        return factory().restore(preferences);
    }

    public static ChronologyDetailsArea create(KlPreferencesFactory preferencesFactory, AreaGridSettings areaGridSettings) {
        return factory().create(preferencesFactory, areaGridSettings);
    }

    public static class Factory implements KlGenericChronologyArea.Factory<BorderPane, ObservableChronology, ChronologyDetailsArea> {
        @Override
        public ChronologyDetailsArea create(KlPreferencesFactory preferencesFactory, AreaGridSettings areaGridSettings) {
            ChronologyDetailsArea chronologyDetailsArea = new ChronologyDetailsArea(preferencesFactory, this);
            chronologyDetailsArea.setAreaLayout(areaGridSettings);
            return chronologyDetailsArea;
        }

        @Override
        public ChronologyDetailsArea restore(KometPreferences preferences) {
            ChronologyDetailsArea chronologyDetailsArea = new ChronologyDetailsArea(preferences);
            return chronologyDetailsArea;
        }
    }
}