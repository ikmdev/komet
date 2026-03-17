/*
 * Copyright © 2015 Integrated Knowledge Management (support@ikm.dev)
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package dev.ikm.komet.kview.klwindows;

import dev.ikm.komet.framework.view.ObservableView;
import dev.ikm.komet.layout.KlPeerable;
import dev.ikm.komet.layout.context.KlContext;
import dev.ikm.komet.layout.context.KnowledgeBaseContext;
import dev.ikm.tinkar.common.id.PublicIdStringKey;
import dev.ikm.tinkar.common.id.PublicIds;
import javafx.scene.Node;

import java.util.Optional;
import java.util.UUID;

/**
 * A lightweight bridge from {@link dev.ikm.komet.framework.view.ViewProperties} /
 * {@link ObservableView} to {@link KlContext}.
 * <p>
 * This adapter is used during the migration from the legacy {@code ViewProperties} pattern
 * to the {@code KlContext}-based architecture. By setting an instance of this class on
 * JavaFX nodes via {@link KlPeerable.PropertyKeys#KL_CONTEXT}, any
 * {@link dev.ikm.komet.layout.KlView}-based component can discover view coordinates
 * through the scene graph walk mechanism.
 * <p>
 * The {@link #klPeer()} method returns the global {@link KnowledgeBaseContext} singleton
 * as a fallback peer, since this bridge operates outside the full knowledge-layout
 * lifecycle. The {@code subscribeDependentContexts()} and {@code unsubscribeDependentContexts()}
 * methods are no-ops because legacy {@code ViewProperties}-based components do not
 * participate in the {@code KlGadget} subscription lifecycle.
 *
 * @param contextId      unique identifier for this context
 * @param observableView the observable view coordinate backing this context
 * @see KlContext
 * @see dev.ikm.komet.layout.KlView#context(Node)
 */
public record ViewPropertiesKlContext(
        PublicIdStringKey<KlContext> contextId,
        ObservableView observableView) implements KlContext {

    /**
     * Creates a bridge context with a generated UUID and the given name.
     *
     * @param name           a descriptive name for this context (e.g. "JournalController")
     * @param observableView the observable view coordinate to expose via {@link #viewCoordinate()}
     */
    public ViewPropertiesKlContext(String name, ObservableView observableView) {
        this(new PublicIdStringKey<>(PublicIds.of(UUID.randomUUID()), name), observableView);
    }

    @Override
    public KlPeerable klPeer() {
        return KnowledgeBaseContext.INSTANCE;
    }

    @Override
    public Optional<Node> graphic() {
        return Optional.empty();
    }

    @Override
    public ObservableView viewCoordinate() {
        return observableView;
    }

    @Override
    public void unsubscribeDependentContexts() {
        // No-op: ViewProperties-based components don't use KlGadget subscriptions
    }

    @Override
    public void subscribeDependentContexts() {
        // No-op: ViewProperties-based components don't use KlGadget subscriptions
    }
}
