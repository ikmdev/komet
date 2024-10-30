package dev.ikm.komet.kview.mvvm.view.journal;

import javafx.collections.ObservableList;
import javafx.scene.Node;
import javafx.scene.layout.Pane;

/**
 * VerticallyFilledPane works like Pane, it doesn't do any positioning of its children, only resizing.
 * The difference from Pane is that when resizing its children it sets their height so that they occupy the whole height of the
 * VerticallyFilledPane. The width of each child is set to their preferred width.
 * Use case: this "Layout container" was created to be used in Tray Panes so that he content fills the Tray Pane.
 */
public class VerticallyFilledPane extends Pane {

    @Override
    protected void layoutChildren() {
        ObservableList<Node> children = getChildren();

        for (int i = 0, max = children.size(); i < max; i++) {
            final Node node = children.get(i);
            if (node.isResizable() && node.isManaged()) {
                double paneHeight = getHeight();
                double width = node.prefWidth(paneHeight);
                node.resize(width, paneHeight);
            }
        }
    }
}
