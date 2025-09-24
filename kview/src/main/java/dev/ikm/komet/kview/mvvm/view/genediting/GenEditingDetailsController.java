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
package dev.ikm.komet.kview.mvvm.view.genediting;

import dev.ikm.komet.framework.Identicon;
import dev.ikm.komet.framework.controls.TimeUtils;
import dev.ikm.komet.framework.observable.ObservableEntity;
import dev.ikm.komet.framework.observable.ObservableField;
import dev.ikm.komet.framework.observable.ObservablePattern;
import dev.ikm.komet.framework.observable.ObservablePatternSnapshot;
import dev.ikm.komet.framework.observable.ObservablePatternVersion;
import dev.ikm.komet.framework.observable.ObservableSemantic;
import dev.ikm.komet.framework.observable.ObservableSemanticSnapshot;
import dev.ikm.komet.framework.observable.ObservableSemanticVersion;
import dev.ikm.komet.framework.view.ViewMenuModel;
import dev.ikm.komet.framework.view.ViewProperties;
import dev.ikm.komet.kview.common.ViewCalculatorUtils;
import dev.ikm.komet.kview.controls.ComponentItem;
import dev.ikm.komet.kview.controls.KLReadOnlyBaseControl;
import dev.ikm.komet.kview.controls.KLReadOnlyComponentControl;
import dev.ikm.komet.kview.controls.PublicIDListControl;
import dev.ikm.komet.kview.controls.StampViewControl;
import dev.ikm.komet.kview.controls.Toast;
import dev.ikm.komet.kview.events.ClosePropertiesPanelEvent;
import dev.ikm.komet.kview.events.StampEvent;
import dev.ikm.komet.kview.events.genediting.GenEditingEvent;
import dev.ikm.komet.kview.events.genediting.PropertyPanelEvent;
import dev.ikm.komet.kview.fxutils.SlideOutTrayHelper;
import dev.ikm.komet.kview.klfields.KlFieldHelper;
import dev.ikm.komet.kview.mvvm.view.stamp.StampEditController;
import dev.ikm.komet.kview.mvvm.viewmodel.GenEditingViewModel;
import dev.ikm.komet.kview.mvvm.viewmodel.stamp.StampFormViewModelBase;
import dev.ikm.tinkar.coordinate.language.calculator.LanguageCalculator;
import dev.ikm.tinkar.coordinate.stamp.calculator.Latest;
import dev.ikm.tinkar.coordinate.view.calculator.ViewCalculator;
import dev.ikm.tinkar.entity.ConceptEntity;
import dev.ikm.tinkar.entity.Entity;
import dev.ikm.tinkar.entity.EntityVersion;
import dev.ikm.tinkar.entity.FieldRecord;
import dev.ikm.tinkar.entity.PatternEntity;
import dev.ikm.tinkar.entity.PatternEntityVersion;
import dev.ikm.tinkar.entity.PatternVersionRecord;
import dev.ikm.tinkar.entity.SemanticEntity;
import dev.ikm.tinkar.entity.SemanticEntityVersion;
import dev.ikm.tinkar.entity.StampEntityVersion;
import dev.ikm.tinkar.entity.transaction.Transaction;
import dev.ikm.tinkar.events.EvtBusFactory;
import dev.ikm.tinkar.events.EvtType;
import dev.ikm.tinkar.events.Subscriber;
import dev.ikm.tinkar.terms.EntityFacade;
import dev.ikm.tinkar.terms.PatternFacade;
import dev.ikm.tinkar.terms.State;
import javafx.application.Platform;
import javafx.beans.property.ObjectProperty;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.Node;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.ButtonType;
import javafx.scene.control.Label;
import javafx.scene.control.MenuButton;
import javafx.scene.control.TitledPane;
import javafx.scene.control.ToggleButton;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Pane;
import javafx.scene.layout.Region;
import javafx.scene.layout.VBox;
import org.carlfx.cognitive.loader.Config;
import org.carlfx.cognitive.loader.FXMLMvvmLoader;
import org.carlfx.cognitive.loader.InjectViewModel;
import org.carlfx.cognitive.loader.JFXNode;
import org.carlfx.cognitive.loader.NamedVm;
import org.controlsfx.control.PopOver;
import org.eclipse.collections.api.list.ImmutableList;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.net.URL;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.function.BiFunction;
import java.util.function.Consumer;

