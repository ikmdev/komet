package dev.ikm.tinkar.common.bind.annotations.axioms;

import dev.ikm.tinkar.common.bind.ClassConceptBinding;

import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Repeatable;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * An annotation used to specify the parent concept for a class implementing the {@code ConceptClass} interface.
 * The parent concept is represented as another class that also implements {@code ConceptClass}, allowing
 * for hierarchical relationships to be defined between concepts or entities.
 *
 * <p>This annotation is designed to provide a way to organize and navigate conceptual hierarchies
 * in systems that model relationships between entities dynamically. It specifies the 'parent'
 * {@code ConceptClass} of the annotated type, enabling reflective operations at runtime.</p>
 *
 * <p><b>Usage:</b> This annotation is applied to classes, interfaces, or enums that implement
 * {@code ConceptClass}. By specifying the parent concept, it facilitates the construction of
 * conceptual hierarchies.</p>
 *
 * Example:
 * <pre>{@code
 * @ParentConcept(SuperConceptClass.class)
 * public class SubConceptClass implements ConceptClass {
 *     // Implementation here
 * }
 * }</pre>
 *
 * <p>The annotation supports runtime reflection, allowing the parent concept of an annotated
 * element to be retrieved dynamically during runtime.</p>
 *
 * <p><b>Metadata:</b>
 * <ul>
 *   <li><b>Retention:</b> {@link RetentionPolicy#RUNTIME} - The annotation is retained and accessible at runtime.</li>
 *   <li><b>Target:</b> {@link ElementType#TYPE} - The annotation can only be applied at the class, interface, or enum level.</li>
 *   <li><b>Documented:</b> This annotation is included in generated API documentation.</li>
 * </ul>
 * </p>
 *
 * @see ClassConceptBinding
 */
@Documented
@Retention(RetentionPolicy.RUNTIME)
@Target({ElementType.FIELD, ElementType.TYPE})
@Repeatable(ParentConcepts.class)
public @interface ParentConcept {
    /**
     * Retrieves the class type that extends the {@code ConceptClass} associated with
     * the annotated element. This class specifies the particular implementation of the concept.
     *
     * @return the parent {@code ConceptClass} object that this {@code ConceptClass} is
     * subsumed under.
     */
    Class<? extends ClassConceptBinding> value();
}
