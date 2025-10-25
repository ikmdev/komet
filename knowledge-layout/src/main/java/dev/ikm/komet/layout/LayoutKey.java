package dev.ikm.komet.layout;

import dev.ikm.komet.framework.observable.FeatureKey;
import dev.ikm.komet.layout.area.AreaGridSettings;
import dev.ikm.tinkar.common.binary.*;
import dev.ikm.tinkar.common.util.uuid.UuidT5Generator;

import java.util.UUID;


/**
 * Represents a sealed interface that serves as a key for defining and managing layout structures.
 * It defines a set of nested sealed interfaces that specify different levels and properties
 * within the layout hierarchy. The {@code LayoutKey} is reproducible per the hierarhcy of the layout,
 * and can then be used to override the layout by providing a custom grid layout for a particular key.
 */
public sealed interface LayoutKey extends Encodable {
    UUID namespace = UuidT5Generator.get(LayoutKey.class.getName());
    int marshalVersion = Encodable.LATEST_VERSION;
    /**
     * Represents an empty {@code LayoutKey} instance.
     * This constant serves as a default or placeholder {@link LayoutKey} with no specific hierarchical
     * or contextual value attached to it, using a UUID of {@code "00000000-0000-0000-0000-000000000000"}.
     * <p>
     * It is a canonical representation for scenarios where a "null object" pattern is needed
     * or when no meaningful {@code LayoutKey} is applicable in a given context.
     */
    ForArea EMPTY = new LayoutKeyRecord(UUID.fromString("00000000-0000-0000-0000-000000000000"));
    UUID id();
    /**
     * Generates the first level key of a layout hierarchy using a specified root layout.
     *
     * @param rootLayout the root layout that defines the structure from which the first level key is derived
     * @return an instance of {@link ForArea} representing the first level key of the layout
     */
    static ForArea makeTopArea(Class<? extends KnowledgeLayout> rootLayoutClass) {
        return new LayoutKeyRecord(UuidT5Generator.get(namespace, rootLayoutClass.getName()));
    }

    /**
     * Creates a {@link ForArea} instance using the specified UUID as the unique identifier.
     *
     * @param topAreaUuid the {@link UUID} representing the unique identifier for the top area
     * @return a new instance of {@link ForArea} encapsulated in a {@link LayoutKeyRecord}
     */
    static ForArea makeTopArea(UUID topAreaUuid) {
        return new LayoutKeyRecord(topAreaUuid);
    }

    /**
     * Represents a sealed interface that defines the base behavior for a {@code LayoutKey} generation at a layout level.
     * A layout hierarchy can consist of multiple levels, and this interface provides the
     * fundamental operations to create property-based layout keys and transition to the next level
     * in the hierarchy.
     * <p>
     * This interface is a part of the {@code LayoutKey} hierarchy and permits the {@code LayoutKeyRecord}
     * implementation. It serves as a foundational building block for defining structured layout keys.
     */
    sealed interface ForArea extends LayoutKey permits LayoutKeyRecord {
        Property makePropertyLayoutKey(FeatureKey locator);

        Supplemental makeSupplementalLayoutKey(AreaGridSettings areaGridSettings);

        AreaKeyProvider makeAreaKeyProvider();
    }

    /**
     * Represents a sealed interface for generating the next level in a layout hierarchy.
     * This interface extends {@link LayoutKey} and is primarily used to compute
     * the transition or generation of a new {@code LayoutKey.ForArea} based on the current
     * context provided by a {@link LayoutComputer}.
     * Implementations are responsible for defining the mechanism to produce the next layout level,
     * ensuring continuity and consistency across the hierarchy.
     * <p>
     * The {@code LayoutComputer} only allows passing a {@code NextLevel} which must then generate a
     * new level, for use in its layout. This ensures a bipartite relationship between the {@code LayoutKey.Level}}
     * and the {@code LayoutKey.NextLevel} passed to the {@code LayoutComputer}.
     * <p>
     * This sealed interface permits the {@code LayoutKeyRecord} implementation, which
     * encapsulates the behavior associated with the creation of the next level
     * while adhering to the layout structure as defined in a hierarchical manner.
     */
    sealed interface AreaKeyProvider extends LayoutKey permits LayoutKeyRecord {
        ForArea make(LayoutComputer layoutComputer);
    }

