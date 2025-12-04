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
 * <h2>Language Coordinate System</h2>
 *
 * <p>Provides coordinates that specify how to retrieve and display human-readable descriptions
 * for concepts, semantics, and other entities in the Tinkar knowledge graph. Language coordinates
 * enable multilingual support, dialect preferences, and description type prioritization.</p>
 *
 * <h3>Core Responsibilities</h3>
 *
 * <p>Language coordinates control five dimensions of description selection and ranking:</p>
 *
 * <dl>
 * <dt><strong>Natural Language</strong></dt>
 * <dd>Specifies the primary human language (e.g., English, Spanish, French). The language concept
 * NID identifies which language descriptions should be preferred. Special value {@code TinkarTerm.LANGUAGE}
 * acts as a wildcard, matching any language.</dd>
 *
 * <dt><strong>Description Patterns</strong></dt>
 * <dd>Defines which description patterns are acceptable for retrieval. Patterns specify the
 * structure and semantics of descriptions. Most commonly uses {@code TinkarTerm.DESCRIPTION_PATTERN},
 * but custom patterns can be specified for specialized vocabularies.</dd>
 *
 * <dt><strong>Description Types</strong></dt>
 * <dd>Prioritized list of description types to search for, including:
 * <ul>
 *   <li><strong>Fully Qualified Name (FQN)</strong> - Complete, unambiguous name with qualifiers</li>
 *   <li><strong>Regular Name</strong> - Common or preferred term</li>
 *   <li><strong>Definition</strong> - Formal textual definition</li>
 * </ul>
 * The first matching description type found is returned, allowing fallback behavior.</dd>
 *
 * <dt><strong>Dialect Preferences</strong></dt>
 * <dd>Ordered list of dialect patterns (e.g., US English, GB English, Canadian French) used to
 * rank descriptions when multiple descriptions of the same type exist. Earlier dialects in the
 * list are preferred over later ones.</dd>
 *
 * <dt><strong>Module Preferences</strong></dt>
 * <dd>Ordered list of modules used to adjudicate when multiple descriptions match all other
 * criteria. For example, when multiple modules provide different preferred names for the same
 * concept, module preference determines which one to return.</dd>
 * </dl>
 *
 * <h3>Description Selection Algorithm</h3>
 *
 * <p>When retrieving a description for a concept, the language coordinate applies a multi-stage
 * ranking algorithm:</p>
 *
 * <ol>
 * <li><strong>Language Match</strong> - Filter to descriptions in the specified language
 * (or all languages if wildcard)</li>
 * <li><strong>Pattern Match</strong> - Filter to descriptions using acceptable patterns</li>
 * <li><strong>Type Priority</strong> - Select descriptions matching the highest priority type
 * from the type preference list</li>
 * <li><strong>Dialect Priority</strong> - Rank matching descriptions by dialect preference</li>
 * <li><strong>Module Priority</strong> - Rank remaining descriptions by module preference</li>
 * <li><strong>STAMP Filtering</strong> - Apply version filtering based on STAMP coordinate</li>
 * </ol>
 *
 * <p>The highest-ranked description after all stages is returned as the preferred description.</p>
 *
 * <h3>Core Interface</h3>
 *
 * <p>The {@link dev.ikm.tinkar.coordinate.language.LanguageCoordinate} interface defines the
 * contract for all language coordinate implementations. It provides:</p>
 *
 * <ul>
 * <li>Accessor methods for all five coordinate components</li>
 * <li>Content-based UUID generation for coordinate identity and caching</li>
 * <li>Conversion to {@link dev.ikm.tinkar.coordinate.language.LanguageCoordinateRecord}</li>
 * <li>User-friendly string representation</li>
 * </ul>
 *
 * <h3>Implementation Patterns</h3>
 *
 * <h4>LanguageCoordinateRecord</h4>
 * <p>Immutable record implementation providing thread-safe language coordinates. Created via
 * factory methods in the {@code Coordinates.Language} class:</p>
 * <pre>{@code
 * // US English, preferring regular names
 * LanguageCoordinateRecord usEnglish =
 *     Coordinates.Language.UsEnglishRegularName();
 *
 * // GB English, preferring fully qualified names
 * LanguageCoordinateRecord gbEnglish =
 *     Coordinates.Language.GbEnglishFullyQualifiedName();
 *
 * // Spanish, preferring regular names
 * LanguageCoordinateRecord spanish =
 *     Coordinates.Language.SpanishPreferredName();
 *
 * // Any language, preferring definitions (useful as fallback)
 * LanguageCoordinateRecord anyDefinition =
 *     Coordinates.Language.AnyLanguageDefinition();
 * }</pre>
 *
 * <h4>LanguageCoordinateDelegate</h4>
 * <p>Delegation pattern allowing classes to implement LanguageCoordinate by delegating to an
 * underlying instance:</p>
 * <pre>{@code
 * public class MyCalculator implements LanguageCoordinateDelegate {
 *     private final LanguageCoordinate languageCoordinate;
 *
 *     @Override
 *     public LanguageCoordinate getLanguageCoordinate() {
 *         return languageCoordinate;
 *     }
 * }
 * }</pre>
 *
 * <h3>Common Language Coordinate Patterns</h3>
 *
 * <h4>Language-Agnostic Coordinates</h4>
 * <p>Useful as fallback coordinates when language-specific descriptions are unavailable:</p>
 * <ul>
 * <li>{@code AnyLanguageRegularName()} - Any language, regular name</li>
 * <li>{@code AnyLanguageFullyQualifiedName()} - Any language, FQN</li>
 * <li>{@code AnyLanguageDefinition()} - Any language, definition</li>
 * </ul>
 *
 * <h4>English Variants</h4>
 * <p>Supporting US and GB dialect preferences:</p>
 * <ul>
 * <li>{@code UsEnglishRegularName()} - US first, then GB, regular names preferred</li>
 * <li>{@code UsEnglishFullyQualifiedName()} - US first, then GB, FQNs preferred</li>
 * <li>{@code GbEnglishPreferredName()} - GB first, then US, regular names preferred</li>
 * <li>{@code GbEnglishFullyQualifiedName()} - GB first, then US, FQNs preferred</li>
 * </ul>
 *
 * <h4>International Languages</h4>
 * <ul>
 * <li>{@code SpanishPreferredName()} - Spanish regular names</li>
 * <li>{@code SpanishFullyQualifiedName()} - Spanish FQNs</li>
 * </ul>
 *
 * <h3>Cascading Language Coordinates</h3>
 *
 * <p>View coordinates can specify multiple language coordinates in priority order, enabling
 * graceful fallback when descriptions aren't available in the preferred language:</p>
 *
 * <pre>{@code
 * ViewCoordinateRecord view = ViewCoordinateRecord.make(
 *     stampCoordinate,
 *     Lists.immutable.of(
 *         Coordinates.Language.SpanishPreferredName(),      // Try Spanish first
 *         Coordinates.Language.UsEnglishRegularName(),      // Fall back to English
 *         Coordinates.Language.AnyLanguageRegularName()     // Accept any language as last resort
 *     ),
 *     logicCoordinate,
 *     navigationCoordinate,
 *     editCoordinate
 * );
 * }</pre>
 *
 * <h3>Integration with Calculators</h3>
 *
 * <p>Language coordinates are used by {@link dev.ikm.tinkar.coordinate.language.calculator.LanguageCalculator}
 * implementations to compute preferred descriptions. The calculator package provides the actual
 * description selection and ranking logic based on these coordinate specifications.</p>
 *
 * <h3>Custom Language Coordinates</h3>
 *
 * <p>Create custom language coordinates for specialized needs:</p>
 * <pre>{@code
 * // Custom coordinate preferring technical definitions in US English
 * LanguageCoordinateRecord technical = LanguageCoordinateRecord.make(
 *     TinkarTerm.ENGLISH_LANGUAGE.nid(),
 *     IntIds.list.of(TinkarTerm.DESCRIPTION_PATTERN.nid()),
 *     IntIds.list.of(
 *         TinkarTerm.DEFINITION_DESCRIPTION_TYPE.nid(),      // Prefer definitions
 *         TinkarTerm.FULLY_QUALIFIED_NAME_DESCRIPTION_TYPE.nid(),
 *         TinkarTerm.REGULAR_NAME_DESCRIPTION_TYPE.nid()
 *     ),
 *     IntIds.list.of(TinkarTerm.US_DIALECT_PATTERN.nid()),
 *     IntIds.list.of(TinkarTerm.TECHNICAL_MODULE.nid())      // Custom module priority
 * );
 * }</pre>
 *
 * <h3>Thread Safety and Immutability</h3>
 *
 * <p>LanguageCoordinateRecord instances are immutable and thread-safe, suitable for:</p>
 * <ul>
 * <li>Concurrent access across multiple threads</li>
 * <li>Use as map keys or in sets</li>
 * <li>Caching in static fields or singletons</li>
 * <li>Functional composition and transformation</li>
 * </ul>
 *
 * <h3>Performance Considerations</h3>
 *
 * <p>Language coordinates generate content-based UUIDs for efficient caching. Identical
 * language coordinates (same language, dialects, types, modules) produce identical UUIDs,
 * enabling calculator implementations to cache description results.</p>
 *
 * @see dev.ikm.tinkar.coordinate.language.LanguageCoordinate
 * @see dev.ikm.tinkar.coordinate.language.LanguageCoordinateRecord
 * @see dev.ikm.tinkar.coordinate.language.LanguageCoordinateDelegate
 * @see dev.ikm.tinkar.coordinate.language.calculator
 * @see dev.ikm.tinkar.coordinate.Coordinates.Language
 */
package dev.ikm.tinkar.coordinate.language;
