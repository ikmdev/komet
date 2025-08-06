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
package dev.ikm.komet.kview.mvvm.view.details;

import static dev.ikm.komet.kview.events.ClosePropertiesPanelEvent.*;
import static dev.ikm.komet.kview.fxutils.IconsHelper.IconType.*;
import static dev.ikm.komet.kview.fxutils.MenuHelper.*;

import static dev.ikm.komet.kview.fxutils.SlideOutTrayHelper.*;
import static dev.ikm.komet.kview.fxutils.ViewportHelper.*;
import static dev.ikm.komet.kview.fxutils.window.DraggableSupport.*;
import static dev.ikm.komet.kview.klfields.KlFieldHelper.*;
import static dev.ikm.komet.kview.mvvm.model.DataModelHelper.*;
import static dev.ikm.komet.kview.mvvm.viewmodel.ConceptViewModel.*;
import static dev.ikm.komet.kview.mvvm.viewmodel.ConceptViewModel.CREATE;
import static dev.ikm.komet.kview.mvvm.viewmodel.ConceptViewModel.CURRENT_JOURNAL_WINDOW_TOPIC;
import static dev.ikm.komet.kview.mvvm.viewmodel.ConceptViewModel.EDIT;
import static dev.ikm.komet.kview.mvvm.viewmodel.FormViewModel.*;
import static dev.ikm.komet.kview.mvvm.viewmodel.FormViewModel.MODE;
import static dev.ikm.komet.kview.mvvm.viewmodel.StampViewModel.*;
import static dev.ikm.tinkar.coordinate.stamp.StampFields.*;
import static dev.ikm.tinkar.coordinate.stamp.StampFields.MODULE;
import static dev.ikm.tinkar.coordinate.stamp.StampFields.PATH;
import static dev.ikm.tinkar.events.FrameworkTopics.*;
import static dev.ikm.tinkar.terms.TinkarTerm.*;
import dev.ikm.komet.framework.*;
import dev.ikm.komet.framework.events.appevents.*;
import dev.ikm.komet.framework.observable.*;
import dev.ikm.komet.framework.propsheet.*;
import dev.ikm.komet.framework.view.*;
import dev.ikm.komet.kview.controls.*;
import dev.ikm.komet.kview.events.*;
import dev.ikm.komet.kview.events.genediting.*;
import dev.ikm.komet.kview.fxutils.*;
import dev.ikm.komet.kview.mvvm.model.*;
import dev.ikm.komet.kview.mvvm.view.journal.*;
import dev.ikm.komet.kview.mvvm.view.properties.*;
import dev.ikm.komet.kview.mvvm.view.stamp.*;
import dev.ikm.komet.kview.mvvm.viewmodel.*;
import dev.ikm.komet.preferences.*;
import dev.ikm.tinkar.common.id.*;
import dev.ikm.tinkar.coordinate.stamp.calculator.*;
import dev.ikm.tinkar.coordinate.view.calculator.*;
import dev.ikm.tinkar.entity.*;
import dev.ikm.tinkar.entity.FieldDefinitionRecord;
import dev.ikm.tinkar.events.*;
import dev.ikm.tinkar.terms.*;
import javafx.application.*;
import javafx.beans.*;
import javafx.beans.Observable;
import javafx.beans.property.*;
import javafx.collections.*;
import javafx.css.*;
import javafx.event.*;
import javafx.fxml.*;
import javafx.geometry.*;
import javafx.scene.*;
import javafx.scene.Node;
import javafx.scene.control.*;
import javafx.scene.image.*;
import javafx.scene.input.*;
import javafx.scene.layout.*;
import javafx.scene.shape.*;
import javafx.scene.text.*;
import org.carlfx.cognitive.loader.*;
import org.carlfx.cognitive.viewmodel.*;
import org.controlsfx.control.*;
import org.eclipse.collections.api.factory.*;
import org.eclipse.collections.api.list.*;
import org.slf4j.*;

import java.time.*;
import java.time.format.*;
import java.util.*;
import java.util.function.*;
import java.util.stream.*;
import java.util.function.Consumer;
import java.util.stream.Collectors;

import static dev.ikm.komet.kview.events.ClosePropertiesPanelEvent.CLOSE_PROPERTIES;
import static dev.ikm.tinkar.common.service.PrimitiveData.PREMUNDANE_TIME;
import static dev.ikm.tinkar.common.util.time.DateTimeUtil.PREMUNDANE;
import static dev.ikm.tinkar.events.FrameworkTopics.CALCULATOR_CACHE_TOPIC;
import static dev.ikm.tinkar.events.FrameworkTopics.RULES_TOPIC;
import static dev.ikm.komet.kview.fxutils.IconsHelper.IconType.ATTACHMENT;
import static dev.ikm.komet.kview.fxutils.IconsHelper.IconType.COMMENTS;
import static dev.ikm.komet.kview.fxutils.MenuHelper.fireContextMenuEvent;
import static dev.ikm.komet.kview.fxutils.SlideOutTrayHelper.*;
import static dev.ikm.komet.kview.fxutils.ViewportHelper.clipChildren;
import static dev.ikm.komet.kview.fxutils.window.DraggableSupport.addDraggableNodes;
import static dev.ikm.komet.kview.fxutils.window.DraggableSupport.removeDraggableNodes;
import static dev.ikm.komet.kview.klfields.KlFieldHelper.retrieveCommittedLatestVersion;
import static dev.ikm.komet.kview.mvvm.model.DataModelHelper.*;
import static dev.ikm.komet.kview.mvvm.viewmodel.ConceptViewModel.*;
import static dev.ikm.komet.kview.mvvm.viewmodel.FormViewModel.MODE;
import static dev.ikm.komet.kview.mvvm.viewmodel.StampViewModel.MODULES_PROPERTY;
import static dev.ikm.komet.kview.mvvm.viewmodel.StampViewModel.PATHS_PROPERTY;
import static dev.ikm.tinkar.coordinate.stamp.StampFields.*;
import static dev.ikm.tinkar.coordinate.stamp.StampFields.MODULE;
import static dev.ikm.tinkar.coordinate.stamp.StampFields.PATH;
import static dev.ikm.tinkar.terms.TinkarTerm.*;

public class DetailsController  {

    private static final PseudoClass STAMP_SELECTED = PseudoClass.getPseudoClass("selected");

    private static final Logger LOG = LoggerFactory.getLogger(DetailsController.class);

    private static final String EDIT_STAMP_OPTIONS_FXML = "stamp-edit.fxml";

    @FXML
    private Pane stampContainer;

    @FXML
    private MenuButton coordinatesMenuButton;

    /**
     * model required for the filter coordinates menu, used with coordinatesMenuButton
     */
    private ViewMenuModel viewMenuModel;

    @FXML
    private Button closeConceptButton;

    /**
     * The outermost part of the details (may remove)
     */
    @FXML
    private BorderPane detailsOuterBorderPane;

    /**
     * The inner border pane contains all content.
     */
    @FXML
    private BorderPane detailsCenterBorderPane;

    @FXML
    private Button addDescriptionButton;


    //////////  Banner area /////////////////////
    @FXML
    private ImageView identiconImageView;

    @FXML
    private Label fqnTitleText;
    @FXML
    private Tooltip conceptNameTooltip;

    @FXML
    private TextField definitionTextField;

    @FXML
    private PublicIDControl identifierControl;

    @FXML
    private Label lastUpdatedLabel;

    @FXML
    private Label moduleLabel;

    @FXML
    private Label pathLabel;

    @FXML
    private Label statusLabel;

    /**
     * Applied to lastUpdatedText component.
     */
    private Tooltip authorTooltip = new Tooltip();

    @FXML
    private ScrollPane conceptContentScrollPane;

    ///// Descriptions Section /////////////////////////////////
    @FXML
    private TitledPane descriptionsTitledPane;

    @FXML
    private Button editConceptButton;

    @FXML
    private TextFlow fqnContainer;

    @FXML
    private Text latestFqnText;

    @FXML
    private Text fqnDescriptionSemanticText;

    @FXML
    private TextFlow fqnDateAddedTextFlow;

    @FXML
    private Label fqnAddDateLabel;

    @FXML
    private Text otherNamesHeaderText;

    /**
     * Responsible for holding rows of other names (regular) description semantics.
     */
    @FXML
    private KLExpandableNodeListControl otherNamesNodeListControl;

    ///// Axioms Section    ///////////////////
    @FXML
    private TitledPane axiomsTitledPane;

    @FXML
    private Button elppSemanticCountButton;

    /**
     * Responsible for holding rows of Axiom semantics as Property Sheet (SheetItem) from ControlsFX.
     */
    @FXML
    private BorderPane inferredAxiomPane;

    @FXML
    private BorderPane statedAxiomPane;

    @FXML
    private Label notAvailInferredAxiomLabel;

    @FXML
    private Label notAvailStatedAxiomLabel;


    @FXML
    private HBox conceptHeaderControlToolBarHbox;

    @FXML
    private Button addAxiomButton;

    /**
     * Opens or slides out the properties window.
     */
    @FXML
    private ToggleButton propertiesToggleButton;
    /**
     * This is called after dependency injection has occurred to the JavaFX controls above.
     */

    /**
     * Used slide out the properties view
     */
    @FXML
    private VerticallyFilledPane propertiesSlideoutTrayPane;

