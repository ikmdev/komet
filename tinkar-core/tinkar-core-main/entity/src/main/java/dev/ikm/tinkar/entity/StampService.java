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

import dev.ikm.tinkar.common.id.IntIdSet;
import dev.ikm.tinkar.common.service.PrimitiveData;
import dev.ikm.tinkar.entity.internal.StampServiceFinder;
import dev.ikm.tinkar.entity.util.StampRealizer;
import dev.ikm.tinkar.terms.ConceptFacade;
import dev.ikm.tinkar.terms.EntityProxy;
import org.eclipse.collections.api.factory.Sets;
import org.eclipse.collections.api.list.primitive.ImmutableLongList;
import org.eclipse.collections.api.set.ImmutableSet;
import org.eclipse.collections.api.set.MutableSet;

public interface StampService {

    static StampService get() {
        return StampServiceFinder.INSTANCE.get();
    }

    /**
     * Very inefficient. Please override.
     *
     * @return IntIdSet of the stamp nids.
     */
    default IntIdSet getStampNids() {
        StampRealizer stampRealizer = new StampRealizer();
        PrimitiveData.get().forEach(stampRealizer);
        return stampRealizer.stampNids();
    }

    default ImmutableSet<ConceptFacade> getAuthorsInUse() {
        MutableSet<ConceptFacade> authors = Sets.mutable.empty();
        for (int authorNid : getAuthorNidsInUse().toArray()) {
            authors.add(EntityProxy.Concept.make(authorNid));
        }
        return authors.toImmutable();
    }

    IntIdSet getAuthorNidsInUse();

    default ImmutableSet<ConceptFacade> getModulesInUse() {
        MutableSet<ConceptFacade> modules = Sets.mutable.empty();
        for (int moduleNid : getModuleNidsInUse().toArray()) {
            modules.add(EntityProxy.Concept.make(moduleNid));
        }
        return modules.toImmutable();
    }

    IntIdSet getModuleNidsInUse();

    default ImmutableSet<ConceptFacade> getPathsInUse() {
        MutableSet<ConceptFacade> paths = Sets.mutable.empty();
        for (int pathNid : getPathNidsInUse().toArray()) {
            paths.add(EntityProxy.Concept.make(pathNid));
        }
        return paths.toImmutable();
    }

    IntIdSet getPathNidsInUse();

    ImmutableLongList getTimesInUse();

}
