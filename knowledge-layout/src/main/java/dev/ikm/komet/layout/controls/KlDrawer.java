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
package dev.ikm.komet.layout.controls;

import dev.ikm.komet.layout.KlArea;
import dev.ikm.komet.layout.controls.skin.KlDrawerSkin;
import javafx.beans.property.BooleanProperty;
import javafx.beans.property.ObjectProperty;
import javafx.beans.property.SimpleBooleanProperty;
import javafx.beans.property.SimpleObjectProperty;
import javafx.geometry.Side;
import javafx.scene.control.Control;
import javafx.scene.control.Skin;
import javafx.scene.layout.Region;
import javafx.util.Duration;

/**
 * A general-purpose, content-independent slide-out drawer. It docks to any one edge of its host
 * — {@link Side#TOP top}, {@link Side#RIGHT right}, {@link Side#BOTTOM bottom}, or {@link Side#LEFT left} —
 * and reveals or conceals a single piece of {@linkplain #contentProperty() content} by animating along the
 * axis implied by that side (width for {@code LEFT}/{@code RIGHT}, height for {@code TOP}/{@code BOTTOM}).
 *
 * <p>The drawer is the chrome primitive that replaces the kview {@code SlideOutTrayHelper} pattern: rather
 * than a stateless static helper hand-wired per window with a right-only, width-driven geometry, {@code KlDrawer}
 * is a first-class {@link Control} with an observable {@link #expandedProperty() expanded} state, a {@link #sideProperty()
 * side} that picks the animation axis, and a {@link KlDrawerSkin skin} that owns the reveal. It knows nothing about
 * what it contains — a settings area, an editing area, a timeline — so any {@link KlArea} can be placed in it.
 *
 * <p>The content is always kept attached to the scene graph (it is clipped, never removed), so a drag gesture
 * begun inside the drawer is never canceled by a collapse — the basis for the later drag-out recede behavior.
 *
 * @see KlDrawerSkin
 */
public class KlDrawer extends Control {

    /** The default style class applied to every {@code KlDrawer}. */
    public static final String DEFAULT_STYLE_CLASS = "kl-drawer";

    /** The default slide animation duration. */
    public static final Duration DEFAULT_SLIDE_DURATION = Duration.millis(200);

    /**
     * Constructs an empty drawer docked to the {@link Side#RIGHT right}, initially collapsed.
     */
    public KlDrawer() {
        getStyleClass().add(DEFAULT_STYLE_CLASS);
    }

    /**
     * Constructs a drawer docked to the given side hosting the given content, initially collapsed.
     *
     * @param side    the edge this drawer docks to (selects the animation axis)
     * @param content the content region to reveal, or {@code null}
     */
    public KlDrawer(Side side, Region content) {
        this();
        setSide(side);
        setContent(content);
    }

    // -----------------------------------------------------------------------------------------
    // side
    // -----------------------------------------------------------------------------------------

    private final ObjectProperty<Side> side = new SimpleObjectProperty<>(this, "side", Side.RIGHT);

    /**
     * The edge this drawer docks to. {@code LEFT}/{@code RIGHT} animate along the width; {@code TOP}/{@code BOTTOM}
     * along the height.
     *
     * @return the side property
     */
    public final ObjectProperty<Side> sideProperty() {
        return side;
    }

    /**
     * Returns the edge this drawer docks to.
     *
     * @return the current side
     */
    public final Side getSide() {
        return side.get();
    }

    /**
     * Sets the edge this drawer docks to.
     *
     * @param value the side
     */
    public final void setSide(Side value) {
        side.set(value);
    }

    /**
     * Indicates whether this drawer animates along the horizontal axis (i.e. docked {@code LEFT} or {@code RIGHT}).
     *
     * @return {@code true} for {@code LEFT}/{@code RIGHT}, {@code false} for {@code TOP}/{@code BOTTOM}
     */
    public final boolean isHorizontalAxis() {
        return getSide() == Side.LEFT || getSide() == Side.RIGHT;
    }

    // -----------------------------------------------------------------------------------------
    // content
    // -----------------------------------------------------------------------------------------

    private final ObjectProperty<Region> content = new SimpleObjectProperty<>(this, "content");

    /**
     * The single content region revealed by this drawer. Typically a {@link KlArea}'s
     * {@link KlArea#fxObject() fxObject}; the drawer never inspects what it holds.
     *
     * @return the content property
     */
    public final ObjectProperty<Region> contentProperty() {
        return content;
    }

    /**
     * Returns the content region, or {@code null} if none is set.
     *
     * @return the content region
     */
    public final Region getContent() {
        return content.get();
    }

    /**
     * Sets the content region.
     *
     * @param value the content region, or {@code null}
     */
    public final void setContent(Region value) {
        content.set(value);
    }

    /**
     * Sets the drawer's content to the given area's root node, a convenience for hosting a {@link KlArea}.
     *
     * @param area the area whose {@link KlArea#fxObject() fxObject} becomes this drawer's content
     */
    public final void setContent(KlArea<? extends Region> area) {
        content.set(area == null ? null : area.fxObject());
    }

    // -----------------------------------------------------------------------------------------
    // expanded
    // -----------------------------------------------------------------------------------------

    private final BooleanProperty expanded = new SimpleBooleanProperty(this, "expanded", false);

    /**
     * Whether the drawer is open. Toggling this drives the slide animation. (Persistence of this state is
     * handled by the hosting framework, not by the control itself.)
     *
     * @return the expanded property
     */
    public final BooleanProperty expandedProperty() {
        return expanded;
    }

    /**
     * Returns whether the drawer is currently open.
     *
     * @return {@code true} if open
     */
    public final boolean isExpanded() {
        return expanded.get();
    }

    /**
     * Sets whether the drawer is open.
     *
     * @param value {@code true} to open, {@code false} to close
     */
    public final void setExpanded(boolean value) {
        expanded.set(value);
    }

    /** Opens the drawer (equivalent to {@code setExpanded(true)}). */
    public final void open() {
        setExpanded(true);
    }

    /** Closes the drawer (equivalent to {@code setExpanded(false)}). */
    public final void close() {
        setExpanded(false);
    }

    /** Toggles the drawer between open and closed. */
    public final void toggle() {
        setExpanded(!isExpanded());
    }

    // -----------------------------------------------------------------------------------------
    // animated
    // -----------------------------------------------------------------------------------------

    private final BooleanProperty animated = new SimpleBooleanProperty(this, "animated", true);

    /**
     * Whether open/close transitions are animated. When {@code false}, the drawer snaps to its end state.
     *
     * @return the animated property
     */
    public final BooleanProperty animatedProperty() {
        return animated;
    }

    /**
     * Returns whether open/close transitions are animated.
     *
     * @return {@code true} if animated
     */
    public final boolean isAnimated() {
        return animated.get();
    }

    /**
     * Sets whether open/close transitions are animated.
     *
     * @param value {@code true} to animate
     */
    public final void setAnimated(boolean value) {
        animated.set(value);
    }

    // -----------------------------------------------------------------------------------------
    // Skin + stylesheet
    // -----------------------------------------------------------------------------------------

    /**
     * {@inheritDoc}
     */
    @Override
    protected Skin<?> createDefaultSkin() {
        return new KlDrawerSkin(this);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public String getUserAgentStylesheet() {
        return KlDrawer.class.getResource("kl-drawer.css").toExternalForm();
    }
}