    @FXML
    private VerticallyFilledPane timelineSlideoutTrayPane;

    @FXML
    private ContextMenu reasonerContextMenu;

    @FXML
    private MenuItem incrementalReasoner;

    /**
     * A function from the caller. This class passes a boolean true if classifier button is pressed invoke caller's function to be returned a view.
     */
    private Consumer<ToggleButton> reasonerResultsControllerConsumer;

    private PropertiesController propertiesController;

    @InjectViewModel
    private ConceptViewModel conceptViewModel;
    private EvtBus eventBus;

    private UUID conceptTopic;

    private Subscriber<EditConceptFullyQualifiedNameEvent> editConceptFullyQualifiedNameEventSubscriber;

    private Subscriber<AddFullyQualifiedNameEvent> addFullyQualifiedNameEventSubscriber;

    private Subscriber<EditOtherNameConceptEvent> editOtherNameConceptEventSubscriber;
    private Subscriber<EditConceptEvent> editConceptEventSubscriber;

    private Subscriber<AddOtherNameToConceptEvent> addOtherNameToConceptEventSubscriber;

    private Subscriber<ClosePropertiesPanelEvent> closePropertiesPanelEventSubscriber;

    private Subscriber<CreateConceptEvent> createConceptEventSubscriber;


    private Subscriber<AxiomChangeEvent> changeSetTypeEventSubscriber;

    private Subscriber<RefreshCalculatorCacheEvent> refreshCalculatorEventSubscriber;


    private PublicId fqnPublicId;

    // Pseudo-class for vertical scrollbar visibility.
    private static final PseudoClass V_SCROLLBAR_NEEDED = PseudoClass.getPseudoClass("vertical-scroll-needed");

    /**
     * Stamp Edit
     */
    private PopOver stampEdit;
    private StampEditController stampEditController;

    /**
     *  TODO: View Calculator will need to be refreshed.
     */
    private ViewCalculatorWithCache viewCalculatorWithCache;

    private final EventHandler<MouseEvent> mouseFilterPressedOnScene = this::onMouseFilterPressedOnScene;

    private BooleanProperty stampSelected;

    public DetailsController() {
    }

    public DetailsController(UUID conceptTopic) {
        this.conceptTopic = conceptTopic;
    }

    @FXML
    public void initialize() {
        stampSelected = new SimpleBooleanProperty(false);
        stampSelected.subscribe(this::onStampSelectionChanged);

        identiconImageView.setOnContextMenuRequested(contextMenuEvent -> {
            // query all available memberships (semantics having the purpose as 'membership', and no fields)
            // query current concept's membership semantic records.
            // build menuItems according to 'add' or 'remove' , style to look like figma designs style classes.
            // show offset to the right of the identicon
            ViewCalculator viewCalculator = conceptViewModel.getViewProperties().calculator();
            EntityFacade currentConceptFacade = conceptViewModel.getPropertyValue(CURRENT_ENTITY);
            List<PatternEntityVersion> patterns = getMembershipPatterns();
            ContextMenu membershipContextMenu = new ContextMenu();
            membershipContextMenu.getStyleClass().add("kview-context-menu");

            Comparator<MenuItem> patternMenuComparator = (m1, m2) -> m1.getText().compareToIgnoreCase(m2.getText());
            List<MenuItem> addedMenuItems = new ArrayList<>();
            List<MenuItem> removedMenuItems = new ArrayList<>();
            for (PatternEntityVersion pattern : patterns) {
                MenuItem menuItem = new MenuItem();
                if (isInMembershipPattern(currentConceptFacade.nid(), pattern.nid(), viewCalculator)) {
                    menuItem.setText("Remove from " + pattern.entity().description());
                    menuItem.setOnAction(evt -> removeFromMembershipPattern(currentConceptFacade.nid(), pattern.entity(), viewCalculator));
                    addedMenuItems.add(menuItem);
                } else {
                    menuItem.setText("Add to " + pattern.entity().description());
                    menuItem.setOnAction(evt -> addToMembershipPattern(currentConceptFacade, pattern.entity(), viewCalculator));
                    removedMenuItems.add(menuItem);
                }
            }
            if (!addedMenuItems.isEmpty()) {
                // sort the added (able to be removed)
                addedMenuItems.sort(patternMenuComparator);
                membershipContextMenu.getItems().addAll(addedMenuItems);
                // then add a menu line separator
                if (!removedMenuItems.isEmpty()) {
                    membershipContextMenu.getItems().add(new SeparatorMenuItem());
                }
            }
            // then add the sorted removed (that can be added)
            removedMenuItems.sort(patternMenuComparator);
            membershipContextMenu.getItems().addAll(removedMenuItems);

            membershipContextMenu.show(identiconImageView, contextMenuEvent.getScreenX(),
                    contextMenuEvent.getSceneY() + identiconImageView.getFitHeight());
        });

        Tooltip.install(lastUpdatedLabel, authorTooltip);
        Tooltip.install(fqnTitleText, conceptNameTooltip);

        clearView();

        eventBus = EvtBusFactory.getDefaultEvtBus();

        // when the user clicks a fully qualified name, open the PropertiesPanel
        editConceptFullyQualifiedNameEventSubscriber = evt -> {
            if (!propertiesToggleButton.isSelected()) {
                propertiesToggleButton.fire();
            }
        };
        eventBus.subscribe(conceptTopic, EditConceptFullyQualifiedNameEvent.class, editConceptFullyQualifiedNameEventSubscriber);

        addFullyQualifiedNameEventSubscriber = evt -> {
            if (!propertiesToggleButton.isSelected()) {
                propertiesToggleButton.fire();
            }
        };
        eventBus.subscribe(conceptTopic, AddFullyQualifiedNameEvent.class, addFullyQualifiedNameEventSubscriber);

        // when the user clicks one of the other names, open the PropertiesPanel
        editOtherNameConceptEventSubscriber = evt -> {
            if (!propertiesToggleButton.isSelected()) {
                propertiesToggleButton.fire();
            }
        };
        eventBus.subscribe(conceptTopic, EditOtherNameConceptEvent.class, editOtherNameConceptEventSubscriber);

        addOtherNameToConceptEventSubscriber = evt -> {
            if (!propertiesToggleButton.isSelected()) {
                propertiesToggleButton.fire();
            }
        };
        eventBus.subscribe(conceptTopic, AddOtherNameToConceptEvent.class, addOtherNameToConceptEventSubscriber);

        // if the user clicks the Close Properties Button from the Edit Descriptions panel
        // in that state, the properties bump out will be slid out, therefore firing will perform a slide in
        closePropertiesPanelEventSubscriber = evt -> propertiesToggleButton.fire();
        eventBus.subscribe(conceptTopic, ClosePropertiesPanelEvent.class, closePropertiesPanelEventSubscriber);

        // Listener when user enters a new fqn
        ObjectProperty<DescrName> fqnProp = getConceptViewModel().getProperty(FULLY_QUALIFIED_NAME);
        fqnProp.addListener(observable -> {
            // not null, populate banner area.
            DescrName fqnDescrName = fqnProp.get();
            updateConceptBanner();
            updateFQNConceptDescription(fqnDescrName);
        });
        ObservableList<DescrName> otherNames = getConceptViewModel().getObservableList(OTHER_NAMES);
        otherNames.addListener((InvalidationListener) obs -> updateOtherNamesDescription(otherNames));

        // Listens for events related to new fqn or other names added to this concept. Subscriber is responsible for
        // the final create concept transaction.
        createConceptEventSubscriber = evt -> {
            DescrName descrName = evt.getModel();

            if (getConceptViewModel() == null || descrName == null) {
                LOG.warn("ViewModel should not be null. Event type:" + evt.getEventType());
                return;
            }

            if (CREATE.equals(conceptViewModel.getPropertyValue(MODE))) {
                if (evt.getEventType() == CreateConceptEvent.ADD_FQN) {
                    getConceptViewModel().setPropertyValue(FULLY_QUALIFIED_NAME, descrName);
                } else if (evt.getEventType() == CreateConceptEvent.ADD_OTHER_NAME) {
                    otherNames.add(descrName);
                }else if (evt.getEventType() == CreateConceptEvent.EDIT_OTHER_NAME){ // Since we are
                    updateOtherNamesDescription(otherNames);
                }
                // Attempts to write data
                boolean isWritten = conceptViewModel.createConcept(conceptViewModel.getViewProperties().calculator().viewCoordinateRecord().editCoordinate());
                // when written the mode changes to EDIT.
                LOG.info("Is " + conceptViewModel + " created? " + isWritten);
                if (isWritten) {
                    updateView();
                }
                // remove 'Add Fully Qualified Name' from the menu
                setUpDescriptionContextMenu(addDescriptionButton);
                //TODO revisit: why should the mode ever be edit inside a create event?
            } else if (EDIT.equals(conceptViewModel.getPropertyValue(MODE))){
                    conceptViewModel.addOtherName(conceptViewModel.getViewProperties().calculator().viewCoordinateRecord().editCoordinate(), descrName);
                    otherNames.add(descrName);
            }

        };
        eventBus.subscribe(conceptTopic, CreateConceptEvent.class, createConceptEventSubscriber);

        // set up the event handler for editing a concept
        editConceptEventSubscriber = evt -> {
            DescrName descrName = evt.getModel();

            if (getConceptViewModel() == null || descrName == null) {
                LOG.warn("ViewModel should not be null. Event type:" + evt.getEventType());
                return;
            }
            if (EDIT.equals(conceptViewModel.getPropertyValue(MODE))) {
                if (evt.getEventType() == EditConceptEvent.EDIT_FQN) {
                    // the listener will fire on the FQN when we update this
                    getConceptViewModel().setPropertyValue(FULLY_QUALIFIED_NAME, descrName);
                }
            }
        };
        eventBus.subscribe(conceptTopic, EditConceptEvent.class, editConceptEventSubscriber);


        // listen to rules changes to update the axioms
        changeSetTypeEventSubscriber = evt -> updateAxioms();
        eventBus.subscribe(RULES_TOPIC, AxiomChangeEvent.class, changeSetTypeEventSubscriber);

        // Filter out scroll events that try to scroll beyond the content's limits.
        conceptContentScrollPane.addEventFilter(ScrollEvent.SCROLL, event -> {
            if (shouldConsumeVerticalScroll(conceptContentScrollPane, event)) {
                event.consume();
            }
        });

        setUpEditCoordinateMenu();

        // Update the pseudo-class when the viewport or content size changes.
        conceptContentScrollPane.viewportBoundsProperty().addListener((obs) ->
                conceptContentScrollPane.pseudoClassStateChanged(V_SCROLLBAR_NEEDED, isVerticalScrollbarVisible(conceptContentScrollPane)));
        conceptContentScrollPane.getContent().layoutBoundsProperty().addListener((obs) ->
                conceptContentScrollPane.pseudoClassStateChanged(V_SCROLLBAR_NEEDED, isVerticalScrollbarVisible(conceptContentScrollPane)));

        // TODO: When event bus is more universally used the database can emit events. For now we listen for a refresh calculator events
        // Refresh Concept window
        refreshCalculatorEventSubscriber = _ -> {
            LOG.info("Refresh concept window details");
            Runnable code = () -> {
                clearView();
                updateView();
            };
            if (Platform.isFxApplicationThread()) {
                code.run();
            } else {
                Platform.runLater(code);
            }
        };

        eventBus.subscribe(CALCULATOR_CACHE_TOPIC, RefreshCalculatorCacheEvent.class, refreshCalculatorEventSubscriber);

        // Setup window support with explicit draggable nodes
        addDraggableNodes(detailsOuterBorderPane, conceptHeaderControlToolBarHbox);

        // Check if the properties panel is initially open and add draggable nodes if needed
        if (propertiesToggleButton.isSelected() || isOpen(propertiesSlideoutTrayPane)) {
            updateDraggableNodesForPropertiesPanel(true);
        }

        Subscriber<GenEditingEvent> refreshSubscriber = evt -> {
            if (evt.getEventType() == GenEditingEvent.PUBLISH){
                updateView();
            }
        };
        EvtBusFactory.getDefaultEvtBus().subscribe(conceptViewModel.getPropertyValue(CURRENT_JOURNAL_WINDOW_TOPIC),
                GenEditingEvent.class, refreshSubscriber);

        stampContainer.sceneProperty().addListener(new InvalidationListener() {
            @Override
            public void invalidated(Observable observable) {
                if (stampContainer.getScene() != null) {
                    stampContainer.getScene().addEventFilter(MouseEvent.MOUSE_PRESSED, mouseFilterPressedOnScene);
                    stampContainer.sceneProperty().removeListener(this);
                }
            }
        });
    }

