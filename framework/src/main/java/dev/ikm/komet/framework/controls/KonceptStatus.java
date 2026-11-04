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
package dev.ikm.komet.framework.controls;

import dev.ikm.komet.framework.StyleClasses;

/**
 * The taxonomic classification a {@link KonceptBadge} renders as a small status glyph.
 *
 * <p>Historically this meaning lived in the navigator/axiom taxonomy icon (see
 * {@code dev.ikm.komet.framework.panel.axiom.AxiomView#computeGraphic}). In the refreshed
 * presentation the {@link dev.ikm.komet.framework.Identicon LifeHash identicon} takes the old
 * icon's slot, so the classification is preserved here as a leading glyph instead: {@code ≡}
 * for a fully defined (equivalent) concept, {@code ⊑} for a primitive (sub-class only) concept,
 * and {@code ⊤} for a navigation root. A concept with more than one parent additionally carries
 * the {@value #MULTI_PARENT_GLYPH} fork glyph.
 *
 * <p>Since the ike-issues#742 design amendment the status vocabulary — glyphs, colours,
 * accessible names — is <em>single-sourced</em> from the shared, JavaFX-free
 * {@link network.ike.docs.konceptcore.KonceptStatus koncept-core KonceptStatus} that every
 * medium renders (adoc, PNG, HTML, JavaFX), so the on-screen badge and a generated document
 * cannot disagree about what a status looks like. This enum remains the JavaFX-side shape —
 * the status × multi-parent product that the badge's CSS style classes key on — and delegates
 * the vocabulary to the core enum via {@link #core()}.
 *
 * <p>The glyph colour on screen is supplied by CSS through {@link #styleClass()} (see the
 * {@code .koncept-status} rules in {@code komet.css}, which mirror the core enum's hex values);
 * a renderer with no stylesheet (the drag glyph snapshot) reads the colours from
 * {@link #core()} directly.
 */
public enum KonceptStatus {

    /** No mark applies: a role value, a component that is not a concept, or a status hidden by the settings. */
    NONE(network.ike.docs.konceptcore.KonceptStatus.NONE, null, false),

    /**
     * A concept with no stated definition. It has no copula to lead with, so it leads with a
     * {@value #UNDEFINED_GLYPH} instead of standing bare: a bare badge would read as nothing
     * known about the concept, when what is known is that it was never defined. koncept-core
     * has no such status yet, so the glyph and colour are held here, and generated media still
     * draw these concepts bare.
     */
    UNDEFINED(network.ike.docs.konceptcore.KonceptStatus.NONE, StyleClasses.KONCEPT_UNDEFINED, false),

    /** A navigation root — a concept with no parents. */
    ROOT(network.ike.docs.konceptcore.KonceptStatus.ROOT, StyleClasses.KONCEPT_ROOT, false),

    /** A fully defined (equivalent) concept with a single parent. */
    DEFINED(network.ike.docs.konceptcore.KonceptStatus.DEFINED, StyleClasses.KONCEPT_DEFINED, false),

    /** A fully defined (equivalent) concept with more than one parent. */
    DEFINED_MULTIPARENT(network.ike.docs.konceptcore.KonceptStatus.DEFINED,
            StyleClasses.KONCEPT_DEFINED, true),

    /** A primitive (sub-class only) concept with a single parent. */
    PRIMITIVE(network.ike.docs.konceptcore.KonceptStatus.PRIMITIVE,
            StyleClasses.KONCEPT_PRIMITIVE, false),

    /** A primitive (sub-class only) concept with more than one parent. */
    PRIMITIVE_MULTIPARENT(network.ike.docs.konceptcore.KonceptStatus.PRIMITIVE,
            StyleClasses.KONCEPT_PRIMITIVE, true);

    /**
     * The fork glyph ({@code ⋎}) appended after the classification glyph for multi-parent
     * concepts — the koncept-core constant, so every medium appends the same fork.
     */
    public static final String MULTI_PARENT_GLYPH =
            network.ike.docs.konceptcore.KonceptStatus.MULTI_PARENT_GLYPH;

    /** The letter a concept with no stated definition leads with. */
    public static final String UNDEFINED_GLYPH = "C";

