package dev.ikm.komet.framework.observable;


import java.util.Collection;

import dev.ikm.tinkar.collection.ImmutableEnumSet;

import static dev.ikm.komet.framework.observable.FieldCategory.*;

/**
 * A concrete implementation of {@link ImmutableEnumSet} that uses {@link FieldCategory}
 * as the enumeration type. This class provides a type-safe, immutable set of {@link FieldCategory}
 * elements, enabling efficient storage and quick operations for sets involving {@link FieldCategory}.
 */
public class FieldCategorySet extends ImmutableEnumSet<FieldCategory> {
    /**
     * Constructs a new {@code ComponentFieldSet} instance, representing an immutable set of
     * {@link FieldCategory} elements. This constructor allows for the direct initialization of
     * the set using a variable number of {@link FieldCategory} elements.
     *
     * @param elements the enumeration elements to include in the set. These must be of type {@link FieldCategory}.
     */
    public FieldCategorySet(FieldCategory... elements) {
        super(elements);
    }

    /**
     * Constructs a new {@code ComponentFieldSet} instance, representing an immutable set of
     * {@link FieldCategory} elements using the elements provided within the given collection.
     * This constructor allows for initializing the set with a collection of {@link FieldCategory}
     * elements, ensuring type safety and immutability.
     *
     * @param elements the collection of {@link FieldCategory} elements to include in the set.
     *                 These elements must be of type {@link FieldCategory}.
     */
    public FieldCategorySet(Collection<? extends FieldCategory> elements) {
        super(elements);
    }

    /**
     * Retrieves the {@link Class} object associated with the enumeration type
     * {@link FieldCategory}, used by this class to define immutable enum sets.
     *
     * @return the {@link Class} object representing the {@link FieldCategory} enumeration type.
     */
    @Override
    protected Class<FieldCategory> enumClass() {
        return FieldCategory.class;
    }


    private static final FieldCategorySet COMPONENT_FIELDS = new FieldCategorySet(PUBLIC_ID_FIELD, COMPONENT_VERSIONS_LIST);
    private static final FieldCategorySet PATTERN_VERSION_FIELDS = new FieldCategorySet(PUBLIC_ID_FIELD, PATTERN_MEANING_FIELD, PATTERN_PURPOSE_FIELD, PATTERN_FIELD_DEFINITION_LIST,
            STATUS_FIELD, TIME_FIELD, AUTHOR_FIELD, MODULE_FIELD, PATH_FIELD);

    private static final FieldCategorySet SEMANTIC_FIELDS = new FieldCategorySet(PUBLIC_ID_FIELD, SEMANTIC_PATTERN_FIELD, SEMANTIC_REFERENCED_COMPONENT_FIELD, COMPONENT_VERSIONS_LIST);
    private static final FieldCategorySet SEMANTIC_VERSION_FIELDS = new FieldCategorySet(PUBLIC_ID_FIELD, SEMANTIC_PATTERN_FIELD, SEMANTIC_REFERENCED_COMPONENT_FIELD, SEMANTIC_FIELD_LIST,
            STATUS_FIELD, TIME_FIELD, AUTHOR_FIELD, MODULE_FIELD, PATH_FIELD);

    private static final FieldCategorySet STAMP_VERSION_FIELDS = new FieldCategorySet(PUBLIC_ID_FIELD,
            STATUS_FIELD, TIME_FIELD, AUTHOR_FIELD, MODULE_FIELD, PATH_FIELD);
    public static FieldCategorySet componentFields() {
        return COMPONENT_FIELDS;
    }

    public static FieldCategorySet conceptFields() {
        return COMPONENT_FIELDS;
    }

    public static FieldCategorySet patternFields() {
        return COMPONENT_FIELDS;
    }

    public static FieldCategorySet semanticFields() {
        return SEMANTIC_FIELDS;
    }

    public static FieldCategorySet stampFields() {
        return COMPONENT_FIELDS;
    }

    public static FieldCategorySet conceptVersionFields() {
        return STAMP_VERSION_FIELDS;
    }

    public static FieldCategorySet semanticVersionFields() {
        return SEMANTIC_VERSION_FIELDS;
    }

    public static FieldCategorySet patternVersionFields() {
        return PATTERN_VERSION_FIELDS;
    }

    public static FieldCategorySet stampVersionFields() {
        return STAMP_VERSION_FIELDS;
    }

    public static FieldCategorySet fieldsForObservable(ObservableEntity observableEntity) {
        return switch (observableEntity) {
            case ObservableConcept _ -> conceptFields();
            case ObservablePattern _ -> patternFields();
            case ObservableSemantic _ -> semanticFields();
            case ObservableStamp _ -> stampFields();
        };
    }

    public static FieldCategorySet fieldsForObservable(ObservableEntitySnapshot observableSnapshot) {
        return switch (observableSnapshot) {
            case ObservableConceptSnapshot _ -> conceptFields();
            case ObservablePatternSnapshot _ -> patternFields();
            case ObservableSemanticSnapshot _ -> semanticFields();
        };
    }

    public static FieldCategorySet fieldsForObservable(ObservableVersion observableVersion) {
        return switch (observableVersion) {
            case ObservableConceptVersion _ -> conceptVersionFields();
            case ObservableSemanticVersion _ -> semanticVersionFields();
            case ObservablePatternVersion _ -> patternVersionFields();
            case ObservableStampVersion _ -> stampVersionFields();
        };
    }
}
