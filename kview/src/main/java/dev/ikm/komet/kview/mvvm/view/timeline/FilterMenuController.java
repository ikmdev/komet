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
package dev.ikm.komet.kview.mvvm.view.timeline;

import dev.ikm.komet.kview.mvvm.view.BasicController;
import dev.ikm.komet.framework.view.ViewProperties;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.layout.Pane;
import javafx.scene.layout.VBox;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;
import java.util.function.BiConsumer;

public class FilterMenuController implements BasicController {
    private static final Logger LOG = LoggerFactory.getLogger(FilterMenuController.class);

    @FXML
    private VBox filterMenuVbox;

    @FXML
    private VBox pathSelectionVBox;

    @FXML
    private VBox extensionSelectionVBox;

    @FXML
    private ToggleGroup selectedPathToggleGroup;

    @FXML
    private Button saveFilterMenu;

    private ViewProperties viewProperties;
    private TimelinePathMap pathMap;
    private BiConsumer<String, List<Integer>> pathAndModuleNids;

    @Override
    public void initialize() {
        pathSelectionVBox.getChildren().clear();
        extensionSelectionVBox.getChildren().clear();
        selectedPathToggleGroup.selectedToggleProperty().addListener(((observableValue, t0, t1) -> {
            getExtensionSelectionVBox().getChildren().clear();
            if (t1 != null &&
                    t1.isSelected() &&
                    t1 instanceof ToggleButton toggleButton){

                getPathMap().getModuleNids(toggleButton.getText()).forEach(moduleId ->{
                    // get module name
                    String moduleName = getViewProperties().calculator().getPreferredDescriptionTextWithFallbackOrNid(moduleId);
                    // set user data to be nid
                    CheckBox checkBox = new CheckBox(moduleName);
                    checkBox.setSelected(true);
                    checkBox.setUserData(moduleId);
                    getExtensionSelectionVBox().getChildren().add(checkBox);
                });
            };
        }));
    }

    public TimelinePathMap getPathMap() {
        return pathMap;
    }

    public VBox getExtensionSelectionVBox() {
        return extensionSelectionVBox;
    }

    public void setExtensionSelectionVBox(VBox extensionSelectionVBox) {
        this.extensionSelectionVBox = extensionSelectionVBox;
    }

    public ViewProperties getViewProperties() {
        return viewProperties;
    }

    public void setViewProperties(ViewProperties viewProperties) {
        this.viewProperties = viewProperties;
    }

    public void updateModel(ViewProperties viewProperties, TimelinePathMap pathMap){
        this.viewProperties = viewProperties;
        this.pathMap = pathMap;
    }

    @Override
    public void updateView() {

        clearView();

        pathMap.keySet().forEach(pathName -> {
            RadioButton pathRadioButton = new RadioButton(pathName);
            pathRadioButton.setToggleGroup(selectedPathToggleGroup);
            pathSelectionVBox.getChildren().add(pathRadioButton);
        });
        if (pathSelectionVBox.getChildren().size() > 0) {
            RadioButton defaultPath = (RadioButton) pathSelectionVBox.getChildren().get(0);
            defaultPath.setSelected(true);
        }

        // Set the default Path
        if (!pathSelectionVBox.getChildren().isEmpty()) {
            getExtensionSelectionVBox().getChildren().clear();
            RadioButton radioButton = (RadioButton) pathSelectionVBox.getChildren().get(0);
            radioButton.setToggleGroup(selectedPathToggleGroup);
            pathMap.getModuleNids(radioButton.getText()).forEach(moduleNid ->{
                // get module name
                String moduleName = getViewProperties().calculator().getPreferredDescriptionTextWithFallbackOrNid(moduleNid);
                // set user data to be nid
                CheckBox checkBox = new CheckBox(moduleName);
                checkBox.setSelected(true);
                checkBox.setUserData(moduleNid);
                getExtensionSelectionVBox().getChildren().add(checkBox);
            });
        }

    }

    @Override
    public void clearView() {
        pathSelectionVBox.getChildren().clear();
        extensionSelectionVBox.getChildren().clear();
    }

    @Override
    public void cleanup() {

    }
    @FXML
    void saveFilterMenu(ActionEvent event) {
        if (filterMenuVbox.getParent() != null) {
            Pane journalDesktop = (Pane) filterMenuVbox.getParent();
            journalDesktop.getChildren().remove(filterMenuVbox);

            RadioButton selected = (RadioButton)selectedPathToggleGroup.getSelectedToggle();
            if (selected != null) {
                String pathName = ((RadioButton) selectedPathToggleGroup.getSelectedToggle()).getText();
                List<Integer> moduleIds = getExtensionSelectionVBox()
                        .getChildren()
                        .stream()
                        .filter(node -> ((CheckBox)node).isSelected())
                        .map(node -> (Integer)node.getUserData())
                        .toList();
                pathAndModuleNids.accept(pathName, moduleIds);
            }

        }
    }

    public void onSaveAction(BiConsumer<String, List<Integer>> pathAndModuleNids) {
        this.pathAndModuleNids = pathAndModuleNids;
    }
}
