package dev.ikm.komet.layout.selection;

import org.eclipse.collections.api.list.ImmutableList;

public interface Selection {
    ImmutableList<ComponentSelection<? extends VersionSelection>> elements();
}
