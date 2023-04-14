package dev.ikm.komet.framework.view;

import javafx.beans.property.Property;
import dev.ikm.tinkar.coordinate.ImmutableCoordinate;

public interface ObservableCoordinate<T extends ImmutableCoordinate> extends Property<T> {
    /**
     *
     * @return The properties this coordinate defines, not the properties that contained
     * coordinates may define.
     */
    Property<?>[] getBaseProperties();

    /**
     *
     * @return composite coordinates, so that properties of composite coordinates can be
     * recursively identified.
     */
    ObservableCoordinate<?>[] getCompositeCoordinates();

    default boolean hasOverrides() {
        for (Property property: getBaseProperties()) {
            if (property instanceof PropertyWithOverride) {
                PropertyWithOverride propertyWithOverride = (PropertyWithOverride) property;
                if (propertyWithOverride.isOverridden()) {
                    return true;
                }
            }
        }
        for (ObservableCoordinate coordinate: getCompositeCoordinates()) {
            if (coordinate.hasOverrides()) {
                return true;
            }
        }
        return false;
    }

    default void removeOverrides() {
        for (Property property: getBaseProperties()) {
            if (property instanceof PropertyWithOverride) {
                PropertyWithOverride propertyWithOverride = (PropertyWithOverride) property;
                if (propertyWithOverride.isOverridden()) {
                    propertyWithOverride.removeOverride();
                }
            }
        }
        for (ObservableCoordinate coordinate: getCompositeCoordinates()) {
            if (coordinate.hasOverrides()) {
                coordinate.removeOverrides();
            }
        }
        this.setValue(getOriginalValue());
    }

    void setExceptOverrides(T updatedCoordinate);

    /**
     * If the underlying coordinate supports overrides, returns the original value of the coordinate removing any
     * overrides. If the underlying coordinate does not support overrides, returns the current value of the coordinate.
     * @return the original value of this coordinate.
     */
    T getOriginalValue();

}
