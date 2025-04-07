package dev.ikm.komet.rules.actions.axiom;

import dev.ikm.komet.framework.panel.axiom.AxiomSubjectRecord;
import dev.ikm.tinkar.common.service.TinkExecutor;
import dev.ikm.tinkar.coordinate.edit.EditCoordinate;
import dev.ikm.tinkar.coordinate.edit.EditCoordinateRecord;
import dev.ikm.tinkar.coordinate.view.calculator.ViewCalculator;
import dev.ikm.tinkar.entity.graph.adaptor.axiom.LogicalAxiom;
import dev.ikm.tinkar.entity.graph.adaptor.axiom.LogicalExpressionBuilder;
import javafx.event.ActionEvent;
import javafx.scene.control.TextInputDialog;

import java.util.Optional;

/**
 * The SetValue class is an abstract implementation of AbstractAxiomAction that provides a framework
 * for updating specific feature values associated with an axiom in the axiom tree.
 * It includes core methods to handle the user input via a dialog box, validating and converting
 * the input into an appropriate value type, and updating the logical expression with the new value.
 *
 * This class is designed to be extended by concrete implementations that specify how the current value
 * is derived and how the result value from the dialog is processed.
 */
public abstract class SetValue extends AbstractAxiomAction {

    public SetValue(String text, AxiomSubjectRecord axiomSubjectRecord, ViewCalculator viewCalculator, EditCoordinate editCoordinate) {
        super(text, axiomSubjectRecord, viewCalculator, editCoordinate);
    }
    public void doAction() {
        doAction(new ActionEvent());
    }

    /**
     * Retrieves the current value associated with the specified {@link AxiomSubjectRecord}.
     *
     * @param axiomSubjectRecord the {@link AxiomSubjectRecord} from which the current value is to be retrieved.
     * @return the current value as a {@code String} derived from the provided {@link AxiomSubjectRecord}.
     */
    protected abstract String currentValue(AxiomSubjectRecord axiomSubjectRecord);

    /**
     * Converts the provided result string into an appropriate object representation.
     * This method is intended to be implemented by subclasses to handle specific
     * data type conversions or transformations based on the context of the application.
     *
     * @param resultString the string representation of the result that needs to be converted.
     * @return an object that represents the converted value of the input string.
     */
    protected abstract Object resultValue(String resultString);


    /**
     * Executes an action that enables updating a feature value in the logical expression of
     * the provided {@link AxiomSubjectRecord}. Displays a dialog to the user for value input,
     * processes the user input, and updates the logical expression based on the provided
     * edit coordinate.
     *
     * @param t                 the {@link ActionEvent} that triggers this action.
     * @param axiomSubjectRecord the {@link AxiomSubjectRecord} containing the axiom and its context
     *                           to be updated.
     * @param editCoordinate     the {@link EditCoordinateRecord} defining the editing context,
     *                           such as module and path for recording changes.
     */
    @Override
    public final void doAction(ActionEvent t, AxiomSubjectRecord axiomSubjectRecord, EditCoordinateRecord editCoordinate) {
        TextInputDialog dialog = new TextInputDialog(currentValue(axiomSubjectRecord));
        dialog.setTitle("Update Feature Value");
        dialog.setHeaderText("Enter a value: ");
        dialog.setContentText("Context text");

        // Show the dialog and wait for a response
        Optional<String> result = dialog.showAndWait();
        TinkExecutor.threadPool().execute(() -> {
            if (result.isPresent()) {
                LogicalExpressionBuilder leb = new LogicalExpressionBuilder(axiomSubjectRecord.axiomTree());
                switch (leb.get(axiomSubjectRecord.axiomIndex())) {
                    case LogicalAxiom.Atom.TypedAtom.Feature featureAxiom ->
                        leb.updateFeatureLiteralValue(featureAxiom, resultValue(result.get()));

                    default ->
                        throw new IllegalStateException("Unexpected value: " + leb.get(axiomSubjectRecord.axiomIndex()));
                }
                putUpdatedLogicalExpression(editCoordinate, leb.build());
            }
        });
    }

}
