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

    /** No classification glyph is shown (e.g. role values, or a concept with no logical definition). */
    NONE(network.ike.docs.konceptcore.KonceptStatus.NONE, null, false),

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
     * @return the glyph string ({@code ≡} defined, {@code ⊑} primitive, {@code ⊤} root), or
     *         {@code null} for {@link #NONE}
     */
    public String glyph() {
        return core.glyph();
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
     * Whether this status renders a visible classification glyph.
     *
     * @return {@code true} for every status except {@link #NONE}
     */
    public boolean hasGlyph() {
        return core.hasGlyph();
    }
}