    private void onMouseFilterPressedOnScene(MouseEvent mouseEvent) {
        Point2D localMousePos = detailsOuterBorderPane.sceneToLocal(mouseEvent.getSceneX(), mouseEvent.getSceneY());
        if (!detailsOuterBorderPane.contains(localMousePos)) {
            stampSelected.set(false);
        }
    }

    /**
     * Checks whether the vertical scrollbar should be visible.
     * This considers the scrollbar policy, viewport size, and content size.
     *
     * @param scrollPane the ScrollPane to check.
     * @return true if the content height exceeds the viewport height and the scrollbar is allowed.
     */
    private boolean isVerticalScrollbarVisible(ScrollPane scrollPane) {
        // 1) Only proceed if vertical scrolling is allowed.
        if (scrollPane.getVbarPolicy() == ScrollPane.ScrollBarPolicy.NEVER) {
            return false;
        }
        Node content = scrollPane.getContent();
        if (content == null) {
            return false;
        }
        double viewportHeight = scrollPane.getViewportBounds().getHeight();
        // 2) Skip if the viewport isn't yet sized or has zero height.
        if (viewportHeight <= 0) {
            return false;
        }
        double contentHeight = content.getLayoutBounds().getHeight();
        // 3) Return true if the content height exceeds the viewport height.
        return contentHeight > viewportHeight;
    }

    /**
     * Determines whether a vertical scroll event should be consumed because it attempts to scroll
     * beyond the boundaries (i.e. when at the top or bottom of the scrollable content).
     *
     * @param scrollPane the ScrollPane receiving the scroll event.
     * @param event the ScrollEvent.
     * @return true if the event should be consumed.
     */
    private boolean shouldConsumeVerticalScroll(ScrollPane scrollPane, ScrollEvent event) {
        final double EPSILON = 1e-5;

        // If the vertical scrollbar is not visible (i.e. not needed), no consumption is required.
        if (!isVerticalScrollbarVisible(scrollPane)) {
            return false;
        }

        double deltaY = event.getDeltaY();
        // Skip if there's negligible scroll movement.
        if (Math.abs(deltaY) <= EPSILON) {
            return false;
        }

        double vValue = scrollPane.getVvalue();
        double vMin = scrollPane.getVmin();
        double vMax = scrollPane.getVmax();

        // Determine if we are at the top or bottom.
        boolean atTop = Math.abs(vValue - vMin) < EPSILON;
        boolean atBottom = Math.abs(vValue - vMax) < EPSILON;

        // Consume if scrolling upward at the top or downward at the bottom.
        return (atTop && deltaY > 0) || (atBottom && deltaY < 0);
    }

    public ValidationViewModel getConceptViewModel() {
        return conceptViewModel;
    }

    public ValidationViewModel getStampViewModel() {
        return conceptViewModel.getPropertyValue(CONCEPT_STAMP_VIEW_MODEL);
    }

    private void setUpDescriptionContextMenu(Button addDescriptionButton) {
        ContextMenu contextMenu = buildMenuOptionContextMenu();
        addDescriptionButton.setContextMenu(contextMenu);
        addDescriptionButton.setOnAction(this::onAddDescriptionButtonPressed);
    }

    private void onAddDescriptionButtonPressed(ActionEvent actionEvent) {
        if (this.conceptViewModel.getPropertyValue(MODE).equals(CREATE) &&
                (getConceptViewModel().getPropertyValue(FULLY_QUALIFIED_NAME) == null)) {
            // Show the context menu with 'Add Fully Qualified' option when it is a new concept in create mode and
            // there is no fully qualified name.
            fireContextMenuEvent(actionEvent, Side.RIGHT, 2, 0);
        } else {
            // Just show the UI to add another name otherwise (don't show context menu in this case).
            showAddAnotherNameUI();
        }
    }

    private void showAddAnotherNameUI() {
        ConceptEntity currentConcept = null;
        if (getConceptViewModel().getPropertyValue(CURRENT_ENTITY) instanceof EntityProxy.Concept concept) {
            currentConcept = (ConceptEntity) EntityService.get().getEntity(concept.nid()).get();
        } else {
            currentConcept = getConceptViewModel().getPropertyValue(CURRENT_ENTITY);
        }
        if (currentConcept != null) {
            // in edit mode, will have a concept and public id
            eventBus.publish(conceptTopic, new AddOtherNameToConceptEvent(addDescriptionButton,
                    // pass the publicId of the Concept
                    AddOtherNameToConceptEvent.ADD_DESCRIPTION, currentConcept.publicId())); // concept's publicId
        } else {
            // in create mode, we won't have a concept and public id yet
            eventBus.publish(conceptTopic, new AddOtherNameToConceptEvent(addDescriptionButton,
                    AddOtherNameToConceptEvent.ADD_DESCRIPTION));
        }
    }

