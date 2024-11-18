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

import dev.ikm.komet.framework.view.ViewProperties;
import dev.ikm.komet.kview.mvvm.model.DescrName;
import dev.ikm.komet.kview.mvvm.model.PatternDefinition;
import dev.ikm.komet.kview.mvvm.model.PatternField;
import dev.ikm.tinkar.common.id.PublicId;
import dev.ikm.tinkar.common.id.PublicIds;
import dev.ikm.tinkar.component.Concept;
import dev.ikm.tinkar.component.Stamp;
import dev.ikm.tinkar.composer.Composer;
import dev.ikm.tinkar.composer.Session;
import dev.ikm.tinkar.composer.assembler.PatternAssembler;
import dev.ikm.tinkar.composer.template.FullyQualifiedName;
import dev.ikm.tinkar.composer.template.Synonym;
import dev.ikm.tinkar.composer.template.USDialect;
import dev.ikm.tinkar.coordinate.Calculators;
import dev.ikm.tinkar.coordinate.stamp.calculator.Latest;
import dev.ikm.tinkar.coordinate.stamp.calculator.StampCalculator;
import dev.ikm.tinkar.coordinate.view.calculator.ViewCalculator;
import dev.ikm.tinkar.entity.ConceptEntity;
import dev.ikm.tinkar.entity.Entity;
import dev.ikm.tinkar.entity.EntityService;
import dev.ikm.tinkar.entity.EntityVersion;
import dev.ikm.tinkar.entity.FieldDefinitionForEntity;
import dev.ikm.tinkar.entity.PatternEntity;
import dev.ikm.tinkar.entity.PatternEntityVersion;
import dev.ikm.tinkar.entity.PatternVersionRecord;
import dev.ikm.tinkar.entity.SemanticEntityVersion;
import dev.ikm.tinkar.terms.ConceptFacade;
import dev.ikm.tinkar.terms.EntityFacade;
import dev.ikm.tinkar.terms.EntityProxy;
import dev.ikm.tinkar.terms.State;
import dev.ikm.tinkar.terms.TinkarTerm;
import javafx.beans.property.ObjectProperty;
import javafx.beans.value.ObservableValue;
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
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.stream.Collectors;

import static dev.ikm.komet.kview.mvvm.model.DataModelHelper.fetchDescendentsOfConcept;
import static dev.ikm.tinkar.common.service.PrimitiveData.PREMUNDANE_TIME;
import static dev.ikm.tinkar.coordinate.stamp.StampFields.*;
import static dev.ikm.tinkar.terms.EntityProxy.Pattern;
import static dev.ikm.tinkar.terms.TinkarTerm.ACCEPTABLE;
import static dev.ikm.tinkar.terms.TinkarTerm.DEFINITION_DESCRIPTION_TYPE;
import static dev.ikm.tinkar.terms.TinkarTerm.DESCRIPTION_CASE_SENSITIVE;
import static dev.ikm.tinkar.terms.TinkarTerm.DESCRIPTION_CASE_SIGNIFICANCE;
import static dev.ikm.tinkar.terms.TinkarTerm.DESCRIPTION_INITIAL_CHARACTER_CASE_SENSITIVE;
import static dev.ikm.tinkar.terms.TinkarTerm.DESCRIPTION_NOT_CASE_SENSITIVE;
import static dev.ikm.tinkar.terms.TinkarTerm.ENGLISH_LANGUAGE;
import static dev.ikm.tinkar.terms.TinkarTerm.FULLY_QUALIFIED_NAME_DESCRIPTION_TYPE;
import static dev.ikm.tinkar.terms.TinkarTerm.LANGUAGE;
import static dev.ikm.tinkar.terms.TinkarTerm.LANGUAGE_CONCEPT_NID_FOR_DESCRIPTION;
import static dev.ikm.tinkar.terms.TinkarTerm.NOT_APPLICABLE;
import static dev.ikm.tinkar.terms.TinkarTerm.REGULAR_NAME_DESCRIPTION_TYPE;

public class PatternViewModel extends FormViewModel {

    private static final Logger LOG = LoggerFactory.getLogger(PatternViewModel.class);


    // --------------------------------------------
    // Known properties
    // --------------------------------------------
    public static String STAMP_VIEW_MODEL = "stampViewModel";

