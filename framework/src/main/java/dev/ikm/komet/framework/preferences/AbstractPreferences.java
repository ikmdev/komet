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
import javafx.beans.value.ObservableValue;
import javafx.beans.value.WeakChangeListener;
import javafx.collections.FXCollections;
import javafx.collections.ListChangeListener;
import javafx.collections.ObservableList;
import javafx.collections.WeakListChangeListener;
import javafx.event.ActionEvent;
import javafx.scene.Node;
import javafx.scene.control.Button;
import javafx.scene.control.ToolBar;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.Region;
import org.controlsfx.control.PropertySheet;
import dev.ikm.komet.framework.propsheet.KometPropertyEditorFactory;
import dev.ikm.komet.framework.propsheet.KometPropertySheet;
import dev.ikm.komet.framework.view.ObservableViewWithOverride;
import dev.ikm.komet.framework.view.ViewProperties;
import dev.ikm.komet.preferences.KometPreferences;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;
import java.util.Optional;
import java.util.UUID;
import java.util.prefs.BackingStoreException;

import static dev.ikm.komet.framework.preferences.PreferenceGroup.Keys.*;

/**
 * 
 */
public abstract class AbstractPreferences implements PreferenceGroup {

    private static final Logger LOG = LoggerFactory.getLogger(AbstractPreferences.class);

    protected final KometPreferences preferencesNode;
    protected final KometPreferencesController kpc;
    private final BooleanProperty initialized = new SimpleBooleanProperty(this, INITIALIZED.toString());
    private final BooleanProperty changed = new SimpleBooleanProperty(this, "changed", false);
    private final SimpleStringProperty groupNameProperty = new SimpleStringProperty(this, "group name");
    private final ObservableList<PropertySheet.Item> itemList = FXCollections.observableArrayList();
    private final ViewProperties viewProperties;
    private final Button revertButton = new Button("Revert");
    private final Button saveButton = new Button("Save");
    private final Button deleteButton = new Button("Delete");
    private final BorderPane propertySheetBorderPane = new BorderPane();
    protected PreferencesTreeItem treeItem;
    Region spacer = new Region();
    Region spacer2 = new Region();
    ToolBar bottomBar = new ToolBar(spacer, revertButton, saveButton, spacer2, deleteButton);
    private PropertySheet propertySheet;

    {
        itemList.addListener((ListChangeListener.Change<? extends PropertySheet.Item> c) -> {
            makePropertySheet();
        });
    }

    {
        deleteButton.setOnAction(this::deleteSelf);

        revertButton.setOnAction((event) -> {
            revert();
            changed.setValue(Boolean.FALSE);
        });
        revertButton.setDisable(true);

        saveButton.setOnAction((event) -> {
            save();
            changed.setValue(Boolean.FALSE);
        });
        saveButton.setDisable(true);

        changed.addListener((observable, oldValue, newValue) -> {
            if (newValue == true) {
                revertButton.setDisable(false);
                saveButton.setDisable(false);
            } else {
                revertButton.setDisable(true);
                saveButton.setDisable(true);
            }
        });
    }

    {
        HBox.setHgrow(spacer, Priority.ALWAYS);
        HBox.setHgrow(spacer2, Priority.ALWAYS);
    }

    public AbstractPreferences(KometPreferences preferencesNode, String groupName, ViewProperties viewProperties,
                               KometPreferencesController kpc) {
        if (preferencesNode == null) {
            throw new NullPointerException("preferencesNode cannot be null.");
        }
        if (groupName == null) {
            throw new NullPointerException("groupName cannot be null.");
        }
        if (viewProperties == null) {
            throw new NullPointerException("Manifold cannot be null.");
        }
        if (viewProperties.nodeView().languageCoordinates() == null) {
            throw new NullPointerException("Manifold.getLanguageCoordinate() cannot be null.");
        }
        if (viewProperties.nodeView().logicCoordinate() == null) {
            throw new NullPointerException("Manifold.getLogicCoordinate() cannot be null.");
        }
        if (kpc == null) {
            throw new NullPointerException("KometPreferencesController cannot be null.");
        }
        this.preferencesNode = preferencesNode;
        this.initialized.setValue(preferencesNode.getBoolean(INITIALIZED, false));
        this.groupNameProperty.set(groupName);
        this.viewProperties = viewProperties;
        this.kpc = kpc;
        this.groupNameProperty.addListener(this::changeGroupName);
    }

    private void changeGroupName(ObservableValue<? extends String> observable, String oldValue, String newValue) {
        if (treeItem != null) {
            PreferenceGroup group = treeItem.getValue();
            group.groupNameProperty().setValue(newValue);
            treeItem.setValue(null);
            treeItem.setValue(group);
        }
    }

    protected static final String getGroupName(KometPreferences preferencesNode) {
        return preferencesNode.get(PreferenceGroup.Keys.GROUP_NAME, UUID.randomUUID().toString());
    }

    protected static final String getGroupName(KometPreferences preferencesNode, String defaultValue) {
        return preferencesNode.get(PreferenceGroup.Keys.GROUP_NAME, defaultValue);
    }

    protected void deleteSelf(ActionEvent event) {
        try {
            PreferencesTreeItem parentTreeItem = (PreferencesTreeItem) this.treeItem.getParent();
            ParentPanel parentPanel = (ParentPanel) parentTreeItem.getValue();
            parentPanel.removeChild(this);
            parentPanel.save();

            this.getPreferencesNode().removeNode();
            this.treeItem.getParent().getChildren().remove(this.treeItem);

        } catch (BackingStoreException ex) {
            throw new RuntimeException(ex.getLocalizedMessage(), ex);
        }
    }

