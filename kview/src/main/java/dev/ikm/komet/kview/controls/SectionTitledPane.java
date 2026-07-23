package dev.ikm.komet.kview.controls;

import dev.ikm.komet.kview.controls.skin.SectionTitledPaneSkin;
import javafx.beans.property.BooleanProperty;
import javafx.beans.property.IntegerProperty;
import javafx.beans.property.ObjectProperty;
import javafx.beans.property.SimpleBooleanProperty;
import javafx.beans.property.SimpleIntegerProperty;
import javafx.beans.property.SimpleObjectProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.scene.Node;
import javafx.scene.control.ListCell;
import javafx.scene.control.ListView;
import javafx.scene.control.Skin;
import javafx.scene.control.TitledPane;
import javafx.util.Callback;

public class SectionTitledPane<T> extends TitledPane {

    public SectionTitledPane() {
        getStyleClass().add("section-titled-pane");
    }

    @Override
    protected Skin<?> createDefaultSkin() {
        return new SectionTitledPaneSkin<>(this);
    }

    // -- on edit action
    private final ObjectProperty<EventHandler<ActionEvent>> onEditAction = new SimpleObjectProperty<>();
    public EventHandler<ActionEvent> getOnEditAction() { return onEditAction.get(); }
    public ObjectProperty<EventHandler<ActionEvent>> onEditActionProperty() { return onEditAction; }
    public void setOnEditAction(EventHandler<ActionEvent> onEditAction) { this.onEditAction.set(onEditAction); }

    // -- edit enabled
    private final BooleanProperty editEnabled = new SimpleBooleanProperty(true);
    public boolean isEditEnabled() { return editEnabled.get(); }
    public BooleanProperty editEnabledProperty() { return editEnabled; }
    public void setEditEnabled(boolean editEnabled) { this.editEnabled.set(editEnabled); }

    // -- required chip visible
    /**
     * Whether the title bar shows the required-pattern chip.
     * The window controller turns this on while the window is in create mode for sections
     * hosting at least one required pattern.
     */
    private final BooleanProperty requiredChipVisible = new SimpleBooleanProperty(false);
    public boolean isRequiredChipVisible() { return requiredChipVisible.get(); }
    public BooleanProperty requiredChipVisibleProperty() { return requiredChipVisible; }
    public void setRequiredChipVisible(boolean visible) { requiredChipVisible.set(visible); }

    // -- required satisfied
    /**
     * Whether the section's required condition has been met.
     * Dashed amber REQUIRED while unsatisfied, green "✓ REQUIREMENT MET" once satisfied.
     */
    private final BooleanProperty requiredSatisfied = new SimpleBooleanProperty(false);
    public boolean isRequiredSatisfied() { return requiredSatisfied.get(); }
    public BooleanProperty requiredSatisfiedProperty() { return requiredSatisfied; }
    public void setRequiredSatisfied(boolean satisfied) { requiredSatisfied.set(satisfied); }

    // -- number columns
    private final IntegerProperty numberColumns = new SimpleIntegerProperty();
    public int getNumberColumns() { return numberColumns.get(); }
    public IntegerProperty numberColumnsProperty() { return numberColumns; }
    public void setNumberColumns(int numberColumns) { this.numberColumns.set(numberColumns); }

    // -- items
    // The views laid out in this Section's content.
    private final ObservableList<Node> items = FXCollections.observableArrayList();
    public ObservableList<Node> getItems() { return items; }

    // -- reference components
    private final ObservableList<T> referenceComponents = FXCollections.observableArrayList();
    public ObservableList<T> getReferenceComponents() { return referenceComponents; }

    // -- reference components cell factory
    private final ObjectProperty<Callback<ListView<T>, ListCell<T>>> referenceComponentCellFactory = new SimpleObjectProperty<>();
    public final void setReferenceComponentCellFactory(Callback<ListView<T>, ListCell<T>> value) { referenceComponentCellFactoryProperty().set(value); }
    public final Callback<ListView<T>, ListCell<T>> getReferenceComponentCellFactory() {return referenceComponentCellFactoryProperty().get(); }
    public final ObjectProperty<Callback<ListView<T>, ListCell<T>>> referenceComponentCellFactoryProperty() { return referenceComponentCellFactory; }

    // -- reference components button cell factory
    private final ObjectProperty<ListCell<T>> referenceComponentButtonCellFactory = new SimpleObjectProperty<>(this, "buttonCell");
    public final ObjectProperty<ListCell<T>> referenceComponentButtonCellFactoryProperty() { return referenceComponentButtonCellFactory; }
    public final void setReferenceComponentButtonCellFactory(ListCell<T> value) { referenceComponentButtonCellFactoryProperty().set(value); }
    public final ListCell<T> getReferenceComponentButtonCellFactory() {return referenceComponentButtonCellFactoryProperty().get(); }

    // -- selected reference component
    private final ObjectProperty<T> selectedReferenceComponent = new SimpleObjectProperty<>();
    public final ObjectProperty<T> selectedReferenceComponentProperty() { return selectedReferenceComponent; }
    public final void setSelectedReferenceComponent(T selectedReferenceComponent) { selectedReferenceComponentProperty().set(selectedReferenceComponent); }
    public final T getSelectedReferenceComponent() { return selectedReferenceComponentProperty().get(); }
}