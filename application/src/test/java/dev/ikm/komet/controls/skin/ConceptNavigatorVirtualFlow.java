package dev.ikm.komet.controls.skin;

import dev.ikm.komet.controls.ConceptNavigatorModel;
import dev.ikm.komet.controls.KLConceptNavigatorTreeCell;
import javafx.scene.control.TreeCell;
import javafx.scene.control.skin.VirtualFlow;

import java.util.function.Consumer;

public class ConceptNavigatorVirtualFlow extends VirtualFlow<TreeCell<ConceptNavigatorModel>> {

    public void applyToAllVisibleCellsBefore(KLConceptNavigatorTreeCell currentCell, Consumer<KLConceptNavigatorTreeCell> consumerCell) {
        KLConceptNavigatorTreeCell firstVisibleCell = (KLConceptNavigatorTreeCell) getFirstVisibleCell();
        for (int index = firstVisibleCell.getIndex(); index < currentCell.getIndex(); index++) {
            consumerCell.accept((KLConceptNavigatorTreeCell) getCell(index));
        }
    }
}
