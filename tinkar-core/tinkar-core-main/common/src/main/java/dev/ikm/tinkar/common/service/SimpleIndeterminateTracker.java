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
package dev.ikm.tinkar.common.service;

public class SimpleIndeterminateTracker extends TrackingCallable<Void> {
    boolean finished = false;
    String title;
    private Thread sleepingThread;

    public SimpleIndeterminateTracker(String title) {
        super(false, true);
        this.title = title;
        updateTitle(title + " executing");
    }

    public void finished() {
        this.finished = true;
        if (sleepingThread != null) {
            this.sleepingThread.interrupt();
        }
    }

    @Override
    protected Void compute() throws Exception {
        while (!finished) {
            try {
                this.sleepingThread = Thread.currentThread();
                Thread.sleep(2000);
            } catch (InterruptedException e) {
                if (finished) {
                    break;
                }
            }
        }
        updateTitle(title + " completed");
        updateMessage("In " + durationString());
        return null;
    }
}
