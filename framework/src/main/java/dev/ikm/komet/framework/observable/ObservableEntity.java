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
package dev.ikm.komet.framework.observable;

import static dev.ikm.tinkar.events.EntityVersionChangeEvent.VERSION_UPDATED;
import static dev.ikm.tinkar.events.FrameworkTopics.VERSION_CHANGED_TOPIC;
import com.github.benmanes.caffeine.cache.Cache;
import com.github.benmanes.caffeine.cache.Caffeine;
import dev.ikm.komet.framework.observable.binding.Binding;
import dev.ikm.tinkar.common.id.IntIdSet;
import dev.ikm.tinkar.common.service.PrimitiveData;
import dev.ikm.tinkar.events.EntityVersionChangeEvent;
import dev.ikm.tinkar.events.EvtBusFactory;
import dev.ikm.tinkar.common.util.broadcast.Subscriber;
import dev.ikm.tinkar.component.FieldDataType;
import dev.ikm.tinkar.coordinate.view.calculator.ViewCalculator;
import dev.ikm.tinkar.entity.ConceptEntity;
import dev.ikm.tinkar.entity.ConceptRecord;
import dev.ikm.tinkar.entity.Entity;
import dev.ikm.tinkar.entity.EntityVersion;
import dev.ikm.tinkar.entity.PatternEntity;
import dev.ikm.tinkar.entity.PatternRecord;
import dev.ikm.tinkar.entity.SemanticEntity;
import dev.ikm.tinkar.entity.SemanticRecord;
import dev.ikm.tinkar.entity.StampEntity;
import dev.ikm.tinkar.entity.StampRecord;
import javafx.application.Platform;
import javafx.beans.property.ReadOnlyObjectProperty;
import javafx.beans.property.ReadOnlyObjectWrapper;
import javafx.beans.property.ReadOnlyProperty;
import org.eclipse.collections.api.factory.Lists;
import org.eclipse.collections.api.list.ImmutableList;
import org.eclipse.collections.api.list.MutableList;
import org.eclipse.collections.api.map.ImmutableMap;
import org.eclipse.collections.api.map.primitive.MutableIntObjectMap;
import org.eclipse.collections.impl.map.mutable.primitive.IntObjectHashMap;

import java.util.Collection;
import java.util.Comparator;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicReference;

