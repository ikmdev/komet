package dev.ikm.komet.framework.view;

import javafx.beans.property.LongProperty;
import javafx.beans.property.ObjectProperty;
import javafx.beans.property.Property;
import dev.ikm.tinkar.coordinate.stamp.StampCoordinateDelegate;
import dev.ikm.tinkar.coordinate.stamp.StampCoordinateRecord;
import dev.ikm.tinkar.terms.ConceptFacade;

public interface ObservableStampCoordinate
        extends StampCoordinateDelegate, StampFilterTemplateProperties, ObservableCoordinate<StampCoordinateRecord>  {

    default Property<?>[] getBaseProperties() {
        return new Property<?>[] {
                pathConceptProperty(),
                timeProperty(),
                moduleSpecificationsProperty(),
                excludedModuleSpecificationsProperty(),
                allowedStatesProperty(),
                modulePriorityOrderProperty()
        };
    }

    default ObservableCoordinate<?>[] getCompositeCoordinates() {
        return new ObservableCoordinate<?>[]{

        };
    }

    /**
     *
     * @return property that identifies the time for this filter.
     */
    LongProperty timeProperty();

    /**
     *
     * @return the property that identifies the path concept for this path coordinate
     */
    ObjectProperty<ConceptFacade> pathConceptProperty();

}
