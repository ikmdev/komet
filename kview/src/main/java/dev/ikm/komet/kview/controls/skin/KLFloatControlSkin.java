package dev.ikm.komet.kview.controls.skin;

import dev.ikm.komet.kview.controls.KLFloatControl;
import javafx.animation.PauseTransition;
import javafx.geometry.Insets;
import javafx.scene.control.Label;
import javafx.scene.control.SkinBase;
import javafx.scene.control.TextField;
import javafx.scene.control.TextFormatter;
import javafx.util.Subscription;

import java.text.MessageFormat;
import java.util.ResourceBundle;
import java.util.regex.Pattern;

/**
 * Default skin implementation for the {@link KLFloatControl} control
 */
public class KLFloatControlSkin extends SkinBase<KLFloatControl> {

    private static final Pattern NUMERICAL_PATTERN = Pattern.compile("^[+-]?(\\d+([.]\\d*)?([eE][+-]?\\d+)?|[.]\\d+([eE][+-]?\\d+)?)$");
    private static final ResourceBundle resources = ResourceBundle.getBundle("dev.ikm.komet.kview.controls.float-control");

    private final Label titleLabel;
    private final TextField textField;
    private final Label errorLabel;

    private Subscription subscription;
    private boolean textChangedViaKeyEvent;

