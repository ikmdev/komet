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

import org.eclipse.collections.api.factory.Lists;
import org.eclipse.collections.api.list.ImmutableList;
import org.eclipse.collections.api.list.MutableList;
import dev.ikm.tinkar.common.id.IntIdCollection;
import dev.ikm.tinkar.coordinate.stamp.calculator.Latest;
import dev.ikm.tinkar.coordinate.stamp.calculator.VersionCategory;
import dev.ikm.tinkar.coordinate.view.calculator.ViewCalculator;
import dev.ikm.tinkar.entity.EntityVersion;

import java.util.Comparator;
import java.util.function.Predicate;

/**
 * View-specific projection of an {@link ObservableEntity} that provides access to versions categorized
 * by their temporal and coordination state, enabling "time travel" and version analysis within a specific
 * view context.
 * <p>
 * {@code ObservableEntitySnapshot} is the bridge between raw entity versions and meaningful clinical or
 * business semantics by filtering and categorizing versions according to a {@link ViewCalculator}'s
 * coordinate rules (what paths are visible, which modules are included, what time range applies, etc.).
 *
 * <h2>What is an ObservableEntitySnapshot?</h2>
 * <p>
 * An {@code ObservableEntitySnapshot} represents a <b>view-specific, temporally-aware projection</b> of an
 * observable entity that provides:
 * <ul>
 *   <li><b>Latest Version:</b> The most current version(s) according to view coordinates, which may include
 *       contradictions if multiple versions are current on different paths</li>
 *   <li><b>Time Travel:</b> Access to historic versions, showing what the entity looked like at previous points
 *       in time within the view's scope</li>
 *   <li><b>Version Categorization:</b> Automatic classification into {@link VersionCategory} types:
 *       <ul>
 *           <li>{@code UncontradictedLatest} - Single current version</li>
 *           <li>{@code ContradictedLatest} - Multiple current versions (conflicts on different paths)</li>
 *           <li>{@code Prior} - Historic version superseded by newer versions</li>
 *           <li>{@code Uncommitted} - Local changes not yet persisted</li>
 *       </ul>
 *   </li>
 *   <li><b>Version Processing:</b> Filter, sort, and process versions according to application-specific logic</li>
 * </ul>
 *
 * <h2>Why Use Snapshots?</h2>
 * <p>
 * Direct access to {@link ObservableEntity} gives you <i>all</i> versions across <i>all</i> paths and modules.
 * Snapshots solve three critical problems:
 * <ol>
 *   <li><b>Coordination-Aware Access:</b> Automatically filters versions according to view coordinates
 *       (development path, module scope, time range, language preferences)</li>
 *   <li><b>Semantic Clarity:</b> Distinguishes "current" from "historic" from "contradicting" versions,
 *       enabling proper UI presentation and business logic</li>
 *   <li><b>Performance:</b> Pre-computes version categorization and provides efficient access patterns
 *       for common UI/logic needs</li>
 * </ol>
 *
 * <h2>⚠️ How to Create: Use ObservableEntityHandle</h2>
 * <p>
 * <b>DO NOT</b> construct {@code ObservableEntitySnapshot} instances directly using {@code new}. Instead,
 * use {@link ObservableEntityHandle}, which provides type-safe factory methods for creating snapshots:
 *
 * <pre>{@code
 * // ✅ CORRECT: Use ObservableEntityHandle to get snapshots
 * ViewCalculator viewCalc = // ... from ViewCoordinateRecord
 * ObservableConceptSnapshot snapshot = 
 *     ObservableEntityHandle.getConceptSnapshotOrThrow(conceptNid, viewCalc);
 *
 * // ✅ CORRECT: Fluent API with optional handling
 * ObservableEntityHandle.get(nid)
 *     .ifConceptGetSnapshot(viewCalc, snapshot -> {
 *         Latest<ObservableConceptVersion> latest = snapshot.getLatestVersion();
 *         displayCurrentState(latest);
 *     });
 *
 * // ✅ CORRECT: Safe optional extraction
 * Optional<ObservableConceptSnapshot> optSnapshot = 
 *     ObservableEntityHandle.get(nid).asConceptSnapshot(viewCalc);
 *
 * // ❌ WRONG: Direct construction (fragile, no type safety)
 * ObservableConcept concept = ObservableEntity.get(nid); // deprecated anyway
 * ObservableConceptSnapshot snapshot = new ObservableConceptSnapshot(viewCalc, concept); // DON'T DO THIS
 * }</pre>
 *
 * <h2>Understanding Version Categories</h2>
 * <p>
 * Each version in the snapshot is categorized by {@link #getVersionCategory(EntityVersion)}:
 *
 * <table style="border: 1px solid black; border-collapse: collapse;">
 * <caption>Version Category Meanings</caption>
 * <tr>
 *   <th>Category</th>
 *   <th>Meaning</th>
 *   <th>Use Case</th>
 * </tr>
 * <tr>
 *   <td><b>UncontradictedLatest</b></td>
 *   <td>Single current version with no conflicts</td>
 *   <td>Display in UI, use in calculations</td>
 * </tr>
 * <tr>
 *   <td><b>ContradictedLatest</b></td>
 *   <td>Multiple current versions on different paths</td>
 *   <td>Show conflict warning, require user resolution</td>
 * </tr>
 * <tr>
 *   <td><b>Prior</b></td>
 *   <td>Historic version superseded by newer changes</td>
 *   <td>Audit trail, version history view</td>
 * </tr>
 * <tr>
 *   <td><b>Uncommitted</b></td>
 *   <td>Local edits not yet persisted to database</td>
 *   <td>Preview changes, show "dirty" state</td>
 * </tr>
 * </table>
 *
 * <h2>Common Usage Patterns</h2>
 *
 * <p><b>Pattern 1: Display Current State</b>
 * <pre>{@code
 * ObservableConceptSnapshot snapshot = 
 *     ObservableEntityHandle.getConceptSnapshotOrThrow(nid, viewCalc);
 *
 * Latest<ObservableConceptVersion> latest = snapshot.getLatestVersion();
 * if (latest.contradictions().isEmpty()) {
 *     // Single current version - safe to display
 *     ObservableConceptVersion current = latest.get();
 *     titleLabel.textProperty().bind(current.descriptionProperty());
 * } else {
 *     // Multiple versions - show conflict warning
 *     showConflictDialog(latest.get(), latest.contradictions());
 * }
 * }</pre>
 *
 * <p><b>Pattern 2: Show Version History ("Time Travel")</b>
 * <pre>{@code
 * ObservableConceptSnapshot snapshot = 
 *     ObservableEntityHandle.getConceptSnapshotOrThrow(nid, viewCalc);
 *
 * // Show current version
 * displayVersion(snapshot.getLatestVersion().get(), "Current");
 *
 * // Show what it looked like at previous times
 * for (ObservableConceptVersion historic : snapshot.getHistoricVersions()) {
 *     StampEntity stamp = EntityHandle.getStampOrThrow(historic.stampNid());
 *     displayVersion(historic, "As of " + formatTime(stamp.time()));
 * }
 * }</pre>
 *
 * <p><b>Pattern 3: Process Versions with Filtering</b>
 * <pre>{@code
 * ObservableConceptSnapshot snapshot = 
 *     ObservableEntityHandle.getConceptSnapshotOrThrow(nid, viewCalc);
 *
 * // Filter to only active versions
 * snapshot.filterProcessedVersions(version -> 
 *     EntityHandle.getStampOrThrow(version.stampNid()).state() == State.ACTIVE);
 *
 * // Sort by time (newest first)
 * snapshot.sortProcessedVersions((v1, v2) -> 
 *     Long.compare(v2.time(), v1.time()));
 *
 * // Lock to prevent further modifications
 * snapshot.lockProcessedVersions();
 *
 * // Use processed versions
 * for (ObservableConceptVersion version : snapshot.getProcessedVersions()) {
 *     displayInTimeline(version);
 * }
 * }</pre>
 *
 * <p><b>Pattern 4: Handle Uncommitted Changes</b>
 * <pre>{@code
 * ObservableConceptSnapshot snapshot = 
 *     ObservableEntityHandle.getConceptSnapshotOrThrow(nid, viewCalc);
 *
 * if (!snapshot.getUncommittedVersions().isEmpty()) {
 *     // Show "unsaved changes" indicator
 *     saveButton.setVisible(true);
 *     statusLabel.setText("Modified (not saved)");
 *     
 *     // Preview what will be saved
 *     ObservableConceptVersion uncommitted = snapshot.getUncommittedVersions().get(0);
 *     previewArea.setText(uncommitted.description());
 * }
 * }</pre>
 *
 * <h2>Thread Safety</h2>
 * <p>
 * Like {@link ObservableEntity}, snapshots <b>must be accessed from the JavaFX application thread</b>.
 * The {@link ViewCalculator} and underlying entity properties require JavaFX threading. If you need
 * snapshot data in background threads, extract the necessary data first, then process it off-thread:
 *
 * <pre>{@code
 * // JavaFX thread - create snapshot and extract data
 * ObservableConceptSnapshot snapshot = 
 *     ObservableEntityHandle.getConceptSnapshotOrThrow(nid, viewCalc);
 * String description = snapshot.getLatestVersion().get().description();
 * ImmutableList<ObservableConceptVersion> historic = snapshot.getHistoricVersions();
 *
 * // Now safe to process in background thread
 * CompletableFuture.supplyAsync(() -> {
 *     return analyzeVersionHistory(description, historic);
 * }).thenAccept(result -> {
 *     Platform.runLater(() -> displayResults(result));
 * });
 * }</pre>
 *
 * <h2>Snapshot Lifecycle</h2>
 * <p>
 * Snapshots are <b>point-in-time</b> projections. They capture the state of the {@link ObservableEntity}
 * and {@link ViewCalculator} at the moment of creation. If the entity receives updates or the view
 * coordinates change, create a new snapshot to see the updated state.
 *
 * <h2>Canonical Version Guarantee for Property Binding</h2>
 * <p>
 * <b>Critical Guarantee:</b> The {@link ObservableVersion} instances returned by snapshot methods
 * (like {@link #getLatestVersion()}, {@link #getHistoricVersions()}, {@link #getUncommittedVersions()})
 * are the <b>exact same canonical instances</b> from the underlying {@link ObservableEntity}'s
 * {@code versionPropertyMap()}.
 *
 * <p><b>Why This Matters</b>
 * <p>
 * Because snapshots return canonical version instances, you can <b>reliably bind UI properties</b> to
 * versions extracted from a snapshot, and <b>all observers will receive change notifications</b>:
 *
 * <pre>{@code
 * // ✅ SAFE: Version binding from snapshot works correctly
 * ObservableConceptSnapshot snapshot = 
 *     ObservableEntityHandle.getConceptSnapshotOrThrow(nid, viewCalc);
 *
 * Latest<ObservableConceptVersion> latest = snapshot.getLatestVersion();
 * if (latest.isPresent()) {
 *     ObservableConceptVersion currentVersion = latest.get();
 *     
 *     // This binding will receive updates when the version changes
 *     titleLabel.textProperty().bind(currentVersion.descriptionProperty());
 *     statusLabel.textProperty().bind(currentVersion.stateProperty().asString());
 *     
 *     // All observers (UI components, listeners, etc.) see the same instance
 *     // and receive the same change notifications
 * }
 *
 * // ✅ SAFE: Historic version binding also works
 * for (ObservableConceptVersion historic : snapshot.getHistoricVersions()) {
 *     // Each historic version is also canonical - binding works correctly
 *     Label versionLabel = new Label();
 *     versionLabel.textProperty().bind(historic.descriptionProperty());
 *     historyView.add(versionLabel);
 * }
 * }</pre>
 *
 * <p><b>Implementation Detail</b>
 * <p>
 * The snapshot constructor iterates over the {@code observableEntity.versionPropertyMap().values()},
 * which contains the canonical {@link ObservableVersion} instances managed by the
 * {@link ObservableEntity#CANONICAL_INSTANCES} pool. The snapshot does not create copies - it
 * categorizes and stores <i>references</i> to the canonical versions.
 *
 * <p><b>Contrast with Immutable Entity Versions</b>
 * <p>
 * Unlike immutable {@link dev.ikm.tinkar.entity.EntityVersion} instances (which can have multiple
 * copies in memory with the same data), {@link ObservableVersion} instances follow the "canonical
 * instance" pattern - exactly one instance per (entity NID, stamp NID) combination exists in memory
 * while referenced. This is what makes property binding and change notification reliable.
 *
 * <p><b>What This Means for Multi-Snapshot Scenarios</b>
 * <pre>{@code
 * // Creating two snapshots from the same entity
 * ObservableConceptSnapshot snapshot1 = 
 *     ObservableEntityHandle.getConceptSnapshotOrThrow(nid, viewCalc1);
 * ObservableConceptSnapshot snapshot2 = 
 *     ObservableEntityHandle.getConceptSnapshotOrThrow(nid, viewCalc2);
 *
 * // Both snapshots may filter to different versions based on their view coordinates,
 * // BUT any versions they DO share are the SAME CANONICAL INSTANCES
 * ObservableConceptVersion v1 = snapshot1.getLatestVersion().get();
 * ObservableConceptVersion v2 = snapshot2.getLatestVersion().get();
 *
 * // If both view calculators identify the same version as "latest":
 * assert v1 == v2; // Same object instance (reference equality)
 *
 * // If you bind UI to v1, and another component binds to v2,
 * // both components will receive the same change notifications
 * label1.textProperty().bind(v1.descriptionProperty());
 * label2.textProperty().bind(v2.descriptionProperty());
 * // When description changes, BOTH labels update automatically
 * }</pre>
 *
 * <h2>Three Snapshot Types</h2>
 * <p>
 * This sealed class has three permitted implementations mirroring the three observable entity types:
 * <ul>
 *   <li>{@link ObservableConceptSnapshot} - Snapshot of {@link ObservableConcept}</li>
 *   <li>{@link ObservableSemanticSnapshot} - Snapshot of {@link ObservableSemantic}</li>
 *   <li>{@link ObservablePatternSnapshot} - Snapshot of {@link ObservablePattern}</li>
 * </ul>
 * <p>
 * Note: {@link ObservableStamp} does not have a snapshot variant because stamps represent immutable
 * change metadata and don't have multiple versions in the same sense as other entities.
 *
 * @param <OE> the observable entity type ({@link ObservableConcept}, {@link ObservableSemantic},
 *            or {@link ObservablePattern})
 * @param <OV> the observable version type ({@link ObservableConceptVersion}, {@link ObservableSemanticVersion},
 *            or {@link ObservablePatternVersion})
 * @see ObservableEntityHandle
 * @see ObservableEntity
 * @see ObservableConceptSnapshot
 * @see ObservableSemanticSnapshot
 * @see ObservablePatternSnapshot
 * @see ViewCalculator
 * @see VersionCategory
 * @see Latest
 */
