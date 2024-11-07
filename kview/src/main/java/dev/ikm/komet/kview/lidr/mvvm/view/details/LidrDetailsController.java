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
package dev.ikm.komet.kview.lidr.mvvm.view.details;

import dev.ikm.komet.framework.Identicon;
import dev.ikm.komet.framework.events.EvtBus;
import dev.ikm.komet.framework.events.EvtBusFactory;
import dev.ikm.komet.framework.events.EvtType;
import dev.ikm.komet.framework.events.Subscriber;
import dev.ikm.komet.framework.view.ViewProperties;
import dev.ikm.komet.kview.data.schema.STAMPDetail;
import dev.ikm.komet.kview.events.StampModifiedEvent;
import dev.ikm.komet.kview.lidr.events.AddDeviceEvent;
import dev.ikm.komet.kview.lidr.events.AddResultInterpretationEvent;
import dev.ikm.komet.kview.lidr.events.LidrPropertyPanelEvent;
import dev.ikm.komet.kview.lidr.events.ShowPanelEvent;
import dev.ikm.komet.kview.lidr.mvvm.model.DataModelHelper;
import dev.ikm.komet.kview.lidr.mvvm.model.LidrRecord;
import dev.ikm.komet.kview.lidr.mvvm.view.properties.PropertiesController;
import dev.ikm.komet.kview.lidr.mvvm.viewmodel.AnalyteGroupViewModel;
import dev.ikm.komet.kview.lidr.mvvm.viewmodel.LidrViewModel;
import dev.ikm.komet.kview.mvvm.model.DescrName;
import dev.ikm.komet.kview.mvvm.view.stamp.StampEditController;
import dev.ikm.komet.kview.mvvm.view.timeline.TimelineController;
import dev.ikm.komet.kview.mvvm.viewmodel.StampViewModel;
import dev.ikm.tinkar.common.id.PublicId;
import dev.ikm.tinkar.component.Concept;
import dev.ikm.tinkar.coordinate.view.calculator.ViewCalculator;
import dev.ikm.tinkar.entity.ConceptEntity;
import dev.ikm.tinkar.entity.EntityVersion;
import dev.ikm.tinkar.entity.StampEntity;
import dev.ikm.tinkar.provider.search.Searcher;
import dev.ikm.tinkar.terms.ConceptFacade;
import dev.ikm.tinkar.terms.EntityFacade;
import dev.ikm.tinkar.terms.State;
import javafx.application.Platform;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Node;
import javafx.scene.control.*;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.*;
import javafx.scene.text.Text;
import org.carlfx.cognitive.loader.*;
import org.carlfx.cognitive.viewmodel.ValidationViewModel;
import org.carlfx.cognitive.viewmodel.ViewModel;
import org.controlsfx.control.PopOver;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.net.URL;
import java.time.Instant;
import java.time.ZoneOffset;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.function.Consumer;

import static dev.ikm.komet.kview.fxutils.CssHelper.defaultStyleSheet;
import static dev.ikm.komet.kview.fxutils.SlideOutTrayHelper.*;
import static dev.ikm.komet.kview.fxutils.ViewportHelper.clipChildren;
import static dev.ikm.komet.kview.lidr.events.LidrPropertyPanelEvent.CLOSE_PANEL;
import static dev.ikm.komet.kview.lidr.events.LidrPropertyPanelEvent.OPEN_PANEL;
import static dev.ikm.komet.kview.lidr.events.ShowPanelEvent.SHOW_ADD_ANALYTE_GROUP;
import static dev.ikm.komet.kview.lidr.events.ShowPanelEvent.SHOW_ADD_DEVICE;
import static dev.ikm.komet.kview.lidr.mvvm.model.DataModelHelper.ORDINAL_CONCEPT;
import static dev.ikm.komet.kview.lidr.mvvm.view.details.LidrRecordDetailsController.LIDR_RECORD_FXML;
import static dev.ikm.komet.kview.lidr.mvvm.viewmodel.LidrViewModel.*;
import static dev.ikm.komet.kview.lidr.mvvm.viewmodel.ViewModelHelper.addNewLidrRecord;
import static dev.ikm.komet.kview.lidr.mvvm.viewmodel.ViewModelHelper.toStampDetail;
import static dev.ikm.komet.kview.mvvm.viewmodel.FormViewModel.MODE;
import static dev.ikm.komet.kview.mvvm.viewmodel.StampViewModel.*;
import static dev.ikm.tinkar.coordinate.stamp.StampFields.*;

