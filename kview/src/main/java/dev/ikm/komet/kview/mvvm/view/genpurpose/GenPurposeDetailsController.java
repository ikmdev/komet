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

import static dev.ikm.komet.kview.events.genpurpose.KLPropertyPanelEvent.CLOSE_PANEL;
import static dev.ikm.komet.kview.events.genpurpose.KLPropertyPanelEvent.NO_SELECTION_MADE_PANEL;
import static dev.ikm.komet.kview.events.genpurpose.KLPropertyPanelEvent.OPEN_PANEL;
import static dev.ikm.komet.kview.events.genpurpose.KLPropertyPanelEvent.SHOW_EDIT_SEMANTIC_FIELDS;
import static dev.ikm.komet.kview.events.genpurpose.KLPropertyPanelEvent.SHOW_PATTERN_FIELD_DEFAULTS;
import static dev.ikm.komet.kview.fxutils.SlideOutTrayHelper.isClosed;
import static dev.ikm.komet.kview.fxutils.SlideOutTrayHelper.isOpen;
import static dev.ikm.komet.kview.fxutils.SlideOutTrayHelper.slideIn;
import static dev.ikm.komet.kview.fxutils.SlideOutTrayHelper.slideOut;
import static dev.ikm.komet.kview.fxutils.ViewportHelper.clipChildren;
import static dev.ikm.komet.layout_engine.window.DraggableSupport.addDraggableNodes;
import static dev.ikm.komet.layout_engine.window.DraggableSupport.removeDraggableNodes;
import static dev.ikm.komet.kview.klfields.KlFieldHelper.retrieveCommittedLatestVersion;
import static dev.ikm.komet.kview.mvvm.view.common.ChapterWindowHelper.setupViewCoordinateOptionsPopup;
import static dev.ikm.komet.kview.mvvm.view.journal.JournalController.toast;
import static dev.ikm.komet.kview.mvvm.viewmodel.ViewModelKey.CURRENT_JOURNAL_WINDOW_TOPIC;

