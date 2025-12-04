package dev.ikm.tinkar.common.bind;

import dev.ikm.tinkar.common.bind.annotations.axioms.ParentConcept;
import dev.ikm.tinkar.common.bind.annotations.axioms.ParentConcepts;
import dev.ikm.tinkar.common.bind.annotations.axioms.ParentProxies;
import dev.ikm.tinkar.common.bind.annotations.axioms.ParentProxy;
import dev.ikm.tinkar.common.bind.annotations.names.FullyQualifiedName;
import dev.ikm.tinkar.common.bind.annotations.names.FullyQualifiedNames;
import dev.ikm.tinkar.common.bind.annotations.names.RegularName;
import dev.ikm.tinkar.common.bind.annotations.names.RegularNames;
import dev.ikm.tinkar.common.bind.annotations.publicid.PublicIdAnnotation;
import dev.ikm.tinkar.common.bind.annotations.publicid.UuidAnnotation;
import dev.ikm.tinkar.common.id.PublicId;
import dev.ikm.tinkar.common.id.PublicIds;
import dev.ikm.tinkar.common.util.uuid.UuidT5Generator;
import org.eclipse.collections.api.factory.Lists;
import org.eclipse.collections.api.list.ImmutableList;
import org.eclipse.collections.api.list.MutableList;

import java.lang.annotation.Annotation;
import java.util.UUID;
import java.util.function.Supplier;

/**
 * The {@code ClassConceptBinding} class serves as an interface providing utility methods for associating
 * and managing {@link PublicId}, fully qualified names, and regular names in classes and enums representing
 * conceptual entities or relationships. This interface plays a critical role in organizing and standardizing
 * the use of UUIDs, names, and hierarchical parent relationships based on annotations.
 *
 * Key functionalities include:
 * - Generating or retrieving {@link PublicId} values for implementing classes, enums, or fields, either
 *   through provided annotations or by creating default values based on predefined namespaces and unique naming conventions.
 * - Processing hierarchical relationships through parent-child mappings derived from annotations like
 *   {@link ParentProxies} and {@link ParentConcepts}.
 * - Extracting and generating regular names and fully qualified names tied to the implementing class or enum constants.
 *
 * Classes and enums implementing this interface can leverage these utility methods to ensure consistency and immutability
 * in managing annotations such as {@link PublicIdAnnotation}, {@link ParentProxies}, {@link ParentConcepts},
 * {@link FullyQualifiedNames}, and {@link RegularNames}.
 *
 * The interface supports both default and static methods, facilitating flexible combinations of operations
 * on annotations and class data while maintaining immutability in the resulting lists and identifiers.
 */
public interface ClassConceptBinding {
    UUID uuidNameSpace = UUID.fromString("6cb071e7-661f-4694-8c3b-3176bcfd443f");

    /**
     * Retrieves the {@code PublicId} associated with the implementing class of the {@code ConceptClassBinding} interface.
     * The {@code PublicId} is determined using the class-level {@code PublicIdAnnotation} if present.
     * If no annotation is found, a default UUID is generated based on this interface's uuidNameSpace and
     * the implementation class's class name.
     *
     * @return the {@code PublicId} corresponding to the implementing class
     */
    default PublicId publicId() {
        if (this instanceof Enum) {
            return publicId((Enum<?>) this);
        }
        return publicId(this.getClass());
    }

