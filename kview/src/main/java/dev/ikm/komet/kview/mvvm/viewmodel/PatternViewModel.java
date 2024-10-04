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

import static dev.ikm.tinkar.terms.EntityProxy.Pattern;
import static dev.ikm.tinkar.terms.EntityProxy.Concept;
import static dev.ikm.tinkar.terms.TinkarTerm.ACCEPTABLE;
import static dev.ikm.tinkar.terms.TinkarTerm.DESCRIPTION_NOT_CASE_SENSITIVE;
import static dev.ikm.tinkar.terms.TinkarTerm.DESCRIPTION_PATTERN;
import static dev.ikm.tinkar.terms.TinkarTerm.ENGLISH_LANGUAGE;
import static dev.ikm.tinkar.terms.TinkarTerm.PREFERRED;
import static dev.ikm.tinkar.terms.TinkarTerm.REGULAR_NAME_DESCRIPTION_TYPE;
import dev.ikm.tinkar.component.FieldDefinition;
import dev.ikm.tinkar.composer.Composer;
import dev.ikm.tinkar.composer.Session;
import dev.ikm.tinkar.composer.assembler.PatternAssembler;
import dev.ikm.tinkar.composer.assembler.SemanticAssembler;
import dev.ikm.tinkar.composer.template.FullyQualifiedName;
import dev.ikm.tinkar.composer.template.Synonym;
import dev.ikm.tinkar.composer.template.USDialect;
import dev.ikm.komet.framework.view.ViewProperties;
import dev.ikm.komet.kview.mvvm.model.DescrName;
import dev.ikm.komet.kview.mvvm.model.PatternDefinition;
import dev.ikm.komet.kview.mvvm.model.PatternField;
import dev.ikm.tinkar.common.id.PublicId;
import dev.ikm.tinkar.common.id.PublicIds;
import dev.ikm.tinkar.entity.EntityService;
import dev.ikm.tinkar.terms.EntityFacade;
import dev.ikm.tinkar.terms.State;
import dev.ikm.tinkar.terms.TinkarTerm;
import javafx.beans.property.ObjectProperty;
import javafx.beans.property.SimpleListProperty;
import javafx.collections.ObservableList;
import org.carlfx.cognitive.validator.ValidationMessage;
import org.carlfx.cognitive.validator.ValidationResult;
import org.carlfx.cognitive.viewmodel.ViewModel;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

public class PatternViewModel extends FormViewModel {

    private static final Logger LOG = LoggerFactory.getLogger(PatternViewModel.class);


    // --------------------------------------------
    // Known properties
    // --------------------------------------------
    public static String STAMP_VIEW_MODEL = "stampViewModel";

    public static String DEFINITION_VIEW_MODEL = "definitionViewModel";
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

    public static String IS_INVALID = "IS_INVALID";

    public PatternViewModel() {
        super();
            addProperty(VIEW_PROPERTIES, (ViewProperties) null)
                    .addProperty(PATTERN_TOPIC, (UUID) null)
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
                    .addProperty(IS_INVALID, true)
                    .addValidator(IS_INVALID, "Is Invalid", (ValidationResult vr, ViewModel viewModel) -> {
                        ObjectProperty<EntityFacade> purposeEntity = viewModel.getProperty(PURPOSE_ENTITY);
                        ObjectProperty<EntityFacade> meaningEntity = viewModel.getProperty(MEANING_ENTITY);
                        ObjectProperty<DescrName> fqnProperty = viewModel.getProperty(FQN_DESCRIPTION_NAME);
                        ObservableList<PatternField> fieldsProperty = viewModel.getObservableList(FIELDS_COLLECTION);
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
        State status = State.ACTIVE;
        Concept author = TinkarTerm.USER;
        Concept module = TinkarTerm.MODULE;
        Concept path = TinkarTerm.DEVELOPMENT_PATH;
        Session session = composer.open(status, author, module, path);

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

        return composer.commitSession(session);
    }
}
