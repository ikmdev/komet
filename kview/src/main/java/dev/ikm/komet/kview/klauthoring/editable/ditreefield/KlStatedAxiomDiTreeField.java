package dev.ikm.komet.kview.klauthoring.editable.ditreefield;

import dev.ikm.komet.framework.observable.ObservableEntityHandle;
import dev.ikm.komet.framework.observable.ObservableField;
import dev.ikm.komet.framework.observable.ObservableSemanticVersion;
import dev.ikm.komet.framework.observable.ObservableStamp;
import dev.ikm.komet.framework.panel.axiom.AxiomSubjectRecord;
import dev.ikm.komet.framework.performance.Measures;
import dev.ikm.komet.framework.performance.Topic;
import dev.ikm.komet.framework.performance.impl.ObservationRecord;
import dev.ikm.komet.framework.rulebase.Consequence;
import dev.ikm.komet.framework.rulebase.ConsequenceAction;
import dev.ikm.komet.framework.rulebase.ConsequenceMenu;
import dev.ikm.komet.framework.rulebase.RuleService;
import dev.ikm.komet.framework.view.ObservableView;
import dev.ikm.komet.framework.view.ViewProperties;
import dev.ikm.komet.kview.controls.AxiomRuleAction;
import dev.ikm.komet.kview.controls.KLDiTreeControl;
import dev.ikm.komet.kview.controls.KLDiTreeControlFactory;
import dev.ikm.komet.kview.klfields.BaseDefaultKlField;
import dev.ikm.komet.layout.version.field.KlDirectedTreeField;
import dev.ikm.tinkar.common.util.broadcast.Subscriber;
import dev.ikm.tinkar.coordinate.logic.PremiseType;
import dev.ikm.tinkar.coordinate.stamp.calculator.Latest;
import dev.ikm.tinkar.coordinate.view.calculator.ViewCalculator;
import dev.ikm.tinkar.entity.Entity;
import dev.ikm.tinkar.entity.EntityHandle;
import dev.ikm.tinkar.entity.EntityService;
import dev.ikm.tinkar.entity.SemanticEntityVersion;
import dev.ikm.tinkar.entity.SemanticRecord;
import dev.ikm.tinkar.entity.StampEntity;
import dev.ikm.tinkar.entity.graph.DiTreeEntity;
import dev.ikm.tinkar.entity.graph.EntityVertex;
import dev.ikm.tinkar.entity.graph.adaptor.axiom.LogicalExpressionBuilder;
import dev.ikm.tinkar.entity.transaction.Transaction;
import dev.ikm.tinkar.terms.State;
import javafx.application.Platform;
import javafx.event.ActionEvent;
import org.controlsfx.control.action.Action;
import org.eclipse.collections.api.factory.Lists;
import org.eclipse.collections.api.list.ImmutableList;

import java.util.ArrayList;
import java.util.List;

/**
 * <p>Inline-editing field for the stated logical definition, shown directly in the window body
 * (the axioms area) instead of the read-only tree. Renders the definition as an editable axiom
 * tree ({@link KLDiTreeControl}): concepts are edited by clicking their chips, structure through
 * the clause header menus.</p>
 *
 * <p>The structure menus are sourced from the axiom rules engine — the same Evrete rules
 * ({@code AxiomFocusedRules}) that drive the classic axiom control's context menu — through the
 * control's rule actions provider. The skin substitutes its inline treatments for the actions it
 * recognizes; unrecognized actions run the engine-generated action itself, and the control's value
 * is refreshed from the store afterwards (also whenever any other writer updates the semantic).</p>
 *
 * <p>Each applied edit — a picked concept, an added or removed axiom, a changed set type — is
 * persisted immediately as a new committed semantic version, following the classic axiom editor
 * precedent ({@code AbstractAxiomAction}), where every rule action writes and commits its own
 * transaction. Templates for new content live only in the control until their concepts are
 * picked, so incomplete edits never touch the store.</p>
 */
public class KlStatedAxiomDiTreeField extends BaseDefaultKlField<DiTreeEntity> implements KlDirectedTreeField<DiTreeEntity> {

    private final int semanticNid;

    /** Strong reference — the entity provider holds its subscribers weakly. */
    private Subscriber<Integer> entityChangeSubscriber;

    public KlStatedAxiomDiTreeField(ObservableField<DiTreeEntity> observableDiTreeField, ObservableView observableView, ObservableStamp stamp4field) {
        KLDiTreeControl control = KLDiTreeControlFactory.create(observableView.calculator());
        super(observableDiTreeField, observableView, stamp4field, control);

        this.semanticNid = observableDiTreeField.field().nid();
        control.setTitle(getTitle());
        control.setRootConceptNid(EntityHandle.getSemanticOrThrow(semanticNid).referencedComponentNid());
        control.setValue(observableDiTreeField.editableValueProperty().get());
        // The control's value only changes when an edit is applied in the tree (or refreshed from
        // the store); persist() skips definitions the store already holds.
        control.valueProperty().subscribe((oldTree, newTree) -> {
            if (newTree != null && !newTree.equals(oldTree)) {
                persist(newTree);
            }
        });
    }

