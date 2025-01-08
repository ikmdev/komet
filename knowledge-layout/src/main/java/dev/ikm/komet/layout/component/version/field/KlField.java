package dev.ikm.komet.layout.component.version.field;

import dev.ikm.komet.framework.observable.ObservableField;
import dev.ikm.komet.layout.KlWidget;
import dev.ikm.tinkar.common.bind.ConceptClass;
import dev.ikm.tinkar.common.bind.annotations.axioms.ParentProxy;
import dev.ikm.tinkar.common.bind.annotations.names.RegularName;
import dev.ikm.tinkar.common.bind.annotations.publicid.PublicIdAnnotation;
import dev.ikm.tinkar.common.bind.annotations.publicid.UuidAnnotation;

import java.util.UUID;

/**
 * Represents an observable field in the Komet framework.
 *
 * This interface is parameterized with the type of the value. It extends the
 * KlWidget interface, providing a method to access the underlying field.
 *
 * @param <T> The type of the field's value.
 */
@RegularName("Knowledge Layout Field")
@ParentProxy(parentName = "Komet panels (SOLOR)",
        parentPublicId = @PublicIdAnnotation(@UuidAnnotation("b3d1cdf6-27a5-502d-8f16-ed026a7b9d15")))
public interface KlField<T> extends KlWidget, ConceptClass {
    ObservableField<T> field();
}
