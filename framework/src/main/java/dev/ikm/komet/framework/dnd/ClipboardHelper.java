package dev.ikm.komet.framework.dnd;

import javafx.scene.input.Clipboard;
import javafx.scene.input.ClipboardContent;
import dev.ikm.tinkar.terms.EntityProxy;

import java.util.ArrayList;
import java.util.List;

import static dev.ikm.komet.framework.dnd.KometClipboard.*;

public class ClipboardHelper {

    public static void copyToClipboard(CharSequence charSequence) {
        final ClipboardContent clipboardContent = new ClipboardContent();
        clipboardContent.putString(charSequence.toString());
        Clipboard.getSystemClipboard().setContent(clipboardContent);
    }

    public static List<EntityProxy> getEntityProxyList() {
        if (Clipboard.getSystemClipboard().hasContent(KOMET_PROXY_LIST)) {
            return (List<EntityProxy>) Clipboard.getSystemClipboard().getContent(KOMET_PROXY_LIST);
        }
        if (Clipboard.getSystemClipboard().hasContent(KOMET_CONCEPT_PROXY)) {
            return List.of((EntityProxy) Clipboard.getSystemClipboard().getContent(KOMET_CONCEPT_PROXY));
        }
        if (Clipboard.getSystemClipboard().hasContent(KOMET_PATTERN_PROXY)) {
            return List.of((EntityProxy) Clipboard.getSystemClipboard().getContent(KOMET_PATTERN_PROXY));
        }
        if (Clipboard.getSystemClipboard().hasContent(KOMET_SEMANTIC_PROXY)) {
            return List.of((EntityProxy) Clipboard.getSystemClipboard().getContent(KOMET_SEMANTIC_PROXY));
        }
        return new ArrayList<>();
    }
}
