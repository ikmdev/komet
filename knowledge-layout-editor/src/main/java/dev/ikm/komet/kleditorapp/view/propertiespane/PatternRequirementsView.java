package dev.ikm.komet.kleditorapp.view.propertiespane;

import dev.ikm.komet.kview.controls.KLComponentControl;
import dev.ikm.komet.layout.editor.model.EditorPatternModel;
import dev.ikm.komet.layout.editor.model.EditorPatternRequirement;
import javafx.beans.property.BooleanProperty;
import javafx.beans.property.SimpleBooleanProperty;
import javafx.geometry.Pos;
import javafx.scene.Node;
import javafx.scene.control.Button;
import javafx.scene.control.Hyperlink;
import javafx.scene.control.Label;
import javafx.scene.control.Spinner;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.Region;
import javafx.scene.layout.VBox;
import javafx.util.Subscription;

import java.util.HashSet;
import java.util.Set;

/**
 * The "REQUIRED SEMANTICS" area of the {@link PatternPropertiesPane}: the list of requirement
 * refinements ({@link EditorPatternRequirement}) of a required Pattern. Each rule shows as a
 * one-line summary that expands in place into an editor with the rule's minimum semantic count and
 * one row per Pattern field — concept-typed fields get a {@link KLComponentControl} so the
 * constraint concept can be entered with inline type-ahead search (or drag and drop), other fields
 * accept any value (see {@link FieldConstraintsEditor}).
 */
public class PatternRequirementsView extends VBox {
    public static final String DEFAULT_STYLE_CLASS = "pattern-requirements";

    private final VBox rulesContainer = new VBox();

    private EditorPatternModel pattern;
    private Subscription requirementsSubscription;

    /**
     * The rules currently expanded into their editor, so expansion survives the list rebuilds
     * triggered by rule adds and removes.
     */
    private final Set<EditorPatternRequirement> expandedRules = new HashSet<>();

    public PatternRequirementsView() {
        Label titleLabel = new Label("Required semantics");
        titleLabel.getStyleClass().add("rules-title");

        rulesContainer.getStyleClass().add("rules-container");

        Hyperlink addRequirementLink = new Hyperlink("+ Add requirement");
        addRequirementLink.getStyleClass().add("add-rule");
        addRequirementLink.setOnAction(_ -> addRequirement());

        getChildren().addAll(titleLabel, rulesContainer, addRequirementLink);

        // CSS
        getStyleClass().addAll("constraint-rules", DEFAULT_STYLE_CLASS);
    }

    /**
     * Shows (and edits) the requirements of the passed in Pattern.
     *
     * @param pattern the Pattern whose requirements to show
     */
    public void setPattern(EditorPatternModel pattern) {
        if (requirementsSubscription != null) {
            requirementsSubscription.unsubscribe();
        }

        this.pattern = pattern;
        expandedRules.clear();

        requirementsSubscription = pattern.getRequirements().subscribe(this::rebuildRules);
        rebuildRules();
    }

    private void addRequirement() {
        EditorPatternRequirement requirement = new EditorPatternRequirement();
        // Expand before adding: the add triggers the rebuild that creates the rule's view.
        expandedRules.add(requirement);
        pattern.getRequirements().add(requirement);
    }

    private void rebuildRules() {
        rulesContainer.getChildren().forEach(node -> ((RuleView) node).dispose());
        rulesContainer.getChildren().setAll(
                pattern.getRequirements().stream().map(RuleView::new).toList());
    }

    /**
     * A single requirement rule: a header with the rule's summary, expand chevron and delete
     * button, plus the in-place editor shown while expanded.
     */
    private class RuleView extends VBox {
        private final EditorPatternRequirement requirement;

        private final Label summaryLabel = new Label();
        private final BooleanProperty expanded = new SimpleBooleanProperty();
        private final Subscription summarySubscription;

        RuleView(EditorPatternRequirement requirement) {
            this.requirement = requirement;

            expanded.set(expandedRules.contains(requirement));
            expanded.subscribe((Boolean isExpanded) -> {
                if (isExpanded) {
                    expandedRules.add(requirement);
                } else {
                    expandedRules.remove(requirement);
                }
            });

            // Header
            Region chevronIcon = new Region();
            chevronIcon.getStyleClass().addAll("icon", "chevron-right");
            expanded.subscribe((Boolean isExpanded) -> chevronIcon.setRotate(isExpanded ? 90 : 0));

            Button expandButton = new Button();
            expandButton.setGraphic(chevronIcon);
            expandButton.getStyleClass().add("icon-button");
            expandButton.setOnAction(_ -> expanded.set(!expanded.get()));

            summaryLabel.getStyleClass().add("rule-summary");
            summaryLabel.setMaxWidth(Double.MAX_VALUE);
            HBox.setHgrow(summaryLabel, Priority.ALWAYS);

            Region deleteIcon = new Region();
            deleteIcon.getStyleClass().addAll("icon", "cross");

            Button deleteButton = new Button();
            deleteButton.setGraphic(deleteIcon);
            deleteButton.getStyleClass().add("icon-button");
            deleteButton.setOnAction(_ -> {
                expandedRules.remove(requirement);
                pattern.getRequirements().remove(requirement);
            });

            HBox header = new HBox(expandButton, summaryLabel, deleteButton);
            header.getStyleClass().add("rule-header");
            header.setAlignment(Pos.CENTER_LEFT);

            Node editor = createEditor();
            editor.visibleProperty().bind(expanded);
            editor.managedProperty().bind(editor.visibleProperty());

            getChildren().addAll(header, editor);

            // Summary text tracks the rule's count and constraints
            summarySubscription = requirement.minCountProperty().subscribe(this::updateSummary)
                    .and(requirement.getFieldConstraints().subscribe(this::updateSummary));
            updateSummary();

            // CSS
            getStyleClass().addAll("constraint-rule", "requirement-rule");
        }

        void dispose() {
            summarySubscription.unsubscribe();
        }

        private Node createEditor() {
            VBox editor = new VBox();
            editor.getStyleClass().add("rule-editor");

            // "At least <n> semantic(s) where"
            Spinner<Integer> minCountSpinner = new Spinner<>(1, 99, requirement.getMinCount());
            minCountSpinner.getStyleClass().add("min-count-spinner");
            minCountSpinner.setPrefWidth(55);
            minCountSpinner.valueProperty().subscribe(requirement::setMinCount);

            HBox countRow = new HBox(new Label("At least"), minCountSpinner, new Label("semantic(s) where"));
            countRow.getStyleClass().add("count-row");
            countRow.setAlignment(Pos.CENTER_LEFT);

            editor.getChildren().addAll(countRow,
                    new FieldConstraintsEditor(pattern, requirement.getFieldConstraints()));
            return editor;
        }

        private void updateSummary() {
            String count = requirement.getMinCount() + "×";

            if (requirement.getFieldConstraints().isEmpty()) {
                summaryLabel.setText(count + " any semantic");
                return;
            }

            summaryLabel.setText(count + " where "
                    + FieldConstraintsEditor.describe(pattern, requirement.getFieldConstraints()));
        }
    }
}
