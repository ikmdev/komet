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
import java.util.UUID;

public record SpecimenRecord(PublicId specimenId, PublicId systemId, PublicId methodTypeId) {
public static final EntityProxy.Pattern METHOD_TYPE_ROLETYPE = EntityProxy.Pattern.make(null, UuidUtil.fromSNOMED("260686004"));
    public static final EntityProxy.Pattern SYSTEM_ROLETYPE = EntityProxy.Pattern.make(null, UuidUtil.fromSNOMED("704327008"));


    public static SpecimenRecord make(PublicId specimenId) {
        Optional<Entity> specimenEntity = EntityService.get().getEntity(specimenId.asUuidArray());
        if (specimenEntity.isEmpty()) {
            throw new IllegalArgumentException("PublicId " + specimenId + " is not in database.");
        }
        return make(specimenEntity.get());
    }

    public static SpecimenRecord make(Entity specimenEntity) {
        return new SpecimenRecord(
                specimenEntity.publicId(),
               ViewModelHelper.findConceptReferenceForRoleType(ViewModelHelper.findLatestLogicalDefinition(specimenEntity).get(), SYSTEM_ROLETYPE).get().publicId(),
               ViewModelHelper.findConceptReferenceForRoleType(ViewModelHelper.findLatestLogicalDefinition(specimenEntity).get(), METHOD_TYPE_ROLETYPE).get().publicId()
        );
    }
}
