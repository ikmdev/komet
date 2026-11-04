/*
 * Copyright © 2015 Integrated Knowledge Management (support@ikm.dev)
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package dev.ikm.komet.kview.mvvm.view.genpurpose;

import dev.ikm.komet.framework.view.ViewProperties;
import dev.ikm.komet.kview.controls.ContentSizedSplitPane;
import dev.ikm.komet.kview.common.ViewCalculatorUtils;
import dev.ikm.komet.kview.controls.ReferenceColors;
import dev.ikm.komet.kview.controls.SectionTitledPane;
import dev.ikm.komet.kview.mvvm.view.genpurpose.control.SectionReferenceButtonCell;
import dev.ikm.komet.kview.mvvm.view.genpurpose.control.SectionSemanticsComboBoxCell;
import dev.ikm.komet.layout.InlineEditSaver;
import dev.ikm.komet.layout.KlPatternSemanticsFactory;
import dev.ikm.komet.layout.PatternFieldDefaults;
import dev.ikm.komet.layout.PatternSemanticsPresenter;
import dev.ikm.komet.layout.editor.model.EditorFieldModel;
import dev.ikm.komet.layout.editor.model.EditorPatternModel;
import dev.ikm.komet.layout.editor.model.EditorSectionModel;
import dev.ikm.komet.layout.editor.model.EditorWindowModel;
import dev.ikm.komet.layout_engine.host.SupplementalAreaRenderer;
import dev.ikm.tinkar.entity.EntityHandle;
import dev.ikm.tinkar.entity.EntityService;
import dev.ikm.tinkar.entity.EntityVersion;
import dev.ikm.tinkar.entity.PatternEntity;
import dev.ikm.tinkar.entity.SemanticEntity;
import dev.ikm.tinkar.entity.SemanticEntityVersion;
import dev.ikm.tinkar.terms.EntityFacade;
import javafx.beans.binding.Bindings;
import javafx.beans.property.ObjectProperty;
import javafx.beans.property.SimpleObjectProperty;
import javafx.collections.ListChangeListener;
import javafx.css.PseudoClass;
import javafx.scene.Node;
import javafx.scene.control.Label;
import javafx.scene.control.TitledPane;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.Priority;
import javafx.scene.layout.VBox;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.function.BiConsumer;
import java.util.function.Consumer;
import java.util.function.Predicate;

/**
 * The sections of a general-purpose KL window: one titled pane per section of the KL-editor window
 * definition, each laying out its patterns' semantics views and supplemental areas. Owns what is
 * displayed where — which titled pane a section has, which presenter a pattern has, which
 * presenter shows a semantic — and the two things built on that: how a section resolves the
 * component its rows show (its reference component, which may be a semantic shown by another
 * section, resolving to any depth), and the chips in the section headers.
 *
 * <p>The window's controller keeps what the sections cannot decide alone, and is told through
 * the handler properties: a section's pencil was pressed, a section collapsed or expanded,
 * something that feeds the Publish button changed.
 */
final class WindowSections {

    /**
     * Active on a Pattern's container while the Pattern shows its title. Drives the vertical spacing
     * the title needs in kview.css.
     */
    private static final PseudoClass TITLE_VISIBLE_PSEUDO_CLASS = PseudoClass.getPseudoClass("title-visible");

    /**
     * Active on a Pattern's container while none of its fields show their title. The gap the Pattern
     * title sets below itself is sized for the field labels that normally lead each field, so kview.css
     * closes it when the field values follow the title directly.
     */
    private static final PseudoClass FIELD_TITLES_HIDDEN_PSEUDO_CLASS = PseudoClass.getPseudoClass("field-titles-hidden");

    private final EditorWindowModel windowModel;
    private final WindowEditSession session;
    private final ViewProperties viewProperties;
    /** The split pane the sections' titled panes stack in. */
    private final ContentSizedSplitPane content;
    /** The owning journal's topic, which the pattern presenters publish their events on. */
    private final UUID journalTopic;
    /**
     * What edits made inline in a semantics view (the stated definition's axiom tree) are saved
     * through, uncommitted until the window publishes; null when inline edits commit as applied.
     */
    private final InlineEditSaver inlineEditSaver;

    /**
     * Given a SectionModel what's its associated SectionTitledPane.
     */
    private final Map<EditorSectionModel, SectionTitledPane<EntityFacade>> sectionModelToTitledPane = new HashMap<>();

    /**
     * Given an Editor Pattern Model what is the associated Pattern Presenter.
     */
    private final Map<EditorPatternModel, PatternSemanticsPresenter> editorPatternModelToPatternPresenter = new HashMap<>();

    /**
     * Given a Semantic what is the associated Pattern Control that has it.
     */
    private final Map<SemanticEntity<SemanticEntityVersion>, PatternSemanticsPresenter> semanticEntityToPatternSemanticsPresenter = new HashMap<>();

    /**
     * Per section anchored on a reference pattern, its reference-component options' fields as
     * display text in pattern order, by option nid (see {@link ReferenceComponentSummaries}): what
     * the section's reference strip and combobox show. Recomputed whenever the options change.
     */
    private final Map<EditorSectionModel, Map<Long, List<ReferenceComponentSummaries.FieldText>>> sectionReferenceSummaries = new HashMap<>();

    /**
     * Per section anchored on a reference pattern, whether the user could see its selected
     * reference component at the last layout pass (see {@link #updateReferencesOnScreen}); a
     * section not looked at yet counts as on screen, so it shows no strip before the window has
     * had a look.
     */
    private final Map<EditorSectionModel, Boolean> sectionReferenceOnScreen = new HashMap<>();

