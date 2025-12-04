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
package dev.ikm.tinkar.integration.provider.mvstore;


import dev.ikm.tinkar.common.service.PrimitiveData;
import dev.ikm.tinkar.common.util.time.Stopwatch;
import dev.ikm.tinkar.entity.EntityCountSummary;
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

/**
 * Concerned that MVStore may not be "bullet proof" based on this exception. Will watch, and save for posterity.
 * <p>
 * Mar 28, 2021 9:41:28 AM dev.ikm.tinkar.integration.provider.mvstore.MVStoreProviderTest teardownSuite
 * INFO: teardownSuite
 * <p>
 * java.lang.IllegalStateException: Chunk 77 not found [1.4.200/9]
 * <p>
 * at org.h2.mvstore.DataUtils.newIllegalStateException(DataUtils.java:950)
 * at org.h2.mvstore.MVStore.getChunk(MVStore.java:1230)
 * at org.h2.mvstore.MVStore.readBufferForPage(MVStore.java:1214)
 * at org.h2.mvstore.MVStore.readPage(MVStore.java:2209)
 * at org.h2.mvstore.MVMap.readPage(MVMap.java:672)
 * at org.h2.mvstore.Page$NonLeaf.getChildPage(Page.java:1043)
 * at org.h2.mvstore.Cursor.hasNext(Cursor.java:53)
 * at org.h2.mvstore.MVMap$2$1.hasNext(MVMap.java:802)
 * at java.base/java.util.concurrent.ConcurrentMap.forEach(ConcurrentMap.java:112)
 * at dev.ikm.tinkar.provider.mvstore.MVStoreProvider.forEachSemanticNidForComponentOfType(MVStoreProvider.java:98)
 */
@TestInstance(TestInstance.Lifecycle.PER_CLASS)
@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
class MVStoreProviderIT {

    private static final Logger LOG = LoggerFactory.getLogger(MVStoreProviderIT.class);
    private static final File DATASTORE_ROOT = TestConstants.createFilePathInTargetFromClassName.apply(MVStoreProviderIT.class);

    @BeforeAll
    static void beforeAll() {
        TestHelper.startDataBase(DataStore.MV_STORE, DATASTORE_ROOT);
    }

    @Test
    @Order(1)
    public void loadChronologies() {
        File file = TestConstants.PB_STARTER_DATA_REASONED;
        LoadEntitiesFromProtobufFile loadProto = new LoadEntitiesFromProtobufFile(file);
        EntityCountSummary count = loadProto.compute();
        LOG.info(count + " entitles loaded from file: " + loadProto.summarize() + "\n\n");
    }

    @Test
    @Order(2)
    public void count() {
        if (!PrimitiveData.running()) {
            LOG.info("Reloading MVStoreProvider");
            Stopwatch reloadStopwatch = new Stopwatch();
            PrimitiveData.start();
            LOG.info("Reloading in: " + reloadStopwatch.durationString() + "\n\n");
        }
        EntityProcessor processor = new EntityCounter();
        PrimitiveData.get().forEach(processor);
        LOG.info("MVS Sequential count: \n" + processor.report() + "\n\n");
        processor = new EntityCounter();
        PrimitiveData.get().forEachParallel(processor);
        LOG.info("MVS Parallel count: \n" + processor.report() + "\n\n");
        processor = new EntityRealizer();
        PrimitiveData.get().forEach(processor);
        LOG.info("MVS Sequential realization: \n" + processor.report() + "\n\n");
        processor = new EntityRealizer();
        PrimitiveData.get().forEachParallel(processor);
        LOG.info("MVS Parallel realization: \n" + processor.report() + "\n\n");
        processor = new EntityRealizer();
        PrimitiveData.get().forEach(processor);
        LOG.info("MVS Sequential realization: \n" + processor.report() + "\n\n");
        processor = new EntityRealizer();
        PrimitiveData.get().forEachParallel(processor);
        LOG.info("MVS Parallel realization: \n" + processor.report() + "\n\n");
    }

    @Test
    @Order(3)
    public void openAndClose() {
        if (PrimitiveData.running()) {
            Stopwatch closingStopwatch = new Stopwatch();
            PrimitiveData.stop();
            closingStopwatch.end();
            LOG.info("MVS Closed in: " + closingStopwatch.durationString() + "\n\n");
        }
        LOG.info("Reloading MVStoreProvider");
        Stopwatch reloadStopwatch = new Stopwatch();
        PrimitiveData.start();
        LOG.info("MVS Reloading in: " + reloadStopwatch.durationString() + "\n\n");
        EntityProcessor processor = new EntityCounter();
        PrimitiveData.get().forEach(processor);
        LOG.info("MVS Sequential count: \n" + processor.report() + "\n\n");
        processor = new EntityCounter();
        PrimitiveData.get().forEachParallel(processor);
        LOG.info("MVS Parallel count: \n" + processor.report() + "\n\n");
        processor = new EntityRealizer();
        PrimitiveData.get().forEach(processor);
        LOG.info("MVS Sequential realization: \n" + processor.report() + "\n\n");
        processor = new EntityRealizer();
        PrimitiveData.get().forEachParallel(processor);
        LOG.info("MVS Parallel realization: \n" + processor.report() + "\n\n");
        processor = new EntityRealizer();
        PrimitiveData.get().forEach(processor);
        LOG.info("MVS Sequential realization: \n" + processor.report() + "\n\n");
        processor = new EntityRealizer();
        PrimitiveData.get().forEachParallel(processor);
        LOG.info("MVS Parallel realization: \n" + processor.report() + "\n\n");
    }

}
