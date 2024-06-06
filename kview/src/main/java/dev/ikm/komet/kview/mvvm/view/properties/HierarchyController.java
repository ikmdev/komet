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
package dev.ikm.komet.kview.mvvm.view.properties;

import dev.ikm.komet.kview.mvvm.view.BasicController;
import dev.ikm.komet.kview.mvvm.model.ChangeCoordinate;
import dev.ikm.komet.framework.view.ViewProperties;
import dev.ikm.tinkar.common.id.IntIdList;
import dev.ikm.tinkar.common.util.time.DateTimeUtil;
import dev.ikm.tinkar.coordinate.stamp.StampCoordinateRecord;
import dev.ikm.tinkar.coordinate.view.ViewCoordinateRecord;
import dev.ikm.tinkar.coordinate.view.calculator.ViewCalculator;
import dev.ikm.tinkar.coordinate.view.calculator.ViewCalculatorWithCache;
import dev.ikm.tinkar.entity.Entity;
import dev.ikm.tinkar.entity.StampEntity;
import dev.ikm.tinkar.entity.StampEntityVersion;
import dev.ikm.tinkar.terms.EntityFacade;
import javafx.application.Platform;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;
import java.util.stream.Collectors;

/**
 * The user is shown a search and dropdown to filter between Change items (concept &amp; semantic versions).
 * Hierarchy Change view is responsible for displaying a view (hierarchy-change.fxml) consisting of
 * muliple versions of concept and semantic changes. Each row is shown using ChangeListItemController.
 */
public class HierarchyController implements BasicController {
    private static final Logger LOG = LoggerFactory.getLogger(HierarchyController.class);
    @FXML
    private ChoiceBox<String> changeFilterChoiceBox;

    @FXML
    private TreeView<ConceptTreeItemRecord> hiearchyTreeView;

    @FXML
    private ToggleButton searchFilterToggleButton;

    @FXML
    private TextField searchTextField;

    private EntityFacade entityFacade;
    private ViewProperties viewProperties;

    private Set<ChangeCoordinate> timelineRangeChangeCoordinateSet;
    private final static DateTimeFormatter DATE_POINT_FORMATTER = DateTimeFormatter.ofPattern("MM-dd-yyyy");

    @FXML
    public void initialize()  {
        clearView();
        changeFilterChoiceBox.getItems().addAll("All", "Concept", "Description", "Axiom");
        changeFilterChoiceBox.setValue("All");
        hiearchyTreeView.setCellFactory(param -> new HierarchyTreeCell());

        // provide a root tree item
        TreeItem<ConceptTreeItemRecord> rootTreeItem = new TreeItem<>();
        rootTreeItem.setValue(new ConceptTreeItemRecord(0, "root", "", "", null));
        hiearchyTreeView.setRoot(rootTreeItem);
        hiearchyTreeView.setShowRoot(false);
        rootTreeItem.setExpanded(true);
    }
    @Override
    public void cleanup() {
        timelineRangeChangeCoordinateSet.clear();
    }
    public void clearView() {
        if (hiearchyTreeView.getRoot() != null) {
            hiearchyTreeView.getRoot().getChildren().clear();
        }
    }

    private boolean isFilterSelected(String ...value) {
        return Arrays
                .stream(value)
                .filter(s -> s.equals(changeFilterChoiceBox.getValue()))
                .findFirst()
                .isPresent();
    }

    /*

       parent
       parent
       parent
        concept
          child
          child
     */
    public void updateModel(final ViewProperties viewProperties, final EntityFacade entityFacade) {
        this.viewProperties = viewProperties;
        this.entityFacade = entityFacade;
    }

