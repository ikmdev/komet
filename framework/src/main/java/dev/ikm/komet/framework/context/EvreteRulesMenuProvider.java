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
package dev.ikm.komet.framework.context;

import dev.ikm.komet.framework.performance.Measures;
import dev.ikm.komet.framework.performance.Topic;
import dev.ikm.komet.framework.performance.impl.ObservationRecord;
import dev.ikm.komet.framework.rulebase.Consequence;
import dev.ikm.komet.framework.rulebase.ConsequenceAction;
import dev.ikm.komet.framework.rulebase.ConsequenceMenu;
import dev.ikm.komet.framework.rulebase.RuleService;
import dev.ikm.komet.framework.view.ViewProperties;
import dev.ikm.tinkar.coordinate.stamp.calculator.Latest;
import dev.ikm.tinkar.entity.EntityVersion;
import dev.ikm.tinkar.terms.EntityFacade;
import javafx.beans.property.SimpleIntegerProperty;
import javafx.beans.value.ObservableValue;
import javafx.scene.control.ContextMenu;
import javafx.scene.control.Control;
import org.controlsfx.control.action.Action;
import org.controlsfx.control.action.ActionUtils;
import org.eclipse.collections.api.factory.Lists;
import org.eclipse.collections.api.list.ImmutableList;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * An {@link AddToContextMenu} that populates a component's context menu from the
 * Evrete rule engine: it asserts a {@code COMPONENT_FOCUSED} observation for the
 * focused entity's latest version, fires {@link RuleService}, and renders the
 * resulting {@link Consequence}s as menu items — the same pipeline the built-in
 * component panels use, made available to any context menu (e.g. the identicon
 * menu) through {@link AddToContextMenu#providers()}.
 *
 * <p>Because the engine discovers plugin rules via the {@code RuleProvider} SPI,
 * plugin-contributed items (e.g. "Post state + history to Zulip") appear here
 * automatically, with no change to the menu that invokes the providers.
 */
public final class EvreteRulesMenuProvider implements AddToContextMenu {

    private static final Logger LOG = LoggerFactory.getLogger(EvreteRulesMenuProvider.class);

    @Override
    public void addToContextMenu(Control controlWithContext, ContextMenu contextMenu,
                                 ViewProperties viewProperties,
                                 ObservableValue<EntityFacade> conceptFocusProperty,
                                 SimpleIntegerProperty selectionIndexProperty,
                                 Runnable unlink) {
        if (viewProperties == null || conceptFocusProperty == null) {
            return;
        }
        EntityFacade entity = conceptFocusProperty.getValue();
        if (entity == null) {
            return;
        }
        try {
            Latest<EntityVersion> latest = viewProperties.calculator().latest(entity);
            if (!latest.isPresent()) {
                return;
            }
            ObservationRecord observation = new ObservationRecord(
                    Topic.COMPONENT_FOCUSED, latest.get(), Measures.present());
            ImmutableList<Consequence<?>> consequences = RuleService.get().execute(
                    "Component context menu", Lists.immutable.of(observation),
                    viewProperties, viewProperties.nodeView().editCoordinate());
            for (Consequence<?> consequence : consequences) {
                switch (consequence) {
                    case ConsequenceMenu consequenceMenu ->
                            contextMenu.getItems().add(consequenceMenu.generatedMenu());
                    case ConsequenceAction consequenceAction -> {
                        if (consequenceAction.generatedAction() instanceof Action action) {
                            contextMenu.getItems().add(ActionUtils.createMenuItem(action));
                        }
                    }
                    default -> {
                        // Unknown consequence type; ignore.
                    }
                }
            }
        } catch (RuntimeException e) {
            // No rule service available, or a rule failed — degrade to no rule items.
            LOG.debug("Rule-driven context menu items unavailable: {}", e.getMessage());
        }
    }
}
