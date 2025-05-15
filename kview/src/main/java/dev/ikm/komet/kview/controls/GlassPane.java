/*
 * Copyright © 2015 Integrated Knowledge Management (support@ikm.dev)
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package dev.ikm.komet.kview.controls;

import javafx.animation.FadeTransition;
import javafx.beans.property.DoubleProperty;
import javafx.beans.property.ReadOnlyBooleanProperty;
import javafx.beans.property.ReadOnlyBooleanWrapper;
import javafx.geometry.HPos;
import javafx.geometry.Insets;
import javafx.geometry.VPos;
import javafx.scene.Node;
import javafx.scene.layout.Background;
import javafx.scene.layout.BackgroundFill;
import javafx.scene.layout.CornerRadii;
import javafx.scene.layout.Pane;
import javafx.scene.layout.Region;
import javafx.scene.paint.Color;
import javafx.util.Duration;
import javafx.util.Subscription;

import java.util.List;
import java.util.Objects;

import static dev.ikm.komet.kview.fxutils.FXUtils.DEFAULT_ANIMATION_DURATION;

/**
 * A glass pane component that provides a semi-transparent overlay (scrim) on top of a root pane.
 * The glass pane can be shown/hidden with fade animations and can contain additional content
 * that appears above the scrim.
 * <p>
 * The glass pane automatically resizes itself to match the dimensions of the root pane.
 */
public class GlassPane extends Pane {

    private static final String DEFAULT_STYLE_CLASS = "glass-pane";

    private final Pane rootPane;
    private final Region scrimRegion;
    private boolean performingLayout = false;

    private final FadeTransition showTransition;
    private final FadeTransition hideTransition;
    
    // Width subscription for tracking root pane width changes
    private Subscription widthSubscription;

    // Height subscription for tracking root pane height changes
    private Subscription heightSubscription;

    /**
     * Creates a new glass pane that will overlay the specified root pane.
     *
     * @param rootPane The pane that this glass pane will overlay
     * @throws NullPointerException if rootPane is null
     */
    public GlassPane(Pane rootPane) {
        Objects.requireNonNull(rootPane, "Root pane cannot be null");

        getStyleClass().add(DEFAULT_STYLE_CLASS);
        setPickOnBounds(false);

        this.rootPane = rootPane;

        // Initialize the scrim region (semi-transparent overlay)
        scrimRegion = new Region();
        scrimRegion.setPickOnBounds(false);
        scrimRegion.setBackground(new Background(new BackgroundFill(Color.BLACK, CornerRadii.EMPTY, Insets.EMPTY)));
        scrimRegion.setManaged(false);
        setScrimOpacity(0.0);

        getChildren().add(scrimRegion);

        // Initialize transitions
        showTransition = new FadeTransition(DEFAULT_ANIMATION_DURATION, scrimRegion);
        showTransition.setFromValue(0.0);
        showTransition.setToValue(0.5);

        hideTransition = new FadeTransition(DEFAULT_ANIMATION_DURATION, scrimRegion);
        hideTransition.setFromValue(0.5);
        hideTransition.setToValue(0.0);
    }

    /**
     * Gets the current opacity of the scrim region.
     *
     * @return The current opacity value (0.0-1.0)
     */
    public double getScrimOpacity() {
        return scrimRegion.getOpacity();
    }

    /**
     * Sets the current opacity of the scrim region.
     *
     * @param value The opacity value (0.0-1.0)
     * @throws IllegalArgumentException if value is outside the range [0.0, 1.0]
     */
    public void setScrimOpacity(double value) {
        if (value < 0.0 || value > 1.0) {
            throw new IllegalArgumentException("Opacity must be between 0.0 and 1.0");
        }
        scrimRegion.setOpacity(value);
    }

    /**
     * Gets the opacity property of the scrim region.
     *
     * @return The opacity property
     */
    public DoubleProperty scrimOpacityProperty() {
        return scrimRegion.opacityProperty();
    }

    /**
     * Sets the target opacity value for the scrim when fully shown.
     *
     * @param opacity The target opacity (0.0-1.0)
     * @throws IllegalArgumentException if opacity is outside the range [0.0, 1.0]
     */
    public void setTargetScrimOpacity(double opacity) {
        if (opacity < 0.0 || opacity > 1.0) {
            throw new IllegalArgumentException("Opacity must be between 0.0 and 1.0");
        }
        showTransition.setToValue(opacity);
        hideTransition.setFromValue(opacity);
    }

