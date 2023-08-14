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
package dev.ikm.komet.framework;

import javafx.scene.Node;
import javafx.scene.control.MenuItem;

/**
 * The toString() method of MenuItem does not contain the text of the menu, which makes
 * it difficult to debug. This class includes the text of the menu item in the toString() method.
 */
public class MenuItemWithText extends MenuItem {
    public MenuItemWithText() {
    }

    public MenuItemWithText(String text) {
        super(text);
    }

    public MenuItemWithText(String text, Node graphic) {
        super(text, graphic);
    }

    @Override public String toString() {
        String superString = super.toString();
        int insertPosition = superString.indexOf('[');
        StringBuilder sbuf = new StringBuilder(superString.substring(0, insertPosition + 1));
        sbuf.append("text=");
        sbuf.append(getText());
        sbuf.append(", ");
        sbuf.append(superString.substring(insertPosition + 1));
        return sbuf.toString();
    }
}
