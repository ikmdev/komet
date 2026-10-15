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
import dev.ikm.tinkar.terms.ConceptFacade;

/**
 * One kind of action Komet performs, bound to the action concept it implements: the part of an
 * action that does the work, apart from any surface (ike-lab-documents topic
 * {@code dev-komet-actions}). A menu adapter, a tool adapter and the headless runner all perform
 * it through {@link #perform}.
 *
 * <p>Nothing from JavaFX enters an implementation, and a failure is a result, never an exception:
 * {@link KometActionResult#refused} when the subject or inputs do not allow the action, nothing
 * written; {@link KometActionResult#failed} when it started and could not finish. A change action
 * writes under the edit coordinate and leaves what it wrote uncommitted, for the person to commit
 * with their other edits.
 *
 * <p>Implementations are found through {@link KometActions}; a plugin contributes one with
 * {@code provides dev.ikm.komet.actions.KometAction with ...} and a public no-argument constructor.
 */
public interface KometAction {

    /**
     * @return the action concept this implements, a concept beneath {@code Komet action}
     */
    ConceptFacade concept();

    /**
     * Performs one run. Implementations are stateless and safe to call from any thread but the
     * JavaFX application thread.
     *
     * @param input the run's input: this action, its subject, and any further fields
     * @param view  the view the subject is read in
     * @param edit  the edit coordinate a change is written under
     * @return what the run produced
     */
    KometActionResult perform(KometActionInput input, ViewCalculator view, EditCoordinate edit);
}
