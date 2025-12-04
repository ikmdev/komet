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


import dev.ikm.tinkar.common.id.IntIdSet;
import dev.ikm.tinkar.common.service.PrimitiveData;

public class IntId1Set extends IntId1 implements IntIdSet {
    public IntId1Set(int element) {
        super(element);
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) {
            return true;
        }
        if (obj instanceof IntIdSet intIdSet) {
            return intIdSet.size() == 1 && intIdSet.toArray()[0] == element;
        }
        return false;
    }

    @Override
    public int hashCode() {
        return element;
    }

    @Override
    public String toString() {
        return "IntIdSet[" + PrimitiveData.textWithNid(element) + "]";
    }

}
