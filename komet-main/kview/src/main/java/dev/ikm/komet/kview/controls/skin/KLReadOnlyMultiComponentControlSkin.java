package dev.ikm.komet.kview.controls.skin;

import static dev.ikm.komet.kview.events.EventTopics.JOURNAL_TOPIC;
import dev.ikm.tinkar.events.EvtBusFactory;
import dev.ikm.komet.kview.controls.ComponentItem;
import dev.ikm.komet.kview.controls.KLReadOnlyMultiComponentControl;
import dev.ikm.komet.kview.controls.KometIcon;
import dev.ikm.komet.kview.events.MakeConceptWindowEvent;
import dev.ikm.tinkar.entity.ConceptEntity;
import dev.ikm.tinkar.entity.EntityService;
import dev.ikm.tinkar.terms.EntityFacade;
import javafx.beans.binding.StringBinding;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.scene.Node;
import javafx.scene.control.ContextMenu;
import javafx.scene.control.Label;
import javafx.scene.control.MenuItem;
import javafx.scene.control.SeparatorMenuItem;
import javafx.scene.control.SkinBase;
import javafx.scene.layout.VBox;

import java.util.HashMap;

import static dev.ikm.komet.kview.controls.skin.KLReadOnlyBaseControlSkin.EDIT_MODE_PSEUDO_CLASS;

public abstract class KLReadOnlyMultiComponentControlSkin<C extends KLReadOnlyMultiComponentControl> extends SkinBase<C> {

    private static final String POPULATE_CONCEPT_MENU_ITEM_LABEL = "Populate Concept";

    private final VBox mainContainer = new VBox();

    private final Label titleLabel = new Label();
    protected final Label promptTextLabel = new Label();

    protected final VBox componentsAndPlaceholderContainer = new VBox();
    protected final VBox componentsContainer = new VBox();

    protected final HashMap<ComponentItem, Node> componentUIItems = new HashMap<>();

    private boolean wasEditActionFired = false;

    /**
     * @param control The control for which this Skin should attach to.
     */
    protected KLReadOnlyMultiComponentControlSkin(C control) {
        super(control);

        mainContainer.getChildren().addAll(
                titleLabel,
                componentsAndPlaceholderContainer
        );

        componentsAndPlaceholderContainer.getChildren().addAll(
                promptTextLabel,
                componentsContainer
        );

        getChildren().add(mainContainer);

        // title
        titleLabel.textProperty().bind(new StringBinding() {
            {
                super.bind(control.titleProperty());
            }
            @Override
            protected String computeValue() {
                String title = control.getTitle();
                if (title != null) {
                    return control.getTitle().toUpperCase();
                } else {
                    return "";
                }
            }
        });

        promptTextLabel.textProperty().bind(control.promptTextProperty());
        promptTextLabel.setMaxWidth(Double.MAX_VALUE);
        ContextMenu promptTextContextMenu = createContextMenu(null);
        promptTextLabel.setContextMenu(promptTextContextMenu);
        promptTextContextMenu.setOnShown(value -> onContextMenuForPromptShown(promptTextContextMenu));

        control.editModeProperty().subscribe(this::onEditModeChanged);

        // CSS
        mainContainer.getStyleClass().add("main-container");
        componentsContainer.getStyleClass().add("components-container");
        titleLabel.getStyleClass().add("title");
        promptTextLabel.getStyleClass().add("prompt-text");
    }

    protected void removeUIItem(ComponentItem componentItem) {
        Node componentRow = componentUIItems.get(componentItem);
        componentsContainer.getChildren().remove(componentRow);
        componentUIItems.remove(componentItem);

        updatePromptTextOrComponentsVisibility();
    }

    protected void updatePromptTextOrComponentsVisibility() {
        boolean promptTextVisible = componentsContainer.getChildren().isEmpty();

        promptTextLabel.setVisible(promptTextVisible);
        promptTextLabel.setManaged(promptTextVisible);
        componentsContainer.setVisible(!promptTextVisible);
        componentsContainer.setManaged(!promptTextVisible);
    }

    protected final ContextMenu createContextMenu(ComponentItem componentItem) {
        KLReadOnlyMultiComponentControl control = getSkinnable();

        ContextMenu contextMenu = new ContextMenu();

        contextMenu.getStyleClass().add("klcontext-menu");

        // Populate Concept
        MenuItem populateMenuItem = createMenuItem(POPULATE_CONCEPT_MENU_ITEM_LABEL, KometIcon.IconValue.POPULATE,
                actionEvent -> this.fireOnPopulateAction(actionEvent, componentItem.getNid()));

        // Populate and Edit
        contextMenu.getItems().addAll(
                populateMenuItem,
                createMenuItem(getEditMenuItemLabel(), KometIcon.IconValue.PENCIL, this::fireOnEditAction)
        );

        // Remove
        MenuItem removeMenuItem = createMenuItem("Remove", KometIcon.IconValue.TRASH,
                actionEvent -> this.fireOnRemoveAction(actionEvent, componentItem));

        contextMenu.getItems().addAll(
                new SeparatorMenuItem(),
                removeMenuItem
        );
        if (componentItem == null) {
            removeMenuItem.setDisable(true);
        }

        contextMenu.showingProperty().addListener(observable -> {
            if (!contextMenu.isShowing() && !wasEditActionFired) {
                control.setEditMode(false);
            } else if (!contextMenu.isShowing() && wasEditActionFired){
                control.setEditMode(true);
            }
        });

        return contextMenu;
    }

    protected abstract String getEditMenuItemLabel();

    private void onContextMenuForPromptShown(ContextMenu contextMenu) {
        KLReadOnlyMultiComponentControl control = getSkinnable();
        control.pseudoClassStateChanged(KLReadOnlyMultiComponentControl.EDIT_MODE_PSEUDO_CLASS, true);

        contextMenu.setOnHidden(event -> control.pseudoClassStateChanged(KLReadOnlyMultiComponentControl.EDIT_MODE_PSEUDO_CLASS, false));
    }

    protected MenuItem createMenuItem(String text, KometIcon.IconValue icon, EventHandler<ActionEvent> actionHandler) {
        MenuItem menuItem = new MenuItem(text, KometIcon.create(icon, "icon-klcontext-menu"));
        menuItem.setOnAction(actionHandler);
        return menuItem;
    }

    protected void fireOnEditAction(ActionEvent actionEvent) {
        if (getSkinnable().getOnEditAction() != null) {
            wasEditActionFired = true;
            getSkinnable().getOnEditAction().run();
        }
    }

    protected void fireOnRemoveAction(ActionEvent actionEvent, ComponentItem componentItem) {
        if (getSkinnable().getOnRemoveAction() != null) {
            getSkinnable().getOnRemoveAction().accept(componentItem);
        }
    }

    protected void fireOnPopulateAction(ActionEvent actionEvent, Integer nid) {
        if (getSkinnable().getOnPopulateAction() != null) {
            getSkinnable().getOnPopulateAction().accept(nid);
        }
    }
  
    private void onEditModeChanged() {
        pseudoClassStateChanged(EDIT_MODE_PSEUDO_CLASS, getSkinnable().isEditMode());
        wasEditActionFired = false;
    }
}
