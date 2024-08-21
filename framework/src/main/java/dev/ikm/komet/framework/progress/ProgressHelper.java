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

import dev.ikm.komet.framework.concurrent.TaskWrapper;
import dev.ikm.komet.framework.events.EvtBusFactory;
import dev.ikm.komet.framework.events.appevents.ProgressEvent;
import dev.ikm.tinkar.common.service.TinkExecutor;
import dev.ikm.tinkar.common.service.TrackingCallable;
import javafx.concurrent.Task;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.concurrent.Future;

import static dev.ikm.komet.framework.events.FrameworkTopics.PROGRESS_TOPIC;

/**
 * Wraps task or TrackingCallable instances to be messaged to the journal window's progress popup.
 */
public class ProgressHelper {
    private static final Logger LOG = LoggerFactory.getLogger(ProgressHelper.class);
    private ProgressHelper() {}

    /**
     * Wraps TrackingCallable instance to be messaged to the journal window's progress popup. Note: It will execute on caller's behalf.
     * @param task A TrackingCallable instance
     * @param cancelButtonText the cancel button's text
     * @return A Future or an asynchronous call.
     */
    public static <T> Future<T> progress(TrackingCallable<T> task, String cancelButtonText) {
        TaskWrapper<T> javafxTask = TaskWrapper.make(task);
        return progress(javafxTask, cancelButtonText);
    }

    /**
     * A known JavaFX task to be messaged to the journal window's progress popup. Note: It will execute on caller's behalf.
     * @param task A TrackingCallable instance
     * @param cancelButtonText the cancel button's text
     * @return A Future or an asynchronous call.
     */
    public static <T> Future<T> progress(Task<T> task, String cancelButtonText) {
        EvtBusFactory
                .getDefaultEvtBus()
                .publish(PROGRESS_TOPIC, new ProgressEvent(task, ProgressEvent.SUMMON, task, cancelButtonText));
        return (Future<T>) TinkExecutor.threadPool().submit(task);
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
