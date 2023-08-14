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
package au.csiro.snorocket.core.concurrent;

import java.util.Map;
import java.util.Queue;
import java.util.Set;
import java.util.concurrent.ConcurrentMap;

import au.csiro.ontology.Node;
import au.csiro.ontology.model.NamedConcept;
import au.csiro.snorocket.core.IFactory;
import au.csiro.snorocket.core.util.IConceptSet;
import au.csiro.snorocket.core.util.IntIterator;

/**
 * Connects the nodes in the taxonomy based on the direct map.
 * 
 * @author Alejandro Metke
 *
 */
public class TaxonomyWorker2 implements Runnable {
    
    private final IFactory factory;
    private final Map<String, Node> conceptNodeIndex;
    private final ConcurrentMap<Integer, IConceptSet> direc;
    private final Queue<Node> todo;
    private final Set<Node> nodeSet;
    
    /**
     * 
     */
    public TaxonomyWorker2(IFactory factory, 
            Map<String, Node> conceptNodeIndex, 
            ConcurrentMap<Integer, IConceptSet> direc, Queue<Node> todo, 
            Set<Node> nodeSet) {
        this.factory = factory;
        this.conceptNodeIndex = conceptNodeIndex;
        this.direc = direc;
        this.todo = todo;
        this.nodeSet = nodeSet;
    }

    public void run() {
        while(true) {
            Node node = todo.poll();
            if(node == null) break;
            if(node.getEquivalentConcepts().contains(NamedConcept.BOTTOM)) continue;
            
            for (String c : node.getEquivalentConcepts()) {
                // Get direct super-concepts
                IConceptSet dc = direc.get(factory.getConcept(c));
                if (dc != null) {
                    for(IntIterator it = dc.iterator(); it.hasNext(); ) {
                        int d = it.next();
                        Node parent = conceptNodeIndex.get(
                                factory.lookupConceptId(d));
                        if (parent != null) {
                            node.getParents().add(parent);
                            parent.getChildren().add(node);
                            nodeSet.remove(parent);
                        }
                    }
                }
            }
        }
    }

}
