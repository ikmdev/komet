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
package dev.ikm.komet.framework.dnd;

import java.util.Collections;
import java.util.Set;
import java.util.WeakHashMap;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicLong;
import java.util.function.IntSupplier;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import dev.ikm.komet.framework.FxUtils;
import dev.ikm.tinkar.common.service.CachingService;
import dev.ikm.tinkar.common.service.TinkExecutor;
import javafx.application.Platform;
import javafx.scene.Node;
import javafx.scene.effect.Effect;

/**
 * {@link DragRegistry}
 *
 * @author <a href="mailto:daniel.armbrust.list@gmail.com">Dan Armbrust</a>
 * 
 */
public class DragRegistry {
    private static final Logger LOG = LoggerFactory.getLogger(DragRegistry.class);

    public static class CacheProvider implements CachingService {
        @Override
        public void reset() {
            existingEffect.clear();
            codeDropTargets.clear();
            singleton = null;
        }
    }

    private static DragRegistry singleton;

    public static DragRegistry get() {
        if (singleton == null) {
            singleton = new DragRegistry();
        }
        return singleton;
    }

    private final AtomicLong dragStartedAt = new AtomicLong();
    private ScheduledFuture<?> timedDragCancel;
    /**
     * To enable garbage collection,
     */
    private static final Set<Node> codeDropTargets = Collections.newSetFromMap(new WeakHashMap<>());
    private static final WeakHashMap<Node, Effect> existingEffect = new WeakHashMap<>();

    private DragRegistry() {
        LOG.debug("Drag Registry init");
    }

    public void removeDragCapability(Node n) {
        n.setOnDragDetected(null);
        n.setOnDragDone(null);
        n.setOnDragOver(null);
        n.setOnDragEntered(null);
        n.setOnDragExited(null);
        n.setOnDragDropped(null);
        codeDropTargets.remove(n);
        n.setEffect(existingEffect.remove(n));
    }

    public synchronized void conceptDragStarted() {
        LOG.debug("Drag Started");
        // There is a bug in javafx with comboboxes - it seems to fire dragStarted events twice.
        // http://javafx-jira.kenai.com/browse/RT-28778
        if ((System.currentTimeMillis() - dragStartedAt.get()) < 2000) {
            LOG.debug("Ignoring duplicate drag started event");
            return;
        }
        if (dragStartedAt.get() > 0) {
            LOG.warn("Unclosed drag event is still active while another was started!  Cleaning up...");
            conceptDragCompleted();
        }
        dragStartedAt.set(System.currentTimeMillis());
        codeDropTargets.stream().map((n) -> {
            Effect existing = n.getEffect();
            if (existing != null) {
                existingEffect.put(n, existing);
            }
            return n;
        }).forEachOrdered((n) -> {
            n.setEffect(FxUtils.LIGHT_GREEN_DROP_SHADOW);
        });
        timedDragCancel = TinkExecutor.scheduled().schedule(()
                -> {
            if (dragStartedAt.get() > 0) {
                LOG.warn("Unclosed drag event is still active 10 seconds after starting!  Cleaning up...");
                Platform.runLater(() -> {
                    conceptDragCompleted();
                });
            }
        }, 10, TimeUnit.SECONDS);
    }

    public synchronized void conceptDragCompleted() {
        LOG.debug("Drag Completed");
        dragStartedAt.set(0);
        codeDropTargets.forEach((n) -> {
            n.setEffect(existingEffect.remove(n));
        });
        if (timedDragCancel != null) {
            timedDragCancel.cancel(false);
            timedDragCancel = null;
        }
    }


    public static void dragComplete() {
        DragRegistry.get().conceptDragCompleted();
    }

    public static void dragStart() {
        DragRegistry.get().conceptDragStarted();
    }

    public void setupDragOnly(final Node n, IntSupplier nidSupplier) {
        LOG.trace("Configure drag support for node {}", n);
        n.setOnDragDetected(new DragDetectedCellEventHandler(nidSupplier));
        n.setOnDragDone(new DragDoneEventHandler());
    }

    public void setupDragOnly(final Node n) {
        LOG.trace("Configure drag support for node {}", n);
        n.setOnDragDetected(new DragDetectedCellEventHandler());
        n.setOnDragDone(new DragDoneEventHandler());
    }

}
