package dev.ikm.komet.kview.controls.skin;

import dev.ikm.tinkar.terms.ConceptFacade;
import javafx.scene.control.TreeCell;
import javafx.scene.control.skin.VirtualFlow;

/**
 * ConceptNavigatorVirtualFlow is the {@link VirtualFlow} for the
 * {@link dev.ikm.komet.kview.controls.KLConceptNavigatorControl}
 */
public class ConceptNavigatorVirtualFlow extends VirtualFlow<TreeCell<ConceptFacade>> {

    private Runnable onLayout;

    /**
     * <p>Sets the action that runs after each layout pass of this virtual flow, that is, whenever the
     * visible cells or their positions change: after scrolling, resizing, or expanding and collapsing items.
     * @param onLayout the action to run, or null
     */
    public void setOnLayout(Runnable onLayout) {
        this.onLayout = onLayout;
    }

    /** {@inheritDoc} */
    @Override
    protected void layoutChildren() {
        super.layoutChildren();
        if (onLayout != null) {
            onLayout.run();
        }
    }
}
