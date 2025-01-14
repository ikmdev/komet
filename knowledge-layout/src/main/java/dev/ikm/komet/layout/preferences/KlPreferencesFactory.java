package dev.ikm.komet.layout.preferences;

import dev.ikm.komet.layout.window.KlWindow;
import dev.ikm.komet.layout.window.KlWindowFactory;
import dev.ikm.komet.preferences.KometPreferences;
import dev.ikm.komet.preferences.PreferencesService;
import javafx.scene.Scene;

import java.util.function.Supplier;
import java.util.prefs.BackingStoreException;

/**
 * The KlPreferencesFactory interface provides a method for creating instances of
 * KometPreferences.
 */
public class KlPreferencesFactory implements Supplier<KometPreferences>{

    private final KometPreferences parentPreferences;
    private final Class implementationClass;

    /**
     * Constructs a new {@code KlPreferencesFactory} instance.
     *
     * @param parentPreferences the parent {@code KometPreferences} node in which the
     *                          preferences for the factory will be managed
     * @param implementationClass the implementation class for which this factory is created
     */
    private KlPreferencesFactory(KometPreferences parentPreferences, Class implementationClass) {
        this.parentPreferences = parentPreferences;
        this.implementationClass = implementationClass;
    }

    /**
     * Provides a new unique instance of {@code KometPreferences} associated with the specified
     * implementation class within the parent preferences node.
     *
     * @return a {@code KometPreferences} instance uniquely associated with the implementation class
     *         and created within the parent preferences node
     */
    @Override
    public KometPreferences get() {
        return createSequentiallyUniquePreferences(parentPreferences, implementationClass);
    }

    /**
     * Creates a new {@code KlPreferencesFactory} instance for managing preferences
     * related to a specific implementation class within the provided parent preferences node.
     *
     * @param parentPreferences the parent {@code KometPreferences} node in which the
     *                          new {@code KlPreferencesFactory} will be created
     * @param implementationClass the class for which the preferences factory is to be created
     * @return a {@code KlPreferencesFactory} instance associated with the specified
     *         implementation class
     */
    public static KlPreferencesFactory createFactory(KometPreferences parentPreferences, Class implementationClass) {
        return new KlPreferencesFactory(parentPreferences, implementationClass);
    }
    /**
     * Creates a new {@code KometPreferences} instance for managing preferences related to a specific
     * scene configuration. The preference node sequentially named using the {@code Scene.class}
     * type and is created within the specified parent preferences node.
     *
     * @param parentPreferences the parent {@code KometPreferences} node under which the new scene
     *                          preferences node will be created
     * @return a {@code KometPreferences} instance uniquely associated with the {@code Scene.class}
     */
    private static KometPreferences createScenePreferences(KometPreferences parentPreferences) {
        return createSequentiallyUniquePreferences(parentPreferences, Scene.class);
    }

    /**
     * Creates a {@code Supplier} that provides a new {@code KometPreferences} instance
     * for managing preferences related to a specific scene configuration. The preferences
     * are created under the specified {@code parentPreferences} node.
     *
     * @param parentPreferences the parent {@code KometPreferences} node under which the
     *                          preferences for the scene will be managed
     * @return a {@code Supplier} that supplies a {@code KometPreferences} instance uniquely
     *         associated with the scene configuration
     */
    public static Supplier<KometPreferences> createScenePreferencesSupplier(KometPreferences parentPreferences) {
        return () -> createScenePreferences(parentPreferences);
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
        return createSequentiallyUniquePreferences(windowPreferencesRoot, implementationClass);
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
