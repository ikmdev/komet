package dev.ikm.komet.kleditorapp.view.control;

import javafx.beans.InvalidationListener;
import javafx.beans.Observable;
import javafx.beans.binding.Bindings;
import javafx.beans.property.IntegerProperty;
import javafx.beans.property.ObjectProperty;
import javafx.beans.property.ReadOnlyObjectProperty;
import javafx.beans.property.ReadOnlyObjectWrapper;
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
import javafx.scene.input.Dragboard;
import javafx.scene.input.TransferMode;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.ColumnConstraints;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.Priority;
import javafx.scene.layout.StackPane;

import java.util.ArrayList;
import java.util.List;
import java.util.function.BiConsumer;

import static dev.ikm.komet.kleditorapp.view.control.PatternBrowserCell.KL_EDITOR_VERSION_PROXY;

public class SectionViewControl extends EditorWindowBaseControl {
    public static final String DEFAULT_STYLE_CLASS = "section-view";

    private final BorderPane mainContainer = new BorderPane();
    private final StackPane titleContainer = new StackPane();
    private final Label tagTextLabel = new Label();
    private final TitledPane titledPane = new TitledPane();
    private final GridPane contentContainer = new GridPane();

    public SectionViewControl() {
        titleContainer.getChildren().add(tagTextLabel);
        titledPane.setContent(contentContainer);
        mainContainer.setTop(titleContainer);
        mainContainer.setCenter(titledPane);
        getChildren().add(mainContainer);

        titledPane.setMaxHeight(Double.MAX_VALUE);

        titledPane.textProperty().bind(name);
        setTitledPaneUnCollapsible(titledPane);

        tagTextLabel.textProperty().bind(tagText);
        Bindings.bindContent(contentContainer.getChildren(), getPatterns());

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

            doPatternDrop(event, this);
        });

        patterns.addListener(this::onPatternsChanged);

        numberColumns.subscribe(this::updateNumberOfColumns);

        contentContainer.setHgap(5);
        contentContainer.setVgap(5);

        // CSS
        titleContainer.getStyleClass().add("title-container");
        contentContainer.getStyleClass().add("content");
        getStyleClass().add(DEFAULT_STYLE_CLASS);
    }

    private void updateNumberOfColumns(Number numberColumns) {
        List<ColumnConstraints> columns = new ArrayList<>();
        for (int i = 0; i < numberColumns.intValue(); ++i) {
            ColumnConstraints columnConstraints = new ColumnConstraints();
            columnConstraints.setHgrow(Priority.ALWAYS);
            columnConstraints.setPercentWidth(100 / ((double)numberColumns.intValue()));
            columns.add(columnConstraints);
        }
        contentContainer.getColumnConstraints().setAll(columns);
    }

    private void onPatternsChanged(ListChangeListener.Change<? extends PatternViewControl> change) {
        while (change.next()) {
            if (change.wasAdded()) {
                change.getAddedSubList().forEach(pattern -> {
                    pattern.setParentSection(this);

                    pattern.setRowIndex(patterns.indexOf(pattern));
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

    private void doPatternDrop(DragEvent event, SectionViewControl sectionViewControl) {
        if (getOnPatternDropped() != null) {
            Dragboard dragboard = event.getDragboard();
            Integer patternNid = (Integer) dragboard.getContent(KL_EDITOR_VERSION_PROXY);
            getOnPatternDropped().accept(event, patternNid);
        }
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

    // -- on pattern dropped
    private ObjectProperty<BiConsumer<DragEvent, Integer>> onPatternDropped = new SimpleObjectProperty<>();
    public BiConsumer<DragEvent, Integer> getOnPatternDropped() { return onPatternDropped.get(); }
    public ObjectProperty<BiConsumer<DragEvent, Integer>> onPatternDroppedProperty() { return onPatternDropped; }
    public void setOnPatternDropped(BiConsumer<DragEvent, Integer> onPatternDropped) { this.onPatternDropped.set(onPatternDropped); }
}
