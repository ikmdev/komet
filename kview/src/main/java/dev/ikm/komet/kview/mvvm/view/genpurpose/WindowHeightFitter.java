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
package dev.ikm.komet.kview.mvvm.view.genpurpose;

import dev.ikm.komet.kview.controls.KLWorkspace;
import javafx.animation.Interpolator;
import javafx.animation.KeyFrame;
import javafx.animation.KeyValue;
import javafx.animation.Timeline;
import javafx.beans.value.ObservableDoubleValue;
import javafx.scene.Parent;
import javafx.scene.control.ScrollPane;
import javafx.scene.layout.Region;

import static dev.ikm.komet.kview.fxutils.FXUtils.DEFAULT_ANIMATION_DURATION;

/**
 * Grows a KL window, animated, to the height its open properties panel needs to show in full — so
 * a form's fields can be filled in without scrolling the panel (komet-desktop#184) — and gives the
 * height back as the panel closes.
 *
 * <p>The window only ever grows while the panel is open: content that needs less (the confirmation
 * shown after a submit, a shorter form) leaves the height alone, so the window doesn't bounce
 * between consecutive edits. Growth stops at {@link #maxGrownHeight()}.
 */
public final class WindowHeightFitter {

    /**
     * Gap kept between the bottom of a grown window and the bottom of the workspace's visible area
     * (see {@link #maxGrownHeight()}).
     */
    public static final double BOTTOM_GAP = 16;

    private final Region window;
    private final Region tray;
    private final ObservableDoubleValue requiredTrayHeight;

    /** The preferred height the window was grown to, or a negative value while it isn't grown. */
    private double grownHeight = -1;
    /**
     * The window's preferred height from before it was grown — {@code USE_COMPUTED_SIZE} for a
     * window sized to its content — which {@link #restorePreviousHeight()} goes back to.
     */
    private double prefHeightBeforeGrowth;
    /** The running animation and the height it is heading to; null when none runs. */
    private Timeline animation;
    private double animationTarget;
    /** The preferred height the window is left with once {@link #animation} ends. */
    private double prefHeightAfterAnimation;

    /**
     * @param window             the window's root, whose preferred height sizes the window
     * @param tray               the tray holding the properties panel; it fills the window's height
     *                           below the window's chrome
     * @param requiredTrayHeight the height the tray needs to show the panel's content in full
     */
    public WindowHeightFitter(Region window, Region tray, ObservableDoubleValue requiredTrayHeight) {
        this.window = window;
        this.tray = tray;
        this.requiredTrayHeight = requiredTrayHeight;
    }

    /**
     * Grows the window to fit what the tray requires now. Does nothing when the window is tall
     * enough already, or can't grow any further.
     */
    public void growToFitProperties() {
        if (window.getHeight() <= 0) {
            return;
        }

        // Everything in the window that isn't the tray — the toolbar, the hint strips, the borders —
        // stays as tall as it is, whatever height the window and the tray share at the moment.
        final double chromeHeight = window.getHeight() - tray.getHeight();
        final double target = Math.ceil(Math.min(maxGrownHeight(), chromeHeight + requiredTrayHeight.get()));
        final double current = animation != null ? animationTarget : window.getHeight();
        if (target <= current + 1) {
            return;
        }

        finishAnimationNow();
        if (grownHeight < 0 || window.getPrefHeight() != grownHeight) {
            // Not grown yet — or grown and resized by the user since, which makes the height
            // they chose the one to go back to.
            prefHeightBeforeGrowth = window.getPrefHeight();
        }
        grownHeight = target;
        animate(target, target);
    }

    /**
     * Gives back the height {@link #growToFitProperties()} added. A window the user has resized in the meantime
     * keeps their height.
     */
    public void restorePreviousHeight() {
        if (grownHeight < 0) {
            return;
        }
        finishAnimationNow();
        final boolean resizedSince = window.getPrefHeight() != grownHeight;
        grownHeight = -1;
        if (resizedSince) {
            return;
        }

        if (prefHeightBeforeGrowth > 0) {
            animate(prefHeightBeforeGrowth, prefHeightBeforeGrowth);
        } else {
            // The window was sized to its content: close to what the content asks for now (it may
            // have changed while the panel was open), then hand the sizing back to the content.
            window.setPrefHeight(Region.USE_COMPUTED_SIZE);
            final double contentHeight = Math.min(KLWorkspace.MAX_WINDOW_HEIGHT, window.prefHeight(-1));
            animate(contentHeight, Region.USE_COMPUTED_SIZE);
        }
    }

    /**
     * Changes the height of a window that carries a preferred height of its own by {@code delta},
     * as something in it (a collapsing section) gives up or takes height. For a grown window the
     * delta goes into the grown height and the height {@link #restorePreviousHeight()} goes back to alike; a
     * window that was sized to its content follows the change on its own once restored.
     */
    public void changeHeightBy(double delta) {
        finishAnimationNow();
        final double height = window.getPrefHeight();
        final double newHeight = Math.min(KLWorkspace.MAX_WINDOW_HEIGHT, height + delta);
        if (height == grownHeight) {
            grownHeight = newHeight;
            if (prefHeightBeforeGrowth > 0) {
                prefHeightBeforeGrowth = Math.min(KLWorkspace.MAX_WINDOW_HEIGHT, prefHeightBeforeGrowth + delta);
            }
        }
        window.setPrefHeight(newHeight);
    }

    /**
     * Ends a running grow or restore animation right away, leaving the window at the height the
     * animation was heading to. Call it before reading or changing the window's preferred height:
     * mid-animation that height is a passing in-between value, and the animation would overwrite
     * any change on its next frame. Does nothing when no animation is running.
     */
    public void finishAnimationNow() {
        if (animation == null) {
            return;
        }
        animation.stop();
        animation = null;
        window.setPrefHeight(prefHeightAfterAnimation);
    }

    /**
     * The most the window is grown to: the workspace's maximum window height and, within it, no
     * further than the bottom of the workspace's visible area — height added below it would only
     * trade scrolling the panel for scrolling the workspace.
     */
    private double maxGrownHeight() {
        for (Parent ancestor = window.getParent(); ancestor != null; ancestor = ancestor.getParent()) {
            if (ancestor instanceof ScrollPane workspaceScrollPane) {
                final double visibleBottomInScene = workspaceScrollPane
                        .localToScene(0, workspaceScrollPane.getViewportBounds().getHeight()).getY();
                final double visibleBelowWindowTop = window.sceneToLocal(0, visibleBottomInScene).getY();
                return Math.min(KLWorkspace.MAX_WINDOW_HEIGHT, visibleBelowWindowTop - BOTTOM_GAP);
            }
        }
        return KLWorkspace.MAX_WINDOW_HEIGHT;
    }

    /**
     * Animates the window from the height it has to {@code toHeight}, leaving it with
     * {@code prefHeightAfter} as its preferred height — {@code toHeight} itself, or
     * {@code USE_COMPUTED_SIZE} to hand a window back to sizing by its content.
     */
    private void animate(double toHeight, double prefHeightAfter) {
        finishAnimationNow();

        // A window sized to its content has no preferred height to animate from — start from the
        // height it has.
        window.setPrefHeight(window.getHeight());
        animationTarget = toHeight;
        prefHeightAfterAnimation = prefHeightAfter;
        animation = new Timeline(new KeyFrame(DEFAULT_ANIMATION_DURATION,
                _ -> finishAnimationNow(),
                new KeyValue(window.prefHeightProperty(), toHeight, Interpolator.EASE_BOTH)));
        animation.play();
    }
}
