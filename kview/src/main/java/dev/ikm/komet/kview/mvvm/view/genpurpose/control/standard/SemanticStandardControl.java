package dev.ikm.komet.kview.mvvm.view.genpurpose.control.standard;

import dev.ikm.komet.kview.controls.KLReadOnlyBaseControl;
import javafx.beans.property.BooleanProperty;
import javafx.beans.property.IntegerProperty;
import javafx.beans.property.SimpleBooleanProperty;
import javafx.beans.property.SimpleIntegerProperty;
import javafx.beans.property.SimpleStringProperty;
import javafx.beans.property.StringProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.geometry.Orientation;
import javafx.scene.control.Control;
import javafx.scene.control.Skin;

/**
 * A control that shows 1 Semantic.
 */
public class SemanticStandardControl extends Control {

    public SemanticStandardControl() {
        getStyleClass().add("semantic-view");
    }

    @Override
    protected Skin<?> createDefaultSkin() {
        return new SemanticStandardControlSkin(this);
    }

    /**
     * A field's value wraps, so a field's height — and with it this control's height — depends on
     * the width its column gives it. Without a content bias the enclosing layouts measure this
     * control with {@code prefHeight(-1)} and only make room for single-line fields, which squeezes
     * every field in the semantic.
     *
     * @return {@link Orientation#HORIZONTAL}
     */
    @Override
    public Orientation getContentBias() {
        return Orientation.HORIZONTAL;
    }

    // -- read only fields
    private ObservableList<KLReadOnlyBaseControl> readOnlyFields = FXCollections.observableArrayList();
    public  ObservableList<KLReadOnlyBaseControl> getFields() { return readOnlyFields; }

    // -- placeholder text
    /**
     * Text rendered in place of this semantic's fields. Set when the semantic has no version for the
     * current view: there are no field values to lay out, so the semantic shows one muted line saying
     * so — the treatment the classic concept window gives such a semantic. Null (the default) renders
     * the fields.
     */
    private final StringProperty placeholderText = new SimpleStringProperty();
    public String getPlaceholderText() { return placeholderText.get(); }
    public StringProperty placeholderTextProperty() { return placeholderText; }
    public void setPlaceholderText(String placeholderText) { this.placeholderText.set(placeholderText); }

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

    // -- preview mode
    BooleanProperty previewMode = new SimpleBooleanProperty();
    public boolean isPreviewMode() { return previewMode.get(); }
    public BooleanProperty previewModeProperty() { return previewMode; }
    public void setPreviewMode(boolean previewMode) { this.previewMode.set(previewMode); }
}
