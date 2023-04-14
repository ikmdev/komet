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

//~--- JDK imports ------------------------------------------------------------

import javafx.beans.property.*;
import javafx.beans.value.ObservableValue;
import dev.ikm.komet.terms.KometTerm;
import dev.ikm.komet.framework.view.SimpleEqualityBasedObjectProperty;
import dev.ikm.tinkar.coordinate.stamp.StampPosition;
import dev.ikm.tinkar.coordinate.stamp.StampPositionRecord;
import dev.ikm.tinkar.terms.ConceptFacade;

//~--- non-JDK imports --------------------------------------------------------

//~--- classes ----------------------------------------------------------------

/**
 * The Class ObservableStampPositionImpl.
 *
 * @author kec
 */
public class ObservableStampPositionNoOverride
        extends ObservableStampPositionBase {

   //~--- constructors --------------------------------------------------------

   /**
    * Instantiates a new observable stamp position impl.
    *
    * @param stampPosition the stamp position
    */
   public ObservableStampPositionNoOverride(StampPositionRecord stampPosition, String coordinateName) {
      super(stampPosition, coordinateName);
   }
   public ObservableStampPositionNoOverride(StampPositionRecord stampPosition) {
      super(stampPosition, "Stamp position");
   }

   @Override
   public void setExceptOverrides(StampPositionRecord updatedCoordinate) {
      setValue(updatedCoordinate);
   }

   protected ObjectProperty<ConceptFacade> makePathConceptProperty(StampPosition stampPosition) {
      return new SimpleEqualityBasedObjectProperty(this,
              KometTerm.PATH_FOR_PATH_COORDINATE.toXmlFragment(),
              stampPosition.getPathForPositionConcept());
   }

   @Override
   protected StampPositionRecord baseCoordinateChangedListenersRemoved(ObservableValue<? extends StampPositionRecord> observable,
                                                                       StampPositionRecord oldValue, StampPositionRecord newValue) {
      this.pathConceptProperty().setValue(newValue.getPathForPositionConcept());
      this.timeProperty().set(newValue.time());
      return newValue;
   }


   protected LongProperty makeTimeProperty(StampPosition stampPosition) {
      return new SimpleLongProperty(this,
              KometTerm.POSITION_ON_PATH.toXmlFragment(),
              stampPosition.time());
   }

   @Override
   public StampPositionRecord getOriginalValue() {
      return getValue();
   }
}

