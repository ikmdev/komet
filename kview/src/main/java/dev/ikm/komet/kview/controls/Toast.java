package dev.ikm.komet.kview.controls;

import javafx.animation.*;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.scene.Node;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.layout.*;
import javafx.stage.Popup;
import javafx.stage.Window;
import javafx.util.Duration;

public class Toast {

    public enum Status {
        SUCCESS, FAILURE
    }

    private static final int DEFAULT_AUTO_DISMISS_MILLIS = 10_000;

    private final Node parent;
    private final Popup popup;
    private final HBox toastContainer;
    private final StackPane iconContainer;
    private final Region iconRegion;
    private final Label messageLabel;
    private final Button undoButton;
    private final StackPane closeContainer;
    private final Button closeButton;

    public Toast(Node parent) {
        this.parent = parent;

        toastContainer = new HBox();
        toastContainer.getStyleClass().add("toast");
        toastContainer.setOpacity(0);

        iconRegion = new Region();

        iconContainer = new StackPane();
        iconContainer.getChildren().setAll(iconRegion);

        messageLabel = new Label();
        messageLabel.getStyleClass().add("message");

        undoButton = new Button("UNDO");
        undoButton.getStyleClass().add("undo");

        closeButton = new Button("✕");
        closeButton.getStyleClass().add("close");

        closeContainer = new StackPane();
        closeContainer.getChildren().setAll(closeButton);
        closeContainer.getStyleClass().add("close-container");

        Region spacer = new Region();
        HBox.setHgrow(spacer, Priority.ALWAYS);

        toastContainer.getChildren().addAll(iconContainer, messageLabel, spacer, undoButton, closeContainer);
        toastContainer.getStylesheets().add(Toast.class.getResource("toast.css").toExternalForm());

        popup = new Popup();
        popup.getContent().add(toastContainer);
        popup.setAutoFix(true);
        popup.setAutoHide(false);
        popup.setHideOnEscape(true);

        closeButton.setOnAction(e -> popup.hide());
    }

    public Toast withUndoAction(EventHandler<ActionEvent> undoAction) {
        undoButton.setOnAction(undoAction);
        return this;
    }

    public void show(Status status, String message) {
        show(status, message, DEFAULT_AUTO_DISMISS_MILLIS);
    }

    public void show(Status status, String message, int autoDismissMillis) {
        // Update content and style
        iconRegion.getStyleClass().setAll("icon");
        if (status == Status.SUCCESS) {
            iconRegion.getStyleClass().add("icon-success");
        } else {
            iconRegion.getStyleClass().add("icon-failure");
        }

        messageLabel.setText(message);

        boolean showUndo = undoButton.getOnAction() != null;
        undoButton.setVisible(showUndo);
        undoButton.setManaged(showUndo);

        // Position relative to parent node
        Window window = parent.getScene().getWindow();
        double centerX = window.getX() + parent.getScene().getWidth() / 2 - toastContainer.getWidth() / 2;
        double bottomY = window.getY() + parent.getScene().getHeight() - 80;
        popup.show(window, centerX, bottomY);

        // Reset position and opacity for animation
        toastContainer.setTranslateY(30);
        toastContainer.setOpacity(0);

        // Entrance animation
        TranslateTransition slideIn = new TranslateTransition(Duration.millis(250), toastContainer);
        slideIn.setFromY(30);
        slideIn.setToY(0);
        slideIn.setInterpolator(Interpolator.EASE_OUT);

        FadeTransition fadeIn = new FadeTransition(Duration.millis(250), toastContainer);
        fadeIn.setFromValue(0);
        fadeIn.setToValue(1.0);
        fadeIn.setInterpolator(Interpolator.EASE_OUT);

        new ParallelTransition(slideIn, fadeIn).play();

        FadeTransition fadeOut = new FadeTransition(Duration.millis(400), toastContainer);
        fadeOut.setFromValue(1);
        fadeOut.setToValue(0);
        fadeOut.setOnFinished(_ -> popup.hide());

        // Auto-dismiss
        PauseTransition autoDismiss = new PauseTransition(Duration.millis(autoDismissMillis));
        autoDismiss.setOnFinished(_ -> fadeOut.play());
        autoDismiss.play();
    }
}