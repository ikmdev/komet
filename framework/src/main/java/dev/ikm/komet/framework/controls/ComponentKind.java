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

/**
 * The component-kind a badge is <em>honest</em> about — a different axis from the taxonomic
 * {@link KonceptStatus}. It is the safety layer for typed clipboard / drag-and-drop
 * (ike-issues#734): a badge must say <em>what kind</em> of component it carries so a description or
 * a stamp can never masquerade as a concept pill. See the design note {@code dev-component-badge}
 * (ike-issues#638) for the full scheme.
 *
 * <p><b>Asymmetric sigil scheme — "concept bare, everything else marked."</b> The default
 * ({@link #CONCEPT}) carries <em>no</em> sigil, so the eye learns "no mark = concept" and any mark
 * means "not a concept — look":
 *
 * <ul>
 *   <li>{@link #CONCEPT} — no sigil (the taxonomic ≡/⊑/⊤ glyph may still show; different axis).</li>
 *   <li>{@link #DESCRIPTION} — {@code D} (a semantic on a description pattern; the caller resolves
 *       this against the view <em>coordinate's</em> description patterns, never a hardcoded one).</li>
 *   <li>{@link #SEMANTIC} — {@code S} (any other semantic).</li>
 *   <li>{@link #PATTERN} — {@code P}.</li>
 *   <li>{@link #STAMP} — a {@link StampSigil pentagon-dots glyph} in a gray chip, never a name-pill.</li>
 *   <li>{@link #UNKNOWN} — {@code ?} (unresolvable, or a presentation-only badge). The guardrail:
 *       "no sigil" must mean <em>resolved and a concept</em>, never "did not check," so a kind that
 *       cannot be positively determined is {@code UNKNOWN}, not {@code CONCEPT}.</li>
 * </ul>
 *
 * <p><b>Accessibility — two channels, never colour alone.</b> Each kind carries a glyph/letter
 * <em>and</em> a {@link #colorHex() colour}. Only the {@link #STAMP} gray and the pentagon geometry
 * are locked by ike-issues#638; the D/S/P/? colours here are the proposed mapping.
 *
 * <p><b>Alarm is contextual.</b> This enum never escalates. In a context that expects a concept (a
 * concept slot, the assistant chip) the mere presence of any sigil <em>is</em> the alarm and the
 * host/drop-target escalates it; in mixed contexts the sigils are merely informative.
 *
 * <p><b>Cross-medium.</b> Kind is data: the glyph and colour are values (not JavaFX styling) so
 * every adapter — the JavaFX badge, the adoc {@code k:} macro, the Zulip PNG, the HTML/email
 * fragment — renders the same sigil (ike-issues#623).
 */
public enum ComponentKind {

    /** A concept — the bare default; rendered with no kind sigil. */
    CONCEPT(null, null, "Concept"),

    /** A semantic whose pattern is one of the view coordinate's description patterns. Amber {@code D}. */
    DESCRIPTION("D", "#b8860b", "Description"),

    /** Any semantic that is not a description. Green {@code S}. */
    SEMANTIC("S", "#3b8c2f", "Semantic"),

    /** A pattern. Violet {@code P}. */
    PATTERN("P", "#7a4fb5", "Pattern"),

    /**
     * A stamp (status · time · author · module · path). Rendered as the {@link StampSigil} pentagon
     * in a gray chip — never a name-pill. The gray {@value StampSigilGeometry#COLOR} is locked.
     */
    STAMP(null, StampSigilGeometry.COLOR, "Stamp"),

    /** Kind could not be positively determined (unresolvable id, presentation-only badge). Red {@code ?}. */
    UNKNOWN("?", "#b00020", "Unknown");

    private final String glyph;
    private final String colorHex;
    private final String accessibleName;

    ComponentKind(String glyph, String colorHex, String accessibleName) {
        this.glyph = glyph;
        this.colorHex = colorHex;
        this.accessibleName = accessibleName;
    }

    /**
     * The single-letter sigil for this kind, or {@code null} when the kind has no letter glyph
     * ({@link #CONCEPT}, which is bare, and {@link #STAMP}, which uses the pentagon).
     *
     * @return the letter sigil ({@code D}/{@code S}/{@code P}/{@code ?}), or {@code null}
     */
    public String glyph() {
        return glyph;
    }

    /**
     * The sigil colour as a web hex string — a data value, so every medium adapter renders the same
     * colour. {@code null} for {@link #CONCEPT}, which has no sigil.
     *
     * @return the colour hex (e.g. {@code #b8860b}), or {@code null}
     */
    public String colorHex() {
        return colorHex;
    }

    /**
     * A human-readable kind name for tooltips and assistive technology (the non-colour, non-glyph
     * accessibility channel).
     *
     * @return the accessible kind name
     */
    public String accessibleName() {
        return accessibleName;
    }

    /**
     * Whether this is the bare default ({@link #CONCEPT}) — the only kind that shows no sigil.
     *
     * @return {@code true} for {@link #CONCEPT}
     */
    public boolean isBare() {
        return this == CONCEPT;
    }

    /**
     * Whether this kind renders the {@link StampSigil} pentagon rather than a letter glyph.
     *
     * @return {@code true} for {@link #STAMP}
     */
    public boolean isStamp() {
        return this == STAMP;
    }

    /**
     * Whether this kind renders a single-letter sigil ({@code D}/{@code S}/{@code P}/{@code ?}).
     *
     * @return {@code true} when {@link #glyph()} is non-null
     */
    public boolean hasLetterGlyph() {
        return glyph != null;
    }

    /**
     * Whether this kind shows any sigil at all (everything except the bare {@link #CONCEPT}).
     *
     * @return {@code true} for every kind except {@link #CONCEPT}
     */
    public boolean hasSigil() {
        return this != CONCEPT;
    }
}
