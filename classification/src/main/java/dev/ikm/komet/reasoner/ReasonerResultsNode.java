package dev.ikm.komet.reasoner;

import javafx.application.Platform;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.fxml.FXMLLoader;
import javafx.scene.Node;
import javafx.scene.control.Label;
import javafx.scene.control.MenuItem;
import javafx.scene.control.SeparatorMenuItem;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.HBox;
import org.eclipse.collections.api.list.ImmutableList;
import dev.ikm.komet.framework.ExplorationNodeAbstract;
import dev.ikm.komet.framework.TopPanelFactory;
import dev.ikm.komet.framework.activity.ActivityStreams;
import dev.ikm.komet.framework.view.ViewProperties;
import dev.ikm.komet.preferences.KometPreferences;
import dev.ikm.komet.reasoner.elk.RunElkReasonerTask;
import dev.ikm.komet.reasoner.sorocket.RunSnoRocketReasonerTask;
import dev.ikm.tinkar.common.alert.AlertStreams;
import dev.ikm.tinkar.common.service.TinkExecutor;
import dev.ikm.tinkar.terms.EntityFacade;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.util.ArrayList;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Future;

import static dev.ikm.tinkar.terms.TinkarTerm.EL_PLUS_PLUS_INFERRED_AXIOMS_PATTERN;
import static dev.ikm.tinkar.terms.TinkarTerm.EL_PLUS_PLUS_STATED_AXIOMS_PATTERN;

public class ReasonerResultsNode extends ExplorationNodeAbstract {
    private static final Logger LOG = LoggerFactory.getLogger(ReasonerResultsNode.class);
    protected static final String STYLE_ID = "classification-results-node";
    protected static final String TITLE = "Reasoner Results";
    private final BorderPane contentPane = new BorderPane();
    private final HBox centerBox;

    private ReasonerResultsController resultsController;

    public ReasonerResultsNode(ViewProperties viewProperties, KometPreferences nodePreferences) {
        super(viewProperties, nodePreferences);
        this.centerBox = new HBox(5, new Label("   reasoner "));

        Platform.runLater(() -> {
            TopPanelFactory.TopPanelParts topPanelParts = TopPanelFactory.make(viewProperties,
                    activityStreamKeyProperty, optionForActivityStreamKeyProperty, centerBox);
            this.contentPane.setTop(topPanelParts.topPanel());
            Platform.runLater(() -> {
                ArrayList<MenuItem> collectionMenuItems = new ArrayList<>();
                collectionMenuItems.add(new SeparatorMenuItem());

                MenuItem snorocketReasonerMenuItem = new MenuItem("Run SnoRocket reasoner");
                snorocketReasonerMenuItem.setOnAction(this::snorocketReasoner);
                collectionMenuItems.add(snorocketReasonerMenuItem);

                MenuItem elkReasonerMenuItem = new MenuItem("Run ELK reasoner");
                elkReasonerMenuItem.setOnAction(this::elkReasoner);
                collectionMenuItems.add(elkReasonerMenuItem);

                ObservableList<MenuItem> topMenuItems = topPanelParts.viewPropertiesMenuButton().getItems();
                topMenuItems.addAll(collectionMenuItems);
            });
        });

        Platform.runLater(() -> {
            try {
                FXMLLoader loader = new FXMLLoader(getClass().getResource("/dev/ikm/komet/reasoner/ReasonerResultsInterface.fxml"));
                loader.load();
                this.resultsController = loader.getController();

                resultsController.setViewProperties(this.viewProperties, ActivityStreams.get(ActivityStreams.REASONER));
                contentPane.setCenter(loader.getRoot());
            } catch (IOException e) {
                AlertStreams.dispatchToRoot(e);
            }
        });
    }

    private void snorocketReasoner(ActionEvent actionEvent) {
        TinkExecutor.threadPool().execute(() -> {
            RunSnoRocketReasonerTask runSnoRocketReasonerTask =
                    new RunSnoRocketReasonerTask(getViewProperties().calculator(), EL_PLUS_PLUS_STATED_AXIOMS_PATTERN,
                            EL_PLUS_PLUS_INFERRED_AXIOMS_PATTERN, resultsController::setResults);
            Future<AxiomData> reasonerFuture = TinkExecutor.threadPool().submit(runSnoRocketReasonerTask);
            AxiomData axiomData = null;
            int statedCount = 0;
            try {
                axiomData = reasonerFuture.get();
                statedCount = axiomData.processedSemantics.get();
            } catch (InterruptedException | ExecutionException e) {
                AlertStreams.dispatchToRoot(e);
            }

            LOG.info("Stated axiom count: " + statedCount + " " + runSnoRocketReasonerTask.durationString());
        });
    }

    private void elkReasoner(ActionEvent actionEvent) {
        TinkExecutor.threadPool().execute(() -> {
            RunElkReasonerTask runElkReasonerTask =
                    new RunElkReasonerTask(getViewProperties().calculator(), EL_PLUS_PLUS_STATED_AXIOMS_PATTERN,
                            EL_PLUS_PLUS_INFERRED_AXIOMS_PATTERN, resultsController::setResults);
            Future<AxiomData> reasonerFuture = TinkExecutor.threadPool().submit(runElkReasonerTask);
            AxiomData axiomData = null;
            int statedCount = 0;
            try {
                axiomData = reasonerFuture.get();
                statedCount = axiomData.processedSemantics.get();
            } catch (InterruptedException | ExecutionException e) {
                AlertStreams.dispatchToRoot(e);
            }

            LOG.info("Stated axiom count: " + statedCount + " " + runElkReasonerTask.durationString());
        });
    }

    @Override
    public String getDefaultTitle() {
        return TITLE;
    }

    @Override
    public void handleActivity(ImmutableList<EntityFacade> entities) {
        // Nothing to do...
    }

    @Override
    public void revertAdditionalPreferences() {

    }

    @Override
    public String getStyleId() {
        return STYLE_ID;
    }

    @Override
    protected void saveAdditionalPreferences() {

    }

    @Override
    public Node getNode() {
        return contentPane;
    }

    @Override
    public void close() {

    }

    @Override
    public boolean canClose() {
        return false;
    }

    @Override
    public Class factoryClass() {
        return ReasonerResultsNodeFactory.class;
    }
}