    /**
     *     1. get all changes (date points) in range.
     *     2. get the oldest date point in range as a stamp coordinate record.
     *     3. get the latest date point in range as a stamp coordinate record.
     *     4. obtain a view calculator for oldest date point
     *     5. obtain a view calculator for latest date point
     *     6. With the latest view calculator generate list of parents, itself and it's children
     *     7. With the oldest view calculator generate list of parents, itself and it's children
     *     8. do a diff of parents &amp; children (added, retired, (edit?))
     *     9. render tree view.
     *           parent (a)
     *           parent (r)
     *           parent
     *           concept (C)
     *             child (a)
     *             child (r)
     *             child (E)
     */
    public void updateView() {
        if (getViewProperties() == null || getEntityFacade() == null) return;
        if (isUpdatingTreeView) return;
        isUpdatingTreeView = true;
        // Clear VBox of rows
        clearView();
        String conceptShow = getViewProperties().calculator().getPreferredDescriptionTextWithFallbackOrNid(getEntityFacade().nid());

        NavigableSet<Long> datePointsEpochMillis = extractEpochTimeMillis();
        if (datePointsEpochMillis.size() == 0) {
            // show the current navigation
            System.out.println("Shows the current navigation (as of now) concept: " + conceptShow);
            updateHierarchy();
        } else if (datePointsEpochMillis.size() == 1) {
            // range date will be change date to now.
            System.out.println("show range diff of one (past) date point to latest datetime (now). concept: " + conceptShow);
            updateHierarchy(datePointsEpochMillis.first());
        } else {
            // take start point and end point and diff them.
            System.out.println("show range diff of past Point (lower range slider) and latest Point (upper range slider). concept: " + conceptShow);
            updateHierarchy(datePointsEpochMillis.last(), datePointsEpochMillis.first());
        }
    }

    private NavigableSet<Long> extractEpochTimeMillis() {
        TreeSet<Long> times = new TreeSet<>();
        if (timelineRangeChangeCoordinateSet != null) {
            timelineRangeChangeCoordinateSet.forEach(changeCoordinate -> {
                int stampNid = changeCoordinate.versionChangeRecord().stampNid();
                StampEntity<? extends StampEntityVersion> stampForChange = Entity.getStamp(stampNid);
                long pastEpochMillis = stampForChange.time();
                times.add(pastEpochMillis);
            });
        }
        return times.descendingSet();
    }

