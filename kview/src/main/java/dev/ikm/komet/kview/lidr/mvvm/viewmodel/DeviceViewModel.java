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
import org.carlfx.cognitive.validator.ValidationMessage;
import org.eclipse.collections.api.factory.Lists;
import org.eclipse.collections.api.list.MutableList;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.UUID;

public class DeviceViewModel extends FormViewModel {

    private static final Logger LOG = LoggerFactory.getLogger(DeviceViewModel.class);

    // --------------------------------------------
    // Known properties
    // --------------------------------------------
    public static String LIDR_RECORD = "lidrRecord";

    public static String DEVICE_ENTITY = "entityFacade";

    public static String FULLY_QUALIFIED_NAME = "fqn";

    public static String MANUFACTURER_ENTITY = "manufacturer";

    public DeviceViewModel() {
        super();
        addProperty(LIDR_RECORD, (SemanticRecord) null)
                .addProperty(CONCEPT_TOPIC, (UUID) null)
                .addProperty(VIEW_PROPERTIES, (ViewProperties) null)
                .addProperty(DEVICE_ENTITY, (EntityFacade) null) // this is/will be the device concept entity
                // in non-manual mode, a device entity will already have a FQN
                .addProperty(FULLY_QUALIFIED_NAME, (Object) null) // this is the FQN of the device concept
                .addProperty(MANUFACTURER_ENTITY, (EntityFacade) null); // this is the manufacturer concept

        //TODO add validations, for create LIDR_RECORD, DEVICE_ENTITY and MANUFACTURER_ENTITY all must be populated

        //TODO figure out when to set the mode to CREATE | EDIT; possibly set by the view?
        //setValue(MODE, CREATE);

    }

    public boolean createDevice(EditCoordinateRecord editCoordinateRecord) {
        save(); // View Model xfer values. does not save to the database but validates data and then copies data from properties to model values.

        // Validation errors will not create record.
        if (hasErrorMsgs()) {
            for (ValidationMessage validationMessage : getValidationMessages()) {
                LOG.error(validationMessage.toString());
            }
            return false;
        }
        //add the device concept and manufacturer concept to the LIDR_RECORD Semantic Record
        Transaction transaction = Transaction.make();

        SemanticRecord lidrRecord = getPropertyValue(LIDR_RECORD);

        StampEntity stampEntity = transaction.getStamp(
                State.fromConceptNid(TinkarTerm.ACTIVE_STATE.nid()), // default to active
                System.currentTimeMillis(),
                TinkarTerm.USER.nid(),
                lidrRecord.nid(), // is this correct?
                TinkarTerm.DEVELOPMENT_PATH.nid()); // default to dev path???

        MutableList<Object> descriptionFields = Lists.mutable.empty();
        descriptionFields.add(getValue(DEVICE_ENTITY));
        descriptionFields.add(getValue(MANUFACTURER_ENTITY));

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
