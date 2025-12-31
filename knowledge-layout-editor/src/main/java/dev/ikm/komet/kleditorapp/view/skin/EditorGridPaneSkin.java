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

import java.util.ArrayList;
import java.util.List;

public class EditorGridPaneSkin extends SkinBase<EditorGridPane> {

    private final GridPane gridPane = new GridPane();
    private final List<GridTile> gridTiles = new ArrayList<>();

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
        control.getItems().forEach(this::setupDragAndDrop);
        updateNumberColumns();
        updateTiles();
    }

    private void onItemsChanged(ListChangeListener.Change<? extends GridBaseControl> change) {
        while(change.next()) {
            if (change.wasAdded()) {
                change.getAddedSubList().forEach(gridBaseControl -> {
                    gridPane.getChildren().add(gridBaseControl);
                    setupDragAndDrop(gridBaseControl);
                });
            }
        }
        updateTiles();
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