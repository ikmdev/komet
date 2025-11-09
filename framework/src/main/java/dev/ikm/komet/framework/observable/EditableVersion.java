package dev.ikm.komet.framework.observable;

/**
 * Marker interface for editable entity versions that cache field changes.
 * <p>
 * Provides a generic-free type for working with editable versions without exposing
 * entity and version type parameters. This is Layer 1 (Marker) of the MGC pattern
 * for editable versions.
 *
 * <h2>MGC Pattern Layers for Editable Versions</h2>
 * <ul>
 *   <li><b>Layer 1:</b> {@code EditableVersion} - Marker interface (this interface)</li>
 *   <li><b>Layer 2:</b> {@link ObservableEntityVersion.Editable} - Generic abstract class</li>
 *   <li><b>Layer 3:</b> {@link ObservableConceptVersion.Editable},
 *       {@link ObservablePatternVersion.Editable}, etc. - Concrete final nested classes</li>
 * </ul>
 *
 * <h2>Editable Version Lifecycle</h2>
 * <pre>
 * Create Editable → Modify Fields → save() → commit()
 *                        ↓                       ↓
 *                   Cached Changes      Written to DB
 *                        ↓                       ↓
 *                   isDirty()?              Committed
 *                        ↓
 *                   reset() ← Cancel Changes
 * </pre>
 *
 * <h2>Key Characteristics</h2>
 * <ul>
 *   <li><b>Cached Editing:</b> Changes accumulate in memory until save()</li>
 *   <li><b>Transaction Support:</b> commit() makes changes permanent</li>
 *   <li><b>Rollback Support:</b> reset() discards changes</li>
 *   <li><b>Canonical Instance:</b> Same stamp always returns same editable instance</li>
 *   <li><b>JavaFX Properties:</b> Editable fields can be bound to UI controls</li>
 * </ul>
 *
 * <h2>Usage</h2>
 * <pre>{@code
 * // Get editable version
 * ObservableConceptVersion version = concept.lastVersion();
 * EditableVersion editable = version.getEditableVersion(editStamp);
 *
 * // Pattern matching for specific operations
 * switch (editable) {
 *     case ObservableConceptVersion.Editable ce -> {
 *         // Concept-specific editing
 *         ce.editableStateProperty().set(State.ACTIVE);
 *     }
 *     case ObservableSemanticVersion.Editable se -> {
 *         // Semantic-specific editing
 *         se.getEditableFields().get(0).setValue("New text");
 *     }
 *     default -> throw new IllegalStateException();
 * }
 *
 * // Save and commit
 * editable.save();
 * editable.commit();
 * }</pre>
 *
 * <h2>Relationship to ObservableVersion</h2>
 * <p>
 * Every {@link ObservableVersion} can produce an {@code EditableVersion} via
 * {@code getEditableVersion(ObservableStamp)}. The editable version wraps the
 * read-only version and provides cached modification capabilities.
 *
 * @see ObservableEntityVersion.Editable
 * @see ObservableVersion
 * @see ObservableStamp
 * @see ObservableComposer
 */
public sealed interface EditableVersion
        permits ObservableConceptVersion.Editable, ObservableEntityVersion.Editable, ObservablePatternVersion.Editable, ObservableSemanticVersion.Editable, ObservableStampVersion.Editable {
    
    /**
     * Returns the original read-only ObservableVersion being edited.
     */
    ObservableVersion getObservableVersion();
    
    /**
     * Returns the observable stamp for this editable version.
     */
    ObservableStamp getEditStamp();
    
    /**
     * Returns whether this editable version has unsaved changes.
     */
    boolean isDirty();
    
    /**
     * Saves the current working version as an uncommitted version to the database.
     */
    void save();
    
    /**
     * Commits the transaction and writes the committed version to the database.
     */
    void commit();
    
    /**
     * Discards all cached changes and reverts to the original version.
     */
    void reset();
}