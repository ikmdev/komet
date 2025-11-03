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
package dev.ikm.komet.framework.observable;

import dev.ikm.tinkar.common.id.PublicId;
import dev.ikm.tinkar.entity.*;
import dev.ikm.tinkar.entity.transaction.Transaction;
import dev.ikm.tinkar.terms.EntityFacade;
import dev.ikm.tinkar.terms.State;
import javafx.application.Platform;
import javafx.beans.property.ReadOnlyBooleanProperty;
import javafx.beans.property.ReadOnlyBooleanWrapper;
import javafx.beans.property.ReadOnlyObjectProperty;
import javafx.beans.property.ReadOnlyObjectWrapper;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.function.Consumer;

/**
 * JavaFX-native composer for building and managing observable entities with full UI binding support.
 * <p>
 * ObservableComposer simplifies the creation and editing of Tinkar entities by providing a fluent,
 * type-safe API that integrates seamlessly with JavaFX properties and the Observable framework.
 * Unlike traditional composers that work with immutable entities, ObservableComposer leverages
 * {@link ObservableEntity}, {@link ObservableEditableVersion}, {@link ObservableEditableField},
 * and {@link ObservableStamp} to provide real-time UI binding, change notifications, and
 * transaction management.
 *
 * <h2>Key Features for JavaFX Developers</h2>
 * <ul>
 *   <li><b>Direct UI Binding:</b> All entity properties are JavaFX observable, enabling
 *       bidirectional binding with UI controls</li>
 *   <li><b>Live Change Notifications:</b> UI automatically updates when entity data changes
 *       via JavaFX property listeners</li>
 *   <li><b>Transaction Management:</b> Integrated transaction lifecycle with observable state
 *       (pending, uncommitted, committed)</li>
 *   <li><b>Type-Safe API:</b> Fluent builders with compile-time type checking prevent
 *       runtime errors</li>
 *   <li><b>Observable Stamp Integration:</b> Uses {@link ObservableStamp} for automatic
 *       UI updates when stamps transition from uncommitted to committed</li>
 *   <li><b>Editable Version Support:</b> Works with {@link ObservableEditableVersion} for
 *       cached editing with save/commit/rollback capabilities</li>
 * </ul>
 *
 * <h2>Advantages Over Traditional Composer Approaches</h2>
 * <table border="1" cellpadding="5">
 * <caption>ObservableComposer vs Traditional Composer Comparison</caption>
 * <tr>
 *   <th>Feature</th>
 *   <th>Traditional Composer</th>
 *   <th>ObservableComposer</th>
 * </tr>
 * <tr>
 *   <td><b>UI Binding</b></td>
 *   <td>❌ Manual polling/refresh required</td>
 *   <td>✅ Automatic via JavaFX properties</td>
 * </tr>
 * <tr>
 *   <td><b>Change Notifications</b></td>
 *   <td>❌ No built-in notification</td>
 *   <td>✅ JavaFX property listeners + event bus</td>
 * </tr>
 * <tr>
 *   <td><b>Transaction State Visibility</b></td>
 *   <td>⚠️ Opaque, must track manually</td>
 *   <td>✅ Observable properties expose state</td>
 * </tr>
 * <tr>
 *   <td><b>Editing Experience</b></td>
 *   <td>⚠️ Immediate writes or manual buffering</td>
 *   <td>✅ {@link ObservableEditableVersion} with save/commit</td>
 * </tr>
 * <tr>
 *   <td><b>Stamp Lifecycle</b></td>
 *   <td>❌ Immutable stamps, no state tracking</td>
 *   <td>✅ {@link ObservableStamp} tracks uncommitted → committed</td>
 * </tr>
 * <tr>
 *   <td><b>Canonical Instance Guarantee</b></td>
 *   <td>❌ Multiple instances possible</td>
 *   <td>✅ Single observable per NID via cache</td>
 * </tr>
 * <tr>
 *   <td><b>Thread Safety Model</b></td>
 *   <td>✅ Thread-safe (immutable)</td>
 *   <td>⚠️ JavaFX thread only (enforced)</td>
 * </tr>
 * </table>
 *
 * <h2>Usage Patterns</h2>
 *
 * <h3>Pattern 1: Creating New Entities with UI Binding</h3>
 * <pre>{@code
 * // Create a new concept with observable properties
 * ObservableComposer composer = ObservableComposer.builder()
 *     .author(TinkarTerm.USER)
 *     .module(TinkarTerm.PRIMORDIAL_MODULE)
 *     .path(TinkarTerm.DEVELOPMENT_PATH)
 *     .build();
 *
 * // Start building a concept
 * ObservableConceptBuilder conceptBuilder = composer.createConcept();
 *
 * // Get the editable version for UI binding
 * ObservableEditableConceptVersion editableVersion = conceptBuilder.getEditableVersion();
 *
 * // Bind UI controls directly to editable properties
 * statusComboBox.valueProperty().bindBidirectional(
 *     editableVersion.editableStateProperty()
 * );
 *
 * // Save as uncommitted (visible in UI immediately)
 * conceptBuilder.save();
 *
 * // The UI will show uncommitted state via ObservableStamp
 * ObservableStamp stamp = editableVersion.getEditStamp();
 * uncommittedLabel.visibleProperty().bind(
 *     stamp.lastVersion().timeProperty().isEqualTo(Long.MAX_VALUE)
 * );
 *
 * // Commit the transaction (stamp transitions to committed)
 * composer.commit();
 * // UI automatically updates to show committed state
 * }</pre>
 *
 * <h3>Pattern 2: Editing Existing Entities</h3>
 * <pre>{@code
 * // Get an existing observable entity
 * ObservableConcept concept = ObservableEntityHandle.getConceptOrThrow(conceptNid);
 *
 * // Create composer with editing context
 * ObservableComposer composer = ObservableComposer.builder()
 *     .author(currentUser)
 *     .module(TinkarTerm.PRIMORDIAL_MODULE)
 *     .path(TinkarTerm.DEVELOPMENT_PATH)
 *     .build();
 *
 * // Edit the concept
 * ObservableConceptEditor editor = composer.editConcept(concept);
 *
 * // Get editable version
 * ObservableEditableConceptVersion editable = editor.getEditableVersion();
 *
 * // UI controls bound to editable properties automatically show changes
 * stateField.textProperty().bind(editable.editableStateProperty().asString());
 *
 * // Check if dirty
 * saveButton.disableProperty().bind(editable.isDirtyProperty().not());
 *
 * // Save and commit
 * editor.save();
 * composer.commit();
 * }</pre>
 *
 * <h3>Pattern 3: Building Semantics with Fields</h3>
 * <pre>{@code
 * ObservableComposer composer = ObservableComposer.builder()
 *     .author(TinkarTerm.USER)
 *     .module(TinkarTerm.PRIMORDIAL_MODULE)
 *     .path(TinkarTerm.DEVELOPMENT_PATH)
 *     .build();
 *
 * // Create semantic on a concept
 * ObservableSemanticBuilder semanticBuilder = composer.createSemantic(
 *     referencedConcept,
 *     patternForDescription
 * );
 *
 * // Set field values
 * semanticBuilder
 *     .setFieldValue(0, "English description text")
 *     .setFieldValue(1, TinkarTerm.ENGLISH_LANGUAGE)
 *     .setFieldValue(2, TinkarTerm.FULLY_QUALIFIED_NAME);
 *
 * // Get editable fields for UI binding
 * ObservableList<ObservableEditableField<?>> fields =
 *     semanticBuilder.getEditableFields();
 *
 * // Bind each field to UI
 * for (int i = 0; i < fields.size(); i++) {
 *     ObservableEditableField<?> field = fields.get(i);
 *     TextField textField = textFields.get(i);
 *
 *     // Bidirectional binding
 *     if (field.getValue() instanceof String) {
 *         textField.textProperty().bindBidirectional(
 *             (javafx.beans.property.Property<String>) field.editableValueProperty()
 *         );
 *     }
 * }
 *
 * // Save and commit
 * semanticBuilder.save();
 * composer.commit();
 * }</pre>
 *
 * <h3>Pattern 4: Transaction State Monitoring</h3>
 * <pre>{@code
 * ObservableComposer composer = ObservableComposer.builder()
 *     .author(TinkarTerm.USER)
 *     .module(TinkarTerm.PRIMORDIAL_MODULE)
 *     .path(TinkarTerm.DEVELOPMENT_PATH)
 *     .build();
 *
 * // Bind UI to transaction state
 * commitButton.disableProperty().bind(
 *     composer.hasUncommittedChangesProperty().not()
 * );
 *
 * rollbackButton.disableProperty().bind(
 *     composer.hasUncommittedChangesProperty().not()
 * );
 *
 * statusLabel.textProperty().bind(
 *     composer.transactionStateProperty().asString()
 * );
 *
 * // Create and modify entities...
 * ObservableConceptBuilder builder = composer.createConcept();
 * builder.save(); // hasUncommittedChangesProperty becomes true
 *
 * // User clicks commit button (automatically enabled)
 * composer.commit(); // hasUncommittedChangesProperty becomes false
 * }</pre>
 *
 * <h2>Thread Safety</h2>
 * <p>
 * <b>⚠️ IMPORTANT:</b> ObservableComposer must be used exclusively on the JavaFX application thread.
 * All methods enforce this requirement and will throw {@link RuntimeException} if called from
 * other threads. This is consistent with the Observable framework's threading model.
 *
 * <h2>Transaction Lifecycle</h2>
 * <p>
 * ObservableComposer manages transactions automatically:
 * <ol>
 *   <li><b>Creation:</b> Transaction created lazily on first entity modification</li>
 *   <li><b>Uncommitted:</b> Changes saved to database but transaction not committed
 *       (stamps have time = Long.MAX_VALUE)</li>
 *   <li><b>Committed:</b> Transaction committed, stamps updated to actual time</li>
 *   <li><b>Rollback:</b> Uncommitted changes discarded, transaction cancelled</li>
 * </ol>
 * <p>
 * Transaction state is exposed via observable properties, enabling UI components to react
 * to state changes automatically.
 *
 * @see ObservableEntity
 * @see ObservableEditableVersion
 * @see ObservableEditableField
 * @see ObservableStamp
 * @see ObservableEntityHandle
 * @see Transaction
 */
