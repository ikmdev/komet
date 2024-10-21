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

import dev.ikm.komet.framework.Dialogs;
import dev.ikm.komet.framework.MenuItemWithText;
import dev.ikm.komet.framework.StyleClasses;
import dev.ikm.komet.framework.controls.EntityLabel;
import dev.ikm.komet.framework.dnd.DragImageMaker;
import dev.ikm.komet.framework.dnd.KometClipboard;
import dev.ikm.komet.framework.docbook.DocBook;
import dev.ikm.komet.framework.graphics.Icon;
import dev.ikm.komet.framework.observable.ObservableSemantic;
import dev.ikm.komet.framework.observable.ObservableSemanticSnapshot;
import dev.ikm.komet.framework.performance.Measures;
import dev.ikm.komet.framework.performance.Topic;
import dev.ikm.komet.framework.performance.impl.ObservationRecord;
import dev.ikm.komet.framework.rulebase.Consequence;
import dev.ikm.komet.framework.rulebase.ConsequenceAction;
import dev.ikm.komet.framework.rulebase.ConsequenceMenu;
import dev.ikm.komet.framework.rulebase.RuleService;
import dev.ikm.komet.framework.view.ViewProperties;
import dev.ikm.tinkar.common.id.IntIdList;
import dev.ikm.tinkar.common.id.PublicId;
import dev.ikm.tinkar.common.service.PrimitiveData;
import dev.ikm.tinkar.common.util.text.NaturalOrder;
import dev.ikm.tinkar.common.util.time.DateTimeUtil;
import dev.ikm.tinkar.component.Concept;
import dev.ikm.tinkar.component.graph.DiTree;
import dev.ikm.tinkar.coordinate.Coordinates;
import dev.ikm.tinkar.coordinate.logic.PremiseType;
import dev.ikm.tinkar.coordinate.stamp.calculator.Latest;
import dev.ikm.tinkar.coordinate.view.calculator.ViewCalculator;
import dev.ikm.tinkar.entity.Entity;
import dev.ikm.tinkar.entity.EntityVersion;
import dev.ikm.tinkar.entity.SemanticEntityVersion;
import dev.ikm.tinkar.entity.graph.DiTreeEntity;
import dev.ikm.tinkar.entity.graph.EntityVertex;
import dev.ikm.tinkar.terms.ConceptFacade;
import dev.ikm.tinkar.terms.EntityFacade;
import dev.ikm.tinkar.terms.State;
import dev.ikm.tinkar.terms.TinkarTerm;
import javafx.beans.property.SimpleBooleanProperty;
import javafx.event.Event;
import javafx.geometry.Bounds;
import javafx.geometry.Insets;
import javafx.scene.control.*;
import javafx.scene.input.*;
import javafx.scene.layout.*;
import javafx.scene.paint.Color;
import org.controlsfx.control.PopOver;
import org.controlsfx.control.action.Action;
import org.controlsfx.control.action.ActionGroup;
import org.controlsfx.control.action.ActionUtils;
import org.eclipse.collections.api.factory.Lists;
import org.eclipse.collections.api.list.ImmutableList;
import org.eclipse.collections.api.list.MutableList;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.Set;

import static dev.ikm.komet.framework.PseudoClasses.INACTIVE_PSEUDO_CLASS;
import static dev.ikm.komet.framework.panel.axiom.AxiomView.*;
import static dev.ikm.komet.framework.panel.axiom.LogicalOperatorsForVertex.CONCEPT;
import static dev.ikm.komet.framework.panel.axiom.LogicalOperatorsForVertex.FEATURE;
import static dev.ikm.tinkar.coordinate.logic.PremiseType.STATED;
import static dev.ikm.tinkar.terms.TinkarTerm.CONCEPT_REFERENCE;

/**
 * Each clause in an axiom is presented with the ClauseView.
 */
public class ClauseView {
    private static final Logger LOG = LoggerFactory.getLogger(ClauseView.class);

    protected final AxiomView axiomView;
    protected final EntityVertex axiomVertex;
    protected final Label titleLabel = new Label();
    protected final BorderPane rootBorderPane = new BorderPane();
    protected final GridPane rootGridPane = new GridPane();
    protected final Button editButton = new Button("");
    protected final ToggleButton expandButton = new ToggleButton("", Icon.OPEN.makeIcon());
    protected final List<ClauseView> childClauses = new ArrayList<>();
    protected final SimpleBooleanProperty expanded = new SimpleBooleanProperty(true);
    protected final VBox childBox = new VBox();
    protected PopOver popover;
    Button openConceptButton = new Button("", Icon.LINK_EXTERNAL.makeIcon());
    TransferMode[] transferMode = null;
    Background originalBackground;
    boolean editable = false;
    boolean addChildren = true;

    public ClauseView(EntityVertex axiomVertex, AxiomView axiomView) {
        this.axiomVertex = axiomVertex;
        this.axiomView = axiomView;
        this.axiomView.setPremiseTypePseudoClasses(rootBorderPane);
        rootGridPane.setBorder(TOOL_BAR_BORDER);
        rootGridPane.setPadding(new Insets(2));
        rootBorderPane.setBorder(INNER_ROOT_BORDER);
        rootBorderPane.setPadding(new Insets(3));
        expandButton.setPadding(Insets.EMPTY);
        expandButton.selectedProperty().bindBidirectional(expanded);
        expanded.addListener((observable, oldValue, newValue) -> {
            this.toggleExpansion();
        });
        editButton.getStyleClass().add("pencil-button");
        editButton.setOnMousePressed(this::handleEditClick);

        titleLabel.getStyleClass().add("komet-version-general-cell");
        titleLabel.setOnDragOver(this::handleDragOver);
        titleLabel.setOnDragEntered(this::handleDragEntered);
        titleLabel.setOnDragDetected(this::handleDragDetected);
        titleLabel.setOnDragExited(this::handleDragExited);
        titleLabel.setOnDragDone(this::handleDragDone);
        //titleLabel.setMaxWidth(425);
        LogicalOperatorsForVertex vertexLogicalOperator = LogicalOperatorsForVertex.get(axiomVertex);
        switch (vertexLogicalOperator) {
            case CONCEPT -> setupForConcept();
            case FEATURE -> setupForFeature();
            case ROLE -> {
                ConceptFacade roleOperator = axiomVertex.propertyFast(TinkarTerm.ROLE_OPERATOR);
                if (roleOperator.nid() == TinkarTerm.EXISTENTIAL_RESTRICTION.nid()) {
                    setupForRoleSome();
                } else if (roleOperator.nid() == TinkarTerm.UNIVERSAL_RESTRICTION.nid()) {
                    setupForRoleAll();
                }
            }
            case NECESSARY_SET -> setupForNecessarySet();
            case SUFFICIENT_SET -> setupForSufficientSet();
            case INCLUSION_SET -> setupForInclusionSet();
            case DEFINITION_ROOT -> setupForDefinitionRoot();
            case PROPERTY_SET -> setupForPropertySet();
            case PROPERTY_PATTERN_IMPLICATION -> setupForPropertyPatternImplication();
        }

        rootBorderPane.setPadding(new Insets(2, 0, 0, 0));
        rootBorderPane.setTop(rootGridPane);
        this.axiomView.setPremiseTypePseudoClasses(childBox);
        childBox.setBorder(CHILD_BOX_BORDER);
        childBox.setPadding(new Insets(0, 10, 0, 10));
        if (addChildren) {
            for (EntityVertex childNode : axiomTree().successors(axiomVertex)) {
                switch (LogicalOperatorsForVertex.get(childNode)) {
                    case AND -> {
                        for (EntityVertex andChildNode : axiomTree().successors(childNode)) {
                            ClauseView andChildClause = new ClauseView(andChildNode, axiomView);
                            childClauses.add(andChildClause);
                        }
                    }
                    default -> {
                        ClauseView childClause = new ClauseView(childNode, axiomView);
                        childClauses.add(childClause);
                    }
                }
            }
        }

        childClauses.sort(new AxiomComparator(this.axiomTree(), this.viewProperties()));
        for (ClauseView childClause : childClauses) {
            childBox.getChildren().add(childClause.rootBorderPane);
        }
        rootBorderPane.setCenter(childBox);
        rootBorderPane.setUserData(axiomVertex);
    }

