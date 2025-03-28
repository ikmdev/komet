package dev.ikm.komet.kview.controls.skin;

import dev.ikm.komet.kview.controls.ComponentItem;
import dev.ikm.komet.kview.controls.KLReadOnlyComponentListControl;
import dev.ikm.komet.kview.controls.KometIcon;
import javafx.collections.ListChangeListener;
import javafx.scene.control.ContextMenu;
import javafx.scene.control.Label;
import javafx.scene.control.SeparatorMenuItem;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;

public class KLReadOnlyComponentListControlSkin extends KLReadOnlyMultiComponentControlSkin<KLReadOnlyComponentListControl> {

    /**
     * @param control The control for which this Skin should attach to.
     */
    public KLReadOnlyComponentListControlSkin(KLReadOnlyComponentListControl control) {
        super(control);

        // sync items observableList
        for (ComponentItem componentItem : control.getItems()) {
            addNewUIItem(componentItem);
        }
        control.getItems().addListener(this::itemsChanged);
    }

    private void itemsChanged(ListChangeListener.Change<? extends ComponentItem> change) {
        while (change.next()) {
            if (change.wasAdded()) {
                for (ComponentItem addedComponentItem : change.getAddedSubList()) {
                    addNewUIItem(addedComponentItem);
                }
            } else if (change.wasRemoved()) {
                for (ComponentItem removedComponentItem : change.getRemoved()) {
                    removeUIItem(removedComponentItem);
                }
            }
        }
    }

    private void addNewUIItem(ComponentItem componentItem) {
        HBox componentRow = new HBox();
        componentRow.getStyleClass().add("component-row");

        Label numberLabel = new Label();
        numberLabel.getStyleClass().add("number-label");

        // Component (Icon + Text)
        ComponentItemNode componentUIItem = new ComponentItemNode(componentItem);

        // Context Menu
        ContextMenu contextMenu = new ContextMenu();
        contextMenu.getItems().addAll(
                createMenuItem("Edit List", KometIcon.IconValue.PENCIL, this::fireOnEditAction),
                new SeparatorMenuItem(),
                createMenuItem("Remove", KometIcon.IconValue.TRASH, actionEvent -> this.fireOnRemoveAction(actionEvent, componentItem))
        );
        componentUIItem.setContextMenu(contextMenu);

        componentRow.getChildren().addAll(
            numberLabel,
            componentUIItem
        );
        HBox.setHgrow(componentUIItem, Priority.ALWAYS);

        componentsContainer.getChildren().add(componentRow);

        numberLabel.setText((componentsContainer.getChildren().indexOf(componentRow) + 1) + ".");

        componentUIItems.put(componentItem, componentRow);

        updatePromptTextOrComponentsVisibility();
    }
}
