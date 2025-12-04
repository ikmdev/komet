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


import dev.ikm.tinkar.common.id.impl.IntId0Set;
import dev.ikm.tinkar.common.id.impl.IntId1Set;
import dev.ikm.tinkar.common.id.impl.IntId2Set;
import dev.ikm.tinkar.common.id.impl.IntIdSetArray;
import dev.ikm.tinkar.common.id.impl.IntIdSetRoaring;

import java.util.Arrays;


enum IntIdSetFactoryEnum implements IntIdSetFactory {
    INSTANCE;

    @Override
    public IntIdSet of(IntIdSet idSet, int... elements) {
        int[] combined = new int[idSet.size() + elements.length];
        int[] listArray = idSet.toArray();
        int elementIndex = 0;
        for (int i = 0; i < combined.length; i++) {
            if (i < listArray.length) {
                combined[i] = listArray[i];
            } else {
                combined[i] = elements[elementIndex++];
            }
        }
        combined = Arrays.stream(combined).distinct().toArray();
        return of(combined);
    }

    @Override
    public IntIdSet empty() {
        return IntId0Set.INSTANCE;
    }

    @Override
    public IntIdSet of() {
        return this.empty();
    }

    @Override
    public IntIdSet of(int one) {
        return new IntId1Set(one);
    }


    @Override
    public IntIdSet of(int one, int two) {
        if (one != two) {
            return new IntId2Set(one, two);
        }
        return of(one);
    }

    @Override
    public IntIdSet of(int... elements) {
        if (elements == null || elements.length == 0) {
            return empty();
        }
        if (elements.length == 1) {
            return this.of(elements[0]);
        }
        elements = Arrays.stream(elements).distinct().toArray();
        if (elements.length == 2) {
            if (elements[0] == elements[1]) {
                return this.of(elements[0]);
            } else {
                return this.of(elements[0], elements[1]);
            }
        }
        if (elements.length < 1024) {
            return IntIdSetArray.newIntIdSet(elements);
        }
        return IntIdSetRoaring.newIntIdSet(elements);
    }

    @Override
    public IntIdSet ofAlreadySorted(int... elements) {
        if (elements == null || elements.length == 0) {
            return empty();
        }
        if (elements.length == 1) {
            return this.of(elements[0]);
        }
        if (elements.length == 2) {
            if (elements[0] == elements[1]) {
                return this.of(elements[0]);
            } else {
                return this.of(elements[0], elements[1]);
            }
        }
        if (elements.length < 1024) {
            return IntIdSetArray.newIntIdSetAlreadySorted(elements);
        }
        return IntIdSetRoaring.newIntIdSetAlreadySorted(elements);
    }
}
