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
package dev.ikm.komet.kview.mvvm.view.genpurpose;

import dev.ikm.komet.framework.observable.ObservableComposer;
import dev.ikm.komet.framework.observable.ObservableEntity;
import dev.ikm.komet.framework.observable.ObservableEntityHandle;
import dev.ikm.komet.framework.observable.ObservableField;
import dev.ikm.komet.framework.observable.ObservablePattern;
import dev.ikm.komet.framework.observable.ObservablePatternVersion;
import dev.ikm.komet.framework.observable.ObservableSemantic;
import dev.ikm.komet.framework.observable.ObservableSemanticVersion;
import dev.ikm.komet.framework.view.ViewProperties;
import dev.ikm.komet.kview.mvvm.viewmodel.FormViewModel.FormMode;
import dev.ikm.komet.layout.KlTerms;
import dev.ikm.komet.layout.PatternDefinitionSeeder;
import dev.ikm.komet.layout.PatternFieldDefaults;
import dev.ikm.tinkar.common.id.PublicIds;
import dev.ikm.tinkar.entity.Entity;
import dev.ikm.tinkar.entity.EntityHandle;
import dev.ikm.tinkar.entity.EntityVersion;
import dev.ikm.tinkar.entity.SemanticEntity;
import dev.ikm.tinkar.entity.SemanticEntityVersion;
import dev.ikm.tinkar.entity.transaction.Transaction;
import dev.ikm.tinkar.terms.EntityFacade;
import dev.ikm.tinkar.terms.State;
import javafx.beans.property.ObjectProperty;
import javafx.beans.property.ReadOnlyBooleanProperty;
import javafx.beans.property.ReadOnlyBooleanWrapper;
import javafx.beans.property.ReadOnlyObjectProperty;
import javafx.beans.property.ReadOnlyObjectWrapper;
import javafx.beans.property.SimpleObjectProperty;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.function.Consumer;

/**
 * What a general-purpose KL window is editing: the component it frames, whether that component
 * exists yet, and the transaction its edits stay uncommitted in until they commit. Shared by the window's
 * controller, its properties panel and the panel's forms, and free of any scene graph.
 *
 * <p>A window opened from the Journal's "+" button frames a component that doesn't exist yet:
 * it starts in {@link FormMode#CREATE create mode} with no component, gets one — still
 * uncommitted — when the user authors the first semantic, and {@link #enterEditMode() enters
 * edit mode} once that component is committed. A window opened on an existing component
 * ("Open as ...") starts in edit mode with it.
 *
 * <p>Every edit goes through the session's {@link #createOrGetComposer() composer}: a new component, a
 * new or edited semantic, is saved as an uncommitted version in the composer's open transaction
 * and stays there until the session {@link #commit() commits}, which commits everything left uncommitted
 * since the last commit together.
 *
 * <p>The one exception is a semantic that exists only so its edit form can show
 * ({@link #createSemanticInSingleSemanticTransaction}): until the form is submitted it lives in a
 * <em>single-semantic transaction</em>, a transaction that holds that one semantic and nothing
 * else, so that cancelling the form can cancel the whole transaction, and the semantic with it,
 * without touching the window's other edits ({@link #cancelSingleSemanticTransaction}). The store
 * merges an entity's versions by stamp, so a version only ever leaves it when its stamp is
 * canceled, and the window transaction's stamp is shared by everything the window edited.
 */
public final class WindowEditSession {

    private final ViewProperties viewProperties;

    /** The composer the window's edits are saved uncommitted in; created on first use, replaced by each commit. */
    private ObservableComposer composer;

    /**
     * The composer of the pattern a create-mode standard Pattern window brought into existence
     * ({@link #createUncommittedComponent}), kept until the commit that finalizes it so the
     * pattern's inline definition is written through the same composer — a second composer of
     * the same uncommitted version would commit its own copy too. Null in edit mode, and again
     * once committed.
     */
    private ObservableComposer.EntityComposer<ObservablePatternVersion.Editable, ObservablePattern> newPatternComposer;