public class LidrDetailsController {
    private static final Logger LOG = LoggerFactory.getLogger(LidrDetailsController.class);
    public static final String EDIT_STAMP_OPTIONS_FXML = "edit-stamp.fxml";
    public static final URL LIDR_PROPERTIES_VIEW_FXML_URL = PropertiesController.class.getResource("lidr-properties.fxml");

    protected static final String CONCEPT_TIMELINE_VIEW_FXML_FILE = "timeline.fxml";

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
    private Label deviceTitleText;
    @FXML
    private Tooltip conceptNameTooltip;


    @FXML
    private TextField snomedIdentifierText;

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
    private Text deviceSummaryText;

    @FXML
    private Text mfgSummaryText;

    ///// Results Section    ///////////////////
    @FXML
    private Button addAnalyteGroupButton; // see action showAnalyteGroupPanel() method.

    @FXML
    private TitledPane lidrRecordDetailsTitledPane;
    @FXML
    private VBox lidrRecordsVBox;

    @FXML
    private Button elppSemanticCountButton;

    @FXML
    private HBox conceptHeaderControlToolBarHbox;

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

    /**
     * A function from the caller. This class passes a boolean true if classifier button is pressed invoke caller's function to be returned a view.
     */
    private Consumer<ToggleButton> reasonerResultsControllerConsumer;

    @InjectViewModel
    private LidrViewModel lidrViewModel;
    private EvtBus eventBus;
    
    private UUID conceptTopic;

    /**
     * Stamp Edit
     */
    private PopOver stampEdit;
    private StampEditController stampEditController;

    private PropertiesController propertiesViewController;
    private BorderPane propertiesViewBorderPane;
    private BorderPane timelineViewBorderPane;
    private TimelineController timelineViewController;
    // style the same as the details view

    public LidrDetailsController() {
    }

