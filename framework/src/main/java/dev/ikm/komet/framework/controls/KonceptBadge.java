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
import network.ike.docs.konceptcore.KonceptKind;

import dev.ikm.komet.framework.Identicon;
import dev.ikm.komet.framework.StyleClasses;
import dev.ikm.komet.framework.dnd.KometClipboard;
import dev.ikm.komet.framework.dnd.KonceptDragGlyph;
import dev.ikm.komet.framework.dnd.KonceptDragSource;
import dev.ikm.komet.framework.graphics.SmallCapsFonts;
import dev.ikm.komet.framework.view.ViewProperties;
import dev.ikm.tinkar.common.id.IntIdList;
import dev.ikm.tinkar.common.id.IntIds;
import dev.ikm.tinkar.common.id.PublicId;
import dev.ikm.tinkar.common.service.PrimitiveData;
import dev.ikm.tinkar.coordinate.logic.PremiseType;
import dev.ikm.tinkar.coordinate.stamp.calculator.Latest;
import dev.ikm.tinkar.entity.EntityVersion;
import dev.ikm.tinkar.entity.graph.DiTreeEntity;
import dev.ikm.tinkar.terms.EntityFacade;
import dev.ikm.tinkar.terms.State;
import dev.ikm.tinkar.terms.TinkarTerm;
import javafx.css.PseudoClass;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Node;
import javafx.scene.control.Tooltip;
import javafx.scene.image.ImageView;
import javafx.scene.input.Dragboard;
import javafx.scene.input.TransferMode;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.paint.Color;
import javafx.scene.text.Font;
import javafx.scene.text.Text;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Locale;

/**
 * A reusable, CSS-driven badge that renders a single concept the way the AsciiDoc {@code k:}
 * Koncept chip does: the component's {@link Identicon LifeHash identicon} on the left, the
 * store-resolved name in a soft rounded small-caps pill, and a small leading status glyph that
 * preserves the taxonomic classification the navigator/axiom icon used to carry
 * (see {@link KonceptStatus}).
 *
 * <p>Because the identicon and the name are deterministic functions of the component's
 * {@link PublicId}, an on-screen badge matches the identicon and label shown for the same
 * component in generated documents (ike-issues#563). When the component's latest version is
 * inactive (retired) in the view, the name is struck through — a real strikethrough on the name's
 * {@code Text} node, driven by {@code komet.css} — and shown in the retired colour (#586).
 *
 * <p>Colour and the retired strikethrough live in {@code komet.css} ({@code .koncept-chip},
 * {@code .koncept-label}, {@code .koncept-status} and the
 * {@code .koncept-defined}/{@code -primitive}/{@code -multiparent}/{@code -root}
 * colour modifiers), mirroring the AsciiDoc {@code koncept.css}; this control adds only the style
 * classes and the {@code inactive} pseudo-class. JavaFX CSS has no {@code font-variant}, so true
 * small caps come from the bundled dedicated family ({@link SmallCapsFonts} — capitals full height,
 * the rest small capitals, the name in its natural case), set in code because CSS cannot express
 * the runtime fallback: absent the font the label falls back to the shrunken all-caps
 * approximation (#855). The name is an {@link EllipsisText}, so it still ellipsises in a
 * width-constrained host.
 *
 * <p>The badge is a drag source (copy) carrying the component on a {@link KometClipboard}, and
 * exposes the full grounded identity (name, optional SCTID, UUID, nid) on hover. It is the shared
 * atom for the refreshed axiom tree (ike-issues#639) and the recursive semantic viewer
 * (ike-issues#641); it never applies policy truncation to the concept label — only width-driven
 * ellipsis when a host constrains it, with the full name preserved on the identity tooltip.
 */
public class KonceptBadge extends HBox {

    private static final Logger LOG = LoggerFactory.getLogger(KonceptBadge.class);

    /** Pseudo-class driving the struck-through, retired-colour label when the component is inactive. */
    private static final PseudoClass INACTIVE = PseudoClass.getPseudoClass("inactive");

    /**
     * Pseudo-class a concept-expecting host enables via {@link #setConceptExpected(boolean)} to
     * escalate (for example a red border) a badge that carries a non-concept kind sigil.
     */
    private static final PseudoClass ALARM = PseudoClass.getPseudoClass("alarm");

