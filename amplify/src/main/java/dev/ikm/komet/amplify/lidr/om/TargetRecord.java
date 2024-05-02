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
import dev.ikm.tinkar.entity.Entity;
import dev.ikm.tinkar.entity.EntityService;
import dev.ikm.tinkar.terms.EntityProxy;

import java.util.Optional;
import java.util.UUID;

public record TargetRecord(PublicId targetId, PublicId targetAnalyteId) {
    public static final EntityProxy.Concept ANALYTE_ROLETYPE = EntityProxy.Concept.make(null, UUID.fromString("8c9214df-511c-36ba-bd5d-f4d38ce25f2f"));

    public static TargetRecord make(PublicId targetId) {
        Optional<Entity> targetEntity = EntityService.get().getEntity(targetId.asUuidArray());
        if (targetEntity.isEmpty()) {
            throw new IllegalArgumentException("PublicId " + targetId + " is not in database.");
        }
        return make(targetEntity.get());
    }

    public static TargetRecord make(Entity targetEntity) {
        return new TargetRecord(
                targetEntity.publicId(),
               ViewModelHelper.findConceptReferenceForRoleType(ViewModelHelper.findLatestLogicalDefinition(targetEntity).get(), ANALYTE_ROLETYPE).get().publicId()
        );
    }
}