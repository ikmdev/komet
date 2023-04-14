/**
 * Copyright CSIRO Australian e-Health Research Centre (http://aehrc.com).
 * All rights reserved. Use is subject to license terms and conditions. 
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
