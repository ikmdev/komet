package dev.ikm.komet.kleditorapp.view.skin;

import dev.ikm.komet.framework.dnd.DragImageMaker;
import dev.ikm.komet.kleditorapp.view.GridDropInfo;
import dev.ikm.komet.kleditorapp.view.control.EditorGridPane;
import dev.ikm.komet.kleditorapp.view.control.GridBaseControl;
import javafx.beans.property.BooleanProperty;
import javafx.beans.property.IntegerProperty;
import javafx.beans.property.SimpleBooleanProperty;
import javafx.beans.property.SimpleIntegerProperty;
import javafx.collections.ListChangeListener;
import javafx.css.PseudoClass;
import javafx.geometry.Bounds;
import javafx.scene.Cursor;
import javafx.scene.Node;
import javafx.scene.control.SkinBase;
import javafx.scene.image.Image;
import javafx.scene.input.ClipboardContent;
import javafx.scene.input.Dragboard;
import javafx.scene.input.TransferMode;
import javafx.scene.layout.ColumnConstraints;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.Priority;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Color;
import javafx.scene.shape.Rectangle;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

public class EditorGridPaneSkin extends SkinBase<EditorGridPane> {
    private static final int DRAG_LINE_WIDTH = 5;
    private static final PseudoClass MOUSE_HOVER_WHILE_DRAGGING_PSEUDO_CLASS = PseudoClass.getPseudoClass("mouse-hover-dragging");

    private final GridPane gridPane = new GridPane();
    private final List<GridTile> gridTiles = new ArrayList<>();

    private final List<GridTile> mouseHoverGridTiles = new ArrayList<>();
    private GridTile currentMouseHoverGridTile;

    /**
     * Constructor for EditorGridPaneSkin instances.
     *
     * @param control The EditorGridPane for which this Skin should attach to.
     */
    public EditorGridPaneSkin(EditorGridPane control) {
        super(control);

        getChildren().add(gridPane);

        // Add starting GridTile
        GridTile gridTile = new GridTile(control, true);
        gridTile.setColumnIndex(0);
        gridTile.setRowIdex(0);
        gridPane.getChildren().add(gridTile);


        // synchronize internal GridPane with Control's properties via binding, etc
        gridPane.hgapProperty().bind(control.hgapProperty());
        gridPane.vgapProperty().bind(control.vgapProperty());

        gridPane.getChildren().addAll(control.getItems());

        control.numberColumnsProperty().subscribe(() -> {
            updateNumberColumns();
            updateTiles();
        });
        control.getItems().addListener(this::onItemsChanged);

        // init control
        control.getItems().forEach(this::initGridControl);
        updateNumberColumns();
        updateTiles();
    }

    private void onItemsChanged(ListChangeListener.Change<? extends GridBaseControl> change) {
        while(change.next()) {
            if (change.wasAdded()) {
                change.getAddedSubList().forEach(gridBaseControl -> {
                    gridPane.getChildren().add(gridBaseControl);

                    initGridControl(gridBaseControl);
                });
            }
        }
        updateTiles();
    }

    private Optional<GridTile> getGridTile(int rowIndex, int columnIndex) {
        for (GridTile gridTile : gridTiles) {
            if (gridTile.getRowIdex() == rowIndex && gridTile.getColumnIndex() == columnIndex) {
                return Optional.of(gridTile);
            }
        }

        return Optional.empty();
    }

    private void initGridControl(GridBaseControl gridControl) {
        addDragHandles(gridPane, gridControl);
        setupDragAndDrop(gridControl);
    }

    private void setupDragAndDrop(GridBaseControl gridBaseControl) {
        // Set up the drag detection event handler
        gridBaseControl.setOnDragDetected(mouseEvent -> {

            // Initiate a drag-and-drop gesture with copy or move transfer mode
            Dragboard dragboard = gridBaseControl.startDragAndDrop(TransferMode.MOVE);

            // Create the content to be placed on the dragboard
            ClipboardContent clipboardContent = new ClipboardContent();
            clipboardContent.putString(gridBaseControl.toString());

            // Generate the drag image using DragImageMaker
            DragImageMaker dragImageMaker = new DragImageMaker(gridBaseControl);
            Image dragImage = dragImageMaker.getDragImage();
            // Set the drag image on the dragboard
            if (dragImage != null) {
                dragboard.setDragView(dragImage);
            }

            // Place the content on the dragboard
            dragboard.setContent(clipboardContent);

            // Consume the mouse event to prevent further processing
            mouseEvent.consume();
        });
    }

