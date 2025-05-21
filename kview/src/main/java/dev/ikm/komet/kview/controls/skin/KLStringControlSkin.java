package dev.ikm.komet.kview.controls.skin;

import dev.ikm.komet.kview.controls.KLStringControl;
import javafx.animation.KeyFrame;
import javafx.animation.Timeline;
import javafx.geometry.Insets;
import javafx.scene.control.Label;
import javafx.scene.control.SkinBase;
import javafx.scene.control.TextField;
import javafx.util.Duration;

/**
 * Default skin implementation for the {@link KLStringControl} control
 */
public class KLStringControlSkin extends SkinBase<KLStringControl> {

    private final Label titleLabel;
    private final TextField textField;

    /**
     * Creates a new KLStringControlSkin instance, installing the necessary child
     * nodes into the Control {@link javafx.scene.control.Control#getChildrenUnmodifiable() children} list, as
     * well as the necessary input mappings for handling key, mouse, etc. events.
     *
     * @param control The control that this skin should be installed onto.
     */
    public KLStringControlSkin(KLStringControl control) {
        super(control);

        titleLabel = new Label();
        titleLabel.textProperty().bind(control.titleProperty());
        titleLabel.getStyleClass().add("editable-title-label");

        textField = new TextField();
        textField.promptTextProperty().bind(control.promptTextProperty());
        textField.getStyleClass().add("text-field");

        getChildren().addAll(titleLabel, textField);

       // textField.textProperty().bindBidirectional(control.textProperty());

        control.textProperty().subscribe(textField::setText);

        Timeline timeline = new Timeline();
        KeyFrame keyFrame1 = new KeyFrame(Duration.millis(3000), (evt) -> {});
        timeline.getKeyFrames().addAll(keyFrame1);

        timeline.setOnFinished((evt) -> {
            updateTextProperty();
        });

        textField.setOnKeyPressed( _ -> {
            timeline.playFromStart();
        });

        textField.focusedProperty().subscribe( () -> {
           if (!textField.isFocused()) {
               timeline.stop();
               updateTextProperty();
           }
        });
    }

    private void updateTextProperty(){
        getSkinnable().setText(textField.getText());
    }

    /** {@inheritDoc} */
    @Override
    protected double computeMinWidth(double width, double topInset, double rightInset, double bottomInset, double leftInset) {
        return computePrefHeight(width, topInset, rightInset, bottomInset, leftInset);
    }

    /** {@inheritDoc} */
    @Override
    protected double computePrefHeight(double width, double topInset, double rightInset, double bottomInset, double leftInset) {
        final double titleLabelHeight = titleLabel.prefHeight(-1);
        final double innerTextControlHeight = textField.prefHeight(width);
        return topInset + titleLabelHeight + innerTextControlHeight + bottomInset;
    }

    /** {@inheritDoc} */
    @Override
    protected double computeMaxHeight(double width, double topInset, double rightInset, double bottomInset, double leftInset) {
        return computePrefHeight(width, topInset, rightInset, bottomInset, leftInset);
    }

    /** {@inheritDoc} */
    @Override
    protected void layoutChildren(double contentX, double contentY, double contentWidth, double contentHeight) {
        super.layoutChildren(contentX, contentY, contentWidth, contentHeight);
        Insets padding = getSkinnable().getPadding();
        double labelPrefWidth = titleLabel.prefWidth(-1);
        double labelPrefHeight = titleLabel.prefHeight(labelPrefWidth);
        double x = contentX + padding.getLeft();
        double y = contentY + padding.getTop();
        titleLabel.resizeRelocate(x, y, labelPrefWidth, labelPrefHeight);
        y += labelPrefHeight;

        double textFieldPrefHeight = textField.prefHeight(contentWidth);
        textField.resizeRelocate(x, y, contentWidth, textFieldPrefHeight);
    }

}
