package dev.ikm.komet.layout.version.field;

import dev.ikm.komet.framework.observable.ObservableField;
import dev.ikm.komet.layout.KlArea;
import dev.ikm.tinkar.common.bind.ClassConceptBinding;
import dev.ikm.tinkar.common.bind.annotations.axioms.ParentProxy;
import dev.ikm.tinkar.common.bind.annotations.names.RegularName;
import dev.ikm.tinkar.common.bind.annotations.publicid.PublicIdAnnotation;
import dev.ikm.tinkar.common.bind.annotations.publicid.UuidAnnotation;
import javafx.scene.layout.Region;

/**
 * Represents an observable attribute in the Komet framework.
 * <p>
 * This interface is parameterized with the type of the value. It extends the
 * KlWidget interface, providing a method to access the underlying attribute.
 *
 * @param <DT> The data type of the attribute's value.
 */
@RegularName("Knowledge Layout Field")
@ParentProxy(parentName = "Komet panels (SOLOR)",
        parentPublicId = @PublicIdAnnotation(@UuidAnnotation("b3d1cdf6-27a5-502d-8f16-ed026a7b9d15")))
public non-sealed interface KlField<DT> extends KlArea<Region>, ClassConceptBinding {
    ObservableField<DT> field();
}