public final class ObservableComposer {

    private final int authorNid;
    private final int moduleNid;
    private final int pathNid;
    private final State defaultState;
    private final String transactionComment;

    private Transaction transaction;
    private final List<ObservableEditableVersion<?, ?>> trackedEditables = new ArrayList<>();

    private final ReadOnlyObjectWrapper<TransactionState> transactionStateProperty =
            new ReadOnlyObjectWrapper<>(this, "transactionState", TransactionState.NONE);

    private final ReadOnlyBooleanWrapper hasUncommittedChangesProperty =
            new ReadOnlyBooleanWrapper(this, "hasUncommittedChanges", false);

    /**
     * Transaction state enumeration for UI binding.
     */
    public enum TransactionState {
        /** No active transaction */
        NONE,
        /** Transaction created, changes saved but not committed */
        UNCOMMITTED,
        /** Transaction committed successfully */
        COMMITTED,
        /** Transaction rolled back */
        ROLLED_BACK
    }

    /**
     * Private constructor. Use static factory methods {@link #create(State, EntityFacade, EntityFacade, EntityFacade)}
     * or {@link #create(State, EntityFacade, EntityFacade, EntityFacade, String)} to create instances.
     *
     * @param authorNid the author NID
     * @param moduleNid the module NID
     * @param pathNid the path NID
     * @param defaultState the default state for new entities
     * @param transactionComment optional comment describing the transaction
     */
    private ObservableComposer(int authorNid, int moduleNid, int pathNid, State defaultState, String transactionComment) {
        requireJavaFXThread();
        this.authorNid = authorNid;
        this.moduleNid = moduleNid;
        this.pathNid = pathNid;
        this.defaultState = defaultState;
        this.transactionComment = transactionComment;
    }

