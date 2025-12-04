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

//~--- JDK imports ------------------------------------------------------------

import dev.ikm.tinkar.common.alert.AlertStreams;
import javafx.application.Platform;
import javafx.scene.Node;
import javafx.scene.control.TreeItem;
import org.eclipse.collections.api.collection.ImmutableCollection;
import dev.ikm.komet.framework.view.ObservableView;
import dev.ikm.tinkar.common.id.IntIdSet;
import dev.ikm.tinkar.common.id.IntIds;
import dev.ikm.tinkar.common.id.PublicId;
import dev.ikm.tinkar.common.service.TinkExecutor;
import dev.ikm.tinkar.common.util.text.NaturalOrder;
import dev.ikm.tinkar.coordinate.navigation.calculator.Edge;
import dev.ikm.tinkar.coordinate.view.calculator.ViewCalculator;
import dev.ikm.tinkar.entity.ConceptEntity;
import dev.ikm.tinkar.entity.Entity;
import dev.ikm.tinkar.terms.ConceptFacade;
import dev.ikm.tinkar.terms.TinkarTerm;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.OptionalInt;
import java.util.concurrent.CountDownLatch;
import java.util.regex.Pattern;

/**
 * A {@link TreeItem} for modeling nodes in ISAAC taxonomies.
 * <p>
 * The {@code MultiParentGraphItemImpl} is not a visual component. The
 * {@code MultiParentGraphCell} provides the rendering for this tree item.
 *
 *
 * @author ocarlsen
 * @author <a href="mailto:daniel.armbrust.list@gmail.com">Dan Armbrust</a>
 * @see MultiParentGraphCell
 */
