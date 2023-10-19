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
package dev.ikm.komet.amplify.properties;

import dev.ikm.komet.amplify.commons.BasicController;
import dev.ikm.komet.amplify.om.ChangeCoordinate;
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
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.*;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.Region;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.time.format.DateTimeFormatter;
import java.util.Arrays;
import java.util.NavigableSet;
import java.util.Set;
import java.util.TreeSet;
import java.util.stream.Collectors;

/**
 * The user is shown a search and dropdown to filter between Change items (concept & semantic versions).
 * Hierarchy Change controller is responsible for displaying a view (hierarchy-change.fxml) consisting of
 * muliple versions of concept and semantic changes. Each row is shown using ChangeListItemController.
 */
public class HierarchyController implements BasicController {
    private static final Logger LOG = LoggerFactory.getLogger(HierarchyController.class);
    @FXML
    private ChoiceBox<String> changeFilterChoiceBox;

    @FXML
    private TreeView<String> hiearchyTreeView;

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
//        hiearchyTreeView.setCellFactory(stringTreeView -> new HierarchyTreeCell());
        // provide a root tree item
        TreeItem<String> rootTreeItem = new TreeItem<>("root");
        hiearchyTreeView.setRoot(rootTreeItem);
        hiearchyTreeView.setShowRoot(false);
        rootTreeItem.setExpanded(true);
    }
    @Override
    public void cleanup() {
        timelineRangeChangeCoordinateSet.clear();
    }
    public void clearView() {
        hiearchyTreeView.getRoot().getChildren().clear();
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
     *     8. do a diff of parents & children (added, retired, (edit?))
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
        // Clear VBox of rows
        clearView();

        NavigableSet<Long> datePointsEpochMillis = extractEpochTimeMillis();
        if (datePointsEpochMillis.size() == 0) {
            // show the current navigation
            System.out.println("show the current navigation now");
            updateHierarchy();
        } else if (datePointsEpochMillis.size() == 1) {
            // range date will be change date to now.
            System.out.println("show range diff of one (past) date point to now.");
            updateHierarchy(datePointsEpochMillis.first());
        } else {
            // take start point and end point and diff them.
            System.out.println("show range diff of past Point and latest Point.");
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

        TreeItem<String> root = hiearchyTreeView.getRoot();

        //

        addConceptTreeItems(root, latestViewCalculator, latestEpochMillis,"ADDED", "added", addedNewInFutureSet);
        addConceptTreeItems(root, latestViewCalculator,   pastMillis,    "", "", sameNewInFutureSet);
        addConceptTreeItems(root, pastViewCalculator  , latestEpochMillis,"RETIRED", "retired", retiredInFutureSet);

        // Create the main concept selected.
        TreeItem<String> conceptSelected = new TreeItem<>(conceptName); //
        conceptSelected.setExpanded(true);
        root.getChildren().add(conceptSelected);

        final Set<Integer> futureChildrenSet = new TreeSet<>(latestChildrenOfconcept.intStream().boxed().toList());
        final Set<Integer> pastChildrenSet = new TreeSet<>(pastChildrenOfconcept.intStream().boxed().toList());
        addedNewInFutureSet = futureChildrenSet.stream().filter(e -> !pastChildrenSet.contains(e)).collect(Collectors.toSet());
        sameNewInFutureSet = futureChildrenSet.stream().filter(e -> pastChildrenSet.contains(e)).collect(Collectors.toSet());
        retiredInFutureSet = pastChildrenSet.stream().filter(e -> !futureChildrenSet.contains(e)).collect(Collectors.toSet());

        addConceptTreeItems(conceptSelected, latestViewCalculator, latestEpochMillis,"ADDED", "added", addedNewInFutureSet);
        addConceptTreeItems(conceptSelected, latestViewCalculator, pastMillis,    "", "", sameNewInFutureSet);
        addConceptTreeItems(conceptSelected, pastViewCalculator, latestEpochMillis,"RETIRED", "retired", retiredInFutureSet);
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
            th.printStackTrace();
            String dateStr = DateTimeUtil.format(System.currentTimeMillis(), DATE_POINT_FORMATTER);
            dateStr = dateStr.substring(0, 5) + "-" + dateStr.substring(8);
            return dateStr;
        }

    }

    private void updateHierarchy() {
        ViewCalculator viewCalculator = getViewProperties().calculator();
        long latestMillis = System.currentTimeMillis();
        StampCoordinateRecord latestStampCoordinate = viewCalculator.vertexStampCalculator().filter().withStampPositionTime(latestMillis);
        ViewCoordinateRecord latestViewCoordinate = viewCalculator.viewCoordinateRecord().withStampCoordinate(latestStampCoordinate);
        ViewCalculatorWithCache latestViewCalculator = new ViewCalculatorWithCache(latestViewCoordinate);

        IntIdList latestParentsOfconcept = latestViewCalculator.navigationCalculator().parentsOf(getEntityFacade().nid());
        IntIdList latestChildrenOfconcept = latestViewCalculator.navigationCalculator().childrenOf(getEntityFacade().nid());
        String conceptName = latestViewCalculator.getPreferredDescriptionTextWithFallbackOrNid(getEntityFacade().nid());

        TreeItem<String> root = hiearchyTreeView.getRoot();

        addConceptTreeItems(root, viewCalculator, latestMillis, "", null, new TreeSet<>(latestParentsOfconcept.intStream().boxed().toList()));

        // Create the main concept selected.
        TreeItem<String> conceptSelected = new TreeItem<>(conceptName); //
        root.getChildren().add(conceptSelected);
        conceptSelected.setExpanded(true);

        // Add children concepts

        addConceptTreeItems(conceptSelected, latestViewCalculator, latestMillis, null, null, new TreeSet<>(latestChildrenOfconcept.intStream().boxed().toList()));
    }

    private void addConceptTreeItems(TreeItem<String> parentTreeNode, ViewCalculator viewCalculator, long epochTime, String transaction, String styleClasses, Set<Integer> conceptNids) {
        conceptNids.forEach(nid -> {
            String conceptTitle = viewCalculator.getPreferredDescriptionTextWithFallbackOrNid(nid);
            String dateStr = simpleDateStr(epochTime);
            TreeItem<String> treeItem = new TreeItem<>("");
            //treeItem.setValue(transaction + conceptTitle);
            String[] styleClassesArray = null;
            if (styleClasses != null && styleClasses.trim().length() > 0) {
                styleClassesArray =  styleClasses.split("\\.");
            }
            treeItem.setGraphic(createTreeCellGraphic(conceptTitle, dateStr, transaction, styleClassesArray));
            parentTreeNode.getChildren().add(treeItem);
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
    /**
     *  [disclosure] [icon] [concept text] [date badge]
     * @param conceptTitle
     * @param date
     * @return
     */
    private HBox createTreeCellGraphic(String conceptTitle, String date, String transaction, String ...states) {

        HBox treeItemDisplay = new HBox();
        treeItemDisplay.setAlignment(Pos.CENTER_LEFT);
        treeItemDisplay.setMaxWidth(Double.MAX_VALUE);
        treeItemDisplay.setPrefWidth(200.0);
        treeItemDisplay.setSpacing(5.0);
        treeItemDisplay.getStyleClass().add("node-cell");
        HBox.setHgrow(treeItemDisplay, Priority.ALWAYS);

        // Concept circle
        Region icon = new Region();
        HBox.setMargin(icon, new Insets(0,0,3, 0));
        icon.getStyleClass().addAll("icon", "circle");
        treeItemDisplay.getChildren().add(icon);

        // Label
        Label conceptTitleLabel = new Label(conceptTitle);
        conceptTitleLabel.setPrefWidth(255.0);
        conceptTitleLabel.setMaxWidth(Double.MAX_VALUE);
        Tooltip.install(conceptTitleLabel, new Tooltip(conceptTitle));
        conceptTitleLabel.getStyleClass().add("title-text");
        HBox.setHgrow(conceptTitleLabel, Priority.ALWAYS);
        treeItemDisplay.getChildren().add(conceptTitleLabel);

        // Transaction badge
//        Label transactionLabel = new Label(transaction);
//        if (states != null && states.length > 0) {
//            transactionLabel.getStyleClass().addAll(states);
//        }
//        transactionLabel.setPrefSize(16, 58);
//        HBox.setHgrow(transactionLabel, Priority.ALWAYS);
//        treeItemDisplay.getChildren().add(transactionLabel);

        // Date badge
        Label dateLabel = new Label(date);
        dateLabel.getStyleClass().addAll("date-text");
        if (states != null && states.length > 0) {
            dateLabel.getStyleClass().addAll(states);
        }
        dateLabel.setPrefSize(16, 58);
        HBox.setHgrow(dateLabel, Priority.ALWAYS);
        treeItemDisplay.getChildren().add(dateLabel);


        return treeItemDisplay;
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
        timelineRangeChangeCoordinateSet = changeCoordinates;
        updateView();
    }
}
