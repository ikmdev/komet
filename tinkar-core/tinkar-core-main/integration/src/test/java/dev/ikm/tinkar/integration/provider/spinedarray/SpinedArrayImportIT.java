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
package dev.ikm.tinkar.integration.provider.spinedarray;

import dev.ikm.tinkar.common.id.PublicIds;
import dev.ikm.tinkar.common.util.io.FileUtil;
import dev.ikm.tinkar.coordinate.Coordinates;
import dev.ikm.tinkar.coordinate.stamp.calculator.StampCalculatorWithCache;
import dev.ikm.tinkar.coordinate.view.ViewCoordinateRecord;
import dev.ikm.tinkar.coordinate.view.calculator.ViewCalculatorWithCache;
import dev.ikm.tinkar.entity.Entity;
import dev.ikm.tinkar.entity.EntityCountSummary;
import dev.ikm.tinkar.entity.EntityService;
import dev.ikm.tinkar.entity.load.LoadEntitiesFromProtobufFile;
import dev.ikm.tinkar.integration.TestConstants;
import dev.ikm.tinkar.integration.helper.DataStore;
import dev.ikm.tinkar.integration.helper.TestHelper;
import dev.ikm.tinkar.terms.TinkarTerm;
import org.junit.jupiter.api.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.net.URL;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotEquals;

@TestInstance(TestInstance.Lifecycle.PER_CLASS)
@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
class SpinedArrayImportIT {
    private static final Logger LOG = LoggerFactory.getLogger(SpinedArrayImportIT.class);
    private static final File DATASTORE_ROOT = TestConstants.createFilePathInTargetFromClassName.apply(
            SpinedArrayImportIT.class);

    @BeforeAll
    static void beforeAll() {
        FileUtil.recursiveDelete(DATASTORE_ROOT);
        TestHelper.startDataBase(DataStore.SPINED_ARRAY_STORE, DATASTORE_ROOT);
        File file = TestConstants.PB_EXAMPLE_DATA_REASONED;
        LoadEntitiesFromProtobufFile loadProto = new LoadEntitiesFromProtobufFile(file);
        EntityCountSummary count = loadProto.compute();
        LOG.info(count + " entitles loaded from file: " + loadProto.summarize() + "\n\n");
    }

    @AfterAll
    static void afterAll() {
        TestHelper.stopDatabase();
    }

    @Test
    public void givenFQNChangeSet_whenImported_thenViewCalcReturnsCorrectNewFQNText() {
        // Set up ViewCalculatorWithCache to replicate calculator for Komet window
        ViewCoordinateRecord viewCoord = Coordinates.View.DefaultView();
        ViewCalculatorWithCache viewCalc = ViewCalculatorWithCache.getCalculator(viewCoord);
        StampCalculatorWithCache stampCalc = StampCalculatorWithCache.getCalculator(viewCoord.stampCoordinate());

        // Query concept using Calculator.latest() (use both methods: latest(entity) AND latest(nid))
        String fqnBeforeFromEntityFacade = viewCalc.getFullyQualifiedDescriptionTextWithFallbackOrNid(TinkarTerm.ACTIVE_STATE);
        String fqnBeforeFromNid = viewCalc.getFullyQualifiedDescriptionTextWithFallbackOrNid(TinkarTerm.ACTIVE_STATE.nid());
        String otherBeforeFromNid = viewCalc.getRegularDescriptionText(TinkarTerm.ACTIVE_STATE.nid()).get();

        // Import pb file
        URL pbResourceUrl = getClass().getClassLoader().getResource("active-state-fqn-change-ike-cs.zip");
        assert pbResourceUrl != null;
        File pbFile = new File(pbResourceUrl.getFile());

        LoadEntitiesFromProtobufFile loadProto = new LoadEntitiesFromProtobufFile(pbFile);
        EntityCountSummary count = loadProto.compute();
        LOG.info(count + " entitles loaded from file: " + loadProto.summarize() + "\n\n");

        // Query again and compare results
        String fqnAfterFromEntityFacade = viewCalc.getFullyQualifiedDescriptionTextWithFallbackOrNid(TinkarTerm.ACTIVE_STATE);
        String fqnAfterFromNid = viewCalc.getFullyQualifiedDescriptionTextWithFallbackOrNid(TinkarTerm.ACTIVE_STATE.nid());

        // Adding sleep to account for delay in Real world user scenarios
        try {
            Thread.sleep(1000);
        } catch (InterruptedException e) {
            throw new RuntimeException(e);
        }

        String otherAfterFromNid = viewCalc.getRegularDescriptionText(TinkarTerm.ACTIVE_STATE.nid()).get();

        //STAMP Changes
        UUID changedStampUUID = UUID.fromString("3d296499-654f-566a-83ea-334cbec2c2e1");
        var changedStamp = EntityService.get().getStamp(changedStampUUID).get();
        int changedStampNid = Entity.nid(PublicIds.of(changedStampUUID));

        //Semantic Changes
        UUID changedSemanticUUID = UUID.fromString("65378077-2984-413d-a9f5-b43e1c611732");
        var changedSemantic = EntityService.get().getEntity(changedSemanticUUID).get();
        int changedSemanticNid = Entity.nid(PublicIds.of(changedSemanticUUID));

        var incorrectSemantic = viewCalc.latest(changedSemanticNid).get();
        var correctSemantic = viewCalc.latest(Entity.get(changedSemanticNid).get()).get();

        assertNotEquals(fqnBeforeFromEntityFacade, fqnAfterFromEntityFacade);
        assertNotEquals(fqnBeforeFromNid, fqnAfterFromNid);
        assertEquals(otherBeforeFromNid, otherAfterFromNid);
        assertEquals(incorrectSemantic, correctSemantic);
    }

