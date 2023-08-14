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
package dev.ikm.komet.collection;

import javafx.application.Platform;
import javafx.beans.property.SimpleObjectProperty;
import javafx.collections.ListChangeListener;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.geometry.Pos;
import javafx.scene.Node;
import javafx.scene.control.Label;
import javafx.scene.control.MenuItem;
import javafx.scene.control.SeparatorMenuItem;
import javafx.scene.control.TextField;
import javafx.scene.input.Clipboard;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.HBox;
import javafx.stage.FileChooser;
import org.eclipse.collections.api.list.ImmutableList;
import org.eclipse.collections.api.list.primitive.MutableIntList;
import org.eclipse.collections.api.set.primitive.MutableIntSet;
import org.eclipse.collections.impl.factory.primitive.IntLists;
import org.eclipse.collections.impl.factory.primitive.IntSets;
import dev.ikm.komet.framework.ExplorationNodeAbstract;
import dev.ikm.komet.framework.TopPanelFactory;
import dev.ikm.komet.framework.activity.ActivityStream;
import dev.ikm.komet.framework.activity.ActivityStreamOption;
import dev.ikm.komet.framework.activity.ActivityStreams;
import dev.ikm.komet.framework.dnd.ClipboardHelper;
import dev.ikm.komet.framework.dnd.KometClipboard;
import dev.ikm.komet.framework.propsheet.editor.IntIdCollectionEditor;
import dev.ikm.komet.framework.propsheet.editor.IntIdListEditor;
import dev.ikm.komet.framework.view.ViewProperties;
import dev.ikm.komet.list.ListNode;
import dev.ikm.komet.list.ListNodeFactory;
import dev.ikm.komet.preferences.KometPreferences;
import dev.ikm.tinkar.common.alert.AlertStreams;
import dev.ikm.tinkar.common.id.*;
import dev.ikm.tinkar.common.util.uuid.UuidUtil;
import dev.ikm.tinkar.entity.Entity;
import dev.ikm.tinkar.terms.EntityFacade;
import dev.ikm.tinkar.terms.EntityProxy;
import dev.ikm.tinkar.terms.ProxyFactory;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardOpenOption;
import java.util.ArrayList;
import java.util.List;

public abstract class CollectionNode<T extends IntIdCollection> extends ExplorationNodeAbstract {
    private static final Logger LOG = LoggerFactory.getLogger(ListNode.class);
    public static final String STYLE_ID = "list-node";
    protected final SimpleObjectProperty<Path> collectionPathProperty = new SimpleObjectProperty<>();
    protected final SimpleObjectProperty<T> collectionItemsProperty = new SimpleObjectProperty<>();
    protected final SimpleObjectProperty<PublicIdStringKey<T>> collectionKeyProperty = new SimpleObjectProperty<>();
    private final BorderPane contentPane = new BorderPane();
    private final TextField listNameField = new TextField();
    private final HBox centerBox;
    private final IntIdCollectionEditor<T> collectionEditor;
    private final MenuItem saveMenuItem = new MenuItem("Save collection as...");


