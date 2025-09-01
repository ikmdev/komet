package dev.ikm.komet.kview.events;

import dev.ikm.tinkar.events.Evt;
import dev.ikm.tinkar.events.EvtType;

public class CreateStampEvent extends Evt {
    public CreateStampEvent(Object source, EvtType eventType) {
        super(source, eventType);
    }
}