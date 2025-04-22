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


import static dev.ikm.komet.framework.observable.ObservableEntity.updateVersions;
import static dev.ikm.komet.kview.events.genediting.PropertyPanelEvent.CLOSE_PANEL;
import static dev.ikm.komet.kview.events.genediting.PropertyPanelEvent.OPEN_PANEL;
import static dev.ikm.komet.kview.events.genediting.PropertyPanelEvent.SHOW_ADD_REFERENCE_SEMANTIC_FIELD;
import static dev.ikm.komet.kview.events.genediting.PropertyPanelEvent.SHOW_EDIT_SEMANTIC_FIELDS;
import static dev.ikm.komet.kview.events.genediting.PropertyPanelEvent.SHOW_EDIT_SINGLE_SEMANTIC_FIELD;
import static dev.ikm.komet.kview.fxutils.SlideOutTrayHelper.isClosed;
import static dev.ikm.komet.kview.fxutils.SlideOutTrayHelper.isOpen;
import static dev.ikm.komet.kview.fxutils.SlideOutTrayHelper.slideIn;
import static dev.ikm.komet.kview.fxutils.SlideOutTrayHelper.slideOut;
import static dev.ikm.komet.kview.fxutils.ViewportHelper.clipChildren;
import static dev.ikm.komet.kview.klfields.KlFieldHelper.retrieveCommittedLatestVersion;
import static dev.ikm.komet.kview.mvvm.viewmodel.DescrNameViewModel.MODULES_PROPERTY;
import static dev.ikm.komet.kview.mvvm.viewmodel.FormViewModel.CREATE;
import static dev.ikm.komet.kview.mvvm.viewmodel.FormViewModel.CURRENT_JOURNAL_WINDOW_TOPIC;
import static dev.ikm.komet.kview.mvvm.viewmodel.FormViewModel.EDIT;
import static dev.ikm.komet.kview.mvvm.viewmodel.FormViewModel.MODE;
import static dev.ikm.komet.kview.mvvm.viewmodel.FormViewModel.VIEW_PROPERTIES;
import static dev.ikm.komet.kview.mvvm.viewmodel.GenEditingViewModel.COMPOSER;
import static dev.ikm.komet.kview.mvvm.viewmodel.GenEditingViewModel.PATTERN;
import static dev.ikm.komet.kview.mvvm.viewmodel.GenEditingViewModel.REF_COMPONENT;
import static dev.ikm.komet.kview.mvvm.viewmodel.GenEditingViewModel.SEMANTIC;
import static dev.ikm.komet.kview.mvvm.viewmodel.GenEditingViewModel.SESSION;
import static dev.ikm.komet.kview.mvvm.viewmodel.GenEditingViewModel.STAMP_VIEW_MODEL;
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
import dev.ikm.komet.framework.observable.ObservableEntity;
import dev.ikm.komet.framework.observable.ObservableField;
import dev.ikm.komet.framework.observable.ObservableSemantic;
import dev.ikm.komet.framework.observable.ObservableSemanticSnapshot;
import dev.ikm.komet.framework.observable.ObservableSemanticVersion;
import dev.ikm.komet.framework.view.ViewProperties;
import dev.ikm.komet.kview.controls.ComponentItem;
import dev.ikm.komet.kview.controls.KLReadOnlyBaseControl;
import dev.ikm.komet.kview.controls.KLReadOnlyComponentControl;
import dev.ikm.komet.kview.events.genediting.GenEditingEvent;
import dev.ikm.komet.kview.events.genediting.PropertyPanelEvent;
import dev.ikm.komet.kview.klfields.KlFieldHelper;
import dev.ikm.komet.kview.mvvm.model.DataModelHelper;
import dev.ikm.komet.kview.mvvm.view.stamp.StampEditController;
import dev.ikm.komet.kview.mvvm.viewmodel.GenEditingViewModel;
import dev.ikm.komet.kview.mvvm.viewmodel.StampViewModel;
import dev.ikm.tinkar.composer.Composer;
import dev.ikm.tinkar.composer.Session;
import dev.ikm.tinkar.coordinate.language.calculator.LanguageCalculator;
import dev.ikm.tinkar.coordinate.stamp.calculator.Latest;
import dev.ikm.tinkar.coordinate.stamp.calculator.StampCalculator;
import dev.ikm.tinkar.entity.ConceptEntity;
import dev.ikm.tinkar.entity.Entity;
import dev.ikm.tinkar.entity.EntityService;
import dev.ikm.tinkar.entity.FieldRecord;
import dev.ikm.tinkar.entity.PatternEntityVersion;
import dev.ikm.tinkar.entity.PatternVersionRecord;
import dev.ikm.tinkar.entity.SemanticEntityVersion;
import dev.ikm.tinkar.entity.StampEntity;
import dev.ikm.tinkar.terms.ConceptFacade;
import dev.ikm.tinkar.terms.EntityFacade;
import dev.ikm.tinkar.terms.PatternFacade;
import dev.ikm.tinkar.terms.SemanticFacade;
import dev.ikm.tinkar.terms.State;
import dev.ikm.tinkar.terms.TinkarTerm;
import javafx.beans.property.ObjectProperty;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.Node;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.TitledPane;
import javafx.scene.control.ToggleButton;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.Pane;
import javafx.scene.layout.Region;
import javafx.scene.layout.VBox;
import javafx.scene.text.Text;
import org.carlfx.cognitive.loader.Config;
import org.carlfx.cognitive.loader.FXMLMvvmLoader;
import org.carlfx.cognitive.loader.InjectViewModel;
import org.carlfx.cognitive.loader.JFXNode;
import org.carlfx.cognitive.loader.NamedVm;
import org.carlfx.cognitive.viewmodel.ValidationViewModel;
import org.carlfx.cognitive.viewmodel.ViewModel;
import org.controlsfx.control.PopOver;
import org.eclipse.collections.api.list.ImmutableList;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.net.URL;
import java.time.Instant;
import java.time.ZoneOffset;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.function.BiFunction;
import java.util.function.Consumer;

