package dev.ikm.komet.layout.area;

import javafx.beans.property.Property;
import javafx.beans.property.ReadOnlyProperty;

public interface KlFeaturePropertyForArea<F> {

    /**
     * Sets the property for this property area using the provided value.
     *
     * @param property the property of type {@code PT} to set for this property area
     */
    default void setFeatureProperty(ReadOnlyProperty<F> property) {
        featurePropertyWrapper().setValue(property);
    }

    /**
     * Retrieves the property associated with this property area.
     *
     * @return the property of type {@code PT} associated with this property area.
     */
    default ReadOnlyProperty<F> getFeatureProperty() {
        return featurePropertyWrapper().getValue();
    }

    Property<ReadOnlyProperty<F>> featurePropertyWrapper();
}
