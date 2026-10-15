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
package dev.ikm.komet.rules.test;

import dev.ikm.komet.actions.KometActionInput;
import dev.ikm.komet.actions.KometActions;
import dev.ikm.komet.framework.performance.Measures;
import dev.ikm.komet.framework.performance.Topic;
import dev.ikm.komet.framework.performance.impl.ObservationRecord;
import dev.ikm.komet.framework.rulebase.Consequence;
import dev.ikm.komet.framework.rulebase.ConsequenceAction;
import dev.ikm.komet.framework.view.ObservableViewNoOverride;
import dev.ikm.komet.framework.view.ViewProperties;
import dev.ikm.komet.rules.actions.KometActionMenuItem;
import dev.ikm.komet.rules.evrete.EvreteRulesService;
import dev.ikm.komet.terms.KometConcept;
import dev.ikm.tinkar.common.service.CachingService;
import dev.ikm.tinkar.common.service.PrimitiveData;
import dev.ikm.tinkar.coordinate.Coordinates;
import dev.ikm.tinkar.entity.EntityVersion;
import dev.ikm.tinkar.entity.load.LoadEntitiesFromProtobufFile;
import dev.ikm.tinkar.terms.ConceptFacade;
import dev.ikm.tinkar.terms.EntityFacade;
import org.eclipse.collections.api.factory.Lists;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestInstance;

import java.io.File;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * The component focus rules offer Komet's actions as Komet's context menu runs them: a
 * {@code COMPONENT_FOCUSED} observation of the latest version, fired through the rule service, read
 * back as menu items. An active component is offered inactivation and an inactive one activation,
 * each as a {@link KometActionMenuItem} named after the action concept, on the focused subject.
 */
@TestInstance(TestInstance.Lifecycle.PER_CLASS)
class ComponentFocusOffersIT {

    private static final File PB_STARTER_DATA = new File("target/data/ike-starter-set-reasoned-pb.zip");

    private ViewProperties viewProperties;
    private EvreteRulesService rules;

    @BeforeAll
    void startStore() throws Exception {
        assertTrue(PB_STARTER_DATA.exists(), "Starter data must be present at " + PB_STARTER_DATA.getAbsolutePath());
        CachingService.clearAll();
        PrimitiveData.selectControllerByName("Load Ephemeral Store");
        PrimitiveData.start();
        new LoadEntitiesFromProtobufFile(PB_STARTER_DATA).compute();
        viewProperties = new ObservableViewNoOverride(Coordinates.View.DefaultView()).makeOverridableViewProperties();
        rules = new EvreteRulesService();
    }

    @AfterAll
    void stopStore() {
        PrimitiveData.stop();
    }

    @Test
    void anActiveComponentIsOfferedInactivation() {
        ConceptFacade subject = KometConcept.GREATER_THAN;

        List<KometActionMenuItem> items = offeredOn(subject);

        assertEquals(List.of("Inactivate component"), items.stream().map(KometActionMenuItem::getText).toList());
        assertTrue(EntityFacade.same(subject, items.getFirst().offer().subject()), "offered on the focused subject");
    }

    @Test
    void anInactiveComponentIsOfferedActivation() {
        ConceptFacade subject = KometConcept.LESS_THAN;
        assertTrue(KometActions.expect(KometConcept.INACTIVATE_COMPONENT)
                .perform(KometActionInput.of(KometConcept.INACTIVATE_COMPONENT, subject),
                        viewProperties.calculator(), viewProperties.nodeView().editCoordinate())
                .completed());

        List<KometActionMenuItem> items = offeredOn(subject);

        assertEquals(List.of("Activate component"), items.stream().map(KometActionMenuItem::getText).toList());
        assertFalse(items.getFirst().getLongText().isBlank(), "the action concept's definition is the long text");
    }

    private List<KometActionMenuItem> offeredOn(ConceptFacade subject) {
        EntityVersion latest = viewProperties.calculator().<EntityVersion>latest(subject.nid()).get();
        ObservationRecord observation = new ObservationRecord(Topic.COMPONENT_FOCUSED, latest, Measures.present());
        List<Consequence<?>> consequences = rules.execute("Component context menu", Lists.immutable.of(observation),
                viewProperties, viewProperties.nodeView().editCoordinate()).castToList();
        return consequences.stream()
                .filter(c -> c instanceof ConsequenceAction a && a.generatedAction() instanceof KometActionMenuItem)
                .map(c -> (KometActionMenuItem) ((ConsequenceAction) c).generatedAction())
                .toList();
    }
}
