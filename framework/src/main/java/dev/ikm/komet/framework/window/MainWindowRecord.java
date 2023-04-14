package dev.ikm.komet.framework.window;

import javafx.fxml.FXMLLoader;
import javafx.scene.layout.BorderPane;

import java.io.IOException;

public record MainWindowRecord(KometStageController controller, BorderPane root) {
    
    public static MainWindowRecord make() throws IOException {
        FXMLLoader kometStageLoader = new FXMLLoader(MainWindowRecord.class.getResource("KometStageScene.fxml"));
        BorderPane kometRoot = kometStageLoader.load();
        return new MainWindowRecord(kometStageLoader.getController(), kometRoot);
    }
}
