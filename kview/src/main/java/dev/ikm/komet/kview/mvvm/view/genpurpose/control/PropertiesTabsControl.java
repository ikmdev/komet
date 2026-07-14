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
package dev.ikm.komet.kview.mvvm.view.genpurpose.control;

import javafx.beans.property.ObjectProperty;
import javafx.beans.property.SimpleObjectProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.scene.control.Control;
import javafx.scene.control.Skin;

/**
 * The row of tab toggle buttons shown at the top of a properties panel (ADD/EDIT, HIERARCHY,
 * INSTANCES, HISTORY, COMMENTS).
 * <p>
 * The {@link #getTabs() tabs} list controls which tab buttons are shown and in what order — add,
 * remove or reorder entries to change the visible tabs. It defaults to ADD_EDIT, INSTANCES,
 * HISTORY, COMMENTS.
 * <p>
 * At most one tab is selected at a time; a {@code null} {@link #selectedTabProperty() selectedTab}
 * means no tab is selected. Clicking the selected tab again keeps it selected — deselection is
 * only possible programmatically by setting {@code selectedTab} to {@code null}. The selection is
 * independent of the {@code tabs} list: a selected tab that is removed from the list stays
 * selected and shows as such when added back.
 */
public class PropertiesTabsControl extends Control {

    /**
     * The tabs that can be shown by this control.
     */
    public enum Tab {
        ADD_EDIT,
        HIERARCHY,
        INSTANCES,
        HISTORY,
        COMMENTS
    }

    public PropertiesTabsControl() {
        getStyleClass().add("properties-tabs");
    }

    @Override
    protected Skin<?> createDefaultSkin() {
        return new PropertiesTabsControlSkin(this);
    }

    /***************************************************************************
     *                                                                         *
     * Properties                                                              *
     *                                                                         *
     **************************************************************************/

    // -- tabs
    private final ObservableList<Tab> tabs = FXCollections.observableArrayList(
            Tab.ADD_EDIT, Tab.INSTANCES, Tab.HISTORY, Tab.COMMENTS);
    public ObservableList<Tab> getTabs() { return tabs; }

    // -- selected tab
    private final ObjectProperty<Tab> selectedTab = new SimpleObjectProperty<>(this, "selectedTab");
    public Tab getSelectedTab() { return selectedTab.get(); }
    public ObjectProperty<Tab> selectedTabProperty() { return selectedTab; }
    public void setSelectedTab(Tab tab) { selectedTab.set(tab); }
}