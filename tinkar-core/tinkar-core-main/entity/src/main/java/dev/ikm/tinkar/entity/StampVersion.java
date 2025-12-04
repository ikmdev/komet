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
package dev.ikm.tinkar.entity;

import dev.ikm.tinkar.common.service.PrimitiveData;
import dev.ikm.tinkar.common.util.time.DateTimeUtil;
import dev.ikm.tinkar.component.Stamp;
import dev.ikm.tinkar.terms.ConceptFacade;
import dev.ikm.tinkar.terms.EntityProxy;
import dev.ikm.tinkar.terms.State;

import java.time.Instant;

public interface StampVersion extends Stamp, VersionData {
    default String describe() {
        return "s:" + PrimitiveData.text(stateNid()) +
                " t:" + DateTimeUtil.format(time(), DateTimeUtil.SEC_FORMATTER) +
                " a:" + PrimitiveData.text(authorNid()) +
                " m:" + PrimitiveData.text(moduleNid()) +
                " p:" + PrimitiveData.text(pathNid());
    }

    int stateNid();

    int authorNid();

    int moduleNid();

    int pathNid();

    default State state() {
        return State.fromConceptNid(stateNid());
    }

    long time();

    default ConceptFacade author() {
        return EntityProxy.Concept.make(authorNid());
    }

    default ConceptFacade module() {
        return EntityProxy.Concept.make(moduleNid());
    }

    default ConceptFacade path() {
        return EntityProxy.Concept.make(pathNid());
    }

    default Instant instant() {
        return DateTimeUtil.epochMsToInstant(time());
    }
}
