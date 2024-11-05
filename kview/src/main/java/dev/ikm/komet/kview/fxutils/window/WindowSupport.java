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
package dev.ikm.komet.kview.fxutils.window;

import javafx.beans.property.DoubleProperty;
import javafx.beans.property.ObjectProperty;
import javafx.beans.property.SimpleDoubleProperty;
import javafx.beans.property.SimpleObjectProperty;
import javafx.geometry.Bounds;
import javafx.geometry.Point2D;
import javafx.scene.Cursor;
import javafx.scene.Node;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.Pane;
import javafx.scene.paint.Color;
import javafx.scene.shape.Circle;
import javafx.scene.shape.Line;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Consumer;

/**
 * Window Support is responsible for wrapping an existing UI Pane owned by a journal Pane to provide windowing behavior
 * such as resizing and moving by way of dragging a title bar.
 * The strategy to detect and resize a window pane uses four circles and four lines, where the circles are touch points as the mouse hovers over them
 * to allow the user to drag corners to resize the window. The lines represent the north, east, south and west edge of the window to be resized.
 * TODO user of the API should call removeSupport() on the WindowSupport instance to avoid possible memory leaks.
 */
public class WindowSupport {
    private static final Logger LOG = LoggerFactory.getLogger(WindowSupport.class);

    public final static Color TRANSLUCENT_COLOR = Color.color(1, 1,1, .01);
    public final static Color HIGHLIGHT_COLOR = Color.web("#67BA3FFF");

    /////////////////////////////////////////////////////
    //  Below is the mouse resizing a Pane or window
    //  inside a journal pane area.
    /////////////////////////////////////////////////////
    private final ObjectProperty<Point2D> anchorPathPaneXYCoordValue = new SimpleObjectProperty<>();

    // Stage's xTo and yTo current upper left corner location
    private final DoubleProperty paneXCoordValue = new SimpleDoubleProperty(-1);
    private final DoubleProperty paneYCoordValue = new SimpleDoubleProperty();

    private final ObjectProperty<Point2D> anchorCoordValue = new SimpleObjectProperty<>();
    private final DoubleProperty anchorWidthSizeValue = new SimpleDoubleProperty();
    private final DoubleProperty anchorHeightSizeValue = new SimpleDoubleProperty();
    private final DoubleProperty resizeWidthValue = new SimpleDoubleProperty();
    private final DoubleProperty resizeHeightValue = new SimpleDoubleProperty();

    private PaneMousePressed mousePressed;
    private PaneMouseDragged mouseDragged;
    private PaneMouseReleased mouseReleased;
    private final ObjectProperty<CursorMappings.RESIZE_DIRECTION> currentResizeDirection = new SimpleObjectProperty<>(CursorMappings.RESIZE_DIRECTION.NONE);

    private final List<Line> resizeLineSegments = new ArrayList<>();
    /////////////////////////////////////////////////////
    //  Below is the mouse dragging a Pane or window
    //  inside a journal pane area.
    /////////////////////////////////////////////////////
    private final SimpleObjectProperty<Point2D> anchorPtProperty = new SimpleObjectProperty<>(new Point2D(0,0));
    private final SimpleObjectProperty<Point2D> previousLocationProperty = new SimpleObjectProperty<>(new Point2D(0,0));

    /**
     * Draggable title area.
     */
    private final Node[] draggableNodes;

    /**
     * The pane or sub window on journal pane.
     */
    private final Pane pane;

    /**
     * Desktop area surface.
     */
    private final Pane desktopPane;

    private boolean enableDrag = true;

    private Consumer<MouseEvent> positionWindowPress;
    private Consumer<MouseEvent> positionWindowDrag;
    private Consumer<MouseEvent> positionWindowRelease;

    // We will initialize these Consumers in the constructor
    private final Consumer<MouseEvent> defaultPositionWindowPress;
    private final Consumer<MouseEvent> defaultPositionWindowDrag;
    private final Consumer<MouseEvent> defaultPositionWindowRelease;

