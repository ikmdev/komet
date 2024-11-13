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
import dev.ikm.tinkar.entity.EntityService;
import dev.ikm.tinkar.entity.graph.adaptor.axiom.LogicalExpression;
import dev.ikm.tinkar.ext.lang.owl.SctOwlUtilities;
import dev.ikm.tinkar.terms.EntityFacade;
import dev.ikm.tinkar.terms.State;
import dev.ikm.tinkar.terms.TinkarTerm;
import org.carlfx.cognitive.validator.ValidationMessage;
import org.carlfx.cognitive.viewmodel.ValidationViewModel;
import org.eclipse.collections.api.factory.Lists;
import org.eclipse.collections.api.list.MutableList;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.util.*;
import java.util.function.Function;
import java.util.function.Supplier;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import static dev.ikm.komet.kview.lidr.mvvm.model.DataModelHelper.*;
import static dev.ikm.komet.kview.lidr.mvvm.viewmodel.ResultsViewModel.*;
import static dev.ikm.komet.kview.mvvm.viewmodel.StampViewModel.*;
import static dev.ikm.tinkar.coordinate.stamp.StampFields.*;


public class ViewModelHelper {
    public static final String VIEW_PROPERTIES = "viewProperties";

    private static final Logger LOG = LoggerFactory.getLogger(ViewModelHelper.class);
    // TODO: Access LIDR PublicIds in a more maintainable way

    // Result conformance & OWL Expression values;
    public static final String ROLE_GROUP_PUBLICID_STRING   = "[" + TinkarTerm.ROLE_GROUP.publicId().asUuidArray()[0] + "]";
    public static final String LOINC_PROPERTY_UUID          = "[066462e2-f926-35d5-884a-4e276dad4c2c]";
    public static final String LOINC_SCALE_UUID             = "[087afdd2-23cd-34c3-93a4-09088dfd480c]";
    public static final String LOINC_ACNC_UUID              = "[86939da1-1f1f-3d56-93f0-15f03439b338]";
    public static final String LOINC_QN_UUID                = "[6b8c30c5-63d7-3614-a675-2b5d03c541f4]";



    public static String findDescrNameText(PublicId publicId) {
        return findDescrNameText(publicId, "");
    }
    public static String findDescrNameText(PublicId publicId, String defaultValue) {
        if (publicId == null) return defaultValue;
        Optional<Entity> entity = EntityService.get().getEntity(publicId.asUuidArray());
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
        PublicId statusPublicId = state != null ? state.publicId() : TinkarTerm.ACTIVE_STATE.publicId();
        Concept author = stampViewModel.getValue(AUTHOR);
        PublicId authorPublicId = author != null ? author.publicId() : TinkarTerm.USER.publicId();
        Long time = stampViewModel.getValue(TIME);
        long epochMillis = time == null ? System.currentTimeMillis() : time; // This may change due to when the actual record is written.
        Concept module = stampViewModel.getValue(MODULE);
        PublicId modulePublicId = module != null ? module.publicId() : TinkarTerm.DEVELOPMENT_MODULE.publicId();
        Concept path = stampViewModel.getValue(PATH);
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

        // Create Result Concept
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

        // Add Axiom Semantic
        SemanticWriter axiomSemantic = new SemanticWriter(newStampPublicId);
        PublicId newAxiomId = PublicIds.newRandom();
        axiomSemantic.semantic(newAxiomId,
                new SemanticDetail(
                        TinkarTerm.EL_PLUS_PLUS_STATED_AXIOMS_PATTERN,
                        resultPublicId,
                        () -> {
                            MutableList<Object> semanticFields = Lists.mutable.empty();
                            String owlExpression = generateOwlResultConformanceExpression(generateResultConformanceValueMap(resultPublicId, scaleType.publicId()));
                            //Build DiTree
                            try {
                                LogicalExpression expression = SctOwlUtilities.sctToLogicalExpression(owlExpression, "");
                                semanticFields.add(expression.sourceGraph());
                                System.out.println(expression);
                            } catch (IOException e) {
                                throw new RuntimeException(e);
                            }
                            return semanticFields;
                        })
        );
        return resultPublicId;
    }
    static Pattern VARIABLE_PATTERN = Pattern.compile("\\$\\{([a-zA-Z\\d\\_]+)\\}");


    public static String interpolateTemplate(String template, Map<String, String> map) {
        Matcher matcher = VARIABLE_PATTERN.matcher(template);
        List<String> props = new ArrayList<>();
        String newMessage = template;
        while (matcher.find()) {
            String subPropName = matcher.group();
            String propName = matcher.group(1);
            props.add(matcher.group());
            String friendlyName = map.get(propName);
            if (friendlyName != null) {
                newMessage = newMessage.replace(subPropName, friendlyName);
            }
        }
        return newMessage!= null ? newMessage : "";
    }
    public static Map<String, String> generateResultConformanceValueMap(PublicId createdConceptID, PublicId scaleTypeId) {
        Map<String, String> valueMap = new HashMap<>();
        Function<String, String>  removeQuotationMarks = (conceptID) -> conceptID.replaceAll("\"","");

        valueMap.put("createdConceptID",            removeQuotationMarks.apply(createdConceptID.idString())); // :[uuid]
        valueMap.put("resultConformancePublicId",   removeQuotationMarks.apply(RESULT_CONFORMANCE_CONCEPT.publicId().idString())); // conceptTypeToPublicIdMap.get("Result Conformance Concept")
        valueMap.put("roleGroupPublicIDstring",     ROLE_GROUP_PUBLICID_STRING);          //
        valueMap.put("loincPropertyUuid",           LOINC_PROPERTY_UUID);                 // LOINC_PROPERTY_UUID
        valueMap.put("propertyPublicId",            LOINC_ACNC_UUID); // conceptTypeToPublicIdMap.get("Property")
        valueMap.put("loincScaleUuid",              LOINC_SCALE_UUID);                    // LOINC_SCALE_UUID
        valueMap.put("scalePublicId",               removeQuotationMarks.apply(scaleTypeId.idString()));
        return valueMap;
    }
    public static String generateOwlResultConformanceExpression(Map<String, String> valueMap) {
        String owlExpressionString = """
                EquivalentClasses(:${createdConceptID} 
                   ObjectIntersectionOf(:${resultConformancePublicId} 
                      ObjectSomeValuesFrom(:${roleGroupPublicIDstring}
                         ObjectSomeValuesFrom(
                            :${loincPropertyUuid} 
                            :${propertyPublicId}
                         )
                      )
                      ObjectSomeValuesFrom(:${roleGroupPublicIDstring}
                         ObjectSomeValuesFrom(
                            :${loincScaleUuid}
                            :${scalePublicId}
                         )
                      )
                   )
                )
                ))))
                """;
        return interpolateTemplate(owlExpressionString, valueMap);
    }
    public static PublicId createQuanitativeResultConcept(ResultsViewModel resultsViewModel, STAMPDetail stampDetail) {
        PublicId resultPublicId = PublicIds.newRandom();

        String resultName = resultsViewModel.getValue(RESULTS_NAME);
        EntityFacade scaleType = resultsViewModel.getValue(SCALE_TYPE);
        EntityFacade dataResultType = resultsViewModel.getValue(DATA_RESULTS_TYPE);

        return resultPublicId;

    }
}
