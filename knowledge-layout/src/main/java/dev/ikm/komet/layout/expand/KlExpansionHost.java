package dev.ikm.komet.layout.expand;

import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.layout.Pane;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.function.Supplier;

/**
 * The rungs a {@link KlExpandable} figure can expand into. A <b>host</b> is an ancestor {@link Pane}
 * that declares itself a valid expansion target; a figure expands to fill the nearest one, and from
 * there may climb to successively larger ones — a block figure filling its document, then its card,
 * then the journal window.
 *
 * <p>Hosts <em>opt in</em> rather than being discovered by walking for any {@code Pane}: most ancestors
 * cannot host an overlay honestly. A {@code ScrollPane}'s content is taller than its viewport (an
 * overlay there would be document-sized and scroll away); a {@code SplitPane} is a control whose items
 * are skin-managed; the workspace's desktop pane is a 5184&times;3240 free-positioning canvas, not a
 * viewport. Opting in is the only way to distinguish "an ancestor" from "a surface a reader would
 * recognise as a place".
 *
 * <p>A host declares itself by stamping a {@link Rung} into its node properties. That inverts the
 * dependency: {@code knowledge-layout} owns the key and never learns about the tiers above it, so the
 * journal window (in {@code kview}) can be a rung without {@code knowledge-layout} depending on kview —
 * the same idiom kview already uses to pass authored window sizes down.
 *
 * <p>All methods must be used on the JavaFX application thread.
 */
public final class KlExpansionHost {

    /** Node-property key under which a {@link Pane} declares itself an expansion host. */
    enum PropertyKeys {
        /** The {@link Rung} this pane offers; absent on panes that are not hosts. */
        EXPANSION_HOST
    }

    /**
     * One rung of the ladder: a pane a figure can expand to fill.
     *
     * @param fillPane           the pane the expanded figure fills
     * @param displayName        supplies the rung's name for the "Expand further" control. A supplier,
     *                           not a string, because a host is stamped when it is built — before the
     *                           thing that names it (a card's title) necessarily exists
     * @param osFullScreenBeyond whether OS full screen lies beyond this rung (true only for a window)
     */
    public record Rung(Pane fillPane, Supplier<String> displayName, boolean osFullScreenBeyond) {

        /**
         * This rung's display name, resolved now.
         *
         * @return the name, or the empty string if none was supplied
         */
        public String name() {
            if (displayName == null) {
                return "";
            }
            String resolved = displayName.get();
            return resolved == null ? "" : resolved;
        }
    }

    private KlExpansionHost() {
    }

    /**
     * Declares {@code pane} an expansion host. Idempotent — stamping again replaces the rung.
     *
     * @param pane               the pane a figure may expand to fill
     * @param displayName        supplies the rung's display name (see {@link Rung})
     * @param osFullScreenBeyond whether OS full screen lies beyond this rung
     */
    public static void mark(Pane pane, Supplier<String> displayName, boolean osFullScreenBeyond) {
        Objects.requireNonNull(pane, "pane is null");
        pane.getProperties().put(PropertyKeys.EXPANSION_HOST, new Rung(pane, displayName, osFullScreenBeyond));
    }

    /**
     * The ladder of hosts above {@code figure}, nearest first. The figure's own {@linkplain #homePane
     * home} is skipped — expanding into the pane that merely holds you in place is indistinguishable
     * from staying put.
     *
     * @param figure the figure being expanded
     * @return the rungs, nearest first; empty if the figure has no host ancestor (never {@code null})
     */
    public static List<Rung> ladder(Node figure) {
        List<Rung> rungs = new ArrayList<>();
        if (figure == null) {
            return rungs;
        }
        Parent home = homePane(figure);
        for (Parent parent = figure.getParent(); parent != null; parent = parent.getParent()) {
            if (parent != home && parent.getProperties().get(PropertyKeys.EXPANSION_HOST) instanceof Rung rung) {
                rungs.add(rung);
            }
        }
        return rungs;
    }

    /**
     * The pane that merely holds {@code figure} in place — the parent of its expand-affordance host, or
     * of the figure itself when it carries no affordance. An area whose sole content is one figure is
     * that figure's home, not a larger place to expand into.
     *
     * <p>This is a <em>structural</em> test rather than a size comparison. Sizes tempt: "skip a rung no
     * bigger than the figure" reads well until you notice it makes a block's first rung depend on the
     * document stack's padding, and a tall figure's rung depend on where the reader had scrolled.
     */
    private static Parent homePane(Node figure) {
        Parent host = figure.getParent();
        if (host != null && host.getStyleClass().contains(KlExpandable.HOST_STYLE_CLASS)) {
            return host.getParent();
        }
        return host;
    }
}
