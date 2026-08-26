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

import java.net.URL;
import java.text.NumberFormat;
import java.util.ResourceBundle;
import java.util.Set;
import java.util.function.Function;

import dev.ikm.komet.framework.observable.collection.ObservableIntList;
import org.eclipse.collections.api.factory.Lists;
import org.eclipse.collections.api.list.MutableList;
import org.eclipse.collections.api.map.primitive.ImmutableIntObjectMap;

import dev.ikm.komet.framework.activity.ActivityStream;
import dev.ikm.komet.framework.view.ViewProperties;
import dev.ikm.tinkar.common.service.PrimitiveData;
import dev.ikm.tinkar.common.service.TinkExecutor;
import dev.ikm.tinkar.common.service.RemoteReasonerService;
import dev.ikm.tinkar.common.id.PublicIds;
import org.eclipse.collections.api.factory.primitive.IntLists;
import org.eclipse.collections.api.factory.primitive.IntSets;
import org.eclipse.collections.api.factory.Sets;
import org.eclipse.collections.api.list.primitive.ImmutableIntList;
import org.eclipse.collections.api.list.primitive.MutableIntList;
import dev.ikm.tinkar.reasoner.service.ClassifierResults;
import dev.ikm.tinkar.terms.EntityFacade;
import dev.ikm.tinkar.terms.EntityProxy;
import javafx.application.Platform;
import javafx.collections.ListChangeListener;
import javafx.fxml.FXML;
import javafx.scene.control.Accordion;
import javafx.scene.control.ContextMenu;
import javafx.scene.control.ListCell;
import javafx.scene.control.ListView;
import javafx.scene.control.SelectionMode;
import javafx.scene.control.TextArea;
import javafx.scene.control.TitledPane;
import javafx.scene.control.TreeItem;
import javafx.scene.control.TreeView;
import javafx.util.Callback;

public class ReasonerResultsController {

    @FXML
    private ResourceBundle resources;

    @FXML
    private URL location;

    @FXML
    private Accordion resultsAccordion;

    @FXML
    private TitledPane cyclesPane;

    @FXML
    private TreeView<StringWithOptionalConceptFacade> cyclesTree;

    @FXML
    private TitledPane conceptSetPane;

    @FXML
    private TitledPane orphansPane;

    @FXML
    private TitledPane equivalenciesPane;

    @FXML
    private TreeView<StringWithOptionalConceptFacade> equivalenciesTree;

    @FXML
    private TitledPane inferredChangesPane;

    @FXML
    private ListView<Integer> inferredChangesList;

    @FXML
    private ListView<Integer> orphanList;

    @FXML
    private TitledPane stampCoordinatePane;

    @FXML
    private TextArea stampTextArea;

    @FXML
    private TitledPane logicCoordinatePane;

    @FXML
    private TextArea logicTextArea;

    @FXML
    private TitledPane editCoordinatePane;

    @FXML
    private TextArea editTextArea;
    private ViewProperties viewProperties;
    private ActivityStream activityStream;