    /**
     * Per section anchored on a reference pattern, whether its closed combobox currently cuts the
     * selected reference component's value off, as its button cell reports; the other input to
     * the strip decision (see {@link #updateReferenceStripVisibility}).
     */
    private final Map<EditorSectionModel, Boolean> sectionReferenceLabelTruncated = new HashMap<>();

    /**
     * The semantics currently rendered in the window, by nid, in render order — each the entity
     * object its view was built from, which is the key its presenter is filed under in
     * {@link #semanticEntityToPatternSemanticsPresenter}. Kept in step by {@link #doAddSemanticViews}
     * and {@link #clearSemanticViews}; read by {@link #unpublishedSemantics}.
     */
    private final Map<Long, SemanticEntity<SemanticEntityVersion>> displayedSemantics = new LinkedHashMap<>();

    /**
     * Sections currently being refreshed by a reference-component cascade. Used as a re-entrancy guard
     * so a malformed (cyclic) reference chain can't drive infinite recursion (see komet-desktop #3).
     */
    private final Set<EditorSectionModel> refreshingSections = new HashSet<>();

    private PatternSemanticsPresenter previousPatternSemanticsInEditMode;

    /**
     * @param windowModel     the KL-editor window definition whose sections to show
     * @param session         what the window is editing
     * @param content         the split pane the sections stack in
     * @param journalTopic    the owning journal's topic
     * @param inlineEditSaver what inline edits are saved through; null to commit them as applied
     */
    WindowSections(EditorWindowModel windowModel, WindowEditSession session, ContentSizedSplitPane content,
                   UUID journalTopic, InlineEditSaver inlineEditSaver) {
        this.windowModel = windowModel;
        this.session = session;
        this.viewProperties = session.getViewProperties();
        this.content = content;
        this.journalTopic = journalTopic;
        this.inlineEditSaver = inlineEditSaver;

        // Whether a section's reference component is on screen follows scrolling, collapsing and
        // resizing, so it is re-read after every layout pass of the scene the sections are in.
        content.sceneProperty().subscribe((oldScene, newScene) -> {
            if (oldScene != null) {
                oldScene.removePostLayoutPulseListener(this::updateReferencesOnScreen);
            }
            if (newScene != null) {
                newScene.addPostLayoutPulseListener(this::updateReferencesOnScreen);
            }
        });
    }

    /**
     * Re-reads, for each section anchored on a reference pattern, whether the user can currently
     * see its selected reference component in the section showing it, and re-decides the
     * section's reference strip when that changed (see {@link #updateReferenceStripVisibility});
     * only looked up for sections with several options, the only ones that could show a strip.
     */
    private void updateReferencesOnScreen() {
        sectionModelToTitledPane.forEach((section, titledPane) -> {
            if (section.getReferenceComponent() == null || titledPane.getReferenceComponents().size() < 2) {
                return;
            }
            EntityFacade selected = titledPane.getSelectedReferenceComponent();
            PatternSemanticsPresenter presenter = selected instanceof SemanticEntity<?> semantic
                    ? semanticEntityToPatternSemanticsPresenter.get((SemanticEntity<SemanticEntityVersion>) semantic)
                    : null;
            boolean onScreen = presenter != null
                    && presenter.isSemanticOnScreen((SemanticEntity<SemanticEntityVersion>) selected);
            if (!Objects.equals(sectionReferenceOnScreen.put(section, onScreen), onScreen)) {
                updateReferenceStripVisibility(section, titledPane);
            }
        });
    }

    /**
     * Builds the definition's sections — the main section and the additional ones — with their
     * patterns and supplemental areas, and follows sections and patterns added to the definition
     * afterwards (the editor and the window share the model).
     */
    void build() {
        EditorSectionModel mainSection = windowModel.getMainSection();

        // Main TitledPane
        TitledPane mainTitledPane = createTitledPane(mainSection);
        addPatternViewsOfSection(mainSection.getPatterns());
        addSupplementalAreaViewsOfSection(mainSection);
        content.getItems().add(mainTitledPane);

        mainSection.getPatterns().addListener((ListChangeListener<? super EditorPatternModel>) this::onSectionPatternsChanged);

        // Additional Sections
        windowModel.getAdditionalSections().forEach(section -> {
            TitledPane titledPane = createTitledPane(section);
            addPatternViewsOfSection(section.getPatterns());
            addSupplementalAreaViewsOfSection(section);
            content.getItems().add(titledPane);
        });

        // TODO: will sections need to be refreshed on coordinate changes?
        windowModel.getAdditionalSections().addListener(this::onAdditionalSectionsChanged);
    }

    /**
     * The stated axioms pattern per the view's logic coordinate — the pattern whose semantics
     * edit inline as an axiom tree (see {@code KlFieldHelper.createReadOnlyKlField}).
     */
    long statedAxiomsPatternNid() {
        return viewProperties.calculator().viewCoordinateRecord().logicCoordinate().statedAxiomsPatternNid();
    }

    /** The titled pane showing the section. */
    SectionTitledPane<EntityFacade> titledPaneOf(EditorSectionModel section) {
        return sectionModelToTitledPane.get(section);
    }

    /**
     * Previews a semantic in the view showing it, or stops previewing it — the hover feedback of
     * the places that list a section's semantics. A semantic no view shows (one the pattern's
     * display filters hide) has nothing to preview.
     */
    void setPreviewingSemantic(SemanticEntity<SemanticEntityVersion> semantic, boolean previewing) {
        PatternSemanticsPresenter patternSemanticsPresenter = semanticEntityToPatternSemanticsPresenter.get(semantic);
        if (patternSemanticsPresenter != null) {
            patternSemanticsPresenter.setPreviewingSemantic(previewing ? semantic : null);
        }
    }

