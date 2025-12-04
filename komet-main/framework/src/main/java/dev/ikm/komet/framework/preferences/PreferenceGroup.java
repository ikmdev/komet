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
package dev.ikm.komet.framework.preferences;

import javafx.beans.property.SimpleStringProperty;
import javafx.scene.Node;
import dev.ikm.komet.framework.view.ViewProperties;
import dev.ikm.komet.preferences.KometPreferences;

import java.util.List;

/**
 * 
 */
public interface PreferenceGroup {
    public static void removeChild(KometPreferences preferences, String uuidStr) {
        List<String> propertySheetChildren = preferences.getList(Keys.CHILDREN_NODES);
        propertySheetChildren.remove(uuidStr);
        preferences.putList(Keys.CHILDREN_NODES, propertySheetChildren);
    }

    /**
     * @param viewProperties
     * @return property sheet for editing properties in this group.
     */
    Node getCenterPanel(ViewProperties viewProperties);

    /**
     * @param viewProperties
     * @return possibly null panel
     */
    Node getRightPanel(ViewProperties viewProperties);

    /**
     * @param viewProperties
     * @return possibly null panel
     */
    Node getTopPanel(ViewProperties viewProperties);

    /**
     * @param viewProperties
     * @return possibly null panel
     */
    Node getBottomPanel(ViewProperties viewProperties);

    /**
     * @param viewProperties
     * @return possibly null panel
     */
    Node getLeftPanel(ViewProperties viewProperties);

    /**
     * @return name for this group. Will be used in tree view navigation of
     * preferences.
     */
    String getGroupName();

    SimpleStringProperty groupNameProperty();

    /**
     * Save preferences in group to preferences store
     */
    void save();

    /**
     * Revert any changed preferences to values currently in preferences store
     */
    void revert();

    /**
     * @return True of this PreferenceGroup is previously initialized, and was
     * read from preferences. False if this PreferenceGroup is to be newly created
     * with default values.
     */
    boolean initialized();

    PreferencesTreeItem getTreeItem();

    void setTreeItem(PreferencesTreeItem item);


    enum Keys {
        INITIALIZED,
        GROUP_NAME,
        PROPERTY_SHEET_CLASS,
        CHILDREN_NODES
    }

}
