package dev.ikm.komet.kleditorapp.view.skin;

import dev.ikm.komet.framework.QuadConsumer;
import dev.ikm.komet.kleditorapp.view.control.EditorGridPane;
import dev.ikm.komet.kleditorapp.view.control.EditorWindowBaseControl;
import javafx.beans.property.BooleanProperty;
import javafx.beans.property.SimpleBooleanProperty;
import javafx.collections.ListChangeListener;
import javafx.css.PseudoClass;
import javafx.scene.Node;
import javafx.scene.control.SkinBase;
import javafx.scene.input.DragEvent;
import javafx.scene.input.Dragboard;
import javafx.scene.input.TransferMode;
import javafx.scene.layout.ColumnConstraints;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.Priority;
import javafx.scene.layout.VBox;

import java.util.ArrayList;
import java.util.List;

import static dev.ikm.komet.kleditorapp.view.control.PatternBrowserCell.KL_EDITOR_VERSION_PROXY;

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
        GridPane.setRowIndex(gridTile, 0);
        GridPane.setColumnIndex(gridTile, 0);
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
        updateNumberColumns();
        updateTiles();
    }

    private void onItemsChanged(ListChangeListener.Change<? extends EditorWindowBaseControl> change) {
        while(change.next()) {
            if (change.wasAdded()) {
                gridPane.getChildren().addAll(change.getAddedSubList());
            }
        }
        updateTiles();
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

                GridPane.setRowIndex(gridTile, rowIndex);
                GridPane.setColumnIndex(gridTile, columnIndex);

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
            // Drag and drop
            setOnDragOver(event -> {
                if (event.getDragboard().hasContent(KL_EDITOR_VERSION_PROXY)) {
                    event.acceptTransferModes(TransferMode.COPY);
                }

                event.consume();
            });

            setOnDragDropped(event -> {
                if (!event.getDragboard().hasContent(KL_EDITOR_VERSION_PROXY)) {
                    event.setDropCompleted(false);
                    event.consume();
                    return;
                }

                doPatternDrop(event);
            });

            setOnDragEntered(event -> {
                pseudoClassStateChanged(DRAG_HOVER_CLASS, true);
            });

            setOnDragExited(event -> {
                pseudoClassStateChanged(DRAG_HOVER_CLASS, false);
            });
        }

        private void doPatternDrop(DragEvent event) {
            if (editorGridPane.getOnPatternDropped() != null) {
                QuadConsumer<DragEvent, Integer, Integer, Integer> onPatternDropped = editorGridPane.getOnPatternDropped();

                Dragboard dragboard = event.getDragboard();

                Integer patternNid = (Integer) dragboard.getContent(KL_EDITOR_VERSION_PROXY);
                Integer rowIndex = GridPane.getRowIndex(this);
                Integer columnIndex = GridPane.getColumnIndex(this);

                onPatternDropped.accept(event, patternNid, rowIndex, columnIndex);
            }
        }

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