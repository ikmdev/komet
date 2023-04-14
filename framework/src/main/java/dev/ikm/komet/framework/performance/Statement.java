package dev.ikm.komet.framework.performance;

import org.eclipse.collections.api.factory.Lists;
import org.eclipse.collections.api.list.ImmutableList;

/**
 * a request or observation
 */
public interface Statement extends Performance {
    default ImmutableList<Object> authors() {
        return Lists.immutable.empty();
    }

}
