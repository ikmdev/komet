package dev.ikm.komet.framework.dnd;

import javafx.event.EventHandler;
import javafx.scene.input.DragEvent;

/**
 * {@link DragDoneEventHandler}
 *
 * @author <a href="mailto:daniel.armbrust.list@gmail.com">Dan Armbrust</a>
 */
public class DragDoneEventHandler implements EventHandler<DragEvent> {

    /**
     * @param event
     * @see javafx.event.EventHandler#handle(javafx.event.Event)
     */
    @Override
    public void handle(DragEvent event) {
        DragRegistry.dragComplete();
    }
}
