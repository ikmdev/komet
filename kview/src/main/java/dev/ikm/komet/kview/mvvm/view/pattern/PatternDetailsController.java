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


import dev.ikm.komet.framework.events.EvtBusFactory;
import dev.ikm.komet.framework.events.EvtType;
import dev.ikm.komet.framework.events.Subscriber;
import dev.ikm.komet.framework.view.ViewProperties;
import dev.ikm.komet.kview.events.pattern.*;
import dev.ikm.komet.kview.fxutils.MenuHelper;
import dev.ikm.komet.kview.mvvm.model.DescrName;
import dev.ikm.komet.kview.mvvm.model.PatternField;
import dev.ikm.komet.kview.mvvm.view.stamp.StampEditController;
import dev.ikm.komet.kview.mvvm.viewmodel.PatternViewModel;
import dev.ikm.tinkar.coordinate.view.calculator.ViewCalculator;
import dev.ikm.tinkar.entity.ConceptEntity;
import dev.ikm.tinkar.terms.EntityFacade;
import dev.ikm.tinkar.terms.State;
import javafx.beans.binding.Bindings;
import javafx.beans.binding.StringBinding;
import javafx.beans.property.ObjectProperty;
import javafx.beans.property.StringProperty;
import javafx.collections.ListChangeListener;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.fxml.FXML;
import javafx.geometry.Side;
import javafx.scene.Node;
import javafx.scene.control.*;
import javafx.scene.layout.*;
import javafx.scene.paint.Color;
import javafx.scene.shape.FillRule;
import javafx.scene.shape.SVGPath;
import javafx.scene.text.Text;
import javafx.scene.text.TextFlow;
import org.carlfx.axonic.StateMachine;
import org.carlfx.cognitive.loader.*;
import org.carlfx.cognitive.viewmodel.ValidationViewModel;
import org.carlfx.cognitive.viewmodel.ViewModel;
import org.controlsfx.control.PopOver;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.net.URL;
import java.time.Instant;
import java.time.LocalDate;
import java.time.ZoneOffset;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.function.Consumer;

import static dev.ikm.komet.kview.events.pattern.PatternFieldsPanelEvent.EDIT_FIELD;
import static dev.ikm.komet.kview.events.pattern.PropertyPanelEvent.CLOSE_PANEL;
import static dev.ikm.komet.kview.events.pattern.PropertyPanelEvent.OPEN_PANEL;
import static dev.ikm.komet.kview.events.pattern.ShowPatternFormInBumpOutEvent.*;
import static dev.ikm.komet.kview.fxutils.SlideOutTrayHelper.*;
import static dev.ikm.komet.kview.fxutils.TitledPaneHelper.putArrowOnRight;
import static dev.ikm.komet.kview.fxutils.ViewportHelper.clipChildren;
import static dev.ikm.komet.kview.mvvm.viewmodel.FormViewModel.VIEW_PROPERTIES;
import static dev.ikm.komet.kview.mvvm.viewmodel.PatternViewModel.*;
import static dev.ikm.tinkar.coordinate.stamp.StampFields.*;

public class PatternDetailsController {

    private static final Logger LOG = LoggerFactory.getLogger(PatternDetailsController.class);

    public static final URL PATTERN_PROPERTIES_VIEW_FXML_URL = PatternDetailsController.class.getResource("pattern-properties.fxml");

    private static final String EDIT_STAMP_OPTIONS_FXML = "edit-stamp.fxml";

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
    private TextField identifierText;

    @FXML
    private Text lastUpdatedText;

    @FXML
    private Text moduleText;

    @FXML
    private Text pathText;

    @FXML
    private Text statusText;

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

    @FXML
    private Button savePatternButton;

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

    /**
     * Stamp Edit
     */
    private PopOver stampEdit;
    private StampEditController stampEditController;

    @InjectViewModel
    private PatternViewModel patternViewModel;

