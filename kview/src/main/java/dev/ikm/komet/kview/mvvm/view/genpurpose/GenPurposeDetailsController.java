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

import static dev.ikm.komet.kview.controls.FilterOptionsPopup.FILTER_TYPE.CHAPTER_WINDOW;
import static dev.ikm.komet.kview.events.genpurpose.KLPropertyPanelEvent.CLOSE_PANEL;
import static dev.ikm.komet.kview.events.genpurpose.KLPropertyPanelEvent.NO_SELECTION_MADE_PANEL;
import static dev.ikm.komet.kview.events.genpurpose.KLPropertyPanelEvent.OPEN_PANEL;
import static dev.ikm.komet.kview.events.genpurpose.KLPropertyPanelEvent.SHOW_EDIT_SEMANTIC_FIELDS;
import static dev.ikm.komet.kview.fxutils.SlideOutTrayHelper.isClosed;
import static dev.ikm.komet.kview.fxutils.SlideOutTrayHelper.isOpen;
import static dev.ikm.komet.kview.fxutils.SlideOutTrayHelper.slideIn;
import static dev.ikm.komet.kview.fxutils.SlideOutTrayHelper.slideOut;
import static dev.ikm.komet.kview.fxutils.ViewportHelper.clipChildren;
import static dev.ikm.komet.kview.fxutils.window.DraggableSupport.addDraggableNodes;
import static dev.ikm.komet.kview.fxutils.window.DraggableSupport.removeDraggableNodes;
import static dev.ikm.komet.kview.klfields.KlFieldHelper.retrieveCommittedLatestVersion;
import static dev.ikm.komet.kview.mvvm.view.common.ChapterWindowHelper.setupViewCoordinateOptionsPopup;
import static dev.ikm.komet.kview.mvvm.viewmodel.FormViewModel.CURRENT_JOURNAL_WINDOW_TOPIC;
import static dev.ikm.komet.kview.mvvm.viewmodel.GenEditingViewModel.REF_COMPONENT;
import static dev.ikm.komet.kview.mvvm.viewmodel.GenEditingViewModel.WINDOW_TOPIC;

import dev.ikm.komet.framework.Identicon;
import dev.ikm.komet.framework.controls.TimeUtils;
import dev.ikm.komet.framework.observable.ObservableComposer;
import dev.ikm.komet.framework.observable.ObservableEntity;
import dev.ikm.komet.framework.observable.ObservableEntitySnapshot;
import dev.ikm.komet.framework.observable.ObservableField;
import dev.ikm.komet.framework.observable.ObservableSemanticSnapshot;
import dev.ikm.komet.framework.view.ViewProperties;
import dev.ikm.komet.kview.common.ViewCalculatorUtils;
import dev.ikm.komet.kview.controls.FilterOptionsPopup;
import dev.ikm.komet.kview.controls.KLReadOnlyBaseControl;
import dev.ikm.komet.kview.controls.KometLabel;
import dev.ikm.komet.kview.controls.PublicIDListControl;
import dev.ikm.komet.kview.controls.SectionTitledPane;
import dev.ikm.komet.kview.controls.StampViewControl;
import dev.ikm.komet.kview.controls.TitledMenuPopup;
import dev.ikm.komet.kview.events.ClosePropertiesPanelEvent;
import dev.ikm.komet.kview.events.genpurpose.KLPropertyPanelEvent;
import dev.ikm.komet.kview.klfields.KlFieldHelper;
import dev.ikm.komet.kview.mvvm.view.genpurpose.control.SectionSemanticsComboBoxCell;
import dev.ikm.komet.kview.mvvm.view.genpurpose.control.SemanticViewControl;
import dev.ikm.komet.kview.mvvm.view.journal.VerticallyFilledPane;
import dev.ikm.komet.kview.mvvm.viewmodel.GenPurposeViewModel;
import dev.ikm.komet.layout.editor.EditorWindowManager;
import dev.ikm.komet.layout.editor.model.EditorFieldModel;
import dev.ikm.komet.layout.editor.model.EditorPatternModel;
import dev.ikm.komet.layout.editor.model.EditorSectionModel;
import dev.ikm.komet.layout.editor.model.EditorWindowModel;
import dev.ikm.komet.preferences.KometPreferences;
import dev.ikm.tinkar.coordinate.stamp.calculator.Latest;
import dev.ikm.tinkar.coordinate.view.calculator.ViewCalculator;
import dev.ikm.tinkar.entity.EntityHandle;
import dev.ikm.tinkar.entity.EntityService;
import dev.ikm.tinkar.entity.EntityVersion;
import dev.ikm.tinkar.entity.Field;
import dev.ikm.tinkar.entity.FieldRecord;
import dev.ikm.tinkar.entity.PatternEntity;
import dev.ikm.tinkar.entity.SemanticEntity;
import dev.ikm.tinkar.entity.SemanticEntityVersion;
import dev.ikm.tinkar.entity.StampEntity;
import dev.ikm.tinkar.events.EvtBusFactory;
import dev.ikm.tinkar.events.EvtType;
import dev.ikm.tinkar.events.Subscriber;
import dev.ikm.tinkar.terms.ConceptFacade;
import dev.ikm.tinkar.terms.EntityFacade;
import dev.ikm.tinkar.terms.State;
import javafx.beans.binding.Bindings;
import javafx.collections.ListChangeListener;
import javafx.event.ActionEvent;
import javafx.event.Event;
import javafx.event.EventHandler;
import javafx.fxml.FXML;
import javafx.geometry.Bounds;
import javafx.geometry.Point2D;
import javafx.scene.Node;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.MenuButton;
import javafx.scene.control.Separator;
import javafx.scene.control.TitledPane;
import javafx.scene.control.ToggleButton;
import javafx.scene.control.Tooltip;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.ColumnConstraints;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Pane;
import javafx.scene.layout.Priority;
import javafx.scene.layout.Region;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;
import javafx.scene.text.Text;
import org.carlfx.cognitive.loader.Config;
import org.carlfx.cognitive.loader.FXMLMvvmLoader;
import org.carlfx.cognitive.loader.InjectViewModel;
import org.carlfx.cognitive.loader.JFXNode;
import org.carlfx.cognitive.loader.NamedVm;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.net.URL;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicReference;
import java.util.function.Consumer;
import java.util.function.Function;

