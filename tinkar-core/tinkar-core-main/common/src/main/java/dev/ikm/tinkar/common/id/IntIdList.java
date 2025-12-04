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


import org.eclipse.collections.api.factory.Lists;
import org.eclipse.collections.api.list.ImmutableList;
import org.eclipse.collections.api.list.MutableList;

import java.util.function.IntFunction;

public interface IntIdList extends IdList, IntIdCollection {
    default <T extends Object> ImmutableList<T> map(IntFunction<T> function) {
        MutableList<T> list = Lists.mutable.ofInitialCapacity(size());
        for (int i = 0; i < size(); i++) {
            list.add(function.apply(get(i)));
        }
        return list.toImmutable();
    }

    int get(int index);

    default boolean notEmpty() {
        return !this.isEmpty();
    }

    boolean isEmpty();

    default IntIdList with(int... valuesToAdd) {
        return IntIds.list.of(this, valuesToAdd);
    }


}
