package dev.ikm.komet.layout;

/**
 * Saves an edit made inline in a semantic's view (e.g. in the stated definition's axiom tree) as an
 * uncommitted version, for the hosting window's Publish action to commit along with its other
 * uncommitted changes. Supplied by a window with a Publish action of its own (see
 * {@link PatternSemanticsPresenter#setInlineEditSaver}); without one, inline edits commit as they
 * are applied.
 */
@FunctionalInterface
public interface InlineEditSaver {
    /**
     * @param semanticNid  the semantic the edit was made to
     * @param fieldIndex   the edited field's index in the semantic's pattern
     * @param newValue     the field's new value
     */
    void saveUncommittedFieldValue(int semanticNid, int fieldIndex, Object newValue);
}
