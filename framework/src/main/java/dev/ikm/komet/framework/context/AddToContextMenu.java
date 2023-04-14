package dev.ikm.komet.framework.context;

import javafx.beans.property.SimpleIntegerProperty;
import javafx.beans.value.ObservableValue;
import javafx.scene.control.ContextMenu;
import javafx.scene.control.Control;
import dev.ikm.komet.framework.view.ViewProperties;
import dev.ikm.tinkar.terms.EntityFacade;

/**
 * See also: MenuSupplierForFocusedEntity TODO:
 */
public interface AddToContextMenu {
    void addToContextMenu(Control controlWithContext, ContextMenu contextMenu, ViewProperties viewProperties,
                          ObservableValue<EntityFacade> conceptFocusProperty,
                          SimpleIntegerProperty selectionIndexProperty,
                          Runnable unlink);
}
