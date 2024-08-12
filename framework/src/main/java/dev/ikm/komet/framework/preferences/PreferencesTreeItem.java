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


import dev.ikm.tinkar.common.service.PluggableService;
import javafx.scene.control.TreeItem;
import dev.ikm.komet.framework.view.ViewProperties;
import dev.ikm.komet.preferences.KometPreferences;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.util.List;
import java.util.Optional;

import static dev.ikm.komet.framework.preferences.PreferenceGroup.Keys.CHILDREN_NODES;
import static dev.ikm.komet.framework.preferences.PreferenceGroup.Keys.PROPERTY_SHEET_CLASS;

/**
 * 
 */
public class PreferencesTreeItem extends TreeItem<PreferenceGroup> {
    private static final Logger LOG = LoggerFactory.getLogger(PreferencesTreeItem.class);
    ;
    final KometPreferencesController controller;
    KometPreferences preferences;

    private PreferencesTreeItem(PreferenceGroup value,
                                KometPreferences preferences, ViewProperties viewProperties, KometPreferencesController controller) {
        super(value);
        this.preferences = preferences;
        this.controller = controller;
        List<String> propertySheetChildren = preferences.getList(CHILDREN_NODES);
        for (String child : propertySheetChildren) {
            Optional<PreferencesTreeItem> childTreeItem = from(preferences.node(child), viewProperties, controller);
            if (childTreeItem.isPresent()) {
                getChildren().add(childTreeItem.get());
                childTreeItem.get().getValue().setTreeItem(childTreeItem.get());
            }
        }
    }

    static public Optional<PreferencesTreeItem> from(KometPreferences preferences,
                                                     ViewProperties viewProperties, KometPreferencesController controller) {
        Optional<String> optionalPropertySheetClass = preferences.get(PROPERTY_SHEET_CLASS);
        if (optionalPropertySheetClass.isPresent()) {
            try {
                String propertySheetClassName = optionalPropertySheetClass.get();

                Class preferencesSheetClass = PluggableService.forName(propertySheetClassName);
                Constructor<PreferenceGroup> c = preferencesSheetClass.getConstructor(
                        KometPreferences.class,
                        ViewProperties.class,
                        KometPreferencesController.class);
                PreferenceGroup preferencesSheet = c.newInstance(preferences, viewProperties, controller);
                PreferencesTreeItem preferencesTreeItem = new PreferencesTreeItem(preferencesSheet, preferences,
                        viewProperties, controller);
                preferencesSheet.setTreeItem(preferencesTreeItem);
                return Optional.of(preferencesTreeItem);
            } catch (ClassNotFoundException | NoSuchMethodException | SecurityException | InstantiationException | IllegalAccessException | IllegalArgumentException | InvocationTargetException ex) {
                PreferencesTreeItem.LOG.error("PropertySheetClass: " + optionalPropertySheetClass + " " + ex.getLocalizedMessage(), ex);
            }
        } else {
            preferences.put(PROPERTY_SHEET_CLASS, "dev.ikm.komet.framework.preferences.RootPreferences");
            return from(preferences, viewProperties, controller);
        }
        return Optional.empty();
    }

    public KometPreferences getPreferences() {
        return preferences;
    }

    public void setPreferences(KometPreferences preferences) {
        this.preferences = preferences;
    }

    public KometPreferencesController getController() {
        return controller;
    }

    public void select() {
        this.controller.getPreferenceTree().getSelectionModel().select(this);
    }

    public void removeChild(String uuid) {
        PreferenceGroup.removeChild(preferences, uuid);
    }

    @Override
    public String toString() {
        return getValue().getGroupName();
    }
}
