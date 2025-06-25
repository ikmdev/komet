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
package dev.ikm.komet.navigator.graph;

import dev.ikm.komet.framework.Dialogs;
import dev.ikm.komet.framework.KometNode;
import dev.ikm.komet.framework.LayoutAnimator;
import dev.ikm.komet.framework.RefreshListener;
import dev.ikm.komet.framework.activity.ActivityStream;
import dev.ikm.komet.framework.activity.ActivityStreams;
import dev.ikm.komet.framework.alerts.AlertPanel;
import dev.ikm.komet.framework.concurrent.TaskWrapper;
import dev.ikm.komet.framework.dnd.ClipboardHelper;
import dev.ikm.komet.framework.dnd.KometClipboard;
import dev.ikm.komet.framework.graphics.Icon;
import dev.ikm.komet.framework.temp.FxGet;
import dev.ikm.komet.framework.view.ObservableView;
import dev.ikm.komet.framework.view.ViewMenuModel;
import dev.ikm.komet.framework.view.ViewProperties;
import dev.ikm.komet.navigator.graph.treetasks.ExpandTask;
import dev.ikm.komet.preferences.KometPreferences;
import dev.ikm.tinkar.common.alert.AlertCategory;
import dev.ikm.tinkar.common.alert.AlertObject;
import dev.ikm.tinkar.common.alert.AlertType;
import dev.ikm.tinkar.common.id.IntIdList;
import dev.ikm.tinkar.common.id.IntIds;
import dev.ikm.tinkar.common.id.PublicIdStringKey;
import dev.ikm.tinkar.common.service.TinkExecutor;
import dev.ikm.tinkar.common.util.broadcast.Subscriber;
import dev.ikm.tinkar.coordinate.navigation.calculator.Edge;
import dev.ikm.tinkar.coordinate.stamp.StampPathImmutable;
import dev.ikm.tinkar.coordinate.view.ViewCoordinate;
import dev.ikm.tinkar.coordinate.view.ViewCoordinateRecord;
import dev.ikm.tinkar.coordinate.view.calculator.ViewCalculator;
import dev.ikm.tinkar.entity.ConceptEntity;
import dev.ikm.tinkar.entity.Entity;
import dev.ikm.tinkar.entity.VersionProxy;
import dev.ikm.tinkar.entity.VersionProxyFactory;
import dev.ikm.tinkar.terms.*;
import javafx.application.Platform;
import javafx.beans.property.SimpleObjectProperty;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.collections.FXCollections;
import javafx.collections.ListChangeListener;
import javafx.collections.MapChangeListener;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.geometry.HPos;
import javafx.geometry.VPos;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.input.Clipboard;
import javafx.scene.input.DragEvent;
import javafx.scene.input.Dragboard;
import javafx.scene.input.TransferMode;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.Priority;
import org.eclipse.collections.api.list.ImmutableList;
import org.eclipse.collections.api.list.primitive.MutableIntList;
import org.eclipse.collections.api.set.primitive.MutableIntSet;
import org.eclipse.collections.impl.factory.primitive.IntLists;
import org.eclipse.collections.impl.factory.primitive.IntSets;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.*;
import java.util.concurrent.CountDownLatch;

import static dev.ikm.komet.framework.StyleClasses.MULTI_PARENT_TREE_NODE;

public class MultiParentGraphViewController implements RefreshListener {
    private static final Logger LOG = LoggerFactory.getLogger(MultiParentGraphViewController.class);
    private static volatile boolean shutdownRequested = false;
    private final EntityChangeSubscriber ENTITY_CHANGE_SUBSCRIBER = new EntityChangeSubscriber();
    {
        Entity.provider().addSubscriberWithWeakReference(ENTITY_CHANGE_SUBSCRIBER);
    }

