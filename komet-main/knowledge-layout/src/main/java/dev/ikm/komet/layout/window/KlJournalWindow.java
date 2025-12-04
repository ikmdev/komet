package dev.ikm.komet.layout.window;

import dev.ikm.komet.layout.KlGadget;
import dev.ikm.komet.layout.KlStateCommands;
import dev.ikm.komet.layout.context.KlContext;
import dev.ikm.komet.layout.context.KlContextProvider;
import dev.ikm.komet.layout.context.KnowledgeBaseContext;
import javafx.scene.Node;

/**
 * Placeholder for now.
 */
public non-sealed interface KlJournalWindow<FX extends Node> extends KlStateCommands, KlContextProvider, KlGadget<FX> {
    @Override
    default KlContext context() {
        if (this.fxGadget() instanceof Node node) {
            return KlGadget.context(node);
        }
        return KnowledgeBaseContext.INSTANCE.context();
    }
}