    /**
     * A parent node is a panel (Pane) to view. The draggableNode is typically a title bar that allows the user to
     * drag the window around the journal.
     *
     * @param parentNode - The window panel to be displayed on the journal view.
     * @param desktopPane - The workspace pane (e.g., desktopSurfacePane) to keep the window within its bounds.
     * @param draggableNodes The draggable nodes. This typically is a title area to allow the user to position (dragging) the window on the journal.
     */
    public WindowSupport(final Pane parentNode, Pane desktopPane, Node... draggableNodes) {
        this.pane = parentNode;
        this.desktopPane = desktopPane;
        this.draggableNodes = draggableNodes;

        // Initialize Consumers after 'pane' is assigned
        this.defaultPositionWindowPress = (mouseEvent -> {
            anchorPtProperty.set(new Point2D(mouseEvent.getSceneX(), mouseEvent.getSceneY()));
            previousLocationProperty.set(new Point2D(pane.getTranslateX(), pane.getTranslateY()));
        });

        this.defaultPositionWindowDrag = (mouseEvent -> {
            if (isEnableDrag()) {
                if (anchorPtProperty.isNotNull().get() && previousLocationProperty.isNotNull().get()) {
                    getPane().setTranslateX(previousLocationProperty.get().getX()
                            + mouseEvent.getSceneX()
                            - anchorPtProperty.get().getX());
                    getPane().setTranslateY(previousLocationProperty.get().getY()
                            + mouseEvent.getSceneY()
                            - anchorPtProperty.get().getY());
                    clampWindowPosition(); // Clamp position after dragging
                }
            }
        });

        this.defaultPositionWindowRelease = (mouseEvent ->
                previousLocationProperty.set(new Point2D(pane.getTranslateX(), pane.getTranslateY())));

        parentNode.addEventFilter(MouseEvent.MOUSE_PRESSED, this::handleParentNodeMousePressedFilter);

        for (Node draggableNode : draggableNodes) {
            draggableNode.addEventHandler(MouseEvent.MOUSE_PRESSED, this::handlePositionWindowMousePressed);
            draggableNode.addEventHandler(MouseEvent.MOUSE_DRAGGED, this::handlePositionWindowMouseDragged);
            draggableNode.addEventHandler(MouseEvent.MOUSE_RELEASED, this::handlePositionWindowMouseReleased);
        }

        // Add segments to sides
        Line northEdge = createLine(Cursor.N_RESIZE);
        Line eastEdge = createLine(Cursor.E_RESIZE);
        Line southEdge = createLine(Cursor.S_RESIZE);
        Line westEdge = createLine(Cursor.W_RESIZE);

        // Style behavior around window when user is hovering over the window pane. Highlight Green
        resizeLineSegments.addAll(List.of(northEdge, eastEdge, southEdge, westEdge));
        this.pane.addEventHandler(MouseEvent.MOUSE_ENTERED, mouseEvent -> resizeLineSegments.forEach(line -> line.setStroke(HIGHLIGHT_COLOR)));
        this.pane.addEventHandler(MouseEvent.MOUSE_EXITED, mouseEvent -> resizeLineSegments.forEach(line -> line.setStroke(TRANSLUCENT_COLOR)));

        // Add draggable corners
        Circle upperLeft = createCircle(Cursor.NW_RESIZE);
        Circle upperRight = createCircle(Cursor.NE_RESIZE);
        Circle lowerRight = createCircle(Cursor.SE_RESIZE);
        Circle lowerLeft = createCircle(Cursor.SW_RESIZE);

        // apply lines first so circles will overlay for corners.
        parentNode.getChildren()
                .addAll(northEdge, eastEdge, southEdge, westEdge,
                        upperLeft, upperRight, lowerRight, lowerLeft);

        // When bounds change update line segments and circles.
        parentNode.layoutBoundsProperty().addListener(observable -> {
            Bounds layoutBounds = parentNode.getLayoutBounds();
            double w = layoutBounds.getWidth();
            double h = layoutBounds.getHeight();
            //System.out.println("Layout changed w,h (%s,%s)".formatted(w, h));
            northEdge.setStartX(0);
            northEdge.setStartY(0);
            northEdge.setEndX(w);
            northEdge.setEndY(0);

            eastEdge.setStartX(w);
            eastEdge.setStartY(0);
            eastEdge.setEndX(w);
            eastEdge.setEndY(h);

            southEdge.setStartX(0);
            southEdge.setStartY(h);
            southEdge.setEndX(w);
            southEdge.setEndY(h);

            westEdge.setStartX(0);
            westEdge.setStartY(0);
            westEdge.setEndX(0);
            westEdge.setEndY(h);

            upperLeft.setCenterX(0);
            upperLeft.setCenterY(0);

            upperRight.setCenterX(w);
            upperRight.setCenterY(0);

            lowerRight.setCenterX(w);
            lowerRight.setCenterY(h);

            lowerLeft.setCenterX(0);
            lowerLeft.setCenterY(h);
        });

        // Add listeners to workspace size changes to ensure windows stay within bounds
        desktopPane.widthProperty().addListener((obs, oldVal, newVal) -> clampWindowPosition());
        desktopPane.heightProperty().addListener((obs, oldVal, newVal) -> clampWindowPosition());

        // Add listeners to pane's width and height properties
        pane.widthProperty().addListener((obs, oldVal, newVal) -> clampWindowPosition());
        pane.heightProperty().addListener((obs, oldVal, newVal) -> clampWindowPosition());

        ///////////////////////////////////////////////////
        // Code to resize the window pane in the journal
        //////////////////////////////////////////////////
        // Change stage's bindXToWidth
        resizeWidthValue.addListener( obs -> {
            double newWidth = resizeWidthValue.get();
            this.pane.setPrefWidth(Math.max(this.pane.getMinWidth(), newWidth));
            clampWindowPosition();
        });

        // Change stage's bindYToHeight
        resizeHeightValue.addListener( obs -> {
            double newHeight = resizeHeightValue.get();
            this.pane.setPrefHeight(Math.max(this.pane.getMinHeight(), newHeight));
            clampWindowPosition();
        });

        // Change pane's upper left corner's X
        paneXCoordValue.addListener(obs -> {
            this.pane.setTranslateX(paneXCoordValue.get());
            clampWindowPosition();
        });

        // Change pane's upper left corner's Y
        paneYCoordValue.addListener(obs -> {
            this.pane.setTranslateY(paneYCoordValue.get());
            clampWindowPosition();
        });

        // Listener to drag edges of windows
        Line[] lines = new Line[] { northEdge, eastEdge, southEdge, westEdge};
        for (Line line : lines) {
            line.addEventHandler(MouseEvent.MOUSE_PRESSED, mouseEvent ->
                    this.mousePressed.pressed(mouseEvent, this)
            );

            // Listener to drag window dragged (mouse dragged)
            line.addEventHandler(MouseEvent.MOUSE_DRAGGED, mouseEvent ->
                    this.mouseDragged.dragged(mouseEvent, this)
            );

            // Listener to drag window stop (mouse release)
            line.addEventHandler(MouseEvent.MOUSE_RELEASED, mouseEvent -> this.mouseReleased.released(mouseEvent, this));
        }

        // Listener to drag window start (mouse press)
        Circle[] corners = new Circle[] { upperRight, lowerRight, lowerLeft, upperLeft};
        for (Circle corner : corners) {
            corner.addEventHandler(MouseEvent.MOUSE_PRESSED, mouseEvent ->
                    this.mousePressed.pressed(mouseEvent, this)
            );

            // Listener to drag window dragged (mouse dragged)
            corner.addEventHandler(MouseEvent.MOUSE_DRAGGED, mouseEvent ->
                    this.mouseDragged.dragged(mouseEvent, this)
            );

            // Listener to drag window stop (mouse release)
            corner.addEventHandler(MouseEvent.MOUSE_RELEASED, mouseEvent -> this.mouseReleased.released(mouseEvent, this));
        }

        // sets up the resizing listeners when user hovers and drags corners and edges.
        wireListeners();
    }

