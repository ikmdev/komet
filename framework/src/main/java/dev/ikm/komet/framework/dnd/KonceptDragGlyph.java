/*
 * Copyright © 2026 Knowledge Graphlet / IKE Network
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     https://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package dev.ikm.komet.framework.dnd;

import dev.ikm.komet.framework.Identicon;
import dev.ikm.komet.framework.controls.EllipsisText;
import dev.ikm.komet.framework.graphics.SmallCapsFonts;
import dev.ikm.tinkar.common.id.PublicId;
import dev.ikm.tinkar.common.service.PrimitiveData;
import dev.ikm.tinkar.coordinate.stamp.calculator.Latest;
import dev.ikm.tinkar.coordinate.view.calculator.ViewCalculator;
import dev.ikm.tinkar.entity.EntityVersion;
import javafx.geometry.Bounds;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Group;
import javafx.scene.Node;
import javafx.scene.Scene;
import javafx.scene.SnapshotParameters;
import javafx.scene.control.Label;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.image.PixelReader;
import javafx.scene.image.PixelWriter;
import javafx.scene.image.WritableImage;
import javafx.scene.input.Dragboard;
import javafx.scene.input.TransferMode;
import javafx.scene.layout.HBox;
import javafx.scene.layout.StackPane;
import javafx.embed.swing.SwingFXUtils;
import javafx.scene.paint.Color;
import javafx.scene.text.Font;
import javafx.scene.text.Text;
import javafx.scene.transform.Affine;
import javafx.stage.Screen;

import javax.imageio.ImageIO;
import java.nio.file.Files;
import java.nio.file.Path;
import dev.ikm.komet.framework.controls.KonceptBadge;
import dev.ikm.komet.framework.controls.KonceptKindResolver;
import dev.ikm.komet.framework.controls.KonceptSigils;
import dev.ikm.komet.framework.controls.KonceptStatus;
import dev.ikm.tinkar.coordinate.logic.PremiseType;
import network.ike.docs.konceptcore.KonceptKind;

import java.util.Locale;

/**
 * The one koncept drag glyph, built <em>from concept identity</em> ({@link PublicId} → LifeHash
 * identicon + store-resolved name + inactive state) rather than snapshotted from whatever node
 * happens to be under the pointer ({@code IKE-Network/ike-issues#854}). This is what makes a
 * concept's drag image pixel-identical across every single-concept drag source — the assistant
 * chips, the concept navigators, and the concept/component list controls — including the two
 * navigator sources whose cells carry no identicon and so could never be converged by snapshot.
 *
 * <p>The glyph is a dedicated, headless-rendered pill — an identicon and a width-bounded small-caps
 * name in a soft rounded pill, laid out in a throwaway {@link Scene} so CSS and sizing resolve, then
 * snapshotted 1:1 at fixed integer geometry so it renders to the pixel. A long name ellipsises with
 * {@code …} rather than producing a very wide image the OS then downscales. Lifted from the
 * assistant's original {@code KonceptDragImage}, now the shared canonical builder.
 *
 * <p>The drag-affordance border is {@linkplain #setBorder(Color, double) configurable} so the
 * appearance can be revised without re-touching the builder; it defaults to the shared floating
 * grey of the #742 appearance spec (the #861 settled border policy for floating media).
 *
 * <p>All methods build live JavaFX nodes and must be called on the JavaFX application thread.
 */
public final class KonceptDragGlyph {

