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
package dev.ikm.komet.app.test;

import static dev.ikm.tinkar.terms.EntityProxy.Concept;
import static dev.ikm.tinkar.terms.EntityProxy.Pattern;
import static dev.ikm.tinkar.terms.TinkarTerm.DEFINITION_DESCRIPTION_TYPE;
import static dev.ikm.tinkar.terms.TinkarTerm.DESCRIPTION_NOT_CASE_SENSITIVE;
import static dev.ikm.tinkar.terms.TinkarTerm.DESCRIPTION_PATTERN;
import static dev.ikm.tinkar.terms.TinkarTerm.ENGLISH_LANGUAGE;
import static dev.ikm.tinkar.terms.TinkarTerm.PREFERRED;
import static dev.ikm.tinkar.terms.TinkarTerm.REGULAR_NAME_DESCRIPTION_TYPE;
import dev.ikm.komet.framework.view.ViewProperties;
import dev.ikm.komet.framework.window.WindowSettings;
import dev.ikm.komet.kview.mvvm.model.DescrName;
import dev.ikm.komet.kview.mvvm.model.PatternField;
import dev.ikm.komet.preferences.KometPreferences;
import dev.ikm.komet.preferences.KometPreferencesImpl;
import dev.ikm.tinkar.common.id.PublicId;
import dev.ikm.tinkar.common.id.PublicIds;
import dev.ikm.tinkar.common.service.CachingService;
import dev.ikm.tinkar.common.service.PrimitiveData;
import dev.ikm.tinkar.common.service.ServiceKeys;
import dev.ikm.tinkar.common.service.ServiceProperties;
import dev.ikm.tinkar.composer.Composer;
import dev.ikm.tinkar.composer.Session;
import dev.ikm.tinkar.composer.assembler.PatternAssembler;
import dev.ikm.tinkar.composer.assembler.SemanticAssembler;
import dev.ikm.tinkar.composer.template.FullyQualifiedName;
import dev.ikm.tinkar.composer.template.USDialect;
import dev.ikm.tinkar.coordinate.stamp.calculator.Latest;
import dev.ikm.tinkar.coordinate.view.calculator.ViewCalculator;
import dev.ikm.tinkar.entity.Entity;
import dev.ikm.tinkar.entity.EntityService;
import dev.ikm.tinkar.entity.EntityVersion;
import dev.ikm.tinkar.entity.FieldDefinitionRecord;
import dev.ikm.tinkar.entity.PatternRecord;
import dev.ikm.tinkar.entity.PatternVersionRecord;
import dev.ikm.tinkar.terms.ConceptFacade;
import dev.ikm.tinkar.terms.State;
import dev.ikm.tinkar.terms.TinkarTerm;
import javafx.application.Platform;
import org.eclipse.collections.api.list.ImmutableList;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.util.UUID;
import java.util.concurrent.atomic.AtomicInteger;

public class PatternViewModelTest {

    private static final Logger LOG = LoggerFactory.getLogger(PatternViewModelTest.class);

    //@BeforeAll
    public static void setUpBefore() {
        LOG.info("Clear caches");
        //File dataStore = new File(System.getProperty("user.home") + "/Solor/LoincFullDataSet_v2_77_8-21-2024");
        File dataStore = new File(System.getProperty("user.home") + "/Solor/September2024_ConnectathonDataset_v1");
        CachingService.clearAll();
        LOG.info("Setup Ephemeral Suite: " + LOG.getName());
        LOG.info(ServiceProperties.jvmUuid());
        ServiceProperties.set(ServiceKeys.DATA_STORE_ROOT, dataStore);
        PrimitiveData.selectControllerByName("Open SpinedArrayStore");

        PrimitiveData.start();

        // set up the composer session
        composer = new Composer("Test Save Pattern Definition");
        State status = State.ACTIVE;


        Concept author = TinkarTerm.USER;
        Concept module = TinkarTerm.MODULE;
        Concept path = TinkarTerm.DEVELOPMENT_PATH;

        // when we don't provide a time for the STAMP, it is taken care of by the framework
        session = composer.open(status, author, module, path);
    }


    public static Composer composer;

    public static Session session;

    //@AfterAll
    public static void tearDownAfter() {
        PrimitiveData.stop();
    }

    public static void main(String[] args) {

        Platform.startup(() -> {
            setUpBefore();
            PatternViewModelTest testHarness = new PatternViewModelTest();
            testHarness.summonPatternTest();
           // create the pattern definition
            PublicId patternPublicId = testHarness.createPattern();
            LOG.info(patternPublicId.toString());
            composer.commitSession(session);
            tearDownAfter();
            System.exit(0);
        });
    }

