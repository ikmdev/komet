package dev.ikm.komet.layout;

/**
 * The KlStateCommands interface defines a contract for managing the lifecycle of a state or configuration.
 * Implementing classes are expected to provide functionality for saving, reverting, and deleting states.
 */
public interface KlStateCommands {
    /**
     * Saves the current state or configuration.
     * This method is expected to persist any changes or updates
     * to the underlying state or data model.
     */
    void save();
    /**
     * Reverts the state or configuration to its previous saved state.
     * This method is used to undo changes and restore the state to a known
     * or previously committed configuration. It is expected that any changes
     * made since the last save operation will be discarded.
     */
    void revert();
    /**
     * Deletes the current state or configuration.
     * This method is expected to remove or clear the existing state,
     * configuration, or data associated with the implementation.
     * Any saved or persisted information may be permanently removed
     * as a result of this operation.
     */
    void delete();
}
