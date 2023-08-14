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
