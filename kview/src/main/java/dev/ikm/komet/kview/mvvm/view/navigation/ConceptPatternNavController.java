package dev.ikm.komet.kview.mvvm.view.navigation;

import static dev.ikm.komet.kview.mvvm.viewmodel.FormViewModel.VIEW_PROPERTIES;
import static dev.ikm.komet.kview.mvvm.viewmodel.PatternNavViewModel.PATTERN_COLLECTION;
import dev.ikm.komet.framework.Identicon;
import dev.ikm.komet.framework.view.ViewProperties;
import dev.ikm.komet.kview.mvvm.viewmodel.PatternNavViewModel;
import dev.ikm.tinkar.common.service.PrimitiveData;
import dev.ikm.tinkar.common.service.TinkExecutor;
import dev.ikm.tinkar.common.util.text.NaturalOrder;
import dev.ikm.tinkar.common.util.time.DateTimeUtil;
import dev.ikm.tinkar.coordinate.stamp.calculator.Latest;
import dev.ikm.tinkar.entity.Entity;
import dev.ikm.tinkar.entity.EntityService;
import dev.ikm.tinkar.entity.PatternEntityVersion;
import dev.ikm.tinkar.entity.SemanticEntity;
import dev.ikm.tinkar.entity.SemanticEntityVersion;
import dev.ikm.tinkar.entity.SemanticRecord;
import dev.ikm.tinkar.terms.EntityFacade;
import dev.ikm.tinkar.terms.EntityProxy;
import javafx.application.Platform;
import javafx.beans.binding.DoubleBinding;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.geometry.Pos;
import javafx.scene.control.ContentDisplay;
import javafx.scene.control.Label;
import javafx.scene.control.ListCell;
import javafx.scene.control.ListView;
import javafx.scene.control.ScrollPane;
import javafx.scene.control.ToggleButton;
import javafx.scene.control.ToggleGroup;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Pane;
import javafx.scene.layout.Region;
import javafx.scene.layout.VBox;
import org.carlfx.cognitive.loader.FXMLMvvmLoader;
import org.carlfx.cognitive.loader.InjectViewModel;
import org.carlfx.cognitive.loader.JFXNode;
import org.eclipse.collections.api.list.ImmutableList;

import java.text.NumberFormat;
import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.atomic.AtomicInteger;

public class ConceptPatternNavController {

    public static final String PATTERN_NAV_ENTRY_FXML = "pattern-nav-entry.fxml";

    private static final int maxChildrenInPatternViewer = 150;

    @FXML
    private ToggleGroup conPatToggleGroup;

    @FXML
    private ToggleButton conceptsToggleButton;

    @FXML
    private ToggleButton patternsToggleButton;

    @FXML
    private BorderPane navContentPane;

    private Pane patternNavigationPane;

    private VBox patternsVBox;


    private Pane classicConceptNavigator;

    @FXML
    private ScrollPane scrollPane;

    @FXML
    private VBox scrollPaneContent;

    @InjectViewModel
    private PatternNavViewModel patternNavViewModel;

    public ConceptPatternNavController(Pane navigatorNodePanel) {
        classicConceptNavigator = navigatorNodePanel;
    }

    @FXML
    public void initialize() {
        patternNavigationPane = new Pane();
        patternsVBox = new VBox();

        patternNavigationPane.getChildren().add(patternsVBox);
        patternsVBox.getChildren().clear();

        // default to classic concept navigation
        navContentPane.setCenter(classicConceptNavigator);

        loadAllPatterns();

        // ScrollPane content should be at a minimum as high (height wise) as the ScrollPane viewport (ScrollPane's visible area) height
        scrollPaneContent.minHeightProperty().bind(new DoubleBinding() {
            {
                super.bind(scrollPane.viewportBoundsProperty());
            }

            @Override
            protected double computeValue() {
                return scrollPane.getViewportBounds().getHeight();
            }
        });
    }

    static final EntityProxy identifierPatternProxy = EntityProxy.make("Identifier pattern",
            new UUID[] {UUID.fromString("65dd3f06-71ff-5650-8fb3-ce4019e50642")});