    private final Label navigationLabel = new Label();
    private final MutableIntList expandedNids = IntLists.mutable.empty();
    private final ObservableList<AlertObject> alertList = FXCollections.observableArrayList();
    /**
     * added to prevent garbage collection of listener while this node is still
     * active
     */
    private final LayoutAnimator topPaneAnimator = new LayoutAnimator();
    private final LayoutAnimator alertsAnimator = new LayoutAnimator();
    private final SimpleObjectProperty<Navigator> navigatorProperty = new SimpleObjectProperty<>();
    private final UUID uuid = UUID.randomUUID();
    @FXML
    Menu navigationCoordinateMenu;
    ViewMenuModel viewMenuModel;
    private ObservableView observableView;
    @FXML
    private BorderPane topBorderPane;
    @FXML
    private GridPane topGridPane;
    @FXML
    private ToolBar toolBar;
    @FXML
    private MenuButton navigationMenuButton;
    //~--- fieldValues --------------------------------------------------------------
    private NavigatorDisplayPolicies displayPolicies;
    private OptionalInt selectedItemNidOptional = OptionalInt.empty();
    private KometPreferences nodePreferences;

    private MultiParentVertexImpl rootTreeItem;
    private TreeView<ConceptFacade> treeView;
    private ViewProperties viewProperties;
    private final ChangeListener<ViewCoordinateRecord> viewChangedListener = this::viewChanged;
    private GraphNavigatorNode graphNavigatorNode;
    private SimpleObjectProperty<PublicIdStringKey<ActivityStream>> activityStreamKeyProperty = new SimpleObjectProperty<>();
    private SimpleObjectProperty<ActivityStream> activityStreamProperty = new SimpleObjectProperty<>();

    /**
     * Tell the tree to stop whatever threading operations it has running, since
     * the application is exiting.
     */
    public static void globalShutdownRequested() {
        shutdownRequested = true;
        LOG.info("Global Navigator shutdown called!");
    }

    protected static boolean wasGlobalShutdownRequested() {
        return shutdownRequested;
    }

    private void sceneChanged(ObservableValue<? extends Scene> observableValue, Scene oldScene, Scene newScene) {
        if (newScene == null) {
            this.topBorderPane.sceneProperty().removeListener(this.sceneChangedListener);
            this.getObservableView().removeListener(this.viewChangedListener);
        }
    }

    public void shutdownInstance() {
        LOG.info("Shutdown graph view instance");
        this.getObservableView().removeListener(this.viewChangedListener);
        if (rootTreeItem != null) {
            rootTreeItem.clearChildren();  // This recursively cancels any active lookups
        }
    }

    public ObservableView getObservableView() {
        return this.observableView;
    }

    private void viewChanged(ObservableValue<? extends ViewCoordinateRecord> observable,
                             ViewCoordinateRecord oldValue,
                             ViewCoordinateRecord newValue) {
        this.menuUpdate();
        this.refreshTaxonomy();
    }

    @FXML
    void initialize() {

        this.treeView = new TreeView<>();

        MenuItem generateGraphSource = new MenuItem("Generate graph source");
        generateGraphSource.setOnAction(this::generateJGraphTCode);

        activityStreamKeyProperty.addListener((observable, oldValue, newValue) -> {
            this.activityStreamProperty.set(ActivityStreams.get(newValue));
        });

        this.treeView.setContextMenu(new ContextMenu(generateGraphSource));

        this.treeView.getSelectionModel()
                .setSelectionMode(SelectionMode.MULTIPLE);
        this.treeView.setCellFactory((TreeView<ConceptFacade> p) -> new MultiParentGraphCell(treeView));
        this.treeView.setShowRoot(false);
        this.rootTreeItem = new MultiParentVertexImpl(
                MultiParentGraphViewController.this);
        this.treeView.setRoot(rootTreeItem);

        // put this event handler on the root
        rootTreeItem.addEventHandler(
                TreeItem.branchCollapsedEvent(),
                (TreeItem.TreeModificationEvent<ConceptFacade> t) -> {
                    ((MultiParentVertexImpl) t.getSource()).removeChildren();
                });
        rootTreeItem.addEventHandler(
                TreeItem.branchExpandedEvent(),
                (TreeItem.TreeModificationEvent<ConceptFacade> t) -> {
                    MultiParentVertexImpl sourceTreeItem = (MultiParentVertexImpl) t.getSource();
                    if (sourceTreeItem.getChildren().isEmpty()) {
                        TinkExecutor.threadPool()
                                .execute(() -> sourceTreeItem.addChildren());
                    }
                });

        alertList.addListener(this::onChanged);


        this.topBorderPane.getStyleClass().setAll(MULTI_PARENT_TREE_NODE.toString());

        this.topBorderPane.setCenter(this.treeView);
        toolBar.getItems().add(this.navigationLabel);

        this.navigationMenuButton.setGraphic(Icon.VIEW.makeIcon());

    }

