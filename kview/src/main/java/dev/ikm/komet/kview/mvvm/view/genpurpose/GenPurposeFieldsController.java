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
package dev.ikm.komet.kview.mvvm.view.genpurpose;


import dev.ikm.komet.framework.observable.ObservableComposer;
import dev.ikm.komet.framework.observable.ObservableConcept;
import dev.ikm.komet.framework.observable.ObservableEntity;
import dev.ikm.komet.framework.observable.ObservableEntityHandle;
import dev.ikm.komet.framework.observable.ObservableEntitySnapshot;
import dev.ikm.komet.framework.observable.ObservableEntityVersion;
import dev.ikm.komet.framework.observable.ObservableField;
import dev.ikm.komet.framework.observable.ObservablePattern;
import dev.ikm.komet.framework.observable.ObservableSemantic;
import dev.ikm.komet.framework.observable.ObservableSemanticSnapshot;
import dev.ikm.komet.framework.observable.ObservableSemanticVersion;
import dev.ikm.komet.framework.observable.ObservableStamp;
import dev.ikm.komet.framework.observable.ObservableVersion;
import dev.ikm.komet.framework.view.ObservableViewWithOverride;
import dev.ikm.komet.framework.view.ViewProperties;
import dev.ikm.komet.kview.controls.Toast;
import dev.ikm.komet.kview.events.genediting.GenEditingEvent;
import dev.ikm.komet.kview.events.genediting.PropertyPanelEvent;
import dev.ikm.komet.kview.events.pattern.PatternSavedEvent;
import dev.ikm.komet.kview.mvvm.view.genediting.ConfirmationDialogController;
import dev.ikm.komet.kview.mvvm.viewmodel.GenPurposeViewModel;
import dev.ikm.komet.kview.mvvm.viewmodel.stamp.StampFormViewModelBase;
import dev.ikm.komet.layout.version.field.KlField;
import dev.ikm.tinkar.common.id.PublicIds;
import dev.ikm.tinkar.component.FeatureDefinition;
import dev.ikm.tinkar.coordinate.stamp.calculator.Latest;
import dev.ikm.tinkar.coordinate.stamp.calculator.StampCalculator;
import dev.ikm.tinkar.entity.EntityHandle;
import dev.ikm.tinkar.entity.EntityVersion;
import dev.ikm.tinkar.entity.Field;
import dev.ikm.tinkar.entity.FieldRecord;
import dev.ikm.tinkar.entity.PatternEntityVersion;
import dev.ikm.tinkar.entity.SemanticEntityVersion;
import dev.ikm.tinkar.entity.SemanticVersionRecord;
import dev.ikm.tinkar.entity.VersionData;
import dev.ikm.tinkar.events.EntityVersionChangeEvent;
import dev.ikm.tinkar.events.EvtBusFactory;
import dev.ikm.tinkar.events.Subscriber;
import dev.ikm.tinkar.terms.ConceptFacade;
import dev.ikm.tinkar.terms.EntityBinding;
import dev.ikm.tinkar.terms.EntityFacade;
import dev.ikm.tinkar.terms.EntityProxy;
import dev.ikm.tinkar.terms.State;
import javafx.application.Platform;
import javafx.beans.property.BooleanProperty;
import javafx.beans.property.ObjectProperty;
import javafx.beans.property.SimpleBooleanProperty;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.Node;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.ButtonType;
import javafx.scene.control.Separator;
import javafx.scene.layout.VBox;
import org.carlfx.cognitive.loader.InjectViewModel;
import org.eclipse.collections.api.list.ImmutableList;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicReference;

