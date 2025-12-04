package dev.ikm.komet.kview.controls.skin;

import dev.ikm.komet.kview.controls.NotificationPopup;
import javafx.beans.value.ChangeListener;
import javafx.scene.Node;
import javafx.scene.control.Skin;
import javafx.scene.layout.VBox;

import java.util.Objects;

/**
 * A default {@link Skin} implementation for the {@link NotificationPopup} control.
 * <p>
 * This skin manages the display of a single content {@link Node} within a {@link VBox}.
 * It also listens for content changes on the {@code NotificationPopup} and updates
 * the displayed content accordingly.
 */
public class NotificationPopupSkin implements Skin<NotificationPopup> {

    /**
     * The associated {@link NotificationPopup} control that this skin is rendering.
     */
    private NotificationPopup control;

    /**
     * The container that holds the popup's content node.
     */
    private final VBox container;

    /**
     * A listener that updates the container when the content property changes.
     */
    private final ChangeListener<Node> contentListener;

    /**
     * Constructs a new skin for the specified {@link NotificationPopup} control.
     *
     * @param control the {@link NotificationPopup} to be skinned; must not be null
     * @throws NullPointerException if {@code control} is null
     */
    public NotificationPopupSkin(final NotificationPopup control) {
        this.control = Objects.requireNonNull(control, "Control cannot be null");

        // Create the container for holding the content
        container = new VBox();
        container.getStyleClass().add("notification-popup-container");

        // If the popup already has content, initialize the container's children
        if (control.getContent() != null) {
            container.getChildren().setAll(control.getContent());
        }

        // Listen for content changes on the control, update the container accordingly
        contentListener = (obs, oldContent, newContent) -> {
            if (newContent != null) {
                container.getChildren().setAll(newContent);
            } else {
                container.getChildren().clear();
            }
        };
        control.contentProperty().addListener(contentListener);
    }

    /**
     * {@inheritDoc}
     * <p>
     * Returns the underlying {@link NotificationPopup} that this skin is attached to.
     */
    @Override
    public NotificationPopup getSkinnable() {
        return control;
    }

    /**
     * {@inheritDoc}
     * <p>
     * Returns the root {@link Node} for this skin, which is a {@link VBox}
     * containing the popup's content.
     */
    @Override
    public Node getNode() {
        return container;
    }

    /**
     * {@inheritDoc}
     * <p>
     * Cleans up listeners and references when this skin is disposed.
     */
    @Override
    public void dispose() {
        if (control != null) {
            control.contentProperty().removeListener(contentListener);
        }
        this.control = null;
    }
}