    private void setupForPropertyPatternImplication() {
        // TODO get CSS and related gui setup.
        // TODO get style class for property set
        if (this.axiomView.premiseType == STATED) {
            editable = true;
        }
        // TODO, when we move LogicalExpression to tinkar-core, then also add logical expression, and use the logical expression...
        rootBorderPane.getStyleClass()
                .add(StyleClasses.DEF_FEATURE.toString());
        int column = 0;
        openConceptButton.getStyleClass().setAll(StyleClasses.OPEN_CONCEPT_BUTTON.toString());
        this.axiomView.addToGridPaneNoGrowTopAlign(rootGridPane, openConceptButton, column++);
        openConceptButton.setOnMouseClicked(this::handleShowFeatureNodeClick);
        StringBuilder builder = new StringBuilder();
        builder.append("π: ");
        Optional<IntIdList> optionalPropertyPattern = this.axiomVertex.property(TinkarTerm.PROPERTY_SEQUENCE);
        optionalPropertyPattern.ifPresent(propertyPattern -> {
            for (int propertyPatternNid : propertyPattern.intStream().toArray()) {
                builder.append("[" + calculator().getPreferredDescriptionTextWithFallbackOrNid(propertyPatternNid) + "] ");
            }
        });
        builder.append("⇒ ");
        Optional<ConceptFacade> optionalImplication = this.axiomVertex.propertyAsConcept(TinkarTerm.PROPERTY_PATTERN_IMPLICATION);
        optionalImplication.ifPresent(implication -> {
            builder.append("[" + calculator().getPreferredDescriptionTextWithFallbackOrNid(implication.nid()) + "] ");
        });
        titleLabel.setText(builder.toString());
        this.axiomView.addToGridPaneGrow(rootGridPane, titleLabel, column++);
        if (this.axiomView.premiseType == STATED) {
            this.axiomView.addToGridPaneNoGrow(rootGridPane, editButton, column++);
        }
    }

    private void setupForPropertySet() {
        // TODO get CSS and related gui setup.
        // TODO get style class for property set
        rootBorderPane.getStyleClass()
                .add(StyleClasses.DEF_SUFFICIENT_SET.toString());
        titleLabel.setText("Property axioms");
        titleLabel.setGraphic(Icon.ALERT_WARN2.makeIcon());
        int column = 0;
        this.axiomView.addToGridPaneNoGrow(rootGridPane, expandButton, column++);
        this.axiomView.addToGridPaneGrow(rootGridPane, titleLabel, column++);
        if (this.axiomView.premiseType == STATED) {
            this.axiomView.addToGridPaneNoGrow(rootGridPane, editButton, column++);
        }
    }

    private void setupForLiteralString(String stringLiteral) {
        rootBorderPane.getStyleClass()
                .add(StyleClasses.DEF_LITERAL.toString());
        titleLabel.setText(stringLiteral);
        titleLabel.setGraphic(Icon.LITERAL_STRING.makeIcon());
        int column = 0;
        this.axiomView.addToGridPaneGrow(rootGridPane, titleLabel, column++);
    }

    private void setupForLiteralInteger(Integer integerLiteral) {
        rootBorderPane.getStyleClass()
                .add(StyleClasses.DEF_LITERAL.toString());
        titleLabel.setText(Integer.toString(integerLiteral));
        titleLabel.setGraphic(Icon.LITERAL_NUMERIC.makeIcon());
        int column = 0;
        this.axiomView.addToGridPaneGrow(rootGridPane, titleLabel, column++);
    }

    private void setupForLiteralInstant(Instant instantLiteral) {
        rootBorderPane.getStyleClass()
                .add(StyleClasses.DEF_LITERAL.toString());
        titleLabel.setText(DateTimeUtil.format(instantLiteral));
        titleLabel.setGraphic(Icon.LITERAL_NUMERIC.makeIcon());
        int column = 0;
        this.axiomView.addToGridPaneGrow(rootGridPane, titleLabel, column++);
    }

    private void setupForLiteralBoolean(Boolean booleanLiteral) {
        rootBorderPane.getStyleClass()
                .add(StyleClasses.DEF_LITERAL.toString());
        titleLabel.setText(Boolean.toString(booleanLiteral));
        titleLabel.setGraphic(Icon.LITERAL_NUMERIC.makeIcon());
        int column = 0;
        this.axiomView.addToGridPaneGrow(rootGridPane, titleLabel, column++);
    }

    private void setupForLiteralFloat(Float floatLiteral) {
        rootBorderPane.getStyleClass()
                .add(StyleClasses.DEF_LITERAL.toString());
        titleLabel.setText(Float.toString(floatLiteral));
        titleLabel.setGraphic(Icon.LITERAL_NUMERIC.makeIcon());
        int column = 0;
        this.axiomView.addToGridPaneGrow(rootGridPane, titleLabel, column++);
    }

    private void setupForDefinitionRoot() {
        // TODO should never be null, need better handling.
        if (this.axiomTreeSemanticVersion() != null) {
            rootBorderPane.getStyleClass()
                    .add(StyleClasses.DEF_ROOT.toString());
            rootBorderPane.setBorder(ROOT_BORDER);
            titleLabel.setText(axiomView.getEntityForAxiomsText(null));
            Latest<EntityVersion> latest = calculator().latest(axiomView.getEntityBeingDefinedNid());
            if (latest.isPresent()) {
                titleLabel.setGraphic(computeGraphic(axiomTreeSemanticVersion().referencedComponentNid(), false,
                        latest.get().stamp().state(), viewProperties(), this.axiomView.premiseType));
                rootBorderPane.pseudoClassStateChanged(INACTIVE_PSEUDO_CLASS, !latest.get().active());
                titleLabel.pseudoClassStateChanged(INACTIVE_PSEUDO_CLASS, !latest.get().active());
                rootGridPane.pseudoClassStateChanged(INACTIVE_PSEUDO_CLASS, !latest.get().active());
            } else {
                titleLabel.setGraphic(computeGraphic(axiomTreeSemanticVersion().referencedComponentNid(), false,
                        State.PRIMORDIAL, viewProperties(), this.axiomView.premiseType));
            }

            titleLabel.setContextMenu(getContextMenu());
            int column = 0;
            this.axiomView.addToGridPaneNoGrow(rootGridPane, expandButton, column++);
            this.axiomView.addToGridPaneGrow(rootGridPane, titleLabel, column++);
            if (this.axiomView.premiseType == STATED) {
                this.axiomView.addToGridPaneNoGrow(rootGridPane, editButton, column++);
            }
        } else {
            LOG.info("TODO should never be null, need better handling. 39");
        }

    }

