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

import dev.ikm.komet.terms.KometTerm;
import dev.ikm.komet.kview.data.schema.STAMPDetail;
import dev.ikm.komet.kview.data.schema.SemanticDetail;
import dev.ikm.komet.kview.data.persistence.ConceptWriter;
import dev.ikm.komet.kview.data.persistence.STAMPWriter;
import dev.ikm.komet.kview.data.persistence.SemanticWriter;
import dev.ikm.komet.kview.lidr.mvvm.model.LidrRecord;
import dev.ikm.komet.kview.lidr.mvvm.model.DataModelHelper;
import dev.ikm.tinkar.common.id.IntIdSet;
import dev.ikm.tinkar.common.id.IntIds;
import dev.ikm.tinkar.common.id.PublicId;
import dev.ikm.tinkar.common.id.PublicIds;
import dev.ikm.tinkar.component.Concept;
import dev.ikm.tinkar.entity.Entity;
import dev.ikm.tinkar.entity.EntityHandle;
import dev.ikm.tinkar.entity.EntityVersion;
import dev.ikm.tinkar.entity.graph.adaptor.axiom.LogicalExpression;
import dev.ikm.tinkar.entity.graph.adaptor.axiom.LogicalExpressionBuilder;
import dev.ikm.tinkar.terms.EntityProxy;
import dev.ikm.tinkar.terms.EntityFacade;
import dev.ikm.tinkar.terms.State;
import dev.ikm.tinkar.terms.KernelTerm;
import org.carlfx.cognitive.validator.ValidationMessage;
import org.carlfx.cognitive.viewmodel.ValidationViewModel;
import org.eclipse.collections.api.factory.Lists;
import org.eclipse.collections.api.list.MutableList;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.*;
import java.util.function.Supplier;

import static dev.ikm.komet.kview.lidr.mvvm.model.DataModelHelper.*;
import static dev.ikm.komet.kview.lidr.mvvm.viewmodel.ResultsViewModel.*;
import static dev.ikm.komet.kview.mvvm.viewmodel.StampViewModel.*;
import static dev.ikm.tinkar.coordinate.stamp.StampFields.*;


public class ViewModelHelper {
    // VIEW_PROPERTIES moved to dev.ikm.komet.kview.mvvm.viewmodel.ViewModelKey.

    private static final Logger LOG = LoggerFactory.getLogger(ViewModelHelper.class);
    // TODO: Access LIDR PublicIds in a more maintainable way

    // Result conformance & OWL Expression values;
    public static final EntityProxy.Concept LOINC_PROPERTY = EntityProxy.Concept.make(PublicIds.of(UUID.fromString("066462e2-f926-35d5-884a-4e276dad4c2c")));
    public static final EntityProxy.Concept LOINC_SCALE    = EntityProxy.Concept.make(PublicIds.of(UUID.fromString("087afdd2-23cd-34c3-93a4-09088dfd480c")));
    public static final EntityProxy.Concept LOINC_ACNC     = EntityProxy.Concept.make(PublicIds.of(UUID.fromString("86939da1-1f1f-3d56-93f0-15f03439b338")));
    public static final String LOINC_QN_UUID                = "[6b8c30c5-63d7-3614-a675-2b5d03c541f4]";



    public static String findDescrNameText(PublicId publicId) {
        return findDescrNameText(publicId, "");
    }
    @SuppressWarnings("removal")
    public static String findDescrNameText(PublicId publicId, String defaultValue) {
        if (publicId == null) return defaultValue;
        Optional<Entity<? extends EntityVersion>> entity = EntityHandle.get(publicId.asUuidArray()).entity();
        Optional<String> stringOptional = DataModelHelper.viewPropertiesNode().calculator().getFullyQualifiedNameText(entity.get().nid());
        return stringOptional.orElse(defaultValue);
    }

    /**
     * Creates Semantic record as a LIDR Record into the database. If user doesn't specify a module or path the code will
     * use default values. Also, the time will be the current time.
     *
     * Note: Stamp Values will be altered depending on defaults and current time.
     * @param lidrRecord valid Lidr information to be written as a semantic record
     * @param device device concept's public id
     * @param stampViewModel the
     * @return
     */
    public static PublicId addNewLidrRecord(LidrRecord lidrRecord, PublicId device, ValidationViewModel stampViewModel) {
        if (device == null || lidrRecord == null || stampViewModel == null) {
            throw new RuntimeException("Error Unable to create a LIDR record to the database. lidr record = " + lidrRecord);
        }
        // Generate a new Stamp / with a new time.
        STAMPDetail stampDetail = toStampDetail(stampViewModel).with(System.currentTimeMillis());

        // Create a stamp into the database.
        PublicId newStampPublicId = PublicIds.newRandom();
        STAMPWriter stampWriter = new STAMPWriter(newStampPublicId);
        stampWriter.write(stampDetail);

        // Lidr record is written to database. It needs a device and stamp entity.
        return DataModelHelper.write(lidrRecord, device, newStampPublicId);
    }

