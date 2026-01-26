package dev.ikm.komet.kleditorapp.view.control;

import dev.ikm.komet.kleditorapp.view.GridDropInfo;
import javafx.beans.InvalidationListener;
import javafx.beans.Observable;
import javafx.beans.binding.Bindings;
import javafx.beans.property.BooleanProperty;
import javafx.beans.property.IntegerProperty;
import javafx.beans.property.ObjectProperty;
import javafx.beans.property.ReadOnlyObjectProperty;
import javafx.beans.property.ReadOnlyObjectWrapper;
import javafx.beans.property.SimpleBooleanProperty;
import javafx.beans.property.SimpleIntegerProperty;
import javafx.beans.property.SimpleObjectProperty;
import javafx.beans.property.SimpleStringProperty;
import javafx.beans.property.StringProperty;
import javafx.collections.FXCollections;
import javafx.collections.ListChangeListener;
import javafx.collections.ObservableList;
import javafx.scene.Node;
import javafx.scene.control.Label;
import javafx.scene.control.TitledPane;
import javafx.scene.input.DragEvent;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.StackPane;

import java.util.function.BiConsumer;
import java.util.function.Consumer;

public class SectionViewControl extends EditorWindowBaseControl {
    public static final String DEFAULT_STYLE_CLASS = "section-view";

    private final BorderPane mainContainer = new BorderPane();
    private final StackPane titleContainer = new StackPane();
    private final Label tagTextLabel = new Label();
    private final TitledPane titledPane = new TitledPane();
    private final EditorGridPane gridPane = new EditorGridPane();

    public SectionViewControl() {
        titleContainer.getChildren().add(tagTextLabel);
        titledPane.setContent(gridPane);
        mainContainer.setTop(titleContainer);
        mainContainer.setCenter(titledPane);
        getChildren().add(mainContainer);

        titledPane.setMaxHeight(Double.MAX_VALUE);

        titledPane.textProperty().bind(name);
        titledPane.expandedProperty().bind(collapsed.not());
        setTitledPaneUnCollapsible(titledPane);

        titledPane.setAnimated(false);

        tagTextLabel.textProperty().bind(tagText);
        Bindings.bindContent(gridPane.getItems(), getPatterns());

        patterns.addListener(this::onPatternsChanged);

        gridPane.numberColumnsProperty().bind(numberColumns);

        gridPane.setHgap(5);
        gridPane.setVgap(5);

        gridPane.onDragDroppedIntoTileProperty().bind(onDragDroppedIntoTileProperty());
        gridPane.onDragOverIntoTileProperty().bind(onDragOverIntoTileProperty());

        // CSS
        titleContainer.getStyleClass().add("title-container");
        gridPane.getStyleClass().add("content");
        getStyleClass().add(DEFAULT_STYLE_CLASS);
    }

    private void onPatternsChanged(ListChangeListener.Change<? extends PatternViewControl> change) {
        while (change.next()) {
            if (change.wasAdded()) {
                change.getAddedSubList().forEach(pattern -> {
                    pattern.setParentSection(this);
                });
            }
        }
    }

    @Override
    public void delete() {
        getParentWindow().getSectionViews().remove(this);
    }

    private void setTitledPaneUnCollapsible(TitledPane titledPane) {
        // This is a hack to make the TitledPane not be collapsible while still showing the arrow
        titledPane.skinProperty().addListener(new InvalidationListener() {
            @Override
            public void invalidated(Observable observable) {
                Node title = titledPane.lookup(".title");
                title.setDisable(true);
                titledPane.sceneProperty().removeListener(this);
            }
        });
    }

    @Override
    protected void layoutChildren() {
        double leftInsets = snappedLeftInset();
        double rightInsets = snappedRightInset();
        double topInsets = snappedTopInset();
        double bottomInsets = snappedBottomInset();
        double width = getWidth();
        double height = getHeight();

        mainContainer.resizeRelocate(leftInsets, topInsets,
                width - leftInsets - rightInsets, height - topInsets - bottomInsets);
    }

    // -- tag text
    private final StringProperty tagText = new SimpleStringProperty();
    public String getTagText() { return tagText.get(); }
    public StringProperty tagTextProperty() { return tagText; }
    public void setTagText(String text) { tagText.set(text); }

    // -- title
    private final StringProperty name = new SimpleStringProperty();
    public String getName() { return name.get(); }
    public StringProperty nameProperty() { return name; }
    public void setName(String name) {this.name.set(name); }

    // -- number columns
    private final IntegerProperty numberColumns = new SimpleIntegerProperty(1);
    public int getNumberColumns() { return numberColumns.get(); }
    public IntegerProperty numberColumnsProperty() { return numberColumns; }
    public void setNumberColumns(int number) { numberColumns.set(number); }

    // -- items
    private final ObservableList<PatternViewControl> patterns = FXCollections.observableArrayList();
    public ObservableList<PatternViewControl> getPatterns() { return patterns; }

    // -- parent window
    private final ReadOnlyObjectWrapper<EditorWindowControl> parentWindow = new ReadOnlyObjectWrapper<>();
    public EditorWindowControl getParentWindow() { return parentWindow.get(); }
    public ReadOnlyObjectProperty<EditorWindowControl> parentWindowProperty() { return parentWindow.getReadOnlyProperty(); }
    void setParentWindow(EditorWindowControl parent) { this.parentWindow.set(parent); }

    // -- on drag over into tile
    private final ObjectProperty<Consumer<DragEvent>> onDragOverIntoTile = new SimpleObjectProperty<>();
    public Consumer<DragEvent> getOnDragOverIntoTile() { return onDragOverIntoTile.get(); }
    public ObjectProperty<Consumer<DragEvent>> onDragOverIntoTileProperty() { return onDragOverIntoTile; }
    public void setOnDragOverIntoTile(Consumer<DragEvent> consumer) { onDragOverIntoTile.set(consumer); }

    // -- on drag dropped into tile
    private ObjectProperty<BiConsumer<DragEvent, GridDropInfo>> onDragDroppedIntoTile = new SimpleObjectProperty<>();
    public BiConsumer<DragEvent, GridDropInfo> getOnDragDroppedIntoTile() { return onDragDroppedIntoTile.get(); }
    public ObjectProperty<BiConsumer<DragEvent, GridDropInfo>> onDragDroppedIntoTileProperty() { return onDragDroppedIntoTile; }
    public void setOnDragDroppedIntoTile(BiConsumer<DragEvent, GridDropInfo> onDragDroppedIntoTile) { this.onDragDroppedIntoTile.set(onDragDroppedIntoTile); }

    // -- start collapsed
    private final BooleanProperty collapsed = new SimpleBooleanProperty();
    public boolean getCollapsed() { return collapsed.get(); }
    public BooleanProperty collapsedProperty() { return collapsed; }
    public void setCollapsed(boolean value) { collapsed.set(value); }
}
