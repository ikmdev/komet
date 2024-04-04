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
package dev.ikm.komet.amplify.viewmodels;

import dev.ikm.komet.amplify.mvvm.ViewModel;
import dev.ikm.komet.amplify.mvvm.validator.MessageType;
import dev.ikm.komet.amplify.mvvm.validator.ValidationMessage;
import dev.ikm.komet.amplify.om.DescrName;
import dev.ikm.komet.framework.view.ViewProperties;
import dev.ikm.tinkar.common.id.IntIdSet;
import dev.ikm.tinkar.common.id.PublicId;
import dev.ikm.tinkar.entity.*;
import dev.ikm.tinkar.terms.TinkarTerm;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

public class DescrNameViewModel extends FormViewModel {
    public static final String NAME_TEXT = "nameText";
    public static final String NAME_TYPE = "nameType";
    public static final String CASE_SIGNIFICANCE = "caseSignificance";
    public static final String STATUS = "status";
    public static final String MODULE = "module";
    public static final String LANGUAGE = "language";
    public static final String IS_SUBMITTED = "isSubmitted";
    public static final String  MODULES_PROPERTY = "modules";
    public static final String  PATHS_PROPERTY = "paths";

    public static final String PUBLIC_ID = "publidId";


    public DescrNameViewModel() {
        super(); // defaults to View mode
        addProperty(NAME_TEXT, "")
                .addProperty(NAME_TYPE, (Object) null)
                .addProperty(CASE_SIGNIFICANCE, (ConceptEntity) null)
                .addProperty(STATUS, TinkarTerm.ACTIVE_STATE)
                .addProperty(MODULE, (ConceptEntity) null)
                .addProperty(LANGUAGE, (ConceptEntity) null)
                .addProperty(IS_SUBMITTED, false)
                .addProperty(PUBLIC_ID, (PublicId) null);
    }

    public Set<ConceptEntity> findAllLanguages(ViewProperties viewProperties) {
        IntIdSet languageDescendents = viewProperties.calculator().descendentsOf(TinkarTerm.LANGUAGE.nid());
        Set<ConceptEntity> allLangs = languageDescendents.intStream()
                .mapToObj(langNid -> (ConceptEntity) Entity.getFast(langNid))
                .collect(Collectors.toSet());
        return allLangs;
    }

    public Set<ConceptEntity> findAllStatuses(ViewProperties viewProperties) {
        Entity<? extends EntityVersion> statusEntity = EntityService.get().getEntityFast(TinkarTerm.STATUS_VALUE);
        IntIdSet statusDescendents = viewProperties.calculator().descendentsOf(statusEntity.nid());
        Set<ConceptEntity> allStatuses = statusDescendents.intStream()
                .mapToObj(statusNid -> (ConceptEntity) Entity.getFast(statusNid))
                .collect(Collectors.toSet());
        return allStatuses;
    }

    public Set<ConceptEntity> findAllCaseSignificants(ViewProperties viewProperties) {
        IntIdSet caseSenseDescendents = viewProperties.calculator().descendentsOf(TinkarTerm.DESCRIPTION_CASE_SIGNIFICANCE.nid());
        Set<ConceptEntity> allCaseDescendents = caseSenseDescendents.intStream()
                .mapToObj(caseNid -> (ConceptEntity) Entity.getFast(caseNid))
                .collect(Collectors.toSet());

        return allCaseDescendents;
    }
    public List<ConceptEntity> findAllModules(ViewProperties viewProperties) {
        try {
            Entity<? extends EntityVersion> moduleEntity = EntityService.get().getEntityFast(TinkarTerm.MODULE);
            IntIdSet moduleDescendents = viewProperties.calculator().descendentsOf(moduleEntity.nid());

            // get all descendant modules
            List<ConceptEntity> allModules =
                    moduleDescendents.intStream()
                            .mapToObj(moduleNid -> (ConceptEntity) Entity.getFast(moduleNid))
                            .toList();
            return allModules;
        } catch (Throwable th) {
            addValidator(MODULES_PROPERTY, "Module Entities", (Void prop, ViewModel vm) -> new ValidationMessage(MessageType.ERROR, "PrimitiveData services are not up. Attempting to retrieve ${%s}. Must call start().".formatted(MODULES_PROPERTY), th));
            return List.of();
        }
    }
    public List<ConceptEntity<ConceptEntityVersion>> findAllPaths(ViewProperties viewProperties) {
        try {
            //List of Concepts that represent available Paths in the data
            List<ConceptEntity<ConceptEntityVersion>> paths = new ArrayList<>();
            //Get all Path semantics from the Paths Pattern
            int[] pathSemanticNids = EntityService.get().semanticNidsOfPattern(TinkarTerm.PATHS_PATTERN.nid());
            //For each Path semantic get the concept that the semantic is referencing
            for (int pathSemanticNid : pathSemanticNids) {
                SemanticEntity<SemanticEntityVersion> semanticEntity = Entity.getFast(pathSemanticNid);
                int pathConceptNid = semanticEntity.referencedComponentNid();
                paths.add(EntityService.get().getEntityFast(pathConceptNid));
            }
            return paths;
        } catch (Throwable th) {
            addValidator(PATHS_PROPERTY, "Path Entities", (Void prop, ViewModel vm) -> new ValidationMessage(MessageType.ERROR, "PrimitiveData services are not up. Attempting to retrieve ${%s}. Must call start().".formatted(PATHS_PROPERTY), th));
            return List.of();
        }
    }
    public DescrName create() {
        return new DescrName(getValue(PUBLIC_ID),
                getValue(NAME_TEXT),
                getValue(NAME_TYPE),
                getValue(CASE_SIGNIFICANCE),
                getValue(STATUS),
                getValue(MODULE),
                getValue(LANGUAGE)
        );
    }
}