    String copySVGPath = "M11.7266 8.89513L11.7266 10.7756L14.0916 10.7756L14.0916 13.1738L15.9721 13.1738L15.9721 10.7756L18.4034 10.7756L18.4034 8.89513L15.9721 8.89513L15.9721 6.49699L14.0916 6.49699L14.0916 8.89513L11.7266 8.89513Z M2.40039 18.6975L2.40039 11.7384C2.40038 11.2622 2.40037 10.8492 2.4282 10.5086C2.45764 10.1482 2.52284 9.78699 2.70036 9.43858C2.96422 8.92072 3.38526 8.49969 3.90311 8.23583C4.25153 8.0583 4.61277 7.99311 4.9731 7.96366C5.31375 7.93583 5.72667 7.93584 6.2029 7.93586L7.83625 7.93586L7.83625 6.3025C7.83624 5.82625 7.83623 5.41338 7.86406 5.07271C7.8935 4.71238 7.95869 4.35114 8.13622 4.00272C8.40008 3.48486 8.82111 3.06383 9.33897 2.79997C9.68738 2.62245 10.0486 2.55725 10.409 2.52781C10.7497 2.49997 11.1626 2.49999 11.6389 2.5L18.5977 2.5C19.074 2.49999 19.487 2.49997 19.8277 2.52781C20.188 2.55725 20.5493 2.62245 20.8977 2.79997C21.4155 3.06383 21.8366 3.48486 22.1004 4.00272C22.2779 4.35114 22.3431 4.71237 22.3726 5.07271C22.4004 5.41342 22.4004 5.82635 22.4004 6.30267L22.4004 13.2615C22.4004 13.7378 22.4004 14.1507 22.3726 14.4914C22.3431 14.8518 22.2779 15.213 22.1004 15.5614C21.8366 16.0793 21.4155 16.5003 20.8977 16.7642C20.5493 16.9417 20.188 17.0069 19.8277 17.0363C19.487 17.0642 19.0741 17.0642 18.5978 17.0641L16.9645 17.0641L16.9645 18.6976C16.9645 19.1738 16.9646 19.5866 16.9367 19.9273C16.9073 20.2876 16.8421 20.6489 16.6646 20.9973C16.4007 21.5151 15.9797 21.9362 15.4618 22.2C15.1134 22.3776 14.7522 22.4427 14.3918 22.4722C14.0512 22.5 13.6383 22.5 13.162 22.5L6.20291 22.5C5.72666 22.5 5.31377 22.5 4.9731 22.4722C4.61277 22.4427 4.25153 22.3776 3.90311 22.2C3.38526 21.9362 2.96423 21.5151 2.70036 20.9973C2.52284 20.6489 2.45764 20.2876 2.4282 19.9273C2.40037 19.5866 2.40038 19.1737 2.40039 18.6975ZM4.30243 19.7742C4.2816 19.5192 4.28086 19.1836 4.28086 18.6606L4.28086 11.7752C4.28086 11.2522 4.2816 10.9167 4.30243 10.6617C4.32237 10.4176 4.35595 10.3314 4.37588 10.2923C4.45945 10.1283 4.59281 9.99491 4.75683 9.91134C4.79593 9.89142 4.88212 9.85784 5.12623 9.83789C5.38119 9.81706 5.71676 9.81633 6.23975 9.81633L7.83625 9.81633L7.83625 13.2616C7.83624 13.7378 7.83623 14.1508 7.86406 14.4914C7.8935 14.8518 7.95869 15.213 8.13622 15.5614C8.40008 16.0793 8.82111 16.5003 9.33897 16.7642C9.68739 16.9417 10.0486 17.0069 10.409 17.0363C10.7496 17.0642 11.1626 17.0642 11.6388 17.0641L15.0841 17.0641L15.0841 18.6606C15.0841 19.1836 15.0833 19.5192 15.0625 19.7742C15.0426 20.0183 15.009 20.1045 14.9891 20.1436C14.9055 20.3076 14.7721 20.4409 14.6081 20.5245C14.569 20.5444 14.4828 20.578 14.2387 20.598C13.9837 20.6188 13.6482 20.6195 13.1252 20.6195L6.23975 20.6195C5.71676 20.6195 5.38119 20.6188 5.12623 20.598C4.88212 20.578 4.79593 20.5444 4.75683 20.5245C4.59281 20.4409 4.45945 20.3076 4.37588 20.1436C4.35595 20.1045 4.32237 20.0183 4.30243 19.7742ZM9.73829 14.3383C9.71746 14.0833 9.71672 13.7478 9.71672 13.2248L9.71672 6.33936C9.71672 5.81637 9.71745 5.4808 9.73829 5.22584C9.75823 4.98173 9.79181 4.89554 9.81173 4.85644C9.89531 4.69241 10.0287 4.55906 10.1927 4.47548C10.2318 4.45556 10.318 4.42198 10.5621 4.40204C10.817 4.38121 11.1526 4.38047 11.6756 4.38047L18.561 4.38047C19.084 4.38047 19.4196 4.38121 19.6745 4.40204C19.9187 4.42198 20.0048 4.45556 20.0439 4.47548C20.208 4.55906 20.3413 4.69241 20.4249 4.85644C20.4448 4.89554 20.4784 4.98173 20.4984 5.22584C20.5192 5.4808 20.5199 5.81637 20.5199 6.33936L20.5199 13.2248C20.5199 13.7478 20.5192 14.0833 20.4984 14.3383C20.4784 14.5824 20.4448 14.6686 20.4249 14.7077C20.3413 14.8717 20.208 15.0051 20.044 15.0887C20.0049 15.1086 19.9187 15.1422 19.6746 15.1621C19.4196 15.1829 19.084 15.1837 18.561 15.1837L11.6756 15.1837C11.1526 15.1837 10.817 15.1829 10.5621 15.1621C10.318 15.1422 10.2318 15.1086 10.1927 15.0887C10.0287 15.0051 9.89531 14.8717 9.81173 14.7077C9.79181 14.6686 9.75823 14.5824 9.73829 14.3383Z";

