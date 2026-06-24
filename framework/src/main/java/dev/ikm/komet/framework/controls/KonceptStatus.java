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
 * <p>The glyph colour is supplied entirely by CSS through {@link #styleClass()} (see the
 * {@code .koncept-status} rules in {@code komet.css}); this enum carries no inline styling.
 */
public enum KonceptStatus {

    /** No classification glyph is shown (e.g. role values, or a concept with no logical definition). */
    NONE(null, null, false),

    /** A navigation root — a concept with no parents. */
    ROOT("⊤", StyleClasses.KONCEPT_ROOT, false),

    /** A fully defined (equivalent) concept with a single parent. */
    DEFINED("≡", StyleClasses.KONCEPT_DEFINED, false),

    /** A fully defined (equivalent) concept with more than one parent. */
    DEFINED_MULTIPARENT("≡", StyleClasses.KONCEPT_DEFINED, true),

    /** A primitive (sub-class only) concept with a single parent. */
    PRIMITIVE("⊑", StyleClasses.KONCEPT_PRIMITIVE, false),

    /** A primitive (sub-class only) concept with more than one parent. */
    PRIMITIVE_MULTIPARENT("⊑", StyleClasses.KONCEPT_PRIMITIVE, true);

    /** The fork glyph ({@code ⋎}) appended after the classification glyph for multi-parent concepts. */
    public static final String MULTI_PARENT_GLYPH = "⋎";

    private final String glyph;
    private final StyleClasses styleClass;
    private final boolean multiParent;

    KonceptStatus(String glyph, StyleClasses styleClass, boolean multiParent) {
        this.glyph = glyph;
        this.styleClass = styleClass;
        this.multiParent = multiParent;
    }

    /**
     * The classification glyph this status displays.
     *
     * @return the glyph string ({@code ≡} defined, {@code ⊑} primitive, {@code ⊤} root), or
     *         {@code null} for {@link #NONE}
     */
    public String glyph() {
        return glyph;
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
        return glyph != null;
    }
}
