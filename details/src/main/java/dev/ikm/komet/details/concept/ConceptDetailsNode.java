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
package dev.ikm.komet.details.concept;

import dev.ikm.komet.framework.*;
import dev.ikm.komet.framework.controls.EntityLabelWithDragAndDrop;
import dev.ikm.komet.framework.graphics.Icon;
import dev.ikm.komet.framework.observable.*;
import dev.ikm.komet.framework.propsheet.KometPropertyEditorFactory;
import dev.ikm.komet.framework.propsheet.KometPropertySheet;
import dev.ikm.komet.framework.view.SimpleEqualityBasedListProperty;
import dev.ikm.komet.framework.view.ViewProperties;
import dev.ikm.komet.preferences.KometPreferences;
import dev.ikm.tinkar.common.service.PrimitiveData;
import dev.ikm.tinkar.common.service.TinkExecutor;
import dev.ikm.tinkar.common.util.text.NaturalOrder;
import dev.ikm.tinkar.entity.*;
import dev.ikm.tinkar.terms.*;
import javafx.animation.Animation;
import javafx.animation.FadeTransition;
import javafx.animation.ParallelTransition;
import javafx.application.Platform;
import javafx.beans.property.SimpleIntegerProperty;
import javafx.beans.property.SimpleObjectProperty;
import javafx.beans.value.ObservableValue;
import javafx.collections.FXCollections;
import javafx.collections.ListChangeListener;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.event.Event;
import javafx.event.EventHandler;
import javafx.geometry.HPos;
import javafx.geometry.Insets;
import javafx.geometry.Point2D;
import javafx.geometry.VPos;
import javafx.scene.Node;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.ScrollPane;
import javafx.scene.control.ToggleButton;
import javafx.scene.layout.*;
import javafx.scene.paint.Color;
import javafx.util.Duration;
import org.controlsfx.control.PopOver;
import org.controlsfx.control.PropertySheet;
import org.eclipse.collections.api.list.ImmutableList;
import org.eclipse.collections.api.list.primitive.IntList;
import org.eclipse.collections.api.map.primitive.MutableIntIntMap;
import org.eclipse.collections.api.set.primitive.IntSet;
import org.eclipse.collections.impl.factory.primitive.IntIntMaps;
import org.eclipse.collections.impl.factory.primitive.IntLists;
import org.eclipse.collections.impl.factory.primitive.IntSets;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.*;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicInteger;

import static dev.ikm.komet.framework.KometNodeFactory.THE_CURRENT_OPERATION_IS_NOT_SUPPORTED;

