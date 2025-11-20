
package dev.ikm.komet.framework.observable;

/**
 * Marker interface for observable feature types.
 * <p>
 * Completes the Marker-Generic-Concrete pattern for the observable feature hierarchy,
 * providing a generic-free type for downstream method signatures.
 * 
 * <h2>MGC Pattern Layers for Observable Features</h2>
 * <ul>
 *   <li><b>Layer 1:</b> {@code ObservableFeatureType} - Marker interface (this interface)</li>
 *   <li><b>Layer 2:</b> {@link ObservableSemanticField} - Generic abstract class</li>
 *   <li><b>Layer 3:</b> {@link ObservableField}, {@link ObservableFeatureDefinition} - Concrete classes</li>
 * </ul>
 * 
 * @see ObservableSemanticField
 * @see ObservableField
 * @see ObservableFeatureDefinition
 */
public sealed interface ObservableFeature
        permits ObservableSemanticField, ObservableFeatureDefinition {
    // Marker interface - intentionally empty
}