    public void summonPatternTest () {
        // Some UUIDS that can be used for testing:
        // 561f817a-130e-5e56-984d-910e9991558c
        //c6553e16-dad5-51ff-a697-85b63d659fd3
        //91b9b62d-477c-493a-b42e-a34f92b2d27c
        //922697f7-36ba-4afc-9dd5-f29d54b0fdec
        Entity entity = EntityService.get().getEntityFast(UUID.fromString("922697f7-36ba-4afc-9dd5-f29d54b0fdec"));
        ViewProperties viewProperties = createViewProperties();
        ViewCalculator viewCalculator = viewProperties.calculator();
        LOG.info( " SUMMON Pattern: " + entity);
        //Load the Pattern Fields.
        ImmutableList<FieldDefinitionRecord> fieldDefinitionRecords = ((PatternRecord) entity).versions().getLastOptional().get().fieldDefinitions();
        fieldDefinitionRecords.stream().forEachOrdered( fieldDefinitionForEntity ->
        {
            EntityVersion latest = (EntityVersion) viewCalculator.latest(fieldDefinitionForEntity.meaning()).get();
            PatternField patternField = new PatternField(fieldDefinitionForEntity.meaning().description(), fieldDefinitionForEntity.dataType(),
            fieldDefinitionForEntity.purpose(), fieldDefinitionForEntity.meaning(), "", latest.stamp());
            LOG.info("Pattern FIELDS: " + patternField.displayName());
        });

        Latest<EntityVersion> latestEntityVersion = viewCalculator.latest(entity);
        EntityVersion entityVersion = latestEntityVersion.get();

        Entity purposeEntity = ((PatternVersionRecord) entityVersion).semanticPurpose();
        Entity meaningEntity = ((PatternVersionRecord) entityVersion).semanticMeaning();

        LOG.info("Purpose: " +purposeEntity);
        LOG.info("Meaning: " +meaningEntity);

        AtomicInteger counter = new AtomicInteger(0);
        viewCalculator.getFieldForSemanticWithMeaning(entity.nid(), TinkarTerm.MEANING.nid());
        viewCalculator.forEachSemanticVersionForComponentOfPattern(entity.nid(), TinkarTerm.DESCRIPTION_PATTERN.nid(),
                (semanticEntityVersion,  entityVersion1, patternEntityVersion) -> {

            ConceptFacade language = (ConceptFacade) semanticEntityVersion.fieldValues().get(0);
            String string = (String) semanticEntityVersion.fieldValues().get(1);
            ConceptFacade caseSignificance = (ConceptFacade) semanticEntityVersion.fieldValues().get(2);
            ConceptFacade descriptionType = (ConceptFacade) semanticEntityVersion.fieldValues().get(3);
            DescrName descrName = new DescrName(null, string, descriptionType,
            Entity.getFast(caseSignificance.nid()), Entity.getFast(semanticEntityVersion.state().nid()), Entity.getFast(semanticEntityVersion.module().nid()),
            Entity.getFast(language.nid()), semanticEntityVersion.publicId());
            System.out.println(descrName.toString());
                if(PublicId.equals(descriptionType.publicId(),TinkarTerm.FULLY_QUALIFIED_NAME_DESCRIPTION_TYPE.publicId())){
                    // set Property Value FQN D
                    LOG.info(" Add to FQN : " + descrName.getNameText());
                } else if(PublicId.equals(descriptionType.publicId(), REGULAR_NAME_DESCRIPTION_TYPE.publicId())) {
                    // add to list.
                    LOG.info(" Add to Other Name : " + descrName.getNameText());
                } else if (PublicId.equals(descriptionType.publicId(), DEFINITION_DESCRIPTION_TYPE.publicId())) {
                    LOG.info(" Add to Definition Name : " + descrName.getNameText());

                }
        });
    }
    public static ViewProperties createViewProperties() {
        // TODO how do we get a viewProperties?
        KometPreferences appPreferences = KometPreferencesImpl.getConfigurationRootPreferences();
        KometPreferences windowPreferences = appPreferences.node("main-komet-window");
        WindowSettings windowSettings = new WindowSettings(windowPreferences);
        ViewProperties viewProperties = windowSettings.getView().makeOverridableViewProperties();
        return viewProperties;
    }

    /**
     * test creating a pattern using the composer API
     */
    public PublicId createPattern() {

        // a06158ff-e08a-5d7d-bcfa-6cbfdb138910
        //Concept patternMeaning = Concept.make(UUID.randomUUID().toString()); // find a meaning
        Concept patternMeaning = EntityService.get().getEntityFast(UUID.fromString("a06158ff-e08a-5d7d-bcfa-6cbfdb138910")).toProxy();

        //c3dffc48-6493-54df-a2f0-14be8ba03091
        //Concept patternPurpose = Concept.make(UUID.randomUUID().toString()); // find a purpose
        Concept patternPurpose = EntityService.get().getEntityFast(UUID.fromString("c3dffc48-6493-54df-a2f0-14be8ba03091")).toProxy();

        PublicId patternPublicId = PublicIds.newRandom();
        Pattern pattern = Pattern.make(patternPublicId);




        // the composer handles saving to an uncommitted stamp
        // it is un-committed until you say commit
        session.compose((PatternAssembler patternAssembler) -> patternAssembler
                .pattern(pattern)
                .meaning(patternMeaning)
                .purpose(patternPurpose)
                .fieldDefinition(patternPurpose.toProxy(), patternMeaning.toProxy(), TinkarTerm.LONG, 0)
                .attach((FullyQualifiedName fqn) -> fqn
                        .language(ENGLISH_LANGUAGE)
                        .text("FQN for Pattern")
                        .caseSignificance(DESCRIPTION_NOT_CASE_SENSITIVE))
        );


        session.compose((SemanticAssembler semanticAssembler) -> semanticAssembler
                .reference(pattern)
                .pattern(DESCRIPTION_PATTERN)
                .fieldValues(fieldValues -> fieldValues
                        .with(ENGLISH_LANGUAGE)
                        .with("Pattern Other Name")
                        .with(DESCRIPTION_NOT_CASE_SENSITIVE)
                        .with(REGULAR_NAME_DESCRIPTION_TYPE))
                .attach((USDialect dialect) -> dialect
                        .acceptability(PREFERRED)));

        // versions should be only 1
        assert(EntityService.get().getEntityFast(pattern.asUuidList()).versions().size() == 1);

        return patternPublicId;
    }

}
