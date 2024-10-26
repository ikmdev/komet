package dev.ikm.komet.kview.mvvm.view.journal;

import javafx.collections.ObservableList;
import javafx.scene.Node;
import javafx.scene.layout.Pane;

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