    /**
     * Creates a new ObservableComposer with the specified STAMP coordinates.
     * <p>
     * This static factory method provides a convenient way to create a composer without
     * needing to extract NIDs from EntityFacades manually.
     *
     * @param state the default state for entities (typically State.ACTIVE)
     * @param author the author entity (use EntityHandle for canonical reference)
     * @param module the module entity (use EntityHandle for canonical reference)
     * @param path the path entity (use EntityHandle for canonical reference)
     * @return a new ObservableComposer instance
     * @throws NullPointerException if any parameter is null
     */
    public static ObservableComposer create(State state, EntityFacade author, EntityFacade module, EntityFacade path) {
        return create(state, author, module, path, "");
    }

    /**
     * Creates a new ObservableComposer with the specified STAMP coordinates and transaction comment.
     * <p>
     * The transaction comment is used to identify the transaction for debugging and logging purposes.
     *
     * @param state the default state for entities (typically State.ACTIVE)
     * @param author the author entity (use EntityHandle for canonical reference)
     * @param module the module entity (use EntityHandle for canonical reference)
     * @param path the path entity (use EntityHandle for canonical reference)
     * @param transactionComment optional descriptive comment for the transaction
     * @return a new ObservableComposer instance
     * @throws NullPointerException if any parameter except transactionComment is null
     */
    public static ObservableComposer create(State state, EntityFacade author, EntityFacade module, EntityFacade path, String transactionComment) {
        return new ObservableComposer(
                author.nid(),
                module.nid(),
                path.nid(),
                state,
                transactionComment == null ? "" : transactionComment
        );
    }

