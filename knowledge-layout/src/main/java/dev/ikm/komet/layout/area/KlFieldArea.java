package dev.ikm.komet.layout.area;

import dev.ikm.komet.framework.observable.ObservableField;
import dev.ikm.tinkar.common.bind.annotations.names.FullyQualifiedName;
import dev.ikm.tinkar.common.bind.annotations.names.RegularName;
import javafx.beans.property.Property;
import javafx.scene.layout.Region;

/**
 * Represents a sealed interface in the Knowledge Layout framework for managing
 * field areas associated with specific data types and their corresponding observable
 * fields and JavaFX regions. This interface extends {@link KlAreaForFeature} and serves
 * as a foundational component for defining and interacting with various concrete
 * field area implementations.
 *
 * This interface supports generic type parameters to allow for type-safe operations
 * on specific data types, their observable field representations, and associated
 * JavaFX regional components. It enables observation, binding, and manipulation of
 * field data while maintaining a modular and extensible architecture.
 *
 * @param <DT> The data type associated with this field area.
 * @param <FX> The type of JavaFX {@link Region} associated with this field area for displaying or managing the field.
 */
@FullyQualifiedName("Knowledge layout field area")
@RegularName("Field area")
@Deprecated
public non-sealed interface KlFieldArea<DT, FX extends Region>
        extends KlAreaForFeature<DT, ObservableField<DT>, FX> {


    /**
     * Sets the specified field to this field area. This method assigns an observable
     * field of type {@code DT} to the attribute area, representing a piece of data
     * or functionality to be managed and displayed within the associated JavaFX {@code Region}.
     *
     * @param field the observable field of type {@code DT} that needs to be set and
     *              managed within this field area
     */
    default void setField(ObservableField<DT> field) {
        if (getFeatureProperty() instanceof Property property) {
            property.setValue(field);
        } else {
            throw new IllegalStateException("Property is ReadOnly");
        }
    }

    /**
     * Retrieves the observable field being managed in the current field area.
     * The observable field is of type {@code DT} and represents a piece of
     * data or functionality that is monitored and potentially updated
     * within the associated JavaFX {@code Region}.
     *
     * @return the observable field of type {@code DT} that is set for this field area
     */
    default ObservableField<DT> getField() {
        return getFeatureProperty().getValue();
    }


    /**
     * Represents a factory interface for creating and managing instances of knowledge layout field areas
     * that associate a specific data type with JavaFX {@code Region} elements and their corresponding
     * field handling areas.
     *
     * This interface extends {@code KlPropertyArea.Factory} to provide specialized support for the creation
     * and configuration of field areas that bind observable fields of a specified data type to associated
     * JavaFX regions. It defines the contract for building region components integrated with field properties
     * and enabling property observation and manipulation through type-safe operations.
     *
     * @param <DT> the data type managed by the field area.
     * @param <FX> the type of JavaFX region associated with the field area, extending {@code Region}.
     * @param <KL> the type of field area, extending {@code KlFieldArea}, representing the logical area and
     *            its behavior for managing observable fields of type {@code DT} with regions of type {@code FX}.
     */
    interface Factory<DT, FX extends Region, KL extends KlFieldArea<DT, FX>>
            extends KlAreaForFeature.Factory<DT, ObservableField<DT>, FX, KL> {

    }

}