public class GenPurposeDetailsController {

    private static final Logger LOG = LoggerFactory.getLogger(GenPurposeDetailsController.class);

    private final HashMap<EditorSectionModel, GridPane> sectionModelToTitledPaneGridPane = new HashMap<>();
    private final HashMap<EditorSectionModel, SectionTitledPane<EntityFacade>> sectionModelToTitledPane = new HashMap<>();
    private final HashMap<SemanticEntity<SemanticEntityVersion>, SemanticViewControl> semanticEntityToSemanticView = new HashMap<>();

    private final Tooltip publishTooltip = new Tooltip();
    private final List<KLReadOnlyBaseControl> controls = new ArrayList<>();

    private SemanticViewControl previousSemanticViewInEditMode;

    @FXML
    StampViewControl stampViewControl;
    @FXML
    private VBox mainContent;
    @FXML
    private BorderPane detailsOuterBorderPane;
    @FXML
    private ToggleButton propertiesToggleButton;
    @FXML
    private MenuButton coordinatesMenuButton;
    /**
     * popup for the filter coordinates menu, used with coordinatesMenuButton. An instance of FilterOptionsPopup.
     */
    private FilterOptionsPopup filterOptionsPopup;
    /**
     * Used slide out the properties view
     */
    @FXML
    private VerticallyFilledPane propertiesSlideoutTrayPane;
    @FXML
    private ImageView identiconImageView;
    @FXML
    private Label conceptTitleText;
    @FXML
    private Tooltip conceptNameTooltip;
    @FXML
    private PublicIDListControl identifierControl;
    @FXML
    private Button savePatternButton;
    @FXML
    private StackPane publishStackPane;
    @FXML
    private HBox tabHeader;
    @FXML
    private HBox conceptHeaderControlToolBarHbox;
    @FXML
    private Text windowTitleLabel;
    private BorderPane propertiesBorderPane;
    private GenPurposePropertiesController propertiesController;
    private EditorWindowModel editorWindowModel;
    private boolean isUpdatingStampSelection = false;
    private ViewProperties viewProperties;
    @InjectViewModel
    private GenPurposeViewModel genPurposeViewModel;
    private Consumer<GenPurposeDetailsController> onCloseConceptWindow;

    private Subscriber<ClosePropertiesPanelEvent> closePropertiesPanelEventSubscriber;

