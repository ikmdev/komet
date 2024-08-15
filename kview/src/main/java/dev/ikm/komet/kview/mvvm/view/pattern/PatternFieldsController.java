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
import static dev.ikm.komet.kview.mvvm.viewmodel.PatternFieldsViewModel.COMMENTS;
import static dev.ikm.komet.kview.mvvm.viewmodel.PatternFieldsViewModel.DATA_TYPE;
import static dev.ikm.komet.kview.mvvm.viewmodel.PatternFieldsViewModel.DISPLAY_NAME;
import static dev.ikm.komet.kview.mvvm.viewmodel.PatternFieldsViewModel.FIELD_ORDER;
import static dev.ikm.komet.kview.mvvm.viewmodel.PatternFieldsViewModel.MEANING_ENTITY;
import static dev.ikm.komet.kview.mvvm.viewmodel.PatternFieldsViewModel.PURPOSE_ENTITY;
import static dev.ikm.komet.kview.mvvm.viewmodel.PatternViewModel.PATTERN_TOPIC;
import dev.ikm.komet.framework.Identicon;
import dev.ikm.komet.framework.events.EvtBus;
import dev.ikm.komet.framework.events.EvtBusFactory;
import dev.ikm.komet.framework.utils.StringUtils;
import dev.ikm.komet.kview.events.pattern.PatternFieldsPanelEvent;
import dev.ikm.komet.kview.events.pattern.PatternPropertyPanelEvent;
import dev.ikm.komet.kview.mvvm.model.PatternField;
import dev.ikm.komet.kview.mvvm.view.common.ConceptDragOverAnimationController;
import dev.ikm.komet.kview.mvvm.view.common.ConceptSearchFormItemController;
import dev.ikm.komet.kview.mvvm.view.common.SelectedConceptController;
import dev.ikm.komet.kview.mvvm.viewmodel.PatternFieldsViewModel;
import dev.ikm.tinkar.common.id.PublicId;
import dev.ikm.tinkar.entity.Entity;
import dev.ikm.tinkar.entity.EntityService;
import javafx.beans.InvalidationListener;
import javafx.collections.ListChangeListener;
import javafx.collections.ObservableList;
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
import org.carlfx.cognitive.loader.Config;
import org.carlfx.cognitive.loader.FXMLMvvmLoader;
import org.carlfx.cognitive.loader.InjectViewModel;
import org.carlfx.cognitive.loader.JFXNode;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.net.URL;
import java.util.Collection;
import java.util.Iterator;
import java.util.List;
import java.util.ListIterator;
import java.util.function.Consumer;

public class PatternFieldsController {

    private static final Logger LOG = LoggerFactory.getLogger(PatternFieldsController.class);

    private static final URL DRAG_OVER_ANIMATION_FXML_URL = ConceptDragOverAnimationController.class.getResource("concept-drag-over-animation.fxml");

    private static final URL CONCEPT_SEARCH_ITEM_FXML_URL = ConceptSearchFormItemController.class.getResource("concept-search-form-item.fxml");

    private static final URL SELECTED_CONCEPT_FXML_URL = SelectedConceptController.class.getResource("selected-concept.fxml");

    @InjectViewModel
    private PatternFieldsViewModel patternFieldsViewModel;

    private EvtBus eventBus;

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

    private ObservableList<Integer> fieldOrderValues;

