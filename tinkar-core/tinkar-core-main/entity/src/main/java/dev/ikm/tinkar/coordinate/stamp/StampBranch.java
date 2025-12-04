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

import dev.ikm.tinkar.common.service.PrimitiveData;
import dev.ikm.tinkar.entity.Entity;
import dev.ikm.tinkar.terms.ConceptFacade;

import java.time.Instant;

public interface StampBranch
        extends Comparable<StampBranch> {

    /**
     * Gets the time.
     *
     * @return the time
     */
    long getBranchOriginTime();

    /**
     * Gets the time as instant.
     *
     * @return the time as instant
     */
    default Instant getTimeAsInstant() {
        return Instant.ofEpochMilli(this.getBranchOriginTime());
    }

    /**
     * Compare to.
     *
     * @param o the o
     * @return the int
     */
    @Override
    default int compareTo(StampBranch o) {
        final int comparison = Long.compare(this.getBranchOriginTime(), o.getBranchOriginTime());

        if (comparison != 0) {
            return comparison;
        }

        return Integer.compare(this.getPathOfBranchNid(), o.getPathOfBranchNid());
    }


    int getPathOfBranchNid();

    /**
     * Gets the stamp path ConceptFacade.
     *
     * @return the stamp path ConceptFacade
     */
    default ConceptFacade getPathOfBranchConcept() {
        return Entity.getFast(getPathOfBranchNid());
    }

    StampBranchRecord toStampBranchRecord();

    default String toUserString() {
        final StringBuilder sb = new StringBuilder();


        if (this.getBranchOriginTime() == Long.MAX_VALUE) {
            sb.append("latest");
        } else if (this.getBranchOriginTime() == Long.MIN_VALUE) {
            sb.append("CANCELED");
        } else {
            sb.append(getTimeAsInstant());
        }

        sb.append(" on '")
                .append(PrimitiveData.text(this.getPathOfBranchNid())).append("'");
        return sb.toString();
    }
}