    public CollectionNode(ViewProperties viewProperties, KometPreferences nodePreferences) {
        super(viewProperties, nodePreferences);
        this.collectionEditor = getCollectionEditor(viewProperties, collectionItemsProperty);
        if (this.collectionEditor instanceof IntIdListEditor) {
            this.centerBox = new HBox(5, new Label("   List name: "), listNameField);
            this.collectionEditor.setValue((T) IntIds.list.empty());
            this.collectionKeyProperty.set(PublicIdStringKey.make("Temp list name"));
        } else {
            this.centerBox = new HBox(5, new Label("   Set name: "), listNameField);
            this.collectionEditor.setValue((T) IntIds.set.empty());
            this.collectionKeyProperty.set(PublicIdStringKey.make("Temp set name"));
        }
        this.listNameField.setText(this.collectionKeyProperty.getValue().getString());
        this.listNameField.setPrefColumnCount(40);
        this.listNameField.selectAll();
        this.listNameField.textProperty().addListener((observable, oldValue, newValue) -> {
            collectionKeyProperty.get().updateString(newValue);
        });
        this.centerBox.setAlignment(Pos.CENTER_LEFT);

        this.activityStreamKeyProperty.setValue(ActivityStreams.LIST);
        this.optionForActivityStreamKeyProperty.set(ActivityStreamOption.PUBLISH.keyForOption());
        Platform.runLater(() -> {
            TopPanelFactory.TopPanelParts topPanelParts = TopPanelFactory.make(viewProperties,
                    activityStreamKeyProperty, optionForActivityStreamKeyProperty, centerBox);
            this.contentPane.setTop(topPanelParts.topPanel());
            this.contentPane.setCenter(this.collectionEditor.getEditor());
            Platform.runLater(() -> {
                ArrayList<MenuItem> collectionMenuItems = new ArrayList<>();
                collectionMenuItems.add(new SeparatorMenuItem());
                MenuItem copySelectedItemsMenuItem = new MenuItem("Copy selected items");
                copySelectedItemsMenuItem.setOnAction(this::copyItems);
                collectionMenuItems.add(copySelectedItemsMenuItem);
                MenuItem pasteItemsMenuItem = new MenuItem("Paste clipboard items");
                pasteItemsMenuItem.setOnAction(this::pasteItems);
                collectionMenuItems.add(pasteItemsMenuItem);
                MenuItem deleteSelectedItemsMenuItem = new MenuItem("Delete selected items");
                deleteSelectedItemsMenuItem.setOnAction(this::deleteItems);
                collectionMenuItems.add(deleteSelectedItemsMenuItem);
                collectionMenuItems.add(new SeparatorMenuItem());

                MenuItem newCollectionMenuItem = new MenuItem("New collection");
                newCollectionMenuItem.setOnAction(this::newCollection);
                collectionMenuItems.add(newCollectionMenuItem);
                MenuItem openCollectionMenuItem = new MenuItem("Open collection");
                openCollectionMenuItem.setOnAction(this::openCollection);
                collectionMenuItems.add(openCollectionMenuItem);
                saveMenuItem.setOnAction(this::saveCollection);
                collectionMenuItems.add(saveMenuItem);
                MenuItem importCollectionMenuItem = new MenuItem("Import collection");
                importCollectionMenuItem.setOnAction(this::importCollection);
                collectionMenuItems.add(importCollectionMenuItem);

                ObservableList<MenuItem> topMenuItems = topPanelParts.viewPropertiesMenuButton().getItems();
                topMenuItems.addAll(collectionMenuItems);
            });
        });
        collectionEditor.getSelectionModel().getSelectedItems().addListener(this::onSelectionChanged);
    }

    protected abstract IntIdCollectionEditor<T> getCollectionEditor(ViewProperties viewProperties, SimpleObjectProperty<T> collectionItems);

    private void copyItems(ActionEvent actionEvent) {
        if (!collectionEditor.getSelectionModel().getSelectedIndices().isEmpty()) {
            List<EntityProxy> entityProxyList = new ArrayList<>();
            for (Integer nid : collectionEditor.getSelectionModel().getSelectedItems()) {
                entityProxyList.add(Entity.getFast(nid).toProxy());
            }
            KometClipboard content = new KometClipboard(entityProxyList);
            Clipboard.getSystemClipboard().setContent(content);
        }
    }

    private void pasteItems(ActionEvent actionEvent) {
        MutableIntList newItems = IntLists.mutable.empty();
        for (EntityProxy proxy : ClipboardHelper.getEntityProxyList()) {
            newItems.add(proxy.nid());
        }
        collectionEditor.setValue((T) collectionEditor.getValue().with(newItems.toArray()));
    }

    private void deleteItems(ActionEvent actionEvent) {
        if (!collectionEditor.getSelectionModel().getSelectedIndices().isEmpty()) {
            if (collectionEditor.getValue() instanceof IntIdSet idSet) {
                MutableIntSet mutableIntSet = IntSets.mutable.of(idSet.toArray());
                for (Integer nid : collectionEditor.getSelectionModel().getSelectedItems()) {
                    mutableIntSet.remove(nid);
                }
                collectionEditor.setValue((T) IntIds.set.of(mutableIntSet.toArray()));
            } else if (collectionEditor.getValue() instanceof IntIdList idList) {
                MutableIntList mutableIntList = IntLists.mutable.of(idList.toArray());
                List<Integer> indexesToRemove = new ArrayList<>(collectionEditor.getSelectionModel().getSelectedIndices());
                indexesToRemove.sort((x, y) -> Integer.compare(y, x));
                for (Integer index : indexesToRemove) {
                    mutableIntList.removeAtIndex(index);
                }
                collectionEditor.setValue((T) IntIds.list.of(mutableIntList.toArray()));
            }
        }
    }

