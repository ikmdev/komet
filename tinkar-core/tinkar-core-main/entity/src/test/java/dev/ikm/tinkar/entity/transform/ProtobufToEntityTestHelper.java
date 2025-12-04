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
package dev.ikm.tinkar.entity.transform;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import dev.ikm.tinkar.common.id.PublicId;
import dev.ikm.tinkar.component.Concept;
import dev.ikm.tinkar.entity.Entity;
import dev.ikm.tinkar.entity.EntityService;
import dev.ikm.tinkar.schema.StampVersion;
import dev.ikm.tinkar.terms.EntityProxy;
import org.mockito.MockedStatic;
import org.mockito.Mockito;

import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.time.Instant;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.function.BiConsumer;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

/**
 * Test Helper utility class helps the developer & tester to do the following:
 * <pre>
 *     1. Open a session to perform tests and mock entity related services.
 *     2. Create Protobuffer objects
 *     3. Any utility functions to assist in the test cases.
 * </pre>
 * This allows you to create unit tests without wiring up a full entity service.
 *
 */
public class ProtobufToEntityTestHelper {
    /** Primative starter data */
    public static final String TEST_TINKAR_STARTER_JSON = "test-tinkar-starter-data.json";

    /** JSON property concepts an array of concept objects. */
    public static final String JSON_CONCEPTS_ARRAY_PROP = "concepts";

    /** JSON property name of a concept object. */
    public static final String JSON_CONCEPT_NAME_PROP   = "name";

    /** JSON property uuid of a concept object. */
    public static final String JSON_CONCEPT_UUID_PROP   = "uuid";

    /** A module concept object. */
    public static final String MODULE_CONCEPT_NAME      = "moduleConcept";
    /** A path concept object. */
    public static final String PATH_CONCEPT_NAME        = "pathConcept";
    /** An author concept object. */
    public static final String AUTHOR_CONCEPT_NAME      = "authorConcept";
    /** A status concept object. */
    public static final String STATUS_CONCEPT_NAME      = "statusConcept";
    /** A simple test concept object. */
    public static final String TEST_CONCEPT_NAME        = "testConcept";

    /** A simple referencedComponentPurposeConcept concept object. */
    public static final String REF_COMP_PURPOSE_CONCEPT_NAME = "referencedComponentPurposeConcept";
    /** A referencedComponentMeaningConcept concept object. */
    public static final String REF_COMP_MEANING_CONCEPT_NAME = "referencedComponentMeaningConcept";
    /** A meaningConcept concept object. */
    public static final String MEANING_CONCEPT_NAME          = "meaningConcept";
    /** A dataTypeConcept object. */
    public static final String DATATYPE_CONCEPT_NAME         = "dataTypeConcept";
    /** A purposeConcept concept object. */
    public static final String PURPOSE_CONCEPT_NAME          = "purposeConcept";


    public static dev.ikm.tinkar.schema.PublicId createPBPublicId(){
        return dev.ikm.tinkar.schema.PublicId.newBuilder().build();
    }
    public static dev.ikm.tinkar.schema.PublicId createPBPublicId(Concept concept){
        return createPBPublicId(concept.publicId());
    }
    public static dev.ikm.tinkar.schema.PublicId createPBPublicId(PublicId publicId){
        return dev.ikm.tinkar.schema.PublicId.newBuilder().addUuids(publicId.asUuidList().get(0).toString()).build();
    }

    /**
     * Returns the current epoch time in milliseconds as a long.
     * @return long current epoch time in milliseconds.
     */
    public static long nowEpochMillis() {
        return Instant.now().toEpochMilli();
    }
    public static long nowEpochMillis(long millisToAdd) {
        return Instant.now().plusMillis(millisToAdd).toEpochMilli();
    }

    /**
     * Returns the current epoch time in milliseconds as a long
     * @return long current epoch time in milliseconds.
     */
    public static long nowTimestamp() {
        return nowEpochMillis();
    }
    public static long nowTimestamp(long millisToAdd) {
        return nowEpochMillis(millisToAdd);
    }

    /**
     * This will create a simple concept having a name and uuid. Becareful the concept returned will not have a nid inside the PublicId.
     * @param conceptName Name of the unique concept
     * @param uuidStr UUID hash string
     * @return Concept The concept.
     */
    protected static Concept createSimpleConcept(String conceptName, String uuidStr) {
        return EntityProxy.Concept.make(conceptName, UUID.fromString(uuidStr));
    }

