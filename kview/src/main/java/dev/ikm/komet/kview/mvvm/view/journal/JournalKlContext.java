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
package dev.ikm.komet.kview.mvvm.view.journal;

import dev.ikm.komet.framework.view.ObservableView;
import dev.ikm.komet.layout.KlPeerable;
import dev.ikm.komet.layout.context.KlContext;
import dev.ikm.tinkar.common.id.PublicIdStringKey;
import dev.ikm.tinkar.common.id.PublicIds;
import javafx.scene.Node;

import java.util.Optional;

/**
 * The journal stratum of the view-coordinate hierarchy: a minimal {@link KlContext} that exposes the
 * journal's live overridable coordinate ({@code journalViewProperties.nodeView()}) on the
 * {@code KLWorkspace} pane, so a chapter window resolves it as its coordinate parent by walking the
 * scene graph's {@code KL_CONTEXT} chain and establishes its own coordinate {@code childOf} it
 * (ike-issues#698, KB &rarr; journal &rarr; window).
 * <p>
 * This is intentionally a <em>parent-only</em> context: it is consumed solely via {@link #viewCoordinate()}
 * (the only method {@code KlContext.childOf} calls on a parent), so the remaining members are inert.
 * It deliberately does <em>not</em> reuse {@code ViewContext}/{@code ViewContextBlueprint}, because those
 * carry full layout-area machinery (a preferences node, a factory, area grid settings, a {@code BorderPane})
 * that a coordinate-parent does not need. A first-class journal {@code KlView} that owns a real
 * {@code ViewContext} — and that {@code KlView.recursiveAddContexts} can traverse so {@code contexts()}
 * reports the journal stratum — is deferred to the journal-as-gadget work.
 * <p>
 * <b>Invariant:</b> nothing walks this context's {@link #klPeer()}. The cascade only reads
 * {@link #viewCoordinate()}, and {@code KlView.recursiveAddContexts} terminates at the window's
 * {@code KlTopView} (adding the knowledge-base terminus) before it can climb to the workspace pane, so it
 * never resolves this context. {@link #klPeer()} therefore fails loud rather than returning a value that
 * would misclassify in the {@code KlPeerable}-sealed switch — if it is ever called, that invariant has
 * broken and the journal stratum must become a first-class {@code KlView}.
 */
public final class JournalKlContext implements KlContext {

    private final ObservableView journalView;
    private final PublicIdStringKey<KlContext> contextId;

    /**
     * Creates the journal context over the journal's live overridable coordinate.
     *
     * @param journalView the journal's live overridable view, i.e. {@code journalViewProperties.nodeView()}
     */
    public JournalKlContext(ObservableView journalView) {
        this.journalView = journalView;
        this.contextId = new PublicIdStringKey<>(PublicIds.newRandom(), "Journal context");
    }

    @Override
    public ObservableView viewCoordinate() {
        return journalView;
    }

    @Override
    public PublicIdStringKey<KlContext> contextId() {
        return contextId;
    }

    /**
     * {@inheritDoc}
     * <p>Unsupported: this context is parent-only and is never the subject of a peer walk (see the class
     * invariant). Failing loud surfaces a broken invariant instead of a silent misclassification.
     */
    @Override
    public KlPeerable klPeer() {
        throw new UnsupportedOperationException(
                "JournalKlContext is a parent-only coordinate context; klPeer() must not be called. "
                        + "If it was, KlView.recursiveAddContexts reached the journal stratum and it must "
                        + "become a first-class KlView (ike-issues#698).");
    }

    @Override
    public Optional<Node> graphic() {
        return Optional.empty();
    }

    /**
     * {@inheritDoc}
     * <p>No-op: dependent chapter-window contexts subscribe themselves on scene-graph attach
     * ({@code knowledgeLayoutBind}); the journal context does not drive the dependent-context DFS.
     */
    @Override
    public void subscribeDependentContexts() {
        // intentionally empty — see class documentation
    }

    /**
     * {@inheritDoc}
     * <p>No-op counterpart to {@link #subscribeDependentContexts()}.
     */
    @Override
    public void unsubscribeDependentContexts() {
        // intentionally empty — see class documentation
    }
}