    private void newCollection(ActionEvent actionEvent) {
        if (this.collectionEditor instanceof IntIdListEditor) {
            this.collectionEditor.setValue((T) IntIds.list.empty());
            this.listNameField.setText("Temp list name");
        } else {
            this.collectionEditor.setValue((T) IntIds.set.empty());
            this.listNameField.setText("Temp set name");
        }
        this.collectionKeyProperty.set(PublicIdStringKey.make(listNameField.getText()));
        this.collectionPathProperty.set(null);
        this.saveMenuItem.setText("Save collection as...");
        Platform.runLater(() -> this.listNameField.selectAll());
    }

    private void openCollection(ActionEvent actionEvent) {
        FileChooser fileChooser = new FileChooser();
        Path collectionsDirectory = Paths.get(System.getProperty("user.home"), "Solor", "collections");
        collectionsDirectory.toFile().mkdirs();
        fileChooser.setInitialDirectory(collectionsDirectory.toFile());
        FileChooser.ExtensionFilter filter = new FileChooser.ExtensionFilter(getCollectionType().userPrefix, "*" + getCollectionType().extension);
        fileChooser.getExtensionFilters().clear();
        fileChooser.getExtensionFilters().add(filter);
        fileChooser.setSelectedExtensionFilter(filter);
        File selectedFile = fileChooser.showOpenDialog(this.centerBox.getScene().getWindow());
        if (selectedFile != null) {
            if (!selectedFile.getName().endsWith(getCollectionType().extension)) {
                AlertStreams.dispatchToRoot(new IllegalStateException("Selected file has wrong extension. \n" +
                        "Found: " + selectedFile.getName() + "\n should be: " + getCollectionType().extension));
            } else {
                try (BufferedReader bufferedReader = Files.newBufferedReader(selectedFile.toPath(), StandardCharsets.UTF_8)) {
                    String collectionName = bufferedReader.readLine();
                    String collectionUuids = bufferedReader.readLine();
                    PublicId collectionId = PublicIds.of(UuidUtil.fromString(collectionUuids));
                    PublicIdStringKey<T> collectionStringKey = new PublicIdStringKey<>(collectionId, collectionName);
                    String proxyText = bufferedReader.readLine();
                    MutableIntList nidsToAdd = IntLists.mutable.empty();
                    while (proxyText != null) {
                        ProxyFactory.fromXmlFragmentOptional(proxyText).ifPresent(entityProxy ->
                                Entity.get(entityProxy.nid()).ifPresentOrElse(
                                        entity -> nidsToAdd.add(entity.nid()),
                                        () -> AlertStreams.dispatchToRoot(new IllegalStateException("No entity in database for: " + entityProxy))));
                        proxyText = bufferedReader.readLine();
                    }
                    collectionEditor.setValue((T) collectionEditor.getValue().with(nidsToAdd.toArray()));
                    this.collectionKeyProperty.set(collectionStringKey);
                    this.listNameField.setText(collectionName);
                    this.collectionPathProperty.set(selectedFile.toPath());
                } catch (IOException e) {
                    AlertStreams.dispatchToRoot(e);
                }
            }
        }
    }

    private void saveCollection(ActionEvent actionEvent) {
        if (collectionPathProperty.get() == null) {
            FileChooser fileChooser = new FileChooser();
            Path collectionsDirectory = Paths.get(System.getProperty("user.home"), "Solor", "collections");
            collectionsDirectory.toFile().mkdirs();
            fileChooser.setInitialDirectory(collectionsDirectory.toFile());
            fileChooser.setInitialFileName(this.listNameField.getText() + getCollectionType().extension);
            File selectedFile = fileChooser.showSaveDialog(this.centerBox.getScene().getWindow());
            if (selectedFile != null) {
                this.collectionPathProperty.set(selectedFile.toPath());
                this.saveMenuItem.setText("Save collection");
                saveCollection();
            }
        } else {
            saveCollection();
        }
    }

