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

import dev.ikm.komet.framework.rulebase.GeneratedActionSuggested;
import dev.ikm.tinkar.coordinate.edit.EditCoordinate;
import dev.ikm.tinkar.coordinate.view.calculator.ViewCalculator;

public abstract non-sealed class AbstractActionSuggested extends AbstractActionGenerated implements GeneratedActionSuggested {

    public AbstractActionSuggested(String text, ViewCalculator viewCalculator, EditCoordinate editCoordinate) {
        super(text, viewCalculator, editCoordinate);
    }
}
