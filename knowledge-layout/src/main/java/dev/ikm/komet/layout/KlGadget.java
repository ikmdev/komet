package dev.ikm.komet.layout;

import dev.ikm.komet.framework.view.ObservableView;
import dev.ikm.komet.layout.context.KlContext;
import dev.ikm.komet.layout.context.KnowledgeBaseContext;
import dev.ikm.komet.layout.window.KlFxWindow;
import dev.ikm.komet.layout.window.KlJournalWindow;
import dev.ikm.komet.layout.window.KlWindowPane;
import dev.ikm.komet.preferences.KometPreferences;
import dev.ikm.tinkar.common.service.PluggableService;
import javafx.collections.ObservableMap;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Window;
import org.eclipse.collections.api.factory.Lists;
import org.eclipse.collections.api.list.ImmutableList;
import org.eclipse.collections.api.list.MutableList;

import java.lang.reflect.InvocationTargetException;
import java.util.Optional;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.prefs.BackingStoreException;

import static dev.ikm.komet.layout.KlObject.PreferenceKeys.FACTORY_CLASS;
import static dev.ikm.komet.layout.KlObject.PropertyKeys.*;

/**
 * Represents an interface for a knowledge layout gadget with functionality for managing
 * properties, preferences, and view calculations associated with JavaFX components.
 * This interface provides methods and enumerations to define property keys, user preferences,
 * and mechanisms for retrieving contextual instances of {@code ViewCalculator}.
 *
 * @param <T> the type of the JavaFX component associated with this gadget
 */
