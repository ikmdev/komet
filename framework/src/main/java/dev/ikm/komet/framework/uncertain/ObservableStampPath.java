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

import javafx.beans.property.ListProperty;
import javafx.beans.property.ObjectProperty;
import javafx.beans.property.Property;
import javafx.beans.property.SetProperty;
import dev.ikm.komet.framework.view.ObservableCoordinate;
import dev.ikm.tinkar.coordinate.stamp.StampPathImmutable;
import dev.ikm.tinkar.coordinate.stamp.StampPathDelegate;
import dev.ikm.tinkar.coordinate.stamp.StampPositionRecord;
import dev.ikm.tinkar.terms.ConceptFacade;

//~--- interfaces -------------------------------------------------------------

/**
 * The ObservableStampPath implementation.
 *
 * 
 */
public interface ObservableStampPath extends ObservableCoordinate<StampPathImmutable>, StampPathDelegate {

   default Property<?>[] getBaseProperties() {
      return new Property<?>[] {
              pathConceptProperty(),
              pathOriginsProperty(),
      };
   }

   default ObservableCoordinate<?>[] getCompositeCoordinates() {
      return new ObservableCoordinate<?>[]{

      };
   }

   /**
    *
    * @return the property that identifies the path concept for this path coordinate
    */
   ObjectProperty<ConceptFacade> pathConceptProperty();

   /**
    *
    * @return the origins of this path.
    */
   SetProperty<StampPositionRecord> pathOriginsProperty();

   /**
    *
    * @return path origins as a list, as a convenience for interface elements based on
    * lists rather than on sets. Backed by the underlying set representation.
    */
   ListProperty<StampPositionRecord> pathOriginsAsListProperty();

}

