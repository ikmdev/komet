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

import static dev.ikm.komet.kview.mvvm.viewmodel.stamp.StampFormViewModelBase.Properties.AUTHOR;
import static dev.ikm.komet.kview.mvvm.viewmodel.stamp.StampFormViewModelBase.Properties.MODULE;
import static dev.ikm.komet.kview.mvvm.viewmodel.stamp.StampFormViewModelBase.Properties.PATH;
import static dev.ikm.komet.kview.mvvm.viewmodel.stamp.StampFormViewModelBase.Properties.STATUS;
import static dev.ikm.tinkar.common.service.PrimitiveData.PREMUNDANE_TIME;
import static dev.ikm.tinkar.terms.EntityProxy.Pattern;
import static dev.ikm.tinkar.terms.TinkarTerm.ACCEPTABLE;
import static dev.ikm.tinkar.terms.TinkarTerm.DEFINITION_DESCRIPTION_TYPE;
import static dev.ikm.tinkar.terms.TinkarTerm.FULLY_QUALIFIED_NAME_DESCRIPTION_TYPE;
import static dev.ikm.tinkar.terms.TinkarTerm.REGULAR_NAME_DESCRIPTION_TYPE;
import dev.ikm.komet.framework.observable.ObservableComposer;
import dev.ikm.komet.framework.observable.ObservableComposer.EntityComposer;
import dev.ikm.komet.framework.observable.ObservableField;
import dev.ikm.komet.framework.observable.ObservablePattern;
import dev.ikm.komet.framework.observable.ObservablePatternVersion;
import dev.ikm.komet.framework.observable.ObservableSemantic;
import dev.ikm.komet.framework.observable.ObservableSemanticVersion;
import dev.ikm.tinkar.coordinate.stamp.calculator.Latest;
import dev.ikm.komet.framework.view.ViewProperties;
import dev.ikm.komet.kview.mvvm.model.DescrName;
import dev.ikm.komet.kview.mvvm.model.PatternDefinition;
import dev.ikm.komet.kview.mvvm.model.PatternField;
import dev.ikm.komet.kview.mvvm.viewmodel.stamp.StampFormViewModelBase;
import dev.ikm.tinkar.common.id.PublicId;
import dev.ikm.tinkar.common.id.PublicIds;
import dev.ikm.tinkar.common.service.PrimitiveData;
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
import dev.ikm.tinkar.terms.*;
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
    // STAMP_VIEW_MODEL, FIELDS_COLLECTION, PATTERN inherited from ViewModelKey.

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
            addProperty(ViewModelKey.VIEW_PROPERTIES, (ViewProperties) null)
                    .addProperty(PATTERN_TITLE_TEXT, "")
                    .addProperty(PATTERN_TOPIC, (UUID) null)
                    .addProperty(STATE_MACHINE, (StateMachine) null)
                    .addProperty(ViewModelKey.STAMP_VIEW_MODEL, (StampFormViewModelBase) null)
                    .addProperty(FQN_DESCRIPTION_NAME, (DescrName) null)
                    .addProperty(OTHER_NAMES, new ArrayList<DescrName>())
                    .addProperty(OTHER_NAME_SEMANTIC_VERSION_MAP, new HashMap<DescrName, SemanticEntityVersion>())
                    // PATTERN>DEFINITION Purpose and Meaning
                    .addProperty(PURPOSE_ENTITY, (EntityFacade) null) // this is/will be the 'purpose' concept entity
                    .addProperty(MEANING_ENTITY, (EntityFacade) null) // this is/will be the 'meaning' concept entity
                    .addProperty(PURPOSE_TEXT, "")
                    .addProperty(MEANING_TEXT, "")
                    .addProperty(PUBLISH_PENDING,false)
                    // PATTERN>DESCRIPTION FQN and Other Name
                    .addProperty(FQN_DESCRIPTION_NAME_TEXT, "")
                    // Ordered collection of Fields
                    .addProperty(ViewModelKey.FIELDS_COLLECTION, new ArrayList<PatternField>())
                    .addProperty(SELECTED_PATTERN_FIELD, (PatternField) null)
                    .addProperty(IS_INVALID, true)
                    .addProperty(ViewModelKey.PATTERN, (EntityFacade) null) // once saved, this is the pattern facade
                    .addProperty(FQN_PROXY, (EntityProxy.Semantic) null)
                    .addProperty(HAS_CHANGED, false)
                    .addValidator(IS_INVALID, "Is Invalid", (ValidationResult vr, ViewModel viewModel) -> {
                        ObjectProperty<EntityFacade> purposeEntity = viewModel.getProperty(PURPOSE_ENTITY);
                        ObjectProperty<EntityFacade> meaningEntity = viewModel.getProperty(MEANING_ENTITY);
                        ObjectProperty<DescrName> fqnProperty = viewModel.getProperty(FQN_DESCRIPTION_NAME);
                        StampFormViewModelBase stampFormViewModel = viewModel.getPropertyValue(ViewModelKey.STAMP_VIEW_MODEL);
                        ObjectProperty<?> stampModule = stampFormViewModel.getProperty(MODULE);
                        ObjectProperty<?> stampPath = stampFormViewModel.getProperty(PATH);
                        ObjectProperty<?> stampStatus = stampFormViewModel.getProperty(STATUS);
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
        ObservableList<PatternField> patternFieldObsList = getObservableList(ViewModelKey.FIELDS_COLLECTION);
        patternFieldObsList.clear();
        ObservableList<DescrName> otherNamesList = getObservableList(OTHER_NAMES);
        otherNamesList.clear();
        loadPatternValues();
    }

    @SuppressWarnings("removal")
    public void loadPatternValues(){
        ObjectProperty<EntityFacade> patternProperty = getProperty(ViewModelKey.PATTERN);
        EntityFacade patternFacade = patternProperty.getValue();
        if (patternFacade != null && getPropertyValue(ViewModelKey.MODE).equals(EDIT)) {
            Entity entity = EntityService.get().getEntityFast(patternFacade);
            ViewCalculator viewCalculator = getViewProperties().calculator();

            // Load Fields data.
            ObservableList<PatternField> patternFieldObsList = getObservableList(ViewModelKey.FIELDS_COLLECTION);
            PatternVersionRecord patternVersionRecord = (PatternVersionRecord) viewCalculator.latest(entity).get();
            ImmutableList<FieldDefinitionRecord> fieldDefinitionRecords = patternVersionRecord.fieldDefinitions();

            fieldDefinitionRecords.stream().forEachOrdered( fieldDefinitionForEntity ->
            {
                EntityVersion latest = (EntityVersion) viewCalculator.latest(fieldDefinitionForEntity.meaning()).get();
                PatternField patternField = new PatternField(viewCalculator.getDescriptionTextOrNid(fieldDefinitionForEntity.meaning().nid()), fieldDefinitionForEntity.dataType(),
                        fieldDefinitionForEntity.purpose(), fieldDefinitionForEntity.meaning(), "", latest.stamp());
                patternFieldObsList.add(patternField);
            });

            // load Definition Semantics
            Latest<EntityVersion> latestEntityVersion = viewCalculator.latest(entity);
            EntityVersion entityVersion = latestEntityVersion.get();

            Entity purposeEntity = ((PatternVersionRecord) entityVersion).semanticPurpose();
            setPropertyValue(PURPOSE_ENTITY, purposeEntity);
            setPropertyValue(PURPOSE_TEXT, viewCalculator.getDescriptionTextOrNid(purposeEntity.nid()));

            Entity meaningEntity = ((PatternVersionRecord) entityVersion).semanticMeaning();
            setPropertyValue(MEANING_ENTITY, meaningEntity);
            setPropertyValue(MEANING_TEXT, viewCalculator.getDescriptionTextOrNid(meaningEntity.nid()));

            String patternTitleText = retrieveDisplayName((PatternFacade) patternFacade);
            setPropertyValue(PATTERN_TITLE_TEXT, patternTitleText);

            loadFqnDetails(patternFacade);

            viewCalculator.forEachSemanticVersionForComponentOfPattern(entity.nid(), TinkarTerm.DESCRIPTION_PATTERN.nid(),
                (semanticEntityVersion,  entityVersion1, patternEntityVersion) -> {
                    EntityFacade language = (EntityFacade) semanticEntityVersion.fieldValues().get(0);
                    String nameText = (String) semanticEntityVersion.fieldValues().get(1);
                    EntityFacade caseSignificance = (EntityFacade) semanticEntityVersion.fieldValues().get(2);
                    EntityFacade descriptionType = (EntityFacade) semanticEntityVersion.fieldValues().get(3);
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

        // Reload STAMP Form View Model
        StampFormViewModelBase stampFormViewModel = getPropertyValue(ViewModelKey.STAMP_VIEW_MODEL);
        stampFormViewModel.update(patternFacade, getPropertyValue(PATTERN_TOPIC), getViewProperties());

        logPopulateDiagnostics(patternFacade);
    }

    /**
     * One-shot diagnostic for the intermittent "empty shell" pattern window on restore: a window whose
     * chrome appears but whose fields/descriptions are blank because content was populated exactly once,
     * synchronously, at restore — before the datastore was warm. Two empty-shell signatures are possible:
     * (1) the persisted nid did not resolve to an entity yet (the restore factory's {@code Entity.getFast}
     * returned {@code null}, so {@code PATTERN} is {@code null} and the EDIT-mode load block above is
     * skipped entirely); or (2) the entity resolved but no latest version is resolvable under this
     * window's coordinate. Unlike the concept window, the pattern window registers no data-ready re-render
     * hook, so either case persists until a coordinate change or a commit. This emits one definitive line.
     *
     * @param patternFacade the PATTERN facade as seen at the end of this populate (may be {@code null})
     */
    private void logPopulateDiagnostics(EntityFacade patternFacade) {
        final Object mode = getPropertyValue(ViewModelKey.MODE);
        if (patternFacade == null) {
            LOG.warn("EMPTY-SHELL? Pattern window populate: patternFacade=null mode={} "
                    + "(persisted nid did not resolve to an entity at restore; load block skipped)", mode);
            return;
        }
        final Entity entity = EntityService.get().getEntityFast(patternFacade.nid());
        final boolean entityResolved = entity != null;
        final boolean dataLoaded = entityResolved
                && getViewProperties().calculator().latest(entity).isPresent();
        final int fieldNodes = getObservableList(ViewModelKey.FIELDS_COLLECTION).size();
        final int otherNameNodes = getObservableList(OTHER_NAMES).size();
        final String msg = "Pattern window populate: nid={} mode={} entityResolved={} dataLoaded={} fieldNodes={} otherNameNodes={}";
        if (!entityResolved || !dataLoaded) {
            LOG.warn("EMPTY-SHELL? " + msg, patternFacade.nid(), mode, entityResolved, dataLoaded, fieldNodes, otherNameNodes);
        } else {
            LOG.info(msg, patternFacade.nid(), mode, entityResolved, dataLoaded, fieldNodes, otherNameNodes);
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
        Latest<SemanticEntityVersion> latestFqn = getViewProperties().calculator().languageCalculator()
                .getFullyQualifiedDescription(patternFacade);
        if (!latestFqn.isPresent()) {
            LOG.warn("No FQN description found for pattern: {} (nid={})", patternFacade, patternFacade.nid());
            return;
        }
        SemanticEntityVersion fqnSemanticEntityVersion = latestFqn.get();

        EntityFacade fqnLanguage = (EntityFacade) fqnSemanticEntityVersion.fieldValues().get(0);
        String fqnString = (String) fqnSemanticEntityVersion.fieldValues().get(1);
        EntityFacade fqnCaseSignificance = (EntityFacade) fqnSemanticEntityVersion.fieldValues().get(2);
        EntityFacade fqnDescriptionType = (EntityFacade) fqnSemanticEntityVersion.fieldValues().get(3);
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
        ViewProperties viewProperties = getPropertyValue(ViewModelKey.VIEW_PROPERTIES);
        // Follow the view coordinate's description-type preference (FQN vs regular) so the pattern
        // title tracks the language coordinate like the concept banner and axiom badges (#660).
        return viewProperties.calculator().getDescriptionTextOrNid(patternFacade.nid());
    }

    /**
     * Re-resolves the coordinate-dependent display values (title, semantic purpose, semantic meaning)
     * and updates their bound properties. Unlike {@link #loadPatternValues()} (which appends to the
     * field/name lists) this is safe to call repeatedly, so it is wired to a view-coordinate change to
     * keep these following the language coordinate (ike-issues#660). The field-name list is resolved at
     * load time only; refreshing those live would require rebuilding the field list.
     */
    public void refreshForCoordinate() {
        ViewCalculator viewCalculator = getViewProperties().calculator();
        EntityFacade patternFacade = getPropertyValue(ViewModelKey.PATTERN);
        if (patternFacade != null) {
            setPropertyValue(PATTERN_TITLE_TEXT, retrieveDisplayName((PatternFacade) patternFacade));
        }
        Entity purposeEntity = getPropertyValue(PURPOSE_ENTITY);
        if (purposeEntity != null) {
            setPropertyValue(PURPOSE_TEXT, viewCalculator.getDescriptionTextOrNid(purposeEntity.nid()));
        }
        Entity meaningEntity = getPropertyValue(MEANING_ENTITY);
        if (meaningEntity != null) {
            setPropertyValue(MEANING_TEXT, viewCalculator.getDescriptionTextOrNid(meaningEntity.nid()));
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

        PublicId patternPublicId = getPropertyValue(ViewModelKey.PATTERN) == null
                ? PublicIds.newRandom()
                : ((PatternFacade) getPropertyValue(ViewModelKey.PATTERN)).publicId();
        boolean isEdit = getPropertyValue(ViewModelKey.PATTERN) != null;

        // Extract STAMP values from the nested stampViewModel
        StampFormViewModelBase stampFormViewModel = getPropertyValue(ViewModelKey.STAMP_VIEW_MODEL);
        State state = stampFormViewModel.getPropertyValue(STATUS);

        Object authorObject = stampFormViewModel.getPropertyValue(AUTHOR);
        EntityProxy.Concept authorConcept = null;
        if (authorObject instanceof EntityProxy.Concept concept) {
            authorConcept = concept;
        } else if (authorObject instanceof ConceptRecord authorConceptRecord) {
            authorConcept = EntityProxy.Concept.make(authorConceptRecord.nid());
        }

        ConceptEntity module = stampFormViewModel.getPropertyValue(MODULE);
        ConceptEntity path = stampFormViewModel.getPropertyValue(PATH);

        // Create ObservableComposer — handles scoped values internally
        ObservableComposer composer = ObservableComposer.create(
                getViewProperties().calculator(), state, authorConcept, module, path,
                "Save Pattern Definition");

        // --- Compose the pattern entity ---
        EntityComposer<ObservablePatternVersion.Editable, ObservablePattern> patternComposer =
                composer.composePattern(patternPublicId);
        ObservablePatternVersion.Editable patternEditable = patternComposer.getEditableVersion();

        EntityFacade meaningEntity = (EntityFacade) getPropertyValue(MEANING_ENTITY);
        EntityFacade purposeEntity = (EntityFacade) getPropertyValue(PURPOSE_ENTITY);
        patternEditable.getMeaningProperty().set(meaningEntity);
        patternEditable.getPurposeProperty().set(purposeEntity);

        // Build field definitions from the UI collection
        ObservableList<PatternField> fieldsProperty = getObservableList(ViewModelKey.FIELDS_COLLECTION);
        int patternNid = PrimitiveData.nid(patternPublicId);
        int stampNid = patternEditable.getEditStamp().nid();
        org.eclipse.collections.api.list.MutableList<FieldDefinitionRecord> fieldDefs =
                org.eclipse.collections.api.factory.Lists.mutable.ofInitialCapacity(fieldsProperty.size());
        for (int i = 0; i < fieldsProperty.size(); i++) {
            PatternField pf = fieldsProperty.get(i);
            fieldDefs.add(new FieldDefinitionRecord(
                    pf.dataType().nid(), pf.purpose().nid(), pf.meaning().nid(),
                    stampNid, patternNid, i));
        }
        patternEditable.setFieldDefinitions(fieldDefs.toImmutable());
        patternComposer.save();

        ObservablePattern observablePattern = patternComposer.getEntity();

        // --- Compose FQN description semantic ---
        // Only compose if creating new, or FQN has changed in edit mode
        ObjectProperty<EntityProxy.Semantic> fqnProp = getObjectProperty(FQN_PROXY);
        if (!isEdit || generateFqnHash() != changeHash) {
            PublicId fqnPublicId = (fqnProp.get() != null)
                    ? fqnProp.get().publicId()
                    : PublicIds.newRandom();

            EntityComposer<ObservableSemanticVersion.Editable, ObservableSemantic> fqnComposer =
                    composer.composeSemantic(fqnPublicId, observablePattern, TinkarTerm.DESCRIPTION_PATTERN);
            ObservableSemanticVersion.Editable fqnEditable = fqnComposer.getEditableVersion();

            // DESCRIPTION_PATTERN fields: [0]=language, [1]=text, [2]=caseSignificance, [3]=descriptionType
            fqnEditable.getEditableField(0).setObjectValue(getPropertyValue(FQN_LANGUAGE));
            fqnEditable.getEditableField(1).setObjectValue(getPropertyValue(FQN_DESCRIPTION_NAME_TEXT));
            fqnEditable.getEditableField(2).setObjectValue(getPropertyValue(FQN_CASE_SIGNIFICANCE));
            fqnEditable.getEditableField(3).setObjectValue(FULLY_QUALIFIED_NAME_DESCRIPTION_TYPE);
            fqnComposer.save();

            // Update the FQN proxy property
            fqnProp.set(EntityProxy.Semantic.make(fqnPublicId));

            // Compose US Dialect for the FQN
            PublicId dialectPublicId = PublicIds.newRandom();
            EntityComposer<ObservableSemanticVersion.Editable, ObservableSemantic> dialectComposer =
                    composer.composeSemantic(dialectPublicId, fqnComposer.getEntity(), TinkarTerm.US_DIALECT_PATTERN);
            ObservableSemanticVersion.Editable dialectEditable = dialectComposer.getEditableVersion();
            // US_DIALECT_PATTERN fields: [0]=acceptability
            dialectEditable.getEditableField(0).setObjectValue(ACCEPTABLE);
            dialectComposer.save();
        }

        // --- Compose other name (synonym) description semantics ---
        ObservableList<DescrName> otherNamesProperty = getObservableList(OTHER_NAMES);
        Map<String, Integer> currentOtherNameMap = Map.of();
        if (isEdit) {
            currentOtherNameMap = generateOtherNameHash();
        }
        final Map<String, Integer> finalCurrentOtherNameMap = currentOtherNameMap;

        for (DescrName otherName : otherNamesProperty) {
            boolean shouldCompose;
            PublicId otherNamePublicId;

            if (isEdit) {
                String otKey = otherName.getSemanticPublicId() != null
                        ? otherName.getSemanticPublicId().idString() : "-not-found-";
                shouldCompose = !baselineOtherNameHashMap.containsKey(otKey)
                        || !baselineOtherNameHashMap.get(otKey).equals(finalCurrentOtherNameMap.get(otKey));
                otherNamePublicId = otherName.getSemanticPublicId() != null
                        ? otherName.getSemanticPublicId() : PublicIds.newRandom();
            } else {
                shouldCompose = true;
                otherNamePublicId = PublicIds.newRandom();
            }

            if (shouldCompose) {
                EntityComposer<ObservableSemanticVersion.Editable, ObservableSemantic> synComposer =
                        composer.composeSemantic(otherNamePublicId, observablePattern, TinkarTerm.DESCRIPTION_PATTERN);
                ObservableSemanticVersion.Editable synEditable = synComposer.getEditableVersion();
                synEditable.getEditableField(0).setObjectValue(otherName.getLanguage());
                synEditable.getEditableField(1).setObjectValue(otherName.getNameText());
                synEditable.getEditableField(2).setObjectValue(otherName.getCaseSignificance());
                synEditable.getEditableField(3).setObjectValue(REGULAR_NAME_DESCRIPTION_TYPE);
                synComposer.save();

                // Compose US Dialect for the synonym (only for new synonyms)
                if (!isEdit || otherName.getSemanticPublicId() == null) {
                    PublicId synDialectPublicId = PublicIds.newRandom();
                    EntityComposer<ObservableSemanticVersion.Editable, ObservableSemantic> synDialectComposer =
                            composer.composeSemantic(synDialectPublicId, synComposer.getEntity(), TinkarTerm.US_DIALECT_PATTERN);
                    synDialectComposer.getEditableVersion().getEditableField(0).setObjectValue(ACCEPTABLE);
                    synDialectComposer.save();
                }
            }
        }

        // Commit all composed entities
        composer.commit();

        setPropertyValue(ViewModelKey.PATTERN, Pattern.make(null, patternPublicId));
        return true;
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
        return getPropertyValue(ViewModelKey.VIEW_PROPERTIES);
    }
}