    /**
     * Gets the target opacity value for the scrim when fully shown.
     *
     * @return The target opacity
     */
    public double getTargetScrimOpacity() {
        return showTransition.getToValue();
    }

    /**
     * A property representing the showing state of the glass pane.
     */
    private ReadOnlyBooleanWrapper showing;
    
    /**
     * Determines if the glass pane is currently showing.
     *
     * @return true if the glass pane is showing, false otherwise
     */
    public boolean isShowing() {
        return showing != null && showing.get();
    }

    /**
     * Sets the showing state of the glass pane.
     *
     * @param value true to show the glass pane, false to hide it
     */
    private void setShowing(boolean value) {
        showingPropertyImpl().set(value);
    }

    /**
     * Gets the read-only showing property of the glass pane.
     *
     * @return The showing property
     */
    public ReadOnlyBooleanProperty showingProperty() {
        return showingPropertyImpl().getReadOnlyProperty();
    }

    /**
     * Gets or creates the showing property implementation.
     *
     * @return The showing property wrapper
     */
    private ReadOnlyBooleanWrapper showingPropertyImpl() {
        if (showing == null) {
            showing = new ReadOnlyBooleanWrapper(this, "showing") {
                @Override
                protected void invalidated() {
                    if (get()) { // showing
                        // Clean up existing subscriptions if they exist
                        cleanupSubscriptions();

                        // Create new subscriptions
                        widthSubscription = rootPane.widthProperty().subscribe(() -> setWidth(rootPane.getWidth()));
                        heightSubscription = rootPane.heightProperty().subscribe(() -> setHeight(rootPane.getHeight()));

                        // Set initial dimensions
                        setWidth(rootPane.getWidth());
                        setHeight(rootPane.getHeight());

                        // Add to root pane
                        rootPane.getChildren().add(GlassPane.this);
                    } else {
                        // Remove from root pane
                        rootPane.getChildren().remove(GlassPane.this);

                        // Clean up subscriptions
                        cleanupSubscriptions();
                    }
                }
            };
        }
        return showing;
    }

    /**
     * Cleans up width and height subscriptions to prevent memory leaks.
     */
    private void cleanupSubscriptions() {
        if (widthSubscription != null) {
            widthSubscription.unsubscribe();
            widthSubscription = null;
        }

        if (heightSubscription != null) {
            heightSubscription.unsubscribe();
            heightSubscription = null;
        }
    }

    /**
     * Shows the glass pane with a fade-in animation.
     */
    public void show() {
        setShowing(true);
        showTransition.play();
    }

    /**
     * Hides the glass pane with a fade-out animation.
     * The glass pane will be removed from the scene after the animation completes.
     */
    public void hide() {
        hideTransition.setOnFinished(event -> setShowing(false));
        hideTransition.play();
    }

    /**
     * Gets the duration of the show/hide animations.
     *
     * @return The animation duration
     */
    public Duration getAnimationDuration() {
        return showTransition.getDuration();
    }

    /**
     * Sets the duration for both show and hide animations.
     *
     * @param duration The duration to set
     * @throws NullPointerException if duration is null
     */
    public void setAnimationDuration(Duration duration) {
        Objects.requireNonNull(duration, "Animation duration cannot be null");
        showTransition.setDuration(duration);
        hideTransition.setDuration(duration);
    }

    /**
     * Adds a node to be displayed on top of the scrim.
     *
     * @param node The node to add
     * @throws NullPointerException if node is null
     */
    public void addContent(Node node) {
        Objects.requireNonNull(node, "Content node cannot be null");
        getChildren().add(node);
    }

    /**
     * Removes a node from the glass pane.
     *
     * @param node The node to remove
     * @return true if the node was removed, false otherwise
     */
    public boolean removeContent(Node node) {
        return getChildren().remove(node);
    }

    @Override
    public void requestLayout() {
        if (performingLayout) {
            return;
        }
        super.requestLayout();
    }

    @Override
    protected void layoutChildren() {
        performingLayout = true;
        final double width = getWidth();
        final double height = getHeight();

        final List<Node> managed = getManagedChildren();
        for (Node node : managed) {
            if (node.isResizable()) {
                node.autosize();
            }
            positionInArea(node, 0, 0, width, height, 0, HPos.CENTER, VPos.CENTER);
        }

        scrimRegion.resizeRelocate(0.0, 0.0, width, height);
        performingLayout = false;
    }
}