    private void updateHierarchy(long pastEpochMillis, long latestEpochMillis) {
        ViewCalculator viewCalculator = getViewProperties().calculator();

        latestEpochMillis = latestEpochMillis > 0 ? latestEpochMillis : System.currentTimeMillis();
        StampCoordinateRecord latestStampCoordinate = viewCalculator.vertexStampCalculator().filter().withStampPositionTime(latestEpochMillis);
        ViewCoordinateRecord latestViewCoordinate = viewCalculator.viewCoordinateRecord().withStampCoordinate(latestStampCoordinate);
        ViewCalculatorWithCache latestViewCalculator = new ViewCalculatorWithCache(latestViewCoordinate);

        IntIdList latestParentsOfconcept = latestViewCalculator.navigationCalculator().parentsOf(getEntityFacade().nid());
        IntIdList latestChildrenOfconcept = latestViewCalculator.navigationCalculator().childrenOf(getEntityFacade().nid());
        String conceptName = latestViewCalculator.getPreferredDescriptionTextWithFallbackOrNid(getEntityFacade().nid());

        long pastMillis = pastEpochMillis;
        StampCoordinateRecord pastStampCoordinate = viewCalculator.vertexStampCalculator().filter().withStampPositionTime(pastMillis);
        ViewCoordinateRecord pastViewCoordinate = viewCalculator.viewCoordinateRecord().withStampCoordinate(pastStampCoordinate);
        ViewCalculatorWithCache pastViewCalculator = new ViewCalculatorWithCache(pastViewCoordinate);

        IntIdList pastParentsOfconcept = pastViewCalculator.navigationCalculator().parentsOf(getEntityFacade().nid());
        IntIdList pastChildrenOfconcept = pastViewCalculator.navigationCalculator().childrenOf(getEntityFacade().nid());
        String pastConceptName = pastViewCalculator.getPreferredDescriptionTextWithFallbackOrNid(getEntityFacade().nid());

        final Set<Integer> futureParentSet = new TreeSet<>(latestParentsOfconcept.intStream().boxed().toList());
        final Set<Integer> pastParentSet = new TreeSet<>(pastParentsOfconcept.intStream().boxed().toList());

        Set<Integer> addedNewInFutureSet = futureParentSet.stream().filter(e -> !pastParentSet.contains(e)).collect(Collectors.toSet());
        Set<Integer> sameNewInFutureSet = futureParentSet.stream().filter(e -> pastParentSet.contains(e)).collect(Collectors.toSet());
        Set<Integer> retiredInFutureSet = pastParentSet.stream().filter(e -> !futureParentSet.contains(e)).collect(Collectors.toSet());

        //
        CompletableFuture<Set<TreeItem<ConceptTreeItemRecord>>>  addedParentFuture = addConceptTreeItems(latestViewCalculator, latestEpochMillis,"ADDED", "added", addedNewInFutureSet);
        CompletableFuture<Set<TreeItem<ConceptTreeItemRecord>>>  existingParentFuture = addConceptTreeItems(latestViewCalculator,   pastMillis,    "", "", sameNewInFutureSet);
        CompletableFuture<Set<TreeItem<ConceptTreeItemRecord>>>  retiredParentFuture = addConceptTreeItems(pastViewCalculator  , latestEpochMillis,"RETIRED", "retired", retiredInFutureSet);

        final Set<Integer> futureChildrenSet = new TreeSet<>(latestChildrenOfconcept.intStream().boxed().toList());
        final Set<Integer> pastChildrenSet = new TreeSet<>(pastChildrenOfconcept.intStream().boxed().toList());

        Set<Integer> addedChildrenInFutureSet = futureChildrenSet.stream().filter(e -> !pastChildrenSet.contains(e)).collect(Collectors.toSet());
        Set<Integer> sameChildrenInFutureSet = futureChildrenSet.stream().filter(e -> pastChildrenSet.contains(e)).collect(Collectors.toSet());
        Set<Integer> retiredChildrenInFutureSet = pastChildrenSet.stream().filter(e -> !futureChildrenSet.contains(e)).collect(Collectors.toSet());

        CompletableFuture<Set<TreeItem<ConceptTreeItemRecord>>>  addedChildrenFuture = addConceptTreeItems(latestViewCalculator, latestEpochMillis,"ADDED", "added", addedChildrenInFutureSet);
        CompletableFuture<Set<TreeItem<ConceptTreeItemRecord>>>  existingChildrenFuture = addConceptTreeItems(latestViewCalculator, pastMillis,    "", "", sameChildrenInFutureSet);
        CompletableFuture<Set<TreeItem<ConceptTreeItemRecord>>>  retiredChildrenFuture = addConceptTreeItems(pastViewCalculator, latestEpochMillis,"RETIRED", "retired", retiredChildrenInFutureSet);

        CompletableFuture<TreeItem<String>>[] futures = new CompletableFuture[] {
                addedParentFuture, existingParentFuture, retiredParentFuture,
                addedChildrenFuture, existingChildrenFuture, retiredChildrenFuture
        };
        final String latestConceptDateStr = simpleDateStr(latestEpochMillis);
        // when all queries are completed add to the tree node.
        CompletableFuture
            .allOf(futures)
            .whenComplete((result, throwable) -> {
                if (throwable != null) {
                    isUpdatingTreeView = false;
                    throw new RuntimeException(throwable);
                }
                // build root concept
                Platform.runLater(() ->{
                    try {
                        List<TreeItem<ConceptTreeItemRecord>> rootChildren = hiearchyTreeView.getRoot().getChildren();
                        rootChildren.addAll(addedParentFuture.get());
                        rootChildren.addAll(existingParentFuture.get());
                        rootChildren.addAll(retiredParentFuture.get());
                        TreeItem<ConceptTreeItemRecord> conceptSelected = new TreeItem<>(); //
                        conceptSelected.setValue(new ConceptTreeItemRecord(getEntityFacade().nid(), conceptName, latestConceptDateStr,"", "concept"));
                        conceptSelected.setExpanded(true);
                        rootChildren.add(conceptSelected);

                        List<TreeItem<ConceptTreeItemRecord>> conceptChildren = conceptSelected.getChildren();
                        conceptChildren.addAll(addedChildrenFuture.get());
                        conceptChildren.addAll(existingChildrenFuture.get());
                        conceptChildren.addAll(retiredChildrenFuture.get());

                    } catch (InterruptedException e) {
                        throw new RuntimeException(e);
                    } catch (ExecutionException e) {
                        throw new RuntimeException(e);
                    } finally {
                        hiearchyTreeView.refresh();
                        isUpdatingTreeView = false;
                    }
                });
        });
    }

