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

import static dev.ikm.komet.kview.events.EventTopics.SAVE_PATTERN_TOPIC;
import static dev.ikm.komet.layout_engine.window.DraggableSupport.addDraggableNodes;
import static dev.ikm.komet.kview.klfields.KlFieldHelper.retrieveCommittedLatestVersion;
import static dev.ikm.komet.kview.mvvm.view.common.ChapterWindowHelper.setupViewCoordinateOptionsPopup;
import static dev.ikm.komet.kview.mvvm.view.journal.JournalController.toast;

import dev.ikm.komet.framework.Identicon;
import dev.ikm.komet.framework.observable.ObservableEntity;
import dev.ikm.komet.framework.observable.ObservableEntityHandle;
import dev.ikm.komet.framework.observable.ObservableEntitySnapshot;
import dev.ikm.komet.framework.observable.ObservableField;
import dev.ikm.komet.framework.view.ViewProperties;
import dev.ikm.komet.kview.common.ViewCalculatorUtils;
import dev.ikm.komet.kview.controls.ContentSizedSplitPane;
import dev.ikm.komet.kview.controls.ComponentItem;
import dev.ikm.komet.kview.controls.KLWorkspace;
import dev.ikm.komet.kview.controls.KlWindowControlToolbar;
import dev.ikm.komet.layout.controls.FilterOptionsPopup;
import dev.ikm.komet.kview.controls.PublicIDListControl;
import dev.ikm.komet.kview.controls.SectionTitledPane;
import dev.ikm.komet.kview.controls.StampViewControl;
import dev.ikm.komet.kview.controls.Toast;
import dev.ikm.komet.kview.controls.ComponentItemNode;
import dev.ikm.komet.kview.events.genediting.MakeGenEditingWindowEvent;
import dev.ikm.komet.kview.events.pattern.PatternSavedEvent;
import dev.ikm.komet.kview.mvvm.view.genpurpose.control.PropertiesTabsControl;
import dev.ikm.komet.kview.mvvm.view.genpurpose.control.PropertiesTabsControl.Tab;
import dev.ikm.komet.kview.mvvm.view.genpurpose.control.standard.SemanticStandardControl;
import dev.ikm.komet.kview.mvvm.view.journal.VerticallyFilledPane;
import dev.ikm.komet.kview.mvvm.viewmodel.FormViewModel.FormMode;
import dev.ikm.komet.layout.InlineEditSaver;
import dev.ikm.komet.layout.editor.EditorWindowManager;
import dev.ikm.komet.layout.editor.model.EditorPatternModel;
import dev.ikm.komet.layout.editor.model.EditorSectionModel;
import dev.ikm.komet.layout.editor.model.EditorWindowModel;
import dev.ikm.komet.layout.editor.model.EditorWindowType;
import dev.ikm.komet.layout_engine.window.WindowSupport;
import dev.ikm.komet.preferences.KometPreferences;
import dev.ikm.tinkar.common.util.time.DateTimeUtil;
import dev.ikm.tinkar.coordinate.stamp.calculator.Latest;
import dev.ikm.tinkar.coordinate.view.calculator.ViewCalculator;
import dev.ikm.tinkar.entity.Entity;
import dev.ikm.tinkar.entity.EntityHandle;
import dev.ikm.komet.layout.PatternFieldDefaults;
import dev.ikm.tinkar.entity.EntityVersion;
import dev.ikm.tinkar.entity.SemanticEntity;
import dev.ikm.tinkar.entity.SemanticEntityVersion;
import dev.ikm.tinkar.entity.StampEntity;
import dev.ikm.tinkar.entity.graph.DiTreeEntity;
import dev.ikm.tinkar.events.EvtBusFactory;
import dev.ikm.tinkar.terms.ConceptFacade;
import dev.ikm.tinkar.terms.EntityFacade;
import dev.ikm.tinkar.terms.EntityProxy;
import dev.ikm.tinkar.terms.PatternFacade;
import dev.ikm.tinkar.terms.State;
import javafx.application.Platform;
import javafx.css.PseudoClass;
import javafx.scene.control.Label;
import javafx.scene.control.Tooltip;
import javafx.scene.image.Image;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Region;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.nio.file.Paths;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;
import java.util.function.Consumer;

public class GenPurposeDetailsController {

    private static final Logger LOG = LoggerFactory.getLogger(GenPurposeDetailsController.class);

    /**
     * Active while the window is in create mode, i.e. framing a component that doesn't exist yet.
     * Drives the "ghost window" styling in kview.css (dimmed blue chrome, dashed frame, dimmed STAMP).
     */
    private static final PseudoClass CREATE_MODE = PseudoClass.getPseudoClass("create-mode");

    /**
     * Active while the window "has focus": the mouse was pressed inside it or any of its controls
     * holds keyboard focus. Drives the enlarged drop shadow in kview.css that makes the window
     * read as sitting closer to the screen.
     */
    private static final PseudoClass WINDOW_FOCUSED = PseudoClass.getPseudoClass("window-focused");

    /**
     * Key a collapsed section keeps the content height it gave up under, so expanding it again
     * hands the window back the very height its collapse closed (see
     * {@link #resizeWindowWithSection(SectionTitledPane, boolean)}).
     */
    private static final String COLLAPSED_CONTENT_HEIGHT_KEY = "gen-purpose-collapsed-content-height";

    /**
     * Given a SemanticEntity what's its associated Semantic Control.
     */
    private final Map<SemanticEntity<SemanticEntityVersion>, SemanticStandardControl> semanticEntityToSemanticView = new HashMap<>();

    /** The definition's sections: what is displayed where, and how each section resolves its component. */
    private final WindowSections windowSections;
    /** The menu a section's pencil button opens. */
    private final SectionEditMenu sectionEditMenu;

    /**
     * The semantics an "Add …" entry created ({@link #onCreateSemantic}) whose edit form has not
     * been submitted yet, by nid. Cancelling the form of one of them discards the semantic (see
     * {@link #onSemanticCancelled}); a submit, or the publish that commits it, takes it out.
     */
    private final Set<Long> unsubmittedNewSemanticNids = new HashSet<>();