    @FXML
    void initialize() {
        assert resultsAccordion != null : "fx:id=\"resultsAccordion\" was not injected: check your FXML file 'ClassifierResultsInterface.fxml'.";
        assert cyclesPane != null : "fx:id=\"cyclesPane\" was not injected: check your FXML file 'ClassifierResultsInterface.fxml'.";
        assert cyclesTree != null : "fx:id=\"cyclesTree\" was not injected: check your FXML file 'ClassifierResultsInterface.fxml'.";
        assert orphansPane != null : "fx:id=\"orphansPane\" was not injected: check your FXML file 'ClassifierResultsInterface.fxml'.";
        assert equivalenciesPane != null : "fx:id=\"equivalenciesPane\" was not injected: check your FXML file 'ClassifierResultsInterface.fxml'.";
        assert equivalenciesTree != null : "fx:id=\"equivalenciesTree\" was not injected: check your FXML file 'ClassifierResultsInterface.fxml'.";
        assert inferredChangesPane != null : "fx:id=\"inferredChangesPane\" was not injected: check your FXML file 'ClassifierResultsInterface.fxml'.";
        assert inferredChangesList != null : "fx:id=\"inferredChangesList\" was not injected: check your FXML file 'ClassifierResultsInterface.fxml'.";
        assert orphanList != null : "fx:id=\"orphanList\" was not injected: check your FXML file 'ClassifierResultsInterface.fxml'.";
        assert stampCoordinatePane != null : "fx:id=\"stampCoordinatePane\" was not injected: check your FXML file 'ClassifierResultsInterface.fxml'.";
        assert stampTextArea != null : "fx:id=\"stampTextArea\" was not injected: check your FXML file 'ClassifierResultsInterface.fxml'.";
        assert logicCoordinatePane != null : "fx:id=\"logicCoordinatePane\" was not injected: check your FXML file 'ClassifierResultsInterface.fxml'.";
        assert logicTextArea != null : "fx:id=\"logicTextArea\" was not injected: check your FXML file 'ClassifierResultsInterface.fxml'.";
        assert editCoordinatePane != null : "fx:id=\"editCoordinatePane\" was not injected: check your FXML file 'ClassifierResultsInterface.fxml'.";
        assert editTextArea != null : "fx:id=\"editTextArea\" was not injected: check your FXML file 'ClassifierResultsInterface.fxml'.";

        inferredChangesList.setItems(new ObservableIntList());
        inferredChangesList.setCellFactory(conceptCellFactory());
        orphanList.setCellFactory(conceptCellFactory());
        orphanList.setItems(new ObservableIntList());
        orphanList.getSelectionModel().setSelectionMode(SelectionMode.MULTIPLE);
        inferredChangesList.getSelectionModel().getSelectedItems().addListener(this::onChanged);
        inferredChangesList.getSelectionModel().setSelectionMode(SelectionMode.MULTIPLE);
        orphanList.getSelectionModel().getSelectedItems().addListener(this::onChanged);
        orphanList.getSelectionModel().setSelectionMode(SelectionMode.MULTIPLE);
        equivalenciesTree.getSelectionModel().getSelectedItems().addListener(this::onTreeSelectionChanged);
        equivalenciesTree.getSelectionModel().setSelectionMode(SelectionMode.MULTIPLE);
        cyclesTree.getSelectionModel().getSelectedItems().addListener(this::onTreeSelectionChanged);
        cyclesTree.getSelectionModel().setSelectionMode(SelectionMode.MULTIPLE);

    }
    private void onTreeSelectionChanged(ListChangeListener.Change<? extends TreeItem<StringWithOptionalConceptFacade>> change) {
        if (!change.getList().isEmpty()) {
            MutableList<EntityFacade> changeList = Lists.mutable.ofInitialCapacity(change.getList().size());
            for (TreeItem<StringWithOptionalConceptFacade> treeItem: change.getList()) {
                treeItem.getValue().getOptionalConceptSpecification().ifPresent(conceptFacade -> {
                    if (conceptFacade instanceof EntityProxy.Concept conceptProxy) {
                        changeList.add(conceptProxy);
                    } else {
                        changeList.add(EntityProxy.Concept.make(conceptFacade));
                    }
                });
            }
            this.activityStream.dispatch(changeList.toImmutable());
        }
    }

    // create a context menu to compare and launch window
    public void setOnContextMenuForEquiv(Function<TreeView<StringWithOptionalConceptFacade>, ContextMenu> contextMenuConsumer) {
        equivalenciesTree.setContextMenu(contextMenuConsumer.apply(equivalenciesTree));
    }

    private void onChanged(ListChangeListener.Change<? extends Integer> change) {
        if (!change.getList().isEmpty()) {
            MutableList<EntityFacade> changeList = Lists.mutable.ofInitialCapacity(change.getList().size());
            for (Integer conceptNid: change.getList()) {
                changeList.add(EntityProxy.Concept.make(conceptNid));
            }
            this.activityStream.dispatch(changeList.toImmutable());
        }
    }

    private Callback<ListView<Integer>, ListCell<Integer>> conceptCellFactory() {
        return new Callback<>() {
            @Override
            public ListCell<Integer> call(ListView<Integer> param) {
                return new ListCell<>() {
                    @Override
                    protected void updateItem(final Integer item, boolean empty) {
                        super.updateItem(item, empty);
                        if (item == null || empty) {
                            setText("");
                            setGraphic(null);
                        } else {
                            setText(PrimitiveData.text(item));
                        }
                    }
                };
            }
        };
    }

