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


import static dev.ikm.komet.framework.events.FrameworkTopics.VERSION_CHANGED_TOPIC;
import static dev.ikm.komet.kview.events.EventTopics.SAVE_PATTERN_TOPIC;
import static dev.ikm.komet.kview.events.genediting.GenEditingEvent.PUBLISH;
import static dev.ikm.komet.kview.events.pattern.PatternCreationEvent.PATTERN_CREATION_EVENT;
import static dev.ikm.komet.kview.klfields.KlFieldHelper.calculteHashValue;
import static dev.ikm.komet.kview.klfields.KlFieldHelper.retrieveCommittedLatestVersion;
import static dev.ikm.komet.kview.mvvm.viewmodel.FormViewModel.CREATE;
import static dev.ikm.komet.kview.mvvm.viewmodel.FormViewModel.CURRENT_JOURNAL_WINDOW_TOPIC;
import static dev.ikm.komet.kview.mvvm.viewmodel.FormViewModel.EDIT;
import static dev.ikm.komet.kview.mvvm.viewmodel.FormViewModel.MODE;
import static dev.ikm.komet.kview.mvvm.viewmodel.FormViewModel.VIEW_PROPERTIES;
import static dev.ikm.komet.kview.mvvm.viewmodel.GenEditingViewModel.DEFAULT_FIELDS_HASH;
import static dev.ikm.komet.kview.mvvm.viewmodel.GenEditingViewModel.SEMANTIC;
import static dev.ikm.tinkar.provider.search.Indexer.FIELD_INDEX;
import static dev.ikm.tinkar.terms.TinkarTerm.ANONYMOUS_CONCEPT;
import static dev.ikm.tinkar.terms.TinkarTerm.BOOLEAN_FIELD;
import static dev.ikm.tinkar.terms.TinkarTerm.COMPONENT_FIELD;
import static dev.ikm.tinkar.terms.TinkarTerm.COMPONENT_ID_LIST_FIELD;
import static dev.ikm.tinkar.terms.TinkarTerm.COMPONENT_ID_SET_FIELD;
import static dev.ikm.tinkar.terms.TinkarTerm.FLOAT_FIELD;
import static dev.ikm.tinkar.terms.TinkarTerm.INTEGER_FIELD;
import static dev.ikm.tinkar.terms.TinkarTerm.STRING;
import static dev.ikm.tinkar.terms.TinkarTerm.STRING_FIELD;
import dev.ikm.komet.framework.events.EntityVersionChangeEvent;
import dev.ikm.komet.framework.events.EvtBusFactory;
import dev.ikm.komet.framework.events.Subscriber;
import dev.ikm.komet.framework.observable.ObservableEntity;
import dev.ikm.komet.framework.observable.ObservableField;
import dev.ikm.komet.framework.observable.ObservableSemantic;
import dev.ikm.komet.framework.observable.ObservableSemanticSnapshot;
import dev.ikm.komet.framework.observable.ObservableSemanticVersion;
import dev.ikm.komet.framework.view.ViewProperties;
import dev.ikm.komet.kview.events.genediting.GenEditingEvent;
import dev.ikm.komet.kview.events.pattern.PatternCreationEvent;
import dev.ikm.komet.kview.klfields.KlFieldHelper;
import dev.ikm.komet.kview.mvvm.viewmodel.GenEditingViewModel;
import dev.ikm.tinkar.common.id.IntIds;
import dev.ikm.tinkar.common.service.TinkExecutor;
import dev.ikm.tinkar.coordinate.stamp.calculator.Latest;
import dev.ikm.tinkar.coordinate.stamp.calculator.StampCalculator;
import dev.ikm.tinkar.entity.Entity;
import dev.ikm.tinkar.entity.EntityVersion;
import dev.ikm.tinkar.entity.FieldDefinitionForEntity;
import dev.ikm.tinkar.entity.FieldRecord;
import dev.ikm.tinkar.entity.PatternEntityVersion;
import dev.ikm.tinkar.entity.SemanticEntityVersion;
import dev.ikm.tinkar.entity.SemanticVersionRecord;
import dev.ikm.tinkar.entity.StampRecord;
import dev.ikm.tinkar.entity.VersionData;
import dev.ikm.tinkar.entity.transaction.CommitTransactionTask;
import dev.ikm.tinkar.entity.transaction.Transaction;
import dev.ikm.tinkar.terms.EntityFacade;
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

