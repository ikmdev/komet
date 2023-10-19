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
import dev.ikm.tinkar.coordinate.stamp.calculator.Latest;
import dev.ikm.tinkar.coordinate.stamp.change.ChangeChronology;
import dev.ikm.tinkar.coordinate.stamp.change.VersionChangeRecord;
import dev.ikm.tinkar.coordinate.view.calculator.ViewCalculator;
import dev.ikm.tinkar.entity.*;
import dev.ikm.tinkar.terms.EntityFacade;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Node;
import javafx.scene.control.ChoiceBox;
import javafx.scene.control.TextField;
import javafx.scene.control.ToggleButton;
import javafx.scene.input.InputMethodEvent;
import javafx.scene.layout.Pane;
import javafx.scene.layout.VBox;
import org.eclipse.collections.api.list.ImmutableList;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.util.*;

import static dev.ikm.komet.amplify.commons.CssHelper.defaultStyleSheet;
import static dev.ikm.komet.amplify.properties.ChangeListItemController.COLORS_FOR_EXTENSIONS;

/**
 * The user is shown a search and dropdown to filter between change list items (concept & semantic versions).
 * History Change controller is responsible for displaying the view (change-list-item.fxml) consisting of
 * muliple versions of concept and semantic changes. Each row is shown using ChangeListItemController.
 */
public class HistoryChangeController implements BasicController {
    private static final Logger LOG = LoggerFactory.getLogger(HistoryChangeController.class);
    protected static final String DESCRIPTION_LIST_ITEM_FXML_FILE = "change-list-item.fxml";
    @FXML
    private ChoiceBox<String> changeFilterChoiceBox;

    @FXML
    private ToggleButton searchFilterToggleButton;

    @FXML
    private TextField searchTextField;

    @FXML
    private VBox changeChronologyPane;
    private EntityFacade entityFacade;
    private ViewProperties viewProperties;
    private List<Node> cacheOfRows = new ArrayList<>();
    @FXML
    public void initialize()  {
        clearView();
        changeFilterChoiceBox.getItems().addAll("All", "Concept", "Description", "Axiom");
        changeFilterChoiceBox.setValue("All");
        changeFilterChoiceBox.valueProperty().addListener( (obs, oldVal, newVal) -> {
            if (getViewProperties() != null && getEntityFacade() != null) {
                updateModel(getViewProperties(), getEntityFacade());
                updateView();
            }
        });


    }
    @Override
    public void cleanup() {

    }
    public void clearView() {

    }


    private boolean isFilterSelected(String ...value) {
        return Arrays
                .stream(value)
                .filter(s -> s.equals(changeFilterChoiceBox.getValue()))
                .findFirst()
                .isPresent();
    }

    private TreeMap<String, TreeSet<Integer>> pathMap = new TreeMap<>();
    public void updateModel(final ViewProperties viewProperties, final EntityFacade entityFacade) {
        this.viewProperties = viewProperties;
        this.entityFacade = entityFacade;
        buildPathModel();
    }

    private void buildPathModel() {
        pathMap.clear();
        ViewCalculator viewCalculator = getViewProperties().calculator();
        // get concept's changes
        buildPathModuleSet(getEntityFacade().nid());

        // Description semantics
        ImmutableList<SemanticEntity> descrSemanticEntities = viewCalculator.getDescriptionsForComponent(getEntityFacade().nid());
        // For each populate a row based on changes
        descrSemanticEntities.forEach(semanticEntity -> buildPathModuleSet(semanticEntity.nid()));

        // Axioms
        Latest<SemanticEntityVersion> inferredSemanticVersion = viewCalculator.getInferredAxiomSemanticForEntity(getEntityFacade().nid());
        inferredSemanticVersion.ifPresent(semanticEntityVersion -> buildPathModuleSet(semanticEntityVersion.nid()));

        Latest<SemanticEntityVersion> statedSemanticVersion = viewCalculator.getStatedAxiomSemanticForEntity(getEntityFacade().nid());
        statedSemanticVersion.ifPresent(semanticEntityVersion -> {
            buildPathModuleSet(semanticEntityVersion.nid());
        });

    }
    private void buildPathModuleSet(int entityNid) {
        ViewCalculator viewCalculator = getViewProperties().calculator();
        ChangeChronology changeChronology = viewCalculator.changeChronology(entityNid);
        for(VersionChangeRecord versionChangeRecord:changeChronology.changeRecords()) {
            StampEntity<? extends StampEntityVersion> stampForChange = Entity.getStamp(versionChangeRecord.stampNid());
            String pathName = getViewProperties().calculator().getPreferredDescriptionTextWithFallbackOrNid(stampForChange.pathNid());
            if (!pathMap.containsKey(pathName)) {
                pathMap.put(pathName, new TreeSet<>());
            }
            int moduleNid = stampForChange.moduleNid();
            pathMap.get(pathName).add(moduleNid);
        }
    }
    private List<Integer> getModuleNids(String pathName){
        return pathMap.get(pathName).stream().toList();
    }
    public void highlightListItemByChangeCoordinate(ChangeCoordinate changeCoordinate) {
        this.changeChronologyPane.getChildren().forEach(node -> {
            if (checkEqualityOfChangeCoordinate(changeCoordinate, (ChangeCoordinate) node.getUserData())) {
                node.getStyleClass().add("selected");
            } else {
                node.getStyleClass().remove("selected");
            }
        });
    }