    // The parts of the window's view (see GenPurposeWindowView) this controller wires behavior onto.
    private final StampViewControl stampViewControl;
    private final ContentSizedSplitPane mainContent;
    private final BorderPane detailsOuterBorderPane;
    private final KlWindowControlToolbar windowControlToolbar;
    /**
     * popup for the filter coordinates menu, used with the toolbar's coordinates menu button.
     * An instance of FilterOptionsPopup.
     */
    private FilterOptionsPopup filterOptionsPopup;
    /**
     * Used slide out the properties view
     */
    private final VerticallyFilledPane propertiesSlideoutTrayPane;
    private final ComponentItemNode windowConceptTitle;
    private final Tooltip windowConceptTitleTooltip;
    private final PublicIDListControl identifierControl;
    private final Label createModeHintLabel;
    /** Strip naming the changes not published yet (see {@link #updatePublishState}). */
    private final HBox unpublishedHint;
    private final Label unpublishedHintLabel;
    private final javafx.scene.control.Hyperlink unpublishedDiscardLink;
    private PropertiesTray propertiesTray;
    private GenPurposePropertiesController propertiesController;
    /** Grows the window to fit the open properties panel, and gives the height back as it closes. */
    private WindowHeightFitter windowHeightFitter;
    /** The KL-editor window definition this window is built from; shared with the editor while both are open. */
    private final EditorWindowModel editorWindowModel;
    /**
     * The kind of component this window frames ("Concept", "Pattern" or "Semantic") — from the
     * authored window type. Names the component in the create-mode hint and the publish toast.
     */
    private final String componentKindString;
    private final ViewProperties viewProperties;
    /** What the window is editing: its component and whether it exists yet. Shared with the panel's forms. */
    private final WindowEditSession session;
    /** The owning journal's topic, which the pattern presenters publish their events on. */
    private final UUID journalTopic;
    private Consumer<GenPurposeDetailsController> onCloseConceptWindow;

    /**
     * Re-evaluates the required chips when a stated definition changes: inline axiom edits
     * persist straight to the store without a PUBLISH event, yet they can satisfy — or, by
     * removing the definition's last set, un-satisfy — the stated pattern's requirement.
     * Strong reference: the entity provider holds its subscribers weakly.
     */
    private dev.ikm.tinkar.common.util.broadcast.Subscriber<Long> statedDefinitionChangeSubscriber;

    /**
     * Wires the window's behavior onto its view for the KL-editor window definition held at
     * {@code editorWindowPreferences}: the definition is loaded, the chrome wired and the
     * definition's sections built before this returns — nothing is left for a second phase.
     *
     * @param view                     the window's scene graph, already the window's root pane
     * @param session                  what the window is editing, on the window's derived coordinate
     *                                 (see {@code AbstractEntityChapterKlWindow})
     * @param journalTopic             the owning journal's topic
     * @param editorWindowPreferences  the {@code kl-editor-app/{user,standard}-windows/<title>} node of the definition
     */
    public GenPurposeDetailsController(GenPurposeWindowView view, WindowEditSession session, UUID journalTopic,
                                       KometPreferences editorWindowPreferences) {
        this.session = session;
        this.journalTopic = journalTopic;
        this.viewProperties = session.getViewProperties();
        this.detailsOuterBorderPane = view;
        this.windowControlToolbar = view.getWindowControlToolbar();
        this.stampViewControl = view.getStampViewControl();
        this.mainContent = view.getMainContent();
        this.propertiesSlideoutTrayPane = view.getPropertiesSlideoutTrayPane();
        this.windowConceptTitle = view.getWindowConceptTitle();
        this.windowConceptTitleTooltip = view.getWindowConceptTitleTooltip();
        this.identifierControl = view.getIdentifierControl();
        this.createModeHintLabel = view.getCreateModeHintLabel();
        this.unpublishedHint = view.getUnpublishedHint();
        this.unpublishedHintLabel = view.getUnpublishedHintLabel();
        this.unpublishedDiscardLink = view.getUnpublishedDiscardLink();
        unpublishedDiscardLink.setOnAction(event -> discardUnpublished());

        // The definition comes first: the chrome wiring below already asks its window type
        // (see usesPublishFlow).
        String windowTitle = Paths.get(editorWindowPreferences.absolutePath()).getFileName().toString();
        this.editorWindowModel = EditorWindowManager.loadWindowModel(editorWindowPreferences,
                viewProperties.calculator(), windowTitle);
        this.componentKindString = switch (editorWindowModel.getWindowType()) {
            case STANDARD_CONCEPT -> "Concept";
            case STANDARD_PATTERN -> "Pattern";
            case STANDARD_SEMANTIC, SEMANTICS -> "Semantic";
        };

        // In the Publish-flow window edits made inline in the details area (the stated definition's
        // axiom tree) are saved uncommitted like the properties panel's submits, until the toolbar's
        // Publish button commits them (see publish); elsewhere they commit as applied. The axiom
        // tree already shows the edit, so nothing re-renders for it; the required chips and the
        // Publish button follow through the stated-definition change subscriber.
        InlineEditSaver inlineEditSaver = usesPublishFlow() ? session::saveUncommittedFieldEdit : null;
        this.windowSections = new WindowSections(editorWindowModel, session, mainContent, journalTopic, inlineEditSaver);
        windowSections.setOnSectionExpandedChanged(this::resizeWindowWithSection);
        windowSections.setOnPublishInputsChanged(this::updatePublishState);

        this.sectionEditMenu = new SectionEditMenu(editorWindowModel, session, windowSections);
        sectionEditMenu.setOnEditSemantic(this::showEditSemanticFieldsPanel);
        sectionEditMenu.setOnCreateSemantic(this::onCreateSemantic);
        sectionEditMenu.setOnCreateStatedDefinition(this::createSeededStatedDefinition);
        windowSections.setOnSectionEditRequested(sectionEditMenu::open);
        view.getUnpublishedShowLink().setOnAction(event -> windowSections.revealFirstUnpublishedSemantic());

        wireChrome();
        buildWindowFromDefinition(windowTitle);
    }

