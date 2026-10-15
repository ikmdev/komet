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
package dev.ikm.komet.actions;

import dev.ikm.tinkar.coordinate.edit.EditCoordinate;
import dev.ikm.tinkar.coordinate.view.calculator.ViewCalculator;
import dev.ikm.tinkar.terms.EntityFacade;

import java.util.Objects;

/**
 * An action a rule offers in a context, with the inputs the rule can already fill: here, its
 * subject. Offers stay in memory, since rules fire on every change of focus; a run is recorded
 * only when a person or the assistant performs one. A menu item and an assistant tool are two
 * presentations of the same offer.
 *
 * @param action  the action offered
 * @param subject what it would be performed on
 */
public record KometActionOffer(KometAction action, EntityFacade subject) {

    public KometActionOffer {
        Objects.requireNonNull(action, "action");
        Objects.requireNonNull(subject, "subject");
    }

    /**
     * Performs the offer: a new input for its action and subject, then one run.
     *
     * @param view the view the subject is read in
     * @param edit the edit coordinate a change is written under
     * @return what the run produced
     */
    public KometActionResult perform(ViewCalculator view, EditCoordinate edit) {
        return action.perform(KometActionInput.of(action.concept(), subject), view, edit);
    }
}
