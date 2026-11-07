/*
 * Copyright © 2026 Knowledge Graphlet / IKE Network
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     https://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package dev.ikm.komet.layout_engine.window;

import javafx.event.Event;
import javafx.event.EventType;
import javafx.scene.Node;
import javafx.scene.Scene;
import javafx.stage.Stage;

/**
 * Asks the workspace a node sits in to show the window holding it: scroll the journal to the
 * window, bring it to the front, flash its outline and give it the focus. A card fires this at
 * its own node when a gesture finds it already open ("Open in Journal" on a document a card
 * already shows), so the person sees where it is; raising the window in the stacking order alone
 * changes nothing they can see when the window is scrolled out of view.
 *
 * <p>The event bubbles from the node to the workspace, so the firing code never sees the
 * workspace. A node in a window of its own, outside every workspace, has its stage raised and
 * nothing else.
 */
public class RevealWindowEvent extends Event {

    /** The one type of this event. */
    public static final EventType<RevealWindowEvent> REVEAL_WINDOW =
            new EventType<>(Event.ANY, "REVEAL_WINDOW");

    /** Constructs the event; fire it with {@link #reveal(Node)}. */
    public RevealWindowEvent() {
        super(REVEAL_WINDOW);
    }

    /**
     * Shows the window holding a node: raises its stage, when that is another journal's window,
     * and asks its workspace to scroll to, raise, flash and focus it.
     *
     * @param node a node in the window to show
     */
    public static void reveal(Node node) {
        Scene scene = node.getScene();
        if (scene != null && scene.getWindow() instanceof Stage stage) {
            if (stage.isIconified()) {
                stage.setIconified(false);
            }
            stage.toFront();
        }
        node.fireEvent(new RevealWindowEvent());
    }
}