    /**
     * Creates the field with its structure menus sourced from the axiom rules engine, which needs
     * the full {@link ViewProperties} that the {@code KlFieldFactory} contract does not carry —
     * the same extra-parameter precedent as {@code KlReadOnlyComponentSetFieldFactory}.
     */
    public KlStatedAxiomDiTreeField(ObservableField<DiTreeEntity> observableDiTreeField, ViewProperties viewProperties, ObservableStamp stamp4field) {
        this(observableDiTreeField, viewProperties.nodeView(), stamp4field);
        KLDiTreeControl control = (KLDiTreeControl) fxObject();
        control.setRuleActionsProvider(vertex -> ruleActions(vertex, viewProperties, control));
        // Engine-run actions (and any other writer, e.g. the classic axiom control in another
        // window) persist straight to the store; mirror those writes back into the control.
        entityChangeSubscriber = changedNid -> {
            if (changedNid == semanticNid) {
                Platform.runLater(() -> refreshFromStore(control));
            }
        };
        Entity.provider().addSubscriberWithWeakReference(entityChangeSubscriber);
    }

    /**
     * Presents the vertex to the rules engine as an {@code AXIOM_FOCUSED} observation — exactly
     * like {@code AxiomRulesMenu} — and adapts the consequences for the skin. A null vertex stands
     * for the definition root; when no definition exists yet, an empty one (root vertex only) is
     * presented so the engine offers the initial add-set actions.
     */
    private List<AxiomRuleAction> ruleActions(EntityVertex vertex, ViewProperties viewProperties, KLDiTreeControl control) {
        DiTreeEntity tree = control.getValue() != null ? control.getValue() : emptyDefinition();
        int vertexIndex = vertex != null ? vertex.vertexIndex() : tree.root().vertexIndex();
        ObservableSemanticVersion semanticVersion = ObservableEntityHandle.get(semanticNid)
                .asSemantic().orElseThrow()
                .getSnapshot(viewProperties.calculator())
                .getLatestVersion().get();
        AxiomSubjectRecord axiomSubject = new AxiomSubjectRecord(vertexIndex, tree, semanticVersion,
                PremiseType.STATED, control);
        ObservationRecord observation = new ObservationRecord(Topic.AXIOM_FOCUSED, axiomSubject, Measures.present());
        ImmutableList<Consequence<?>> consequences = RuleService.get().execute("Axiom tree structure menu",
                Lists.immutable.of(observation), viewProperties, viewProperties.nodeView().editCoordinate());

        List<AxiomRuleAction> actions = new ArrayList<>();
        for (Consequence<?> consequence : consequences) {
            switch (consequence) {
                case ConsequenceAction consequenceAction -> {
                    if (consequenceAction.generatedAction() instanceof Action action) {
                        actions.add(new AxiomRuleAction.Command(action.getClass().getSimpleName(), action.getText(),
                                () -> {
                                    action.handle(new ActionEvent());
                                    refreshFromStore(control);
                                }));
                    }
                }
                case ConsequenceMenu consequenceMenu -> actions.add(new AxiomRuleAction.Submenu(
                        consequenceMenu.generatedMenu().getText(), consequenceMenu.generatedMenu()));
                default -> { }
            }
        }
        return actions;
    }

    private static DiTreeEntity emptyDefinition() {
        return switch (new LogicalExpressionBuilder().build().sourceGraph()) {
            case DiTreeEntity tree -> tree;
            case DiTreeEntity.Builder builder -> builder.build();
            default -> throw new IllegalStateException("Unexpected source graph type");
        };
    }

    private void refreshFromStore(KLDiTreeControl control) {
        Latest<SemanticEntityVersion> latestVersion = observableView.calculator().latest(semanticNid);
        latestVersion.ifPresent(version -> control.setValue((DiTreeEntity) version.fieldValues().get(0)));
    }

    /**
     * Not the only writer of this semantic: unmapped engine actions persist through
     * {@code AbstractAxiomAction.putUpdatedDiTree}, which stamps from the edit coordinate (this
     * method stamps from the view coordinate — same values while no override diverges them). A
     * change to stamping or commit policy here must be mirrored there, or both funneled through
     * one shared helper.
     */
    private void persist(DiTreeEntity newTree) {
        ViewCalculator viewCalculator = observableView.calculator();
        Latest<SemanticEntityVersion> latestVersion = viewCalculator.latest(semanticNid);
        if (latestVersion.isPresent() && newTree.equals(latestVersion.get().fieldValues().get(0))) {
            // The store already holds this definition — the value was refreshed after an
            // engine-run action or another writer persisted it.
            return;
        }
        Transaction transaction = Transaction.make();
        StampEntity<?> stamp = transaction.getStamp(State.ACTIVE,
                viewCalculator.viewCoordinateRecord().getAuthorNidForChanges(),
                viewCalculator.viewCoordinateRecord().getDefaultModuleNid(),
                viewCalculator.viewCoordinateRecord().getDefaultPathNid());
        transaction.addComponent(semanticNid);
        SemanticRecord updated = viewCalculator.updateFields(semanticNid, Lists.immutable.of(newTree), stamp.nid());
        EntityService.get().putEntity(updated);
        transaction.commit();
    }
}
