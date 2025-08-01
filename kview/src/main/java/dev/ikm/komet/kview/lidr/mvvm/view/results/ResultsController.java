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
package dev.ikm.komet.kview.lidr.mvvm.view.results;

import dev.ikm.komet.framework.Identicon;
import dev.ikm.tinkar.events.EvtBus;
import dev.ikm.tinkar.events.EvtBusFactory;
import dev.ikm.tinkar.events.Subscriber;
import dev.ikm.komet.framework.search.SearchPanelController;
import dev.ikm.komet.framework.search.SearchResultCell;
import dev.ikm.komet.kview.data.schema.STAMPDetail;
import dev.ikm.komet.kview.events.StampModifiedEvent;
import dev.ikm.komet.kview.fxutils.ComboBoxHelper;
import dev.ikm.komet.kview.lidr.events.AddResultEvent;
import dev.ikm.komet.kview.lidr.events.ShowPanelEvent;
import dev.ikm.komet.kview.lidr.mvvm.viewmodel.ResultsViewModel;
import dev.ikm.komet.kview.lidr.mvvm.viewmodel.ViewModelHelper;
import dev.ikm.komet.kview.mvvm.view.AbstractBasicController;
import dev.ikm.komet.kview.mvvm.view.BasicController;
import dev.ikm.komet.kview.mvvm.viewmodel.FormViewModel;
import dev.ikm.komet.navigator.graph.MultiParentGraphCell;
import dev.ikm.tinkar.common.id.PublicId;
import dev.ikm.tinkar.entity.ConceptEntity;
import dev.ikm.tinkar.entity.Entity;
import dev.ikm.tinkar.entity.EntityService;
import dev.ikm.tinkar.terms.ComponentWithNid;
import dev.ikm.tinkar.terms.ConceptFacade;
import dev.ikm.tinkar.terms.EntityFacade;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Node;
import javafx.scene.control.*;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.input.Dragboard;
import javafx.scene.input.TransferMode;
import javafx.scene.layout.*;
import org.carlfx.cognitive.loader.InjectViewModel;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.*;
import java.util.function.Consumer;

import static dev.ikm.komet.kview.lidr.events.AddResultEvent.ADD_RESULT_TO_ANALYTE_GROUP;
import static dev.ikm.komet.kview.lidr.events.ShowPanelEvent.SHOW_ADD_ANALYTE_GROUP;
import static dev.ikm.komet.kview.lidr.mvvm.model.DataModelHelper.*;
import static dev.ikm.komet.kview.lidr.mvvm.viewmodel.ResultsViewModel.*;
import static dev.ikm.komet.kview.mvvm.viewmodel.FormViewModel.CONCEPT_TOPIC;
import static dev.ikm.komet.kview.mvvm.viewmodel.FormViewModel.VIEW_PROPERTIES;

/**
 * Manual Entry for Results Conformance Panel.
 * This screen has a toggle between Qualitative and Quantitative results. When the user selects Qualitative
 * the following fields are shown:
 * <pre>
 *     Name - Result name
 *     Scale Type - Ordinal (enumeration), units. Think of Ordinal as enumerated values such as DETECTED, NOT_DETECTED, UNKNOWN.
 *     Allowable Results - Ordinal values (concepts). If using units, values are numeric.
 * </pre>
 */
public class ResultsController extends AbstractBasicController implements BasicController {

    private static final Logger LOG = LoggerFactory.getLogger(ResultsController.class);
    public static final String DRAG_AND_DROP_CONCEPT_S_HERE = "Drag and drop concept(s) here";
    @FXML
    private TextField resultName;

    @FXML
    private Button cancelButton;

    @FXML
    private Button clearButton;

    @FXML
    private Button addButton;

    @FXML
    private RadioButton qualitativeRadioButton;

    @FXML
    private RadioButton quantitativeRadioButton;

    @FXML
    private ToggleGroup dataResultTypeGroup;

    @FXML
    private VBox resultsFormContainer;

    @FXML
    private ComboBox<EntityFacade> scaleTypeComboBox;

    @InjectViewModel
    private ResultsViewModel resultsViewModel;



    private STAMPDetail stampDetail;

    ///////////////////////////////////////////////////
    private VBox generatedResultsFormContainer;

    private List<Node> qualitativeFields = new ArrayList<>();
    private List<Node> quantitativeFields = new ArrayList<>();

