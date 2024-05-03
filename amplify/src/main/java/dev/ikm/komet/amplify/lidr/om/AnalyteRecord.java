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

public record AnalyteRecord(PublicId analyteId, PublicId componentId, PublicId timeAspectId, PublicId methodTypeId) {
    public static final EntityProxy.Pattern COMPONENT_ROLETYPE = EntityProxy.Pattern.make(null, UuidUtil.fromSNOMED("246093002"));
    public static final EntityProxy.Pattern TIME_ASPECT_ROLETYPE = EntityProxy.Pattern.make(null, UuidUtil.fromSNOMED("370134009"));
    public static final EntityProxy.Pattern METHOD_TYPE_ROLETYPE = EntityProxy.Pattern.make(null, UuidUtil.fromSNOMED("260686004"));

    public AnalyteRecord(PublicId analyteId){
        this(analyteId, null, null, null);
    }
    public static AnalyteRecord make(PublicId analyteId) {
        Optional<Entity> analyteEntity = EntityService.get().getEntity(analyteId.asUuidArray());
        if (analyteEntity.isEmpty()) {
            throw new IllegalArgumentException("PublicId " + analyteId + " is not in database.");
        }
        return make(analyteEntity.get());
    }

    public static AnalyteRecord make(Entity analyteEntity) {
        return new AnalyteRecord(
                analyteEntity.publicId(),
               ViewModelHelper.findConceptReferenceForRoleType(ViewModelHelper.findLatestLogicalDefinition(analyteEntity).get(), COMPONENT_ROLETYPE).get().publicId(),
               ViewModelHelper.findConceptReferenceForRoleType(ViewModelHelper.findLatestLogicalDefinition(analyteEntity).get(), TIME_ASPECT_ROLETYPE).get().publicId(),
               ViewModelHelper.findConceptReferenceForRoleType(ViewModelHelper.findLatestLogicalDefinition(analyteEntity).get(), METHOD_TYPE_ROLETYPE).get().publicId()
        );
    }
}