    static final EntityProxy inferredDefinitionPatternProxy = EntityProxy.make("Inferred definition pattern",
            new UUID[] {UUID.fromString("9f011812-15c9-5b1b-85f8-bb262bc1b2a2")});

    static final EntityProxy inferredNavigationPatternProxy = EntityProxy.make("Inferred navigation pattern",
            new UUID[] {UUID.fromString("a53cc42d-c07e-5934-96b3-2ede3264474e")});

    static final EntityProxy pathMembershipProxy = EntityProxy.make("Path membership",
            new UUID[] {UUID.fromString("add1db57-72fe-53c8-a528-1614bda20ec6")});

    static final EntityProxy statedDefinitionPatternProxy = EntityProxy.make("Stated definition pattern",
            new UUID[] {UUID.fromString("e813eb92-7d07-5035-8d43-e81249f5b36e")});

    static final EntityProxy statedNavigationPatternProxy = EntityProxy.make("Stated navigation pattern",
            new UUID[] {UUID.fromString("d02957d6-132d-5b3c-adba-505f5778d998")});

    static final EntityProxy ukDialectPatternProxy = EntityProxy.make("UK Dialect Pattern",
            new UUID[] {UUID.fromString("561f817a-130e-5e56-984d-910e9991558c")});

    static final EntityProxy usDialectPatternProxy = EntityProxy.make("US Dialect Pattern",
            new UUID[] {UUID.fromString("08f9112c-c041-56d3-b89b-63258f070074")});

    static final EntityProxy versionControlPathOriginPatternProxy = EntityProxy.make("Version control path origin pattern",
            new UUID[] {UUID.fromString("70f89dd5-2cdb-59bb-bbaa-98527513547c")});


    @FXML
    private void showConcepts() {
        navContentPane.setCenter(classicConceptNavigator);
    }

    @FXML
    private void showPatterns() {
        navContentPane.setCenter(patternNavigationPane);
    }

    public void loadPatternsIntoPane(Map<EntityFacade, List<SemanticRecord>> patterns) {
        if (patterns.isEmpty()) {
            return;
        }
        patternNavViewModel.setPropertyValue(PATTERN_COLLECTION, patterns);

        //VBox resultsVBox = new VBox(); //FIXME turn into a ListView


        patterns.forEach((pattern, listOfSemantics) -> {
            JFXNode<Pane, PatternNavEntryController> patternNavEntryJFXNode = FXMLMvvmLoader
                    .make(PatternNavEntryController.class.getResource(PATTERN_NAV_ENTRY_FXML));
            HBox patternHBox = (HBox) patternNavEntryJFXNode.node();


            PatternNavEntryController controller = patternNavEntryJFXNode.controller();


//            resultsVBox.setSpacing(4); // space between pattern entries
//            patternHBox.setAlignment(Pos.CENTER);
//            Region leftPadding = new Region();
//            leftPadding.setPrefWidth(12); // pad each entry with an empty region
//            leftPadding.setPrefHeight(1);

            controller.setPatternName(pattern.description());

//            controller.getInstancesVBox().getChildren().clear();
//            controller.getInstancesVBox().getChildren().addAll(createInstances(listOfSemantics));


            //resultsVBox.getChildren().addAll(new HBox(leftPadding, patternHBox));
        });
    }

