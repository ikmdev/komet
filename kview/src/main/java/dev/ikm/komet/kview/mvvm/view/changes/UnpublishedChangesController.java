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
package dev.ikm.komet.kview.mvvm.view.changes;

import dev.ikm.komet.framework.observable.read.NavigationReads;
import dev.ikm.komet.framework.view.ObservableEditCoordinate;
import dev.ikm.komet.framework.view.ViewProperties;
import dev.ikm.komet.kview.events.MakeKLWindowEvent;
import dev.ikm.komet.kview.events.genediting.MakeGenEditingWindowEvent;
import dev.ikm.komet.kview.mvvm.model.ViewCoordinateHelper;
import dev.ikm.komet.layout.editor.StandardEditorWindows;
import dev.ikm.komet.terms.KometConcept;
import dev.ikm.tinkar.common.alert.AlertObject;
import dev.ikm.tinkar.common.alert.AlertStreams;
import dev.ikm.tinkar.common.flow.FlowSubscriber;
import dev.ikm.tinkar.common.service.TinkExecutor;
import dev.ikm.tinkar.coordinate.view.calculator.ViewCalculator;
import dev.ikm.tinkar.entity.ConceptEntity;
import dev.ikm.tinkar.entity.ConceptVersionRecord;
import dev.ikm.tinkar.entity.Entity;
import dev.ikm.tinkar.entity.EntityHandle;
import dev.ikm.tinkar.entity.EntityVersion;
import dev.ikm.tinkar.entity.PatternVersionRecord;
import dev.ikm.tinkar.entity.SemanticVersionRecord;
import dev.ikm.tinkar.entity.StampEntity;
import dev.ikm.tinkar.entity.transaction.CancelTransactionTask;
import dev.ikm.tinkar.entity.transaction.CancelVersionTask;
import dev.ikm.tinkar.entity.transaction.CommitTransactionTask;
import dev.ikm.tinkar.entity.transaction.CommitVersionTask;
import dev.ikm.tinkar.entity.transaction.Transaction;
import dev.ikm.tinkar.events.CommitEvent;
import dev.ikm.tinkar.events.EvtBus;
import dev.ikm.tinkar.events.EvtBusFactory;
import dev.ikm.tinkar.events.FrameworkTopics;
import dev.ikm.tinkar.events.Subscriber;
import dev.ikm.tinkar.terms.ConceptFacade;
import dev.ikm.tinkar.terms.EntityFacade;
import dev.ikm.tinkar.terms.KernelPattern;
import javafx.application.Platform;
import javafx.beans.binding.Bindings;
import javafx.beans.binding.BooleanBinding;
import javafx.beans.property.BooleanProperty;
import javafx.beans.property.ReadOnlyIntegerProperty;
import javafx.beans.property.ReadOnlyIntegerWrapper;
import javafx.beans.property.SimpleBooleanProperty;
import javafx.css.PseudoClass;
import javafx.fxml.FXML;
import javafx.geometry.Side;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.ButtonType;
import javafx.scene.control.ComboBox;
import javafx.scene.control.ContextMenu;
import javafx.scene.control.Label;
import javafx.scene.control.ListCell;
import javafx.scene.control.MenuItem;
import javafx.scene.control.SeparatorMenuItem;
import javafx.scene.control.TextField;
import javafx.scene.control.Tooltip;
import javafx.scene.control.TreeCell;
import javafx.scene.control.TreeItem;
import javafx.scene.control.TreeView;
import javafx.scene.input.MouseButton;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.Region;
import javafx.scene.layout.VBox;
import javafx.util.StringConverter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Comparator;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.Callable;
import java.util.concurrent.atomic.AtomicBoolean;

/**
 * The journal sidebar's unpublished-changes tray: every component with a version saved in an
 * open {@link Transaction}, grouped under the concept or pattern it belongs to, with a footer
 * that names the module and path to publish to and publishes (commits) or discards (cancels)
 * them all. A group's or a row's menu publishes or discards just that; a double-click opens the
 * component in its window.
 * <p>
 * The tree follows the store: it is rebuilt when any entity is written and when any transaction
 * commits, and again each time the tray slides out ({@link #refresh()}).
 */
public class UnpublishedChangesController {

