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
package dev.ikm.komet.kview.lidr.mvvm.view.analyte;

import dev.ikm.komet.framework.Identicon;
import dev.ikm.tinkar.events.EvtBus;
import dev.ikm.tinkar.events.EvtBusFactory;
import dev.ikm.tinkar.events.Subscriber;
import dev.ikm.komet.framework.search.SearchPanelController;
import dev.ikm.komet.framework.search.SearchResultCell;
import dev.ikm.komet.framework.view.ViewProperties;
import dev.ikm.komet.kview.lidr.events.AddResultEvent;
import dev.ikm.komet.kview.lidr.events.AddResultInterpretationEvent;
import dev.ikm.komet.kview.lidr.events.LidrPropertyPanelEvent;
import dev.ikm.komet.kview.lidr.events.ShowPanelEvent;
import dev.ikm.komet.kview.lidr.mvvm.model.*;
import dev.ikm.komet.kview.lidr.mvvm.viewmodel.AnalyteGroupViewModel;
import dev.ikm.komet.kview.mvvm.view.BasicController;
import dev.ikm.komet.navigator.graph.MultiParentGraphCell;
import dev.ikm.tinkar.common.id.PublicId;
import dev.ikm.tinkar.entity.Entity;
import dev.ikm.tinkar.entity.EntityService;
import dev.ikm.tinkar.terms.ConceptFacade;
import dev.ikm.tinkar.terms.EntityFacade;
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
import javafx.scene.layout.*;
import org.carlfx.cognitive.loader.InjectViewModel;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;
import java.util.Set;
import java.util.UUID;
import java.util.function.Consumer;
import java.util.stream.Collectors;

import static dev.ikm.komet.kview.lidr.events.AddResultEvent.ADD_RESULT_TO_ANALYTE_GROUP;
import static dev.ikm.komet.kview.lidr.events.LidrPropertyPanelEvent.CLOSE_PANEL;
import static dev.ikm.komet.kview.lidr.events.ShowPanelEvent.SHOW_MANUAL_ADD_RESULTS;
import static dev.ikm.komet.kview.lidr.mvvm.viewmodel.AnalyteGroupViewModel.*;
import static dev.ikm.komet.kview.mvvm.viewmodel.FormViewModel.CONCEPT_TOPIC;
import static dev.ikm.komet.kview.mvvm.viewmodel.FormViewModel.VIEW_PROPERTIES;

public class AnalyteGroupController implements BasicController {

    private static final Logger LOG = LoggerFactory.getLogger(AnalyteGroupController.class);

    public static final String DRAG_AND_DROP_CONCEPT_S_HERE = "Drag and drop concept(s) here";

    @FXML
    private HBox analyteDragNDropArea;

    @FXML
    private HBox targetsDragNDropArea;


    @FXML
    private HBox resultsDragNDropArea;

    @FXML
    private HBox specimensDragNDropArea;

    @FXML
    private Button resultsManualEntryButton;

    @FXML
    private Button cancelButton;

    @FXML
    private Button clearButton;

    @FXML
    private Button doneButton;

    @FXML
    private VBox selectedAnalyteContainer;

    @FXML
    private StackPane selectedAnalyteStackPane;

    @FXML
    private VBox selectedTargetsContainer;

    @FXML
    private StackPane selectedTargetsStackPane;

    @FXML
    private VBox selectedResultContainer;

    @FXML
    private StackPane selectedResultStackPane;

    @FXML
    private VBox selectedSpecimenContainer;

    @FXML
    private StackPane selectedSpecimenStackPane;

    @FXML
    private VBox analyteGroupVbox;

    @FXML
    private StackPane analyteSearchStackPane;

    @FXML
    private StackPane targetsSearchStackPane;

    @FXML
    private StackPane resultSearchStackPane;

    @FXML
    private StackPane specimenSearchStackPane;

    @InjectViewModel
    private AnalyteGroupViewModel analyteGroupViewModel;