    private ChangeListener<Scene> sceneChangedListener = this::sceneChanged;

    @FXML
    void copySelectedConcepts(ActionEvent event) {
        List<EntityProxy> identifiedObjects = new ArrayList<>();
        for (TreeItem<ConceptFacade> ConceptEntityTreeItem : this.treeView.getSelectionModel().getSelectedItems()) {
            identifiedObjects.add(ConceptEntityTreeItem.getValue().toProxy());
        }
        Clipboard.getSystemClipboard().setContent(new KometClipboard(identifiedObjects));
    }

    private void savePreferences() {
        // TODO selected graphConfigurationKey should be saved in preferences.
        this.nodePreferences.putObject(KometNode.PreferenceKey.ACTIVITY_STREAM_KEY, this.activityStreamKeyProperty.get());

    }

    private void menuUpdate() {
        ImmutableList<String> navigationPatternDescriptions = this.viewProperties.nodeView().calculator().
                getPreferredDescriptionTextListForComponents(this.observableView.navigationCoordinate().navigationPatternNids());
        this.navigationLabel.setText(navigationPatternDescriptions.toString());
        refreshTaxonomy();
    }

    public void setProperties(GraphNavigatorNode graphNavigatorNode, ViewProperties viewProperties, KometPreferences nodePreferences) {
        this.graphNavigatorNode = graphNavigatorNode;
        this.nodePreferences = nodePreferences;
        this.viewProperties = viewProperties;
        this.observableView = this.viewProperties.nodeView();
        this.navigationCoordinateMenu.setGraphic(Icon.COORDINATES.makeIcon());
        this.viewMenuModel = new ViewMenuModel(viewProperties, navigationMenuButton, navigationCoordinateMenu);
        this.menuUpdate();
        FxGet.pathCoordinates(viewProperties.nodeView().calculator()).addListener((MapChangeListener<PublicIdStringKey, StampPathImmutable>) change -> menuUpdate());
        this.observableView.addListener(this.viewChangedListener);

        nodePreferences.getObject(KometNode.PreferenceKey.ACTIVITY_STREAM_KEY).ifPresent(activityStreamKey ->
                this.activityStreamKeyProperty.set((PublicIdStringKey<ActivityStream>) activityStreamKey));

        this.treeView.getSelectionModel().getSelectedItems().addListener(this::onSelectionChanged);

        this.displayPolicies = new DefaultNavigatorDisplayPolicies();

        this.topPaneAnimator.observe(topGridPane);
        this.topBorderPane.setTop(topGridPane);
        this.alertsAnimator.observe(this.topBorderPane.getChildren());
        handleDescriptionTypeChange(null);

        setupTopPane();
        // Not a leak, since the taxonomy service adds a weak reference to the listener.
        // TODO: decide how to manage taxonomy refresh.
        // Get.taxonomyService().addTaxonomyRefreshListener(this);

        this.topBorderPane.setOnDragOver(this::dragOver);
        this.topBorderPane.setOnDragDropped(this::dragDropped);
        refreshTaxonomy();
        this.getObservableView().addListener(this.viewChangedListener);
        this.topBorderPane.sceneProperty().addListener(this.sceneChangedListener);
    }

