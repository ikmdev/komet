package dev.ikm.komet.layout.orchestration;

import dev.ikm.komet.layout.KlView;
import dev.ikm.komet.layout.preferences.KlProfiles;
import dev.ikm.komet.layout.window.KlFxWindow;
import dev.ikm.komet.preferences.KometPreferences;
import org.controlsfx.control.action.Action;
import org.eclipse.collections.api.factory.Lists;
import org.eclipse.collections.api.list.ImmutableList;
import org.eclipse.collections.api.list.MutableList;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.prefs.BackingStoreException;

import static dev.ikm.komet.layout.KlRestorable.PreferenceKeys.FACTORY_CLASS_NAME;
import static dev.ikm.komet.layout.KlRestorable.PreferenceKeys.NAME_FOR_RESTORE;

public class WindowRestoreMenuProvider implements WindowRestoreProvider {
    private static final Logger LOG = LoggerFactory.getLogger(WindowRestoreMenuProvider.class);

    @Override
    public ImmutableList<Action> restoreWindowActions() {
        MutableList<Action> windowActions =  Lists.mutable.empty();
        try {
            for (KometPreferences windowPreferences: KlProfiles.sharedWindowPreferences().children()) {
                windowPreferences.get(NAME_FOR_RESTORE).ifPresent(nameForRestore -> {
                    windowPreferences.get(FACTORY_CLASS_NAME).ifPresent(factoryClassName -> {
                        Action newLayoutWindowAction = new Action(nameForRestore,
                                event -> {
                                    KlView<?> klView = KlView.restoreWithChildren(windowPreferences);
                                    if (klView instanceof KlFxWindow fxWindow) {
                                        fxWindow.show();
                                    } else {
                                        LOG.error("Unable to restore window for class {}", factoryClassName);
                                    }
                                });
                        windowActions.add(newLayoutWindowAction);
                    });
                });
            }
        } catch (BackingStoreException e) {
            throw new RuntimeException(e);
        }
        return windowActions.toImmutable();
    }
}