    public static String DEFINITION_VIEW_MODEL = "definitionViewModel";

    public static String STATE_MACHINE = "stateMachine";

    public static String FQN_DESCRIPTION_NAME = "fqnDescriptionName";

    public static String FQN_DATE_ADDED_STR = "fqnDateAddedStr";

    public static String FQN_CASE_SIGNIFICANCE = "fqnCaseSignificance";

    public static String FQN_LANGUAGE = "fqnLanguage";

    public static String OTHER_NAMES = "otherDescriptionNames";

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

    public static String IS_INVALID = "IS_INVALID";

    // Used to load the values in the PatternField controller from PatternDetailsController.
    public static String SELECTED_PATTERN_FIELD = "selectedPatternField";

    public PatternViewModel() {
        super();
            addProperty(VIEW_PROPERTIES, (ViewProperties) null)
                    .addProperty(PATTERN_TOPIC, (UUID) null)
                    .addProperty(STATE_MACHINE, (StateMachine) null)
                    .addProperty(STAMP_VIEW_MODEL, (ViewModel) null)
                    .addProperty(DEFINITION_VIEW_MODEL, (ViewModel) null)
                    .addProperty(FQN_DESCRIPTION_NAME, (DescrName) null)
                    .addProperty(FQN_DATE_ADDED_STR, "")
                    .addProperty(OTHER_NAMES, new ArrayList<DescrName>())
                    // PATTERN>DEFINITION Purpose and Meaning
                    .addProperty(PURPOSE_ENTITY, (EntityFacade) null) // this is/will be the 'purpose' concept entity
                    .addProperty(MEANING_ENTITY, (EntityFacade) null) // this is/will be the 'meaning' concept entity
                    .addProperty(PURPOSE_TEXT, "")
                    .addProperty(MEANING_TEXT, "")
                    .addProperty(PURPOSE_DATE_STR, "")
                    .addProperty(MEANING_DATE_STR, "")
                    // PATTERN>DESCRIPTION FQN and Other Name
                    .addProperty(FQN_DESCRIPTION_NAME_TEXT, "")
                    // Ordered collection of Fields
                    .addProperty(FIELDS_COLLECTION, new ArrayList<PatternField>())
                    .addProperty(SELECTED_PATTERN_FIELD, (PatternField) null)
                    .addProperty(IS_INVALID, true)
                    .addProperty(PATTERN, (EntityFacade) null) // once saved, this is the pattern facade
                    .addValidator(IS_INVALID, "Is Invalid", (ValidationResult vr, ViewModel viewModel) -> {
                        ObjectProperty<EntityFacade> purposeEntity = viewModel.getProperty(PURPOSE_ENTITY);
                        ObjectProperty<EntityFacade> meaningEntity = viewModel.getProperty(MEANING_ENTITY);
                        ObjectProperty<DescrName> fqnProperty = viewModel.getProperty(FQN_DESCRIPTION_NAME);
                        ObservableList<PatternField> fieldsProperty = viewModel.getObservableList(FIELDS_COLLECTION);
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
                        if (fieldsProperty.isEmpty()) {
                            vr.error("At least one field is required for a Pattern.  Please add one or more fields.");
                        }
                        if (stampModule.isNull().get() || stampPath.isNull().get() || stampStatus.isNull().get()) {
                            vr.error("STAMP values are required.  Please fill out the STAMP information.");
                        }
                        viewModel.setPropertyValue(IS_INVALID, !vr.getMessages().isEmpty());
                    });
    }

    public boolean isPatternPopulated() {
        ObjectProperty<Pattern> patternObjectProperty = getProperty(PATTERN);
        return patternObjectProperty.isNotNull().get();
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
    }

