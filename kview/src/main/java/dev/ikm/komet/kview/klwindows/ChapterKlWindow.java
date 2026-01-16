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

import dev.ikm.komet.layout.window.KlJournalWindow;
import javafx.scene.Node;

import java.util.UUID;

/**
 * The {@code ChapterKlWindow} interface extends the {@link KlJournalWindow} interface
 * and provides additional functionality for window lifecycle management, including
 * initialization, close notifications, display events, and window identification.
 *
 * @param <T> the type parameter representing the root JavaFX node that this window contains
 *            and manages. Must extend {@link Node}.
 */
public interface ChapterKlWindow<T extends Node> extends KlJournalWindow<T> {

    /**
     * Sets a callback that is invoked when this window is closed.
     *
     * @param onClose A {@link Runnable} to be executed when the window is closed, or null if no action is required.
     */
    void setOnClose(Runnable onClose);

    /**
     * Called when the window is shown to the user. Implementations can use this method
     * to perform initialization that requires the window to be visible, such as
     * focusing on specific elements or performing layout operations.
     */
    void onShown();

    /**
     * Returns the unique identifier associated with this window's topic or content.
     * This UUID can be used to track or reference this window across the application.
     *
     * @return The UUID representing this window's topic
     */
    UUID getWindowTopic();

    /**
     * Returns the semantic type of this window, which categorizes its content and purpose
     * within the application. The window type determines how the window behaves and
     * what kind of content it displays within the knowledge management system.
     *
     * @return The {@link EntityKlWindowType} representing the semantic type of this window
     *         (e.g., "CONCEPT", "PATTERN", "LIDR", etc.)
     */
    EntityKlWindowType getWindowType();
}