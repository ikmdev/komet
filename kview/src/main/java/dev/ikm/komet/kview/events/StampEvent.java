package dev.ikm.komet.kview.events;

import dev.ikm.tinkar.events.Evt;
import dev.ikm.tinkar.events.EvtType;

public class StampEvent extends Evt {
    public static final EvtType<CreateConceptEvent> CREATE_STAMP = new EvtType<>(Evt.ANY, "CREATE_STAMP");
    public static final EvtType<CreateConceptEvent> ADD_STAMP = new EvtType<>(Evt.ANY, "ADD_STAMP");

    public StampEvent(Object source, EvtType eventType) {
        super(source, eventType);
    }
}