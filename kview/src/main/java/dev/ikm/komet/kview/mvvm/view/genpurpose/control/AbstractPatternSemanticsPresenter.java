package dev.ikm.komet.kview.mvvm.view.genpurpose.control;

import dev.ikm.komet.framework.observable.ObservableEntityHandle;
import dev.ikm.komet.framework.observable.ObservableSemantic;
import dev.ikm.komet.framework.observable.ObservableSemanticSnapshot;
import dev.ikm.komet.framework.observable.ObservableSemanticVersion;
import dev.ikm.tinkar.coordinate.stamp.calculator.Latest;
import dev.ikm.tinkar.coordinate.view.calculator.ViewCalculator;
import dev.ikm.tinkar.entity.SemanticEntity;
import dev.ikm.tinkar.entity.SemanticEntityVersion;

public class AbstractPatternSemanticsPresenter {

    /**
     * The semantic's latest version <em>for the given view</em>, resolved through its snapshot so the
     * view coordinate's path, module, state and time filters apply — the same resolution the classic
     * concept window uses ({@code ConceptController.fields()}).
     *
     * <p>Not {@code ObservableEntity.versions().getLast()}: that list is an unordered map's values
     * ({@code IntObjectHashMap} keyed by stamp nid), so its last element is an arbitrary version of
     * the semantic and ignores the view entirely. A semantic with a single version — most
     * descriptions — looked right either way, but one with several (a stated definition carrying
     * releases plus authored edits) rendered a different definition from the one the classic window
     * shows.
     *
     * @param semanticEntity the semantic to resolve
     * @param viewCalculator the view to resolve it against
     * @return the latest version for the view, absent when the view holds no version of this semantic
     */
    protected static Latest<ObservableSemanticVersion> latestVersionForView(
            SemanticEntity<SemanticEntityVersion> semanticEntity, ViewCalculator viewCalculator) {
        return snapshotForView(semanticEntity, viewCalculator).getLatestVersion();
    }

    /**
     * The semantic's snapshot for the given view: its latest version (see {@link #latestVersionForView})
     * plus the versions before it and the ones not published yet.
     *
     * @param semanticEntity the semantic to resolve
     * @param viewCalculator the view to resolve it against
     * @return the snapshot
     */
    protected static ObservableSemanticSnapshot snapshotForView(
            SemanticEntity<SemanticEntityVersion> semanticEntity, ViewCalculator viewCalculator) {
        ObservableSemantic observableSemantic = ObservableEntityHandle.get(semanticEntity.publicId())
                .asSemantic().orElseThrow(() -> new IllegalArgumentException(
                        "Entity is not a semantic: " + semanticEntity.publicId()));
        return observableSemantic.getSnapshot(viewCalculator);
    }

}
