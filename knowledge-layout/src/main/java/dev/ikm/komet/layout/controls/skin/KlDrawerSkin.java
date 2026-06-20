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
package dev.ikm.komet.layout.controls.skin;

import dev.ikm.komet.layout.controls.KlDrawer;
import javafx.animation.Interpolator;
import javafx.animation.KeyFrame;
import javafx.animation.KeyValue;
import javafx.animation.Timeline;
import javafx.beans.property.DoubleProperty;
import javafx.beans.property.SimpleDoubleProperty;
import javafx.geometry.Side;
import javafx.scene.control.SkinBase;
import javafx.scene.layout.Region;
import javafx.scene.shape.Rectangle;

/**
 * Skin for {@link KlDrawer}. It hosts the drawer's content in a clipped viewport and reveals it by
 * animating a single {@link #revealFraction reveal fraction} from {@code 0} (collapsed) to {@code 1} (open).
 *
 * <p><b>Geometry.</b> The reveal is a growing viewport with the content anchored to the docked edge; the
 * content keeps its full preferred cross-axis size and is <em>clipped</em> to the drawer's current box, so the
 * portion nearest the docked edge appears first and the box grows inward as it opens:
 * <ul>
 *   <li>{@code LEFT}/{@code RIGHT} — the box width animates {@code 0 → contentPrefWidth}; height fills.</li>
 *   <li>{@code TOP}/{@code BOTTOM} — the box height animates {@code 0 → contentPrefHeight}; width fills.</li>
 * </ul>
 *
 * <p>The content is never removed from the scene graph during a collapse — only clipped — so a drag gesture
 * started inside the drawer is not canceled when it recedes.
 */
public class KlDrawerSkin extends SkinBase<KlDrawer> {

    /** Reveal progress: {@code 0} fully collapsed, {@code 1} fully open. Drives both layout and clip. */
    private final DoubleProperty revealFraction = new SimpleDoubleProperty(this, "revealFraction", 0.0);

    /** Clip that confines the content to the drawer's current box, bound to the control's size. */
    private final Rectangle clip = new Rectangle();

    /** The running slide animation, if any. */
    private Timeline slide;

    /** The content currently installed as a child, tracked so it can be swapped out. */
    private Region installedContent;

    /**
     * Creates a skin for the given drawer, installs its content and clip, and wires the reveal to the
     * control's {@code side}, {@code content}, and {@code expanded} properties.
     *
     * @param control the drawer being skinned
     */
    public KlDrawerSkin(KlDrawer control) {
        super(control);

        clip.widthProperty().bind(control.widthProperty());
        clip.heightProperty().bind(control.heightProperty());
        control.setClip(clip);

        revealFraction.set(control.isExpanded() ? 1.0 : 0.0);
        installContent(control.getContent());

        // Re-layout when the fraction changes (the pref size is fraction-dependent, so this also re-measures).
        revealFraction.addListener((obs, oldValue, newValue) -> control.requestLayout());
        control.sideProperty().addListener((obs, oldValue, newValue) -> control.requestLayout());
        control.contentProperty().addListener((obs, oldValue, newValue) -> installContent(newValue));
        control.expandedProperty().addListener((obs, oldValue, newValue) -> animateTo(newValue));
    }

    /**
     * Swaps the installed content node, preserving the current reveal state.
     *
     * @param content the new content, or {@code null}
     */
    private void installContent(Region content) {
        if (installedContent != null) {
            getChildren().remove(installedContent);
        }
        installedContent = content;
        if (content != null) {
            getChildren().add(content);
        }
        getSkinnable().requestLayout();
    }

    /**
     * Animates (or, when the control is not animated, snaps) the reveal toward the given open state.
     *
     * @param open {@code true} to open, {@code false} to close
     */
    private void animateTo(boolean open) {
        double target = open ? 1.0 : 0.0;
        if (slide != null) {
            slide.stop();
            slide = null;
        }
        if (!getSkinnable().isAnimated() || installedContent == null) {
            revealFraction.set(target);
            return;
        }
        if (open) {
            // Resolve looked-up colors before the clipped content is revealed (JDK-8093516).
            getSkinnable().applyCss();
            installedContent.applyCss();
        }
        slide = new Timeline(new KeyFrame(KlDrawer.DEFAULT_SLIDE_DURATION,
                new KeyValue(revealFraction, target, Interpolator.EASE_BOTH)));
        slide.play();
    }