    /**
     * Marks the semantic as the one being edited in the view showing it, un-marking the one marked
     * before; null un-marks only.
     */
    void setEditingSemantic(SemanticEntity<SemanticEntityVersion> semantic) {
        if (previousPatternSemanticsInEditMode != null) {
            previousPatternSemanticsInEditMode.setEditingSemantic(null);
        }
        PatternSemanticsPresenter patternSemanticsPresenter =
                semantic == null ? null : semanticEntityToPatternSemanticsPresenter.get(semantic);
        if (patternSemanticsPresenter != null) {
            patternSemanticsPresenter.setEditingSemantic(semantic);
        }
        previousPatternSemanticsInEditMode = patternSemanticsPresenter;
    }

    /**
     * Shows a semantic just created for a pattern of the section, below the pattern's existing
     * ones, and offers it as a reference-component option to every section anchored on the pattern.
     */
    void showNewSemantic(EditorSectionModel section, EditorPatternModel editorPatternModel,
                         SemanticEntity<SemanticEntityVersion> semantic) {
        PatternSemanticsPresenter patternSemanticsPresenter = editorPatternModelToPatternPresenter.get(editorPatternModel);

        // Add content to Pattern inside Section
        if (patternSemanticsPresenter == null) {
            // First time adding a Semantic for the given Pattern
            addPatternViewsOfSection(section.getPatterns());
        } else {
            // Not the first time adding a Semantic for this Pattern so we just add the new one below the existing ones
            patternSemanticsPresenter.addNewSemantic(semantic);
        }

        // Every section anchored on this pattern gets the new semantic as a reference-component
        // option — several sections may reference the same pattern (komet-desktop#193). The refresh
        // re-reads the options from the store (the semantic is saved, uncommitted), keeps a
        // surviving selection and otherwise selects the first option, so a section whose only
        // option this is selects it and enables its edit button.
        refreshSectionsReferencing(referencePattern -> referencePattern == editorPatternModel);
    }

    /**
     * Recomputes the reference-component options of every section whose reference pattern the
     * predicate accepts (see {@link #refreshSectionReferenceComponents}).
     */
    void refreshSectionsReferencing(Predicate<EditorPatternModel> referencePattern) {
        forEachSection(section -> {
            if (section.getReferenceComponent() != null && referencePattern.test(section.getReferenceComponent())) {
                refreshSectionReferenceComponents(section);
            }
        });
    }

    private TitledPane createTitledPane(EditorSectionModel sectionModel) {
        SectionTitledPane<EntityFacade> titledPane = new SectionTitledPane<>();
        titledPane.textProperty().bind(sectionModel.nameProperty());

        titledPane.setMaxHeight(Double.MAX_VALUE);
        titledPane.setMaxWidth(Double.MAX_VALUE);

        titledPane.setExpanded(!sectionModel.isStartCollapsed());

        titledPane.getStyleClass().add("pattern-titled-pane");

        titledPane.numberColumnsProperty().bind(sectionModel.numberColumnsProperty());

        // Section Semantics ComboBox
        List<EntityFacade> semanticsOfPattern = null;
        if (sectionModel.getReferenceComponent() != null) {
            EditorPatternModel patternReferenceComponent = sectionModel.getReferenceComponent();
            semanticsOfPattern = semanticsOfPattern(patternReferenceComponent);
            titledPane.getReferenceComponents().addAll(semanticsOfPattern);
        }
        titledPane.setReferenceComponentCellFactory(_ -> createSectionSemanticsComboBoxCell(sectionModel, viewProperties));

        // The options' labels can all read the same ("Module in Test Performed Pattern"), so the
        // closed combobox shows the value telling the selection apart plus its pattern, the
        // options lead with the values telling them apart, and the strip under the title bar
        // spells the fields out when the combobox cannot.
        SectionReferenceButtonCell buttonCell = new SectionReferenceButtonCell(
                option -> ReferenceComponentSummaries.buttonValue(referenceSummaryOf(sectionModel, option)),
                this::patternDisplayName);
        titledPane.setReferenceComponentButtonCellFactory(buttonCell);
        buttonCell.valueTruncatedProperty().subscribe(truncated -> {
            sectionReferenceLabelTruncated.put(sectionModel, truncated);
            updateReferenceStripVisibility(sectionModel, titledPane);
        });

        titledPane.getReferenceComponents().subscribe(() -> updateReferenceSummaries(sectionModel, titledPane));
        titledPane.selectedReferenceComponentProperty().subscribe(() -> updateReferenceStrip(sectionModel, titledPane));
        updateReferenceSummaries(sectionModel, titledPane);

        titledPane.setOnEditAction(_ -> getOnSectionEditRequested().accept(sectionModel));

        titledPane.editEnabledProperty().bind(sectionModel.referenceComponentProperty().isNull()
                .or(Bindings.isNotEmpty(titledPane.getReferenceComponents())));

        sectionModelToTitledPane.put(sectionModel, titledPane);

        // Collapsing a section closes the window by the height the section's content occupied, so
        // that height leaves the window instead of turning into blank space at the bottom of the
        // last section (komet-desktop#159).
        titledPane.expandedProperty().subscribe((_, isExpanded) -> getOnSectionExpandedChanged().accept(titledPane, isExpanded));

        // When this section's resolved reference component changes, cascade to any section that anchors
        // on it (i.e. whose reference pattern is displayed in this section), so downstream sections in a
        // reference-component chain re-resolve and re-populate (see komet-desktop #3).
        titledPane.selectedReferenceComponentProperty().subscribe(() -> {
            refreshSectionsAnchoredOn(sectionModel);
            updateReferencedByTags();
        });

        return titledPane;
    }

