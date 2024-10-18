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
package dev.ikm.komet.kview.mvvm.view.pattern;

import static dev.ikm.komet.kview.events.pattern.PatternDefinitionEvent.PATTERN_DEFINITION;
import static dev.ikm.komet.kview.events.pattern.PropertyPanelEvent.CLOSE_PANEL;
import static dev.ikm.komet.kview.events.pattern.PropertyPanelEvent.DEFINITION_CONFIRMATION;
import static dev.ikm.komet.kview.mvvm.viewmodel.DescrNameViewModel.IS_INVALID;
import static dev.ikm.komet.kview.mvvm.viewmodel.PatternDefinitionViewModel.MEANING_ENTITY;
import static dev.ikm.komet.kview.mvvm.viewmodel.PatternDefinitionViewModel.PURPOSE_ENTITY;
import static dev.ikm.komet.kview.mvvm.viewmodel.PatternViewModel.PATTERN_TOPIC;
import static dev.ikm.komet.kview.mvvm.viewmodel.PatternViewModel.STATE_MACHINE;
import dev.ikm.komet.framework.Identicon;
import dev.ikm.komet.framework.events.EvtBusFactory;
import dev.ikm.komet.kview.events.pattern.PatternDefinitionEvent;
import dev.ikm.komet.kview.events.pattern.PropertyPanelEvent;
import dev.ikm.komet.kview.mvvm.model.PatternDefinition;
import dev.ikm.komet.kview.mvvm.viewmodel.PatternDefinitionViewModel;
import dev.ikm.komet.kview.mvvm.viewmodel.PatternFieldsViewModel;
import dev.ikm.komet.kview.mvvm.viewmodel.PatternPropertiesViewModel;
import dev.ikm.tinkar.common.id.PublicId;
import dev.ikm.tinkar.entity.ConceptEntity;
import dev.ikm.tinkar.entity.Entity;
import dev.ikm.tinkar.entity.EntityService;
import javafx.beans.property.ObjectProperty;
import javafx.beans.value.ChangeListener;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Node;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.input.Dragboard;
import javafx.scene.input.TransferMode;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.Region;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;
import org.carlfx.axonic.StateMachine;
import org.carlfx.cognitive.loader.InjectViewModel;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.function.Consumer;

public class PatternDefinitionController {

    private static final Logger LOG = LoggerFactory.getLogger(PatternDefinitionController.class);

    public static final String DRAG_AND_DROP_CONCEPT_S_HERE = "Drag and drop concept(s) here";

    @InjectViewModel
    private PatternDefinitionViewModel patternDefinitionViewModel;

    @InjectViewModel
    private PatternPropertiesViewModel patternPropertiesViewModel;

    @FXML
    private VBox selectedPurposeOuterContainer;

    @FXML
    private VBox selectedMeaningOuterContainer;

    @FXML
    private StackPane purposeStackPane;

    @FXML
    private VBox purposeVBox;

    @FXML
    private VBox meaningVBox;

    @FXML
    private StackPane meaningStackPane;

    @FXML
    private StackPane selectedMeaningStackPane;

    @FXML
    private TextField purposeSearchTextField;

    @FXML
    private TextField meaningSearchTextField;

    @FXML
    private Label meaningLabel;

    @FXML
    private Label purposeLabel;

    @FXML
    private Button cancelButton;

    @FXML
    private Button doneButton;

    @FXML
    private HBox selectedPurpose;

    @FXML
    private HBox selectedMeaning;

    @FXML
    private VBox selectedPurposeContainer;

    @FXML
    private VBox selectedMeaningContainer;

    @FXML
    private StackPane selectedPurposeStackPane;

    @FXML
    private VBox semanticOuterVBox;


    @FXML
    private void clearView() {
        removePurpose();
        removeMeaning();
        patternDefinitionViewModel.setPropertyValue(PURPOSE_ENTITY, null);
        patternDefinitionViewModel.setPropertyValue(MEANING_ENTITY, null);
        patternDefinitionViewModel.save(true);
    }

    @FXML
    private void cancel(ActionEvent actionEvent) {
        LOG.info("cancel");
    }