    private int getUsedRowCount() {
        int maxNumberRows = 0;
        for (Node node : getSkinnable().getItems()) {
            Integer row = GridPane.getRowIndex(node);
            maxNumberRows = Math.max(maxNumberRows, row == null ? 0 : row + 1);
        }
        return maxNumberRows;
    }

    private void updateNumberColumns() {
        int newNumberColumns = getSkinnable().getNumberColumns();

        List<ColumnConstraints> columns = new ArrayList<>();
        for (int i = 0; i < newNumberColumns; ++i) {
            ColumnConstraints columnConstraints = new ColumnConstraints();
            columnConstraints.setHgrow(Priority.ALWAYS);
            columnConstraints.setPercentWidth(100 / ((double)newNumberColumns));
            columns.add(columnConstraints);
        }
        gridPane.getColumnConstraints().setAll(columns);
    }

    private void updateTiles() {
        // For now this method brute forces recreates everything. We can be smarter (more efficient with lower
        // impact on performance) later if we want
        gridPane.getChildren().removeAll(gridTiles);
        int numberColumns = gridPane.getColumnConstraints().size();
        int numberUsedRows = getUsedRowCount();

        for (int columnIndex = 0; columnIndex < numberColumns; ++columnIndex) {
            for (int rowIndex = 0; rowIndex < numberUsedRows + 1; ++rowIndex) {
                GridTile gridTile;
                if (rowIndex < numberUsedRows) {
                    gridTile = new GridTile(getSkinnable());
                } else {
                    gridTile = new GridTile(getSkinnable(), true);
                }

                gridPane.getChildren().addFirst(gridTile);

                gridTile.setRowIdex(rowIndex);
                gridTile.setColumnIndex(columnIndex);

                gridTiles.add(gridTile);
            }
        }
    }

    private void addDragHandles(GridPane gridPane, GridBaseControl gridBaseControl) {
        // Rectangle that becomes visible when user is dragging and covers the Grid Control's bounds
        Rectangle rectWhileDragging = new Rectangle();
        rectWhileDragging.getStyleClass().add("rect-while-dragging");

        rectWhileDragging.setManaged(false);
        rectWhileDragging.setMouseTransparent(true);

        rectWhileDragging.setVisible(false);

        // Rectangle (line) used to catch the actual drag to change column span
        Rectangle rightEdge = new Rectangle();
        rightEdge.setManaged(false);

        rightEdge.setCursor(Cursor.H_RESIZE);
        rightEdge.setFill(Color.TRANSPARENT);
        rightEdge.setStroke(Color.TRANSPARENT);

        getChildren().add(rectWhileDragging);
        getChildren().add(rightEdge);

        gridBaseControl.boundsInParentProperty().subscribe(bounds -> {
            // Update drag handles and drag visuals if control changes bounds
            Bounds grindControlBounds = gridPane.localToParent(bounds);
            rectWhileDragging.setLayoutX(grindControlBounds.getMinX());
            rectWhileDragging.setLayoutY(grindControlBounds.getMinY());
            rectWhileDragging.setWidth(grindControlBounds.getWidth());
            rectWhileDragging.setHeight(grindControlBounds.getHeight());

            rightEdge.setLayoutX(grindControlBounds.getMaxX() - DRAG_LINE_WIDTH);
            rightEdge.setWidth(DRAG_LINE_WIDTH);
            rightEdge.setLayoutY(grindControlBounds.getMinY());
            rightEdge.setHeight(grindControlBounds.getHeight());
        });

        //=====================  Setup mouse events on Right Edge  ==========================

        rightEdge.setOnMouseDragged(mouseEvent -> {
            double mouseX = rightEdge.localToParent(mouseEvent.getX(), 0).getX();

            rectWhileDragging.setVisible(true);
            rectWhileDragging.setWidth(mouseX - rectWhileDragging.getLayoutX());

            Bounds gridControlBounds = gridPane.localToParent(gridBaseControl.getBoundsInParent());
            if (mouseX > gridControlBounds.getMaxX()) {
                // Dragging mouse to make Tile bigger
                int columnCount = 1;
                while(true) {
                    Optional<GridTile> optionalGridTile = getGridTile(gridBaseControl.getRowIndex(), gridBaseControl.getColumnIndex() + columnCount);
                    if (optionalGridTile.isPresent()) {
                        GridTile gridTile = optionalGridTile.get();
                        Bounds gridTileBounds = gridPane.localToParent(gridTile.getBoundsInParent());
                        if (mouseX >= gridTileBounds.getCenterX()) {
                            currentMouseHoverGridTile = gridTile;
                            mouseHoverGridTiles.add(currentMouseHoverGridTile);
                            if (mouseX <= currentMouseHoverGridTile.getLayoutX() + gridTileBounds.getWidth()) {
                                break; // Found the Tile where the mouse is hover
                            }
                        }
                    } else {
                        break;
                    }

                    ++columnCount;
                }
            }
        });

        rightEdge.setOnMouseReleased(mouseEvent -> {
            rectWhileDragging.setVisible(false);

            if (currentMouseHoverGridTile == null) {
                return;
            }

            int gridBaseControlColIndex = gridBaseControl.getColumnIndex();
            int mouseHoverGridTileColIndex = currentMouseHoverGridTile.getColumnIndex();
            gridBaseControl.setColumnSpan(mouseHoverGridTileColIndex - gridBaseControlColIndex + 1);

            currentMouseHoverGridTile = null;
        });
    }

