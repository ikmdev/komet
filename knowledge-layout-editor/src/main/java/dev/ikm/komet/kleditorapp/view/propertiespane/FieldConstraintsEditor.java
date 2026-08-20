package dev.ikm.komet.kleditorapp.view.propertiespane;

import dev.ikm.komet.kview.controls.KLComponentControl;
import dev.ikm.komet.kview.controls.KLComponentControlFactory;
import dev.ikm.komet.layout.editor.model.EditorPatternModel;
import dev.ikm.tinkar.entity.FieldDefinitionRecord;
import dev.ikm.tinkar.terms.EntityProxy;
import javafx.collections.ObservableMap;
import javafx.scene.Node;
import javafx.scene.control.Label;
import javafx.scene.layout.VBox;

import java.util.Map;
import java.util.stream.Collectors;

import static dev.ikm.tinkar.terms.TinkarTerm.COMPONENT_FIELD;
import static dev.ikm.tinkar.terms.TinkarTerm.CONCEPT_FIELD;

/**
 * The per-field constraint editor shared by the rules of {@link PatternRequirementsView} and
 * {@link PatternSemanticFiltersView}: one row per Pattern field — the field's name above its
 * constraint editor, so names aren't truncated by the pane's narrow width. Concept-typed fields get a
 * {@link KLComponentControl} so the constraint concept can be entered with inline type-ahead search
 * (or drag and drop); other fields accept any value.
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

        KLComponentControl componentControl =
                KLComponentControlFactory.createComponentControl(pattern.getViewCalculator());
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