    private static final Logger LOG = LoggerFactory.getLogger(UnpublishedChangesController.class);

    private static final PseudoClass GROUP_ROW = PseudoClass.getPseudoClass("group-row");
    private static final PseudoClass CHANGE_ROW = PseudoClass.getPseudoClass("change-row");

    @FXML
    private Label countLabel;

    @FXML
    private TreeView<UnpublishedRow> changesTreeView;

    @FXML
    private VBox emptyStateBox;

    @FXML
    private ComboBox<ConceptEntity> moduleComboBox;

    @FXML
    private ComboBox<ConceptEntity> pathComboBox;

    @FXML
    private TextField noteTextField;

    @FXML
    private Button discardAllButton;

    @FXML
    private Button publishAllButton;

    private final ViewProperties viewProperties;
    private final UUID journalTopic;
    private final EvtBus eventBus = EvtBusFactory.getDefaultEvtBus();

    private final ReadOnlyIntegerWrapper changeCount = new ReadOnlyIntegerWrapper(this, "changeCount", 0);
    private final ReadOnlyIntegerWrapper groupCount = new ReadOnlyIntegerWrapper(this, "groupCount", 0);

    /** True while a publish or discard runs; the footer and row actions wait for it. */
    private final BooleanProperty busy = new SimpleBooleanProperty(this, "busy", false);

    /** Coalesces the store's notifications into one refresh per FX pulse. */
    private final AtomicBoolean refreshQueued = new AtomicBoolean();

    /** Held by this field: the entity provider keeps its subscribers by weak reference. */
    private final FlowSubscriber<Long> entityChangeSubscriber = new FlowSubscriber<>(_ -> scheduleRefresh());

    private final Subscriber<CommitEvent> commitSubscriber = _ -> scheduleRefresh();

    /** The changes shown, flat; the tree groups them. */
    private List<UnpublishedChange> changes = List.of();

    /** Groups the user unfolded; a rebuilt tree unfolds them again. Groups start folded. */
    private final Set<Long> expandedGroups = new HashSet<>();

    private final TreeItem<UnpublishedRow> treeRoot = new TreeItem<>();

    /**
     * @param viewProperties the journal's view, on which the rows' description texts are read
     * @param journalTopic   the journal's event topic, on which a double-clicked row opens its window
     */
    public UnpublishedChangesController(ViewProperties viewProperties, UUID journalTopic) {
        this.viewProperties = viewProperties;
        this.journalTopic = journalTopic;
    }

    @FXML
    private void initialize() {
        changesTreeView.setRoot(treeRoot);
        changesTreeView.setShowRoot(false);
        changesTreeView.setCellFactory(_ -> new UnpublishedRowCell());

        countLabel.textProperty().bind(Bindings.createStringBinding(() -> {
            int n = changeCount.get();
            int g = groupCount.get();
            if (n == 0) {
                return "0";
            }
            return n + " in " + g + (g == 1 ? " component" : " components");
        }, changeCount, groupCount));

        BooleanBinding nothingToPublish = changeCount.isEqualTo(0);
        emptyStateBox.visibleProperty().bind(nothingToPublish);
        emptyStateBox.managedProperty().bind(nothingToPublish);
        changesTreeView.visibleProperty().bind(nothingToPublish.not());
        changesTreeView.managedProperty().bind(nothingToPublish.not());

        BooleanBinding footerIdle = nothingToPublish.or(busy);
        noteTextField.disableProperty().bind(footerIdle);
        discardAllButton.disableProperty().bind(footerIdle);
        publishAllButton.disableProperty().bind(footerIdle);
        moduleComboBox.disableProperty().bind(busy);
        pathComboBox.disableProperty().bind(busy);

        setupPublishTo();

        Entity.provider().addSubscriberWithWeakReference(entityChangeSubscriber);
        eventBus.subscribe(FrameworkTopics.COMMIT_TOPIC, CommitEvent.class, commitSubscriber);
        refresh();
    }

    /** Stops following the store; called when the journal window shuts down. */
    public void shutdown() {
        Entity.provider().removeSubscriber(entityChangeSubscriber);
        eventBus.unsubscribe(FrameworkTopics.COMMIT_TOPIC, CommitEvent.class, commitSubscriber);
    }

