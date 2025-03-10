package dev.ikm.komet.layout.component;

import dev.ikm.komet.layout.KlWidget;
import javafx.scene.layout.Pane;

/**
 * The {@code KlPane} interface represents a widget that integrates with a JavaFX {@code Pane}.
 * It provides access to the underlying JavaFX {@code Pane} used for layout and display purposes.
 * This interface is typically extended to create more specialized pane implementations.
 *
 * @param <P> the type of JavaFX {@code Pane} associated with the implementation of this interface
 */
public interface KlArea<P extends Pane> extends KlWidget<P> {

    /**
     * Returns the JavaFX {@code Pane} associated with this version pane.
     *
     * Exposes the layout panes which need to expose the children list as
     * public so that users of the subclass can freely add/remove children.
     *
     * @return the JavaFX {@code Pane} used to display content
     */
    default Pane pane() {
        return klWidget();
    }
}
