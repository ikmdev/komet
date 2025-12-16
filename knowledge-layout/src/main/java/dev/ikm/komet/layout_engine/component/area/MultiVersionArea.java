package dev.ikm.komet.layout_engine.component.area;

import dev.ikm.komet.framework.observable.*;
import dev.ikm.komet.layout.*;
import dev.ikm.komet.layout.area.*;
import dev.ikm.komet.layout.area.KlAreaForListOfVersions.VersionsAndSelection;
import dev.ikm.komet.layout.preferences.KlPreferencesFactory;
import dev.ikm.komet.layout_engine.blueprint.SupplementalAreaBlueprint;
import dev.ikm.komet.layout_engine.layout.ColumnIncrementLayoutComputer;
import dev.ikm.komet.preferences.KometPreferences;
import dev.ikm.tinkar.entity.EntityVersion;
import javafx.application.Platform;
import javafx.beans.property.ReadOnlyObjectProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.scene.control.Label;
import javafx.scene.layout.BorderPane;
import javafx.util.Subscription;
import org.eclipse.collections.api.factory.Lists;
import org.eclipse.collections.api.list.ImmutableList;
import org.eclipse.collections.api.list.MutableList;

import java.util.concurrent.atomic.AtomicReference;

public final class MultiVersionArea
        extends SupplementalAreaBlueprint
        implements KlMultiVersionArea<ObservableVersion, BorderPane> {


    final AtomicReference<Subscription> transientSubscriptions = new AtomicReference<>(Subscription.EMPTY);
    final ObservableList<KlAreaForVersion<ObservableVersion, BorderPane>> versionAreas = FXCollections.observableArrayList();
    final MutableList<KlArea> layoutAreas = Lists.mutable.empty();

    private MultiVersionArea(KometPreferences preferences) {
        super(preferences);
    }

    private MultiVersionArea(KlPreferencesFactory preferencesFactory, KlArea.Factory areaFactory) {
        super(preferencesFactory, areaFactory);
    }

    @Override
    protected void subAreaRestoreFromPreferencesOrDefault() {
        // TODO: Decide if additional items need saving and restoring beyond supplemental area blueprint
        //
    }

    @Override
    public void contextChanged() {
        KlPeerToRegion.LOG.info("MVA Context changed");
    }

    private void selectionChanged(VersionsAndSelection versionsAndSelection) {
        KlPeerToRegion.LOG.info("Selected items: " + versionsAndSelection.selectedVersions().size() + " " + versionsAndSelection.selectedVersions());
        if (lifecycleState.get().ordinal() >= LifecycleState.BOUND.ordinal()) {
            // Defensive copy, list was changing in the background.
            ImmutableList<Feature> selectedVersions = Lists.immutable.ofAll(versionsAndSelection.selectedVersions());
            Platform.runLater(() -> {
                // The order of the layout may change, need to feed them all to the layout manager.
                layoutAreas.forEach(KlArea::unbindSelfAndKnowledgeLayoutDescendents);
                gridPaneForChildren.getChildren().clear();
                layoutAreas.clear();
                versionAreas.clear();

                KlPeerToRegion.LOG.info("Laying out: " + this.getClass().getSimpleName());
                ColumnIncrementLayoutComputer columnIncrementLayoutComputer = ColumnIncrementLayoutComputer.create(this.getMasterLayout());
                ImmutableList<LayoutComputer.LayoutElement> columnLayout =
                        columnIncrementLayoutComputer.layout(selectedVersions.toImmutable(), this.getLayoutKeyForArea().makeAreaKeyProvider());
                if (columnLayout.isEmpty()) {
                    gridPaneForChildren.add(new Label("No versions selected."), 0, 0);
                }
                columnLayout.forEach(layoutElement -> {
                    KlArea elementArea = layoutElement.areaGridSettings().makeAndAddToParent(this);
                    layoutAreas.add(elementArea);
                    if (elementArea instanceof KlAreaForVersion areaForVersion) {
                        versionAreas.add(areaForVersion);
                    }
                    elementArea.setId(layoutElement);
                });
                layoutAreas.forEach(KlArea::bindSelfAndKnowledgeLayoutDescendents);
                fxObject().setVisible(!versionsAndSelection.selectedVersions().isEmpty());
            });
        }
    }

    @Override
    public void setVersionAndSelectionProperty(ReadOnlyObjectProperty<VersionsAndSelection> versionsAndSelectionProperty) {
        transientSubscriptions.getAndSet(Subscription.EMPTY).unsubscribe();
        transientSubscriptions.get().and(versionsAndSelectionProperty.subscribe(this::selectionChanged));
    }

    @Override
    public ObservableList<KlAreaForVersion<ObservableVersion, BorderPane>> klVersionAreas() {
        return versionAreas;
    }

    @Override
    protected void subAreaRevert() {

    }

    @Override
    protected void subAreaSave() {

    }


    @Override
    public void knowledgeLayoutUnbind() {
        transientSubscriptions.get().unsubscribe();
    }

    @Override
    public void knowledgeLayoutBind() {
        // Find the first KlListOfVersionArea sibling or ancestor to bind to.
        findKlSiblingOrAncestor(klArea -> klArea instanceof KlAreaForListOfVersions<?>)
                .ifPresent(klArea -> {
                    switch (klArea) {
                        case KlAreaForListOfVersions versionArea -> {
                            this.setVersionAndSelectionProperty(versionArea.versionsAndSelectionProperty());
                        }
                        default -> KlPeerToRegion.LOG.warn("KlListOfVersionArea expected, but got: " + klArea);
                    }
                });
        Platform.runLater(() -> this.lifecycleState.set(LifecycleState.BOUND));
    }


    public static Factory factory() {
        return new Factory();
    }

    public static MultiVersionArea create(KlPreferencesFactory preferencesFactory, AreaGridSettings areaGridSettings) {
        return new Factory().create(preferencesFactory, areaGridSettings);
    }

    public static MultiVersionArea create(KlPreferencesFactory preferencesFactory) {
        return new Factory().create(preferencesFactory);
    }


    public static class Factory implements KlMultiVersionArea.Factory<BorderPane, ObservableVersion, MultiVersionArea> {
        public Factory() {}

        @Override
        public MultiVersionArea restore(KometPreferences preferences) {
            MultiVersionArea multiVersionArea = new MultiVersionArea(preferences);
            return multiVersionArea;
        }

        @Override
        public MultiVersionArea create(KlPreferencesFactory preferencesFactory, AreaGridSettings areaGridSettings) {
            MultiVersionArea multiVersionArea = new MultiVersionArea(preferencesFactory, this);
            multiVersionArea.setAreaLayout(areaGridSettings);
            return multiVersionArea;
        }
    }
}