    /**
     * Marks, in the section showing it, each semantic some section is anchored on (the semantic
     * is that section's chosen reference component) with a bar in a color, and has the sections
     * anchored on it wear the same color on their reference combobox, so the user can tell which
     * semantic a section uses. The color identifies the chosen semantic: among the sections
     * anchored on a pattern that other sections are anchored on too, each distinct chosen semantic
     * gets the next color, in section order across the window, and sections that chose the same
     * semantic share it. A section alone on its pattern wears no color and its mark is the
     * default one. Only for sections with several options, since with one there is nothing to
     * tell apart, and only on semantics with a view (the pattern's display filters can hide one).
     * Recomputed from scratch whenever a section's options or selection change and whenever
     * semantic views are rebuilt.
     */
    private void updateReferencedByTags() {
        Map<Long, Integer> sectionsReferencingPattern = new HashMap<>();
        forEachSection(section -> {
            if (section.getReferenceComponent() != null) {
                sectionsReferencingPattern.merge(section.getReferenceComponent().getNid(), 1, Integer::sum);
            }
        });

        Map<Long, Integer> colorBySemantic = new HashMap<>();
        Map<Long, List<String>> sectionNamesBySemantic = new LinkedHashMap<>();
        forEachSection(section -> {
            SectionTitledPane<EntityFacade> titledPane = sectionModelToTitledPane.get(section);
            if (section.getReferenceComponent() == null || titledPane == null) {
                return;
            }
            EntityFacade selected = titledPane.getSelectedReferenceComponent();
            boolean sharesPattern = sectionsReferencingPattern.get(section.getReferenceComponent().getNid()) > 1;
            int colorIndex = sharesPattern && selected != null
                    ? colorBySemantic.computeIfAbsent(selected.nid(), _ -> colorBySemantic.size() % ReferenceColors.COUNT)
                    : -1;
            titledPane.setReferenceColorIndex(colorIndex);

            if (titledPane.getReferenceComponents().size() < 2 || selected == null) {
                return;
            }
            sectionNamesBySemantic.computeIfAbsent(selected.nid(), _ -> new ArrayList<>()).add(section.getName());
        });
        semanticEntityToPatternSemanticsPresenter.forEach((semantic, presenter) -> {
            List<String> sectionNames = sectionNamesBySemantic.get(semantic.nid());
            presenter.setReferencedBy(semantic, sectionNames == null ? null
                    : new PatternSemanticsPresenter.ReferencingSection(String.join(", ", sectionNames),
                            colorBySemantic.getOrDefault(semantic.nid(), 0)));
        });
    }

    /**
     * Reacts to a change of the section's reference-component options: recomputes their display
     * text, then refreshes everything showing it (the strip, the closed combobox) and the marks
     * on the chosen semantics, which depend on how many options the section has.
     */
    private void updateReferenceSummaries(EditorSectionModel section, SectionTitledPane<EntityFacade> titledPane) {
        sectionReferenceSummaries.put(section,
                ReferenceComponentSummaries.summarize(titledPane.getReferenceComponents(), viewProperties));
        updateReferenceStrip(section, titledPane);
        // The closed combobox only re-reads its texts when the selection changes.
        titledPane.refreshReferenceComponentLabel();
        // Whether the section tags its reference component depends on how many options it has.
        updateReferencedByTags();
    }

    /** Fills the section's reference strip with the selected reference component's fields, in pattern order. */
    private void updateReferenceStrip(EditorSectionModel section, SectionTitledPane<EntityFacade> titledPane) {
        List<SectionTitledPane.ReferenceField> fields = new ArrayList<>();
        for (ReferenceComponentSummaries.FieldText field : referenceSummaryOf(section, titledPane.getSelectedReferenceComponent())) {
            fields.add(new SectionTitledPane.ReferenceField(field.name(), field.value()));
        }
        titledPane.getReferenceSummary().setAll(fields);
        updateReferenceStripVisibility(section, titledPane);
    }

    /**
     * Shows a section's reference strip when the user cannot see the selected reference component
     * itself (see {@link #updateReferencesOnScreen}) and the combobox's label, the first field's
     * value, does not show the whole semantic: it has further fields, or that one value is cut off
     * in the combobox. Re-decided whenever any of those changes. The pane is passed in, as the
     * first decision is made while the section is still being built, before it is on record.
     */
    private void updateReferenceStripVisibility(EditorSectionModel section, SectionTitledPane<EntityFacade> titledPane) {
        int fields = titledPane.getReferenceSummary().size();
        boolean truncated = sectionReferenceLabelTruncated.getOrDefault(section, false);
        boolean labelEnough = fields == 0 || (fields == 1 && !truncated);
        titledPane.setReferenceStripVisible(!sectionReferenceOnScreen.getOrDefault(section, true) && !labelEnough);
    }

    private String patternDisplayName(EntityFacade option) {
        return option instanceof SemanticEntity<?> semantic
                ? ViewCalculatorUtils.retrieveDisplayName(semantic.pattern(), viewProperties)
                : "";
    }

    private List<ReferenceComponentSummaries.FieldText> referenceSummaryOf(EditorSectionModel section, EntityFacade option) {
        if (option == null) {
            return List.of();
        }
        return sectionReferenceSummaries.getOrDefault(section, Map.of()).getOrDefault(option.nid(), List.of());
    }

    private SectionSemanticsComboBoxCell createSectionSemanticsComboBoxCell(EditorSectionModel sectionModel, ViewProperties viewProperties) {
        SectionSemanticsComboBoxCell sectionSemanticsComboBoxCell = new SectionSemanticsComboBoxCell(viewProperties,
                option -> ReferenceComponentSummaries.optionText(referenceSummaryOf(sectionModel, option)));
        sectionSemanticsComboBoxCell.hoverProperty().subscribe(() -> {
            SemanticEntity<SemanticEntityVersion> semanticEntity = (SemanticEntity<SemanticEntityVersion>) sectionSemanticsComboBoxCell.getItem();
            setPreviewingSemantic(semanticEntity, sectionSemanticsComboBoxCell.isHover());
        });
        return sectionSemanticsComboBoxCell;
    }

