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

import javafx.beans.property.BooleanProperty;
import javafx.beans.property.SimpleBooleanProperty;
import javafx.beans.property.SimpleStringProperty;
import dev.ikm.komet.framework.propsheet.SheetItem;
import dev.ikm.komet.framework.view.ViewProperties;
import dev.ikm.komet.preferences.KometPreferences;
import dev.ikm.tinkar.terms.TinkarTerm;

import java.util.prefs.BackingStoreException;

import static dev.ikm.komet.framework.preferences.PreferenceGroup.Keys.GROUP_NAME;

/**
 * 
 */
public class ConfigurationPreferencePanel extends AbstractPreferences implements ConfigurationPreference {

    private final SimpleStringProperty nameProperty
            = new SimpleStringProperty(this, TinkarTerm.CONFIGURATION_NAME.toXmlFragment());
    private final BooleanProperty enableEdit = new SimpleBooleanProperty(this, TinkarTerm.ENABLE_EDITING.toXmlFragment());
    private final SimpleStringProperty datastoreLocationProperty
            = new SimpleStringProperty(this, TinkarTerm.DATASTORE_LOCATION.toXmlFragment());

    public ConfigurationPreferencePanel(KometPreferences preferencesNode, ViewProperties viewProperties,
                                        KometPreferencesController kpc) {
        super(preferencesNode, preferencesNode.get(GROUP_NAME, "KOMET"), viewProperties,
                kpc);
        this.nameProperty.set(groupNameProperty().get());
        this.enableEdit.setValue(preferencesNode.getBoolean(this.enableEdit.getName(), true));
        revertFields();
        save();
        getItemList().add(SheetItem.make(this.nameProperty));
        getItemList().add(SheetItem.make(this.enableEdit));
    }

    @Override
    protected void saveFields() throws BackingStoreException {
        getPreferencesNode().put(Keys.CONFIGURATION_NAME, this.nameProperty.get());
        getPreferencesNode().putBoolean(Keys.ENABLE_EDITING, this.enableEdit.get());
    }

    @Override
    final protected void revertFields() {
        this.nameProperty.set(getPreferencesNode().get(Keys.CONFIGURATION_NAME, getGroupName()));
        this.enableEdit.set(getPreferencesNode().getBoolean(Keys.ENABLE_EDITING, true));
    }

    public enum Keys {
        ENABLE_EDITING,
        CONFIGURATION_NAME,
    }


}