    /**
     * Populates the panel from a classification that ran on a remote datastore.
     *
     * <p>A separate entry point rather than adapting the outcome into {@link ClassifierResults}:
     * that type is built from local nids and a full {@code ViewCoordinateRecord}, and a remote
     * run has neither. In particular the remote service sends the size of the classification
     * concept set but not its members — they number in the hundreds of thousands and this panel
     * only ever displays the count — so constructing a ClassifierResults would mean inventing a
     * list of the right length, which would be a lie to anything that later read it.
     *
     * <p>The concept sets that ARE sent arrive as public IDs and are resolved to local nids
     * here, so the same tree-building tasks as the local path can be reused.
     */
    public void setRemoteResults(RemoteReasonerService.RemoteReasonerOutcome outcome) {
        if (!Platform.isFxApplicationThread()) {
            Platform.runLater(() -> setRemoteResults(outcome));
            return;
        }
        if (outcome == null) {
            setResults(null);
            return;
        }

        // Same treatment as the local path: a count, then disabled and collapsed.
        this.conceptSetPane.setText(String.format("Concept set size: %,d",
                outcome.classifiedConceptCount()));
        this.conceptSetPane.setDisable(true);
        this.conceptSetPane.setExpanded(false);

        // Cycles are not reported by the remote service, so there is nothing to show rather
        // than nothing found — say so instead of implying a clean result.
        cyclesPane.setText(cyclesPane.getText() + ": not reported remotely");
        cyclesPane.setDisable(true);

        ImmutableIntList orphans = toNids(outcome.orphans());
        if (orphans.isEmpty()) {
            orphansPane.setText(orphansPane.getText() + ": none");
            orphansPane.setDisable(true);
        } else {
            orphansPane.setText(orphansPane.getText() + ": "
                    + NumberFormat.getInstance().format(orphans.size()));
            TinkExecutor.threadPool().submit(new PrepareConceptSetTask("Sorting list of orphans",
                    IntSets.immutable.ofAll(orphans), orphanList.getItems(), this.viewProperties));
        }

        Set<ImmutableIntList> equivalentSets = outcome.equivalentSets().stream()
                .map(this::toNids)
                .collect(java.util.stream.Collectors.toSet());
        if (equivalentSets.isEmpty()) {
            equivalenciesPane.setText(equivalenciesPane.getText() + ": none");
            equivalenciesPane.setDisable(true);
        } else {
            equivalenciesPane.setText(equivalenciesPane.getText() + ": "
                    + NumberFormat.getInstance().format(equivalentSets.size()));
            TinkExecutor.threadPool().submit(new PrepareClassifierEquivalenciesTask(
                    Sets.immutable.ofAll(equivalentSets), equivalenciesTree, this.viewProperties));
        }

        ImmutableIntList inferredChanges = toNids(outcome.conceptsWithInferredChanges());
        if (inferredChanges.isEmpty()) {
            inferredChangesPane.setText(inferredChangesPane.getText() + ": none");
            inferredChangesPane.setDisable(true);
        } else {
            inferredChangesPane.setText(inferredChangesPane.getText() + ": "
                    + NumberFormat.getInstance().format(inferredChanges.size()));
            TinkExecutor.threadPool().submit(new PrepareConceptSetTask(
                    "Sorting list of inferred changes", IntSets.immutable.ofAll(inferredChanges),
                    inferredChangesList.getItems(), this.viewProperties));
        }

        // Pre-rendered on the server: these describe the coordinate the remote run used, which
        // is not necessarily this client's.
        stampTextArea.setText(outcome.stampCoordinateText());
        logicTextArea.setText(outcome.logicCoordinateText());
        editTextArea.setText(outcome.editCoordinateText());

        // Advance the view to the remote commit so the newly written inferences are visible,
        // matching applyCommitTimeToView on the local path.
        if (outcome.commitTime() > 0 && viewProperties != null) {
            viewProperties.parentView().stampCoordinate().timeProperty().set(outcome.commitTime());
            viewProperties.nodeView().stampCoordinate().timeProperty().set(outcome.commitTime());
        }
    }

    /**
     * Resolves public IDs to this store's nids. In gRPC mode an unknown concept is fetched on
     * demand, so this can block — callers run it off the UI thread.
     */
    private ImmutableIntList toNids(java.util.List<java.util.List<java.util.UUID>> publicIds) {
        MutableIntList nids = IntLists.mutable.empty();
        for (java.util.List<java.util.UUID> uuids : publicIds) {
            if (uuids.isEmpty()) {
                continue;
            }
            nids.add(PrimitiveData.nid(PublicIds.of(uuids.toArray(new java.util.UUID[0]))));
        }
        return nids.toImmutable();
    }

