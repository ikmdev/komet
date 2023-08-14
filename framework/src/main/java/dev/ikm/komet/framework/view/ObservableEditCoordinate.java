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

import javafx.beans.property.ObjectProperty;
import javafx.beans.property.Property;
import dev.ikm.tinkar.coordinate.edit.EditCoordinateDelegate;
import dev.ikm.tinkar.coordinate.edit.EditCoordinateRecord;
import dev.ikm.tinkar.terms.ConceptFacade;

//~--- interfaces -------------------------------------------------------------

/**
 * The Interface ObservableEditCoordinate.
 *
 * 
 */
public interface ObservableEditCoordinate
        extends EditCoordinateDelegate, ObservableCoordinate<EditCoordinateRecord> {
    @Override
    default EditCoordinateRecord toEditCoordinateRecord() {
        return this.getValue();
    }

    default Property<?>[] getBaseProperties() {
        return new Property<?>[]{
                authorForChangesProperty(),
                defaultModuleProperty(),
                destinationModuleProperty(),
                defaultPathProperty(),
                promotionPathProperty()
        };
    }

    /**
     * Author Nid property.
     *
     * @return the integer property
     */
    ObjectProperty<ConceptFacade> authorForChangesProperty();

    /**
     * Module nid property.
     *
     * @return the integer property
     */
    ObjectProperty<ConceptFacade> defaultModuleProperty();

    /**
     * Module nid property.
     *
     * @return the integer property
     */
    ObjectProperty<ConceptFacade> destinationModuleProperty();

    /**
     * Default path property.
     *
     * @return the integer property
     */
    ObjectProperty<ConceptFacade> defaultPathProperty();

    /**
     * Path nid property.
     *
     * @return the integer property
     */
    ObjectProperty<ConceptFacade> promotionPathProperty();

    default ObservableCoordinate<?>[] getCompositeCoordinates() {
        return new ObservableCoordinate<?>[]{};
    }

}

