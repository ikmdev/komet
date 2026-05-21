package dev.ikm.komet.kview.controls.skin;

import dev.ikm.komet.kview.controls.ComponentItem;
import dev.ikm.komet.kview.controls.KLReadOnlyMultiComponentControl;
import dev.ikm.tinkar.common.id.PublicId;
import javafx.beans.property.BooleanProperty;
import javafx.beans.property.ObjectProperty;
import javafx.beans.property.SimpleBooleanProperty;
import javafx.beans.property.SimpleObjectProperty;
import javafx.scene.SnapshotParameters;
import javafx.scene.control.ContextMenu;
import javafx.scene.control.Label;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.image.WritableImage;
import javafx.scene.input.ClipboardContent;
import javafx.scene.input.ContextMenuEvent;
import javafx.scene.input.Dragboard;
import javafx.scene.input.TransferMode;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.Region;
import javafx.scene.shape.Circle;
import java.util.Arrays;
import java.util.UUID;
import java.util.function.Supplier;
import java.util.stream.Collectors;

import static dev.ikm.komet.framework.dnd.KometClipboard.COMPONENT_DRAG_FORMAT;

/**
 * A Node used to render a Component (icon + text)
 */
public class ComponentItemNode extends Region {
    private final ImageView iconImageView = new ImageView();
    private final Label textLabel = new Label();

    private Circle circleClip;

    private ContextMenu contextMenu;

    public ComponentItemNode() {
        iconImageView.setFitHeight(16);
        iconImageView.setFitWidth(16);

        // Clip for circled image mode
        circleClip = new Circle(8);
        circleClip.setCenterX(8);
        circleClip.setCenterY(8);

        // Label (Text)
        textLabel.setGraphic(iconImageView);

        textLabel.setMaxWidth(Double.MAX_VALUE);
        HBox.setHgrow(textLabel, Priority.ALWAYS);

        getChildren().add(textLabel);

        setOnContextMenuRequested(this::onContextMenuRequested);

        setupComponentItemUIBinding();

        setupDragAndDrop();

        // CSS
        getStyleClass().add("component-item");
    }

    private void setupDragAndDrop() {
        setOnDragDetected(event -> {
            Dragboard dragboard = startDragAndDrop(TransferMode.COPY);

            PublicId publicId = componentItem.get().getPublicId();

            // Clipboard content
            ClipboardContent content = new ClipboardContent();
            String encoded = Arrays.stream(publicId.asUuidArray())
                    .map(UUID::toString)
                    .collect(Collectors.joining(","));
            content.put(COMPONENT_DRAG_FORMAT, encoded);
            dragboard.setContent(content);

            // Drag Image
            // — temporarily force text color for visibility
            String previousStyle = textLabel.getStyle();
            textLabel.setStyle("-fx-text-fill: #111111;");

            if (dragImageSupplier.get() != null) {
                dragboard.setDragView(dragImageSupplier.get().get());
            } else {
                SnapshotParameters p = new SnapshotParameters();
                WritableImage snapshot = snapshot(p, null);
                dragboard.setDragView(snapshot);
            }

            // - Restore original style
            textLabel.setStyle(previousStyle);

            event.consume();
        });
    }

    public ComponentItemNode(String text, Image icon) {
        this();
        componentItem.get().setText(text);
        componentItem.get().setIcon(icon);
    }

    public ComponentItemNode(ComponentItem componentItem) {
        this();
        setComponentItem(componentItem);
    }

    private void setupComponentItemUIBinding() {
        iconImageView.imageProperty().unbind();
        textLabel.textProperty().unbind();

        iconImageView.imageProperty().bind(componentItem.get().iconProperty());
        textLabel.textProperty().bind(componentItem.get().textProperty());
    }

    void setContextMenu(ContextMenu contextMenu) {
        this.contextMenu = contextMenu;

    }

    private void onContextMenuRequested(ContextMenuEvent contextMenuEvent) {
        pseudoClassStateChanged(KLReadOnlyMultiComponentControl.EDIT_MODE_PSEUDO_CLASS, true);

        contextMenu.setOnHidden(event -> pseudoClassStateChanged(KLReadOnlyMultiComponentControl.EDIT_MODE_PSEUDO_CLASS, false));
        contextMenu.show(this, contextMenuEvent.getScreenX(), contextMenuEvent.getScreenY());
    }

    @Override
    protected double computeMinHeight(double width) {
        // Make the min height be the same as the pref height
        return super.computePrefHeight(width);
    }

    // -- circular
    private final BooleanProperty circular = new SimpleBooleanProperty(false) {
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

    // -- component item
    private final ObjectProperty<ComponentItem> componentItem = new SimpleObjectProperty<>(new ComponentItem()) {
        @Override
        protected void invalidated() {
            setupComponentItemUIBinding();
        }
    };
    public ComponentItem getComponentItem() { return componentItem.get(); }
    public ObjectProperty<ComponentItem> componentItemProperty() { return componentItem; }
    public void setComponentItem(ComponentItem componentItem) { this.componentItem.set(componentItem); }

    // -- drag image supplier
    private final ObjectProperty<Supplier<Image>> dragImageSupplier = new SimpleObjectProperty<>();
    public Supplier<Image> getDragImageSupplier() { return dragImageSupplier.get(); }
    public ObjectProperty<Supplier<Image>> dragImageSupplierProperty() { return dragImageSupplier; }
    public void setDragImageSupplier(Supplier<Image> dragImageSupplier) { this.dragImageSupplier.set(dragImageSupplier); }
}