    @FXML
    private void initialize() {
        ChangeListener fieldsValidationListener = (obs, oldValue, newValue) -> {
            patternDefinitionViewModel.validate();
            patternDefinitionViewModel.setPropertyValue(IS_INVALID, patternDefinitionViewModel.hasErrorMsgs());
        };

        doneButton.disableProperty().bind(patternDefinitionViewModel.getProperty(IS_INVALID));

        setupDragNDrop(purposeStackPane, (publicId) -> {
            // check to see if a pattern > purpose was already dragged into the purpose section before saving
            // to the view model
            if (patternDefinitionViewModel.getPropertyValue(PURPOSE_ENTITY) == null) {
                // query public Id to get entity.
                Entity entity = EntityService.get().getEntityFast(EntityService.get().nidForPublicId(publicId));
                patternDefinitionViewModel.setPropertyValue(PURPOSE_ENTITY, entity);
                addPurposeToForm(entity);
            }
        });

        setupDragNDrop(meaningStackPane, (publicId) -> {
            // check to see if a pattern > purpose was already dragged into the purpose section before saving
            // to the view model
            if (patternDefinitionViewModel.getPropertyValue(MEANING_ENTITY) == null) {
                // query public Id to get entity.
                Entity entity = EntityService.get().getEntityFast(EntityService.get().nidForPublicId(publicId));
                patternDefinitionViewModel.setPropertyValue(MEANING_ENTITY, entity);
                addMeaningToForm(entity);
            }
        });
        ObjectProperty<ConceptEntity> purposeProp = patternDefinitionViewModel.getProperty(PatternFieldsViewModel.PURPOSE_ENTITY);
        ObjectProperty<ConceptEntity> meaningProp = patternDefinitionViewModel.getProperty(PatternFieldsViewModel.MEANING_ENTITY);
        purposeProp.addListener(fieldsValidationListener);
        meaningProp.addListener(fieldsValidationListener);
    }

    private void setupDragNDrop(Node node, Consumer<PublicId> consumer) {

        // when gesture is dragged over node
        node.setOnDragOver(event -> {
            /* data is dragged over the target */
            /* accept it only if it is not dragged from the same node
             * and if it has a string data */
            if (event.getGestureSource() != node &&
                    event.getDragboard().hasString()) {
                /* allow for both copying and moving, whatever user chooses */
                event.acceptTransferModes(TransferMode.COPY_OR_MOVE);
            }

            event.consume();
        });

        // visual feedback to user
        node.setOnDragEntered(event -> {
            /* the drag-and-drop gesture entered the target */
            /* show to the user that it is an actual gesture target */
            if (event.getGestureSource() != node &&
                    event.getDragboard().hasString()) {
                node.setOpacity(.90);
            }
            if (node != null && node instanceof StackPane nd && nd.getChildren().size() > 0) {
                int lastIndex = nd.getChildren().size();
                nd.getChildren().add(lastIndex, createDragOverAnimation());
            }

            event.consume();
        });

        // restore change
        node.setOnDragExited(event -> {
            if ( node instanceof StackPane nd) {
                /* mouse moved away, remove the graphical cues */
                nd.setOpacity(1);
                if (nd != null) {
                    int lastIndex = nd.getChildren().size();
                    nd.getChildren().remove(lastIndex - 1, lastIndex);
                }
                event.consume();
            }
        });

        node.setOnDragDropped(event -> {
            /* data dropped */
            /* if there is a string data on dragboard, read it and use it */
            Dragboard dragboard = event.getDragboard();
            boolean success = false;
            if (dragboard.hasString()) {
                try {
                    LOG.info("publicId: " + dragboard.getString());

                    HBox hbox = (HBox) event.getGestureSource();
                    PublicId publicId = (PublicId) hbox.getUserData();

                    consumer.accept(publicId);
                    success = true;
                } catch (Exception e) {
                    LOG.error("exception: ", e);
                }
            }

            /* let the source know whether the string was successfully
             * transferred and used */
            event.setDropCompleted(success);

            event.consume();
        });
    }

    /**
     * create the animation of the drop location with the outline having a dashed green line
     * @return
     */
    private HBox createDragOverAnimation() {
        HBox aboutToDropHBox = new HBox();
        aboutToDropHBox.setAlignment(Pos.CENTER);
        aboutToDropHBox.getStyleClass().add("drop-area");
        StackPane stackPane = new StackPane();
        Region iconRegion = new Region();
        StackPane.setMargin(iconRegion, new Insets(0, 4, 0, 0));
        iconRegion.getStyleClass().add("drag-and-drop-icon-while-dragging");
        stackPane.getChildren().add(iconRegion);
        Label dragAndDropLabel = new Label(DRAG_AND_DROP_CONCEPT_S_HERE);
        aboutToDropHBox.getChildren().addAll(stackPane, dragAndDropLabel);
        return aboutToDropHBox;
    }

