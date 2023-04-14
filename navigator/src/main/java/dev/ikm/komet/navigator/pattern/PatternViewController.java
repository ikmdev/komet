package dev.ikm.komet.navigator.pattern;

import javafx.application.Platform;
import javafx.beans.property.SimpleObjectProperty;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.collections.ListChangeListener;
import javafx.collections.MapChangeListener;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.GridPane;
import org.eclipse.collections.api.list.ImmutableList;
import dev.ikm.komet.framework.KometNode;
import dev.ikm.komet.framework.activity.ActivityStream;
import dev.ikm.komet.framework.graphics.Icon;
import dev.ikm.komet.framework.temp.FxGet;
import dev.ikm.komet.framework.view.ObservableView;
import dev.ikm.komet.framework.view.ViewMenuModel;
import dev.ikm.komet.framework.view.ViewProperties;
import dev.ikm.komet.preferences.KometPreferences;
import dev.ikm.tinkar.common.id.PublicIdStringKey;
import dev.ikm.tinkar.common.service.TinkExecutor;
import dev.ikm.tinkar.common.service.PrimitiveData;
import dev.ikm.tinkar.common.util.text.NaturalOrder;
import dev.ikm.tinkar.coordinate.stamp.StampPathImmutable;
import dev.ikm.tinkar.coordinate.stamp.calculator.Latest;
import dev.ikm.tinkar.coordinate.view.ViewCoordinateRecord;
import dev.ikm.tinkar.entity.Entity;
import dev.ikm.tinkar.entity.PatternEntityVersion;
import dev.ikm.tinkar.terms.EntityFacade;
import dev.ikm.tinkar.terms.EntityProxy;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.net.URL;
import java.util.ArrayList;
import java.util.OptionalInt;
import java.util.ResourceBundle;

public class PatternViewController {
    private static final Logger LOG = LoggerFactory.getLogger(PatternViewController.class);
    private static volatile boolean shutdownRequested = false;
    ViewMenuModel viewMenuModel;
    @FXML
    private ResourceBundle resources;
    @FXML
    private URL location;
    @FXML
    private BorderPane topBorderPane;
    @FXML
    private GridPane topGridPane;
    @FXML
    private ToolBar toolBar;
    @FXML
    private MenuButton navigationMenuButton;
    @FXML
    private Menu navigationCoordinateMenu;
    private ObservableView observableView;
    private OptionalInt selectedItemNidOptional = OptionalInt.empty();
    private KometPreferences nodePreferences;
    private TreeItem<Object> rootTreeItem = new TreeItem<>("Pattern root");
    private TreeView<Object> treeView = new TreeView<>();
    private ViewProperties viewProperties;
    private final ChangeListener<ViewCoordinateRecord> viewChangedListener = this::viewChanged;
    private PatternNavigatorNode patternNavigatorNode;
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

    @FXML
    void copySelectedConcepts(ActionEvent event) {

    }

    @FXML
    void initialize() {
        assert topBorderPane != null : "fx:id=\"topBorderPane\" was not injected: check your FXML file 'PatternView.fxml'.";
        assert topGridPane != null : "fx:id=\"topGridPane\" was not injected: check your FXML file 'PatternView.fxml'.";
        assert toolBar != null : "fx:id=\"toolBar\" was not injected: check your FXML file 'PatternView.fxml'.";
        assert navigationMenuButton != null : "fx:id=\"navigationMenuButton\" was not injected: check your FXML file 'PatternView.fxml'.";
        assert navigationCoordinateMenu != null : "fx:id=\"navigationCoordinateMenu\" was not injected: check your FXML file 'PatternView.fxml'.";
        this.treeView.setRoot(rootTreeItem);
        this.treeView.setShowRoot(false);
        rootTreeItem.setExpanded(true);
        topBorderPane.setCenter(this.treeView);
    }

    private void sceneChanged(ObservableValue<? extends Scene> observableValue, Scene oldScene, Scene newScene) {
        if (newScene == null) {
            shutdownInstance();
            this.topBorderPane.sceneProperty().removeListener(this.sceneChangedListener);
            this.getObservableView().removeListener(this.viewChangedListener);
        }
    }