    /** Default identicon edge length in pixels, sized to sit beside body text. */
    private static final double DEFAULT_ICON_SIZE = 14;

    /** Inline edge length (px) of the {@link KonceptKind#STAMP} pentagon sigil. */
    private static final double STAMP_SIGIL_SIZE = 14;

    /**
     * Name font size (px) in the true small-caps family. Slightly larger than the fallback because
     * the family's lowercase glyphs are small capitals (well below full height), the same
     * size-per-mode split the assistant chip uses.
     */
    private static final double SC_FONT_SIZE = 12;

    /** Name font size (px) for the shrunken all-caps fallback — the pre-#855 rendering. */
    private static final double FALLBACK_FONT_SIZE = 11;

    /** Name size as a fraction of the ambient font, in the true small-caps family. */
    private static final double NAME_SCALE = 0.9;

    /** Name size as a fraction of the ambient font in the shrunken all-caps fallback. */
    private static final double NAME_SCALE_FALLBACK = 0.8;

    /** Identicon edge as a fraction of the ambient font, so it sits on the name's midline. */
    private static final double ICON_SCALE = 0.92;

    /** Inline pill used when no stylesheet reaches the badge — the koncept palette of komet.css. */
    private static final String STANDALONE_PILL_STYLE =
            "-fx-background-color: #e9eff6; -fx-background-radius: 6; -fx-padding: 1 5 1 4;";

    /** Inline active label colour, matching {@code .koncept-label}. */
    private static final String LABEL_ACTIVE = "#2a5a8a";

    /** Inline retired label colour, matching {@code .koncept-label:inactive}. */
    private static final String LABEL_INACTIVE = "#b00020";

    /** Sentinel nid for a presentation-only badge built without a populated store/view. */
    private static final int UNKNOWN_NID = Integer.MIN_VALUE;

    private final int nid;
    private final PublicId publicId;
    private final ViewProperties viewProperties;
    private boolean inactive;
    /** Whether {@link #setStandaloneStyling} is in effect, so a state change repaints inline. */
    private boolean standalone;

    private final HBox sigilBox = new HBox();
    private final HBox statusBox = new HBox();
    private final ImageView identicon;
    private final EllipsisText nameNode = new EllipsisText();

    private String conceptName;
    private String sctid;
    private PremiseType premiseType = PremiseType.INFERRED;
    private KonceptStatus status = KonceptStatus.NONE;
    private KonceptKind kind = KonceptKind.CONCEPT;
    /** Letter-sigil size (px); 0 leaves it to the stylesheet. Set by {@link #setAmbientFontSize}. */
    private double letterSigilSize = 0;
    private boolean conceptExpected = false;

    /**
     * Creates a badge for the given component, resolving its name, identicon, inactive state and
     * taxonomic status from the supplied view.
     *
     * @param entity         the component to render; its {@link EntityFacade#nid() nid} and
     *                       {@link EntityFacade#publicId() publicId} drive resolution
     * @param viewProperties the view used to resolve the name, latest state and classification
     */
    public KonceptBadge(EntityFacade entity, ViewProperties viewProperties) {
        this(entity.nid(), entity.publicId(), null, viewProperties, true);
    }

    /**
     * Creates a badge for the component with the given nid, resolving everything from the view.
     *
     * @param entityNid      the component nid
     * @param viewProperties the view used to resolve the name, latest state and classification
     */
    public KonceptBadge(int entityNid, ViewProperties viewProperties) {
        this(entityNid, PrimitiveData.publicId(entityNid), null, viewProperties, true);
    }

    /**
     * Creates a badge for the component with the given nid, optionally suppressing the status glyph
     * (for example for role values in an axiom tree, where classification is not meaningful).
     *
     * @param entityNid      the component nid
     * @param viewProperties the view used to resolve the name, latest state and classification
     * @param showStatus     {@code true} to compute and show the taxonomic status glyph
     */
    public KonceptBadge(int entityNid, ViewProperties viewProperties, boolean showStatus) {
        this(entityNid, PrimitiveData.publicId(entityNid), null, viewProperties, showStatus);
    }