    private void onSelectionChanged(ListChangeListener.Change<? extends TreeItem<ConceptFacade>> c) {
        ActivityStream activityStream = this.activityStreamProperty.get();
        if (activityStream != null) {
//            EntityFacade[] selectionArray = new EntityFacade[c.getList().size()];
//            int i = 0;
//            for (TreeItem<ConceptFacade> treeItem : c.getList()) {
//                selectionArray[i++] = treeItem.getValue();
//            }
//            activityStream.dispatch(selectionArray);
//            LOG.atTrace().log("Selected: " + c.getList());
            EntityFacade[] selectionArray = new EntityFacade[0];
            c.getList().stream()
                    .filter(treeItem -> treeItem!=null && treeItem.getValue() instanceof EntityFacade)
                    .map(treeItem -> treeItem.getValue()).toList().toArray(selectionArray);

            if (selectionArray != null && selectionArray.length > 0) {
                activityStream.dispatch(selectionArray);
            }
            LOG.atTrace().log("Selected: " + c.getList());
        }
    }

    private void dragDropped(DragEvent event) {
        Dragboard db = event.getDragboard();
        boolean success = false;
        if (db.hasContent(KometClipboard.KOMET_CONCEPT_PROXY)) {
            EntityProxy.Concept conceptProxy = ProxyFactory.fromXmlFragment((String) db.getContent(KometClipboard.KOMET_CONCEPT_PROXY));
            showConcept(conceptProxy.nid());
            success = true;
        } else if (db.hasContent(KometClipboard.KOMET_SEMANTIC_PROXY)) {
            EntityProxy.Semantic semanticProxy = ProxyFactory.fromXmlFragment((String) db.getContent(KometClipboard.KOMET_SEMANTIC_PROXY));
            Optional<ConceptEntity> optionalConcept = Entity.getConceptForSemantic(semanticProxy);
            optionalConcept.ifPresent(conceptEntity -> showConcept(conceptEntity.nid()));
            success = optionalConcept.isPresent();
        } else if (db.hasContent(KometClipboard.KOMET_PATTERN_PROXY)) {
            EntityProxy.Pattern patternProxy = ProxyFactory.fromXmlFragment((String) db.getContent(KometClipboard.KOMET_PATTERN_PROXY));
            // don't know what to do with pattern and navigation...
        } else if (db.hasContent(KometClipboard.KOMET_CONCEPT_VERSION_PROXY)) {
            VersionProxy.Concept conceptProxy = VersionProxyFactory.fromXmlFragment((String) db.getContent(KometClipboard.KOMET_CONCEPT_VERSION_PROXY));
            showConcept(conceptProxy.nid());
        } else if (db.hasContent(KometClipboard.KOMET_SEMANTIC_VERSION_PROXY)) {
            VersionProxy.Semantic semanticProxy = VersionProxyFactory.fromXmlFragment((String) db.getContent(KometClipboard.KOMET_SEMANTIC_VERSION_PROXY));
            Optional<ConceptEntity> optionalConcept = Entity.getConceptForSemantic(semanticProxy);
            optionalConcept.ifPresent(conceptEntity -> showConcept(conceptEntity.nid()));
            success = optionalConcept.isPresent();
        } else if (db.hasContent(KometClipboard.KOMET_PATTERN_VERSION_PROXY)) {
            VersionProxy.Pattern patternProxy = VersionProxyFactory.fromXmlFragment((String) db.getContent(KometClipboard.KOMET_PATTERN_VERSION_PROXY));
            // don't know what to do with pattern and navigation...
        }
        /* let the source know if the dropped item was successfully
         * transferred and used */
        event.setDropCompleted(success);

        event.consume();
    }