    // Fixed, integer glyph geometry. The glyph is snapshotted at scale 1.0 (no rescaling), and the
    // identicon and the name box are pinned to the SAME height with symmetric vertical insets, so the
    // identicon lands on integer pixel rows — vertically centred to the pixel, not a fraction off.
    /** Identicon edge (px). */
    private static final int ICON = 22;
    /** Name font size (px). */
    private static final double FONT = 16;
    /** Symmetric top/bottom padding (px) inside the pill. */
    private static final int V_PAD = 4;
    /** Left padding before the identicon (px). */
    private static final int PAD_LEFT = 4;
    /** Right padding after the name (px). */
    private static final int PAD_RIGHT = 6;
    /** Gap between the identicon and the name (px). */
    private static final int GAP = 4;
    /** Max width (px) of the name; a longer name ellipsises. */
    private static final int MAX_LABEL_WIDTH = 260;
    /** Gap placed to the right of the identicon before the cursor (px). */
    private static final double CURSOR_GAP = 4.0;
    /**
     * Component-kind letter sigil size (px) — set explicitly; no stylesheet reaches the snapshot.
     * The normative ratio is 15:12 of the name font (KonceptFigureRenderer, the renderer that
     * draws the badge-spec anatomy figures): a quarter larger than the name, bold.
     */
    private static final double SIGIL_FONT = FONT * 15.0 / 12.0;
    /** Edge length (px) of the STAMP pentagon sigil: 20:32 of the identicon, same source. */
    private static final double SIGIL_STAMP = ICON * 20.0 / 32.0;
    /**
     * Logical-status glyph size (px) — the normative 10:12 ratio of the name font from
     * komet.css ({@code .koncept-status} 10px against the badge's 12px name), applied to this
     * glyph's name font. Set explicitly; no stylesheet reaches the snapshot.
     */
    private static final double STATUS_FONT = FONT * 10.0 / 12.0;
    /** Key marking the identicon within the pill, so the cursor anchor survives a leading sigil. */
    private static final Object IDENTICON_KEY = new Object();
    /** The one shared badge appearance (ike-issues#742/#860) the palette below reads from. */
    private static final network.ike.docs.konceptcore.KonceptAppearance SPEC =
            network.ike.docs.konceptcore.KonceptAppearance.defaults();
    /** Active label colour, from the spec. */
    private static final String LABEL_ACTIVE = SPEC.labelColorHex();
    /** Inactive/retired label colour, from the spec. */
    private static final String LABEL_INACTIVE = SPEC.labelColorInactiveHex();
    /** Pill fill, from the spec. */
    private static final String PILL_FILL = SPEC.pillFillHex();
    /** Pill corner radius, from the spec. */
    private static final int PILL_RADIUS = (int) SPEC.cornerRadiusPx();

    /**
     * Outset-bevel treatment (ikmdev/komet#885 interim). The drag-view-to-native conversion
     * flattens alpha, so the image must be opaque edge to edge; blurred shadows and flat corner
     * tints both read as careless at drag-image size. This frames the pill in a crisp CSS-style
     * {@code outset} bevel instead: an asymmetric border — {@value #OUTSET_LIT}px of highlight on
     * top and left, {@value #OUTSET_SHADOW}px of shade on bottom and right (KEC 2026-07-24: the
     * lit edge trimmed to a pixel; a raised object shows a thin lit edge and a thicker shadowed
     * one) — around a {@value #OUTSET_PAD}px card reveal. Every width is an integer and nothing
     * is blurred, so the frame is pixel-exact by construction; the pill inside keeps its rounded
     * 6. Restore {@code Color.TRANSPARENT} as the snapshot fill (and drop the frame) when #885
     * resolves.
     */
    private static final int OUTSET_LIT = 1;

    /** Bevel width (px) of the shadowed bottom and right edges. */
    private static final int OUTSET_SHADOW = 2;

    /** Card reveal (px) between the bevel and the pill. */
    private static final int OUTSET_PAD = 1;

    /** Left-side frame inset (lit bevel plus reveal) — the cursor anchor's offset. */
    private static final int OUTSET_INSET = OUTSET_LIT + OUTSET_PAD;

    /** The card tone inside the bevel — light and cool, in the pill's family. */
    private static final String CARD_FILL = "#eef1f7";

    /**
     * The ONE bevel dial (per the 2026-07-24 drag-rendering handoff): both bevel tones derive
     * from this single base — classic CSS outset semantics, where highlight and shade are a
     * lighten and a darken of one colour. The base is the spec's shared floating border grey
     * (the #742/#861 floating-context colour), so the frame and the pill border sit in one
     * family. Retune the frame by moving the spec's colour alone.
     */
    private static final Color OUTSET_BASE = Color.web(SPEC.floatingBorderHex());

    /** Bevel highlight (top and left) — the base lifted 85% toward white: the lit edge. */
    private static final Color OUTSET_LIGHT = OUTSET_BASE.interpolate(Color.WHITE, 0.85);

    /** Bevel shade (bottom and right) — the base deepened (sat ×1.6, brightness ×0.82): the shadowed edge. */
    private static final Color OUTSET_SHADE = OUTSET_BASE.deriveColor(0, 1.6, 0.82, 1);

    /**
     * Supersample factor: the card is laid out and snapshotted at this scale. For a dragboard on
     * a HiDPI screen the oversampled raster is delivered <em>as the display density</em> (see
     * {@link #hiDpiDragImage}); everywhere else it is box-downsampled exactly
     * {@value #SUPERSAMPLE}:1 back to the fixed 1× geometry. Every frame width is an integer at
     * 1×, so axis-aligned edges land on whole {@value #SUPERSAMPLE}×{@value #SUPERSAMPLE} blocks
     * and stay pixel-crisp, while text and the pill's rounded corners keep the supersampled
     * smoothing.
     */
    private static final int SUPERSAMPLE = 2;

