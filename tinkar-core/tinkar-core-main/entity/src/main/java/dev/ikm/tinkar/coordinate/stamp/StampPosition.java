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
package dev.ikm.tinkar.coordinate.stamp;

//~--- JDK imports ------------------------------------------------------------

import dev.ikm.tinkar.common.service.PrimitiveData;
import dev.ikm.tinkar.common.util.time.DateTimeUtil;
import dev.ikm.tinkar.entity.Entity;
import dev.ikm.tinkar.terms.ConceptFacade;

import java.time.Instant;

//~--- interfaces -------------------------------------------------------------

/**
 * The class StampPosition.
 * An immutable class.
 *
 * 
 */
public interface StampPosition
        extends Comparable<StampPosition> {

    /**
     * Gets the time.
     *
     * @return the time
     */
    long time();

    /**
     * Gets the time as instant.
     *
     * @return the time as instant
     */
    default Instant instant() {
        return DateTimeUtil.epochMsToInstant(time());
    }


    /**
     * Compare to.
     *
     * @param o the o
     * @return the int
     */
    @Override
    default int compareTo(StampPosition o) {
        final int comparison = Long.compare(this.time(), o.time());

        if (comparison != 0) {
            return comparison;
        }

        return Integer.compare(this.getPathForPositionNid(), o.getPathForPositionNid());
    }


    int getPathForPositionNid();

    /**
     * Gets the stamp path ConceptFacade.
     *
     * @return the stamp path ConceptFacade
     */
    default ConceptFacade getPathForPositionConcept() {
        return Entity.getFast(getPathForPositionNid());
    }


    StampPosition withTime(long time);

    StampPosition withPathForPositionNid(int pathForPositionNid);

    StampPositionRecord toStampPositionImmutable();

    default String toUserString() {
        final StringBuilder sb = new StringBuilder();
        sb.append(DateTimeUtil.format(time()));
        sb.append(" on ")
                .append(PrimitiveData.text(this.getPathForPositionNid()));
        return sb.toString();
    }
}

