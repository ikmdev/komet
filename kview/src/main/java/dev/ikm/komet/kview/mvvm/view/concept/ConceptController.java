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
package dev.ikm.komet.kview.mvvm.view.concept;

import dev.ikm.komet.framework.Identicon;
import dev.ikm.komet.framework.controls.TimeUtils;
import dev.ikm.komet.framework.events.appevents.RefreshCalculatorCacheEvent;
import dev.ikm.komet.framework.observable.*;
import dev.ikm.komet.framework.propsheet.KometPropertySheet;
import dev.ikm.komet.framework.propsheet.SheetItem;
import dev.ikm.komet.framework.view.ViewMenuModel;
import dev.ikm.komet.framework.view.ViewProperties;
import dev.ikm.komet.kview.controls.KLExpandableNodeListControl;
import dev.ikm.komet.kview.controls.PublicIDListControl;
import dev.ikm.komet.kview.controls.StampViewControl;
import dev.ikm.komet.kview.events.*;
import dev.ikm.komet.kview.events.genediting.GenEditingEvent;
import dev.ikm.komet.kview.fxutils.IconsHelper;
import dev.ikm.komet.kview.fxutils.MenuHelper;
import dev.ikm.komet.kview.fxutils.SlideOutTrayHelper;
import dev.ikm.komet.kview.mvvm.model.DescrName;
import dev.ikm.komet.kview.mvvm.view.journal.VerticallyFilledPane;
import dev.ikm.komet.kview.mvvm.view.properties.PropertiesController;
import dev.ikm.komet.kview.mvvm.viewmodel.ConceptViewModel;
import dev.ikm.komet.kview.mvvm.viewmodel.stamp.StampFormViewModelBase;
import dev.ikm.komet.preferences.KometPreferences;
import dev.ikm.tinkar.common.id.PublicId;
import dev.ikm.tinkar.coordinate.stamp.calculator.Latest;
import dev.ikm.tinkar.coordinate.view.calculator.ViewCalculator;
import dev.ikm.tinkar.coordinate.view.calculator.ViewCalculatorWithCache;
import dev.ikm.tinkar.entity.*;
import dev.ikm.tinkar.events.AxiomChangeEvent;
import dev.ikm.tinkar.events.EvtBus;
import dev.ikm.tinkar.events.EvtBusFactory;
import dev.ikm.tinkar.events.Subscriber;
import dev.ikm.tinkar.terms.*;
import javafx.application.Platform;
import javafx.beans.InvalidationListener;
import javafx.collections.ObservableList;
import javafx.css.PseudoClass;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.fxml.FXML;
import javafx.geometry.Side;
import javafx.scene.Node;
import javafx.scene.control.*;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.input.ScrollEvent;
import javafx.scene.layout.*;
import javafx.scene.shape.SVGPath;
import javafx.scene.text.Text;
import javafx.scene.text.TextFlow;
import org.carlfx.cognitive.loader.InjectViewModel;
import org.carlfx.cognitive.viewmodel.ValidationViewModel;
import org.eclipse.collections.api.factory.Lists;
import org.eclipse.collections.api.list.ImmutableList;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.time.Instant;
import java.time.LocalDate;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.time.format.FormatStyle;
import java.util.*;
import java.util.function.Consumer;

