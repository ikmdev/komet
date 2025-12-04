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

/**
 * <h2>Navigation Calculator System</h2>
 *
 * <p>Provides computational implementations that construct and traverse navigational directed graphs
 * based on navigation coordinates. Navigation calculators build unified graphs from multiple
 * navigation patterns, apply vertex filtering and sorting, and provide efficient graph traversal
 * operations including parent/child queries, ancestor/descendant calculations, and subsumption testing.</p>
 *
 * <h3>Core Responsibilities</h3>
 *
 * <p>Navigation calculators handle:</p>
 * <ul>
 * <li><strong>Graph Construction</strong> - Build navigational digraphs from pattern specifications</li>
 * <li><strong>Pattern Merging</strong> - Combine edges from multiple navigation patterns</li>
 * <li><strong>Vertex Filtering</strong> - Apply state-based filtering (active/inactive)</li>
 * <li><strong>Vertex Sorting</strong> - Sort children according to coordinate specifications</li>
 * <li><strong>Traversal Operations</strong> - Provide parent, child, ancestor, descendant queries</li>
 * <li><strong>Subsumption Testing</strong> - Determine ancestor/descendant relationships</li>
 * <li><strong>Graph Caching</strong> - Cache constructed graphs for efficient reuse</li>
 * </ul>
 *
 * <h3>Core Interfaces and Classes</h3>
 *
 * <h4>NavigationCalculator</h4>
 * <p>The primary interface defining graph traversal operations. Key methods include:</p>
 *
 * <ul>
 * <li><strong>parentsOf()</strong> - Get immediate parents of a concept</li>
 * <li><strong>childrenOf()</strong> - Get immediate children of a concept</li>
 * <li><strong>ancestorsOf()</strong> - Get all ancestors (transitive closure of parents)</li>
 * <li><strong>descendantsOf()</strong> - Get all descendants (transitive closure of children)</li>
 * <li><strong>isDescendentOf()</strong> - Test if one concept is descendant of another</li>
 * <li><strong>isChildOf()</strong> - Test if one concept is immediate child of another</li>
 * <li><strong>roots()</strong> - Get root concepts (concepts with no parents)</li>
 * <li><strong>leaves()</strong> - Get leaf concepts (concepts with no children)</li>
 * </ul>
 *
 * <h4>NavigationCalculatorWithCache</h4>
 * <p>Cached implementation that stores graph structures for performance. Features:</p>
 * <ul>
 * <li>Thread-safe caching of parent/child relationships</li>
 * <li>Content-based coordinate UUID for cache key generation</li>
 * <li>Static factory method {@code getCalculator()} for instance retrieval</li>
 * <li>Automatic integration with STAMP calculator for version resolution</li>
 * <li>Lazy graph construction (built on first access)</li>
 * </ul>
 *
 * <pre>{@code
 * // Get a cached calculator instance
 * StampCoordinateRecord stampCoord = Coordinates.Stamp.DevelopmentLatestActiveOnly();
 * NavigationCoordinateRecord navCoord = Coordinates.Navigation.inferred();
 *
 * NavigationCalculator calculator =
 *     NavigationCalculatorWithCache.getCalculator(stampCoord, navCoord);
 *
 * // Traverse the graph (results cached)
 * IntIdSet parents = calculator.parentsOf(conceptNid);
 * IntIdSet children = calculator.childrenOf(conceptNid);
 * }</pre>
 *
 * <h4>NavigationCalculatorDelegate</h4>
 * <p>Delegation interface allowing classes to provide navigation calculator functionality by
 * delegating to an underlying calculator instance. Used in view calculators:</p>
 * <pre>{@code
 * public class ViewCalculator implements NavigationCalculatorDelegate {
 *     private final NavigationCalculator navigationCalculator;
 *
 *     @Override
 *     public NavigationCalculator navigationCalculator() {
 *         return navigationCalculator;
 *     }
 *
 *     // Inherits all NavigationCalculator methods via delegation
 * }
 * }</pre>
 *
 * <h4>Edge and EdgeRecord</h4>
 * <p>Represent directed edges in the navigation graph:</p>
 * <ul>
 * <li><strong>Edge</strong> - Interface representing a directed relationship between concepts</li>
 * <li><strong>EdgeRecord</strong> - Immutable record implementation storing source, destination,
 * and edge metadata</li>
 * <li>Edges flow from child (source) to parent (destination)</li>
 * <li>Multiple edges can exist between the same concepts from different patterns</li>
 * </ul>
 *
 * <h3>Graph Traversal Operations</h3>
 *
 * <h4>Immediate Relationships</h4>
 * <pre>{@code
 * NavigationCalculator calc = NavigationCalculatorWithCache.getCalculator(stamp, nav);
 *
 * // Get immediate parents
 * IntIdSet parents = calc.parentsOf(conceptNid);
 * parents.forEach(parentNid -> {
 *     // Process each parent
 * });
 *
 * // Get immediate children (sorted if coordinate specifies)
 * IntIdSet children = calc.childrenOf(conceptNid);
 * children.forEach(childNid -> {
 *     // Process each child
 * });
 *
 * // Test immediate relationship
 * boolean isChild = calc.isChildOf(childNid, parentNid);
 * }</pre>
 *
 * <h4>Transitive Relationships</h4>
 * <pre>{@code
 * // Get all ancestors (parents, grandparents, etc.)
 * IntIdSet ancestors = calc.ancestorsOf(conceptNid);
 *
 * // Get all descendants (children, grandchildren, etc.)
 * IntIdSet descendants = calc.descendantsOf(conceptNid);
 *
 * // Test transitive relationship (subsumption)
 * boolean isDescendant = calc.isDescendentOf(descendantNid, ancestorNid);
 *
 * // Equivalent to:
 * boolean isDescendant = calc.ancestorsOf(descendantNid).contains(ancestorNid);
 * }</pre>
 *
 * <h4>Special Nodes</h4>
 * <pre>{@code
 * // Get root concepts (no parents)
 * IntIdSet roots = calc.roots();
 *
 * // Get leaf concepts (no children)
 * IntIdSet leaves = calc.leaves();
 *
 * // Get all concepts in the navigation graph
 * IntIdSet allConcepts = calc.allReachableConcepts();
 * }</pre>
 *
 * <h3>Graph Construction Process</h3>
 *
 * <p>The calculator builds navigation graphs through multiple stages:</p>
 *
 * <ol>
 * <li><strong>Pattern Collection</strong>
 * <pre>{@code
 * for (int patternNid : navCoord.navigationPatternNids().toArray()) {
 *     // Load navigation semantic for this pattern
 * }
 * }</pre>
 * </li>
 *
 * <li><strong>Edge Extraction</strong>
 * <pre>{@code
 * // For each navigation pattern, extract edges
 * Entity.provider().forEachSemanticForPattern(patternNid, semantic -> {
 *     // Extract parent-child edges from semantic
 *     edges.add(new EdgeRecord(childNid, parentNid, patternNid));
 * });
 * }</pre>
 * </li>
 *
 * <li><strong>STAMP Filtering</strong>
 * <pre>{@code
 * // Keep only edges from semantics visible in STAMP coordinate
 * Latest<SemanticEntityVersion> latest = stampCalc.latest(semantic);
 * if (latest.isPresent()) {
 *     // Include this edge
 * }
 * }</pre>
 * </li>
 *
 * <li><strong>Vertex State Filtering</strong>
 * <pre>{@code
 * // Remove edges to/from concepts not matching vertex state set
 * if (navCoord.vertexStates().contains(concept.state())) {
 *     // Include concept in graph
 * }
 * }</pre>
 * </li>
 *
 * <li><strong>Vertex Sorting</strong>
 * <pre>{@code
 * if (navCoord.sortVertices()) {
 *     children = sortChildren(children, navCoord, langCoord);
 * }
 * }</pre>
 * </li>
 * </ol>
 *
 * <h3>Multi-Pattern Navigation</h3>
 *
 * <p>When multiple navigation patterns are specified, edges from all patterns are merged:</p>
 *
 * <pre>{@code
 * NavigationCoordinateRecord multiPattern = NavigationCoordinateRecord.make(
 *     IntIds.set.of(
 *         TinkarTerm.INFERRED_NAVIGATION.nid(),
 *         TinkarTerm.PART_OF_NAVIGATION.nid()
 *     ),
 *     StateSet.ACTIVE,
 *     IntIds.list.empty(),
 *     true
 * );
 *
 * NavigationCalculator calc =
 *     NavigationCalculatorWithCache.getCalculator(stamp, multiPattern);
 *
 * // Parents includes both is-a parents and part-of parents
 * IntIdSet parents = calc.parentsOf(conceptNid);
 * }</pre>
 *
 * <h3>Sorting Implementation</h3>
 *
 * <p>When vertex sorting is enabled, children are sorted in multiple stages:</p>
 *
 * <pre>{@code
 * List<Integer> sortedChildren = new ArrayList<>(children);
 *
 * // Stage 1: Apply custom sort patterns in priority order
 * for (int sortPatternNid : navCoord.verticesSortPatternNidList().toArray()) {
 *     sortedChildren = applySortPattern(sortedChildren, sortPatternNid);
 * }
 *
 * // Stage 2: Apply natural alphabetical order
 * sortedChildren.sort((c1, c2) -> {
 *     String desc1 = langCalc.getDescriptionText(c1);
 *     String desc2 = langCalc.getDescriptionText(c2);
 *     return NaturalOrder.compareStrings(desc1, desc2);
 * });
 * }</pre>
 *
 * <h3>Common Usage Patterns</h3>
 *
 * <h4>Building Hierarchy Trees</h4>
 * <pre>{@code
 * // Recursively build tree from root
 * void buildTree(int conceptNid, NavigationCalculator calc, int depth) {
 *     String indent = "  ".repeat(depth);
 *     String name = langCalc.getDescriptionText(conceptNid);
 *     System.out.println(indent + name);
 *
 *     IntIdSet children = calc.childrenOf(conceptNid);
 *     children.forEach(childNid -> buildTree(childNid, calc, depth + 1));
 * }
 * }</pre>
 *
 * <h4>Finding Common Ancestors</h4>
 * <pre>{@code
 * IntIdSet ancestors1 = calc.ancestorsOf(concept1Nid);
 * IntIdSet ancestors2 = calc.ancestorsOf(concept2Nid);
 *
 * // Intersection = common ancestors
 * IntIdSet commonAncestors = ancestors1.intersect(ancestors2);
 * }</pre>
 *
 * <h4>Subsumption Testing</h4>
 * <pre>{@code
 * // Test if concept1 is more general than concept2
 * boolean concept1SubsumesConcept2 =
 *     calc.isDescendentOf(concept2Nid, concept1Nid);
 *
 * // Test if concepts are in same hierarchy
 * boolean related =
 *     calc.isDescendentOf(concept1Nid, concept2Nid) ||
 *     calc.isDescendentOf(concept2Nid, concept1Nid);
 * }</pre>
 *
 * <h4>Computing Path Distance</h4>
 * <pre>{@code
 * // Find shortest path between concepts
 * OptionalInt distance = calc.pathDistance(fromNid, toNid);
 * if (distance.isPresent()) {
 *     System.out.println("Distance: " + distance.getAsInt());
 * }
 * }</pre>
 *
 * <h3>Performance and Caching</h3>
 *
 * <p>NavigationCalculatorWithCache provides significant performance benefits:</p>
 *
 * <ul>
 * <li><strong>Graph Structure Caching</strong> - Complete parent/child maps cached</li>
 * <li><strong>Coordinate-Based Keys</strong> - Cache keys from stamp + navigation coordinate UUIDs</li>
 * <li><strong>Lazy Construction</strong> - Graphs built only when first accessed</li>
 * <li><strong>Thread-Safe Access</strong> - Concurrent map-based caching</li>
 * <li><strong>Transitive Closure Caching</strong> - Ancestor/descendant sets cached</li>
 * </ul>
 *
 * <p>Performance characteristics:</p>
 * <ul>
 * <li>First access: Full graph construction from semantic patterns</li>
 * <li>Immediate relationships: O(1) cache lookup</li>
 * <li>Transitive relationships: O(1) if cached, O(V + E) for first computation</li>
 * <li>Memory overhead: O(V + E) per unique coordinate combination</li>
 * </ul>
 *
 * <h3>Integration with View Calculators</h3>
 *
 * <p>Navigation calculators are typically accessed through view calculators:</p>
 * <pre>{@code
 * ViewCalculator viewCalc = ViewCalculatorWithCache.getCalculator(viewCoord);
 *
 * // ViewCalculator delegates to NavigationCalculator
 * IntIdSet parents = viewCalc.parentsOf(conceptNid);
 * boolean isDescendant = viewCalc.isDescendentOf(childNid, parentNid);
 * }</pre>
 *
 * <h3>Thread Safety</h3>
 *
 * <p>All calculator implementations are thread-safe and can be shared across threads.
 * The cached implementation uses concurrent data structures for safe concurrent access
 * to graph structures.</p>
 *
 * @see dev.ikm.tinkar.coordinate.navigation.calculator.NavigationCalculator
 * @see dev.ikm.tinkar.coordinate.navigation.calculator.NavigationCalculatorWithCache
 * @see dev.ikm.tinkar.coordinate.navigation.calculator.NavigationCalculatorDelegate
 * @see dev.ikm.tinkar.coordinate.navigation.calculator.Edge
 * @see dev.ikm.tinkar.coordinate.navigation.calculator.EdgeRecord
 * @see dev.ikm.tinkar.coordinate.navigation.NavigationCoordinate
 */
package dev.ikm.tinkar.coordinate.navigation.calculator;
