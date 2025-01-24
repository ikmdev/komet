package dev.ikm.komet.layout.preferences;

import dev.ikm.komet.layout.window.KlWindow;
import dev.ikm.komet.preferences.KometPreferences;

import java.util.concurrent.atomic.AtomicReference;
import java.util.prefs.BackingStoreException;

class KlPreferenceFactoryProvider  {

    /**
     * The {@code PreferenceFactoryWithParentFactory} class is an implementation of {@code KlPreferencesFactory}.
     * It provides a mechanism to create and manage unique {@code KometPreferences} nodes
     * within a hierarchical structure, associating them with a specific implementation class.
     * This implementation ensures that preferences are lazily initialized and associated
     * with a parent factory to enable hierarchical preference management.
     */
    static class PreferenceFactoryWithParentFactory implements KlPreferencesFactory {
        private final KlPreferencesFactory preferencesFactory;
        private final AtomicReference<KometPreferences> parentPreferences = new AtomicReference<>();
        private final Class implementationClass;

        PreferenceFactoryWithParentFactory(KlPreferencesFactory preferencesFactory, Class implementationClass) {
            this.preferencesFactory = preferencesFactory;
            this.implementationClass = implementationClass;
        }

        @Override
        public KometPreferences get() {
            return parentPreferences.accumulateAndGet(null,
                    (existingValue, unused) ->
                            existingValue != null ? existingValue :
                                    createSequentiallyUniquePreferences(preferencesFactory.get(), implementationClass));
        }

        /**
         * Creates a new {@code KlPreferencesFactory} instance that provides preferences specific
         * to the given implementation class within the context of the current preferences factory.
         *
         * @param implementationClass The class for which the specific preferences factory is being created.
         * @return A {@code KlPreferencesFactory} instance associated with the provided implementation class.
         */
        @Override
        public KlPreferencesFactory childFactory(Class implementationClass) {
            return new PreferenceFactoryWithParentFactory(this, implementationClass);
        }
    }

    /**
     * A factory class that provides a mechanism to create instances of {@code KometPreferences}
     * within the context of a specified parent preferences node. This factory allows for the creation
     * of preferences associated with a specific implementation class, ensuring unique nodes
     * under the given parent preferences.
     *
     * This implementation supports hierarchical preferences management through the parent-child
     * relationship of preference nodes.
     */
    static class PreferenceFactoryWithParentPreferences implements KlPreferencesFactory {
        private final KometPreferences parentPreferences;
        private final Class implementationClass;

        PreferenceFactoryWithParentPreferences(KometPreferences parentPreferences, Class implementationClass) {
            this.parentPreferences = parentPreferences;
            this.implementationClass = implementationClass;
        }

        @Override
        public KometPreferences get() {
            return createSequentiallyUniquePreferences(parentPreferences, implementationClass);
        }

        /**
         * Creates a new {@code KlPreferencesFactory} instance that is specific
         * to the given implementation class and tied to the current context
         * of this preferences factory. This allows for hierarchical and
         * context-specific preference management.
         *
         * @param implementationClass The class for which the preferences factory should be created.
         * @return A new instance of {@code KlPreferencesFactory} associated with the specified implementation class.
         */
        @Override
        public KlPreferencesFactory childFactory(Class implementationClass) {
            return new PreferenceFactoryWithParentFactory(this, implementationClass);
        }
    }


    /**
     * Creates a new unique {@code KometPreferences} node for the specified implementation class within the
     * specified parent preferences node. The uniqueness is ensured by appending a numeric sequence to the
     * node name if nodes with the same base name already exist.
     *
     * @param parentPreferences the parent {@code KometPreferences} node in which the new node will be created
     * @param implementationClass the class for which the preference node is to be created
     * @return the new {@code KometPreferences} node associated with the specified implementation class
     * @throws RuntimeException if an error occurs while checking existing nodes or creating the new node
     */
    private static KometPreferences createSequentiallyUniquePreferences(KometPreferences parentPreferences, Class implementationClass) {
        int sequence = 0;
        try {
            while (parentPreferences.nodeExists(sequentialNodeName(implementationClass, sequence))) {
                sequence++;
            }
        } catch (BackingStoreException e) {
            throw new RuntimeException(e);
        }
        return parentPreferences.node(sequentialNodeName(implementationClass, sequence));
    }

    /**
     * Generates a unique sequential name for a preference node based on the simple name of the given
     * implementation class and a sequence number.
     *
     * @param implementationClass the class of the {@code KlWindow} implementation for which the sequential node
     *                            name is being generated
     * @param sequence the integer sequence number to append to the base name of the implementation class
     * @return a string representing the sequentially generated node name in the format
     *         "{ClassSimpleName}_{sequence}"
     */
    private static String sequentialNodeName(Class<? extends KlWindow> implementationClass,
                                             int sequence) {
        return implementationClass.getSimpleName() + "_" + sequence;
    }
}