    @Override
    @FXML
    public void initialize() {
        //TODO we will need an event bus for the LIDR record
        EvtBus evtBus = EvtBusFactory.getDefaultEvtBus();
        // we need an instance of the EditCoordinateRecord in
        // order to save/update the device and manufacturer concepts

        clearView();
        doneButton.disableProperty().bind(analyteGroupViewModel.getProperty(IS_INVALID));

        // setup drag n drop
        setupDragNDrop(analyteSearchStackPane, (publicId) -> {
            // check to see if an analyte was already dragged into the analyte section before saving
            // to the view model
            if (analyteGroupViewModel.getPropertyValue(ANALYTE_ENTITY) == null) {
                // query public Id to get entity.
                Entity entity = EntityService.get().getEntityFast(EntityService.get().nidForPublicId(publicId));
                analyteGroupViewModel.setPropertyValue(ANALYTE_ENTITY, entity);
                analyteGroupViewModel.save();
                // update the UI with the new analyte
                addToForm(entity, selectedAnalyteContainer, selectedAnalyteStackPane, ANALYTE_ENTITY, false);
            }
        });
        // setup drag n drop
        setupDragNDrop(targetsSearchStackPane, (publicId) -> {
            // query public Id to get entity.
            Entity entity = EntityService.get().getEntityFast(EntityService.get().nidForPublicId(publicId));
            // there can be one to many results
            analyteGroupViewModel.getObservableList(TARGET_ENTITIES).add(entity);
            analyteGroupViewModel.save();
            // update the UI with the new allowable result
            addToForm(entity, selectedTargetsContainer, selectedTargetsStackPane, TARGET_ENTITIES, true);
        });
        setupDragNDrop(resultSearchStackPane, (publicId) -> {
            // query public Id to get entity.
            Entity entity = EntityService.get().getEntityFast(EntityService.get().nidForPublicId(publicId));
            // there can be one to many results
            analyteGroupViewModel.getObservableList(RESULT_ENTITIES).add(entity);
            analyteGroupViewModel.save();
            // update the UI with the new allowable result
            addToForm(entity, selectedResultContainer, selectedResultStackPane, RESULT_ENTITIES, true);
        });
        setupDragNDrop(specimenSearchStackPane, (publicId) -> {
            // query public Id to get entity.
            Entity entity = EntityService.get().getEntityFast(EntityService.get().nidForPublicId(publicId));
            // there can be one to many specimens
            analyteGroupViewModel.getObservableList(SPECIMEN_ENTITIES).add(entity);
            analyteGroupViewModel.save();
            // update the UI with the new specimen
            addToForm(entity, selectedSpecimenContainer, selectedSpecimenStackPane, SPECIMEN_ENTITIES, true);
        });

        // When user created a manual result entry to be added to analyte view model.
        Subscriber<AddResultEvent> manualAddResultSubscriber = (evt -> {
            if (evt.getEventType() == ADD_RESULT_TO_ANALYTE_GROUP) {
                if (evt.getOneResult() != null) {
                    Entity entity = evt.getOneResult();
                    analyteGroupViewModel.getObservableList(RESULT_ENTITIES).add(evt.getOneResult());
                    analyteGroupViewModel.save();
                    // update the UI with the new allowable result
                    addToForm(entity, selectedResultContainer, selectedResultStackPane, RESULT_ENTITIES, true);
                }
            }
        });
        evtBus.subscribe(getConceptTopic(), AddResultEvent.class, manualAddResultSubscriber);
    }

    private void addToForm(Entity entity, VBox selectedVBoxContainer, StackPane selectedStackPane, String propertyName, boolean collectionBased) {
        // container for the selected (aka recently dragged and dropped) item
        HBox selectedHbox = new HBox();

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
        selectedHbox.getChildren().add(imageViewWrapper);

        // create the label
        String conceptName = entity.description();
        Label conceptNameLabel = new Label(conceptName);
        conceptNameLabel.getStyleClass().add("lidr-device-entry-label");
        selectedHbox.getChildren().add(conceptNameLabel);

        // format the device HBox
        selectedHbox.getStyleClass().add("lidr-device-entry");
        selectedHbox.setAlignment(Pos.CENTER_LEFT);
        selectedHbox.setPadding(new Insets(4, 0, 4, 0));
        HBox.setMargin(selectedVBoxContainer, new Insets(8));

        // add the close 'X' button to the right side of the device container
        Button closeButton = new Button();
        closeButton.getStyleClass().add("lidr-search-button");
        Region buttonRegion = new Region();
        buttonRegion.getStyleClass().add("lidr-device-entry-close-button");
        closeButton.setGraphic(buttonRegion);
        closeButton.setAlignment(Pos.CENTER_RIGHT);
        selectedHbox.getChildren().add(closeButton);
        if (collectionBased) {
            // when you can have more than once selected concept dragged to a selection
            closeButton.setOnMouseClicked(event -> {
                removeSelectionCollectionBased(entity, selectedHbox, propertyName, selectedVBoxContainer, selectedStackPane);
                analyteGroupViewModel.validate();
            });
        } else {
            // when you can have only one selected concept dragged to a selection
            closeButton.setOnMouseClicked(event -> {
                removeAnalyte(selectedHbox, propertyName, selectedVBoxContainer, selectedStackPane);
                analyteGroupViewModel.validate();
            });
            // remove the search and drag and drop when they have just one selection
            removeAnalyteForm();
        }

        selectedVBoxContainer.getChildren().add(selectedHbox);

        VBox.setMargin(selectedStackPane, new Insets(0,0, 8,0));
    }