    /**
     * Represents a sealed interface within the {@code LayoutKey} system, signifying a property-based
     * layout key in a structured layout hierarchy. The {@code Property} interface acts as a marker
     * for identifying specific property-related aspects of a layout configuration.
     * <p>
     * This interface is a part of the {@code LayoutKey} hierarchy and is intended to be implemented
     * only by permitted subclasses, such as {@code LayoutKeyRecord}. It plays a role in supporting
     * customization and extension of layout properties, providing a mechanism to define and structure
     * specific property keys within the layout architecture.
     * <p>
     * Permits:
     * - {@link LayoutKeyRecord}
     */
    sealed interface Property extends LayoutKey permits LayoutKeyRecord {
        ForArea forArea();
    }

    /**
     * Represents a supplemental key within the {@code LayoutKey} system for hierarchical layout structures that are
     * not based on a property locator. For these areas, the default computed grid layout is used to generate
     * a unique key for the layout area that can then be overridden using the key if desired.
     * The {@code Supplemental} interface is part of the {@code LayoutKey} hierarchy and provides additional
     * functionality or extended features that complement the primary layout configuration.
     * <p>
     * Implementations of this interface can define mechanisms to extend the layout structure by
     * incorporating supplemental layout keys, typically in scenarios where additional configurations
     * or specialized behaviors are required.
     * <p>
     * This sealed interface permits the {@code LayoutKeyRecord}, ensuring a consistent implementation
     * pattern across the layout system.
     */
    sealed interface Supplemental extends LayoutKey permits LayoutKeyRecord {
        ForArea forArea();
    }


    /**
     * A record implementation of the {@code LayoutKey} interface that encapsulates a {@link UUID}
     * as the unique identifier for defining hierarchical layout keys.
     * <p>
     * This record serves as the default concrete implementation and fulfills the contracts of the
     * nested {@code Level}, {@code Property}, and {@code NextLevel} sealed interfaces.
     * <p>
     * The {@code LayoutKeyRecord} provides mechanisms for:
     * <p> - Creating property-based layout keys at a given hierarchy level.
     * <p> - Transitioning to the next level in a layout hierarchy.
     * <p> - Computing a new layout level key based on a {@link LayoutComputer}'s context.
     * <p>
     * This implementation generates deterministic UUIDs for layout keys using the {@code UuidT5Generator},
     * ensuring consistency and reproducibility across the layout hierarchy.
     *
     * @param id The {@link UUID} representing the unique identifier for the layout key.
     */
    record LayoutKeyRecord(UUID id)
            implements ForArea, Property, AreaKeyProvider, Supplemental {
        @Override
        public Property makePropertyLayoutKey(FeatureKey locator) {
            return new LayoutKeyRecord(UuidT5Generator.get(namespace, locator.toString()));
        }

        @Override
        public Supplemental makeSupplementalLayoutKey(AreaGridSettings areaGridSettings) {
            return new LayoutKeyRecord(UuidT5Generator.get(namespace, areaGridSettings.withLayoutKeyForArea(EMPTY).toString()));
        }

        @Override
        public AreaKeyProvider makeAreaKeyProvider() {
            return this;
        }

        @Override
        public ForArea make(LayoutComputer layoutComputer) {
            return new LayoutKeyRecord(UuidT5Generator.get(
                    UuidT5Generator.get(namespace, id.toString()), layoutComputer.getClass().getName()));
        }

        @Encoder
        @Override
        public void encode(EncoderOutput out) {
            out.writeUuid(id);
        }

        @Decoder
        public static LayoutKeyRecord decode(DecoderInput in) {
            switch (Encodable.checkVersion(in)) {
                default:
                    return new LayoutKeyRecord(in.readUuid());
            }
        }

        @Override
        public ForArea forArea() {
            return this;
        }

        @Override
        public String toString() {
            return "LayoutKey{" +  id + '}';
        }
    }
}
