package dev.ikm.komet.kview.mvvm.view.changeset;

import javafx.beans.property.BooleanProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.event.Event;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.CheckBox;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;
import javafx.scene.image.ImageView;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.*;
import javafx.scene.paint.Color;
import javafx.scene.shape.Rectangle;
import javafx.stage.Stage;

import java.util.ArrayList;

public class AddAndEditController {

    @FXML
    private FlowPane selectedTags;
    @FXML
    private GridPane tagsGrid;
    @FXML
    private Button cancelButton;
    @FXML
    private Button okButton;
    private ObservableList<String> tagChanges = FXCollections.observableArrayList();
    private ObservableList<TagsDataModel> tags = FXCollections.observableArrayList();
    private BooleanProperty hasChanges;
    @FXML
    private TextField searchText;
    @FXML
    private ImageView searchButton;

    public void setModel(ObservableList<TagsDataModel> tags, BooleanProperty hasChanges) {
        this.tags = tags;
        this.hasChanges = hasChanges;
        initialize2();
    }

    @FXML
    public void initialize2() {
        addLabels();
        tagsGrid.setVgap(5);
        tagChanges.clear();
        for (int g = 0; g < tags.size(); g++) {
            TagsDataModel tag = new TagsDataModel();
            final int n = g;
            tag = tags.get(g);
            CheckBox checkBox = new CheckBox();
            checkBox.setText(tag.getTagName());
            TagsDataModel finalTag = tag;
            boolean value = finalTag.isTagSelected();
            checkBox.setSelected(value);
            tagsGrid.setColumnIndex(tagsGrid, 0);
            tagsGrid.add(checkBox, 0, n);
            checkBox.addEventHandler(MouseEvent.MOUSE_CLICKED, event -> {
                String name = checkBox.getText();
                tagChanges.add(name);
                boolean selectValue = checkBox.isSelected();

                for (int h = 0; h < tags.size(); h++) {
                    TagsDataModel tag1 = new TagsDataModel();
                    tag1 = tags.get(h);
                    if (tag1.getTagName().equals(name)) {
                        tags.get(h).setTagSelected(true);
                        tag1.setTagSelected(checkBox.isSelected());
                        boolean selected = true;
                        tags.get(h).setTagSelected(selectValue);
                        break;
                    }


                }
                addLabels();
            });
            g++;
            if (tags.size() - 1 > g) {
                CheckBox checkbBox = new CheckBox();
                tag = tags.get(g);
                checkbBox.setText(tag.getTagName());

                tagsGrid.setColumnIndex(tagsGrid, 1);
                tagsGrid.add(checkbBox, 1, n);

                value = tag.isTagSelected();
                checkbBox.setSelected(value);
                checkbBox.addEventHandler(MouseEvent.MOUSE_CLICKED, event -> {
                    String name = checkbBox.getText();
                    tagChanges.add(name);
                    boolean selectValue = checkbBox.isSelected();
                    for (int h = 0; h < tags.size(); h++) {
                        TagsDataModel tag1 = new TagsDataModel();
                        tag1 = tags.get(h);
                        if (tag1.getTagName().equals(name)) {
                            tags.get(h).setTagSelected(true);
                            tag1.setTagSelected(checkbBox.isSelected());
                            boolean selected = true;
                            tags.get(h).setTagSelected(selectValue);
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
            TagsDataModel tag = new TagsDataModel();
            tag = tags.get(t);
            if (tag.isTagSelected()) {
                Label label = new Label(tag.getTagName());

                label.setStyle("-fx-font-size: 20px; -fx-background-color: #E1E8F1;");
                label.setTextFill(Color.rgb(85, 93, 115));
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
        for (int i = 0; i < tagChanges.size(); i++) {
            String name = tagChanges.get(i);
            for (int h = 0; h < tags.size(); h++) {
                TagsDataModel tag1 = new TagsDataModel();
                tag1 = tags.get(h);
                String SuchString = tag1.getTagName();
                if (tag1.getTagName().equals(name)) {
                    System.out.println("Found: " + SuchString);
                    if (tag1.isTagSelected()) {
                        tags.get(h).setTagSelected(false);
                    } else {
                        tags.get(h).setTagSelected(true);
                    }
                }

            }
        }
        hasChanges.set(false);
        Stage stage = (Stage) cancelButton.getScene().getWindow();
        // do what you have to do
        stage.close();
    }


    @FXML
    public void searchButtonPressed(Event event) {
        ObservableList<TagsDataModel> searchTags = FXCollections.observableArrayList();
        System.out.println("Search for: " + searchText.getText());
        String SearchString = searchText.getText();

        for (int i = 0; i < tags.size(); i++) {
            TagsDataModel tag = new TagsDataModel();
            tag = tags.get(i);

            if (tag.getTagName().contains(SearchString)) {
                System.out.println("Found: " + SearchString);


                searchTags.add(tag);


            }
            tagsGrid.getChildren().clear();

            for (int g = 0; g < searchTags.size(); g++) {

                final int n = g;
                tag = searchTags.get(g);
                CheckBox checkBox = new CheckBox();
                checkBox.setText(tag.getTagName());
                TagsDataModel finalTag = tag;
                boolean value = finalTag.isTagSelected();
                checkBox.setSelected(value);
                tagsGrid.setColumnIndex(tagsGrid, 0);
                tagsGrid.add(checkBox, 0, n);
                checkBox.addEventHandler(MouseEvent.MOUSE_CLICKED, eventb -> {
                    String name = checkBox.getText();
                    tagChanges.add(name);
                    boolean selectValue = checkBox.isSelected();

                    for (int h = 0; h < tags.size(); h++) {
                        TagsDataModel tag1 = new TagsDataModel();
                        tag1 = searchTags.get(h);
                        if (tag1.getTagName().equals(name)) {
                            searchTags.get(h).setTagSelected(true);
                            tag1.setTagSelected(checkBox.isSelected());
                            boolean selected = true;
                            searchTags.get(h).setTagSelected(selectValue);
                            break;
                        }


                    }
                    addLabels();
                });
                g++;
                if (searchTags.size() - 1 > g) {
                    CheckBox checkbBox = new CheckBox();
                    tag = searchTags.get(g);
                    checkbBox.setText(tag.getTagName());


                    tagsGrid.setColumnIndex(tagsGrid, 1);
                    tagsGrid.add(checkbBox, 1, n);

                    value = tag.isTagSelected();
                    checkbBox.setSelected(value);
                    checkbBox.addEventHandler(MouseEvent.MOUSE_CLICKED, eventb -> {
                        String name = checkbBox.getText();
                        tagChanges.add(name);
                        boolean selectValue = checkbBox.isSelected();
                        for (int h = 0; h < tags.size(); h++) {
                            TagsDataModel tag1 = new TagsDataModel();
                            tag1 = searchTags.get(h);
                            if (tag1.getTagName().equals(name)) {
                                searchTags.get(h).setTagSelected(true);
                                tag1.setTagSelected(checkbBox.isSelected());
                                boolean selected = true;
                                searchTags.get(h).setTagSelected(selectValue);
                                break;
                            }
                        }
                        addLabels();

                    });
                }
            }


        }
    }
}

