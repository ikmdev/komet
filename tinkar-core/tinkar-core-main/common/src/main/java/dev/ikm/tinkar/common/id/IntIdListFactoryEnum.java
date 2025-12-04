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

import dev.ikm.tinkar.common.id.impl.IntId0List;
import dev.ikm.tinkar.common.id.impl.IntId1List;
import dev.ikm.tinkar.common.id.impl.IntId2List;
import dev.ikm.tinkar.common.id.impl.IntIdListArray;

/**
 *
 */
enum IntIdListFactoryEnum implements IntIdListFactory {
    INSTANCE;

    @Override
    public IntIdList empty() {
        return IntId0List.INSTANCE;
    }

    @Override
    public IntIdList of() {
        return this.empty();
    }

    @Override
    public IntIdList of(int one) {
        return new IntId1List(one);
    }

    @Override
    public IntIdList of(int one, int two) {
        return new IntId2List(one, two);
    }

    @Override
    public IntIdList of(IntIdList list, int... elements) {
        int[] combined = new int[list.size() + elements.length];
        int[] listArray = list.toArray();
        int elementIndex = 0;
        for (int i = 0; i < combined.length; i++) {
            if (i < listArray.length) {
                combined[i] = listArray[i];
            } else {
                combined[i] = elements[elementIndex++];
            }
        }
        return of(combined);
    }

    @Override
    public IntIdList of(int... elements) {
        if (elements == null || elements.length == 0) {
            return this.empty();
        }
        if (elements.length == 1) {
            return new IntId1List(elements[0]);
        }
        if (elements.length == 2) {
            return new IntId2List(elements[0], elements[1]);
        }
        return new IntIdListArray(elements);
    }


}
