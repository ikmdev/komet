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

import dev.ikm.tinkar.common.id.Nid;
import dev.ikm.tinkar.common.id.PublicId;
import dev.ikm.tinkar.common.id.PublicIds;
import dev.ikm.tinkar.component.SemanticVersion;
import dev.ikm.tinkar.coordinate.stamp.calculator.Latest;
import dev.ikm.tinkar.coordinate.stamp.calculator.StampCalculator;
import dev.ikm.tinkar.entity.*;
import dev.ikm.tinkar.entity.transaction.Transaction;
import dev.ikm.tinkar.terms.*;
import javafx.application.Platform;
import javafx.beans.property.ReadOnlyBooleanProperty;
import javafx.beans.property.ReadOnlyBooleanWrapper;
import javafx.beans.property.ReadOnlyObjectProperty;
import javafx.beans.property.ReadOnlyObjectWrapper;
import javafx.collections.ObservableList;
import org.eclipse.collections.api.factory.Lists;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

/**
 * JavaFX-native composer for building and managing observable entities with full UI binding support.
 * <p>
 * ObservableComposer simplifies the creation and editing of Tinkar entities by providing a fluent,
 * type-safe API that integrates seamlessly with JavaFX properties and the Observable framework.
 * Unlike traditional composers that work with immutable entities, ObservableComposer leverages
 * {@link ObservableEntity}, {@link ObservableVersion.Editable}, {@link ObservableField.Editable},
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
 *   <li><b>Editable Version Support:</b> Works with {@link ObservableVersion.Editable} for
 *       cached editing with save/commit/rollback capabilities</li>
 * </ul>
 *
 * <h2>Advantages Over Traditional Composer Approaches</h2>
 * <table style="border: 1px solid black; border-collapse: collapse;">
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
 *   <td>✅ {@link ObservableVersion.Editable} with save/commit</td>
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
 * <h2>Simplified Unified API</h2>
 * <p>
 * ObservableComposer provides a simplified API through the {@link EntityComposer} interface that
 * eliminates the need to know whether you're creating or editing an entity. Simply provide a PublicId,
 * and the framework handles the rest:
 * <pre>{@code
 * ObservableComposer composer = ObservableComposer.builder()
 *     .author(TinkarTerm.USER)
 *     .module(TinkarTerm.PRIMORDIAL_MODULE)
 *     .path(TinkarTerm.DEVELOPMENT_PATH)
 *     .build();
 *
 * // Simple unified API - just provide a PublicId
 * PublicId myConceptId = PublicIds.of(UUID.fromString("..."));
 * EntityComposer<ObservableConceptVersion.Editable, ObservableConcept> conceptComposer =
 *     composer.composeConcept(myConceptId);
 *
 * // The implementation (builder vs editor) is hidden - you just work with the entity
 * ObservableConceptVersion.Editable editable = conceptComposer.getEditableVersion();
 * ObservableConcept concept = conceptComposer.getEntity();
 * editable.editableStateProperty().set(State.ACTIVE);
 *
 * // Save and commit
 * conceptComposer.save();
 * composer.commit();
 * }</pre>
 * <p>
 * The same pattern works for all entity types:
 * <ul>
 *   <li>{@link #composeConcept(PublicId)} - Compose concepts</li>
 *   <li>{@link #composeSemantic(PublicId, EntityFacade, PatternFacade)} - Compose semantics</li>
 *   <li>{@link #composePattern(PublicId)} - Compose patterns</li>
 * </ul>
 * <p>
 * <b>Key Benefits:</b>
 * <ul>
 *   <li>No need to know if entity exists - framework handles it automatically</li>
 *   <li>No builder vs editor classes to learn - just use {@link EntityComposer}</li>
 *   <li>Consistent API across all entity types</li>
 *   <li>Type-safe with full JavaFX property binding support</li>
 * </ul>
 *
 * <h2>Usage Patterns</h2>
 *
 * <p><b>Pattern 1: Creating New Entities with UI Binding</b>
 * <pre>{@code
 * // Create a new concept with observable properties
 * ObservableComposer composer = ObservableComposer.builder()
 *     .author(TinkarTerm.USER)
 *     .module(TinkarTerm.PRIMORDIAL_MODULE)
 *     .path(TinkarTerm.DEVELOPMENT_PATH)
 *     .build();
 *
 * // Start building a concept
 * ObservableConceptBuilder conceptBuilder = composer.createConceptBuilder();
 *
 * // Get the editable version for UI binding
 * ObservableConceptVersion.Editable editableVersion = conceptBuilder.getEditableVersion();
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
 * <p><b>Pattern 2: Editing Existing Entities</b>
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
 * ObservableConceptVersion.Editable editable = editor.getEditableVersion();
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
 * <p><b>Pattern 3: Building Semantics with Fields</b>
 * <pre>{@code
 * ObservableComposer composer = ObservableComposer.builder()
 *     .author(TinkarTerm.USER)
 *     .module(TinkarTerm.PRIMORDIAL_MODULE)
 *     .path(TinkarTerm.DEVELOPMENT_PATH)
 *     .build();
 *
 * // Create semantic on a concept
 * ObservableSemanticBuilder semanticBuilder = composer.createSemanticBuilder(
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
 * ObservableList<ObservableField.Editable<?>> fields =
 *     semanticBuilder.getEditableFields();
 *
 * // Bind each field to UI
 * for (int i = 0; i < fields.size(); i++) {
 *     ObservableField.Editable<?> field = fields.get(i);
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
 * <p><b>Pattern 4: Transaction State Monitoring</b>
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
 * ObservableConceptBuilder builder = composer.createConceptBuilder();
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
 * @see ObservableVersion.Editable
 * @see ObservableField.Editable
 * @see ObservableStamp
 * @see ObservableEntityHandle
 * @see Transaction
 */
