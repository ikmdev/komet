package dev.ikm.komet.layout.area;

import dev.ikm.komet.framework.observable.ObservableVersion;
import javafx.scene.layout.Pane;

/**
 * The {@code KlGenericVersionPane} interface represents a generic implementation of {@link KlAreaForVersion}
 * tailored to work with observable versions of entities.
 * <p>
 * This interface extends the {@code KlVersionPane} with the following specifications:
 * - It works with {@link ObservableVersion} as the version type.
 * - It uses a JavaFX Pane or a subclass thereof specified by the generic parameter {@code P}.
 * <p>
 * Purpose:
 * - The {@code KlGenericVersionPane} serves as a flexible and reusable interface for managing
 *   and displaying versioned entities in UI components.
 * <p>
 * Design:
 * - This non-sealed interface can be further extended or implemented for specific use cases
 *   involving generic pane types and entity versions.
 * <p>
 * Type Parameters:
 * @param <FX> the type of the JavaFX Pane this version pane manages
 *
 * @see KlAreaForVersion
 * @see ObservableVersion
 */
public non-sealed interface KlAreaForGenericVersion<FX extends Pane> extends KlAreaForVersion<ObservableVersion, FX> {

    non-sealed interface Factory<FX extends Pane, KL extends KlAreaForGenericVersion<FX>>
            extends KlAreaForVersion.Factory<FX, ObservableVersion, KL> {

    }
}