    /**
     * Wires the window chrome: toolbar actions, the coordinates popup, create-mode and focus
     * styling, the properties panel and its events, window dragging.
     */
    private void wireChrome() {
        // Drive the coordinates menu from the relocated FilterOptionsPopup (ike-issues#661); the popup
        // writes the window's nodeView override, which the window's KL context + areas resolve through.
        filterOptionsPopup = setupViewCoordinateOptionsPopup(viewProperties,
                FilterOptionsPopup.FILTER_TYPE.CHAPTER_WINDOW, detailsOuterBorderPane,
                windowControlToolbar.getCoordinatesMenuButton(), this::updateView);

        // Wire the toolbar's behaviour: the close and Publish buttons take actions, while the
        // properties panel reacts to the toggle's selected state (driven by user clicks or
        // setPropertiesSelected).
        windowControlToolbar.setOnCloseAction(this::closeConceptWindow);
        windowControlToolbar.setOnPublishAction(this::publish);
        windowControlToolbar.setOnFieldDefaultsAction(this::openPatternFieldDefaults);
        // The Publish button follows the session's uncommitted changes (see updatePublishState).
        session.hasUncommittedChangesProperty().subscribe(this::updatePublishState);
        // Invalidation-based, so it reacts to changes only: the tray it drives is created after
        // the chrome (see setupProperties), and the panel starts out closed like the toggle.
        windowControlToolbar.propertiesSelectedProperty()
                .subscribe(() -> onPropertiesToggleChanged(windowControlToolbar.isPropertiesSelected()));

        // The header STAMP is view-only in this window — clicking it must not select it or open
        // the STAMP form.
        stampViewControl.setSelectable(false);

        // Ghost-window styling while in create mode: the window frames a component that doesn't
        // exist yet, so the chrome dims and the frame dashes (see :create-mode in kview.css) and
        // the DRAFT chip + hint appear. Publishing flips the mode to EDIT, which clears all of it.
        session.modeProperty().subscribe(mode -> {
            boolean creating = mode == FormMode.CREATE;
            detailsOuterBorderPane.pseudoClassStateChanged(CREATE_MODE, creating);
            windowControlToolbar.setDraftVisible(creating);
            createModeHintLabel.setVisible(creating);
            createModeHintLabel.setManaged(creating);
            updateRequiredChips();
        });

        // Setup Properties Bump out view
        setupProperties();

        // Selecting the panel's DEFAULTS tab loads the pattern's field defaults into it, and its
        // INSTANCES tab the pattern's instances (both tabs are only offered in the standard Pattern
        // window once the pattern exists, see init).
        PropertiesTabsControl propertiesTabs = propertiesController.getPropertiesTabs();
        propertiesTabs.selectedTabProperty().subscribe(() -> {
            if (propertiesTabs.getSelectedTab() == Tab.DEFAULTS) {
                showPatternFieldDefaults();
            } else if (propertiesTabs.getSelectedTab() == Tab.INSTANCES) {
                showPatternInstances();
            }
        });

        // This window opts out of WindowSupport's hover/resize outline: the edge resize cursors
        // are the resize affordance. Must be set before addDraggableNodes below creates the
        // window support.
        detailsOuterBorderPane.getProperties().put(WindowSupport.HIGHLIGHT_DISABLED_KEY, Boolean.TRUE);

        // Setup window support with explicit draggable nodes. The toolbar's own title tab is
        // part of the toolbar control, so it drags the window through the toolbar handle.
        addDraggableNodes(detailsOuterBorderPane, windowControlToolbar);

        // Window focus shadow: the window counts as focused while keyboard focus is anywhere
        // inside it (focus-within). A mouse press anywhere in the window routes focus to the
        // window root first — the filter runs before the pressed control claims focus for
        // itself — so clicking non-focusable areas also focuses the window. Focus moving into
        // another window clears it. Drives :window-focused in kview.css.
        detailsOuterBorderPane.focusWithinProperty().subscribe(() ->
                detailsOuterBorderPane.pseudoClassStateChanged(WINDOW_FOCUSED,
                        detailsOuterBorderPane.isFocusWithin()));
        detailsOuterBorderPane.addEventFilter(MouseEvent.MOUSE_PRESSED, _ -> {
            if (!detailsOuterBorderPane.isFocusWithin()) {
                detailsOuterBorderPane.requestFocus();
            }
        });
    }

    /**
     * Opens the properties panel: the toolbar's Properties toggle follows, the tray slides out and
     * the window grows to fit what the panel shows.
     */
    private void openPropertiesPanel() {
        LOG.info("propBumpOutListener - Opening Properties bumpout toggle = " + windowControlToolbar.isPropertiesSelected());

        windowControlToolbar.setPropertiesSelected(true);
        propertiesTray.open();

        // The panel's content may have been swapped in this same pulse, ahead of the layout pass
        // that measures it — measure it now, so the window grows for what is about to show.
        propertiesController.refreshRequiredHeight();
        growWindowToFitProperties();
    }

    /**
     * Closes the properties panel: the toolbar's Properties toggle follows, the tray slides back
     * in and the window gets back the height it had before the panel grew it.
     */
    private void closePropertiesPanel() {
        LOG.info("propBumpOutListener - Close Properties bumpout toggle = " + windowControlToolbar.isPropertiesSelected());

        windowControlToolbar.setPropertiesSelected(false);
        propertiesTray.close();
        windowHeightFitter.restorePreviousHeight();
    }

    /**
     * Grows the window to fit its properties panel while the panel is open (see
     * {@link WindowHeightFitter}); closing the panel gives the height back.
     */
    private void growWindowToFitProperties() {
        if (windowControlToolbar.isPropertiesSelected()) {
            windowHeightFitter.growToFitProperties();
        }
    }

    /**
     * Runs when the toolbar's Properties toggle changes, by a user click or by
     * {@code setPropertiesSelected}: the panel follows the toggle.
     *
     * @param selected the new selected state of the properties toggle
     */
    private void onPropertiesToggleChanged(boolean selected) {
        if (selected) {
            openPropertiesPanel();
        } else {
            closePropertiesPanel();
        }
    }

    /// Show the public ID
    private void updateDisplayIdentifier(EntityFacade refComponent) {
        ViewCalculator viewCalculator = getViewProperties().calculator();
        identifierControl.updatePublicIdList(viewCalculator, refComponent);
    }