    /**
     * Whether a dragboard on a HiDPI screen receives the full-density drag image via the
     * {@code @2x} variant convention (KEC 2026-07-24: detect the display and provide the best
     * image for it). The screen scale is read from {@link Screen#getOutputScaleX()}; delivery
     * writes the raster pair to temp files and reloads through the JavaFX {@code name@2x.png}
     * resolution-variant loader, which is the one public route to an {@link Image} that carries
     * a pixel density. VALIDATED 2026-07-24 by pixel forensics on a full-resolution retina
     * capture of a live drag: the glyph occupied its correct point size at exactly 2× device
     * density with single-device-pixel edge transitions — the platform pipeline preserves the
     * density. The 1× supersampled path remains the fallback and is used whenever delivery
     * fails (and by every non-dragboard consumer).
     */
    private static final boolean HIDPI_DRAG_VIEW = true;

    /**
     * The configurable drag-affordance border colour — the shared floating border the #742
     * appearance spec assigns to floating media (drag, Zulip/email, HTML email), reconciled
     * from the earlier subtle blue per the #861 settled border policy.
     */
    private static Color borderColor = Color.web(SPEC.floatingBorderHex());
    /** The configurable drag-affordance border width (px) — the spec's floating width. */
    private static double borderWidth = SPEC.floatingBorderWidthPx();

    private KonceptDragGlyph() {
    }

    /**
     * Sets the drag glyph's border, so the appearance can be revised in one place
     * ({@code IKE-Network/ike-issues#854} — border policy pending under #742).
     *
     * @param color the border colour; {@code null} leaves it unchanged
     * @param width the border width in px
     */
    public static void setBorder(Color color, double width) {
        if (color != null) {
            borderColor = color;
        }
        borderWidth = width;
    }

    /**
     * Wires {@code source} as a concept drag source whose drag image is the canonical glyph and
     * whose payload is the concept proxy. Safe to call from a node constructor; the gesture is
     * skipped (not thrown) if the node is detached from its scene when the drag threshold is crossed.
     *
     * @param source   the node the drag starts on
     * @param nid      the concept nid (the drag payload)
     * @param publicId the concept id (for the identicon)
     * @param name     the store-resolved name shown in the glyph
     * @param inactive whether the concept is retired (glyph name shown in the retired colour)
     */
    public static void install(Node source, int nid, PublicId publicId, String name, boolean inactive) {
        source.setOnDragDetected(event -> {
            if (source.getScene() == null) {
                return;
            }
            try {
                double[] cursorX = new double[1];
                // Resolve the kind from the nid this installer already has, so a dragged pattern
                // keeps its sigil. Without it the glyph was built from the PublicId alone and every
                // drag came out bare, whatever the source rendered (ikmdev/komet#883).
                Image image = render(glyph(kind(nid), publicId, name, inactive), cursorX, true);
                Dragboard dragboard = source.startDragAndDrop(TransferMode.COPY);
                dragboard.setDragView(image, cursorX[0], image.getHeight());
                dragboard.setContent(KometClipboard.forComponent(nid));
                event.consume();
            } catch (RuntimeException e) {
                // A drag-image failure must never break the gesture; the drag proceeds without an image.
            }
        });
    }

    /**
     * Places the canonical single-concept drag view on {@code dragboard} — for a drag source that
     * manages its own payload (the navigators set their own multi-format clipboard content). The
     * cursor sits just to the right of the identicon with its tip on the image's bottom border.
     *
     * @param dragboard the active dragboard
     * @param publicId  the concept id
     * @param name      the store-resolved name
     * @param inactive  whether the concept is retired
     */
    public static void setDragView(Dragboard dragboard, PublicId publicId, String name, boolean inactive) {
        setDragView(dragboard, KonceptKind.CONCEPT, publicId, name, inactive);
    }

    /**
     * Places the canonical single-component drag view, leading with {@code kind}'s sigil — for a
     * drag source that knows the component kind but has no {@link ViewCalculator} to resolve it
     * from (ikmdev/komet#883).
     *
     * @param dragboard the active dragboard
     * @param kind      the component kind; {@link KonceptKind#CONCEPT} draws the bare pill
     * @param publicId  the component id (drives the identicon)
     * @param name      the store-resolved name
     * @param inactive  whether the component is retired
     */
    public static void setDragView(Dragboard dragboard, KonceptKind kind, PublicId publicId,
                                   String name, boolean inactive) {
        setDragView(dragboard, kind, KonceptStatus.NONE, publicId, name, inactive);
    }