/**
 * JavaFX-compatible observable wrapper for immutable Tinkar entities, providing reactive property bindings
 * for UI components and change notification support.
 * <p>
 * {@code ObservableEntity} bridges the gap between immutable Tinkar entities and JavaFX's reactive
 * programming model by wrapping {@link Entity} instances with JavaFX properties. This enables direct
 * binding to UI controls, automatic UI updates when entity data changes, and listener registration for
 * entity version changes.
 *
 * <h2>What is an ObservableEntity?</h2>
 * <p>
 * An {@code ObservableEntity} is a <b>mutable, observable wrapper</b> around an immutable {@link Entity}
 * that provides:
 * <ul>
 *   <li><b>JavaFX Properties:</b> Expose entity data as {@link javafx.beans.property.Property} objects
 *       for direct UI binding (e.g., {@code label.textProperty().bind(concept.descriptionProperty())})</li>
 *   <li><b>Change Notifications:</b> Automatically notify observers when the underlying entity versions
 *       are updated or new versions are committed</li>
 *   <li><b>Canonical Instances:</b> Maintain a single in-memory instance per NID (while strongly referenced),
 *       ensuring all observers see consistent state</li>
 *   <li><b>Thread Safety:</b> Require JavaFX application thread for access, enforcing proper UI threading</li>
 * </ul>
 *
 * <h2>Four Observable Entity Types</h2>
 * <p>
 * This sealed interface has four permitted implementations mirroring the four entity types:
 * <ul>
 *   <li>{@link ObservableConcept} - Observable wrapper for {@link ConceptEntity}</li>
 *   <li>{@link ObservableSemantic} - Observable wrapper for {@link SemanticEntity}</li>
 *   <li>{@link ObservablePattern} - Observable wrapper for {@link PatternEntity}</li>
 *   <li>{@link ObservableStamp} - Observable wrapper for {@link StampEntity}</li>
 * </ul>
 *
 * <h2>⚠️ How to Access: Use ObservableEntityHandle</h2>
 * <p>
 * <b>DO NOT</b> call the static {@code get()} methods on this class directly. They are deprecated and
 * will be made module-internal in a future release. Instead, use {@link ObservableEntityHandle}, which
 * provides a fluent, type-safe API for accessing observable entities.
 *
 * <h3>Why Use ObservableEntityHandle?</h3>
 * <ul>
 *   <li><b>Type Safety:</b> Compile-time checks ensure you're working with the correct entity type
 *       (Concept, Semantic, Pattern, or Stamp)</li>
 *   <li><b>Null Safety:</b> Explicit handling of absent entities via {@link java.util.Optional} or
 *       fluent conditional methods</li>
 *   <li><b>Composability:</b> Chain operations fluently without manual type checks or casts</li>
 *   <li><b>Three Access Patterns:</b> Side effects ({@code ifXxx}), safe extraction ({@code asXxx}),
 *       or direct assertion ({@code expectXxx}) - choose the right pattern for your use case</li>
 * </ul>
 *
 * <h3>Correct Usage Examples</h3>
 * <pre>{@code
 * // ✅ CORRECT: Use ObservableEntityHandle for type-safe access
 * ObservableConcept concept = ObservableEntityHandle.getConceptOrThrow(conceptNid);
 * titleLabel.textProperty().bind(concept.descriptionProperty());
 *
 * // ✅ CORRECT: Fluent API with type checking
 * ObservableEntityHandle.get(nid)
 *     .ifConcept(concept -> {
 *         titleLabel.textProperty().bind(concept.descriptionProperty());
 *         statusIcon.visibleProperty().bind(concept.activeProperty());
 *     })
 *     .ifSemantic(semantic -> bindSemanticFields(semantic))
 *     .ifAbsent(() -> showNotFound());
 *
 * // ✅ CORRECT: Safe Optional-based extraction
 * ObservableEntityHandle.get(userInputNid)
 *     .asConcept()
 *     .ifPresent(concept -> displayLabel.setText(concept.description()));
 *
 * // ❌ WRONG: Direct static method (deprecated, will be removed)
 * ObservableConcept concept = ObservableEntity.get(conceptNid); // DON'T DO THIS
 * }</pre>
 *
 * <h2>When to Use ObservableEntity vs Entity</h2>
 * <table border="1" cellpadding="5">
 * <caption>ObservableEntity vs Entity Comparison</caption>
 * <tr>
 *   <th>Use Case</th>
 *   <th>Use Entity</th>
 *   <th>Use ObservableEntity</th>
 * </tr>
 * <tr>
 *   <td><b>UI Binding</b></td>
 *   <td>❌ Not reactive</td>
 *   <td>✅ Direct property binding</td>
 * </tr>
 * <tr>
 *   <td><b>Change Notifications</b></td>
 *   <td>❌ Manual polling</td>
 *   <td>✅ Automatic listeners</td>
 * </tr>
 * <tr>
 *   <td><b>Threading</b></td>
 *   <td>✅ Any thread</td>
 *   <td>⚠️ JavaFX thread only</td>
 * </tr>
 * <tr>
 *   <td><b>Immutability</b></td>
 *   <td>✅ Fully immutable</td>
 *   <td>⚠️ Mutable wrapper</td>
 * </tr>
 * <tr>
 *   <td><b>Calculations/Logic</b></td>
 *   <td>✅ Preferred</td>
 *   <td>❌ Unnecessary overhead</td>
 * </tr>
 * <tr>
 *   <td><b>Background Processing</b></td>
 *   <td>✅ Thread-safe</td>
 *   <td>❌ Requires Platform.runLater()</td>
 * </tr>
 * </table>
 *
 * <h2>Canonical Instance Pool</h2>
 * <p>
 * The {@link #CANONICAL_INSTANCES} cache ensures that for any given NID, only one
 * {@code ObservableEntity} instance exists in memory at a time (while strongly referenced). This is
 * critical for JavaFX property binding - all UI components must observe the <b>exact same object</b>
 * to receive change notifications. The cache uses weak references, allowing automatic cleanup when
 * no UI components or code hold references to the entity.
 *
 * <h2>Thread Safety Requirements</h2>
 * <p>
 * <b>⚠️ IMPORTANT:</b> All {@code ObservableEntity} access must occur on the JavaFX application thread.
 * Attempting to access from other threads will throw {@link RuntimeException}. If you need entity data
 * in background threads, use immutable {@link Entity} instead, then wrap in {@code ObservableEntity}
 * on the JavaFX thread when updating UI.
 *
 * <pre>{@code
 * // Background thread
 * CompletableFuture.supplyAsync(() -> {
 *     // Use immutable Entity for calculations
 *     Entity<?> entity = Entity.getFast(nid);
 *     return computeResult(entity);
 * }).thenAccept(result -> {
 *     // Switch to JavaFX thread for UI updates
 *     Platform.runLater(() -> {
 *         ObservableConcept concept = ObservableEntityHandle.getConceptOrThrow(nid);
 *         resultLabel.textProperty().bind(concept.descriptionProperty());
 *     });
 * });
 * }</pre>
 *
 * <h2>Working with Snapshots for View-Specific Access</h2>
 * <p>
 * While {@code ObservableEntity} gives you access to <i>all</i> versions of an entity, you typically need
 * to work within a specific <b>view context</b> - seeing only the versions visible on certain development
 * paths, in certain modules, or at a specific point in time. This is where {@link ObservableEntitySnapshot}
 * becomes essential.
 *
 * <h3>What Are Snapshots?</h3>
 * <p>
 * An {@link ObservableEntitySnapshot} is a view-specific projection that:
 * <ul>
 *   <li><b>Filters versions</b> according to {@link ViewCalculator} coordinates (path, module, time, language)</li>
 *   <li><b>Categorizes versions</b> into latest (current), historic (superseded), contradicted (conflicts), and uncommitted (unsaved)</li>
 *   <li><b>Enables "time travel"</b> - see what the entity looked like at previous points in time</li>
 *   <li><b>Provides processing tools</b> - filter, sort, and analyze versions within the view context</li>
 * </ul>
 *
 * <h3>Why Use Snapshots?</h3>
 * <p>
 * <b>Problem:</b> Directly using {@code ObservableEntity} gives you all versions, but you need to determine
 * which ones are "current" for a specific user's view, which are historic, and which represent conflicts.
 * <p>
 * <b>Solution:</b> {@link ObservableEntitySnapshot} automatically applies view coordinates and categorizes
 * versions, giving you instant access to:
 * <ul>
 *   <li>The latest version(s) visible in this view</li>
 *   <li>Any contradictions (multiple "current" versions on different paths)</li>
 *   <li>Historic versions (what it looked like before recent changes)</li>
 *   <li>Uncommitted local changes not yet saved</li>
 * </ul>
 *
 * <h3>Creating Snapshots via ObservableEntityHandle</h3>
 * <p>
 * Always create snapshots using {@link ObservableEntityHandle}, never by calling deprecated methods or
 * constructing directly:
 *
 * <pre>{@code
 * // ✅ CORRECT: Type-safe snapshot creation
 * ViewCalculator viewCalc = // ... from ViewCoordinateRecord
 * ObservableConceptSnapshot snapshot = 
 *     ObservableEntityHandle.getConceptSnapshotOrThrow(conceptNid, viewCalc);
 *
 * // ✅ CORRECT: Fluent API with snapshot
 * ObservableEntityHandle.get(nid)
 *     .ifConceptGetSnapshot(viewCalc, snapshot -> {
 *         Latest<ObservableConceptVersion> latest = snapshot.getLatestVersion();
 *         if (latest.contradictions().isEmpty()) {
 *             // Single current version
 *             displayVersion(latest.get());
 *         } else {
 *             // Multiple current versions - show conflict
 *             showConflictDialog(latest);
 *         }
 *     });
 *
 * // ✅ CORRECT: Time travel through history
 * ObservableConceptSnapshot snapshot = 
 *     ObservableEntityHandle.getConceptSnapshotOrThrow(nid, viewCalc);
 * for (ObservableConceptVersion historic : snapshot.getHistoricVersions()) {
 *     displayHistoricVersion(historic);
 * }
 * }</pre>
 *
 * <h3>Observable Entity vs Snapshot Decision Guide</h3>
 * <table border="1" cellpadding="5">
 * <caption>When to Use ObservableEntity vs ObservableEntitySnapshot</caption>
 * <tr>
 *   <th>Scenario</th>
 *   <th>Use ObservableEntity</th>
 *   <th>Use ObservableEntitySnapshot</th>
 * </tr>
 * <tr>
 *   <td><b>Direct property binding</b></td>
 *   <td>✅ Bind to properties directly</td>
 *   <td>❌ Not needed</td>
 * </tr>
 * <tr>
 *   <td><b>View-specific version filtering</b></td>
 *   <td>❌ Shows all versions</td>
 *   <td>✅ Filters by view coordinates</td>
 * </tr>
 * <tr>
 *   <td><b>Determining "current" version</b></td>
 *   <td>❌ Manual calculation needed</td>
 *   <td>✅ Automatic categorization</td>
 * </tr>
 * <tr>
 *   <td><b>Detecting contradictions</b></td>
 *   <td>❌ Manual analysis required</td>
 *   <td>✅ Built-in detection</td>
 * </tr>
 * <tr>
 *   <td><b>Version history/"time travel"</b></td>
 *   <td>⚠️ All history, no filtering</td>
 *   <td>✅ View-filtered history</td>
 * </tr>
 * <tr>
 *   <td><b>Change notifications</b></td>
 *   <td>✅ Automatic via properties</td>
 *   <td>⚠️ Snapshot is point-in-time</td>
 * </tr>
 * </table>
 * <p>
 * <b>Rule of Thumb:</b> Use {@code ObservableEntity} for direct UI binding and change notifications.
 * Use {@link ObservableEntitySnapshot} when you need to determine what's "current" vs "historic" vs
 * "contradicting" within a specific view context, or when implementing version history/audit features.
 *
 * @param <OV> the observable version type ({@link ObservableConceptVersion}, {@link ObservableSemanticVersion}, etc.)
 * @see ObservableEntityHandle
 * @see ObservableEntitySnapshot
 * @see ObservableConcept
 * @see ObservableSemantic
 * @see ObservablePattern
 * @see ObservableStamp
 * @see Entity
 */
