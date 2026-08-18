package dev.ikm.komet.kleditorapp.view.propertiespane;

import dev.ikm.komet.kview.controls.KLComponentControl;
import dev.ikm.komet.layout.editor.model.EditorPatternModel;
import dev.ikm.komet.layout.editor.model.EditorPatternSemanticFilter;
import javafx.beans.property.BooleanProperty;
import javafx.beans.property.SimpleBooleanProperty;
import javafx.geometry.Pos;
import javafx.scene.Node;
import javafx.scene.control.Button;
import javafx.scene.control.Hyperlink;
import javafx.scene.control.Label;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.Region;
import javafx.scene.layout.VBox;
import javafx.util.Subscription;

import java.util.HashSet;
import java.util.Set;

/**
 * The "VISIBLE SEMANTICS" area of the {@link PatternPropertiesPane}: the list of display filters
 * ({@link EditorPatternSemanticFilter}) picking which of a Pattern's semantics are shown — a
 * Description Pattern filtered to "Description type = Fully qualified name" shows only its fully
 * qualified name. With no filters every semantic is shown; with several, the semantics passing any
 * one of them are.
 *
 * <p>Each filter shows as a one-line summary that expands in place into an editor with one row per
 * Pattern field — concept-typed fields get a {@link KLComponentControl} so the field value to match
 * can be entered with inline type-ahead search (or drag and drop), other fields accept any value
 * (see {@link FieldConstraintsEditor}). This mirrors {@link PatternRequirementsView}, which
 * constrains fields the same way to demand semantics rather than to hide them.
 */
public class PatternSemanticFiltersView extends VBox {
    public static final String DEFAULT_STYLE_CLASS = "pattern-semantic-filters";

    private final VBox rulesContainer = new VBox();

    /** Shown while there are no filters, so "everything is visible" reads as a state, not as emptiness. */
    private final Label allShownLabel = new Label("All semantics are shown");

    private EditorPatternModel pattern;
    private Subscription filtersSubscription;

    /**
     * The filters currently expanded into their editor, so expansion survives the list rebuilds
     * triggered by filter adds and removes.
     */
    private final Set<EditorPatternSemanticFilter> expandedRules = new HashSet<>();

    public PatternSemanticFiltersView() {
        Label titleLabel = new Label("Show only semantics where");
        titleLabel.getStyleClass().add("rules-title");

        rulesContainer.getStyleClass().add("rules-container");

        allShownLabel.getStyleClass().add("all-shown");
        allShownLabel.managedProperty().bind(allShownLabel.visibleProperty());

        Hyperlink addFilterLink = new Hyperlink("+ Add filter");
        addFilterLink.getStyleClass().add("add-rule");
        addFilterLink.setOnAction(_ -> addFilter());

        getChildren().addAll(titleLabel, allShownLabel, rulesContainer, addFilterLink);

        // CSS
        getStyleClass().addAll("constraint-rules", DEFAULT_STYLE_CLASS);
    }

    /**
     * Shows (and edits) the semantic display filters of the passed in Pattern.
     *
     * @param pattern the Pattern whose filters to show
     */
    public void setPattern(EditorPatternModel pattern) {
        if (filtersSubscription != null) {
            filtersSubscription.unsubscribe();
        }

        this.pattern = pattern;
        expandedRules.clear();

        filtersSubscription = pattern.getSemanticFilters().subscribe(this::rebuildRules);
        rebuildRules();
    }

    private void addFilter() {
        EditorPatternSemanticFilter filter = new EditorPatternSemanticFilter();
        // Expand before adding: the add triggers the rebuild that creates the filter's view.
        expandedRules.add(filter);
        pattern.getSemanticFilters().add(filter);
    }

    private void rebuildRules() {
        rulesContainer.getChildren().forEach(node -> ((RuleView) node).dispose());
        rulesContainer.getChildren().setAll(
                pattern.getSemanticFilters().stream().map(RuleView::new).toList());

        allShownLabel.setVisible(pattern.getSemanticFilters().isEmpty());
    }

    /**
     * A single filter: a header with the filter's summary, expand chevron and delete button, plus
     * the in-place editor shown while expanded.
     */
    private class RuleView extends VBox {
        private final EditorPatternSemanticFilter filter;

        private final Label summaryLabel = new Label();
        private final BooleanProperty expanded = new SimpleBooleanProperty();
        private final Subscription summarySubscription;

        RuleView(EditorPatternSemanticFilter filter) {
            this.filter = filter;

            expanded.set(expandedRules.contains(filter));
            expanded.subscribe((Boolean isExpanded) -> {
                if (isExpanded) {
                    expandedRules.add(filter);
                } else {
                    expandedRules.remove(filter);
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
                expandedRules.remove(filter);
                pattern.getSemanticFilters().remove(filter);
            });

            HBox header = new HBox(expandButton, summaryLabel, deleteButton);
            header.getStyleClass().add("rule-header");
            header.setAlignment(Pos.CENTER_LEFT);

            Node editor = createEditor();
            editor.visibleProperty().bind(expanded);
            editor.managedProperty().bind(editor.visibleProperty());

            getChildren().addAll(header, editor);

            // Summary text tracks the filter's constraints
            summarySubscription = filter.getFieldConstraints().subscribe(this::updateSummary);
            updateSummary();

            // CSS
            getStyleClass().addAll("constraint-rule", "filter-rule");
        }

        void dispose() {
            summarySubscription.unsubscribe();
        }

        private Node createEditor() {
            VBox editor = new VBox();
            editor.getStyleClass().add("rule-editor");
            editor.getChildren().add(new FieldConstraintsEditor(pattern, filter.getFieldConstraints()));
            return editor;
        }

        private void updateSummary() {
            if (filter.getFieldConstraints().isEmpty()) {
                // A filter with no field values set matches everything, so say so rather than
                // leaving the header blank.
                summaryLabel.setText("Any semantic");
                return;
            }

            summaryLabel.setText(FieldConstraintsEditor.describe(pattern, filter.getFieldConstraints()));
        }
    }
}