public class GenEditingDetailsController {

    private static final Logger LOG = LoggerFactory.getLogger(GenEditingDetailsController.class);

    public static final URL GENEDITING_PROPERTIES_VIEW_FXML_URL = GenEditingDetailsController.class.getResource("genediting-properties.fxml");

    private Consumer<ToggleButton> reasonerResultsControllerConsumer;

    @FXML
    private KLReadOnlyComponentControl referenceComponent;

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
    private TitledPane semanticDetailsTitledPane;

    @FXML
    private VBox semanticDetailsVBox;

    private PropertiesController propertiesController;

    private BorderPane propertiesBorderPane;

    @FXML
    private Text semanticMeaningText;

    @FXML
    private Text semanticPurposeText;

    @FXML
    private Button addReferenceButton;

    @FXML
    private Button editFieldsButton;

    @FXML
    private Button saveButton;

    @InjectViewModel
    private StampViewModel stampViewModel;

    @InjectViewModel
    private GenEditingViewModel genEditingViewModel;

    private List<ObservableField<?>> observableFields = new ArrayList<>();

    private List<Node> nodes = new ArrayList<>();

    /**
     * Stamp Edit
     */
    private PopOver stampEdit;
    private StampEditController stampEditController;

    private Subscriber<PropertyPanelEvent> propertiesEventSubscriber;

    private Latest<SemanticEntityVersion> semanticEntityVersionLatest;

    ObservableSemantic observableSemantic;

    ObservableSemanticSnapshot observableSemanticSnapshot;

    public GenEditingDetailsController() {
    }

