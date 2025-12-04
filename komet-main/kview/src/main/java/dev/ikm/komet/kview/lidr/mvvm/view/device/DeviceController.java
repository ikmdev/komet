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
package dev.ikm.komet.kview.lidr.mvvm.view.device;

import dev.ikm.komet.kview.lidr.mvvm.viewmodel.DeviceViewModel;
import dev.ikm.komet.kview.mvvm.view.BasicController;
import dev.ikm.komet.kview.lidr.events.AddDeviceEvent;
import dev.ikm.komet.kview.lidr.events.LidrPropertyPanelEvent;
import dev.ikm.komet.framework.Identicon;
import dev.ikm.tinkar.events.EvtBus;
import dev.ikm.tinkar.events.EvtBusFactory;
import dev.ikm.komet.framework.search.SearchPanelController;
import dev.ikm.komet.framework.search.SearchResultCell;
import dev.ikm.komet.framework.view.ViewProperties;
import dev.ikm.komet.navigator.graph.MultiParentGraphCell;
import dev.ikm.tinkar.common.id.PublicId;
import dev.ikm.tinkar.entity.Entity;
import dev.ikm.tinkar.entity.EntityService;
import dev.ikm.tinkar.terms.ConceptFacade;
import javafx.beans.property.BooleanProperty;
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

import java.util.UUID;
import java.util.function.Consumer;

import static dev.ikm.komet.kview.lidr.events.AddDeviceEvent.ADD_DEVICE;
import static dev.ikm.komet.kview.lidr.events.LidrPropertyPanelEvent.CLOSE_PANEL;
import static dev.ikm.komet.kview.lidr.mvvm.viewmodel.DeviceViewModel.DEVICE_ENTITY;
import static dev.ikm.komet.kview.lidr.mvvm.viewmodel.DeviceViewModel.IS_INVALID;
import static dev.ikm.komet.kview.mvvm.viewmodel.FormViewModel.CONCEPT_TOPIC;
import static dev.ikm.komet.kview.mvvm.viewmodel.FormViewModel.VIEW_PROPERTIES;

public class DeviceController {

    private static final Logger LOG = LoggerFactory.getLogger(DeviceController.class);
    public static final String DRAG_AND_DROP_CONCEPT_S_HERE = "Drag and drop concept(s) here";
    @FXML
    private HBox deviceDragAndDropArea;

    @FXML
    private Button cancelButton;

    @FXML
    private Button clearButton;

    @FXML
    private Button doneButton;

    @FXML
    private StackPane outerSearchStackPane;

    @FXML
    private VBox selectedDeviceContainer;

    @FXML
    private StackPane selectedDeviceStackPane;

    @FXML
    private HBox selectedConcept;

    @FXML
    private VBox deviceOuterVBox;

    @FXML
    private VBox searchFormVBox;

    @InjectViewModel
    private DeviceViewModel deviceViewModel;

    EvtBus evtBus = EvtBusFactory.getDefaultEvtBus();
    private Consumer<PublicId> deviceDropListener;
    @FXML
    private void initialize() {

        // The is invalid property is set when ever a view model's validation occurs.
        BooleanProperty isInvalidProp = deviceViewModel.getProperty(IS_INVALID);
        // The done button's disabled property is synced to isValid property.
        // IMPORTANT: When this Pane is shown it then changes IS_INVALID property to true.
        // e.g. initialize (IS_INVALID prop set to true via view model) see PropertiesController.initialize().
        doneButton.disableProperty().bind(isInvalidProp);

        // When a user drops a device entity query and update property.
        deviceDropListener = (publicId) -> {
            // check to see if an analyte was already dragged into the analyte section before saving
            // to the view model
            if (deviceViewModel.getPropertyValue(DEVICE_ENTITY) == null) {
                // query public Id to get entity.
                Entity entity = EntityService.get().getEntityFast(EntityService.get().nidForPublicId(publicId));
                deviceViewModel.setPropertyValue(DEVICE_ENTITY, entity);
                addDeviceToForm(entity);

                // This validate will update IS_INVALID property for done button's disable property
                deviceViewModel.validate();
            }
        };
        // we need an instance of the EditCoordinateRecord in
        // order to save/update the device and manufacturer concepts
        clearView();

        // setup drag n drop
        setupDragNDrop(outerSearchStackPane, deviceDropListener);
    }

    public ViewProperties getViewProperties() {
        return deviceViewModel.getPropertyValue(VIEW_PROPERTIES);
    }