    private void handleParentNodeMousePressedFilter(MouseEvent mouseEvent) {
        pane.toFront();
    }

    public double getResizeWidthValue() {
        return resizeWidthValue.get();
    }

    public DoubleProperty resizeWidthValueProperty() {
        return resizeWidthValue;
    }

    public Pane getPane() {
        return pane;
    }

    private Circle createCircle(Cursor cursor) {
        Circle circle = new Circle(5, Color.color(1, 1,1, .01));
        circle.setUserData(cursor);
        circle.setOnMouseEntered(mouseEvent -> getPane().getScene().setCursor(cursor));
        circle.setOnMouseExited(mouseEvent -> getPane().getScene().setCursor(Cursor.DEFAULT));
        return circle;
    }
    private Line createLine(Cursor cursor) {
        Color translucentColor = Color.color(1, 1,1, .01);

        Line line = new Line();
        line.setStrokeWidth(3);
        line.setStroke(HIGHLIGHT_COLOR);
        line.setUserData(cursor);
        line.setOnMouseEntered(mouseEvent -> getPane().getScene().setCursor(cursor));
        line.setOnMouseExited(mouseEvent -> getPane().getScene().setCursor(Cursor.DEFAULT));
        return line;
    }
    public void removeSupport() {
        for (Node draggableNode : draggableNodes) {
            draggableNode.removeEventHandler(MouseEvent.MOUSE_PRESSED, this::handlePositionWindowMousePressed);
            draggableNode.removeEventHandler(MouseEvent.MOUSE_DRAGGED, this::handlePositionWindowMouseDragged);
            draggableNode.removeEventHandler(MouseEvent.MOUSE_RELEASED, this::handlePositionWindowMouseReleased);
        }
    }

