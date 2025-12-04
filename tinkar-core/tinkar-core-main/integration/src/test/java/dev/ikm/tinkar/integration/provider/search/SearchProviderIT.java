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
package dev.ikm.tinkar.integration.provider.search;

import dev.ikm.tinkar.common.id.PublicId;
import dev.ikm.tinkar.common.id.PublicIds;
import dev.ikm.tinkar.integration.TestConstants;
import dev.ikm.tinkar.integration.helper.DataStore;
import dev.ikm.tinkar.integration.helper.TestHelper;
import dev.ikm.tinkar.provider.search.Searcher;
import dev.ikm.tinkar.terms.TinkarTerm;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestInstance;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.util.Arrays;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;

@TestInstance(TestInstance.Lifecycle.PER_CLASS)
public class SearchProviderIT {

    private static final Logger LOG = LoggerFactory.getLogger(SearchProviderIT.class);

    @BeforeAll
    public void beforeAll() {
        TestHelper.startDataBase(DataStore.EPHEMERAL_STORE);
        TestHelper.loadDataFile(TestConstants.PB_STARTER_DATA_REASONED);
    }

    @Test
    public void getChildrenIT() {
        List<PublicId> expectedUserChildren = Arrays.asList(
                TinkarTerm.ORDER_FOR_AXIOM_ATTACHMENTS.publicId(),
                TinkarTerm.ORDER_FOR_CONCEPT_ATTACHMENTS.publicId(),
                TinkarTerm.ORDER_FOR_DESCRIPTION_ATTACHMENTS.publicId(),
                TinkarTerm.KOMET_USER.publicId(),
                TinkarTerm.KOMET_USER_LIST.publicId(),
                TinkarTerm.MODULE_FOR_USER.publicId(),
                TinkarTerm.PATH_FOR_USER.publicId(),
                TinkarTerm.STARTER_DATA_AUTHORING.publicId()
        );

        List<PublicId> actualUserChildren = Searcher.childrenOf(TinkarTerm.USER.publicId());

        expectedUserChildren.sort(Comparator.naturalOrder());
        actualUserChildren.sort(Comparator.naturalOrder());

        assertEquals(expectedUserChildren, actualUserChildren,
                "Children returned are not as expected.\n" +
                        "   Expected: " + expectedUserChildren + "\n" +
                        "   Actual: " + actualUserChildren
                );
    }

    @Test
    public void getDescendantsIT() {
        List<PublicId> expectedUserDescendants = Arrays.asList(
                // Children of Role
                TinkarTerm.ROLE_OPERATOR.publicId(),
                TinkarTerm.ROLE_TYPE.publicId(),
                TinkarTerm.ROLE_RESTRICTION.publicId(),
                // Children of Role Operator
                TinkarTerm.REFERENCED_COMPONENT_SUBTYPE_RESTRICTION.publicId(),
                TinkarTerm.REFERENCED_COMPONENT_TYPE_RESTRICTION.publicId(),
                TinkarTerm.UNIVERSAL_RESTRICTION.publicId(),
                TinkarTerm.EXISTENTIAL_RESTRICTION.publicId()
        );

        List<PublicId> actualUserDescendants = Searcher.descendantsOf(TinkarTerm.ROLE.publicId());

        expectedUserDescendants.sort(Comparator.naturalOrder());
        actualUserDescendants.sort(Comparator.naturalOrder());

        assertEquals(expectedUserDescendants, actualUserDescendants,
                "Descendants returned are not as expected.\n" +
                        "   Expected: " + expectedUserDescendants + "\n" +
                        "   Actual: " + actualUserDescendants
        );
    }

    @Test
    public void getDescriptionsIT() {
        List<String> expectedFQNs = List.of(
                "Integrated Knowledge Management (SOLOR)",
                "Meaning",
                "Purpose"
        );

        List<PublicId> conceptsWithFQNs = List.of(
                TinkarTerm.ROOT_VERTEX.publicId(),
                TinkarTerm.MEANING.publicId(),
                TinkarTerm.PURPOSE.publicId()
        );

        List<String> actualFQNs = Searcher.descriptionsOf(conceptsWithFQNs);

        assertEquals(expectedFQNs, actualFQNs,
                "Descriptions returned are not as expected.\n" +
                        "   Expected: " + expectedFQNs + "\n" +
                        "   Actual: " + actualFQNs
        );
    }

    private void setupSnomedLoincLidrData() {
        File dataStore = new File(System.getProperty("user.home") + "/Solor/snomedLidrLoinc-data-5-6-2024-withCollabData-dev");
        TestHelper.stopDatabase();
        TestHelper.startDataBase(DataStore.SPINED_ARRAY_STORE, dataStore);
    }

    @Test
    @Disabled
    public void getResultConformancesFromLidrRecordIT() {
        setupSnomedLoincLidrData();

        PublicId lidrRecordId = PublicIds.of(UUID.fromString("ac475ee0-8f34-49e7-b0ba-28f5b4cb2a44"));
        List<PublicId> expectedResultConformances = Arrays.asList(PublicIds.of(UUID.fromString("46366a93-9895-3703-ad7f-cb2596e7cb5d")));
        List<PublicId> actualResultConformances = Searcher.getResultConformancesFromLidrRecord(lidrRecordId);

        Collections.sort(expectedResultConformances);
        Collections.sort(actualResultConformances);
        assertEquals(expectedResultConformances, actualResultConformances, "LIDR Record Result Conformances do not match");
    }

    @Test
    @Disabled
    public void getAllowedResultsFromResultConformanceIT() {
        setupSnomedLoincLidrData();

        PublicId resultConformanceId = PublicIds.of(UUID.fromString("46366a93-9895-3703-ad7f-cb2596e7cb5d"));
        List<PublicId> expectedAllowedResults = Arrays.asList(
                PublicIds.of(UUID.fromString("97b0fbff-cd01-3018-9f72-03ffc7c9027c")),
                PublicIds.of(UUID.fromString("f477b09d-0760-396a-97b0-5abc4fbde352")),
                PublicIds.of(UUID.fromString("2bf6b6b1-74f7-3ebc-a48b-5a7f12980559")),
                PublicIds.of(UUID.fromString("cff1d554-6d56-33f3-bf5d-9d5a6e231128")),
                PublicIds.of(UUID.fromString("152f35e3-f0a3-3a5e-9de2-d79ae288f7ba")),
                PublicIds.of(UUID.fromString("39925f20-bd93-343f-a4c0-588762316250"))
        );
        List<PublicId> actualAllowedResults = Searcher.getAllowedResultsFromResultConformance(resultConformanceId);

        Collections.sort(expectedAllowedResults);
        Collections.sort(actualAllowedResults);
        assertEquals(expectedAllowedResults, actualAllowedResults, "Result Conformance allowed results do not match");
    }

}