    /**
     * Places the canonical single-component drag view, leading with {@code kind}'s sigil or — for
     * a bare Koncept — the logical-status copula cluster (ike-issues#861): the drag image carries
     * the same one leading mark as the on-screen badge. For a drag source that already resolved
     * the status (a {@code KonceptBadge} passes its live status at gesture time).
     *
     * @param dragboard the active dragboard
     * @param kind      the component kind; {@link KonceptKind#CONCEPT} draws the status cluster
     * @param status    the concept's logical-definition status; {@link KonceptStatus#NONE} stays bare
     * @param publicId  the component id (drives the identicon)
     * @param name      the store-resolved name
     * @param inactive  whether the component is retired
     */
    public static void setDragView(Dragboard dragboard, KonceptKind kind, KonceptStatus status,
                                   PublicId publicId, String name, boolean inactive) {
        double[] cursorX = new double[1];
        Image image = render(glyph(kind, status, publicId, name, inactive), cursorX, true);
        dragboard.setDragView(image, cursorX[0], image.getHeight());
    }

    /**
     * Places the canonical single-concept drag view, resolving the name and inactive state from the
     * store — for a drag source that has a nid and a view but manages its own payload.
     *
     * @param dragboard the active dragboard
     * @param nid       the concept nid
     * @param viewCalc  the view for resolving the name and active state
     */
    public static void setDragView(Dragboard dragboard, int nid, ViewCalculator viewCalc) {
        // Resolve the kind AND the status here: this overload has the nid and the view, which is
        // everything both marks need. A pattern therefore drags with its sigil (ikmdev/komet#883)
        // and a concept with its copula cluster (ike-issues#861); the PublicId overloads cannot
        // know either and stay bare, as before.
        double[] cursorX = new double[1];
        Image image = render(glyph(kind(nid, viewCalc), status(nid, viewCalc),
                PrimitiveData.publicId(nid), name(nid, viewCalc), isInactive(nid, viewCalc)),
                cursorX, true);
        dragboard.setDragView(image, cursorX[0], image.getHeight());
    }

    /**
     * The logical-status classification for {@code nid}, or {@link KonceptStatus#NONE} when it
     * cannot be resolved — a drag must never fail because the status could not be determined.
     *
     * @param nid      the component nid
     * @param viewCalc the view used to classify; {@code null} yields {@link KonceptStatus#NONE}
     * @return the resolved status, never {@code null}
     */
    private static KonceptStatus status(int nid, ViewCalculator viewCalc) {
        if (viewCalc == null) {
            return KonceptStatus.NONE;
        }
        try {
            return KonceptBadge.computeStatus(nid, viewCalc, PremiseType.INFERRED);
        } catch (RuntimeException e) {
            return KonceptStatus.NONE;
        }
    }

    /**
     * The component kind for {@code nid}, or {@link KonceptKind#CONCEPT} when it cannot be resolved
     * — a drag must never fail because the sigil could not be determined.
     *
     * @param nid      the component nid
     * @param viewCalc the view used to resolve the kind
     * @return the resolved kind, never {@code null}
     */
    /**
     * The component kind for {@code nid} without a view — a concept, pattern, or stamp follows from
     * the entity type alone; a semantic degrades to {@link KonceptKind#SEMANTIC} rather than being
     * distinguished as a description.
     *
     * @param nid the component nid
     * @return the resolved kind, never {@code null}
     */
    private static KonceptKind kind(int nid) {
        return kind(nid, null);
    }

    private static KonceptKind kind(int nid, ViewCalculator viewCalc) {
        try {
            return KonceptKindResolver.resolve(nid, viewCalc);
        } catch (RuntimeException e) {
            return KonceptKind.CONCEPT;
        }
    }

    /**
     * Places the canonical <em>multi</em>-concept drag view on {@code dragboard}: the lead concept's
     * pill with a trailing count badge, so a many-concept drag reads as one canonical glyph rather
     * than a composite of tile snapshots.
     *
     * @param dragboard    the active dragboard
     * @param leadPublicId the first concept's id (its identicon leads the glyph)
     * @param leadName     the first concept's store-resolved name
     * @param leadInactive whether the first concept is retired
     * @param count        the total number of concepts being dragged (badge shown when {@code > 1})
     */
    public static void setMultiDragView(Dragboard dragboard, PublicId leadPublicId, String leadName,
                                        boolean leadInactive, int count) {
        double[] cursorX = new double[1];
        Image image = renderMulti(leadPublicId, leadName, leadInactive, count, cursorX, true);
        dragboard.setDragView(image, cursorX[0], image.getHeight());
    }

