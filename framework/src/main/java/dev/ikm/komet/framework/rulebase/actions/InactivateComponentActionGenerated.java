package dev.ikm.komet.framework.rulebase.actions;

import javafx.event.ActionEvent;
import dev.ikm.tinkar.coordinate.edit.EditCoordinate;
import dev.ikm.tinkar.coordinate.edit.EditCoordinateRecord;
import dev.ikm.tinkar.coordinate.view.calculator.ViewCalculator;
import dev.ikm.tinkar.entity.EntityVersion;

public class InactivateComponentActionGenerated extends AbstractActionSuggested {

    final EntityVersion entityVersion;

    public InactivateComponentActionGenerated(EntityVersion entityVersion, ViewCalculator viewCalculator, EditCoordinate editCoordinate) {
        super("Inactivate", viewCalculator, editCoordinate);
        this.entityVersion = entityVersion;
    }

    public final void doAction(ActionEvent actionEvent, EditCoordinateRecord editCoordinate) {
        throw new UnsupportedOperationException();
    }

}
