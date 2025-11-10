package dev.ikm.komet.layout.area;


import dev.ikm.komet.framework.observable.ObservableSemanticVersion;
import javafx.beans.property.ReadOnlyProperty;
import javafx.scene.layout.Pane;

/**
 * The {@code KlSemanticVersionPane} interface represents a pane that displays a single version
 * of a semantic entity.
 *
 * This interface extends {@link KlAreaForVersion} and is specifically tailored for
 * handling {@link ObservableSemanticVersion} types.
 *
 * It provides methods to access and interact with versions of semantic entities
 * in the context of a JavaFX UI component.
 *
 * @param <FX> the type of the Pane that the interface works with
 *
 * @see KlAreaForVersion
 * @see ObservableSemanticVersion
 */
public non-sealed interface KlAreaForSemanticVersion<FX extends Pane> extends KlAreaForVersion<ObservableSemanticVersion, FX> {
    default ObservableSemanticVersion semanticVersion() {
        return KlAreaForVersion.super.version();
    }

    default ReadOnlyProperty<ObservableSemanticVersion> semanticVersionProperty() {
        return versionProperty();
    }

    non-sealed interface Factory<FX extends Pane, KL extends KlAreaForSemanticVersion<FX>>
            extends KlAreaForVersion.Factory<FX, ObservableSemanticVersion, KL> {
    }
}