import static dev.ikm.komet.kview.events.ClosePropertiesPanelEvent.CLOSE_PROPERTIES;
import static dev.ikm.komet.kview.events.genediting.GenEditingEvent.PUBLISH;
import static dev.ikm.komet.kview.events.genediting.PropertyPanelEvent.CLOSE_PANEL;
import static dev.ikm.komet.kview.events.genediting.PropertyPanelEvent.NO_SELECTION_MADE_PANEL;
import static dev.ikm.komet.kview.events.genediting.PropertyPanelEvent.OPEN_PANEL;
import static dev.ikm.komet.kview.events.genediting.PropertyPanelEvent.SHOW_ADD_REFERENCE_SEMANTIC_FIELD;
import static dev.ikm.komet.kview.events.genediting.PropertyPanelEvent.SHOW_EDIT_SEMANTIC_FIELDS;
import static dev.ikm.komet.kview.events.genediting.PropertyPanelEvent.SHOW_EDIT_SINGLE_SEMANTIC_FIELD;
import static dev.ikm.komet.kview.fxutils.SlideOutTrayHelper.isClosed;
import static dev.ikm.komet.kview.fxutils.SlideOutTrayHelper.isOpen;
import static dev.ikm.komet.kview.fxutils.SlideOutTrayHelper.slideIn;
import static dev.ikm.komet.kview.fxutils.SlideOutTrayHelper.slideOut;
import static dev.ikm.komet.kview.fxutils.ViewportHelper.clipChildren;
import static dev.ikm.komet.kview.fxutils.window.DraggableSupport.addDraggableNodes;
import static dev.ikm.komet.kview.fxutils.window.DraggableSupport.removeDraggableNodes;
import static dev.ikm.komet.kview.klfields.KlFieldHelper.retrieveCommittedLatestVersion;
import static dev.ikm.komet.kview.mvvm.view.journal.JournalController.toast;
import static dev.ikm.komet.kview.mvvm.viewmodel.FormViewModel.CREATE;
import static dev.ikm.komet.kview.mvvm.viewmodel.FormViewModel.CURRENT_JOURNAL_WINDOW_TOPIC;
import static dev.ikm.komet.kview.mvvm.viewmodel.FormViewModel.EDIT;
import static dev.ikm.komet.kview.mvvm.viewmodel.FormViewModel.MODE;
import static dev.ikm.komet.kview.mvvm.viewmodel.FormViewModel.VIEW_PROPERTIES;
import static dev.ikm.komet.kview.mvvm.viewmodel.GenEditingViewModel.PATTERN;
import static dev.ikm.komet.kview.mvvm.viewmodel.GenEditingViewModel.REF_COMPONENT;
import static dev.ikm.komet.kview.mvvm.viewmodel.GenEditingViewModel.SEMANTIC;
import static dev.ikm.komet.kview.mvvm.viewmodel.GenEditingViewModel.STAMP_VIEW_MODEL;
import static dev.ikm.komet.kview.mvvm.viewmodel.GenEditingViewModel.WINDOW_TOPIC;
import static dev.ikm.komet.kview.mvvm.viewmodel.stamp.StampFormViewModelBase.Properties.AUTHOR;
import static dev.ikm.komet.kview.mvvm.viewmodel.stamp.StampFormViewModelBase.Properties.CURRENT_STAMP;
import static dev.ikm.komet.kview.mvvm.viewmodel.stamp.StampFormViewModelBase.Properties.IS_CONFIRMED_OR_SUBMITTED;
import static dev.ikm.komet.kview.mvvm.viewmodel.stamp.StampFormViewModelBase.Properties.MODULE;
import static dev.ikm.komet.kview.mvvm.viewmodel.stamp.StampFormViewModelBase.Properties.PATH;
import static dev.ikm.komet.kview.mvvm.viewmodel.stamp.StampFormViewModelBase.Properties.STATUS;
import static dev.ikm.komet.kview.mvvm.viewmodel.stamp.StampFormViewModelBase.Properties.TIME;

public class GenEditingDetailsController {

    private static final Logger LOG = LoggerFactory.getLogger(GenEditingDetailsController.class);

    public static final URL GENEDITING_PROPERTIES_VIEW_FXML_URL = GenEditingDetailsController.class.getResource("genediting-properties.fxml");

    private Consumer<ToggleButton> reasonerResultsControllerConsumer;

    @FXML
    private KLReadOnlyComponentControl referenceComponent;

    @FXML
    private BorderPane detailsOuterBorderPane;

    @FXML
    private ToggleButton propertiesToggleButton;

    @FXML
    private MenuButton coordinatesMenuButton;

    /**
     * model required for the filter coordinates menu, used with coordinatesMenuButton
     */
    private ViewMenuModel viewMenuModel;

    /**
     * Used slide out the properties view
     */
    @FXML
    private Pane propertiesSlideoutTrayPane;

    @FXML
    private Pane timelineSlideoutTrayPane;

    @FXML
    private Label semanticTitleText;

    @FXML
    private Label semanticDescriptionLabel;

    @FXML
    private StampViewControl stampViewControl;

    @FXML
    private TitledPane referenceComponentTitledPane;

    @FXML
    private TitledPane semanticDetailsTitledPane;

    @FXML
    private VBox semanticDetailsVBox;

    private PropertiesController propertiesController;

    private BorderPane propertiesBorderPane;

    @FXML
    private Label semanticMeaningText;

    @FXML
    private Label semanticPurposeText;

    @FXML
    private Button addReferenceButton;

    @FXML
    private Button editFieldsButton;

    @FXML
    private Button saveButton;

    @FXML
    private HBox tabHeader;

    @FXML
    private HBox conceptHeaderControlToolBarHbox;

    @FXML
    private ImageView identiconImageView;

    @FXML
    private PublicIDListControl identifierControl;

    @InjectViewModel
    private GenEditingViewModel genEditingViewModel;

    private List<ObservableField<?>> observableFields = new ArrayList<>();

    private final List<Node> nodes = new ArrayList<>();

    /**
     * Stamp Edit
     */
    private PopOver stampEdit;
    private StampEditController stampEditController;

    private List<Subscriber> subscriberList = new ArrayList<>();

    private Latest<EntityVersion> semanticEntityVersionLatest;

    private ObservableSemantic observableSemantic;

    private ObservableSemanticSnapshot observableSemanticSnapshot;

    private boolean isUpdatingStampSelection = false;

    private Subscriber<ClosePropertiesPanelEvent> closePropertiesPanelEventSubscriber;

    private Subscriber<StampEvent> stampEventSubscriber;

    public GenEditingDetailsController() {
    }

