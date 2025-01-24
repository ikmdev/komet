package dev.ikm.komet.layout.window;

import dev.ikm.komet.layout.KlFactory;
import dev.ikm.komet.layout.preferences.KlPreferencesFactory;
import dev.ikm.komet.preferences.KometPreferences;
import dev.ikm.tinkar.common.service.PluggableService;
import org.controlsfx.control.action.Action;
import org.eclipse.collections.api.factory.Lists;
import org.eclipse.collections.api.list.ImmutableList;
import org.eclipse.collections.api.list.MutableList;

import java.util.ServiceLoader;

/**
 * Factory interface for creating instances of {@link KlWindow}.
 * Extends the {@link KlFactory} interface to provide specific
 * functionality for producing top-level windows.
 */
public interface KlWindowFactory<W> extends KlFactory<KlWindow<W>> {
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
     * Retrieves the type of window that this factory is designed to create.
     *
     * @return The {@link WindowType} associated with the factory, specifying
     * the type of window (e.g., JOURNAL or JAVAFX) that can be produced.
     */
    WindowType factoryWindowType();

    /**
     * Creates a new instance of {@link KlWindow} using the provided preferences factory.
     *
     * @param preferencesFactory The {@link KlPreferencesFactory} used to supply preferences for the creation
     *                           of the {@link KlWindow}. These preferences define the state and configuration
     *                           of the window to be created.
     * @return A newly created instance of {@link KlWindow} configured with the specified preferences.
     */
    KlWindow<W> create(KlPreferencesFactory preferencesFactory);

    /**
     * Creates a list of actions for opening new windows using the specified whiteboard factories.
     * The generated actions allow users to instantiate new windows with configurations
     * provided by the given factories.
     *
     * @param windowPaneFactories Varargs parameter of {@link KlWindowPaneFactory} instances,
     *                            each responsible for generating a specific type of whiteboard
     *                            to be included in a new window.
     * @return An {@code ImmutableList<Action>} containing actions for creating new windows
     *         with whiteboards from the specified factories.
     */
    ImmutableList<Action> createNewWindowActions(KlPreferencesFactory preferencesFactory,
                                                 KlWindowPaneFactory... windowPaneFactories);

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
    KlWindow<W> restore(KometPreferences preferences);

    /**
     * Creates and returns an immutable list of actions that can be used to create new windows
     * from the discovered {@link KlWindowPaneFactory} providers.
     *
     * @return An {@code ImmutableList<Action>} representing actions for restoring windows.
     */
    ImmutableList<Action> createRestoreWindowActions();

    /**
     * Creates a list of actions to open new windows by discovering available {@link KlWindowPaneFactory}
     * implementations. Each action, when triggered, launches a new {@link KlWindow} instance
     * with configurations provided by the respective {@link KlWindowPaneFactory}.
     *
     * @param preferencesFactory A {@link KlPreferencesFactory} instance used to supply preferences
     *                           for the creation of new windows. These preferences define the state
     *                           and configuration of the windows to be created.
     *
     * @return An {@code ImmutableList<Action>} containing actions for creating new windows,
     *         each associated with a discovered {@link KlWindowPaneFactory}.
     */
    default ImmutableList<Action> createNewWindowActionsByDiscovery(KlPreferencesFactory preferencesFactory) {
        ServiceLoader<KlWindowPaneFactory> serviceLoader = PluggableService.load(KlWindowPaneFactory.class);
        MutableList<Action> actions = Lists.mutable.empty();
        serviceLoader.forEach(whiteBoardFactory ->
                actions.add(new Action("New " + whiteBoardFactory.name(), event -> {
                    KlWindow window = this.create(preferencesFactory);
                    window.show();
                })));
        return actions.toImmutable();
    }

}
