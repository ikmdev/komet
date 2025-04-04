package dev.ikm.komet.rules.actions.axiom;

import dev.ikm.komet.framework.panel.axiom.AxiomSubjectRecord;
import dev.ikm.tinkar.coordinate.edit.EditCoordinate;
import dev.ikm.tinkar.coordinate.edit.EditCoordinateRecord;
import dev.ikm.tinkar.coordinate.view.calculator.ViewCalculator;
import dev.ikm.tinkar.terms.TinkarTerm;
import javafx.event.ActionEvent;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.math.BigDecimal;
import java.util.Optional;

/**
 * The SetValueDecimal class extends the SetValue abstract class to provide functionality for
 * setting and updating decimal feature values associated with an axiom in the axiom tree.
 * It provides specific implementations for retrieving and processing decimal values.
 *
 * This class is designed to handle user input for decimal values and to convert and update
 * the logical expression using the provided edit coordinate.
 */
public class SetValueDecimal extends SetValue {
    private static final Logger LOG = LoggerFactory.getLogger(SetValue.class);

    public SetValueDecimal(String text, AxiomSubjectRecord axiomSubjectRecord, ViewCalculator viewCalculator, EditCoordinate editCoordinate) {
        super(text, axiomSubjectRecord, viewCalculator, editCoordinate);
    }

    /**
     * Retrieves the current decimal value associated with the specified axiom subject record.
     * If the value is not a valid BigDecimal, returns "0".
     *
     * @param axiomSubjectRecord an instance of AxiomSubjectRecord containing the axiom data from which the value is extracted
     * @return the decimal value as a string if present; otherwise, a string representation of "0"
     */
    @Override
    protected String currentValue(AxiomSubjectRecord axiomSubjectRecord) {
        Optional optionalValue = axiomSubjectRecord.getAxiomVertex().property(TinkarTerm.LITERAL_VALUE);
        return switch (optionalValue.get()) {
            case BigDecimal decimal -> decimal.toString();
            default -> BigDecimal.ZERO.toString();
        };
    }

    /**
     * Converts the provided result string into a BigDecimal instance.
     *
     * @param resultString the string representation of the result to be converted.
     * @return a BigDecimal instance representing the numerical value of the input string.
     */
    @Override
    protected Object resultValue(String resultString) {
        return new BigDecimal(resultString);
    }
}