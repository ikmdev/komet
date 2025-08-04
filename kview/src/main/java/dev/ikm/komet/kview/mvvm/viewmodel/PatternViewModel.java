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
package dev.ikm.komet.kview.mvvm.viewmodel;

import static dev.ikm.tinkar.common.service.PrimitiveData.PREMUNDANE_TIME;
import static dev.ikm.tinkar.coordinate.stamp.StampFields.AUTHOR;
import static dev.ikm.tinkar.coordinate.stamp.StampFields.MODULE;
import static dev.ikm.tinkar.coordinate.stamp.StampFields.PATH;
import static dev.ikm.tinkar.coordinate.stamp.StampFields.STATUS;
import static dev.ikm.tinkar.coordinate.stamp.StampFields.TIME;
import static dev.ikm.tinkar.terms.EntityProxy.Pattern;
import static dev.ikm.tinkar.terms.TinkarTerm.ACCEPTABLE;
import static dev.ikm.tinkar.terms.TinkarTerm.DEFINITION_DESCRIPTION_TYPE;
import static dev.ikm.tinkar.terms.TinkarTerm.REGULAR_NAME_DESCRIPTION_TYPE;
import dev.ikm.komet.framework.view.ViewProperties;
import dev.ikm.komet.kview.mvvm.model.DescrName;
import dev.ikm.komet.kview.mvvm.model.PatternDefinition;
import dev.ikm.komet.kview.mvvm.model.PatternField;
import dev.ikm.tinkar.common.id.PublicId;
import dev.ikm.tinkar.common.id.PublicIds;
import dev.ikm.tinkar.component.Stamp;
import dev.ikm.tinkar.composer.Composer;
import dev.ikm.tinkar.composer.Session;
import dev.ikm.tinkar.composer.assembler.PatternAssembler;
import dev.ikm.tinkar.composer.template.FullyQualifiedName;
import dev.ikm.tinkar.composer.template.Synonym;
import dev.ikm.tinkar.composer.template.USDialect;
import dev.ikm.tinkar.coordinate.stamp.calculator.Latest;
import dev.ikm.tinkar.coordinate.stamp.calculator.StampCalculator;
import dev.ikm.tinkar.coordinate.view.calculator.ViewCalculator;
import dev.ikm.tinkar.entity.ConceptEntity;
import dev.ikm.tinkar.entity.ConceptRecord;
import dev.ikm.tinkar.entity.Entity;
import dev.ikm.tinkar.entity.EntityService;
import dev.ikm.tinkar.entity.EntityVersion;
import dev.ikm.tinkar.entity.FieldDefinitionRecord;
import dev.ikm.tinkar.entity.PatternVersionRecord;
import dev.ikm.tinkar.entity.SemanticEntity;
import dev.ikm.tinkar.entity.SemanticEntityVersion;
import dev.ikm.tinkar.terms.ConceptFacade;
import dev.ikm.tinkar.terms.EntityFacade;
import dev.ikm.tinkar.terms.EntityProxy;
import dev.ikm.tinkar.terms.PatternFacade;
import dev.ikm.tinkar.terms.State;
import dev.ikm.tinkar.terms.TinkarTerm;
import javafx.beans.property.BooleanProperty;
import javafx.beans.property.ObjectProperty;
import javafx.collections.ObservableList;
import org.carlfx.axonic.StateMachine;
import org.carlfx.cognitive.validator.ValidationMessage;
import org.carlfx.cognitive.validator.ValidationResult;
import org.carlfx.cognitive.viewmodel.ViewModel;
import org.eclipse.collections.api.list.ImmutableList;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.time.Instant;
import java.time.LocalDate;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;

public class PatternViewModel extends FormViewModel {

    private static final Logger LOG = LoggerFactory.getLogger(PatternViewModel.class);


    // --------------------------------------------
    // Known properties
    // --------------------------------------------
    public static String STAMP_VIEW_MODEL = "stampViewModel";

