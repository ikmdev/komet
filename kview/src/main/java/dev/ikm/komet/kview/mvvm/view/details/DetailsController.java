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

import dev.ikm.komet.framework.Identicon;
import dev.ikm.komet.framework.events.AxiomChangeEvent;
import dev.ikm.komet.framework.events.EvtBus;
import dev.ikm.komet.framework.events.EvtBusFactory;
import dev.ikm.komet.framework.events.Subscriber;
import dev.ikm.komet.framework.observable.ObservableField;
import dev.ikm.komet.framework.propsheet.KometPropertySheet;
import dev.ikm.komet.framework.propsheet.SheetItem;
import dev.ikm.komet.framework.view.ViewProperties;
import dev.ikm.komet.kview.events.*;
import dev.ikm.komet.kview.fxutils.MenuHelper;
import dev.ikm.komet.kview.mvvm.model.DataModelHelper;
import dev.ikm.komet.kview.mvvm.model.DescrName;
import dev.ikm.komet.kview.mvvm.view.stamp.StampEditController;
import dev.ikm.komet.kview.mvvm.viewmodel.ConceptViewModel;
import dev.ikm.komet.kview.mvvm.viewmodel.StampViewModel;
import dev.ikm.komet.preferences.KometPreferences;
import dev.ikm.tinkar.common.id.PublicId;
import dev.ikm.tinkar.coordinate.stamp.calculator.Latest;
import dev.ikm.tinkar.coordinate.view.calculator.ViewCalculator;
import dev.ikm.tinkar.entity.*;
import dev.ikm.tinkar.terms.*;
import javafx.beans.InvalidationListener;
import javafx.beans.property.ObjectProperty;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.fxml.FXML;
import javafx.geometry.Side;
import javafx.scene.Node;
import javafx.scene.control.*;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.*;
import javafx.scene.text.Text;
import javafx.scene.text.TextFlow;
import org.carlfx.cognitive.loader.*;
import org.carlfx.cognitive.viewmodel.ValidationViewModel;
import org.carlfx.cognitive.viewmodel.ViewModel;
import org.controlsfx.control.PopOver;
import org.eclipse.collections.api.factory.Lists;
import org.eclipse.collections.api.list.ImmutableList;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.time.Instant;
import java.time.ZoneOffset;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.function.Consumer;
import java.util.stream.Collectors;

import static dev.ikm.komet.framework.events.FrameworkTopics.RULES_TOPIC;
import static dev.ikm.komet.kview.fxutils.MenuHelper.fireContextMenuEvent;
import static dev.ikm.komet.kview.fxutils.SlideOutTrayHelper.slideIn;
import static dev.ikm.komet.kview.fxutils.SlideOutTrayHelper.slideOut;
import static dev.ikm.komet.kview.fxutils.ViewportHelper.clipChildren;
import static dev.ikm.komet.kview.mvvm.model.DataModelHelper.*;
import static dev.ikm.komet.kview.mvvm.viewmodel.ConceptViewModel.*;
import static dev.ikm.komet.kview.mvvm.viewmodel.FormViewModel.MODE;
import static dev.ikm.komet.kview.mvvm.viewmodel.StampViewModel.MODULES_PROPERTY;
import static dev.ikm.komet.kview.mvvm.viewmodel.StampViewModel.PATHS_PROPERTY;
import static dev.ikm.tinkar.coordinate.stamp.StampFields.MODULE;
import static dev.ikm.tinkar.coordinate.stamp.StampFields.PATH;
import static dev.ikm.tinkar.coordinate.stamp.StampFields.*;
import static dev.ikm.tinkar.terms.TinkarTerm.*;

public class DetailsController  {
    private static final Logger LOG = LoggerFactory.getLogger(DetailsController.class);
    private static final String EDIT_STAMP_OPTIONS_FXML = "edit-stamp.fxml";
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
    private TextArea definitionTextArea;

    @FXML
    private TextField identifierText;

    @FXML
    private Tooltip identifierTooltip;

    @FXML
    private Text lastUpdatedText;

    @FXML
    private Text moduleText;

    @FXML
    private Text pathText;

    @FXML
    private Text originationText;

    @FXML
    private Text statusText;

    /**
     * Applied to lastUpdatedText component.
     */
    private Tooltip authorTooltip = new Tooltip();

    ///// Descriptions Section /////////////////////////////////
    @FXML
    private TitledPane descriptionsTitledPane;
    @FXML
    private Button editConceptButton;

