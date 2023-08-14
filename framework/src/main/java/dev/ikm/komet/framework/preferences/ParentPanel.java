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

import javafx.event.ActionEvent;
import javafx.scene.Node;
import javafx.scene.control.Button;
import javafx.scene.control.ContextMenu;
import javafx.scene.control.MenuItem;
import javafx.scene.control.ToolBar;
import javafx.stage.Stage;
import dev.ikm.komet.framework.MenuItemWithText;
import dev.ikm.komet.framework.view.ViewProperties;
import dev.ikm.komet.preferences.KometPreferences;
import dev.ikm.komet.preferences.PreferencesService;

import java.util.Optional;
import java.util.Stack;
import java.util.UUID;
import java.util.prefs.BackingStoreException;

import static dev.ikm.komet.framework.preferences.PreferenceGroup.Keys.CHILDREN_NODES;
import static dev.ikm.komet.framework.preferences.PreferenceGroup.Keys.GROUP_NAME;

/**
 * 
 */
public abstract class ParentPanel extends AbstractPreferences {

    Stack<PreferencesTreeItem> childrenToAdd = new Stack<>();

    public ParentPanel(KometPreferences preferencesNode, String groupName,
                       ViewProperties viewProperties, KometPreferencesController kpc) {
        super(preferencesNode, groupName, viewProperties, kpc);
    }

    protected final void removeChild(AbstractPreferences child) {
        this.getTreeItem().removeChild(child.preferencesNode.name());
        save();
    }

    @Override
    public Node getTopPanel(ViewProperties viewProperties) {
        Button addButton = new Button("Add");
        addButton.setOnAction(this::newChild);
        ToolBar toolbar = new ToolBar(addButton);
        MenuItem resetUserItems = new MenuItemWithText("Clear user items");
        resetUserItems.setOnAction(this::resetUserItems);
        MenuItem resetConfigurationAndUserItems = new MenuItemWithText("Clear user and child items");
        resetConfigurationAndUserItems.setOnAction(this::resetConfigurationAndUserItems);
        toolbar.setContextMenu(new ContextMenu(resetUserItems, resetConfigurationAndUserItems));
        return toolbar;
    }

    @Override
    protected final void addChildren() {
        while (!childrenToAdd.empty()) {
            getTreeItem().getChildren().add(childrenToAdd.pop());
            getTreeItem().setExpanded(true);
        }
    }

    @Override
    public boolean showRevertAndSave() {
        return false;
    }

    protected final void newChild(ActionEvent action) {
        UUID newUuid = UUID.randomUUID();
        addChildPanel(newUuid, Optional.empty());
    }

    private void resetUserItems(ActionEvent actionEvent) {
        PreferencesService.get().clearUserPreferences();
        Stage stage = (Stage) this.getTreeItem().getController().getPreferenceTree().getScene().getWindow();
        stage.close();
    }

    private void resetConfigurationAndUserItems(ActionEvent actionEvent) {
        try {
            KometPreferences configurationNode = PreferencesService.configurationPreferences().node(getPreferencesNode().absolutePath());
            configurationNode.node(getPreferencesNode().absolutePath());
            configurationNode.remove(CHILDREN_NODES);
            configurationNode.sync();
            PreferencesService.userPreferences().clear();
            // Close the view, not the preferences service.
            // PreferencesService.get()..closePreferences();
        } catch (BackingStoreException ex) {
            throw new RuntimeException(ex);
        }
    }

    final protected KometPreferences addChildPanel(UUID childUuid, Optional<String> groupName) {
        KometPreferences preferencesNode = getPreferencesNode().node(childUuid.toString());
        if (groupName.isPresent()) {
            preferencesNode.put(GROUP_NAME, groupName.get());
        }
        addChild(childUuid.toString(), getChildClass());
        Optional<PreferencesTreeItem> optionalActionItem = PreferencesTreeItem.from(preferencesNode,
                getViewProperties(), kpc);
        if (optionalActionItem.isPresent()) {
            PreferencesTreeItem actionItem = optionalActionItem.get();
            if (getTreeItem() == null) {
                childrenToAdd.push(actionItem);
            } else {
                getTreeItem().getChildren().add(actionItem);
                getTreeItem().setExpanded(true);
                actionItem.select();
            }
        }
        save();
        return preferencesNode;
    }

    abstract protected Class getChildClass();

}