    private void importCollection(ActionEvent actionEvent) {
        FileChooser fileChooser = new FileChooser();
        Path collectionsDirectory = Paths.get(System.getProperty("user.home"), "Solor", "collections");
        collectionsDirectory.toFile().mkdirs();
        fileChooser.setInitialDirectory(collectionsDirectory.toFile());
        FileChooser.ExtensionFilter filter = new FileChooser.ExtensionFilter("collection",
                "*" + CollectionType.LIST.extension, "*" + CollectionType.SET.extension);
        fileChooser.getExtensionFilters().clear();
        fileChooser.getExtensionFilters().add(filter);
        fileChooser.setSelectedExtensionFilter(filter);
        File selectedFile = fileChooser.showOpenDialog(this.centerBox.getScene().getWindow());
        if (selectedFile != null) {
            if (!selectedFile.getName().endsWith(CollectionType.SET.extension) & !
                    selectedFile.getName().endsWith(CollectionType.LIST.extension)) {
                AlertStreams.dispatchToRoot(new IllegalStateException("Selected file has wrong extension. \n" +
                        "Found: " + selectedFile.getName() + "\n should be: " + CollectionType.SET.extension +
                        " or " + CollectionType.LIST.extension));
            } else {
                try (BufferedReader bufferedReader = Files.newBufferedReader(selectedFile.toPath(), StandardCharsets.UTF_8)) {
                    String collectionName = bufferedReader.readLine(); // ignore collectionName
                    String collectionUuids = bufferedReader.readLine(); // ignore collectionUuids
                    String proxyText = bufferedReader.readLine();
                    MutableIntList nidsToAdd = IntLists.mutable.empty();
                    while (proxyText != null) {
                        ProxyFactory.fromXmlFragmentOptional(proxyText).ifPresent(entityProxy ->
                                Entity.get(entityProxy.nid()).ifPresentOrElse(
                                        entity -> nidsToAdd.add(entity.nid()),
                                        () -> AlertStreams.dispatchToRoot(new IllegalStateException("No entity in database for: " + entityProxy))));
                        proxyText = bufferedReader.readLine();
                    }
                    collectionEditor.setValue((T) collectionEditor.getValue().with(nidsToAdd.toArray()));

                } catch (IOException e) {
                    AlertStreams.dispatchToRoot(e);
                }
            }
        }
    }

    private void onSelectionChanged(ListChangeListener.Change<? extends Integer> c) {

        if (this.getActivityStreamOption().equals(ActivityStreamOption.PUBLISH.keyForOption())) {
            ActivityStream activityStream = this.getActivityStream();
            if (activityStream != null) {
                if (!c.getList().isEmpty()) {
                    EntityFacade[] selectionArray = new EntityFacade[c.getList().size()];
                    int i = 0;
                    for (Integer nid : c.getList()) {
                        selectionArray[i++] = Entity.getFast(nid);
                    }
                    activityStream.dispatch(selectionArray);
                    LOG.atTrace().log("Selected: " + c.getList());
                }
            }
        }

    }

    protected abstract CollectionType getCollectionType();

    private void saveCollection() {
        try (BufferedWriter writer = Files.newBufferedWriter(this.collectionPathProperty.get(), StandardCharsets.UTF_8,
                StandardOpenOption.CREATE, StandardOpenOption.WRITE)) {
            writer.append(collectionKeyProperty.getValue().getString());
            writer.newLine();
            writer.write(UuidUtil.toString(collectionKeyProperty.get().getPublicId().asUuidArray()));
            writer.newLine();
            for (int nid : this.collectionEditor.getItems()) {
                writer.write(Entity.getFast(nid).toXmlFragment());
                writer.newLine();
            }
        } catch (IOException e) {
            AlertStreams.dispatchToRoot(e);
        }
    }

    @Override
    public void handleActivity(ImmutableList<EntityFacade> entities) {
        // Nothing to do...
    }

    @Override
    public void revertAdditionalPreferences() {

    }

    public Node getMenuIconGraphic() {
        Label menuIcon = new Label("(…)");
        return menuIcon;
    }

    @Override
    public String getStyleId() {
        return STYLE_ID;
    }

    @Override
    protected void saveAdditionalPreferences() {

    }

    @Override
    public Node getNode() {
        return contentPane;
    }

    @Override
    public void close() {

    }

    @Override
    public boolean canClose() {
        return false;
    }

    @Override
    public Class factoryClass() {
        return ListNodeFactory.class;
    }
}