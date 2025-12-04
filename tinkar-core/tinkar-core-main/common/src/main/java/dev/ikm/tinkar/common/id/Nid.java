package dev.ikm.tinkar.common.id;

/**
 * Native Identifier (NID) validation utilities for the Tinkar system.
 * <p>
 * A NID is an integer identifier used throughout Tinkar to reference entities like concepts,
 * semantics, and patterns. Certain integer values are reserved for special purposes and should
 * not be used as valid NIDs.
 * <p>
 * <b>Reserved Values:</b>
 * <ul>
 *   <li><b>0</b> - Represents "not set" or invalid NID</li>
 *   <li><b>Integer.MAX_VALUE</b> - Sentinel value (e.g., uncommitted timestamps)</li>
 *   <li><b>Integer.MIN_VALUE</b> - Sentinel value for invalid/special states</li>
 * </ul>
 * <p>
 * This interface provides static methods to validate NIDs in both throwing and non-throwing
 * variants, ensuring data integrity and preventing corruption from reserved value usage.
 *
 * @see #validate(int) for throwing validation
 * @see #isValid(int) for non-throwing validation
 */
public interface Nid {

    /**
     * Validates that a NID is not one of the reserved/invalid values and returns it.
     * <p>
     * These values are reserved in Tinkar for special purposes:
     * <ul>
     *   <li><b>0</b> - Used to represent "not set" or invalid NID</li>
     *   <li><b>Integer.MAX_VALUE</b> - Used as sentinel value (e.g., uncommitted timestamps)</li>
     *   <li><b>Integer.MIN_VALUE</b> - Used as sentinel value for invalid/special states</li>
     * </ul>
     * <p>
     * This validation is useful for:
     * <ul>
     *   <li>Unit testing field values to ensure they don't contain reserved integers</li>
     *   <li>Validating user input before storing in semantic fields</li>
     *   <li>Asserting that entity NIDs are valid before processing</li>
     *   <li>Preventing data corruption from reserved value usage</li>
     * </ul>
     *
     * <h3>Usage Examples:</h3>
     * <pre>{@code
     * // Validate and assign in one line
     * public void setMeaningNid(int meaningNid) {
     *     this.meaningNid = Nid.validate(meaningNid);
     * }
     *
     * // In tests - validate field values
     * @Test
     * void testSemanticFieldsNotReserved() {
     *     SemanticEntity semantic = EntityHandle.getSemanticOrThrow(semanticNid);
     *     semantic.fieldValues().forEach(field -> {
     *         if (field instanceof Integer intValue) {
     *             Nid.validate(intValue);
     *         }
     *     });
     * }
     *
     * // Validate entity references before use
     * Entity<?> entity = EntityHandle.getEntityOrThrow(Nid.validate(entityNid));
     * }</pre>
     *
     * @param nid the NID value to validate
     * @return the validated NID (same as input)
     * @throws IllegalArgumentException if the NID is one of the reserved values (0, Integer.MAX_VALUE, Integer.MIN_VALUE)
     * @see #isValid(int) for non-throwing validation
     */
    static int validate(int nid) {
        if (nid == 0) {
            throw new IllegalArgumentException(
                    "NID cannot be 0 - this is a reserved value representing 'not set' or invalid NID"
            );
        }
        if (nid == Integer.MAX_VALUE) {
            throw new IllegalArgumentException(
                    "NID cannot be Integer.MAX_VALUE (" + Integer.MAX_VALUE + ") - " +
                            "this is a reserved sentinel value (e.g., for uncommitted timestamps)"
            );
        }
        if (nid == Integer.MIN_VALUE) {
            throw new IllegalArgumentException(
                    "NID cannot be Integer.MIN_VALUE (" + Integer.MIN_VALUE + ") - " +
                            "this is a reserved sentinel value for invalid/special states"
            );
        }
        return nid;
    }

    /**
     * Checks if a NID is valid (not one of the reserved values).
     * <p>
     * This is a non-throwing variant of {@link #validate(int)} that returns a boolean
     * instead of throwing an exception. Use this when you want to check validity without
     * exception handling.
     *
     * <h3>Usage Examples:</h3>
     * <pre>{@code
     * // Filter valid NIDs from a collection
     * List<Integer> validNids = allNids.stream()
     *     .filter(Nid::isValid)
     *     .toList();
     *
     * // Conditional validation
     * if (Nid.isValid(nid)) {
     *     processEntity(nid);
     * } else {
     *     LOG.warn("Skipping invalid NID: {}", nid);
     * }
     *
     * // Count valid vs invalid
     * long validCount = nids.stream()
     *     .filter(Nid::isValid)
     *     .count();
     * }</pre>
     *
     * @param nid the NID value to check
     * @return {@code true} if the NID is valid (not 0, Integer.MAX_VALUE, or Integer.MIN_VALUE)
     * @see #validate(int) for validation that throws exceptions
     */
    static boolean isValid(int nid) {
        return nid != 0 && nid != Integer.MAX_VALUE && nid != Integer.MIN_VALUE;
    }

    /**
     * Validates that a NID is valid with a custom error message and returns it.
     * <p>
     * This variant allows you to provide domain-specific context in the exception message.
     *
     * <h3>Usage Example:</h3>
     * <pre>{@code
     * // Validate with context and assign in one line
     * public void setMeaningNid(int meaningNid) {
     *     this.meaningNid = Nid.validate(meaningNid, "Field meaning NID");
     * }
     * }</pre>
     *
     * @param nid the NID value to validate
     * @param contextDescription a description of what this NID represents (e.g., "field value", "concept reference")
     * @return the validated NID (same as input)
     * @throws IllegalArgumentException if the NID is one of the reserved values, with contextual error message
     */
    static int validate(int nid, String contextDescription) {
        if (nid == 0) {
            throw new IllegalArgumentException(
                    contextDescription + " cannot be 0 - this is a reserved value representing 'not set' or invalid NID"
            );
        }
        if (nid == Integer.MAX_VALUE) {
            throw new IllegalArgumentException(
                    contextDescription + " cannot be Integer.MAX_VALUE (" + Integer.MAX_VALUE + ") - " +
                            "this is a reserved sentinel value"
            );
        }
        if (nid == Integer.MIN_VALUE) {
            throw new IllegalArgumentException(
                    contextDescription + " cannot be Integer.MIN_VALUE (" + Integer.MIN_VALUE + ") - " +
                            "this is a reserved sentinel value"
            );
        }
        return nid;
    }
}
