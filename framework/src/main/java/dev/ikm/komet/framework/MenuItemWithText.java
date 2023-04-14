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
