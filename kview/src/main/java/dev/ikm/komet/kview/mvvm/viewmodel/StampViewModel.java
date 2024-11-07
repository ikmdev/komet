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
package dev.ikm.komet.kview.mvvm.viewmodel;

import dev.ikm.komet.framework.view.ViewProperties;
import dev.ikm.tinkar.common.id.IntIdSet;
import dev.ikm.tinkar.entity.*;
import dev.ikm.tinkar.terms.State;
import dev.ikm.tinkar.terms.TinkarTerm;
import javafx.beans.property.ReadOnlyObjectProperty;
import org.carlfx.cognitive.validator.MessageType;
import org.carlfx.cognitive.validator.ValidationMessage;
import org.carlfx.cognitive.viewmodel.ViewModel;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import static dev.ikm.tinkar.coordinate.stamp.StampFields.*;

public class StampViewModel extends FormViewModel {

    public final static String MODULES_PROPERTY = "modules";
    public final static String PATHS_PROPERTY = "paths";

    public StampViewModel() {
        super(); // Default to ViewMode
        addProperty(STATUS, State.ACTIVE)
                .addProperty(AUTHOR, TinkarTerm.USER)
                .addProperty(TIME, System.currentTimeMillis())
                .addProperty(MODULE, (ConceptEntity) null)
                .addProperty(PATH, (ConceptEntity) null)
                .addProperty(MODULES_PROPERTY, Collections.emptyList(), true)
                .addProperty(PATHS_PROPERTY, Collections.emptyList(), true);

        addValidator(MODULE, "Module", (ReadOnlyObjectProperty nidProp, ViewModel vm) -> {
            if (nidProp.isNull().get()) {
                return new ValidationMessage(MODULE, MessageType.ERROR, "Stamp's ${%s} is required.".formatted(MODULE));
            }
            return VALID;
        });
        addValidator(PATH, "Path", (ReadOnlyObjectProperty nidProp, ViewModel vm) -> {
            if (nidProp.isNull().get()) {
                return new ValidationMessage(PATH, MessageType.ERROR, "Stamp's ${%s} is required.".formatted(PATH));
            }
            return VALID;
        });
    }

    @Override
    public StampViewModel save(boolean force) {
        return (StampViewModel) super.save(force);
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
}
