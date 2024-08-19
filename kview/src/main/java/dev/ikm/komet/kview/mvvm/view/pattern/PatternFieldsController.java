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


import static dev.ikm.komet.kview.events.pattern.PatternFieldsPanelEvent.PATTERN_FIELDS;
import static dev.ikm.komet.kview.events.pattern.PatternPropertyPanelEvent.CLOSE_PANEL;
import static dev.ikm.komet.kview.mvvm.viewmodel.DataViewModelHelper.DATA_TYPE_OPTIONS;
import static dev.ikm.komet.kview.mvvm.viewmodel.FormViewModel.VIEW_PROPERTIES;
import static dev.ikm.komet.kview.mvvm.viewmodel.PatternFieldsViewModel.COMMENTS;
import static dev.ikm.komet.kview.mvvm.viewmodel.PatternFieldsViewModel.DATA_TYPE;
import static dev.ikm.komet.kview.mvvm.viewmodel.PatternFieldsViewModel.DISPLAY_NAME;
import static dev.ikm.komet.kview.mvvm.viewmodel.PatternFieldsViewModel.FIELD_ORDER;
import static dev.ikm.komet.kview.mvvm.viewmodel.PatternFieldsViewModel.MEANING_ENTITY;
import static dev.ikm.komet.kview.mvvm.viewmodel.PatternFieldsViewModel.PURPOSE_ENTITY;
import static dev.ikm.komet.kview.mvvm.viewmodel.PatternViewModel.PATTERN_TOPIC;
import dev.ikm.komet.framework.Identicon;
import dev.ikm.komet.framework.events.EvtBusFactory;
import dev.ikm.komet.framework.view.ViewProperties;
import dev.ikm.komet.kview.events.pattern.PatternFieldsPanelEvent;
import dev.ikm.komet.kview.events.pattern.PatternPropertyPanelEvent;
import dev.ikm.komet.kview.mvvm.model.PatternField;
import dev.ikm.komet.kview.mvvm.view.common.ConceptDragOverAnimationController;
import dev.ikm.komet.kview.mvvm.view.common.ConceptSearchFormItemController;
import dev.ikm.komet.kview.mvvm.view.common.SelectedConceptController;
import dev.ikm.komet.kview.mvvm.viewmodel.PatternFieldsViewModel;
import dev.ikm.tinkar.common.id.PublicId;
import dev.ikm.tinkar.coordinate.view.calculator.ViewCalculator;
import dev.ikm.tinkar.entity.ConceptEntity;
import dev.ikm.tinkar.entity.Entity;
import dev.ikm.tinkar.entity.EntityService;
import dev.ikm.tinkar.terms.EntityFacade;
import javafx.beans.property.ObjectProperty;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.Node;
import javafx.scene.control.Button;
import javafx.scene.control.ComboBox;
import javafx.scene.control.TextField;
import javafx.scene.input.Dragboard;
import javafx.scene.input.TransferMode;
import javafx.scene.layout.HBox;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;
import javafx.util.StringConverter;
import org.carlfx.cognitive.loader.Config;
import org.carlfx.cognitive.loader.FXMLMvvmLoader;
import org.carlfx.cognitive.loader.InjectViewModel;
import org.carlfx.cognitive.loader.JFXNode;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.net.URL;
import java.util.function.Consumer;

public class PatternFieldsController {

    private static final Logger LOG = LoggerFactory.getLogger(PatternFieldsController.class);

    private static final URL DRAG_OVER_ANIMATION_FXML_URL = ConceptDragOverAnimationController.class.getResource("concept-drag-over-animation.fxml");

    private static final URL CONCEPT_SEARCH_ITEM_FXML_URL = ConceptSearchFormItemController.class.getResource("concept-search-form-item.fxml");

    private static final URL SELECTED_CONCEPT_FXML_URL = SelectedConceptController.class.getResource("selected-concept.fxml");

    @InjectViewModel
    private PatternFieldsViewModel patternFieldsViewModel;


    @FXML
    private TextField displayNameTextField;

    @FXML
    private Button cancelButton;

