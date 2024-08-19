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

import dev.ikm.komet.framework.view.ViewProperties;
import dev.ikm.komet.framework.window.WindowSettings;
import dev.ikm.komet.preferences.KometPreferences;
import dev.ikm.komet.preferences.KometPreferencesImpl;
import dev.ikm.tinkar.common.id.IntIdSet;
import dev.ikm.tinkar.common.service.CachingService;
import dev.ikm.tinkar.common.service.PrimitiveData;
import dev.ikm.tinkar.common.service.ServiceKeys;
import dev.ikm.tinkar.common.service.ServiceProperties;
import dev.ikm.tinkar.coordinate.view.calculator.ViewCalculator;
import dev.ikm.tinkar.entity.ConceptEntity;
import dev.ikm.tinkar.entity.Entity;
import dev.ikm.tinkar.entity.EntityCountSummary;
import dev.ikm.tinkar.entity.load.LoadEntitiesFromProtobufFile;
import dev.ikm.tinkar.terms.EntityFacade;
import dev.ikm.tinkar.terms.TinkarTerm;
import javafx.application.Platform;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.TestInstance;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.util.Optional;
import java.util.Set;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.stream.Collectors;

import static dev.ikm.komet.kview.mvvm.viewmodel.DataViewModelHelper.DATA_TYPE_OPTIONS;


@TestInstance(TestInstance.Lifecycle.PER_CLASS)
public class DataTypesViewModelTest {

    private static final Logger LOG = LoggerFactory.getLogger(DataTypesViewModelTest.class);


    public void sartDataStore() {
        LOG.info("Clear caches");
        File dataStore = new File(System.getProperty("user.home") + "/Solor/testPBFile");
        CachingService.clearAll();
        LOG.info("Setup Ephemeral Suite: " + LOG.getName());
        LOG.info(ServiceProperties.jvmUuid());
        ServiceProperties.set(ServiceKeys.DATA_STORE_ROOT, dataStore);
        PrimitiveData.selectControllerByName("Open SpinedArrayStore");
        PrimitiveData.start();
    }

    @BeforeAll
    public void init(){
        sartDataStore();
        loadStarterData();

    }

    @AfterAll
    public void stopDB() {
        PrimitiveData.stop();
        System.exit(0);
    }

  //  @Test
    public void loadDataTypesTest(){
        Platform.startup(() -> {
            ViewProperties viewProperties = createViewProperties();
            ViewCalculator viewCalculator = viewProperties.calculator();
            IntIdSet dataTypeFields = viewCalculator.descendentsOf(TinkarTerm.DISPLAY_FIELDS);
            LOG.info(dataTypeFields.size() + " = " + dataTypeFields);

            Set<EntityFacade> allDataTypes =
                dataTypeFields.intStream()
                    .mapToObj(conceptNid -> (EntityFacade) Entity.getFast(conceptNid))
   //                 .filter(p -> DATA_TYPE_OPTIONS.contains((EntityFacade) Entity.getFast(p)))
                    .collect(Collectors.toSet());

            IntIdSet dataTypeDynamic = viewCalculator.descendentsOf(TinkarTerm.DYNAMIC_COLUMN_DATA_TYPES);
            LOG.info(dataTypeDynamic.size() + " = " + dataTypeDynamic);

            allDataTypes.addAll(dataTypeDynamic.intStream()
                .mapToObj(moduleNid -> (ConceptEntity) Entity.getFast(moduleNid))
   //             .filter(p -> DATA_TYPE_OPTIONS.contains((EntityFacade) Entity.getFast(p)))
                .collect(Collectors.toSet()));


            AtomicInteger counter = new AtomicInteger();
            allDataTypes.forEach(entityFacade -> {
                Optional<String> stringOptional = viewCalculator.getFullyQualifiedNameText(entityFacade.nid());
                LOG.info(counter.incrementAndGet() + " - " + stringOptional.orElse(""));
            });
        });
    }

 //   @Test
    public void loadDataTypesTest2(){
        Platform.startup(() -> {
            ViewProperties viewProperties = createViewProperties();
            ViewCalculator viewCalculator = viewProperties.calculator();
            AtomicInteger counter = new AtomicInteger();
            DATA_TYPE_OPTIONS.forEach(entityFacade -> {
                Optional<String> stringOptional = viewCalculator.getFullyQualifiedNameText(entityFacade.nid());
                LOG.info(counter.incrementAndGet() + " - " + stringOptional.orElse(""));
            });
        });
    }

    private void loadStarterData() {
        File pbFile = new File(System.getProperty("user.home") + "/Solor/tinkar-starter-data-reasoned-0.1-pb.zip");
        LoadEntitiesFromProtobufFile loadProto = new LoadEntitiesFromProtobufFile(pbFile);
        EntityCountSummary count = loadProto.compute();
        LOG.info(count + " entitles loaded from file: " + loadProto.summarize() + "\n\n");
    }


    public static ViewProperties createViewProperties() {
        // TODO how do we get a viewProperties?
        KometPreferences appPreferences = KometPreferencesImpl.getConfigurationRootPreferences();
        KometPreferences windowPreferences = appPreferences.node("main-komet-window");
        WindowSettings windowSettings = new WindowSettings(windowPreferences);
        ViewProperties viewProperties = windowSettings.getView().makeOverridableViewProperties();
        return viewProperties;
    }

}
