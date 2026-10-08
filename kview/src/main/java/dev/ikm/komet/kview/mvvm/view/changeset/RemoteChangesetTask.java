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
package dev.ikm.komet.kview.mvvm.view.changeset;

import dev.ikm.tinkar.common.service.EntityCountSummary;
import dev.ikm.tinkar.common.service.RemoteChangesetService;
import dev.ikm.tinkar.common.service.ServiceLifecycleManager;
import dev.ikm.tinkar.common.service.TrackingCallable;

import java.util.Optional;

/**
 * Runs a changeset import or export on the remote datastore, for Komet connected over gRPC.
 *
 * <p>In that mode Komet's own store is only a local view of the server's: importing into it would
 * be lost on close and seen by nobody else, and exporting from it would cover only what Komet had
 * loaded. So the import and export dialogs hand the work to the server whenever a
 * {@link RemoteChangesetService} is running, and to the local loader and exporter otherwise.
 */
final class RemoteChangesetTask extends TrackingCallable<EntityCountSummary> {

    /** The remote call to make, given where to report progress and the handle that cancels it. */
    @FunctionalInterface
    interface Work {
        EntityCountSummary run(RemoteChangesetService service, RemoteChangesetService.ProgressListener listener,
                               TrackingCallable<?> tracker);
    }

    private final RemoteChangesetService service;
    private final Work work;

    RemoteChangesetTask(RemoteChangesetService service, String title, Work work) {
        super(true, true);
        this.service = service;
        this.work = work;
        updateTitle(title);
    }

    /** The remote service, when Komet is connected to one. */
    static Optional<RemoteChangesetService> remote() {
        return ServiceLifecycleManager.get().getRunningService(RemoteChangesetService.class);
    }

    @Override
    protected EntityCountSummary compute() {
        updateProgress(-1, 1);
        EntityCountSummary result = work.run(service, (done, total, message) -> {
            if (message != null && !message.isBlank()) {
                updateMessage(message);
            }
            if (total > 1 && done >= 0) {
                updateProgress(done, total);
            }
        }, this);
        updateProgress(1, 1);
        updateMessage("Done on the server in " + durationString());
        return result;
    }
}
