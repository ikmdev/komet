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
package dev.ikm.komet.framework;

import dev.ikm.tinkar.common.id.PublicId;
import dev.ikm.tinkar.common.id.PublicIds;
import dev.ikm.tinkar.common.service.PrimitiveData;
import dev.ikm.tinkar.common.service.PrimitiveDataService;
import dev.ikm.tinkar.common.service.RemoteConceptSearchService;
import dev.ikm.tinkar.common.service.ServiceLifecycleManager;
import dev.ikm.tinkar.entity.EntityHandle;

import java.util.OptionalInt;
import java.util.UUID;

/**
 * Finds the component of the open knowledge base that a public id names, when the public id
 * comes from outside it: read from preferences, or typed into a search field
 * ({@code IKE-Network/ike-issues#1171}, {@code IKE-Network/ike-issues#1174}).
 *
 * <p>Such a public id may name a component this knowledge base does not hold. Asking the store
 * for its nid would assign one anyway, and the assignment is kept. This lookup asks the store
 * first whether it holds the public id, and assigns nothing for one it does not hold.
 *
 * <p>A knowledge base served remotely holds locally only the components it has fetched in this
 * session, so there the store's answer says nothing about a component not yet fetched. In that
 * case the nid is assigned — it lasts only for the session — and the component is fetched to
 * find out whether the knowledge base holds it.
 */
public final class ComponentLookup {

    private ComponentLookup() {
    }

    /**
     * Finds the nid, in the open knowledge base, of the component a public id names.
     *
     * @param publicId the public id, from preferences or from text
     * @return the nid, or empty if the public id is unknown to the open knowledge base
     */
    public static OptionalInt nid(PublicId publicId) {
        PrimitiveDataService store = PrimitiveData.get();
        if (store.hasPublicId(publicId)) {
            return OptionalInt.of(store.nidForPublicId(publicId));
        }
        if (!servedRemotely()) {
            return OptionalInt.empty();
        }
        // Not fetched yet in this session. Assign the session's nid for it and fetch it: the
        // server, not the local identity map, knows whether the knowledge base holds it.
        int nid = store.nidForPublicId(publicId);
        return EntityHandle.get(nid).isPresent() ? OptionalInt.of(nid) : OptionalInt.empty();
    }

    /**
     * Finds the nid, in the open knowledge base, of the component a UUID names.
     *
     * @param uuid a UUID of the component's public id, from preferences or from text
     * @return the nid, or empty if the UUID is unknown to the open knowledge base
     */
    public static OptionalInt nid(UUID uuid) {
        return nid(PublicIds.of(uuid));
    }

    /**
     * Whether the open knowledge base is served remotely, and so holds locally only what it has
     * fetched. A remote knowledge base runs a remote search service; a local one does not.
     */
    private static boolean servedRemotely() {
        return ServiceLifecycleManager.get().getRunningService(RemoteConceptSearchService.class).isPresent();
    }
}
