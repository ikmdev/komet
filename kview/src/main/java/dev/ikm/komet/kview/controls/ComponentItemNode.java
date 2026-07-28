package dev.ikm.komet.kview.controls;

import javafx.beans.property.BooleanProperty;
import javafx.beans.property.ObjectProperty;
import javafx.beans.property.SimpleBooleanProperty;
import javafx.beans.property.SimpleObjectProperty;
import javafx.scene.control.ContextMenu;
import javafx.scene.control.Label;
import javafx.scene.control.Tooltip;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.input.ContextMenuEvent;
import javafx.scene.input.Dragboard;
import javafx.scene.input.TransferMode;
import javafx.geometry.HPos;
import javafx.geometry.VPos;
import javafx.scene.layout.Region;
import javafx.scene.layout.StackPane;
import javafx.scene.shape.Circle;

import java.util.function.Supplier;

/**
 * A Node used to render a Component (icon + text)
 */
public class ComponentItemNode extends Region {
    private final ImageView iconImageView = new ImageView();
    private final Label textLabel = new Label();

    private Circle circleClip;

    private ContextMenu contextMenu;

    private StackPane dragHandleIcon;

    /*=========================================================================*
     *                                                                         *
     * Constructors                                                            *
     *                                                                         *
     *=========================================================================*/

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

        textLabel.tooltipProperty().bind(tooltipProperty());
        textLabel.wrapTextProperty().bind(wrapTextProperty());

        getChildren().add(textLabel);

        setOnContextMenuRequested(this::onContextMenuRequested);

        setupComponentItemUIBinding();

        setupDragAndDrop();

        // CSS
        getStyleClass().add("component-item");
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

