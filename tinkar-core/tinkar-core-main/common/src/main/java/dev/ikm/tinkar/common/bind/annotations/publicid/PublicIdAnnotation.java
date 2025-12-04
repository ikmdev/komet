package dev.ikm.tinkar.common.bind.annotations.publicid;

import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * An annotation used to associate multiple {@code UuidAnnotation} definitions with a type.
 * It acts as a container annotation for {@code UuidAnnotation} instances, allowing a class
 * to be annotated with multiple UUIDs representing its public identifiers.
 *
 * This annotation is primarily used to define and manage public identifiers for classes
 * within the framework. By grouping UUIDs, it simplifies the process of associating
 * multiple identifiers with a single type, facilitating consistent and unique identification.
 *
 * The {@code value} element contains an array of {@code UuidAnnotation} instances,
 * each representing a UUID in standard string format.
 *
 * Usage of this annotation ensures standardized handling of public identifiers
 * across the system, making it easier to retrieve and work with the associated IDs.
 */
@Documented
@Retention(RetentionPolicy.RUNTIME)
@Target({ElementType.TYPE, ElementType.FIELD})
public @interface PublicIdAnnotation {
    UuidAnnotation[] value();
}
