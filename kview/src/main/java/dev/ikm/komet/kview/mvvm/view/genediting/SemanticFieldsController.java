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


import static dev.ikm.komet.kview.events.genediting.GenEditingEvent.PUBLISH;
import static dev.ikm.komet.kview.mvvm.viewmodel.FormViewModel.CURRENT_JOURNAL_WINDOW_TOPIC;
import static dev.ikm.komet.kview.mvvm.viewmodel.FormViewModel.VIEW_PROPERTIES;
import static dev.ikm.komet.kview.mvvm.viewmodel.GenEditingViewModel.SEMANTIC;
import static dev.ikm.tinkar.provider.search.Indexer.FIELD_INDEX;
import dev.ikm.komet.framework.events.EvtBusFactory;
import dev.ikm.komet.framework.observable.ObservableField;
import dev.ikm.komet.framework.view.ViewProperties;
import dev.ikm.komet.kview.events.genediting.GenEditingEvent;
import dev.ikm.komet.kview.klfields.KlFieldHelper;
import dev.ikm.komet.kview.mvvm.model.DataModelHelper;
import dev.ikm.tinkar.common.alert.AlertObject;
import dev.ikm.tinkar.common.alert.AlertStreams;
import dev.ikm.tinkar.common.id.IntIdCollection;
import dev.ikm.tinkar.common.id.PublicId;
import dev.ikm.tinkar.common.service.TinkExecutor;
import dev.ikm.tinkar.coordinate.stamp.calculator.Latest;
import dev.ikm.tinkar.coordinate.stamp.calculator.StampCalculator;
import dev.ikm.tinkar.entity.Entity;
import dev.ikm.tinkar.entity.FieldRecord;
import dev.ikm.tinkar.entity.PatternEntityVersion;
import dev.ikm.tinkar.entity.SemanticEntityVersion;
import dev.ikm.tinkar.entity.SemanticRecord;
import dev.ikm.tinkar.entity.SemanticVersionRecord;
import dev.ikm.tinkar.entity.StampEntity;
import dev.ikm.tinkar.entity.StampRecord;
import dev.ikm.tinkar.entity.transaction.CommitTransactionTask;
import dev.ikm.tinkar.entity.transaction.Transaction;
import dev.ikm.tinkar.terms.EntityFacade;
import dev.ikm.tinkar.terms.TinkarTerm;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.Node;
import javafx.scene.control.Button;
import javafx.scene.control.Separator;
import javafx.scene.layout.VBox;
import org.carlfx.cognitive.loader.InjectViewModel;
import org.carlfx.cognitive.viewmodel.ValidationViewModel;
import org.eclipse.collections.api.factory.Lists;
import org.eclipse.collections.api.list.MutableList;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.concurrent.Future;
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
    private ValidationViewModel semanticFieldsViewModel;

    private List<ObservableField<?>> observableFields = new ArrayList<>();

    private List<Node> nodes = new ArrayList<>();

    // Maintain the list of committed field records
    private final List<FieldRecord<?>> commitedFields = new ArrayList<>();


    //Enable/Disable submit button if field values change.
    private void semanticModified() {
        boolean disableButton = true;
        for(ObservableField<?> observableField : observableFields){
            FieldRecord<?> commmitedFieldRecord = commitedFields.get(observableField.fieldIndex());
            if(disableButton){
                disableButton = commmitedFieldRecord.value().equals(observableField.value());
            }
            if(disableButton && PublicId.equals(observableField.dataType().publicId(), TinkarTerm.COMPONENT_ID_LIST_FIELD.publicId()) ||
                    PublicId.equals(observableField.dataType().publicId(),TinkarTerm.COMPONENT_ID_SET_FIELD.publicId())){
                disableButton = fieldOrderChanged(observableField, commmitedFieldRecord);
            }

        }
        submitButton.disableProperty().setValue(disableButton);
    }

    private boolean fieldOrderChanged(ObservableField<?> observableField, FieldRecord<?> commmitedFieldRecord) {
            int [] originalIntArray = ((IntIdCollection) commmitedFieldRecord.value()).toArray();
            int[] changedIntIdArray = ((IntIdCollection) observableField.value()).toArray();

        return originalIntArray.length == changedIntIdArray.length && Arrays.hashCode(originalIntArray) == Arrays.hashCode(changedIntIdArray);

    }

    @FXML
    private void initialize() {
        // clear all semantic details.
        editFieldsVBox.setSpacing(8.0);
        editFieldsVBox.getChildren().clear();

        EntityFacade semantic = semanticFieldsViewModel.getPropertyValue(SEMANTIC);
        if (semantic != null) {
            StampCalculator stampCalculator = getViewProperties().calculator().stampCalculator();
            Latest<SemanticEntityVersion> semanticEntityVersionLatest = stampCalculator.latest(semantic.nid());
            if (semanticEntityVersionLatest.isPresent()) {
                // Populate the Semantic Details
                // Displaying editable controls and populating the observable fields array list.
                observableFields.addAll(KlFieldHelper
                        .generateObservableFieldsAndNodes(getViewProperties(),
                                nodes,
                                semanticEntityVersionLatest, true));
                editFieldsVBox.getChildren().clear();

                //Load CommittedField Data
                commitedFields.addAll(loadCommittedData(semantic));
                //Add listener for valueProperty of each field to check when data is modified.
                observableFields.forEach(observableField ->
                        observableField.valueProperty()
                        .subscribe(newvalue -> semanticModified())
                );

                semanticModified();
            } else {
                // TODO Add a new semantic based on a pattern (blank fields).
            }
        }

        // subscribe to changes... if the FIELD_INDEX is -1 or unset, then the user clicked the
        //  pencil icon and wants to edit all the fields
        // if the FIELD_INDEX is >= 0 then the user chose the context menu of a single field
        //  to edit that field
        semanticFieldsViewModel.getObjectProperty(FIELD_INDEX).subscribe(fieldIndex -> {
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

    private List<FieldRecord<Object>> loadCommittedData(EntityFacade semantic) {
        List<FieldRecord<Object>> commitedFieldData = new ArrayList<>();
        StampCalculator stampCalculator = getViewProperties().calculator().stampCalculator();
        Latest<SemanticEntityVersion> semanticEntityVersionLatest = stampCalculator.latest(semantic.nid());
        semanticEntityVersionLatest.ifPresent(semanticEntityVersion -> {
            Latest<PatternEntityVersion> patternEntityVersionLatest = stampCalculator.latest(semanticEntityVersion.pattern());
            patternEntityVersionLatest.ifPresent(patternEntityVersion -> {
                List<FieldRecord<Object>> fieldRecords = DataModelHelper.fieldRecords(semanticEntityVersion, patternEntityVersion);
                commitedFieldData.addAll(fieldRecords);
            });
        });
        return commitedFieldData;
    }



    private static Separator createSeparator() {
        Separator separator = new Separator();
        separator.getStyleClass().add("field-separator");
        return separator;
    }

    public ViewProperties getViewProperties() {
        return semanticFieldsViewModel.getPropertyValue(VIEW_PROPERTIES);
    }

    @FXML
    private void cancel(ActionEvent actionEvent) {
        actionEvent.consume();
        // if previous state was closed cancel will close properties bump out.
        // else show
    }

    @FXML
    private void clearForm(ActionEvent actionEvent) {
        actionEvent.consume();
    }

    @FXML
    public void submit(ActionEvent actionEvent) {
        cancelButton.requestFocus();
        //create new list for passing to the event.
        List<ObservableField<?>> list = new ArrayList<>(observableFields);

        Transaction transaction = writeToTempTranscation();
        if(transaction != null){
            commitTransactionTask(transaction);
        }

        //Get the semantic need to pass along with event for loading values across Opened Semantics.
        EntityFacade semantic = semanticFieldsViewModel.getPropertyValue(SEMANTIC);
        //EventBus implementation changes to refresh the details area
        EvtBusFactory.getDefaultEvtBus().publish(semanticFieldsViewModel.getPropertyValue(CURRENT_JOURNAL_WINDOW_TOPIC), new GenEditingEvent(actionEvent.getSource(), PUBLISH, list, semantic.nid()));

    }

    /**  This method is used to create and return transaction instance when records are modified.
     *
     * @return transaction
     */
    private Transaction writeToTempTranscation() {
        EntityFacade semantic = semanticFieldsViewModel.getPropertyValue(SEMANTIC);
        SemanticRecord semanticRecord =  Entity.getFast(semantic.nid());
        AtomicReference<Transaction> transactionAtomicReference = new AtomicReference<>();
        StampCalculator stampCalculator = getViewProperties().calculator().stampCalculator();
        Latest<SemanticEntityVersion> semanticEntityVersionLatest = stampCalculator.latest(semantic.nid());
        semanticEntityVersionLatest.ifPresent(semanticEntityVersion ->{
            StampRecord stamp = Entity.getStamp(semanticEntityVersion.stampNid());
            SemanticVersionRecord version = Entity.getVersionFast(semantic.nid(), stamp.nid());
            MutableList fieldsForNewVersion = Lists.mutable.of(version.fieldValues().toArray());
            observableFields.forEach(of -> {
                fieldsForNewVersion.set(of.fieldIndex(), of.value());
            });
            SemanticVersionRecord newVersion =null;
            if(stamp.lastVersion().committed()){
                // Create transaction
                transactionAtomicReference.set(Transaction.make());
                // newStamp already written to the entity store.
                StampEntity newStamp = transactionAtomicReference.get().getStampForEntities(stamp.state(), stamp.authorNid(), stamp.moduleNid(), stamp.pathNid(), version.entity());
                // Create new version...
                newVersion = version.with().fieldValues(fieldsForNewVersion.toImmutable()).stampNid(newStamp.nid()).build();

            }else {
                newVersion = version.withFieldValues(fieldsForNewVersion.toImmutable());
                // if a version with the same stamp as newVersion exists, that version will be removed
            }

            if(newVersion != null){
                // prior to adding the new version so you don't get duplicate versions with the same stamp.
                SemanticRecord analogue = semanticRecord.with(newVersion).build();
                // Entity provider will broadcast the nid of the changed entity.
                Entity.provider().putEntity(analogue);
            }
        });
        return transactionAtomicReference.get();
    }

    private static void commitTransactionTask(Transaction transaction) {
        CommitTransactionTask commitTransactionTask = new CommitTransactionTask(transaction);
        Future<Void> future = TinkExecutor.threadPool().submit(commitTransactionTask);
        TinkExecutor.threadPool().execute(() -> {
            try {
                future.get();
            } catch (Exception e) {
                AlertStreams.getRoot().dispatch(AlertObject.makeError(e));
            }
        });
    }
}
