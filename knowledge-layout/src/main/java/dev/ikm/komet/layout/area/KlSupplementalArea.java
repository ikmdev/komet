package dev.ikm.komet.layout.area;

import dev.ikm.komet.layout.KlArea;
import dev.ikm.komet.layout.KlParent;
import javafx.scene.layout.Region;

/**
 * The KlSupplementalArea interface represents a specialized type of {@code KlArea}
 * that is designed to extend or complement the functionalities of a main area.
 * It provides a mechanism for flexible integration of additional features or
 * layout configurations in conjunction with the base area implementation.
 *
 * This interface is a non-sealed interface and can be implemented by various
 * concrete classes to define supplemental behavior or layout customizations
 * for specific use cases.
 *
 * @param <FX> the type of JavaFX {@code Region} associated with the implementation
 *             of this interface
 */
public non-sealed interface KlSupplementalArea<FX extends Region> extends KlArea<FX>, KlParent<FX> {
    non-sealed interface Factory<FX extends Region, KL extends KlSupplementalArea<FX>>
            extends KlArea.Factory<FX, KL> {
    }
}