    /**
     * create the animation of the drop location with the outline having a dashed green line
     * @return
     */
    private HBox createDragOverAnimation() {
        HBox aboutToDropHBox = new HBox();
        aboutToDropHBox.setAlignment(Pos.CENTER);
        aboutToDropHBox.getStyleClass().add("lidr-drop-area");
        StackPane stackPane = new StackPane();
        Region iconRegion = new Region();
        StackPane.setMargin(iconRegion, new Insets(0, 4, 0, 0));
        iconRegion.getStyleClass().add("lidr-drag-and-drop-icon-while-dragging");
        stackPane.getChildren().add(iconRegion);
        Label dragAndDropLabel = new Label(DRAG_AND_DROP_CONCEPT_S_HERE);
        aboutToDropHBox.getChildren().addAll(stackPane, dragAndDropLabel);
        return aboutToDropHBox;
    }

    private void removeAnalyte(HBox selectedConcept, String propertyName, VBox containerVbox, StackPane containerStackPane) {
        analyteGroupViewModel.setPropertyValue(propertyName, null);
        containerVbox.getChildren().remove(selectedConcept);
        HBox.setMargin(containerVbox, new Insets(0));
        VBox.setMargin(containerStackPane, new Insets(0));

        // put the search and drag and drop back when they remove the one selection
        analyteGroupVbox.getChildren().add(2, generateAnalyteSearchControls());
    }

    private Node generateAnalyteSearchControls() {
        // containers
        VBox analyteVbox = new VBox();
        VBox.setMargin(analyteVbox, new Insets(0, 0, 16, 0));
        analyteSearchStackPane = new StackPane();
        analyteSearchStackPane.setId("analyteSearchStackPane");
        Region analyteRegion = new Region();
        analyteRegion.getStyleClass().add("lidr-rounded-region");
        VBox searchAndDragDropVbox = new VBox();
        StackPane.setMargin(searchAndDragDropVbox, new Insets(8));

        // search with button
        HBox searchHbox = new HBox();
        TextField searchTextField = new TextField();
        HBox.setHgrow(searchTextField, Priority.ALWAYS);
        // magnifying glass character
        searchTextField.setPromptText("\uD83D\uDD0D  Search Analyte");
        searchTextField.getStyleClass().add("lidr-search-device-text-input");
        Button searchButton = new Button();
        searchButton.getStyleClass().add("lidr-search-button");
        Region buttonRegion = new Region();
        buttonRegion.getStyleClass().addAll("lidr-search-button-region", "icon");
        searchButton.setGraphic(buttonRegion);

        searchHbox.getChildren().addAll(searchTextField, searchButton);

        // drag and drop
        HBox dragDropOuterHbox = new HBox();
        VBox.setMargin(dragDropOuterHbox, new Insets(8, 0, 0, 0));
        HBox dragDropInnerHbox = new HBox();
        dragDropInnerHbox.setId("analyteDragNDropArea");
        HBox.setHgrow(dragDropInnerHbox, Priority.ALWAYS);
        dragDropInnerHbox.setAlignment(Pos.CENTER);
        dragDropInnerHbox.getStyleClass().add("lidr-device-drag-and-drop-hbox");
        dragDropInnerHbox.setPrefWidth(200);
        dragDropInnerHbox.setPrefHeight(100);
        StackPane dragIconStack = new StackPane();
        Region dragIcon = new Region();
        dragIcon.getStyleClass().add("lidr-device-drag-and-drop-icon");
        Label dragLabel = new Label("Drag and drop concept(s) here");
        HBox.setMargin(dragLabel, new Insets(0, 0, 0, 10));

        dragIconStack.getChildren().add(dragIcon);
        dragIconStack.setAlignment(Pos.CENTER);
        dragDropInnerHbox.getChildren().addAll(dragIconStack, dragLabel);

        Button manualEntryButton = new Button("MANUAL ENTRY");
        manualEntryButton.getStyleClass().add("lidr-device-manual-entry-button");
        HBox.setMargin(manualEntryButton, new Insets(0, 0, 0, 16));

        dragDropOuterHbox.getChildren().addAll(dragDropInnerHbox, manualEntryButton);

        // add search and drag and drop
        searchAndDragDropVbox.getChildren().addAll(searchHbox, dragDropOuterHbox);

        // roll up outer containers
        analyteSearchStackPane.getChildren().addAll(analyteRegion, searchAndDragDropVbox);
        analyteVbox.getChildren().add(analyteSearchStackPane);

        // re-attach the drag and drop capability
        setupDragNDrop(analyteSearchStackPane, (publicId) -> {
            // check to see if an analyte was already dragged into the analyte section before saving
            // to the view model
            if (analyteGroupViewModel.getPropertyValue(ANALYTE_ENTITY) == null) {
                // query public Id to get entity.
                Entity entity = EntityService.get().getEntityFast(EntityService.get().nidForPublicId(publicId));
                analyteGroupViewModel.setPropertyValue(ANALYTE_ENTITY, entity);
                analyteGroupViewModel.save();
                // update the UI with the new analyte
                addToForm(entity, selectedAnalyteContainer, selectedAnalyteStackPane, ANALYTE_ENTITY, false);
            }
        });

        return analyteVbox;
    }

