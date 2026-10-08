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
package dev.ikm.komet.framework.concurrent;

import dev.ikm.tinkar.common.service.TrackingCallable;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

/** A tracking callable that never names itself is titled by its class, in words. */
class TaskWrapperTest {

    static final class LoadDataSourceTask extends TrackingCallable<Void> {
        LoadDataSourceTask() {
            super(false, false);
        }

        @Override
        protected Void compute() {
            return null;
        }
    }

    @Test
    void aNamedClassIsItsNameInWords() {
        assertEquals("Load data source task", TaskWrapper.defaultTitle(new LoadDataSourceTask()));
    }

    @Test
    void anAnonymousClassIsNamedForItsEnclosingClass() {
        TrackingCallable<Void> anonymous = new TrackingCallable<>(false, false) {
            @Override
            protected Void compute() {
                return null;
            }
        };
        assertEquals("Task wrapper test", TaskWrapper.defaultTitle(anonymous));
    }
}
