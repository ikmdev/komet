package dev.ikm.komet.framework.observable;

import dev.ikm.tinkar.common.binary.Decoder;
import dev.ikm.tinkar.common.binary.DecoderInput;
import dev.ikm.tinkar.common.binary.Encoder;
import dev.ikm.tinkar.common.binary.EncoderOutput;

/**
 * Represents a field locator for an associated component list field within the observable field framework.
 * This class implements the {@link FieldLocator} interface and provides information about fields that
 * relate to a list of associated components. It is identified by the combination of an associated
 * component identifier, a field category, and an index in the list.
 * <p>
 * The associated component is represented by its numeric identifier (NID). Each list field is categorized
 * by a {@link FieldCategory}, which helps define the context of the field within the framework,
 * such as component fields, version-specific fields, or pattern fields.
 * <p>
 * This record is used to index and retrieving fields associated with specific
 * components and their lists in an observable field system, ensuring proper field organization and access.
 *
 * @param associatedComponentNid the identifier (NID) of the associated component
 * @param category the category of the field as defined by {@link FieldCategory}
 * @param index the index within the list of associated fields
 */
public record AssociatedComponentFieldListElement(int associatedComponentNid,
                                                  FieldCategory category,
                                                  int index) implements FieldLocatorForAssociatedComponent {
    @Encoder
    @Override
    public void subEncode(EncoderOutput out) {
        out.writeNid(associatedComponentNid);
        out.writeString(category.name());
        out.writeInt(index);
    }

    @Decoder
    public static AssociatedComponentFieldListElement decode(DecoderInput in) {
        int associatedComponentNid = in.readNid();
        FieldCategory category = FieldCategory.valueOf(in.readString());
        int index = in.readInt();
        return new AssociatedComponentFieldListElement(associatedComponentNid, category, index);
    }
}
