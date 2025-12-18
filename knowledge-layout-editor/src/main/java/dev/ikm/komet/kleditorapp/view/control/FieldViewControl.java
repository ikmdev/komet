package dev.ikm.komet.kleditorapp.view.control;

import javafx.beans.binding.Bindings;
import javafx.beans.property.IntegerProperty;
import javafx.beans.property.ReadOnlyObjectProperty;
import javafx.beans.property.ReadOnlyObjectWrapper;
import javafx.beans.property.SimpleIntegerProperty;
import javafx.beans.property.SimpleStringProperty;
import javafx.beans.property.StringProperty;
import javafx.scene.control.Label;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;

public class FieldViewControl extends EditorWindowBaseControl {
    public static final String DEFAULT_STYLE_CLASS = "field-view";

    private final HBox mainContainer = new HBox();
    private final Label fieldNumberLabel = new Label();
    private final Label fieldTitleLabel = new Label();
    private final Label fieldSeparatorLabel = new Label(":");

    public FieldViewControl() {
        mainContainer.getChildren().addAll(
                fieldNumberLabel,
                fieldSeparatorLabel,
                fieldTitleLabel
        );
        HBox.setHgrow(fieldTitleLabel, Priority.ALWAYS);

        fieldNumberLabel.textProperty().bind(Bindings.concat("Field ", fieldNumber.asString()));
        fieldTitleLabel.textProperty().bind(title);

        getChildren().add(mainContainer);

        // -- CSS
        mainContainer.getStyleClass().add("field-container");
        getStyleClass().add(DEFAULT_STYLE_CLASS);
    }

    @Override
    public void delete() {
        getParentPattern().getFields().remove(this);
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

    // -- parent pattern
    private ReadOnlyObjectWrapper<PatternViewControl> parentPattern = new ReadOnlyObjectWrapper<>();
    public PatternViewControl getParentPattern() { return parentPattern.get(); }
    public ReadOnlyObjectProperty<PatternViewControl> parentPatternProperty() { return parentPattern.getReadOnlyProperty(); }
    void setParentPattern(PatternViewControl parentSection) { this.parentPattern.set(parentSection); }

    // -- title
    private final StringProperty title = new SimpleStringProperty();
    public String getTitle() { return title.get(); }
    public StringProperty titleProperty() { return title; }
    public void setTitle(String title) { this.title.set(title); }

    // -- field number
    private final IntegerProperty fieldNumber = new SimpleIntegerProperty();
    public int getFieldNumber() { return fieldNumber.get(); }
    public IntegerProperty fieldNumberProperty() { return fieldNumber; }
    public void setFieldNumber(int number) { fieldNumber.set(number); }

    // -- column Index
    private final IntegerProperty columnIndex = new SimpleIntegerProperty(){
        @Override
        protected void invalidated() {
            GridPane.setColumnIndex(FieldViewControl.this, get());
        }
    };
    public int getColumnIndex() { return columnIndex.get(); }
    public IntegerProperty columnIndexProperty() { return columnIndex; }
    public void setColumnIndex(int index) { columnIndex.set(index); }

    // -- row index
    private final IntegerProperty rowIndex = new SimpleIntegerProperty(){
        @Override
        protected void invalidated() {
            GridPane.setRowIndex(FieldViewControl.this, get());
        }
    };
    public int getRowIndex() { return rowIndex.get(); }
    public IntegerProperty rowIndexProperty() { return rowIndex; }
    public void setRowIndex(int index) { rowIndex.set(index); }

    // -- column span
    private final IntegerProperty columnSpan = new SimpleIntegerProperty(1){
        @Override
        protected void invalidated() {
            GridPane.setColumnSpan(FieldViewControl.this, get());
        }
    };
    public int getColumnSpan() { return columnSpan.get(); }
    public IntegerProperty columnSpanProperty() { return columnSpan; }
    public void setColumnSpan(int index) { columnSpan.set(index); }
}