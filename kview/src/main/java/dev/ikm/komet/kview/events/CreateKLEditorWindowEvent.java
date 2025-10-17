package dev.ikm.komet.kview.events;

import dev.ikm.komet.framework.preferences.PrefX;
import dev.ikm.tinkar.events.Evt;
import dev.ikm.tinkar.events.EvtType;

public class CreateKLEditorWindowEvent extends Evt {

    public static final EvtType<CreateJournalEvent> CREATE_KL_EDITOR = new EvtType<>(Evt.ANY, "CREATE");

    private PrefX klEditorWindowSettingsObjectMap;

    public CreateKLEditorWindowEvent(Object source, EvtType<? extends Evt> evtType, PrefX klEditorWindowSettingsObjectMap) {
        super(source, evtType);
        this.klEditorWindowSettingsObjectMap = klEditorWindowSettingsObjectMap;
    }

    public PrefX getWindowSettingsObjectMap() {
        return klEditorWindowSettingsObjectMap;
    }

    public void setWindowSettingsObjectMap(PrefX klEditorWindowSettingsObjectMap) {
        this.klEditorWindowSettingsObjectMap = klEditorWindowSettingsObjectMap;
    }
}