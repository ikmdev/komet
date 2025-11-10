package dev.ikm.komet.layout.area;

import dev.ikm.komet.framework.observable.Feature;
import dev.ikm.tinkar.common.bind.annotations.axioms.ParentConcept;
import dev.ikm.tinkar.common.bind.annotations.names.FullyQualifiedName;
import dev.ikm.tinkar.common.bind.annotations.names.RegularName;
import dev.ikm.tinkar.terms.StampFacade;
import javafx.scene.layout.Region;


/**
 * Represents a specialized non-sealed interface in the Knowledge Layout (KL) system
 * designed for managing stamp-based field areas within a field layout.
 * This interface extends {@link KlFieldArea} with a fixed data type of {@link StampFacade}
 * and a generic type for JavaFX regions.
 * <p>
 * The KlFieldAreaForStamp interface establishes a contract for interacting with
 * and managing fields that are specifically tied to stamp data. It inherits
 * the core functionality from its parent interface while providing additional
 * specificity for handling stamps, encapsulated in the {@link StampFacade}.
 * The associated JavaFX region type is defined generically using {@code FX}.
 *
 * @param <FX> The type of JavaFX {@link Region} associated with this stamp-based field area.
 */
@FullyQualifiedName("Knowledge layout stamp feature area")
@RegularName("Stamp area")
@ParentConcept(KlAreaForFeature.class)
public non-sealed interface KlAreaForStamp<FX extends Region>
        extends KlAreaForFeature<StampFacade, Feature<StampFacade>, FX> {

    /**
     * Represents a factory interface for creating and managing instances of {@code KlFieldAreaForStamp},
     * which are specialized field areas designed for managing observable fields of type {@code StampFacade}
     * associated with JavaFX {@code Region} elements.
     * <p>
     * This interface extends {@code KlFieldArea.Factory} and provides the contract for
     * creating and configuring field areas specifically for stamp-based observable fields.
     * It leverages the type-safe operations defined in the parent interface to bind and
     * manage {@code StampFacade} fields within JavaFX components.
     *
     * @param <FX> the type of JavaFX region associated with the field area, extending {@code Region}.
     */
    interface Factory<FX extends Region>
            extends KlAreaForFeature.Factory<StampFacade, Feature<StampFacade>, FX, KlAreaForStamp<FX>> {
    }
}