    /**
     * Composer for the window pattern's defaults semantic (see {@link PatternFieldDefaults}).
     * The defaults semantic commits in the defaults module rather than the edit coordinate's
     * default module, so it gets a transaction of its own, which the DEFAULTS form's Publish
     * button commits — the defaults are not part of the window's own uncommitted changes. Created on
     * first use and kept for the window's lifetime: a committed composer opens a fresh transaction
     * on its next compose.
     */
    private ObservableComposer defaultsComposer;

    /**
     * The composer of each single-semantic transaction still open, by the nid of the one semantic
     * it holds: a semantic created by {@link #createSemanticInSingleSemanticTransaction} whose edit
     * form has not been submitted or cancelled yet. {@link #endSingleSemanticTransactionOnSubmit}
     * and {@link #cancelSingleSemanticTransaction} take the entry out.
     */
    private final Map<Long, ObservableComposer> singleSemanticTransactionComposers = new HashMap<>();

    /**
     * The composers of the single-semantic transactions that {@link #commit()} commits together
     * with the window's: their semantic was submitted with its seeded values unchanged, so the
     * submit saved nothing into the window's transaction and the single-semantic transaction is
     * the only place the submitted semantic exists.
     */
    private final List<ObservableComposer> singleSemanticComposersToCommit = new ArrayList<>();

    /**
     * @param viewProperties the window's derived coordinate, which every edit reads and writes through
     * @param component      the component the window was opened on; null to create a new one
     */
    public WindowEditSession(ViewProperties viewProperties, EntityFacade component) {
        this.viewProperties = viewProperties;
        this.mode = new SimpleObjectProperty<>(component == null ? FormMode.CREATE : FormMode.EDIT);
        this.component = new ReadOnlyObjectWrapper<>(component);
    }

    public ViewProperties getViewProperties() {
        return viewProperties;
    }

    public boolean isCreateMode() {
        return mode.get() == FormMode.CREATE;
    }

    /** The window's component now exists: it was committed. */
    public void enterEditMode() {
        mode.set(FormMode.EDIT);
    }

    /**
     * The composer the window's edits are saved uncommitted in. Created on first use on the edit coordinate's
     * author, default module and path; a {@link #commit()} replaces it, so callers ask for it
     * per edit rather than keeping it. It {@linkplain ObservableComposer#joinPendingWork() joins
     * pending work}: an edit of a component another window or the assistant changed, under the
     * same author, module and path, continues that change's transaction, so the edit replaces the
     * pending version and one Publish or Discard settles both (IKE-Network/ike-issues#1326).
     */
    public ObservableComposer createOrGetComposer() {
        if (composer == null) {
            composer = newComposer("Edit Semantic Details").joinPendingWork();
            hasUncommittedChanges.bind(composer.hasUncommittedChangesProperty());
        }
        return composer;
    }

    /** A composer on the edit coordinate's author, default module and path. */
    private ObservableComposer newComposer(String transactionComment) {
        var editCoordinate = viewProperties.nodeView().editCoordinate();
        return ObservableComposer.create(
                viewProperties.calculator(),
                State.ACTIVE,
                editCoordinate.getAuthorForChanges(),
                editCoordinate.getDefaultModule(),
                editCoordinate.getDefaultPath(),
                transactionComment
        );
    }

    /**
     * Commits the composer's open transaction, finalizing everything uncommitted since the last
     * commit: submitted semantic versions and, in create mode, the lazily created component
     * itself — and, with it, the single-semantic transactions of the semantics submitted with
     * their seeded values unchanged (see {@link #endSingleSemanticTransactionOnSubmit}). The next
     * edit is saved uncommitted in a fresh transaction.
     */
    public void commit() {
        createOrGetComposer().commit();
        singleSemanticComposersToCommit.forEach(ObservableComposer::commit);
        singleSemanticComposersToCommit.clear();
        hasUncommittedChanges.unbind();
        composer = null;
        newPatternComposer = null;
        createOrGetComposer();
    }

