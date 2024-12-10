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
package dev.ikm.komet.kview.mvvm.view.genediting;


import static dev.ikm.komet.kview.events.genediting.PropertyPanelEvent.CLOSE_PANEL;
import static dev.ikm.komet.kview.events.genediting.PropertyPanelEvent.OPEN_PANEL;
import static dev.ikm.komet.kview.fxutils.TitledPaneHelper.putArrowOnRight;
import static dev.ikm.komet.kview.fxutils.ViewportHelper.clipChildren;
import static dev.ikm.komet.kview.mvvm.viewmodel.DescrNameViewModel.MODULES_PROPERTY;
import static dev.ikm.komet.kview.mvvm.viewmodel.FormViewModel.VIEW_PROPERTIES;
import static dev.ikm.komet.kview.mvvm.viewmodel.GenEditingViewModel.REF_COMPONENT;
import static dev.ikm.komet.kview.mvvm.viewmodel.GenEditingViewModel.SEMANTIC;
import static dev.ikm.komet.kview.mvvm.viewmodel.GenEditingViewModel.WINDOW_TOPIC;
import static dev.ikm.komet.kview.mvvm.viewmodel.StampViewModel.PATHS_PROPERTY;
import static dev.ikm.tinkar.coordinate.stamp.StampFields.AUTHOR;
import static dev.ikm.tinkar.coordinate.stamp.StampFields.MODULE;
import static dev.ikm.tinkar.coordinate.stamp.StampFields.PATH;
import static dev.ikm.tinkar.coordinate.stamp.StampFields.STATUS;
import static dev.ikm.tinkar.coordinate.stamp.StampFields.TIME;
import dev.ikm.komet.framework.Identicon;
import dev.ikm.komet.framework.events.EvtBusFactory;
import dev.ikm.komet.framework.events.EvtType;
import dev.ikm.komet.framework.events.Subscriber;
import dev.ikm.komet.framework.view.ViewProperties;
import dev.ikm.komet.kview.events.genediting.PropertyPanelEvent;
import dev.ikm.komet.kview.fxutils.MenuHelper;
import dev.ikm.komet.kview.mvvm.model.DataModelHelper;
import dev.ikm.komet.kview.mvvm.model.DescrName;
import dev.ikm.komet.kview.mvvm.model.PatternField;
import dev.ikm.komet.kview.mvvm.view.stamp.StampEditController;
import dev.ikm.komet.kview.mvvm.viewmodel.GenEditingViewModel;
import dev.ikm.komet.kview.mvvm.viewmodel.StampViewModel;
import dev.ikm.tinkar.common.id.IntIdSet;
import dev.ikm.tinkar.component.Concept;
import dev.ikm.tinkar.coordinate.language.calculator.LanguageCalculator;
import dev.ikm.tinkar.coordinate.stamp.calculator.Latest;
import dev.ikm.tinkar.coordinate.stamp.calculator.StampCalculator;
import dev.ikm.tinkar.coordinate.view.calculator.ViewCalculator;
import dev.ikm.tinkar.entity.ConceptEntity;
import dev.ikm.tinkar.entity.EntityVersion;
import dev.ikm.tinkar.entity.FieldRecord;
import dev.ikm.tinkar.entity.PatternEntity;
import dev.ikm.tinkar.entity.PatternEntityVersion;
import dev.ikm.tinkar.entity.SemanticEntityVersion;
import dev.ikm.tinkar.entity.StampEntity;
import dev.ikm.tinkar.entity.StampEntityVersion;
import dev.ikm.tinkar.entity.graph.DiTreeEntity;
import dev.ikm.tinkar.terms.ConceptFacade;
import dev.ikm.tinkar.terms.EntityFacade;
import dev.ikm.tinkar.terms.PatternFacade;
import dev.ikm.tinkar.terms.SemanticFacade;
import dev.ikm.tinkar.terms.State;
import dev.ikm.tinkar.terms.TinkarTerm;
import javafx.application.Platform;
import javafx.beans.property.ObjectProperty;
import javafx.beans.property.SimpleStringProperty;
import javafx.beans.property.StringProperty;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.geometry.Side;
import javafx.scene.Node;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.TitledPane;
import javafx.scene.control.ToggleButton;
import javafx.scene.control.Tooltip;
import javafx.scene.image.ImageView;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.Pane;
import javafx.scene.layout.Region;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Color;
import javafx.scene.shape.FillRule;
import javafx.scene.shape.SVGPath;
import javafx.scene.text.Text;
import javafx.scene.text.TextFlow;
import org.carlfx.cognitive.loader.Config;
import org.carlfx.cognitive.loader.FXMLMvvmLoader;
import org.carlfx.cognitive.loader.InjectViewModel;
import org.carlfx.cognitive.loader.JFXNode;
import org.carlfx.cognitive.loader.NamedVm;
import org.carlfx.cognitive.viewmodel.ValidationViewModel;
import org.carlfx.cognitive.viewmodel.ViewModel;
import org.controlsfx.control.PopOver;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.swing.text.LabelView;
import java.net.URL;
import java.time.Instant;
import java.time.ZoneOffset;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.function.Consumer;

