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
package dev.ikm.komet.framework.panel;

import static dev.ikm.komet.framework.StyleClasses.COMPONENT_VERSION_BORDER_PANEL;
import static dev.ikm.komet.framework.StyleClasses.COMPONENT_VERSION_PANEL;
import static dev.ikm.komet.framework.StyleClasses.EDIT_COMPONENT_BUTTON;
import static dev.ikm.komet.framework.StyleClasses.STAMP_LABEL;

import java.util.ArrayList;
import java.util.concurrent.Future;

import org.controlsfx.control.action.Action;
import org.controlsfx.control.action.ActionUtils;
import org.eclipse.collections.api.factory.Lists;
import org.eclipse.collections.api.list.ImmutableList;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import dev.ikm.komet.framework.EditedConceptTracker;
import dev.ikm.komet.framework.PseudoClasses;
import dev.ikm.komet.framework.graphics.Icon;
import dev.ikm.komet.framework.observable.ObservableConceptVersion;
import dev.ikm.komet.framework.observable.ObservablePatternVersion;
import dev.ikm.komet.framework.observable.ObservableSemanticVersion;
import dev.ikm.komet.framework.observable.ObservableStampVersion;
import dev.ikm.komet.framework.observable.ObservableVersion;
import dev.ikm.komet.framework.performance.Measures;
import dev.ikm.komet.framework.performance.Topic;
import dev.ikm.komet.framework.performance.impl.ObservationRecord;
import dev.ikm.komet.framework.rulebase.Consequence;
import dev.ikm.komet.framework.rulebase.ConsequenceAction;
import dev.ikm.komet.framework.rulebase.ConsequenceMenu;
import dev.ikm.komet.framework.rulebase.RuleService;
import dev.ikm.komet.framework.view.ViewProperties;
import dev.ikm.tinkar.common.alert.AlertObject;
import dev.ikm.tinkar.common.alert.AlertStreams;
import dev.ikm.tinkar.common.service.PrimitiveData;
import dev.ikm.tinkar.common.service.TinkExecutor;
import dev.ikm.tinkar.common.util.text.NaturalOrder;
import dev.ikm.tinkar.common.util.time.DateTimeUtil;
import dev.ikm.tinkar.coordinate.Coordinates;
import dev.ikm.tinkar.coordinate.stamp.calculator.Latest;
import dev.ikm.tinkar.entity.EntityVersion;
import dev.ikm.tinkar.entity.SemanticEntityVersion;
import dev.ikm.tinkar.entity.SemanticVersionRecord;
import dev.ikm.tinkar.entity.StampEntity;
import dev.ikm.tinkar.entity.transaction.CancelTransactionTask;
import dev.ikm.tinkar.entity.transaction.CancelVersionTask;
import dev.ikm.tinkar.entity.transaction.CommitTransactionTask;
import dev.ikm.tinkar.entity.transaction.CommitVersionTask;
import dev.ikm.tinkar.entity.transaction.Transaction;
import javafx.application.Platform;
import javafx.geometry.Pos;
import javafx.scene.Node;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.MenuButton;
import javafx.scene.control.TitledPane;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.HBox;

public abstract class ComponentVersionIsFinalPanel<OV extends ObservableVersion> {
	private static final Logger LOG = LoggerFactory.getLogger(ComponentVersionIsFinalPanel.class);
	protected final BorderPane versionDetailsPane = new BorderPane();
	protected final TitledPane collapsiblePane = new TitledPane("version", versionDetailsPane);
	private final OV observableVersion;
	private final ViewProperties viewProperties;