    /** How many components have an unpublished version; the sidebar badge shows it. */
    public ReadOnlyIntegerProperty changeCountProperty() {
        return changeCount.getReadOnlyProperty();
    }

    // ---- Publish to: module and path ----------------------------------------------------------

    /**
     * The "Publish to" line: the modules and the leaf paths of the view, in two quiet combo boxes
     * that start on the journal's edit coordinate (its default module and path). What is chosen
     * applies to everything listed when it is published.
     */
    private void setupPublishTo() {
        StringConverter<ConceptEntity> byDescription = new StringConverter<>() {
            @Override
            public String toString(ConceptEntity concept) {
                return concept == null ? "" : UnpublishedChange.text(concept.nid(), "Unnamed", viewProperties.calculator());
            }

            @Override
            public ConceptEntity fromString(String string) {
                return null;
            }
        };
        for (ComboBox<ConceptEntity> comboBox : List.of(moduleComboBox, pathComboBox)) {
            comboBox.setConverter(byDescription);
            comboBox.setCellFactory(_ -> new ListCell<>() {
                @Override
                protected void updateItem(ConceptEntity concept, boolean empty) {
                    super.updateItem(concept, empty);
                    setText(empty || concept == null ? null : byDescription.toString(concept));
                }
            });
        }
        moduleComboBox.setTooltip(new Tooltip("Change the module to publish to"));
        pathComboBox.setTooltip(new Tooltip("Change the path to publish to"));

        ObservableEditCoordinate editCoordinate = viewProperties.nodeView().editCoordinate();
        ConceptFacade defaultModule = editCoordinate.defaultModuleProperty().get();
        ConceptFacade defaultPath = editCoordinate.defaultPathProperty().get();

        TinkExecutor.threadPool().execute(() -> {
            ViewCalculator navigation = ViewCoordinateHelper.createNavigationCalculatorWithPatternNidsLatest(
                    viewProperties, KernelPattern.STATED_NAVIGATION_PATTERN.nid());
            List<ConceptEntity> modules = sortedByName(NavigationReads.descendantsOf(navigation, KometConcept.MODULE));
            List<ConceptEntity> paths = sortedByName(NavigationReads.leafDescendantsOf(navigation, KometConcept.PATH));
            Platform.runLater(() -> {
                moduleComboBox.getItems().setAll(modules);
                pathComboBox.getItems().setAll(paths);
                select(moduleComboBox, defaultModule);
                select(pathComboBox, defaultPath);
            });
        });
    }

    private List<ConceptEntity> sortedByName(Collection<ConceptEntity> concepts) {
        List<ConceptEntity> sorted = new ArrayList<>(concepts);
        sorted.sort(Comparator.comparing(
                concept -> UnpublishedChange.text(concept.nid(), "", viewProperties.calculator()),
                String.CASE_INSENSITIVE_ORDER));
        return sorted;
    }

    /** Selects the item with the facade's nid; a default not among the items is added to them. */
    private static void select(ComboBox<ConceptEntity> comboBox, ConceptFacade facade) {
        if (facade == null) {
            return;
        }
        for (ConceptEntity item : comboBox.getItems()) {
            if (item.nid() == facade.nid()) {
                comboBox.getSelectionModel().select(item);
                return;
            }
        }
        Entity<?> entity = EntityHandle.get(facade.nid()).orNull();
        if (entity instanceof ConceptEntity<?> concept) {
            comboBox.getItems().add(concept);
            comboBox.getSelectionModel().select(concept);
        }
    }

    // ---- Refresh ------------------------------------------------------------------------------

    /** Rebuilds the tree from the open transactions, off the FX thread, and shows the result. */
    public void refresh() {
        TinkExecutor.threadPool().execute(() -> {
            List<UnpublishedChange> collected = collect();
            List<UnpublishedGroup> groups = UnpublishedGroup.groupsOf(collected, viewProperties.calculator());
            Platform.runLater(() -> show(collected, groups));
        });
    }

    private void scheduleRefresh() {
        if (refreshQueued.compareAndSet(false, true)) {
            Platform.runLater(() -> {
                refreshQueued.set(false);
                refresh();
            });
        }
    }

