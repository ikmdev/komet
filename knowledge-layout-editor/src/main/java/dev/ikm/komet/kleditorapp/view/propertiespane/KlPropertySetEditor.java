package dev.ikm.komet.kleditorapp.view.propertiespane;

import dev.ikm.komet.kview.controls.ToggleSwitch;
import dev.ikm.komet.layout.editor.property.KlPropertySet;
import javafx.beans.property.BooleanProperty;
import javafx.beans.property.DoubleProperty;
import javafx.beans.property.IntegerProperty;
import javafx.beans.property.Property;
import javafx.beans.property.StringProperty;
import javafx.collections.FXCollections;
import javafx.scene.Node;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Label;
import javafx.scene.control.Spinner;
import javafx.scene.control.TextField;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.RowConstraints;

/**
 * Builds the editor UI for a {@link KlPropertySet}, Scene Builder style: each property is rendered
 * with a control chosen by its value <em>type</em>, bound bidirectionally to the live JavaFX
 * property so edits flow straight into the model (and on to the journal control).
 *
 * <p>The properties are laid out in a two-column {@link GridPane} — labels in the first column,
 * editing controls in the second — so every label lines up vertically with the other labels and
 * every control with the other controls, matching the fixed properties (Column Position, Row Span,
 * ...) elsewhere in the properties pane.
 *
 * <p>This is the "built-in renderer per type" registry. Adding support for a new property type means
 * adding a branch here — the property declarations themselves stay plain JavaFX properties.
 */
public final class KlPropertySetEditor {

    public static final String CONTAINER_STYLE_CLASS = "factory-properties-container";

    private KlPropertySetEditor() {
    }

    /**
     * Creates a container node rendering every property in the given set, one per grid row. Returns
     * an empty container when the set is {@code null} or has no properties.
     *
     * @param propertySet the property set to render
     * @return a grid of labelled controls, one row per property
     */
    public static Node create(KlPropertySet propertySet) {
        GridPane container = new PropertyGridPane();
        container.getStyleClass().add(CONTAINER_STYLE_CLASS);
        container.setVgap(4);

        if (propertySet == null) {
            return container;
        }

        int rowIndex = 0;
        for (KlPropertySet.KlPropertyItem item : propertySet.discoverProperties()) {
            Node control = createControl(item);
            if (control == null) {
                continue;
            }

            Label label = new Label(item.displayName() + ":");

            RowConstraints row = new RowConstraints();
            row.setMinHeight(10);
            row.setPrefHeight(30);
            container.getRowConstraints().add(row);

            container.addRow(rowIndex++, label, control);
        }

        return container;
    }

    /**
     * Builds just the editing control for a property, choosing it by the property's value type. The
     * label is added separately by {@link #create} so labels and controls stay column-aligned.
     */
    private static Node createControl(KlPropertySet.KlPropertyItem item) {
        Class<?> type = item.valueType();
        Property<?> property = item.property();

        // A constrained property renders as a drop-down regardless of its underlying type.
        if (!item.choices().isEmpty()) {
            return createChoiceEditor(item);
        }

        if (type == Boolean.class) {
            ToggleSwitch toggleSwitch = new ToggleSwitch();
            toggleSwitch.selectedProperty().bindBidirectional((BooleanProperty) property);
            return toggleSwitch;
        }

        if (type == String.class) {
            TextField textField = new TextField();
            textField.textProperty().bindBidirectional((StringProperty) property);
            return textField;
        }

        if (type == Integer.class) {
            IntegerProperty integerProperty = (IntegerProperty) property;
            Spinner<Integer> spinner = new Spinner<>(Integer.MIN_VALUE, Integer.MAX_VALUE, integerProperty.get());
            spinner.setEditable(true);
            spinner.getValueFactory().valueProperty().bindBidirectional(integerProperty.asObject());
            return spinner;
        }

        if (type == Double.class) {
            DoubleProperty doubleProperty = (DoubleProperty) property;
            Spinner<Double> spinner = new Spinner<>(-Double.MAX_VALUE, Double.MAX_VALUE, doubleProperty.get(), 1.0);
            spinner.setEditable(true);
            spinner.getValueFactory().valueProperty().bindBidirectional(doubleProperty.asObject());
            return spinner;
        }

        if (Enum.class.isAssignableFrom(type)) {
            return createEnumEditor(type, property);
        }

        // Unsupported type for now: render nothing rather than guess at a control.
        return null;
    }

    @SuppressWarnings({"unchecked", "rawtypes"})
    private static Node createChoiceEditor(KlPropertySet.KlPropertyItem item) {
        Class<?> type = item.valueType();
        Property<?> property = item.property();

        ComboBox comboBox = new ComboBox<>(FXCollections.observableArrayList(item.choices()));

        if (type == Integer.class) {
            comboBox.valueProperty().bindBidirectional(((IntegerProperty) property).asObject());
        } else if (type == Double.class) {
            comboBox.valueProperty().bindBidirectional(((DoubleProperty) property).asObject());
        } else {
            comboBox.valueProperty().bindBidirectional(property);
        }

        return comboBox;
    }

    @SuppressWarnings({"unchecked", "rawtypes"})
    private static Node createEnumEditor(Class<?> type, Property<?> property) {
        ComboBox comboBox = new ComboBox<>(FXCollections.observableArrayList(type.getEnumConstants()));
        comboBox.valueProperty().bindBidirectional(property);
        return comboBox;
    }
}