    private void setupForInclusionSet() {
        rootBorderPane.getStyleClass()
                .add(StyleClasses.DEF_INCLUSION_SET.toString());
        titleLabel.setText(axiomView.getEntityForAxiomsText(
                calculator().getPreferredDescriptionTextWithFallbackOrNid(axiomVertex.getMeaningNid())));
        titleLabel.setGraphic(Icon.TAXONOMY_DEFINED_SINGLE_PARENT.makeIcon());
        int column = 0;
        this.axiomView.addToGridPaneNoGrow(rootGridPane, expandButton, column++);
        this.axiomView.addToGridPaneGrow(rootGridPane, titleLabel, column++);
        if (this.axiomView.premiseType == STATED) {
            this.axiomView.addToGridPaneNoGrow(rootGridPane, editButton, column++);
        }
    }

    private void setupForSufficientSet() {
        rootBorderPane.getStyleClass()
                .add(StyleClasses.DEF_SUFFICIENT_SET.toString());
        titleLabel.setText(axiomView.getEntityForAxiomsText(
                calculator().getPreferredDescriptionTextWithFallbackOrNid(axiomVertex.getMeaningNid())));
        titleLabel.setGraphic(Icon.TAXONOMY_DEFINED_SINGLE_PARENT.makeIcon());
        int column = 0;
        this.axiomView.addToGridPaneNoGrow(rootGridPane, expandButton, column++);
        this.axiomView.addToGridPaneGrow(rootGridPane, titleLabel, column++);
        if (this.axiomView.premiseType == STATED) {
            this.axiomView.addToGridPaneNoGrow(rootGridPane, editButton, column++);
        }
    }

    private void setupForNecessarySet() {
        rootBorderPane.getStyleClass()
                .add(StyleClasses.DEF_NECESSARY_SET.toString());
        titleLabel.setText(axiomView.getEntityForAxiomsText(
                calculator().getPreferredDescriptionTextWithFallbackOrNid(axiomVertex.getMeaningNid())
        ));
        titleLabel.setGraphic(Icon.TAXONOMY_ROOT_ICON.makeIcon());
        int column = 0;
        this.axiomView.addToGridPaneNoGrow(rootGridPane, expandButton, column++);
        this.axiomView.addToGridPaneGrow(rootGridPane, titleLabel, column++);
        if (this.axiomView.premiseType == STATED) {
            this.axiomView.addToGridPaneNoGrow(rootGridPane, editButton, column++);
        }
    }

    private void setupForRoleSome() {
        int column = 0;
        ConceptFacade roleType = axiomVertex.propertyFast(TinkarTerm.ROLE_TYPE);
        if (PublicId.equals(roleType.publicId(), TinkarTerm.ROLE_GROUP)) {
            expanded.set(true);
            rootBorderPane.getStyleClass().add(StyleClasses.DEF_ROLE_GROUP.toString());
            titleLabel.setGraphic(Icon.ROLE_GROUP.makeIcon());
            ImmutableList<EntityVertex> descendents = axiomTree().descendents(axiomVertex);
            // apply sort here for particular cases...

            MutableList<String> descendentConceptDescriptions = Lists.mutable.ofInitialCapacity(descendents.size());
            for (EntityVertex descendentNode : descendents) {
                if (CONCEPT_REFERENCE.nid() == descendentNode.getMeaningNid()) {
                    ConceptFacade vertexConcept = CONCEPT.getPropertyFast(descendentNode);
                    descendentConceptDescriptions.add("[" + calculator().getPreferredDescriptionTextWithFallbackOrNid(vertexConcept) +
                            "] ");
                }
            }
            descendentConceptDescriptions.sort(NaturalOrder.getStringComparator());
            StringBuilder builder = new StringBuilder();
            for (String conceptDescription : descendentConceptDescriptions) {
                builder.append(conceptDescription);
            }
            final String roleGroupLabel = builder.toString();
            titleLabel.setText(roleGroupLabel);
            expanded.addListener((observable, wasExpanded, isExpanded) -> {
                if (isExpanded) {
                    titleLabel.setText("");
                } else {
                    titleLabel.setText(roleGroupLabel);
                }
            });
            this.axiomView.addToGridPaneNoGrow(rootGridPane, expandButton, column++);
            this.axiomView.addToGridPaneGrow(rootGridPane, titleLabel, column++);
        } else {
//                        openConceptButton.getStyleClass().setAll(StyleClasses.OPEN_CONCEPT_BUTTON.toString());
//                        openConceptButton.setOnMouseClicked(this::handleShowRoleNodeClick);
//                        this.axiomView.addToGridPaneNoGrow(rootGridPane, expandButton, column++);
//                        this.axiomView.addToGridPaneNoGrowTopAlign(rootGridPane, openConceptButton, column++);
            if (this.axiomView.premiseType == STATED) {
                editable = true;
            }
            rootBorderPane.getStyleClass()
                    .add(StyleClasses.DEF_ROLE.toString());

            HBox roleBox = new HBox();
            roleBox.getChildren().add(new Label("∃ ("));
            EntityLabel typeNode = new EntityLabel(roleType, viewProperties());
            //typeNode.setBorder(ROLE_BORDER);
            typeNode.setPadding(new Insets(1, 3, 1, 3));
            roleBox.getChildren().add(typeNode);
            roleBox.getChildren().add(new Label(")➞["));
            for (EntityVertex restrictionChild : axiomTree().successors(axiomVertex)) {
                ConceptFacade restrictionConcept = CONCEPT.getPropertyFast(restrictionChild);
                EntityLabel restrictionNode = new EntityLabel(restrictionConcept, viewProperties());
                restrictionNode.setPadding(new Insets(1, 3, 1, 3));
                roleBox.getChildren().add(restrictionNode);
            }
            roleBox.getChildren().add(new Label("]"));
//                        StringBuilder builder = new StringBuilder();
//                        builder.append("∃ (");
//                        builder.append(manifold.getPreferredDescriptionText(roleNode.getTypeConceptNid()));
//                        builder.append(")➞[");
//                        for (LogicNode descendentNode : roleNode.getDescendents()) {
//                            if (descendentNode.getNodeSemantic() == NodeSemantic.CONCEPT) {
//                                ConceptNodeWithNids conceptNode = (ConceptNodeWithNids) descendentNode;
//                                builder.append(manifold.getPreferredDescriptionText(conceptNode.getConceptNid()));
//                            }
//                        }
//                        builder.append("]");
//                        titleLabel.setText(builder.toString());

            this.axiomView.addToGridPaneGrow(rootGridPane, roleBox, column++);
            addChildren = false;
        }

        if (this.axiomView.premiseType == STATED) {
            this.axiomView.addToGridPaneNoGrow(rootGridPane, editButton, column++);
        }
    }

    private void setupForRoleAll() {
        // TODO: Support Universal Restriction
        throw new UnsupportedOperationException();
    }

