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
 * <h2>STAMP Calculator System</h2>
 *
 * <p>Provides computational implementations that apply STAMP coordinates to resolve versions,
 * determine relative temporal positions, and track changes. STAMP calculators implement the core
 * version filtering algorithm, provide latest version selection, and support temporal queries
 * across the knowledge graph.</p>
 *
 * <h3>Core Responsibilities</h3>
 *
 * <p>STAMP calculators handle:</p>
 * <ul>
 * <li><strong>Version Resolution</strong> - Apply STAMP coordinates to select latest visible versions</li>
 * <li><strong>Temporal Ordering</strong> - Determine relative position of stamps on paths</li>
 * <li><strong>Path Analysis</strong> - Evaluate visibility of stamps across path origins</li>
 * <li><strong>Change Detection</strong> - Compare versions to identify changes</li>
 * <li><strong>Latest Caching</strong> - Store latest version results for performance</li>
 * <li><strong>Entity Snapshots</strong> - Capture entity state at specific stamp positions</li>
 * </ul>
 *
 * <h3>Core Interfaces and Classes</h3>
 *
 * <h4>StampCalculator</h4>
 * <p>Primary interface defining version resolution operations. Key methods include:</p>
 * <ul>
 * <li><strong>latest()</strong> - Get latest version of an entity matching stamp coordinate</li>
 * <li><strong>relativePosition()</strong> - Determine temporal ordering of two stamps</li>
 * <li><strong>isLatestActive()</strong> - Test if an entity has active latest version</li>
 * <li><strong>forEachLatestVersion()</strong> - Iterate over latest versions of entity collection</li>
 * <li><strong>snapshot()</strong> - Create entity snapshot at specific time/path</li>
 * <li><strong>stampCoordinate()</strong> - Access underlying STAMP coordinate</li>
 * </ul>
 *
 * <h4>StampCalculatorWithCache</h4>
 * <p>Cached implementation storing latest version results. Features:</p>
 * <ul>
 * <li>Thread-safe caching of Latest results</li>
 * <li>Content-based coordinate UUID for cache keys</li>
 * <li>Static factory method {@code getCalculator()} for instance retrieval</li>
 * <li>Automatic cache invalidation support via subscribers</li>
 * <li>Integration with path services for path origin resolution</li>
 * </ul>
 *
 * <pre>{@code
 * // Get a cached calculator instance
 * StampCoordinateRecord stampCoord = Coordinates.Stamp.DevelopmentLatestActiveOnly();
 * StampCalculator calculator = StampCalculatorWithCache.getCalculator(stampCoord);
 *
 * // Resolve latest version (cached on subsequent calls)
 * ConceptEntity concept = Entity.getConceptForNid(conceptNid);
 * Latest<ConceptVersion> latest = calculator.latest(concept);
 * }</pre>
 *
 * <h4>Latest<T></h4>
 * <p>Result container for version resolution. Represents three possible outcomes:</p>
 * <ul>
 * <li><strong>PRESENT</strong> - Single version found matching all criteria</li>
 * <li><strong>ABSENT</strong> - No versions found (filtered out or don't exist)</li>
 * <li><strong>CONTRADICTION</strong> - Multiple versions with same timestamp found</li>
 * </ul>
 *
 * <pre>{@code
 * Latest<ConceptVersion> latest = calculator.latest(concept);
 *
 * // Check outcome
 * if (latest.isPresent()) {
 *     ConceptVersion version = latest.get();
 *     // Process version
 * } else if (latest.isAbsent()) {
 *     // No visible version
 * } else if (latest.isContradiction()) {
 *     // Multiple contradictory versions
 *     List<ConceptVersion> contradictions = latest.contradictions();
 * }
 *
 * // Functional style
 * latest.ifPresent(version -> {
 *     // Process version
 * });
 * }</pre>
 *
 * <h4>RelativePosition</h4>
 * <p>Enumeration describing temporal relationship between two stamps:</p>
 * <ul>
 * <li><strong>BEFORE</strong> - First stamp occurs before second</li>
 * <li><strong>EQUAL</strong> - Stamps are identical</li>
 * <li><strong>AFTER</strong> - First stamp occurs after second</li>
 * <li><strong>UNREACHABLE</strong> - Stamps on unreachable paths</li>
 * <li><strong>CONTRADICTION</strong> - Same time but different values</li>
 * </ul>
 *
 * <h4>EntitySnapshot</h4>
 * <p>Captures complete entity state at a specific STAMP position, including all components
 * and semantics visible at that point in time.</p>
 *
 * <h3>Version Resolution Algorithm</h3>
 *
 * <p>The calculator implements the multi-stage latest version algorithm:</p>
 *
 * <pre>{@code
 * Latest<T> latest(Entity<T> entity) {
 *     List<T> candidates = new ArrayList<>();
 *
 *     // Stage 1: State filtering
 *     for (T version : entity.versions()) {
 *         if (stampCoord.allowedStates().contains(version.state())) {
 *             candidates.add(version);
 *         }
 *     }
 *
 *     // Stage 2: Module filtering
 *     candidates.removeIf(v ->
 *         !matchesModuleFilter(v.moduleNid()));
 *
 *     // Stage 3: Path + time filtering
 *     candidates.removeIf(v ->
 *         !isVisibleOnPath(v.stampNid()) ||
 *         v.time() > stampCoord.time());
 *
 *     // Stage 4: Select latest by time
 *     if (candidates.isEmpty()) {
 *         return Latest.empty();
 *     }
 *
 *     T latest = candidates.get(0);
 *     for (T candidate : candidates) {
 *         if (candidate.time() > latest.time()) {
 *             latest = candidate;
 *         }
 *     }
 *
 *     // Stage 5: Handle same-time versions via module priority
 *     List<T> sameTime = candidates.stream()
 *         .filter(v -> v.time() == latest.time())
 *         .toList();
 *
 *     if (sameTime.size() == 1) {
 *         return Latest.of(latest);
 *     } else {
 *         return resolveByModulePriority(sameTime);
 *     }
 * }
 * }</pre>
 *
 * <h3>Relative Position Calculation</h3>
 *
 * <p>Determining temporal ordering between stamps:</p>
 *
 * <pre>{@code
 * StampEntity stamp1 = Entity.getStamp(stamp1Nid);
 * StampEntity stamp2 = Entity.getStamp(stamp2Nid);
 *
 * RelativePosition position = calculator.relativePosition(stamp1Nid, stamp2Nid);
 *
 * switch (position) {
 *     case BEFORE -> {
 *         // stamp1 created before stamp2
 *     }
 *     case AFTER -> {
 *         // stamp1 created after stamp2
 *     }
 *     case EQUAL -> {
 *         // Same stamp
 *     }
 *     case UNREACHABLE -> {
 *         // On different, unreachable paths
 *     }
 *     case CONTRADICTION -> {
 *         // Same time but different metadata
 *     }
 * }
 * }</pre>
 *
 * <h3>Path Visibility Analysis</h3>
 *
 * <p>Determining if a stamp is visible on a coordinate's path:</p>
 *
 * <pre>{@code
 * boolean isVisibleOnPath(int stampNid) {
 *     StampEntity stamp = Entity.getStamp(stampNid);
 *     int stampPathNid = stamp.pathNid();
 *     int coordPathNid = stampCoord.pathNidForFilter();
 *
 *     // Direct path match
 *     if (stampPathNid == coordPathNid) {
 *         return true;
 *     }
 *
 *     // Check path origins (branching)
 *     StampPath coordPath = pathService.getPath(coordPathNid);
 *     for (StampPosition origin : coordPath.pathOrigins()) {
 *         if (origin.getPathForPositionNid() == stampPathNid &&
 *             stamp.time() <= origin.time()) {
 *             return true; // Stamp predates branch point
 *         }
 *     }
 *
 *     return false;
 * }
 * }</pre>
 *
 * <h3>Change Detection</h3>
 *
 * <p>Tracking changes between versions:</p>
 *
 * <pre>{@code
 * // Get change record between two stamp positions
 * StampPositionRecord pos1 = StampPositionRecord.make(time1, path);
 * StampPositionRecord pos2 = StampPositionRecord.make(time2, path);
 *
 * ChangeChronology changes = calculator.getChanges(
 *     conceptEntity,
 *     pos1,
 *     pos2
 * );
 *
 * // Process field changes
 * for (VersionChangeRecord versionChange : changes.versionChanges()) {
 *     for (FieldChangeRecord fieldChange : versionChange.fieldChanges()) {
 *         Object oldValue = fieldChange.oldValue();
 *         Object newValue = fieldChange.newValue();
 *         // Handle change
 *     }
 * }
 * }</pre>
 *
 * <h3>Entity Snapshots</h3>
 *
 * <p>Capturing complete entity state at a specific time:</p>
 *
 * <pre>{@code
 * // Create snapshot of concept at specific time
 * StampPositionRecord historicPosition = StampPositionRecord.make(
 *     historicTimestamp,
 *     TinkarTerm.MASTER_PATH
 * );
 *
 * EntitySnapshot snapshot = calculator.snapshot(conceptEntity, historicPosition);
 *
 * // Access historic state
 * Latest<ConceptVersion> historicVersion = snapshot.latestVersion();
 * List<Latest<SemanticEntityVersion>> semantics = snapshot.semantics();
 * }</pre>
 *
 * <h3>Bulk Operations</h3>
 *
 * <p>Efficiently process latest versions for multiple entities:</p>
 *
 * <pre>{@code
 * // Process all concepts in a set
 * IntIdSet conceptNids = ...;
 *
 * calculator.forEachLatestVersion(conceptNids, latest -> {
 *     if (latest.isPresent()) {
 *         ConceptVersion version = latest.get();
 *         // Process each latest version
 *     }
 * });
 *
 * // Stream pattern matching
 * calculator.streamLatestVersionForPattern(TinkarTerm.DESCRIPTION_PATTERN)
 *     .filter(Latest::isPresent)
 *     .map(Latest::get)
 *     .forEach(desc -> {
 *         // Process each description
 *     });
 * }</pre>
 *
 * <h3>Active Status Testing</h3>
 *
 * <pre>{@code
 * // Test if concept has active latest version
 * boolean isActive = calculator.isLatestActive(conceptNid);
 *
 * // Equivalent to:
 * Latest<ConceptVersion> latest = calculator.latest(concept);
 * boolean isActive = latest.isPresent() &&
 *                   latest.get().state() == State.ACTIVE;
 * }</pre>
 *
 * <h3>Performance and Caching</h3>
 *
 * <p>StampCalculatorWithCache provides significant performance benefits:</p>
 * <ul>
 * <li><strong>Latest Result Caching</strong> - Latest version results cached per entity</li>
 * <li><strong>Coordinate-Based Keys</strong> - Cache keys from stamp coordinate UUID</li>
 * <li><strong>Lazy Computation</strong> - Latest calculated only when requested</li>
 * <li><strong>Thread-Safe Access</strong> - Concurrent map-based caching</li>
 * <li><strong>Path Service Integration</strong> - Path origin lookups cached</li>
 * </ul>
 *
 * <p>Performance characteristics:</p>
 * <ul>
 * <li>First latest() call: O(V) where V is version count</li>
 * <li>Cached latest() calls: O(1) map lookup</li>
 * <li>Relative position: O(1) with path caching</li>
 * <li>Memory overhead: One Latest per entity per coordinate</li>
 * </ul>
 *
 * <h3>Cache Invalidation</h3>
 *
 * <p>The {@link dev.ikm.tinkar.coordinate.stamp.calculator.CacheInvalidationIfPatternSubscriber}
 * supports automatic cache invalidation when underlying data changes.</p>
 *
 * <h3>Integration with Other Calculators</h3>
 *
 * <p>STAMP calculators are used by all other calculator types:</p>
 * <ul>
 * <li>LanguageCalculator uses StampCalculator for description version filtering</li>
 * <li>LogicCalculator uses StampCalculator for axiom version filtering</li>
 * <li>NavigationCalculator uses StampCalculator for navigation semantic filtering</li>
 * <li>ViewCalculator delegates to StampCalculator for all version operations</li>
 * </ul>
 *
 * <h3>Thread Safety</h3>
 *
 * <p>All calculator implementations are thread-safe. The cached implementation uses concurrent
 * data structures for safe concurrent access to version caches.</p>
 *
 * @see dev.ikm.tinkar.coordinate.stamp.calculator.StampCalculator
 * @see dev.ikm.tinkar.coordinate.stamp.calculator.StampCalculatorWithCache
 * @see dev.ikm.tinkar.coordinate.stamp.calculator.Latest
 * @see dev.ikm.tinkar.coordinate.stamp.calculator.RelativePosition
 * @see dev.ikm.tinkar.coordinate.stamp.calculator.EntitySnapshot
 * @see dev.ikm.tinkar.coordinate.stamp.StampCoordinate
 */
package dev.ikm.tinkar.coordinate.stamp.calculator;
