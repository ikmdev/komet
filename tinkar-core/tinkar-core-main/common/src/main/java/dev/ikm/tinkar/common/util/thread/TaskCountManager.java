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
package dev.ikm.tinkar.common.util.thread;

import java.util.concurrent.Semaphore;

/**
 * Class to manage multi-threaded tasks, to ensure number of
 * concurrent tasks do not overwhelm queue.
 */
public class TaskCountManager {


    /**
     * Use when multi threading a task, to ensure that queue resources don't get overwhelmed.
     * Search for usages for examples. Semaphore count is from the permitCount() method on this class.
     * @return a Semaphore for governing task execution.
     */
    public static TaskCountManager get() {
        return new TaskCountManager();
    }

    private final int taskCount;
    private final Semaphore taskSemaphore;

    public TaskCountManager(int taskCount) {
        this.taskCount = taskCount;
        this.taskSemaphore = new Semaphore(taskCount);
    }

    /**
     * Provides a standard manager for concurrent additions to queues for multi-threaded tasks. The size prevents
     * the queues from being overwhelmed, but also is large enough to keep the CPU occupied.
     * Creates a TaskCountManager with count = Runtime.getRuntime().availableProcessors() * 2.
     */
    public TaskCountManager() {
        this(Runtime.getRuntime().availableProcessors() * 2);
    }

    public void acquire() throws InterruptedException {
        this.taskSemaphore.acquire();
    }
    public void release() {
        this.taskSemaphore.release();
    }

    public void waitForCompletion() throws InterruptedException {
        this.taskSemaphore.acquire(taskCount);
        this.taskSemaphore.release(taskCount);
    }

}