public sealed interface KlGadget<T> extends KlContextSensitiveComponent, KlObject
        permits KlView, KlWidget, KlFxWindow, KlJournalWindow, KlWindowPane {

    /**
     * Provides an instance of the generic type T JavaFx gadget associated with the knowledge layout component.
     *
     * @return an instance of type T, representing a specific knowledge layout gadget.
     */
    T fxGadget();

    @Override
    default ObservableMap<Object, Object> properties() {
        return switch (this) {
            case KlView view -> view.fxGadget().getProperties();
            case KlWidget widget -> widget.fxGadget().getProperties();
            case KlFxWindow window -> window.fxGadget().getProperties();
            case KlJournalWindow journalWindow -> throw new UnsupportedOperationException();
            case KlWindowPane windowPane -> windowPane.fxGadget().getProperties();
        };
    }

    @Override
    default boolean hasProperties() {
        return switch (this) {
            case KlView view -> view.fxGadget().hasProperties();
            case KlWidget widget -> widget.fxGadget().hasProperties();
            case KlFxWindow window -> window.fxGadget().hasProperties();
            case KlJournalWindow journalWindow -> throw new UnsupportedOperationException();
            case KlWindowPane windowPane -> windowPane.fxGadget().hasProperties();
        };
    }

    @Override
    default boolean hasProperty(Object key) {
        if (hasProperties()) {
            return switch (this) {
                case KlView view -> view.fxGadget().getProperties().containsKey(key);
                case KlWidget widget -> widget.fxGadget().getProperties().containsKey(key);
                case KlFxWindow window -> window.fxGadget().getProperties().containsKey(key);
                case KlJournalWindow journalWindow -> throw new UnsupportedOperationException();
                case KlWindowPane windowPane -> windowPane.fxGadget().getProperties().containsKey(key);
            };
        }
        return false;
    }

    /**
     * Retrieves an immutable list of KlContext objects associated with the instance.
     * The method populates the list by recursively adding contexts from the fxGadget.
     *
     * @return an immutable list of KlContext objects
     */
    default ImmutableList<KlContext> contexts() {
        MutableList<KlContext> contexts = Lists.mutable.empty();
        recursiveAddContexts(this.fxGadget(), contexts);
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
            case KlGadget<?> gadget -> {
                switch (gadget) {
                    case KlView view -> {
                        if ((view.fxGadget().getParent() != null)) {
                            recursiveAddContexts(view.fxGadget().getParent(), contexts);
                        } else {
                            recursiveAddContexts(view.fxGadget().getScene(), contexts);
                        }
                    }
                    case KlWidget widget -> recursiveAddContexts(widget.fxGadget().getParent(), contexts);
                    case KlFxWindow ignored -> contexts.add(KnowledgeBaseContext.INSTANCE.context());
                    case KlJournalWindow ignored ->contexts.add(KnowledgeBaseContext.INSTANCE.context());
                    case KlWindowPane windowPane -> {
                        if ((windowPane.fxGadget().getParent() != null)) {
                            recursiveAddContexts(windowPane.fxGadget().getParent(), contexts);
                        } else {
                            recursiveAddContexts(windowPane.fxGadget().getScene(), contexts);
                        }
                    }
                };
            }
            case KlKnowledgeBaseContext ignored -> {}
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
        return  switch (this.fxGadget()) {
            case Node node -> context(node);
            case Window window -> context(window);
            case Scene scene -> context(scene);
            default -> KnowledgeBaseContext.INSTANCE.context();
        };
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
        Window top = switch (this.fxGadget()) {
            case Node node -> node.getScene().getWindow();
            case Window window -> window;
            case Scene scene -> scene.getWindow();
            default -> throw new IllegalStateException("Unexpected value: " + this.fxGadget());
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
                    Object gadget = window.getProperties().get(KlObject.PropertyKeys.KL_PEER);
                    test.apply(gadget).ifPresent(peers::add);
                }
                recursiveFindPeers(window.getScene(), peers, test);
            }
            case Scene scene -> {
                if (scene.hasProperties()) {
                    Object gadget = scene.getProperties().get(KlObject.PropertyKeys.KL_PEER);
                    test.apply(gadget).ifPresent(peers::add);
                }
                recursiveFindPeers(scene.getRoot(), peers, test);
            }
            case Parent parent -> {
                if (parent.hasProperties()) {
                    Object gadget = parent.getProperties().get(KlObject.PropertyKeys.KL_PEER);
                    test.apply(gadget).ifPresent(peers::add);
                }
                for (Node node : parent.getChildrenUnmodifiable()) {
                    recursiveFindPeers(node, peers, test);
                }
            }
            case Node node -> {
                if (node.hasProperties()) {
                    Object gadget = node.getProperties().get(KlObject.PropertyKeys.KL_PEER);
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
    default void dfsProcessKlGadgets(Consumer<KlGadget<?>> action) {
        switch (this) {
            case KlFxWindow klWindow -> {
                action.accept(klWindow);
                Window fxWindow = klWindow.fxGadget();
                dfsProcessNodesWithKlPeer(fxWindow.getScene().getRoot(), action);
            }
            case KlJournalWindow klJournalWindow -> {
                action.accept(klJournalWindow);
                Node journalWindow = (Node) klJournalWindow.fxGadget();
                dfsProcessNodesWithKlPeer(journalWindow.getScene().getRoot(), action);
            }
            case KlView view -> {
                action.accept(view);
                dfsProcessNodesWithKlPeer(view.fxGadget(), action);
            }
            case KlWindowPane windowPane -> {
                action.accept(windowPane);
                dfsProcessNodesWithKlPeer(windowPane.fxGadget(), action);
            }
            case KlWidget widget -> {
                action.accept(widget);
                dfsProcessNodesWithKlPeer(widget.fxGadget(), action);
            }
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
    private void dfsProcessNodesWithKlPeer(Node node, Consumer<KlGadget<?>> action) {
        if (node.hasProperties() && node.getProperties().containsKey(KlObject.PropertyKeys.KL_PEER)) {
            KlGadget gadget = (KlGadget) node.getProperties().get(KlObject.PropertyKeys.KL_PEER);
            action.accept(gadget);
        }
        if (node instanceof Parent parent) {
            parent.getChildrenUnmodifiable().forEach(child -> dfsProcessNodesWithKlPeer(child, action));
        }
    }

    /**
     * Recursively traverses a given JavaFX object's tree, extracting and collecting peers that meet a certain condition.
     * The traversal covers various types of JavaFX objects (e.g., Window, Scene, Parent, Node) and inspects their properties.
     *
     * @param <T>       The type of elements to be collected as peers.
     * @param klGadget  The root object to start the recursive search from, which can be a Window, Scene, Parent, or Node.
     * @param peers     A mutable list where the discovered peers will be collected.
     * @param test      A function that tests whether a specific property of the JavaFX object qualifies as a peer.
     *                  The function returns an Optional containing the peer if it matches, or an empty Optional otherwise.
     */
    default <T> void recursiveDfsGadgets(KlGadget klGadget, MutableList<T> peers, Function<Object,Optional<T>> test) {
        switch (klGadget) {
            case Window window -> {
                if (window.hasProperties()) {
                    Object gadget = window.getProperties().get(KlObject.PropertyKeys.KL_PEER);
                    test.apply(gadget).ifPresent(peers::add);
                }
                recursiveFindPeers(window.getScene(), peers, test);
            }
            case Scene scene -> {
                if (scene.hasProperties()) {
                    Object gadget = scene.getProperties().get(KlObject.PropertyKeys.KL_PEER);
                    test.apply(gadget).ifPresent(peers::add);
                }
                recursiveFindPeers(scene.getRoot(), peers, test);
            }
            case Parent parent -> {
                if (parent.hasProperties()) {
                    Object gadget = parent.getProperties().get(KlObject.PropertyKeys.KL_PEER);
                    test.apply(gadget).ifPresent(peers::add);
                }
                for (Node node : parent.getChildrenUnmodifiable()) {
                    recursiveFindPeers(node, peers, test);
                }
            }
            case Node node -> {
                if (node.hasProperties()) {
                    Object gadget = node.getProperties().get(KlObject.PropertyKeys.KL_PEER);
                    test.apply(gadget).ifPresent(peers::add);
                }
            }
            default -> throw new IllegalStateException("Unexpected value: " + klGadget);
        }
    }
    /**
     * Restores a list of {@code KlGadget} instances from all child preferences of the provided
     * {@code KometPreferences}. This method iterates through all children of the given preferences,
     * restoring a {@code KlGadget} from each child and collecting the results in an immutable list.
     *
     * @param preferences the {@code KometPreferences} containing child preferences from which to restore
     *                    {@code KlGadget} instances.
     * @return an immutable list of restored {@code KlGadget} instances.
     * @throws RuntimeException if an error occurs while accessing the backing store during the restoration process.
     */
    default ImmutableList<? extends KlGadget> restoreFromAllChildren(KometPreferences preferences) {
        try {
            MutableList<? extends KlGadget> gadgets = Lists.mutable.empty();
            for (KometPreferences childPreferences: preferences.children()) {
                gadgets.add(restore(childPreferences));
            }
            return gadgets.toImmutable();
        } catch (BackingStoreException e) {
            throw new RuntimeException(e);
        }
    }
    /**
     * Restores a {@code KlGadget} instance from the only child preference of the given {@code KometPreferences}.
     * This method assumes that the provided preferences have exactly one child. If the number of child preferences
     * is not equal to one, an exception is thrown. The restored {@code KlGadget} is derived using the child preference.
     *
     * @param <G> the type of {@code KlGadget} that will be restored.
     * @param preferences the {@code KometPreferences} containing the child preference from which to restore the {@code KlGadget}.
     * @return the restored {@code KlGadget} instance of type {@code G}.
     * @throws IllegalStateException if the number of child preferences is not equal to one.
     * @throws RuntimeException if there is an error during the restoration process.
     */
    default <G extends KlGadget> G restoreFromOnlyChild(KometPreferences preferences) {
        try {
            KometPreferences[] childPreferences = preferences.children();
            if (childPreferences.length != 1) {
                throw new IllegalStateException("Expecting 1 child preference, got " + childPreferences.length + " instead.");
            }
            return restore(childPreferences[0]);
        } catch (BackingStoreException e) {
            throw new RuntimeException(e);
        }
    }

    /**
     * Restores a {@code KlGadget} instance using the provided {@code KometPreferences}.
     * This method attempts to load a factory class from the preferences, instantiate it,
     * and then invoke its restore method to reconstruct the {@code KlGadget}.
     *
     * @param preferences the {@code KometPreferences} from which to restore the {@code KlGadget}.
     *                     Must contain the class name of the factory under the key {@code FACTORY_CLASS}.
     * @return the restored {@code KlGadget} instance of type {@code G}.
     * @throws IllegalStateException if the key {@code FACTORY_CLASS} does not exist in the preferences.
     * @throws RuntimeException if any error occurs during the instantiation and execution of the factory class.
     */
    default <G extends KlGadget> G restore(KometPreferences preferences) {
        try {
            Optional<String> optionalFactoryClassName = preferences.get(FACTORY_CLASS);
            if (optionalFactoryClassName.isPresent()) {
                Class<KlFactory> factoryClass = (Class<KlFactory>) PluggableService.forName(optionalFactoryClassName.get());
                KlFactory klFactory = factoryClass.getDeclaredConstructor().newInstance();
                return (G) klFactory.restore(preferences);
            } else {
                throw new IllegalStateException("FACTORY_CLASS not found in child preferences.");
            }
        } catch (InstantiationException | IllegalAccessException | InvocationTargetException | NoSuchMethodException |
                 ClassNotFoundException e) {
            throw new RuntimeException(e);
        }
    }
}