    /**
     * Creates a new builder for ObservableComposer.
     * <p>
     * Note: Consider using the static factory methods {@link #create(State, EntityFacade, EntityFacade, EntityFacade)}
     * for a more concise API.
     */
    public static Builder builder() {
        return new Builder();
    }

    /**
     * Returns the observable property for transaction state.
     * UI components can bind to this property to react to transaction lifecycle changes.
     */
    public ReadOnlyObjectProperty<TransactionState> transactionStateProperty() {
        return transactionStateProperty.getReadOnlyProperty();
    }

    /**
     * Returns the current transaction state.
     */
    public TransactionState getTransactionState() {
        return transactionStateProperty.get();
    }

    /**
     * Returns the observable property indicating if there are uncommitted changes.
     */
    public ReadOnlyBooleanProperty hasUncommittedChangesProperty() {
        return hasUncommittedChangesProperty.getReadOnlyProperty();
    }

    /**
     * Returns true if there are uncommitted changes.
     */
    public boolean hasUncommittedChanges() {
        return hasUncommittedChangesProperty.get();
    }

    /**
     * Creates a builder for a new concept entity.
     *
     * @return a new concept builder
     */
    public ObservableConceptBuilder createConcept() {
        requireJavaFXThread();
        ensureTransaction();
        return new ObservableConceptBuilder(this);
    }

    /**
     * Creates a builder for a new semantic entity.
     *
     * @param referencedComponent the component this semantic references
     * @param pattern the pattern defining the semantic's structure
     * @return a new semantic builder
     */
    public ObservableSemanticBuilder createSemantic(EntityFacade referencedComponent, EntityFacade pattern) {
        requireJavaFXThread();
        ensureTransaction();
        return new ObservableSemanticBuilder(this, referencedComponent.nid(), pattern.nid());
    }

    /**
     * Creates a builder for a new pattern entity.
     *
     * @return a new pattern builder
     */
    public ObservablePatternBuilder createPattern() {
        requireJavaFXThread();
        ensureTransaction();
        return new ObservablePatternBuilder(this);
    }

    /**
     * Creates an editor for an existing concept.
     *
     * @param concept the concept to edit
     * @return a concept editor
     */
    public ObservableConceptEditor editConcept(ObservableConcept concept) {
        requireJavaFXThread();
        ensureTransaction();
        return new ObservableConceptEditor(this, concept);
    }

    /**
     * Creates an editor for an existing semantic.
     *
     * @param semantic the semantic to edit
     * @return a semantic editor
     */
    public ObservableSemanticEditor editSemantic(ObservableSemantic semantic) {
        requireJavaFXThread();
        ensureTransaction();
        return new ObservableSemanticEditor(this, semantic);
    }

    /**
     * Creates an editor for an existing pattern.
     *
     * @param pattern the pattern to edit
     * @return a pattern editor
     */
    public ObservablePatternEditor editPattern(ObservablePattern pattern) {
        requireJavaFXThread();
        ensureTransaction();
        return new ObservablePatternEditor(this, pattern);
    }

    /**
     * Commits the current transaction, finalizing all changes.
     * After commit, all {@link ObservableStamp} instances transition from uncommitted to committed state,
     * and UI components bound to stamp properties will automatically update.
     */
    public void commit() {
        requireJavaFXThread();
        if (transaction == null) {
            return;
        }

        // Commit all tracked editable versions first
        for (ObservableEditableVersion<?, ?> editable : trackedEditables) {
            if (editable.isDirty()) {
                editable.save();
            }
            editable.commit();
        }

        // Commit the transaction
        transaction.commit();

        // Update state properties
        transactionStateProperty.set(TransactionState.COMMITTED);
        hasUncommittedChangesProperty.set(false);

        // Clear transaction
        transaction = null;
        trackedEditables.clear();
    }

