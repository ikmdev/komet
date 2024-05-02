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
package dev.ikm.komet.amplify.lidr.om;

import dev.ikm.komet.amplify.lidr.viewmodels.ViewModelHelper;
import dev.ikm.tinkar.common.id.PublicId;
import dev.ikm.tinkar.common.util.uuid.UuidUtil;
import dev.ikm.tinkar.entity.Entity;
import dev.ikm.tinkar.entity.EntityService;
import dev.ikm.tinkar.terms.EntityProxy;

import java.util.Optional;

public record ResultConformanceRecord(PublicId resultConformanceId, PublicId scaleId, PublicId propertyId) {
    public static final EntityProxy.Pattern SCALE_ROLETYPE = EntityProxy.Pattern.make(null, UuidUtil.fromSNOMED("370132008"));
    public static final EntityProxy.Pattern PROPERTY_ROLETYPE = EntityProxy.Pattern.make(null, UuidUtil.fromSNOMED("370130000"));

    public static ResultConformanceRecord make(PublicId resultConformanceId) {
        Optional<Entity> resultConformanceEntity = EntityService.get().getEntity(resultConformanceId.asUuidArray());
        if (resultConformanceEntity.isEmpty()) {
            throw new IllegalArgumentException("PublicId " + resultConformanceId + " is not in database.");
        }
        return make(resultConformanceEntity.get());
    }

    public static ResultConformanceRecord make(Entity resultConformanceEntity) {
        return new ResultConformanceRecord(
                resultConformanceEntity.publicId(),
               ViewModelHelper.findConceptReferenceForRoleType(ViewModelHelper.findLatestLogicalDefinition(resultConformanceEntity).get(), SCALE_ROLETYPE).get().publicId(),
               ViewModelHelper.findConceptReferenceForRoleType(ViewModelHelper.findLatestLogicalDefinition(resultConformanceEntity).get(), PROPERTY_ROLETYPE).get().publicId()
        );
    }
}