    @FXML
    private void initialize() {

        // Set up the filter options popup for the coordinates menu button.
        filterOptionsPopup = setupViewCoordinateOptionsPopup(
                genPurposeViewModel.getViewProperties(),
                CHAPTER_WINDOW,
                detailsOuterBorderPane,
                coordinatesMenuButton,
                this::updateView
        );

        stampViewControl.selectedProperty().subscribe(this::onStampSelectionChanged);

        // Bind the Publish button's disable property to the ViewModel
        publishTooltip.textProperty().bind(Bindings.when(savePatternButton.disableProperty())
                .then("Publish: Disabled")
                .otherwise("Submit"));

        // Setup Properties Bump out view
        setupProperties();


        // Assign the tooltip to the StackPane (container of Publish button)
        setupTooltipForDisabledButton(savePatternButton);

        // Setup window support with explicit draggable nodes
        addDraggableNodes(detailsOuterBorderPane, tabHeader, conceptHeaderControlToolBarHbox);

        // if the user clicks the Close Properties Button from the Edit Descriptions panel
        // in that state, the properties bump out will be slid out, therefore firing will perform a slide in
        closePropertiesPanelEventSubscriber = evt -> propertiesToggleButton.fire();
        EvtBusFactory.getDefaultEvtBus().subscribe(genPurposeViewModel.getPropertyValue(WINDOW_TOPIC), ClosePropertiesPanelEvent.class, closePropertiesPanelEventSubscriber);
    }

    private void openPropertiesPanel() {
        LOG.info("propBumpOutListener - Opening Properties bumpout toggle = " + propertiesToggleButton.isSelected());

        propertiesToggleButton.setSelected(true);
        if (isClosed(propertiesSlideoutTrayPane)) {
            slideOut(propertiesSlideoutTrayPane, detailsOuterBorderPane);
        }

        updateDraggableNodesForPropertiesPanel(true);
    }

