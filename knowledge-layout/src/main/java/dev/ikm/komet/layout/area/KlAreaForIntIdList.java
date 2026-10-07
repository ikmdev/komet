package dev.ikm.komet.layout.area;

import dev.ikm.komet.framework.observable.Feature;
import dev.ikm.tinkar.common.bind.annotations.axioms.ParentConcept;
import dev.ikm.tinkar.common.bind.annotations.names.FullyQualifiedName;
import dev.ikm.tinkar.common.bind.annotations.names.RegularName;
import dev.ikm.tinkar.common.id.LongIdList;
import javafx.scene.layout.Region;

/**
 * Represents a specialized field area in the Knowledge Layout framework specifically for
 * LongIdList data types. This interface extends {@link KlAreaForFeature} and provides type-safe
 * operations for managing observable LongIdList fields and their associated JavaFX {@code Region}.
 *
 * It is a non-sealed interface, allowing for further extension and customization.
 *
 * @param <FX> The type of JavaFX {@link Region} associated with this field area, used for
 *             displaying or managing the LongIdList field.
 */
@FullyQualifiedName("Knowledge layout LongIdList field area")
@RegularName("LongIdList field area")
@ParentConcept(KlAreaForFeature.class)
public non-sealed interface KlAreaForIntIdList<FX extends Region>
        extends KlAreaForFeature<LongIdList, Feature<LongIdList>, FX> {

    /**
     * Represents a factory interface for creating and managing instances of
     * {@link KlAreaForIntIdList}, which are specialized field areas in the
     * Knowledge Layout framework designed for managing observable LongIdList fields
     * and their associated JavaFX {@link Region}.
     * <p>     * This interface extends {@link KlAreaForFeature.Factory} with LongIdList-specific
     * behavior, enabling the creation of field areas that bind observable LongIdList
     * fields to JavaFX regions. It defines the contract for building, configuring,
     * and interacting with these field areas in a type-safe manner, ensuring proper
     * integration of LongIdList data types with corresponding UI components.
     *
     * @param <FX> the type of JavaFX {@link Region} associated with the field area,
     *             extending {@code Region}. This represents the UI component or
     *             layout element for managing and displaying LongIdList field data.
     */
    interface Factory<FX extends Region>
            extends KlAreaForFeature.Factory<LongIdList, Feature<LongIdList>, FX, KlAreaForIntIdList<FX>> {
    }
}