    /**
     * Retrieves an immutable list of {@link PublicId} objects representing the logical parents of the implementing class.
     * The method collects parent identifiers through annotations declared on the class, specifically {@link ParentProxies}
     * and {@link ParentConcepts}. These annotations provide hierarchical information about the entity or concept the
     * class represents.
     *
     * For {@link ParentProxies}, the method processes each {@link ParentProxy} annotation to extract UUIDs using the
     * associated {@link PublicIdAnnotation}, and creates a {@link PublicId} for each logical parent.
     *
     * Similarly, for {@link ParentConcepts}, information about the annotated parent {@link ClassConceptBinding} is gathered.
     * If the parent class is annotated with {@link PublicIdAnnotation}, the corresponding {@link PublicId} is constructed
     * from the UUIDs provided in the annotation.
     *
     * @return an {@link ImmutableList} of {@link PublicId} objects representing the logical parents
     */
    default ImmutableList<PublicId> parents() {
        if (this instanceof Enum enumField) {
            return parents(getAnnotation(enumField, ParentProxies.class),
                    getAnnotation(enumField, ParentConcepts.class));
        }
        return parents(this.getClass().getAnnotation(ParentProxies.class),
                this.getClass().getAnnotation(ParentConcepts.class));
    }
   /**
    * Retrieves an immutable list of {@link PublicId} objects representing the logical parents
    * specified by the provided {@link ParentProxies} and {@link ParentConcepts}.
    *
    * The method processes the following:
    * - {@link ParentProxies}: Extracts UUIDs from each {@link ParentProxy} using its
    * associated {@link PublicIdAnnotation}, and constructs a {@link PublicId} for each logical parent.
    * - {@link ParentConcepts}: Gathers parent class information annotated with {@link PublicIdAnnotation},
    * extracts associated UUIDs, and constructs a {@link PublicId} for each logical parent class.
    *
    * @param parentProxies the {@link ParentProxies} annotation containing the proxies to logical parents,
    *                      each represented by a {@link ParentProxy}.
    * @param parentConcepts the {@link ParentConcepts} annotation containing the parent class information
    *                       annotated with {@link PublicIdAnnotation}.
    * @return an {@link ImmutableList} of {@link PublicId} objects representing the logical parents.
    */
   static ImmutableList<PublicId> parents(ParentProxies parentProxies, ParentConcepts parentConcepts) {
        MutableList<PublicId> parents = Lists.mutable.empty();

         for (ParentProxy parentProxy: parentProxies.value()) {
            MutableList<UUID> parentUuids = Lists.mutable.empty();
            PublicIdAnnotation publicUuidAnnotation = parentProxy.parentPublicId();
            for (UuidAnnotation uuidAnnotation : publicUuidAnnotation.value()) {
                parentUuids.add(UUID.fromString(uuidAnnotation.value()));
            }
            parents.add(PublicIds.of(parentUuids));
        }
        for (ParentConcept parentConcept: parentConcepts.value()) {
            MutableList<UUID> parentUuids = Lists.mutable.empty();
            Class<? extends ClassConceptBinding> parentClass = parentConcept.value();
            PublicIdAnnotation publicUuidAnnotation = parentClass.getAnnotation(PublicIdAnnotation.class);
            if (publicUuidAnnotation != null) {
                for (UuidAnnotation uuidAnnotation : publicUuidAnnotation.value()) {
                    parentUuids.add(UUID.fromString(uuidAnnotation.value()));
                }
            }
        }
        return parents.toImmutable();
    }


    /**
     * Retrieves an immutable list of fully qualified names associated with the implementing class
     * or Enum field of the current instance.
     *
     * For Enum fields, the method retrieves the associated {@link FullyQualifiedNames} annotation
     * of the Enum constant and extracts its values. If the annotation is not present or does not
     * provide values, the method constructs a default fully qualified name based on the regular
     * names of the Enum and its declaring class's simple name.
     *
     * For other types, the method retrieves the {@link FullyQualifiedNames} annotation of the
     * class itself and extracts its values. If the annotation is not present or does not
     * provide values, the method constructs a default fully qualified name based on the regular
     * names and the class's package name.
     *
     * @return an {@code ImmutableList<String>} containing the fully qualified names associated
     *         with the current instance or a default name if no fully qualified names are specified.
     */
    default ImmutableList<String> fullyQualifiedNames() {
        if (this instanceof Enum enumField) {
            return fullyQualifiedNames(this, enumField.toString(), getAnnotation(enumField, FullyQualifiedNames.class));
        }
        return fullyQualifiedNames(this, this.getClass().getSimpleName(), this.getClass().getAnnotation(FullyQualifiedNames.class));
    }

