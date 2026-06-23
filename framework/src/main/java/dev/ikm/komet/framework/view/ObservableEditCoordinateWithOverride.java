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
        // Depth-independent override nesting (ike-issues#663): an override may wrap another override.

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
            this.defaultPathProperty().setValue(newValue.getDefaultPath());
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

    /**
     * Applies {@code coordinateWithOverrides} as this coordinate's override state: each dimension is
     * {@link OverrideOf#set set}, which pins it when the value differs from the inherited parent and clears
     * the pin (reverting to inheriting) when it equals the parent. Dimensions matching the parent stay
     * inherited, so cascade tracking is preserved.
     *
     * @param coordinateWithOverrides the desired resolved edit coordinate
     */
    public void setOverrides(EditCoordinateRecord coordinateWithOverrides) {
        authorForChangesProperty().setValue(coordinateWithOverrides.getAuthorForChanges());
        defaultModuleProperty().setValue(coordinateWithOverrides.getDefaultModule());
        destinationModuleProperty().setValue(coordinateWithOverrides.getDestinationModule());
        defaultPathProperty().setValue(coordinateWithOverrides.getDefaultPath());
        promotionPathProperty().setValue(coordinateWithOverrides.getPromotionPath());
    }

    /**
     * Re-pins only the edit dimensions that genuinely differ between {@code resolved} (the captured override)
     * and {@code baseline} (the inherited parent at capture time), leaving every matching dimension inherited
     * so it keeps tracking the current parent. The delta-aware inverse of {@link #setOverrides}
     * (IKE-Network/ike-issues#745).
     *
     * @param resolved the captured resolved edit coordinate
     * @param baseline the inherited parent edit coordinate at capture time
     */
    public void setOverridesFromDelta(EditCoordinateRecord resolved, EditCoordinateRecord baseline) {
        if (resolved.getAuthorNidForChanges() != baseline.getAuthorNidForChanges()) {
            authorForChangesProperty().setValue(resolved.getAuthorForChanges());
        }
        if (resolved.getDefaultModuleNid() != baseline.getDefaultModuleNid()) {
            defaultModuleProperty().setValue(resolved.getDefaultModule());
        }
        if (resolved.getDestinationModuleNid() != baseline.getDestinationModuleNid()) {
            destinationModuleProperty().setValue(resolved.getDestinationModule());
        }
        if (resolved.getDefaultPathNid() != baseline.getDefaultPathNid()) {
            defaultPathProperty().setValue(resolved.getDefaultPath());
        }
        if (resolved.getPromotionPathNid() != baseline.getPromotionPathNid()) {
            promotionPathProperty().setValue(resolved.getPromotionPath());
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
        return new OverrideOf<>(observableEditCoordinate.authorForChangesProperty(), this);
    }

    @Override
    protected SimpleEqualityBasedObjectProperty<ConceptFacade> makeDefaultModuleProperty(EditCoordinate editCoordinate) {
        ObservableEditCoordinate observableEditCoordinate = (ObservableEditCoordinate) editCoordinate;
        return new OverrideOf<>(observableEditCoordinate.defaultModuleProperty(), this);
    }

    @Override
    protected SimpleEqualityBasedObjectProperty<ConceptFacade> makeDestinationModuleProperty(EditCoordinate editCoordinate) {
        ObservableEditCoordinate observableEditCoordinate = (ObservableEditCoordinate) editCoordinate;
        return new OverrideOf<>(observableEditCoordinate.destinationModuleProperty(), this);
    }

    @Override
    protected SimpleEqualityBasedObjectProperty<ConceptFacade> makeDefaultPathProperty(EditCoordinate editCoordinate) {
        ObservableEditCoordinate observableEditCoordinate = (ObservableEditCoordinate) editCoordinate;
        return new OverrideOf<>(observableEditCoordinate.defaultPathProperty(), this);
    }

    @Override
    protected SimpleEqualityBasedObjectProperty<ConceptFacade> makePromotionPathProperty(EditCoordinate editCoordinate) {
        ObservableEditCoordinate observableEditCoordinate = (ObservableEditCoordinate) editCoordinate;
        return new OverrideOf<>(observableEditCoordinate.promotionPathProperty(), this);
    }

    @Override
    public OverrideOf<ConceptFacade> authorForChangesProperty() {
        return (OverrideOf<ConceptFacade>) super.authorForChangesProperty();
    }

    @Override
    public OverrideOf<ConceptFacade> defaultModuleProperty() {
        return (OverrideOf<ConceptFacade>) super.defaultModuleProperty();
    }

    @Override
    public OverrideOf<ConceptFacade> destinationModuleProperty() {
        return (OverrideOf<ConceptFacade>) super.destinationModuleProperty();
    }

    @Override
    public OverrideOf<ConceptFacade> defaultPathProperty() {
        return (OverrideOf<ConceptFacade>) super.defaultPathProperty();
    }

    @Override
    public OverrideOf<ConceptFacade> promotionPathProperty() {
        return (OverrideOf<ConceptFacade>) super.promotionPathProperty();
    }
}

