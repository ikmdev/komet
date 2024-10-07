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
import javafx.scene.control.ContextMenu;
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

    public static final int NAME = 0;
    public static final int ENABLED = 1;
    public static final int STYLES = 2;
    public static final int ACTION = 3;
    public static final int GRAPHIC = 4;

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

    /**
     * This method takes list of menuItems in 2D array form.
     * The 2nd array can have the following items in sequence: public NAME = 0; ENABLED = 1; STYLES = 2; ACTION = 3; GRAPHIC = 4;
     * @param menuItems
     * @return contextMenu  returns the context menu with the list of menu items.
     */

    public ContextMenu createContextMenuWithMenuItems(Object [][] menuItems){
        ContextMenu contextMenu = new ContextMenu();
        contextMenu.getStyleClass().add("kview-context-menu");
        contextMenu.setHideOnEscape(true);
        contextMenu.setAutoHide(true);
        contextMenu.setConsumeAutoHidingEvents(true);
        for(Object [] menuItemObj : menuItems){
            if (SEPARATOR.equals(menuItemObj[NAME])){
                contextMenu.getItems().add(new SeparatorMenuItem());
                continue;
            }
            // uses a default action if one is not given.
            EventHandler<ActionEvent> menuItemAction = switch (menuItemObj[ACTION]) {
                case null ->  null;
                case EventHandler  eventHandler -> eventHandler;
                default -> null;
            };
            // Create a menu item.
            MenuItem menuItem = createMenuOption(
                    String.valueOf(menuItemObj[NAME]),                           /* name */
                    Boolean.parseBoolean(String.valueOf(menuItemObj[ENABLED])),  /* enabled */
                    (String[]) menuItemObj[STYLES],                              /* styling */
                    menuItemAction,                                              /* action when selected */
                    (Node) menuItemObj[GRAPHIC]                                  /* optional graphic */
            );
            contextMenu.getItems().add(menuItem);

        }
        return contextMenu;
    }
}
