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
package dev.ikm.komet.navigator.graph;

import javafx.scene.Node;
import javafx.scene.layout.HBox;
import dev.ikm.komet.framework.StyleClasses;
import dev.ikm.komet.framework.graphics.Icon;
import dev.ikm.tinkar.common.id.IntIdSet;
import dev.ikm.tinkar.coordinate.stamp.calculator.Latest;
import dev.ikm.tinkar.coordinate.view.calculator.ViewCalculator;
import dev.ikm.tinkar.entity.EntityVersion;
import dev.ikm.tinkar.terms.TinkarTerm;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * DefaultNavigatorDisplayPolicies
 *
 * 
 * @author <a href="mailto:joel.kniaz@gmail.com">Joel Kniaz</a>
 */
public class DefaultNavigatorDisplayPolicies implements NavigatorDisplayPolicies {
    private static final Logger LOG = LoggerFactory.getLogger(DefaultNavigatorDisplayPolicies.class);


    @Override
    public Node computeGraphic(MultiParentVertex item, ViewCalculator viewCalculator) {
        //TODO consider cases where an edge has more than one type...
        IntIdSet navigationConceptNids = viewCalculator.navigationCalculator().navigationCoordinate().navigationPatternNids();

        Node navigationGraphic = getNavigationGraphic(item);
        if (navigationConceptNids.size() > 1 && item.getTypeNids().contains(TinkarTerm.IS_A.nid()) && item.getOptionalParentNid().isPresent()) {
            // could be stated and inferred...
            if (navigationConceptNids.contains(viewCalculator.logicCoordinateRecord().inferredNavigationPatternNid()) &&
                    navigationConceptNids.contains(viewCalculator.logicCoordinateRecord().statedNavigationPatternNid())) {
                // Stated and inferred, need to indicate which.
                HBox combinedGraphic = new HBox(navigationGraphic);
                combinedGraphic.setSpacing(2);

                item.getOptionalParentNid().ifPresent(parentNid -> {
                    // Stated
                    int statedPatternNid = viewCalculator.logicCoordinateRecord().statedNavigationPatternNid();
                    if (navigationConceptNids.contains(statedPatternNid)) {
                        // See if the parent nid is in the stated navigation pattern...
                        if (viewCalculator.navigationCalculator().unsortedParentsOf(item.getConceptNid(), statedPatternNid).contains(parentNid)) {
                            Node navigationBadge = Icon.STATED.makeIconWithStyles(StyleClasses.NAVIGATION_BADGE.toString());
                            combinedGraphic.getChildren().add(navigationBadge);
                        }
                    }
                    // Inferred
                    int inferredPatternNid = viewCalculator.logicCoordinateRecord().inferredNavigationPatternNid();
                    if (navigationConceptNids.contains(inferredPatternNid)) {
                        // See if the parent nid is in the inferred navigation pattern...
                        if (viewCalculator.navigationCalculator().unsortedParentsOf(item.getConceptNid(), inferredPatternNid).contains(parentNid)) {
                            Node navigationBadge = Icon.INFERRED.makeIconWithStyles(StyleClasses.NAVIGATION_BADGE.toString());
                            combinedGraphic.getChildren().add(navigationBadge);
                        }
                    }
                });

                navigationGraphic = combinedGraphic;
            }
        }
        return navigationGraphic;
    }

    private Node getNavigationGraphic(MultiParentVertex item) {
        if (item.isRoot()) {
            // TODO get dynamic icons from Assemblages.
            if (item.getConceptNid() == TinkarTerm.PRIMORDIAL_PATH.nid()) {
                return Icon.SOURCE_BRANCH_1.makeIcon();
            } else if (item.getConceptNid() == TinkarTerm.PRIMORDIAL_MODULE.nid()) {
                return Icon.LINK_EXTERNAL.makeIcon();
            }
            return Icon.TAXONOMY_ROOT_ICON.makeIcon();
        }

        //TODO consider cases with more than one type nid...
        if (!item.getTypeNids().contains(TinkarTerm.IS_A.nid())) {
            // TODO get dynamic icons from Assemblages.
            if (item.getTypeNids().contains(TinkarTerm.PATH_ORIGINS_PATTERN.nid())) {
                return Icon.SOURCE_BRANCH_1.makeIcon();
            } else if (item.getTypeNids().contains(TinkarTerm.DEPENDENCY_MANAGEMENT_ASSEMBLAGE.nid())) {
                return Icon.LINK_EXTERNAL.makeIcon();
            }
            return Icon.ALERT_CONFIRM.makeIcon();
        }

        if (item.isDefined() && (item.isMultiParent() || item.getMultiParentDepth() > 0)) {
            if (item.isSecondaryParentOpened()) {
                return Icon.TAXONOMY_DEFINED_MULTIPARENT_OPEN.makeIcon();
            } else {
                return Icon.TAXONOMY_DEFINED_MULTIPARENT_CLOSED.makeIcon();
            }
        } else if (!item.isDefined() && (item.isMultiParent() || item.getMultiParentDepth() > 0)) {
            if (item.isSecondaryParentOpened()) {
                return Icon.TAXONOMY_PRIMITIVE_MULTIPARENT_OPEN.makeIcon();
            } else {
                return Icon.TAXONOMY_PRIMITIVE_MULTIPARENT_CLOSED.makeIcon();
            }
        } else if (item.isDefined() && !item.isMultiParent()) {
            return Icon.TAXONOMY_DEFINED_SINGLE_PARENT.makeIcon();
        }
        return Icon.TAXONOMY_PRIMITIVE_SINGLE_PARENT.makeIcon();
    }

    @Override
    public boolean shouldDisplay(MultiParentVertex treeItem, ViewCalculator viewCalculator) {
        if (treeItem.isRoot()) {
            return true;
        }
        int conceptNid = treeItem.getConceptNid();
        Latest<EntityVersion> latestVertexVersion = viewCalculator.vertexStampCalculator().latest(conceptNid);
        return latestVertexVersion.isPresent();
    }
}
