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
import dev.ikm.tinkar.component.Component;
import dev.ikm.tinkar.component.Stamp;
import dev.ikm.tinkar.component.Version;
import dev.ikm.tinkar.terms.ConceptFacade;
import dev.ikm.tinkar.terms.StampFacade;
import dev.ikm.tinkar.terms.State;
import dev.ikm.tinkar.terms.TinkarTerm;
import org.eclipse.collections.api.factory.Lists;
import org.eclipse.collections.api.list.ImmutableList;

import static dev.ikm.tinkar.common.util.time.DateTimeUtil.SEC_FORMATTER;

public interface StampEntity<V extends StampEntityVersion> extends Entity<V>,
        Stamp, Component, Version {

    default State state() {
        return lastVersion().state();
    }

    default long time() {
        if (lastVersion() != null) {
            return lastVersion().time();
        }
        return Long.MIN_VALUE;
    }

    default ConceptFacade author() {
        return Entity.provider().getEntityFast(authorNid());
    }

    default ConceptFacade module() {
        return Entity.provider().getEntityFast(moduleNid());
    }

    default ConceptFacade path() {
        return Entity.provider().getEntityFast(pathNid());
    }

    StampEntity stamp();

    default int pathNid() {
        if (lastVersion() != null) {
            return lastVersion().pathNid();
        }
        return TinkarTerm.CANCELED_STATE.nid();
    }

    default int moduleNid() {
        if (lastVersion() != null) {
            return lastVersion().moduleNid();
        }
        return TinkarTerm.CANCELED_STATE.nid();
    }

    default int authorNid() {
        if (lastVersion() != null) {
            return lastVersion().authorNid();
        }
        return TinkarTerm.CANCELED_STATE.nid();
    }

    default StampEntityVersion lastVersion() {
        if (versions().size() == 1) {
            return versions().get(0);
        }
        StampEntityVersion latest = null;
        for (StampEntityVersion version : versions()) {
            if (version.time() == Long.MIN_VALUE) {
                // if canceled (Long.MIN_VALUE), latest is canceled.
                return version;
            } else if (latest == null) {
                latest = version;
            } else if (latest.time() == Long.MAX_VALUE) {
                latest = version;
            } else if (version.time() == Long.MAX_VALUE) {
                // ignore uncommitted version;
            } else if (latest.time() < version.time()) {
                latest = version;
            }
        }
        return latest;
    }

    @Override
    ImmutableList<V> versions();

    /**
     * TODO: Consider if STAMP should just be a semantic...
     * @return
     */
    default ImmutableList<Object> fieldValues() {
        return Lists.immutable.of(state(), time(), author(), module(), path());
    }


    @Override
    default boolean canceled() {
        return Entity.super.canceled();
    }

    default int stateNid() {
        if (lastVersion() != null) {
            return lastVersion().stateNid();
        }
        return TinkarTerm.CANCELED_STATE.nid();
    }

    default String describe() {
        return "s:" + PrimitiveData.text(stateNid()) +
                " t:" + DateTimeUtil.format(time(), SEC_FORMATTER) +
                " a:" + PrimitiveData.text(authorNid()) +
                " m:" + PrimitiveData.text(moduleNid()) +
                " p:" + PrimitiveData.text(pathNid());
    }
}
