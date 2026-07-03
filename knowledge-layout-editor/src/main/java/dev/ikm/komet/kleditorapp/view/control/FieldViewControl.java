package dev.ikm.komet.kleditorapp.view.control;

import javafx.beans.binding.Bindings;
import javafx.beans.property.IntegerProperty;
import javafx.beans.property.ReadOnlyObjectProperty;
import javafx.beans.property.ReadOnlyObjectWrapper;
import javafx.beans.property.SimpleIntegerProperty;
import javafx.beans.property.SimpleStringProperty;
import javafx.beans.property.StringProperty;
import javafx.scene.control.Label;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;

public class FieldViewControl extends GridBaseControl {
    public static final String DEFAULT_STYLE_CLASS = "field-view";

    private final HBox mainContainer = new HBox();
    private final Label fieldNumberLabel = new Label();
    private final Label fieldTitleLabel = new Label();
    private final Label fieldSeparatorLabel = new Label(":");

    FieldViewControl() {
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
    protected void layoutContent(double contentX, double contentY, double contentWidth, double contentHeight) {
        mainContainer.resizeRelocate(contentX, contentY, contentWidth, contentHeight);
    }

    // -- parent pattern
    private ReadOnlyObjectWrapper<PatternStandardEditorControl> parentPattern = new ReadOnlyObjectWrapper<>();
    public PatternStandardEditorControl getParentPattern() { return parentPattern.get(); }
    public ReadOnlyObjectProperty<PatternStandardEditorControl> parentPatternProperty() { return parentPattern.getReadOnlyProperty(); }
    void setParentPattern(PatternStandardEditorControl parentSection) { this.parentPattern.set(parentSection); }

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

    // -- data type nid
    private final IntegerProperty dataTypeNid = new SimpleIntegerProperty();
    public int getDataTypeNid() { return dataTypeNid.get(); }
    public IntegerProperty dataTypeNidProperty() { return dataTypeNid; }
    public void setDataTypeNid(int value) { dataTypeNid.set(value); }
}