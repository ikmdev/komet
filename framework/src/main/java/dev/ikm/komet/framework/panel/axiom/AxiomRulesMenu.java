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
package dev.ikm.komet.framework.panel.axiom;

import dev.ikm.komet.framework.observable.ObservableSemanticVersion;
import dev.ikm.komet.framework.performance.Measures;
import dev.ikm.komet.framework.performance.Topic;
import dev.ikm.komet.framework.performance.impl.ObservationRecord;
import dev.ikm.komet.framework.rulebase.Consequence;
import dev.ikm.komet.framework.rulebase.ConsequenceAction;
import dev.ikm.komet.framework.rulebase.ConsequenceMenu;
import dev.ikm.komet.framework.rulebase.RuleService;
import dev.ikm.komet.framework.view.ViewProperties;
import dev.ikm.tinkar.coordinate.logic.PremiseType;
import dev.ikm.tinkar.entity.graph.DiTreeEntity;
import dev.ikm.tinkar.entity.graph.EntityVertex;
import javafx.scene.Node;
import javafx.scene.control.ContextMenu;
import javafx.scene.control.Menu;
import javafx.scene.control.SeparatorMenuItem;
import org.controlsfx.control.action.Action;
import org.controlsfx.control.action.ActionGroup;
import org.controlsfx.control.action.ActionUtils;
import org.eclipse.collections.api.factory.Lists;
import org.eclipse.collections.api.list.ImmutableList;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Builds and shows the Evrete-rules context menu for a single axiom clause — the same rule-driven
 * menu the classic {@code ClauseView} pencil produces, extracted so alternative axiom renderers
 * (e.g. the refreshed {@code KonceptAxiomTree}, ike-issues#639) can offer it without duplicating the
 * rules-engine wiring.
 *
 * <p>It presents the focused clause to the rules engine as a {@link Topic#AXIOM_FOCUSED} observation
 * carrying an {@link AxiomSubjectRecord} (the clause vertex, its tree, the containing semantic, and
 * the premise), and renders the returned {@link Consequence consequences} — axiom-clause actions
 * such as move/delete/wrap — as menu items. Only meaningful for stated axioms.
 */
public final class AxiomRulesMenu {

    private static final Logger LOG = LoggerFactory.getLogger(AxiomRulesMenu.class);

    private AxiomRulesMenu() {
    }

    /**
     * Runs the axiom-focused rules for the given clause and, if any actions result, shows them as a
     * context menu at the supplied screen location.
     *
     * @param vertex          the clause's vertex in the logical-definition tree
     * @param axiomTree       the concept's logical-definition tree
     * @param semanticVersion the semantic version containing the axioms
     * @param premiseType     the premise (rule actions are intended for {@link PremiseType#STATED})
     * @param viewProperties  the view the rules run against
     * @param anchor          the node to anchor the popup to
     * @param screenX         the screen x to show the menu at
     * @param screenY         the screen y to show the menu at
     */
    public static void show(EntityVertex vertex, DiTreeEntity axiomTree, ObservableSemanticVersion semanticVersion,
                            PremiseType premiseType, ViewProperties viewProperties, Node anchor,
                            double screenX, double screenY) {
        try {
            AxiomSubjectRecord axiomSubjectRecord = new AxiomSubjectRecord(
                    vertex.vertexIndex(), axiomTree, semanticVersion, premiseType, anchor);
            ObservationRecord observation = new ObservationRecord(
                    Topic.AXIOM_FOCUSED, axiomSubjectRecord, Measures.present());
            ImmutableList<Consequence<?>> consequences = RuleService.get().execute(
                    "Koncept axiom clause context menu", Lists.immutable.of(observation),
                    viewProperties, viewProperties.nodeView().editCoordinate());
            if (consequences.isEmpty()) {
                return;
            }
            ContextMenu contextMenu = new ContextMenu();
            for (Consequence<?> consequence : consequences) {
                switch (consequence) {
                    case ConsequenceAction consequenceAction -> {
                        if (consequenceAction.generatedAction() instanceof Action action) {
                            if (action instanceof ActionGroup actionGroup) {
                                Menu menu = ActionUtils.createMenu(action);
                                for (Action actionInGroup : actionGroup.getActions()) {
                                    if (actionInGroup == ActionUtils.ACTION_SEPARATOR) {
                                        menu.getItems().add(new SeparatorMenuItem());
                                    } else {
                                        menu.getItems().add(ActionUtils.createMenuItem(actionInGroup));
                                    }
                                }
                                contextMenu.getItems().add(menu);
                            } else if (action == ActionUtils.ACTION_SEPARATOR) {
                                contextMenu.getItems().add(new SeparatorMenuItem());
                            } else {
                                contextMenu.getItems().add(ActionUtils.createMenuItem(action));
                            }
                        }
                    }
                    case ConsequenceMenu consequenceMenu -> contextMenu.getItems().add(consequenceMenu.generatedMenu());
                    default -> LOG.warn("Cannot handle consequence of type: {}", consequence);
                }
            }
            if (!contextMenu.getItems().isEmpty()) {
                contextMenu.show(anchor, screenX, screenY);
            }
        } catch (RuntimeException e) {
            LOG.warn("Could not build axiom rules context menu", e);
        }
    }
}
