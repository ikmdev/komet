package dev.ikm.komet.kview.mvvm.view.genpurpose.control;

import dev.ikm.komet.kview.controls.KLReadOnlyBaseControl;
import javafx.beans.property.BooleanProperty;
import javafx.beans.property.IntegerProperty;
import javafx.beans.property.SimpleBooleanProperty;
import javafx.beans.property.SimpleIntegerProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.scene.control.Control;
import javafx.scene.control.Skin;

/**
 * A control that shows 1 Semantic.
 */
public class SemanticViewControl extends Control {

    public SemanticViewControl() {
        getStyleClass().add("semantic-view");
    }

    @Override
    protected Skin<?> createDefaultSkin() {
        return new SemanticViewControlSkin(this);
    }

    // -- read only fields
    private ObservableList<KLReadOnlyBaseControl> readOnlyFields = FXCollections.observableArrayList();
    public  ObservableList<KLReadOnlyBaseControl> getFields() { return readOnlyFields; }

    // -- number columns
    private IntegerProperty numberColumns = new SimpleIntegerProperty();
    public int getNumberColumns() { return numberColumns.get(); }
    public IntegerProperty numberColumnsProperty() { return numberColumns; }
    public void setNumberColumns(int numberColumns) { this.numberColumns.set(numberColumns); }

    // -- edit mode
    BooleanProperty editMode = new SimpleBooleanProperty();
    public boolean isEditMode() { return editMode.get(); }
    public BooleanProperty editModeProperty() { return editMode; }
    public void setEditMode(boolean editMode) { this.editMode.set(editMode); }
}
