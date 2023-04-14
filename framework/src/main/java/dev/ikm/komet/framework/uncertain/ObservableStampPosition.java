/* 
 * Licensed under the Apache License, Version 2.0 (the "License");
 *
 * You may not use this file except in compliance with the License.
 *
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *
 * See the License for the specific language governing permissions and
 * limitations under the License.
 *
 * Contributions from 2013-2017 where performed either by US government 
 * employees, or under US Veterans Health Administration contracts. 
 *
 * US Veterans Health Administration contributions by government employees
 * are work of the U.S. Government and are not subject to copyright
 * protection in the United States. Portions contributed by government 
 * employees are USGovWork (17USC §105). Not subject to copyright. 
 * 
 * Contribution by contractors to the US Veterans Health Administration
 * during this period are contractually contributed under the
 * Apache License, Version 2.0.
 *
 * See: https://www.usa.gov/government-works
 * 
 * Contributions prior to 2013:
 *
 * Copyright (C) International Health Terminology Standards Development Organisation.
 * Licensed under the Apache License, Version 2.0.
 *
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
 * @author kec
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

