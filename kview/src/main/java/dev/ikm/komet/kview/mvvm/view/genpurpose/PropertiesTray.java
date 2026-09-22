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
package dev.ikm.komet.kview.mvvm.view.genpurpose;

import dev.ikm.komet.kview.fxutils.SlideOutTrayHelper;
import javafx.scene.Node;
import javafx.scene.layout.Pane;
import javafx.scene.layout.Region;

import static dev.ikm.komet.kview.fxutils.ViewportHelper.clipChildren;
import static dev.ikm.komet.layout_engine.window.DraggableSupport.addDraggableNodes;
import static dev.ikm.komet.layout_engine.window.DraggableSupport.removeDraggableNodes;

/**
 * The slide-out tray on a general-purpose KL window's right side that holds the properties
 * panel: it slides out of the window's edge when opened and back in when closed. While it is
 * open the panel's drag handle also drags the window, like the toolbar does.
 *
 * <p>The tray only moves. What the panel shows, the toolbar's Properties toggle and the window's
 * height stay with the caller.
 */
public final class PropertiesTray {

    private final Pane window;
    private final Pane trayPane;
    private final Node dragHandle;

    /**
     * @param window     the window's root pane, which the tray slides out of
     * @param trayPane   the (empty) tray pane in the window's view
     * @param content    the properties panel to hold in the tray
     * @param dragHandle the part of the panel that drags the window while the tray is open
     */
    public PropertiesTray(Pane window, Pane trayPane, Region content, Node dragHandle) {
        this.window = window;
        this.trayPane = trayPane;
        this.dragHandle = dragHandle;
        setContent(content);
    }

    /** Puts the content into the tray, starting out closed. */
    private void setContent(Region content) {
        double width = content.getWidth();
        content.setLayoutX(width);
        content.getStyleClass().add("slideout-tray-pane");

        trayPane.getChildren().add(content);
        clipChildren(trayPane, 0);
        content.setLayoutX(-width);
        trayPane.setMaxWidth(0);

        // The tray takes the height the window gives it and must not ask for a height of its own.
        // Left to report one, it ratchets the window: the tray is a Pane, so its preferred height is
        // its content's, and the content's preferred height is bound to the tray's height just below
        // — the tray would go on asking for whatever height it already had, and the window could
        // never shrink back once its sections did (komet-desktop#159).
        trayPane.setMinHeight(0);
        trayPane.setPrefHeight(0);

        // The content fills the tray's height, so that when the window resizes the content in the
        // tray stays aligned with the details view.
        content.prefHeightProperty().bind(trayPane.heightProperty());
    }

    /** Slides the tray out, if it isn't already. */
    public void open() {
        if (SlideOutTrayHelper.isClosed(trayPane)) {
            SlideOutTrayHelper.slideOut(trayPane, window);
        }
        addDraggableNodes(window, dragHandle);
    }

    /** Slides the tray back in, if it isn't already. */
    public void close() {
        if (SlideOutTrayHelper.isOpen(trayPane)) {
            SlideOutTrayHelper.slideIn(trayPane, window);
        }
        removeDraggableNodes(window, dragHandle);
    }

    public boolean isOpen() {
        return SlideOutTrayHelper.isOpen(trayPane);
    }
}
