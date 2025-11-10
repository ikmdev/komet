package dev.ikm.komet.layout;

import dev.ikm.komet.framework.observable.Feature;
import dev.ikm.komet.layout.area.AreaGridSettings;
import org.eclipse.collections.api.list.ImmutableList;

import java.util.Optional;

/**
 * The LayoutComputer interface defines the contract for generating and managing
 * layout configurations dynamically. It facilitates the creation of layout structures
 * based on specified attributes and layout customization parameters.
 * Implementations are responsible for translating attribute locators and layout
 * overrides into a collection of dynamic area factories that define the layout
 * composition and behavior.
 */
public interface LayoutComputer {
    /**
     * Represents a layout element in the layout computation process. A layout element
     * consists of settings for grid management and an optional specific feature that
     * defines additional characteristics of the layout.
     *
     * Instances of this record are utilized within the layout computation system to
     * dynamically construct and configure layout compositions.
     *
     * Fields:
     * - {@code areaGridSettings}: Contains grid-related settings and configurations
     *   associated with this layout element.
     * - {@code optionalFeature}: Encapsulates an optional feature to provide additional
     *   properties or behaviors to the layout element.
     */
    record LayoutElement(AreaGridSettings areaGridSettings, Optional<Feature> optionalFeature) {
        public LayoutElement(AreaGridSettings areaGridSettings, Feature feature) {
            this(areaGridSettings, Optional.ofNullable(feature));
        }
        public LayoutElement(AreaGridSettings areaGridSettings) {
            this(areaGridSettings, Optional.empty());
        }
    }

    /**
     * All layout computers perform the layout within an overarching {@code KnowledgeLayout}.
     * This {@code KnowledgeLayout} provides necessary contextual information for layout such as
     * the {@code LayoutOverrides} and {@code LayoutKey} instances.
     *
     * @return the overarching {@code KnowledgeLayout} for this computer.
     */
    KnowledgeLayout masterLayout();

    /**
     * Retrieves the {@code LayoutOverrides} instance for this layout computer.
     * The {@code LayoutOverrides} object encapsulates mechanisms to customize, serialize,
     * and restore layout configurations for specific graph locations within the layout context.
     *
     * @return the {@code LayoutOverrides} instance associated with the layout context of this computer.
     */
    default LayoutOverrides layoutOverrides() {
        return masterLayout().layoutOverrides();
    }


    /**
     * Creates a list of area factories based on provided locators, layout customization,
     * and hierarchical layout configurations.
     *
     * @param features        a list of locators used to identify and specify properties for creating area factories
     * @param areaKeyProvider the hierarchical key identifying the next layout level configuration to be used
     * @return an immutable list of {@code LayoutElement} instances representing the layout components.
     */
    ImmutableList<LayoutElement> layout(ImmutableList<? extends Feature> features,
                                        LayoutKey.AreaKeyProvider areaKeyProvider);
}
