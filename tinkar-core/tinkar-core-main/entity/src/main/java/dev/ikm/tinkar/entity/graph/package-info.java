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
 * <h2>Entity Graph System</h2>
 *
 * <p>Provides graph structures for representing description logic axioms, navigational hierarchies,
 * and other graph-based knowledge representations in Tinkar. This package implements directed trees
 * (DiTrees) and directed graphs (DiGraphs) with entity vertices for encoding complex logical
 * expressions and relationships.</p>
 *
 * <h3>Core Graph Structures</h3>
 *
 * <h4>DiTreeEntity - Directed Trees for Logic</h4>
 * <p>{@link dev.ikm.tinkar.entity.graph.DiTreeEntity} represents directed trees used primarily for
 * encoding description logic axioms. DiTrees are tree structures where:</p>
 *
 * <ul>
 * <li><strong>Single Root</strong> - Every tree has exactly one root vertex</li>
 * <li><strong>Directed Edges</strong> - Edges flow from parent to children</li>
 * <li><strong>No Cycles</strong> - Tree structure prohibits cycles</li>
 * <li><strong>Entity Vertices</strong> - Vertices reference concepts defining logical operators
 * and roles</li>
 * <li><strong>Immutable</strong> - Trees are immutable once constructed</li>
 * </ul>
 *
 * <p>DiTrees encode description logic expressions like:</p>
 * <pre>{@code
 * SUFFICIENT_SET
 *   └─ AND
 *       ├─ ROLE_GROUP
 *       │   ├─ SOME finding_site
 *       │   │   └─ Lung structure
 *       │   └─ SOME associated_morphology
 *       │       └─ Inflammation
 *       └─ SOME has_interpretation
 *           └─ Abnormal
 * }</pre>
 *
 * <p>This represents: "Pneumonia is sufficiently defined by: finding site is SOME lung structure,
 * associated morphology is SOME inflammation, and interpretation is SOME abnormal."</p>
 *
 * <h4>DiGraphEntity - Directed Graphs for Navigation</h4>
 * <p>{@link dev.ikm.tinkar.entity.graph.DiGraphEntity} represents directed graphs used for
 * navigational hierarchies and relationship networks. DiGraphs allow:</p>
 *
 * <ul>
 * <li><strong>Multiple Roots</strong> - Can have zero or more root vertices</li>
 * <li><strong>Cycles Allowed</strong> - Can represent cyclic relationships</li>
 * <li><strong>Multiple Paths</strong> - Multiple paths between vertices supported</li>
 * <li><strong>Rich Connectivity</strong> - Models complex relationship networks</li>
 * </ul>
 *
 * <h3>Vertex Types</h3>
 *
 * <h4>EntityVertex</h4>
 * <p>{@link dev.ikm.tinkar.entity.graph.EntityVertex} represents a vertex in entity-level graphs
 * (DiTreeEntity, DiGraphEntity). Each vertex:</p>
 *
 * <ul>
 * <li>Has a unique integer index within the graph</li>
 * <li>References a concept via NID (the vertex's meaning)</li>
 * <li>May have properties (additional data attached to the vertex)</li>
 * <li>Supports predecessor and successor relationships</li>
 * </ul>
 *
 * <pre>{@code
 * // Access vertex meaning
 * EntityVertex vertex = diTree.vertex(vertexIndex);
 * int meaningNid = vertex.getMeaningNid();
 * ConceptEntity meaning = Entity.getConceptForNid(meaningNid);
 *
 * // Check for specific meanings
 * if (vertex.getMeaningNid() == TinkarTerm.SUFFICIENT_SET.nid()) {
 *     // This is a sufficient definition
 * }
 * }</pre>
 *
 * <h4>VersionVertex</h4>
 * <p>{@link dev.ikm.tinkar.entity.graph.VersionVertex} represents a vertex in version-level graphs
 * (DiTreeVersion). Version vertices include STAMP metadata for temporal queries.</p>
 *
 * <h3>Description Logic Encoding</h3>
 *
 * <p>DiTrees encode description logic expressions using concept vertices with specific meanings:</p>
 *
 * <h4>Logical Operators</h4>
 * <ul>
 * <li><strong>AND</strong> - Conjunction of child concepts</li>
 * <li><strong>OR</strong> - Disjunction of child concepts</li>
 * <li><strong>NOT</strong> - Negation of child concept</li>
 * <li><strong>SUFFICIENT_SET</strong> - Sufficient definition (if-and-only-if semantics)</li>
 * <li><strong>NECESSARY_SET</strong> - Necessary conditions (if semantics)</li>
 * </ul>
 *
 * <h4>Existential Restrictions</h4>
 * <ul>
 * <li><strong>SOME</strong> - Existential quantification (∃ role.concept)</li>
 * <li>First child is the role (e.g., "finding site")</li>
 * <li>Second child is the filler concept (e.g., "Lung structure")</li>
 * </ul>
 *
 * <h4>Role Groups</h4>
 * <ul>
 * <li><strong>ROLE_GROUP</strong> - Groups related roles together</li>
 * <li>Used in SNOMED CT and other terminologies for role correlation</li>
 * <li>All roles within a group are interpreted together</li>
 * </ul>
 *
 * <pre>{@code
 * // Example: Traversing a DiTree to extract axiom structure
 * DiTreeEntity axiomTree = ...;
 * EntityVertex root = axiomTree.root();
 *
 * if (root.getMeaningNid() == TinkarTerm.SUFFICIENT_SET.nid()) {
 *     // Sufficient definition
 *     processChildren(axiomTree, root);
 * }
 *
 * void processChildren(DiTreeEntity tree, EntityVertex parent) {
 *     tree.successors(parent).forEach(childIndex -> {
 *         EntityVertex child = tree.vertex(childIndex);
 *         int meaning = child.getMeaningNid();
 *
 *         if (meaning == TinkarTerm.AND.nid()) {
 *             // Conjunction - process all children
 *             processChildren(tree, child);
 *         } else if (meaning == TinkarTerm.SOME.nid()) {
 *             // Existential - get role and filler
 *             IntList successors = tree.successors(child).toList();
 *             int roleNid = tree.vertex(successors.get(0)).getMeaningNid();
 *             int fillerNid = tree.vertex(successors.get(1)).getMeaningNid();
 *             // ... process restriction
 *         }
 *     });
 * }
 * }</pre>
 *
 * <h3>Graph Construction</h3>
 *
 * <p>Graphs are built using builder patterns or direct construction:</p>
 *
 * <pre>{@code
 * // Build a simple DiTree
 * MutableList<EntityVertex> vertices = Lists.mutable.empty();
 * MutableIntObjectMap<MutableIntList> successorMap = IntObjectMaps.mutable.empty();
 * MutableIntIntMap predecessorMap = IntIntMaps.mutable.empty();
 *
 * // Add root vertex (SUFFICIENT_SET)
 * int rootIndex = 0;
 * vertices.add(EntityVertex.make(
 *     TinkarTerm.SUFFICIENT_SET.nid(),
 *     rootIndex
 * ));
 *
 * // Add AND vertex
 * int andIndex = 1;
 * vertices.add(EntityVertex.make(
 *     TinkarTerm.AND.nid(),
 *     andIndex
 * ));
 * successorMap.put(rootIndex, IntLists.mutable.of(andIndex));
 * predecessorMap.put(andIndex, rootIndex);
 *
 * // ... add more vertices
 *
 * // Create immutable DiTree
 * DiTreeEntity tree = new DiTreeEntity(
 *     vertices.get(rootIndex),
 *     vertices.toImmutable(),
 *     successorMap.toImmutable(),
 *     predecessorMap.toImmutable()
 * );
 * }</pre>
 *
 * <h3>Graph Traversal</h3>
 *
 * <p>Graphs support multiple traversal patterns:</p>
 *
 * <h4>Depth-First Traversal</h4>
 * <pre>{@code
 * void depthFirst(DiTreeEntity tree, int vertexIndex, int depth) {
 *     EntityVertex vertex = tree.vertex(vertexIndex);
 *     System.out.println("  ".repeat(depth) +
 *         Entity.getConceptForNid(vertex.getMeaningNid()).description());
 *
 *     tree.successors(vertexIndex).forEach(childIndex -> {
 *         depthFirst(tree, childIndex, depth + 1);
 *     });
 * }
 * }</pre>
 *
 * <h4>Breadth-First Traversal</h4>
 * <pre>{@code
 * void breadthFirst(DiTreeEntity tree) {
 *     Queue<Integer> queue = new LinkedList<>();
 *     queue.add(tree.root().vertexIndex());
 *
 *     while (!queue.isEmpty()) {
 *         int vertexIndex = queue.poll();
 *         EntityVertex vertex = tree.vertex(vertexIndex);
 *         // Process vertex
 *
 *         tree.successors(vertexIndex).forEach(queue::add);
 *     }
 * }
 * }</pre>
 *
 * <h4>Visit Processor Pattern</h4>
 * <p>{@link dev.ikm.tinkar.entity.graph.VisitProcessor} provides a visitor pattern for
 * graph traversal with state tracking:</p>
 *
 * <pre>{@code
 * tree.processDepthFirst(vertex -> {
 *     // Process each vertex
 *     // Return true to continue, false to stop
 *     return true;
 * });
 * }</pre>
 *
 * <h3>Isomorphic Analysis</h3>
 *
 * <p>The {@link dev.ikm.tinkar.entity.graph.isomorphic} subpackage provides algorithms for
 * determining if two graphs are structurally equivalent:</p>
 *
 * <pre>{@code
 * DiTreeEntity tree1 = ...;
 * DiTreeEntity tree2 = ...;
 *
 * IsomorphicResults results = tree1.isomorphicResults(tree2);
 *
 * if (results.isIsomorphic()) {
 *     // Trees have same structure
 *     // Get vertex mapping
 *     ImmutableIntIntMap vertexMap = results.getVertexMapping();
 * }
 * }</pre>
 *
 * <p>Isomorphic analysis is useful for:</p>
 * <ul>
 * <li>Comparing stated vs. inferred axioms</li>
 * <li>Detecting duplicate definitions</li>
 * <li>Merging equivalent axioms from different sources</li>
 * <li>Quality assurance of terminology content</li>
 * </ul>
 *
 * <h3>Graph Utilities</h3>
 *
 * <h4>JGraphUtil</h4>
 * <p>{@link dev.ikm.tinkar.entity.graph.JGraphUtil} provides integration with the JGraphT library
 * for advanced graph algorithms:</p>
 * <ul>
 * <li>Cycle detection</li>
 * <li>Shortest path computation</li>
 * <li>Connected component analysis</li>
 * <li>Graph visualization export</li>
 * </ul>
 *
 * <h4>Vertex Status</h4>
 * <p>{@link dev.ikm.tinkar.entity.graph.VertexStatus} tracks vertex processing state during
 * graph algorithms:</p>
 * <ul>
 * <li><strong>UNVISITED</strong> - Not yet processed</li>
 * <li><strong>PROCESSING</strong> - Currently being processed</li>
 * <li><strong>PROCESSED</strong> - Fully processed</li>
 * </ul>
 *
 * <h3>Graph Serialization</h3>
 *
 * <p>Graphs can be serialized for storage and transmission:</p>
 *
 * <pre>{@code
 * // Serialize to bytes
 * ByteBuf buffer = ByteBuf.wrapForWriting(new byte[estimatedSize]);
 * diTree.writeTo(buffer);
 * byte[] serialized = buffer.array();
 *
 * // Deserialize from bytes
 * ByteBuf readBuffer = ByteBuf.wrapForReading(serialized);
 * DiTreeEntity restored = DiTreeEntity.make(readBuffer);
 * }</pre>
 *
 * <h3>Use in Logic Coordinates</h3>
 *
 * <p>DiTrees are stored as field values in semantic entities conforming to axiom patterns:</p>
 *
 * <pre>{@code
 * // Get axiom DiTree from semantic
 * LogicCoordinate logic = Coordinates.Logic.ElPlusPlus();
 * Latest<DiTreeEntity> axioms = logic.getStatedAxiomsVersion(
 *     conceptNid,
 *     stampCoord
 * );
 *
 * if (axioms.isPresent()) {
 *     DiTreeEntity tree = axioms.get();
 *     // Process axiom tree
 * }
 * }</pre>
 *
 * <h3>Performance Considerations</h3>
 *
 * <ul>
 * <li><strong>Immutability Benefits</strong> - Graphs are immutable, enabling safe caching</li>
 * <li><strong>Index-Based Access</strong> - Vertex access by index is O(1)</li>
 * <li><strong>Successor/Predecessor Maps</strong> - Efficient edge traversal</li>
 * <li><strong>Memory Efficiency</strong> - Compact representation using primitive collections</li>
 * </ul>
 *
 * <h3>Thread Safety</h3>
 *
 * <p>All graph implementations are immutable and thread-safe:</p>
 * <ul>
 * <li>Graphs can be safely shared across threads</li>
 * <li>No synchronization needed for graph traversal</li>
 * <li>Vertex and edge collections are immutable</li>
 * </ul>
 *
 * @see dev.ikm.tinkar.entity.graph.DiTreeEntity
 * @see dev.ikm.tinkar.entity.graph.DiGraphEntity
 * @see dev.ikm.tinkar.entity.graph.EntityVertex
 * @see dev.ikm.tinkar.entity.graph.VersionVertex
 * @see dev.ikm.tinkar.entity.graph.isomorphic
 * @see dev.ikm.tinkar.coordinate.logic.LogicCoordinate
 */
package dev.ikm.tinkar.entity.graph;
