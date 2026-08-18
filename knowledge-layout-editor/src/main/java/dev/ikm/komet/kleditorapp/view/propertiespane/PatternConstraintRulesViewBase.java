package dev.ikm.komet.kleditorapp.view.propertiespane;

import dev.ikm.komet.kview.controls.KLComponentControl;
import dev.ikm.komet.layout.editor.model.EditorPatternModel;
import dev.ikm.komet.layout.editor.model.EditorSemanticConstraintBase;
import javafx.beans.property.BooleanProperty;
import javafx.beans.property.SimpleBooleanProperty;
import javafx.collections.ObservableList;
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
import java.util.List;
import java.util.Set;

/**
 * An editable list of {@link EditorSemanticConstraintBase} rules for a Pattern, as shown in the
 * {@link PatternPropertiesPane}: a demoted title, one row per rule, and an "add" link. Each rule
 * shows as a one-line summary that expands in place into an editor with one row per Pattern field —
 * concept-typed fields get a {@link KLComponentControl} so the field value to match can be entered
 * with inline type-ahead search (or drag and drop), other fields accept any value (see
 * {@link FieldConstraintsEditor}).
 *
 * <p>Subclasses supply what differs between the two kinds of rule: which list of the Pattern they
 * edit, how a rule reads as a summary, and any editor rows and summary sources beyond the field
 * constraints (the requirement's minimum count).
 *
 * @param <T> the kind of rule edited
 */
public abstract class PatternConstraintRulesViewBase<T extends EditorSemanticConstraintBase> extends VBox {

    private final VBox rulesContainer = new VBox();

    /** Shown while there are no rules, when the subclass asked for it; {@code null} otherwise. */
    private final Label emptyStateLabel;

    private EditorPatternModel pattern;
    private Subscription rulesSubscription;

    /**
     * The rules currently expanded into their editor, so expansion survives the list rebuilds
     * triggered by rule adds and removes.
     */
    private final Set<T> expandedRules = new HashSet<>();

    /**
     * @param title the area's title, shown above the rules
     * @param addRuleText the text of the link that adds a rule
     * @param emptyStateText what to show while there are no rules, or {@code null} to show nothing
     * @param styleClass the subclass's own style class, added next to the shared one
     */
    protected PatternConstraintRulesViewBase(String title, String addRuleText, String emptyStateText,
                                             String styleClass) {
        Label titleLabel = new Label(title);
        titleLabel.getStyleClass().add("rules-title");

        rulesContainer.getStyleClass().add("rules-container");

        Hyperlink addRuleLink = new Hyperlink(addRuleText);
        addRuleLink.getStyleClass().add("add-rule");
        addRuleLink.setOnAction(_ -> addRule());

        getChildren().add(titleLabel);

        if (emptyStateText == null) {
            emptyStateLabel = null;
        } else {
            emptyStateLabel = new Label(emptyStateText);
            emptyStateLabel.getStyleClass().add("empty-state");
            emptyStateLabel.managedProperty().bind(emptyStateLabel.visibleProperty());
            getChildren().add(emptyStateLabel);
        }

        getChildren().addAll(rulesContainer, addRuleLink);

        // CSS
        getStyleClass().addAll("constraint-rules", styleClass);
    }

    /**
     * Shows (and edits) the rules of the passed in Pattern.
     *
     * @param pattern the Pattern whose rules to show
     */
    public final void setPattern(EditorPatternModel pattern) {
        if (rulesSubscription != null) {
            rulesSubscription.unsubscribe();
        }

        this.pattern = pattern;
        expandedRules.clear();

        rulesSubscription = rulesOf(pattern).subscribe(this::rebuildRules);
        rebuildRules();
    }

    /**
     * The Pattern currently shown, for subclasses resolving rule summaries against it.
     */
    protected final EditorPatternModel getPattern() {
        return pattern;
    }

    /**
     * The list of rules this view edits, e.g. {@link EditorPatternModel#getRequirements()}.
     */
    protected abstract ObservableList<T> rulesOf(EditorPatternModel pattern);

    /**
     * A new, unconstrained rule, added when the user clicks the add link.
     */
    protected abstract T createRule();

    /**
     * The rule as its one-line collapsed summary.
     */
    protected abstract String summaryOf(T rule);

    /**
     * Editor rows shown above the rule's field constraints — the requirement's minimum count row.
     * None by default.
     */
    protected List<Node> createEditorHeaderNodes(T rule) {
        return List.of();
    }

    /**
     * Subscribes anything the rule's summary depends on beyond its field constraints (which are
     * always subscribed), so the collapsed summary tracks it. Nothing by default.
     *
     * @param rule the rule shown
     * @param updateSummary re-renders the rule's summary
     * @return the subscription, unsubscribed when the rule's view goes away
     */
    protected Subscription subscribeExtraSummarySources(T rule, Runnable updateSummary) {
        return Subscription.EMPTY;
    }

    private void addRule() {
        T rule = createRule();
        // Expand before adding: the add triggers the rebuild that creates the rule's view.
        expandedRules.add(rule);
        rulesOf(pattern).add(rule);
    }

    private void rebuildRules() {
        rulesContainer.getChildren().forEach(node -> ((RuleView) node).dispose());
        rulesContainer.getChildren().setAll(rulesOf(pattern).stream().map(RuleView::new).toList());

        if (emptyStateLabel != null) {
            emptyStateLabel.setVisible(rulesOf(pattern).isEmpty());
        }
    }

    /**
     * A single rule: a header with the rule's summary, expand chevron and delete button, plus the
     * in-place editor shown while expanded.
     */
    private class RuleView extends VBox {
        private final T rule;

        private final Label summaryLabel = new Label();
        private final BooleanProperty expanded = new SimpleBooleanProperty();
        private final Subscription summarySubscription;

        RuleView(T rule) {
            this.rule = rule;

            expanded.set(expandedRules.contains(rule));
            expanded.subscribe((Boolean isExpanded) -> {
                if (isExpanded) {
                    expandedRules.add(rule);
                } else {
                    expandedRules.remove(rule);
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
                expandedRules.remove(rule);
                rulesOf(pattern).remove(rule);
            });

            HBox header = new HBox(expandButton, summaryLabel, deleteButton);
            header.getStyleClass().add("rule-header");
            header.setAlignment(Pos.CENTER_LEFT);

            Node editor = createEditor();
            editor.visibleProperty().bind(expanded);
            editor.managedProperty().bind(editor.visibleProperty());

            getChildren().addAll(header, editor);

            // Summary text tracks the rule's constraints, plus whatever else the subclass renders it from
            summarySubscription = rule.getFieldConstraints().subscribe(this::updateSummary)
                    .and(subscribeExtraSummarySources(rule, this::updateSummary));
            updateSummary();

            // CSS
            getStyleClass().add("constraint-rule");
        }

        void dispose() {
            summarySubscription.unsubscribe();
        }

        private Node createEditor() {
            VBox editor = new VBox();
            editor.getStyleClass().add("rule-editor");
            editor.getChildren().addAll(createEditorHeaderNodes(rule));
            editor.getChildren().add(new FieldConstraintsEditor(pattern, rule.getFieldConstraints()));
            return editor;
        }

        private void updateSummary() {
            summaryLabel.setText(summaryOf(rule));
        }
    }
}
