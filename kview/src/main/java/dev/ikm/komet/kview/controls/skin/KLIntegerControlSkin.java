package dev.ikm.komet.kview.controls.skin;

import dev.ikm.komet.kview.controls.KLIntegerControl;
import javafx.animation.PauseTransition;
import javafx.css.PseudoClass;
import javafx.geometry.Insets;
import javafx.scene.control.Label;
import javafx.scene.control.SkinBase;
import javafx.scene.control.TextField;
import javafx.scene.control.TextFormatter;
import javafx.util.Duration;
import javafx.util.Subscription;
import javafx.util.converter.IntegerStringConverter;

import java.text.MessageFormat;
import java.util.ResourceBundle;
import java.util.regex.Pattern;

/**
 * Default skin implementation for the {@link KLIntegerControl} control
 */
public class KLIntegerControlSkin extends SkinBase<KLIntegerControl> {

    private static final Pattern NUMERICAL_PATTERN = Pattern.compile("^-?([1-9][0-9]*)?$"); // allow '-', don't start with 0
    private static final PseudoClass ERROR_PSEUDO_CLASS = PseudoClass.getPseudoClass("error");
    private static final ResourceBundle resources = ResourceBundle.getBundle("dev.ikm.komet.kview.controls.integer-control");

    private final Label titleLabel;
    private final TextField textField;
    private final Label errorLabel;

    private Subscription subscription;
    private boolean textChangedViaKeyEvent;

    /**
     * Creates a new KLIntegerControlSkin instance, installing the necessary child
     * nodes into the Control {@link javafx.scene.control.Control#getChildrenUnmodifiable() children} list, as
     * well as the necessary input mappings for handling key, mouse, etc. events.
     *
     * @param control The control that this skin should be installed onto.
     */
    public KLIntegerControlSkin(KLIntegerControl control) {
        super(control);

        titleLabel = new Label();
        titleLabel.textProperty().bind(control.titleProperty());
        titleLabel.getStyleClass().add("title-label");

        textField = new TextField();
        textField.promptTextProperty().bind(control.promptTextProperty());
        textField.getStyleClass().add("value-text-field");

        errorLabel = new Label();
        errorLabel.visibleProperty().bind(control.showErrorProperty().and(
                errorLabel.textProperty().isNotNull()));
        errorLabel.getStyleClass().add("error-label");

        getChildren().addAll(titleLabel, textField, errorLabel);

        textField.setTextFormatter(new TextFormatter<>(new IntegerStringConverter(), null, change -> {
            errorLabel.setText(null);
            control.setShowError(false);
            String text = change.getControlNewText();
            if (text.isEmpty() || NUMERICAL_PATTERN.matcher(text).matches()) {
                if (!text.isEmpty() && !"-".equals(text)) {
                    // check that text is within [MIN_VALUE, MAX_VALUE]
                    try {
                        Integer.parseInt(text);
                    } catch (Exception e) {
                        // or else discard the change and warn
                        errorLabel.setText(MessageFormat.format(resources.getString("error.integer.text"), text));
                        control.setShowError(true);
                        return null;
                    }
                }
                return change;
            } else if ("-".equals(change.getText()) ) {
                if (change.getControlText().startsWith("-")) { // if text starts with '-', cancel it
                    change.setText("");
                    change.setRange(0, 1);
                    change.setCaretPosition(change.getCaretPosition() - 2);
                    change.setAnchor(change.getAnchor() - 2);
                } else { // a '-', typed from any position, will be set at the beginning of the text
                    change.setRange(0, 0);
                }
                return change;
            } else {
                errorLabel.setText(MessageFormat.format(resources.getString("error.integer.text"), change.getText()));
                control.setShowError(true);
            }
            return null;
        }));

        // value was set externally
        subscription = control.valueProperty().subscribe(nv -> {
            if (!textChangedViaKeyEvent) {
                textField.setText(nv == null ? null : nv.toString());
            }
        });
        subscription = subscription.and(textField.textProperty().subscribe(nv -> {
            textChangedViaKeyEvent = true;
            if (nv == null || nv.isEmpty() || "-".equals(nv)) {
                control.setValue(null);
            } else {
                control.setValue(Integer.parseInt(nv));
            }
            textChangedViaKeyEvent = false;
        }));

        final PauseTransition pauseTransition = new PauseTransition(Duration.seconds(1));
        pauseTransition.setOnFinished(f -> {
            textField.pseudoClassStateChanged(ERROR_PSEUDO_CLASS, false);
            errorLabel.setText(null);
        });
        subscription = subscription.and(errorLabel.textProperty().subscribe(nv -> {
            if (nv != null) {
                textField.pseudoClassStateChanged(ERROR_PSEUDO_CLASS, true);
                pauseTransition.playFromStart();
            } else {
                textField.pseudoClassStateChanged(ERROR_PSEUDO_CLASS, false);
                pauseTransition.stop();
            }
        }));
    }

    /** {@inheritDoc} */
    @Override
    public void dispose() {
        if (subscription != null) {
            subscription.unsubscribe();
        }
        super.dispose();
    }

    /** {@inheritDoc} */
    @Override
    protected double computeMinWidth(double width, double topInset, double rightInset, double bottomInset, double leftInset) {
        return computePrefHeight(width, topInset, rightInset, bottomInset, leftInset);
    }

    /** {@inheritDoc} */
    @Override
    protected double computePrefHeight(double width, double topInset, double rightInset, double bottomInset, double leftInset) {
        final double titleLabelHeight = titleLabel.prefHeight(-1) + 5;
        final double innerTextControlHeight = textField.prefHeight(width);
        final double errorLabelHeight = getSkinnable().isShowError() ? errorLabel.prefHeight(-1) + 2 : 0;
        return topInset + innerTextControlHeight + titleLabelHeight + errorLabelHeight + bottomInset;
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
        y += labelPrefHeight + 5;

        double textFieldPrefHeight = textField.prefHeight(contentWidth);
        textField.resizeRelocate(x, y, contentWidth, textFieldPrefHeight);

        if (getSkinnable().isShowError()) {
            y += textFieldPrefHeight + 2;
            labelPrefWidth = errorLabel.prefWidth(textField.getWidth());
            labelPrefHeight = errorLabel.prefHeight(labelPrefWidth);
            errorLabel.resizeRelocate(x, y, textField.getWidth(), labelPrefHeight);
        }
    }

}