    @FXML
    public void initialize() {
        // event bus will listen on this topic.
        if (conceptTopic == null) {
            // if not set caller used the one set inside the view model.
            conceptTopic = lidrViewModel.getPropertyValue(CONCEPT_TOPIC);
        }

        Tooltip.install(identifierText, identifierTooltip);
        Tooltip.install(snomedIdentifierText, identifierTooltip);
        Tooltip.install(lastUpdatedText, authorTooltip);

        clearView();

        eventBus = EvtBusFactory.getDefaultEvtBus();

        // listen for open and close events
        Subscriber<LidrPropertyPanelEvent> propBumpOutListener = (evt) -> {
                if (evt.getEventType() == CLOSE_PANEL) {
                    LOG.info("propBumpOutListener - Close Properties bumpout toggle = " + propertiesToggleButton.isSelected());
                    propertiesToggleButton.setSelected(false);
                    if (isOpen(propertiesSlideoutTrayPane)) {
                        slideIn(propertiesSlideoutTrayPane, detailsOuterBorderPane);
                    }
                } else if (evt.getEventType() == OPEN_PANEL) {
                    LOG.info("propBumpOutListener - Opening Properties bumpout toggle = " + propertiesToggleButton.isSelected());
                    propertiesToggleButton.setSelected(true);
                    if (isClosed(propertiesSlideoutTrayPane)) {
                        slideOut(propertiesSlideoutTrayPane, detailsOuterBorderPane);
                    }
                }
        };
        eventBus.subscribe(conceptTopic, LidrPropertyPanelEvent.class, propBumpOutListener);

        // Listen when a new device is being added to this lidr details populates mfg
        Subscriber<AddDeviceEvent> addDeviceEventSubscriber = (evt) -> {
            // TODO Update the UI and add device.
            LOG.info("addDeviceEventSubscriber -> TODO Update the UI and add a new device.");
            EntityFacade currentDevice = getLidrViewModel().getPropertyValue(DEVICE_ENTITY);
            boolean sameDevice = currentDevice == null ? false : PublicId.equals(evt.deviceEntity.publicId(), currentDevice.publicId());
            // if it's a different device than clear details and
            if (sameDevice) {
                return;
            }
            getLidrViewModel().setPropertyValue(DEVICE_ENTITY, evt.deviceEntity);
            propertiesViewController.updateModel(getViewProperties(), evt.deviceEntity);
            propertiesViewController.updateView();
            clearView();
            updateView();
        };
        eventBus.subscribe(conceptTopic, AddDeviceEvent.class, addDeviceEventSubscriber);

        // (Transaction) Listen when a new analyte group is added to this device. Will write to db and add to Lidr record details
        Subscriber<AddResultInterpretationEvent> addResultInterpretationEventSubscriber = (evt) -> {

            LOG.info("addResultInterpretationEventSubscriber -> Lidr created and details displayed");
            LidrRecord lidrRecord = evt.getLidrRecord();
            // Create a lidr record in the database.
            Concept device = getLidrViewModel().getPropertyValue(DEVICE_ENTITY);

            PublicId testPerformedId = DataModelHelper.findTestPerformed(device.publicId());
            PublicId resultOrdinalId = ORDINAL_CONCEPT.publicId();
            LidrRecord newLidrRecord = new LidrRecord(
                    lidrRecord.lidrRecordId(),
                    testPerformedId,
                    resultOrdinalId,
                    lidrRecord.analyte(),
                    lidrRecord.targets(),
                    lidrRecord.specimens(),
                    lidrRecord.resultConformances());
            PublicId lidrPublicId = addNewLidrRecord(newLidrRecord, device.publicId(), getStampViewModel());

            // Populate with the Accordion containing one Analyte Group (aka LIDR record semantic record)
            addLidrRecordDetailsAccordion(lidrPublicId);
        };
        eventBus.subscribe(conceptTopic, AddResultInterpretationEvent.class, addResultInterpretationEventSubscriber);

        // Setup Properties
        setupProperties();
        setupTimelineBumpOut();
    }

    /**
     * Adds an accordion of a lidr record details.
     * @param lidrRecordPublicId public id of existing lidr record details
     */
    private void addLidrRecordDetailsAccordion(PublicId lidrRecordPublicId) {
        LidrRecord lidrRecord = null;
        try {
            lidrRecord = DataModelHelper.makeLidrRecord(lidrRecordPublicId);
        } catch (NoSuchElementException ex) {
            // TODO data is bad. Not able to get Stated or Inferred discription logic in axioms.
            LOG.error("Error Not able to get Stated or Inferred description logic in axioms.", ex);
            return; // eat exception
        }
        // Populate with the Accordion containing one Analyte Group (aka LIDR record semantic record)
        AnalyteGroupViewModel analyteGroupViewModel = new AnalyteGroupViewModel();
        analyteGroupViewModel.setPropertyValue(CONCEPT_TOPIC, conceptTopic)
                .addProperty(AnalyteGroupViewModel.LIDR_RECORD, lidrRecord)
                .save(true);
        NamedVm analyteViewModel = new NamedVm("analyteGroupViewModel", analyteGroupViewModel);
        JFXNode<TilePane, LidrRecordDetailsController> lidrNodeController = FXMLMvvmLoader.make(this.getClass().getResource(LIDR_RECORD_FXML), analyteViewModel);
        Platform.runLater(()-> {
            lidrRecordsVBox.getChildren().add(lidrNodeController.node());
            lidrNodeController.controller().updateView();
        });
    }
    private void setupProperties() {
        // Setup Property screen bump out
        // Load Concept Properties View Panel (FXML & Controller)
        Config config = new Config(LIDR_PROPERTIES_VIEW_FXML_URL)
                .updateViewModel("propertiesViewModel", (propertiesViewModel) ->
                        propertiesViewModel
                                .setPropertyValue(VIEW_PROPERTIES, getViewProperties())
                                .setPropertyValue(CONCEPT_TOPIC, conceptTopic)
                                .setPropertyValue(DEVICE_ENTITY, getLidrViewModel().getPropertyValue(DEVICE_ENTITY)));

        JFXNode<BorderPane, PropertiesController> propsFXMLLoader = FXMLMvvmLoader.make(config);
        this.propertiesViewBorderPane = propsFXMLLoader.node();
        this.propertiesViewController = propsFXMLLoader.controller();

        // setup view and view into details view
        attachPropertiesViewSlideoutTray(this.propertiesViewBorderPane);
    }

