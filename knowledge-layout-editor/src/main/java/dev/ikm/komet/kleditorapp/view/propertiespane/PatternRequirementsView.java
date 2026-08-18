package dev.ikm.komet.kleditorapp.view.propertiespane;

import dev.ikm.komet.layout.editor.model.EditorPatternModel;
import dev.ikm.komet.layout.editor.model.EditorPatternRequirement;
import javafx.collections.ObservableList;
import javafx.geometry.Pos;
import javafx.scene.Node;
import javafx.scene.control.Label;
import javafx.scene.control.Spinner;
import javafx.scene.layout.HBox;
import javafx.util.Subscription;

import java.util.List;

/**
 * The "REQUIRED SEMANTICS" area of the {@link PatternPropertiesPane}: the requirement refinements
 * ({@link EditorPatternRequirement}) of a required Pattern, edited as
 * {@linkplain PatternConstraintRulesViewBase constraint rules} with a minimum semantic count each.
 */
public class PatternRequirementsView extends PatternConstraintRulesViewBase<EditorPatternRequirement> {
    public static final String DEFAULT_STYLE_CLASS = "pattern-requirements";

    public PatternRequirementsView() {
        super("Required semantics", "+ Add requirement", null, DEFAULT_STYLE_CLASS);
    }

    @Override
    protected ObservableList<EditorPatternRequirement> rulesOf(EditorPatternModel pattern) {
        return pattern.getRequirements();
    }

    @Override
    protected EditorPatternRequirement createRule() {
        return new EditorPatternRequirement();
    }

    @Override
    protected List<Node> createEditorHeaderNodes(EditorPatternRequirement requirement) {
        // "At least <n> semantic(s) where"
        Spinner<Integer> minCountSpinner = new Spinner<>(1, 99, requirement.getMinCount());
        minCountSpinner.getStyleClass().add("min-count-spinner");
        minCountSpinner.setPrefWidth(55);
        minCountSpinner.valueProperty().subscribe(requirement::setMinCount);

        HBox countRow = new HBox(new Label("At least"), minCountSpinner, new Label("semantic(s) where"));
        countRow.getStyleClass().add("count-row");
        countRow.setAlignment(Pos.CENTER_LEFT);

        return List.of(countRow);
    }

    @Override
    protected Subscription subscribeExtraSummarySources(EditorPatternRequirement requirement,
                                                        Runnable updateSummary) {
        return requirement.minCountProperty().subscribe(updateSummary);
    }

    @Override
    protected String summaryOf(EditorPatternRequirement requirement) {
        String count = requirement.getMinCount() + "×";

        if (requirement.getFieldConstraints().isEmpty()) {
            return count + " any semantic";
        }

        return count + " where "
                + FieldConstraintsEditor.describe(getPattern(), requirement.getFieldConstraints());
    }
}
