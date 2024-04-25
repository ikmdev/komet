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
package dev.ikm.komet.amplify.lidr.viewmodels;

import dev.ikm.komet.framework.panel.axiom.LogicalOperatorsForVertex;
import dev.ikm.komet.framework.view.ObservableView;
import dev.ikm.komet.framework.view.ViewProperties;
import dev.ikm.komet.framework.window.WindowSettings;
import dev.ikm.komet.preferences.KometPreferences;
import dev.ikm.komet.preferences.KometPreferencesImpl;
import dev.ikm.tinkar.common.id.PublicId;
import dev.ikm.tinkar.common.id.PublicIds;
import dev.ikm.tinkar.component.Concept;
import dev.ikm.tinkar.component.graph.DiTree;
import dev.ikm.tinkar.component.graph.Vertex;
import dev.ikm.tinkar.coordinate.navigation.calculator.NavigationCalculator;
import dev.ikm.tinkar.coordinate.stamp.calculator.RelativePosition;
import dev.ikm.tinkar.coordinate.stamp.calculator.StampCalculator;
import dev.ikm.tinkar.entity.EntityService;
import dev.ikm.tinkar.entity.StampEntity;
import dev.ikm.tinkar.entity.StampEntityVersion;
import dev.ikm.tinkar.terms.EntityFacade;
import dev.ikm.tinkar.terms.TinkarTerm;
import org.eclipse.collections.api.list.ImmutableList;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Optional;
import java.util.UUID;
import java.util.concurrent.atomic.AtomicReference;


public class ViewModelHelper {
    private static final Logger LOG = LoggerFactory.getLogger(ViewModelHelper.class);

    public static Optional<Concept> findDeviceManufacturer(PublicId pubId) {
        return findDeviceManufacturer(viewPropertiesNode().calculator().navigationCalculator(), pubId);
    }

    public static Optional<Concept> findDeviceManufacturer(NavigationCalculator navCalc, PublicId pubId) {
        // TODO: Access Manufacturer PublicId in a more maintainable way
        PublicId manufacturerId = PublicIds.of(UUID.nameUUIDFromBytes("Manufactured By".getBytes()));
        int componentNid = EntityService.get().nidForPublicId(pubId);
        StampCalculator stampCalculator = navCalc.stampCalculator();
        AtomicReference<StampEntity<StampEntityVersion>> latestStamp = new AtomicReference<>();
        AtomicReference<DiTree<Vertex>> latestInferredDefinitionSemanticVersion = new AtomicReference<>();

        for (int navigationPatternNid : navCalc.navigationCoordinate().navigationPatternNids().toArray()) {
            int logicalDefintionPatternNid =
                    navigationPatternNid != TinkarTerm.STATED_NAVIGATION.nid() ?
                    TinkarTerm.EL_PLUS_PLUS_INFERRED_AXIOMS_PATTERN.nid() : TinkarTerm.EL_PLUS_PLUS_STATED_AXIOMS_PATTERN.nid();

            EntityService.get().forEachSemanticForComponentOfPattern(componentNid, logicalDefintionPatternNid, (semanticEntity) -> {
                stampCalculator.latest(semanticEntity)
                        .ifPresent((semanticEntityVersion) -> {
                            if (latestStamp.get() == null) {
                                latestStamp.set(semanticEntityVersion.stamp());
                                latestInferredDefinitionSemanticVersion.set((DiTree) semanticEntityVersion.fieldValues().get(0));
                            } else {
                                if (RelativePosition.AFTER == stampCalculator.relativePosition(semanticEntityVersion.stampNid(), latestStamp.get().nid())) {
                                    latestStamp.set(semanticEntityVersion.stamp());
                                    latestInferredDefinitionSemanticVersion.set((DiTree) semanticEntityVersion.fieldValues().get(0));
                                }
                            }
                        });
            });
        }
        return findConceptReferenceForRoleType(latestInferredDefinitionSemanticVersion.get(), manufacturerId);
    }

    private static Optional<Concept> findConceptReferenceForRoleType(DiTree<Vertex> logicalDefinition, PublicId roleTypeToFind) {
        ImmutableList<Vertex> vertexList = logicalDefinition.vertexMap();
        for (Vertex vertex : vertexList) {
            if (LogicalOperatorsForVertex.ROLE.semanticallyEqual((EntityFacade) vertex.meaning())) {
                Concept roleTypeProperty = vertex.propertyAsConcept(TinkarTerm.ROLE_TYPE).get();
                if (roleTypeProperty.equals(roleTypeToFind)) {
                    Vertex manufacturerVertex = logicalDefinition.successors(vertex).get(0);
                    return manufacturerVertex.propertyAsConcept(TinkarTerm.CONCEPT_REFERENCE);
                }
            }
        }
        return Optional.empty();
    }

    public static ObservableView viewPropertiesNode() {
        // TODO how do we get a viewProperties?
        KometPreferences appPreferences = KometPreferencesImpl.getConfigurationRootPreferences();
        KometPreferences windowPreferences = appPreferences.node("main-komet-window");
        WindowSettings windowSettings = new WindowSettings(windowPreferences);
        ViewProperties viewProperties = windowSettings.getView().makeOverridableViewProperties();
        return viewProperties.nodeView();
    }

}
