package dev.ikm.komet.framework.observable;

import dev.ikm.tinkar.coordinate.stamp.calculator.Latest;
import dev.ikm.tinkar.coordinate.view.calculator.ViewCalculator;
import org.eclipse.collections.api.list.ImmutableList;

import java.util.Comparator;
import java.util.function.Predicate;

/**
 * View-specific projection of an {@link ObservableStamp} providing access to stamp versions
 * categorized by their temporal and coordination state.
 * <p>
 * Unlike other snapshot types, {@code ObservableStampSnapshot} has simplified semantics because
 * stamps represent immutable change metadata (Status, Time, Author, Module, Path) rather than
 * domain concepts that evolve over time. A stamp entity typically has a single version that
 * captures the metadata about when and by whom a change was made.
 *
 * <h2>Stamp Snapshot Characteristics</h2>
 * <p>
 * While stamps technically can have versions (for rare metadata corrections), in practice:
 * <ul>
 *   <li><b>Single Version Common:</b> Most stamp entities have exactly one version</li>
 *   <li><b>No Contradictions Expected:</b> Stamp metadata corrections are rare and typically sequential</li>
 *   <li><b>Historic Versions Rare:</b> When present, indicate metadata corrections after creation</li>
 *   <li><b>View Filtering Still Applies:</b> Stamps on different paths may still be filtered by view coordinates</li>
 * </ul>
 *
 * <h2>⚠️ How to Create: Use ObservableEntityHandle</h2>
 * <p>
 * <b>DO NOT</b> construct {@code ObservableStampSnapshot} instances directly. Instead,
 * use {@link ObservableEntityHandle} for type-safe snapshot creation:
 *
 * <pre>{@code
 * // ✅ CORRECT: Use ObservableEntityHandle
 * ViewCalculator viewCalc = // ... from ViewCoordinateRecord
 * ObservableStampSnapshot snapshot =
 *     ObservableEntityHandle.getStampSnapshotOrThrow(stampNid, viewCalc);
 *
 * // ✅ CORRECT: Fluent API with optional handling
 * ObservableEntityHandle.get(nid)
 *     .ifStampGetSnapshot(viewCalc, snapshot -> {
 *         Latest<ObservableStampVersion> latest = snapshot.getLatestVersion();
 *         displayStampMetadata(latest);
 *     });
 *
 * // ✅ CORRECT: Safe optional extraction
 * Optional<ObservableStampSnapshot> optSnapshot =
 *     ObservableEntityHandle.get(nid).asStampSnapshot(viewCalc);
 * }</pre>
 *
 * <h2>Common Usage Patterns</h2>
 *
 * <p><b>Pattern 1: Display Stamp Metadata</b>
 * <pre>{@code
 * ObservableStampSnapshot snapshot =
 *     ObservableEntityHandle.getStampSnapshotOrThrow(stampNid, viewCalc);
 *
 * Latest<ObservableStampVersion> latest = snapshot.getLatestVersion();
 * if (latest.isPresent()) {
 *     ObservableStampVersion stamp = latest.get();
 *     // Bind to UI
 *     statusLabel.textProperty().bind(stamp.stateProperty().asString());
 *     authorLabel.textProperty().bind(stamp.authorProperty().asString());
 *     timeLabel.textProperty().bind(stamp.timeProperty().asString());
 * }
 * }</pre>
 *
 * <p><b>Pattern 2: Check for Metadata Corrections</b>
 * <pre>{@code
 * ObservableStampSnapshot snapshot =
 *     ObservableEntityHandle.getStampSnapshotOrThrow(stampNid, viewCalc);
 *
 * if (!snapshot.getHistoricVersions().isEmpty()) {
 *     // Unusual case - stamp metadata was corrected
 *     LOG.warn("Stamp {} has {} historic versions (metadata corrections)",
 *         stampNid, snapshot.getHistoricVersions().size());
 *
 *     // Show correction history
 *     for (ObservableStampVersion historic : snapshot.getHistoricVersions()) {
 *         displayCorrection(historic);
 *     }
 * }
 * }</pre>
 *
 * <p><b>Pattern 3: Access Stamp Fields</b>
 * <pre>{@code
 * ObservableStampSnapshot snapshot =
 *     ObservableEntityHandle.getStampSnapshotOrThrow(stampNid, viewCalc);
 *
 * Latest<ObservableStampVersion> latest = snapshot.getLatestVersion();
 * latest.ifPresent(stamp -> {
 *     State state = stamp.state();
 *     long time = stamp.time();
 *     int authorNid = stamp.authorNid();
 *     int moduleNid = stamp.moduleNid();
 *     int pathNid = stamp.pathNid();
 *
 *     processStampMetadata(state, time, authorNid, moduleNid, pathNid);
 * });
 * }</pre>
 *
 * <h2>Thread Safety</h2>
 * <p>
 * Like all observable snapshots, {@code ObservableStampSnapshot} <b>must be accessed from the
 * JavaFX application thread</b>. The underlying {@link ObservableStamp} and {@link ViewCalculator}
 * require JavaFX threading.
 *
 * <h2>Canonical Version Guarantee</h2>
 * <p>
 * {@link ObservableStampVersion} instances returned by this snapshot are the <b>same canonical
 * instances</b> from the underlying {@link ObservableStamp}'s {@code versionPropertyMap()}.
 * This means UI property bindings to stamp versions work correctly and receive change notifications.
 *
 * @see ObservableEntityHandle
 * @see ObservableStamp
 * @see ObservableStampVersion
 * @see ObservableEntitySnapshot
 * @see ViewCalculator
 */