    public final KometPreferences getPreferencesNode() {
        return preferencesNode;
    }

    protected abstract void saveFields() throws BackingStoreException;

    protected final KometPreferences addChild(String childName, Class<? extends AbstractPreferences> childPreferencesClass) {
        KometPreferences childNode = this.preferencesNode.node(childName);
        childNode.put(PROPERTY_SHEET_CLASS, childPreferencesClass.getName());
        List<String> childPreferences = this.preferencesNode.getList(CHILDREN_NODES);
        if (!childPreferences.contains(childName)) {
            childPreferences.add(childName);
        }

        this.preferencesNode.putList(CHILDREN_NODES, childPreferences);
        return childNode;
    }

    @Override
    public String toString() {
        return getGroupName();
    }

    public final List<PropertySheet.Item> getItemList() {
        return itemList;
    }

    @Override
    public Node getCenterPanel(ViewProperties viewProperties) {
        if (this.propertySheet == null) {
            makePropertySheet();
        }
        return this.propertySheetBorderPane;
    }

    protected void makePropertySheet() {
        KometPropertySheet sheet = new KometPropertySheet(this.viewProperties);
        sheet.setMode(PropertySheet.Mode.NAME);
        sheet.setSearchBoxVisible(false);
        sheet.setModeSwitcherVisible(false);
        sheet.setPropertyEditorFactory(new KometPropertyEditorFactory(viewProperties));
        sheet.getItems().addAll(itemList);
        for (PropertySheet.Item item : itemList) {
            if (item instanceof PreferenceChanged) {
                PreferenceChanged preferenceChangedItem = (PreferenceChanged) item;
                preferenceChangedItem.changedProperty().addListener((obs, oldValue, newValue) -> {
                    if (newValue) {
                        changed.set(true);
                        preferenceChangedItem.changedProperty().set(false);
                    }
                });
            } else {
                Optional<ObservableValue<? extends Object>> observable = item.getObservableValue();
                if (observable.isPresent()) {
                    observable.get().addListener((obs, oldValue, newValue) -> {
                        validateChange(oldValue, newValue);
                    });
                }
            }
        }
        this.propertySheetBorderPane.setCenter(sheet);
    }

    private void validateChange(Object oldValue, Object newValue) {
        if (oldValue != newValue) {
            if (newValue != null) {
                if (!newValue.equals(oldValue)) {
                    changed.set(true);
                }
            } else {
                changed.set(true);
            }
        }
    }

    @Override
    public Node getRightPanel(ViewProperties viewProperties) {
        return null;
    }

    @Override
    public Node getTopPanel(ViewProperties viewProperties) {
        return null;
    }

    @Override
    public Node getBottomPanel(ViewProperties viewProperties) {
        if (showRevertAndSave()) {
            if (showDelete()) {
                bottomBar = new ToolBar(deleteButton, spacer, revertButton, saveButton);
            } else {
                bottomBar = new ToolBar(spacer, revertButton, saveButton);
            }
            return this.bottomBar;
        }
        return null;
    }

    @Override
    public Node getLeftPanel(ViewProperties viewProperties) {
        return null;
    }

    @Override
    public final String getGroupName() {
        return groupNameProperty.get();
    }

    @Override
    public final SimpleStringProperty groupNameProperty() {
        return groupNameProperty;
    }

    @Override
    public final void save() {
        try {
            initialized.set(true);
            preferencesNode.putBoolean(INITIALIZED, initialized.get());
            preferencesNode.putEnum(preferencesNode.getNodeType());
            saveFields();
            preferencesNode.sync();
            preferencesNode.flush();
        } catch (BackingStoreException ex) {
            throw new RuntimeException(ex);
        }
    }

    @Override
    public final void revert() {
        try {
            initialized.setValue(preferencesNode.getBoolean(INITIALIZED, false));
            preferencesNode.putEnum(preferencesNode.getNodeType());
            revertFields();
        } catch (BackingStoreException ex) {
            throw new RuntimeException(ex);
        }
    }

    @Override
    public final boolean initialized() {
        return initialized.get();
    }

    @Override
    public PreferencesTreeItem getTreeItem() {
        return treeItem;
    }

    @Override
    public void setTreeItem(PreferencesTreeItem treeItem) {
        this.treeItem = treeItem;
        this.treeItem.setPreferences(this.preferencesNode);
        addChildren();
    }

    protected void addChildren() {
        // Override if node adds children to tree.
    }

    protected abstract void revertFields() throws BackingStoreException;

    public final void setGroupName(String groupName) {
        this.groupNameProperty.set(groupName);
    }

    /**
     * Override for panels that have not state, such as parentTreeItem panels
     * with no fields.
     *
     * @return true if the revert and save buttons should be shown.
     */
    public boolean showRevertAndSave() {
        return true;
    }

    public boolean showDelete() {
        return false;
    }

    protected final void addProperty(ObservableValue<?> observableValue) {
        observableValue.addListener(new WeakChangeListener<>((observable, oldValue, newValue) -> {
            validateChange(oldValue, newValue);
        }));
    }

    protected final void addProperty(ObservableList<? extends Object> observableList) {
        observableList.addListener(new WeakListChangeListener<>((ListChangeListener.Change<? extends Object> c) -> {
            changed.set(true);
        }));
    }

    public ObservableViewWithOverride getView() {
        return viewProperties.nodeView();
    }

    public ViewProperties getViewProperties() {
        return viewProperties;
    }

}
