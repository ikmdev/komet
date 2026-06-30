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

    /**
     * Returns the transfer mode for a dragboard whose content is a droppable entity (concept, semantic, or
     * pattern), or {@code null} otherwise. Computed per event from the live dragboard so the accept decision
     * does not depend on {@code DRAG_ENTERED} firing — which is unreliable for a container target when the
     * cursor is over a child node. {@code DRAG_OVER} and {@code DRAG_DROPPED} bubble to the container reliably.
     *
     * @param dragboard the drag-and-drop content
     * @return {@link TransferMode#COPY_OR_MOVE} if the content is droppable, otherwise {@code null}
     */
    private static TransferMode[] droppableTransferMode(Dragboard dragboard) {
        Set<DataFormat> types = dragboard.getContentTypes();
        if (KometClipboard.containsAny(types, KometClipboard.CONCEPT_TYPES)
                || KometClipboard.containsAny(types, KometClipboard.SEMANTIC_TYPES)
                || KometClipboard.containsAny(types, KometClipboard.PATTERN_TYPES)) {
            return TransferMode.COPY_OR_MOVE;
        }
        return null;
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
        if (droppableTransferMode(event.getDragboard()) != null) {
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
        } else if (KometClipboard.containsAny(contentTypes, KometClipboard.SEMANTIC_TYPES)) {
            backgroundColor = Color.OLIVEDRAB;
        } else if (KometClipboard.containsAny(contentTypes, KometClipboard.PATTERN_TYPES)) {
            backgroundColor = Color.BLUEVIOLET;
        } else {
            backgroundColor = Color.RED;
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
        if (droppableTransferMode(event.getDragboard()) != null) {
            Dragboard db = event.getDragboard();
            this.draggedObjectAcceptor.accept(db);
            event.setDropCompleted(true);
        }
        region.setBackground(originalBackground);
        event.consume();
    }
}