public class GenEditingDetailsController {

    private static final Logger LOG = LoggerFactory.getLogger(GenEditingDetailsController.class);

    public static final URL PATTERN_PROPERTIES_VIEW_FXML_URL = GenEditingDetailsController.class.getResource("pattern-properties.fxml");

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
    private Label semanticTitleText;

    @FXML
    private Label semanticDescriptionLabel;

    @FXML
    private Text lastUpdatedText;

    @FXML
    private Text moduleText;

    @FXML
    private Text pathText;

    @FXML
    private Text statusText;

    @FXML
    private TitledPane referenceComponentTitledPane;

    @FXML
    private Label refComponentType;

    @FXML
    private ImageView refComponentIdenticonImageView;

    @FXML
    private Label refComponentLabel;

    @FXML
    private TitledPane semanticDetailsTitledPane;

    @FXML
    private VBox semanticDetailsVBox;

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

    @FXML
    private Button editFieldsButton;

        /**
     * Stamp Edit
     */
    private PopOver stampEdit;
    private StampEditController stampEditController;

    @InjectViewModel
    private GenEditingViewModel genEditingViewModel;

    @InjectViewModel
    private StampViewModel stampViewModel;

    String copySVGPath = "M11.7266 8.89513L11.7266 10.7756L14.0916 10.7756L14.0916 13.1738L15.9721 13.1738L15.9721 10.7756L18.4034 10.7756L18.4034 8.89513L15.9721 8.89513L15.9721 6.49699L14.0916 6.49699L14.0916 8.89513L11.7266 8.89513Z M2.40039 18.6975L2.40039 11.7384C2.40038 11.2622 2.40037 10.8492 2.4282 10.5086C2.45764 10.1482 2.52284 9.78699 2.70036 9.43858C2.96422 8.92072 3.38526 8.49969 3.90311 8.23583C4.25153 8.0583 4.61277 7.99311 4.9731 7.96366C5.31375 7.93583 5.72667 7.93584 6.2029 7.93586L7.83625 7.93586L7.83625 6.3025C7.83624 5.82625 7.83623 5.41338 7.86406 5.07271C7.8935 4.71238 7.95869 4.35114 8.13622 4.00272C8.40008 3.48486 8.82111 3.06383 9.33897 2.79997C9.68738 2.62245 10.0486 2.55725 10.409 2.52781C10.7497 2.49997 11.1626 2.49999 11.6389 2.5L18.5977 2.5C19.074 2.49999 19.487 2.49997 19.8277 2.52781C20.188 2.55725 20.5493 2.62245 20.8977 2.79997C21.4155 3.06383 21.8366 3.48486 22.1004 4.00272C22.2779 4.35114 22.3431 4.71237 22.3726 5.07271C22.4004 5.41342 22.4004 5.82635 22.4004 6.30267L22.4004 13.2615C22.4004 13.7378 22.4004 14.1507 22.3726 14.4914C22.3431 14.8518 22.2779 15.213 22.1004 15.5614C21.8366 16.0793 21.4155 16.5003 20.8977 16.7642C20.5493 16.9417 20.188 17.0069 19.8277 17.0363C19.487 17.0642 19.0741 17.0642 18.5978 17.0641L16.9645 17.0641L16.9645 18.6976C16.9645 19.1738 16.9646 19.5866 16.9367 19.9273C16.9073 20.2876 16.8421 20.6489 16.6646 20.9973C16.4007 21.5151 15.9797 21.9362 15.4618 22.2C15.1134 22.3776 14.7522 22.4427 14.3918 22.4722C14.0512 22.5 13.6383 22.5 13.162 22.5L6.20291 22.5C5.72666 22.5 5.31377 22.5 4.9731 22.4722C4.61277 22.4427 4.25153 22.3776 3.90311 22.2C3.38526 21.9362 2.96423 21.5151 2.70036 20.9973C2.52284 20.6489 2.45764 20.2876 2.4282 19.9273C2.40037 19.5866 2.40038 19.1737 2.40039 18.6975ZM4.30243 19.7742C4.2816 19.5192 4.28086 19.1836 4.28086 18.6606L4.28086 11.7752C4.28086 11.2522 4.2816 10.9167 4.30243 10.6617C4.32237 10.4176 4.35595 10.3314 4.37588 10.2923C4.45945 10.1283 4.59281 9.99491 4.75683 9.91134C4.79593 9.89142 4.88212 9.85784 5.12623 9.83789C5.38119 9.81706 5.71676 9.81633 6.23975 9.81633L7.83625 9.81633L7.83625 13.2616C7.83624 13.7378 7.83623 14.1508 7.86406 14.4914C7.8935 14.8518 7.95869 15.213 8.13622 15.5614C8.40008 16.0793 8.82111 16.5003 9.33897 16.7642C9.68739 16.9417 10.0486 17.0069 10.409 17.0363C10.7496 17.0642 11.1626 17.0642 11.6388 17.0641L15.0841 17.0641L15.0841 18.6606C15.0841 19.1836 15.0833 19.5192 15.0625 19.7742C15.0426 20.0183 15.009 20.1045 14.9891 20.1436C14.9055 20.3076 14.7721 20.4409 14.6081 20.5245C14.569 20.5444 14.4828 20.578 14.2387 20.598C13.9837 20.6188 13.6482 20.6195 13.1252 20.6195L6.23975 20.6195C5.71676 20.6195 5.38119 20.6188 5.12623 20.598C4.88212 20.578 4.79593 20.5444 4.75683 20.5245C4.59281 20.4409 4.45945 20.3076 4.37588 20.1436C4.35595 20.1045 4.32237 20.0183 4.30243 19.7742ZM9.73829 14.3383C9.71746 14.0833 9.71672 13.7478 9.71672 13.2248L9.71672 6.33936C9.71672 5.81637 9.71745 5.4808 9.73829 5.22584C9.75823 4.98173 9.79181 4.89554 9.81173 4.85644C9.89531 4.69241 10.0287 4.55906 10.1927 4.47548C10.2318 4.45556 10.318 4.42198 10.5621 4.40204C10.817 4.38121 11.1526 4.38047 11.6756 4.38047L18.561 4.38047C19.084 4.38047 19.4196 4.38121 19.6745 4.40204C19.9187 4.42198 20.0048 4.45556 20.0439 4.47548C20.208 4.55906 20.3413 4.69241 20.4249 4.85644C20.4448 4.89554 20.4784 4.98173 20.4984 5.22584C20.5192 5.4808 20.5199 5.81637 20.5199 6.33936L20.5199 13.2248C20.5199 13.7478 20.5192 14.0833 20.4984 14.3383C20.4784 14.5824 20.4448 14.6686 20.4249 14.7077C20.3413 14.8717 20.208 15.0051 20.044 15.0887C20.0049 15.1086 19.9187 15.1422 19.6746 15.1621C19.4196 15.1829 19.084 15.1837 18.561 15.1837L11.6756 15.1837C11.1526 15.1837 10.817 15.1829 10.5621 15.1621C10.318 15.1422 10.2318 15.1086 10.1927 15.0887C10.0287 15.0051 9.89531 14.8717 9.81173 14.7077C9.79181 14.6686 9.75823 14.5824 9.73829 14.3383Z";

