package dev.ikm.komet.framework.panel.axiom;

import javafx.event.Event;
import javafx.geometry.HPos;
import javafx.geometry.VPos;
import javafx.scene.Node;
import javafx.scene.layout.*;
import javafx.scene.paint.Color;
import org.controlsfx.property.editor.PropertyEditor;
import dev.ikm.komet.framework.PseudoClasses;
import dev.ikm.komet.framework.graphics.Icon;
import dev.ikm.komet.framework.view.ViewProperties;
import dev.ikm.tinkar.common.id.IntIdList;
import dev.ikm.tinkar.common.id.IntIds;
import dev.ikm.tinkar.component.graph.DiTree;
import dev.ikm.tinkar.coordinate.logic.PremiseType;
import dev.ikm.tinkar.coordinate.stamp.calculator.Latest;
import dev.ikm.tinkar.coordinate.view.calculator.ViewCalculator;
import dev.ikm.tinkar.entity.SemanticEntityVersion;
import dev.ikm.tinkar.entity.graph.DiTreeEntity;
import dev.ikm.tinkar.entity.graph.EntityVertex;
import dev.ikm.tinkar.terms.State;
import dev.ikm.tinkar.terms.TinkarTerm;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * @author kec
 */
public class AxiomView implements PropertyEditor<DiTree<EntityVertex>> {
    public static final Border TOOL_BAR_BORDER = new Border(
            new BorderStroke(Color.LIGHTGRAY, BorderStrokeStyle.SOLID, CornerRadii.EMPTY, new BorderWidths(0, 0, 0, 0))
    );
    public static final Border INNER_ROOT_BORDER = new Border(
            new BorderStroke(Color.DARKGRAY, BorderStrokeStyle.SOLID, CornerRadii.EMPTY, new BorderWidths(1, 0, 1, 1))
    );
    static final Border CHILD_BOX_BORDER = new Border(
            new BorderStroke(Color.LIGHTGRAY, BorderStrokeStyle.SOLID, CornerRadii.EMPTY, new BorderWidths(0, 0, 0, 0))
    );
    static final Border ROOT_BORDER = new Border(
            new BorderStroke(Color.DARKGRAY, BorderStrokeStyle.SOLID, CornerRadii.EMPTY, new BorderWidths(1, 1, 1, 1))
    );
    private static final Logger LOG = LoggerFactory.getLogger(AxiomView.class);
    public final PremiseType premiseType;
    final ViewProperties viewProperties;
    final SemanticEntityVersion axiomTreeSemanticVersion;
    private final AnchorPane anchorPane = new AnchorPane();
    DiTree<EntityVertex> axiomTree;
    BorderPane borderPane = new BorderPane();

    private AxiomView(SemanticEntityVersion axiomTreeSemanticVersion, PremiseType premiseType, ViewProperties viewProperties) {
        this.axiomTreeSemanticVersion = axiomTreeSemanticVersion;
        viewProperties.calculator().getFieldForSemanticWithPurpose(axiomTreeSemanticVersion.nid(), TinkarTerm.LOGICAL_DEFINITION).ifPresentOrElse(objectField -> {
            axiomTree = (DiTree<EntityVertex>) objectField.value();
        }, () -> {
            throw new IllegalStateException("No logical definition found. ");
        });
        this.viewProperties = viewProperties;
        this.premiseType = premiseType;
        ClauseView clauseView = new ClauseView(axiomTree.root(), this);
        borderPane.setCenter(clauseView.rootBorderPane);
    }

    public static final Node computeGraphic(int conceptNid, boolean expanded, State state, ViewProperties viewProperties, PremiseType premiseType) {

        if (conceptNid == -1
                || conceptNid == TinkarTerm.UNINITIALIZED_COMPONENT.nid()) {
            return Icon.ALERT_CONFIRM2.makeIcon();
        }
        IntIdList parents = IntIds.list.empty();
        try {
            parents = viewProperties.calculator().navigationCalculator().parentsOf(conceptNid);
        } catch (RuntimeException ex) {
            LOG.error("Error retrieving parents", ex);
        }
        Latest<DiTreeEntity> conceptExpression = viewProperties.calculator().getAxiomTreeForEntity(conceptNid, premiseType);
        if (!conceptExpression.isPresent()) {
            conceptExpression = viewProperties.calculator().getAxiomTreeForEntity(conceptNid, PremiseType.STATED);
            if (!conceptExpression.isPresent()) {
                return Icon.ALERT_CONFIRM2.makeIcon();
            }
        }
        boolean multiParent = !parents.isEmpty();
        boolean sufficient = conceptExpression.get().containsVertexWithMeaning(TinkarTerm.SUFFICIENT_SET);

        if (parents.isEmpty()) {
            return Icon.TAXONOMY_ROOT_ICON.makeIcon();
        } else if (sufficient && multiParent) {
            if (expanded) {
                return Icon.TAXONOMY_DEFINED_MULTIPARENT_OPEN.makeIcon();
            } else {
                return Icon.TAXONOMY_DEFINED_MULTIPARENT_CLOSED.makeIcon();
            }
        } else if (!sufficient && multiParent) {
            if (expanded) {
                return Icon.TAXONOMY_PRIMITIVE_MULTIPARENT_OPEN.makeIcon();
            } else {
                return Icon.TAXONOMY_PRIMITIVE_MULTIPARENT_CLOSED.makeIcon();
            }
        } else if (sufficient && !multiParent) {
            return Icon.TAXONOMY_DEFINED_SINGLE_PARENT.makeIcon();
        }
        return Icon.TAXONOMY_PRIMITIVE_SINGLE_PARENT.makeIcon();
    }