    @FXML
    private void initialize() {
        stampViewControl.selectedProperty().subscribe(this::onStampSelectionChanged);

        ObjectProperty<EntityFacade> refComponent = genEditingViewModel.getObjectProperty(REF_COMPONENT);
        //Enable edit fields button if refComponent is NOT null else disable it.
        editFieldsButton.setDisable(refComponent.isNull().get());
        //Enable reference component edit button if refComponent is NULL else disable it.
        addReferenceButton.setDisable(refComponent.isNotNull().get());

        // clear all semantic details.
        semanticDetailsVBox.getChildren().clear();
        // Setup Properties Bump out view.
        setupProperties();
        //Populate the Title Pattern meaning purpose
        updateSemanticForPatternInfo();
        setupFilterCoordinatesMenu();

        //Populate readonly reference component.
        setupReferenceComponentUI();
        //Populate readonly semantic details
        setupSemanticDetails();

        // Setup window dragging support with explicit draggable nodes
        addDraggableNodes(detailsOuterBorderPane, tabHeader, conceptHeaderControlToolBarHbox);

        // Check if the properties panel is initially open and add draggable nodes if needed
        if (propertiesToggleButton.isSelected() || isOpen(propertiesSlideoutTrayPane)) {
            updateDraggableNodesForPropertiesPanel(true);
        }

        updateIdenticon(refComponent);
        updateDisplayUUID();

        genEditingViewModel.getProperty(STAMP_VIEW_MODEL).bind(propertiesController.stampFormViewModelProperty());

        genEditingViewModel.getProperty(SEMANTIC).subscribe(newSemantic -> {
            propertiesController.updateModel((EntityFacade) newSemantic);
            updateUIStamp(propertiesController.getStampFormViewModel());

            // update the identicon
            updateIdenticon(refComponent);

            // update the display UUID
            updateDisplayUUID();
        });

        propertiesController.getStampFormViewModel().getBooleanProperty(IS_CONFIRMED_OR_SUBMITTED).subscribe(
                isConfirmed -> onConfirmOrSubmitted(isConfirmed));

        // if the user clicks the Close Properties Button from the Edit Descriptions panel
        // in that state, the properties bump out will be slid out, therefore firing will perform a slide in
        closePropertiesPanelEventSubscriber = evt -> propertiesToggleButton.fire();
        EvtBusFactory.getDefaultEvtBus().subscribe(genEditingViewModel.getPropertyValue(WINDOW_TOPIC), ClosePropertiesPanelEvent.class, closePropertiesPanelEventSubscriber);

        // Listening to Stamp events
        // If someone triggers the stamp event from outside we need to update the stamp control and window accordingly
        stampEventSubscriber = evt -> {
            isUpdatingStampSelection = true;
            stampViewControl.setSelected(true);
            isUpdatingStampSelection = false;

            openPropertiesPanel();
        };

        EvtBusFactory.getDefaultEvtBus().subscribe(genEditingViewModel.getPropertyValue(WINDOW_TOPIC), StampEvent.class, stampEventSubscriber);
    }

    private void onConfirmOrSubmitted(Boolean isConfirmedOrSubmitted) {
        updateUIStamp(propertiesController.getStampFormViewModel());

        if(genEditingViewModel.getPropertyValue(MODE).equals(CREATE)) {
            addReferenceButton.setDisable(!isConfirmedOrSubmitted);
            stampViewControl.setDisable(isConfirmedOrSubmitted);
        }
    }

    private void openPropertiesPanel() {
        LOG.info("propBumpOutListener - Opening Properties bumpout toggle = " + propertiesToggleButton.isSelected());

        propertiesToggleButton.setSelected(true);
        if (isClosed(propertiesSlideoutTrayPane)) {
            slideOut(propertiesSlideoutTrayPane, detailsOuterBorderPane);
        }

        updateDraggableNodesForPropertiesPanel(true);
    }

    private void onStampSelectionChanged() {
        if (isUpdatingStampSelection) {
            return;
        }

        if (stampViewControl.isSelected()) {
            if (!propertiesToggleButton.isSelected()) {
                propertiesToggleButton.fire();
            }

            if (CREATE.equals(genEditingViewModel.getPropertyValue(MODE))) {
                EvtBusFactory.getDefaultEvtBus().publish(genEditingViewModel.getPropertyValue(WINDOW_TOPIC), new StampEvent(stampViewControl, StampEvent.CREATE_STAMP));
            } else if (EDIT.equals(genEditingViewModel.getPropertyValue(MODE))) {
                EvtBusFactory.getDefaultEvtBus().publish(genEditingViewModel.getPropertyValue(WINDOW_TOPIC), new StampEvent(stampViewControl, StampEvent.ADD_STAMP));
            }
        } else {
            EvtBusFactory.getDefaultEvtBus().publish(genEditingViewModel.getPropertyValue(WINDOW_TOPIC), new ClosePropertiesPanelEvent(stampViewControl, CLOSE_PROPERTIES));
        }
    }

    private void updateDisplayUUID() {
        EntityFacade semanticComponent = genEditingViewModel.getPropertyValue(SEMANTIC);
        if (semanticComponent != null) {
            identifierControl.updatePublicIdList(genEditingViewModel.getViewProperties().calculator(), semanticComponent);
        }
    }

    private void updateIdenticon(ObjectProperty<EntityFacade> refComponent) {
        if (refComponent.isNotNull().get()) {
            EntityFacade semantic = genEditingViewModel.getPropertyValue(SEMANTIC);

            Image identicon = Identicon.generateIdenticonImage(semantic.publicId());
            identiconImageView.setImage(identicon);
        }
    }

    /**
     * Creates the filter coordinates menu using the ViewMenuModel.
     */
    public void setupFilterCoordinatesMenu() {
        this.viewMenuModel = new ViewMenuModel(genEditingViewModel.getViewProperties(), coordinatesMenuButton, "GenEditingDetailsController");
    }

