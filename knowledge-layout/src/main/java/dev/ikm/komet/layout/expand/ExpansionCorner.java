package dev.ikm.komet.layout.expand;

import dev.ikm.tinkar.common.bind.EnumConceptBinding;
import dev.ikm.tinkar.common.bind.annotations.names.RegularName;
import dev.ikm.tinkar.common.bind.annotations.publicid.PublicIdAnnotation;
import dev.ikm.tinkar.common.bind.annotations.publicid.UuidAnnotation;
import javafx.geometry.HPos;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.geometry.VPos;

/**
 * The corner of a figure in which its {@link KlExpandable} "expand to full surface" icon is drawn.
 * The default is {@link #BOTTOM_RIGHT} — the fullscreen-video convention (the most recognizable
 * "go fullscreen" location); top-right is the panel/window-maximize convention. Each constant is an
 * {@link EnumConceptBinding} carrying a pinned {@code publicId()} (frozen, freeze-tested) so the
 * choice can become a Tinkar concept later, and it maps to a JavaFX {@link Pos} for placement in a
 * {@code StackPane}.
 */
public enum ExpansionCorner implements EnumConceptBinding {

    /** Top-left corner. */
    @PublicIdAnnotation(@UuidAnnotation("e0d4f79c-ba0d-5da9-9aba-b95d98433390"))
    @RegularName("Top left")
    TOP_LEFT(Pos.TOP_LEFT),

    /** Top-right corner (panel/window-maximize convention). */
    @PublicIdAnnotation(@UuidAnnotation("35c078a2-a499-5919-9cfc-c0c624a40e49"))
    @RegularName("Top right")
    TOP_RIGHT(Pos.TOP_RIGHT),

    /** Bottom-left corner. */
    @PublicIdAnnotation(@UuidAnnotation("df862aa4-78d4-5c41-9394-d0ff98f471ec"))
    @RegularName("Bottom left")
    BOTTOM_LEFT(Pos.BOTTOM_LEFT),

    /** Bottom-right corner — the default (fullscreen-video convention). */
    @PublicIdAnnotation(@UuidAnnotation("93f4b913-1437-5955-a0dc-b93f147e0473"))
    @RegularName("Bottom right")
    BOTTOM_RIGHT(Pos.BOTTOM_RIGHT);

    /** The default corner for the expand affordance. */
    public static final ExpansionCorner DEFAULT = BOTTOM_RIGHT;

    private final Pos pos;

    ExpansionCorner(Pos pos) {
        this.pos = pos;
    }

    /**
     * The JavaFX alignment for placing the expand icon in this corner of a {@code StackPane}.
     *
     * @return the {@link Pos} corresponding to this corner
     */
    public Pos pos() {
        return pos;
    }

    /**
     * Grows {@code base} on the edges this corner shares with a figure's scroll bars, so an icon
     * placed here never sits on top of one. A vertical scroll bar occupies the right edge, so it only
     * displaces a right-hand corner; a horizontal scroll bar occupies the bottom edge, so it only
     * displaces a bottom corner.
     *
     * @param base                the corner's base margin
     * @param verticalBarWidth    the width of the figure's visible vertical scroll bar, or {@code 0}
     * @param horizontalBarHeight the height of the figure's visible horizontal scroll bar, or {@code 0}
     * @return the margin that keeps the icon clear of those scroll bars
     */
    public Insets insetsClearing(Insets base, double verticalBarWidth, double horizontalBarHeight) {
        Insets from = base == null ? Insets.EMPTY : base;
        double right = from.getRight() + (pos.getHpos() == HPos.RIGHT ? Math.max(0, verticalBarWidth) : 0);
        double bottom = from.getBottom() + (pos.getVpos() == VPos.BOTTOM ? Math.max(0, horizontalBarHeight) : 0);
        return new Insets(from.getTop(), right, bottom, from.getLeft());
    }
}
