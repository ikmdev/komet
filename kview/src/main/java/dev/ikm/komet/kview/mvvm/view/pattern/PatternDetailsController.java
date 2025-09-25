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


import static dev.ikm.komet.kview.controls.KometIcon.IconValue.PLUS;
import static dev.ikm.komet.kview.events.ClosePropertiesPanelEvent.CLOSE_PROPERTIES;
import static dev.ikm.komet.kview.events.EventTopics.SAVE_PATTERN_TOPIC;
import static dev.ikm.komet.kview.events.pattern.MakePatternWindowEvent.OPEN_PATTERN;
import static dev.ikm.komet.kview.events.pattern.PatternDescriptionEvent.PATTERN_EDIT_OTHER_NAME;
import static dev.ikm.komet.kview.events.pattern.PatternFieldsPanelEvent.EDIT_FIELD;
import static dev.ikm.komet.kview.events.pattern.PropertyPanelEvent.CLOSE_PANEL;
import static dev.ikm.komet.kview.events.pattern.PropertyPanelEvent.OPEN_PANEL;
import static dev.ikm.komet.kview.events.pattern.ShowPatternFormInBumpOutEvent.SHOW_ADD_DEFINITION;
import static dev.ikm.komet.kview.events.pattern.ShowPatternFormInBumpOutEvent.SHOW_ADD_FIELDS;
import static dev.ikm.komet.kview.events.pattern.ShowPatternFormInBumpOutEvent.SHOW_ADD_FQN;
import static dev.ikm.komet.kview.events.pattern.ShowPatternFormInBumpOutEvent.SHOW_ADD_OTHER_NAME;
import static dev.ikm.komet.kview.events.pattern.ShowPatternFormInBumpOutEvent.SHOW_EDIT_DEFINITION;
import static dev.ikm.komet.kview.events.pattern.ShowPatternFormInBumpOutEvent.SHOW_EDIT_FIELDS;
import static dev.ikm.komet.kview.events.pattern.ShowPatternFormInBumpOutEvent.SHOW_EDIT_FQN;
import static dev.ikm.komet.kview.events.pattern.ShowPatternFormInBumpOutEvent.SHOW_EDIT_OTHER_NAME;
import static dev.ikm.komet.kview.fxutils.IconsHelper.IconType.ATTACHMENT;
import static dev.ikm.komet.kview.fxutils.IconsHelper.IconType.COMMENTS;
import static dev.ikm.komet.kview.fxutils.SlideOutTrayHelper.isClosed;
import static dev.ikm.komet.kview.fxutils.SlideOutTrayHelper.isOpen;
import static dev.ikm.komet.kview.fxutils.SlideOutTrayHelper.slideIn;
import static dev.ikm.komet.kview.fxutils.SlideOutTrayHelper.slideOut;
import static dev.ikm.komet.kview.fxutils.TitledPaneHelper.putArrowOnRight;
import static dev.ikm.komet.kview.fxutils.ViewportHelper.clipChildren;
import static dev.ikm.komet.kview.fxutils.window.DraggableSupport.addDraggableNodes;
import static dev.ikm.komet.kview.fxutils.window.DraggableSupport.removeDraggableNodes;
import static dev.ikm.komet.kview.mvvm.model.DragAndDropType.CONCEPT;
import static dev.ikm.komet.kview.mvvm.model.DragAndDropType.SEMANTIC;
import static dev.ikm.komet.kview.mvvm.view.common.SVGConstants.DUPLICATE_SVG_PATH;
import static dev.ikm.komet.kview.mvvm.viewmodel.FormViewModel.CURRENT_JOURNAL_WINDOW_TOPIC;
import static dev.ikm.komet.kview.mvvm.viewmodel.FormViewModel.EDIT;
import static dev.ikm.komet.kview.mvvm.viewmodel.FormViewModel.MODE;
import static dev.ikm.komet.kview.mvvm.viewmodel.FormViewModel.VIEW_PROPERTIES;
import static dev.ikm.komet.kview.mvvm.viewmodel.PatternViewModel.CREATE;
import static dev.ikm.komet.kview.mvvm.viewmodel.PatternViewModel.FIELDS_COLLECTION;
import static dev.ikm.komet.kview.mvvm.viewmodel.PatternViewModel.FQN_CASE_SIGNIFICANCE;
import static dev.ikm.komet.kview.mvvm.viewmodel.PatternViewModel.FQN_DESCRIPTION_NAME;
import static dev.ikm.komet.kview.mvvm.viewmodel.PatternViewModel.FQN_DESCRIPTION_NAME_TEXT;
import static dev.ikm.komet.kview.mvvm.viewmodel.PatternViewModel.FQN_LANGUAGE;
import static dev.ikm.komet.kview.mvvm.viewmodel.PatternViewModel.IS_INVALID;
import static dev.ikm.komet.kview.mvvm.viewmodel.PatternViewModel.MEANING_ENTITY;
import static dev.ikm.komet.kview.mvvm.viewmodel.PatternViewModel.MEANING_TEXT;
import static dev.ikm.komet.kview.mvvm.viewmodel.PatternViewModel.OTHER_NAMES;
import static dev.ikm.komet.kview.mvvm.viewmodel.PatternViewModel.PATTERN;
import static dev.ikm.komet.kview.mvvm.viewmodel.PatternViewModel.PATTERN_TITLE_TEXT;
import static dev.ikm.komet.kview.mvvm.viewmodel.PatternViewModel.PATTERN_TOPIC;
import static dev.ikm.komet.kview.mvvm.viewmodel.PatternViewModel.PUBLISH_PENDING;
import static dev.ikm.komet.kview.mvvm.viewmodel.PatternViewModel.PURPOSE_ENTITY;
import static dev.ikm.komet.kview.mvvm.viewmodel.PatternViewModel.PURPOSE_TEXT;
import static dev.ikm.komet.kview.mvvm.viewmodel.PatternViewModel.SELECTED_PATTERN_FIELD;
import static dev.ikm.komet.kview.mvvm.viewmodel.PatternViewModel.STAMP_VIEW_MODEL;
import static dev.ikm.komet.kview.mvvm.viewmodel.PatternViewModel.STATE_MACHINE;
import static dev.ikm.komet.kview.mvvm.viewmodel.stamp.StampFormViewModelBase.Properties.AUTHOR;
import static dev.ikm.komet.kview.mvvm.viewmodel.stamp.StampFormViewModelBase.Properties.FORM_TIME_TEXT;
import static dev.ikm.komet.kview.mvvm.viewmodel.stamp.StampFormViewModelBase.Properties.IS_CONFIRMED_OR_SUBMITTED;
import static dev.ikm.komet.kview.mvvm.viewmodel.stamp.StampFormViewModelBase.Properties.MODULE;
import static dev.ikm.komet.kview.mvvm.viewmodel.stamp.StampFormViewModelBase.Properties.PATH;
import static dev.ikm.komet.kview.mvvm.viewmodel.stamp.StampFormViewModelBase.Properties.STATUS;
import static dev.ikm.tinkar.common.service.PrimitiveData.PREMUNDANE_TIME;
import static dev.ikm.tinkar.common.util.time.DateTimeUtil.PREMUNDANE;
import dev.ikm.komet.framework.Identicon;
import dev.ikm.komet.framework.dnd.DragImageMaker;
import dev.ikm.komet.framework.dnd.KometClipboard;
import dev.ikm.komet.framework.view.ViewMenuModel;
import dev.ikm.komet.framework.view.ViewProperties;
import dev.ikm.komet.kview.common.ViewCalculatorUtils;
import dev.ikm.komet.kview.controls.KometIcon;
import dev.ikm.komet.kview.controls.PublicIDListControl;
import dev.ikm.komet.kview.controls.StampViewControl;
import dev.ikm.komet.kview.events.ClosePropertiesPanelEvent;
import dev.ikm.komet.kview.events.StampEvent;
import dev.ikm.komet.kview.events.genediting.MakeGenEditingWindowEvent;
import dev.ikm.komet.kview.events.pattern.MakePatternWindowEvent;
import dev.ikm.komet.kview.events.pattern.PatternDefinitionEvent;
import dev.ikm.komet.kview.events.pattern.PatternDescriptionEvent;
import dev.ikm.komet.kview.events.pattern.PatternFieldsPanelEvent;
import dev.ikm.komet.kview.events.pattern.PatternSavedEvent;
import dev.ikm.komet.kview.events.pattern.PropertyPanelEvent;
import dev.ikm.komet.kview.events.pattern.ShowPatternFormInBumpOutEvent;
import dev.ikm.komet.kview.fxutils.IconsHelper;
import dev.ikm.komet.kview.fxutils.MenuHelper;
import dev.ikm.komet.kview.fxutils.SlideOutTrayHelper;
import dev.ikm.komet.kview.mvvm.model.DescrName;
import dev.ikm.komet.kview.mvvm.model.DragAndDropInfo;
import dev.ikm.komet.kview.mvvm.model.DragAndDropType;
import dev.ikm.komet.kview.mvvm.model.PatternDefinition;
import dev.ikm.komet.kview.mvvm.model.PatternField;
import dev.ikm.komet.kview.mvvm.view.journal.VerticallyFilledPane;
import dev.ikm.komet.kview.mvvm.viewmodel.PatternViewModel;
import dev.ikm.komet.kview.mvvm.viewmodel.stamp.StampFormViewModelBase;
import dev.ikm.tinkar.coordinate.stamp.calculator.Latest;
import dev.ikm.tinkar.coordinate.view.calculator.ViewCalculator;
import dev.ikm.tinkar.entity.Entity;
import dev.ikm.tinkar.entity.EntityVersion;
import dev.ikm.tinkar.events.EvtBusFactory;
import dev.ikm.tinkar.events.EvtType;
import dev.ikm.tinkar.events.Subscriber;
import dev.ikm.tinkar.terms.ConceptFacade;
import dev.ikm.tinkar.terms.EntityFacade;
import dev.ikm.tinkar.terms.PatternFacade;
import dev.ikm.tinkar.terms.SemanticFacade;
import dev.ikm.tinkar.terms.State;
import javafx.beans.binding.Bindings;
import javafx.beans.binding.BooleanBinding;
import javafx.beans.property.BooleanProperty;
import javafx.beans.property.ObjectProperty;
import javafx.beans.property.SimpleStringProperty;
import javafx.collections.ListChangeListener;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.fxml.FXML;
import javafx.geometry.Bounds;
import javafx.geometry.Side;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.control.Button;
import javafx.scene.control.ContextMenu;
import javafx.scene.control.Hyperlink;
import javafx.scene.control.Label;
import javafx.scene.control.MenuButton;
import javafx.scene.control.MenuItem;
import javafx.scene.control.TitledPane;
import javafx.scene.control.ToggleButton;
import javafx.scene.control.Tooltip;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.input.Dragboard;
import javafx.scene.input.MouseEvent;
import javafx.scene.input.TransferMode;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Pane;
import javafx.scene.layout.Region;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.TilePane;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Color;
import javafx.scene.shape.FillRule;
import javafx.scene.shape.SVGPath;
import javafx.scene.text.Text;
import javafx.scene.text.TextFlow;
import org.carlfx.axonic.StateMachine;
import org.carlfx.cognitive.loader.Config;
import org.carlfx.cognitive.loader.FXMLMvvmLoader;
import org.carlfx.cognitive.loader.InjectViewModel;
import org.carlfx.cognitive.loader.JFXNode;
import org.carlfx.cognitive.loader.NamedVm;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.net.URL;
import java.time.Instant;
import java.time.LocalDate;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.time.format.FormatStyle;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.Objects;
import java.util.function.Consumer;

