package dev.ikm.komet.layout;

import dev.ikm.komet.framework.observable.FieldLocator;
import org.eclipse.collections.api.list.ImmutableList;
import org.eclipse.collections.api.map.ImmutableMap;

public interface GridLayoutForComponentFactory {
    ImmutableMap<FieldLocator, GridLayoutForComponent> create(ImmutableList<FieldLocator> componentFieldSpecifications);
}
