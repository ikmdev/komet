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
package dev.ikm.komet.rules.actions.membership;

import dev.ikm.tinkar.coordinate.stamp.calculator.Latest;
import dev.ikm.tinkar.coordinate.view.calculator.ViewCalculator;
import dev.ikm.tinkar.entity.ConceptEntityVersion;
import dev.ikm.tinkar.entity.SemanticEntity;
import dev.ikm.tinkar.entity.SemanticEntityVersion;

/**
 * The membership actions leave their version uncommitted, for the person to publish or discard, and
 * refuse a membership semantic whose latest version is itself uncommitted: a second uncommitted
 * version would not supersede the first, and committing it would settle someone's pending work
 * unasked (IKE-Network/ike-issues#1308).
 */
final class PendingMembership {

    private PendingMembership() {
    }

    static void refuseIfPending(SemanticEntity<SemanticEntityVersion> membership, ConceptEntityVersion concept,
                                String model, ViewCalculator viewCalculator) {
        Latest<SemanticEntityVersion> latest = viewCalculator.latest(membership);
        if (latest.isPresent() && latest.get().uncommitted()) {
            throw new IllegalStateException(viewCalculator.getDescriptionTextOrNid(concept.nid())
                    + " has an uncommitted change to its membership in the " + model
                    + "; publish or discard it first.");
        }
    }

    static String transactionName(String actionText, ConceptEntityVersion concept, ViewCalculator viewCalculator) {
        return actionText + ": " + viewCalculator.getDescriptionTextOrNid(concept.nid());
    }
}
