package dev.ikm.komet.kview.controls.skin;

import dev.ikm.komet.kview.controls.ComponentItem;
import dev.ikm.komet.kview.controls.KLReadOnlyMultiComponentControl;
import dev.ikm.komet.kview.controls.KometIcon;
import javafx.beans.binding.StringBinding;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.scene.Node;
import javafx.scene.control.Label;
import javafx.scene.control.MenuItem;
import javafx.scene.control.SkinBase;
import javafx.scene.layout.VBox;

import java.util.HashMap;

public abstract class KLReadOnlyMultiComponentControlSkin<C extends KLReadOnlyMultiComponentControl> extends SkinBase<C> {
    private final VBox mainContainer = new VBox();

    private final Label titleLabel = new Label();
    protected final Label promptTextLabel = new Label();

    protected final VBox componentsAndPlaceholderContainer = new VBox();
    protected final VBox componentsContainer = new VBox();

    protected final HashMap<ComponentItem, Node> componentUIItems = new HashMap<>();

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

    protected MenuItem createMenuItem(String text, KometIcon.IconValue icon, EventHandler<ActionEvent> actionHandler) {
        MenuItem menuItem = new MenuItem(text, KometIcon.create(icon, "icon-klcontext-menu"));
        menuItem.setOnAction(actionHandler);
        return menuItem;
    }

    protected void fireOnEditAction(ActionEvent actionEvent) {
        if (getSkinnable().getOnEditAction() != null) {
            getSkinnable().getOnEditAction().run();
        }
    }

    protected void fireOnRemoveAction(ActionEvent actionEvent, ComponentItem componentItem) {
        if (getSkinnable().getOnRemoveAction() != null) {
            getSkinnable().getOnRemoveAction().accept(componentItem);
        }
    }
}
