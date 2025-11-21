package dev.ikm.komet.layout;

import dev.ikm.komet.framework.view.ObservableView;
import dev.ikm.komet.layout.context.KlContext;
import dev.ikm.komet.layout.context.KlContextProvider;
import dev.ikm.komet.layout.context.KnowledgeBaseContext;
import dev.ikm.komet.layout.window.KlFxWindow;
import dev.ikm.komet.layout.window.KlJournalWindow;
import dev.ikm.komet.layout.window.KlRenderView;
import dev.ikm.komet.preferences.KometPreferences;
import dev.ikm.tinkar.common.service.PluggableService;
import dev.ikm.tinkar.common.service.TinkExecutor;
import dev.ikm.tinkar.coordinate.view.calculator.ViewCalculator;
import javafx.collections.ObservableMap;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Window;
import org.eclipse.collections.api.factory.Lists;
import org.eclipse.collections.api.list.ImmutableList;
import org.eclipse.collections.api.list.MutableList;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.lang.reflect.InvocationTargetException;
import java.util.Optional;
import java.util.concurrent.Future;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.prefs.BackingStoreException;

import static dev.ikm.komet.layout.KlPeerable.PropertyKeys.KL_CONTEXT;
import static dev.ikm.komet.layout.KlRestorable.PreferenceKeys.FACTORY_CLASS_NAME;

/**
 * Represents an interface for a knowledge layout gadget with functionality for managing
 * properties, preferences, and view calculations associated with JavaFX components.
 * This interface provides methods and enumerations to define property keys, user preferences,
 * and mechanisms for retrieving contextual instances of {@code ViewCalculator}.
 *
 * @param <FX> the type of the JavaFX component associated with this gadget
 */

