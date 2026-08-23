package dev.ikm.komet.kview.controls.skin;

import dev.ikm.komet.kview.controls.ComponentItem;
import dev.ikm.komet.kview.controls.ComponentItemNode;
import dev.ikm.komet.kview.controls.ComponentItemNodeFactory;
import dev.ikm.komet.kview.controls.KLReadOnlyComponentListControl;
import javafx.collections.ListChangeListener;
import javafx.scene.control.Label;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.Region;

public class KLReadOnlyComponentListControlSkin extends KLReadOnlyMultiComponentControlSkin<KLReadOnlyComponentListControl> {

    private static final String EDIT_MENU_ITEM_LABEL = "Edit List";

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
        // HBox shrinks every child when the row is narrower than its content, and the wrapping
        // component alongside always asks for more width than the field has. Hold the ordinal at
        // its natural width so it never ellipsizes away to "…".
        numberLabel.setMinWidth(Region.USE_PREF_SIZE);

        // Component (Icon + Text). A component's description can be arbitrarily long, so it wraps
        // onto as many lines as it needs instead of running past the field's — and the card's — edge.
        ComponentItemNode componentUIItem = ComponentItemNodeFactory.create(componentItem);
        componentUIItem.setWrapText(true);

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
