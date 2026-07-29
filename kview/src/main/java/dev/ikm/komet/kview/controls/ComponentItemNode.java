package dev.ikm.komet.kview.controls;

import dev.ikm.komet.framework.StyleClasses;
import dev.ikm.komet.framework.controls.KonceptBadge;
import dev.ikm.komet.framework.controls.KonceptSigils;
import dev.ikm.komet.framework.controls.KonceptStatus;
import dev.ikm.komet.framework.graphics.KonceptGlyphFonts;
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
import javafx.geometry.Pos;
import javafx.geometry.VPos;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Region;
import javafx.scene.layout.StackPane;
import javafx.scene.shape.Circle;
import javafx.scene.text.Text;
import network.ike.docs.konceptcore.KonceptKind;

import java.util.function.Supplier;

/**
 * A Node used to render a Component (icon + text), leading with the same component-detail glyphs a
 * {@link KonceptBadge} shows (as in the pattern navigator): the component-kind sigil (D/S/P letter,
 * or the STAMP pentagon) set via {@link #setKonceptKind(KonceptKind)}, and the taxonomic status
 * cluster ({@code ≡} defined, {@code ⊑} primitive, {@code ⊤} root, plus the {@code ⋎} multi-parent
 * fork) set via {@link #setKonceptStatus(KonceptStatus)}. Each glyph carries its accessible name on
 * a tooltip, the non-colour channel that explains it.
 *
 * <p>This control is UI-only: it renders whatever kind and status it is given and never asks the
 * store. {@link ComponentItemNodeFactory} is the piece that knows the rest of the app — it wires
 * nodes that resolve both from the rendered component's {@code PublicId}. A node built bare, off
 * the factory, shows no glyphs (the pre-glyph rendering).
 */
public class ComponentItemNode extends Region {

    /**
     * Letter-sigil size as a fraction of the label font. Deliberately larger than the badge's
     * 10px komet.css sigil: this node's glyphs read at the label's own size, not a chip's.
     */
    private static final double SIGIL_TO_NAME = 1.0;

    /** Pentagon edge as a fraction of the identicon edge, matching the letter sigil. */
    private static final double PENTAGON_TO_ICON = 0.75;

    /** Status-cluster size as a fraction of the label font, trimmed 18% from the 11/8 bump. */
    private static final double STATUS_TO_NAME = 1;

    private final ImageView iconImageView = new ImageView();
    private final HBox sigilBox = new HBox();
    private final HBox statusBox = new HBox();
    /** The status cluster's explanatory tooltip, installed once; only its text changes. */
    private final Tooltip statusTooltip = new Tooltip();
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

        // Label (Text): the graphic leads with the component-detail glyphs, then the identicon —
        // the same [one mark][identicon][name] anatomy as a KonceptBadge.
        sigilBox.setAlignment(Pos.CENTER);
        statusBox.setAlignment(Pos.CENTER);

        Tooltip.install(statusBox, statusTooltip);

        HBox graphicBox = new HBox(4, sigilBox, statusBox, iconImageView);

        graphicBox.setAlignment(Pos.CENTER_LEFT);
        textLabel.setGraphic(graphicBox);

        // The defaults (bare CONCEPT, NONE) never fire the properties' invalidated hooks, so
        // normalize the empty boxes here — unmanaged, opening no gap before the identicon.
        updateSigil();
        updateStatus();

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

