package dev.ikm.tinkar.common.bind.annotations.names;

import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Repeatable;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Annotation to specify a collection of regular names associated with a Concept bound to a Java type.
 *
 * Names for concepts follow the SNOMED convention described here:
 * https://confluence.ihtsdotools.org/display/DOCECL/Appendix+C+-+Dialect+Aliases
 *
 * This annotation allows defining a set of descriptive names or labels
 * for a concept or entity that may serve as identifiers, aliases,
 * or commonly used terminologies. These names can represent a concept
 * semantically and be leveraged in reflection-based operations or user interfaces.
 *
 * Target: This annotation is applicable at the type level (e.g., classes or interfaces),
 * binding the regular names to the annotated type.
 *
 * Retention Policy: This annotation is retained at runtime, enabling runtime
 * access and processing of the names via reflection.
 *
 * Expected Use Case: Applied to classes or interfaces that represent a specific
 * concept, where the retrieval of its regular names is an integral part of its
 * semantic definition.
 *
 * The values provided must be valid strings and are represented as an array
 * of strings in the annotation's value().
 */
@Documented
@Retention(RetentionPolicy.RUNTIME)
@Target({ElementType.TYPE, ElementType.FIELD})
@Repeatable(RegularNames.class)
public @interface RegularName {

    /**
     * Specifies the text associated with the regular name.
     *
     * @return the text value as a string.
     */
    String value();

    /**
     * Specifies the language associated with the regular name.
     *
     * This represents the language code in ISO 639-1 format, allowing the regular name
     * to be contextually associated with a specific language.
     *
     * https://en.wikipedia.org/wiki/List_of_ISO_639_language_codes
     *
     * @return the language code as a string, defaulting to "en" (English) if not specified.
     *
     */
    String language() default "en";

    /**
     * Specifies the dialect associated with the regular name.
     *
     * This represents the dialect code in ISO 3166-1 format, which provides
     * additional geographical or regional context for the regular name.
     *
     * @return the dialect code as a string, defaulting to "US" if not specified.
     */
    String dialect() default "US";

    /**
     * Specifies the case significance of the associated regular name.
     *
     * The case significance determines whether the textual representation of the name
     * is case-sensitive or case-insensitive. This attribute follows the guidelines
     * outlined for case significance in SNOMED.
     *
     * https://confluence.ihtsdotools.org/display/DOCEG/Case+Significance
     *
     * @return the case significance value as a string, defaulting to "ci" (case-insensitive) if not specified.
     */
    String caseSignificance() default "ci";
}
