package dev.ikm.komet.layout;

/**
 * Stages an edit made inline in a semantic's view (e.g. in the stated definition's axiom tree) as a
 * version not published yet, for the hosting window's Publish action to commit along with its other
 * staged changes. Supplied by a window with a Publish action of its own (see
 * {@link PatternSemanticsPresenter#setInlineEditStager}); without one, inline edits commit as they
 * are applied.
 */
@FunctionalInterface
public interface InlineEditStager {
    /**
     * @param semanticNid  the semantic the edit was made to
     * @param fieldIndex   the edited field's index in the semantic's pattern
     * @param newValue     the field's new value
     */
    void stageFieldValue(int semanticNid, int fieldIndex, Object newValue);
}
