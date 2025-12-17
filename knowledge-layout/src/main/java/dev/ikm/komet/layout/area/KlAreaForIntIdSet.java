package dev.ikm.komet.layout.area;

import dev.ikm.komet.framework.observable.Feature;
import dev.ikm.tinkar.common.bind.annotations.axioms.ParentConcept;
import dev.ikm.tinkar.common.bind.annotations.names.FullyQualifiedName;
import dev.ikm.tinkar.common.bind.annotations.names.RegularName;
import dev.ikm.tinkar.common.id.IntIdSet;
import javafx.scene.layout.Region;

/**
 * Represents a specialized field area in the Knowledge Layout framework specifically for
 * IntIdSet data types. This interface extends {@link KlFieldArea} and provides type-safe
 * operations for managing observable IntIdSet fields and their associated JavaFX {@code Region}.
 *
 * It is a non-sealed interface, allowing for further extension and customization.
 *
 * @param <FX> The type of JavaFX {@link Region} associated with this field area, used for
 *             displaying or managing the IntIdSet field.
 */
@FullyQualifiedName("Knowledge layout IntIdSet field area")
@RegularName("IntIdSet field area")
@ParentConcept(KlFieldArea.class)
public non-sealed interface KlAreaForIntIdSet<FX extends Region>
        extends KlAreaForFeature<IntIdSet, Feature<IntIdSet>, FX> {

    /**
     * Represents a factory interface for creating and managing instances of
     * {@link KlAreaForIntIdSet}, which are specialized field areas in the
     * Knowledge Layout framework designed for managing observable IntIdSet fields
     * and their associated JavaFX {@link Region}.
     * <p>
     * This interface extends {@link KlFieldArea.Factory} with IntIdSet-specific
     * behavior, enabling the creation of field areas that bind observable IntIdSet
     * fields to JavaFX regions. It defines the contract for building, configuring,
     * and interacting with these field areas in a type-safe manner, ensuring proper
     * integration of IntIdSet data types with corresponding UI components.
     *
     * @param <FX> the type of JavaFX {@link Region} associated with the field area,
     *             extending {@code Region}. This represents the UI component or
     *             layout element for managing and displaying IntIdSet field data.
     */
    interface Factory<FX extends Region>
            extends KlAreaForFeature.Factory<IntIdSet, Feature<IntIdSet>, FX, KlAreaForIntIdSet<FX>> {
    }
}
