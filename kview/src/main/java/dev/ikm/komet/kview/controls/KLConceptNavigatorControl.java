package dev.ikm.komet.kview.controls;

import dev.ikm.komet.kview.controls.skin.ConceptNavigatorHelper;
import dev.ikm.komet.kview.controls.skin.KLConceptNavigatorTreeViewSkin;
import dev.ikm.komet.navigator.graph.Navigator;
import dev.ikm.tinkar.common.service.TinkExecutor;
import dev.ikm.tinkar.entity.Entity;
import dev.ikm.tinkar.terms.ConceptFacade;
import javafx.animation.PauseTransition;
import javafx.application.Platform;
import javafx.beans.property.BooleanProperty;
import javafx.beans.property.DoubleProperty;
import javafx.beans.property.ObjectProperty;
import javafx.beans.property.SimpleBooleanProperty;
import javafx.beans.property.SimpleDoubleProperty;
import javafx.beans.property.SimpleObjectProperty;
import javafx.beans.property.SimpleStringProperty;
import javafx.beans.property.StringProperty;
import javafx.scene.control.SelectionMode;
import javafx.scene.control.Skin;
import javafx.scene.control.TreeView;
import javafx.util.Duration;

import java.util.Arrays;
import java.util.List;
import java.util.Random;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Future;
import java.util.function.Consumer;
import java.util.function.Function;

/**
 * <p>The KLConceptNavigatorControl control is a {@link TreeView} that provides a view on to a tree root
 * (of type {@link ConceptNavigatorTreeItem}), allowing traversing down into the children of a TreeItem, recursively
 * until a TreeItem has no children (that is, it is a leaf node in the tree).
 * </p>
 * <p>In the simplest case, ConceptNavigatorTreeItem instances may be created in memory, starting from a rootNid and
 * a {@link Navigator}, as such:
 * </p>
 * <pre><code>
 *
 * private List<ConceptNavigatorTreeItem> getChildren(int nid) {
 *     return getNavigator().getChildEdges(nid).stream()
 *             .map(edge -&gt; getConceptNavigatorTreeItem(edge.destinationNid(), nid))
 *             .toList();
 * }
 *
 * private ConceptNavigatorTreeItem getConceptNavigatorTreeItem(int nid, int parentNid) {
 *     ConceptNavigatorTreeItem conceptNavigatorTreeItem = new ConceptNavigatorTreeItem(getNavigator(), nid, parentNid);
 *     conceptNavigatorTreeItem.getChildren().addAll(getChildren(nid));
 *     return conceptNavigatorTreeItem;
 * }
 *
 * public void createNavigator(int rootNid) {
 *     ConceptNavigatorTreeItem root = getConceptNavigatorTreeItem(Entity.getFast(rootNid), -1);
 *     root.setExpanded(true);
 *
 *     KLConceptNavigatorControl conceptNavigator = new KLConceptNavigatorControl();
 *     conceptNavigator.setRoot(root);
 * }
 * </code></pre>
 *
 * <p>This approach works well for simple tree structures, or when the data is not
 * excessive (so that it can easily fit in memory).
 * </p>
 * <p>However, the current implementation takes into account that the tree data can be huge, so
 * the ConceptNavigatorTreeItem instances are created on-demand in a memory-efficient way, only when
 * the user requires them, which is when the treeItem gets expanded:
 * </p>
 * <pre><code>
 *
 * private List<ConceptNavigatorTreeItem> getChildren(int nid) {
 *     return getNavigator().getChildEdges(nid).stream()
 *             .map(edge -&gt; getConceptNavigatorTreeItem(edge.destinationNid(), nid))
 *             .toList();
 * }
 *
 * private ConceptNavigatorTreeItem getConceptNavigatorTreeItem(int nid, int parentNid) {
 *     ConceptNavigatorTreeItem conceptNavigatorTreeItem = new ConceptNavigatorTreeItem(getNavigator(), nid, parentNid);
 *     conceptNavigatorTreeItem.expandedProperty().subscribe((_, expanded) -&gt; {
 *             if (expanded && conceptNavigatorTreeItem.getChildren().isEmpty()) {
 *                 conceptNavigatorTreeItem.getChildren().addAll(getChildren(nid));
 *             }
 *         });
 *     return conceptNavigatorTreeItem;
 * }
 *
 * public void createNavigator(int rootNid) {
 *     ConceptNavigatorTreeItem root = getConceptNavigatorTreeItem(Entity.getFast(rootNid), -1);
 *     root.setExpanded(true);
 *
 *     KLConceptNavigatorControl conceptNavigator = new KLConceptNavigatorControl();
 *     conceptNavigator.setRoot(root);
 * }
 * </code></pre>
 * <p>The visuals of the KLConceptNavigatorControl are customized by replacing the default cell factory with a cell
 * factory is used to generate {@link KLConceptNavigatorTreeCell} instances, which are used to represent an item
 * in the TreeView.
 * </p>
 * <p>This TreeView implementation is customized with connecting lines are added between a
 * {@link ConceptNavigatorTreeItem} and all its ancestors, to illustrate the concept lineage. These lines become
 * highlighted when the concepts are long-hovered or selected.
 * </p>
 * <p>A floating or sticky header is added, on top of the cells.</p>
 */