    public static String STATE_MACHINE = "stateMachine";

    public static String FQN_DESCRIPTION_NAME = "fqnDescriptionName";

    public static String FQN_DATE_ADDED_STR = "fqnDateAddedStr";

    public static String FQN_CASE_SIGNIFICANCE = "fqnCaseSignificance";

    public static String FQN_LANGUAGE = "fqnLanguage";

    public static String OTHER_NAMES = "otherDescriptionNames";

    //TODO Need to refactor this to use only this variable instead of OTHER_NAMES.
    // This is used during editing only for now to map the DescrName class with SEMANTIC_VERSIONS.
    public static String OTHER_NAME_SEMANTIC_VERSION_MAP = "regularNames";

    public static String PURPOSE_ENTITY = "purposeEntity";

    public static String MEANING_ENTITY = "meaningEntity";

    public static String PATTERN_TOPIC = "patternTopic";

    public static String PURPOSE_TEXT = "purposeText";

    public static String MEANING_TEXT = "meaningText";

    public static String FQN_DESCRIPTION_NAME_TEXT = "fqnDescrNameText";

    public static String FIELDS_COLLECTION = "fieldsCollection";

    public static String PURPOSE_DATE_STR = "purposeDateStr";

    public static String MEANING_DATE_STR = "meaningDateStr";

    public static String PATTERN = "pattern";

    public static String FQN_PROXY = "fqnProxy";

    public static String HAS_CHANGED = "hasChanged";

    public static String IS_INVALID = "IS_INVALID";

    public static String PATTERN_TITLE_TEXT = "patternTitleText";

    public static String PUBLISH_PENDING = "publishPending";

    // Used to load the values in the PatternField controller from PatternDetailsController.
    public static String SELECTED_PATTERN_FIELD = "selectedPatternField";

    private int changeHash;

    // key is the string of the publicId of the semantic, and value is the hashCode of the other name
    private Map<String, Integer> baselineOtherNameHashMap = new HashMap<>();

    public PatternViewModel() {
        super();
            addProperty(VIEW_PROPERTIES, (ViewProperties) null)
                    .addProperty(PATTERN_TITLE_TEXT, "")
                    .addProperty(PATTERN_TOPIC, (UUID) null)
                    .addProperty(STATE_MACHINE, (StateMachine) null)
                    .addProperty(STAMP_VIEW_MODEL, (ViewModel) null)
                    .addProperty(FQN_DESCRIPTION_NAME, (DescrName) null)
                    .addProperty(OTHER_NAMES, new ArrayList<DescrName>())
                    .addProperty(OTHER_NAME_SEMANTIC_VERSION_MAP, new HashMap<DescrName, SemanticEntityVersion>())
                    // PATTERN>DEFINITION Purpose and Meaning
                    .addProperty(PURPOSE_ENTITY, (EntityFacade) null) // this is/will be the 'purpose' concept entity
                    .addProperty(MEANING_ENTITY, (EntityFacade) null) // this is/will be the 'meaning' concept entity
                    .addProperty(PURPOSE_TEXT, "")
                    .addProperty(MEANING_TEXT, "")
                    .addProperty(PURPOSE_DATE_STR, "")
                    .addProperty(MEANING_DATE_STR, "")
                    .addProperty(PUBLISH_PENDING,false)
                    // PATTERN>DESCRIPTION FQN and Other Name
                    .addProperty(FQN_DESCRIPTION_NAME_TEXT, "")
                    // Ordered collection of Fields
                    .addProperty(FIELDS_COLLECTION, new ArrayList<PatternField>())
                    .addProperty(SELECTED_PATTERN_FIELD, (PatternField) null)
                    .addProperty(IS_INVALID, true)
                    .addProperty(PATTERN, (EntityFacade) null) // once saved, this is the pattern facade
                    .addProperty(FQN_PROXY, (EntityProxy.Semantic) null)
                    .addProperty(HAS_CHANGED, false)
                    .addValidator(IS_INVALID, "Is Invalid", (ValidationResult vr, ViewModel viewModel) -> {
                        ObjectProperty<EntityFacade> purposeEntity = viewModel.getProperty(PURPOSE_ENTITY);
                        ObjectProperty<EntityFacade> meaningEntity = viewModel.getProperty(MEANING_ENTITY);
                        ObjectProperty<DescrName> fqnProperty = viewModel.getProperty(FQN_DESCRIPTION_NAME);
                        ViewModel stampViewModel = viewModel.getPropertyValue(STAMP_VIEW_MODEL);
                        ObjectProperty<?> stampModule = stampViewModel.getProperty(MODULE);
                        ObjectProperty<?> stampPath = stampViewModel.getProperty(PATH);
                        ObjectProperty<?> stampStatus = stampViewModel.getProperty(STATUS);
                        // reset the error list on each validation check
                        vr.getMessages().clear();
                        if (purposeEntity.isNull().get()) {
                            vr.error("A purpose is required for a Pattern.  Please add a purpose.");
                        }
                        if (meaningEntity.isNull().get()) {
                            vr.error("A meaning is required for a Pattern.  Please add a meaning.");
                        }
                        if (fqnProperty.isNull().get()) {
                            vr.error("A fully qualified name is required for a Pattern.  Please add a fully qualified name.");
                        }
                        if (stampModule.isNull().get() || stampPath.isNull().get() || stampStatus.isNull().get()) {
                            vr.error("STAMP values are required.  Please fill out the STAMP information.");
                        }
                        viewModel.setPropertyValue(IS_INVALID, !vr.getMessages().isEmpty());
                    });
    }