import static dev.ikm.komet.kview.events.EventTopics.SAVE_PATTERN_TOPIC;
import static dev.ikm.komet.kview.events.genediting.GenEditingEvent.CONFIRM_REFERENCE_COMPONENT;
import static dev.ikm.komet.kview.events.genediting.GenEditingEvent.PUBLISH;
import static dev.ikm.komet.kview.events.genediting.PropertyPanelEvent.CLOSE_PANEL;
import static dev.ikm.komet.kview.klfields.KlFieldHelper.calculateHashValue;
import static dev.ikm.komet.kview.klfields.KlFieldHelper.createDefaultFieldValues;
import static dev.ikm.komet.kview.klfields.KlFieldHelper.createEditableKlField;
import static dev.ikm.komet.kview.klfields.KlFieldHelper.retrieveCommittedLatestVersion;
import static dev.ikm.komet.kview.mvvm.view.journal.JournalController.toast;
import static dev.ikm.komet.kview.mvvm.viewmodel.FormViewModel.CREATE;
import static dev.ikm.komet.kview.mvvm.viewmodel.FormViewModel.EDIT;
import static dev.ikm.komet.kview.mvvm.viewmodel.FormViewModel.MODE;
import static dev.ikm.komet.kview.mvvm.viewmodel.FormViewModel.VIEW_PROPERTIES;
import static dev.ikm.komet.kview.mvvm.viewmodel.GenEditingViewModel.PATTERN;
import static dev.ikm.komet.kview.mvvm.viewmodel.GenEditingViewModel.REF_COMPONENT;
import static dev.ikm.komet.kview.mvvm.viewmodel.GenEditingViewModel.SEMANTIC;
import static dev.ikm.komet.kview.mvvm.viewmodel.GenEditingViewModel.STAMP_VIEW_MODEL;
import static dev.ikm.komet.kview.mvvm.viewmodel.GenEditingViewModel.WINDOW_TOPIC;
import static dev.ikm.komet.kview.mvvm.viewmodel.stamp.StampFormViewModelBase.Properties.MODULE;
import static dev.ikm.komet.kview.mvvm.viewmodel.stamp.StampFormViewModelBase.Properties.PATH;
import static dev.ikm.komet.terms.KometTerm.BLANK_CONCEPT;
import static dev.ikm.tinkar.events.FrameworkTopics.VERSION_CHANGED_TOPIC;
import static dev.ikm.tinkar.provider.search.Indexer.FIELD_INDEX;
import static dev.ikm.tinkar.terms.TinkarTerm.COMPONENT_FIELD;
import static dev.ikm.tinkar.terms.TinkarTerm.IMAGE_FIELD;

public class GenPurposeFieldsController {

    private static final Logger LOG = LoggerFactory.getLogger(GenPurposeFieldsController.class);

    /**
     * Provide the standard Confirm Clear dialog title for use in other classes
     */
    public static final String CONFIRM_CLEAR_TITLE = "Confirm Clear Form";
    /**
     * Provide the standard Confirm Clear dialog message for use in other classes
     */
    public static final String CONFIRM_CLEAR_MESSAGE =  "Are you sure you want to clear the form? All entered data will be lost.";

    @FXML
    private VBox editFieldsVBox;
    @FXML
    private Button cancelButton;

    @FXML
    private Button clearOrResetFormButton;

    @FXML
    private Button submitButton;

    @InjectViewModel
    private GenPurposeViewModel genPurposeViewModel;

    // ObservableComposer integration for proper transaction management
    private ObservableComposer composer;
    private ObservableComposer.EntityComposer<ObservableSemanticVersion.Editable, ObservableSemantic> semanticEditor;
    private ObservableSemanticVersion.Editable editableVersion;
    private ObservableStamp currentEditStamp;

    private final List<Node> nodes = new ArrayList<>();
    private final List<KlField> klFields = new ArrayList<>();

    private int committedHash;
    /**
     * Flag to indicate if any field has changed. Reset upon submit.
     */
    private BooleanProperty readyToEditVersion = new SimpleBooleanProperty(false);

    ObservableEntityHandle observableEntityHandle;

    ObservableEntitySnapshot observableEntitySnapshot;

    Subscriber<EntityVersionChangeEvent> entityVersionChangeEventSubscriber;

    private boolean reloadPatternNavigator;

