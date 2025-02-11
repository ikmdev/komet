package dev.ikm.komet.layout.component.version;


import dev.ikm.komet.framework.observable.ObservablePatternVersion;
import javafx.beans.property.ObjectProperty;

/**
 * The {@code KlPatternVersionPane} interface represents a pane that displays a single version
 * of a pattern.
 *
 * This interface is a specialization of {@link KlVersionPane} for handling
 * {@link ObservablePatternVersion} types.
 *
 * @see KlVersionPane
 * @see ObservablePatternVersion
 */
public non-sealed interface KlPatternVersionPane extends KlVersionPane<ObservablePatternVersion> {
    /**
     * Returns the observable pattern version associated with this pane.
     * The method retrieves the version by delegating to the {@code version()} method
     * of the {@code KlVersionPane} interface, ensuring consistent behavior with
     * the observable version handling defined in the parent contract.
     *
     * @return the observable pattern version associated with this pane
     */
    default ObservablePatternVersion patternVersion() {
        return KlVersionPane.super.version();
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
