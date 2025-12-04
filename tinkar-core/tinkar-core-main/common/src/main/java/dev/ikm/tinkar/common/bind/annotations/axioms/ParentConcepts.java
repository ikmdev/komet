package dev.ikm.tinkar.common.bind.annotations.axioms;

import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * An annotation that allows aggregation of multiple {@link ParentConcept} annotations on a single class, interface, or enum.
 * This supports the concept of multiple parent relationships in hierarchical or conceptual structures.
 *
 * This annotation facilitates defining and organizing multiple parent concepts for
 * entities or classes that implement the {@code ConceptClass} interface.
 * It allows runtime querying and navigation of the annotated types and their associated parent concepts.
 *
 * Metadata:
 * - Retention: {@link RetentionPolicy#RUNTIME} - The annotation is retained and accessible at runtime for reflective operations.
 * - Target: {@link ElementType#TYPE} - This annotation is applicable only at the class, interface, or enum level.
 * - Documented: This annotation is included in the generated API documentation.
 *
 * @see ParentConcept
 */
@Documented
@Retention(RetentionPolicy.RUNTIME)
@Target({ElementType.TYPE})
public @interface ParentConcepts {
    ParentConcept[] value();
}
