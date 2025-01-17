package dev.ikm.komet.kview.controls;

import dev.ikm.komet.kview.controls.skin.NotificationPopupSkin;
import javafx.animation.FadeTransition;
import javafx.beans.property.ObjectProperty;
import javafx.beans.property.SimpleObjectProperty;
import javafx.event.EventHandler;
import javafx.event.WeakEventHandler;
import javafx.geometry.Point2D;
import javafx.scene.Node;
import javafx.scene.control.PopupControl;
import javafx.scene.control.Skin;
import javafx.stage.Window;
import javafx.stage.WindowEvent;
import javafx.util.Duration;

import java.util.Objects;
import java.util.function.Supplier;

/**
 * A specialized {@link PopupControl} that provides fade-in and fade-out animations
 * when shown or hidden. This {@code NotificationPopup} automatically hides when the
 * {@code ESCAPE} key is pressed or when focus is lost (via {@link #setAutoHide(boolean)}).
 * <p>
 * The {@link #fadeInDuration} and {@link #fadeOutDuration} properties allow customization
 * of the animation durations. By default, they use a duration of 200 milliseconds.
 *
 * <p><b>Usage Example:</b></p>
 *
 * <pre>{@code
 * // Create notification content
 * Label label = new Label("Operation Successful!");
 *
 * // Create NotificationPopup with content
 * NotificationPopup notification = new NotificationPopup(label);
 *
 * // Configure durations (optional)
 * notification.setFadeInDuration(Duration.millis(300));
 * notification.setFadeOutDuration(Duration.millis(300));
 *
 * // Show notification
 * notification.show(someNode.getScene().getWindow());
 * }</pre>
 */
public class NotificationPopup extends PopupControl {

    /**
     * The default style class used by this notification popup.
     */
    private static final String DEFAULT_STYLE_CLASS = "notification-popup";

    /**
     * The default duration used for both fade-in and fade-out transitions.
     */
    private static final Duration DEFAULT_FADE_DURATION = Duration.millis(200);

    /**
     * The owner window of this popup. Once set, this popup will hide
     * if the owner window begins hiding, thus preventing a floating
     * popup without an owner and producing a cleaner user experience.
     */
    private Window ownerWindow;
    private final EventHandler<WindowEvent> closePopupOnOwnerWindowHiding = event -> ownerWindowHiding();
    private final WeakEventHandler<WindowEvent> weakClosePopupOnOwnerWindowHiding = new WeakEventHandler<>(closePopupOnOwnerWindowHiding);

    //-----------------------------------------------------------------------------------------
    // Constructors
    //-----------------------------------------------------------------------------------------

    /**
     * Creates a new {@code NotificationPopup} with default settings.
     * <p>
     * This constructor sets up the default style class, enables auto-hide,
     * and installs event filters/handlers to handle {@code ESCAPE} key events
     * and initiate fade-in animations when the popup is shown.
     */
    public NotificationPopup() {
        super();
        getStyleClass().add(DEFAULT_STYLE_CLASS);

        // Automatically hide the popup on focus loss or other triggers
        setAutoHide(true);

        // Hide on ESC key press
        setHideOnEscape(true);

        // Fade-in animation when window is shown
        addEventHandler(WindowEvent.WINDOW_SHOWN, e -> {
            Duration fadeInDuration = getFadeInDuration();
            if (fadeInDuration == null) {
                fadeInDuration = DEFAULT_FADE_DURATION;
            }

            if (fadeInDuration.greaterThan(Duration.ZERO)) {
                showFadeInAnimation(fadeInDuration);
            }

            // Listen for the owner window hiding event
            ownerWindow = getOwnerWindow();
            if (ownerWindow != null) {
                ownerWindow.addEventFilter(WindowEvent.WINDOW_HIDING, weakClosePopupOnOwnerWindowHiding);
            }
        });
    }

    /**
     * Creates a new {@code NotificationPopup} with the specified content {@link Node}.
     *
     * @param content the {@link Node} to be displayed as the content of this notification popup
     */
    public NotificationPopup(Node content) {
        this();
        setContent(content);
    }

    //-----------------------------------------------------------------------------------------
    // Content node property
    //-----------------------------------------------------------------------------------------

    private ObjectProperty<Node> content;

    /**
     * Returns the content {@link Node} of this notification popup.
     *
     * @return the content node, or {@code null} if none is set
     */
    public final Node getContent() {
        return (content == null) ? null : content.get();
    }

    /**
     * Sets the content {@link Node} of this notification popup.
     *
     * @param content the node to display as the notification content
     */
    public final void setContent(Node content) {
        contentProperty().set(content);
    }

    /**
     * The property representing the content {@link Node} of this notification popup.
     *
     * @return the {@link ObjectProperty} holding the notification’s content node
     */
    public final ObjectProperty<Node> contentProperty() {
        if (content == null) {
            content = new SimpleObjectProperty<>(this, "content");
        }
        return content;
    }

    //-----------------------------------------------------------------------------------------
    // Fade-in duration property
    //-----------------------------------------------------------------------------------------

    private ObjectProperty<Duration> fadeInDuration;

    /**
     * Gets the duration of the fade-in animation when this notification popup is shown.
     *
     * @return the current fade-in duration; never {@code null}
     */
    public final Duration getFadeInDuration() {
        return fadeInDuration == null ? DEFAULT_FADE_DURATION : fadeInDuration.get();
    }

    /**
     * Sets the duration of the fade-in animation when this notification popup is shown.
     *
     * @param duration the desired fade-in duration; if {@code null},
     *                 the default duration is used
     */
    public final void setFadeInDuration(Duration duration) {
        fadeInDurationProperty().set(duration);
    }