    private void setupSemanticDetails() {
        EntityFacade semantic = genEditingViewModel.getPropertyValue(SEMANTIC);
        // if the semantic is null, then we generate a default one
        if (semantic == null) {
            // Set the mode to Create
            genEditingViewModel.setPropertyValue(MODE, CREATE);

            // Set empty Semantic Details using pattern fields
            PatternFacade patternFacade = (PatternFacade) genEditingViewModel.getProperty(PATTERN).getValue();
            PatternVersionRecord patternVersionRecord = (PatternVersionRecord) getViewProperties().calculator().latest(patternFacade).get();
            // generate read only UI controls in create mode
            List<KLReadOnlyBaseControl> readOnlyControls = KlFieldHelper.addReadOnlyBlankControlsToContainer(patternVersionRecord, getViewProperties());
            nodes.addAll(readOnlyControls);
            semanticDetailsVBox.getChildren().addAll(readOnlyControls);
        } else {
            genEditingViewModel.setPropertyValue(MODE, EDIT);
            observableSemantic = ObservableEntity.get(semantic.nid());
            observableSemanticSnapshot = observableSemantic.getSnapshot(getViewProperties().calculator());
            //retrieve latest committed semanticVersion
            semanticEntityVersionLatest = retrieveCommittedLatestVersion(observableSemanticSnapshot);
            //Set and Update STAMP values
//            semanticEntityVersionLatest.ifPresent(semanticEntityVersion -> {
//                StampEntity stampEntity = semanticEntityVersion.stamp();
//                stampViewModel.setPropertyValue(STATUS, stampEntity.state())
//                        .setPropertyValue(TIME, stampEntity.time())
//                        .setPropertyValue(AUTHOR, stampEntity.author())
//                        .setPropertyValue(MODULE, stampEntity.module())
//                        .setPropertyValue(PATH, stampEntity.path())
//                ;
//                stampViewModel.save(true);
//            });
            // Populate the Semantic Details
            populateSemanticDetails();
        }

        Subscriber<GenEditingEvent> refreshSubscriber = evt -> {
            //Set up the Listener to refresh the details area (After user hits submit button on the right side)
            ObjectProperty<EntityFacade> semanticProperty = genEditingViewModel.getProperty(SEMANTIC);
            if (semanticProperty.isNull().get()) {
                // If the window is in creation mode ignore the refresh event
                return;
            }
            if (genEditingViewModel.getPropertyValue(MODE).equals(EDIT)) {
                observableSemanticSnapshot = observableSemantic.getSnapshot(getViewProperties().calculator());
            }
            // TODO update identicon and identifier fields.
            EntityFacade finalSemantic = semanticProperty.get();
            if (evt.getEventType() == GenEditingEvent.PUBLISH
                    && evt.getNid() == finalSemantic.nid()) {
                if (genEditingViewModel.getPropertyValue(MODE).equals(CREATE)) {
                    // get the latest value for the semantic created.
                    observableSemantic = ObservableEntity.get(finalSemantic.nid());
                    // populate the semantic and its observable fields once saved
                    semanticEntityVersionLatest = retrieveCommittedLatestVersion(observableSemantic.getSnapshot(getViewProperties().calculator()));
                    // clear out the temporary placeholders
                    semanticDetailsVBox.getChildren().clear();
                    nodes.clear();
                    // set up the real observables now that the semantic has been created
                    populateSemanticDetails();
                    // change the mode from CREATE to EDIT
                    genEditingViewModel.setPropertyValue(MODE, EDIT);
                    // Update STAMP control and STAMP form
                    StampFormViewModelBase stampFormViewModelBase = propertiesController.getStampFormViewModel();
                    stampFormViewModelBase.update(semanticEntityVersionLatest.get().entity(),
                            genEditingViewModel.getPropertyValue(WINDOW_TOPIC), genEditingViewModel.getViewProperties());
                    updateUIStamp(stampFormViewModelBase);
                }

                // Update read-only field values
                for (int i = 0; i < evt.getList().size(); i++) {
                    ObservableField observableField = observableFields.get(i);
                    Object updatedField = evt.getList().get(i);
                    if (updatedField != null && observableField != null) {
                        // readonly integer value 1, editable integer value 1 don't update
                        // readonly integer value 1, editable integer value 5 do update
                        // readonly IntIdSet value [1,2] editable IntIdSet value [1,2] don't update
                        // Should we check if the value is different before updating? (blindly updating now).
                        Runnable setValue = () -> observableField.valueProperty().setValue(updatedField);
                        if (!Platform.isFxApplicationThread()) {
                            Platform.runLater(setValue);
                        } else {
                            setValue.run();
                        }
                    }
                }
            }

            semanticEntityVersionLatest = retrieveCommittedLatestVersion(observableSemanticSnapshot);
            //Set and Update STAMP values
            semanticEntityVersionLatest.ifPresent(semanticEntityVersion -> {
                updateUIStamp(semanticEntityVersion.stamp().lastVersion());
            });
        };
        subscriberList.add(refreshSubscriber);
        EvtBusFactory.getDefaultEvtBus().subscribe(genEditingViewModel.getPropertyValue(CURRENT_JOURNAL_WINDOW_TOPIC),
                GenEditingEvent.class, refreshSubscriber);
    }

