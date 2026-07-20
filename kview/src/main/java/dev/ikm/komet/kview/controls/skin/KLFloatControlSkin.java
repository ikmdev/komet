package dev.ikm.komet.kview.controls.skin;

import dev.ikm.komet.kview.controls.AutoCompleteTextField;
import dev.ikm.komet.kview.controls.KLFloatControl;
import javafx.animation.PauseTransition;
import javafx.css.PseudoClass;
import javafx.geometry.Insets;
import javafx.scene.control.Label;
import javafx.scene.control.TextFormatter;
import javafx.util.Subscription;

import java.util.List;
import java.util.Locale;
import java.util.ResourceBundle;
import java.util.regex.Pattern;

/**
 * Default skin implementation for the {@link KLFloatControl} control
 */
public class KLFloatControlSkin extends KLDebounceControlSkin<KLFloatControl> {

    private static final Pattern NUMERICAL_PATTERN = Pattern.compile("^[+-]?(\\d+([.]\\d*)?([eE][+-]?\\d+)?|[.]\\d+([eE][+-]?\\d+)?)$");
    private static final ResourceBundle resources = ResourceBundle.getBundle("dev.ikm.komet.kview.controls.float-control");

    private static final String INFINITY = "Infinity";
    private static final String NEGATIVE_INFINITY = "-Infinity";
    private static final String INFINITY_SYMBOL = "∞";
    private static final String NEGATIVE_INFINITY_SYMBOL = "-∞";

    /**
     * Pseudo-class set on the text field while it displays the infinity symbol, so CSS can
     * switch to a font that contains the glyph (the default Noto Sans doesn't, and the
     * fallback system font renders it small and faded).
     */
    private static final PseudoClass INFINITY_PSEUDO_CLASS = PseudoClass.getPseudoClass("infinity");

    private final Label titleLabel;

    private final Label errorLabel;

    private Subscription subscription;

