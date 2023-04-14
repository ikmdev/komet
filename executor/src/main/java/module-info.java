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