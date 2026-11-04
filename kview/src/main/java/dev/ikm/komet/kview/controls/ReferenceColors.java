package dev.ikm.komet.kview.controls;

import javafx.css.PseudoClass;
import javafx.scene.Node;

/**
 * The colors telling apart the sections of a window anchored on the same pattern: a section's
 * color is on its reference combobox (and reference strip) and on the mark beside the semantic it
 * chose, which is how the user pairs the two. A color is an index into the palette kview.css
 * defines (-reference-color-0 to -reference-color-19), applied to a node as the pseudo-class
 * :reference-color-N; index 0 is the default blue a lone referencing section keeps.
 */
public final class ReferenceColors {

    /** How many colors the palette holds; indices wrap past it. */
    public static final int COUNT = 20;

    private static final PseudoClass[] PSEUDO_CLASSES = new PseudoClass[COUNT];

    static {
        for (int i = 0; i < COUNT; i++) {
            PSEUDO_CLASSES[i] = PseudoClass.getPseudoClass("reference-color-" + i);
        }
    }

    private ReferenceColors() {
    }

    /** Gives the node the color's pseudo-class, and only that one; a negative index clears them all. */
    public static void apply(Node node, int colorIndex) {
        for (int i = 0; i < COUNT; i++) {
            node.pseudoClassStateChanged(PSEUDO_CLASSES[i], colorIndex >= 0 && i == colorIndex % COUNT);
        }
    }
}