public class PatternDetailsController {

    private static final Logger LOG = LoggerFactory.getLogger(PatternDetailsController.class);

    public static final URL PATTERN_PROPERTIES_VIEW_FXML_URL = PatternDetailsController.class.getResource("pattern-properties.fxml");

    private Consumer<ToggleButton> reasonerResultsControllerConsumer;

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
    private VerticallyFilledPane timelineSlideoutTrayPane;

    @FXML
    private ImageView identiconImageView;

    @FXML
    private Label patternTitleText;

    @FXML
    private PublicIDListControl identifierControl;

    @FXML
    private TextFlow latestFqnTextFlow;

    @FXML
    StampViewControl stampViewControl;

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
    private Text semanticPurposeText;

    @FXML
    private Button savePatternButton;

    @FXML
    private StackPane publishStackPane;

    // pattern definition fields
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
    private Text latestFqnText; // fqn = fully qualified name

    @FXML
    private Text fqnDescriptionSemanticText;

    @FXML
    private Label fqnAddDateLabel;

    @FXML
    private VBox otherNamesVBox;

    @FXML
    private Button addDescriptionButton;

    @FXML
    private ContextMenu descriptionContextMenu;

    @FXML
    private MenuItem addFqnMenuItem;

    @FXML
    private MenuItem addOtherNameMenuItem;

