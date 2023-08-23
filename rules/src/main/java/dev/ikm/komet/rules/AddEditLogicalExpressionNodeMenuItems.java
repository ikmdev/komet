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
package dev.ikm.komet.rules;

/**
 *
 * @author kec
 */
public class AddEditLogicalExpressionNodeMenuItems {

/*

    public void changeFeatureTypeToNewSearchSelection() {
        if (this.axiomToEdit.getLogicalAxiomSemantic() == LogicalAxiomSemantic.FEATURE) {
            Action changeFeatureTypeToSearchSelection = new Action("Change feature type...", new CatchThrowableEventHandler((ActionEvent event) -> {
                showFindConceptPopup(this::changeFeatureTypeToNewSearchSelection, "Search for feature type replacement");
            }));
            actionItems.add(changeFeatureTypeToSearchSelection);
        } else {
            throw new IllegalStateException(this.axiomToEdit + " getLogicalAxiomSemantic() == LogicalAxiomSemantic.ROLE_SOME");
        }
    }
    private void changeFeatureTypeToNewSearchSelection(ActionEvent event) {
        hidePopover();
        if (this.findSelectedConceptSpecification.get() != null) {
            FeatureNodeWithNids featureNode = (FeatureNodeWithNids) axiomToEdit;
            featureNode.setTypeConceptNid(this.findSelectedConceptSpecification.get().getNid());
            updateExpression();
        }
    }
    public void changeFeatureUnitsToNewSearchSelection() {
        if (this.axiomToEdit.getLogicalAxiomSemantic() == LogicalAxiomSemantic.FEATURE) {
            Action changeFeatureUnitsToSearchSelection = new Action("Change feature units...", new CatchThrowableEventHandler((ActionEvent event) -> {
                showFindConceptPopup(this::changeFeatureUnitsToNewSearchSelection, "Search for feature units replacement");
            }));
            actionItems.add(changeFeatureUnitsToSearchSelection);
        } else {
            throw new IllegalStateException(this.axiomToEdit + " getLogicalAxiomSemantic() == LogicalAxiomSemantic.ROLE_SOME");
        }
    }
    private void changeFeatureUnitsToNewSearchSelection(ActionEvent event) {
        hidePopover();
        if (this.findSelectedConceptSpecification.get() != null) {
            FeatureNodeWithNids featureNode = (FeatureNodeWithNids) axiomToEdit;
            featureNode.setMeasureSemanticNid(this.findSelectedConceptSpecification.get().getNid());
            updateExpression();
        }
    }
    public void changeFeatureUnitsToRecentSelection() {
        if (this.axiomToEdit.getLogicalAxiomSemantic() == LogicalAxiomSemantic.FEATURE) {

            for (ActivityFeed feed: this.viewProperties.getActivityFeeds()) {
                List<Action> actions = new ArrayList<>();
                ActionGroup actionGroup = newActionGroup("Change feature units from " + feed.getFeedName() + " history",
                        ViewProperties.getOptionalGraphicForActivityFeed(feed), actions);
                for (IdentifiedObject historyRecord : feed.feedHistoryProperty()) {
                    Action addAction = new Action("Change feature units to " + manifoldCoordinate.getPreferredDescriptionText(historyRecord.getNid()), new CatchThrowableEventHandler((ActionEvent event) -> {
                        FeatureNodeWithNids featureNode = (FeatureNodeWithNids) this.axiomToEdit;
                        featureNode.setMeasureSemanticNid(historyRecord.getNid());
                        updateExpression();
                    }));
                    actionGroup.getActions().add(addAction);
                }
                if (!actionGroup.getActions().isEmpty()) {
                    actionItems.add(actionGroup);
                }
            }
        } else {
            throw new IllegalStateException(this.axiomToEdit + " getLogicalAxiomSemantic() == LogicalAxiomSemantic.FEATURE");
        }
    }

    public void changeFeatureRelationalOperator() {
        if (this.axiomToEdit.getLogicalAxiomSemantic() == LogicalAxiomSemantic.FEATURE) {
            List<Action> actions = new ArrayList<>();
            ActionGroup actionGroup = new ActionGroup("Change feature relational operator",  actions);
            for (ConcreteDomainOperators operator: ConcreteDomainOperators.values()) {
                Action addAction = new Action("Change relational operator to " + operator, new CatchThrowableEventHandler((ActionEvent event) -> {
                    FeatureNodeWithNids featureNode = (FeatureNodeWithNids) this.axiomToEdit;
                    featureNode.setOperator(operator);
                    updateExpression();
                }));
                actionGroup.getActions().add(addAction);
            }
            actionItems.add(actionGroup);
        } else {
            throw new IllegalStateException(this.axiomToEdit + " getLogicalAxiomSemantic() == LogicalAxiomSemantic.FEATURE");
        }
    }

    public void changeFeatureTypeToRecentSelection() {
        if (this.axiomToEdit.getLogicalAxiomSemantic() == LogicalAxiomSemantic.FEATURE) {

            for (ActivityFeed feed: this.viewProperties.getActivityFeeds()) {
                List<Action> actions = new ArrayList<>();
                ActionGroup actionGroup = newActionGroup("Change feature type from " + feed.getFeedName() + " history",
                        ViewProperties.getOptionalGraphicForActivityFeed(feed), actions);
                for (IdentifiedObject historyRecord : feed.feedHistoryProperty()) {
                    Action addAction = new Action("Change feature type to " + manifoldCoordinate.getPreferredDescriptionText(historyRecord.getNid()), new CatchThrowableEventHandler((ActionEvent event) -> {
                        FeatureNodeWithNids featureNode = (FeatureNodeWithNids) this.axiomToEdit;
                        featureNode.setTypeConceptNid(historyRecord.getNid());
                        updateExpression();
                    }));
                    actionGroup.getActions().add(addAction);
                }
                if (!actionGroup.getActions().isEmpty()) {
                    actionItems.add(actionGroup);
                }
            }
        } else {
            throw new IllegalStateException(this.axiomToEdit + " getLogicalAxiomSemantic() == LogicalAxiomSemantic.FEATURE");
        }
    }

    public void changeFeatureValue() {
        Action changeValue = new Action("Change feature value...", new CatchThrowableEventHandler((ActionEvent event) -> {

            FeatureNodeWithNids featureNode = (FeatureNodeWithNids) this.axiomToEdit;
            LiteralNodeDouble floatNode = (LiteralNodeDouble) featureNode.getOnlyChild();
            featureNode.getOnlyChild();
            SimpleFloatProperty floatProperty = new SimpleFloatProperty();
            floatProperty.set((float) floatNode.getLiteralValue());
            floatProperty.addListener((observable, oldValue, newValue) -> {
                floatNode.setLiteralValue(newValue.doubleValue());
                updateExpression();
            });
            PropertySheetItemFloatWrapper floatWrapper = new PropertySheetItemFloatWrapper("Value", floatProperty);
            PropertySheet propertySheet = new PropertySheet();
            propertySheet.getItems().add(floatWrapper);
            propertySheet.setSearchBoxVisible(false);


            PopOver valuePopOver = new PopOver();
            valuePopOver.getRoot().getStylesheets().add(FxGet.fxConfiguration().getUserCSSURL().toString());
            valuePopOver.getRoot().getStylesheets().add(IconographyHelper.getStyleSheetStringUrl());
            valuePopOver.setCloseButtonEnabled(true);
            valuePopOver.setHeaderAlwaysVisible(false);
            valuePopOver.setTitle("Enter new value");
            valuePopOver.setArrowLocation(PopOver.ArrowLocation.LEFT_TOP);
            valuePopOver.setContentNode(propertySheet);
            valuePopOver.show(mouseEvent.getPickResult().getIntersectedNode());
        }));
        actionItems.add(changeValue);
    }

    public void addFloatFeatureAction() {
        StringBuilder builder = new StringBuilder();
        builder.append("Add feature...");
        Action addNewRoleAction = new Action(builder.toString(), new CatchThrowableEventHandler((ActionEvent event) -> {
            showFindConceptPopup(this::addFeatureTypeFromSelection, "Select feature type concept");
        }));
        actionItems.add(addNewRoleAction);
    }
    private void addFeatureTypeFromSelection(ActionEvent event) {
        hidePopover();
        if (this.findSelectedConceptSpecification.get() != null) {

            FeatureNodeWithNids newFeature = logicalExpression.Feature(this.findSelectedConceptSpecification.get().getNid(),
                    MetaData.MEASURE_SEMANTIC____SOLOR.getNid(), ConcreteDomainOperators.EQUALS, logicalExpression.DoubleLiteral(0.0));
            for (LogicNode node : axiomToEdit.getChildren()) {
                if (node.getLogicalAxiomSemantic() == LogicalAxiomSemantic.AND) {
                    node.addChildren(newFeature);
                    break;
                }
            }
            updateExpression();
        }
    }


    public void addFloatFeatureAction(ConceptSpecification typeSpec, ConceptSpecification measureSemanticNid, ConcreteDomainOperators operator) {
        addFloatFeatureAction(typeSpec.getNid(), measureSemanticNid.getNid(), operator);
    }

    public void addFeatureTypeFromRecentHistory() {

        // create action group for each
        for (ActivityFeed feed: this.viewProperties.getActivityFeeds()) {
            List<Action> actions = new ArrayList<>();
            ActionGroup actionGroup = newActionGroup("Add feature type from " + feed.getFeedName() + " history",
                    ViewProperties.getOptionalGraphicForActivityFeed(feed), actions);
            for (IdentifiedObject historyRecord : feed.feedHistoryProperty()) {
                Action roleTypeAction = new Action("Add feature type " + manifoldCoordinate.getPreferredDescriptionText(historyRecord.getNid()), new CatchThrowableEventHandler((ActionEvent event) -> {
                    FeatureNodeWithNids newFeature = logicalExpression.Feature(historyRecord.getNid(),
                            MetaData.MEASURE_SEMANTIC____SOLOR.getNid(), ConcreteDomainOperators.EQUALS, logicalExpression.DoubleLiteral(0.0));
                    for (LogicNode node : axiomToEdit.getChildren()) {
                        if (node.getLogicalAxiomSemantic() == LogicalAxiomSemantic.AND) {
                            node.addChildren(newFeature);
                            break;
                        }
                    }
                    updateExpression();
                }));
                actionGroup.getActions().add(roleTypeAction);
            }
            if (!actionGroup.getActions().isEmpty()) {
                actionItems.add(actionGroup);
            }
        }
    }
    public void addFloatFeatureAction(int typeNid, int measureSemanticNid, ConcreteDomainOperators operator) {
        StringBuilder builder = new StringBuilder();
        builder.append("Add ⒡ ");
        builder.append(manifoldCoordinate.getPreferredDescriptionText(typeNid));
        builder.append(" ");
        builder.append(operator);
        builder.append(" 0.0 ");
        builder.append(manifoldCoordinate.getPreferredDescriptionText(measureSemanticNid));
        Action addFeatureAction = new Action(builder.toString(), new CatchThrowableEventHandler((ActionEvent event) -> {
            FeatureNodeWithNids newRole = logicalExpression.Feature(typeNid,
                    measureSemanticNid, operator, logicalExpression.DoubleLiteral(0.0));
            for (LogicNode node : axiomToEdit.getChildren()) {
                if (node.getLogicalAxiomSemantic() == LogicalAxiomSemantic.AND) {
                    node.addChildren(newRole);
                    break;
                }
            }
            updateExpression();
        }));
        actionItems.add(addFeatureAction);
    }
*/

}