    private void enableDisableButtons() {
        boolean emptyFields = checkForEmptyFields();
        int uncommittedHash = calculateHashValue(getObservableEditables(), getStampCalculator());
        boolean fieldsHaveNotChanged = committedHash == uncommittedHash;

        submitButton.setDisable(emptyFields || fieldsHaveNotChanged);
        clearOrResetFormButton.setDisable(fieldsHaveNotChanged);
    }

    private StampCalculator getStampCalculator() {
        ObservableViewWithOverride view = genPurposeViewModel.getViewProperties().nodeView();
        StampCalculator stampCalculator = view.calculator();
        return stampCalculator;
    }

    /**
     * Initialize ObservableComposer with STAMP coordinates from ViewProperties.
     */
    private void initializeComposer() {
        if (composer != null) {
            return; // Already initialized
        }
        ConceptFacade author = getViewProperties().parentView().editCoordinate().getAuthorForChanges();
        ConceptFacade module = getViewProperties().nodeView().editCoordinate().getDefaultModule();
        ConceptFacade path = getViewProperties().nodeView().editCoordinate().getDefaultPath();
        if (genPurposeViewModel.getPropertyValue(MODE).equals(CREATE)) {
            StampFormViewModelBase stampFormViewModel = genPurposeViewModel.getPropertyValue(STAMP_VIEW_MODEL);
            module = stampFormViewModel.getPropertyValue(MODULE);
            path = stampFormViewModel.getPropertyValue(PATH);
        } else {
            // Edit MODE
            // get latest module and path from edit coordinate
            if (observableEntityHandle.isSemantic()) {
                Optional<ObservableSemantic> s = observableEntityHandle.asSemantic();
                if (s.isPresent()) {
                    Latest<EntityVersion> semanticVersion = getViewProperties().calculator().latest(s.get().nid());
                    if (semanticVersion.isPresent()) {
                        module = semanticVersion.get().module();
                        path = semanticVersion.get().path();
                    }
                }
            }
        }

        composer = ObservableComposer.create(getViewProperties().calculator(),
            State.ACTIVE,
            author,
            module,
            path,
            "Edit Semantic Fields"
        );
//        // EDIT MODE: Start a transaction for editing (Adding new semantic version).
//        if (genEditingViewModel.getPropertyValue(MODE).equals(EDIT)) {
//            composer.getOrCreateTransaction();
//        }
        LOG.info("ObservableComposer initialized for semantic fields editing");
    }

    /**
     * Getter for ObservableComposer. Lambdas used to have final access to the variable composer.
     */
    private ObservableComposer getObservableComposer() {
        return composer;
    }
    /**
     * This method checks for empty/blank/null fields
     * @return invalid
     */
    private boolean checkForEmptyFields() {
        AtomicBoolean invalid = new AtomicBoolean(false);

        for (ObservableField.Editable<?> editableField : getObservableEditables()) {
            ObservableField<?> observableField = editableField.getObservableFeature();
            FeatureDefinition fieldDefinition = observableField.definition(getStampCalculator());
            if (fieldDefinition.dataTypeNid() == IMAGE_FIELD.nid()) {
                invalid.set(observableField.valueProperty().get() == null || (((byte[]) observableField.valueProperty().get()).length == 0));
            } else if (fieldDefinition.dataTypeNid() == COMPONENT_FIELD.nid()) {
                invalid.set(observableField.valueProperty().get() == null || observableField.valueProperty().get() == BLANK_CONCEPT);
            }
            if (!invalid.get()) {
                invalid.set((observableField.value() == null || observableField.value().toString().isEmpty()));
            }
            if (invalid.get()) {
                return true;
            }
        }

        return false;
    }

