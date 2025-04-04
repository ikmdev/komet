package dev.ikm.komet.kview.controls.skin;

import dev.ikm.komet.kview.controls.ComponentItem;
import dev.ikm.komet.kview.controls.KLReadOnlyComponentSetControl;
import javafx.collections.ListChangeListener;

public class KLReadOnlyComponentSetControlSkin extends KLReadOnlyMultiComponentControlSkin<KLReadOnlyComponentSetControl> {

    private static final String EDIT_MENU_ITEM_LABEL = "Edit Set";

    /**
     * @param control The control for which this Skin should attach to.
     */
    public KLReadOnlyComponentSetControlSkin(KLReadOnlyComponentSetControl control) {
        super(control);

        // sync items observableList
        for (ComponentItem componentItem : control.getItems()) {
            addNewUIItem(componentItem);
        }
        control.getItems().addListener(this::itemsChanged);
    }

    private void itemsChanged(ListChangeListener.Change<? extends ComponentItem> change) {
        while (change.next()) {
            if (change.wasRemoved()) {
                for (ComponentItem removedComponentItem : change.getRemoved()) {
                    removeUIItem(removedComponentItem);
                }
            }
            if (change.wasAdded()) {
                for (ComponentItem addedComponentItem : change.getAddedSubList()) {
                    addNewUIItem(addedComponentItem);
                }
            }
        }
    }

    private void addNewUIItem(ComponentItem componentItem) {
        ComponentItemNode componentItemNode = new ComponentItemNode(componentItem);

        componentItemNode.setContextMenu(createContextMenu(componentItem));

        componentsContainer.getChildren().add(componentItemNode);
        componentUIItems.put(componentItem, componentItemNode);

        updatePromptTextOrComponentsVisibility();
    }

    @Override
    protected String getEditMenuItemLabel() {
        return EDIT_MENU_ITEM_LABEL;
    }
}