    public boolean isEnableDrag() {
        return enableDrag;
    }

    /**
     * Position window mouse pressed
     * @param mouseEvent
     */
    public void handlePositionWindowMousePressed(MouseEvent mouseEvent) {
        if (getPositionWindowPress() == null) {
            setPositionWindowPress(defaultPositionWindowPress);
        }
        getPositionWindowPress().accept(mouseEvent);
    }

    /**
     * Position window mouse dragged
     * @param mouseEvent
     */
    public void handlePositionWindowMouseDragged(MouseEvent mouseEvent) {
        if (getPositionWindowDrag() == null) {
            setPositionWindowDrag(defaultPositionWindowDrag);
        }
        getPositionWindowDrag().accept(mouseEvent);

        clampWindowPosition();
    }

    /**
     * Positioning window mouse release
     * @param mouseEvent
     */
    public void handlePositionWindowMouseMoved(MouseEvent mouseEvent) {
        LOG.info(String.valueOf(mouseEvent));
    }
    public void handlePositionWindowMouseReleased(MouseEvent mouseEvent) {
        if (getPositionWindowRelease() == null) {
            setPositionWindowRelease(defaultPositionWindowRelease);
        }
        getPositionWindowRelease().accept(mouseEvent);
    }
    public Consumer<MouseEvent> getPositionWindowPress() {
        return positionWindowPress;
    }

    public void setPositionWindowPress(Consumer<MouseEvent> positionWindowPress) {
        this.positionWindowPress = positionWindowPress;
    }

    public Consumer<MouseEvent> getPositionWindowDrag() {
        return positionWindowDrag;
    }

    public void setPositionWindowDrag(Consumer<MouseEvent> positionWindowDrag) {
        this.positionWindowDrag = positionWindowDrag;
    }

    public Consumer<MouseEvent> getPositionWindowRelease() {
        return positionWindowRelease;
    }

    public void setPositionWindowRelease(Consumer<MouseEvent> positionWindowRelease) {
        this.positionWindowRelease = positionWindowRelease;
    }

    public Point2D getAnchorPtProperty() {
        return anchorPtProperty.get();
    }

    public SimpleObjectProperty<Point2D> anchorPtPropertyProperty() {
        return anchorPtProperty;
    }

    public void setAnchorPtProperty(Point2D anchorPtProperty) {
        this.anchorPtProperty.set(anchorPtProperty);
    }

    public Point2D getPreviousLocationProperty() {
        return previousLocationProperty.get();
    }

    public SimpleObjectProperty<Point2D> previousLocationPropertyProperty() {
        return previousLocationProperty;
    }

    public void setPreviousLocationProperty(Point2D previousLocationProperty) {
        this.previousLocationProperty.set(previousLocationProperty);
    }

