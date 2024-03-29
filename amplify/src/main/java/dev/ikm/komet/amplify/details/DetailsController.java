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
package dev.ikm.komet.amplify.details;

import static dev.ikm.komet.amplify.commons.SlideOutTrayHelper.slideIn;
import static dev.ikm.komet.amplify.commons.SlideOutTrayHelper.slideOut;
import static dev.ikm.komet.amplify.commons.ViewportHelper.clipChildren;
import static dev.ikm.tinkar.terms.TinkarTerm.*;

import dev.ikm.komet.amplify.events.ClosePropertiesPanelEvent;
import dev.ikm.komet.amplify.events.EditConceptFullyQualifiedNameEvent;
import dev.ikm.komet.amplify.events.EditOtherNameConceptEvent;
import dev.ikm.komet.framework.Identicon;
import dev.ikm.komet.framework.events.EvtBus;
import dev.ikm.komet.framework.events.EvtBusFactory;
import dev.ikm.komet.framework.events.Subscriber;
import dev.ikm.komet.framework.observable.ObservableField;
import dev.ikm.komet.framework.propsheet.KometPropertySheet;
import dev.ikm.komet.framework.propsheet.SheetItem;
import dev.ikm.komet.framework.view.ViewProperties;
import dev.ikm.komet.preferences.KometPreferences;
import dev.ikm.tinkar.coordinate.stamp.calculator.Latest;
import dev.ikm.tinkar.coordinate.view.calculator.ViewCalculator;
import dev.ikm.tinkar.entity.*;
import dev.ikm.tinkar.terms.ConceptFacade;
import dev.ikm.tinkar.terms.EntityFacade;
import dev.ikm.tinkar.terms.State;
import dev.ikm.tinkar.terms.TinkarTerm;
import java.io.Serializable;
import java.time.Instant;
import java.time.ZoneOffset;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.function.Consumer;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Pane;
import javafx.scene.layout.VBox;
import javafx.scene.text.Text;
import javafx.scene.text.TextFlow;
import org.eclipse.collections.api.factory.Lists;
import org.eclipse.collections.api.list.ImmutableList;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class DetailsController implements Serializable {
    private static final Logger LOG = LoggerFactory.getLogger(DetailsController.class);

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
    private ScrollPane statedAxiomScrollPane;

    @FXML
    private ScrollPane inferredAxiomScrollPane;

    @FXML
    private Label notAvailInferredAxiomLabel;

    @FXML
    private Label notAvailStatedAxiomLabel;


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
     * A function from the caller. This class passes a boolean true if classifier button is pressed invoke caller's function to be returned a controller.
     */
    private Consumer<ToggleButton> reasonerResultsControllerConsumer;

    private ViewProperties viewProperties;
    private EntityFacade entityFacade;

    private EvtBus eventBus;
    
    private UUID conceptTopic;

    private Subscriber<EditConceptFullyQualifiedNameEvent> fqnSubscriber;

    private Subscriber<EditOtherNameConceptEvent> editOtherNameConceptEventSubscriber;

    private Subscriber<ClosePropertiesPanelEvent> closePropertiesPanelEventSubscriber;

    public DetailsController() {
    }

    public DetailsController(UUID conceptTopic) {
        this.conceptTopic = conceptTopic;
    }

    @FXML
    public void initialize() {
        Tooltip.install(identifierText, identifierTooltip);
        Tooltip.install(lastUpdatedText, authorTooltip);
        Tooltip.install(fqnTitleText, conceptNameTooltip);

        clearView();

        eventBus = EvtBusFactory.getDefaultEvtBus();

        // when the user clicks a fully qualified name, open the PropertiesPanel
        fqnSubscriber = evt -> {
            if (!propertiesToggleButton.isSelected()) {
                propertiesToggleButton.fire();
            }
        };
        eventBus.subscribe(conceptTopic, EditConceptFullyQualifiedNameEvent.class, fqnSubscriber);

        // when the user clicks one of the other names, open the PropertiesPanel
        editOtherNameConceptEventSubscriber = evt -> {
            if (!propertiesToggleButton.isSelected()) {
                propertiesToggleButton.fire();
            }
        };
        eventBus.subscribe(conceptTopic, EditOtherNameConceptEvent.class, editOtherNameConceptEventSubscriber);

        // if the user clicks the Close Properties Button from the Edit Descriptions panel
        // in that state, the properties bump out will be slid out, therefore firing will perform a slide in
        closePropertiesPanelEventSubscriber = evt -> propertiesToggleButton.fire();
        eventBus.subscribe(conceptTopic, ClosePropertiesPanelEvent.class, closePropertiesPanelEventSubscriber);
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

    public void updateModel(final ViewProperties viewProperties, EntityFacade entityFacade) {
        this.viewProperties = viewProperties;
        this.entityFacade = entityFacade;
    }
    public void updateView() {
        // Display info for top banner area
        updateConceptBanner();

        // Display Description info area
        updateConceptDescription();

        // Axioms area
        updateAxioms();
    }
    public void onReasonerSlideoutTray(Consumer<ToggleButton> reasonerResultsControllerConsumer) {
        this.reasonerResultsControllerConsumer = reasonerResultsControllerConsumer;
    }
    /**
     * Responsible for populating the top banner area of the concept view panel.
     */
    public void updateConceptBanner() {
        // TODO do a null check on the entityFacade
        // Title (FQN of concept)
        final ViewCalculator viewCalculator = viewProperties.calculator();
        String conceptNameStr = viewCalculator.getFullyQualifiedDescriptionTextWithFallbackOrNid(entityFacade);
        fqnTitleText.setText(conceptNameStr);
        conceptNameTooltip.setText(conceptNameStr);

        // Definition description text
        definitionTextArea.setText(viewCalculator.getDefinitionDescriptionText(entityFacade.nid()).orElse(""));

        // Public ID (UUID)
        String uuidStr = entityFacade.publicId() != null ? entityFacade.publicId().asUuidArray()[0].toString(): "";
        identifierText.setText(uuidStr);
        identifierTooltip.setText(uuidStr);

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

    /**
     * Responsible for populating the Descriptions TitledPane area. This retrieves the latest concept version and
     * semantics for language and case significance.
     */
    public void updateConceptDescription() {
        final ViewCalculator viewCalculator = viewProperties.calculator();
        // populate UI with FQN and other names. e.g. Hello Solor (English | Case-insensitive)
        Map<SemanticEntityVersion, List<String>> descriptionSemanticsMap = latestDescriptionSemantics(viewCalculator, entityFacade);
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
                otherNamesVBox.getChildren().clear();

                // start adding a row
                List<TextFlow> rows = generateOtherNameRow(semanticEntityVersion, fieldDescriptions);
                rows.forEach(textFlowPane -> {
                    // if the edit button is selected in the properties bump out
                    // then allow editing of the description that the user clicked
                    textFlowPane.setOnMouseClicked(event -> {
                        eventBus.publish(conceptTopic,
                                new EditOtherNameConceptEvent(entityFacade,
                                        EditOtherNameConceptEvent.EDIT_OTHER_NAME));
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

        Text semanticDescrLabel = new Text();
        if (fieldDescriptions.size() > 0) {
            semanticDescrLabel.setText(" (%s)".formatted(descrSemanticStr));
            semanticDescrLabel.getStyleClass().add("descr-semantic");
        } else {
            semanticDescrLabel.setText("");
        }
        // add the other name label and description semantic label
        row1.getChildren().addAll(otherNameLabel, semanticDescrLabel);

        TextFlow row2 = new TextFlow();
        Text dateAddedLabel = new Text("Date Added:");
        dateAddedLabel.getStyleClass().add("descr-semantic");
        Text dateLabel = new Text(time);
        dateLabel.getStyleClass().add("descr-semantic");

        Hyperlink attachmentHyperlink = new Hyperlink("Attachment");
        Hyperlink commentHyperlink = new Hyperlink("Comment");

        // Add the date info and additional hyperlinks
        row2.getChildren().addAll(dateAddedLabel, dateLabel, attachmentHyperlink, commentHyperlink);

        textFlows.add(row1);
        textFlows.add(row2);
        return textFlows;
    }

    private void updateFQNSemantics(SemanticEntityVersion semanticEntityVersion, List<String> fieldDescriptions) {
        DateTimeFormatter DATE_TIME_FORMATTER = DateTimeFormatter.ofPattern("MMM dd, yyyy");
        // Latest FQN
        String fullyQualifiedName = (String) semanticEntityVersion.fieldValues().get(1);
        latestFqnText.setText(fullyQualifiedName);

        latestFqnText.setOnMouseClicked(event -> {
            eventBus.publish(conceptTopic,
                    new EditConceptFullyQualifiedNameEvent(fullyQualifiedName,
                            EditConceptFullyQualifiedNameEvent.EDIT_FQN));
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
        // clear Axioms areas
        ViewCalculator viewCalculator = viewProperties.calculator();

        // Create a SheetItem (AXIOM inferred semantic version)
        // TODO Should this be reused instead of instanciating a new one everytime?
        KometPropertySheet inferredPropertySheet = new KometPropertySheet(viewProperties, true);
        Latest<SemanticEntityVersion> inferredSemanticVersion = viewCalculator.getInferredAxiomSemanticForEntity(entityFacade.nid());
        makeSheetItem(viewProperties, inferredPropertySheet, inferredSemanticVersion);
        inferredAxiomScrollPane.setFitToWidth(true);
        inferredAxiomScrollPane.setFitToHeight(true);
        inferredAxiomScrollPane.setContent(inferredPropertySheet);


        // Create a SheetItem (AXIOM stated semantic version)
        KometPropertySheet statedPropertySheet = new KometPropertySheet(viewProperties, true);
        Latest<SemanticEntityVersion> statedSemanticVersion    = viewCalculator.getStatedAxiomSemanticForEntity(entityFacade.nid());
        makeSheetItem(viewProperties, statedPropertySheet, statedSemanticVersion);
        statedAxiomScrollPane.setFitToWidth(true);
        statedAxiomScrollPane.setFitToHeight(true);
        statedAxiomScrollPane.setContent(statedPropertySheet);

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
        inferredAxiomScrollPane.setContent(notAvailInferredAxiomLabel);
        statedAxiomScrollPane.setContent(notAvailStatedAxiomLabel);
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
    private void openReasonerSlideout(ActionEvent event) {
        ToggleButton reasonerToggle = (ToggleButton) event.getSource();
        reasonerResultsControllerConsumer.accept(reasonerToggle);
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
