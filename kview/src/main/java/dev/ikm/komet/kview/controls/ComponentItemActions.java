package dev.ikm.komet.kview.controls;

import dev.ikm.komet.framework.dnd.KonceptDragGlyph;
import dev.ikm.komet.framework.dnd.KonceptDragSource;
import dev.ikm.komet.kview.mvvm.view.JournalNavigationUtils;
import dev.ikm.komet.kview.mvvm.view.common.SVGConstants;
import dev.ikm.tinkar.common.id.PublicId;
import dev.ikm.tinkar.common.service.PrimitiveData;
import dev.ikm.tinkar.coordinate.Calculators;
import javafx.embed.swing.SwingFXUtils;
import javafx.scene.Node;
import javafx.scene.control.ContextMenu;
import javafx.scene.control.MenuItem;
import javafx.scene.image.Image;
import javafx.scene.input.Clipboard;
import javafx.scene.input.ClipboardContent;
import javafx.scene.input.Dragboard;
import javafx.scene.layout.StackPane;
import javafx.scene.shape.FillRule;
import javafx.scene.shape.SVGPath;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.ByteArrayOutputStream;
import java.util.Base64;

import static dev.ikm.komet.framework.dnd.KometClipboard.COMPONENT_DRAG_FORMAT;
import static dev.ikm.komet.framework.dnd.KometClipboard.encodePublicId;
import static dev.ikm.komet.kview.controls.KometIcon.IconValue.POPULATE;

/**
 * Shared drag-out, copy-to-clipboard and context menu behavior for a rendered component
 * (icon + text), so a component behaves the same whether it is rendered by a read only
 * control (through {@link ComponentItemNode}) or by an editable control skin.
 */
public final class ComponentItemActions {

    private ComponentItemActions() {}

    /**
     * Builds the standard component context menu ("Open in Concept Navigator" for concepts,
     * "Open in Journal", "Copy to clipboard") for the component rendered by the given node.
     */
    public static ContextMenu buildContextMenu(Node owner, ComponentItem componentItem) {
        ContextMenu menu = new ContextMenu();
        menu.getStyleClass().add("klcontext-menu");

        if (componentItem.getPublicId() != null) {
            if (componentItem.isConcept()) {
                MenuItem openInConceptNavigatorItem = new MenuItem("Open in Concept Navigator", KometIcon.create(POPULATE, "icon-klcontext-menu"));
                openInConceptNavigatorItem.setOnAction(_ ->
                        JournalNavigationUtils.openConceptInNavigatorForContainingJournal(
                                owner,
                                owner,
                                componentItem.getPublicId()
                        )
                );

                menu.getItems().add(openInConceptNavigatorItem);
            }

            MenuItem openInJournalItem = new MenuItem("Open in Journal", KometIcon.create(POPULATE, "icon-klcontext-menu"));
            openInJournalItem.setOnAction(_ ->
                    JournalNavigationUtils.openEntityInJournalForContainingJournal(
                            owner,
                            owner,
                            componentItem.getPublicId()
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
        copyItem.setOnAction(e -> copyToClipboard(componentItem));

        menu.getItems().add(copyItem);
        return menu;
    }

    private static void copyToClipboard(ComponentItem componentItem) {
        ClipboardContent content = buildClipboardContent(componentItem);
        if (!content.isEmpty()) {
            Clipboard.getSystemClipboard().setContent(content);
        }
    }

    /**
     * Builds the clipboard content for a component: the {@code COMPONENT_DRAG_FORMAT} encoded
     * public id, plus plain-text and HTML renderings of the component's title and icon.
     */
    public static ClipboardContent buildClipboardContent(ComponentItem componentItem) {
        ClipboardContent content = new ClipboardContent();

        PublicId publicId = componentItem.getPublicId();
        content.put(COMPONENT_DRAG_FORMAT, encodePublicId(publicId));

        String title = componentItem.getText();
        if (title != null && !title.isBlank()) {
            content.putString(title);
        }

        String html = buildHtmlPayload(componentItem.getIcon(), title);
        if (html != null) {
            content.putHtml(html);
        }

        return content;
    }

    /**
     * Sets the drag view for a component drag. A concept drags as the canonical koncept pill,
     * built from its identity (ike-issues#854); a non-concept component drags as a snapshot
     * of the given node.
     */
    public static void setDragView(Dragboard dragboard, ComponentItem componentItem, Node snapshotNode) {
        if (componentItem != null && componentItem.isConcept() && componentItem.getPublicId() != null) {
            // Resolve name (fully-qualified first) and inactive through the default view — the
            // same overload the navigators use — so the glyph is identical from every source.
            KonceptDragGlyph.setDragView(dragboard,
                    PrimitiveData.nid(componentItem.getPublicId()), Calculators.View.Default());
        } else {
            KonceptDragSource.setDragView(dragboard, snapshotNode);
        }
    }

    /**
     * Builds an HTML fragment containing the identicon as a base64-encoded PNG
     * and the entity title side by side. Returns null if both inputs are absent.
     */
    private static String buildHtmlPayload(Image identiconImage, String title) {
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
    private static String toBase64Png(Image image) {
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
    private static String escapeHtml(String text) {
        return text.replace("&", "&amp;")
                .replace("<", "&lt;")
                .replace(">", "&gt;")
                .replace("\"", "&quot;");
    }
}