    public void populatePattern() {
        ObjectProperty<EntityFacade> patternProperty = getProperty(PATTERN);
        EntityFacade patternFacade = patternProperty.getValue();
        StampCalculator stampCalculator = getViewProperties().calculator().stampCalculator();
        if (patternFacade != null && getPropertyValue(MODE).equals(EDIT)) {
            ViewCalculator viewCalculator = getViewProperties().calculator();
            PatternEntity patternEntity = EntityService.get().getEntityFast(patternFacade.nid());

            // bind purpose
            //FIXME... when you bump out the DEFINITIONS, the purpose and meaning aren't populated
            //do we need to pass it to the patternPropertiesViewModel???

            Latest<PatternVersionRecord> latestPatternVerRec = viewCalculator.latest((patternFacade).nid());
            EntityFacade purposeEntity = Entity.getFast(latestPatternVerRec.get().semanticPurposeNid());
            setPropertyValue(PURPOSE_ENTITY, purposeEntity);
            setPropertyValue(PURPOSE_TEXT, purposeEntity.description());

            EntityVersion purposeLatest = stampCalculator.latest(purposeEntity).get();
            Long purposeMilis = purposeLatest.stamp().time();
            if (purposeMilis.equals(PREMUNDANE_TIME)) {
                setPropertyValue(PURPOSE_DATE_STR, "Date Added: Premundane");
            } else {
                LocalDate purposeDate =
                        Instant.ofEpochMilli(purposeMilis).atZone(ZoneId.systemDefault()).toLocalDate();
                String purposeDateStr = purposeDate.format(DateTimeFormatter.ofPattern("MMM d, yyyy")).toString();
                setPropertyValue(PURPOSE_DATE_STR, "Date Added: " +  purposeDateStr);
            }


            EntityFacade meaningEntity = Entity.getFast(latestPatternVerRec.get().semanticMeaningNid());
            setPropertyValue(MEANING_ENTITY, meaningEntity);
            setPropertyValue(MEANING_TEXT, meaningEntity.description());
            EntityVersion meaningLatest = stampCalculator.latest(purposeEntity).get();
            Long meaningMillis = meaningLatest.stamp().time();
            if (meaningMillis.equals(PREMUNDANE_TIME)) {
                setPropertyValue(MEANING_DATE_STR, "Date Added: Premundane");
            } else {
                LocalDate meaningDate =
                        Instant.ofEpochMilli(meaningMillis).atZone(ZoneId.systemDefault()).toLocalDate();
                String meaningDateStr = meaningDate.format(DateTimeFormatter.ofPattern("MMM d, yyyy")).toString();
                setPropertyValue(MEANING_DATE_STR, "Date Added: " + meaningDateStr);
            }

            Map<SemanticEntityVersion, List<String>> descriptionSemanticsMap = latestDescriptionSemantics(viewCalculator, patternFacade);

            ObservableList<DescrName> descrNameObservableList = getObservableList(OTHER_NAMES);
            descriptionSemanticsMap.forEach((semanticEntityVersion, fieldDescriptions) -> {

                boolean isFQN = semanticEntityVersion
                        .fieldValues()
                        .stream()
                        .anyMatch( fieldValue ->
                                (fieldValue instanceof ConceptFacade facade) &&
                                        facade.nid() == FULLY_QUALIFIED_NAME_DESCRIPTION_TYPE.nid());
                PatternEntityVersion patternEntityVersion = (PatternEntityVersion) viewCalculator.latest(patternEntity).get();
                if (isFQN) {
                    String fqnNameDescription = (String) semanticEntityVersion.fieldValues().stream().filter(fv -> fv instanceof String)
                            .collect(Collectors.toList()).getFirst();

                    ConceptEntity caseEntity = getCaseConcept(patternEntity, viewCalculator, semanticEntityVersion);
                    ConceptEntity langEntity = getLanguageConcept(patternEntityVersion, viewCalculator, semanticEntityVersion);

                    DescrName fqnDescrName = new DescrName(null, fqnNameDescription, FULLY_QUALIFIED_NAME_DESCRIPTION_TYPE,
                            caseEntity, null, null,
                            langEntity, semanticEntityVersion.publicId());
                    setPropertyValue(FQN_DESCRIPTION_NAME, fqnDescrName);
                    setPropertyValue(FQN_DESCRIPTION_NAME_TEXT, fqnDescrName.getNameText());

                    EntityVersion fqnLatest = (EntityVersion) stampCalculator.latest(semanticEntityVersion.entity()).get();
                    Long fqnMillis = fqnLatest.stamp().time();
                    if (fqnMillis.equals(PREMUNDANE_TIME)) {
                        setPropertyValue(FQN_DATE_ADDED_STR, "Premundane");
                    } else {
                        LocalDate fqnDate =
                                Instant.ofEpochMilli(fqnLatest.stamp().time()).atZone(ZoneId.systemDefault()).toLocalDate();
                        String fqnDateStr = fqnDate.format(DateTimeFormatter.ofPattern("MMM d, yyyy")).toString();
                        setPropertyValue(FQN_DATE_ADDED_STR, fqnDateStr);
                    }
                } else {
                    String otherNameDescription = (String) semanticEntityVersion.fieldValues().stream().filter(fv -> fv instanceof String)
                            .collect(Collectors.toList()).getFirst();

                    ConceptEntity caseEntity = getCaseConcept(patternEntity, viewCalculator, semanticEntityVersion);

                    // put the stamp in the DescrName (or just the time)
                    Stamp stamp = semanticEntityVersion.stamp();
                    ConceptEntity langEntity = getLanguageConcept(patternEntityVersion, viewCalculator, semanticEntityVersion);

                    DescrName descrName = new DescrName(null, otherNameDescription, REGULAR_NAME_DESCRIPTION_TYPE,
                            caseEntity, null, null,
                            langEntity, semanticEntityVersion.publicId(), stamp);
                    descrNameObservableList.add(descrName);
                }
            });

            // load the pattern fields
            StampCalculator stampCalc = Calculators.Stamp.DevelopmentLatestActiveOnly();
            PatternEntityVersion latestDescriptionPattern = (PatternEntityVersion) stampCalc.latest(patternFacade).get();
            ImmutableList<? extends FieldDefinitionForEntity> fieldDefinitions = latestDescriptionPattern.fieldDefinitions();

            ObservableList<PatternField> patternFieldObsList = getObservableList(FIELDS_COLLECTION);
            List<PatternField> patternFields = convertFieldDefinitions(fieldDefinitions);
            patternFields.forEach(patternField -> patternFieldObsList.add(patternField));
        }
    }

