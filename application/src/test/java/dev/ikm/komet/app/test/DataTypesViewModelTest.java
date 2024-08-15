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
import dev.ikm.tinkar.terms.TinkarTerm;
import javafx.application.Platform;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.util.Arrays;
import java.util.HashSet;
import java.util.Optional;
import java.util.Set;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.stream.Collectors;

public class DataTypesViewModelTest {

    private static final Logger LOG = LoggerFactory.getLogger(DataTypesViewModelTest.class);

    @BeforeAll
    public static void sartDataStore() {
        LOG.info("Clear caches");
        File dataStore = new File(System.getProperty("user.home") + "/Solor/testPBFile");
        CachingService.clearAll();
        LOG.info("Setup Ephemeral Suite: " + LOG.getName());
        LOG.info(ServiceProperties.jvmUuid());
        ServiceProperties.set(ServiceKeys.DATA_STORE_ROOT, dataStore);
        PrimitiveData.selectControllerByName("Open SpinedArrayStore");
        PrimitiveData.start();
    }

    @AfterAll
    public static void stopDB() {
        PrimitiveData.stop();
        System.exit(0);
    }

    @Test
    public void loadDataTypesTest(){
        loadStarterData();
        Platform.startup(() -> {

            Set<ConceptEntity> dataTypeOptions = new HashSet<>();
            dataTypeOptions.add(Entity.getFast(TinkarTerm.STRING.nid()));
            dataTypeOptions.add(Entity.getFast(TinkarTerm.COMPONENT_FIELD.nid()));
            dataTypeOptions.add(Entity.getFast(TinkarTerm.COMPONENT_ID_SET_FIELD.nid()));
            dataTypeOptions.add(Entity.getFast(TinkarTerm.COMPONENT_ID_LIST_FIELD.nid()));
            dataTypeOptions.add(Entity.getFast(TinkarTerm.DITREE_FIELD.nid()));
            dataTypeOptions.add(Entity.getFast(TinkarTerm.DIGRAPH_FIELD.nid()));
            dataTypeOptions.add(Entity.getFast(TinkarTerm.CONCEPT_FIELD.nid()));
            dataTypeOptions.add(Entity.getFast(TinkarTerm.SEMANTIC_FIELD_TYPE.nid()));
            dataTypeOptions.add(Entity.getFast(TinkarTerm.INTEGER_FIELD.nid()));
            dataTypeOptions.add(Entity.getFast(TinkarTerm.FLOAT_FIELD.nid()));
            dataTypeOptions.add(Entity.getFast(TinkarTerm.BOOLEAN_FIELD.nid()));
            dataTypeOptions.add(Entity.getFast(TinkarTerm.BYTE_ARRAY_FIELD.nid()));
            dataTypeOptions.add(Entity.getFast(TinkarTerm.ARRAY_FIELD.nid()));
            dataTypeOptions.add(Entity.getFast(TinkarTerm.INSTANT_LITERAL.nid()));
            dataTypeOptions.add(Entity.getFast(TinkarTerm.LONG.nid()));

            ViewProperties viewProperties = createViewProperties();
            ViewCalculator viewCalculator = viewProperties.calculator();
            IntIdSet dataTypeFields = viewCalculator.descendentsOf(TinkarTerm.DISPLAY_FIELDS);
            LOG.info(dataTypeFields.size() + " = " + dataTypeFields);

            Set<ConceptEntity> allDataTypes =
                dataTypeFields.intStream()
                    .mapToObj(conceptNid -> (ConceptEntity) Entity.getFast(conceptNid))
                    .filter(p -> dataTypeOptions.contains((ConceptEntity) Entity.getFast(p)))
                    .collect(Collectors.toSet());

            IntIdSet dataTypeDynamic = viewCalculator.descendentsOf(TinkarTerm.DYNAMIC_COLUMN_DATA_TYPES);
            LOG.info(dataTypeDynamic.size() + " = " + dataTypeDynamic);

            allDataTypes.addAll(dataTypeDynamic.intStream()
                .mapToObj(moduleNid -> (ConceptEntity) Entity.getFast(moduleNid))
                .filter(p -> dataTypeOptions.contains((ConceptEntity) Entity.getFast(p)))
                .collect(Collectors.toSet()));


            AtomicInteger counter = new AtomicInteger();
            allDataTypes.forEach(conceptEntity -> {
                Optional<String> stringOptional = viewCalculator.getFullyQualifiedNameText(conceptEntity.nid());
                LOG.info(counter.incrementAndGet() + " - " + stringOptional.orElse("") + " UUID: " + Arrays.stream(conceptEntity.asUuidArray()).findFirst().get());
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
