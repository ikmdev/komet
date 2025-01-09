package dev.ikm.komet.kview.controls.skin;

import dev.ikm.komet.kview.controls.KLReadOnlyStringControl;
import dev.ikm.komet.kview.controls.KLReadOnlyStringControl.DataType;
import dev.ikm.komet.kview.controls.KometIcon;
import dev.ikm.komet.kview.controls.KometIcon.IconValue;
import javafx.beans.InvalidationListener;
import javafx.beans.Observable;
import javafx.css.PseudoClass;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.scene.control.ContextMenu;
import javafx.scene.control.Label;
import javafx.scene.control.MenuItem;
import javafx.scene.control.SeparatorMenuItem;
import javafx.scene.control.SkinBase;
import javafx.scene.layout.Region;
import javafx.scene.layout.VBox;

public class KLReadOnlyStringControlSkin extends SkinBase<KLReadOnlyStringControl> {
    private static final PseudoClass EDIT_MODE_PSEUDO_CLASS = PseudoClass.getPseudoClass("edit-mode");

    private final VBox mainContainer = new VBox();
    private final Label textLabel = new Label();
    private final Label titleLabel = new Label();

    private final ContextMenu contextMenu = new ContextMenu();

    private InvalidationListener editModeChanged = this::onEditModeChanged;

    /**
     * @param control The control for which this Skin should attach to.
     */
    public KLReadOnlyStringControlSkin(KLReadOnlyStringControl control) {
        super(control);

        mainContainer.getChildren().addAll(titleLabel, textLabel);
        getChildren().add(mainContainer);

        titleLabel.textProperty().bind(control.titleProperty());
        textLabel.textProperty().bind(control.textProperty());
        control.editModeProperty().addListener(editModeChanged);

        mainContainer.setFillWidth(true);
        titleLabel.setPrefWidth(Double.MAX_VALUE);
        titleLabel.setMaxWidth(Region.USE_PREF_SIZE);
        textLabel.setPrefWidth(Double.MAX_VALUE);
        textLabel.setMaxWidth(Region.USE_PREF_SIZE);

        setupContextMenu(control);

        // CSS
        mainContainer.getStyleClass().add("main-container");
        titleLabel.getStyleClass().add("title");
        textLabel.getStyleClass().add("text");

        contextMenu.getStyleClass().add("klcontext-menu");
    }

    private void setupContextMenu(KLReadOnlyStringControl control) {
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

    private void addMenuItemsToContextMenu(KLReadOnlyStringControl control) {
        switch (control.getDataType()) {
            case DataType.STRING -> addMenuItemsForStringType(control);
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

    private void fireOnEditAction(ActionEvent actionEvent) {
        if (getSkinnable().getOnEditAction() != null) {
            getSkinnable().getOnEditAction().run();
        }
    }

    private void fireOnAddUnitsOfMeasureAction(ActionEvent actionEvent) {
        if (getSkinnable().getOnAddUnitsOfMeasureAction() != null) {
            getSkinnable().getOnAddUnitsOfMeasureAction().run();
        }
    }

    private void fireOnRmoveAction(ActionEvent actionEvent) {
        if (getSkinnable().getOnRemoveAction() != null) {
            getSkinnable().getOnRemoveAction().run();
        }
    }

    private MenuItem createMenuItem(String text, KometIcon.IconValue icon, EventHandler<ActionEvent> actionHandler) {
        MenuItem menuItem = new MenuItem(text, KometIcon.create(icon, "icon-klcontext-menu"));
        menuItem.setOnAction(actionHandler);
        return menuItem;
    }

    private void onEditModeChanged(Observable observable) {
        pseudoClassStateChanged(EDIT_MODE_PSEUDO_CLASS, getSkinnable().isEditMode());
    }
}