    private Subscriber<PropertyPanelEvent> propertiesEventSubscriber;


    public GenEditingDetailsController() {}

    @FXML
    private void initialize() {
        // clear all semantic details.
        semanticDetailsVBox.getChildren().clear();

        EntityFacade semantic = genEditingViewModel.getPropertyValue(SEMANTIC);
        Latest<SemanticEntityVersion> semanticEntityVersionLatest = null;
        StampCalculator stampCalculator = getViewProperties().calculator().stampCalculator();
        LanguageCalculator languageCalculator = getViewProperties().calculator().languageCalculator();
        if (semantic != null) {
            semanticEntityVersionLatest = stampCalculator.latest(semantic.nid());
            semanticEntityVersionLatest.ifPresent(semanticEntityVersion -> {
                Latest<PatternEntityVersion> patternEntityVersionLatest = stampCalculator.latest(semanticEntityVersion.pattern());
                patternEntityVersionLatest.ifPresent(patternEntityVersion -> {
                    semanticDescriptionLabel.setText("Semantic for %s".formatted(patternEntityVersion.entity().description()));
                    String meaning = languageCalculator.getDescriptionText(patternEntityVersion.semanticMeaningNid()).orElse("No Description");
                    String purpose = languageCalculator.getDescriptionText(patternEntityVersion.semanticPurposeNid()).orElse("No Description");
                    semanticMeaningText.setText(meaning);
                    semanticPurposeText.setText(purpose);
                });
            });
        } else {
            semanticDescriptionLabel.setText("New Semantic no Pattern associated.");
        }

        // update reference component
        setupReferenceComponentUI(semanticEntityVersionLatest);

        // Setup Stamp
        setupStampPopup(semanticEntityVersionLatest);
        updateUIStamp(getStampViewModel());

        // Populate the Semantic Details
        setupSemanticDetailsUI(semanticEntityVersionLatest);

        // Setup Properties
        setupProperties();
    }