    private Subscriber<PropertyPanelEvent> patternPropertiesEventSubscriber;

    private Subscriber<PatternDefinitionEvent> patternDefinitionEventSubscriber;

    private Subscriber<PatternDescriptionEvent> patternDescriptionEventSubscriber;

    private Subscriber<PatternFieldsPanelEvent> patternFieldsPanelEventSubscriber;

    public PatternDetailsController() {}

    @FXML
    private void initialize() {
        identifierText.setText("");
        fieldsTilePane.getChildren().clear();
        fieldsTilePane.setPrefColumns(2);
        otherNamesVBox.getChildren().clear();

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
        EvtBusFactory.getDefaultEvtBus().subscribe(patternViewModel.getPropertyValue(PATTERN_TOPIC), PropertyPanelEvent.class, patternPropertiesEventSubscriber);

        savePatternButton.disableProperty().bind(patternViewModel.getProperty(IS_INVALID));

        // capture pattern definition information
        purposeText.textProperty().bind(patternViewModel.getProperty(PURPOSE_TEXT));
        purposeText.getStyleClass().add("text-noto-sans-bold-grey-twelve");

        meaningText.textProperty().bind(patternViewModel.getProperty(MEANING_TEXT));
        meaningText.getStyleClass().add("text-noto-sans-bold-grey-twelve");

        meaningDate.textProperty().bind(patternViewModel.getProperty(MEANING_DATE_STR));
        purposeDate.textProperty().bind(patternViewModel.getProperty(PURPOSE_DATE_STR));

        patternDefinitionEventSubscriber = evt -> patternViewModel.setPurposeAndMeaningText(evt.getPatternDefinition());

        EvtBusFactory.getDefaultEvtBus().subscribe(patternViewModel.getPropertyValue(PATTERN_TOPIC), PatternDefinitionEvent.class, patternDefinitionEventSubscriber);

        // capture descriptions information
        StringProperty fqnTextProperty = patternViewModel.getProperty(FQN_DESCRIPTION_NAME_TEXT);
        latestFqnText.textProperty().bind(fqnTextProperty);

        // This will listen to the pattern descriptions event. Adding an FQN, Adding other name.
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
                ObservableList<DescrName> descrNameObservableList = patternViewModel.getObservableList(OTHER_NAMES);
                descrNameObservableList.add(evt.getDescrName());
                patternSM.t("otherNameDone");
            }
        };
        EvtBusFactory.getDefaultEvtBus().subscribe(patternViewModel.getPropertyValue(PATTERN_TOPIC), PatternDescriptionEvent.class, patternDescriptionEventSubscriber);

        // Bind FQN property with description text, date and FQN menu item.
        ObjectProperty<DescrName> fqnNameProp = patternViewModel.getProperty(FQN_DESCRIPTION_NAME);
        // Generate description semantic and show
        fqnDescriptionSemanticText.textProperty().bind(fqnNameProp.map(descrName -> " (%s)".formatted(generateDescriptionSemantics(descrName))).orElse(""));
        // display current date else blank.
        fqnAddDateLabel.textProperty().bind(fqnNameProp.map((fqnName) -> LocalDate.now().format(DateTimeFormatter.ofPattern("MMM d, yyyy"))).orElse(""));
        // hide menu item if FQN is added.
        addFqnMenuItem.visibleProperty().bind(fqnNameProp.isNull());


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
                    }
                }
            }
        });

        ObservableList<PatternField> patternFieldList = patternViewModel.getObservableList(FIELDS_COLLECTION);
        patternFieldsPanelEventSubscriber = evt -> {
            PatternField patternField = evt.getPatternField();
            PatternField previousPatternField = evt.getPreviousPatternField();
            int fieldPosition = evt.getCurrentFieldOrder()-1;
            if(evt.getEventType() == EDIT_FIELD && previousPatternField != null){
                // 1st remove it from list before adding the new entry
                patternFieldList.remove(previousPatternField);
            }
            //Update the fields collection data.
            patternFieldList.add(fieldPosition, patternField);
             // save and therefore validate
            patternViewModel.save();
        };

        EvtBusFactory.getDefaultEvtBus().subscribe(patternViewModel.getPropertyValue(PATTERN_TOPIC), PatternFieldsPanelEvent.class, patternFieldsPanelEventSubscriber);

        //Listen to the changes int he fieldsTilePane and update the field numbers.
        ObservableList<Node> fieldsTilePaneList = fieldsTilePane.getChildren();
        fieldsTilePaneList.addListener((ListChangeListener<Node>) (listener) -> {
            while(listener.next()){
                if(listener.wasAdded() || listener.wasRemoved()){
                    updateFieldValues();
                }
            }
        });

        patternFieldList.addListener((ListChangeListener<? super PatternField>) changeListner -> {
            while(changeListner.next()){
                if(changeListner.wasAdded()){
                    int fieldPosition = changeListner.getTo()-1;
                    //update the display.
                    fieldsTilePane.getChildren().add(fieldPosition, createFieldEntry(changeListner.getAddedSubList().getFirst(), changeListner.getTo()));
                }else if(changeListner.wasRemoved()){
                    fieldsTilePaneList.remove(changeListner.getTo());
                }
            }
        });

        Label fqnAddDateLabel = new Label();
        ObjectProperty<DescrName> objectProperty = patternViewModel.getProperty(FQN_DESCRIPTION_NAME);
        StringBinding dateStrProp = Bindings
                .when(objectProperty.isNotNull())
                .then(LocalDate.now().format(DateTimeFormatter.ofPattern("MMM d, yyyy")))
                .otherwise("");
        fqnAddDateLabel.textProperty().bind(dateStrProp);
        // Setup Properties
        setupProperties();
    }

    private ContextMenu createContextMenuForPatternField(PatternField selectedPatternField) {

        Object[][] menuItems = new Object[][]{
                {"Edit", true, new String[]{"menu-item"}, (EventHandler<ActionEvent>) actionEvent -> showEditFieldsPanel(actionEvent, selectedPatternField), createGraphics("edit-icon")},
                {MenuHelper.SEPARATOR},
                {"Copy", false, new String[]{"menu-item"}, null, createSVGGraphic(copySVGPath)},
                {"Save to Favorites",  false, new String[]{"menu-item"}, null, createGraphics("favorites-icon")},
                {MenuHelper.SEPARATOR},
                {"Add Comment",  false, new String[]{"menu-item"}, null, createGraphics("comment-icon")},
                {"Remove", true, new String[]{"menu-item"}, (EventHandler<ActionEvent>) actionEvent -> patternViewModel.getObservableList(FIELDS_COLLECTION).remove(selectedPatternField)
                , createGraphics("remove-icon")}
        };
        return MenuHelper.getInstance().createContextMenuWithMenuItems(menuItems);
    }

    private Region createGraphics(String iconString) {
        Region region = new Region();
        region.getStyleClass().add(iconString);
        return region;
    }

    //The copy image is not dispalyed properly in Region css hence using the SVGPath node.
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
        ConceptEntity caseSigConcept = descrName.getCaseSignificance();
        String casSigText = viewCalculator.getRegularDescriptionText(caseSigConcept.nid())
                .orElse(caseSigConcept.nid()+"");
        ConceptEntity langConcept = descrName.getLanguage();
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

        //Text area of semantics used for the Other name text
        Text semanticDescrText = new Text();
        semanticDescrText.setText(" (%s)".formatted(generateDescriptionSemantics(otherName)));
        semanticDescrText.getStyleClass().add("descr-semantic");

        // add the other name label and description semantic label
        row1.getStyleClass().add("descr-semantic-container");

        row1.getChildren().addAll(otherNameLabel);

        TextFlow row2 = new TextFlow();
        row2.getChildren().addAll(semanticDescrText);

        // update date
        String dateAddedStr = LocalDate.now().format(DateTimeFormatter.ofPattern("MMM d, yyyy")).toString();
        TextFlow row3 = new TextFlow();
        Text dateAddedLabel = new Text("Date Added:");
        dateAddedLabel.getStyleClass().add("text-noto-sans-normal-grey-eight");
        Text dateLabel = new Text(dateAddedStr);
        dateLabel.getStyleClass().add("text-noto-sans-normal-grey-eight");

        Hyperlink attachmentHyperlink = new Hyperlink("Attachment");
        Hyperlink commentHyperlink = new Hyperlink("Comment");

        // Add the date info and additional hyperlinks
        row3.getChildren().addAll(dateAddedLabel, dateLabel, attachmentHyperlink, commentHyperlink);

        textFlows.add(row1);
        textFlows.add(row2);
        textFlows.add(row3);
        return textFlows;
    }

    /**
     * This method updates the Field label with the correct field number value.
     */
    private void updateFieldValues() {
        ObservableList<Node> fieldVBoxes = fieldsTilePane.getChildren();
        for(int i=0 ; i < fieldVBoxes.size(); i++){
            Node node = fieldVBoxes.get(i);
            Node labelNode = node.lookup(".pattern-field");
            if(labelNode instanceof Label label){
                label.setText("FIELD " + (i+1));
            }
        }
    }

    private Node createFieldEntry(PatternField patternField, int fieldNum) {
        VBox fieldVBoxContainer = new VBox();
        fieldVBoxContainer.prefWidth(330);
        Label fieldLabel = new Label("FIELD " + fieldNum);
        fieldLabel.getStyleClass().add("pattern-field");
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
        fieldVBoxContainer.setOnMouseClicked(mouseEvent -> {
            ContextMenu contextMenu = createContextMenuForPatternField (patternField);
            contextMenu.show(fieldVBoxContainer, mouseEvent.getScreenX(),  mouseEvent.getScreenY());
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

        //FIXME this doesn't work properly, should leave for a future effort...
        // open the panel, allow the state machine to determine which panel to show
        //EvtBusFactory.getDefaultEvtBus().publish(patternViewModel.getPropertyValue(PATTERN_TOPIC), new PropertyPanelEvent(propertiesToggleButton, OPEN_PANEL));
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

    /**
     * show bump out and display Add or Edit Description panel
     * @param actionEvent
     */
    @FXML
    private void showAddEditDefinitionPanel(ActionEvent actionEvent) {
        actionEvent.consume();
        LOG.info("Todo show bump out and display Edit Definition panel \n" + actionEvent);
        StateMachine patternSM = patternViewModel.getPropertyValue(STATE_MACHINE);
        patternSM.t("addField");
        ObservableList<PatternField> patternFieldList = patternViewModel.getObservableList(FIELDS_COLLECTION);
        // publish property open.
        EvtBusFactory.getDefaultEvtBus().publish(patternViewModel.getPropertyValue(PATTERN_TOPIC), new ShowPatternFormInBumpOutEvent(actionEvent.getSource(), SHOW_ADD_DEFINITION, patternFieldList.size()+1));
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
        EvtBusFactory.getDefaultEvtBus().publish(patternViewModel.getPropertyValue(PATTERN_TOPIC), new ShowPatternFormInBumpOutEvent(actionEvent.getSource(), SHOW_ADD_FQN, patternFieldsObsList.size()));
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
    public void popupStampEdit(ActionEvent event) {
        if (stampEdit != null && stampEditController != null) {
            stampEdit.show((Node) event.getSource());
            stampEditController.selectActiveStatusToggle();
            return;
        }

        // The stampViewModel is already created for the PatternDetailsController when instantiated
        // inside the JournalController
        // Inject Stamp view model into form.
        Config stampConfig = new Config(StampEditController.class.getResource(EDIT_STAMP_OPTIONS_FXML))
                .addNamedViewModel(new NamedVm("stampViewModel", getStampViewModel()));
        JFXNode<Pane, StampEditController> stampJFXNode = FXMLMvvmLoader.make(stampConfig);

        // for now, we are in create mode, but in the future we will check to see if we are in EDIT mode

        Pane editStampPane = stampJFXNode.node();
        PopOver popOver = new PopOver(editStampPane);
        popOver.getStyleClass().add("filter-menu-popup");
        StampEditController stampEditController = stampJFXNode.controller();

        stampEditController.updateModel(getViewProperties());

        // default the status=Active, disable inactive
        stampEditController.selectActiveStatusToggle();

        popOver.setOnHidden(windowEvent -> {
            // set Stamp info into Details form
            getStampViewModel().save();
            patternViewModel.save();
            updateUIStamp(getStampViewModel());
        });

        popOver.show((Node) event.getSource());

        // store and use later.
        stampEdit = popOver;
        this.stampEditController = stampEditController;
    }

    private void updateUIStamp(ViewModel stampViewModel) {
        if (patternViewModel.isPatternPopulated()) {
            updateTimeText(stampViewModel.getValue(TIME));
        }
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
        State status = stampViewModel.getValue(STATUS);
        String statusMsg = status == null ? "Active" : getViewProperties().calculator().getPreferredDescriptionTextWithFallbackOrNid(status.nid());
        statusText.setText(statusMsg);
    }

    private void updateTimeText(Long time) {
        DateTimeFormatter DATE_TIME_FORMATTER = DateTimeFormatter.ofPattern("yyyy-MMM-dd HH:mm:ss");
        Instant stampInstance = Instant.ofEpochSecond(time/1000);
        ZonedDateTime stampTime = ZonedDateTime.ofInstant(stampInstance, ZoneOffset.UTC);
        String lastUpdated = DATE_TIME_FORMATTER.format(stampTime);
        lastUpdatedText.setText(lastUpdated);
    }

    public ValidationViewModel getStampViewModel() {
        return patternViewModel.getPropertyValue(STAMP_VIEW_MODEL);
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

        Region contentRegion = contentViewPane;
        // binding the child's height to the preferred height of hte parent
        // so that when we resize the window the content in the slide out pane
        // aligns with the details view
        contentRegion.prefHeightProperty().bind(slideoutTrayPane.heightProperty());
    }

    public void updateView() {
        EntityFacade entityFacade = patternViewModel.getValue(PATTERN);
        if(entityFacade == null){
            getStampViewModel().setPropertyValue(MODE, CREATE);
        }else {
            getStampViewModel().setPropertyValue(MODE, EDIT);
        }
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
        updatePatternBanner();
        updateView();
    }

    private void updatePatternBanner() {
        // update the title to the FQN
        patternTitleText.setText(patternViewModel.getPatternTitle());

        // get the latest stamp time
        if (patternViewModel.isPatternPopulated()) {
            updateTimeText(getStampViewModel().getValue(TIME));
        }

        // update the public id
        identifierText.setText(patternViewModel.getPatternIdentifierText());
    }
}
