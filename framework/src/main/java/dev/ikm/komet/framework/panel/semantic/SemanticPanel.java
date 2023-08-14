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
package dev.ikm.komet.framework.panel.semantic;

import javafx.beans.property.SimpleObjectProperty;
import javafx.collections.ObservableSet;
import javafx.geometry.Pos;
import javafx.scene.control.ContextMenu;
import javafx.scene.control.MenuItem;
import javafx.scene.layout.HBox;
import dev.ikm.komet.framework.PseudoClasses;
import dev.ikm.komet.framework.observable.ObservableSemantic;
import dev.ikm.komet.framework.observable.ObservableSemanticSnapshot;
import dev.ikm.komet.framework.observable.ObservableSemanticVersion;
import dev.ikm.komet.framework.panel.ComponentIsFinalPanel;
import dev.ikm.komet.framework.view.ViewProperties;
import dev.ikm.tinkar.coordinate.stamp.calculator.Latest;
import dev.ikm.tinkar.entity.PatternEntityVersion;
import dev.ikm.tinkar.entity.SemanticVersionRecord;
import dev.ikm.tinkar.terms.EntityFacade;
import dev.ikm.tinkar.terms.TinkarTerm;

public class SemanticPanel extends ComponentIsFinalPanel<
        ObservableSemanticSnapshot,
        ObservableSemantic,
        ObservableSemanticVersion,
        SemanticVersionRecord> {

    public SemanticPanel(ObservableSemanticSnapshot semanticSnapshot, ViewProperties viewProperties, SimpleObjectProperty<EntityFacade> topEnclosingComponentProperty, ObservableSet<Integer> referencedNids) {
        super(semanticSnapshot, viewProperties, topEnclosingComponentProperty, referencedNids);
        Latest<PatternEntityVersion> latestPatternVersion = viewProperties.calculator().latestPatternEntityVersion(semanticSnapshot.patternNid());

        latestPatternVersion.ifPresent(patternEntityVersion -> {
            StringBuilder sb = new StringBuilder("[");
            sb.append(viewProperties.calculator().getPreferredDescriptionTextWithFallbackOrNid(patternEntityVersion.semanticMeaningNid()));
            if (patternEntityVersion.semanticMeaningNid() != patternEntityVersion.semanticPurposeNid()) {
                sb.append("] of component for [");
                sb.append(viewProperties.calculator().getPreferredDescriptionTextWithFallbackOrNid(patternEntityVersion.semanticPurposeNid()));
            }
            sb.append("] in [");
            sb.append(viewProperties.calculator().getPreferredDescriptionTextWithFallbackOrNid(patternEntityVersion.nid()));
            sb.append("]");
            collapsiblePane.setText(sb.toString());
            HBox referencedComponentInfo = new HBox(3);
            referencedComponentInfo.setAlignment(Pos.CENTER_LEFT);

            ContextMenu contextMenu = new ContextMenu();
            collapsiblePane.setContextMenu(contextMenu);
            MenuItem patternMenuItem = new MenuItem("Focus on pattern: " +
                    viewProperties.calculator().getPreferredDescriptionTextWithFallbackOrNid(patternEntityVersion.nid()));
            patternMenuItem.setOnAction(event -> {
                topEnclosingComponentProperty.setValue(patternEntityVersion.chronology());
            });
            contextMenu.getItems().add(patternMenuItem);

            MenuItem topEnclosingComponentMenuItem = new MenuItem("Focus on top enclosing component: " +
                    viewProperties.calculator().getPreferredDescriptionTextWithFallbackOrNid(semanticSnapshot.observableEntity().topEnclosingComponentNid()));
            topEnclosingComponentMenuItem.setOnAction(event -> {
                topEnclosingComponentProperty.setValue(semanticSnapshot.observableEntity().topEnclosingComponent());
            });
            contextMenu.getItems().add(topEnclosingComponentMenuItem);

        });


        if (semanticSnapshot.patternNid() == TinkarTerm.EL_PLUS_PLUS_STATED_AXIOMS_PATTERN.nid() ||
                semanticSnapshot.patternNid() == TinkarTerm.EL_PLUS_PLUS_INFERRED_AXIOMS_PATTERN.nid()) {
            this.getComponentPanelBox().pseudoClassStateChanged(PseudoClasses.LOGICAL_DEFINITION_PSEUDO_CLASS, true);
            this.getComponentDetailPane().pseudoClassStateChanged(PseudoClasses.LOGICAL_DEFINITION_PSEUDO_CLASS, true);
        } else if (semanticSnapshot.patternNid() == TinkarTerm.DESCRIPTION_PATTERN.nid()) {
            this.getComponentPanelBox().pseudoClassStateChanged(PseudoClasses.DESCRIPTION_PSEUDO_CLASS, true);
            this.getComponentDetailPane().pseudoClassStateChanged(PseudoClasses.DESCRIPTION_PSEUDO_CLASS, true);
        } else {
            this.getComponentPanelBox().pseudoClassStateChanged(PseudoClasses.SEMANTIC_PSEUDO_CLASS, true);
            this.getComponentDetailPane().pseudoClassStateChanged(PseudoClasses.SEMANTIC_PSEUDO_CLASS, true);
        }
    }
}
