package dev.ikm.komet.framework.concurrent;

import javafx.collections.ObservableList;
import javafx.concurrent.Task;

public interface TaskListsService {
    static TaskListsService get() {
        return TaskListsProviderFinder.INSTANCE.get();
    }

    ObservableList<Task<?>> pendingTasks();

    ObservableList<Task<?>> executingTasks();

    ObservableList<Task<?>> completedTasks();
}
