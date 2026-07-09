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
import static dev.ikm.komet.kview.fxutils.SlideOutTrayHelper.isClosed;
import static dev.ikm.komet.kview.fxutils.SlideOutTrayHelper.isOpen;
import static dev.ikm.komet.kview.fxutils.SlideOutTrayHelper.slideIn;
import static dev.ikm.komet.kview.fxutils.SlideOutTrayHelper.slideOut;
import static dev.ikm.komet.kview.fxutils.ViewportHelper.clipChildren;
import static dev.ikm.komet.layout_engine.window.DraggableSupport.addDraggableNodes;
import static dev.ikm.komet.layout_engine.window.DraggableSupport.removeDraggableNodes;
import static dev.ikm.komet.kview.klfields.KlFieldHelper.retrieveCommittedLatestVersion;
import static dev.ikm.komet.kview.mvvm.view.common.ChapterWindowHelper.setupViewCoordinateOptionsPopup;
import static dev.ikm.komet.kview.mvvm.viewmodel.ViewModelKey.CURRENT_JOURNAL_WINDOW_TOPIC;

import dev.ikm.komet.framework.Identicon;
import dev.ikm.komet.framework.controls.TimeUtils;
import dev.ikm.komet.framework.observable.ObservableComposer;
import dev.ikm.komet.framework.observable.ObservableConcept;
import dev.ikm.komet.framework.observable.ObservableConceptVersion;
import dev.ikm.komet.framework.observable.ObservableEntity;
import dev.ikm.komet.framework.observable.ObservableEntityHandle;
import dev.ikm.komet.framework.observable.ObservableEntitySnapshot;
import dev.ikm.komet.framework.observable.ObservablePattern;
import dev.ikm.komet.framework.observable.ObservableSemantic;
import dev.ikm.komet.framework.observable.ObservableSemanticVersion;
import dev.ikm.komet.framework.view.ViewProperties;
import dev.ikm.komet.kview.common.ViewCalculatorUtils;
import dev.ikm.komet.kview.controls.ComponentItem;
import dev.ikm.komet.kview.controls.KLWorkspace;
import dev.ikm.komet.kview.controls.KlWindowControlToolbar;
import dev.ikm.komet.layout.controls.FilterOptionsPopup;
import dev.ikm.komet.kview.controls.KometLabel;
import dev.ikm.komet.kview.controls.PublicIDListControl;
import dev.ikm.komet.kview.controls.SectionTitledPane;
import dev.ikm.komet.kview.controls.StampViewControl;
import dev.ikm.komet.kview.controls.SectionEditPopup;
import dev.ikm.komet.kview.controls.ComponentItemNode;
import dev.ikm.komet.kview.events.ClosePropertiesPanelEvent;
import dev.ikm.komet.kview.events.genpurpose.GenPurposeEvent;
import dev.ikm.komet.kview.events.genpurpose.KLPropertyPanelEvent;
import dev.ikm.komet.kview.mvvm.view.genpurpose.control.PropertiesTabsControl.Tab;
import dev.ikm.komet.kview.mvvm.view.genpurpose.control.SectionSemanticsComboBoxCell;
import dev.ikm.komet.kview.mvvm.view.genpurpose.control.standard.SemanticStandardControl;
import dev.ikm.komet.kview.mvvm.view.journal.VerticallyFilledPane;
import dev.ikm.komet.kview.mvvm.viewmodel.FormViewModel.FormMode;
import dev.ikm.komet.kview.mvvm.viewmodel.GenPurposeViewModel;
import dev.ikm.komet.layout.KlPatternSemanticsFactory;
import dev.ikm.komet.layout.PatternSemanticsPresenter;
import dev.ikm.komet.layout.editor.EditorWindowManager;
import dev.ikm.komet.layout.editor.model.EditorPatternModel;
import dev.ikm.komet.layout.editor.model.EditorSectionModel;
import dev.ikm.komet.layout.editor.model.EditorWindowModel;
import dev.ikm.komet.layout.editor.model.EditorWindowType;
import dev.ikm.komet.preferences.KometPreferences;
import dev.ikm.tinkar.common.id.PublicIds;
import dev.ikm.tinkar.coordinate.stamp.calculator.Latest;
import dev.ikm.tinkar.coordinate.view.calculator.ViewCalculator;
import dev.ikm.tinkar.entity.EntityHandle;
import dev.ikm.tinkar.entity.EntityService;
import dev.ikm.tinkar.entity.EntityVersion;
import dev.ikm.tinkar.entity.PatternEntity;
import dev.ikm.tinkar.entity.SemanticEntity;
import dev.ikm.tinkar.entity.SemanticEntityVersion;
import dev.ikm.tinkar.entity.StampEntity;
import dev.ikm.tinkar.events.EvtBusFactory;
import dev.ikm.tinkar.events.EvtType;
import dev.ikm.tinkar.events.Subscriber;
import dev.ikm.tinkar.terms.ConceptFacade;
import dev.ikm.tinkar.terms.EntityFacade;
import dev.ikm.tinkar.terms.PatternFacade;
import dev.ikm.tinkar.terms.State;
import javafx.beans.binding.Bindings;
import javafx.collections.ListChangeListener;
import javafx.event.ActionEvent;
import javafx.event.Event;
import javafx.fxml.FXML;
import javafx.geometry.Point2D;
import javafx.scene.Node;
import javafx.scene.control.SplitPane;
import javafx.scene.control.TitledPane;
import javafx.scene.control.Tooltip;
import javafx.scene.image.Image;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.GridPane;
import dev.ikm.komet.layout_engine.host.SupplementalAreaRenderer;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Pane;
import javafx.scene.layout.Priority;
import javafx.scene.layout.Region;
import javafx.scene.text.Text;
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
import java.util.concurrent.atomic.AtomicReference;
import java.util.function.Consumer;
import dev.ikm.komet.kview.mvvm.viewmodel.ViewModelKey;