    private void dragOver(DragEvent event) {

        /* accept it only if it is  not dragged from the same node */
        if (event.getGestureSource() != this) {
            /* allow for both copying */
            event.acceptTransferModes(TransferMode.COPY);
        }

        event.consume();
    }

    @Override
    public UUID getListenerUuid() {
        return this.uuid;
    }

    @Override
    public void refresh() {
        Platform.runLater(() -> {
            this.refreshTaxonomy();
        });
    }

    public void showConcept(final int conceptNid) {
        // Do work in background.
        ShowConceptInGraphTask task
                = new ShowConceptInGraphTask(this, conceptNid);

        TinkExecutor.threadPool()
                .submit(task);
    }

    /**
     * The first call you make to this should pass in the root node.
     * <p>
     * After that you can call it repeatedly to walk down the tree (you need to
     * know the path first) This will handle the waiting for each node to open,
     * before moving on to the next node.
     * <p>
     * This should be called on a background thread.
     *
     * @param item
     * @param targetChildNid
     * @return the found child, or null, if not found. found child will have
     * already been told to expand and fetch its children.
     * @throws InterruptedException
     */
    protected MultiParentVertexImpl findChild(final MultiParentVertexImpl item,
                                              final int targetChildNid)
            throws InterruptedException {
        LOG.debug("Looking for " + targetChildNid);

        SimpleObjectProperty<MultiParentVertexImpl> found = new SimpleObjectProperty<>(null);

        if (item.getValue().nid() == targetChildNid) {
            // Found it.
            found.set(item);
        } else {
            item.blockUntilChildrenReady();

            // Iterate through children and look for child with target UUID.
            for (TreeItem<ConceptFacade> child : item.getChildren()) {
                if ((child != null) && (child.getValue() != null) && child.getValue().nid() == targetChildNid) {
                    // Found it.
                    found.set((MultiParentVertexImpl) child);
                    break;
                }
            }
        }

        if (found.get() != null) {
            found.get().blockUntilChildrenReady();

            CountDownLatch cdl = new CountDownLatch(1);

            Platform.runLater(
                    () -> {
                        treeView.scrollTo(treeView.getRow(found.get()));
                        found.get().setExpanded(true);
                        cdl.countDown();
                    });
            cdl.await();
        } else {
            LOG.debug("Find child failed to find {}", targetChildNid);
        }

        return found.get();
    }

    //~--- methods -------------------------------------------------------------

    private void onChanged(ListChangeListener.Change<? extends AlertObject> change) {
        setupTopPane();
    }

    private void restoreExpanded() {
        treeView.getSelectionModel()
                .clearSelection();
        TinkExecutor.threadPool()
                .execute(
                        () -> {
                            try {
                                SimpleObjectProperty<MultiParentVertexImpl> scrollTo = new SimpleObjectProperty<>();
                                if (scrollTo.get() != null) {

                                    restoreExpanded(rootTreeItem, scrollTo);
                                    expandedNids.clear();
                                    selectedItemNidOptional = OptionalInt.empty();

                                    if (scrollTo.get() != null) {
                                        Platform.runLater(
                                                () -> {
                                                    treeView.scrollTo(treeView.getRow(scrollTo.get()));
                                                    treeView.getSelectionModel()
                                                            .select(scrollTo.get());
                                                });
                                    }
                                }
                            } catch (InterruptedException e) {
                                LOG.info("Interrupted while looking restoring expanded items");
                            }
                        });
    }

    private void restoreExpanded(MultiParentVertexImpl item,
                                 SimpleObjectProperty<MultiParentVertexImpl> scrollTo)
            throws InterruptedException {
        if (expandedNids.contains(item.getConceptNid())) {
            item.addChildren();
            item.blockUntilChildrenReady();
            Platform.runLater(() -> item.setExpanded(true));

            List<TreeItem<ConceptFacade>> list = new ArrayList<>(item.getChildren());

            for (TreeItem<ConceptFacade> child : list) {
                restoreExpanded((MultiParentVertexImpl) child, scrollTo);
            }
        }
        selectedItemNidOptional.ifPresent(nid -> {
            if (nid == item.getConceptNid()) {
                scrollTo.set(item);
            }
        });
    }

