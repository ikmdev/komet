package dev.ikm.komet.framework.rulebase.actions;

import dev.ikm.komet.framework.rulebase.GeneratedActionImmediate;
import dev.ikm.tinkar.coordinate.edit.EditCoordinate;
import dev.ikm.tinkar.coordinate.view.calculator.ViewCalculator;

public abstract non-sealed class AbstractActionImmediate extends AbstractActionGenerated implements GeneratedActionImmediate {

    public AbstractActionImmediate(String text, ViewCalculator viewCalculator, EditCoordinate editCoordinate) {
        super(text, viewCalculator, editCoordinate);
    }
}