    private void processCommittedValues() {
        AtomicReference<ImmutableList<ObservableField>> immutableList = new AtomicReference<>();
        //Get the latest version
        Latest<ObservableVersion> latestObservableVersion = observableEntitySnapshot.getLatestVersion();
        latestObservableVersion.ifPresent(observableVersion -> { // if latest version present
            if (observableVersion instanceof ObservableEntityVersion<?,?> oev && oev.committed()) {
                if (observableEntitySnapshot instanceof ObservableSemanticSnapshot observableSemanticSnapshot) {
                    immutableList.set(observableSemanticSnapshot.getLatestFields().get());
                }
                // and if the latest version is committed, then get the latest fields
           } else { //if The latest version is Uncommitted, then retrieve the committed version from historic versions list.
               ImmutableList<ObservableSemanticVersion> observableSemanticVersionImmutableList = observableEntitySnapshot.getHistoricVersions();
               // replace any versions with uncommited stamp
               Optional<ObservableSemanticVersion> observableSemanticVersionOptional = observableSemanticVersionImmutableList.stream().filter(VersionData::committed).findFirst();
               observableSemanticVersionOptional.ifPresent(committedObservableSemanticVersion -> {
                   EntityFacade pattern = EntityFacade.make(committedObservableSemanticVersion.patternNid());
                   Latest<PatternEntityVersion> patternEntityVersionLatest = getViewProperties().calculator().latest(pattern.nid());
                   if (patternEntityVersionLatest.isPresent()) {
                       immutableList.set(committedObservableSemanticVersion.fields());
                   }
               });
           }
        });
        if (immutableList.get() != null) {
            List<ObservableField<?>> observableFieldsList = new ArrayList<>((Collection) immutableList.get());
            committedHash = calculateHashValue(getObservableEditables(), getStampCalculator());  // and calculate the hashValue for commited data.
        }

    }

    @FXML
    private void initialize() {
        // clear all semantic details.
        editFieldsVBox.setSpacing(8.0);
        editFieldsVBox.getChildren().clear();
        submitButton.setDisable(true); // disable submit until fields changed.
        genPurposeViewModel.save();

//        EntityFacade semantic = genPurposeViewModel.getPropertyValue(SEMANTIC);
        reloadPatternNavigator = true;
//        ObjectProperty<EntityFacade> semanticProperty = genPurposeViewModel.getProperty(SEMANTIC);
        // listen if the semantic property is updated during Create mode.
//        semanticProperty.addListener( _ -> setupEditSemanticDetails());

        // Create a transaction and uncommitted semantic when reference component is confirmed.
        Subscriber<GenEditingEvent> createUncommittedSemanticSubscriber = evt -> {
            // After confirming stamp and reference component create
            if (evt.getEventType() == CONFIRM_REFERENCE_COMPONENT) {
                EntityFacade referencedComponentFacade = genPurposeViewModel.getPropertyValue(REF_COMPONENT);
                EntityFacade patternFacade = genPurposeViewModel.getPropertyValue(PATTERN);
                ObservableEntity observableReferenceComponent = ObservableEntityHandle.get(referencedComponentFacade.nid()).expectEntity();
                ObservablePattern observablePattern = ObservableEntityHandle.get(patternFacade.nid()).expectPattern();

                initializeComposer();
                semanticEditor = getObservableComposer().composeSemantic(PublicIds.newRandom(), observableReferenceComponent, observablePattern);

                editableVersion = semanticEditor.getEditableVersion();

                semanticEditor.save(); // Save to create an uncommitted version
                EntityHandle.get(semanticEditor.getEntity().nid()).asSemantic().ifPresent(semanticEntity ->
                        genPurposeViewModel.setPropertyValue(SEMANTIC, semanticEntity));
            }
        };
        EvtBusFactory.getDefaultEvtBus().subscribe(genPurposeViewModel.getPropertyValue(WINDOW_TOPIC),
                GenEditingEvent.class, createUncommittedSemanticSubscriber);

//        if (semantic != null && genPurposeViewModel.getPropertyValue(MODE) == EDIT) {
//            //Change the button name to RESET FORM in EDIT MODE
//            clearOrResetFormButton.setText("RESET FORM");
//            setupEditSemanticDetails();
//        }
        genPurposeViewModel.getProperty(MODE).subscribe((mode) -> {
            if(mode == EDIT){
                clearOrResetFormButton.setText("RESET FORM");
            }else {
                clearOrResetFormButton.setText("CLEAR FORM");
            }
        });

        readyToEditVersion.set(true); // initial load of fields.
        // This will reconstitute the editable fields when any field changes.
        readyToEditVersion.subscribe( (oldVal, changed) -> {
            if (changed && !oldVal.equals(changed)) {
                rebindNewEditableVersion();
            }
        });
    }