    /**
     * Places the canonical multi-concept drag view, resolving the lead concept's name and inactive
     * state from the store. A {@code count} of 1 shows no badge — identical to the single glyph — so
     * a drag source can route both single- and multi-select selections through this one call.
     *
     * @param dragboard the active dragboard
     * @param leadNid   the first concept's nid (its identicon leads the glyph)
     * @param viewCalc  the view for resolving the lead concept's name and active state
     * @param count     the total number of concepts being dragged (badge shown when {@code > 1})
     */
    public static void setMultiDragView(Dragboard dragboard, int leadNid, ViewCalculator viewCalc, int count) {
        // As the single-glyph nid overload: the lead component's kind and status are resolvable
        // here, so the lead pill carries its one leading mark (ikmdev/komet#883, ike-issues#861).
        double[] cursorX = new double[1];
        HBox glyph = glyph(kind(leadNid, viewCalc), status(leadNid, viewCalc),
                PrimitiveData.publicId(leadNid),
                name(leadNid, viewCalc), isInactive(leadNid, viewCalc));
        if (count > 1) {
            glyph.getChildren().add(countBadge(count));
        }
        Image image = render(glyph, cursorX, true);
        dragboard.setDragView(image, cursorX[0], image.getHeight());
    }

    /**
     * The canonical multi-concept drag image (lead pill + count badge), for a caller that wants the
     * {@link Image} directly.
     *
     * @param leadPublicId the first concept's id
     * @param leadName     the first concept's store-resolved name
     * @param leadInactive whether the first concept is retired
     * @param count        the total number of concepts (badge shown when {@code > 1})
     * @return the multi-concept drag image
     */
    public static Image multiImage(PublicId leadPublicId, String leadName, boolean leadInactive, int count) {
        return renderMulti(leadPublicId, leadName, leadInactive, count, new double[1], false);
    }

    private static Image renderMulti(PublicId leadPublicId, String leadName, boolean leadInactive,
                                     int count, double[] cursorX, boolean forDragboard) {
        HBox glyph = glyph(leadPublicId, leadName, leadInactive);
        if (count > 1) {
            glyph.getChildren().add(countBadge(count));
        }
        return render(glyph, cursorX, forDragboard);
    }

    /**
     * The canonical single-concept drag image, for a caller that wants the {@link Image} directly.
     *
     * @param publicId the concept id
     * @param name     the store-resolved name
     * @param inactive whether the concept is retired
     * @return the drag image
     */
    public static Image image(PublicId publicId, String name, boolean inactive) {
        return render(glyph(publicId, name, inactive), new double[1]);
    }

    /**
     * The canonical single-concept drag image, resolving the name and inactive state from the store.
     *
     * @param nid      the concept nid
     * @param viewCalc the view for resolving the name and active state
     * @return the drag image
     */
    public static Image image(int nid, ViewCalculator viewCalc) {
        PublicId publicId = PrimitiveData.publicId(nid);
        return image(publicId, name(nid, viewCalc), isInactive(nid, viewCalc));
    }

    /**
     * The canonical drag image with an explicit kind and status — the full one-leading-mark form
     * (kind sigil for a marked kind, status copula cluster for a bare Koncept; ike-issues#861) —
     * for a caller that wants the {@link Image} directly.
     *
     * @param kind     the component kind; {@link KonceptKind#CONCEPT} draws the status cluster
     * @param status   the concept's logical-definition status; {@link KonceptStatus#NONE} stays bare
     * @param publicId the component id (drives the identicon)
     * @param name     the store-resolved name
     * @param inactive whether the component is retired
     * @return the drag image
     */
    public static Image image(KonceptKind kind, KonceptStatus status, PublicId publicId,
                              String name, boolean inactive) {
        return render(glyph(kind, status, publicId, name, inactive), new double[1]);
    }

    /**
     * The store name for {@code nid}: the view's coordinate-preferred description (the language
     * coordinate's description-type priority), else the nid — the same resolution the on-screen
     * badge uses ({@code KonceptBadge}), so the drag name matches the chip it left
     * (ike-issues#942).
     *
     * @param nid      the concept nid
     * @param viewCalc the resolving view; {@code null} yields an empty name (icon-only glyph)
     * @return the resolved name, never {@code null}
     */
    public static String name(int nid, ViewCalculator viewCalc) {
        if (viewCalc == null) {
            return "";
        }
        return viewCalc.getDescriptionTextOrNid(nid);
    }

    /**
     * Whether {@code nid}'s latest version is inactive/retired in {@code viewCalc}.
     *
     * @param nid      the concept nid
     * @param viewCalc the resolving view; {@code null} yields {@code false}
     * @return {@code true} if the latest version is inactive
     */
    public static boolean isInactive(int nid, ViewCalculator viewCalc) {
        if (viewCalc == null) {
            return false;
        }
        try {
            Latest<EntityVersion> latest = viewCalc.stampCalculator().latest(nid);
            return latest.isPresent() && latest.get().inactive();
        } catch (RuntimeException e) {
            return false;
        }
    }

