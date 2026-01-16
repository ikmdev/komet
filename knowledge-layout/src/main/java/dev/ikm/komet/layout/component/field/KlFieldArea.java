package dev.ikm.komet.layout.component.field;

import dev.ikm.komet.framework.observable.ObservableField;
import dev.ikm.komet.layout.KlWidget;
import dev.ikm.tinkar.common.bind.ClassConceptBinding;
import dev.ikm.tinkar.common.bind.annotations.axioms.ParentProxy;
import dev.ikm.tinkar.common.bind.annotations.names.FullyQualifiedName;
import dev.ikm.tinkar.common.bind.annotations.names.RegularName;
import dev.ikm.tinkar.common.bind.annotations.publicid.PublicIdAnnotation;
import dev.ikm.tinkar.common.bind.annotations.publicid.UuidAnnotation;
import javafx.scene.layout.Region;

/**
 * Represents a pane within the Knowledge Layout framework that is used to manage
 * and interact with fields associated with various data types. This interface
 * serves as a base structure for field panes supporting observation and manipulation
 * of field values through the use of the {@code ObservableField} abstraction.
 *
 * This sealed interface defines the contract for all specific types of field panes
 * within the framework, including those managing boolean values, concept entities,
 * generic data, or component fields. It allows for type-safe interactions with the
 * corresponding field values and provides support for parent node association.
 *
 * @param <DT> the data type of the value held and managed within this field pane
 * @param <FX> the FX component type of the parent UI element associated with the pane
 */
@FullyQualifiedName("Knowledge Layout field pane")
@RegularName("Field pane")
@ParentProxy(parentName = "Komet panels (SOLOR)",
        parentPublicId = @PublicIdAnnotation(@UuidAnnotation("b3d1cdf6-27a5-502d-8f16-ed026a7b9d15")))
public sealed interface KlFieldArea<DT, FX extends Region> extends KlWidget<FX>, ClassConceptBinding
        permits KlBooleanFieldArea, KlComponentFieldArea, KlConceptFieldArea, KlGenericFieldArea, KlListFieldArea {
    /**
     * Retrieves the value associated with the field pane by accessing the value
     * of the underlying {@code ObservableField} instance.
     *
     * @return the value of type {@code T} managed by the associated {@code ObservableField}
     */
    default DT fieldValue() {
        return getField().value();
    }

    /**
     * Sets the {@code ObservableField} for this field pane.
     *
     * @param field the {@code ObservableField} instance to be associated with this field pane.
     *              It provides the value and observable properties for this field.
     */
    void setField(ObservableField<DT> field);

    /**
     * Retrieves the {@code ObservableField} instance associated with this field pane.
     * The {@code ObservableField} provides access to the field's value and supports
     * observation of property changes.
     *
     * @return the {@code ObservableField} instance managing the field's data and properties
     */
    ObservableField<DT> getField();

}
