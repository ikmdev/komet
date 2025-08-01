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


import dev.ikm.tinkar.events.EntityVersionChangeEvent;
import dev.ikm.tinkar.events.EvtBusFactory;
import dev.ikm.tinkar.events.Subscriber;
import dev.ikm.komet.framework.observable.*;
import dev.ikm.komet.framework.view.ViewProperties;
import dev.ikm.komet.kview.controls.Toast;
import dev.ikm.komet.kview.events.genediting.GenEditingEvent;
import dev.ikm.komet.kview.events.genediting.PropertyPanelEvent;
import dev.ikm.komet.kview.events.pattern.PatternSavedEvent;
import dev.ikm.komet.kview.mvvm.viewmodel.GenEditingViewModel;
import dev.ikm.tinkar.common.service.TinkExecutor;
import dev.ikm.tinkar.coordinate.stamp.calculator.Latest;
import dev.ikm.tinkar.entity.*;
import dev.ikm.tinkar.entity.transaction.CommitTransactionTask;
import dev.ikm.tinkar.entity.transaction.Transaction;
import dev.ikm.tinkar.terms.EntityFacade;
import dev.ikm.tinkar.terms.EntityProxy;
import javafx.application.Platform;
import javafx.beans.property.ObjectProperty;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.Node;
import javafx.scene.control.*;
import javafx.scene.layout.VBox;
import org.carlfx.cognitive.loader.InjectViewModel;
import org.eclipse.collections.api.list.ImmutableList;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.Future;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicReference;

import static dev.ikm.tinkar.events.FrameworkTopics.VERSION_CHANGED_TOPIC;
import static dev.ikm.komet.kview.events.EventTopics.SAVE_PATTERN_TOPIC;
import static dev.ikm.komet.kview.events.genediting.GenEditingEvent.PUBLISH;
import static dev.ikm.komet.kview.events.genediting.PropertyPanelEvent.CLOSE_PANEL;
import static dev.ikm.komet.kview.klfields.KlFieldHelper.*;
import static dev.ikm.komet.kview.mvvm.view.journal.JournalController.toast;
import static dev.ikm.komet.kview.mvvm.viewmodel.FormViewModel.*;
import static dev.ikm.komet.kview.mvvm.viewmodel.GenEditingViewModel.SEMANTIC;
import static dev.ikm.komet.kview.mvvm.viewmodel.GenEditingViewModel.WINDOW_TOPIC;
import static dev.ikm.tinkar.provider.search.Indexer.FIELD_INDEX;
import static dev.ikm.tinkar.terms.TinkarTerm.ANONYMOUS_CONCEPT;
import static dev.ikm.tinkar.terms.TinkarTerm.IMAGE_FIELD;

public class SemanticFieldsController {

    private static final Logger LOG = LoggerFactory.getLogger(SemanticFieldsController.class);

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
    private GenEditingViewModel genEditingViewModel;

    private List<ObservableField<?>> observableFields = new ArrayList<>();

    private List<Node> nodes = new ArrayList<>();

    private int committedHash;

    ObservableSemantic observableSemantic;

    ObservableSemanticSnapshot observableSemanticSnapshot;

    Subscriber<EntityVersionChangeEvent> entityVersionChangeEventSubscriber;

    private boolean reloadPatternNavigator;

    private void enableDisableButtons() {
        boolean emptyFields = checkForEmptyFields();
        int uncommittedHash = calculateHashValue(observableFields);
        boolean fieldsHaveNotChanged = committedHash == uncommittedHash;

        submitButton.setDisable(emptyFields || fieldsHaveNotChanged);
        clearOrResetFormButton.setDisable(fieldsHaveNotChanged);
    }

    /**
     * This method checks for empty/blank/null fields
     * @return invalid
     */
    private boolean checkForEmptyFields() {
        AtomicBoolean invalid = new AtomicBoolean(false);
        observableFields.forEach(observableField -> {
            if (observableField.dataTypeNid() == IMAGE_FIELD.nid()) {
                invalid.set(observableField.valueProperty().get() == null || (((byte[]) observableField.valueProperty().get()).length == 0));
            }
            if (!invalid.get()) {
                invalid.set((observableField.value() == null || observableField.value().toString().isEmpty()));
            }
        });
        return invalid.get();
    }

