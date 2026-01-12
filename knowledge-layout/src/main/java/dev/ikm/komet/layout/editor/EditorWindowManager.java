package dev.ikm.komet.layout.editor;

import dev.ikm.komet.layout.editor.model.EditorWindowModel;
import dev.ikm.komet.preferences.KometPreferences;
import dev.ikm.tinkar.coordinate.view.calculator.ViewCalculator;

import java.util.HashMap;

/**
 * Window Manager used to load window instances (each loaded window is a canonical reference).
 */
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

    /**
     * Saves the Window into KometPreferences (stored preferences).
     *
     * @param klEditorAppPreferences the stored preferences pointing to the kl editor app
     * @param editorWindowModel the EditorWindowModel to save to preferences
     */
    public static void save(KometPreferences klEditorAppPreferences, EditorWindowModel editorWindowModel) {
        editorWindowModel.save(klEditorAppPreferences);
        titleToWindowModel.put(editorWindowModel.getTitle(), editorWindowModel);
    }

    /**
     * Returns a reference to an already initialized Window.
     *
     * @param title the title of the Window
     */
    public static EditorWindowModel getWindowInstance(String title) {
        EditorWindowModel editorWindowModel = titleToWindowModel.get(title);
        if (editorWindowModel == null) {
            throw new RuntimeException("EditorWindowModel hasn't been initialized yet. Call loadWindowModel first.");
        }
        return editorWindowModel;
    }

    /**
     * Called when the Editor "app" gets closed. It should be used to clean up.
     */
    public static void shutdown() {
        titleToWindowModel.remove(EditorWindowModel.EMPTY_WINDOW_TITLE);
    }
}