    @Test
    public void givenOtherNameChangeSet_whenImported_thenViewCalcReturnsCorrectNewOtherNameText() {
        // Set up ViewCalculatorWithCache to replicate calculator for Komet window
        ViewCoordinateRecord viewCoord = Coordinates.View.DefaultView();
        ViewCalculatorWithCache viewCalc = ViewCalculatorWithCache.getCalculator(viewCoord);
        StampCalculatorWithCache stampCalc = StampCalculatorWithCache.getCalculator(viewCoord.stampCoordinate());

        // Query concept using Calculator.latest() (use both methods: latest(entity) AND latest(nid))
        String otherBeforeFromNid = viewCalc.getRegularDescriptionText(TinkarTerm.ACTIVE_STATE.nid()).get();

        // Import pb file
        URL pbResourceUrl = getClass().getClassLoader().getResource("active-state-other-change-ike-cs.zip");
        assert pbResourceUrl != null;
        File pbFile = new File(pbResourceUrl.getFile());

        LoadEntitiesFromProtobufFile loadProto = new LoadEntitiesFromProtobufFile(pbFile);
        EntityCountSummary count = loadProto.compute();
        LOG.info(count + " entitles loaded from file: " + loadProto.summarize() + "\n\n");

        //STAMP Changes
        UUID changedStampUUID = UUID.fromString("cf1e9214-42be-51fe-99f1-4eaf3e6c95ad");
        var changedStamp = EntityService.get().getStamp(changedStampUUID).get();
        int changedStampNid = Entity.nid(PublicIds.of(changedStampUUID));

        //Semantic Changes
        UUID changedSemanticUUID = UUID.fromString("101cea57-bfe4-4840-9cf4-da61ffb8463e");
        var changedSemantic = EntityService.get().getEntity(changedSemanticUUID).get();
        int changedSemanticNid = Entity.nid(PublicIds.of(changedSemanticUUID));

        var incorrectSemantic = viewCalc.latest(changedSemanticNid).get();  //Directly from the cache
        var correctSemantic = viewCalc.latest(Entity.get(changedSemanticNid).get()).get(); //iterating over the object

        // Adding sleep to account for delay in Real world user scenarios
        try {
            Thread.sleep(1000);
        } catch (InterruptedException e) {
            throw new RuntimeException(e);
        }

        // Query again and compare results
		String otherAfterFromNid = viewCalc.getRegularDescriptionText(TinkarTerm.ACTIVE_STATE.nid()).get();

        assertNotEquals(otherBeforeFromNid, otherAfterFromNid); //Wrong from a langcalc perspective
        assertEquals(incorrectSemantic, correctSemantic); //wrong from stampcalc perspective
    }

}
