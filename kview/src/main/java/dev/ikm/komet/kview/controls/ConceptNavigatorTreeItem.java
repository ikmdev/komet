package dev.ikm.komet.kview.controls;

import dev.ikm.komet.navigator.graph.Navigator;
import dev.ikm.tinkar.common.service.TinkExecutor;
import dev.ikm.tinkar.terms.ConceptFacade;
import javafx.beans.property.BooleanProperty;
import javafx.beans.property.ObjectProperty;
import javafx.beans.property.SimpleBooleanProperty;
import javafx.beans.property.SimpleObjectProperty;
import javafx.scene.control.TreeItem;

import java.util.BitSet;
import java.util.List;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Future;

import static dev.ikm.komet.kview.controls.KLConceptNavigatorControl.MAX_LEVEL;

/**
 * <p>{@link ConceptNavigatorTreeItem} is the model for a single node supplying a hierarchy of values to the
 * {@link KLConceptNavigatorControl}.
 * </p>
 * <p>The model uses {@link ConceptFacade} as the type of the value property.
 *</p>
 */
public class ConceptNavigatorTreeItem extends TreeItem<ConceptFacade> {

    private final Navigator navigator;

    /**
     * <p>Tags are used in conjunction with the timeline. By default, no tag is set,
     * but it can be "added" or "retired" from a certain timeline point.
     * </p>
     * @see #tagProperty
     * @see KLConceptNavigatorControl#showTagsProperty()
     */
    public enum TAG {
        NONE,
        ADDED,
        RETIRED
    }

    /**
     * <p>A ConceptNavigatorTreeItem by default has no state, but it can be hovered, if the user
     * moves the mouse over it, and after some time, this is set as long_hovered state. If the
     * user press the mouse and selects the item, it gets the selected state.
     * </p>
     * <p>Note: The STATE enum is only used internally.
     * </p>
     * @see KLConceptNavigatorControl#activationProperty()
     * @see PS_STATE
     */
    public enum STATE {
        LONG_HOVER,
        SELECTED
    }

    /**
     * <p>This enum is used to set the required pseudoClasses that are used to visually display the
     * connecting lines between different ConceptNavigatorTreeItem in the long_hovered state or in
     * the selected state.
     * </p>
     * <p>Note: The PS_STATE enum is only used internally. Each enum holds a fixed bit value.
     * If the {@link #bitset} is set or not for that position, the related pseudoClass is set or not.
     * </p>
     * <p>
     *
     * </p>
     * @see STATE
     * @see ConceptNavigatorUtils#STYLE
     * @see KLConceptNavigatorControl#MAX_LEVEL
     * @see #bitset
     * @see ConceptTile
     */
    public enum PS_STATE {
        /**
         * Bit 0. Associated with the 'cn-long-hover' pseudoClass, refers to the actual long-hovered conceptItem
         * setting a light-blue background, and 2px blue border for the {@link ConceptTile}.
         */
        LONG_HOVER(0),
        /**
         * Bit 1. Associated with the 'cn-border-long-hover' pseudoClass, refers to the ancestors of the long-hovered
         * conceptItem, setting a white background, and 2px blue border for the {@link ConceptTile}.
         */
        BORDER_LONG_HOVER(1),
        /**
         * Bit 2. Associated with the 'cn-curved-line-long-hover' pseudoClass, refers to the 2px blue curved line that
         * connects to the {@link ConceptTile} of the actual long-hovered conceptItem and all of its ancestors.
         */
        CURVED_LINE_LONG_HOVER(2),
        /**
         * Bits 3 to 3 + MAX_LEVEL - 1. Associated with the 'cn-line-long-hover-i' pseudoClass, refers to the
         * 2px blue vertical line that connects the actual long-hovered conceptItem with all its ancestors,
         * at the indentation level i, from 0 to MAX_LEVEL.
         */
        LINE_I_LONG_HOVER(3),

