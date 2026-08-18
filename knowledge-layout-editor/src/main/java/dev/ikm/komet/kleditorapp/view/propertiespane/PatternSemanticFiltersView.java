package dev.ikm.komet.kleditorapp.view.propertiespane;

import dev.ikm.komet.layout.editor.model.EditorPatternModel;
import dev.ikm.komet.layout.editor.model.EditorPatternSemanticFilter;
import javafx.collections.ObservableList;

/**
 * The "VISIBLE SEMANTICS" area of the {@link PatternPropertiesPane}: the display filters
 * ({@link EditorPatternSemanticFilter}) picking which of a Pattern's semantics are shown, edited as
 * {@linkplain PatternConstraintRulesViewBase constraint rules}. A Description Pattern filtered to
 * "Description type = Fully qualified name" shows only its fully qualified name. With no filters
 * every semantic is shown; with several, the semantics passing any one of them are.
 */
public class PatternSemanticFiltersView extends PatternConstraintRulesViewBase<EditorPatternSemanticFilter> {
    public static final String DEFAULT_STYLE_CLASS = "pattern-semantic-filters";

    public PatternSemanticFiltersView() {
        super("Show only semantics where", "+ Add filter", "All semantics are shown", DEFAULT_STYLE_CLASS);
    }

    @Override
    protected ObservableList<EditorPatternSemanticFilter> rulesOf(EditorPatternModel pattern) {
        return pattern.getSemanticFilters();
    }

    @Override
    protected EditorPatternSemanticFilter createRule() {
        return new EditorPatternSemanticFilter();
    }

    @Override
    protected String summaryOf(EditorPatternSemanticFilter filter) {
        if (filter.getFieldConstraints().isEmpty()) {
            // A filter with no field values set matches everything, so say so rather than leaving
            // the header blank.
            return "Any semantic";
        }

        return FieldConstraintsEditor.describe(getPattern(), filter.getFieldConstraints());
    }
}
