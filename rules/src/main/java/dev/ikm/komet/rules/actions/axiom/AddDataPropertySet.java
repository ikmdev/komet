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

import java.util.UUID;

import dev.ikm.elk.snomed.SnomedIds;
import dev.ikm.komet.framework.panel.axiom.AxiomSubjectRecord;
import dev.ikm.tinkar.common.service.PrimitiveData;
import dev.ikm.tinkar.common.util.uuid.UuidUtil;
import dev.ikm.tinkar.coordinate.edit.EditCoordinate;
import dev.ikm.tinkar.coordinate.edit.EditCoordinateRecord;
import dev.ikm.tinkar.coordinate.view.calculator.ViewCalculator;
import dev.ikm.tinkar.entity.graph.adaptor.axiom.LogicalExpressionBuilder;
import javafx.event.ActionEvent;

public class AddDataPropertySet extends AbstractAxiomAction {

	public AddDataPropertySet(String text, AxiomSubjectRecord axiomSubjectRecord, ViewCalculator viewCalculator,
			EditCoordinate editCoordinate) {
		super(text, axiomSubjectRecord, viewCalculator, editCoordinate);
	}

	@Override
	public void doAction(ActionEvent t, AxiomSubjectRecord axiomSubjectRecord, EditCoordinateRecord editCoordinate) {
		LogicalExpressionBuilder leb = new LogicalExpressionBuilder(axiomSubjectRecord.axiomTree());
		UUID uuid = UuidUtil.fromSNOMED("" + SnomedIds.concept_model_data_attribute);
		int nid = PrimitiveData.nid(uuid);
		leb.DataPropertySet(leb.And(leb.ConceptAxiom(nid)));
		putUpdatedLogicalExpression(editCoordinate, leb.build());
	}
}
