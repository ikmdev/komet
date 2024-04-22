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
package dev.ikm.komet.amplify.lidr.analyte;

import dev.ikm.komet.amplify.commons.BasicController;
import dev.ikm.komet.amplify.lidr.events.AddResultEvent;
import dev.ikm.komet.amplify.lidr.events.AddResultInterpretationEvent;
import dev.ikm.komet.amplify.lidr.events.LidrPropertyPanelEvent;
import dev.ikm.komet.amplify.lidr.events.ShowPanelEvent;
import dev.ikm.komet.amplify.mvvm.loader.InjectViewModel;
import dev.ikm.komet.amplify.lidr.viewmodels.AnalyteViewModel;
import dev.ikm.komet.framework.events.EvtBus;
import dev.ikm.komet.framework.events.EvtBusFactory;
import dev.ikm.komet.framework.events.Subscriber;
import dev.ikm.komet.framework.view.ViewProperties;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.Node;
import javafx.scene.control.Button;
import javafx.scene.input.Dragboard;
import javafx.scene.input.TransferMode;
import javafx.scene.layout.HBox;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Date;
import java.util.UUID;
import java.util.function.Consumer;

import static dev.ikm.komet.amplify.lidr.events.AddResultEvent.ADD_RESULT_TO_ANALYTE_GROUP;
import static dev.ikm.komet.amplify.lidr.events.LidrPropertyPanelEvent.CLOSE_PANEL;
import static dev.ikm.komet.amplify.lidr.events.ShowPanelEvent.SHOW_MANUAL_ADD_RESULTS;
import static dev.ikm.komet.amplify.lidr.viewmodels.AnalyteViewModel.RESULTS_ENTITY;
import static dev.ikm.komet.amplify.viewmodels.FormViewModel.CONCEPT_TOPIC;
import static dev.ikm.komet.amplify.viewmodels.FormViewModel.VIEW_PROPERTIES;

public class AnalyteGroupController implements BasicController {

    private static final Logger LOG = LoggerFactory.getLogger(AnalyteGroupController.class);
    @FXML
    private HBox analyteDragNDropArea;
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

    @InjectViewModel
    private AnalyteViewModel analyteViewModel;


    @Override
    @FXML
    public void initialize() {
        //TODO we will need an event bus for the LIDR record

        // we need an instance of the EditCoordinateRecord in
        // order to save/update the device and manufacturer concepts

        clearView();

        // setup drag n drop
        setupDragNDrop(analyteDragNDropArea, (publicId) -> {
            // query public Id to get entity.
            // Entity e = ...;
            // analyteViewModel.setPropertyValue(ANALYTE_ENTITY, e);
            analyteViewModel.save();
        });
        setupDragNDrop(resultsDragNDropArea, (publicId) -> {
            // query public Id to get entity.
            // Entity e = ...;
            // analyteViewModel.setPropertyValue(RESULTS_ENTITY, e);
            analyteViewModel.save();
        });
        setupDragNDrop(specimensDragNDropArea, (publicId) -> {
            // query public Id to get entity.
            // Entity e = ...;
            // analyteViewModel.setPropertyValue(SPECIMEN_ENTITY, e);
            analyteViewModel.save();
        });

        // When user created a manual result entry to be added to analyte view model.
        Subscriber<AddResultEvent> manualAddResultSubscriber = (evt -> {
            if (evt.getEventType() == ADD_RESULT_TO_ANALYTE_GROUP) {
                analyteViewModel.setPropertyValue(RESULTS_ENTITY, evt.getOneResult());
            }
        });
        EvtBusFactory.getDefaultEvtBus().subscribe(getConceptTopic(), AddResultEvent.class, manualAddResultSubscriber);
    }
    private ViewProperties getViewProperties() {
        return analyteViewModel.getPropertyValue(VIEW_PROPERTIES);
    }
    private UUID getConceptTopic() {
        return analyteViewModel.getPropertyValue(CONCEPT_TOPIC);
    }
    private void setupDragNDrop(Node node, Consumer<String> consumer) {

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

            event.consume();
        });

        // restore change
        node.setOnDragExited(event -> {
            /* mouse moved away, remove the graphical cues */
            node.setOpacity(1);
            event.consume();
        });

        node.setOnDragDropped(event -> {
            /* data dropped */
            /* if there is a string data on dragboard, read it and use it */
            Dragboard db = event.getDragboard();
            boolean success = false;
            if (db.hasString()) {
                String publicId = db.getString(); // TODO find entity by public id.
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
        // TODO gather all data from view model after save. To be added to the details controller. One result interpretation is an object as a payload.

        //analyteViewModel.reset();
        EvtBus evtBus = EvtBusFactory.getDefaultEvtBus();
        evtBus.publish(getConceptTopic(), new AddResultInterpretationEvent(event.getSource(), AddResultInterpretationEvent.ADD_ANALYTE_GROUP, new Object(){
            @Override
            public String toString() {
                return "One ADD_ANALYTE_GROUP added to device " + new Date();
            }
        }));
        evtBus.publish(getConceptTopic(), new LidrPropertyPanelEvent(event.getSource(), CLOSE_PANEL));
    }

    @FXML
    public void createDevice() {
        //deviceViewModel.createDevice()
    }

    @FXML
    public void cancel(ActionEvent event) {
        // close properties bump out via event bus
        analyteViewModel.reset();
        EvtBus evtBus = EvtBusFactory.getDefaultEvtBus();
        evtBus.publish(getConceptTopic(), new LidrPropertyPanelEvent(event.getSource(), CLOSE_PANEL));
    }

    @Override
    public void updateView() {

    }

    @FXML
    public void clearForm(ActionEvent event) {
        clearView();
        // TODO update UI with new prop values
    }
    @Override
    public void clearView() {
        // reset view model
        analyteViewModel.reset();
    }

    @Override
    public void cleanup() {

    }
}
