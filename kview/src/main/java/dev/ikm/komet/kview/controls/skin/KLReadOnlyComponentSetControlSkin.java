package dev.ikm.komet.kview.controls.skin;

import dev.ikm.komet.kview.controls.ComponentItem;
import dev.ikm.komet.kview.controls.KLReadOnlyComponentSetControl;
import dev.ikm.komet.kview.controls.KometIcon;
import javafx.beans.binding.StringBinding;
import javafx.collections.SetChangeListener;
import javafx.css.PseudoClass;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.scene.Node;
import javafx.scene.control.ContextMenu;
import javafx.scene.control.Label;
import javafx.scene.control.MenuItem;
import javafx.scene.control.SeparatorMenuItem;
import javafx.scene.control.SkinBase;
import javafx.scene.image.ImageView;
import javafx.scene.input.ContextMenuEvent;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.Region;
import javafx.scene.layout.VBox;

import java.util.HashMap;

public class KLReadOnlyComponentSetControlSkin extends SkinBase<KLReadOnlyComponentSetControl> {
    private final VBox mainContainer = new VBox();

    private final Label titleLabel = new Label();
    private final Label promptTextLabel = new Label();

    private final VBox componentsContainer = new VBox();

    private final HashMap<ComponentItem, Node> componentUIItems = new HashMap<>();

    /**
     * @param control The control for which this Skin should attach to.
     */
    public KLReadOnlyComponentSetControlSkin(KLReadOnlyComponentSetControl control) {
        super(control);

        mainContainer.getChildren().addAll(
                titleLabel,
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

        // sync items observableSet
        for (ComponentItem componentItem : control.getItems()) {
            addNewUIItem(componentItem);
        }
        control.getItems().addListener(this::itemsChanged);

        // CSS
        mainContainer.getStyleClass().add("main-container");
        componentsContainer.getStyleClass().add("components-container");
        titleLabel.getStyleClass().add("title");
        promptTextLabel.getStyleClass().add("prompt-text");
    }

    private void itemsChanged(SetChangeListener.Change<? extends ComponentItem> change) {
        if (change.wasAdded()) {
            addNewUIItem(change.getElementAdded());
        } else if (change.wasRemoved()) {
            removeUIItem(change.getElementRemoved());
        }
    }

    private void addNewUIItem(ComponentItem componentItem) {
        Node componentUIItem = new ComponentItemNode(this, componentItem);;
        componentsContainer.getChildren().add(componentUIItem);
        componentUIItems.put(componentItem, componentUIItem);
    }

    private void removeUIItem(ComponentItem componentItem) {
        Node componentUIItem = componentUIItems.get(componentItem);
        componentsContainer.getChildren().remove(componentUIItem);
        componentUIItems.remove(componentItem);
    }

    private static MenuItem createMenuItem(String text, KometIcon.IconValue icon, EventHandler<ActionEvent> actionHandler) {
        MenuItem menuItem = new MenuItem(text, KometIcon.create(icon, "icon-klcontext-menu"));
        menuItem.setOnAction(actionHandler);
        return menuItem;
    }

    protected void fireOnEditAction(ActionEvent actionEvent) {
        if (getSkinnable().getOnEditAction() != null) {
            getSkinnable().getOnEditAction().run();
        }
    }

    protected void fireOnRmoveAction(ActionEvent actionEvent, ComponentItem componentItem) {
        if (getSkinnable().getOnRemoveAction() != null) {
            getSkinnable().getOnRemoveAction().accept(componentItem);
        }
    }

    /*******************************************************************************
     *                                                                             *
     * Supporting Classes                                                          *
     *                                                                             *
     ******************************************************************************/

    public static class ComponentItemNode extends Region {
        private static final PseudoClass EDIT_MODE_PSEUDO_CLASS = PseudoClass.getPseudoClass("edit-mode");

        private final HBox container = new HBox();
        private final ImageView iconImageView = new ImageView();
        private final Label textLabel = new Label();

        private ContextMenu contextMenu = new ContextMenu();

        public ComponentItemNode(KLReadOnlyComponentSetControlSkin componentSetControlSkin, ComponentItem componentItem) {
            // Image View
            iconImageView.imageProperty().bind(componentItem.iconProperty());
            iconImageView.setFitHeight(20);
            iconImageView.setFitWidth(20);

            // Label (Image View Icon + Text)
            textLabel.textProperty().bind(componentItem.textProperty());

            container.getChildren().addAll(iconImageView, textLabel);
            getChildren().add(container);

            textLabel.setMaxWidth(Double.MAX_VALUE);
            HBox.setHgrow(textLabel, Priority.ALWAYS);


            contextMenu.getStyleClass().add("klcontext-menu");
            contextMenu.getItems().addAll(
                    createMenuItem("Edit Set", KometIcon.IconValue.PENCIL, componentSetControlSkin::fireOnEditAction),
                    new SeparatorMenuItem(),
                    createMenuItem("Remove", KometIcon.IconValue.TRASH, actionEvent -> componentSetControlSkin.fireOnRmoveAction(actionEvent, componentItem))
            );

            setOnContextMenuRequested(this::onContextMenuRequested);

            // CSS
            getStyleClass().add("component-item");
            container.getStyleClass().add("container");
        }

        private void onContextMenuRequested(ContextMenuEvent contextMenuEvent) {
            pseudoClassStateChanged(EDIT_MODE_PSEUDO_CLASS, true);

            contextMenu.setOnHidden(event -> pseudoClassStateChanged(EDIT_MODE_PSEUDO_CLASS, false));
            contextMenu.show(this, contextMenuEvent.getScreenX(), contextMenuEvent.getScreenY());
        }

        @Override
        protected void layoutChildren() {
            double leftInsets = snappedLeftInset();
            double rightInsets = snappedRightInset();
            double topInsets = snappedTopInset();
            double bottomInsets = snappedBottomInset();
            double width = getWidth();
            double height = getHeight();

            container.resizeRelocate(leftInsets, topInsets,
                    width - leftInsets - rightInsets, height - topInsets - bottomInsets);
        }
    }
}