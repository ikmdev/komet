package dev.ikm.komet.layout.preferences;

import dev.ikm.komet.layout.KlFactory;
import dev.ikm.komet.layout.window.KlWindow;
import dev.ikm.komet.layout.window.KlWindowFactory;
import dev.ikm.komet.preferences.KometPreferences;
import dev.ikm.tinkar.common.util.time.DateTimeUtil;

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
        FACTORY_CLASS,

        /**
         * Represents the name of the specific implementation of a {@link KlWindow}
         * that can be restored from preferences. This key is used to identify
         * and manage restoration of the window's state during application initialization
         * or when reloading user preferences.
         */
        NAME_FOR_RESTORE
    }

    default void universalInitialize(KlFactory factory) {
        preferences().putBoolean(Keys.INITIALIZED, true);
        preferences().put(Keys.FACTORY_CLASS, factory.getClass().getName());
        preferences().put(Keys.NAME_FOR_RESTORE, factory.klName() + " from " + DateTimeUtil.timeNowSimple());
        classInitialize();
    }

    void classInitialize();

}