    /**
     * User is clicking on the Toggle switch to open or close Properties bump out.
     *
     * @param event Button click event.
     */
    @FXML
    private void openPropertiesPanel(ActionEvent event) {
        ToggleButton propertyToggle = (ToggleButton) event.getSource();
        EvtType<KLPropertyPanelEvent> eventEvtType = propertyToggle.isSelected() ? KLPropertyPanelEvent.OPEN_PANEL : KLPropertyPanelEvent.CLOSE_PANEL;

        updateDraggableNodesForPropertiesPanel(propertyToggle.isSelected());

//        isUpdatingStampSelection = true;
//        stampViewControl.setSelected(propertyToggle.isSelected());
//        isUpdatingStampSelection = false;

        EvtBusFactory.getDefaultEvtBus().publish(genPurposeViewModel.getPropertyValue(WINDOW_TOPIC), new KLPropertyPanelEvent(propertyToggle, eventEvtType));
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

    private void onStampSelectionChanged() {
        if (isUpdatingStampSelection) {
            return;
        }

        if (stampViewControl.isSelected()) {
            if (!propertiesToggleButton.isSelected()) {
                propertiesToggleButton.fire();
            }
//            if (CREATE.equals(patternViewModel.getPropertyValue(MODE))) {
//                EvtBusFactory.getDefaultEvtBus().publish(patternViewModel.getPropertyValue(PATTERN_TOPIC), new StampEvent(stampViewControl, StampEvent.CREATE_STAMP));
//            } else {
//                EvtBusFactory.getDefaultEvtBus().publish(patternViewModel.getPropertyValue(PATTERN_TOPIC), new StampEvent(stampViewControl, StampEvent.ADD_STAMP));
//            }
//        } else {
//            EvtBusFactory.getDefaultEvtBus().publish(patternViewModel.getPropertyValue(PATTERN_TOPIC), new ClosePropertiesPanelEvent(stampViewControl, CLOSE_PROPERTIES));
        }
    }

    /// Show the public ID
    private void updateDisplayIdentifier(EntityFacade refComponent) {
        ViewCalculator viewCalculator = getViewProperties().calculator();
        identifierControl.updatePublicIdList(viewCalculator, refComponent);
    }

    private void updateIdenticon(EntityFacade refComponent) {
        Image identicon = Identicon.generateIdenticonImage(refComponent.publicId());
        identiconImageView.setImage(identicon);
    }

    private void updateStampControl(EntityFacade refConcept) {
        ObservableEntity observableEntity = ObservableEntity.get(refConcept.nid());
        ObservableEntitySnapshot observableEntitySnapshot = observableEntity.getSnapshot(viewProperties.calculator());
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

    private void updateWindowTitle(EntityFacade refConcept) {
        String conceptNameStr = getViewProperties().calculator().languageCalculator().getPreferredDescriptionTextWithFallbackOrNid(refConcept.nid());
        conceptTitleText.setText(conceptNameStr);
        conceptNameTooltip.setText(conceptNameStr);
    }

    /**
     * Creates the filter coordinates menu using the view calculator.
     * TODO Note that this is not a working menu, this is the first step to have propagating, inherited, filter coordinates
     * in the window/node hierarchy.
     */
    public void setupFilterCoordinatesMenu() {
//        this.viewMenuModel = new ViewMenuModel(patternViewModel.getViewProperties(), coordinatesMenuButton, "PatternDetailsController");
    }

    private void setupProperties() {
        URL genpurposePropertiesFXML = GenPurposeDetailsController.class.getResource("genpurpose-properties.fxml");
        Config config = new Config(genpurposePropertiesFXML)
                .addNamedViewModel(new NamedVm("genPurposeViewModel", genPurposeViewModel));

        JFXNode<BorderPane, GenPurposePropertiesController> propsFXMLLoader = FXMLMvvmLoader.make(config);
        this.propertiesBorderPane = propsFXMLLoader.node();
        this.propertiesController = propsFXMLLoader.controller();
        attachPropertiesViewSlideoutTray(this.propertiesBorderPane);

        // open the panel, allow the state machine to determine which panel to show
        // listen for open and close events
        Subscriber<KLPropertyPanelEvent> propertiesEventSubscriber = (evt) -> {
            if (evt.getEventType() == CLOSE_PANEL) {
                LOG.info("propBumpOutListener - Close Properties bumpout toggle = " + propertiesToggleButton.isSelected());
                propertiesToggleButton.setSelected(false);
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
        EvtBusFactory.getDefaultEvtBus().subscribe(genPurposeViewModel.getPropertyValue(WINDOW_TOPIC), KLPropertyPanelEvent.class, propertiesEventSubscriber);
    }

    private void onStampConfirmedOrSubmitted(boolean isSubmittedOrConfirmed) {
        if (!isSubmittedOrConfirmed) {
            return;
        }

//        updateStampControlFromViewModel();

//        if (patternViewModel.getPropertyValue(MODE).equals(EDIT)) {
//            patternViewModel.setPropertyValue(PUBLISH_PENDING, true);
//        }

        stampViewControl.setDisable(true);
    }

    public ViewProperties getViewProperties() {
        return viewProperties;
    }

    public void setOnCloseConceptWindow(Consumer<GenPurposeDetailsController> onClose) {
        this.onCloseConceptWindow = onClose;
    }

    @FXML
    void closeConceptWindow(ActionEvent event) {
        LOG.info("Cleanup occurring: Closing Window with pattern: " + conceptTitleText.getText());

        if (this.onCloseConceptWindow != null) {
            onCloseConceptWindow.accept(this);
        }
    }

    @FXML
    private void saveConceptKL(ActionEvent actionEvent) {
    }

    private void setupTooltipForDisabledButton(Button button) {

        button.disabledProperty().subscribe(isNowDisabled -> {
            if (isNowDisabled) {
                Tooltip.uninstall(button, publishTooltip);

                // Create unique handlers for each button-tooltip pair
                EventHandler<MouseEvent> showHandler = showTooltipOnDisabledButton(button, publishTooltip);
                EventHandler<MouseEvent> hideHandler = hideTooltipHandler(publishTooltip);

                // Store handlers on the button's properties for later removal
                button.getProperties().put("showHandler", showHandler);
                button.getProperties().put("hideHandler", hideHandler);

                publishStackPane.addEventFilter(MouseEvent.MOUSE_MOVED, showHandler);
                publishStackPane.addEventFilter(MouseEvent.MOUSE_EXITED, hideHandler);
            } else {
                Tooltip.install(button, publishTooltip);
                publishTooltip.hide();

                // Remove handlers if present
                EventHandler<MouseEvent> showHandler = (EventHandler<MouseEvent>) button.getProperties().get("showHandler");
                EventHandler<MouseEvent> hideHandler = (EventHandler<MouseEvent>) button.getProperties().get("hideHandler");
                if (showHandler != null) publishStackPane.removeEventFilter(MouseEvent.MOUSE_MOVED, showHandler);
                if (hideHandler != null) publishStackPane.removeEventFilter(MouseEvent.MOUSE_EXITED, hideHandler);
            }
        });
    }

    private EventHandler<MouseEvent> showTooltipOnDisabledButton(Button button, Tooltip tooltip) {
        return event -> {
            if (button.isDisabled()) {
                Bounds bounds = button.localToScreen(button.getBoundsInLocal());
                double mouseX = event.getScreenX();
                double mouseY = event.getScreenY();
                if (bounds.contains(mouseX, mouseY)) {
                    if (!tooltip.isShowing()) {
                        tooltip.show(button, mouseX, mouseY + 10);
                    }
                } else {
                    tooltip.hide();
                }
            } else {
                tooltip.hide();
            }
        };
    }

    private EventHandler<MouseEvent> hideTooltipHandler(Tooltip tooltip) {
        return event -> tooltip.hide();
    }

    /**
     * Updates draggable behavior for the properties panel based on its open/closed state.
     * <p>
     * When opened, adds the properties tabs pane as a draggable node. When closed,
     * safely removes the draggable behavior to prevent memory leaks.
     *
     * @param isOpen {@code true} to add draggable nodes, {@code false} to remove them
     */
    private void updateDraggableNodesForPropertiesPanel(boolean isOpen) {
        if (propertiesController != null && propertiesController.getPropertiesTabsPane() != null) {
            if (isOpen) {
                addDraggableNodes(detailsOuterBorderPane, propertiesController.getPropertiesTabsPane());
                LOG.debug("Added properties nodes as draggable");
            } else {
                removeDraggableNodes(detailsOuterBorderPane, propertiesController.getPropertiesTabsPane());
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

        EditorSectionModel mainSection = editorWindowModel.getMainSection();

        // Main TitledPane
        TitledPane mainTitledPane = createTitledPane(mainSection);
        addPatternViews(mainSection, mainSection.getPatterns());
        mainContent.getChildren().add(mainTitledPane);

        mainSection.getPatterns().addListener((ListChangeListener<? super EditorPatternModel>) change -> onSectionPatternsChanged(mainSection, change));

        // Additional Sections
        editorWindowModel.getAdditionalSections().forEach(section -> {
            TitledPane titledPane = createTitledPane(section);
            addPatternViews(section, section.getPatterns());
            mainContent.getChildren().add(titledPane);
        });

        // TODO: will sections need to be refreshed on coordinate changes?
        editorWindowModel.getAdditionalSections().addListener(this::onAdditionalSectionsChanged);

        // Initial view update
        updateView();
    }

    /**
     * Called to update the view when coordinate changes occur.
     */
    private void updateView() {
        LOG.info("Update view called - implement coordinate changes here.");
        EntityFacade refConcept = (EntityFacade) genPurposeViewModel.getProperty(REF_COMPONENT).getValue();
        if (refConcept != null) {
            updateDisplayIdentifier(refConcept);
            updateIdenticon(refConcept);
            updateWindowTitle(refConcept);
            updateStampControl(refConcept);
        } else {
            LOG.warn("REF_COMPONENT is null, cannot update view.");
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

        GridPane titledPaneGridPane = new GridPane();

        sectionModel.numberColumnsProperty().subscribe(newNumberColumns -> {
            List<ColumnConstraints> columns = new ArrayList<>();
            for (int i = 0; i < newNumberColumns.intValue(); ++i) {
                ColumnConstraints columnConstraints = new ColumnConstraints();
                columnConstraints.setHgrow(Priority.ALWAYS);
                columnConstraints.setPercentWidth(100 / ((double) newNumberColumns.intValue()));
                columns.add(columnConstraints);
            }
            titledPaneGridPane.getColumnConstraints().setAll(columns);
        });

        // Section Semantics ComboBox
        if (sectionModel.getReferenceComponent() != null) {
            titledPane.getReferenceComponents().addAll(getSemanticsOfPattern(sectionModel.getReferenceComponent()));
        }
        titledPane.setReferenceComponentCellFactory(_ -> createSectionSemanticsComboBoxCell(viewProperties));
        titledPane.setReferenceComponentButtonCellFactory(new SectionSemanticsComboBoxCell(viewProperties));

        // Content
        titledPane.setContent(titledPaneGridPane);

        titledPane.setOnEditAction(actionEvent -> onEditAction(actionEvent, sectionModel));

        sectionModelToTitledPaneGridPane.put(sectionModel, titledPaneGridPane);

        sectionModelToTitledPane.put(sectionModel, titledPane);

        return titledPane;
    }

    private SectionSemanticsComboBoxCell createSectionSemanticsComboBoxCell(ViewProperties viewProperties) {
        SectionSemanticsComboBoxCell sectionSemanticsComboBoxCell = new SectionSemanticsComboBoxCell(viewProperties);
        sectionSemanticsComboBoxCell.hoverProperty().subscribe(() -> {
            SemanticEntity<SemanticEntityVersion> semanticEntity = (SemanticEntity<SemanticEntityVersion>) sectionSemanticsComboBoxCell.getItem();
            SemanticViewControl semanticViewControl = semanticEntityToSemanticView.get(semanticEntity);
            if (semanticViewControl != null) {
                semanticViewControl.setPreviewMode(sectionSemanticsComboBoxCell.isHover());
            }
        });
        return sectionSemanticsComboBoxCell;
    }

    private void onEditAction(ActionEvent actionEvent, EditorSectionModel sectionModel) {
        TitledMenuPopup popup = new TitledMenuPopup();
        popup.setTitle("EDIT SEMANTIC");

        // Get the entity (Semantic) to edit
        EntityFacade refComponent;

        if (sectionModel.getReferenceComponent() == null) {
            refComponent = genPurposeViewModel.getPropertyValue(GenPurposeViewModel.REF_COMPONENT);
        } else {
            SectionTitledPane<EntityFacade> sectionTitledPane = sectionModelToTitledPane.get(sectionModel);
            refComponent = sectionTitledPane.getSelectedReferenceComponent();
        }

        AtomicInteger numberSemantics = new AtomicInteger();
        AtomicReference<SemanticEntity<SemanticEntityVersion>> lastSemantic = new AtomicReference<>();

        EntityService.get().forEachSemanticForComponentOfPattern(refComponent.nid(),
                sectionModel.getPatterns().getFirst().getNid(), (semantic) -> {
                    KometLabel semanticLabel = new KometLabel(semantic, viewProperties);
                    semanticLabel.setShowTooltip(true);
                    semanticLabel.getStyleClass().add("semantic-label");

                    semanticLabel.setOnMouseClicked(_ -> {
                        showEditSemanticFieldsPanel(actionEvent, semantic);
                        popup.hide();
                    });

                    semanticLabel.hoverProperty().subscribe(() -> {
                        semanticEntityToSemanticView.get(semantic).setPreviewMode(semanticLabel.isHover());
                    });

                    popup.getItems().add(semanticLabel);

                    numberSemantics.incrementAndGet();
                    lastSemantic.set(semantic);
                });

        if (numberSemantics.get() > 1) {
            SectionTitledPane<?> sectionTitledPane = sectionModelToTitledPane.get(sectionModel);

            Point2D popupPosition = sectionTitledPane.getLocalToSceneTransform().transform(sectionTitledPane.getLayoutX() + sectionTitledPane.getWidth(),
                    sectionTitledPane.getBoundsInLocal().getMinY());
            popup.show(sectionTitledPane, popupPosition.getX(), popupPosition.getY());
        } else {
            showEditSemanticFieldsPanel(actionEvent, lastSemantic.get());
        }
    }

    private void showEditSemanticFieldsPanel(Event event, SemanticEntity<SemanticEntityVersion> semanticEntity) {
        // notify bump out to display edit fields in bump out area.
        EvtBusFactory.getDefaultEvtBus()
                .publish(genPurposeViewModel.getPropertyValue(WINDOW_TOPIC),
                        new KLPropertyPanelEvent(event.getSource(),
                                SHOW_EDIT_SEMANTIC_FIELDS, semanticEntity));
        // open properties bump out.
        EvtBusFactory.getDefaultEvtBus().publish(genPurposeViewModel.getPropertyValue(WINDOW_TOPIC), new KLPropertyPanelEvent(event.getSource(), OPEN_PANEL));

        // Turn on Edit mode for the Semantic
        if (previousSemanticViewInEditMode != null) {
            previousSemanticViewInEditMode.setEditMode(false);
        }
        SemanticViewControl semanticViewControl = semanticEntityToSemanticView.get(semanticEntity);
        semanticViewControl.setEditMode(true);
        previousSemanticViewInEditMode = semanticViewControl;
    }

    private void addPatternViews(EditorSectionModel sectionModel, List<? extends EditorPatternModel> patternModels) {
        GridPane content = sectionModelToTitledPaneGridPane.get(sectionModel);
        for (EditorPatternModel editorPatternModel : patternModels) {
            addPatternView(editorPatternModel, content, sectionModel);
        }
    }

    private void addPatternView(EditorPatternModel editorPatternModel, GridPane content, EditorSectionModel parentSection) {
        VBox patternMainContainer = new VBox();
        patternMainContainer.getStyleClass().add("pattern-container");

        // Pattern title
        Label patternTitle = new Label(editorPatternModel.getTitle());
        patternTitle.getStyleClass().add("gen-purpose-pattern-title");
        patternTitle.visibleProperty().bind(editorPatternModel.titleVisibleProperty());
        patternTitle.managedProperty().bind(editorPatternModel.titleVisibleProperty());
        patternMainContainer.getChildren().add(patternTitle);

        // Pattern Fields Container
        VBox semanticsContainer = new VBox();
        semanticsContainer.getStyleClass().add("semantics-container");

        // Pattern fields
        createSemanticViews(editorPatternModel, semanticsContainer, parentSection);

        editorPatternModel.rowIndexProperty().subscribe(newRowIndex -> {
            GridPane.setRowIndex(patternMainContainer, newRowIndex.intValue());
        });

        editorPatternModel.columnIndexProperty().subscribe(newColumnIndex -> {
            GridPane.setColumnIndex(patternMainContainer, newColumnIndex.intValue());
        });

        editorPatternModel.columnSpanProperty().subscribe(newColumnSpan -> {
            GridPane.setColumnSpan(patternMainContainer, newColumnSpan.intValue());
        });

        patternMainContainer.getChildren().add(semanticsContainer);
        content.getChildren().add(patternMainContainer);
    }

    private List<KLReadOnlyBaseControl> createSemanticViews(EditorPatternModel editorPatternModel, VBox semanticsContainer,
                                                            EditorSectionModel parentSection) {
//        PatternVersionRecord patternVersionRecord = (PatternVersionRecord) getViewProperties().calculator().latest(patternEntity).get();

        List<EntityFacade> refComponents = getReferenceComponentsToUse(parentSection.getReferenceComponent());

        List<KLReadOnlyBaseControl> controlItems = new ArrayList<>();

        doCreateSemanticViews(editorPatternModel, semanticsContainer, refComponents, controlItems);

        SectionTitledPane<EntityFacade> titledPane = sectionModelToTitledPane.get(parentSection);
        titledPane.setSelectedReferenceComponent(refComponents.getFirst());
        titledPane.selectedReferenceComponentProperty().subscribe(() -> {
            semanticsContainer.getChildren().clear();
            doCreateSemanticViews(editorPatternModel, semanticsContainer, List.of(titledPane.getSelectedReferenceComponent()), controlItems);
        });

        controls.addAll(controlItems);
        return controlItems;
    }

    private void doCreateSemanticViews(EditorPatternModel editorPatternModel, VBox semanticsContainer,
                                       List<EntityFacade> refComponents, List<KLReadOnlyBaseControl> controlItems) {

        // Pattern Entity
        int patternNid = editorPatternModel.getNid();
        EntityHandle handle = EntityHandle.get(patternNid);
        PatternEntity patternEntity;
        if (handle.asPattern().isEmpty()) {
            throw new RuntimeException("Expecting a Pattern to be present instead of an empty Optional");
        }
        patternEntity = handle.asPattern().get();

        // Composer
        ObservableComposer composer = createComposer();

        // Start adding Semantics
        AtomicInteger index = new AtomicInteger(0);
        EntityService.get().forEachSemanticForComponentOfPattern(refComponents.getFirst().nid(), patternEntity.nid(),
                (semantic) -> {
                    // add Separator
                    if (index.get() > 0) {
                        Separator separator = new Separator();
                        semanticsContainer.getChildren().add(separator);
                    }

                    SemanticViewControl semanticViewControl = new SemanticViewControl();

                    semanticEntityToSemanticView.put(semantic, semanticViewControl);

                    ObservableEntitySnapshot<?, ?> snap = composer.snapshot(semantic.nid()).get();
                    if (snap instanceof ObservableSemanticSnapshot semanticSnapshot) {
                        for (ObservableField<?> observableField : semanticSnapshot.getLatestFields().get()) {
                            for (EditorFieldModel editorFieldModel : editorPatternModel.getFields()) {
                                if (observableField.field().indexInPattern() == editorFieldModel.getIndex()) {
                                    createFieldView(observableField, editorFieldModel, controlItems, semanticViewControl);
                                }
                            }
                        }
                    }

                    semanticViewControl.numberColumnsProperty().bind(editorPatternModel.numberColumnsProperty());

                    semanticsContainer.getChildren().add(semanticViewControl);

                    index.incrementAndGet();
                });
    }

    private ObservableComposer createComposer() {
        ConceptFacade author = getViewProperties().nodeView().editCoordinate().getAuthorForChanges();
        ConceptFacade module = getViewProperties().nodeView().editCoordinate().getDefaultModule();
        ConceptFacade path = getViewProperties().nodeView().editCoordinate().getDefaultPath();

        ObservableComposer composer = ObservableComposer.create(
                getViewProperties().calculator(),
                State.ACTIVE,
                author,
                module,
                path,
                "Edit Semantic Details"
        );
        return composer;
    }

    private List<EntityFacade> getReferenceComponentsToUse(EditorPatternModel sectionReferenceComponent) {
        List<EntityFacade> refComponents = new ArrayList<>();

        EntityFacade windowRefComponent = genPurposeViewModel.getPropertyValue(REF_COMPONENT);

        if (sectionReferenceComponent != null) {
            EntityService.get().forEachSemanticForComponentOfPattern(windowRefComponent.nid(), sectionReferenceComponent.getNid(),
                    (SemanticEntity<SemanticEntityVersion> semantic) -> {
                        refComponents.add(semantic);
                    }
            );
        } else {
            refComponents.add(windowRefComponent);
        }

        return refComponents;
    }

    private List<EntityFacade> getSemanticsOfPattern(EditorPatternModel editorPatternModel) {
        EntityFacade windowRefComponent = genPurposeViewModel.getPropertyValue(REF_COMPONENT);

        List<EntityFacade> refComponents = new ArrayList<>();

        EntityService.get().forEachSemanticForComponentOfPattern(windowRefComponent.nid(), editorPatternModel.getNid(),
                (SemanticEntity<SemanticEntityVersion> semantic) -> {
                    refComponents.add(semantic);
                }
        );

        return refComponents;
    }

    private void createFieldView(ObservableField<?> observableField, EditorFieldModel fieldModel,
                                 List<KLReadOnlyBaseControl> controlItems, SemanticViewControl semanticViewControl) {
        Field<?> field = observableField.field();

        // Generate node using the underlying ObservableField (read-only view)
        // This was throwing a cast exception, expecting KLReadOnlyBaseControl.
        Node baseControl = KlFieldHelper.createReadOnlyKlField(
                (FieldRecord<?>) field,
                observableField, // Use underlying ObservableField for display
                getViewProperties(),
                null,
                genPurposeViewModel.getPropertyValue(CURRENT_JOURNAL_WINDOW_TOPIC)
        );

        // Eliminated unsafe cast here...
        if (baseControl instanceof KLReadOnlyBaseControl klReadOnlyBaseControl) {
//                                klReadOnlyBaseControl.setOnEditAction(editAction.apply(klReadOnlyBaseControl, index++));
//                                semanticDetailsVBox.getChildren().add(klReadOnlyBaseControl);
            controlItems.add(klReadOnlyBaseControl);
        }

        fieldModel.rowIndexProperty().subscribe(newRowIndex -> {
            GridPane.setRowIndex(baseControl, newRowIndex.intValue());
        });

        fieldModel.columnIndexProperty().subscribe(newColumnIndex -> {
            GridPane.setColumnIndex(baseControl, newColumnIndex.intValue());
        });

        fieldModel.columnSpanProperty().subscribe(newColumnSpan -> {
            GridPane.setColumnSpan(baseControl, newColumnSpan.intValue());
        });

        semanticViewControl.getFields().add((KLReadOnlyBaseControl) baseControl);
    }


    private void onSectionPatternsChanged(EditorSectionModel editorSectionModel, ListChangeListener.Change<? extends EditorPatternModel> change) {
        while (change.next()) {
            if (change.wasAdded()) {
                addPatternViews(editorSectionModel, change.getAddedSubList());
            }
        }
    }

    private void onAdditionalSectionsChanged(ListChangeListener.Change<? extends EditorSectionModel> change) {
        while (change.next()) {
            if (change.wasAdded()) {
                for (EditorSectionModel additionalSectionModel : change.getAddedSubList()) {
                    TitledPane titledPane = createTitledPane(additionalSectionModel);
                    addPatternViews(additionalSectionModel, additionalSectionModel.getPatterns());
                    additionalSectionModel.getPatterns().addListener((ListChangeListener<? super EditorPatternModel>)
                            patternsChange -> onSectionPatternsChanged(additionalSectionModel, patternsChange));

                    mainContent.getChildren().add(titledPane);
                }
            }
        }
    }
}