public final class ObservableComposer {

    private static final Logger LOG = LoggerFactory.getLogger(ObservableComposer.class);

    private final StampCalculator stampCalculator;
    private final int authorNid;
    private final int moduleNid;
    private final int pathNid;
    private final State defaultState;
    private final String transactionComment;

    private Transaction transaction;
    private final List<ObservableVersion.Editable<?, ?, ?>> trackedEditables = new ArrayList<>();

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
     * Initializes a new instance of the ObservableComposer with specific STAMP coordinates and transaction comment.
     *
     * @param stampCalculator the StampCalculator instance used for calculations; must not be null
     * @param authorNid the NID of the author; identifies the author for the transaction
     * @param moduleNid the NID of the module; identifies the module for the transaction
     * @param pathNid the NID of the path; identifies the path for the transaction
     * @param defaultState the default state for entities (typically State.ACTIVE); must not be null
     * @param transactionComment an optional descriptive comment for the transaction
     */
    private ObservableComposer(StampCalculator stampCalculator, int authorNid, int moduleNid, int pathNid, State defaultState, String transactionComment) {
        requireJavaFXThread();
        this.stampCalculator = Objects.requireNonNull(stampCalculator, "stampCalculator cannot be null");
        this.authorNid = Nid.validate(authorNid);
        this.moduleNid = Nid.validate(moduleNid);
        this.pathNid = Nid.validate(pathNid);
        this.defaultState = Objects.requireNonNull(defaultState, "defaultState cannot be null");
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
    public static ObservableComposer create(StampCalculator stampCalculator, State state, EntityFacade author, EntityFacade module, EntityFacade path) {
        return create(stampCalculator, state, author, module, path, "");
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
    public static ObservableComposer create(StampCalculator stampCalculator, State state, EntityFacade author, EntityFacade module, EntityFacade path, String transactionComment) {
        return new ObservableComposer(
                stampCalculator,
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
     * Note: Consider using the static factory methods {@link #create(StampCalculator, State, EntityFacade, EntityFacade, EntityFacade)}
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
     * Creates a composer for a new concept entity.
     * <p>
     * For a unified API that automatically handles existing entities, use {@link #composeConcept(PublicId)}.
     *
     * @return a new concept composer
     */
    private ObservableConceptBuilder createConceptBuilder() {
        requireJavaFXThread();
        ensureTransaction();
        return new ObservableConceptBuilder(this);
    }

    /**
     * Creates a composer for a new semantic entity.
     * <p>
     * For a unified API that automatically handles existing entities, use {@link #composeSemantic(PublicId, EntityFacade, PatternFacade)}.
     *
     * @param referencedComponent the component this semantic references
     * @param pattern the pattern defining the semantic's structure
     * @return a new semantic composer
     */
    private ObservableSemanticBuilder createSemanticBuilder(EntityFacade referencedComponent, PatternFacade pattern) {
        requireJavaFXThread();
        ensureTransaction();
        return switch (referencedComponent) {
            case null -> throw new IllegalArgumentException("referencedComponent cannot be null");
            case ObservableEntity observableEntity -> new ObservableSemanticBuilder(this, observableEntity, pattern.nid());
            case Entity entity -> new ObservableSemanticBuilder(this, ObservableEntity.packagePrivateGet(entity), pattern.nid());
            case EntityFacade entityFacade -> new ObservableSemanticBuilder(this, ObservableEntity.packagePrivateGet(entityFacade.nid()), pattern.nid());
        };
    }

    /**
     * Creates a composer for a new pattern entity.
     * <p>
     * For a unified API that automatically handles existing entities, use {@link #composePattern(PublicId)}.
     *
     * @return a new pattern composer
     */
    private ObservablePatternBuilder createPatternBuilder() {
        requireJavaFXThread();
        ensureTransaction();
        return new ObservablePatternBuilder(this);
    }

    /**
     * Creates a unified composer for a concept entity.
     * <p>
     * This is the primary API for working with concepts in ObservableComposer. It automatically
     * determines whether to create a new entity or edit an existing one based on the PublicId.
     * <p>
     * <b>Usage:</b>
     * <pre>{@code
     * // If entity with publicId exists, it will be edited; otherwise a new one is created
     * EntityComposer<ObservableConceptVersion.Editable, ObservableConcept> composer =
     *     observableComposer.composeConcept(myPublicId);
     *
     * // Work with the entity without knowing if it's new or existing
     * ObservableConceptVersion.Editable editable = composer.getEditableVersion();
     * editable.editableStateProperty().set(State.ACTIVE);
     * composer.save();
     * }</pre>
     *
     * @param publicId the PublicId of the concept; if an entity with this ID exists, it will be edited,
     *                 otherwise a new entity with this ID will be created
     * @return a unified concept composer
     * @throws IllegalArgumentException if the PublicId exists but does not correspond to a concept
     */
    public EntityComposer<ObservableConceptVersion.Editable, ObservableConcept> composeConcept(PublicId publicId) {
        requireJavaFXThread();
        ensureTransaction();
        Objects.requireNonNull(publicId, "publicId cannot be null");

        ObservableEntityHandle entityHandle = ObservableEntityHandle.get(publicId);
        if (entityHandle.isAbsent()) {
            return createConceptBuilder().publicId(publicId);
        }
        ObservableConcept concept = entityHandle.asConcept()
                .orElseThrow(() -> new IllegalArgumentException("PublicId does not correspond to a concept"));
        return editConcept(concept);
    }

    /**
     * Internal method to create an editor for an existing concept.
     */
    private ObservableConceptEditor editConcept(ObservableConcept concept) {
        return new ObservableConceptEditor(this, concept);
    }

    /**
     * Creates a unified composer for a semantic entity by PublicId.
     * <p>
     * This is the primary API for working with semantics in ObservableComposer. It automatically
     * determines whether to create a new entity or edit an existing one based on the PublicId.
     *
     * @param publicId the PublicId of the semantic; if an entity with this ID exists, it will be edited,
     *                 otherwise a new entity with this ID will be created
     * @param referencedComponent the component this semantic references (required if creating new)
     * @param pattern the pattern defining the semantic's structure (required if creating new)
     * @return a unified semantic composer
     * @throws IllegalArgumentException if the PublicId exists but does not correspond to a semantic
     */
    public EntityComposer<ObservableSemanticVersion.Editable, ObservableSemantic> composeSemantic(
            PublicId publicId, EntityFacade referencedComponent, PatternFacade pattern) {
        requireJavaFXThread();
        ensureTransaction();
        Objects.requireNonNull(publicId, "publicId cannot be null");

        ObservableEntityHandle entityHandle = ObservableEntityHandle.get(publicId);
        if (entityHandle.isAbsent()) {
            return createSemanticBuilder(referencedComponent, pattern).publicId(publicId);
        }
        ObservableSemantic semantic = entityHandle.asSemantic()
                .orElseThrow(() -> new IllegalArgumentException("PublicId does not correspond to a semantic"));
        return editSemantic(semantic);
    }

    /**
     * Internal method to create an editor for an existing semantic.
     */
    private ObservableSemanticEditor editSemantic(ObservableSemantic semantic) {
        return new ObservableSemanticEditor(this, semantic);
    }

    /**
     * Creates a unified composer for a pattern entity by PublicId.
     * <p>
     * This is the primary API for working with patterns in ObservableComposer. It automatically
     * determines whether to create a new entity or edit an existing one based on the PublicId.
     *
     * @param publicId the PublicId of the pattern; if an entity with this ID exists, it will be edited,
     *                 otherwise a new entity with this ID will be created
     * @return a unified pattern composer
     * @throws IllegalArgumentException if the PublicId exists but does not correspond to a pattern
     */
    public EntityComposer<ObservablePatternVersion.Editable, ObservablePattern> composePattern(PublicId publicId) {
        requireJavaFXThread();
        ensureTransaction();
        Objects.requireNonNull(publicId, "publicId cannot be null");

        ObservableEntityHandle entityHandle = ObservableEntityHandle.get(publicId);
        if (entityHandle.isAbsent()) {
            return createPatternBuilder().publicId(publicId);
        }
        ObservablePattern pattern = entityHandle.asPattern()
                .orElseThrow(() -> new IllegalArgumentException("PublicId does not correspond to a pattern"));
        return editPattern(pattern);
    }

    /**
     * Internal method to create an editor for an existing pattern.
     */
    private ObservablePatternEditor editPattern(ObservablePattern pattern) {
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
        for (ObservableVersion.Editable<?, ?, ?> editable : trackedEditables) {
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
     * All {@link ObservableVersion.Editable} instances are reset to their original state.
     */
    public void rollback() {
        requireJavaFXThread();
        if (transaction == null) {
            return;
        }

        // Reset all tracked editable versions
        for (ObservableVersion.Editable<?, ?, ?> editable : trackedEditables) {
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
        StampEntity stampEntity = transaction.getStamp(state,
                getAuthorNid(),
                getModuleNid(),
                getPathNid());
        if (entity != null) {
            transaction.addComponent(entity);
        }
        return ObservableEntity.packagePrivateGetStamp(stampEntity);
    }

    /**
     * Tracks an editable version for automatic commit/rollback management.
     */
    void trackEditable(ObservableVersion.Editable<?, ?, ?> editable) {
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
        private StampCalculator stampCalculator;
        private EntityFacade author;
        private EntityFacade module;
        private EntityFacade path;
        private State defaultState = State.ACTIVE;
        private String transactionComment = "";

        private Builder() {}

        /**
         * Sets the author for all entities created by this composer.
         */
        public Builder stampCalculator(StampCalculator stampCalculator) {
            this.stampCalculator = Objects.requireNonNull(stampCalculator, "StampCalculator cannot be null");
            return this;
        }

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
            Objects.requireNonNull(stampCalculator, "stampCalculator must be set");
            Objects.requireNonNull(author, "author must be set");
            Objects.requireNonNull(module, "module must be set");
            Objects.requireNonNull(path, "path must be set");

            return new ObservableComposer(
                    stampCalculator,
                    author.nid(),
                    module.nid(),
                    path.nid(),
                    defaultState,
                    transactionComment
            );
        }
    }

    /**
     * Unified API for both editing existing entities and building new ones.
     * <p>
     * This interface provides a consistent API regardless of whether you're creating
     * a new entity or modifying an existing one. Users don't need to know the difference.
     *
     * @param <V> the type of editable version (e.g., ObservableConceptVersion.Editable)
     * @param <E> the type of observable entity (e.g., ObservableConcept)
     */
    public interface EntityComposer<V extends ObservableVersion.Editable<?, ?, ?>, E extends ObservableEntity<?>> {
        /**
         * Returns the editable version for UI binding.
         */
        V getEditableVersion();

        /**
         * Returns the observable entity being composed (built or edited).
         */
        E getEntity();

        /**
         * Saves changes as uncommitted.
         */
        void save();

        /**
         * Checks if there are unsaved changes.
         */
        boolean isDirty();
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
     * Builder for creating NEW concept entities with fluent configuration.
     * <p>
     * Use this when you need to create a brand new concept from scratch. The builder provides
     * a fluent API for configuring the concept before it's saved to the database.
     *
     * <p><b>When to Use Builder vs Editor</b>
     * <ul>
     *   <li><b>Use Builder</b> - When creating a NEW entity that doesn't exist yet</li>
     *   <li><b>Use Editor</b> - When modifying an EXISTING entity you already have</li>
     * </ul>
     *
     * <p><b>Unified API</b>
     * Both Builder and Editor provide the same core methods for consistency:
     * <ul>
     *   <li>{@link #getEditableVersion()} - Access editable version for UI binding</li>
     *   <li>{@link #getObservableConcept()} - Access the observable concept (after save/build)</li>
     *   <li>{@link #save()} - Save changes as uncommitted</li>
     *   <li>{@link #isDirty()} - Check if there are unsaved changes</li>
     * </ul>
     *
     * <p><b>Example Usage</b>
     * <pre>{@code
     * ObservableComposer composer = ObservableComposer.builder()
     *     .author(TinkarTerm.USER)
     *     .module(TinkarTerm.PRIMORDIAL_MODULE)
     *     .path(TinkarTerm.DEVELOPMENT_PATH)
     *     .build();
     *
     * // Create a new concept
     * ObservableConceptBuilder builder = composer.createConceptBuilder();
     *
     * // Optional: set custom public ID
     * builder.publicId(PublicIds.of(myUuid));
     *
     * // Save as uncommitted (creates entity in database)
     * builder.save();
     *
     * // Now you can access the concept and its editable version
     * ObservableConcept concept = builder.getConcept();
     * ObservableConceptVersion.Editable editable = builder.getEditableVersion();
     *
     * // Use concept as reference for other entities
     * ObservableSemanticBuilder semantic =
     *     composer.createSemanticBuilder(TinkarTerm.DESCRIPTION_PATTERN, concept);
     *
     * // Commit when ready
     * composer.commit();
     * }</pre>
     *
     * @see ObservableConceptEditor for editing existing concepts
     */
    private static final class ObservableConceptBuilder extends EntityBuilder<ObservableConcept>
            implements EntityComposer<ObservableConceptVersion.Editable, ObservableConcept> {
        private PublicId publicId;
        private ObservableConceptVersion.Editable editableVersion;
        private ObservableConcept observableConcept;
        private ConceptRecord conceptRecord;
        private ObservableStamp stampEntity;

        private ObservableConceptBuilder(ObservableComposer composer) {
            super(composer);
        }

        private void ensureInitialized() {
            if (observableConcept == null) {
                requireJavaFXThread();

                // Generate PublicId if not provided
                if (publicId == null) {
                    publicId = PublicIds.newRandom();
                }

                // Create the stamp first
                stampEntity = composer.createStamp(state != null ? state : composer.getDefaultState(), null);

                // Create empty version list builder
                RecordListBuilder<ConceptVersionRecord> versionRecords = RecordListBuilder.make();

                // Create the concept record using the new makeNew method (in-memory)
                this.conceptRecord = ConceptRecord.makeNew(publicId, versionRecords);

                // Add the initial empty version for the current stamp
                ConceptVersionRecord versionRecord = new ConceptVersionRecord(this.conceptRecord, stampEntity.nid());
                versionRecords.addAndBuild(versionRecord);

                // Wrap in observable without persisting yet
                observableConcept = ObservableEntity.packagePrivateGet(this.conceptRecord);

                // Create the editable version for the new stamp-backed version
                editableVersion = ObservableConceptVersion.Editable.getOrCreate(
                        observableConcept,
                        observableConcept.versions().getFirst(),
                        stampEntity
                );

                // Track for commit/rollback management
                composer.getTransaction().addComponent(stampEntity);
                composer.getTransaction().addComponent(observableConcept);
                composer.trackEditable(editableVersion);
            }
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
        public ObservableConceptVersion.Editable getEditableVersion() {
            ensureInitialized();
            return editableVersion;
        }

        /**
         * Returns the observable concept being built.
         */
        public ObservableConcept getObservableConcept() {
            ensureInitialized();
            return observableConcept;
        }

        /**
         * Returns the observable entity being composed (implements EntityComposer).
         */
        @Override
        public ObservableConcept getEntity() {
            return getObservableConcept();
        }

        /**
         * Checks if the editable version has unsaved changes.
         */
        @Override
        public boolean isDirty() {
            return editableVersion != null && editableVersion.isDirty();
        }

        @Override
        public ObservableConcept build() {
            // Implementation would create the concept entity
            throw new UnsupportedOperationException("Implementation pending");
        }

        @Override
        public void save() {
            ensureInitialized();
            // Persist the concept record and stage components in the transaction
            Entity.provider().putEntity(this.conceptRecord);
            if (editableVersion.isDirty()) {
                editableVersion.save();
            }
            composer.getTransaction().addComponent(stampEntity);
            composer.getTransaction().addComponent(observableConcept);
        }
    }

    /**
     * Builder for creating NEW semantic entities with fluent field configuration.
     * <p>
     * Use this when you need to create a new semantic annotation on an existing entity.
     * Semantics attach meaning to entities through patterns with typed fields.
     *
     * <p><b>When to Use Builder vs Editor</b>
     * <ul>
     *   <li><b>Use Builder</b> - When creating a NEW semantic that doesn't exist yet</li>
     *   <li><b>Use Editor</b> - When modifying an EXISTING semantic you already have</li>
     * </ul>
     *
     * <p><b>Unified API</b>
     * Both Builder and Editor provide the same core methods for consistency:
     * <ul>
     *   <li>{@link #getEditableVersion()} - Access editable version for UI binding</li>
     *   <li>{@link #getEditableFields()} - Access editable fields for UI binding</li>
     *   <li>{@link #getObservableSemantic()} - Access the observable semantic (after save/build)</li>
     *   <li>{@link #save()} - Save changes as uncommitted</li>
     *   <li>{@link #isDirty()} - Check if there are unsaved changes</li>
     * </ul>
     *
     * <p><b>Example Usage</b>
     * <pre>{@code
     * // Create a description semantic on a concept
     * ObservableSemanticBuilder builder = composer.createSemanticBuilder(
     *     TinkarTerm.DESCRIPTION_PATTERN,
     *     myConcept
     * );
     *
     * // Save to create the semantic entity
     * builder.save();
     *
     * // Access the semantic and set field values
     * ObservableSemantic semantic = builder.getSemantic();
     * ObservableList<ObservableField.Editable<?>> fields = builder.getEditableFields();
     *
     * // Bind fields to UI controls
     * ((ObservableField.Editable<String>) fields.get(0))
     *     .editableValueProperty()
     *     .bindBidirectional(textField.textProperty());
     *
     * // Or set values programmatically
     * ((ObservableField.Editable<String>) fields.get(0)).setValue("My description");
     * ((ObservableField.Editable<Object>) fields.get(1)).setValue(TinkarTerm.ENGLISH_LANGUAGE);
     *
     * // Check for changes
     * if (builder.isDirty()) {
     *     builder.save();
     * }
     *
     * composer.commit();
     * }</pre>
     *
     * @see ObservableSemanticEditor for editing existing semantics
     */
    private final class ObservableSemanticBuilder extends EntityBuilder<ObservableSemantic>
            implements EntityComposer<ObservableSemanticVersion.Editable, ObservableSemantic> {
        private final Entity referencedComponent;
        private final PatternEntity pattern;
        private PublicId publicId;
        private final List<Object> fieldValues = new ArrayList<>();
        private ObservableSemanticVersion.Editable editableVersion;
        private ObservableSemantic observableSemantic;
        private SemanticRecord semanticRecord;
        private ObservableStamp stampEntity;

        private ObservableSemanticBuilder(ObservableComposer composer, int referencedComponentNid, int patternNid) {
            super(composer);
            this.referencedComponent = ObservableEntityHandle.get(referencedComponentNid).expectEntity();
            this.pattern = EntityHandle.get(patternNid).expectPattern();
        }

        private ObservableSemanticBuilder(ObservableComposer composer, ObservableEntity referencedComponent, int patternNid) {
            super(composer);
            this.referencedComponent = referencedComponent;
            this.pattern = EntityHandle.get(patternNid).expectPattern();
        }

        private void ensureInitialized() {
            if (observableSemantic == null) {
                // Generate publicId if not provided
                if (publicId == null) {
                    publicId = dev.ikm.tinkar.common.id.PublicIds.newRandom();
                }

                // Create the stamp first
                stampEntity = composer.createStamp(state != null ? state : composer.getDefaultState(), null);

                // Prepare versions list (in-memory)
                RecordListBuilder<SemanticVersionRecord> versions = RecordListBuilder.make();

                // Create a new semantic record in-memory
                this.semanticRecord = SemanticRecord.makeNew(publicId, pattern.nid(), referencedComponent.nid(), versions);

                // Always start with an empty version sized by the pattern (null fields allowed)
                makeEmptyVersion(this.semanticRecord, stampEntity, versions);

                versions.build();

                // Wrap in Observable without persisting yet
                observableSemantic = ObservableEntity.packagePrivateGet(this.semanticRecord);

                // Create editable version for current stamp
                editableVersion = observableSemantic.getVersion(this.stampEntity)
                        .orElseThrow(() -> new IllegalStateException("New semantic missing version for stamp nid " + this.stampEntity.nid()))
                        .getEditableVersion(this.stampEntity);

                // Track for commit/rollback management
                composer.getTransaction().addComponent(this.stampEntity);
                composer.getTransaction().addComponent(observableSemantic);
                composer.trackEditable(editableVersion);
            }
        }

        private void makeEmptyVersion(SemanticRecord semanticRecord, ObservableStamp stampEntity, RecordListBuilder versions) {
            Latest<PatternEntityVersion> latestPattern = ObservableComposer.this.stampCalculator.latestPatternEntityVersion(this.pattern);
            latestPattern.ifPresentOrElse(version -> {
               int fieldCount = version.fieldDefinitions().size();
               Object[] fieldValues = new Object[fieldCount];
               SemanticVersion semanticVersion = new SemanticVersionRecord(semanticRecord, stampEntity.nid(), Lists.immutable.of(fieldValues));
               versions.add(semanticVersion);
            }, () -> {
                String errorString = "Pattern latest version not found: \n" +
                        this.pattern + " \n\n For coordinate: " + stampCalculator.stampCoordinate() + "\n\n";
                LOG.error(errorString);
                throw new IllegalStateException(errorString);
            });
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
         * Returns the editable version for UI binding.
         */
        public ObservableSemanticVersion.Editable getEditableVersion() {
            ensureInitialized();
            return editableVersion;
        }

        /**
         * Returns the editable fields for UI binding.
         */
        public ObservableList<ObservableField.Editable<?>> getEditableFields() {
            ensureInitialized();
            return editableVersion.getEditableFields();
        }

        /**
         * Returns the observable semantic being built.
         */
        public ObservableSemantic getObservableSemantic() {
            ensureInitialized();
            return observableSemantic;
        }

        /**
         * Returns the observable entity being composed (implements EntityComposer).
         */
        @Override
        public ObservableSemantic getEntity() {
            return getObservableSemantic();
        }

        /**
         * Checks if the editable version has unsaved changes.
         */
        @Override
        public boolean isDirty() {
            return editableVersion != null && editableVersion.isDirty();
        }

        @Override
        public ObservableSemantic build() {
            throw new UnsupportedOperationException("Implementation pending");
        }

        @Override
        public void save() {
            ensureInitialized();
            // Persist the semantic record; versions may have null fields until authored
            Entity.provider().putEntity(this.semanticRecord);
            if (editableVersion.isDirty()) {
                editableVersion.save();
            }
            // Components already added to transaction during initialization
        }
    }

    /**
     * Builder for creating new pattern entities.
     */
    private final class ObservablePatternBuilder extends EntityBuilder<ObservablePattern>
            implements EntityComposer<ObservablePatternVersion.Editable, ObservablePattern> {
        private PublicId publicId;
        private ObservablePatternVersion.Editable editableVersion; // Strong reference to ensure it's not GC'd
        private ObservablePattern observablePattern; // Strong reference to avoid GC
        private PatternRecord patternRecord;
        private ObservableStamp stampEntity;

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
        public ObservablePatternVersion.Editable getEditableVersion() {
            ensureInitialized();
            return editableVersion;
        }

        private void ensureInitialized() {
            if (observablePattern == null) {
                // Generate publicId if not provided
                if (publicId == null) {
                    publicId = dev.ikm.tinkar.common.id.PublicIds.newRandom();
                }

                // Create the stamp first (in-memory)
                stampEntity = composer.createStamp(state != null ? state : composer.getDefaultState(), null);

                // Prepare versions list (in-memory)
                RecordListBuilder versions = RecordListBuilder.make();

                // Create a Pattern entity in-memory and keep reference
                this.patternRecord = PatternRecord.makeNew(publicId, versions);

                // Wrap in Observable without persisting yet
                observablePattern = ObservableEntity.packagePrivateGet(this.patternRecord);

                // Create the editable version
                if (observablePattern.versions().isEmpty()) {
                    makeEmptyVersion(patternRecord, stampEntity, versions);
                } else {
                    // Is there a version matching the stamp already? If so, we are done.
                    if (!observablePattern.getVersion(stampEntity.nid()).isPresent()) {
                        // No match, so we need to create a new one and will populate it with the latest version
                        ObservableComposer.this.stampCalculator.latestPatternEntityVersion(observablePattern).ifPresentOrElse(version -> {
                            int definitionCount = version.fieldDefinitions().size();
                            FieldDefinitionRecord[] definitions = new FieldDefinitionRecord[definitionCount];
                            for (int i = 0; i < definitionCount; i++) {
                                definitions[i] = (FieldDefinitionRecord) version.fieldDefinitions().get(i);
                            }
                            PatternVersionRecord semanticVersion = new PatternVersionRecord(patternRecord, stampEntity.nid(),
                                    version.semanticPurposeNid(), version.semanticMeaningNid(), Lists.immutable.of(definitions));
                            versions.add(semanticVersion);
                            observablePattern.versions().forEach(v ->
                                    versions.add(v.getVersionRecord().withChronology(patternRecord)));
                        }, () -> {
                            // no latest, so create an empty one.
                            makeEmptyVersion(patternRecord, stampEntity, versions);
                            observablePattern.versions().forEach(v ->
                                    versions.add(v.getVersionRecord().withChronology(patternRecord)));
                        });
                    }
                }
                // Update the observable pattern
                observablePattern = ObservableEntity.packagePrivateGet(patternRecord);
                editableVersion = observablePattern.getVersion(stampEntity).get().getEditableVersion(stampEntity);
                composer.getTransaction().addComponent(stampEntity);
                composer.getTransaction().addComponent(observablePattern);
                composer.trackEditable(editableVersion);
            }
        }

        private void makeEmptyVersion(PatternRecord patternRecord, ObservableStamp stampEntity, RecordListBuilder versions) {
            PatternVersionRecord patternVersionRecord = new PatternVersionRecord(patternRecord, stampEntity.nid(),
            TinkarTerm.PURPOSE.nid(), TinkarTerm.MEANING.nid(),
                    versions);
            versions.add(patternVersionRecord);
        }

        /**
         * Returns the observable pattern being built.
         */
        public ObservablePattern getObservablePattern() {
            ensureInitialized();
            return observablePattern;
        }

        /**
         * Returns the observable entity being composed (implements EntityComposer).
         */
        @Override
        public ObservablePattern getEntity() {
            return getObservablePattern();
        }

        /**
         * Checks if the editable version has unsaved changes.
         */
        @Override
        public boolean isDirty() {
            return editableVersion != null && editableVersion.isDirty();
        }

        @Override
        public ObservablePattern build() {
            throw new UnsupportedOperationException("Implementation pending");
        }

        @Override
        public void save() {
            ensureInitialized();
            // Persist the pattern record; definitions may be incomplete until authored
            Entity.provider().putEntity(this.patternRecord);
            if (editableVersion.isDirty()) {
                editableVersion.save();
            }
            // Components already added to transaction during initialization
        }
    }

    /**
     * Editor for modifying EXISTING concept entities.
     * <p>
     * Use this when you need to edit a concept that already exists in the database.
     * The editor creates a new editable version with the composer's STAMP coordinates,
     * allowing you to modify the entity while tracking changes.
     *
     * <p><b>When to Use Editor vs Builder</b>
     * <ul>
     *   <li><b>Use Builder</b> - When creating a NEW entity that doesn't exist yet</li>
     *   <li><b>Use Editor</b> - When modifying an EXISTING entity you already have</li>
     * </ul>
     *
     * <p><b>Unified API</b>
     * Both Builder and Editor provide the same core methods for consistency:
     * <ul>
     *   <li>{@link #getEditableVersion()} - Access editable version for UI binding</li>
     *   <li>{@link #getConcept()} - Access the observable concept being edited</li>
     *   <li>{@link #save()} - Save changes as uncommitted</li>
     *   <li>{@link #isDirty()} - Check if there are unsaved changes</li>
     * </ul>
     *
     * <p><b>Example Usage</b>
     * <pre>{@code
     * // Get an existing concept
     * ObservableConcept concept = ObservableEntityHandle.getConceptOrThrow(conceptNid);
     *
     * // Create composer and start editing
     * ObservableComposer composer = ObservableComposer.builder()
     *     .author(TinkarTerm.USER)
     *     .module(TinkarTerm.PRIMORDIAL_MODULE)
     *     .path(TinkarTerm.DEVELOPMENT_PATH)
     *     .build();
     *
     * ObservableConceptEditor editor = composer.editConcept(concept);
     *
     * // Get the editable version and modify it
     * ObservableConceptVersion.Editable editable = editor.getEditableVersion();
     * editable.editableStateProperty().set(State.INACTIVE);
     *
     * // Check for changes before saving
     * if (editor.isDirty()) {
     *     editor.save();
     * }
     *
     * // Commit when ready
     * composer.commit();
     * }</pre>
     *
     * @see ObservableConceptBuilder for creating new concepts
     */
    private static final class ObservableConceptEditor
            implements EntityComposer<ObservableConceptVersion.Editable, ObservableConcept> {
        private final ObservableComposer composer;
        private final ObservableConcept concept;
        private ObservableConceptVersion.Editable editableVersion;

        private ObservableConceptEditor(ObservableComposer composer, ObservableConcept concept) {
            this.composer = composer;
            this.concept = concept;
        }

        /**
         * Returns the editable version for UI binding.
         */
        public ObservableConceptVersion.Editable getEditableVersion() {
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
        @Override
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

        /**
         * Returns the observable entity being composed (implements EntityComposer).
         */
        @Override
        public ObservableConcept getEntity() {
            return concept;
        }

        /**
         * Checks if the editable version has unsaved changes.
         */
        @Override
        public boolean isDirty() {
            return editableVersion != null && editableVersion.isDirty();
        }
    }

    /**
     * Editor for modifying EXISTING semantic entities.
     * <p>
     * Use this when you need to edit a semantic that already exists in the database.
     * The editor creates a new editable version with the composer's STAMP coordinates,
     * providing access to editable fields for UI binding and modification.
     *
     * <p><b>When to Use Editor vs Builder</b>
     * <ul>
     *   <li><b>Use Builder</b> - When creating a NEW semantic that doesn't exist yet</li>
     *   <li><b>Use Editor</b> - When modifying an EXISTING semantic you already have</li>
     * </ul>
     *
     * <p><b>Unified API</b>
     * Both Builder and Editor provide the same core methods for consistency:
     * <ul>
     *   <li>{@link #getEditableVersion()} - Access editable version for UI binding</li>
     *   <li>{@link #getEditableFields()} - Access editable fields for UI binding</li>
     *   <li>{@link #getSemantic()} - Access the observable semantic being edited</li>
     *   <li>{@link #save()} - Save changes as uncommitted</li>
     *   <li>{@link #isDirty()} - Check if there are unsaved changes</li>
     * </ul>
     *
     * <p><b>Example Usage</b>
     * <pre>{@code
     * // Get an existing semantic (e.g., a description)
     * ObservableSemantic semantic = ObservableEntityHandle.getSemanticOrThrow(semanticNid);
     *
     * // Create composer and start editing
     * ObservableComposer composer = ObservableComposer.builder()
     *     .author(TinkarTerm.USER)
     *     .module(TinkarTerm.PRIMORDIAL_MODULE)
     *     .path(TinkarTerm.DEVELOPMENT_PATH)
     *     .build();
     *
     * ObservableSemanticEditor editor = composer.editSemantic(semantic);
     *
     * // Get editable fields and modify them
     * ObservableList<ObservableField.Editable<?>> fields = editor.getEditableFields();
     * ObservableField.Editable<String> textField = (ObservableField.Editable<String>) fields.get(0);
     *
     * // Bind to UI control
     * myTextField.textProperty().bindBidirectional(textField.editableValueProperty());
     *
     * // Or modify directly
     * textField.setValue("Updated description text");
     *
     * // Check for changes and save
     * if (editor.isDirty()) {
     *     editor.save();
     * }
     *
     * // Commit the transaction
     * composer.commit();
     * }</pre>
     *
     * @see ObservableSemanticBuilder for creating new semantics
     */
    private static final class ObservableSemanticEditor
            implements EntityComposer<ObservableSemanticVersion.Editable, ObservableSemantic> {
        private final ObservableComposer composer;
        private final ObservableSemantic semantic;
        private ObservableSemanticVersion.Editable editableVersion;

        private ObservableSemanticEditor(ObservableComposer composer, ObservableSemantic semantic) {
            this.composer = composer;
            this.semantic = semantic;
        }

        /**
         * Returns the editable version for UI binding.
         */
        public ObservableSemanticVersion.Editable getEditableVersion() {
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
        public ObservableList<ObservableField.Editable<?>> getEditableFields() {
            return getEditableVersion().getEditableFields();
        }

        /**
         * Saves changes as uncommitted.
         */
        @Override
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

        /**
         * Returns the observable entity being composed (implements EntityComposer).
         */
        @Override
        public ObservableSemantic getEntity() {
            return semantic;
        }

        /**
         * Checks if the editable version has unsaved changes.
         */
        @Override
        public boolean isDirty() {
            return editableVersion != null && editableVersion.isDirty();
        }
    }

    /**
     * Editor for existing pattern entities.
     */
    private static final class ObservablePatternEditor
            implements EntityComposer<ObservablePatternVersion.Editable, ObservablePattern> {
        private final ObservableComposer composer;
        private final ObservablePattern pattern;
        private ObservablePatternVersion.Editable editableVersion;

        private ObservablePatternEditor(ObservableComposer composer, ObservablePattern pattern) {
            this.composer = composer;
            this.pattern = pattern;
        }

        /**
         * Returns the editable version for UI binding.
         */
        public ObservablePatternVersion.Editable getEditableVersion() {
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
        @Override
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

        /**
         * Returns the observable entity being composed (implements EntityComposer).
         */
        @Override
        public ObservablePattern getEntity() {
            return pattern;
        }

        /**
         * Checks if the editable version has unsaved changes.
         */
        @Override
        public boolean isDirty() {
            return editableVersion != null && editableVersion.isDirty();
        }
    }
}
