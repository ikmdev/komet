package dev.ikm.komet.layout;

import dev.ikm.tinkar.entity.SemanticEntity;
import dev.ikm.tinkar.entity.SemanticEntityVersion;
import javafx.scene.Node;

public interface PatternSemanticsPresenter {

    /**
     * The sections of the window anchored on one of this presenter's semantics (the semantic is
     * their chosen reference component): their names, comma-separated, and the index of the color
     * the semantic's view marks it with. Sections anchored on the same semantic share the color,
     * so a semantic carries one mark. The same color is on those sections' reference comboboxes,
     * which is how the user pairs them; 0 is the default mark, the one a semantic gets when its
     * only referencing section is alone on the pattern.
     */
    record ReferencingSection(String sectionName, int colorIndex) {}

    /**
     * Marks the semantic's view with the sections anchored on it, replacing the mark it had; null
     * clears it. A semantic this presenter does not show is ignored. Presenters without a
     * per-semantic view have nothing to do.
     */
    default void setReferencedBy(SemanticEntity<SemanticEntityVersion> semanticEntity, ReferencingSection referencedBy) {
    }

    /**
     * Whether the user can currently see the semantic's view, or enough of it to recognize it:
     * false for a semantic this presenter does not show (its pattern's display filters hide it),
     * one scrolled out of view, or one in a collapsed section. Answered from the current layout.
     * Presenters without a per-semantic view never show one.
     */
    default boolean isSemanticOnScreen(SemanticEntity<SemanticEntityVersion> semanticEntity) {
        return false;
    }

    void addNewSemantic(SemanticEntity<SemanticEntityVersion> semanticEntity);
    void clearSemantics();

    void setPreviewingSemantic(SemanticEntity<SemanticEntityVersion> semanticEntity);
    void setEditingSemantic(SemanticEntity<SemanticEntityVersion> semanticEntity);

    /**
     * Brings the given semantic's view into sight, scrolling whatever hides it, so the user can find
     * it from a summary elsewhere in the window. Presenters without a scrolling view have nothing
     * to do.
     */
    default void revealSemantic(SemanticEntity<SemanticEntityVersion> semanticEntity) {
    }

    /**
     * Sets the saver that edits made inline in the semantics' views (e.g. the stated definition's
     * axiom tree) go through, so they stay uncommitted for the hosting window's Publish action instead of
     * committing as they are applied. Set before semantics are added. Presenters without inline
     * editing have nothing to do.
     */
    default void setInlineEditSaver(InlineEditSaver inlineEditSaver) {
    }

    Node getView();
}