    /**
     * Creates a presentation-only badge from a {@link PublicId} and a pre-resolved name, for
     * contexts without a populated store or view. Such a badge shows the identicon and label only —
     * no status glyph, inactive styling or drag source.
     *
     * @param publicId the component identifier driving the identicon and tooltip
     * @param name     the concept name to display (not truncated); may be {@code null}
     */
    public KonceptBadge(PublicId publicId, String name) {
        this(UNKNOWN_NID, publicId, name, null, false);
    }

    /**
     * Creates a presentation badge for a <em>known</em> component without a view: the caller
     * supplies the name and state (resolved through its own calculator), and the badge is a full
     * drag source, since the nid is known. This is the assistant chip's case — the two-argument
     * form is presentation-only ({@link #UNKNOWN_NID}) and installs no drag handler, which
     * silently made every chip undraggable when the chip stopped installing its own
     * (ikmdev/komet#742).
     *
     * @param nid      the component nid; the drag payload
     * @param publicId the component identifier driving the identicon and tooltip
     * @param name     the concept name to display (not truncated); may be {@code null}
     */
    public KonceptBadge(int nid, PublicId publicId, String name) {
        this(nid, publicId, name, null, false);
    }

    private KonceptBadge(int nid, PublicId publicId, String explicitName, ViewProperties viewProperties,
                         boolean showStatus) {
        this.nid = nid;
        this.publicId = publicId;
        this.viewProperties = viewProperties;
        this.inactive = computeInactive(nid, viewProperties);

        getStyleClass().add(StyleClasses.KONCEPT_CHIP.toString());
        // An HBox defaults to TOP_LEFT, which pinned the small sigil and status glyphs to the top
        // while the taller identicon set the row height — the sigil sat visibly above the name
        // instead of on its midline (ikmdev/komet#883). Centre everything on the name's line.
        setAlignment(Pos.CENTER_LEFT);
        sigilBox.setAlignment(Pos.CENTER);
        statusBox.setAlignment(Pos.CENTER);

        this.identicon = (publicId != null)
                ? Identicon.generateIdenticon(publicId, (int) Math.round(DEFAULT_ICON_SIZE),
                        (int) Math.round(DEFAULT_ICON_SIZE))
                : new ImageView();
        this.identicon.setSmooth(false);
        this.identicon.getStyleClass().add(StyleClasses.KONCEPT_IDENTICON.toString());

        this.nameNode.textNode().getStyleClass().add(StyleClasses.KONCEPT_LABEL.toString());
        // True small caps via the bundled dedicated family; absent the font, the shrunken all-caps
        // fallback (see the class comment). Set in code, not komet.css, because only code can ask
        // the resolver whether the family registered.
        String scFamily = SmallCapsFonts.family();
        this.nameNode.setFont(scFamily != null
                ? Font.font(scFamily, SC_FONT_SIZE)
                : Font.font(FALLBACK_FONT_SIZE));
        // Let the name shrink and ellipsize (with the full name on the identity tooltip) so the
        // badge fits a fixed-width container without forcing a horizontal scrollbar.
        HBox.setHgrow(this.nameNode, Priority.ALWAYS);
        setMaxWidth(Double.MAX_VALUE);
        setConceptName(explicitName != null ? explicitName : resolveName(nid, viewProperties));

        getChildren().addAll(sigilBox, statusBox, identicon, nameNode);
        setStatus(showStatus && viewProperties != null && nid != UNKNOWN_NID
                ? computeStatus(nid, viewProperties, premiseType)
                : KonceptStatus.NONE);
        // Be honest about the component kind: a concept stays bare, every other kind gets its sigil
        // (a presentation-only badge, with no view to verify, stays the bare concept default).
        setKind(viewProperties != null && nid != UNKNOWN_NID
                ? KonceptKindResolver.resolve(nid, viewProperties.calculator())
                : KonceptKind.CONCEPT);
        // A stamp shows its compact provenance (status · date-time · author) rather than a name.
        if (kind.isStamp() && viewProperties != null && nid != UNKNOWN_NID) {
            setConceptName(StampText.compact(nid, viewProperties.calculator()));
        }

        pseudoClassStateChanged(INACTIVE, inactive);

        if (nid != UNKNOWN_NID) {
            // The canonical GENERATED glyph, not a snapshot of this badge (ikmdev/komet#882).
            // The snapshot path rescales whatever width the host stretched the badge to and wears
            // kview's 4px -Primary-05 drag-affordance border (.draggable-node:snapshot) — the wide,
            // green-framed, soft drag image reported from the pattern navigator. The glyph is
            // tight, 1:1, and carries the kind sigil. Installed on the badge itself, reading its
            // CURRENT kind/name/retired state at gesture time, so every badge drags identically —
            // including hosts whose own drag handler this badge's press shadows.
            setOnDragDetected(event -> {
                if (getScene() == null) {
                    return;
                }
                Dragboard dragboard = startDragAndDrop(TransferMode.COPY);
                KonceptDragGlyph.setDragView(dragboard, kind, publicId, conceptName, inactive);
                dragboard.setContent(KometClipboard.forComponent(nid));
                event.consume();
            });
        }
        installTooltip();
    }

