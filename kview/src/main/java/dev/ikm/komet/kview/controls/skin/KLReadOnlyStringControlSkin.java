package dev.ikm.komet.kview.controls.skin;

import dev.ikm.komet.kview.controls.KLReadOnlyStringControl;
import dev.ikm.komet.kview.controls.KLReadOnlyStringControl.DataType;
import dev.ikm.komet.kview.controls.KometIcon;
import dev.ikm.komet.kview.controls.KometIcon.IconValue;
import javafx.beans.InvalidationListener;
import javafx.beans.Observable;
import javafx.css.PseudoClass;
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
        if (control.getDataType() == DataType.STRING) {
            addMenuItemsForStringType(control);
        } else if (control.getDataType() == DataType.INTEGER) {
            addMenuItemsForIntegerType(control);
        }

        SeparatorMenuItem separatorMenuItem = new SeparatorMenuItem();

        MenuItem removeItem = new MenuItem("Remove");
        removeItem.setGraphic(KometIcon.create(IconValue.TRASH));

        contextMenu.getItems().addAll(
                separatorMenuItem,
                removeItem
        );

        removeItem.setOnAction(actionEvent -> {
            if (control.getOnRemoveAction() != null) {
                control.getOnRemoveAction().run();
            }
        });
    }

    private void addMenuItemsForStringType(KLReadOnlyStringControl control) {
        MenuItem editTextItem = new MenuItem("Edit Text");
        editTextItem.setGraphic(KometIcon.create(IconValue.PENCIL));

        contextMenu.getItems().add(editTextItem);

        editTextItem.setOnAction(actionEvent -> {
            if (control.getOnEditAction() != null) {
                control.getOnEditAction().run();
            }
        });
    }

    private void addMenuItemsForIntegerType(KLReadOnlyStringControl control) {
        MenuItem editTextItem = new MenuItem("Edit Integer");
        editTextItem.setGraphic(KometIcon.create(IconValue.PENCIL));

        contextMenu.getItems().add(editTextItem);

        editTextItem.setOnAction(actionEvent -> {
            if (control.getOnEditAction() != null) {
                control.getOnEditAction().run();
            }
        });
    }

    private void onEditModeChanged(Observable observable) {
        pseudoClassStateChanged(EDIT_MODE_PSEUDO_CLASS, getSkinnable().isEditMode());
    }
}