package dev.ikm.komet.layout.orchestration;

import org.controlsfx.control.action.Action;
import org.eclipse.collections.api.list.ImmutableList;

public interface WindowRestoreProvider {
    ImmutableList<Action> restoreWindowActions();
}
