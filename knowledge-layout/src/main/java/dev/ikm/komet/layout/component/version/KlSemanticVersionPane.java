package dev.ikm.komet.layout.component.version;


import dev.ikm.komet.framework.observable.ObservableSemanticVersion;
import javafx.beans.property.ObjectProperty;

/**
 * The {@code KlSemanticVersionPane} interface represents a pane that displays a single version
 * of a semantic entity.
 *
 * This interface is a specialization of {@link KlVersionPane} for handling
 * {@link ObservableSemanticVersion} types.
 *
 * @see KlVersionPane
 * @see ObservableSemanticVersion
 */
public non-sealed interface KlSemanticVersionPane extends KlVersionPane<ObservableSemanticVersion> {
    default ObservableSemanticVersion semanticVersion() {
        return KlVersionPane.super.version();
    }

    default ObjectProperty<ObservableSemanticVersion> semanticVersionProperty() {
        return versionProperty();
    }
}