    private void updateStampControl(EntityFacade refConcept) {
        ObservableEntity observableEntity = ObservableEntityHandle.get(refConcept.nid()).expectEntity();
        ObservableEntitySnapshot observableEntitySnapshot;
        try {
            observableEntitySnapshot = observableEntity.getSnapshot(viewProperties.calculator());
        } catch (IllegalStateException e) {
            // No version of the concept passes the current view coordinate (e.g. a status/path filter that
            // excludes every version); leave the stamp control as-is rather than throwing out of the
            // coordinate-change listener chain — which would also abort the supplemental areas' re-render
            // (ike-issues#666).
            LOG.debug("No latest version for nid {} under the current view coordinate; skipping stamp update",
                    refConcept.nid());
            return;
        }
        Latest<EntityVersion> latestEntityVersion = retrieveCommittedLatestVersion(observableEntitySnapshot);
        latestEntityVersion.ifPresent(latestVersion -> {
            StampEntity stampEntity = latestEntityVersion.get().stamp();

            // -- status
            State newStatus = stampEntity.state();
            String statusMsg = newStatus == null ? "Active" : getViewProperties().calculator().getPreferredDescriptionTextWithFallbackOrNid(((State) newStatus).nid());
            stampViewControl.setStatus(statusMsg);

            // -- time
            long newTime = stampEntity.time();
            stampViewControl.setLastUpdated(DateTimeUtil.format(newTime, DateTimeUtil.DAY_FORMATTER));

            // -- author
            ConceptFacade authorConcept = stampEntity.author();
            String authorDescription = ViewCalculatorUtils.getDescriptionTextWithFallbackOrNid(authorConcept, getViewProperties());
            stampViewControl.setAuthor(authorDescription);

            // -- module
            ConceptFacade newModule = stampEntity.module();
            String newModuleDescription;
            if (newModule == null) {
                newModuleDescription = "";
            } else {
                newModuleDescription = getViewProperties().calculator().getPreferredDescriptionTextWithFallbackOrNid((newModule).nid());
            }
            stampViewControl.setModule(newModuleDescription);

            // -- path
            ConceptFacade newPath = stampEntity.path();
            String pathDescr;
            if (newPath == null) {
                pathDescr = "";
            } else {
                pathDescr = getViewProperties().calculator().getPreferredDescriptionTextWithFallbackOrNid((newPath).nid());
            }
            stampViewControl.setPath(pathDescr);
        });
    }

    /**
     * Populates the header STAMP control from the edit coordinate — the author, module and path a
     * newly created component will be committed with, and the Active status it will be committed
     * as. Used in create mode, where no committed STAMP exists yet to read those values from.
     */
    private void populateStampFromEditCoordinate() {
        var editCoordinate = getViewProperties().nodeView().editCoordinate();

        stampViewControl.setStatus(getViewProperties().calculator()
                .getPreferredDescriptionTextWithFallbackOrNid(State.ACTIVE.nid()));

        ConceptFacade author = editCoordinate.getAuthorForChanges();
        stampViewControl.setAuthor(ViewCalculatorUtils.getDescriptionTextWithFallbackOrNid(author, getViewProperties()));

        ConceptFacade module = editCoordinate.defaultModuleProperty().get();
        stampViewControl.setModule(ViewCalculatorUtils.getDescriptionTextWithFallbackOrNid(module, getViewProperties()));

        ConceptFacade path = editCoordinate.defaultPathProperty().get();
        stampViewControl.setPath(ViewCalculatorUtils.getDescriptionTextWithFallbackOrNid(path, getViewProperties()));
    }

    private void updateWindowTitle(EntityFacade refConcept) {
        // Follow the view coordinate's description-type preference (FQN vs preferred), like the axiom
        // badges, so the header tracks the coordinate too (ike-issues#660).
        String conceptNameStr = getViewProperties().calculator().getDescriptionTextOrNid(refConcept.nid());
        Image identicon = Identicon.generateIdenticonImage(refConcept.publicId());

        boolean isConcept = EntityHandle.get(refConcept).isConcept();
        ComponentItem componentItem = new ComponentItem(conceptNameStr, identicon, refConcept.publicId(), isConcept);

        windowConceptTitle.setComponentItem(componentItem);

        windowConceptTitleTooltip.setText(conceptNameStr);
    }

    /**
     * Creates the filter coordinates menu using the view calculator.
     * TODO Note that this is not a working menu, this is the first step to have propagating, inherited, filter coordinates
     * in the window/node hierarchy.
     */
    public void setupFilterCoordinatesMenu() {
//        this.viewMenuModel = new ViewMenuModel(patternViewModel.getViewProperties(), coordinatesMenuButton, "PatternDetailsController");
    }

    /**
     * Creates a transaction and uncommited Semantic.
     *
     * @param referenceComponent the Reference Component of the Semantic that is going to be created
     * @param pattern the Pattern of the Semantic that is going to be created
     * @param fieldSeeds the concepts to seed fields with, keyed by the field's index in the Pattern
     *                   — a display filter's constraints (see {@link #addCreateEntries})
     * @return The uncommited Semantic
     */
    private SemanticEntity<SemanticEntityVersion> createUncommitedSemantic(EntityFacade referenceComponent, PatternFacade pattern,
            Map<Integer, EntityFacade> fieldSeeds) {
        return session.createSemanticInSingleSemanticTransaction(referenceComponent, pattern.nid(), editableFields -> {
            // Start from the pattern's field defaults — the values a new semantic of this pattern
            // begins with in every window (see PatternFieldDefaults) — before the create entry's
            // filter seeds below, which take precedence over them.
            PatternFieldDefaults.applyDefaults(editableFields,
                    PatternFieldDefaults.defaultValues(pattern.nid(), getViewProperties().calculator()).castToList());

            // Seed the fields the create entry's display filter constrains, so the new semantic passes
            // that filter and shows up in the filtered view it was created from.
            fieldSeeds.forEach((fieldIndex, filterConcept) -> {
                @SuppressWarnings("unchecked")
                ObservableField.Editable<EntityProxy> editableField = (ObservableField.Editable<EntityProxy>)
                        editableFields.get(fieldIndex);
                editableField.setValue(filterConcept.toProxy());
            });
        });
    }

    /**
     * Creates the window's reference component as a new, uncommitted entity — used in create mode,
     * where the window was opened without one. The component kind follows the authored window type:
     * the standard Pattern window creates a Pattern, all others create a Concept.
     *
     * @return the new uncommitted component, already set as the window's reference component
     */
    private EntityFacade createUncommitedReferenceComponent() {
        return session.createUncommittedComponent(editorWindowModel.getWindowType() == EditorWindowType.STANDARD_PATTERN);
    }