    private void updateHierarchy(long pastEpochMillis) {
        updateHierarchy(pastEpochMillis, -1);
    }

    private static String simpleDateStr(long epochTime) {
        try {
            String dateStr = DateTimeUtil.format(epochTime, DATE_POINT_FORMATTER);
            dateStr = dateStr.substring(0, 5) + "-" + dateStr.substring(8);
            return dateStr;
        } catch (Throwable th) {
            String dateStr = DateTimeUtil.format(System.currentTimeMillis(), DATE_POINT_FORMATTER);
            dateStr = dateStr.substring(0, 5) + "-" + dateStr.substring(8);
            LOG.error("Error date from this MM-dd-yyyy formatting date using epochTime: %s format to: %s, defaulted to System.currentTimeMillis() or %s".formatted(epochTime,
                    DateTimeUtil.format(epochTime, DATE_POINT_FORMATTER),
                    dateStr), th);
            return dateStr;
        }

    }
    private volatile boolean isUpdatingTreeView = false;
    /**
     * Query the database to display the latest view of the concept's parents and children.
     */
    private void updateHierarchy() {

        ViewCalculator viewCalculator = getViewProperties().calculator();
        long latestMillis = System.currentTimeMillis();
        StampCoordinateRecord latestStampCoordinate = viewCalculator.vertexStampCalculator().filter().withStampPositionTime(latestMillis);
        ViewCoordinateRecord latestViewCoordinate = viewCalculator.viewCoordinateRecord().withStampCoordinate(latestStampCoordinate);
        ViewCalculatorWithCache latestViewCalculator = new ViewCalculatorWithCache(latestViewCoordinate);

        IntIdList latestParentsOfconcept = latestViewCalculator.navigationCalculator().parentsOf(getEntityFacade().nid());
        IntIdList latestChildrenOfconcept = latestViewCalculator.navigationCalculator().childrenOf(getEntityFacade().nid());
        String conceptName = latestViewCalculator.getPreferredDescriptionTextWithFallbackOrNid(getEntityFacade().nid());

        CompletableFuture<Set<TreeItem<ConceptTreeItemRecord>>> parentTreeItems = addConceptTreeItems(viewCalculator, latestMillis, "", null, new TreeSet<>(latestParentsOfconcept.intStream().boxed().toList()));

        // Add children concepts
        CompletableFuture<Set<TreeItem<ConceptTreeItemRecord>>> childrenTreeItems = addConceptTreeItems(latestViewCalculator, latestMillis, null, null, new TreeSet<>(latestChildrenOfconcept.intStream().boxed().toList()));
        CompletableFuture
                .allOf(parentTreeItems, childrenTreeItems)
                .whenComplete((result, throwable)-> {
                    if (throwable != null) {
                        isUpdatingTreeView = false;
                        throw new RuntimeException(throwable);
                    }
            Platform.runLater(() ->{
                List<TreeItem<ConceptTreeItemRecord>> rootChildren = hiearchyTreeView.getRoot().getChildren();
                try {
                    // Add parents (appear as siblings)
                    rootChildren.addAll(parentTreeItems.get());

                    // Create the main concept selected.
                    TreeItem<ConceptTreeItemRecord> conceptSelected = new TreeItem<>(); //
                    conceptSelected.setValue(new ConceptTreeItemRecord(getEntityFacade().nid(), conceptName, simpleDateStr(latestMillis), "", "concept"));
                    rootChildren.add(conceptSelected);
                    conceptSelected.setExpanded(true);

                    // Add children to the main concept
                    conceptSelected.getChildren().addAll(childrenTreeItems.get());
                } catch (InterruptedException e) {
                    throw new RuntimeException(e);
                } catch (ExecutionException e) {
                    throw new RuntimeException(e);
                } finally {
                    hiearchyTreeView.refresh();
                    isUpdatingTreeView = false;
                }

            });
        });
    }

