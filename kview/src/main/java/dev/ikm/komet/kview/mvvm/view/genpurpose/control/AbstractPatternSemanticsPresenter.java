package dev.ikm.komet.kview.mvvm.view.genpurpose.control;

import dev.ikm.komet.framework.observable.ObservableEntityHandle;
import dev.ikm.komet.framework.observable.ObservableSemantic;
import dev.ikm.komet.framework.observable.ObservableSemanticVersion;
import dev.ikm.tinkar.entity.SemanticEntity;
import dev.ikm.tinkar.entity.SemanticEntityVersion;

public class AbstractPatternSemanticsPresenter {

    protected static ObservableSemanticVersion getObservableSemanticFromSemanticEntity(SemanticEntity<SemanticEntityVersion> semanticEntity) {
        ObservableSemantic observableSemantic = ObservableEntityHandle.get(semanticEntity.publicId())
                .asSemantic().orElseThrow(() -> new IllegalArgumentException(
                        "Entity is not a semantic: " + semanticEntity.publicId()));
        ObservableSemanticVersion latestVersion = observableSemantic.versions().getLast();
        return latestVersion;
    }

}