public abstract sealed class ObservableEntitySnapshot<OE extends ObservableEntity<OV>,
        OV extends ObservableEntityVersion<?,?>>
        permits ObservableConceptSnapshot, ObservablePatternSnapshot, ObservableSemanticSnapshot, ObservableStampSnapshot {
    protected final Latest<OV> latestVersion;
    protected final IntIdCollection latestStampIds;
    protected final IntIdCollection allStampIds;
    protected final OE observableEntity;
    protected final ImmutableList<OV> uncommittedVersions;
    protected final ImmutableList<OV> historicVersions;
    protected final ViewCalculator viewCalculator;
    protected MutableList<OV> processedVersions;

    public ObservableEntitySnapshot(ViewCalculator viewCalculator, OE entity) {
        this.viewCalculator = viewCalculator;
        this.observableEntity = entity;
        this.latestVersion = viewCalculator.latest(entity);
        if (latestVersion.isPresent()) {
            this.allStampIds = latestVersion.get().entity().stampNids();
            this.latestStampIds = latestVersion.stampNids();
        } else {
            throw new IllegalStateException("No latest value: " + latestVersion);
        }
        processedVersions = Lists.mutable.ofInitialCapacity(entity.versions().size());
        MutableList<OV> uncommittedVersions = Lists.mutable.empty();
        MutableList<OV> historicVersions = Lists.mutable.empty();

        for (OV version : this.observableEntity.versionPropertyMap().values()) {
            processedVersions.add(version);
            if (version.uncommitted()) {
                uncommittedVersions.add(version);
            } else if (!latestStampIds.contains(version.stampNid())) {
                historicVersions.add(version);
            }
        }
        this.uncommittedVersions = uncommittedVersions.toImmutable();
        // reverse sort, oldest record at the end on seconds granularity...
        // since some changes (classification then incremental classification)
        historicVersions.sort((o1, o2) -> Long.compare(o2.time(), o1.time()));
        this.historicVersions = historicVersions.toImmutable();
    }


    //~--- methods -------------------------------------------------------------


    public ImmutableList<OV> getProcessedVersions() {
        return processedVersions.toImmutable();
    }

    public void filterProcessedVersions(Predicate<OV> filter) {
        processedVersions = processedVersions.select(filter::test);
    }

    public void sortProcessedVersions(Comparator<OV> comparator) {
        processedVersions = processedVersions.sortThis(comparator);
    }

    public void lockProcessedVersions() {
        processedVersions = processedVersions.asUnmodifiable();
    }

    public int nid() {
        return this.observableEntity.nid();
    }

    public OE observableEntity() {
        return observableEntity;
    }

    @Override
    public String toString() {
        return "Observable Snapshot{\n   latest: " + latestVersion +
                "\n   uncommitted: " + uncommittedVersions + "" +
                "\n   historic: " + historicVersions +
                "\n   latest stamps: " + latestStampIds +
                "\n   all stamps: " + allStampIds + '}';
    }

    public ImmutableList<OV> getUncommittedVersions() {
        return uncommittedVersions;
    }

    public ImmutableList<OV> getHistoricVersions() {
        return historicVersions;
    }

    public Latest<OV> getLatestVersion() {
        return latestVersion;
    }

    public VersionCategory getVersionCategory(EntityVersion version) {

        if (version.uncommitted()) {
            return VersionCategory.Uncommitted;
        }

        int stampNid = version.stampNid();

        if (latestStampIds.contains(stampNid)) {
            if (latestVersion.contradictions().isEmpty()) {
                return VersionCategory.UncontradictedLatest;
            }

            return VersionCategory.ContradictedLatest;
        }

        if (this.allStampIds.contains(stampNid)) {
            return VersionCategory.Prior;
        }
        // should never reach here.
        throw new IllegalStateException();
    }
}
