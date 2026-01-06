package dev.ikm.komet.layout.orchestration;

import dev.ikm.komet.preferences.KometPreferences;
import dev.ikm.komet.preferences.KometPreferencesImpl;
import javafx.application.Platform;
import javafx.concurrent.Task;

import java.util.List;

public class ForgetWindowTask extends Task<Void> {

    final String windowName;

    public ForgetWindowTask(String windowName) {
        this.windowName = windowName;
    }

    @Override
    protected Void call() throws Exception {
        KometPreferences appPreferences = KometPreferencesImpl.getConfigurationRootPreferences();
        if (appPreferences.nodeExists(windowName)) {
            KometPreferences windowPreferencesNode = appPreferences.node(windowName);
            windowPreferencesNode.removeNode();
        }
        List<String> savedWindows = appPreferences.getList(WindowServiceKeys.SAVED_WINDOWS);
        savedWindows.remove(this.windowName);
        appPreferences.putList(WindowServiceKeys.SAVED_WINDOWS, savedWindows);
        appPreferences.flush();
        Platform.runLater(() -> WindowMenuManager.updateMenus());
        return null;
    }

}
