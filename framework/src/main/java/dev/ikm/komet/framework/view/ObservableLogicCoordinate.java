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

//~--- non-JDK imports --------------------------------------------------------

//~--- interfaces -------------------------------------------------------------

import javafx.beans.property.ObjectProperty;
import javafx.beans.property.Property;
import dev.ikm.tinkar.coordinate.logic.LogicCoordinateRecord;
import dev.ikm.tinkar.coordinate.logic.LogicCoordinateDelegate;
import dev.ikm.tinkar.terms.ConceptFacade;
import dev.ikm.tinkar.terms.PatternFacade;

/**
 * The Interface ObservableLogicCoordinate.
 *
 * 
 */
public interface ObservableLogicCoordinate
        extends LogicCoordinateDelegate, ObservableCoordinate<LogicCoordinateRecord> {

   @Override
   default ObservableCoordinate<?>[] getCompositeCoordinates() {
      return new ObservableCoordinate[0];
   }

   default Property<?>[] getBaseProperties() {
      return new Property<?>[] {
              classifierProperty(),
              conceptMemberPatternProperty(),
              descriptionLogicProfileProperty(),
              inferredAxiomsPatternProperty(),
              statedAxiomsPatternProperty(),
              statedNavigationPatternProperty(),
              inferredNavigationPatternProperty(),
              rootConceptProperty()
      };
   }

   /**
    * Classifier property.
    *
    * @return the classifier concept property. 
    */
   ObjectProperty<ConceptFacade> classifierProperty();

   /**
    * Concept assemblage property.
    *
    * @return the assemblage concept property. 
    */
   ObjectProperty<PatternFacade> conceptMemberPatternProperty();

   /**
    * Description logic profile property.
    *
    * @return the description logic profile concept property. 
    */
   ObjectProperty<ConceptFacade> descriptionLogicProfileProperty();


   /**
    * Stated assemblage property.
    *
    * @return the stated assemblage concept property.
    */
   ObjectProperty<PatternFacade> statedAxiomsPatternProperty();
   /**
    * Inferred assemblage property.
    *
    * @return the inferred assemblage concept property. 
    */
   ObjectProperty<PatternFacade> inferredAxiomsPatternProperty();

   ObjectProperty<PatternFacade> statedNavigationPatternProperty();

   ObjectProperty<PatternFacade> inferredNavigationPatternProperty();

   ObjectProperty<ConceptFacade> rootConceptProperty();
}

