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
package dev.ikm.komet.amplify.commons;

import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.scene.control.MenuItem;
import javafx.scene.control.SeparatorMenuItem;

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
        return createMenuOption(name, true, null, action);
    }
    public MenuItem createMenuOption(String name, String[] styleClasses){
        return createMenuOption(name, true, styleClasses, null);
    }

    public MenuItem createMenuOption(String name, boolean active, String[] styleClasses, EventHandler<ActionEvent> action){
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
        return menuItem;

    }
}
