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