    private String text(int nid) {
        return getViewProperties().calculator().languageCalculator().getDescriptionText(nid).orElse("No Description found");
    }

    private void setupSemanticDetailsUI(Latest<SemanticEntityVersion> semanticEntityVersionLatest) {
        semanticEntityVersionLatest.ifPresent(semanticEntityVersion -> {
            StampCalculator stampCalculator = getViewProperties().calculator().stampCalculator();
            Latest<PatternEntityVersion> patternEntityVersionLatest = stampCalculator.latest(semanticEntityVersion.pattern());
            patternEntityVersionLatest.ifPresent(patternEntityVersion -> {
                List<FieldRecord<Object>> fieldRecords = DataModelHelper.fieldRecords(semanticEntityVersion, patternEntityVersion);
                fieldRecords.forEach(fieldRecord -> {
                    Node readOnlyNode = null;
                    System.out.println("---> dataType() " + fieldRecord.dataType().description());
                    int dataTypeNid = fieldRecord.dataType().nid();
                    if (dataTypeNid == TinkarTerm.COMPONENT_FIELD.nid()) {
                        // load a read-only component
                        readOnlyNode = createReadOnlyComponent(fieldRecord);
                    } else if (dataTypeNid == TinkarTerm.STRING_FIELD.nid() || fieldRecord.dataType().nid() == TinkarTerm.STRING.nid()) {
                        readOnlyNode = createReadOnlyString(fieldRecord);
                    } else if (dataTypeNid == TinkarTerm.COMPONENT_ID_SET_FIELD.nid()) {
                        readOnlyNode = createReadOnlyComponentSet(fieldRecord);
                    } else if (dataTypeNid == TinkarTerm.DITREE_FIELD.nid()) {
                        readOnlyNode = createReadOnlyDiTree(fieldRecord);
                    }

                    // Add to VBox
                    if (readOnlyNode != null) {
                        semanticDetailsVBox.getChildren().add(readOnlyNode);
                    }
                    System.out.println("field record: " + fieldRecord);
                });
            });
        });
    }

    private Node createReadOnlyDiTree(FieldRecord<Object> fieldRecord) {
        JFXNode<Node, Void> jfxNode = FXMLMvvmLoader.make(this.getClass().getResource("read-only-ditree-field.fxml"));
        Node componentRow = jfxNode.node();
        // update field's meaning title label
        Label fieldMeaning = (Label) componentRow.lookup(".semantic-field-type-label");
        fieldMeaning.setTooltip(new Tooltip(text(fieldRecord.purposeNid())));
        fieldMeaning.setText(text(fieldRecord.meaningNid()));

        // update field's purpose
        TextFlow fieldValue = (TextFlow) componentRow.lookup(".semantic-field-ditree-value");
        DiTreeEntity value = (DiTreeEntity) fieldRecord.value();
        fieldValue.getChildren().add(new Text(value.toString()));
        return componentRow;
    }


