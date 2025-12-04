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
 * <h2>STAMP Change Tracking System</h2>
 *
 * <p>Provides records for tracking and representing changes between versions of entities in the
 * Tinkar knowledge graph. These records enable change detection, diff generation, audit trails,
 * and synchronization workflows by capturing what changed, when it changed, and how it changed.</p>
 *
 * <h3>Core Responsibilities</h3>
 *
 * <p>The change tracking system captures:</p>
 * <ul>
 * <li><strong>Version Changes</strong> - New versions added to entity chronologies</li>
 * <li><strong>Field Changes</strong> - Modifications to specific fields within versions</li>
 * <li><strong>Change Chronology</strong> - Complete history of changes for an entity</li>
 * <li><strong>Change Attribution</strong> - STAMP metadata for each change</li>
 * <li><strong>Change Magnitude</strong> - Categorization of change significance</li>
 * </ul>
 *
 * <h3>Core Record Types</h3>
 *
 * <h4>ChangeChronology</h4>
 * <p>Represents the complete change history for an entity between two STAMP positions. Contains:</p>
 * <ul>
 * <li><strong>Entity Identity</strong> - Which entity changed</li>
 * <li><strong>Time Range</strong> - From position and to position</li>
 * <li><strong>Version Changes</strong> - List of all version-level changes</li>
 * <li><strong>Change Type</strong> - Addition, modification, retirement, etc.</li>
 * </ul>
 *
 * <pre>{@code
 * // Get changes for concept between two times
 * StampCalculator calculator = StampCalculatorWithCache.getCalculator(stampCoord);
 *
 * StampPositionRecord fromPos = StampPositionRecord.make(
 *     time1, TinkarTerm.DEVELOPMENT_PATH
 * );
 * StampPositionRecord toPos = StampPositionRecord.make(
 *     time2, TinkarTerm.DEVELOPMENT_PATH
 * );
 *
 * ChangeChronology changes = calculator.getChanges(conceptEntity, fromPos, toPos);
 *
 * // Examine change chronology
 * int changeCount = changes.versionChanges().size();
 * boolean hasChanges = !changes.versionChanges().isEmpty();
 * }</pre>
 *
 * <h4>VersionChangeRecord</h4>
 * <p>Represents changes to a single version of an entity. Contains:</p>
 * <ul>
 * <li><strong>Version Identity</strong> - Which version changed</li>
 * <li><strong>Change Type</strong> - Created, modified, inactivated, reactivated</li>
 * <li><strong>STAMP Metadata</strong> - Status, time, author, module, path for the change</li>
 * <li><strong>Field Changes</strong> - List of individual field modifications</li>
 * <li><strong>Previous Version</strong> - Reference to prior version (if modified)</li>
 * </ul>
 *
 * <pre>{@code
 * for (VersionChangeRecord versionChange : changes.versionChanges()) {
 *     // Examine version-level change
 *     int stampNid = versionChange.stampNid();
 *     StampEntity stamp = Entity.getStamp(stampNid);
 *
 *     System.out.println("Changed at: " + stamp.time());
 *     System.out.println("Changed by: " + stamp.author());
 *     System.out.println("New state: " + stamp.state());
 *
 *     // Process field changes
 *     for (FieldChangeRecord fieldChange : versionChange.fieldChanges()) {
 *         // ... examine field-level changes
 *     }
 * }
 * }</pre>
 *
 * <h4>FieldChangeRecord</h4>
 * <p>Represents a change to a specific field within a version. Contains:</p>
 * <ul>
 * <li><strong>Field Index</strong> - Which field changed (position in field array)</li>
 * <li><strong>Field Meaning</strong> - Semantic meaning of the field</li>
 * <li><strong>Old Value</strong> - Previous field value (null if new)</li>
 * <li><strong>New Value</strong> - Current field value (null if deleted)</li>
 * <li><strong>Change Type</strong> - Added, modified, removed</li>
 * </ul>
 *
 * <pre>{@code
 * for (FieldChangeRecord fieldChange : versionChange.fieldChanges()) {
 *     int fieldIndex = fieldChange.fieldIndex();
 *     Object oldValue = fieldChange.oldValue();
 *     Object newValue = fieldChange.newValue();
 *
 *     if (oldValue == null) {
 *         System.out.println("Field " + fieldIndex + " added: " + newValue);
 *     } else if (newValue == null) {
 *         System.out.println("Field " + fieldIndex + " removed: " + oldValue);
 *     } else {
 *         System.out.println("Field " + fieldIndex +
 *             " changed from " + oldValue + " to " + newValue);
 *     }
 * }
 * }</pre>
 *
 * <h3>Change Detection Patterns</h3>
 *
 * <h4>Simple Change Detection</h4>
 * <pre>{@code
 * // Has entity changed between two times?
 * StampCalculator calc = StampCalculatorWithCache.getCalculator(stampCoord);
 *
 * Latest<ConceptVersion> v1 = calc.latest(concept, time1);
 * Latest<ConceptVersion> v2 = calc.latest(concept, time2);
 *
 * boolean changed = !v1.equals(v2);
 *
 * if (changed && v1.isPresent() && v2.isPresent()) {
 *     ChangeChronology details = calc.getChanges(
 *         concept,
 *         StampPositionRecord.make(time1, path),
 *         StampPositionRecord.make(time2, path)
 *     );
 *     // Examine detailed changes
 * }
 * }</pre>
 *
 * <h4>Bulk Change Detection</h4>
 * <pre>{@code
 * // Find all concepts that changed in time range
 * List<ChangeChronology> changes = new ArrayList<>();
 *
 * for (int conceptNid : allConceptNids) {
 *     ConceptEntity concept = Entity.getConceptForNid(conceptNid);
 *     ChangeChronology changeChronology = calc.getChanges(
 *         concept, fromPos, toPos
 *     );
 *
 *     if (!changeChronology.versionChanges().isEmpty()) {
 *         changes.add(changeChronology);
 *     }
 * }
 *
 * System.out.println("Found " + changes.size() + " changed concepts");
 * }</pre>
 *
 * <h4>Specific Field Change Detection</h4>
 * <pre>{@code
 * // Find changes to specific field (e.g., descriptions)
 * for (VersionChangeRecord versionChange : changes.versionChanges()) {
 *     for (FieldChangeRecord fieldChange : versionChange.fieldChanges()) {
 *         // Check if this is a description field
 *         if (fieldChange.fieldMeaning() == TinkarTerm.DESCRIPTION_TEXT.nid()) {
 *             String oldText = (String) fieldChange.oldValue();
 *             String newText = (String) fieldChange.newValue();
 *             System.out.println("Description changed:");
 *             System.out.println("  Old: " + oldText);
 *             System.out.println("  New: " + newText);
 *         }
 *     }
 * }
 * }</pre>
 *
 * <h3>Change Types and Patterns</h3>
 *
 * <h4>Creation Changes</h4>
 * <p>New entity or version created:</p>
 * <ul>
 * <li>VersionChangeRecord with no previous version</li>
 * <li>All field changes are additions (oldValue = null)</li>
 * <li>STAMP indicates creation time, author, module, path</li>
 * </ul>
 *
 * <h4>Modification Changes</h4>
 * <p>Existing version modified:</p>
 * <ul>
 * <li>VersionChangeRecord references previous version</li>
 * <li>Field changes show old and new values</li>
 * <li>Some fields may be unchanged</li>
 * </ul>
 *
 * <h4>State Changes</h4>
 * <p>Entity activated or inactivated:</p>
 * <ul>
 * <li>STAMP status field changes between ACTIVE and INACTIVE</li>
 * <li>Other fields may or may not change</li>
 * <li>Common for retirement and deprecation</li>
 * </ul>
 *
 * <h4>Module Reorganization</h4>
 * <p>Entity moved between modules:</p>
 * <ul>
 * <li>STAMP module field changes</li>
 * <li>Content fields typically unchanged</li>
 * <li>Reflects modularization activity</li>
 * </ul>
 *
 * <h3>Use Cases</h3>
 *
 * <h4>Audit Trails</h4>
 * <pre>{@code
 * // Generate audit trail for entity
 * void auditEntity(Entity entity) {
 *     // Get all changes from beginning to now
 *     StampPositionRecord beginning = StampPositionRecord.make(0L, path);
 *     StampPositionRecord now = StampPositionRecord.make(
 *         System.currentTimeMillis(), path
 *     );
 *
 *     ChangeChronology history = calc.getChanges(entity, beginning, now);
 *
 *     System.out.println("Audit trail for: " + entity.nid());
 *     for (VersionChangeRecord change : history.versionChanges()) {
 *         StampEntity stamp = Entity.getStamp(change.stampNid());
 *         System.out.println(stamp.time() + " by " +
 *             stamp.author() + ": " + change.changeType());
 *     }
 * }
 * }</pre>
 *
 * <h4>Synchronization</h4>
 * <pre>{@code
 * // Synchronize changes from source to target
 * void syncChanges(StampPositionRecord lastSync, StampPositionRecord now) {
 *     for (int nid : allEntityNids) {
 *         Entity entity = Entity.getFast(nid);
 *         ChangeChronology changes = calc.getChanges(entity, lastSync, now);
 *
 *         if (!changes.versionChanges().isEmpty()) {
 *             // Apply changes to target system
 *             applyChangesToTarget(changes);
 *         }
 *     }
 * }
 * }</pre>
 *
 * <h4>Diff Generation</h4>
 * <pre>{@code
 * // Generate human-readable diff
 * void generateDiff(Entity entity, StampPositionRecord v1, StampPositionRecord v2) {
 *     ChangeChronology changes = calc.getChanges(entity, v1, v2);
 *
 *     for (VersionChangeRecord versionChange : changes.versionChanges()) {
 *         for (FieldChangeRecord fieldChange : versionChange.fieldChanges()) {
 *             String fieldName = getFieldName(fieldChange.fieldIndex());
 *             System.out.println("- " + fieldName + ": " + fieldChange.oldValue());
 *             System.out.println("+ " + fieldName + ": " + fieldChange.newValue());
 *         }
 *     }
 * }
 * }</pre>
 *
 * <h4>Change Notification</h4>
 * <pre>{@code
 * // Notify subscribers of changes
 * void notifyChanges(long lastCheckTime) {
 *     long now = System.currentTimeMillis();
 *     StampPositionRecord lastCheck = StampPositionRecord.make(lastCheckTime, path);
 *     StampPositionRecord nowPos = StampPositionRecord.make(now, path);
 *
 *     for (int subscribedNid : subscribedEntities) {
 *         Entity entity = Entity.getFast(subscribedNid);
 *         ChangeChronology changes = calc.getChanges(entity, lastCheck, nowPos);
 *
 *         if (!changes.versionChanges().isEmpty()) {
 *             notifySubscribers(subscribedNid, changes);
 *         }
 *     }
 * }
 * }</pre>
 *
 * <h3>Integration with Version Control</h3>
 *
 * <p>Change records integrate with STAMP-based version control:</p>
 * <ul>
 * <li>Changes are path-relative (branch-aware)</li>
 * <li>Time ranges respect path origins (branching points)</li>
 * <li>Changes can be merged across paths</li>
 * <li>Conflict detection via contradiction analysis</li>
 * </ul>
 *
 * <h3>Performance Considerations</h3>
 *
 * <ul>
 * <li>Change detection is O(V) where V is version count</li>
 * <li>Field-level comparison is O(F) where F is field count</li>
 * <li>Bulk change detection benefits from calculator caching</li>
 * <li>Consider batching change detection operations</li>
 * </ul>
 *
 * <h3>Thread Safety</h3>
 *
 * <p>All change record types are immutable and thread-safe. They can be safely shared across
 * threads and used in concurrent change detection workflows.</p>
 *
 * @see dev.ikm.tinkar.coordinate.stamp.change.ChangeChronology
 * @see dev.ikm.tinkar.coordinate.stamp.change.VersionChangeRecord
 * @see dev.ikm.tinkar.coordinate.stamp.change.FieldChangeRecord
 * @see dev.ikm.tinkar.coordinate.stamp.calculator.StampCalculator
 * @see dev.ikm.tinkar.coordinate.stamp.StampPosition
 */
package dev.ikm.tinkar.coordinate.stamp.change;