    private ContextMenu buildMenuOptionContextMenu() {
        MenuHelper menuHelper = MenuHelper.getInstance();
        // name, state, style class
        ContextMenu contextMenu = new ContextMenu();
        contextMenu.setHideOnEscape(true);
        contextMenu.setAutoHide(true);
        contextMenu.setConsumeAutoHidingEvents(true);

        contextMenu.getStyleClass().add("kview-context-menu");

        final int NAME = 0;
        final int ENABLED = 1;
        final int STYLES = 2;
        final int ACTION = 3;
        final int GRAPHIC = 4;

        // if there is a fully qualified name, then do not give the option Add Fully Qualified
        Object[][] menuItems;
        // show the 'Add Fully Qualified' option when it is a new concept in create mode and there is no fully qualified name
        if (this.conceptViewModel.getPropertyValue(MODE).equals(CREATE) &&
                (getConceptViewModel().getPropertyValue(FULLY_QUALIFIED_NAME) == null)) {
            menuItems = new Object[][]{
                    {"ADD DESCRIPTION", true, new String[]{"menu-header-left-align"}, null, null},
                    {MenuHelper.SEPARATOR},
                    {"Add Fully Qualified Name", true, null, (EventHandler<ActionEvent>) actionEvent ->
                            eventBus.publish(conceptTopic, new AddFullyQualifiedNameEvent(contextMenu,
                                    AddFullyQualifiedNameEvent.ADD_FQN, conceptViewModel.getViewProperties())),
                            createConceptEditDescrIcon()},
                    {"Add Other Name", true, null, (EventHandler<ActionEvent>) actionEvent -> {
                        eventBus.publish(conceptTopic, new AddOtherNameToConceptEvent(contextMenu,
                                AddOtherNameToConceptEvent.ADD_DESCRIPTION));
                    },
                            createConceptEditDescrIcon()},
                    {MenuHelper.SEPARATOR},
            };

            for (Object[] menuItemObj : menuItems) {
                if (MenuHelper.SEPARATOR.equals(menuItemObj[NAME])){
                    contextMenu.getItems().add(new SeparatorMenuItem());
                    continue;
                }

                // uses a default action if one is not given.
                EventHandler<ActionEvent> menuItemAction = switch (menuItemObj[ACTION]) {
                    case null ->  actionEvent -> LOG.info(menuItemObj[NAME] + " " + fqnTitleText.getText());
                    case EventHandler  eventHandler -> eventHandler;
                    default -> null;
                };


                // Create a menu item. Todo: if/when you have sub menus
                MenuItem menuItem = menuHelper.createMenuOption(
                        String.valueOf(menuItemObj[NAME]),                           /* name */
                        Boolean.parseBoolean(String.valueOf(menuItemObj[ENABLED])),  /* enabled */
                        (String[]) menuItemObj[STYLES],                                                  /* styling */
                        menuItemAction,                                              /* action when selected */
                        (Node) menuItemObj[GRAPHIC]                                                         /* optional graphic */
                );
                contextMenu.getItems().add(menuItem);
            }
        }

        return contextMenu;
    }

    private Region createConceptEditDescrIcon() {
        Region circlePlusIcon = new Region();
        circlePlusIcon.setPrefHeight(20);
        circlePlusIcon.setPrefWidth(20);
        circlePlusIcon.getStyleClass().add("concept-edit-description-menu-icon");
        return circlePlusIcon;
    }

    @FXML
    private void popupAddAxiomContextMenu(ActionEvent actionEvent) {
        MenuHelper.fireContextMenuEvent(actionEvent, Side.RIGHT, 0, 0);
    }

    @FXML
    private void addNecessarySet(ActionEvent actionEvent) {
        conceptViewModel.setPropertyValue(AXIOM, ConceptViewModel.NECESSARY_SET);

        // Attempts to write data
        if (CREATE.equals(conceptViewModel.getPropertyValue(MODE))) {
            boolean isWritten = conceptViewModel.createConcept(conceptViewModel.getViewProperties().calculator().viewCoordinateRecord().editCoordinate());
            LOG.info("Is " + conceptViewModel + " created? " + isWritten);
            if (isWritten) {
                updateView();
            }
        }
    }

    @FXML
    private void addSufficientSet(ActionEvent actionEvent) {
        conceptViewModel.setPropertyValue(AXIOM, ConceptViewModel.SUFFICIENT_SET);

        // Attempts to write data
        if (CREATE.equals(conceptViewModel.getPropertyValue(MODE))) {
            boolean isWritten = conceptViewModel.createConcept(conceptViewModel.getViewProperties().calculator().viewCoordinateRecord().editCoordinate());
            LOG.info("Is " + conceptViewModel + " created? " + isWritten);
            if (isWritten) {
                updateView();
            }
        }
    }

    public void attachPropertiesViewSlideoutTray(Pane propertiesViewBorderPane,
                                                 PropertiesController propertiesController) {
        this.propertiesController = propertiesController;
        addPaneToTray(propertiesViewBorderPane, propertiesSlideoutTrayPane);
    }

    public void attachTimelineViewSlideoutTray(Pane timelineViewBorderPane) {
        addPaneToTray(timelineViewBorderPane, timelineSlideoutTrayPane);
    }
    private void addPaneToTray(Pane contentViewPane, Pane slideoutTrayPane) {
        double width = contentViewPane.getWidth();
        contentViewPane.setLayoutX(width);
        contentViewPane.getStyleClass().add("slideout-tray-pane");

        slideoutTrayPane.getChildren().add(contentViewPane);
        clipChildren(slideoutTrayPane, 0);
        contentViewPane.setLayoutX(-width);
        slideoutTrayPane.setMaxWidth(0);
    }

    private Consumer<DetailsController> onCloseConceptWindow;
    public void setOnCloseConceptWindow(Consumer<DetailsController> onClose) {
        this.onCloseConceptWindow = onClose;
    }

    @FXML
    void closeConceptWindow(ActionEvent event) {
        LOG.info("Cleanup occurring: Closing Window with concept: " + fqnTitleText.getText());

        // Clean up the draggable nodes
        removeDraggableNodes(detailsOuterBorderPane,
                conceptHeaderControlToolBarHbox,
                propertiesController != null ? propertiesController.getPropertiesTabsPane() : null);

        if (this.onCloseConceptWindow != null) {
            onCloseConceptWindow.accept(this);
        }
        LOG.info("Closing & cleaning concept window: %s - %s".formatted(identifierControl.getPublicId(), fqnTitleText.getText()));
        // unsubscribe listeners
        eventBus.unsubscribe(editConceptFullyQualifiedNameEventSubscriber,
                addFullyQualifiedNameEventSubscriber,
                editOtherNameConceptEventSubscriber,
                editConceptEventSubscriber,
                addOtherNameToConceptEventSubscriber,
                closePropertiesPanelEventSubscriber,
                createConceptEventSubscriber,
                changeSetTypeEventSubscriber,
                refreshCalculatorEventSubscriber
        );
    }
    public Pane getPropertiesSlideoutTrayPane() {
        return propertiesSlideoutTrayPane;
    }

    public void updateView() {
        EntityFacade entityFacade = conceptViewModel.getPropertyValue(CURRENT_ENTITY);
        StampViewModel stampViewModel = new StampViewModel();
        if (entityFacade != null) { // edit concept
            getConceptViewModel().setPropertyValue(MODE, EDIT);
            if (conceptViewModel.getPropertyValue(CONCEPT_STAMP_VIEW_MODEL) == null) {

                // add a new stamp view model to the concept view model
                //Populate from database
                stampViewModel.setPropertyValue(MODE, EDIT)
                        .setPropertyValues(MODULES_PROPERTY, fetchDescendentsOfConcept(conceptViewModel.getViewProperties(), TinkarTerm.MODULE.publicId()))
                        .setPropertyValues(PATHS_PROPERTY, fetchDescendentsOfConcept(conceptViewModel.getViewProperties(), TinkarTerm.PATH.publicId()));

                conceptViewModel.setPropertyValue(CONCEPT_STAMP_VIEW_MODEL,stampViewModel);
            }

            // TODO: Ability to change Concept record. but for now user can edit stamp but not affect Concept version.
            EntityVersion latestVersion = conceptViewModel.getViewProperties().calculator().latest(entityFacade).get();
            StampEntity stamp = latestVersion.stamp();
            updateStampViewModel(EDIT, stamp);
        } else { // create concept
            getConceptViewModel().setPropertyValue(MODE, CREATE);
            stampViewModel.setPropertyValue(MODE, CREATE);
        }
        conceptViewModel.setPropertyValue(CONCEPT_STAMP_VIEW_MODEL, stampViewModel);


        // Display info for top banner area
        updateConceptBanner();

        // Display Description info area
        updateConceptDescription();

        // Axioms area
        updateAxioms();

        // Add a context menu to the pencil+ icon for: Add Fully Qualified, Add Other Name
        setUpDescriptionContextMenu(addDescriptionButton);
        // TODO Update stamps view model

    }
    public void onReasonerSlideoutTray(Consumer<ToggleButton> reasonerResultsControllerConsumer) {
        this.reasonerResultsControllerConsumer = reasonerResultsControllerConsumer;
    }

    /**
     * Responsible for populating the top banner area of the concept view panel.
     */
    public void updateConceptBanner() {
        // do not update ui should be blank
        if (getConceptViewModel().getPropertyValue(MODE) == CREATE) {
            return;
        }

        EntityFacade entityFacade = conceptViewModel.getPropertyValue(CURRENT_ENTITY);
        // TODO do a null check on the entityFacade
        // Title (FQN of concept)
        final ViewCalculator viewCalculator = conceptViewModel.getViewProperties().calculator();
        String conceptNameStr = viewCalculator.getFullyQualifiedDescriptionTextWithFallbackOrNid(entityFacade.nid());
        fqnTitleText.setText(conceptNameStr);
        conceptNameTooltip.setText(conceptNameStr);

        // Definition description text
        definitionTextField.setText(viewCalculator.getDefinitionDescriptionText(entityFacade.nid()).orElse(""));

        setupDisplayUUID(entityFacade, viewCalculator);

        // Identicon
        Image identicon = Identicon.generateIdenticonImage(entityFacade.publicId());
        identiconImageView.setImage(identicon);

        // Obtain STAMP info
        EntityVersion latestVersion = viewCalculator.latest(entityFacade).get();
        StampEntity stamp = latestVersion.stamp();

        // Status
        String status = stamp.state() != null && State.ACTIVE == stamp.state() ? "Active" : "Inactive";
        statusLabel.setText(status);

        // Module
        String module = stamp.module().description();
        moduleLabel.setText(module);

        // Path
        String path = stamp.path().description();
        pathLabel.setText(path);

        // Latest update time
        DateTimeFormatter DATE_TIME_FORMATTER = dateFormatter("yyyy-MMM-dd HH:mm:ss");
        Instant stampInstance = Instant.ofEpochSecond(stamp.time()/1000);
        String time = DATE_TIME_FORMATTER.format(stampInstance);
        lastUpdatedLabel.setText(time);

        // Author tooltip
        authorTooltip.setText(stamp.author().description());

    }