    private void processCommittedValues() {
        AtomicReference<ImmutableList<ObservableField>> immutableList = new AtomicReference<>();
        //Get the latest version
        Latest<ObservableSemanticVersion> observableSemanticVersionLatest = observableSemanticSnapshot.getLatestVersion();
        observableSemanticVersionLatest.ifPresent(observableSemanticVersion -> { // if latest version present
           if (observableSemanticVersion.committed()) { // and if latest version is committed then,
               immutableList.set(observableSemanticSnapshot.getLatestFields(true, false).get()); // get the latest fields
           } else { //if The latest version is Uncommitted, then retrieve the committed version from historic versions list.
               ImmutableList<ObservableSemanticVersion> observableSemanticVersionImmutableList = observableSemanticSnapshot.getHistoricVersions();
               // replace any versions with uncommited stamp
               Optional<ObservableSemanticVersion> observableSemanticVersionOptional = observableSemanticVersionImmutableList.stream().filter(VersionData::committed).findFirst();
               observableSemanticVersionOptional.ifPresent(committedObservableSemanticVersion -> {
                   EntityFacade pattern = EntityFacade.make(committedObservableSemanticVersion.patternNid());
                   Latest<PatternEntityVersion> patternEntityVersionLatest = getViewProperties().calculator().latest(pattern.nid());
                   if (patternEntityVersionLatest.isPresent()) {
                       immutableList.set(committedObservableSemanticVersion.fields(patternEntityVersionLatest.get()));
                   }
               });
           }
        });
        if (immutableList.get() != null) {
            List<ObservableField<?>> observableFieldsList = new ArrayList<>((Collection) immutableList.get());
            committedHash = calculateHashValue(observableFieldsList);  // and calculate the hashValue for commited data.
        }

    }

    @FXML
    private void initialize() {
        // clear all semantic details.
        editFieldsVBox.setSpacing(8.0);
        editFieldsVBox.getChildren().clear();
        submitButton.setDisable(true);
        genEditingViewModel.save();
        EntityFacade semantic = genEditingViewModel.getPropertyValue(SEMANTIC);
        reloadPatternNavigator = true;
        ObjectProperty<EntityFacade> semanticProperty = genEditingViewModel.getProperty(SEMANTIC);
        // listen if the semantic property is updated during Create mode.
        semanticProperty.addListener( _ -> setupEditSemanticDetails());

        if (semantic != null && genEditingViewModel.getPropertyValue(MODE) == EDIT) {
            //Change the button name to RESET FORM in EDIT MODE
            clearOrResetFormButton.setText("RESET FORM");
            setupEditSemanticDetails();
        }
        genEditingViewModel.getProperty(MODE).subscribe((mode) -> {
            if(mode == EDIT){
                clearOrResetFormButton.setText("RESET FORM");
            }else {
                clearOrResetFormButton.setText("CLEAR FORM");
            }
        });

    }

    private void setupEditSemanticDetails() {
        EntityFacade semantic = genEditingViewModel.getPropertyValue(SEMANTIC);
        observableSemantic = ObservableEntity.get(semantic.nid());
        observableSemanticSnapshot = observableSemantic.getSnapshot(getViewProperties().calculator());
        processCommittedValues();
        loadUIData(); // And populates Nodes and Observable fields.
        entityVersionChangeEventSubscriber = evt -> {
            LOG.info("Version has been updated: " + evt.getEventType());
            // get payload
            if (evt.getEntityVersion().nid() == observableSemantic.nid()
                    && evt.getEntityVersion() instanceof SemanticVersionRecord semanticVersionRecord) {
                ImmutableList<Object> values = semanticVersionRecord.fieldValues();
                for (int i = 0; i< values.size(); i++) {
                    ObservableField observableField = observableFields.get(i);
                    observableField.autoSaveOff();
                    observableField.valueProperty().set(values.get(i));
                    observableField.autoSaveOn();
                }
            }
            if(reloadPatternNavigator && genEditingViewModel.getPropertyValue(MODE) == CREATE) {
                // refresh the pattern navigation
                EvtBusFactory.getDefaultEvtBus().publish(SAVE_PATTERN_TOPIC,
                        new PatternSavedEvent(this, PatternSavedEvent.PATTERN_CREATION_EVENT));
                reloadPatternNavigator = false;
            }
            enableDisableButtons();
        };

        EvtBusFactory.getDefaultEvtBus().subscribe(VERSION_CHANGED_TOPIC,
                EntityVersionChangeEvent.class, entityVersionChangeEventSubscriber);
    }

