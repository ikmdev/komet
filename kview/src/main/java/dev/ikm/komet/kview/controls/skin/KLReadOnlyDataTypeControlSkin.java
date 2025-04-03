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
    public KLReadOnlyDataTypeControlSkin(KLReadOnlyDataTypeControl<T> control) {
        super(control);

        mainContainer.getChildren().addAll(textContainer);

        textContainer.getChildren().addAll(promptTextLabel, textLabel);

        textLabel.textProperty().bind(new ObjectBinding<>() {
            {
                super.bind(control.valueProperty());
            }
            @Override
            protected String computeValue() {
                if (control.getValue() == null || (control.getValue() instanceof String string && string.isEmpty())) {
                    return "";
                } else {
                    String valueString = String.valueOf(control.getValue());
                    // We always want the first letter to be upper case
                    String capitalized = valueString.substring(0, 1).toUpperCase() + valueString.substring(1);
                    return capitalized;
                }
            }
        });

        textLabel.setPrefWidth(Double.MAX_VALUE);
        textLabel.setMaxWidth(Region.USE_PREF_SIZE);

        initTexts(control);

        addMenuItemsToContextMenu(control);

        // CSS
        textContainer.getStyleClass().add("text-container");
        textLabel.getStyleClass().add("text");
    }

    private void initTexts(KLReadOnlyDataTypeControl<T> control) {
        updatePromptTextAndTextLabelVisibility(control);
        control.valueProperty().addListener(observable -> updatePromptTextAndTextLabelVisibility(control));
    }

    private void updatePromptTextAndTextLabelVisibility(KLReadOnlyDataTypeControl<T> control) {
        boolean showPromptText = control.getValue() == null;

        NodeUtils.setShowing(promptTextLabel, showPromptText);
        NodeUtils.setShowing(textLabel, !showPromptText);
    }

    private void addMenuItemsToContextMenu(KLReadOnlyDataTypeControl<T> control) {
        Class<T> dataClassType = control.getClassDataType();

        if (dataClassType.equals(String.class)) {
            addMenuItemsForStringType(control);
        } else if (dataClassType.equals(Integer.class)) {
            addMenuItemsForIntegerType(control);
        } else if (dataClassType.equals(Float.class)) {
            addMenuItemsForFloatType(control);
        } else if (dataClassType.equals(Boolean.class)) {
            addMenuItemsForBooleanType(control);
        }

        contextMenu.getItems().addAll(
                new SeparatorMenuItem(),
                createMenuItem("Remove", IconValue.TRASH, this::fireOnRmoveAction)
        );
    }

    private void addMenuItemsForStringType(KLReadOnlyDataTypeControl<T> control) {
        contextMenu.getItems().add(
                createMenuItem("Edit Text", IconValue.PENCIL, this::fireOnEditAction)
        );
    }

    private void addMenuItemsForIntegerType(KLReadOnlyDataTypeControl<T> control) {
        contextMenu.getItems().add(
                createMenuItem("Edit Integer", IconValue.PENCIL, this::fireOnEditAction)
        );
    }

    private void addMenuItemsForFloatType(KLReadOnlyDataTypeControl<T> control) {
        contextMenu.getItems().addAll(
                createMenuItem("Edit Float", IconValue.PENCIL, this::fireOnEditAction),
                createMenuItem("Add Unit of Measure", IconValue.PLUS, this::fireOnAddUnitsOfMeasureAction)
        );
    }

    private void addMenuItemsForBooleanType(KLReadOnlyDataTypeControl<T> control) {
        contextMenu.getItems().add(
                createMenuItem("Edit Selection", IconValue.PENCIL, this::fireOnEditAction)
        );
    }

    private void addMenuItemsForUUIDType(KLReadOnlyDataTypeControl<T> control) {
        contextMenu.getItems().add(
                createMenuItem("Edit Identifier", IconValue.PENCIL, this::fireOnEditAction)
        );
    }

    private void addMenuItemsForInstantType(KLReadOnlyDataTypeControl<T> control) {
        contextMenu.getItems().add(
                createMenuItem("Edit Identifier", IconValue.PENCIL, this::fireOnEditAction)
        );
    }

    private void addMenuItemsForByteArrayType(KLReadOnlyDataTypeControl<T> control) {
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