    private UUID getConceptTopic() {
        return deviceViewModel.getPropertyValue(CONCEPT_TOPIC);
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
            if (outerSearchStackPane != null && outerSearchStackPane.getChildren().size() > 0) {
                int lastIndex = outerSearchStackPane.getChildren().size();
                outerSearchStackPane.getChildren().add(lastIndex, createDragOverAnimation());
            }

            event.consume();
        });

        // restore change
        node.setOnDragExited(event -> {
            /* mouse moved away, remove the graphical cues */
            node.setOpacity(1);
            if (outerSearchStackPane != null) {
                int lastIndex = outerSearchStackPane.getChildren().size();
                outerSearchStackPane.getChildren().remove(lastIndex - 1, lastIndex);
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

    /**
     * create a JavaFX node for the device concept and add it to
     * above the search form
     *
     * @param entity
     */
    private void addDeviceToForm(Entity entity) {

        // update the UI to show the device that the user just dragged (or searched or manually entered)
        selectedConcept = new HBox();

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
        selectedConcept.getChildren().add(imageViewWrapper);

        // create the label
        String conceptName = entity.description();
        Label conceptNameLabel = new Label(conceptName);
        conceptNameLabel.getStyleClass().add("lidr-device-entry-label");
        selectedConcept.getChildren().add(conceptNameLabel);

        // format the device HBox
        selectedConcept.getStyleClass().add("lidr-device-entry");
        selectedConcept.setAlignment(Pos.CENTER_LEFT);
        selectedConcept.setPadding(new Insets(0, 0, 4, 0));
        HBox.setMargin(selectedDeviceContainer, new Insets(8));

        // add the close 'X' button to the right side of the device container
        Button closeButton = new Button();
        closeButton.getStyleClass().add("lidr-search-button");
        Region buttonRegion = new Region();
        buttonRegion.getStyleClass().add("lidr-device-entry-close-button");
        closeButton.setGraphic(buttonRegion);
        closeButton.setAlignment(Pos.CENTER_RIGHT);
        selectedConcept.getChildren().add(closeButton);
        closeButton.setOnMouseClicked(event -> {
            // This validate will update IS_INVALID property for done button's disable property
            clearViewAndValidate();
        });

        selectedDeviceContainer.getChildren().add(selectedConcept);

        VBox.setMargin(selectedDeviceStackPane, new Insets(0,0, 8,0));
        removeDeviceForm();
    }

    private void removeDeviceForm() {
        if (deviceOuterVBox != null) {
            deviceOuterVBox.getChildren().remove(2, 3);
        }
    }

    private void removeDevice() {
        if (selectedDeviceContainer.getChildren().size() > 0) {
            deviceViewModel.setPropertyValue(DEVICE_ENTITY, null);
            selectedDeviceContainer.getChildren().remove(selectedConcept);
            HBox.setMargin(selectedDeviceContainer, new Insets(0));
            VBox.setMargin(selectedDeviceStackPane, new Insets(0));
            deviceOuterVBox.getChildren().add(2, generateDeviceSearchControls());
        }
    }

    private Node generateDeviceSearchControls() {
        // containers
        VBox deviceVbox = new VBox();
        VBox.setMargin(deviceVbox, new Insets(0, 0, 16, 0));
        outerSearchStackPane = new StackPane();
        outerSearchStackPane.setId("outerSearchStackPane");
        Region deviceRegion = new Region();
        deviceRegion.getStyleClass().add("lidr-rounded-region");
        VBox searchAndDragDropVbox = new VBox();
        StackPane.setMargin(searchAndDragDropVbox, new Insets(8));

        // search with button
        HBox searchHbox = new HBox();
        TextField searchTextField = new TextField();
        HBox.setHgrow(searchTextField, Priority.ALWAYS);
        // magnifying glass character
        searchTextField.setPromptText("\uD83D\uDD0D  Search Devices");
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
        dragDropInnerHbox.setId("deviceDragAndDropArea");
        HBox.setHgrow(dragDropInnerHbox, Priority.ALWAYS);
        dragDropInnerHbox.setAlignment(Pos.CENTER);
        dragDropInnerHbox.getStyleClass().add("lidr-device-drag-and-drop-hbox");
        dragDropInnerHbox.setPrefWidth(200);
        dragDropInnerHbox.setPrefHeight(100);
        StackPane dragIconStack = new StackPane();
        Region dragIcon = new Region();
        dragIcon.getStyleClass().add("lidr-device-drag-and-drop-icon");
        Label dragLabel = new Label(DRAG_AND_DROP_CONCEPT_S_HERE);
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
        outerSearchStackPane.getChildren().addAll(deviceRegion, searchAndDragDropVbox);
        deviceVbox.getChildren().add(outerSearchStackPane);

        // re-attach the drag and drop capability
        setupDragNDrop(outerSearchStackPane, deviceDropListener);

        return deviceVbox;
    }

    @FXML
    public void selectDevice(ActionEvent event) {
        LOG.info("createDevice -> Todo publish event containing the device record to be added to lidr details view.");
        deviceViewModel.save();
        if (deviceViewModel.hasNoErrorMsgs()) {
            // 1. publish
            // 2. reset screen for next entry
            // 3. publish
            evtBus.publish(getConceptTopic(), new LidrPropertyPanelEvent(event.getSource(), CLOSE_PANEL));
            evtBus.publish(getConceptTopic(), new AddDeviceEvent(event.getSource(), ADD_DEVICE, deviceViewModel.getValue(DEVICE_ENTITY)));
            clearView(); // This DOES NOT set IS_INVALID flag to true.
        }
    }

    @FXML
    public void cancel(ActionEvent event) {
        EvtBus evtBus = EvtBusFactory.getDefaultEvtBus();
        evtBus.publish(getConceptTopic(), new LidrPropertyPanelEvent(event.getSource(), CLOSE_PANEL));
        // close properties bump out via event bus
        clearViewAndValidate();
    }
    public void clearViewAndValidate() {
        removeDevice();
        // This validate will update IS_INVALID property for done button's disable property
        deviceViewModel.validate();
    }

    /**
     * Clears screen but does not validate or change IS_INVALID property.
     */
    public void clearView() {
        removeDevice();
    }

    @FXML
    public void clearView(ActionEvent event) {
        clearViewAndValidate();
    }
}
