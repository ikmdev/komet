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

    Node getView();
}