    /**
     * The components with an uncommitted version in any open transaction, one row each. A
     * transaction's stamps are components of it too and are not changes; a component whose
     * version left the transaction (withdrawn, or published on its own) is not either.
     */
    private List<UnpublishedChange> collect() {
        Map<Long, UnpublishedChange> byNid = new LinkedHashMap<>();
        for (Transaction transaction : Transaction.activeTransactions()) {
            transaction.forEachComponentInTransaction(nid -> {
                if (byNid.containsKey(nid)) {
                    return;
                }
                Entity<?> entity = EntityHandle.get(nid).orNull();
                if (entity == null || entity instanceof StampEntity<?> || !entity.uncommitted()) {
                    return;
                }
                byNid.put(nid, UnpublishedChange.of(entity, viewProperties.calculator()));
            });
        }
        return new ArrayList<>(byNid.values());
    }

    private void show(List<UnpublishedChange> collected, List<UnpublishedGroup> groups) {
        changes = collected;
        List<TreeItem<UnpublishedRow>> items = new ArrayList<>();
        for (UnpublishedGroup group : groups) {
            TreeItem<UnpublishedRow> item = new TreeItem<>(group);
            for (UnpublishedChange change : group.semanticChanges()) {
                item.getChildren().add(new TreeItem<>(change));
            }
            item.setExpanded(expandedGroups.contains(group.nid()));
            item.expandedProperty().subscribe(expanded -> {
                if (expanded) {
                    expandedGroups.add(group.nid());
                } else {
                    expandedGroups.remove(group.nid());
                }
            });
            items.add(item);
        }
        treeRoot.getChildren().setAll(items);
        changeCount.set(collected.size());
        groupCount.set(groups.size());
    }

    // ---- Publish all / Discard all ------------------------------------------------------------

    @FXML
    private void publishAll() {
        String note = noteTextField.getText() == null ? "" : noteTextField.getText().strip();
        List<Callable<?>> tasks = new ArrayList<>();
        tasks.add(restampTask(changes));
        for (Transaction transaction : transactionsHoldingAnyOf(nidsOf(changes))) {
            if (!note.isEmpty()) {
                transaction.setTransactionName(note);
            }
            tasks.add(new CommitTransactionTask(transaction));
        }
        runTasks(tasks);
    }

    @FXML
    private void discardAll() {
        int count = changes.size();
        Alert confirm = new Alert(Alert.AlertType.CONFIRMATION,
                "Discard " + count + (count == 1 ? " unpublished change" : " unpublished changes")
                        + "? The edits are lost and cannot be recovered.",
                ButtonType.CANCEL, ButtonType.OK);
        confirm.setTitle("Discard all changes");
        confirm.setHeaderText(null);
        confirm.initOwner(changesTreeView.getScene().getWindow());
        if (confirm.showAndWait().orElse(ButtonType.CANCEL) != ButtonType.OK) {
            return;
        }
        List<Callable<?>> tasks = new ArrayList<>();
        for (Transaction transaction : transactionsHoldingAnyOf(nidsOf(changes))) {
            tasks.add(new CancelTransactionTask(transaction));
        }
        runTasks(tasks);
    }

    private static Set<Long> nidsOf(Collection<UnpublishedChange> changes) {
        Set<Long> nids = new HashSet<>();
        changes.forEach(change -> nids.add(change.nid()));
        return nids;
    }

    /**
     * The open transactions holding any of the components. A transaction with none of them (one
     * a window opened and saved nothing in yet) is left to its window.
     */
    private static List<Transaction> transactionsHoldingAnyOf(Set<Long> nids) {
        List<Transaction> transactions = new ArrayList<>();
        for (Transaction transaction : Transaction.activeTransactions()) {
            if (holdsAnyOf(transaction, nids)) {
                transactions.add(transaction);
            }
        }
        return transactions;
    }

    private static boolean holdsAnyOf(Transaction transaction, Set<Long> nids) {
        boolean[] holds = {false};
        transaction.forEachComponentInTransaction(nid -> holds[0] |= nids.contains(nid));
        return holds[0];
    }

