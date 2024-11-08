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
import dev.ikm.tinkar.component.Stamp;
import dev.ikm.tinkar.composer.Composer;
import dev.ikm.tinkar.composer.Session;
import dev.ikm.tinkar.composer.assembler.PatternAssembler;
import dev.ikm.tinkar.composer.template.FullyQualifiedName;
import dev.ikm.tinkar.composer.template.Synonym;
import dev.ikm.tinkar.composer.template.USDialect;
import dev.ikm.tinkar.coordinate.stamp.calculator.StampCalculator;
import dev.ikm.tinkar.entity.ConceptEntity;
import dev.ikm.tinkar.terms.EntityFacade;
import dev.ikm.tinkar.terms.State;
import javafx.beans.property.ObjectProperty;
import javafx.collections.ObservableList;
import org.carlfx.axonic.StateMachine;
import org.carlfx.cognitive.validator.ValidationMessage;
import org.carlfx.cognitive.validator.ValidationResult;
import org.carlfx.cognitive.viewmodel.ViewModel;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.UUID;

import static dev.ikm.tinkar.coordinate.stamp.StampFields.*;
import static dev.ikm.tinkar.terms.EntityProxy.Concept;
import static dev.ikm.tinkar.terms.EntityProxy.Pattern;
import static dev.ikm.tinkar.terms.TinkarTerm.ACCEPTABLE;

public class PatternViewModel extends FormViewModel {

    private static final Logger LOG = LoggerFactory.getLogger(PatternViewModel.class);


    // --------------------------------------------
    // Known properties
    // --------------------------------------------
    public static String STAMP_VIEW_MODEL = "stampViewModel";

    public static String DEFINITION_VIEW_MODEL = "definitionViewModel";

    public static String STATE_MACHINE = "stateMachine";

    public static String FQN_DESCRIPTION_NAME = "fqnDescriptionName";

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
        Concept author = stampViewModel.getPropertyValue(AUTHOR);
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

    public String getPatternIdentifierText() {
        EntityFacade patternFacade = getPropertyValue(PATTERN);
        return String.valueOf(patternFacade.toProxy().publicId().asUuidList().getLastOptional().get());
    }
}
