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
import dev.ikm.tinkar.entity.Entity;
import dev.ikm.tinkar.entity.EntityService;
import dev.ikm.tinkar.entity.EntityVersion;
import dev.ikm.tinkar.entity.PatternEntity;
import dev.ikm.tinkar.entity.PatternEntityVersion;
import dev.ikm.tinkar.entity.PatternRecord;
import dev.ikm.tinkar.entity.PatternRecordBuilder;
import dev.ikm.tinkar.terms.EntityProxy;
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

        // set up the composer session
        composer = new Composer("Test Save Pattern Definition");
        State status = State.ACTIVE;
        long uncommittedStampTime = Long.MAX_VALUE; // is this correct?

        Concept author = Concept.make(UUID.randomUUID().toString());
        Concept module = Concept.make(UUID.randomUUID().toString());
        Concept path = Concept.make(UUID.randomUUID().toString());
        session = composer.open(status, uncommittedStampTime, author, module, path);
    }


    public static Composer composer;

    public static Session session;

    //@AfterAll
    public static void tearDownAfter() {
        // perform the commit
        // submit... (really publish??? need to revisit this)

        PrimitiveData.stop();
    }

    public static void main(String[] args) {

        Platform.startup(() -> {
            setUpBefore();
            try {
                PatternViewModelTest testHarness = new PatternViewModelTest();

                // create the pattern definition; do not commit yet
                PublicId patternPublicId = testHarness.setPatternDefintion();

                // create the pattern description; do not commit yet
                testHarness.setPatternDescription(patternPublicId);

                // create the pattern fields; do not commit yet
                testHarness.setPatternFields(patternPublicId);

                composer.commitSession(session);

                Pattern pattern = (Pattern) EntityService.get().getEntity(patternPublicId.asUuidList()).get();
                int patternVersionCount = EntityService.get().getEntityFast(pattern.asUuidList()).versions().size();
                System.out.println("***************************************");
                System.out.println("***** Pattern version count = %d *******".formatted(patternVersionCount));
                System.out.println("***************************************");

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
    public PublicId setPatternDefintion() {

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
        );
        //session.cancel(); // should we cancel?


        // versions should be only 1
        assert(EntityService.get().getEntityFast(pattern.asUuidList()).versions().size() == 1);

        return patternPublicId;

        // commit vs uncommitted = the timestamp value on the STAMP. long max vs a real date
        //      you can have as many concepts and patters as you want pointing to that stamp
        //      when you say commit it changes that timestamp from long.max to that point in time

        // if you create a stamp with the same params in the same transaction, then it will be the same stamp

        // CANCEL the temp transaction and re-save to accomplish

        //Pattern pattern = ?

        // a pattern has fields IN IT... they are not a separate semantic
        // whereas a concept does NOT have semantics... a semantic that is part of a concept will point to the concept
        // the pattern field definitions are essentially a list of field definitions that live inside the pattern
        // you never want to write pattern definitions for the same pattern that have different numbers of field definitions
        //      e.g. A Person Pattern
        //          1) they have a first name and last name field... uncommitted transaction...(both Strings)
        //          (assuming we are in the uncommitted transaction...)
        //              ^^ we are writing an uncommitted version when we do this
        //                  * you have to make sure that we take that uncommitted version, save off to memory, cancel that uncommitted transaction
        //                  * and create a new transaction
        //                  - goal is to summon the pattern window again and resume working on creating it
        //          2) then add another pattern version by adding an additional field, e.g. middle initial <- BAD
        //          3) can the user reorder the fields ? e.g. last name, first name
        //              ^^^ 2 and 3 are both possible, new pattern versions + different number or order
        //              shouldn't do this, if you have written semantics against this it will break them
        //              if you make new pattern version you have to trust the existing viewCalculator queries throughout the codebase
        //          * a work-around is to retire the old pattern and create a new pattern if you want to reorder or change the
        //              the fields


        // uncommitted transactions are lost on restart because they are java objects... on restart of komet you will lose that transaction
        // and accessing the stamp
    }

    private void setPatternDescription(PublicId patternPublicId) {

        Pattern pattern = EntityService.get().getEntity(patternPublicId.asUuidList()).get().toProxy();


        session.compose((PatternAssembler patternAssembler) -> patternAssembler
                        .pattern(pattern)
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

    }

    private void setPatternFields(PublicId patterPublicId) {

    }
}
