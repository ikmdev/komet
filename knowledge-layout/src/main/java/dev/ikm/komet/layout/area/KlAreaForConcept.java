package dev.ikm.komet.layout.area;

import dev.ikm.komet.framework.observable.Feature;
import dev.ikm.tinkar.common.bind.annotations.axioms.ParentConcept;
import dev.ikm.tinkar.common.bind.annotations.names.FullyQualifiedName;
import dev.ikm.tinkar.common.bind.annotations.names.RegularName;
import dev.ikm.tinkar.terms.ConceptFacade;
import javafx.scene.layout.Region;

/**
 * Represents a specialized non-sealed interface for a field area tied to the concept domain within the Knowledge Layout framework.
 * This interface extends {@code KlFieldArea} to handle field areas associated with the {@code ConceptFacade} data type and
 * its corresponding JavaFX {@code Region}.
 *
 * This interface provides a foundation for managing and displaying concept-related fields in a type-safe and extensible manner.
 * Through generic parameters, it enables precise association between the {@code ConceptFacade} data type and its respective JavaFX
 * {@code Region} implementation.
 *
 * @param <FX> the specific type of JavaFX {@code Region} associated with this concept field area.
 */
@FullyQualifiedName("Knowledge layout concept feature area")
@RegularName("Concept area")
@ParentConcept(KlAreaForFeature.class)
public non-sealed interface KlAreaForConcept<FX extends Region>
        extends KlAreaForFeature<ConceptFacade, Feature<ConceptFacade>, FX> {

    /**
     * Represents a factory interface for creating and managing instances of {@code KlFieldAreaForConcept}
     * associated with the {@code ConceptFacade} data type and JavaFX {@code Region} components.
     * <p>
     * This interface extends the generic {@code KlFieldArea.Factory} to provide specialized contract definitions
     * for constructing, configuring, and managing field areas tied specifically to the {@code ConceptFacade} domain.
     * It ensures the type-safe creation of field areas that bind observable fields of type {@code ConceptFacade}
     * with their respective JavaFX regions of type {@code FX}.
     *
     * @param <FX> the specific type of JavaFX {@code Region} associated with this factory, extending {@code Region}.
     */
    interface Factory<FX extends Region>
            extends KlAreaForFeature.Factory<ConceptFacade, Feature<ConceptFacade>, FX, KlAreaForConcept<FX>> {
    }

}