    private List<PatternField> convertFieldDefinitions(ImmutableList<? extends FieldDefinitionForEntity> fieldDefinitions) {
        List<PatternField> patternFieldList = new ArrayList<>(fieldDefinitions.size());
        AtomicInteger idx = new AtomicInteger();
        idx.set(0);
        fieldDefinitions.stream().forEach(f -> {
            StampCalculator stampCalculator = getViewProperties().calculator().stampCalculator();
            EntityVersion latest = (EntityVersion) stampCalculator.latest(f.meaning()).get();

            patternFieldList.add(idx.getAndIncrement(), new PatternField(f.meaning().description(), f.dataType(), f.purpose(), f.meaning(), "", latest.stamp()));
        });
        return patternFieldList;
    }

    private Map<SemanticEntityVersion, List<String>> latestDescriptionSemantics(final ViewCalculator viewCalculator, EntityFacade conceptFacade) {
        Map<SemanticEntityVersion, List<String>> descriptionSemanticsMap = new HashMap<>();

        // FQN - English | Case Sensitive
        // REG - English | Case Sensitive

        //Get latest description semantic version of the passed in concept (entityfacade)
        //Latest<SemanticEntityVersion> latestDescriptionSemanticVersion = viewCalculator.getDescription(conceptFacade);

        //There should always be one FQN
        //There can be 0 or more Regular Names
        //Loop through, conditionally sort semantics by their description type concept object
        //Update UI via the descriptionRegularName function on the
        viewCalculator.getDescriptionsForComponent(conceptFacade).stream()
                .filter(semanticEntity -> {
                    // semantic -> semantic version -> pattern version(index meaning field from DESCR_Type)
                    Latest<SemanticEntityVersion> semanticVersion = viewCalculator.latest(semanticEntity);

                    PatternEntity<PatternEntityVersion> patternEntity = semanticEntity.pattern();
                    PatternEntityVersion patternEntityVersion = viewCalculator.latest(patternEntity).get();

                    int indexForDescrType = patternEntityVersion.indexForMeaning(TinkarTerm.DESCRIPTION_TYPE);

                    // Filter (include) semantics where they contain descr type having FQN, Regular name, Definition Descr.
                    Object descriptionTypeConceptValue = semanticVersion.get().fieldValues().get(indexForDescrType);
                    if(descriptionTypeConceptValue instanceof EntityFacade descriptionTypeConcept ){
                        int typeId = descriptionTypeConcept.nid();
                        return (typeId == FULLY_QUALIFIED_NAME_DESCRIPTION_TYPE.nid() ||
                                typeId == REGULAR_NAME_DESCRIPTION_TYPE.nid() ||
                                typeId == DEFINITION_DESCRIPTION_TYPE.nid());
                    }
                    return false;
                }).forEach(semanticEntity -> {
                    // Each description obtain the latest semantic version, pattern version and their field values based on index
                    Latest<SemanticEntityVersion> semanticVersion = viewCalculator.latest(semanticEntity);
                    PatternEntity<PatternEntityVersion> patternEntity = semanticEntity.pattern();
                    PatternEntityVersion patternEntityVersion = viewCalculator.latest(patternEntity).get();

                    int indexCaseSig = patternEntityVersion.indexForMeaning(DESCRIPTION_CASE_SIGNIFICANCE);
                    int indexLang = patternEntityVersion.indexForMeaning(LANGUAGE_CONCEPT_NID_FOR_DESCRIPTION);

                    List<String> descrFields = new ArrayList<>();
                    descriptionSemanticsMap.put(semanticVersion.get(), descrFields);
                    Object caseSigConcept = semanticVersion.get().fieldValues().get(indexCaseSig);
                    Object langConcept = semanticVersion.get().fieldValues().get(indexLang);

                    // e.g. FQN - English | Case Sensitive
                    String casSigText = viewCalculator.getRegularDescriptionText(((ConceptFacade) caseSigConcept).nid())
                            .orElse(String.valueOf(((ConceptFacade) caseSigConcept).nid()));
                    String langText = viewCalculator.getRegularDescriptionText(((ConceptFacade) langConcept).nid())
                            .orElse(String.valueOf(((ConceptFacade) langConcept).nid()));

                    descrFields.add(casSigText);
                    descrFields.add(langText);
                });
        return descriptionSemanticsMap;

    }

