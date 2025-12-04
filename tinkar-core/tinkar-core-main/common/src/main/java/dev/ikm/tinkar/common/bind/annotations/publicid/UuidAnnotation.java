package dev.ikm.tinkar.common.bind.annotations.publicid;

import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Repeatable;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * An annotation used to associate a UUID with a type. It is primarily utilized
 * in conjunction with the {@code PublicIdAnnotation} to define public identifiers
 * for annotated classes. The UUID serves as a unique and persistent identifier
 * that facilitates consistent object identification within the framework.
 *
 * This annotation can be applied to classes and is repeatable. When multiple
 * UUIDs are associated with a single type, they can be specified with multiple
 * {@code UuidAnnotation} declarations or via the container annotation {@code PublicIdAnnotation}.
 *
 * The UUID specified in the {@code value} element should be in the standard UUID
 * string format (e.g., "123e4567-e89b-12d3-a456-426614174000").
 */
@Documented
@Retention(RetentionPolicy.RUNTIME)
@Target({ElementType.TYPE, ElementType.FIELD})
@Repeatable(PublicIdAnnotation.class)
public @interface UuidAnnotation {
    String value();
}
