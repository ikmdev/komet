package dev.ikm.komet.layout_engine.blueprint;

import dev.ikm.komet.framework.observable.*;
import dev.ikm.komet.layout.KlArea;
import dev.ikm.komet.layout.area.KlAreaForEntity;
import dev.ikm.komet.layout.component.KlChronologyArea;
import dev.ikm.komet.layout.component.KlGenericChronologyArea;
import dev.ikm.komet.layout.preferences.KlPreferencesFactory;
import dev.ikm.komet.preferences.KometPreferences;
import dev.ikm.tinkar.entity.StampRecord;
import dev.ikm.tinkar.terms.ConceptFacade;
import dev.ikm.tinkar.terms.EntityProxy;
import dev.ikm.tinkar.terms.TinkarTerm;
import javafx.beans.property.ObjectProperty;
import javafx.beans.property.SimpleObjectProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.scene.control.MenuButton;
import javafx.scene.control.MenuItem;
import javafx.scene.control.ToolBar;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.GridPane;
import javafx.util.Subscription;

import java.util.UUID;
import java.util.concurrent.atomic.AtomicReference;

public abstract class ChronologyAreaBlueprint<OC extends ObservableChronology>
        extends FeatureAreaBlueprint<OC, Feature<OC>, BorderPane>
        implements KlChronologyArea<BorderPane, OC> {

    final AtomicReference<Subscription> selectedItemsSubscriptionReference = new AtomicReference<>(Subscription.EMPTY);
    final SimpleObjectProperty<OC> componentProperty = new SimpleObjectProperty<>();
    {
        addPreferenceSubscription(componentProperty.subscribe(this::componentChanged));
    }

    final ObservableList<ObservableVersion> selectedItems = FXCollections.observableArrayList();

    final MenuButton menuButton = new MenuButton();
    final ToolBar toolBar = new ToolBar(menuButton);
    private final GridPane gridPaneForChildren = new GridPane();
    {
        gridPaneForChildren.setAccessibleRoleDescription("Simple Chronology Area Child GridPane");
        toolBar.setMinHeight(25);
        fxObject().setTop(toolBar);
        fxObject().setCenter(gridPaneForChildren);
        MenuItem procedure = new MenuItem("Procedure");
        procedure.setOnAction(event -> componentProperty.set((OC) ObservableEntityHandle.get(TinkarTerm.PROCEDURE.nid()).expectConcept()));

        MenuItem tofMenuItem = new MenuItem("Tetralogy of Fallot");
        ConceptFacade tofFacade = EntityProxy.Concept.make("Tetralogy of Fallot", UUID.fromString("4ebf1040-5f4c-5f56-96a7-8ee8de0a5bb2"));
        tofMenuItem.setOnAction(event -> componentProperty.set((OC) ObservableEntityHandle.get((tofFacade.nid())).expectConcept()));

        MenuItem descriptionPattern = new MenuItem("Description Pattern");
        descriptionPattern.setOnAction(event -> componentProperty.set((OC) ObservableEntityHandle.get((TinkarTerm.DESCRIPTION_PATTERN.nid())).expectConcept()));

        MenuItem description = new MenuItem("English Description");
        description.setOnAction(event -> componentProperty.set((OC) ObservableEntityHandle.get(TinkarTerm.ENGLISH_LANGUAGE.nid()).expectConcept()));

        MenuItem stamp = new MenuItem("Non-existent Stamp");
        stamp.setOnAction(event -> componentProperty.set((OC) ObservableEntityHandle.get(StampRecord.nonExistentStamp().nid()).expectStamp()));

        menuButton.getItems().addAll(procedure, tofMenuItem, descriptionPattern, description, stamp);
    }

    public ChronologyAreaBlueprint(KometPreferences preferences) {
        super(preferences, new BorderPane());
    }

    public ChronologyAreaBlueprint(KlPreferencesFactory preferencesFactory,
                                   KlArea.Factory areaFactory) {
        super(preferencesFactory, areaFactory, new BorderPane());
    }

    public final GridPane gridPaneForChildren() {
        return gridPaneForChildren;
    }

    @Override
    public final ObjectProperty<OC> chronologyProperty() {
        return componentProperty;
    }

    @Override
    public final ObservableList<ObservableVersion> selectedVersions() {
        return selectedItems;
    }

    abstract protected void componentChanged(ObservableChronology oldValue,
                                             ObservableChronology newValue);

    @Override
    protected final void subAreaRevert() {
        preferences().getEntity(KlChronologyArea.PreferenceKeys.CURRENT_ENTITY).ifPresentOrElse(
                entityProxy ->
                        componentProperty.set((OC) ObservableEntityHandle.get(entityProxy.nid()).expectEntity()),
                () -> componentProperty.set(null));
        subChronologyAreaRevert();
    }

    protected abstract void subChronologyAreaRevert();

    @Override
    protected final void subAreaSave() {
        if (componentProperty.get() != null) {
            EntityProxy entityFacade = EntityProxy.make(componentProperty.get().nid());
            preferences().putEntity(KlChronologyArea.PreferenceKeys.CURRENT_ENTITY, entityFacade);
        } else {
            preferences().remove(KlChronologyArea.PreferenceKeys.CURRENT_ENTITY);
        }
        preferencesChanged();
        subChronologyAreaSave();
    }

    protected abstract void subChronologyAreaSave();

    public interface Factory<OC extends ObservableChronology, KL extends KlGenericChronologyArea<BorderPane, OC>>
            extends FeatureAreaBlueprint.Factory<OC, Feature<OC>, BorderPane, KL> {

    }

}
/*
    public interface Factory<DT, F extends Feature<DT>, FX extends Region, KL extends KlAreaForFeature<DT, F, FX>>
            extends KlAreaForFeature.Factory<DT, F, FX, KL> {
    }

 */
