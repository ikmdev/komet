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

import java.util.Optional;

import dev.ikm.elk.snomed.interval.Interval;
import dev.ikm.komet.framework.panel.axiom.AxiomSubjectRecord;
import dev.ikm.tinkar.coordinate.edit.EditCoordinate;
import dev.ikm.tinkar.coordinate.edit.EditCoordinateRecord;
import dev.ikm.tinkar.coordinate.view.calculator.ViewCalculator;
import dev.ikm.tinkar.entity.graph.adaptor.axiom.LogicalAxiom;
import dev.ikm.tinkar.entity.graph.adaptor.axiom.LogicalExpressionBuilder;
import javafx.event.ActionEvent;
import javafx.scene.control.Alert;
import javafx.scene.control.Alert.AlertType;
import javafx.scene.control.TextInputDialog;

public class ChangeIntervalBound extends AbstractAxiomAction {

	private final Interval interval;
	private final boolean lower;

	public ChangeIntervalBound(Interval interval, boolean lower, String text, AxiomSubjectRecord axiomSubjectRecord,
			ViewCalculator viewCalculator, EditCoordinate editCoordinate) {
		super(text, axiomSubjectRecord, viewCalculator, editCoordinate);
		this.interval = interval;
		this.lower = lower;
	}

	public void doAction() {
		doAction(new ActionEvent());
	}

	@Override
	public void doAction(ActionEvent t, AxiomSubjectRecord axiomSubjectRecord, EditCoordinateRecord editCoordinate) {
		TextInputDialog dialog = new TextInputDialog(
				"" + (lower ? interval.getLowerBound() : interval.getUpperBound()));
		dialog.setTitle("Update Interval");
		dialog.setHeaderText(null);
		dialog.setContentText((lower ? "Lower" : "Upper") + " bound: ");
		Optional<String> result = dialog.showAndWait();
		if (!result.isPresent())
			return;
		try {
			int value = Integer.parseInt(result.get());
			if (lower) {
				interval.setLowerBound(value);
			} else {
				interval.setUpperBound(value);
			}
		} catch (NumberFormatException ex) {
			new Alert(AlertType.ERROR, "Enter an integer").showAndWait();
			return;
		}
		LogicalExpressionBuilder leb = new LogicalExpressionBuilder(axiomSubjectRecord.axiomTree());
		switch (leb.get(axiomSubjectRecord.axiomIndex())) {
		case LogicalAxiom.Atom.TypedAtom.IntervalRole roleAxiom -> {
			leb.updateIntervalRoleValue(roleAxiom, interval.getLowerBound(), interval.isLowerOpen(),
					interval.getUpperBound(), interval.isUpperOpen());
		}
		default -> throw new IllegalStateException("Unexpected value: " + leb.get(axiomSubjectRecord.axiomIndex()));
		}
		putUpdatedLogicalExpression(editCoordinate, leb.build());
	}

}