    @FXML
    private Button doneButton;

    @FXML
    private VBox purposeVBox;  // container for the purpose search form item

    @FXML
    private StackPane purposeStackPane;  // stack pane for the purpose search form item

    @FXML
    private StackPane selectedPurposeStackPane;  // StackPane to hold the dropped/selected purpose item


    private HBox dragOverAnimation; // reusable animation element temporarily put on a StackPane for a form item

    @FXML
    private VBox meaningVBox;  // container for the meaning search form item

    @FXML
    private StackPane meaningStackPane;  // stack pane for the meaning search form item

    @FXML
    private StackPane selectedMeaningStackPane;  // StackPane to hold the dropped/selected meaning item

    @FXML
    private ComboBox<Integer> fieldOrderComboBox = new ComboBox<>();

    @FXML
    private ComboBox<EntityFacade> dataTypeComboBox = new ComboBox<>();


    @FXML
    private void initialize() {

        // load drag over animation for reuse on every drag and drop
        Config animeConfig = new Config(DRAG_OVER_ANIMATION_FXML_URL);
        JFXNode<HBox, ConceptDragOverAnimationController> amineJFXNode = FXMLMvvmLoader.make(animeConfig);
        dragOverAnimation = amineJFXNode.node();

        setupDragNDrop(purposeVBox, purposeStackPane, (publicId) -> {
            // check to see if a pattern > purpose was already dragged into the purpose section before saving
            // to the view model
            ObjectProperty<ConceptEntity> purposeProp = patternFieldsViewModel.getProperty(PURPOSE_ENTITY);
            if (purposeProp.isNull().get()) {
                // query public Id to get entity.
                Entity entity = EntityService.get().getEntityFast(EntityService.get().nidForPublicId(publicId));
                patternFieldsViewModel.setPropertyValue(PURPOSE_ENTITY, entity);
                addPurposeToForm(entity);
            }
        });

        setupDragNDrop(meaningVBox, meaningStackPane, (publicId) -> {
            // check to see if a pattern > purpose was already dragged into the purpose section before saving
            // to the view model
            if (patternFieldsViewModel.getPropertyValue(MEANING_ENTITY) == null) {
                // query public Id to get entity.
                Entity entity = EntityService.get().getEntityFast(EntityService.get().nidForPublicId(publicId));
                patternFieldsViewModel.setPropertyValue(MEANING_ENTITY, entity);
                addMeaningToForm(entity);
            }
        });

        loadDataTypeComboBox();

    }

    ViewProperties viewProperties;

    public void setViewProperties(ViewProperties viewProperties) {
        this.viewProperties = viewProperties;
    }

    private void loadDataTypeComboBox(){
      /*
          ViewCalculator viewCalculator = viewProperties.calculator();
          IntIdSet dataTypeFields = viewCalculator.descendentsOf(TinkarTerm.DISPLAY_FIELDS);
            Set<ConceptEntity> allDataTypes =
                    dataTypeFields.intStream()
                            .mapToObj(moduleNid -> (ConceptEntity) Entity.getFast(moduleNid))
                            .collect(Collectors.toSet());

            IntIdSet dataTypeDynamic = viewCalculator.descendentsOf(TinkarTerm.DYNAMIC_COLUMN_DATA_TYPES);

            allDataTypes.addAll(dataTypeDynamic.intStream()
                    .mapToObj(moduleNid -> (ConceptEntity) Entity.getFast(moduleNid))
                    .collect(Collectors.toSet()));

            dataTypeComboBox.setConverter((new StringConverter<ConceptEntity>() {
                @Override
                public String toString(ConceptEntity conceptEntity) {
                    Optional<String> stringOptional= viewCalculator.getFullyQualifiedNameText(conceptEntity.nid());
                    return stringOptional.orElse("");
                }

                @Override
                public ConceptEntity fromString(String s) {
                    return null;
                }
            }));
        */
        dataTypeComboBox.setConverter((new StringConverter<EntityFacade>() {
            @Override
            public String toString(EntityFacade conceptEntity) {
                ViewCalculator viewCalculator = viewProperties.calculator();
                return viewCalculator.getRegularDescriptionText(conceptEntity).get();
            }

            @Override
            public EntityFacade fromString(String s) {
                return null;
            }
        }));
        dataTypeComboBox.getItems().addAll(DATA_TYPE_OPTIONS.stream().sorted((entityFacade1, entityFacade2) -> {
            ViewCalculator viewCalculator = getViewProperties().calculator();
            return viewCalculator.getRegularDescriptionText(entityFacade1).get()
                            .compareToIgnoreCase(viewCalculator.getRegularDescriptionText(entityFacade2).get());
        }).toList());
    }

