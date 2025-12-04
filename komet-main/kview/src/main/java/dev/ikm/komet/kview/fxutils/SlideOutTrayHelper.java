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
package dev.ikm.komet.kview.fxutils;

import javafx.animation.KeyFrame;
import javafx.animation.KeyValue;
import javafx.animation.Timeline;
import javafx.scene.Node;
import javafx.scene.layout.Pane;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import static dev.ikm.komet.kview.fxutils.FXUtils.DEFAULT_ANIMATION_DURATION;
import static dev.ikm.komet.kview.fxutils.ViewportHelper.clipChildren;
import static javafx.animation.Interpolator.EASE_IN;

/**
 * Slide out tray helper is a utility class to create JavaFX slide out tray capability.
 * Pressing the search button on a sidebar control will slide out a tray panel allowing the user to search.
 * <p>
 * Example usage:
 * <pre>{@code
 * // Create tray and panel
 * Pane trayPane = new Pane();
 * Pane searchPanel = new SearchPanel();
 *
 * // Setup the tray
 * SlideOutTrayHelper.setupSlideOutTrayPane(searchPanel, trayPane);
 *
 * // Show the tray
 * SlideOutTrayHelper.slideOut(trayPane);
 *
 * // Hide the tray
 * SlideOutTrayHelper.slideIn(trayPane);
 * }</pre>
 */
public interface SlideOutTrayHelper {

    /**
     * Logger instance for debugging slide animations and state changes.
     */
    Logger LOG = LoggerFactory.getLogger(SlideOutTrayHelper.class);

    /**
     * The ability to setup a slide out tray pane that will contain only one child node (nodePanel).
     *
     * @param nodePanel The pane to be added to the slide out tray pane.
     * @param trayPane  The tray pane that will hold a single child node.
     */
    static void setupSlideOutTrayPane(Pane nodePanel, Pane trayPane) {
        final double width = nodePanel.getWidth();
        trayPane.getChildren().add(nodePanel);
        clipChildren(trayPane, 0);
        nodePanel.setLayoutX(-width);
        trayPane.setMaxWidth(0);
    }

    /**
     * Performs an animated slide out effect with the tray pane using default animation settings.
     * <p>
     * This is a convenience method that calls {@link #slideOut(Pane, boolean)} with
     * animation enabled.
     *
     * @param trayPane A tray pane consisting of one child (the panel to display).
     * @see #slideOut(Pane, boolean)
     */
    static void slideOut(Pane trayPane) {
        slideOut(trayPane, true);
    }

    /**
     * Performs a slide out effect with the tray pane, optionally animated.
     * <p>
     * When sliding out:
     * <ul>
     *   <li>The panel moves from left (hidden) to its normal position</li>
     *   <li>The tray pane expands from 0 width to the panel's width</li>
     *   <li>Animation uses ease-in interpolation for smooth motion</li>
     * </ul>
     *
     * @param trayPane A tray pane consisting of one child (the panel to display).
     * @param animated If true, the slide out will be animated over {@link FXUtils#DEFAULT_ANIMATION_DURATION}.
     *                 If false, the panel will appear immediately in its final position.
     */
    static void slideOut(Pane trayPane, boolean animated) {
        final Node panel = trayPane.getChildren().getFirst();
        final double width = panel.getBoundsInLocal().getWidth();

        if (animated) {
            // Start from hidden position
            panel.setLayoutX(-width);
            trayPane.setMaxWidth(0);

            // Animate to visible position
            Timeline timeline = new Timeline(new KeyFrame(DEFAULT_ANIMATION_DURATION,
                    _ -> {
                        LOG.info("slide out complete");
                        panel.setLayoutX(0);
                        trayPane.setMaxWidth(-1);
                    },
                    new KeyValue(panel.layoutXProperty(), 0, EASE_IN),
                    new KeyValue(trayPane.maxWidthProperty(), width, EASE_IN)
            )
            );
            timeline.play();
        } else {
            // Set final state immediately
            panel.setLayoutX(0);
            trayPane.setMaxWidth(-1);
        }
    }

    /**
     * Performs an animated slide in effect with the tray pane using default animation settings.
     * <p>
     * This is a convenience method that calls {@link #slideIn(Pane, boolean)} with
     * animation enabled.
     *
     * @param trayPane A tray pane consisting of one child (the panel to hide).
     * @see #slideIn(Pane, boolean)
     */
    static void slideIn(Pane trayPane) {
        slideIn(trayPane, true);
    }

    /**
     * Performs a slide in effect with the tray pane, optionally animated.
     * <p>
     * When sliding in:
     * <ul>
     *   <li>The panel moves from its normal position to the left (hidden)</li>
     *   <li>The tray pane shrinks from the panel's width to 0</li>
     *   <li>Animation uses default interpolation for smooth motion</li>
     * </ul>
     *
     * @param trayPane A tray pane consisting of one child (the panel to hide).
     * @param animated If true, the slide in will be animated over {@link FXUtils#DEFAULT_ANIMATION_DURATION}.
     *                 If false, the panel will disappear immediately.
     */
    static void slideIn(Pane trayPane, boolean animated) {
        final Node panel = trayPane.getChildren().getFirst();
        final double width = panel.getBoundsInLocal().getWidth();

        if (animated) {
            // Start from visible position
            trayPane.setMaxWidth(width);
            panel.setLayoutX(0);

            // Animate to hidden position
            Timeline timeline = new Timeline(new KeyFrame(DEFAULT_ANIMATION_DURATION,
                    _ -> {
                        LOG.info("slide in complete");
                        panel.setLayoutX(-width);
                        trayPane.setMaxWidth(0);
                    },
                    new KeyValue(panel.layoutXProperty(), -width),
                    new KeyValue(trayPane.maxWidthProperty(), 0)));
            timeline.play();
        } else {
            // Set final state immediately
            panel.setLayoutX(-width);
            trayPane.setMaxWidth(0);
        }
    }

