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
package dev.ikm.komet.reasoner.ui;

import dev.ikm.tinkar.common.service.RemoteReasonerService;
import dev.ikm.tinkar.common.service.TrackingCallable;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.function.Consumer;

/**
 * Runs the reasoner on a remote datastore, reporting progress through the same
 * {@link TrackingCallable} machinery as the local {@link RunReasonerTaskBase}.
 *
 * <p>The remote service reports the same four phases, in the same order and wording, as the
 * local pipeline. Driving the shared progress UI from those events means a user sees the same
 * thing whether the datastore is local or remote — which is the point: the difference should be
 * where the work happens, not what the run looks like.
 *
 * <p>Unlike the local task there is no per-phase sub-task to submit here; the phases have
 * already run by the time each event arrives, so this reports completion rather than driving it.
 */
public class RunRemoteReasonerTask extends TrackingCallable<RemoteReasonerService.RemoteReasonerOutcome> {

    private static final Logger LOG = LoggerFactory.getLogger(RunRemoteReasonerTask.class);

    /** Phases the pipeline reports, matching the local reasoner task's maxWork. */
    private static final int EXPECTED_PHASES = 4;

    private final RemoteReasonerService reasonerService;

    private final Consumer<RemoteReasonerService.RemoteReasonerOutcome> outcomeConsumer;

    public RunRemoteReasonerTask(RemoteReasonerService reasonerService,
                                 Consumer<RemoteReasonerService.RemoteReasonerOutcome> outcomeConsumer) {
        super(true, true);
        this.reasonerService = reasonerService;
        this.outcomeConsumer = outcomeConsumer;
        updateTitle("Running reasoner on the remote datastore");
    }

    @Override
    public void updateMessage(String msg) {
        super.updateMessage(msg);
        LOG.info(msg);
    }

    @Override
    protected RemoteReasonerService.RemoteReasonerOutcome compute() throws Exception {
        // Matches the local pipeline's four phases; the remote service reports the same count
        // in each event, which is what drives the bar from then on.
        updateProgress(0, EXPECTED_PHASES);
        RemoteReasonerService.RemoteReasonerOutcome outcome =
                reasonerService.runFullReasoner((step, totalSteps, message) -> {
                    updateMessage("Step " + step + " of " + totalSteps + ": " + message);
                    updateProgress(step, totalSteps);
                }, this);
        // this, as the tracker: Komet's cancel calls cancel() on this task, and the remote
        // service watches it to stop the classification server-side.
        updateMessage("Reasoner run complete in " + durationString());
        if (outcomeConsumer != null) {
            outcomeConsumer.accept(outcome);
        }
        return outcome;
    }
}
