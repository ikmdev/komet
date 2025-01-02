package dev.ikm.komet.layout.component;

import dev.ikm.komet.layout.KlWidget;
import javafx.scene.layout.Pane;

public interface KlPane extends KlWidget {

    /**
     * Returns the JavaFX {@code Pane} associated with this version pane.
     *
     * Exposes the layout panes which need to expose the children list as
     * public so that users of the subclass can freely add/remove children.
     *
     * @return the JavaFX {@code Pane} used to display content
     */
    default Pane pane() {
        return sceneGraphNode();
    }
}
