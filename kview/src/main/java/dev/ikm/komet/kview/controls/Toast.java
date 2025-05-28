package dev.ikm.komet.kview.controls;

import javafx.animation.*;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.geometry.Point2D;
import javafx.scene.Node;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.layout.*;
import javafx.stage.Popup;
import javafx.stage.Window;
import javafx.util.Duration;

/**
 * A lightweight, animated Toast notification component for displaying transient feedback messages
 * in JavaFX applications. Toasts are non-blocking, visually prominent, and automatically dismiss
 * after a configurable duration.
 *
 * <p>This implementation supports:
 * <ul>
 *   <li>Success and failure status icons</li>
 *   <li>Custom message text</li>
 *   <li>Optional "UNDO" action button</li>
 *   <li>A close (✕) button to dismiss manually</li>
 * </ul>
 *
 * <p>Usage example:
 * <pre>{@code
 * toast
 *     .withUndoAction(event -> handleUndo());
 *     .show(Toast.Status.SUCCESS, "Data saved successfully!");
 * }</pre>
 *
 */
public class Toast {

    /**
     * Status enum indicating the type of message to display.
     */
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

    /**
     * Creates a new {@code Toast} bound to the given parent {@link Node}.
     * The Toast will appear slightly above the bottom center of this node’s window.
     *
     * @param parent the JavaFX node over which the toast will display
     */
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

    /**
     * Adds an {@link EventHandler} for the undo button.
     * If set, the undo button will be shown in the Toast.
     *
     * @param undoAction the handler to invoke when the undo button is clicked
     * @return this {@code Toast} instance for method chaining
     */
    public Toast withUndoAction(EventHandler<ActionEvent> undoAction) {
        undoButton.setOnAction(undoAction);
        return this;
    }

    /**
     * Displays the Toast with the given status and message.
     * The Toast will auto-dismiss after the default duration (10 seconds).
     *
     * @param status  the status icon to display (success or failure)
     * @param message the message text to show in the toast
     */
    public void show(Status status, String message) {
        show(status, message, DEFAULT_AUTO_DISMISS_MILLIS);
    }

    /**
     * Displays the Toast with the given status, message, and auto-dismiss duration.
     *
     * @param status            the status icon to display (success or failure)
     * @param message           the message text to show in the toast
     * @param autoDismissMillis the time in milliseconds after which the toast auto-dismisses
     */
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

        Point2D parentScreenPos = parent.localToScreen(parent.getBoundsInLocal().getMinX(), parent.getBoundsInLocal().getMaxY());
        // force layout pass to calculate dimensions
        toastContainer.applyCss();
        toastContainer.layout();

        double centerX = parentScreenPos.getX() + parent.getBoundsInParent().getWidth() / 2 - toastContainer.prefWidth(-1) / 2;
        double bottomY = parentScreenPos.getY() + parent.getBoundsInParent().getHeight() - 80;
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