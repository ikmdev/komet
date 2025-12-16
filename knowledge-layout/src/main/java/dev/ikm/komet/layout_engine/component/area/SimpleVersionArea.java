package dev.ikm.komet.layout_engine.component.area;

import dev.ikm.komet.framework.observable.*;
import dev.ikm.komet.layout.KlPeerToRegion;
import dev.ikm.komet.layout.KlPeerable;
import dev.ikm.komet.layout.area.AreaGridSettings;
import dev.ikm.komet.layout.KlArea;
import dev.ikm.komet.layout.area.KlAreaForFeature;
import dev.ikm.komet.layout.area.KlAreaForGenericVersion;
import dev.ikm.komet.layout.preferences.KlPreferencesFactory;
import dev.ikm.komet.layout_engine.blueprint.AreaBlueprint;
import dev.ikm.komet.layout_engine.blueprint.FeatureAreaBlueprint;
import dev.ikm.komet.layout_engine.layout.AreaLayoutPropertySheet;
import dev.ikm.komet.layout_engine.layout.RowIncrementLayoutComputer;
import dev.ikm.komet.preferences.KometPreferences;
import dev.ikm.tinkar.entity.EntityVersion;
import javafx.application.Platform;
import javafx.beans.property.ReadOnlyProperty;
import javafx.scene.Scene;
import javafx.scene.control.ContextMenu;
import javafx.scene.control.MenuItem;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;
import javafx.util.Subscription;
import org.eclipse.collections.api.factory.Lists;
import org.eclipse.collections.api.list.ImmutableList;
import org.eclipse.collections.api.list.MutableList;

import java.util.Optional;
import java.util.concurrent.atomic.AtomicReference;

import static javafx.stage.StageStyle.UTILITY;