    private void addPatternViewsOfSection(List<? extends EditorPatternModel> patternModels) {
        if (patternModels == null || patternModels.isEmpty()) {
            // A section may legitimately contain no patterns — e.g. one that holds only
            // supplemental areas (a Claude/Evrete check or chat). Nothing to render here.
            return;
        }

        for (EditorPatternModel editorPatternModel : patternModels) {
            addSinglePatternView(editorPatternModel);
        }
    }

    /**
     * Renders the section's placed supplemental areas (Claude/Evrete checks, chat, …). The whole
     * capability lives in knowledge-layout's {@link SupplementalAreaRenderer}, which materializes
     * each area generically from its plugin factory and injects this window's view and reference
     * concept. This window only delegates.
     */
    private void addSupplementalAreaViewsOfSection(EditorSectionModel section) {
        SectionTitledPane<EntityFacade> titledPane = sectionModelToTitledPane.get(section);
        EntityFacade refComponent = session.getComponent();
        SupplementalAreaRenderer.renderInto(section, titledPane.getItems(), viewProperties, refComponent);
    }

    private void addSinglePatternView(EditorPatternModel editorPatternModel) {
        EditorSectionModel parentSection = editorPatternModel.getParentSection();
        SectionTitledPane<EntityFacade> titledPane = sectionModelToTitledPane.get(parentSection);

        // Pattern fields
        PatternSemanticsPresenter patternSemanticsPresenter = addSemanticViews(editorPatternModel);

        editorPatternModelToPatternPresenter.put(editorPatternModel, patternSemanticsPresenter);

        Node view = createPatternContainer(editorPatternModel, patternSemanticsPresenter.getView());

        editorPatternModel.rowIndexProperty().subscribe(newRowIndex -> {
            GridPane.setRowIndex(view, newRowIndex.intValue());
        });

        editorPatternModel.columnIndexProperty().subscribe(newColumnIndex -> {
            GridPane.setColumnIndex(view, newColumnIndex.intValue());
        });

        editorPatternModel.columnSpanProperty().subscribe(newColumnSpan -> {
            GridPane.setColumnSpan(view, newColumnSpan.intValue());
        });

        titledPane.getItems().add(view);
    }

    /**
     * Wraps a Pattern's semantics view — whichever representation its factory supplies — in the
     * container that goes into the Section's grid, with the Pattern's title above it. Title text and
     * title visibility are authored per Pattern in the KL Editor, and apply to every representation,
     * so the title is rendered here rather than by each representation's own control.
     */
    private Node createPatternContainer(EditorPatternModel editorPatternModel, Node semanticsView) {
        Label patternTitle = new Label();
        patternTitle.getStyleClass().add("gen-purpose-pattern-title");
        // Uppercased here rather than in CSS — JavaFX has no text-transform — so the title reads in
        // the same group-label language as the field labels under it (KLReadOnlyBaseControlSkin
        // uppercases those the same way). Only the display changes; the authored title is untouched.
        patternTitle.textProperty().bind(editorPatternModel.titleProperty().map(String::toUpperCase));
        patternTitle.visibleProperty().bind(editorPatternModel.titleVisibleProperty());
        patternTitle.managedProperty().bind(editorPatternModel.titleVisibleProperty());

        VBox patternContainer = new VBox(patternTitle, semanticsView);
        patternContainer.getStyleClass().add("pattern-view-container");
        VBox.setVgrow(semanticsView, Priority.ALWAYS);

        // A shown title needs the room the semantics view claims above itself (see the negative top
        // inset on .pattern-container in kview.css), so the state is exposed to CSS.
        editorPatternModel.titleVisibleProperty().subscribe(titleVisible ->
                patternContainer.pseudoClassStateChanged(TITLE_VISIBLE_PSEUDO_CLASS, titleVisible));

        // The title's gap is likewise sized for the field labels below it, so whether any field still
        // shows one is exposed too. Recomputed as the author toggles the fields in the KL Editor.
        for (EditorFieldModel fieldModel : editorPatternModel.getVisibleFields()) {
            fieldModel.titleVisibleProperty().subscribe(() ->
                    updateFieldTitlesHidden(patternContainer, editorPatternModel));
        }
        updateFieldTitlesHidden(patternContainer, editorPatternModel);

        return patternContainer;
    }

    private static void updateFieldTitlesHidden(VBox patternContainer, EditorPatternModel editorPatternModel) {
        boolean anyFieldTitleShown = editorPatternModel.getVisibleFields().stream().anyMatch(EditorFieldModel::isTitleVisible);
        patternContainer.pseudoClassStateChanged(FIELD_TITLES_HIDDEN_PSEUDO_CLASS, !anyFieldTitleShown);
    }

    private PatternSemanticsPresenter addSemanticViews(EditorPatternModel editorPatternModel) {
        EditorSectionModel parentSection = editorPatternModel.getParentSection();

        List<EntityFacade> refComponents = getReferenceComponentsToUse(parentSection.getReferenceComponent());

        SectionTitledPane<EntityFacade> titledPane = sectionModelToTitledPane.get(parentSection);

        // The model always supplies a factory (it defaults to the Standard factory), so no fallback is needed here.
        KlPatternSemanticsFactory klPatternSemanticsFactory = editorPatternModel.getFactory();

        PatternSemanticsPresenter patternSemanticsPresenter = klPatternSemanticsFactory.createJournalControl(editorPatternModel,
                viewProperties, session.createOrGetComposer(), journalTopic);
        // In the Publish-flow window edits made inline in the details area (the stated
        // definition's axiom tree) stay uncommitted like the properties panel's submits do, until the
        // toolbar's Publish button commits them.
        if (inlineEditSaver != null) {
            patternSemanticsPresenter.setInlineEditSaver(inlineEditSaver);
        }

        if (!refComponents.isEmpty()) {
            doAddSemanticViews(editorPatternModel, patternSemanticsPresenter, refComponents.getFirst());
            titledPane.setSelectedReferenceComponent(refComponents.getFirst());
        }

        titledPane.selectedReferenceComponentProperty().subscribe(() -> {
            clearSemanticViews(patternSemanticsPresenter);
            doAddSemanticViews(editorPatternModel, patternSemanticsPresenter, titledPane.getSelectedReferenceComponent());
        });

        return patternSemanticsPresenter;
    }