    /**
     * {@inheritDoc}
     * <p>Anchors the content to the docked edge at its full preferred cross-axis size; the clip reveals the
     * portion within the current box.
     */
    @Override
    protected void layoutChildren(double contentX, double contentY, double contentWidth, double contentHeight) {
        Region content = installedContent;
        if (content == null) {
            return;
        }
        double cw = content.prefWidth(-1);
        double ch = content.prefHeight(-1);
        switch (getSkinnable().getSide()) {
            case LEFT -> content.resizeRelocate(contentX, contentY, cw, contentHeight);
            case RIGHT -> content.resizeRelocate(contentX + contentWidth - cw, contentY, cw, contentHeight);
            case TOP -> content.resizeRelocate(contentX, contentY, contentWidth, ch);
            case BOTTOM -> content.resizeRelocate(contentX, contentY + contentHeight - ch, contentWidth, ch);
        }
    }

    /**
     * {@inheritDoc}
     * <p>On the animation axis the size is the content's preferred size scaled by the reveal fraction (so the
     * drawer occupies {@code 0} when collapsed and the full content size when open); on the cross axis it is the
     * content's preferred size.
     */
    @Override
    protected double computePrefWidth(double height, double topInset, double rightInset,
                                      double bottomInset, double leftInset) {
        Region content = installedContent;
        double cw = content == null ? 0.0 : content.prefWidth(-1);
        double axisScale = getSkinnable().isHorizontalAxis() ? revealFraction.get() : 1.0;
        return leftInset + rightInset + cw * axisScale;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    protected double computePrefHeight(double width, double topInset, double rightInset,
                                       double bottomInset, double leftInset) {
        Region content = installedContent;
        double ch = content == null ? 0.0 : content.prefHeight(-1);
        double axisScale = getSkinnable().isHorizontalAxis() ? 1.0 : revealFraction.get();
        return topInset + bottomInset + ch * axisScale;
    }

    /**
     * {@inheritDoc}
     * <p>Allows the animation axis to collapse to nothing so a closed drawer reserves no space.
     */
    @Override
    protected double computeMinWidth(double height, double topInset, double rightInset,
                                     double bottomInset, double leftInset) {
        return getSkinnable().isHorizontalAxis()
                ? leftInset + rightInset
                : computePrefWidth(height, topInset, rightInset, bottomInset, leftInset);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    protected double computeMinHeight(double width, double topInset, double rightInset,
                                      double bottomInset, double leftInset) {
        return getSkinnable().isHorizontalAxis()
                ? computePrefHeight(width, topInset, rightInset, bottomInset, leftInset)
                : topInset + bottomInset;
    }

    /**
     * {@inheritDoc}
     * <p>Lets a host stretch the drawer along the docked edge (the cross axis) while pinning the animation axis
     * to its revealed size.
     */
    @Override
    protected double computeMaxWidth(double height, double topInset, double rightInset,
                                     double bottomInset, double leftInset) {
        return getSkinnable().isHorizontalAxis()
                ? computePrefWidth(height, topInset, rightInset, bottomInset, leftInset)
                : Double.MAX_VALUE;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    protected double computeMaxHeight(double width, double topInset, double rightInset,
                                      double bottomInset, double leftInset) {
        return getSkinnable().isHorizontalAxis()
                ? Double.MAX_VALUE
                : computePrefHeight(width, topInset, rightInset, bottomInset, leftInset);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void dispose() {
        if (slide != null) {
            slide.stop();
            slide = null;
        }
        clip.widthProperty().unbind();
        clip.heightProperty().unbind();
        getSkinnable().setClip(null);
        if (installedContent != null) {
            getChildren().remove(installedContent);
            installedContent = null;
        }
        super.dispose();
    }
}
