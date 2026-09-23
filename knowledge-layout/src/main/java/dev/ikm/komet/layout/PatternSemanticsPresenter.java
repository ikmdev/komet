package dev.ikm.komet.layout;

import dev.ikm.tinkar.entity.SemanticEntity;
import dev.ikm.tinkar.entity.SemanticEntityVersion;
import javafx.scene.Node;

public interface PatternSemanticsPresenter {
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
     * Sets the stager that edits made inline in the semantics' views (e.g. the stated definition's
     * axiom tree) go through, so they stage for the hosting window's Publish action instead of
     * committing as they are applied. Set before semantics are added. Presenters without inline
     * editing have nothing to do.
     */
    default void setInlineEditStager(InlineEditStager inlineEditStager) {
    }

    Node getView();
}