    @FXML
    private void initialize() {
        // clear all semantic details.
        semanticDetailsVBox.getChildren().clear();
        EntityFacade semantic = genEditingViewModel.getPropertyValue(SEMANTIC);
        Composer composer = new Composer("Semantic composer");
        genEditingViewModel.setPropertyValue(COMPOSER, composer);

        // if the semantic is null, then we generate a default one
        if (semantic == null) {
            genEditingViewModel.setPropertyValue(MODE, CREATE);
            EntityFacade pattern = genEditingViewModel.getPropertyValue(PATTERN);

            StampViewModel defaultStampViewModel = new StampViewModel();
            defaultStampViewModel.setPropertyValue(STATUS, State.ACTIVE);
            defaultStampViewModel.setPropertyValue(AUTHOR, TinkarTerm.USER);
            defaultStampViewModel.setPropertyValue(MODULE, TinkarTerm.DEVELOPMENT_MODULE);
            defaultStampViewModel.setPropertyValue(PATH, TinkarTerm.DEVELOPMENT_PATH);
            Session session = composer.open(State.ACTIVE, TinkarTerm.USER, TinkarTerm.DEVELOPMENT_MODULE, TinkarTerm.DEVELOPMENT_PATH);
            defaultStampViewModel.save();

            genEditingViewModel.setPropertyValue(STAMP_VIEW_MODEL, defaultStampViewModel);

            // create empty semantic for the pattern and set it in the view model
            genEditingViewModel.setPropertyValue(SESSION, session);
            semantic = DataModelHelper.createEmptySemantic(getViewProperties(), pattern, session);

            genEditingViewModel.setPropertyValue(SEMANTIC, semantic);
        } else {
            genEditingViewModel.setPropertyValue(MODE, EDIT);
        }

        StampCalculator stampCalculator = getViewProperties().calculator().stampCalculator();
        LanguageCalculator languageCalculator = getViewProperties().calculator().languageCalculator();
        observableSemantic = ObservableEntity.get(semantic.nid());
        updateVersions(Entity.getFast(observableSemantic.nid()), observableSemantic);
        observableSemanticSnapshot = observableSemantic.getSnapshot(getViewProperties().calculator());
        //retrieve latest committed semanticVersion
        semanticEntityVersionLatest = stampCalculator.latest(semantic.nid());
        if(semanticEntityVersionLatest.get().stamp().lastVersion().uncommitted()){
            semanticEntityVersionLatest = retrieveCommittedLatestVersion(semanticEntityVersionLatest, observableSemanticSnapshot);
        }
        semanticEntityVersionLatest.ifPresent(semanticEntityVersion -> {
            Latest<PatternEntityVersion> patternEntityVersionLatest = stampCalculator.latest(semanticEntityVersion.pattern());
            patternEntityVersionLatest.ifPresent(patternEntityVersion -> {
                String meaning = languageCalculator.getDescriptionText(patternEntityVersion.semanticMeaningNid()).orElse("No Description");
                String purpose = languageCalculator.getDescriptionText(patternEntityVersion.semanticPurposeNid()).orElse("No Description");
                semanticMeaningText.setText(meaning);
                semanticPurposeText.setText(purpose);

                String patternFQN = getViewProperties().calculator().languageCalculator()
                        .getFullyQualifiedDescriptionTextWithFallbackOrNid(patternEntityVersion.nid());
                semanticDescriptionLabel.setText("Semantic for %s".formatted(patternFQN));
                semanticTitleText.setText("%s of component for %s in %s".formatted(meaning, purpose, patternFQN));
            });
        });

        // Setup Stamp section
        setupStampPopup(semanticEntityVersionLatest);
        updateUIStamp(getStampViewModel());

        // Update reference component section
        setupReferenceComponentUI(semanticEntityVersionLatest);

        // Populate the Semantic Details

        if (genEditingViewModel.getPropertyValue(MODE).equals(EDIT)) {
            setUpObservables();
        } else {
            EntityFacade pattern = genEditingViewModel.getPropertyValue(PATTERN);
            PatternVersionRecord patternVersionRecord = (PatternVersionRecord) getViewProperties().calculator().latest(pattern).get();

            // generate read only UI controls in create mode
            List<KLReadOnlyBaseControl> readOnlyControls = KlFieldHelper.addReadOnlyBlankControlsToContainer(patternVersionRecord, getViewProperties());
            nodes.addAll(readOnlyControls);
            semanticDetailsVBox.getChildren().addAll(readOnlyControls);
        }

        // Setup Properties Bump out view.
        setupProperties();

        //Set up the Listener to refresh the details area (After user hits submit button on the right side)
        EntityFacade finalSemantic = semantic;
        Latest<SemanticEntityVersion> latestSemanticEntityVersion = getViewProperties().calculator().stampCalculator().latest(semantic.nid());
        Subscriber<GenEditingEvent> refreshSubscriber = evt -> {
            if (evt.getEventType() == GenEditingEvent.PUBLISH && evt.getNid() == finalSemantic.nid()) {
                if (genEditingViewModel.getPropertyValue(MODE).equals(CREATE)) {
                    // populate the semantic and its observable fields once saved
                    semanticEntityVersionLatest = retrieveCommittedLatestVersion(latestSemanticEntityVersion, observableSemantic.getSnapshot(getViewProperties().calculator()));

                    // clear out the temporary placeholders
                    semanticDetailsVBox.getChildren().clear();
                    nodes.clear();

                    // set up the real observables now that the semantic has been created
                    setUpObservables();

                    // change the mode from CREATE to EDIT
                    genEditingViewModel.setPropertyValue(MODE, EDIT);
                }
                for (int i = 0; i < evt.getList().size(); i++) {
                    ObservableField field = observableFields.get(i);
                    ObservableField updatedField = evt.getList().get(i);
                    if (updatedField != null && field != null) {
                        // readonly integer value 1, editable integer value 1 don't update
                        // readonly integer value 1, editable integer value 5 do update
                        // readonly IntIdSet value [1,2] editable IntIdSet value [1,2] don't update
                        // Should we check if the value is different before updating? (blindly updating now).
                        //if (!field.value().equals(updatedField.valueProperty())) {
                            field.valueProperty().setValue(updatedField.valueProperty().getValue());
                        //}
                    }
                }
            } /*else if (evt.getEventType() == REFERENCE_COMPONENT_CHANGED_EVENT) {
                setupReferenceComponentUI(semanticEntityVersionLatest);
            }*/
        };
        EvtBusFactory.getDefaultEvtBus().subscribe(genEditingViewModel.getPropertyValue(CURRENT_JOURNAL_WINDOW_TOPIC),
                GenEditingEvent.class, refreshSubscriber);
    }

