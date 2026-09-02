package dev.ikm.komet.kleditorapp.view.propertiespane;

import dev.ikm.komet.kview.controls.KLComponentComboBoxControl;
import dev.ikm.komet.kview.controls.KLComponentControl;
import dev.ikm.komet.kview.controls.KLComponentControlFactory;
import dev.ikm.komet.kview.klfields.ComponentFieldOptions;
import dev.ikm.komet.layout.editor.model.EditorPatternModel;
import dev.ikm.tinkar.coordinate.view.calculator.ViewCalculator;
import dev.ikm.tinkar.entity.FieldDefinitionRecord;
import dev.ikm.tinkar.terms.EntityProxy;
import javafx.collections.ObservableMap;
import javafx.scene.Node;
import javafx.scene.control.Label;
import javafx.scene.layout.VBox;

import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

import static dev.ikm.tinkar.terms.TinkarTerm.COMPONENT_FIELD;
import static dev.ikm.tinkar.terms.TinkarTerm.CONCEPT_FIELD;

/**
 * The per-field constraint editor shared by the rules of {@link PatternRequirementsView} and
 * {@link PatternSemanticFiltersView}: one row per Pattern field — the field's name above its
 * constraint editor, so names aren't truncated by the pane's narrow width. Concept-typed fields
 * whose values come from a predefined set of concepts (description type, case significance,
 * language, ... — the same rule set the KL windows' semantic editors use, see
 * {@link ComponentFieldOptions}) get a clearable {@link KLComponentComboBoxControl} to pick the
 * constraint concept from (clearing it lifts the constraint); other concept-typed fields get a
 * {@link KLComponentControl} so the constraint concept can be entered with inline type-ahead search
 * (or drag and drop); the remaining fields accept any value.
 *
 * <p>The rows are the Pattern's fields as the database defines them
 * ({@link EditorPatternModel#getFieldDefinitions()}), not the fields laid out in the editor window:
 * a rule selects semantics by their values, so it can constrain a field the author chose not to
 * display, and removing a field from the layout must not take away the ability to constrain it.
 *
 * <p>Edits are written straight into the passed in constraints map — clearing a field's concept
 * removes its entry, which is what "accepts any value" is.
 */
class FieldConstraintsEditor extends VBox {

    FieldConstraintsEditor(EditorPatternModel pattern, ObservableMap<Integer, EntityProxy> constraints) {
        for (FieldDefinitionRecord field : pattern.getFieldDefinitions()) {
            Label fieldNameLabel = new Label(getFieldTitle(field));
            fieldNameLabel.getStyleClass().add("field-name");

            VBox fieldRow = new VBox(fieldNameLabel, createFieldValueNode(pattern, constraints, field));
            fieldRow.getStyleClass().add("field-row");
            getChildren().add(fieldRow);
        }

        // CSS
        getStyleClass().add("fields-container");
    }

    private static Node createFieldValueNode(EditorPatternModel pattern,
                                             ObservableMap<Integer, EntityProxy> constraints,
                                             FieldDefinitionRecord field) {
        int dataTypeNid = field.dataTypeNid();
        if (dataTypeNid != COMPONENT_FIELD.nid() && dataTypeNid != CONCEPT_FIELD.nid()) {
            // Only concept-valued fields can be constrained (for now)
            Label anyValueLabel = new Label("Any value");
            anyValueLabel.getStyleClass().add("any-value");
            anyValueLabel.setMaxWidth(Double.MAX_VALUE);
            return anyValueLabel;
        }

        ViewCalculator viewCalculator = pattern.getViewCalculator();
        Optional<List<EntityProxy>> componentOptions = ComponentFieldOptions.componentOptions(viewCalculator, field);
        return componentOptions
                .map(options -> createComboBoxNode(viewCalculator, constraints, field, options))
                .orElseGet(() -> createComponentControlNode(viewCalculator, constraints, field));
    }

    /**
     * A combo box of the given options for a field constrained to a predefined set of concepts,
     * clearable so the constraint can be lifted again.
     */
    private static Node createComboBoxNode(ViewCalculator viewCalculator,
                                           ObservableMap<Integer, EntityProxy> constraints,
                                           FieldDefinitionRecord field,
                                           List<EntityProxy> options) {
        KLComponentComboBoxControl comboBoxControl =
                KLComponentControlFactory.createComponentComboBoxControl(viewCalculator, options);
        comboBoxControl.setPromptText("Any value");
        comboBoxControl.setClearable(true);
        comboBoxControl.setMaxWidth(Double.MAX_VALUE);

        comboBoxControl.setValue(constraints.get(field.indexInPattern()));

        comboBoxControl.valueProperty().subscribe(() -> {
            EntityProxy value = comboBoxControl.getValue();
            if (value == null) {
                constraints.remove(field.indexInPattern());
            } else {
                constraints.put(field.indexInPattern(), value);
            }
        });

        return comboBoxControl;
    }

    /**
     * A free-form component control for a field that may be constrained to any concept.
     */
    private static Node createComponentControlNode(ViewCalculator viewCalculator,
                                                   ObservableMap<Integer, EntityProxy> constraints,
                                                   FieldDefinitionRecord field) {
        KLComponentControl componentControl = KLComponentControlFactory.createComponentControl(viewCalculator);
        componentControl.setMaxWidth(Double.MAX_VALUE);

        EntityProxy constraint = constraints.get(field.indexInPattern());
        if (constraint != null) {
            componentControl.setEntity(constraint);
        }

        componentControl.entityProperty().subscribe(() -> {
            EntityProxy entity = componentControl.getEntity();
            if (KLComponentControl.isEmpty(entity)) {
                constraints.remove(field.indexInPattern());
            } else {
                constraints.put(field.indexInPattern(), entity);
            }
        });

        return componentControl;
    }

    /**
     * The constraints as one line of text — {@code "Description type = Fully qualified name, ..."} —
     * for the collapsed summary of a rule. Empty when there are no constraints.
     *
     * @param pattern the Pattern the constrained fields belong to
     * @param constraints the constrained fields, keyed by field index
     * @return the constraints rendered as text
     */
    static String describe(EditorPatternModel pattern, ObservableMap<Integer, EntityProxy> constraints) {
        return constraints.entrySet().stream()
                .sorted(Map.Entry.comparingByKey())
                .map(constraint -> getFieldTitle(pattern, constraint.getKey())
                        + " = " + conceptName(pattern, constraint.getValue()))
                .collect(Collectors.joining(", "));
    }

    private static String getFieldTitle(EditorPatternModel pattern, int fieldIndex) {
        return pattern.getFieldDefinitions().stream()
                .filter(field -> field.indexInPattern() == fieldIndex)
                .findFirst()
                .map(FieldConstraintsEditor::getFieldTitle)
                .orElse("Field " + fieldIndex);
    }

    private static String getFieldTitle(FieldDefinitionRecord field) {
        return field.meaning().description();
    }

    private static String conceptName(EditorPatternModel pattern, EntityProxy concept) {
        return pattern.getViewCalculator().languageCalculator().getDescriptionTextOrNid(concept.nid());
    }
}