    private ViewProperties getViewProperties() {
        return patternFieldsViewModel.getPropertyValue(VIEW_PROPERTIES);
    }

    private void setupDragNDrop(Node node, StackPane dragOverContainer, Consumer<PublicId> consumer) {

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
            if (dragOverContainer != null && dragOverContainer instanceof StackPane nd && nd.getChildren().size() > 0) {
                int lastIndex = nd.getChildren().size();
                nd.getChildren().add(lastIndex, dragOverAnimation);
            }

            event.consume();
        });

        // restore change
        node.setOnDragExited(event -> {
            if ( dragOverContainer instanceof StackPane nd) {
                /* mouse moved away, remove the graphical cues */
                nd.setOpacity(1);
                if (nd != null) {
                    int lastIndex = nd.getChildren().size();
                    // remove the drag over animation that
                    // off the top of the stack unless the stack is empty
                    // in the case we've dropped the concept onto it
                    if (lastIndex > 2) {
                        nd.getChildren().remove(lastIndex - 1, lastIndex);
                    }
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
     * create a JavaFX node for the purpose concept and add it to
     * above the search form
     *
     * @param entity
     */
    private void addPurposeToForm(Entity entity) {
        // load concept item
        Config config = new Config(SELECTED_CONCEPT_FXML_URL);
        JFXNode<Node, SelectedConceptController> conceptJFXNode = FXMLMvvmLoader.make(config);
        SelectedConceptController controller = conceptJFXNode.controller();

        // set the entity name
        controller.setConceptName(entity.description());

        // attach the behavior to remove the selected concept to the 'X' close button
        controller.setCloseButtonAction(event -> removePurpose());

        // set concept's unique identicon
        controller.setIdenticon(Identicon.generateIdenticonImage(entity.publicId()));

        selectedPurposeStackPane.getChildren().add(conceptJFXNode.node());

        removePurposeForm();
    }


    private void removePurposeForm() {
        purposeStackPane.getChildren().clear();
    }

    private void removePurpose() {
        selectedPurposeStackPane.getChildren().clear();
        patternFieldsViewModel.setPropertyValue(PURPOSE_ENTITY, null);
        generatePurposeForm();
    }

    private void generatePurposeForm() {
        // load concept search form item
        Config config = new Config(CONCEPT_SEARCH_ITEM_FXML_URL);
        JFXNode<Node, ConceptSearchFormItemController> conceptFormJFXNode = FXMLMvvmLoader.make(config);

        purposeVBox.getChildren().remove(purposeStackPane);
        purposeStackPane = (StackPane) conceptFormJFXNode.node();
        purposeStackPane.setId("purposeStackPane");
        purposeVBox.getChildren().add(0, purposeStackPane);

        setupDragNDrop(purposeVBox, purposeStackPane, (publicId) -> {
            // check to see if a pattern > purpose was already dragged into the purpose section before saving
            // to the view model
            if (patternFieldsViewModel.getPropertyValue(PURPOSE_ENTITY) == null) {
                // query public Id to get entity.
                Entity entity = EntityService.get().getEntityFast(EntityService.get().nidForPublicId(publicId));
                patternFieldsViewModel.setPropertyValue(PURPOSE_ENTITY, entity);
                // save calls validate
                patternFieldsViewModel.save();
                addPurposeToForm(entity);
            }
        });
    }

    /**
     * create a JavaFX node for the meaning concept and add it to
     * above the search form
     *
     * @param entity
     */
    private void addMeaningToForm(Entity entity) {
        // load concept item
        Config config = new Config(SELECTED_CONCEPT_FXML_URL);
        JFXNode<Node, SelectedConceptController> conceptJFXNode = FXMLMvvmLoader.make(config);
        SelectedConceptController controller = conceptJFXNode.controller();

        // set the entity name
        controller.setConceptName(entity.description());

        // attach the behavior to remove the selected concept to the 'X' close button
        controller.setCloseButtonAction(event -> removeMeaning());

        // set concept's unique identicon
        controller.setIdenticon(Identicon.generateIdenticonImage(entity.publicId()));

        selectedMeaningStackPane.getChildren().add(conceptJFXNode.node());

        removeMeaningForm();
    }

    private void removeMeaningForm() {
        meaningStackPane.getChildren().clear();
    }

    private void removeMeaning() {
        selectedMeaningStackPane.getChildren().clear();
        patternFieldsViewModel.setPropertyValue(MEANING_ENTITY, null);
        generateMeaningForm();
    }

    private void generateMeaningForm() {
        // load concept search form item
        Config config = new Config(CONCEPT_SEARCH_ITEM_FXML_URL);
        JFXNode<Node, ConceptSearchFormItemController> conceptFormJFXNode = FXMLMvvmLoader.make(config);

        meaningVBox.getChildren().remove(meaningStackPane);
        meaningStackPane = (StackPane) conceptFormJFXNode.node();
        meaningStackPane.setId("meaningStackPane");
        meaningVBox.getChildren().add(0, meaningStackPane);

        setupDragNDrop(meaningVBox, meaningStackPane, (publicId) -> {
            // check to see if a pattern > meaning was already dragged into the meaning section before saving
            // to the view model
            if (patternFieldsViewModel.getPropertyValue(MEANING_ENTITY) == null) {
                // query public Id to get entity.
                Entity entity = EntityService.get().getEntityFast(EntityService.get().nidForPublicId(publicId));
                patternFieldsViewModel.setPropertyValue(MEANING_ENTITY, entity);
                // save calls validate
                patternFieldsViewModel.save();
                addMeaningToForm(entity);
            }
        });
    }


    @FXML
    private void clearView(ActionEvent actionEvent) {
        patternFieldsViewModel.setPropertyValue(DISPLAY_NAME, "");
        patternFieldsViewModel.save(true);
    }

    @FXML
    private void onCancel(ActionEvent actionEvent) {
    }

    private void collectFormData() {
        patternFieldsViewModel.setPropertyValue(DISPLAY_NAME, displayNameTextField.getText());
        //TODO collect all the data
    }

    @FXML
    public void onDone(ActionEvent actionEvent) {
        actionEvent.consume();
        collectFormData();

        // save calls validate
        patternFieldsViewModel.save();
        if (patternFieldsViewModel.hasErrorMsgs()) {
            // when there are validators, we potentially will have errors
            return; // do not proceed.
        }

        //publish close env
        EvtBusFactory.getDefaultEvtBus().publish(patternFieldsViewModel.getPropertyValue(PATTERN_TOPIC),
                new PatternPropertyPanelEvent(actionEvent.getSource(), CLOSE_PANEL));


        //publish form submission data
        PatternField patternField = new PatternField(
                patternFieldsViewModel.getValue(FIELD_ORDER),
                patternFieldsViewModel.getValue(DISPLAY_NAME),
                patternFieldsViewModel.getValue(DATA_TYPE),
                patternFieldsViewModel.getValue(PURPOSE_ENTITY),
                patternFieldsViewModel.getValue(MEANING_ENTITY),
                patternFieldsViewModel.getValue(COMMENTS)
        );

        EvtBusFactory.getDefaultEvtBus().publish(patternFieldsViewModel.getPropertyValue(PATTERN_TOPIC),
                new PatternFieldsPanelEvent(actionEvent.getSource(), PATTERN_FIELDS, patternField));
    }
}
