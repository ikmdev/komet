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

import dev.ikm.komet.framework.Identicon;
import dev.ikm.komet.framework.controls.TimeUtils;
import dev.ikm.komet.framework.observable.ObservableComposer;
import dev.ikm.komet.framework.observable.ObservableEntity;
import dev.ikm.komet.framework.observable.ObservableEntitySnapshot;
import dev.ikm.komet.framework.observable.ObservableField;
import dev.ikm.komet.framework.observable.ObservableSemanticSnapshot;
import dev.ikm.komet.framework.view.ViewMenuModel;
import dev.ikm.komet.framework.view.ViewProperties;
import dev.ikm.komet.kview.common.ViewCalculatorUtils;
import dev.ikm.komet.kview.controls.KLReadOnlyBaseControl;
import dev.ikm.komet.kview.controls.PublicIDListControl;
import dev.ikm.komet.kview.controls.SectionTitledPane;
import dev.ikm.komet.kview.controls.StampViewControl;
import dev.ikm.komet.kview.events.genediting.PropertyPanelEvent;
import dev.ikm.komet.kview.klfields.KlFieldHelper;
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
import dev.ikm.tinkar.entity.PatternVersionRecord;
import dev.ikm.tinkar.entity.StampEntity;
import dev.ikm.tinkar.events.EvtBusFactory;
import dev.ikm.tinkar.terms.ConceptFacade;
import dev.ikm.tinkar.terms.EntityFacade;
import dev.ikm.tinkar.terms.State;
import javafx.beans.binding.Bindings;
import javafx.collections.ListChangeListener;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.fxml.FXML;
import javafx.geometry.Bounds;
import javafx.geometry.NodeOrientation;
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
import java.util.function.Consumer;

import static dev.ikm.komet.kview.events.genediting.PropertyPanelEvent.OPEN_PANEL;
import static dev.ikm.komet.kview.events.genediting.PropertyPanelEvent.SHOW_EDIT_SEMANTIC_FIELDS;
import static dev.ikm.komet.kview.fxutils.SlideOutTrayHelper.isClosed;
import static dev.ikm.komet.kview.fxutils.SlideOutTrayHelper.slideOut;
import static dev.ikm.komet.kview.fxutils.ViewportHelper.clipChildren;
import static dev.ikm.komet.kview.fxutils.window.DraggableSupport.addDraggableNodes;
import static dev.ikm.komet.kview.fxutils.window.DraggableSupport.removeDraggableNodes;
import static dev.ikm.komet.kview.klfields.KlFieldHelper.retrieveCommittedLatestVersion;
import static dev.ikm.komet.kview.mvvm.viewmodel.FormViewModel.CURRENT_JOURNAL_WINDOW_TOPIC;
import static dev.ikm.komet.kview.mvvm.viewmodel.GenEditingViewModel.REF_COMPONENT;
import static dev.ikm.komet.kview.mvvm.viewmodel.GenEditingViewModel.SEMANTIC;
import static dev.ikm.komet.kview.mvvm.viewmodel.GenEditingViewModel.WINDOW_TOPIC;

public class GenPurposeDetailsController {

    private static final Logger LOG = LoggerFactory.getLogger(GenPurposeDetailsController.class);

    @FXML
    private VBox mainContent;

    @FXML
    private BorderPane detailsOuterBorderPane;

    @FXML
    private ToggleButton propertiesToggleButton;

    @FXML
    private MenuButton coordinatesMenuButton;

    /**
     * model required for the filter coordinates menu, used with coordinatesMenuButton
     */
    private ViewMenuModel viewMenuModel;

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
    StampViewControl stampViewControl;

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

    private final HashMap<EditorSectionModel, TitledPane> sectionModelToTitledPane = new HashMap<>();

    private EditorWindowModel editorWindowModel;

    private boolean isUpdatingStampSelection = false;

    private final Tooltip publishTooltip = new Tooltip();
    private ViewProperties viewProperties;

