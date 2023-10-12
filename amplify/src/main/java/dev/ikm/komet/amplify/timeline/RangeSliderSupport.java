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
package dev.ikm.komet.amplify.timeline;

import javafx.beans.property.DoubleProperty;
import javafx.beans.property.SimpleDoubleProperty;
import javafx.scene.Node;
import javafx.scene.input.MouseEvent;

import java.util.function.Consumer;

public class RangeSliderSupport {
    private final DoubleProperty anchorRangeSliderButtonYValue = new SimpleDoubleProperty();
    private final DoubleProperty previousRangeSliderButtonYValue = new SimpleDoubleProperty();
    private final DoubleProperty currentRangeYValue = new SimpleDoubleProperty();
    private Node draggableNode;
    private Node oppositeNode;
    private boolean isTopRangeSlider = false;

    private final double offsetFromButtonY;

    private Consumer<MouseEvent> mousePressedEventConsumer;
    private Consumer<MouseEvent> mouseDraggedEventConsumer;
    private Consumer<MouseEvent> mouseReleasedEventConsumer;


    /**
     * The user will add listeners to the currentRangeYValue to see the y value to be used to grab items in view.
     * @param draggableNode
     * @param oppositeNode
     */
    public RangeSliderSupport(Node draggableNode, Node oppositeNode) {
        this.draggableNode = draggableNode;
        this.oppositeNode = oppositeNode;
        offsetFromButtonY = draggableNode.getBoundsInLocal().getHeight()/2;
        // guesstimate
        isTopRangeSlider = draggableNode.getLayoutY() < oppositeNode.getLayoutY();

        previousRangeSliderButtonYValue.set(draggableNode.getLayoutY());
        draggableNode.addEventHandler(MouseEvent.MOUSE_PRESSED, this::handlePositionMousePressed);
        draggableNode.addEventHandler(MouseEvent.MOUSE_DRAGGED, this::handlePositionMouseDragged);
        draggableNode.addEventHandler(MouseEvent.MOUSE_RELEASED, this::handlePositionMouseReleased);

        // update current Y
        updateRangeYValue();
    }


    public void onMousePressed(Consumer<MouseEvent> eventConsumer) {
        this.mousePressedEventConsumer = eventConsumer;
    }
    public void onMouseDragged(Consumer<MouseEvent> eventConsumer) {
        this.mouseDraggedEventConsumer = eventConsumer;
    }
    public void onMouseReleased(Consumer<MouseEvent> eventConsumer) {
        this.mouseReleasedEventConsumer = eventConsumer;
    }

    public Node getDraggableNode() {
        return draggableNode;
    }
    private void updateRangeYValue() {
        double centerLine = getDraggableNode().getBoundsInLocal().getHeight()/2;
        currentRangeYValue.set(getDraggableNode().getLayoutY() + centerLine);
    }
    /**
     * Position window mouse pressed
     * @param mouseEvent
     */
    public void handlePositionMousePressed(MouseEvent mouseEvent) {
        anchorRangeSliderButtonYValue.set(mouseEvent.getY());
        updateRangeYValue();

        // Caller may want to do additional work after mouse event.
        if (this.mousePressedEventConsumer != null) {
            this.mousePressedEventConsumer.accept(mouseEvent);
        }
    }


    /**
     * Position window mouse dragged
     * @param mouseEvent
     */
    public void handlePositionMouseDragged(MouseEvent mouseEvent) {
        double amountMoved = mouseEvent.getY() - anchorRangeSliderButtonYValue.get();
        Node node = getDraggableNode();
        double amountToMove = node.getLayoutY() + amountMoved;
        double currentY = 0;

        // if it's the top slider and y is less than other node's y per
        boolean move = false;
        if (isTopRangeSlider) {
            currentY = amountToMove - offsetFromButtonY;
            if (currentY < oppositeNode.getLayoutY()) {
                move = true;
            }
        } else {
            currentY = amountToMove + offsetFromButtonY;
            if (currentY > rangeValueOfNode(oppositeNode)) {
                move = true;
            }
        }
        if (move) {
            node.setLayoutY(amountToMove);
            updateRangeYValue();
        }

        // Caller may want to do additional work after mouse event.
        if (this.mouseDraggedEventConsumer != null) {
            this.mouseDraggedEventConsumer.accept(mouseEvent);
        }
    }
    private double rangeValueOfNode(Node node) {
        return node.getLayoutY() + node.getBoundsInLocal().getHeight()/2;
    }
    public void handlePositionMouseReleased(MouseEvent mouseEvent) {
        updateRangeYValue();
        // Caller may want to do additional work after mouse event.
        if (this.mouseReleasedEventConsumer != null) {
            this.mouseReleasedEventConsumer.accept(mouseEvent);
        }
    }


    public DoubleProperty currentRangeYValueProperty() {
        return currentRangeYValue;
    }

}
