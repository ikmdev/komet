package dev.ikm.komet.kview.controls.skin;

import javafx.scene.control.Control;
import javafx.scene.control.SkinBase;
import javafx.scene.control.TextField;

/**
 * Base skin of the controls that edit their value in a text field. The control's value follows the
 * text as it is edited, so whatever watches the value (a form's Submit button, say) reacts to the
 * first keystroke; the text follows the value when it is set from outside, and when the text field
 * loses focus, which puts text that isn't a value (a half-typed number) back to the value's own.
 */
public abstract class KLBaseTextFieldControlSkin<C extends Control> extends SkinBase<C> {

    protected final TextField textField;

    /** Set while the text is copied to the value or the value to the text, so one doesn't echo the other. */
    private boolean syncing;

    /**
     * Constructor for all SkinBase instances.
     *
     * @param control The control for which this Skin should attach to.
     */
    protected KLBaseTextFieldControlSkin(C control) {
        this(control, new TextField());
    }

    /**
     * Constructor that allows subclasses to supply their own text field
     * implementation (e.g. an {@code AutoCompleteTextField}).
     *
     * @param control The control for which this Skin should attach to.
     * @param textField The text field to use as the editing area of this skin.
     */
    protected KLBaseTextFieldControlSkin(C control, TextField textField) {
        super(control);
        control.setFocusTraversable(false);

        this.textField = textField;

        textField.textProperty().subscribe(() -> {
            if (syncing) {
                return;
            }
            syncing = true;
            updateValueProperty();
            syncing = false;
        });

        textField.focusedProperty().subscribe(() -> {
            if (!textField.isFocused()) {
                refreshText();
            }
        });
    }

    /**
     * Shows the control's value in the text field. Subclasses call this when the value changes; it
     * does nothing while the value is being taken from the text, which would rewrite what the user
     * is typing (e.g. "1." into "1.0").
     */
    protected final void refreshText() {
        if (syncing) {
            return;
        }
        syncing = true;
        textField.setText(getValueText());
        syncing = false;
    }

    /** Sets the control's value from the text field's text, if the text is a value. */
    protected abstract void updateValueProperty();

    /** The control's value as the text field shows it. */
    protected abstract String getValueText();
}
