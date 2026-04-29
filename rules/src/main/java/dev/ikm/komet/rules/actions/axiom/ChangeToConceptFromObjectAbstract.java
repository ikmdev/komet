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
import dev.ikm.tinkar.terms.EntityFacade;
import dev.ikm.tinkar.terms.SemanticFacade;
import javafx.event.ActionEvent;

public abstract class ChangeToConceptFromObjectAbstract extends AbstractAxiomAction {

    ConceptFacade conceptFacade;

    @SuppressWarnings("removal")
    public ChangeToConceptFromObjectAbstract(String text, Object object, AxiomSubjectRecord axiomSubjectRecord, ViewCalculator viewCalculator, EditCoordinate editCoordinate) {
        super(text, axiomSubjectRecord, viewCalculator, editCoordinate);
        switch (object) {
            case EntityFacade entityFacade -> {
                if (EntityHandle.get(entityFacade).isConcept()) {
                    this.conceptFacade = EntityHandle.get(entityFacade).expectConcept();
                } else {
                    this.conceptFacade = EntityHandle.get(EntityHandle.get(entityFacade).expectSemantic().nid()).expectConcept();
                }
            }

            case EntityVersion entityVersion -> {
                if (EntityHandle.get(entityVersion.nid()).isConcept()) {
                    this.conceptFacade = EntityHandle.get(entityVersion.nid()).expectConcept();
                } else {
                    this.conceptFacade = EntityHandle.get(EntityHandle.get(entityVersion.nid()).expectSemantic().nid()).expectConcept();
                }
            }
            case SearchPanelController.NidTextRecord nidTextRecord -> {

                EntityHandle.get(nidTextRecord.nid()).ifPresent((Entity<?> entity) -> {
                    switch (entity) {
                        case ConceptEntity<?> c -> this.conceptFacade = c;
                        case SemanticEntity<?> s when EntityHandle.get(s.referencedComponentNid()).isConcept() ->
                            this.conceptFacade = EntityHandle.get(s.referencedComponentNid()).expectConcept();
                        case SemanticEntity<?> s -> throw new IllegalStateException("Expecting semantic pointing to a concept. Found: " + entity);
                        default ->  throw new IllegalStateException("Expecting a concept. Found: " + entity);
                     }
                });
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
