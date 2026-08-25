package dev.ikm.komet.layout.editor;

import dev.ikm.komet.layout.KlPatternSemanticsFactories;
import dev.ikm.komet.layout.PatternDefinitionSeeder;
import dev.ikm.komet.layout.PatternDefinitionTerms;
import dev.ikm.komet.layout.editor.model.EditorPatternModel;
import dev.ikm.komet.layout.editor.model.EditorPatternRequirement;
import dev.ikm.komet.layout.editor.model.EditorPatternSemanticFilter;
import dev.ikm.komet.layout.editor.model.EditorSectionModel;
import dev.ikm.komet.layout.editor.model.EditorWindowModel;
import dev.ikm.komet.layout.editor.model.EditorWindowType;
import dev.ikm.komet.layout.editor.property.StandardPatternProperties;
import dev.ikm.komet.preferences.KometPreferences;
import dev.ikm.tinkar.common.service.RemoteConceptSearchService;
import dev.ikm.tinkar.common.service.ServiceLifecycleManager;
import dev.ikm.tinkar.coordinate.view.calculator.ViewCalculator;
import dev.ikm.tinkar.terms.EntityFacade;
import dev.ikm.tinkar.terms.EntityProxy;
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

    /** Name of the section holding the Description pattern, in both standard windows. */
    private static final String DESCRIPTION_SECTION_NAME = "Description";

    /**
     * Version of the standard window definitions authored below. Bump this whenever a standard
     * window's definition changes: seeded definitions carrying an older version are removed and
     * re-seeded from the current code, so application-shipped windows never go stale in the
     * preferences. User-authored windows live in the user-windows folder and are untouched.
     */
    private static final int CURRENT_STANDARD_WINDOWS_VERSION = 8;

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
     * The standard Concept window: a main "Description" section laying the Description pattern out in
     * two rows — fully qualified names and other names side by side, definitions spanning both
     * columns below them (see
     * {@link #populateConceptDescriptionSection}) — plus an "Axiom" section with the Inferred
     * definition pattern on top and the Stated definition pattern below it. The fully qualified name
     * column and the Stated definition pattern are required when the window is opened in the Journal
     * in create mode.
     */
    private static void saveConceptWindow2(KometPreferences standardWindowsPreferences,
                                           ViewCalculator viewCalculator) {
        EditorWindowModel window = new EditorWindowModel();
        window.setTitle(CONCEPT_WINDOW_2);
        window.setWindowType(EditorWindowType.STANDARD_CONCEPT);
        window.setTimelineVisible(true);

        // Description
        populateConceptDescriptionSection(viewCalculator, window.getMainSection());

        // Axiom
        EditorSectionModel axiomSection = createAxiomSection(viewCalculator);
        window.getAdditionalSections().add(axiomSection);

        window.save(standardWindowsPreferences);
    }

    /**
     * Turns the separator between consecutive semantics off for every pattern in the section. Applied
     * to the section in one pass rather than pattern by pattern, so a pattern added to it later comes
     * without separators too.
     *
     * <p>Separators are a Standard-factory property ({@link StandardPatternProperties}), so a pattern
     * shown through another factory — the Table factory lays its semantics out as table rows — has no
     * such property and is left alone.
     */
    private static void hideSemanticSeparators(EditorSectionModel section) {
        for (EditorPatternModel pattern : section.getPatterns()) {
            if (pattern.getFactoryProperties() instanceof StandardPatternProperties standardProperties) {
                standardProperties.setSeparatorVisible(false);
            }
        }
    }

    /**
     * Lays the passed in section out as the standard Concept window's Description section: the
     * Description pattern placed once per description type over two rows — fully qualified names and
     * other names side by side on the first row, definitions spanning both columns on the row below —
     * each placement showing only the descriptions of its own type (see
     * {@link EditorPatternSemanticFilter}) under its own title, and only their text (the language,
     * case significance and description type of every row would repeat what the placement already says).
     * No placement separates its descriptions with a line (see {@link #hideSemanticSeparators}).
     *
     * <p>The fully qualified name placement is the required one, refined so the concept can only be
     * created once it has a fully qualified name — the same requirement the section carried when it
     * held a single unfiltered Description pattern.
     */
    private static void populateConceptDescriptionSection(ViewCalculator viewCalculator,
                                                          EditorSectionModel descriptionSection) {
        ensureLocallyResolvable(viewCalculator, TinkarTerm.DESCRIPTION_PATTERN);

        // Named here rather than left to the section's auto-naming, which would take the name from the
        // authored title of the first pattern placed in it ("Fully qualified names:").
        descriptionSection.setName(DESCRIPTION_SECTION_NAME);

        descriptionSection.setNumberColumns(2);

        // FQN
        EditorPatternModel fullyQualifiedNames = createDescriptionColumn(viewCalculator,
                "Fully qualified names:", TinkarTerm.FULLY_QUALIFIED_NAME_DESCRIPTION_TYPE, 0);
        fullyQualifiedNames.setRequired(true);
        requireFullyQualifiedName(fullyQualifiedNames, viewCalculator);

        // Other name
        EditorPatternModel otherNames = createDescriptionColumn(viewCalculator,
                "Other names:", TinkarTerm.REGULAR_NAME_DESCRIPTION_TYPE, 1);

        // Definition
        EditorPatternModel definitions = createDescriptionColumn(viewCalculator,
                "Definition:", TinkarTerm.DEFINITION_DESCRIPTION_TYPE, 0);
        definitions.setRowIndex(1);
        definitions.setColumnSpan(2);

        descriptionSection.getPatterns().addAll(fullyQualifiedNames, otherNames, definitions);

        hideSemanticSeparators(descriptionSection);
    }

    /**
     * One column of the Description section: the Description pattern placed in the passed in column,
     * titled, showing only the descriptions holding the passed in description type, and of those only
     * their text.
     */
    private static EditorPatternModel createDescriptionColumn(ViewCalculator viewCalculator, String title,
                                                              EntityProxy.Concept descriptionType,
                                                              int columnIndex) {
        EditorPatternModel descriptionColumn =
                new EditorPatternModel(viewCalculator, TinkarTerm.DESCRIPTION_PATTERN.nid());
        descriptionColumn.setTitle(title);
        descriptionColumn.setTitleVisible(true);
        descriptionColumn.setColumnIndex(columnIndex);

        filterByDescriptionType(descriptionColumn, descriptionType, viewCalculator);
        showDescriptionTextOnly(descriptionColumn, viewCalculator);

        return descriptionColumn;
    }

    /**
     * Shows only the semantics holding {@code descriptionType} in the Description pattern's
     * "Description type" field (see {@link EditorPatternSemanticFilter}).
     */
    private static void filterByDescriptionType(EditorPatternModel descriptionPattern,
                                                EntityProxy.Concept descriptionType,
                                                ViewCalculator viewCalculator) {
        ensureLocallyResolvable(viewCalculator, descriptionType);

        viewCalculator.latestPatternEntityVersion(TinkarTerm.DESCRIPTION_PATTERN).ifPresent(patternVersion -> {
            EditorPatternSemanticFilter descriptionTypeFilter = new EditorPatternSemanticFilter();
            descriptionTypeFilter.getFieldConstraints().put(
                    patternVersion.indexForMeaning(TinkarTerm.DESCRIPTION_TYPE),
                    descriptionType);
            descriptionPattern.getSemanticFilters().add(descriptionTypeFilter);
        });
    }

    /**
     * Reduces the pattern's displayed fields to the description's text, shown without its field title:
     * the column's own title names what the text is.
     */
    private static void showDescriptionTextOnly(EditorPatternModel descriptionPattern,
                                                ViewCalculator viewCalculator) {
        viewCalculator.latestPatternEntityVersion(TinkarTerm.DESCRIPTION_PATTERN).ifPresent(patternVersion -> {
            int textFieldIndex = patternVersion.indexForMeaning(TinkarTerm.TEXT_FOR_DESCRIPTION);
            descriptionPattern.getVisibleFields().removeIf(field -> field.getIndex() != textFieldIndex);
            descriptionPattern.getVisibleFields().forEach(textField -> {
                // The fields were laid out one per row in pattern order, so the text field kept the row
                // of its index — with the fields above it gone, it moves up to the first row.
                textField.setRowIndex(0);
                textField.setTitleVisible(false);
            });
        });
    }

    /**
     * The standard Pattern window: the pattern is defined through the pattern-definition patterns —
     * a "Pattern Definition" section with the Meaning and Purpose pattern, a "Description" section
     * with the Description pattern, and a "Fields" section with the Fields pattern shown as a table
     * (one row per field of the pattern being defined). The Meaning and Purpose and Description
     * patterns are required when the window is opened in the Journal in create mode, and — as in the
     * Concept window — the Description pattern additionally requires one of its semantics to be the
     * pattern's fully qualified name; Fields is not required (a membership pattern has no fields).
     * Unlike the Concept window it keeps the default grey
     * chrome (see the concept-window-theme rules in kview.css, applied only to
     * {@link EditorWindowType#STANDARD_CONCEPT}).
     */
    private static void savePatternWindow2(KometPreferences standardWindowsPreferences,
                                           ViewCalculator viewCalculator) {
        EditorWindowModel window = new EditorWindowModel();
        window.setTitle(PATTERN_WINDOW_2);
        window.setWindowType(EditorWindowType.STANDARD_PATTERN);
        window.setTimelineVisible(true);

        // Definition
        EditorSectionModel definitionSection = window.getMainSection();
        populateDefinitionSection(viewCalculator, definitionSection);

        // Description
        EditorSectionModel descriptionSection = createPatternDescriptionSection(viewCalculator);
        window.getAdditionalSections().add(descriptionSection);

        // Fields
        EditorSectionModel fieldsSection = createFieldsSection(viewCalculator);
        window.getAdditionalSections().add(fieldsSection);

        window.save(standardWindowsPreferences);
    }

    private static EditorSectionModel createAxiomSection(ViewCalculator viewCalculator) {
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
        return axiomSection;
    }

    /**
     * The Description pattern as the standard Pattern window places it — showing every description of
     * the pattern in one column — required and refined so at least one of its semantics is a fully
     * qualified name: a pattern can't be created without one, just as a concept can't (the Concept
     * window carries that same requirement on its fully qualified name column, see
     * {@link #populateConceptDescriptionSection}).
     */
    private static EditorPatternModel createRequiredDescriptionPattern(ViewCalculator viewCalculator) {
        ensureLocallyResolvable(viewCalculator, TinkarTerm.DESCRIPTION_PATTERN);

        EditorPatternModel descriptionPattern =
                new EditorPatternModel(viewCalculator, TinkarTerm.DESCRIPTION_PATTERN.nid());
        descriptionPattern.setRequired(true);
        requireFullyQualifiedName(descriptionPattern, viewCalculator);
        return descriptionPattern;
    }

    /**
     * Refines the Description pattern's required flag ({@link EditorPatternRequirement}): at least one
     * of its semantics must hold "Fully qualified name description type" in the pattern's
     * "Description type" field, so the window's component can only be created once it has a fully
     * qualified name.
     */
    private static void requireFullyQualifiedName(EditorPatternModel descriptionPattern,
                                                  ViewCalculator viewCalculator) {
        ensureLocallyResolvable(viewCalculator, TinkarTerm.FULLY_QUALIFIED_NAME_DESCRIPTION_TYPE);

        viewCalculator.latestPatternEntityVersion(TinkarTerm.DESCRIPTION_PATTERN).ifPresent(patternVersion -> {
            EditorPatternRequirement fullyQualifiedNameRequirement = new EditorPatternRequirement();
            fullyQualifiedNameRequirement.getFieldConstraints().put(
                    patternVersion.indexForMeaning(TinkarTerm.DESCRIPTION_TYPE),
                    TinkarTerm.FULLY_QUALIFIED_NAME_DESCRIPTION_TYPE);
            descriptionPattern.getRequirements().add(fullyQualifiedNameRequirement);
        });
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

    private static EditorSectionModel createFieldsSection(ViewCalculator viewCalculator) {
        EditorSectionModel fieldsSection = new EditorSectionModel();
        fieldsSection.setName("Fields");
        EditorPatternModel fieldsPattern = new EditorPatternModel(viewCalculator,
                PatternDefinitionTerms.FIELDS_PATTERN.nid());
        KlPatternSemanticsFactories.byClassName(KlPatternSemanticsFactories.TABLE_FACTORY_CLASS_NAME)
                .ifPresent(fieldsPattern::setFactory);
        fieldsSection.getPatterns().add(fieldsPattern);
        return fieldsSection;
    }

    private static EditorSectionModel createPatternDescriptionSection(ViewCalculator viewCalculator) {
        EditorSectionModel descriptionSection = new EditorSectionModel();
        descriptionSection.setName(DESCRIPTION_SECTION_NAME);
        descriptionSection.getPatterns().add(createRequiredDescriptionPattern(viewCalculator));
        return descriptionSection;
    }

    private static void populateDefinitionSection(ViewCalculator viewCalculator, EditorSectionModel definitionSection) {
        definitionSection.setName("Pattern Definition");
        EditorPatternModel meaningAndPurposePattern = new EditorPatternModel(viewCalculator,
                PatternDefinitionTerms.MEANING_AND_PURPOSE_PATTERN.nid());
        meaningAndPurposePattern.setRequired(true);
        definitionSection.getPatterns().add(meaningAndPurposePattern);
    }
}