    /**
     * Sets the taxonomic status glyph shown ahead of the identicon, replacing any current glyph.
     *
     * @param status the classification to display; {@code null} is treated as {@link KonceptStatus#NONE}
     */
    public final void setStatus(KonceptStatus status) {
        this.status = (status == null) ? KonceptStatus.NONE : status;
        statusBox.getChildren().clear();
        boolean visible = this.status.hasGlyph();
        statusBox.setManaged(visible);
        statusBox.setVisible(visible);
        if (visible) {
            Text glyph = new Text(this.status.glyph());
            glyph.getStyleClass().addAll(StyleClasses.KONCEPT_STATUS.toString(), this.status.styleClass().toString());
            statusBox.getChildren().add(glyph);
            if (this.status.isMultiParent()) {
                Text fork = new Text(KonceptStatus.MULTI_PARENT_GLYPH);
                fork.getStyleClass().addAll(StyleClasses.KONCEPT_STATUS.toString(),
                        StyleClasses.KONCEPT_MULTIPARENT.toString());
                statusBox.getChildren().add(fork);
            }
        }
    }

    /**
     * The taxonomic status currently shown.
     *
     * @return the current {@link KonceptStatus} (never {@code null})
     */
    public KonceptStatus getStatus() {
        return status;
    }

    /**
     * Sets the component-kind sigil shown ahead of the identicon, replacing any current sigil
     * (ike-issues#638). A {@link KonceptKind#CONCEPT} shows no sigil (the bare default);
     * {@link KonceptKind#STAMP} shows the {@link StampSigil} pentagon; every other kind shows its
     * coloured letter glyph. A sigil always immediately precedes the identicon and is never bare:
     * a stamp keeps the STAMP's own identicon after its pentagon — the identicon tells one STAMP
     * from another at a glance — with the compact provenance text in place of a name. The
     * accessible kind name is installed on the sigil's tooltip — the non-colour accessibility
     * channel, so kind is never carried by colour alone.
     *
     * @param kind the component kind; {@code null} is treated as {@link KonceptKind#CONCEPT}
     */
    public final void setKind(KonceptKind kind) {
        this.kind = (kind == null) ? KonceptKind.CONCEPT : kind;
        sigilBox.getChildren().clear();
        boolean visible = this.kind.hasSigil();
        sigilBox.setManaged(visible);
        sigilBox.setVisible(visible);
        // The sigil node comes from the shared factory, so the badge and the drag glyph cannot
        // disagree about what a kind looks like (ikmdev/komet#883). Size 0: the stylesheet reaches
        // this control and sizes the letter.
        KonceptSigils.create(this.kind, STAMP_SIGIL_SIZE, letterSigilSize)
                .ifPresent(sigil -> sigilBox.getChildren().add(sigil));
        // Every kind keeps its identicon — for a stamp the pentagon precedes the STAMP's own
        // identicon (a sigil is never bare; the identicon tells one STAMP from another at a
        // glance), with the compact provenance text in place of a name (revised ike-issues#638).
        refreshAlarm();
    }

    /**
     * The component kind this badge is honest about.
     *
     * @return the current {@link KonceptKind} (never {@code null})
     */
    public KonceptKind getKind() {
        return kind;
    }

