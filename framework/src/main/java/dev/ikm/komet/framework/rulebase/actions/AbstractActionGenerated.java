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
package dev.ikm.komet.framework.rulebase.actions;

import javafx.event.ActionEvent;
import org.controlsfx.control.action.Action;
import dev.ikm.komet.framework.Dialogs;
import dev.ikm.komet.framework.rulebase.GeneratedAction;
import dev.ikm.tinkar.coordinate.edit.EditCoordinate;
import dev.ikm.tinkar.coordinate.edit.EditCoordinateRecord;
import dev.ikm.tinkar.coordinate.view.calculator.ViewCalculator;

public sealed abstract class AbstractActionGenerated
        extends Action
        implements GeneratedAction
        permits AbstractActionImmediate, AbstractActionSuggested {

    protected final ViewCalculator viewCalculator;
    private final EditCoordinate editCoordinate;

    public AbstractActionGenerated(String text, ViewCalculator viewCalculator, EditCoordinate editCoordinate) {
        super(text);
        this.viewCalculator = viewCalculator;
        this.editCoordinate = editCoordinate;
        setEventHandler(this::doAction);
    }

    public final void doAction(ActionEvent actionEvent) {
        try {
            doAction(actionEvent, editCoordinate.toEditCoordinateRecord());
        } catch (Throwable ex) {
            Dialogs.showErrorDialog("Error executing " + getText(), ex.getMessage(), ex);
        }
    }

    public abstract void doAction(ActionEvent t, EditCoordinateRecord editCoordinate);

    public ViewCalculator viewCalculator() {
        return viewCalculator;
    }

}
