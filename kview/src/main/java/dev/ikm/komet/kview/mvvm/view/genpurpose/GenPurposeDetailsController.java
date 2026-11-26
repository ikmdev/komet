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
import static dev.ikm.komet.kview.mvvm.view.journal.JournalController.toast;

import dev.ikm.komet.framework.observable.ObservableField;
import dev.ikm.komet.framework.view.ViewProperties;
import dev.ikm.komet.kview.controls.KLWorkspace;
import dev.ikm.komet.kview.controls.SectionTitledPane;
import dev.ikm.komet.kview.controls.Toast;
import dev.ikm.komet.kview.events.MakeKLWindowEvent;
import dev.ikm.komet.kview.events.genediting.MakeGenEditingWindowEvent;
import dev.ikm.komet.kview.events.pattern.PatternSavedEvent;
import dev.ikm.komet.kview.mvvm.view.genpurpose.control.PropertiesTabsControl;
import dev.ikm.komet.kview.mvvm.view.genpurpose.control.PropertiesTabsControl.Tab;
import dev.ikm.komet.kview.mvvm.viewmodel.FormViewModel.FormMode;
import dev.ikm.komet.layout.InlineEditSaver;
import dev.ikm.komet.layout.editor.EditorWindowManager;
import dev.ikm.komet.layout.editor.StandardEditorWindows;
import dev.ikm.komet.layout.editor.model.EditorPatternModel;
import dev.ikm.komet.layout.editor.model.EditorSectionModel;
import dev.ikm.komet.layout.editor.model.EditorWindowModel;
import dev.ikm.komet.layout.editor.model.EditorWindowType;
import dev.ikm.komet.preferences.KometPreferences;
import dev.ikm.tinkar.entity.Entity;
import dev.ikm.tinkar.entity.EntityHandle;
import dev.ikm.komet.layout.PatternFieldDefaults;
import dev.ikm.tinkar.entity.SemanticEntity;
import dev.ikm.tinkar.entity.SemanticEntityVersion;
import dev.ikm.tinkar.entity.graph.DiTreeEntity;
import dev.ikm.tinkar.events.EvtBusFactory;
import dev.ikm.tinkar.terms.EntityFacade;
import dev.ikm.tinkar.terms.EntityProxy;
import dev.ikm.tinkar.terms.PatternFacade;
import javafx.application.Platform;
import javafx.scene.layout.Region;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.nio.file.Paths;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.function.Consumer;

public class GenPurposeDetailsController {

    private static final Logger LOG = LoggerFactory.getLogger(GenPurposeDetailsController.class);

    /**
     * Active while the window is in create mode, i.e. framing a component that doesn't exist yet.
     * Drives the "ghost window" styling in kview.css (dimmed blue chrome, dashed frame, dimmed STAMP).
     */
    /**
     * Active while the window "has focus": the mouse was pressed inside it or any of its controls
     * holds keyboard focus. Drives the enlarged drop shadow in kview.css that makes the window
     * read as sitting closer to the screen.
     */
    /**
     * Key a collapsed section keeps the content height it gave up under, so expanding it again
     * hands the window back the very height its collapse closed (see
     * {@link #resizeWindowWithSection(SectionTitledPane, boolean)}).
     */
    private static final String COLLAPSED_CONTENT_HEIGHT_KEY = "gen-purpose-collapsed-content-height";

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

    private final GenPurposeWindowView view;
    private GenPurposePropertiesController propertiesController;
    /** The KL-editor window definition this window is built from; shared with the editor while both are open. */
    private final EditorWindowModel editorWindowModel;
    /**
     * In the standard Semantic window, the placement of the pattern the window's semantic is a
     * semantic of (see {@link #isSemanticWindow()}); null in every other window.
     */
    private final EditorPatternModel semanticPatternModel;
    /**
     * In the standard Semantic window, what its Reference Component section shows: the component
     * the semantic is about, or the chooser for it while the semantic doesn't exist; null in every
     * other window.
     */
    private final ReferenceComponentView referenceComponentView;
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
    private Consumer<GenPurposeDetailsController> onCloseWindow;
    /**
     * The Window's size follows the KL editor's window model, which is shared by every Window of that
     * definition and kept for the session (EditorWindowManager); released by {@link #releaseSubscriptions()}.
     */
    private javafx.util.Subscription editorWindowSizeSubscription = javafx.util.Subscription.EMPTY;