public abstract sealed class ObservableEntity<OV extends ObservableVersion<?>>
        implements Entity<OV>, ObservableComponent
        permits ObservableConcept, ObservablePattern, ObservableSemantic, ObservableStamp {

    /**
     * Canonical object pool ensuring a single in-memory instance per entity using weak references.
     * <p>
     * When an entity is in memory, all observers operate on the same object instance, ensuring
     * all changes are visible to all observers. Weak references allow automatic cleanup when
     * no observers hold the entity anymore.
     *
     * <h3>Migration from SOFT to WEAK References</h3>
     * <p>
     * <b>Previous Implementation:</b> Used SOFT references via {@code ConcurrentReferenceHashMap}.
     * <p>
     * <b>Current Implementation:</b> Uses WEAK references via Caffeine cache with {@code weakValues()}.
     *
     * <h3>Why WEAK is Superior for This Use Case</h3>
     * <ul>
     *   <li><b>Immediate Cleanup:</b> Entities are GC'd as soon as all observers release them
     *       (no strong references remain). This prevents memory buildup from unused entities.</li>
     *   <li><b>"Flyweight While In Use" Semantics:</b> Shared instance during active use
     *       (UI bound, listeners attached), automatic cleanup when done.</li>
     *   <li><b>Predictable Memory Behavior:</b> No accumulation of unused entities waiting
     *       for memory pressure. Memory is reclaimed promptly when observers are done.</li>
     * </ul>
     *
     * <h3>Why NOT SOFT References</h3>
     * <ul>
     *   <li><b>SOFT references persist too long:</b> They're only cleared when the JVM is
     *       under memory pressure, which may not occur even when entities are no longer needed.</li>
     *   <li><b>Memory Accumulation:</b> Unused entities can accumulate indefinitely if memory
     *       pressure doesn't trigger GC, defeating the purpose of automatic cleanup.</li>
     *   <li><b>Unpredictable Cleanup:</b> Cleanup timing depends on JVM memory state rather
     *       than actual usage, making behavior less deterministic.</li>
     * </ul>
     *
     * <h3>Behavior Guarantee</h3>
     * <p>
     * While any code holds a strong reference to an {@code ObservableEntity} (e.g., stored in
     * a variable, bound to UI, registered as listener), the entity remains in the cache and all
     * code accessing the same nid receives the same instance. When all strong references are
     * released, GC can collect the entity and the cache entry is automatically cleared.
     *
     * <h3>Implementation Notes</h3>
     * <p>
     * Migrated from legacy {@code ConcurrentReferenceHashMap} (2600 lines, pre-Java 8
     * segment-based locking) to modern Caffeine cache (actively maintained, optimized for
     * Java 8+ with better concurrency characteristics and cleaner API).
     *
     * @see com.github.benmanes.caffeine.cache.Cache
     * @see java.lang.ref.WeakReference
     */
    protected static final Cache<Integer, ObservableEntity> CANONICAL_INSTANCES =
            Caffeine.newBuilder()
                    .weakValues()
                    .build();
    private static final EntityChangeSubscriber ENTITY_CHANGE_SUBSCRIBER = new EntityChangeSubscriber();

    static {
        Entity.provider().addSubscriberWithWeakReference(ENTITY_CHANGE_SUBSCRIBER);
    }

    private final FeatureList<OV> versionSetAsList;

    private MutableIntObjectMap<OV> versionPropertyMap = new IntObjectHashMap<>();

    final private AtomicReference<Entity<?>> entityReference;

    /**
     * Saves the uncommited entity version to the DB and fires event (VERSION_UPDATED).
     * it also adds the version to the versionProperty list.
     * @param analogue the entity record
     * @param newVersionRecord entity version record
     */
    public void saveToDB(Entity<?> analogue, EntityVersion newVersionRecord , EntityVersion oldVersionRecord) {
        Entity.provider().putEntity(analogue);
        versionPropertyMap.put(newVersionRecord.stamp().nid(), wrap(newVersionRecord));
        EvtBusFactory.getDefaultEvtBus()
                .publish(VERSION_CHANGED_TOPIC, new EntityVersionChangeEvent(this, VERSION_UPDATED, newVersionRecord));
    }

    ObservableEntity(Entity<?> entity) {
        Entity<?> entityClone = switch (entity) {
            case ConceptRecord conceptEntity -> conceptEntity.analogueBuilder().build();

            case PatternRecord patternEntity -> patternEntity.analogueBuilder().build();

            case SemanticRecord semanticEntity -> semanticEntity.analogueBuilder().build();

            case StampRecord stampEntity -> stampEntity.analogueBuilder().build();

            default -> throw new UnsupportedOperationException("Can't handle: " + entity);
        };
        this.versionSetAsList = FeatureList.makeEmptyList(FeatureKey.Entity.VersionSet(entity.nid()),
                Binding.Component.pattern(), Binding.Component.versionsFieldDefinitionIndex(), this);

        this.entityReference = new AtomicReference<>(entityClone);
        for (EntityVersion version : entity.versions()) {
            OV wrappedVersion = wrap(version);
            versionPropertyMap.put(version.stamp().nid(), wrappedVersion);
            versionSetAsList.add(wrappedVersion);
        }
    }

    protected abstract OV wrap(EntityVersion version);

    /**
     * @deprecated Use {@link ObservableEntityHandle#getSnapshot(int, ViewCalculator)} instead.
     * <p>
     * This static accessor method is being phased out in favor of the fluent
     * {@link ObservableEntityHandle} API, which provides better type safety, null handling,
     * and composability. This method will be made module-internal in a future release.
     * <p>
     * <b>Migration:</b>
     * <pre>{@code
     * // Old (deprecated):
     * ObservableEntitySnapshot snapshot = ObservableEntity.getSnapshot(nid, calculator);
     *
     * // New (recommended):
     * Optional<ObservableEntitySnapshot<?, ?>> snapshot =
     *     ObservableEntityHandle.getSnapshot(nid, calculator);
     * }</pre>
     *
     * @see ObservableEntityHandle#getSnapshot(int, ViewCalculator)
     * @see ObservableEntityHandle#getConceptSnapshotOrThrow(int, ViewCalculator)
     * @see ObservableEntityHandle#getSemanticSnapshotOrThrow(int, ViewCalculator)
     * @see ObservableEntityHandle#getPatternSnapshotOrThrow(int, ViewCalculator)
     */
    @Deprecated(since = "Current", forRemoval = true)
    public static <OE extends ObservableEntity<OV>, OV extends ObservableVersion<EV>, EV extends EntityVersion>
    ObservableEntitySnapshot<OE, OV> getSnapshot(int nid, ViewCalculator calculator) {
        return packagePrivateGetSnapshot(nid, calculator);
    }

    /**
     * Package-private method for internal use by ObservableEntityHandle.
     * External code should use {@link ObservableEntityHandle#getSnapshot(int, ViewCalculator)}.
     */
    static <OE extends ObservableEntity<OV>, OV extends ObservableVersion<EV>, EV extends EntityVersion>
    ObservableEntitySnapshot<OE, OV> packagePrivateGetSnapshot(int nid, ViewCalculator calculator) {
        return packagePrivateGet(Entity.packagePrivateGetFast(nid)).getSnapshot(calculator);
    }

    public abstract ObservableEntitySnapshot<?,?> getSnapshot(ViewCalculator calculator);

    /**
     * Package-private method for internal use by ObservableEntityHandle.
     * External code should use {@link ObservableEntityHandle#get(int)}.
     */
    static <OE extends ObservableEntity> OE packagePrivateGet(Entity<? extends EntityVersion> entity) {
        if (!Platform.isFxApplicationThread()) {
            //Throw exception since we need to get the version using JavaFx thread.
            throw new RuntimeException( "Invalid calling thread.");
        }
        ObservableEntity observableEntity = switch (entity) {
            case ObservableEntity oe -> oe;
            case ConceptEntity conceptEntity -> new ObservableConcept(conceptEntity);
            case PatternEntity patternEntity -> new ObservablePattern(patternEntity);
            case SemanticEntity semanticEntity -> new ObservableSemantic(semanticEntity);
            case StampEntity stampEntity -> new ObservableStamp(stampEntity);
            default -> throw new UnsupportedOperationException("Can't handle: " + entity);
        };
        observableEntity.updateVersions(entity);
        return (OE) observableEntity;
    }

    /**
     * @deprecated Use {@link ObservableEntityHandle#get(int)} or type-specific methods instead.
     * <p>
     * This static accessor method is being phased out in favor of the fluent
     * {@link ObservableEntityHandle} API, which provides better type safety, null handling,
     * and composability. This method will be made module-internal in a future release.
     * <p>
     * <b>Migration:</b>
     * <pre>{@code
     * // Old (deprecated):
     * ObservableConcept concept = ObservableEntity.get(nid);
     *
     * // New (recommended - type-safe):
     * ObservableConcept concept = ObservableEntityHandle.getConceptOrThrow(nid);
     *
     * // Or with safe Optional handling:
     * ObservableEntityHandle.get(nid)
     *     .asConcept()
     *     .ifPresent(concept -> process(concept));
     * }</pre>
     *
     * @see ObservableEntityHandle#get(int)
     * @see ObservableEntityHandle#getConceptOrThrow(int)
     * @see ObservableEntityHandle#getSemanticOrThrow(int)
     * @see ObservableEntityHandle#getPatternOrThrow(int)
     */
    @Deprecated(since = "Current", forRemoval = true)
    public static <OE extends ObservableEntity> OE get(Entity<? extends EntityVersion> entity) {
        return packagePrivateGet(entity);
    }

    /**
     * Updates the versions in the versionProperty list.
     * @param newEntity
     */
    private void updateVersions(Entity<? extends EntityVersion> newEntity) {
        // Entities are immutable, so if the entity identities are the same, then the versions are also the same.
        // Versions can never be removed, we are append only. Do not have to check for deletions, just additions or
        // updates.
        if (entityReference.get() != newEntity) {
            final AtomicBoolean changed = new AtomicBoolean(false);
            // Find if there is a changed version...
            for (EntityVersion newVersion: newEntity.versions()) {
                OV oldVersion = versionPropertyMap.get(newVersion.stampNid());
                if (oldVersion == null) {
                    changed.set(true);
                    OV newWrappedVersion = wrap(newVersion);
                    versionPropertyMap.put(newVersion.stampNid(), newWrappedVersion);
                    versionSetAsList.add(oldVersion);
                } else if (oldVersion.version().time() == Long.MAX_VALUE) {
                    changed.set(true);
                    int index = versionSetAsList.indexOf(oldVersion);
                    OV newWrappedVersion = wrap(newVersion);
                    versionPropertyMap.put(newVersion.stampNid(), newWrappedVersion);
                    versionSetAsList.set(index, newWrappedVersion);
                } else if (newVersion.time() != oldVersion.version().time()) {
                    throw new IllegalStateException("Version time mismatch: " + newVersion.time() + " != " + oldVersion.version().time());
                }
                if (changed.get()) {
                    entityReference.set(newEntity);
                }
            }
        }
    }

    /**
     * Package-private method for internal use by ObservableEntityHandle.
     * External code should use {@link ObservableEntityHandle#get(int)}.
     */
    static <OE extends ObservableEntity> OE packagePrivateGet(int nid) {
        return packagePrivateGet(Entity.packagePrivateGetFast(nid));
    }

    /**
     * @deprecated Use {@link ObservableEntityHandle#get(int)} or type-specific methods instead.
     * <p>
     * This static accessor method is being phased out in favor of the fluent
     * {@link ObservableEntityHandle} API, which provides better type safety, null handling,
     * and composability. This method will be made module-internal in a future release.
     * <p>
     * <b>Migration:</b>
     * <pre>{@code
     * // Old (deprecated):
     * ObservableConcept concept = ObservableEntity.get(nid);
     *
     * // New (recommended - type-safe):
     * ObservableConcept concept = ObservableEntityHandle.getConceptOrThrow(nid);
     *
     * // Or using fluent API:
     * ObservableEntityHandle.get(nid)
     *     .ifConcept(concept -> process(concept))
     *     .ifAbsent(() -> handleMissing());
     * }</pre>
     *
     * @see ObservableEntityHandle#get(int)
     * @see ObservableEntityHandle#getConceptOrThrow(int)
     * @see ObservableEntityHandle#getSemanticOrThrow(int)
     * @see ObservableEntityHandle#getPatternOrThrow(int)
     */
    @Deprecated(since = "Current", forRemoval = true)
    public static <OE extends ObservableEntity> OE get(int nid) {
        return packagePrivateGet(nid);
    }

    protected Entity<?> entity() {
        return entityReference.get();
    }

    public MutableIntObjectMap<OV> versionPropertyMap() {
        return versionPropertyMap;
    }

    @Override
    public ImmutableList<OV> versions() {
        return Lists.immutable.ofAll(versionPropertyMap.values());
    }

    @Override
    public byte[] getBytes() {
        return entityReference.get().getBytes();
    }

    @Override
    public FieldDataType entityDataType() {
        return entityReference.get().entityDataType();
    }

    @Override
    public FieldDataType versionDataType() {
        return entityReference.get().versionDataType();
    }

    @Override
    public int nid() {
        return entityReference.get().nid();
    }

    @Override
    public long mostSignificantBits() {
        return entityReference.get().mostSignificantBits();
    }

    @Override
    public long leastSignificantBits() {
        return entityReference.get().leastSignificantBits();
    }

    @Override
    public long[] additionalUuidLongs() {
        return entityReference.get().additionalUuidLongs();
    }

    public Iterable<ObservableSemantic> getObservableSemanticList() {
        throw new UnsupportedOperationException();
    }


    // TODO: replace with JEP 502: Stable Values when finalized to allow lazy initialization of feature.
    final AtomicReference<FeatureWrapper> publicIdFeatureReference = new AtomicReference<>();
    private FeatureWrapper getPublicIdFeature() {
        return publicIdFeatureReference.updateAndGet(currentValue -> currentValue != null
                ? currentValue
                : makePublicIdFeature());
    }
    private FeatureWrapper makePublicIdFeature() {
        return new FeatureWrapper(this.publicId(),
                Binding.Component.pattern().nid(),
                Binding.Component.publicIdFieldDefinitionIndex(),
                this,
                FeatureKey.Entity.PublicId(this.nid()));
    }

    @Override
    public final ImmutableList<Feature> getFeatures() {
        // TODO: replace with JEP 502: Stable Values when finalized to allow lazy initialization of feature lists.
        // TODO: Handle changes in StampCalculator.
        MutableList<Feature> features = Lists.mutable.empty();

        // Public ID:
        features.add(getPublicIdFeature());
        // Versions
        features.add(this.versionSetAsList);

        for (OV version : versions()) {
            features.add(version);
        }

        addAdditionalChronologyFeatures(features);

        return features.toImmutable();
    }

    protected abstract void addAdditionalChronologyFeatures(MutableList<Feature> features);

    public Feature<?> getFeature(FeatureKey featureKey) {
        return switch (featureKey) {
            case FeatureKey.ChronologyFeature chronologyFeatureKey -> getFeatures().select(feature -> chronologyFeatureKey.match(feature.featureKey())).getOnly();
            case FeatureKey.VersionFeature versionFeatureKey -> getVersion(versionFeatureKey.stampNid()).get().getFeature(versionFeatureKey);
        };
    }

    public final class EntityFeature implements Feature<ObservableEntity<OV>> {

        private EntityFeature() {
        }

        @Override
        public ReadOnlyProperty<? extends Feature<ObservableEntity<OV>>> featureProperty() {
            return entityFeatureWrapper;
        }

        @Override
        public FeatureKey featureKey() {
            return FeatureKey.Entity.Object(ObservableEntity.this.nid());
        }

        @Override
        public ObservableComponent containingComponent() {
            return ObservableEntity.this;
        }

        @Override
        public int patternNid() {
            return switch (ObservableEntity.this) {
                case ObservableConcept _-> Binding.Concept.pattern().nid();
                case ObservablePattern _-> Binding.Pattern.pattern().nid();
                case ObservableSemantic _-> Binding.Semantic.pattern().nid();
                case ObservableStamp _-> Binding.Stamp.pattern().nid();
            };
        }

        @Override
        public int indexInPattern() {
            // TODO: replace with a new index after adding a new field for the meaning and purpose of the entity class
            return 0;
        }

        @Override
        public String toString() {
            return ObservableEntity.this.getClass().getSimpleName() + " Feature<" + nid() + "> " + PrimitiveData.text(nid());
        }

    }

    // TODO: replace with JEP 502: Stable Values when finalized to allow lazy initialization of feature.
    private final ReadOnlyObjectProperty<? extends Feature<ObservableEntity<OV>>> entityFeatureWrapper =
            new ReadOnlyObjectWrapper<>(this, this.getClass().getSimpleName(), new ObservableEntity<OV>.EntityFeature()).getReadOnlyProperty();

    private ReadOnlyProperty<? extends Feature<ObservableEntity<OV>>> featureProperty() {
        return entityFeatureWrapper;
    }

    public Feature<ObservableEntity<OV>> asFeature() {
        return entityFeatureWrapper.getValue();
    }

    private static class EntityChangeSubscriber implements Subscriber<Integer> {

        @Override
        public void onNext(Integer nid) {
            // Do nothing with item, but request another...
            if (CANONICAL_INSTANCES.getIfPresent(nid) != null) {
                if (!Platform.isFxApplicationThread()) {
                    Platform.runLater(() -> get(nid));
                } else {
                    get(nid);
                }
            }
        }
    }
}
