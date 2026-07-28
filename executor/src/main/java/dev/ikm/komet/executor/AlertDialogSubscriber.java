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

import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.concurrent.atomic.AtomicBoolean;

/**
 * Presents dialogs for alerts.
 * <p>Alerts are queued and shown one at a time to prevent infinite nesting:
 * {@code showAndWait()} enters a nested JavaFX event loop that would
 * otherwise process the next queued {@code Platform.runLater} alert,
 * causing unbounded stack growth.
 * <p>Repeated identical alerts are <em>deduplicated</em> (ikmdev/komet#886): an alert whose
 * title, description, and cause match one whose dialog was dismissed within the last
 * {@value #DEDUPE_WINDOW_MS}&nbsp;ms is logged instead of re-dialogued. One failure that fires
 * per-node — a stylesheet hot-swap NPE-ing every CSS-styled icon in the scene, dozens of times
 * per swap — therefore produces one dialog and a counted log line, not a modal storm whose
 * nested event loops feed each other.
 */
public class AlertDialogSubscriber implements AlertReportingService {
    private static final Logger LOG = LoggerFactory.getLogger(AlertDialogSubscriber.class);

    /**
     * Suppression window (ms), measured from the dismissal of the duplicate's dialog. Long
     * enough to absorb a burst (a CSS-swap storm queues its duplicates while the first dialog
     * is open); short enough that a genuinely recurring problem resurfaces.
     */
    static final long DEDUPE_WINDOW_MS = 30_000;

    private final ConcurrentLinkedQueue<AlertObject> alertQueue = new ConcurrentLinkedQueue<>();
    private final AtomicBoolean showing = new AtomicBoolean(false);
    /** Dedupe key → the time its dialog was last dismissed. */
    private final ConcurrentHashMap<String, Long> recentlyShown = new ConcurrentHashMap<>();
    /** Dedupe key → how many duplicates were suppressed since its dialog last showed. */
    private final ConcurrentHashMap<String, Integer> suppressedCounts = new ConcurrentHashMap<>();

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
                String key = dedupeKey(item);
                long now = System.currentTimeMillis();
                Long dismissedAt = recentlyShown.get(key);
                if (dismissedAt != null && now - dismissedAt < DEDUPE_WINDOW_MS) {
                    int suppressed = suppressedCounts.merge(key, 1, Integer::sum);
                    LOG.warn("Suppressed duplicate alert ({} in the last {}s): {}",
                            suppressed, DEDUPE_WINDOW_MS / 1000, key);
                    continue;
                }
                suppressedCounts.remove(key);
                Dialogs.showDialogForAlert(item);
                // Stamp at DISMISSAL, not at show: duplicates queue while showAndWait blocks,
                // so a window anchored at show time could expire under a long-open dialog and
                // let the very storm this guards against through.
                recentlyShown.put(key, System.currentTimeMillis());
                pruneRecentlyShown();
            }
        } finally {
            showing.set(false);
        }
    }

    /**
     * The identity under which alerts deduplicate: title, description, and — for an error
     * carrying a cause — the throwable's class and message. Distinct problems keep distinct
     * dialogs; the same problem repeated (per node, per event) collapses to one.
     *
     * @param item the alert
     * @return the dedupe key
     */
    static String dedupeKey(AlertObject item) {
        StringBuilder key = new StringBuilder();
        key.append(item.getAlertTitle()).append('|').append(item.getAlertDescription());
        item.getThrowable().ifPresent(throwable ->
                key.append('|').append(throwable.getClass().getName())
                        .append(':').append(throwable.getMessage()));
        return key.toString();
    }

    /** Drops expired entries so the dedupe maps cannot grow without bound. */
    private void pruneRecentlyShown() {
        long cutoff = System.currentTimeMillis() - DEDUPE_WINDOW_MS;
        recentlyShown.entrySet().removeIf(entry -> {
            if (entry.getValue() < cutoff) {
                suppressedCounts.remove(entry.getKey());
                return true;
            }
            return false;
        });
    }
}
