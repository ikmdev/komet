package dev.ikm.komet.layout.window;

import dev.ikm.komet.layout.KlGadget;
import dev.ikm.komet.layout.KlStateCommands;
import dev.ikm.komet.layout.context.KlContext;
import dev.ikm.komet.layout.context.KlContextProvider;
import dev.ikm.komet.layout.context.KnowledgeBaseContext;
import javafx.scene.Node;
import javafx.scene.Scene;
import javafx.stage.Window;

/**
 * Placeholder for now.
 */
public non-sealed interface KlJournalWindow<T extends Node> extends KlStateCommands, KlContextProvider, KlGadget<T> {
    @Override
    default KlContext context() {
        if (this.fxGadget() instanceof Node node) {
            return KlGadget.context(node);
        }
        return KnowledgeBaseContext.INSTANCE.context();
    }
}