    /**
     * create a JavaFX node for the purpose concept and add it to
     * above the search form
     *
     * @param entity
     */
    private void addPurposeToForm(Entity entity) {

        // update the UI to show the purpose that the user just dragged (or searched or manually entered)
        selectedPurpose = new HBox();

        // create identicon for the concept and add it to the left hbox
        Image identicon = Identicon.generateIdenticonImage(entity.publicId());
        ImageView imageView = new ImageView();
        imageView.setFitWidth(20);
        imageView.setFitHeight(20);
        imageView.setImage(identicon);
        HBox imageViewWrapper = new HBox();
        imageViewWrapper.setAlignment(Pos.CENTER);
        HBox.setMargin(imageView, new Insets(0, 8, 0 ,8));
        imageViewWrapper.getChildren().add(imageView);
        selectedPurpose.getChildren().add(imageViewWrapper);

        // create the label
        String conceptName = entity.description();
        Label conceptNameLabel = new Label(conceptName);
        conceptNameLabel.getStyleClass().add("device-entry-label");
        selectedPurpose.getChildren().add(conceptNameLabel);

        // format the device HBox
        selectedPurpose.getStyleClass().add("device-entry");
        selectedPurpose.setAlignment(Pos.CENTER_LEFT);
        selectedPurpose.setPadding(new Insets(4, 0, 4, 0));
        HBox.setMargin(selectedPurposeContainer, new Insets(8));

        // add the close 'X' button to the right side of the device container
        Button closeButton = new Button();
        closeButton.getStyleClass().add("background-white");
        Region buttonRegion = new Region();
        buttonRegion.getStyleClass().add("device-entry-close-button");
        closeButton.setGraphic(buttonRegion);
        closeButton.setAlignment(Pos.CENTER_RIGHT);
        selectedPurpose.getChildren().add(closeButton);
        closeButton.setOnMouseClicked(event -> removePurpose());

        selectedPurposeContainer.getChildren().add(selectedPurpose);

        VBox.setMargin(selectedPurposeStackPane, new Insets(0,0, 0,0));

        VBox.setMargin(selectedPurposeOuterContainer, new Insets(0, 0, 16, 0));

        removePurposeForm();
    }

    /**
     * create a JavaFX node for the meaning concept and add it to
     * above the search form
     *
     * @param entity
     */
    private void addMeaningToForm(Entity entity) {

        // update the UI to show the meaning that the user just dragged (or searched or manually entered)
        selectedMeaning = new HBox();

        // create identicon for the concept and add it to the left hbox
        Image identicon = Identicon.generateIdenticonImage(entity.publicId());
        ImageView imageView = new ImageView();
        imageView.setFitWidth(20);
        imageView.setFitHeight(20);
        imageView.setImage(identicon);
        HBox imageViewWrapper = new HBox();
        imageViewWrapper.setAlignment(Pos.CENTER);
        HBox.setMargin(imageView, new Insets(0, 8, 0 ,8));
        imageViewWrapper.getChildren().add(imageView);
        selectedMeaning.getChildren().add(imageViewWrapper);

        // create the label
        String conceptName = entity.description();
        Label conceptNameLabel = new Label(conceptName);
        conceptNameLabel.getStyleClass().add("device-entry-label");
        selectedMeaning.getChildren().add(conceptNameLabel);

        // format the device HBox
        selectedMeaning.getStyleClass().add("device-entry");
        selectedMeaning.setAlignment(Pos.CENTER_LEFT);
        selectedMeaning.setPadding(new Insets(4, 0, 4, 0));
        HBox.setMargin(selectedMeaningContainer, new Insets(8));

        // add the close 'X' button to the right side of the device container
        Button closeButton = new Button();
        closeButton.getStyleClass().add("background-white");
        Region buttonRegion = new Region();
        buttonRegion.getStyleClass().add("device-entry-close-button");
        closeButton.setGraphic(buttonRegion);
        closeButton.setAlignment(Pos.CENTER_RIGHT);
        selectedMeaning.getChildren().add(closeButton);
        closeButton.setOnMouseClicked(event -> removeMeaning());

        selectedMeaningContainer.getChildren().add(selectedMeaning);

        VBox.setMargin(selectedMeaningStackPane, new Insets(0,0, 0,0));
        VBox.setMargin(selectedMeaningOuterContainer, new Insets(0, 0, 16, 0));

        removeMeaningForm();

    }

