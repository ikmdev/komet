package dev.ikm.komet.layout;

import dev.ikm.komet.framework.observable.FieldLocator;
import dev.ikm.komet.preferences.KometPreferences;
import dev.ikm.tinkar.coordinate.view.ViewCoordinate;
import org.eclipse.collections.api.list.ImmutableList;
import org.eclipse.collections.api.map.ImmutableMap;

/**
 * 1. **Scalable Component Interaction**:
 * Through tightly controlled parent-child context propagation,
 * we've established a scalable method for gadgets and subcomponents
 * to communicate, optimizing their layout within nested grid systems.
 */
public interface KlConstructionContextForGadget {

    /**
     * Only context providers should call this method to get a child view coordinate for its context. When called,
     * the new view coordinate becomes the parent coordinate for the context provided by {@code forFactory()},
     * when not called, the parent coordinate remains unchanged.
     * @param override
     * @return
     */
    ViewCoordinate viewCoordinate(boolean override);

    /**
     * Create a new child preference. Will throw an exception if called more than once.
     * @return
     */
    KometPreferences newPreferenceChild();

    /**
     * Create a GridLayout for the provided field specifications. The layout will assume
     * a starting position of Column = 0, Row = 0, and increment as appropriate for the layout
     * algorithm. Later calls will begin again at Column = 0, Row = 0, and can be used to lay out
     * embedded grids. Only gadgets that lay out subcomponents on a grid need to call this method.
     * @param componentFieldSpecifications
     * @return
     */
    ImmutableMap<FieldLocator, GridLayoutForComponent>
        createLayout(ImmutableList<FieldLocator> componentFieldSpecifications);

    /**
     * If the gadget calls factories to create subcomponents, this method provides
     * a child context with the proper configuration.
     * @return
     */
    KlConstructionContextForFactory forFactory();
}
