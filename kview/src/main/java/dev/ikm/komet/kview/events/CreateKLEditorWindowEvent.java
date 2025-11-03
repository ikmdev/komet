package dev.ikm.komet.kview.events;

import dev.ikm.komet.framework.preferences.PrefX;
import dev.ikm.tinkar.events.Evt;
import dev.ikm.tinkar.events.EvtType;

import java.util.Optional;

public class CreateKLEditorWindowEvent extends Evt {

    public static final EvtType<CreateJournalEvent> CREATE_KL_WINDOW = new EvtType<>(Evt.ANY, "CREATE");

    private final PrefX klEditorWindowSettingsObjectMap;
    private final String windowToLoad;

    public CreateKLEditorWindowEvent(Object source, EvtType<? extends Evt> evtType, PrefX klEditorWindowSettingsObjectMap, String windowToLoad) {
        super(source, evtType);

        this.klEditorWindowSettingsObjectMap = klEditorWindowSettingsObjectMap;
        this.windowToLoad = windowToLoad;
    }

    public PrefX getWindowSettingsObjectMap() {
        return klEditorWindowSettingsObjectMap;
    }

    public Optional<String> getWindowToLoad() {
        return windowToLoad == null ? Optional.empty() : Optional.of(windowToLoad);
    }
}