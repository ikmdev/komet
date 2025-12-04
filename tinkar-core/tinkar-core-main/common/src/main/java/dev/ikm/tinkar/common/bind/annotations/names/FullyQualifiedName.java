package dev.ikm.tinkar.common.bind.annotations.names;

import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Repeatable;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Annotation to define the fully qualified name associated with a concept bound to a Java type.
 *
 * This annotation is used to specify a single fully qualified name, represented as
 * a string, that uniquely identifies the associated concept or entity within a system.
 * The fully qualified name is typically a descriptive name or identifier
 * defined in the context of the system.
 *
 * Target: This annotation is applied at type level (classes or interfaces),
 * associating the fully qualified name to the annotated type.
 *
 * Retention Policy: This annotation is retained at runtime, allowing
 * the fully qualified name to be accessed and processed using reflection-based methods.
 *
 * Usage: This annotation is generally used in systems where types need
 * to be associated or identified by their specific fully qualified names
 * for purposes such as dynamic type resolution, entity identification, or
 * mapping contextual definitions to Java types.
 */
@Documented
@Retention(RetentionPolicy.RUNTIME)
@Target({ElementType.TYPE, ElementType.FIELD})
@Repeatable(FullyQualifiedNames.class)
public @interface FullyQualifiedName {

    /**
     * Specifies the text associated with the fully qualified name.
     *
     * @return the text value as a string.
     */
    String value();

    /**
     * Specifies the language associated with the fully qualified name.
     *
     * This represents the language code in ISO 639-1 format, allowing the fully qualified name
     * to be contextually associated with a specific language.
     *
     * https://en.wikipedia.org/wiki/List_of_ISO_639_language_codes
     *
     * @return the language code as a string, defaulting to "en" (English) if not specified.
     *
     */
    String language() default "en";

    /**
     * Specifies the dialect associated with the fully qualified name.
     *
     * This represents the dialect code in ISO 3166-1 format, which provides
     * additional geographical or regional context for the fully qualified name.
     *
     * @return the dialect code as a string, defaulting to "US" if not specified.
     */
    String dialect() default "US";

    /**
     * Specifies the case significance of the associated fully qualified name.
     *
     * The case significance determines whether the textual representation of the name
     * is case-sensitive or case-insensitive. This attribute follows the guidelines
     * outlined for case significance in SNOMED.
     *
     * https://confluence.ihtsdotools.org/display/DOCEG/Case+Significance
     *
     * @return the case significance value as a string, defaulting to "ci" (case-insensitive) if not specified.
     */
    String caseSignificance() default "ci";}