public class ConceptDetailsNode extends ExplorationNodeAbstract {
    private static final Logger LOG = LoggerFactory.getLogger(ConceptDetailsNode.class);
    protected static final String STYLE_ID = StyleClasses.CONCEPT_DETAIL_PANE.toString();
    protected static final String TITLE = "Concept Details Node";
    private static final int TRANSITION_OFF_TIME = 250;
    private static final int TRANSITION_ON_TIME = 300;
    private static final ConceptFacade[] defaultDetailOrder = new ConceptFacade[]{
            TinkarTerm.CONCEPT_FOCUS, TinkarTerm.DESCRIPTION_FOCUS,
            TinkarTerm.AXIOM_FOCUS
    };
    private static final ConceptFacade[] defaultDescriptionTypeOrder = new ConceptFacade[]{
            TinkarTerm.FULLY_QUALIFIED_NAME_DESCRIPTION_TYPE, TinkarTerm.REGULAR_NAME_DESCRIPTION_TYPE, TinkarTerm.DEFINITION_DESCRIPTION_TYPE,
            ObservableFields.WILDCARD_FOR_ORDER
    };
    private static final PatternFacade[] defaultAxiomSourceOrder = new PatternFacade[]{
            TinkarTerm.EL_PLUS_PLUS_INFERRED_AXIOMS_PATTERN, TinkarTerm.EL_PLUS_PLUS_STATED_AXIOMS_PATTERN
    };
    private static final ConceptFacade[] defaultSemanticOrderForConcept = new ConceptFacade[]{
            TinkarTerm.SCTID, TinkarTerm.LOINC_ID_ASSEMBLAGE,
            TinkarTerm.RXNORM_CUI,
            ObservableFields.WILDCARD_FOR_ORDER
    };
    private static final ConceptFacade[] defaultSemanticOrderForDescription = new ConceptFacade[]{
            TinkarTerm.US_ENGLISH_DIALECT, TinkarTerm.GB_ENGLISH_DIALECT, TinkarTerm.SCTID, ObservableFields.WILDCARD_FOR_ORDER
    };
    private static final ConceptFacade[] defaultSemanticOrderForAxiom = new ConceptFacade[]{
            ObservableFields.WILDCARD_FOR_ORDER
    };
    final SimpleObjectProperty<EntityFacade> entityFocusProperty = new SimpleObjectProperty<>();
    //~--- fieldValues --------------------------------------------------------------
    private final SimpleIntegerProperty selectionIndexProperty = new SimpleIntegerProperty(0);
    private final HashMap<String, AtomicBoolean> disclosureStateMap = new HashMap<>();
    private final BorderPane detailsPane = new BorderPane();
    private final VBox componentPanelBox = new VBox(8);
    private final GridPane versionBranchGrid = new GridPane();
    private final GridPane toolGrid = new GridPane();
    private final ExpandControl expandControl = new ExpandControl();
    private final Label expandControlLabel = new Label("Expand All", expandControl);
    private final Button panelSettings = new Button(null, Icon.PANEL_PREFERENCE_SLIDERS.makeIcon());
    private final Button conceptFocusSettings = new Button(null, Icon.PANEL_PREFERENCE_SLIDERS.makeIcon());
    private final Button descriptionFocusSettings = new Button(null, Icon.PANEL_PREFERENCE_SLIDERS.makeIcon());
    private final Button axiomFocusSettings = new Button(null, Icon.PANEL_PREFERENCE_SLIDERS.makeIcon());
    private final MutableIntIntMap stampOrderHashMap = IntIntMaps.mutable.empty();
    private final Button addDescriptionButton = new Button("+ Add");
    private final ToggleButton versionGraphToggle = new ToggleButton("", Icon.SOURCE_BRANCH_1.makeIcon());
    private final ArrayList<Integer> sortedStampNids = new ArrayList<>();
    private final List<ComponentPaneModel> componentPaneModels = new ArrayList<>();
    private final ScrollPane scrollPane;
    private final ObservableList<ObservableCompoundVersion> newDescriptions = FXCollections.observableArrayList();
    // Preference items
    private final SimpleEqualityBasedListProperty<ConceptFacade> detailOrderList = new SimpleEqualityBasedListProperty<>(this,
            ObservableFields.DETAIL_ORDER_FOR_DETAILS_PANE.toExternalString(),
            FXCollections.observableArrayList());
    private final SimpleEqualityBasedListProperty<ConceptFacade> descriptionTypeList = new SimpleEqualityBasedListProperty<>(this,
            ObservableFields.DESCRIPTION_TYPE_ORDER_FOR_DETAILS_PANE.toExternalString(),
            FXCollections.observableArrayList());
    private final SimpleEqualityBasedListProperty<PatternFacade> axiomSourceList = new SimpleEqualityBasedListProperty<>(this,
            ObservableFields.AXIOM_ORDER_FOR_DETAILS_PANE.toExternalString(),
            FXCollections.observableArrayList());
    private final SimpleEqualityBasedListProperty<ConceptFacade> semanticOrderForConceptDetails = new SimpleEqualityBasedListProperty<>(this,
            ObservableFields.SEMANTIC_ORDER_FOR_CONCEPT_DETAILS.toExternalString(),
            FXCollections.observableArrayList());
    private final SimpleEqualityBasedListProperty<ConceptFacade> semanticOrderForDescriptionDetails = new SimpleEqualityBasedListProperty<>(this,
            ObservableFields.SEMANTIC_ORDER_FOR_DESCRIPTION_DETAILS.toExternalString(),
            FXCollections.observableArrayList());
    private final SimpleEqualityBasedListProperty<ConceptFacade> semanticOrderForAxiomDetails = new SimpleEqualityBasedListProperty<>(this,
            ObservableFields.SEMANTIC_ORDER_FOR_AXIOM_DETAILS.toExternalString(),
            FXCollections.observableArrayList());
    private final PropertySheetEntityListWrapper detailsSettingsWrapper;
    private final PropertySheetEntityListWrapper conceptSettingsWrapper;
    private final PropertySheetEntityListWrapper descriptionAttachmentsOrderWrapper;
    private final PropertySheetEntityListWrapper descriptionSettingsWrapper;
    private final PropertySheetEntityListWrapper axiomSettingsWrapper;
    private final PropertySheetEntityListWrapper semanticOrderForAxiomDetailsWrapper;

    {
        titleProperty.setValue("empty");
        menuIconProperty.setValue(Icon.CONCEPT_DETAILS.makeIcon());
    }

    //~--- initializers --------------------------------------------------------
    {
        expandControlLabel.setGraphicTextGap(0);
    }

