package dev.ikm.komet.framework.observable;

import org.eclipse.collections.api.list.ImmutableList;

/**
 * ObservableComponent defines a sealed interface that represents a
 * foundational construct for components in a system. This interface
 * is designed to be extended by specific observable component types.
 * <p>
 * As a sealed interface, the extensions of ObservableComponent are
 * explicitly restricted to the defined permits. The interface serves
 * as a common type for observing and interacting with various system-level
 * components, managing their versioning, state, and observable behaviors.
 * <p>
 * <p>Permitted subtypes:
 *<p> - ObservableEntity: Represents an observable construct associated
 *   with an entity, which includes fields and versions for observation.
 *<p> - ObservableVersion: Represents an observable version of an entity,
 *   equipped with properties like state, time, and author.
 *<p> - ObservableField: Represents an observable field within a system,
 *   capable of handling changes and maintaining field properties.
 *<p> - ObservableFieldDefinition: Defines an observable definition
 *   for fields, including metadata such as data type and purpose.
 * <p>
 * This interface enables consistent handling and interaction with
 * observable components across the system, leveraging its extensions
 * for specific component behaviors.
 */
public sealed interface ObservableComponent
        permits ObservableEntity, ObservableVersion {
    /**
     * Retrieves an immutable list of features associated with this observable component
     * based on the provided stamp calculator. The list represents the attributes or
     * characteristics of the component, each defined by a {@code Feature}.
     *
     * @return an {@code ImmutableList} of {@code Feature} objects associated with this component.
     */
    ImmutableList<Feature> getFeatures();

    /**
     * Retrieves the native identifier (nid) of the observable component.
     * The nid is a unique, integer-based identifier used to represent
     * and distinguish components within the system.
     *
     * @return the native identifier (nid) as an integer.
     */
    int nid();
}
