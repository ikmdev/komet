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
package au.csiro.ontology;

import java.io.Serializable;
import java.util.Collections;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Represents a node in the taxonomy generated after classifying an ontology.
 * 
 * @author Alejandro Metke
 * 
 */
public class Node implements Serializable {
    /**
     * 
     */
    private static final long serialVersionUID = 1L;

    /**
     * Set of equivalent concepts in this node.
     */
    protected final Set<String> equivalentConcepts = Collections.newSetFromMap(
            new ConcurrentHashMap<String, Boolean>());
    
    /**
     * Set of parents nodes.
     */
    protected final Set<Node> parents = Collections.newSetFromMap(
            new ConcurrentHashMap<Node, Boolean>());
    
    /**
     * Set of child nodes.
     */
    protected final Set<Node> children = Collections.newSetFromMap(
            new ConcurrentHashMap<Node, Boolean>());

    /**
     * @return the equivalentConcepts
     */
    public Set<String> getEquivalentConcepts() {
        return equivalentConcepts;
    }

    /**
     * @return the parents
     */
    public Set<Node> getParents() {
        return parents;
    }

    /**
     * @return the children
     */
    public Set<Node> getChildren() {
        return children;
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        int size = equivalentConcepts.size();
        int i = 0;
        sb.append("{");
        for (String equiv : equivalentConcepts) {
            sb.append(equiv);
            if (++i < size)
                sb.append(", ");
        }
        sb.append("}");
        return sb.toString();
    }

}