    /***************************************************************************
     *                                                                         *
     * Support Classes                                                         *
     *                                                                         *
     **************************************************************************/

    /**
     * Represents a tile cell in the EditorGridPane
     */
    private static class GridTile extends VBox {
        public static final String DEFAULT_STYLE_CLASS = "grid-tile";

        public static PseudoClass EMPTY_PSEUDO_CLASS = PseudoClass.getPseudoClass("empty");
        public static PseudoClass DRAG_HOVER_CLASS = PseudoClass.getPseudoClass("drag-hover");

        private EditorGridPane editorGridPane;

        public GridTile(EditorGridPane editorGridPane) {
            this(editorGridPane, false);
        }

        public GridTile(EditorGridPane editorGridPane, boolean empty) {
            this.editorGridPane = editorGridPane;

            getStyleClass().add(DEFAULT_STYLE_CLASS);

            setupDragAndDrop();

            this.empty.set(empty);
        }

        private void setupDragAndDrop() {
            setOnDragOver(event -> {
                if (event.getGestureSource() instanceof GridBaseControl) {
                    event.acceptTransferModes(TransferMode.MOVE);
                    event.consume();
                    return;
                }

                if (editorGridPane.getOnDragOverIntoTile() != null) {
                    editorGridPane.getOnDragOverIntoTile().accept(event);
                }
            });

            setOnDragDropped(event -> {
                if (event.getGestureSource() instanceof GridBaseControl gridBaseControl) {
                    moveGridBaseControl(gridBaseControl, getRowIdex(), getColumnIndex());
                    event.setDropCompleted(true);
                    event.consume();
                    return;
                }

                if (editorGridPane.getOnDragDroppedIntoTile() != null) {
                    GridDropInfo gridDropInfo = new GridDropInfo(getRowIdex(), getColumnIndex());
                    editorGridPane.getOnDragDroppedIntoTile().accept(event, gridDropInfo);
                }
            });

            setOnDragEntered(event -> {
                pseudoClassStateChanged(DRAG_HOVER_CLASS, true);
            });

            setOnDragExited(event -> {
                pseudoClassStateChanged(DRAG_HOVER_CLASS, false);
            });
        }

        private void moveGridBaseControl(GridBaseControl gridBaseControl, int rowIdex, int columnIndex) {
            gridBaseControl.setRowIndex(rowIdex);
            gridBaseControl.setColumnIndex(columnIndex);
        }

        // -- row index
        private IntegerProperty rowIdex = new SimpleIntegerProperty() {
            @Override
            protected void invalidated() {
                GridPane.setRowIndex(GridTile.this, get());
            }
        };
        public int getRowIdex() { return rowIdex.get(); }
        public IntegerProperty rowIdexProperty() { return rowIdex; }
        public void setRowIdex(int rowIdex) { this.rowIdex.set(rowIdex); }

        // -- column index
        private IntegerProperty columnIndex = new SimpleIntegerProperty() {
            @Override
            protected void invalidated() {
                GridPane.setColumnIndex(GridTile.this, get());
            }
        };
        public int getColumnIndex() { return columnIndex.get(); }
        public IntegerProperty columnIndexProperty() { return columnIndex; }
        public void setColumnIndex(int columnIndex) { this.columnIndex.set(columnIndex); }

        // -- empty
        private BooleanProperty empty = new SimpleBooleanProperty() {
            @Override
            protected void invalidated() {
                pseudoClassStateChanged(EMPTY_PSEUDO_CLASS, get());
            }
        };
        public boolean isEmpty() { return empty.get(); }
        public BooleanProperty emptyProperty() { return empty; }
        public void setEmpty(boolean empty) { this.empty.set(empty); }
    }
}