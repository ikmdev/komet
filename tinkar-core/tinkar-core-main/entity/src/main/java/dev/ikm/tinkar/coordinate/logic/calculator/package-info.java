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
 * <h2>Logic Calculator System</h2>
 *
 * <p>Provides computational implementations that combine logic coordinates with reasoning
 * operations to retrieve axioms, process concept definitions, and support classification workflows.
 * Logic calculators provide high-level access to stated and inferred axioms with STAMP-based
 * version filtering and caching support.</p>
 *
 * <h3>Core Responsibilities</h3>
 *
 * <p>Logic calculators handle:</p>
 * <ul>
 * <li><strong>Axiom Retrieval</strong> - Access stated and inferred logical definitions</li>
 * <li><strong>Version Filtering</strong> - Apply STAMP coordinates to get latest axiom versions</li>
 * <li><strong>Premise Selection</strong> - Route queries to stated or inferred axiom patterns</li>
 * <li><strong>Axiom Caching</strong> - Store retrieved axioms for efficient re-access</li>
 * <li><strong>Pattern Resolution</strong> - Map premise types to appropriate semantic patterns</li>
 * <li><strong>Classification Support</strong> - Provide axioms to classifiers for reasoning</li>
 * </ul>
 *
 * <h3>Core Interfaces and Classes</h3>
 *
 * <h4>LogicCalculator</h4>
 * <p>The primary interface defining logic calculation operations. Key methods include:</p>
 *
 * <ul>
 * <li><strong>getAxiomTreeForEntity()</strong> - Get axiom tree for a concept with premise type</li>
 * <li><strong>getStatedAxiomTree()</strong> - Get stated axiom tree specifically</li>
 * <li><strong>getInferredAxiomTree()</strong> - Get inferred axiom tree specifically</li>
 * <li><strong>logicCoordinate()</strong> - Access the underlying logic coordinate</li>
 * </ul>
 *
 * <h4>LogicCalculatorWithCache</h4>
 * <p>Cached implementation that stores axiom tree lookup results for performance. Features:</p>
 * <ul>
 * <li>Thread-safe caching of DiTree axiom structures</li>
 * <li>Content-based coordinate UUID for cache key generation</li>
 * <li>Static factory method {@code getCalculator()} for instance retrieval</li>
 * <li>Automatic integration with STAMP calculator for version resolution</li>
 * </ul>
 *
 * <pre>{@code
 * // Get a cached calculator instance
 * StampCoordinateRecord stampCoord = Coordinates.Stamp.DevelopmentLatestActiveOnly();
 * LogicCoordinateRecord logicCoord = Coordinates.Logic.ElPlusPlus();
 *
 * LogicCalculator calculator =
 *     LogicCalculatorWithCache.getCalculator(stampCoord, logicCoord);
 *
 * // Retrieve axioms (cached on subsequent calls)
 * Latest<DiTreeEntity> statedAxioms =
 *     calculator.getStatedAxiomTree(conceptNid);
 * }</pre>
 *
 * <h4>LogicCalculatorDelegate</h4>
 * <p>Delegation interface allowing classes to provide logic calculator functionality by
 * delegating to an underlying calculator instance. Used in view calculators and other
 * composite coordinate types:</p>
 * <pre>{@code
 * public class ViewCalculator implements LogicCalculatorDelegate {
 *     private final LogicCalculator logicCalculator;
 *
 *     @Override
 *     public LogicCalculator logicCalculator() {
 *         return logicCalculator;
 *     }
 *
 *     // Inherits all LogicCalculator methods via delegation
 * }
 * }</pre>
 *
 * <h3>Axiom Tree Retrieval</h3>
 *
 * <p>The calculator provides multiple methods for accessing concept axioms:</p>
 *
 * <h4>Basic Axiom Access</h4>
 * <pre>{@code
 * LogicCalculator calc = LogicCalculatorWithCache.getCalculator(stamp, logic);
 *
 * // Get stated axioms
 * Latest<DiTreeEntity> stated = calc.getStatedAxiomTree(conceptNid);
 * if (stated.isPresent()) {
 *     DiTreeEntity axiomTree = stated.get();
 *     // Process stated logical definition
 * }
 *
 * // Get inferred axioms
 * Latest<DiTreeEntity> inferred = calc.getInferredAxiomTree(conceptNid);
 * if (inferred.isPresent()) {
 *     DiTreeEntity axiomTree = inferred.get();
 *     // Process inferred logical definition
 * }
 * }</pre>
 *
 * <h4>Premise-Parameterized Access</h4>
 * <pre>{@code
 * // Generic access with premise type parameter
 * PremiseType premiseType = PremiseType.STATED; // or INFERRED
 * Latest<DiTreeEntity> axioms =
 *     calc.getAxiomTreeForEntity(conceptNid, premiseType);
 *
 * // Useful for algorithms that work with either premise type
 * public void processAxioms(int conceptNid, PremiseType premise) {
 *     Latest<DiTreeEntity> axioms =
 *         calc.getAxiomTreeForEntity(conceptNid, premise);
 *     // Process regardless of stated vs. inferred
 * }
 * }</pre>
 *
 * <h4>Working with Latest Results</h4>
 * <pre>{@code
 * Latest<DiTreeEntity> axioms = calc.getStatedAxiomTree(conceptNid);
 *
 * // Check presence
 * if (axioms.isPresent()) {
 *     DiTreeEntity tree = axioms.get();
 *     // ... process tree
 * } else if (axioms.isAbsent()) {
 *     // No version matches STAMP coordinate
 * } else if (axioms.isContradiction()) {
 *     // Multiple contradictory versions
 * }
 *
 * // Use ifPresent for clean handling
 * axioms.ifPresent(tree -> {
 *     // Process tree
 * });
 * }</pre>
 *
 * <h3>DiTree Structure</h3>
 *
 * <p>Axioms are represented as directed tree structures ({@code DiTreeEntity}) encoding
 * description logic expressions:</p>
 *
 * <pre>{@code
 * DiTreeEntity axiomTree = axioms.get();
 *
 * // Check for sufficient definition
 * boolean isSufficientlyDefined =
 *     axiomTree.containsVertexWithMeaning(TinkarTerm.SUFFICIENT_SET);
 *
 * // Check for necessary definition
 * boolean isNecessary =
 *     axiomTree.containsVertexWithMeaning(TinkarTerm.NECESSARY_SET);
 *
 * // Traverse tree vertices
 * axiomTree.vertices().forEach(vertex -> {
 *     int meaningNid = vertex.getMeaningNid();
 *     // Process vertex based on meaning (AND, OR, SOME, etc.)
 * });
 * }</pre>
 *
 * <h3>Integration with Classification</h3>
 *
 * <p>Logic calculators provide axioms to classifiers in classification workflows:</p>
 *
 * <pre>{@code
 * // Classification workflow example
 * LogicCalculator calc = LogicCalculatorWithCache.getCalculator(stamp, logic);
 * Classifier classifier = getClassifier(logic.classifierNid());
 *
 * // Load stated axioms for all concepts
 * for (int conceptNid : conceptsToClassify) {
 *     Latest<DiTreeEntity> statedAxioms =
 *         calc.getStatedAxiomTree(conceptNid);
 *
 *     if (statedAxioms.isPresent()) {
 *         // Feed to classifier
 *         classifier.addAxiom(conceptNid, statedAxioms.get());
 *     }
 * }
 *
 * // Run classification
 * classifier.classify();
 *
 * // Store inferred results back to knowledge base
 * for (int conceptNid : conceptsToClassify) {
 *     DiTreeEntity inferredAxioms = classifier.getInferredAxioms(conceptNid);
 *     // Write to pattern specified by logic.inferredAxiomsPatternNid()
 * }
 * }</pre>
 *
 * <h3>STAMP Coordinate Integration</h3>
 *
 * <p>Logic calculators apply STAMP coordinates to filter axiom versions:</p>
 *
 * <ul>
 * <li><strong>Active/Inactive Filtering</strong> - Only active axioms returned if STAMP
 * specifies active-only state set</li>
 * <li><strong>Temporal Filtering</strong> - Only axioms visible at the STAMP position time
 * and path are returned</li>
 * <li><strong>Module Filtering</strong> - Only axioms from allowed modules are considered</li>
 * <li><strong>Latest Version Selection</strong> - When multiple versions exist, only the
 * latest according to STAMP rules is returned</li>
 * </ul>
 *
 * <pre>{@code
 * // Different STAMP coordinates yield different axiom versions
 * StampCoordinateRecord developmentStamp =
 *     Coordinates.Stamp.DevelopmentLatestActiveOnly();
 * StampCoordinateRecord masterStamp =
 *     Coordinates.Stamp.MasterLatestActiveOnly();
 *
 * LogicCalculator devCalc =
 *     LogicCalculatorWithCache.getCalculator(developmentStamp, logic);
 * LogicCalculator masterCalc =
 *     LogicCalculatorWithCache.getCalculator(masterStamp, logic);
 *
 * // May return different axiom versions
 * Latest<DiTreeEntity> devAxioms = devCalc.getStatedAxiomTree(conceptNid);
 * Latest<DiTreeEntity> masterAxioms = masterCalc.getStatedAxiomTree(conceptNid);
 * }</pre>
 *
 * <h3>Performance and Caching</h3>
 *
 * <p>LogicCalculatorWithCache provides significant performance benefits for axiom access:</p>
 *
 * <ul>
 * <li><strong>DiTree Caching</strong> - Axiom trees cached after first retrieval</li>
 * <li><strong>Coordinate-Based Keys</strong> - Cache keys derived from stamp + logic coordinate UUIDs</li>
 * <li><strong>Thread-Safe Access</strong> - Concurrent map-based caching</li>
 * <li><strong>Lazy Computation</strong> - Axioms loaded only when first requested</li>
 * </ul>
 *
 * <p>Performance characteristics:</p>
 * <ul>
 * <li>First access: Full semantic search + STAMP filtering + DiTree deserialization</li>
 * <li>Subsequent accesses: O(1) cache lookup</li>
 * <li>Memory overhead: One DiTreeEntity per concept per unique coordinate combination</li>
 * </ul>
 *
 * <h3>Common Usage Patterns</h3>
 *
 * <h4>Checking for Definitions</h4>
 * <pre>{@code
 * // Check if concept has sufficient definition
 * boolean isDefined(int conceptNid) {
 *     Latest<DiTreeEntity> stated = calc.getStatedAxiomTree(conceptNid);
 *     if (stated.isPresent()) {
 *         return stated.get().containsVertexWithMeaning(
 *             TinkarTerm.SUFFICIENT_SET);
 *     }
 *     return false;
 * }
 * }</pre>
 *
 * <h4>Comparing Stated vs. Inferred</h4>
 * <pre>{@code
 * // Compare stated and inferred axioms
 * Latest<DiTreeEntity> stated = calc.getStatedAxiomTree(conceptNid);
 * Latest<DiTreeEntity> inferred = calc.getInferredAxiomTree(conceptNid);
 *
 * if (stated.isPresent() && inferred.isPresent()) {
 *     DiTreeEntity statedTree = stated.get();
 *     DiTreeEntity inferredTree = inferred.get();
 *
 *     // Compare for authoring quality checks
 *     boolean match = statedTree.equals(inferredTree);
 * }
 * }</pre>
 *
 * <h4>Extracting Relationships</h4>
 * <pre>{@code
 * // Extract role groups from axiom tree
 * Latest<DiTreeEntity> axioms = calc.getStatedAxiomTree(conceptNid);
 * if (axioms.isPresent()) {
 *     DiTreeEntity tree = axioms.get();
 *
 *     // Find role groups (AND vertices with SOME children)
 *     tree.vertices().stream()
 *         .filter(v -> v.getMeaningNid() == TinkarTerm.AND.nid())
 *         .forEach(roleGroup -> {
 *             // Process relationships in role group
 *         });
 * }
 * }</pre>
 *
 * <h3>Integration with View Calculators</h3>
 *
 * <p>Logic calculators are typically accessed through view calculators:</p>
 * <pre>{@code
 * ViewCalculator viewCalc = ViewCalculatorWithCache.getCalculator(viewCoord);
 *
 * // ViewCalculator delegates to LogicCalculator
 * Latest<DiTreeEntity> axioms = viewCalc.getStatedAxiomTree(conceptNid);
 * boolean isDefined = viewCalc.isDefined(conceptNid);
 * }</pre>
 *
 * <h3>Thread Safety</h3>
 *
 * <p>All calculator implementations are thread-safe and can be shared across threads.
 * The cached implementation uses concurrent data structures for safe concurrent access
 * to axiom caches.</p>
 *
 * @see dev.ikm.tinkar.coordinate.logic.calculator.LogicCalculator
 * @see dev.ikm.tinkar.coordinate.logic.calculator.LogicCalculatorWithCache
 * @see dev.ikm.tinkar.coordinate.logic.calculator.LogicCalculatorDelegate
 * @see dev.ikm.tinkar.coordinate.logic.LogicCoordinate
 * @see dev.ikm.tinkar.coordinate.logic.PremiseType
 * @see dev.ikm.tinkar.entity.graph.DiTreeEntity
 */
package dev.ikm.tinkar.coordinate.logic.calculator;
