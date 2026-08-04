package dev.ikm.komet.kview.controls.skin;

import dev.ikm.komet.kview.NodeUtils;
import dev.ikm.komet.kview.controls.ComponentItemNode;
import dev.ikm.komet.kview.controls.ComponentItemNodeFactory;
import dev.ikm.komet.kview.controls.KLReadOnlyDiTreeControl;
import dev.ikm.tinkar.common.id.PublicId;
import dev.ikm.tinkar.entity.graph.DiTreeEntity;
import dev.ikm.tinkar.entity.graph.EntityVertex;
import dev.ikm.tinkar.terms.ConceptFacade;
import dev.ikm.tinkar.terms.TinkarTerm;
import javafx.geometry.Pos;
import javafx.scene.Node;
import javafx.scene.control.Label;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.Region;
import javafx.scene.layout.VBox;

import java.util.function.IntFunction;

/**
 * <p>Skin for {@link KLReadOnlyDiTreeControl}: walks the {@link DiTreeEntity} logical definition
 * and renders each vertex per its meaning.</p>
 *
 * <ul>
 *     <li>The definition root vertex is transparent — its children render at the top level (the
 *     field title and the window's referenced component already identify premise and subject).</li>
 *     <li>Necessary / sufficient / inclusion sets and role groups become clause headers: a colored
 *     accent bar, a disclosure toggle, and a small-caps clause label.</li>
 *     <li>Concept references become full-width component chips ({@link ComponentItemNode}), which
 *     provide the standard component context menu and drag-and-drop.</li>
 *     <li>Existential roles become {@code ∃ (role type) → [restriction]} rows whose role type and
 *     restriction are inline component chips.</li>
 *     <li>{@code AND} vertices are transparent operators: their children are flattened into the
 *     enclosing level.</li>
 * </ul>
 *
 * <p>This is the rendering-only first iteration: each chip is a slot with the same geometry as the
 * selected state of {@code KLComponentControl}, so inline editing (search-in-place), and structural
 * editing on the clause headers, can mount into the same layout later without re-arranging it.</p>
 */
public class KLReadOnlyDiTreeControlSkin extends KLReadOnlyBaseControlSkin<KLReadOnlyDiTreeControl> {

    private final VBox treeContainer = new VBox();

    /**
     * @param control The control for which this Skin should attach to.
     */
    public KLReadOnlyDiTreeControlSkin(KLReadOnlyDiTreeControl control) {
        super(control);

        mainContainer.getChildren().addAll(promptTextLabel, treeContainer);

        // The chips provide their own context menus (ComponentItemNode), so the control-level
        // context menu from the base skin is not wanted here.
        control.setContextMenu(null);

        control.valueProperty().subscribe(this::rebuildTree);
        control.componentItemResolverProperty().subscribe(() -> rebuildTree(control.getValue()));

        // CSS
        treeContainer.getStyleClass().add("ditree-container");
    }

    private void rebuildTree(DiTreeEntity tree) {
        treeContainer.getChildren().clear();
        boolean showPromptText = tree == null || getSkinnable().getComponentItemResolver() == null;
        NodeUtils.setShowing(promptTextLabel, showPromptText);
        NodeUtils.setShowing(treeContainer, !showPromptText);
        if (showPromptText) {
            return;
        }
        EntityVertex root = tree.root();
        if (root.getMeaningNid() == TinkarTerm.DEFINITION_ROOT.nid()) {
            appendChildren(treeContainer, root, tree);
        } else {
            treeContainer.getChildren().add(buildVertex(root, tree));
        }
    }

    private void appendChildren(VBox container, EntityVertex vertex, DiTreeEntity tree) {
        for (EntityVertex child : tree.successors(vertex)) {
            if (child.getMeaningNid() == TinkarTerm.AND.nid()) {
                appendChildren(container, child, tree);
            } else {
                container.getChildren().add(buildVertex(child, tree));
            }
        }
    }

    private Node buildVertex(EntityVertex vertex, DiTreeEntity tree) {
        int meaning = vertex.getMeaningNid();
        if (meaning == TinkarTerm.NECESSARY_SET.nid()) {
            return clauseNode(vertex, tree, "necessary-set", "Necessary set");
        }
        if (meaning == TinkarTerm.SUFFICIENT_SET.nid()) {
            return clauseNode(vertex, tree, "sufficient-set", "Sufficient set");
        }
        if (meaning == TinkarTerm.INCLUSION_SET.nid()) {
            return clauseNode(vertex, tree, "inclusion-set", "Inclusion set");
        }
        if (meaning == TinkarTerm.CONCEPT_REFERENCE.nid()) {
            ConceptFacade concept = vertex.propertyFast(TinkarTerm.CONCEPT_REFERENCE);
            return conceptRow(concept.nid());
        }
        if (meaning == TinkarTerm.ROLE.nid()) {
            ConceptFacade roleType = vertex.propertyFast(TinkarTerm.ROLE_TYPE);
            if (roleType != null && PublicId.equals(roleType.publicId(), TinkarTerm.ROLE_GROUP)) {
                return clauseNode(vertex, tree, "role-group", "Role group");
            }
            return roleRow(vertex, tree, roleType);
        }
        // Features, intervals, property sets, … — fall back to a plain clause header for v1.
        IntFunction<String> descriptionResolver = getSkinnable().getDescriptionResolver();
        String clauseText = descriptionResolver != null ? descriptionResolver.apply(meaning) : Integer.toString(meaning);
        return clauseNode(vertex, tree, "feature", clauseText);
    }

