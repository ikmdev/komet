package dev.ikm.komet.layout.editor.property;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Optional hint applied to a {@code xxxProperty()} accessor inside a {@link KlPropertySet}.
 *
 * <p>A {@link KlPropertySet} is discovered purely by reflection — every public, no-argument
 * method ending in {@code Property} that returns a JavaFX {@link javafx.beans.property.Property}
 * becomes an editable property. By default the label shown in the editor is derived from the
 * method name ("headerVisible" → "Header Visible") and ordering is alphabetical. This annotation
 * is the escape hatch for the cases reflection can't express on its own: a custom label, or
 * restricting a property to a fixed set of choices.
 *
 * <p>It is entirely optional — a factory author can declare a {@link KlPropertySet} with no
 * annotations at all and everything still works.
 *
 * <p>It may be placed either on the {@code xxxProperty()} accessor or on the backing field of the
 * same name (e.g. {@code headerVisible} for {@code headerVisibleProperty()}). When both are
 * annotated, the accessor's annotation wins.
 */
@Retention(RetentionPolicy.RUNTIME)
@Target({ElementType.METHOD, ElementType.FIELD})
public @interface KlProperty {

    /**
     * The label shown next to this property's editor control. When empty (the default) the label
     * is derived from the property's name.
     */
    String label() default "";

    /**
     * Restricts an {@code int}-typed property to this fixed set of values. When non-empty the editor
     * renders a drop-down of these choices instead of a free spinner. Applies to {@code Integer}
     * properties only.
     */
    int[] intChoices() default {};
}
