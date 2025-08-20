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


import dev.ikm.tinkar.coordinate.edit.EditCoordinate;
import dev.ikm.tinkar.coordinate.edit.EditCoordinateRecord;
import dev.ikm.tinkar.terms.ConceptFacade;
import javafx.beans.value.ObservableValue;

public class ObservableEditCoordinateWithOverride
        extends ObservableEditCoordinateBase {

    //~--- constructors --------------------------------------------------------

    public ObservableEditCoordinateWithOverride(ObservableEditCoordinate editCoordinate) {
        this(editCoordinate, editCoordinate.getName());
    }

    /**
     * Instantiates a new observable edit coordinate impl.
     *
     * @param editCoordinate the edit coordinate
     */
    public ObservableEditCoordinateWithOverride(ObservableEditCoordinate editCoordinate, String coordinateName) {
        super(editCoordinate, coordinateName);
        if (editCoordinate instanceof ObservableEditCoordinateWithOverride) {
            throw new IllegalStateException("Cannot override an overridden Coordinate. ");
        }

    }

    @Override
    protected EditCoordinateRecord baseCoordinateChangedListenersRemoved(ObservableValue<? extends EditCoordinateRecord> observable, EditCoordinateRecord oldValue, EditCoordinateRecord newValue) {
        if (!this.authorForChangesProperty().isOverridden()) {
            this.authorForChangesProperty().setValue(newValue.getAuthorForChanges());
        }
        if (!this.defaultModuleProperty().isOverridden()) {
            this.defaultModuleProperty().setValue(newValue.getDefaultModule());
        }
        if (!this.destinationModuleProperty().isOverridden()) {
            this.destinationModuleProperty().setValue(newValue.getDestinationModule());
        }
        if (!this.defaultPathProperty().isOverridden()) {
            this.defaultPathProperty().setValue(newValue.getPromotionPath());
        }
        if (!this.promotionPathProperty().isOverridden()) {
            this.promotionPathProperty().setValue(newValue.getPromotionPath());
        }
        /*
int authorNid, int defaultModuleNid, int promotionPathNid, int destinationModuleNid
         */
        return EditCoordinateRecord.make(this.authorForChangesProperty().get().nid(),
                this.defaultModuleProperty().get().nid(),
                this.destinationModuleProperty().get().nid(),
                this.defaultPathProperty().get().nid(),
                this.promotionPathProperty().get().nid()
        );
    }

    @Override
    public void setExceptOverrides(EditCoordinateRecord updatedCoordinate) {
        if (hasOverrides()) {
            ConceptFacade author = updatedCoordinate.getAuthorForChanges();
            if (authorForChangesProperty().isOverridden()) {
                author = authorForChangesProperty().get();
            }

            ConceptFacade defaultModule = updatedCoordinate.getDefaultModule();
            if (defaultModuleProperty().isOverridden()) {
                defaultModule = defaultModuleProperty().get();
            }

            ConceptFacade defaultPath = updatedCoordinate.getDefaultPath();
            if (defaultPathProperty().isOverridden()) {
                defaultPath = promotionPathProperty().get();
            }

            ConceptFacade promotionPath = updatedCoordinate.getPromotionPath();
            if (promotionPathProperty().isOverridden()) {
                promotionPath = promotionPathProperty().get();
            }

            ConceptFacade destinationModule = updatedCoordinate.getDestinationModule();
            if (destinationModuleProperty().isOverridden()) {
                destinationModule = destinationModuleProperty().get();
            }

            setValue(EditCoordinateRecord.make(author, defaultModule, destinationModule, defaultPath, promotionPath));
        } else {
            setValue(updatedCoordinate);
        }
    }

    @Override
    public EditCoordinateRecord getOriginalValue() {
        return EditCoordinateRecord.make(authorForChangesProperty().getOriginalValue(),
                defaultModuleProperty().getOriginalValue(),
                destinationModuleProperty().getOriginalValue(),
                defaultPathProperty().getOriginalValue(),
                promotionPathProperty().getOriginalValue()
        );
    }

    @Override
    protected SimpleEqualityBasedObjectProperty<ConceptFacade> makeAuthorForChangesProperty(EditCoordinate editCoordinate) {
        ObservableEditCoordinate observableEditCoordinate = (ObservableEditCoordinate) editCoordinate;
        return new ObjectPropertyWithOverride<>(observableEditCoordinate.authorForChangesProperty(), this);
    }

    @Override
    protected SimpleEqualityBasedObjectProperty<ConceptFacade> makeDefaultModuleProperty(EditCoordinate editCoordinate) {
        ObservableEditCoordinate observableEditCoordinate = (ObservableEditCoordinate) editCoordinate;
        return new ObjectPropertyWithOverride<>(observableEditCoordinate.defaultModuleProperty(), this);
    }

    @Override
    protected SimpleEqualityBasedObjectProperty<ConceptFacade> makeDestinationModuleProperty(EditCoordinate editCoordinate) {
        ObservableEditCoordinate observableEditCoordinate = (ObservableEditCoordinate) editCoordinate;
        return new ObjectPropertyWithOverride<>(observableEditCoordinate.destinationModuleProperty(), this);
    }

    @Override
    protected SimpleEqualityBasedObjectProperty<ConceptFacade> makeDefaultPathProperty(EditCoordinate editCoordinate) {
        ObservableEditCoordinate observableEditCoordinate = (ObservableEditCoordinate) editCoordinate;
        return new ObjectPropertyWithOverride<>(observableEditCoordinate.defaultPathProperty(), this);
    }

    @Override
    protected SimpleEqualityBasedObjectProperty<ConceptFacade> makePromotionPathProperty(EditCoordinate editCoordinate) {
        ObservableEditCoordinate observableEditCoordinate = (ObservableEditCoordinate) editCoordinate;
        return new ObjectPropertyWithOverride<>(observableEditCoordinate.promotionPathProperty(), this);
    }

    @Override
    public ObjectPropertyWithOverride<ConceptFacade> authorForChangesProperty() {
        return (ObjectPropertyWithOverride<ConceptFacade>) super.authorForChangesProperty();
    }

    @Override
    public ObjectPropertyWithOverride<ConceptFacade> defaultModuleProperty() {
        return (ObjectPropertyWithOverride<ConceptFacade>) super.defaultModuleProperty();
    }

    @Override
    public ObjectPropertyWithOverride<ConceptFacade> destinationModuleProperty() {
        return (ObjectPropertyWithOverride<ConceptFacade>) super.destinationModuleProperty();
    }

    @Override
    public ObjectPropertyWithOverride<ConceptFacade> defaultPathProperty() {
        return (ObjectPropertyWithOverride<ConceptFacade>) super.defaultPathProperty();
    }

    @Override
    public ObjectPropertyWithOverride<ConceptFacade> promotionPathProperty() {
        return (ObjectPropertyWithOverride<ConceptFacade>) super.promotionPathProperty();
    }
}