    //~--- constructors --------------------------------------------------------
    public ConceptDetailsNode(ViewProperties viewProperties, KometPreferences preferences) {
        super(viewProperties, preferences);

        this.detailsPane.getStyleClass()
                .add(StyleClasses.CONCEPT_DETAIL_PANE.toString());
        this.scrollPane = new ScrollPane(componentPanelBox);
        this.scrollPane.setFitToWidth(true);
        this.scrollPane.setHbarPolicy(ScrollPane.ScrollBarPolicy.NEVER);
        this.scrollPane.setVbarPolicy(ScrollPane.ScrollBarPolicy.AS_NEEDED);
        this.detailsPane.setCenter(this.scrollPane);
        this.versionBranchGrid.add(versionGraphToggle, 0, 0);
        this.versionGraphToggle.getStyleClass()
                .setAll(StyleClasses.VERSION_GRAPH_TOGGLE.toString());
        this.versionGraphToggle.selectedProperty()
                .addListener(this::toggleVersionGraph);
        this.detailsPane.setLeft(versionBranchGrid);
        this.componentPanelBox.getStyleClass()
                .add(StyleClasses.COMPONENT_DETAIL_BACKGROUND.toString());
        this.componentPanelBox.setFillWidth(true);
        setupToolGrid();

        this.expandControl.expandActionProperty()
                .addListener(this::expandAllAction);

        getActivityStream().lastDispatchOfIndex(selectionIndexProperty.get()).ifPresentOrElse(entityFacade -> {
            titleProperty.set(this.viewCalculator().getPreferredDescriptionTextWithFallbackOrNid(entityFacade.nid()));
        }, () -> {
            titleProperty.set(EntityLabelWithDragAndDrop.EMPTY_TEXT);
        });


        this.detailOrderList.setAll(preferences.getConceptList(ConceptDetailNodeKeys.DETAIL_ORDER, defaultDetailOrder));
        this.descriptionTypeList.setAll(preferences.getConceptList(ConceptDetailNodeKeys.DESCRIPTION_TYPE_ORDER, defaultDescriptionTypeOrder));
        this.axiomSourceList.setAll(preferences.getPatternList(ConceptDetailNodeKeys.AXIOM_ORDER, defaultAxiomSourceOrder));
        this.semanticOrderForConceptDetails.setAll(preferences.getConceptList(ConceptDetailNodeKeys.CONCEPT_SEMANTICS_ORDER, defaultSemanticOrderForConcept));
        this.semanticOrderForDescriptionDetails.setAll(preferences.getConceptList(ConceptDetailNodeKeys.DESCRIPTION_SEMANTIC_ORDER, defaultSemanticOrderForDescription));
        this.semanticOrderForAxiomDetails.setAll(preferences.getConceptList(ConceptDetailNodeKeys.AXIOM_SEMANTIC_ORDER, defaultSemanticOrderForAxiom));


        this.revertPreferences();
        this.savePreferences();

        this.viewProperties.nodeView().addListener((observable, oldValue, newValue) -> {
            Platform.runLater(() -> resetConceptFromFocus());
        });

        detailsSettingsWrapper = new PropertySheetEntityListWrapper(viewCalculator(), detailOrderList);
        detailsSettingsWrapper.setConstraints(viewCalculator().referencedConceptsIfSemanticActiveForPattern(TinkarTerm.DETAIL_ORDER_OPTIONS_PATTERN));
        detailsSettingsWrapper.setAllowDuplicates(false);
        panelSettings.setOnAction(makeChangeSettingsAction(detailsSettingsWrapper));

        conceptSettingsWrapper = new PropertySheetEntityListWrapper(viewCalculator(), semanticOrderForConceptDetails);
        conceptSettingsWrapper.setConstraints(viewCalculator().referencedConceptsIfSemanticActiveForPattern((TinkarTerm.CONCEPT_ATTACHMENT_ORDER_OPTIONS_PATTERN)));
        conceptSettingsWrapper.setAllowDuplicates(false);
        conceptFocusSettings.setOnAction(makeChangeSettingsAction(conceptSettingsWrapper));

        descriptionAttachmentsOrderWrapper = new PropertySheetEntityListWrapper(viewCalculator(), semanticOrderForDescriptionDetails);
        descriptionAttachmentsOrderWrapper.setConstraints(viewCalculator().referencedConceptsIfSemanticActiveForPattern(TinkarTerm.DESCRIPTION_ATTACHMENT_ORDER_OPTIONS_PATTERN));
        descriptionAttachmentsOrderWrapper.setAllowDuplicates(false);

        descriptionSettingsWrapper = new PropertySheetEntityListWrapper(viewCalculator(), descriptionTypeList);
        descriptionSettingsWrapper.setConstraints(viewCalculator().referencedConceptsIfSemanticActiveForPattern(TinkarTerm.DESCRIPTION_TYPE_ORDER_OPTIONS_PATTERN));
        descriptionSettingsWrapper.setAllowDuplicates(false);
        descriptionFocusSettings.setOnAction(makeChangeSettingsAction(descriptionSettingsWrapper, descriptionAttachmentsOrderWrapper));

        axiomSettingsWrapper = new PropertySheetEntityListWrapper(viewCalculator(), axiomSourceList);
        axiomSettingsWrapper.setConstraints(viewCalculator().referencedConceptsIfSemanticActiveForPattern(TinkarTerm.AXIOM_ORDER_OPTIONS_PATTERN));
        axiomSettingsWrapper.setAllowDuplicates(false);

        semanticOrderForAxiomDetailsWrapper = new PropertySheetEntityListWrapper(viewCalculator(), semanticOrderForAxiomDetails);
        semanticOrderForAxiomDetailsWrapper.setConstraints(viewCalculator().referencedConceptsIfSemanticActiveForPattern(TinkarTerm.AXIOM_ATTACHMENT_ORDER_OPTIONS_PATTERN));
        semanticOrderForAxiomDetailsWrapper.setAllowDuplicates(false);
        axiomFocusSettings.setOnAction(makeChangeSettingsAction(axiomSettingsWrapper, semanticOrderForAxiomDetailsWrapper));

        detailOrderList.addListener(this::handleSettingsChange);
        descriptionTypeList.addListener(this::handleSettingsChange);
        axiomSourceList.addListener(this::handleSettingsChange);
        semanticOrderForConceptDetails.addListener(this::handleSettingsChange);
        descriptionTypeList.addListener(this::handleSettingsChange);

        this.axiomSourceList.setAll(preferences.getPatternList(ConceptDetailNodeKeys.AXIOM_ORDER, defaultAxiomSourceOrder));
        this.semanticOrderForConceptDetails.setAll(preferences.getConceptList(ConceptDetailNodeKeys.CONCEPT_SEMANTICS_ORDER, defaultSemanticOrderForConcept));
        this.semanticOrderForDescriptionDetails.setAll(preferences.getConceptList(ConceptDetailNodeKeys.DESCRIPTION_SEMANTIC_ORDER, defaultSemanticOrderForDescription));
        this.semanticOrderForAxiomDetails.setAll(preferences.getConceptList(ConceptDetailNodeKeys.AXIOM_SEMANTIC_ORDER, defaultSemanticOrderForAxiom));

    }