    public void setPurposeAndMeaningText(PatternDefinition patternDefinition) {
        setPropertyValue(PURPOSE_ENTITY, patternDefinition.purpose());
        setPropertyValue(MEANING_ENTITY, patternDefinition.meaning());

        String dateAddedStr = LocalDate.now().format(DateTimeFormatter.ofPattern("MMM d, yyyy")).toString();
        if (patternDefinition.meaning() != null) {
            setPropertyValue(MEANING_DATE_STR, "Date Added: " + dateAddedStr);
        }
        if (patternDefinition.purpose() != null) {
            setPropertyValue(PURPOSE_DATE_STR, "Date Added: " + dateAddedStr);
        }

        EntityFacade purposeFacade = getPropertyValue(PURPOSE_ENTITY);
        EntityFacade meaningFacade = getPropertyValue(MEANING_ENTITY);

        if (purposeFacade != null) {
            setPropertyValue(PURPOSE_TEXT, purposeFacade.description());
        }
        if (meaningFacade != null) {
            setPropertyValue(MEANING_TEXT, meaningFacade.description());
        }
        setPropertyValue(PUBLISH_PENDING, true);
    }

    public void reLoadPatternValues(){
        ObservableList<PatternField> patternFieldObsList = getObservableList(FIELDS_COLLECTION);
        patternFieldObsList.clear();
        ObservableList<DescrName> otherNamesList = getObservableList(OTHER_NAMES);
        otherNamesList.clear();
        loadPatternValues();
    }

