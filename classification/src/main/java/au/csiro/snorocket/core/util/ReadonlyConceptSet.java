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
package au.csiro.snorocket.core.util;

public final class ReadonlyConceptSet implements IConceptSet {

    /**
     * Serialisation version.
     */
    private static final long serialVersionUID = 1L;
    
    private IConceptSet set;

    public ReadonlyConceptSet(IConceptSet set) {
        this.set = set;
    }

    public void add(int concept) {
        throw new UnsupportedOperationException();
    }

    public void addAll(IConceptSet set) {
        throw new UnsupportedOperationException();
    }

    public void clear() {
        throw new UnsupportedOperationException();
    }

    public boolean contains(int concept) {
        return set.contains(concept);
    }

    public boolean containsAll(IConceptSet concepts) {
        return set.containsAll(concepts);
    }

    public IntIterator iterator() {
        return set.iterator();
    }

    public void remove(int concept) {
        throw new UnsupportedOperationException();
    }

    public void removeAll(IConceptSet set) {
        throw new UnsupportedOperationException();
    }

    public boolean isEmpty() {
        return set.isEmpty();
    }

    public int size() {
        return set.size();
    }

    public void grow(int increment) {
        throw new UnsupportedOperationException(
                "Cannot grow the EmptyConceptSet!");
    }

    public String toString() {
        return String.valueOf(set);
    }

    public int[] toArray() {
        return set.toArray();
    }
}