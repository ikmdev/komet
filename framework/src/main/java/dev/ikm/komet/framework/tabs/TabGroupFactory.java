package dev.ikm.komet.framework.tabs;

import java.util.UUID;

public abstract class TabGroupFactory {

    TabGroup create(TabGroup source) {
        TabGroup newTabGroup = TabGroup.create(source.windowView(), source.getParentNodePreferences().node("tab-group-" +
                UUID.randomUUID()));
        newTabGroup.setSceneFactory(source.getSceneFactory());
        newTabGroup.setStageOwnerFactory(source.getStageOwnerFactory());
        newTabGroup.setScope(source.getScope());
        newTabGroup.setTabClosingPolicy(source.getTabClosingPolicy());
        newTabGroup.setCloseIfEmpty(true);
        newTabGroup.setTabGroupFactory(source.getTabGroupFactory());
        init(newTabGroup);
        return newTabGroup;
    }

    /**
     * Callback method to initialize newly created TabGroup for the Tab
     * that is being detached/docked.
     *
     * @param newTabGroup
     */
    protected abstract void init(TabGroup newTabGroup);
}
