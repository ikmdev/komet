package dev.ikm.komet.amplifydetails;

import dev.ikm.komet.framework.Identicon;
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
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.FlowPane;
import javafx.scene.layout.VBox;
import javafx.scene.text.Text;
import javafx.scene.text.TextFlow;
import org.eclipse.collections.api.factory.Lists;
import org.eclipse.collections.api.list.ImmutableList;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.Serializable;
import java.time.Instant;
import java.time.ZoneOffset;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static dev.ikm.tinkar.terms.TinkarTerm.*;

public class AmplifyDetailsController implements Serializable {
    private static final Logger LOG = LoggerFactory.getLogger(AmplifyDetailsController.class);


    //////////  Banner area /////////////////////
    @FXML
    private ImageView identiconImageView;

    @FXML
    private Label fqnTitleText;

    @FXML
    private Label definitionText;

    @FXML
    private TextField identifierText;

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
    private Button elppSemanticCountButton;

    /**
     * Responsible for holding rows of Axiom semantics as Property Sheet (SheetItem) from ControlsFX.
     */
    @FXML
    private VBox inferredAxiomsVBox;
    @FXML
    private VBox statedAxiomsVBox;

    /**
     * This displays as rounded rectangle with a number inside. That can be pressed.
     */
    @FXML
    private Button editAxiomsButton;

    /**
     * This is called after dependency injection has occurred to the JavaFX controls above.
     */
    @FXML
    public void initialize() {
        Tooltip.install(lastUpdatedText, authorTooltip);
        clearView();
    }

    public void updateView(final ViewProperties viewProperties, EntityFacade entityFacade) {
        ViewCalculator viewCalculator = viewProperties.calculator();

        // Display info for top banner area
        updateConceptBanner(viewCalculator, entityFacade);

        // Display Description info area
        updateConceptDescription(viewCalculator, entityFacade);

        // Axioms area
        updateAxioms(viewProperties, entityFacade);
    }

