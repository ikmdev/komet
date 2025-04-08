package dev.ikm.komet.rules.actions.axiom;

import dev.ikm.komet.framework.panel.axiom.AxiomSubjectRecord;
import dev.ikm.tinkar.coordinate.edit.EditCoordinate;
import dev.ikm.tinkar.coordinate.view.calculator.ViewCalculator;
import dev.ikm.tinkar.terms.TinkarTerm;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Optional;

/**
 * The {@code SetValueInteger} class provides an implementation of the {@link SetValue}
 * abstract class for handling integer-based feature values within axioms in the axiom tree.
 * This class is specifically tailored to retrieve, process, and update integer values
 * associated with a given axiom and utilizes mechanisms established in the parent class.
 *
 * It includes functionality to:
 * - Retrieve the current integer value associated with an axiom.
 * - Convert user-provided input, represented as a string, into an integer object.
 * - Update the feature value within the logical expression of the axiom using the parsed integer.
 */
public class SetValueInteger extends SetValue {
    private static final Logger LOG = LoggerFactory.getLogger(SetValue.class);

    public SetValueInteger(String text, AxiomSubjectRecord axiomSubjectRecord, ViewCalculator viewCalculator, EditCoordinate editCoordinate) {
        super(text, axiomSubjectRecord, viewCalculator, editCoordinate);
    }

    /**
     * Retrieves the current value associated with the {@code axiomSubjectRecord}'s axiom vertex.
     * If the property corresponds to an integer value, it returns its string representation.
     * If no integer value is found, it returns "NaN".
     *
     * @param axiomSubjectRecord the record that contains information about the axiom and its related vertex.
     * @return the current value as a string if present; otherwise, "NaN".
     */
    @Override
    protected String currentValue(AxiomSubjectRecord axiomSubjectRecord) {
        Optional optionalValue = axiomSubjectRecord.getAxiomVertex().property(TinkarTerm.LITERAL_VALUE);
        return switch (optionalValue.get()) {
            case Integer integer -> integer.toString();
            default -> "0";
        };
    }

    /**
     * Converts the given string representation of a value into an object, specifically an Integer.
     * This method parses the input string and returns the corresponding integer value.
     *
     * @param resultString the string representation of the value to be converted.
     * @return an Integer object representing the parsed value of the input string.
     * @throws NumberFormatException if the string does not contain a parsable integer.
     */
    @Override
    protected Object resultValue(String resultString) {
        return Integer.parseInt(resultString);
    }
}