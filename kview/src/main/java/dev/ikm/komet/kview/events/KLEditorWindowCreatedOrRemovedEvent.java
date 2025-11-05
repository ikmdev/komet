package dev.ikm.komet.kview.events;

import dev.ikm.tinkar.events.Evt;
import dev.ikm.tinkar.events.EvtType;

public class KLEditorWindowCreatedOrRemovedEvent extends Evt {

    public static final EvtType<CreateJournalEvent> KL_EDITOR_WINDOW_CREATED = new EvtType<>(Evt.ANY, "CREATED");
    public static final EvtType<CreateJournalEvent> KL_EDITOR_WINDOW_REMOVED = new EvtType<>(Evt.ANY, "REMOVED");

    private String windowTitle;

    public KLEditorWindowCreatedOrRemovedEvent(Object source, EvtType<? extends Evt> evtType, String windowTitle) {
        super(source, evtType);
        this.windowTitle = windowTitle;
    }

    public String getWindowTitle() {
        return windowTitle;
    }
}