public class SemanticFieldsController {

    private static final Logger LOG = LoggerFactory.getLogger(SemanticFieldsController.class);

    @FXML
    private VBox editFieldsVBox;
    @FXML
    private Button cancelButton;

    @FXML
    private Button clearFormButton;

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

    private void enableDisableSubmitButton(){
        //Disable submit button if any of the fields are blank.
        boolean disabled = checkForEmptyFields();
        if (!disabled) {
            int uncommittedHash = calculteHashValue(observableFields);
            // carve out in the logic so that when you are in create mode and the default values
            // are exactly what they are then you are allowed to just save them as is
            if ((genEditingViewModel.getPropertyValue(MODE) == CREATE) &&
                (Integer.valueOf(uncommittedHash).equals(genEditingViewModel.getPropertyValue(DEFAULT_FIELDS_HASH)))) {
                disabled = false;
            } else {
                disabled = (committedHash == uncommittedHash);
            }
        }
        submitButton.setDisable(disabled);
    }

    /**
     * This method checks for empty/blank/null fields
     * @return invalid
     */
    private boolean checkForEmptyFields() {
        AtomicBoolean invalid = new AtomicBoolean(false);
        observableFields.forEach(observableField -> {
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
               immutableList.set(observableSemanticSnapshot.getLatestFields().get()); // get the latest fields
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
        if (immutableList.get() !=null) {
            List<ObservableField<?>> observableFieldsList = new ArrayList<>((Collection) immutableList.get());
            committedHash = calculteHashValue(observableFieldsList);  // and calculate the hashValue for commited data.
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
                        new PatternCreationEvent(this, PATTERN_CREATION_EVENT));
                reloadPatternNavigator = false;
            }
            enableDisableSubmitButton();
        };

        EvtBusFactory.getDefaultEvtBus().subscribe(VERSION_CHANGED_TOPIC,
                EntityVersionChangeEvent.class, entityVersionChangeEventSubscriber);

        //Change the button name to RESET FORM
        if (genEditingViewModel.getPropertyValue(MODE) == EDIT) {
            clearFormButton.setText("RESET FORM");
        }
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
            if (fieldIdx >= 0) {
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
        StampCalculator stampCalculator = getViewProperties().calculator().stampCalculator();
        Latest<SemanticEntityVersion> semanticEntityVersionLatest = stampCalculator.latest(observableSemantic.nid());
        if (semanticEntityVersionLatest.isPresent()) {
            // Populate the Semantic Details
            // Displaying editable controls and populating the observable fields array list.
            observableFields.clear();
            observableFields.addAll((Collection) observableSemanticSnapshot.getLatestFields().get());
            observableFields.forEach(observableField -> {
                // disable calling writeToData method of observable field by setting refresh flag to true.
                FieldRecord<?> fieldRecord = observableField.field();
                nodes.add(KlFieldHelper.generateNode(fieldRecord, observableField, getViewProperties(), semanticEntityVersionLatest, true));
                observableField.autoSaveOn();
              });
        }
        //Set the hascode for the committed values.
        enableDisableSubmitButton();
        loadVBox();
    }

    private static Separator createSeparator() {
        Separator separator = new Separator();
        separator.getStyleClass().add("field-separator");
        return separator;
    }

    public ViewProperties getViewProperties() {
        return genEditingViewModel.getPropertyValue(VIEW_PROPERTIES);
    }

    @FXML
    private void cancel(ActionEvent actionEvent) {
        actionEvent.consume();
        // if previous state was closed cancel will close properties bump out.
        // else show
    }

    @FXML
    private void clearForm(ActionEvent actionEvent) {
        EntityFacade semantic = genEditingViewModel.getPropertyValue(SEMANTIC);
        Latest<SemanticEntityVersion>  latestCommitted =  retrieveCommittedLatestVersion(observableSemanticSnapshot);
        latestCommitted.ifPresentOrElse(this::updateFieldValues, () -> {
            EntityFacade pattern = EntityFacade.make(observableSemantic.pattern().nid());
            Latest<PatternEntityVersion> patternEntityVersionLatest = getViewProperties().calculator().latest(pattern.nid());
            updateFieldValues(patternEntityVersionLatest.get());
        });
        actionEvent.consume();
    }

    private void updateFieldValues(EntityVersion entityVersion) {
        if(entityVersion instanceof PatternEntityVersion patternEntityVersion) {
            for (int i = 0; i < patternEntityVersion.fieldDefinitions().size(); i++) {
                Object object = null;
                FieldDefinitionForEntity fieldDefinitionForEntity = patternEntityVersion.fieldDefinitions().get(i);
                if (fieldDefinitionForEntity.dataTypeNid() == COMPONENT_FIELD.nid()) {
                    object = ANONYMOUS_CONCEPT;
                } else if (fieldDefinitionForEntity.dataTypeNid() == STRING_FIELD.nid()
                        || fieldDefinitionForEntity.dataTypeNid() == STRING.nid()) {
                    object = "";
                } else if (fieldDefinitionForEntity.dataTypeNid() == INTEGER_FIELD.nid()) {
                    object = 0;
                } else if (fieldDefinitionForEntity.dataTypeNid() == FLOAT_FIELD.nid()) {
                    object = 0.0F;
                } else if (fieldDefinitionForEntity.dataTypeNid() == BOOLEAN_FIELD.nid()) {
                    object = false;
                } else if (fieldDefinitionForEntity.dataTypeNid() == COMPONENT_ID_LIST_FIELD.nid()) {
                    object = IntIds.list.empty();
                } else if (fieldDefinitionForEntity.dataTypeNid() == COMPONENT_ID_SET_FIELD.nid()) {
                    object = IntIds.set.empty();
                }
                ObservableField observableField = observableFields.get(i);
                observableField.valueProperty().setValue(object);
            }
        } else if (entityVersion instanceof SemanticEntityVersion semanticEntityVersion) {
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
       List<ObservableField<?>> list = new ArrayList<>(observableFields);

       //Get the semantic need to pass along with event for loading values across Opened Semantics.
       EntityFacade semantic = genEditingViewModel.getPropertyValue(SEMANTIC);

       Latest<SemanticEntityVersion> semanticEntityVersionLatest = getViewProperties().calculator().stampCalculator().latest(semantic.nid());
       semanticEntityVersionLatest.ifPresent(semanticEntityVersion -> {
           StampRecord stamp = Entity.getStamp(semanticEntityVersion.stampNid());
           SemanticVersionRecord version = Entity.getVersionFast(observableSemantic.nid(), stamp.nid());
           Transaction.forVersion(version).ifPresentOrElse(transaction -> {
               commitTransactionTask(transaction);
               // EventBus implementation changes to refresh the details area if commit successful
               EvtBusFactory.getDefaultEvtBus().publish(genEditingViewModel.getPropertyValue(CURRENT_JOURNAL_WINDOW_TOPIC),
                       new GenEditingEvent(actionEvent.getSource(), PUBLISH, list, observableSemantic.nid()));
               // refesh the pattern navigation
               EvtBusFactory.getDefaultEvtBus().publish(SAVE_PATTERN_TOPIC,
                       new PatternCreationEvent(actionEvent.getSource(), PATTERN_CREATION_EVENT));
           }, () -> {
               //TODO this is a temp alert / workaround till we figure how to reload transactions across multiple restarts of app.
               LOG.error("Unable to commit: Transaction for the given version does not exist.");
               Alert alert = new Alert(Alert.AlertType.ERROR, "Transaction for current changes does not exist.", ButtonType.OK);
               alert.setHeaderText("Unable to Commit transaction.");
               alert.showAndWait();
           });
       });

        // EventBus implementation changes to refresh the details area if commit successful
        EvtBusFactory.getDefaultEvtBus().publish(genEditingViewModel.getPropertyValue(CURRENT_JOURNAL_WINDOW_TOPIC),
                new GenEditingEvent(actionEvent.getSource(), PUBLISH, list, semantic.nid()));

        processCommittedValues();
        enableDisableSubmitButton();
    }

    private void commitTransactionTask(Transaction transaction) {
        CommitTransactionTask commitTransactionTask = new CommitTransactionTask(transaction);
        TinkExecutor.threadPool().submit(commitTransactionTask);
    }
}
