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
package dev.ikm.komet.layout_engine.component.area;

import dev.ikm.komet.framework.observable.ObservableSemantic;
import dev.ikm.komet.framework.observable.ObservableSemanticSnapshot;
import dev.ikm.komet.framework.observable.ObservableSemanticVersion;
import dev.ikm.komet.framework.view.ObservableView;
import dev.ikm.komet.framework.view.ObservableViewNoOverride;
import dev.ikm.komet.framework.view.ViewProperties;
import dev.ikm.komet.layout.KlArea;
import dev.ikm.komet.layout.KlPeerable;
import dev.ikm.komet.layout.area.KlAxiomArea;
import dev.ikm.komet.layout.context.KlContext;
import dev.ikm.komet.layout.preferences.KlPreferencesFactory;
import dev.ikm.komet.layout_engine.blueprint.SupplementalAreaBlueprint;
import dev.ikm.komet.preferences.KometPreferences;
import dev.ikm.tinkar.coordinate.logic.PremiseType;
import dev.ikm.tinkar.coordinate.stamp.calculator.Latest;
import dev.ikm.tinkar.coordinate.view.ViewCoordinateRecord;
import dev.ikm.tinkar.terms.EntityFacade;
import javafx.application.Platform;
import javafx.beans.property.ObjectProperty;
import javafx.beans.property.SimpleObjectProperty;
import javafx.scene.Node;
import javafx.scene.Scene;
import javafx.scene.control.Label;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.Priority;
import javafx.stage.Window;
import javafx.util.Subscription;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Optional;

/**
 * Shared base for the swappable concept axiom-view supplemental areas (ike-issues#644/#639): it
 * implements the {@link KlAxiomArea} injection seam and the common "resolve the concept's axiom
 * semantic for the current premise, then render it" flow, leaving only the actual node-building to
 * subclasses via {@link #renderAxioms}.
 *
 * <p>The host window injects the focused concept ({@link #setFocusConcept}), the
 * {@link ViewProperties} ({@link #setAxiomViewProperties}) and the {@link PremiseType}
 * ({@link #setPremiseType}); the area then resolves the axiom semantic (the same
 * {@code ObservableSemantic.getAxiomSnapshot} path the inline popover uses) and mounts whatever
 * {@link #renderAxioms} returns. {@code ClassicAxiomArea} (wrapping the classic {@code AxiomView})
 * and {@code KonceptAxiomTreeArea} (the refreshed KonceptBadge tree) are the two implementations.
 */
public abstract class AbstractAxiomArea extends SupplementalAreaBlueprint implements KlAxiomArea {

    private static final Logger LOG = LoggerFactory.getLogger(AbstractAxiomArea.class);

    private final ObjectProperty<EntityFacade> focus = new SimpleObjectProperty<>(this, "focus");
    private ViewProperties injectedViewProperties;
    private PremiseType premiseType = PremiseType.INFERRED;

    // Two coordinate sources are tracked while we diagnose responsiveness (ike-issues#637):
    //   • the injected kview ViewProperties (what the chapter-window View Options popup writes to), and
    //   • the scene-graph-resolved KL context view (viewForContext(), walked up the KL_CONTEXT chain:
    //     KB → Journal → Journal window → per-window coordinate).
    // We subscribe to both and log each change so the running app reveals which one the popup actually
    // drives and what the context resolves to (a real ViewContext vs the KnowledgeBaseContext default).
    private Subscription injectedViewSubscription;
    private Subscription contextViewSubscription;
    private boolean sceneListenerInstalled;

    {
        focus.addListener((obs, oldFocus, newFocus) -> rebuildLater());
    }

    /**
     * Restore constructor.
     *
     * @param preferences the preferences node backing this area
     */
    protected AbstractAxiomArea(KometPreferences preferences) {
        super(preferences);
    }