        // A new component means the previous component's glyphs no longer apply. This hook runs
        // before external listeners, so a factory-wired node re-resolves right after; a bare
        // node honestly shows no glyphs rather than stale ones.
        setKonceptKind(KonceptKind.CONCEPT);
        setKonceptStatus(KonceptStatus.NONE);
    }

    /**
     * Renders the component-kind sigil via the shared {@link KonceptSigils} factory, which also
     * installs the kind's accessible name as the sigil's tooltip.
     */
    private void updateSigil() {
        sigilBox.getChildren().clear();

        double sigilSize = textLabel.getFont().getSize() * SIGIL_TO_NAME;
        KonceptSigils.create(getKonceptKind(), iconImageView.getFitWidth() * PENTAGON_TO_ICON, sigilSize)
                .ifPresent(sigil -> {
                    // Inline, because komet.css's .koncept-sigil rule fixes 10px (the chip
                    // size) wherever it reaches, and only an inline style outranks it.
                    sigil.setStyle("-fx-font-size: " + sigilSize + "px; -fx-font-weight: bold;");
                    sigilBox.getChildren().add(sigil);
                });

        // Unmanage the empty box so the graphic HBox spacing does not open a gap before the identicon.
        boolean hasSigil = !sigilBox.getChildren().isEmpty();
        sigilBox.setManaged(hasSigil);
        sigilBox.setVisible(hasSigil);
    }

    /**
     * Renders the taxonomic status cluster — the same single-sourced vocabulary the
     * {@link KonceptBadge} renders, with the accessible reading on the cluster's tooltip
     * (ike-issues#861).
     */
    private void updateStatus() {
        statusBox.getChildren().clear();

        KonceptStatus status = getKonceptStatus();
        if (status != null && status.hasGlyph()) {
            String glyphFamily = KonceptGlyphFonts.family();
            double statusSize = textLabel.getFont().getSize() * STATUS_TO_NAME;
            // Inline, for the same reason as the sigil: komet.css's .koncept-status rule
            // fixes the chip's 10px wherever it reaches. The family rides along so the
            // cluster keeps the bundled glyph face (ike-issues#953). Never request bold here:
            // the face ships only a Regular weight, and asking for bold sends font resolution
            // outside the family — the OS fallback that draws the ⋎ fork upside-down.
            String statusStyle = (glyphFamily != null
                    ? "-fx-font-family: '" + glyphFamily + "'; "
                    : "")
                    + "-fx-font-size: " + statusSize + "px;";
            Text glyph = new Text(status.glyph());
            glyph.getStyleClass().addAll(StyleClasses.KONCEPT_STATUS.toString(),
                    status.styleClass().toString());
            glyph.setStyle(statusStyle);

            statusBox.getChildren().add(glyph);

            if (status.isMultiParent()) {
                Text fork = new Text(KonceptStatus.MULTI_PARENT_GLYPH);
                fork.getStyleClass().addAll(StyleClasses.KONCEPT_STATUS.toString(),
                        StyleClasses.KONCEPT_MULTIPARENT.toString());
                fork.setStyle(statusStyle);
                statusBox.getChildren().add(fork);
            }

            statusTooltip.setText(status.accessibleText());
        }

        // Unmanage the empty box so the graphic HBox spacing does not open a gap before the identicon.
        boolean hasStatus = !statusBox.getChildren().isEmpty();
        statusBox.setManaged(hasStatus);
        statusBox.setVisible(hasStatus);
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

    // -- koncept kind
    /**
     * The component kind whose sigil leads the identicon; a bare {@link KonceptKind#CONCEPT} (the
     * default) shows none. Presentation-only: the host (normally the
     * {@link ComponentItemNodeFactory} wiring) resolves the kind and sets it here.
     */
    private final ObjectProperty<KonceptKind> konceptKind = new SimpleObjectProperty<>(KonceptKind.CONCEPT) {
        @Override
        protected void invalidated() {
            updateSigil();
        }
    };
    public KonceptKind getKonceptKind() { return konceptKind.get(); }
    public ObjectProperty<KonceptKind> konceptKindProperty() { return konceptKind; }
    public void setKonceptKind(KonceptKind kind) { this.konceptKind.set(kind); }

    // -- koncept status
    /**
     * The taxonomic status whose cluster leads the identicon; {@link KonceptStatus#NONE} (the
     * default) shows none. Presentation-only, like {@link #konceptKindProperty()}.
     */
    private final ObjectProperty<KonceptStatus> konceptStatus = new SimpleObjectProperty<>(KonceptStatus.NONE) {
        @Override
        protected void invalidated() {
            updateStatus();
        }
    };
    public KonceptStatus getKonceptStatus() { return konceptStatus.get(); }
    public ObjectProperty<KonceptStatus> konceptStatusProperty() { return konceptStatus; }
    public void setKonceptStatus(KonceptStatus status) { this.konceptStatus.set(status); }

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