    /**
     * Runs the tasks in order on the shared pool, then rebuilds the tree. The store's own
     * notifications rebuild it too; this one also clears the note and releases the footer.
     */
    private void runTasks(List<? extends Callable<?>> tasks) {
        busy.set(true);
        TinkExecutor.threadPool().execute(() -> {
            try {
                for (Callable<?> task : tasks) {
                    task.call();
                }
            } catch (Exception e) {
                // The store or a transaction refused the ending: report it, and show what is left.
                LOG.error("Ending unpublished changes failed", e);
                AlertStreams.getRoot().dispatch(AlertObject.makeError(e));
            }
            Platform.runLater(() -> {
                busy.set(false);
                noteTextField.clear();
                refresh();
            });
        });
    }

    // ---- Re-stamping onto the chosen module and path ------------------------------------------

    /**
     * A task that moves every uncommitted version of the changes onto the module and path chosen
     * in the footer, where it is on another: the version is rewritten under a new uncommitted
     * stamp of its own transaction, and the store gives up the version under the old stamp in
     * the same write ({@link dev.ikm.tinkar.entity.EntityService#putEntityReplacingVersion}).
     * The commit that follows finalizes the new stamps with the rest.
     */
    private Callable<Void> restampTask(List<UnpublishedChange> toPublish) {
        ConceptEntity module = moduleComboBox.getValue();
        ConceptEntity path = pathComboBox.getValue();
        if (module == null || path == null) {
            return () -> null;
        }
        long moduleNid = module.nid();
        long pathNid = path.nid();
        List<UnpublishedChange> snapshot = List.copyOf(toPublish);
        return () -> {
            for (UnpublishedChange change : snapshot) {
                restamp(change.nid(), moduleNid, pathNid);
            }
            return null;
        };
    }

    private static void restamp(long nid, long moduleNid, long pathNid) {
        Entity<?> entity = EntityHandle.get(nid).orNull();
        if (entity == null) {
            return;
        }
        for (EntityVersion version : entity.versions()) {
            if (!version.uncommitted()) {
                continue;
            }
            StampEntity<?> stamp = version.stamp();
            if (stamp.moduleNid() == moduleNid && stamp.pathNid() == pathNid) {
                continue;
            }
            Transaction transaction = Transaction.forVersion(version).orElse(null);
            if (transaction == null) {
                // Not in an open transaction: not this panel's to move.
                continue;
            }
            StampEntity<?> newStamp = transaction.getStamp(version.state(), stamp.authorNid(), moduleNid, pathNid);
            Entity<?> analogue = switch (version) {
                case ConceptVersionRecord concept ->
                        concept.chronology().without(concept).with(concept.withStampNid(newStamp.nid())).build();
                case SemanticVersionRecord semantic ->
                        semantic.chronology().without(semantic).with(semantic.withStampNid(newStamp.nid())).build();
                case PatternVersionRecord pattern ->
                        pattern.chronology().without(pattern).with(pattern.withStampNid(newStamp.nid())).build();
                default -> throw new IllegalStateException("Not a concept, semantic or pattern version: " + version);
            };
            Entity.provider().putEntityReplacingVersion(analogue, stamp.nid());
        }
    }

    // ---- One row, one group -------------------------------------------------------------------

    /**
     * The tasks that end some changes apart from the rest: their transactions whole when those
     * hold nothing else listed, else each semantic's or pattern's uncommitted version on its own
     * ({@link CommitVersionTask}, {@link CancelVersionTask}). A concept's version cannot leave a
     * shared transaction; that branch throws. It is unreachable today: a window's transaction only
     * holds versions rooted at the window's component, so a transaction never spans two headings.
     */
    private List<Callable<?>> endingsOf(Collection<UnpublishedChange> selection, boolean publish) {
        Set<Long> selected = nidsOf(selection);
        Set<Long> others = nidsOf(changes);
        others.removeAll(selected);
        List<Transaction> transactions = transactionsHoldingAnyOf(selected);
        List<Callable<?>> tasks = new ArrayList<>();
        if (transactions.stream().noneMatch(transaction -> holdsAnyOf(transaction, others))) {
            for (Transaction transaction : transactions) {
                tasks.add(publish ? new CommitTransactionTask(transaction) : new CancelTransactionTask(transaction));
            }
            return tasks;
        }
        for (UnpublishedChange change : selection) {
            Entity<?> entity = EntityHandle.get(change.nid()).expectEntity();
            for (EntityVersion version : entity.versions()) {
                if (!version.uncommitted()) {
                    continue;
                }
                tasks.add(switch (version) {
                    case SemanticVersionRecord semantic ->
                            publish ? new CommitVersionTask(semantic) : new CancelVersionTask(semantic);
                    case PatternVersionRecord pattern ->
                            publish ? new CommitVersionTask(pattern) : new CancelVersionTask(pattern);
                    default -> throw new IllegalStateException("A concept's version cannot leave its transaction: " + version);
                });
            }
        }
        return tasks;
    }