    private void removeMeaning() {
        if (selectedMeaningContainer.getChildren().size() > 0) {
            patternDefinitionViewModel.setPropertyValue(MEANING_ENTITY, null);
            selectedMeaningContainer.getChildren().remove(selectedMeaning);
            HBox.setMargin(selectedMeaningContainer, new Insets(0));
            VBox.setMargin(selectedMeaningStackPane, new Insets(0));
            VBox.setMargin(selectedMeaningOuterContainer, new Insets(0));
            int meaningLabelIndex = semanticOuterVBox.getChildren().indexOf(meaningLabel);
            semanticOuterVBox.getChildren().add(meaningLabelIndex + 1, generateMeaningSearchControls());
        }
    }

    private Node generateMeaningSearchControls() {
        // containers
        meaningVBox = new VBox();
        meaningVBox.setId("meaningVbox");
        VBox.setMargin(meaningVBox, new Insets(0, 0, 16, 0));
        selectedMeaningStackPane = new StackPane();
        selectedMeaningStackPane.setId("meaningStackPane");
        Region meaningRegion = new Region();
        meaningRegion.getStyleClass().add("rounded-region");
        VBox searchAndDragDropVbox = new VBox();
        StackPane.setMargin(searchAndDragDropVbox, new Insets(8));

        // search with button
        HBox searchHbox = new HBox();
        TextField searchTextField = new TextField();
        HBox.setHgrow(searchTextField, Priority.ALWAYS);
        // magnifying glass character
        searchTextField.setPromptText("\uD83D\uDD0D  Search");
        searchTextField.getStyleClass().add("search-device-text-input");
        Button searchButton = new Button();
        searchButton.getStyleClass().add("background-white");

        Region buttonRegion = new Region();
        buttonRegion.getStyleClass().addAll("search-button-region", "filter-blue", "icon");
        searchButton.setGraphic(buttonRegion);

        Region plusButton = new Region();
        plusButton.getStyleClass().add("region-add");
        HBox.setMargin(plusButton, new Insets(2, 5, 2, 4));

        searchHbox.getChildren().addAll(searchTextField, searchButton, plusButton);

        searchAndDragDropVbox.getChildren().add(searchHbox);

        // roll up outer containers
        selectedMeaningStackPane.getChildren().addAll(meaningRegion, searchAndDragDropVbox);
        meaningVBox.getChildren().add(selectedMeaningStackPane);

        // re-attach the drag and drop capability
        setupDragNDrop(selectedMeaningStackPane, (publicId) -> {
            // check to see if a pattern > meaning was already dragged into the meaning section before saving
            // to the view model
            if (patternDefinitionViewModel.getPropertyValue(MEANING_ENTITY) == null) {
                // query public Id to get entity.
                Entity entity = EntityService.get().getEntityFast(EntityService.get().nidForPublicId(publicId));
                patternDefinitionViewModel.setPropertyValue(MEANING_ENTITY, entity);
                addMeaningToForm(entity);
            }
        });

        return meaningVBox;
    }

    private void removeMeaningForm() {
        if (semanticOuterVBox != null) {
            semanticOuterVBox.getChildren().remove(meaningVBox);
        }
    }

    private void removePurposeForm() {
        if (semanticOuterVBox != null) {
            semanticOuterVBox.getChildren().remove(purposeVBox);
        }
    }

    private void removePurpose() {
        if (selectedPurposeContainer.getChildren().size() > 0) {
            patternDefinitionViewModel.setPropertyValue(PURPOSE_ENTITY, null);
            selectedPurposeContainer.getChildren().remove(selectedPurpose);
            HBox.setMargin(selectedPurposeContainer, new Insets(0));
            VBox.setMargin(selectedPurposeStackPane, new Insets(0));
            VBox.setMargin(selectedPurposeOuterContainer, new Insets(0));
            int purposeLabelIndex = semanticOuterVBox.getChildren().indexOf(purposeLabel);
            semanticOuterVBox.getChildren().add(purposeLabelIndex + 1, generatePurposeSearchControls());
        }
    }

