package dev.ikm.komet.kview.mvvm.view.kleditor;

import javafx.stage.Stage;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;

import java.io.IOException;

public class KLEditorWindow extends Stage {
    public KLEditorWindow() {
        FXMLLoader loader = new FXMLLoader(KLEditorWindow.class.getResource("KLEditorMainScreen.fxml"));
        Parent root = null;
        try {
            root = loader.load();
        } catch (IOException e) {
            throw new RuntimeException(e);
        }

        Scene scene = new Scene(root);
        scene.getStylesheets().add(KLEditorWindow.class.getResource("kl-editor-styles.css").toExternalForm());

        setTitle("Knowledge Layout Editor");
        setScene(scene);
    }
}