    /**
     * Creates the stated definition semantic seeded with one necessary or sufficient set — the
     * classic concept window's "Add Necessary Set" / "Add Sufficient Set" actions: the set holds
     * an is-a to "Anonymous concept", the placeholder chip the user then replaces in the inline
     * axiom tree. The new semantic is submitted through the window's PUBLISH flow — the same
     * path a properties-panel submit takes: it stays uncommitted in the composer's open transaction until
     * the toolbar's Publish button commits it ({@link #publish}), and the details area re-renders
     * bound to the new semantic. In create mode the window may not have a reference component
     * yet — the seeded definition brings it into existence, exactly like authoring the first
     * semantic through the section pencil ({@link #onCreateSemantic}).
     */
    private void createSeededStatedDefinition(EditorSectionModel section, EditorPatternModel patternModel,
            boolean necessary) {
        EntityFacade refComponent = windowSections.resolveReferenceComponent(section);
        if (refComponent == null) {
            refComponent = createUncommitedReferenceComponent();
        }

        DiTreeEntity seededDefinition = StatedDefinitionSeeds.seedDefinition(necessary);

        SemanticEntity<SemanticEntityVersion> semantic = session.createSemanticInWindowTransaction(refComponent,
                patternModel.getNid(), editableFields -> {
                    // The stated axiom pattern's single field holds the definition.
                    @SuppressWarnings("unchecked")
                    ObservableField.Editable<DiTreeEntity> definitionField =
                            (ObservableField.Editable<DiTreeEntity>) editableFields.getFirst();
                    definitionField.setValue(seededDefinition);
                });

        // Submitted like a properties-panel submit. Outside the Publish flow that commits when the
        // seeded set was the last unmet requirement, creating the concept — announce it like the
        // panel's submit does. (In the Publish-flow window the concept is only created by the
        // toolbar's Publish button, so this stays quiet.)
        if (onSemanticSubmitted(semantic) == SubmitOutcome.COMPONENT_CREATED) {
            toast().show(Toast.Status.SUCCESS, "Concept created");
        }
    }

    /**
     * Runs when a semantic is submitted from the properties panel's edit form (or seeded by
     * {@link #createSeededStatedDefinition}), its edited values already saved as an uncommitted
     * version. Decides whether that version commits now and refreshes the details area either way.
     *
     * <p>In the Publish-flow window a submit only saves the change uncommitted: the saved version stays in
     * the composer's open transaction, alongside, in create mode, the lazily created reference
     * component, until the toolbar's Publish button commits everything together ({@link #publish}).
     * The details area refreshes so the submitted field values show, the required chips
     * re-evaluate and the Publish button follows the uncommitted changes.
     *
     * <p>Without the Publish flow, the submit itself commits. In create mode the component only
     * truly gets created once every required pattern has at least one semantic. Until then the
     * commit is deferred: the submitted semantic stays uncommitted in the composer's open
     * transaction (alongside the lazily created reference concept) and commits together with it
     * later. The details area still refreshes so the submitted (still uncommitted) values show.
     *
     * @return what became of the submitted semantic, for the form's toast
     */
    SubmitOutcome onSemanticSubmitted(SemanticEntity<SemanticEntityVersion> semantic) {
        if (unsubmittedNewSemanticNids.remove(semantic.nid())) {
            session.endSingleSemanticTransactionOnSubmit(semantic);
        }

        if (usesPublishFlow()) {
            windowSections.reloadSemanticViews(semantic);
            updateRequiredChips();
            return SubmitOutcome.UNCOMMITTED_UNTIL_PUBLISHED;
        }

        if (session.isCreateMode() && !windowSections.allRequiredPatternsSatisfied()) {
            windowSections.reloadSemanticViews(semantic);
            // The submitted semantic now shows in the details area — flip its section's chip to MET.
            updateRequiredChips();
            return SubmitOutcome.UNCOMMITTED_UNTIL_REQUIREMENTS_MET;
        }

        // Commit transaction, finalizing all impending changes
        session.commit();

        // In create mode that commit also finalized the window's lazily created reference
        // component (see createUncommitedReferenceComponent) — the window is now editing a
        // real component, so refresh the banner/identifier/STAMP from the committed entity.
        boolean componentCreated = session.isCreateMode();
        if (componentCreated) {
            session.enterEditMode();
            updateView();
        }

        windowSections.reloadSemanticViews(semantic);
        return componentCreated ? SubmitOutcome.COMPONENT_CREATED : SubmitOutcome.COMMITTED;
    }

    /**
     * Runs when the properties panel's edit form is cancelled, its fields already put back. A
     * semantic the form was creating — added by an "Add …" entry and never submitted — is
     * discarded: canceled in the session's transaction and gone from the details area, from the
     * reference-component options it was one of, and from the required chips. For any other
     * semantic only the form's unsaved edits are dropped from the transaction; what the window
     * shows stays as it is. Either way the semantic stops showing as being edited.
     */
    private void onSemanticCancelled(SemanticEntity<SemanticEntityVersion> semantic) {
        if (unsubmittedNewSemanticNids.remove(semantic.nid())) {
            session.cancelSingleSemanticTransaction(semantic);
            windowSections.reloadSemanticViews(semantic);
            windowSections.refreshSectionsReferencing(referencePattern -> referencePattern.getNid() == semantic.patternNid());
            updateRequiredChips();
        } else {
            session.discardUnsavedEdits(semantic);
        }

        windowSections.setEditingSemantic(null);
    }

    private void setupProperties() {
        this.propertiesController = new GenPurposePropertiesController(session);
        // The panel's edit form hands its submitted or cancelled semantic here, and its forms close
        // the panel once they are done (submit, cancel).
        propertiesController.setOnSemanticSubmitted(this::onSemanticSubmitted);
        propertiesController.setOnSemanticCancelled(this::onSemanticCancelled);
        propertiesController.setOnCloseRequested(this::closePropertiesPanel);
        // An instance double-clicked on the INSTANCES tab opens in a semantic window of the journal.
        propertiesController.setOnInstanceOpened(this::openInstanceWindow);
        // The panel's tabs are its drag handle: while the tray is open they drag the window too.
        this.propertiesTray = new PropertiesTray(detailsOuterBorderPane, propertiesSlideoutTrayPane,
                propertiesController.getNode(), propertiesController.getPropertiesTabs());

        // Follow the open panel's content: a form loaded, or swapped for a taller one, grows the window.
        windowHeightFitter = new WindowHeightFitter(detailsOuterBorderPane, propertiesSlideoutTrayPane,
                propertiesController.requiredHeightProperty());
        propertiesController.requiredHeightProperty().subscribe(_ -> growWindowToFitProperties());
    }

    public ViewProperties getViewProperties() {
        return viewProperties;
    }

    public void setOnCloseConceptWindow(Consumer<GenPurposeDetailsController> onClose) {
        this.onCloseConceptWindow = onClose;
    }

    void closeConceptWindow() {
        LOG.info("Cleanup occurring: Closing Window: " + windowConceptTitle.getComponentItem());

        if (this.onCloseConceptWindow != null) {
            onCloseConceptWindow.accept(this);
        }
    }

