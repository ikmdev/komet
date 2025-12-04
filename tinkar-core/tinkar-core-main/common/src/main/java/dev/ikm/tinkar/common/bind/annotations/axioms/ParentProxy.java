package dev.ikm.tinkar.common.bind.annotations.axioms;

import dev.ikm.tinkar.common.bind.annotations.publicid.PublicIdAnnotation;

import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Repeatable;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Annotation for a ConceptClass to specify the PublicIds of its logical parents. This is used
 * to assign logical parents that are not java ConceptClass types, but are managed externally
 * to the java module, and have available PublicIds.
 *
 * This annotation allows defining a collection of parent identifiers, represented as an array of strings,
 * that establish relationships or hierarchies between entities or concepts. The IDs typically
 * correspond to the UUIDs of the parent entities associated with the annotated type.
 *
 * Target: This annotation is applied at the type level (classes or interfaces).
 *
 * Retention Policy: This annotation is retained at runtime, enabling access to the parent
 * identifiers via reflection during runtime operations.
 *
 * Usage: This annotation is utilized to associate the annotated type with its hierarchical
 * parent concepts or entities in systems where relationships between entities are defined
 * or resolved dynamically.
 */
@Documented
@Retention(RetentionPolicy.RUNTIME)
@Target({ElementType.TYPE})
@Repeatable(ParentProxies.class)
public @interface ParentProxy {
    /**
     * Specifies a string value that represents the name of the logical parent in the context of this
     * annotation. The value is typically used to establish a relationship or hierarchy for the annotated type.
     *
     * @return a string representing the logical parent identifier for the annotated entity
     */
    String parentName();

    /**
     * Retrieves the {@code PublicIdAnnotation} associated with the logical parent
     * specified by this {@code ParentProxy} annotation. The {@code PublicIdAnnotation}
     * encapsulates an array of {@code UuidAnnotation} definitions, which represent
     * the public identifiers (UUIDs) for the parent entity or concept.
     *
     * @return the {@code PublicIdAnnotation} representing the public identifiers of the parent
     */
    PublicIdAnnotation parentPublicId();
}
