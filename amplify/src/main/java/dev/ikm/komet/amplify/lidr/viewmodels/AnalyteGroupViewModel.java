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
package dev.ikm.komet.amplify.lidr.viewmodels;

import dev.ikm.komet.amplify.mvvm.validator.ValidationMessage;
import dev.ikm.komet.amplify.viewmodels.FormViewModel;
import dev.ikm.komet.framework.view.ViewProperties;
import dev.ikm.tinkar.common.service.TinkExecutor;
import dev.ikm.tinkar.coordinate.edit.EditCoordinateRecord;
import dev.ikm.tinkar.entity.*;
import dev.ikm.tinkar.entity.transaction.CommitTransactionTask;
import dev.ikm.tinkar.entity.transaction.Transaction;
import dev.ikm.tinkar.terms.EntityFacade;
import dev.ikm.tinkar.terms.State;
import dev.ikm.tinkar.terms.TinkarTerm;
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

    public static String RESULT_ENTITIES = "resultEntities";

    public static String SPECIMEN_ENTITIES = "specimenEntities";

    public AnalyteGroupViewModel() {
        super();
        addProperty(CONCEPT_TOPIC, (UUID) null)
                .addProperty(VIEW_PROPERTIES, (ViewProperties) null)
                .addProperty(ANALYTE_ENTITY, (EntityFacade) null) // this is an analyte as a concept
                .addProperty(RESULT_ENTITIES, new ArrayList<>()) // this represents the results as a concept
                .addProperty(SPECIMEN_ENTITIES, new ArrayList<>()); // this is the specimen as a concept

        //TODO add validations

        //TODO figure out when to set the mode to CREATE | EDIT; possibly set by the controller?
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