public class KLConceptNavigatorControl extends TreeView<ConceptFacade> {

    /**
     * <p>Sets the maximum level of indentation expected in the dataset. This is required to create in advance
     * the pseudoClasses for all the connecting lines that are drawn per {@link KLConceptNavigatorTreeCell} based
     * on the indentation level of a given {@link ConceptNavigatorTreeItem}.
     * </p>
     * <p>If needed, it can be increased without any significant performance lost.</p>
     * @see ConceptNavigatorUtils#STYLE
     */
    public static final int MAX_LEVEL = 32;

    /**
     * Used to access internal methods of KLConceptNavigatorControl from
     * {@link dev.ikm.komet.kview.controls.skin.KLConceptNavigatorTreeViewSkin}.
     */
    static {
        ConceptNavigatorHelper.setConceptNavigatorAccessor(new ConceptNavigatorHelper.ConceptNavigatorAccessor() {

            @Override
            public Future<Boolean> fetchChildrenTask(KLConceptNavigatorControl treeView, ConceptNavigatorTreeItem item) {
                return treeView.fetchChildrenTask(item);
            }

            @Override
            public ConceptNavigatorTreeItem getConceptNavigatorTreeItem(KLConceptNavigatorControl treeView, int nid, int parentNid) {
                return treeView.getConceptNavigatorTreeItem(nid, parentNid);
            }
        });
    }

    private KLConceptNavigatorTreeViewSkin conceptNavigatorTreeViewSkin;

    /**
     * <p>Creates a {@link KLConceptNavigatorControl} instance, and sets by default a hidden root tree item, and a
     * multiple selection mode.
     * </p>
     * <p>{@link KLConceptNavigatorTreeCell} is set as the cell factory.
     * </p>
     * <p>Listeners are added to keep the highlighted {@link ConceptNavigatorTreeItem} in sync with
     * the current selected item.
     * </p>
     * @see KLConceptNavigatorTreeViewSkin#selectAllAncestors(ConceptNavigatorTreeItem)
     */
    public KLConceptNavigatorControl() {
        setShowRoot(false);
        getSelectionModel().setSelectionMode(SelectionMode.MULTIPLE);

        setCellFactory(p -> new KLConceptNavigatorTreeCell(this));
        expandedItemCountProperty().subscribe((o, n) -> {
            if (conceptNavigatorTreeViewSkin == null) {
                return;
            }
            // clean up
            conceptNavigatorTreeViewSkin.unhoverAllItems();
            conceptNavigatorTreeViewSkin.unselectAllItems();
            ConceptNavigatorTreeItem selectedItem = (ConceptNavigatorTreeItem) getSelectionModel().getSelectedItem();
            if (selectedItem != null) {
                conceptNavigatorTreeViewSkin.selectAllAncestors(selectedItem);
            }
        });
        getSelectionModel().selectedItemProperty().subscribe((o, n) -> {
            if (conceptNavigatorTreeViewSkin == null) {
                return;
            }
            conceptNavigatorTreeViewSkin.unhoverAllItems();
            if (o != null) {
                conceptNavigatorTreeViewSkin.unselectAllItems();
            }
            if (n != null) {
                conceptNavigatorTreeViewSkin.selectAllAncestors((ConceptNavigatorTreeItem) n);
            }
        });

        getStyleClass().add("concept-navigator-control");
        getStylesheets().add(getUserAgentStylesheet());
        getStylesheets().add(ConceptNavigatorUtils.STYLE);
    }

