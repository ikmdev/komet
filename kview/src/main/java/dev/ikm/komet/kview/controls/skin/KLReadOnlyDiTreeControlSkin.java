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

import java.math.BigDecimal;
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
 *     <li>Interval roles become {@code I (role type) → (lower,upper) unit} rows — the bracket
 *     shapes carry the bounds' open/closed state, as in the classic axiom control.</li>
 *     <li>{@code AND} vertices are transparent operators: their children are flattened into the
 *     enclosing level.</li>
 * </ul>
 *
 * <p>The protected hooks — {@link #createChipNode(EntityVertex, int, ChipKind)},
 * {@link #decorateClauseHeader(HBox, VBox, EntityVertex)}, {@link #decorateRoleRow(HBox, EntityVertex)}
 * and {@link #afterRebuild(DiTreeEntity)} — exist for the editing subclass
 * ({@code KLDiTreeControlSkin}) to mount its interactions without re-arranging the layout.</p>
 */
public class KLReadOnlyDiTreeControlSkin extends KLReadOnlyBaseControlSkin<KLReadOnlyDiTreeControl> {

    /**
     * The editing role a chip plays in the tree, which determines what an inline edit of the chip
     * means for the logical definition.
     */
    protected enum ChipKind {
        /** A concept reference that is an is-a of the enclosing set: edits replace the concept. */
        CONCEPT_REFERENCE,
        /** The type of an existential role: edits replace the role type. */
        ROLE_TYPE,
        /** The restriction concept of an existential role: edits replace the restriction. */
        ROLE_RESTRICTION,
        /** The type of an interval role: edits replace the interval role type. */
        INTERVAL_ROLE_TYPE,
        /** The unit of measure of an interval role: edits replace the unit. */
        INTERVAL_UNIT
    }

    protected final VBox treeContainer = new VBox();

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
        control.rootConceptNidProperty().subscribe(() -> rebuildTree(control.getValue()));

        // CSS
        treeContainer.getStyleClass().add("ditree-container");
    }

    protected final void rebuildTree(DiTreeEntity tree) {
        treeContainer.getChildren().clear();
        boolean hasContent = getSkinnable().getComponentItemResolver() != null
                && (tree != null || renderEmptyDefinition());
        NodeUtils.setShowing(promptTextLabel, !hasContent);
        NodeUtils.setShowing(treeContainer, hasContent);
        if (hasContent) {
            int rootConceptNid = getSkinnable().getRootConceptNid();
            if (rootConceptNid != 0) {
                treeContainer.getChildren().add(rootNode(rootConceptNid, tree));
            } else if (tree != null) {
                EntityVertex root = tree.root();
                if (root.getMeaningNid() == TinkarTerm.DEFINITION_ROOT.nid()) {
                    appendChildren(treeContainer, root, tree);
                } else {
                    treeContainer.getChildren().add(buildVertex(root, tree));
                }
            }
        }
        afterRebuild(tree);
    }

    /**
     * Builds the root row — the subject concept's chip with the definition indented below it —
     * shown when the control knows its {@link KLReadOnlyDiTreeControl#rootConceptNidProperty()
     * root concept}.
     */
    private Node rootNode(int rootConceptNid, DiTreeEntity tree) {
        VBox childrenBox = new VBox();
        childrenBox.getStyleClass().add("ditree-children");
        if (tree != null) {
            EntityVertex root = tree.root();
            if (root.getMeaningNid() == TinkarTerm.DEFINITION_ROOT.nid()) {
                appendChildren(childrenBox, root, tree);
            } else {
                childrenBox.getChildren().add(buildVertex(root, tree));
            }
        }

        HBox header = new HBox();
        header.getStyleClass().add("ditree-clause-header");
        header.setAlignment(Pos.CENTER_LEFT);
        boolean hasChildren = !childrenBox.getChildren().isEmpty();
        if (hasChildren) {
            header.getChildren().add(createDisclosureWithChildren(childrenBox));
        }
        ComponentItemNode rootChip =
                ComponentItemNodeFactory.create(getSkinnable().getComponentItemResolver().apply(rootConceptNid));
        rootChip.setShowDragHandleOnHover(true);
        rootChip.setMaxWidth(Double.MAX_VALUE);
        HBox.setHgrow(rootChip, Priority.ALWAYS);
        header.getChildren().add(rootChip);
        decorateRootHeader(header, childrenBox);

        NodeUtils.setShowing(childrenBox, hasChildren);
        VBox node = new VBox(header, childrenBox);
        node.getStyleClass().add("ditree-node");
        return node;
    }

    /**
     * Hook for the editing subclass to add affordances (add button, structure context menu) to the
     * root row. Default: no-op.
     *
     * @param header      the root header row
     * @param childrenBox the container holding the definition's sets, where new sets mount
     */
    protected void decorateRootHeader(HBox header, VBox childrenBox) {
    }

    /**
     * Whether the tree renders when the value is null — the editing subclass returns true so an
     * empty definition still shows the root row to build from. Default: false.
     */
    protected boolean renderEmptyDefinition() {
        return false;
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
            return createClauseNode(vertex, tree, "necessary-set", "Necessary set");
        }
        if (meaning == TinkarTerm.SUFFICIENT_SET.nid()) {
            return createClauseNode(vertex, tree, "sufficient-set", "Sufficient set");
        }
        if (meaning == TinkarTerm.INCLUSION_SET.nid()) {
            return createClauseNode(vertex, tree, "inclusion-set", "Inclusion set");
        }
        if (meaning == TinkarTerm.CONCEPT_REFERENCE.nid()) {
            ConceptFacade concept = vertex.propertyFast(TinkarTerm.CONCEPT_REFERENCE);
            return conceptRow(vertex, concept.nid());
        }
        if (meaning == TinkarTerm.ROLE.nid()) {
            ConceptFacade roleType = vertex.propertyFast(TinkarTerm.ROLE_TYPE);
            if (roleType != null && PublicId.equals(roleType.publicId(), TinkarTerm.ROLE_GROUP)) {
                return createClauseNode(vertex, tree, "role-group", "Role group");
            }
            return roleRow(vertex, tree, roleType);
        }
        if (meaning == TinkarTerm.INTERVAL_ROLE.nid()) {
            return intervalRow(vertex);
        }
        // Features, property sets, … — fall back to a plain clause header for v1.
        IntFunction<String> descriptionResolver = getSkinnable().getDescriptionResolver();
        String clauseText = descriptionResolver != null ? descriptionResolver.apply(meaning) : Integer.toString(meaning);
        return createClauseNode(vertex, tree, "feature", clauseText);
    }

    /**
     * Builds a clause node: a header row (accent bar, disclosure toggle, small-caps clause label)
     * with the vertex's children indented below it. The children box is always part of the clause
     * (hidden while empty), so the editing subclass can insert template rows into empty clauses.
     */
    private Node createClauseNode(EntityVertex vertex, DiTreeEntity tree, String barStyleClass, String clauseText) {
        VBox childrenBox = new VBox();
        childrenBox.getStyleClass().add("ditree-children");
        appendChildren(childrenBox, vertex, tree);

        HBox header = new HBox();
        header.getStyleClass().add("ditree-clause-header");
        header.setAlignment(Pos.CENTER_LEFT);
        header.getChildren().add(clauseBar(barStyleClass));
        boolean hasChildren = !childrenBox.getChildren().isEmpty();
        if (hasChildren) {
            header.getChildren().add(createDisclosureWithChildren(childrenBox));
        }
        header.getChildren().add(clauseLabel(clauseText));
        decorateClauseHeader(header, childrenBox, vertex);

        NodeUtils.setShowing(childrenBox, hasChildren);
        VBox clause = new VBox(header, childrenBox);
        clause.getStyleClass().add("ditree-node");
        return clause;
    }

    /**
     * Builds an expand/collapse toggle that shows or hides the given child container (default
     * expanded) — so sets and role groups can be collapsed.
     */
    private Label createDisclosureWithChildren(VBox childrenBox) {
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
    private Node conceptRow(EntityVertex vertex, int nid) {
        Node itemNode = createChipNode(vertex, nid, ChipKind.CONCEPT_REFERENCE);
        if (itemNode instanceof Region region) {
            region.setMaxWidth(Double.MAX_VALUE);
        }
        HBox row = new HBox(itemNode);
        row.getStyleClass().add("ditree-concept-row");
        row.setAlignment(Pos.CENTER_LEFT);
        HBox.setHgrow(itemNode, Priority.ALWAYS);
        decorateConceptRow(row, vertex);
        return row;
    }

    /**
     * Hook for the editing subclass to add affordances (edit/remove context menu) to an is-a row.
     * Default: no-op.
     *
     * @param row    the concept reference row
     * @param vertex the concept reference vertex
     */
    protected void decorateConceptRow(HBox row, EntityVertex vertex) {
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
            row.getChildren().add(createChipNode(vertex, roleType.nid(), ChipKind.ROLE_TYPE));
        }
        row.getChildren().add(operator(") → ["));
        for (EntityVertex child : tree.successors(vertex)) {
            if (child.getMeaningNid() == TinkarTerm.CONCEPT_REFERENCE.nid()) {
                ConceptFacade restriction = child.propertyFast(TinkarTerm.CONCEPT_REFERENCE);
                row.getChildren().add(createChipNode(child, restriction.nid(), ChipKind.ROLE_RESTRICTION));
            }
        }
        row.getChildren().add(operator("]"));
        decorateRoleRow(row, vertex);
        return row;
    }

    /**
     * Builds the chip for a concept in the tree. The default is a read-only
     * {@link ComponentItemNode} with the hover drag handle; the editing subclass overrides this to
     * add click-to-edit behavior.
     *
     * @param vertex     the vertex the chip belongs to: the concept reference vertex itself for
     *                   {@link ChipKind#CONCEPT_REFERENCE} and {@link ChipKind#ROLE_RESTRICTION},
     *                   or the role vertex for {@link ChipKind#ROLE_TYPE}
     * @param conceptNid the concept the chip renders
     * @param kind       the editing role the chip plays
     */
    protected Node createChipNode(EntityVertex vertex, int conceptNid, ChipKind kind) {
        ComponentItemNode itemNode =
                ComponentItemNodeFactory.create(getSkinnable().getComponentItemResolver().apply(conceptNid));
        itemNode.setShowDragHandleOnHover(true);
        return itemNode;
    }

    /**
     * Hook for the editing subclass to add affordances (add button, structure context menu) to a
     * clause header. Called for sets, role groups and fallback clauses. Default: no-op.
     *
     * @param header      the header row
     * @param childrenBox the container holding the clause's children, where new child rows mount
     * @param vertex      the clause vertex
     */
    protected void decorateClauseHeader(HBox header, VBox childrenBox, EntityVertex vertex) {
    }

    /**
     * Hook for the editing subclass to add affordances (structure context menu) to a role row.
     * Default: no-op.
     *
     * @param row    the role row
     * @param vertex the role vertex
     */
    protected void decorateRoleRow(HBox row, EntityVertex vertex) {
    }

    /**
     * Builds the {@code I (role type) → (lower,upper) unit} row for an interval role — the same
     * shape the classic axiom control renders ({@code IntervalUtil.getIntervalRoleString}), with
     * the role type and unit of measure as inline component chips.
     */
    private Node intervalRow(EntityVertex vertex) {
        HBox row = new HBox();
        row.getStyleClass().add("ditree-interval-row");
        row.setAlignment(Pos.CENTER_LEFT);
        row.getChildren().add(clauseBar("interval-role"));
        row.getChildren().add(operator("I ("));
        ConceptFacade roleType = vertex.propertyFast(TinkarTerm.INTERVAL_ROLE_TYPE);
        row.getChildren().add(createChipNode(vertex, roleType.nid(), ChipKind.INTERVAL_ROLE_TYPE));
        row.getChildren().add(operator(") →"));
        row.getChildren().add(createIntervalBoundsNode(vertex));
        ConceptFacade unit = vertex.propertyFast(TinkarTerm.UNIT_OF_MEASURE);
        row.getChildren().add(createChipNode(vertex, unit.nid(), ChipKind.INTERVAL_UNIT));
        decorateIntervalRow(row, vertex);
        return row;
    }

    /**
     * Builds the bounds part of an interval row — {@code (lower,upper)}, where each bracket is
     * round when its bound is open and square when closed (the {@code Interval} notation). The
     * default is a plain label; the editing subclass overrides this with inline bound editors.
     *
     * @param vertex the interval role vertex
     */
    protected Node createIntervalBoundsNode(EntityVertex vertex) {
        BigDecimal lowerBound = vertex.propertyFast(TinkarTerm.INTERVAL_LOWER_BOUND);
        boolean lowerOpen = vertex.propertyFast(TinkarTerm.LOWER_BOUND_OPEN);
        BigDecimal upperBound = vertex.propertyFast(TinkarTerm.INTERVAL_UPPER_BOUND);
        boolean upperOpen = vertex.propertyFast(TinkarTerm.UPPER_BOUND_OPEN);
        return operator((lowerOpen ? "(" : "[") + lowerBound.toPlainString() + ","
                + upperBound.toPlainString() + (upperOpen ? ")" : "]"));
    }

    /**
     * Hook for the editing subclass to add affordances (structure context menu) to an interval
     * role row. Default: no-op.
     *
     * @param row    the interval role row
     * @param vertex the interval role vertex
     */
    protected void decorateIntervalRow(HBox row, EntityVertex vertex) {
    }

    /**
     * Hook invoked after the tree has been (re)built — also when the value is null. The editing
     * subclass appends its add-set affordance here. Default: no-op.
     *
     * @param tree the rendered tree, may be null
     */
    protected void afterRebuild(DiTreeEntity tree) {
    }

    protected final Label clauseLabel(String clauseText) {
        Label label = new Label(clauseText.toUpperCase());
        label.getStyleClass().add("ditree-clause-label");
        return label;
    }

    protected final Region clauseBar(String styleClass) {
        Region bar = new Region();
        bar.getStyleClass().addAll("ditree-clause-bar", styleClass);
        return bar;
    }

    protected final Label operator(String glyph) {
        Label label = new Label(glyph);
        label.getStyleClass().add("ditree-op");
        // Never shrink the operator below its glyph, so neighbouring chips give way instead of
        // crowding out the ∃/→.
        label.setMinWidth(Region.USE_PREF_SIZE);
        return label;
    }
}