    @FXML
    private Button addFieldsButton;

    @FXML
    private TilePane fieldsTilePane;

    @FXML
    private ContextMenu contextMenu;

    @FXML
    private HBox tabHeader;

    @FXML
    private HBox conceptHeaderControlToolBarHbox;

    @InjectViewModel
    private PatternViewModel patternViewModel;

    private boolean isUpdatingStampSelection = false;

    private final Tooltip publishTooltip = new Tooltip();

    private Subscriber<PropertyPanelEvent> patternPropertiesEventSubscriber;

    private Subscriber<PatternDefinitionEvent> patternDefinitionEventSubscriber;

    private Subscriber<PatternDescriptionEvent> patternDescriptionEventSubscriber;

    private Subscriber<PatternFieldsPanelEvent> patternFieldsPanelEventSubscriber;

    private Subscriber<ClosePropertiesPanelEvent> closePropertiesPanelEventSubscriber;

    public PatternDetailsController() {}

    @FXML
    private void initialize() {
        purposeText.setText("");
        meaningText.setText("");
        fqnAddDateLabel.setText("");
        fieldsTilePane.getChildren().clear();
        fieldsTilePane.setPrefColumns(2);
        otherNamesVBox.getChildren().clear();

        stampViewControl.selectedProperty().subscribe(this::onStampSelectionChanged);

        //DragNDrop feature for Semantic Purpose and Semantic Meaning
        setUpDraggable(purposeText, patternViewModel.getProperty(PURPOSE_ENTITY));
        setUpDraggable(meaningText, patternViewModel.getProperty(MEANING_ENTITY));

        setUpAddSemanticMenu();

        // Bind the Publish button's disable property to the ViewModel
        BooleanProperty newChangeProp = patternViewModel.getProperty(PUBLISH_PENDING);
        publishTooltip.textProperty().bind(Bindings.when(savePatternButton.disableProperty())
                .then("Publish: Disabled")
                .otherwise("Submit"));

        // Assign the tooltip to the StackPane (container of Publish button)
        setupTooltipForDisabledButton(savePatternButton);

        // Disable button when not valid or newChangeProp is false.
        savePatternButton.disableProperty().bind(patternViewModel.getBooleanProperty(IS_INVALID).or(newChangeProp.not()));

        // listen for open and close events
        patternPropertiesEventSubscriber = (evt) -> {
            if (evt.getEventType() == CLOSE_PANEL) {
                LOG.info("propBumpOutListener - Close Properties bumpout toggle = " + propertiesToggleButton.isSelected());
                propertiesToggleButton.setSelected(false);
                if (isOpen(propertiesSlideoutTrayPane)) {
                    slideIn(propertiesSlideoutTrayPane, detailsOuterBorderPane);
                }

                updateDraggableNodesForPropertiesPanel(false);
            } else if (evt.getEventType() == OPEN_PANEL) {
                LOG.info("propBumpOutListener - Opening Properties bumpout toggle = " + propertiesToggleButton.isSelected());
                propertiesToggleButton.setSelected(true);
                if (isClosed(propertiesSlideoutTrayPane)) {
                    slideOut(propertiesSlideoutTrayPane, detailsOuterBorderPane);
                }

                updateDraggableNodesForPropertiesPanel(true);
            }
        };
        EvtBusFactory.getDefaultEvtBus().subscribe(patternViewModel.getPropertyValue(PATTERN_TOPIC), PropertyPanelEvent.class, patternPropertiesEventSubscriber);

        patternDefinitionEventSubscriber = evt -> patternViewModel.setPurposeAndMeaningText(evt.getPatternDefinition());

        EvtBusFactory.getDefaultEvtBus().subscribe(patternViewModel.getPropertyValue(PATTERN_TOPIC), PatternDefinitionEvent.class, patternDefinitionEventSubscriber);


        // Update Other names section based on changes in List.
        ObservableList<DescrName> descrNameObservableList = patternViewModel.getObservableList(OTHER_NAMES);
        descrNameObservableList.addListener(new ListChangeListener<DescrName>() {
            @Override
            public void onChanged(Change<? extends DescrName> change) {
                while(change.next()){
                    if(change.wasAdded()){
                        DescrName descrName = change.getAddedSubList().getFirst();
                        List<TextFlow> rows = generateOtherNameRow(descrName);
                        otherNamesVBox.getChildren().addAll(rows);
                    } else if (change.wasRemoved()) {
                        //when the modified record is removed from list, there is no easy way to track it in the VBOX.
                        // Hence, we recreate set all the records.
                        List<TextFlow> rows = generateOtherNameRows();
                        otherNamesVBox.getChildren().setAll(rows);
                    }
                }
            }
        });

        // This will listen to the pattern descriptions event. Adding a FQN version, Adding other name.
        patternDescriptionEventSubscriber = evt -> {
            DescrName descrName = evt.getDescrName();
            StateMachine patternSM = patternViewModel.getPropertyValue(STATE_MACHINE);
            if (evt.getEventType() == PatternDescriptionEvent.PATTERN_ADD_FQN) {
                // This if is invoked when the data is coming from FQN name screen.
                patternViewModel.setPropertyValue(FQN_DESCRIPTION_NAME_TEXT, descrName.getNameText());
                patternViewModel.setPropertyValue(FQN_DESCRIPTION_NAME, descrName);
                patternViewModel.setPropertyValue(FQN_CASE_SIGNIFICANCE, descrName.getCaseSignificance());
                patternViewModel.setPropertyValue(FQN_LANGUAGE, descrName.getLanguage());
                patternSM.t("fqnDone");
            } else if (evt.getEventType() == PatternDescriptionEvent.PATTERN_ADD_OTHER_NAME) {
                // This if is invoked when the data is coming from Other Name screen.
                descrNameObservableList.add(evt.getDescrName());
                patternSM.t("otherNameDone");
            }else if(evt.getEventType() == PATTERN_EDIT_OTHER_NAME){
                // triggers the OTHER_NAME list listener to clear the UI and
                // add back all the items back from the list except for the one removed.
                descrNameObservableList.remove(evt.getDescrName());
                // add the modified item back to the UI by
                descrNameObservableList.add(evt.getDescrName());
                patternSM.t("otherNameDone");
            }
            patternViewModel.setPropertyValue(PUBLISH_PENDING, true);
        };
        EvtBusFactory.getDefaultEvtBus().subscribe(patternViewModel.getPropertyValue(PATTERN_TOPIC), PatternDescriptionEvent.class, patternDescriptionEventSubscriber);

        // bind view model
        if (!patternTitleText.textProperty().isBound()) {
            patternTitleText.textProperty().bind(patternViewModel.getProperty(PATTERN_TITLE_TEXT));
        }

        // set the identicon
        ObjectProperty<EntityFacade> patternProperty = patternViewModel.getProperty(PATTERN);

        patternProperty.subscribe(entityFacade -> {
            if (propertiesController != null) {
                propertiesController.updateModel(entityFacade);
            }

            if (entityFacade != null) {
                patternViewModel.setPropertyValue(MODE, EDIT);

                // dynamically update the identicon image.
                Image identicon = Identicon.generateIdenticonImage(entityFacade.publicId());
                identiconImageView.setImage(identicon);
            } else {
                patternViewModel.setPropertyValue(MODE, CREATE);
            }
        });

        updateDisplayIdentifier();

        // capture pattern definition information
        purposeText.textProperty().bind(patternViewModel.getProperty(PURPOSE_TEXT));
        purposeText.getStyleClass().add("text-noto-sans-bold-grey-twelve");

        meaningText.textProperty().bind(patternViewModel.getProperty(MEANING_TEXT));
        meaningText.getStyleClass().add("text-noto-sans-bold-grey-twelve");

        // capture descriptions information
        latestFqnText.textProperty().bind(patternViewModel.getProperty(FQN_DESCRIPTION_NAME_TEXT));
        // Bind FQN property with description text, date and FQN menu item.
        ObjectProperty<DescrName> fqnNameProp = patternViewModel.getProperty(FQN_DESCRIPTION_NAME);
        // Generate description semantic and show
        fqnDescriptionSemanticText.textProperty().bind(fqnNameProp.map(descrName -> " (%s)".formatted(generateDescriptionSemantics(descrName))).orElse(""));

        fqnNameProp.subscribe(descrName -> {
            if (descrName != null && descrName.getSemanticPublicId() != null) {
                Latest<EntityVersion> semanticVersionLatest = getViewProperties().calculator().latest(Entity.nid(descrName.getSemanticPublicId()));
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
                    fqnAddDateLabel.setText(dateText);
                });
            }
        });

        // hide menu item if FQN is added.
        addFqnMenuItem.visibleProperty().bind(fqnNameProp.isNull());
        latestFqnTextFlow.setOnMouseClicked(mouseEvent -> {
            EvtBusFactory.getDefaultEvtBus().publish(patternViewModel.getPropertyValue(PATTERN_TOPIC), new ShowPatternFormInBumpOutEvent(mouseEvent.getSource(), SHOW_EDIT_FQN, fqnNameProp.getValue()));
            EvtBusFactory.getDefaultEvtBus().publish(patternViewModel.getPropertyValue(PATTERN_TOPIC), new PropertyPanelEvent(mouseEvent.getSource(), OPEN_PANEL));
        });

        //Listen to the changes in the fieldsTilePane and update the field numbers.
        ObservableList<Node> fieldsTilePaneList = fieldsTilePane.getChildren();
        fieldsTilePaneList.addListener((ListChangeListener<Node>) (listener) -> {
            while (listener.next()) {
                if (listener.wasAdded() || listener.wasRemoved()) {
                    updateFieldValues();
                }
            }
        });
        ObservableList<PatternField> patternFieldList = patternViewModel.getObservableList(FIELDS_COLLECTION);
        patternFieldsPanelEventSubscriber = evt -> {
            PatternField patternField = evt.getPatternField();
            PatternField previousPatternField = evt.getPreviousPatternField();
            int fieldPosition = evt.getCurrentFieldOrder() - 1;
            if (evt.getEventType() == EDIT_FIELD && previousPatternField != null) {
                // 1st remove it from list before adding the new entry
                patternFieldList.remove(previousPatternField);
            }
            // Update the fields collection data.
            patternFieldList.add(fieldPosition, patternField);
            // save and therefore validate
            patternViewModel.save();
            patternViewModel.setPropertyValue(PUBLISH_PENDING, true);
        };
        EvtBusFactory.getDefaultEvtBus().subscribe(patternViewModel.getPropertyValue(PATTERN_TOPIC), PatternFieldsPanelEvent.class, patternFieldsPanelEventSubscriber);

        patternFieldList.addListener((ListChangeListener<? super PatternField>) changeListener -> {
            while (changeListener.next()) {
                // when the collection is cleared, the removed size will equal the tile pane size, so clear the tile pane
                // since the changeListner.wasRemoved() will not account for clearing all of them if the collection is cleared
                if (changeListener.getRemovedSize() > 0 && changeListener.getRemovedSize() == fieldsTilePaneList.size()) {
                    fieldsTilePaneList.clear();
                } else {
                    if (changeListener.wasAdded()) {
                        int fieldPosition = changeListener.getTo() - 1;
                        // update the display.
                        fieldsTilePane.getChildren().add(fieldPosition, createFieldEntry(changeListener.getAddedSubList().getFirst(), changeListener.getTo()));
                    } else if (changeListener.wasRemoved()) {
                        fieldsTilePaneList.remove(changeListener.getTo());

                    }
                    patternViewModel.save();
                }
            }
        });

        // if a pattern is already in the database ( e.g we are not in create mode)
        // than the user should not be able to add editional fields. only edit existing once
        SimpleStringProperty mode =  patternViewModel.getProperty(MODE);
        BooleanBinding patternNotInCreateMode = Bindings.notEqual(mode, CREATE);

        addFieldsButton.disableProperty().bind(patternNotInCreateMode);

        // if the user clicks the Close Properties Button from the Edit Descriptions panel
        // in that state, the properties bump out will be slid out, therefore firing will perform a slide in
        closePropertiesPanelEventSubscriber = evt -> propertiesToggleButton.fire();
        EvtBusFactory.getDefaultEvtBus().subscribe(patternViewModel.getPropertyValue(PATTERN_TOPIC), ClosePropertiesPanelEvent.class, closePropertiesPanelEventSubscriber);

        // Setup Properties
        setupProperties();
        setupFilterCoordinatesMenu();

        // Setup window support with explicit draggable nodes
        addDraggableNodes(detailsOuterBorderPane, tabHeader, conceptHeaderControlToolBarHbox);

        // Check if the properties panel is initially open and add draggable nodes if needed
        if (propertiesToggleButton.isSelected() || isOpen(propertiesSlideoutTrayPane)) {
            updateDraggableNodesForPropertiesPanel(true);
        }
    }

    private void onStampSelectionChanged() {
        if (isUpdatingStampSelection) {
            return;
        }

        if (stampViewControl.isSelected()) {
            if (!propertiesToggleButton.isSelected()) {
                propertiesToggleButton.fire();
            }
            if (CREATE.equals(patternViewModel.getPropertyValue(MODE))) {
                EvtBusFactory.getDefaultEvtBus().publish(patternViewModel.getPropertyValue(PATTERN_TOPIC), new StampEvent(stampViewControl, StampEvent.CREATE_STAMP));
            } else {
                EvtBusFactory.getDefaultEvtBus().publish(patternViewModel.getPropertyValue(PATTERN_TOPIC), new StampEvent(stampViewControl, StampEvent.ADD_STAMP));
            }
        } else {
            EvtBusFactory.getDefaultEvtBus().publish(patternViewModel.getPropertyValue(PATTERN_TOPIC), new ClosePropertiesPanelEvent(stampViewControl, CLOSE_PROPERTIES));
        }
    }

    /// Show the public ID
    private void updateDisplayIdentifier() {
        ViewCalculator viewCalculator = getViewProperties().calculator();
        PatternFacade patternFacade = (PatternFacade) patternViewModel.getProperty(PATTERN).getValue();

        if (patternFacade != null) {
            identifierControl.updatePublicIdList(viewCalculator, patternFacade);
        }
    }

    private DragAndDropType getDragAndDropType(EntityFacade entityFacade) {
        return switch (entityFacade){
            case ConceptFacade conceptFacade -> CONCEPT;
            case SemanticFacade semanticFacade -> SEMANTIC;
            case PatternFacade patternFacade -> DragAndDropType.PATTERN;
            default -> throw new IllegalStateException("Unexpected value: " + entityFacade);
        };
    }

    /**
     * Configures the specified {@link Node} to support drag-and-drop operations associated with the given {@link EntityFacade}.
     * <p>
     * When a drag is detected on the node, this method initializes a dragboard with the entity's identifier and
     * sets a custom drag image for visual feedback.
     * </p>
     *
     * @param node   the JavaFX {@link Node} to be made draggable
     * @param entityProperty the {@link EntityFacade} associated with the node, providing data for the drag operation
     * @throws NullPointerException if either {@code node} or {@code entity} is {@code null}
     */
    private void setUpDraggable(Node node, ObjectProperty<EntityFacade> entityProperty) {
        Objects.requireNonNull(node, "The node must not be null.");

        // Set up the drag detection event handler
        node.setOnDragDetected(mouseEvent -> {
            if (entityProperty.isNull().get()) {
                mouseEvent.consume();
                return;
            }
            EntityFacade entityFacade = entityProperty.get();

            // Initiate a drag-and-drop gesture with copy or move transfer mode
            Dragboard dragboard = node.startDragAndDrop(TransferMode.COPY_OR_MOVE);

            // Create the content to be placed on the dragboard
            // Here, KometClipboard is used to encapsulate the entity's unique identifier (nid)
            KometClipboard content = new KometClipboard(EntityFacade.make(entityFacade.nid()));

            DragAndDropType dropType = getDragAndDropType(entityFacade);
            node.setUserData(new DragAndDropInfo(dropType, entityFacade.publicId()));

            // Generate the drag image using DragImageMaker
            DragImageMaker dragImageMaker = new DragImageMaker(node);
            Image dragImage = dragImageMaker.getDragImage();
            // Set the drag image on the dragboard
            if (dragImage != null) {
                dragboard.setDragView(dragImage);
            }

            // Place the content on the dragboard
            dragboard.setContent(content);

            // Log the drag event details for debugging or auditing
            LOG.info("Drag detected on node: " + mouseEvent.toString());

            // Consume the mouse event to prevent further processing
            mouseEvent.consume();
        });
    }

    /**
     * Creates the filter coordinates menu using the view calculator.
     * TODO Note that this is not a working menu, this is the first step to have propagating, inherited, filter coordinates
     * in the window/node hierarchy.
     */
    public void setupFilterCoordinatesMenu() {
        this.viewMenuModel = new ViewMenuModel(patternViewModel.getViewProperties(), coordinatesMenuButton, "PatternDetailsController");
    }

    private void setUpAddSemanticMenu() {
        contextMenu = new ContextMenu();
        contextMenu.setHideOnEscape(true);
        KometIcon kometPlusIcon = KometIcon.create(PLUS,"icon-klcontext-menu");
        MenuItem addNewSemanticElement = new MenuItem("Add New Semantic Element",kometPlusIcon);
        contextMenu.getItems().addAll(addNewSemanticElement);
        this.contextMenu.getStyleClass().add("klcontext-menu");
        detailsOuterBorderPane.setOnContextMenuRequested(contextMenuEvent -> {
            contextMenu.show(detailsOuterBorderPane.getScene().getWindow(), contextMenuEvent.getScreenX(), contextMenuEvent.getScreenY());
            contextMenuEvent.consume();
        });
        addNewSemanticElement.setOnAction(actionEvent -> {
            EntityFacade patternFacade = patternViewModel.getPropertyValue(PATTERN);
            LOG.info("Summon create new Semantic Element. " + patternFacade.description());
            EvtBusFactory.getDefaultEvtBus().publish(patternViewModel.getPropertyValue(CURRENT_JOURNAL_WINDOW_TOPIC),
                    new MakeGenEditingWindowEvent(this,
                            MakeGenEditingWindowEvent.OPEN_GEN_AUTHORING, patternFacade, patternViewModel.getPropertyValue(VIEW_PROPERTIES)));
        });
    }

    private List<TextFlow> generateOtherNameRows() {
        List<TextFlow> rows = new ArrayList<>();
        patternViewModel.getObservableList(OTHER_NAMES).forEach( descrName -> {
            rows.addAll(generateOtherNameRow((DescrName) descrName));
        });
        return rows;
    }

    private ContextMenu createContextMenuForPatternField(PatternField selectedPatternField) {

        String patternMode =  patternViewModel.getPropertyValue(MODE);
        boolean isCreateMode = CREATE.equals(patternMode);

        Object[][] menuItems = new Object[][]{
                {"Edit", true, new String[]{"menu-item"}, (EventHandler<ActionEvent>) actionEvent -> showEditFieldsPanel(actionEvent, selectedPatternField), createGraphics("edit-icon")},
                {MenuHelper.SEPARATOR},
                {"Copy", false, new String[]{"menu-item"}, null, createSVGGraphic(DUPLICATE_SVG_PATH)},
                {"Save to Favorites",  false, new String[]{"menu-item"}, null, createGraphics("favorites-icon")},
                {MenuHelper.SEPARATOR},
                {"Add Comment",  false, new String[]{"menu-item"}, null, createGraphics("comment-icon")},
                {"Remove", isCreateMode, new String[]{"menu-item"}, (EventHandler<ActionEvent>) actionEvent -> patternViewModel.getObservableList(FIELDS_COLLECTION).remove(selectedPatternField)
                , createGraphics("remove-icon")}
        };
        return MenuHelper.getInstance().createContextMenuWithMenuItems(menuItems);
    }

    private Region createGraphics(String iconString) {
        Region region = new Region();
        region.getStyleClass().add(iconString);
        return region;
    }

    //The copy image is not displayed properly in Region css hence using the SVGPath node.
    private SVGPath createSVGGraphic(String content){
        SVGPath svgImagePath = new SVGPath();
        svgImagePath.setContent(content);
        svgImagePath.setFill(Color.WHITE);
        svgImagePath.setFillRule(FillRule.EVEN_ODD);
        return svgImagePath;
    }

    /**
     * This method Retrives language and case semantics.
     * @param descrName
     * @return String.
     *
     */
    private String generateDescriptionSemantics(DescrName descrName){
        ViewCalculator viewCalculator = getViewProperties().calculator();
        EntityFacade caseSigConcept = descrName.getCaseSignificance();
        String casSigText = viewCalculator.getRegularDescriptionText(caseSigConcept.nid())
                .orElse(caseSigConcept.nid()+"");
        EntityFacade langConcept = descrName.getLanguage();
        String langText = viewCalculator.getRegularDescriptionText(langConcept.nid())
                .orElse(String.valueOf(langConcept.nid()));
        return "%s | %s".formatted(casSigText, langText);
    }

    private List<TextFlow> generateOtherNameRow(DescrName otherName) {
        List<TextFlow> textFlows = new ArrayList<>();
        // create textflow to hold regular name label
        TextFlow row1 = new TextFlow();
        Object obj = otherName.getNameText();
        String nameLabel = String.valueOf(obj);
        Text otherNameLabel = new Text(nameLabel);
        otherNameLabel.getStyleClass().add("text-noto-sans-bold-grey-twelve");
        row1.setOnMouseClicked(mouseEvent -> {
            EvtBusFactory.getDefaultEvtBus().publish(patternViewModel.getPropertyValue(PATTERN_TOPIC), new ShowPatternFormInBumpOutEvent(mouseEvent.getSource(), SHOW_EDIT_OTHER_NAME, otherName));
            EvtBusFactory.getDefaultEvtBus().publish(patternViewModel.getPropertyValue(PATTERN_TOPIC), new PropertyPanelEvent(mouseEvent.getSource(), OPEN_PANEL));
        });
        //Text area of semantics used for the Other name text
        Text semanticDescrText = new Text();
        semanticDescrText.setText(" (%s)".formatted(generateDescriptionSemantics(otherName)));
        semanticDescrText.getStyleClass().add("descr-semantic");

        // add the other name label and description semantic label
        row1.getStyleClass().add("descr-semantic-container");

        row1.getChildren().addAll(otherNameLabel);

        TextFlow row2 = new TextFlow();
        row2.getChildren().addAll(semanticDescrText);

        TextFlow row3 = new TextFlow();
        Text dateAddedLabel = new Text("Date Added: ");
        dateAddedLabel.getStyleClass().add("grey8-12pt-bold");

        if (otherName.getSemanticPublicId() != null) {
            Latest<EntityVersion> semanticVersionLatest = getViewProperties().calculator().latest(Entity.nid(otherName.getSemanticPublicId()));
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
                Hyperlink attachmentHyperlink = createActionLink(IconsHelper.createIcon(ATTACHMENT));
                Hyperlink commentsHyperlink = createActionLink(IconsHelper.createIcon(COMMENTS));

                // Add the date info and additional hyperlinks
                row3.getChildren().addAll(dateAddedLabel, dateLabel, attachmentHyperlink, commentsHyperlink);
            });
        }

        textFlows.add(row1);
        textFlows.add(row2);
        textFlows.add(row3);
        return textFlows;
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
     * This method updates the Field label with the correct field number value.
     */
    private void updateFieldValues() {
        ObservableList<Node> fieldVBoxes = fieldsTilePane.getChildren();
        for (int i=0 ; i < fieldVBoxes.size(); i++) {
            Node node = fieldVBoxes.get(i);
            if (node instanceof Parent parent) {
                for (Node child : parent.getChildrenUnmodifiable()) {
                    if (child instanceof Label label && label.getStyleClass().contains("grey8-12pt-bold")) {
                        label.setText("FIELD " + (i + 1) + ":");
                        break;
                    }
                }
            }
        }
    }

    private Node createFieldEntry(PatternField patternField, int fieldNum) {
        VBox fieldVBoxContainer = new VBox();
        fieldVBoxContainer.prefWidth(330);
        Label fieldLabel = new Label("FIELD " + fieldNum + ":");
        fieldLabel.getStyleClass().add("grey8-12pt-bold");
        Text fieldText = new Text(patternField.displayName());
        HBox outerHBox = new HBox();
        outerHBox.setSpacing(8);
        HBox innerHBox = new HBox();
        Region commentIconRegion = new Region();
        commentIconRegion.getStyleClass().add("comment-icon");
        outerHBox.getChildren().addAll(innerHBox, commentIconRegion);
        fieldVBoxContainer.getChildren().addAll(fieldLabel, fieldText, outerHBox);

        fieldVBoxContainer.setOnContextMenuRequested(contextMenuEvent -> {
            ContextMenu contextMenu = createContextMenuForPatternField(patternField);
            contextMenu.show(fieldVBoxContainer.getScene().getWindow(), contextMenuEvent.getScreenX(), contextMenuEvent.getScreenY());
            contextMenuEvent.consume();
        });

        return fieldVBoxContainer;
    }

    private void setupProperties() {
        // Setup Property screen bump out
        // Load Concept Properties View Panel (FXML & Controller)
        Config config = new Config(PATTERN_PROPERTIES_VIEW_FXML_URL)
                .addNamedViewModel(new NamedVm("patternViewModel", patternViewModel))
                .updateViewModel("patternPropertiesViewModel",
                        (patternPropertiesViewModel) -> patternPropertiesViewModel
                                .setPropertyValue(PATTERN_TOPIC, patternViewModel.getPropertyValue(PATTERN_TOPIC))
                                .setPropertyValue(VIEW_PROPERTIES, patternViewModel.getPropertyValue(VIEW_PROPERTIES) )
                                .setPropertyValue(STATE_MACHINE, patternViewModel.getPropertyValue(STATE_MACHINE))
                );

        JFXNode<BorderPane, PropertiesController> propsFXMLLoader = FXMLMvvmLoader.make(config);
        this.propertiesBorderPane = propsFXMLLoader.node();
        this.propertiesController = propsFXMLLoader.controller();
        attachPropertiesViewSlideoutTray(this.propertiesBorderPane);

        // Stamp
        patternViewModel.getProperty(STAMP_VIEW_MODEL).bind(propertiesController.stampFormViewModelProperty());

        propertiesController.updateModel(patternViewModel.getPropertyValue(PATTERN));

        patternViewModel.getProperty(MODE).subscribe(newMode -> {
            if (newMode.equals(EDIT)) {
                updateStampControlFromViewModel();

                // now in EDIT mode, update the identifier
                updateDisplayIdentifier();
            }
        });

        propertiesController.getStampFormViewModel().getBooleanProperty(IS_CONFIRMED_OR_SUBMITTED).subscribe(this::onStampConfirmedOrSubmitted);

        //FIXME this doesn't work properly, should leave for a future effort...
        // open the panel, allow the state machine to determine which panel to show
        //EvtBusFactory.getDefaultEvtBus().publish(patternViewModel.getPropertyValue(PATTERN_TOPIC), new PropertyPanelEvent(propertiesToggleButton, OPEN_PANEL));
    }

    private void onStampConfirmedOrSubmitted(boolean isSubmittedOrConfirmed) {
        if (!isSubmittedOrConfirmed) {
            return;
        }

        updateStampControlFromViewModel();

        if (patternViewModel.getPropertyValue(MODE).equals(EDIT)) {
            patternViewModel.setPropertyValue(PUBLISH_PENDING, true);
        }

        stampViewControl.setDisable(true);
    }

    private void updateStampControlFromViewModel() {
        StampFormViewModelBase stampFormViewModel = propertiesController.getStampFormViewModel();

        if (stampFormViewModel == null) {
            return;
        }

        // -- status
        State newStatus = stampFormViewModel.getPropertyValue(STATUS);
        String statusMsg = newStatus == null ? "Active" : getViewProperties().calculator().getPreferredDescriptionTextWithFallbackOrNid(((State) newStatus).nid());
        stampViewControl.setStatus(statusMsg);

        // -- time
        String newTime = stampFormViewModel.getPropertyValue(FORM_TIME_TEXT);
        stampViewControl.setLastUpdated(newTime);

        // -- author
        EntityFacade newAuthor = stampFormViewModel.getPropertyValue(AUTHOR);
        String authorDescription = ViewCalculatorUtils.getDescriptionTextWithFallbackOrNid(newAuthor, getViewProperties());
        stampViewControl.setAuthor(authorDescription);

        // -- module
        ConceptFacade newModule = stampFormViewModel.getPropertyValue(MODULE);
        String newModuleDescription;
        if (newModule == null) {
            newModuleDescription = "";
        } else {
            newModuleDescription = getViewProperties().calculator().getPreferredDescriptionTextWithFallbackOrNid((newModule).nid());
        }
        stampViewControl.setModule(newModuleDescription);

        // -- path
        ConceptFacade newPath = stampFormViewModel.getPropertyValue(PATH);
        String pathDescr;
        if (newPath == null) {
            pathDescr = "";
        } else {
            pathDescr = getViewProperties().calculator().getPreferredDescriptionTextWithFallbackOrNid((newPath).nid());
        }
        stampViewControl.setPath(pathDescr);
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

        // Clean up the draggable nodes
        removeDraggableNodes(detailsOuterBorderPane,
                tabHeader,
                conceptHeaderControlToolBarHbox,
                propertiesController != null ? propertiesController.getPropertiesTabsPane() : null);

        if (this.onCloseConceptWindow != null) {
            onCloseConceptWindow.accept(this);
        }
    }

    @FXML
    private void showEditView(ActionEvent actionEvent) {
        // put the edit view in the properties pane
    }

    /**
     * show bump out and display Add or Edit Description panel
     * @param actionEvent
     */
    @FXML
    private void showAddEditDefinitionPanel(ActionEvent actionEvent) {
        actionEvent.consume();
        LOG.info("Todo show bump out and display Edit Definition panel \n" + actionEvent);
        StateMachine patternSM = patternViewModel.getPropertyValue(STATE_MACHINE);
        EvtType<ShowPatternFormInBumpOutEvent> patternDefEventType = SHOW_ADD_DEFINITION;
        patternSM.t("addDefinition"); // Default is to add definition state.
        PatternDefinition patternDefinition = new PatternDefinition(
                patternViewModel.getPropertyValue(PURPOSE_ENTITY),
                patternViewModel.getPropertyValue(MEANING_ENTITY),
                null);
        if(patternDefinition.meaning() != null || patternDefinition.purpose() !=null || patternDefinition.membershipTag() !=null){
            patternSM.t("editDefinition");
            patternDefEventType = SHOW_EDIT_DEFINITION;
        }


        // publish property open.
        EvtBusFactory.getDefaultEvtBus().publish(patternViewModel.getPropertyValue(PATTERN_TOPIC), new ShowPatternFormInBumpOutEvent(actionEvent.getSource(), patternDefEventType , patternDefinition));
        EvtBusFactory.getDefaultEvtBus().publish(patternViewModel.getPropertyValue(PATTERN_TOPIC), new PropertyPanelEvent(actionEvent.getSource(), OPEN_PANEL));
    }

    private void showEditFieldsPanel(ActionEvent actionEvent, PatternField selectedPatternField) {
        LOG.info("Todo show bump out and display Edit Fields panel \n" + actionEvent);
        actionEvent.consume();
        StateMachine patternSM = patternViewModel.getPropertyValue(STATE_MACHINE);
        patternSM.t("editField");
        patternViewModel.setPropertyValue(SELECTED_PATTERN_FIELD, selectedPatternField );
        ObservableList<PatternField> patternFieldsObsList = patternViewModel.getObservableList(FIELDS_COLLECTION);
        int fieldNum = (patternFieldsObsList.indexOf(selectedPatternField)+1);
        EvtBusFactory.getDefaultEvtBus().publish(patternViewModel.getPropertyValue(PATTERN_TOPIC), new ShowPatternFormInBumpOutEvent(actionEvent.getSource(), SHOW_EDIT_FIELDS, patternFieldsObsList.size(), selectedPatternField, fieldNum));
        EvtBusFactory.getDefaultEvtBus().publish(patternViewModel.getPropertyValue(PATTERN_TOPIC), new PropertyPanelEvent(actionEvent.getSource(), OPEN_PANEL));
    }

    @FXML
    private void showAddFieldsPanel(ActionEvent actionEvent) {
        LOG.info("Todo show bump out and display Add Fields panel \n" + actionEvent);
        EvtBusFactory.getDefaultEvtBus().publish(patternViewModel.getPropertyValue(PATTERN_TOPIC), new ShowPatternFormInBumpOutEvent(actionEvent.getSource(), SHOW_ADD_FIELDS, patternViewModel.getObservableList(FIELDS_COLLECTION).size()));
        EvtBusFactory.getDefaultEvtBus().publish(patternViewModel.getPropertyValue(PATTERN_TOPIC), new PropertyPanelEvent(actionEvent.getSource(), OPEN_PANEL));
    }

    @FXML
    private void popupAddDescriptionContextMenu(ActionEvent actionEvent) {
        MenuHelper.fireContextMenuEvent(actionEvent, Side.RIGHT, 0, 0);
    }

    @FXML
    private void showAddFqnPanel(ActionEvent actionEvent) {
        LOG.info("Bumpout Add FQN panel \n" + actionEvent);
        actionEvent.consume();
        StateMachine patternSM = patternViewModel.getPropertyValue(STATE_MACHINE);
        patternSM.t("addFqn");
        ObservableList<PatternField> patternFieldsObsList = patternViewModel.getObservableList(FIELDS_COLLECTION);
        EvtBusFactory.getDefaultEvtBus().publish(patternViewModel.getPropertyValue(PATTERN_TOPIC), new ShowPatternFormInBumpOutEvent(actionEvent.getSource(), SHOW_ADD_FQN));
        EvtBusFactory.getDefaultEvtBus().publish(patternViewModel.getPropertyValue(PATTERN_TOPIC), new PropertyPanelEvent(actionEvent.getSource(), OPEN_PANEL));
    }

    @FXML
    private void showAddOtherNamePanel(ActionEvent actionEvent) {
        LOG.info("Bumpout Add Other name panel \n" + actionEvent);
        actionEvent.consume();
        StateMachine patternSM = patternViewModel.getPropertyValue(STATE_MACHINE);
        patternSM.t("addOtherName");
        ObservableList<PatternField> patternFieldsObsList = patternViewModel.getObservableList(FIELDS_COLLECTION);
        EvtBusFactory.getDefaultEvtBus().publish(patternViewModel.getPropertyValue(PATTERN_TOPIC), new ShowPatternFormInBumpOutEvent(actionEvent.getSource(), SHOW_ADD_OTHER_NAME, patternFieldsObsList.size()));
        EvtBusFactory.getDefaultEvtBus().publish(patternViewModel.getPropertyValue(PATTERN_TOPIC), new PropertyPanelEvent(actionEvent.getSource(), OPEN_PANEL));
    }

    @FXML
    private void openReasonerSlideout(ActionEvent event) {
        LOG.info("not implemented yet");
//        ToggleButton reasonerToggle = (ToggleButton) event.getSource();
//        reasonerResultsControllerConsumer.accept(reasonerToggle);
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
        EvtType<PropertyPanelEvent> eventEvtType = propertyToggle.isSelected() ? OPEN_PANEL : CLOSE_PANEL;

        updateDraggableNodesForPropertiesPanel(propertyToggle.isSelected());

        isUpdatingStampSelection = true;
        stampViewControl.setSelected(propertyToggle.isSelected());
        isUpdatingStampSelection = false;

        EvtBusFactory.getDefaultEvtBus().publish(patternViewModel.getPropertyValue(PATTERN_TOPIC), new PropertyPanelEvent(propertyToggle, eventEvtType));
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
    }

    public void putTitlePanesArrowOnRight() {
        putArrowOnRight(this.patternDefinitionTitledPane);
        putArrowOnRight(this.descriptionsTitledPane);
        putArrowOnRight(this.fieldsTitledPane);
    }

    @FXML
    private void savePattern(ActionEvent actionEvent) {
        boolean isValidSave = patternViewModel.createPattern();
        LOG.info(isValidSave ? "success" : "failed");
        if(isValidSave){
            patternViewModel.setPropertyValue(MODE, EDIT);
            EvtBusFactory.getDefaultEvtBus().publish(SAVE_PATTERN_TOPIC, new PatternSavedEvent(actionEvent.getSource(), PatternSavedEvent.PATTERN_UPDATE_EVENT));

            EvtBusFactory.getDefaultEvtBus().publish(SAVE_PATTERN_TOPIC,
                    new MakePatternWindowEvent(actionEvent.getSource(), OPEN_PATTERN, patternViewModel.getPropertyValue(PATTERN), patternViewModel.getViewProperties()));

            patternViewModel.setPropertyValue(PUBLISH_PENDING, false);
            patternViewModel.reLoadPatternValues();
            updateStampControlFromViewModel();
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

    /**
     * Checks whether the properties panel is currently open.
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

    public PropertiesController getPropertiesController() {
        return propertiesController;
    }
}