    /**
     * <p>String property that is used to define the text for the floating or sticky header.
     * </p>
     * <p>If null or not set, the header is not visible</p>
     */
    private final StringProperty headerProperty = new SimpleStringProperty(this, "header");
    public final StringProperty headerProperty() {
       return headerProperty;
    }
    public final String getHeader() {
       return headerProperty.get();
    }
    public final void setHeader(String value) {
        headerProperty.set(value);
    }

    /**
     * <p>This property sets a {@link Function} that can be used to define an {@link Consumer<ConceptFacade>} after
     * a given {@link CONTEXT_MENU_ACTION action}, so when one or more {@link ConceptFacade} items are
     * selected from the {@link KLConceptNavigatorControl}, the operation defined for such function can be performed for
     * each of them.
     * </p>
     * <p>For instance, after selecting a number of items, a drag and drop gesture would drag those items from
     * the control and drop them in the {@link KLWorkspace}.
     * </p>
     */
    private final ObjectProperty<Function<CONTEXT_MENU_ACTION, Consumer<ConceptFacade>>> onActionProperty = new SimpleObjectProperty<>(this, "onAction");
    public final ObjectProperty<Function<CONTEXT_MENU_ACTION, Consumer<ConceptFacade>>> onActionProperty() {
       return onActionProperty;
    }
    public final Function<CONTEXT_MENU_ACTION, Consumer<ConceptFacade>> getOnAction() {
       return onActionProperty.get();
    }
    public final void setOnAction(Function<CONTEXT_MENU_ACTION, Consumer<ConceptFacade>> value) {
        onActionProperty.set(value);
    }

    /**
     * <p>Double property that sets the milliseconds of activation or delay for highlighting a
     * {@link ConceptNavigatorTreeItem} and its ancestors, or for showing a tooltip, after "long" hovering over
     * such item.</p>
     * <p>The 'long-hover` event is defined as the sustained hovering over an item that lasts at least the
     * activation milliseconds.</p>
     * <p>The default value is set to 500 ms.</p>
     */
    private final DoubleProperty activationProperty = new SimpleDoubleProperty(this, "activation", 500);
    public final DoubleProperty activationProperty() {
       return activationProperty;
    }
    public final double getActivation() {
       return activationProperty.get();
    }
    public final void setActivation(double value) {
        activationProperty.set(value);
    }

    /**
     * <p>Object property that sets the {@link Navigator} that holds the dataset used to construct this
     * {@link KLConceptNavigatorControl}.</p>
     * <p>Only after a valid Navigator is set, the first {@link ConceptNavigatorTreeItem} is created
     * and set as root the treeView.</p>
     * <p>The root is hidden, so all its direct children are initially visible and collapsed. Only when
     * the user expands any of those items, the related children are created and added to the treeView,
     * and so on and so forth.
     * </p>
     */
    private final ObjectProperty<Navigator> navigatorProperty = new SimpleObjectProperty<>(this, "navigator") {
        @Override
        protected void invalidated() {
            if (get() != null) {
                ConceptNavigatorTreeItem first = getConceptNavigatorRoot().getFirst();
                setRoot(first);
                // debug
//                new Thread(() -> ConceptNavigatorUtils.getConceptNavigatorDepth(first.getValue().nid(), get())).start();
            }
        }
    };
    public final ObjectProperty<Navigator> navigatorProperty() {
       return navigatorProperty;
    }
    public final Navigator getNavigator() {
       return navigatorProperty.get();
    }
    public final void setNavigator(Navigator value) {
        navigatorProperty.set(value);
    }