    public ConceptEntity getLanguageConcept(PatternEntityVersion patternEntityVersion, ViewCalculator viewCalculator, SemanticEntityVersion semanticEntityVersion) {
        int indexLang = patternEntityVersion.indexForMeaning(LANGUAGE_CONCEPT_NID_FOR_DESCRIPTION);

        Object langConcept = (indexLang != -1)
                ? semanticEntityVersion.fieldValues().get(indexLang)
                : ENGLISH_LANGUAGE;

        String langText = viewCalculator.getRegularDescriptionText(((ConceptFacade) langConcept).nid())
                .orElse(String.valueOf(((ConceptFacade) langConcept).nid())).toUpperCase();

        Set<ConceptEntity> possibleLanguages = fetchDescendentsOfConcept(getViewProperties(), LANGUAGE.publicId());

        Optional<ConceptEntity> langEntity = possibleLanguages.stream().filter(lang -> lang.toString().equalsIgnoreCase(langText)).findFirst();

        return langEntity.orElse(Entity.getFast(ENGLISH_LANGUAGE));
    }

    public ConceptEntity getCaseConcept(PatternEntity patternEntity, ViewCalculator viewCalculator, SemanticEntityVersion semanticEntityVersion) {
        PatternEntityVersion patternEntityVersion = (PatternEntityVersion) viewCalculator.latest(patternEntity).get();
        int indexCaseSig = patternEntityVersion.indexForMeaning(DESCRIPTION_CASE_SIGNIFICANCE);


        Object caseSigConcept = (indexCaseSig != -1)
                ? (Concept) semanticEntityVersion.fieldValues().get(indexCaseSig)
                : NOT_APPLICABLE;

        String casSigText = viewCalculator.getRegularDescriptionText(((ConceptFacade) caseSigConcept).nid())
                .orElse(String.valueOf(((ConceptFacade) caseSigConcept).nid())).toUpperCase();

        //FIXME: need a better way to query the Description semantic
        return switch (casSigText) {
            case "DESCRIPTION_NOT_CASE_SENSITIVE":
                yield Entity.getFast(DESCRIPTION_NOT_CASE_SENSITIVE.nid());
            case "DESCRIPTION_CASE_SENSITIVE":
                yield Entity.getFast(DESCRIPTION_CASE_SENSITIVE.nid());
            case "DESCRIPTION_INITIAL_CHARACTER_CASE_SENSITIVE":
                yield Entity.getFast(DESCRIPTION_INITIAL_CHARACTER_CASE_SENSITIVE.nid());
            default:
                yield Entity.getFast(NOT_APPLICABLE.nid());
        };
    }