    public void expandAndSelect(IntIdList expansionPath) {
        boolean foundRoot = false;
        for (TreeItem<ConceptFacade> rootConcept : rootTreeItem.getChildren()) {
            MultiParentVertexImpl viewRoot = (MultiParentVertexImpl) rootConcept;
            if (viewRoot.getConceptNid() == expansionPath.get(0)) {
                foundRoot = true;
                ExpandTask expandTask = new ExpandTask(this, expansionPath);
                TinkExecutor.threadPool().execute(TaskWrapper.make(expandTask));
            }
        }
        if (!foundRoot) {
            String alertTitle = "Expansion error";
            String alertDescription = "Expansion path for concept ends at: " + this.viewProperties.nodeView().getDescriptionTextOrNid(expansionPath.get(0));
            AlertType alertType = AlertType.ERROR;
            AlertCategory alertCategory = AlertCategory.TAXONOMY;
            int[] affectedComponents = new int[0];
            AlertObject alert = new AlertObject(alertTitle, alertDescription, alertType, alertCategory,
                    affectedComponents);
            this.graphNavigatorNode.dispatchAlert(alert);
        }
    }

    private void saveExpanded() {
        if (rootTreeItem.getChildren()
                .isEmpty()) {
            // keep the last save
            return;
        }

        TreeItem<ConceptFacade> selected = treeView.getSelectionModel().getSelectedItem();

        if (selected == null) {
            selectedItemNidOptional = OptionalInt.empty();
        } else {
            selectedItemNidOptional = OptionalInt.of(selected.getValue().nid());
        }
        expandedNids.clear();
        saveExpanded(rootTreeItem);
        LOG.debug("Saved {} expanded nodes", expandedNids.size());
    }

    private void saveExpanded(MultiParentVertexImpl item) {
        if (!item.isLeaf() && item.isExpanded()) {
            expandedNids.add(item.getConceptNid());

            if (!item.isLeaf()) {
                for (TreeItem<ConceptFacade> child : item.getChildren()) {
                    saveExpanded((MultiParentVertexImpl) child);
                }
            }
        }
    }

    private void setupTopPane() {

        // Node child, int columnIndex, int rowIndex, int columnspan, int rowspan,
        // HPos halignment, VPos valignment, Priority hgrow, Priority vgrow
        topPaneAnimator.unobserve(topGridPane.getChildren());
        topGridPane.getChildren().clear();
        double yStart = topGridPane.getLayoutY();
        int row = 0;

        GridPane.setConstraints(toolBar, 0, row++, 1, 1, HPos.LEFT, VPos.TOP, Priority.ALWAYS, Priority.NEVER);
        topGridPane.getChildren()
                .add(toolBar);

        for (AlertObject alert : alertList) {
            AlertPanel alertPanel = new AlertPanel(alert);
            alertPanel.layoutYProperty().set(toolBar.getHeight());
            topPaneAnimator.observe(alertPanel);

            GridPane.setConstraints(alertPanel, 0, row++, 1, 1, HPos.LEFT, VPos.TOP, Priority.ALWAYS, Priority.NEVER);
            topGridPane.getChildren()
                    .add(alertPanel);
        }

    }