    /**
     * <p>Boolean property that toggles the visibility of the tags that are applied to some concepts.
     * </p>
     */
    private final BooleanProperty showTagsProperty = new SimpleBooleanProperty(this, "showTags");
    public final BooleanProperty showTagsProperty() {
       return showTagsProperty;
    }
    public final boolean isShowTags() {
       return showTagsProperty.get();
    }
    public final void setShowTags(boolean value) {
        showTagsProperty.set(value);
    }

    /**
     * <p>This enum defines the possible actions that can be executed from the
     * different context menus that are shown for this control.
     * </p>
     * @see #onActionProperty()
     * @see SingleSelectionContextMenu
     * @see MultipleSelectionContextMenu
     */
    public enum CONTEXT_MENU_ACTION {
        /**
         * <p>For a single ConceptNavigatorTreeItem, when this action is fired, show a submenu with a menuItem
         * per related concept, with its own action.
         * </p>
         * @see ConceptNavigatorTreeItem#relatedConceptsProperty()
         */
        SHOW_RELATED_CONCEPTS,

        /**
         * <p>For a single ConceptNavigatorTreeItem, when this action is fired, open it in the
         * workspace.
         * </p>
         * @see KLWorkspace
         */
        OPEN_IN_WORKSPACE,

        /**
         * <p>For a multiple selection of ConceptNavigatorTreeItem, when this action is fired, open them in the
         * workspace.</p>
         * @see KLWorkspace
         */
        POPULATE_SELECTION,

        /**
         * TODO
         */
        SEND_TO_JOURNAL,

        /**
         * TODO
         */
        SEND_TO_CHAPTER,

        /**
         * TODO
         */
        COPY,

        /**
         * TODO
         */
        SAVE_TO_FAVORITES;

        public static List<CONTEXT_MENU_ACTION> getSingleActions() {
            return List.of(SHOW_RELATED_CONCEPTS, OPEN_IN_WORKSPACE);
        }

        public static List<CONTEXT_MENU_ACTION> getMultipleActions() {
            return List.of(POPULATE_SELECTION, SEND_TO_JOURNAL, SEND_TO_CHAPTER, COPY, SAVE_TO_FAVORITES);
        }


    }

    /** {@inheritDoc} **/
    @Override
    protected Skin<?> createDefaultSkin() {
        conceptNavigatorTreeViewSkin = new KLConceptNavigatorTreeViewSkin(this);
        return conceptNavigatorTreeViewSkin;
    }

    /** {@inheritDoc} **/
    @Override
    public String getUserAgentStylesheet() {
        return KLConceptNavigatorControl.class.getResource("concept-navigator.css").toExternalForm();
    }

    /**
     * <p>Once the {@link Navigator} is set, gets the root nids, and for each of those creates a
     * {@link ConceptNavigatorTreeItem}</p>
     * <p>While {@link Navigator#getRootNids()} returns an array, only the first item will be actually
     * set as root of this {@link KLConceptNavigatorControl}.
     * </p>
     * @return a list of {@link ConceptNavigatorTreeItem}
     */
    private List<ConceptNavigatorTreeItem> getConceptNavigatorRoot() {
        return Arrays.stream(getNavigator().getRootNids())
                .mapToObj(rootNid -> {
                    ConceptNavigatorTreeItem treeItem = getConceptNavigatorTreeItem(rootNid, -1);
                    fetchChildren(treeItem);
                    treeItem.setExpanded(true);
                    return treeItem;
                })
                .toList();
    }