    /**
     * Creates the window's component as a new, uncommitted entity — in create mode, where the
     * window was opened without one. The new component joins the composer's current transaction,
     * so it commits together with the semantic whose creation triggered it.
     *
     * @param pattern a Pattern when true (the standard Pattern window), a Concept otherwise
     * @return the new uncommitted component, already set as the window's component
     */
    public EntityFacade createUncommittedComponent(boolean pattern) {
        ObservableComposer.EntityComposer<?, ?> entityComposer;
        if (pattern) {
            newPatternComposer = createOrGetComposer().composePattern(PublicIds.newRandom());
            entityComposer = newPatternComposer;
        } else {
            entityComposer = createOrGetComposer().composeConcept(PublicIds.newRandom());
        }

        entityComposer.save(); // Save to create an uncommitted version

        EntityFacade newComponent = entityComposer.getEntity();
        setComponent(newComponent);
        return newComponent;
    }

    /**
     * Creates a new, uncommitted semantic of the pattern against the reference component, its
     * field values seeded by {@code seedFields} before the version is saved — in the window's
     * transaction, like a submitted edit, so it commits with the window's other edits and cannot
     * be dropped on its own. For a semantic that is final as soon as it is created (the seeded
     * stated definition); a semantic opened in the edit form, which the user may still cancel, is
     * created by {@link #createSemanticInSingleSemanticTransaction} instead.
     *
     * @param referenceComponent the reference component of the semantic
     * @param patternNid         the pattern of the semantic
     * @param seedFields         sets the new version's field values; given the pattern's fields, in order
     * @return the new uncommitted semantic
     */
    public SemanticEntity<SemanticEntityVersion> createSemanticInWindowTransaction(EntityFacade referenceComponent, long patternNid,
                                                                                   Consumer<List<ObservableField.Editable<?>>> seedFields) {
        return composeNewSemantic(createOrGetComposer(), referenceComponent, patternNid, seedFields);
    }

    /**
     * Creates a new semantic of the pattern against the reference component for its edit form to
     * show, seeded like {@link #createSemanticInWindowTransaction} but saved uncommitted in a
     * single-semantic transaction: a new transaction that holds this semantic and nothing else,
     * rather than the window's. It stays there until the form is submitted
     * ({@link #endSingleSemanticTransactionOnSubmit}) or cancelled
     * ({@link #cancelSingleSemanticTransaction}), either of which ends that transaction.
     *
     * @param referenceComponent the reference component of the semantic
     * @param patternNid         the pattern of the semantic
     * @param seedFields         sets the new version's field values; given the pattern's fields, in order
     * @return the new uncommitted semantic
     */
    public SemanticEntity<SemanticEntityVersion> createSemanticInSingleSemanticTransaction(EntityFacade referenceComponent, long patternNid,
                                                                                           Consumer<List<ObservableField.Editable<?>>> seedFields) {
        ObservableComposer singleSemanticComposer = newComposer("Single-semantic transaction");
        SemanticEntity<SemanticEntityVersion> semantic =
                composeNewSemantic(singleSemanticComposer, referenceComponent, patternNid, seedFields);
        singleSemanticTransactionComposers.put(semantic.nid(), singleSemanticComposer);
        return semantic;
    }

    private SemanticEntity<SemanticEntityVersion> composeNewSemantic(ObservableComposer composer,
                                                                     EntityFacade referenceComponent, long patternNid,
                                                                     Consumer<List<ObservableField.Editable<?>>> seedFields) {
        ObservableEntity observableReferenceComponent = ObservableEntityHandle.get(referenceComponent.nid()).expectEntity();
        ObservablePattern observablePattern = ObservableEntityHandle.get(patternNid).expectPattern();
        ObservableComposer.EntityComposer<ObservableSemanticVersion.Editable, ObservableSemantic> semanticEditor =
                composer.composeSemantic(PublicIds.newRandom(), observableReferenceComponent, observablePattern);

        seedFields.accept(semanticEditor.getEditableVersion().getEditableFields());
        semanticEditor.save(); // Save to create an uncommitted version

        return EntityHandle.get(semanticEditor.getEntity().nid()).asSemantic()
                .orElseThrow(() -> new IllegalStateException("The new uncommitted semantic is not a semantic"));
    }