    private void setupForFeature() {
        if (this.axiomView.premiseType == STATED) {
            editable = true;
        }
        // TODO, when we move LogicalExpression to tinkar-core, then also add logical expression, and use the logical expression...
        rootBorderPane.getStyleClass()
                .add(StyleClasses.DEF_FEATURE.toString());
        int column = 0;
        openConceptButton.getStyleClass().setAll(StyleClasses.OPEN_CONCEPT_BUTTON.toString());
        this.axiomView.addToGridPaneNoGrowTopAlign(rootGridPane, openConceptButton, column++);
        openConceptButton.setOnMouseClicked(this::handleShowFeatureNodeClick);
        StringBuilder builder = new StringBuilder();
        builder.append("⒡ ");
        Optional<ConceptFacade> optionalTypeConcept = this.axiomVertex.propertyAsConcept(TinkarTerm.FEATURE_TYPE);
        Optional<ConceptFacade> optionalConcreteDomainOperator = this.axiomVertex.propertyAsConcept(TinkarTerm.CONCRETE_DOMAIN_OPERATOR);
        if (optionalTypeConcept.isPresent()  && optionalConcreteDomainOperator.isPresent()) {
            ConceptFacade typeConcept = optionalTypeConcept.get();
            ConceptFacade concreteDomainOperatorConcept = optionalConcreteDomainOperator.get();
            builder.append(calculator().getPreferredDescriptionTextWithFallbackOrNid(typeConcept));
            ConcreteDomainOperators operator = ConcreteDomainOperators.fromConcept(concreteDomainOperatorConcept);
            switch (operator) {
                case EQUALS:
                    builder.append(" = ");
                    break;
                case GREATER_THAN:
                    builder.append(" > ");
                    break;
                case GREATER_THAN_EQUALS:
                    builder.append(" ≥ ");
                    break;
                case LESS_THAN:
                    builder.append(" < ");
                    break;
                case LESS_THAN_EQUALS:
                    builder.append(" ≤ ");
                    break;
                default:
                    throw new UnsupportedOperationException("Can't handle: " + PrimitiveData.text(concreteDomainOperatorConcept.nid()));
            }
        } else {
            throw new IllegalStateException("Feature node does not contain type and operator: " + this.axiomVertex);
        }

        Optional<Object> optionalLiteral = this.axiomVertex.property(TinkarTerm.LITERAL_VALUE);
        optionalLiteral.ifPresentOrElse(literal -> builder.append(literal.toString()),
                () -> builder.append("not specified"));
        titleLabel.setText(builder.toString());
        this.axiomView.addToGridPaneGrow(rootGridPane, titleLabel, column++);
        if (this.axiomView.premiseType == STATED) {
            this.axiomView.addToGridPaneNoGrow(rootGridPane, editButton, column++);
        }

    }

    private void setupForConcept() {
        if (axiomView.premiseType == STATED) {
            editable = true;
        }
        rootBorderPane.getStyleClass()
                .add(StyleClasses.DEF_CONCEPT.toString());
        ConceptFacade conceptForVertex = CONCEPT.getPropertyFast(axiomVertex);
        titleLabel.setText(calculator().getPreferredDescriptionTextWithFallbackOrNid(conceptForVertex));

        Latest<EntityVersion> latest = calculator().latest(conceptForVertex);
        if (latest.isPresent()) {
            State latestStatus = latest.get().stamp().state();
            titleLabel.setGraphic(computeGraphic(conceptForVertex.nid(), false,
                    latestStatus, viewProperties(), this.axiomView.premiseType));
            titleLabel.pseudoClassStateChanged(INACTIVE_PSEUDO_CLASS, latestStatus != State.ACTIVE);
            rootBorderPane.pseudoClassStateChanged(INACTIVE_PSEUDO_CLASS, latestStatus != State.ACTIVE);
            rootGridPane.pseudoClassStateChanged(INACTIVE_PSEUDO_CLASS, latestStatus != State.ACTIVE);
        } else {
            titleLabel.setGraphic(computeGraphic(conceptForVertex.nid(), false,
                    State.PRIMORDIAL, viewProperties(), this.axiomView.premiseType));
        }

        openConceptButton.getStyleClass().setAll(StyleClasses.OPEN_CONCEPT_BUTTON.toString());
        openConceptButton.setOnMouseClicked(this::handleShowConceptNodeClick);

        int column = 0;
        axiomView.addToGridPaneNoGrowTopAlign(rootGridPane, openConceptButton, column++);
        axiomView.addToGridPaneGrow(rootGridPane, titleLabel, column++);
        if (axiomView.premiseType == STATED) {
            axiomView.addToGridPaneNoGrow(rootGridPane, editButton, column++);
        }
    }

    private void handleDragDetected(MouseEvent event) {
        LOG.debug("Drag detected: " + event);

        DragImageMaker dragImageMaker = new DragImageMaker(titleLabel);
        Dragboard db = titleLabel.startDragAndDrop(TransferMode.COPY);

        db.setDragView(dragImageMaker.getDragImage());

        int conceptNid = switch (LogicalOperatorsForVertex.get(axiomVertex)) {
            case CONCEPT -> {
                ConceptFacade conceptForVertex = CONCEPT.getPropertyFast(axiomVertex);
                yield conceptForVertex.nid();
            }
            case SUFFICIENT_SET, NECESSARY_SET -> axiomVertex.getMeaningNid();
            case DEFINITION_ROOT -> axiomView.getEntityBeingDefinedNid();

            case ROLE -> {
                ConceptFacade roleTypeForVertex = axiomVertex.propertyFast(TinkarTerm.ROLE_TYPE);
                yield roleTypeForVertex.nid();
            }
            case FEATURE -> {
                ConceptFacade featureTypeForVertex = FEATURE.getPropertyFast(axiomVertex);
                yield featureTypeForVertex.nid();
            }
            default -> axiomView.getEntityBeingDefinedNid();
        };

        try {
            KometClipboard content = new KometClipboard((Entity) Entity.getFast(conceptNid));
            db.setContent(content);
        } catch (Exception e) {
            Dialogs.showErrorDialog("Error dragging object...", e.getClass().getSimpleName() + " during drag.", e.getLocalizedMessage());
        }
        event.consume();
    }

    private void handleDragDone(DragEvent event) {
        LOG.debug("Dragging done: " + event);
        titleLabel.setBackground(originalBackground);
        this.transferMode = null;
    }

    private void handleDragEntered(DragEvent event) {
        if (editable) {
            LOG.debug("Dragging entered: " + event);
            this.originalBackground = titleLabel.getBackground();

            Color backgroundColor;
            Set<DataFormat> contentTypes = event.getDragboard()
                    .getContentTypes();

            if (KometClipboard.containsAny(contentTypes, KometClipboard.CONCEPT_TYPES)) {
                backgroundColor = Color.AQUA;
                this.transferMode = TransferMode.COPY_OR_MOVE;
            } else if (KometClipboard.containsAny(contentTypes, KometClipboard.SEMANTIC_TYPES)) {
                backgroundColor = Color.OLIVEDRAB;
                this.transferMode = TransferMode.COPY_OR_MOVE;
            } else {
                backgroundColor = Color.RED;
                this.transferMode = null;
            }

            BackgroundFill fill = new BackgroundFill(backgroundColor, CornerRadii.EMPTY, Insets.EMPTY);

            titleLabel.setBackground(new Background(fill));
        }
    }

    private void handleDragExited(DragEvent event) {
        LOG.debug("Dragging exited: " + event);
        titleLabel.setBackground(originalBackground);
        this.transferMode = null;
    }

    private void handleDragOver(DragEvent event) {
        // LOG.debug("Dragging over: " + event );
        if (this.transferMode != null) {
            event.acceptTransferModes(TransferMode.COPY_OR_MOVE);
            event.consume();
        }
    }

    private void toggleExpansion() {
        if (expanded.get()) {
            expandButton.setGraphic(Icon.OPEN.makeIcon());
            for (ClauseView childClause : childClauses) {
                childBox.getChildren().add(childClause.rootBorderPane);
            }
        } else {
            expandButton.setGraphic(Icon.CLOSE.makeIcon());
            childBox.getChildren().clear();
        }
    }

