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

import dev.ikm.komet.amplify.data.om.STAMPDetail;
import dev.ikm.komet.amplify.data.om.SemanticDetail;
import dev.ikm.komet.amplify.data.persistence.ConceptWriter;
import dev.ikm.komet.amplify.data.persistence.STAMPWriter;
import dev.ikm.komet.amplify.data.persistence.SemanticWriter;
import dev.ikm.komet.amplify.lidr.om.LidrRecord;
import dev.ikm.komet.amplify.mvvm.ValidationViewModel;
import dev.ikm.komet.amplify.mvvm.validator.ValidationMessage;
import dev.ikm.tinkar.common.id.IntIdSet;
import dev.ikm.tinkar.common.id.IntIds;
import dev.ikm.tinkar.common.id.PublicId;
import dev.ikm.tinkar.common.id.PublicIds;
import dev.ikm.tinkar.component.Concept;
import dev.ikm.tinkar.entity.Entity;
import dev.ikm.tinkar.entity.EntityService;
import dev.ikm.tinkar.terms.EntityFacade;
import dev.ikm.tinkar.terms.State;
import dev.ikm.tinkar.terms.TinkarTerm;
import org.eclipse.collections.api.factory.Lists;
import org.eclipse.collections.api.list.MutableList;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;
import java.util.Optional;
import java.util.function.Supplier;

import static dev.ikm.komet.amplify.lidr.om.DataModelHelper.*;
import static dev.ikm.komet.amplify.lidr.viewmodels.ResultsViewModel.*;
import static dev.ikm.komet.amplify.viewmodels.StampViewModel.*;


public class ViewModelHelper {
    public static final String VIEW_PROPERTIES = "viewProperties";

    private static final Logger LOG = LoggerFactory.getLogger(ViewModelHelper.class);
    // TODO: Access LIDR PublicIds in a more maintainable way



    public static String findDescrNameText(PublicId publicId) {
        return findDescrNameText(publicId, "");
    }
    public static String findDescrNameText(PublicId publicId, String defaultValue) {
        if (publicId == null) return defaultValue;
        Optional<Entity> entity = EntityService.get().getEntity(publicId.asUuidArray());
        Optional<String> stringOptional = viewPropertiesNode().calculator().getRegularDescriptionText(entity.get().nid());
        return stringOptional.orElse(defaultValue);
    }

    public static PublicId addNewLidrRecord(LidrRecord lidrRecord, PublicId device, ValidationViewModel stampViewModel) {
        if (device == null || lidrRecord == null || stampViewModel == null) {
            throw new RuntimeException("Error Unable to create a LIDR record to the database. lidr record = " + lidrRecord);
        }
        // Generate a new Stamp
        STAMPDetail stampDetail = toStampDetail(stampViewModel);

        // Create a stamp into the database.
        PublicId newStampPublicId = PublicIds.newRandom();
        STAMPWriter stampWriter = new STAMPWriter(newStampPublicId);
        stampWriter.write(stampDetail);

        // Lidr record is written to database. It needs a device and stamp entity.
        return write(lidrRecord, device, newStampPublicId);
    }

    public static STAMPDetail toStampDetail(ValidationViewModel stampViewModel) {
        stampViewModel.save();
        if (stampViewModel.hasErrorMsgs()) {
            StringBuilder sb = new StringBuilder();
            for(ValidationMessage message: stampViewModel.getValidationMessages()) {
                sb.append(message.interpolate(stampViewModel) + "\n");
            }
            throw new RuntimeException("Error(s) with validation message(s)\n" + sb);
        }
        State status = stampViewModel.getValue(STATUS_PROPERTY);
        PublicId statusPublicId = status != null ? status.publicId() : TinkarTerm.ACTIVE_STATE.publicId();
        Concept author = stampViewModel.getValue(AUTHOR_PROPERTY);
        PublicId authorPublicId = author != null ? author.publicId() : TinkarTerm.USER.publicId();
        Long time = stampViewModel.getValue(TIME_PROPERTY);
        long epochMillis = time == null ? System.currentTimeMillis() : time;
        Concept module = stampViewModel.getValue(MODULE_PROPERTY);
        PublicId modulePublicId = module != null ? module.publicId() : TinkarTerm.DEVELOPMENT_MODULE.publicId();
        Concept path = stampViewModel.getValue(PATH_PROPERTY);
        PublicId pathPublicId = path != null ? path.publicId() : TinkarTerm.DEVELOPMENT_PATH.publicId();

        return new STAMPDetail(statusPublicId, epochMillis, authorPublicId, modulePublicId, pathPublicId);

    }

