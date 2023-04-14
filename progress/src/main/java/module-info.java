import dev.ikm.komet.framework.KometNodeFactory;
import dev.ikm.komet.framework.concurrent.TaskListsService;
import dev.ikm.komet.progress.CompletionNodeFactory;
import dev.ikm.komet.progress.ProgressNodeFactory;

module dev.ikm.komet.progress {
    exports dev.ikm.komet.progress;
    requires dev.ikm.komet.framework;
    requires org.controlsfx.controls;
    requires org.kordamp.ikonli.core;
    requires dev.ikm.komet.preferences;
    requires org.kordamp.ikonli.javafx;

    provides KometNodeFactory
            with ProgressNodeFactory, CompletionNodeFactory;

    uses TaskListsService;
}