    public void loadPatternValues(){
        ObjectProperty<EntityFacade> patternProperty = getProperty(PATTERN);
        EntityFacade patternFacade = patternProperty.getValue();
        if (patternFacade != null && getPropertyValue(MODE).equals(EDIT)) {
            Entity entity = EntityService.get().getEntityFast(patternFacade);
            ViewCalculator viewCalculator = getViewProperties().calculator();

            // Load Fields data.
            ObservableList<PatternField> patternFieldObsList = getObservableList(FIELDS_COLLECTION);
            PatternVersionRecord patternVersionRecord = (PatternVersionRecord) viewCalculator.latest(entity).get();
            ImmutableList<FieldDefinitionRecord> fieldDefinitionRecords = patternVersionRecord.fieldDefinitions();

            fieldDefinitionRecords.stream().forEachOrdered( fieldDefinitionForEntity ->
            {
                EntityVersion latest = (EntityVersion) viewCalculator.latest(fieldDefinitionForEntity.meaning()).get();
                PatternField patternField = new PatternField(fieldDefinitionForEntity.meaning().description(), fieldDefinitionForEntity.dataType(),
                        fieldDefinitionForEntity.purpose(), fieldDefinitionForEntity.meaning(), "", latest.stamp());
                patternFieldObsList.add(patternField);
            });

            // load Definition Semantics
            Latest<EntityVersion> latestEntityVersion = viewCalculator.latest(entity);
            EntityVersion entityVersion = latestEntityVersion.get();

            Entity purposeEntity = ((PatternVersionRecord) entityVersion).semanticPurpose();
            setPropertyValue(PURPOSE_ENTITY, purposeEntity);
            setPropertyValue(PURPOSE_TEXT, purposeEntity.description());

            Long purposeMilis = viewCalculator.latest(Entity.getFast(purposeEntity.nid())).get().stamp().time();
            if (purposeMilis.equals(PREMUNDANE_TIME)) {
                setPropertyValue(PURPOSE_DATE_STR, "Date Added: Premundane");
            } else {
                LocalDate purposeDate =
                        Instant.ofEpochMilli(purposeMilis).atZone(ZoneId.systemDefault()).toLocalDate();
                String purposeDateStr = purposeDate.format(DateTimeFormatter.ofPattern("MMM d, yyyy")).toString();
                setPropertyValue(PURPOSE_DATE_STR, "Date Added: " +  purposeDateStr);
            }

            Entity meaningEntity = ((PatternVersionRecord) entityVersion).semanticMeaning();
            setPropertyValue(MEANING_ENTITY, meaningEntity);
            setPropertyValue(MEANING_TEXT, meaningEntity.description());

            Long meaningMillis = viewCalculator.latest(Entity.getFast(meaningEntity.nid())).get().stamp().time();
            if (meaningMillis.equals(PREMUNDANE_TIME)) {
                setPropertyValue(MEANING_DATE_STR, "Date Added: Premundane");
            } else {
                LocalDate meaningDate =
                        Instant.ofEpochMilli(meaningMillis).atZone(ZoneId.systemDefault()).toLocalDate();
                String meaningDateStr = meaningDate.format(DateTimeFormatter.ofPattern("MMM d, yyyy")).toString();
                setPropertyValue(MEANING_DATE_STR, "Date Added: " + meaningDateStr);
            }
            String patternTitleText = retrieveDisplayName((PatternFacade) patternFacade);
            setPropertyValue(PATTERN_TITLE_TEXT, patternTitleText);

            loadFqnDetails(patternFacade);

            viewCalculator.forEachSemanticVersionForComponentOfPattern(entity.nid(), TinkarTerm.DESCRIPTION_PATTERN.nid(),
                (semanticEntityVersion,  entityVersion1, patternEntityVersion) -> {
                    ConceptFacade language = (ConceptFacade) semanticEntityVersion.fieldValues().get(0);
                    String nameText = (String) semanticEntityVersion.fieldValues().get(1);
                    ConceptFacade caseSignificance = (ConceptFacade) semanticEntityVersion.fieldValues().get(2);
                    ConceptFacade descriptionType = (ConceptFacade) semanticEntityVersion.fieldValues().get(3);
                    DescrName descrName = new DescrName(null, nameText, descriptionType,
                        Entity.getFast(caseSignificance.nid()), Entity.getFast(semanticEntityVersion.state().nid()),
                            Entity.getFast(semanticEntityVersion.module().nid()),Entity.getFast(language.nid()), semanticEntityVersion.publicId());
                if (PublicId.equals(descriptionType.publicId(), REGULAR_NAME_DESCRIPTION_TYPE.publicId())) {
                    ObservableList<DescrName> otherNamesList = getObservableList(OTHER_NAMES);
                    HashMap<DescrName, SemanticEntityVersion> regularNamesMap = getPropertyValue(OTHER_NAME_SEMANTIC_VERSION_MAP);
                    // add to list.
                    otherNamesList.add(descrName);
                    regularNamesMap.put(descrName, semanticEntityVersion);
                } else if (PublicId.equals(descriptionType.publicId(), DEFINITION_DESCRIPTION_TYPE.publicId())) {
                    LOG.info(" Add to Definition Name : " + descrName.getNameText());
                }
            });
            // regenerate with updated
            baselineOtherNameHashMap = generateOtherNameHash();
        }
    }