    /**
     * Builds the window from the loaded KL-editor window definition: the chrome and properties tabs its window
     * type calls for, the create-mode hint, the authored window settings, and the sections with
     * their patterns and supplemental areas.
     *
     * @param windowTitle the definition's title, as the toolbar's title tab shows it
     */
    private void buildWindowFromDefinition(String windowTitle) {
        windowControlToolbar.setTitle(windowTitle.substring(0, 1).toUpperCase() + windowTitle.substring(1));

        // The standard Concept window gets the classic concept window's blue chrome (see
        // .concept-window-theme in kview.css) and its own set of properties tabs. User-created
        // Semantics Windows get the light chrome (.custom-window-theme) that sets them apart from
        // the standard windows; the other standard windows keep the default grey chrome and tabs.
        if (editorWindowModel.getWindowType() == EditorWindowType.STANDARD_CONCEPT) {
            detailsOuterBorderPane.getStyleClass().add("concept-window-theme");
            propertiesController.getPropertiesTabs().getTabs().setAll(
                    Tab.ADD_EDIT, Tab.HIERARCHY, Tab.HISTORY, Tab.COMMENTS);
        } else if (editorWindowModel.getWindowType() == EditorWindowType.SEMANTICS) {
            detailsOuterBorderPane.getStyleClass().add("custom-window-theme");
        } else if (editorWindowModel.getWindowType() == EditorWindowType.STANDARD_PATTERN) {
            // The standard Pattern window also edits its pattern's field defaults — the values
            // new semantics of the pattern start with in every KL window (see PatternFieldDefaults)
            // — and lists its instances — once the pattern exists: the INSTANCES and DEFAULTS tabs and
            // the toolbar's field-defaults button appear when the window leaves create mode.
            session.modeProperty().subscribe(mode -> {
                boolean patternExists = mode == FormMode.EDIT;
                propertiesController.getPropertiesTabs().getTabs().setAll(patternExists
                        ? List.of(Tab.ADD_EDIT, Tab.INSTANCES, Tab.DEFAULTS, Tab.COMMENTS)
                        : List.of(Tab.ADD_EDIT, Tab.COMMENTS));
                windowControlToolbar.setFieldDefaultsVisible(patternExists);
            });
        }

        // The create-mode hint names the kind of component this window will create.
        createModeHintLabel.setText("This " + componentKindString
                + " doesn't exist yet - it's created when you fill out the required values and "
                + (usesPublishFlow() ? "hit Publish." : "submit."));

        // The Publish UX — the toolbar Publish button and uncommitted-until-published changes — is
        // scoped to the standard Pattern and Concept windows for now; the other window types keep
        // committing on each properties-panel submit (see the PUBLISH event handler and the
        // fields controller's submit toast, both of which branch on this).
        windowControlToolbar.setPublishVisible(usesPublishFlow());

        // Apply the Window settings authored in the KL editor (this window shares the same model).
        applyEditorWindowSettings();

        windowSections.build();

        // Initial view update
        updateView();

        if (session.isCreateMode()) {
            populateStampFromEditCoordinate();
        }

        // Sections exist now — show the required-pattern chips (create mode only).
        updateRequiredChips();

        // Inline stated-definition edits persist without a PUBLISH event, so track them here:
        // a change to any stated-axiom semantic re-evaluates the required chips.
        statedDefinitionChangeSubscriber = changedNid -> EntityHandle.get(changedNid).asSemantic()
                .filter(semantic -> semantic.patternNid() == windowSections.statedAxiomsPatternNid())
                .ifPresent(semantic -> Platform.runLater(this::updateRequiredChips));
        Entity.provider().addSubscriberWithWeakReference(statedDefinitionChangeSubscriber);
    }

    /**
     * Applies the Window settings authored in the KL editor — the control-bar options (Coordinate and
     * Timeline icons) and the Window's view size — to this Window. The editor and this Window
     * share the same {@link EditorWindowModel} instance, so edits made while both are open take effect
     * live. An "Auto" size ({@link EditorWindowModel#AUTO_SIZE}) leaves the Window's own (workspace)
     * sizing in charge.
     */
    private void applyEditorWindowSettings() {
        // Control-bar options: show/hide the Coordinate and Timeline icons.
        windowControlToolbar.coordinateVisibleProperty().bind(editorWindowModel.coordinateVisibleProperty());
        windowControlToolbar.timelineVisibleProperty().bind(editorWindowModel.timelineVisibleProperty());

        // View size: apply an explicit Width/Height. "Auto" (< 0) leaves the workspace's own sizing
        // alone. The framework sizes this same root pane via setPrefWidth/Height, so we set (not bind) it.
        // We also advertise the authored size on the pane's properties so the workspace honors it when
        // placing the window (otherwise its default placement overrides our preferred width/height).
        editorWindowModel.prefWidthProperty().subscribe(width -> {
            final double w = width.doubleValue();
            if (w >= 0) {
                detailsOuterBorderPane.setPrefWidth(w);
                detailsOuterBorderPane.getProperties().put(KLWorkspace.WINDOW_AUTHORED_WIDTH_KEY, w);
            } else {
                detailsOuterBorderPane.getProperties().remove(KLWorkspace.WINDOW_AUTHORED_WIDTH_KEY);
            }
        });
        editorWindowModel.prefHeightProperty().subscribe(height -> {
            final double h = height.doubleValue();
            if (h >= 0) {
                detailsOuterBorderPane.setPrefHeight(h);
                detailsOuterBorderPane.getProperties().put(KLWorkspace.WINDOW_AUTHORED_HEIGHT_KEY, h);
            } else {
                detailsOuterBorderPane.getProperties().remove(KLWorkspace.WINDOW_AUTHORED_HEIGHT_KEY);
            }
        });
    }

    /**
     * Called to update the view when coordinate changes occur.
     */
    private void updateView() {
        LOG.info("Update view called - implement coordinate changes here.");
        EntityFacade refConcept = session.getComponent();
        if (refConcept != null) {
            updateDisplayIdentifier(refConcept);
            updateWindowTitle(refConcept);
            updateStampControl(refConcept);
        } else {
            LOG.warn("The window has no component yet, cannot update view.");
        }
    }