    /**
     * Ends the single-semantic transaction of a semantic whose edit form was just submitted. The
     * submit saved the edited values in the window's transaction, the way every submit does, so
     * the single-semantic transaction, holding only the seeded values, is canceled: the seeded
     * version goes away, the submitted one stays. Submitted with the seeded values unchanged, the
     * submit saved nothing in the window's transaction; the single-semantic transaction is then
     * the only place the submitted semantic exists, so it is kept and commits with the window's
     * ({@link #commit()}).
     */
    public void endSingleSemanticTransactionOnSubmit(SemanticEntity<SemanticEntityVersion> semantic) {
        ObservableComposer singleSemanticComposer = singleSemanticTransactionComposers.remove(semantic.nid());
        if (singleSemanticComposer == null || !singleSemanticComposer.getOrCreateTransaction().isOpen()) {
            return;
        }
        if (savedInWindowTransaction(semantic.nid())) {
            singleSemanticComposer.cancel();
        } else {
            singleSemanticComposersToCommit.add(singleSemanticComposer);
        }
    }

    /**
     * Drops the edit form's unsaved edits to a semantic: the form was cancelled. The composer stops
     * tracking the semantic's editable version, so the next commit neither saves nor commits what
     * the form left in it, and a semantic whose form was merely opened no longer counts as an
     * uncommitted change; a version of it submitted earlier stays and publishes as before.
     */
    public void discardUnsavedEdits(SemanticEntity<SemanticEntityVersion> semantic) {
        createOrGetComposer().discardUnsavedEdits(semantic.nid());
    }

    /**
     * Cancels the single-semantic transaction of a semantic whose edit form was cancelled before
     * it was ever submitted. The form's unsaved edits are dropped from the window's transaction
     * ({@link #discardUnsavedEdits}), and cancelling the single-semantic transaction cancels the
     * semantic's only version, and so the semantic itself ({@link Entity#canceled()}): gone from
     * the window and from what the next commit publishes, while the window's edits to every other
     * entity stay. In create mode the component that creating the semantic brought into existence
     * stays, uncommitted, for the next semantic to join.
     */
    public void cancelSingleSemanticTransaction(SemanticEntity<SemanticEntityVersion> semantic) {
        discardUnsavedEdits(semantic);
        ObservableComposer singleSemanticComposer = singleSemanticTransactionComposers.remove(semantic.nid());
        if (singleSemanticComposer != null && singleSemanticComposer.getOrCreateTransaction().isOpen()) {
            singleSemanticComposer.cancel();
        }
    }

    /** Whether a version of the semantic is saved, uncommitted, in the window's open transaction. */
    private boolean savedInWindowTransaction(long semanticNid) {
        Transaction windowTransaction = createOrGetComposer().getOrCreateTransaction();
        return EntityHandle.get(semanticNid).expectEntity().versions().stream()
                .filter(EntityVersion::uncommitted)
                .anyMatch(version -> Transaction.forVersion(version).filter(windowTransaction::equals).isPresent());
    }

    /**
     * Saves an uncommitted version of an existing semantic holding a new value for one field,
     * in the composer's open transaction. Going through the composer matters for a version the
     * composer itself saved uncommitted: the composer commits its own working copy of such a version, so
     * the edit has to land in that copy.
     */
    public void saveUncommittedFieldEdit(long semanticNid, int fieldIndex, Object newValue) {
        ObservableSemantic observableSemantic = ObservableEntityHandle.get(semanticNid).expectSemantic();
        ObservableEntity observableReferenceComponent = ObservableEntityHandle.get(observableSemantic.referencedComponentNid()).expectEntity();
        ObservablePattern observablePattern = ObservableEntityHandle.get(observableSemantic.patternNid()).expectPattern();
        ObservableComposer.EntityComposer<ObservableSemanticVersion.Editable, ObservableSemantic> semanticEditor =
                createOrGetComposer().composeSemantic(observableSemantic.publicId(), observableReferenceComponent, observablePattern);

        @SuppressWarnings("unchecked")
        ObservableField.Editable<Object> editableField = (ObservableField.Editable<Object>)
                semanticEditor.getEditableVersion().getEditableFields().get(fieldIndex);
        editableField.setValue(newValue);
        semanticEditor.save(); // Save as an uncommitted version holding the edit
    }

