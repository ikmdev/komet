package dev.ikm.komet.framework.observable;

import dev.ikm.tinkar.common.binary.Decoder;
import dev.ikm.tinkar.common.binary.DecoderInput;
import dev.ikm.tinkar.common.binary.Encoder;
import dev.ikm.tinkar.common.binary.EncoderOutput;

/**
 * Represents a specific field element associated with a component in the observable field framework.
 * This class implements the {@link FieldLocator} interface and provides a way to locate fields by their category.
 * <p>
 * The category of this field element is defined by a {@link FieldCategory}, which classifies
 * and organizes fields based on their context within the framework. Each category corresponds to a
 * specific type of field, such as fields related to components, versions, patterns, semantics, or stamps.
 * <p>
 * This class is used to facilitate the identification and handling of fields within the observable
 * component framework, ensuring consistency and clear field categorization in various contexts.
 * <p>
 * It is part of the field location system within the observable field framework and contributes to
 * simplifying field-based operations like retrieval and categorization.
 *
 * @param category the category of the field as defined by {@link FieldCategory}
 */
public record ComponentFieldLocator(FieldCategory category) implements FieldLocatorForComponent {
    @Encoder
    @Override
    public void subEncode(EncoderOutput out) {
        out.writeString(category.name());
    }

    @Decoder
    public static ComponentFieldLocator decode(DecoderInput in) {
        FieldCategory category = FieldCategory.valueOf(in.readString());
        return new ComponentFieldLocator(category);
    }
}