    @InjectViewModel
    private GenPurposeViewModel genPurposeViewModel;

    private final List<KLReadOnlyBaseControl> controls = new ArrayList<>();

    @FXML
    private void initialize() {
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
    }

    @FXML
    private void openPropertiesPanel() {
        LOG.info("propBumpOutListener - Opening Properties bumpout toggle = " + propertiesToggleButton.isSelected());

        propertiesToggleButton.setSelected(true);
        if (isClosed(propertiesSlideoutTrayPane)) {
            slideOut(propertiesSlideoutTrayPane, detailsOuterBorderPane);
        }

        updateDraggableNodesForPropertiesPanel(true);
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
        } else {
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

    private Consumer<GenPurposeDetailsController> onCloseConceptWindow;

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
        editorWindowModel.getAdditionalSections().addListener(this::onAdditionalSectionsChanged);

        EntityFacade refConcept = (EntityFacade) genPurposeViewModel.getProperty(REF_COMPONENT).getValue();
        updateDisplayIdentifier(refConcept);
        updateIdenticon(refConcept);
        updateWindowTitle(refConcept);
        updateStampControl(refConcept);
    }

    private TitledPane createTitledPane(EditorSectionModel sectionModel) {
        SectionTitledPane titledPane = new SectionTitledPane();
        titledPane.textProperty().bind(sectionModel.nameProperty());

        titledPane.setMaxHeight(Double.MAX_VALUE);
        titledPane.setMaxWidth(Double.MAX_VALUE);

        titledPane.setNodeOrientation(NodeOrientation.LEFT_TO_RIGHT);

        titledPane.getStyleClass().add("pattern-titled-pane");

        GridPane titledPaneContent = new GridPane();

        sectionModel.numberColumnsProperty().subscribe(newNumberColumns -> {
            List<ColumnConstraints> columns = new ArrayList<>();
            for (int i = 0; i < newNumberColumns.intValue(); ++i) {
                ColumnConstraints columnConstraints = new ColumnConstraints();
                columnConstraints.setHgrow(Priority.ALWAYS);
                columnConstraints.setPercentWidth(100 / ((double)newNumberColumns.intValue()));
                columns.add(columnConstraints);
            }
            titledPaneContent.getColumnConstraints().setAll(columns);
        });

        titledPane.setContent(titledPaneContent);

        titledPane.setOnEditAction(this::showAndEditSemanticFieldsPanel);

        sectionModelToTitledPane.put(sectionModel, titledPane);

        return titledPane;
    }

    private void showAndEditSemanticFieldsPanel(ActionEvent actionEvent) {
        EntityFacade semantic = genPurposeViewModel.getPropertyValue(SEMANTIC);

        // notify bump out to display edit fields in bump out area.
        EvtBusFactory.getDefaultEvtBus()
                .publish(genPurposeViewModel.getPropertyValue(WINDOW_TOPIC),
                        new PropertyPanelEvent(actionEvent.getSource(),
                                SHOW_EDIT_SEMANTIC_FIELDS, semantic));
        // open properties bump out.
        EvtBusFactory.getDefaultEvtBus().publish(genPurposeViewModel.getPropertyValue(WINDOW_TOPIC), new PropertyPanelEvent(actionEvent.getSource(), OPEN_PANEL));

        // Set all controls to edit mode
        for (Node node : controls) {
            KLReadOnlyBaseControl klReadOnlyBaseControl = (KLReadOnlyBaseControl) node;
            klReadOnlyBaseControl.setEditMode(true);
        }
    }

    private void addPatternViews(EditorSectionModel sectionModel, List<? extends EditorPatternModel> patternModels) {
        GridPane content = (GridPane) sectionModelToTitledPane.get(sectionModel).getContent();
        for (EditorPatternModel editorPatternModel : patternModels) {
            int nid = editorPatternModel.getNid();

            EntityHandle handle = EntityHandle.get(nid);
            handle.asPattern().ifPresentOrElse(patternEntity -> addPatternView(editorPatternModel, patternEntity, content),
            () -> {
              throw new RuntimeException("Expecting a Pattern");
            });
        }
    }