    public static List<ObservableSemanticSnapshot> filterAndSortByPattern(List<ObservableSemanticSnapshot> semantics,
                                                                          SimpleEqualityBasedListProperty<PatternFacade> entityPriorityList) {
        // SimpleEqualityBasedListProperty<ConceptFacade>
        // Delete any versions not active in configuration
        IntList entityOrderList = IntLists.immutable.ofAll(entityPriorityList.stream().mapToInt(value -> value.nid()));
        List<ObservableSemanticSnapshot> filteredAndSortedSemantics = new ArrayList<>(semantics.size());
        if (!entityOrderList.contains(TinkarTerm.ANY_COMPONENT.nid())) {
            // need to filter
            IntSet allowedPatternSet = IntSets.immutable.ofAll(entityOrderList);
            semantics.stream().forEach(semanticSnapshot -> {
                if (allowedPatternSet.contains(semanticSnapshot.patternNid())) {
                    filteredAndSortedSemantics.add(semanticSnapshot);
                }
            });
        } else {
            filteredAndSortedSemantics.addAll(semantics);
        }
        // now need to sort...
        filteredAndSortedSemantics.sort(compareWithList(entityOrderList));
        return filteredAndSortedSemantics;
    }

    public static Comparator<ObservableEntitySnapshot> compareWithList(IntList patternOrderList) {
        return (o1, o2) -> {
            if (o1 instanceof SemanticEntityVersion os1) {
                if (o2 instanceof SemanticEntityVersion os2) {
                    int o1index = patternOrderList.indexOf(os1.patternNid());
                    int o2index = patternOrderList.indexOf(os2.patternNid());
                    if (o1index == o2index) {
                        // same assemblage
                        return o1.toString().compareTo(o2.toString());
                    }
                    if (o1index == -1) {
                        return 1;
                    }
                    if (o2index == -1) {
                        return -1;
                    }
                    return (o1index < o2index) ? -1 : 1;
                }
                return -1;
            }
            return 1;
        };
    }

    public static List<ObservableSemanticSnapshot> filterAndSortDescriptions(List<ObservableSemanticSnapshot> entitySnapshotList,
                                                                             SimpleEqualityBasedListProperty<ConceptFacade> descriptionTypeOrderList) {

        IntList typeOrderNidList = IntLists.immutable.ofAll(descriptionTypeOrderList.stream().mapToInt(value -> value.nid()));
        final boolean filterByType = !typeOrderNidList.contains(TinkarTerm.ANY_COMPONENT.nid());
        return entitySnapshotList.stream()
                // Only description semantic snapshots
                .filter(semanticSnapshot -> semanticSnapshot.patternNid() == TinkarTerm.DESCRIPTION_PATTERN.nid())
                // Only descriptions snapshots whose latest meet description type criterion.
                .filter(semanticSnapshot -> semanticSnapshot.findFirstField(field -> field.meaningNid() == TinkarTerm.DESCRIPTION_TYPE.nid())
                        .ifAbsentOrFunction(() -> false,
                                typeField -> {
                                    if (filterByType) {
                                        return switch (typeField.value()) {
                                            case EntityFacade entity -> typeOrderNidList.contains(entity.nid());
                                            default -> false;
                                        };
                                    } else {
                                        return true;
                                    }
                                }))
                // Sort descriptions by type then text.
                .sorted((ObservableSemanticSnapshot d1Snapshot,
                         ObservableSemanticSnapshot d2Snapshot) -> {
                    int o1index = typeOrderNidList.indexOf(d1Snapshot.findFirstFieldNidValueOrMaxValue(field -> field.meaningNid() == TinkarTerm.DESCRIPTION_TYPE.nid()));
                    int o2index = typeOrderNidList.indexOf(d2Snapshot.findFirstFieldNidValueOrMaxValue(field -> field.meaningNid() == TinkarTerm.DESCRIPTION_TYPE.nid()));
                    if (o1index == o2index) {
                        // alphabetical by text if types are the same
                        return NaturalOrder.compareStrings(
                                d1Snapshot.findFirstFieldStringValueOrEmpty(field -> field.meaningNid() == TinkarTerm.TEXT_FOR_DESCRIPTION.nid()),
                                d2Snapshot.findFirstFieldStringValueOrEmpty(field -> field.meaningNid() == TinkarTerm.TEXT_FOR_DESCRIPTION.nid()));
                    }
                    if (o1index == -1) {
                        return 1;
                    }
                    if (o2index == -1) {
                        return -1;
                    }
                    return (o1index < o2index) ? -1 : 1;
                }).toList();
    }

    public static void addDefaultNodePreferences(KometPreferences nodePreferences) {

        throw new UnsupportedOperationException(THE_CURRENT_OPERATION_IS_NOT_SUPPORTED);
    }

    @Override
    public String getDefaultTitle() {
        return "Concept Details";
    }

    @Override
    public void handleActivity(ImmutableList<EntityFacade> entities) {
        throw new UnsupportedOperationException();
    }