    protected final void handleEditClick(MouseEvent mouseEvent) {
        ContextMenu contextMenu = new ContextMenu();
        MenuItem doNothing = new MenuItemWithText("");
        contextMenu.getItems().addAll(doNothing);

        AxiomSubjectRecord axiomSubjectRecord = new AxiomSubjectRecord(this.axiomVertex.vertexIndex(),
                this.axiomView.axiomTree,
                this.axiomView.axiomTreeSemanticVersion,
                this.axiomView.premiseType,
                this.rootGridPane);
        ObservationRecord observation = new ObservationRecord(Topic.AXIOM_FOCUSED,
                axiomSubjectRecord, Measures.present());

        ImmutableList<Consequence<?>> consequences =
                RuleService.get().execute("Knowledge base name",
                        Lists.immutable.of(observation),
                        axiomView.viewProperties,
                        Coordinates.Edit.Default());


        if (consequences.notEmpty()) {
            contextMenu.getItems().clear();
            contextMenu.getItems().add(new SeparatorMenuItem());
            for (Consequence<?> consequence : consequences) {
                switch (consequence) {
                    case ConsequenceAction consequenceAction -> {
                        if (consequenceAction.generatedAction() instanceof Action action) {
                            if (action instanceof ActionGroup) {
                                ActionGroup actionGroup = (ActionGroup) action;
                                Menu menu = ActionUtils.createMenu(action);
                                //menu.setGraphic(actionGroup.getGraphic());
                                for (Action actionInGroup : actionGroup.getActions()) {
                                    if (actionInGroup == ActionUtils.ACTION_SEPARATOR) {
                                        menu.getItems().add(new SeparatorMenuItem());
                                    } else {
                                        menu.getItems().add(ActionUtils.createMenuItem(actionInGroup));
                                    }
                                }
                                contextMenu.getItems().add(menu);
                            } else {
                                if (action == ActionUtils.ACTION_SEPARATOR) {
                                    contextMenu.getItems().add(new SeparatorMenuItem());
                                } else {
                                    contextMenu.getItems().add(ActionUtils.createMenuItem(action));
                                }
                            }
                        } else {
                            LOG.error("Can't handle action of type: " + consequenceAction.generatedAction().getClass().getName() + "\n\n" + consequenceAction.generatedAction());
                        }
                    }

                    case ConsequenceMenu consequenceMenu -> {
                        contextMenu.getItems().add(consequenceMenu.generatedMenu());
                    }

                    default -> LOG.error("Can't handle consequence of type: " + consequence);
                }
            }

            mouseEvent.consume();
            contextMenu.show(editButton, mouseEvent.getScreenX(), mouseEvent.getScreenY());
        }
    }

    private void handleShowRoleNodeClick(MouseEvent mouseEvent) {
        if (mouseEvent.getButton().equals(MouseButton.PRIMARY)) {
            ConceptFacade typeFacade = axiomVertex.propertyFast(TinkarTerm.ROLE_TYPE);
            showPopup(typeFacade, mouseEvent);
        }
    }

    private void showPopup(EntityFacade entity, MouseEvent mouseEvent) {
        showPopup(entity.nid(), mouseEvent);
    }

    private void showPopup(int conceptNid, MouseEvent mouseEvent) {
        Optional<ObservableSemanticSnapshot> optionalAxiomSnapshot =
                ObservableSemantic.getAxiomSnapshot(conceptNid, this.axiomView.premiseType, viewProperties().calculator());

        optionalAxiomSnapshot.ifPresent(observableAxiomSnapshot -> {
            observableAxiomSnapshot.getLatestVersion().ifPresent(observableSemanticVersion -> {
                PopOver popover = new PopOver();
                AxiomView axiomView = AxiomView.createWithCommitPanel(observableSemanticVersion,
                        this.axiomView.premiseType,
                        viewProperties());
                popover.setContentNode(axiomView.getEditor());
                popover.setCloseButtonEnabled(true);
                popover.setHeaderAlwaysVisible(false);
                popover.setTitle("");
                popover.show(openConceptButton, mouseEvent.getScreenX(), mouseEvent.getScreenY());
                mouseEvent.consume();
            });
        });
    }

    private ViewCalculator calculator() {
        return axiomView.calculator();
    }

    private ViewProperties viewProperties() {
        return axiomView.viewProperties;
    }

    private DiTree<EntityVertex> axiomTree() {
        return axiomView.axiomTree;
    }

    private SemanticEntityVersion axiomTreeSemanticVersion() {
        return axiomView.axiomTreeSemanticVersion;
    }

    private void handleShowFeatureNodeClick(MouseEvent mouseEvent) {
        if (mouseEvent.getButton().equals(MouseButton.PRIMARY)) {
            ConceptFacade featureType = axiomVertex.propertyFast(TinkarTerm.FEATURE_TYPE);
            showPopup(featureType.nid(), mouseEvent);
        }
    }

    private void handleShowConceptNodeClick(MouseEvent mouseEvent) {
        if (mouseEvent.getButton().equals(MouseButton.PRIMARY)) {
            ConceptFacade vertexConcept = CONCEPT.getPropertyFast(axiomVertex);
            showPopup(vertexConcept.nid(), mouseEvent);
        }
    }

