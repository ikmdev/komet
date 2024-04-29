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
package dev.ikm.komet.amplify.lidr.device;

import dev.ikm.komet.amplify.commons.BasicController;
import dev.ikm.komet.amplify.lidr.events.AddDeviceEvent;
import dev.ikm.komet.amplify.lidr.events.LidrPropertyPanelEvent;
import dev.ikm.komet.amplify.mvvm.loader.InjectViewModel;
import dev.ikm.komet.amplify.lidr.viewmodels.DeviceViewModel;
import dev.ikm.komet.framework.Identicon;
import dev.ikm.komet.framework.events.EvtBus;
import dev.ikm.komet.framework.events.EvtBusFactory;
import dev.ikm.komet.framework.search.SearchPanelController;
import dev.ikm.komet.framework.search.SearchResultCell;
import dev.ikm.komet.framework.view.ViewProperties;
import dev.ikm.komet.navigator.graph.MultiParentGraphCell;
import dev.ikm.tinkar.common.id.PublicId;
import dev.ikm.tinkar.entity.Entity;
import dev.ikm.tinkar.entity.EntityService;
import dev.ikm.tinkar.entity.EntityVersion;
import dev.ikm.tinkar.terms.ConceptFacade;
import dev.ikm.tinkar.terms.EntityFacade;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Node;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.input.Dragboard;
import javafx.scene.input.TransferMode;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Pane;
import javafx.scene.layout.Region;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Optional;
import java.util.UUID;

import static dev.ikm.komet.amplify.lidr.events.AddDeviceEvent.ADD_DEVICE;
import static dev.ikm.komet.amplify.lidr.events.LidrPropertyPanelEvent.CLOSE_PANEL;
import static dev.ikm.komet.amplify.lidr.viewmodels.DeviceViewModel.DEVICE_ENTITY;
import static dev.ikm.komet.amplify.viewmodels.FormViewModel.CONCEPT_TOPIC;
import static dev.ikm.komet.amplify.viewmodels.FormViewModel.VIEW_PROPERTIES;

public class DeviceController implements BasicController {

    private static final Logger LOG = LoggerFactory.getLogger(DeviceController.class);
    @FXML
    private HBox deviceDragAndDropArea;

    @FXML
    private Button cancelButton;

    @FXML
    private Button clearButton;

    @FXML
    private Button doneButton;

    @FXML
    private VBox selectedDeviceContainer;

    @FXML
    private StackPane selectedDeviceStackPane;

    private HBox selectedConcept;


    @InjectViewModel
    private DeviceViewModel deviceViewModel;
    EvtBus evtBus = EvtBusFactory.getDefaultEvtBus();

    @Override
    @FXML
    public void initialize() {
        //TODO we will need an event bus for the LIDR record

        // we need an instance of the EditCoordinateRecord in
        // order to save/update the device and manufacturer concepts

        clearView();

        // setup drag n drop
        setupDragNDrop(deviceDragAndDropArea);
    }

    public ViewProperties getViewProperties() {
        return deviceViewModel.getPropertyValue(VIEW_PROPERTIES);
    }

    private UUID getConceptTopic() {
        return deviceViewModel.getPropertyValue(CONCEPT_TOPIC);
    }

    private void setupDragNDrop(Node node) {

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
                ConceptFacade conceptFacade = null;
                if (event.getGestureSource() instanceof SearchResultCell) {
                    SearchPanelController.NidTextRecord nidTextRecord = (SearchPanelController.NidTextRecord) ((SearchResultCell) event.getGestureSource()).getItem();
                    conceptFacade = ConceptFacade.make(nidTextRecord.nid());
                } else if (event.getGestureSource() instanceof MultiParentGraphCell) {
                    conceptFacade = ((MultiParentGraphCell) event.getGestureSource()).getItem();
                }
                // add the component to the device view model
                if (conceptFacade != null && deviceViewModel.getPropertyValue(DEVICE_ENTITY) == null) {
                    addDeviceToForm(conceptFacade);
                }
                success = true;
            }
            /* let the source know whether the string was successfully
             * transferred and used */
            event.setDropCompleted(success);

