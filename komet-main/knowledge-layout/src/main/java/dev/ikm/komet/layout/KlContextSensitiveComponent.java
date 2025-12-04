package dev.ikm.komet.layout;

/**
 * The {@code KlContextSensitiveComponent} interface defines the structure for components
 * that interact with a {@code KlContext}, enabling them to subscribe and unsubscribe
 * to contextual properties or events dynamically. This serves as a foundational contract
 * for creating context-aware components in the knowledge layout system.
 *
 * TODO: Change implementation from default after refactor of widgets is possible.
 */
public interface KlContextSensitiveComponent {

    /**
     * Signals that this instance of {@code KlGadget} needs to unsubscribe from all  {@code KlContext} properties,
     * prior to {@code KlGadget} deletion or reorganization. The specific behavior and implementation of this method
     * are left to the discretion of the implementing class. Calls to {@code KlGadget.unsubscribeFromContext()}
     * must occur in a depth-first manner, staring with the top {@code KlGadget} that will encapsulate all the
     * intended changes. It is not the responsibility of this method to provide the depth-first logic. That responsibility
     * is placed on the {@code KlContext} object which will notify subordinate {@code KlGadget} of an impending change.
     */
    default void unsubscribeFromContext(){};

    /**
     * Signals this {@code KlGadget} instance to subscribe to any necessary {@code KlContext} properties
     * or events. This method ensures the gadget is actively synchronized with any relevant contextual
     * updates within the knowledge layout system. The specific subscription logic and its scope
     * are left to the implementing class.
     * <p>
     * It is the responsibility of the implementing class to define how and which properties or
     * events of the {@code KlContext} are subscribed to. This provides flexibility for the gadget
     * to interact with its contextual environment according to its requirements.
     */
    default void subscribeToContext(){};


}
