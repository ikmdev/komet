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


import dev.ikm.komet.amplify.lidr.om.*;
import dev.ikm.komet.amplify.lidr.viewmodels.ViewModelHelper;
import dev.ikm.komet.amplify.viewmodels.StampViewModel;
import dev.ikm.tinkar.common.id.PublicId;
import dev.ikm.tinkar.common.id.PublicIds;
import dev.ikm.tinkar.common.service.CachingService;
import dev.ikm.tinkar.common.service.PrimitiveData;
import dev.ikm.tinkar.common.service.ServiceKeys;
import dev.ikm.tinkar.common.service.ServiceProperties;
import dev.ikm.tinkar.component.graph.Vertex;
import dev.ikm.tinkar.coordinate.view.calculator.ViewCalculator;
import dev.ikm.tinkar.entity.Entity;
import dev.ikm.tinkar.entity.EntityService;
import dev.ikm.tinkar.entity.StampEntity;
import dev.ikm.tinkar.provider.search.Searcher;
import dev.ikm.tinkar.terms.EntityFacade;
import dev.ikm.tinkar.terms.State;
import dev.ikm.tinkar.terms.TinkarTerm;
import javafx.application.Platform;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;

import static dev.ikm.komet.amplify.lidr.om.DataModelHelper.*;
import static dev.ikm.komet.amplify.viewmodels.StampViewModel.*;
import static org.junit.jupiter.api.Assertions.assertEquals;

public class LidrViewModelTest {

    private static final Logger LOG = LoggerFactory.getLogger(LidrViewModelTest.class);

//    public static final String EPHEMERAL_STORE_NAME = "Load Ephemeral Store";
//    public static final Function<String,File> createFilePathInTarget = (pathName) -> new File("%s/target/%s".formatted(System.getProperty("user.dir"), pathName));
//    public static final File TINK_TEST_FILE = createFilePathInTarget.apply("data/tinkar-test-dto-1.1.0.zip");