    /**
     * Copy validate StampViewModel model values and create a STAMPDetail object ready for writers.
     * Note: Validation occurs but will only log the errors.
     * @param stampViewModel Lidr viewer and Concept windows has StampViewModels to accept input from the user.
     * @return STAMPDetail object containing a long for time (epoch millis) and public ids of Status, Author, Module, Path.
     */
    public static STAMPDetail toStampDetail(ValidationViewModel stampViewModel) {
        stampViewModel.save();
        if (stampViewModel.hasErrorMsgs()) {
            StringBuilder sb = new StringBuilder();
            for(ValidationMessage message: stampViewModel.getValidationMessages()) {
                sb.append(message.interpolate(stampViewModel) + "\n");
            }
            LOG.error("Error(s) with validation message(s)\n" + sb);
        }
        State state = stampViewModel.getValue(STATUS);
        PublicId statusPublicId = state != null ? state.publicId() : KernelTerm.ACTIVE_STATE.publicId();
        Concept author = stampViewModel.getValue(AUTHOR);
        PublicId authorPublicId = author != null ? author.publicId() : KernelTerm.USER.publicId();
        Long time = stampViewModel.getValue(TIME);
        long epochMillis = time == null ? System.currentTimeMillis() : time; // This may change due to when the actual record is written.
        Concept module = stampViewModel.getValue(MODULE);
        PublicId modulePublicId = module != null ? module.publicId() : KometTerm.DEVELOPMENT_MODULE.publicId();
        Concept path = stampViewModel.getValue(PATH);
        PublicId pathPublicId = path != null ? path.publicId() : KernelTerm.DEVELOPMENT_PATH.publicId();

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

        // Create Result Concept
        UUID resultUuid = UUID.randomUUID();
        PublicId resultPublicId = PublicIds.of(resultUuid);
        ConceptWriter conceptWriter = new ConceptWriter(newStampPublicId);
        conceptWriter.write(resultPublicId);

        // Create Fully q name semantic
        PublicId fqnDescrSemantic = PublicIds.newRandom();
        SemanticWriter descrSemantic = new SemanticWriter(newStampPublicId);
        descrSemantic.description(fqnDescrSemantic, resultPublicId, FQN_DESCR_CONCEPT.publicId(), resultName);

        // Identifier
        PublicId identifierUUID = PublicIds.newRandom();
        descrSemantic.identifier(identifierUUID, resultPublicId, UUID_CONCEPT.publicId(), resultUuid.toString());

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

        // Add Axiom Semantic
        SemanticWriter axiomSemantic = new SemanticWriter(newStampPublicId);
        PublicId newAxiomId = PublicIds.newRandom();
        axiomSemantic.semantic(newAxiomId,
                new SemanticDetail(
                        KernelTerm.EL_PLUS_PLUS_STATED_AXIOMS_PATTERN,
                        resultPublicId,
                        () -> {
                            MutableList<Object> semanticFields = Lists.mutable.empty();
                            semanticFields.add(resultConformanceDefinition(scaleType).sourceGraph());
                            return semanticFields;
                        })
        );
        return resultPublicId;
    }
    /**
     * The stated definition of a result-conformance concept: sufficient set of the
     * result-conformance concept with two role groups, one for the LOINC property
     * (always ACNC) and one for the LOINC scale.
     */
    static LogicalExpression resultConformanceDefinition(EntityFacade scaleType) {
        LogicalExpressionBuilder leb = new LogicalExpressionBuilder();
        leb.SufficientSet(leb.And(
                leb.ConceptAxiom(RESULT_CONFORMANCE_CONCEPT),
                leb.SomeRole(KernelTerm.ROLE_GROUP, leb.And(leb.SomeRole(LOINC_PROPERTY, leb.ConceptAxiom(LOINC_ACNC)))),
                leb.SomeRole(KernelTerm.ROLE_GROUP, leb.And(leb.SomeRole(LOINC_SCALE, leb.ConceptAxiom(scaleType.nid()))))));
        return leb.build();
    }
    public static PublicId createQuanitativeResultConcept(ResultsViewModel resultsViewModel, STAMPDetail stampDetail) {
        PublicId resultPublicId = PublicIds.newRandom();

        String resultName = resultsViewModel.getValue(RESULTS_NAME);
        EntityFacade scaleType = resultsViewModel.getValue(SCALE_TYPE);
        EntityFacade dataResultType = resultsViewModel.getValue(DATA_RESULTS_TYPE);

        return resultPublicId;

    }
}