import static dev.ikm.komet.kview.events.ClosePropertiesPanelEvent.CLOSE_PROPERTIES;
import static dev.ikm.komet.kview.fxutils.IconsHelper.IconType.ATTACHMENT;
import static dev.ikm.komet.kview.fxutils.IconsHelper.IconType.COMMENTS;
import static dev.ikm.komet.kview.fxutils.MenuHelper.fireContextMenuEvent;
import static dev.ikm.komet.kview.fxutils.SlideOutTrayHelper.*;
import static dev.ikm.komet.kview.fxutils.ViewportHelper.clipChildren;
import static dev.ikm.komet.kview.fxutils.window.DraggableSupport.addDraggableNodes;
import static dev.ikm.komet.kview.fxutils.window.DraggableSupport.removeDraggableNodes;
import static dev.ikm.komet.kview.mvvm.model.DataModelHelper.*;
import static dev.ikm.komet.kview.mvvm.viewmodel.ConceptViewModel.*;
import static dev.ikm.komet.kview.mvvm.viewmodel.FormViewModel.MODE;
import static dev.ikm.komet.kview.mvvm.viewmodel.stamp.StampFormViewModelBase.Properties.FORM_TIME_TEXT;
import static dev.ikm.komet.kview.mvvm.viewmodel.stamp.StampFormViewModelBase.Properties.IS_CONFIRMED_OR_SUBMITTED;
import static dev.ikm.tinkar.common.service.PrimitiveData.PREMUNDANE_TIME;
import static dev.ikm.tinkar.common.util.time.DateTimeUtil.PREMUNDANE;
import static dev.ikm.tinkar.coordinate.stamp.StampFields.*;
import static dev.ikm.tinkar.coordinate.stamp.StampFields.MODULE;
import static dev.ikm.tinkar.coordinate.stamp.StampFields.PATH;
import static dev.ikm.tinkar.events.FrameworkTopics.CALCULATOR_CACHE_TOPIC;
import static dev.ikm.tinkar.events.FrameworkTopics.RULES_TOPIC;
import static dev.ikm.tinkar.terms.TinkarTerm.*;

public class ConceptController {

    private static final PseudoClass STAMP_SELECTED = PseudoClass.getPseudoClass("selected");

    private static final Logger LOG = LoggerFactory.getLogger(ConceptController.class);

    private static final String EDIT_STAMP_OPTIONS_FXML = "stamp-edit.fxml";

    private static final String NO_VERSION_PRESENT_TEXT = "No version present";

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

    @FXML
    private BorderPane detailsInnerBorderPane;

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
    private Label definitionTextField;

    @FXML
    private PublicIDListControl identifierControl;

    @FXML
    private StampViewControl stampViewControl;

    @FXML
    private ScrollPane conceptContentScrollPane;

    ///// Descriptions Section /////////////////////////////////
    @FXML
    private TitledPane descriptionsTitledPane;

    @FXML
    private Button editConceptButton;

    @FXML
    private Text fullyQualifiedNameHeaderText;

    /**
     * Responsible for holding rows of other names (regular) description semantics.
     */
    @FXML
    private KLExpandableNodeListControl fullyQualifiedNameNodeListControl;

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
     *  TODO: View Calculator will need to be refreshed.
     */
    private ViewCalculatorWithCache viewCalculatorWithCache;

    private boolean isUpdatingStampSelection = false;

    public ConceptController() {
    }

    public ConceptController(UUID conceptTopic) {
        this.conceptTopic = conceptTopic;
    }

    @FXML
    public void initialize() {
        stampViewControl.selectedProperty().subscribe(this::onStampSelectionChanged);

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
        ObservableList<DescrName> fullyQualifiedNames = getConceptViewModel().getObservableList(FULLY_QUALIFIED_NAMES);
        fullyQualifiedNames.addListener((InvalidationListener) observable -> {
            if(!fullyQualifiedNames.isEmpty()){
                DescrName fqnDescrName = fullyQualifiedNames.get(0);
                updateConceptBanner();
            }
            updateFullyQualifiedNamesDescription(fullyQualifiedNames);
        });

        ObservableList<DescrName> otherNames = getConceptViewModel().getObservableList(OTHER_NAMES);
        otherNames.addListener((InvalidationListener) obs -> {
            if (!otherNames.isEmpty()) {
                propertiesController.setHasOtherName(true);
            }
            updateOtherNamesDescription(otherNames);
        });

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
                    fullyQualifiedNames.clear();
                    fullyQualifiedNames.add(descrName);
                } else if (evt.getEventType() == CreateConceptEvent.ADD_OTHER_NAME) {
                    otherNames.add(descrName);
                }else if (evt.getEventType() == CreateConceptEvent.EDIT_OTHER_NAME) { // Since we are
                    updateOtherNamesDescription(otherNames);
                }else { // Since we are
                    updateFullyQualifiedNamesDescription(fullyQualifiedNames);
                }
                // Attempts to write data
                boolean isWritten = conceptViewModel.createConcept(propertiesController.getStampFormViewModel());
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
                    fullyQualifiedNames.add(descrName);
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

