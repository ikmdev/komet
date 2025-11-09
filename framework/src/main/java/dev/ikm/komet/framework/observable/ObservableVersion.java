package dev.ikm.komet.framework.observable;

import dev.ikm.tinkar.entity.EntityVersion;
import org.eclipse.collections.api.list.ImmutableList;

/**
 * Marker interface for observable versions.
 * <p>
 * Simplifies downstream signatures by eliminating generic parameters.
 * Enables clean, generic-free APIs for version handling in UI and business logic.
 *
 * <p>This sealed interface follows the Marker-Generic-Concrete (MGC) pattern,
 * where this marker interface (Layer 1) provides a simple type alias,
 * {@link ObservableEntityVersion} (Layer 2) provides generic type safety,
 * and concrete classes (Layer 3) provide full type reification.
 *
 * <h2>Pattern Symmetry</h2>
 * <p>This interface mirrors {@link ObservableChronology} to maintain consistent
 * structure across the tightly-coupled entity/version hierarchies:
 * <pre>
 * Chronology Side:              Version Side:
 * ObservableChronology    ←→    ObservableVersion
 * ObservableEntity        ←→    ObservableEntityVersion
 * ObservableConcept       ←→    ObservableConceptVersion
 * </pre>
 *
 * <h2>Usage in Consumer APIs</h2>
 * <pre>{@code
 * // Clean, generic-free signatures
 * public void processVersion(ObservableVersion version) {
 *     // Work with any version type
 * }
 *
 * // Pattern matching on concrete types
 * switch (version) {
 *     case ObservableConceptVersion cv -> handleConcept(cv);
 *     case ObservablePatternVersion pv -> handlePattern(pv);
 *     case ObservableSemanticVersion sv -> handleSemantic(sv);
 *     case ObservableStampVersion stv -> handleStamp(stv);
 * }
 *
 * // Collections without wildcards
 * ObservableList<ObservableVersion> versions = FXCollections.observableArrayList();
 * }</pre>
 *
 * @see ObservableEntityVersion
 * @see ObservableChronology
 * @see ObservableEntity
 */
public sealed interface ObservableVersion
        extends EntityVersion, ObservableComponent, Feature<ObservableVersion>
        permits ObservableConceptVersion, ObservableEntityVersion,
                ObservablePatternVersion, ObservableSemanticVersion, ObservableStampVersion {

     EntityVersion getVersionRecord();

    /**
     * Returns the observable entity (chronology) that contains this version.
     * <p>
     * Returns the marker interface type for maximum flexibility in consumer code.
     */
    ObservableChronology getObservableEntity();

    @Override
    default int nid() {
        return getVersionRecord().nid();
    }

    @Override
    default int stampNid() {
        return getVersionRecord().stampNid();
    }
}