public class GenPurposeDetailsController {

    private static final Logger LOG = LoggerFactory.getLogger(GenPurposeDetailsController.class);

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
    private SplitPane mainContent;
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
    private HBox tabHeader;
    @FXML
    private Text windowTitleLabel;
    private BorderPane propertiesBorderPane;
    private GenPurposePropertiesController propertiesController;
    private EditorWindowModel editorWindowModel;
    private ViewProperties viewProperties;
    @InjectViewModel
    private GenPurposeViewModel genPurposeViewModel;
    private Consumer<GenPurposeDetailsController> onCloseConceptWindow;

    private Subscriber<ClosePropertiesPanelEvent> closePropertiesPanelEventSubscriber;

    private ObservableComposer composer;

    @FXML
    private void initialize() {

        // Drive the coordinates menu from the relocated FilterOptionsPopup (ike-issues#661); the popup
        // writes the window's nodeView override, which the window's KL context + areas resolve through.
        filterOptionsPopup = setupViewCoordinateOptionsPopup(genPurposeViewModel.getViewProperties(),
                FilterOptionsPopup.FILTER_TYPE.CHAPTER_WINDOW, detailsOuterBorderPane,
                windowControlToolbar.getCoordinatesMenuButton(), this::updateView);

        // Wire the toolbar's behaviour: the close button takes an action, while the properties panel
        // reacts to the toggle's selected state (driven by user clicks or setPropertiesSelected).
        windowControlToolbar.setOnCloseAction(this::closeConceptWindow);
        windowControlToolbar.propertiesSelectedProperty()
                .subscribe((w) -> onPropertiesToggleChanged(windowControlToolbar.isPropertiesSelected()));

        // The header STAMP is view-only in this window — clicking it must not select it or open
        // the STAMP form.
        stampViewControl.setSelectable(false);

        // Setup Properties Bump out view
        setupProperties();

        Subscriber<GenPurposeEvent> refreshSubscriber = evt -> {
            //Set up the Listener to refresh the details area (After user hits submit button on the right side)
//            ObjectProperty<EntityFacade> semanticProperty = genEditingViewModel.getProperty(SEMANTIC);
//            if (semanticProperty.isNull().get()) {
//                // If the window is in creation mode ignore the refresh event
//                return;
//            }
//            if (genEditingViewModel.getPropertyValue(MODE).equals(EDIT)) {
//                observableSemanticSnapshot = observableSemantic.getSnapshot(getViewProperties().calculator());
//                // populate the semantic and its observable fields once saved
//                semanticEntityVersionLatest = retrieveCommittedLatestVersion(observableSemantic.getSnapshot(getViewProperties().calculator()));
//            }
            // TODO update identicon and identifier fields.

            SemanticEntity<SemanticEntityVersion> semantic = evt.getSemantic();

            if (evt.getEventType() == GenPurposeEvent.PUBLISH) {
//                if (genEditingViewModel.getPropertyValue(MODE).equals(CREATE)) {
//                    // get the latest value for the semantic created.
//                    observableSemantic = ObservableEntityHandle.getSemanticOrThrow(finalSemantic);
//                    // populate the semantic and its observable fields once saved
//                    semanticEntityVersionLatest = retrieveCommittedLatestVersion(observableSemantic.getSnapshot(getViewProperties().calculator()));
//                    // clear out the temporary placeholders
//                    semanticDetailsVBox.getChildren().clear();
//                    nodes.clear();
//                    // set up the real observables now that the semantic has been created
//                    populateSemanticDetails();
//                    // change the mode from CREATE to EDIT
//                    genEditingViewModel.setPropertyValue(MODE, EDIT);
//                    // Update STAMP control and STAMP form
//                    StampFormViewModelBase stampFormViewModelBase = propertiesController.getStampFormViewModel();
//                    stampFormViewModelBase.update(semanticEntityVersionLatest.get().entity(),
//                            genEditingViewModel.getPropertyValue(WINDOW_TOPIC), genEditingViewModel.getViewProperties());
//                    updateUIStamp(stampFormViewModelBase);
//                }

                // Commit transaction, finalizing all impending changes
                composer.commit();

                composer = null;
                initializeComposer();

                // In create mode that commit also finalized the window's lazily created reference
                // concept (see createUncommitedReferenceConcept) — the window is now editing a
                // real component, so refresh the banner/identifier/STAMP from the committed entity.
                if (genPurposeViewModel.getMode() == FormMode.CREATE) {
                    genPurposeViewModel.setMode(FormMode.EDIT);
                    updateView();
                }

                reloadSemanticViews(semantic);
            }

//            semanticEntityVersionLatest = retrieveCommittedLatestVersion(observableSemanticSnapshot);
//            //Set and Update STAMP values
//            semanticEntityVersionLatest.ifPresent(semanticEntityVersion -> {
//                updateUIStamp(semanticEntityVersion.stamp().lastVersion());
//            });
        };
//        subscriberList.add(refreshSubscriber);
        EvtBusFactory.getDefaultEvtBus().subscribe(genPurposeViewModel.getPropertyValue(ViewModelKey.WINDOW_TOPIC),
                GenPurposeEvent.class, refreshSubscriber);

        // Setup window support with explicit draggable nodes
        addDraggableNodes(detailsOuterBorderPane, tabHeader, windowControlToolbar);

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
     * @return The uncommited Semantic
     */
    private SemanticEntity<SemanticEntityVersion> createUncommitedSemantic(EntityFacade referenceComponent, PatternFacade pattern) {
        ObservableEntity observableReferenceComponent = ObservableEntityHandle.get(referenceComponent.nid()).expectEntity();
        ObservablePattern observablePattern = ObservableEntityHandle.get(pattern.nid()).expectPattern();

        initializeComposer();

        ObservableComposer.EntityComposer<ObservableSemanticVersion.Editable, ObservableSemantic> semanticEditor = composer.composeSemantic(PublicIds.newRandom(), observableReferenceComponent, observablePattern);

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
     * Creates the window's reference component as a new, uncommitted concept — used in create mode,
     * where the window was opened without one. The concept joins the composer's current transaction,
     * so it gets committed together with the semantic whose creation triggered it.
     *
     * @return the new uncommitted concept, already set as the window's reference component
     */
    private EntityFacade createUncommitedReferenceConcept() {
        initializeComposer();

        ObservableComposer.EntityComposer<ObservableConceptVersion.Editable, ObservableConcept> conceptComposer =
                composer.composeConcept(PublicIds.newRandom());

        conceptComposer.save(); // Save to create an uncommitted version

        EntityFacade newConcept = conceptComposer.getEntity();
        genPurposeViewModel.setPropertyValue(ViewModelKey.REF_COMPONENT, newConcept);
        return newConcept;
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
        windowTitleLabel.setText(lastDirName.substring(0, 1).toUpperCase() + lastDirName.substring(1));

        editorWindowModel = EditorWindowManager.loadWindowModel(editorWindowPreferences, viewCalculator, windowTitle);

        // The standard Concept window gets the classic concept window's blue chrome (see
        // .concept-window-theme in kview.css) and its own set of properties tabs. User-created
        // Semantics Windows and the other standard windows keep the default grey chrome and tabs.
        if (editorWindowModel.getWindowType() == EditorWindowType.STANDARD_CONCEPT) {
            detailsOuterBorderPane.getStyleClass().add("concept-window-theme");
            propertiesController.getPropertiesTabs().getTabs().setAll(
                    Tab.ADD_EDIT, Tab.HIERARCHY, Tab.HISTORY, Tab.COMMENTS);
        }

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

        // When this section's resolved reference component changes, cascade to any section that anchors
        // on it (i.e. whose reference pattern is displayed in this section), so downstream sections in a
        // reference-component chain re-resolve and re-populate (see komet-desktop #3).
        titledPane.selectedReferenceComponentProperty().subscribe(() -> refreshSectionsAnchoredOn(sectionModel));

        return titledPane;
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

        // In create mode the standard Concept window has no reference component yet — the new
        // concept is created lazily when the user authors the first semantic (see onCreateSemantic),
        // so the popup still opens, offering only "Create Semantic".
        boolean canCreateReferenceComponent = sectionModel.getReferenceComponent() == null
                && genPurposeViewModel.getMode() == FormMode.CREATE
                && editorWindowModel.getWindowType() == EditorWindowType.STANDARD_CONCEPT;

        if (refComponent == null && !canCreateReferenceComponent) {
            // No reference concept to edit against — nothing to populate.
            return;
        }

        if (refComponent != null) {
            // Populate the Popup
            EntityService.get().forEachSemanticForComponentOfPattern(refComponent.nid(),
                    sectionModel.getPatterns().getFirst().getNid(), (semantic) -> {
                        KometLabel semanticLabel = new KometLabel(semantic, viewProperties);
                        semanticLabel.setShowTooltip(true);

                        semanticLabel.setOnMouseClicked(_ -> {
                            initializeComposer();
                            showEditSemanticFieldsPanel(actionEvent, semantic);
                            popup.hide();
                        });

                        semanticLabel.hoverProperty().subscribe(() -> {
                            PatternSemanticsPresenter patternSemanticsPresenter = semanticEntityToPatternSemanticsPresenter.get(semantic);

                            if (semanticLabel.isHover()) {
                                patternSemanticsPresenter.setPreviewingSemantic(semantic);
                            } else {
                                patternSemanticsPresenter.setPreviewingSemantic(null);
                            }
                        });

                        popup.getItems().add(semanticLabel);
                    });
        }

        popup.setOnCreateSemanticAction(() -> {
            initializeComposer();
            onCreateSemantic(actionEvent, sectionModel, refComponent);
        });

        // Show Popup
        SectionTitledPane<?> sectionTitledPane = sectionModelToTitledPane.get(sectionModel);
        Point2D screenPoint = sectionTitledPane.localToScreen(
                sectionTitledPane.getWidth(),
                0
        );
        popup.show(sectionTitledPane, screenPoint.getX(), screenPoint.getY());
    }

    private void onCreateSemantic(ActionEvent actionEvent, EditorSectionModel sectionModelOfPattern, EntityFacade refComponent) {
        // Lazy reference-component creation: in create mode the window has no component yet — the
        // first semantic the user authors brings the new concept into existence with it. Both join
        // the composer's transaction, so submitting the semantic commits them together.
        if (refComponent == null) {
            refComponent = createUncommitedReferenceConcept();
        }

        EditorPatternModel editorPatternModel = sectionModelOfPattern.getPatterns().getFirst();
        PatternFacade patternFacade = PatternFacade.make(editorPatternModel.getNid());

        // Create uncommited Semantic
        SemanticEntity<SemanticEntityVersion> uncommitedSemantic = createUncommitedSemantic(refComponent, patternFacade);

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
        showEditSemanticFieldsPanel(actionEvent, uncommitedSemantic);
    }

    private void showEditSemanticFieldsPanel(Event event, SemanticEntity<SemanticEntityVersion> semanticEntity) {
        // Notify bump out (right side) to display edit fields in Semantic Editing mode
        EvtBusFactory.getDefaultEvtBus()
                .publish(genPurposeViewModel.getPropertyValue(ViewModelKey.WINDOW_TOPIC),
                        new KLPropertyPanelEvent(event.getSource(),
                                SHOW_EDIT_SEMANTIC_FIELDS, semanticEntity));
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

//        VBox patternMainContainer = createPatternContainer(editorPatternModel);
//
//        // Pattern title
//        Label patternTitle = new Label(editorPatternModel.getTitle());
//        patternTitle.getStyleClass().add("gen-purpose-pattern-title");
//        patternTitle.visibleProperty().bind(editorPatternModel.titleVisibleProperty());
//        patternTitle.managedProperty().bind(editorPatternModel.titleVisibleProperty());
//        patternMainContainer.getChildren().add(patternTitle);

        // Pattern fields
        PatternSemanticsPresenter patternSemanticsPresenter = addSemanticViews(editorPatternModel);

        editorPatternModelToPatternPresenter.put(editorPatternModel, patternSemanticsPresenter);

        Node view = patternSemanticsPresenter.getView();

        editorPatternModel.rowIndexProperty().subscribe(newRowIndex -> {
            GridPane.setRowIndex(view, newRowIndex.intValue());
        });

        editorPatternModel.columnIndexProperty().subscribe(newColumnIndex -> {
            GridPane.setColumnIndex(view, newColumnIndex.intValue());
        });

        editorPatternModel.columnSpanProperty().subscribe(newColumnSpan -> {
            GridPane.setColumnSpan(view, newColumnSpan.intValue());
        });

        // Always expand to fill height of the GridPane
        GridPane.setVgrow(view, Priority.ALWAYS);

        titledPane.getItems().add(view);
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

        // Start adding Semantics
        EntityService.get().forEachSemanticForComponentOfPattern(referenceComponent.nid(), patternEntity.nid(),
                (semantic) -> {
                    patternSemanticsPresenter.addNewSemantic(semantic);
                    semanticEntityToPatternSemanticsPresenter.put(semantic, patternSemanticsPresenter);
                });
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
    private void forEachSection(Consumer<EditorSectionModel> action) {
        if (editorWindowModel == null) {
            return;
        }
        action.accept(editorWindowModel.getMainSection());
        editorWindowModel.getAdditionalSections().forEach(action);
    }

    /**
     * Re-resolves every section whose reference pattern lives in {@code changedSection}, so a change to
     * that section's reference component propagates down the reference-component chain. Each refreshed
     * section's own selection change drives the next hop, so the cascade naturally reaches arbitrary
     * depth (see komet-desktop #3).
     */
    private void refreshSectionsAnchoredOn(EditorSectionModel changedSection) {
        forEachSection(section -> {
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
            }
        }
    }
}