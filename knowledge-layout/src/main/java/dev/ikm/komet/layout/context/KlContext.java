package dev.ikm.komet.layout.context;

import dev.ikm.komet.framework.view.ObservableView;
import dev.ikm.komet.layout.KlGadget;
import dev.ikm.komet.layout.KlObject;
import dev.ikm.komet.layout.preferences.PropertyWithDefault;
import dev.ikm.tinkar.common.id.PublicIdStringKey;
import dev.ikm.tinkar.coordinate.Coordinates;
import javafx.scene.Node;
import org.eclipse.collections.api.list.ImmutableList;

import java.util.Optional;

import static dev.ikm.tinkar.common.util.uuid.UuidUtil.NIL_UUID;

/**
 * Represents a context that can be used for layout orchestration and user interface configuration.
 * A context contains an identifier, an optional graphical representation, a view coordinate,
 * and may have a parent context.
 */
public interface KlContext {
    enum PreferenceKeys implements PropertyWithDefault {
        CONTEXT_NAME("Unnamed Context"),
        CONTEXT_UUID(NIL_UUID),
        VIEW_COORDINATE(Coordinates.View.DefaultView())
            ;

        Object defaultValue;

        PreferenceKeys(Object defaultValue) {
            this.defaultValue = defaultValue;
        }

        @Override
        public Object defaultValue() {
            return this.defaultValue;
        }
    }

    /**
    TODO: The generic in the PublicIdStringKey can be associated with a membership
    pattern... Enabling both java and terminology constraints to align.
     */
    PublicIdStringKey<KlContext> contextId();

    /**
     * Retrieves the name of the context associated with this instance.
     * The name is derived from the string representation of the context's identifier.
     *
     * @return a string representing the name of the context
     */
    default String name() {
        return contextId().getString();
    }

    /**
     * Retrieves the peer {@code KlObject} associated with the current {@code KlContext}.
     * The peer represents a corresponding object linked within the layout or context
     * system, providing additional functionality, behavior, or relationships to
     * the current context.
     *
     * @return the {@code KlObject} peer associated with this {@code KlContext}.
     */
    KlObject klPeer();


    /*
    Graphic could also be associated with a concept via a pattern/semantic.
     */
    Optional<Node> graphic();

    /**
     * Retrieves the view coordinate associated with the context.
     *
     * @return an {@link ObservableView} instance representing the view coordinate
     *         for managing and observing coordinate states within the layout context.
     */
    ObservableView viewCoordinate();

    /**
     * This method will signal all dependent {@code KlGadget} objects to unsubscribe from any KlContext properties.
     * The invalidation signal is performed by calling {@code KlGadget.unsubscribeFromContext()} for subordinate
     * {@code KlGadget}s. This method signals that the JavaFx object hierarchy (and {@code KlContext} objects
     * within that node hierarchy is about to change. {@code KlGadget} objects should call
     * {@code Subscription.unsubscribe()} in response to this signal. {@code KlContext.unsubscribeDependentContexts}
     * should be called prior to {@code KlGadget} deletion or insertion of a new {@code KlContext} into
     * JavaFx object hierarchy.
     * <p>
     * The existing JavaFx Node tree must be retained before calling this method, as the Node tree is
     * how the dependent contexts are identified.
     */
    void unsubscribeDependentContexts();

    /**
     * Signals dependent {@code KlGadget} objects to subscribe to the current {@code KlContext} properties.
     * The signal is performed by calling {@code KlGadget.subscribeToContext()} on subordinate
     * {@code KlGadget}s.
     * This method is intended to establish or re-establish the necessary subscriptions to {@code KlContext} properties,
     * and update any content-dependent content within the {@code KlGadget} objects.
     * These dependencies may involve dynamic subscriptions to property
     * changes or event-based interactions among contexts. Implementation is determined by specific
     * dependency requirements of the {@code KlContext} hierarchy.
     * <p>
     * This method must be called after the JavaFX Node tree has been constructed to ensure
     * proper identification and subscription of dependent contexts. Calling this method at the appropriate
     * time ensures that dependents are initialized and ready for subsequent updates or state changes in
     * the current context.
     * <p>
     * A complete JavaFx Object (Node, Scene, Window, and Stage) tree must be in place before calling this method, as
     * this Node tree is how the dependent {@code KlGadget} objects are identified.
     */
    void subscribeDependentContexts();


}
