package dev.ikm.komet.kleditorapp.view.control;

import dev.ikm.komet.kleditorapp.view.GridDropInfo;
import dev.ikm.komet.kleditorapp.view.skin.EditorGridPaneSkin;
import javafx.beans.property.IntegerProperty;
import javafx.beans.property.ObjectProperty;
import javafx.beans.property.SimpleIntegerProperty;
import javafx.beans.property.SimpleObjectProperty;
import javafx.beans.value.ObservableValue;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.css.CssMetaData;
import javafx.css.Styleable;
import javafx.css.StyleableProperty;
import javafx.css.StyleablePropertyFactory;
import javafx.scene.control.Control;
import javafx.scene.control.Skin;
import javafx.scene.input.DragEvent;

import java.util.List;
import java.util.function.BiConsumer;
import java.util.function.Consumer;
import java.util.function.Predicate;

public class EditorGridPane extends Control {
    public static final String DEFAULT_STYLE_CLASS = "editor-grid-pane";

    private static final StyleablePropertyFactory<EditorGridPane> FACTORY =
            new StyleablePropertyFactory<>(Control.getClassCssMetaData());

    public EditorGridPane() {
        getStyleClass().add(DEFAULT_STYLE_CLASS);
    }

    @Override
    protected Skin<?> createDefaultSkin() {
        return new EditorGridPaneSkin(this);
    }

    public static List<CssMetaData<? extends Styleable, ?>> getClassCssMetaData() {
        return FACTORY.getCssMetaData();
    }

    @Override
    public List<CssMetaData<? extends Styleable, ?>> getControlCssMetaData() {
        return getClassCssMetaData();
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
    private final StyleableProperty<Number> hgap =
            FACTORY.createStyleableNumberProperty(this, "hgap", "-hgap", control -> control.hgap);
    public double getHgap() { return hgap.getValue().doubleValue(); }
    public ObservableValue<Number> hgapProperty() { return (ObservableValue<Number>) hgap; }
    public void setHgap(double value) { hgap.setValue(value); }

    // -- v gap
    private final StyleableProperty<Number> vgap =
            FACTORY.createStyleableNumberProperty(this, "vgap", "-vgap", control -> control.vgap);
    public double getVgap() { return vgap.getValue().doubleValue(); }
    public ObservableValue<Number> vgapProperty() { return (ObservableValue<Number>) vgap; }
    public void setVgap(double value) { vgap.setValue(value); }

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

    // -- on should drag and drop rearrange
    /**
     * Predicate that when set gets called when there is a drag and drop to rearrange a control
     * within its parent GridPane (change it to a different row and/or column).
     * If this method returns true then that drag and drop is allowed.
     */
    private final ObjectProperty<Predicate<GridBaseControl>> onShouldDragAndDropRearrange = new SimpleObjectProperty<>();
    public Predicate<GridBaseControl> getOnShouldDragAndDropRearrange() { return onShouldDragAndDropRearrange.get(); }
    public ObjectProperty<Predicate<GridBaseControl>> onShouldDragAndDropRearrangeProperty() { return onShouldDragAndDropRearrange; }
    public void setOnShouldDragAndDropRearrange(Predicate<GridBaseControl> value) { onShouldDragAndDropRearrange.set(value); }
}