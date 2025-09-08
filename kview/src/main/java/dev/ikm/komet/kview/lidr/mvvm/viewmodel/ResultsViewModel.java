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

import dev.ikm.komet.kview.lidr.mvvm.model.DataModelHelper;
import dev.ikm.komet.kview.mvvm.model.DescrName;
import dev.ikm.komet.kview.mvvm.viewmodel.FormViewModel;
import dev.ikm.komet.framework.builder.ConceptEntityBuilder;
import dev.ikm.komet.framework.view.ViewProperties;
import dev.ikm.tinkar.common.id.PublicId;
import dev.ikm.tinkar.common.id.PublicIds;
import dev.ikm.tinkar.common.service.TinkExecutor;
import dev.ikm.tinkar.coordinate.edit.EditCoordinateRecord;
import dev.ikm.tinkar.entity.ConceptRecord;
import dev.ikm.tinkar.entity.StampEntity;
import dev.ikm.tinkar.entity.transaction.CommitTransactionTask;
import dev.ikm.tinkar.entity.transaction.Transaction;
import dev.ikm.tinkar.terms.ConceptFacade;
import dev.ikm.tinkar.terms.EntityFacade;
import dev.ikm.tinkar.terms.State;
import dev.ikm.tinkar.terms.TinkarTerm;
import org.carlfx.cognitive.validator.MessageType;
import org.carlfx.cognitive.validator.ValidationMessage;
import org.carlfx.cognitive.viewmodel.ViewModel;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.Collection;
import java.util.UUID;

import static dev.ikm.komet.kview.lidr.mvvm.model.DataModelHelper.QUALITATIVE_CONCEPT;

public class ResultsViewModel extends FormViewModel {

    private static final Logger LOG = LoggerFactory.getLogger(ResultsViewModel.class);

    public static String RESULTS_NAME = "resultsName";

    public static String ALLOWABLE_RESULTS = "allowableResults";

    public static String SCALE_TYPE = "scaleType";
    public static String AVAILABLE_SCALE_TYPES = "availableScaleTypes";

    public static String DATA_RESULTS_TYPE = "dataResultsType";

    public static String RESULTS_PROPERTY = "resultsProperty";

    public static String EXAMPLE_UNITS = "exampleUnits";

    public static String REFERENCE_RANGES = "referenceRanges";
    public static String IS_POPULATED = "isPopulated";
    public static String ADD_BUTTON_STATE = "buttonState";


    public ResultsViewModel() {
        super();
        addProperty(CONCEPT_TOPIC, (UUID) null)
                .addProperty(VIEW_PROPERTIES, (ViewProperties) null)
                .addProperty(ADD_BUTTON_STATE, true)        // disable property (true) by default
                .addProperty(RESULTS_NAME, (String) null)
                .addProperty(ALLOWABLE_RESULTS, new ArrayList<EntityFacade>())
                .addProperty(SCALE_TYPE, (EntityFacade) null)
                .addProperty(AVAILABLE_SCALE_TYPES, (Collection<EntityFacade>) null)
                .addProperty(DATA_RESULTS_TYPE, (EntityFacade) null)
                .addProperty(RESULTS_PROPERTY, (EntityFacade) null)
                .addProperty(EXAMPLE_UNITS, (EntityFacade) null)
                .addProperty(REFERENCE_RANGES, (EntityFacade) null);

        // Is Analyte Group valid? Custom validator will alter button state.
        addValidator(IS_POPULATED, "Is Populated", (Void prop, ViewModel vm) -> {
            // check if it's qualitative vs quantitative.
            EntityFacade selectedDataResultType = getPropertyValue(DATA_RESULTS_TYPE);
            if (PublicId.equals(selectedDataResultType.publicId(), QUALITATIVE_CONCEPT.publicId())) {
                // if any fields are empty then it is not populated (invalid)
                if (getPropertyValue(RESULTS_NAME) == null
                        || getPropertyValue(RESULTS_NAME).toString().trim().isEmpty()
                        || getPropertyValue(SCALE_TYPE) == null
                        || getObservableList(ALLOWABLE_RESULTS).size() == 0) {

                    // update is populated
                    setPropertyValue(ADD_BUTTON_STATE, true); // disable is true
                    // let caller know why it is not valid
                    return new ValidationMessage(ADD_BUTTON_STATE, MessageType.ERROR, "Qualitative Results Conformance is not populated");
                }
                // update is populated
                setPropertyValue(ADD_BUTTON_STATE, false); // disable is false (enabled)
            } else {
                // TODO: Validate when it is quantitative
                setPropertyValue(ADD_BUTTON_STATE, true); // disable is true
            }

            return VALID;
        });
    }

    public boolean createResult(EditCoordinateRecord editCoordinateRecord) {
        save(); // View Model xfer values. does not save to the database but validates data and then copies data from properties to model values.
        // Validation errors will not create record.
        if (hasErrorMsgs()) {
            for (ValidationMessage validationMessage : getValidationMessages()) {
                LOG.error(validationMessage.toString());
            }
            return false;
        }

        //FIXME assuming the entire results record is a concept, then we need a concept
        // with a semantic description for its name and the rest of the fields would be child concepts


        // Create concept for the Result
        DescrName resultsName = getPropertyValue(RESULTS_NAME);

        Transaction transaction = Transaction.make("New concept for: " + resultsName.getNameText());

        //FIXME is there default stamp info for a Result entity?
        StampEntity stampEntity = transaction.getStamp(
                State.fromConceptNid(TinkarTerm.ACTIVE_STATE.nid()), // default to active
                System.currentTimeMillis(),
                TinkarTerm.USER.nid(),
                -1, // have no idea what the NID would be here
                TinkarTerm.DEVELOPMENT_PATH.nid()); // default to dev path???

        ConceptEntityBuilder newConceptBuilder = ConceptEntityBuilder.builder(stampEntity);

        PublicId conceptPublicId = PublicIds.newRandom();
        ConceptRecord conceptRecord = ConceptRecord.build(conceptPublicId.asUuidList().get(0), stampEntity.lastVersion());

        ConceptFacade conceptFacade = ConceptFacade.make(conceptRecord.nid());

        // commit the transaction
        CommitTransactionTask commitTransactionTask = new CommitTransactionTask(transaction);
        TinkExecutor.threadPool().submit(commitTransactionTask);

        LOG.info("transaction complete");
        return true;
    }

}