    /// Show the public ID
    private void setupDisplayUUID(EntityFacade entityFacade, ViewCalculator viewCalculator) {
        List<String> idList = entityFacade.publicId().asUuidList().stream()
                .map(UUID::toString)
                .collect(Collectors.toList());
        idList.addAll(DataModelHelper.getIdsToAppend(viewCalculator, entityFacade.toProxy()));
        String idStr = String.join(", ", idList);

        identifierControl.setPublicId(idStr);
    }

    private void updateStampViewModel(String mode, StampEntity stamp) {
        ValidationViewModel stampViewModel = conceptViewModel.getPropertyValue(CONCEPT_STAMP_VIEW_MODEL);
        if (conceptViewModel.getPropertyValue(CONCEPT_STAMP_VIEW_MODEL) != null) {
            stampViewModel.setPropertyValue(MODE, mode)
                    .setPropertyValue(STATUS, stamp.state())
                    .setPropertyValue(MODULE, stamp.moduleNid())
                    .setPropertyValue(PATH, stamp.pathNid())
                    .setPropertyValue(TIME, stamp.time())
                    .save(true);
        }
    }

    public void updateFQNConceptDescription(DescrName fqnDescrName) {
        // populate UI with FQN and other names. e.g. Hello Solor (English | Case-insensitive)
        // Latest FQN
        String fullyQualifiedName = fqnDescrName.getNameText();
        latestFqnText.setText(fullyQualifiedName);

        fqnContainer.setOnMouseClicked(event -> eventBus.publish(conceptTopic,
                new EditConceptFullyQualifiedNameEvent(latestFqnText,
                        EditConceptFullyQualifiedNameEvent.EDIT_FQN, fqnDescrName)));
        // these should never be null, if the drop-downs are populated then the
        // submit button will not be enabled on the Add FQN form
        if (fqnDescrName.getCaseSignificance() != null && fqnDescrName.getLanguage() != null) {
            fqnDescriptionSemanticText.setText(" (" + fqnDescrName.getCaseSignificance().description()
                    + ", " + fqnDescrName.getLanguage().description() + ")");
        } else {
            LOG.error("missing case sensitivity and language when adding a fully qualified name");
            fqnDescriptionSemanticText.setText("");
        }

        fqnContainer.getChildren().setAll(latestFqnText, fqnDescriptionSemanticText);
    }

    public void updateOtherNamesDescription(List<DescrName> descrNameViewModels) {
        otherNamesNodeListControl.getItems().clear();
        descrNameViewModels.forEach(otherName -> {
                    // start adding a row
                    VBox otherNameBox = generateOtherNameRow(otherName);
                    TextFlow firstRow = (TextFlow) otherNameBox.getChildren().getFirst();
                    firstRow.setOnMouseClicked(event -> eventBus.publish(conceptTopic,
                            new EditOtherNameConceptEvent(otherNameBox,
                                    EditOtherNameConceptEvent.EDIT_OTHER_NAME, otherName)));
                    otherNamesNodeListControl.getItems().add(otherNameBox);
        });
    }
    /**
     * Responsible for populating the Descriptions TitledPane area. This retrieves the latest concept version and
     * semantics for language and case significance.
     */
    public void updateConceptDescription() {
        // do not update ui should be blank
        if (getConceptViewModel().getPropertyValue(MODE) == CREATE) {
            return;
        }

        final ViewCalculator viewCalculator = conceptViewModel.getViewProperties().calculator();

        EntityFacade entityFacade = conceptViewModel.getPropertyValue(CURRENT_ENTITY);
        // populate UI with FQN and other names. e.g. Hello Solor (English | Case-insensitive)
        Map<SemanticEntityVersion, List<String>> descriptionSemanticsMap = latestDescriptionSemantics(entityFacade);
        otherNamesNodeListControl.getItems().clear();

        //Obtain the index field of DESCRIPTION_TYPE
        PatternEntityVersion patternEntityVersion = (PatternEntityVersion)viewCalculator.latest(DESCRIPTION_PATTERN.nid()).get();
        int descriptionTypeIndex = patternEntityVersion.indexForMeaning(DESCRIPTION_TYPE.nid());

        descriptionSemanticsMap.forEach((semanticEntityVersion, fieldDescriptions) -> {
            EntityFacade fieldTypeValue = (EntityFacade) semanticEntityVersion.fieldValues().get(descriptionTypeIndex);
            boolean isFQN = FULLY_QUALIFIED_NAME_DESCRIPTION_TYPE.nid() == fieldTypeValue.nid();
            boolean isOtherName = REGULAR_NAME_DESCRIPTION_TYPE.nid() == fieldTypeValue.nid();

            if (isFQN) {
                // Latest FQN
                updateFQNSemantics(semanticEntityVersion, fieldDescriptions);
                LOG.debug("FQN Name = " + semanticEntityVersion + " " + fieldDescriptions);
            } else if (isOtherName) {
                // start adding a row
                VBox otherNameBox = generateOtherNameRow(semanticEntityVersion, fieldDescriptions);
                PublicId otherNamePublicId = (PublicId) otherNameBox.getChildren().getFirst().getUserData();
                TextFlow firstRow = (TextFlow) otherNameBox.getChildren().getFirst();
                firstRow.setOnMouseClicked(event -> eventBus.publish(conceptTopic,
                        new EditOtherNameConceptEvent(otherNameBox,
                                EditOtherNameConceptEvent.EDIT_OTHER_NAME, otherNamePublicId)));
                otherNamesNodeListControl.getItems().add(otherNameBox);

                LOG.debug("Other Names = " + semanticEntityVersion + " " + fieldDescriptions);
            }
        });

        final int otherNamesCount = otherNamesNodeListControl.getItems().size();
        otherNamesHeaderText.setText(otherNamesCount > 0 ?
                String.format("OTHER NAMES (%d):", otherNamesCount) : "OTHER NAMES:");
    }

    /**
     * Returns a list of TextFlow objects as rows of text items allowing the window to allow text
     * to be responsive when the gets width smaller.
     * @param semanticEntityVersion
     * @param fieldDescriptions
     * @return
     */
    private VBox generateOtherNameRow(SemanticEntityVersion semanticEntityVersion, List<String> fieldDescriptions) {
        VBox textFlowsBox = new VBox();
        String descrSemanticStr = String.join(", ", fieldDescriptions);

        // create textflow to hold regular name label
        TextFlow row1 = new TextFlow();
        String otherNameDescText = getFieldValueByMeaning(semanticEntityVersion, TinkarTerm.TEXT_FOR_DESCRIPTION);
        Text otherNameLabel = new Text(otherNameDescText);
        otherNameLabel.getStyleClass().add("descr-concept-name");

        Text semanticDescrText = new Text();
        if (!fieldDescriptions.isEmpty()) {
            semanticDescrText.setText(" (%s)".formatted(descrSemanticStr));
            semanticDescrText.getStyleClass().add("descr-concept-name");
        } else {
            semanticDescrText.setText("");
        }
        // add the other name label and description semantic label
        row1.getStyleClass().add("descr-semantic-container");
        // store the public id of this semantic entity version
        // so that when clicked the event bus can pass it to the form
        // and the form can populate the data from the publicId
        row1.setUserData(semanticEntityVersion.publicId());
        row1.getChildren().addAll(otherNameLabel, semanticDescrText);

        TextFlow row2 = new TextFlow();
        Text dateAddedLabel = new Text("Date Added: ");
        dateAddedLabel.getStyleClass().add("grey8-12pt-bold");

        if (semanticEntityVersion.publicId() != null) {
            ViewCalculator viewCalculator = conceptViewModel.getViewProperties().calculator();
            Latest<EntityVersion> semanticVersionLatest = viewCalculator.latest(Entity.nid(semanticEntityVersion.publicId()));
            semanticVersionLatest.ifPresent(entityVersion -> {
                long rawTime = entityVersion.time();
                String dateText = null;
                if (rawTime == PREMUNDANE_TIME) {
                    dateText = PREMUNDANE;
                } else {
                    Locale userLocale = Locale.getDefault();
                    LocalDate localDate = Instant.ofEpochMilli(rawTime).atZone(ZoneId.systemDefault()).toLocalDate();
                    DateTimeFormatter formatter = DateTimeFormatter.ofLocalizedDate(FormatStyle.MEDIUM).withLocale(userLocale);
                    dateText = formatter.format(localDate);
                }

                Text dateLabel = new Text(dateText);
                dateLabel.getStyleClass().add("grey8-12pt-bold");

                Region spacer = new Region();
                spacer.setMinWidth(10);

                Hyperlink attachmentHyperlink = createActionLink(IconsHelper.createIcon(ATTACHMENT));
                Hyperlink commentsHyperlink = createActionLink(IconsHelper.createIcon(COMMENTS));

                // Add the date info and additional hyperlinks
                row2.getChildren().addAll(dateAddedLabel, dateLabel, spacer, attachmentHyperlink, commentsHyperlink);
            });
        }

        textFlowsBox.getChildren().addAll(row1, row2);
        return textFlowsBox;
    }

