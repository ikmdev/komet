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
package dev.ikm.komet.framework.uncertain;

//~--- non-JDK imports --------------------------------------------------------

import javafx.beans.property.LongProperty;
import javafx.beans.property.ObjectProperty;
import javafx.beans.property.Property;
import dev.ikm.komet.framework.view.ObservableCoordinate;
import dev.ikm.tinkar.coordinate.stamp.StampPositionRecord;
import dev.ikm.tinkar.coordinate.stamp.StampPositionDelegate;
import dev.ikm.tinkar.terms.ConceptFacade;
//~--- interfaces -------------------------------------------------------------

/**
 * The Interface ObservableStampPosition.
 *
 * 
 */
public interface ObservableStampPosition
        extends StampPositionDelegate, ObservableCoordinate<StampPositionRecord> {

    default Property<?>[] getBaseProperties() {
        return new Property<?>[] {
                pathConceptProperty(),
                timeProperty(),
        };
    }
    default ObservableCoordinate<?>[] getCompositeCoordinates() {
        return new ObservableCoordinate[0];
    }

   /**
    * Filter path nid property.
    *
    * @return the concept specification property for the path of this position is on.
    */
    ObjectProperty<ConceptFacade> pathConceptProperty();

   /**
    * Time property.
    *
    * @return the long property
    */
   LongProperty timeProperty();
}