    /**
     * Builds a clause node: a header row (accent bar, disclosure toggle, small-caps clause label)
     * with the vertex's children indented below it.
     */
    private Node clauseNode(EntityVertex vertex, DiTreeEntity tree, String barStyleClass, String clauseText) {
        VBox childrenBox = new VBox();
        childrenBox.getStyleClass().add("ditree-children");
        appendChildren(childrenBox, vertex, tree);

        HBox header = new HBox();
        header.getStyleClass().add("ditree-clause-header");
        header.setAlignment(Pos.CENTER_LEFT);
        header.getChildren().add(clauseBar(barStyleClass));
        boolean hasChildren = !childrenBox.getChildren().isEmpty();
        if (hasChildren) {
            header.getChildren().add(disclosure(childrenBox));
        }
        Label clauseLabel = new Label(clauseText.toUpperCase());
        clauseLabel.getStyleClass().add("ditree-clause-label");
        header.getChildren().add(clauseLabel);

        VBox clause = new VBox(header);
        clause.getStyleClass().add("ditree-node");
        if (hasChildren) {
            clause.getChildren().add(childrenBox);
        }
        return clause;
    }

    /**
     * Builds an expand/collapse toggle that shows or hides the given child container (default
     * expanded) — so sets and role groups can be collapsed.
     */
    private Label disclosure(VBox childrenBox) {
        Label toggle = new Label("▾");
        toggle.getStyleClass().add("ditree-toggle");
        toggle.setOnMouseClicked(event -> {
            boolean show = !childrenBox.isVisible();
            NodeUtils.setShowing(childrenBox, show);
            toggle.setText(show ? "▾" : "▸");
            event.consume();
        });
        return toggle;
    }

    /**
     * Builds a full-width chip row for a concept reference (an is-a of the enclosing set).
     */
    private Node conceptRow(int nid) {
        ComponentItemNode itemNode = chip(nid);
        itemNode.setMaxWidth(Double.MAX_VALUE);
        HBox row = new HBox(itemNode);
        row.getStyleClass().add("ditree-concept-row");
        HBox.setHgrow(itemNode, Priority.ALWAYS);
        return row;
    }

    /**
     * Builds the {@code ∃ (role type) → [restriction]} row for an existential role restriction:
     * fixed-width operator glyphs with a chip for the role type and one per restriction concept.
     */
    private Node roleRow(EntityVertex vertex, DiTreeEntity tree, ConceptFacade roleType) {
        HBox row = new HBox();
        row.getStyleClass().add("ditree-role-row");
        row.setAlignment(Pos.CENTER_LEFT);
        row.getChildren().add(clauseBar("role"));
        row.getChildren().add(operator("∃ ("));
        if (roleType != null) {
            row.getChildren().add(chip(roleType.nid()));
        }
        row.getChildren().add(operator(") → ["));
        for (EntityVertex child : tree.successors(vertex)) {
            if (child.getMeaningNid() == TinkarTerm.CONCEPT_REFERENCE.nid()) {
                ConceptFacade restriction = child.propertyFast(TinkarTerm.CONCEPT_REFERENCE);
                row.getChildren().add(chip(restriction.nid()));
            }
        }
        row.getChildren().add(operator("]"));
        return row;
    }

    private ComponentItemNode chip(int nid) {
        ComponentItemNode itemNode = ComponentItemNodeFactory.create(getSkinnable().getComponentItemResolver().apply(nid));
        itemNode.setShowDragHandleOnHover(true);
        return itemNode;
    }

    private Region clauseBar(String styleClass) {
        Region bar = new Region();
        bar.getStyleClass().addAll("ditree-clause-bar", styleClass);
        return bar;
    }

    private Label operator(String glyph) {
        Label label = new Label(glyph);
        label.getStyleClass().add("ditree-op");
        // Never shrink the operator below its glyph, so neighbouring chips give way instead of
        // crowding out the ∃/→.
        label.setMinWidth(Region.USE_PREF_SIZE);
        return label;
    }
}