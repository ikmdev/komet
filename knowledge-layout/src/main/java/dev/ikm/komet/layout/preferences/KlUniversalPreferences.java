package dev.ikm.komet.layout.preferences;

import dev.ikm.komet.layout.KlFactory;
import dev.ikm.komet.layout.window.KlWindowFactory;
import dev.ikm.komet.preferences.KometPreferences;

import java.util.Optional;

public interface KlUniversalPreferences {

    KometPreferences preferences();

    default Class<? extends KlFactory> getFactoryClass() {
        Optional<String> factoryClassString =  preferences().get(Keys.FACTORY_CLASS);
        if (factoryClassString.isPresent()) {
            try {
                return (Class<? extends KlFactory>) Class.forName(factoryClassString.get());
            } catch (ClassNotFoundException e) {
                throw new RuntimeException(e);
            }
        }
        throw new IllegalStateException("Preferences not initialized with Keys.FACTORY_CLASS");
    }

    enum Keys {
        /**
         * Boolean string representing if the preferences have been initialized.
         */
        INITIALIZED,
        /**
         * Fully qualified name of the factory class. Used to restore the KlWindow from preferences.
         */
        FACTORY_CLASS
    }

    default void universalInitialize() {
        preferences().putBoolean(Keys.INITIALIZED, true);
        preferences().put(Keys.FACTORY_CLASS, getFactoryClass().getName());
        classInitialize();
    }

    void classInitialize();

}
