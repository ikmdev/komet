package dev.ikm.komet.kleditorapp.view;

import dev.ikm.komet.kview.controls.Toast;
import javafx.scene.Parent;

public class KLToastManager {
    private static Toast toast;

    public KLToastManager(Parent toastParentReference) {
        toast = new Toast(toastParentReference);
    }

    public static Toast toast() { return toast; }
}