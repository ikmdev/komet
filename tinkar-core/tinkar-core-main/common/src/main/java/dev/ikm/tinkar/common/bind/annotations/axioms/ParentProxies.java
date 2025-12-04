package dev.ikm.tinkar.common.bind.annotations.axioms;

import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * A container annotation that aggregates multiple {@link ParentProxy} annotations.
 *
 * This annotation facilitates the definition of multiple logical parent relationships
 * for a type. The {@link ParentProxy} defines a single logical parent with its identifier;
 * {@code ParentProxies} allows grouping and managing multiple {@code ParentProxy} annotations
 * associated with a type.
 *
 * The retention policy ensures that this annotation is available at runtime for reflective
 * operations, enabling dynamic resolution of logical parent identities. This is particularly useful
 * in contexts where entities or concepts have complex hierarchical structures.
 *
 * Targeted for use on types (classes or interfaces), this annotation allows developers to
 * specify parent relationships in a centralized manner, providing a structured way to document
 * and manage parent hierarchy definitions.
 *
 * Metadata:
 * - Retention: {@link RetentionPolicy#RUNTIME}, making this annotation available at runtime.
 * - Target: {@link ElementType#TYPE}, specifying this annotation can only be applied to types.
 * - Documented: Included in the generated API documentation to improve readability and usage clarity.
 *
 * Usage:
 * - Provides a mechanism to specify multiple parent relationships for an annotated type using {@link ParentProxy}.
 * - Used typically in systems requiring dynamic and hierarchical management of entities or concepts.
 *
 * Example:
 * Instead of applying multiple {@link ParentProxy} annotations directly to a type, using {@code ParentProxies}
 * to bundle the parent associations ensures better organization and single-point maintenance of parent definitions.
 *
 * @see ParentProxy
 */
@Documented
@Retention(RetentionPolicy.RUNTIME)
@Target({ElementType.TYPE})
public @interface ParentProxies {
    ParentProxy[] value();
}
