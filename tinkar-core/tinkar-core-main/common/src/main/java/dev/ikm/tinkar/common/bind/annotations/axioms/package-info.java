/**
 * Contains annotations for defining hierarchical relationships, metadata, and logical structure
 * between concepts or entities within the Tinkar framework.
 *
 * <p>This package provides a set of annotations to facilitate the modeling of parent-child
 * relationships, conceptual hierarchies, and other metadata-driven connections for
 * classes that implement or represent conceptual models. These annotations are useful in systems
 * that require dynamic resolution of concepts and their relationships at runtime.</p>
 *
 * <p><b>Main Features:</b>
 * <ul>
 *   <li>Define parent-child structures in conceptual hierarchies.</li>
 *   <li>Manage UUID-based identifiers for parent entities.</li>
 *   <li>Enable runtime reflection to retrieve relationships and metadata dynamically.</li>
 * </ul>
 * </p>
 *
 * <p>The annotations in this package are designed to support the creation and management
 * of logical axioms, metadata, and hierarchical structures within conceptual models.</p>
 *
 * <p><b>Use {@code ParentConcept} when referencing a parent represented as a ConceptClass:</b>
 * <pre>{@code
 *  @ParentConcept(SuperConceptClass.class)
 *  public class SubConceptClass implements ConceptClass {
 *      // Implementation here
 *  }
 *  }</pre>
 *
 *
 * <p><b>Use {@code ParentProxy} when referencing a parent represented externally:</b>
 * <pre>{@code
 *  @ParentProxy(parentName = "Super concept",
 *         parentPublicId = @PublicIdAnnotation({
 *              @UuidAnnotation("f37f7b64-6e78-4375-a773-6e22bd2a1fe1")
 *         }))
 *  public class SubConceptClass implements ConceptClass {
 *      // Implementation here
 *  }
 *  }</pre>
 *
 *
 *
 * <p><b>Typical Use Cases:</b>
 * <ul>
 *   <li>Create complex hierarchies of concepts with runtime visibility.</li>
 *   <li>Define unique identifiers (e.g., UUIDs) for parent relationships.</li>
 *   <li>Associate metadata or logical relationships with conceptual classes.</li>
 * </ul>
 * </p>
 *
 * <p>This package is an integral part of the Tinkar framework's ability to handle dynamic
 * conceptual modeling and relationship management.</p>
 */
package dev.ikm.tinkar.common.bind.annotations.axioms;