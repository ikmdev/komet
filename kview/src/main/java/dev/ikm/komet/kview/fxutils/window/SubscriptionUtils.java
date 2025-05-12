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
package dev.ikm.komet.kview.fxutils.window;

import javafx.beans.InvalidationListener;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.collections.ObservableList;
import javafx.event.Event;
import javafx.event.EventHandler;
import javafx.event.EventType;
import javafx.scene.Node;
import javafx.util.Subscription;

import java.util.ArrayList;
import java.util.List;
import java.util.function.BiConsumer;
import java.util.function.Consumer;

/**
 * Utility class providing methods for creating JavaFX subscriptions.
 * <p>
 * This class encapsulates common patterns for working with JavaFX event handlers,
 * property listeners, and other observable sources using the Subscription pattern
 * for resource management.
 */
public interface SubscriptionUtils {

    /**
     * Creates a subscription by applying a registration function and returning
     * an unregistration function wrapped as a Subscription.
     *
     * @param register The function that registers the listener/handler
     * @param unregister The function that unregisters the listener/handler
     * @return A subscription that calls the unregister function when unsubscribed
     */
    static Subscription createSubscription(Runnable register, Runnable unregister) {
        register.run();
        return unregister::run;
    }

    /**
     * Creates a subscription for JavaFX event handlers.
     *
     * @param <T> The type of event
     * @param node The node to attach the handler to
     * @param eventType The type of event to handle
     * @param handler The event handler to register
     * @return A subscription that cleans up the handler when unsubscribed
     */
    static <T extends Event> Subscription createEventSubscription(Node node,
                                                                         EventType<T> eventType,
                                                                         EventHandler<T> handler) {
        return createSubscription(
                () -> node.addEventHandler(eventType, handler),
                () -> node.removeEventHandler(eventType, handler)
        );
    }

    /**
     * Creates a subscription for JavaFX event handlers using a consumer function.
     * Useful for simpler handlers that don't need additional context.
     *
     * @param <T> The type of event
     * @param node The node to attach the handler to
     * @param eventType The type of event to handle
     * @param handler The consumer function to handle the event
     * @return A subscription that cleans up the handler when unsubscribed
     */
    static <T extends Event> Subscription createConsumerSubscription(
            Node node,
            EventType<T> eventType,
            Consumer<T> handler) {
        EventHandler<T> eventHandler = handler::accept;
        return createSubscription(
                () -> node.addEventHandler(eventType, eventHandler),
                () -> node.removeEventHandler(eventType, eventHandler)
        );
    }

    /**
     * Creates a subscription for context-aware JavaFX event handlers.
     * Automatically passes the specified context to the handler.
     *
     * @param <T> The type of event
     * @param <C> The type of context
     * @param node The node to attach the handler to
     * @param eventType The type of event to handle
     * @param context The context object to pass to the handler
     * @param handler The BiConsumer function that will receive both the event and context
     * @return A subscription that cleans up the handler when unsubscribed
     */
    static <T extends Event, C> Subscription createContextEventSubscription(
            Node node,
            EventType<T> eventType,
            C context,
            BiConsumer<T, C> handler) {
        EventHandler<T> eventHandler = event -> handler.accept(event, context);
        return createSubscription(
                () -> node.addEventHandler(eventType, eventHandler),
                () -> node.removeEventHandler(eventType, eventHandler)
        );
    }

    /**
     * Creates a subscription for a JavaFX property invalidation listener.
     *
     * @param <T> The type of the property value
     * @param property The observable property to listen to
     * @param listener The invalidation listener to register
     * @return A subscription that unregisters the listener when unsubscribed
     */
    static <T> Subscription createPropertySubscription(ObservableValue<T> property,
                                                              InvalidationListener listener) {
        return createSubscription(
                () -> property.addListener(listener),
                () -> property.removeListener(listener)
        );
    }

    /**
     * Creates a subscription for a JavaFX property change listener.
     *
     * @param <T> The type of the property value
     * @param property The observable property to listen to
     * @param listener The change listener to register
     * @return A subscription that unregisters the listener when unsubscribed
     */
    static <T> Subscription createPropertyChangeSubscription(ObservableValue<T> property,
                                                                    ChangeListener<T> listener) {
        return createSubscription(
                () -> property.addListener(listener),
                () -> property.removeListener(listener)
        );
    }

    /**
     * Creates a subscription for a JavaFX observable list.
     *
     * @param <E> The type of elements in the list
     * @param list The observable list to observe
     * @param listener The invalidation listener to register
     * @return A subscription that unregisters the listener when unsubscribed
     */
    static <E> Subscription createListSubscription(
            ObservableList<E> list,
            InvalidationListener listener) {
        return createSubscription(
                () -> list.addListener(listener),
                () -> list.removeListener(listener)
        );
    }

    /**
     * Creates a subscription for multiple JavaFX properties, applying the same invalidation listener to all of them.
     * This is useful when the same action should be triggered by changes to any of several properties.
     *
     * @param listener The invalidation listener to register with all properties
     * @param properties The observable properties to listen to
     * @return A subscription that unregisters the listener from all properties when unsubscribed
     */
    static Subscription createMultiPropertySubscription(
            InvalidationListener listener,
            ObservableValue<?>... properties) {
        List<Subscription> subscriptions = new ArrayList<>();
        for (ObservableValue<?> property : properties) {
            subscriptions.add(createPropertySubscription(property, listener));
        }
        return Subscription.combine(subscriptions.toArray(Subscription[]::new));
    }

    /**
     * Creates a subscription that triggers a cleanup action when a node is detached from the scene graph.
     * <p>
     * This is particularly useful for releasing resources associated with a node when it's no longer
     * visible or active in the application.
     *
     * @param node The node to monitor for scene detachment
     * @param cleanupAction The action to execute when the node is detached from the scene
     * @return A subscription that can be used to stop monitoring the node
     */
    static Subscription createSceneDetachmentSubscription(Node node, Runnable cleanupAction) {
        return createPropertyChangeSubscription(
                node.sceneProperty(),
                (_, oldScene, newScene) -> {
                    if (oldScene != null && newScene == null) {
                        cleanupAction.run();
                    }
                }
        );
    }
}
