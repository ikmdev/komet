package dev.ikm.komet.kview.events;

import dev.ikm.komet.framework.events.Evt;
import dev.ikm.komet.framework.events.EvtType;
import dev.ikm.komet.framework.view.ViewProperties;
import dev.ikm.tinkar.common.id.PublicId;

public class AddStampEvent extends Evt {
    public AddStampEvent(Object source, EvtType eventType) {
        super(source, eventType);
    }
}