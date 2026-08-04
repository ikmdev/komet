package dev.ikm.komet.kview.klauthoring.editable.ditreefield;

import dev.ikm.komet.framework.observable.ObservableField;
import dev.ikm.komet.framework.observable.ObservableStamp;
import dev.ikm.komet.framework.view.ObservableView;
import dev.ikm.komet.kview.controls.KLDiTreeControl;
import dev.ikm.komet.kview.controls.KLDiTreeControlFactory;
import dev.ikm.komet.kview.klfields.BaseDefaultKlField;
import dev.ikm.komet.layout.version.field.KlDirectedTreeField;
import dev.ikm.tinkar.coordinate.view.calculator.ViewCalculator;
import dev.ikm.tinkar.entity.EntityHandle;
import dev.ikm.tinkar.entity.EntityService;
import dev.ikm.tinkar.entity.SemanticRecord;
import dev.ikm.tinkar.entity.StampEntity;
import dev.ikm.tinkar.entity.graph.DiTreeEntity;
import dev.ikm.tinkar.entity.transaction.Transaction;
import dev.ikm.tinkar.terms.State;
import org.eclipse.collections.api.factory.Lists;

/**
 * <p>Inline-editing field for the stated logical definition, shown directly in the window body
 * (the axioms area) instead of the read-only tree. Renders the definition as an editable axiom
 * tree ({@link KLDiTreeControl}): concepts are edited by clicking their chips, structure through
 * the clause header menus and the add-set affordance.</p>
 *
 * <p>Each applied edit — a picked concept, an added or removed axiom, a changed set type — is
 * persisted immediately as a new committed semantic version, following the classic axiom editor
 * precedent ({@code AbstractAxiomAction}), where every rule action writes and commits its own
 * transaction. Templates for new content live only in the control until their concepts are
 * picked, so incomplete edits never touch the store.</p>
 */
public class KlStatedAxiomDiTreeField extends BaseDefaultKlField<DiTreeEntity> implements KlDirectedTreeField<DiTreeEntity> {

    private final int semanticNid;

    public KlStatedAxiomDiTreeField(ObservableField<DiTreeEntity> observableDiTreeField, ObservableView observableView, ObservableStamp stamp4field) {
        KLDiTreeControl control = KLDiTreeControlFactory.create(observableView.calculator());
        super(observableDiTreeField, observableView, stamp4field, control);

        this.semanticNid = observableDiTreeField.field().nid();
        control.setTitle(getTitle());
        control.setRootConceptNid(EntityHandle.getSemanticOrThrow(semanticNid).referencedComponentNid());
        control.setValue(observableDiTreeField.editableValueProperty().get());
        // The control's value only changes when the user applies an edit in the tree (the field
        // never pushes values from below), so every change persists a new version.
        control.valueProperty().subscribe((oldTree, newTree) -> {
            if (newTree != null && !newTree.equals(oldTree)) {
                persist(newTree);
            }
        });
    }

    private void persist(DiTreeEntity newTree) {
        ViewCalculator viewCalculator = observableView.calculator();
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
