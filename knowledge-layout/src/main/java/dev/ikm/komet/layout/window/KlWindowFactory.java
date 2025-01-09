package dev.ikm.komet.layout.window;

import dev.ikm.komet.layout.KlFactory;
import dev.ikm.komet.preferences.KometPreferences;
import javafx.scene.control.MenuItem;
import org.eclipse.collections.api.list.ImmutableList;

/**
 * Factory interface for creating instances of {@link KlWindow}.
 * Extends the {@link KlFactory} interface to provide specific
 * functionality for producing top-level windows.
 */
public interface KlWindowFactory extends KlFactory {
    /**
     * Represents the types of windows that can be created in the Komet application.
     * This enumeration is part of the {@link KlWindowFactory} and categorizes the
     * two specific types of windows supported by the factory:
     *
     * - JOURNAL: Represents a window that is focused on journal views and interactions.
     * - JAVAFX: Represents a top-level JavaFx window.
     */
    enum WindowType {
        JOURNAL,
        JAVAFX
    }

    /**
     * Creates and returns an immutable list of menu items that will create new windows
     * corresponding to the different types of windows and scenes supported by this
     * factory.
     *
     * @return An {@code ImmutableList<MenuItem>} containing the constructed menu items.
     */
    ImmutableList<MenuItem> createMenuItems();
    /**
     * Creates a new instance of a KlWindow using the specified preferences.
     *
     * @param preferences The KometPreferences object that specifies the configuration
     *                    settings to initialize the KlWindow. If the preferences are
     *                    uninitialized, default preferences will be used and then
     *                    written to the preferences.
     * @return A new instance of KlWindow configured with the provided preferences.
     */
    KlWindow create(KometPreferences preferences);

    /**
     * Creates a new instance of a KlWindow using the specified preferences and scene factory.
     *
     * @param preferences The KometPreferences object that specifies where two write configuration
     *                    the default configuration preferences.
     * @param sceneFactory The KlSceneFactory object used to produce the scene
     *                     for the KlWindow. It provides the mechanism to create
     *                     and configure the scene that will be hosted within the window.
     * @return A new instance of KlWindow configured with the specified preferences and scene factory.
     */
    KlWindow create(KometPreferences preferences, KlSceneFactory sceneFactory);

    /**
     * Retrieves the type of window that this factory is designed to create.
     *
     * @return The {@link WindowType} associated with the factory, specifying
     * the type of window (e.g., JOURNAL or JAVAFX) that can be produced.
     */
    WindowType factoryWindowType();
}
