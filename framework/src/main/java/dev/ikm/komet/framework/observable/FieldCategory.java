package dev.ikm.komet.framework.observable;

/**
 * The ComponentFieldCategories enumeration represents the various categories of fields associated
 * with components in an observable field framework. Each constant defines a specific type of field
 * and its corresponding source, which is derived from the {@link FieldSource} enumeration.
 * The categories are used to classify and organize field definitions according to their context within
 * the system.
 * <p>
 * <p> Each field category is associated with one of the following sources:
 * <p> - COMPONENT: Represents fields directly contained within a component.
 * <p> - COMPONENT_VERSION: Represents fields specific to a version of a component.
 * <p> - PATTERN: Represents fields directly contained within a pattern.
 * <p> - PATTERN_VERSION: Represents fields directly contained within a specific version of a pattern.
 * <p> - SEMANTIC: Represents fields directly contained within a semantic, including fields not directly defined in a component.
 * <p> - SEMANTIC_VERSION: Represents fields specific to a version of a semantic.
 * <p> - STAMP: Represents fields specific to a stamp.
 * <p> - STAMP_VERSION: Represents fields specific to a version of a stamp.
 * <p>
 * <p> This enumeration is commonly used in contexts where fields need to be referenced and categorized
 * such as in field definitions, record structures, or field-related operations within frameworks.
 * <p>
 * <p> The constants provided by this enumeration include:
 * <p> - PUBLIC_ID_FIELD: A field for public identifiers, sourced from COMPONENT.
 * <p> - COMPONENT_VERSIONS_LIST: A field representing versions of a component, sourced from COMPONENT.
 * <p> - VERSION_STAMP_FIELD: A field for version-related stamps, sourced from COMPONENT_VERSION.
 * <p> - PATTERN_MEANING_FIELD: A field for pattern meaning, sourced from PATTERN_VERSION.
 * <p> - PATTERN_PURPOSE_FIELD: A field for pattern purpose, sourced from PATTERN_VERSION.
 * <p> - PATTERN_FIELD_DEFINITION_LIST: A pattern field definition list, sourced from PATTERN_VERSION.
 * <p> - PATTERN_FIELD_DEFINITION: A single pattern field definition, sourced from the PATTERN_FIELD_DEFINITION_LIST on the PATTERN_VERSION.
 * <p> - SEMANTIC_PATTERN_FIELD: A field for the semantic pattern, sourced from SEMANTIC.
 * <p> - SEMANTIC_REFERENCED_COMPONENT_FIELD: A field referencing a semantic component, sourced from SEMANTIC.
 * <p> - SEMANTIC_FIELD_LIST: A list of semantic fields, sourced from SEMANTIC_VERSION.
 * <p> - SEMANTIC_FIELD: A single semantic field, sourced from the SEMANTIC_FIELD_LIST on the SEMANTIC_VERSION.
 * <p> - STATUS_FIELD: A field representing the status, sourced from STAMP_VERSION retrieved via the VERSION_STAMP_FIELD.
 * <p> - TIME_FIELD: A field representing the timestamp, sourced from STAMP_VERSION retrieved via the VERSION_STAMP_FIELD.
 * <p> - AUTHOR_FIELD: A field representing the author, sourced from STAMP_VERSION retrieved via the VERSION_STAMP_FIELD.
 * <p> - MODULE_FIELD: A field representing the module, sourced from STAMP_VERSION retrieved via the VERSION_STAMP_FIELD.
 * <p> - PATH_FIELD: A field representing the path, sourced from STAMP_VERSION retrieved via the VERSION_STAMP_FIELD.
 * <p>
 * Each enum constant includes a corresponding {@link FieldSource} to specify the origin
 * of the field category.
 */
public enum FieldCategory {
    PUBLIC_ID_FIELD(FieldSource.COMPONENT),
    COMPONENT_VERSIONS_LIST(FieldSource.COMPONENT),
    VERSION_STAMP_FIELD(FieldSource.COMPONENT_VERSION),
    PATTERN_MEANING_FIELD(FieldSource.PATTERN_VERSION),
    PATTERN_PURPOSE_FIELD(FieldSource.PATTERN_VERSION),
    PATTERN_FIELD_DEFINITION_LIST(FieldSource.PATTERN_VERSION),
    PATTERN_FIELD_DEFINITION(FieldSource.PATTERN_VERSION),
    SEMANTIC_PATTERN_FIELD(FieldSource.SEMANTIC),
    SEMANTIC_REFERENCED_COMPONENT_FIELD(FieldSource.SEMANTIC),
    SEMANTIC_FIELD_LIST(FieldSource.SEMANTIC_VERSION),
    SEMANTIC_FIELD(FieldSource.SEMANTIC_VERSION),
    STATUS_FIELD(FieldSource.STAMP_VERSION),
    TIME_FIELD(FieldSource.STAMP_VERSION),
    AUTHOR_FIELD(FieldSource.STAMP_VERSION),
    MODULE_FIELD(FieldSource.STAMP_VERSION),
    PATH_FIELD(FieldSource.STAMP_VERSION);

    public final FieldSource source;

    FieldCategory(FieldSource fieldSource) {
        this.source = fieldSource;
    }
}
