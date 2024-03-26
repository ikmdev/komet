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
package dev.ikm.komet.amplify.details;

import dev.ikm.komet.amplify.commons.BasicController;
import dev.ikm.komet.amplify.mvvm.ViewModel;
import dev.ikm.komet.framework.view.ViewProperties;
import dev.ikm.tinkar.coordinate.view.calculator.ViewCalculator;
import dev.ikm.tinkar.entity.ConceptEntity;
import javafx.beans.property.IntegerProperty;
import javafx.fxml.FXML;
import javafx.geometry.NodeOrientation;
import javafx.scene.control.RadioButton;
import javafx.scene.control.TitledPane;
import javafx.scene.control.ToggleGroup;
import javafx.scene.layout.VBox;

import java.util.List;

import static dev.ikm.komet.amplify.details.StampViewModel.*;

public class StampEditController implements BasicController {

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
    private ViewProperties viewProperties;
    private ViewModel stampViewModel;

    @FXML
    @Override
    public void initialize() {
        clearView();
        moduleToggleGroup.selectedToggleProperty().addListener((observableValue, toggle, t1) -> {
            getStampViewModel().setPropertyValue(MODULE_PROPERTY, t1.getUserData());
            String description = getViewProperties().calculator().getPreferredDescriptionTextWithFallbackOrNid((int)t1.getUserData());
            moduleTitledPane.setText("Module: " + description);
        });

        pathToggleGroup.selectedToggleProperty().addListener(((observableValue, toggle, t1) -> {
            getStampViewModel().setPropertyValue(PATH_PROPERTY, t1.getUserData());
            String description = getViewProperties().calculator().getPreferredDescriptionTextWithFallbackOrNid((int)t1.getUserData());
            pathTitledPane.setText("Path: " + description);

        }));
    }
    private ViewModel getStampViewModel() {
        return stampViewModel;
    }
    private ViewProperties getViewProperties() {
        return viewProperties;
    }
    public void updateModel(ViewProperties viewProperties, ViewModel stampViewModel) {
        this.viewProperties = viewProperties;
        this.stampViewModel = stampViewModel;
    }

    @Override
    public void updateView() {
        ViewCalculator vc = viewProperties.calculator();

        // populate paths
        List<ConceptEntity> paths = stampViewModel.getObservableList(PATHS_PROPERTY);
        paths.forEach(path -> {
            RadioButton rb = new RadioButton(vc.getPreferredDescriptionStringOrNid(path.nid()));
            rb.setNodeOrientation(NodeOrientation.LEFT_TO_RIGHT);
            rb.setUserData(path.nid());
            rb.setToggleGroup(pathToggleGroup);
            IntegerProperty pathNid = getStampViewModel().getProperty(PATH_PROPERTY);
            if (pathNid.get() == path.nid()) {
                rb.setSelected(true);
            }
            pathVBox.getChildren().add(rb);
        });

        // populate modules
        List<ConceptEntity> mods = stampViewModel.getObservableList(MODULES_PROPERTY);
        mods.forEach(module -> {
            RadioButton rb = new RadioButton(vc.getPreferredDescriptionStringOrNid(module.nid()));
            rb.setNodeOrientation(NodeOrientation.LEFT_TO_RIGHT);
            rb.setUserData(module.nid());
            rb.setToggleGroup(moduleToggleGroup);
            IntegerProperty moduleNid = getStampViewModel().getProperty(MODULE_PROPERTY);
            if (moduleNid.get() == module.nid()) {
                rb.setSelected(true);
            }
            moduleVBox.getChildren().add(rb);
        });
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
