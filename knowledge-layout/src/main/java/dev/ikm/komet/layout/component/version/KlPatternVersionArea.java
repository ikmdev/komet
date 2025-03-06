package dev.ikm.komet.layout.component.version;


import dev.ikm.komet.framework.observable.ObservablePatternVersion;
import javafx.beans.property.ObjectProperty;
import javafx.scene.layout.Pane;

/**
 * The {@code KlPatternVersionPane} interface represents a specialized version pane for managing
 * and presenting instances of {@link ObservablePatternVersion} within the context of a UI layout.
 * It extends the generic {@code KlVersionPane} to provide specific functionality for handling pattern
 * version-related entities while maintaining compatibility with the broader version pane framework.
 *
 * @param <P> the type of the JavaFX {@link Pane} that is used in this pane
 *
 * @see ObservablePatternVersion
 * @see KlVersionArea
 */
public non-sealed interface KlPatternVersionArea<P extends Pane> extends KlVersionArea<ObservablePatternVersion, P> {
    /**
     * Returns the observable pattern version associated with this pane.
     * The method retrieves the version by delegating to the {@code version()} method
     * of the {@code KlVersionPane} interface, ensuring consistent behavior with
     * the observable version handling defined in the parent contract.
     *
     * @return the observable pattern version associated with this pane
     */
    default ObservablePatternVersion patternVersion() {
        return KlVersionArea.super.version();
    }

     /**
      * Returns the property containing the {@link ObservablePatternVersion} associated with this pane.
      * This method provides an observable property for tracking and interacting with the version
      * data of the pattern, delegating to the {@code versionProperty()} method for implementation.
      *
      * @return the property containing the {@link ObservablePatternVersion} associated with this pane
      */
     default ObjectProperty<ObservablePatternVersion> patternVersionProperty() {
        return versionProperty();
     }
}