    /** Builds the pill: identicon + width-bounded, ellipsising small-caps name in a bordered pill. */
    private static HBox glyph(PublicId publicId, String name, boolean inactive) {
        return glyph(KonceptKind.CONCEPT, KonceptStatus.NONE, publicId, name, inactive);
    }

    /** Builds the pill with a kind sigil and no status — the pre-#861 shape, for bare callers. */
    private static HBox glyph(KonceptKind kind, PublicId publicId, String name, boolean inactive) {
        return glyph(kind, KonceptStatus.NONE, publicId, name, inactive);
    }

    /**
     * Builds the pill, leading with its one mark — {@code kind}'s sigil when it carries one, or a
     * bare Koncept's logical-status copula cluster ({@code ≡}/{@code ⊑}/{@code ⊤} with the
     * {@code ⋎} fork appended for multi-parent) — so the drag image is as honest about the
     * component as the badge is (ikmdev/komet#883, ike-issues#861). Kind sigils and status marks
     * never co-occur; both composite into the same fixed integer geometry; nothing is rescaled.
     *
     * <p>The marks are sized and coloured explicitly rather than by style class: {@link #render}
     * snapshots in a throwaway scene with no stylesheet attached, so a CSS-sized glyph would fall
     * back to the default font here and render at the wrong size. The status colours come straight
     * from the single-sourced koncept-core vocabulary ({@code KonceptStatus.core()}).
     */
    private static HBox glyph(KonceptKind kind, KonceptStatus status, PublicId publicId,
                              String name, boolean inactive) {
        // Synchronous identicon: the glyph is snapshotted immediately, so the async placeholder that
        // generateIdenticon returns on first lookup would snapshot blank (ike-issues#854 — a concept
        // whose identicon a navigator row never rendered dragged with no identicon).
        ImageView icon = Identicon.generateIdenticonSync(publicId, ICON, ICON);
        icon.setSmooth(false);
        icon.setFitWidth(ICON);
        icon.setFitHeight(ICON);

        // True small caps via the bundled dedicated family: the name renders in its natural case
        // (capitals full height, the rest small capitals). Absent the font, fall back to shrunken
        // all-caps so the glyph never breaks on a missing resource.
        String safeName = name == null ? "" : name;
        String scFamily = SmallCapsFonts.family();
        String text = scFamily != null ? safeName : safeName.toUpperCase(Locale.ROOT);
        Font font = scFamily != null ? Font.font(scFamily, FONT) : Font.font(FONT);

        // A Text node (not a Label) so the name can carry a strikethrough — the dedicated
        // inactive/retired signal (#586), matching the on-screen chip; Text has no overrun, so the
        // width bound is applied by ellipsising the string by hand.
        Text nameText = new Text(EllipsisText.fitToWidth(text, font, MAX_LABEL_WIDTH));
        nameText.setFont(font);
        nameText.setFill(Color.web(inactive ? LABEL_INACTIVE : LABEL_ACTIVE));
        nameText.setStrikethrough(inactive);

        HBox glyph = new HBox(GAP, icon, nameText);
        KonceptSigils.create(kind, SIGIL_STAMP, SIGIL_FONT)
                .ifPresent(sigil -> glyph.getChildren().add(0, sigil));
        // The one-mark rule (ike-issues#742/#861): only a bare Koncept leads with its status
        // cluster; a kind sigil and a status mark never co-occur.
        if (kind == KonceptKind.CONCEPT && status != null && status.hasGlyph()) {
            glyph.getChildren().add(0, statusCluster(status));
        }
        glyph.setAlignment(Pos.CENTER_LEFT);
        glyph.setPadding(new Insets(V_PAD, PAD_RIGHT, V_PAD, PAD_LEFT));
        glyph.setStyle(glyphStyle());
        // The cursor anchors just right of the IDENTICON, which is no longer necessarily the first
        // child once a sigil leads the pill.
        glyph.getProperties().put(IDENTICON_KEY, icon);
        return glyph;
    }