    @Override
    public void revertAdditionalPreferences() {
        this.selectionIndexProperty.setValue(this.nodePreferences.getInt(Keys.ACTIVITY_SELECTION_INDEX, this.selectionIndexProperty.getValue()));
        revertConceptList(ConceptDetailNodeKeys.DETAIL_ORDER, this.detailOrderList);
        revertConceptList(ConceptDetailNodeKeys.DESCRIPTION_TYPE_ORDER, this.descriptionTypeList);
        revertPatternList(ConceptDetailNodeKeys.AXIOM_ORDER, this.axiomSourceList);
        revertConceptList(ConceptDetailNodeKeys.CONCEPT_SEMANTICS_ORDER, this.semanticOrderForConceptDetails);
        revertConceptList(ConceptDetailNodeKeys.DESCRIPTION_SEMANTIC_ORDER, this.semanticOrderForDescriptionDetails);
        revertConceptList(ConceptDetailNodeKeys.AXIOM_SEMANTIC_ORDER, this.semanticOrderForAxiomDetails);
        this.nodePreferences.getConceptProxy(ConceptDetailNodeKeys.FOCUS_CONCEPT).ifPresent(
                conceptFacade -> this.entityFocusProperty.setValue(conceptFacade));
    }

    @Override
    public Node getMenuIconGraphic() {
        return Icon.CONCEPT_DETAILS.makeIcon();
    }

    @Override
    public String getStyleId() {
        return StyleClasses.CONCEPT_DETAIL_PANE.toString();
    }

    @Override
    protected void saveAdditionalPreferences() {
        Optional<EntityFacade> optionalFocus = Optional.ofNullable(this.entityFocusProperty.get());
        if (optionalFocus.isPresent()) {
            this.nodePreferences.putInt(Keys.ACTIVITY_SELECTION_INDEX, this.selectionIndexProperty.getValue());
        }
        // TODO add activity feed to preferences...
        //this.nodePreferences.put(Keys.ACTIVITY_FEED_NAME, this.getActivityFeed().getFullyQualifiedActivityFeedName());

        this.nodePreferences.putConceptList(ConceptDetailNodeKeys.DETAIL_ORDER, this.detailOrderList);
        this.nodePreferences.putConceptList(ConceptDetailNodeKeys.DESCRIPTION_TYPE_ORDER, this.descriptionTypeList);
        this.nodePreferences.putComponentList(ConceptDetailNodeKeys.AXIOM_ORDER, this.axiomSourceList);
        this.nodePreferences.putConceptList(ConceptDetailNodeKeys.CONCEPT_SEMANTICS_ORDER, this.semanticOrderForConceptDetails);
        this.nodePreferences.putConceptList(ConceptDetailNodeKeys.DESCRIPTION_SEMANTIC_ORDER, this.semanticOrderForDescriptionDetails);
        this.nodePreferences.putConceptList(ConceptDetailNodeKeys.AXIOM_SEMANTIC_ORDER, this.semanticOrderForAxiomDetails);
        optionalFocus.ifPresentOrElse(identifiedObject -> {
            this.nodePreferences.putConceptProxy(ConceptDetailNodeKeys.FOCUS_CONCEPT, Entity.provider().getEntityFast(identifiedObject.nid()));
        }, () -> this.nodePreferences.remove(ConceptDetailNodeKeys.FOCUS_CONCEPT));

    }

    private void revertConceptList(ConceptDetailNodeKeys detailOrder, SimpleEqualityBasedListProperty<ConceptFacade> detailOrderList) {
        if (this.nodePreferences.hasKey(detailOrder)) {
            detailOrderList.setAll(this.nodePreferences.getConceptList(detailOrder));
        }
    }

    private void revertPatternList(ConceptDetailNodeKeys detailOrder, SimpleEqualityBasedListProperty<PatternFacade> detailOrderList) {
        if (this.nodePreferences.hasKey(detailOrder)) {
            detailOrderList.setAll(this.nodePreferences.getPatternList(detailOrder));
        }
    }

    private void handleSettingsChange(ListChangeListener.Change<? extends EntityFacade> c) {
        Platform.runLater(() -> {
            resetConceptFromFocus();
            //Platform.runLater(() -> popOver.show(popOver.getOwnerNode(), popOverArrowLocation.getX(), popOverArrowLocation.getY(), Duration.ZERO));
            TinkExecutor.threadPool().execute(() -> savePreferences());
        });
    }

    private EventHandler<ActionEvent> makeChangeSettingsAction(PropertySheetEntityListWrapper... listWrapper) {
        EventHandler<ActionEvent> changeSettingsHandler = new EventHandler<ActionEvent>() {
            @Override
            public void handle(ActionEvent event) {
                KometPropertySheet propertySheet = new KometPropertySheet(viewProperties);
                propertySheet.setMode(PropertySheet.Mode.NAME);
                propertySheet.setSearchBoxVisible(false);
                propertySheet.setModeSwitcherVisible(false);
                propertySheet.setPropertyEditorFactory(new KometPropertyEditorFactory(viewProperties));
                propertySheet.getItems().addAll(listWrapper);
                PopOver popOver = new PopOver();
                popOver.setContentNode(propertySheet);
                popOver.setCloseButtonEnabled(true);
                popOver.setHeaderAlwaysVisible(false);
                popOver.setTitle("");
                Point2D popOverArrowLocation = ScreenInfo.getMouseLocation();
                popOver.show(ConceptDetailsNode.this.getNode(), popOverArrowLocation.getX(), popOverArrowLocation.getY());

                event.consume();
            }
        };
        return changeSettingsHandler;
    }

    private void revertEntityList(ConceptDetailNodeKeys detailOrder, SimpleEqualityBasedListProperty<EntityFacade> detailOrderList) {
        if (this.nodePreferences.hasKey(detailOrder)) {
            detailOrderList.setAll(this.nodePreferences.getEntityList(detailOrder));
        }
    }

