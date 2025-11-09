
package dev.ikm.komet.framework.observable;

import dev.ikm.komet.framework.observable.binding.Binding;
import dev.ikm.tinkar.terms.EntityFacade;

/**
 * Marker interface for observable entities that represent chronologies (versioned entities).
 * <p>
 * This sealed interface simplifies generic signatures in downstream code by providing a
 * type-level alias for "observable entity with observable versions" without requiring
 * explicit version type parameters.
 *
 * <p><b>Purpose:</b> Enables simpler generic constraints like:
 * <pre>{@code
 * // Before:
 * class MyClass<OE extends ObservableEntity<ObservableVersion<OE, ?>>>
 *
 * // After:
 * class MyClass<OE extends ObservableChronology>
 * }</pre>
 *
 * @see ObservableEntity
 */
public sealed interface ObservableChronology
        extends EntityFacade, ObservableComponent, Feature<ObservableChronology>
        permits ObservableConcept, ObservablePattern, ObservableSemantic, ObservableStamp {

    default int patternNid() {
        return switch (ObservableChronology.this) {
            case ObservableConcept _-> Binding.Concept.pattern().nid();
            case ObservablePattern _-> Binding.Pattern.pattern().nid();
            case ObservableSemantic _-> Binding.Semantic.pattern().nid();
            case ObservableStamp _-> Binding.Stamp.pattern().nid();
        };
    }

    default int indexInPattern() {
        // TODO: replace with a new index after adding a new field for the meaning and purpose of the entity class
        return 0;
    }

    default ObservableComponent containingComponent() {
        return this;
    }

    default FeatureKey featureKey() {
        return FeatureKey.Entity.Object(nid());
    }

}

