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
package dev.ikm.komet.kview.mvvm.view.changeset.exchange;

import dev.ikm.tinkar.common.service.TrackingCallable;
import dev.ikm.tinkar.common.util.time.Stopwatch;
import org.eclipse.jgit.lib.ProgressMonitor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * A Git progress monitor that maps Git progress to a TrackingCallable's progress system.
 * This monitor can be configured to represent a phase within the overall progress of a task.
 * It also tracks task duration when enabled.
 */
public class GitProgressMonitor implements ProgressMonitor {
    private static final Logger LOG = LoggerFactory.getLogger(GitProgressMonitor.class);

    // The TrackingCallable that will receive progress updates
    private final TrackingCallable<?> trackingCallable;

    // Progress range parameters
    private final double rangeStart;      // Start percentage in the overall task (0-100)
    private final double rangeEnd;        // End percentage in the overall task (0-100)
    private final double totalWorkUnits;  // Total work units for the entire tracked process

    // Task tracking state
    private int totalTasks = 1;           // Number of tasks
    private int currentTask = 0;          // Current task index
    private int taskTotalWork = UNKNOWN;  // Total work for current task
    private int taskCompletedWork = 0;    // Completed work for current task
    private String taskTitle = "";        // Current task title
    private boolean isIndeterminate = true; // Whether progress is indeterminate

    // Duration tracking
    private Stopwatch stopwatch;          // Stopwatch for tracking task duration
    private boolean showDuration = false; // Whether to show task duration

    /**
     * Creates a new GitProgressMonitor that reports progress to a TrackingCallable.
     *
     * @param trackingCallable The callable that will receive progress updates
     * @param rangeStart The start percentage in the overall task (0-100)
     * @param rangeEnd The end percentage in the overall task (0-100)
     * @param totalWorkUnits The total work units for the entire tracked process
     */
    public GitProgressMonitor(
            TrackingCallable<?> trackingCallable,
            double rangeStart,
            double rangeEnd,
            double totalWorkUnits) {
        this.trackingCallable = trackingCallable;
        this.rangeStart = rangeStart;
        this.rangeEnd = rangeEnd;
        this.totalWorkUnits = totalWorkUnits;
    }

    /**
     * Creates a new GitProgressMonitor that reports progress to a TrackingCallable
     * using the default range of 0-100%.
     *
     * @param trackingCallable The callable that will receive progress updates
     * @param totalWorkUnits The total work units for the entire tracked process
     */
    public GitProgressMonitor(TrackingCallable<?> trackingCallable, double totalWorkUnits) {
        this(trackingCallable, 0, 100, totalWorkUnits);
    }

    @Override
    public void start(int totalTasks) {
        this.totalTasks = Math.max(1, totalTasks); // Ensure at least 1 task
        this.currentTask = 0;

        LOG.info("Git progress started with {} tasks", totalTasks);
        updateProgress(0); // Initialize progress at the beginning
    }

    @Override
    public void beginTask(String title, int totalWork) {
        this.taskTitle = title;
        this.taskTotalWork = totalWork;
        this.taskCompletedWork = 0;
        this.isIndeterminate = (totalWork <= 0);

        // Initialize stopwatch if duration tracking is enabled
        if (showDuration) {
            stopwatch = new Stopwatch();
        }

        // Calculate the task's portion of the overall range
        double taskRangeSize = (rangeEnd - rangeStart) / totalTasks;
        double taskRangeStart = rangeStart + (currentTask * taskRangeSize);

        LOG.info("Git task started: '{}' with {} work units", title, totalWork);

        // Update message with the task title
        if (title != null && !title.isEmpty()) {
            trackingCallable.updateMessage(title);
        }

        // Set initial progress for this task
        updateProgress(0);
    }

    @Override
    public void update(int completed) {
        taskCompletedWork += completed;

        // Update if enough time has passed OR if this single update represents a significant portion (>=5%) of the total work
        if (trackingCallable.updateIntervalElapsed() ||
                (taskTotalWork > 0 && ((double)completed / taskTotalWork) >= 0.05)) {
            // Log at info level to avoid flooding logs
            LOG.info("Git progress update: {} of {} units completed for '{}'",
                    taskCompletedWork, taskTotalWork, taskTitle);

            updateProgress(taskCompletedWork);
        }
    }

    @Override
    public void endTask() {
        // Mark this task as complete
        currentTask++;

        // Calculate what percentage of the full range we should be at
        double progressPercentage = Math.min(1.0, (double) currentTask / totalTasks);
        double overallProgress = rangeStart + progressPercentage * (rangeEnd - rangeStart);

        // Log task completion with duration if enabled
        if (showDuration && stopwatch != null) {
            stopwatch.end();
            LOG.info("Git task completed: '{}'. Duration: {}", taskTitle, stopwatch.durationString());
        } else {
            LOG.info("Git task completed: '{}'", taskTitle);
        }

        // Update progress to the end of this task's range
        trackingCallable.updateProgress(
                overallProgress * totalWorkUnits / 100.0,
                totalWorkUnits);
    }

    @Override
    public boolean isCancelled() {
        return trackingCallable.isCancelled();
    }

    @Override
    public void showDuration(boolean enabled) {
        this.showDuration = enabled;
    }

    /**
     * Updates the progress in the TrackingCallable based on the current task's progress.
     *
     * @param completedWorkForTask The amount of work completed in the current task
     */
    private void updateProgress(int completedWorkForTask) {
        // Calculate the progress percentage for the current task
        double taskProgressPercentage;
        if (isIndeterminate || taskTotalWork <= 0) {
            // For indeterminate progress, use simulated progress based on completed work
            // This gives some visual feedback even when total work is unknown
            if (completedWorkForTask <= 0) {
                taskProgressPercentage = 0.0;
            } else if (completedWorkForTask < 10) {
                taskProgressPercentage = 0.3;
            } else if (completedWorkForTask < 100) {
                taskProgressPercentage = 0.6;
            } else {
                taskProgressPercentage = 0.8; // Never reach 1.0 for indeterminate tasks until endTask
            }
        } else {
            // For determinate progress, calculate the exact percentage
            taskProgressPercentage = Math.min(1.0, (double) completedWorkForTask / taskTotalWork);
        }

        // Calculate this task's portion of the overall range
        final double taskRangeSize = (rangeEnd - rangeStart) / totalTasks;
        final double taskRangeStart = rangeStart + (currentTask * taskRangeSize);
        final double taskRangeEnd = taskRangeStart + taskRangeSize;

        // Calculate overall progress (as a percentage of the total process)
        final double overallProgress = taskRangeStart + taskProgressPercentage * (taskRangeEnd - taskRangeStart);

        // Map to work units and update the TrackingCallable
        trackingCallable.updateProgress(
                overallProgress * totalWorkUnits / 100.0,
                totalWorkUnits);
    }
}