    /**
     * Whether this badge would violate a concept expectation — it carries a sigil, i.e. it is not a
     * bare concept — so a concept-expecting host can escalate it even without enabling the built-in
     * {@code alarm} styling.
     *
     * @return {@code true} when {@link #getKind()} is anything other than {@link KonceptKind#CONCEPT}
     */
    public boolean isConceptViolation() {
        return kind.hasSigil();
    }

    /**
     * Declares whether this badge sits in a context that <em>expects a concept</em> (a concept slot,
     * the assistant chip). When {@code true} and the badge carries any kind sigil, the badge enters
     * the {@code alarm} pseudo-class state so the host can escalate it (for example a red border).
     * The badge stays neutral by default; the host opts into the alarm.
     *
     * @param conceptExpected {@code true} if the host context requires a concept
     */
    public final void setConceptExpected(boolean conceptExpected) {
        this.conceptExpected = conceptExpected;
        refreshAlarm();
    }

    private void refreshAlarm() {
        pseudoClassStateChanged(ALARM, conceptExpected && kind.hasSigil());
    }

    /**
     * Sets the premise type used when recomputing the taxonomic status, and recomputes it. Has no
     * effect on a presentation-only badge built without a view.
     *
     * @param premiseType the premise type ({@link PremiseType#STATED} or {@link PremiseType#INFERRED})
     */
    public void setPremiseType(PremiseType premiseType) {
        this.premiseType = premiseType;
        if (viewProperties != null && nid != UNKNOWN_NID && status != KonceptStatus.NONE) {
            setStatus(computeStatus(nid, viewProperties, premiseType));
        }
    }

    /**
     * Sets the SCTID shown in the identity tooltip and rebuilds the tooltip.
     *
     * @param sctid the SNOMED CT identifier, or {@code null} to omit it
     */
    public void setSctid(String sctid) {
        this.sctid = sctid;
        installTooltip();
    }

    /**
     * Sets the identicon edge length in pixels, regenerating the raster at that size so the
     * identicon stays crisp rather than upscaling the default {@value #DEFAULT_ICON_SIZE}px raster
     * (the {@link ImageView} has smoothing disabled). A larger badge therefore renders a sharp
     * identicon, not a blurred one.
     *
     * @param pixels the identicon width and height in pixels
     */
    /**
     * Scales the whole badge from an ambient body font size — the name, the identicon, and the kind
     * sigil together — for a host whose text size is not fixed (ikmdev/komet#742). Without this the
     * badge renders at its built-in size, so a surface with its own font control could not adopt it.
     *
     * <p>The ratios are the ones the assistant's inline chip established: the name at
     * {@value #NAME_SCALE} of the ambient size (or {@value #NAME_SCALE_FALLBACK} in the shrunken
     * all-caps fallback, whose glyphs are full height), and the identicon at
     * {@value #ICON_SCALE} so it sits on the name's midline.
     *
     * @param basePx the ambient body font size in px; values {@code <= 0} are ignored
     */
    public void setAmbientFontSize(double basePx) {
        if (basePx <= 0) {
            return;
        }
        String scFamily = SmallCapsFonts.family();
        nameNode.setFont(scFamily != null
                ? Font.font(scFamily, basePx * NAME_SCALE)
                : Font.font(basePx * NAME_SCALE_FALLBACK));
        setIconSize(Math.round(basePx * ICON_SCALE));
        // Rebuild the sigil at the scaled size: a sigil that stayed at the stylesheet's size would
        // dwarf or vanish beside a scaled name.
        this.letterSigilSize = basePx * NAME_SCALE;
        setKind(this.kind);
    }

    /**
     * Styles the badge inline instead of relying on a stylesheet reaching it — for a host whose
     * scene does not load {@code komet.css}, or a node snapshotted off-stage (ikmdev/komet#742).
     *
     * <p>The badge is normally CSS-driven, which is right when it sits in a Komet window. It is
     * wrong wherever CSS does not arrive: the pill loses its fill and the name its colour, silently.
     * This paints both directly, so the badge is safe to embed anywhere.
     *
     * @param standalone {@code true} to paint the pill and label inline
     */
    public void setStandaloneStyling(boolean standalone) {
        this.standalone = standalone;
        if (!standalone) {
            setStyle(null);
            nameNode.textNode().setFill(null);
            return;
        }
        setStyle(STANDALONE_PILL_STYLE);
        nameNode.textNode().setFill(Color.web(isInactive() ? LABEL_INACTIVE : LABEL_ACTIVE));
        nameNode.textNode().setStrikethrough(isInactive());
    }