    //~--- methods -------------------------------------------------------------

    @Override
    public Node getNode() {
        return this.detailsPane;
    }

    @Override
    public void close() {
        // closing just the tab, not saving the window on quit. Nothing to do.
        // TODO Release any resources/listeners similar associated with node. ;
    }

    @Override
    public boolean canClose() {
        // TODO Maybe check for uncommitted changes in window. ;
        return true;
    }

    @Override
    public Class factoryClass() {
        return ConceptDetaisNodeFactory.class;
    }

    private void addCategorizedVersions(ObservableEntitySnapshot categorizedVersions,
                                        List<ConceptFacade> semanticOrderForChronology, ParallelTransition parallelTransition) {
        categorizedVersions.getLatestVersion().ifPresent(observableVersionWithCategories -> {
            parallelTransition.getChildren()
                    .add(addComponent(categorizedVersions, semanticOrderForChronology));
        });
    }

    private Animation addComponent(ConceptBuilderComponentPanel panel) {
        return this.addComponent(panel, new Insets(1, 5, 1, 5));
    }

    private Animation addComponent(ConceptBuilderComponentPanel panel, Insets insets) {

        panel.setOpacity(0);
        VBox.setMargin(panel, insets);
        VBox.setVgrow(panel, Priority.NEVER);
        componentPanelBox.getChildren()
                .add(panel);

        FadeTransition ft = new FadeTransition(Duration.millis(TRANSITION_ON_TIME), panel);

        ft.setFromValue(0);
        ft.setToValue(1);
        return ft;
    }

    private Animation addComponent(ObservableEntitySnapshot entitySnapshot,
                                   List<ConceptFacade> semanticOrderForChronology) {

        if (entitySnapshot.getLatestVersion().isAbsent()) {
            throw new IllegalStateException(
                    "ObservableEntitySnapshot has no latest version or uncommitted version: \n" + entitySnapshot);
        }

        ComponentPaneModel componentPaneModel = new ComponentPaneModel(this.viewProperties, entitySnapshot, semanticOrderForChronology,
                stampOrderHashMap, disclosureStateMap);

        componentPaneModels.add(componentPaneModel);
        componentPaneModel.getBadgedPane().setOpacity(0);
        VBox.setMargin(componentPaneModel.getBadgedPane(), new Insets(1, 5, 1, 5));
        VBox.setVgrow(componentPaneModel.getBadgedPane(), Priority.NEVER);
        componentPanelBox.getChildren()
                .add(componentPaneModel.getBadgedPane());

        FadeTransition ft = new FadeTransition(Duration.millis(TRANSITION_ON_TIME), componentPaneModel.getBadgedPane());

        ft.setFromValue(0);
        ft.setToValue(1);
        return ft;
    }

    private Animation addNode(Node headerNode) {
        headerNode.setOpacity(0);
        VBox.setMargin(headerNode, new Insets(1, 5, 1, 5));
        componentPanelBox.getChildren()
                .add(headerNode);

        FadeTransition ft = new FadeTransition(Duration.millis(TRANSITION_ON_TIME), headerNode);

        ft.setFromValue(0);
        ft.setToValue(1);
        return ft;
    }

    private void animateLayout() {
        componentPanelBox.getChildren().clear();
        populateVersionBranchGrid();
        componentPanelBox.getChildren().add(toolGrid);
        Optional.ofNullable(entityFocusProperty.getValue()).ifPresent(entityFacade -> {
            ConceptEntity<ConceptEntityVersion> conceptEntity = Entity.provider().getEntityFast(entityFacade.nid());
            titleProperty.set(this.viewProperties.calculator().getPreferredDescriptionTextWithFallbackOrNid(entityFacade));
            ObservableConcept observableConcept = ObservableEntity.get(conceptEntity);
            animateFocus(observableConcept);
        });
    }