    private void loadUIData() {
        nodes.clear();
        if (semanticEntityVersionLatest.isPresent()) {
            observableSemanticSnapshot = observableSemantic.getSnapshot(getViewProperties().calculator());
            ImmutableList<ObservableSemanticVersion> observableSemanticVersionImmutableList = observableSemanticSnapshot.getHistoricVersions();
            if (observableSemanticVersionImmutableList.isEmpty()) {
                observableFields.addAll((Collection) observableSemanticSnapshot.getLatestFields().get());
            } else {
                //Cast to mutable list
                List<ObservableSemanticVersion> observableSemanticVersionList = new ArrayList<>(observableSemanticVersionImmutableList.castToList());
                //filter list to have only the latest semantic version passed as argument and remove rest of the entries.
                observableSemanticVersionList.removeIf(p -> !semanticEntityVersionLatest.stampNids().contains(p.stampNid()));
                if (observableSemanticVersionList.isEmpty()) {
                    observableFields.addAll((Collection) observableSemanticSnapshot.getLatestFields().get());
                } else {
                    ObservableSemanticVersion observableSemanticVersion = observableSemanticVersionList.getFirst();
                    Latest<PatternEntityVersion> latestPatternEntityVersion = getViewProperties().calculator().latestPatternEntityVersion(observableSemanticVersion.patternNid());
                    // Populate the Semantic Details
                    // Displaying editable controls and populating the observable fields array list.
                    observableFields.addAll((Collection) observableSemanticVersion.fields(latestPatternEntityVersion.get()));
                }
            }
            observableFields.forEach(observableField -> {
                // disable calling writeToData method of observable field by setting refresh flag to true.
                FieldRecord<?> fieldRecord = observableField.field();
                nodes.add(KlFieldHelper.generateNode(fieldRecord, observableField, getViewProperties(), semanticEntityVersionLatest, false));
            });
        }
    }
    private void setUpObservables() {
        // populate the observable fields and nodes for this semantic
        loadUIData();
        // function to apply for the components' edit action (a.k.a. right click > Edit)
        BiFunction<KLReadOnlyBaseControl, Integer, Runnable> editAction = (readOnlyBaseControl, fieldIndex) ->
                () -> {

                    EvtBusFactory.getDefaultEvtBus().publish(genEditingViewModel.getPropertyValue(WINDOW_TOPIC),
                            new PropertyPanelEvent(readOnlyBaseControl, SHOW_EDIT_SINGLE_SEMANTIC_FIELD, fieldIndex));
                    EvtBusFactory.getDefaultEvtBus().publish(genEditingViewModel.getPropertyValue(WINDOW_TOPIC),
                            new PropertyPanelEvent(readOnlyBaseControl, OPEN_PANEL));
                };

        // add setEditOnAction
        for (int index = 0; index < nodes.size(); index++) {
            KLReadOnlyBaseControl klReadOnlyBaseControl = (KLReadOnlyBaseControl) nodes.get(index);
            klReadOnlyBaseControl.setOnEditAction(editAction.apply(klReadOnlyBaseControl, index));
            semanticDetailsVBox.getChildren().add(klReadOnlyBaseControl);
        }
    }