        /**
         * Bit 3 + MAX_LEVEL. Associated with the 'cn-border-selected' pseudoClass, refers to the ancestors of the
         * actual selected conceptItem, setting a white background, and 1px gray border for the {@link ConceptTile}.
         */
        BORDER_SELECTED(3 + MAX_LEVEL),
        /**
         * Bit 4 + MAX_LEVEL. Associated with the 'cn-curved-line-selected' pseudoClass, refers to the 1px gray curved
         * line that connects to the {@link ConceptTile} of the actual selected conceptItem and all of its ancestors.
         */
        CURVED_LINE_SELECTED(4 + MAX_LEVEL),
        /**
         * Bits 5 + MAX_LEVEL to 4 + 2 * MAX_LEVEL. Associated with the 'cn-line-selected-i' pseudoClass, refers
         * to the 1px gray vertical line that connects the actual selected conceptItem with all its ancestors,
         * at the indentation level i, from 0 to MAX_LEVEL.
         */
        LINE_I_SELECTED(5 + MAX_LEVEL);

        final int bit;

        PS_STATE(int bit) {
            this.bit = bit;
        }

        /**
         * Gets the bit value associated to a given enum
         * @return an integer value between 0 and 5 + 2 * MAX_LEVEL -1
         */
        public int getBit() {
            return bit;
        }

        /**
         * Clears the bits of a bitSet between the first and last bits that relate to a given {@link STATE}.
         * @param bitSet The {@link BitSet}
         * @param state The {@link STATE} that can be either {@link STATE#LONG_HOVER} or {@link STATE#SELECTED}
         */
        public static void clearBitsRange(BitSet bitSet, STATE state) {
            if (state == STATE.LONG_HOVER) {
               bitSet.clear(PS_STATE.LONG_HOVER.getBit(), PS_STATE.BORDER_SELECTED.getBit());
            }
            if (state == STATE.SELECTED) {
                bitSet.clear(PS_STATE.BORDER_SELECTED.getBit(), PS_STATE.LINE_I_SELECTED.getBit() + MAX_LEVEL + 1);
            }
        }
    }

    private final InvertedTree invertedTree;

    private Boolean isLeaf;
    private final Future<Boolean> future;

    /**
     * <p>Creates a ConceptNavigatorTreeItem instance.
     * </p>
     * <p>By default, an empty {@link InvertedTree} instance is created for this concept.
     * </p>
     * @param navigator The {@link Navigator} that holds the dataset
     * @param conceptFacade The {@link ConceptFacade} that defines the concept for this concept TreeItem
     * @param parentNid the nid of the parent of this concept
     */
    public ConceptNavigatorTreeItem(Navigator navigator, ConceptFacade conceptFacade, int parentNid) {
        this.navigator = navigator;
        invertedTree = new InvertedTree(new InvertedTree.ConceptItem(conceptFacade.nid(), parentNid, conceptFacade.description()));
        setValue(conceptFacade);
        future = TinkExecutor.threadPool().submit(() -> navigator.isLeaf(conceptFacade.nid()));
    }

    /**
     * <p>Boolean property that toggles the defined (when set to true) or primitive (false, by default) feature of the
     * concept associated to this concept TreeItem.
     * </p>
     * <p>For the nid of this concept, this value is set based on the following:
     * <pre><code>
     * getNavigator().getViewCalculator().hasSufficientSet(Entity.getFast(nid))
     * </code></pre>
     * </p>
     */
    private final BooleanProperty definedProperty = new SimpleBooleanProperty(this, "defined");
    public final BooleanProperty definedProperty() {
       return definedProperty;
    }
    public final boolean isDefined() {
       return definedProperty.get();
    }
    public final void setDefined(boolean value) {
        definedProperty.set(value);
    }

    /**
     * <p>Boolean property that toggles the multi-parent (when set to true) or single-parent (false, by default) feature
     * of the concept associated to this concept TreeItem.
     * </p>
     * <p>For the nid of this concept, this value is set based on tte following:
     * <pre><code>
     * getNavigator().getParentNids(nid).length &gt; 1
     * </code></pre>
     * </p>
     */
    private final BooleanProperty multiParentProperty = new SimpleBooleanProperty(this, "multiParent");
    public final BooleanProperty multiParentProperty() {
       return multiParentProperty;
    }
    public final boolean isMultiParent() {
       return multiParentProperty.get();
    }
    public final void setMultiParent(boolean value) {
        multiParentProperty.set(value);
    }