    public void loadAllPatterns() {
        ViewProperties viewProperties = patternNavViewModel.getPropertyValue(VIEW_PROPERTIES);

        TinkExecutor.threadPool().execute(() -> {
            ObservableList<EntityFacade> patternItems = FXCollections.observableArrayList();
            PrimitiveData.get().forEachPatternNid(patternNid -> {
                Latest<PatternEntityVersion> latestPattern = viewProperties.calculator().latest(patternNid);
                latestPattern.ifPresent(patternEntityVersion -> {
                    if (EntityService.get().getEntity(patternNid).isPresent()) {
                        patternItems.add(EntityService.get().getEntity(patternNid).get());
                    }
                });
            });
            patternItems.sort((o1, o2) -> {
                if ((Integer) o1.nid() instanceof Integer nid1 && (Integer) o2.nid() instanceof Integer nid2) {
                    return NaturalOrder.compareStrings(viewProperties.calculator().getDescriptionTextOrNid(nid1),
                            viewProperties.calculator().getDescriptionTextOrNid(nid2));
                } else {
                    return NaturalOrder.compareStrings(o1.toString(), o2.toString());
                }
            });
            Platform.runLater(() -> {
                patternItems.stream().forEach(patternItem -> { // each pattern row
                    // load the pattern instances into an observable list
                    ObservableList<Object> patternChildren = FXCollections.observableArrayList();
                    int patternNid = patternItem.nid();
                    AtomicInteger childCount = new AtomicInteger();

                    // populate the collection of instance for each pattern
                    PrimitiveData.get().forEachSemanticNidOfPattern(patternNid, semanticNid -> {
                        if (childCount.incrementAndGet() < maxChildrenInPatternViewer) {
                            patternChildren.add(semanticNid);
                        }
                    });
                    if (childCount.get() >= maxChildrenInPatternViewer) {
                        NumberFormat numberFormat = NumberFormat.getInstance();
                        patternChildren.add(numberFormat.format(childCount.get() - maxChildrenInPatternViewer) + " additional semantics suppressed...");
                    }
                    boolean hasChildren = patternChildren.size() > 0;

                    // load the pattern entry FXML and controller
                    JFXNode<Pane, PatternNavEntryController> patternNavEntryJFXNode = FXMLMvvmLoader
                            .make(PatternNavEntryController.class.getResource(PATTERN_NAV_ENTRY_FXML));
                    HBox patternHBox = (HBox) patternNavEntryJFXNode.node();
                    PatternNavEntryController patternNavEntryController = patternNavEntryJFXNode.controller();

                    patternsVBox.setSpacing(4); // space between pattern entries
                    patternHBox.setAlignment(Pos.CENTER);
                    Region leftPadding = new Region();
                    leftPadding.setPrefWidth(12); // pad each entry with an empty region
                    leftPadding.setPrefHeight(1);
                    if (!hasChildren) {
                        patternNavEntryController.disableInstancesListView();
                    }
                    patternsVBox.getChildren().addAll(new HBox(leftPadding, patternHBox));
                    // set the pattern's name
                    patternNavEntryController.setPatternName(patternItem.description());

                    // populate the pattern instances as a list view
                    ListView<Object> patternInstances = patternNavEntryController.getPatternInstancesListView();

                    // set the cell factory for each pattern's instances list
                    patternInstances.setCellFactory(p -> new ListCell<>() {
                        private final Label label;

                        {
                            setContentDisplay(ContentDisplay.GRAPHIC_ONLY);
                            label = new Label();
                        }

                        @Override
                        protected void updateItem(Object item, boolean empty) {
                            super.updateItem(item, empty);
                            setGraphic(null);
                            if (item != null && !empty) {
                                if (item instanceof String stringItem) {
                                    setContentDisplay(ContentDisplay.TEXT_ONLY);
                                    setText(stringItem);
                                } else if (item instanceof Integer nid) {
                                    String entityDescriptionText = viewProperties.calculator().getPreferredDescriptionTextWithFallbackOrNid(nid);
                                    Entity entity = Entity.getFast(nid);
                                    if (entity instanceof SemanticEntity<?> semanticEntity) {
                                        if (semanticEntity.patternNid() == identifierPatternProxy.nid()) {
                                            //TODO Move better string descriptions to language calculator
                                            Latest<? extends SemanticEntityVersion> latestId = viewProperties.calculator().latest(semanticEntity);
                                            ImmutableList fields = latestId.get().fieldValues();
                                            entityDescriptionText = viewProperties.calculator().getPreferredDescriptionTextWithFallbackOrNid((EntityFacade) fields.get(0)) +
                                                    ": " + fields.get(1);
                                        } else if (semanticEntity.patternNid() == inferredDefinitionPatternProxy.nid()) {
                                            entityDescriptionText =
                                                    "Inferred definition for: " + viewProperties.calculator().getPreferredDescriptionTextWithFallbackOrNid(semanticEntity.referencedComponentNid());
                                        } else if (semanticEntity.patternNid() == inferredNavigationPatternProxy.nid()) {
                                            entityDescriptionText =
                                                    "Inferred is-a relationships for: " + viewProperties.calculator().getPreferredDescriptionTextWithFallbackOrNid(semanticEntity.referencedComponentNid());
                                        } else if (semanticEntity.patternNid() == pathMembershipProxy.nid()) {
                                            entityDescriptionText =
                                                    viewProperties.calculator().getPreferredDescriptionTextWithFallbackOrNid(semanticEntity.referencedComponentNid());
                                        } else if (semanticEntity.patternNid() == statedDefinitionPatternProxy.nid()) {
                                            entityDescriptionText =
                                                    "Stated definition for: " + viewProperties.calculator().getPreferredDescriptionTextWithFallbackOrNid(semanticEntity.referencedComponentNid());
                                        } else if (semanticEntity.patternNid() == statedNavigationPatternProxy.nid()) {
                                            entityDescriptionText =
                                                    "Stated is-a relationships for: " + viewProperties.calculator().getPreferredDescriptionTextWithFallbackOrNid(semanticEntity.referencedComponentNid());
                                        } else if (semanticEntity.patternNid() == ukDialectPatternProxy.nid()) {
                                            Latest<? extends SemanticEntityVersion> latestAcceptability = viewProperties.calculator().latest(semanticEntity);
                                            ImmutableList fields = latestAcceptability.get().fieldValues();
                                            entityDescriptionText =
                                                    "UK dialect " + viewProperties.calculator().getPreferredDescriptionTextWithFallbackOrNid((EntityFacade) fields.get(0)) +
                                                            ": " + viewProperties.calculator().getPreferredDescriptionTextWithFallbackOrNid(semanticEntity.referencedComponentNid());
                                        } else if (semanticEntity.patternNid() == usDialectPatternProxy.nid()) {
                                            Latest<? extends SemanticEntityVersion> latestAcceptability = viewProperties.calculator().latest(semanticEntity);
                                            ImmutableList fields = latestAcceptability.get().fieldValues();
                                            entityDescriptionText =
                                                    "US dialect " + viewProperties.calculator().getPreferredDescriptionTextWithFallbackOrNid((EntityFacade) fields.get(0)) +
                                                            ": " + viewProperties.calculator().getPreferredDescriptionTextWithFallbackOrNid(semanticEntity.referencedComponentNid());
                                        } else if (semanticEntity.patternNid() == versionControlPathOriginPatternProxy.nid()) {
                                            Latest<? extends SemanticEntityVersion> latestPathOrigins = viewProperties.calculator().latest(semanticEntity);
                                            ImmutableList fields = latestPathOrigins.get().fieldValues();
                                            entityDescriptionText =
                                                    viewProperties.calculator().getPreferredDescriptionTextWithFallbackOrNid(semanticEntity.referencedComponentNid()) +
                                                            " origin: " + DateTimeUtil.format((Instant) fields.get(1)) +
                                                            " on " + viewProperties.calculator().getPreferredDescriptionTextWithFallbackOrNid((EntityFacade) fields.get(0));
                                        }
                                    }

                                    setContentDisplay(ContentDisplay.GRAPHIC_ONLY);
                                    label.setText(entityDescriptionText);
                                    if (!entityDescriptionText.isEmpty()) {
                                        Image identicon = Identicon.generateIdenticonImage(entity.publicId());
                                        ImageView imageView = new ImageView(identicon);
                                        imageView.setFitWidth(24);
                                        imageView.setFitHeight(24);
                                        label.setGraphic(imageView);
                                    }
                                    label.getStyleClass().add("pattern-instance");
                                    setGraphic(label);
                                }
                            }
                        }
                    });
                    if (hasChildren) {
                        Platform.runLater(() -> patternInstances.setItems(patternChildren));
                    }

                });

            });
        });
    }

}
