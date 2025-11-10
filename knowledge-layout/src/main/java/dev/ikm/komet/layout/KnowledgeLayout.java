package dev.ikm.komet.layout;

import dev.ikm.tinkar.common.service.SaveState;

/**
 * {@code KnowledgeLayout} implementations provide an encapsulated, customizable,
 * and restorable layout of knowledge components. The knowledge layout will be responsive
 * to the value of the {@code componentProperty()}.
 * <p>
 * When the value of the {@code componentProperty()} changes, the implementation is expected to:
 * <ol>
 *     <li>Retrieve the observable fields of that component</li>
 *     <li>Pass those observable fields to a {@code LayoutComputerFactory} via the
 *     {@code LayoutComputerFactory.layout(KlWidget widget, LayoutKey.NextLevel nextLevelKey,
 *     LayoutOverrides layoutOverrides)}, together with generate a list of {@code KlDynamicAreaFactory} items,
 *     which will then be used by the provider to create individual areas within the layout.</li>
 *     <li>The {@code LayoutComputerFactory} may inturn expand the observable fields by either processing
 *     list items, or adding new associated components to the layout, generating a layout tree of potentially
 *     multiple levels. </li>
 *     <li>Manage the LayoutKey appropriately, so that each level in the layout will have a unique key, and
 *     each observable field will in turn have a unique key within that level</li>
 *     <li>Utilize and update the {@code LayoutOverrides} within the layout, and its descendent levels</li>
 *     <li>Invoke the generated list of {@code KlDynamicAreaFactory} items, and install those areas within
 *     the appropriate parent area.</li>
 * </ol>
 */
public interface KnowledgeLayout extends SaveState {

    /**
     * Retrieves the {@code LayoutOverrides} instance associated with the knowledge layout.
     * The {@code LayoutOverrides} object provides mechanisms for customizing, serializing,
     * and restoring layout configurations for specific graph locations.
     *
     * @return the {@code LayoutOverrides} associated with the layout.
     */
    LayoutOverrides layoutOverrides();

    /**
     * Retrieves the root {@code LayoutKey.Level} of the layout hierarchy.
     * The root layout key serves as the initial level in the hierarchical structure
     * of layout keys, providing a reference point for defining and organizing
     * subsequent levels in the layout. A knowledge layout can have more than one root,
     * where overrides are derived from the root layout key.
     *
     * @return the root {@code LayoutKey.Level} key of the layout hierarchy.
     */
    LayoutKey.ForArea rootLayoutKey();

}
