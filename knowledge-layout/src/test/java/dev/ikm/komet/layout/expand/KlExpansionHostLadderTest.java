package dev.ikm.komet.layout.expand;

import javafx.scene.layout.BorderPane;
import javafx.scene.layout.Pane;
import javafx.scene.layout.Region;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * The ladder walk. These build scene-graph nodes but never show them, so no JavaFX toolkit is needed:
 * {@code Pane}/{@code Region} construction and parenting are toolkit-free; only rendering, CSS and
 * layout are not.
 *
 * <p>The shapes mirror the real one: a document area (a stamped {@code BorderPane}) whose sole content
 * is the surface figure's affordance host, with block figures nested inside the surface's stack, all
 * inside a stamped card and a stamped window.
 */
class KlExpansionHostLadderTest {

    /** Wraps a figure the way {@code KlExpandable.withExpandAffordance()} does. */
    private static StackPane affordanceHost(Region figure) {
        StackPane host = new StackPane(figure);
        host.getStyleClass().add(KlExpandable.HOST_STYLE_CLASS);
        return host;
    }

    @Test
    void ladderIsNearestFirstAndSkipsUnstampedPanes() {
        Region figure = new Region();
        VBox unstamped = new VBox(affordanceHost(figure));
        BorderPane document = new BorderPane(unstamped);
        BorderPane card = new BorderPane(document);
        BorderPane window = new BorderPane(card);
        KlExpansionHost.mark(document, () -> "Document", false);
        KlExpansionHost.mark(card, () -> "Card", false);
        KlExpansionHost.mark(window, () -> "Journal window", true);

        List<KlExpansionHost.Rung> ladder = KlExpansionHost.ladder(figure);

        assertEquals(3, ladder.size(), "the unstamped VBox is not a rung");
        assertSame(document, ladder.get(0).fillPane(), "nearest first");
        assertSame(card, ladder.get(1).fillPane());
        assertSame(window, ladder.get(2).fillPane());
        assertEquals("Journal window", ladder.get(2).name());
        assertTrue(ladder.get(2).osFullScreenBeyond(), "only the window has full screen beyond it");
    }

    @Test
    void aFigureSkipsItsOwnHomePane() {
        // The document area's sole content IS the surface figure — expanding into it changes nothing.
        Region surface = new Region();
        BorderPane document = new BorderPane(affordanceHost(surface));
        BorderPane card = new BorderPane(document);
        KlExpansionHost.mark(document, () -> "Document", false);
        KlExpansionHost.mark(card, () -> "Card", false);

        List<KlExpansionHost.Rung> ladder = KlExpansionHost.ladder(surface);

        assertEquals(1, ladder.size(), "the figure's own home is not a rung");
        assertSame(card, ladder.get(0).fillPane(), "so the surface's first rung is the card");
    }

    @Test
    void aBlockInsideThatSameFigureStillGetsTheDocumentRung() {
        // The block's home is the stack, not the document — so the document IS a rung for it. This must
        // not depend on the block's size, nor on the stack's padding.
        Region block = new Region();
        block.resize(2000, 9000); // taller and wider than any ancestor: still a rung
        VBox stack = new VBox(affordanceHost(block));
        BorderPane document = new BorderPane(stack);
        BorderPane card = new BorderPane(document);
        KlExpansionHost.mark(document, () -> "Document", false);
        KlExpansionHost.mark(card, () -> "Card", false);

        List<KlExpansionHost.Rung> ladder = KlExpansionHost.ladder(block);

        assertEquals(2, ladder.size());
        assertSame(document, ladder.get(0).fillPane(), "a block expands to its document first");
        assertSame(card, ladder.get(1).fillPane());
    }

    @Test
    void anUndecoratedFigureSkipsItsDirectParent() {
        Region figure = new Region();
        BorderPane home = new BorderPane(figure); // no affordance host in between
        BorderPane outer = new BorderPane(home);
        KlExpansionHost.mark(home, () -> "Home", false);
        KlExpansionHost.mark(outer, () -> "Outer", false);

        List<KlExpansionHost.Rung> ladder = KlExpansionHost.ladder(figure);

        assertEquals(1, ladder.size());
        assertSame(outer, ladder.get(0).fillPane());
    }

    @Test
    void aFigureWithNoStampedAncestorHasNoLadder() {
        Region figure = new Region();
        new BorderPane(new VBox(affordanceHost(figure)));

        assertTrue(KlExpansionHost.ladder(figure).isEmpty(), "nothing to expand into");
        assertTrue(KlExpansionHost.ladder(null).isEmpty(), "a null figure has no ladder");
    }

    @Test
    void aSiblingCardsRungIsNeverCollected() {
        Region figure = new Region();
        BorderPane cardA = new BorderPane(new VBox(affordanceHost(figure)));
        BorderPane cardB = new BorderPane();
        Pane desktop = new Pane(cardA, cardB);
        BorderPane window = new BorderPane(desktop);
        KlExpansionHost.mark(cardA, () -> "Card A", false);
        KlExpansionHost.mark(cardB, () -> "Card B", false);
        KlExpansionHost.mark(window, () -> "Journal window", true);

        List<KlExpansionHost.Rung> ladder = KlExpansionHost.ladder(figure);

        assertEquals(2, ladder.size(), "only ancestors are rungs");
        assertSame(cardA, ladder.get(0).fillPane());
        assertSame(window, ladder.get(1).fillPane());
    }

    @Test
    void markIsIdempotentAndReplacesTheRung() {
        Region figure = new Region();
        BorderPane card = new BorderPane(new VBox(affordanceHost(figure)));
        KlExpansionHost.mark(card, () -> "Old name", false);
        KlExpansionHost.mark(card, () -> "New name", true);

        List<KlExpansionHost.Rung> ladder = KlExpansionHost.ladder(figure);

        assertEquals(1, ladder.size(), "stamping twice does not add a second rung");
        assertEquals("New name", ladder.get(0).name());
        assertTrue(ladder.get(0).osFullScreenBeyond());
    }
}
