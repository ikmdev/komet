package dev.ikm.komet.framework.dnd;

import javafx.scene.input.DragEvent;
import javafx.scene.input.Dragboard;
import javafx.scene.input.MouseEvent;
import javafx.scene.input.TransferMode;
import javafx.scene.layout.Region;
import dev.ikm.komet.framework.Dialogs;
import dev.ikm.tinkar.terms.EntityFacade;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.function.Consumer;
import java.util.function.Predicate;
import java.util.function.Supplier;

public class DragHelper {

    protected static final Logger LOG = LoggerFactory.getLogger(DragHelper.class);

    private final Region region;
    private final Supplier<EntityFacade> objectToDragSupplier;
    private final Predicate<MouseEvent> acceptDrag;
    private boolean dragging = false;
    private Consumer<Boolean> dragNotifier = this::noOp;

    public DragHelper(Region region,
                      Supplier<EntityFacade> objectToDragSupplier,
                      Predicate<MouseEvent> acceptDrag, Consumer<Boolean> dragNotifier) {
        this(region,
                objectToDragSupplier,
                acceptDrag);
        this.dragNotifier = dragNotifier;
    }
    public DragHelper(Region region,
                      Supplier<EntityFacade> objectToDragSupplier,
                      Predicate<MouseEvent> acceptDrag) {
        this.region = region;
        this.objectToDragSupplier = objectToDragSupplier;
        this.acceptDrag = acceptDrag;
        region.setOnDragDetected(this::handleDragDetected);
        region.setOnDragDone(this::handleDragDone);
    }

    private void handleDragDetected(MouseEvent event) {
        LOG.atDebug().log("Drag detected: " + event);
        if (acceptDrag.test(event)) {
            this.dragging = true;
            this.dragNotifier.accept(this.dragging);
            Dragboard db = region.startDragAndDrop(TransferMode.COPY);
            KometClipboard content = new KometClipboard(objectToDragSupplier.get());

            DragImageMaker dragImageMaker = new DragImageMaker(region);
            db.setDragView(dragImageMaker.getDragImage());
            db.setContent(content);

            event.consume();
        }
    }

    private void handleDragDone(DragEvent event) {
        LOG.debug("Drag done: " + event);
        this.dragging = false;
        this.dragNotifier.accept(this.dragging);
        if (event.getAcceptedTransferMode() == TransferMode.MOVE) {
            Dialogs.showInformationDialog("Unsupported transfer mode", "TransferMode.MOVE is not supported");
        }
        event.consume();
    }

    private void noOp(boolean value) {
        // noop.
    }
    public boolean isDragging() {
        return dragging;
    }
}
