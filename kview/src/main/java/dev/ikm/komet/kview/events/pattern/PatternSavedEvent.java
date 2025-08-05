package dev.ikm.komet.kview.events.pattern;

import dev.ikm.tinkar.events.Evt;
import dev.ikm.tinkar.events.EvtType;

public class PatternSavedEvent extends Evt {

    public static final EvtType<PatternSavedEvent> PATTERN_UPDATE_EVENT = new EvtType<>(Evt.ANY, "PATTERN_UPDATE_EVENT");

    public static final EvtType<PatternSavedEvent> PATTERN_CREATION_EVENT = new EvtType<>(Evt.ANY, "PATTERN_CREATION_EVENT");


    /**
     * Constructs a prototypical Event.
     *
     * @param source    the object on which the Event initially occurred
     * @param eventType
     * @throws IllegalArgumentException if source is null
     */
    public PatternSavedEvent(Object source, EvtType eventType) {
        super(source, eventType);
    }
}
