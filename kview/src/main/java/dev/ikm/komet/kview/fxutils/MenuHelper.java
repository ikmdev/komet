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
package dev.ikm.komet.kview.fxutils;

import javafx.event.ActionEvent;
import javafx.event.Event;
import javafx.event.EventHandler;
import javafx.geometry.Bounds;
import javafx.geometry.Point2D;
import javafx.geometry.Side;
import javafx.scene.Node;
import javafx.scene.control.MenuItem;
import javafx.scene.control.SeparatorMenuItem;
import javafx.scene.input.ContextMenuEvent;
import javafx.scene.input.PickResult;

/**
 * A JavaFX Menu Helper class to create Menu items. Class is created as a singleton to allow Mocking frameworks to easily test.
 */
public class MenuHelper {
    public static final String SEPARATOR = "SEPARATOR";
    private static MenuHelper INSTANCE = null;
    private MenuHelper() {

    }

    public static MenuHelper getInstance() {
        if (INSTANCE == null) {
            synchronized (MenuHelper.class) {
                if (INSTANCE == null) {
                    INSTANCE = new MenuHelper();
                }
            }
        }
        return INSTANCE;
    }

    public MenuItem createMenuOption(String name, EventHandler<ActionEvent> action){
        return createMenuOption(name, true, null, action, null);
    }
    public MenuItem createMenuOption(String name, String[] styleClasses){
        return createMenuOption(name, true, styleClasses, null, null);
    }

    public MenuItem createMenuOption(String name, boolean active, String[] styleClasses, EventHandler<ActionEvent> action,
                                     Node menuItemGraphic){
        MenuItem menuItem = null;

        // Add a separator
        if (SEPARATOR.equals(name) || name == null) {
            menuItem = new SeparatorMenuItem();
        } else {
            menuItem = new MenuItem(name);
        }

        if (action != null) menuItem.setOnAction(action);
        menuItem.setDisable(!active);

        if (styleClasses != null && styleClasses.length > 0) {
            menuItem.getStyleClass().addAll(styleClasses);
        }

        if (menuItemGraphic != null) {
            menuItem.setGraphic(menuItemGraphic);
        }

        return menuItem;

    }

    /**
     * A way to mimic a context menu right click operation.
     * @param actionEvent Typical button action event.
     * @param side - what side of the button to display context menu
     * @param xOffset - offset x to display context menu
     * @param yOffset - offset y to display context menu
     */
    public static void fireContextMenuEvent(ActionEvent actionEvent, Side side, double xOffset, double yOffset) {
        Node node = (Node) actionEvent.getSource();
        Bounds bounds = node.getBoundsInLocal();
        double x = bounds.getMinX();
        double y = bounds.getMaxY(); // bottom of button
        if (side == Side.BOTTOM) {
            x = bounds.getMinX();
            y = bounds.getMaxY();
        } else if (side == Side.LEFT) {
            x = bounds.getMinX();
            y = bounds.getMaxY();
        } else if (side == Side.TOP) {
            x = bounds.getMinX();
            y = bounds.getMinY();
        } else if (side == Side.RIGHT) {
            x = bounds.getMaxX();
            y = bounds.getMinY();
        }
        Point2D location = node.localToScreen(x, y);
        double scrnX = location.getX();
        double scrnY = location.getY();
        Event event = new ContextMenuEvent(ContextMenuEvent.CONTEXT_MENU_REQUESTED, x, y, scrnX+xOffset, scrnY+yOffset, false, new PickResult(node, x, y));
        Event.fireEvent(node, event);

    }
}