        conceptViewModel.getViewProperties().nodeView().addListener((obs, oldViewCoord, newViewCoord) -> {
            if (newViewCoord != null) {
                LOG.info("refresh concept window when view coordinate has changed." + newViewCoord);
                updateView();
            }
        });

        conceptViewModel.getProperty(MODE).subscribe(() -> {
            propertiesController.setEditMode(conceptViewModel.getPropertyValue(MODE).equals(EDIT));

            if (conceptViewModel.getPropertyValue(MODE).equals(CREATE)) {
                StampFormViewModelBase stampFormViewModel = propertiesController.getStampFormViewModel();
                stampFormViewModel.getProperty(IS_CONFIRMED_OR_SUBMITTED).subscribe(this::onConfirmStampFormWhenCreating);
            }
        });
    }

    private void onConfirmStampFormWhenCreating() {
        // Update StampViewControl
        StampFormViewModelBase stampFormViewModel = propertiesController.getStampFormViewModel();
        ViewCalculator viewCalculator = conceptViewModel.getViewProperties().calculator();

        // - Status
        State status = stampFormViewModel.getPropertyValue(STATUS);
        String statusText = viewCalculator.getPreferredDescriptionTextWithFallbackOrNid(status.nid());
        stampViewControl.setStatus(statusText);

        // - Module
        EntityFacade module = stampFormViewModel.getPropertyValue(MODULE);
        String moduleText = viewCalculator.getPreferredDescriptionTextWithFallbackOrNid(module.nid());
        stampViewControl.setModule(moduleText);

        // - Author
        EntityFacade author = stampFormViewModel.getPropertyValue(AUTHOR);
        String authorDescription = viewCalculator.getPreferredDescriptionTextWithFallbackOrNid(author.nid());
        stampViewControl.setAuthor(authorDescription);

        // - Path
        EntityFacade path = stampFormViewModel.getPropertyValue(PATH);
        String pathText = viewCalculator.getPreferredDescriptionTextWithFallbackOrNid(path.nid());
        stampViewControl.setPath(pathText);

        // Latest update time
        stampViewControl.setLastUpdated(stampFormViewModel.getPropertyValue(FORM_TIME_TEXT));
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

    private void setUpDescriptionContextMenu(Button addDescriptionButton) {
        ContextMenu contextMenu = buildMenuOptionContextMenu();
        addDescriptionButton.setContextMenu(contextMenu);
        addDescriptionButton.setOnAction(this::onAddDescriptionButtonPressed);
    }

    private void onAddDescriptionButtonPressed(ActionEvent actionEvent) {
        if (this.conceptViewModel.getPropertyValue(MODE).equals(CREATE) &&
                getConceptViewModel().getObservableList(FULLY_QUALIFIED_NAMES).isEmpty()) {
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
                getConceptViewModel().getObservableList(FULLY_QUALIFIED_NAMES).isEmpty()) {
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
            boolean isWritten = conceptViewModel.createConcept(propertiesController.getStampFormViewModel());
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
            boolean isWritten = conceptViewModel.createConcept(propertiesController.getStampFormViewModel());
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

    private Consumer<ConceptController> onCloseConceptWindow;
    public void setOnCloseConceptWindow(Consumer<ConceptController> onClose) {
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
        LOG.info("Closing & cleaning concept window: %s - %s".formatted(identifierControl.getPublicIdList(), fqnTitleText.getText()));
        // unsubscribe listeners
        eventBus.unsubscribe(conceptTopic, EditConceptFullyQualifiedNameEvent.class, editConceptFullyQualifiedNameEventSubscriber);
        eventBus.unsubscribe(conceptTopic, AddFullyQualifiedNameEvent.class, addFullyQualifiedNameEventSubscriber);
        eventBus.unsubscribe(conceptTopic, EditOtherNameConceptEvent.class, editOtherNameConceptEventSubscriber);
        eventBus.unsubscribe(conceptTopic, AddOtherNameToConceptEvent.class, addOtherNameToConceptEventSubscriber);
        eventBus.unsubscribe(conceptTopic, ClosePropertiesPanelEvent.class, closePropertiesPanelEventSubscriber);
        eventBus.unsubscribe(conceptTopic, CreateConceptEvent.class, createConceptEventSubscriber);
        eventBus.unsubscribe(conceptTopic, EditConceptEvent.class, editConceptEventSubscriber);
        eventBus.unsubscribe(RULES_TOPIC, AxiomChangeEvent.class, changeSetTypeEventSubscriber);
        eventBus.unsubscribe(CALCULATOR_CACHE_TOPIC, RefreshCalculatorCacheEvent.class, refreshCalculatorEventSubscriber);
    }
    public Pane getPropertiesSlideoutTrayPane() {
        return propertiesSlideoutTrayPane;
    }

    public void updateView() {
        EntityFacade entityFacade = conceptViewModel.getPropertyValue(CURRENT_ENTITY);
        if (entityFacade != null) { // edit concept
            getConceptViewModel().setPropertyValue(MODE, EDIT);
        } else { // create concept
            getConceptViewModel().setPropertyValue(MODE, CREATE);
        }

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

        final ViewCalculator viewCalculator = conceptViewModel.getViewProperties().calculator();

        // check to see if the latest version exists
        viewCalculator.latest(entityFacade).ifPresentOrElse(
                entityVersion -> {

                    // Title (FQN of concept)
            String conceptNameStr = viewCalculator.languageCalculator().getDescriptionTextOrNid(entityFacade.nid());
            fqnTitleText.setText(conceptNameStr);
            conceptNameTooltip.setText(conceptNameStr);

            // Definition description text
            viewCalculator
                    .languageCalculator()
                    .getDefinitionDescriptionText(entityFacade)
                    .ifPresentOrElse(definition ->
                            definitionTextField.setText(definition),
                            () -> definitionTextField.setText(""));

            updateDisplayIdentifier(viewCalculator, (ConceptFacade) entityFacade);

            // Identicon
            Image identicon = Identicon.generateIdenticonImage(entityFacade.publicId());
            identiconImageView.setImage(identicon);

            // Obtain STAMP info
            StampEntity stamp = entityVersion.stamp();

            // Status
            String statusText = viewCalculator.getDescriptionTextOrNid(stamp.stateNid());
            stampViewControl.setStatus(statusText);

            // Module
            String moduleText = viewCalculator.getDescriptionTextOrNid(stamp.moduleNid());
            stampViewControl.setModule(moduleText);

            // Author
            String authorDescription = viewCalculator.getDescriptionTextOrNid(stamp.authorNid());
            stampViewControl.setAuthor(authorDescription);

            // Path
            String pathText = viewCalculator.getDescriptionTextOrNid(stamp.pathNid());
            stampViewControl.setPath(pathText);

            // Latest update time
            long stampTime = stamp.time();
            stampViewControl.setLastUpdated(TimeUtils.toDateString(stampTime));
        },
        // else no value present
        () -> {
            stampViewControl.setStatus(NO_VERSION_PRESENT_TEXT);
            stampViewControl.setModule(NO_VERSION_PRESENT_TEXT);
            stampViewControl.setAuthor(NO_VERSION_PRESENT_TEXT);
            stampViewControl.setPath(NO_VERSION_PRESENT_TEXT);
            stampViewControl.setLastUpdated(NO_VERSION_PRESENT_TEXT);
            fqnTitleText.setText(NO_VERSION_PRESENT_TEXT);
        });
    }

    /// Show the public IDs
    private void updateDisplayIdentifier(ViewCalculator viewCalculator, ConceptFacade conceptFacade) {
        identifierControl.updatePublicIdList(viewCalculator, conceptFacade);
    }

    public void updateFullyQualifiedNamesDescription(List<DescrName> descrNameViewModels) {
        fullyQualifiedNameNodeListControl.getItems().clear();
        descrNameViewModels.forEach(fullyQualifedName -> {
            // start adding a row
            VBox fullyQualifiedNameVBox = generateDescriptionSemanticRow(fullyQualifedName);
            TextFlow firstRow = (TextFlow) fullyQualifiedNameVBox.getChildren().getFirst();
            firstRow.setOnMouseClicked(event -> eventBus.publish(conceptTopic,
                    new EditConceptFullyQualifiedNameEvent(fullyQualifiedNameVBox,
                            EditConceptFullyQualifiedNameEvent.EDIT_FQN, fullyQualifedName)));
            fullyQualifiedNameNodeListControl.getItems().add(fullyQualifiedNameVBox);
        });
    }

    public void updateOtherNamesDescription(List<DescrName> descrNameViewModels) {
        otherNamesNodeListControl.getItems().clear();
        descrNameViewModels.forEach(otherName -> {
                    // start adding a row
                    VBox otherNameBox = generateDescriptionSemanticRow(otherName);
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

        viewCalculator.latest(entityFacade).ifPresentOrElse(
            _ -> {
            // populate UI with FQN and other names. e.g. Hello Solor (English | Case-insensitive)
            Map<SemanticEntityVersion, List<String>> descriptionSemanticsMap = latestDescriptionSemantics(entityFacade);
            otherNamesNodeListControl.getItems().clear();
            fullyQualifiedNameNodeListControl.getItems().clear();

            //Obtain the index field of DESCRIPTION_TYPE
            PatternEntityVersion patternEntityVersion = (PatternEntityVersion)viewCalculator.latest(DESCRIPTION_PATTERN.nid()).get();
            int descriptionTypeIndex = patternEntityVersion.indexForMeaning(DESCRIPTION_TYPE.nid());

            descriptionSemanticsMap.forEach((semanticEntityVersion, fieldDescriptions) -> {
                EntityFacade fieldTypeValue = (EntityFacade) semanticEntityVersion.fieldValues().get(descriptionTypeIndex);
                boolean isFQN = FULLY_QUALIFIED_NAME_DESCRIPTION_TYPE.nid() == fieldTypeValue.nid();
                boolean isOtherName = REGULAR_NAME_DESCRIPTION_TYPE.nid() == fieldTypeValue.nid();

                if (isFQN) {
                    // Latest FQN
                    //   updateFQNSemantics(semanticEntityVersion, fieldDescriptions);
                    VBox fullyQualifiedNameBox = generateDescriptionSemanticRow(semanticEntityVersion, fieldDescriptions);
                    PublicId fullyQuallifiedNamePublicId = (PublicId) fullyQualifiedNameBox.getChildren().getFirst().getUserData();
                    TextFlow row = (TextFlow) fullyQualifiedNameBox.getChildren().getFirst();
                    row.setOnMouseClicked(event -> eventBus.publish(conceptTopic,
                            new EditConceptFullyQualifiedNameEvent(fullyQualifiedNameBox,
                                    EditConceptFullyQualifiedNameEvent.EDIT_FQN, fullyQuallifiedNamePublicId)));
                    fullyQualifiedNameNodeListControl.getItems().add(fullyQualifiedNameBox);
                    LOG.debug("FQN Name = " + semanticEntityVersion + " " + fieldDescriptions);
                } else if (isOtherName) {
                    // start adding a row
                    VBox otherNameBox = generateDescriptionSemanticRow(semanticEntityVersion, fieldDescriptions);
                    PublicId otherNamePublicId = (PublicId) otherNameBox.getChildren().getFirst().getUserData();
                    TextFlow firstRow = (TextFlow) otherNameBox.getChildren().getFirst();
                    firstRow.setOnMouseClicked(event -> eventBus.publish(conceptTopic,
                            new EditOtherNameConceptEvent(otherNameBox,
                                    EditOtherNameConceptEvent.EDIT_OTHER_NAME, otherNamePublicId)));
                    otherNamesNodeListControl.getItems().add(otherNameBox);

                    LOG.debug("Other Names = " + semanticEntityVersion + " " + fieldDescriptions);
                }
            });

            final int fullyQualifiedNameCount = fullyQualifiedNameNodeListControl.getItems().size();
            fullyQualifiedNameHeaderText.setText(fullyQualifiedNameCount > 0 ?
                    String.format("FULLY QUALIFIED NAMES (%d):", fullyQualifiedNameCount) : "FULLY QUALIFIED NAMES:");

            final int otherNamesCount = otherNamesNodeListControl.getItems().size();
            otherNamesHeaderText.setText(otherNamesCount > 0 ?
                    String.format("OTHER NAMES (%d):", otherNamesCount) : "OTHER NAMES:");
        },
        // else no value present
        () -> {
            //FIXME
            System.out.println("no fqn or ot value???");
        });

    }

    /**
     * Returns a list of TextFlow objects as rows of text items allowing the window to allow text
     * to be responsive when the gets width smaller.
     * @param semanticEntityVersion
     * @param fieldDescriptions
     * @return
     */
    private VBox generateDescriptionSemanticRow(SemanticEntityVersion semanticEntityVersion, List<String> fieldDescriptions) {
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

    private VBox generateDescriptionSemanticRow(DescrName otherName) {
        VBox textFlowsBox = new VBox();
        ViewCalculator viewCalculator = conceptViewModel.getViewProperties().calculator();
        ConceptEntity caseSigConcept = otherName.getCaseSignificance();
        String casSigText = viewCalculator.languageCalculator().getDescriptionTextOrNid(caseSigConcept.nid());
        ConceptEntity langConcept = otherName.getLanguage();

        String langText = viewCalculator.languageCalculator().getDescriptionTextOrNid(langConcept.nid());

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

                    Latest<SemanticEntityVersion> semanticEntityVersionLatest = conceptViewModel.getViewProperties().calculator().latest(semanticEntity.nid());
                    if (semanticEntityVersionLatest.isAbsent()) {
                        return false; // No version found
                    }

                    SemanticEntityVersion latestVersion = semanticEntityVersionLatest.get();

                    if (!latestVersion.uncommitted()) {
                        // Latest version is committed
                        return true;
                    }

                    // Latest is uncommitted, search for latest committed version in history
                    ImmutableList<EntityVersion> entityVersionsList = Entity.getFast(semanticEntity.nid()).versions();

                    // Return true if any committed version exists
                    return entityVersionsList.stream()
                            .anyMatch(p -> !p.uncommitted());

                }).forEach(semanticEntity -> {
                    // Each description obtain the latest semantic version, pattern version and their field values based on index

                    // TODO FIXME - the latest() methods should be relative to the
                    //              concept navigator's view coordinates based on stamp (date time).
                    //              This will always return the latest record from the database not the
                    //              latest from the view coordinate position data time range.

                    Latest<SemanticEntityVersion> semanticEntityVersionLatest = conceptViewModel.getViewProperties().calculator().latest(semanticEntity.nid());
                    if(semanticEntityVersionLatest.isAbsent()) {
                        return;
                    }
                    // Filter (include) semantics where they contain descr type having FQN, Regular name, Definition Descr.
                    EntityFacade descriptionTypeConceptValue = getFieldValueByMeaning(semanticEntityVersionLatest.get(), TinkarTerm.DESCRIPTION_TYPE);

                    PatternEntity<PatternEntityVersion> patternEntity = semanticEntity.pattern();
                    PatternEntityVersion patternEntityVersion = viewCalculator.latest(patternEntity).get();

                    int indexCaseSig = patternEntityVersion.indexForMeaning(DESCRIPTION_CASE_SIGNIFICANCE);
                    int indexLang = patternEntityVersion.indexForMeaning(LANGUAGE_CONCEPT_NID_FOR_DESCRIPTION);

                    List<String> descrFields = new ArrayList<>();
                    descriptionSemanticsMap.put(semanticEntityVersionLatest.get(), descrFields);
                    Object caseSigConcept = semanticEntityVersionLatest.get().fieldValues().get(indexCaseSig);
                    Object langConcept = semanticEntityVersionLatest.get().fieldValues().get(indexLang);

                    // e.g. FQN - English | Case Sensitive
                    String casSigText = viewCalculator.languageCalculator().getDescriptionTextOrNid(((EntityFacade) caseSigConcept).nid());
                    String langText = viewCalculator.languageCalculator().getDescriptionTextOrNid(((EntityFacade) langConcept).nid());

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
    private static ImmutableList<ObservableField> fields(SemanticEntityVersion semanticEntityVersion, PatternEntityVersion patternVersion, ViewCalculator viewCalculator) {

        ObservableSemantic observableSemantic = (ObservableSemantic) ObservableEntity.get(semanticEntityVersion.entity());
        ObservableSemanticSnapshot observableSemanticSnapshot = observableSemantic.getSnapshot(viewCalculator);
        Latest<ObservableSemanticVersion> latest = observableSemanticSnapshot.getLatestVersion();
        if(latest.isPresent()){
            return latest.get().fields(patternVersion, false);
        } else {
            return Lists.immutable.of(new ObservableField[semanticEntityVersion.fieldValues().size()]);
        }
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

        viewCalculator.latest(entityFacade).ifPresentOrElse(
        entityVersion -> {
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
            Latest<SemanticEntityVersion> statedSemanticVersion = viewCalculator.getStatedAxiomSemanticForEntity(entityFacade.nid());
            makeSheetItem(conceptViewModel.getViewProperties(), statedPropertySheet, statedSemanticVersion);
            statedAxiomPane.setCenter(statedPropertySheet);

            //TODO discuss the blue theme color related to AXIOMs
        },
        // no latest version present
        () -> {
            inferredAxiomPane.setCenter(showNoVersionPresentForAxiom());
            statedAxiomPane.setCenter(showNoVersionPresentForAxiom());
        });
    }

    private Node showNoVersionPresentForAxiom() {
        return new Label(NO_VERSION_PRESENT_TEXT);
    }

    private void makeSheetItem(ViewProperties viewProperties,
                               KometPropertySheet propertySheet,
                               Latest<SemanticEntityVersion> semanticVersion) {
        semanticVersion.ifPresent(semanticEntityVersion -> {
            Latest<PatternEntityVersion> statedPatternVersion = conceptViewModel.getViewProperties().calculator().latestPatternEntityVersion(semanticEntityVersion.pattern());
            ImmutableList<ObservableField> fields = fields(semanticEntityVersion, statedPatternVersion.get(), conceptViewModel.getViewProperties().calculator());
            fields.forEach(field ->
                    // create a row as a label: editor. For Axioms we hide the left labels.
                    propertySheet.getItems().add(SheetItem.make(field, semanticEntityVersion, conceptViewModel.getViewProperties())));
        });

    }

    public void clearView() {
        definitionTextField.setText("");
        identifierControl.setPublicIdList(null);

        stampViewControl.clear();

        notAvailInferredAxiomLabel.setVisible(true);
        notAvailStatedAxiomLabel.setVisible(true);
        fullyQualifiedNameNodeListControl.getItems().clear();
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

    private void onStampSelectionChanged() {
        if (isUpdatingStampSelection) {
            return;
        }

        if (stampViewControl.isSelected()) {
            if (CREATE.equals(conceptViewModel.getPropertyValue(MODE))) {
                eventBus.publish(conceptTopic, new StampEvent(stampViewControl, StampEvent.CREATE_STAMP));
            } else {
                eventBus.publish(conceptTopic, new StampEvent(stampViewControl, StampEvent.ADD_STAMP));
            }

            if (!propertiesToggleButton.isSelected()) {
                propertiesToggleButton.fire();
            }
        } else {
            eventBus.publish(conceptTopic, new ClosePropertiesPanelEvent(stampViewControl, CLOSE_PROPERTIES));
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

            if (CREATE.equals(conceptViewModel.getPropertyValue(MODE)) && !stampViewControl.isSelected()) {
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

        isUpdatingStampSelection = true;
        stampViewControl.setSelected(propertyToggle.isSelected());
        isUpdatingStampSelection = false;
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

    @FXML
    private void popupAddContextMenu(ActionEvent actionEvent) {
        MenuHelper.fireContextMenuEvent(actionEvent, Side.BOTTOM, 0, 0);
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