    /**
     * Writes the window pattern's inline definition from the pattern-definition semantics the
     * window stores the definition as (see {@link PatternDefinitionSeeder#writeInlineDefinition}),
     * saved uncommitted in the composer's open transaction so it commits with them. The inline definition
     * is what a new semantic of the pattern takes its fields from — without it a pattern created
     * here showed no fields when a semantic of it was added in a KL window
     * (ikmdev/komet-desktop#192). Does nothing while the window has no component.
     */
    public void writeInlinePatternDefinition() {
        EntityFacade pattern = getComponent();
        if (pattern == null) {
            return;
        }
        ObservableComposer.EntityComposer<ObservablePatternVersion.Editable, ObservablePattern> patternComposer =
                newPatternComposer != null ? newPatternComposer : createOrGetComposer().composePattern(pattern.publicId());
        PatternDefinitionSeeder.writeInlineDefinition(patternComposer, viewProperties.calculator());
    }

    /**
     * Commits the open transactions holding the unpublished versions of the passed in semantics
     * and of the window's component — versions saved uncommitted by an earlier instance of the window, in
     * that instance's still-open transactions. A transaction persists as a record in a store that
     * holds the change transaction pattern, and is rebuilt when the store opens
     * (IKE-Network/ike-issues#1308), so versions saved before a restart are published here too; in
     * a store without the pattern, a version saved in an earlier session has no transaction to
     * commit and stays unpublished.
     *
     * @param unpublishedSemantics the semantics shown whose latest version is not published yet
     * @return how many of them, the component included, still carry an unpublished version afterwards
     */
    public int commitUnpublishedTransactions(Collection<SemanticEntity<SemanticEntityVersion>> unpublishedSemantics) {
        List<Entity<?>> unpublished = unpublishedEntities(unpublishedSemantics);
        owningTransactions(unpublished).forEach(Transaction::commit);

        // A single-semantic transaction whose semantic's form was still open got committed here:
        // the semantic is published as seeded, and its form, cancelled or submitted later, has no
        // transaction left to end.
        singleSemanticTransactionComposers.values().removeIf(singleSemanticComposer -> !singleSemanticComposer.getOrCreateTransaction().isOpen());

        return stillUnpublished(unpublished);
    }

    /**
     * Cancels the open transactions holding the unpublished versions of the passed in semantics
     * and of the window's component: the person discards what is not published yet, whether this
     * window saved it, an earlier instance of the window did, or a session before a restart did
     * (its transaction rebuilt from its record, IKE-Network/ike-issues#1308). The counterpart of
     * {@link #commitUnpublishedTransactions}; nothing is cancelled unless the person asks. A
     * transaction this window's composers hold is cancelled through its composer, which then opens
     * a fresh one, so the window goes on editing.
     *
     * @param unpublishedSemantics the semantics shown whose latest version is not published yet
     * @return how many of them, the component included, still carry an unpublished version afterwards
     */
    public int cancelUnpublishedTransactions(Collection<SemanticEntity<SemanticEntityVersion>> unpublishedSemantics) {
        List<Entity<?>> unpublished = unpublishedEntities(unpublishedSemantics);
        Set<Transaction> transactions = owningTransactions(unpublished);
        if (composer != null && transactions.remove(composer.getOrCreateTransaction())) {
            composer.cancel();
            hasUncommittedChanges.unbind();
            composer = null;
            newPatternComposer = null;
            createOrGetComposer();
        }
        singleSemanticTransactionComposers.values().removeIf(singleSemanticComposer -> {
            if (transactions.remove(singleSemanticComposer.getOrCreateTransaction())) {
                singleSemanticComposer.cancel();
                return true;
            }
            return false;
        });
        singleSemanticComposersToCommit.removeIf(composerToCommit -> !composerToCommit.getOrCreateTransaction().isOpen());
        transactions.forEach(Transaction::cancel);
        return stillUnpublished(unpublished);
    }

    /** The semantics passed in and the window's component. */
    private List<Entity<?>> unpublishedEntities(Collection<SemanticEntity<SemanticEntityVersion>> unpublishedSemantics) {
        List<Entity<?>> unpublished = new ArrayList<>(unpublishedSemantics);
        if (getComponent() != null) {
            unpublished.add(EntityHandle.get(getComponent().nid()).expectEntity());
        }
        return unpublished;
    }

