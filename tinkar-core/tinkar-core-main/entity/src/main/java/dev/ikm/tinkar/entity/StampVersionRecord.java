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

import dev.ikm.tinkar.common.util.Validator;
import dev.ikm.tinkar.component.Stamp;
import io.soabase.recordbuilder.core.RecordBuilder;

/**
 * Maybe handle commit by listening to commit reactive stream, and if version is committed, add it to the chronology?
 */
@RecordBuilder
public record StampVersionRecord(StampRecord chronology,
                                 int stateNid, long time, int authorNid,
                                 int moduleNid,
                                 int pathNid) implements StampEntityVersion, ImmutableVersion, StampVersionRecordBuilder.With {


    public StampVersionRecord {
        Validator.notZero(stateNid);
        Validator.notZero(time);
        Validator.notZero(authorNid);
        Validator.notZero(moduleNid);
        Validator.notZero(pathNid);
    }

    public StampVersionRecord(StampRecord chronology,
                              StampVersion version) {
        this(chronology,
                Entity.nid(version.state()),
                version.time(),
                Entity.nid(version.author()),
                Entity.nid(version.module()),
                Entity.nid(version.path()));
    }
    
    @Override
    public StampRecord entity() {
        return chronology;
    }

    @Override
    public int stampNid() {
        return chronology.nid();
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        StampVersionRecord that = (StampVersionRecord) o;
        return stateNid == that.stateNid && time == that.time && authorNid == that.authorNid && moduleNid == that.moduleNid && pathNid == that.pathNid;
    }

    @Override
    public int hashCode() {
        return Integer.hashCode(nid());
    }

    @Override
    public String toString() {
        return "sv: ≤" + describe() + " nid: " + nid() + "≥";
    }

    @Override
    public StampEntity stamp() {
        return chronology;
    }

}
