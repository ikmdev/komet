package dev.ikm.komet.kview.controls.skin;

import dev.ikm.komet.kview.controls.KLIntegerControl;
import javafx.scene.control.TextField;
import javafx.scene.control.TextFormatter;

import java.util.function.UnaryOperator;

public class KLIntegerControlSkin extends KLStringControlSkin<KLIntegerControl> {

    /**
     * @param control The KLIntegerControl to which this skin will attach
     */
    public KLIntegerControlSkin(KLIntegerControl control) {
        super(control);

        initNumericalTextField(textField);
    }

    private void initNumericalTextField(TextField textField) {
        textField.setPromptText("Enter Numerical Value Only");

        UnaryOperator<TextFormatter.Change> filter = change -> {
            String newText = change.getControlNewText();
            if (newText.isEmpty() || newText.matches("\\d+")) {
                return change;
            }
            return null;
        };
        TextFormatter<Integer> formatter = new TextFormatter<>(filter);
        textField.setTextFormatter(formatter);
    }
}