    private boolean checkEqualityOfChangeCoordinate(ChangeCoordinate changeCoordinate, ChangeCoordinate userData) {
        StampEntity<StampEntityVersion> stamp1 = Entity.getStamp(changeCoordinate.versionChangeRecord().stampNid());
        StampEntity<StampEntityVersion> stamp2 = Entity.getStamp(userData.versionChangeRecord().stampNid());
        return (stamp1.time() == stamp2.time()
                && stamp1.moduleNid() == stamp2.moduleNid()
                && stamp1.pathNid() == stamp2.pathNid());
    }

    public void updateView() {
        if (getViewProperties() == null || getEntityFacade() == null) return;

        // Clear VBox of rows
        this.changeChronologyPane.getChildren().clear();

        ViewCalculator viewCalculator = viewProperties.calculator();

        // Read data to predetermine all paths and all modules.
        // 1. Listen for range selection/or all
        //       show filtered
        // 2. Listen for a date point selected
        //       items above filtered will be faded
        //       highlight (green) around selected item
        // 3. Color left side of list item.
        // 4. Generate module ids, names and paths to populate filter dialog

        final int entityNid = getEntityFacade().nid();
        // User selects All or Concepts display
        if (isFilterSelected("All", "Concept")) {
            // Populate concept versions
            ChangeChronology conceptChronologyChange = viewCalculator.changeChronology(entityNid);
            List<Pane> rows = generateRows(getViewProperties(), getEntityFacade().nid(), conceptChronologyChange);
            this.changeChronologyPane.getChildren().addAll(rows);
        }

        if (isFilterSelected("All", "Description")) {
            // Populate all description semantics of this entity.
            ImmutableList<SemanticEntity> descrSemanticEntities = viewCalculator.getDescriptionsForComponent(entityNid);
            // For each populate a row based on changes
            descrSemanticEntities.forEach(semanticEntity -> {
                ChangeChronology changeChronology = viewCalculator.changeChronology(semanticEntity.nid());
                List<Pane> rows = generateRows(viewProperties, semanticEntity.nid(), changeChronology);
                this.changeChronologyPane.getChildren().addAll(rows);
            });
        }

        // TODO Axioms (displaying things correctly?)
        if (isFilterSelected("All", "Axiom")) {
            // Inferred Axioms
            Latest<SemanticEntityVersion> inferredSemanticVersion = viewCalculator.getInferredAxiomSemanticForEntity(entityNid);
            inferredSemanticVersion.ifPresent(semanticEntityVersion -> {
                ChangeChronology axiomInferredChange = viewCalculator.changeChronology(semanticEntityVersion.nid());
                List<Pane> rows = generateRows(viewProperties, semanticEntityVersion.nid(), axiomInferredChange);
                this.changeChronologyPane.getChildren().addAll(rows);
            });

            // Stated Axioms
            Latest<SemanticEntityVersion> statedSemanticVersion = viewCalculator.getStatedAxiomSemanticForEntity(entityNid);
            statedSemanticVersion.ifPresent(semanticEntityVersion -> {
                ChangeChronology axiomStatedChange = viewCalculator.changeChronology(semanticEntityVersion.nid());
                List<Pane> rows2 = generateRows(viewProperties, semanticEntityVersion.nid(), axiomStatedChange);
                this.changeChronologyPane.getChildren().addAll(rows2);
            });
        }

        // This will cache all displayed rows.
        cacheOfRows.clear();
        cacheOfRows.addAll(this.changeChronologyPane.getChildren());
    }

    public List<Pane> generateRows(final ViewProperties viewProperties, int entityNid, ChangeChronology changeChronology) {
        List<Pane> paneList = new ArrayList<>();
        for (VersionChangeRecord changeRecord:changeChronology.changeRecords()) {
            FXMLLoader loader = new FXMLLoader(getClass().getResource(DESCRIPTION_LIST_ITEM_FXML_FILE));
            Pane listItem = null;
            try {
                listItem = loader.load();
            } catch (IOException e) {
                throw new RuntimeException(e);
            }

            StampEntity<? extends StampVersion> stamp = Entity.getStamp(changeRecord.stampNid());
            String pathName = viewProperties.calculator().getPreferredDescriptionTextWithFallbackOrNid(stamp.pathNid());
            ChangeListItemController changeListItemController = loader.getController();
            ChangeCoordinate changeCoordinate = new ChangeCoordinate(pathName, stamp.moduleNid(), changeRecord);

            // Update View (each row)
            changeListItemController.updateModel(viewProperties, entityNid, changeCoordinate);
            changeListItemController.updateView();

            // add user data
            listItem.setUserData(changeCoordinate);

//            The following is how to programmatically set a color for the vertical bar beside row.
            int index = pathMap.get(pathName).stream().toList().indexOf(stamp.moduleNid());
            if (index > -1 && index < COLORS_FOR_EXTENSIONS.length) {
                changeListItemController.setExtensionVLineColor(COLORS_FOR_EXTENSIONS[index]);
            }

            // Programmatically change CSS Theme
            String styleSheet = defaultStyleSheet();
            listItem.getStylesheets().add(styleSheet);
            paneList.add(listItem);

        }
        return paneList;
    }

    @FXML
    void filterVersonRecords(InputMethodEvent event) {

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

    public void filterByRange(Set<ChangeCoordinate> changeCoordinates) {
        List<Node> remain = this.cacheOfRows.stream().filter(node -> changeCoordinates.contains(node.getUserData())).toList();
        // TODO performance and behavior is to add what's missing leave rows already in view. instead of clearing.
        this.changeChronologyPane.getChildren().clear();
        this.changeChronologyPane.getChildren().addAll(remain);
    }

    public void unfilterByRange() {
        this.changeChronologyPane.getChildren().clear();
        this.changeChronologyPane.getChildren().addAll(cacheOfRows);

    }
}
