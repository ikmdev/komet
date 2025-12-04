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
package dev.ikm.tinkar.integration.helper;

import dev.ikm.tinkar.common.service.CachingService;
import dev.ikm.tinkar.common.service.PrimitiveData;
import dev.ikm.tinkar.common.service.ServiceKeys;
import dev.ikm.tinkar.common.service.ServiceProperties;
import dev.ikm.tinkar.entity.EntityCountSummary;
import dev.ikm.tinkar.entity.load.LoadEntitiesFromProtobufFile;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;

public class TestHelper {
    private static final Logger LOG = LoggerFactory.getLogger(TestHelper.class);

    public static void startDataBase(DataStore dataStore, File fileDataStore) {
        if (fileDataStore.exists()) {
            LOG.warn("Datastore {} already exists. Loading this datastore may impact test results.\n" +
                    "Consider leveraging Maven's clean lifecycle phase or `FileUtil.recursiveDelete` during test setup.", fileDataStore.getName());
        }
        CachingService.clearAll();
        LOG.info("Cleared caches");
        ServiceProperties.set(ServiceKeys.DATA_STORE_ROOT, fileDataStore);
        startDataController(dataStore);
    }

    public static void startDataBase(DataStore dataStore) {
        CachingService.clearAll();
        LOG.info("Cleared caches");
        startDataController(dataStore);
    }

    private static void startDataController(DataStore dataStore) {
        LOG.info("JVM Version: " + System.getProperty("java.version"));
        LOG.info("JVM Name: " + System.getProperty("java.vm.name"));
        LOG.info(ServiceProperties.jvmUuid());
        PrimitiveData.selectControllerByName(dataStore.CONTROLLER_NAME);
        PrimitiveData.start();
    }

    public static EntityCountSummary loadDataFile(File dataFile){
        EntityCountSummary entityCountSummary = new LoadEntitiesFromProtobufFile(dataFile).compute();
        LOG.info("Import complete for {}. Imported {} Entities.", dataFile.getName(), entityCountSummary.getTotalCount());
        return entityCountSummary;
    }

    public static void stopDatabase() {
        LOG.info("Teardown Suite: " + LOG.getName());
        PrimitiveData.stop();
    }

}
