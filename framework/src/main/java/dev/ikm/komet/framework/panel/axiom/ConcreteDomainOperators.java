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
package dev.ikm.komet.framework.panel.axiom;

import dev.ikm.tinkar.common.service.PrimitiveData;
import dev.ikm.tinkar.terms.ConceptFacade;
import dev.ikm.tinkar.terms.TinkarTerm;

public enum ConcreteDomainOperators {
    /**
     * The equals.
     */
    EQUALS("=", TinkarTerm.EQUAL_TO),

    /**
     * The less than.
     */
    LESS_THAN("<", TinkarTerm.LESS_THAN),

    /**
     * The less than equals.
     */
    LESS_THAN_EQUALS("≤", TinkarTerm.LESS_THAN_OR_EQUAL_TO),

    /**
     * The greater than.
     */
    GREATER_THAN(">", TinkarTerm.GREATER_THAN),

    /**
     * The greater than equals.
     */
    GREATER_THAN_EQUALS("≥", TinkarTerm.GREATER_THAN_OR_EQUAL_TO);

    public final ConceptFacade conceptRepresentation;

    public final String symbol;

    ConcreteDomainOperators(String symbol, ConceptFacade conceptRepresentation) {
        this.symbol = symbol;
        this.conceptRepresentation = conceptRepresentation;
    }


    @Override
    public String toString() {
        return symbol;
    }

    public static ConcreteDomainOperators fromConcept(ConceptFacade conceptRepresentation) {
        for (ConcreteDomainOperators operator: ConcreteDomainOperators.values()) {
            if (operator.conceptRepresentation.nid() == conceptRepresentation.nid()) {
                return operator;
            }
        }
        throw new IllegalStateException("No ConcreteDomainOperators for " + PrimitiveData.text(conceptRepresentation.nid()));
    }

}
