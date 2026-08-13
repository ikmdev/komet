package dev.ikm.komet.layout.editor;

import dev.ikm.komet.layout.KlPatternSemanticsFactories;
import dev.ikm.komet.layout.PatternDefinitionSeeder;
import dev.ikm.komet.layout.PatternDefinitionTerms;
import dev.ikm.komet.layout.editor.model.EditorPatternModel;
import dev.ikm.komet.layout.editor.model.EditorSectionModel;
import dev.ikm.komet.layout.editor.model.EditorWindowModel;
import dev.ikm.komet.layout.editor.model.EditorWindowType;
import dev.ikm.komet.preferences.KometPreferences;
import dev.ikm.tinkar.common.service.RemoteConceptSearchService;
import dev.ikm.tinkar.common.service.ServiceLifecycleManager;
import dev.ikm.tinkar.coordinate.view.calculator.ViewCalculator;
import dev.ikm.tinkar.terms.EntityFacade;
import dev.ikm.tinkar.terms.TinkarTerm;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;
import java.util.prefs.BackingStoreException;

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

    /** Title of the standard Pattern window. */
    public static final String PATTERN_WINDOW_2 = "Pattern (2)";

    /**
     * Version of the standard window definitions authored below. Bump this whenever a standard
     * window's definition changes: seeded definitions carrying an older version are removed and
     * re-seeded from the current code, so application-shipped windows never go stale in the
     * preferences. User-authored windows live in the user-windows folder and are untouched.
     */
    private static final int CURRENT_STANDARD_WINDOWS_VERSION = 2;

    /** Preferences key holding the version the seeded standard windows were created from. */
    private static final String STANDARD_WINDOWS_VERSION_KEY = "STANDARD-WINDOWS-VERSION";

    private StandardEditorWindows() {
    }

    /**
     * Creates any standard window definitions not yet present in the given standard-windows
     * preferences folder. Definitions seeded by an older application version
     * ({@link #CURRENT_STANDARD_WINDOWS_VERSION}) are removed first and re-seeded from the current code.
     * This runs before every standard-window load (journal load and window summoning), so a
     * removed definition is always re-created before anything reads it.
     *
     * @param standardWindowsPreferences the kl-editor-app/standard-windows preferences node
     * @param viewCalculator             the view calculator used to resolve the pattern definitions
     */
    public static void ensureStandardWindows(KometPreferences standardWindowsPreferences,
                                             ViewCalculator viewCalculator) {
        // The standard Pattern window is composed from the pattern-definition patterns, so make
        // sure they exist in the data store before any window definition is created from them.
        PatternDefinitionSeeder.ensureSeeded(viewCalculator);

        if (standardWindowsPreferences.getInt(STANDARD_WINDOWS_VERSION_KEY, 0) != CURRENT_STANDARD_WINDOWS_VERSION) {
            removeStandardWindows(standardWindowsPreferences);
            standardWindowsPreferences.putInt(STANDARD_WINDOWS_VERSION_KEY, CURRENT_STANDARD_WINDOWS_VERSION);
        }

        List<String> standardWindows = standardWindowsPreferences.getList(KL_EDITOR_WINDOWS);
        if (!standardWindows.contains(CONCEPT_WINDOW_2)) {
            saveConceptWindow2(standardWindowsPreferences, viewCalculator);
        }
        if (!standardWindows.contains(PATTERN_WINDOW_2)) {
            savePatternWindow2(standardWindowsPreferences, viewCalculator);
        }
    }

    /**
     * Removes every seeded standard window definition (their window nodes and the window list),
     * so the seeding in {@link #ensureStandardWindows} re-creates them from the current code.
     */
    private static void removeStandardWindows(KometPreferences standardWindowsPreferences) {
        for (String windowTitle : standardWindowsPreferences.getList(KL_EDITOR_WINDOWS)) {
            try {
                standardWindowsPreferences.node(windowTitle).removeNode();
            } catch (BackingStoreException e) {
                LOG.warn("Failed to remove outdated standard window '{}': {}", windowTitle, e.getMessage());
            }
        }
        standardWindowsPreferences.putList(KL_EDITOR_WINDOWS, List.of());
    }

    /**
     * The standard Concept window: a main section containing the Description pattern, plus an
     * "Axiom" section with the Inferred definition pattern on top and the Stated definition
     * pattern below it. The Description and Stated definition patterns are required when the
     * window is opened in the Journal in create mode.
     */
    private static void saveConceptWindow2(KometPreferences standardWindowsPreferences,
                                           ViewCalculator viewCalculator) {
        EditorWindowModel window = new EditorWindowModel();
        window.setTitle(CONCEPT_WINDOW_2);
        window.setWindowType(EditorWindowType.STANDARD_CONCEPT);
        window.setTimelineVisible(true);

        ensureLocallyResolvable(viewCalculator, TinkarTerm.DESCRIPTION_PATTERN);

        EditorPatternModel descriptionPattern =
                new EditorPatternModel(viewCalculator, TinkarTerm.DESCRIPTION_PATTERN.nid());
        descriptionPattern.setRequired(true);
        window.getMainSection().getPatterns().add(descriptionPattern);

        EditorSectionModel axiomSection = new EditorSectionModel();
        axiomSection.setName("Axiom");

        EditorPatternModel inferredDefinitionPattern = new EditorPatternModel(viewCalculator,
                TinkarTerm.EL_PLUS_PLUS_INFERRED_AXIOMS_PATTERN.nid());
        EditorPatternModel statedDefinitionPattern = new EditorPatternModel(viewCalculator,
                TinkarTerm.EL_PLUS_PLUS_STATED_AXIOMS_PATTERN.nid());
        // Required like in the classic concept window: the concept can only be created once its
        // stated definition has a necessary or sufficient set (the required check is
        // definition-aware for this pattern, not just semantic-existence).
        statedDefinitionPattern.setRequired(true);
        statedDefinitionPattern.setRowIndex(1);
        axiomSection.getPatterns().addAll(inferredDefinitionPattern, statedDefinitionPattern);

        window.getAdditionalSections().add(axiomSection);

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

    /**
     * The standard Pattern window: the pattern is defined through the pattern-definition patterns —
     * a "Pattern Definition" section with the Meaning and Purpose pattern, a "Description" section
     * with the Description pattern, and a "Fields" section with the Fields pattern shown as a table
     * (one row per field of the pattern being defined). The Meaning and Purpose and Description
     * patterns are required when the window is opened in the Journal in create mode; Fields is not
     * (a membership pattern has no fields). Unlike the Concept window it keeps the default grey
     * chrome (see the concept-window-theme rules in kview.css, applied only to
     * {@link EditorWindowType#STANDARD_CONCEPT}).
     */
    private static void savePatternWindow2(KometPreferences standardWindowsPreferences,
                                           ViewCalculator viewCalculator) {
        EditorWindowModel window = new EditorWindowModel();
        window.setTitle(PATTERN_WINDOW_2);
        window.setWindowType(EditorWindowType.STANDARD_PATTERN);
        window.setTimelineVisible(true);

        EditorSectionModel definitionSection = window.getMainSection();
        definitionSection.setName("Pattern Definition");
        EditorPatternModel meaningAndPurposePattern = new EditorPatternModel(viewCalculator,
                PatternDefinitionTerms.MEANING_AND_PURPOSE_PATTERN.nid());
        meaningAndPurposePattern.setRequired(true);
        definitionSection.getPatterns().add(meaningAndPurposePattern);

        EditorSectionModel descriptionSection = new EditorSectionModel();
        descriptionSection.setName("Description");
        EditorPatternModel descriptionPattern =
                new EditorPatternModel(viewCalculator, TinkarTerm.DESCRIPTION_PATTERN.nid());
        descriptionPattern.setRequired(true);
        descriptionSection.getPatterns().add(descriptionPattern);
        window.getAdditionalSections().add(descriptionSection);

        EditorSectionModel fieldsSection = new EditorSectionModel();
        fieldsSection.setName("Fields");
        EditorPatternModel fieldsPattern = new EditorPatternModel(viewCalculator,
                PatternDefinitionTerms.FIELDS_PATTERN.nid());
        KlPatternSemanticsFactories.byClassName(KlPatternSemanticsFactories.TABLE_FACTORY_CLASS_NAME)
                .ifPresent(fieldsPattern::setFactory);
        fieldsSection.getPatterns().add(fieldsPattern);
        window.getAdditionalSections().add(fieldsSection);

        window.save(standardWindowsPreferences);
    }
}
