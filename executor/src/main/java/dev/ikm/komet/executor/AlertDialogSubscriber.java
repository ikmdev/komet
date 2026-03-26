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
package dev.ikm.komet.executor;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import dev.ikm.komet.framework.Dialogs;
import dev.ikm.tinkar.common.alert.AlertObject;
import dev.ikm.tinkar.common.alert.AlertReportingService;
import dev.ikm.tinkar.common.alert.AlertStreams;
import dev.ikm.tinkar.common.id.PublicIdStringKey;
import dev.ikm.tinkar.common.util.broadcast.Broadcaster;
import javafx.application.Platform;

import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.concurrent.atomic.AtomicBoolean;

/**
 * Presents dialogs for alerts.
 * <p>Alerts are queued and shown one at a time to prevent infinite nesting:
 * {@code showAndWait()} enters a nested JavaFX event loop that would
 * otherwise process the next queued {@code Platform.runLater} alert,
 * causing unbounded stack growth.
 */
public class AlertDialogSubscriber implements AlertReportingService {
    private static final Logger LOG = LoggerFactory.getLogger(AlertDialogSubscriber.class);

    private final ConcurrentLinkedQueue<AlertObject> alertQueue = new ConcurrentLinkedQueue<>();
    private final AtomicBoolean showing = new AtomicBoolean(false);

    public AlertDialogSubscriber() {
        this(AlertStreams.ROOT_ALERT_STREAM_KEY);
    }

    public AlertDialogSubscriber(PublicIdStringKey<Broadcaster<AlertObject>> alertStreamKey) {
        LOG.info("Constructing AlertDialogSubscriber");
        AlertStreams.get(alertStreamKey).addSubscriberWithWeakReference(this);
    }

    @Override
    public void onNext(AlertObject item) {
        alertQueue.add(item);
        Platform.runLater(this::drainQueue);
    }

    private void drainQueue() {
        if (!showing.compareAndSet(false, true)) {
            return;
        }
        try {
            AlertObject item;
            while ((item = alertQueue.poll()) != null) {
                Dialogs.showDialogForAlert(item);
            }
        } finally {
            showing.set(false);
        }
    }
}