    /**
     * Rolls back the current transaction, discarding all uncommitted changes.
     * All {@link ObservableEditableVersion} instances are reset to their original state.
     */
    public void rollback() {
        requireJavaFXThread();
        if (transaction == null) {
            return;
        }

        // Reset all tracked editable versions
        for (ObservableEditableVersion<?, ?> editable : trackedEditables) {
            editable.reset();
        }

        // Cancel the transaction
        transaction.cancel();

        // Update state properties
        transactionStateProperty.set(TransactionState.ROLLED_BACK);
        hasUncommittedChangesProperty.set(false);

        // Clear transaction
        transaction = null;
        trackedEditables.clear();
    }

    /**
     * Cancels the current transaction without rolling back changes.
     * This is useful when you want to discard the transaction but keep the changes in memory.
     */
    public void cancel() {
        requireJavaFXThread();
        if (transaction != null) {
            transaction.cancel();
            transaction = null;
            transactionStateProperty.set(TransactionState.NONE);
            hasUncommittedChangesProperty.set(false);
            trackedEditables.clear();
        }
    }

    /**
     * Creates or retrieves an observable stamp for the current author/module/path/state.
     */
    ObservableStamp createStamp(State state, Entity<?> entity) {
        StampEntity stampEntity = transaction.getStampForEntities(
                state,
                authorNid,
                moduleNid,
                pathNid,
                entity
        );
        return ObservableEntityHandle.getStampOrThrow(stampEntity.nid());
    }

    /**
     * Tracks an editable version for automatic commit/rollback management.
     */
    void trackEditable(ObservableEditableVersion<?, ?> editable) {
        trackedEditables.add(editable);
        hasUncommittedChangesProperty.set(true);
    }

    Transaction getTransaction() {
        return transaction;
    }

    /**
     * Gets or creates the underlying Transaction for advanced use cases.
     * <p>
     * This method is intended for scenarios where direct Transaction access is needed,
     * such as creating stamps programmatically without using the Observable entity builders.
     * <p>
     * <b>Note:</b> Direct transaction manipulation bypasses the Observable framework's
     * tracking and binding capabilities. Use the standard create/edit methods when possible.
     *
     * @return the Transaction instance, creating it if necessary
     */
    public Transaction getOrCreateTransaction() {
        ensureTransaction();
        return transaction;
    }

    int getAuthorNid() {
        return authorNid;
    }

    int getModuleNid() {
        return moduleNid;
    }

    int getPathNid() {
        return pathNid;
    }

    State getDefaultState() {
        return defaultState;
    }

    private void ensureTransaction() {
        if (transaction == null) {
            transaction = transactionComment.isEmpty()
                ? Transaction.make()
                : Transaction.make(transactionComment);
            transactionStateProperty.set(TransactionState.UNCOMMITTED);
        }
    }

    private static void requireJavaFXThread() {
        if (!Platform.isFxApplicationThread()) {
            throw new RuntimeException("ObservableComposer must be used on JavaFX application thread");
        }
    }

    /**
     * Builder for ObservableComposer instances.
     */
    public static final class Builder {
        private EntityFacade author;
        private EntityFacade module;
        private EntityFacade path;
        private State defaultState = State.ACTIVE;
        private String transactionComment = "";

        private Builder() {}

        /**
         * Sets the author for all entities created by this composer.
         */
        public Builder author(EntityFacade author) {
            this.author = Objects.requireNonNull(author, "author cannot be null");
            return this;
        }

        /**
         * Sets the module for all entities created by this composer.
         */
        public Builder module(EntityFacade module) {
            this.module = Objects.requireNonNull(module, "module cannot be null");
            return this;
        }

        /**
         * Sets the path for all entities created by this composer.
         */
        public Builder path(EntityFacade path) {
            this.path = Objects.requireNonNull(path, "path cannot be null");
            return this;
        }

        /**
         * Sets the default state for new entities (default: ACTIVE).
         */
        public Builder defaultState(State defaultState) {
            this.defaultState = Objects.requireNonNull(defaultState, "defaultState cannot be null");
            return this;
        }

        /**
         * Sets an optional comment describing the transaction.
         * <p>
         * This comment is used when creating the underlying {@link Transaction} and can be helpful
         * for debugging and logging purposes.
         *
         * @param transactionComment descriptive comment for the transaction (may be empty or null)
         * @return this builder for method chaining
         */
        public Builder transactionComment(String transactionComment) {
            this.transactionComment = transactionComment == null ? "" : transactionComment;
            return this;
        }