    public final void generateSmartGraphCode(ActionEvent event) {
        TreeItem<ConceptFacade> item = this.treeView.getSelectionModel().getSelectedItem();
        ConceptFacade concept = item.getValue();
        Navigator navigator = navigatorProperty.get();
        MutableIntSet conceptNids = IntSets.mutable.empty();
        HashMap<Integer, ArrayList<Edge>> taxonomyLinks = new HashMap<>();
        handleConcept(concept.nid(), navigator, conceptNids, taxonomyLinks);
        String conceptName = this.viewProperties.calculator().getDescriptionTextOrNid(concept.nid());
        conceptName = conceptName.replaceAll("\\s+", "_");
        conceptName = conceptName.replaceAll("-", "_");
        StringBuffer buff = new StringBuffer("private Graph<String, String> build_" + conceptName + "() {\n");
        buff.append("\n   Graph<String, String> g = new GraphEdgeList<>();\n\n");
        ViewCoordinate viewCoordinate = this.getObservableView();
        conceptNids.forEach(nid -> {
            buff.append("   g.insertVertex(\"").append(this.viewProperties.calculator().getPreferredDescriptionTextWithFallbackOrNid(nid)).append("\");\n");
        });
        buff.append("\n");
        int edgeCount = 1;
        for (Map.Entry<Integer, ArrayList<Edge>> entry : taxonomyLinks.entrySet()) {
            for (Edge link : entry.getValue()) {
                buff.append("   g.insertEdge(\"").append(this.viewProperties.calculator().getPreferredDescriptionTextWithFallbackOrNid(entry.getKey())).append("\", \"")
                        .append(this.viewProperties.calculator().getPreferredDescriptionTextWithFallbackOrNid(link.destinationNid())).append("\", \"").append(edgeCount++).append("\");\n");
            }
        }
        buff.append("   return g;\n}\n");
        ClipboardHelper.copyToClipboard(buff);
        LOG.info(event.toString());
    }

    public final void generateJGraphTCode(ActionEvent event) {
        TreeItem<ConceptFacade> item = this.treeView.getSelectionModel().getSelectedItem();
        ConceptFacade concept = item.getValue();
        Navigator navigator = navigatorProperty.get();
        MutableIntSet conceptNids = IntSets.mutable.empty();
        HashMap<Integer, ArrayList<Edge>> taxonomyLinks = new HashMap<>();
        handleConcept(concept.nid(), navigator, conceptNids, taxonomyLinks);
        String conceptName = this.viewProperties.calculator().getDescriptionTextOrNid(concept.nid());
        conceptName = conceptName.replaceAll("\\s+", "_");
        conceptName = conceptName.replaceAll("-", "_");
        conceptName = conceptName.replace('(', '_');
        conceptName = conceptName.replace(')', '_');
        StringBuffer buff = new StringBuffer("private static Graph<String, DefaultEdge> build_" + conceptName + "() {\n");
        buff.append("\n   Graph<String, DefaultEdge> g = new DefaultDirectedGraph<>(DefaultEdge.class);\n\n");
        ViewCoordinate observableView = this.getObservableView();
        conceptNids.forEach(nid -> {
            buff.append("   g.addVertex(\"\\\"").append(this.viewProperties.calculator().getPreferredDescriptionTextWithFallbackOrNid(nid)).append("\\\"\");\n");
        });
        buff.append("\n");
        int edgeCount = 1;
        for (Map.Entry<Integer, ArrayList<Edge>> entry : taxonomyLinks.entrySet()) {
            for (Edge link : entry.getValue()) {
                buff.append("   g.addEdge(\"\\\"").append(this.viewProperties.calculator().getPreferredDescriptionTextWithFallbackOrNid(entry.getKey())).append("\\\"\", \"\\\"")
                        .append(this.viewProperties.calculator().getPreferredDescriptionTextWithFallbackOrNid(link.destinationNid())).append("\\\"\");\n");
            }
        }
        buff.append("   return g;\n}\n");
        ClipboardHelper.copyToClipboard(buff);
        LOG.info(event.toString());
    }