    private void updateSemanticForPatternInfo() {
        PatternFacade patternFacade = (PatternFacade) genEditingViewModel.getProperty(PATTERN).getValue();
        LanguageCalculator languageCalculator = getViewProperties().calculator().languageCalculator();
        ObservablePattern observablePattern = ObservableEntity.get(patternFacade.nid());
        ObservablePatternSnapshot observablePatternSnapshot = observablePattern.getSnapshot(getViewProperties().calculator());
        ObservablePatternVersion observablePatternVersion = observablePatternSnapshot.getLatestVersion().get();
        PatternEntityVersion patternEntityVersion = observablePatternVersion.getVersionRecord();
        String meaning = languageCalculator.getDescriptionText(patternEntityVersion.semanticMeaningNid()).orElse("No Description");
        String purpose = languageCalculator.getDescriptionText(patternEntityVersion.semanticPurposeNid()).orElse("No Description");
        semanticMeaningText.setText(meaning);
        semanticPurposeText.setText(purpose);
        String patternFQN = getViewProperties().calculator().languageCalculator()
                .getFullyQualifiedDescriptionTextWithFallbackOrNid(patternEntityVersion.nid());
        semanticDescriptionLabel.setText("Semantic for %s".formatted(patternFQN));

        ObjectProperty<EntityFacade> refComponentProp = genEditingViewModel.getProperty(REF_COMPONENT);
        if(refComponentProp != null){
            EntityFacade refComponent = refComponentProp.get();
            if(refComponent != null) {
                String refComponentTitle = getViewProperties().calculator().languageCalculator().getDescriptionText(refComponent.nid()).get();
                //TODO in the future we can internationalize the word "in" (and other labels and text) for the preferred language
                semanticTitleText.setText(refComponentTitle + " in " + patternFQN);
            }
        }
    }

    //TODO revisit and optimize this method.
    private void populateSemanticDetails() {
        nodes.clear();
        if (semanticEntityVersionLatest.isPresent()) {
            observableSemanticSnapshot = observableSemantic.getSnapshot(getViewProperties().calculator());
            ImmutableList<ObservableSemanticVersion> observableSemanticVersionImmutableList = observableSemanticSnapshot.getHistoricVersions();
            if (observableSemanticVersionImmutableList.isEmpty()) {
                observableFields.addAll((Collection) observableSemanticSnapshot.getLatestFields(false, false).get());
            } else {
                //Cast to mutable list
                List<ObservableSemanticVersion> observableSemanticVersionList = new ArrayList<>(observableSemanticVersionImmutableList.castToList());
                //filter list to have only the latest semantic version passed as argument and remove rest of the entries.
                observableSemanticVersionList.removeIf(p -> !semanticEntityVersionLatest.stampNids().contains(p.stampNid()));
                if (observableSemanticVersionList.isEmpty()) {
                    observableFields.addAll((Collection) observableSemanticSnapshot.getLatestFields(false, false).get());
                } else {
                    ObservableSemanticVersion observableSemanticVersion = observableSemanticVersionList.getFirst();
                    Latest<PatternEntityVersion> latestPatternEntityVersion = getViewProperties().calculator().latestPatternEntityVersion(observableSemanticVersion.patternNid());
                    // Populate the Semantic Details
                    // Displaying editable controls and populating the observable fields array list.
                    observableFields.addAll((Collection) observableSemanticVersion.fields(latestPatternEntityVersion.get()));
                }
            }
            // function to apply for the components' edit action (a.k.a. right click > Edit)
            BiFunction<KLReadOnlyBaseControl, Integer, Runnable> editAction = (readOnlyBaseControl, fieldIndex) ->
                () -> {
                    // Clear edit mode for all other controls (in case any of them was already in edit mode)
                    for (Node node : nodes) {
                        if (node != readOnlyBaseControl) {
                            KLReadOnlyBaseControl klReadOnlyBaseControl = (KLReadOnlyBaseControl) node;
                            klReadOnlyBaseControl.setEditMode(false);
                        }
                    }
                    EvtBusFactory.getDefaultEvtBus().publish(genEditingViewModel.getPropertyValue(WINDOW_TOPIC),
                            new PropertyPanelEvent(readOnlyBaseControl, SHOW_EDIT_SINGLE_SEMANTIC_FIELD, fieldIndex));
                    EvtBusFactory.getDefaultEvtBus().publish(genEditingViewModel.getPropertyValue(WINDOW_TOPIC),
                            new PropertyPanelEvent(readOnlyBaseControl, OPEN_PANEL));
                };
            int index = 0;
            for(ObservableField<?> observableField : observableFields){
                FieldRecord<?> fieldRecord = observableField.field();
                KLReadOnlyBaseControl klReadOnlyBaseControl = (KLReadOnlyBaseControl) KlFieldHelper.generateNode(fieldRecord, observableField, getViewProperties(), false, genEditingViewModel.getPropertyValue(CURRENT_JOURNAL_WINDOW_TOPIC));
                nodes.add(klReadOnlyBaseControl);
                klReadOnlyBaseControl.setOnEditAction(editAction.apply(klReadOnlyBaseControl, index++));
                semanticDetailsVBox.getChildren().add(klReadOnlyBaseControl);
            }
        }
    }

    private void updateUIStamp(StampEntityVersion stampEntityVersion){

        State state = stampEntityVersion.state();
        stampViewControl.setStatus(state == null? "" : getViewProperties().calculator().getDescriptionTextOrNid(state.nid()));

        // -- Time
        updateTimeText(stampEntityVersion.time());

        EntityFacade author = stampEntityVersion.author();
        stampViewControl.setAuthor(author == null? "" : getViewProperties().calculator().getDescriptionTextOrNid(author.nid()));

        EntityFacade module = stampEntityVersion.module();
        stampViewControl.setModule(getViewProperties().calculator().getDescriptionTextOrNid(module.nid()));
        EntityFacade path = stampEntityVersion.path();
        stampViewControl.setPath(getViewProperties().calculator().getDescriptionTextOrNid(path.nid()));

    }

