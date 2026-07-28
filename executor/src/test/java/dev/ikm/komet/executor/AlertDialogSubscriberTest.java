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

import dev.ikm.tinkar.common.alert.AlertObject;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotEquals;

/**
 * Pins the alert-dedupe identity (ikmdev/komet#886): repeats of one failure — same title,
 * description, and cause — share a key and collapse to one dialog; distinct problems keep
 * distinct keys and distinct dialogs.
 */
public class AlertDialogSubscriberTest {

    @Test
    public void identicalAlertsShareAKey() {
        AlertObject first = AlertObject.makeError("Uncaught exception", "FX thread",
                new NullPointerException("Argument 'code' must not be null"));
        AlertObject second = AlertObject.makeError("Uncaught exception", "FX thread",
                new NullPointerException("Argument 'code' must not be null"));

        assertEquals(AlertDialogSubscriber.dedupeKey(first),
                AlertDialogSubscriber.dedupeKey(second),
                "the same failure repeated must collapse to one key");
    }

    @Test
    public void distinctCausesKeepDistinctKeys() {
        AlertObject npe = AlertObject.makeError("Uncaught exception", "FX thread",
                new NullPointerException("Argument 'code' must not be null"));
        AlertObject cce = AlertObject.makeError("Uncaught exception", "FX thread",
                new ClassCastException("String cannot be cast to Color"));

        assertNotEquals(AlertDialogSubscriber.dedupeKey(npe),
                AlertDialogSubscriber.dedupeKey(cce),
                "a different cause is a different problem — it must keep its own dialog");
    }

    @Test
    public void distinctTitlesKeepDistinctKeys() {
        AlertObject warning = AlertObject.makeWarning("Sync conflict", "file A");
        AlertObject other = AlertObject.makeWarning("Sync failure", "file A");

        assertNotEquals(AlertDialogSubscriber.dedupeKey(warning),
                AlertDialogSubscriber.dedupeKey(other));
    }
}