    @FXML
    private Text latestFqnText;

    @FXML
    private Text fqnDescriptionSemanticText;

    @FXML
    private Label fqnAddDateLabel;

    /**
     * Responsible for holding rows of other names (regular) description semantics.
     */
    @FXML
    private VBox otherNamesVBox;

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
    private Pane propertiesSlideoutTrayPane;

    @FXML
    private Pane timelineSlideoutTrayPane;

    @FXML
    private ContextMenu reasonerContextMenu;

    @FXML
    private MenuItem incrementalReasoner;

    /**
     * A function from the caller. This class passes a boolean true if classifier button is pressed invoke caller's function to be returned a view.
     */
    private Consumer<ToggleButton> reasonerResultsControllerConsumer;

    private ViewProperties viewProperties;

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


    private PublicId fqnPublicId;


    /**
     * Stamp Edit
     */
    private PopOver stampEdit;
    private StampEditController stampEditController;

    public DetailsController() {
    }

    public DetailsController(UUID conceptTopic) {
        this.conceptTopic = conceptTopic;
    }

    @FXML
    public void initialize() {
        identiconImageView.setOnContextMenuRequested(contextMenuEvent -> {
            // query all available memberships (semantics having the purpose as 'membership', and no fields)
            // query current concept's membership semantic records.
            // build menuItems according to 'add' or 'remove' , style to look like figma designs style classes.
            // show offset to the right of the identicon
            ViewCalculator viewCalculator = viewProperties.calculator();
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

        Tooltip.install(identifierText, identifierTooltip);
        Tooltip.install(lastUpdatedText, authorTooltip);
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
            updateConceptBanner(fqnDescrName);
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
                boolean isWritten = conceptViewModel.createConcept(viewProperties.calculator().viewCoordinateRecord().editCoordinate());
                // when written the mode changes to EDIT.
                LOG.info("Is " + conceptViewModel + " created? " + isWritten);
                if (isWritten) {
                    updateModel(getViewProperties());
                    updateView();
                }
                // remove 'Add Fully Qualified Name' from the menu
                setUpDescriptionContextMenu(addDescriptionButton);
                //TODO revisit: why should the mode ever be edit inside a create event?
            } else if (EDIT.equals(conceptViewModel.getPropertyValue(MODE))){
                    conceptViewModel.addOtherName(viewProperties.calculator().viewCoordinateRecord().editCoordinate(), descrName);
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

    }

    public ViewProperties getViewProperties() {
        return viewProperties;
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
        addDescriptionButton.setOnAction(actionEvent -> fireContextMenuEvent(actionEvent, Side.RIGHT, 2, 0));
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
                                    AddFullyQualifiedNameEvent.ADD_FQN, getViewProperties())),
                            createConceptEditDescrIcon()},
                    {"Add Other Name", true, null, (EventHandler<ActionEvent>) actionEvent -> {
                        eventBus.publish(conceptTopic, new AddOtherNameToConceptEvent(contextMenu,
                                AddOtherNameToConceptEvent.ADD_DESCRIPTION));
                    },
                            createConceptEditDescrIcon()},
                    {MenuHelper.SEPARATOR},
            };
        } else { // EDIT mode OR Create Mode after FQN has been added
            menuItems = new Object[][]{
                    {"ADD DESCRIPTION", true, new String[]{"menu-header-left-align"}, null, null},
                    {MenuHelper.SEPARATOR},
                    {"Add Other Name", true, null, (EventHandler<ActionEvent>) actionEvent -> {
                        ConceptEntity currentConcept = null;
                        if (getConceptViewModel().getPropertyValue(CURRENT_ENTITY) instanceof EntityProxy.Concept concept) {
                            currentConcept = (ConceptEntity) EntityService.get().getEntity(concept.nid()).get();
                        } else {
                            currentConcept = getConceptViewModel().getPropertyValue(CURRENT_ENTITY);
                        }
                        if (currentConcept != null) {
                            // in edit mode, will have a concept and public id
                            eventBus.publish(conceptTopic, new AddOtherNameToConceptEvent(contextMenu,
                                    // pass the publicId of the Concept
                                    AddOtherNameToConceptEvent.ADD_DESCRIPTION, currentConcept.publicId())); // concept's publicId
                        } else {
                            // in create mode, we won't have a concept and public id yet
                            eventBus.publish(conceptTopic, new AddOtherNameToConceptEvent(contextMenu,
                                    AddOtherNameToConceptEvent.ADD_DESCRIPTION));
                        }
                    },
                            createConceptEditDescrIcon()},
                    {MenuHelper.SEPARATOR},
            };
        }
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
            boolean isWritten = conceptViewModel.createConcept(viewProperties.calculator().viewCoordinateRecord().editCoordinate());
            LOG.info("Is " + conceptViewModel + " created? " + isWritten);
            if (isWritten) {
                updateModel(getViewProperties());
                updateView();
            }
        }
    }

    @FXML
    private void addSufficientSet(ActionEvent actionEvent) {
        conceptViewModel.setPropertyValue(AXIOM, ConceptViewModel.SUFFICIENT_SET);

        // Attempts to write data
        if (CREATE.equals(conceptViewModel.getPropertyValue(MODE))) {
            boolean isWritten = conceptViewModel.createConcept(viewProperties.calculator().viewCoordinateRecord().editCoordinate());
            LOG.info("Is " + conceptViewModel + " created? " + isWritten);
            if (isWritten) {
                updateModel(getViewProperties());
                updateView();
            }
        }
    }

    public void attachPropertiesViewSlideoutTray(Pane propertiesViewBorderPane) {
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

        Region contentRegion = contentViewPane;
        // binding the child's height to the preferred height of hte parent
        // so that when we resize the window the content in the slide out pane
        // aligns with the details view
        contentRegion.prefHeightProperty().bind(slideoutTrayPane.heightProperty());
    }
    private Consumer<DetailsController> onCloseConceptWindow;
    public void setOnCloseConceptWindow(Consumer<DetailsController> onClose) {
        this.onCloseConceptWindow = onClose;
    }
    @FXML
    void closeConceptWindow(ActionEvent event) {
        LOG.info("Cleanup occurring: Closing Window with concept: " + fqnTitleText.getText());
        if (this.onCloseConceptWindow != null) {
            onCloseConceptWindow.accept(this);
        }
        Pane parent = (Pane) detailsOuterBorderPane.getParent();
        parent.getChildren().remove(detailsOuterBorderPane);
    }
    public Pane getPropertiesSlideoutTrayPane() {
        return propertiesSlideoutTrayPane;
    }

    public void updateModel(final ViewProperties viewProperties) {
        this.viewProperties = viewProperties;
    }

    public void updateView() {
        EntityFacade entityFacade = conceptViewModel.getPropertyValue(CURRENT_ENTITY);
        StampViewModel stampViewModel = new StampViewModel();
        if (entityFacade != null) { // edit concept
            getConceptViewModel().setPropertyValue(MODE, EDIT);
            if (conceptViewModel.getPropertyValue(CONCEPT_STAMP_VIEW_MODEL) == null) {

                // add a new stamp view model to the concept view model
                stampViewModel.setPropertyValue(MODE, EDIT)
                        .setPropertyValues(MODULES_PROPERTY, stampViewModel.findAllModules(viewProperties), true)
                        .setPropertyValues(PATHS_PROPERTY, stampViewModel.findAllPaths(viewProperties), true);

                conceptViewModel.setPropertyValue(CONCEPT_STAMP_VIEW_MODEL,stampViewModel);
            }

            // TODO: Ability to change Concept record. but for now user can edit stamp but not affect Concept version.
            EntityVersion latestVersion = viewProperties.calculator().latest(entityFacade).get();
            StampEntity stamp = latestVersion.stamp();
            updateStampViewModel(EDIT, stamp);
        } else { // create concept
            getConceptViewModel().setPropertyValue(MODE, CREATE);
            stampViewModel.setPropertyValue(MODE, CREATE);
        }
        conceptViewModel.setPropertyValue(CONCEPT_STAMP_VIEW_MODEL,stampViewModel);


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
    public void updateConceptBanner(DescrName fqnDescrName) {
        if (fqnDescrName == null) return;

        // Title (FQN of concept)
        String conceptNameStr = fqnDescrName.getNameText();
        fqnTitleText.setText(conceptNameStr);
        conceptNameTooltip.setText(conceptNameStr);

        // Definition description text
        definitionTextArea.setText("");

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
        final ViewCalculator viewCalculator = viewProperties.calculator();
        String conceptNameStr = viewCalculator.getFullyQualifiedDescriptionTextWithFallbackOrNid(entityFacade);
        fqnTitleText.setText(conceptNameStr);
        conceptNameTooltip.setText(conceptNameStr);

        // Definition description text
        definitionTextArea.setText(viewCalculator.getDefinitionDescriptionText(entityFacade.nid()).orElse(""));

        // Public ID (UUID)
        List<String> idList = entityFacade.publicId().asUuidList().stream()
                .map(UUID::toString)
                .collect(Collectors.toList());
        idList.addAll(DataModelHelper.getIdsToAppend(viewCalculator, entityFacade.toProxy()));
        String idStr = String.join(", ", idList);
        identifierText.setText(idStr);
        identifierTooltip.setText(idStr);

        // Identicon
        Image identicon = Identicon.generateIdenticonImage(entityFacade.publicId());
        identiconImageView.setImage(identicon);

        // Obtain STAMP info
        EntityVersion latestVersion = viewCalculator.latest(entityFacade).get();
        StampEntity stamp = latestVersion.stamp();

        // Status
        String status = stamp.state() != null && State.ACTIVE == stamp.state() ? "Active" : "Inactive";
        statusText.setText(status);

        // Module
        String module = stamp.module().description();
        moduleText.setText(module);

        // Path
        String path = stamp.path().description();
        pathText.setText(path);

        // Latest update time
        DateTimeFormatter DATE_TIME_FORMATTER = DateTimeFormatter.ofPattern("yyyy-MMM-dd HH:mm:ss");
        Instant stampInstance = Instant.ofEpochSecond(stamp.time()/1000);
        ZonedDateTime stampTime = ZonedDateTime.ofInstant(stampInstance, ZoneOffset.UTC);
        String time = DATE_TIME_FORMATTER.format(stampTime);
        lastUpdatedText.setText(time);

        // Author tooltip
        authorTooltip.setText(stamp.author().description());

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
        DateTimeFormatter DATE_TIME_FORMATTER = DateTimeFormatter.ofPattern("MMM dd, yyyy");
        // Latest FQN
        String fullyQualifiedName = fqnDescrName.getNameText();
        latestFqnText.setText(fullyQualifiedName);

        latestFqnText.setOnMouseClicked(event -> {
            eventBus.publish(conceptTopic,
                    new EditConceptFullyQualifiedNameEvent(latestFqnText,
                            EditConceptFullyQualifiedNameEvent.EDIT_FQN, fqnDescrName));
        });
        // these should never be null, if the drop-downs are populated then the
        // submit button will not be enabled on the Add FQN form
        if (fqnDescrName.getCaseSignificance() != null && fqnDescrName.getLanguage() != null) {
            fqnDescriptionSemanticText.setText(" (" + fqnDescrName.getCaseSignificance().description()
                    + " | " + fqnDescrName.getLanguage().description() + ")");
        } else {
            LOG.error("missing case sensitivity and language when adding a fully qualified name");
            fqnDescriptionSemanticText.setText("");
        }
    }

    public void updateOtherNamesDescription(List<DescrName> descrNameViewModels) {
        otherNamesVBox.getChildren().clear();
        descrNameViewModels.stream().forEach( otherName -> {
            // start adding a row
            List<TextFlow> rows = generateOtherNameRow(otherName);
            rows.forEach(textFlowPane -> {
                textFlowPane.setOnMouseClicked(event -> {
                    eventBus.publish(conceptTopic,
                            new EditOtherNameConceptEvent(textFlowPane,
                                    EditOtherNameConceptEvent.EDIT_OTHER_NAME, otherName));
                });
            });
            otherNamesVBox.getChildren().addAll(rows);
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

        final ViewCalculator viewCalculator = viewProperties.calculator();
        EntityFacade entityFacade = conceptViewModel.getPropertyValue(CURRENT_ENTITY);
        // populate UI with FQN and other names. e.g. Hello Solor (English | Case-insensitive)
        Map<SemanticEntityVersion, List<String>> descriptionSemanticsMap = latestDescriptionSemantics(viewCalculator, entityFacade);
        otherNamesVBox.getChildren().clear();
        descriptionSemanticsMap.forEach((semanticEntityVersion, fieldDescriptions) -> {

            PatternEntity<PatternEntityVersion> patternEntity = semanticEntityVersion.pattern();
            PatternEntityVersion patternEntityVersion = viewCalculator.latest(patternEntity).get();

            boolean isFQN = semanticEntityVersion
                    .fieldValues()
                    .stream()
                    .anyMatch( fieldValue ->
                            (fieldValue instanceof ConceptFacade facade) &&
                                    facade.nid() == FULLY_QUALIFIED_NAME_DESCRIPTION_TYPE.nid());


            if (isFQN) {
                // Latest FQN
                updateFQNSemantics(semanticEntityVersion, fieldDescriptions);
                LOG.debug("FQN Name = " + semanticEntityVersion + " " + fieldDescriptions);
            } else {
                // start adding a row
                List<TextFlow> rows = generateOtherNameRow(semanticEntityVersion, fieldDescriptions);
                PublicId otherNamePublicId = (PublicId) rows.get(0).getUserData();
                rows.forEach(textFlowPane -> {
                    textFlowPane.setOnMouseClicked(event -> {
                        eventBus.publish(conceptTopic,
                                new EditOtherNameConceptEvent(textFlowPane,
                                        EditOtherNameConceptEvent.EDIT_OTHER_NAME, otherNamePublicId));
                    });
                });
                otherNamesVBox.getChildren().addAll(rows);

                LOG.debug("Other Names = " + semanticEntityVersion + " " + fieldDescriptions);
            }
        });
    }

    /**
     * Returns a list of TextFlow objects as rows of text items allowing the window to allow text
     * to be responsive when the gets width smaller.
     * @param semanticEntityVersion
     * @param fieldDescriptions
     * @return
     */
    private List<TextFlow> generateOtherNameRow(SemanticEntityVersion semanticEntityVersion, List<String> fieldDescriptions) {

        List<TextFlow> textFlows = new ArrayList<>();
        DateTimeFormatter DATE_TIME_FORMATTER = DateTimeFormatter.ofPattern("MMM dd, yyyy");

        String descrSemanticStr = String.join(" | ", fieldDescriptions);

        // update date
        Instant stampInstance = Instant.ofEpochSecond(semanticEntityVersion.stamp().time()/1000);
        ZonedDateTime stampTime = ZonedDateTime.ofInstant(stampInstance, ZoneOffset.UTC);
        String time = DATE_TIME_FORMATTER.format(stampTime);

        // create textflow to hold regular name label
        TextFlow row1 = new TextFlow();
        Text otherNameLabel = new Text(String.valueOf(semanticEntityVersion.fieldValues().get(1)));
        otherNameLabel.getStyleClass().add("descr-concept-name");


        Text semanticDescrText = new Text();
        if (fieldDescriptions.size() > 0) {
            semanticDescrText.setText(" (%s)".formatted(descrSemanticStr));
            semanticDescrText.getStyleClass().add("descr-semantic");
        } else {
            semanticDescrText.setText("");
        }
        // add the other name label and description semantic label
        row1.getStyleClass().add("descr-semantic-container");
        // store the public id of this semantic entity version
        // so that when clicked the event bus can pass it to the form
        // and the form can populate the data from the publicId
        row1.setUserData(semanticEntityVersion.publicId());
        row1.getChildren().addAll(otherNameLabel);

        TextFlow row2 = new TextFlow();
        row2.getChildren().addAll(semanticDescrText);

        TextFlow row3 = new TextFlow();
        Text dateAddedLabel = new Text("Date Added:");
        dateAddedLabel.getStyleClass().add("descr-semantic");
        Text dateLabel = new Text(time);
        dateLabel.getStyleClass().add("descr-semantic");

        Hyperlink attachmentHyperlink = new Hyperlink("Attachment");
        Hyperlink commentHyperlink = new Hyperlink("Comment");

        // Add the date info and additional hyperlinks
        row3.getChildren().addAll(dateAddedLabel, dateLabel, attachmentHyperlink, commentHyperlink);

        textFlows.add(row1);
        textFlows.add(row2);
        textFlows.add(row3);
        return textFlows;
    }
    private List<TextFlow> generateOtherNameRow(DescrName otherName) {

        List<TextFlow> textFlows = new ArrayList<>();
        DateTimeFormatter DATE_TIME_FORMATTER = DateTimeFormatter.ofPattern("MMM dd, yyyy");
        ViewCalculator viewCalculator = getViewProperties().calculator();
        ConceptEntity caseSigConcept = otherName.getCaseSignificance();
        String casSigText = viewCalculator.getRegularDescriptionText(caseSigConcept.nid())
                .orElse(caseSigConcept.nid()+"");
        ConceptEntity langConcept = otherName.getLanguage();

        String langText = viewCalculator.getRegularDescriptionText(langConcept.nid())
                .orElse(String.valueOf(langConcept.nid()));

        String descrSemanticStr = "%s | %s".formatted(casSigText, langText);

        // update date
        long epochmillis = getStampViewModel() == null ? System.currentTimeMillis() : getStampViewModel().getValue(TIME);
        Instant stampInstance = Instant.ofEpochSecond(epochmillis/1000);
        ZonedDateTime stampTime = ZonedDateTime.ofInstant(stampInstance, ZoneOffset.UTC);
        String time = DATE_TIME_FORMATTER.format(stampTime);

        // create textflow to hold regular name label
        TextFlow row1 = new TextFlow();
        Object obj = otherName.getNameText();
        String nameLabel = String.valueOf(obj);
        Text otherNameLabel = new Text(nameLabel);
        otherNameLabel.getStyleClass().add("descr-concept-name");

        Text semanticDescrText = new Text();
        semanticDescrText.setText(" (%s)".formatted(descrSemanticStr));
        semanticDescrText.getStyleClass().add("descr-semantic");

        // add the other name label and description semantic label
        row1.getStyleClass().add("descr-semantic-container");
        // store the public id of this semantic entity version
        // so that when clicked the event bus can pass it to the form
        // and the form can populate the data from the publicId
//        this.otherNamePublicId = semanticEntityVersion.publicId();

        row1.getChildren().addAll(otherNameLabel);

        TextFlow row2 = new TextFlow();
        row2.getChildren().addAll(semanticDescrText);

        TextFlow row3 = new TextFlow();
        Text dateAddedLabel = new Text("Date Added:");
        dateAddedLabel.getStyleClass().add("descr-semantic");
        Text dateLabel = new Text(time);
        dateLabel.getStyleClass().add("descr-semantic");

        Hyperlink attachmentHyperlink = new Hyperlink("Attachment");
        Hyperlink commentHyperlink = new Hyperlink("Comment");

        // Add the date info and additional hyperlinks
        row3.getChildren().addAll(dateAddedLabel, dateLabel, attachmentHyperlink, commentHyperlink);

        textFlows.add(row1);
        textFlows.add(row2);
        textFlows.add(row3);
        return textFlows;
    }

    private void updateFQNSemantics(SemanticEntityVersion semanticEntityVersion, List<String> fieldDescriptions) {
        DateTimeFormatter DATE_TIME_FORMATTER = DateTimeFormatter.ofPattern("MMM dd, yyyy");
        // Latest FQN
        String fullyQualifiedName = (String) semanticEntityVersion.fieldValues().get(1);
        latestFqnText.setText(fullyQualifiedName);

        this.fqnPublicId = semanticEntityVersion.publicId();
        latestFqnText.setOnMouseClicked(event -> {
            eventBus.publish(conceptTopic,
                    new EditConceptFullyQualifiedNameEvent(latestFqnText,
                            EditConceptFullyQualifiedNameEvent.EDIT_FQN, fqnPublicId));
        });

        String descrSemanticStr = String.join(" | ", fieldDescriptions);
        if (fieldDescriptions.size() > 0) {
            fqnDescriptionSemanticText.setText(" (%s)".formatted(descrSemanticStr));
        } else {
            fqnDescriptionSemanticText.setText("");
        }
        // update date
        Instant stampInstance = Instant.ofEpochSecond(semanticEntityVersion.stamp().time()/1000);
        ZonedDateTime stampTime = ZonedDateTime.ofInstant(stampInstance, ZoneOffset.UTC);
        String time = DATE_TIME_FORMATTER.format(stampTime);
        fqnAddDateLabel.setText(time);
    }

    /**
     * Returns a list of description semantics. This currently returns two specific semantics
     * Case significance & Language preferred. E.g. (Case-sensitive | English)
     * @return Map<Integer, List<String>> Map of nids to a List of strings containing field's values.
     */
    private Map<SemanticEntityVersion, List<String>> latestDescriptionSemantics(final ViewCalculator viewCalculator, EntityFacade conceptFacade) {
        Map<SemanticEntityVersion, List<String>> descriptionSemanticsMap = new HashMap<>();

        // FQN - English | Case Sensitive
        // REG - English | Case Sensitive

        //Get latest description semantic version of the passed in concept (entityfacade)
        //Latest<SemanticEntityVersion> latestDescriptionSemanticVersion = viewCalculator.getDescription(conceptFacade);

        //There should always be one FQN
        //There can be 0 or more Regular Names
        //Loop through, conditionally sort semantics by their description type concept object
        //Update UI via the descriptionRegularName function on the
        viewCalculator.getDescriptionsForComponent(conceptFacade).stream()
                .filter(semanticEntity -> {
                    // semantic -> semantic version -> pattern version(index meaning field from DESCR_Type)
                    Latest<SemanticEntityVersion> semanticVersion = viewCalculator.latest(semanticEntity);

                    PatternEntity<PatternEntityVersion> patternEntity = semanticEntity.pattern();
                    PatternEntityVersion patternEntityVersion = viewCalculator.latest(patternEntity).get();

                    int indexForDescrType = patternEntityVersion.indexForMeaning(TinkarTerm.DESCRIPTION_TYPE);

                    // Filter (include) semantics where they contain descr type having FQN, Regular name, Definition Descr.
                    Object descriptionTypeConceptValue = semanticVersion.get().fieldValues().get(indexForDescrType);
                    if(descriptionTypeConceptValue instanceof EntityFacade descriptionTypeConcept ){
                        int typeId = descriptionTypeConcept.nid();
                        return (typeId == FULLY_QUALIFIED_NAME_DESCRIPTION_TYPE.nid() ||
                                typeId == REGULAR_NAME_DESCRIPTION_TYPE.nid() ||
                                typeId == DEFINITION_DESCRIPTION_TYPE.nid());
                    }
                    return false;
                }).forEach(semanticEntity -> {
                    // Each description obtain the latest semantic version, pattern version and their field values based on index
                    Latest<SemanticEntityVersion> semanticVersion = viewCalculator.latest(semanticEntity);
                    PatternEntity<PatternEntityVersion> patternEntity = semanticEntity.pattern();
                    PatternEntityVersion patternEntityVersion = viewCalculator.latest(patternEntity).get();

                    int indexCaseSig = patternEntityVersion.indexForMeaning(DESCRIPTION_CASE_SIGNIFICANCE);
                    int indexLang = patternEntityVersion.indexForMeaning(LANGUAGE_CONCEPT_NID_FOR_DESCRIPTION);

                    List<String> descrFields = new ArrayList<>();
                    descriptionSemanticsMap.put(semanticVersion.get(), descrFields);
                    Object caseSigConcept = semanticVersion.get().fieldValues().get(indexCaseSig);
                    Object langConcept = semanticVersion.get().fieldValues().get(indexLang);

                    // e.g. FQN - English | Case Sensitive
                    String casSigText = viewCalculator.getRegularDescriptionText(((ConceptFacade) caseSigConcept).nid())
                            .orElse(String.valueOf(((ConceptFacade) caseSigConcept).nid()));
                    String langText = viewCalculator.getRegularDescriptionText(((ConceptFacade) langConcept).nid())
                            .orElse(String.valueOf(((ConceptFacade) langConcept).nid()));

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
        ViewCalculator viewCalculator = viewProperties.calculator();
        EntityFacade entityFacade = conceptViewModel.getPropertyValue(CURRENT_ENTITY);

        // add axiom pencil
        addAxiomButton.setVisible(entityFacade == null); // In view mode you can't add a sufficient/necc set

        // Create a SheetItem (AXIOM inferred semantic version)
        // TODO Should this be reused instead of instanciating a new one everytime?
        KometPropertySheet inferredPropertySheet = new KometPropertySheet(viewProperties, true);
        Latest<SemanticEntityVersion> inferredSemanticVersion = viewCalculator.getInferredAxiomSemanticForEntity(entityFacade.nid());
        makeSheetItem(viewProperties, inferredPropertySheet, inferredSemanticVersion);
        inferredAxiomPane.setCenter(inferredPropertySheet);


        // Create a SheetItem (AXIOM stated semantic version)
        KometPropertySheet statedPropertySheet = new KometPropertySheet(viewProperties, true);
        Latest<SemanticEntityVersion> statedSemanticVersion    = viewCalculator.getStatedAxiomSemanticForEntity(entityFacade.nid());
        makeSheetItem(viewProperties, statedPropertySheet, statedSemanticVersion);
        statedAxiomPane.setCenter(statedPropertySheet);

        //TODO discuss the blue theme color related to AXIOMs

    }

    private void makeSheetItem(ViewProperties viewProperties,
                               KometPropertySheet propertySheet,
                               Latest<SemanticEntityVersion> semanticVersion) {
        semanticVersion.ifPresent(semanticEntityVersion -> {
            Latest<PatternEntityVersion> statedPatternVersion = viewProperties.calculator().latestPatternEntityVersion(semanticEntityVersion.pattern());
            ImmutableList<ObservableField> fields = fields(semanticEntityVersion, statedPatternVersion.get());
            fields.forEach(field ->
                    // create a row as a label: editor. For Axioms we hide the left labels.
                    propertySheet.getItems().add(SheetItem.make(field, semanticEntityVersion, viewProperties)));
        });

    }

    public void clearView() {
        identiconImageView.setImage(null);
        //fqnTitleText.setText(""); // Defaults to 'Concept Name'. It's what is specified in Scene builder
        definitionTextArea.setText("");
        identifierText.setText("");
        lastUpdatedText.setText("");
        moduleText.setText("");
        pathText.setText("");
        originationText.setText("");
        statusText.setText("");
        authorTooltip.setText("");
        notAvailInferredAxiomLabel.setVisible(true);
        notAvailStatedAxiomLabel.setVisible(true);
        otherNamesVBox.getChildren().clear();
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
    private void openPropertiesPanel(ActionEvent event) {
        ToggleButton propertyToggle = (ToggleButton) event.getSource();
        // if selected open properties
        if (propertyToggle.isSelected()) {
            LOG.info("Opening slideout of properties");
            slideOut(propertiesSlideoutTrayPane, detailsOuterBorderPane);

            if (CREATE.equals(conceptViewModel.getPropertyValue(MODE))) {
                // show the Add FQN
                eventBus.publish(conceptTopic, new AddFullyQualifiedNameEvent(propertyToggle,
                        AddFullyQualifiedNameEvent.ADD_FQN, getViewProperties()));
            } else if (EDIT.equals(conceptViewModel.getPropertyValue(MODE))){
                // show the button form
                eventBus.publish(conceptTopic, new OpenPropertiesPanelEvent(propertyToggle,
                        OpenPropertiesPanelEvent.OPEN_PROPERTIES_PANEL, fqnPublicId, fqnTitleText.getText()));
            }
        } else {
            LOG.info("Close Properties slideout");
            slideIn(propertiesSlideoutTrayPane, detailsOuterBorderPane);
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
            stampEdit.show((Node) event.getSource());
            return;
        }
        // Prefetch modules and paths for view to populate radio buttons in form.
        StampViewModel stampViewModel = new StampViewModel();

        // Populate from database
        stampViewModel.setPropertyValue(PATHS_PROPERTY, stampViewModel.findAllPaths(viewProperties), true)
                .setPropertyValue(MODULES_PROPERTY, stampViewModel.findAllModules(viewProperties), true);

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

        stampEditController.updateModel(viewProperties);

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
        DateTimeFormatter DATE_TIME_FORMATTER = DateTimeFormatter.ofPattern("yyyy-MMM-dd HH:mm:ss");
        Instant stampInstance = Instant.ofEpochSecond(time/1000);
        ZonedDateTime stampTime = ZonedDateTime.ofInstant(stampInstance, ZoneOffset.UTC);
        String lastUpdated = DATE_TIME_FORMATTER.format(stampTime);

        lastUpdatedText.setText(lastUpdated);
        ConceptEntity moduleEntity = stampViewModel.getValue(MODULE);
        if (moduleEntity == null) {
            LOG.warn("Must select a valid module for Stamp.");
            return;
        }
        String moduleDescr = viewProperties.calculator().getPreferredDescriptionTextWithFallbackOrNid(moduleEntity.nid());
        moduleText.setText(moduleDescr);
        ConceptEntity pathEntity = stampViewModel.getValue(PATH);
        String pathDescr = viewProperties.calculator().getPreferredDescriptionTextWithFallbackOrNid(pathEntity.nid());
        pathText.setText(pathDescr);
        State status = stampViewModel.getValue(STATUS);
        String statusMsg = status == null ? "Active" : viewProperties.calculator().getPreferredDescriptionTextWithFallbackOrNid(status.nid());
        statusText.setText(statusMsg);
    }

    public void compactSizeWindow() {
        descriptionsTitledPane.setExpanded(false);
        axiomsTitledPane.setExpanded(false);
        //581 x 242
        detailsOuterBorderPane.setPrefSize(581, 242);
    }

    public void setConceptTopic(UUID conceptTopic) {
        this.conceptTopic = conceptTopic;
    }
}
