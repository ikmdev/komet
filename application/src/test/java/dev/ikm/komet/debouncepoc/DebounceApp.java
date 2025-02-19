package dev.ikm.komet.debouncepoc;

import javafx.animation.KeyFrame;
import javafx.animation.Timeline;
import javafx.application.Application;
import javafx.scene.Scene;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;
import javafx.util.Duration;

/**
 * proof of concept for debouncing in next generation Komet
 */
public class DebounceApp extends Application {

    public static void main(String[] args) {
        launch(args);
    }

    @Override
    public void start(Stage stage) throws Exception {
        System.setProperty("prism.lcdtext", "false"); // nicer fonts (not necessary for this sample)

        stage.setTitle("Debounce POC");

        VBox root = new VBox();
        TextField textField = new TextField("Hello World");
        Label labelNonDebounce = new Label("output: ");

        Label labelDebounce = new Label("debounce: ");

        root.getChildren().addAll(textField, labelNonDebounce, labelDebounce);

        Timeline timeline = new Timeline();

        KeyFrame keyFrame1 = new KeyFrame(Duration.millis(1000), (evt) -> {});

        timeline.getKeyFrames().addAll(keyFrame1);

        timeline.setOnFinished((evt) -> {
            System.out.println("write to database %s".formatted(textField.getText()));
            labelDebounce.setText(textField.getText());
        });
        textField.textProperty().subscribe(s -> {
            labelNonDebounce.setText(s); // without debouncing, just for display purposes
            timeline.playFromStart();
        });

        Scene scene = new Scene(root, 600, 400);

        // Show stage
        stage.setScene(scene);
        stage.show();
    }
}