    private void setupTimelineBumpOut() {
        // Load Timeline View Panel (FXML & Controller)
        FXMLLoader timelineFXMLLoader = new FXMLLoader(TimelineController.class.getResource(CONCEPT_TIMELINE_VIEW_FXML_FILE));
        try {
            this.timelineViewBorderPane = timelineFXMLLoader.load();
            this.timelineViewController = timelineFXMLLoader.getController();

            // This will highlight with green around the pane when the user selects a date point in the timeline.
            timelineViewController.onDatePointSelected((changeCoordinate) ->{
                propertiesViewController.getHistoryChangeController().highlightListItemByChangeCoordinate(changeCoordinate);
            });
            // When Date points are in range (range slider)
            timelineViewController.onDatePointInRange((rangeToggleOn, changeCoordinates) -> {
                if (rangeToggleOn) {
                    propertiesViewController.getHistoryChangeController().filterByRange(changeCoordinates);
                    propertiesViewController.getHierarchyController().diffNavigationGraph(changeCoordinates);
                } else {
                    propertiesViewController.getHistoryChangeController().unfilterByRange();
                    propertiesViewController.getHierarchyController().diffNavigationGraph(Set.of());
                }
            });

            // style the same as the details view
            this.timelineViewBorderPane.getStylesheets().add(defaultStyleSheet());

            // setup view and view into details view
            attachTimelineViewSlideoutTray(this.timelineViewBorderPane);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }

    }

    public ViewProperties getViewProperties() {
        return getLidrViewModel().getPropertyValue(VIEW_PROPERTIES);
    }

    public ValidationViewModel getLidrViewModel() {
        return lidrViewModel;
    }

    public ValidationViewModel getStampViewModel() {
        return lidrViewModel.getPropertyValue(STAMP_VIEW_MODEL);
    }

