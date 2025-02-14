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

/**
 * The {@code ChapterKlWindow} interface extends the {@link KlJournalWindow} interface
 * and provides additional methods for setting a callback that is invoked when the window is closed.
 *
 * @param <T> the type of the root pane, must extend {@link Node}.
 */
// TODO: This interface is purely a placeholder and temporarily replacement for the original KlJournalWindow interface.
//       It can be removed once the original KlJournalWindow interface will be updated.
public interface ChapterKlWindow<T extends Node> extends KlJournalWindow<T> {

    /**
     * Sets a callback that is invoked when this window is closed.
     *
     * @param onClose A {@link Runnable} to be executed on close, or null if no action is required.
     */
    void setOnClose(Runnable onClose);
}