    private void loadVBox() {
        // subscribe to changes... if the FIELD_INDEX is -1 or unset, then the user clicked the
        //  pencil icon and wants to edit all the fields
        // if the FIELD_INDEX is >= 0 then the user chose the context menu of a single field
        //  to edit that field
        genEditingViewModel.getObjectProperty(FIELD_INDEX).subscribe(fieldIndex -> {
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

    private void loadUIData() {
        nodes.clear();
        // Populate the Semantic Details
        // Displaying editable controls and populating the observable fields array list.
        observableFields.clear();
        if(observableSemanticSnapshot != null) {
            observableFields.addAll((Collection) observableSemanticSnapshot.getLatestFields(true, false).get());
        }
        observableFields.forEach(observableField -> {
            if (genEditingViewModel.getPropertyValue(MODE) == CREATE && observableField.value() instanceof EntityProxy entityProxy){
                if(entityProxy.nid() == ANONYMOUS_CONCEPT.nid()){
                    observableField.valueProperty().setValue(null);
                }
            }
            // disable calling writeToData method of observable field by setting refresh flag to true.
            FieldRecord<?> fieldRecord = observableField.field();
            nodes.add(generateNode(fieldRecord, observableField, getViewProperties(), true, genEditingViewModel.getPropertyValue(CURRENT_JOURNAL_WINDOW_TOPIC)));
            // Any changes top any observable field should re-enable the clear or reset button
            observableField.autoSaveOn();
          });

        //Set the hascode for the committed values.
        enableDisableButtons();
        loadVBox();
    }

    private static Separator createSeparator() {
        Separator separator = new Separator();
        separator.getStyleClass().add("field-separator");
        separator.setFocusTraversable(false);
        return separator;
    }

    public ViewProperties getViewProperties() {
        return genEditingViewModel.getPropertyValue(VIEW_PROPERTIES);
    }

    @FXML
    private void cancel(ActionEvent actionEvent) {
        doTheClearOrResetForm();
        EvtBusFactory.getDefaultEvtBus().publish(genEditingViewModel.getPropertyValue(WINDOW_TOPIC), new PropertyPanelEvent(actionEvent.getSource(), CLOSE_PANEL));
        // if previous state was closed cancel will close properties bump out.
        // else show
    }

    @FXML
    private void clearOrResetForm(ActionEvent actionEvent) {
        // if create mode display the confirm clear dialog
        if (genEditingViewModel.getPropertyValue(MODE) == CREATE) {
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
        Latest<SemanticEntityVersion>  latestCommitted =  retrieveCommittedLatestVersion(observableSemanticSnapshot);
        latestCommitted.ifPresentOrElse(this::resetFieldValues, this::clearField);
        clearOrResetFormButton.setDisable(true);
    }

    /**
     * Clears the fields in create mode
     */
    private void clearField(){
        EntityFacade pattern = EntityFacade.make(observableSemantic.pattern().nid());
        ImmutableList<Object> fieldValues = createDefaultFieldValues(pattern, getViewProperties());
        for (int i = 0; i < fieldValues.size(); i++) {
            ObservableField observableField = observableFields.get(i);
            observableField.valueProperty().setValue(fieldValues.get(i));
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
                ObservableField observableField = observableFields.get(i);
                observableField.valueProperty().setValue(object);
            }
        }
    }

    @FXML
    public void submit(ActionEvent actionEvent) {
       cancelButton.requestFocus();

       //create new list for passing to the event.
       List<Object> list = new ArrayList<>(observableFields.size());
       observableFields.forEach(observableField -> list.add(observableField.value()));

       //Get the semantic need to pass along with event for loading values across Opened Semantics.
       EntityFacade semantic = genEditingViewModel.getPropertyValue(SEMANTIC);

       Latest<SemanticEntityVersion> semanticEntityVersionLatest = getViewProperties().calculator().stampCalculator().latest(semantic.nid());
       semanticEntityVersionLatest.ifPresent(semanticEntityVersion -> {
           StampRecord stamp = Entity.getStamp(semanticEntityVersion.stampNid());
           SemanticVersionRecord version = Entity.getVersionFast(semantic.nid(), stamp.nid());
           Transaction.forVersion(version).ifPresentOrElse(transaction -> {
               //                       EntityService.get().endLoadPhase();
               createSemanticVersionTransactionTask(transaction, () -> {
                   // This runs after the first transaction parameter runs
                   Platform.runLater(() -> {
                       //update the observableSemantic version and observableSemanticSnapShot
                       observableSemantic = ObservableEntity.get(semantic.nid());
                       observableSemanticSnapshot = observableSemantic.getSnapshot(getViewProperties().calculator());
                       processCommittedValues();
                       enableDisableButtons();
                       // EventBus implementation changes to refresh the details area if commit successful
                       EvtBusFactory.getDefaultEvtBus().publish(genEditingViewModel.getPropertyValue(CURRENT_JOURNAL_WINDOW_TOPIC),
                               new GenEditingEvent(actionEvent.getSource(), PUBLISH, list, semantic.nid()));
//                       EntityService.get().beginLoadPhase();

                       String submitMessage = "Semantic Details %s Successfully!".formatted(genEditingViewModel.getStringProperty(MODE).equals(EDIT) ? "Editing" : "Added");
                       toast()
                               .withUndoAction(undoActionEvent ->
                                       LOG.info("undo called")
                               )
                               .show(
                                       Toast.Status.SUCCESS,
                                       submitMessage
                               );
                   });
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

    private Future<Void> createSemanticVersionTransactionTask(Transaction transaction, Runnable runAfterTransactionCompletes) {
        CommitTransactionTask commitTransactionTask = new CommitTransactionTask(transaction);
        return TinkExecutor.threadPool().submit(() -> {
            commitTransactionTask.call();
            runAfterTransactionCompletes.run();
            return null;
        });
    }
}
