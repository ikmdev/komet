package dev.ikm.komet.rules.actions.axiom;

import dev.ikm.komet.framework.panel.axiom.AxiomSubjectRecord;
import dev.ikm.tinkar.coordinate.edit.EditCoordinate;
import dev.ikm.tinkar.coordinate.view.calculator.ViewCalculator;
import dev.ikm.tinkar.terms.TinkarTerm;

import java.util.Optional;

/**
 * The SetValueBoolean class is a specialization of the {@link SetValue} abstract class, designed
 * to handle the process of updating Boolean feature values within an axiom tree structure.
 * It provides implementations for obtaining the current Boolean value as a string and converting
 * user-provided input into a Boolean result.
 *
 * This class leverages the {@link AxiomSubjectRecord} to retrieve and update the axiom's feature
 * value and utilizes the provided context for editing and calculation.
 */
public class SetValueBoolean extends SetValue {

    public SetValueBoolean(String text, AxiomSubjectRecord axiomSubjectRecord, ViewCalculator viewCalculator, EditCoordinate editCoordinate) {
        super(text, axiomSubjectRecord, viewCalculator, editCoordinate);
    }

    /**
     * Retrieves the current literal value associated with the axiom vertex
     * from the provided {@code axiomSubjectRecord}. If the value is not present,
     * it returns "false".
     *
     * @param axiomSubjectRecord An instance of {@link AxiomSubjectRecord} that
     * contains the context for the axiom vertex and its properties.
     * @return A string representation of the current literal value if present;
     * otherwise, returns "false".
     */
    @Override
    protected String currentValue(AxiomSubjectRecord axiomSubjectRecord) {
        Optional optionalValue = axiomSubjectRecord.getAxiomVertex().property(TinkarTerm.LITERAL_VALUE);
        return optionalValue.isPresent() ? optionalValue.get().toString() : "false";
    }

    /**
     * Converts the provided string representation of a potential Boolean value into a Boolean object.
     * Interprets the input string as {@code true} if it matches "true" (ignoring case), and as {@code false} otherwise.
     *
     * @param resultString the string representation of the value to be converted to a Boolean.
     * @return {@code Boolean.TRUE} if the input string represents "true", otherwise {@code Boolean.FALSE}.
     */
    @Override
    protected Object resultValue(String resultString) {
        return Boolean.parseBoolean(resultString) ? Boolean.TRUE : Boolean.FALSE;
    }

}
