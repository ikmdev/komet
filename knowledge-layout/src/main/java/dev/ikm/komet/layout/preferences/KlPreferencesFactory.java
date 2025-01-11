package dev.ikm.komet.layout.preferences;

import dev.ikm.komet.layout.window.KlWindow;
import dev.ikm.komet.layout.window.KlWindowFactory;
import dev.ikm.komet.preferences.KometPreferences;
import dev.ikm.komet.preferences.PreferencesService;

import java.util.prefs.BackingStoreException;

/**
 * The KlPreferencesFactory interface provides a method for creating instances of
 * KometPreferences.
 */
public class KlPreferencesFactory {
    private final KometPreferences parentPreferences;

    private KlPreferencesFactory(KometPreferences parentPreferences) {
        this.parentPreferences = parentPreferences;
    }

    /**
     * Creates a new instance of {@code KlPreferencesFactory} using the provided parent preferences.
     *
     * @param parentPreferences the parent {@code KometPreferences} used to initialize the factory
     * @return a new instance of {@code KlPreferencesFactory}
     */
    public static KlPreferencesFactory createFactory(KometPreferences parentPreferences) {
        return new KlPreferencesFactory(parentPreferences);
    }
    public KometPreferences createWidgetPreferences(Class implementationClass) {
        return createWindowPreferences(parentPreferences, implementationClass);
    }

    /**
     * Creates a new unique instance of {@code KometPreferences} for the specified KlWindow implementation class.
     *
     * @param implementationClass the class of the {@code KlWindow} implementation for which
     *                            preferences are to be created
     * @return a {@code KometPreferences} instance associated with the specified implementation class
     */
    public static KometPreferences createWindowPreferences(Class<? extends KlWindow> implementationClass) {
        KometPreferences configurationPreferences = PreferencesService.configurationPreferences();
        KometPreferences windowPreferencesRoot = configurationPreferences.node(KlWindowFactory.PREFERENCES_ROOT);
        return createWindowPreferences(windowPreferencesRoot, implementationClass);
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
    public static KometPreferences createWindowPreferences(KometPreferences parentPreferences, Class implementationClass) {
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