    /** The colour of {@link #UNDEFINED_GLYPH}, as a web hex string: the badge's name colour. */
    public static final String UNDEFINED_COLOR_HEX = "#2a5a8a";

    private final network.ike.docs.konceptcore.KonceptStatus core;
    private final StyleClasses styleClass;
    private final boolean multiParent;

    KonceptStatus(network.ike.docs.konceptcore.KonceptStatus core, StyleClasses styleClass,
                  boolean multiParent) {
        this.core = core;
        this.styleClass = styleClass;
        this.multiParent = multiParent;
    }

    /**
     * The shared, medium-neutral status vocabulary this JavaFX status delegates to — the
     * single source of the glyph, colour hex, and accessible name (ike-issues#742/#861).
     *
     * @return the koncept-core status, never {@code null}
     */
    public network.ike.docs.konceptcore.KonceptStatus core() {
        return core;
    }

    /**
     * The classification glyph this status displays.
     *
     * @return the glyph string ({@code ≡} defined, {@code ⊑} primitive, {@code ⊤} root,
     *         {@value #UNDEFINED_GLYPH} undefined), or {@code null} for {@link #NONE}
     */
    public String glyph() {
        return this == UNDEFINED ? UNDEFINED_GLYPH : core.glyph();
    }

    /**
     * The glyph's colour as a web hex string, for a renderer no stylesheet reaches.
     *
     * @return the colour hex, or {@code null} for {@link #NONE}
     */
    public String colorHex() {
        return this == UNDEFINED ? UNDEFINED_COLOR_HEX : core.colorHex();
    }

    /**
     * Whether the glyph is a letter rather than a logic symbol. A letter is set in the text face,
     * as a kind sigil is; the bundled glyph face holds the logic symbols.
     *
     * @return {@code true} for {@link #UNDEFINED}
     */
    public boolean isLetter() {
        return this == UNDEFINED;
    }

    /**
     * The CSS style class that colours the classification glyph.
     *
     * @return the {@link StyleClasses} modifier applied to the glyph, or {@code null} for {@link #NONE}
     */
    public StyleClasses styleClass() {
        return styleClass;
    }

    /**
     * The full accessible reading of this status — the copula's accessible name, with the
     * multi-parent reading appended when this is a multi-parent status. The non-colour,
     * non-glyph accessibility channel, suitable for a tooltip.
     *
     * @return the accessible text, e.g. {@code "Primitive · Multiple parents"}
     */
    public String accessibleText() {
        return multiParent
                ? core.accessibleName() + " · "
                        + network.ike.docs.konceptcore.KonceptStatus.MULTI_PARENT_ACCESSIBLE_NAME
                : core.accessibleName();
    }

    /**
     * Whether the concept has more than one parent, in which case the badge appends the
     * {@value #MULTI_PARENT_GLYPH} fork glyph after the classification glyph.
     *
     * @return {@code true} if this is a multi-parent status
     */
    public boolean isMultiParent() {
        return multiParent;
    }

    /**
     * This status with the multiple-parents fork dropped: the single-parent status of the same
     * classification, or this status itself when it carries no fork.
     *
     * @return the single-parent counterpart, never {@code null}
     */
    public KonceptStatus singleParent() {
        return switch (this) {
            case DEFINED_MULTIPARENT -> DEFINED;
            case PRIMITIVE_MULTIPARENT -> PRIMITIVE;
            default -> this;
        };
    }

    /**
     * This status as the glyph settings show it: {@link #NONE} while
     * the definition status is hidden, the {@link #singleParent() single-parent} status while the
     * fork is, and this status when both are shown. Renderers keep the resolved status and draw
     * the shown one, so nothing needs re-resolving when a setting changes.
     *
     * @param definitionStatus whether the classification glyph is shown
     * @param multipleParents  whether the multiple-parents fork is shown
     * @return the status to draw, never {@code null}
     */
    public KonceptStatus shown(boolean definitionStatus, boolean multipleParents) {
        if (!definitionStatus) {
            return NONE;
        }
        return multipleParents ? this : singleParent();
    }

    /**
     * Whether this status renders a visible classification glyph.
     *
     * @return {@code true} for every status except {@link #NONE}
     */
    public boolean hasGlyph() {
        return this == UNDEFINED || core.hasGlyph();
    }
}
