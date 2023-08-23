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

import dev.ikm.komet.framework.panel.axiom.ConcreteDomainOperators;
import dev.ikm.tinkar.coordinate.view.calculator.ViewCalculator;
import javafx.application.Platform;
import javafx.scene.control.Menu;
import javafx.scene.control.MenuItem;
import javafx.scene.text.Text;

import java.util.function.Consumer;

public class ChooseConcreteOperatorMenu extends Menu {
    final ViewCalculator viewCalculator;
    final Consumer<Object> chosenConceptConsumer;

    public ChooseConcreteOperatorMenu(String s, ViewCalculator viewCalculator,
                             Consumer<Object> chosenConceptConsumer) {
        super(s);
        this.viewCalculator = viewCalculator;
        this.chosenConceptConsumer = chosenConceptConsumer;
        Platform.runLater(() -> {
            for (ConcreteDomainOperators operator: ConcreteDomainOperators.values()) {
                Text symbol = new Text(operator.symbol);
                MenuItem selectOperatorMenuItem =
                        new MenuItem(this.viewCalculator.getPreferredDescriptionTextWithFallbackOrNid(operator.conceptRepresentation),
                                symbol);
                selectOperatorMenuItem.setOnAction(event -> {
                    chosenConceptConsumer.accept(operator.conceptRepresentation);
                });
                this.getItems().add(selectOperatorMenuItem);
            }
            this.getItems().sort((o1, o2) -> o1.getText().compareTo(o2.getText()));
        });
    }
}