    public static AxiomView create(SemanticEntityVersion logicGraphVersion, PremiseType premiseType, ViewProperties viewProperties) {
        AxiomView axiomView = new AxiomView(logicGraphVersion, premiseType, viewProperties);
        BorderPane axiomBorderPane = axiomView.create(axiomView.axiomTree.root());
        AnchorPane.setBottomAnchor(axiomBorderPane, 0.0);
        AnchorPane.setLeftAnchor(axiomBorderPane, 0.0);
        AnchorPane.setRightAnchor(axiomBorderPane, 0.0);
        AnchorPane.setTopAnchor(axiomBorderPane, 0.0);
        axiomView.anchorPane.getChildren().setAll(axiomBorderPane);
        return axiomView;
    }

    private BorderPane create(EntityVertex logicNode) {
        ClauseView clauseView = new ClauseView(logicNode, this);
        return clauseView.rootBorderPane;
    }

    public static AxiomView createWithCommitPanel(SemanticEntityVersion logicGraphVersion, PremiseType premiseType, ViewProperties viewProperties) {
        AxiomView axiomView = new AxiomView(logicGraphVersion, premiseType, viewProperties);
        BorderPane axiomBorderPane = axiomView.create(axiomView.axiomTree.root());
        AnchorPane.setBottomAnchor(axiomBorderPane, 0.0);
        AnchorPane.setLeftAnchor(axiomBorderPane, 0.0);
        AnchorPane.setRightAnchor(axiomBorderPane, 0.0);
        AnchorPane.setTopAnchor(axiomBorderPane, 0.0);
        axiomView.anchorPane.getChildren().setAll(axiomBorderPane);
        axiomView.borderPane = new BorderPane(axiomView.anchorPane);
        return axiomView;
    }

    @Override
    public Node getEditor() {
        return anchorPane;
    }

    @Override
    public DiTree<EntityVertex> getValue() {
        return axiomTree;
    }

    @Override
    public void setValue(DiTree<EntityVertex> value) {
        this.axiomTree = axiomTree;
        ClauseView clauseView = new ClauseView(axiomTree.root(), this);
        borderPane.setCenter(clauseView.rootBorderPane);
    }

    String getEntityForAxiomsText(String prefix) {
        if (axiomTreeSemanticVersion.referencedComponentNid() != TinkarTerm.UNINITIALIZED_COMPONENT.nid()) {
            StringBuilder builder = new StringBuilder();
            if (prefix != null) {
                builder.append(prefix);
                builder.append(": ");
            }
            builder.append(viewProperties.calculator().getPreferredDescriptionTextWithFallbackOrNid(axiomTreeSemanticVersion.referencedComponentNid()));
            return builder.toString();
        } else if (prefix != null) {
            return prefix + ": Concept being defined";
        }
        return "Concept being defined";
    }

    public final void setPremiseTypePseudoClasses(Node node) {
        switch (premiseType) {
            case INFERRED:
                node.pseudoClassStateChanged(PseudoClasses.STATED_PSEUDO_CLASS, false);
                node.pseudoClassStateChanged(PseudoClasses.INFERRED_PSEUDO_CLASS, true);
                break;
            case STATED:
                node.pseudoClassStateChanged(PseudoClasses.STATED_PSEUDO_CLASS, true);
                node.pseudoClassStateChanged(PseudoClasses.INFERRED_PSEUDO_CLASS, false);
                break;
            default:
                break;
        }
    }

    public void addToGridPaneNoGrowTopAlign(GridPane rootToolBar, Node node, int column) {
        GridPane.setConstraints(node, column, 0, 1, 1, HPos.LEFT, VPos.TOP, Priority.NEVER, Priority.NEVER);
        rootToolBar.getChildren().add(node);
    }

    void addToGridPaneNoGrow(GridPane rootToolBar, Node node, int column) {
        GridPane.setConstraints(node, column, 0, 1, 1, HPos.LEFT, VPos.BASELINE, Priority.NEVER, Priority.NEVER);
        rootToolBar.getChildren().add(node);
    }

    public void addToGridPaneGrow(GridPane rootToolBar, Node node, int column) {
        GridPane.setConstraints(node, column, 0, 1, 1, HPos.LEFT, VPos.BASELINE, Priority.ALWAYS, Priority.NEVER);
        rootToolBar.getChildren().add(node);
    }

    private void cancelEdit(Event event) {
        throw new UnsupportedOperationException();
    }

    int getEntityBeingDefinedNid() {
        return this.axiomTreeSemanticVersion.referencedComponentNid();
    }

    private void commitEdit(Event event) {
        throw new UnsupportedOperationException();
    }

    protected ViewCalculator calculator() {
        return viewProperties.calculator();
    }
}