    /**
     * Creates a read-only component. Such as language value is English Language.
     * @param fieldRecord
     * @return JavaFX Node representing a component for semantic details.
     */
    private Node createReadOnlyComponent(FieldRecord<Object> fieldRecord) {
        JFXNode<Node, Void> jfxNode = FXMLMvvmLoader.make(this.getClass().getResource("read-only-component-field.fxml"));
        Node componentRow = jfxNode.node();
        // update field's meaning title label
        Label fieldMeaning = (Label) componentRow.lookup(".semantic-field-type-label");
        fieldMeaning.setTooltip(new Tooltip(text(fieldRecord.purposeNid())));
        fieldMeaning.setText(text(fieldRecord.meaningNid()));

        // update identicon
        ImageView identicon = (ImageView) componentRow.lookup(".identicon-image-view");
        identicon.setImage(Identicon.generateIdenticonImage(fieldRecord.purpose().publicId()));

        // update field's purpose
        Label fieldValue = (Label) componentRow.lookup(".semantic-field-value");
        EntityFacade value = (EntityFacade) fieldRecord.value();
        fieldValue.setText(value.description());
        return componentRow;
    }
    private Node createReadOnlyComponentSet(FieldRecord<Object> fieldRecord) {
        JFXNode<Node, Void> jfxNode = FXMLMvvmLoader.make(this.getClass().getResource("read-only-component-set-field.fxml"));
        Node componentRow = jfxNode.node();
        // obtain vbox to add items from set
        VBox container = (VBox) componentRow.lookup(".semantic-field-set-container");

        // update field's meaning title label
        Label fieldMeaning = (Label) componentRow.lookup(".semantic-field-type-label");
        fieldMeaning.setTooltip(new Tooltip(text(fieldRecord.purposeNid())));
        fieldMeaning.setText(text(fieldRecord.meaningNid()));

        // loop through all components
        IntIdSet componentSet = (IntIdSet) fieldRecord.value();
        componentSet.forEach(componentId -> {
            Latest<EntityVersion> component = getViewProperties().calculator().stampCalculator().latest(componentId);
            component.ifPresent(entityVersion -> {
                Node componentRow2 = createReadOnlyComponentListItem(component);
                container.getChildren().add(componentRow2);
            });
        });
        return componentRow;
    }

    private Node createReadOnlyComponentListItem(Latest<EntityVersion> component){
        if (component.isPresent()) {
            JFXNode<Node, Void> jfxNodeItem = FXMLMvvmLoader.make(this.getClass().getResource("read-only-component-list-item.fxml"));
            Node componentRowItem = jfxNodeItem.node();
            // update identicon
            ImageView identicon = (ImageView) componentRowItem.lookup(".identicon-image-view");
            identicon.setImage(Identicon.generateIdenticonImage(component.get().publicId()));

            // update field's purpose
            Label fieldValue = (Label) componentRowItem.lookup(".semantic-field-value");
            EntityFacade value = component.get().entity();
            fieldValue.setText(text(value.nid()));
            return componentRowItem;
        }
        return null;
    }
    private Node createReadOnlyString(FieldRecord<Object> fieldRecord) {
        JFXNode<Node, Void> jfxNode = FXMLMvvmLoader.make(this.getClass().getResource("read-only-value-field.fxml"));
        Node componentRow = jfxNode.node();
        // update field's meaning title label
        Label fieldMeaning = (Label) componentRow.lookup(".semantic-field-type-label");
        fieldMeaning.setTooltip(new Tooltip(text(fieldRecord.purposeNid())));
        fieldMeaning.setText(text(fieldRecord.meaningNid()));

        // update field's purpose
        Label fieldValue = (Label) componentRow.lookup(".semantic-field-value");
        Object value = fieldRecord.value();
        fieldValue.setText(String.valueOf(value));
        return componentRow;
    }

