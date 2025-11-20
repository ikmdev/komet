package dev.ikm.komet.kleditorapp.view;

import javafx.beans.property.SimpleStringProperty;
import javafx.beans.property.StringProperty;
import javafx.collections.FXCollections;
import javafx.collections.ListChangeListener;
import javafx.collections.ObservableList;
import javafx.scene.control.Label;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Region;
import javafx.scene.layout.VBox;

public class PatternViewControl extends Region {
    private final VBox patternContainer = new VBox();
    private final Label patternTitle = new Label();

    private final ObservableList<String> fields = FXCollections.observableArrayList();

    private int fieldIndex = 1;

    public PatternViewControl() {
        patternContainer.getStyleClass().add("pattern-container");

        patternTitle.textProperty().bind(title);
        patternContainer.getChildren().add(patternTitle);

        fields.addListener(this::onFieldAdded);

        getChildren().add(patternContainer);
    }

    private void onFieldAdded(ListChangeListener.Change<? extends String> change) {
        while(change.next()) {
            if (change.wasAdded()) {
                for (String addedFieldString : change.getAddedSubList()) {
                    HBox patternFieldContainer = new HBox();
                    patternFieldContainer.getStyleClass().add("field-container");

                    Label patternFieldLabel = new Label("Field " + fieldIndex + ": ");
                    Label patternFieldText = new Label(addedFieldString);

                    patternFieldContainer.getChildren().addAll(patternFieldLabel, patternFieldText);

                    patternContainer.getChildren().add(patternFieldContainer);

                    ++fieldIndex;
                }
            }
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

        patternContainer.resizeRelocate(leftInsets, topInsets,
                width - leftInsets - rightInsets, height - topInsets - bottomInsets);
    }

    // -- title
    private final StringProperty title = new SimpleStringProperty();
    public String getTitle() { return title.get(); }
    public StringProperty titleProperty() { return title; }
    public void setTitle(String title) { this.title.set(title); }

    // -- fields
    public ObservableList<String> getFields() { return fields; }
}
