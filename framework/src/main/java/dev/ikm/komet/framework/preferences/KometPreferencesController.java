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

import javafx.beans.value.ObservableValue;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.TreeItem;
import javafx.scene.control.TreeView;
import javafx.scene.layout.BorderPane;
import dev.ikm.komet.framework.view.ViewProperties;

import java.net.URL;
import java.util.ResourceBundle;

/**
 * FXML Controller class
 *
 * 
 */
public class KometPreferencesController implements Initializable {


    ViewProperties viewProperties;
    @FXML // ResourceBundle that was given to the FXMLLoader
    private ResourceBundle resources;
    @FXML // URL location of the FXML file that was given to the FXMLLoader
    private URL location;
    @FXML // fx:id="preferenceTree"
    private TreeView<PreferenceGroup> preferenceTree; // Value injected by FXMLLoader
    @FXML // fx:id="detailBorderPane"
    private BorderPane detailBorderPane; // Value injected by FXMLLoader

    /**
     * Initializes the view class.
     */
    @FXML // This method is called by the FXMLLoader when initialization is complete
    @Override
    public void initialize(URL location, ResourceBundle resources) {
        this.resources = resources;
        this.location = location;

        assert preferenceTree != null : "fx:id=\"preferenceTree\" was not injected: check your FXML file 'KometPreferences.fxml'.";
        assert detailBorderPane != null : "fx:id=\"detailBorderPane\" was not injected: check your FXML file 'KometPreferences.fxml'.";

        preferenceTree.showRootProperty().setValue(Boolean.FALSE);
        preferenceTree.getSelectionModel().selectedItemProperty().addListener(this::selectionChanged);
    }

    private void selectionChanged(ObservableValue<? extends TreeItem<PreferenceGroup>> observable,
                                  TreeItem<PreferenceGroup> oldValue,
                                  TreeItem<PreferenceGroup> newValue) {
        ObservableList<TreeItem<PreferenceGroup>> selectedItems = preferenceTree.getSelectionModel().getSelectedItems();
        if (selectedItems.isEmpty()) {
            detailBorderPane.setCenter(null);
            detailBorderPane.setTop(null);
            detailBorderPane.setBottom(null);
            detailBorderPane.setLeft(null);
            detailBorderPane.setRight(null);
        } else {
            TreeItem<PreferenceGroup> selectedItem = selectedItems.get(0);
            detailBorderPane.setCenter(selectedItem.getValue().getCenterPanel(this.viewProperties));
            detailBorderPane.setTop(selectedItem.getValue().getTopPanel(this.viewProperties));
            detailBorderPane.setBottom(selectedItem.getValue().getBottomPanel(this.viewProperties));
            detailBorderPane.setLeft(selectedItem.getValue().getLeftPanel(this.viewProperties));
            detailBorderPane.setRight(selectedItem.getValue().getRightPanel(this.viewProperties));
        }
    }

    public void setRoot(PreferencesTreeItem root) {
        preferenceTree.setRoot(root);
        if (!root.getChildren().isEmpty()) {
            preferenceTree.getSelectionModel().select(root.getChildren().get(0));
        }
    }

    public void updateDetail() {
        ObservableList<TreeItem<PreferenceGroup>> selectedItems = preferenceTree.getSelectionModel().getSelectedItems();
        if (selectedItems.isEmpty()) {
            detailBorderPane.setCenter(null);
            detailBorderPane.setTop(null);
            detailBorderPane.setBottom(null);
            detailBorderPane.setLeft(null);
            detailBorderPane.setRight(null);
        } else {
            TreeItem<PreferenceGroup> selectedItem = selectedItems.get(0);
            detailBorderPane.setCenter(selectedItem.getValue().getCenterPanel(this.viewProperties));
            detailBorderPane.setTop(selectedItem.getValue().getTopPanel(this.viewProperties));
            detailBorderPane.setBottom(selectedItem.getValue().getBottomPanel(this.viewProperties));
            detailBorderPane.setLeft(selectedItem.getValue().getLeftPanel(this.viewProperties));
            detailBorderPane.setRight(selectedItem.getValue().getRightPanel(this.viewProperties));
        }
    }

    public TreeView<PreferenceGroup> getPreferenceTree() {
        return preferenceTree;
    }

    public void setViewProperties(ViewProperties viewProperties) {
        this.viewProperties = viewProperties;
    }
}
