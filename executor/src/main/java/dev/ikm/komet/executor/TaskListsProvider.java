package dev.ikm.komet.executor;

import com.google.auto.service.AutoService;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.concurrent.Task;
import dev.ikm.komet.framework.concurrent.TaskListsService;

@AutoService(TaskListsService.class)
public class TaskListsProvider implements TaskListsService {

    public static final ObservableList<Task<?>> pendingTasks = FXCollections.observableArrayList();
    public static final ObservableList<Task<?>> executingTasks = FXCollections.observableArrayList();
    public static final ObservableList<Task<?>> completedTasks = FXCollections.observableArrayList();

    public ObservableList<Task<?>> pendingTasks() {
        return pendingTasks;
    }

    public ObservableList<Task<?>> executingTasks() {
        return executingTasks;
    }

    public ObservableList<Task<?>> completedTasks() {
        return completedTasks;
    }
}
