package dev.ikm.komet.framework.concurrent;

import javafx.application.Platform;
import javafx.concurrent.Task;
import dev.ikm.tinkar.common.service.TrackingCallable;
import dev.ikm.tinkar.common.service.TrackingListener;
import dev.ikm.tinkar.common.util.time.DateTimeUtil;

import java.util.function.Consumer;

public class TaskWrapper<V> extends Task<V> implements TrackingListener<V> {
    private static int maxCompletedTaskListSize = 200;
    private final TrackingCallable<V> trackingCallable;
    private final Consumer<V> appThreadConsumer;

    private TaskWrapper(TrackingCallable<V> trackingCallable) {
        this.trackingCallable = trackingCallable;
        this.appThreadConsumer = null;
        this.updateProgress(-1, -1);
        this.trackingCallable.addListener(this);
    }

    private TaskWrapper(TrackingCallable<V> trackingCallable, Consumer<V> appThreadConsumer) {
        this.trackingCallable = trackingCallable;
        this.appThreadConsumer = appThreadConsumer;
        this.updateProgress(-1, -1);
        this.trackingCallable.addListener(this);
    }

    public static <V> TaskWrapper<V> make(TrackingCallable<V> trackingCallable) {
        return new TaskWrapper<>(trackingCallable);
    }

    public static <V> TaskWrapper<V> make(TrackingCallable<V> trackingCallable, Consumer<V> appThreadConsumer) {
        return new TaskWrapper<>(trackingCallable, appThreadConsumer);
    }

    @Override
    protected V call() throws Exception {
        V result = trackingCallable.call();
        if (appThreadConsumer != null) {
            Platform.runLater(() -> appThreadConsumer.accept(result));
        }
        return result;
    }

    @Override
    protected void scheduled() {
        TaskListsService.get().pendingTasks().add(this);
    }

    @Override
    protected void running() {
        TaskListsService.get().pendingTasks().remove(this);
        TaskListsService.get().executingTasks().add(this);
    }

    @Override
    protected void succeeded() {
        TaskListsService.get().executingTasks().remove(this);
        handleRetention();
    }

    private void handleRetention() {
        if (this.trackingCallable.retainWhenComplete()) {
            CompletedTask completedTask = new CompletedTask(this.getTitle(), this.getMessage(), DateTimeUtil.nowWithZone());
            TaskListsService.get().completedTasks().add(0, completedTask);
            if (TaskListsService.get().completedTasks().size() > maxCompletedTaskListSize) {
                TaskListsService.get().completedTasks().remove(maxCompletedTaskListSize, TaskListsService.get().completedTasks().size());
            }
        }
    }

    @Override
    protected void cancelled() {
        this.trackingCallable.cancel();
    }

    @Override
    protected void failed() {
        TaskListsService.get().executingTasks().remove(this);
        handleRetention();
        //Failure notification handled by afterExecute(Runnable r, Throwable t) on KometThreadPoolExecutor and KometScheduledExecutor
        //Platform.runLater(() -> showExceptionDialog());
    }


    @Override
    public void updateProgress(double workDone, double max) {
        super.updateProgress(workDone, max);
    }

    @Override
    public void updateMessage(String message) {
        super.updateMessage(message);
    }

    @Override
    public void updateTitle(String title) {
        super.updateTitle(title);
    }

    @Override
    public void updateValue(V result) {
        super.updateValue(result);
    }
}
