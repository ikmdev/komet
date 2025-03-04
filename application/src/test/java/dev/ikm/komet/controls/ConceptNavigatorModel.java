package dev.ikm.komet.controls;

import dev.ikm.tinkar.terms.ConceptFacade;
import javafx.beans.property.BooleanProperty;
import javafx.beans.property.ObjectProperty;
import javafx.beans.property.SimpleBooleanProperty;
import javafx.beans.property.SimpleObjectProperty;

import java.util.BitSet;
import java.util.List;

public class ConceptNavigatorModel {

    public static final int MAX_LEVEL = 10;

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

    public ConceptNavigatorModel(ConceptFacade conceptFacade) {
        setModel(conceptFacade);
    }

    // modelProperty
    private final ObjectProperty<ConceptFacade> modelProperty = new SimpleObjectProperty<>(this, "model");
    public final ObjectProperty<ConceptFacade> modelProperty() {
       return modelProperty;
    }
    public final ConceptFacade getModel() {
       return modelProperty.get();
    }
    public final void setModel(ConceptFacade value) {
        modelProperty.set(value);
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

    // expandedProperty
    private final BooleanProperty expandedProperty = new SimpleBooleanProperty(this, "expanded");
    public final BooleanProperty expandedProperty() {
       return expandedProperty;
    }
    public final boolean isExpanded() {
       return expandedProperty.get();
    }
    public final void setExpanded(boolean value) {
        expandedProperty.set(value);
    }

    private BitSet bitset;

    public BitSet getBitSet() {
        if (bitset == null) {
            bitset = new BitSet(4 + 2 * MAX_LEVEL);
        }
        return bitset;
    }

    @Override
    public String toString() {
        return "Model[" + modelProperty.get() + (isDefined() ? ", defined" : "") + (isMultiParent() ? ", multiParent" : "") + ", b=" + bitset + "]";
    }
}
