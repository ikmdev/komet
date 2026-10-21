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
package dev.ikm.komet.actions;

import dev.ikm.komet.terms.KometConcept;
import dev.ikm.komet.terms.KometPattern;
import dev.ikm.tinkar.common.service.CachingService;
import dev.ikm.tinkar.common.service.PrimitiveData;
import dev.ikm.tinkar.coordinate.Calculators;
import dev.ikm.tinkar.coordinate.view.calculator.ViewCalculator;
import dev.ikm.tinkar.entity.EntityService;
import dev.ikm.tinkar.entity.SemanticEntityVersion;
import dev.ikm.tinkar.entity.load.LoadEntitiesFromProtobufFile;
import dev.ikm.tinkar.terms.EntityFacade;
import org.eclipse.collections.api.list.ImmutableList;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestInstance;

import java.io.File;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * Every action the starter set defines has exactly one implementation, and every implementation is
 * an action the starter set defines. An action is defined by an {@code Action definition pattern}
 * semantic on its concept; the categories above the actions (read, change, check, navigate) carry
 * none. Only the actions on this module's path are checked: an action a plugin implements is
 * listed in {@link #IMPLEMENTED_BY_PLUGINS}, has no implementation here, and is checked where the
 * plugin is built.
 */
@TestInstance(TestInstance.Lifecycle.PER_CLASS)
class KometActionConsistencyIT {

    private static final File PB_STARTER_DATA = new File("target/data/ike-starter-set-reasoned-pb.zip");

    /** Actions the starter set defines that a plugin implements, with the plugin that does. */
    private static final Map<KometConcept, String> IMPLEMENTED_BY_PLUGINS = Map.of(
            KometConcept.EDIT_DOCUMENT_BLOCK, "komet-claude-plugin",
            KometConcept.EDIT_DOCUMENT_SENTENCE, "komet-claude-plugin");

    private ViewCalculator view;
    private Set<Long> definedActions;

    @BeforeAll
    void startStore() {
        assertTrue(PB_STARTER_DATA.exists(), "Starter data must be present at " + PB_STARTER_DATA.getAbsolutePath());
        CachingService.clearAll();
        PrimitiveData.selectControllerByName("Load Ephemeral Store");
        PrimitiveData.start();
        new LoadEntitiesFromProtobufFile(PB_STARTER_DATA).compute();
        view = Calculators.View.Default();
        definedActions = EntityService.get().semanticsOfPattern(KometPattern.ACTION_DEFINITION_PATTERN.nid())
                .map(definition -> definition.referencedComponentNid())
                .collect(Collectors.toSet());
    }

    @AfterAll
    void stopStore() {
        PrimitiveData.stop();
    }

    @Test
    void theStarterSetDefinesTheActionsKometBinds() {
        assertTrue(definedActions.contains(KometConcept.ACTIVATE_COMPONENT.nid()), "Activate component is defined");
        assertTrue(definedActions.contains(KometConcept.INACTIVATE_COMPONENT.nid()), "Inactivate component is defined");
        assertTrue(definedActions.contains(KometConcept.EDIT_DOCUMENT_BLOCK.nid()), "Edit document block is defined");
        assertTrue(definedActions.contains(KometConcept.EDIT_DOCUMENT_SENTENCE.nid()), "Edit document sentence is defined");
    }

    @Test
    void eachActionsDefinitionNamesItsInputAndResultPatterns() {
        Map<KometConcept, KometPattern> inputs = Map.of(
                KometConcept.ACTIVATE_COMPONENT, KometPattern.COMPONENT_ACTION_INPUT_PATTERN,
                KometConcept.INACTIVATE_COMPONENT, KometPattern.COMPONENT_ACTION_INPUT_PATTERN,
                KometConcept.EDIT_DOCUMENT_BLOCK, KometPattern.DOCUMENT_BLOCK_EDIT_INPUT_PATTERN,
                KometConcept.EDIT_DOCUMENT_SENTENCE, KometPattern.DOCUMENT_BLOCK_EDIT_INPUT_PATTERN);
        List<String> problems = new ArrayList<>();
        inputs.forEach((action, input) -> {
            List<SemanticEntityVersion> definitions = EntityService.get()
                    .semanticsForComponentOfPattern(action.nid(), KometPattern.ACTION_DEFINITION_PATTERN.nid())
                    .map(definition -> view.latest(definition).get()).toList();
            if (definitions.size() != 1) {
                problems.add(action + " has " + definitions.size() + " definitions");
                return;
            }
            ImmutableList<Object> fields = definitions.getFirst().fieldValues();
            if (((EntityFacade) fields.get(0)).nid() != input.nid()) {
                problems.add(action + " takes " + view.getDescriptionTextOrNid(((EntityFacade) fields.get(0)).nid()) + ", not " + input);
            }
            if (((EntityFacade) fields.get(1)).nid() != KometPattern.VERSION_CHANGE_RESULT_PATTERN.nid()) {
                problems.add(action + " reports " + view.getDescriptionTextOrNid(((EntityFacade) fields.get(1)).nid()));
            }
        });
        assertEquals(List.of(), problems);
    }

    @Test
    void everyDefinedActionHasExactlyOneImplementation() {
        List<String> problems = new ArrayList<>();
        Set<Long> inPlugins = IMPLEMENTED_BY_PLUGINS.keySet().stream().map(KometConcept::nid).collect(Collectors.toSet());
        for (long action : definedActions) {
            long implementations = KometActions.all().stream().filter(a -> a.concept().nid() == action).count();
            long expected = inPlugins.contains(action) ? 0 : 1;
            if (implementations != expected) {
                problems.add(view.getDescriptionTextOrNid(action) + " has " + implementations + " implementations here, not " + expected);
            }
        }
        IMPLEMENTED_BY_PLUGINS.keySet().stream().filter(action -> !definedActions.contains(action.nid()))
                .forEach(action -> problems.add(action + " is listed as a plugin's action but has no Action definition"));
        assertEquals(List.of(), problems);
    }

    @Test
    void everyImplementationIsADefinedActionBeneathKometAction() {
        List<String> problems = new ArrayList<>();
        for (KometAction action : KometActions.all()) {
            String name = action.getClass().getName() + " (" + view.getDescriptionTextOrNid(action.concept().nid()) + ")";
            if (!definedActions.contains(action.concept().nid())) {
                problems.add(name + " implements a concept with no Action definition");
            }
            if (!view.navigationCalculator().descendentsOf(KometConcept.KOMET_ACTION).contains(action.concept().nid())) {
                problems.add(name + " implements a concept that is not a Komet action");
            }
        }
        assertEquals(List.of(), problems);
        assertTrue(KometActions.all().stream().anyMatch(a -> EntityFacade.same(a.concept(), KometConcept.INACTIVATE_COMPONENT)),
                "Komet's own actions are found");
    }
}
