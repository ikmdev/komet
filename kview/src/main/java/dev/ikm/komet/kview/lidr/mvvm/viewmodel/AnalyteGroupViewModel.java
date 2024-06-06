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
package dev.ikm.komet.kview.lidr.mvvm.viewmodel;

import dev.ikm.komet.kview.mvvm.viewmodel.FormViewModel;
import dev.ikm.komet.framework.view.ViewProperties;
import dev.ikm.tinkar.common.service.TinkExecutor;
import dev.ikm.tinkar.coordinate.edit.EditCoordinateRecord;
import dev.ikm.tinkar.entity.*;
import dev.ikm.tinkar.entity.transaction.CommitTransactionTask;
import dev.ikm.tinkar.entity.transaction.Transaction;
import dev.ikm.tinkar.terms.EntityFacade;
import dev.ikm.tinkar.terms.State;
import dev.ikm.tinkar.terms.TinkarTerm;
import org.carlfx.cognitive.validator.MessageType;
import org.carlfx.cognitive.validator.ValidationMessage;
import org.carlfx.cognitive.viewmodel.ViewModel;
import org.eclipse.collections.api.factory.Lists;
import org.eclipse.collections.api.list.MutableList;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.UUID;

public class AnalyteGroupViewModel extends FormViewModel {
    private static final Logger LOG = LoggerFactory.getLogger(AnalyteGroupViewModel.class);


    public static String LIDR_RECORD = "lidrRecord";

    public static String ANALYTE_ENTITY = "analyteEntity";
    public static String TARGET_ENTITIES = "targetEntities";

    public static String RESULT_ENTITIES = "resultEntities";

    public static String SPECIMEN_ENTITIES = "specimenEntities";

    public static String IS_POPULATED = "isPopulated";
    public static String SAVE_BUTTON_STATE = "buttonState";

    public AnalyteGroupViewModel() {
        super();
        addProperty(CONCEPT_TOPIC, (UUID) null)
                .addProperty(VIEW_PROPERTIES, (ViewProperties) null)
                .addProperty(SAVE_BUTTON_STATE, true)        // disable property (true) by default
                .addProperty(ANALYTE_ENTITY, (EntityFacade) null)   // this is an analyte as a concept
                .addProperty(TARGET_ENTITIES, new ArrayList<>())    // analytes have targets
                .addProperty(RESULT_ENTITIES, new ArrayList<>())    // this represents the results as a concept
                .addProperty(SPECIMEN_ENTITIES, new ArrayList<>()); // this is the specimen as a concept

        // Is Analyte Group valid? Custom validator will alter button state.
        addValidator(IS_POPULATED, "Is Populated", (Void prop, ViewModel vm) -> {
            // if any fields are empty then it is not populated (invalid)
            if (getPropertyValue(ANALYTE_ENTITY) == null
                    || getObservableList(TARGET_ENTITIES).size() == 0
                    || getObservableList(RESULT_ENTITIES).size() == 0
                    || getObservableList(SPECIMEN_ENTITIES).size() == 0) {

                // update is populated
                setPropertyValue(SAVE_BUTTON_STATE, true); // disable is true
                // let caller know why it is not valid
                return new ValidationMessage(SAVE_BUTTON_STATE, MessageType.ERROR, "Analyte Group is not populated");
            }
            // update is populated
            setPropertyValue(SAVE_BUTTON_STATE, false); // disable is false (enabled)
            return VALID;
        });
        //TODO figure out when to set the mode to CREATE | EDIT; possibly set by the view?
        //setValue(MODE, CREATE);
    }

    public boolean createAnalyteGroup(EditCoordinateRecord editCoordinateRecord) {
        // Validation errors will not create record.
        if (hasErrorMsgs()) {
            for (ValidationMessage validationMessage : getValidationMessages()) {
                LOG.error(validationMessage.toString());
            }
            return false;
        }
        Transaction transaction = Transaction.make();

        // TODO LIDR_RECORD is not a semantic record, it is a LidrRecord object.
        SemanticRecord lidrRecord = getPropertyValue(LIDR_RECORD);

        StampEntity stampEntity = transaction.getStamp(
                State.fromConcept(TinkarTerm.ACTIVE_STATE), // default to active
                System.currentTimeMillis(),
                TinkarTerm.USER.nid(),
                lidrRecord.nid(), // is this correct?
                TinkarTerm.DEVELOPMENT_PATH.nid()); // default to dev path???

        MutableList<Object> descriptionFields = Lists.mutable.empty();
        descriptionFields.add(getPropertyValue(ANALYTE_ENTITY));
        descriptionFields.add(getPropertyValue(TARGET_ENTITIES));
        descriptionFields.add(getPropertyValue(RESULT_ENTITIES));
        descriptionFields.add(getPropertyValue(SPECIMEN_ENTITIES));

        RecordListBuilder versions = RecordListBuilder.make();
        lidrRecord.versions().forEach(version -> versions.add(version));
        versions.add(SemanticVersionRecordBuilder.builder()
                .chronology(lidrRecord)
                .stampNid(stampEntity.nid())
                .fieldValues(descriptionFields.toImmutable())
                .build());

        // apply the updated versions to the new semantic record
        SemanticRecord newLidrRecord = SemanticRecordBuilder.builder(lidrRecord).versions(versions.toImmutable()).build();

        // put the new semantic record in the transaction
        transaction.addComponent(newLidrRecord);

        // perform the save
        Entity.provider().putEntity(newLidrRecord);

        // commit the transaction
        CommitTransactionTask commitTransactionTask = new CommitTransactionTask(transaction);
        TinkExecutor.threadPool().submit(commitTransactionTask);

        LOG.info("transaction complete");

        return true;
    }
}