    private void publish(Collection<UnpublishedChange> selection) {
        List<Callable<?>> tasks = new ArrayList<>();
        tasks.add(restampTask(new ArrayList<>(selection)));
        tasks.addAll(endingsOf(selection, true));
        runTasks(tasks);
    }

    private void discard(Collection<UnpublishedChange> selection) {
        runTasks(endingsOf(selection, false));
    }

    private void open(UnpublishedRow row) {
        EntityFacade component = switch (row) {
            case UnpublishedChange change -> change.entity();
            case UnpublishedGroup group -> group.component();
        };
        if (component == null) {
            return;
        }
        UnpublishedChange.Type type = switch (row) {
            case UnpublishedChange change -> change.type();
            case UnpublishedGroup group -> group.type();
        };
        switch (type) {
            case CONCEPT -> eventBus.publish(journalTopic, new MakeKLWindowEvent(this,
                    MakeKLWindowEvent.OPEN_STANDARD_WINDOW, component, StandardEditorWindows.CONCEPT_WINDOW));
            case PATTERN -> eventBus.publish(journalTopic, new MakeKLWindowEvent(this,
                    MakeKLWindowEvent.OPEN_STANDARD_WINDOW, component, StandardEditorWindows.PATTERN_WINDOW));
            case SEMANTIC -> eventBus.publish(journalTopic, new MakeGenEditingWindowEvent(this,
                    MakeGenEditingWindowEvent.OPEN_GEN_EDIT, component, viewProperties));
        }
    }

    private static List<UnpublishedChange> changesOf(UnpublishedRow row) {
        return switch (row) {
            case UnpublishedChange change -> List.of(change);
            case UnpublishedGroup group -> group.changes();
        };
    }

    /***************************************************************************
     *                                                                         *
     * Supporting Classes                                                      *
     *                                                                         *
     **************************************************************************/

    /**
     * A tree row. A group: type badge, the component's title, its own kind tag or "Published",
     * the count of semantic changes under it (none for a component changed on its own), and the
     * menu. A change: a smaller badge, title over the
     * pattern name, the kind tag, and the menu.
     */
    private final class UnpublishedRowCell extends TreeCell<UnpublishedRow> {

        private final HBox row = new HBox();
        /** The group's fold arrow, drawn in the row so it centres with it; the skin's own is hidden. */
        private final Region caret = new Region();
        private final Label typeGlyph = new Label();
        private final Label title = new Label();
        private final Label subtitle = new Label();
        private final Label kindTag = new Label();
        private final Label count = new Label();
        private final Button menuButton = new Button();
        private final ContextMenu menu = new ContextMenu();
        private final MenuItem openItem = new MenuItem("Open");
        private final MenuItem publishItem = new MenuItem();
        private final MenuItem discardItem = new MenuItem();

