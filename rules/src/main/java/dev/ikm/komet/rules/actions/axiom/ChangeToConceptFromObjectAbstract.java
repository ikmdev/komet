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
package dev.ikm.komet.rules.actions.axiom;

import dev.ikm.komet.framework.panel.axiom.AxiomSubjectRecord;
import dev.ikm.komet.framework.search.SearchPanelController;
import dev.ikm.tinkar.coordinate.edit.EditCoordinate;
import dev.ikm.tinkar.coordinate.edit.EditCoordinateRecord;
import dev.ikm.tinkar.coordinate.view.calculator.ViewCalculator;
import dev.ikm.tinkar.entity.*;
import dev.ikm.tinkar.terms.ConceptFacade;
import dev.ikm.tinkar.terms.SemanticFacade;
import javafx.event.ActionEvent;

public abstract class ChangeToConceptFromObjectAbstract extends AbstractAxiomAction {

    final ConceptFacade conceptFacade;

    public ChangeToConceptFromObjectAbstract(String text, Object object, AxiomSubjectRecord axiomSubjectRecord, ViewCalculator viewCalculator, EditCoordinate editCoordinate) {
        super(text, axiomSubjectRecord, viewCalculator, editCoordinate);
        switch (object) {
            case ConceptFacade conceptFacade -> this.conceptFacade = conceptFacade;
            case ConceptEntityVersion conceptEntityVersion -> this.conceptFacade = EntityService.get().getEntityFast(conceptEntityVersion.nid());
            case SemanticFacade semanticFacade -> {
                SemanticEntity semantic = EntityService.get().getEntityFast(semanticFacade);
                this.conceptFacade = (ConceptFacade) semantic.topEnclosingComponent();
            }
            case SemanticEntityVersion semanticVersion -> {
                SemanticEntity semantic = EntityService.get().getEntityFast(semanticVersion.nid());
                this.conceptFacade = (ConceptFacade) semantic.topEnclosingComponent();
            }
            case SearchPanelController.NidTextRecord nidTextRecord -> {
                switch (Entity.getFast(nidTextRecord.nid())) {
                    case ConceptEntity conceptEntity -> this.conceptFacade = conceptEntity;
                    case SemanticEntity semanticFacade -> this.conceptFacade = (ConceptFacade) semanticFacade.topEnclosingComponent();
                    case null -> throw new IllegalStateException("Null object provided");
                    default -> throw new IllegalStateException("Entity is not a concept: " + object);
                }
            }
            case null -> throw new IllegalStateException("Null object provided");
            default -> throw new IllegalStateException("Entity is not a concept: " + object);
        }
    }

    @Override
    public final void doAction(ActionEvent t, AxiomSubjectRecord axiomSubjectRecord, EditCoordinateRecord editCoordinate) {
        doAction(t, conceptFacade, axiomSubjectRecord, editCoordinate);
    }

    public abstract void doAction(ActionEvent t, ConceptFacade conceptToChangeTo, AxiomSubjectRecord axiomSubjectRecord, EditCoordinateRecord editCoordinate);

    public void doAction() {
        doAction(new ActionEvent());
    }

}
