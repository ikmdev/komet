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

import java.util.Map;

import au.csiro.ontology.Node;

/**
 * @author Alejandro Metke
 *
 */
public class Utils {
    
    public static void printTaxonomy(Node top, Node bottom, Map<String, String> idNameMap) {
    	if(top.equals(bottom)) return;
        System.out.println(nodeToString(top, idNameMap));
    	for(Node child : top.getChildren()) {
            printTaxonomyLevel(child, bottom, 1, idNameMap);
        }
    }
    
    private static void printTaxonomyLevel(Node root, 
            Node bottom, int level, Map<String, String> idNameMap) {
        if(root.equals(bottom)) return;
        System.out.println(spaces(level)+nodeToString(root, idNameMap));
        for(Node child : root.getChildren()) {
            printTaxonomyLevel(child, bottom, level+1, idNameMap);
        }
    }
    
    private static String nodeToString(Node node, Map<String, String> idNameMap) {
        StringBuilder sb = new StringBuilder();
        sb.append("{");
        for(String concept : node.getEquivalentConcepts()) {
            sb.append(" ");
            String desc = idNameMap.get(concept);
            if(desc == null) desc = "NA";
            sb.append(desc);
        }
        sb.append(" }");
        return sb.toString();
    }
    
    public static void printTaxonomy(Node top, Node bottom) {
        for(Node child : top.getChildren()) {
            printTaxonomyLevel(child, bottom, 0);
        }
    }
    
    private static void printTaxonomyLevel(Node root, Node bottom, int level) {
        if(root.equals(bottom)) return;
        System.out.println(spaces(level)+root.toString());
        for(Node child : root.getChildren()) {
            printTaxonomyLevel(child, bottom, level+1);
        }
    }
    
    private static String spaces(int num) {
        StringBuilder sb = new StringBuilder();
        for(int i = 0; i < num; i++) {
            sb.append("  ");
        }
        return sb.toString();
    }

}
