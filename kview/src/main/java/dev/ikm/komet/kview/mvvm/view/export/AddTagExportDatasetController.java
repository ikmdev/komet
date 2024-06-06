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
package dev.ikm.komet.kview.mvvm.view.export;

import dev.ikm.komet.kview.mvvm.view.BasicController;
import dev.ikm.komet.framework.events.EvtBus;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.geometry.NodeOrientation;
import javafx.scene.control.Button;
import javafx.scene.control.CheckBox;
import javafx.scene.control.Label;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Pane;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;

import java.util.ArrayList;
import java.util.List;

public class AddTagExportDatasetController implements BasicController{

    @FXML
    private Button addButton;
    @FXML
    private Button cancelButton;

    @FXML
    private CheckBox tinkarStarterSetCheckbox;
    @FXML
    private CheckBox ikmSetCheckbox;
    @FXML
    private CheckBox oregonHealthSetCheckbox;
    @FXML
    Pane checkboxPane;
    @FXML
    HBox addTagHBox;

    @FXML
    VBox membershipTagsVBox;

    private ArrayList<CheckBox> checkBoxArrayList = new ArrayList<>();
    private EvtBus evtBusExportDataset;


    @Override
    public void initialize() {
        setupMembershipTag();
    }

    @Override
    public void updateView() {

    }

    @Override
    public void clearView() {
        membershipTagsVBox.getChildren().clear();

    }

    @Override
    public void cleanup() {

    }


    @FXML
    private void handleCancelButtonEvent(ActionEvent event) {
        Stage stage = (Stage) cancelButton.getScene().getWindow();
        stage.close();
    }
    public void setupMembershipTag() {

        // TODO Data API is needed to get membership tags
        List<String> membershipTags = List.of("Tag 2", "Tag 3", "Tag 4");
        for (String memTags : membershipTags) {
            CheckBox membershipTagsCheckBox = new CheckBox(memTags);
            membershipTagsCheckBox.setNodeOrientation(NodeOrientation.LEFT_TO_RIGHT);
            membershipTagsCheckBox.setUserData(memTags);
            membershipTagsVBox.getChildren().add(membershipTagsCheckBox);

            membershipTagsCheckBox.selectedProperty().addListener((observable, oldValues, newValue) -> {
                        Label datasetLabel = new Label(membershipTagsCheckBox.getText());
                        if (newValue) {
                            checkBoxArrayList.add(membershipTagsCheckBox);
                            datasetLabel.setStyle("-fx-background-color: lightgray; -fx-border-color: black; -fx-border-width: 1;");
                            addTagHBox.getChildren().add(datasetLabel);
                        } else {
                             addTagHBox.getChildren().removeIf(node -> node instanceof Label && ((Label)node).getText().equals(membershipTagsCheckBox.getText()));
                             checkBoxArrayList.remove(membershipTagsCheckBox);
                        }
                    }
            );
        }
//        addButton.setOnAction(event -> {
//            SharedModel.getInstance().getSelectedValues().clear();
//            for (CheckBox datasetCheckBoxSelected : checkBoxArrayList) {
//                if (!SharedModel.getInstance().getSelectedValues().contains(datasetCheckBoxSelected.getText())) {
//                    SharedModel.getInstance().getSelectedValues().add(datasetCheckBoxSelected.getText());
//                }
//            }
//            ((Stage) checkboxPane.getScene().getWindow()).close();
//        });
    }

}
