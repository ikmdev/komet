package dev.ikm.komet.framework.window;

import javafx.scene.Node;
import org.eclipse.collections.api.list.ImmutableList;
import dev.ikm.komet.framework.view.ObservableViewNoOverride;
import dev.ikm.komet.preferences.KometPreferences;

/**
 * WindowComponents must have a create method:
 * <br>public static WindowComponent create(ObservableViewNoOverride windowView, KometPreferences nodePreferences);
 * <br> so that they can be constructed with default values saved to preferences, and reconstructed from preferences.
 * Two scenarios:
 * <p>
 * 1. First creation of a WindowComponent
 * <p>Look for an absent INITIALIZED key, and then set defaults accordingly.
 * <p> 2. Restore a WindowComponent from its preferences.
 * <p>
 * If INITIALIZED key is present, read configuration from preferences and set fields accordingly.
 * </p>
 */
public interface WindowComponent {
    ObservableViewNoOverride windowView();

    KometPreferences nodePreferences();

    ImmutableList<WindowComponent> children();

    void saveConfiguration();

    /**
     * @return The node to be displayed
     */
    Node getNode();

    /**
     * Class that has a static reconstructor method:
     *
     * @return class that has a static @Reconstructor method to recreate the object with its saved state.
     * @Reconstructor public static Object create(ObservableViewNoOverride windowView, KometPreferences nodePreferences)
     */
    Class factoryClass();

    enum WindowComponentKeys {
        INITIALIZED,
        FACTORY_CLASS,
        CHILDREN
    }
}