    /**
     * Create constructor.
     *
     * @param preferencesFactory factory for this area's preferences node
     * @param areaFactory        the factory creating this area
     */
    protected AbstractAxiomArea(KlPreferencesFactory preferencesFactory, KlArea.Factory areaFactory) {
        super(preferencesFactory, areaFactory);
    }

    /**
     * Renders the resolved axiom semantic into a node to mount in the area. Implemented by each
     * renderer (classic {@code AxiomView} wrapper, refreshed KonceptBadge tree, …).
     *
     * @param axiomVersion   the concept's stated/inferred axiom semantic version
     * @param premiseType    the premise being rendered
     * @param viewProperties the view to render against
     * @return the node to display
     */
    protected abstract Node renderAxioms(ObservableSemanticVersion axiomVersion,
                                         PremiseType premiseType,
                                         ViewProperties viewProperties);

    @Override
    public final void setFocusConcept(EntityFacade concept) {
        focus.set(concept);
    }

    /**
     * Returns the concept currently rendered.
     *
     * @return the focused concept, or {@code null} if none
     */
    public final EntityFacade getFocusConcept() {
        return focus.get();
    }

    /**
     * The concept this area renders; a host may bind this to the surrounding layout's focus.
     *
     * @return the focus property
     */
    public final ObjectProperty<EntityFacade> focusProperty() {
        return focus;
    }

    @Override
    public final void setAxiomViewProperties(ViewProperties viewProperties) {
        this.injectedViewProperties = viewProperties;
        installSceneListenerIfNeeded();
        wireCoordinateSubscriptions();
        rebuildLater();
    }

    /**
     * Installs a one-time listener on this area's scene-graph node so that, whenever it is (re)attached
     * to a scene, the scene-graph-resolved {@link #viewForContext() context coordinate} is re-resolved
     * and re-subscribed. The KL_CONTEXT a node inherits depends on its position in the scene graph, so
     * the context view is only meaningful once the node is attached.
     */
    private void installSceneListenerIfNeeded() {
        if (sceneListenerInstalled) {
            return;
        }
        if (fxObject() instanceof Node node) {
            sceneListenerInstalled = true;
            node.sceneProperty().subscribe((oldScene, newScene) -> {
                LOG.debug("AxiomArea[{}] scene changed (attached={}); re-resolving context coordinate",
                        getClass().getSimpleName(), newScene != null);
                wireCoordinateSubscriptions();
                rebuildLater();
            });
        }
    }

    /**
     * (Re)subscribes to both coordinate sources — the injected kview {@link ViewProperties} and the
     * scene-graph KL {@link #viewForContext() context view} — using the change (BiConsumer) overload so
     * every value change fires reliably. Each subscription logs on change; the initial resolution of
     * both is logged here so the running app shows which coordinate is wired where.
     */
    private void wireCoordinateSubscriptions() {
        if (injectedViewSubscription != null) {
            injectedViewSubscription.unsubscribe();
            injectedViewSubscription = null;
        }
        if (contextViewSubscription != null) {
            contextViewSubscription.unsubscribe();
            contextViewSubscription = null;
        }

        if (injectedViewProperties != null) {
            ObservableView injectedView = injectedViewProperties.nodeView();
            injectedViewSubscription = injectedView.subscribe(
                    (oldView, newView) -> onCoordinateChanged("injected nodeView", injectedView));
        }
        try {
            ObservableView contextView = viewForContext();
            if (contextView != null) {
                contextViewSubscription = contextView.subscribe(
                        (oldView, newView) -> onCoordinateChanged("context view", contextView));
            }
        } catch (RuntimeException e) {
            LOG.warn("AxiomArea could not subscribe to the scene-graph context view", e);
        }

        LOG.debug("AxiomArea[{}] wired coordinates: injected={} | {} | premise={}",
                getClass().getSimpleName(),
                injectedViewProperties == null ? "<null>" : describe(injectedViewProperties.nodeView()),
                describeContext(), premiseType);
        logContextChain("wire");
    }

