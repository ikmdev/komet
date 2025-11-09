
package dev.ikm.komet.framework.observable;

/**
 * Marker interface for observable chronologies (versioned entities).
 * <p>
 * Provides a generic-free type for working with observable entities without
 * exposing version type parameters. This is Layer 1 (Marker) of the MGC pattern
 * for observable entities.
 *
 * <h2>MGC Pattern Layers for Observable Entities</h2>
 * <ul>
 *   <li><b>Layer 1:</b> {@code ObservableChronology} - Marker interface (this interface)</li>
 *   <li><b>Layer 2:</b> {@link ObservableEntity} - Generic abstract class</li>
 *   <li><b>Layer 3:</b> {@link ObservableConcept}, {@link ObservablePattern},
 *       {@link ObservableSemantic}, {@link ObservableStamp} - Concrete final classes</li>
 * </ul>
 *
 * <h2>Usage</h2>
 * <p>
 * Use this marker interface for method parameters and return types when you don't
 * need to know the specific version type:
 *
 * <pre>{@code
 * // Clean signature without generics
 * public void process(ObservableChronology chronology) {
 *     // Pattern matching for specifics
 *     switch (chronology) {
 *         case ObservableConcept concept -> handleConcept(concept);
 *         case ObservableSemantic semantic -> handleSemantic(semantic);
 *         case ObservablePattern pattern -> handlePattern(pattern);
 *         case ObservableStamp stamp -> handleStamp(stamp);
 *     }
 * }
 *
 * // Simple collections
 * List<ObservableChronology> entities = new ArrayList<>();
 * }</pre>
 *
 * <h2>Thread Safety</h2>
 * <p>
 * All observable chronologies must be accessed from the JavaFX application thread.
 *
 * @see ObservableEntity
 * @see ObservableConcept
 * @see ObservablePattern
 * @see ObservableSemantic
 * @see ObservableStamp
 */
public sealed interface ObservableChronology
        permits ObservableConcept, ObservablePattern, ObservableSemantic, ObservableStamp {
    // Marker interface - intentionally empty
}

