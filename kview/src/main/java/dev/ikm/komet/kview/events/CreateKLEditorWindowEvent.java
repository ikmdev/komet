package dev.ikm.komet.kview.events;

import dev.ikm.komet.framework.preferences.PrefX;
import dev.ikm.tinkar.events.Evt;
import dev.ikm.tinkar.events.EvtType;

public class CreateKLEditorWindowEvent extends Evt {

    public static final EvtType<CreateJournalEvent> CREATE_KL_WINDOW = new EvtType<>(Evt.ANY, "CREATE");

    private final PrefX klEditorWindowSettingsObjectMap;
    private final String windowToLoad;
    private final boolean standardWindow;

    public CreateKLEditorWindowEvent(Object source, EvtType<? extends Evt> evtType, PrefX klEditorWindowSettingsObjectMap, String windowToLoad) {
        this(source, evtType, klEditorWindowSettingsObjectMap, windowToLoad, false);
    }

    public CreateKLEditorWindowEvent(Object source, EvtType<? extends Evt> evtType, PrefX klEditorWindowSettingsObjectMap,
                                     String windowToLoad, boolean standardWindow) {
        super(source, evtType);

        this.klEditorWindowSettingsObjectMap = klEditorWindowSettingsObjectMap;
        this.windowToLoad = windowToLoad;
        this.standardWindow = standardWindow;
    }

    public PrefX getWindowSettingsObjectMap() {
        return klEditorWindowSettingsObjectMap;
    }

    public String getWindowToLoad() {
        return windowToLoad;
    }

    /**
     * Whether the window to load is a standard (application-provided) window, stored in the
     * standard-windows folder, rather than a user-created window from the user-windows folder.
     */
    public boolean isStandardWindow() {
        return standardWindow;
    }
}