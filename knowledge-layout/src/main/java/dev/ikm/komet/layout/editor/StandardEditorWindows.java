package dev.ikm.komet.layout.editor;

import dev.ikm.komet.layout.editor.model.EditorPatternModel;
import dev.ikm.komet.layout.editor.model.EditorWindowModel;
import dev.ikm.komet.preferences.KometPreferences;
import dev.ikm.tinkar.common.service.RemoteConceptSearchService;
import dev.ikm.tinkar.common.service.ServiceLifecycleManager;
import dev.ikm.tinkar.coordinate.view.calculator.ViewCalculator;
import dev.ikm.tinkar.terms.EntityFacade;
import dev.ikm.tinkar.terms.TinkarTerm;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;

import static dev.ikm.komet.preferences.KLEditorPreferences.KL_EDITOR_WINDOWS;

/**
 * Seeds the standard (application-provided) KL Windows into the standard-windows folder of the
 * KL Editor 'App' preferences. Standard windows are pre-designed layouts that ship with the
 * application, as opposed to the layouts the user designs in the KL Editor (which live in the
 * user-windows folder).
 */
public final class StandardEditorWindows {

    private static final Logger LOG = LoggerFactory.getLogger(StandardEditorWindows.class);

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

        ensureLocallyResolvable(viewCalculator, TinkarTerm.DESCRIPTION_PATTERN);

        EditorPatternModel descriptionPattern =
                new EditorPatternModel(viewCalculator, TinkarTerm.DESCRIPTION_PATTERN.nid());
        descriptionPattern.setRequired(true);
        window.getMainSection().getPatterns().add(descriptionPattern);

        window.save(standardWindowsPreferences);
    }

    /**
     * If {@code concept} has no local description text — e.g. a remote-backed provider whose
     * local entity store starts empty and only loads entities on demand — fetches its full
     * entity graph from the active {@link RemoteConceptSearchService} so its name resolves
     * normally afterward. No-op when the concept already resolves locally, or when no remote
     * search service is active (plain local providers always have core TinkarTerm concepts
     * loaded from starter data).
     */
    private static void ensureLocallyResolvable(ViewCalculator viewCalculator, EntityFacade concept) {
        if (viewCalculator.getRegularDescriptionText(concept).isPresent()
                || viewCalculator.getFullyQualifiedNameText(concept).isPresent()) {
            return;
        }
        ServiceLifecycleManager.get().getRunningService(RemoteConceptSearchService.class)
                .ifPresent(remote -> {
                    try {
                        remote.loadConceptWithSemantics(concept.publicId().asUuidList().toList());
                    } catch (Exception e) {
                        LOG.warn("Failed to load {} from remote backend: {}", concept, e.getMessage());
                    }
                });
    }
}