    private void setupReferenceComponentUI(Latest<SemanticEntityVersion> semanticEntityVersionLatest) {
        // check if there is a reference component if not check if there is a semantic entity.
        ObjectProperty<EntityFacade> refComponentProp = genEditingViewModel.getProperty(REF_COMPONENT);
        EntityFacade refComponent = refComponentProp.get();

        Consumer<EntityFacade> updateRefComponentInfo = (refComponent2) -> {
            // update items
            String refType = switch (refComponent2) {
                case ConceptFacade ignored -> "Concept";
                case SemanticFacade ignored -> "Semantic";
                case PatternFacade ignored -> "Pattern";
                default -> "Unknown";
            };
            refComponentType.setText(refType);
            refComponentIdenticonImageView.setImage(Identicon.generateIdenticonImage(refComponent2.publicId()));
            refComponentLabel.setText(refComponent2.description());
        };

        // when ever the property REF_COMPONENT changes update the UI.
        refComponentProp.addListener((observable, oldValue, newValue) -> {
            updateRefComponentInfo.accept(newValue);
        });

        // if empty look up semantic's reference component.
        if (refComponent == null) {
            if (semanticEntityVersionLatest != null) {
                semanticEntityVersionLatest.ifPresent(semanticEntityVersion -> {
                    refComponentProp.set(semanticEntityVersion.referencedComponent());
                });
            }
        } else {
            updateRefComponentInfo.accept(refComponent);
        }
    }

