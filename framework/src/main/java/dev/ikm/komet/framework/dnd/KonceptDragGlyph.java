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
import javafx.scene.image.WritableImage;
import javafx.scene.input.Dragboard;
import javafx.scene.input.TransferMode;
import javafx.scene.layout.HBox;
import javafx.scene.paint.Color;
import javafx.scene.text.Font;
import javafx.scene.text.Text;
import javafx.scene.transform.Affine;

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
 * appearance can be revised without re-touching the builder; it defaults to a subtle blue.
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
    /** Active label colour. */
    private static final String LABEL_ACTIVE = "#2a5a8a";
    /** Inactive/retired label colour. */
    private static final String LABEL_INACTIVE = "#b00020";
    /** Pill fill (koncept palette). */
    private static final String PILL_FILL = "#e9eff6";

    /** The configurable drag-affordance border colour; a subtle blue by default. */
    private static Color borderColor = Color.web("#6E9BD1");
    /** The configurable drag-affordance border width (px). */
    private static double borderWidth = 1.5;

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
                Image image = render(glyph(publicId, name, inactive), cursorX);
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
        double[] cursorX = new double[1];
        Image image = render(glyph(publicId, name, inactive), cursorX);
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
        setDragView(dragboard, PrimitiveData.publicId(nid), name(nid, viewCalc), isInactive(nid, viewCalc));
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
        Image image = renderMulti(leadPublicId, leadName, leadInactive, count, cursorX);
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
        setMultiDragView(dragboard, PrimitiveData.publicId(leadNid),
                name(leadNid, viewCalc), isInactive(leadNid, viewCalc), count);
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
        return renderMulti(leadPublicId, leadName, leadInactive, count, new double[1]);
    }

    private static Image renderMulti(PublicId leadPublicId, String leadName, boolean leadInactive,
                                     int count, double[] cursorX) {
        HBox glyph = glyph(leadPublicId, leadName, leadInactive);
        if (count > 1) {
            glyph.getChildren().add(countBadge(count));
        }
        return render(glyph, cursorX);
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
     * The store name for {@code nid}: the fully-qualified name, else the preferred description,
     * else the nid — the same recipe the on-screen chip uses, so the drag name matches the chip.
     *
     * @param nid      the concept nid
     * @param viewCalc the resolving view; {@code null} yields an empty name (icon-only glyph)
     * @return the resolved name, never {@code null}
     */
    public static String name(int nid, ViewCalculator viewCalc) {
        if (viewCalc == null) {
            return "";
        }
        return viewCalc.getFullyQualifiedNameText(nid)
                .orElseGet(() -> viewCalc.getPreferredDescriptionTextWithFallbackOrNid(nid));
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
        glyph.setAlignment(Pos.CENTER_LEFT);
        glyph.setPadding(new Insets(V_PAD, PAD_RIGHT, V_PAD, PAD_LEFT));
        glyph.setStyle(glyphStyle());
        return glyph;
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

    /** The pill style, built from the configurable border. */
    private static String glyphStyle() {
        return "-fx-background-color: " + PILL_FILL + "; -fx-background-radius: 6;"
                + " -fx-border-color: " + web(borderColor) + ";"
                + " -fx-border-width: " + borderWidth + ";"
                + " -fx-border-radius: 6;";
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
        // A throwaway scene applies CSS and enables layout; it is never shown (no Stage). Off-stage
        // there is no layout pulse, so the glyph must be resized to its preferred size explicitly.
        new Scene(new Group(glyph));
        glyph.applyCss();
        glyph.resize(glyph.prefWidth(-1), glyph.prefHeight(-1));
        glyph.layout();

        Bounds visual = glyph.getBoundsInLocal();
        double iconRight = glyph.getChildren().isEmpty()
                ? 0 : glyph.getChildren().get(0).getBoundsInParent().getMaxX();
        cursorX[0] = Math.max(0, iconRight) + CURSOR_GAP;

        Affine transform = new Affine();
        transform.appendTranslation(-visual.getMinX(), -visual.getMinY());
        SnapshotParameters params = new SnapshotParameters();
        params.setTransform(transform);
        params.setFill(Color.TRANSPARENT);

        int width = (int) Math.max(1, Math.ceil(visual.getWidth()));
        int height = (int) Math.max(1, Math.ceil(visual.getHeight()));
        return glyph.snapshot(params, new WritableImage(width, height));
    }
}