    /**
     * Common handler for a change from either coordinate source: logs which source fired, the full
     * scene-graph context chain (so we can see whether the change reached any wired {@code KL_CONTEXT}),
     * then rebuilds.
     */
    private void onCoordinateChanged(String source, ObservableView view) {
        LOG.debug("AxiomArea[{}] coordinate change [{}] → {}", getClass().getSimpleName(), source, describe(view));
        logContextChain(source + "-change");
        rebuildLater();
    }

    /**
     * Walks this area's actual scene-graph ancestry — every {@link Node} up to the root, then the
     * {@link Scene} and {@link Window} — and logs the {@code KL_CONTEXT} (if any) attached at each level,
     * with the name each resolves for the focused concept. This shows exactly where the view-coordinate
     * hierarchy (KB → Journal → Journal window → per-window coordinate) is wired into the graph, and is
     * the authoritative answer to which coordinate {@link #viewForContext()} resolves and why.
     *
     * @param when a short label for the moment this snapshot was taken (e.g. {@code "wire"})
     */
    private void logContextChain(String when) {
        if (!(fxObject() instanceof Node startNode)) {
            LOG.debug("AxiomArea[{}] context chain @{}: fxObject is not a Node: {}",
                    getClass().getSimpleName(), when, fxObject());
            return;
        }
        EntityFacade concept = focus.get();
        StringBuilder sb = new StringBuilder();
        sb.append("AxiomArea[").append(getClass().getSimpleName()).append("] context chain @")
                .append(when).append(" (focus=").append(concept == null ? "<none>" : concept.nid()).append("):");

        // KL-native ordered context hierarchy (nearest first, up to the KnowledgeBaseContext): this is
        // the authoritative KB → Journal → Journal window → per-window list the architecture itself
        // walks (KlView.contexts() → recursiveAddContexts over the KL peer chain).
        try {
            int idx = 0;
            sb.append(String.format("%n  ordered contexts() [KL-native, nearest first]:"));
            for (KlContext kc : contexts()) {
                ObservableView view = kc.viewCoordinate();
                String resolved = (concept == null)
                        ? "<no focus>"
                        : view.calculator().getDescriptionTextOrNid(concept.nid());
                sb.append(String.format("%n    (%d) '%s'(%s) view#%08x → '%s'",
                        idx++, kc.name(), kc.getClass().getSimpleName(), System.identityHashCode(view), resolved));
            }
        } catch (RuntimeException e) {
            sb.append(String.format("%n  ordered contexts() <error: %s>", e));
        }

        // Raw scene-graph walk, as a cross-check on where each KL_CONTEXT is physically attached.
        sb.append(String.format("%n  raw scene-graph nodes:"));
        int depth = 0;
        for (Node n = startNode; n != null; n = n.getParent(), depth++) {
            sb.append(String.format("%n  [%d] %s%s", depth, nodeTypeName(n),
                    contextTag(n.hasProperties() ? n.getProperties().get(KlPeerable.PropertyKeys.KL_CONTEXT) : null, concept)));
        }
        Scene scene = startNode.getScene();
        if (scene == null) {
            sb.append(String.format("%n  [scene] <not attached to a scene>"));
        } else {
            sb.append(String.format("%n  [scene] %s%s", nodeTypeName(scene),
                    contextTag(scene.hasProperties() ? scene.getProperties().get(KlPeerable.PropertyKeys.KL_CONTEXT) : null, concept)));
            Window window = scene.getWindow();
            if (window != null) {
                sb.append(String.format("%n  [window] %s%s", nodeTypeName(window),
                        contextTag(window.hasProperties() ? window.getProperties().get(KlPeerable.PropertyKeys.KL_CONTEXT) : null, concept)));
            }
        }
        sb.append(String.format("%n  → context() resolves to %s", describeContext()));
        LOG.debug(sb.toString());
    }