    private void addSvg(StringBuilder builder, int depth, double xOffset, double yOffset) {

        Bounds rootBounds = rootBorderPane.localToScreen(rootBorderPane.getBoundsInLocal());
        String leftStroke = "stroke: #c3cdd3;";
        int textOffset = 5;
        int preTextIconWidth = 0;
        if (rootBounds != null) {
            double leftWidth = 1;
            String nodeText = titleLabel.getText();
            double bottomInset = 0;
            double childOffset = 0;
            LogicalOperatorsForVertex vertexOperator = LogicalOperatorsForVertex.get(axiomVertex.getMeaningNid());
            switch (vertexOperator) {

                case DEFINITION_ROOT:
                    preTextIconWidth = 23;
                    if (this.axiomView.premiseType == STATED) {
                        builder.append("<use xlink:href=\"#stated\" x=\"");
                        builder.append((xOffset + rootBounds.getMinX() + leftWidth + textOffset - 1) * 33);
                        builder.append("\" y=\"");
                        builder.append((yOffset + rootBounds.getMinY() + textOffset + 1) * 33);

                        builder.append("\" style=\"fill: black; stroke: black; \"");

                        builder.append(" transform=\" scale(.03) \"/>");
                    } else {
                        builder.append("<use xlink:href=\"#inferred\" x=\"");
                        builder.append((xOffset + rootBounds.getMinX() + leftWidth + textOffset) * 100);
                        builder.append("\" y=\"");
                        builder.append((yOffset + rootBounds.getMinY() + textOffset + 4) * 100);
                        builder.append("\" style=\"fill: black; stroke: black; \"");
                        builder.append(" transform=\" scale(.01) \"/>");
                    }

                    break;
                case NECESSARY_SET:
                    leftStroke = "stroke: #FF4E08;";
                    leftWidth = 4;
                    nodeText = "Necessary set";
                    bottomInset = 4;
                    preTextIconWidth = 20;
                    builder.append("<use xlink:href=\"#hexagon\" x=\"");
                    builder.append((xOffset + rootBounds.getMinX() + leftWidth + textOffset) * 33);
                    builder.append("\" y=\"");
                    builder.append((yOffset + rootBounds.getMinY() + textOffset + 1) * 33);
                    builder.append("\" style=\"fill: white; stroke: #FF4E08; stroke-width: 50.0;\"");
                    builder.append(" transform=\" scale(.03) \"/>");
                    break;
                case SUFFICIENT_SET:
                    leftStroke = "stroke: #5ec200;";
                    leftWidth = 4;
                    nodeText = "Sufficient set";
                    bottomInset = 4;
                    preTextIconWidth = 20;
                    builder.append("<use xlink:href=\"#circle\" x=\"");
                    builder.append((xOffset + rootBounds.getMinX() + leftWidth + textOffset) * 33);
                    builder.append("\" y=\"");
                    builder.append((yOffset + rootBounds.getMinY() + textOffset + 1) * 33);
                    builder.append("\" style=\"fill: white; stroke: #5ec200; stroke-width: 50.0;\"");
                    builder.append(" transform=\" scale(.03) \"/>");
                    break;
                case INCLUSION_SET:
                    leftStroke = "stroke: #5ec200;";
                    leftWidth = 4;
                    nodeText = "Inclusion set";
                    bottomInset = 4;
                    preTextIconWidth = 20;
                    builder.append("<use xlink:href=\"#circle\" x=\"");
                    builder.append((xOffset + rootBounds.getMinX() + leftWidth + textOffset) * 33);
                    builder.append("\" y=\"");
                    builder.append((yOffset + rootBounds.getMinY() + textOffset + 1) * 33);
                    builder.append("\" style=\"fill: white; stroke: #5ec200; stroke-width: 50.0;\"");
                    builder.append(" transform=\" scale(.03) \"/>");
                    break;
                case CONCEPT:
                    leftStroke = "stroke: #c3cdd3;";
                    leftWidth = 4;
                    bottomInset = 5;
                    preTextIconWidth = 20;

                    ConceptFacade conceptForVertex = CONCEPT.getPropertyFast(axiomVertex);
                    boolean defined = isDefined(conceptForVertex);
                    boolean multiParent = isMultiparent(conceptForVertex);
                    if (defined) {
                        if (multiParent) {
                            builder.append("<use xlink:href=\"#arrow-circle\" ");
                            builder.append(" style=\"fill: #5ec200; stroke: #5ec200; \"");
                        } else {
                            builder.append("<use xlink:href=\"#circle\" ");
                            builder.append(" style=\"fill: #5ec200; stroke: #5ec200; \"");
                        }
                    } else {
                        if (multiParent) {
                            builder.append("<use xlink:href=\"#arrow-hexagon\" ");
                            builder.append(" style=\"fill: #FF4E08; stroke: #FF4E08; \"");
                        } else {
                            builder.append("<use xlink:href=\"#hexagon\" ");
                            builder.append(" style=\"fill: #FF4E08; stroke: #FF4E08; \"");
                        }
                    }

                    builder.append(" x=\"");
                    builder.append((xOffset + rootBounds.getMinX() + leftWidth + textOffset) * 33);
                    builder.append("\" y=\"");
                    builder.append((yOffset + rootBounds.getMinY() + textOffset + 1) * 33);

                    builder.append("\" transform=\"scale(.03) \"/>");
                    break;
                case ROLE:
                    ConceptFacade roleTypeForVertex = axiomVertex.propertyFast(TinkarTerm.ROLE_TYPE);
                    if (roleTypeForVertex.nid() == TinkarTerm.ROLE_GROUP.nid()) {
                        leftStroke = "stroke: #009bff;";
                        nodeText = "Role group";
                        bottomInset = 5;
                        childOffset = 3;
                        preTextIconWidth = 20;
                        builder.append("<use xlink:href=\"#role-group\" x=\"");
                        builder.append((xOffset + rootBounds.getMinX() + leftWidth + textOffset) * 25);
                        builder.append("\" y=\"");
                        builder.append((yOffset + rootBounds.getMinY() + textOffset) * 25);

                        builder.append("\" style=\"fill: black; stroke: black;\"");

                        builder.append(" transform=\" scale(.04) \"/>\n");

                    } else {
                        leftStroke = "stroke: #ff9100;";
                        bottomInset = 5;

                        StringBuilder roleStrBuilder = new StringBuilder();
                        roleStrBuilder.append("∃ (");

                        boolean roleDefined = isDefined(roleTypeForVertex);
                        boolean roleMultiParent = isMultiparent(roleTypeForVertex);
                        if (roleDefined) {
                            if (roleMultiParent) {
                                roleStrBuilder.append("<tspan dy=\"1.5\" style=\"font-family: Material Design Icons; fill: #5ec200; stroke: #5ec200; \">&#xF060; </tspan>\n<tspan dy=\"-1.5\" />");
                            } else {
                                roleStrBuilder.append("<tspan dy=\"1.5\" style=\"font-family: Material Design Icons; fill: #5ec200; stroke: #5ec200; \">&#xF12F; </tspan>\n<tspan dy=\"-1.5\" />");
                            }
                        } else {
                            if (roleMultiParent) {
                                roleStrBuilder.append("<tspan dy=\"1.5\" style=\"font-family: Material Design Icons; fill: #FF4E08; stroke: #FF4E08; \">&#xF061; </tspan>\n<tspan dy=\"-1.5\" />");
                            } else {
                                roleStrBuilder.append("<tspan dy=\"1.5\" style=\"font-family: Material Design Icons; fill: #FF4E08; stroke: #FF4E08; \">&#xF2D8; </tspan>\n<tspan dy=\"-1.5\" />");
                            }
                        }

                        roleStrBuilder.append(calculator().getPreferredDescriptionTextWithFallbackOrNid(roleTypeForVertex));
                        roleStrBuilder.append(")➞[");

                        for (EntityVertex descendentNode : axiomTree().successors(axiomVertex)) {

                            if (CONCEPT.semanticallyEqual(descendentNode.getMeaningNid())) {
                                ConceptFacade roleRestriction = CONCEPT.getPropertyFast(descendentNode);
                                roleDefined = isDefined(roleRestriction.nid());
                                roleMultiParent = isMultiparent(roleRestriction.nid());
                                if (roleDefined) {
                                    if (roleMultiParent) {
                                        roleStrBuilder.append("<tspan dy=\"1.5\" style=\"font-family: Material Design Icons; fill: #5ec200; stroke: #5ec200; \">&#xF060; </tspan>\n<tspan dy=\"-1.5\" />");
                                    } else {
                                        roleStrBuilder.append("<tspan dy=\"1.5\" style=\"font-family: Material Design Icons; fill: #5ec200; stroke: #5ec200; \">&#xF12F; </tspan>\n<tspan dy=\"-1.5\" />");
                                    }
                                } else {
                                    if (roleMultiParent) {
                                        roleStrBuilder.append("<tspan dy=\"1.5\" style=\"font-family: Material Design Icons; fill: #FF4E08; stroke: #FF4E08; \">&#xF061; </tspan>\n<tspan dy=\"-1.5\" />");
                                    } else {
                                        roleStrBuilder.append("<tspan dy=\"1.5\" style=\"font-family: Material Design Icons; fill: #FF4E08; stroke: #FF4E08; \">&#xF2D8; </tspan>\n<tspan dy=\"-1.5\" />");
                                    }
                                }

                                roleStrBuilder.append(calculator().getPreferredDescriptionTextWithFallbackOrNid(roleRestriction));
                            }
                        }
                        roleStrBuilder.append("]");
                        nodeText = roleStrBuilder.toString();

                    }

                    leftWidth = 4;
                    break;

                default:
            }

            double topWidth = 1;
            double halfTopWidth = topWidth / 2;

            double bottomWidth = 1;
            double halfBottomWidth = bottomWidth / 2;

            double halfLeftWidth = leftWidth / 2;

            int rightLineExtra;
            if (depth == 0) {
                rightLineExtra = 0;
            } else {
                rightLineExtra = 1;
            }
            // Top
            addLine(builder, xOffset + rootBounds.getMinX(), yOffset + rootBounds.getMinY(),
                    xOffset + rootBounds.getMaxX() + rightLineExtra, yOffset + rootBounds.getMinY(), topWidth, "stroke: #c3cdd3;");

            if (depth == 0) {
                // Right
                addLine(builder, xOffset + rootBounds.getMaxX(), yOffset + rootBounds.getMinY(),
                        xOffset + rootBounds.getMaxX(), yOffset + rootBounds.getMaxY() - bottomInset, 1, "stroke: #c3cdd3;");
            }

            // Bottom
            addLine(builder, xOffset + rootBounds.getMaxX() + rightLineExtra, yOffset + rootBounds.getMaxY() - bottomInset,
                    xOffset + rootBounds.getMinX(), yOffset + rootBounds.getMaxY() - bottomInset, bottomWidth, "stroke: #c3cdd3;");

            // Left
            addLine(builder, xOffset + rootBounds.getMinX() + halfLeftWidth, yOffset + rootBounds.getMaxY() + halfTopWidth - bottomInset,
                    xOffset + rootBounds.getMinX() + halfLeftWidth, yOffset + rootBounds.getMinY() - halfBottomWidth, leftWidth, leftStroke);

            // Text
            addText(builder, xOffset + rootBounds.getMinX() + leftWidth + textOffset + preTextIconWidth,
                    yOffset + rootBounds.getMinY() + textOffset + 9,
                    nodeText,
                    "font-size: 9pt; font-family: Open Sans Condensed Light, Symbol, Material Design Icons; baseline-shift: sub;");

            for (ClauseView child : childClauses) {
                child.addSvg(builder, depth + 1, xOffset, yOffset - childOffset);
            }
        }

    }