    /**
     * Based on the location of the class file is where text json files will reside.
     * @param clazz The test class being run lets this method know the resource path of the json file.
     * @param fileName The json file name
     * @return JsonNode which allows you to read and parse values.
     */
    public static JsonNode loadJsonFile(Class clazz, String fileName)  {
        InputStream inputStream = clazz.getResourceAsStream(fileName);
        try {
            String jsonText = new String(inputStream.readAllBytes(), StandardCharsets.UTF_8);
            ObjectMapper objectMapper = new ObjectMapper();
            return objectMapper.readTree(jsonText);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    /**
     * This can only be used when the Entity.nid(PublicId) is mocked.
     * @param concept The concept the method will get the nid for
     * @return int concept nid
     */
    public static int nid(Concept concept) {
        return Entity.nid(concept.publicId());
    }

    /**
     * Create PB stamp Version
     * @param expectedTime
     * @param statusConcept
     * @param authorConcept
     * @param moduleConcept
     * @param pathConcept
     * @return
     */
    public static StampVersion createPbStampVersion(long expectedTime, Concept statusConcept, Concept authorConcept, Concept moduleConcept, Concept pathConcept) {
        StampVersion pbStampVersion = StampVersion.newBuilder()
                .setStatusPublicId(createPBPublicId(statusConcept))
                .setTime(expectedTime)
                .setAuthorPublicId(createPBPublicId(authorConcept))
                .setModulePublicId(createPBPublicId(moduleConcept))
                .setPathPublicId(createPBPublicId(pathConcept))
                .build();
        return pbStampVersion;
    }

    public static StampVersion createPbStampVersion(Map<String, Concept> conceptMap, long expectedTime) {
        StampVersion pbStampVersion = StampVersion.newBuilder()
                .setStatusPublicId(createPBPublicId(conceptMap.get(STATUS_CONCEPT_NAME)))
                .setTime(expectedTime)
                .setAuthorPublicId(createPBPublicId(conceptMap.get(AUTHOR_CONCEPT_NAME)))
                .setModulePublicId(createPBPublicId(conceptMap.get(MODULE_CONCEPT_NAME)))
                .setPathPublicId(createPBPublicId(conceptMap.get(PATH_CONCEPT_NAME)))
                .build();
        return pbStampVersion;
    }
    /**
     * A statically mocked Entity class when nid(PublicId) and nid(Component) is called.
     * Also map containing the concept name and concept instance for easy lookup.
     * @param test The test class enabling the core concepts to be loaded from a json file
     * @param session A scope for the test developer to perform testing so Mokito can unregister statically mocked objects.
     */
    public static void openSession(Object test, BiConsumer<MockedStatic<Entity>, Map<String, Concept>> session) {
        Map<String, Concept> conceptMap = new HashMap<>();
        JsonNode jsonNode = loadJsonFile(test.getClass(), TEST_TINKAR_STARTER_JSON);
        JsonNode concepts = jsonNode.get(JSON_CONCEPTS_ARRAY_PROP);

        // Convert to a list of json concepts
        List<JsonNode> conceptList = new ArrayList<>();
        concepts.iterator().forEachRemaining(conceptList::add);

        try (MockedStatic<Entity> mockedEntity = Mockito.mockStatic(Entity.class)) {
            // Mock the EntityService provider
            EntityService entityService = mock(EntityService.class);

            for(int i=0; i<conceptList.size(); i++) {
                int nid = (i+1) * 10;
                // Each JSON concept from the json file (resources)
                JsonNode conceptJson = conceptList.get(i);
                Concept concept = createSimpleConcept(conceptJson.get(JSON_CONCEPT_NAME_PROP).asText(),
                        conceptJson.get(JSON_CONCEPT_UUID_PROP).asText());
                conceptMap.put(conceptJson.get(JSON_CONCEPT_UUID_PROP).asText(), concept);
                conceptMap.put(conceptJson.get(JSON_CONCEPT_NAME_PROP).asText(), concept);

                // Mock the nid(PublicId) method
                mockedEntity.when(() -> Entity.nid(concept.publicId())).thenReturn(nid);

                // Mock the nid(Concept) method
                mockedEntity.when(() -> Entity.nid(concept)).thenReturn(nid);

                // put entity and store nid
                when(entityService.nidForComponent(concept)).thenReturn(nid);
                when(entityService.nidForPublicId(concept.publicId())).thenReturn(nid);
                mockedEntity.when(() -> Entity.provider()).thenReturn(entityService);
            }

            // Pass mocked entity service and conc
            session.accept(mockedEntity, conceptMap);

        } // closable (unregisters statically mocked object)

    }

}