    private void setSemanticVersion(Latest<SemanticEntityVersion> semanticEntityVersionLatest) {
        this.semanticEntityVersionLatest = semanticEntityVersionLatest;
    }

    private Latest<SemanticEntityVersion> getSemanticVersion() {
        return semanticEntityVersionLatest;
    }

    /**
     * Upper right button that allows user to edit stamp popup
     *
     * @param semanticEntityVersionLatest
     */
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

    private void updateUIStamp(ViewModel stampViewModel) {
        updateTimeText(stampViewModel.getValue(TIME));
        ConceptEntity moduleEntity = stampViewModel.getValue(MODULE);
        if (moduleEntity == null) {
            LOG.warn("Must select a valid module for Stamp.");
            return;
        }
        if (genEditingViewModel.getPropertyValue(MODE) == EDIT) {
            moduleText.setText(moduleEntity.description());
            ConceptEntity pathEntity = stampViewModel.getValue(PATH);
            pathText.setText(pathEntity.description());
            State status = stampViewModel.getValue(STATUS);
            statusText.setText(status.name());
        }
    }

    public ValidationViewModel getStampViewModel() {
        return stampViewModel;
    }

    private void updateTimeText(Long time) {
        if (genEditingViewModel.getPropertyValue(MODE) == CREATE) {
            lastUpdatedText.setText("");
        } else {
            DateTimeFormatter DATE_TIME_FORMATTER = DateTimeFormatter.ofPattern("yyyy-MMM-dd HH:mm:ss");
            Instant stampInstance = Instant.ofEpochSecond(time / 1000);
            ZonedDateTime stampTime = ZonedDateTime.ofInstant(stampInstance, ZoneOffset.UTC);
            String lastUpdated = DATE_TIME_FORMATTER.format(stampTime);
            lastUpdatedText.setText(lastUpdated);
        }
    }

    /**
     * Display the Reference Component section underneath Semantic Title.
     *
     * @param semanticEntityVersionLatest
     */
    private void setupReferenceComponentUI(Latest<SemanticEntityVersion> semanticEntityVersionLatest) {
        // check if there is a reference component if not check if there is a semantic entity.
        ObjectProperty<EntityFacade> refComponentProp = genEditingViewModel.getProperty(REF_COMPONENT);
        EntityFacade refComponent = refComponentProp.get();

        //Disable the  edit the Reference Component of an existing semantic once submitted
        addReferenceButton.setDisable(refComponent != null);

        Consumer<EntityFacade> updateRefComponentInfo = (refComponent2) -> {
            if (refComponent2 == null) {
                return;
            }
            EntityFacade entityFacade = EntityService.get().getEntityFast(refComponent2.nid());
            // update items
            String refType = switch (entityFacade) {
                case ConceptFacade ignored -> "Concept";
                case SemanticFacade ignored -> "Semantic";
                case PatternFacade ignored -> "Pattern";
                default -> "Unknown";
            };

            ComponentItem componentItem = new ComponentItem(refComponent2.description(), Identicon.generateIdenticonImage(refComponent2.publicId()));

            referenceComponent.setTitle(refType);
            referenceComponent.setValue(componentItem);
        };

        // when ever the property REF_COMPONENT changes update the UI.
        refComponentProp.addListener((observable, oldValue, newValue) -> {
            updateRefComponentInfo.accept(newValue);
        });

        // if empty look up semantic's reference component.
        if (refComponent == null) {
            if (semanticEntityVersionLatest != null) {
                semanticEntityVersionLatest.ifPresent(semanticEntityVersion -> {
                    refComponentProp.set(semanticEntityVersion.referencedComponent().toProxy());
                });
            }
        } else {
            updateRefComponentInfo.accept(refComponent);
        }
    }