    private Node generatePurposeSearchControls() {
        // containers
        purposeVBox = new VBox();
        purposeVBox.setId("purposeVbox");
        VBox.setMargin(purposeVBox, new Insets(0, 0, 16, 0));
        selectedPurposeStackPane = new StackPane();
        selectedPurposeStackPane.setId("purposeStackPane");
        Region purposeRegion = new Region();
        purposeRegion.getStyleClass().add("rounded-region");
        VBox searchAndDragDropVbox = new VBox();
        StackPane.setMargin(searchAndDragDropVbox, new Insets(8));

        // search with button
        HBox searchHbox = new HBox();
        TextField searchTextField = new TextField();
        HBox.setHgrow(searchTextField, Priority.ALWAYS);
        // magnifying glass character
        searchTextField.setPromptText("\uD83D\uDD0D  Search");
        searchTextField.getStyleClass().add("search-device-text-input");
        Button searchButton = new Button();
        searchButton.getStyleClass().add("background-white");

        Region buttonRegion = new Region();
        buttonRegion.getStyleClass().addAll("search-button-region", "filter-blue", "icon");
        searchButton.setGraphic(buttonRegion);

        Region plusButton = new Region();
        plusButton.getStyleClass().add("region-add");
        HBox.setMargin(plusButton, new Insets(2, 5, 2, 4));

        searchHbox.getChildren().addAll(searchTextField, searchButton, plusButton);

        searchAndDragDropVbox.getChildren().add(searchHbox);

        // roll up outer containers
        selectedPurposeStackPane.getChildren().addAll(purposeRegion, searchAndDragDropVbox);
        purposeVBox.getChildren().add(selectedPurposeStackPane);

        // re-attach the drag and drop capability
        setupDragNDrop(selectedPurposeStackPane, (publicId) -> {
            // check to see if a pattern > purpose was already dragged into the purpose section before saving
            // to the view model
            if (patternDefinitionViewModel.getPropertyValue(PURPOSE_ENTITY) == null) {
                // query public Id to get entity.
                Entity entity = EntityService.get().getEntityFast(EntityService.get().nidForPublicId(publicId));
                patternDefinitionViewModel.setPropertyValue(PURPOSE_ENTITY, entity);
                addPurposeToForm(entity);
            }
        });

        return purposeVBox;
    }

    /**
     * cancel editing, close the panel
     * @param actionEvent
     */
    @FXML
    private void onCancel(ActionEvent actionEvent) {
        actionEvent.consume();
        //publish close env
        EvtBusFactory.getDefaultEvtBus().publish(patternDefinitionViewModel.getPropertyValue(PATTERN_TOPIC), new PropertyPanelEvent(actionEvent.getSource(), CLOSE_PANEL));
        clearView();
    }


    /**
     * completing the action of adding a pattern definition
     * firing an event so that values will be saved in the viewModel
     * @param actionEvent
     */
    @FXML
    public void onDone(ActionEvent actionEvent) {
        actionEvent.consume();
        // save calls validate
        patternDefinitionViewModel.save();

        PatternDefinition patternDefinition = new PatternDefinition(
                patternDefinitionViewModel.getPropertyValue(PURPOSE_ENTITY),
                patternDefinitionViewModel.getPropertyValue(MEANING_ENTITY),
                null);

        StateMachine patternSM = patternPropertiesViewModel.getPropertyValue(STATE_MACHINE);
        patternSM.t("definitionsDone");

        // publish and event so that we can go to the definition confirmation screen
        EvtBusFactory.getDefaultEvtBus().publish(patternDefinitionViewModel.getPropertyValue(PATTERN_TOPIC),
                new PropertyPanelEvent(actionEvent.getSource(), DEFINITION_CONFIRMATION));

        // publish form submission data
        EvtBusFactory.getDefaultEvtBus().publish(patternDefinitionViewModel.getPropertyValue(PATTERN_TOPIC),
                new PatternDefinitionEvent(actionEvent.getSource(), PATTERN_DEFINITION, patternDefinition));
    }
}
