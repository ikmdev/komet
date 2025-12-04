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
package dev.ikm.tinkar.integration.provider.ephemeral;

import dev.ikm.tinkar.entity.export.ExportEntitiesToProtobufFile;
import dev.ikm.tinkar.entity.load.LoadEntitiesFromProtobufFile;
import dev.ikm.tinkar.integration.TestConstants;
import dev.ikm.tinkar.integration.helper.DataStore;
import dev.ikm.tinkar.integration.helper.TestHelper;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Order;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestInstance;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;

@TestInstance(TestInstance.Lifecycle.PER_CLASS)
public class ProtobufPerformanceIT {

    private static final Logger LOG = LoggerFactory.getLogger(ProtobufRoundTripIT.class);

    @BeforeAll
    public void beforeAll() {
        TestHelper.startDataBase(DataStore.EPHEMERAL_STORE);
    }

    @Test
    @Order(1)
    public void roundTripPerformanceTest() {
        File starterDataFile = TestConstants.PB_STARTER_DATA_REASONED;
        long loadTimeBefore = System.currentTimeMillis();
        long expectedEntityCount = new LoadEntitiesFromProtobufFile(starterDataFile).compute().getTotalCount();
        long loadElapsedMillis = System.currentTimeMillis() - loadTimeBefore;
        System.out.println("[1] The size of the original Protobuf file is: " + starterDataFile.length() + " bytes long.");
        System.out.println("[1] The count of Entities loaded is: " + expectedEntityCount);
        System.out.println("[1] The initial Load operation took " + loadElapsedMillis + " milliseconds.");

        File roundTripFile = TestConstants.PB_PERFORMANCE_TEST_FILE;
        long exportTimeBefore = System.currentTimeMillis();
        long actualProtobufExportCount = new ExportEntitiesToProtobufFile(roundTripFile).compute().getTotalCount();
        long exportElapsedMillis = System.currentTimeMillis() - exportTimeBefore;
        //Printing out File size for this transformation
        System.out.println("[2] The size of the file is: " + roundTripFile.length() + " bytes long.");
        System.out.println("[2] The count of Entities exported is: " + actualProtobufExportCount);
        System.out.println("[2] The Export operation took: " + exportElapsedMillis + " milliseconds.");

        //Stopping and starting the database
        TestHelper.stopDatabase();
        TestHelper.startDataBase(DataStore.EPHEMERAL_STORE);

        long loadRoundTripFileTimeBefore = System.currentTimeMillis();
        long actualProtobufImportCount = new LoadEntitiesFromProtobufFile(roundTripFile).compute().getTotalCount();
        long loadRoundTripFileElapsedMillis = System.currentTimeMillis() - loadRoundTripFileTimeBefore;
        System.out.println("[3] The count of Entities loaded is: " + actualProtobufImportCount);
        System.out.println("[3] The second Load operation took: " + loadRoundTripFileElapsedMillis + " milliseconds.");
    }

}
