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

import dev.ikm.tinkar.common.service.PrimitiveData;
import dev.ikm.tinkar.entity.EntityCountSummary;
import dev.ikm.tinkar.entity.export.ExportEntitiesToProtobufFile;
import dev.ikm.tinkar.entity.load.LoadEntitiesFromProtobufFile;
import dev.ikm.tinkar.entity.util.EntityCounter;
import dev.ikm.tinkar.entity.util.EntityProcessor;
import dev.ikm.tinkar.entity.util.EntityRealizer;
import dev.ikm.tinkar.integration.TestConstants;
import dev.ikm.tinkar.integration.helper.DataStore;
import dev.ikm.tinkar.integration.helper.TestHelper;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.MethodOrderer;
import org.junit.jupiter.api.Order;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestInstance;
import org.junit.jupiter.api.TestMethodOrder;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;

@TestInstance(TestInstance.Lifecycle.PER_CLASS)
@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
public class EphemeralProtobufIT {

    private static final Logger LOG = LoggerFactory.getLogger(EphemeralProtobufIT.class);

    @BeforeAll
    public static void beforeAll() {
        TestHelper.startDataBase(DataStore.EPHEMERAL_STORE);
    }

    @Test
    @Order(1)
    public void loadProtoBufFile() {
        File file = TestConstants.PB_STARTER_DATA_REASONED;
        LoadEntitiesFromProtobufFile loadProto = new LoadEntitiesFromProtobufFile(file);
        EntityCountSummary entityCountSummary = loadProto.compute();
        LOG.info(entityCountSummary.getTotalCount() + " entities loaded from file: " + loadProto.summarize() + "\n\n");
    }

    @Test
    @Order(2)
    public void countEntities(){
        EntityProcessor processor = new EntityCounter();
        PrimitiveData.get().forEach(processor);
        LOG.info("EPH Sequential count: \n" + processor.report() + "\n\n");
        processor = new EntityCounter();
        PrimitiveData.get().forEachParallel(processor);
        LOG.info("EPH Parallel count: \n" + processor.report() + "\n\n");
        processor = new EntityRealizer();
        PrimitiveData.get().forEach(processor);
        LOG.info("EPH Sequential realization: \n" + processor.report() + "\n\n");
        processor = new EntityRealizer();
        PrimitiveData.get().forEachParallel(processor);
        LOG.info("EPH Parallel realization: \n" + processor.report() + "\n\n");
        processor = new EntityRealizer();
        PrimitiveData.get().forEach(processor);
        LOG.info("EPH Sequential realization: \n" + processor.report() + "\n\n");
        processor = new EntityRealizer();
        PrimitiveData.get().forEachParallel(processor);
        LOG.info("EPH Parallel realization: \n" + processor.report() + "\n\n");
    }

    @Test
    @Order(3)
    public void exportEntitiesToProtobuf() {
        File file = TestConstants.PB_TEST_FILE;
        ExportEntitiesToProtobufFile exportEntitiesToProtobufFile = new ExportEntitiesToProtobufFile(file);
        EntityCountSummary entityCountSummary = exportEntitiesToProtobufFile.compute();
        LOG.info(entityCountSummary.getTotalCount() + " entities exported from file");
    }

}
