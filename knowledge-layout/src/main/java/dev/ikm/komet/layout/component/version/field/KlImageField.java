package dev.ikm.komet.layout.component.version.field;

import dev.ikm.tinkar.common.bind.annotations.axioms.ParentConcept;
import dev.ikm.tinkar.common.bind.annotations.names.RegularName;
import javafx.scene.image.Image;

/**
 * Represents a field that holds an Image value.
 *
 * This interface extends KlField parameterized with an Image type.
 */
@RegularName("Image Field")
@ParentConcept(KlField.class)
public interface KlImageField extends KlField<Image> {
}
