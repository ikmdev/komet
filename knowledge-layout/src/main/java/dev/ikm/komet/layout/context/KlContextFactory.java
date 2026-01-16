package dev.ikm.komet.layout.context;


import dev.ikm.komet.layout.KlGadget;
import dev.ikm.komet.preferences.KometPreferences;

import java.util.function.Supplier;

/**
 * A factory interface for creating instances of {@link KlContext}.
 * This factory extends {@link Supplier} to provide a functional programming
 * approach for generating KlContext instances.
 * <p>
 * KlContexts are hierarchical, but the hierarchy is not defined by, or represented within,
 * the factory. The hierarchy is defined dynamically (late binding) by discovering
 * the other contexts within the KlGadgets hierarchy, and then finally the top level
 * application or user context stored in the KometPreferences for the user, or the
 * shared preferences.
 */
public interface KlContextFactory  {
    /**
     * Creates a new instance of {@link KlContext} using the provided {@link KlContextProvider}.
     * This method leverages the context provider to initialize and configure the resulting
     * {@link KlContext}, providing an encapsulated representation of user interface and layout
     * orchestration properties.
     *
     * @param klContextProvider an instance of {@link KlContextProvider} used to supply
     *                          information and dependencies required for creating the
     *                          {@link KlContext}
     * @return a new instance of {@link KlContext} configured based on the provided
     *         {@link KlContextProvider}
     */
    KlContext create(KlContextProvider klContextProvider);

    /**
     * Restores a {@link KlContext} from the provided preferences and associates it with
     * the provided context provider.
     * This method re-establishes a previously persisted state of a {@link KlContext}
     * using the preferences and a given context provider.
     *
     * @param preferences an instance of {@link KometPreferences} containing the persisted
     *                    state and configuration of the {@link KlContext}
     * @param klContextProvider an instance of {@link KlContextProvider} that contributes to
     *                          creating or retrieving the required {@link KlContext}
     *
     * @return a {@link KlContext} instance restored to the state represented in
     *         the provided preferences
     */
    KlContext restore(KometPreferences preferences, KlContextProvider klContextProvider);
}
