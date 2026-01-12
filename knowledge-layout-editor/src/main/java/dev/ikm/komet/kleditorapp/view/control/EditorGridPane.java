package dev.ikm.komet.kleditorapp.view.control;

import dev.ikm.komet.kleditorapp.view.GridDropInfo;
import dev.ikm.komet.kleditorapp.view.skin.EditorGridPaneSkin;
import javafx.beans.property.IntegerProperty;
import javafx.beans.property.ObjectProperty;
import javafx.beans.property.SimpleIntegerProperty;
import javafx.beans.property.SimpleObjectProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.scene.control.Control;
import javafx.scene.control.Skin;
import javafx.scene.input.DragEvent;

import java.util.function.BiConsumer;
import java.util.function.Consumer;

public class EditorGridPane extends Control {
    public static final String DEFAULT_STYLE_CLASS = "editor-grid-pane";

    public EditorGridPane() {
        getStyleClass().add(DEFAULT_STYLE_CLASS);
    }

    @Override
    protected Skin<?> createDefaultSkin() {
        return new EditorGridPaneSkin(this);
    }

    // -- items
    private ObservableList<GridBaseControl> items = FXCollections.observableArrayList();
    public ObservableList<GridBaseControl> getItems() { return items; }

    // -- number columns
    private final IntegerProperty numberColumns = new SimpleIntegerProperty(1);
    public int getNumberColumns() { return numberColumns.get(); }
    public IntegerProperty numberColumnsProperty() { return numberColumns; }
    public void setNumberColumns(int number) { numberColumns.set(number); }

    // -- h gap
    private final IntegerProperty hgap = new SimpleIntegerProperty();
    public int getHgap() { return hgap.get(); }
    public IntegerProperty hgapProperty() { return hgap; }
    public void setHgap(int number) { hgap.set(number); }

    // -- v gap
    private final IntegerProperty vgap = new SimpleIntegerProperty();
    public int getVgap() { return vgap.get(); }
    public IntegerProperty vgapProperty() { return vgap; }
    public void setVgap(int number) { vgap.set(number); }

    // -- on drag over into tile
    private final ObjectProperty<Consumer<DragEvent>> onDragOverIntoTile = new SimpleObjectProperty<>();
    public Consumer<DragEvent> getOnDragOverIntoTile() { return onDragOverIntoTile.get(); }
    public ObjectProperty<Consumer<DragEvent>> onDragOverIntoTileProperty() { return onDragOverIntoTile; }
    public void setOnDragDroppedIntoTile(Consumer<DragEvent> consumer) { onDragOverIntoTile.set(consumer); }

    // -- on drag dropped
    private final ObjectProperty<BiConsumer<DragEvent, GridDropInfo>> onDragDroppedIntoTile = new SimpleObjectProperty<>();
    public BiConsumer<DragEvent, GridDropInfo> getOnDragDroppedIntoTile() { return onDragDroppedIntoTile.get(); }
    public ObjectProperty<BiConsumer<DragEvent, GridDropInfo>> onDragDroppedIntoTileProperty() {return onDragDroppedIntoTile; }
    public void setOnDragDroppedIntoTile(BiConsumer<DragEvent, GridDropInfo> onDragDroppedIntoTile) { this.onDragDroppedIntoTile.set(onDragDroppedIntoTile); }
}