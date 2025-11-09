package dev.ikm.komet.framework.observable;

/**
 * Marker interface for editable versions that cache field changes for GUI editing.
 * <p>
 * This interface is <b>orthogonal</b> to the Marker-Generic-Concrete (MGC) pattern hierarchy.
 * It represents a cross-cutting concern (editability) that applies to concrete version types
 * without breaking the three-layer MGC structure.
 *
 * <h2>Relationship to MGC Pattern</h2>
 * <p>This is NOT a fourth layer in the MGC hierarchy. Instead, it's a separate abstraction
 * that Layer 3 (concrete) Editable inner classes implement:
 * <pre>
 * MGC Layers (Vertical):           Editable Concern (Orthogonal):
 * ┌──────────────────────┐          ┌──────────────────────┐
 * │ ObservableVersion    │          │ EditableVersion      │
 * │ (L1 - Marker)        │          │ (orthogonal marker)  │
 * └──────────┬───────────┘          └──────────┬───────────┘
 *            │ implements                      │ implements
 * ┌───────────────────────┐         ┌──────────────────────┐
 * │ObservableEntityVersion│         │ConceptVersion.       │
 * │ (L2 - Generic)        │         │   Editable           │
 * └──────────┬────────────┘         │ (also L3!)           │
 *            │ extends              └──────────────────────┘
 * ┌────────────────────────┐
 * │ObservableConceptVersion│
 * │ (L3 - Concrete)        │
 * └────────────────────────┘
 * </pre>
 *
 * <h2>Why Orthogonal?</h2>
 * <ul>
 *   <li><b>Composition over Inheritance:</b> Editables <i>wrap</i> versions rather than
 *       extending them in the type hierarchy</li>
 *   <li><b>Cross-Cutting Concern:</b> Editability applies to all version types uniformly,
 *       like {@link java.io.Serializable} applies across hierarchies</li>
 *   <li><b>Preserves MGC Pattern:</b> Doesn't add a fourth layer - keeps the clean
 *       three-layer structure intact</li>
 * </ul>
 *
 * <h2>Editable Lifecycle</h2>
 * <p>An editable version goes through these states:
 * <ol>
 *   <li><b>Created:</b> Wraps an {@link ObservableVersion} with an {@link ObservableStamp}</li>
 *   <li><b>Modified:</b> Fields are changed via JavaFX properties, {@link #isDirty()} returns true</li>
 *   <li><b>Saved:</b> {@link #save()} writes uncommitted version to database</li>
 *   <li><b>Committed:</b> {@link #commit()} finalizes changes and makes them permanent</li>
 *   <li><b>Reset:</b> {@link #reset()} discards changes and reverts to original</li>
 * </ol>
 *
 * <h2>Canonical Instance Guarantee</h2>
 * <p>For any given combination of {@link ObservableVersion} and {@link ObservableStamp},
 * there exists exactly ONE editable instance. Multiple calls to
 * {@code version.getEditableVersion(stamp)} with the same stamp return the same object,
 * ensuring all GUI components share the same working copy.
 *
 * <h2>Usage in Consumer APIs</h2>
 * <pre>{@code
 * // Clean, generic-free editing APIs
 * public void setupEditForm(EditableVersion editable) {
 *     // Bind dirty indicator
 *     dirtyIndicator.visibleProperty().bind(
 *         Bindings.createBooleanBinding(editable::isDirty));
 *
 *     // Wire up buttons
 *     saveButton.setOnAction(e -> editable.save());
 *     commitButton.setOnAction(e -> editable.commit());
 *     cancelButton.setOnAction(e -> editable.reset());
 * }
 *
 * // Pattern matching for type-specific editing
 * public void bindFields(EditableVersion editable) {
 *     switch (editable) {
 *         case ObservableConceptVersion.Editable ce -> {
 *             // Concepts have minimal fields
 *             bindStampFields(ce);
 *         }
 *         case ObservablePatternVersion.Editable pe -> {
 *             // Pattern-specific editable properties
 *             purposeField.valueProperty().bindBidirectional(
 *                 pe.getPurposeProperty());
 *             meaningField.valueProperty().bindBidirectional(
 *                 pe.getMeaningProperty());
 *         }
 *         case ObservableSemanticVersion.Editable se -> {
 *             // Semantic field editing
 *             bindSemanticFields(se);
 *         }
 *         case ObservableStampVersion.Editable ste -> {
 *             // Stamp field editing
 *             bindStampFields(ste);
 *         }
 *     }
 * }
 *
 * // Collections without wildcards
 * ObservableList<EditableVersion> editables = FXCollections.observableArrayList();
 * }</pre>
 *
 * <h2>Thread Safety</h2>
 * <p>⚠️ <b>IMPORTANT:</b> Editables must be accessed only on the JavaFX application thread,
 * as they wrap {@link ObservableVersion} instances which have the same thread requirement.
 *
 * <h2>Relationship to SheetItem</h2>
 * <p>For property sheet editing, use {@link dev.ikm.komet.framework.propsheet.SheetItem#makeEditable} methods which
 * work with editables to provide field-level editing with proper validation and formatting.
 *
 * @see ObservableVersion
 * @see ObservableEntityVersion.Editable
 * @see dev.ikm.komet.framework.propsheet.SheetItem#makeEditable(dev.ikm.komet.framework.observable.ObservableSemanticVersion.Editable, int, dev.ikm.komet.framework.view.ViewProperties)
 */