    private EvtBus evtBus = EvtBusFactory.getDefaultEvtBus();

    @Override
    @FXML
    public void initialize() {
        // The caller updated the view model's view properties object. This class extends from AbstractBasicController having updateModel() method.
        updateModel(resultsViewModel.getPropertyValue(VIEW_PROPERTIES));

        clearView();
        // Create subscriber to listen to when lidr details stamp changes
        Subscriber<StampModifiedEvent> stampModifiedEventSubscriber = (event) -> {
            setStampDetail(event.getStampDetail());
        };
        evtBus.subscribe(getConceptTopic(), StampModifiedEvent.class, stampModifiedEventSubscriber);
        addButton.disableProperty().bind(resultsViewModel.getProperty(ADD_BUTTON_STATE));

        // TODO: Add the additional Scale types to populate the dropdown (combobox). For now we just add Ordinal as a valid Scale Type.
        scaleTypeComboBox.getItems().add(ORDINAL_CONCEPT);
        resultsViewModel.setPropertyValue(SCALE_TYPE, ORDINAL_CONCEPT);

        // adds a listener when user changes value & code that displays a combobox cell.
        // params are: combobox, listener, and cellFactory.
        ComboBoxHelper.setupComboBox(scaleTypeComboBox, (observable ->
            // when chosen add new item to view model and validate
            resultsViewModel
                    .setPropertyValue(SCALE_TYPE, scaleTypeComboBox.getSelectionModel().getSelectedItem())
                    .validate()),

                // how to display text in a combo box cell.
                (EntityFacade entity) ->
                    getViewProperties()
                            .calculator()
                            .getRegularDescriptionText(entity.nid())
                            .orElse("")
        );

        // As user types text the view model is updated.
        resultName.textProperty().addListener((observableValue, s, t1) -> {
            resultsViewModel.setPropertyValue(RESULTS_NAME, t1);
            resultsViewModel.validate();
        });

        // By default select Qualitative
        dataResultTypeGroup
                .selectedToggleProperty()
                .addListener((obs, oldValue, newValue) -> {
                    removeAmendedForm();
                    // if qualitative, show allowable results
                    // if quantitative show fields for quantitative
                    if (dataResultTypeGroup.getSelectedToggle().equals(qualitativeRadioButton)) {
                        // set data result
                        resultsViewModel.setPropertyValue(DATA_RESULTS_TYPE, QUALITATIVE_CONCEPT);
                        showQualitativeForm(true);
                    } else if (dataResultTypeGroup.getSelectedToggle().equals(quantitativeRadioButton)) {
                        resultsViewModel.setPropertyValue(DATA_RESULTS_TYPE, QUANTITATIVE_CONCEPT);
                        showQuantitativeForm();
                    }
                    resultsViewModel.validate();
                });
        // Default to showing the qualitative fields.
        resultsViewModel.setPropertyValue(DATA_RESULTS_TYPE, QUALITATIVE_CONCEPT);
        showQualitativeForm(true);
    }

    private void setStampDetail(STAMPDetail stampDetail) {
        this.stampDetail = stampDetail;
    }
    public STAMPDetail getStampDetail() {
        return stampDetail;
    }
    private VBox getGeneratedResultsFormContainer() {
        return generatedResultsFormContainer;
    }

    private void setGeneratedResultsFormContainer(VBox generatedResultsFormContainer) {
        this.generatedResultsFormContainer = generatedResultsFormContainer;
    }

    private void removeAmendedForm() {
        resultsFormContainer.getChildren().clear();
    }