public sealed interface KlView<FX>
        extends KlPeerable, KlContextProvider, KlContextSensitiveComponent, KlViewLayoutLifecycle
        permits KlArea, KlTopView, KlRenderView {
    Logger LOG = LoggerFactory.getLogger(KlView.class);
    default void addChild(KlView view) {
        switch (this) {
            case KlParent<?> parent
                    when view instanceof KlArea<?> area -> parent.gridPaneForChildren().getChildren().add(area.fxObject());
            case KlRenderView renderView
                    when view instanceof KlArea<?> area -> renderView.setKlRootArea(area);
            case KlTopView<?> topView
                    when view instanceof KlRenderView renderView -> topView.setKlRenderView(renderView);
            default -> throw new IllegalStateException("Can't add " + view.getClass().getSimpleName() + " to parent: " + this.getClass().getSimpleName());
        }
    }
    default void setFxPeer(Object fxPeer) {
        this.properties().put(PropertyKeys.KL_PEER, this);
        this.properties().put(PropertyKeys.FX_PEER, fxPeer);
        switch (fxPeer) {
            case Node node -> node.setAccessibleRoleDescription("Peer for " + this.getClass().getSimpleName());
            case Window _,
                 Scene _ -> { /* Valid object type, but nothing to do. */}
            case null -> {
                this.properties().remove(PropertyKeys.KL_PEER);
                this.properties().remove(PropertyKeys.FX_PEER);
            }
            default -> throw new IllegalStateException("Unexpected value: " + fxPeer);
        }
    }

    default FX getFxPeer() {
        if (this.hasProperties()) {
            return (FX) this.properties().get(PropertyKeys.FX_PEER);
        }
        throw new IllegalStateException("Peer not found for " + this.getClass().getName());
    }

    /**
     * Provides the encapsulated JavaFx object.
     *
     * @return a JavaFx object
     */
    FX fxObject();

    @Override
    default ObservableMap<Object, Object> properties() {
        return switch (this.fxObject()) {
            case Window window -> window.getProperties();
            case Node node -> node.getProperties();
            case Scene scene -> scene.getProperties();
            default -> throw new IllegalStateException("Unexpected value: " + this.fxObject());
        };
    }

    @Override
    default boolean hasProperties() {
        return switch (this.fxObject()) {
            case Window window -> window.hasProperties();
            case Node node -> node.hasProperties();
            case Scene scene -> scene.hasProperties();
            default -> throw new IllegalStateException("Unexpected value: " + this.fxObject());
        };
    }

    @Override
    default boolean hasProperty(Object key) {
        if (hasProperties()) {
            return this.properties().containsKey(key);
        }
        return false;
    }

    /**
     * Retrieves an immutable list of KlContext objects associated with the instance.
     * The method populates the list by recursively adding contexts from the fxObject.
     *
     * @return an immutable list of KlContext objects
     */
    default ImmutableList<KlContext> contexts() {
        MutableList<KlContext> contexts = Lists.mutable.empty();
        recursiveAddContexts(this.fxObject(), contexts);
        return contexts.toImmutable();
    }

    /**
     * Recursively adds context objects to a collection based on the type of the provided FX object.
     * The method determines the context for the given object and its hierarchy (e.g., parent Node,
     * associated Scene, or Window) and adds it to the provided list of contexts.
     *
     * @param fxObject the JavaFX object, such as a Node, Scene, or Window, for which contexts are derived
     * @param contexts the collection to which the resolved contexts are added
     */
    static void recursiveAddContexts(Object fxObject, MutableList<KlContext> contexts) {
        KlContext context = switch (fxObject) {
            case Node node -> context(node);
            case Window window -> context(window);
            case Scene scene -> context(scene);
            default -> KnowledgeBaseContext.INSTANCE.context();
        };
        contexts.add(context);
        switch (context.klPeer()) {
            case KlView<?> klView -> {
                switch (klView) {
                    case KlArea<?> area when area.getFxPeer().getParent() != null ->  {
                        recursiveAddContexts(area.getFxPeer().getParent(), contexts);
                    }
                    case KlArea<?> area -> recursiveAddContexts(area.getFxPeer().getScene(), contexts);
                    case KlTopView _ -> contexts.add(KnowledgeBaseContext.INSTANCE.context());
                    case KlRenderView renderView -> recursiveAddContexts(renderView.topView(), contexts);
                }
            }
            case KlKnowledgeBaseContext _ -> {}
        }
    }

    /**
     * Retrieves the context associated with the current JavaFX gadget.
     * Depending on the type of the gadget (Node, Window, or Scene), it obtains the appropriate context.
     * If the gadget type does not match any of these, a default context is returned.
     *
     * @return the {@code KlContext} associated with the current JavaFX gadget, or a default context if no specific context is found.
     */
    default KlContext context() {
        return  switch (this.fxObject()) {
            case Node node -> context(node);
            case Window window -> context(window);
            case Scene scene -> context(scene);
            default -> KnowledgeBaseContext.INSTANCE.context();
        };
    }

    default ViewCalculator calculatorForContext() {
        return context().viewCoordinate().calculator();
    }

    /**
     * Retrieves the {@code ObservableView} for the current context.
     *
     * @return the {@code ObservableView} associated with the view coordinate of the current context.
     */
    default ObservableView viewForContext() {
        return context().viewCoordinate();
    }
    /**
     * Retrieves the {@code KlContext} associated with the provided {@code Window}.
     * If the {@code Window} has properties and the {@code KL_CONTEXT} key is present,
     * this method returns the corresponding {@code KlContext} instance. Otherwise,
     * it returns a default context using {@code KnowledgeBaseContext}.
     *
     * @param window the {@code Window} from which to retrieve the {@code KlContext}.
     *               Must have properties for the {@code KL_CONTEXT} key to be checked.
     * @return the {@code KlContext} associated with the given {@code Window}, or
     *         the default context if no specific context is found.
     */
    static KlContext context(Window window) {
        if (window.hasProperties() && window.getProperties().containsKey(KL_CONTEXT)) {
            return (KlContext) window.getProperties().get(KL_CONTEXT);
        }
        return KnowledgeBaseContext.INSTANCE.context();
    }
    /**
     * Retrieves the {@code KlContext} associated with the provided {@code Node}.
     * If the {@code Node} has properties and contains the {@code KL_CONTEXT} key,
     * this method returns the corresponding {@code KlContext} instance.
     * Otherwise, it recursively checks the parent of the node, and if no context
     * is found there, it checks the associated {@code Scene}.
     * Finally, if no specific context is discovered, a default context is returned
     * using the {@code KnowledgeBaseContext}.
     *
     * @param node the {@code Node} for which the {@code KlContext} is to be retrieved.
     *             This node must be part of a JavaFX hierarchy and may optionally have
     *             properties or parent-child relationships.
     * @return the {@code KlContext} associated with the given {@code Node},
     *         or a default context if no specific context is found.
     */
    static KlContext context(Node node) {
        if (node.hasProperties() && node.getProperties().containsKey(KL_CONTEXT)) {
            return (KlContext) node.getProperties().get(KL_CONTEXT);
        }
        Node parent = node.getParent();
        if (parent != null) {
            return context(parent);
        }
        Scene scene = node.getScene();
        if (scene != null) {
            return context(scene);
        }
        return KnowledgeBaseContext.INSTANCE.context();
    }
    /**
     * Retrieves the KlContext associated with the given scene. If the scene contains
     * properties with a key corresponding to KL_CONTEXT, the associated KlContext
     * is returned. If not, the method attempts to derive the context from the
     * window associated with the scene. If neither is applicable, it falls back
     * to the default KnowledgeBaseContext.
     *
     * @param scene the Scene from which the KlContext is to be retrieved
     * @return the KlContext associated with the provided Scene, derived from its
     * properties, associated window, or the default KnowledgeBaseContext if unavailable
     */
    static KlContext context(Scene scene) {
        if (scene.hasProperties() && scene.getProperties().containsKey(KL_CONTEXT)) {
            return (KlContext) scene.getProperties().get(KL_CONTEXT);
        }
        if (scene.getWindow() != null) {
            return context(scene.getWindow());
        }
        return KnowledgeBaseContext.INSTANCE.context();
    }

    /**
     * Finds and collects Knowledge Layout peers within the JavaFx hierarchy based on a testing function.
     * The method applies the provided functional test to determine eligible peers and collects them into an immutable list.
     *
     * @param test a function that takes an object and returns an Optional of type {@code T} if the object meets the criteria, or an empty Optional if it doesn't.
     * @return an immutable list containing all the peers that satisfy the testing function.
     */
    default <T> ImmutableList<T> findPeers(Function<Object, Optional<T>> test) {
        MutableList<T> peers = Lists.mutable.empty();
        Window top = switch (this.fxObject()) {
            case Node node -> node.getScene().getWindow();
            case Window window -> window;
            case Scene scene -> scene.getWindow();
            default -> throw new IllegalStateException("Unexpected value: " + this.fxObject());
        };
        recursiveFindPeers(top, peers, test);
        return peers.toImmutable();
    }

    /**
     * Recursively traverses a given JavaFX object's tree, extracting and collecting peers that meet a certain condition.
     * The traversal covers various types of JavaFX objects (e.g., Window, Scene, Parent, Node) and inspects their properties.
     *
     * @param <T>       The type of elements to be collected as peers.
     * @param fxObject  The root object to start the recursive search from, which can be a Window, Scene, Parent, or Node.
     * @param peers     A mutable list where the discovered peers will be collected.
     * @param test      A function that tests whether a specific property of the JavaFX object qualifies as a peer.
     *                  The function returns an Optional containing the peer if it matches, or an empty Optional otherwise.
     */
    default <T> void recursiveFindPeers(Object fxObject, MutableList<T> peers, Function<Object,Optional<T>> test) {
        switch (fxObject) {
            case Window window -> {
                if (window.hasProperties()) {
                    Object gadget = window.getProperties().get(PropertyKeys.FX_PEER);
                    test.apply(gadget).ifPresent(peers::add);
                }
                recursiveFindPeers(window.getScene(), peers, test);
            }
            case Scene scene -> {
                if (scene.hasProperties()) {
                    Object gadget = scene.getProperties().get(PropertyKeys.FX_PEER);
                    test.apply(gadget).ifPresent(peers::add);
                }
                recursiveFindPeers(scene.getRoot(), peers, test);
            }
            case Parent parent -> {
                if (parent.hasProperties()) {
                    Object gadget = parent.getProperties().get(PropertyKeys.FX_PEER);
                    test.apply(gadget).ifPresent(peers::add);
                }
                for (Node node : parent.getChildrenUnmodifiable()) {
                    recursiveFindPeers(node, peers, test);
                }
            }
            case Node node -> {
                if (node.hasProperties()) {
                    Object gadget = node.getProperties().get(PropertyKeys.FX_PEER);
                    test.apply(gadget).ifPresent(peers::add);
                }
            }
            default -> throw new IllegalStateException("Unexpected value: " + fxObject);
        }
    }

    /**
     * Performs a depth-first search (DFS) processing of KlGadgets, using the hierarchy of Window,
     * Scene, and scene graph. The DFS is started at the location of this {@code KlGadget} within
     * the hierarchy, processes all descendents, and applies the given {@link Consumer} action to
     * each KlGadget encountered. Parent {@code KlGadget} objects are not processed by this method.
     *
     * @param action the {@link Consumer} to be applied to each KlGadget during the DFS traversal
     */
    default void dfsProcessKlView(Consumer<KlView<?>> action) {
        switch (this) {
            case KlFxWindow windowView -> dfsProcessNodesWithKlPeer(windowView.getFxPeer(), action);
            case KlJournalWindow journalView -> dfsProcessNodesWithKlPeer(journalView.getFxPeer(), action);
            case KlRenderView klRenderView -> dfsProcessNodesWithKlPeer(klRenderView.fxObject().getRoot(), action);
            case KlArea<?> area -> dfsProcessNodesWithKlPeer(area.fxObject(), action);
        }
    };

    /**
     * Performs a depth-first search (DFS) traversal of the given node and its children,
     * processing any nodes that contain the KlPeer key using the specified action.
     *
     * @param node   The root node to start the DFS traversal from. It can contain properties
     *               and may have child nodes if it is an instance of the Parent class.
     * @param action A consumer that defines the action to apply to each KlGadget found in a {@code Node}
     *               object's properties with the {@code KL_PEER} key.
     */
    private void dfsProcessNodesWithKlPeer(Node node, Consumer<KlView<?>> action) {
        if (node.hasProperties() && node.getProperties().containsKey(PropertyKeys.KL_PEER)) {
            KlView view = (KlView) node.getProperties().get(PropertyKeys.KL_PEER);
            action.accept(view);
        }
        if (node instanceof Parent parent) {
            parent.getChildrenUnmodifiable().forEach(child -> dfsProcessNodesWithKlPeer(child, action));
        }
    }

    private void dfsProcessNodesWithKlPeer(Window window, Consumer<KlView<?>> action) {
        if (window.hasProperties() && window.getProperties().containsKey(PropertyKeys.KL_PEER)) {
            KlView view = (KlView) window.getProperties().get(PropertyKeys.KL_PEER);
            action.accept(view);
        }
        dfsProcessNodesWithKlPeer(window.getScene(), action);
    }

    private void dfsProcessNodesWithKlPeer(Scene scene, Consumer<KlView<?>> action) {
        if (scene.hasProperties() && scene.getProperties().containsKey(PropertyKeys.KL_PEER)) {
            KlView view = (KlView) scene.getProperties().get(PropertyKeys.KL_PEER);
            action.accept(view);
        }
        dfsProcessNodesWithKlPeer(scene.getRoot(), action);
    }



    /**
     * Recursively traverses a given JavaFX object's tree, extracting and collecting peers that meet a certain condition.
     * The traversal covers various types of JavaFX objects (e.g., Window, Scene, Parent, Node) and inspects their properties.
     *
     * @param <T>       The type of elements to be collected as peers.
     * @param klView  The root object to start the recursive search from, which can be a Window, Scene, Parent, or Node.
     * @param peers     A mutable list where the discovered peers will be collected.
     * @param test      A function that tests whether a specific property of the JavaFX object qualifies as a peer.
     *                  The function returns an Optional containing the peer if it matches, or an empty Optional otherwise.
     */
    default <T> void recursiveDfsGadgets(KlView klView, MutableList<T> peers, Function<Object,Optional<T>> test) {
        switch (klView) {
            case Window window -> {
                if (window.hasProperties()) {
                    Object gadget = window.getProperties().get(PropertyKeys.FX_PEER);
                    test.apply(gadget).ifPresent(peers::add);
                }
                recursiveFindPeers(window.getScene(), peers, test);
            }
            case Scene scene -> {
                if (scene.hasProperties()) {
                    Object gadget = scene.getProperties().get(PropertyKeys.FX_PEER);
                    test.apply(gadget).ifPresent(peers::add);
                }
                recursiveFindPeers(scene.getRoot(), peers, test);
            }
            case Parent parent -> {
                if (parent.hasProperties()) {
                    Object gadget = parent.getProperties().get(PropertyKeys.FX_PEER);
                    test.apply(gadget).ifPresent(peers::add);
                }
                for (Node node : parent.getChildrenUnmodifiable()) {
                    recursiveFindPeers(node, peers, test);
                }
            }
            case Node node -> {
                if (node.hasProperties()) {
                    Object gadget = node.getProperties().get(PropertyKeys.FX_PEER);
                    test.apply(gadget).ifPresent(peers::add);
                }
            }
            default -> throw new IllegalStateException("Unexpected value: " + klView);
        }
    }

    default Future<Void> backgroundSave() {
        return (Future<Void>) TinkExecutor.ioThreadPool().submit(
                () -> this.save()
        );
    }

    void save();

    static KlView<?> restoreWithChildren(KometPreferences preferences) {
        MutableList<KlView<?>> restoredViewList = Lists.mutable.ofInitialCapacity(16);
        KlView<?> restoredView = restore(preferences);
        restoredViewList.add(restoredView);

        restoreWithChildren(restoredView, preferences, restoredViewList);

        restoredViewList.forEach(klView -> klView.restoreFromPreferencesOrDefaults());
        restoredViewList.forEach(klView -> klView.knowledgeLayoutBind());
        return restoredView;
    }

    static void restoreWithChildren(KlView<?> parentView, KometPreferences preferences, MutableList<KlView<?>> viewList) {
        try {
            if (preferences.hasChildren()) {
                for (KometPreferences childPreferences: preferences.children()) {
                    KlView<?> restoredChild = restore(childPreferences);
                    viewList.add(restoredChild);
                    parentView.addChild(restoredChild);
                    restoreWithChildren(restoredChild, childPreferences, viewList);
                }
            }
        } catch (BackingStoreException e) {
            throw new RuntimeException(e);
        }
    }

    /**
     * Restores a {@code KlView} instance using the provided {@code KometPreferences}.
     * This method attempts to load a factory class from the preferences, instantiate it,
     * and then invoke its restore method to reconstruct the {@code KlGadget}.
     *
     * @param preferences the {@code KometPreferences} from which to restore the {@code KlGadget}.
     *                     Must contain the class name of the factory under the key {@code FACTORY_CLASS}.
     * @return the restored {@code KlGadget} instance of type {@code G}.
     * @throws IllegalStateException if the key {@code FACTORY_CLASS} does not exist in the preferences.
     * @throws RuntimeException if any error occurs during the instantiation and execution of the factory class.
     */
    static <KL extends KlView> KL restore(KometPreferences preferences) {
        try {
            LOG.info("Restoring: " + preferences.name() + ": " + preferences.getMap());
            Optional<String> optionalFactoryClassName = preferences.get(FACTORY_CLASS_NAME);
            if (optionalFactoryClassName.isPresent()) {
                Class<KlView.Factory> factoryClass = (Class<KlView.Factory>) PluggableService.forName(optionalFactoryClassName.get());
                KlView.Factory klFactory = factoryClass.getDeclaredConstructor().newInstance();
                return (KL) klFactory.restore(preferences);
            } else {
                throw new IllegalStateException("FACTORY_CLASS not found in child preferences.");
            }
        } catch (InstantiationException | IllegalAccessException | InvocationTargetException | NoSuchMethodException |
                 ClassNotFoundException | BackingStoreException e) {
            throw new RuntimeException(e);
        }
    }

    sealed interface Factory<FX, KL extends KlView<FX>> extends KlPeerable.Factory<FX, KL>
            permits KlArea.Factory, KlTopView.Factory, KlRenderView.Factory {
    }
}
