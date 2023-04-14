package dev.ikm.komet.framework.dnd;

import javafx.scene.input.DragEvent;
import javafx.scene.input.Dragboard;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.Region;
import dev.ikm.tinkar.terms.EntityFacade;

import java.util.function.Consumer;
import java.util.function.Predicate;
import java.util.function.Supplier;

public class DragAndDropHelper {
    final DragHelper dragHelper;
    final DropHelper dropHelper;

    public DragAndDropHelper(Region region,
                             Supplier<EntityFacade> objectToDragSupplier,
                             Consumer<Dragboard> draggedObjectAcceptor,
                             Predicate<MouseEvent> acceptDrag,
                             Predicate<DragEvent> acceptDrop) {
        this.dragHelper = new DragHelper(region, objectToDragSupplier, acceptDrag);
        this.dropHelper = new DropHelper(region, draggedObjectAcceptor, acceptDrop, this.dragHelper::isDragging);
    }

}