    private VBox generateOtherNameRow(DescrName otherName) {
        VBox textFlowsBox = new VBox();
        ViewCalculator viewCalculator = conceptViewModel.getViewProperties().calculator();
        ConceptEntity caseSigConcept = otherName.getCaseSignificance();
        String casSigText = viewCalculator.getRegularDescriptionText(caseSigConcept.nid())
                .orElse(caseSigConcept.nid()+"");
        ConceptEntity langConcept = otherName.getLanguage();

        String langText = viewCalculator.getRegularDescriptionText(langConcept.nid())
                .orElse(String.valueOf(langConcept.nid()));

        String descrSemanticStr = "%s, %s".formatted(casSigText, langText);

        // create textflow to hold regular name label
        TextFlow row1 = new TextFlow();

        // TODO: need to retrieve the description Semantic's Text field (latest version text).

        PublicId semanticPid = otherName.getSemanticPublicId();
        Text otherNameLabel;

        // the semanticPublicId is null in CREATE mode, so use the nameText that was entered
        // instead of the semanticPublicId field value
        if (semanticPid != null) {
            int nid = EntityService.get().nidForPublicId(semanticPid);
            Latest<SemanticEntityVersion> regularDescriptionTextversion = viewCalculator.latest(nid);
            otherNameLabel = new Text(regularDescriptionTextversion.get().fieldValues().get(1).toString());
        } else {
            otherNameLabel = new Text(otherName.getNameText());
        }
        LOG.info("otherNameLabel : "+otherNameLabel);

        otherNameLabel.getStyleClass().add("descr-concept-name");

        Text semanticDescrText = new Text();
        semanticDescrText.setText(" (%s)".formatted(descrSemanticStr));
        semanticDescrText.getStyleClass().add("descr-concept-name");

        // add the other name label and description semantic label
        row1.getStyleClass().add("descr-semantic-container");
        // store the public id of this semantic entity version
        // so that when clicked the event bus can pass it to the form
        // and the form can populate the data from the publicId
//        this.otherNamePublicId = semanticEntityVersion.publicId();

        row1.getChildren().addAll(otherNameLabel, semanticDescrText);

        TextFlow row2 = new TextFlow();
        Text dateAddedLabel = new Text("Date Added: ");
        dateAddedLabel.getStyleClass().add("grey8-12pt-bold");

        if (otherName.getSemanticPublicId() != null) {
            Latest<EntityVersion> semanticVersionLatest = viewCalculator.latest(Entity.nid(otherName.getSemanticPublicId()));
            semanticVersionLatest.ifPresent(entityVersion -> {
                long rawTime = entityVersion.time();
                String dateText = null;
                if (rawTime == PREMUNDANE_TIME) {
                    dateText = PREMUNDANE;
                } else {
                    Locale userLocale = Locale.getDefault();
                    LocalDate localDate = Instant.ofEpochMilli(rawTime).atZone(ZoneId.systemDefault()).toLocalDate();
                    DateTimeFormatter formatter = DateTimeFormatter.ofLocalizedDate(FormatStyle.MEDIUM).withLocale(userLocale);
                    dateText = formatter.format(localDate);
                }

                Text dateLabel = new Text(dateText);
                dateLabel.getStyleClass().add("grey8-12pt-bold");

                Region spacer = new Region();
                spacer.setMinWidth(10);

                Hyperlink attachmentHyperlink = createActionLink(IconsHelper.createIcon(ATTACHMENT));
                Hyperlink commentsHyperlink = createActionLink(IconsHelper.createIcon(COMMENTS));

                // Add the date info and additional hyperlinks
                row2.getChildren().addAll(dateAddedLabel, dateLabel, spacer, attachmentHyperlink, commentsHyperlink);
            });
        }
        textFlowsBox.getChildren().addAll(row1, row2);
        return textFlowsBox;
    }

    private void updateFQNSemantics(SemanticEntityVersion semanticEntityVersion, List<String> fieldDescriptions) {
        DateTimeFormatter DATE_TIME_FORMATTER = dateFormatter("MMM dd, yyyy");
        String fqnTextDescr = getFieldValueByMeaning(semanticEntityVersion, TinkarTerm.TEXT_FOR_DESCRIPTION);
        // obtain the fqn description
        latestFqnText.setText(fqnTextDescr);

        this.fqnPublicId = semanticEntityVersion.publicId();
        fqnContainer.setOnMouseClicked(event -> eventBus.publish(conceptTopic,
                new EditConceptFullyQualifiedNameEvent(latestFqnText,
                        EditConceptFullyQualifiedNameEvent.EDIT_FQN, fqnPublicId)));

        String descrSemanticStr = String.join(", ", fieldDescriptions);
        if (!fieldDescriptions.isEmpty()) {
            fqnDescriptionSemanticText.setText(" (%s)".formatted(descrSemanticStr));
        } else {
            fqnDescriptionSemanticText.setText("");
        }

        fqnContainer.getChildren().setAll(latestFqnText, fqnDescriptionSemanticText);

        // update date
        String dateAddedStr = "";
        if (semanticEntityVersion.stamp() == null) {
            dateAddedStr = LocalDate.now().format(DateTimeFormatter.ofPattern("MMM d, yyyy")).toString();
        } else {
            Long fieldMilis = semanticEntityVersion.stamp().time();
            if (fieldMilis.equals(PREMUNDANE_TIME)) {
                dateAddedStr = PREMUNDANE;
            } else {
                LocalDate localDate = Instant.ofEpochMilli(fieldMilis).atZone(ZoneId.systemDefault()).toLocalDate();
                dateAddedStr = localDate.format(DateTimeFormatter.ofPattern("MMM d, yyyy")).toString();
            }
        }
        fqnAddDateLabel.setText("Date Added: " + dateAddedStr);

        Region spacer = new Region();
        spacer.setMinWidth(10);

        Hyperlink attachmentHyperlink = createActionLink(IconsHelper.createIcon(ATTACHMENT));
        Hyperlink commentsHyperlink = createActionLink(IconsHelper.createIcon(COMMENTS));

        fqnDateAddedTextFlow.getChildren().setAll(fqnAddDateLabel, spacer, attachmentHyperlink, commentsHyperlink);
    }

    /**
     * Creates a hyperlink with the provided SVG icon.
     * Applies consistent styling to the icon for use in action links.
     *
     * @param icon The SVG icon to use in the hyperlink
     * @return A configured Hyperlink with the icon as its graphic
     */
    private Hyperlink createActionLink(SVGPath icon) {
        Hyperlink hyperlink = new Hyperlink();
        icon.getStyleClass().add("descr-concept-icon");
        hyperlink.setGraphic(icon);
        return hyperlink;
    }