    /**
     * Re-evaluates the required chips when a stated definition changes: inline axiom edits
     * persist straight to the store without a PUBLISH event, yet they can satisfy — or, by
     * removing the definition's last set, un-satisfy — the stated pattern's requirement.
     * Strong reference: the entity provider holds its subscribers weakly.
     */
    private dev.ikm.tinkar.common.util.broadcast.Subscriber<Long> statedDefinitionChangeSubscriber;

    /**
     * Follows publishes and discards made outside this window (the journal's unpublished-changes
     * panel, the change review window): see {@link #followStoreChanges()}. Strong reference: the
     * entity provider holds its subscribers weakly. Null once the window is released.
     */
    private dev.ikm.tinkar.common.util.broadcast.Subscriber<Long> storeChangeSubscriber;

    /** True from a store change until {@link #followStoreChanges()} runs for it: one run per burst. */
    private final AtomicBoolean storeChangePending = new AtomicBoolean();

    /**
     * The components this window last showed as not published: the unpublished semantics and,
     * when its own latest version is unpublished, the window's component. Kept by
     * {@link #updatePublishState()}; one of them ending elsewhere is what the window follows.
     */
    private final Set<Long> shownUnpublishedNids = new HashSet<>();

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
        this(view, session, journalTopic, EditorWindowManager.loadWindowModel(editorWindowPreferences,
                session.getViewProperties().calculator(),
                Paths.get(editorWindowPreferences.absolutePath()).getFileName().toString()));
    }

    /**
     * Wires the window's behavior onto its view for a KL-editor window definition already at
     * hand: one loaded from preferences (see the other constructor), or the standard Semantic
     * window's, which is generated for the semantic's pattern and stored nowhere
     * ({@code StandardEditorWindows.createSemanticWindow}).
     *
     * @param view              the window's scene graph, already the window's root pane
     * @param session           what the window is editing, on the window's derived coordinate
     * @param journalTopic      the owning journal's topic
     * @param editorWindowModel the window definition
     */
    public GenPurposeDetailsController(GenPurposeWindowView view, WindowEditSession session, UUID journalTopic,
                                       EditorWindowModel editorWindowModel) {
        this.session = session;
        this.journalTopic = journalTopic;
        this.viewProperties = session.getViewProperties();
        this.view = view;
        view.setOnDiscardUnpublished(this::discardUnpublished);

        // The definition comes first: the chrome wiring below already asks its window type
        // (see usesPublishFlow).
        String windowTitle = editorWindowModel.getTitle();
        this.editorWindowModel = editorWindowModel;
        // The standard Semantic window places one pattern: the one its semantic is a semantic of.
        this.semanticPatternModel = isSemanticWindow() ? editorWindowModel.getAllPatterns().getFirst() : null;
        this.referenceComponentView = isSemanticWindow() ? new ReferenceComponentView(viewProperties) : null;
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
        this.windowSections = new WindowSections(editorWindowModel, session, view, journalTopic, inlineEditSaver);
        windowSections.setOnSectionExpandedChanged(this::resizeWindowWithSection);
        windowSections.setOnPublishInputsChanged(this::updatePublishState);

        this.sectionEditMenu = new SectionEditMenu(editorWindowModel, session, windowSections);
        sectionEditMenu.setOnEditSemantic(this::showEditSemanticFieldsPanel);
        sectionEditMenu.setOnCreateSemantic(this::onCreateSemantic);
        sectionEditMenu.setOnCreateStatedDefinition(this::createSeededStatedDefinition);
        // A section's pencil opens the menu of its patterns' semantics. The standard Semantic window
        // has the one semantic, so its pencil opens that semantic's edit form straight away.
        windowSections.setOnSectionEditRequested(isSemanticWindow() ? _ -> editWindowSemantic() : sectionEditMenu::open);
        view.setOnShowUnpublished(windowSections::revealFirstUnpublishedSemantic);

        wireChrome();
        buildWindowFromDefinition(windowTitle);
    }

    /**
     * Wires the window chrome: toolbar actions, the coordinates popup, create-mode styling, the
     * properties panel and its events.
     */
    private void wireChrome() {
        // Drive the coordinates menu from the relocated FilterOptionsPopup (ike-issues#661); the popup
        // writes the window's nodeView override, which the window's KL context + areas resolve through.
        view.setupCoordinatesPopup(viewProperties, this::updateView);

        // Wire the toolbar's behaviour: the close and Publish buttons take actions, while the window's
        // height follows the properties tray (opened by a toggle click or by setPropertiesTrayOpen).
        view.setOnClose(this::closeWindow);
        view.setOnPublish(this::publish);
        view.setOnFieldDefaults(this::openPatternFieldDefaults);
        // The Publish button follows the window's own pending edits (see updatePublishState).
        session.hasPendingEditsInThisWindowProperty().subscribe(this::updatePublishState);
        // Invalidation-based, so it reacts to changes only: the tray starts out closed like the toggle.
        view.propertiesTrayOpenProperty().subscribe(() -> onPropertiesTrayOpenChanged(view.isPropertiesTrayOpen()));

        // Ghost-window styling while in create mode: the window frames a component that doesn't
        // exist yet, so the chrome dims and the frame dashes (see :create-mode in kview.css) and
        // the DRAFT chip + hint appear. Publishing flips the mode to EDIT, which clears all of it.
        session.modeProperty().subscribe(mode -> {
            view.setCreating(mode == FormMode.CREATE);
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
    }

    /**
     * Follows the properties tray: as it opens the window grows to fit what the panel shows, and
     * as it closes the window gets back the height it had before the panel grew it.
     */
    private void onPropertiesTrayOpenChanged(boolean open) {
        if (open) {
            // The panel's content may have been swapped in this same pulse, ahead of the layout pass
            // that measures it — measure it now, so the window grows for what is about to show.
            propertiesController.refreshRequiredHeight();
            growWindowToFitProperties();
        } else {
            view.restorePreviousHeight();
        }
    }

    /**
     * Grows the window to fit its properties panel while the panel is open; closing the panel
     * gives the height back.
     */
    private void growWindowToFitProperties() {
        if (view.isPropertiesTrayOpen()) {
            view.growToFitProperties(propertiesController.requiredHeightProperty().get());
        }
    }

    /**
     * Creates the filter coordinates menu using the view calculator.
     * TODO Note that this is not a working menu, this is the first step to have propagating, inherited, filter coordinates
     * in the window/node hierarchy.
     */

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
     * Whether this is the standard Semantic window, whose component is a semantic rather than a
     * concept or pattern semantics are on: the window shows that one semantic's reference
     * component and fields, and creating its component means creating the semantic.
     */
    private boolean isSemanticWindow() {
        return editorWindowModel.getWindowType() == EditorWindowType.STANDARD_SEMANTIC;
    }

    /**
     * The standard Semantic window's create step: runs when the user confirms the component the
     * new semantic is about. The semantic is created against it, uncommitted, with the pattern's
     * field defaults, and becomes the window's component; its edit form opens for the values.
     * Cancelling that form before it was ever submitted discards the semantic again (see
     * {@link #onSemanticCancelled}), which takes the window back to the choice.
     *
     * <p>A pattern without fields has no form to fill: its semantic is complete as soon as it has
     * a reference component, so it is saved with the window's other edits right away.
     */
    private void onReferenceComponentConfirmed(EntityFacade referenceComponent) {
        PatternFacade pattern = semanticPatternModel.getPatternFacade();
        boolean hasFields = !semanticPatternModel.getFieldDefinitions().isEmpty();

        SemanticEntity<SemanticEntityVersion> semantic;
        if (hasFields) {
            semantic = createUncommitedSemantic(referenceComponent, pattern, Map.of());
            unsubmittedNewSemanticNids.add(semantic.nid());
        } else {
            semantic = session.createSemanticInWindowTransaction(referenceComponent, pattern.nid(), _ -> { });
        }

        session.setComponent(semantic);
        updateView();
        windowSections.reloadAllSemanticViews();
        updateRequiredChips();

        if (hasFields) {
            showEditSemanticFieldsPanel(semantic, semanticPatternModel);
        }
    }

    /** The standard Semantic window's pencil: opens the edit form of the window's semantic. */
    private void editWindowSemantic() {
        @SuppressWarnings("unchecked")
        SemanticEntity<SemanticEntityVersion> semantic = EntityHandle.get(session.getComponent().nid()).expectSemantic();
        showEditSemanticFieldsPanel(semantic, semanticPatternModel);
    }

    /**
     * Opens a component named in the standard Semantic window (the semantic's pattern, its
     * reference component) in the standard window for its kind, on the owning journal.
     */
    private void openInOwnWindow(EntityFacade component) {
        EntityHandle handle = EntityHandle.get(component.nid());
        if (handle.isSemantic()) {
            openInstanceWindow(component);
        } else {
            EvtBusFactory.getDefaultEvtBus().publish(journalTopic, new MakeKLWindowEvent(this,
                    MakeKLWindowEvent.OPEN_STANDARD_WINDOW, component,
                    handle.isPattern() ? StandardEditorWindows.PATTERN_WINDOW : StandardEditorWindows.CONCEPT_WINDOW));
        }
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
     * transaction (alongside the lazily created reference component) and commits together with it
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
            if (isSemanticWindow()) {
                // The discarded semantic was the window's own: the window is back to having none,
                // its Reference Component section offering the choice again.
                session.setComponent(null);
                updateView();
            }
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
        propertiesController.setOnCloseRequested(() -> view.setPropertiesTrayOpen(false));
        // An instance double-clicked on the INSTANCES tab opens in a semantic window of the journal.
        propertiesController.setOnInstanceOpened(this::openInstanceWindow);
        // The panel's tabs are its drag handle: while the tray is open they drag the window too.
        view.setPropertiesPanel(propertiesController.getNode(), propertiesController.getPropertiesTabs());

        // Follow the open panel's content: a form loaded, or swapped for a taller one, grows the window.
        propertiesController.requiredHeightProperty().subscribe(_ -> growWindowToFitProperties());
    }

    public ViewProperties getViewProperties() {
        return viewProperties;
    }

    public void setOnCloseWindow(Consumer<GenPurposeDetailsController> onClose) {
        this.onCloseWindow = onClose;
    }

    void closeWindow() {
        LOG.info("Cleanup occurring: Closing Window: " + session.getComponent());

        if (this.onCloseWindow != null) {
            onCloseWindow.accept(this);
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
        view.setWindowTitle(windowTitle.substring(0, 1).toUpperCase() + windowTitle.substring(1));

        // Each kind of window has its own chrome colours, which the view knows how to show: blue
        // for the standard Concept window, teal for the standard Semantic window, light for the
        // windows users create in the KL Editor, and grey for the standard Pattern window.
        view.setTheme(switch (editorWindowModel.getWindowType()) {
            case STANDARD_CONCEPT -> GenPurposeWindowView.Theme.CONCEPT;
            case STANDARD_SEMANTIC -> GenPurposeWindowView.Theme.SEMANTIC;
            case SEMANTICS -> GenPurposeWindowView.Theme.CUSTOM;
            case STANDARD_PATTERN -> GenPurposeWindowView.Theme.PATTERN;
        });

        // The standard windows have their own sets of properties tabs.
        if (editorWindowModel.getWindowType() == EditorWindowType.STANDARD_CONCEPT) {
            propertiesController.getPropertiesTabs().getTabs().setAll(
                    Tab.ADD_EDIT, Tab.HIERARCHY, Tab.HISTORY, Tab.COMMENTS);
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
                view.setFieldDefaultsVisible(patternExists);
            });
        } else if (isSemanticWindow()) {
            propertiesController.getPropertiesTabs().getTabs().setAll(Tab.ADD_EDIT, Tab.HISTORY, Tab.COMMENTS);

            // The tab names the pattern, so two Semantic windows are told apart by their tabs,
            // as the classic Semantic window's are.
            view.setWindowTitle(createSemanticWindowTabTitle());
            view.setOnOpenSemanticPattern(() -> openInOwnWindow(semanticPatternModel.getPatternFacade()));
        }

        // The create-mode hint names the kind of component this window will create.
        view.setCreateModeHintText("This " + componentKindString
                + " doesn't exist yet - it's created when you fill out the required values and "
                + (usesPublishFlow() ? "hit Publish." : "submit."));

        // The Publish UX — the toolbar Publish button and uncommitted-until-published changes — is
        // scoped to the standard Pattern and Concept windows for now; the other window types keep
        // committing on each properties-panel submit (see the PUBLISH event handler and the
        // fields controller's submit toast, both of which branch on this).
        view.setPublishVisible(usesPublishFlow());

        // Apply the Window settings authored in the KL editor (this window shares the same model).
        applyEditorWindowSettings();

        windowSections.build();

        if (isSemanticWindow()) {
            buildSemanticReferenceSection();
        }

        // Initial view update
        updateView();

        if (session.isCreateMode()) {
            view.setStampFromEditCoordinate(viewProperties);
        }

        // Sections exist now — show the required-pattern chips (create mode only).
        updateRequiredChips();

        // Inline stated-definition edits persist without a PUBLISH event, so track them here:
        // a change to any stated-axiom semantic re-evaluates the required chips.
        statedDefinitionChangeSubscriber = changedNid -> EntityHandle.get(changedNid).asSemantic()
                .filter(semantic -> semantic.patternNid() == windowSections.statedAxiomsPatternNid())
                .ifPresent(semantic -> Platform.runLater(this::updateRequiredChips));
        Entity.provider().addSubscriberWithWeakReference(statedDefinitionChangeSubscriber);

        // A publish or discard writes many entities, from a background thread: follow the burst once.
        storeChangeSubscriber = _ -> {
            if (storeChangePending.compareAndSet(false, true)) {
                Platform.runLater(this::followStoreChanges);
            }
        };
        Entity.provider().addSubscriberWithWeakReference(storeChangeSubscriber);
    }

    /**
     * The words on the standard Semantic window's title tab: the pattern's name, by the name the
     * view reads it by, followed by "semantic" (see {@link PatternUIUtils#semanticTabTitle}).
     */
    private String createSemanticWindowTabTitle() {
        long patternNid = semanticPatternModel.getNid();
        var languageCalculator = viewProperties.calculator().languageCalculator();
        String patternName = languageCalculator.getDescriptionText(patternNid)
                .orElseGet(() -> languageCalculator.getFullyQualifiedDescriptionTextWithFallbackOrNid(patternNid));
        return PatternUIUtils.semanticTabTitle(patternName);
    }

    /**
     * Fills the standard Semantic window's two sections with what the definition's patterns
     * cannot say. The Reference Component section places no pattern: it shows the component the
     * window's semantic is about, which follows the window's component, or the chooser for it
     * while the semantic doesn't exist. It shows no pencil, since a reference component
     * cannot be changed and the chooser is in the section itself; the details section's pencil edits the semantic, so it waits
     * for the semantic to exist.
     */
    private void buildSemanticReferenceSection() {
        SectionTitledPane<EntityFacade> referencePane = windowSections.titledPaneOf(editorWindowModel.getMainSection());
        referencePane.getItems().add(referenceComponentView);
        referencePane.setEditVisible(false);

        SectionTitledPane<EntityFacade> detailsPane = windowSections.titledPaneOf(semanticPatternModel.getParentSection());
        detailsPane.editEnabledProperty().unbind();
        detailsPane.editEnabledProperty().bind(session.componentProperty().isNotNull());

        session.componentProperty().subscribe(component -> referenceComponentView.setReferenceComponent(
                component == null ? null : EntityHandle.get(component.nid()).expectSemantic().referencedComponent()));
        referenceComponentView.setOnConfirmed(this::onReferenceComponentConfirmed);
        referenceComponentView.setOnOpenRequested(this::openInOwnWindow);
    }

    /**
     * Brings the window up to date when something it shows as not published was published or
     * discarded outside it: the session lets go of the transactions
     * that ended, and the banner, the semantics and the Publish state are read again from the
     * store, as after the window's own {@link #publish()}. A window still creating its component
     * enters edit mode when the component was published, and closes when it was discarded.
     */
    private void followStoreChanges() {
        storeChangePending.set(false);
        if (storeChangeSubscriber == null || shownUnpublishedNids.stream().allMatch(GenPurposeDetailsController::isSavedButUnpublished)) {
            return;
        }
        session.releaseTransactionsEndedElsewhere();
        unsubmittedNewSemanticNids.removeIf(nid -> !isSavedButUnpublished(nid));

        EntityFacade component = session.getComponent();
        if (session.isCreateMode() && component != null && !isSavedButUnpublished(component.nid())) {
            Entity<?> current = EntityHandle.get(component.nid()).orNull();
            if (current == null || current.versions().isEmpty() || current.canceled()) {
                closeWindow();
                return;
            }
            session.enterEditMode();
        }
        updateView();
        windowSections.reloadAllSemanticViews();
        updatePublishState();
    }

    /** Whether the component has a version saved and still waiting: neither published nor discarded. */
    private static boolean isSavedButUnpublished(long nid) {
        return EntityHandle.get(nid).orNull() instanceof Entity<?> entity && entity.uncommitted();
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
        view.coordinateVisibleProperty().bind(editorWindowModel.coordinateVisibleProperty());
        view.timelineVisibleProperty().bind(editorWindowModel.timelineVisibleProperty());

        // View size: apply an explicit Width/Height. "Auto" (< 0) leaves the workspace's own sizing
        // alone. The framework sizes this same root pane via setPrefWidth/Height, so we set (not bind) it.
        // We also advertise the authored size on the pane's properties so the workspace honors it when
        // placing the window (otherwise its default placement overrides our preferred width/height).
        editorWindowSizeSubscription = editorWindowModel.prefWidthProperty().subscribe(width -> {
            final double w = width.doubleValue();
            if (w >= 0) {
                view.setPrefWidth(w);
                view.getProperties().put(KLWorkspace.WINDOW_AUTHORED_WIDTH_KEY, w);
            } else {
                view.getProperties().remove(KLWorkspace.WINDOW_AUTHORED_WIDTH_KEY);
            }
        });
        editorWindowSizeSubscription = editorWindowSizeSubscription.and(editorWindowModel.prefHeightProperty().subscribe(height -> {
            final double h = height.doubleValue();
            if (h >= 0) {
                view.setPrefHeight(h);
                view.getProperties().put(KLWorkspace.WINDOW_AUTHORED_HEIGHT_KEY, h);
            } else {
                view.getProperties().remove(KLWorkspace.WINDOW_AUTHORED_HEIGHT_KEY);
            }
        }));
    }

    /**
     * Update the current view taking into account the current Reference Component of the Window
     */
    private void updateView() {
        LOG.info("Update view called - implement coordinate changes here.");
        EntityFacade refComponent = session.getComponent();
        if (isSemanticWindow()) {
            // What the semantic is a semantic of, and about, is named whether or not it exists
            // yet; until it does, the banner's title says what the window is going to create.
            view.setSemanticPattern(viewProperties, semanticPatternModel.getPatternFacade());
            referenceComponentView.refresh();
            if (refComponent == null) {
                view.showPlaceholderTitle("New " + createSemanticWindowTabTitle());
                return;
            }
        }
        if (refComponent != null) {
            view.setComponent(viewProperties, refComponent);
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
        view.finishHeightAnimationNow();
        final double windowHeight = view.getPrefHeight();
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

        // Through the view's growth helper, so a window grown to fit its properties panel takes the
        // section's height out of (or into) the height closing the panel restores as well.
        view.changeHeightBy(delta);
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
        view.setPropertiesTrayOpen(true);

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
        view.setPropertiesTrayOpen(true);
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
        view.setPropertiesTrayOpen(true);
    }

    /**
     * Opens a semantic (an instance listed on the INSTANCES tab, or a semantic the standard
     * Semantic window names) in a window of its own on the journal: the journal answers the gen
     * editing event with the standard KL Semantic window, which derives its coordinate from the
     * journal's.
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
                || editorWindowModel.getWindowType() == EditorWindowType.STANDARD_CONCEPT
                || editorWindowModel.getWindowType() == EditorWindowType.STANDARD_SEMANTIC;
    }

    /**
     * Recomputes the toolbar Publish button's enablement and tooltip. Publishing needs uncommitted
     * changes in the composer's open transaction, and in create mode additionally every required
     * pattern satisfied ({@link #allRequiredPatternsSatisfied}) — the component only comes into
     * existence complete. Runs whenever those inputs may have moved: with the required chips
     * (mode changes, PUBLISH submits, inline stated-definition edits) and on the composer's
     * change tracking (see {@link WindowEditSession#hasPendingEditsInThisWindowProperty}).
     */
    /**
     * Refreshes each section's required-pattern chip (see {@link WindowSections#updateRequiredChips})
     * and, since the same check gates the Publish button in create mode, the button with them.
     */
    private void updateRequiredChips() {
        windowSections.updateRequiredChips();
        if (isSemanticWindow()) {
            // The Reference Component section places no pattern to be required, yet the semantic
            // cannot exist without a reference component: met once the window's semantic exists.
            // Null until the sections are built (the mode subscription runs before that).
            SectionTitledPane<EntityFacade> referencePane = windowSections.titledPaneOf(editorWindowModel.getMainSection());
            if (referencePane != null) {
                referencePane.setRequiredChipVisible(session.isCreateMode());
                referencePane.setRequiredSatisfied(session.getComponent() != null);
            }
        }
        updatePublishState();
    }

    private void updatePublishState() {
        boolean disabled;
        String publishTooltip;
        int unpublishedChanges = 0;
        if (session.isCreateMode()) {
            // Read from the store, not from the composer: the component being created is saved
            // there, unpublished, from the first semantic authored on it, whether this window
            // saved it or it was opened on one saved elsewhere.
            disabled = countUnpublishedInStore() == 0 || !windowSections.allRequiredPatternsSatisfied();
            // The standard Semantic window requires no semantics of its component: it requires
            // the semantic's own reference component and values.
            publishTooltip = !disabled ? "Publish"
                    : isSemanticWindow() ? "Complete the required values to publish"
                    : "Complete the required semantics to publish";
        } else {
            // Changes saved but not published count whether this window instance saved them or
            // an earlier one did (the window was closed and reopened): they are read from the
            // store, not from the composer. The composer adds a semantic whose edit form is open,
            // which a publish saves as the form stands.
            unpublishedChanges = countUnpublishedInStore();
            disabled = !session.hasPendingEditsInThisWindow() && unpublishedChanges == 0;
            publishTooltip = disabled ? "No changes to publish"
                    : unpublishedChanges == 0 ? "Publish"
                    : "Publish " + unpublishedChanges + (unpublishedChanges == 1 ? " change" : " changes");
        }
        view.setPublishDisable(disabled);
        view.setPublishTooltip(publishTooltip);

        // The strip under the toolbar names the changes not published yet. Not in create mode,
        // whose DRAFT chip and hint already say the whole component is unpublished.
        boolean showHint = unpublishedChanges > 0;
        view.setUnpublishedHintVisible(showHint);
        view.setUnpublishedDiscardVisible(!session.isCreateMode());
        if (showHint) {
            view.setUnpublishedHintText(unpublishedChanges == 1
                    ? "1 change not published yet. Only you can see it until you publish."
                    : unpublishedChanges + " changes not published yet. Only you can see them until you publish.");
        }

        windowSections.updateUnpublishedChips();

        shownUnpublishedNids.clear();
        windowSections.getUnpublishedSemantics().forEach(semantic -> shownUnpublishedNids.add(semantic.nid()));
        EntityFacade component = session.getComponent();
        if (component != null && isSavedButUnpublished(component.nid())) {
            shownUnpublishedNids.add(component.nid());
        }
    }

    /**
     * How many of the components this window shows have a version saved in the store but not
     * published yet: the semantics shown, plus the window's own component. Whoever saved it
     * counts (this window, another one, an earlier instance of this one, the assistant), and it
     * is the number the unpublished strip and the Publish tooltip show. An edit form that is open
     * with nothing saved yet is not counted; {@link WindowEditSession#hasPendingEditsInThisWindowProperty}
     * covers that.
     */
    private int countUnpublishedInStore() {
        List<SemanticEntity<SemanticEntityVersion>> unpublishedSemantics = windowSections.getUnpublishedSemantics();
        int count = unpublishedSemantics.size();
        EntityFacade refComponent = session.getComponent();
        // In the standard Semantic window the component is itself the semantic shown: counted once.
        if (refComponent != null && EntityHandle.get(refComponent.nid()).expectEntity().uncommitted()
                && unpublishedSemantics.stream().noneMatch(semantic -> semantic.nid() == refComponent.nid())) {
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
        int changes = countUnpublishedInStore();
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
        session.cancelUnpublishedTransactions(windowSections.getUnpublishedSemantics());
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

        int unpublishable = session.commitUnpublishedTransactions(windowSections.getUnpublishedSemantics());

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

    /** Stops following the shared window model, which otherwise holds this Window and its journal. */
    public void releaseSubscriptions() {
        editorWindowSizeSubscription.unsubscribe();
        editorWindowSizeSubscription = javafx.util.Subscription.EMPTY;
        storeChangeSubscriber = null;
        windowSections.release();
    }
}