    /** Re-renders the semantics of every pattern view showing the semantic's pattern, from the store. */
    void reloadSemanticViews(SemanticEntity<SemanticEntityVersion> semantic) {
        editorPatternModelToPatternPresenter.forEach((patternModel, presenter) -> {
            if (patternModel.getNid() == semantic.patternNid()) {
                clearSemanticViews(presenter);
                doAddSemanticViews(patternModel, presenter, semantic.referencedComponent());
            }
        });
    }

    /**
     * Re-renders every pattern's semantics from the store — after a publish, so the versions just
     * published stop showing as not published.
     */
    void reloadAllSemanticViews() {
        editorPatternModelToPatternPresenter.forEach((patternModel, presenter) -> {
            clearSemanticViews(presenter);
            // Resolved the way the sections are built (see resolveReferenceComponent): the
            // main section anchors on the window's component, which its titled pane never selects.
            doAddSemanticViews(patternModel, presenter,
                    resolveReferenceComponent(patternModel.getParentSection()));
        });
    }

    /**
     * Clears a presenter's semantic views, forgetting the semantics it displayed.
     */
    private void clearSemanticViews(PatternSemanticsPresenter presenter) {
        presenter.clearSemantics();
        displayedSemantics.values().removeIf(semantic -> semanticEntityToPatternSemanticsPresenter.get(semantic) == presenter);
    }

    private void doAddSemanticViews(EditorPatternModel editorPatternModel, PatternSemanticsPresenter patternSemanticsPresenter, EntityFacade referenceComponent) {
        if (referenceComponent == null) {
            // No reference concept selected — nothing to render for this pattern's semantics.
            return;
        }

        // Pattern Entity
        long patternNid = editorPatternModel.getNid();
        EntityHandle handle = EntityHandle.get(patternNid);
        PatternEntity patternEntity;
        if (handle.asPattern().isEmpty()) {
            throw new RuntimeException("Expecting a Pattern to be present instead of an empty Optional");
        }
        patternEntity = handle.asPattern().get();

        // Start adding Semantics — skipping the ones the pattern's display filters hide (see
        // EditorPatternSemanticFilter), e.g. a Description pattern showing only fully qualified names.
        EntityService.get().forEachSemanticForComponentOfPattern(referenceComponent.nid(), patternEntity.nid(),
                (semantic) -> {
                    // A pattern's defaults semantic is a semantic of the pattern referencing
                    // the pattern itself; it holds the defaults, not content (see PatternFieldDefaults).
                    if (PatternFieldDefaults.isDefaultsSemantic(semantic)) {
                        return;
                    }
                    // A semantic whose creation was cancelled is still in the store, canceled.
                    if (semantic.canceled()) {
                        return;
                    }
                    if (!editorPatternModel.displaysSemantic(semantic.nid(), viewProperties.calculator())) {
                        return;
                    }
                    patternSemanticsPresenter.addNewSemantic(semantic);
                    semanticEntityToPatternSemanticsPresenter.put(semantic, patternSemanticsPresenter);
                    displayedSemantics.put(semantic.nid(), semantic);
                });

        // The semantics just rendered may carry versions not published yet (see unpublishedSemantics).
        getOnPublishInputsChanged().run();
        // Views are new, so the tags of the semantics some section is anchored on are reapplied.
        updateReferencedByTags();
    }

    /**
     * The component a section's rows resolve against: the window's reference component for a section
     * with no reference pattern, otherwise the component currently selected for that section. This is
     * what lets reference-component chains resolve to arbitrary depth — a downstream section anchors on
     * the resolved component of the section that owns its reference pattern, rather than always anchoring
     * on the window component (see komet-desktop #3).
     */
    EntityFacade resolveReferenceComponent(EditorSectionModel section) {
        if (section == null || section.getReferenceComponent() == null) {
            return session.getComponent();
        }
        SectionTitledPane<EntityFacade> titledPane = sectionModelToTitledPane.get(section);
        return titledPane == null ? null : titledPane.getSelectedReferenceComponent();
    }

    private List<EntityFacade> getReferenceComponentsToUse(EditorPatternModel sectionReferenceComponent) {
        List<EntityFacade> refComponents = new ArrayList<>();

        if (sectionReferenceComponent != null) {
            // Anchor on the resolved reference component of the section that owns the reference pattern,
            // so chains where one section references another section's semantic resolve to any depth.
            EntityFacade base = resolveReferenceComponent(sectionReferenceComponent.getParentSection());
            if (base != null) {
                EntityService.get().forEachSemanticForComponentOfPattern(base.nid(), sectionReferenceComponent.getNid(),
                        (SemanticEntity<SemanticEntityVersion> semantic) -> {
                            if (!semantic.canceled()) {
                                refComponents.add(semantic);
                            }
                        }
                );
            }
        } else {
            // No section reference pattern — the section resolves directly against the window component.
            // Never return a list containing null (callers treat a non-empty list as having a usable component).
            EntityFacade windowRefComponent = session.getComponent();
            if (windowRefComponent != null) {
                refComponents.add(windowRefComponent);
            }
        }

        return refComponents;
    }

