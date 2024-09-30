package dev.ikm.komet.kview.events.reasoner;

import dev.ikm.komet.framework.events.Evt;
import dev.ikm.komet.framework.events.EvtType;

public class CloseReasonerPanelEvent extends Evt {

    public static final EvtType<CloseReasonerPanelEvent> CLOSE = new EvtType<>(Evt.ANY, "CLOSE");

    /**
     * Constructs a prototypical Event.
     *
     * @param source    the object on which the Event initially occurred
     * @param eventType
     * @throws IllegalArgumentException if source is null
     */
    public CloseReasonerPanelEvent(Object source, EvtType eventType) {
        super(source, eventType);
    }
}
