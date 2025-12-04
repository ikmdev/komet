package dev.ikm.tinkar.common.bind.annotations.names;

import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Annotation to define a collection of fully qualified names associated with a concept
 * bound to a Java type.
 *
 * This annotation provides a mechanism to specify multiple {@link FullyQualifiedName} annotations,
 * each representing a fully qualified name that identifies the associated concept or entity
 * within a system. It acts as a container for the {@link FullyQualifiedName} annotations,
 * enabling the association of multiple fully qualified names with a single type.
 *
 * Target: This annotation is applied at the type level (classes or interfaces).
 *
 * Retention Policy: This annotation is retained at runtime, allowing the fully qualified names
 * to be accessed and processed using reflection.
 *
 * Usage: Used in scenarios where multiple fully qualified names need to be associated with
 * a specific concept or type, such as in systems involving complex hierarchies, multilingual
 * identifiers, or dynamic type resolution.
 *
 * The {@code value()} method holds an array of {@link FullyQualifiedName} annotations that define
 * the fully qualified names.
 */
@Documented
@Retention(RetentionPolicy.RUNTIME)
@Target({ElementType.TYPE, ElementType.FIELD})
public @interface FullyQualifiedNames {
    FullyQualifiedName[] value();
}
