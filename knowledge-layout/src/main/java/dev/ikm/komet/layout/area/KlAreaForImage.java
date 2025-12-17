package dev.ikm.komet.layout.area;

import dev.ikm.komet.framework.observable.Feature;
import dev.ikm.tinkar.common.bind.annotations.axioms.ParentConcept;
import dev.ikm.tinkar.common.bind.annotations.names.FullyQualifiedName;
import dev.ikm.tinkar.common.bind.annotations.names.RegularName;
import javafx.scene.layout.Region;

/**
 * Represents a specialized field area in the Knowledge Layout framework specifically for
 * byte[] data types. This interface extends {@link KlFieldArea} and provides type-safe
 * operations for managing observable byte[] fields and their associated JavaFX {@code Region}.
 *
 * It is a non-sealed interface, allowing for further extension and customization.
 *
 * @param <FX> The type of JavaFX {@link Region} associated with this field area, used for
 *             displaying or managing the byte[] field.
 */
@FullyQualifiedName("Knowledge layout image byte[] field area")
@RegularName("image byte[] field area")
@ParentConcept(KlFieldArea.class)
public non-sealed interface KlAreaForImage<FX extends Region>
        extends KlAreaForFeature<byte[], Feature<byte[]>, FX> {

    /**
     * Represents a factory interface for creating and managing instances of
     * {@link KlAreaForImage}, which are specialized field areas in the
     * Knowledge Layout framework designed for managing observable byte[] fields
     * and their associated JavaFX {@link Region}.
     * <p>
     * This interface extends {@link KlFieldArea.Factory} with byte[]-specific
     * behavior, enabling the creation of field areas that bind observable byte[]
     * fields to JavaFX regions. It defines the contract for building, configuring,
     * and interacting with these field areas in a type-safe manner, ensuring proper
     * integration of byte[] data types with corresponding UI components.
     *
     * @param <FX> the type of JavaFX {@link Region} associated with the field area,
     *             extending {@code Region}. This represents the UI component or
     *             layout element for managing and displaying byte[] field data.
     */
    interface Factory<FX extends Region>
            extends KlAreaForFeature.Factory<byte[], Feature<byte[]>, FX, KlAreaForImage<FX>> {
    }
}