    //@BeforeAll
    public static void setUpBefore() {
        LOG.info("Clear caches");
        File dataStore = new File(System.getProperty("user.home") + "/Solor/snomed+loinc+lidr_int_2024-05-02_reasoned");
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
                LidrViewModelTest testHarness = new LidrViewModelTest();
                /// testHarness.displayLidrRecordBasedOnDevice();
                //testHarness.createLidrRecord();

                // Testing writers
                PublicId lidrPublicId = testHarness.createLidrRecordWithWriters();
                testHarness.displayLidrRecordBasedOnDevice2(lidrPublicId);

                // Testing stamp view model and writers
                PublicId lidrPublicId2 = testHarness.createLidrRecordWithWriters2();
                testHarness.displayLidrRecordBasedOnDevice2(lidrPublicId2);


            } catch (Throwable e) {
                e.printStackTrace();
                tearDownAfter();
                System.exit(0);
            }
            tearDownAfter();
            System.exit(0);
        });
    }

    public PublicId createLidrRecordWithWriters() {
        // given a lidr record create a lidr record from a device.
        // Device 1868cec8-600a-3e4c-a27a-f7819b87aadd //BD Respiratory Viral Panel for BD MAX System
        PublicId deviceId = PublicIds.of("1868cec8-600a-3e4c-a27a-f7819b87aadd");
        Optional<Entity> deviceEntityQuery = EntityService.get().getEntity(deviceId.asUuidArray());

        // Covid
        AnalyteRecord covidAnalyte = makeAnalyteRecord(PublicIds.of("b8963659-ca41-30e1-8580-aefb70052104"));

        // Matrix (M1) gene
        PublicId targetMatrixM1Id = PublicIds.of("1d9ab589-2fd1-331e-a79d-e9190c415d36");
        Set<TargetRecord> targetRecords = Set.of(makeTargetRecord(targetMatrixM1Id));

        // anterior nasal (nares) swab
        PublicId specimenId = PublicIds.of("6357abcd-b1fe-335f-bf22-7f94fdd07358");
        Set<SpecimenRecord> specimenRecords = Set.of(makeSpecimenRecord(specimenId));

        // Borrelia afzelii+burgdorferi+garinii IgG Ab [Units/volume] in Serum by Immunoassay
        PublicId resultConfId = PublicIds.of("bec2eb34-753c-3ed3-8f5f-99205d8447bc");
        Set<ResultConformanceRecord> resultsConforms = Set.of(makeResultConformanceRecord(resultConfId));

        LidrRecord lidrRecord = new LidrRecord(PublicIds.newRandom(),
                targetMatrixM1Id,
                targetMatrixM1Id,
                covidAnalyte,
                targetRecords,
                specimenRecords,
                resultsConforms);

        Entity deviceEntity = deviceEntityQuery.get();
        ViewCalculator viewCalculator = viewPropertiesNode().calculator();

        // Get device's stamp // TODO Create a stamp based on the user.
        StampEntity stampEntity = viewCalculator.latest(deviceEntity.nid()).get().stamp();

        // Creating the following: Lidr semantic,
        PublicId lidrPublicId = DataModelHelper.write(lidrRecord, deviceEntity.publicId(), stampEntity);
        System.out.println("Created a LIDR record " + lidrPublicId);
        return lidrPublicId;
    }
    public PublicId createLidrRecordWithWriters2() {
        // given a lidr record create a lidr record from a device.
        // Device 1868cec8-600a-3e4c-a27a-f7819b87aadd //BD Respiratory Viral Panel for BD MAX System
        PublicId deviceId = PublicIds.of("1868cec8-600a-3e4c-a27a-f7819b87aadd");
        Optional<Entity> deviceEntityQuery = EntityService.get().getEntity(deviceId.asUuidArray());

        // Covid
        AnalyteRecord covidAnalyte = makeAnalyteRecord(PublicIds.of("b8963659-ca41-30e1-8580-aefb70052104"));

        // Matrix (M1) gene
        PublicId targetMatrixM1Id = PublicIds.of("1d9ab589-2fd1-331e-a79d-e9190c415d36");
        Set<TargetRecord> targetRecords = Set.of(makeTargetRecord(targetMatrixM1Id));

        // anterior nasal (nares) swab
        PublicId specimenId = PublicIds.of("6357abcd-b1fe-335f-bf22-7f94fdd07358");
        Set<SpecimenRecord> specimenRecords = Set.of(makeSpecimenRecord(specimenId));

        // Borrelia afzelii+burgdorferi+garinii IgG Ab [Units/volume] in Serum by Immunoassay
        PublicId resultConfId = PublicIds.of("bec2eb34-753c-3ed3-8f5f-99205d8447bc");
        Set<ResultConformanceRecord> resultsConforms = Set.of(makeResultConformanceRecord(resultConfId));

        LidrRecord lidrRecord = new LidrRecord(PublicIds.newRandom(),
                targetMatrixM1Id,
                targetMatrixM1Id,
                covidAnalyte,
                targetRecords,
                specimenRecords,
                resultsConforms);

        StampViewModel stampViewModel = new StampViewModel();
        stampViewModel.setPropertyValue(STATUS_PROPERTY, State.ACTIVE)
                .setPropertyValue(AUTHOR_PROPERTY, TinkarTerm.USER)
                .setPropertyValue(TIME_PROPERTY, System.currentTimeMillis())
                .setPropertyValue(MODULE_PROPERTY, TinkarTerm.DEVELOPMENT_MODULE)
                .setPropertyValue(PATH_PROPERTY, TinkarTerm.DEVELOPMENT_PATH);

        PublicId lidrRecordPublicId = ViewModelHelper.addNewLidrRecord(lidrRecord, deviceId, stampViewModel);
        System.out.println("Created a LIDR record " + lidrRecordPublicId);
        return lidrRecordPublicId;
    }
    // @Test
    public void displayLidrRecordBasedOnDevice() {
        PublicId deviceId = PublicIds.of("ca616ab7-3f96-3d3f-90cf-f8b97351e884");
        Searcher.getLidrRecordSemanticsFromTestKit(deviceId).forEach(System.out::println);
        Searcher.getResultConformanceFromTestKit(deviceId).forEach(System.out::println);

        PublicId covidAnalyteId = PublicIds.of("b8963659-ca41-30e1-8580-aefb70052104");
        PublicId targetMatrixM1Id = PublicIds.of("1d9ab589-2fd1-331e-a79d-e9190c415d36");
        PublicId resultConformanceId = PublicIds.of("bec2eb34-753c-3ed3-8f5f-99205d8447bc");
        PublicId testLogicalDef = deviceId;

        DataModelHelper
                .findLatestLogicalDefinition(testLogicalDef)
                .ifPresentOrElse(logicalDefinition -> {
                    /* is Present */
                        System.out.println(logicalDefinition);
                        for (Object o : logicalDefinition.vertexMap()) {
                            if (o instanceof Vertex v) {
                                if (v.meaning().equals(TinkarTerm.ROLE_TYPE)) {
                                    System.out.println(v.propertyAsConcept(TinkarTerm.ROLE_TYPE));
                                }
                            }
                        }
                    },
                    /* Not Present */
                    ()-> System.err.println("Error: could not find %s".formatted(testLogicalDef))
                );


        EntityFacade manufacturerEntity = (EntityFacade) findDeviceManufacturer(deviceId).get();
        System.out.println("### MANUFACTURER ENTITY: " + manufacturerEntity);
        System.out.println("### MANUFACTURER DESCRIPTION: " + viewPropertiesNode().calculator().getPreferredDescriptionTextWithFallbackOrNid(manufacturerEntity));

        EntityFacade expectedManufacturerEntity = EntityFacade.make(PrimitiveData.nid(UUID.fromString("75d2303b-ee3c-35e2-83df-99c492e08127")));
        assertEquals(manufacturerEntity, expectedManufacturerEntity, "Unexpected Manufacturer");
    }
    public void displayLidrRecordBasedOnDevice2(PublicId lidrPublicId) {
        //1868cec8-600a-3e4c-a27a-f7819b87aadd
        PublicId deviceId = PublicIds.of("1868cec8-600a-3e4c-a27a-f7819b87aadd"); // BD Max System
        List<PublicId> lidrRecordIds = Searcher.getLidrRecordSemanticsFromTestKit(deviceId);

        lidrRecordIds.forEach(lidrRecordPublicId -> {
            if (PublicId.equals(lidrRecordPublicId, lidrPublicId)) {
                LidrRecord lidrRecord = makeLidrRecord(lidrRecordPublicId);

                AnalyteRecord analyteRecord = lidrRecord.analyte();
                LOG.info(analyteRecord.toString());
                Set<SpecimenRecord> specimenRecords = lidrRecord.specimens();
                specimenRecords.forEach(System.out::println);

                Set<ResultConformanceRecord> resultConformanceRecords = lidrRecord.resultConformances();
                resultConformanceRecords.forEach(System.out::println);

                System.out.println("break");
            }

        });

        Searcher.getResultConformanceFromTestKit(deviceId).forEach(System.out::println);

        PublicId covidAnalyteId = PublicIds.of("b8963659-ca41-30e1-8580-aefb70052104");
        PublicId targetMatrixM1Id = PublicIds.of("1d9ab589-2fd1-331e-a79d-e9190c415d36");
        PublicId resultConformanceId = PublicIds.of("bec2eb34-753c-3ed3-8f5f-99205d8447bc");
        PublicId testLogicalDef = deviceId;

        DataModelHelper
                .findLatestLogicalDefinition(testLogicalDef)
                .ifPresentOrElse(logicalDefinition -> {
                            /* is Present */
                            System.out.println(logicalDefinition);
                            for (Object o : logicalDefinition.vertexMap()) {
                                if (o instanceof Vertex v) {
                                    if (v.meaning().equals(TinkarTerm.ROLE_TYPE)) {
                                        System.out.println(v.propertyAsConcept(TinkarTerm.ROLE_TYPE));
                                    }
                                }
                            }
                        },
                        /* Not Present */
                        ()-> System.err.println("Error: could not find %s".formatted(testLogicalDef))
                );


        EntityFacade manufacturerEntity = (EntityFacade) findDeviceManufacturer(deviceId).get();
        System.out.println("### MANUFACTURER ENTITY: " + manufacturerEntity);
        System.out.println("### MANUFACTURER DESCRIPTION: " + viewPropertiesNode().calculator().getPreferredDescriptionTextWithFallbackOrNid(manufacturerEntity));

        EntityFacade expectedManufacturerEntity = EntityFacade.make(PrimitiveData.nid(UUID.fromString("75d2303b-ee3c-35e2-83df-99c492e08127")));
        assertEquals(manufacturerEntity, expectedManufacturerEntity, "Unexpected Manufacturer");
    }

}
