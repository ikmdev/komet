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
import dev.ikm.komet.framework.view.ViewProperties;
import dev.ikm.tinkar.coordinate.stamp.calculator.Latest;
import dev.ikm.tinkar.coordinate.stamp.change.ChangeChronology;
import dev.ikm.tinkar.coordinate.stamp.change.VersionChangeRecord;
import dev.ikm.tinkar.coordinate.view.calculator.ViewCalculator;
import dev.ikm.tinkar.entity.SemanticEntity;
import dev.ikm.tinkar.entity.SemanticEntityVersion;
import dev.ikm.tinkar.terms.EntityFacade;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
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
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import static dev.ikm.komet.amplify.commons.CssHelper.defaultStyleSheet;

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

    @FXML
    public void initialize()  {
        clearView();
        changeFilterChoiceBox.getItems().addAll("All", "Concept", "Description", "Axiom");
        changeFilterChoiceBox.setValue("All");
        changeFilterChoiceBox.valueProperty().addListener( (obs, oldVal, newVal) ->
            updateView(getViewProperties(), getEntityFacade()));

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
    public void updateView(final ViewProperties viewProperties, final EntityFacade entityFacade) {
        if (viewProperties == null || entityFacade == null) return;
        this.viewProperties = viewProperties;
        this.entityFacade = entityFacade;
        // Clear VBox of rows
        this.changeChronologyPane.getChildren().clear();

        ViewCalculator viewCalculator = viewProperties.calculator();

        // User selects All or Concepts display
        if (isFilterSelected("All", "Concept")) {
            // Populate concept versions
            ChangeChronology conceptChronologyChange = viewCalculator.changeChronology(entityFacade.nid());
            List<Pane> rows = generateRows(viewProperties, entityFacade.nid(), conceptChronologyChange);
            this.changeChronologyPane.getChildren().addAll(rows);
        }

        if (isFilterSelected("All", "Description")) {
            // Populate all description semantics of this entity.
            ImmutableList<SemanticEntity> descrSemanticEntities = viewCalculator.getDescriptionsForComponent(entityFacade.nid());
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
            Latest<SemanticEntityVersion> inferredSemanticVersion = viewCalculator.getInferredAxiomSemanticForEntity(entityFacade.nid());
            inferredSemanticVersion.ifPresent(semanticEntityVersion -> {
                ChangeChronology axiomInferredChange = viewCalculator.changeChronology(semanticEntityVersion.nid());
                List<Pane> rows = generateRows(viewProperties, semanticEntityVersion.nid(), axiomInferredChange);
                this.changeChronologyPane.getChildren().addAll(rows);
            });

            // Stated Axioms
            Latest<SemanticEntityVersion> statedSemanticVersion = viewCalculator.getStatedAxiomSemanticForEntity(entityFacade.nid());
            statedSemanticVersion.ifPresent(semanticEntityVersion -> {
                ChangeChronology axiomStatedChange = viewCalculator.changeChronology(semanticEntityVersion.nid());
                List<Pane> rows2 = generateRows(viewProperties, semanticEntityVersion.nid(), axiomStatedChange);
                this.changeChronologyPane.getChildren().addAll(rows2);
            });
        }
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
            ChangeListItemController changeListItemController = loader.getController();

            changeListItemController.updateView(viewProperties, entityNid, changeRecord);
//            The following is how to programmatically set a color for the vertical bar beside row.
//            changeListItemController.setExtensionVLineColor(COLORS_FOR_EXTENSIONS[1]);

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
}
