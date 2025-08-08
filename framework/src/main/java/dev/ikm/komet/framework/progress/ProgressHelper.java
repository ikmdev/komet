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
package dev.ikm.komet.framework.progress;

import static dev.ikm.tinkar.events.FrameworkTopics.PROGRESS_TOPIC;
import dev.ikm.komet.framework.concurrent.TaskWrapper;
import dev.ikm.tinkar.events.EvtBusFactory;
import dev.ikm.komet.framework.events.appevents.ProgressEvent;
import dev.ikm.tinkar.common.service.TinkExecutor;
import dev.ikm.tinkar.common.service.TrackingCallable;
import javafx.concurrent.Task;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Future;

/**
 * Wraps task or TrackingCallable instances to be messaged to the journal window's progress popup.
 */
public class ProgressHelper {
    private static final Logger LOG = LoggerFactory.getLogger(ProgressHelper.class);
    private ProgressHelper() {}

    /**
     * Wraps TrackingCallable instance to be messaged to the journal window's progress popup with default cancel button text. Note: It will execute on caller's behalf.
     * @param task A TrackingCallable instance
     * @return A CompletableFuture or an asynchronous call.
     */
    public static <T> CompletableFuture<T> progress(TrackingCallable<T> task) {
        return progress(task, "Cancel");
    }

    /**
     * Wraps TrackingCallable instance to be messaged to the journal window's progress popup with default cancel button text. Note: It will execute on caller's behalf.
     * @param topic The topic to publish the progress event to.
     * @param task A TrackingCallable instance
     * @return A CompletableFuture or an asynchronous call.
     */
    public static <T> CompletableFuture<T> progress(Object topic, TrackingCallable<T> task) {
        return progress(topic, task, "Cancel");
    }

    /**
     * Wraps TrackingCallable instance to be messaged to the journal window's progress popup. Note: It will execute on caller's behalf.
     * @param task A TrackingCallable instance
     * @param cancelButtonText the cancel button's text
     * @return A CompletableFuture or an asynchronous call.
     */
    public static <T> CompletableFuture<T> progress(TrackingCallable<T> task, String cancelButtonText) {
        TaskWrapper<T> javafxTask = TaskWrapper.make(task);
        return progress(javafxTask, cancelButtonText);
    }

    /**
     * Wraps TrackingCallable instance to be messaged to the journal window's progress popup. Note: It will execute on caller's behalf.
     * @param topic The topic to publish the progress event to.
     * @param task A TrackingCallable instance
     * @param cancelButtonText the cancel button's text
     * @return A CompletableFuture or an asynchronous call.
     */
    public static <T> CompletableFuture<T> progress(Object topic, TrackingCallable<T> task, String cancelButtonText) {
        TaskWrapper<T> javafxTask = TaskWrapper.make(task);
        return progress(topic, javafxTask, cancelButtonText);
    }

    /**
     * A known JavaFX task to be messaged to the journal window's progress popup. Note: It will execute on caller's behalf.
     * @param task A TrackingCallable instance
     * @param cancelButtonText the cancel button's text
     * @return A CompletableFuture or an asynchronous call.
     */
    public static <T> CompletableFuture<T> progress(Task<T> task, String cancelButtonText) {
        EvtBusFactory
                .getDefaultEvtBus()
                .publish(PROGRESS_TOPIC, new ProgressEvent(task, ProgressEvent.SUMMON, task, cancelButtonText));
        Future future = TinkExecutor.threadPool().submit(task);
        return wrap(future, TinkExecutor.threadPool());
    }

    /**
     * A known JavaFX task to be messaged to the journal window's progress popup. Note: It will execute on caller's behalf.
     * @param topic The topic to publish the progress event to.
     * @param task A TrackingCallable instance
     * @param cancelButtonText the cancel button's text
     * @return A CompletableFuture or an asynchronous call.
     */
    public static <T> CompletableFuture<T> progress(Object topic, Task<T> task, String cancelButtonText) {
        EvtBusFactory
                .getDefaultEvtBus()
                .publish(topic, new ProgressEvent(task, ProgressEvent.SUMMON, task, cancelButtonText));
        Future future = TinkExecutor.threadPool().submit(task);
        return wrap(future, TinkExecutor.threadPool());
    }

    /**
     * Returns a CompletableFuture wrap the caller's future. This allows the caller to provide additional steps when complete.
     * @param future The future to wrap.
     * @param executor The executor (thread pool) to submit task.
     * @return CompletableFuture wrap the caller's future. This allows the caller to provide additional steps when complete
     * @param <T> The Future's return type.
     */
    public static <T> CompletableFuture<T> wrap(Future<T> future, ExecutorService executor) {
        CompletableFuture<T> completableFuture = new CompletableFuture<>();

        executor.submit(() -> {
            try {
                completableFuture.complete(future.get());
            } catch (Exception e) {
                completableFuture.completeExceptionally(e);
            }
        });
        return completableFuture;
    }

    /**
     * Notifies other progress sections referencing the same task to cancel.
     * @param task JavaFX task to be cancelled.
     */
    public static <T> void cancel(Task<T> task) {
        EvtBusFactory.getDefaultEvtBus()
                .publish(PROGRESS_TOPIC, new ProgressEvent(task, ProgressEvent.CANCEL, task));
    }
}