    private void updateUIStamp(StampFormViewModelBase stampFormViewModel) {

        // -- Status
        State status = stampFormViewModel.getValue(STATUS);
        stampViewControl.setStatus(status == null? "" : ViewCalculatorUtils.getDescriptionTextWithFallbackOrNid(status, getViewProperties()));

        // -- Time
        updateTimeText(stampFormViewModel.getValue(TIME));

        // -- Author
        EntityFacade author = stampFormViewModel.getValue(AUTHOR);
        stampViewControl.setAuthor(author == null? "" : ViewCalculatorUtils.getDescriptionTextWithFallbackOrNid(author, getViewProperties()));

        // -- Module
        ConceptEntity moduleEntity = stampFormViewModel.getValue(MODULE);
        if (moduleEntity == null) {
            LOG.warn("Must select a valid module for Stamp.");
            return;
        }
        stampViewControl.setModule(ViewCalculatorUtils.getDescriptionTextWithFallbackOrNid(moduleEntity, getViewProperties()));

        // -- Path
        ConceptEntity pathEntity = stampFormViewModel.getValue(PATH);
        stampViewControl.setPath(pathEntity == null? "" : ViewCalculatorUtils.getDescriptionTextWithFallbackOrNid(pathEntity, getViewProperties()));
    }

    private void updateTimeText(Long time) {
        if (genEditingViewModel.getPropertyValue(MODE) == CREATE) {
            stampViewControl.setLastUpdated("Uncommitted");
        } else {
            stampViewControl.setLastUpdated(TimeUtils.toDateString(time));
        }
    }

    /**
     * Display the Reference Component section underneath Semantic Title.
     *
     */
    private void setupReferenceComponentUI() {
        // check if there is a reference component if not check if there is a semantic entity.
        ObjectProperty<EntityFacade> refComponentProp = genEditingViewModel.getProperty(REF_COMPONENT);
        EntityFacade refComponent = refComponentProp.get();

        //Disable the  edit the Reference Component of an existing semantic once submitted
        Consumer<EntityFacade> updateRefComponentInfo = (refComponent2) -> {
            Entity<? extends EntityVersion> entity = Entity.getFast(refComponent2.nid());
            // update items

            String refType = "Unknown";
            String description = null;
            switch (entity) {
                case SemanticEntity semanticEntity -> {
                    refType = "Semantic";
                    ViewCalculator viewCalculator = getViewProperties().calculator();
                    description = viewCalculator.languageCalculator()
                            .getFullyQualifiedDescriptionTextWithFallbackOrNid(semanticEntity.nid());
                }
                case ConceptEntity ignored -> {
                    refType = "Concept";
                    description = refComponent2.description();
                }
                case PatternEntity ignored -> {
                    refType= "Pattern";
                    description = refComponent2.description();
                }
                default ->  {
                    refType = "Unknown";
                    description = refComponent2.description();
                }
            };

            ComponentItem componentItem = new ComponentItem(description,
                    Identicon.generateIdenticonImage(refComponent2.publicId()), refComponent2.nid());

            referenceComponent.setTitle(refType);
            referenceComponent.setValue(componentItem);
            updateSemanticForPatternInfo();
        };
        if (refComponent != null) {
            updateRefComponentInfo.accept(refComponent);
        }
        Subscriber<GenEditingEvent> refComponentSubscriber = evt -> {
            if (evt.getEventType() == GenEditingEvent.CONFIRM_REFERENCE_COMPONENT) {
                ObjectProperty<EntityFacade> newRefComponentProp = genEditingViewModel.getProperty(REF_COMPONENT);
                updateRefComponentInfo.accept(newRefComponentProp.get());

                // If the Pattern has no field definitions, then commit the Semantic automatically
                if (genEditingViewModel.getObjectProperty(PATTERN).isNotNull().get()) {
                    Entity<EntityVersion> patternEntity = Entity.getFast(genEditingViewModel.getPropertyValue(PATTERN));
                    PatternEntityVersion latestPatternVersion = (PatternEntityVersion) genEditingViewModel.getViewProperties().calculator()
                            .latest(patternEntity)
                            .orElse(patternEntity.versions().getAny());
                    if (latestPatternVersion.fieldDefinitions().isEmpty()) {
                        submitSemanticWithEmptyFields();
                        editFieldsButton.setDisable(true);
                    } else {
                        editFieldsButton.setDisable(newRefComponentProp.isNull().get());
                    }
                    addReferenceButton.setDisable(newRefComponentProp.isNotNull().get());
                }
            }
        };
        subscriberList.add(refComponentSubscriber);
        EvtBusFactory.getDefaultEvtBus().subscribe(genEditingViewModel.getPropertyValue(WINDOW_TOPIC),
                GenEditingEvent.class, refComponentSubscriber);
    }

    private void submitSemanticWithEmptyFields() {
        EntityFacade semantic = genEditingViewModel.getPropertyValue(SEMANTIC);

        Latest<SemanticEntityVersion> semanticEntityVersionLatest = getViewProperties().calculator().stampCalculator().latest(semantic.nid());
        semanticEntityVersionLatest.ifPresent(semanticEntityVersion -> {
            Transaction.forVersion(semanticEntityVersion).ifPresentOrElse(transaction -> {
                transaction.commit();
                // EventBus implementation changes to refresh the details area if commit successful
                EvtBusFactory.getDefaultEvtBus().publish(genEditingViewModel.getPropertyValue(CURRENT_JOURNAL_WINDOW_TOPIC),
                        new GenEditingEvent(genEditingViewModel.getPropertyValue(WINDOW_TOPIC), PUBLISH, semanticEntityVersion.fieldValues().toList(), semantic.nid()));
                String submitMessage = "Semantic Details %s Successfully!".formatted(genEditingViewModel.getStringProperty(MODE).equals(EDIT) ? "Editing" : "Added");
                Platform.runLater(() -> {
                    observableSemantic = ObservableEntity.get(semantic.nid());
                    observableSemanticSnapshot = observableSemantic.getSnapshot(getViewProperties().calculator());
                    toast()
                            .withUndoAction(undoActionEvent ->
                                    LOG.info("undo called")
                            )
                            .show(
                                    Toast.Status.SUCCESS,
                                    submitMessage
                            );
                });
            }, () -> {
                //TODO this is a temp alert / workaround till we figure how to reload transactions across multiple restarts of app.
                LOG.error("Unable to commit: Transaction for the given version does not exist.");
                Alert alert = new Alert(Alert.AlertType.ERROR, "Transaction for current changes does not exist.", ButtonType.OK);
                alert.setHeaderText("Unable to Commit transaction.");
                alert.showAndWait();
            });
        });
    }

