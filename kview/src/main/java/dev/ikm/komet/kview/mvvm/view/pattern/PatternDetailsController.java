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
package dev.ikm.komet.kview.mvvm.view.pattern;

import static dev.ikm.komet.kview.events.ShowPatternPanelEvent.SHOW_ADD_DEFINITION;
import static dev.ikm.komet.kview.events.ShowPatternPanelEvent.SHOW_ADD_FQN;
import static dev.ikm.komet.kview.events.ShowPatternPanelEvent.SHOW_ADD_OTHER_NAME;
import static dev.ikm.komet.kview.events.ShowPatternPanelEvent.SHOW_EDIT_FIELDS;
import static dev.ikm.komet.kview.events.pattern.PatternPropertyPanelEvent.CLOSE_PANEL;
import static dev.ikm.komet.kview.events.pattern.PatternPropertyPanelEvent.OPEN_PANEL;
import static dev.ikm.komet.kview.fxutils.SlideOutTrayHelper.isClosed;
import static dev.ikm.komet.kview.fxutils.SlideOutTrayHelper.isOpen;
import static dev.ikm.komet.kview.fxutils.SlideOutTrayHelper.slideIn;
import static dev.ikm.komet.kview.fxutils.SlideOutTrayHelper.slideOut;
import static dev.ikm.komet.kview.fxutils.TitledPaneHelper.putArrowOnRight;
import static dev.ikm.komet.kview.fxutils.ViewportHelper.clipChildren;
import static dev.ikm.komet.kview.mvvm.viewmodel.FormViewModel.VIEW_PROPERTIES;
import static dev.ikm.komet.kview.mvvm.viewmodel.PatternViewModel.FIELDS_COLLECTION;
import static dev.ikm.komet.kview.mvvm.viewmodel.PatternViewModel.FQN_DESCRIPTION_NAME_TEXT;
import static dev.ikm.komet.kview.mvvm.viewmodel.PatternViewModel.MEANING_DATE_STR;
import static dev.ikm.komet.kview.mvvm.viewmodel.PatternViewModel.MEANING_TEXT;
import static dev.ikm.komet.kview.mvvm.viewmodel.PatternViewModel.OTHER_NAME_DESCRIPTION_NAME_TEXT;
import static dev.ikm.komet.kview.mvvm.viewmodel.PatternViewModel.PATTERN_TOPIC;
import static dev.ikm.komet.kview.mvvm.viewmodel.PatternViewModel.PURPOSE_DATE_STR;
import static dev.ikm.komet.kview.mvvm.viewmodel.PatternViewModel.PURPOSE_TEXT;
import dev.ikm.komet.framework.events.EvtBusFactory;
import dev.ikm.komet.framework.events.EvtType;
import dev.ikm.komet.framework.events.Subscriber;
import dev.ikm.komet.framework.view.ViewProperties;
import dev.ikm.komet.kview.events.ShowPatternPanelEvent;
import dev.ikm.komet.kview.events.pattern.PatternDefinitionEvent;
import dev.ikm.komet.kview.events.pattern.PatternDescriptionEvent;
import dev.ikm.komet.kview.events.pattern.PatternFieldsPanelEvent;
import dev.ikm.komet.kview.events.pattern.PatternPropertyPanelEvent;
import dev.ikm.komet.kview.fxutils.MenuHelper;
import dev.ikm.komet.kview.mvvm.model.PatternField;
import dev.ikm.komet.kview.mvvm.viewmodel.PatternViewModel;
import javafx.beans.property.StringProperty;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.geometry.Side;
import javafx.scene.Node;
import javafx.scene.control.Button;
import javafx.scene.control.ContextMenu;
import javafx.scene.control.Label;
import javafx.scene.control.MenuItem;
import javafx.scene.control.TitledPane;
import javafx.scene.control.ToggleButton;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Pane;
import javafx.scene.layout.Region;
import javafx.scene.layout.TilePane;
import javafx.scene.layout.VBox;
import javafx.scene.text.Text;
import org.carlfx.cognitive.loader.Config;
import org.carlfx.cognitive.loader.FXMLMvvmLoader;
import org.carlfx.cognitive.loader.InjectViewModel;
import org.carlfx.cognitive.loader.JFXNode;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.net.URL;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.function.Consumer;

public class PatternDetailsController {

    private static final Logger LOG = LoggerFactory.getLogger(PatternDetailsController.class);

    public static final URL PATTERN_PROPERTIES_VIEW_FXML_URL = PatternDetailsController.class.getResource("pattern-properties.fxml");

    private Consumer<ToggleButton> reasonerResultsControllerConsumer;

    @FXML
    private BorderPane detailsOuterBorderPane;

    @FXML
    private ToggleButton propertiesToggleButton;