            event.consume();
        });
    }

    /**
     * create a JavaFX node for the device concept and add it to
     * above the search form
     *
     * @param conceptFacade
     */
    private void addDeviceToForm(ConceptFacade conceptFacade) {
        // add the device to the view model
        deviceViewModel.setPropertyValue(DEVICE_ENTITY, conceptFacade);

        // update the UI to show the device that the user just dragged (or searched or manually entered)
        selectedConcept = new HBox();

        // create identicon for the concept and add it to the left hbox
        Image identicon = Identicon.generateIdenticonImage(conceptFacade.publicId());
        ImageView imageView = new ImageView();
        imageView.setFitWidth(20);
        imageView.setFitHeight(20);
        imageView.setImage(identicon);
        HBox imageViewWrapper = new HBox();
        imageViewWrapper.setAlignment(Pos.CENTER);
        HBox.setMargin(imageView, new Insets(0, 8, 0 ,8));
        imageViewWrapper.getChildren().add(imageView);
        selectedConcept.getChildren().add(imageViewWrapper);

        // create the label
        String conceptName = conceptFacade.description();
        Label conceptNameLabel = new Label(conceptName);
        conceptNameLabel.getStyleClass().add("lidr-device-entry-label");
        selectedConcept.getChildren().add(conceptNameLabel);

        // format the device HBox
        selectedConcept.getStyleClass().add("lidr-device-entry");
        selectedConcept.setAlignment(Pos.CENTER_LEFT);
        selectedConcept.setPadding(new Insets(4, 0, 4, 0));
        HBox.setMargin(selectedDeviceContainer, new Insets(8));

        // add the close 'X' button to the right side of the device container
        Button closeButton = new Button();
        closeButton.getStyleClass().add("lidr-search-button");
        Region buttonRegion = new Region();
        buttonRegion.getStyleClass().add("lidr-device-entry-close-button");
        closeButton.setGraphic(buttonRegion);
        closeButton.setAlignment(Pos.CENTER_RIGHT);
        selectedConcept.getChildren().add(closeButton);
        closeButton.setOnMouseClicked(event -> removeDevice());

        selectedDeviceContainer.getChildren().add(selectedConcept);

        VBox.setMargin(selectedDeviceStackPane, new Insets(0,0, 8,0));
        doneButton.setDisable(false);
    }

    private void removeDevice() {
        deviceViewModel.setPropertyValue(DEVICE_ENTITY, null);
        selectedDeviceContainer.getChildren().remove(selectedConcept);
        HBox.setMargin(selectedDeviceContainer, new Insets(0));
        VBox.setMargin(selectedDeviceStackPane, new Insets(0));
        doneButton.setDisable(true);
    }

    @FXML
    public void createDevice(ActionEvent event) {
        // TODO create device and publish to lidr details controller.
        LOG.info("createDevice -> Todo publish event containing the device record to be added to lidr details controller.");
        // 1. publish
        // 2. reset screen for next entry
        // 3. publish
        Object device = new Object();
        evtBus.publish(getConceptTopic(), new LidrPropertyPanelEvent(event.getSource(), CLOSE_PANEL));

        // TODO put a real entity or public id as the payload.
        evtBus.publish(getConceptTopic(), new AddDeviceEvent(event.getSource(), ADD_DEVICE, null));
    }

    @FXML
    public void cancel(ActionEvent event) {
        // close properties bump out via event bus
        clearView();
        EvtBus evtBus = EvtBusFactory.getDefaultEvtBus();
        evtBus.publish(getConceptTopic(), new LidrPropertyPanelEvent(event.getSource(), CLOSE_PANEL));
    }


    @Override
    public void updateView() {

    }

    @Override
    @FXML
    public void clearView() {
        removeDevice();
    }

    @Override
    public void cleanup() {

    }
}
