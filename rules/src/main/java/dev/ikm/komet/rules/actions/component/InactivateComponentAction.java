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
package dev.ikm.komet.rules.actions.component;

import dev.ikm.komet.rules.actions.AbstractActionSuggested;
import javafx.event.ActionEvent;
import dev.ikm.tinkar.coordinate.edit.EditCoordinate;
import dev.ikm.tinkar.coordinate.edit.EditCoordinateRecord;
import dev.ikm.tinkar.coordinate.view.calculator.ViewCalculator;
import dev.ikm.tinkar.entity.EntityVersion;

public class InactivateComponentAction extends AbstractActionSuggested {

    final EntityVersion entityVersion;

    public InactivateComponentAction(EntityVersion entityVersion, ViewCalculator viewCalculator, EditCoordinate editCoordinate) {
        super("Inactivate component", viewCalculator, editCoordinate);
        this.entityVersion = entityVersion;
        this.setLongText("An action that will inactivate a component, leaving component in an uncommitted state");
    }

    public final void doAction(ActionEvent actionEvent, EditCoordinateRecord editCoordinate) {
        throw new UnsupportedOperationException();
    }

}
