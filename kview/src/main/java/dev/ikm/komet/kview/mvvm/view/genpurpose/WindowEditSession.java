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
import java.util.HashSet;
import java.util.List;
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
     * per edit rather than keeping it.
     */
    public ObservableComposer createOrGetComposer() {
        if (composer == null) {
            var editCoordinate = viewProperties.nodeView().editCoordinate();
            composer = ObservableComposer.create(
                    viewProperties.calculator(),
                    State.ACTIVE,
                    editCoordinate.getAuthorForChanges(),
                    editCoordinate.getDefaultModule(),
                    editCoordinate.getDefaultPath(),
                    "Edit Semantic Details"
            );
            hasUncommittedChanges.bind(composer.hasUncommittedChangesProperty());
        }
        return composer;
    }

    /**
     * Commits the composer's open transaction, finalizing everything uncommitted since the last
     * commit: submitted semantic versions and, in create mode, the lazily created component
     * itself. The next edit is saved uncommitted in a fresh transaction.
     */
    public void commit() {
        createOrGetComposer().commit();
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
     * field values seeded by {@code seedFields} before the version is saved.
     *
     * @param referenceComponent the reference component of the semantic
     * @param patternNid         the pattern of the semantic
     * @param seedFields         sets the new version's field values; given the pattern's fields, in order
     * @return the new uncommitted semantic
     */
    public SemanticEntity<SemanticEntityVersion> createUncommittedSemantic(EntityFacade referenceComponent, long patternNid,
                                                                           Consumer<List<ObservableField.Editable<?>>> seedFields) {
        ObservableEntity observableReferenceComponent = ObservableEntityHandle.get(referenceComponent.nid()).expectEntity();
        ObservablePattern observablePattern = ObservableEntityHandle.get(patternNid).expectPattern();
        ObservableComposer.EntityComposer<ObservableSemanticVersion.Editable, ObservableSemantic> semanticEditor =
                createOrGetComposer().composeSemantic(PublicIds.newRandom(), observableReferenceComponent, observablePattern);

        seedFields.accept(semanticEditor.getEditableVersion().getEditableFields());
        semanticEditor.save(); // Save to create an uncommitted version

        return EntityHandle.get(semanticEditor.getEntity().nid()).asSemantic()
                .orElseThrow(() -> new IllegalStateException("The new uncommitted semantic is not a semantic"));
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
     * that instance's still-open transactions. Transactions live in memory only, so a version
     * saved in an earlier session has none to commit; those versions stay unpublished.
     *
     * @param unpublishedSemantics the semantics shown whose latest version is not published yet
     * @return how many of them, the component included, still carry an unpublished version afterwards
     */
    public int commitUnpublishedTransactions(Collection<SemanticEntity<SemanticEntityVersion>> unpublishedSemantics) {
        List<Entity<?>> unpublished = new ArrayList<>(unpublishedSemantics);
        if (getComponent() != null) {
            unpublished.add(EntityHandle.get(getComponent().nid()).expectEntity());
        }

        Set<Transaction> transactions = new HashSet<>();
        for (Entity<?> entity : unpublished) {
            for (EntityVersion version : EntityHandle.get(entity.nid()).expectEntity().versions()) {
                if (version.uncommitted()) {
                    Transaction.forStamp(version.stamp().publicId()).ifPresent(transactions::add);
                }
            }
        }
        transactions.forEach(Transaction::commit);

        return (int) unpublished.stream()
                .filter(entity -> EntityHandle.get(entity.nid()).expectEntity().uncommitted())
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
