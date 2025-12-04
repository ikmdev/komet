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
package dev.ikm.tinkar.coordinate.logic;


import dev.ikm.tinkar.common.id.PublicId;
import dev.ikm.tinkar.entity.Entity;
import dev.ikm.tinkar.terms.ComponentWithNid;
import dev.ikm.tinkar.terms.ConceptFacade;
import dev.ikm.tinkar.terms.EntityProxy;
import dev.ikm.tinkar.terms.TinkarTerm;

/**
 * The Enum PremiseType.
 *
 * 
 */
public enum PremiseType implements dev.ikm.tinkar.component.Concept, ComponentWithNid {
    /**
     * Compute the taxonomy from stated axioms.
     */
    STATED("Stated", TinkarTerm.STATED_PREMISE_TYPE, TaxonomyFlag.STATED),

    /**
     * Compute the taxonomy from inferred axioms.
     */
    INFERRED("Inferred", TinkarTerm.INFERRED_PREMISE_TYPE, TaxonomyFlag.INFERRED);

    String displayName;
    EntityProxy.Concept premiseTypeConcept;
    int[] flags;

    PremiseType(String displayName, EntityProxy.Concept premiseTypeConcept, TaxonomyFlag taxonomyFlag) {
        this.displayName = displayName;
        this.premiseTypeConcept = premiseTypeConcept;
        this.flags = new int[]{taxonomyFlag.bits};
    }

    public static PremiseType fromConcept(ConceptFacade concept) {
        if (Entity.nid(concept) == TinkarTerm.INFERRED_PREMISE_TYPE.nid()) {
            return INFERRED;
        }
        if (Entity.nid(concept) == TinkarTerm.STATED_PREMISE_TYPE.nid()) {
            return STATED;
        }
        throw new IllegalStateException("PremiseType.fromConcept can't handle: " + concept);
    }

    public ConceptFacade getPremiseTypeConcept() {
        return premiseTypeConcept;
    }

    public PremiseType next() {
        switch (this) {
            case INFERRED:
                return STATED;
            case STATED:
                return INFERRED;
            default:
                throw new UnsupportedOperationException("h Can't handle: " + this);
        }
    }

    @Override
    public String toString() {
        return displayName;
    }

    public int[] getFlags() {
        return flags;
    }

    @Override
    public PublicId publicId() {
        return premiseTypeConcept;
    }

    @Override
    public int nid() {
        return premiseTypeConcept.nid();
    }
}