    private void addPatternView(EditorPatternModel editorPatternModel, PatternEntity patternEntity, GridPane content) {
        VBox patternMainContainer = new VBox();
        patternMainContainer.getStyleClass().add("pattern-container");

        // Pattern title
        Label patternTitle = new Label(editorPatternModel.getTitle());
        patternTitle.getStyleClass().add("gen-purpose-pattern-title");
        patternTitle.visibleProperty().bind(editorPatternModel.titleVisibleProperty());
        patternTitle.managedProperty().bind(editorPatternModel.titleVisibleProperty());
        patternMainContainer.getChildren().add(patternTitle);

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

        // Pattern Fields Container
        VBox semanticsContainer = new VBox();
        semanticsContainer.getStyleClass().add("semantics-container");

        // Pattern fields
        createFieldViews(editorPatternModel, patternEntity, composer, semanticsContainer);

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

    private List<KLReadOnlyBaseControl> createFieldViews(EditorPatternModel editorPatternModel, PatternEntity patternEntity,
                                                         ObservableComposer composer, Pane semanticsContainer) {
        PatternVersionRecord patternVersionRecord = (PatternVersionRecord) getViewProperties().calculator().latest(patternEntity).get();
        EntityFacade refComponent = genPurposeViewModel.getPropertyValue(REF_COMPONENT);
        List<KLReadOnlyBaseControl> controlItems = new ArrayList<>();

        AtomicInteger index = new AtomicInteger(0);
        EntityService.get().forEachSemanticForComponentOfPattern(refComponent.nid(), patternEntity.nid(),
                (semantic) -> {
                    // add Separator
                    if (index.get() > 0) {
                        Separator separator = new Separator();
                        semanticsContainer.getChildren().add(separator);
                    }

                    GridPane fieldsContainer = new GridPane();
                    fieldsContainer.getStyleClass().add("fields-container");

                    ObservableEntitySnapshot<?,?> snap = composer.snapshot(semantic.nid()).get();
                    if (snap instanceof ObservableSemanticSnapshot semanticSnapshot) {
                        for(ObservableField<?> observableField : semanticSnapshot.getLatestFields().get()){
                            EditorFieldModel fieldModel = editorPatternModel.getFields().get(observableField.field().indexInPattern());
                            createFieldView(observableField, fieldModel, controlItems, fieldsContainer);
                        }
                    }

                    editorPatternModel.numberColumnsProperty().subscribe(numberColumns -> {
                        List<ColumnConstraints> columns = new ArrayList<>();
                        for (int i = 0; i < numberColumns.intValue(); ++i) {
                            ColumnConstraints columnConstraints = new ColumnConstraints();
                            columnConstraints.setHgrow(Priority.ALWAYS);
                            columnConstraints.setPercentWidth(100 / ((double)numberColumns.intValue()));
                            columns.add(columnConstraints);
                        }
                        fieldsContainer.getColumnConstraints().setAll(columns);
                    });

                    semanticsContainer.getChildren().add(fieldsContainer);

                    index.incrementAndGet();
                });

        controls.addAll(controlItems);
        return controlItems;
    }

    private void createFieldView(ObservableField<?> observableField, EditorFieldModel fieldModel, List<KLReadOnlyBaseControl> controlItems, GridPane fieldContainer) {
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

        fieldContainer.getChildren().add(baseControl);
    }


    private void onSectionPatternsChanged(EditorSectionModel editorSectionModel, ListChangeListener.Change<? extends EditorPatternModel> change) {
        while(change.next()) {
            if (change.wasAdded()) {
                addPatternViews(editorSectionModel, change.getAddedSubList());
            }
        }
    }

    private void onAdditionalSectionsChanged(ListChangeListener.Change<? extends EditorSectionModel> change) {
        while(change.next()) {
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