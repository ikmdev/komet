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

import javafx.beans.property.ListProperty;
import javafx.beans.property.ObjectProperty;
import javafx.beans.property.Property;
import javafx.beans.property.SetProperty;
import dev.ikm.tinkar.coordinate.stamp.StateSet;
import dev.ikm.tinkar.terms.ConceptFacade;

interface StampFilterTemplateProperties {

    default Property<?>[] getBaseProperties() {
        return new Property<?>[] {
                allowedStatesProperty(),
                moduleSpecificationsProperty(),
                excludedModuleSpecificationsProperty(),
                modulePriorityOrderProperty()
        };
    }

    default ObservableCoordinate<?>[] getEmbeddedCoordinates() {
        return new ObservableCoordinate<?>[]{

        };
    }

    /**
     *
     * @return a set of allowed status values to filter computation results.
     */
    ObjectProperty<StateSet> allowedStatesProperty();

    /**
     *
     * @return the specified modules property
     */
    SetProperty<ConceptFacade> moduleSpecificationsProperty();

    /**
     *
     * @return the specified modules property
     */
    SetProperty<ConceptFacade> excludedModuleSpecificationsProperty();

    /**
     * Module preference list property.
     *
     * @return the object property
     */
    ListProperty<ConceptFacade> modulePriorityOrderProperty();
}