    public boolean createPattern() {
        save();

        if (hasErrorMsgs()) {
            for (ValidationMessage validationMessage : getValidationMessages()) {
                LOG.error(validationMessage.toString());
            }
            return false;
        }

        PublicId patternPublicId = PublicIds.newRandom();
        Pattern pattern = Pattern.make(patternPublicId);
        Composer composer = new Composer("Save Pattern Definition");

        // get the STAMP values from the nested stampViewModel
        StampViewModel stampViewModel = getPropertyValue(STAMP_VIEW_MODEL);
        State state = stampViewModel.getPropertyValue(STATUS);
        EntityProxy.Concept author = stampViewModel.getPropertyValue(AUTHOR);
        ConceptEntity module = stampViewModel.getPropertyValue(MODULE);
        ConceptEntity path = stampViewModel.getPropertyValue(PATH);
        Session session = composer.open(state, author, module.toProxy(), path.toProxy());

        // set up pattern with the fully qualified name
        ObservableList<PatternField> fieldsProperty = getObservableList(FIELDS_COLLECTION);
        session.compose((PatternAssembler patternAssembler) -> {
                patternAssembler
                            .pattern(pattern)
                            .meaning(((EntityFacade)getPropertyValue(MEANING_ENTITY)).toProxy());
            // add the field definitions
            for (int i = 0; i< fieldsProperty.size(); i++) {
                PatternField patternField = fieldsProperty.get(i);
                patternAssembler.fieldDefinition(patternField.meaning().toProxy(), patternField.purpose().toProxy(),
                        patternField.dataType().toProxy(), i);
            }
            patternAssembler.purpose(((EntityFacade)getPropertyValue(PURPOSE_ENTITY)).toProxy())
                            .attach((FullyQualifiedName fqn) -> fqn
                                    .language(((EntityFacade)getPropertyValue(FQN_LANGUAGE)).toProxy())
                                    .text(getPropertyValue(FQN_DESCRIPTION_NAME_TEXT))
                                    .caseSignificance(((EntityFacade)getPropertyValue(FQN_CASE_SIGNIFICANCE)).toProxy()));
        });

        // add the other name description semantics if they exist
        ObservableList<DescrName> otherNamesProperty = getObservableList(OTHER_NAMES);
        if (!otherNamesProperty.isEmpty()) {
            otherNamesProperty.forEach(otherName ->
                    session.compose(new Synonym()
                                    .language(otherName.getLanguage().toProxy())
                                    .text(otherName.getNameText())
                                    .caseSignificance(otherName.getCaseSignificance().toProxy()), pattern)
                            .attach(new USDialect()
                                    .acceptability(ACCEPTABLE))
            );
        }
        boolean isSuccess = composer.commitSession(session);
        setPropertyValue(PATTERN, pattern);
        updateStamp();
        return isSuccess;
    }

    public String getPatternTitle() {
        return ((EntityFacade) getPropertyValue(PATTERN)).description();
    }

    public ViewProperties getViewProperties() {
        return getPropertyValue(VIEW_PROPERTIES);
    }

    public void updateStamp() {
        EntityFacade patternFacade = getPropertyValue(PATTERN);
        StampCalculator stampCalculator = getViewProperties().calculator().stampCalculator();

        StampViewModel stampViewModel = getPropertyValue(STAMP_VIEW_MODEL);

        Stamp stamp = stampCalculator.latest(patternFacade).get().stamp();
        stampViewModel.setValue(STATUS, stamp.state());
        stampViewModel.setValue(TIME, stamp.time());
        stampViewModel.setValue(AUTHOR, stamp.author());
        stampViewModel.setValue(MODULE, stamp.module());
        stampViewModel.setValue(PATH, stamp.path());
    }



}
