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

import dev.ikm.tinkar.common.id.IntIdList;
import dev.ikm.tinkar.common.id.IntIdSet;
import dev.ikm.tinkar.common.id.IntIds;
import dev.ikm.tinkar.common.service.PrimitiveData;
import dev.ikm.tinkar.entity.Entity;
import dev.ikm.tinkar.terms.ConceptFacade;
import dev.ikm.tinkar.terms.EntityFacade;
import dev.ikm.tinkar.terms.EntityProxy;
import dev.ikm.tinkar.terms.State;
import org.eclipse.collections.api.list.ImmutableList;
import org.eclipse.collections.api.set.ImmutableSet;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.UUID;

public interface StampCoordinate
        extends TimeBasedAnalogMaker<StampCoordinate> {

    /**
     * @return a content based uuid, such that identical stamp coordinates
     * will have identical uuids, and that different stamp coordinates will
     * always have different uuids.
     */
    default UUID getStampFilterUuid() {
        ArrayList<UUID> uuidList = new ArrayList<>();
        for (State state : allowedStates().toEnumSet()) {
            Entity.provider().addSortedUuids(uuidList, state.nid());
        }
        Entity.provider().addSortedUuids(uuidList, stampPosition().getPathForPositionNid());
        Entity.provider().addSortedUuids(uuidList, moduleNids().toArray());
        Entity.provider().addSortedUuids(uuidList, modulePriorityNidList().toArray());
        StringBuilder b = new StringBuilder();
        b.append(uuidList.toString());
        b.append(stampPosition().time());
        return UUID.nameUUIDFromBytes(b.toString().getBytes());
    }

    /**
     * Determine what states should be included in results based on this
     * stamp coordinate. If current—but inactive—versions are desired,
     * the allowed states must include {@code Status.INACTIVE}
     *
     * @return the set of allowed states for results based on this stamp coordinate.
     */
    StateSet allowedStates();

    /**
     * Gets the stamp position.
     *
     * @return the position (time on a path) that is used to
     * compute what stamped objects versions are the latest with respect to this
     * position.
     */
    StampPosition stampPosition();

    /**
     * An empty array is a wild-card, and should match all modules. If there are
     * one or more module nids specified, only those modules will be included
     * in the results.
     *
     * @return an unmodifiable set of module nids to include in results based on this
     * stamp coordinate.
     */
    IntIdSet moduleNids();

    /**
     * Gets the module preference list for versions. Used to adjudicate which component to
     * return when more than one version is available. For example, if two modules
     * have versions the same component, which one do you prefer to return?
     *
     * @return an unmodifiable module preference list for versions.
     */

    IntIdList modulePriorityNidList();

    StampCoordinate withAllowedStates(StateSet stateSet);

    default ConceptFacade pathForFilter() {
        return EntityProxy.Concept.make(pathNidForFilter());
    }

    int pathNidForFilter();

    /**
     * Create a new StampFilter identical to the this filter, but with the modules modified.
     *
     * @param modules the new modules list.
     * @return the new path coordinate
     */
    default StampCoordinate withModules(Collection<ConceptFacade> modules) {
        return withModuleNids(IntIds.set.of(modules, EntityFacade::toNid));
    }

    StampCoordinate withModuleNids(IntIdSet moduleNids);

    default StampCoordinate withExcludedModules(Collection<ConceptFacade> excludedModules) {
        return withExcludedModuleNids(IntIds.set.of(excludedModules, EntityFacade::toNid));
    }

    StampCoordinate withExcludedModuleNids(IntIdSet excludedModuleNids);

    default StampCoordinate withModulePriorityNidList(List<ConceptFacade> excludedModules) {
        return withModulePriorityNidList(IntIds.list.of(excludedModules, EntityFacade::toNid));
    }

    StampCoordinate withModulePriorityNidList(IntIdList modulePriorityNidList);

    /**
     * Create a new Filter ImmutableCoordinate identical to the this coordinate, but with the path for position replaced.
     *
     * @param pathForPosition the new path for position
     * @return the new path coordinate
     */
    default StampCoordinate withPath(ConceptFacade pathForPosition) {
        return withStampPosition(stampPosition().withPathForPositionNid(pathForPosition.nid()).toStampPositionImmutable());
    }

    StampCoordinate withStampPosition(StampPositionRecord stampPosition);

    /**
     * @return multi-line string output suitable for presentation to user, as opposed to use in debugging.
     */

    default String toUserString() {
        final StringBuilder builder = new StringBuilder();

        builder.append("allowed states: ");
        builder.append(this.allowedStates().toUserString());

        builder.append("\n   position: ")
                .append(this.stampPosition().toUserString())
                .append("\n   modules: ");

        if (this.moduleNids().isEmpty()) {
            builder.append("all ");
        } else {
            builder.append(PrimitiveData.textList(this.moduleNids().toArray()))
                    .append(" ");
        }

        builder.append("\n   excluded modules: ");

        if (this.excludedModuleNids().isEmpty()) {
            builder.append("none ");
        } else {
            builder.append(PrimitiveData.textList(this.excludedModuleNids().toArray()))
                    .append(" ");
        }

        builder.append("\n   module priorities: ");
        if (this.modulePriorityNidList().isEmpty()) {
            builder.append("none");
        } else {
            builder.append(PrimitiveData.textList(this.modulePriorityNidList().toArray()));
        }

        return builder.toString();
    }

    /**
     * An empty array indicates that no modules should be excluded. If there are
     * one or more module nids specified, only those modules will be excluded
     * from the results.
     *
     * @return an unmodifiable set of module nids to exclude in results based on this
     * stamp filter.
     */
    IntIdSet excludedModuleNids();

    default StampCoordinateRecord toStampCoordinateRecord() {
        return StampCoordinateRecord.make(allowedStates(),
                stampPosition());
    }

    default long time() {
        return stampPosition().time();
    }

    /**
     * An empty array indicates that no modules should be excluded. If there are
     * one or more module nids specified, only those modules will be excluded
     * from the results.
     *
     * @return an unmodifiable set of modules to exclude in results based on this
     * stamp filter.
     */
    default ImmutableSet<dev.ikm.tinkar.component.Concept> excludedModules() {
        return excludedModuleNids().map(nid -> Entity.getFast(nid));
    }

    /**
     * An empty list is a wild-card, and should match all modules. If there are
     * one or more modules specified, only those modules will be included
     * in the results.
     *
     * @return an unmodifiable set of modules to include in results based on this
     * stamp coordinate.
     */
    default ImmutableSet<dev.ikm.tinkar.component.Concept> moduleSpecifications() {
        return moduleNids().map(nid -> Entity.getFast(nid));

    }

    /**
     * Gets the module preference list for versions. Used to adjudicate which component to
     * return when more than one version is available. For example, if two modules
     * have versions the same component, which one do you prefer to return?
     *
     * @return an unmodifiable module preference list for versions.
     */

    default ImmutableList<dev.ikm.tinkar.component.Concept> modulePriorityOrderSpecifications() {
        return modulePriorityNidList().map(nid -> Entity.getFast(nid));
    }
}
