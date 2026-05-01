package dev.ikm.komet.kview.mvvm.view.genpurpose;

import dev.ikm.tinkar.entity.SemanticEntity;
import dev.ikm.tinkar.entity.SemanticEntityVersion;
import javafx.scene.Node;

public interface PatternSemanticsPresenter {
    void addNewSemantic(SemanticEntity<SemanticEntityVersion> semanticEntity);
    void clearSemantics();

    void setPreviewingSemantic(SemanticEntity<SemanticEntityVersion> semanticEntity);
    void setEditingSemantic(SemanticEntity<SemanticEntityVersion> semanticEntity);

    Node getView();
}