    private CompletableFuture<Set<TreeItem<ConceptTreeItemRecord>>> addConceptTreeItems(ViewCalculator viewCalculator,
                                                                                 long epochTime,
                                                                                 String transaction,
                                                                                 String styleClasses,
                                                                                 Set<Integer> conceptNids) {
        // This now uses async threads off of the application thread.
        Comparator<TreeItem<ConceptTreeItemRecord>> c = (treeItem1, treeItem2) -> treeItem1.getValue().conceptTitle().compareTo(treeItem2.getValue().conceptTitle());
        return CompletableFuture.supplyAsync( () -> {
            final TreeSet<TreeItem<ConceptTreeItemRecord>> treeItems = new TreeSet<>(c);
            conceptNids.forEach(nid -> {
                String conceptTitle = viewCalculator.getPreferredDescriptionTextWithFallbackOrNid(nid);
                String dateStr = simpleDateStr(epochTime);
                TreeItem<ConceptTreeItemRecord> treeItem = new TreeItem<>();
                String[] styleClassesArray = null;
                if (styleClasses != null && styleClasses.trim().length() > 0) {
                    styleClassesArray =  styleClasses.split("\\.");
                }
                // display graphic instead of text.
                treeItem.setValue(new ConceptTreeItemRecord(nid, conceptTitle, dateStr, transaction, styleClassesArray));
                treeItems.add(treeItem);
            });
            return treeItems;
        });
    }

    public static void main(String[] args) {
        String styleClasses = "added";
        String[] styleClassesArray = null;
        if (styleClasses != null && styleClasses.trim().length() > 0) {
            styleClassesArray =  styleClasses.split("\\.");
        }
        Arrays.stream(styleClassesArray).forEach(s -> System.out.println(s));
        System.out.println(styleClassesArray);
        String dateStr = simpleDateStr(System.currentTimeMillis());
        System.out.println(dateStr);
    }

    @FXML
    void popupSearchFilterOptions(ActionEvent event) {

    }

    @FXML
    void searchText(ActionEvent event) {

    }

    public EntityFacade getEntityFacade() {
        return entityFacade;
    }

    public ViewProperties getViewProperties() {
        return viewProperties;
    }

    /**
     * Called when the user selects a range of date points on the timeline control.
     * @param changeCoordinates
     */
    public void diffNavigationGraph(Set<ChangeCoordinate> changeCoordinates) {
        if (timelineRangeChangeCoordinateSet != null && timelineRangeChangeCoordinateSet.size()>0 && timelineRangeChangeCoordinateSet.containsAll(changeCoordinates)) {
            timelineRangeChangeCoordinateSet = changeCoordinates;
            updateView();
        } else {
            timelineRangeChangeCoordinateSet = changeCoordinates;
        }
    }
}
