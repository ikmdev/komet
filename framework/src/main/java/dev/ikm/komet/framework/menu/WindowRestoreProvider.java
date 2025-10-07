package dev.ikm.komet.framework.menu;

import org.controlsfx.control.action.Action;
import org.eclipse.collections.api.list.ImmutableList;

public interface WindowRestoreProvider {
    ImmutableList<Action> restoreWindowActions();
}
