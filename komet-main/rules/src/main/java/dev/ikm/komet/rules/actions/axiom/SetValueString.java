package dev.ikm.komet.rules.actions.axiom;

import dev.ikm.komet.framework.panel.axiom.AxiomSubjectRecord;
import dev.ikm.tinkar.coordinate.edit.EditCoordinate;
import dev.ikm.tinkar.coordinate.view.calculator.ViewCalculator;
import dev.ikm.tinkar.terms.TinkarTerm;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Optional;

/**
 * The SetValueString class extends the SetValue abstract class. It provides an implementation
 * specifically for handling string-based values associated with a feature in the axiom tree.
 * This class facilitates the retrieval and updating of string values for a specified axiom record.
 *
 * It implements methods to fetch the current value of a string property from an {@link AxiomSubjectRecord},
 * and process the result input string into the appropriate form for updating.
 */
public class SetValueString extends SetValue {
    private static final Logger LOG = LoggerFactory.getLogger(SetValue.class);

    public SetValueString(String text, AxiomSubjectRecord axiomSubjectRecord, ViewCalculator viewCalculator, EditCoordinate editCoordinate) {
        super(text, axiomSubjectRecord, viewCalculator, editCoordinate);
    }

    /**
     * Retrieves the current string value associated with the {@link AxiomSubjectRecord}.
     * This method accesses the {@code TinkarTerm.LITERAL_VALUE} property of the axiom vertex
     * within the provided {@code AxiomSubjectRecord} and returns it as a string.
     *
     * @param axiomSubjectRecord The {@code AxiomSubjectRecord} from which to retrieve the current value.
     *                           This record provides access to the axiom vertex containing the desired property.
     * @return The string representation of the current value retrieved from the axiom vertex's
     *         {@code TinkarTerm.LITERAL_VALUE} property. If no value is present, the method may throw an exception.
     */
    @Override
    protected String currentValue(AxiomSubjectRecord axiomSubjectRecord) {
        Optional optionalValue = axiomSubjectRecord.getAxiomVertex().property(TinkarTerm.LITERAL_VALUE);
        return optionalValue.get().toString();
    }

    /**
     * Converts the provided result string into an object representation.
     *
     * @param resultString the input string to be converted.
     * @return the converted object representation of the input string.
     */
    @Override
    protected Object resultValue(String resultString) {
        return resultString;
    }
}