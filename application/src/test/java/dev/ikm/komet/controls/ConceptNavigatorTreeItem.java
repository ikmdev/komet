package dev.ikm.komet.controls;

import dev.ikm.komet.navigator.graph.Navigator;
import dev.ikm.tinkar.terms.ConceptFacade;
import javafx.beans.property.BooleanProperty;
import javafx.beans.property.ObjectProperty;
import javafx.beans.property.SimpleBooleanProperty;
import javafx.beans.property.SimpleObjectProperty;
import javafx.scene.control.TreeItem;

import java.util.ArrayList;
import java.util.BitSet;
import java.util.List;

public class ConceptNavigatorTreeItem extends TreeItem<ConceptFacade> {

    public static final int MAX_LEVEL = 10;
    private final Navigator navigator;

    public enum STATE {
        LONG_HOVER,
        SELECTED
    }

    public enum PS_STATE {
        LONG_HOVER(0),
        BORDER_LONG_HOVER(1),
        CURVED_LINE_LONG_HOVER(2),
        LINE_I_LONG_HOVER(3),

        BORDER_SELECTED(3 + MAX_LEVEL),
        CURVED_LINE_SELECTED(4 + MAX_LEVEL),
        LINE_I_SELECTED(5 + MAX_LEVEL);

        final int bit;

        PS_STATE(int bit) {
            this.bit = bit;
        }

        public int getBit() {
            return bit;
        }

        public static List<Integer> getStatesBitRange(STATE state) {
            if (state == STATE.LONG_HOVER) {
                return List.of(PS_STATE.LONG_HOVER.getBit(), PS_STATE.BORDER_SELECTED.getBit());
            }
            return List.of(PS_STATE.BORDER_SELECTED.getBit(), PS_STATE.LINE_I_SELECTED.getBit() + MAX_LEVEL + 1);
        }
    }
    private final InvertedTree invertedTree;

    public ConceptNavigatorTreeItem(Navigator navigator, ConceptFacade conceptFacade, int parentNid) {
        this.navigator = navigator;
        invertedTree = new InvertedTree(new InvertedTree.ConceptItem(conceptFacade.nid(), parentNid, conceptFacade.description()));
        setValue(conceptFacade);
    }

    // definedProperty
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

    // multiParentProperty
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

    // viewLineageProperty
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

    public final InvertedTree getInvertedTree() {
        return invertedTree;
    }

    private BitSet bitset;

    public BitSet getBitSet() {
        if (bitset == null) {
            bitset = new BitSet(4 + 2 * MAX_LEVEL);
        }
        return bitset;
    }

    @Override
    public boolean isLeaf() {
        if (getValue() == null || navigator == null) {
            return true;
        }
        int nid = getValue().nid();
        if (nid == Integer.MAX_VALUE) {
            return false;
        }
        return navigator.isLeaf(nid);
    }

    @Override
    public String toString() {
        return "Model[" + getValue() + (isDefined() ? ", defined" : "") + (isMultiParent() ? ", multiParent" : "") + ", b=" + bitset + "]";
    }
}
