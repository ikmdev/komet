package dev.ikm.komet.controls;

import javafx.beans.property.BooleanProperty;
import javafx.beans.property.SimpleBooleanProperty;
import javafx.beans.property.SimpleStringProperty;
import javafx.beans.property.StringProperty;

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

    public ConceptNavigatorModel(String text) {
        setText(text);
    }

    // textProperty
    private final StringProperty textProperty = new SimpleStringProperty(this, "text");
    public final StringProperty textProperty() {
       return textProperty;
    }
    public final String getText() {
       return textProperty.get();
    }
    public final void setText(String value) {
        textProperty.set(value);
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


    private BitSet bitset;

    public BitSet getBitSet() {
        if (bitset == null) {
            bitset = new BitSet(4 + 2 * MAX_LEVEL);
        }
        return bitset;
    }

    @Override
    public String toString() {
        return "Model[" + textProperty.get() + (isDefined() ? ", defined" : "") + ", b=" + bitset + "]";
    }
}
