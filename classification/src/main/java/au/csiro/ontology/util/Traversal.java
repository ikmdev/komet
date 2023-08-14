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

package au.csiro.ontology.util;

import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.Map;
import java.util.Set;

import au.csiro.ontology.Ontology;
import au.csiro.ontology.Node;

public interface Traversal {
    
    /**
     * A level-order traversal where the level is the min distance from the 
     * root.
     * <p>
     * <b>Will</b> visit descendants of {@code start} that have paths to the 
     * root not also containing {@code start}.
     */
    final public static Traversal BFS_MIN = new AbstractTraversal() {
        public void accept(Node start, Visitor... visitors) {
            final Set<Node> done = new HashSet<Node>();
            final LinkedList<Node> queue = new LinkedList<Node>();
            queue.add(start);
            
            while (!queue.isEmpty()) {
                Node node = queue.poll();
                if (!done.contains(node)) {     // don't visit more than once
                    for (Visitor v: visitors) {
                        v.visit(node);
                    }
                    done.add(node);
                    queue.addAll(node.getChildren());
                }
            }
        }
    };
    
    /**
     * A level-order traversal where the level is the max distance from the root.
     * <p>
     * <b>Will not</b> visit descendants of {@code start} that have paths to the root not also containing {@code start}.
     */
    final public static Traversal BFS_MAX = new AbstractTraversal() {
        
        public void accept(Node start, Visitor... visitors) {
            final Set<Node> done = new HashSet<Node>();
            final LinkedList<Node> queue = new LinkedList<Node>();
            queue.add(start);
            
            while (!queue.isEmpty()) {
                Node node = queue.poll();
                if (!done.contains(node)) {     // don't visit more than once
                    for (Visitor v: visitors) {
                        v.visit(node);
                    }
                    done.add(node);
                    for (Node child: node.getChildren()) {
                        if (done.containsAll(child.getParents())) {     // check all parents are done first
                            queue.add(child);
                        }
                    }
                }
            }
        }
    };

    public void accept(Ontology ont, Visitor... visitors);
    public void accept(Node node, Visitor... visitors);
    
    static interface Visitor {
        void visit(Node node);
    }

    static class Stats {
        /**
         * Traverses the ontology to compute min and max path length to root for each node.
         * 
         * @param ont
         * @return
         */
        public static Map<Node, Object> computeStats(final Ontology ont) {
            final Map<Node, Object> result = new HashMap<Node, Object>();
            
            final Map<Node, Integer> minLevel = new HashMap<Node, Integer>();
            final Map<Node, Integer> maxLevel = new HashMap<Node, Integer>();
            
            final Visitor v = new Visitor() {
                public void visit(final Node node) {
                    int min;
                    int max;
                    
                    if (ont.getTopNode().equals(node)) {
                        min = max = 0;
                    } else {
                        min = Integer.MAX_VALUE;
                        max = Integer.MIN_VALUE;
                        for (Node parent: node.getParents()) {
                            min = Math.min(min, minLevel.get(parent) + 1);
                            max = Math.max(max, maxLevel.get(parent) + 1);
                        }
                    }
                    minLevel.put(node, min);
                    maxLevel.put(node, max);
                    
                    result.put(node, new int[] {min, max});
                }
            };

            BFS_MAX.accept(ont, v);
            
            return result;
        }
    }
    
}

abstract class AbstractTraversal implements Traversal {
    
    public void accept(Ontology ont, Visitor... visitors) {
        accept(ont.getTopNode(), visitors);
    }
    
}