    /**
     * Returns a list of description semantics. This currently returns two specific semantics
     * Case significance & Language preferred. E.g. (Case-sensitive | English)
     * @return Map<Integer, List<String>> Map of nids to a List of strings containing field's values.
     */
    private Map<SemanticEntityVersion, List<String>> latestDescriptionSemantics(EntityFacade conceptFacade) {
        Map<SemanticEntityVersion, List<String>> descriptionSemanticsMap = new HashMap<>();

        // FQN - English | Case Sensitive
        // REG - English | Case Sensitive

        //Get latest description semantic version of the passed in concept (entityfacade)
        //Latest<SemanticEntityVersion> latestDescriptionSemanticVersion = viewCalculator.getDescription(conceptFacade);

        //There should always be one FQN
        //There can be 0 or more Regular Names
        //Loop through, conditionally sort semantics by their description type concept object
        //Update UI via the descriptionRegularName function on the
        // TODO: This should rely on the view calculator from the parent view properties. Below will always get the actual latest semantic version
        //        ViewCalculator viewCalculator = getViewProperties().calculator(); /* after import this is not getting latest */

        ViewCalculator viewCalculator = conceptViewModel.getViewProperties().calculator(); /* returns committed latest from db */
        viewCalculator.getDescriptionsForComponent(conceptFacade).stream()
                .filter(semanticEntity -> {
                    // TODO FIXME - the latest() methods should be relative to the
                    //              concept navigator's view coordinates based on stamp (date time).
                    //              This will always return the latest record from the database not the
                    //              latest from the view coordinate position data time range.
                    ObservableSemantic observableSemantic = ObservableEntity.get(semanticEntity.nid());
                    ObservableSemanticSnapshot observableSemanticSnapshot = observableSemantic.getSnapshot(viewCalculator);
                    Latest<SemanticEntityVersion>  semanticVersion =  retrieveCommittedLatestVersion(observableSemanticSnapshot);

                    // Filter (include) semantics where they contain descr type having FQN, Regular name, Definition Descr.
                    EntityFacade descriptionTypeConceptValue = getFieldValueByMeaning(semanticVersion.get(), TinkarTerm.DESCRIPTION_TYPE);
                    if(descriptionTypeConceptValue instanceof EntityFacade descriptionTypeConcept ){
                        int typeId = descriptionTypeConcept.nid();
                        return (typeId == FULLY_QUALIFIED_NAME_DESCRIPTION_TYPE.nid() ||
                                typeId == REGULAR_NAME_DESCRIPTION_TYPE.nid() ||
                                typeId == DEFINITION_DESCRIPTION_TYPE.nid());
                    }
                    return false;

                }).forEach(semanticEntity -> {
                    // Each description obtain the latest semantic version, pattern version and their field values based on index

                    // TODO FIXME - the latest() methods should be relative to the
                    //              concept navigator's view coordinates based on stamp (date time).
                    //              This will always return the latest record from the database not the
                    //              latest from the view coordinate position data time range.

                    ObservableSemantic observableSemantic = ObservableEntity.get(semanticEntity.nid());
                    ObservableSemanticSnapshot observableSemanticSnapshot = observableSemantic.getSnapshot(viewCalculator);
                    Latest<SemanticEntityVersion>  semanticVersion =  retrieveCommittedLatestVersion(observableSemanticSnapshot);

                    PatternEntity<PatternEntityVersion> patternEntity = semanticEntity.pattern();
                    PatternEntityVersion patternEntityVersion = viewCalculator.latest(patternEntity).get();

                    int indexCaseSig = patternEntityVersion.indexForMeaning(DESCRIPTION_CASE_SIGNIFICANCE);
                    int indexLang = patternEntityVersion.indexForMeaning(LANGUAGE_CONCEPT_NID_FOR_DESCRIPTION);

                    List<String> descrFields = new ArrayList<>();
                    descriptionSemanticsMap.put(semanticVersion.get(), descrFields);
                    Object caseSigConcept = semanticVersion.get().fieldValues().get(indexCaseSig);
                    Object langConcept = semanticVersion.get().fieldValues().get(indexLang);

                    // e.g. FQN - English | Case Sensitive
                    String casSigText = viewCalculator.getRegularDescriptionText(((EntityFacade) caseSigConcept).nid())
                            .orElse(String.valueOf(((EntityFacade) caseSigConcept).nid()));
                    String langText = viewCalculator.getRegularDescriptionText(((EntityFacade) langConcept).nid())
                            .orElse(String.valueOf(((EntityFacade) langConcept).nid()));

                    descrFields.add(casSigText);
                    descrFields.add(langText);
                });
        return descriptionSemanticsMap;

    }

    /**
     * Returns a list of fields with their values (FieldRecord) based on the latest pattern (field definitions).
     * @param semanticEntityVersion - the latest semantic version
     * @param patternVersion - the latest pattern version
     * @return a list of fields with their values (FieldRecord) based on the latest pattern (field definitions).
     */
    private static ImmutableList<ObservableField> fields(SemanticEntityVersion semanticEntityVersion, PatternEntityVersion patternVersion) {

        ObservableField[] fieldArray = new ObservableField[semanticEntityVersion.fieldValues().size()];
        for (int indexInPattern = 0; indexInPattern < fieldArray.length; indexInPattern++) {
            Object value = semanticEntityVersion.fieldValues().get(indexInPattern);
            FieldDefinitionForEntity fieldDef = patternVersion.fieldDefinitions().get(indexInPattern);
            FieldDefinitionRecord fieldDefinitionRecord = new FieldDefinitionRecord(fieldDef.dataTypeNid(),
                    fieldDef.purposeNid(), fieldDef.meaningNid(), patternVersion.stampNid(), patternVersion.nid(), indexInPattern);
            fieldArray[indexInPattern] = new ObservableField(new FieldRecord(value, semanticEntityVersion.nid(), semanticEntityVersion.stampNid(), fieldDefinitionRecord));
        }
        return Lists.immutable.of(fieldArray);
    }

    /**
     * This will update the EL++ inferred and stated terminological axioms
     */
    private void updateAxioms() {

        // do not update ui should be blank
        if (getConceptViewModel().getPropertyValue(MODE) == CREATE) {
            return;
        }

        // clear Axioms areas
        ViewCalculator viewCalculator = conceptViewModel.getViewProperties().calculator();
        EntityFacade entityFacade = conceptViewModel.getPropertyValue(CURRENT_ENTITY);

        // add axiom pencil
        addAxiomButton.setVisible(entityFacade == null); // In view mode you can't add a sufficient/necc set

        // Create a SheetItem (AXIOM inferred semantic version)
        // TODO Should this be reused instead of instanciating a new one everytime?
        KometPropertySheet inferredPropertySheet = new KometPropertySheet(conceptViewModel.getViewProperties(), true);
        Latest<SemanticEntityVersion> inferredSemanticVersion = viewCalculator.getInferredAxiomSemanticForEntity(entityFacade.nid());
        makeSheetItem(conceptViewModel.getViewProperties(), inferredPropertySheet, inferredSemanticVersion);
        inferredAxiomPane.setCenter(inferredPropertySheet);


        // Create a SheetItem (AXIOM stated semantic version)
        KometPropertySheet statedPropertySheet = new KometPropertySheet(conceptViewModel.getViewProperties(), true);
        Latest<SemanticEntityVersion> statedSemanticVersion    = viewCalculator.getStatedAxiomSemanticForEntity(entityFacade.nid());
        makeSheetItem(conceptViewModel.getViewProperties(), statedPropertySheet, statedSemanticVersion);
        statedAxiomPane.setCenter(statedPropertySheet);

        //TODO discuss the blue theme color related to AXIOMs

    }

    private void makeSheetItem(ViewProperties viewProperties,
                               KometPropertySheet propertySheet,
                               Latest<SemanticEntityVersion> semanticVersion) {
        semanticVersion.ifPresent(semanticEntityVersion -> {
            Latest<PatternEntityVersion> statedPatternVersion = conceptViewModel.getViewProperties().calculator().latestPatternEntityVersion(semanticEntityVersion.pattern());
            ImmutableList<ObservableField> fields = fields(semanticEntityVersion, statedPatternVersion.get());
            fields.forEach(field ->
                    // create a row as a label: editor. For Axioms we hide the left labels.
                    propertySheet.getItems().add(SheetItem.make(field, semanticEntityVersion, conceptViewModel.getViewProperties())));
        });

    }

    public void clearView() {
        definitionTextField.clear();
        identifierControl.setPublicId("");
        lastUpdatedLabel.setText("");
        fqnAddDateLabel.setText("");
        moduleLabel.setText("");
        pathLabel.setText("");
        statusLabel.setText("");
        authorTooltip.setText("");
        notAvailInferredAxiomLabel.setVisible(true);
        notAvailStatedAxiomLabel.setVisible(true);
        fqnContainer.getChildren().clear();
        fqnDateAddedTextFlow.getChildren().clear();
        otherNamesNodeListControl.getItems().clear();
    }
    @FXML
    private void displayEditConceptView(ActionEvent event) {
        event.consume();
        LOG.info(event.toString());
    }
    public void displayEditConceptView(ViewProperties viewProperties, KometPreferences nodePreferences, EntityFacade entityFacade){

    }

    public HBox getConceptHeaderControlToolBarHbox() {
        return conceptHeaderControlToolBarHbox;
    }

    @FXML
    private void onMousePressedOnStamp(MouseEvent mouseEvent) {
        stampSelected.setValue(!stampSelected.get());
    }

    private void onStampSelectionChanged() {
        boolean isSelected = stampSelected.get();

        stampContainer.pseudoClassStateChanged(STAMP_SELECTED, isSelected);

        if (isSelected) {
            eventBus.publish(conceptTopic, new AddStampEvent(stampContainer,
                    AddStampEvent.ANY));
            if (!propertiesToggleButton.isSelected()) {
                propertiesToggleButton.fire();
            }


        } else {
            eventBus.publish(conceptTopic, new ClosePropertiesPanelEvent(stampContainer, CLOSE_PROPERTIES));
        }
    }

