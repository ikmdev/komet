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
import dev.ikm.komet.kview.controls.KometLabel;
import dev.ikm.komet.kview.controls.SectionEditPopup;
import dev.ikm.komet.kview.controls.SectionTitledPane;
import dev.ikm.komet.layout.PatternFieldDefaults;
import dev.ikm.komet.layout.editor.model.EditorPatternModel;
import dev.ikm.komet.layout.editor.model.EditorPatternSemanticFilter;
import dev.ikm.komet.layout.editor.model.EditorSectionModel;
import dev.ikm.komet.layout.editor.model.EditorWindowModel;
import dev.ikm.komet.layout.editor.model.EditorWindowType;
import dev.ikm.tinkar.entity.EntityService;
import dev.ikm.tinkar.entity.SemanticEntity;
import dev.ikm.tinkar.entity.SemanticEntityVersion;
import dev.ikm.tinkar.terms.EntityFacade;
import javafx.beans.property.ObjectProperty;
import javafx.beans.property.SimpleObjectProperty;
import javafx.geometry.Point2D;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.function.BiConsumer;
import java.util.stream.Collectors;

/**
 * The menu a section's pencil button opens in a general-purpose KL window: the section's existing
 * semantics, each opening its edit form, followed by the "Add …" entries that create a new one.
 * The stated definition pattern takes over its section's menu, offering the definition seeds
 * instead of the per-pattern entries. With nothing to edit and one obvious entry to offer, that
 * entry runs straight away instead of opening the menu.
 *
 * <p>What a chosen entry does belongs to the window's controller, which is told through the
 * handler properties: edit this semantic, create a semantic of this pattern, seed the stated
 * definition.
 */
final class SectionEditMenu {

    private final EditorWindowModel windowModel;
    private final WindowEditSession session;
    private final WindowSections windowSections;
    private final ViewProperties viewProperties;

    /**
     * @param windowModel    the KL-editor window definition the sections come from
     * @param session        what the window is editing
     * @param windowSections the sections the menu opens on and reads its semantics from
     */
    SectionEditMenu(EditorWindowModel windowModel, WindowEditSession session, WindowSections windowSections) {
        this.windowModel = windowModel;
        this.session = session;
        this.windowSections = windowSections;
        this.viewProperties = session.getViewProperties();
    }

