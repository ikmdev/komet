package dev.ikm.komet.kview.controls;

import dev.ikm.komet.framework.dnd.KonceptDragSource;
import dev.ikm.komet.kview.mvvm.view.JournalNavigationUtils;
import dev.ikm.komet.kview.mvvm.view.common.SVGConstants;
import dev.ikm.tinkar.common.id.PublicId;
import javafx.beans.property.BooleanProperty;
import javafx.beans.property.ObjectProperty;
import javafx.beans.property.SimpleBooleanProperty;
import javafx.beans.property.SimpleObjectProperty;
import javafx.scene.control.ContextMenu;
import javafx.scene.control.Label;
import javafx.scene.control.MenuItem;
import javafx.scene.control.Tooltip;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.input.Clipboard;
import javafx.scene.input.ClipboardContent;
import javafx.scene.input.ContextMenuEvent;
import javafx.scene.input.Dragboard;
import javafx.scene.input.TransferMode;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.Region;
import javafx.scene.layout.StackPane;
import javafx.scene.shape.Circle;
import javafx.embed.swing.SwingFXUtils;
import javafx.scene.shape.FillRule;
import javafx.scene.shape.SVGPath;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.ByteArrayOutputStream;
import java.util.Base64;
import java.util.function.Supplier;

import static dev.ikm.komet.framework.dnd.KometClipboard.COMPONENT_DRAG_FORMAT;
import static dev.ikm.komet.framework.dnd.KometClipboard.encodePublicId;
import static dev.ikm.komet.kview.controls.KometIcon.IconValue.POPULATE;

/**
 * A Node used to render a Component (icon + text)
 */
public class ComponentItemNode extends Region {
    private final ImageView iconImageView = new ImageView();
    private final Label textLabel = new Label();

    private Circle circleClip;

    private ContextMenu contextMenu;

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
        HBox.setHgrow(textLabel, Priority.ALWAYS);

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
            contextMenu = buildDefaultContextMenu();
        }

        pseudoClassStateChanged(KLReadOnlyMultiComponentControl.EDIT_MODE_PSEUDO_CLASS, true);

        contextMenu.setOnHidden(event -> pseudoClassStateChanged(KLReadOnlyMultiComponentControl.EDIT_MODE_PSEUDO_CLASS, false));
        contextMenu.show(this, contextMenuEvent.getScreenX(), contextMenuEvent.getScreenY());
    }

    private ContextMenu buildDefaultContextMenu() {
        ContextMenu menu = new ContextMenu();
        menu.getStyleClass().add("klcontext-menu");

        if (componentItem.get().getPublicId() != null) {
            if (componentItem.get().isConcept()) {
                MenuItem openInConceptNavigatorItem = new MenuItem("Open in Concept Navigator", KometIcon.create(POPULATE, "icon-klcontext-menu"));
                openInConceptNavigatorItem.setOnAction(_ ->
                        JournalNavigationUtils.openConceptInNavigatorForContainingJournal(
                                this,
                                this,
                                componentItem.get().getPublicId()
                        )
                );

                menu.getItems().add(openInConceptNavigatorItem);
            }

            MenuItem openInJournalItem = new MenuItem("Open in Journal", KometIcon.create(POPULATE, "icon-klcontext-menu"));
            openInJournalItem.setOnAction(_ ->
                    JournalNavigationUtils.openEntityInJournalForContainingJournal(
                            this,
                            this,
                            componentItem.get().getPublicId()
                    )
            );

            menu.getItems().add(openInJournalItem);
        }

        // the SVG graphic for the copy to clipboard icon
        var svgPath = new SVGPath();
        svgPath.setContent(SVGConstants.COPY_TO_CLIPBOARD_SVG_PATH);
        svgPath.setFillRule(FillRule.EVEN_ODD);
        svgPath.getStyleClass().addAll(
                "copy-to-clipboard-svg",
                "icon-klcontext-menu"
        );

        StackPane svgIcon = new StackPane(svgPath);
        svgIcon.getStyleClass().addAll("copy-to-clipboard-icon", "icon");

        MenuItem copyItem = new MenuItem("Copy to clipboard", svgIcon);
        copyItem.setOnAction(e -> copyToClipboard());

        menu.getItems().add(copyItem);
        return menu;
    }

    private void copyToClipboard() {
        ClipboardContent content = buildClipboardContent();
        if (!content.isEmpty()) {
            Clipboard.getSystemClipboard().setContent(content);
        }
    }

    private ClipboardContent buildClipboardContent() {
        ClipboardContent content = new ClipboardContent();

        PublicId publicId = componentItem.get().getPublicId();
        content.put(COMPONENT_DRAG_FORMAT, encodePublicId(publicId));

        String title = componentItem.get().getText();
        if (title != null && !title.isBlank()) {
            content.putString(title);
        }

        String html = buildHtmlPayload(componentItem.get().getIcon(), title);
        if (html != null) {
            content.putHtml(html);
        }

        return content;
    }

    private void setupDragAndDrop() {
        setOnDragDetected(event -> {
            Dragboard dragboard = startDragAndDrop(TransferMode.COPY);

            dragboard.setContent(buildClipboardContent());

            // Drag Image
            String previousStyle = textLabel.getStyle();
            textLabel.setStyle("-fx-text-fill: #111111;");

            if (dragImageSupplier.get() != null) {
                dragboard.setDragView(dragImageSupplier.get().get());
            } else if (getScene() != null) {
                // Standard-size drag image with canonical cursor placement (right of the identicon);
                // the caller-supplied image branch above is left intact.
                KonceptDragSource.setDragView(dragboard, this);
            }

            textLabel.setStyle(previousStyle);

            event.consume();
        });
    }

    /**
     * Builds an HTML fragment containing the identicon as a base64-encoded PNG
     * and the entity title side by side. Returns null if both inputs are absent.
     */
    private String buildHtmlPayload(Image identiconImage, String title) {
        StringBuilder sb = new StringBuilder();
        sb.append("<span style=\"display:inline-flex;align-items:center;gap:6px;font-family:sans-serif;\">");

        if (identiconImage != null) {
            String base64 = toBase64Png(identiconImage);
            if (base64 != null) {
                sb.append("<img src=\"data:image/png;base64,")
                        .append(base64)
                        .append("\" width=\"24\" height=\"24\" style=\"vertical-align:middle;\"/>");
            }
        }

        if (title != null && !title.isBlank()) {
            sb.append("<span>").append(escapeHtml(title)).append("</span>");
        }

        sb.append("</span>");

        // Return null if nothing meaningful was added
        boolean hasImage = identiconImage != null;
        boolean hasTitle = title != null && !title.isBlank();
        return (hasImage || hasTitle) ? sb.toString() : null;
    }

    /**
     * Converts a JavaFX Image to a base64-encoded PNG string.
     * Returns null if conversion fails.
     */
    private String toBase64Png(Image image) {
        try {
            BufferedImage bufferedImage = SwingFXUtils.fromFXImage(image, null);
            ByteArrayOutputStream baos = new ByteArrayOutputStream();
            ImageIO.write(bufferedImage, "png", baos);
            return Base64.getEncoder().encodeToString(baos.toByteArray());
        } catch (Exception e) {
            return null;
        }
    }

    /**
     * Minimal HTML escaping to prevent title text from breaking the markup.
     **/
    private String escapeHtml(String text) {
        return text.replace("&", "&amp;")
                .replace("<", "&lt;")
                .replace(">", "&gt;")
                .replace("\"", "&quot;");
    }


    @Override
    protected double computeMinHeight(double width) {
        // Make the min height be the same as the pref height
        return super.computePrefHeight(width);
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