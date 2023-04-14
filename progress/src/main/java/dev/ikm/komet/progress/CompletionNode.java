package dev.ikm.komet.progress;

import javafx.application.Platform;
import javafx.beans.binding.Bindings;
import javafx.concurrent.Task;
import javafx.scene.Node;
import org.controlsfx.control.TaskProgressView;
import org.eclipse.collections.api.list.ImmutableList;
import dev.ikm.komet.framework.ExplorationNodeAbstract;
import dev.ikm.komet.framework.concurrent.CompletedTask;
import dev.ikm.komet.framework.concurrent.TaskListsService;
import dev.ikm.komet.framework.view.ViewProperties;
import dev.ikm.komet.preferences.KometPreferences;
import dev.ikm.tinkar.terms.EntityFacade;
import org.kordamp.ikonli.javafx.FontIcon;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

public class CompletionNode extends ExplorationNodeAbstract {

    protected static final String STYLE_ID = "completion-node";
    protected static final String TITLE = "Completions";

    TaskProgressView<Task<?>> progressView = new TaskProgressView<>();
    TaskListsService taskLists = TaskListsService.get();

    {
        progressView.setRetainTasks(true);
        CompletionViewSkin skin = new CompletionViewSkin<>(progressView, taskLists.completedTasks());
        progressView.setSkin(skin);
        Bindings.bindContent(progressView.getTasks(), taskLists.completedTasks());
    }

    public CompletionNode(ViewProperties viewProperties, KometPreferences nodePreferences) {
        super(viewProperties, nodePreferences);
        revertPreferences();
    }

    @Override
    public String getDefaultTitle() {
        return TITLE;
    }

    @Override
    public void handleActivity(ImmutableList<EntityFacade> entities) {
        throw new UnsupportedOperationException();
    }

    @Override
    public void revertAdditionalPreferences() {
        List<Task<?>> completedTasks = new ArrayList<>();
        List<String> completedTaskStrings = nodePreferences.getList(CompletionKeys.COMPLETED_TASKS);
        Iterator<String> completedTaskStringItr = completedTaskStrings.listIterator();
        while (completedTaskStringItr.hasNext() && completedTasks.size() < 12) {
            CompletedTask completedTask = new CompletedTask(completedTaskStringItr.next(),
                    completedTaskStringItr.next(),
                    completedTaskStringItr.next());
            completedTasks.add(completedTask);
        }
        Platform.runLater(() -> progressView.getTasks().addAll(completedTasks));
    }

    @Override
    public String getStyleId() {
        return STYLE_ID;
    }

    @Override
    protected void saveAdditionalPreferences() {
        List<String> completedTaskStrings = new ArrayList<>();
        for (Task task : progressView.getTasks()) {
            if (task instanceof CompletedTask completedTask) {
                completedTaskStrings.add(completedTask.title());
                completedTaskStrings.add(completedTask.message());
                completedTaskStrings.add(completedTask.completionTime());
            }
        }
        nodePreferences.putList(CompletionKeys.COMPLETED_TASKS, completedTaskStrings);
    }

    private void writeDataToArray(List<String> completedTaskStrings) {
    }

    private Node getTitleGraphic() {
        FontIcon icon = new FontIcon();
        icon.setIconLiteral("mdi2f-flag-checkered:16:white");
        icon.setId(getStyleId());
        return icon;
    }

    public void removeTask(Task<?> task) {
        Platform.runLater(() -> taskLists.completedTasks().remove(task));
    }

    @Override
    public Node getNode() {
        return progressView;
    }

    @Override
    public void close() {

    }

    @Override
    public boolean canClose() {
        return false;
    }

    @Override
    public Class factoryClass() {
        return CompletionNodeFactory.class;
    }

    enum CompletionKeys {
        COMPLETED_TASKS
    }
}
