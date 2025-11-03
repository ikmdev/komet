package dev.ikm.komet.kview.events;

import dev.ikm.tinkar.events.Evt;
import dev.ikm.tinkar.events.EvtType;

public class KLEditorWindowCreatedEvent extends Evt {

    public static final EvtType<CreateJournalEvent> KL_EDITOR_WINDOW_CREATED = new EvtType<>(Evt.ANY, "CREATED");

    private String windowTitle;

    public KLEditorWindowCreatedEvent(Object source, EvtType<? extends Evt> evtType, String windowTitle) {
        super(source, evtType);
        this.windowTitle = windowTitle;
    }

    public String getWindowTitle() {
        return windowTitle;
    }
}