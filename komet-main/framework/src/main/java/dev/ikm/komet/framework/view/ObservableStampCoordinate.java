/*
 * Copyright © 2015 Integrated Knowledge Management (support@ikm.dev)
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
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
