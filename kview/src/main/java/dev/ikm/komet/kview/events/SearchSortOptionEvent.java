package dev.ikm.komet.kview.events;

import dev.ikm.komet.framework.events.Evt;
import dev.ikm.komet.framework.events.EvtType;

public class SearchSortOptionEvent extends Evt {

    public static final EvtType<SearchSortOptionEvent> SORT_BY_COMPONENT = new EvtType<>(Evt.ANY, "SORT_BY_COMPONENT");

    public static final EvtType<SearchSortOptionEvent> SORT_BY_COMPONENT_ALPHA = new EvtType<>(Evt.ANY, "SORT_BY_COMPONENT_ALPHA");

    public static final EvtType<SearchSortOptionEvent> SORT_BY_SEMANTIC = new EvtType<>(Evt.ANY, "SORT_BY_SEMANTIC");

    public static final EvtType<SearchSortOptionEvent> SORT_BY_SEMANTIC_ALPHA = new EvtType<>(Evt.ANY, "SORT_BY_SEMANTIC_ALPHA");

    /**
     * Constructs a prototypical Event.
     *
     * @param source    the object on which the Event initially occurred
     * @param eventType
     * @throws IllegalArgumentException if source is null
     */
    public SearchSortOptionEvent(Object source, EvtType eventType) {
        super(source, eventType);
    }
}