    /**
     * when the user clicks quantitative,
     * they will then see:
     *      Example Units
     *      Reference Ranges
     */
    private void showQuantitativeForm() {
        if (quantitativeFields.size() > 0) {
            resultsFormContainer.getChildren().addAll(quantitativeFields);
            return;
        }

        // create the label
        Label exampleUnitsLabel = new Label("Example Units");
        exampleUnitsLabel.getStyleClass().add("lidr-device-label");

        VBox.setMargin(exampleUnitsLabel, new Insets(0,0, 8,0));

        // create the container for the search box and the search button
        HBox exampleUnitsSearchContainer = new HBox();
        TextField exampleUnitsTextField = new TextField();
        // below is a magnifying glass icon inside the prompt text
        exampleUnitsTextField.setPromptText("\uD83D\uDD0D  Search Example Units");
        exampleUnitsTextField.getStyleClass().add("lidr-search-device-text-input");
        HBox.setHgrow(exampleUnitsTextField, Priority.ALWAYS);
        Button exampleUnitsSearchButton = new Button();
        exampleUnitsSearchButton.getStyleClass().add("lidr-search-button");
        Region buttonRegion = new Region();
        buttonRegion.getStyleClass().addAll("lidr-search-button-region", "icon");
        exampleUnitsSearchButton.setGraphic(buttonRegion);

        // put the text field and the button in the HBox
        exampleUnitsSearchContainer.getChildren().addAll(exampleUnitsTextField, exampleUnitsSearchButton);
        VBox.setMargin(exampleUnitsSearchContainer, new Insets(0,0, 8,0));

        // add the reference ranges

        // create the reference ranges label
        Label referenceRangesLabel = new Label("Reference Ranges");
        referenceRangesLabel.getStyleClass().add("lidr-device-label");
        VBox.setMargin(referenceRangesLabel, new Insets(0,0, 8,0));

        // create the reference ranges combobox
        ComboBox referenceRangesComboBox = new ComboBox<>();
        referenceRangesComboBox.getStyleClass().add("lidr-combo-box");
        referenceRangesComboBox.setMaxWidth(Double.MAX_VALUE);
        referenceRangesComboBox.setPromptText("Enter Reference Ranges");

        // put the label and the hbox in the VBox container
        resultsFormContainer.getChildren().addAll(exampleUnitsLabel, exampleUnitsSearchContainer,
                // add the reference range label and combobox
                referenceRangesLabel, referenceRangesComboBox);
        quantitativeFields.addAll(resultsFormContainer.getChildren());
    }

