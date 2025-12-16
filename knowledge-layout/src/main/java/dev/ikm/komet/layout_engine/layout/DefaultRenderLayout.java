package dev.ikm.komet.layout_engine.layout;

import dev.ikm.komet.layout.KnowledgeLayout;
import dev.ikm.komet.layout.LayoutKey;
import dev.ikm.komet.layout.LayoutOverrides;

import java.util.UUID;
import java.util.concurrent.CompletableFuture;

public class DefaultRenderLayout implements KnowledgeLayout {
     final LayoutKey.ForArea layoutKey;
     final LayoutOverrides overrides;

    public DefaultRenderLayout(UUID layoutId, LayoutOverrides overrides) {
        this.layoutKey = LayoutKey.makeTopArea(layoutId);
        this.overrides = overrides;
    }

    @Override
    public LayoutOverrides layoutOverrides() {
        return overrides;
    }

    @Override
    public LayoutKey.ForArea rootLayoutKey() {
        return layoutKey;
    }

    @Override
    public CompletableFuture<Void> save() {
        overrides.save();
        return CompletableFuture.completedFuture(null);

    }
}