    /**
     * Closes or opens the window by the height a section's content occupies, as that section
     * collapses or expands. Without this a window carrying a height of its own — one restored from a
     * saved state, or one the user has resized — keeps that height, and the height the collapsed
     * section gave up simply becomes blank space at the bottom of the last section
     * (komet-desktop#159).
     *
     * <p>A window that carries no height of its own is sized to its content, so it follows the
     * collapse on its own and is left alone.
     */
    private void resizeWindowWithSection(SectionTitledPane<EntityFacade> section, boolean expanded) {
        windowHeightFitter.finishAnimationNow();
        final double windowHeight = detailsOuterBorderPane.getPrefHeight();
        if (windowHeight <= 0 || !(section.getContent() instanceof Region content)) {
            return;
        }

        final double delta;
        if (expanded) {
            // Give back exactly what collapsing took away. A section that opens for the first time
            // (one that started collapsed) never gave anything up, so it takes what its content asks for.
            Object heightGivenUp = section.getProperties().remove(COLLAPSED_CONTENT_HEIGHT_KEY);
            delta = heightGivenUp instanceof Number number
                    ? number.doubleValue()
                    : content.prefHeight(content.getWidth());
        } else {
            delta = -content.getHeight();
            section.getProperties().put(COLLAPSED_CONTENT_HEIGHT_KEY, content.getHeight());
        }

        // Through the fitter, so a window grown to fit its properties panel takes the section's
        // height out of (or into) the height closing the panel restores as well.
        windowHeightFitter.changeHeightBy(delta);
    }

    private void onCreateSemantic(EditorSectionModel sectionModelOfPattern,
                                  EditorPatternModel editorPatternModel, EntityFacade refComponent,
                                  Map<Integer, EntityFacade> fieldSeeds) {
        // Lazy reference-component creation: in create mode the window has no component yet — the
        // first semantic the user authors brings the new component into existence with it. Both
        // join the composer's transaction, so submitting the semantic commits them together.
        if (refComponent == null) {
            refComponent = createUncommitedReferenceComponent();
        }

        PatternFacade patternFacade = PatternFacade.make(editorPatternModel.getNid());

        // Create uncommited Semantic
        SemanticEntity<SemanticEntityVersion> uncommitedSemantic = createUncommitedSemantic(refComponent, patternFacade, fieldSeeds);
        unsubmittedNewSemanticNids.add(uncommitedSemantic.nid());

        windowSections.showNewSemantic(sectionModelOfPattern, editorPatternModel, uncommitedSemantic);

        // Show Edit Panel to the right
        showEditSemanticFieldsPanel(uncommitedSemantic, editorPatternModel);
    }

    /**
     * Opens the properties panel on the semantic's edit form. The pattern model is the KL Editor's
     * placement of the semantic's pattern, whose fields say how the form's fields behave (e.g.
     * whether they can still be edited in edit mode).
     */
    private void showEditSemanticFieldsPanel(SemanticEntity<SemanticEntityVersion> semanticEntity,
                                             EditorPatternModel editorPatternModel) {
        propertiesController.showEditForm(semanticEntity, editorPatternModel);
        openPropertiesPanel();

        // Turn on Edit mode on the left side for the Semantic being edited
        windowSections.setEditingSemantic(semanticEntity);
    }

    /**
     * The toolbar's field-defaults button: selects the panel's DEFAULTS tab, whose selection loads
     * the defaults (see {@link #setupProperties}). When the tab is already selected — the panel
     * remembers it across a close — there is no selection change to react to, so the defaults
     * are loaded directly.
     */
    private void openPatternFieldDefaults() {
        PropertiesTabsControl propertiesTabs = propertiesController.getPropertiesTabs();
        if (propertiesTabs.getSelectedTab() == Tab.DEFAULTS) {
            showPatternFieldDefaults();
        } else {
            propertiesTabs.setSelectedTab(Tab.DEFAULTS);
        }
    }

    /**
     * Opens the properties panel on its DEFAULTS tab, showing the window pattern's
     * defaults semantic — created, uncommitted, when the pattern has none yet — in the regular semantic
     * form: one editor per field of the pattern, a field left blank meaning no default. Runs when
     * the panel's DEFAULTS tab is selected (see {@link #setupProperties}); only the standard
     * Pattern window in edit mode offers the tab. The form's own Publish button commits the edit
     * through the defaults composer.
     */
    private void showPatternFieldDefaults() {
        if (session.getComponent() == null) {
            // Create mode: the pattern doesn't exist yet, so neither can its defaults (the tab and
            // button only show once it does — this is the tab remembered across a panel reopen).
            return;
        }
        propertiesController.showDefaultsForm(session.getOrCreateDefaultsSemantic(), session.getDefaultsComposer(),
                "Field Default Values");
        openPropertiesPanel();
    }

    /**
     * Opens the properties panel on its INSTANCES tab, listing the window pattern's semantics. Runs
     * when the panel's INSTANCES tab is selected (see {@link #setupProperties}); only the standard
     * Pattern window in edit mode offers the tab.
     */
    private void showPatternInstances() {
        if (session.getComponent() == null) {
            // Create mode: the pattern doesn't exist yet, so it has no instances (the tab only shows
            // once it does — this is the tab remembered across a panel reopen).
            return;
        }
        propertiesController.showInstances(session.getComponent());
        openPropertiesPanel();
    }

    /**
     * Opens a semantic (an instance listed on the INSTANCES tab) in the journal's gen editing
     * window — the window the next-gen search opens a semantic in, there being no KL-driven window
     * showing a single semantic — with its own overridable coordinate derived from the journal's.
     */
    private void openInstanceWindow(EntityFacade instance) {
        ViewProperties windowViewProperties = viewProperties.parentView()
                .makeOverridableViewProperties("GenPurposeDetailsController.openInstanceWindow");
        EvtBusFactory.getDefaultEvtBus().publish(journalTopic, new MakeGenEditingWindowEvent(this,
                MakeGenEditingWindowEvent.OPEN_GEN_EDIT, instance, windowViewProperties));
    }

    /**
     * Whether this window uses the toolbar Publish flow: changes stay uncommitted in the composer's open
     * transaction until the Publish button commits them. Scoped to the standard Pattern and
     * Concept windows for now — the other window types keep the classic commit-on-submit flow
     * (and hide the Publish button) until they adopt the Publish UX too.
     */
    private boolean usesPublishFlow() {
        return editorWindowModel.getWindowType() == EditorWindowType.STANDARD_PATTERN
                || editorWindowModel.getWindowType() == EditorWindowType.STANDARD_CONCEPT;
    }

