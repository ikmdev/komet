package dev.ikm.komet.kview.controls.skin;

import dev.ikm.komet.kview.NodeUtils;
import dev.ikm.komet.kview.controls.KLReadOnlyDataTypeControl;
import dev.ikm.komet.kview.controls.KometIcon.IconValue;
import javafx.beans.binding.ObjectBinding;
import javafx.event.ActionEvent;
import javafx.scene.control.Label;
import javafx.scene.control.SeparatorMenuItem;
import javafx.scene.layout.Region;
import javafx.scene.layout.VBox;

public class KLReadOnlyDataTypeControlSkin<T> extends KLReadOnlyBaseControlSkin<KLReadOnlyDataTypeControl<T>> {

    private final VBox textContainer = new VBox();
    private final Label textLabel = new Label();

    /**
     * @param control The control for which this Skin should attach to.
     */
    public KLReadOnlyDataTypeControlSkin(KLReadOnlyDataTypeControl control) {
        super(control);

        mainContainer.getChildren().addAll(textContainer);

        textContainer.getChildren().addAll(promptTextLabel, textLabel);

        textLabel.textProperty().bind(new ObjectBinding<>() {
            {
                super.bind(control.valueProperty());
            }
            @Override
            protected String computeValue() {
                if (control.getValue() == null) {
                    return "";
                } else {
                    return String.valueOf(control.getValue());
                }
            }
        });

        textLabel.setPrefWidth(Double.MAX_VALUE);
        textLabel.setMaxWidth(Region.USE_PREF_SIZE);

        initTexts(control);

        setupContextMenu(control);

        // CSS
        textContainer.getStyleClass().add("text-container");
        textLabel.getStyleClass().add("text");
    }

    private void initTexts(KLReadOnlyDataTypeControl control) {
        updatePromptTextAndTextLabelVisibility(control);
        control.valueProperty().addListener(observable -> updatePromptTextAndTextLabelVisibility(control));
    }

    private void updatePromptTextAndTextLabelVisibility(KLReadOnlyDataTypeControl control) {
        boolean showPromptText = control.getValue() == null;

        NodeUtils.setShowing(promptTextLabel, showPromptText);
        NodeUtils.setShowing(textLabel, !showPromptText);
    }

    private void setupContextMenu(KLReadOnlyDataTypeControl control) {
        addMenuItemsToContextMenu(control);

        control.typeProperty().addListener(observable -> {
            contextMenu.getItems().clear();
            addMenuItemsToContextMenu(control);
        });
    }

    private void addMenuItemsToContextMenu(KLReadOnlyDataTypeControl control) {
        switch (control.getType()) {
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

    private void addMenuItemsForStringType(KLReadOnlyDataTypeControl control) {
        contextMenu.getItems().add(
                createMenuItem("Edit Text", IconValue.PENCIL, this::fireOnEditAction)
        );
    }

    private void addMenuItemsForIntegerType(KLReadOnlyDataTypeControl control) {
        contextMenu.getItems().add(
                createMenuItem("Edit Integer", IconValue.PENCIL, this::fireOnEditAction)
        );
    }

    private void addMenuItemsForFloatType(KLReadOnlyDataTypeControl control) {
        contextMenu.getItems().addAll(
                createMenuItem("Edit Float", IconValue.PENCIL, this::fireOnEditAction),
                createMenuItem("Add Unit of Measure", IconValue.PLUS, this::fireOnAddUnitsOfMeasureAction)
        );
    }

    private void addMenuItemsForBooleanType(KLReadOnlyDataTypeControl control) {
        contextMenu.getItems().add(
                createMenuItem("Edit Selection", IconValue.PENCIL, this::fireOnEditAction)
        );
    }

    private void addMenuItemsForUUIDType(KLReadOnlyDataTypeControl control) {
        contextMenu.getItems().add(
                createMenuItem("Edit Identifier", IconValue.PENCIL, this::fireOnEditAction)
        );
    }

    private void addMenuItemsForInstantType(KLReadOnlyDataTypeControl control) {
        contextMenu.getItems().add(
                createMenuItem("Edit Identifier", IconValue.PENCIL, this::fireOnEditAction)
        );
    }

    private void addMenuItemsForByteArrayType(KLReadOnlyDataTypeControl control) {
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