package dev.ikm.komet.layout.area;

import dev.ikm.komet.layout.KlArea;
import javafx.beans.property.Property;
import javafx.collections.ObservableList;
import javafx.scene.layout.Region;

/**
 * Represents a sealed interface within the Knowledge Layout (KL) framework for managing
 * and interacting with areas that handle observable lists of elements. This interface defines
 * contract methods for managing and observing list-based properties and their JavaFX
 * region-based representation.
 * <p>
 * The primary purpose of this interface is to provide functionality for retrieving
 * and updating the managed list of elements, as well as observing the selected elements
 * in the list. These methods facilitate seamless integration between the observable
 * data structure and its UI representation.
 *
 * @param <LE> the type of elements contained within the observable list
 * @param <FX> the type of the JavaFX region associated with the list area, extending {@code Region}
 */
public sealed interface KlAreaForList<LE, OL extends ObservableList<LE>, FX extends Region>
        extends KlArea<FX>, KlFeaturePropertyForArea<OL>
        permits KlAreaForListOfFeatures {


    /**
     * Retrieves the observable list of elements managed by this list area. The returned list
     * represents the current set of elements managed within the area.
     *
     * @return an {@code ObservableList} of elements of type {@code LE}, representing the current list managed by the area.
     */
    default OL getList() {
        return getFeatureProperty().getValue();
    }

    /**
     * Sets the observable list of elements managed by this list area. This method assigns
     * a new {@code ObservableList} of type {@code LE} to the current property of the list area.
     *
     * @param list the {@code ObservableList} of elements of type {@code LE} to be managed
     *             and stored within this list area
     */
    default void setList(OL list) {
        if (getFeatureProperty() instanceof Property property) {
            property.setValue(list);
        } else {
            throw new IllegalStateException("Property is ReadOnly");
        }
    }


    /**
     * Retrieves the observable list of selected elements within the list area. The returned list
     * contains the currently selected elements of type {@code LE}, which are dynamically observed
     * and updated to reflect changes in the selection state of the underlying list.
     *
     * @return an {@code ObservableList} of selected elements of type {@code LE}, representing
     *         the current selection within the list area.
     */
    ObservableList<LE> selectedItems();


    /**
     * Represents a factory interface within the Knowledge Layout (KL) framework for creating
     * and managing instances of list areas that handle observable lists of elements and their
     * associated JavaFX regions.
     * <p>
     * This factory extends the functionality of {@code KlPropertyArea.Factory} by specializing
     * in the creation of list-based property areas. It focuses on regions containing observable
     * lists and provides support for bindings and operations specific to such lists.
     *
     * @param <LE> the type of elements contained within the observable list managed by the list area.
     * @param <FX> the type of JavaFX region associated with the list area, extending {@code Region}.
     * @param <KL> the type of list area, extending {@code KlListArea}, which represents the area for
     *             managing observable lists of type {@code LE} associated with regions of type {@code FX}.
     */
    non-sealed interface Factory<LE, OL extends ObservableList<LE>, FX extends Region, KL extends KlAreaForList<LE, OL, FX>>
            extends KlArea.Factory<FX, KL> {
    }
}