	public ComponentVersionIsFinalPanel(OV observableVersion, ViewProperties viewProperties) {
		this.observableVersion = observableVersion;
		this.viewProperties = viewProperties;
		Node versionNode = makeCenterNode(observableVersion, viewProperties);
		if (versionNode != null) {
			BorderPane.setAlignment(versionNode, Pos.TOP_LEFT);
			versionNode.pseudoClassStateChanged(PseudoClasses.INACTIVE_PSEUDO_CLASS, !observableVersion.active());
		}
		this.versionDetailsPane.getStyleClass().add(COMPONENT_VERSION_BORDER_PANEL.toString());
		this.versionDetailsPane.pseudoClassStateChanged(PseudoClasses.UNCOMMITTED_PSEUDO_CLASS,
				observableVersion.uncommitted());
		this.versionDetailsPane.setCenter(versionNode);
		// this.versionDetailsPane.setBottom(new StampPanel<V>(version,
		// viewProperties));
		StampEntity stampEntity = observableVersion.stamp();
		Label stampLabel = new Label(stampEntity.state() + " as of "
				+ DateTimeUtil.format(stampEntity.time(), DateTimeUtil.SEC_FORMATTER) + " on "
				+ viewProperties.calculator().getPreferredDescriptionTextWithFallbackOrNid(stampEntity.pathNid())
				+ " in "
				+ viewProperties.calculator().getPreferredDescriptionTextWithFallbackOrNid(stampEntity.moduleNid())
				+ " by "
				+ viewProperties.calculator().getPreferredDescriptionTextWithFallbackOrNid(stampEntity.authorNid()));
		stampLabel.getStyleClass().add(STAMP_LABEL.toString());
		this.collapsiblePane.setText("");
		this.collapsiblePane.pseudoClassStateChanged(PseudoClasses.INACTIVE_PSEUDO_CLASS, !observableVersion.active());
		this.collapsiblePane.pseudoClassStateChanged(PseudoClasses.UNCOMMITTED_PSEUDO_CLASS,
				observableVersion.uncommitted());
		this.collapsiblePane.getStyleClass().add(COMPONENT_VERSION_PANEL.toString());

		// Don't block on computation of actions for component version...
		TinkExecutor.threadPool().execute(() -> {
			ObservationRecord observation = new ObservationRecord(Topic.COMPONENT_FOCUSED,
					observableVersion.getVersionRecord(), Measures.present());
			ImmutableList<Consequence<?>> consequences = RuleService.get().execute("Knowledge base name",
					Lists.immutable.of(observation), viewProperties, Coordinates.Edit.Default());
			if (consequences.notEmpty()) {
				Platform.runLater(() -> {
					MenuButton menuButton = new MenuButton("", Icon.EDIT_PENCIL.makeIcon());
					menuButton.getStyleClass().add(EDIT_COMPONENT_BUTTON.toString());
					for (Consequence<?> consequence : consequences) {
						switch (consequence) {
						case ConsequenceMenu consequenceMenu -> {
							menuButton.getItems().add(consequenceMenu.generatedMenu());
						}
						case ConsequenceAction consequenceAction -> {
							if (consequenceAction.generatedAction() instanceof Action action) {
								menuButton.getItems().add(ActionUtils.createMenuItem(action));
							} else {
								LOG.error("Can't handle action of type: "
										+ consequenceAction.generatedAction().getClass().getName() + "\n\n"
										+ consequenceAction.generatedAction());
							}
						}
						default -> LOG.error("Can't handle consequence of type: " + consequence);
						}
					}
					menuButton.getItems().sort((o1, o2) -> NaturalOrder.compareStrings(o1.getText(), o2.getText()));
					ArrayList<Node> buttonList = new ArrayList<>(3);
					buttonList.add(menuButton);
					if (observableVersion.uncommitted()) {
						buttonList.add(newCancelComponentButton(observableVersion));
						if (observableVersion instanceof SemanticEntityVersion semanticEntityVersion) {
							Latest<EntityVersion> latestReferencedEntity = viewProperties.calculator()
									.latest(semanticEntityVersion.referencedComponentNid());
							if (latestReferencedEntity.isPresentAnd(entityVersion -> entityVersion.committed())) {
								buttonList.add(newCommitVersionButton(observableVersion));
							}
						} else {
							buttonList.add(newCommitVersionButton(observableVersion));
						}
						buttonList.add(newCancelTransactionButton(observableVersion));
						buttonList.add(newCommitTransactionButton(observableVersion));
					}
					if (buttonList.size() == 1) {
						stampLabel.setGraphic(buttonList.get(0));
					} else {
						HBox buttonsBox = new HBox();
						buttonsBox.getChildren().addAll(buttonList);
						stampLabel.setGraphic(buttonsBox);
					}
				});
			}
		});
		this.collapsiblePane.setGraphic(stampLabel);
	}

	protected abstract Node makeCenterNode(OV version, ViewProperties viewProperties);

