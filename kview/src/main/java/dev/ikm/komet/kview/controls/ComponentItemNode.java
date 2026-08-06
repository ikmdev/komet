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
import javafx.beans.property.SimpleStringProperty;
import javafx.beans.property.StringProperty;
import javafx.event.Event;
import javafx.scene.Node;
import javafx.scene.control.ContextMenu;
import javafx.scene.control.Label;
import javafx.scene.control.MenuItem;
import javafx.scene.control.SeparatorMenuItem;
import javafx.scene.control.Tooltip;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.input.ContextMenuEvent;
import javafx.scene.input.Dragboard;
import javafx.scene.input.TransferMode;
import javafx.geometry.HPos;
import javafx.geometry.Orientation;
import javafx.geometry.Pos;
import javafx.geometry.VPos;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Region;
import javafx.scene.layout.StackPane;
import javafx.scene.shape.Circle;
import javafx.scene.text.Text;
import network.ike.docs.konceptcore.KonceptKind;

import java.util.List;
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
    /** The label's graphic: [sigil][status][identicon], leading the name. */
    private final HBox graphicBox = new HBox(4, sigilBox, statusBox, iconImageView);
    /** Single-line sample for measuring the label font's descent (wrap-mode graphic alignment). */
    private final Text lineMeasurer = new Text("Ag");

    /**
     * Optical fine-tune of the wrap-mode graphic bottom relative to the first line's bottom, in
     * pixels (negative = up). The geometric line bottom includes the font's descent, which is
     * empty space on a line without descenders, so flush-with-the-letters sits a touch higher.
     */
    private static final double WRAP_GRAPHIC_BOTTOM_NUDGE = -3;

    private Circle circleClip;

    private ContextMenu contextMenu;

    private StackPane dragHandleIcon;

    private StackPane discardButton;

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
        // With a decorator the menu is rebuilt on each request, so the appended items can reflect
        // current state (e.g. an item disabled depending on the surrounding model).
        if (contextMenu == null || getContextMenuItemsSupplier() != null) {
            contextMenu = ComponentItemActions.buildContextMenu(this, componentItem.get());
            if (getContextMenuItemsSupplier() != null) {
                List<MenuItem> extraItems = getContextMenuItemsSupplier().get();
                if (!extraItems.isEmpty()) {
                    contextMenu.getItems().add(new SeparatorMenuItem());
                    contextMenu.getItems().addAll(extraItems);
                }
            }
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
        return computePrefHeight(width);
    }

    @Override
    protected double computePrefHeight(double width) {
        if (isWrapText() && width >= 0) {
            return snappedTopInset() + snappedBottomInset()
                    + textLabel.prefHeight(width - snappedLeftInset() - snappedRightInset());
        }
        return super.computePrefHeight(width);
    }

    @Override
    public Orientation getContentBias() {
        // In wrap mode height depends on the width the parent grants, so it must size
        // width-first for the wrapped lines to be given room.
        return isWrapText() ? Orientation.HORIZONTAL : super.getContentBias();
    }

    @Override
    protected double computePrefWidth(double height) {
        if (getDropHintText() != null) {
            return dropHintLockedWidth;
        }
        return super.computePrefWidth(height);
    }

    @Override
    protected void layoutChildren() {
        // Stretch the label to fill the available width, so its hover/edit-mode highlight spans
        // the whole row — but never below its preferred width: when a parent squeezes this node,
        // the label keeps its preferred size and overflows (Region's default behavior) instead of
        // wrapping or truncating. Except in wrap mode, where the label is held to the available
        // width precisely so it wraps within it.
        double contentWidth = getWidth() - snappedLeftInset() - snappedRightInset();
        double contentHeight = getHeight() - snappedTopInset() - snappedBottomInset();
        // While a drop hint shows, the label is capped at the locked width so the hint ellipsizes
        // instead of growing the chip.
        double labelWidth = getDropHintText() != null || isWrapText() ? contentWidth
                : Math.max(textLabel.prefWidth(-1), contentWidth);
        layoutInArea(textLabel, snappedLeftInset(), snappedTopInset(), labelWidth, contentHeight,
                -1, HPos.LEFT, VPos.CENTER);
        // The label skin centers its graphic against the whole text block, so on a wrapped
        // multi-line title the glyphs would float between the lines. Shift them so the graphic's
        // bottom sits on the bottom of the first text line. Measured off the skin's actual layout —
        // the label is forced to lay out now (already sized above; the later pass is a no-op) so
        // the skin-placed text and graphic positions for this pass can be read back.
        if (isWrapText() && getDropHintText() == null) {
            textLabel.layout();
            for (Node child : textLabel.getChildrenUnmodifiable()) {
                if (child instanceof Text textNode) {
                    // Top edge in label coords (minY folds away the textOrigin convention), plus
                    // the first line's ascent, is the baseline; the font's descent below it ends
                    // the first line.
                    double baselineY = textNode.getLayoutY() + textNode.getLayoutBounds().getMinY()
                            + textNode.getBaselineOffset();
                    lineMeasurer.setFont(textNode.getFont());
                    double descent = lineMeasurer.getLayoutBounds().getHeight()
                            - lineMeasurer.getBaselineOffset();
                    double graphicBottom = graphicBox.getLayoutY() + graphicBox.getLayoutBounds().getHeight();
                    graphicBox.setTranslateY(baselineY + descent + WRAP_GRAPHIC_BOTTOM_NUDGE - graphicBottom);
                    break;
                }
            }
        } else {
            graphicBox.setTranslateY(0);
        }
        // Overlay the affordances inside the label's right edge, in the space reserved by the
        // .show-drag-handle / .show-discard-button label padding: [ … text … grip ✕ ].
        double nextX = snappedLeftInset() + labelWidth;
        if (discardButton != null) {
            double discardWidth = discardButton.prefWidth(-1);
            nextX -= DISCARD_RIGHT_INSET + discardWidth;
            layoutInArea(discardButton, nextX, snappedTopInset(), discardWidth, contentHeight,
                    -1, HPos.LEFT, VPos.CENTER);
        }
        if (dragHandleIcon != null) {
            double gripWidth = dragHandleIcon.prefWidth(-1);
            nextX -= DRAG_HANDLE_RIGHT_INSET + gripWidth;
            layoutInArea(dragHandleIcon, nextX, snappedTopInset(), gripWidth, contentHeight,
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

    // -- show discard button on hover
    private static final double DISCARD_RIGHT_INSET = 4;

    /**
     * When true, hovering this node reveals a ✕ button at its right edge — the same affordance
     * {@code KLComponentControl} shows for discarding its selected component. Pressing it runs the
     * {@link #onDiscardActionProperty() discard action}. Off by default. The hosting control
     * should reserve space for the button by styling the label of the
     * {@code .component-item.show-discard-button} node with extra right padding.
     */
    private final BooleanProperty showDiscardButtonOnHover = new SimpleBooleanProperty(false) {
        @Override
        protected void invalidated() {
            if (get()) {
                if (discardButton == null) {
                    Region discardIcon = new Region();
                    discardIcon.getStyleClass().add("component-item-discard-icon");
                    discardButton = new StackPane(discardIcon);
                    // Sized by the hosting control's stylesheet (pref/min/max width and height
                    // on .component-item-discard-button).
                    discardButton.getStyleClass().add("component-item-discard-button");
                    discardButton.setManaged(false);
                    discardButton.visibleProperty().bind(hoverProperty().and(showDiscardButtonOnHover));
                    // The button acts on its own: neither the click nor a drag gesture started on
                    // it may bubble into the node's handlers.
                    discardButton.setOnDragDetected(Event::consume);
                    discardButton.setOnMouseClicked(e -> {
                        if (getOnDiscardAction() != null) {
                            getOnDiscardAction().run();
                        }
                        e.consume();
                    });
                    getChildren().add(discardButton);
                }
                getStyleClass().add("show-discard-button");
            } else {
                getStyleClass().remove("show-discard-button");
            }
        }
    };
    public boolean isShowDiscardButtonOnHover() { return showDiscardButtonOnHover.get(); }
    public BooleanProperty showDiscardButtonOnHoverProperty() { return showDiscardButtonOnHover; }
    public void setShowDiscardButtonOnHover(boolean value) { showDiscardButtonOnHover.set(value); }

    // -- on discard action
    /**
     * The action the hover-revealed ✕ button runs — e.g. the axiom tree swaps the chip for an
     * inline component search slot.
     */
    private final ObjectProperty<Runnable> onDiscardAction = new SimpleObjectProperty<>();
    public Runnable getOnDiscardAction() { return onDiscardAction.get(); }
    public ObjectProperty<Runnable> onDiscardActionProperty() { return onDiscardAction; }
    public void setOnDiscardAction(Runnable action) { onDiscardAction.set(action); }

    // -- drop hint text
    private double dropHintLockedWidth;
    private Region dropHintIcon;

    /**
     * While non-null, the node shows a drop hint in place of the component's text and identicon —
     * the drag-and-drop icon (styled as {@code .component-item-drop-hint-icon}) with the given
     * message, matching {@code KLComponentControl}'s drop area — e.g. while an accepted drag
     * hovers this node. The node is locked at its current width for the duration, so the hint
     * ellipsizes rather than resizing the chip. Set back to null to restore the component's own
     * text, identicon and sizing.
     */
    private final StringProperty dropHintText = new SimpleStringProperty(this, "dropHintText") {
        @Override
        protected void invalidated() {
            if (get() != null) {
                dropHintLockedWidth = getWidth();
                if (dropHintIcon == null) {
                    dropHintIcon = new Region();
                    dropHintIcon.getStyleClass().add("component-item-drop-hint-icon");
                }
                textLabel.textProperty().unbind();
                textLabel.setText(get());
                textLabel.setGraphic(dropHintIcon);
            } else {
                textLabel.setGraphic(iconImageView);
                setupComponentItemUIBinding();
            }
            requestLayout();
        }
    };
    public String getDropHintText() { return dropHintText.get(); }
    public StringProperty dropHintTextProperty() { return dropHintText; }
    public void setDropHintText(String text) { dropHintText.set(text); }

    // -- context menu items supplier
    /**
     * Optional supplier of extra {@link MenuItem}s appended (after a separator) to the standard
     * component context menu. The host sets this to contribute host-specific actions — e.g. the
     * axiom tree adds "Remove axiom" to its is-a chips. When set, the menu is rebuilt on each
     * request so the supplied items can reflect current state.
     */
    private final ObjectProperty<Supplier<List<MenuItem>>> contextMenuItemsSupplier = new SimpleObjectProperty<>();
    public Supplier<List<MenuItem>> getContextMenuItemsSupplier() { return contextMenuItemsSupplier.get(); }
    public ObjectProperty<Supplier<List<MenuItem>>> contextMenuItemsSupplierProperty() { return contextMenuItemsSupplier; }
    public void setContextMenuItemsSupplier(Supplier<List<MenuItem>> supplier) { contextMenuItemsSupplier.set(supplier); }

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