    private void setupEditSemanticDetails() {
        EntityFacade semantic = genPurposeViewModel.getPropertyValue(SEMANTIC);
        this.observableEntityHandle = ObservableEntityHandle.get(semantic.nid());
        this.observableEntityHandle.ifSemantic(observableSemantic -> {
            observableEntitySnapshot = observableSemantic.getSnapshot(getViewProperties().calculator());

            //Setting author to author for change. This value will be used during auto-save
            ConceptFacade authorForChanges = getViewProperties().nodeView().editCoordinate().getAuthorForChanges();
            //TODO: setAuthorForChanges seems to be an API decision to revisit.
            if (observableEntitySnapshot instanceof ObservableSemanticSnapshot observableSemanticSnapshot) {
                observableSemanticSnapshot.getLatestVersion().get().setAuthorForChanges(authorForChanges);
            }
              // Not sure this is needed and needs to be revisted
//            processCommittedValues();
            loadUIData(); // And populates Nodes and Observable fields.
            entityVersionChangeEventSubscriber = evt -> {
                LOG.info("Version has been updated: " + evt.getEventType());
                // get payload
                if (evt.getEntityVersion().nid() == observableSemantic.nid()
                        && evt.getEntityVersion() instanceof SemanticVersionRecord semanticVersionRecord) {
                    ImmutableList<Object> values = semanticVersionRecord.fieldValues();
                    for (int i = 0; i< values.size(); i++) {
                        ObservableField.Editable<?> editableField = getKlFields().get(i).fieldEditable();
                        // Update via editable field's cached property
                        @SuppressWarnings("unchecked")
                        ObservableField.Editable<Object> uncheckedField = (ObservableField.Editable<Object>) editableField;
                        uncheckedField.setValue(values.get(i));
                    }
                }
                if(reloadPatternNavigator && genPurposeViewModel.getPropertyValue(MODE) == CREATE) {
                    // refresh the pattern navigation
                    EvtBusFactory.getDefaultEvtBus().publish(SAVE_PATTERN_TOPIC,
                            new PatternSavedEvent(this, PatternSavedEvent.PATTERN_CREATION_EVENT));
                    reloadPatternNavigator = false;
                }
//                enableDisableButtons();
            };

            EvtBusFactory.getDefaultEvtBus().subscribe(VERSION_CHANGED_TOPIC,
                    EntityVersionChangeEvent.class, entityVersionChangeEventSubscriber);
        });
    }

    private void loadVBox() {
        // subscribe to changes... if the FIELD_INDEX is -1 or unset, then the user clicked the
        //  pencil icon and wants to edit all the fields
        // if the FIELD_INDEX is >= 0 then the user chose the context menu of a single field
        //  to edit that field
        genPurposeViewModel.getObjectProperty(FIELD_INDEX).subscribe(fieldIndex -> {
            int fieldIdx = (int)fieldIndex;
            editFieldsVBox.getChildren().clear();
            // single field to edit
            if (fieldIdx >= 0 && nodes.size() > 0) {
                editFieldsVBox.getChildren().add(nodes.get(fieldIdx));
            } else {
                // all fields to edit
                for (int i = 0; i < nodes.size(); i++) {
                    editFieldsVBox.getChildren().add(nodes.get(i));
                    if (i < nodes.size() - 1) {
                        editFieldsVBox.getChildren().add(createSeparator());
                    }
                }
            }
        });
    }

