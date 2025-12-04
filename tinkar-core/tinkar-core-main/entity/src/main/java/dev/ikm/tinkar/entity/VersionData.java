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

import dev.ikm.tinkar.common.util.time.DateTimeUtil;
import dev.ikm.tinkar.component.Stamp;
import dev.ikm.tinkar.component.Version;
import dev.ikm.tinkar.entity.transaction.Transaction;
import dev.ikm.tinkar.terms.ConceptFacade;
import dev.ikm.tinkar.terms.State;

import java.time.Instant;

public interface VersionData extends Version, Stamp {

    Entity entity();

    default State state() {
        return stamp().state();
    }

    default StampEntity stamp() {
        return Entity.getStamp(stampNid());
    }

    int stampNid();

    default long time() {
        return stamp().time();
    }

    default ConceptFacade author() {
        return stamp().author();
    }

    default ConceptFacade module() {
        return stamp().module();
    }

    default ConceptFacade path() {
        return stamp().path();
    }

    default int authorNid() {
        return stamp().authorNid();
    }

    default int moduleNid() {
        return stamp().moduleNid();
    }

    default int pathNid() {
        return stamp().pathNid();
    }

    default boolean inactive() {
        return !active();
    }

    default boolean active() {
        return stamp().state().nid() == State.ACTIVE.nid();
    }

    default boolean canceled() {
        return (stamp().state() != null) ? stamp().state().nid() == State.CANCELED.nid() : true;
    }


    default boolean committed() {
        return !uncommitted();
    }

    default boolean uncommitted() {
        StampEntity stamp = stamp();
        if (stamp.time() == Long.MAX_VALUE) {
            return true;
        }
        if (stamp().state().nid() == State.CANCELED.nid()) {
            return false;
        }
        if (Transaction.forStamp(stamp).isPresent()) {
            // Participating in an active transaction...
            return true;
        }
        return false;
    }

    Entity chronology();

    default Instant instant() {
        return DateTimeUtil.epochMsToInstant(time());
    }

}
