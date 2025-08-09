package dev.ikm.komet.kview.events;

import dev.ikm.tinkar.events.Evt;
import dev.ikm.tinkar.events.EvtType;

public class AddStampEvent extends Evt {
    public AddStampEvent(Object source, EvtType eventType) {
        super(source, eventType);
    }
}