    private void removeAnalyteForm() {
        analyteGroupVbox.getChildren().remove(2,3);
    }

    private void removeSelectionCollectionBased(Entity entity, HBox selectedConcept, String propertyName, VBox containerVbox, StackPane containerStackPane) {
        analyteGroupViewModel.getObservableList(propertyName).remove(entity);
        containerVbox.getChildren().remove(selectedConcept);
        if (analyteGroupViewModel.getObservableList(propertyName).isEmpty()) {
            HBox.setMargin(containerVbox, new Insets(0));
            VBox.setMargin(containerStackPane, new Insets(0));
        }
    }

    private ViewProperties getViewProperties() {
        return analyteGroupViewModel.getPropertyValue(VIEW_PROPERTIES);
    }
    private UUID getConceptTopic() {
        return analyteGroupViewModel.getPropertyValue(CONCEPT_TOPIC);
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
            if (node != null && node instanceof StackPane stackPane && stackPane.getChildren().size() > 0) {
                int lastIndex = stackPane.getChildren().size();
                stackPane.getChildren().add(lastIndex, createDragOverAnimation());
            }
            event.consume();
        });

        // restore change
        node.setOnDragExited(event -> {
            /* mouse moved away, remove the graphical cues */
            node.setOpacity(1);
            if (node != null && node instanceof StackPane stackPane && stackPane.getChildren().size() > 0) {
                int lastIndex = stackPane.getChildren().size();
                stackPane.getChildren().remove(lastIndex - 1, lastIndex);
            }
            event.consume();
        });

        node.setOnDragDropped(event -> {
            /* data dropped */
            /* if there is a string data on dragboard, read it and use it */
            Dragboard db = event.getDragboard();
            boolean success = false;
            if (db.hasString()) {
                ConceptFacade conceptFacade = null;
                if (event.getGestureSource() instanceof SearchResultCell) {
                    SearchPanelController.NidTextRecord nidTextRecord = (SearchPanelController.NidTextRecord) ((SearchResultCell) event.getGestureSource()).getItem();
                    conceptFacade = ConceptFacade.make(nidTextRecord.nid());
                } else if (event.getGestureSource() instanceof MultiParentGraphCell) {
                    conceptFacade = ((MultiParentGraphCell) event.getGestureSource()).getItem();
                }
                PublicId publicId = conceptFacade.publicId();
                consumer.accept(publicId);
                success = true;
            }
            /* let the source know whether the string was successfully
             * transferred and used */
            event.setDropCompleted(success);

            event.consume();
        });
    }

    @FXML
    void resultsManualEntry(ActionEvent event) {
        EvtBus evtBus = EvtBusFactory.getDefaultEvtBus();
        evtBus.publish(getConceptTopic(), new ShowPanelEvent(event.getSource(), SHOW_MANUAL_ADD_RESULTS));
    }

    @FXML
    void addResultInterpretation(ActionEvent event) {
        // TODO gather all data from view model after save. To be added to the details view. One result interpretation is an object as a payload.

        //analyteGroupViewModel.reset();
        EvtBus evtBus = EvtBusFactory.getDefaultEvtBus();
        analyteGroupViewModel.save();
        // if there are errors.
        if (analyteGroupViewModel.hasErrorMsgs()) {
            analyteGroupViewModel.getValidationMessages().forEach(vMsg -> {
                LOG.error("Error: msg Type: %s errorcode: %s, msg: %s".formatted(vMsg.messageType(), vMsg.errorCode(), vMsg.interpolate(analyteGroupViewModel)) );
            });
            return;
        }
        EntityFacade analyte = analyteGroupViewModel.getValue(ANALYTE_ENTITY);

        List<EntityFacade> targets = analyteGroupViewModel.getValue(TARGET_ENTITIES);
        Set<TargetRecord> targetsSet = targets.stream().map(t -> new TargetRecord(t.publicId(), analyte.publicId())).collect(Collectors.toSet());

        List<EntityFacade> resultConformances = analyteGroupViewModel.getValue(RESULT_ENTITIES);
        Set<ResultConformanceRecord> resultConformanceSet = resultConformances.stream().map(s -> new ResultConformanceRecord(s.publicId())).collect(Collectors.toSet());
        List<EntityFacade> specimens = analyteGroupViewModel.getValue(SPECIMEN_ENTITIES);
        Set<SpecimenRecord> specimensSet = specimens.stream().map(s -> new SpecimenRecord(s.publicId())).collect(Collectors.toSet());
        /*
        PublicId lidrRecordId,
                         PublicId testPerformedId,
                         PublicId dataResultsTypeId,
                         AnalyteRecord analyte,
                         Set<TargetRecord> targets,
                         Set<SpecimenRecord> specimens,
                         Set<ResultConformanceRecord> resultConformances) {
         */
        AnalyteRecord analyteRecord = new AnalyteRecord(analyte.publicId());
        LidrRecord lidrRecord = new LidrRecord(null, null, null, analyteRecord, targetsSet, specimensSet, resultConformanceSet);
        evtBus.publish(getConceptTopic(), new AddResultInterpretationEvent(event.getSource(), AddResultInterpretationEvent.ADD_ANALYTE_GROUP, lidrRecord));
        evtBus.publish(getConceptTopic(), new LidrPropertyPanelEvent(event.getSource(), CLOSE_PANEL));

        clearView();
    }

    @FXML
    public void cancel(ActionEvent event) {
        // close properties bump out via event bus
        clearViewAndValidate();
        EvtBus evtBus = EvtBusFactory.getDefaultEvtBus();
        evtBus.publish(getConceptTopic(), new LidrPropertyPanelEvent(event.getSource(), CLOSE_PANEL));
    }

    @Override
    public void updateView() {

    }

    public void clearViewAndValidate() {
        clearView();
        analyteGroupViewModel.validate();
    }
    @FXML
    public void clearView(ActionEvent actionEvent) {
        clearViewAndValidate();
    }
    @Override
    public void clearView() {

        // remove analyte and add the search controls back
        clearDragNDropZones(selectedAnalyteContainer, () -> {
            analyteGroupViewModel.setPropertyValue(ANALYTE_ENTITY, null);
            analyteGroupVbox.getChildren().add(2, generateAnalyteSearchControls());
        });

        // remove targets and add the search controls back
        clearDragNDropZones(selectedTargetsContainer, () ->
            analyteGroupViewModel.getObservableList(TARGET_ENTITIES).clear());

        // remove all selected results conformences
        clearDragNDropZones(selectedResultContainer, () ->
            analyteGroupViewModel.getObservableList(RESULT_ENTITIES).clear());

        // remove all selected specimens
        clearDragNDropZones(selectedSpecimenContainer, () ->
            analyteGroupViewModel.getObservableList(SPECIMEN_ENTITIES).clear());

        // cancel means to clear properties and the model values.
        analyteGroupViewModel.save(true);

    }

    private void clearDragNDropZones(Pane selectedContainer, Runnable task) {
        // remove all selected items
        if (selectedContainer.getChildren().size() > 0) {
            selectedContainer.getChildren().clear();
            HBox.setMargin(selectedContainer, new Insets(0));
            VBox.setMargin(selectedContainer, new Insets(0));
            task.run();
        }
    }
    @Override
    public void cleanup() {

    }
}
