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
 * <h2>View Coordinate System</h2>
 *
 * <p>Provides unified coordinates that combine all five coordinate types (STAMP, Language, Logic,
 * Navigation, and Edit) into a single, cohesive specification for accessing and interacting with
 * the Tinkar knowledge graph. View coordinates answer the comprehensive question: "How should I
 * access, interpret, describe, navigate, and modify this knowledge?"</p>
 *
 * <h3>Core Concept: Unified Access</h3>
 *
 * <p>A view coordinate represents a complete, self-contained specification of:</p>
 * <ul>
 * <li><strong>Temporal Context</strong> - Which versions are visible (STAMP coordinate)</li>
 * <li><strong>Linguistic Context</strong> - How entities are described (Language coordinate)</li>
 * <li><strong>Logical Context</strong> - How reasoning is performed (Logic coordinate)</li>
 * <li><strong>Navigational Context</strong> - How hierarchies are traversed (Navigation coordinate)</li>
 * <li><strong>Editorial Context</strong> - How changes are attributed (Edit coordinate)</li>
 * </ul>
 *
 * <p>By bundling all coordinate types together, view coordinates ensure consistent interpretation
 * of knowledge across all operations: querying, browsing, reasoning, and editing.</p>
 *
 * <h3>Core Responsibilities</h3>
 *
 * <p>View coordinates unify access to:</p>
 * <ul>
 * <li><strong>Version Resolution</strong> - Via STAMP coordinate</li>
 * <li><strong>Description Selection</strong> - Via language coordinate(s)</li>
 * <li><strong>Axiom Retrieval</strong> - Via logic coordinate</li>
 * <li><strong>Graph Traversal</strong> - Via navigation coordinate</li>
 * <li><strong>Change Attribution</strong> - Via edit coordinate</li>
 * </ul>
 *
 * <h3>Core Interfaces</h3>
 *
 * <h4>ViewCoordinate</h4>
 * <p>Primary interface defining unified coordinate access. Key methods include:</p>
 * <ul>
 * <li><strong>stampCoordinate()</strong> - Access STAMP coordinate</li>
 * <li><strong>languageCoordinateIterable()</strong> - Access cascading language coordinates</li>
 * <li><strong>logicCoordinate()</strong> - Access logic coordinate</li>
 * <li><strong>navigationCoordinate()</strong> - Access navigation coordinate</li>
 * <li><strong>editCoordinate()</strong> - Access edit coordinate (via delegation)</li>
 * <li><strong>toViewCoordinateRecord()</strong> - Convert to immutable record</li>
 * <li><strong>languageCalculator()</strong> - Get language calculator for this view</li>
 * </ul>
 *
 * <h4>ViewCoordinateRecord</h4>
 * <p>Immutable record implementation providing thread-safe view coordinates. Created via factory
 * methods or explicit construction:</p>
 *
 * <pre>{@code
 * // Use default view
 * ViewCoordinateRecord view = Coordinates.View.DefaultView();
 *
 * // Or construct custom view
 * ViewCoordinateRecord customView = ViewCoordinateRecord.make(
 *     Coordinates.Stamp.DevelopmentLatestActiveOnly(),
 *     Lists.immutable.of(Coordinates.Language.UsEnglishRegularName()),
 *     Coordinates.Logic.ElPlusPlus(),
 *     Coordinates.Navigation.inferred(),
 *     Coordinates.Edit.Default()
 * );
 * }</pre>
 *
 * <h4>ViewCoordinateDelegate</h4>
 * <p>Delegation interface allowing classes to implement ViewCoordinate by delegating to an
 * underlying instance. Used in calculator implementations and service classes.</p>
 *
 * <h3>Cascading Language Coordinates</h3>
 *
 * <p>View coordinates support multiple language coordinates in priority order, enabling graceful
 * fallback when descriptions aren't available in the preferred language:</p>
 *
 * <pre>{@code
 * ViewCoordinateRecord multilingualView = ViewCoordinateRecord.make(
 *     stampCoord,
 *     Lists.immutable.of(
 *         Coordinates.Language.SpanishPreferredName(),      // 1st: Spanish
 *         Coordinates.Language.UsEnglishRegularName(),      // 2nd: English
 *         Coordinates.Language.AnyLanguageRegularName()     // 3rd: Any language
 *     ),
 *     logicCoord,
 *     navCoord,
 *     editCoord
 * );
 * }</pre>
 *
 * <h3>Default View Configuration</h3>
 *
 * <p>The standard default view provides sensible defaults for most use cases:</p>
 * <ul>
 * <li><strong>STAMP</strong> - Latest active and inactive versions on development path</li>
 * <li><strong>Language</strong> - US English, preferring regular names</li>
 * <li><strong>Logic</strong> - EL++ profile with Snorocket classifier</li>
 * <li><strong>Navigation</strong> - Inferred taxonomy from classification</li>
 * <li><strong>Edit</strong> - Default user/module/path configuration</li>
 * </ul>
 *
 * <pre>{@code
 * ViewCoordinateRecord view = Coordinates.View.DefaultView();
 * // Equivalent to:
 * ViewCoordinateRecord view = ViewCoordinateRecord.make(
 *     Coordinates.Stamp.DevelopmentLatest(),
 *     Lists.immutable.of(Coordinates.Language.UsEnglishRegularName()),
 *     Coordinates.Logic.ElPlusPlus(),
 *     Coordinates.Navigation.inferred(),
 *     Coordinates.Edit.Default()
 * );
 * }</pre>
 *
 * <h3>View Coordinate Customization</h3>
 *
 * <p>Create specialized views by modifying individual coordinates:</p>
 *
 * <pre>{@code
 * ViewCoordinateRecord baseView = Coordinates.View.DefaultView();
 *
 * // Spanish view (same logic, navigation, edit)
 * ViewCoordinateRecord spanishView = baseView.withLanguageCoordinate(
 *     Coordinates.Language.SpanishPreferredName()
 * );
 *
 * // Historical view (different time)
 * ViewCoordinateRecord historicView = baseView.withStampCoordinate(
 *     Coordinates.Stamp.DevelopmentLatestActiveOnly()
 *         .withStampPosition(StampPositionRecord.make(historicTime, path))
 * );
 *
 * // Stated navigation view (author-asserted hierarchy)
 * ViewCoordinateRecord statedView = baseView.withNavigationCoordinate(
 *     Coordinates.Navigation.stated()
 * );
 * }</pre>
 *
 * <h3>Integration with View Calculators</h3>
 *
 * <p>View coordinates are typically used through {@link dev.ikm.tinkar.coordinate.view.calculator.ViewCalculator}
 * implementations that provide high-level operations combining all coordinate types:</p>
 *
 * <pre>{@code
 * ViewCoordinateRecord view = Coordinates.View.DefaultView();
 * ViewCalculator calculator = ViewCalculatorWithCache.getCalculator(view);
 *
 * // Unified operations combining multiple coordinate types
 * String description = calculator.getDescriptionText(conceptNid);  // Language + STAMP
 * IntIdSet parents = calculator.parentsOf(conceptNid);             // Navigation + STAMP
 * Latest<DiTreeEntity> axioms = calculator.getStatedAxiomTree(conceptNid);  // Logic + STAMP
 * boolean isDefined = calculator.isDefined(conceptNid);            // Logic + STAMP
 * }</pre>
 *
 * <h3>Common View Patterns</h3>
 *
 * <h4>Development View</h4>
 * <p>For active development and authoring:</p>
 * <pre>{@code
 * ViewCoordinateRecord devView = ViewCoordinateRecord.make(
 *     Coordinates.Stamp.DevelopmentLatestActiveOnly(),
 *     Lists.immutable.of(Coordinates.Language.UsEnglishRegularName()),
 *     Coordinates.Logic.ElPlusPlus(),
 *     Coordinates.Navigation.stated(),  // Use author-asserted hierarchy
 *     Coordinates.Edit.Default()
 * );
 * }</pre>
 *
 * <h4>Production View</h4>
 * <p>For end-user applications:</p>
 * <pre>{@code
 * ViewCoordinateRecord prodView = ViewCoordinateRecord.make(
 *     Coordinates.Stamp.MasterLatestActiveOnly(),
 *     Lists.immutable.of(Coordinates.Language.UsEnglishRegularName()),
 *     Coordinates.Logic.ElPlusPlus(),
 *     Coordinates.Navigation.inferred(),  // Use classified taxonomy
 *     Coordinates.Edit.Default()
 * );
 * }</pre>
 *
 * <h4>Administrative View</h4>
 * <p>For system administration and analysis:</p>
 * <pre>{@code
 * ViewCoordinateRecord adminView = ViewCoordinateRecord.make(
 *     Coordinates.Stamp.DevelopmentLatest(),  // Include inactive
 *     Lists.immutable.of(Coordinates.Language.AnyLanguageFullyQualifiedName()),
 *     Coordinates.Logic.ElPlusPlus(),
 *     Coordinates.Navigation.inferred(),
 *     Coordinates.Edit.Default()
 * );
 * }</pre>
 *
 * <h4>Multilingual View</h4>
 * <p>For international applications:</p>
 * <pre>{@code
 * ViewCoordinateRecord multilingualView = ViewCoordinateRecord.make(
 *     Coordinates.Stamp.MasterLatestActiveOnly(),
 *     Lists.immutable.of(
 *         Coordinates.Language.SpanishPreferredName(),
 *         Coordinates.Language.UsEnglishRegularName(),
 *         Coordinates.Language.AnyLanguageRegularName()
 *     ),
 *     Coordinates.Logic.ElPlusPlus(),
 *     Coordinates.Navigation.inferred(),
 *     Coordinates.Edit.Default()
 * );
 * }</pre>
 *
 * <h3>User Preference Integration</h3>
 *
 * <p>View coordinates are ideal for storing user preferences:</p>
 * <pre>{@code
 * // Build view from user preferences
 * ViewCoordinateRecord buildUserView(UserPreferences prefs) {
 *     return ViewCoordinateRecord.make(
 *         buildStampFromPrefs(prefs),
 *         buildLanguagesFromPrefs(prefs),
 *         buildLogicFromPrefs(prefs),
 *         buildNavigationFromPrefs(prefs),
 *         buildEditFromPrefs(prefs)
 *     );
 * }
 *
 * // Store as user preference
 * UUID viewUuid = userView.getViewCoordinateUuid();
 * userPrefs.setPreferredView(viewUuid);
 * }</pre>
 *
 * <h3>Vertex Sorting</h3>
 *
 * <p>View coordinates include vertex sorting specifications via navigation coordinate and
 * additional sort customization:</p>
 * <ul>
 * <li>{@link dev.ikm.tinkar.coordinate.view.VertexSort} - Interface for sort strategies</li>
 * <li>{@link dev.ikm.tinkar.coordinate.view.VertexSortNaturalOrder} - Alphabetical sorting</li>
 * <li>{@link dev.ikm.tinkar.coordinate.view.VertexSortNone} - No sorting (faster)</li>
 * </ul>
 *
 * <h3>Coordinate Consistency</h3>
 *
 * <p>View coordinates ensure consistency across operations:</p>
 * <ul>
 * <li>Same STAMP coordinate used for all version resolution</li>
 * <li>Same language cascades used for all descriptions</li>
 * <li>Same logic coordinate used for all reasoning</li>
 * <li>Same navigation coordinate used for all traversal</li>
 * <li>Same edit coordinate used for all modifications</li>
 * </ul>
 *
 * <p>This consistency eliminates ambiguity and ensures predictable behavior.</p>
 *
 * <h3>Thread Safety and Immutability</h3>
 *
 * <p>ViewCoordinateRecord instances are immutable and thread-safe, suitable for:</p>
 * <ul>
 * <li>Sharing across multiple threads and requests</li>
 * <li>Use as map keys for view-specific caches</li>
 * <li>Storage in static fields or singletons</li>
 * <li>Serialization and deserialization</li>
 * <li>Web session storage</li>
 * </ul>
 *
 * <h3>Performance Considerations</h3>
 *
 * <ul>
 * <li><strong>Content-Based UUIDs</strong> - Views generate stable UUIDs for calculator caching</li>
 * <li><strong>Calculator Reuse</strong> - Same view coordinate = same cached calculator</li>
 * <li><strong>Immutability Benefits</strong> - No defensive copying needed</li>
 * <li><strong>Coordinate Caching</strong> - Reuse standard view coordinates when possible</li>
 * </ul>
 *
 * <h3>Use in Service Layers</h3>
 *
 * <p>View coordinates are commonly passed as context to service methods:</p>
 * <pre>{@code
 * public class ConceptService {
 *     public ConceptDTO getConcept(int conceptNid, ViewCoordinate view) {
 *         ViewCalculator calc = ViewCalculatorWithCache.getCalculator(
 *             view.toViewCoordinateRecord()
 *         );
 *
 *         String name = calc.getDescriptionText(conceptNid);
 *         IntIdSet parents = calc.parentsOf(conceptNid);
 *
 *         return new ConceptDTO(conceptNid, name, parents);
 *     }
 * }
 * }</pre>
 *
 * @see dev.ikm.tinkar.coordinate.view.ViewCoordinate
 * @see dev.ikm.tinkar.coordinate.view.ViewCoordinateRecord
 * @see dev.ikm.tinkar.coordinate.view.ViewCoordinateDelegate
 * @see dev.ikm.tinkar.coordinate.view.calculator
 * @see dev.ikm.tinkar.coordinate.stamp.StampCoordinate
 * @see dev.ikm.tinkar.coordinate.language.LanguageCoordinate
 * @see dev.ikm.tinkar.coordinate.logic.LogicCoordinate
 * @see dev.ikm.tinkar.coordinate.navigation.NavigationCoordinate
 * @see dev.ikm.tinkar.coordinate.edit.EditCoordinate
 * @see dev.ikm.tinkar.coordinate.Coordinates.View
 */
package dev.ikm.tinkar.coordinate.view;