    /**
     * Setup the Properties bump out when user clicks on the Properties toggle to slide open the Properties view.
     */
    private void setupProperties() {
        // Setup Property screen bump out
        // Load Concept Properties View Panel (FXML & Controller)
        Config config = new Config(GENEDITING_PROPERTIES_VIEW_FXML_URL)
                .addNamedViewModel(new NamedVm("genEditingViewModel", genEditingViewModel));

        JFXNode<BorderPane, PropertiesController> propsFXMLLoader = FXMLMvvmLoader.make(config);
        this.propertiesBorderPane = propsFXMLLoader.node();
        this.propertiesController = propsFXMLLoader.controller();
        attachPropertiesViewSlideoutTray(this.propertiesBorderPane);

        // open the panel, allow the state machine to determine which panel to show
        // listen for open and close events
        Subscriber<PropertyPanelEvent> propertiesEventSubscriber = (evt) -> {
            if (evt.getEventType() == CLOSE_PANEL) {
                LOG.info("propBumpOutListener - Close Properties bumpout toggle = " + propertiesToggleButton.isSelected());
                propertiesToggleButton.setSelected(false);
                if (isOpen(propertiesSlideoutTrayPane)) {
                    slideIn(propertiesSlideoutTrayPane, detailsOuterBorderPane);
                }

                updateDraggableNodesForPropertiesPanel(false);

                // Turn off edit mode for all read only controls
                for (Node node : nodes) {
                    KLReadOnlyBaseControl klReadOnlyBaseControl = (KLReadOnlyBaseControl) node;
                    klReadOnlyBaseControl.setEditMode(false);
                }
            } else if (evt.getEventType() == OPEN_PANEL || evt.getEventType() == NO_SELECTION_MADE_PANEL) {
                openPropertiesPanel();
            }
        };
        subscriberList.add(propertiesEventSubscriber);
        EvtBusFactory.getDefaultEvtBus().subscribe(genEditingViewModel.getPropertyValue(WINDOW_TOPIC), PropertyPanelEvent.class, propertiesEventSubscriber);
    }

    public ViewProperties getViewProperties() {
        return genEditingViewModel.getPropertyValue(VIEW_PROPERTIES);
    }

    private Consumer<GenEditingDetailsController> onCloseConceptWindow;

    public void setOnCloseConceptWindow(Consumer<GenEditingDetailsController> onClose) {
        this.onCloseConceptWindow = onClose;
    }

    public void onReasonerSlideoutTray(Consumer<ToggleButton> reasonerResultsControllerConsumer) {
        this.reasonerResultsControllerConsumer = reasonerResultsControllerConsumer;
    }

    public void attachPropertiesViewSlideoutTray(Pane propertiesViewBorderPane) {
        addPaneToTray(propertiesViewBorderPane, propertiesSlideoutTrayPane);
    }

    private void addPaneToTray(Pane contentViewPane, Pane slideoutTrayPane) {
        double width = contentViewPane.getWidth();
        contentViewPane.setLayoutX(width);
        contentViewPane.getStyleClass().add("slideout-tray-pane");

        slideoutTrayPane.getChildren().add(contentViewPane);
        clipChildren(slideoutTrayPane, 0);
        contentViewPane.setLayoutX(-width);
        slideoutTrayPane.setMaxWidth(0);

        Region contentRegion = contentViewPane;
        // binding the child's height to the preferred height of hte parent
        // so that when we resize the window the content in the slide out pane
        // aligns with the details view
        contentRegion.prefHeightProperty().bind(slideoutTrayPane.heightProperty());
    }

    /**
     * Workaround to place disclosure arrow button to the right of the accordion.
     */
    public void putTitlePanesArrowOnRight() {
        //TODO Fix the right arrow - commenting these lines since it is throwing null pointer.
//        putArrowOnRight(this.referenceComponentTitledPane);
//        putArrowOnRight(this.semanticDetailsTitledPane);
    }

    @FXML
    void closeConceptWindow(ActionEvent event) {
        // Clean up the draggable nodes
        removeDraggableNodes(detailsOuterBorderPane,
                tabHeader,
                conceptHeaderControlToolBarHbox,
                propertiesController != null ? propertiesController.getPropertiesTabsPane() : null);

        if (this.onCloseConceptWindow != null) {
            onCloseConceptWindow.accept(this);
        }
        // TODO Create an event to notify children panes to clean up their subscribers.
        subscriberList.forEach(subscriber -> EvtBusFactory.getDefaultEvtBus().unsubscribe(subscriber));
    }

    @FXML
    private void showAddRefComponentPanel(ActionEvent actionEvent) {
        EntityFacade refComponent = genEditingViewModel.getPropertyValue(REF_COMPONENT);

        // notify bump out to display edit fields in bump out area.
        EvtBusFactory.getDefaultEvtBus()
                .publish(genEditingViewModel.getPropertyValue(WINDOW_TOPIC),
                        new PropertyPanelEvent(actionEvent.getSource(),
                                SHOW_ADD_REFERENCE_SEMANTIC_FIELD, refComponent));
        // open properties bump out.
        EvtBusFactory.getDefaultEvtBus().publish(genEditingViewModel.getPropertyValue(WINDOW_TOPIC), new PropertyPanelEvent(actionEvent.getSource(), OPEN_PANEL));
    }