    private void addText(StringBuilder builder, double x, double y, String text, String style) {
        text = text.replace("➞", "<tspan style=\"font-family: Symbol;\">→</tspan>\n<tspan style=\"" + style + "\"/>\n");
        addText(builder, (int) x, (int) y, text, style);
    }

    private void addText(StringBuilder builder, int x, int y, String text, String style) {
        builder.append("    <text x=\"");
        builder.append(x);
        builder.append("\" y=\"");
        builder.append(y);
        builder.append("\" style=\"");
        builder.append(style);
        builder.append("\">");
        builder.append(text);
        builder.append("</text>\n");
    }

    boolean isDefined(EntityFacade facade) {
        return isDefined(facade.nid());
    }

    boolean isDefined(int conceptNid) {
        Latest<DiTreeEntity> conceptExpression = calculator().getAxiomTreeForEntity(conceptNid, premiseType());
        if (!conceptExpression.isPresent()) {
            return false;
        }
        return conceptExpression.get().containsVertexWithMeaning(TinkarTerm.SUFFICIENT_SET);
    }

    PremiseType premiseType() {
        return axiomView.premiseType;
    }

    boolean isMultiparent(EntityFacade facade) {
        return viewProperties().calculator().isMultiparent(facade);
    }

    boolean isMultiparent(int conceptNid) {
        return viewProperties().calculator().isMultiparent(conceptNid);
    }

    private void addLine(StringBuilder builder, double x1, double y1, double x2, double y2, double width, String style) {
        builder.append("<line x1=\"");
        builder.append(x1);
        builder.append("\" y1=\"");
        builder.append(y1);
        builder.append("\" x2=\"");
        builder.append(x2);
        builder.append("\" y2=\"");
        builder.append(y2);
        builder.append("\" style=\"");
        builder.append(style);
        builder.append(" stroke-width: ");
        builder.append(width);
        builder.append(";\"/>\n");
    }

    private ContextMenu getContextMenu() {
        MenuItem svgItem = new MenuItemWithText("Make concept svg");
        svgItem.setOnAction(this::makeSvg);
        MenuItem inlineSvgItem = new MenuItemWithText("Make inline svg");
        inlineSvgItem.setOnAction(this::makeInlineSvg);
        MenuItem mediaObjectSvgItem = new MenuItemWithText("Make media object svg");
        mediaObjectSvgItem.setOnAction(this::makeMediaObjectSvg);
        MenuItem glossaryEntryItem = new MenuItemWithText("Make glossary entry");
        glossaryEntryItem.setOnAction(this::makeGlossaryEntry);
        MenuItem javaExpressionItem = new MenuItemWithText("Make java expression");
        javaExpressionItem.setOnAction(this::makeJavaExpression);
        return new ContextMenu(svgItem, inlineSvgItem, mediaObjectSvgItem,
                glossaryEntryItem, javaExpressionItem);
    }

    private void makeJavaExpression(Event event) {
        putOnClipboard(axiomTree().toString());
    }

    private void makeMediaObjectSvg(Event event) {
        StringBuilder builder = new StringBuilder();
        builder.append("<mediaobject>\n");
        builder.append("       <imageobject>\n");
        builder.append("            <imagedata>\n");
        makeSvg(builder);
        builder.append("\n          </imagedata>");
        builder.append("\n     </imageobject>");
        builder.append("\n</mediaobject>");

        putOnClipboard(builder.toString());
    }

    private void makeGlossaryEntry(Event event) {
        StringBuilder builder = new StringBuilder();
        builder.append("<inlinemediaobject>\n");
        builder.append("                <imageobject>\n");
        builder.append("                    <imagedata>\n");
        makeSvg(builder);
        builder.append("\n                    </imagedata>");
        builder.append("\n                </imageobject>");
        builder.append("\n</inlinemediaobject>");

        putOnClipboard(DocBook.getGlossentry(axiomTreeSemanticVersion().referencedComponentNid(), viewProperties(), builder.toString()));
    }

    private void makeInlineSvg(Event event) {
        StringBuilder builder = new StringBuilder();
        builder.append("<inlinemediaobject>\n");
        builder.append("                <imageobject>\n");
        builder.append("                    <imagedata>\n");
        makeSvg(builder);
        builder.append("\n                    </imagedata>");
        builder.append("\n                </imageobject>");
        builder.append("\n</inlinemediaobject>");

        putOnClipboard(builder.toString());
    }

    private void makeSvg(Event event) {

        StringBuilder builder = makeSvg(new StringBuilder());

        putOnClipboard(builder.toString());

    }

    private void putOnClipboard(String string) {
        final Clipboard clipboard = Clipboard.getSystemClipboard();
        final ClipboardContent content = new ClipboardContent();
        content.putString(string);
        clipboard.setContent(content);
    }

