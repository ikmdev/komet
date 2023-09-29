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
package dev.ikm.komet.framework.panel.concept;


import dev.ikm.tinkar.coordinate.stamp.change.ChangeChronology;
import dev.ikm.tinkar.entity.EntityService;
import javafx.beans.property.SimpleObjectProperty;
import javafx.collections.ObservableSet;
import dev.ikm.komet.framework.PseudoClasses;
import dev.ikm.komet.framework.observable.ObservableConcept;
import dev.ikm.komet.framework.observable.ObservableConceptSnapshot;
import dev.ikm.komet.framework.observable.ObservableConceptVersion;
import dev.ikm.komet.framework.panel.ComponentIsFinalPanel;
import dev.ikm.komet.framework.view.ViewProperties;
import dev.ikm.tinkar.entity.ConceptVersionRecord;
import dev.ikm.tinkar.terms.EntityFacade;
import javafx.scene.control.ContextMenu;
import javafx.scene.control.MenuItem;

public class ConceptPanel extends ComponentIsFinalPanel<
        ObservableConceptSnapshot,
        ObservableConcept,
        ObservableConceptVersion,
        ConceptVersionRecord> {

    public ConceptPanel(ObservableConceptSnapshot conceptEntity,
                        ViewProperties viewProperties,
                        SimpleObjectProperty<EntityFacade> topEnclosingComponentProperty,
                        ObservableSet<Integer> referencedNids) {
        super(conceptEntity, viewProperties, topEnclosingComponentProperty, referencedNids);
        this.collapsiblePane.setText("Concept panel");
        this.getComponentPanelBox().pseudoClassStateChanged(PseudoClasses.CONCEPT_PSEUDO_CLASS, true);
        this.getComponentDetailPane().pseudoClassStateChanged(PseudoClasses.CONCEPT_PSEUDO_CLASS, true);
        ContextMenu contextMenu = new ContextMenu();
        collapsiblePane.setContextMenu(contextMenu);
        MenuItem versionChronologyMenuItem = new MenuItem("Version chronology for: " +
                viewProperties.calculator().getPreferredDescriptionTextWithFallbackOrNid(conceptEntity.nid()));
        versionChronologyMenuItem.setOnAction(event -> {
            ChangeChronology changeChronology = this.viewProperties.calculator().changeChronology(conceptEntity.nid());
            System.out.println(changeChronology.toString(viewProperties.calculator(), true));
            System.out.println(changeChronology.toString(viewProperties.calculator(), false));
        });
        contextMenu.getItems().add(versionChronologyMenuItem);
        MenuItem versionChronologyRecursiveMenuItem = new MenuItem("Recursive version chronology for: " +
                viewProperties.calculator().getPreferredDescriptionTextWithFallbackOrNid(conceptEntity.nid()));
        versionChronologyRecursiveMenuItem.setOnAction(event -> {
            recursiveVersionChronology(conceptEntity.nid(), viewProperties);
        });
        contextMenu.getItems().add(versionChronologyRecursiveMenuItem);
    }

    private void recursiveVersionChronology(int versionNid, ViewProperties viewProperties) {
        ChangeChronology changeChronology = this.viewProperties.calculator().changeChronology(versionNid);
        System.out.println(changeChronology.toString(viewProperties.calculator(), true));
        for (int semanticNid: EntityService.get().semanticNidsForComponent(versionNid)) {
            recursiveVersionChronology(semanticNid, viewProperties);
        }
    }
}
