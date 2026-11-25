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
package dev.ikm.komet.kview.storage.test;

import dev.ikm.komet.kview.storage.StorageGestures;
import dev.ikm.tinkar.common.alert.AlertLog;
import dev.ikm.tinkar.common.alert.AlertType;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

/** How the storage gestures report: what worked is kept as a success, and what did not as a warning. */
class StorageReportTest {

    @Test
    void whatWorkedIsASuccessAndWhatDidNotIsAWarning() {
        assertEquals(AlertType.SUCCESS, reported(true, "Exported 12 components."));
        assertEquals(AlertType.WARNING, reported(false, "The file could not be written."));
    }

    private static AlertType reported(boolean done, String message) {
        AlertLog log = AlertLog.root();
        StorageGestures.report(null, StorageGestures.EXPORT, done, message);
        AlertLog.Entry last = log.entries().getLast();
        assertEquals(StorageGestures.EXPORT, last.title());
        assertEquals(message, last.description());
        return last.type();
    }
}
