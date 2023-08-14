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
import dev.ikm.komet.executor.AlertDialogSubscriber;
import dev.ikm.komet.executor.KometExecutorController;
import dev.ikm.komet.executor.TaskListsProvider;
import dev.ikm.komet.framework.concurrent.TaskListsService;
import dev.ikm.tinkar.common.alert.AlertReportingService;
import dev.ikm.tinkar.common.service.CachingService;
import dev.ikm.tinkar.common.service.ExecutorController;

module dev.ikm.komet.executor {

    exports dev.ikm.komet.executor;
    provides AlertReportingService with AlertDialogSubscriber;
    provides CachingService with KometExecutorController.CacheProvider;
    provides ExecutorController with KometExecutorController;
    provides TaskListsService with TaskListsProvider;
    requires transitive javafx.base;
    requires transitive javafx.controls;
    requires transitive javafx.graphics;
    requires transitive dev.ikm.komet.framework;
    requires transitive dev.ikm.tinkar.common;
    uses TaskListsService;
}