package dev.ikm.komet.layout;

import static dev.ikm.komet.layout.KlObject.PropertyKeys.KL_CONTEXT;
import dev.ikm.komet.framework.view.ObservableView;
import dev.ikm.komet.layout.context.KlContext;
import dev.ikm.komet.layout.context.KlContextProvider;
import dev.ikm.komet.layout.context.KnowledgeBaseContext;
import javafx.scene.Node;
import javafx.scene.Scene;
import javafx.scene.layout.BorderPane;
import javafx.stage.Window;

/**
 * KlView is an interface that extends KlWidget specifically to provide a node to manage contextual
 * view coordinates. All descendent KlWidget items will inherit the view coordinate from a parent KlView.
 */
public non-sealed interface KlView extends KlGadget<BorderPane>, KlContextProvider {

    /**
     * Retrieves the {@code ObservableView} for the current context.
     *
     * @return the {@code ObservableView} associated with the view coordinate of the current context.
     */
    default ObservableView viewForContext() {
        return context().viewCoordinate();
    }

    /**
     * Retrieves the associated {@code KlContext} for this view or layout element.
     * A {@code KlContext} represents contextual information including coordinates wrapped
     * into a view coordinate.
     *
     * @return the {@code KlContext} associated with this view.
     */
    KlContext context();

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
}
