package dev.ikm.komet.kview.controls.skin;

import dev.ikm.komet.kview.controls.ComponentItem;
import dev.ikm.komet.kview.controls.KLReadOnlyMultiComponentControl;
import javafx.beans.property.BooleanProperty;
import javafx.beans.property.SimpleBooleanProperty;
import javafx.scene.control.ContextMenu;
import javafx.scene.control.Label;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.input.ContextMenuEvent;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.Region;
import javafx.scene.shape.Circle;

/**
 * A Node used to render a Component (icon + text)
 */
public class ComponentItemNode extends Region {
    private final HBox container = new HBox();
    private final ImageView iconImageView = new ImageView();
    private final Label textLabel = new Label();

    private Circle circleClip;

    private ContextMenu contextMenu;

    public ComponentItemNode(String text, Image icon) {
        this(new ComponentItem(text, icon));
    }

    public ComponentItemNode(ComponentItem componentItem) {
        // Image View
        iconImageView.imageProperty().bind(componentItem.iconProperty());
        iconImageView.setFitHeight(16);
        iconImageView.setFitWidth(16);

        // Clip for circled image mode
        circleClip = new Circle(8);
        circleClip.setCenterX(8);
        circleClip.setCenterY(8);

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

    // -- circular
    BooleanProperty circular = new SimpleBooleanProperty(false) {
        @Override
        protected void invalidated() {
            if (get()) {
                iconImageView.setClip(circleClip);
            } else {
                iconImageView.setClip(null);
            }
        }
    };
    public boolean isCircular() { return circular.get(); }
    public BooleanProperty circularProperty() { return circular; }
    public void setCircular(boolean circular) { this.circular.set(circular); }


    void setContextMenu(ContextMenu contextMenu) {
        this.contextMenu = contextMenu;

    }

    private void onContextMenuRequested(ContextMenuEvent contextMenuEvent) {
        pseudoClassStateChanged(KLReadOnlyMultiComponentControl.EDIT_MODE_PSEUDO_CLASS, true);

        contextMenu.setOnHidden(event -> pseudoClassStateChanged(KLReadOnlyMultiComponentControl.EDIT_MODE_PSEUDO_CLASS, false));
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