    public static PublicId createQualitativeResultConcept(ResultsViewModel resultsViewModel, STAMPDetail stampDetail) {

        String resultName = resultsViewModel.getValue(RESULTS_NAME);
        EntityFacade scaleType = resultsViewModel.getValue(SCALE_TYPE);
        EntityFacade dataResultType = resultsViewModel.getValue(DATA_RESULTS_TYPE);
        List<EntityFacade> allowableResults = resultsViewModel.getList(ALLOWABLE_RESULTS);
        //Creating a Result conformance (Concept)
        // 1. descrip Semantic (done), identifier
        // 2. result conformance semantic.
        //   a. qualitative pattern
        //   b. quantitative pattern
        // 3. property and scale Axiom section.

        // Create a stamp into the database.
        PublicId newStampPublicId = PublicIds.newRandom();
        STAMPWriter stampWriter = new STAMPWriter(newStampPublicId);
        stampWriter.write(stampDetail);

        // Create Concept
        PublicId resultPublicId = PublicIds.newRandom();
        ConceptWriter conceptWriter = new ConceptWriter(newStampPublicId);
        conceptWriter.write(resultPublicId);

        // Create Fully q name semantic
        PublicId fqnDescrSemantic = PublicIds.newRandom();
        SemanticWriter descrSemantic = new SemanticWriter(newStampPublicId);
        descrSemantic.description(fqnDescrSemantic, resultPublicId, FQN_DESCR_CONCEPT.publicId(), resultName);

        // Identifier
        PublicId identifierUUID = PublicIds.newRandom();
        descrSemantic.identifier(identifierUUID, resultPublicId, UUID_CONCEPT.publicId(), resultPublicId.asUuidArray()[0].toString());

        // Result Conformance Semantic has pattern of
        SemanticWriter resultConformanceSemantic = new SemanticWriter(newStampPublicId);
        PublicId resultConformanceSemanticId = PublicIds.newRandom();


        Supplier<MutableList<Object>> fieldsSupplier = () -> {
            // Allowable Results. Such as detected or not detected
            IntIdSet allowableResultsNids = allowableResults.size() == 0 ? IntIds.set.empty() : IntIds.set.of(allowableResults,
                    (entityFacade) -> entityFacade.nid());

            // Create pattern's field definitions
            MutableList<Object> allowableFields = Lists.mutable.empty();
            allowableFields.add(allowableResultsNids);
            return allowableFields;
        };
        resultConformanceSemantic.semantic(resultConformanceSemanticId, new SemanticDetail(ALLOWED_RESULTS_PATTERN.publicId(), resultPublicId, fieldsSupplier));

        //////////////// TODO Fix issue getting a null pointer exception
        // Create a stated Axiom description logic
//        PublicId statedAxiomId = PublicIds.newRandom();
//        resultConformanceSemantic.statedAxiom(statedAxiomId, resultPublicId, List.of());
//

        return resultPublicId;
    }

    public static PublicId createQuanitativeResultConcept(ResultsViewModel resultsViewModel, STAMPDetail stampDetail) {
        PublicId resultPublicId = PublicIds.newRandom();

        String resultName = resultsViewModel.getValue(RESULTS_NAME);
        EntityFacade scaleType = resultsViewModel.getValue(SCALE_TYPE);
        EntityFacade dataResultType = resultsViewModel.getValue(DATA_RESULTS_TYPE);

        return resultPublicId;

    }
}
