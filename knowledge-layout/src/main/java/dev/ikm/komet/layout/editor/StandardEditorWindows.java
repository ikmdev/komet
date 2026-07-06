package dev.ikm.komet.layout.editor;

import dev.ikm.komet.layout.editor.model.EditorPatternModel;
import dev.ikm.komet.layout.editor.model.EditorWindowModel;
import dev.ikm.komet.preferences.KometPreferences;
import dev.ikm.tinkar.coordinate.view.calculator.ViewCalculator;
import dev.ikm.tinkar.terms.TinkarTerm;

import java.util.List;

import static dev.ikm.komet.preferences.KLEditorPreferences.KL_EDITOR_WINDOWS;

/**
 * Seeds the standard (application-provided) KL Windows into the standard-windows folder of the
 * KL Editor 'App' preferences. Standard windows are pre-designed layouts that ship with the
 * application, as opposed to the layouts the user designs in the KL Editor (which live in the
 * user-windows folder).
 */
public final class StandardEditorWindows {

    /** Title of the standard Concept window. */
    public static final String CONCEPT_WINDOW_2 = "Concept (2)";

    private StandardEditorWindows() {
    }

    /**
     * Creates any standard window definitions not yet present in the given standard-windows
     * preferences folder.
     *
     * @param standardWindowsPreferences the kl-editor-app/standard-windows preferences node
     * @param viewCalculator             the view calculator used to resolve the pattern definitions
     */
    public static void ensureStandardWindows(KometPreferences standardWindowsPreferences,
                                             ViewCalculator viewCalculator) {
        List<String> standardWindows = standardWindowsPreferences.getList(KL_EDITOR_WINDOWS);
        if (!standardWindows.contains(CONCEPT_WINDOW_2)) {
            saveConceptWindow2(standardWindowsPreferences, viewCalculator);
        }
    }

    /**
     * The standard Concept window: a single section containing the Description pattern, required
     * when the window is opened in the Journal in create mode.
     */
    private static void saveConceptWindow2(KometPreferences standardWindowsPreferences,
                                           ViewCalculator viewCalculator) {
        EditorWindowModel window = new EditorWindowModel();
        window.setTitle(CONCEPT_WINDOW_2);

        EditorPatternModel descriptionPattern =
                new EditorPatternModel(viewCalculator, TinkarTerm.DESCRIPTION_PATTERN.nid());
        descriptionPattern.setRequired(true);
        window.getMainSection().getPatterns().add(descriptionPattern);

        window.save(standardWindowsPreferences);
    }
}