    /**
     * The leading logical-status cluster for a bare Koncept (ike-issues#861): the copula glyph in
     * its status colour with the fork appended for multi-parent, coloured straight from the
     * single-sourced koncept-core vocabulary — the snapshot has no stylesheet, so the colours are
     * set inline, exactly as the adoc renderer does.
     */
    private static HBox statusCluster(KonceptStatus status) {
        Text copula = new Text(status.glyph());
        copula.setFont(Font.font(STATUS_FONT));
        copula.setFill(Color.web(status.core().colorHex()));
        HBox cluster = new HBox(copula);
        if (status.isMultiParent()) {
            Text fork = new Text(KonceptStatus.MULTI_PARENT_GLYPH);
            fork.setFont(Font.font(STATUS_FONT));
            fork.setFill(Color.web(
                    network.ike.docs.konceptcore.KonceptStatus.MULTI_PARENT_COLOR_HEX));
            cluster.getChildren().add(fork);
        }
        cluster.setAlignment(Pos.CENTER);
        return cluster;
    }

    /** The trailing count badge for a multi-concept drag: a small accent pill showing the total. */
    private static Label countBadge(int count) {
        Label badge = new Label(Integer.toString(count));
        badge.setFont(Font.font(FONT * 0.8));
        badge.setTextFill(Color.WHITE);
        badge.setAlignment(Pos.CENTER);
        badge.setMinHeight(ICON);
        badge.setPrefHeight(ICON);
        badge.setMaxHeight(ICON);
        badge.setMinWidth(ICON);
        badge.setStyle("-fx-background-color: #2F5FA6; -fx-background-radius: 9; -fx-padding: 0 6 0 6;");
        return badge;
    }

    /**
     * The pill style, built from the configurable border. The pill keeps its rounded 6; the
     * rectangle's corners outside it are colour-treated via {@link #CORNER_TINT}
     * (ikmdev/komet#885 interim), so the image is opaque edge to edge without squaring the pill.
     */
    private static String glyphStyle() {
        return "-fx-background-color: " + PILL_FILL + "; -fx-background-radius: " + PILL_RADIUS + ";"
                + " -fx-border-color: " + web(borderColor) + ";"
                + " -fx-border-width: " + borderWidth + ";"
                + " -fx-border-radius: " + PILL_RADIUS + ";";
    }

    /** The {@code #RRGGBB} form of a colour, for a JavaFX inline style. */
    private static String web(Color c) {
        return String.format(Locale.ROOT, "#%02X%02X%02X",
                (int) Math.round(c.getRed() * 255),
                (int) Math.round(c.getGreen() * 255),
                (int) Math.round(c.getBlue() * 255));
    }

    /**
     * Lays the glyph out in a throwaway scene and snapshots it at 1:1 (no rescaling), so the fixed
     * integer geometry renders to the pixel. Writes the cursor's x (just right of the identicon) into
     * {@code cursorX}.
     */
    private static Image render(HBox glyph, double[] cursorX) {
        return render(glyph, cursorX, false);
    }

    private static Image render(HBox glyph, double[] cursorX, boolean forDragboard) {
        // The outset-bevel frame (ikmdev/komet#885): per-side border colours and widths are how
        // CSS outset is defined — a thin lit top/left, a thicker shaded bottom/right — and JavaFX
        // takes them directly. All integer widths, no blur: crisp by construction, opaque edge to
        // edge.
        StackPane card = new StackPane(glyph);
        card.setPadding(new Insets(OUTSET_PAD));
        card.setStyle("-fx-background-color: " + CARD_FILL + ";"
                + " -fx-border-width: " + OUTSET_LIT + " " + OUTSET_SHADOW + " "
                + OUTSET_SHADOW + " " + OUTSET_LIT + ";"
                + " -fx-border-color: " + web(OUTSET_LIGHT) + " " + web(OUTSET_SHADE) + " "
                + web(OUTSET_SHADE) + " " + web(OUTSET_LIGHT) + ";");

        // A throwaway scene applies CSS and enables layout; it is never shown (no Stage). Off-stage
        // there is no layout pulse, so the card must be resized to its preferred size explicitly.
        new Scene(new Group(card));
        card.applyCss();
        card.resize(card.prefWidth(-1), card.prefHeight(-1));
        card.layout();

        // The identicon, not "the first child": a sigil or status cluster leads the pill, and
        // anchoring the cursor to it would drift the grab point (ikmdev/komet#883). The anchor
        // is expressed in card coordinates, so the margin shifts it too.
        Object marked = glyph.getProperties().get(IDENTICON_KEY);
        Node identicon = marked instanceof Node node ? node
                : (glyph.getChildren().isEmpty() ? null : glyph.getChildren().get(0));
        double iconRight = identicon == null ? 0 : identicon.getBoundsInParent().getMaxX();
        cursorX[0] = OUTSET_INSET + Math.max(0, iconRight) + CURSOR_GAP;

        Bounds visual = card.getLayoutBounds();
        int width = (int) Math.max(1, Math.ceil(visual.getWidth()));
        int height = (int) Math.max(1, Math.ceil(visual.getHeight()));

        // Supersample: snapshot at SUPERSAMPLE×, then box-downsample exactly back to the 1×
        // geometry (see the SUPERSAMPLE constant for why edges stay crisp while curves and text
        // gain smoothing). The scale is appended before the translation so the transform reads
        // scale∘translate — points translate in unscaled coordinates first.
        Affine transform = new Affine();
        transform.appendScale(SUPERSAMPLE, SUPERSAMPLE);
        transform.appendTranslation(-visual.getMinX(), -visual.getMinY());
        SnapshotParameters params = new SnapshotParameters();
        params.setTransform(transform);
        params.setFill(Color.web(CARD_FILL));

        WritableImage oversampled = card.snapshot(params,
                new WritableImage(width * SUPERSAMPLE, height * SUPERSAMPLE));

        // A dragboard on a HiDPI screen gets the full-density raster (KEC 2026-07-24); every
        // other consumer — and any failed delivery — gets the crisp 1× supersampled image.
        if (forDragboard && HIDPI_DRAG_VIEW && SUPERSAMPLE == 2 && outputScale() > 1.5) {
            Image hiDpi = hiDpiDragImage(oversampled, width, height);
            if (hiDpi != null) {
                return hiDpi;
            }
        }
        return downsample(oversampled, width, height, SUPERSAMPLE);
    }