    private void loadFqnDetails(EntityFacade patternFacade) {
        //TODO - We might not want to use the contradictions collection in future.
        // Once a backend fix is done we will use different approach.
        /**
         * NOTE:
         * A contradiction implies a discrepancy. IN other parts of the code where we needed to sort thing based on time,
         * there are position records that can be sorted via a HashTree Collection object.
         * */
        SemanticEntityVersion fqnSemanticEntityVersion = getViewProperties().calculator().languageCalculator()
                .getFullyQualifiedDescription(patternFacade).getWithContradictions().getFirstOptional().get();

        ConceptFacade fqnLanguage = (ConceptFacade) fqnSemanticEntityVersion.fieldValues().get(0);
        String fqnString = (String) fqnSemanticEntityVersion.fieldValues().get(1);
        ConceptFacade fqnCaseSignificance = (ConceptFacade) fqnSemanticEntityVersion.fieldValues().get(2);
        ConceptFacade fqnDescriptionType = (ConceptFacade) fqnSemanticEntityVersion.fieldValues().get(3);
        DescrName fqnDescrName = new DescrName(null, fqnString, fqnDescriptionType,
                Entity.getFast(fqnCaseSignificance.nid()), Entity.getFast(fqnSemanticEntityVersion.state().nid()),
                Entity.getFast(fqnSemanticEntityVersion.module().nid()),Entity.getFast(fqnLanguage.nid()), fqnSemanticEntityVersion.publicId());
        setPropertyValue(FQN_DESCRIPTION_NAME, fqnDescrName);
        setPropertyValue(FQN_DESCRIPTION_NAME_TEXT, fqnString);
        setPropertyValue(FQN_CASE_SIGNIFICANCE, fqnCaseSignificance);
        setPropertyValue(FQN_LANGUAGE, fqnLanguage);
        setPropertyValue(FQN_PROXY, fqnSemanticEntityVersion.entity().toProxy());
        changeHash = generateFqnHash();
    }

    private String retrieveDisplayName(PatternFacade patternFacade) {
        ViewProperties viewProperties = getPropertyValue(VIEW_PROPERTIES);
        ViewCalculator viewCalculator = viewProperties.calculator();
        Optional<String> optionalStringRegularName = viewCalculator.getRegularDescriptionText(patternFacade);
        Optional<String> optionalStringFQN = viewCalculator.getFullyQualifiedNameText(patternFacade);
        return optionalStringRegularName.orElseGet(optionalStringFQN::get);
    }

