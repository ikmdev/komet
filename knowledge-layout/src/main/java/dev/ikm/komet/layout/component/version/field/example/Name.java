package dev.ikm.komet.layout.component.version.field.example;


import java.lang.annotation.*;

@Documented
@Retention(RetentionPolicy.RUNTIME)
@Target({ElementType.TYPE})
public @interface Name {
    String value();
}