    /**
     * <p>Boolean property that toggles the visibility of the {@link LineageBox} (visible, when set to true, or hidden
     * when set to false, by default) associated to this concept TreeItem.
     * </p>
     */
    private final BooleanProperty viewLineageProperty = new SimpleBooleanProperty(this, "viewLineage");
    public final BooleanProperty viewLineageProperty() {
       return viewLineageProperty;
    }
    public final boolean isViewLineage() {
       return viewLineageProperty.get();
    }
    public final void setViewLineage(boolean value) {
        viewLineageProperty.set(value);
    }

    /**
     * Property that sets the {@link TAG} for the concept associated to this concept TreeItem. By default the tag is
     * set to {@link TAG#NONE}.
     */
    private final ObjectProperty<TAG> tagProperty = new SimpleObjectProperty<>(this, "tag", TAG.NONE);
    public final ObjectProperty<TAG> tagProperty() {
       return tagProperty;
    }
    public final TAG getTag() {
       return tagProperty.get();
    }
    public final void setTag(TAG value) {
        tagProperty.set(value);
    }

    /**
     * Property that toggles the highlighted state of this concept TreeItem.
     */
    private final BooleanProperty highlightedProperty = new SimpleBooleanProperty(this, "highlighted");
    public final BooleanProperty highlightedProperty() {
       return highlightedProperty;
    }
    public final boolean isHighlighted() {
       return highlightedProperty.get();
    }
    public final void setHighlighted(boolean value) {
        highlightedProperty.set(value);
    }

    /**
     * Property that adds a list of related concepts to this concept TreeItem
     * TODO
     */
    private final ObjectProperty<List<ConceptFacade>> relatedConceptsProperty = new SimpleObjectProperty<>(this, "relatedConcepts");
    public final ObjectProperty<List<ConceptFacade>> relatedConceptsProperty() {
       return relatedConceptsProperty;
    }
    public final List<ConceptFacade> getRelatedConcepts() {
       return relatedConceptsProperty.get();
    }
    public final void setRelatedConcepts(List<ConceptFacade> value) {
        relatedConceptsProperty.set(value);
    }

    /**
     * <p>Gets the {@link InvertedTree} instance of this concept TreeItem, which might by empty.
     * </p>
     * @return an {@link InvertedTree}
     * @see ConceptNavigatorUtils#buildInvertedTree(int, Navigator)
     */
    public final InvertedTree getInvertedTree() {
        return invertedTree;
    }

    private BitSet bitset;

    /**
     * <p>Returns the bitSet that holds the information for the 4 + 2 * MAX_LEVEL pseudoClasses
     * that are used to highlight the connecting lines when there is a long-hover or selected {@link STATE}.
     * </p>
     * @return a bitSet
     */
    public BitSet getBitSet() {
        if (bitset == null) {
            bitset = new BitSet(4 + 2 * MAX_LEVEL);
        }
        return bitset;
    }

    /**
     * <p>A ConceptNavigatorTreeItem is a leaf if it has no children. Given that the {@link KLConceptNavigatorControl}
     * doesn't have the full dataset in memory, to find out if this item has children or not, this method
     * overrides the default implementation in order to use {@link Navigator#isLeaf(int)} instead.
     * </p>
     * @return true if this ConceptNavigatorTreeItem has no children
     */
    @Override
    public boolean isLeaf() {
        if (getValue() == null || navigator == null) {
            return true;
        }
        int nid = getValue().nid();
        if (nid == Integer.MAX_VALUE) {
            return false;
        }
        if (isLeaf == null) {
            try {
                isLeaf = future.get();
            } catch (InterruptedException | ExecutionException e) {
                throw new RuntimeException(e);
            }
        }
        return isLeaf;
    }

    /**
     * Returns a string representation of this {@code ConceptNavigatorTreeItem} object.
     * @return a string representation of this {@code ConceptNavigatorTreeItem} object.
     */
    @Override
    public String toString() {
        return "ConceptNavigatorTreeItem[" + getValue() +
                (isDefined() ? ", defined" : "") +
                (isMultiParent() ? ", multiParent" : "") +
                (isViewLineage() ? ", viewLineage" : "") +
                (getTag() != TAG.NONE ? ", tag: " + getTag() : "") +
                ", b=" + bitset + "]";
    }
}