        UnpublishedRowCell() {
            // A cell of no preferred width never widens the tree past its tray: the row takes the
            // tree's width and the labels ellipsize within it.
            setPrefWidth(0);
            // The skin's disclosure node sits at the top of a tall row; an empty, invisible one
            // takes no space, and the row draws its own fold arrow, centred with the rest of it.
            Region noDisclosure = new Region();
            noDisclosure.setVisible(false);
            noDisclosure.setManaged(false);
            setDisclosureNode(noDisclosure);
            row.getStyleClass().add("unpublished-row");
            caret.getStyleClass().add("group-caret");
            caret.setMinSize(7, 4);
            caret.setPrefSize(7, 4);
            caret.setMaxSize(7, 4);
            caret.setOnMouseClicked(event -> {
                if (event.getButton() == MouseButton.PRIMARY && getTreeItem() != null) {
                    getTreeItem().setExpanded(!getTreeItem().isExpanded());
                    event.consume();
                }
            });
            typeGlyph.getStyleClass().add("type-glyph");
            title.getStyleClass().add("change-title");
            subtitle.getStyleClass().add("change-subtitle");
            kindTag.getStyleClass().add("kind-tag");
            count.getStyleClass().add("group-count");

            VBox text = new VBox(title, subtitle);
            text.getStyleClass().add("change-text");
            HBox.setHgrow(text, Priority.ALWAYS);
            // Only the text gives way when the row is narrow; the chips keep their full width.
            text.setMinWidth(0);
            for (Region fixed : List.of(caret, typeGlyph, kindTag, count, menuButton)) {
                fixed.setMinWidth(Region.USE_PREF_SIZE);
            }
            title.setMinWidth(0);
            title.setMaxWidth(Double.MAX_VALUE);
            subtitle.setMinWidth(0);
            subtitle.setMaxWidth(Double.MAX_VALUE);

            Region kebab = new Region();
            kebab.getStyleClass().addAll("icon", "kebab-icon");
            menuButton.setGraphic(kebab);
            menuButton.getStyleClass().add("row-menu-button");
            menuButton.setFocusTraversable(false);
            menuButton.setOnAction(_ -> menu.show(menuButton, Side.BOTTOM, 0, 0));

            menu.getStyleClass().add("klcontext-menu");
            menu.getItems().addAll(openItem, new SeparatorMenuItem(), publishItem, discardItem);
            openItem.setOnAction(_ -> open(getItem()));
            publishItem.setOnAction(_ -> publish(changesOf(getItem())));
            discardItem.setOnAction(_ -> discard(changesOf(getItem())));

            row.getChildren().addAll(caret, typeGlyph, text, kindTag, count, menuButton);
            row.setOnMouseClicked(event -> {
                if (event.getButton() == MouseButton.PRIMARY && event.getClickCount() == 2 && getItem() != null) {
                    open(getItem());
                    event.consume();
                }
            });
        }

        @Override
        protected void updateItem(UnpublishedRow item, boolean empty) {
            super.updateItem(item, empty);
            pseudoClassStateChanged(GROUP_ROW, !empty && item instanceof UnpublishedGroup);
            pseudoClassStateChanged(CHANGE_ROW, !empty && item instanceof UnpublishedChange);
            if (empty || item == null) {
                setGraphic(null);
                return;
            }
            boolean folds = item instanceof UnpublishedGroup group && !group.semanticChanges().isEmpty();
            caret.setVisible(folds);
            caret.setManaged(folds);
            switch (item) {
                case UnpublishedGroup group -> {
                    showType(group.type());
                    title.setText(group.title());
                    subtitle.setVisible(false);
                    subtitle.setManaged(false);
                    if (group.isPublished()) {
                        showTag("Published", "published");
                    } else {
                        showTag(group.kind().label(), group.kind().name().toLowerCase());
                    }
                    int n = group.semanticChanges().size();
                    count.setText(Integer.toString(n));
                    count.setVisible(n > 0);
                    count.setManaged(n > 0);
                    publishItem.setText("Publish this component");
                    discardItem.setText("Discard this component");
                }
                case UnpublishedChange change -> {
                    showType(change.type());
                    title.setText(change.title());
                    subtitle.setText(change.subtitle());
                    subtitle.setVisible(true);
                    subtitle.setManaged(true);
                    showTag(change.kind().label(), change.kind().name().toLowerCase());
                    count.setVisible(false);
                    count.setManaged(false);
                    publishItem.setText("Publish this change");
                    discardItem.setText("Discard this change");
                }
            }
            publishItem.setDisable(busy.get());
            discardItem.setDisable(busy.get());
            setGraphic(row);
        }

        private void showType(UnpublishedChange.Type type) {
            typeGlyph.setText(type.glyph());
            typeGlyph.getStyleClass().removeIf(styleClass -> !styleClass.equals("type-glyph"));
            typeGlyph.getStyleClass().add(type.name().toLowerCase());
        }

        private void showTag(String text, String styleClass) {
            kindTag.setText(text);
            kindTag.getStyleClass().removeIf(existing -> !existing.equals("kind-tag"));
            kindTag.getStyleClass().add(styleClass);
        }
    }
}
