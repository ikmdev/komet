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
import static dev.ikm.tinkar.terms.TinkarTerm.DESCRIPTION_NOT_CASE_SENSITIVE;
import static dev.ikm.tinkar.terms.TinkarTerm.DESCRIPTION_PATTERN;
import static dev.ikm.tinkar.terms.TinkarTerm.ENGLISH_LANGUAGE;
import static dev.ikm.tinkar.terms.TinkarTerm.PREFERRED;
import static dev.ikm.tinkar.terms.TinkarTerm.REGULAR_NAME_DESCRIPTION_TYPE;
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
import dev.ikm.tinkar.entity.EntityService;
import dev.ikm.tinkar.terms.State;
import dev.ikm.tinkar.terms.TinkarTerm;
import javafx.application.Platform;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.util.UUID;

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
            try {
                PatternViewModelTest testHarness = new PatternViewModelTest();

                // create the pattern definition
                PublicId patternPublicId = testHarness.createPattern();
                LOG.info(patternPublicId.toString());
                composer.commitSession(session);

            } catch (Throwable e) {
                e.printStackTrace();
                tearDownAfter();
                System.exit(0);
            }
            tearDownAfter();
            System.exit(0);
        });
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
