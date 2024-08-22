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
package dev.ikm.komet.kview.mvvm.model;

import dev.ikm.komet.framework.view.ViewProperties;
import dev.ikm.tinkar.common.id.IntIdSet;
import dev.ikm.tinkar.common.id.PublicId;
import dev.ikm.tinkar.entity.ConceptEntity;
import dev.ikm.tinkar.entity.Entity;
import dev.ikm.tinkar.entity.EntityService;

import java.util.Set;
import java.util.stream.Collectors;

import static dev.ikm.tinkar.terms.TinkarTerm.*;

public class DataViewModelHelper {



    public static Set<ConceptEntity> fetchFieldDefinitionDataTypes() {

        return Set.of(
                Entity.getFast(STRING.nid()),
                Entity.getFast(COMPONENT_FIELD.nid()),
                Entity.getFast(COMPONENT_ID_SET_FIELD.nid()),
                Entity.getFast(COMPONENT_ID_LIST_FIELD.nid()),
                Entity.getFast(DITREE_FIELD.nid()),
                Entity.getFast(DIGRAPH_FIELD.nid()),
                Entity.getFast(CONCEPT_FIELD.nid()),
                Entity.getFast(SEMANTIC_FIELD_TYPE.nid()),
                Entity.getFast(INTEGER_FIELD.nid()),
                Entity.getFast(FLOAT_FIELD.nid()),
                Entity.getFast(BOOLEAN_FIELD.nid()),
                Entity.getFast(BYTE_ARRAY_FIELD.nid()),
                Entity.getFast(ARRAY_FIELD.nid()),
                Entity.getFast(INSTANT_LITERAL.nid()),
                Entity.getFast(LONG.nid())
        );
    }

    public static Set<ConceptEntity> fetchStatusOpions(){
        return Set.of(
                Entity.getFast(ACTIVE_STATE.nid()),
                Entity.getFast(INACTIVE_STATE.nid()),
                Entity.getFast(WITHDRAWN_STATE.nid()),
                Entity.getFast(CANCELED_STATE.nid()),
                Entity.getFast(PRIMORDIAL_STATE.nid())
        );

    }

    public static Set<ConceptEntity> fetchDescriptionTypes(){
        return Set.of(
                Entity.getFast(FULLY_QUALIFIED_NAME_DESCRIPTION_TYPE.nid()),
                Entity.getFast(REGULAR_NAME_DESCRIPTION_TYPE.nid())
        );
    }

    public static Set<ConceptEntity> fetchDescendentsOfConcept(ViewProperties viewProperties, PublicId publicId) {
        IntIdSet decendents = viewProperties.calculator().descendentsOf(EntityService.get().nidForPublicId(publicId));
        Set<ConceptEntity> allDecendents = decendents.intStream()
                .mapToObj(decendentNid -> (ConceptEntity) Entity.getFast(decendentNid))
                .collect(Collectors.toSet());
        return allDecendents;
    }
}
