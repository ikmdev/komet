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


import dev.ikm.komet.amplify.lidr.viewmodels.LidrViewModel;
import dev.ikm.komet.amplify.lidr.viewmodels.ViewModelHelper;
import dev.ikm.tinkar.common.id.PublicId;
import dev.ikm.tinkar.common.id.PublicIds;
import dev.ikm.tinkar.common.service.CachingService;
import dev.ikm.tinkar.common.service.PrimitiveData;
import dev.ikm.tinkar.common.service.ServiceKeys;
import dev.ikm.tinkar.common.service.ServiceProperties;
import dev.ikm.tinkar.component.graph.DiTree;
import dev.ikm.tinkar.component.graph.Vertex;
import dev.ikm.tinkar.provider.search.Searcher;
import dev.ikm.tinkar.terms.EntityFacade;
import dev.ikm.tinkar.terms.TinkarTerm;
import javafx.application.Platform;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;

public class LidrViewModelTest {

    private static final Logger LOG = LoggerFactory.getLogger(LidrViewModelTest.class);

//    public static final String EPHEMERAL_STORE_NAME = "Load Ephemeral Store";
//    public static final Function<String,File> createFilePathInTarget = (pathName) -> new File("%s/target/%s".formatted(System.getProperty("user.dir"), pathName));
//    public static final File TINK_TEST_FILE = createFilePathInTarget.apply("data/tinkar-test-dto-1.1.0.zip");

    //@BeforeAll
    public static void setUpBefore() {
        LOG.info("Clear caches");
        File dataStore = new File(System.getProperty("user.home") + "/Solor/lidr-data-starterdataV3-2024-05-01");
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

        Platform.startup(() ->{
            setUpBefore();
            try {
                new LidrViewModelTest().displayManufacturer();

            } catch (Throwable e) {
                e.printStackTrace();
                tearDownAfter();
                System.exit(0);
            }
            tearDownAfter();
            System.exit(0);
        });
    }

    // @Test
    public void displayManufacturer() {
        LidrViewModel lidrViewModel = new LidrViewModel();
        PublicId deviceId = PublicIds.of(UUID.fromString("ca616ab7-3f96-3d3f-90cf-f8b97351e884"));
        Searcher.getLidrRecordSemanticsFromTestKit(deviceId).forEach(System.out::println);
        Searcher.getResultConformanceFromTestKit(deviceId).forEach(System.out::println);

        PublicId covidAnalyteId = PublicIds.of(UUID.fromString("b8963659-ca41-30e1-8580-aefb70052104"));
        PublicId targetMatrixM1Id = PublicIds.of(UUID.fromString("1d9ab589-2fd1-331e-a79d-e9190c415d36"));
        PublicId resultConformanceId = PublicIds.of(UUID.fromString("bec2eb34-753c-3ed3-8f5f-99205d8447bc"));
        DiTree logicalDefinition = ViewModelHelper.findLatestLogicalDefinition(targetMatrixM1Id).get();
        System.out.println(logicalDefinition);
        for (Object o : logicalDefinition.vertexMap()) {
            if (o instanceof Vertex v) {
                if (v.meaning().equals(TinkarTerm.ROLE_TYPE)) {
                    System.out.println(v.propertyAsConcept(TinkarTerm.ROLE_TYPE));
                }
            }
        }

        EntityFacade manufacturerEntity = (EntityFacade) ViewModelHelper.findDeviceManufacturer(deviceId).get();
        System.out.println("### MANUFACTURER ENTITY: " + manufacturerEntity);
        System.out.println("### MANUFACTURER DESCRIPTION: " + ViewModelHelper.viewPropertiesNode().calculator().getPreferredDescriptionTextWithFallbackOrNid(manufacturerEntity));

        EntityFacade expectedManufacturerEntity = EntityFacade.make(PrimitiveData.nid(UUID.fromString("75d2303b-ee3c-35e2-83df-99c492e08127")));
        assertEquals(manufacturerEntity, expectedManufacturerEntity, "Unexpected Manufacturer");
    }

}