        /**
         * Builds the ObservableComposer instance.
         */
        public ObservableComposer build() {
            Objects.requireNonNull(author, "author must be set");
            Objects.requireNonNull(module, "module must be set");
            Objects.requireNonNull(path, "path must be set");

            return new ObservableComposer(
                    author.nid(),
                    module.nid(),
                    path.nid(),
                    defaultState,
                    transactionComment
            );
        }
    }

    /**
     * Base class for entity builders.
     */
    public abstract static class EntityBuilder<T extends ObservableEntity<?>> {
        protected final ObservableComposer composer;
        protected State state;

        protected EntityBuilder(ObservableComposer composer) {
            this.composer = composer;
            this.state = composer.getDefaultState();
        }

        /**
         * Sets the state for this entity.
         */
        @SuppressWarnings("unchecked")
        public <B extends EntityBuilder<T>> B state(State state) {
            this.state = state;
            return (B) this;
        }

        /**
         * Builds the observable entity.
         */
        public abstract T build();

        /**
         * Saves the entity as uncommitted.
         */
        public abstract void save();
    }

    /**
     * Builder for creating new concept entities.
     */
    public static final class ObservableConceptBuilder extends EntityBuilder<ObservableConcept> {
        private PublicId publicId;
        private ObservableEditableConceptVersion editableVersion;

        private ObservableConceptBuilder(ObservableComposer composer) {
            super(composer);
        }

        /**
         * Sets the public ID for this concept (optional - will be generated if not provided).
         */
        public ObservableConceptBuilder publicId(PublicId publicId) {
            this.publicId = publicId;
            return this;
        }

        /**
         * Returns the editable version for UI binding.
         */
        public ObservableEditableConceptVersion getEditableVersion() {
            if (editableVersion == null) {
                throw new IllegalStateException("Must call save() or build() first");
            }
            return editableVersion;
        }

        @Override
        public ObservableConcept build() {
            // Implementation would create the concept entity
            throw new UnsupportedOperationException("Implementation pending");
        }

        @Override
        public void save() {
            // Implementation would save as uncommitted
            throw new UnsupportedOperationException("Implementation pending");
        }
    }

    /**
     * Builder for creating new semantic entities.
     */
    public static final class ObservableSemanticBuilder extends EntityBuilder<ObservableSemantic> {
        private final int referencedComponentNid;
        private final int patternNid;
        private PublicId publicId;
        private final List<Object> fieldValues = new ArrayList<>();
        private ObservableEditableSemanticVersion editableVersion;

        private ObservableSemanticBuilder(ObservableComposer composer, int referencedComponentNid, int patternNid) {
            super(composer);
            this.referencedComponentNid = referencedComponentNid;
            this.patternNid = patternNid;
        }

        /**
         * Sets the public ID for this semantic (optional).
         */
        public ObservableSemanticBuilder publicId(PublicId publicId) {
            this.publicId = publicId;
            return this;
        }

        /**
         * Sets a field value by index.
         */
        public ObservableSemanticBuilder setFieldValue(int index, Object value) {
            while (fieldValues.size() <= index) {
                fieldValues.add(null);
            }
            fieldValues.set(index, value);
            return this;
        }

        /**
         * Returns the editable fields for UI binding.
         */
        public ObservableList<ObservableEditableField<?>> getEditableFields() {
            if (editableVersion == null) {
                throw new IllegalStateException("Must call save() or build() first");
            }
            return editableVersion.getEditableFields();
        }

        /**
         * Returns the editable version for UI binding.
         */
        public ObservableEditableSemanticVersion getEditableVersion() {
            if (editableVersion == null) {
                throw new IllegalStateException("Must call save() or build() first");
            }
            return editableVersion;
        }

        @Override
        public ObservableSemantic build() {
            throw new UnsupportedOperationException("Implementation pending");
        }

        @Override
        public void save() {
            throw new UnsupportedOperationException("Implementation pending");
        }
    }

    /**
     * Builder for creating new pattern entities.
     */
    public static final class ObservablePatternBuilder extends EntityBuilder<ObservablePattern> {
        private PublicId publicId;
        private ObservableEditablePatternVersion editableVersion;

        private ObservablePatternBuilder(ObservableComposer composer) {
            super(composer);
        }

