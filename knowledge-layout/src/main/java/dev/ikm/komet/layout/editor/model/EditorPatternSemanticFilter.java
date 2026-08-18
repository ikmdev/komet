package dev.ikm.komet.layout.editor.model;

/**
 * A filter on which of a Pattern's semantics are displayed (see
 * {@link EditorPatternModel#getSemanticFilters()}): a semantic passes the filter when its field
 * values match the filter's {@link #getFieldConstraints() fieldConstraints}. For example, the
 * Description Pattern can be filtered so only semantics whose "Description type" field is "Fully
 * qualified name" are shown.
 *
 * <p>A Pattern with no filters shows all of its semantics, and a Pattern with several filters shows
 * the semantics passing any one of them.
 */
public class EditorPatternSemanticFilter extends EditorSemanticConstraintBase {

    /**
     * Encodes this filter into a single preferences string: its encoded field constraints, which is
     * everything a filter is (the empty string when it constrains nothing).
     */
    public String toPreferenceString() {
        return constraintsToPreferenceString();
    }

    /**
     * Decodes a filter encoded by {@link #toPreferenceString()}.
     */
    public static EditorPatternSemanticFilter fromPreferenceString(String encoded) {
        EditorPatternSemanticFilter filter = new EditorPatternSemanticFilter();
        filter.loadConstraintsFromPreferenceString(encoded);
        return filter;
    }
}
