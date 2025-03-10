package dev.ikm.komet.layout.component.field;

import javafx.scene.layout.Region;

/**
 * Represents a pane in the Knowledge Layout framework that is specifically designed
 * to manage and interact with fields of type {@code Boolean}. This interface extends
 * {@link KlFieldArea}, parameterized with {@code Boolean}, and provides mechanisms
 * for handling boolean field values within the framework.
 * <p>
 * This interface serves as a specialization of {@code KlFieldPane<Boolean, FX>}
 * to support boolean fields, allowing for observation and interaction
 * with the field's value and properties in layouts.
 *
 * @param <FX> the type of the parent node associated with this pane
 */
public non-sealed interface KlBooleanFieldArea<FX extends Region> extends KlFieldArea<Boolean, FX> {

}
