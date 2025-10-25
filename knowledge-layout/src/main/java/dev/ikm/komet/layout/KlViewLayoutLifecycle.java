package dev.ikm.komet.layout;

/**
 * Defines lifecycle hooks for {@link dev.ikm.komet.layout.KlArea} components participating in a layout.
 * <p>
 * Implementations of this interface should manage resource binding and cleanup for layout participation.
 * This enables layout managers to perform atomic, batched updates and prevent redundant event binding.
 * </p>
 *
 * <p>
 * Method details:
 * <ul>
 *     <li>
 *         <b>knowledgeLayoutUnbind()</b> – Invoked <em>before</em> the area is removed from or updated within the layout.
 *         Use this for cleanup operations such as unbinding listeners and releasing resources.
 *     </li>
 *     <li>
 *         <b>knowledgeLayoutBind()</b> – Invoked <em>after</em> the area has been added to or reconfigured in the layout.
 *         Use this to allocate resources and establish necessary event or data bindings.
 *     </li>
 * </ul>
 * </p>
 *
 * <p>
 * Example implementation:
 * <pre>
 * public class MyArea implements KlAreaLayoutLifecycle {
 *     &#64;Override
 *     public void knowledgeLayoutUnbind() {
 *         // Remove listeners and clean up resources
 *     }
 *     &#64;Override
 *     public void knowledgeLayoutBind() {
 *         // Re-establish listeners and resources
 *     }
 * }
 * </pre>
 * </p>
 */
public interface KlViewLayoutLifecycle {
    /**
     * Called before the implementing area is removed from or updated in the layout context.
     * <p>
     * Use this method to perform cleanup, such as releasing resources or unbinding from event sources.
     * </p>
     */
    void knowledgeLayoutUnbind();

    /**
     * Called after the implementing area is added to or reconfigured within the layout context.
     * <p>
     * Use this method to initialize or restore state, establish event/data bindings, or allocate resources
     * necessary for proper operation in its new layout context.
     * </p>
     */
    void knowledgeLayoutBind();
}