    /**
     * Used slide out the properties view
     */
    @FXML
    private Pane propertiesSlideoutTrayPane;

    @FXML
    private Pane timelineSlideoutTrayPane;

    @FXML
    private Label patternTitleText;

    @FXML
    private TitledPane patternDefinitionTitledPane;

    @FXML
    private TitledPane descriptionsTitledPane;

    @FXML
    private TitledPane fieldsTitledPane;

    private PropertiesController propertiesController;

    private BorderPane propertiesBorderPane;

    @FXML
    private Button addDefinitionButton;

    @FXML
    private Text semanticMeaningText;

    @FXML
    private Text semanticPurposeText;

    // pattern defintion fields
    @FXML
    private Text meaningText;

    @FXML
    private Text meaningDate;

    @FXML
    private Text purposeText;

    @FXML
    private Text purposeDate;

    // pattern description fields
    @FXML
    private Text fqnText; // fqn = fully qualified name

    @FXML
    private Text fqnDate;

    @FXML
    private Text otherNameText;

    @FXML
    private Text otherNameDate;

    @FXML
    private Button addDescriptionButton;

    @FXML
    private ContextMenu descriptionContextMenu;

    @FXML
    private MenuItem addFqnMenuItem;

    @FXML
    private MenuItem addOtherNameMenuItem;

    @FXML
    private Button editFieldsButton;

    @FXML
    private TilePane fieldsTilePane;

    @InjectViewModel
    private PatternViewModel patternViewModel;

    private Subscriber<PatternPropertyPanelEvent> patternPropertiesEventSubscriber;

    private Subscriber<PatternDefinitionEvent> patternDefinitionEventSubscriber;

    private Subscriber<PatternDescriptionEvent> patternDescriptionEventSubscriber;

    private Subscriber<PatternFieldsPanelEvent> patternFieldsPanelEventSubscriber;

    public PatternDetailsController() {}