    /**
     * Retrieves an immutable list of fully qualified names based on the given parameters.
     *
     * The method processes the {@link FullyQualifiedNames} annotation to extract the values
     * of fully qualified names. If no values are found in the annotation, default fully qualified
     * names are constructed using the instance's class or Enum context. For Enum fields, the
     * fully qualified names are constructed using the regular names and the declaring class.
     * For other types, the regular names are used alongside the package name of the instance's class.
     *
     * @param theInstance the instance of {@code ClassConceptBinding} from which regular names
     *        or contextual information are derived when fully qualified names are not provided
     * @param simpleName the simple name to assist in constructing default fully qualified names
     *        when necessary
     * @param fullyQualifiedNamesAnnotation the annotation containing fully qualified names
     *        to be extracted; if absent or empty, default names are generated
     * @return an {@code ImmutableList<String>} containing the fully qualified names, either from
     *         the annotation or constructed as default names based on the context
     */
    static ImmutableList<String> fullyQualifiedNames(ClassConceptBinding theInstance, String simpleName, FullyQualifiedNames fullyQualifiedNamesAnnotation) {
        MutableList<String> fullyQualifiedNames = Lists.mutable.empty();
        if (fullyQualifiedNamesAnnotation != null && fullyQualifiedNamesAnnotation.value() != null) {
            for (FullyQualifiedName fullyQualifiedName: fullyQualifiedNamesAnnotation.value()) {
                fullyQualifiedNames.add(fullyQualifiedName.value());
            }
        }
        if (fullyQualifiedNames.isEmpty()) {
            ImmutableList<String> regularNames = theInstance.regularNames();
            if (theInstance instanceof Enum enumField) {
                fullyQualifiedNames.add(regularNames.getAny() + " in " + enumField.getDeclaringClass().getSimpleName());
            } else {
                fullyQualifiedNames.add(regularNames.getAny() + " in " + theInstance.getClass().getPackageName());
            }
        }
        return fullyQualifiedNames.toImmutable();
    }


    /**
     * Retrieves an immutable list of regular names associated with the implementing class.
     * The method evaluates the {@link RegularNames} annotation, if present, on the class.
     * Each {@link RegularName} annotation within the {@link RegularNames} annotation is processed
     * to extract its value and add it to the list of regular names.
     *
     * If the implementing class does not have a {@link RegularNames} annotation or if the list of
     * names extracted from the annotation is empty, the method generates a default regular name
     * by converting the class's simple name from camelCase to a space-separated format.
     *
     * @return an immutable list of regular names (`ImmutableList<String>`) representing descriptive
     * labels or names for the implementing class.
     */
    default ImmutableList<String> regularNames() {
        if (this instanceof Enum enumField) {
            return regularNames(getAnnotation(enumField, RegularNames.class), enumField.toString());
        }
        return regularNames(this.getClass().getAnnotation(RegularNames.class), this.getClass().getSimpleName());
    }

    /**
     * Extracts and returns a list of regular names from the provided annotation. If no regular
     * names are present, it falls back to generating a name based on the supplied simpleName.
     *
     * @param regularNamesAnnotation The annotation containing an array of regular name values.
     * @param simpleName The simple name used as a fallback for generating a regular name if
     *                   no names are found in the annotation.
     * @return An immutable list of regular names, either derived from the annotation or from
     *         the simpleName if the annotation is empty.
     */
    static ImmutableList<String> regularNames(RegularNames regularNamesAnnotation, String simpleName) {
        MutableList<String> regularNames = Lists.mutable.empty();
        if (regularNamesAnnotation != null && regularNamesAnnotation.value() != null) {
            for (RegularName regularName: regularNamesAnnotation.value()) {
                regularNames.add(regularName.value());
            }
        }
        if (regularNames.isEmpty()) {
            regularNames.add(camelCaseToWords(simpleName));
        }
        return regularNames.toImmutable();
    }