	private Button newCancelComponentButton(OV version) {
		Button button = new Button("cancel version");
		button.setOnAction(event -> {
			Transaction.forVersion(version).ifPresentOrElse(transaction -> {
				CancelVersionTask cancelVersionTask = switch (version) {
				case ObservableConceptVersion observableConceptVersion ->
					new CancelVersionTask(observableConceptVersion.getVersionRecord());
				case ObservablePatternVersion observablePatternVersion ->
					new CancelVersionTask(observablePatternVersion.getVersionRecord());
				case ObservableSemanticVersion observableSemanticVersion ->
					new CancelVersionTask(observableSemanticVersion.getVersionRecord());
				case ObservableStampVersion observableStampVersion ->
					new CancelVersionTask(observableStampVersion.getVersionRecord());
				default -> throw new IllegalStateException("Unexpected value: " + version);
				};
				Future<Void> future = TinkExecutor.threadPool().submit(cancelVersionTask);
				TinkExecutor.threadPool().execute(() -> {
					try {
						future.get();
					} catch (Exception e) {
						AlertStreams.getRoot().dispatch(AlertObject.makeError(e));
					}
				});
			}, () -> {
				createSurrogateTransactionToCancel(version);
			});
		});
		return button;
	}

	private Button newCommitVersionButton(OV version) {
		Button button = new Button("commit version");
		button.setOnAction(event -> {
			Transaction.forVersion(version).ifPresentOrElse(transaction -> {
				CommitVersionTask commitVersionTask = switch (version) {
				case ObservableConceptVersion observableConceptVersion ->
					new CommitVersionTask(observableConceptVersion.getVersionRecord());
				case ObservablePatternVersion observablePatternVersion ->
					new CommitVersionTask(observablePatternVersion.getVersionRecord());
				case ObservableSemanticVersion observableSemanticVersion ->
					new CommitVersionTask(observableSemanticVersion.getVersionRecord());
				case ObservableStampVersion observableStampVersion ->
					new CommitVersionTask(observableStampVersion.getVersionRecord());
				default -> throw new IllegalStateException("Unexpected value: " + version);
				};
				Future<Void> future = TinkExecutor.threadPool().submit(commitVersionTask);
				TinkExecutor.threadPool().execute(() -> {
					try {
						future.get();
					} catch (Exception e) {
						AlertStreams.getRoot().dispatch(AlertObject.makeError(e));
					}
				});
			}, () -> {
				surrogateTransactionForCommit(version);
			});
		});
		return button;
	}

	private static void surrogateTransactionForCommit(ObservableVersion version) {
		LOG.warn("No transaction for version: " + version + ". Will create surrogate for commit. ");
		Transaction transaction = Transaction.make("Surrogate to commit missing transaction");
		transaction.addComponent(version.entity());
		commitTransactionTask(transaction);
	}

	private Button newCancelTransactionButton(OV version) {
		Button button = new Button("cancel transaction");
		button.setOnAction(event -> {
			Transaction.forVersion(version).ifPresentOrElse(transaction -> {
				cancelTransactionTask(transaction);
			}, () -> {
				createSurrogateTransactionToCancel(version);
			});
		});
		return button;
	}

	private static void cancelTransactionTask(Transaction transaction) {
		CancelTransactionTask cancelTransactionTask = new CancelTransactionTask(transaction);
		Future<Void> future = TinkExecutor.threadPool().submit(cancelTransactionTask);
		TinkExecutor.threadPool().execute(() -> {
			try {
				future.get();
			} catch (Exception e) {
				AlertStreams.getRoot().dispatch(AlertObject.makeError(e));
			}
		});
	}

	private static void createSurrogateTransactionToCancel(ObservableVersion version) {
		LOG.warn("No transaction for version: " + version + ". Will create surrogate. ");
		Transaction transaction = Transaction.make("Surrogate to cancel missing transaction");
		transaction.addComponent(version.entity());
		cancelTransactionTask(transaction);
	}

	private Button newCommitTransactionButton(OV version) {
		Button button = new Button("commit transaction");
		button.setOnAction(event -> {
			Transaction.forVersion(version).ifPresentOrElse(transaction -> {
				commitTransactionTask(transaction);
			}, () -> {
				surrogateTransactionForCommit(version);
			});
		});
		return button;
	}

	private static void commitTransactionTask(Transaction transaction) {
		CommitTransactionTask commitTransactionTask = new CommitTransactionTask(transaction);
		Future<Void> future = TinkExecutor.threadPool().submit(commitTransactionTask);
		TinkExecutor.threadPool().execute(() -> {
			try {
				future.get();
			} catch (Exception e) {
				AlertStreams.getRoot().dispatch(AlertObject.makeError(e));
			}
		});
	}

	public TitledPane getVersionDetailsPane() {
		return collapsiblePane;
	}
}
