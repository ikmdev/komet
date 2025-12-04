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
 * <h2>View Calculator System</h2>
 *
 * <p>Provides the highest-level computational interface for accessing the Tinkar knowledge graph.
 * View calculators combine all specialized calculators (STAMP, Language, Logic, Navigation) into
 * a single, unified API that delegates to the appropriate calculator based on the view coordinate
 * specification.</p>
 *
 * <h3>Core Concept: Unified Calculation</h3>
 *
 * <p>View calculators provide a "one-stop shop" for knowledge graph access by integrating:</p>
 * <ul>
 * <li><strong>Version Resolution</strong> - Via delegated StampCalculator</li>
 * <li><strong>Description Retrieval</strong> - Via delegated LanguageCalculator</li>
 * <li><strong>Axiom Access</strong> - Via delegated LogicCalculator</li>
 * <li><strong>Graph Traversal</strong> - Via delegated NavigationCalculator</li>
 * <li><strong>Consistent Context</strong> - All operations use the same view coordinate</li>
 * </ul>
 *
 * <h3>Core Interfaces and Classes</h3>
 *
 * <h4>ViewCalculator</h4>
 * <p>The primary interface providing unified access to all calculator operations. Extends all
 * four calculator delegate interfaces:</p>
 * <ul>
 * <li><strong>StampCalculatorDelegate</strong> - Version resolution methods</li>
 * <li><strong>LanguageCalculatorDelegate</strong> - Description retrieval methods</li>
 * <li><strong>LogicCalculatorDelegate</strong> - Axiom and reasoning methods</li>
 * <li><strong>NavigationCalculatorDelegate</strong> - Graph traversal methods</li>
 * </ul>
 *
 * <p>This means ViewCalculator instances have direct access to all methods from all four
 * specialized calculator types:</p>
 *
 * <pre>{@code
 * ViewCalculator calc = ViewCalculatorWithCache.getCalculator(viewCoord);
 *
 * // STAMP operations (from StampCalculatorDelegate)
 * Latest<ConceptVersion> latest = calc.latest(concept);
 *
 * // Language operations (from LanguageCalculatorDelegate)
 * String description = calc.getDescriptionText(conceptNid);
 *
 * // Logic operations (from LogicCalculatorDelegate)
 * Latest<DiTreeEntity> axioms = calc.getStatedAxiomTree(conceptNid);
 *
 * // Navigation operations (from NavigationCalculatorDelegate)
 * IntIdSet parents = calc.parentsOf(conceptNid);
 * }</pre>
 *
 * <h4>ViewCalculatorWithCache</h4>
 * <p>Cached implementation that maintains separate cached calculator instances for each
 * coordinate type. Features:</p>
 * <ul>
 * <li>Delegates to cached specialized calculators</li>
 * <li>Content-based view coordinate UUID for cache keys</li>
 * <li>Static factory method {@code getCalculator()} for instance retrieval</li>
 * <li>Thread-safe access to all calculator caches</li>
 * <li>Single point of access for all knowledge graph operations</li>
 * </ul>
 *
 * <pre>{@code
 * // Get or create cached calculator for view
 * ViewCoordinateRecord view = Coordinates.View.DefaultView();
 * ViewCalculator calculator = ViewCalculatorWithCache.getCalculator(view);
 *
 * // All operations automatically use appropriate cached calculators
 * String text = calculator.getDescriptionText(conceptNid);
 * IntIdSet children = calculator.childrenOf(conceptNid);
 * }</pre>
 *
 * <h4>ViewCalculatorDelegate</h4>
 * <p>Delegation interface allowing classes to provide ViewCalculator functionality. Not typically
 * used directly by application code, but useful for building higher-level service abstractions.</p>
 *
 * <h3>Calculator Delegation Architecture</h3>
 *
 * <p>ViewCalculatorWithCache maintains instances of all specialized calculators:</p>
 *
 * <pre>{@code
 * public class ViewCalculatorWithCache implements ViewCalculator {
 *     private final ViewCoordinateRecord viewCoordinateRecord;
 *     private final StampCalculator stampCalculator;
 *     private final LanguageCalculator languageCalculator;
 *     private final LogicCalculator logicCalculator;
 *     private final NavigationCalculator navigationCalculator;
 *
 *     // Delegate interface implementations return appropriate calculator
 *     @Override
 *     public StampCalculator stampCalculator() {
 *         return stampCalculator;
 *     }
 *
 *     @Override
 *     public LanguageCalculator languageCalculator() {
 *         return languageCalculator;
 *     }
 *
 *     // ... etc for logic and navigation
 * }
 * }</pre>
 *
 * <p>When you call methods on ViewCalculator, they automatically delegate to the appropriate
 * specialized calculator, all using consistent coordinates from the view.</p>
 *
 * <h3>Comprehensive Example</h3>
 *
 * <pre>{@code
 * // Create view and calculator
 * ViewCoordinateRecord view = Coordinates.View.DefaultView();
 * ViewCalculator calc = ViewCalculatorWithCache.getCalculator(view);
 *
 * // Version resolution (STAMP)
 * ConceptEntity concept = Entity.getConceptForNid(conceptNid);
 * Latest<ConceptVersion> latest = calc.latest(concept);
 *
 * if (latest.isPresent()) {
 *     ConceptVersion version = latest.get();
 *
 *     // Description (Language + STAMP)
 *     String preferredName = calc.getDescriptionText(conceptNid);
 *     String fqn = calc.getFullyQualifiedName(conceptNid).orElse("Unknown");
 *
 *     // Navigation (Navigation + STAMP)
 *     IntIdSet parents = calc.parentsOf(conceptNid);
 *     IntIdSet children = calc.childrenOf(conceptNid);
 *     boolean isRoot = parents.isEmpty();
 *
 *     // Logic (Logic + STAMP)
 *     Latest<DiTreeEntity> statedAxioms = calc.getStatedAxiomTree(conceptNid);
 *     boolean isDefined = calc.isDefined(conceptNid);
 *
 *     // Reasoning (Navigation + Logic + STAMP)
 *     IntIdSet ancestors = calc.ancestorsOf(conceptNid);
 *     boolean isKindOf = calc.isDescendentOf(conceptNid, parentConceptNid);
 *
 *     // All operations use consistent view coordinate!
 * }
 * }</pre>
 *
 * <h3>Common Usage Patterns</h3>
 *
 * <h4>Concept Information Retrieval</h4>
 * <pre>{@code
 * public ConceptInfo getConceptInfo(int conceptNid, ViewCoordinate view) {
 *     ViewCalculator calc = ViewCalculatorWithCache.getCalculator(
 *         view.toViewCoordinateRecord()
 *     );
 *
 *     return ConceptInfo.builder()
 *         .nid(conceptNid)
 *         .preferredName(calc.getDescriptionText(conceptNid))
 *         .fullyQualifiedName(calc.getFullyQualifiedName(conceptNid).orElse(""))
 *         .parents(calc.parentsOf(conceptNid))
 *         .children(calc.childrenOf(conceptNid))
 *         .isDefined(calc.isDefined(conceptNid))
 *         .isActive(calc.isLatestActive(conceptNid))
 *         .build();
 * }
 * }</pre>
 *
 * <h4>Hierarchy Browsing</h4>
 * <pre>{@code
 * public HierarchyNode buildHierarchy(int rootNid, ViewCoordinate view, int depth) {
 *     ViewCalculator calc = ViewCalculatorWithCache.getCalculator(
 *         view.toViewCoordinateRecord()
 *     );
 *
 *     return buildNode(rootNid, calc, depth);
 * }
 *
 * private HierarchyNode buildNode(int nid, ViewCalculator calc, int depth) {
 *     HierarchyNode node = new HierarchyNode(
 *         nid,
 *         calc.getDescriptionText(nid)
 *     );
 *
 *     if (depth > 0) {
 *         calc.childrenOf(nid).forEach(childNid -> {
 *             node.addChild(buildNode(childNid, calc, depth - 1));
 *         });
 *     }
 *
 *     return node;
 * }
 * }</pre>
 *
 * <h4>Search with Description and Hierarchy</h4>
 * <pre>{@code
 * public List<SearchResult> search(String query, ViewCoordinate view) {
 *     ViewCalculator calc = ViewCalculatorWithCache.getCalculator(
 *         view.toViewCoordinateRecord()
 *     );
 *
 *     List<SearchResult> results = new ArrayList<>();
 *
 *     // Search for matching concepts (implementation dependent)
 *     IntIdSet matches = searchIndex.find(query);
 *
 *     matches.forEach(conceptNid -> {
 *         // Get description and hierarchy info using same calculator
 *         String name = calc.getDescriptionText(conceptNid);
 *         IntIdSet parents = calc.parentsOf(conceptNid);
 *
 *         // Get parent names for breadcrumb
 *         List<String> parentNames = parents.stream()
 *             .map(calc::getDescriptionText)
 *             .collect(Collectors.toList());
 *
 *         results.add(new SearchResult(conceptNid, name, parentNames));
 *     });
 *
 *     return results;
 * }
 * }</pre>
 *
 * <h4>Subsumption Testing with Descriptions</h4>
 * <pre>{@code
 * public boolean isKindOf(int specificNid, int generalNid, ViewCoordinate view) {
 *     ViewCalculator calc = ViewCalculatorWithCache.getCalculator(
 *         view.toViewCoordinateRecord()
 *     );
 *
 *     // Navigation + Logic determine subsumption
 *     boolean subsumes = calc.isDescendentOf(specificNid, generalNid);
 *
 *     if (subsumes) {
 *         // Use language for logging
 *         String specificName = calc.getDescriptionText(specificNid);
 *         String generalName = calc.getDescriptionText(generalNid);
 *         LOG.info("{} is a kind of {}", specificName, generalName);
 *     }
 *
 *     return subsumes;
 * }
 * }</pre>
 *
 * <h4>Definition Analysis</h4>
 * <pre>{@code
 * public DefinitionAnalysis analyzeDefinition(int conceptNid, ViewCoordinate view) {
 *     ViewCalculator calc = ViewCalculatorWithCache.getCalculator(
 *         view.toViewCoordinateRecord()
 *     );
 *
 *     DefinitionAnalysis analysis = new DefinitionAnalysis();
 *     analysis.setConceptName(calc.getDescriptionText(conceptNid));
 *
 *     // Get stated axioms
 *     Latest<DiTreeEntity> statedAxioms = calc.getStatedAxiomTree(conceptNid);
 *     if (statedAxioms.isPresent()) {
 *         DiTreeEntity tree = statedAxioms.get();
 *         analysis.setStatedAxioms(tree);
 *         analysis.setSufficientlyDefined(
 *             tree.containsVertexWithMeaning(TinkarTerm.SUFFICIENT_SET)
 *         );
 *     }
 *
 *     // Get inferred axioms
 *     Latest<DiTreeEntity> inferredAxioms = calc.getInferredAxiomTree(conceptNid);
 *     if (inferredAxioms.isPresent()) {
 *         analysis.setInferredAxioms(inferredAxioms.get());
 *     }
 *
 *     // Get inferred parents for comparison
 *     IntIdSet inferredParents = calc.parentsOf(conceptNid);
 *     analysis.setInferredParentNames(
 *         inferredParents.stream()
 *             .map(calc::getDescriptionText)
 *             .collect(Collectors.toList())
 *     );
 *
 *     return analysis;
 * }
 * }</pre>
 *
 * <h3>Performance Benefits</h3>
 *
 * <p>Using ViewCalculatorWithCache provides compound performance benefits:</p>
 * <ul>
 * <li><strong>Single Calculator Instance</strong> - Reuse same calculator for entire request/session</li>
 * <li><strong>Cascading Caches</strong> - All underlying calculators cache their results</li>
 * <li><strong>Consistent Coordinates</strong> - Same view = same cache = no redundant computation</li>
 * <li><strong>Lazy Delegation</strong> - Only creates specialized calculators when first needed</li>
 * </ul>
 *
 * <p>Performance characteristics for repeated operations on same view:</p>
 * <ul>
 * <li>First getDescriptionText(): Full language calculation + STAMP filtering</li>
 * <li>Subsequent getDescriptionText(): O(1) cache lookup</li>
 * <li>First parentsOf(): Full graph construction + filtering</li>
 * <li>Subsequent parentsOf(): O(1) cache lookup</li>
 * <li>Mixed operations: Each calculator caches independently</li>
 * </ul>
 *
 * <h3>View Coordinate Consistency</h3>
 *
 * <p>A critical advantage of ViewCalculator is coordinate consistency. All operations
 * automatically use the same view specification:</p>
 *
 * <pre>{@code
 * ViewCalculator calc = ViewCalculatorWithCache.getCalculator(view);
 *
 * // All these operations see the same version
 * Latest<ConceptVersion> version = calc.latest(concept);
 * Latest<DiTreeEntity> axioms = calc.getStatedAxiomTree(conceptNid);
 * IntIdSet parents = calc.parentsOf(conceptNid);
 * String description = calc.getDescriptionText(conceptNid);
 *
 * // Same STAMP coordinate → same version visibility
 * // Same language coordinate → same description selection
 * // Same navigation coordinate → same hierarchy structure
 * // Same logic coordinate → same axiom patterns
 * }</pre>
 *
 * <p>This eliminates subtle bugs from mixing incompatible coordinates.</p>
 *
 * <h3>Thread Safety</h3>
 *
 * <p>ViewCalculator implementations are thread-safe and can be shared across threads. The
 * cached implementation uses thread-safe calculator instances internally.</p>
 *
 * <h3>Best Practices</h3>
 *
 * <ol>
 * <li><strong>Reuse Calculators</strong> - Get calculator once per request/session, reuse for all operations</li>
 * <li><strong>Use Consistent Views</strong> - Don't mix multiple view coordinates in same operation</li>
 * <li><strong>Cache View Coordinates</strong> - Standard views like "DefaultView" can be cached statically</li>
 * <li><strong>Prefer View over Specialized Calculators</strong> - Use ViewCalculator unless you specifically
 * need only one calculator type</li>
 * <li><strong>Handle Latest Properly</strong> - Always check Latest.isPresent() before accessing versions</li>
 * </ol>
 *
 * @see dev.ikm.tinkar.coordinate.view.calculator.ViewCalculator
 * @see dev.ikm.tinkar.coordinate.view.calculator.ViewCalculatorWithCache
 * @see dev.ikm.tinkar.coordinate.view.calculator.ViewCalculatorDelegate
 * @see dev.ikm.tinkar.coordinate.view.ViewCoordinate
 * @see dev.ikm.tinkar.coordinate.stamp.calculator.StampCalculator
 * @see dev.ikm.tinkar.coordinate.language.calculator.LanguageCalculator
 * @see dev.ikm.tinkar.coordinate.logic.calculator.LogicCalculator
 * @see dev.ikm.tinkar.coordinate.navigation.calculator.NavigationCalculator
 */
package dev.ikm.tinkar.coordinate.view.calculator;
