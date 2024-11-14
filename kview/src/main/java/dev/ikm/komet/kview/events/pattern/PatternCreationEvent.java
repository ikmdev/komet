package dev.ikm.komet.kview.events.pattern;

import dev.ikm.komet.framework.events.Evt;
import dev.ikm.komet.framework.events.EvtType;

public class PatternCreationEvent extends Evt {

    public static final EvtType<PatternCreationEvent> PATTERN_CREATION_EVENT = new EvtType<>(Evt.ANY, "PATTERN_CREATION_EVENT");

    /**
     * Constructs a prototypical Event.
     *
     * @param source    the object on which the Event initially occurred
     * @param eventType
     * @throws IllegalArgumentException if source is null
     */
    public PatternCreationEvent(Object source, EvtType eventType) {
        super(source, eventType);
    }
}
