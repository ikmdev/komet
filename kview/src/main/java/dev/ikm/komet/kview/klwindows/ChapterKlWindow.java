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
package dev.ikm.komet.kview.klwindows;

import dev.ikm.komet.layout.window.KlWindow;
import javafx.scene.Node;
import javafx.scene.Scene;

/**
 * The {@code ChapterKlWindow} interface extends the {@link KlWindow} interface
 * and provides a generic contract for Komet Layout windows that manage
 * a root pane of type {@code T}.
 *
 * @param <T> the type of the root pane, must extend {@link Node}.
 */
// TODO: This interface is purely a placeholder and temporarily replacement for the original KlWindow interface.
//       It can be removed once the original KlWindow interface will be updated.
public interface ChapterKlWindow<T extends Node> extends KlWindow {

    /**
     * Returns the root pane for this window.
     * <p>
     * The root pane serves as the main container for all UI components within
     * this window. It must be a subclass of {@link Node}.
     *
     * @return the root pane of this window, or {@code null} if not set
     */
    T getRootPane();

    /**
     * Provides the {@link Scene} associated with the root pane.
     * <p>
     * This default implementation retrieves the scene from the {@code root}
     * node returned by {@link #getRootPane()}.
     * If the root pane is {@code null}, this method returns {@code null}.
     *
     * @return the {@link Scene} associated with the root pane,
     * or {@code null} if the root pane is {@code null}
     */
    @Override
    default Scene scene() {
        final T root = getRootPane();
        if (root != null) {
            return root.getScene();
        }
        return null;
    }
}
