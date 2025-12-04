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
import dev.ikm.tinkar.entity.graph.adaptor.axiom.LogicalAxiom;
import dev.ikm.tinkar.entity.graph.adaptor.axiom.LogicalExpressionBuilder;
import dev.ikm.tinkar.coordinate.edit.EditCoordinate;
import dev.ikm.tinkar.coordinate.edit.EditCoordinateRecord;
import dev.ikm.tinkar.coordinate.view.calculator.ViewCalculator;
import dev.ikm.tinkar.terms.ConceptFacade;
import javafx.event.ActionEvent;

public class ChangeFeatureType extends ChangeToConceptFromObjectAbstract {

    public ChangeFeatureType(String text, Object o, AxiomSubjectRecord axiomSubjectRecord, ViewCalculator viewCalculator, EditCoordinate editCoordinate) {
        super(text, o, axiomSubjectRecord, viewCalculator, editCoordinate);
    }

    @Override
    public void doAction(ActionEvent t, ConceptFacade conceptToChangeTo, AxiomSubjectRecord axiomSubjectRecord, EditCoordinateRecord editCoordinate) {
        LogicalExpressionBuilder leb = new LogicalExpressionBuilder(axiomSubjectRecord.axiomTree());
        switch (leb.get(axiomSubjectRecord.axiomIndex())) {
            case LogicalAxiom.Atom.TypedAtom.Feature featureAxiom -> {
                leb.updateFeatureType(featureAxiom, conceptToChangeTo);
            }
            default -> throw new IllegalStateException("Unexpected value: " + leb.get(axiomSubjectRecord.axiomIndex()));
        }
        putUpdatedLogicalExpression(editCoordinate, leb.build());
    }
}
