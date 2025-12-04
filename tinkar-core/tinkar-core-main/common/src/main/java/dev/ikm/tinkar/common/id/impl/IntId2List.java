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
package dev.ikm.tinkar.common.id.impl;

import dev.ikm.tinkar.common.id.IntIdList;
import dev.ikm.tinkar.common.service.PrimitiveData;

import java.util.Arrays;

public class IntId2List extends IntId2 implements IntIdList {
    public IntId2List(int element, int element2) {
        super(element, element2);
    }

    @Override
    public int get(int index) {
        if (index == 0) {
            return element;
        }
        if (index == 1) {
            return element2;
        }
        throw new IndexOutOfBoundsException("Index: " + index);
    }

    @Override
    public boolean isEmpty() {
        return false;
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) {
            return true;
        }
        if (obj instanceof IntIdList intIdList) {
            if (intIdList.size() == 2 && Arrays.equals(this.toArray(), intIdList.toArray())) {
                return true;
            }
        }
        return false;
    }

    @Override
    public int hashCode() {
        return 31 * (31 + element) + element2;
    }

    @Override
    public String toString() {
        return "IntIdList[" + PrimitiveData.textWithNid(element) + ", " + PrimitiveData.textWithNid(element2) + "]";
    }
}