    private CursorMappings.RESIZE_DIRECTION getCurrentResizeDirection() {
        return currentResizeDirection.get();
    }
    private void wireListeners() {

        setOnMousePressed((mouseEvent, wt) -> {
            Point2D windowXY = new Point2D(pane.getTranslateX(), pane.getTranslateY());
            wt.anchorPathPaneXYCoordValue.set(windowXY);
            // TODO Revisit code b/c this might be doing the same thing as line above.
            wt.paneXCoordValue.set(windowXY.getX());
            wt.paneYCoordValue.set(windowXY.getY());
            // anchor of the mouse screen x,y position.
            // store anchor x,y of the PathPane parent (upper left)
            Point2D mouseDesktopXY = new Point2D(mouseEvent.getX(), mouseEvent.getY());
            wt.anchorCoordValue.set(mouseDesktopXY);
            // current width and height
            wt.anchorWidthSizeValue.set(pane.getWidth());
            wt.anchorHeightSizeValue.set(pane.getHeight());
            //System.out.println("press mouseX = " + mouseEvent.getX() + " translateX = " + getTranslateX());
            // current resize direction
            //String source = mouseEvent.getSource().toString();
            wt.currentResizeDirection.set(getCurrentResizeDirection());
            // current line segment
            Node shapeNode = (Node) mouseEvent.getSource();
            if (shapeNode instanceof Circle || shapeNode instanceof Line) {
                Cursor cursor = (Cursor) shapeNode.getUserData();
                if (cursor == Cursor.NE_RESIZE) {
                    wt.currentResizeDirection.set(CursorMappings.RESIZE_DIRECTION.NE);
                } else if (cursor == Cursor.SE_RESIZE) {
                    wt.currentResizeDirection.set(CursorMappings.RESIZE_DIRECTION.SE);
                } else if (cursor == Cursor.SW_RESIZE) {
                    wt.currentResizeDirection.set(CursorMappings.RESIZE_DIRECTION.SW);
                } else if (cursor == Cursor.NW_RESIZE) {
                    wt.currentResizeDirection.set(CursorMappings.RESIZE_DIRECTION.NW);
                } else if (cursor == Cursor.N_RESIZE) {
                    wt.currentResizeDirection.set(CursorMappings.RESIZE_DIRECTION.N);
                } else if (cursor == Cursor.E_RESIZE) {
                    wt.currentResizeDirection.set(CursorMappings.RESIZE_DIRECTION.E);
                } else if (cursor == Cursor.S_RESIZE) {
                    wt.currentResizeDirection.set(CursorMappings.RESIZE_DIRECTION.S);
                } else if (cursor == Cursor.W_RESIZE) {
                    wt.currentResizeDirection.set(CursorMappings.RESIZE_DIRECTION.W);
                } else {
                    wt.currentResizeDirection.set(CursorMappings.RESIZE_DIRECTION.NONE);
                }
            }

        });

        setOnMouseDragged((mouseEvent, wt) -> {
            CursorMappings.RESIZE_DIRECTION direction = wt.currentResizeDirection.get();
            switch (direction) {
                case NW:
                    // TODO Northwest or Upper Left accuracy
                    resizeNorth(mouseEvent, wt);
                    resizeWest(mouseEvent, wt);
                    break;
                case N:
                    resizeNorth(mouseEvent, wt);
                    break;
                case NE:
                    //TODO Northeast Upper right corner accuracy
                    resizeNorth(mouseEvent, wt);
                    resizeEast(mouseEvent, wt);
                    break;
                case E:
                    resizeEast(mouseEvent, wt);
                    break;
                case SE:
                    resizeSouth(mouseEvent, wt);
                    resizeEast(mouseEvent, wt);
                    break;
                case S:
                    resizeSouth(mouseEvent, wt);
                    break;
                case SW:
                    resizeSouth(mouseEvent, wt);
                    resizeWest(mouseEvent, wt);
                    break;
                case W:
                    // TODO update offset West left side accuracy
                    resizeWest(mouseEvent, wt);
                    break;
                default:
                    break;
            }
            clampWindowPosition();
        });

        // after user resizes (mouse release) the previous location is reset
        setOnMouseReleased((mouseEvent, wt) -> {
//            previousLocation = new Point2D(getTranslateX(),getTranslateY());
            previousLocationProperty.set(new Point2D(pane.getTranslateX(), pane.getTranslateY()));
        });
    }

