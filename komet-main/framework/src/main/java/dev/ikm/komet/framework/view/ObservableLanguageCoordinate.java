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


import javafx.beans.property.ListProperty;
import javafx.beans.property.ObjectProperty;
import javafx.beans.property.Property;
import dev.ikm.tinkar.coordinate.language.LanguageCoordinateRecord;
import dev.ikm.tinkar.coordinate.language.LanguageCoordinateDelegate;
import dev.ikm.tinkar.terms.ConceptFacade;
import dev.ikm.tinkar.terms.PatternFacade;

//~--- interfaces -------------------------------------------------------------

/**
 * The Interface ObservableLanguageCoordinate.
 *
 * 
 */
public interface ObservableLanguageCoordinate
        extends LanguageCoordinateDelegate, ObservableCoordinate<LanguageCoordinateRecord> {

    default Property<?>[] getBaseProperties() {
        return new Property<?>[] {
                languageConceptProperty(),
                descriptionPatternPreferenceListProperty(),
                descriptionTypePreferenceListProperty(),
                dialectPatternPreferenceListProperty(),
                modulePreferenceListForLanguageProperty(),
        };
    }

    default ObservableCoordinate<?>[] getCompositeCoordinates() {
        return new ObservableCoordinate<?>[]{};
    }
    ListProperty<PatternFacade> descriptionPatternPreferenceListProperty();

    /**
     * 
     * @return the language coordinate that this observable wraps. 
     */
    LanguageCoordinateRecord getLanguageCoordinate();

    /**
     * Language concept nid property.
     *
     * @return the integer property
     */
    ObjectProperty<ConceptFacade> languageConceptProperty();

    /**
    * Description type preference list property.
    *
    * @return the object property
    */
    ListProperty<ConceptFacade> descriptionTypePreferenceListProperty();

   /**
    * Dialect assemblage preference list property.
    *
    * @return the object property
    */
   ListProperty<PatternFacade> dialectPatternPreferenceListProperty();

   ListProperty<ConceptFacade> modulePreferenceListForLanguageProperty();
}

