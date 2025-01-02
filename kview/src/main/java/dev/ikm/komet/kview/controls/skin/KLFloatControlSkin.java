package dev.ikm.komet.kview.controls.skin;

import dev.ikm.komet.kview.controls.KLFloatControl;
import javafx.animation.PauseTransition;
import javafx.css.PseudoClass;
import javafx.geometry.Insets;
import javafx.scene.control.Label;
import javafx.scene.control.SkinBase;
import javafx.scene.control.TextField;
import javafx.scene.control.TextFormatter;
import javafx.util.Duration;
import javafx.util.Subscription;
import javafx.util.converter.DoubleStringConverter;

import java.text.MessageFormat;
import java.util.ResourceBundle;
import java.util.regex.Pattern;

/**
 * Default skin implementation for the {@link KLFloatControl} control
 */
public class KLFloatControlSkin extends SkinBase<KLFloatControl> {

    private static final Pattern NUMERICAL_PATTERN = Pattern.compile("^[+-]?(\\d+([.]\\d*)?([eE][+-]?\\d+)?|[.]\\d+([eE][+-]?\\d+)?)$");
    private static final PseudoClass ERROR_PSEUDO_CLASS = PseudoClass.getPseudoClass("error");
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

        textField.setTextFormatter(new TextFormatter<>(new DoubleStringConverter(), null, change -> {
            errorLabel.setText(null);
            String oldText = change.getControlText();
            String newText = change.getControlNewText();
            String addedText = change.getText();

            // Valid change (even if number is still not valid):
            // - empty (null value)
            // - adding e/E if oldText didn't have e/E, and wasn't empty
            // - back when text ends in [-, e, e-, E, E-]
            // - pattern
            if (newText.isEmpty() ||
                    (hasExponent(newText) && !hasExponent(oldText) && !oldText.isEmpty()) ||
                    ("-".equals(addedText) && (oldText.isEmpty() || endsWithExponent(oldText))) ||
                    (addedText.isEmpty() && ("-".equals(newText) || hasExponent(newText) || hasNegativeExponent(newText))) ||
                    NUMERICAL_PATTERN.matcher(newText).matches()) {
                try {
                    double value = Double.parseDouble(newText);
                    // discard if we have a valid value, but it is infinite or NaN
                    if (Double.isInfinite(value) || Double.isNaN(value)) {
                        errorLabel.setText(MessageFormat.format(resources.getString("error.float.text"), newText));
                        return null;
                    }
                } catch (NumberFormatException nfe) {
                    // ignore
                }
                return change;
            }
            errorLabel.setText(MessageFormat.format(resources.getString("error.input.text"), addedText));
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
            // When text is null or has a valid expression but not a valid number yet:
            if (nv == null || nv.isEmpty() || "-".equals(nv) || endsWithExponent(nv) || endsWithNegativeExponent(nv)) {
                control.setValue(null);
            } else {
                try {
                    // only set when it is a valid number
                    double value = Double.parseDouble(nv);
                    control.setValue(value);
                } catch (NumberFormatException e) {
                    // ignore
                }
                control.setValue(null);
            }
            textChangedViaKeyEvent = false;
        }));

        final PauseTransition pauseTransition = new PauseTransition(Duration.seconds(1));
        pauseTransition.setOnFinished(f -> {
            getSkinnable().pseudoClassStateChanged(ERROR_PSEUDO_CLASS, false);
            errorLabel.setText(null);
        });
        subscription = subscription.and(errorLabel.textProperty().subscribe(nv -> {
            if (nv != null) {
                getSkinnable().pseudoClassStateChanged(ERROR_PSEUDO_CLASS, true);
                pauseTransition.playFromStart();
            } else {
                getSkinnable().pseudoClassStateChanged(ERROR_PSEUDO_CLASS, false);
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
            errorLabel.resizeRelocate(x, y, labelPrefWidth, labelPrefHeight);
        }
    }

    private static boolean hasExponent(String text) {
        if (text == null || text.isEmpty()) {
            return false;
        }
        return text.contains("e") || text.contains("E");
    }

    private static boolean endsWithExponent(String text) {
        if (text == null || text.isEmpty()) {
            return false;
        }
        return text.endsWith("e") || text.endsWith("E");
    }

    private static boolean hasNegativeExponent(String text) {
        if (text == null || text.isEmpty()) {
            return false;
        }
        return text.contains("e-") || text.contains("E-");
    }

    private static boolean endsWithNegativeExponent(String text) {
        if (text == null || text.isEmpty()) {
            return false;
        }
        return text.endsWith("e-") || text.endsWith("E-");
    }

}
