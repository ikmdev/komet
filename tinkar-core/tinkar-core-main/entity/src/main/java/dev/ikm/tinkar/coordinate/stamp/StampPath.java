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

import dev.ikm.tinkar.common.id.PublicId;
import dev.ikm.tinkar.entity.Entity;
import dev.ikm.tinkar.terms.ConceptFacade;
import org.eclipse.collections.api.set.ImmutableSet;

import java.util.Arrays;

//~--- interfaces -------------------------------------------------------------

/**
 * The Interface StampPath.
 *
 * 
 */
public interface StampPath
        extends Comparable<StampPath> {
    default PublicId pathCoordinateId() {
        return Entity.provider().getEntityFast(pathConceptNid());
    }

    /**
     * Gets the path Concept nid.
     *
     * @return the nid of the Concept that defines this STAMP path.
     */
    int pathConceptNid();

    default ConceptFacade pathConcept() {
        return Entity.getFast(pathConceptNid());
    }

    @Override
    default int compareTo(StampPath that) {
        if (this.pathConceptNid() != that.pathConceptNid()) {
            return Integer.compare(this.pathConceptNid(), that.pathConceptNid());
        }
        if (this.getPathOrigins().size() != that.getPathOrigins().size()) {
            return Integer.compare(this.getPathOrigins().size(), that.getPathOrigins().size());
        }
        StampPosition[] thisOrigins = (StampPosition[]) this.getPathOrigins().toArray();
        Arrays.sort(thisOrigins);
        StampPosition[] thatOrigins = (StampPosition[]) that.getPathOrigins().toArray();
        Arrays.sort(thatOrigins);
        return Arrays.compare(thisOrigins, thatOrigins);
    }

    /**
     * Gets the path origins.
     *
     * @return The origins of this path.
     */
    ImmutableSet<StampPositionRecord> getPathOrigins();

    StampPathImmutable toStampPathImmutable();

    /**
     * @return a StampFilterImmutable representing the latest on this path, with no author constraints.
     */
    default StampCoordinateRecord getStampFilter() {
        return StampPathImmutable.getStampFilter(this);
    }
}