    /** The open transactions owning the entities' uncommitted versions. */
    private static Set<Transaction> owningTransactions(List<Entity<?>> entities) {
        Set<Transaction> transactions = new HashSet<>();
        for (Entity<?> entity : entities) {
            for (EntityVersion version : EntityHandle.get(entity.nid()).expectEntity().versions()) {
                if (version.uncommitted()) {
                    Transaction.forStamp(version.stamp().publicId()).filter(Transaction::isOpen).ifPresent(transactions::add);
                }
            }
        }
        return transactions;
    }

    private static int stillUnpublished(List<Entity<?>> entities) {
        return (int) entities.stream()
                .filter(entity -> EntityHandle.get(entity.nid()).orNull() instanceof Entity<?> current && current.uncommitted())
                .count();
    }

    /** The composer the window pattern's field defaults are edited through; see {@link #defaultsComposer}. */
    public ObservableComposer getDefaultsComposer() {
        if (defaultsComposer == null) {
            var editCoordinate = viewProperties.nodeView().editCoordinate();
            defaultsComposer = ObservableComposer.create(
                    viewProperties.calculator(),
                    State.ACTIVE,
                    editCoordinate.getAuthorForChanges(),
                    KlTerms.FIELD_DEFAULTS_MODULE,
                    editCoordinate.getDefaultPath(),
                    "Edit pattern field defaults"
            );
        }
        return defaultsComposer;
    }

    /**
     * The window pattern's defaults semantic (see {@link PatternFieldDefaults}) — created,
     * uncommitted, in the {@link #getDefaultsComposer() defaults composer} when the pattern has
     * none yet. The window's component must be a pattern.
     */
    public SemanticEntity<SemanticEntityVersion> getOrCreateDefaultsSemantic() {
        EntityFacade pattern = getComponent();

        // Before composing: composing mints the defaults semantic's nid, after which the store knows the
        // identity whether or not a semantic has been written under it.
        boolean defaultsSemanticExists = PatternFieldDefaults.defaultsSemantic(pattern.nid()).isPresent();

        ObservablePattern observablePattern = ObservableEntityHandle.get(pattern.nid()).expectPattern();
        ObservableComposer.EntityComposer<ObservableSemanticVersion.Editable, ObservableSemantic> defaultsSemanticComposer =
                getDefaultsComposer().composeSemantic(PatternFieldDefaults.defaultsSemanticId(pattern.publicId()),
                        observablePattern, observablePattern);
        if (!defaultsSemanticExists) {
            defaultsSemanticComposer.save(); // Save to create an uncommitted version
        }
        return EntityHandle.get(defaultsSemanticComposer.getEntity().nid()).asSemantic()
                .orElseThrow(() -> new IllegalStateException("The defaults semantic is not a semantic"));
    }

    /*=*************************************************************************
     *                                                                         *
     * Properties                                                              *
     *                                                                         *
     ************************************************************************=*/

    // -- form mode
    /**
     * Whether the window's component exists yet ({@link FormMode#EDIT}) or is still being created
     * ({@link FormMode#CREATE}).
     */
    private final ObjectProperty<FormMode> mode;
    public ObjectProperty<FormMode> modeProperty() {
        return mode;
    }
    public FormMode getMode() {
        return mode.get();
    }

    // -- component
    /**
     * The component the window frames: the one it was opened on, or the one created while in
     * create mode (uncommitted until its first commit); null while none exists.
     */
    private final ReadOnlyObjectWrapper<EntityFacade> component;
    public ReadOnlyObjectProperty<EntityFacade> componentProperty() {
        return component.getReadOnlyProperty();
    }
    public EntityFacade getComponent() {
        return component.get();
    }
    /** The component the window creates in create mode, as soon as it exists (uncommitted). */
    void setComponent(EntityFacade component) {
        this.component.set(component);
    }

    // -- has uncommitted changes
    /**
     * Whether the composer's open transaction holds changes saved uncommitted since the last commit. False
     * until the first edit asks for the composer.
     */
    private final ReadOnlyBooleanWrapper hasUncommittedChanges = new ReadOnlyBooleanWrapper(false);
    public ReadOnlyBooleanProperty hasUncommittedChangesProperty() { return hasUncommittedChanges.getReadOnlyProperty(); }
    public boolean hasUncommittedChanges() { return hasUncommittedChanges.get(); }
}