    protected void shutdownInstance() {
        LOG.info("Shutdown graph view instance");
        this.getObservableView().removeListener(this.viewChangedListener);
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

    private void menuUpdate() {
        ImmutableList<String> navigationPatternDescriptions = this.viewProperties.nodeView().calculator().
                getPreferredDescriptionTextListForComponents(this.observableView.navigationCoordinate().navigationPatternNids());
        refreshTaxonomy();
    }

    private void refreshTaxonomy() {
        this.rootTreeItem.getChildren().clear();
        TinkExecutor.threadPool().execute(() -> {
            ArrayList<TreeItem<Object>> patternItems = new ArrayList<>();
            PrimitiveData.get().forEachPatternNid(patternNid -> {
                Latest<PatternEntityVersion> latestPattern = viewProperties.calculator().latest(patternNid);
                latestPattern.ifPresent(patternEntityVersion -> {
                    patternItems.add(new TreeItem<>(patternNid));
                });
            });
            patternItems.sort((o1, o2) -> {
                if (o1.getValue() instanceof Integer nid1 && o2.getValue() instanceof Integer nid2) {
                    return NaturalOrder.compareStrings(viewProperties.calculator().getDescriptionTextOrNid(nid1),
                            viewProperties.calculator().getDescriptionTextOrNid(nid2));
                } else {
                    return NaturalOrder.compareStrings(o1.toString(), o2.toString());
                }
            });
            Platform.runLater(() -> this.rootTreeItem.getChildren().setAll(patternItems));
            for (TreeItem<Object> patternItem : patternItems) {
                ArrayList<TreeItem<Object>> patternChildren = new ArrayList<>();
                int patternNid = (Integer) patternItem.getValue();
                this.viewProperties.calculator().forEachSemanticVersionOfPattern(patternNid, (semanticEntityVersion, patternEntityVersion) -> {
                    patternChildren.add(new TreeItem<>(semanticEntityVersion.nid()));
                });
                Platform.runLater(() -> patternItem.getChildren().setAll(patternChildren));
            }
        });
    }

    private void savePreferences() {
        // TODO selected graphConfigurationKey should be saved in preferences.
        this.nodePreferences.putObject(KometNode.PreferenceKey.ACTIVITY_STREAM_KEY, this.activityStreamKeyProperty.get());

    }

    public TreeView<Object> getTreeView() {
        return treeView;
    }

    public void setProperties(PatternNavigatorNode patternNavigatorNode, ViewProperties viewProperties, KometPreferences nodePreferences) {
        this.patternNavigatorNode = patternNavigatorNode;
        this.nodePreferences = nodePreferences;
        this.viewProperties = viewProperties;
        this.observableView = this.viewProperties.nodeView();
        this.navigationCoordinateMenu.setGraphic(Icon.COORDINATES.makeIcon());
        this.viewMenuModel = new ViewMenuModel(viewProperties, navigationMenuButton, navigationCoordinateMenu);
        treeView.setCellFactory(p -> new EntityNidTreeCell(PatternViewController.this.viewProperties));
        this.menuUpdate();
        FxGet.pathCoordinates(viewProperties.nodeView().calculator()).addListener((MapChangeListener<PublicIdStringKey, StampPathImmutable>) change -> menuUpdate());
        this.observableView.addListener(this.viewChangedListener);

        nodePreferences.getObject(KometNode.PreferenceKey.ACTIVITY_STREAM_KEY).ifPresent(activityStreamKey ->
                this.activityStreamKeyProperty.set((PublicIdStringKey<ActivityStream>) activityStreamKey));

        this.treeView.getSelectionModel().getSelectedItems().addListener(this::onSelectionChanged);

        this.topBorderPane.setTop(topGridPane);


        refreshTaxonomy();
        this.getObservableView().addListener(this.viewChangedListener);
        this.topBorderPane.sceneProperty().addListener(this.sceneChangedListener);
    }

    private void onSelectionChanged(ListChangeListener.Change<? extends TreeItem<Object>> c) {
        ActivityStream activityStream = this.activityStreamProperty.get();
        if (activityStream != null) {
            EntityFacade[] selectionArray = new EntityFacade[c.getList().size()];
            int i = 0;
            for (TreeItem<Object> treeItem : c.getList()) {
                if (treeItem.getValue() instanceof Entity entity)
                    selectionArray[i++] = EntityProxy.make(entity.nid());
            }
            activityStream.dispatch(selectionArray);
            LOG.atTrace().log("Selected: " + c.getList());
        }
    }

    private ChangeListener<Scene> sceneChangedListener = this::sceneChanged;


}