    /**
     * Setup the Properties bump out when user clicks on the Properties toggle to slide open the Properties view.
     */
    private void setupProperties() {
        // Setup Property screen bump out
        // Load Concept Properties View Panel (FXML & Controller)
        Config config = new Config(GENEDITING_PROPERTIES_VIEW_FXML_URL)
                .addNamedViewModel(new NamedVm("genEditingViewModel", genEditingViewModel));

        JFXNode<BorderPane, PropertiesController> propsFXMLLoader = FXMLMvvmLoader.make(config);
        this.propertiesBorderPane = propsFXMLLoader.node();
        this.propertiesController = propsFXMLLoader.controller();
        attachPropertiesViewSlideoutTray(this.propertiesBorderPane);

        // open the panel, allow the state machine to determine which panel to show
        // listen for open and close events
        propertiesEventSubscriber = (evt) -> {
            if (evt.getEventType() == dev.ikm.komet.kview.events.genediting.PropertyPanelEvent.CLOSE_PANEL) {
                LOG.info("propBumpOutListener - Close Properties bumpout toggle = " + propertiesToggleButton.isSelected());
                propertiesToggleButton.setSelected(false);
                if (isOpen(propertiesSlideoutTrayPane)) {
                    slideIn(propertiesSlideoutTrayPane, detailsOuterBorderPane);
                }

                // Turn off edit mode for all read only controls
                for (Node node : nodes) {
                    KLReadOnlyBaseControl klReadOnlyBaseControl = (KLReadOnlyBaseControl) node;
                    klReadOnlyBaseControl.setEditMode(false);
                }
            } else if (evt.getEventType() == PropertyPanelEvent.OPEN_PANEL
                    || evt.getEventType() == PropertyPanelEvent.NO_SELECTION_MADE_PANEL) {
                LOG.info("propBumpOutListener - Opening Properties bumpout toggle = " + propertiesToggleButton.isSelected());
                propertiesToggleButton.setSelected(true);
                if (isClosed(propertiesSlideoutTrayPane)) {
                    slideOut(propertiesSlideoutTrayPane, detailsOuterBorderPane);
                }
            }
        };
        EvtBusFactory.getDefaultEvtBus().subscribe(genEditingViewModel.getPropertyValue(WINDOW_TOPIC), PropertyPanelEvent.class, propertiesEventSubscriber);
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

    /**
     * Workaround to place disclosure arrow button to the right of the accordion.
     */
    public void putTitlePanesArrowOnRight() {
        //TODO Fix the right arrow - commenting these lines since it is throwing null pointer.
//        putArrowOnRight(this.referenceComponentTitledPane);
//        putArrowOnRight(this.semanticDetailsTitledPane);
    }

    @FXML
    void closeConceptWindow(ActionEvent event) {
        if (this.onCloseConceptWindow != null) {
            onCloseConceptWindow.accept(this);
        }
        // TODO Create an event to notify children panes to clean up their subscribers.
        EvtBusFactory.getDefaultEvtBus().unsubscribe(genEditingViewModel.getPropertyValue(WINDOW_TOPIC), PropertyPanelEvent.class, propertiesEventSubscriber);
    }

    @FXML
    private void showAddRefComponentPanel(ActionEvent actionEvent) {
        EntityFacade refComponent = genEditingViewModel.getPropertyValue(REF_COMPONENT);

        // notify bump out to display edit fields in bump out area.
        EvtBusFactory.getDefaultEvtBus()
                .publish(genEditingViewModel.getPropertyValue(WINDOW_TOPIC),
                        new PropertyPanelEvent(actionEvent.getSource(),
                                SHOW_ADD_REFERENCE_SEMANTIC_FIELD, refComponent));
        // open properties bump out.
        EvtBusFactory.getDefaultEvtBus().publish(genEditingViewModel.getPropertyValue(WINDOW_TOPIC), new PropertyPanelEvent(actionEvent.getSource(), OPEN_PANEL));
    }

    @FXML
    private void showAndEditSemanticFieldsPanel(ActionEvent actionEvent) {
        EntityFacade semantic = genEditingViewModel.getPropertyValue(SEMANTIC);

        // notify bump out to display edit fields in bump out area.
        EvtBusFactory.getDefaultEvtBus()
                .publish(genEditingViewModel.getPropertyValue(WINDOW_TOPIC),
                        new PropertyPanelEvent(actionEvent.getSource(),
                                SHOW_EDIT_SEMANTIC_FIELDS, semantic));
        // open properties bump out.
        EvtBusFactory.getDefaultEvtBus().publish(genEditingViewModel.getPropertyValue(WINDOW_TOPIC), new PropertyPanelEvent(actionEvent.getSource(), OPEN_PANEL));

        // Set all controls to edit mode
        for (Node node : nodes) {
            KLReadOnlyBaseControl klReadOnlyBaseControl = (KLReadOnlyBaseControl) node;
            klReadOnlyBaseControl.setEditMode(true);
        }
    }

    @FXML
    private void openReasonerSlideout(ActionEvent actionEvent) {
        // TODO: perform reasoner
    }

    @FXML
    private void openTimelinePanel(ActionEvent actionEvent) {
        // TODO: perform reasoner
    }

    @FXML
    public void popupStampEdit(ActionEvent event) {
        if (stampEdit != null && stampEditController != null) {
            // refresh modules
            stampViewModel.getObservableList(StampViewModel.MODULES_PROPERTY).clear();
            stampViewModel.getObservableList(StampViewModel.MODULES_PROPERTY).addAll(stampViewModel.findAllModules(getViewProperties()));

            // refresh path
            stampViewModel.getObservableList(PATHS_PROPERTY).clear();
            stampViewModel.getObservableList(PATHS_PROPERTY).addAll(stampViewModel.findAllPaths(getViewProperties()));

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

    /**
     * When user clicks on the pencil icon to reveal the dynamic edit (KlFields) fields.
     *
     * @param actionEvent Button click action
     */
    @FXML
    private void showSemanticEditFieldsPanel(ActionEvent actionEvent) {
        LOG.info("Todo show bump out and display Edit Fields panel \n" + actionEvent);
        actionEvent.consume();
        EvtBusFactory.getDefaultEvtBus().publish(genEditingViewModel.getPropertyValue(WINDOW_TOPIC), new PropertyPanelEvent(actionEvent.getSource(), SHOW_EDIT_SEMANTIC_FIELDS));
        EvtBusFactory.getDefaultEvtBus().publish(genEditingViewModel.getPropertyValue(WINDOW_TOPIC), new PropertyPanelEvent(actionEvent.getSource(), SHOW_ADD_REFERENCE_SEMANTIC_FIELD));
        EvtBusFactory.getDefaultEvtBus().publish(genEditingViewModel.getPropertyValue(WINDOW_TOPIC), new PropertyPanelEvent(actionEvent.getSource(), OPEN_PANEL));
    }

    /**
     * User is clicking on the Toggle switch to open or close Properties bump out.
     *
     * @param event Button click event.
     */
    @FXML
    private void openPropertiesPanel(ActionEvent event) {
        ToggleButton propertyToggle = (ToggleButton) event.getSource();
        EvtType<PropertyPanelEvent> eventEvtType = propertyToggle.isSelected() ? OPEN_PANEL : CLOSE_PANEL;
        EvtBusFactory.getDefaultEvtBus().publish(genEditingViewModel.getPropertyValue(WINDOW_TOPIC), new PropertyPanelEvent(propertyToggle, eventEvtType));
    }

    @FXML
    private void save(ActionEvent actionEvent) {
        // TODO create a commit transaction of current Semantic (Add or edit will add a new Semantic Version)
    }

}
