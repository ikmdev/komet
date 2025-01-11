package dev.ikm.komet.layout.preferences;

import dev.ikm.komet.layout.window.KlWindowFactory;
import dev.ikm.komet.preferences.KometPreferences;

public interface KlUniversalPreferences {

    KometPreferences preferences();

    Class<? extends KlWindowFactory> getFactoryClass();

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