    /**
     * Creates a new KLFloatControlSkin instance, installing the necessary child
     * nodes into the Control {@link javafx.scene.control.Control#getChildrenUnmodifiable() children} list, as
     * well as the necessary input mappings for handling key, mouse, etc. events.
     *
     * @param control The control that this skin should be installed onto.
     */
    public KLFloatControlSkin(KLFloatControl control) {
        super(control, new AutoCompleteTextField<String>());

        titleLabel = new Label();
        titleLabel.textProperty().bind(control.titleProperty());
        titleLabel.getStyleClass().add("editable-title-label");

        textField.promptTextProperty().bind(control.promptTextProperty());
        textField.getStyleClass().add("text-field");

        // suggest Infinity/-Infinity while the user types an infinity token,
        // both as a shortcut and to make the support for infinity discoverable
        AutoCompleteTextField<String> autoCompleteTextField = (AutoCompleteTextField<String>) textField;
        autoCompleteTextField.setCompleter(KLFloatControlSkin::infinitySuggestions);
        autoCompleteTextField.getPopupStyleClasses().add("float-popup");
        // must match the -fx-cell-size of the float-popup cells in float-control.css
        autoCompleteTextField.setSuggestionsNodeHeight(28);

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

            if (!change.isContentChange()) {
                // change can also be cursor location, so if no content change, then it is
                // something else like cursor location change
                return change;
            }

            // partial or complete infinity token (e.g. "-In", "inf", "Infinity", "∞"),
            // accepted as it is being typed
            if (isPartialInfinityToken(newText)) {
                return change;
            }

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
                // checking for exponent at the end of the newText allows the exponent
                // to be entered naturally
                if (newText.isEmpty() || "-".equals(newText) || "+".equals(newText) || endsWithExponent(newText)) {
                    return change;
                }
                try {
                    double value = Double.parseDouble(newText);
                    // discard if we have a valid value, but it is infinite or NaN
                    if (Double.isInfinite(value) || Double.isNaN(value)) {
                        errorLabel.setText(resources.getString("error.float.text"));
                        control.setShowError(true);
                        return null;
                    }
                } catch (NumberFormatException nfe) {
                    errorLabel.setText(resources.getString("error.float.text"));
                    control.setShowError(true);
                    return null;
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
            errorLabel.setText(resources.getString("error.float.text"));
            control.setShowError(true);
            return null;
        }));

        // value was set externally
        subscription = control.valueProperty().subscribe(nv -> textField.setText(toDisplayText(nv)));

        subscription = subscription.and(textField.textProperty().subscribe(nv ->
                textField.pseudoClassStateChanged(INFINITY_PSEUDO_CLASS,
                        nv != null && INFINITY_SYMBOL.equals(stripSign(nv)))));

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

    @Override
    protected void updateValueProperty() {
        String nv = textField.getText();
        KLFloatControl control = getSkinnable();
        if (nv == null || nv.isEmpty()) {
            // When new text is null or empty, reset control's value
            control.setValue(null);
        } else if (isInfinityToken(nv)) {
            control.setValue(nv.startsWith("-") ? Float.NEGATIVE_INFINITY : Float.POSITIVE_INFINITY);
        } else if (!("-".equals(nv) || "+".equals(nv) || endsWithExponent(nv) || endsWithSignedExponent(nv))) {
            try {
                // only set control's value when it is a valid number
                float value = Float.parseFloat(nv);
                control.setValue(value);
            } catch (NumberFormatException e) {
                // ignore, and keep control with its old value
            }
        }
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
        final double titleLabelHeight = titleLabel.prefHeight(-1);
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
        y += labelPrefHeight;

        double textFieldPrefHeight = textField.prefHeight(contentWidth);
        textField.resizeRelocate(x, y, contentWidth, textFieldPrefHeight);

        if (getSkinnable().isShowError()) {
            y += textFieldPrefHeight + 2;
            labelPrefWidth = errorLabel.prefWidth(-1);
            labelPrefHeight = errorLabel.prefHeight(labelPrefWidth);
            errorLabel.resizeRelocate(x, y, textField.getWidth(), labelPrefHeight);
        }
    }

    /**
     * Returns the text to show in the text field for a given value, using the
     * infinity symbol for infinite values.
     */
    private static String toDisplayText(Float value) {
        if (value == null) {
            return null;
        }
        if (value == Float.POSITIVE_INFINITY) {
            return INFINITY_SYMBOL;
        }
        if (value == Float.NEGATIVE_INFINITY) {
            return NEGATIVE_INFINITY_SYMBOL;
        }
        return value.toString();
    }

    /**
     * Returns true if the text is a complete infinity token: an optional sign
     * followed by "inf" or "infinity" (case-insensitive), or the infinity symbol.
     */
    private static boolean isInfinityToken(String text) {
        String unsigned = stripSign(text);
        String lower = unsigned.toLowerCase(Locale.ROOT);
        return "inf".equals(lower) || "infinity".equals(lower) || INFINITY_SYMBOL.equals(unsigned);
    }

    /**
     * Returns true if the text is a complete infinity token or a partially typed
     * one: an optional sign followed by a prefix of "infinity" (case-insensitive)
     * or by the infinity symbol.
     */
    private static boolean isPartialInfinityToken(String text) {
        String unsigned = stripSign(text);
        if (unsigned.isEmpty()) {
            return false;
        }
        return INFINITY_SYMBOL.equals(unsigned) || "infinity".startsWith(unsigned.toLowerCase(Locale.ROOT));
    }

    private static String stripSign(String text) {
        return text.startsWith("-") || text.startsWith("+") ? text.substring(1) : text;
    }

    /**
     * Completer for the auto-complete text field: while the user is typing an
     * infinity token, suggests "Infinity" and/or "-Infinity" depending on the sign
     * typed so far.
     */
    private static List<String> infinitySuggestions(String text) {
        if (text == null) {
            return List.of();
        }
        String unsigned = stripSign(text);
        if (unsigned.isEmpty() || INFINITY_SYMBOL.equals(unsigned) ||
                !"infinity".startsWith(unsigned.toLowerCase(Locale.ROOT))) {
            return List.of();
        }
        if (text.startsWith("-")) {
            return List.of(NEGATIVE_INFINITY);
        }
        if (text.startsWith("+")) {
            return List.of(INFINITY);
        }
        return List.of(INFINITY, NEGATIVE_INFINITY);
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