    private void handleConcept(int conceptNid, Navigator navigator, MutableIntSet conceptNids, HashMap<Integer, ArrayList<Edge>> taxonomyLinks) {
        if (!conceptNids.contains(conceptNid)) {
            conceptNids.add(conceptNid);
            ArrayList<Edge> linkList = new ArrayList<>();
            taxonomyLinks.put(conceptNid, linkList);
            for (Edge link : navigator.getParentEdges(conceptNid)) {
                if (link.typeNids().contains(TinkarTerm.IS_A.nid())) {
                    linkList.add(link);
                }
                handleConcept(link.destinationNid(), navigator, conceptNids, taxonomyLinks);
            }
        }
    }

    public final void handleDescriptionTypeChange(ActionEvent event) {
        this.rootTreeItem.invalidate();
        this.treeView.refresh();
    }

    private void refreshTaxonomy() {
        saveExpanded();
        Navigator navigator = new EmptyNavigator(this.getObservableView());
        try {
            navigator = new ViewNavigator(this.viewProperties.nodeView());
        } catch (IllegalStateException ex) {
            Dialogs.showErrorDialog("Error computing view navigator", "Do you have more that one premise type selected?", ex, topGridPane.getScene().getWindow());
        }
        this.navigatorProperty.set(navigator);
        this.rootTreeItem.clearChildren();
        if (this.navigatorProperty.get().getRootNids().length > 1) {
            LOG.error("To many roots: " + this.navigatorProperty.get().getRootNids());
        }
        for (int rootNid : this.navigatorProperty.get().getRootNids()) {
            MultiParentVertexImpl graphRoot = new MultiParentVertexImpl(
                    Entity.getFast(rootNid),
                    MultiParentGraphViewController.this,
                    IntIds.set.empty(),
                    Icon.TAXONOMY_ROOT_ICON.makeIcon());
            this.rootTreeItem.getChildren().add(graphRoot);
        }
        for (TreeItem<ConceptFacade> rootChild : this.rootTreeItem.getChildren()) {
            ((MultiParentVertexImpl) rootChild).clearChildren();
            TinkExecutor.threadPool().execute(() -> ((MultiParentVertexImpl) rootChild).addChildren());
        }

        this.rootTreeItem.invalidate();
        this.alertList.clear();
        restoreExpanded();
    }

    //~--- get methods ---------------------------------------------------------
    public NavigatorDisplayPolicies getDisplayPolicies() {
        return displayPolicies;
    }

    //~--- set methods ---------------------------------------------------------
    public void setDisplayPolicies(NavigatorDisplayPolicies policies) {
        this.displayPolicies = policies;
    }

    public MultiParentVertexImpl getRoot() {
        return rootTreeItem;
    }

    public Navigator getNavigator() {
        return navigatorProperty.get();
    }

    public BorderPane getPane() {
        return topBorderPane;
    }

    public TreeView<ConceptFacade> getTreeView() {
        return treeView;
    }

    public ViewCalculator getViewCalculator() {
        return getObservableView().calculator();
    }

    public void dispatchAlert(AlertObject alertObject) {
        graphNavigatorNode.dispatchAlert(alertObject);
    }



    //~--- get methods ---------------------------------------------------------

    private class EntityChangeSubscriber implements Subscriber<Integer> {

        @Override
        public void onNext(Integer nid) {
            Platform.runLater(() -> this.handleChange(nid, MultiParentGraphViewController.this.rootTreeItem));
        }

        private void handleChange(int nid, MultiParentVertexImpl treeItem) {
            // TODO: Change could be a semantic, concept, pattern, or stamp...
            // Need to decide how (or if) to handle STAMP. Do we look at the versions and see if the stamp matches?
            if (treeItem.getConceptNid() == nid) {
                // Update description if desc changed
                treeItem.invalidate();
                // refresh the children of this changed node...
                refreshTaxonomy();
            } else {
                // see if any of the recursive children of this node match...
                ObservableList<TreeItem<ConceptFacade>> children = treeItem.getChildren();
                for (MultiParentVertexImpl childItem: children.toArray(new MultiParentVertexImpl[children.size()])) {
                    handleChange(nid, childItem);
                }
            }
        }
    }
}

