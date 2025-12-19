package dev.ikm.komet.layout.area;

import dev.ikm.komet.framework.observable.Feature;
import dev.ikm.tinkar.common.bind.annotations.axioms.ParentConcept;
import dev.ikm.tinkar.common.bind.annotations.names.FullyQualifiedName;
import dev.ikm.tinkar.common.bind.annotations.names.RegularName;
import dev.ikm.tinkar.terms.EntityFacade;
import javafx.scene.layout.Region;

/**
 * Represents a non-sealed interface in the Knowledge Layout framework for managing
 * field areas specifically associated with an entity facade and JavaFX regional components.
 * This interface extends {@link KlFieldArea} and provides a specialized contract for
 * defining and interacting with field areas tailored to entities.
 *
 * This interface is generic and allows for type-safe interactions with JavaFX regions
 * by parameterizing the region type. It supports extensibility for customized field areas
 * while maintaining core functionality defined by the parent {@link KlFieldArea}.
 * @param <FX> The type of JavaFX {@link Region} associated with this field area
 *             for managing and displaying fields bound to entity data.
 */
@FullyQualifiedName("Knowledge layout entity feature area")
@RegularName("Component (EntityFacade) area")
@ParentConcept(KlAreaForFeature.class)
public non-sealed interface KlAreaForEntityFacade<FX extends Region> // TODO Rename to KlAreaForEntityFacade
        extends KlAreaForFeature<EntityFacade, Feature<EntityFacade>, FX> {


    /**
     * Represents a factory interface within the Knowledge Layout framework for creating and managing
     * field areas specifically intended for entities and their corresponding JavaFX {@code Region} components.
     * This interface extends {@code KlFieldArea.Factory} to provide tailored support for creating
     * field areas associated with entities.
     *
     * This interface defines the contract for building and initializing components that bind
     * {@code ObservableChronology}-related observable fields to JavaFX regions of a specified type.
     * It enables property observation and manipulation with type-safe operations, maintaining
     * specialized behavior for entity-focused field management.
     *
     * @param <FX> the type of JavaFX {@link Region} associated with the field area, extending {@code Region}.
     */
    interface Factory<FX extends Region, KL extends KlAreaForEntityFacade<FX>>
            extends KlAreaForFeature.Factory<EntityFacade, Feature<EntityFacade>, FX, KL> {
    }

}