    private void resizeNorth(MouseEvent mouseEvent, WindowSupport wt) {
        // Note: mouse cursor x,y is local to this Pane and has to be local to parent screen coordinate.
        Point2D desktopPoint = pane.localToParent(mouseEvent.getX(), mouseEvent.getY());
        double screenY = desktopPoint.getY();
        double distance = wt.anchorPathPaneXYCoordValue.get().getY() - screenY;

        wt.paneYCoordValue.set(wt.anchorPathPaneXYCoordValue.get().getY() - distance);
        double newHeight = wt.anchorHeightSizeValue.get() + distance;
        wt.resizeHeightValue.set(newHeight);
    }

    private void resizeSouth(MouseEvent mouseEvent, WindowSupport wt) {
        double screenY = mouseEvent.getY();
        double newHeight = wt.anchorHeightSizeValue.get() + screenY - wt.anchorCoordValue.get().getY();
        wt.resizeHeightValue.set(newHeight);
    }

    private void resizeEast(MouseEvent mouseEvent, WindowSupport wt) {
        double screenX = mouseEvent.getX();
        double newWidth = wt.anchorWidthSizeValue.get() + screenX - wt.anchorCoordValue.get().getX();
        wt.resizeWidthValue.set(newWidth);
    }

    private void resizeWest(MouseEvent mouseEvent, WindowSupport wt) {
        //System.out.println("mouse x, y " + mouseEvent.getX() + ", " + mouseEvent.getY());
        // Note: mouse cursor x,y is local to this Pane and has to be local to parent screen coordinate.
        Point2D desktopPoint = pane.localToParent(mouseEvent.getX(), mouseEvent.getY());

        double screenX = desktopPoint.getX();
        //double offset = wt.currentSegmentIndex.intValue() == 8 ? 10 : 0; // TODO magic numbers fix.
        double offset = 0; // TODO magic numbers fix.
        double distance = wt.anchorPathPaneXYCoordValue.get().getX() - screenX + offset; // offset left side segment 8 (10 pixels)
        wt.paneXCoordValue.set(wt.anchorPathPaneXYCoordValue.get().getX() - distance);

        double newWidth = wt.anchorWidthSizeValue.get() + distance;
        wt.resizeWidthValue.set(newWidth);
    }

    // ================================= SET-UP MOUSE EVENTS ==========================

    public void setOnMousePressed(PaneMousePressed mousePressed) {
        this.mousePressed = mousePressed;
    }

    public void setOnMouseDragged(PaneMouseDragged mouseDragged) {
        this.mouseDragged = mouseDragged;
    }
    public void setOnMouseReleased(PaneMouseReleased mouseReleased) {
        this.mouseReleased = mouseReleased;
    }


    private void clampWindowPosition() {
        double desktopPaneWidth = desktopPane.getWidth();
        double desktopPaneHeight = desktopPane.getHeight();
        double windowWidth = pane.getWidth();
        double windowHeight = pane.getHeight();

        // Check if dimensions are valid
        if (desktopPaneWidth <= 0 || desktopPaneHeight <= 0 || windowWidth <= 0 || windowHeight <= 0) {
            // Sizes are not initialized yet; skip clamping
            return;
        }

        double newX = pane.getTranslateX();
        double newY = pane.getTranslateY();

        // Clamp X position
        if (newX < 0) {
            pane.setTranslateX(0);
        } else if (newX + windowWidth > desktopPaneWidth) {
            pane.setTranslateX(desktopPaneWidth - windowWidth);
        }

        // Clamp Y position
        if (newY < 0) {
            pane.setTranslateY(0);
        } else if (newY + windowHeight > desktopPaneHeight) {
            pane.setTranslateY(desktopPaneHeight - windowHeight);
        }

        // Ensure window size does not exceed workspace
        if (pane.getWidth() > desktopPaneWidth) {
            pane.setPrefWidth(desktopPaneWidth);
        }
        if (pane.getHeight() > desktopPaneHeight) {
            pane.setPrefHeight(desktopPaneHeight);
        }
    }
}