    private StringBuilder makeSvg(StringBuilder builder) {
        Bounds rootBoundsInScreen = rootBorderPane.localToScreen(axiomView.borderPane.getBoundsInLocal());
        builder.append("<svg xmlns=\"http://www.w3.org/2000/svg\" width=\"");
        builder.append(rootBoundsInScreen.getWidth() + 5);
        builder.append("px\" height=\"");
        builder.append(rootBoundsInScreen.getHeight() + 5);
        builder.append("px\">\n");
        builder.append("   <defs>\n");

        builder.append("<path id=\"arrow-circle\" d=\"M149,234h64v85h86v-85h64L256,127L149,234z M407,385c40-40,60.7-90.3,62-151c-1.3-60.7-22-111-62-151 ");
        builder.append("s-90.3-60.7-151-62c-60.7,1.3-111,22-151,62s-60.7,90.3-62,151c1.3,60.7,22,111,62,151s90.3,60.7,151,62 ");
        builder.append("C316.7,445.7,367,425,407,385z M135,355c-32-32-48.7-72.3-50-121c1.3-48.7,18-89,50-121s72.3-48.7,121-50c48.7,1.3,89,18,121,50 ");
        builder.append("s48.7,72.3,50,121c-1.3,48.7-18,89-50,121s-72.3,48.7-121,50C207.3,403.7,167,387,135,355z\"/>\n");
        builder.append("<path id=\"arrow-hexagon\" d=\"M133,227h64v85h86v-85h64L240,120L133,227z M432,131c-0.7-8.7-4.3-15-11-19L252,18c-3.3-2.7-7.3-4-12-4 ");
        builder.append("c-4.7,0-8.7,1.3-12,4L59,112c-6.7,4-10.3,10.3-11,19v192c0.7,8.7,4.3,15,11,19l169,94c3.3,2.7,7.3,4,12,4c4.7,0,8.7-1.3,12-4l169-94 ");
        builder.append("c6.7-4,10.3-10.3,11-19V131z M389,144v166l-149,84L91,310V144l149-84L389,144z\"/>\n");
        builder.append("<path  id=\"circle\" d=\"M256 21q-73 2 -121 50t-50 121q2 73 50 121t121 50q73 -2 121 -50t50 -121q-2 -73 -50 -121t-121 -50zM256 405q-91 -2 -151 -62t-62 -151q2 -91 62 -151t151 -62q91 2 151 62t62 151q-2 91 -62 151t-151 62z\"/> \n");
        builder.append("<path  id=\"hexagon\" d=\"M448 96q-1 -13 -11 -19l-169 -94q-5 -4 -12 -4t-12 4l-169 94q-10 6 -11 19v192q1 13 11 19l169 94q5 4 12 4t12 -4l169 -94q10 -6 11 -19v-192z\"/> \n");
        builder.append("<path  id=\"role-group\" d=\"M107 245l53 -96h-107zM64 363h85v-86h-85v86zM107 21q18 1 30 13t12 30t-12 30t-30 12t-30.5 -12t-12.5 -30t12.5 -30t30.5 -13zM192 341v-42h256v42h-256zM192 43h256v42h-256v-42zM192 171h256v42h-256v-42z\"/> \n");
        builder.append("<path id=\"stated\" d=\"M252.5,37c38.7,0,75,6.7,109,20c34,12.7,61,30.3,81,53c19.3,22.7,29,47,29,73s-9.7,50.3-29,73\n"
                + "	c-20,22.7-47,40.3-81,53c-34,13.3-70.3,20-109,20c-11.3,0-23.7-0.7-37-2l-16-2l-13,11c-23.3,20.7-49.3,37-78,49\n"
                + "	c8.7-15.3,15.3-31.7,20-49l7-28l-24-14c-25.3-14-44.7-30.7-58-50s-20-39.7-20-61c0-26,9.7-50.3,29-73c20-22.7,47-40.3,81-53\n"
                + "	C177.5,43.7,213.8,37,252.5,37L252.5,37z M508.5,183c0-33.3-11.3-64-34-92c-23.3-28-54.3-50-93-66c-39.3-16.7-82.3-25-129-25\n"
                + "	s-89.7,8.3-129,25c-38.7,16-69.7,38-93,66c-22.7,28-34,58.7-34,92c0,28.7,8.7,55.3,26,80c17.3,25.3,41,46.3,71,63\n"
                + "	c-2.7,8-5.3,15.3-8,22s-5.3,12.3-8,17c-6,9.3-9,14-9,14l-9,12l-10,10c-4,5.3-6.7,8.7-8,10c-0.7,0-1.7,1-3,3l-2,2l0,0l-1,3\n"
                + "	c-1.3,1.3-2,2.3-2,3v2c-0.7,2-0.7,3.3,0,4l0,0c0.7,3.3,2,6,4,8c2.7,2,5.3,3,8,3h2c12-1.3,22.7-3.3,32-6c50-12.7,94-35.7,132-69\n"
                + "	c14,1.3,27.7,2,41,2c46.7,0,89.7-8.3,129-25c38.7-16,69.7-38,93-66C497.2,247,508.5,216.3,508.5,183L508.5,183z\"/>\n");
        builder.append("    <path id=\"inferred\" \n"
                + "d=\"M896 640q0 106 -75 181t-181 75t-181 -75t-75 -181t75 -181t181 -75t181 75t75 181zM1664 128q0 52 -38 90t-90 38t-90 -38t-38 -90q0 -53 37.5 -90.5t90.5 -37.5t90.5 37.5t37.5 90.5zM1664 1152q0 52 -38 90t-90 38t-90 -38t-38 -90q0 -53 37.5 -90.5t90.5 -37.5\n"
                + "t90.5 37.5t37.5 90.5zM1280 731v-185q0 -10 -7 -19.5t-16 -10.5l-155 -24q-11 -35 -32 -76q34 -48 90 -115q7 -11 7 -20q0 -12 -7 -19q-23 -30 -82.5 -89.5t-78.5 -59.5q-11 0 -21 7l-115 90q-37 -19 -77 -31q-11 -108 -23 -155q-7 -24 -30 -24h-186q-11 0 -20 7.5t-10 17.5\n"
                + "l-23 153q-34 10 -75 31l-118 -89q-7 -7 -20 -7q-11 0 -21 8q-144 133 -144 160q0 9 7 19q10 14 41 53t47 61q-23 44 -35 82l-152 24q-10 1 -17 9.5t-7 19.5v185q0 10 7 19.5t16 10.5l155 24q11 35 32 76q-34 48 -90 115q-7 11 -7 20q0 12 7 20q22 30 82 89t79 59q11 0 21 -7\n"
                + "l115 -90q34 18 77 32q11 108 23 154q7 24 30 24h186q11 0 20 -7.5t10 -17.5l23 -153q34 -10 75 -31l118 89q8 7 20 7q11 0 21 -8q144 -133 144 -160q0 -8 -7 -19q-12 -16 -42 -54t-45 -60q23 -48 34 -82l152 -23q10 -2 17 -10.5t7 -19.5zM1920 198v-140q0 -16 -149 -31\n"
                + "q-12 -27 -30 -52q51 -113 51 -138q0 -4 -4 -7q-122 -71 -124 -71q-8 0 -46 47t-52 68q-20 -2 -30 -2t-30 2q-14 -21 -52 -68t-46 -47q-2 0 -124 71q-4 3 -4 7q0 25 51 138q-18 25 -30 52q-149 15 -149 31v140q0 16 149 31q13 29 30 52q-51 113 -51 138q0 4 4 7q4 2 35 20\n"
                + "t59 34t30 16q8 0 46 -46.5t52 -67.5q20 2 30 2t30 -2q51 71 92 112l6 2q4 0 124 -70q4 -3 4 -7q0 -25 -51 -138q17 -23 30 -52q149 -15 149 -31zM1920 1222v-140q0 -16 -149 -31q-12 -27 -30 -52q51 -113 51 -138q0 -4 -4 -7q-122 -71 -124 -71q-8 0 -46 47t-52 68\n"
                + "q-20 -2 -30 -2t-30 2q-14 -21 -52 -68t-46 -47q-2 0 -124 71q-4 3 -4 7q0 25 51 138q-18 25 -30 52q-149 15 -149 31v140q0 16 149 31q13 29 30 52q-51 113 -51 138q0 4 4 7q4 2 35 20t59 34t30 16q8 0 46 -46.5t52 -67.5q20 2 30 2t30 -2q51 71 92 112l6 2q4 0 124 -70\n"
                + "q4 -3 4 -7q0 -25 -51 -138q17 -23 30 -52q149 -15 149 -31z\" />");
        builder.append("");
        builder.append("");
        builder.append("");
        builder.append("");
        builder.append("");

        builder.append("   </defs>\n");

        builder.append("    <g alignment-baseline=\"baseline\"></g>\n");
        addSvg(builder, 0, -rootBoundsInScreen.getMinX(), -rootBoundsInScreen.getMinY());
        builder.append("</svg>\n");
        return builder;
    }
}
