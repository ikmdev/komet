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
package dev.ikm.komet.framework.rulebase;

import dev.ikm.tinkar.common.util.text.NaturalOrder;
import javafx.scene.control.Menu;
import org.controlsfx.control.action.Action;

import java.util.UUID;

public record ConsequenceMenu(UUID consequenceUUID,
                              String ruleMethod,
                              Menu generatedMenu) implements Consequence<Menu> {
    @Override
    public Menu get() {
        return generatedMenu;
    }

    @Override
    public int compareTo(Consequence o) {
        String thisCompareString = generatedMenu.getText();
        String thatCompareString = switch (o.get()) {
            case Action action -> action.getText();
            case Menu menu -> menu.getText();
            default -> o.get().toString();
        };
        return NaturalOrder.compareStrings(thisCompareString, thatCompareString);
    }
 }
