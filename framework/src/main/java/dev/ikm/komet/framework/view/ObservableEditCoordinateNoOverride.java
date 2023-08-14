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

import javafx.beans.value.ObservableValue;
import dev.ikm.tinkar.coordinate.edit.EditCoordinate;
import dev.ikm.tinkar.coordinate.edit.EditCoordinateRecord;
import dev.ikm.tinkar.terms.ConceptFacade;
import dev.ikm.tinkar.terms.TinkarTerm;

//~--- classes ----------------------------------------------------------------

/**
 * The Class ObservableEditCoordinateImpl.
 *
 * 
 */
public class ObservableEditCoordinateNoOverride
        extends ObservableEditCoordinateBase {

    //~--- constructors --------------------------------------------------------

    /**
     * Instantiates a new observable edit coordinate impl.
     *
     * @param editCoordinate the edit coordinate
     */
    public ObservableEditCoordinateNoOverride(EditCoordinate editCoordinate, String coordinateName) {
        super(editCoordinate.toEditCoordinateRecord(), coordinateName);
    }

    public ObservableEditCoordinateNoOverride(EditCoordinate editCoordinate) {
        super(editCoordinate.toEditCoordinateRecord(), "Edit coordinate");
    }

    @Override
    protected EditCoordinateRecord baseCoordinateChangedListenersRemoved(ObservableValue<? extends EditCoordinateRecord> observable, EditCoordinateRecord oldValue, EditCoordinateRecord newValue) {
        this.authorForChangesProperty().setValue(newValue.getAuthorForChanges());
        this.defaultModuleProperty().setValue(newValue.getDefaultModule());
        this.destinationModuleProperty().setValue(newValue.getDestinationModule());
        this.promotionPathProperty().setValue(newValue.getPromotionPath());
        return newValue;
    }

    @Override
    public void setExceptOverrides(EditCoordinateRecord updatedCoordinate) {
        setValue(updatedCoordinate);
    }

    @Override
    public EditCoordinateRecord getOriginalValue() {
        return getValue();
    }

    @Override
    protected SimpleEqualityBasedObjectProperty<ConceptFacade> makeAuthorForChangesProperty(EditCoordinate editCoordinate) {
        return new SimpleEqualityBasedObjectProperty<>(this,
                TinkarTerm.AUTHOR_FOR_EDIT_COORDINATE.toXmlFragment(),
                editCoordinate.getAuthorForChanges());
    }

    @Override
    protected SimpleEqualityBasedObjectProperty<ConceptFacade> makeDefaultModuleProperty(EditCoordinate editCoordinate) {
        return new SimpleEqualityBasedObjectProperty(this,
                TinkarTerm.DEFAULT_MODULE_FOR_EDIT_COORDINATE.toXmlFragment(),
                editCoordinate.getDefaultModule());
    }

    @Override
    protected SimpleEqualityBasedObjectProperty<ConceptFacade> makeDestinationModuleProperty(EditCoordinate editCoordinate) {
        return new SimpleEqualityBasedObjectProperty<>(this,
                TinkarTerm.DESTINATION_MODULE_FOR_EDIT_COORDINATE.toXmlFragment(),
                editCoordinate.getDestinationModule());
    }

    @Override
    protected SimpleEqualityBasedObjectProperty<ConceptFacade> makeDefaultPathProperty(EditCoordinate editCoordinate) {
        return new SimpleEqualityBasedObjectProperty<>(this,
                //TODO make concept for PATH_FOR_EDIT_COORDINATE
                TinkarTerm.PATH_FOR_PATH_COORDINATE.toXmlFragment(),
                editCoordinate.getAuthorForChanges());
    }

    @Override
    protected SimpleEqualityBasedObjectProperty<ConceptFacade> makePromotionPathProperty(EditCoordinate editCoordinate) {
        return new SimpleEqualityBasedObjectProperty(this,
                TinkarTerm.PROMOTION_PATH_FOR_EDIT_CORDINATE.toXmlFragment(),
                editCoordinate.getPromotionPath());
    }
}

