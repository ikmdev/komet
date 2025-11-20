package dev.ikm.komet.layout.area;


import dev.ikm.komet.framework.observable.ObservablePatternVersion;
import javafx.beans.property.ReadOnlyProperty;
import javafx.scene.layout.Pane;

/**
 * The {@code KlPatternVersionPane} interface represents a specialized version pane for managing
 * and presenting instances of {@link ObservablePatternVersion} within the context of a UI layout.
 * It extends the generic {@code KlVersionPane} to provide specific functionality for handling pattern
 * version-related entities while maintaining compatibility with the broader version pane framework.
 *
 * @param <FX> the type of the JavaFX {@link Pane} that is used in this pane
 *
 * @see ObservablePatternVersion
 * @see KlAreaForVersion
 */
public non-sealed interface KlAreaForPatternVersion<FX extends Pane> extends KlAreaForVersion<ObservablePatternVersion, FX> {
    /**
     * Returns the observable pattern version associated with this pane.
     * The method retrieves the version by delegating to the {@code version()} method
     * of the {@code KlVersionPane} interface, ensuring consistent behavior with
     * the observable version handling defined in the parent contract.
     *
     * @return the observable pattern version associated with this pane
     */
    default ObservablePatternVersion patternVersion() {
        return KlAreaForVersion.super.version();
    }

     /**
      * Returns the property containing the {@link ObservablePatternVersion} associated with this pane.
      * This method provides an observable property for tracking and interacting with the version
      * data of the pattern, delegating to the {@code versionProperty()} method for implementation.
      *
      * @return the property containing the {@link ObservablePatternVersion} associated with this pane
      */
     default ReadOnlyProperty<ObservablePatternVersion> patternVersionProperty() {
        return versionProperty();
     }
    non-sealed interface Factory<FX extends Pane, KL extends KlAreaForPatternVersion<FX>>
            extends KlAreaForVersion.Factory<FX, ObservablePatternVersion, KL> {
    }}
