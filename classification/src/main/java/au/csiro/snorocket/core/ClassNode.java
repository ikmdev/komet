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
package au.csiro.snorocket.core;

import java.util.HashSet;
import java.util.Set;

import au.csiro.snorocket.core.util.IConceptSet;
import au.csiro.snorocket.core.util.IntIterator;
import au.csiro.snorocket.core.util.SparseConceptSet;

/**
 * Represents a node in the taxonomy generated after classifying an ontology.
 * 
 * @author Alejandro Metke
 * 
 */
public class ClassNode {
    private final IConceptSet equivalentConcepts = new SparseConceptSet();
    private final Set<ClassNode> parents = new HashSet<ClassNode>();
    private final Set<ClassNode> children = new HashSet<ClassNode>();

    /**
     * @return the equivalentConcepts
     */
    public IConceptSet getEquivalentConcepts() {
        return equivalentConcepts;
    }

    /**
     * @return the parents
     */
    public Set<ClassNode> getParents() {
        return parents;
    }

    /**
     * @return the children
     */
    public Set<ClassNode> getChildren() {
        return children;
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        int size = equivalentConcepts.size();
        int i = 0;
        sb.append("{");
        for (IntIterator it = equivalentConcepts.iterator(); it.hasNext();) {
            sb.append(it.next());
            if (++i < size)
                sb.append(", ");
        }
        sb.append("}");
        return sb.toString();
    }

}
