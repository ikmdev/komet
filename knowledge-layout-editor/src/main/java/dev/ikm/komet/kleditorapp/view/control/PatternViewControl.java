package dev.ikm.komet.kleditorapp.view.control;

import javafx.beans.binding.Bindings;
import javafx.beans.property.IntegerProperty;
import javafx.beans.property.ReadOnlyObjectProperty;
import javafx.beans.property.ReadOnlyObjectWrapper;
import javafx.beans.property.SimpleIntegerProperty;
import javafx.beans.property.SimpleStringProperty;
import javafx.beans.property.StringProperty;
import javafx.collections.FXCollections;
import javafx.collections.ListChangeListener;
import javafx.collections.ObservableList;
import javafx.scene.control.Label;
import javafx.scene.layout.VBox;

public class PatternViewControl extends GridBaseControl {
    public static final String DEFAULT_STYLE_CLASS = "pattern-view";

    private final VBox patternContainer = new VBox();
    private final Label patternTitle = new Label();
    private final EditorGridPane gridPane = new EditorGridPane();

    public PatternViewControl() {
        patternContainer.getStyleClass().add("pattern-container");

        patternTitle.textProperty().bind(title);
        patternContainer.getChildren().addAll(patternTitle, gridPane);

        Bindings.bindContent(gridPane.getItems(), getFields());

        getChildren().add(patternContainer);

        fields.addListener(this::onFieldsChanged);

        gridPane.numberColumnsProperty().bind(numberColumns);

        gridPane.setHgap(5);
        gridPane.setVgap(0);

        // CSS
        patternTitle.getStyleClass().add("pattern-title");
        getStyleClass().add(DEFAULT_STYLE_CLASS);
    }

    private void onFieldsChanged(ListChangeListener.Change<? extends FieldViewControl> change) {
        while (change.next()) {
            if (change.wasAdded()) {
                change.getAddedSubList().forEach(fieldViewControl -> {
                    fieldViewControl.setParentPattern(this);
                });
            }
        }
    }

    @Override
    public void delete() {
        getParentSection().getPatterns().remove(this);
    }

    @Override
    protected void layoutChildren() {
        double leftInsets = snappedLeftInset();
        double rightInsets = snappedRightInset();
        double topInsets = snappedTopInset();
        double bottomInsets = snappedBottomInset();
        double width = getWidth();
        double height = getHeight();

        patternContainer.resizeRelocate(leftInsets, topInsets,
                width - leftInsets - rightInsets, height - topInsets - bottomInsets);
    }

    // -- parent section
    private ReadOnlyObjectWrapper<SectionViewControl> parentSection = new ReadOnlyObjectWrapper<>();
    public SectionViewControl getParentSection() { return parentSection.get(); }
    public ReadOnlyObjectProperty<SectionViewControl> parentSectionProperty() { return parentSection.getReadOnlyProperty(); }
    void setParentSection(SectionViewControl parentSection) { this.parentSection.set(parentSection); }

    // -- title
    private final StringProperty title = new SimpleStringProperty();
    public String getTitle() { return title.get(); }
    public StringProperty titleProperty() { return title; }
    public void setTitle(String title) { this.title.set(title); }

    // -- number columns
    private final IntegerProperty numberColumns = new SimpleIntegerProperty(1);
    public int getNumberColumns() { return numberColumns.get(); }
    public IntegerProperty numberColumnsProperty() { return numberColumns; }
    public void setNumberColumns(int number) { numberColumns.set(number); }

    // -- fields
    private final ObservableList<FieldViewControl> fields = FXCollections.observableArrayList();
    public ObservableList<FieldViewControl> getFields() { return fields; }
}
