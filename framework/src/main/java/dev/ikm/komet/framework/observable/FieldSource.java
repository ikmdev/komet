package dev.ikm.komet.framework.observable;

/**
 * The ComponentFieldSource enumeration defines the origin or source of various fields associated
 * with components in the observable field framework. Each constant provides a categorization of
 * fields that can be used within the system.
 * <p> <p>
 * <p> COMPONENT: Categorizes fields contained directly within a component.
 * <p> COMPONENT_VERSION: Categorizes fields specific to a particular version of a component.
 * <p> PATTERN: Categorizes fields contained directly within a pattern (added for symmetry,
 * currently there are no fields contained directly within a pattern that aren't on the component).
 * <p> PATTERN_VERSION: Categorizes fields associated with a specific version of a pattern.
 * <p> SEMANTIC: Categorizes fields specific to a semantic, including fields beyond those defined in a component.
 * <p> SEMANTIC_VERSION: Categorizes fields specific to a particular version of a semantic.
 * <p> STAMP: Categorizes fields contained directly with a stamp (added for symmetry,
 *  * currently there are no fields contained directly within a stamp that aren't on the component).
 * <p> STAMP_VERSION: Categorizes fields specific to a particular version of a stamp.
 * <p>
 * This enumeration is used to categorize and identify the context of various
 * field definitions, enabling consistent handling throughout the framework.
 */
public enum FieldSource {
    COMPONENT,
    COMPONENT_VERSION,
    PATTERN, // for symmetry, there are no fields on a pattern that aren't on the component
    PATTERN_VERSION,
    SEMANTIC, // Semantic has fields that aren't on the component
    SEMANTIC_VERSION,
    STAMP, // for symmetry, there are no fields on a pattern that aren't on the component
    STAMP_VERSION
}
