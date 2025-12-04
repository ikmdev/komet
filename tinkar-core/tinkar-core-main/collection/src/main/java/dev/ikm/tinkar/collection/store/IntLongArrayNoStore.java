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
package dev.ikm.tinkar.collection.store;

import java.util.Optional;
import java.util.concurrent.atomic.AtomicReferenceArray;

public class IntLongArrayNoStore implements IntLongArrayStore{
    @Override
    public Optional<AtomicReferenceArray<long[]>> get(int spineIndex) {
        throw new UnsupportedOperationException("Persistence is not supported");
    }

    @Override
    public void put(int spineIndex, AtomicReferenceArray<long[]> spine) {
        throw new UnsupportedOperationException("Persistence is not supported");
    }

    @Override
    public int sizeOnDisk() {
        throw new UnsupportedOperationException("Persistence is not supported");
    }

    @Override
    public int getSpineCount() {
        throw new UnsupportedOperationException("Persistence is not supported");
    }

    @Override
    public void writeSpineCount(int spineCount) {
        throw new UnsupportedOperationException("Persistence is not supported");
    }
}