    @FXML
    private void initialize() {
        // initially set the combo box to 1
        setUpObservableFieldOrder();
        fieldOrderComboBox = new ComboBox<>(fieldOrderValues);

        eventBus = EvtBusFactory.getDefaultEvtBus();

        // load drag over animation for reuse on every drag and drop
        Config animeConfig = new Config(DRAG_OVER_ANIMATION_FXML_URL);
        JFXNode<HBox, ConceptDragOverAnimationController> amineJFXNode = FXMLMvvmLoader.make(animeConfig);
        dragOverAnimation = amineJFXNode.node();

        setupDragNDrop(purposeVBox, purposeStackPane, (publicId) -> {
            // check to see if a pattern > purpose was already dragged into the purpose section before saving
            // to the view model
            if (patternFieldsViewModel.getPropertyValue(PURPOSE_ENTITY) == null) {
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
    }

    private void setUpObservableFieldOrder() {
        fieldOrderValues = new ObservableList<Integer>() {
            @Override
            public void addListener(ListChangeListener<? super Integer> listener) {

            }

            @Override
            public void removeListener(ListChangeListener<? super Integer> listener) {

            }

            @Override
            public boolean addAll(Integer... elements) {
                return false;
            }

            @Override
            public boolean setAll(Integer... elements) {
                return false;
            }

            @Override
            public boolean setAll(Collection<? extends Integer> col) {
                return false;
            }

            @Override
            public boolean removeAll(Integer... elements) {
                return false;
            }

            @Override
            public boolean retainAll(Integer... elements) {
                return false;
            }

            @Override
            public void remove(int from, int to) {

            }

            @Override
            public int size() {
                return 0;
            }

            @Override
            public boolean isEmpty() {
                return false;
            }

            @Override
            public boolean contains(Object o) {
                return false;
            }

            @Override
            public Iterator<Integer> iterator() {
                return null;
            }

            @Override
            public Object[] toArray() {
                return new Object[0];
            }

            @Override
            public <T> T[] toArray(T[] a) {
                return null;
            }

            @Override
            public boolean add(Integer integer) {
                return false;
            }

            @Override
            public boolean remove(Object o) {
                return false;
            }

            @Override
            public boolean containsAll(Collection<?> c) {
                return false;
            }

            @Override
            public boolean addAll(Collection<? extends Integer> c) {
                return false;
            }

            @Override
            public boolean addAll(int index, Collection<? extends Integer> c) {
                return false;
            }

            @Override
            public boolean removeAll(Collection<?> c) {
                return false;
            }

            @Override
            public boolean retainAll(Collection<?> c) {
                return false;
            }

            @Override
            public void clear() {

            }

            @Override
            public Integer get(int index) {
                return null;
            }

            @Override
            public Integer set(int index, Integer element) {
                return null;
            }

            @Override
            public void add(int index, Integer element) {

            }

            @Override
            public Integer remove(int index) {
                return null;
            }

            @Override
            public int indexOf(Object o) {
                return 0;
            }

            @Override
            public int lastIndexOf(Object o) {
                return 0;
            }

            @Override
            public ListIterator<Integer> listIterator() {
                return null;
            }

            @Override
            public ListIterator<Integer> listIterator(int index) {
                return null;
            }

            @Override
            public List<Integer> subList(int fromIndex, int toIndex) {
                return null;
            }

            @Override
            public void addListener(InvalidationListener listener) {

            }

            @Override
            public void removeListener(InvalidationListener listener) {

            }
        };
        fieldOrderValues.add(1);
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
        displayNameTextField.setText(StringUtils.EMPTY);
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

        //publish close env
        eventBus.publish(patternFieldsViewModel.getPropertyValue(PATTERN_TOPIC),
                new PatternPropertyPanelEvent(actionEvent.getSource(), CLOSE_PANEL));


        //publish form submission data
        PatternField patternField = new PatternField(
                patternFieldsViewModel.getPropertyValue(FIELD_ORDER),
                patternFieldsViewModel.getPropertyValue(DISPLAY_NAME),
                patternFieldsViewModel.getPropertyValue(DATA_TYPE),
                patternFieldsViewModel.getPropertyValue(PURPOSE_ENTITY),
                patternFieldsViewModel.getPropertyValue(MEANING_ENTITY),
                patternFieldsViewModel.getPropertyValue(COMMENTS)
        );

        eventBus.publish(patternFieldsViewModel.getPropertyValue(PATTERN_TOPIC),
                new PatternFieldsPanelEvent(actionEvent.getSource(), PATTERN_FIELDS, patternField));
    }
}
