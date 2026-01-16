package dev.ikm.komet.layout;

import dev.ikm.komet.layout.context.KlContext;
import dev.ikm.komet.layout.context.KlContextProvider;

public non-sealed interface KlKnowledgeBaseContext extends KlObject, KlContextProvider {
    KlContext context();
}
