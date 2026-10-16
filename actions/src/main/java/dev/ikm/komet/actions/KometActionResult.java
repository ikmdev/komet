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

import dev.ikm.komet.terms.KometConcept;
import dev.ikm.tinkar.common.id.PublicId;
import dev.ikm.tinkar.terms.ConceptFacade;
import dev.ikm.tinkar.terms.EntityFacade;

import java.util.Objects;
import java.util.Optional;

/**
 * What one run of an action produced: the shape of a semantic of the {@code Version change result
 * pattern}, whose referenced component is the run's input. The outcome is
 * {@link KometConcept#ACTION_COMPLETED}, {@link KometConcept#ACTION_REFUSED} (nothing was written)
 * or {@link KometConcept#ACTION_FAILED} (nothing it began was committed).
 *
 * @param input          the public id of the run's input
 * @param outcome        whether the run did what it was asked
 * @param writtenVersion the STAMP of the version the run wrote; empty when it wrote none
 * @param message        text for the person or the model about the outcome; empty when there is
 *                       nothing to add
 */
public record KometActionResult(PublicId input, ConceptFacade outcome, Optional<EntityFacade> writtenVersion,
                                String message) {

    public KometActionResult {
        Objects.requireNonNull(input, "input");
        Objects.requireNonNull(outcome, "outcome");
        Objects.requireNonNull(writtenVersion, "writtenVersion");
        Objects.requireNonNull(message, "message");
    }

    /**
     * @param input   the run's input
     * @param stamp   the STAMP of the version written
     * @param message what to tell the person
     * @return a completed run that wrote a version
     */
    public static KometActionResult completed(KometActionInput input, EntityFacade stamp, String message) {
        return new KometActionResult(input.id(), KometConcept.ACTION_COMPLETED, Optional.of(stamp), message);
    }

    /**
     * @param input  the run's input
     * @param reason why the subject or inputs did not allow the action
     * @return a refused run: nothing was written
     */
    public static KometActionResult refused(KometActionInput input, String reason) {
        return new KometActionResult(input.id(), KometConcept.ACTION_REFUSED, Optional.empty(), reason);
    }

    /**
     * @param input  the run's input
     * @param reason why the run could not finish
     * @return a failed run: nothing it began was committed
     */
    public static KometActionResult failed(KometActionInput input, String reason) {
        return new KometActionResult(input.id(), KometConcept.ACTION_FAILED, Optional.empty(), reason);
    }

    /**
     * @return whether the run did what it was asked
     */
    public boolean completed() {
        return EntityFacade.same(outcome, KometConcept.ACTION_COMPLETED);
    }
}