    /**
     * Opens the menu for the section, anchored at its titled pane's top right corner — or runs its
     * one obvious create entry, or does nothing when there is nothing to offer.
     */
    void open(EditorSectionModel sectionModel) {
        SectionEditPopup popup = new SectionEditPopup();

        // The Reference Component to use
        EntityFacade refComponent = windowSections.resolveReferenceComponent(sectionModel);

        // In create mode the standard Concept/Pattern window has no reference component yet — the
        // new component is created lazily when the user authors the first semantic (see
        // the on-create-semantic handler), so the popup still opens, offering only its create entries.
        boolean canCreateReferenceComponent = sectionModel.getReferenceComponent() == null
                && session.isCreateMode()
                && (windowModel.getWindowType() == EditorWindowType.STANDARD_CONCEPT
                        || windowModel.getWindowType() == EditorWindowType.STANDARD_PATTERN);

        if (refComponent == null && !canCreateReferenceComponent) {
            // No reference concept to edit against — nothing to populate.
            return;
        }

        // The stated definition pattern takes over its section's popup: its semantics are the
        // ones offered for editing (never the reasoner-owned inferred definition's), and its
        // create entries seed the definition with a set instead of the per-pattern "Add …"
        // entries — the classic concept window's axiom + menu.
        EditorPatternModel statedPattern = sectionStatedPattern(sectionModel);
        EditorPatternModel editPattern = statedPattern != null
                ? statedPattern
                : sectionModel.getPatterns().getFirst();

        if (refComponent != null) {
            // Populate the Popup
            EntityService.get().forEachSemanticForComponentOfPattern(refComponent.nid(),
                    editPattern.getNid(), (semantic) -> {
                        // The pattern's defaults semantic is edited through its own entry
                        // below, never as one of the pattern's semantics; a semantic whose
                        // creation was cancelled is still in the store, canceled.
                        if (PatternFieldDefaults.isDefaultsSemantic(semantic) || semantic.canceled()) {
                            return;
                        }
                        KometLabel semanticLabel = new KometLabel(semantic, viewProperties);
                        semanticLabel.setShowTooltip(true);

                        semanticLabel.setOnMouseClicked(_ -> {
                            getOnEditSemantic().accept(semantic, editPattern);
                            popup.hide();
                        });

                        // The popup lists every semantic of the pattern, while the details area
                        // shows only the ones passing the pattern's display filters — e.g. the
                        // standard Concept window's Description columns, which between them show
                        // fully qualified names, other names and definitions, so a description of
                        // any other type has no view to preview.
                        semanticLabel.hoverProperty().subscribe(() ->
                                windowSections.setPreviewingSemantic(semantic, semanticLabel.isHover()));

                        popup.getItems().add(semanticLabel);
                    });
        }

        List<CreateEntry> createEntries = new ArrayList<>();
        if (statedPattern != null) {
            // A concept has at most one stated definition — offer the seeds only until it
            // exists (afterwards more sets are added inline, on the tree's root row), and only
            // while the pattern accepts new semantics. Sufficient before necessary, matching the
            // classic concept window's menu.
            if (popup.getItems().isEmpty() && canAddSemantics(statedPattern)) {
                createEntries.add(new CreateEntry(statedPattern, Map.of(),
                        new SectionEditPopup.CreateAction("Add sufficient set",
                                () -> getOnCreateStatedDefinition().createStatedDefinition(sectionModel, statedPattern, false))));
                createEntries.add(new CreateEntry(statedPattern, Map.of(),
                        new SectionEditPopup.CreateAction("Add necessary set",
                                () -> getOnCreateStatedDefinition().createStatedDefinition(sectionModel, statedPattern, true))));
            }
        } else {
            for (EditorPatternModel patternModel : sectionModel.getPatterns()) {
                // Once in edit mode, only patterns authored in the KL Editor as allowing new
                // semantics accept them.
                if (!canAddSemantics(patternModel)) {
                    continue;
                }
                addCreateEntries(createEntries, sectionModel, patternModel, refComponent);
            }
        }
        createEntries.forEach(entry -> popup.getCreateActions().add(entry.createAction()));

        // With no semantic to edit, the popup would only offer its create entries — when one of
        // them is the obvious choice it runs straight away instead of asking, and with none left
        // to offer (the patterns don't accept new semantics anymore) there is nothing to show.
        if (popup.getItems().isEmpty()) {
            if (createEntries.isEmpty()) {
                return;
            }
            Optional<CreateEntry> directRunEntry = PatternRequirementUtils.getDirectRunEntry(sectionModel.getPatterns(),
                    createEntries, windowSections::semanticsOfPattern, viewProperties.calculator(),
                    windowSections.statedAxiomsPatternNid());
            if (directRunEntry.isPresent()) {
                directRunEntry.get().createAction().action().run();
                return;
            }
        }

        // Show Popup
        SectionTitledPane<?> sectionTitledPane = windowSections.titledPaneOf(sectionModel);
        Point2D screenPoint = sectionTitledPane.localToScreen(
                sectionTitledPane.getWidth(),
                0
        );
        popup.show(sectionTitledPane, screenPoint.getX(), screenPoint.getY());
    }

    /**
     * The section's stated definition pattern, or null when the section doesn't hold it.
     */
    private EditorPatternModel sectionStatedPattern(EditorSectionModel sectionModel) {
        return sectionModel.getPatterns().stream()
                .filter(pattern -> pattern.getNid() == windowSections.statedAxiomsPatternNid())
                .findFirst().orElse(null);
    }

    /**
     * Whether new semantics of the pattern can be added right now. In create mode they always can;
     * once the window is in edit mode the pattern must allow new semantics, as authored in the KL
     * Editor. Existing semantics stay editable either way.
     */
    private boolean canAddSemantics(EditorPatternModel patternModel) {
        return session.isCreateMode() || patternModel.isAllowNewSemantics();
    }

