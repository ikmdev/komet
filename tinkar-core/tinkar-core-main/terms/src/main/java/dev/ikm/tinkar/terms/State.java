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
package dev.ikm.tinkar.terms;


import dev.ikm.tinkar.common.id.PublicId;
import org.eclipse.collections.api.map.primitive.ImmutableIntObjectMap;
import org.eclipse.collections.api.map.primitive.MutableIntObjectMap;
import org.eclipse.collections.impl.factory.primitive.IntObjectMaps;

public enum State implements dev.ikm.tinkar.component.Concept, ComponentWithNid {
    ACTIVE(TinkarTerm.ACTIVE_STATE),
    INACTIVE(TinkarTerm.INACTIVE_STATE),
    WITHDRAWN(TinkarTerm.WITHDRAWN_STATE),
    CANCELED(TinkarTerm.CANCELED_STATE),
    /* TODO Consider changing from PRIMORDIAL to Premundane */
    PRIMORDIAL(TinkarTerm.PRIMORDIAL_STATE);

    final EntityProxy.Concept proxyForState;

    State(EntityProxy.Concept proxyForState) {
        this.proxyForState = proxyForState;
    }

    @Override
    public PublicId publicId() {
        return proxyForState;
    }

    @Override
    public int nid() {
        return proxyForState.nid();
    }

    private static ImmutableIntObjectMap<State> nidStateMap;

    public static State fromConceptNid(int conceptNid) {
        if (nidStateMap == null) {
            MutableIntObjectMap<State> mutableNidStateMap = IntObjectMaps.mutable.ofInitialCapacity(5);
            for (State state : State.values()) {
                mutableNidStateMap.put(state.nid(), state);
            }
            nidStateMap = mutableNidStateMap.toImmutable();
        }
        return nidStateMap.get(conceptNid);
    }
    public static State fromConcept(ConceptFacade concept) {
        return nidStateMap.get(concept.nid());
    }
}
