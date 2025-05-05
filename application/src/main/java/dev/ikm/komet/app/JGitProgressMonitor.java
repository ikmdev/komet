package dev.ikm.komet.app;

import org.eclipse.jgit.lib.ProgressMonitor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import dev.ikm.tinkar.common.util.time.Stopwatch;

/**
 * JGitProgressMonitor is an implementation of the ProgressMonitor interface from JGit. It logs the progress of
 * operations using a logger.
 */
class JGitProgressMonitor implements ProgressMonitor {
    private static final Logger LOG = LoggerFactory.getLogger(JGitProgressMonitor.class);

    private Stopwatch stopwatch;

    private boolean showDuration = false;
    /**
     * Logs the start of the progress monitor with the total number of tasks.
     *
     * @param totalTasks the total number of tasks to be monitored
     */
    @Override
    public void start(int totalTasks) {
        LOG.info("Starting progress monitor. Total tasks: {}", totalTasks);
    }

    /**
     * Begins a new task with the specified title and total work.
     *
     * @param title The title of the task.
     * @param totalWork The total amount of work for the task.
     */
    @Override
    public void beginTask(String title, int totalWork) {
        LOG.info("Begin task: {}. Total work: {}", title, totalWork);
        if (showDuration) {
            stopwatch = new Stopwatch();
        }
    }

    /**
     * Updates the progress of a task being monitored. This method is called periodically to update
     * the progress of the task.
     *
     * @param completed The number of completed units of work.
     */
    @Override
    public void update(int completed) {
        LOG.info("Update progress: {}. ", completed);
    }

    /**
     * Ends the current task being monitored.
     * <p>
     * This method is called when the current task is completed or canceled.
     * It logs the end of the task using a logger.
     */
    @Override
    public void endTask() {
        if (showDuration && stopwatch != null) {
            stopwatch.end();
            LOG.info("End task. Duration: {}", stopwatch.durationString());
        }  else {
            LOG.info("End task.");
        }
    }

    /**
     * Returns true if the current task being monitored is cancelled, false otherwise.
     *
     * @return true if the task is cancelled, false otherwise.
     */
    @Override
    public boolean isCancelled() {
        return false;
    }

    /**
     * Displays the duration of the current task being monitored.
     *
     * @param enabled a boolean value indicating whether to show the duration or not
     */
    @Override
    public void showDuration(boolean enabled) {
        this.showDuration = true;
    }
}
