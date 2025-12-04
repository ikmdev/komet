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
package dev.ikm.tinkar.integration.changeSet;

import dev.ikm.tinkar.entity.EntityCountSummary;
import dev.ikm.tinkar.entity.EntityService;
import dev.ikm.tinkar.integration.TestConstants;
import dev.ikm.tinkar.integration.helper.DataStore;
import dev.ikm.tinkar.integration.helper.TestHelper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.io.File;
import java.util.concurrent.ExecutionException;

import static dev.ikm.tinkar.integration.TestConstants.createFilePathInTarget;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

class EntityServiceIT {

    private static final File SAP_DATASTORE_ROOT = TestConstants.createFilePathInTargetFromClassName.apply(EntityServiceIT.class);

    @BeforeEach
    void beforeEach() {
        TestHelper.startDataBase(DataStore.SPINED_ARRAY_STORE, SAP_DATASTORE_ROOT);
        TestHelper.loadDataFile(TestConstants.PB_STARTER_DATA_REASONED);
    }

    @Test
    @DisplayName("Test export entities in temporal range")
    void testExportEntitiesInSpecifiedTemporalRange() throws ExecutionException, InterruptedException {

        File exportFile = createFilePathInTarget.apply("data/testExportEntitiesInSpecifiedTemporalRange-pb.zip");
        exportFile.delete(); // Clean up previously created file

        // Starter Data is created at Premundane time which is Long.MIN_VALUE+1 epoch milliseconds
        long fromEpoch = Long.MIN_VALUE;
        long toEpoch = Long.MIN_VALUE+2;

        // Perform the temporal export operation
        EntityCountSummary summary = EntityService.get().temporalExport(exportFile, fromEpoch, toEpoch).get();

        // Verify the summary
        assertNotNull(summary);
        // Add your assertions here based on the expected summary values
        // For example:
        assertEquals(316, summary.conceptsCount());
        assertEquals(3223, summary.semanticsCount());
        assertEquals(18, summary.patternsCount());
        assertEquals(1, summary.stampsCount());
    }

}