    /** The largest output scale across the attached screens, or 1 when it cannot be read. */
    private static double outputScale() {
        try {
            return Screen.getScreens().stream()
                    .mapToDouble(Screen::getOutputScaleX).max().orElse(1.0);
        } catch (RuntimeException e) {
            return 1.0;
        }
    }

    /**
     * Delivers the oversampled raster as a density-carrying {@link Image} through the JavaFX
     * {@code name@2x.png} resolution-variant convention — the one public route to an image whose
     * backing pixels exceed its logical size: the 1× and 2× rasters are written to a temp pair
     * and reloaded by the variant-aware loader, which on a HiDPI screen picks the {@code @2x}
     * file and reports the logical {@code width}×{@code height}.
     *
     * @param oversampled the 2× snapshot
     * @param width       the logical (1×) width
     * @param height      the logical (1×) height
     * @return the density-carrying image, or {@code null} when delivery fails (the caller falls
     *         back to the 1× downsample; a drag must never fail for image quality)
     */
    private static Image hiDpiDragImage(WritableImage oversampled, int width, int height) {
        try {
            Path dir = Files.createTempDirectory("koncept-drag");
            dir.toFile().deleteOnExit();
            Path base = dir.resolve("glyph.png");
            Path twoX = dir.resolve("glyph@2x.png");
            ImageIO.write(SwingFXUtils.fromFXImage(
                    downsample(oversampled, width, height, SUPERSAMPLE), null), "png", base.toFile());
            ImageIO.write(SwingFXUtils.fromFXImage(oversampled, null), "png", twoX.toFile());
            base.toFile().deleteOnExit();
            twoX.toFile().deleteOnExit();
            Image image = new Image(base.toUri().toString());
            return image.isError() || image.getWidth() != width ? null : image;
        } catch (Exception e) {
            return null;
        }
    }

    /**
     * Exact {@code factor}:1 box downsample: every destination pixel is the plain average of its
     * {@code factor}×{@code factor} source block. Because the card's geometry is integer at 1×,
     * every axis-aligned edge lands on a block boundary and survives untouched; only antialiased
     * detail (text, the pill's rounded corners) blends — supersampling without edge softening.
     *
     * @param source the oversampled snapshot ({@code width*factor} × {@code height*factor})
     * @param width  the destination width (the 1× geometry)
     * @param height the destination height
     * @param factor the supersample factor
     * @return the downsampled image
     */
    private static WritableImage downsample(WritableImage source, int width, int height, int factor) {
        PixelReader reader = source.getPixelReader();
        WritableImage out = new WritableImage(width, height);
        PixelWriter writer = out.getPixelWriter();
        int samples = factor * factor;
        for (int y = 0; y < height; y++) {
            for (int x = 0; x < width; x++) {
                int a = 0;
                int r = 0;
                int g = 0;
                int b = 0;
                for (int dy = 0; dy < factor; dy++) {
                    for (int dx = 0; dx < factor; dx++) {
                        int argb = reader.getArgb(x * factor + dx, y * factor + dy);
                        a += argb >>> 24;
                        r += (argb >> 16) & 0xFF;
                        g += (argb >> 8) & 0xFF;
                        b += argb & 0xFF;
                    }
                }
                writer.setArgb(x, y, (a / samples << 24) | (r / samples << 16)
                        | (g / samples << 8) | (b / samples));
            }
        }
        return out;
    }
}