    /**
     * For a given nid of a parent concept, {@link Navigator#getChildNids(int)} provides a list of
     * {@link dev.ikm.tinkar.coordinate.navigation.calculator.Edge}, that is used to generate a
     * list of {@link ConceptNavigatorTreeItem} as children of that concept.
     * @param nid the nid of a parent concept
     * @return a list of {@link ConceptNavigatorTreeItem} with the children of that parent
     */
    private List<ConceptNavigatorTreeItem> getChildren(int nid) {
        return getNavigator().getChildEdges(nid).stream()
                .map(edge -> getConceptNavigatorTreeItem(edge.destinationNid(), nid))
                .toList();
    }

    /**
     * <p>Create a {@link ConceptNavigatorTreeItem} without descendents.
     * </p>
     * <p> Only if this item is not a leaf and the user expands it, its children are
     * really generated, in a background thread.
     * </p>
     * @param nid the nid of the concept
     * @param parentNid the nid of the parent of the concept, or -1 if root.
     * @return a {@link ConceptNavigatorTreeItem} for that concept
     */
    private ConceptNavigatorTreeItem getConceptNavigatorTreeItem(int nid, int parentNid) {
        ConceptNavigatorTreeItem conceptNavigatorTreeItem = createSingleConceptNavigatorTreeItem(nid, parentNid);
        conceptNavigatorTreeItem.expandedProperty().subscribe((_, expanded) -> {
            if (expanded && conceptNavigatorTreeItem.getChildren().isEmpty()) {
                // when a new branch is expanded, prune the collapsed branches of the treeView,
                // to keep as much low number of items in the treeView as possible
                if (getRoot() != null) {
                    ConceptNavigatorUtils.iterateTree((ConceptNavigatorTreeItem) getRoot(), i -> {
                        if (i != null && i.getValue().nid() != nid && !i.getChildren().isEmpty() && !i.isExpanded()) {
                            i.getChildren().clear();
                        }
                    });
                }
                TinkExecutor.threadPool().execute(() -> {
                    Future<Boolean> booleanFuture = fetchChildrenTask(conceptNavigatorTreeItem);
                    if (booleanFuture != null) {
                        try {
                            if (!booleanFuture.get()) {
                                // There was an error, the list of children was null or empty
                            }
                        } catch (InterruptedException | ExecutionException e) {
                            // log error
                            throw new RuntimeException(e);
                        }
                    }
                });
            }
        });
        return conceptNavigatorTreeItem;
    }

    /**
     * <p>Gets the children of a given {@link ConceptNavigatorTreeItem} that doesn't have its descendents
     * generated yet, in the JavaFX Application Thread.
     * </p>
     * @param conceptNavigatorTreeItem the {@link ConceptNavigatorTreeItem} that doesn't have its children added yet.
     */
    private void fetchChildren(ConceptNavigatorTreeItem conceptNavigatorTreeItem) {
        int nid = conceptNavigatorTreeItem.getValue().nid();
        if (!getNavigator().getChildEdges(nid).isEmpty()) {
            conceptNavigatorTreeItem.getChildren().addAll(getChildren(nid));
        }
    }

    /**
     * <p>A task that generates the children of a given {@link ConceptNavigatorTreeItem}, in a background thread,
     * and returns true if a non-empty children list is found.
     * </p>
     * @param conceptNavigatorTreeItem the {@link ConceptNavigatorTreeItem} that doesn't have its children added yet.
     * @return a future with a boolean result that is true if the {@link ConceptNavigatorTreeItem} has its children
     * added, false if the list is null or empty, or null if the item doesn't have any children to fetch
     * in the first place.
     */
    private Future<Boolean> fetchChildrenTask(ConceptNavigatorTreeItem conceptNavigatorTreeItem) {
        int nid = conceptNavigatorTreeItem.getValue().nid();
        if (getNavigator().getChildEdges(nid).isEmpty()) {
            return null;
        }
        return TinkExecutor.threadPool().submit(() -> {
            List<ConceptNavigatorTreeItem> children = getChildren(nid);
            CountDownLatch latch = new CountDownLatch(1);
            Platform.runLater(() -> {
                conceptNavigatorTreeItem.getChildren().addAll(children);
                latch.countDown();
            });
            latch.await();
            return children != null && !children.isEmpty();
        });
    }