    /**
     * Refactored to use ObservableComposer pattern for proper transaction management.
     */
    private void loadUIData() {
        if (observableEntitySnapshot == null) {
            return;
        }
        if (!(observableEntitySnapshot instanceof ObservableSemanticSnapshot)) {
            return;
        }

        // Get the observable semantic from the handle
        observableEntityHandle.ifSemantic(observableSemantic -> {
            // Initialize composer if not already done
            initializeComposer();

            // Create semantic editor using composer unified API
            // Get referenced component and pattern from the semantic
            ObservableEntity referencedComponent = ObservableEntityHandle.get(observableSemantic.referencedComponentNid()).expectEntity();
            ObservablePattern pattern = ObservableEntityHandle.get(observableSemantic.patternNid()).expectPattern();
            semanticEditor = getObservableComposer().composeSemantic(observableSemantic.publicId(), referencedComponent, pattern);

            // Get editable version with cached editing capabilities
            editableVersion = semanticEditor.getEditableVersion();

            // Get the edit stamp for UI generation
            currentEditStamp = editableVersion.getEditStamp();

            ObservableList<ObservableField.Editable<?>> editables =  editableVersion.getEditableFields();
            // Generate UI nodes from editable fields
            for (ObservableField.Editable editableField : editables) {
                if (genPurposeViewModel.getPropertyValue(MODE) == CREATE && editableField.getValue() instanceof EntityProxy) {
                    // Set default blank concept for new semantics
                    @SuppressWarnings("unchecked")
                    ObservableField.Editable<EntityProxy> proxyField = (ObservableField.Editable<EntityProxy>) editableField;
                    proxyField.setValue(BLANK_CONCEPT);
                }

                Field field = editableField.field();
                KlField<?> klField = createEditableKlField(
                        (FieldRecord<?>) field,
                        editableField,
                        getViewProperties(),
                        currentEditStamp);
                // detect changes to rebind fields if the semantic version changes.
                klField.doOnEditableValuePropertyChange(()-> {
                    // Enable submit button and set flag to rebind if changes occur.
                    submitButton.setDisable(false);
                    readyToEditVersion.set(true); // indicate changes present. see listeners to change observable version.
                    LOG.info("readyToEditVersion = {}, submit button disable = {} %n",
                            readyToEditVersion.get(), submitButton.disableProperty().get());
                });
                getKlFields().add(klField);
                // Generate node using the underlying ObservableField (read-only view)
                nodes.add(klField.fxObject());
            }
            submitButton.setDisable(true);
            LOG.info("Loaded UI with {} editable fields using ObservableComposer", getKlFields().size());
        });

        //Set the hascode for the committed values.
//        enableDisableButtons();
        loadVBox();
    }

    /**
     * Reconstitutes the editable fields by rebinding them to the current observable semantic version.
     */
    private void rebindNewEditableVersion() {
        EntityFacade semantic = genPurposeViewModel.getPropertyValue(SEMANTIC);
        this.observableEntityHandle = ObservableEntityHandle.get(semantic.nid());
        this.observableEntityHandle.ifSemantic(observableSemantic -> {
            observableEntitySnapshot = observableSemantic.getSnapshot(getViewProperties().calculator());
            // Initialize composer if not already done
            initializeComposer();

            // Create semantic editor using composer unified API
            // Get referenced component and pattern from the semantic
            ObservableEntity referencedComponent = ObservableEntityHandle.get(observableSemantic.referencedComponentNid()).expectEntity();
            ObservablePattern pattern = ObservableEntityHandle.get(observableSemantic.patternNid()).expectPattern();
            semanticEditor = getObservableComposer().composeSemantic(observableSemantic.publicId(), referencedComponent, pattern);

            // Get editable version with cached editing capabilities
            editableVersion = semanticEditor.getEditableVersion();

            // Get the edit stamp for UI generation
            currentEditStamp = editableVersion.getEditStamp();

            // Get editable fields from the editable version
            // hold KlFields.
            ObservableList<ObservableField.Editable<?>> editables =  editableVersion.getEditableFields();
            // Generate UI nodes from editable fields
            for (int i = 0; i < getKlFields().size(); i++) {
                // Rebind each KlField to the new ObservableField.Editable
                KlField klField = getKlFields().get(i);
                ObservableField.Editable<?> newEditableField = editables.get(i);
                klField.rebind(newEditableField);
                // detect changes to rebind fields if the semantic version changes.
                klField.doOnEditableValuePropertyChange( () -> {
                    // Enable submit button and set flag to rebind if changes occur.
                    submitButton.setDisable(false);
                    readyToEditVersion.set(true); // indicate changes present. see listeners to change observable version.
                    LOG.info("readyToEditVersion = {}, submit button disable = {} %n",
                            readyToEditVersion.get(), submitButton.disableProperty().get());
                });
            }
            LOG.info("Reconstituted {} editable fields using ObservableComposer", getKlFields().size());
        });

    }
    private static Separator createSeparator() {
        Separator separator = new Separator();
        separator.getStyleClass().add("field-separator");
        separator.setFocusTraversable(false);
        return separator;
    }

