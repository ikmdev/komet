package dev.ikm.komet.framework.observable;

import dev.ikm.tinkar.entity.EntityVersion;
import org.eclipse.collections.api.list.ImmutableList;

/**
 * Marker interface for observable entity versions.
 * <p>
 * Provides a generic-free type for working with observable versions without
 * exposing entity and version record type parameters. This is Layer 1 (Marker)
 * of the MGC pattern for observable versions.
 *
 * <h2>MGC Pattern Layers for Observable Versions</h2>
 * <ul>
 *   <li><b>Layer 1:</b> {@code ObservableVersion} - Marker interface (this interface)</li>
 *   <li><b>Layer 2:</b> {@link ObservableEntityVersion} - Generic abstract class</li>
 *   <li><b>Layer 3:</b> {@link ObservableConceptVersion}, {@link ObservablePatternVersion},
 *       {@link ObservableSemanticVersion}, {@link ObservableStampVersion} - Concrete final classes</li>
 * </ul>
 *
 * <h2>Relationship to EditableVersion</h2>
 * <p>
 * {@code ObservableVersion} represents read-only versions with JavaFX properties for UI binding.
 * {@link EditableVersion} represents editable versions with cached changes.
 *
 * <pre>{@code
 * ObservableVersion ←→ EditableVersion
 *        │                    │
 *   Read-only             Editable
 *   Properties            Cached changes
 *   Immediate updates     save()/commit()
 * }</pre>
 *
 * <h2>Usage</h2>
 * <pre>{@code
 * // Clean method signature
 * public void displayVersion(ObservableVersion version) {
 *     // Pattern matching for specific types
 *     switch (version) {
 *         case ObservableConceptVersion cv -> displayConcept(cv);
 *         case ObservableSemanticVersion sv -> displaySemantic(sv);
 *         default -> displayGeneric(version);
 *     }
 * }
 * }</pre>
 *
 * @see ObservableEntityVersion
 * @see EditableVersion
 * @see ObservableConceptVersion
 * @see ObservablePatternVersion
 * @see ObservableSemanticVersion
 * @see ObservableStampVersion
 */
public sealed interface ObservableVersion extends ObservableComponent, Feature<ObservableVersion>
        permits ObservableConceptVersion, ObservableEntityVersion, ObservablePatternVersion, ObservableSemanticVersion, ObservableStampVersion {
    // Marker interface - intentionally empty
    
    /**
     * Returns the observable entity (chronology) containing this version.
     */
    ObservableChronology getObservableEntity();
    
    /**
     * Returns the underlying entity version record.
     */
    EntityVersion getVersionRecord();


    @Override
    default int indexInPattern() {
        return switch (this) {
            case ObservableConceptVersion cv -> cv.indexInPattern();
            case ObservablePatternVersion pv -> pv.indexInPattern();
            case ObservableSemanticVersion sv -> sv.indexInPattern();
            case ObservableStampVersion sv -> sv.indexInPattern();
        };
    }

    @Override
    default int patternNid() {
        return switch (this) {
            case ObservableConceptVersion cv -> cv.patternNid();
            case ObservablePatternVersion pv -> pv.patternNid();
            case ObservableSemanticVersion sv -> sv.patternNid();
            case ObservableStampVersion sv -> sv.patternNid();
        };
    }

}