    @FXML
    private void initialize() {
        fieldsTilePane.getChildren().clear();
        fieldsTilePane.setPrefColumns(2);

        // listen for open and close events
        patternPropertiesEventSubscriber = (evt) -> {
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
        EvtBusFactory.getDefaultEvtBus().subscribe(patternViewModel.getPropertyValue(PATTERN_TOPIC), PatternPropertyPanelEvent.class, patternPropertiesEventSubscriber);

        // capture pattern definition information
        purposeText.textProperty().bind(patternViewModel.getProperty(PURPOSE_TEXT));
        purposeText.getStyleClass().add("text-noto-sans-bold-grey-twelve");

        meaningText.textProperty().bind(patternViewModel.getProperty(MEANING_TEXT));
        meaningText.getStyleClass().add("text-noto-sans-bold-grey-twelve");

        meaningDate.textProperty().bind(patternViewModel.getProperty(MEANING_DATE_STR));
        purposeDate.textProperty().bind(patternViewModel.getProperty(PURPOSE_DATE_STR));

        patternDefinitionEventSubscriber = evt -> {
            patternViewModel.setPurposeAndMeaningText(evt.getPatternDefinition());
        };
        EvtBusFactory.getDefaultEvtBus().subscribe(patternViewModel.getPropertyValue(PATTERN_TOPIC), PatternDefinitionEvent.class, patternDefinitionEventSubscriber);

        meaningDate.getStyleClass().add("text-noto-sans-normal-grey-eight");
        purposeDate.getStyleClass().add("text-noto-sans-normal-grey-eight");

        // capture descriptions information
        StringProperty fqnTextProperty = patternViewModel.getProperty(FQN_DESCRIPTION_NAME_TEXT);
        fqnText.textProperty().bind(fqnTextProperty);
        StringProperty otTextProperty = patternViewModel.getProperty(OTHER_NAME_DESCRIPTION_NAME_TEXT);
        otherNameText.textProperty().bind(otTextProperty);
        patternDescriptionEventSubscriber = evt -> {
            if (evt.getEventType() == PatternDescriptionEvent.PATTERN_ADD_FQN) {
                patternViewModel.setFullyQualifiedName(evt.getDescrName());
            }
            if (evt.getEventType() == PatternDescriptionEvent.PATTERN_ADD_OTHER_NAME) {
                patternViewModel.setOtherNameText(evt.getDescrName());
            }
            String dateAddedStr = LocalDate.now().format(DateTimeFormatter.ofPattern("MMM d, yyyy")).toString();
            if (patternViewModel.getProperty(FQN_DESCRIPTION_NAME_TEXT) != null
                && !((String)patternViewModel.getPropertyValue(FQN_DESCRIPTION_NAME_TEXT)).isEmpty()) {
                fqnDate.setText("Date Added: " + dateAddedStr);
                fqnDate.getStyleClass().add("text-noto-sans-normal-grey-eight");
            } else {
                fqnDate.setText("");
            }
            if (patternViewModel.getProperty(OTHER_NAME_DESCRIPTION_NAME_TEXT) != null
                && !((String)patternViewModel.getPropertyValue(OTHER_NAME_DESCRIPTION_NAME_TEXT)).isEmpty()) {
                otherNameDate.setText("Date Added: " + dateAddedStr);
                otherNameDate.getStyleClass().add("text-noto-sans-normal-grey-eight");
            } else {
                otherNameDate.setText("");
            }
        };
        EvtBusFactory.getDefaultEvtBus().subscribe(patternViewModel.getPropertyValue(PATTERN_TOPIC), PatternDescriptionEvent.class, patternDescriptionEventSubscriber);

        patternFieldsPanelEventSubscriber = evt -> {
            List<PatternField> patternFieldList = patternViewModel.getObservableList(FIELDS_COLLECTION);
            patternFieldList.add(evt.getPatternField());
            addField(evt.getPatternField());
        };
        EvtBusFactory.getDefaultEvtBus().subscribe(patternViewModel.getPropertyValue(PATTERN_TOPIC), PatternFieldsPanelEvent.class, patternFieldsPanelEventSubscriber);

        // Setup Properties
        setupProperties();
    }

    private void addField(PatternField patternField) {
        int fieldNum = fieldsTilePane.getChildren().size() + 1;
        fieldsTilePane.getChildren().add(createFieldEntry(patternField, fieldNum));
    }

    private Node createFieldEntry(PatternField patternField, int fieldNum) {
        VBox fieldVBoxContainer = new VBox();
        fieldVBoxContainer.prefWidth(330);
        Label fieldLabel = new Label("Field " + fieldNum);
        Text fieldText = new Text(patternField.displayName());
        fieldText.getStyleClass().add("grey12-12pt-bold");
        HBox outerHBox = new HBox();
        outerHBox.setSpacing(8);
        HBox innerHBox = new HBox();
        Label dateAddedLabel = new Label("Date Added: ");
        String dateAddedStr = LocalDate.now().format(DateTimeFormatter.ofPattern("MMM d, yyyy")).toString();
        Label dateLabel = new Label(dateAddedStr);
        double dateWidth = 90;
        dateLabel.prefWidth(dateWidth);
        dateLabel.maxWidth(dateWidth);
        innerHBox.getChildren().addAll(dateAddedLabel, dateLabel);
        Region commentIconRegion = new Region();
        commentIconRegion.getStyleClass().add("grey-comment-icon");
        outerHBox.getChildren().addAll(innerHBox, commentIconRegion);
        fieldVBoxContainer.getChildren().addAll(fieldLabel, fieldText, outerHBox);
        return fieldVBoxContainer;
    }

    private void setupProperties() {
        // Setup Property screen bump out
        // Load Concept Properties View Panel (FXML & Controller)
        Config config = new Config(PATTERN_PROPERTIES_VIEW_FXML_URL)
                .updateViewModel("patternPropertiesViewModel",
                        (patternPropertiesViewModel) -> patternPropertiesViewModel
                                .setPropertyValue(PATTERN_TOPIC, patternViewModel.getPropertyValue(PATTERN_TOPIC))
                                .setPropertyValue(VIEW_PROPERTIES, patternViewModel.getPropertyValue(VIEW_PROPERTIES) ));

        JFXNode<BorderPane, PropertiesController> propsFXMLLoader = FXMLMvvmLoader.make(config);
        this.propertiesBorderPane = propsFXMLLoader.node();
        this.propertiesController = propsFXMLLoader.controller();



        attachPropertiesViewSlideoutTray(this.propertiesBorderPane);
    }

    public ViewProperties getViewProperties() {
        return getPatternViewModel().getPropertyValue(VIEW_PROPERTIES);
    }

    private PatternViewModel getPatternViewModel() {
        return patternViewModel;
    }

    private Consumer<PatternDetailsController> onCloseConceptWindow;

    public void setOnCloseConceptWindow(Consumer<PatternDetailsController> onClose) {
        this.onCloseConceptWindow = onClose;
    }

    public void onReasonerSlideoutTray(Consumer<ToggleButton> reasonerResultsControllerConsumer) {
        this.reasonerResultsControllerConsumer = reasonerResultsControllerConsumer;
    }

    @FXML
    void closeConceptWindow(ActionEvent event) {
        LOG.info("Cleanup occurring: Closing Window with pattern: " + patternTitleText.getText());
        if (this.onCloseConceptWindow != null) {
            onCloseConceptWindow.accept(this);
        }
        Pane parent = (Pane) detailsOuterBorderPane.getParent();
        parent.getChildren().remove(detailsOuterBorderPane);
    }

    @FXML
    private void showEditView(ActionEvent actionEvent) {
        // put the edit view in the properties pane
    }

    @FXML
    private void showAddDefinitionPanel(ActionEvent actionEvent) {
        // Todo show bump out and display Edit Description panel
        LOG.info("Todo show bump out and display Edit Definition panel \n" + actionEvent);
        // publish property open.
        EvtBusFactory.getDefaultEvtBus().publish(patternViewModel.getPropertyValue(PATTERN_TOPIC), new ShowPatternPanelEvent(actionEvent.getSource(), SHOW_ADD_DEFINITION));

        EvtBusFactory.getDefaultEvtBus().publish(patternViewModel.getPropertyValue(PATTERN_TOPIC), new PatternPropertyPanelEvent(actionEvent.getSource(), OPEN_PANEL));
    }


    @FXML
    private void showEditFieldsPanel(ActionEvent actionEvent) {
        LOG.info("Todo show bump out and display Edit Fields panel \n" + actionEvent);

        EvtBusFactory.getDefaultEvtBus().publish(patternViewModel.getPropertyValue(PATTERN_TOPIC), new ShowPatternPanelEvent(actionEvent.getSource(), SHOW_EDIT_FIELDS));

        EvtBusFactory.getDefaultEvtBus().publish(patternViewModel.getPropertyValue(PATTERN_TOPIC), new PatternPropertyPanelEvent(actionEvent.getSource(), OPEN_PANEL));
    }

    @FXML
    private void popupAddDescriptionContextMenu(ActionEvent actionEvent) {
        MenuHelper.fireContextMenuEvent(actionEvent, Side.RIGHT, 0, 0);
    }

    @FXML
    private void showAddFqnPanel(ActionEvent actionEvent) {
        LOG.info("Bumpout Add FQN panel \n" + actionEvent);
        EvtBusFactory.getDefaultEvtBus().publish(patternViewModel.getPropertyValue(PATTERN_TOPIC), new ShowPatternPanelEvent(actionEvent.getSource(), SHOW_ADD_FQN));
        EvtBusFactory.getDefaultEvtBus().publish(patternViewModel.getPropertyValue(PATTERN_TOPIC), new PatternPropertyPanelEvent(actionEvent.getSource(), OPEN_PANEL));
    }
    @FXML
    private void showAddOtherNamePanel(ActionEvent actionEvent) {
        LOG.info("Bumpout Add Other name panel \n" + actionEvent);
        EvtBusFactory.getDefaultEvtBus().publish(patternViewModel.getPropertyValue(PATTERN_TOPIC), new ShowPatternPanelEvent(actionEvent.getSource(), SHOW_ADD_OTHER_NAME));
        EvtBusFactory.getDefaultEvtBus().publish(patternViewModel.getPropertyValue(PATTERN_TOPIC), new PatternPropertyPanelEvent(actionEvent.getSource(), OPEN_PANEL));
    }

    @FXML
    private void openReasonerSlideout(ActionEvent event) {
        LOG.info("not implemented yet");
//        ToggleButton reasonerToggle = (ToggleButton) event.getSource();
//        reasonerResultsControllerConsumer.accept(reasonerToggle);
    }

    @FXML
    public void popupStampEdit(ActionEvent event) {
        //TODO implement this method
    }

    @FXML
    private void openTimelinePanel(ActionEvent event) {
        LOG.info("not implemented yet");
//        ToggleButton timelineToggle = (ToggleButton) event.getSource();
//        // if selected open properties
//        if (timelineToggle.isSelected()) {
//            LOG.info("Opening slideout of timeline panel");
//            slideOut(timelineSlideoutTrayPane, detailsOuterBorderPane);
//        } else {
//            LOG.info("Close Properties timeline panel");
//            slideIn(timelineSlideoutTrayPane, detailsOuterBorderPane);
//        }
    }

    @FXML
    private void openPropertiesPanel(ActionEvent event) {
        ToggleButton propertyToggle = (ToggleButton) event.getSource();
        EvtType<PatternPropertyPanelEvent> eventEvtType = propertyToggle.isSelected() ? OPEN_PANEL : CLOSE_PANEL;
        EvtBusFactory.getDefaultEvtBus().publish(patternViewModel.getPropertyValue(PATTERN_TOPIC), new PatternPropertyPanelEvent(propertyToggle, eventEvtType));
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

    public void updateView() {
    }

    public void putTitlePanesArrowOnRight() {
        putArrowOnRight(this.patternDefinitionTitledPane);
        putArrowOnRight(this.descriptionsTitledPane);
        putArrowOnRight(this.fieldsTitledPane);
    }


}