    /**
     * Responsible for populating the top banner area of the concept view panel.
     * @param viewCalculator View Calculator determines valid items to display.
     * @param entityFacade A minimalistic object representing an entity.
     */
    public void updateConceptBanner(final ViewCalculator viewCalculator, EntityFacade entityFacade) {
        // TODO do a null check on the entityFacade
        // Title (FQN of concept)
        fqnTitleText.setText(viewCalculator.getFullyQualifiedDescriptionTextWithFallbackOrNid(entityFacade));

        // Definition description text
        definitionText.setText(viewCalculator.getDefinitionDescriptionText(entityFacade.nid()).orElse(""));

        // Public ID (UUID)
        identifierText.setText(entityFacade.publicId() != null ? entityFacade.publicId().asUuidArray()[0].toString(): "");

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
     *
     * @param viewCalculator View Calculator determines valid items to display.
     * @param conceptFacade A minimalistic object representing an entity.
     */
    public void updateConceptDescription(final ViewCalculator viewCalculator, final EntityFacade conceptFacade) {
        // populate UI with FQN and other names. e.g. Hello Solor (English | Case-insensitive)
        Map<SemanticEntityVersion, List<String>> descriptionSemanticsMap = latestDescriptionSemantics(viewCalculator, conceptFacade);
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
                otherNamesVBox.getChildren().add(generateOtherNameRow(semanticEntityVersion, fieldDescriptions));

                LOG.debug("Other Names = " + semanticEntityVersion + " " + fieldDescriptions);
            }
        });
    }

    private VBox generateOtherNameRow(SemanticEntityVersion semanticEntityVersion, List<String> fieldDescriptions) {
        DateTimeFormatter DATE_TIME_FORMATTER = DateTimeFormatter.ofPattern("MMM dd, yyyy");

        String descrSemanticStr = String.join(" | ", fieldDescriptions);

        // update date
        Instant stampInstance = Instant.ofEpochSecond(semanticEntityVersion.stamp().time()/1000);
        ZonedDateTime stampTime = ZonedDateTime.ofInstant(stampInstance, ZoneOffset.UTC);
        String time = DATE_TIME_FORMATTER.format(stampTime);


        VBox rowGroup = new VBox();
        TextFlow row1 = new TextFlow();
        Label otherNameLabel = new Label(String.valueOf(semanticEntityVersion.fieldValues().get(1)));
        otherNameLabel.getStyleClass().add("descr-concept-name");

        Label semanticDescrLabel = new Label();
        if (fieldDescriptions.size() > 0) {
            semanticDescrLabel.setText(" (%s)".formatted(descrSemanticStr));
            semanticDescrLabel.getStyleClass().add("descr-semantic");
        } else {
            semanticDescrLabel.setText("");
        }
        row1.getChildren().addAll(otherNameLabel, semanticDescrLabel);

        FlowPane row2 = new FlowPane();
        Label dateAddedLabel = new Label("Date Added:"); dateAddedLabel.getStyleClass().add("descr-semantic");
        Label dateLabel = new Label(time);                    dateLabel.getStyleClass().add("descr-semantic");

        Hyperlink attachmentHyperlink = new Hyperlink("Attachment");
        Hyperlink commentHyperlink = new Hyperlink("Comment");
        row2.getChildren().addAll(dateAddedLabel, dateLabel, attachmentHyperlink, commentHyperlink);

        rowGroup.getChildren().addAll(row1, row2);
        return rowGroup;
    }

    private void updateFQNSemantics(SemanticEntityVersion semanticEntityVersion, List<String> fieldDescriptions) {
        DateTimeFormatter DATE_TIME_FORMATTER = DateTimeFormatter.ofPattern("MMM dd, yyyy");
        // Latest FQN
        latestFqnText.setText((String) semanticEntityVersion.fieldValues().get(1));

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
                    String casSigText = viewCalculator.getRegularDescriptionText(((ConceptFacade) caseSigConcept).nid()).get();
                    String langText = viewCalculator.getRegularDescriptionText(((ConceptFacade) langConcept).nid()).get();

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
     * @param viewProperties view properties containing a view calculator
     * @param conceptFacade a concept facade.
     */
    private void updateAxioms(ViewProperties viewProperties, EntityFacade conceptFacade) {
        // clear Axioms areas
        inferredAxiomsVBox.getChildren().clear();
        statedAxiomsVBox.getChildren().clear();

        ViewCalculator viewCalculator = viewProperties.calculator();


        // Create a SheetItem (AXIOM inferred semantic version)
        // TODO Should this be reused instead of instanciating a new one everytime?
        KometPropertySheet inferredPropertySheet = new KometPropertySheet(viewProperties, true);
        Latest<SemanticEntityVersion> inferredSemanticVersion = viewCalculator.getInferredAxiomSemanticForEntity(conceptFacade.nid());
        makeSheetItem(viewProperties, inferredPropertySheet, inferredSemanticVersion);
        inferredAxiomsVBox.getChildren().add(inferredPropertySheet);

        // Create a SheetItem (AXIOM stated semantic version)
        KometPropertySheet statedPropertySheet = new KometPropertySheet(viewProperties, true);
        Latest<SemanticEntityVersion> statedSemanticVersion    = viewCalculator.getStatedAxiomSemanticForEntity(conceptFacade.nid());
        makeSheetItem(viewProperties, statedPropertySheet, statedSemanticVersion);
        statedAxiomsVBox.getChildren().add(statedPropertySheet);

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
        definitionText.setText("");
        identifierText.setText("");
        lastUpdatedText.setText("");
        moduleText.setText("");
        pathText.setText("");
        originationText.setText("");
        statusText.setText("");
        authorTooltip.setText("");
        //inferredAxiomsVBox.getChildren().clear(); // Defaults to 'Not Available'
        //statedAxiomsVBox.getChildren().clear();   // Defaults to 'Not Available'
    }
    @FXML
    private void displayEditConceptView(ActionEvent event) {
        event.consume();
        LOG.info(event.toString());
    }
    public void displayEditConceptView(ViewProperties viewProperties, KometPreferences nodePreferences, EntityFacade entityFacade){

    }
}
