package dev.ikm.tinkar.common.bind.annotations.names;

import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Annotation to specify a collection of regular names associated with a concept bound to a Java type.
 *
 * This annotation allows defining an array of `RegularName` annotations, each describing a particular
 * name, language, dialect, and case significance for a concept. It is utilized when multiple regular
 * names should be associated with a single type.
 *
 * Target: This annotation is applicable at the type level (e.g., classes or interfaces),
 * allowing multiple `RegularName` definitions to be grouped and bound to the annotated type.
 *
 * Retention Policy: This annotation is retained at runtime, enabling runtime access and processing
 * of the associated regular names through reflection.
 *
 * Usage: This annotation is typically used to organize multiple descriptive names or labels
 * for a concept, enabling semantic representation and clarity in systems which may require
 * different naming conventions or languages.
 *
 * It serves as a container for multiple `RegularName` annotations and supports the repeatable
 * annotation feature by allowing multiple `@RegularName` declarations on the same type.
 */
@Documented
@Retention(RetentionPolicy.RUNTIME)
@Target({ElementType.TYPE, ElementType.FIELD})
public @interface RegularNames {
    RegularName[] value();
}
