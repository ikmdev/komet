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

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Map;
import java.util.Set;

import au.csiro.ontology.model.Axiom;
import au.csiro.ontology.model.NamedConcept;

/**
 * Represents an ontology in our internal format. Includes the DL representation (a collection of axioms) in stated 
 * form with additional information about every concept (which is needed for retrieval but not for classification).
 * 
 * @author Alejandro Metke
 *
 */
public class Ontology {
    
    /**
     * The id of the ontology.
     */
    protected final String id;
    
    /**
     * The version of the ontology.
     */
    protected final String version;
    
    /**
     * The collection of stated axioms that form the ontology.
     */
    protected final Collection<Axiom> statedAxioms;
    
    /**
     * The collection of inferred axioms that form the ontology.
     */
    protected final Collection<Axiom> inferredAxioms = new ArrayList<Axiom>();
    
    /**
     * A map that contains references to all the nodes in the taxonomy indexed
     * by id.
     */
    protected final Map<String, Node> nodeMap = new HashMap<String, Node>();
    
    /**
     * Set of {@link Node}s potentially affected by the last incremental
     * classification.
     */
    protected final Set<Node> lastAffectedNodes = new HashSet<Node>();
    
    /**
     * Builds a new ontology.
     * 
     * @param id
     * @param version
     * @param statedAxioms
     * @param nodeMap
     * @param lastAffectedNodes
     */
    public Ontology(String id, String version, Collection<Axiom> statedAxioms, Map<String, Node> nodeMap, 
            Set<Node> lastAffectedNodes) {
        this.id = id;
        this.version = version;
        if(statedAxioms == null) {
            this.statedAxioms = new ArrayList<Axiom>();
        } else {
            this.statedAxioms = statedAxioms;
        }
        if(nodeMap != null)
            this.nodeMap.putAll(nodeMap);
        if(lastAffectedNodes != null)
            this.lastAffectedNodes.addAll(lastAffectedNodes);
    }
    
    /**
     * Builds a new ontology.
     * 
     * @param statedAxioms
     * @param nodeMap
     */
    public Ontology(String id, String version, Collection<Axiom> statedAxioms, Map<String, Node> nodeMap) {
    	this(id, version, statedAxioms, nodeMap, null);
    }
    
    public Collection<Axiom> getStatedAxioms() {
        return statedAxioms;
    }

    public Collection<Axiom> getInferredAxioms() {
        return inferredAxioms;
    }
    
    public Node getNode(String id) {
        return nodeMap.get(id);
    }

    public Iterator<Node> nodeIterator() {
        Set<Node> set = new HashSet<Node>(nodeMap.values());
        return set.iterator();
    }
    
    public Map<String, Node> getNodeMap() {
        return nodeMap;
    }

    public Node getTopNode() {
        return getNode(NamedConcept.TOP);
    }
    
    public Node getBottomNode() {
        return getNode(NamedConcept.BOTTOM);
    }
    
    public void setNodeMap(Map<String, Node> nodeMap) {
        this.nodeMap.clear();
        this.nodeMap.putAll(nodeMap);
    }

    public Set<Node> getAffectedNodes() {
        return lastAffectedNodes;
    }
    
    public void setAffectedNodes(Set<Node> nodes) {
        lastAffectedNodes.clear();
        lastAffectedNodes.addAll(nodes);
    }
    
    public String getId() {
        return id;
    }

    public String getVersion() {
        return version;
    }
    
}