    /**
     * Adds the section popup's "Add …" create entries for one pattern of the section to the passed
     * in list. A pattern without display filters gets one entry named after the pattern; a
     * filtered pattern gets one entry per filter, named after the concept(s) the filter selects on
     * — e.g. "Add Fully qualified name" for a Description pattern filtered to fully qualified
     * names — and that entry's new semantic is seeded with the filter's field constraints, so it
     * comes out passing the filter it was created from. Either way a trailing "Pattern" word is
     * dropped from the entry's name.
     */
    private void addCreateEntries(List<CreateEntry> createEntries,
            EditorSectionModel sectionModel, EditorPatternModel patternModel, EntityFacade refComponent) {
        if (patternModel.getSemanticFilters().isEmpty()) {
            createEntries.add(new CreateEntry(patternModel, Map.of(), new SectionEditPopup.CreateAction(
                    "Add " + PatternUIUtils.stripPatternSuffix(patternModel.getTitle()), () -> {
                        getOnCreateSemantic().createSemantic(sectionModel, patternModel, refComponent, Map.of());
                    })));
            return;
        }

        for (EditorPatternSemanticFilter filter : patternModel.getSemanticFilters()) {
            String filterConceptNames = filter.getFieldConstraints().values().stream()
                    .map(filterConcept -> viewProperties.calculator()
                            .getPreferredDescriptionTextWithFallbackOrNid(filterConcept.nid()))
                    .distinct()
                    .collect(Collectors.joining(", "));
            // A filter constraining nothing selects every semantic — its entry falls back to the
            // pattern's name.
            String entryName = filterConceptNames.isEmpty()
                    ? patternModel.getTitle()
                    : filterConceptNames;

            // Snapshot the constraints at popup-build time; they seed the new semantic's fields.
            Map<Integer, EntityFacade> fieldSeeds = Map.copyOf(filter.getFieldConstraints());
            createEntries.add(new CreateEntry(patternModel, fieldSeeds, new SectionEditPopup.CreateAction(
                    "Add " + PatternUIUtils.stripPatternSuffix(entryName), () -> {
                        getOnCreateSemantic().createSemantic(sectionModel, patternModel, refComponent, fieldSeeds);
                    })));
        }
    }

    /**
     * One "Add …" entry of a section's edit popup, kept with the pattern it creates a semantic of
     * and the field values that new semantic starts out with, so
     * {@link PatternRequirementUtils#getDirectRunEntry} can tell which entry meets a requirement.
     */
    private record CreateEntry(EditorPatternModel pattern, Map<Integer, EntityFacade> fieldSeeds,
                               SectionEditPopup.CreateAction createAction)
            implements PatternRequirementUtils.SeededEntry {
    }

    /** Creates a new semantic of a section's pattern, its fields starting out with the seeds. */
    @FunctionalInterface
    interface CreateSemanticHandler {
        void createSemantic(EditorSectionModel section, EditorPatternModel pattern, EntityFacade referenceComponent,
                            Map<Integer, EntityFacade> fieldSeeds);
    }

    /** Creates a section's stated definition, seeded with a necessary or a sufficient set. */
    @FunctionalInterface
    interface CreateStatedDefinitionHandler {
        void createStatedDefinition(EditorSectionModel section, EditorPatternModel pattern, boolean necessary);
    }

    /*=========================================================================*
     *                                                                         *
     * Properties                                                              *
     *                                                                         *
     *=========================================================================*/

    // -- on edit semantic
    /** Called with a semantic picked from the menu and the KL Editor's placement of its pattern. */
    private final ObjectProperty<BiConsumer<SemanticEntity<SemanticEntityVersion>, EditorPatternModel>> onEditSemantic = new SimpleObjectProperty<>(this, "onEditSemantic");
    public BiConsumer<SemanticEntity<SemanticEntityVersion>, EditorPatternModel> getOnEditSemantic() { return onEditSemantic.get(); }
    public ObjectProperty<BiConsumer<SemanticEntity<SemanticEntityVersion>, EditorPatternModel>> onEditSemanticProperty() { return onEditSemantic; }
    public void setOnEditSemantic(BiConsumer<SemanticEntity<SemanticEntityVersion>, EditorPatternModel> handler) { this.onEditSemantic.set(handler); }

    // -- on create semantic
    /**
     * Called when an "Add …" entry is picked, with the section, the pattern, the section's
     * reference component (null in create mode before the window's component exists) and the
     * field seeds of the display filter the entry came from.
     */
    private final ObjectProperty<CreateSemanticHandler> onCreateSemantic = new SimpleObjectProperty<>(this, "onCreateSemantic");
    public CreateSemanticHandler getOnCreateSemantic() { return onCreateSemantic.get(); }
    public ObjectProperty<CreateSemanticHandler> onCreateSemanticProperty() { return onCreateSemantic; }
    public void setOnCreateSemantic(CreateSemanticHandler handler) { this.onCreateSemantic.set(handler); }

    // -- on create stated definition
    /** Called when an "Add sufficient set" or "Add necessary set" entry is picked. */
    private final ObjectProperty<CreateStatedDefinitionHandler> onCreateStatedDefinition = new SimpleObjectProperty<>(this, "onCreateStatedDefinition");
    public CreateStatedDefinitionHandler getOnCreateStatedDefinition() { return onCreateStatedDefinition.get(); }
    public ObjectProperty<CreateStatedDefinitionHandler> onCreateStatedDefinitionProperty() { return onCreateStatedDefinition; }
    public void setOnCreateStatedDefinition(CreateStatedDefinitionHandler handler) { this.onCreateStatedDefinition.set(handler); }
}