    private void setupStampPopup(Latest<SemanticEntityVersion> semanticEntityVersionLatest) {
        //initialize stampsViewModel with basic data.
        stampViewModel.setPropertyValue(PATHS_PROPERTY, stampViewModel.findAllPaths(getViewProperties()), true)
                .setPropertyValue(MODULES_PROPERTY, stampViewModel.findAllModules(getViewProperties()), true);

        // populate STAMP values
        StampEntity stampEntity = semanticEntityVersionLatest.get().stamp();
        stampViewModel.setPropertyValue(STATUS, stampEntity.state())
                .setPropertyValue(TIME, stampEntity.time())
                .setPropertyValue(AUTHOR, stampEntity.author())
                .setPropertyValue(MODULE, stampEntity.module())
                .setPropertyValue(PATH, stampEntity.path())
        ;
        stampViewModel.save(true);
    }

//    private ContextMenu createContextMenuForPatternField(PatternField selectedPatternField) {
//
//        Object[][] menuItems = new Object[][]{
//                {"Edit", true, new String[]{"menu-item"}, (EventHandler<ActionEvent>) actionEvent -> showEditFieldsPanel(actionEvent, selectedPatternField), createGraphics("edit-icon")},
//                {MenuHelper.SEPARATOR},
//                {"Copy", false, new String[]{"menu-item"}, null, createSVGGraphic(copySVGPath)},
//                {"Save to Favorites",  false, new String[]{"menu-item"}, null, createGraphics("favorites-icon")},
//                {MenuHelper.SEPARATOR},
//                {"Add Comment",  false, new String[]{"menu-item"}, null, createGraphics("comment-icon")},
//                {"Remove", true, new String[]{"menu-item"}, (EventHandler<ActionEvent>) actionEvent -> patternViewModel.getObservableList(FIELDS_COLLECTION).remove(selectedPatternField)
//                , createGraphics("remove-icon")}
//        };
//        return MenuHelper.getInstance().createContextMenuWithMenuItems(menuItems);
//    }

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
        ConceptEntity caseSigConcept = descrName.getCaseSignificance();
        String casSigText = viewCalculator.getRegularDescriptionText(caseSigConcept.nid())
                .orElse(caseSigConcept.nid()+"");
        ConceptEntity langConcept = descrName.getLanguage();
        String langText = viewCalculator.getRegularDescriptionText(langConcept.nid())
                .orElse(String.valueOf(langConcept.nid()));
        return "%s | %s".formatted(casSigText, langText);
    }

    private void setupProperties() {
        // Setup Property screen bump out
        // Load Concept Properties View Panel (FXML & Controller)
//        Config config = new Config(PATTERN_PROPERTIES_VIEW_FXML_URL)
//                .addNamedViewModel(new NamedVm("patternViewModel", patternViewModel))
//                .updateViewModel("patternPropertiesViewModel",
//                        (patternPropertiesViewModel) -> patternPropertiesViewModel
//                                .setPropertyValue(PATTERN_TOPIC, patternViewModel.getPropertyValue(PATTERN_TOPIC))
//                                .setPropertyValue(VIEW_PROPERTIES, patternViewModel.getPropertyValue(VIEW_PROPERTIES) )
//                                .setPropertyValue(STATE_MACHINE, patternViewModel.getPropertyValue(STATE_MACHINE))
//                );
//
//        JFXNode<BorderPane, PropertiesController> propsFXMLLoader = FXMLMvvmLoader.make(config);
//        this.propertiesBorderPane = propsFXMLLoader.node();
//        this.propertiesController = propsFXMLLoader.controller();
//        attachPropertiesViewSlideoutTray(this.propertiesBorderPane);

        //FIXME this doesn't work properly, should leave for a future effort...
        // open the panel, allow the state machine to determine which panel to show
        //EvtBusFactory.getDefaultEvtBus().publish(patternViewModel.getPropertyValue(PATTERN_TOPIC), new PropertyPanelEvent(propertiesToggleButton, OPEN_PANEL));
    }

    public ViewProperties getViewProperties() {
        return genEditingViewModel.getPropertyValue(VIEW_PROPERTIES);
    }


    private Consumer<GenEditingDetailsController> onCloseConceptWindow;

    public void setOnCloseConceptWindow(Consumer<GenEditingDetailsController> onClose) {
        this.onCloseConceptWindow = onClose;
    }

    public void onReasonerSlideoutTray(Consumer<ToggleButton> reasonerResultsControllerConsumer) {
        this.reasonerResultsControllerConsumer = reasonerResultsControllerConsumer;
    }

    @FXML
    void closeConceptWindow(ActionEvent event) {
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
    private void showAddEditRefComponentPanel(ActionEvent actionEvent) {

    }
    private void showEditFieldsPanel(ActionEvent actionEvent, PatternField selectedPatternField) {
        LOG.info("Todo show bump out and display Edit Fields panel \n" + actionEvent);
        actionEvent.consume();
    }

    @FXML
    private void showAddFieldsPanel(ActionEvent actionEvent) {
        LOG.info("Todo show bump out and display Add Fields panel \n" + actionEvent);
    }

    @FXML
    private void popupAddDescriptionContextMenu(ActionEvent actionEvent) {
        MenuHelper.fireContextMenuEvent(actionEvent, Side.RIGHT, 0, 0);
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
        Config stampConfig = new Config(StampEditController.class.getResource("stamp-edit.fxml"));
        stampConfig.addNamedViewModel(new NamedVm("stampViewModel", getStampViewModel()));
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
            genEditingViewModel.save();
            updateUIStamp(getStampViewModel());
        });

        popOver.show((Node) event.getSource());

        // store and use later.
        stampEdit = popOver;
        this.stampEditController = stampEditController;
    }

    private void updateUIStamp(ViewModel stampViewModel) {
        updateTimeText(stampViewModel.getValue(TIME));
        ConceptEntity moduleEntity = stampViewModel.getValue(MODULE);
        if (moduleEntity == null) {
            LOG.warn("Must select a valid module for Stamp.");
            return;
        }
        moduleText.setText(moduleEntity.description());
        ConceptEntity pathEntity = stampViewModel.getValue(PATH);
        pathText.setText(pathEntity.description());
        State status = stampViewModel.getValue(STATUS);
        statusText.setText(status.name());
    }

    private void updateTimeText(Long time) {
        DateTimeFormatter DATE_TIME_FORMATTER = DateTimeFormatter.ofPattern("yyyy-MMM-dd HH:mm:ss");
        Instant stampInstance = Instant.ofEpochSecond(time/1000);
        ZonedDateTime stampTime = ZonedDateTime.ofInstant(stampInstance, ZoneOffset.UTC);
        String lastUpdated = DATE_TIME_FORMATTER.format(stampTime);
        lastUpdatedText.setText(lastUpdated);
    }

    public ValidationViewModel getStampViewModel() {
        return stampViewModel;
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
        EvtBusFactory.getDefaultEvtBus().publish(genEditingViewModel.getPropertyValue(WINDOW_TOPIC), new PropertyPanelEvent(propertyToggle, eventEvtType));
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


    public void putTitlePanesArrowOnRight() {
        putArrowOnRight(this.referenceComponentTitledPane);
        putArrowOnRight(this.semanticDetailsTitledPane);
    }




    @FXML
    private void save(ActionEvent actionEvent) {
//        boolean isValidSave = genEditingViewModel.createPattern();
//        LOG.info(isValidSave ? "success" : "failed");
//        if(isValidSave){
//            EvtBusFactory.getDefaultEvtBus().publish(SAVE_PATTERN_TOPIC, new PatternCreationEvent(actionEvent.getSource(), PATTERN_CREATION_EVENT));
//        }
    }

}
