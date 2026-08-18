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
import network.ike.docs.konceptcore.KonceptAppearance;
import network.ike.docs.konceptcore.KonceptKind;

import dev.ikm.komet.framework.Identicon;
import dev.ikm.komet.framework.StyleClasses;
import dev.ikm.komet.framework.graphics.Icon;
import dev.ikm.komet.framework.panel.axiom.AxiomPopover;
import dev.ikm.komet.framework.dnd.KometClipboard;
import dev.ikm.komet.framework.dnd.KonceptDragGlyph;
import dev.ikm.komet.framework.dnd.KonceptDragSource;
import dev.ikm.komet.framework.graphics.KonceptGlyphFonts;
import dev.ikm.komet.framework.graphics.SmallCapsFonts;
import dev.ikm.komet.framework.view.ViewProperties;
import dev.ikm.tinkar.common.id.IntIdList;
import dev.ikm.tinkar.common.id.IntIds;
import dev.ikm.tinkar.common.id.PublicId;
import dev.ikm.tinkar.common.service.PrimitiveData;
import dev.ikm.tinkar.coordinate.logic.PremiseType;
import dev.ikm.tinkar.coordinate.stamp.calculator.Latest;
import dev.ikm.tinkar.coordinate.view.calculator.ViewCalculator;
import dev.ikm.tinkar.entity.EntityVersion;
import dev.ikm.tinkar.entity.graph.DiTreeEntity;
import dev.ikm.tinkar.terms.EntityFacade;
import dev.ikm.tinkar.terms.State;
import dev.ikm.tinkar.terms.TinkarTerm;
import javafx.css.PseudoClass;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Cursor;
import javafx.scene.Node;
import javafx.scene.control.Button;
import javafx.scene.control.ContentDisplay;
import javafx.scene.control.ContextMenu;
import javafx.scene.control.MenuItem;
import javafx.scene.control.TextArea;
import javafx.scene.control.Tooltip;
import javafx.scene.image.ImageView;
import javafx.scene.input.Clipboard;
import javafx.scene.input.ClipboardContent;
import javafx.scene.input.Dragboard;
import javafx.scene.input.MouseButton;
import javafx.scene.input.MouseEvent;
import javafx.scene.input.TransferMode;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.Region;
import javafx.scene.layout.StackPane;
import javafx.scene.paint.Color;
import javafx.scene.text.Font;
import javafx.scene.text.FontWeight;
import javafx.scene.text.Text;
import org.controlsfx.control.PopOver;
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
 *
 * <p>The label-fidelity rule is <em>contextual</em> (ike-issues#1036): a wrapping host — an
 * assistant table cell, where content folds to the column — enables
 * {@link #setMultiLineLabel(boolean) the multi-line label} and the badge wraps its whole name
 * like the text beside it; a single-line host constrains the badge and it ellipsises. The two
 * meet on the ellipsised badge itself: its identity tooltip carries a multi-line rendering of
 * the badge ({@link #expandedRendering(double)}), and clicking the ellipsised badge opens the
 * same rendering live (drag source, definition popout) in a transient {@link PopOver} — the
 * full identity is always one hover or one click away.
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

    /** The one shared badge appearance (ike-issues#742/#860) every value below reads from. */
    private static final KonceptAppearance SPEC = KonceptAppearance.defaults();

    /** Default identicon edge length in pixels, sized to sit beside body text. */
    private static final double DEFAULT_ICON_SIZE = SPEC.identiconSizePx();

    /**
     * Letter-sigil size as a fraction of the name font — the normative ratio, read from the
     * anatomy renderer that draws the badge-spec figures (KonceptFigureRenderer: sigil bold 15
     * over name 12). The sigil is a quarter LARGER than the name, and bold.
     */
    private static final double SIGIL_TO_NAME = 15.0 / 12.0;

    /**
     * Pentagon edge as a fraction of the identicon edge — normative, from the same source
     * (pentagonBox 20 over identiconSize 32).
     */
    private static final double PENTAGON_TO_ICON = 20.0 / 32.0;

    /**
     * Status-cluster size as a fraction of the name font — the komet.css normative ratio
     * ({@code .koncept-status} 10px against the badge's 12px name).
     */
    private static final double STATUS_TO_NAME = 10.0 / 12.0;

    /**
     * Name font size (px) in the true small-caps family. Slightly larger than the fallback because
     * the family's lowercase glyphs are small capitals (well below full height), the same
     * size-per-mode split the assistant chip uses.
     */
    private static final double SC_FONT_SIZE = SPEC.labelSizePx();

    /** Name font size (px) for the shrunken all-caps fallback — the pre-#855 rendering. */
    private static final double FALLBACK_FONT_SIZE = 11;

    /** Name size as a fraction of the ambient font, in the true small-caps family. */
    private static final double NAME_SCALE = 0.9;

    /** Name size as a fraction of the ambient font in the shrunken all-caps fallback. */
    private static final double NAME_SCALE_FALLBACK = 0.8;

    /** Identicon edge as a fraction of the ambient font, so it sits on the name's midline. */
    private static final double ICON_SCALE = 0.92;

    /**
     * Inline pill used when no stylesheet reaches the badge — built from the shared
     * {@link KonceptAppearance} spec (#861), so the standalone rendering and the komet.css
     * {@code .koncept-chip} rules (which mirror the same spec) cannot diverge.
     */
    private static final String STANDALONE_PILL_STYLE = standalonePillStyle(SPEC);

    /** Inline active label colour, from the spec ({@code .koncept-label} mirrors it). */
    private static final String LABEL_ACTIVE = SPEC.labelColorHex();

    /** Inline retired label colour, from the spec ({@code .koncept-label:inactive} mirrors it). */
    private static final String LABEL_INACTIVE = SPEC.labelColorInactiveHex();

    /**
     * The standalone pill's inline style from an appearance spec — fill, corner radius, and
     * the unified paddings, exactly the values {@code .koncept-chip} mirrors in komet.css.
     *
     * @param spec the appearance to render
     * @return the inline JavaFX style string
     */
    static String standalonePillStyle(KonceptAppearance spec) {
        return "-fx-background-color: " + spec.pillFillHex() + ";"
                + " -fx-background-radius: " + (int) spec.cornerRadiusPx() + ";"
                + " -fx-padding: " + (int) spec.padTopPx() + " " + (int) spec.padRightPx()
                + " " + (int) spec.padBottomPx() + " " + (int) spec.padLeftPx() + ";";
    }

    /** Sentinel nid for a presentation-only badge built without a populated store/view. */
    private static final int UNKNOWN_NID = Integer.MIN_VALUE;

    /**
     * Width cap (px) of the expanded multi-line rendering used by the identity tooltip and the
     * click-to-expand popover (ike-issues#1036) — wide enough for a long clinical name to wrap
     * into two or three comfortable lines rather than a tall sliver.
     */
    private static final double EXPANDED_MAX_WIDTH = 340;

    private final int nid;
    private final PublicId publicId;
    private final ViewProperties viewProperties;
    private boolean inactive;
    /** Whether {@link #setStandaloneStyling} is in effect, so a state change repaints inline. */
    private boolean standalone;
    /** Whether the name wraps across lines instead of ellipsising (ike-issues#1036). */
    private boolean multiLineLabel;
    /** The ambient body font size last applied via {@link #setAmbientFontSize}, or 0 when unset. */
    private double ambientFontSize;
    /** The label typography last applied via {@link #setLabelTypography}; never {@code null}. */
    private KonceptLabelTypography typography = KonceptLabelTypography.DEFAULT;
    /** Whether the status cluster renders in a reserved constant-width slot (ike-issues#1049). */
    private boolean statusSlotReserved;
    /** The single identity tooltip, its content refreshed lazily each time it shows. */
    private Tooltip identityTooltip;
    /** The open click-to-expand popover, so a second click toggles instead of stacking. */
    private PopOver expandedPopover;

    /**
     * Where the definition-popout affordance sits in the badge (ike-issues#941).
     */
    public enum PopoutPosition {
        /**
         * Between the identicon and the name — the classic {@code ClauseView} reading, where
         * the affordance leads the label.
         */
        AFTER_IDENTICON,
        /**
         * After the name or its ellipsis, at the row end — the default: it keeps the badge's
         * published anatomy ([one mark][identicon][name]) uninterrupted and matches the
         * navigator's row-end-indicator convention.
         */
        TRAILING
    }

    private final HBox sigilBox = new HBox();
    private final HBox statusBox = new HBox();
    private final ImageView identicon;
    private final EllipsisText nameNode = new EllipsisText();
    /** The definition-popout affordance (ike-issues#941), or {@code null} when the badge cannot honour it. */
    private Button popoutButton;
    private PopoutPosition popoutPosition = PopoutPosition.TRAILING;

    private String conceptName;
    private String sctid;
    private PremiseType premiseType = PremiseType.INFERRED;
    private KonceptStatus status = KonceptStatus.NONE;
    private KonceptKind kind = KonceptKind.CONCEPT;
    /** Letter-sigil size (px), {@link #SIGIL_TO_NAME} of the current name font. */
    private double letterSigilSize = SC_FONT_SIZE * SIGIL_TO_NAME;
    /** Pentagon edge (px), {@link #PENTAGON_TO_ICON} of the current identicon edge. */
    private double stampSigilSize = DEFAULT_ICON_SIZE * PENTAGON_TO_ICON;
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

    /**
     * Creates a badge for a known component with a caller-supplied {@link PublicId} AND a view —
     * the interactive-chip case (ike-issues#941): the id the caller already holds (for example a
     * {@code k:} token's UUID) drives the identicon, while the view resolves everything else —
     * the coordinate-preferred name, the logical-status cluster, kind, retired state, and the
     * definition popout. The name deliberately comes from the view, never the caller: this form
     * once accepted a caller-resolved name so the assistant chip could keep its FQN-first recipe,
     * which is exactly how chips ended up ignoring the coordinate's description-type priority
     * (ike-issues#942).
     *
     * @param nid            the component nid; the drag payload
     * @param publicId       the component identifier driving the identicon and tooltip
     * @param viewProperties the view used to resolve the name, status, kind, state, and the popout
     */
    public KonceptBadge(int nid, PublicId publicId, ViewProperties viewProperties) {
        this(nid, publicId, null, viewProperties, true);
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
        applyNameFont();
        // The sigil rides the name it sits beside — spec ratio, whichever face resolved.
        this.letterSigilSize = nameNode.textNode().getFont().getSize() * SIGIL_TO_NAME;
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
        // The definition popout (ike-issues#941) is installed by the setKind call above: every
        // at-rest badge that can honour it — a known concept with a view to resolve its
        // definition through — carries the classic axiom view's LINK_EXTERNAL affordance,
        // opening the shared AxiomPopover. Presentation-only badges and non-concept kinds
        // have no definition to open and stay clean.

        pseudoClassStateChanged(INACTIVE, inactive);

        if (nid != UNKNOWN_NID) {
            // A grabbable chip shows the hand, not whatever the host shows — in a RichTextArea
            // that is the text I-beam, which misreads the badge as text. JavaFX resolves the
            // cursor from the innermost node that sets one, so this wins over any ancestor.
            // Presentation-only badges (no nid, no drag) keep the host's cursor honestly.
            setCursor(Cursor.HAND);
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
                // The badge's live status rides along, so a bare Koncept drags with the same
                // copula cluster it shows on screen (ike-issues#861).
                KonceptDragGlyph.setDragView(dragboard, kind, status, publicId, conceptName, inactive);
                dragboard.setContent(KometClipboard.forComponent(nid));
                event.consume();
            });
        }
        installTooltip();
        // Click-to-expand from the ellipsis (ike-issues#1036): a truncated badge opens its full
        // multi-line rendering. Installed on every badge — the gate reads the live ellipsis
        // state, so an unconstrained badge never reacts. CLICKED (not PRESSED) keeps the drag
        // gesture and the definition popout untouched: a real drag fails isStillSincePress, and
        // the popout button consumes its own clicks.
        addEventHandler(MouseEvent.MOUSE_CLICKED, this::onClickExpandFromEllipsis);
        // The badge's name is a Text node — not natively selectable — so copying rides two
        // affordances (ike-issues#1036): a context menu (whole-value copies + "Select text…"),
        // and double-click as the direct entry into the selection popover, whose read-only
        // text field gives real word/character/line selection.
        installCopyMenu();
        addEventHandler(MouseEvent.MOUSE_CLICKED, this::onDoubleClickSelectText);
    }

    /**
     * Installs the copy context menu: the whole name, the canonical id-bearing {@code k:}
     * interchange token (paste-ready for the assistant and adoc), and the selection popover
     * for partial copies. The event is consumed so a host's own menu (a table's copy-table
     * menu) never stacks on top of the badge's.
     */
    private void installCopyMenu() {
        MenuItem copyName = new MenuItem("Copy name");
        copyName.setOnAction(action -> copyToClipboard(conceptName == null ? "" : conceptName));
        MenuItem copyToken = new MenuItem("Copy k: token");
        copyToken.setOnAction(action -> copyToClipboard(interchangeToken()));
        MenuItem selectText = new MenuItem("Select text…");
        selectText.setOnAction(action -> {
            javafx.geometry.Bounds bounds = localToScreen(getBoundsInLocal());
            if (bounds != null) {
                showTextSelectionPopover(bounds.getMinX(), bounds.getMaxY());
            }
        });
        ContextMenu menu = new ContextMenu(copyName, copyToken, selectText);
        setOnContextMenuRequested(event -> {
            menu.show(this, event.getScreenX(), event.getScreenY());
            event.consume();
        });
    }

    /**
     * The badge's canonical id-bearing {@code k:} interchange token
     * ({@code k:uuid=<id>[Name]}, ike-issues#735) — the paste-everywhere form; the bare name
     * when the badge has no identifier.
     */
    private String interchangeToken() {
        String name = conceptName == null ? "" : conceptName;
        if (publicId == null || publicId.asUuidArray().length == 0) {
            return name;
        }
        return "k:uuid=" + publicId.asUuidArray()[0] + "[" + name + "]";
    }

    /** Puts {@code value} on the system clipboard as plain text. */
    private static void copyToClipboard(String value) {
        ClipboardContent content = new ClipboardContent();
        content.putString(value);
        Clipboard.getSystemClipboard().setContent(content);
    }

    /** Double-click is the direct gesture into the text-selection popover. */
    private void onDoubleClickSelectText(MouseEvent event) {
        if (event.getButton() == MouseButton.PRIMARY && event.getClickCount() == 2) {
            showTextSelectionPopover(event.getScreenX(), event.getScreenY());
            event.consume();
        }
    }

    /**
     * Opens the selection popover: the full name in a read-only, wrapping text field —
     * pre-selected for an immediate copy, with native word/character/line sub-selection for
     * partial copies (ike-issues#1036). Transient and auto-hiding, like the badge's other
     * popovers.
     */
    private void showTextSelectionPopover(double screenX, double screenY) {
        String name = conceptName == null ? "" : conceptName;
        TextArea area = new TextArea(name);
        area.setEditable(false);
        area.setWrapText(true);
        area.setPrefColumnCount(Math.min(28, Math.max(12, name.length())));
        area.setPrefRowCount(Math.max(1, Math.min(4, 1 + name.length() / 34)));
        StackPane content = new StackPane(area);
        content.setPadding(new Insets(6));
        PopOver popover = new PopOver(content);
        popover.setDetachable(false);
        popover.setHeaderAlwaysVisible(false);
        popover.setCloseButtonEnabled(false);
        popover.setOnShown(shown -> {
            area.requestFocus();
            area.selectAll();
        });
        popover.show(this, screenX, screenY);
    }

    /**
     * Sets the taxonomic status glyph shown ahead of the identicon, replacing any current glyph.
     *
     * @param status the classification to display; {@code null} is treated as {@link KonceptStatus#NONE}
     */
    public final void setStatus(KonceptStatus status) {
        this.status = (status == null) ? KonceptStatus.NONE : status;
        statusBox.getChildren().clear();
        if (this.status.hasGlyph()) {
            Text glyph = new Text(this.status.glyph());
            glyph.getStyleClass().addAll(StyleClasses.KONCEPT_STATUS.toString(), this.status.styleClass().toString());
            statusBox.getChildren().add(glyph);
            if (this.status.isMultiParent()) {
                Text fork = new Text(KonceptStatus.MULTI_PARENT_GLYPH);
                fork.getStyleClass().addAll(StyleClasses.KONCEPT_STATUS.toString(),
                        StyleClasses.KONCEPT_MULTIPARENT.toString());
                statusBox.getChildren().add(fork);
            }
            // The non-colour, non-glyph accessibility channel (ike-issues#861): the cluster is
            // always visible; the tooltip only explains it — parity with the adoc renderer's title.
            Tooltip.install(statusBox, new Tooltip(this.status.accessibleText()));
        }
        // Fonts, then the slot: managed/visible live with the slot mode (ike-issues#1049).
        applyStatusFont();
        applyCompanionSeating();
    }

    /**
     * Applies the bundled glyph face to the status cluster's texts (ike-issues#953): a
     * programmatic font, so a host no stylesheet reaches (the standalone chip, an off-stage
     * snapshot) never resolves the cluster through OS font fallback. Where komet.css does reach
     * the badge, its {@code .koncept-status} rule restates the same family at its fixed 10px and
     * wins, so the two renderings agree by construction ({@code KonceptAppearanceMirrorTest}).
     */
    private void applyStatusFont() {
        Font statusFont = statusFont();
        for (Node child : statusBox.getChildren()) {
            if (child instanceof Text text) {
                text.setFont(statusFont);
            }
        }
        // The reserved slot is measured in this font, so it re-applies whenever the font does.
        applyStatusSlot();
    }

    /**
     * The status cluster's programmatic font: the bundled glyph face at {@link #STATUS_TO_NAME}
     * of the current name size.
     *
     * @return the font the status cluster renders in where no stylesheet reaches
     */
    private Font statusFont() {
        double size = nameNode.textNode().getFont().getSize() * STATUS_TO_NAME;
        String glyphFamily = KonceptGlyphFonts.family();
        return glyphFamily != null ? Font.font(glyphFamily, size) : Font.font(size);
    }

    /**
     * Reserves a constant-width slot for the status cluster, so identicons seat on one column
     * down stacked sibling badges — a taxonomy tree's rows (ike-issues#1049). The slot holds the
     * widest cluster any status can render at the current status font (classification glyph plus
     * the multi-parent fork), the cluster sits right-aligned against the identicon it precedes —
     * the sigil-never-bare adjacency is spatial, not just structural — and the slot is kept,
     * empty, when the status is {@link KonceptStatus#NONE}. Off by default: an inline chip in
     * flowing prose hugs its content.
     *
     * <p>The reservation covers the status cluster only; the kind sigil's width is not reserved,
     * because today's stacked surfaces are concept taxonomies, where a concept is the bare
     * identicon and carries no kind sigil.
     *
     * @param reserved {@code true} to reserve the slot; {@code false} restores content hugging
     */
    public final void setStatusSlotReserved(boolean reserved) {
        this.statusSlotReserved = reserved;
        applyStatusSlot();
    }

    /**
     * Applies the status cluster's slot mode: reserved — constant width, cluster right-aligned
     * against the identicon, managed even when empty — or hugging, where an empty cluster leaves
     * the row entirely (the pre-#1049 behavior).
     */
    private void applyStatusSlot() {
        boolean hasGlyph = status.hasGlyph();
        if (statusSlotReserved) {
            statusBox.setAlignment(Pos.CENTER_RIGHT);
            double width = widestStatusClusterWidth(statusFont());
            statusBox.setMinWidth(width);
            statusBox.setPrefWidth(width);
            statusBox.setManaged(true);
            statusBox.setVisible(hasGlyph);
        } else {
            statusBox.setAlignment(Pos.CENTER);
            statusBox.setMinWidth(Region.USE_COMPUTED_SIZE);
            statusBox.setPrefWidth(Region.USE_COMPUTED_SIZE);
            statusBox.setManaged(hasGlyph);
            statusBox.setVisible(hasGlyph);
        }
    }

    /**
     * The widest cluster any status can render in the given font — each classification glyph
     * with the fork its multi-parent variant appends — the constant width the reserved slot
     * holds, so the slot never re-measures per row.
     *
     * @param font the status-cluster font to measure in
     * @return the widest cluster width in px, rounded up
     */
    private static double widestStatusClusterWidth(Font font) {
        double widest = 0;
        for (KonceptStatus value : KonceptStatus.values()) {
            if (!value.hasGlyph()) {
                continue;
            }
            String cluster = value.isMultiParent()
                    ? value.glyph() + KonceptStatus.MULTI_PARENT_GLYPH
                    : value.glyph();
            Text probe = new Text(cluster);
            probe.setFont(font);
            widest = Math.max(widest, probe.getLayoutBounds().getWidth());
        }
        return Math.ceil(widest);
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
        KonceptSigils.create(this.kind, stampSigilSize, letterSigilSize)
                .ifPresent(sigil -> sigilBox.getChildren().add(sigil));
        // Every kind keeps its identicon — for a stamp the pentagon precedes the STAMP's own
        // identicon (a sigil is never bare; the identicon tells one STAMP from another at a
        // glance), with the compact provenance text in place of a name (revised ike-issues#638).
        refreshAlarm();
        // A kind change can gain or lose the definition popout (only a concept has one).
        refreshPopout();
        applyCompanionSeating();
    }

    /**
     * Where the definition-popout affordance sits; repositioning re-seats an existing button.
     *
     * @param position the position; {@code null} is treated as {@link PopoutPosition#TRAILING}
     */
    public final void setPopoutPosition(PopoutPosition position) {
        this.popoutPosition = (position == null) ? PopoutPosition.TRAILING : position;
        refreshPopout();
    }

    /**
     * Whether this badge shows the definition-popout affordance. On by default wherever the
     * badge can honour it; a host whose surface reads busy with it (a prose-inline chip) can
     * opt out.
     *
     * @param visible {@code false} to suppress the popout on this badge
     */
    public final void setDefinitionPopoutVisible(boolean visible) {
        this.popoutSuppressed = !visible;
        refreshPopout();
    }

    /** Host opt-out flag for the definition popout; the applicability gate stays separate. */
    private boolean popoutSuppressed;

    /**
     * Whether this badge can honour a definition popout: a known concept with a view to
     * resolve its definition through. Pure gate, shared with the tests.
     *
     * @param nid            the component nid
     * @param viewProperties the badge's view, or {@code null} for presentation-only badges
     * @param kind           the component kind
     * @return {@code true} when the popout affordance applies
     */
    static boolean popoutApplicable(int nid, ViewProperties viewProperties, KonceptKind kind) {
        return nid != UNKNOWN_NID && viewProperties != null && kind == KonceptKind.CONCEPT;
    }

    /**
     * (Re)installs the definition popout per the applicability gate, suppression flag, position,
     * and label mode. Single-line: a row child — the button holds its preferred size
     * ({@code USE_PREF_SIZE} min) and the name is the badge's only shrinkable child, so a
     * width-constrained badge ellipsizes the name while the affordance stays fully visible,
     * never clipped and never covering text (ike-issues#941). Multi-line: the button flows
     * <em>inline after the last wrapped word</em> of the name (the name region's trailing node,
     * ike-issues#1036) — trailing the text like a link glyph, never floating at the pill's
     * edge; the position preference applies only to the single-line row.
     */
    private void refreshPopout() {
        if (popoutButton != null) {
            getChildren().remove(popoutButton);
            if (nameNode.getTrailingNode() == popoutButton) {
                nameNode.setTrailingNode(null);
            }
        }
        if (popoutSuppressed || !popoutApplicable(nid, viewProperties, kind)) {
            return;
        }
        if (popoutButton == null) {
            popoutButton = new Button("", Icon.LINK_EXTERNAL.makeIcon());
            popoutButton.getStyleClass().setAll(StyleClasses.OPEN_CONCEPT_BUTTON.toString());
            popoutButton.setMinWidth(Region.USE_PREF_SIZE);
            popoutButton.setFocusTraversable(false);
            Tooltip.install(popoutButton, new Tooltip("Open definition"));
            popoutButton.setOnMouseClicked(event -> {
                if (event.getButton() == MouseButton.PRIMARY) {
                    AxiomPopover.show(nid, premiseType, viewProperties,
                            popoutButton, event.getScreenX(), event.getScreenY());
                    event.consume();
                }
            });
        }
        if (multiLineLabel) {
            nameNode.setTrailingNode(popoutButton);
        } else if (popoutPosition == PopoutPosition.AFTER_IDENTICON) {
            getChildren().add(getChildren().indexOf(nameNode), popoutButton);
        } else {
            getChildren().add(popoutButton);
        }
        applyCompanionSeating();
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
        this.ambientFontSize = basePx;
        applyNameFont();
        double iconSize = Math.round(basePx * ICON_SCALE);
        setIconSize(iconSize);
        // Rebuild the sigil at the scaled size, holding the spec ratios: letter a quarter larger
        // than the name, pentagon five-eighths of the identicon (KonceptFigureRenderer).
        this.letterSigilSize = nameNode.textNode().getFont().getSize() * SIGIL_TO_NAME;
        this.stampSigilSize = iconSize * PENTAGON_TO_ICON;
        setKind(this.kind);
        // The status cluster follows the rescaled name in a stylesheet-free host (#953); under
        // komet.css the .koncept-status rule keeps governing.
        applyStatusFont();
        applyCompanionSeating();
    }

    /**
     * Sets the label typography — small caps versus plain text, and the label weight —
     * re-applying the name face, the display case transform, the kind-sigil and status-cluster
     * sizes that ride the name, and companion seating (ike-issues#1050). The default is
     * {@link KonceptLabelTypography#DEFAULT}, today's ike-issues#855 rendering; the identicon
     * rides the ambient font size alone, so typography never moves it.
     *
     * @param typography the typography to render the label with; {@code null} restores the default
     */
    public final void setLabelTypography(KonceptLabelTypography typography) {
        this.typography = (typography == null) ? KonceptLabelTypography.DEFAULT : typography;
        applyNameFont();
        this.letterSigilSize = nameNode.textNode().getFont().getSize() * SIGIL_TO_NAME;
        // Re-transform the display case for the new mode (the raw name is kept, so this is
        // lossless in both directions), then rebuild the companions that ride the name size.
        setConceptName(conceptName);
        setKind(this.kind);
        applyStatusFont();
        applyCompanionSeating();
    }

    /**
     * The label typography currently applied.
     *
     * @return the typography (never {@code null})
     */
    public final KonceptLabelTypography getLabelTypography() {
        return typography;
    }

    /**
     * Applies the name face for the current typography and ambient size. Small caps: true small
     * caps via the bundled dedicated family — set in code, not komet.css, because only code can
     * ask the resolver whether the family registered — with the shrunken all-caps fallback when
     * the family is absent (see the class comment). Plain text: the platform default family in
     * the name's natural case. Bold raises the requested weight; JavaFX resolves it against the
     * registered faces (it never synthesizes weight), so small-caps bold degrades to the
     * family's registered face until a bold small-caps face is bundled (ike-issues#1050).
     */
    private void applyNameFont() {
        FontWeight weight = typography.bold() ? FontWeight.BOLD : FontWeight.NORMAL;
        if (typography.smallCaps()) {
            String scFamily = SmallCapsFonts.family();
            double size = ambientFontSize > 0
                    ? ambientFontSize * (scFamily != null ? NAME_SCALE : NAME_SCALE_FALLBACK)
                    : (scFamily != null ? SC_FONT_SIZE : FALLBACK_FONT_SIZE);
            nameNode.setFont(Font.font(scFamily, weight, size));
        } else {
            double size = ambientFontSize > 0 ? ambientFontSize * NAME_SCALE : SC_FONT_SIZE;
            nameNode.setFont(Font.font(null, weight, size));
        }
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
        applyStyleLayers();
        if (!standalone) {
            nameNode.textNode().setFill(null);
            return;
        }
        nameNode.textNode().setFill(Color.web(isInactive() ? LABEL_INACTIVE : LABEL_ACTIVE));
        nameNode.textNode().setStrikethrough(isInactive() && SPEC.inactiveStrikethrough());
    }

    /**
     * Composes the badge's inline style from its two independent layers: the standalone pill
     * (for scenes no stylesheet reaches) and the multi-line row alignment. The alignment must
     * be <em>inline</em>, not merely code-set: {@code -fx-alignment} is CSS-styleable on an
     * {@code HBox}, and komet.css's {@code .koncept-chip} rule pins {@code center-left} — an
     * author stylesheet outranks a {@code setAlignment} call, so in a Komet scene a code-set
     * {@code TOP_LEFT} silently loses. An inline style outranks the stylesheet in turn, in
     * every scene.
     */
    private void applyStyleLayers() {
        StringBuilder style = new StringBuilder();
        if (standalone) {
            style.append(STANDALONE_PILL_STYLE);
        }
        if (multiLineLabel) {
            style.append(" -fx-alignment: top-left;");
        }
        setStyle(style.isEmpty() ? null : style.toString());
    }

    /**
     * Switches the name between the single-line ellipsising label (the default) and the
     * multi-line wrapping label (ike-issues#1036). A multi-line badge always shows its whole
     * name, growing in height as its host narrows it — for wrapping contexts, where the badge
     * folds like the text beside it (the assistant's table cells); single-line contexts keep
     * the default ellipsis and get the multi-line form on the identity tooltip and the
     * click-to-expand popover instead.
     *
     * @param multiLine {@code true} to wrap the name across lines; {@code false} to ellipsise
     */
    public final void setMultiLineLabel(boolean multiLine) {
        this.multiLineLabel = multiLine;
        nameNode.setWrapText(multiLine);
        // Rehome the definition popout for the mode: inline after the last wrapped word in
        // multi-line, a row child in single-line.
        refreshPopout();
        applyCompanionSeating();
    }

    /**
     * Seats the badge's companions for the current label mode. Multi-line: the row is
     * top-aligned and every companion — sigil, status cluster, identicon on the left, the
     * definition popout on the right — gets a computed top margin centring it on the
     * <em>first line's</em> band, like a paragraph with a lead-in icon and a trailing link
     * marker; never floating beside the middle of the wrapped block. (Baseline row alignment
     * cannot express this: the row baseline resolves against the tallest companion —
     * the padded popout button — and shifts everything else down.) Single-line: centre
     * seating, the ikmdev/komet#883 ruling, with margins cleared.
     */
    private void applyCompanionSeating() {
        // Both the property and the inline layer: the property serves stylesheet-free scenes
        // and honest reads via getAlignment(); the inline layer is what actually wins where
        // komet.css styles the chip (see applyStyleLayers).
        setAlignment(multiLineLabel ? Pos.TOP_LEFT : Pos.CENTER_LEFT);
        applyStyleLayers();
        setFillHeight(!multiLineLabel);
        double lineHeight = multiLineLabel ? oneLineHeight() : 0;
        for (Node child : getChildren()) {
            if (child == nameNode) {
                HBox.setMargin(child, null);
                continue;
            }
            if (multiLineLabel) {
                double childHeight = child.prefHeight(-1);
                double top = Math.max(0, (lineHeight - childHeight) / 2);
                HBox.setMargin(child, new Insets(top, 0, 0, 0));
            } else {
                HBox.setMargin(child, null);
            }
        }
    }

    /** One line's height in the name's current font — the first-line band companions seat on. */
    private double oneLineHeight() {
        Text probe = new Text("Xg");
        probe.setFont(nameNode.textNode().getFont());
        return probe.getLayoutBounds().getHeight();
    }

    /**
     * Whether the name wraps across lines instead of ellipsising.
     *
     * @return {@code true} when the multi-line label is enabled
     */
    public final boolean isMultiLineLabel() {
        return multiLineLabel;
    }

    /**
     * Builds the expanded multi-line rendering of this badge (ike-issues#1036): the same
     * component with the same anatomy — kind sigil, status cluster, identicon, and the
     * <em>full wrapped name</em> — capped at {@code maxWidthPx} and styled inline (standalone),
     * so it renders identically in a tooltip, a popover, or any other scene the stylesheet may
     * not reach. A view-backed badge resolves its twin fresh through the same view (so the
     * expansion is as current as the coordinate), and the twin of a badge with a known component
     * is a full drag source with the definition popout; a presentation-only badge yields a
     * presentation-only twin carrying this badge's displayed state.
     *
     * @param maxWidthPx the width cap the wrapped name folds to
     * @return the expanded badge (never {@code null})
     */
    public KonceptBadge expandedRendering(double maxWidthPx) {
        KonceptBadge expanded;
        if (viewProperties != null && nid != UNKNOWN_NID) {
            expanded = new KonceptBadge(nid, publicId, viewProperties);
            if (expanded.premiseType != premiseType) {
                expanded.setPremiseType(premiseType);
            }
        } else {
            expanded = (nid != UNKNOWN_NID)
                    ? new KonceptBadge(nid, publicId, conceptName)
                    : new KonceptBadge(publicId, conceptName);
            expanded.setKind(kind);
            expanded.setStatus(status);
            expanded.setInactive(inactive);
        }
        expanded.setConceptExpected(conceptExpected);
        expanded.setSctid(sctid);
        expanded.setPopoutPosition(popoutPosition);
        if (popoutSuppressed) {
            expanded.setDefinitionPopoutVisible(false);
        }
        // The twin renders with this badge's typography; the reserved status slot is not copied —
        // alignment is a stacked-siblings concern, and the expansion stands alone.
        expanded.setLabelTypography(typography);
        if (ambientFontSize > 0) {
            expanded.setAmbientFontSize(ambientFontSize);
        }
        expanded.setMultiLineLabel(true);
        expanded.setStandaloneStyling(true);
        // A Labeled (the tooltip) and a PopOver both lay a node out at its preferred size, so
        // the width cap must live on the preference itself: natural size when it fits, the cap
        // when the one-line name would exceed it (the multi-line name then wraps to it). The
        // measurement is honest here — every font in the badge is set programmatically, so
        // nothing about the natural width waits on CSS.
        double natural = expanded.prefWidth(-1);
        if (natural > maxWidthPx) {
            expanded.setPrefWidth(maxWidthPx);
        }
        expanded.setMaxWidth(maxWidthPx);
        return expanded;
    }

    /**
     * Toggles the click-to-expand popover on a truncated badge (ike-issues#1036): a primary
     * click on an ellipsised name opens the expanded multi-line rendering in a transient
     * auto-hiding {@link PopOver} (the definition popout's popover convention); a click while it
     * is open closes it. Drags and clicks on the popout affordance never arrive here, and an
     * un-truncated badge ignores clicks entirely.
     */
    private void onClickExpandFromEllipsis(MouseEvent event) {
        if (event.getButton() != MouseButton.PRIMARY || !event.isStillSincePress()) {
            return;
        }
        if (expandedPopover != null && expandedPopover.isShowing()) {
            expandedPopover.hide();
            expandedPopover = null;
            event.consume();
            return;
        }
        if (!nameNode.isEllipsized()) {
            return;
        }
        KonceptBadge expanded;
        try {
            expanded = expandedRendering(EXPANDED_MAX_WIDTH);
        } catch (RuntimeException e) {
            LOG.warn("Could not build the expanded rendering for nid {}", nid, e);
            return;
        }
        StackPane content = new StackPane(expanded);
        content.setPadding(new Insets(10));
        PopOver popover = new PopOver(content);
        popover.setDetachable(false);
        popover.setHeaderAlwaysVisible(false);
        popover.setCloseButtonEnabled(false);
        popover.setOnHidden(hidden -> {
            if (expandedPopover == popover) {
                expandedPopover = null;
            }
        });
        expandedPopover = popover;
        popover.show(this, event.getScreenX(), event.getScreenY());
        event.consume();
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
        // Plain-text typography (ike-issues#1050) shows the natural case untransformed; the
        // all-caps transform belongs to the small-caps rendering's font-absent fallback alone.
        nameNode.setText(typography.smallCaps()
                ? displayText(name, SmallCapsFonts.family())
                : (name == null ? "" : name));
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

    /**
     * Installs the single identity tooltip, once. Its content is rebuilt each time it shows
     * (never at construction), so the name, state, SCTID, and the expanded rendering are always
     * the badge's current ones — a later {@code setConceptName}/{@code setSctid} can never leave
     * a stale tooltip behind.
     */
    private void installTooltip() {
        if (identityTooltip != null) {
            return;
        }
        identityTooltip = new Tooltip();
        identityTooltip.setOnShowing(showing -> refreshTooltipContent());
        Tooltip.install(this, identityTooltip);
    }

    /**
     * Rebuilds the identity tooltip's content for this showing: the expanded multi-line
     * rendering of the badge as the graphic (the full name as a real badge, ike-issues#1036)
     * with the identity details beneath it. A badge already showing its whole name — the
     * multi-line mode — needs no graphic and keeps the plain identity text; so does the rare
     * badge whose expansion fails to build.
     */
    private void refreshTooltipContent() {
        if (!multiLineLabel) {
            try {
                identityTooltip.setGraphic(expandedRendering(EXPANDED_MAX_WIDTH));
                identityTooltip.setContentDisplay(ContentDisplay.TOP);
                identityTooltip.setText(identityText(false));
                return;
            } catch (RuntimeException e) {
                LOG.warn("Could not build the tooltip's expanded rendering for nid {}", nid, e);
            }
        }
        identityTooltip.setGraphic(null);
        identityTooltip.setText(identityText(true));
    }

    /**
     * The identity tooltip's text: the inactive note, then (optionally) the name, then the
     * SCTID/UUID/nid details. The name is included only when the tooltip carries no expanded
     * badge graphic — beside the graphic it would repeat what the wrapped badge already shows.
     *
     * @param includeName whether to include the displayed name line
     * @return the tooltip text, possibly empty
     */
    private String identityText(boolean includeName) {
        StringBuilder tip = new StringBuilder();
        if (inactive) {
            tip.append("Inactive — retired in this view\n");
        }
        if (includeName) {
            tip.append(conceptName == null ? "" : conceptName);
        }
        if (sctid != null) {
            tip.append("\nSCTID: ").append(sctid);
        }
        if (publicId != null) {
            tip.append("\nUUID: ").append(publicId.idString());
        }
        if (nid != UNKNOWN_NID) {
            tip.append("\nnid: ").append(nid);
        }
        return tip.toString().strip();
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
        return computeStatus(nid, viewProperties.calculator(), premiseType);
    }

    /**
     * Computes the taxonomic classification from a bare {@link ViewCalculator} — the form drag
     * sources use, which carry a calculator but no {@link ViewProperties}
     * (ike-issues#861: the drag glyph renders the same status cluster as the badge).
     *
     * @param nid         the concept nid
     * @param calculator  the view calculator used to read parents and the logical definition
     * @param premiseType the premise type to read the axiom tree under; falls back to
     *                    {@link PremiseType#STATED} when no tree exists for the requested premise
     * @return the classification, or {@link KonceptStatus#NONE} when the concept has no resolvable
     *         logical definition
     */
    public static KonceptStatus computeStatus(int nid, ViewCalculator calculator, PremiseType premiseType) {
        if (nid == UNKNOWN_NID || nid == -1 || nid == TinkarTerm.UNINITIALIZED_COMPONENT.nid()) {
            return KonceptStatus.NONE;
        }
        IntIdList parents;
        try {
            parents = calculator.navigationCalculator().parentsOf(nid);
        } catch (RuntimeException ex) {
            LOG.warn("Could not resolve parents for nid {}", nid, ex);
            parents = IntIds.list.empty();
        }
        Latest<DiTreeEntity> definition = calculator.getAxiomTreeForEntity(nid, premiseType);
        if (!definition.isPresent()) {
            definition = calculator.getAxiomTreeForEntity(nid, PremiseType.STATED);
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