    /**
     * Creates a new KLFloatControlSkin instance, installing the necessary child
     * nodes into the Control {@link javafx.scene.control.Control#getChildrenUnmodifiable() children} list, as
     * well as the necessary input mappings for handling key, mouse, etc. events.
     *
     * @param control The control that this skin should be installed onto.
     */
    public KLFloatControlSkin(KLFloatControl control) {
        super(control);

        titleLabel = new Label();
        titleLabel.textProperty().bind(control.titleProperty());
        titleLabel.getStyleClass().add("title-label");

        textField = new TextField();
        textField.promptTextProperty().bind(control.promptTextProperty());
        textField.getStyleClass().add("text-field");

        errorLabel = new Label();
        errorLabel.visibleProperty().bind(control.showErrorProperty().and(
                errorLabel.textProperty().isNotNull()));
        errorLabel.getStyleClass().add("error-label");

        getChildren().addAll(titleLabel, textField, errorLabel);

        textField.setTextFormatter(new TextFormatter<>(null, null, change -> {
            errorLabel.setText(null);
            control.setShowError(false);
            String oldText = change.getControlText();
            String newText = change.getControlNewText();
            String addedText = change.getText();
            int exponentPosition = exponentPosition(oldText);

            // Valid change (even if number is still not valid):
            // - empty (null value)
            // - adding e/E if oldText didn't have e/E, and wasn't empty
            // - adding '-' or '+' if empty or ends in e/E
            // - back when text ends in [-/+, e, e-/+, E, E-/+]
            // - pattern
            if (newText.isEmpty() ||
                    (hasExponent(newText) && !hasExponent(oldText) && !oldText.isEmpty()) ||
                    (("-".equals(addedText) || "+".equals(addedText)) && (oldText.isEmpty() || endsWithExponent(oldText))) ||
                    (addedText.isEmpty() && ("+".equals(newText) || "-".equals(newText) || hasExponent(newText) || hasSignedExponent(newText))) ||
                    NUMERICAL_PATTERN.matcher(newText).matches()) {
                try {
                    double value = Double.parseDouble(newText);
                    // discard if we have a valid value, but it is infinite or NaN
                    if (Double.isInfinite(value) || Double.isNaN(value)) {
                        errorLabel.setText(MessageFormat.format(resources.getString("error.float.text"), newText));
                        control.setShowError(true);
                        return null;
                    }
                } catch (NumberFormatException nfe) {
                    errorLabel.setText(MessageFormat.format(resources.getString("error.float.text"), newText));
                    control.setShowError(true);
                }
                return change;
            } else if ("-".equals(addedText) || "+".equals(addedText)) { // typing '-'/'+' in any other position
                // start is position 0 when typing without exponent or before it, or
                // exponent position + 1 when typing to the right of the exponent
                int start = change.getRangeStart() <= exponentPosition ? 0 : exponentPosition + 1;

                if (oldText.charAt(start) == addedText.charAt(0)) {
                    // if text at start is '-'/'+', typing same '-'/'+', cancels it
                    change.setText("");
                    change.setRange(start, start + 1);
                    change.setCaretPosition(Math.max(0, change.getCaretPosition() - 2));
                    change.setAnchor(Math.max(0, change.getAnchor() - 2));
                    return change;
                } else if ((oldText.charAt(start) == '-' && "+".equals(addedText)) ||
                        (oldText.charAt(start) == '+' && "-".equals(addedText))) {
                    // if text at start is '-'/'+', typing '+'/'-', replaces it with '+'/'-'
                    change.setRange(start, start + 1);
                    change.setCaretPosition(Math.max(0, change.getCaretPosition() - 1));
                    change.setAnchor(Math.max(0, change.getAnchor() - 1));
                    return change;
                } else if (oldText.charAt(start) != '-' && oldText.charAt(start) != '+') {
                    // if text at start is not '-'/'+', typing '-'/'+' from any position, sets it
                    change.setRange(start, start);
                    return change;
                }
            }
            errorLabel.setText(MessageFormat.format(resources.getString("error.input.text"), addedText));
            control.setShowError(true);
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
            if (nv == null || nv.isEmpty()) {
                // When new text is null or empty, reset control's value
                control.setValue(null);
            } else if (!("-".equals(nv) || "+".equals(nv) || endsWithExponent(nv) || endsWithSignedExponent(nv))) {
                try {
                    // only set control's value when it is a valid number
                    float value = Float.parseFloat(nv);
                    control.setValue(value);
                } catch (NumberFormatException e) {
                    // ignore, and keep control with its old value
                }
            }
            textChangedViaKeyEvent = false;
        }));

        final PauseTransition pauseTransition = new PauseTransition(KLIntegerControlSkin.ERROR_DURATION);
        pauseTransition.setOnFinished(f -> {
            getSkinnable().pseudoClassStateChanged(KLIntegerControlSkin.ERROR_PSEUDO_CLASS, false);
            errorLabel.setText(null);
        });
        subscription = subscription.and(errorLabel.textProperty().subscribe(nv -> {
            if (nv != null) {
                getSkinnable().pseudoClassStateChanged(KLIntegerControlSkin.ERROR_PSEUDO_CLASS, true);
                pauseTransition.playFromStart();
            } else {
                getSkinnable().pseudoClassStateChanged(KLIntegerControlSkin.ERROR_PSEUDO_CLASS, false);
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
            labelPrefWidth = errorLabel.prefWidth(-1);
            labelPrefHeight = errorLabel.prefHeight(labelPrefWidth);
            errorLabel.resizeRelocate(x, y, textField.getWidth(), labelPrefHeight);
        }
    }

    private static boolean hasExponent(String text) {
        if (text == null || text.isEmpty()) {
            return false;
        }
        return text.contains("e") || text.contains("E");
    }

    private static int exponentPosition(String text) {
        if (!hasExponent(text)) {
            return Integer.MAX_VALUE;
        }
        return text.contains("e") ? text.indexOf("e") : text.indexOf("E");
    }

    private static boolean endsWithExponent(String text) {
        if (text == null || text.isEmpty()) {
            return false;
        }
        return text.endsWith("e") || text.endsWith("E");
    }

    private static boolean hasSignedExponent(String text) {
        if (text == null || text.isEmpty()) {
            return false;
        }
        return text.contains("e-") || text.contains("E-") || text.endsWith("e+") || text.endsWith("E+");
    }

    private static boolean endsWithSignedExponent(String text) {
        if (text == null || text.isEmpty()) {
            return false;
        }
        return text.endsWith("e-") || text.endsWith("E-") || text.endsWith("e+") || text.endsWith("E+");
    }

}