        if (componentItem.get() != null) {
            iconImageView.imageProperty().bind(componentItem.get().iconProperty());
            textLabel.textProperty().bind(componentItem.get().textProperty());
        }
    }

    private void onContextMenuRequested(ContextMenuEvent contextMenuEvent) {
        if (contextMenu == null) {
            contextMenu = ComponentItemActions.buildContextMenu(this, componentItem.get());
        }

        pseudoClassStateChanged(KLReadOnlyMultiComponentControl.EDIT_MODE_PSEUDO_CLASS, true);

        contextMenu.setOnHidden(event -> pseudoClassStateChanged(KLReadOnlyMultiComponentControl.EDIT_MODE_PSEUDO_CLASS, false));
        contextMenu.show(this, contextMenuEvent.getScreenX(), contextMenuEvent.getScreenY());
    }

    private void setupDragAndDrop() {
        setOnDragDetected(event -> {
            Dragboard dragboard = startDragAndDrop(TransferMode.COPY);

            dragboard.setContent(ComponentItemActions.buildClipboardContent(componentItem.get()));

            // Drag Image
            String previousStyle = textLabel.getStyle();
            textLabel.setStyle("-fx-text-fill: #111111;");

            if (dragImageSupplier.get() != null) {
                dragboard.setDragView(dragImageSupplier.get().get());
            } else if (getScene() != null) {
                ComponentItemActions.setDragView(dragboard, componentItem.get(), this);
            }

            textLabel.setStyle(previousStyle);

            event.consume();
        });
    }

    @Override
    protected double computeMinHeight(double width) {
        // Make the min height be the same as the pref height
        return super.computePrefHeight(width);
    }

    @Override
    protected void layoutChildren() {
        // Stretch the label to fill the available width, so its hover/edit-mode highlight spans
        // the whole row — but never below its preferred width: when a parent squeezes this node,
        // the label keeps its preferred size and overflows (Region's default behavior) instead of
        // wrapping or truncating.
        double contentWidth = getWidth() - snappedLeftInset() - snappedRightInset();
        double contentHeight = getHeight() - snappedTopInset() - snappedBottomInset();
        double labelWidth = Math.max(textLabel.prefWidth(-1), contentWidth);
        layoutInArea(textLabel, snappedLeftInset(), snappedTopInset(), labelWidth, contentHeight,
                -1, HPos.LEFT, VPos.CENTER);
        if (dragHandleIcon != null) {
            // Overlay the drag handle inside the label's right edge, in the space reserved by the
            // .show-drag-handle label padding.
            double gripWidth = dragHandleIcon.prefWidth(-1);
            double gripX = snappedLeftInset() + labelWidth - gripWidth - DRAG_HANDLE_RIGHT_INSET;
            layoutInArea(dragHandleIcon, gripX, snappedTopInset(), gripWidth, contentHeight,
                    -1, HPos.LEFT, VPos.CENTER);
        }
    }

    /*=========================================================================*
     *                                                                         *
     * Properties                                                              *
     *                                                                         *
     *=========================================================================*/

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
            ComponentItemNode.this.contextMenu = null;
        }
    };
    public ComponentItem getComponentItem() { return componentItem.get(); }
    public ObjectProperty<ComponentItem> componentItemProperty() { return componentItem; }
    public void setComponentItem(ComponentItem componentItem) { this.componentItem.set(componentItem); }

    // -- show drag handle on hover
    private static final double DRAG_HANDLE_WIDTH = 7;
    private static final double DRAG_HANDLE_HEIGHT = 13;
    private static final double DRAG_HANDLE_RIGHT_INSET = 6;

    /**
     * When true, hovering this node reveals a six-dot drag handle at its right edge — the same
     * affordance {@code KLComponentControl} shows for its selected component — signalling that the
     * component can be dragged. Off by default; purely visual, drag-and-drop works either way.
     * The hosting control should reserve space for the handle by styling the label of the
     * {@code .component-item.show-drag-handle} node with extra right padding.
     */
    private final BooleanProperty showDragHandleOnHover = new SimpleBooleanProperty(false) {
        @Override
        protected void invalidated() {
            if (get()) {
                if (dragHandleIcon == null) {
                    dragHandleIcon = new StackPane();
                    dragHandleIcon.getStyleClass().add("drag-handle-icon");
                    dragHandleIcon.setPrefSize(DRAG_HANDLE_WIDTH, DRAG_HANDLE_HEIGHT);
                    dragHandleIcon.setMinSize(DRAG_HANDLE_WIDTH, DRAG_HANDLE_HEIGHT);
                    dragHandleIcon.setMaxSize(DRAG_HANDLE_WIDTH, DRAG_HANDLE_HEIGHT);
                    dragHandleIcon.setManaged(false);
                    dragHandleIcon.setMouseTransparent(true);
                    dragHandleIcon.visibleProperty().bind(hoverProperty().and(showDragHandleOnHover));
                    getChildren().add(dragHandleIcon);
                }
                getStyleClass().add("show-drag-handle");
            } else {
                getStyleClass().remove("show-drag-handle");
            }
        }
    };
    public boolean isShowDragHandleOnHover() { return showDragHandleOnHover.get(); }
    public BooleanProperty showDragHandleOnHoverProperty() { return showDragHandleOnHover; }
    public void setShowDragHandleOnHover(boolean value) { showDragHandleOnHover.set(value); }

    // -- drag image supplier
    private final ObjectProperty<Supplier<Image>> dragImageSupplier = new SimpleObjectProperty<>();
    public Supplier<Image> getDragImageSupplier() { return dragImageSupplier.get(); }
    public ObjectProperty<Supplier<Image>> dragImageSupplierProperty() { return dragImageSupplier; }
    public void setDragImageSupplier(Supplier<Image> dragImageSupplier) { this.dragImageSupplier.set(dragImageSupplier); }

    // -- tooltip
    private final ObjectProperty<Tooltip> tooltip = new SimpleObjectProperty<>();
    public Tooltip getTooltip() { return tooltip.get(); }
    public ObjectProperty<Tooltip> tooltipProperty() { return tooltip; }
    public void setTooltip(Tooltip tooltip) { this.tooltip.set(tooltip); }

    // -- wrap text
    private final BooleanProperty wrapText = new SimpleBooleanProperty(false);
    public boolean isWrapText() { return wrapText.get(); }
    public BooleanProperty wrapTextProperty() { return wrapText; }
    public void setWrapText(boolean wrapText) { this.wrapText.set(wrapText); }
}