    @FXML
    private void showAddAnalyteGroupPanel(ActionEvent actionEvent) {
        // Todo show bump out and display Add analyte group panel.
        System.out.println("Todo show bump out and display Add analyte group panel \n" + actionEvent);
        // publish show Add analyte group panel
        eventBus.publish(conceptTopic, new ShowPanelEvent(actionEvent.getSource(), SHOW_ADD_ANALYTE_GROUP));
        // publish property open.
        eventBus.publish(conceptTopic, new LidrPropertyPanelEvent(actionEvent.getSource(), OPEN_PANEL));

    }
    @FXML
    private void showAddDevicePanel(ActionEvent actionEvent) {
        // Todo show bump out and display Add Device and MFG panel
        System.out.println("Todo show bump out and display Add Device and MFG panel \n" + actionEvent);
        // publish show Add analyte group panel
        eventBus.publish(conceptTopic, new ShowPanelEvent(actionEvent.getSource(), SHOW_ADD_DEVICE));
        // publish property open.
        eventBus.publish(conceptTopic, new LidrPropertyPanelEvent(actionEvent.getSource(), OPEN_PANEL));
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
    private Consumer<LidrDetailsController> onCloseConceptWindow;
    public void setOnCloseConceptWindow(Consumer<LidrDetailsController> onClose) {
        this.onCloseConceptWindow = onClose;
    }
    @FXML
    void closeConceptWindow(ActionEvent event) {
        LOG.info("Cleanup occurring: Closing Window with concept: " + deviceTitleText.getText());
        if (this.onCloseConceptWindow != null) {
            onCloseConceptWindow.accept(this);
        }
        Pane parent = (Pane) detailsOuterBorderPane.getParent();
        parent.getChildren().remove(detailsOuterBorderPane);
    }
    public Pane getPropertiesSlideoutTrayPane() {
        return propertiesSlideoutTrayPane;
    }

    public void updateView() {
        EntityFacade entityFacade = lidrViewModel.getPropertyValue(DEVICE_ENTITY);
        if (entityFacade != null) {
            EntityVersion latestVersion = getViewProperties().calculator().latest(entityFacade).get();
            StampEntity stamp = latestVersion.stamp();

            getLidrViewModel().setPropertyValue(MODE, EDIT);
            if (getStampViewModel() == null) {

                // add a new stamp view model to the concept view model
                StampViewModel stampViewModel = new StampViewModel();
                stampViewModel.setPropertyValue(MODE, EDIT)
                        .setPropertyValue(STATUS, stamp.state())
                        .setPropertyValue(AUTHOR, stamp.author())
                        .setPropertyValue(MODULE, stamp.module())
                        .setPropertyValue(PATH, stamp.path())
                        .setPropertyValues(MODULES_PROPERTY, stampViewModel.findAllModules(getViewProperties()), true)
                        .setPropertyValues(PATHS_PROPERTY, stampViewModel.findAllPaths(getViewProperties()), true);

                getLidrViewModel().setPropertyValue(STAMP_VIEW_MODEL,stampViewModel);
            } else {
                getStampViewModel()
                        .setPropertyValue(STATUS, stamp.state())
                        .setPropertyValue(AUTHOR, stamp.author())
                        .setPropertyValue(MODULE, stamp.module())
                        .setPropertyValue(PATH, stamp.path());
            }

            STAMPDetail stampDetail = toStampDetail(getStampViewModel());
            eventBus.publish(conceptTopic, new StampModifiedEvent("no source", StampModifiedEvent.UPDATED, stampDetail));

            // TODO: Ability to change Concept record. but for now user can edit stamp but not affect Concept version.
            updateStampViewModel(EDIT, stamp);
        }

        if(entityFacade == null){
            getStampViewModel().setPropertyValue(MODE, CREATE);
        }else {
            getStampViewModel().setPropertyValue(MODE, EDIT);
        }
        // Display info for top banner area
        updateDeviceBanner();

        // Display Description info area
        updateDeviceAndMfg();

        // Update Lidr Record Details
        refreshLidrRecordDetails();
    }
    public void onReasonerSlideoutTray(Consumer<ToggleButton> reasonerResultsControllerConsumer) {
        this.reasonerResultsControllerConsumer = reasonerResultsControllerConsumer;
    }
    public void updateDeviceBanner(DescrName fqnDescrName) {
        if (fqnDescrName == null) return;

        // Title (FQN of concept)
        String conceptNameStr = fqnDescrName.getNameText();
        deviceTitleText.setText(conceptNameStr);
        conceptNameTooltip.setText(conceptNameStr);

    }
    /**
     * Responsible for populating the top banner area of the concept view panel.
     */
    public void updateDeviceBanner() {
        EntityFacade entityFacade = getLidrViewModel().getPropertyValue(DEVICE_ENTITY);
        if (entityFacade == null) {
            identiconImageView.setImage(null);
            snomedIdentifierText.setText("");
            identifierText.setText("");
            deviceSummaryText.setText("");
            conceptNameTooltip.setText("");
            deviceTitleText.setText("");
            return;
        }

        deviceTitleText.setText(entityFacade.description());
        conceptNameTooltip.setText(entityFacade.description());

        // TODO do a null check on the entityFacade
        final ViewCalculator viewCalculator = getViewProperties().calculator();

        // Public ID (UUID)
        String uuidStr = entityFacade.publicId() != null ? entityFacade.publicId().asUuidArray()[0].toString(): "";
        identifierText.setText(uuidStr);
        identifierTooltip.setText(uuidStr);

        // Identicon
        Image identicon = Identicon.generateIdenticonImage(entityFacade.publicId());
        identiconImageView.setImage(identicon);
        String formMode = getLidrViewModel().getPropertyValue(MODE);
        if (VIEW.equals(formMode) || EDIT.equals(formMode)) {
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
            Instant stampInstance = Instant.ofEpochSecond(stamp.time() / 1000);
            ZonedDateTime stampTime = ZonedDateTime.ofInstant(stampInstance, ZoneOffset.UTC);
            String time = DATE_TIME_FORMATTER.format(stampTime);
            lastUpdatedText.setText(time);

            // Author tooltip
            authorTooltip.setText(stamp.author().description());

            updateStampViewModel(EDIT, stamp);
        }

    }

    private void updateStampViewModel(String mode, StampEntity stamp) {
        ValidationViewModel stampViewModel = getLidrViewModel().getPropertyValue(STAMP_VIEW_MODEL);
        if (getLidrViewModel().getPropertyValue(STAMP_VIEW_MODEL) != null) {
            stampViewModel.setPropertyValue(MODE, mode)
                    .setPropertyValue(STATUS, stamp.state())
                    .setPropertyValue(MODULE, stamp.module())
                    .setPropertyValue(PATH, stamp.path())
                    .setPropertyValue(TIME, stamp.time())
                    .save(true);
        }
    }

    /**
     * Responsible for populating the Descriptions TitledPane area. This retrieves the latest concept version and
     * semantics for language and case significance.
     */
    public void updateDeviceAndMfg() {
        // do not update ui should be blank
        EntityFacade entityFacade = getLidrViewModel().getPropertyValue(DEVICE_ENTITY);
        if (entityFacade == null) {
            deviceSummaryText.setText("");
            mfgSummaryText.setText("");
            return;
        }

        // Display the name of the device
        deviceSummaryText.setText(entityFacade.description());

        // Update manufacturer if one exists
        Optional<ConceptFacade> mfg = DataModelHelper.findDeviceManufacturer(entityFacade.publicId());
        mfg.ifPresentOrElse(concept -> mfgSummaryText.setText(
                ((ConceptFacade) concept).description()),
                ()-> mfgSummaryText.setText("")
        );
    }

    /**
     * Refresh the Lidr Record Details (Accordions). Clears VBox and populates each lidr record.
     */
    private void refreshLidrRecordDetails() {
        // do not update ui should be blank
        if (getLidrViewModel().getPropertyValue(MODE) == CREATE) {
            return;
        }
        // populate the lidr record details
        EntityFacade entityFacade = getLidrViewModel().getPropertyValue(DEVICE_ENTITY);
        List<PublicId> lidrRecordIds = Searcher.getLidrRecordSemanticsFromTestKit(entityFacade.publicId());
        lidrRecordIds.forEach(lidrRecordPublicId -> {
            // Populate with the Accordion containing one Analyte Group (aka LIDR record semantic record)
            addLidrRecordDetailsAccordion(lidrRecordPublicId);
        });
    }


    public void clearView() {
        identiconImageView.setImage(null);
        snomedIdentifierText.setText("");
        identifierText.setText("");
        lastUpdatedText.setText("");
        moduleText.setText("");
        pathText.setText("");
        originationText.setText("");
        statusText.setText("");
        authorTooltip.setText("");
        lidrRecordsVBox.getChildren().clear();
    }

    @FXML
    private void openPropertiesPanel(ActionEvent event) {
        ToggleButton propertyToggle = (ToggleButton) event.getSource();
        EvtType<LidrPropertyPanelEvent> eventEvtType = propertyToggle.isSelected() ? OPEN_PANEL : CLOSE_PANEL;
        eventBus.publish(conceptTopic, new LidrPropertyPanelEvent(propertyToggle, eventEvtType));
    }

    @FXML
    private void openTimelinePanel(ActionEvent event) {
        ToggleButton timelineToggle = (ToggleButton) event.getSource();
        // if selected open properties
        if (timelineToggle.isSelected()) {
            LOG.info("Opening slideout of timeline panel");
            slideOut(timelineSlideoutTrayPane, detailsOuterBorderPane);
        } else {
            LOG.info("Close Properties timeline panel");
            slideIn(timelineSlideoutTrayPane, detailsOuterBorderPane);
        }
    }

    @FXML
    private void openReasonerSlideout(ActionEvent event) {
        ToggleButton reasonerToggle = (ToggleButton) event.getSource();
        reasonerResultsControllerConsumer.accept(reasonerToggle);
    }

    /**
     * When user selects the STAMP edit button to pop up the options to edit.
     * @param event
     */
    @FXML
    public void popupStampEdit(ActionEvent event) {
        if (stampEdit !=null && stampEditController != null) {
            stampEdit.show((Node) event.getSource());
            stampEditController.selectActiveStatusToggle();
            return;
        }

        // Inject Stamp view model into form.
        Config stampConfig = new Config()
                .fxml(StampEditController.class.getResource(EDIT_STAMP_OPTIONS_FXML))
                .addNamedViewModel(new NamedVm("stampViewModel", lidrViewModel.getPropertyValue(STAMP_VIEW_MODEL)));
        JFXNode<Pane, StampEditController> stampJFXNode = FXMLMvvmLoader.make(stampConfig);

        Pane editStampPane = stampJFXNode.node();
        PopOver popOver = new PopOver(editStampPane);
        popOver.getStyleClass().add("filter-menu-popup");
        StampEditController stampEditController = stampJFXNode.controller();

        stampEditController.updateModel(getViewProperties());
        stampEditController.selectActiveStatusToggle();

        popOver.setOnHidden(windowEvent -> {
            // set Stamp info into Details form
            getStampViewModel().save();
            updateUIStamp(getStampViewModel());
            STAMPDetail stampDetail = toStampDetail(getStampViewModel());
            eventBus.publish(conceptTopic, new StampModifiedEvent(popOver, StampModifiedEvent.UPDATED, stampDetail));
        });
        popOver.show((Node) event.getSource());

        // store and use later.
        stampEdit = popOver;
        this.stampEditController = stampEditController;
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
        String moduleDescr = getViewProperties().calculator().getPreferredDescriptionTextWithFallbackOrNid(moduleEntity.nid());
        moduleText.setText(moduleDescr);

        ConceptEntity pathEntity = stampViewModel.getValue(PATH);
        String pathDescr = getViewProperties().calculator().getPreferredDescriptionTextWithFallbackOrNid(pathEntity.nid());
        pathText.setText(pathDescr);
        statusText.setText(stampViewModel.getValue(STATUS).toString());
    }

    public void compactSizeWindow() {
        descriptionsTitledPane.setExpanded(false);
        lidrRecordDetailsTitledPane.setExpanded(false);
        //581 x 242
        detailsOuterBorderPane.setPrefSize(581, 242);
    }

    public void setConceptTopic(UUID conceptTopic) {
        this.conceptTopic = conceptTopic;
    }
}
