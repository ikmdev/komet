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
import static dev.ikm.tinkar.terms.TinkarTerm.DESCRIPTION_NOT_CASE_SENSITIVE;
import static dev.ikm.tinkar.terms.TinkarTerm.ENGLISH_LANGUAGE;
import dev.ikm.tinkar.common.service.CachingService;
import dev.ikm.tinkar.common.service.PrimitiveData;
import dev.ikm.tinkar.common.service.ServiceKeys;
import dev.ikm.tinkar.common.service.ServiceProperties;
import dev.ikm.tinkar.composer.Composer;
import dev.ikm.tinkar.composer.Session;
import dev.ikm.tinkar.composer.assembler.PatternAssembler;
import dev.ikm.tinkar.composer.template.FullyQualifiedName;

import dev.ikm.tinkar.terms.State;
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
        File dataStore = new File(System.getProperty("user.home") + "/Solor/LoincFullDataSet_v2_77_8-21-2024");
        CachingService.clearAll();
        LOG.info("Setup Ephemeral Suite: " + LOG.getName());
        LOG.info(ServiceProperties.jvmUuid());
        ServiceProperties.set(ServiceKeys.DATA_STORE_ROOT, dataStore);
        PrimitiveData.selectControllerByName("Open SpinedArrayStore");

        PrimitiveData.start();
    }

    //@AfterAll
    public static void tearDownAfter() {
        PrimitiveData.stop();
    }

    public static void main(String[] args) {

        Platform.startup(() -> {
            setUpBefore();
            try {
                PatternViewModelTest testHarness = new PatternViewModelTest();
                testHarness.savePattern();
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
    public void savePattern() {
        Composer composer = new Composer("Test Save Pattern Definition");

        State status = State.ACTIVE;
        long time = Long.MIN_VALUE;

        Concept author = Concept.make(UUID.randomUUID().toString());
        Concept module = Concept.make(UUID.randomUUID().toString());
        Concept path = Concept.make(UUID.randomUUID().toString());

        Concept patternMeaning = Concept.make(UUID.randomUUID().toString()); // find a meaning
        Concept patternPurpose = Concept.make(UUID.randomUUID().toString());

        Session session = composer.open(status, time, author, module, path);

        Concept fieldMeaning = Concept.make(UUID.randomUUID().toString());
        Concept fieldPurpose = Concept.make(UUID.randomUUID().toString());
        Concept fieldDataType = Concept.make(UUID.randomUUID().toString());
        session.compose((PatternAssembler patternAssembler) -> patternAssembler
                .meaning(patternMeaning)
                .purpose(patternPurpose)
                .fieldDefinition(fieldMeaning, fieldPurpose, fieldDataType)
                .attach((FullyQualifiedName fqn) -> fqn
                        .language(ENGLISH_LANGUAGE)
                        .text("FQN for Pattern")
                        .caseSignificance(DESCRIPTION_NOT_CASE_SENSITIVE))
        );

        composer.commitSession(session);
    }
}