    public ViewProperties getViewProperties() {
        return genPurposeViewModel.getPropertyValue(VIEW_PROPERTIES);
    }

    @FXML
    private void cancel(ActionEvent actionEvent) {
        doTheClearOrResetForm();
        EvtBusFactory.getDefaultEvtBus().publish(genPurposeViewModel.getPropertyValue(WINDOW_TOPIC), new PropertyPanelEvent(actionEvent.getSource(), CLOSE_PANEL));
        // if previous state was closed cancel will close properties bump out.
        // else show
    }

    @FXML
    private void clearOrResetForm(ActionEvent actionEvent) {
        // if create mode display the confirm clear dialog
        if (genPurposeViewModel.getPropertyValue(MODE) == CREATE) {
            ConfirmationDialogController.showConfirmationDialog(this.cancelButton, CONFIRM_CLEAR_TITLE, CONFIRM_CLEAR_MESSAGE)
                    .thenAccept(confirmed -> {
                        if (confirmed) {
                            doTheClearOrResetForm();
                        }
                    });
        } else {
            ConfirmationDialogController.showConfirmationDialog(this.cancelButton,
                            "Confirm Reset Form",
                            "Your changes will be lost if you reset the form. Are you sure you want to continue?")
                    .thenAccept(confirmed -> {
                        if (confirmed) {
                            doTheClearOrResetForm();
                        }
                    });
        }
    }

    private void doTheClearOrResetForm() {
        Latest<EntityVersion> latestCommitted =  retrieveCommittedLatestVersion(observableEntitySnapshot);
        latestCommitted.ifPresentOrElse(this::resetFieldValues, this::clearField);
        clearOrResetFormButton.setDisable(true);
    }

    /**
     * Clears the fields in create mode
     */
    private void clearField(){
        if (observableEntityHandle != null) {
            observableEntityHandle.ifPresent(observableEntity -> {
                EntityFacade patternForEntity = switch (observableEntity) {
                    case ObservableConcept concept -> EntityBinding.Concept.pattern();
                    case ObservablePattern pattern -> EntityBinding.Pattern.pattern();
                    case ObservableSemantic semantic -> semantic.pattern();
                    case ObservableStamp stamp -> EntityBinding.Stamp.pattern();
                };
                ImmutableList<Object> fieldValues = createDefaultFieldValues(patternForEntity, getViewProperties());
                for (int i = 0; i < fieldValues.size(); i++) {
                    ObservableField.Editable<?> editableField = getKlFields().get(i).fieldEditable();
                    // Use setValue() to update via editable field
                    @SuppressWarnings("unchecked")
                    ObservableField.Editable<Object> uncheckedField = (ObservableField.Editable<Object>) editableField;
                    uncheckedField.setValue(fieldValues.get(i));
                }
            });
        } else {
//              not sure if this is needed
//            editableFields.clear();
        }
    }

