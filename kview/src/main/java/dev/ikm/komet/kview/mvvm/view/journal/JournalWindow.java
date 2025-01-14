package dev.ikm.komet.kview.mvvm.view.journal;

import dev.ikm.komet.layout.window.KlWindow;
import javafx.scene.Scene;
import javafx.stage.Stage;

public class JournalWindow extends Stage implements KlWindow {

    @Override
    public Scene scene() {
        return getScene();
    }
}