    /**
     * Retrieves a {@code PublicId} for the specified {@code ClassConceptBinding}.
     * The method checks if the class is annotated with {@code PublicIdAnnotation}.
     * If the annotation is present, the UUIDs defined in the annotation are used to create the {@code PublicId}.
     * If the annotation is absent, a new {@code PublicId} is generated by combining a predefined namespace
     * with the class's name using the {@code UuidT5Generator}.
     *
     * @param classConceptBinding the class implementing {@code ClassConceptBinding} whose {@code PublicId} is to be retrieved
     * @return the corresponding {@code PublicId} for the given {@code ClassConceptBinding}
     */
    static PublicId publicId(Class<? extends ClassConceptBinding> classConceptBinding) {
        return publicId(classConceptBinding.getName(),
                () -> classConceptBinding.getAnnotation(PublicIdAnnotation.class));
    }

    /**
     * Generates a {@code PublicId} for a given {@code Enum} constant. The method checks if the
     * {@code Enum} constant is annotated with {@code PublicIdAnnotation}, and extracts UUIDs
     * from it. If the annotation is not present or no UUIDs are provided, a default UUID is
     * generated using a namespace and the fully qualified name of the {@code Enum} constant.
     *
     * @param conceptEnum the {@code Enum} constant for which the {@code PublicId} is to be generated
     * @return the {@code PublicId} associated with the given {@code Enum} constant
     */
    static PublicId publicId(Enum<?> conceptEnum) {
            return publicId(conceptEnum.getDeclaringClass().getName() + "." + conceptEnum.name(),
                    () -> getAnnotation(conceptEnum, PublicIdAnnotation.class));
    }

    /**
     * Retrieves the specified annotation from a given Enum constant.
     *
     * @param <T>            the type of the annotation to retrieve
     * @param conceptEnum    the Enum constant from which the annotation is to be retrieved
     * @param annotationClass the class object of the annotation to retrieve
     * @return the annotation of the specified type if present, or throws a RuntimeException if not found due to reflection issues
     * @throws RuntimeException if the Enum constant does not have a corresponding field or a reflection error occurs
     */
    static <T extends Annotation> T getAnnotation(Enum<?> conceptEnum, Class<T> annotationClass) {
        try {
            return conceptEnum.getClass().getField(conceptEnum.name()).getAnnotation(annotationClass);
        } catch (NoSuchFieldException e) {
            throw new RuntimeException(e);
        }
    }

    /**
     * Generates a {@code PublicId} for a given unique name and a supplier of {@code PublicIdAnnotation}.
     * The method uses the UUIDs provided by the supplied {@code PublicIdAnnotation} if available.
     * If no UUIDs are provided or the annotation is absent, a default UUID is generated
     * using a predefined namespace and the unique name.
     *
     * @param uniqueName                the unique name for which the {@code PublicId} is to be generated
     * @param publicIdAnnotationSupplier the supplier that provides a {@code PublicIdAnnotation} containing UUIDs if present
     * @return the {@code PublicId} generated from the provided UUIDs or a default UUID based on the unique name
     */
    static PublicId publicId(String uniqueName, Supplier<PublicIdAnnotation> publicIdAnnotationSupplier) {
        MutableList<UUID> uuids = Lists.mutable.empty();
        PublicIdAnnotation publicIdAnnotation = publicIdAnnotationSupplier.get();
        if (publicIdAnnotation != null && publicIdAnnotation.value() != null) {
            for (UuidAnnotation uuidAnnotation : publicIdAnnotation.value()) {
                uuids.add(UUID.fromString(uuidAnnotation.value()));
            }
        }
        if (uuids.isEmpty()) {
            uuids.add(UuidT5Generator.get(uuidNameSpace, uniqueName));
        }
        return PublicIds.of(uuids);
    }


    /**
     * Converts a given camelCase string into a space-separated string,
     * where each word starts with the lowercase form of the corresponding uppercase letters in the original string.
     *
     * @param camelCaseString the input string in camelCase format
     * @return the converted string with words separated by spaces
     */
    static String camelCaseToWords(String camelCaseString) {
        StringBuilder result = new StringBuilder();
        for (int i = 0; i < camelCaseString.length(); i++) {
            char ch = camelCaseString.charAt(i);

            if (Character.isUpperCase(ch)) {
                if (i > 0) {
                    result.append(" ");
                }
                result.append(Character.toLowerCase(ch));
            } else {
                result.append(ch);
            }
        }
        return result.toString();
    }

}
