package dev.ikm.komet.framework.observable;

import dev.ikm.tinkar.common.binary.*;

/**
 * The AssociatedComponentFieldElement class represents a field element associated with a specific component
 * within the observable field framework. This class is part of the field location system, implementing
 * the {@link FieldLocator} interface to facilitate field categorization and identification.
 * <p>
 * Each instance of this class associates a component, represented by its unique identifier (nid), with a
 * specific field category. The field category is defined by the {@link FieldCategory} enumeration,
 * which helps classify fields based on their context, such as component-related fields, version-specific
 * fields, or semantic fields.
 * <p>
 * This class is generally used to locate and manage fields tied to components, leveraging the framework's
 * field-based operation mechanisms. It contributes to simplifying field identification, retrieval, and
 * categorization within the observable field framework, ensuring consistent and organized handling of
 * field-related operations.
 *
 * @param associatedComponentNid the unique nid (native identifier) of the associated component
 * @param category the field category as specified by {@link FieldCategory}
 */
public record AssociatedComponentField(int associatedComponentNid, FieldCategory category)
        implements FieldLocatorForAssociatedComponent {

    @Encoder
    @Override
    public void subEncode(EncoderOutput out) {
        out.writeNid(associatedComponentNid);
        out.writeString(category.name());
    }

    @Decoder
    public static AssociatedComponentField decode(DecoderInput in) {
        int associatedComponentNid = in.readNid();
        FieldCategory category = FieldCategory.valueOf(in.readString());
        return new AssociatedComponentField(associatedComponentNid, category);
    }
}
