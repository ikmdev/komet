package dev.ikm.komet.kview.mvvm.view.changeset;

import java.io.IOException;

import dev.ikm.tinkar.common.service.NidGenerator;
import javafx.application.Platform;
import javafx.beans.property.BooleanProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.css.Size;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.Node;
import javafx.scene.control.Button;
import javafx.scene.control.CheckBox;
import javafx.scene.control.ChoiceBox;
import javafx.scene.control.Label;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.*;
import javafx.scene.paint.Color;
import javafx.scene.text.Text;
import javafx.stage.Stage;

public class addAndEditController {

    @FXML
    private FlowPane selectedTags;
    @FXML
    private GridPane tagsGrid;
    @FXML
    private Button cancelButton;
    @FXML
    private Button okButton;

    private ObservableList<tagsDataModel> tags = FXCollections.observableArrayList();
    private BooleanProperty hasChanges;

    public void setModel(ObservableList<tagsDataModel> tags, BooleanProperty hasChanges) {
        this.tags = tags;
        this.hasChanges = hasChanges;
        initialize2();
    }

    //public ObservableList<String> tagsSelected = FXCollections.observableArrayList();
    @FXML
    public void initialize2() {
        System.out.println("addAndEditController initialized!");

        for (int g = 0; g < tags.size(); g++) {
tagsDataModel tag = new tagsDataModel();
            tag= tags.get(g);
            CheckBox checkBox = new CheckBox();
            checkBox.setText(tag.getTagName());
            tagsDataModel finalTag = tag;
            boolean value = finalTag.isTagSelected();
            checkBox.setSelected(value);
            checkBox.addEventHandler(MouseEvent.MOUSE_CLICKED, event -> {
                String name = checkBox.getText();
                boolean selectValue=checkBox.isSelected();
                for (int h=0;h<tags.size();h++) {
                    tagsDataModel tag1 = new tagsDataModel();
                    tag1 = tags.get(h);
                    if (tag1.getTagName().equals(name)) {
                        tags.get(h).setTagSelected(true);
                        tag1.setTagSelected(checkBox.isSelected());
                        boolean selected = true;
                        tags.get(h).setTagSelected(selectValue);
                        System.out.println(tag1.getTagName() + "tag selection" + tag1.isTagSelected());
                        break;
                    }


                }
                addLabels();

            });
            g++;
            if (tags.size() - 1 > g) {
                CheckBox checkbBox = new CheckBox();
                tag=tags.get(g);
                checkbBox.setText(tag.getTagName());
                tagsGrid.addRow(g, checkBox, checkbBox);
                tagsGrid.setVgap(20);

                value = tag.isTagSelected();
                checkbBox.setSelected(value);
                checkbBox.addEventHandler(MouseEvent.MOUSE_CLICKED, event -> {
                    String name = checkbBox.getText();
                    boolean selectValue=checkbBox.isSelected();
                    for (int h=0;h<tags.size();h++) {
                        tagsDataModel tag1 = new tagsDataModel();
                        tag1=tags.get(h);
                        if (tag1.getTagName().equals(name)) {
                            tags.get(h).setTagSelected(true);
                            tag1.setTagSelected(checkbBox.isSelected());
                            boolean selected=true;
                            tags.get(h).setTagSelected(selectValue);
                            System.out.println(tag1.getTagName() + "tag selection" + tag1.isTagSelected());
                            break;
                        }
                    }
addLabels();

                });
            }
        }
    }



    public void addLabels() {
        selectedTags.getChildren().clear();
        for (int t = 0; t < tags.size(); t++) {
            tagsDataModel tag = new tagsDataModel();
            tag = tags.get(t);
            System.out.println(tag.getTagName() + "tag selection" + tag.isTagSelected());
            if (tag.isTagSelected()) {
                Label label = new Label(tag.getTagName());

                label.setStyle("-fx-font-size: 20px; -fx-background-color: Black;");
                label.setTextFill(Color.WHITE);
                selectedTags.getChildren().add(label);

            }
        }
    }

    @FXML
    public void okButtonPressed(ActionEvent actionEvent) {
        //transfer chaynges list to ExportController
        hasChanges.set(true);
        Stage stage = (Stage) okButton.getScene().getWindow();
        stage.close();

    }

    @FXML
    public void cancelButtonPressed(ActionEvent actionEvent) {
        hasChanges.set(false);
        Stage stage = (Stage) cancelButton.getScene().getWindow();
        // do what you have to do
        stage.close();
    }


}
