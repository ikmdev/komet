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
import dev.ikm.tinkar.entity.load.LoadEntitiesFromProtobufFile;
import dev.ikm.tinkar.terms.EntityFacade;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestInstance;

import java.io.File;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * Every action the starter set defines has exactly one implementation, and every implementation is
 * an action the starter set defines. An action is defined by an {@code Action definition pattern}
 * semantic on its concept; the categories above the actions (read, change, check, navigate) carry
 * none. Only the actions on this module's path are checked: a plugin's actions are checked where
 * the plugin is built.
 */
@TestInstance(TestInstance.Lifecycle.PER_CLASS)
class KometActionConsistencyIT {

    private static final File PB_STARTER_DATA = new File("target/data/ike-starter-set-reasoned-pb.zip");

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
    }

    @Test
    void everyDefinedActionHasExactlyOneImplementation() {
        List<String> problems = new ArrayList<>();
        for (long action : definedActions) {
            long implementations = KometActions.all().stream().filter(a -> a.concept().nid() == action).count();
            if (implementations != 1) {
                problems.add(view.getDescriptionTextOrNid(action) + " has " + implementations + " implementations");
            }
        }
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