    /**
     * Performs an animated slide out effect with the tray pane beside the owning panel.
     * <p>
     * This is a convenience method that calls {@link #slideOut(Pane, Pane, boolean)} with
     * animation enabled.
     *
     * @param trayPane    A tray pane consisting of one child (the panel to display).
     * @param owningPanel The owning panel that will be resized to accommodate the tray pane.
     * @see #slideOut(Pane, Pane, boolean)
     */
    static void slideOut(Pane trayPane, Pane owningPanel) {
        slideOut(trayPane, owningPanel, true);
    }

    /**
     * Performs a slide out effect with the tray pane beside (right side) the owning pane.
     * <p>
     * When sliding out:
     * <ul>
     *   <li>The tray panel slides from left (hidden) to its normal position</li>
     *   <li>The owning panel's width increases by the tray panel's width</li>
     *   <li>Both animations occur simultaneously for a coordinated effect</li>
     * </ul>
     *
     * @param trayPane    A tray pane consisting of one child (the panel to display).
     * @param owningPanel The owning panel in display will have the tray pane attached to its right side.
     * @param animated    If true, the slide out will be animated over {@link FXUtils#DEFAULT_ANIMATION_DURATION}.
     *                    If false, both panels will adjust immediately.
     */
    static void slideOut(Pane trayPane, Pane owningPanel, boolean animated) {
        final Node panel = trayPane.getChildren().getFirst();
        final double width = panel.getBoundsInLocal().getWidth();

        if (animated) {
            // Start from hidden position
            panel.setLayoutX(-width);
            trayPane.setMaxWidth(0);

            // Animate to visible position
            Timeline timeline = new Timeline(new KeyFrame(DEFAULT_ANIMATION_DURATION,
                    _ -> {
                        LOG.info("slide out complete");
                        panel.setLayoutX(0);
                        trayPane.setMaxWidth(-1);
                    },
                    new KeyValue(panel.layoutXProperty(), 0, EASE_IN),
                    new KeyValue(owningPanel.prefWidthProperty(), owningPanel.getPrefWidth() + width, EASE_IN),
                    new KeyValue(trayPane.maxWidthProperty(), width, EASE_IN)));
            timeline.play();
        } else {
            // Set final state immediately
            panel.setLayoutX(0);
            trayPane.setMaxWidth(-1);
            owningPanel.setPrefWidth(owningPanel.getPrefWidth() + width);
        }
    }

    /**
     * Performs an animated slide in effect with the tray pane beside the owning panel.
     * <p>
     * This is a convenience method that calls {@link #slideIn(Pane, Pane, boolean)} with
     * animation enabled.
     *
     * @param trayPane    A tray pane consisting of one child (the panel to hide).
     * @param owningPanel The owning panel that will be resized when the tray hides.
     * @see #slideIn(Pane, Pane, boolean)
     */
    static void slideIn(Pane trayPane, Pane owningPanel) {
        slideIn(trayPane, owningPanel, true);
    }

    /**
     * Performs a slide in effect with the tray pane beside (right side) the owning pane.
     * <p>
     * When sliding in:
     * <ul>
     *   <li>The tray panel slides from its normal position to the left (hidden)</li>
     *   <li>The owning panel's width decreases by the tray panel's width</li>
     *   <li>Both animations occur simultaneously for a coordinated effect</li>
     * </ul>
     *
     * @param trayPane    A tray pane consisting of one child (the panel to hide).
     * @param owningPanel The owning panel in display will have the tray pane attached to its right side.
     * @param animated    If true, the slide in will be animated over {@link FXUtils#DEFAULT_ANIMATION_DURATION}.
     *                    If false, both panels will adjust immediately.
     */
    static void slideIn(Pane trayPane, Pane owningPanel, boolean animated) {
        final Node panel = trayPane.getChildren().getFirst();
        final double width = panel.getBoundsInLocal().getWidth();

        if (animated) {
            // Start from visible position
            trayPane.setMaxWidth(width);
            panel.setLayoutX(0);

            // Animate to hidden position
            Timeline timeline = new Timeline(new KeyFrame(DEFAULT_ANIMATION_DURATION,
                    _ -> {
                        LOG.info("slide in complete");
                        panel.setLayoutX(-width);
                        trayPane.setMaxWidth(0);
                    },
                    new KeyValue(panel.layoutXProperty(), -width),
                    new KeyValue(owningPanel.prefWidthProperty(), owningPanel.getPrefWidth() - width, EASE_IN),
                    new KeyValue(trayPane.maxWidthProperty(), 0)));
            timeline.play();
        } else {
            // Set final state immediately
            panel.setLayoutX(-width);
            trayPane.setMaxWidth(0);
            owningPanel.setPrefWidth(owningPanel.getPrefWidth() - width);
        }
    }

    /**
     * Checks if the tray pane is currently in the open (visible) state.
     *
     * @param trayPane The tray pane to check.
     * @return true if the tray is open (visible), false if it is closed (hidden).
     * @see #isClosed(Pane)
     */
    static boolean isOpen(Pane trayPane) {
        return !isClosed(trayPane);
    }

    /**
     * Checks if the tray pane is currently in the closed (hidden) state.
     *
     * @param trayPane The tray pane to check.
     * @return true if the tray is closed (hidden), false if it is open (visible).
     * @see #isOpen(Pane)
     */
    static boolean isClosed(Pane trayPane) {
        return trayPane.maxWidthProperty().get() == 0;
    }
}
