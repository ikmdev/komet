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

import dev.ikm.tinkar.entity.EntityCountSummary;
import dev.ikm.tinkar.entity.export.ExportEntitiesToProtobufFile;
import dev.ikm.tinkar.entity.load.LoadEntitiesFromProtobufFile;
import dev.ikm.tinkar.integration.TestConstants;
import dev.ikm.tinkar.integration.helper.DataStore;
import dev.ikm.tinkar.integration.helper.TestHelper;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestInstance;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.io.IOException;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;



@TestInstance(TestInstance.Lifecycle.PER_CLASS)
public class ProtobufRoundTripIT {

    private static final Logger LOG = LoggerFactory.getLogger(ProtobufRoundTripIT.class);

    @BeforeAll
    public void beforeAll() {
        TestHelper.startDataBase(DataStore.EPHEMERAL_STORE);
    }

    /**
     * 0. Start new database (ephemeral)
     * 1. Dto zip file -> entity store
     * 2. entity store -> Protobuf objects file
     *    // (is there version merging going on?)
     * 3. stop and start new database
     * 4. Protobuf objects file -> entity store.
     * 5. Stop database.
     *
     * @throws IOException
     */
    @Test
    public void roundTripTest() throws IOException {
        // Given initial DTO data
        File file = TestConstants.PB_STARTER_DATA_REASONED;
        LoadEntitiesFromProtobufFile loadProto = new LoadEntitiesFromProtobufFile(file);
        EntityCountSummary count = loadProto.compute();
        LOG.info(count + " entitles loaded from file: " + loadProto.summarize() + "\n\n");

        // When we export Entities data to protobuf
        File fileProtobuf = TestConstants.PB_ROUNDTRIP_TEST_FILE;
        boolean pbZipFileSuccess = true;
        if (fileProtobuf.exists()) {
            pbZipFileSuccess = fileProtobuf.delete();
        }
        pbZipFileSuccess = fileProtobuf.createNewFile();
        if (!pbZipFileSuccess) {
            throw new RuntimeException("Round trip test has failed setup to begin test. Unable to delete or create " + fileProtobuf.getName() + " to begin.");
        }
        ExportEntitiesToProtobufFile exportEntitiesToProtobufFile = new ExportEntitiesToProtobufFile(fileProtobuf);
        long actualProtobufExportCount = exportEntitiesToProtobufFile.compute().getTotalCount();
        LOG.info("Entities exported to protobuf: " + actualProtobufExportCount);

        TestHelper.stopDatabase();
        TestHelper.startDataBase(DataStore.EPHEMERAL_STORE);

        // When we import protobuf data into entities
        LoadEntitiesFromProtobufFile loadEntitiesFromProtobufFile = new LoadEntitiesFromProtobufFile(fileProtobuf);
        long actualProtobufImportCount = loadEntitiesFromProtobufFile.compute().getTotalCount();
        LOG.info("Entities loaded from protobuf: " + actualProtobufImportCount);

        // Then all imported and exported entities counts should match
        boolean boolEntityCount = count.getTotalCount() == actualProtobufExportCount && count.getTotalCount() == actualProtobufImportCount;
        assertTrue(count.getTotalCount() > 0, "Imported DTO count should be greater than zero.");
        assertTrue(actualProtobufExportCount > 0, "Exported Protobuf count should be greater than zero.");
        assertTrue(actualProtobufImportCount > 0, "Imported Protobuf count should be greater than zero.");
        assertEquals(count.getTotalCount(), actualProtobufExportCount, "Entity count and Protobuf Export count do not match.");
        assertEquals(count.getTotalCount(), actualProtobufImportCount, "Entity count and Protobuf Import count do not match.");
        assertTrue(boolEntityCount, "Counts in round-trip do not match.");
    }
}
