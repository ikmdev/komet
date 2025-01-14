package dev.ikm.komet.layout.window;

import dev.ikm.komet.layout.KlFactory;
import dev.ikm.komet.layout.KlGadget;
import dev.ikm.komet.layout.KlWidget;
import dev.ikm.komet.layout.KlWidgetFactory;
import dev.ikm.komet.layout.preferences.KlPreferencesFactory;
import dev.ikm.komet.preferences.KometPreferences;
import javafx.stage.Window;
import org.controlsfx.control.action.Action;
import org.eclipse.collections.api.list.ImmutableList;

/**
 * Factory interface for creating instances of {@link KlWindow}.
 * Extends the {@link KlFactory} interface to provide specific
 * functionality for producing top-level windows.
 */
public interface KlWindowFactory extends KlFactory<KlGadget<Window>> {
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

    String PREFERENCES_ROOT = "KlWindowPreferences";

    /**
     * Retrieves the type of window that this factory is designed to create.
     *
     * @return The {@link WindowType} associated with the factory, specifying
     * the type of window (e.g., JOURNAL or JAVAFX) that can be produced.
     */
    WindowType factoryWindowType();
    /**
     * Creates and returns an immutable list of menu items that will create new windows
     * corresponding to the different types of windows and scenes supported by this
     * factory.
     *
     * @return An {@code ImmutableList<MenuItem>} containing the constructed menu items.
     */
    ImmutableList<Action> createNewWindowActions();

    /**
     * Creates an instance of a {@link KlWindow} using the provided widget factory.
     * This method leverages the supplied {@link KlWidgetFactory} to configure and
     * construct the window, encapsulating its layout and behavior.
     *
     * @param widgetFactory The {@link KlWidgetFactory} responsible for providing the widgets
     *                      and configurations to construct the {@link KlWindow}.
     * @return A new instance of {@link KlWindow} constructed using the given widget factory.
     */
    KlWindow create(KlWidgetFactory widgetFactory);

    /**
     * Creates and returns an immutable list of actions for creating new windows within
     * the application. These actions are constructed using the specified preferences
     * factory and scene factory, enabling configuration and management of window
     * properties and their associated scenes.
     *
     * @param sceneFactory       The {@link KlSceneFactory} responsible for generating
     *                           scenes to be associated with the created windows.
     * @return An {@code ImmutableList<Action>} representing the actions
     *         that create and configure new windows.
     */
    ImmutableList<Action> createNewWindowActions(KlSceneFactory sceneFactory);

    /**
     * Restores a previously configured instance of {@link KlWindow} using the provided preferences.
     * This method allows the recreation of a window with its previous state and configuration
     * as specified in the supplied {@link KometPreferences}.
     *
     * @param preferences The {@link KometPreferences} object containing the saved configuration
     *                    settings for the window to be restored. These preferences determine
     *                    the state and properties of the restored {@link KlWindow}.
     * @return A restored {@link KlWindow} instance configured with the provided preferences.
     */
    @Override
    KlWindow restore(KometPreferences preferences);

    /**
     * Creates and returns an immutable list of actions that can be used to restore
     * previously saved and configured windows. These actions allow for the recreation
     * of window states based on stored preferences or configurations.
     *
     * @return An {@code ImmutableList<Action>} representing actions for restoring windows.
     */
    ImmutableList<Action> createRestoreWindowActions();

}
