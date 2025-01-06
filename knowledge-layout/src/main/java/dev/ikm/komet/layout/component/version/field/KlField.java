package dev.ikm.komet.layout.component.version.field;

import dev.ikm.komet.framework.observable.ObservableField;
import dev.ikm.komet.layout.KlWidget;
import dev.ikm.tinkar.common.bind.ConceptClass;
import dev.ikm.tinkar.common.bind.annotations.names.RegularName;
import dev.ikm.tinkar.terms.ConceptFacade;
import dev.ikm.tinkar.terms.TinkarTerm;

/**
 * Represents an observable field in the Komet framework.
 *
 * This interface is parameterized with the type of the value. It extends the
 * KlWidget interface, providing a method to access the underlying field.
 *
 * @param <T> The type of the field's value.
 */
@RegularName("Semantic Field")
public sealed interface KlField<T> extends KlWidget, ConceptClass
        permits KlComponentField, KlComponentListField, KlComponentSetField,
        KlConceptField, KlDirectedGraphField, KlDirectedTreeField,
        KlFloatField, KlImageField, KlInstantField, KlIntegerField,
        KlLogicalExpressionField, KlPatternField, KlPointPlanarField,
        KlPointSpatialField, KlPublicIdField, KlSemanticField, KlStringField,
        KlUuidField, KlVertexField {
    ObservableField<T> field();


    /**
     * Retrieves the data type associated with the field represented by this observable field.
     * For example, TinkarTerm.INTEGER_FIELD.
     *
     * @return A {@code ConceptFacade} representing the data type of the field.
     *         By default, this method returns {@code TinkarTerm.INTEGER_FIELD}.
     */
    ConceptFacade fieldDataType();
}