import dev.ikm.komet.framework.Identicon;
import dev.ikm.komet.framework.controls.TimeUtils;
import dev.ikm.komet.framework.observable.ObservableComposer;
import dev.ikm.komet.framework.observable.ObservableEntity;
import dev.ikm.komet.framework.observable.ObservableEntityHandle;
import dev.ikm.komet.framework.observable.ObservableEntitySnapshot;
import dev.ikm.komet.framework.observable.ObservableField;
import dev.ikm.komet.framework.observable.ObservablePattern;
import dev.ikm.komet.framework.observable.ObservableSemantic;
import dev.ikm.komet.framework.observable.ObservableSemanticVersion;
import dev.ikm.komet.framework.view.ViewProperties;
import dev.ikm.komet.kview.common.ViewCalculatorUtils;
import dev.ikm.komet.kview.controls.ContentSizedSplitPane;
import dev.ikm.komet.kview.controls.ComponentItem;
import dev.ikm.komet.kview.controls.KLWorkspace;
import dev.ikm.komet.kview.controls.KlWindowControlToolbar;
import dev.ikm.komet.layout.controls.FilterOptionsPopup;
import dev.ikm.komet.kview.controls.KometLabel;
import dev.ikm.komet.kview.controls.PublicIDListControl;
import dev.ikm.komet.kview.controls.SectionTitledPane;
import dev.ikm.komet.kview.controls.StampViewControl;
import dev.ikm.komet.kview.controls.SectionEditPopup;
import dev.ikm.komet.kview.controls.Toast;
import dev.ikm.komet.kview.controls.ComponentItemNode;
import dev.ikm.komet.kview.controls.ComponentItemNodeFactory;
import dev.ikm.komet.kview.events.ClosePropertiesPanelEvent;
import dev.ikm.komet.kview.events.genpurpose.GenPurposeEvent;
import dev.ikm.komet.kview.events.genpurpose.KLPropertyPanelEvent;
import dev.ikm.komet.kview.mvvm.view.genpurpose.control.PropertiesTabsControl;
import dev.ikm.komet.kview.mvvm.view.genpurpose.control.PropertiesTabsControl.Tab;
import dev.ikm.komet.kview.mvvm.view.genpurpose.control.SectionSemanticsComboBoxCell;
import dev.ikm.komet.kview.mvvm.view.genpurpose.control.standard.SemanticStandardControl;
import dev.ikm.komet.kview.mvvm.view.journal.VerticallyFilledPane;
import dev.ikm.komet.kview.mvvm.viewmodel.FormViewModel.FormMode;
import dev.ikm.komet.kview.mvvm.viewmodel.GenPurposeViewModel;
import dev.ikm.komet.layout.KlPatternSemanticsFactory;
import dev.ikm.komet.layout.PatternSemanticsPresenter;
import dev.ikm.komet.layout.editor.EditorWindowManager;
import dev.ikm.komet.layout.editor.model.EditorFieldModel;
import dev.ikm.komet.layout.editor.model.EditorPatternModel;
import dev.ikm.komet.layout.editor.model.EditorPatternRequirement;
import dev.ikm.komet.layout.editor.model.EditorPatternSemanticFilter;
import dev.ikm.komet.layout.editor.model.EditorSectionModel;
import dev.ikm.komet.layout.editor.model.EditorWindowModel;
import dev.ikm.komet.layout.editor.model.EditorWindowType;
import dev.ikm.komet.layout_engine.window.WindowSupport;
import dev.ikm.komet.preferences.KometPreferences;
import dev.ikm.tinkar.common.id.PublicIds;
import dev.ikm.tinkar.coordinate.stamp.calculator.Latest;
import dev.ikm.tinkar.coordinate.view.calculator.ViewCalculator;
import dev.ikm.tinkar.entity.Entity;
import dev.ikm.tinkar.entity.EntityHandle;
import dev.ikm.komet.layout.KlTerms;
import dev.ikm.komet.layout.PatternFieldDefaults;
import dev.ikm.tinkar.entity.EntityService;
import dev.ikm.tinkar.entity.EntityVersion;
import dev.ikm.tinkar.entity.PatternEntity;
import dev.ikm.tinkar.entity.SemanticEntity;
import dev.ikm.tinkar.entity.SemanticEntityVersion;
import dev.ikm.tinkar.entity.StampEntity;
import dev.ikm.tinkar.entity.graph.DiTreeEntity;
import dev.ikm.tinkar.entity.graph.adaptor.axiom.LogicalExpressionBuilder;
import dev.ikm.tinkar.events.EvtBusFactory;
import dev.ikm.tinkar.events.EvtType;
import dev.ikm.tinkar.events.Subscriber;
import dev.ikm.tinkar.terms.ConceptFacade;
import dev.ikm.tinkar.terms.EntityFacade;
import dev.ikm.tinkar.terms.EntityProxy;
import dev.ikm.tinkar.terms.PatternFacade;
import dev.ikm.tinkar.terms.State;
import dev.ikm.tinkar.terms.TinkarTerm;
import javafx.application.Platform;
import javafx.beans.binding.Bindings;
import javafx.collections.ListChangeListener;
import javafx.css.PseudoClass;
import javafx.event.ActionEvent;
import javafx.event.Event;
import javafx.fxml.FXML;
import javafx.geometry.Point2D;
import javafx.scene.Node;
import javafx.scene.control.Label;
import javafx.scene.control.SplitPane;
import javafx.scene.control.TitledPane;
import javafx.scene.control.Tooltip;
import javafx.scene.image.Image;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.GridPane;
import dev.ikm.komet.layout_engine.host.SupplementalAreaRenderer;
import javafx.scene.layout.Pane;
import javafx.scene.layout.Priority;
import javafx.scene.layout.Region;
import javafx.scene.layout.VBox;
import org.carlfx.cognitive.loader.InjectViewModel;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicReference;
import java.util.function.Consumer;
import java.util.stream.Collectors;
import dev.ikm.komet.kview.mvvm.viewmodel.ViewModelKey;

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

    /**
     * Key a collapsed section keeps the content height it gave up under, so expanding it again
     * hands the window back the very height its collapse closed (see
     * {@link #resizeWindowWithSection(SectionTitledPane, boolean)}).
     */
    private static final String COLLAPSED_CONTENT_HEIGHT_KEY = "gen-purpose-collapsed-content-height";

    /**
     * Given a Pattern what is the Section that has it as its Reference Component.
     */
    private final Map<EditorPatternModel, SectionTitledPane<EntityFacade>> patternReferenceComponentToSectionTitledPane = new HashMap<>();

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
     * Given a SemanticEntity what's its associated Semantic Control.
     */
    private final Map<SemanticEntity<SemanticEntityVersion>, SemanticStandardControl> semanticEntityToSemanticView = new HashMap<>();

    /**
     * Sections currently being refreshed by a reference-component cascade. Used as a re-entrancy guard
     * so a malformed (cyclic) reference chain can't drive infinite recursion (see komet-desktop #3).
     */
    private final Set<EditorSectionModel> refreshingSections = new HashSet<>();

    private PatternSemanticsPresenter previousPatternSemanticsInEditMode;

    @FXML
    StampViewControl stampViewControl;
    @FXML
    private ContentSizedSplitPane mainContent;
    @FXML
    private BorderPane detailsOuterBorderPane;
    @FXML
    private KlWindowControlToolbar windowControlToolbar;
    /**
     * popup for the filter coordinates menu, used with the toolbar's coordinates menu button.
     * An instance of FilterOptionsPopup.
     */
    private FilterOptionsPopup filterOptionsPopup;
    /**
     * Used slide out the properties view
     */
    @FXML
    private VerticallyFilledPane propertiesSlideoutTrayPane;
    @FXML
    private ComponentItemNode windowConceptTitle;
    @FXML
    private Tooltip windowConceptTitleTooltip;
    @FXML
    private PublicIDListControl identifierControl;
    @FXML
    private Label createModeHintLabel;
    private BorderPane propertiesBorderPane;
    private GenPurposePropertiesController propertiesController;
    private EditorWindowModel editorWindowModel;
    /**
     * The kind of component this window frames ("Concept", "Pattern" or "Semantic") — set in
     * {@link #init} from the authored window type. Names the component in the create-mode hint
     * and the publish toast.
     */
    private String componentKindString;
    private ViewProperties viewProperties;
    @InjectViewModel
    private GenPurposeViewModel genPurposeViewModel;
    private Consumer<GenPurposeDetailsController> onCloseConceptWindow;

    private Subscriber<ClosePropertiesPanelEvent> closePropertiesPanelEventSubscriber;

    /**
     * Re-evaluates the required chips when a stated definition changes: inline axiom edits
     * persist straight to the store without a PUBLISH event, yet they can satisfy — or, by
     * removing the definition's last set, un-satisfy — the stated pattern's requirement.
     * Strong reference: the entity provider holds its subscribers weakly.
     */
    private dev.ikm.tinkar.common.util.broadcast.Subscriber<Integer> statedDefinitionChangeSubscriber;

    private ObservableComposer composer;

    @FXML
    private void initialize() {

        // The FXML loader instantiates the title node, so the factory cannot; attach its
        // glyph resolution here instead.
        ComponentItemNodeFactory.attachGlyphResolution(windowConceptTitle);

        // Drive the coordinates menu from the relocated FilterOptionsPopup (ike-issues#661); the popup
        // writes the window's nodeView override, which the window's KL context + areas resolve through.
        filterOptionsPopup = setupViewCoordinateOptionsPopup(genPurposeViewModel.getViewProperties(),
                FilterOptionsPopup.FILTER_TYPE.CHAPTER_WINDOW, detailsOuterBorderPane,
                windowControlToolbar.getCoordinatesMenuButton(), this::updateView);

        // Wire the toolbar's behaviour: the close and Publish buttons take actions, while the
        // properties panel reacts to the toggle's selected state (driven by user clicks or
        // setPropertiesSelected).
        windowControlToolbar.setOnCloseAction(this::closeConceptWindow);
        windowControlToolbar.setOnPublishAction(this::publish);
        windowControlToolbar.setOnFieldDefaultsAction(this::openPatternFieldDefaults);
        windowControlToolbar.propertiesSelectedProperty()
                .subscribe((w) -> onPropertiesToggleChanged(windowControlToolbar.isPropertiesSelected()));

        // The header STAMP is view-only in this window — clicking it must not select it or open
        // the STAMP form.
        stampViewControl.setSelectable(false);

        // When the window resizes vertically only the bottom section should grow or shrink; the
        // SplitPane's default is to spread the delta across every section proportionally, so pin
        // all items except the last one. This takes over once the user has claimed the sections'
        // sizing by dragging a divider — until then ContentSizedSplitPane sizes every section to
        // its content and hands the leftover height to that same last section.
        mainContent.getItems().subscribe(() -> {
            List<Node> items = mainContent.getItems();
            items.forEach(item -> SplitPane.setResizableWithParent(item, item == items.getLast()));
        });

        // Ghost-window styling while in create mode: the window frames a component that doesn't
        // exist yet, so the chrome dims and the frame dashes (see :create-mode in kview.css) and
        // the DRAFT chip + hint appear. Publishing flips the mode to EDIT, which clears all of it.
        genPurposeViewModel.modeProperty().subscribe(mode -> {
            boolean creating = mode == FormMode.CREATE;
            detailsOuterBorderPane.pseudoClassStateChanged(CREATE_MODE, creating);
            windowControlToolbar.setDraftVisible(creating);
            createModeHintLabel.setVisible(creating);
            createModeHintLabel.setManaged(creating);
            updateRequiredChips();
        });

        // Setup Properties Bump out view
        setupProperties();

        // Selecting the panel's DEFAULTS tab loads the pattern's field defaults into it (the tab
        // is only offered in the standard Pattern window once the pattern exists, see init).
        PropertiesTabsControl propertiesTabs = propertiesController.getPropertiesTabs();
        propertiesTabs.selectedTabProperty().subscribe(() -> {
            if (propertiesTabs.getSelectedTab() == Tab.DEFAULTS) {
                showPatternFieldDefaults();
            }
        });

        Subscriber<GenPurposeEvent> refreshSubscriber = evt -> {
            SemanticEntity<SemanticEntityVersion> semantic = evt.getSemantic();

            if (evt.getEventType() == GenPurposeEvent.PUBLISH) {
                if (usesPublishFlow()) {
                    // Publish-flow window: a submit only stages the change — the saved (still
                    // uncommitted) version stays in the composer's open transaction, alongside,
                    // in create mode, the lazily created reference component, until the toolbar's
                    // Publish button commits everything together (see publish). The details area
                    // refreshes so the submitted field values show, the required chips re-evaluate
                    // and the Publish button follows the staged changes.
                    reloadSemanticViews(semantic);
                    updateRequiredChips();
                    return;
                }

                // Without the Publish flow, the submit itself commits. In create mode the
                // component only truly gets created once every required pattern has at least one
                // semantic. Until then, skip the commit — the submitted semantic stays uncommitted
                // in the composer's open transaction (alongside the lazily created reference
                // concept) and commits together with it later. The details area still refreshes
                // so the submitted (still uncommitted) field values show.
                if (genPurposeViewModel.getMode() == FormMode.CREATE && !allRequiredPatternsSatisfied()) {
                    reloadSemanticViews(semantic);
                    // The submitted semantic now shows in the details area — flip its section's
                    // chip to MET.
                    updateRequiredChips();
                    return;
                }

                // Commit transaction, finalizing all impending changes
                composer.commit();

                composer = null;
                initializeComposer();

                // In create mode that commit also finalized the window's lazily created reference
                // component (see createUncommitedReferenceComponent) — the window is now editing a
                // real component, so refresh the banner/identifier/STAMP from the committed entity.
                if (genPurposeViewModel.getMode() == FormMode.CREATE) {
                    genPurposeViewModel.setMode(FormMode.EDIT);
                    updateView();
                }

                reloadSemanticViews(semantic);
            }
        };
        EvtBusFactory.getDefaultEvtBus().subscribe(genPurposeViewModel.getPropertyValue(ViewModelKey.WINDOW_TOPIC),
                GenPurposeEvent.class, refreshSubscriber);

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

        // if the user clicks the Close Properties Button from the Edit Descriptions panel
        // in that state, the properties bump out will be slid out, therefore toggling will perform a slide in
        closePropertiesPanelEventSubscriber = evt ->
                windowControlToolbar.setPropertiesSelected(!windowControlToolbar.isPropertiesSelected());
        EvtBusFactory.getDefaultEvtBus().subscribe(genPurposeViewModel.getPropertyValue(ViewModelKey.WINDOW_TOPIC), ClosePropertiesPanelEvent.class, closePropertiesPanelEventSubscriber);
    }

    private void openPropertiesPanel() {
        LOG.info("propBumpOutListener - Opening Properties bumpout toggle = " + windowControlToolbar.isPropertiesSelected());

        windowControlToolbar.setPropertiesSelected(true);
        if (isClosed(propertiesSlideoutTrayPane)) {
            slideOut(propertiesSlideoutTrayPane, detailsOuterBorderPane);
        }

        updateDraggableNodesForPropertiesPanel(true);
    }

    /**
     * Runs when the user toggles the Properties switch. Publishes the matching open/close event, which the
     * {@code KLPropertyPanelEvent} subscriber turns into the actual slide-out / slide-in (including updating
     * the draggable nodes), so this method does not perform the slide itself.
     *
     * @param selected the new selected state of the properties toggle
     */
    private void onPropertiesToggleChanged(boolean selected) {
        EvtType<KLPropertyPanelEvent> eventEvtType = selected ? KLPropertyPanelEvent.OPEN_PANEL : KLPropertyPanelEvent.CLOSE_PANEL;

        EvtBusFactory.getDefaultEvtBus().publish(genPurposeViewModel.getPropertyValue(ViewModelKey.WINDOW_TOPIC), new KLPropertyPanelEvent(windowControlToolbar, eventEvtType));
    }

    public void attachPropertiesViewSlideoutTray(Pane propertiesViewBorderPane) {
        addPaneToTray(propertiesViewBorderPane, propertiesSlideoutTrayPane);
    }

    private void addPaneToTray(Pane contentViewPane, Pane slideoutTrayPane) {
        double width = contentViewPane.getWidth();
        contentViewPane.setLayoutX(width);
        contentViewPane.getStyleClass().add("slideout-tray-pane");

        slideoutTrayPane.getChildren().add(contentViewPane);
        clipChildren(slideoutTrayPane, 0);
        contentViewPane.setLayoutX(-width);
        slideoutTrayPane.setMaxWidth(0);

        // The tray takes the height the window gives it and must not ask for a height of its own.
        // Left to report one, it ratchets the window: the tray is a Pane, so its preferred height is
        // its content's, and the content's preferred height is bound to the tray's height just below
        // — the tray would go on asking for whatever height it already had, and the window could
        // never shrink back once its sections did (komet-desktop#159).
        slideoutTrayPane.setMinHeight(0);
        slideoutTrayPane.setPrefHeight(0);

        Region contentRegion = contentViewPane;
        // binding the child's height to the preferred height of hte parent
        // so that when we resize the window the content in the slide out pane
        // aligns with the details view
        contentRegion.prefHeightProperty().bind(slideoutTrayPane.heightProperty());
    }

    /// Show the public ID
    private void updateDisplayIdentifier(EntityFacade refComponent) {
        ViewCalculator viewCalculator = getViewProperties().calculator();
        identifierControl.updatePublicIdList(viewCalculator, refComponent);
    }

    private void updateStampControl(EntityFacade refConcept) {
        ObservableEntity observableEntity = ObservableEntity.get(refConcept.nid());
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
            stampViewControl.setLastUpdated(TimeUtils.toShortDateString(newTime));

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
            Map<Integer, EntityProxy> fieldSeeds) {
        ObservableEntity observableReferenceComponent = ObservableEntityHandle.get(referenceComponent.nid()).expectEntity();
        ObservablePattern observablePattern = ObservableEntityHandle.get(pattern.nid()).expectPattern();

        initializeComposer();

        ObservableComposer.EntityComposer<ObservableSemanticVersion.Editable, ObservableSemantic> semanticEditor = composer.composeSemantic(PublicIds.newRandom(), observableReferenceComponent, observablePattern);

        // Start from the pattern's field defaults — the values a new semantic of this pattern
        // begins with in every window (see PatternFieldDefaults) — before the create entry's
        // filter seeds below, which take precedence over them.
        PatternFieldDefaults.applyDefaults(semanticEditor.getEditableVersion().getEditableFields(),
                PatternFieldDefaults.defaultValues(pattern.nid(), getViewProperties().calculator()).castToList());

        // Seed the fields the create entry's display filter constrains, so the new semantic passes
        // that filter and shows up in the filtered view it was created from.
        fieldSeeds.forEach((fieldIndex, filterConcept) -> {
            @SuppressWarnings("unchecked")
            ObservableField.Editable<EntityProxy> editableField = (ObservableField.Editable<EntityProxy>)
                    semanticEditor.getEditableVersion().getEditableFields().get(fieldIndex);
            editableField.setValue(filterConcept);
        });

        semanticEditor.save(); // Save to create an uncommitted version

        AtomicReference<SemanticEntity<SemanticEntityVersion>> newSemantic = new AtomicReference<>();
        EntityHandle.get(semanticEditor.getEntity().nid()).asSemantic().ifPresentOrElse(semanticEntity -> {
                    newSemantic.set(semanticEntity);
                },
                () -> {
                    throw new RuntimeException("Error creating new uncommited Semantic");
                });

        return newSemantic.get();
    }

    /**
     * Creates the window's reference component as a new, uncommitted entity — used in create mode,
     * where the window was opened without one. The component kind follows the authored window type:
     * the standard Pattern window creates a Pattern, all others create a Concept. The new component
     * joins the composer's current transaction, so it gets committed together with the semantic
     * whose creation triggered it.
     *
     * @return the new uncommitted component, already set as the window's reference component
     */
    private EntityFacade createUncommitedReferenceComponent() {
        initializeComposer();

        ObservableComposer.EntityComposer<?, ?> entityComposer =
                editorWindowModel.getWindowType() == EditorWindowType.STANDARD_PATTERN
                        ? composer.composePattern(PublicIds.newRandom())
                        : composer.composeConcept(PublicIds.newRandom());

        entityComposer.save(); // Save to create an uncommitted version

        EntityFacade newComponent = entityComposer.getEntity();
        genPurposeViewModel.setPropertyValue(ViewModelKey.REF_COMPONENT, newComponent);
        return newComponent;
    }

    /**
     * The stated axioms pattern per the view's logic coordinate — the pattern whose semantics
     * edit inline as an axiom tree (see {@code KlFieldHelper.createReadOnlyKlField}).
     */
    private int statedAxiomsPatternNid() {
        return getViewProperties().calculator().viewCoordinateRecord().logicCoordinate().statedAxiomsPatternNid();
    }

    /**
     * Creates the stated definition semantic seeded with one necessary or sufficient set — the
     * classic concept window's "Add Necessary Set" / "Add Sufficient Set" actions: the set holds
     * an is-a to "Anonymous concept", the placeholder chip the user then replaces in the inline
     * axiom tree. The new semantic is submitted through the window's PUBLISH flow — the same
     * path a properties-panel submit takes: it stages in the composer's open transaction until
     * the toolbar's Publish button commits it ({@link #publish}), and the details area re-renders
     * bound to the new semantic. In create mode the window may not have a reference component
     * yet — the seeded definition brings it into existence, exactly like authoring the first
     * semantic through the section pencil ({@link #onCreateSemantic}).
     */
    private void createSeededStatedDefinition(EditorSectionModel section, EditorPatternModel patternModel,
            boolean necessary) {
        EntityFacade refComponent = resolveSectionReferenceComponent(section);
        if (refComponent == null) {
            refComponent = createUncommitedReferenceComponent();
        }

        DiTreeEntity seededDefinition = seedDefinition(necessary);

        initializeComposer();
        ObservableEntity observableReferenceComponent = ObservableEntityHandle.get(refComponent.nid()).expectEntity();
        ObservablePattern observablePattern = ObservableEntityHandle.get(patternModel.getNid()).expectPattern();
        ObservableComposer.EntityComposer<ObservableSemanticVersion.Editable, ObservableSemantic> semanticEditor =
                composer.composeSemantic(PublicIds.newRandom(), observableReferenceComponent, observablePattern);

        // The stated axiom pattern's single field holds the definition.
        @SuppressWarnings("unchecked")
        ObservableField.Editable<DiTreeEntity> definitionField = (ObservableField.Editable<DiTreeEntity>)
                semanticEditor.getEditableVersion().getEditableFields().getFirst();
        definitionField.setValue(seededDefinition);
        semanticEditor.save(); // Save to create an uncommitted version holding the definition

        SemanticEntity<SemanticEntityVersion> semantic = EntityHandle.get(semanticEditor.getEntity().nid())
                .asSemantic().orElseThrow();

        // Outside the Publish flow the PUBLISH handler runs synchronously and flips a CREATE
        // window to EDIT when the seeded set was the last unmet requirement and the concept
        // actually got committed — announce that like the properties panel's submit does. (In
        // the Publish-flow window the mode only flips on the toolbar's Publish button, so the
        // condition below stays false and this stays quiet.)
        boolean wasCreateMode = genPurposeViewModel.getMode() == FormMode.CREATE;
        EvtBusFactory.getDefaultEvtBus().publish(genPurposeViewModel.getPropertyValue(ViewModelKey.WINDOW_TOPIC),
                new GenPurposeEvent(this, GenPurposeEvent.PUBLISH, List.of(seededDefinition), semantic));
        if (wasCreateMode && genPurposeViewModel.getMode() == FormMode.EDIT) {
            toast().show(Toast.Status.SUCCESS, "Concept created");
        }
    }

    /**
     * A definition holding one necessary or sufficient set whose only member is an is-a to
     * "Anonymous concept" — the same seed the classic concept window writes
     * ({@code ConceptViewModel.createConcept}).
     */
    private static DiTreeEntity seedDefinition(boolean necessary) {
        LogicalExpressionBuilder builder = new LogicalExpressionBuilder();
        if (necessary) {
            builder.NecessarySet(builder.And(builder.ConceptAxiom(TinkarTerm.ANONYMOUS_CONCEPT.nid())));
        } else {
            builder.SufficientSet(builder.And(builder.ConceptAxiom(TinkarTerm.ANONYMOUS_CONCEPT.nid())));
        }
        return switch (builder.build().sourceGraph()) {
            case DiTreeEntity tree -> tree;
            case DiTreeEntity.Builder treeBuilder -> treeBuilder.build();
            default -> throw new IllegalStateException("Unexpected source graph type");
        };
    }

    private void setupProperties() {
        this.propertiesController = new GenPurposePropertiesController(genPurposeViewModel);
        this.propertiesBorderPane = this.propertiesController.getNode();
        attachPropertiesViewSlideoutTray(this.propertiesBorderPane);

        // open the panel, allow the state machine to determine which panel to show
        // listen for open and close events
        Subscriber<KLPropertyPanelEvent> propertiesEventSubscriber = (evt) -> {
            if (evt.getEventType() == CLOSE_PANEL) {
                LOG.info("propBumpOutListener - Close Properties bumpout toggle = " + windowControlToolbar.isPropertiesSelected());
                windowControlToolbar.setPropertiesSelected(false);
                if (isOpen(propertiesSlideoutTrayPane)) {
                    slideIn(propertiesSlideoutTrayPane, detailsOuterBorderPane);
                }

                updateDraggableNodesForPropertiesPanel(false);

                // Turn off edit mode for all read only controls
//                for (Node node : nodes) {
//                    KLReadOnlyBaseControl klReadOnlyBaseControl = (KLReadOnlyBaseControl) node;
//                    klReadOnlyBaseControl.setEditMode(false);
//                }
            } else if (evt.getEventType() == OPEN_PANEL || evt.getEventType() == NO_SELECTION_MADE_PANEL) {
                openPropertiesPanel();
            }
        };
//        subscriberList.add(propertiesEventSubscriber);
        EvtBusFactory.getDefaultEvtBus().subscribe(genPurposeViewModel.getPropertyValue(ViewModelKey.WINDOW_TOPIC), KLPropertyPanelEvent.class, propertiesEventSubscriber);
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
     * Updates draggable behavior for the properties panel based on its open/closed state.
     * <p>     * When opened, adds the properties tabs pane as a draggable node. When closed,
     * safely removes the draggable behavior to prevent memory leaks.
     *
     * @param isOpen {@code true} to add draggable nodes, {@code false} to remove them
     */
    private void updateDraggableNodesForPropertiesPanel(boolean isOpen) {
        if (propertiesController != null && propertiesController.getPropertiesTabs() != null) {
            if (isOpen) {
                addDraggableNodes(detailsOuterBorderPane, propertiesController.getPropertiesTabs());
                LOG.debug("Added properties nodes as draggable");
            } else {
                removeDraggableNodes(detailsOuterBorderPane, propertiesController.getPropertiesTabs());
                LOG.debug("Removed properties nodes from draggable");
            }
        }
    }

    public void init(KometPreferences editorWindowPreferences, ViewProperties viewProperties) {
        this.viewProperties = viewProperties;

        final ViewCalculator viewCalculator = viewProperties.calculator();

        String absolutePath = editorWindowPreferences.absolutePath();
        Path path = Paths.get(absolutePath);
        String lastDirName = path.getFileName().toString();
        String windowTitle = lastDirName;
        windowControlToolbar.setTitle(lastDirName.substring(0, 1).toUpperCase() + lastDirName.substring(1));

        editorWindowModel = EditorWindowManager.loadWindowModel(editorWindowPreferences, viewCalculator, windowTitle);

        // The standard Concept window gets the classic concept window's blue chrome (see
        // .concept-window-theme in kview.css) and its own set of properties tabs. User-created
        // Semantics Windows and the other standard windows keep the default grey chrome and tabs.
        if (editorWindowModel.getWindowType() == EditorWindowType.STANDARD_CONCEPT) {
            detailsOuterBorderPane.getStyleClass().add("concept-window-theme");
            propertiesController.getPropertiesTabs().getTabs().setAll(
                    Tab.ADD_EDIT, Tab.HIERARCHY, Tab.HISTORY, Tab.COMMENTS);
        } else if (editorWindowModel.getWindowType() == EditorWindowType.STANDARD_PATTERN) {
            // The standard Pattern window also edits its pattern's field defaults — the values
            // new semantics of the pattern start with in every KL window (see PatternFieldDefaults)
            // — once the pattern exists: the DEFAULTS tab and the toolbar's field-defaults button
            // appear when the window leaves create mode.
            genPurposeViewModel.modeProperty().subscribe(mode -> {
                boolean patternExists = mode == FormMode.EDIT;
                propertiesController.getPropertiesTabs().getTabs().setAll(patternExists
                        ? List.of(Tab.ADD_EDIT, Tab.DEFAULTS, Tab.COMMENTS)
                        : List.of(Tab.ADD_EDIT, Tab.COMMENTS));
                windowControlToolbar.setFieldDefaultsVisible(patternExists);
            });
        }

        // The create-mode hint names the kind of component this window will create.
        componentKindString = switch (editorWindowModel.getWindowType()) {
            case STANDARD_CONCEPT -> "Concept";
            case STANDARD_PATTERN -> "Pattern";
            case STANDARD_SEMANTIC, SEMANTICS -> "Semantic";
        };
        createModeHintLabel.setText("This " + componentKindString
                + " doesn't exist yet - it's created when you fill out the required values and "
                + (usesPublishFlow() ? "hit Publish." : "submit."));

        // The Publish UX — the toolbar Publish button and staged-until-published changes — is
        // scoped to the standard Pattern window for now; the other window types keep committing
        // on each properties-panel submit (see the PUBLISH event handler and the fields
        // controller's submit toast, both of which branch on this).
        windowControlToolbar.setPublishVisible(usesPublishFlow());
        genPurposeViewModel.setPropertyValue(ViewModelKey.PUBLISH_FLOW, usesPublishFlow());

        // Apply the Window settings authored in the KL editor (this window shares the same model).
        applyEditorWindowSettings();

        EditorSectionModel mainSection = editorWindowModel.getMainSection();

        // Main TitledPane
        TitledPane mainTitledPane = createTitledPane(mainSection);
        addPatternViewsOfSection(mainSection.getPatterns());
        addSupplementalAreaViewsOfSection(mainSection);
        mainContent.getItems().add(mainTitledPane);

        mainSection.getPatterns().addListener((ListChangeListener<? super EditorPatternModel>) this::onSectionPatternsChanged);

        // Additional Sections
        editorWindowModel.getAdditionalSections().forEach(section -> {
            TitledPane titledPane = createTitledPane(section);
            addPatternViewsOfSection(section.getPatterns());
            addSupplementalAreaViewsOfSection(section);
            mainContent.getItems().add(titledPane);
        });

        // TODO: will sections need to be refreshed on coordinate changes?
        editorWindowModel.getAdditionalSections().addListener(this::onAdditionalSectionsChanged);

        // Initial view update
        updateView();

        if (genPurposeViewModel.getMode() == FormMode.CREATE) {
            populateStampFromEditCoordinate();
        }

        // Sections exist now — show the required-pattern chips (create mode only).
        updateRequiredChips();

        // Inline stated-definition edits persist without a PUBLISH event, so track them here:
        // a change to any stated-axiom semantic re-evaluates the required chips.
        statedDefinitionChangeSubscriber = changedNid -> EntityHandle.get(changedNid).asSemantic()
                .filter(semantic -> semantic.patternNid() == statedAxiomsPatternNid())
                .ifPresent(semantic -> Platform.runLater(this::updateRequiredChips));
        Entity.provider().addSubscriberWithWeakReference(statedDefinitionChangeSubscriber);
    }

    /**
     * Applies the Window settings authored in the KL editor — the control-bar options (Coordinate and
     * Timeline icons) and the Window's view size — to this realized Window. The editor and this Window
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
        EntityFacade refConcept = (EntityFacade) genPurposeViewModel.getProperty(ViewModelKey.REF_COMPONENT).getValue();
        if (refConcept != null) {
            updateDisplayIdentifier(refConcept);
            updateWindowTitle(refConcept);
            updateStampControl(refConcept);
        } else {
            LOG.warn("ViewModelKey.REF_COMPONENT is null, cannot update view.");
            // TODO: Handle null refConcept case appropriately. Display no data found in UI.
        }
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
            semanticsOfPattern = getSemanticsOfPattern(patternReferenceComponent);
            titledPane.getReferenceComponents().addAll(semanticsOfPattern);

            patternReferenceComponentToSectionTitledPane.put(patternReferenceComponent, titledPane);
        }
        titledPane.setReferenceComponentCellFactory(_ -> createSectionSemanticsComboBoxCell(viewProperties));
        titledPane.setReferenceComponentButtonCellFactory(new SectionSemanticsComboBoxCell(viewProperties));

        titledPane.setOnEditAction(actionEvent -> onEditAction(actionEvent, sectionModel));

        titledPane.editEnabledProperty().bind(sectionModel.referenceComponentProperty().isNull()
                .or(Bindings.isNotEmpty(titledPane.getReferenceComponents())));

        sectionModelToTitledPane.put(sectionModel, titledPane);

        // Collapsing a section closes the window by the height the section's content occupied, so
        // that height leaves the window instead of turning into blank space at the bottom of the
        // last section (komet-desktop#159).
        titledPane.expandedProperty().subscribe((_, isExpanded) -> resizeWindowWithSection(titledPane, isExpanded));

        // When this section's resolved reference component changes, cascade to any section that anchors
        // on it (i.e. whose reference pattern is displayed in this section), so downstream sections in a
        // reference-component chain re-resolve and re-populate (see komet-desktop #3).
        titledPane.selectedReferenceComponentProperty().subscribe(() -> refreshSectionsAnchoredOn(sectionModel));

        return titledPane;
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

        detailsOuterBorderPane.setPrefHeight(Math.min(KLWorkspace.MAX_WINDOW_HEIGHT, windowHeight + delta));
    }

    private SectionSemanticsComboBoxCell createSectionSemanticsComboBoxCell(ViewProperties viewProperties) {
        SectionSemanticsComboBoxCell sectionSemanticsComboBoxCell = new SectionSemanticsComboBoxCell(viewProperties);
        sectionSemanticsComboBoxCell.hoverProperty().subscribe(() -> {
            SemanticEntity<SemanticEntityVersion> semanticEntity = (SemanticEntity<SemanticEntityVersion>) sectionSemanticsComboBoxCell.getItem();
            PatternSemanticsPresenter patternSemanticsPresenter = semanticEntityToPatternSemanticsPresenter.get(semanticEntity);

            if (patternSemanticsPresenter != null) {
                if (sectionSemanticsComboBoxCell.isHover()) {
                    patternSemanticsPresenter.setPreviewingSemantic(semanticEntity);
                } else {
                    patternSemanticsPresenter.setPreviewingSemantic(null);
                }
            }
        });
        return sectionSemanticsComboBoxCell;
    }

    /**
     * The section's stated definition pattern, or null when the section doesn't hold it.
     */
    private EditorPatternModel sectionStatedPattern(EditorSectionModel sectionModel) {
        return sectionModel.getPatterns().stream()
                .filter(pattern -> pattern.getNid() == statedAxiomsPatternNid())
                .findFirst().orElse(null);
    }

    /**
     * Whether new semantics of the pattern can be added right now. In create mode they always can;
     * once the window is in edit mode the pattern must allow new semantics, as authored in the KL
     * Editor. Existing semantics stay editable either way.
     */
    private boolean canAddSemantics(EditorPatternModel patternModel) {
        return genPurposeViewModel.getMode() == FormMode.CREATE || patternModel.isAllowNewSemantics();
    }

    private void onEditAction(ActionEvent actionEvent, EditorSectionModel sectionModel) {
        SectionEditPopup popup = new SectionEditPopup();

        // The Reference Component to use
        EntityFacade refComponent;

        if (sectionModel.getReferenceComponent() == null) {
            refComponent = genPurposeViewModel.getPropertyValue(ViewModelKey.REF_COMPONENT);
        } else {
            SectionTitledPane<EntityFacade> sectionTitledPane = sectionModelToTitledPane.get(sectionModel);
            refComponent = sectionTitledPane.getSelectedReferenceComponent();
        }

        // In create mode the standard Concept/Pattern window has no reference component yet — the
        // new component is created lazily when the user authors the first semantic (see
        // onCreateSemantic), so the popup still opens, offering only its create entries.
        boolean canCreateReferenceComponent = sectionModel.getReferenceComponent() == null
                && genPurposeViewModel.getMode() == FormMode.CREATE
                && (editorWindowModel.getWindowType() == EditorWindowType.STANDARD_CONCEPT
                        || editorWindowModel.getWindowType() == EditorWindowType.STANDARD_PATTERN);

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
                        // below, never as one of the pattern's semantics.
                        if (PatternFieldDefaults.isDefaultsSemantic(semantic)) {
                            return;
                        }
                        KometLabel semanticLabel = new KometLabel(semantic, viewProperties);
                        semanticLabel.setShowTooltip(true);

                        semanticLabel.setOnMouseClicked(_ -> {
                            initializeComposer();
                            showEditSemanticFieldsPanel(actionEvent, semantic, editPattern);
                            popup.hide();
                        });

                        semanticLabel.hoverProperty().subscribe(() -> {
                            PatternSemanticsPresenter patternSemanticsPresenter = semanticEntityToPatternSemanticsPresenter.get(semantic);

                            if (patternSemanticsPresenter == null) {
                                // The popup lists every semantic of the pattern, while the details area
                                // shows only the ones passing the pattern's display filters — e.g. the
                                // standard Concept window's Description columns, which between them show
                                // fully qualified names, other names and definitions, so a description of
                                // any other type has no view to preview.
                                return;
                            }

                            if (semanticLabel.isHover()) {
                                patternSemanticsPresenter.setPreviewingSemantic(semantic);
                            } else {
                                patternSemanticsPresenter.setPreviewingSemantic(null);
                            }
                        });

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
                                () -> createSeededStatedDefinition(sectionModel, statedPattern, false))));
                createEntries.add(new CreateEntry(statedPattern, Map.of(),
                        new SectionEditPopup.CreateAction("Add necessary set",
                                () -> createSeededStatedDefinition(sectionModel, statedPattern, true))));
            }
        } else {
            for (EditorPatternModel patternModel : sectionModel.getPatterns()) {
                // Once in edit mode, only patterns authored in the KL Editor as allowing new
                // semantics accept them.
                if (!canAddSemantics(patternModel)) {
                    continue;
                }
                addCreateEntries(createEntries, actionEvent, sectionModel, patternModel, refComponent);
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
            CreateEntry directEntry = directCreateEntry(sectionModel, createEntries);
            if (directEntry != null) {
                directEntry.createAction().action().run();
                return;
            }
        }

        // Show Popup
        SectionTitledPane<?> sectionTitledPane = sectionModelToTitledPane.get(sectionModel);
        Point2D screenPoint = sectionTitledPane.localToScreen(
                sectionTitledPane.getWidth(),
                0
        );
        popup.show(sectionTitledPane, screenPoint.getX(), screenPoint.getY());
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
    private void addCreateEntries(List<CreateEntry> createEntries, ActionEvent actionEvent,
            EditorSectionModel sectionModel, EditorPatternModel patternModel, EntityFacade refComponent) {
        if (patternModel.getSemanticFilters().isEmpty()) {
            createEntries.add(new CreateEntry(patternModel, Map.of(), new SectionEditPopup.CreateAction(
                    "Add " + stripPatternSuffix(patternModel.getTitle()), () -> {
                        initializeComposer();
                        onCreateSemantic(actionEvent, sectionModel, patternModel, refComponent, Map.of());
                    })));
            return;
        }

        for (EditorPatternSemanticFilter filter : patternModel.getSemanticFilters()) {
            String filterConceptNames = filter.getFieldConstraints().values().stream()
                    .map(filterConcept -> getViewProperties().calculator()
                            .getPreferredDescriptionTextWithFallbackOrNid(filterConcept.nid()))
                    .distinct()
                    .collect(Collectors.joining(", "));
            // A filter constraining nothing selects every semantic — its entry falls back to the
            // pattern's name.
            String entryName = filterConceptNames.isEmpty()
                    ? patternModel.getTitle()
                    : filterConceptNames;

            // Snapshot the constraints at popup-build time; they seed the new semantic's fields.
            Map<Integer, EntityProxy> fieldSeeds = Map.copyOf(filter.getFieldConstraints());
            createEntries.add(new CreateEntry(patternModel, fieldSeeds, new SectionEditPopup.CreateAction(
                    "Add " + stripPatternSuffix(entryName), () -> {
                        initializeComposer();
                        onCreateSemantic(actionEvent, sectionModel, patternModel, refComponent, fieldSeeds);
                    })));
        }
    }

    /**
     * One "Add …" entry of a section's edit popup, kept with the pattern it creates a semantic of
     * and the field values that new semantic starts out with (the constraints of the display
     * filter the entry was built from, keyed by field index — empty for an unfiltered pattern),
     * so {@link #directCreateEntry} can tell which entry meets a requirement.
     */
    private record CreateEntry(EditorPatternModel pattern, Map<Integer, EntityProxy> fieldSeeds,
                               SectionEditPopup.CreateAction createAction) {
    }

    /**
     * The create entry the section's pencil button runs right away, skipping the popup, when the
     * section has no semantic to edit — or null when the user has to choose. A lone entry is the
     * only thing the popup could offer, so it runs. Otherwise the section's unmet required
     * patterns decide: the entry seeded to meet the pattern's first unmet requirement (see
     * {@link EditorPatternRequirement}), or the pattern's single entry when it has no refinement
     * or no entry is seeded to meet it (the user then picks the field values in the form).
     * Several candidates — the stated definition's two set seeds, a required pattern with two
     * filters and no refinement — leave the choice to the popup.
     */
    private CreateEntry directCreateEntry(EditorSectionModel sectionModel, List<CreateEntry> createEntries) {
        if (createEntries.size() == 1) {
            return createEntries.getFirst();
        }

        for (EditorPatternModel pattern : sectionModel.getPatterns()) {
            if (!pattern.isRequired() || isRequiredPatternSatisfied(pattern)) {
                continue;
            }
            List<CreateEntry> patternEntries = createEntries.stream()
                    .filter(entry -> entry.pattern() == pattern)
                    .toList();
            List<CreateEntry> candidates = patternEntries;

            List<EntityFacade> semantics = getSemanticsOfPattern(pattern);
            for (EditorPatternRequirement requirement : pattern.getRequirements()) {
                if (isRequirementMet(semantics, requirement)) {
                    continue;
                }
                List<CreateEntry> meetingRequirement = patternEntries.stream()
                        .filter(entry -> seedsMeet(entry.fieldSeeds(), requirement))
                        .toList();
                if (!meetingRequirement.isEmpty()) {
                    candidates = meetingRequirement;
                }
                break;
            }

            if (candidates.size() == 1) {
                return candidates.getFirst();
            }
        }
        return null;
    }

    /**
     * Whether a semantic seeded with the passed in field values (field index to concept) would
     * match the requirement's field constraints.
     */
    private static boolean seedsMeet(Map<Integer, EntityProxy> fieldSeeds, EditorPatternRequirement requirement) {
        return requirement.getFieldConstraints().entrySet().stream().allMatch(constraint -> {
            EntityProxy seed = fieldSeeds.get(constraint.getKey());
            return seed != null && seed.nid() == constraint.getValue().nid();
        });
    }

    /**
     * The passed in name without a trailing "Pattern" word — "Description Pattern" makes the
     * create entry "Add Description". A name that is nothing but that word is kept whole.
     */
    private static String stripPatternSuffix(String name) {
        String trimmed = name.strip();
        int suffixStart = trimmed.length() - "Pattern".length();
        boolean endsWithPatternWord = suffixStart > 0
                && Character.isWhitespace(trimmed.charAt(suffixStart - 1))
                && trimmed.regionMatches(true, suffixStart, "Pattern", 0, "Pattern".length());
        return endsWithPatternWord ? trimmed.substring(0, suffixStart).strip() : trimmed;
    }

    private void onCreateSemantic(ActionEvent actionEvent, EditorSectionModel sectionModelOfPattern,
                                  EditorPatternModel editorPatternModel, EntityFacade refComponent,
                                  Map<Integer, EntityProxy> fieldSeeds) {
        // Lazy reference-component creation: in create mode the window has no component yet — the
        // first semantic the user authors brings the new component into existence with it. Both
        // join the composer's transaction, so submitting the semantic commits them together.
        if (refComponent == null) {
            refComponent = createUncommitedReferenceComponent();
        }

        PatternFacade patternFacade = PatternFacade.make(editorPatternModel.getNid());

        // Create uncommited Semantic
        SemanticEntity<SemanticEntityVersion> uncommitedSemantic = createUncommitedSemantic(refComponent, patternFacade, fieldSeeds);

        PatternSemanticsPresenter patternSemanticsPresenter = editorPatternModelToPatternPresenter.get(editorPatternModel);

        // Add content to Pattern inside Section
        if (patternSemanticsPresenter == null) {
            // First time adding a Semantic for the given Pattern
            addPatternViewsOfSection(sectionModelOfPattern.getPatterns());
        } else {
            // Not the first time adding a Semantic for this Pattern so we just add the new one below the existing ones
            patternSemanticsPresenter.addNewSemantic(uncommitedSemantic);
        }

        // If there are Section TitledPanes that have this Pattern as a Reference Component update them
        SectionTitledPane<EntityFacade> sectionTitledPane = patternReferenceComponentToSectionTitledPane.get(editorPatternModel);
        if (sectionTitledPane != null) {
            sectionTitledPane.getReferenceComponents().add(uncommitedSemantic);
            // If this is going to be the first Semantic, have it selected
            if (sectionTitledPane.getReferenceComponents().size() == 1) {
                sectionTitledPane.setSelectedReferenceComponent(uncommitedSemantic);
            }
        }

        // Show Edit Panel to the right
        showEditSemanticFieldsPanel(actionEvent, uncommitedSemantic, editorPatternModel);
    }

    /**
     * Opens the properties panel on the semantic's edit form. The pattern model is the KL Editor's
     * placement of the semantic's pattern, whose fields say how the form's fields behave (e.g.
     * whether they can still be edited in edit mode).
     */
    private void showEditSemanticFieldsPanel(Event event, SemanticEntity<SemanticEntityVersion> semanticEntity,
                                             EditorPatternModel editorPatternModel) {
        // Notify bump out (right side) to display edit fields in Semantic Editing mode
        EvtBusFactory.getDefaultEvtBus()
                .publish(genPurposeViewModel.getPropertyValue(ViewModelKey.WINDOW_TOPIC),
                        new KLPropertyPanelEvent(event.getSource(),
                                SHOW_EDIT_SEMANTIC_FIELDS, semanticEntity, editorPatternModel));
        // Notify to open properties bump out.
        EvtBusFactory.getDefaultEvtBus().publish(genPurposeViewModel.getPropertyValue(ViewModelKey.WINDOW_TOPIC), new KLPropertyPanelEvent(event.getSource(), OPEN_PANEL));

        // Turn on Edit mode on the left side for the Semantic being edited
        if (previousPatternSemanticsInEditMode != null) {
            previousPatternSemanticsInEditMode.setEditingSemantic(null);
        }
        PatternSemanticsPresenter patternSemanticsPresenter = semanticEntityToPatternSemanticsPresenter.get(semanticEntity);
        if (patternSemanticsPresenter != null) {
            patternSemanticsPresenter.setEditingSemantic(semanticEntity);
        }
        previousPatternSemanticsInEditMode = patternSemanticsPresenter;
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
        EntityFacade refComponent = genPurposeViewModel.getPropertyValue(ViewModelKey.REF_COMPONENT);
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

        initializeComposer();

        // The model always supplies a factory (it defaults to the Standard factory), so no fallback is needed here.
        KlPatternSemanticsFactory klPatternSemanticsFactory = editorPatternModel.getFactory();

        PatternSemanticsPresenter patternSemanticsPresenter = klPatternSemanticsFactory.createJournalControl(editorPatternModel,
                viewProperties, composer, genPurposeViewModel.getPropertyValue(CURRENT_JOURNAL_WINDOW_TOPIC));

        if (!refComponents.isEmpty()) {
            doAddSemanticViews(editorPatternModel, patternSemanticsPresenter, refComponents.getFirst());
            titledPane.setSelectedReferenceComponent(refComponents.getFirst());
        }

        titledPane.selectedReferenceComponentProperty().subscribe(() -> {
            patternSemanticsPresenter.clearSemantics();
            doAddSemanticViews(editorPatternModel, patternSemanticsPresenter, titledPane.getSelectedReferenceComponent());
        });

        return patternSemanticsPresenter;
    }

    private void reloadSemanticViews(SemanticEntity<SemanticEntityVersion> semantic) {
        editorPatternModelToPatternPresenter.forEach((patternModel, presenter) -> {
            if (patternModel.getNid() == semantic.patternNid()) {
                presenter.clearSemantics();
                doAddSemanticViews(patternModel, presenter, semantic.referencedComponent());
            }
        });
    }

    private void doAddSemanticViews(EditorPatternModel editorPatternModel, PatternSemanticsPresenter patternSemanticsPresenter, EntityFacade referenceComponent) {
        if (referenceComponent == null) {
            // No reference concept selected — nothing to render for this pattern's semantics.
            return;
        }

        // Pattern Entity
        int patternNid = editorPatternModel.getNid();
        EntityHandle handle = EntityHandle.get(patternNid);
        PatternEntity patternEntity;
        if (handle.asPattern().isEmpty()) {
            throw new RuntimeException("Expecting a Pattern to be present instead of an empty Optional");
        }
        patternEntity = handle.asPattern().get();

        // Composer
        initializeComposer();

        // Start adding Semantics — skipping the ones the pattern's display filters hide (see
        // EditorPatternSemanticFilter), e.g. a Description pattern showing only fully qualified names.
        EntityService.get().forEachSemanticForComponentOfPattern(referenceComponent.nid(), patternEntity.nid(),
                (semantic) -> {
                    // A pattern's defaults semantic is a semantic of the pattern referencing
                    // the pattern itself; it holds the defaults, not content (see PatternFieldDefaults).
                    if (PatternFieldDefaults.isDefaultsSemantic(semantic)) {
                        return;
                    }
                    if (!editorPatternModel.displaysSemantic(semantic.nid(), getViewProperties().calculator())) {
                        return;
                    }
                    patternSemanticsPresenter.addNewSemantic(semantic);
                    semanticEntityToPatternSemanticsPresenter.put(semantic, patternSemanticsPresenter);
                });
    }

    /**
     * Composer for the window pattern's defaults semantic (see {@link PatternFieldDefaults}).
     * The defaults semantic commits in the defaults module rather than the edit coordinate's
     * default module, so it gets a transaction of its own. It is created on first use and handed
     * to the DEFAULTS form, whose Publish button commits it — the defaults are not part of the
     * window's own staged changes. The instance is kept for the window's lifetime: a committed
     * composer opens a fresh transaction on its next compose.
     */
    private ObservableComposer defaultsComposer;

    private void initializeDefaultsComposer() {
        if (defaultsComposer != null) {
            return;
        }
        var editCoordinate = getViewProperties().nodeView().editCoordinate();
        defaultsComposer = ObservableComposer.create(
                getViewProperties().calculator(),
                State.ACTIVE,
                editCoordinate.getAuthorForChanges(),
                KlTerms.FIELD_DEFAULTS_MODULE,
                editCoordinate.getDefaultPath(),
                "Edit pattern field defaults"
        );
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
        EntityFacade pattern = genPurposeViewModel.getPropertyValue(ViewModelKey.REF_COMPONENT);
        if (pattern == null) {
            // Create mode: the pattern doesn't exist yet, so neither can its defaults (the tab and
            // button only show once it does — this is the tab remembered across a panel reopen).
            return;
        }
        initializeDefaultsComposer();

        // Before composing: composing mints the defaults semantic's nid, after which the store knows the
        // identity whether or not a semantic has been written under it.
        boolean defaultsSemanticExists = PatternFieldDefaults.defaultsSemantic(pattern.nid()).isPresent();

        ObservablePattern observablePattern = ObservableEntityHandle.get(pattern.nid()).expectPattern();
        ObservableComposer.EntityComposer<ObservableSemanticVersion.Editable, ObservableSemantic> defaultsSemanticComposer =
                defaultsComposer.composeSemantic(PatternFieldDefaults.defaultsSemanticId(pattern.publicId()),
                        observablePattern, observablePattern);
        if (!defaultsSemanticExists) {
            defaultsSemanticComposer.save(); // Save to create an uncommitted version
        }
        SemanticEntity<SemanticEntityVersion> defaultsSemantic = EntityHandle.get(defaultsSemanticComposer.getEntity().nid()).asSemantic()
                .orElseThrow(() -> new IllegalStateException("The defaults semantic is not a semantic"));

        EvtBusFactory.getDefaultEvtBus().publish(genPurposeViewModel.getPropertyValue(ViewModelKey.WINDOW_TOPIC),
                new KLPropertyPanelEvent(this, SHOW_PATTERN_FIELD_DEFAULTS, defaultsSemantic,
                        defaultsComposer, "Field Default Values"));
        EvtBusFactory.getDefaultEvtBus().publish(genPurposeViewModel.getPropertyValue(ViewModelKey.WINDOW_TOPIC),
                new KLPropertyPanelEvent(this, OPEN_PANEL));
    }

    private void initializeComposer() {
        if (composer != null) {
            return;
        }

        ConceptFacade author = getViewProperties().nodeView().editCoordinate().getAuthorForChanges();
        ConceptFacade module = getViewProperties().nodeView().editCoordinate().getDefaultModule();
        ConceptFacade path = getViewProperties().nodeView().editCoordinate().getDefaultPath();

        composer = ObservableComposer.create(
                getViewProperties().calculator(),
                State.ACTIVE,
                author,
                module,
                path,
                "Edit Semantic Details"
        );

        // The Publish button follows the transaction's staged changes (see updatePublishState).
        composer.hasUncommittedChangesProperty().subscribe(this::updatePublishState);

        genPurposeViewModel.setPropertyValue(ViewModelKey.COMPOSER, composer);
    }

    /**
     * The component a section's rows resolve against: the window's reference component for a section
     * with no reference pattern, otherwise the component currently selected for that section. This is
     * what lets reference-component chains resolve to arbitrary depth — a downstream section anchors on
     * the resolved component of the section that owns its reference pattern, rather than always anchoring
     * on the window component (see komet-desktop #3).
     */
    private EntityFacade resolveSectionReferenceComponent(EditorSectionModel section) {
        if (section == null || section.getReferenceComponent() == null) {
            return genPurposeViewModel.getPropertyValue(ViewModelKey.REF_COMPONENT);
        }
        SectionTitledPane<EntityFacade> titledPane = sectionModelToTitledPane.get(section);
        return titledPane == null ? null : titledPane.getSelectedReferenceComponent();
    }

    private List<EntityFacade> getReferenceComponentsToUse(EditorPatternModel sectionReferenceComponent) {
        List<EntityFacade> refComponents = new ArrayList<>();

        if (sectionReferenceComponent != null) {
            // Anchor on the resolved reference component of the section that owns the reference pattern,
            // so chains where one section references another section's semantic resolve to any depth.
            EntityFacade base = resolveSectionReferenceComponent(sectionReferenceComponent.getParentSection());
            if (base != null) {
                EntityService.get().forEachSemanticForComponentOfPattern(base.nid(), sectionReferenceComponent.getNid(),
                        (SemanticEntity<SemanticEntityVersion> semantic) -> {
                            refComponents.add(semantic);
                        }
                );
            }
        } else {
            // No section reference pattern — the section resolves directly against the window component.
            // Never return a list containing null (callers treat a non-empty list as having a usable component).
            EntityFacade windowRefComponent = genPurposeViewModel.getPropertyValue(ViewModelKey.REF_COMPONENT);
            if (windowRefComponent != null) {
                refComponents.add(windowRefComponent);
            }
        }

        return refComponents;
    }

    private List<EntityFacade> getSemanticsOfPattern(EditorPatternModel editorPatternModel) {
        List<EntityFacade> refComponents = new ArrayList<>();

        // Anchor on the resolved reference component of the section that owns this reference pattern, so
        // a section referencing another section's semantic populates its options (see komet-desktop #3).
        EntityFacade base = resolveSectionReferenceComponent(editorPatternModel.getParentSection());
        if (base == null) {
            return refComponents;
        }

        EntityService.get().forEachSemanticForComponentOfPattern(base.nid(), editorPatternModel.getNid(),
                (SemanticEntity<SemanticEntityVersion> semantic) -> {
                    refComponents.add(semantic);
                }
        );

        return refComponents;
    }

    /**
     * Visits every section in this window (the main section and all additional sections).
     */
    private void forEachSectionInWindow(Consumer<EditorSectionModel> action) {
        if (editorWindowModel == null) {
            return;
        }
        action.accept(editorWindowModel.getMainSection());
        editorWindowModel.getAdditionalSections().forEach(action);
    }

    /**
     * Refreshes each section's required-pattern chip (see the REQUIRED / "✓ REQUIREMENT MET"
     * chip in the section title bar): shown in create mode on sections hosting a required
     * pattern, flipping to satisfied once every required pattern in the section is satisfied
     * ({@link #isRequiredPatternSatisfied}) — the same check that gates the Publish button in
     * create mode, so the button re-evaluates with the chips.
     */
    private void updateRequiredChips() {
        boolean createMode = genPurposeViewModel.getMode() == FormMode.CREATE;
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
        updatePublishState();
    }

    /**
     * Whether this window uses the toolbar Publish flow: changes stage in the composer's open
     * transaction until the Publish button commits them. Scoped to the standard Pattern window
     * for now — the other window types keep the classic commit-on-submit flow (and hide the
     * Publish button) until they adopt the Publish UX too.
     */
    private boolean usesPublishFlow() {
        return editorWindowModel != null
                && editorWindowModel.getWindowType() == EditorWindowType.STANDARD_PATTERN;
    }

    /**
     * Recomputes the toolbar Publish button's enablement and tooltip. Publishing needs staged
     * changes in the composer's open transaction, and in create mode additionally every required
     * pattern satisfied ({@link #allRequiredPatternsSatisfied}) — the component only comes into
     * existence complete. Runs whenever those inputs may have moved: with the required chips
     * (mode changes, PUBLISH submits, inline stated-definition edits) and on the composer's
     * change tracking (see {@link #initializeComposer}).
     */
    private void updatePublishState() {
        boolean hasStagedChanges = composer != null && composer.hasUncommittedChanges();
        boolean disabled;
        String publishTooltip;
        if (genPurposeViewModel.getMode() == FormMode.CREATE) {
            disabled = !hasStagedChanges || !allRequiredPatternsSatisfied();
            publishTooltip = disabled ? "Complete the required semantics to publish" : "Publish";
        } else {
            disabled = !hasStagedChanges;
            publishTooltip = disabled ? "No changes to publish" : "Publish";
        }
        windowControlToolbar.setPublishDisable(disabled);
        windowControlToolbar.setPublishTooltip(publishTooltip);
    }

    /**
     * Runs when the toolbar's Publish button is pressed — the window's single commit point.
     * Commits the composer's open transaction, finalizing everything staged since the last
     * publish: submitted semantic versions and, in create mode, the lazily created reference
     * component itself. A CREATE window becomes an EDIT window on its first publish.
     */
    private void publish() {
        boolean wasCreateMode = genPurposeViewModel.getMode() == FormMode.CREATE;

        composer.commit();
        composer = null;
        initializeComposer();

        if (wasCreateMode) {
            genPurposeViewModel.setMode(FormMode.EDIT);
        }
        // The commit finalized the staged entities (in create mode the window's reference
        // component itself) — refresh the banner/identifier/STAMP from the committed state.
        updateView();
        updatePublishState();

        toast().show(Toast.Status.SUCCESS,
                wasCreateMode ? componentKindString + " created" : "Changes published");
    }

    /**
     * Whether every pattern marked Required in the KL editor is satisfied
     * ({@link #isRequiredPatternSatisfied}) against its section's resolved reference component.
     * In create mode this gates the actual creation (commit) of the window's component:
     * uncommitted semantics count, since they are found by the entity service once saved.
     */
    private boolean allRequiredPatternsSatisfied() {
        AtomicBoolean satisfied = new AtomicBoolean(true);
        forEachSectionInWindow(section -> {
            for (EditorPatternModel pattern : section.getPatterns()) {
                if (pattern.isRequired() && !isRequiredPatternSatisfied(pattern)) {
                    satisfied.set(false);
                }
            }
        });
        return satisfied.get();
    }

    /**
     * Whether a required pattern's requirement is met: at least one semantic against its
     * section's resolved reference component, plus — when the pattern carries requirement
     * refinements authored in the KL editor ({@link EditorPatternRequirement}) — at least
     * each refinement's minimum count of semantics matching its field constraints. The stated
     * definition pattern demands more — its definition must contain a necessary or sufficient
     * set, the same condition the classic concept window enforces before creating a concept
     * ({@code ConceptViewModel}'s AXIOM validation) — so removing the definition's last set
     * flips the requirement back to unmet.
     */
    private boolean isRequiredPatternSatisfied(EditorPatternModel pattern) {
        List<EntityFacade> semantics = getSemanticsOfPattern(pattern);
        if (semantics.isEmpty()) {
            return false;
        }
        if (pattern.getNid() == statedAxiomsPatternNid()
                && semantics.stream().noneMatch(this::definesNecessaryOrSufficientSet)) {
            return false;
        }
        return pattern.getRequirements().stream().allMatch(requirement -> isRequirementMet(semantics, requirement));
    }

    /**
     * Whether at least the requirement's minimum count of the passed in semantics match its field
     * constraints ({@link #matchesRequirement}).
     */
    private boolean isRequirementMet(List<EntityFacade> semantics, EditorPatternRequirement requirement) {
        return semantics.stream().filter(semantic -> matchesRequirement(semantic, requirement)).count()
                >= requirement.getMinCount();
    }

    /**
     * Whether the semantic's latest version (uncommitted versions count, like the
     * semantic-existence check) matches the requirement's field constraints.
     */
    private boolean matchesRequirement(EntityFacade semantic, EditorPatternRequirement requirement) {
        Latest<SemanticEntityVersion> latestVersion = getViewProperties().calculator().latest(semantic.nid());
        return latestVersion.isPresent() && requirement.matches(latestVersion.get().fieldValues());
    }

    /**
     * Whether the given stated-axiom semantic's latest definition (uncommitted versions count,
     * like the semantic-existence check) contains a necessary or sufficient set.
     */
    private boolean definesNecessaryOrSufficientSet(EntityFacade semantic) {
        Latest<SemanticEntityVersion> latestVersion = getViewProperties().calculator().latest(semantic.nid());
        if (latestVersion.isAbsent()) {
            return false;
        }
        return latestVersion.get().fieldValues().get(0) instanceof DiTreeEntity definition
                && (definition.containsVertexWithMeaning(TinkarTerm.NECESSARY_SET)
                        || definition.containsVertexWithMeaning(TinkarTerm.SUFFICIENT_SET));
    }

    /**
     * Re-resolves every section whose reference pattern lives in {@code changedSection}, so a change to
     * that section's reference component propagates down the reference-component chain. Each refreshed
     * section's own selection change drives the next hop, so the cascade naturally reaches arbitrary
     * depth (see komet-desktop #3).
     */
    private void refreshSectionsAnchoredOn(EditorSectionModel changedSection) {
        forEachSectionInWindow(section -> {
            EditorPatternModel referencePattern = section.getReferenceComponent();
            if (referencePattern != null && referencePattern.getParentSection() == changedSection) {
                refreshSectionReferenceComponents(section);
            }
        });
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

        List<EntityFacade> options = getSemanticsOfPattern(section.getReferenceComponent());
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

                    mainContent.getItems().add(titledPane);
                }
                updateRequiredChips();
            }
        }
    }
}