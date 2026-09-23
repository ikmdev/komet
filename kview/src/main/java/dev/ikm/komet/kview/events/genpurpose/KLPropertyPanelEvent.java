package dev.ikm.komet.kview.events.genpurpose;

import dev.ikm.tinkar.events.Evt;
import dev.ikm.tinkar.events.EvtType;

/**
 * Asks a general-purpose KL window to open or close its properties panel. What the panel shows
 * is set directly on the panel (see {@code GenPurposePropertiesController}).
 */
public class KLPropertyPanelEvent extends Evt {
    public static final EvtType<KLPropertyPanelEvent> OPEN_PANEL = new EvtType<>(Evt.ANY, "OPEN_PANEL");

    public static final EvtType<KLPropertyPanelEvent> CLOSE_PANEL = new EvtType<>(Evt.ANY, "CLOSE_PANEL");

    public static final EvtType<KLPropertyPanelEvent> NO_SELECTION_MADE_PANEL = new EvtType<>(Evt.ANY, "NO_SELECTION_MADE_PANEL");

    /**
     * @param source        the object on which the Event initially occurred
     * @param eventType     type of the event
     */
    public KLPropertyPanelEvent(Object source, EvtType<KLPropertyPanelEvent> eventType) {
        super(source, eventType);
    }
}
