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

import java.util.Collection;
import java.util.function.ToIntFunction;

public interface IntIdSetFactory {
    IntIdSet empty();

    IntIdSet of();

    IntIdSet of(int one);

    IntIdSet of(int one, int two);

    IntIdSet ofAlreadySorted(int... elements);

    IntIdSet of(IntIdSet ids, int... elements);

    default <T> IntIdSet of(Collection<T> components, ToIntFunction<T> function) {
        return of(components.stream().mapToInt(component -> function.applyAsInt(component)).toArray());
    }

    IntIdSet of(int... elements);
}