public sealed interface EditableVersion
        permits ObservableConceptVersion.Editable,
        ObservableEntityVersion.Editable,
        ObservablePatternVersion.Editable, ObservableSemanticVersion.Editable,
        ObservableStampVersion.Editable {

    /**
     * Returns the original observable version being edited.
     * <p>
     * This is the read-only version that this editable wraps. Changes made through
     * this editable do not affect the observable version until {@link #save()} or
     * {@link #commit()} is called.
     *
     * @return the observable version (never null)
     */
    ObservableVersion getObservableVersion();

    /**
     * Returns the observable stamp identifying the editor.
     * <p>
     * The stamp typically identifies the author making changes. The same stamp
     * ensures the same canonical editable instance is returned.
     * <p>
     * Note: The stamp may change from uncommitted to committed state during
     * the editing lifecycle, which is why we use {@link ObservableStamp} instead
     * of immutable {@link dev.ikm.tinkar.entity.StampEntity}.
     *
     * @return the observable stamp (never null)
     */
    ObservableStamp getEditStamp();

    /**
     * Returns whether this editable has unsaved changes.
     * <p>
     * An editable is considered "dirty" when its working version differs from
     * the original observable version's data. This is useful for:
     * <ul>
     *   <li>Showing visual indicators (asterisks, icons) in the UI</li>
     *   <li>Enabling/disabling save/commit buttons</li>
     *   <li>Prompting users before navigating away</li>
     * </ul>
     *
     * @return true if there are unsaved changes, false otherwise
     */
    boolean isDirty();

    /**
     * Saves the current working version as an uncommitted version to the database.
     * <p>
     * This writes the changes to the database but marks them as uncommitted
     * (time = Long.MAX_VALUE). The changes will be reflected back to the
     * observable entity through the event bus, making them visible to other
     * observers.
     * <p>
     * <b>When to use:</b>
     * <ul>
     *   <li>Auto-save functionality</li>
     *   <li>Saving work-in-progress before switching contexts</li>
     *   <li>Checkpointing before major changes</li>
     * </ul>
     * <p>
     * <b>Idempotent:</b> Calling save() when not dirty is a no-op.
     * <p>
     * <b>Thread Safety:</b> Must be called on JavaFX application thread.
     *
     * @see #commit()
     * @see #isDirty()
     */
    void save();

    /**
     * Commits the transaction and writes the committed version to the database.
     * <p>
     * This finalizes all changes, marking them as committed with the current
     * timestamp. Once committed, the version becomes part of the permanent
     * version history and can be seen by all users on the same development path.
     * <p>
     * <b>When to use:</b>
     * <ul>
     *   <li>User explicitly commits changes via "Commit" button</li>
     *   <li>Closing an editing session and publishing changes</li>
     *   <li>Finalizing a batch of edits</li>
     * </ul>
     * <p>
     * <b>Lifecycle:</b> After commit, the editable can continue to be used
     * for further edits, which will create a new uncommitted version if saved.
     * <p>
     * <b>Thread Safety:</b> Must be called on JavaFX application thread.
     *
     * @see #save()
     * @see dev.ikm.tinkar.entity.transaction.Transaction
     */
    void commit();

    /**
     * Discards all cached changes and reverts to the original version.
     * <p>
     * This resets the working version to match the original observable version's
     * data. All field changes are lost. If a transaction was created (via save()),
     * it is canceled.
     * <p>
     * <b>When to use:</b>
     * <ul>
     *   <li>User clicks "Cancel" button</li>
     *   <li>Discarding invalid edits</li>
     *   <li>Reverting after errors</li>
     * </ul>
     * <p>
     * <b>Idempotent:</b> Calling reset() multiple times has the same effect as once.
     * <p>
     * <b>Thread Safety:</b> Must be called on JavaFX application thread.
     *
     * @see #isDirty()
     */
    void reset();
}