    /**
     * The pattern's semantics against the resolved reference component of the section placing it
     * (see {@link #resolveReferenceComponent}), canceled ones left out.
     */
    List<EntityFacade> semanticsOfPattern(EditorPatternModel editorPatternModel) {
        List<EntityFacade> refComponents = new ArrayList<>();

        // Anchor on the resolved reference component of the section that owns this reference pattern, so
        // a section referencing another section's semantic populates its options (see komet-desktop #3).
        EntityFacade base = resolveReferenceComponent(editorPatternModel.getParentSection());
        if (base == null) {
            return refComponents;
        }

        EntityService.get().forEachSemanticForComponentOfPattern(base.nid(), editorPatternModel.getNid(),
                (SemanticEntity<SemanticEntityVersion> semantic) -> {
                    // A semantic whose creation was cancelled stays in the store, canceled.
                    if (!semantic.canceled()) {
                        refComponents.add(semantic);
                    }
                }
        );

        return refComponents;
    }

    /**
     * Visits every section in this window (the main section and all additional sections).
     */
    private void forEachSection(Consumer<EditorSectionModel> action) {
        action.accept(windowModel.getMainSection());
        windowModel.getAdditionalSections().forEach(action);
    }

    /**
     * Refreshes each section's required-pattern chip (see the REQUIRED / "✓ REQUIREMENT MET"
     * chip in the section title bar): shown in create mode on sections hosting a required
     * pattern, flipping to satisfied once every required pattern in the section is satisfied
     * ({@link #isRequiredPatternSatisfied}) — the same check that gates the Publish button in
     * create mode.
     */
    void updateRequiredChips() {
        boolean createMode = session.isCreateMode();
        sectionModelToTitledPane.forEach((section, titledPane) -> {
            List<EditorPatternModel> requiredPatterns = section.getPatterns().stream()
                    .filter(EditorPatternModel::isRequired)
                    .toList();
            boolean chipVisible = createMode && !requiredPatterns.isEmpty();
            titledPane.setRequiredChipVisible(chipVisible);
            if (chipVisible) {
                titledPane.setRequiredSatisfied(requiredPatterns.stream()
                        .allMatch(this::isRequiredPatternSatisfied));
            }
        });
    }

    /**
     * The unpublished strip's "Show" action: brings the first semantic with an unpublished version
     * into view, expanding the section holding it if it is collapsed.
     */
    void revealFirstUnpublishedSemantic() {
        List<SemanticEntity<SemanticEntityVersion>> unpublished = unpublishedSemantics();
        if (unpublished.isEmpty()) {
            return;
        }
        SemanticEntity<SemanticEntityVersion> semantic = unpublished.getFirst();
        PatternSemanticsPresenter presenter = semanticEntityToPatternSemanticsPresenter.get(semantic);
        editorPatternModelToPatternPresenter.forEach((patternModel, patternPresenter) -> {
            if (patternPresenter == presenter) {
                sectionModelToTitledPane.get(patternModel.getParentSection()).setExpanded(true);
            }
        });
        presenter.revealSemantic(semantic);
    }

    /**
     * Sets each section header's NOT PUBLISHED chip and note ("2 changes by you") from the
     * section's semantics whose latest version is saved but not published yet; sections without
     * any show no chip. Not in create mode, whose DRAFT chip and hint already say the whole
     * component is unpublished.
     */
    void updateUnpublishedChips() {
        Map<EditorSectionModel, List<SemanticEntity<SemanticEntityVersion>>> unpublishedBySection = new HashMap<>();
        if (!session.isCreateMode()) {
            for (SemanticEntity<SemanticEntityVersion> semantic : unpublishedSemantics()) {
                PatternSemanticsPresenter presenter = semanticEntityToPatternSemanticsPresenter.get(semantic);
                editorPatternModelToPatternPresenter.forEach((patternModel, patternPresenter) -> {
                    if (patternPresenter == presenter) {
                        unpublishedBySection.computeIfAbsent(patternModel.getParentSection(), section -> new ArrayList<>())
                                .add(semantic);
                    }
                });
            }
        }
        long currentAuthorNid = viewProperties.nodeView().editCoordinate().getAuthorNidForChanges();
        sectionModelToTitledPane.forEach((section, titledPane) -> {
            List<SemanticEntity<SemanticEntityVersion>> unpublished = unpublishedBySection.get(section);
            if (unpublished == null) {
                titledPane.setUnpublishedNote(null);
                return;
            }
            boolean allByCurrentAuthor = unpublished.stream()
                    .flatMap(semantic -> EntityHandle.get(semantic.nid()).expectEntity().versions().stream())
                    .filter(EntityVersion::uncommitted)
                    .allMatch(version -> version.stamp().authorNid() == currentAuthorNid);
            titledPane.setUnpublishedNote((unpublished.size() == 1 ? "1 change" : unpublished.size() + " changes")
                    + (allByCurrentAuthor ? " by you" : ""));
        });
    }

    /**
     * The semantics shown in this window whose latest version is saved but not published yet, in
     * render order — each as the entity object its view was built from (see {@link #displayedSemantics}).
     * Whether a semantic is published is read from the store, since a version may have been
     * submitted or published since its view was built.
     */
    List<SemanticEntity<SemanticEntityVersion>> unpublishedSemantics() {
        return displayedSemantics.values().stream()
                .filter(semantic -> EntityHandle.get(semantic.nid()).expectSemantic().uncommitted())
                .toList();
    }