    /**
     * The text baseline of the badge's name, so a host that seats content on a text line — a
     * {@code RichTextArea} line, a table row — aligns the badge with the surrounding text rather
     * than to its box (ikmdev/komet#742). A plain {@link HBox} reports its own layout baseline,
     * which sits the pill visibly off the line.
     *
     * @return the name's baseline offset within this badge
     */
    @Override
    public double getBaselineOffset() {
        Insets in = getInsets();
        double contentHeight = prefHeight(-1) - in.getTop() - in.getBottom();
        Text text = nameNode.textNode();
        double textTop = in.getTop()
                + Math.max(0, (contentHeight - text.getLayoutBounds().getHeight()) / 2);
        return textTop + text.getBaselineOffset();
    }

    /**
     * Marks the component retired, for a host that resolved the state itself — a badge built
     * without a view cannot compute it, and would otherwise always render as active.
     *
     * @param retired {@code true} if the component's latest version is inactive
     */
    public void setInactive(boolean retired) {
        this.inactive = retired;
        pseudoClassStateChanged(INACTIVE, retired);
        if (standalone) {
            setStandaloneStyling(true);
        }
    }

    /**
     * The name's current font size in px. Package-private: an observation point for the embedding
     * tests, not part of the control's contract.
     *
     * @return the name font size
     */
    double getNameFontSize() {
        return nameNode.textNode().getFont().getSize();
    }

    /**
     * The kind sigil's current font size in px, or {@code 0} when the badge carries no letter sigil.
     *
     * @return the sigil font size
     */
    double getSigilFontSize() {
        return sigilBox.getChildren().stream()
                .filter(Text.class::isInstance)
                .map(Text.class::cast)
                .mapToDouble(text -> text.getFont().getSize())
                .findFirst()
                .orElse(0);
    }

    /**
     * The name's current fill, or {@code null} when the stylesheet owns it.
     *
     * @return the name fill
     */
    Object getNameFill() {
        return nameNode.textNode().getFill();
    }

    public void setIconSize(double pixels) {
        identicon.setFitWidth(pixels);
        identicon.setFitHeight(pixels);
        if (publicId != null && pixels > 0) {
            int px = (int) Math.round(pixels);
            identicon.setImage(Identicon.generateIdenticon(publicId, px, px).getImage());
        }
    }

    /**
     * Whether the component's latest version is inactive (retired) in the badge's view.
     *
     * @return {@code true} if the component is inactive
     */
    public boolean isInactive() {
        return inactive;
    }

    /**
     * Overrides the displayed name, for a host whose own resolution is better than the badge's
     * default — a pattern navigator that says "No description available" where the badge would
     * fall back to the raw nid, for instance. The full name still rides on the identity tooltip.
     *
     * @param name the name to display
     */
    public void setConceptName(String name) {
        this.conceptName = name;
        // The retired strikethrough needs nothing here: the name is a Text node, and komet.css
        // strikes it (with the retired colour) under the inactive pseudo-class.
        nameNode.setText(displayText(name, SmallCapsFonts.family()));
    }

    /**
     * The display form of a concept name: with the dedicated small-caps family, the name in its
     * natural case (the family's own glyphs are the small caps — capitals full height, the rest
     * small capitals); without it, upper-cased, so the shrunken all-caps fallback still reads as
     * small caps.
     *
     * @param name             the concept name; {@code null} yields the empty string
     * @param smallCapsFamily  the resolved small-caps family ({@link SmallCapsFonts#family()}), or
     *                         {@code null} when the bundled font is unavailable
     * @return the string to display, never {@code null}
     */
    static String displayText(String name, String smallCapsFamily) {
        if (name == null) {
            return "";
        }
        return smallCapsFamily != null ? name : name.toUpperCase(Locale.ROOT);
    }