    public boolean createPattern() {
        save();
        if (hasErrorMsgs()) {
            for (ValidationMessage validationMessage : getValidationMessages()) {
                LOG.error(validationMessage.toString());
            }
            return false;
        }
        PublicId patternPublicId =  getPropertyValue(PATTERN) == null ? PublicIds.newRandom():  ((PatternFacade) getPropertyValue(PATTERN)).publicId();
        Pattern pattern = Pattern.make(null, patternPublicId);

        Composer composer = new Composer("Save Pattern Definition");

        // get the STAMP values from the nested stampViewModel
        StampViewModel stampViewModel = getPropertyValue(STAMP_VIEW_MODEL);
        State state = stampViewModel.getPropertyValue(STATUS);

        Object authorObject = stampViewModel.getPropertyValue(AUTHOR);
        EntityProxy.Concept authorConcept = null;
        if (authorObject instanceof EntityProxy.Concept) {
            authorConcept = (EntityProxy.Concept) authorObject;
        } else if (authorObject instanceof ConceptRecord authorConceptRecord) {
            authorConcept = EntityProxy.Concept.make(authorConceptRecord.nid());
        }

        ConceptEntity module = stampViewModel.getPropertyValue(MODULE);
        ConceptEntity path = stampViewModel.getPropertyValue(PATH);
        Session session = composer.open(state, authorConcept, module.toProxy(), path.toProxy());
        EntityProxy.Concept conceptEntityMeaning = EntityProxy.Concept.make(((EntityFacade)getPropertyValue(MEANING_ENTITY)).nid());
        EntityProxy.Concept conceptEntityPurpose = EntityProxy.Concept.make(((EntityFacade)getPropertyValue(PURPOSE_ENTITY)).nid());

        // set up pattern with the fully qualified name
        ObservableList<PatternField> fieldsProperty = getObservableList(FIELDS_COLLECTION);

        // get the fqn semantic version
        ObjectProperty<EntityProxy.Semantic> fqnProp = getObjectProperty(FQN_PROXY);
        if (fqnProp.isNull().get()) {
            // if the FQN is empty, create a new one
            EntityProxy.Semantic fqnProxy = EntityProxy.Semantic.make(null, PublicIds.newRandom());
            fqnProp.set(fqnProxy);
        }

        if (getPropertyValue(PATTERN) == null) {
            // create pattern compose statement
            session.compose((PatternAssembler patternAssembler) -> {
                patternAssembler
                        .pattern(pattern)
                        .meaning(conceptEntityMeaning)
                        .purpose(conceptEntityPurpose)
                        .attach((FullyQualifiedName fqn) -> fqn
                            .semantic(fqnProp.get())
                            .language(((EntityFacade)getPropertyValue(FQN_LANGUAGE)).toProxy())
                            .text(getPropertyValue(FQN_DESCRIPTION_NAME_TEXT))
                            .caseSignificance(((EntityFacade)getPropertyValue(FQN_CASE_SIGNIFICANCE)).toProxy())
                            .attach(new USDialect().acceptability(ACCEPTABLE))
                );

                // add the field definitions
                for (int i = 0; i < fieldsProperty.size(); i++) {
                    PatternField patternField = fieldsProperty.get(i);
                    EntityProxy.Concept conceptEntityFieldMeaning = EntityProxy.Concept.make(patternField.meaning().nid());
                    EntityProxy.Concept conceptEntityFieldPurpose = EntityProxy.Concept.make(patternField.purpose().nid());
                    EntityProxy.Concept conceptEntityFieldDatatype = EntityProxy.Concept.make(patternField.dataType().nid());
                    patternAssembler.fieldDefinition(conceptEntityFieldMeaning, conceptEntityFieldPurpose, conceptEntityFieldDatatype, i);
                }
            });
        } else {
            /*
            only write a fqn version IF there is a change to
                - FQN language,
                - FQN case significance,
                - FQN text (description),
                - FQN status
                - path
                - module
             */
            if (generateFqnHash() != changeHash) {

                session.compose(new FullyQualifiedName()
                                .semantic(fqnProp.get())
                                .language(((EntityFacade) getPropertyValue(FQN_LANGUAGE)).toProxy())
                                .text(getPropertyValue(FQN_DESCRIPTION_NAME_TEXT))
                                .caseSignificance(((EntityFacade) getPropertyValue(FQN_CASE_SIGNIFICANCE)).toProxy()),
                        pattern
                );
            }
        }


        // add the other name description semantics if they exist
        ObservableList<DescrName> otherNamesProperty = getObservableList(OTHER_NAMES);
        boolean isEdit = getPropertyValue(MODE).equals("EDIT");
        Map<String, Integer> currentOtherNameMap = Map.of();
        if (isEdit) {
            currentOtherNameMap = generateOtherNameHash();
        }
        final Map<String, Integer> finalCurrentOtherNameMap = currentOtherNameMap;
        otherNamesProperty.forEach(otherName -> {
            Synonym synonym = new Synonym()
                    .language(otherName.getLanguage().toProxy())
                    .text(otherName.getNameText())
                    .caseSignificance(otherName.getCaseSignificance().toProxy());
            if (isEdit) {
                String otKey = otherName.getSemanticPublicId() != null ? otherName.getSemanticPublicId().idString() : "-not-found-";
                // if there is a CHANGE to the other name, then we allow the update
                if (!baselineOtherNameHashMap.containsKey(otKey) || !baselineOtherNameHashMap.get(otKey).equals(finalCurrentOtherNameMap.get(otKey))) {
                    HashMap<DescrName, SemanticEntityVersion> regularNamesMap = getPropertyValue(OTHER_NAME_SEMANTIC_VERSION_MAP);
                    if (regularNamesMap != null && regularNamesMap.get(otherName) != null) {
                        SemanticEntityVersion semanticEntityVersion = regularNamesMap.get(otherName); // get the right other name to edit
                        SemanticEntity<SemanticEntityVersion> semanticEntity = semanticEntityVersion.chronology();
                        synonym.semantic(semanticEntity.toProxy());
                        session.compose(synonym, pattern);
                    } else {
                        session.compose(synonym, pattern)
                                .attach(new USDialect().acceptability(ACCEPTABLE));
                    }
                }
            } else {
                session.compose(synonym, pattern)
                        .attach(new USDialect().acceptability(ACCEPTABLE));
            }

        });
        boolean isSuccess = composer.commitSession(session);

        // change hash code
        //TODO create the hash of the pattern and its values
        //changeHash = generateFqnHash();


        setPropertyValue(PATTERN, pattern);
        return isSuccess;
    }


