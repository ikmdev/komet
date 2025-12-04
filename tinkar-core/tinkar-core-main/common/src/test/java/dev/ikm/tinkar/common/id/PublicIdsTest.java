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
package dev.ikm.tinkar.common.id;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

public class PublicIdsTest {

    @Test
    public void publicList() {
        PublicIdList idList = PublicIds.list.of(
                PublicIds.newRandom(),
                PublicIds.newRandom()
                );
        assertEquals(idList.size(), 2);
        PublicIdList idList2 = PublicIds.list.of(idList.toIdArray());
        assertEquals(idList, idList2);

        PublicIdSet idSet = PublicIds.set.of(idList);
        PublicIdSet idSet2 = PublicIds.set.of(idList);
        assertEquals(idSet, idSet2);
        PublicIdSet set3 = PublicIds.set.of(idList2.toIdArray());
    }
}