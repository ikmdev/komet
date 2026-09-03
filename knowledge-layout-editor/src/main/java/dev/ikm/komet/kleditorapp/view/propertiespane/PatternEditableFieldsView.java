package dev.ikm.komet.kleditorapp.view.propertiespane;

import dev.ikm.komet.kview.controls.ToggleSwitch;
import dev.ikm.komet.layout.editor.model.EditorPatternModel;
import dev.ikm.tinkar.entity.FieldDefinitionRecord;
import javafx.beans.property.BooleanProperty;
import javafx.geometry.Pos;
import javafx.scene.control.Label;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.Region;
import javafx.scene.layout.VBox;

import java.util.ArrayList;
import java.util.List;

/**
 * The "Editable fields" area of the {@link PatternPropertiesPane}'s INTERACTION group: one row per
 * field the Pattern is defined with ({@link EditorPatternModel#getFieldDefinitions()}), each with a
 * toggle for whether the field can still be edited once the window is in edit mode in the Journal
 * (see {@link EditorPatternModel#fieldEditableProperty(int)}).
 *
 * <p>Every field is listed, not only the ones laid out in the editor: a field removed from the layout
 * can't be selected there to be authored on, yet is still edited in the Journal's edit form.
 */
public class PatternEditableFieldsView extends VBox {
    public static final String DEFAULT_STYLE_CLASS = "editable-fields";

    private final VBox rowsContainer = new VBox();

    /** The rows shown, so their toggles can be unbound from the previous Pattern's properties. */
    private final List<FieldRow> rows = new ArrayList<>();

    private record FieldRow(ToggleSwitch toggle, BooleanProperty editable) {}

    public PatternEditableFieldsView() {
        Label titleLabel = new Label("Editable fields");
        titleLabel.getStyleClass().add("fields-title");

        rowsContainer.getStyleClass().add("rules-container");

        getChildren().addAll(titleLabel, rowsContainer);

        // CSS: laid out like the pane's rule lists — indented off a guide line, with a demoted title
        getStyleClass().addAll("constraint-rules", DEFAULT_STYLE_CLASS);
    }

    /**
     * Shows (and edits) the editability of the fields of the passed in Pattern.
     *
     * @param pattern the Pattern whose fields to show
     */
    public void setPattern(EditorPatternModel pattern) {
        rows.forEach(row -> row.toggle().selectedProperty().unbindBidirectional(row.editable()));
        rows.clear();
        rowsContainer.getChildren().clear();

        for (FieldDefinitionRecord field : pattern.getFieldDefinitions()) {
            Label fieldNameLabel = new Label(field.meaning().description());
            fieldNameLabel.getStyleClass().add("field-name");

            Region spacer = new Region();
            HBox.setHgrow(spacer, Priority.ALWAYS);

            ToggleSwitch editableTSwitch = new ToggleSwitch();
            BooleanProperty editable = pattern.fieldEditableProperty(field.indexInPattern());
            editableTSwitch.selectedProperty().bindBidirectional(editable);

            HBox row = new HBox(fieldNameLabel, spacer, editableTSwitch);
            row.getStyleClass().add("editable-field-row");
            row.setAlignment(Pos.CENTER_LEFT);

            rowsContainer.getChildren().add(row);
            rows.add(new FieldRow(editableTSwitch, editable));
        }
    }
}
