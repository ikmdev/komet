package dev.ikm.komet.kview.mvvm.view;

import dev.ikm.komet.kview.mvvm.view.journal.JournalController;

import java.net.URL;
import java.util.Objects;

public class KViewResources {
    private KViewResources() {}

    public static URL journalFxml() {
        URL url = JournalController.class.getResource("journal.fxml");
        return Objects.requireNonNull(url, "Could not find journal.fxml");
    }
}