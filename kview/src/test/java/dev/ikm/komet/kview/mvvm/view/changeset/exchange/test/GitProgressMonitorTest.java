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
package dev.ikm.komet.kview.mvvm.view.changeset.exchange.test;

import dev.ikm.komet.kview.mvvm.view.changeset.exchange.GitProgressMonitor;
import dev.ikm.tinkar.common.service.TrackingCallable;
import dev.ikm.tinkar.common.service.TrackingListener;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

/** Git's progress stays in the part of an operation it is given, and may begin more tasks than it announces. */
class GitProgressMonitorTest {

    @Test
    void moreTasksThanAnnouncedStayInTheRange() {
        List<Double> reported = new ArrayList<>();
        TrackingCallable<Void> operation = new TrackingCallable<>() {
            @Override
            protected Void compute() {
                return null;
            }
        };
        operation.addListener(new TrackingListener<Void>() {
            @Override
            public void updateValue(Void result) {
            }

            @Override
            public void updateMessage(String message) {
            }

            @Override
            public void updateTitle(String title) {
            }

            @Override
            public void updateProgress(double workDone, double max) {
                reported.add(workDone);
            }
        });
        reported.clear();

        GitProgressMonitor monitor = new GitProgressMonitor(operation, 70, 90, 100);
        monitor.start(1);
        for (String task : List.of("Counting objects", "Finding sources", "Getting sizes", "Compressing objects", "Writing objects")) {
            monitor.beginTask(task, 4);
            for (int unit = 0; unit < 4; unit++) {
                monitor.update(1);
            }
            monitor.endTask();
        }

        assertTrue(reported.stream().allMatch(value -> value >= 70 && value <= 90), reported.toString());
        assertEquals(90, reported.getLast());
    }
}