        /**
         * Sets the public ID for this pattern (optional).
         */
        public ObservablePatternBuilder publicId(PublicId publicId) {
            this.publicId = publicId;
            return this;
        }

        /**
         * Returns the editable version for UI binding.
         */
        public ObservableEditablePatternVersion getEditableVersion() {
            if (editableVersion == null) {
                throw new IllegalStateException("Must call save() or build() first");
            }
            return editableVersion;
        }

        @Override
        public ObservablePattern build() {
            throw new UnsupportedOperationException("Implementation pending");
        }

        @Override
        public void save() {
            throw new UnsupportedOperationException("Implementation pending");
        }
    }

    /**
     * Editor for existing concept entities.
     */
    public static final class ObservableConceptEditor {
        private final ObservableComposer composer;
        private final ObservableConcept concept;
        private ObservableEditableConceptVersion editableVersion;

        private ObservableConceptEditor(ObservableComposer composer, ObservableConcept concept) {
            this.composer = composer;
            this.concept = concept;
        }

        /**
         * Returns the editable version for UI binding.
         */
        public ObservableEditableConceptVersion getEditableVersion() {
            if (editableVersion == null) {
                // Create editable version with composer's stamp
                ObservableStamp stamp = composer.createStamp(composer.getDefaultState(), concept.entity());
                ObservableConceptVersion latestVersion = concept.versions().getLast();
                editableVersion = latestVersion.getEditableVersion(stamp);
                composer.trackEditable(editableVersion);
            }
            return editableVersion;
        }

        /**
         * Saves changes as uncommitted.
         */
        public void save() {
            if (editableVersion != null && editableVersion.isDirty()) {
                editableVersion.save();
            }
        }

        /**
         * Returns the observable concept being edited.
         */
        public ObservableConcept getConcept() {
            return concept;
        }
    }

    /**
     * Editor for existing semantic entities.
     */
    public static final class ObservableSemanticEditor {
        private final ObservableComposer composer;
        private final ObservableSemantic semantic;
        private ObservableEditableSemanticVersion editableVersion;

        private ObservableSemanticEditor(ObservableComposer composer, ObservableSemantic semantic) {
            this.composer = composer;
            this.semantic = semantic;
        }

        /**
         * Returns the editable version for UI binding.
         */
        public ObservableEditableSemanticVersion getEditableVersion() {
            if (editableVersion == null) {
                ObservableStamp stamp = composer.createStamp(composer.getDefaultState(), semantic.entity());
                ObservableSemanticVersion latestVersion = semantic.versions().getLast();
                editableVersion = latestVersion.getEditableVersion(stamp);
                composer.trackEditable(editableVersion);
            }
            return editableVersion;
        }

        /**
         * Returns the editable fields for UI binding.
         */
        public ObservableList<ObservableEditableField<?>> getEditableFields() {
            return getEditableVersion().getEditableFields();
        }

        /**
         * Saves changes as uncommitted.
         */
        public void save() {
            if (editableVersion != null && editableVersion.isDirty()) {
                editableVersion.save();
            }
        }

        /**
         * Returns the observable semantic being edited.
         */
        public ObservableSemantic getSemantic() {
            return semantic;
        }
    }

    /**
     * Editor for existing pattern entities.
     */
    public static final class ObservablePatternEditor {
        private final ObservableComposer composer;
        private final ObservablePattern pattern;
        private ObservableEditablePatternVersion editableVersion;

        private ObservablePatternEditor(ObservableComposer composer, ObservablePattern pattern) {
            this.composer = composer;
            this.pattern = pattern;
        }

        /**
         * Returns the editable version for UI binding.
         */
        public ObservableEditablePatternVersion getEditableVersion() {
            if (editableVersion == null) {
                ObservableStamp stamp = composer.createStamp(composer.getDefaultState(), pattern.entity());
                ObservablePatternVersion latestVersion = pattern.versions().getLast();
                editableVersion = latestVersion.getEditableVersion(stamp);
                composer.trackEditable(editableVersion);
            }
            return editableVersion;
        }

        /**
         * Saves changes as uncommitted.
         */
        public void save() {
            if (editableVersion != null && editableVersion.isDirty()) {
                editableVersion.save();
            }
        }

        /**
         * Returns the observable pattern being edited.
         */
        public ObservablePattern getPattern() {
            return pattern;
        }
    }
}
