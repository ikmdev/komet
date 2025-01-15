package dev.ikm.komet.layout.window;

import dev.ikm.komet.layout.KlFactory;
import dev.ikm.komet.layout.KlGadget;
import dev.ikm.komet.preferences.KometPreferences;
import dev.ikm.tinkar.common.service.PluggableService;
import javafx.stage.Window;
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
     * Creates an instance of a {@link KlWindow} using the provided whiteboard factory.
     * This method leverages the supplied {@link KlWhiteBoardFactory} to configure and
     * construct the window, encapsulating its layout and behavior.
     *
     * @param whiteBoardFactory The {@link KlWhiteBoardFactory} responsible for providing the widgets
     *                      and configurations to construct the {@link KlWindow}.
     * @return A new instance of {@link KlWindow} constructed using the given widget factory.
     */
    KlWindow create(KlWhiteBoardFactory whiteBoardFactory);

    /**
     * Creates a list of actions for opening new windows using the specified whiteboard factories.
     * The generated actions allow users to instantiate new windows with configurations
     * provided by the given factories.
     *
     * @param whiteBoardFactories Varargs parameter of {@link KlWhiteBoardFactory} instances,
     *                            each responsible for generating a specific type of whiteboard
     *                            to be included in a new window.
     * @return An {@code ImmutableList<Action>} containing actions for creating new windows
     *         with whiteboards from the specified factories.
     */
    ImmutableList<Action> createNewWindowActions(KlWhiteBoardFactory... whiteBoardFactories);

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
     * Creates and returns an immutable list of actions that can be used to create new windows
     * from the discovered {@link KlWhiteBoardFactory} providers.
     *
     * @return An {@code ImmutableList<Action>} representing actions for restoring windows.
     */
    ImmutableList<Action> createRestoreWindowActions();

    default ImmutableList<Action> createNewWindowActionsByDiscovery() {
        ServiceLoader<KlWhiteBoardFactory> serviceLoader = PluggableService.load(KlWhiteBoardFactory.class);
        MutableList<Action> actions = Lists.mutable.empty();
        serviceLoader.forEach(whiteBoardFactory ->
                actions.add(new Action("New " + whiteBoardFactory.name(), event -> {
                    KlWindow window = this.create(whiteBoardFactory);
                    window.show();
                })));
        return actions.toImmutable();
    }


}
