package dev.ikm.komet.framework.rulebase.actions;

import dev.ikm.komet.framework.rulebase.GeneratedActionSuggested;
import dev.ikm.tinkar.coordinate.edit.EditCoordinate;
import dev.ikm.tinkar.coordinate.view.calculator.ViewCalculator;

public abstract non-sealed class AbstractActionSuggested extends AbstractActionGenerated implements GeneratedActionSuggested {

    public AbstractActionSuggested(String text, ViewCalculator viewCalculator, EditCoordinate editCoordinate) {
        super(text, viewCalculator, editCoordinate);
    }
}