    @FXML
    private void openPropertiesPanel(ActionEvent event) {
        ToggleButton propertyToggle = (ToggleButton) event.getSource();
        // if selected open properties
        if (propertyToggle.isSelected()) {
            LOG.info("Opening slideout of properties");
            slideOut(propertiesSlideoutTrayPane, detailsOuterBorderPane);

            updateDraggableNodesForPropertiesPanel(true);

            if (CREATE.equals(conceptViewModel.getPropertyValue(MODE))) {
                // show the Add FQN
                eventBus.publish(conceptTopic, new AddFullyQualifiedNameEvent(propertyToggle,
                        AddFullyQualifiedNameEvent.ADD_FQN, conceptViewModel.getViewProperties()));
            } else if (EDIT.equals(conceptViewModel.getPropertyValue(MODE))){
                // show the button form
                eventBus.publish(conceptTopic, new OpenPropertiesPanelEvent(propertyToggle,
                        OpenPropertiesPanelEvent.OPEN_PROPERTIES_PANEL, fqnPublicId, fqnTitleText.getText()));
            }
        } else {
            LOG.info("Close Properties slideout");
            slideIn(propertiesSlideoutTrayPane, detailsOuterBorderPane);

            updateDraggableNodesForPropertiesPanel(false);
        }
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

    @FXML
    private void openTimelinePanel(ActionEvent event) {
        ToggleButton timelineToggle = (ToggleButton) event.getSource();
        // if selected open properties
        if (timelineToggle.isSelected()) {
            LOG.info("Opening slideout of properties");
            slideOut(timelineSlideoutTrayPane, detailsOuterBorderPane);
        } else {
            LOG.info("Close Properties slideout");
            slideIn(timelineSlideoutTrayPane, detailsOuterBorderPane);
        }
    }

    @FXML
    private void runFullReasoner(ActionEvent actionEvent) {
        LOG.info("Run full reaonser");
    }

    @FXML
    private void runIncrementalReasoner(ActionEvent actionEvent) {
        LOG.info("Run incremental reasoner");
    }

    @FXML
    private void redoNavigation(ActionEvent actionEvent) {
        LOG.info("Redo navigation");
    }

    /**
     * When user selects the STAMP edit button to pop up the options to edit.
     * @param event
     */
    @FXML
    public void popupStampEdit(ActionEvent event) {
        if (stampEdit !=null && stampEditController != null) {
            StampViewModel stampViewModel = getConceptViewModel().getPropertyValue(CONCEPT_STAMP_VIEW_MODEL);

            // refresh modules
            stampViewModel.getObservableList(MODULES_PROPERTY).clear();
            stampViewModel.getObservableList(MODULES_PROPERTY).addAll(DataModelHelper.fetchDescendentsOfConcept(conceptViewModel.getViewProperties(), TinkarTerm.MODULE.publicId()));

            // refresh path
            stampViewModel.getObservableList(PATHS_PROPERTY).clear();
            stampViewModel.getObservableList(PATHS_PROPERTY).addAll(DataModelHelper.fetchDescendentsOfConcept(conceptViewModel.getViewProperties(), TinkarTerm.PATH.publicId()));

            stampEdit.show((Node) event.getSource());
            stampEditController.selectActiveStatusToggle();
            return;
        }
        // Prefetch modules and paths for view to populate radio buttons in form.
        StampViewModel stampViewModel = new StampViewModel();

        // Populate from database
        stampViewModel.setPropertyValue(PATHS_PROPERTY, DataModelHelper.fetchDescendentsOfConcept(conceptViewModel.getViewProperties(), TinkarTerm.PATH.publicId()))
                .setPropertyValue(MODULES_PROPERTY, DataModelHelper.fetchDescendentsOfConcept(conceptViewModel.getViewProperties(), TinkarTerm.MODULE.publicId()));

        // setup mode
        if (getConceptViewModel().getPropertyValue(CURRENT_ENTITY) != null) {
            stampViewModel.setPropertyValue(MODE, EDIT);
        } else {
            stampViewModel.setPropertyValue(MODE, CREATE);
        }

        // IMPORTANT: Must set inside of concept view model
        getConceptViewModel().setPropertyValue(CONCEPT_STAMP_VIEW_MODEL, stampViewModel);

        // Inject Stamp view model into form.
        Config stampConfig = new Config(StampEditController.class.getResource(EDIT_STAMP_OPTIONS_FXML))
                .addNamedViewModel(new NamedVm("stampViewModel", stampViewModel));
        JFXNode<Pane, StampEditController> stampJFXNode = FXMLMvvmLoader.make(stampConfig);

        Pane editStampPane = stampJFXNode.node();
        PopOver popOver = new PopOver(editStampPane);
        popOver.getStyleClass().add("filter-menu-popup");
        StampEditController stampEditController = stampJFXNode.controller();

        stampEditController.updateModel(conceptViewModel.getViewProperties());

        // default the status=Active, disable inactive
        stampEditController.selectActiveStatusToggle();

        popOver.setOnHidden(windowEvent -> {
            // set Stamp info into Details form
            getStampViewModel().save();
            updateUIStamp(getStampViewModel());
        });

        popOver.show((Node) event.getSource());

        // store and use later.
        stampEdit = popOver;
        this.stampEditController = stampEditController;
    }

    @FXML
    private void popupAddContextMenu(ActionEvent actionEvent) {
        MenuHelper.fireContextMenuEvent(actionEvent, Side.BOTTOM, 0, 0);
    }

    private void updateUIStamp(ViewModel stampViewModel) {
        long time = stampViewModel.getValue(TIME);
        DateTimeFormatter DATE_TIME_FORMATTER = dateFormatter("yyyy-MMM-dd HH:mm:ss");
        Instant stampInstance = Instant.ofEpochSecond(time/1000);
        String lastUpdated = DATE_TIME_FORMATTER.format(stampInstance);

        lastUpdatedLabel.setText(lastUpdated);
        ConceptEntity moduleEntity = stampViewModel.getValue(MODULE);
        if (moduleEntity == null) {
            LOG.warn("Must select a valid module for Stamp.");
            return;
        }
        String moduleDescr = conceptViewModel.getViewProperties().calculator().getPreferredDescriptionTextWithFallbackOrNid(moduleEntity.nid());
        moduleLabel.setText(moduleDescr);
        ConceptEntity pathEntity = stampViewModel.getValue(PATH);
        String pathDescr = conceptViewModel.getViewProperties().calculator().getPreferredDescriptionTextWithFallbackOrNid(pathEntity.nid());
        pathLabel.setText(pathDescr);
        State status = stampViewModel.getValue(STATUS);
        String statusMsg = status == null ? "Active" : conceptViewModel.getViewProperties().calculator().getPreferredDescriptionTextWithFallbackOrNid(status.nid());
        statusLabel.setText(statusMsg);
    }

    public void compactSizeWindow() {
        descriptionsTitledPane.setExpanded(false);
        axiomsTitledPane.setExpanded(false);
    }

    public void setConceptTopic(UUID conceptTopic) {
        this.conceptTopic = conceptTopic;
    }

    @FXML
    private void showChangeViewCoordinateMenu(ActionEvent actionEvent) {
        MenuHelper.fireContextMenuEvent(actionEvent, Side.BOTTOM, 0, 0);
    }


    /**
     * generate the classic Komet coordinate menu
     */
    public void setUpEditCoordinateMenu() {
        this.viewMenuModel = new ViewMenuModel(conceptViewModel.getViewProperties(), coordinatesMenuButton, "DetailsController");
    }

    private DateTimeFormatter dateFormatter(String formatString) {
        return DateTimeFormatter
                .ofPattern(formatString)
                .withLocale(Locale.US)
                .withZone(ZoneId.of("UTC"));
    }

    private int getFieldIndexByMeaning(SemanticEntityVersion entityVersion, EntityFacade ...meaning) {
        PatternEntity<PatternEntityVersion> patternEntity = entityVersion.entity().pattern();
        PatternEntityVersion patternEntityVersion = conceptViewModel.getViewProperties().calculator().latest(patternEntity).get();
        int index = -1;
        if (meaning != null && meaning.length > 0){
            for (int i=0; i < meaning.length; i++){
                index = patternEntityVersion.indexForMeaning(meaning[i].nid());
                if (index != -1){
                    break;
                }
            }
        }
        return  index;
    }

    private <T> T getFieldValueByMeaning(SemanticEntityVersion entityVersion, EntityFacade ...meaning) {
        int index = getFieldIndexByMeaning(entityVersion, meaning);
        if (index == -1) {
            return null;
        }
        return (T) entityVersion.fieldValues().get(index);
    }

    private <T> T getFieldValueByPurpose(SemanticEntityVersion entityVersion, EntityFacade ...purpose) {
        PatternEntity<PatternEntityVersion> patternEntity = entityVersion.entity().pattern();
        PatternEntityVersion patternEntityVersion = conceptViewModel.getViewProperties().calculator().latest(patternEntity).get();
        int index = -1;
        if (purpose != null && purpose.length > 0){
            for (int i=0; i < purpose.length; i++){
                index = patternEntityVersion.indexForPurpose(purpose[i].nid());
                if (index != -1){
                    break;
                }
            }
        }

        if (index == -1){
            return null;
        }
        return (T) entityVersion.fieldValues().get(index);
    }

    /**
     * Checks whether the properties panel is currently open (slid out).
     * <p>
     * This method determines the open state by checking if the properties
     * slideout tray pane is visible and expanded.
     *
     * @return {@code true} if the properties panel is open and visible,
     *         {@code false} if it is closed or hidden
     */
    public boolean isPropertiesPanelOpen() {
        return SlideOutTrayHelper.isOpen(propertiesSlideoutTrayPane);
    }

    /**
     * Sets the open/closed state of the properties panel programmatically.
     * <p>
     * The animation is performed without transitions when called programmatically
     * to ensure immediate state changes.
     *
     * @param isOpen {@code true} to open (slide out) the properties panel,
     *               {@code false} to close (slide in) the panel
     */
    public void setPropertiesPanelOpen(boolean isOpen) {
        propertiesToggleButton.setSelected(isOpen);

        if (isOpen) {
            SlideOutTrayHelper.slideOut(propertiesSlideoutTrayPane, detailsOuterBorderPane, false);
        } else {
            SlideOutTrayHelper.slideIn(propertiesSlideoutTrayPane, detailsOuterBorderPane, false);
        }

        updateDraggableNodesForPropertiesPanel(isOpen);
    }
}