    private void animateFocus(ObservableConcept observableConceptChronology) {

        final ParallelTransition parallelTransition = new ParallelTransition();
        List<ObservableSemanticSnapshot> descriptionSemantics = new ArrayList<>();
        List<ObservableSemanticSnapshot> axiomSemantics = new ArrayList<>();
        List<ObservableSemanticSnapshot> otherSemantics = new ArrayList<>();

        ObservableEntitySnapshot conceptSnapshot = observableConceptChronology.getSnapshot(
                this.viewProperties.calculator());

        observableConceptChronology.getObservableSemanticList().forEach(observableSemanticChronology -> {
            ObservableSemanticSnapshot semanticSnapshot
                    = observableSemanticChronology.getSnapshot(
                    this.viewProperties.calculator());
            semanticSnapshot.getLatestVersion().ifPresent(semanticVersion -> {
                if (semanticVersion.patternNid() == TinkarTerm.DESCRIPTION_PATTERN.nid()) {
                    descriptionSemantics.add(semanticSnapshot);
                } else if (semanticVersion.patternNid() == TinkarTerm.EL_PLUS_PLUS_INFERRED_AXIOMS_PATTERN.nid() ||
                        semanticVersion.patternNid() == TinkarTerm.EL_PLUS_PLUS_STATED_AXIOMS_PATTERN.nid()) {
                    axiomSemantics.add(semanticSnapshot);
                } else {
                    otherSemantics.add(semanticSnapshot);
                }
            });
        });

        for (ConceptFacade focus : this.detailOrderList) {
            if (focus.nid() == TinkarTerm.CONCEPT_FOCUS.nid()) {
                AnchorPane conceptHeader = FxUtils.setupHeaderPanel("CONCEPT", null, conceptFocusSettings);
                conceptHeader.pseudoClassStateChanged(PseudoClasses.CONCEPT_PSEUDO_CLASS, true);
                parallelTransition.getChildren()
                        .add(addNode(conceptHeader));
                toolTipTextProperty.set(
                        "concept details for: " + this.viewProperties.calculator().getFullyQualifiedDescriptionTextWithFallbackOrNid(observableConceptChronology));

                addCategorizedVersions(conceptSnapshot,
                        semanticOrderForConceptDetails, parallelTransition);
            } else if (focus.nid() == TinkarTerm.DESCRIPTION_FOCUS.nid()) {
                AnchorPane descriptionHeader = FxUtils.setupHeaderPanel("DESCRIPTIONS", addDescriptionButton, descriptionFocusSettings);

                addDescriptionButton.getStyleClass()
                        .setAll(StyleClasses.ADD_DESCRIPTION_BUTTON.toString());

                addDescriptionButton.setOnAction(this::newDescription);
                descriptionHeader.pseudoClassStateChanged(PseudoClasses.DESCRIPTION_PSEUDO_CLASS, true);
                parallelTransition.getChildren()
                        .add(addNode(descriptionHeader));

                Iterator<ObservableCompoundVersion> iter = newDescriptions.iterator();
                while (iter.hasNext()) {
                    // Compound version of a description + a dialect acceptability
                    ObservableCompoundVersion descDialect = iter.next();
                    if (descDialect.uncommitted()) {
                        ConceptBuilderComponentPanel descPanel = new ConceptBuilderComponentPanel(this.viewProperties,
                                descDialect, true, null);
                        parallelTransition.getChildren().add(addComponent(descPanel));
                        descPanel.setCommitHandler((event) -> {
                            newDescriptions.remove(descDialect);
                            clearComponents();
                            throw new UnsupportedOperationException();
                        });
                        descPanel.setCancelHandler((event) -> {
                            newDescriptions.remove(descDialect);
                            clearComponents();
                            throw new UnsupportedOperationException();
                        });

                    } else {
                        iter.remove();
                    }
                }
                // add description versions here...
                filterAndSortDescriptions(descriptionSemantics, descriptionTypeList)
                        .forEach(categorizedVersions -> addCategorizedVersions(categorizedVersions,
                                semanticOrderForDescriptionDetails, parallelTransition));


            } else if (focus.nid() == TinkarTerm.AXIOM_FOCUS.nid()) {
                AnchorPane axiomHeader = FxUtils.setupHeaderPanel("AXIOMS", null, axiomFocusSettings);
                axiomHeader.pseudoClassStateChanged(PseudoClasses.LOGICAL_DEFINITION_PSEUDO_CLASS, true);
                parallelTransition.getChildren()
                        .add(addNode(axiomHeader));
                // add axiom versions here...
                filterAndSortByPattern(axiomSemantics, axiomSourceList)
                        .forEach(categorizedVersions ->
                                addCategorizedVersions(categorizedVersions,
                                        semanticOrderForAxiomDetails, parallelTransition));
            } // else if (lineage view) {
             /* TODO finish lineage view
            AnchorPane lineageHeader = FxUtils.setupHeaderPanel("LINEAGE", null);
            parallelTransition.getChildren()
                    .add(addNode(lineageHeader));

            parallelTransition.getChildren()
                    .add(addNode(LineageTree.makeLineageTree(newValue, this.manifoldProperty.get())));
            */
            // }
        }
        parallelTransition.play();
    }

    private void newDescription(Event event) {
        throw new UnsupportedOperationException();
//        Optional<IdentifiedObject> optionalFocus = this.getFocusedObject();
//        if (optionalFocus.isPresent()) {
//            ObservableCompoundVersion newDescriptionDialect
//                    = new ObservableCompoundVersion(optionalFocus.get().getPrimordialUuid(), TinkarTerm.ENGLISH_LANGUAGE.nid());
//            newDescriptions.add(newDescriptionDialect);
//            // Set with pattern nid, field meaning nid.
//            newDescriptionDialect.setField(TinkarTerm.DESCRIPTION_PATTERN, TinkarTerm.DESCRIPTION_TYPE, TinkarTerm.REGULAR_NAME_DESCRIPTION_TYPE);
//            newDescriptionDialect.getDescription().setDescriptionTypeConceptNid(TinkarTerm.REGULAR_NAME_DESCRIPTION_TYPE.nid());
//            newDescriptionDialect.getDescription().setStatus(State.ACTIVE, null);
//            newDescriptionDialect.getDialect().setStatus(State.ACTIVE, null);
//            clearComponents();
//        }
    }

    private void clearComponents() {
        final ParallelTransition parallelTransition = new ParallelTransition();

        componentPanelBox.getChildren()
                .forEach(
                        (child) -> {
                            if (toolGrid != child) {
                                FadeTransition ft = new FadeTransition(Duration.millis(TRANSITION_OFF_TIME), child);

                                ft.setFromValue(1.0);
                                ft.setToValue(0.0);
                                parallelTransition.getChildren()
                                        .add(ft);
                            }
                        });
        versionBranchGrid.getChildren()
                .forEach(
                        (child) -> {
                            if (versionGraphToggle != child) {
                                FadeTransition ft = new FadeTransition(Duration.millis(TRANSITION_OFF_TIME), child);

                                ft.setFromValue(1.0);
                                ft.setToValue(0.0);
                                parallelTransition.getChildren()
                                        .add(ft);
                            }
                        });
        //parallelTransition.setOnFinished(this::clearAnimationComplete);
        parallelTransition.play();
    }

