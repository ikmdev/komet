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

import dev.ikm.komet.framework.observable.ObservableSemantic;
import dev.ikm.komet.framework.observable.ObservableSemanticSnapshot;
import dev.ikm.komet.framework.view.ViewProperties;
import dev.ikm.tinkar.coordinate.logic.PremiseType;
import javafx.scene.Node;
import org.controlsfx.control.PopOver;

import java.util.Optional;

/**
 * The definition popout (ike-issues#941): a transient {@link PopOver} hosting a nested classic
 * {@link AxiomView} of a concept's logical definition — the behaviour behind the
 * {@code LINK_EXTERNAL} open-concept affordance, extracted from {@code ClauseView} so the
 * classic axiom view's clause buttons and every at-rest {@code KonceptBadge} open the
 * <em>same</em> popover by construction.
 *
 * <p>Resolves the concept's axiom semantic under the given premise type through the view's
 * calculator; when no definition resolves, nothing opens — the affordance's caller gates on a
 * resolvable concept, and a race with a store change degrades to a silent no-op rather than an
 * empty popover.
 */
public final class AxiomPopover {

    private AxiomPopover() {
    }

    /**
     * Shows the definition popover for a concept, anchored at the given owner node.
     *
     * @param conceptNid     the concept whose definition to show
     * @param premiseType    the premise type to resolve the axiom semantic under
     * @param viewProperties the view used to resolve and render the definition
     * @param anchor         the node the popover attaches to (the clicked affordance)
     * @param screenX        the screen x of the invoking gesture
     * @param screenY        the screen y of the invoking gesture
     */
    public static void show(int conceptNid, PremiseType premiseType, ViewProperties viewProperties,
                            Node anchor, double screenX, double screenY) {
        Optional<ObservableSemanticSnapshot> optionalAxiomSnapshot =
                ObservableSemantic.getAxiomSnapshot(conceptNid, premiseType, viewProperties.calculator());

        optionalAxiomSnapshot.ifPresent(observableAxiomSnapshot ->
                observableAxiomSnapshot.getLatestVersion().ifPresent(observableSemanticVersion -> {
                    PopOver popover = new PopOver();
                    AxiomView axiomView = AxiomView.createWithCommitPanel(observableSemanticVersion,
                            premiseType, viewProperties);
                    popover.setContentNode(axiomView.getEditor());
                    popover.setCloseButtonEnabled(true);
                    popover.setHeaderAlwaysVisible(false);
                    popover.setTitle("");
                    popover.show(anchor, screenX, screenY);
                }));
    }
}