    @FXML
    private void showAndEditSemanticFieldsPanel(ActionEvent actionEvent) {
        EntityFacade semantic = genEditingViewModel.getPropertyValue(SEMANTIC);

        // notify bump out to display edit fields in bump out area.
        EvtBusFactory.getDefaultEvtBus()
                .publish(genEditingViewModel.getPropertyValue(WINDOW_TOPIC),
                        new PropertyPanelEvent(actionEvent.getSource(),
                                SHOW_EDIT_SEMANTIC_FIELDS, semantic));
        // open properties bump out.
        EvtBusFactory.getDefaultEvtBus().publish(genEditingViewModel.getPropertyValue(WINDOW_TOPIC), new PropertyPanelEvent(actionEvent.getSource(), OPEN_PANEL));

        // Set all controls to edit mode
        for (Node node : nodes) {
            KLReadOnlyBaseControl klReadOnlyBaseControl = (KLReadOnlyBaseControl) node;
            klReadOnlyBaseControl.setEditMode(true);
        }
    }

    @FXML
    private void openReasonerSlideout(ActionEvent actionEvent) {
        // TODO: perform reasoner
    }

    @FXML
    private void openTimelinePanel(ActionEvent actionEvent) {
        // TODO: perform reasoner
    }

    /**
     * When user clicks on the pencil icon to reveal the dynamic edit (KlFields) fields.
     *
     * @param actionEvent Button click action
     */
    @FXML
    private void showSemanticEditFieldsPanel(ActionEvent actionEvent) {
        LOG.info("Todo show bump out and display Edit Fields panel \n" + actionEvent);
        actionEvent.consume();
        EvtBusFactory.getDefaultEvtBus().publish(genEditingViewModel.getPropertyValue(WINDOW_TOPIC), new PropertyPanelEvent(actionEvent.getSource(), SHOW_EDIT_SEMANTIC_FIELDS));
        EvtBusFactory.getDefaultEvtBus().publish(genEditingViewModel.getPropertyValue(WINDOW_TOPIC), new PropertyPanelEvent(actionEvent.getSource(), SHOW_ADD_REFERENCE_SEMANTIC_FIELD));
        EvtBusFactory.getDefaultEvtBus().publish(genEditingViewModel.getPropertyValue(WINDOW_TOPIC), new PropertyPanelEvent(actionEvent.getSource(), OPEN_PANEL));
    }

    /**
     * User is clicking on the Toggle switch to open or close Properties bump out.
     *
     * @param event Button click event.
     */
    @FXML
    private void openPropertiesPanel(ActionEvent event) {
        ToggleButton propertyToggle = (ToggleButton) event.getSource();
        EvtType<PropertyPanelEvent> eventEvtType = propertyToggle.isSelected() ? OPEN_PANEL : CLOSE_PANEL;

        updateDraggableNodesForPropertiesPanel(propertyToggle.isSelected());

        isUpdatingStampSelection = true;
        stampViewControl.setSelected(propertyToggle.isSelected());
        isUpdatingStampSelection = false;

        EvtBusFactory.getDefaultEvtBus().publish(genEditingViewModel.getPropertyValue(WINDOW_TOPIC), new PropertyPanelEvent(propertyToggle, eventEvtType));
    }

    /**
     * Updates draggable behavior for the properties panel based on its open/closed state.
     * <p>
     * When opened, adds the properties tabs pane as a draggable node. When closed,
     * safely removes the draggable behavior to prevent memory leaks.
     *
     * @param isOpen {@code true} to add draggable nodes, {@code false} to remove them
     */
    private void updateDraggableNodesForPropertiesPanel(boolean isOpen) {
        if (propertiesController != null && propertiesController.getPropertiesTabsPane() != null) {
            if (isOpen) {
                addDraggableNodes(detailsOuterBorderPane, propertiesController.getPropertiesTabsPane());
                LOG.debug("Added properties nodes as draggable");
            } else {
                removeDraggableNodes(detailsOuterBorderPane, propertiesController.getPropertiesTabsPane());
                LOG.debug("Removed properties nodes from draggable");
            }
        }
    }

    @FXML
    private void save(ActionEvent actionEvent) {
        // TODO create a commit transaction of current Semantic (Add or edit will add a new Semantic Version)
    }

    /**
     * Checks whether the properties panel is currently open.
     * <p>
     * This method determines the open state by checking if the properties
     * slideout tray pane is visible and expanded.
     *
     * @return {@code true} if the properties panel is open and visible,
     *         {@code false} if it is closed or hidden
     */
    public boolean isPropertiesPanelOpen() {
        return SlideOutTrayHelper.isOpen(propertiesSlideoutTrayPane);
    }

    /**
     * Sets the open/closed state of the properties panel programmatically.
     * <p>
     * The animation is performed without transitions when called programmatically
     * to ensure immediate state changes.
     *
     * @param isOpen {@code true} to open (slide out) the properties panel,
     *               {@code false} to close (slide in) the panel
     */
    public void setPropertiesPanelOpen(boolean isOpen) {
        propertiesToggleButton.setSelected(isOpen);

        if (isOpen) {
            SlideOutTrayHelper.slideOut(propertiesSlideoutTrayPane, detailsOuterBorderPane, false);
        } else {
            SlideOutTrayHelper.slideIn(propertiesSlideoutTrayPane, detailsOuterBorderPane, false);
            nodes.stream().filter(node -> node instanceof KLReadOnlyBaseControl)
                    .map(node -> (KLReadOnlyBaseControl) node)
                    .forEach(readOnlyControl -> readOnlyControl.setEditMode(false));
        }

        updateDraggableNodesForPropertiesPanel(isOpen);
    }

    public PropertiesController getPropertiesController() {
        return propertiesController;
    }
}