public class SimpleVersionArea
        extends FeatureAreaBlueprint<ObservableVersion, ObservableVersion, BorderPane>
        implements KlAreaForGenericVersion<BorderPane> {

    MutableList<KlAreaForFeature> versionFeatureAreas = Lists.mutable.empty();
    AtomicReference<Subscription> transientSubscriptions = new AtomicReference<>(Subscription.EMPTY);

    private final GridPane gridPane = new GridPane();
    {
        fxObject().setCenter(gridPane);
    }

    private SimpleVersionArea(KometPreferences preferences) {
        super(preferences, new BorderPane());
        AreaBlueprint.LOG.info(this.getClass().getSimpleName() + " fx peer: " + fxObject() + " parent: " + fxObject().getParent());
        fxObject().parentProperty().addListener((observable, oldValue, newValue) -> {
            AreaBlueprint.LOG.info(this.getClass().getSimpleName() + " parent changed: " + oldValue + " -> " + newValue);
        });
    }

    private SimpleVersionArea(KlPreferencesFactory preferencesFactory, KlArea.Factory areaFactory) {
        super(preferencesFactory, areaFactory, new BorderPane());
    }

    @Override
    protected void subFeatureAreaBlueprintRestoreFromPreferencesOrDefault() {
        // Nothing to restore that is not handled by FeatureAreaBlueprint.
    }

    @Override
    public GridPane gridPaneForChildren() {
        return gridPane;
    }

    @Override
    protected void featureChanged(ObservableVersion oldFeature, ObservableVersion newFeature) {
        AreaBlueprint.LOG.debug("Feature changed: " + oldFeature + " -> " + newFeature);
        AreaBlueprint.LOG.debug(this.getClass().getSimpleName() + " FxPeer: " + fxObject() + " parent: " + fxObject().getParent());
        gridPane.getChildren().clear();
        versionFeatureAreas.forEach(klAreaForFeature -> klAreaForFeature.knowledgeLayoutUnbind());
        versionFeatureAreas.clear();
        if (newFeature != null) {

            ImmutableList<Feature<?>> versionFeatures = newFeature.value().getFeatures();
            RowIncrementLayoutComputer rowIncrementLayoutComputer = RowIncrementLayoutComputer.create(getMasterLayout());
            KlPeerToRegion.LOG.info("Laying out: " + this.getClass().getSimpleName());
            rowIncrementLayoutComputer.layout(versionFeatures, getLayoutKeyForArea().makeAreaKeyProvider())
                    .forEach(layoutElement -> {
                        KlArea klArea = layoutElement.areaGridSettings().makeAndAddToParent(this);
                        klArea.setId(layoutElement);
                        Optional<Feature> optionalFeature = layoutElement.optionalFeature();
                        //TODO: Bind klArea to versionElement.optionalFeature()
                        if (klArea instanceof KlAreaForFeature featureArea &&
                                optionalFeature.isPresent()) {
                            featureArea.setFeatureProperty(optionalFeature.get().featureProperty());
                            versionFeatureAreas.add(featureArea);
                        }
                    });
        }
    }

    /**
     * Creates and returns a context menu populated with menu items for each {@code SimpleGenericFieldPane}
     * in the {@code simpleGenericFieldPanes} collection. Each menu item is created using the
     * {@code makeVersionLayoutMenu} method, which provides functionality for editing the grid layout
     * associated with the respective field pane.
     *
     * @return a {@code ContextMenu} containing menu items for editing the grid layout of
     *         all {@code SimpleGenericFieldPane} instances in the collection
     */
    ContextMenu makeContextMenu() {
        ContextMenu contextMenu = new ContextMenu();
        for (KlAreaForFeature featureArea : versionFeatureAreas) {
            contextMenu.getItems().add(makeVersionLayoutMenu(featureArea));
        }
        return contextMenu;
    }

    /**
     * Creates and returns a menu item for editing the grid layout of the provided {@code SimpleGenericFieldPane}.
     * The menu item allows the user to open a new window for configuring the grid layout associated with the field pane.
     *
     * @param simpleGenericFieldPane the {@code SimpleGenericFieldPane} for which the grid layout edit menu item is created
     * @return a {@code MenuItem} configured to launch the grid layout editor for the specified {@code SimpleGenericFieldPane}
     */
    private MenuItem makeVersionLayoutMenu(KlAreaForFeature<?,?,?> simpleGenericFieldPane) {
        if (simpleGenericFieldPane.getFeature().isPresent()) {
            Feature feature = simpleGenericFieldPane.getFeature().get();
            MenuItem editGridLayout = new MenuItem("Edit grid layout for: " +
                    calculatorForContext().getPreferredDescriptionStringOrNid(feature.definition(calculatorForContext()).meaningNid()) + "");
            editGridLayout.setOnAction(event -> {
                AreaLayoutPropertySheet areaLayoutPropertySheet = new AreaLayoutPropertySheet(simpleGenericFieldPane);
                Stage stage = new Stage(UTILITY);
                stage.setTitle("Edit grid layout for: " + calculatorForContext().getDescriptionTextOrNid(feature.definition(calculatorForContext()).meaningNid()));
                Scene scene = new Scene(new VBox(areaLayoutPropertySheet.getPropertySheet()));
                stage.setScene(scene);
                stage.show();
            });
            return editGridLayout;
        } else {
            MenuItem editGridLayout = new MenuItem("Edit grid layout for null feature ");
            editGridLayout.setOnAction(event -> {
                AreaLayoutPropertySheet areaLayoutPropertySheet = new AreaLayoutPropertySheet(simpleGenericFieldPane);
                Stage stage = new Stage(UTILITY);
                stage.setTitle("Edit grid layout for empty feature");
                Scene scene = new Scene(new VBox(areaLayoutPropertySheet.getPropertySheet()));
                stage.setScene(scene);
            });
            return editGridLayout;
        }

    }

    @Override
    public ReadOnlyProperty<ObservableVersion> versionProperty() {
        return featurePropertyWrapper().getValue();
    }

    @Override
    protected void subAreaRevert() {

    }

    @Override
    protected void subAreaSave() {

    }

    public static SimpleVersionArea.Factory factory() {
        return new SimpleVersionArea.Factory();
    }

    public static class Factory implements KlAreaForGenericVersion.Factory<BorderPane, SimpleVersionArea> {

        @Override
        public SimpleVersionArea restore(KometPreferences preferences) {
            SimpleVersionArea simpleVersionArea = new SimpleVersionArea(preferences);
            return simpleVersionArea;
        }

        @Override
        public SimpleVersionArea create(KlPreferencesFactory preferencesFactory, AreaGridSettings areaGridSettings) {
            SimpleVersionArea simpleVersionArea = new SimpleVersionArea(preferencesFactory, this);
            simpleVersionArea.setAreaLayout(areaGridSettings);
            return simpleVersionArea;
        }
    }
}
