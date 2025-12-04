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
package dev.ikm.tinkar.integration.snomed.language;

import dev.ikm.tinkar.common.util.uuid.UuidT5Generator;
import dev.ikm.tinkar.entity.SemanticRecord;
import dev.ikm.tinkar.entity.SemanticVersionRecord;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Order;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestInstance;

import java.util.List;
import java.util.UUID;

import static dev.ikm.tinkar.integration.snomed.core.MockEntity.getNid;
import static dev.ikm.tinkar.integration.snomed.core.TinkarStarterConceptUtil.SNOMED_CT_NAMESPACE;
import static dev.ikm.tinkar.integration.snomed.core.TinkarStarterDataHelper.openSession;
import static dev.ikm.tinkar.integration.snomed.language.SnomedLanguageSemanticMultipleVersions.createLanguageAcceptabilitySemantic;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotEquals;

@Disabled("Stale")
@TestInstance(TestInstance.Lifecycle.PER_CLASS)
class SnomedLanguageSemanticMultipleVersionsIT {

    @Test
    @DisplayName("Language Acceptability Semantic with multiple versions")
    @Order(1)
    void createLanguageAcceptabilitySemanticMultipleVersions() {
        openSession((mockStaticEntityService, starterData) -> {
            String input = "der2_cRefset_LanguageFull-en_US1000124_20220901_5.txt";
            List<SemanticRecord> semanticRecordList = createLanguageAcceptabilitySemantic(input);
            assertEquals(1, semanticRecordList.size(), "has one record");
            SemanticRecord semanticRecord = semanticRecordList.get(0);
            assertNotEquals(semanticRecord.versions().get(0), semanticRecord.versions().get(1), "multiple versions");
            assertNotEquals(semanticRecord.versions().get(0).stampNid(), semanticRecord.versions().get(1).stampNid());
        });
    }

  

    @Test
    @DisplayName("Test Language Acceptability Semantic records data")
    @Order(2)
    void createLanguageAcceptabilitySemanticVersionsTestWithRealData() {
        openSession((mockStaticEntityService, starterData) -> {
            String input = "der2_cRefset_LanguageFull-en_US1000124_20220901_5.txt";
            List<SemanticRecord> semanticRecordList = createLanguageAcceptabilitySemantic(input);
            UUID testStampUUID = getTestStampUUID();
            SemanticRecord semanticRecord = semanticRecordList.get(0);
            SemanticVersionRecord semanticVersionRecord1 = semanticRecord.versions().get(0);
            assertEquals(getNid(testStampUUID), semanticVersionRecord1.stampNid(),"Same StampUUID");

        });
    }

    private static UUID getTestStampUUID() {
        String id = "80008069-c603-5c5b-8944-101a4069a70f";
        String effectiveTime = "20020131";
        String active = "1";
        String moduleId = "900000000000207008";
        String refSetId = "900000000000508004";
        String referenceComponentId = "791072011";
        String acceptabilityId = "900000000000548007";
        UUID testStampUUID = UuidT5Generator.get(SNOMED_CT_NAMESPACE,
                id + effectiveTime +
                        active + moduleId +
                        refSetId
                        + referenceComponentId +
                        acceptabilityId);
        return testStampUUID;
    }

    @Test
    @DisplayName("Test Language Acceptability Semantic records")
    @Order(3)
    void createLanguageAcceptabilitySemanticVersionChronologyTest() {
        openSession((mockStaticEntityService, starterData) -> {
            String input = "der2_cRefset_LanguageFull-en_US1000124_20220901_5.txt";
            List<SemanticRecord> semanticRecordList = createLanguageAcceptabilitySemantic(input);
            SemanticRecord semanticRecord = semanticRecordList.get(0);
            SemanticVersionRecord semanticVersionRecord1 = semanticRecord.versions().get(0);
            SemanticVersionRecord semanticVersionRecord2 = semanticRecord.versions().get(1);
            assertEquals(semanticVersionRecord1.chronology().patternNid(), semanticVersionRecord2.chronology().patternNid());
            assertNotEquals(semanticVersionRecord1.stampNid(), semanticVersionRecord2.stampNid());
            assertNotEquals(semanticVersionRecord1.chronology().versions(), semanticVersionRecord2.chronology().versions());

        });
    
    }
}