public class MultiParentVertexImpl
        extends TreeItem<ConceptFacade>
        implements MultiParentVertex, Comparable<MultiParentVertexImpl> {
    private static final Pattern intPattern = Pattern.compile("^-?[0-9]+$");

    /**
     * The Constant LOG.
     */
    private static final Logger LOG = LoggerFactory.getLogger(MultiParentVertexImpl.class);
    //~--- fieldValues --------------------------------------------------------------
    private final List<MultiParentVertexImpl> extraParents = new ArrayList<>();
    private final int nid;
    private final IntIdSet typeNids;
    private CountDownLatch childrenLoadedLatch = new CountDownLatch(1);
    private volatile boolean cancelLookup = false;
    private boolean defined = false;
    private boolean multiParent = false;
    private int multiParentDepth = 0;
    private boolean secondaryParentOpened = false;
    private MultiParentGraphViewController graphController;
    private String conceptDescriptionText;  // Cached to speed up comparisons with toString method.
    private ImmutableCollection<Edge> childLinks;
    private LeafStatus leafStatus = LeafStatus.UNKNOWN;

    //~--- constructors --------------------------------------------------------
    public MultiParentVertexImpl(MultiParentGraphViewController graphController) {
        super();
        this.graphController = graphController;
        this.nid = Integer.MAX_VALUE;
        this.typeNids = IntIds.set.of(TinkarTerm.UNINITIALIZED_COMPONENT.nid());
    }

    public MultiParentVertexImpl(int conceptNid, MultiParentGraphViewController graphController, IntIdSet typeNids) {
        this(Entity.getFast(conceptNid), graphController, typeNids, null);
    }

    public MultiParentVertexImpl(ConceptEntity conceptEntity
            , MultiParentGraphViewController graphController, IntIdSet typeNids, Node graphic) {
        super(conceptEntity, graphic);
        this.graphController = graphController;
        this.nid = conceptEntity.nid();
        this.typeNids = typeNids;
    }

    private static int getConceptNid(TreeItem<ConceptEntity> item) {
        return ((item != null) && (item.getValue() != null)) ? item.getValue()
                .nid()
                : null;
    }

    //~--- get methods ---------------------------------------------------------
    private static TreeItem<ConceptEntity> getTreeRoot(TreeItem<ConceptEntity> item) {
        TreeItem<ConceptEntity> parent = item.getParent();

        if (parent == null) {
            return item;
        } else {
            return getTreeRoot(parent);
        }
    }

    //~--- methods -------------------------------------------------------------
    public void blockUntilChildrenReady()
            throws InterruptedException {
        childrenLoadedLatch.await();
    }

    /**
     * clears the display nodes, and the nid lists, and resets the calculators
     */
    public void clearChildren() {
        cancelLookup = true;
        childrenLoadedLatch.countDown();
        getChildren().forEach(
                (child) -> {
                    ((MultiParentVertexImpl) child).clearChildren();
                });
        getChildren().clear();
        childLinks = null;
        resetChildrenCalculators();

    }

    @Override
    public int compareTo(MultiParentVertexImpl o) {
        int compare = NaturalOrder.compareStrings(this.toString(), o.toString());
        if (compare != 0) {
            return compare;
        }
        return Integer.compare(nid, o.nid);
    }

    public void updateDescription() {
        if (this.nid != Integer.MAX_VALUE) {
            this.conceptDescriptionText = graphController.getObservableView().getDescriptionTextOrNid(nid);
        } else {
            this.conceptDescriptionText = "hidden root";
        }
    }

    public Node computeGraphic() {
        return graphController.getDisplayPolicies()
                .computeGraphic(this, graphController.getViewCalculator());
    }

    public void invalidate() {
        updateDescription();

        for (TreeItem<ConceptFacade> child : getChildren()) {
            MultiParentVertexImpl multiParentTreeItem = (MultiParentVertexImpl) child;

            multiParentTreeItem.invalidate();
        }
    }

    /**
     * Removed the graphical display nodes from the tree, but does not clear the
     * cached nid set of children
     */
    public void removeChildren() {
        this.getChildren()
                .clear();
    }

    void addChildrenNow() {
        if (getChildren().isEmpty()) {
            try {
                final ConceptFacade conceptFacade = getValue();

                if (!shouldDisplay()) {
                    // Don't add children to something that shouldn't be displayed
                    LOG.atTrace().log("this.shouldDisplay() == false: not adding children to " + this.getConceptPublicId());
                } else if (conceptFacade == null) {
                    LOG.atTrace().log("addChildren(): conceptEntity=" + conceptFacade);
                } else {  // if (conceptEntity != null)
                    // Gather the children
                    LOG.info("addChildrenNOW(): conceptEntity=" + conceptFacade);
                    ArrayList<MultiParentVertexImpl> childrenToAdd = new ArrayList<>();
                    Navigator navigator = graphController.getNavigator();

                    if (childLinks == null) {
                        childLinks = navigator.getChildEdges(conceptFacade.nid());
                    }

                    for (Edge childLink : childLinks) {
                        ConceptEntity childChronology = Entity.getFast(childLink.destinationNid());
                        MultiParentVertexImpl childItem = new MultiParentVertexImpl(childChronology, graphController, childLink.typeNids(), null);
                        ObservableView observableView = graphController.getObservableView();

                        try {
                            childItem.setDefined(observableView.calculator().hasSufficientSet(childChronology.nid()));
                        } catch (Throwable e) {
                            //TODO remove catch after better handing of: More than one set of axioms for concept: ConceptRecord{Model concept <-2142333842>
                            AlertStreams.dispatchToRoot(e);
                            childItem.setDefined(false);
                        }
                        childItem.toString();
                        childItem.setMultiParent(navigator.getParentNids(childLink.destinationNid()).length > 1);

                        if (childItem.shouldDisplay()) {
                            childrenToAdd.add(childItem);
                        } else {
                            LOG.atTrace().log(
                                    "item.shouldDisplay() == false: not adding " + childItem.getConceptPublicId() + " as child of "
                                            + this.getConceptPublicId());
                        }
                    }

                    Collections.sort(childrenToAdd);

                    if (cancelLookup) {
                        return;
                    }
                    getChildren().addAll(childrenToAdd);
                }
            } catch (Exception e) {
                LOG.error("Unexpected error computing children and/or grandchildren for " + this.conceptDescriptionText, e);
            } finally {
                childrenLoadedLatch.countDown();
            }
        }
    }

    public boolean shouldDisplay() {
        if (graphController == null || graphController.getDisplayPolicies() == null) {
            return false;
        }
        return graphController.getDisplayPolicies()
                .shouldDisplay(this, graphController.getViewCalculator());
    }

    public PublicId getConceptPublicId() {
        return (getValue() != null) ? getValue().publicId()
                : null;
    }

    void addChildren() {
        LOG.atTrace().log("addChildren: ConceptEntity=" + this.getValue());
        if (getChildren().isEmpty()) {
            if (shouldDisplay()) {
                FetchChildren fetchTask = new FetchChildren(childrenLoadedLatch, this);
                TinkExecutor.threadPool().submit(fetchTask);
            }
        }
    }

    private void resetChildrenCalculators() {
        CountDownLatch cdl = new CountDownLatch(1);
        Runnable r = () -> {
            cancelLookup = false;
            childrenLoadedLatch.countDown();
            childrenLoadedLatch = new CountDownLatch(1);
            cdl.countDown();
        };

        if (Platform.isFxApplicationThread()) {
            r.run();
        } else {
            Platform.runLater(r);
        }

        try {
            cdl.await();
        } catch (InterruptedException e) {
            LOG.error("unexpected interrupt", e);
        }
    }

    //~--- get methods ---------------------------------------------------------
    protected boolean isCancelRequested() {
        return cancelLookup;
    }

    //~--- get methods ---------------------------------------------------------
    NavigatorDisplayPolicies getDisplayPolicies() {
        return this.graphController.getDisplayPolicies();
    }

    public List<MultiParentVertexImpl> getExtraParents() {
        return extraParents;
    }

    @Override
    public boolean isLeaf() {
        if (this.nid == Integer.MAX_VALUE) {
            return false;
        }
        if (leafStatus != LeafStatus.UNKNOWN) {
            return leafStatus == LeafStatus.IS_LEAF;
        }
        if (multiParentDepth > 0) {
            leafStatus = LeafStatus.IS_LEAF;
            return true;
        }
        if (this.childLinks == null) {
            if (this.graphController.getNavigator().isLeaf(nid)) {
                leafStatus = LeafStatus.IS_LEAF;
            } else {
                leafStatus = LeafStatus.NOT_LEAF;
            }
            return leafStatus == LeafStatus.IS_LEAF;
        }
        if (this.childLinks.isEmpty()) {
            leafStatus = LeafStatus.IS_LEAF;
        } else {
            leafStatus = LeafStatus.NOT_LEAF;
        }
        return leafStatus == LeafStatus.IS_LEAF;
    }

    /**
     * @see javafx.scene.control.TreeItem#toString() WARNING: toString is
     * currently used in compareTo()
     */
    @Override
    public String toString() {
        try {
            if (this.getValue() != null) {
                if ((conceptDescriptionText == null) || conceptDescriptionText.startsWith("no description for ")
                        || (conceptDescriptionText.startsWith("-") && intPattern.matcher(conceptDescriptionText).matches())) {
                    updateDescription();
                }
                return this.conceptDescriptionText;
            }

            return "root";
        } catch (RuntimeException | Error re) {
            LOG.error(re.getLocalizedMessage(), re);
            throw re;
        }
    }
    //~--- get methods ---------------------------------------------------------
    @Override
    public boolean isRoot() {
        if (this.nid == Integer.MAX_VALUE) {
            return true;
        }
        if (TinkarTerm.ROOT_VERTEX.nid() == this.nid) {
            return true;
        } else if (this.getParent() == null) {
            return true;
        } else if (this.getParent() == graphController.getRoot()) {
            return true;
        }
        return false;
    }

    @Override
    public boolean isDefined() {
        return defined;
    }

    //~--- set methods ---------------------------------------------------------
    public void setDefined(boolean defined) {
        this.defined = defined;
    }

    @Override
    public boolean isMultiParent() {
        return multiParent;
    }

    //~--- set methods ---------------------------------------------------------
    public void setMultiParent(boolean multiParent) {
        this.multiParent = multiParent;
    }

    @Override
    public boolean isSecondaryParentOpened() {
        return secondaryParentOpened;
    }

    @Override
    public int getConceptNid() {
        return (getValue() != null) ? getValue().nid()
                : Integer.MIN_VALUE;
    }

    public IntIdSet getTypeNids() {
        return typeNids;
    }

    //~--- get methods ---------------------------------------------------------
    @Override
    public int getMultiParentDepth() {
        return multiParentDepth;
    }

    @Override
    public OptionalInt getOptionalParentNid() {
        if (getParent() != null && getParent().getValue() != null) {
            return OptionalInt.of(getParent().getValue().nid());
        }
        return OptionalInt.empty();
    }

    //~--- set methods ---------------------------------------------------------
    public void setMultiParentDepth(int multiParentDepth) {
        this.multiParentDepth = multiParentDepth;
    }

    //~--- set methods ---------------------------------------------------------
    public void setSecondaryParentOpened(boolean secondaryParentOpened) {
        this.secondaryParentOpened = secondaryParentOpened;
    }

    public MultiParentGraphViewController getGraphController() {
        return graphController;
    }

    public ViewCalculator getViewCalculator() {
        return graphController.getObservableView().calculator();
    }

    enum LeafStatus {
        UNKNOWN, IS_LEAF, NOT_LEAF
    }
}