    public void setResults(ClassifierResults classifierResults) {
        if (Platform.isFxApplicationThread()) {
            if (classifierResults == null) {
                this.conceptSetPane.setText("Concept set empty ");
                cyclesPane.setText(cyclesPane.getText() + ": null result");
                this.conceptSetPane.setExpanded(false);

                TreeItem<StringWithOptionalConceptFacade> root = new TreeItem<>(new StringWithOptionalConceptFacade("Cycles Root"));
                root.setExpanded(true);
                cyclesTree.setRoot(root);
                cyclesTree.setShowRoot(false);
                cyclesPane.setDisable(true);

                orphansPane.setText(orphansPane.getText() + ": null result");
                orphansPane.setDisable(true);

                equivalenciesPane.setText(equivalenciesPane.getText() + ": null result");
                equivalenciesPane.setDisable(true);

                inferredChangesPane.setText(inferredChangesPane.getText() + ": null result");
                inferredChangesPane.setDisable(true);

                stampTextArea.setText("null result");
                logicTextArea.setText("null result");
                editTextArea.setText("null result");

                return;
            }
            //applyCommitTimeToView(classifierResults);
            this.conceptSetPane.setText(String.format("Concept set size: %,d", classifierResults.getClassificationConceptSet().size()));
            this.conceptSetPane.setDisable(true);
            this.conceptSetPane.setExpanded(false);
            if (classifierResults.getCycles().notEmpty()) {
                ImmutableIntObjectMap<Set<int[]>> cycles = classifierResults.getCycles();
                cyclesPane.setText(cyclesPane.getText() + ": " + NumberFormat.getInstance().format(cycles.size()));
                TreeItem<StringWithOptionalConceptFacade> root = new TreeItem<>(new StringWithOptionalConceptFacade("Cycles Root"));
                root.setExpanded(true);
                cycles.forEachKeyValue((key, cycleSets) -> {
                    TreeItem<StringWithOptionalConceptFacade> conceptWithCycle = new TreeItem<>(new StringWithOptionalConceptFacade(
                            PrimitiveData.text(key), EntityProxy.Concept.make(key)));
                    root.getChildren().add(conceptWithCycle);
                    for (int[] cycleSet: cycleSets) {
                        TreeItem<StringWithOptionalConceptFacade> cycleSetTreeItem = new TreeItem<>(new StringWithOptionalConceptFacade(
                                "cycle elements"));
                        cycleSetTreeItem.setExpanded(true);
                        conceptWithCycle.getChildren().add(cycleSetTreeItem);
                        for (int nid: cycleSet) {
                            TreeItem<StringWithOptionalConceptFacade> conceptInCycle = new TreeItem<>(new StringWithOptionalConceptFacade(
                                    PrimitiveData.text(nid), EntityProxy.Concept.make(nid)));
                            cycleSetTreeItem.getChildren().add(conceptInCycle);
                        }
                    }
                });
                cyclesTree.setRoot(root);
                cyclesTree.setShowRoot(false);
            } else {
                cyclesPane.setText(cyclesPane.getText() + ": none");
                cyclesPane.setDisable(true);
            }

            if ( classifierResults.getOrphans().isEmpty()) {
                orphansPane.setText(orphansPane.getText() + ": none");
                orphansPane.setDisable(true);
            } else {
                orphansPane.setText(orphansPane.getText() + ": " + NumberFormat.getInstance().format(classifierResults.getOrphans().size()));
                TinkExecutor.threadPool().submit(new PrepareConceptSetTask("Sorting list of orphans", classifierResults.getOrphans(),
                        orphanList.getItems(), this.viewProperties));
            }

            if ( classifierResults.getEquivalentSets().isEmpty()) {
                equivalenciesPane.setText(equivalenciesPane.getText() + ": none");
                equivalenciesPane.setDisable(true);
            } else {
                equivalenciesPane.setText(equivalenciesPane.getText() + ": "  + NumberFormat.getInstance().format(classifierResults.getEquivalentSets().size()));
                TinkExecutor.threadPool().submit(new PrepareClassifierEquivalenciesTask(classifierResults.getEquivalentSets(), equivalenciesTree, this.viewProperties));
            }

            if ( classifierResults.getConceptsWithInferredChanges().isEmpty()) {
                inferredChangesPane.setText(inferredChangesPane.getText() + ": none");
                inferredChangesPane.setDisable(true);
            } else {
                inferredChangesPane.setText(inferredChangesPane.getText() + ": " +
                        NumberFormat.getInstance().format(classifierResults.getConceptsWithInferredChanges().size()));
                TinkExecutor.threadPool().submit(new PrepareConceptSetTask("Sorting list of inferred changes",
                        classifierResults.getConceptsWithInferredChanges(),
                        inferredChangesList.getItems(),
                        this.viewProperties));
            }

            stampTextArea.setText(classifierResults.getViewCoordinate().stampCoordinate().toUserString());
            logicTextArea.setText(classifierResults.getViewCoordinate().logicCoordinate().toUserString());
            editTextArea.setText(classifierResults.getViewCoordinate().editCoordinate().toUserString());
        } else {
            Platform.runLater(() -> setResults(classifierResults));
        }

    }

    private void applyCommitTimeToView(ClassifierResults classifierResults) {
        if (viewProperties == null) {
            return;
        }
        long commitTime = classifierResults.getViewCoordinate().stampCoordinate().stampPosition().time();
        viewProperties.parentView().stampCoordinate().timeProperty().set(commitTime);
        viewProperties.nodeView().stampCoordinate().timeProperty().set(commitTime);
    }

    public void setViewProperties(ViewProperties viewProperties, ActivityStream activityFeed) {
        this.viewProperties = viewProperties;
        this.activityStream = activityFeed;
    }
}
