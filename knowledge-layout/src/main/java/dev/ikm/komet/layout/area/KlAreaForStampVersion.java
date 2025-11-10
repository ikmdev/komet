package dev.ikm.komet.layout.area;

import dev.ikm.komet.framework.observable.ObservableStampVersion;
import javafx.beans.property.ReadOnlyProperty;
import javafx.scene.layout.Pane;


/**
 * The {@code KlStampVersionPane} interface represents a specialized implementation of
 * {@link KlAreaForVersion} for managing and displaying observable stamp versions in UI components.
 * <p>
 * This interface is designated to handle {@link ObservableStampVersion} types and offers
 * default methods to access and manage the stamp version and its property associated with a pane.
 * <p>
 * Design:
 * - This interface extends the functionality of {@code KlVersionPane} focusing on the
 *   {@code ObservableStampVersion} as the version type.
 * - It inherits methods from {@code KlVersionPane} for version management and provides
 *   convenience wrappers specific to stamp versions.
 * <p>
 * Type Parameters:
 * @param <FX> the type of JavaFX {@link Pane} managed by this interface
 *
 * @see KlAreaForVersion
 * @see ObservableStampVersion
 */
public non-sealed interface KlAreaForStampVersion<FX extends Pane> extends KlAreaForVersion<ObservableStampVersion, FX> {

     default ObservableStampVersion stampVersion() {
        return KlAreaForVersion.super.version();
    }

    default ReadOnlyProperty<ObservableStampVersion> stampVersionProperty() {
         return versionProperty();
    }

    non-sealed interface Factory<FX extends Pane, KL extends KlAreaForStampVersion<FX>>
            extends KlAreaForVersion.Factory<FX, ObservableStampVersion, KL> {
    }
}
