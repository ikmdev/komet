package dev.ikm.komet.framework.dnd;

import javafx.geometry.Insets;
import javafx.scene.input.DataFormat;
import javafx.scene.input.DragEvent;
import javafx.scene.input.Dragboard;
import javafx.scene.input.TransferMode;
import javafx.scene.layout.Background;
import javafx.scene.layout.BackgroundFill;
import javafx.scene.layout.CornerRadii;
import javafx.scene.layout.Region;
import javafx.scene.paint.Color;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Set;
import java.util.function.BooleanSupplier;
import java.util.function.Consumer;
import java.util.function.Predicate;

public class DropHelper {

    protected static final Logger LOG = LoggerFactory.getLogger(DropHelper.class);
    private final Region region;
    private final Consumer<Dragboard> draggedObjectAcceptor;
    private final Predicate<DragEvent> acceptDrop;
    private final BooleanSupplier dragInProgress;
    private Background originalBackground;
    private TransferMode[] transferMode = null;

    public DropHelper(Region region,
                      Consumer<Dragboard> draggedObjectAcceptor,
                      Predicate<DragEvent> acceptDrop,
                      BooleanSupplier dragInProgress) {
        this.region = region;
        this.draggedObjectAcceptor = draggedObjectAcceptor;
        this.acceptDrop = acceptDrop;
        this.dragInProgress = dragInProgress;
        region.setOnDragOver(this::handleDragOver);
        region.setOnDragEntered(this::handleDragEntered);
        region.setOnDragExited(this::handleDragExited);
        region.setOnDragDropped(this::handleDragDropped);
    }

    private void handleDragOver(DragEvent event) {
        // LOG.info("Dragging over: " + event );
        if (this.dragInProgress.getAsBoolean()) {
            event.consume();
            return;
        }
        if (!this.acceptDrop.test(event)) {
            event.consume();
            return;
        }
        if (this.transferMode != null) {
            event.acceptTransferModes(TransferMode.COPY_OR_MOVE);
            event.consume();
        }
    }

    private void handleDragEntered(DragEvent event) {
        LOG.debug("Dragging entered: " + event);
        if (this.dragInProgress.getAsBoolean()) {
            return;
        }
        if (!this.acceptDrop.test(event)) {
            return;
        }
        this.originalBackground = region.getBackground();
        Color backgroundColor;
        Set<DataFormat> contentTypes = event.getDragboard().getContentTypes();

        if (KometClipboard.containsAny(contentTypes, KometClipboard.CONCEPT_TYPES)) {
            backgroundColor = Color.AQUA;
            this.transferMode = TransferMode.COPY_OR_MOVE;
        } else if (KometClipboard.containsAny(contentTypes, KometClipboard.SEMANTIC_TYPES)) {
            backgroundColor = Color.OLIVEDRAB;
            this.transferMode = TransferMode.COPY_OR_MOVE;
        } else if (KometClipboard.containsAny(contentTypes, KometClipboard.PATTERN_TYPES)) {
            backgroundColor = Color.BLUEVIOLET;
            this.transferMode = TransferMode.COPY_OR_MOVE;
        } else {
            backgroundColor = Color.RED;
            this.transferMode = null;
        }
        BackgroundFill fill = new BackgroundFill(backgroundColor, CornerRadii.EMPTY, Insets.EMPTY);
        region.setBackground(new Background(fill));

        event.consume();
    }

    private void handleDragExited(DragEvent event) {
        LOG.debug("Dragging exited: " + event);
        if (this.dragInProgress.getAsBoolean()) {
            return;
        }
        region.setBackground(originalBackground);
        this.transferMode = null;
        event.consume();
    }

    private void handleDragDropped(DragEvent event) {
        LOG.debug("Dragging dropped: " + event);
        if (this.dragInProgress.getAsBoolean()) {
            return;
        }
        if (!this.acceptDrop.test(event)) {
            return;
        }
        if(this.transferMode != null){
            Dragboard db = event.getDragboard();
            this.draggedObjectAcceptor.accept(db);
            event.setDropCompleted(true);
        }
        region.setBackground(originalBackground);
        event.consume();
    }
}
