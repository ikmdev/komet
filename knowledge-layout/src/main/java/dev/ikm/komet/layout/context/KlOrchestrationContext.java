package dev.ikm.komet.layout.context;

import dev.ikm.komet.framework.view.ObservableView;
import dev.ikm.tinkar.common.id.PublicIdStringKey;
import javafx.scene.Node;

import java.util.Optional;

/**
 * Interface representing an orchestration context within an application layout.
 * It provides methods to fetch the context's unique identifier, name, optional graphic representation,
 * and view coordinate. Additionally, it supports hierarchy by offering a method to get the parent context.
 */
public interface KlOrchestrationContext {
    /**
    TODO: The generic in the PublicIdStringKey can be associated with a membership
    pattern... Enabling both java and terminology constraints to align.
     */
    PublicIdStringKey<KlOrchestrationContext> contextId();

    default String name() {
        return contextId().getString();
    }
    /*
    Graphic could also be associated with a concept via a pattern/semantic...
     */
    Optional<Node> graphic();

    ObservableView viewCoordinate();

    /**
     *
     * @return empty if the current context is the top context, otherwise return
     * the parent context.
     */
    Optional<KlOrchestrationContext> parentContext();
}
