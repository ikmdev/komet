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
package dev.ikm.komet.amplify.stamp;

import dev.ikm.komet.amplify.commons.AbstractBasicController;
import dev.ikm.komet.amplify.mvvm.ViewModel;
import dev.ikm.komet.amplify.mvvm.loader.InjectViewModel;
import dev.ikm.komet.amplify.viewmodels.DescrNameViewModel;
import dev.ikm.tinkar.entity.ConceptEntity;
import javafx.beans.property.ObjectProperty;
import javafx.fxml.FXML;
import javafx.geometry.NodeOrientation;
import javafx.scene.control.RadioButton;
import javafx.scene.control.TitledPane;
import javafx.scene.control.ToggleGroup;
import javafx.scene.layout.VBox;

import java.util.List;

import static dev.ikm.komet.amplify.viewmodels.StampViewModel.*;

public class StampEditController extends AbstractBasicController {

    @FXML
    private TitledPane moduleTitledPane;

    @FXML
    private ToggleGroup moduleToggleGroup;

    @FXML
    private VBox moduleVBox;

    @FXML
    private TitledPane pathTitledPane;

    @FXML
    private ToggleGroup pathToggleGroup;

    @FXML
    private VBox pathVBox;

    @FXML
    private TitledPane statusTitledPane;

    @FXML
    private ToggleGroup statusToggleGroup;

    @FXML
    private VBox statusVBox;

    //////////////// private variables ///////////////////////////
    @InjectViewModel
    private ViewModel stampViewModel;

    @FXML
    @Override
    public void initialize() {
        clearView();
        // setup modules and path radio button selection
        setupModuleSelections();
        setupPathSelections();

        // When user selects a radio button
        moduleToggleGroup.selectedToggleProperty().addListener((observableValue, toggle, t1) -> {
            ConceptEntity module = (ConceptEntity) t1.getUserData();
            getStampViewModel().setPropertyValue(MODULE_PROPERTY, module);
            if (module != null) {
                moduleTitledPane.setText("Module: " + module.description());
            }
        });

        pathToggleGroup.selectedToggleProperty().addListener(((observableValue, toggle, t1) -> {
            ConceptEntity path = (ConceptEntity) t1.getUserData();
            getStampViewModel().setPropertyValue(PATH_PROPERTY, path);
            if (path != null) {
                pathTitledPane.setText("Path: " + path.description());
            }

        }));
    }

    private void setupModuleSelections() {
        // populate modules
        List<ConceptEntity> mods = stampViewModel.getObservableList(MODULES_PROPERTY);
        mods.forEach(module -> {
            RadioButton rb = new RadioButton(module.description());
            rb.setNodeOrientation(NodeOrientation.LEFT_TO_RIGHT);
            rb.setUserData(module);
            rb.setToggleGroup(moduleToggleGroup);
            ObjectProperty<ConceptEntity> moduleProperty = getStampViewModel().getProperty(MODULE_PROPERTY);
            if (moduleProperty.isNotNull().get() && moduleProperty.get().nid() == module.nid()) {
                rb.setSelected(true);
            }
            moduleVBox.getChildren().add(rb);
        });
    }
    private void setupPathSelections() {
        // populate paths
        List<ConceptEntity> paths = stampViewModel.getObservableList(PATHS_PROPERTY);
        paths.forEach(path -> {
            RadioButton rb = new RadioButton(path.description());
            rb.setNodeOrientation(NodeOrientation.LEFT_TO_RIGHT);
            rb.setUserData(path);
            rb.setToggleGroup(pathToggleGroup);
            ObjectProperty<ConceptEntity> pathProperty = getStampViewModel().getProperty(PATH_PROPERTY);
            if (pathProperty.isNotNull().get() && pathProperty.get().nid() == path.nid()) {
                rb.setSelected(true);
            }
            pathVBox.getChildren().add(rb);
        });
    }

    private ViewModel getStampViewModel() {
        return stampViewModel;
    }

    @Override
    public DescrNameViewModel getViewModel() {
        return (DescrNameViewModel) stampViewModel;
    }

    @Override
    public void updateView() {

    }

    @Override
    public void clearView() {
        // collapse titled panes
        statusTitledPane.setExpanded(false);
        pathTitledPane.setExpanded(false);
        moduleTitledPane.setExpanded(false);

        // unselect any entries
        statusToggleGroup.selectToggle(null);
        pathToggleGroup.selectToggle(null);
        moduleToggleGroup.selectToggle(null);

        // clear vboxes
        pathVBox.getChildren().clear();
        moduleVBox.getChildren().clear();
    }

    @Override
    public void cleanup() {
    }
}
