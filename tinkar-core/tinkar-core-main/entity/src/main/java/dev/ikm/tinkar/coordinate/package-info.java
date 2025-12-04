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
 * <h2>Tinkar Coordinate System</h2>
 *
 * <p>The coordinate package provides a comprehensive declarative framework for managing knowledge
 * that changes over time. Coordinates specify how to view, navigate, and interact with temporal
 * knowledge graphs by controlling multiple dimensions of access and interpretation.</p>
 *
 * <h3>Core Coordinate Types</h3>
 *
 * <p>The coordinate system is organized into five primary coordinate types, each managing a
 * specific dimension of knowledge access:</p>
 *
 * <dl>
 * <dt><strong>STAMP Coordinates</strong> ({@link dev.ikm.tinkar.coordinate.stamp})</dt>
 * <dd>Control temporal versioning through Status, Time, Author, Module, and Path (STAMP) metadata.
 * STAMP coordinates determine which versions of concepts and semantics are visible by specifying:
 * <ul>
 *   <li>Active/inactive state filtering ({@link dev.ikm.tinkar.coordinate.stamp.StateSet})</li>
 *   <li>Temporal position on a development path ({@link dev.ikm.tinkar.coordinate.stamp.StampPosition})</li>
 *   <li>Module inclusion/exclusion and prioritization</li>
 *   <li>Path-based branching and versioning ({@link dev.ikm.tinkar.coordinate.stamp.StampPath})</li>
 * </ul>
 * STAMP coordinates answer: "What versions are visible at this point in time on this development path?"
 * </dd>
 *
 * <dt><strong>Language Coordinates</strong> ({@link dev.ikm.tinkar.coordinate.language})</dt>
 * <dd>Specify how to retrieve and display human-readable descriptions for concepts and other entities.
 * Language coordinates control:
 * <ul>
 *   <li>Natural language selection (e.g., English, Spanish)</li>
 *   <li>Dialect preferences (e.g., US English vs. GB English)</li>
 *   <li>Description type preferences (e.g., fully qualified names, regular names, definitions)</li>
 *   <li>Description pattern selection and module prioritization</li>
 * </ul>
 * Language coordinates answer: "How should I describe this concept to the user?"
 * </dd>
 *
 * <dt><strong>Logic Coordinates</strong> ({@link dev.ikm.tinkar.coordinate.logic})</dt>
 * <dd>Configure description logic reasoning and axiom processing. Logic coordinates specify:
 * <ul>
 *   <li>The classifier to use for reasoning (e.g., Snorocket)</li>
 *   <li>Description logic profile (e.g., EL++)</li>
 *   <li>Stated and inferred axiom patterns for concepts</li>
 *   <li>Root concepts and concept membership patterns</li>
 *   <li>Navigation patterns for stated and inferred hierarchies</li>
 * </ul>
 * Logic coordinates enable subsumption testing, classification, and reasoning over concept definitions.
 * </dd>
 *
 * <dt><strong>Navigation Coordinates</strong> ({@link dev.ikm.tinkar.coordinate.navigation})</dt>
 * <dd>Integrate organizing representations into navigational graphs that can be based on:
 * <ul>
 *   <li>Inferred subsumption relationships from description logic reasoning</li>
 *   <li>Stated (asserted) hierarchical relationships</li>
 *   <li>Non-defining associations and custom relationships</li>
 *   <li>Multiple navigation patterns combined in a single view</li>
 *   <li>Vertex filtering by state (active/inactive)</li>
 *   <li>Vertex sorting by custom patterns or natural order</li>
 * </ul>
 * Navigation coordinates define the graph structure used for browsing and traversing concepts.
 * </dd>
 *
 * <dt><strong>Edit Coordinates</strong> ({@link dev.ikm.tinkar.coordinate.edit})</dt>
 * <dd>Specify metadata for creating and modifying knowledge content. Edit coordinates control:
 * <ul>
 *   <li>Author attribution for changes</li>
 *   <li>Default module for new content</li>
 *   <li>Destination module for content being modularized</li>
 *   <li>Default path for new content creation</li>
 *   <li>Promotion path for content being promoted across branches</li>
 * </ul>
 * Edit coordinates ensure proper provenance and organization of changes during development.
 * </dd>
 * </dl>
 *
 * <h3>View Coordinates: Unified Access</h3>
 *
 * <p>The {@link dev.ikm.tinkar.coordinate.view.ViewCoordinate} combines all five coordinate types
 * into a single, cohesive specification that can be applied to the entire knowledge graph or to
 * individual entities. A view coordinate answers the comprehensive question:</p>
 *
 * <blockquote>
 * "How should I access, interpret, describe, navigate, and modify this knowledge at this specific
 * point in time, on this development path, in this language, using this reasoning profile?"
 * </blockquote>
 *
 * <h3>Coordinate Composition and Delegation</h3>
 *
 * <p>Coordinates follow a compositional design pattern with three implementation approaches:</p>
 *
 * <ol>
 * <li><strong>Record Implementations</strong> - Immutable value objects (e.g.,
 * {@link dev.ikm.tinkar.coordinate.stamp.StampCoordinateRecord}) that provide efficient,
 * thread-safe coordinate instances.</li>
 *
 * <li><strong>Delegate Pattern</strong> - Interfaces (e.g.,
 * {@link dev.ikm.tinkar.coordinate.stamp.StampCoordinateDelegate}) that allow coordinates to
 * delegate to other coordinate instances, enabling flexible composition.</li>
 *
 * <li><strong>Calculator Pattern</strong> - Specialized calculators (e.g.,
 * {@link dev.ikm.tinkar.coordinate.stamp.calculator.StampCalculator}) that combine coordinates
 * with computation logic for version resolution, description ranking, and navigation graph traversal.</li>
 * </ol>
 *
 * <h3>Coordinate Factory and Presets</h3>
 *
 * <p>The {@link dev.ikm.tinkar.coordinate.Coordinates} class provides factory methods and
 * common coordinate presets for typical use cases:</p>
 *
 * <ul>
 * <li>{@code Coordinates.View.DefaultView()} - A standard view combining development path,
 * US English language, EL++ logic, and inferred navigation</li>
 * <li>{@code Coordinates.Stamp.DevelopmentLatestActiveOnly()} - Latest active versions on the
 * development path</li>
 * <li>{@code Coordinates.Language.UsEnglishRegularName()} - US English with regular name preference</li>
 * <li>{@code Coordinates.Logic.ElPlusPlus()} - EL++ description logic profile with Snorocket classifier</li>
 * </ul>
 *
 * <h3>Key Design Principles</h3>
 *
 * <ul>
 * <li><strong>Declarative Specification</strong> - Coordinates describe what to access, not how to
 * access it</li>
 * <li><strong>Immutability</strong> - Coordinate records are immutable, enabling safe sharing and caching</li>
 * <li><strong>Composability</strong> - Coordinates can be combined and specialized through delegation</li>
 * <li><strong>Separation of Concerns</strong> - Each coordinate type manages one dimension of access</li>
 * <li><strong>UUID-based Identity</strong> - Coordinates generate content-based UUIDs for caching and equality</li>
 * </ul>
 *
 * <h3>Typical Usage Pattern</h3>
 *
 * <pre>{@code
 * // Create a view coordinate for accessing the knowledge graph
 * ViewCoordinateRecord view = Coordinates.View.DefaultView();
 *
 * // Access version-specific content using the view
 * ViewCalculator calculator = ViewCalculatorWithCache.getCalculator(view);
 * String description = calculator.getDescriptionText(conceptNid);
 *
 * // Navigate the hierarchy
 * IntIdSet parents = calculator.parentsOf(conceptNid);
 *
 * // Customize for specific needs
 * ViewCoordinateRecord spanishView = view.withLanguageCoordinate(
 *     Coordinates.Language.SpanishPreferredName()
 * );
 * }</pre>
 *
 * <h3>Package Organization</h3>
 *
 * <ul>
 * <li>{@link dev.ikm.tinkar.coordinate.edit} - Edit coordinate interfaces and records</li>
 * <li>{@link dev.ikm.tinkar.coordinate.language} - Language coordinate interfaces and records</li>
 * <li>{@link dev.ikm.tinkar.coordinate.language.calculator} - Language-specific calculation logic</li>
 * <li>{@link dev.ikm.tinkar.coordinate.logic} - Logic coordinate interfaces and records</li>
 * <li>{@link dev.ikm.tinkar.coordinate.logic.calculator} - Reasoning and axiom processing logic</li>
 * <li>{@link dev.ikm.tinkar.coordinate.navigation} - Navigation coordinate interfaces and records</li>
 * <li>{@link dev.ikm.tinkar.coordinate.navigation.calculator} - Graph navigation and traversal logic</li>
 * <li>{@link dev.ikm.tinkar.coordinate.stamp} - STAMP coordinate interfaces and records</li>
 * <li>{@link dev.ikm.tinkar.coordinate.stamp.calculator} - Version resolution and temporal logic</li>
 * <li>{@link dev.ikm.tinkar.coordinate.stamp.change} - Change tracking and versioning records</li>
 * <li>{@link dev.ikm.tinkar.coordinate.view} - Unified view coordinate interfaces and records</li>
 * <li>{@link dev.ikm.tinkar.coordinate.view.calculator} - Integrated view calculation logic</li>
 * </ul>
 *
 * @see dev.ikm.tinkar.coordinate.Coordinates
 * @see dev.ikm.tinkar.coordinate.view.ViewCoordinate
 * @see dev.ikm.tinkar.coordinate.stamp.StampCoordinate
 * @see dev.ikm.tinkar.coordinate.language.LanguageCoordinate
 * @see dev.ikm.tinkar.coordinate.logic.LogicCoordinate
 * @see dev.ikm.tinkar.coordinate.navigation.NavigationCoordinate
 */
package dev.ikm.tinkar.coordinate;
