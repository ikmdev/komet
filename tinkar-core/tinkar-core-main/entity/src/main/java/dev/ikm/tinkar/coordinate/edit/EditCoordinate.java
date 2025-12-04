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
package dev.ikm.tinkar.coordinate.edit;

import dev.ikm.tinkar.common.service.PrimitiveData;
import dev.ikm.tinkar.entity.Entity;
import dev.ikm.tinkar.terms.ConceptFacade;

import java.util.ArrayList;
import java.util.UUID;

/**
 * Edits occur on the manifold coordinate path when developing.
 * <p>
 * Module is unchanged when developing. A default module is used for any new content.
 * <p>
 * When modularizing, a destination module is provided, and the change will be written to the
 * manifold coordinate path.
 * <p>
 * When promoting, the module will be unchanged, and the promotion path will be where a copy of
 * content on the manifold coordinate path written.
 *
 * 
 */
public interface EditCoordinate {

    default UUID getEditCoordinateUuid() {
        ArrayList<UUID> uuidList = new ArrayList<>();
        Entity.provider().addSortedUuids(uuidList, getAuthorNidForChanges());
        Entity.provider().addSortedUuids(uuidList, getDefaultModuleNid());
        Entity.provider().addSortedUuids(uuidList, getDestinationModuleNid());
        Entity.provider().addSortedUuids(uuidList, getDefaultPathNid());
        Entity.provider().addSortedUuids(uuidList, getPromotionPathNid());
        StringBuilder b = new StringBuilder();
        b.append(uuidList.toString());
        return UUID.nameUUIDFromBytes(b.toString().getBytes());
    }

    /**
     * Gets the author nid.
     *
     * @return the author nid
     */
    int getAuthorNidForChanges();

    /**
     * The default module is the module for new content when developing. Modifications to existing
     * content retain their module.
     *
     * @return
     */
    int getDefaultModuleNid();

    /**
     * The destination module is the module that existing content is moved to when Modularizing
     *
     * @return the nid of the destination module concept
     */
    int getDestinationModuleNid();

    /**
     * The path that new content is created on
     *
     * @return the nid of the promotion concept
     */
    int getDefaultPathNid();

    /**
     * The promotion path is the path that existing content is moved to when Promoting
     *
     * @return the nid of the promotion concept
     */
    int getPromotionPathNid();

    default ConceptFacade getAuthorForChanges() {
        return Entity.getFast(getAuthorNidForChanges());
    }

    /**
     * The default module is the module for new content when developing. Modifications to existing
     * content retain their module.
     *
     * @return
     */
    default ConceptFacade getDefaultModule() {
        return Entity.getFast(getDefaultModuleNid());
    }

    /**
     * The destination module is the module that existing content is moved to when Modularizing
     *
     * @return the destination module concept
     */
    default ConceptFacade getDestinationModule() {
        return Entity.getFast(getDestinationModuleNid());
    }

    EditCoordinateRecord toEditCoordinateRecord();

    /**
     * The path that new content is created on
     *
     * @return the promotion concept
     */
    default ConceptFacade getDefaultPath() {
        return Entity.getFast(getDefaultPathNid());
    }

    /**
     * The promotion path is the path that existing content is moved to when Promoting
     *
     * @return the promotion concept
     */
    default ConceptFacade getPromotionPath() {
        return Entity.getFast(getPromotionPathNid());
    }

    default String toUserString() {
        StringBuilder sb = new StringBuilder();
        sb.append("author: ").append(PrimitiveData.text(getAuthorNidForChanges())).append("\n");
        sb.append("default module: ").append(PrimitiveData.text(getDefaultModuleNid())).append("\n");
        sb.append("destination module: ").append(PrimitiveData.text(getDestinationModuleNid())).append("\n");
        sb.append("default path: ").append(PrimitiveData.text(getDefaultPathNid())).append("\n");
        sb.append("promotion path: ").append(PrimitiveData.text(getPromotionPathNid())).append("\n");
        return sb.toString();
    }

}