    private void installTooltip() {
        StringBuilder tip = new StringBuilder();
        if (inactive) {
            tip.append("Inactive — retired in this view\n");
        }
        tip.append(conceptName == null ? "" : conceptName);
        if (sctid != null) {
            tip.append("\nSCTID: ").append(sctid);
        }
        if (publicId != null) {
            tip.append("\nUUID: ").append(publicId.idString());
        }
        if (nid != UNKNOWN_NID) {
            tip.append("\nnid: ").append(nid);
        }
        Tooltip.install(this, new Tooltip(tip.toString()));
    }

    /**
     * The right edge of the identicon within this badge, in local (unscaled) coordinates —
     * used by {@link KonceptDragSource} to place the drag-view cursor just to the right of the
     * identicon so its detail stays fully visible.
     *
     * @return the identicon's right-edge x in badge-local coordinates
     */
    public double identiconRightEdge() {
        return identicon.getBoundsInParent().getMaxX();
    }

    private static String resolveName(int nid, ViewProperties viewProperties) {
        if (viewProperties == null || nid == UNKNOWN_NID) {
            return Integer.toString(nid);
        }
        // Respect the view's language coordinate (which prioritises the regular/preferred name over
        // the fully qualified name); do not force the FQN.
        return viewProperties.calculator().getDescriptionTextOrNid(nid);
    }

    private static boolean computeInactive(int nid, ViewProperties viewProperties) {
        if (viewProperties == null || nid == UNKNOWN_NID) {
            return false;
        }
        try {
            Latest<EntityVersion> latest = viewProperties.calculator().latest(nid);
            return latest.isPresent() && latest.get().stamp().state() != State.ACTIVE;
        } catch (RuntimeException e) {
            LOG.warn("Could not resolve active state for nid {}", nid, e);
            return false;
        }
    }

    /**
     * Computes the taxonomic classification glyph for a concept — the data counterpart of the
     * navigator/axiom icon produced by {@code AxiomView#computeGraphic}.
     *
     * <p>Unlike that method, which flags every non-root concept as multi-parent (its
     * {@code multiParent} test is {@code !parents.isEmpty()}, leaving the single-parent icon
     * branches unreachable), this uses {@code parents.size() > 1} so the single- and multi-parent
     * statuses are distinguished correctly.
     *
     * @param nid            the concept nid
     * @param viewProperties the view used to read parents and the logical definition
     * @param premiseType    the premise type to read the axiom tree under; falls back to
     *                       {@link PremiseType#STATED} when no tree exists for the requested premise
     * @return the classification, or {@link KonceptStatus#NONE} when the concept has no resolvable
     *         logical definition
     */
    public static KonceptStatus computeStatus(int nid, ViewProperties viewProperties, PremiseType premiseType) {
        if (nid == UNKNOWN_NID || nid == -1 || nid == TinkarTerm.UNINITIALIZED_COMPONENT.nid()) {
            return KonceptStatus.NONE;
        }
        IntIdList parents;
        try {
            parents = viewProperties.calculator().navigationCalculator().parentsOf(nid);
        } catch (RuntimeException ex) {
            LOG.warn("Could not resolve parents for nid {}", nid, ex);
            parents = IntIds.list.empty();
        }
        Latest<DiTreeEntity> definition = viewProperties.calculator().getAxiomTreeForEntity(nid, premiseType);
        if (!definition.isPresent()) {
            definition = viewProperties.calculator().getAxiomTreeForEntity(nid, PremiseType.STATED);
        }
        if (!definition.isPresent()) {
            return KonceptStatus.NONE;
        }
        if (parents.isEmpty()) {
            return KonceptStatus.ROOT;
        }
        boolean multiParent = parents.size() > 1;
        boolean sufficient = definition.get().containsVertexWithMeaning(TinkarTerm.SUFFICIENT_SET);
        if (sufficient) {
            return multiParent ? KonceptStatus.DEFINED_MULTIPARENT : KonceptStatus.DEFINED;
        }
        return multiParent ? KonceptStatus.PRIMITIVE_MULTIPARENT : KonceptStatus.PRIMITIVE;
    }
}
