package dev.ikm.komet.layout_engine.blueprint;

import dev.ikm.komet.framework.observable.Feature;
import dev.ikm.komet.framework.observable.FeatureKey;
import dev.ikm.komet.framework.observable.FeatureList;
import dev.ikm.komet.framework.observable.ObservableEntity;
import dev.ikm.komet.layout.KlArea;
import dev.ikm.komet.layout.KlViewLayoutLifecycle;
import dev.ikm.komet.layout.area.KlAreaForFeature;
import dev.ikm.komet.layout.area.KlAreaForListOfFeatures;
import dev.ikm.komet.layout.area.KlFeaturePropertyForArea;
import dev.ikm.komet.layout.preferences.KlPreferencesFactory;
import dev.ikm.komet.preferences.KometPreferences;
import dev.ikm.tinkar.common.binary.Encodable;
import javafx.beans.Observable;
import javafx.beans.property.ObjectProperty;
import javafx.beans.property.Property;
import javafx.beans.property.ReadOnlyProperty;
import javafx.beans.property.SimpleObjectProperty;
import javafx.collections.ObservableList;
import javafx.scene.control.ListView;
import javafx.scene.control.SelectionMode;
import javafx.util.Subscription;
import org.eclipse.collections.api.factory.primitive.IntLists;
import org.eclipse.collections.api.list.primitive.ImmutableIntList;
import org.eclipse.collections.api.list.primitive.MutableIntList;

import java.util.Optional;
import java.util.concurrent.atomic.AtomicReference;

public non-sealed abstract class FeatureListAreaBlueprint<F extends Feature<?>, FL extends FeatureList<F>>
        extends AreaBlueprint<ListView<F>> implements KlFeaturePropertyForArea<FL>, KlViewLayoutLifecycle {

    final AtomicReference<Subscription> transientSubscriptions = new AtomicReference<>(Subscription.EMPTY);
    final ObjectProperty<ReadOnlyProperty<FL>> featureListPropertyWrapper = new SimpleObjectProperty<>();
    final Subscription featurePropertySubscription = featureListPropertyWrapper.subscribe(this::featureListPropertyChanged);
    {
        fxObject().getSelectionModel().setSelectionMode(SelectionMode.MULTIPLE);
        fxObject().setCellFactory(new FeatureListCellFactory((KlArea) this));
        fxObject().getSelectionModel().selectedItemProperty().addListener(this::selectionChanged);
    }

    protected FeatureListAreaBlueprint(KometPreferences preferences) {
        super(preferences, new ListView());
    }

    protected FeatureListAreaBlueprint(KlPreferencesFactory preferencesFactory, KlArea.Factory areaFactory) {
        super(preferencesFactory, areaFactory, new ListView());
    }

    private void selectionChanged(Observable observable) {
        preferences().putObject(KlAreaForListOfFeatures.PreferenceKeys.SELECTED_FEATURES_KEY,
                new KlAreaForListOfFeatures.Selection((ObservableList<Feature<?>>) fxObject().getSelectionModel().getSelectedItems()));
    }


    public final Property<ReadOnlyProperty<FL>> featurePropertyWrapper() {
        return this.featureListPropertyWrapper;
    }


    protected abstract void updateVersionsAndSelection();

    // Utility method to select indices in any ListView
    private void selectIndices(ImmutableIntList indices) {
        if (indices == null || indices.size() == 0) {
            fxObject().getSelectionModel().clearSelection();
        } else if (indices.size() == 1) {
            fxObject().getSelectionModel().clearAndSelect(indices.get(0));
        } else {
            fxObject().getSelectionModel().clearSelection();
            MutableIntList mutableIndices = indices.toList();
            int first = mutableIndices.removeAtIndex(0);
            fxObject().getSelectionModel().selectIndices(first,
                    mutableIndices.toArray());
        }
    }

    @Override
    protected final void subAreaRestoreFromPreferencesOrDefault() {
        Optional<Object> selectedFeatures = preferences().getObject(KlAreaForListOfFeatures.PreferenceKeys.SELECTED_FEATURES_KEY);
        preferences().getObject(KlAreaForFeature.PreferenceKeys.AREA_FEATURE_KEY).ifPresent(object -> {
            if (object instanceof FeatureKey featureKey) {
                Feature<?> feature = ObservableEntity.get(featureKey.nid()).getFeature(featureKey);
                setFeatureProperty((ReadOnlyProperty<FL>) feature.featureProperty());
            }
        });
        selectedFeatures.ifPresent(object -> {
            if (object != null && object instanceof KlAreaForListOfFeatures.Selection selection) {
                fxObject().getSelectionModel().clearSelection();
                MutableIntList selectedIndices = IntLists.mutable.empty();
                for (int i = 0; i < fxObject().getItems().size(); i++) {
                    Feature<?> feature = fxObject().getItems().get(i);
                    if (selection.getFeatureKeys().stream().anyMatch(feature.featureKey()::equals)) {
                        selectedIndices.add(i);
                    }
                }
                selectIndices(selectedIndices.toImmutable());
            }
        });
    }

    public final Optional<FL> getFeature() {
        if (featureListPropertyWrapper.get() == null) {
            return Optional.empty();
        }
        return Optional.ofNullable(featureListPropertyWrapper.get().getValue());
    }

    private void featureChanged(FL oldFeatureList, FL newFeatureList) {
        fxObject().getSelectionModel().clearSelection();
        fxObject().setItems(newFeatureList);
        saveToPreferences();
    }

    public void featureListPropertyChanged(ReadOnlyProperty<FL> oldProperty,
                                ReadOnlyProperty<FL> newProperty) {
        transientSubscriptions.getAndSet(Subscription.EMPTY).unsubscribe();
        transientSubscriptions.get().and(newProperty.subscribe(this::featureChanged));
        featureChanged(oldProperty == null ? null : oldProperty.getValue(),
                       newProperty == null ? null : newProperty.getValue());
    }

    /**
     * Retrieves the property associated with this property area.
     *
     * @return the property of type {@code PT} associated with this property area.
     */
    public final ReadOnlyProperty<FL> getFeatureProperty() {
        return featurePropertyWrapper().getValue();
    }

    public final ObservableList<F> selectedItems() {
        return fxObject().getSelectionModel().getSelectedItems();
    }

    @Override
    public void knowledgeLayoutUnbind() {
        featurePropertySubscription.unsubscribe();
    }

    @Override
    public void knowledgeLayoutBind() {
        restoreFromPreferencesOrDefaults();
    }

    public void saveToPreferences() {
        Encodable areaFeatureKey = Encodable.nullEncodable;
        KlAreaForListOfFeatures.Selection selectedFeatureKeys = KlAreaForListOfFeatures.EMPTY_SELECTION;
        if (featureListPropertyWrapper.get() != null && featureListPropertyWrapper.get().getValue() != null) {
            areaFeatureKey = featureListPropertyWrapper.get().getValue().featureKey();
            if (fxObject().getSelectionModel().getSelectedItems() != null) {
                selectedFeatureKeys = new KlAreaForListOfFeatures.Selection((ObservableList<Feature<?>>) fxObject().getSelectionModel().getSelectedItems());
            }
        }
        preferences().putObject(KlAreaForFeature.PreferenceKeys.AREA_FEATURE_KEY, areaFeatureKey);
        preferences().putObject(KlAreaForListOfFeatures.PreferenceKeys.SELECTED_FEATURES_KEY, selectedFeatureKeys);
    }
}
