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
package dev.ikm.komet.layout_engine.component.area;

import dev.ikm.komet.framework.StyleClasses;
import dev.ikm.komet.framework.controls.KonceptBadge;
import dev.ikm.komet.framework.observable.ObservableSemanticVersion;
import dev.ikm.komet.framework.panel.axiom.AxiomRulesMenu;
import dev.ikm.komet.framework.view.ViewProperties;
import dev.ikm.tinkar.common.id.PublicId;
import dev.ikm.tinkar.coordinate.logic.PremiseType;
import dev.ikm.tinkar.entity.graph.DiTreeEntity;
import dev.ikm.tinkar.entity.graph.EntityVertex;
import dev.ikm.tinkar.terms.ConceptFacade;
import dev.ikm.tinkar.terms.TinkarTerm;
import javafx.geometry.Pos;
import javafx.scene.Cursor;
import javafx.scene.Node;
import javafx.scene.control.Label;
import javafx.scene.control.ScrollPane;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.Region;
import javafx.scene.layout.VBox;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * The refreshed, model-driven axiom renderer (ike-issues#639): it walks a concept's logical
 * definition ({@code DiTree<EntityVertex>}) and renders each concept reference as a
 * {@link KonceptBadge} (LifeHash identicon + small-caps pill + taxonomic status glyph), reusing the
 * historic clause-type accent bars ({@code .def-*} in {@code komet.css}) for the structure —
 * necessary/sufficient/inclusion sets, role groups, and existential ({@code ∃}) roles.
 *
 * <p>It is the "newly proposed tooling" counterpart to the classic {@code AxiomView}; both are
 * offered as swappable supplemental areas (see {@code KonceptAxiomTreeArea} /
 * {@code ClassicAxiomArea}). This v1 renders fully expanded and falls back to a plain description
 * label for clause kinds it does not yet special-case (features, intervals, property sets).
 */
public final class KonceptAxiomTree {

    private static final Logger LOG = LoggerFactory.getLogger(KonceptAxiomTree.class);

    private KonceptAxiomTree() {
    }

    /**
     * Renders the logical definition carried by an axiom semantic version as a KonceptBadge tree.
     *
     * @param axiomVersion   the concept's stated/inferred axiom semantic version
     * @param premiseType    the premise being rendered (drives badge status)
     * @param viewProperties the view to resolve names, status and identicons against
     * @return a scrollable node containing the rendered tree, or a message node if no definition
     */
    public static Node create(ObservableSemanticVersion axiomVersion, PremiseType premiseType,
                              ViewProperties viewProperties) {
        DiTreeEntity axiomTree = extractTree(axiomVersion, viewProperties);
        if (axiomTree == null) {
            return new Label("No logical definition for this concept.");
        }
        int subjectNid = axiomVersion.referencedComponentNid();
        VBox container = new VBox(buildVertex(axiomTree.root(), axiomTree, axiomVersion, viewProperties, premiseType, subjectNid));
        ScrollPane scroll = new ScrollPane(container);
        scroll.setFitToWidth(true);
        return scroll;
    }

    private static DiTreeEntity extractTree(ObservableSemanticVersion axiomVersion, ViewProperties viewProperties) {
        DiTreeEntity[] holder = new DiTreeEntity[1];
        try {
            viewProperties.calculator().getFieldForSemanticWithPurpose(axiomVersion, TinkarTerm.LOGICAL_DEFINITION)
                    .ifPresent(field -> holder[0] = (DiTreeEntity) field.value());
        } catch (RuntimeException e) {
            LOG.warn("Could not read logical definition from axiom semantic", e);
        }
        return holder[0];
    }

    private static BorderPane buildVertex(EntityVertex vertex, DiTreeEntity tree, ObservableSemanticVersion axiomVersion,
                                          ViewProperties viewProperties, PremiseType premiseType, int subjectNid) {
        int meaning = vertex.getMeaningNid();
        BorderPane node = new BorderPane();
        Node content;
        boolean renderChildren = true;

        if (meaning == TinkarTerm.DEFINITION_ROOT.nid()) {
            node.getStyleClass().add(StyleClasses.DEF_ROOT.toString());
            content = conceptBadge(subjectNid, viewProperties, premiseType, true);
        } else if (meaning == TinkarTerm.NECESSARY_SET.nid()) {
            node.getStyleClass().add(StyleClasses.DEF_NECESSARY_SET.toString());
            content = clauseLabel("Necessary set");
        } else if (meaning == TinkarTerm.SUFFICIENT_SET.nid()) {
            node.getStyleClass().add(StyleClasses.DEF_SUFFICIENT_SET.toString());
            content = clauseLabel("Sufficient set");
        } else if (meaning == TinkarTerm.INCLUSION_SET.nid()) {
            node.getStyleClass().add(StyleClasses.DEF_INCLUSION_SET.toString());
            content = clauseLabel("Inclusion set");
        } else if (meaning == TinkarTerm.CONCEPT_REFERENCE.nid()) {
            node.getStyleClass().add(StyleClasses.DEF_CONCEPT.toString());
            ConceptFacade concept = vertex.propertyFast(TinkarTerm.CONCEPT_REFERENCE);
            content = conceptBadge(concept.nid(), viewProperties, premiseType, true);
            renderChildren = false;
        } else if (meaning == TinkarTerm.ROLE.nid()) {
            ConceptFacade roleType = vertex.propertyFast(TinkarTerm.ROLE_TYPE);
            if (roleType != null && PublicId.equals(roleType.publicId(), TinkarTerm.ROLE_GROUP)) {
                node.getStyleClass().add(StyleClasses.DEF_ROLE_GROUP.toString());
                content = clauseLabel("Role group");
            } else {
                node.getStyleClass().add(StyleClasses.DEF_ROLE.toString());
                content = roleRow(vertex, tree, viewProperties, premiseType, roleType);
                renderChildren = false;
            }
        } else {
            // Features, intervals, property sets, … — fall back to a plain clause label for v1.
            node.getStyleClass().add(StyleClasses.DEF_CONCEPT.toString());
            content = clauseLabel(viewProperties.calculator().getDescriptionTextOrNid(meaning));
        }
        VBox childBox = null;
        if (renderChildren) {
            childBox = new VBox();
            childBox.getStyleClass().add("koncept-axiom-children");
            for (EntityVertex child : tree.successors(vertex)) {
                if (child.getMeaningNid() == TinkarTerm.AND.nid()) {
                    // AND is a transparent operator: flatten its children into this level.
                    for (EntityVertex andChild : tree.successors(child)) {
                        childBox.getChildren().add(buildVertex(andChild, tree, axiomVersion, viewProperties, premiseType, subjectNid));
                    }
                } else {
                    childBox.getChildren().add(buildVertex(child, tree, axiomVersion, viewProperties, premiseType, subjectNid));
                }
            }
        }
        boolean hasChildren = childBox != null && !childBox.getChildren().isEmpty();

        // Header row: an optional expand/collapse toggle (for clauses with children, e.g. role
        // groups and sets) followed by the clause content.
        HBox header = new HBox(4);
        header.setAlignment(Pos.CENTER_LEFT);
        header.getStyleClass().add("koncept-axiom-header");
        if (hasChildren) {
            header.getChildren().add(disclosure(childBox));
        }
        header.getChildren().add(content);
        if (premiseType == PremiseType.STATED) {
            // Hover-reveal affordance: a subtle pencil appears on hover and right-click opens the
            // Evrete rule actions for this clause (stated axioms only).
            header.getStyleClass().add("koncept-axiom-actionable");
            header.setCursor(Cursor.HAND);
            Region spacer = new Region();
            HBox.setHgrow(spacer, Priority.ALWAYS);
            Label actionGlyph = new Label("✎");
            actionGlyph.getStyleClass().add("koncept-axiom-action-glyph");
            actionGlyph.setVisible(false);
            actionGlyph.setManaged(false);
            header.getChildren().addAll(spacer, actionGlyph);
            header.setOnMouseEntered(event -> {
                actionGlyph.setVisible(true);
                actionGlyph.setManaged(true);
            });
            header.setOnMouseExited(event -> {
                actionGlyph.setVisible(false);
                actionGlyph.setManaged(false);
            });
            header.setOnContextMenuRequested(event -> {
                AxiomRulesMenu.show(vertex, tree, axiomVersion, premiseType, viewProperties, header,
                        event.getScreenX(), event.getScreenY());
                event.consume();
            });
        }
        node.setTop(header);
        if (hasChildren) {
            node.setCenter(childBox);
        }
        return node;
    }

    /**
     * Builds an expand/collapse toggle that shows or hides the given child container (default
     * expanded) — so role groups and sets can be collapsed.
     */
    private static Label disclosure(VBox childBox) {
        Label toggle = new Label("▾");
        toggle.getStyleClass().add("koncept-axiom-toggle");
        toggle.setOnMouseClicked(event -> {
            boolean show = !childBox.isVisible();
            childBox.setVisible(show);
            childBox.setManaged(show);
            toggle.setText(show ? "▾" : "▸");
            event.consume();
        });
        return toggle;
    }

    /**
     * Builds the {@code ∃ (role type) → [value]} row for an existential role restriction.
     */
    private static Node roleRow(EntityVertex vertex, DiTreeEntity tree, ViewProperties viewProperties,
                                PremiseType premiseType, ConceptFacade roleType) {
        HBox row = new HBox(4);
        row.setAlignment(Pos.CENTER_LEFT);
        row.getChildren().add(operatorGlyph("∃"));
        if (roleType != null) {
            row.getChildren().add(conceptBadge(roleType.nid(), viewProperties, premiseType, false));
        }
        row.getChildren().add(operatorGlyph("→"));
        for (EntityVertex child : tree.successors(vertex)) {
            if (child.getMeaningNid() == TinkarTerm.CONCEPT_REFERENCE.nid()) {
                ConceptFacade value = child.propertyFast(TinkarTerm.CONCEPT_REFERENCE);
                row.getChildren().add(conceptBadge(value.nid(), viewProperties, premiseType, false));
            }
        }
        return row;
    }

    private static KonceptBadge conceptBadge(int nid, ViewProperties viewProperties, PremiseType premiseType,
                                             boolean showStatus) {
        KonceptBadge badge = new KonceptBadge(nid, viewProperties, showStatus);
        badge.setPremiseType(premiseType);
        return badge;
    }

    private static Label clauseLabel(String text) {
        Label label = new Label(text);
        label.getStyleClass().add("koncept-axiom-clause");
        return label;
    }

    private static Label operatorGlyph(String glyph) {
        Label label = new Label(glyph);
        label.getStyleClass().add("koncept-axiom-op");
        return label;
    }
}
