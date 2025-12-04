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
package dev.ikm.tinkar.integration.snomed.core;

import com.fasterxml.jackson.databind.JsonNode;
import dev.ikm.tinkar.common.util.uuid.UuidT5Generator;

import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.UUID;

import static dev.ikm.tinkar.integration.snomed.core.TinkarStarterConceptUtil.SNOMED_CT_NAMESPACE;
import static dev.ikm.tinkar.integration.snomed.core.TinkarStarterConceptUtil.TEST_SNOMEDCT_MOCK_DATA_JSON;
import static dev.ikm.tinkar.integration.snomed.core.TinkarStarterConceptUtil.loadJsonData;

/**
 * MockEntity class to mock the entity service. Loads the cache to store and get the data.
 * **/
public class MockEntity {
    private static int nidCount = 100;
    private static final Map<UUID, Integer> mockDataMap = new HashMap<>();

    // universal cache of type Object to persist entities (stamp, concept, semantics) to create multiple versions
    private static final Map<UUID, Object> entityCache = new HashMap();

    static {
        init();
    }

    public static void init() {
        JsonNode primitiveData = loadJsonData(MockEntity.class, TEST_SNOMEDCT_MOCK_DATA_JSON);
        Iterator itr = primitiveData.get("data").iterator();

        while(itr.hasNext()) {
            JsonNode mockData = (JsonNode) itr.next();
            String value = mockData.get("value").asText();
            MockDataType type = MockDataType.getEnumType(mockData.get("type").asText());
            Integer nid = mockData.get("nid").asInt();
            MockEntity.populateMockData(value, type, nid);
        }
    }

    public static void populateMockData(String textValue, MockDataType type) {
        populateMockData(textValue, type, nidCount);
        nidCount+=1;
    }

    private static void populateMockData(String textValue, MockDataType type, int nid) {
        UUID value;
        switch(type) {
            case MODULE: {
                value = UuidT5Generator.get(SNOMED_CT_NAMESPACE, textValue);
            }
            break;
            case CONCEPT, PATTERN: {
                value = UuidT5Generator.get(textValue);
            }
            break;
            case ENTITYREF: {
                value = UUID.fromString(textValue);
            }
            break;
            default: {
                value = UUID.randomUUID();
            };
            break;
        }
        mockDataMap.putIfAbsent(value, nid);
    }

    public static int getNid(UUID key) {
        return mockDataMap.get(key);
    }

    public static void putEntity(UUID uuid, Object entity) {
        entityCache.put(uuid, entity);
    }

    public static Object getEntity(UUID key) {
        return entityCache.get(key);
    }

    public static void clearCache(){
        entityCache.clear();
    }
}
