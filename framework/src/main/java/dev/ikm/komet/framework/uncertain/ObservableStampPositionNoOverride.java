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
 * 
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