    /**
     * Whether every pattern marked Required in the KL editor is satisfied
     * ({@link #isRequiredPatternSatisfied}) against its section's resolved reference component.
     * In create mode this gates the actual creation (commit) of the window's component:
     * uncommitted semantics count, since they are found by the entity service once saved.
     */
    boolean allRequiredPatternsSatisfied() {
        AtomicBoolean satisfied = new AtomicBoolean(true);
        forEachSection(section -> {
            for (EditorPatternModel pattern : section.getPatterns()) {
                if (pattern.isRequired() && !isRequiredPatternSatisfied(pattern)) {
                    satisfied.set(false);
                }
            }
        });
        return satisfied.get();
    }

    /**
     * Whether a required pattern's requirement is met against its section's resolved reference
     * component (see {@link PatternRequirementUtils#isPatternSatisfied}).
     */
    private boolean isRequiredPatternSatisfied(EditorPatternModel pattern) {
        return PatternRequirementUtils.isPatternSatisfied(pattern, semanticsOfPattern(pattern),
                viewProperties.calculator(), statedAxiomsPatternNid());
    }

    /**
     * Re-resolves every section whose reference pattern lives in {@code changedSection}, so a change to
     * that section's reference component propagates down the reference-component chain. Each refreshed
     * section's own selection change drives the next hop, so the cascade naturally reaches arbitrary
     * depth (see komet-desktop #3).
     */
    private void refreshSectionsAnchoredOn(EditorSectionModel changedSection) {
        refreshSectionsReferencing(referencePattern -> referencePattern.getParentSection() == changedSection);
    }

    /**
     * Recomputes a section's reference-component options against its (now-changed) upstream anchor,
     * preserving the prior selection when it survives and otherwise defaulting to the first option.
     * Re-selecting re-populates the section's rows and cascades to its own downstream sections.
     */
    private void refreshSectionReferenceComponents(EditorSectionModel section) {
        SectionTitledPane<EntityFacade> titledPane = sectionModelToTitledPane.get(section);
        // Skip when the section isn't built yet (initial load resolves it directly) or it is already
        // mid-refresh (guards against a cyclic reference chain recursing forever).
        if (titledPane == null || !refreshingSections.add(section)) {
            return;
        }

        List<EntityFacade> options = semanticsOfPattern(section.getReferenceComponent());
        EntityFacade previousSelection = titledPane.getSelectedReferenceComponent();

        titledPane.getReferenceComponents().setAll(options);

        EntityFacade newSelection = null;
        if (previousSelection != null) {
            for (EntityFacade option : options) {
                if (option.nid() == previousSelection.nid()) {
                    newSelection = option;
                    break;
                }
            }
        }
        if (newSelection == null && !options.isEmpty()) {
            newSelection = options.getFirst();
        }

        titledPane.setSelectedReferenceComponent(newSelection);
        refreshingSections.remove(section);
    }

    private void onSectionPatternsChanged(ListChangeListener.Change<? extends EditorPatternModel> change) {
        while (change.next()) {
            if (change.wasAdded()) {
                addPatternViewsOfSection(change.getAddedSubList());
            }
        }
    }

    private void onAdditionalSectionsChanged(ListChangeListener.Change<? extends EditorSectionModel> change) {
        while (change.next()) {
            if (change.wasAdded()) {
                for (EditorSectionModel additionalSectionModel : change.getAddedSubList()) {
                    TitledPane titledPane = createTitledPane(additionalSectionModel);
                    addPatternViewsOfSection(additionalSectionModel.getPatterns());
                    additionalSectionModel.getPatterns().addListener((ListChangeListener<? super EditorPatternModel>) this::onSectionPatternsChanged);

                    content.getItems().add(titledPane);
                }
                updateRequiredChips();
                getOnPublishInputsChanged().run();
            }
        }
    }

    /*=========================================================================*
     *                                                                         *
     * Properties                                                              *
     *                                                                         *
     *=========================================================================*/

    // -- on section edit requested
    /** Called with the section whose pencil button was pressed. */
    private final ObjectProperty<Consumer<EditorSectionModel>> onSectionEditRequested = new SimpleObjectProperty<>(this, "onSectionEditRequested");
    public Consumer<EditorSectionModel> getOnSectionEditRequested() { return onSectionEditRequested.get(); }
    public ObjectProperty<Consumer<EditorSectionModel>> onSectionEditRequestedProperty() { return onSectionEditRequested; }
    public void setOnSectionEditRequested(Consumer<EditorSectionModel> handler) { this.onSectionEditRequested.set(handler); }

    // -- on section expanded changed
    /** Called with the section's titled pane and whether it is now expanded (false: collapsed). */
    private final ObjectProperty<BiConsumer<SectionTitledPane<EntityFacade>, Boolean>> onSectionExpandedChanged = new SimpleObjectProperty<>(this, "onSectionExpandedChanged");
    public BiConsumer<SectionTitledPane<EntityFacade>, Boolean> getOnSectionExpandedChanged() { return onSectionExpandedChanged.get(); }
    public ObjectProperty<BiConsumer<SectionTitledPane<EntityFacade>, Boolean>> onSectionExpandedChangedProperty() { return onSectionExpandedChanged; }
    public void setOnSectionExpandedChanged(BiConsumer<SectionTitledPane<EntityFacade>, Boolean> handler) { this.onSectionExpandedChanged.set(handler); }

    // -- on publish inputs changed
    /**
     * Called when semantics were rendered or a section was added: what feeds the Publish button
     * may have moved.
     */
    private final ObjectProperty<Runnable> onPublishInputsChanged = new SimpleObjectProperty<>(this, "onPublishInputsChanged");
    public Runnable getOnPublishInputsChanged() { return onPublishInputsChanged.get(); }
    public ObjectProperty<Runnable> onPublishInputsChangedProperty() { return onPublishInputsChanged; }
    public void setOnPublishInputsChanged(Runnable handler) { this.onPublishInputsChanged.set(handler); }
}
