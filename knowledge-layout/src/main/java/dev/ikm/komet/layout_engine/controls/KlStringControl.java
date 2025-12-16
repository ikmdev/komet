package dev.ikm.komet.layout_engine.controls;

import javafx.beans.DefaultProperty;
import javafx.beans.property.SimpleStringProperty;
import javafx.beans.property.StringProperty;
import javafx.geometry.Insets;
import javafx.scene.control.*;
import javafx.scene.layout.VBox;

/**
 * An editable control for String data types.
 * <p>
 * Displays a title label and a TextField for text entry.
 */
@DefaultProperty("text")
public class KlStringControl extends Control {

    public KlStringControl() {
        getStyleClass().add("kl-string-control");
    }

    // -- title
    private final StringProperty title = new SimpleStringProperty(this, "title");
    public final StringProperty titleProperty() { return title; }
    public final String getTitle() { return title.get(); }
    public final void setTitle(String value) { title.set(value); }

    // -- text (the value)
    private final StringProperty text = new SimpleStringProperty(this, "text");
    public final StringProperty textProperty() { return text; }
    public final String getText() { return text.get(); }
    public final void setText(String value) { text.set(value); }

    // -- prompt text
    private final StringProperty promptText = new SimpleStringProperty(this, "promptText");
    public final StringProperty promptTextProperty() { return promptText; }
    public final String getPromptText() { return promptText.get(); }
    public final void setPromptText(String value) { promptText.set(value); }

    @Override
    protected Skin<?> createDefaultSkin() {
        return new KlStringControlSkin(this);
    }

    /**
     * Default skin implementation for KlStringControl.
     */
    public static class KlStringControlSkin extends SkinBase<KlStringControl> {

        private final VBox mainContainer = new VBox();
        private final Label titleLabel = new Label();
        private final TextField textField = new TextField();

        public KlStringControlSkin(KlStringControl control) {
            super(control);

            mainContainer.getChildren().addAll(titleLabel, textField);
            getChildren().add(mainContainer);

            // Title binding
            titleLabel.textProperty().bind(control.titleProperty());
            titleLabel.getStyleClass().add("kl-editable-title-label");

            // TextField binding
            textField.promptTextProperty().bind(control.promptTextProperty());
            textField.getStyleClass().add("kl-text-field");

            // Bidirectional sync between control.text and textField.text
            // Using listener instead of bidirectional bind to allow for debouncing if needed
            control.textProperty().subscribe(textField::setText);
            textField.textProperty().addListener((obs, oldVal, newVal) -> {
                if (!newVal.equals(control.getText())) {
                    control.setText(newVal);
                }
            });

            // CSS classes
            mainContainer.getStyleClass().add("kl-main-container");
        }

        @Override
        protected double computePrefHeight(double width, double topInset, double rightInset,
                                           double bottomInset, double leftInset) {
            double titleHeight = titleLabel.prefHeight(-1);
            double textFieldHeight = textField.prefHeight(width);
            return topInset + titleHeight + textFieldHeight + bottomInset;
        }

        @Override
        protected void layoutChildren(double contentX, double contentY,
                                      double contentWidth, double contentHeight) {
            Insets padding = getSkinnable().getPadding();
            double x = contentX + padding.getLeft();
            double y = contentY + padding.getTop();

            double labelWidth = titleLabel.prefWidth(-1);
            double labelHeight = titleLabel.prefHeight(labelWidth);
            titleLabel.resizeRelocate(x, y, labelWidth, labelHeight);

            y += labelHeight;
            double textFieldHeight = textField.prefHeight(contentWidth);
            textField.resizeRelocate(x, y, contentWidth, textFieldHeight);
        }
    }
}