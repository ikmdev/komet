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

import dev.ikm.komet.framework.view.ViewProperties;
import dev.ikm.komet.kview.mvvm.viewmodel.TimelineViewModel;
import dev.ikm.komet.kview.mvvm.viewmodel.TimelineViewModel.*;
import javafx.beans.property.SimpleObjectProperty;
import javafx.beans.property.SimpleStringProperty;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.layout.VBox;
import javafx.util.Subscription;
import org.carlfx.cognitive.loader.InjectViewModel;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class FilterMenuController {
    private static final Logger LOG = LoggerFactory.getLogger(FilterMenuController.class);

    @FXML private VBox filterMenuVbox;

    @FXML private VBox pathSelectionVBox;
    @FXML private VBox extensionSelectionVBox;

    @FXML private ToggleGroup selectedPathToggleGroup;

    @FXML private Button closeFilterMenu;

    @InjectViewModel
    TimelineViewModel timelineViewModel;

    @FXML
    public void initialize() {

        // This mini "local state" is a compromise -> otherwise the ViewModel would need to know about CheckBox
        List<Subscription> checkBoxSubscriptionsList = new ArrayList<>();
        Map<RadioButton, List<CheckBox>> checkBoxMap = new HashMap<>();


        SimpleObjectProperty<Map<String, List<Integer>>> pathModulesMapProp = timelineViewModel.getProperty(TimelineProperties.AVAILABLE_PATH_MOULES_MAP);
        SimpleStringProperty pathName =  timelineViewModel.getProperty(TimelineProperties.SELECTED_PATH);
        ObservableList<Integer> moduleIDs = timelineViewModel.getObservableList(TimelineProperties.CHECKED_MODULE_IDS);

        SimpleObjectProperty<ViewProperties> viewPropertiesProperty = timelineViewModel.getProperty(TimelineProperties.VIEW_PROPERTIES);

        // programmatically creating all viable elements

        // ViewModel -> View
        pathModulesMapProp.subscribe( map -> {

            final boolean[] first = {true};

            map.forEach( (path, modules) -> {
                RadioButton pathRadioButton = new RadioButton(path);
                pathRadioButton.setToggleGroup(selectedPathToggleGroup);

                pathSelectionVBox.getChildren().add(pathRadioButton);

                List<CheckBox> modulesVBox = new ArrayList<>();

                modules.forEach( module -> {
                    ViewProperties viewProperties = viewPropertiesProperty.getValue();
                    String moduleName = viewProperties.calculator().getPreferredDescriptionTextWithFallbackOrNid(module);
                    CheckBox moduleCheckBox = new CheckBox(moduleName);
                    moduleCheckBox.setUserData(module);
                    moduleCheckBox.setSelected(true);

                    modulesVBox.add(moduleCheckBox);
                });

                checkBoxMap.put(pathRadioButton, modulesVBox);

                // by default the first pathRadioButton is selected
                if (first[0]) {
                    pathRadioButton.setSelected(true);
                    first[0] = false;
                }
            } ) ;

        });



        selectedPathToggleGroup.selectedToggleProperty().subscribe( (newToggle) -> {
            if (newToggle instanceof RadioButton radioButton && newToggle.isSelected()) {
                extensionSelectionVBox.getChildren().clear();

                checkBoxSubscriptionsList.forEach(Subscription::unsubscribe);
                checkBoxSubscriptionsList.clear();

                // get associated checkboxes
                List<CheckBox> modulesCheckBoxList = checkBoxMap.get(radioButton);
                // set up a subscriber to any checkbox in the view
                // that triggers on any checkBox update
                modulesCheckBoxList.forEach( checkBox -> {
                    Subscription sub = checkBox.selectedProperty().subscribe(() -> {
                        LOG.info("toggled checkBox with number {} with nid {}", checkBox.getText(), checkBox.getUserData());
                        // calculate a new list and provide it to the ViewModel
                        List<Integer> selectedModules = getSelectedModules(extensionSelectionVBox);
                        moduleIDs.setAll(selectedModules);
                    });
                    checkBoxSubscriptionsList.add(sub);


                });
                extensionSelectionVBox.getChildren().setAll(modulesCheckBoxList);

                // On path trigger we need to make sure to also update the selectionBox **once** to that path in the ViewModel
                pathName.setValue(radioButton.getText());
                List<Integer> selectedModules = getSelectedModules(extensionSelectionVBox);
                moduleIDs.setAll(selectedModules);

            }
        });
    }

    private List<Integer> getSelectedModules(VBox checkBoxesVBox) {
        return checkBoxesVBox.getChildren().stream()
                .filter(node -> node instanceof CheckBox)
                .map(node -> (CheckBox) node)
                .filter(CheckBox::isSelected)
                .map(cb -> (Integer) cb.getUserData())
                .toList();
    }

    @FXML
    void onClose() {
        timelineViewModel.setPropertyValue(TimelineProperties.FILTER_POP_UP_VISIBLE, false);
        LOG.info("Hide PopUp");
    }


}
