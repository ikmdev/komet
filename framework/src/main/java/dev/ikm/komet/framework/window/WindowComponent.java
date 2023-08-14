/*
 * Copyright © 2015 Integrated Knowledge Management (support@ikm.dev)
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
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
