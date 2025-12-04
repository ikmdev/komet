package dev.ikm.komet.kview.mvvm.view.journal;

import javafx.collections.ObservableList;
import javafx.scene.Node;
import javafx.scene.layout.Pane;

/**
 * VerticallyFilledPane works like Pane, it doesn't do any positioning of its children, only resizing.
 * Use case: The algorithm for the Tray Panes of Komet relies on the use of a layout container like Pane and its characteristic
 * of allowing children to be positioned outside its bounds (because it doesn't do any positioning). This class goes a step
 * further and provides other useful features for the Tray Pane that JavaFX Pane does not have.
 *
 * The difference from Pane is that when resizing its children it sets their height so that they occupy the whole height of the
 * VerticallyFilledPane. The width of each child is set to their preferred width.
 * Another thing VerticallyFilledPane does differently from Pane is to compute its min height and set it to be the maximum
 * min height of all of its children.
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

    @Override
    protected double computeMinHeight(double width) {
        if (getChildren().isEmpty()) {
            return 0;
        }

        double minHeight = 0;
        for (Node child : getChildren()) {
            minHeight = Math.max(child.minHeight(width), minHeight);
        }
        return minHeight;
    }
}