    /**
     * when the user clicks qualitative,
     * they will then see:
     *      Allowable Result
     */
    private void showQualitativeForm(boolean includeLabel) {
        if (qualitativeFields.size() > 0) {
            resultsFormContainer.getChildren().addAll(qualitativeFields);
            return;
        }

        // create a stack pane fo the drop shadow region, the search input and the drag
        // and drop box below the search
        StackPane stackPane = new StackPane();
        Region dropShadow = new Region();
        dropShadow.getStyleClass().add("lidr-rounded-region");
        VBox containerVbox = new VBox();

        // create the container for the search box and the search button
        HBox allowableSearchHbox = new HBox();
        TextField allowableResultTextField = new TextField();
        // below is a magnifying glass icon inside the prompt text
        allowableResultTextField.setPromptText("\uD83D\uDD0D  Search ");
        allowableResultTextField.getStyleClass().add("lidr-search-device-text-input");
        HBox.setHgrow(allowableResultTextField, Priority.ALWAYS);
        Button allowableResultSearchButton = new Button();
        allowableResultSearchButton.getStyleClass().add("lidr-search-button");
        Region buttonRegion = new Region();
        buttonRegion.getStyleClass().addAll("lidr-search-button-region", "icon");
        allowableResultSearchButton.setGraphic(buttonRegion);

        // put the text field and the button in the HBox
        allowableSearchHbox.getChildren().addAll(allowableResultTextField, allowableResultSearchButton);
        StackPane.setMargin(allowableSearchHbox, new Insets(8));

        // HBox to contain the drag and drop
        HBox dragDropHboxOuter = new HBox();
        HBox dragDropHboxInner = new HBox();
        dragDropHboxInner.getStyleClass().add("lidr-device-drag-and-drop-hbox");
        StackPane stackPaneDragDropIcon = new StackPane();
        Region dragDropRegion = new Region();
        dragDropRegion.getStyleClass().add("lidr-device-drag-and-drop-icon");
        dragDropRegion.setPrefHeight(20);
        dragDropRegion.setPrefWidth(20);
        Label dragDropLabel = new Label("Drag and drop concept(s) here");
        dragDropLabel.getStyleClass().add("lidr-device-drag-and-drop-label");
        HBox.setMargin(dragDropLabel, new Insets(0, 0, 0, 10));

        stackPaneDragDropIcon.getChildren().add(dragDropRegion);
        dragDropHboxInner.getChildren().addAll(stackPaneDragDropIcon, dragDropLabel);
        dragDropHboxInner.setAlignment(Pos.CENTER);
        HBox.setHgrow(dragDropHboxInner, Priority.ALWAYS);

        dragDropHboxOuter.getChildren().add(dragDropHboxInner);
        VBox.setMargin(allowableSearchHbox, new Insets(0, 0, 8, 0));
        containerVbox.getChildren().addAll(allowableSearchHbox, dragDropHboxOuter);
        StackPane.setMargin(containerVbox, new Insets(8));

        // add the drop shadow region and the search hbox to the stack pane
        stackPane.getChildren().addAll(dropShadow, containerVbox);

        // create the vbox area where the draggable concept will be dropped into
        VBox droppedAreaVbox = new VBox();
        StackPane dropStackPane = new StackPane();
        Region dropRegion = new Region();
        dropRegion.getStyleClass().add("lidr-rounded-region");
        HBox dropHbox = new HBox();
        VBox innerDropVbox = new VBox();
        dropHbox.getChildren().add(innerDropVbox);
        dropStackPane.getChildren().addAll(dropRegion, dropHbox);
        droppedAreaVbox.getChildren().add(dropStackPane);
        // create an allowable result form entry


        if (includeLabel) {
            // create the label
            Label allowableResultLabel = new Label("Allowable Result");
            allowableResultLabel.getStyleClass().add("lidr-device-label");
            VBox.setMargin(allowableResultLabel, new Insets(0, 0, 8, 0));
            resultsFormContainer.getChildren().addAll(allowableResultLabel, droppedAreaVbox, stackPane);
        } else {
            resultsFormContainer.getChildren().addAll(droppedAreaVbox, stackPane);
        }
        qualitativeFields.addAll(resultsFormContainer.getChildren());

        setupDragNDrop(stackPane, (chosenAllowResultPublicId) -> {
            List<EntityFacade> allowableResults = resultsViewModel.getObservableList(ALLOWABLE_RESULTS);

            // query public Id to get entity.
            Entity entity = EntityService.get().getEntityFast(EntityService.get().nidForPublicId(chosenAllowResultPublicId));
            // there can be one to many results
            allowableResults.add(entity);
            resultsViewModel.validate();
            setGeneratedResultsFormContainer(innerDropVbox);
            // update the UI with the new allowable result
            addConceptAndRemoveForm(entity, innerDropVbox, dropStackPane, ALLOWABLE_RESULTS);
        });
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

    public void showSelectedAllowableResult(Entity entity, VBox selectedVBoxContainer, StackPane selectedStackPane, String propertyName) {
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


        closeButton.setOnMouseClicked(event -> {
            removeAllowableResult(selectedHbox, propertyName, selectedVBoxContainer, selectedStackPane, entity);
            resultsViewModel.validate();
        });

        // remove the search and drag and drop when they have just one selection
        selectedVBoxContainer.getChildren().add(selectedHbox);

        VBox.setMargin(selectedStackPane, new Insets(0,0, 8,0));
    }

    private void addConceptAndRemoveForm(Entity entity, VBox selectedVBoxContainer, StackPane selectedStackPane, String propertyName) {
        showSelectedAllowableResult(entity, selectedVBoxContainer, selectedStackPane, propertyName);
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

    private void removeAllowableResult(HBox selectedConcept, String propertyName, VBox containerVbox, StackPane containerStackPane, Entity entity) {
        List<EntityFacade> list = resultsViewModel.getObservableList(propertyName);
        list.remove(entity);
        containerVbox.getChildren().remove(selectedConcept);
        HBox.setMargin(containerVbox, new Insets(0));
        VBox.setMargin(containerStackPane, new Insets(0));

        // put the search and drag and drop back when they remove the one selection
        // showQualitativeForm(false);
    }

    private void removeAllowableResultForm() {
        int lastIndex = resultsFormContainer.getChildren().size();
        resultsFormContainer.getChildren().remove((lastIndex-1), lastIndex);
        // we are orphaning an extra, empty VBox on the 2nd+ drag and drop.
        // This cleans it up before the collection grows unbounded.
        if (lastIndex == 4) {
            resultsFormContainer.getChildren().remove(1, 2);
        }
    }

    private UUID getConceptTopic() {
        return resultsViewModel.getPropertyValue(CONCEPT_TOPIC);
    }
    private String getDisplayText(ComponentWithNid conceptEntity) {
        Optional<String> stringOptional = getViewProperties().calculator().getRegularDescriptionText(conceptEntity.nid());
        return stringOptional.orElse("");
    }

    @FXML
    void cancel(ActionEvent event) {
        clearView();
        evtBus.publish(getConceptTopic(), new ShowPanelEvent(event.getSource(), SHOW_ADD_ANALYTE_GROUP));
    }

    @FXML
    void clearView(ActionEvent event) {
        clearView();
    }

    @FXML
    private void createOneResult(ActionEvent event) {
        LOG.info("createOneResult -> Todo publish event containing the result record to be added to analyte group view.");
        // Todo publish event containing the result record to be added to the listener inside the Analyte group view. payload is an object (not a view model).
        // 1. publish
        // 2. reset screen for next entry
        // 3. publish
        resultsViewModel.save();
        // if there are errors.
        if (resultsViewModel.hasErrorMsgs()) {
            resultsViewModel.getValidationMessages().forEach(vMsg -> {
                LOG.error("Error: msg Type: %s errorcode: %s, msg: %s".formatted(vMsg.messageType(), vMsg.errorCode(), vMsg.interpolate(resultsViewModel)) );
            });
            return;
        }

        EntityFacade dataResultType = resultsViewModel.getValue(DATA_RESULTS_TYPE);
        PublicId resultConformanceRecordPublicId = null;
        if (PublicId.equals(dataResultType.publicId(), QUALITATIVE_CONCEPT)) {
            // Create a qualtitative semantic
            resultConformanceRecordPublicId = ViewModelHelper.createQualitativeResultConcept(resultsViewModel, getStampDetail());
        } else if (PublicId.equals(dataResultType.publicId(), QUANTITATIVE_CONCEPT)) {
            // create a quantitative semantic
            resultConformanceRecordPublicId = ViewModelHelper.createQuanitativeResultConcept(resultsViewModel, getStampDetail());
        }

        Optional<Entity> entityOptional = EntityService.get().getEntity(resultConformanceRecordPublicId.asUuidArray());
        Entity entity = entityOptional.get();
        evtBus.publish(getConceptTopic(), new ShowPanelEvent(event.getSource(), SHOW_ADD_ANALYTE_GROUP));
        evtBus.publish(getConceptTopic(), new AddResultEvent(event.getSource(), ADD_RESULT_TO_ANALYTE_GROUP, entity));

        clearView();
    }

    @Override
    public void updateView() {

        //dataResultsTypeComboBox.getItems().addAll(resultsViewModel.findAllDataResultsTypes());
    }

    public void populate(ComboBox comboBox, Collection<ConceptEntity> entities) {
        comboBox.getItems().addAll(entities);
    }

    @Override
    public void clearView() {
        resultName.setText("");
        EntityFacade scaleType = scaleTypeComboBox.getSelectionModel().getSelectedItem();
        if (scaleType == null) {
            scaleType = ORDINAL_CONCEPT;
        }
        scaleTypeComboBox.getSelectionModel().select(scaleType);

        // Clear common related items.
        resultsViewModel.setPropertyValue(ADD_BUTTON_STATE, false)
                .setPropertyValue(RESULTS_NAME, null)
                .setPropertyValue(SCALE_TYPE, scaleType)
                .setPropertyValue(DATA_RESULTS_TYPE, QUALITATIVE_CONCEPT);

        // Clear Qualitative fields
        resultsViewModel.getObservableList(ALLOWABLE_RESULTS).clear();

        // Clear Qualitative fields
        resultsViewModel.setPropertyValue(EXAMPLE_UNITS, null)
                        .setPropertyValue(REFERENCE_RANGES, null);

       // remove all selected results conformences
       clearDragNDropZones(getGeneratedResultsFormContainer(), () ->
                resultsViewModel.getObservableList(ALLOWABLE_RESULTS).clear());

        resultsViewModel.validate();
    }

    @Override
    public void cleanup() {

    }

    @Override
    public FormViewModel getViewModel() {
        return resultsViewModel;
    }

    private void clearDragNDropZones(Pane selectedContainer, Runnable task) {
        // remove all selected items
        if (selectedContainer != null && selectedContainer.getChildren().size() > 0) {
            selectedContainer.getChildren().clear();
            HBox.setMargin(selectedContainer, new Insets(0));
            VBox.setMargin(selectedContainer, new Insets(0));
            task.run();
        }
    }
}
