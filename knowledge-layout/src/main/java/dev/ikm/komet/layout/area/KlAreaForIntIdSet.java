package dev.ikm.komet.layout.area;

import dev.ikm.komet.framework.observable.Feature;
import dev.ikm.tinkar.common.bind.annotations.axioms.ParentConcept;
import dev.ikm.tinkar.common.bind.annotations.names.FullyQualifiedName;
import dev.ikm.tinkar.common.bind.annotations.names.RegularName;
import dev.ikm.tinkar.common.id.LongIdSet;
import javafx.scene.layout.Region;

/**
 * Represents a specialized field area in the Knowledge Layout framework specifically for
 * LongIdSet data types. This interface extends {@link KlAreaForFeature} and provides type-safe
 * operations for managing observable LongIdSet fields and their associated JavaFX {@code Region}.
 *
 * It is a non-sealed interface, allowing for further extension and customization.
 *
 * @param <FX> The type of JavaFX {@link Region} associated with this field area, used for
 *             displaying or managing the LongIdSet field.
 */
@FullyQualifiedName("Knowledge layout LongIdSet field area")
@RegularName("LongIdSet field area")
@ParentConcept(KlAreaForFeature.class)
public non-sealed interface KlAreaForIntIdSet<FX extends Region>
        extends KlAreaForFeature<LongIdSet, Feature<LongIdSet>, FX> {

    /**
     * Represents a factory interface for creating and managing instances of
     * {@link KlAreaForIntIdSet}, which are specialized field areas in the
     * Knowledge Layout framework designed for managing observable LongIdSet fields
     * and their associated JavaFX {@link Region}.
     * <p>     * This interface extends {@link KlAreaForFeature.Factory} with LongIdSet-specific
     * behavior, enabling the creation of field areas that bind observable LongIdSet
     * fields to JavaFX regions. It defines the contract for building, configuring,
     * and interacting with these field areas in a type-safe manner, ensuring proper
     * integration of LongIdSet data types with corresponding UI components.
     *
     * @param <FX> the type of JavaFX {@link Region} associated with the field area,
     *             extending {@code Region}. This represents the UI component or
     *             layout element for managing and displaying LongIdSet field data.
     */
    interface Factory<FX extends Region>
            extends KlAreaForFeature.Factory<LongIdSet, Feature<LongIdSet>, FX, KlAreaForIntIdSet<FX>> {
    }
}