    /**
     * The property representing the fade-in duration for this notification popup.
     *
     * @return the {@link ObjectProperty} holding the fade-in duration
     */
    public final ObjectProperty<Duration> fadeInDurationProperty() {
        if (fadeInDuration == null) {
            fadeInDuration = new SimpleObjectProperty<>(this, "fadeInDuration", DEFAULT_FADE_DURATION);
        }
        return fadeInDuration;
    }

    //-----------------------------------------------------------------------------------------
    // Fade-out duration property
    //-----------------------------------------------------------------------------------------

    private ObjectProperty<Duration> fadeOutDuration;

    /**
     * Gets the duration of the fade-out animation when this notification popup is hidden.
     *
     * @return the current fade-out duration; never {@code null}
     */
    public final Duration getFadeOutDuration() {
        return fadeOutDuration == null ? DEFAULT_FADE_DURATION : fadeOutDuration.get();
    }

    /**
     * Sets the duration of the fade-out animation when this notification popup is hidden.
     *
     * @param duration the desired fade-out duration; if {@code null},
     *                 the default duration is used
     */
    public final void setFadeOutDuration(Duration duration) {
        fadeOutDurationProperty().set(duration);
    }

    /**
     * The property representing the fade-out duration for this notification popup.
     *
     * @return the {@link ObjectProperty} holding the fade-out duration
     */
    public final ObjectProperty<Duration> fadeOutDurationProperty() {
        if (fadeOutDuration == null) {
            fadeOutDuration = new SimpleObjectProperty<>(this, "fadeOutDuration", DEFAULT_FADE_DURATION);
        }
        return fadeOutDuration;
    }

    //-----------------------------------------------------------------------------------------
    // Skin
    //-----------------------------------------------------------------------------------------

    /**
     * {@inheritDoc}
     */
    @Override
    protected Skin<?> createDefaultSkin() {
        return new NotificationPopupSkin(this);
    }

    //-----------------------------------------------------------------------------------------
    // Show / Hide with custom fade transitions
    //-----------------------------------------------------------------------------------------

    /**
     * Shows this notification popup relative to the given {@code ownerNode},
     * positioning it at a location supplied by the given {@code anchorPointSupplier}.
     * <p>
     * This method is useful for more complex positioning where you compute
     * an anchor point.
     *
     * @param ownerNode           the node that owns this popup; often used for
     *                            coordinate conversion or determining the owner window
     * @param anchorPointSupplier a supplier returning a {@link Point2D}
     *                            where the popup will be anchored (top-left corner)
     * @throws NullPointerException if {@code anchorPointSupplier} is {@code null}
     */
    public final void show(Node ownerNode, Supplier<Point2D> anchorPointSupplier) {
        Objects.requireNonNull(anchorPointSupplier, "Anchor point supplier cannot be null");

        final Point2D anchorPoint = anchorPointSupplier.get();
        super.show(ownerNode, anchorPoint.getX(), anchorPoint.getY());
    }

    /**
     * Hides this notification popup using the configured fade-out duration.
     * <p>
     * If the fade-out duration is zero or less, it hides immediately.
     */
    @Override
    public final void hide() {
        hide(getFadeOutDuration());
    }

    /**
     * Hides this notification popup using a custom fade-out duration.
     * <p>
     * If the specified duration is {@code null} or zero, it hides immediately.
     *
     * @param fadeOutDuration the duration to use for the fade-out animation;
     *                        if {@code null}, the default duration is used
     */
    public final void hide(Duration fadeOutDuration) {
        if (fadeOutDuration == null) {
            fadeOutDuration = DEFAULT_FADE_DURATION;
        }

        if (isShowing()) {
            if (fadeOutDuration.greaterThan(Duration.ZERO)) {
                showFadeOutAnimation(fadeOutDuration);
            } else {
                super.hide();
            }
        }
    }

    /**
     * Shows the fade-in animation for this notification popup’s skin node.
     *
     * @param fadeInDuration the duration of the fade-in animation
     */
    private void showFadeInAnimation(Duration fadeInDuration) {
        final Node skinNode = getSkin().getNode();
        if (skinNode == null) {
            return;
        }

        // Start at 0 opacity, then fade in
        skinNode.setOpacity(0.0);

        FadeTransition fadeIn = new FadeTransition(fadeInDuration, skinNode);
        fadeIn.setFromValue(0.0);
        fadeIn.setToValue(1.0);
        fadeIn.play();
    }

    /**
     * Shows the fade-out animation for this notification popup’s skin node,
     * then hides the control.
     *
     * @param fadeOutDuration the duration of the fade-out animation
     */
    private void showFadeOutAnimation(Duration fadeOutDuration) {
        final Node skinNode = getSkin().getNode();
        if (skinNode == null) {
            super.hide();
            return;
        }

        FadeTransition fadeOut = new FadeTransition(fadeOutDuration, skinNode);
        fadeOut.setFromValue(skinNode.getOpacity());
        fadeOut.setToValue(0.0);
        fadeOut.setOnFinished(evt -> super.hide());
        fadeOut.play();
    }

    /**
     * Invoked when the owner window begins hiding. This method hides
     * the notification popup immediately (no fade-out) and removes the
     * associated event filter from the owner window to prevent potential
     * memory leaks.
     */
    private void ownerWindowHiding() {
        hide(Duration.ZERO); // Hide immediately
        if (ownerWindow != null) {
            // Clean up the event filter registration
            ownerWindow.removeEventFilter(WindowEvent.WINDOW_HIDING, weakClosePopupOnOwnerWindowHiding);
        }
    }
}
