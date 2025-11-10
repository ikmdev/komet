package dev.ikm.komet.layout.area;

import dev.ikm.komet.framework.observable.Feature;
import dev.ikm.tinkar.common.bind.annotations.axioms.ParentConcept;
import dev.ikm.tinkar.common.bind.annotations.names.FullyQualifiedName;
import dev.ikm.tinkar.common.bind.annotations.names.RegularName;
import javafx.scene.layout.Region;

/**
 * Represents an interface in the Knowledge Layout framework to manage field areas
 * specifically associated with objects and their corresponding JavaFX regional components.
 * This non-sealed interface builds upon {@link KlFieldArea} with a generalized focus
 * on object-related attributes and their visualization.
 *
 * @param <FX> The type of JavaFX {@link Region} associated with this field area for displaying or managing the object-related fields.
 */
@FullyQualifiedName("Knowledge layout object feature area")
@RegularName("Object area")
@ParentConcept(KlAreaForFeature.class)
public non-sealed interface KlAreaForObject<FX extends Region>
        extends KlAreaForFeature<Object, Feature<Object>, FX> {

    /**
     * Represents a factory interface for creating and managing instances of knowledge layout field areas specifically
     * associated with objects and their corresponding JavaFX regional components. This factory provides the contract
     * for building field areas associated with object data types and managing their properties within JavaFX regions.
     *
     * Extending from {@code KlFieldArea.Factory}, this interface focuses on field areas that integrate object-type observable
     * fields with JavaFX {@code Region} elements. It enables the creation, restoration, and configuration of these field
     * areas, supporting modular and reusable components for object-related layouts.
     *
     * @param <FX> the type of JavaFX region associated with the field area, extending {@code Region}.
     */
    interface Factory<FX extends Region>
            extends KlAreaForFeature.Factory<Object, Feature<Object>, FX, KlAreaForObject<FX>> {

    }

}
