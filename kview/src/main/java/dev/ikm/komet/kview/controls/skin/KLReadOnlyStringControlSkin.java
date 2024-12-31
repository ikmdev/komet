package dev.ikm.komet.kview.controls.skin;

import dev.ikm.komet.kview.NodeUtils;
import dev.ikm.komet.kview.controls.KLReadOnlyStringControl;
import dev.ikm.komet.kview.controls.KLReadOnlyStringControl.StringDataType;
import dev.ikm.komet.kview.controls.KometIcon.IconValue;
import javafx.event.ActionEvent;
import javafx.scene.control.Label;
import javafx.scene.control.SeparatorMenuItem;
import javafx.scene.layout.Region;
import javafx.scene.layout.VBox;

public class KLReadOnlyStringControlSkin extends KLReadOnlyBaseControlSkin<KLReadOnlyStringControl> {
    private final VBox mainContainer = new VBox();
    private final VBox textContainer = new VBox();
    private final Label textLabel = new Label();
    private final Label promptTextLabel = new Label();

    /**
     * @param control The control for which this Skin should attach to.
     */
    public KLReadOnlyStringControlSkin(KLReadOnlyStringControl control) {
        super(control);

        mainContainer.getChildren().addAll(titleLabel, textContainer);
        textContainer.getChildren().addAll(promptTextLabel, textLabel);
        getChildren().add(mainContainer);

        textLabel.textProperty().bind(control.textProperty());

        mainContainer.setFillWidth(true);
        titleLabel.setPrefWidth(Double.MAX_VALUE);
        titleLabel.setMaxWidth(Region.USE_PREF_SIZE);
        textLabel.setPrefWidth(Double.MAX_VALUE);
        textLabel.setMaxWidth(Region.USE_PREF_SIZE);

        initTexts(control);

        setupContextMenu(control);

        // CSS
        mainContainer.getStyleClass().add("main-container");
        textContainer.getStyleClass().add("text-container");
        textLabel.getStyleClass().add("text");
    }

    private void initTexts(KLReadOnlyStringControl control) {
        promptTextLabel.textProperty().bind(control.promptTextProperty());

        updatePromptTextAndTextLabel(control);
        control.textProperty().addListener(observable -> updatePromptTextAndTextLabel(control));
    }

    private void updatePromptTextAndTextLabel(KLReadOnlyStringControl control) {
        boolean showPromptText = control.getText() == null || control.getText().isEmpty();

        NodeUtils.setShowing(promptTextLabel, showPromptText);
        NodeUtils.setShowing(textLabel, !showPromptText);

    }

    private void setupContextMenu(KLReadOnlyStringControl control) {
        addMenuItemsToContextMenu(control);

        control.dataTypeProperty().addListener(observable -> {
            contextMenu.getItems().clear();
            addMenuItemsToContextMenu(control);
        });
    }

    private void addMenuItemsToContextMenu(KLReadOnlyStringControl control) {
        switch (control.getDataType()) {
            case STRING -> addMenuItemsForStringType(control);
            case INTEGER -> addMenuItemsForIntegerType(control);
            case FLOAT -> addMenuItemsForFloatType(control);
            case BOOLEAN -> addMenuItemsForBooleanType(control);
            case UUID -> addMenuItemsForUUIDType(control);
            case INSTANT -> addMenuItemsForInstantType(control);
            case BYTE_ARRAY -> addMenuItemsForByteArrayType(control);
        }

        contextMenu.getItems().addAll(
                new SeparatorMenuItem(),
                createMenuItem("Remove", IconValue.TRASH, this::fireOnRmoveAction)
        );
    }

    private void addMenuItemsForStringType(KLReadOnlyStringControl control) {
        contextMenu.getItems().add(
                createMenuItem("Edit Text", IconValue.PENCIL, this::fireOnEditAction)
        );
    }

    private void addMenuItemsForIntegerType(KLReadOnlyStringControl control) {
        contextMenu.getItems().add(
                createMenuItem("Edit Integer", IconValue.PENCIL, this::fireOnEditAction)
        );
    }

    private void addMenuItemsForFloatType(KLReadOnlyStringControl control) {
        contextMenu.getItems().addAll(
                createMenuItem("Edit Float", IconValue.PENCIL, this::fireOnEditAction),
                createMenuItem("Add Unit of Measure", IconValue.PLUS, this::fireOnAddUnitsOfMeasureAction)
        );
    }

    private void addMenuItemsForBooleanType(KLReadOnlyStringControl control) {
        contextMenu.getItems().add(
                createMenuItem("Edit Selection", IconValue.PENCIL, this::fireOnEditAction)
        );
    }

    private void addMenuItemsForUUIDType(KLReadOnlyStringControl control) {
        contextMenu.getItems().add(
                createMenuItem("Edit Identifier", IconValue.PENCIL, this::fireOnEditAction)
        );
    }

    private void addMenuItemsForInstantType(KLReadOnlyStringControl control) {
        contextMenu.getItems().add(
                createMenuItem("Edit Identifier", IconValue.PENCIL, this::fireOnEditAction)
        );
    }

    private void addMenuItemsForByteArrayType(KLReadOnlyStringControl control) {
        contextMenu.getItems().add(
                createMenuItem("Edit Selection", IconValue.PENCIL, this::fireOnEditAction)
        );
    }

    private void fireOnAddUnitsOfMeasureAction(ActionEvent actionEvent) {
        if (getSkinnable().getOnAddUnitsOfMeasureAction() != null) {
            getSkinnable().getOnAddUnitsOfMeasureAction().run();
        }
    }
}