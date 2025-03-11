package dev.ikm.komet.kview.controls.skin;

import dev.ikm.komet.kview.controls.ComponentItem;
import javafx.css.PseudoClass;
import javafx.scene.control.ContextMenu;
import javafx.scene.control.Label;
import javafx.scene.image.ImageView;
import javafx.scene.input.ContextMenuEvent;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.Region;

/**
 * A Node used to render a Component (icon + text)
 */
class ComponentItemNode extends Region {
    private static final PseudoClass EDIT_MODE_PSEUDO_CLASS = PseudoClass.getPseudoClass("edit-mode");

    private final HBox container = new HBox();
    private final ImageView iconImageView = new ImageView();
    private final Label textLabel = new Label();

    private ContextMenu contextMenu;

    public ComponentItemNode(ComponentItem componentItem) {
        // Image View
        iconImageView.imageProperty().bind(componentItem.iconProperty());
        iconImageView.setFitHeight(20);
        iconImageView.setFitWidth(20);

        // Label (Text)
        textLabel.textProperty().bind(componentItem.textProperty());

        container.getChildren().addAll(iconImageView, textLabel);
        getChildren().add(container);

        textLabel.setMaxWidth(Double.MAX_VALUE);
        HBox.setHgrow(textLabel, Priority.ALWAYS);

        setOnContextMenuRequested(this::onContextMenuRequested);

        // CSS
        getStyleClass().add("component-item");
        container.getStyleClass().add("container");
    }

    void setContextMenu(ContextMenu contextMenu) {
        this.contextMenu = contextMenu;
        this.contextMenu.getStyleClass().add("klcontext-menu");
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