    /**
     * Names a scene-graph node for the chain log. {@link Class#getSimpleName()} returns "" for an
     * anonymous class (which is why nodes printed as blank), so for those we fall back to the binary
     * name (which shows the enclosing class and anonymous index, e.g. {@code KLWorkspace$1}) plus the
     * superclass it extends. Any {@code fx:id} and style classes are appended, since those usually
     * pin down exactly which node it is.
     *
     * @param node the {@link Node}, {@link Scene} or {@link Window} to name
     * @return a human-identifiable type description
     */
    private static String nodeTypeName(Object node) {
        Class<?> c = node.getClass();
        String name = c.getSimpleName();
        if (name.isEmpty()) {
            Class<?> superclass = c.getSuperclass();
            name = c.getName() + " (anon extends " + (superclass == null ? "Object" : superclass.getSimpleName()) + ")";
        }
        if (node instanceof Node n) {
            if (n.getId() != null && !n.getId().isEmpty()) {
                name += " #" + n.getId();
            }
            if (!n.getStyleClass().isEmpty()) {
                name += " ." + String.join(".", n.getStyleClass());
            }
        }
        return name;
    }

    /**
     * Formats the {@code KL_CONTEXT} found at one scene-graph level, or an empty string if none.
     */
    private String contextTag(Object ctx, EntityFacade concept) {
        if (!(ctx instanceof KlContext kc)) {
            return "";
        }
        try {
            ObservableView view = kc.viewCoordinate();
            String resolved = (concept == null)
                    ? "<no focus>"
                    : view.calculator().getDescriptionTextOrNid(concept.nid());
            return String.format("  ← KL_CONTEXT '%s'(%s) view#%08x → '%s'",
                    kc.name(), kc.getClass().getSimpleName(), System.identityHashCode(view), resolved);
        } catch (RuntimeException e) {
            return "  ← KL_CONTEXT <error: " + e + ">";
        }
    }

    /**
     * Renders a compact identity for an {@link ObservableView}: its concrete type, identity hash, and
     * the name it currently resolves for the focused concept (so a description-type reorder is visible
     * as regular-name ↔ fully-qualified-name).
     */
    private String describe(ObservableView view) {
        if (view == null) {
            return "<null>";
        }
        EntityFacade concept = focus.get();
        String resolved = (concept == null)
                ? "<no focus>"
                : view.calculator().getDescriptionTextOrNid(concept.nid());
        return String.format("%s#%08x→'%s'",
                view.getClass().getSimpleName(), System.identityHashCode(view), resolved);
    }

    /**
     * Renders a compact identity for the scene-graph-resolved {@link KlContext}: its name, concrete type
     * (a real {@code ViewContext} vs the {@code KnowledgeBaseContext} default tells us whether a
     * window-level coordinate is wired above this area) and the name it resolves for the focused concept.
     */
    private String describeContext() {
        try {
            KlContext ctx = context();
            ObservableView view = ctx.viewCoordinate();
            EntityFacade concept = focus.get();
            String resolved = (concept == null)
                    ? "<no focus>"
                    : view.calculator().getDescriptionTextOrNid(concept.nid());
            return String.format("context='%s'(%s) view#%08x→'%s'",
                    ctx.name(), ctx.getClass().getSimpleName(), System.identityHashCode(view), resolved);
        } catch (RuntimeException e) {
            return "context=<unresolved: " + e + ">";
        }
    }

    /**
     * KL-native context hook. {@code StateAndContextBlueprint.subscribeToContext()} (final; invoked by
     * the {@code KlContext} depth-first signal when the scene-graph context is established or changes)
     * subscribes {@code context().viewCoordinate().subscribe(this::contextChanged)}. Overriding
     * {@code contextChanged()} — the blueprint's designated extension point — lets this area rebuild
     * when the scene-graph-resolved coordinate changes. This fires only when the host runs the KL
     * context lifecycle; the explicit subscriptions in {@link #wireCoordinateSubscriptions()} cover hosts
     * (such as the hybrid gen-purpose chapter window) that may not.
     */
    @Override
    public void contextChanged() {
        LOG.debug("AxiomArea[{}] contextChanged() [KL lifecycle hook]", getClass().getSimpleName());
        logContextChain("contextChanged");
        rebuildLater();
    }