    /**
     * Reset the observable field values in edit mode.
     * @param entityVersion
     */
    private void resetFieldValues(EntityVersion entityVersion) {
        if (entityVersion instanceof SemanticEntityVersion semanticEntityVersion) {
            for(int i = 0; i < semanticEntityVersion.fieldValues().size(); i++){
                Object object = semanticEntityVersion.fieldValues().get(i);
                ObservableField.Editable<?> editableField = getKlFields().get(i).fieldEditable();
                // Use setValue() to update via editable field
                @SuppressWarnings("unchecked")
                ObservableField.Editable<Object> uncheckedField = (ObservableField.Editable<Object>) editableField;
                uncheckedField.setValue(object);
            }
        }
    }

    /**
     * Returns KlFields used to rebind {@link ObservableField.Editable} objects.
     * @return Returns klFields used to rebind {@link ObservableField.Editable} objects.
     */
    private List<KlField> getKlFields() {
        return klFields;
    }
    private List<ObservableField.Editable> getObservableEditables() {
        return getKlFields()
                .stream()
                .map(KlField::fieldEditable).toList();
    }
    /**
     * Refactored submit using ObservableComposer pattern.
     * Saves editable version and commits the transaction.
     */
    @FXML
    public void submit(ActionEvent actionEvent) {
        submitButton.setDisable(true);
        cancelButton.requestFocus();

        try {
            // Create list of current values for event publishing
            List<Object> fieldValues = getKlFields()
                    .stream()
                    .map(KlField::fieldEditable)
                    .map(ObservableField.Editable::getValue)
                    .toList();

            // Get the semantic for event publishing
            EntityFacade semantic = genPurposeViewModel.getPropertyValue(SEMANTIC);

            // Save editable version (creates uncommitted version)
            semanticEditor.save();
            LOG.info("Saved editable semantic version ");

            try {

                getObservableComposer().commit();
                LOG.info("Committed semantic changes successfully ");
                // Refresh observable handles and snapshots
                observableEntityHandle = ObservableEntityHandle.get(semantic.nid());
                if (observableEntityHandle.isPresent()) {
                    observableEntitySnapshot = observableEntityHandle.expectEntity()
                            .getSnapshot(getViewProperties().calculator());
                }

                // TODO: The following needs to be revisited.
//                 Recalculate committed hash for dirty trackingGenEditingEvent
//                processCommittedValues();
//                enableDisableButtons();

                // Publish event to refresh details area
                EvtBusFactory.getDefaultEvtBus().publish(
                        genPurposeViewModel.getPropertyValue(WINDOW_TOPIC),
                        new GenEditingEvent(actionEvent.getSource(), PUBLISH, fieldValues, semantic.nid())
                );

                // Show success message
                String submitMessage = "Semantic Details %s Successfully!"
                        .formatted(genPurposeViewModel.getStringProperty(MODE).equals(EDIT) ? "Edited" : "Added");
                toast().withUndoAction(undoActionEvent -> LOG.info("undo called"))
                        .show(Toast.Status.SUCCESS, submitMessage);

                // Cleanup and reset
                genPurposeViewModel.setPropertyValue(MODE, EDIT);
                composer = null;
                initializeComposer();
                readyToEditVersion.set(false); // reset change flag when user types older listener will trigger rebind.
            } catch (Exception e) {
                LOG.error("Error committing semantic changes", e);
                Platform.runLater(() -> {
                    Alert alert = new Alert(Alert.AlertType.ERROR,
                            "Failed to commit changes: " + e.getMessage(),
                            ButtonType.OK);
                    alert.setHeaderText("Commit Failed");
                    alert.showAndWait();
                });
            }

        } catch (Exception e) {
            LOG.error("Error during submit", e);
            Alert alert = new Alert(Alert.AlertType.ERROR,
                    "Failed to save changes: " + e.getMessage(),
                    ButtonType.OK);
            alert.setHeaderText("Save Failed");
            alert.showAndWait();
        }
    }
}