    /**
     * <p>Creates a {@link ConceptNavigatorTreeItem} without children, based on a given nid of the related
     * {@link ConceptFacade}, and the parent nid of the parent concept (or -1 if root).
     * </p>
     * <p>Sets its {@link ConceptNavigatorTreeItem#definedProperty()},
     * {@link ConceptNavigatorTreeItem#multiParentProperty()} and {@link ConceptNavigatorTreeItem#tagProperty()},
     * but it doesn't fetch any children.
     * </p>
     * @param nid the nid of the concept
     * @param parentNid the nid of the parent of the concept, or -1 if root.
     * @return a {@link ConceptNavigatorTreeItem} for that concept, without children.
     */
    private ConceptNavigatorTreeItem createSingleConceptNavigatorTreeItem(int nid, int parentNid) {
        ConceptFacade facade = Entity.getFast(nid);
        ConceptNavigatorTreeItem conceptNavigatorTreeItem = new ConceptNavigatorTreeItem(getNavigator(), facade, parentNid);
        conceptNavigatorTreeItem.setDefined(getNavigator().getViewCalculator().isDefined(facade));
        conceptNavigatorTreeItem.setMultiParent(getNavigator().getParentNids(nid).length > 1);
        // DUMMY!
        int nextTag = new Random().nextInt(100);
        conceptNavigatorTreeItem.setTag(ConceptNavigatorTreeItem.TAG.values()[nextTag < 90 ? 0 : nextTag < 95 ? 1 : 2]);
        return conceptNavigatorTreeItem;
    }

    /**
     * <p>Expands the treeView starting from its root node, so this concept gets visible, and
     * then highlights it.
     * </p>
     * <p>From all the possible concept's lineages, the one that gets expanded is the shorter one
     * that matches both its nid and parent nid.
     * </p>
     * @param conceptItem a {@link dev.ikm.komet.kview.controls.InvertedTree.ConceptItem}
     * @see ConceptNavigatorUtils#findShorterLineage(InvertedTree.ConceptItem, Navigator)
     */
    public void expandAndHighlightConcept(InvertedTree.ConceptItem conceptItem) {
        conceptNavigatorTreeViewSkin.expandAndHighlightConcept(conceptItem);
    }

    /**
     * <p>Toggles off the highlight state of every concept that might have it in this treeView.
     * </p>
     */
    public void unhighlightConceptsWithDelay() {
        PauseTransition pause = new PauseTransition(Duration.millis(getActivation()));
        pause.setOnFinished(_ -> ConceptNavigatorUtils.iterateTree((ConceptNavigatorTreeItem) getRoot(), item -> item.setHighlighted(false)));
        pause.play();
    }

    /**
     * <p>Expands the treeView starting from its root node, so this concept gets visible, and
     * then selects it.
     * </p>
     * <p>From all the possible concept's lineages, the one that gets expanded is the shorter one
     * that matches both its nid and parent nid.
     * </p>
     * @param conceptItem a {@link dev.ikm.komet.kview.controls.InvertedTree.ConceptItem}
     * @see ConceptNavigatorUtils#findShorterLineage(InvertedTree.ConceptItem, Navigator)
     */
    public void expandAndSelectConcept(InvertedTree.ConceptItem conceptItem) {
        conceptNavigatorTreeViewSkin.expandAndSelectConcept(conceptItem);
    }

}
