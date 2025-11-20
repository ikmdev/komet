package dev.ikm.komet.layout.editor;

import dev.ikm.komet.layout.editor.model.EditorWindowModel;
import dev.ikm.komet.preferences.KometPreferences;
import dev.ikm.tinkar.coordinate.view.calculator.ViewCalculator;

import java.util.HashMap;

public class EditorWindowManager {
    private static final HashMap<String, EditorWindowModel> titleToWindowModel = new HashMap<>();

    /**
     * Loads a EditorWindowModel from preferences. If the passed in title is null then a new blank WindowModel is created and returned.
     * If there is already one loaded window in the cache (in the HashMap) then that Window is returned.
     *
     * @param klEditorWindowPreferences the window preferences for the window with the given title.
     * @param viewCalculator the view calculator.
     * @param title the title of the window which is the identifier of the Window (there can't be two Windows with the same title).
     * @return the WindowModel loaded from preferences or a blank WindowModel if the passed in title is null.
     */
    public static EditorWindowModel loadWindowModel(KometPreferences klEditorWindowPreferences,
                                                        ViewCalculator viewCalculator,
                                                        String title) {
        EditorWindowModel editorWindowModel;
        String windowTitle = title;

        if (title == null) {
            editorWindowModel = new EditorWindowModel();
            windowTitle = editorWindowModel.getTitle();
        } else {
            editorWindowModel = titleToWindowModel.get(title);
            if (editorWindowModel == null) {
                editorWindowModel = EditorWindowModel.load(klEditorWindowPreferences, viewCalculator, title);
            }
        }

        titleToWindowModel.put(windowTitle, editorWindowModel);

        return editorWindowModel;
    }

    public static void save(KometPreferences klEditorAppPreferences, EditorWindowModel editorWindowModel) {
        editorWindowModel.save(klEditorAppPreferences);
        titleToWindowModel.put(editorWindowModel.getTitle(), editorWindowModel);
    }

    public static EditorWindowModel getWindowInstance(String title) {
        EditorWindowModel editorWindowModel = titleToWindowModel.get(title);
        if (editorWindowModel == null) {
            throw new RuntimeException("EditorWindowModel hasn't been initialized yet. Call loadWindowModel first.");
        }
        return editorWindowModel;
    }

    public static void shutdown() {
        titleToWindowModel.remove(EditorWindowModel.EMPTY_WINDOW_TITLE);
    }
}
