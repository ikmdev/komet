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
import dev.ikm.komet.framework.events.EvtBus;
import dev.ikm.komet.framework.events.EvtBusFactory;
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

import java.util.UUID;

import static dev.ikm.komet.amplify.lidr.events.AddDeviceEvent.ADD_DEVICE;
import static dev.ikm.komet.amplify.lidr.events.LidrPropertyPanelEvent.CLOSE_PANEL;
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
                success = true;
            }
            /* let the source know whether the string was successfully
             * transferred and used */
            event.setDropCompleted(success);

            event.consume();
        });
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
        deviceViewModel.reset(); // clear view model?
        EvtBus evtBus = EvtBusFactory.getDefaultEvtBus();
        evtBus.publish(getConceptTopic(), new LidrPropertyPanelEvent(event.getSource(), CLOSE_PANEL));
    }


    @Override
    public void updateView() {

    }

    @Override
    @FXML
    public void clearView() {

    }

    @Override
    public void cleanup() {

    }
}
