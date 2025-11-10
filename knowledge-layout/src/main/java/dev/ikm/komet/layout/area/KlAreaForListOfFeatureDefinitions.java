package dev.ikm.komet.layout.area;

import dev.ikm.komet.framework.observable.ObservableFeatureDefinition;
import javafx.scene.layout.Region;

/**
 * A non-sealed interface extending {@link KlAreaForListOfFeatures}, designed to handle and manage
 * areas associated with observable field definitions within a JavaFX {@link Region}. This interface
 * specializes in facilitating interactions with field definition components, enabling dynamic and modular
 * configurations within the Knowledge Layout (KL) framework.
 *
 * Classes implementing this interface are expected to provide concrete functionality for managing
 * observable field definitions and their integration into specified JavaFX regions, ensuring seamless
 * data updates and feature handling in interactive environments.
 *
 * @param <FX> the type of JavaFX {@link Region} to which the managed field definitions are associated
 */
public non-sealed interface KlAreaForListOfFeatureDefinitions<FX extends Region>
        extends KlAreaForListOfFeatures<ObservableFeatureDefinition, FX> {


    /**
     * Defines a factory interface for creating and managing instances of
     * {@code KlListOfFieldDefinitionArea} associated with JavaFX {@code Region} components.
     * This interface specializes in handling observable field definitions within
     * list-based areas in the Knowledge Layout (KL) framework.
     * <p>
     * Implementations of this factory are responsible for defining methods to create,
     * restore, and identify implementations of {@code KlListOfFieldDefinitionArea},
     * enabling dynamic instantiation and configuration of field definition areas.
     *
     * @param <FX> the type of JavaFX {@code Region} associated with the field definition area
     */
    interface Factory<FX extends Region>
            extends KlAreaForListOfFeatures.Factory<ObservableFeatureDefinition, FX, KlAreaForListOfFeatureDefinitions<FX>> {

    }
}
