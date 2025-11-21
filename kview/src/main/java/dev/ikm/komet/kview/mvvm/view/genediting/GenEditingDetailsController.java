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
import dev.ikm.komet.framework.observable.*;
import dev.ikm.komet.framework.view.ViewMenuModel;
import dev.ikm.komet.framework.view.ViewProperties;
import dev.ikm.komet.kview.common.ViewCalculatorUtils;
import dev.ikm.komet.kview.controls.ComponentItem;
import dev.ikm.komet.kview.controls.KLReadOnlyBaseControl;
import dev.ikm.komet.kview.controls.KLReadOnlyBaseSingleValueControl;
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
import dev.ikm.tinkar.entity.*;
import dev.ikm.tinkar.entity.transaction.Transaction;
import dev.ikm.tinkar.events.EvtBusFactory;
import dev.ikm.tinkar.events.EvtType;
import dev.ikm.tinkar.events.Subscriber;
import dev.ikm.tinkar.terms.*;
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
import static dev.ikm.komet.kview.mvvm.viewmodel.stamp.StampFormViewModelBase.Properties.IS_CONFIRMED_OR_SUBMITTED;
import static dev.ikm.komet.kview.mvvm.viewmodel.stamp.StampFormViewModelBase.Properties.MODULE;
import static dev.ikm.komet.kview.mvvm.viewmodel.stamp.StampFormViewModelBase.Properties.PATH;
import static dev.ikm.komet.kview.mvvm.viewmodel.stamp.StampFormViewModelBase.Properties.STATUS;
import static dev.ikm.komet.kview.mvvm.viewmodel.stamp.StampFormViewModelBase.Properties.TIME;
import static dev.ikm.tinkar.common.util.time.DateTimeUtil.format;

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

    // ObservableComposer integration for proper transaction management
    private ObservableComposer composer;
    private ObservableComposer.EntityComposer<ObservableSemanticVersion.Editable, ObservableSemantic> semanticEditor;
    private ObservableSemanticVersion.Editable editableVersion;
    private List<ObservableField.Editable> editableFields = new ArrayList<>();
    private ObservableStamp currentEditStamp;

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
            // generate read-only UI controls in creation mode
            List<KLReadOnlyBaseControl> readOnlyControls = KlFieldHelper.addReadOnlyBlankControlsToContainer(patternVersionRecord, getViewProperties());
            nodes.addAll(readOnlyControls);
            semanticDetailsVBox.getChildren().addAll(readOnlyControls);
        } else {
            genEditingViewModel.setPropertyValue(MODE, EDIT);
            ObservableEntityHandle.get(semantic).ifSemantic(observableSemantic -> {
                this.observableSemantic = observableSemantic;
                observableSemanticSnapshot = this.observableSemantic.getSnapshot(getViewProperties().calculator());
                //retrieve latest committed semanticVersion
                semanticEntityVersionLatest = retrieveCommittedLatestVersion(observableSemanticSnapshot);
                // Populate the Semantic Details
                populateSemanticDetails();
            });
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
                // populate the semantic and its observable fields once saved
                semanticEntityVersionLatest = retrieveCommittedLatestVersion(observableSemantic.getSnapshot(getViewProperties().calculator()));
            }
            // TODO update identicon and identifier fields.
            EntityFacade finalSemantic = semanticProperty.get();
            if (evt.getEventType() == GenEditingEvent.PUBLISH
                    && evt.getNid() == finalSemantic.nid()) {
                if (genEditingViewModel.getPropertyValue(MODE).equals(CREATE)) {
                    // get the latest value for the semantic created.
                    observableSemantic = ObservableEntityHandle.getSemanticOrThrow(finalSemantic);
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

                // Update editable field values using ObservableField.Editables
                for (int i = 0; i < evt.getList().size(); i++) {
                    ObservableField.Editable<?> editableField = editableFields.get(i);
                    Object updatedField = evt.getList().get(i);
                    if (updatedField != null && editableField != null) {
                        // Update via editable field's cached property
                        @SuppressWarnings("unchecked")
                        ObservableField.Editable<Object> uncheckedField = (ObservableField.Editable<Object>) editableField;
                        Runnable setValue = () -> uncheckedField.getObservableFeature().editableValueProperty().setValue(updatedField);
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
        EvtBusFactory.getDefaultEvtBus().subscribe(genEditingViewModel.getPropertyValue(WINDOW_TOPIC),
                GenEditingEvent.class, refreshSubscriber);
    }

    private void updateSemanticForPatternInfo() {
        PatternFacade patternFacade = (PatternFacade) genEditingViewModel.getProperty(PATTERN).getValue();
        LanguageCalculator languageCalculator = getViewProperties().calculator().languageCalculator();
        ObservableEntityHandle.get(patternFacade).ifPattern(observablePattern -> {
            ObservablePatternSnapshot observablePatternSnapshot = observablePattern.getSnapshot(getViewProperties().calculator());
            ObservablePatternVersion observablePatternVersion = observablePatternSnapshot.getLatestVersion().get();
            PatternEntityVersion patternEntityVersion = observablePatternVersion.getVersionRecord();
            String meaning = languageCalculator.getDescriptionText(patternEntityVersion.semanticMeaningNid()).orElse("No description for meaning");
            String purpose = languageCalculator.getDescriptionText(patternEntityVersion.semanticPurposeNid()).orElse("No description for purpose");
            semanticMeaningText.setText(meaning);
            semanticPurposeText.setText(purpose);
            String patternFQN = getViewProperties().calculator().languageCalculator()
                    .getFullyQualifiedDescriptionTextWithFallbackOrNid(patternEntityVersion.nid());
            semanticDescriptionLabel.setText("Semantic for %s".formatted(patternFQN));

            ObjectProperty<EntityFacade> refComponentProp = genEditingViewModel.getProperty(REF_COMPONENT);
            if (refComponentProp != null) {
                EntityFacade refComponent = refComponentProp.get();
                if (refComponent != null) {
                    String refComponentTitle = getViewProperties().calculator().languageCalculator()
                            .getDescriptionText(refComponent.nid())
                            .orElse("No description for referenced component " + refComponent.publicId());
                    //TODO in the future we can internationalize the word "in" (and other labels and text) for the preferred language
                    semanticTitleText.setText(refComponentTitle + " in " + patternFQN);
                }
            }
        });
    }

    /**
     * Initialize ObservableComposer with STAMP coordinates from ViewProperties.
     * Creates composer for managing semantic editing transactions.
     */
    private void initializeComposer() {
        if (composer != null) {
            return; // Already initialized
        }

        ConceptFacade author = getViewProperties().nodeView().editCoordinate().getAuthorForChanges();
        ConceptFacade module = getViewProperties().nodeView().editCoordinate().getDefaultModule();
        ConceptFacade path = getViewProperties().nodeView().editCoordinate().getDefaultPath();

        composer = ObservableComposer.create(
            getViewProperties().calculator(),
            State.ACTIVE,
            author,
            module,
            path,
            "Edit Semantic Details"
        );

        LOG.info("ObservableComposer initialized for semantic editing");
    }

    /**
     * Refactored to use ObservableComposer pattern for proper transaction management.
     * Creates ObservableSemanticEditor and gets ObservableField.Editables for UI binding.
     */
    private void populateSemanticDetails() {
        nodes.clear();
        editableFields.clear();

        if (!semanticEntityVersionLatest.isPresent()) {
            return;
        }

        // Initialize composer if not already done
        initializeComposer();

        // Create semantic editor using composer unified API
        // Get referenced component and pattern from the semantic
        ObservableEntity referencedComponent = ObservableEntityHandle.get(observableSemantic.referencedComponentNid()).expectEntity();
        ObservablePattern pattern = ObservableEntityHandle.get(observableSemantic.patternNid()).expectPattern();
        semanticEditor = composer.composeSemantic(observableSemantic.publicId(), referencedComponent, pattern);

        // Get editable version with cached editing capabilities
        editableVersion = semanticEditor.getEditableVersion();


//        if (genEditingViewModel.getPropertyValue(MODE).equals(CREATE)) {
            // In creation mode, use the composer stamp for display
//            currentEditStamp = editableVersion.getEditStamp();
//        }

        // Get editable fields from the editable version
        editableFields.addAll(editableVersion.getEditableFields());

        // Create edit action for field controls
        BiFunction<KLReadOnlyBaseControl, Integer, Runnable> editAction = (readOnlyBaseControl, fieldIndex) ->
            () -> {
                // Clear edit mode for all other controls
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

        // Generate UI nodes from editable fields
        int index = 0;
        for(ObservableField.Editable<?> editableField : editableFields){
            Field<?> field = editableField.field();

            // Generate node using the underlying ObservableField (read-only view)
            // This was throwing a cast exception, expecting KLReadOnlyBaseControl.
            Node baseControl = KlFieldHelper.createReadOnlyKlField(
                (FieldRecord<?>) field,
                editableField.getObservableFeature(), // Use underlying ObservableField for display
                getViewProperties(),
                currentEditStamp,
                genEditingViewModel.getPropertyValue(CURRENT_JOURNAL_WINDOW_TOPIC)
            );

            nodes.add(baseControl);

            // Eliminated unsafe cast here...
            if (baseControl instanceof KLReadOnlyBaseControl klReadOnlyBaseControl) {
                klReadOnlyBaseControl.setOnEditAction(editAction.apply(klReadOnlyBaseControl, index++));
                semanticDetailsVBox.getChildren().add(klReadOnlyBaseControl);
            }
        }

        LOG.info("Populated semantic details with {} editable fields using ObservableComposer", editableFields.size());
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
            stampViewControl.setLastUpdated(format(time));
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
            Entity<? extends EntityVersion> entity = switch (refComponent) {
                case null -> null;
                case SemanticFacade semanticFacade -> EntityHandle.getSemanticOrThrow(semanticFacade);
                case ConceptFacade conceptFacade -> EntityHandle.getConceptOrThrow(conceptFacade);
                case PatternFacade patternFacade -> EntityHandle.getPatternOrThrow(patternFacade);
                default -> throw new IllegalStateException("Stamps can't be editable referenced components: " + refComponent);
            };

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
                case null -> {
                    refType = "Unknown";
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
                    EntityFacade patternFacade = genEditingViewModel.getPropertyValue(PATTERN);
                    Entity<EntityVersion> patternEntity =  EntityHandle.getPatternOrThrow(patternFacade);
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

    /**
     * Submit semantic with empty fields using ObservableComposer pattern.
     * This is called when a pattern has no field definitions.
     */
    private void submitSemanticWithEmptyFields() {
        try {
            // Save creates uncommitted version
            if (semanticEditor != null) {
                semanticEditor.save();
            }

            // Commit finalizes the transaction
            if (composer != null) {
                composer.commit();
            }

            // Publish success event
            EntityFacade semantic = genEditingViewModel.getPropertyValue(SEMANTIC);
            // Get current field values from editable fields
            List<Object> fieldValues = new ArrayList<>();
            if (editableVersion != null) {
                for (ObservableField.Editable<?> field : editableFields) {
                    fieldValues.add(field.getValue());
                }
            }

            EvtBusFactory.getDefaultEvtBus().publish(
                genEditingViewModel.getPropertyValue(CURRENT_JOURNAL_WINDOW_TOPIC),
                new GenEditingEvent(
                    genEditingViewModel.getPropertyValue(WINDOW_TOPIC),
                    PUBLISH,
                    fieldValues,
                    semantic.nid()
                )
            );

            String submitMessage = "Semantic Details %s Successfully!".formatted(
                genEditingViewModel.getStringProperty(MODE).equals(EDIT) ? "Edited" : "Added"
            );

            Platform.runLater(() -> {
                observableSemantic = ObservableEntityHandle.getSemanticOrThrow(semantic);
                observableSemanticSnapshot = observableSemantic.getSnapshot(getViewProperties().calculator());
                toast()
                    .withUndoAction(undoActionEvent -> LOG.info("undo called"))
                    .show(Toast.Status.SUCCESS, submitMessage);
            });

            LOG.info("Semantic with empty fields committed successfully using ObservableComposer");

        } catch (Exception e) {
            LOG.error("Failed to commit semantic with empty fields", e);
            Alert alert = new Alert(Alert.AlertType.ERROR, "Failed to save: " + e.getMessage(), ButtonType.OK);
            alert.setHeaderText("Unable to Commit");
            alert.showAndWait();
        }
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