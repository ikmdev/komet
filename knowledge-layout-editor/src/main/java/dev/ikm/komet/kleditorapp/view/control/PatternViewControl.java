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
import javafx.scene.layout.ColumnConstraints;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.Priority;
import javafx.scene.layout.VBox;

import java.util.ArrayList;
import java.util.List;

public class PatternViewControl extends GridBaseControl {
    public static final String DEFAULT_STYLE_CLASS = "pattern-view";

    private final VBox patternContainer = new VBox();
    private final Label patternTitle = new Label();
    private final GridPane contentContainer = new GridPane();

    public PatternViewControl() {
        patternContainer.getStyleClass().add("pattern-container");

        patternTitle.textProperty().bind(title);
        patternContainer.getChildren().addAll(patternTitle, contentContainer);

        Bindings.bindContent(contentContainer.getChildren(), getFields());

        getChildren().add(patternContainer);

        fields.addListener(this::onFieldsChanged);

        numberColumns.subscribe(this::updateNumberOfColumns);

        contentContainer.setHgap(5);
        contentContainer.setVgap(0);

        patternTitle.getStyleClass().add("pattern-title");
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

    private void onFieldsChanged(ListChangeListener.Change<? extends FieldViewControl> change) {
        while (change.next()) {
            if (change.wasAdded()) {
                change.getAddedSubList().forEach(pattern -> {
                    pattern.setParentPattern(this);

                    pattern.setRowIndex(fields.indexOf(pattern));
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
