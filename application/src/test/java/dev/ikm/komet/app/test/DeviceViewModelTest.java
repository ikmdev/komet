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


import dev.ikm.komet.kview.lidr.mvvm.viewmodel.DeviceViewModel;
import dev.ikm.komet.framework.view.ViewProperties;
import dev.ikm.komet.framework.window.WindowSettings;
import dev.ikm.komet.preferences.KometPreferences;
import dev.ikm.komet.preferences.KometPreferencesImpl;
import dev.ikm.tinkar.common.id.PublicId;
import dev.ikm.tinkar.common.id.PublicIds;
import dev.ikm.tinkar.common.service.*;
import dev.ikm.tinkar.coordinate.edit.EditCoordinateRecord;
import dev.ikm.tinkar.entity.ConceptEntity;
import dev.ikm.tinkar.entity.Entity;
import dev.ikm.tinkar.entity.RecordListBuilder;
import dev.ikm.tinkar.entity.SemanticRecord;
import dev.ikm.tinkar.entity.transaction.CommitTransactionTask;
import dev.ikm.tinkar.entity.transaction.Transaction;
import dev.ikm.tinkar.terms.TinkarTerm;
import javafx.application.Platform;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.io.IOException;

import static dev.ikm.komet.kview.lidr.mvvm.viewmodel.DeviceViewModel.*;

public class DeviceViewModelTest {

    private static final Logger LOG = LoggerFactory.getLogger(DeviceViewModelTest.class);

//    public static final String EPHEMERAL_STORE_NAME = "Load Ephemeral Store";
//    public static final Function<String,File> createFilePathInTarget = (pathName) -> new File("%s/target/%s".formatted(System.getProperty("user.dir"), pathName));
//    public static final File TINK_TEST_FILE = createFilePathInTarget.apply("data/tinkar-test-dto-1.1.0.zip");

    //@BeforeAll
    public static void setUpBefore() {
        LOG.info("Clear caches");
        File dataStore = new File(System.getProperty("user.home") + "/Solor/snomedct-loinc-data-3-7-2024");
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
                new DeviceViewModelTest().createDeviceIntTest();

            } catch (Throwable e) {
                e.printStackTrace();
                tearDownAfter();
                System.exit(0);
            }
            tearDownAfter();
            System.exit(0);
        });
    }

    //@Test
    public void createDeviceIntTest() throws IOException {
        // arrange
        DeviceViewModel deviceViewModel = new DeviceViewModel();

        deviceViewModel.setPropertyValue(LIDR_RECORD, makeSemanticRecordStub())
                .setPropertyValue(DEVICE_ENTITY, makeConceptStub())
                .setPropertyValue(MANUFACTURER_ENTITY, makeConceptStub());

        // TODO how do we get a viewProperties?
        KometPreferences appPreferences = KometPreferencesImpl.getConfigurationRootPreferences();
        KometPreferences windowPreferences = appPreferences.node("main-komet-window");
        WindowSettings windowSettings = new WindowSettings(windowPreferences);
        ViewProperties viewProperties = windowSettings.getView().makeOverridableViewProperties();

        EditCoordinateRecord editCoordinateRecord = viewProperties.nodeView().toEditCoordinateRecord();

        // act
        boolean success = deviceViewModel.createDevice(editCoordinateRecord);

        // assert
        assert(success);
    }


    private static SemanticRecord makeSemanticRecordStub() {

        Transaction transaction = Transaction.make();

        PublicId publicId = PublicIds.newRandom();
        RecordListBuilder versions = RecordListBuilder.make();

        //FIXME is this enough? probably not...?
        SemanticRecord descriptionSemantic = SemanticRecord.makeNew(publicId, TinkarTerm.DESCRIPTION_PATTERN,
                TinkarTerm.DESCRIPTION_PATTERN.nid(), versions);

        Entity.provider().putEntity(descriptionSemantic);

        // commit the transaction
        CommitTransactionTask commitTransactionTask = new CommitTransactionTask(transaction);
        TinkExecutor.threadPool().submit(commitTransactionTask);

        return descriptionSemantic;
    }

    private static ConceptEntity makeConceptStub() {
        return null;
    }
}
