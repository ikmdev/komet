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
package dev.ikm.komet.reasoner;

import dev.ikm.komet.framework.ExplorationNodeAbstract;
import dev.ikm.komet.framework.TopPanelFactory;
import dev.ikm.komet.framework.activity.ActivityStreams;
import dev.ikm.komet.framework.concurrent.TaskWrapper;
import dev.ikm.komet.framework.progress.ProgressHelper;
import dev.ikm.komet.framework.view.ViewProperties;
import dev.ikm.komet.preferences.KometPreferences;
import dev.ikm.komet.reasoner.ui.RunElkOwlReasonerIncrementalTask;
import dev.ikm.komet.reasoner.ui.RunElkOwlReasonerTask;
import dev.ikm.tinkar.common.alert.AlertStreams;
import dev.ikm.tinkar.common.service.PluggableService;
import dev.ikm.tinkar.common.service.TinkExecutor;
import dev.ikm.tinkar.reasoner.service.ReasonerService;
import dev.ikm.tinkar.terms.EntityFacade;
import dev.ikm.tinkar.terms.TinkarTerm;
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
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.ServiceLoader.Provider;
import java.util.concurrent.CancellationException;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Future;

public class ReasonerResultsNode extends ExplorationNodeAbstract {

	public static boolean reinferAllHierarchy = false;

	private static final Logger LOG = LoggerFactory.getLogger(ReasonerResultsNode.class);
	protected static final String STYLE_ID = "classification-results-node";
	protected static final String TITLE = "Reasoner Results";
	private final BorderPane contentPane = new BorderPane();
	private final HBox centerBox;

	private ReasonerService reasonerService;

	private ReasonerResultsController resultsController;

	public ReasonerResultsNode(ViewProperties viewProperties, KometPreferences nodePreferences) {
		super(viewProperties, nodePreferences);
		this.centerBox = new HBox(5, new Label("   Reasoner "));

		Platform.runLater(() -> {
			TopPanelFactory.TopPanelParts topPanelParts = TopPanelFactory.make(viewProperties,
					activityStreamKeyProperty, optionForActivityStreamKeyProperty, centerBox);
			this.contentPane.setTop(topPanelParts.topPanel());
			Platform.runLater(() -> {
				ArrayList<MenuItem> collectionMenuItems = new ArrayList<>();
				collectionMenuItems.add(new SeparatorMenuItem());

				List<ReasonerService> rss = PluggableService.load(ReasonerService.class).stream().map(Provider::get)
						.sorted(Comparator.comparing(ReasonerService::getName)).toList();
				for (ReasonerService rs : rss) {
					LOG.info("Reasoner service add: " + rs);
					MenuItem item = new MenuItem("Use " + rs.getName());
					item.setOnAction(x -> {
						this.reasonerService = rs;
						LOG.info("Reasoner service selected: " + rs.getName());
					});
					collectionMenuItems.add(item);
					if (this.reasonerService == null)
						this.reasonerService = rs;
				}
				if (this.reasonerService == null)
					throw new RuntimeException("No ReasonerService available");
				LOG.info("Default ReasonerService: " + this.reasonerService.getName());
				collectionMenuItems.add(new SeparatorMenuItem());

				{
					MenuItem item = new MenuItem("Run full reasoner");
					item.setOnAction(this::elkOwlReasoner);
					collectionMenuItems.add(item);
				}

				{
					MenuItem item = new MenuItem("Run incremental reasoner");
					item.setOnAction(this::elkOwlReasonerIncremental);
					collectionMenuItems.add(item);
				}

				{
					MenuItem item = new MenuItem("Run redo hierarchy reasoner");
					item.setOnAction(this::elkOwlReasonerRedo);
					collectionMenuItems.add(item);
				}

				ObservableList<MenuItem> topMenuItems = topPanelParts.viewPropertiesMenuButton().getItems();
				topMenuItems.addAll(collectionMenuItems);
			});
		});

		try {
			FXMLLoader loader = new FXMLLoader(
					getClass().getResource("/dev/ikm/komet/reasoner/ReasonerResultsInterface.fxml"));
			loader.load();
			this.resultsController = loader.getController();

			resultsController.setViewProperties(this.viewProperties, ActivityStreams.get(ActivityStreams.REASONER));
			contentPane.setCenter(loader.getRoot());
		} catch (IOException e) {
			AlertStreams.dispatchToRoot(e);
		}

	}

	private void elkOwlReasonerRedo(ActionEvent actionEvent) {
		reinferAllHierarchy = true;
		fullReasoner();
	}

	private void elkOwlReasoner(ActionEvent actionEvent) {
		reinferAllHierarchy = false;
		fullReasoner();
	}

	private void fullReasoner() {

		TinkExecutor.threadPool().execute(() -> {
			// TODO use a factory for the service and then create here
			reasonerService.init(getViewProperties().calculator(), TinkarTerm.EL_PLUS_PLUS_STATED_AXIOMS_PATTERN,
					TinkarTerm.EL_PLUS_PLUS_INFERRED_AXIOMS_PATTERN);
			RunElkOwlReasonerTask task = new RunElkOwlReasonerTask(reasonerService, resultsController::setResults);

			// publish event of task
			TaskWrapper<ReasonerService> javafxTask = TaskWrapper.make(task);
			Future reasonerFuture = ProgressHelper.progress(javafxTask, "Cancel Reasoner");
			int conceptCount = 0;
			try {
				reasonerFuture.get();
				conceptCount = reasonerService.getConceptCount();
			} catch (ExecutionException e) {
				AlertStreams.dispatchToRoot(e);
			} catch (InterruptedException ie) {
				LOG.info(ie.getMessage(), ie);
			} catch (CancellationException ce) {
				LOG.info(ce.getMessage(), ce);
				task.updateMessage("Cancelled full reasoner");
				task.cancel();
				ProgressHelper.cancel(javafxTask);
			}

			LOG.info("Concept count: " + conceptCount + " " + task.durationString());
		});
	}


	private void elkOwlReasonerIncremental(ActionEvent actionEvent) {
		reinferAllHierarchy = false;
		TinkExecutor.threadPool().execute(() -> {
			RunElkOwlReasonerIncrementalTask task = new RunElkOwlReasonerIncrementalTask(reasonerService,
					resultsController::setResults);
			// publish event of task
			TaskWrapper<ReasonerService> javafxTask = TaskWrapper.make(task);
			Future reasonerFuture = ProgressHelper.progress(javafxTask, "Cancel Reasoner");
			int conceptCount = 0;
			try {
				reasonerFuture.get();
				conceptCount = reasonerService.getConceptCount();
			} catch (ExecutionException e) {
				AlertStreams.dispatchToRoot(e);
			} catch (InterruptedException ie) {
				LOG.info(ie.getMessage(), ie);
				task.updateMessage(ie.getMessage());
			} catch (CancellationException ce) {
				LOG.info(ce.getMessage(), ce);
				task.updateMessage("Cancelled incremental reasoner");
				task.cancel();
				ProgressHelper.cancel(javafxTask);
			}

			LOG.info("Concept count: " + conceptCount + " " + task.durationString());
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

	public ReasonerResultsController getResultsController() {
		return resultsController;
	}
}