    private void expandAllAction(ObservableValue<? extends ExpandAction> observable,
                                 ExpandAction oldValue,
                                 ExpandAction newValue) {
        componentPaneModels.forEach((componentPaneModel) -> componentPaneModel.doExpandAllAction(newValue));
    }

    private void populateVersionBranchGrid() {
        versionBranchGrid.getChildren()
                .clear();
        versionBranchGrid.add(versionGraphToggle, 0, 0);

        if (versionGraphToggle.isSelected()) {
            for (int stampOrder = 0; stampOrder < sortedStampNids.size(); stampOrder++) {
                StampControl stampControl = new StampControl();
                int stampNid = sortedStampNids.get(stampOrder);
                StampEntity stampEntity = Entity.getStamp(stampNid);
                stampControl.pseudoClassStateChanged(PseudoClasses.INACTIVE_PSEUDO_CLASS, stampEntity.state() != State.ACTIVE);

                stampControl.setStampedVersion(stampNid, this.viewProperties, stampOrder + 1);
                versionBranchGrid.add(stampControl, 0, stampOrder + 2);
            }
        }
    }

    private void setupToolGrid() {
        GridPane.setConstraints(
                expandControlLabel,
                0,
                0,
                1,
                1,
                HPos.LEFT,
                VPos.CENTER,
                Priority.NEVER,
                Priority.NEVER,
                new Insets(2));
        this.toolGrid.getChildren()
                .add(expandControlLabel);

        Pane spacer = new Pane();

        GridPane.setConstraints(
                spacer,
                1,
                0,
                1,
                1,
                HPos.CENTER,
                VPos.CENTER,
                Priority.ALWAYS,
                Priority.NEVER,
                new Insets(2));
        this.toolGrid.getChildren()
                .add(spacer);


        GridPane.setConstraints(
                panelSettings,
                2,
                0,
                1,
                1,
                HPos.RIGHT,
                VPos.BOTTOM,
                Priority.NEVER,
                Priority.NEVER,
                new Insets(2));
        this.toolGrid.getChildren()
                .add(panelSettings);

        panelSettings.setBorder(Border.EMPTY);
        panelSettings.setBackground(FxUtils.makeBackground(Color.TRANSPARENT));

        componentPanelBox.getChildren()
                .add(toolGrid);
    }

    private void toggleVersionGraph(ObservableValue<? extends Boolean> observable, Boolean oldValue, Boolean newValue) {
        resetConceptFromFocus();
    }

    private void resetConceptFromFocus() {
        setConcept((ConceptEntity<ConceptEntityVersion>) entityFocusProperty.get());

    }

    private void updateStampControls(Entity entity) {
        if (entity == null) {
            return;
        }
        for (int stampNid : entity.stampNids().toArray()) {
            stampOrderHashMap.put(stampNid, 0);
        }
        PrimitiveData.get().forEachSemanticNidForComponent(
                entity.nid(),
                semanticNid -> updateStampControls(Entity.provider().getEntityFast(semanticNid)));
    }

    public void updateFocusedObject(EntityFacade component) {
        if (component != null) {
            Platform.runLater(() -> {
                Optional<? extends Entity> optionalEntity = Entity.get(component.nid());
                optionalEntity.ifPresent(chronology -> {
                    if (chronology instanceof ConceptEntity conceptEntity) {
                        setConcept(conceptEntity);
                    } else if (chronology instanceof SemanticEntity semanticEntity) {
                        Entity topComponent = semanticEntity.topEnclosingComponent();
                        if (topComponent instanceof ConceptEntity conceptEntity) {
                            setConcept(conceptEntity);
                        } else {
                            throw new IllegalStateException("Top component not concept: " + topComponent);
                        }

                    }
                });
            });
        } else {
            setConcept(null);
        }
    }

    //~--- set methods ---------------------------------------------------------

    private void setConcept(ConceptEntity<ConceptEntityVersion> component) {
        clearComponents();

        this.stampOrderHashMap.clear();
        this.componentPaneModels.clear();
        updateStampControls(component);

        this.sortedStampNids.clear();
        this.stampOrderHashMap.forEachKey(stampNid -> sortedStampNids.add(stampNid));

        this.sortedStampNids.sort(
                (stampNid1, stampNid2) -> Entity.getStamp(stampNid2).instant().compareTo(Entity.getStamp(stampNid1).instant()));

        final AtomicInteger stampOrder = new AtomicInteger();

        this.sortedStampNids.forEach((stampSequence) -> {
            this.stampOrderHashMap.put(stampSequence, stampOrder.incrementAndGet());
        });
        populateVersionBranchGrid();
        animateLayout();
    }

    //~--- get methods ---------------------------------------------------------
    public enum Keys {
        ACTIVITY_FEED_NAME,
        ACTIVITY_SELECTION_INDEX,
        DETAIL_NODE_INSTANCE
    }

    public enum ConceptDetailNodeKeys {
        AXIOM_ORDER,
        DETAIL_ORDER,
        DESCRIPTION_TYPE_ORDER,
        CONCEPT_SEMANTICS_ORDER,
        DESCRIPTION_SEMANTIC_ORDER,
        AXIOM_SEMANTIC_ORDER,
        FOCUS_CONCEPT;
    }


}