    /**
     * Recomputes the toolbar Publish button's enablement and tooltip. Publishing needs uncommitted
     * changes in the composer's open transaction, and in create mode additionally every required
     * pattern satisfied ({@link #allRequiredPatternsSatisfied}) — the component only comes into
     * existence complete. Runs whenever those inputs may have moved: with the required chips
     * (mode changes, PUBLISH submits, inline stated-definition edits) and on the composer's
     * change tracking (see {@link WindowEditSession#hasUncommittedChangesProperty}).
     */
    /**
     * Refreshes each section's required-pattern chip (see {@link WindowSections#updateRequiredChips})
     * and, since the same check gates the Publish button in create mode, the button with them.
     */
    private void updateRequiredChips() {
        windowSections.updateRequiredChips();
        updatePublishState();
    }

    private void updatePublishState() {
        boolean hasUncommittedChanges = session.hasUncommittedChanges();
        boolean disabled;
        String publishTooltip;
        int unpublishedChanges = 0;
        if (session.isCreateMode()) {
            disabled = !hasUncommittedChanges || !windowSections.allRequiredPatternsSatisfied();
            publishTooltip = disabled ? "Complete the required semantics to publish" : "Publish";
        } else {
            // Changes saved but not published count whether this window instance saved them or
            // an earlier one did (the window was closed and reopened): they are read from the
            // store, not from the composer.
            unpublishedChanges = unpublishedChangeCount();
            disabled = !hasUncommittedChanges && unpublishedChanges == 0;
            publishTooltip = disabled ? "No changes to publish"
                    : unpublishedChanges == 0 ? "Publish"
                    : "Publish " + unpublishedChanges + (unpublishedChanges == 1 ? " change" : " changes");
        }
        windowControlToolbar.setPublishDisable(disabled);
        windowControlToolbar.setPublishTooltip(publishTooltip);

        // The strip under the toolbar names the changes not published yet. Not in create mode,
        // whose DRAFT chip and hint already say the whole component is unpublished.
        boolean showHint = unpublishedChanges > 0;
        unpublishedHint.setVisible(showHint);
        unpublishedHint.setManaged(showHint);
        unpublishedDiscardLink.setVisible(!session.isCreateMode());
        unpublishedDiscardLink.setManaged(!session.isCreateMode());
        if (showHint) {
            unpublishedHintLabel.setText(unpublishedChanges == 1
                    ? "1 change not published yet. Only you can see it until you publish."
                    : unpublishedChanges + " changes not published yet. Only you can see them until you publish.");
        }

        windowSections.updateUnpublishedChips();
    }

    /**
     * How many of the components this window shows carry a version not published yet: the
     * unpublished semantics plus, when its own latest version is unpublished, the window's
     * reference component.
     */
    private int unpublishedChangeCount() {
        int count = windowSections.unpublishedSemantics().size();
        EntityFacade refComponent = session.getComponent();
        if (refComponent != null && EntityHandle.get(refComponent.nid()).expectEntity().uncommitted()) {
            count++;
        }
        return count;
    }

    /**
     * Runs when the toolbar's Publish button is pressed — the window's single commit point.
     * Commits the composer's open transaction, finalizing everything uncommitted since the last
     * publish: submitted semantic versions and, in create mode, the lazily created reference
     * component itself. A CREATE window becomes an EDIT window on its first publish.
     * <p>
     * Versions saved uncommitted by an earlier instance of this window (closed before publishing) sit in
     * that instance's still-open transactions; those are committed too, so a reopened window
     * publishes everything it shows as not published.
     */
    /**
     * The unpublished strip's "Discard" action: once the person confirms, cancels the changes not
     * published yet — the counterpart of {@link #publish()}, for what this window, an earlier one, or
     * a session before a restart saved (IKE-Network/ike-issues#1308). Not offered in create mode,
     * where discarding would remove the window's own component; cancelling the window does that.
     */
    private void discardUnpublished() {
        int changes = unpublishedChangeCount();
        if (session.isCreateMode() || changes == 0) {
            return;
        }
        javafx.scene.control.Alert confirm = new javafx.scene.control.Alert(javafx.scene.control.Alert.AlertType.CONFIRMATION,
                (changes == 1 ? "Discard the change" : "Discard the " + changes + " changes")
                        + " not published yet? This cannot be undone.",
                javafx.scene.control.ButtonType.CANCEL, javafx.scene.control.ButtonType.OK);
        confirm.setHeaderText(null);
        confirm.setTitle("Discard unpublished changes");
        if (confirm.showAndWait().filter(javafx.scene.control.ButtonType.OK::equals).isEmpty()) {
            return;
        }
        session.cancelUnpublishedTransactions(windowSections.unpublishedSemantics());
        unsubmittedNewSemanticNids.clear();
        updateView();
        windowSections.reloadAllSemanticViews();
        updatePublishState();
    }

    private void publish() {
        boolean wasCreateMode = session.isCreateMode();

        // The standard Pattern window authors its pattern's definition as semantics; the pattern's
        // own version record has to say the same before it commits (see writeInlinePatternDefinition).
        if (editorWindowModel.getWindowType() == EditorWindowType.STANDARD_PATTERN) {
            session.writeInlinePatternDefinition();
        }

        session.commit();

        int unpublishable = session.commitUnpublishedTransactions(windowSections.unpublishedSemantics());

        // A semantic whose form is still open when its creation publishes is published as it
        // stands; cancelling that form now only drops the form's edits.
        unsubmittedNewSemanticNids.clear();

        if (wasCreateMode) {
            session.enterEditMode();
        }
        // The commit finalized the uncommitted entities (in create mode the window's reference
        // component itself) — refresh the banner/identifier/STAMP from the committed state, and
        // the semantics so the versions just published drop their "Not published" marks.
        updateView();
        windowSections.reloadAllSemanticViews();
        updatePublishState();

        // The pattern navigator lists patterns from the committed store, so a pattern created
        // or changed here shows up there only once told the commit happened. Announce it the
        // way the classic pattern window does; the navigator reloads on this topic
        // (ikmdev/komet-desktop#191).
        if (editorWindowModel.getWindowType() == EditorWindowType.STANDARD_PATTERN) {
            EvtBusFactory.getDefaultEvtBus().publish(SAVE_PATTERN_TOPIC, new PatternSavedEvent(this,
                    wasCreateMode ? PatternSavedEvent.PATTERN_CREATION_EVENT : PatternSavedEvent.PATTERN_UPDATE_EVENT));
        }

        if (unpublishable > 0) {
            toast().show(Toast.Status.FAILURE, unpublishable == 1
                    ? "1 change could not be published: it was saved in an earlier session"
                    : unpublishable + " changes could not be published: they were saved in an earlier session");
        } else {
            toast().show(Toast.Status.SUCCESS,
                    wasCreateMode ? componentKindString + " created" : "Changes published");
        }

    }
}