public final class ObservableStampSnapshot
        extends ObservableEntitySnapshot<ObservableStamp, ObservableStampVersion> {

    /**
     * Creates a new stamp snapshot for the given view calculator and observable stamp.
     * <p>
     * <b>Package-private constructor:</b> External code should use {@link ObservableEntityHandle}
     * to create snapshots, not this constructor directly.
     *
     * @param viewCalculator the view calculator defining what versions are visible
     * @param entity the observable stamp entity to snapshot
     * @throws IllegalStateException if no latest version can be determined
     */
    public ObservableStampSnapshot(ViewCalculator viewCalculator, ObservableStamp entity) {
        super(viewCalculator, entity);
    }

    // ========== Type-Safe Method Overrides ==========
    // These override the base class methods to return specific types rather than generic types

    /**
     * Returns the processed versions as an immutable list.
     * <p>
     * Processed versions start as all versions in the stamp, but can be filtered,
     * sorted, and locked via {@link #filterProcessedVersions(Predicate)},
     * {@link #sortProcessedVersions(Comparator)}, and {@link #lockProcessedVersions()}.
     *
     * @return immutable list of processed stamp versions
     * @see #filterProcessedVersions(Predicate)
     * @see #sortProcessedVersions(Comparator)
     */
    @Override
    public ImmutableList<ObservableStampVersion> getProcessedVersions() {
        return super.getProcessedVersions();
    }

    /**
     * Filters the processed versions according to the given predicate.
     * <p>
     * Only versions matching the predicate are retained. This can be called multiple
     * times to progressively narrow the version set.
     *
     * <pre>{@code
     * // Filter to only active stamps (if stamps have state - unusual case)
     * snapshot.filterProcessedVersions(version ->
     *     version.state() == State.ACTIVE);
     * }</pre>
     *
     * @param filter predicate to test each version
     */
    @Override
    public void filterProcessedVersions(Predicate<ObservableStampVersion> filter) {
        super.filterProcessedVersions(filter);
    }

    /**
     * Sorts the processed versions according to the given comparator.
     * <p>
     * Common use case: Sort by time to show chronological order of any metadata corrections.
     *
     * <pre>{@code
     * // Sort by time (newest first)
     * snapshot.sortProcessedVersions((v1, v2) ->
     *     Long.compare(v2.time(), v1.time()));
     * }</pre>
     *
     * @param comparator comparator to determine version order
     */
    @Override
    public void sortProcessedVersions(Comparator<ObservableStampVersion> comparator) {
        super.sortProcessedVersions(comparator);
    }

    /**
     * Returns the observable stamp entity this snapshot represents.
     * <p>
     * The stamp entity contains all versions across all paths and modules.
     * This snapshot filters those versions according to view coordinates.
     *
     * @return the observable stamp entity
     */
    @Override
    public ObservableStamp observableEntity() {
        return super.observableEntity();
    }

    /**
     * Returns uncommitted stamp versions (unsaved local changes).
     * <p>
     * Uncommitted stamp versions are rare - they would only exist if stamp metadata
     * was being edited but not yet persisted. In most cases, stamps are created once
     * and never modified.
     *
     * @return immutable list of uncommitted versions (typically empty)
     */
    @Override
    public ImmutableList<ObservableStampVersion> getUncommittedVersions() {
        return super.getUncommittedVersions();
    }

    /**
     * Returns historic stamp versions (superseded by newer versions).
     * <p>
     * Historic stamp versions indicate metadata corrections after initial creation.
     * This is rare - stamps are typically created once with correct metadata and never changed.
     * When present, historic versions show the evolution of stamp metadata over time.
     *
     * @return immutable list of historic versions, sorted newest to oldest
     */
    @Override
    public ImmutableList<ObservableStampVersion> getHistoricVersions() {
        return super.getHistoricVersions();
    }

    /**
     * Returns the latest stamp version(s) according to view coordinates.
     * <p>
     * In typical cases, this returns a single stamp version with no contradictions.
     * Contradictions would only occur if metadata corrections were made on different
     * development paths and not yet merged.
     *
     * <p><b>Usage Example:</b>
     * <pre>{@code
     * Latest<ObservableStampVersion> latest = snapshot.getLatestVersion();
     * if (latest.contradictions().isEmpty()) {
     *     // Normal case - single current version
     *     ObservableStampVersion stamp = latest.get();
     *     displayStampMetadata(stamp);
     * } else {
     *     // Unusual case - metadata corrections on different paths
     *     LOG.warn("Stamp has contradictions: {}", latest.contradictions().size());
     *     resolveStampContradictions(latest);
     * }
     * }</pre>
     *
     * @return Latest wrapper containing the current version(s) and any contradictions
     */
    @Override
    public Latest<ObservableStampVersion> getLatestVersion() {
        return super.getLatestVersion();
    }
}