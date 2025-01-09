package dev.ikm.komet.kview.controls.skin;

import dev.ikm.komet.kview.NodeUtils;
import dev.ikm.komet.kview.controls.KLReadOnlyStringListControl;
import dev.ikm.komet.kview.controls.KometIcon;
import javafx.collections.ListChangeListener;
import javafx.scene.control.Label;
import javafx.scene.control.SeparatorMenuItem;
import javafx.scene.layout.Region;
import javafx.scene.layout.VBox;
import java.util.HashMap;

public class KLReadOnlyStringListControlSkin extends KLReadOnlyBaseControlSkin<KLReadOnlyStringListControl> {
    private final VBox mainContainer = new VBox();
    private final VBox textsContainer = new VBox();

    private final Label promptTextLabel = new Label();

    private final ListChangeListener<String> textsChanged = this::onTextChanged;
    private final HashMap<String, Label> stringToLabel = new HashMap<>();

    /**
     * @param control The control for which this Skin should attach to.
     */
    public KLReadOnlyStringListControlSkin(KLReadOnlyStringListControl control) {
        super(control);

        mainContainer.getChildren().addAll(titleLabel, textsContainer);
        getChildren().add(mainContainer);

        mainContainer.setFillWidth(true);
        titleLabel.setPrefWidth(Double.MAX_VALUE);
        titleLabel.setMaxWidth(Region.USE_PREF_SIZE);
        textsContainer.setFillWidth(true);
        textsContainer.setPrefWidth(Double.MAX_VALUE);
        textsContainer.setMaxWidth(Region.USE_PREF_SIZE);


        initTexts(control);

        setupContextMenu(control);

        // CSS
        mainContainer.getStyleClass().add("main-container");
        textsContainer.getStyleClass().add("text-container");

        contextMenu.getStyleClass().add("klcontext-menu");
    }

    private void initTexts(KLReadOnlyStringListControl control){
        promptTextLabel.textProperty().bind(control.promptTextProperty());
        textsContainer.getChildren().add(promptTextLabel);

        for (String text : control.getTexts()) {
            createAndAddLabelForText(text);
        }

        updatePromptText(control);

        control.getTexts().addListener(textsChanged);
    }


    private void onTextChanged(ListChangeListener.Change<? extends String> change) {
        while(change.next()) {
            if (change.wasAdded()){
                for (String string : change.getAddedSubList()) {
                    createAndAddLabelForText(string);
                }
            }
            if (change.wasRemoved()) {
                for (String removedString : change.getRemoved()) {
                    textsContainer.getChildren().remove(stringToLabel.get(removedString));
                    stringToLabel.remove(removedString);
                }
            }

            updatePromptText(getSkinnable());
        }
    }

    private Label createAndAddLabelForText(String text) {
        Label label = new Label(text);
        label.getStyleClass().add("text");

        label.setPrefWidth(Double.MAX_VALUE);
        label.setMaxWidth(Double.MAX_VALUE);

        textsContainer.getChildren().add(label);

        stringToLabel.put(text, label);

        return label;
    }

    private void updatePromptText(KLReadOnlyStringListControl control) {
        NodeUtils.setShowing(promptTextLabel, control.getTexts().isEmpty());
    }

    private void setupContextMenu(KLReadOnlyStringListControl control) {
        addMenuItemsToContextMenu(control);

        control.setContextMenu(contextMenu);

        contextMenu.showingProperty().addListener(observable -> {
            control.setEditMode(contextMenu.isShowing());
        });

        control.dataTypeProperty().addListener(observable -> {
            contextMenu.getItems().clear();
            addMenuItemsToContextMenu(control);
        });
    }

    private void addMenuItemsToContextMenu(KLReadOnlyStringListControl control) {
        switch (control.getDataType()) {
            case KLReadOnlyStringListControl.StringListDataType.COMPONENT_ID_SET -> addMenuItemsForComponentSetType(control);
            case KLReadOnlyStringListControl.StringListDataType.COMPONENT_ID_LIST -> addMenuItemsForComponentListType(control);
        }

        contextMenu.getItems().addAll(
                new SeparatorMenuItem(),
                createMenuItem("Remove", KometIcon.IconValue.TRASH, this::fireOnRmoveAction)
        );
    }

    private void addMenuItemsForComponentSetType(KLReadOnlyStringListControl control) {
        contextMenu.getItems().add(
                createMenuItem("Edit Set", KometIcon.IconValue.PENCIL, this::fireOnEditAction)
        );
    }

    private void addMenuItemsForComponentListType(KLReadOnlyStringListControl control) {
        contextMenu.getItems().add(
                createMenuItem("Edit List", KometIcon.IconValue.PENCIL, this::fireOnEditAction)
        );
    }
}