    @Override
    public final void setPremiseType(PremiseType premiseType) {
        this.premiseType = (premiseType == null) ? PremiseType.INFERRED : premiseType;
        rebuildLater();
    }

    private ViewProperties resolveViewProperties() {
        if (injectedViewProperties != null) {
            return injectedViewProperties;
        }
        try {
            ViewCoordinateRecord viewRecord = context().viewCoordinate().toViewCoordinateRecord();
            String name = getClass().getSimpleName();
            return new ObservableViewNoOverride(viewRecord, name).makeOverridableViewProperties(name);
        } catch (RuntimeException e) {
            LOG.warn("Could not derive view properties from context", e);
            return null;
        }
    }

    private void rebuildLater() {
        if (Platform.isFxApplicationThread()) {
            rebuild();
        } else {
            Platform.runLater(this::rebuild);
        }
    }

    private void rebuild() {
        GridPane host = gridPaneForChildren();
        host.getChildren().clear();
        EntityFacade concept = focus.get();
        ViewProperties viewProperties = resolveViewProperties();
        if (concept == null || viewProperties == null) {
            host.add(new Label("No concept in focus."), 0, 0);
            return;
        }
        // Temporary coordinate-responsiveness diagnostic (ike-issues#637): logs both coordinate sources
        // on every rebuild, so a reorder in View Options shows which one (if either) re-resolves the
        // focus concept's name from the regular name to the fully-qualified name. We render from the
        // injected view (rendering is unchanged); the context line shows what the scene-graph would give.
        LOG.debug("AxiomArea rebuild: concept nid={} premise={} | injected={} | {}",
                concept.nid(), premiseType,
                describe(injectedViewProperties == null ? null : injectedViewProperties.nodeView()),
                describeContext());
        try {
            Optional<ObservableSemanticSnapshot> snapshot =
                    ObservableSemantic.getAxiomSnapshot(concept.nid(), premiseType, viewProperties.calculator());
            Latest<ObservableSemanticVersion> latestAxiom =
                    snapshot.map(ObservableSemanticSnapshot::getLatestVersion).orElse(null);
            if (latestAxiom == null || !latestAxiom.isPresent()) {
                host.add(new Label("No " + premiseType.toString().toLowerCase() + " axioms for this concept."), 0, 0);
                return;
            }
            Node rendered = renderAxioms(latestAxiom.get(), premiseType, viewProperties);
            GridPane.setHgrow(rendered, Priority.ALWAYS);
            GridPane.setVgrow(rendered, Priority.ALWAYS);
            host.add(rendered, 0, 0);
        } catch (RuntimeException e) {
            LOG.error("Failed to render axioms for {}", concept, e);
            host.add(new Label("Could not render axioms."), 0, 0);
        }
    }

    @Override
    protected void subAreaRestoreFromPreferencesOrDefault() {
        // Focus, view, and premise are supplied by the host; nothing persisted by this area in v1.
    }

    @Override
    protected void subAreaRevert() {
        // No persisted area-specific state to revert.
    }

    @Override
    protected void subAreaSave() {
        // No persisted area-specific state to save.
    }

    @Override
    public void knowledgeLayoutBind() {
        Platform.runLater(() -> {
            this.lifecycleState.set(LifecycleState.BOUND);
            rebuild();
        });
    }

    @Override
    public void knowledgeLayoutUnbind() {
        if (injectedViewSubscription != null) {
            injectedViewSubscription.unsubscribe();
            injectedViewSubscription = null;
        }
        if (contextViewSubscription != null) {
            contextViewSubscription.unsubscribe();
            contextViewSubscription = null;
        }
    }
}