    private int generateFqnHash() {
        StringBuilder stringBuilder = new StringBuilder();
        stringBuilder.append(((EntityFacade)getPropertyValue(FQN_LANGUAGE)).nid())
                .append("|")
                .append(((EntityFacade)getPropertyValue(FQN_CASE_SIGNIFICANCE)).nid())
                .append("|")
                .append(getStringProperty(FQN_DESCRIPTION_NAME_TEXT).get());
        return stringBuilder.toString().hashCode();
    }

    private Map<String, Integer> generateOtherNameHash() {
        ObservableList<DescrName> otherNamesProperty = getObservableList(OTHER_NAMES);
        Map<String, Integer> map = new HashMap<>();
        otherNamesProperty.forEach(otherName -> {
            StringBuilder stringBuilder = new StringBuilder();
            stringBuilder.append(otherName.getLanguage().nid())
                    .append("|")
                    .append(otherName.getCaseSignificance().nid())
                    .append("|")
                    .append(otherName.getNameText());
            if (otherName.getSemanticPublicId() != null) {
                map.put(otherName.getSemanticPublicId().idString(), stringBuilder.toString().hashCode());
            }
        });
        return map;
    }

    public ViewProperties getViewProperties() {
        return getPropertyValue(VIEW_PROPERTIES);
    }

    public void updateStamp() {
        EntityFacade patternFacade = getPropertyValue(PATTERN);
        StampCalculator stampCalculator = getViewProperties().calculator().stampCalculator();

        StampViewModel stampViewModel = getPropertyValue(STAMP_VIEW_MODEL);

        Stamp stamp = stampCalculator.latest(patternFacade).get().stamp();
        stampViewModel.setPropertyValue(STATUS, stamp.state());
        stampViewModel.setPropertyValue(TIME, stamp.time());
        stampViewModel.setPropertyValue(AUTHOR, stamp.author());
        stampViewModel.setPropertyValue(MODULE, stamp.module());
        stampViewModel.setPropertyValue(PATH, stamp.path());
    }



}
